package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.Writer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import phenoscape.queries.lib.CountTable;
import phenoscape.queries.lib.EQPair;
import phenoscape.queries.lib.PhenotypeScoreTable;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.ProfileScoreSet;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;



public class PhenotypeProfileAnalysis {

	private static final String CAROROOT = "CARO:0000000";
	private static final String TAOROOT = "TAO:0100000";
	private static final String PATOROOT = "PATO:0000001";
	private static final String TTOROOT = "TTO:0";
	private static final String OSTARIOCLUPEOMORPHAROOT = "TTO:253";
	private static final String ASPIDORASROOT = "TTO:105426";
	private static final String AMIIDAEROOT = "TTO:10360";
	private static final String CALLICHTHYIDAEROOT = "TTO:11200";
	private static final String SILURIFORMESROOT = "TTO:1380";
	private static final String CHEIRODONROOT =  "TTO:102205";
	private String ANALYSISROOT = OSTARIOCLUPEOMORPHAROOT;
	
	private static final double IC_CUTOFF =  0.0;


	private static final String TAXONREPORTFILENAME = "../TaxonVariationReport.txt";
	private static final String GENEREPORTFILENAME = "../GeneVariationReport.txt";
	private static final String PHENOTYPEMATCHREPORTFILENAME = "../PhenotypeMatchReport.txt";
	private static final String PROFILEMATCHREPORTFILENAME = "../ProfileMatchReport.txt";


	private static final String TAXONQUERY = "SELECT taxon.node_id,link.node_id, phenotype.node_id,phenotype.entity_node_id, phenotype.entity_uid, phenotype.quality_node_id,phenotype.quality_uid,phenotype.uid,simple_label(phenotype.node_id),simple_label(phenotype.entity_node_id),simple_label(phenotype.quality_node_id) FROM link " +
	"JOIN taxon ON (taxon.node_id = link.node_id AND taxon.node_id = ? AND link.predicate_id = (select node_id FROM node WHERE uid = 'PHENOSCAPE:exhibits'))" +
	"JOIN phenotype ON (link.object_id = phenotype.node_id) WHERE is_inferred = false";

	private static final String GENEANNOTATIONQUERY = 		
		"SELECT gene_node_id, gene_uid, gene_label, dga.phenotype_node_id, p1.entity_node_id, p1.entity_uid, p1.quality_node_id, p1.quality_uid,p1.uid,simple_label(dga.phenotype_node_id), simple_label(p1.entity_node_id),simple_label(p1.quality_node_id) FROM distinct_gene_annotation AS dga " +
		"JOIN phenotype AS p1 ON (p1.node_id = dga.phenotype_node_id)";


	private static final String GENEPHENOTYPECOUNTQUERY =
		"SELECT count(*) FROM distinct_gene_annotation  WHERE distinct_gene_annotation.phenotype_node_id IN " +
		"(SELECT phenotype.node_id from phenotype " +
		"JOIN link phenotype_inheres_in_part_of ON (phenotype_inheres_in_part_of.node_id = phenotype.node_id AND phenotype_inheres_in_part_of.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:inheres_in_part_of')) " +
		"JOIN link quality_is_a ON (quality_is_a.node_id = phenotype.node_id AND quality_is_a.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a')) " +
		"WHERE (phenotype_inheres_in_part_of.object_id = ?  AND quality_is_a.object_id = ?))";

	
	Map<Integer,Profile> taxonProfiles;  //taxon_node_id -> Phenotype profile for taxon
	Map<Integer,Profile> geneProfiles = new HashMap<Integer,Profile>();   //gene_node_id -> Phenotype profile for gene


	Map<Integer,Integer>childDist = new HashMap<Integer,Integer>();

	/**
	 * These two are (at present) strictly for generating the intermediate reports
	 */
	VariationTable taxonVariation = new VariationTable();    
	VariationTable geneVariation = new VariationTable();

	/**
	 * This maps qualities to attributes
	 */
	final Map<Integer,Integer> attributeMap;

	/**
	 * This holds the node ids for each attribute in the character slim + quality
	 */
	final Set<Integer> attributeSet = new HashSet<Integer>(15);

	/**
	 * These hold usage counts for each quality that fails to map to an attribute.  These counts appear the appropriate reports.
	 */
	Map<Integer,Integer> badTaxonQualities = new HashMap<Integer,Integer>();
	Map<Integer,Integer> badGeneQualities = new HashMap<Integer,Integer>();

	/**
	 * This holds the node id for the term 'quality.'
	 */
	final int qualityNodeID;

	static Logger logger = Logger.getLogger(PhenotypeProfileAnalysis.class.getName());

	public PhenotypeProfileAnalysis(Utils u) throws SQLException{
		logger.info("Caching entity UIDs and labels");
		u.cacheEntities();    // this will retrieve entities that appear in EQ's
		
		logger.info("Setting up Attribute table");
		qualityNodeID = u.getQualityNodeID();   //set to the root of PATO
		attributeMap = u.setupAttributes();

		attributeSet.addAll(attributeMap.values());		
		attributeSet.add(qualityNodeID);
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		Utils u = new Utils();
		String kbName;
		try {
			kbName = u.openKB();
		} catch (SQLException e) {
			logger.fatal("Failed to open KB; will exit.  Reason was " + e.toString());
			return;
		}
		PhenotypeProfileAnalysis listQuery;
		try{
			listQuery = new PhenotypeProfileAnalysis(u);
		} catch (SQLException e) {
			logger.fatal("Failed during initialization; will exit.  Reason was " + e.toString());
			return;			
		}
		File outFile1 = new File(TAXONREPORTFILENAME);
		File outFile2 = new File(GENEREPORTFILENAME);
		File outFile3 = new File(PHENOTYPEMATCHREPORTFILENAME);
		File outFile4 = new File(PROFILEMATCHREPORTFILENAME);
		Writer w1 = null;
		Writer w2 = null;
		Writer w3 = null;
		Writer w4 = null;
		Date today;
		DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT);
		today = new Date();
		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
		String timeStamp = dateFormatter.format(today) + " " + timeFormatter.format(today) + " on " + kbName;		
		try {
			w1 = new BufferedWriter(new FileWriter(outFile1));
			w2 = new BufferedWriter(new FileWriter(outFile2));
			w3 = new BufferedWriter(new FileWriter(outFile3));
			w4 = new BufferedWriter(new FileWriter(outFile4));
			u.writeOrDump(timeStamp, w1);
			u.writeOrDump(timeStamp, w2);
			u.writeOrDump(timeStamp, w3);
			u.writeOrDump(timeStamp, w4);
			listQuery.process(u, w1, w2, w3, w4);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				u.closeKB();
				w1.close();
				w2.close();
				w3.close();
				w4.close();
			} catch (Exception e) {
				System.err.print("An exception occurred while closing a report file or database connection");
				e.printStackTrace();
			}
		}
	}


	void process(Utils u,Writer w1, Writer w2, Writer w3, Writer w4) throws IOException,SQLException{
		// process taxa annotations
		logger.info("Building Taxonomy Tree");
		TaxonomyTree t = new TaxonomyTree(ANALYSISROOT,u);
		t.traverseOntologyTree(u);
		taxonProfiles = processTaxonVariation(t,u, w1);
		taxonVariation = traverseTaxonomy(t, t.getRootNodeID(), taxonProfiles, u, w1);
		flushUnvaryingPhenotypes(taxonProfiles,taxonVariation);

		if (taxonProfiles.isEmpty()){
			throw new RuntimeException("No taxa in Profile Set");
		}
		t.report(u, w1);
		taxonVariation.variationReport(u,w1);	

		processGeneExpression(u, w2);
		geneVariation.variationReport(u, w2);
		u.writeOrDump("\nList of qualities that were placed under quality as an attribute by default\n", w2);
		for(Integer bad_id : badGeneQualities.keySet()){
			u.writeOrDump(u.getNodeName(bad_id) + " " + badGeneQualities.get(bad_id), w2);
		}

		w2.close();

		/* These need to happen after the profiles have been constructed, since we don't want to count taxon annotations that don't reflect change */
		CountTable phenotypeCountsForTaxa = new CountTable();  
		CountTable phenotypeCountsForGenes = new CountTable();
		CountTable phenotypeCountsCombined = new CountTable();

		Map <Integer,Set<Integer>> entityParentCache = new HashMap<Integer,Set<Integer>>();
		logger.info("Building entity parents of taxon phenotypes");
		buildEntityParents(entityParentCache,taxonProfiles,u);
		

		logger.info("Building entity parents of gene phenotypes");
		buildEntityParents(entityParentCache,geneProfiles,u);

		/* Test introduction of phenotypeParentCache, which should map an attribute level EQ to all its parents via inheres_in_part_of entity parents and is_a quality parents (cross product) */
		Map <EQPair,Set<EQPair>> phenotypeParentCache = new HashMap<EQPair,Set<EQPair>>();
		buildEQParents(phenotypeParentCache,taxonProfiles,u);
		buildEQParents(phenotypeParentCache,geneProfiles,u);
		
		//fillCountTable(taxonProfiles, phenotypeCountsForTaxa,phenotypeParentCache, u, TAXONPHENOTYPECOUNTQUERY, u.countAssertedTaxonPhenotypeAnnotations());
		fillCountTable(geneProfiles, phenotypeCountsForGenes,phenotypeParentCache, u, GENEPHENOTYPECOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		
		//sumCountTables(phenotypeCountsCombined,phenotypeCountsForTaxa,phenotypeCountsForGenes);

		CountTable phenotypeCountsToUse = phenotypeCountsForGenes;
		
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();


		logger.info("Done building entity parents; building phenotype match cache");
		int attOverlaps = buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, phenotypeCountsToUse, u);
		u.writeOrDump("gene and taxon profiles overlapping on an attribute:  " + attOverlaps,w1);
		w1.close();
		logger.info("Finished building phenotype match cache; Writing Phenotype match summary");
		writePhenotypeMatchSummary(phenotypeScores,u,w3);		
		w3.close();

		logger.info("Finished Writing Phenotype match summary");
		logger.info("Calculating Profile Scores");

		//List<ProfileScoreSet> results = new ArrayList<ProfileScoreSet>(1000);
		u.writeOrDump("Taxon \t Gene \t taxon phenotypes \t gene phenotypes \t maxIC \t iccs \t simIC \t simJ",w4);

		long zeroCount = 0;
		//u.writeOrDump("Sizes: Taxon profiles: " + taxonProfiles.keySet().size() + "; Gene profiles: " + geneProfiles.keySet().size(), null);
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			for(Integer currentGene : geneProfiles.keySet()){
				Profile currentGeneProfile = geneProfiles.get(currentGene);
				ProfileScoreSet result = new ProfileScoreSet(currentTaxon, currentGene,currentTaxonProfile.getAllPhenotypes(), currentGeneProfile.getAllPhenotypes());
				// calculate maxIC
				double maxIC = calcMaxIC(currentTaxonProfile, currentGeneProfile,phenotypeScores);
				result.setMaxICScore(maxIC);

				//calculate ICCS score for this pair of profiles
				double iccs = calcICCS(currentTaxonProfile, currentGeneProfile, phenotypeScores);
				result.setICCSScore(iccs);

				//calculate simIC score for this pair of profiles
				//double simICScore = calcSimIC(currentTaxonProfile, currentGeneProfile, phenotypeScores, phenotypeCountsToUse);
				result.setSimICScore(-1.0);

				//calculate simJ score for this pair of profiles
				//double simJScore = calcSimJ(currentTaxonProfile, currentGeneProfile, phenotypeScores, phenotypeCountsToUse);
				result.setSimJScore(-1.0);

				if (result.isNonZero())
					result.writeScores(u, w4);
				else
					zeroCount++;
			}
		}
		u.writeOrDump("Pairs with zero score = " + zeroCount, w4);
		w4.close();
		logger.info("Done");
	}

	double calcMaxIC(Profile taxonProfile, Profile geneProfile, PhenotypeScoreTable phenotypeScores){
		double maxPhenotypeMatch = Double.NEGATIVE_INFINITY;
		for(Integer att : attributeSet){
			for (Integer tEntity : taxonProfile.getUsedEntities()){
				for (Integer gEntity : geneProfile.getUsedEntities()){
					if(phenotypeScores.hasScore(tEntity, gEntity, att))
						if (phenotypeScores.getScore(tEntity, gEntity, att) > maxPhenotypeMatch){
							maxPhenotypeMatch = phenotypeScores.getScore(tEntity, gEntity, att);
						}
				}
			}
		}
		return maxPhenotypeMatch;
	}

	double calcICCS(Profile taxonProfile, Profile geneProfile, PhenotypeScoreTable phenotypeScores){
		List<Double> maxByTaxon = new ArrayList<Double>();
		for(Integer tEntity : taxonProfile.getUsedEntities()){
			for (Integer att : attributeSet){
				double bestIC = 0.0;
				for (Integer gEntity : geneProfile.getUsedEntities()){
					if (phenotypeScores.hasScore(tEntity, gEntity, att)) {
						if (phenotypeScores.getScore(tEntity, gEntity, att) > bestIC){
							bestIC = phenotypeScores.getScore(tEntity, gEntity, att);
						}
					}
				}
				maxByTaxon.add(bestIC);
			}
		}
		double sum =0;
		for(Double s : maxByTaxon){
			sum += s.doubleValue();
		}
		return sum/((double)maxByTaxon.size());
	}

	private double calcSimIC(Profile taxonProfile, Profile geneProfile, PhenotypeScoreTable phenotypeScores, CountTable phenotypeCounts){
		double simICScore = 0;
		double matchSum = 0;
		double totalSum = 0;
		for(Integer att : attributeSet){
			for (Integer tEntity : taxonProfile.getUsedEntities()){
				for (Integer gEntity : geneProfile.getUsedEntities()){
					if(phenotypeScores.hasScore(tEntity, gEntity, att))
						matchSum += phenotypeScores.getScore(tEntity, gEntity, att);
				}
			}
			for (Integer tEntity : taxonProfile.getUsedEntities()){
				totalSum += phenotypeCounts.getIC(tEntity, att);
			}
			for (Integer gEntity : geneProfile.getUsedEntities()){
				totalSum += phenotypeCounts.getIC(gEntity,att);
			}
		}
		if (totalSum == 0){
			throw new RuntimeException("Unexpected value in simIC: matchSum = " + matchSum + "; totalSum = " + totalSum);
		}
		simICScore = matchSum/totalSum;
		return simICScore;
	}

	private double calcSimJ(Profile taxonProfile, Profile geneProfile, PhenotypeScoreTable phenotypeScores, CountTable phenotypeCounts){
		int matchCount = 0;
		int totalCount = 0;
		for(Integer att : attributeSet){
			for (Integer tEntity : taxonProfile.getUsedEntities()){
				for (Integer gEntity : geneProfile.getUsedEntities()){
					if(phenotypeScores.hasScore(tEntity, gEntity, att))
						matchCount++;
				}
			}
			for (Integer tEntity : taxonProfile.getUsedEntities()){
				if (phenotypeCounts.hasCount(tEntity, att))
					totalCount++;
			}
			for (Integer gEntity : taxonProfile.getUsedEntities()){
				if (phenotypeCounts.hasCount(gEntity, att))
					totalCount++;
			}
		}
		return ((double)matchCount)/((double)totalCount);	

	}





	/**
	 * 
	 * @param t 
	 * @param u
	 * @param reportWriter
	 * @throws SQLException
	 */
	HashMap<Integer,Profile>  processTaxonVariation(TaxonomyTree t, Utils u, Writer reportWriter) throws SQLException{	
		HashMap<Integer,Profile> taxonProfileSet = new HashMap<Integer,Profile>();
		int emptyCount = 0;
		int childCount = 0;
		Set<Integer> taxonSet = t.getAllTaxa();

		for (Integer taxonID : taxonSet){
			final PreparedStatement p1 = u.getPreparedStatement(TAXONQUERY);
			p1.setInt(1, taxonID);
			ResultSet linkResults = p1.executeQuery();
			Profile myProfile = new Profile();
			while(linkResults.next()){
				final int taxon_id = linkResults.getInt(1);
				final int link_id = linkResults.getInt(2);
				final int phenotype_id = linkResults.getInt(3);
				final int entity_id = linkResults.getInt(4);
				final String entity_uid = linkResults.getString(5);
				final int quality_id = linkResults.getInt(6);
				final String quality_uid = linkResults.getString(7);
				final String phenotype_uid = linkResults.getString(8);
				final String phenotype_label = linkResults.getString(9);
				final String entity_label = linkResults.getString(10);
				final String quality_label = linkResults.getString(11);
				u.putNodeUIDName(phenotype_id, phenotype_uid,phenotype_label);
				if (attributeMap.containsKey(quality_id)){
					final int attribute_id = attributeMap.get(quality_id);
					myProfile.addPhenotype(entity_id,attribute_id, phenotype_id);
				}
				else{
					myProfile.addPhenotype(entity_id,qualityNodeID, phenotype_id);
					if (badTaxonQualities.containsKey(quality_id)){
						badTaxonQualities.put(quality_id, badTaxonQualities.get(quality_id).intValue()+1);
					}
					else {
						badTaxonQualities.put(quality_id, 1);
						u.putNodeUIDName(quality_id, quality_uid, quality_label);
					}
				}
				u.putNodeUIDName(entity_id, entity_uid,entity_label);
			}
			taxonProfileSet.put(taxonID,myProfile);
		}
		return taxonProfileSet;
	}


	/**
	 * This method marks taxon phenotypes that display variation. It does this in two passes: the first builds sets of phenotypes for each taxon.
	 * These sets represent the union of all phenotypes of child taxa and any phenotypes asserted for the particular taxon.  More precisely, 
	 * these sets represented as profiles that break down the sets of phenotypes by entity and attribute.  The second pass compares the set of 
	 * phenotypes with the set from each child.  If the child differs from the union, there is variation within the entity attribute set and this
	 * is recorded in the taxonVariation table.  There are two additional twists: the absence of an annotation for a particular entity-attribute
	 * combination in a child taxon is treated as variation if there are sister taxa with annotation for the same combination, but if the taxon
	 * has no annotations whatsoever, it is ignored.
	 * @param t
	 * @param taxon
	 * @param profiles
	 * @param u
	 * @param reportWriter
	 */
	VariationTable traverseTaxonomy(TaxonomyTree t, Integer taxon, Map<Integer, Profile> profiles, Utils u, Writer reportWriter){
		VariationTable result = new VariationTable();
		if (t.nodeIsInternal(taxon, u)){
			//build set of children
			final Set<Integer> children = t.getTable().get(taxon);
			Set<Profile> childProfiles = new HashSet<Profile>();
			for(Integer child : children){
				traverseTaxonomy(t,child,profiles, u,reportWriter);
				childProfiles.add(profiles.get(child));
			}			
			//This builds the inferred (upwards) sets annotations for each taxon with childProfiles
			//Changed to propagate the intersection rather than the union 21 Feb
			Profile parentProfile = profiles.get(taxon);
			for (Profile childProfile : childProfiles){
				if (!childProfile.isEmpty()){
					for (Integer ent : childProfile.getUsedEntities()){
						for (Integer att : childProfile.getUsedAttributes()){
							if (childProfile.hasPhenotypeSet(ent,att)){
								parentProfile.retainAllFromPhenotypeSet(ent, att, childProfile.getPhenotypeSet(ent, att));
							}
						}
					}
				}
			}
	
		//at this point, the inferredTaxonProfile contains the union of the inferred profiles of the children
		//second pass to check for interesting variation
			for (Profile childProfile : childProfiles){
				if (!childProfile.isEmpty()){       //if nothing has been reported for this child then absent annotations are uninformative
					for (Integer ent : parentProfile.getUsedEntities()){
						for (Integer att : parentProfile.getUsedAttributes()){
							if (parentProfile.hasPhenotypeSet(ent, att)){
								if (childProfile.hasPhenotypeSet(ent, att)){
								//System.out.println("att = " + att.intValue() + " ;ent = " + ent.intValue() + " ;parent profile size " + currentTaxonProfile.getPhenotypeSet(att,ent).size() + " ; child profile size" + childProfile.getPhenotypeSet(att, ent).size());
									if (parentProfile.getPhenotypeSet(ent,att).size() != childProfile.getPhenotypeSet(ent,att).size()){
										result.addExhibitor(ent, att, taxon);
									}
								}
								else
									result.addExhibitor(ent, att, taxon);
							}
						}
					}
				}
			}
		}
		return result;
	}
	

	/**
	 * This method removes all phenotypes that don't indicate variation from the profile.
	 */
	void flushUnvaryingPhenotypes(Map<Integer,Profile> taxonProfileSet, VariationTable variation){
		for (Integer taxon : taxonProfileSet.keySet()){
			Profile p = taxonProfileSet.get(taxon);
			Set<Integer> entitySet = new HashSet<Integer>();
			Set<Integer> attributeSet = new HashSet<Integer>();
			entitySet.addAll(p.getUsedEntities());
			attributeSet.addAll(p.getUsedAttributes());
			for (Integer ent : entitySet){
				for (Integer att : attributeSet){
					if (p.hasPhenotypeSet(ent, att)){
						if (!variation.taxonExhibits(ent,att,taxon)){
							p.clearPhenotypeSet(ent, att);
						}
					}
				}
			}
			p.removeAllEmpties();
		}
		Set<Integer> taxa = new HashSet<Integer>();
		taxa.addAll(taxonProfileSet.keySet());
		for (Integer taxon : taxa){
			if (taxonProfileSet.get(taxon).isEmpty())
			taxonProfileSet.remove(taxon);
		}
	}

	/**
	 * Name is a little dicy, but better than GeneVariation
	 */
	void processGeneExpression(Utils u, Writer reportWriter) throws SQLException{
		int annotationCount = 0;
		int usableAnnotationCount = 0;
		Set<Integer>uniqueGenes = new HashSet<Integer>();

		final Statement s2 = u.getStatement();
		ResultSet annotationResults = s2.executeQuery(GENEANNOTATIONQUERY);

		while(annotationResults.next()){
			final int geneID = annotationResults.getInt(1);
			final String gene_uid = annotationResults.getString(2);
			final String gene_label = annotationResults.getString(3);
			final int phenotype_id = annotationResults.getInt(4);
			final int entity_id = annotationResults.getInt(5);
			final String entity_uid = annotationResults.getString(6);
			final int quality_id = annotationResults.getInt(7);
			final String quality_uid = annotationResults.getString(8);
			final String phenotype_uid = annotationResults.getString(9);
			final String phenotype_label = annotationResults.getString(10);
			final String entity_label = annotationResults.getString(11);
			final String quality_label = annotationResults.getString(12);
			u.putNodeUIDName(phenotype_id, phenotype_uid, phenotype_label);
			annotationCount++;
			uniqueGenes.add(geneID);
//			if (gene_label.equals("ttna")){
//				System.out.println("gene: " + gene_label + "; entity_uid: " + entity_uid + "; phenotype_uid " + phenotype_uid);
//			}
			if (attributeMap.containsKey(quality_id)){
				final int attribute_id = attributeMap.get(quality_id);
				if (geneProfiles.containsKey(geneID)){
					geneProfiles.get(geneID).addPhenotype(entity_id,attribute_id, phenotype_id);
				}
				else {
					geneProfiles.put(geneID, new Profile());
					geneProfiles.get(geneID).addPhenotype(entity_id,attribute_id, phenotype_id);
				}
				geneVariation.addExhibitor(entity_id, attribute_id, geneID);
				usableAnnotationCount++;
			}
			else{
				if (geneProfiles.containsKey(geneID)){
					geneProfiles.get(geneID).addPhenotype(entity_id, qualityNodeID, phenotype_id);
				}
				else {
					geneProfiles.put(geneID, new Profile());
					geneProfiles.get(geneID).addPhenotype(entity_id, qualityNodeID, phenotype_id);
				}
				geneVariation.addExhibitor(entity_id, qualityNodeID, geneID);
				if (badGeneQualities.containsKey(quality_id)){
					badGeneQualities.put(quality_id, badGeneQualities.get(quality_id).intValue()+1);
				}
				else {
					badGeneQualities.put(quality_id, 1);
				}
			}
			u.putNodeUIDName(quality_id, quality_uid,quality_label);
			u.putNodeUIDName(entity_id, entity_uid, entity_label);
			u.putNodeUIDName(geneID, gene_uid,gene_label);
		}

		u.writeOrDump("Raw Genes " + uniqueGenes.size() + "; Distinct Gene annotations " + annotationCount + "; Count of genes with annotations " + geneProfiles.keySet().size() + "; Annotations to attributes other than Quality " + usableAnnotationCount, reportWriter);

	}





	/**
	 * For each phenotype in the taxonProfile, this builds the set of entity parents.
	 * Changed to save the parents (iipo parents) indexed by the entity, which is more efficient and useful later on.
	 * 
	 * @param entityParentCache
	 * @param u
	 * @throws SQLException
	 */
	void buildEntityParents(Map <Integer,Set<Integer>> entityParentCache, Map<Integer,Profile> profiles, Utils u) throws SQLException{
		for(Integer currentExhibitor : profiles.keySet()){
			Profile currentProfile = profiles.get(currentExhibitor);
			Set<Integer> currentAttributes = currentProfile.getUsedAttributes();
			Set<Integer> currentEntities = currentProfile.getUsedEntities();
			for (Integer att : currentAttributes){
				for (Integer ent : currentEntities){
					if (currentProfile.hasPhenotypeSet(ent, att)){
						for (Integer pheno : currentProfile.getPhenotypeSet(ent, att)){
							if (!entityParentCache.containsKey(ent)){
								Set<Integer>parentSet = u.collectEntityParents(pheno);  //pass phenotype id, list of entities returned
								entityParentCache.put(ent, parentSet);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * For each phenotype in the taxonProfile, this builds the set of EQ class expression parents.
	 * Changed to save the parents (iipo parents) indexed by the entity, which is more efficient and useful later on.
	 * 
	 * @param entityParentCache
	 * @param u
	 * @throws SQLException
	 */
	void buildEQParents(Map<EQPair,Set<EQPair>> phenotypeParentCache, Map<Integer,Profile> profiles, Utils u) throws SQLException{
		for(Profile currentProfile : profiles.values()){
			Set <Integer> usedEntities = currentProfile.getUsedEntities();
			Set <Integer> usedAttributes = currentProfile.getUsedAttributes();
			for(Integer profileEntity : usedEntities){
				for (Integer curAttribute : usedAttributes){
					EQPair curEQ = new EQPair(profileEntity,curAttribute);
					if (!phenotypeParentCache.containsKey(curEQ)){
						Set<EQPair> eqParentSet = new HashSet<EQPair>();  //pass phenotype id, list of entities returned
						phenotypeParentCache.put(curEQ,eqParentSet);
						Set<Integer> phenoSet = currentProfile.getPhenotypeSet(profileEntity, curAttribute);
						if (phenoSet != null && !phenoSet.isEmpty()){
							Integer pheno;
							Iterator<Integer> phenoI = phenoSet.iterator();
							pheno = phenoI.next();
							Set<Integer> entityParentSet = u.collectEntityParents(pheno);
							Set<Integer> qualityParentSet = u.collectQualityParents(curAttribute);
							if (entityParentSet.isEmpty() || qualityParentSet.isEmpty()){
								curEQ.fillNames(u);
								logger.info("Failed to add Parents of: " + curEQ);
								if (entityParentSet.isEmpty() && qualityParentSet.isEmpty())
									logger.info("Because both parent sets are empty");
								else if (entityParentSet.isEmpty())
									logger.info("Because the entity parent set is empty");
								else
									logger.info("Because the parent set of " + curAttribute + " is empty");
							}
							for(Integer entParent : entityParentSet){
								for(Integer qualParent : qualityParentSet){
									EQPair newParentEQ = new EQPair(entParent,qualParent);
									String bestID = newParentEQ.getFullName(u);
									eqParentSet.add(newParentEQ);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param profiles
	 * @param counts
	 * @param parents
	 * @throws SQLException 
	 */
	void fillCountTable(Map<Integer,Profile> profiles, CountTable counts,Map <EQPair,Set<EQPair>> phenotypeParentCache, Utils u, String query,int annotationCount) throws SQLException{
		counts.setSum(annotationCount);
		final PreparedStatement p = u.getPreparedStatement(query);
		for(Profile currentProfile : profiles.values()){
			Set <Integer> usedEntities = currentProfile.getUsedEntities();
			Set <Integer> usedAttributes = currentProfile.getUsedAttributes();
			for(Integer profileEntity : usedEntities){
				for (Integer curAttribute : usedAttributes){
					EQPair curEQ = new EQPair(profileEntity,curAttribute);
					Set<EQPair> allParents = phenotypeParentCache.get(curEQ);
					if (allParents == null){
						logger.error("The EQ pair " + curEQ + " seems to have no parents");
					}
					else {
						curEQ.fillNames(u);
						//logger.info("Processing " + curEQ);
						for(EQPair eqParent : allParents){
							if (!counts.hasCount(eqParent.getEntity(), eqParent.getQuality())){
								p.setInt(1, eqParent.getEntity());
								p.setInt(2, eqParent.getQuality());
								ResultSet eaResult = p.executeQuery();
								if(eaResult.next()){
									int count = eaResult.getInt(1);
									counts.addCount(eqParent.getEntity(), eqParent.getQuality(), count);
								}
							}
						}
					}
				}
			}
		}		
	}

	/**
	 * 
	 */
	void sumCountTables(CountTable sum, CountTable table1, CountTable table2){
		for(Integer entity : table1.getEntities()){
			Set<Integer>qualSet = table1.getQualitiesForEntity(entity);
			for (Integer quality : qualSet){
				int count1 = table1.getRawCount(entity, quality);
				if (table2.hasCount(entity, quality)){
					int count2 = table2.getRawCount(entity, quality);
					sum.addCount(entity, quality, count1+count2);
				}
				else{
					sum.addCount(entity, quality, count1);
				}
			}
		}
		for(Integer entity : table2.getEntities()){
			Set<Integer>qualSet = table2.getQualitiesForEntity(entity);
			for (Integer quality : qualSet){
				if (!sum.hasCount(entity, quality)){
					sum.addCount(entity, quality, table2.getRawCount(entity, quality));
				}
			}
		}
		sum.setSum(table1.getSum() + table2.getSum());
	}




	/**
	 * This is now correct - it calculates match scores of subsuming phenotypes where the quality is at the attribute level.
	 * @param phenotypeParentCache maps entities to parent entities obtained from inheres_in_part_of parents of a (any) phenotype with the specified entity
	 * @param phenotypeScores
	 * @param entityCounts
	 * @param u
	 * @return
	 */
	int buildPhenotypeMatchCache(Map <EQPair,Set<EQPair>> phenotypeParentCache, PhenotypeScoreTable phenotypeScores, CountTable eaCounts, Utils u){
		int attOverlaps = 0;
		for (Integer curAtt : attributeSet){
			for(Integer currentTaxon : taxonProfiles.keySet()){
				Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
				if (!currentTaxonProfile.isEmpty()){
					for (Integer taxonEntity : currentTaxonProfile.getUsedEntities()){
						EQPair taxonEQ = new EQPair(taxonEntity,curAtt);
						Set<EQPair> teParents = phenotypeParentCache.get(taxonEQ);
						for(Integer currentGene : geneProfiles.keySet()){
							Profile currentGeneProfile = geneProfiles.get(currentGene);
							//u.writeOrDump("Checking taxon = " + u.getNodeName(currentTaxon) + " with " + u.getNodeName(curAtt) + " " + currentTaxonProfile.usesAttribute(curAtt) + " " + currentGeneProfile.usesAttribute(curAtt), null);
								//u.writeOrDump("Processing taxon = " + u.getNodeName(currentTaxon) + " with " + u.getNodeName(curAtt), null);
							for (Integer geneEntity : currentGeneProfile.getUsedEntities()){
								if (currentTaxonProfile.hasPhenotypeSet(taxonEntity, curAtt) && currentGeneProfile.hasPhenotypeSet(geneEntity, curAtt)){
									if (!phenotypeScores.hasScore(taxonEntity, geneEntity, curAtt)){
										EQPair geneEQ = new EQPair(geneEntity,curAtt);
										Set<EQPair> geParents = phenotypeParentCache.get(geneEQ);
										Set<EQPair>matches = new HashSet<EQPair>();
										matches.addAll(teParents);	// add the EQ parents of the EA level taxon phenotype
										matches.retainAll(geParents);   // intersect the EQ parents of the gene phenotype, leaving intersection in matches
										double bestMatch = Double.MAX_VALUE;  //we're using fractions, so minimize
										EQPair bestEQ = null;
										for(EQPair eqM : matches){
											if (eaCounts.hasCount(eqM.getEntity(), eqM.getQuality())){    //EA counts needs to change soon
												double matchScore = eaCounts.getFraction(eqM.getEntity(), eqM.getQuality());
												if (matchScore<bestMatch){
													bestMatch = matchScore;
													bestEQ = eqM;
												}
												else if (matchScore < 0)
													throw new RuntimeException("Bad match score value < 0: " + matchScore + " " + u.getNodeName(eqM.getEntity()) + " " + u.getNodeName(eqM.getQuality()));
											}
										}
										if (bestMatch<Double.MAX_VALUE && bestEQ != null){
											phenotypeScores.addScore(taxonEntity,geneEntity,curAtt,CountTable.calcIC(bestMatch),bestEQ);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return attOverlaps;
	}


	void writePhenotypeMatchSummary(PhenotypeScoreTable phenotypeScores,Utils u, Writer w) throws SQLException{
		u.writeOrDump("Taxon\tGene\tTaxon Entity\tGeneEntity\tAttribute\tLowest Common Subsumer\tScore", w);
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			for(Integer currentGene : geneProfiles.keySet()){
				Profile currentGeneProfile = geneProfiles.get(currentGene);
				for(Integer tEntity : currentTaxonProfile.getUsedEntities()){
					for(Integer gEntity : currentGeneProfile.getUsedEntities()){
						for (Integer att : attributeSet){
							if (currentTaxonProfile.hasPhenotypeSet(tEntity, att) && currentGeneProfile.hasPhenotypeSet(gEntity, att)){
								if (phenotypeScores.hasScore(tEntity, gEntity, att)){
									if (phenotypeScores.getScore(tEntity, gEntity,att)>IC_CUTOFF){
//										if ("ttna".equals(u.getNodeName(currentGene))){
//											System.out.println("ttna: " + tEntity + "( " + u.getNodeName(tEntity) + "); " + gEntity +"( " + u.getNodeName(gEntity) + "); " + att +"( " + u.getNodeName(att) + "); ");
//										}
										EQPair bestSubsumer = phenotypeScores.getBestSubsumer(tEntity, gEntity,att);
										StringBuilder lineBuilder = new StringBuilder(200);
										String bestID = bestSubsumer.getFullName(u);
										lineBuilder.append(u.getNodeName(currentTaxon));
										lineBuilder.append("\t");
										lineBuilder.append(u.getNodeName(currentGene));
										lineBuilder.append("\t");
										lineBuilder.append(u.getNodeName(tEntity));
										lineBuilder.append("\t");
										lineBuilder.append(u.getNodeName(gEntity));
										lineBuilder.append("\t");
										lineBuilder.append(u.getNodeName(att));
										lineBuilder.append("\t");
										lineBuilder.append(bestID);
										lineBuilder.append("\t");
										lineBuilder.append(phenotypeScores.getScore(tEntity, gEntity,att));
										u.writeOrDump(lineBuilder.toString(),w);	
									}
								}
							}
						}
					}
				}
			}

		}

	}

	
	
}
