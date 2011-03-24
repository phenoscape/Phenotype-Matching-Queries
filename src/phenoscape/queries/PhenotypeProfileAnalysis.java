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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import phenoscape.queries.TaxonomyTree.TaxonomicNode;
import phenoscape.queries.lib.CountTable;
import phenoscape.queries.lib.DistinctGeneAnnotationRecord;
import phenoscape.queries.lib.PhenotypeExpression;
import phenoscape.queries.lib.PhenotypeScoreTable;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.ProfileScoreSet;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;
import phenoscape.queries.lib.TaxonPhenotypeLink;



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
	private static final String CATOSTOMIDAEROOT= "TTO:10810";
	private static final String TESTROOT = "TTO:0000015";
	private String ANALYSISROOT = TESTROOT;

	private static final double IC_CUTOFF =  0.0;


	private static final String TAXONREPORTFILENAME = "../TaxonVariationReport.txt";
	private static final String GENEREPORTFILENAME = "../GeneVariationReport.txt";
	private static final String PHENOTYPEMATCHREPORTFILENAME = "../PhenotypeMatchReport.txt";
	private static final String PROFILEMATCHREPORTFILENAME = "../ProfileMatchReport.txt";
	private static final String TAXONGENEMAXICSCOREFILENAME = "../MaxICReport.txt";


	static final String GENEPHENOTYPECOUNTQUERY =
		"SELECT count(*) FROM distinct_gene_annotation  WHERE distinct_gene_annotation.phenotype_node_id IN " +
		"(SELECT phenotype.node_id from phenotype " +
		"JOIN link phenotype_inheres_in_part_of ON (phenotype_inheres_in_part_of.node_id = phenotype.node_id AND phenotype_inheres_in_part_of.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:inheres_in_part_of')) " +
		"JOIN link quality_is_a ON (quality_is_a.node_id = phenotype.node_id AND quality_is_a.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a')) " +
		"WHERE (phenotype_inheres_in_part_of.object_id =  ?  AND quality_is_a.object_id = ?))";

	static final String GENEQUALITYCOUNTQUERY =
		"SELECT count(*) FROM distinct_gene_annotation  WHERE distinct_gene_annotation.phenotype_node_id IN " +
		"(SELECT phenotype.node_id from phenotype " +
		"JOIN link quality_is_a ON (quality_is_a.node_id = phenotype.node_id AND quality_is_a.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a')) " +
		"WHERE (quality_is_a.object_id = ?))";


	Map<Integer,Profile> taxonProfiles;  //taxon_node_id -> Phenotype profile for taxon
	Map<Integer,Profile> geneProfiles;  //gene_node_id -> Phenotype profile for gene


	Map<Integer,Integer>childDist = new HashMap<Integer,Integer>();


	/**
	 * This maps qualities to attributes
	 */
	Map<Integer,Integer> attributeMap;

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
	int qualityNodeID;

	static Logger logger = Logger.getLogger(PhenotypeProfileAnalysis.class.getName());

	public PhenotypeProfileAnalysis(Utils u) throws SQLException{
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
		File outFile5 = new File(TAXONGENEMAXICSCOREFILENAME);
		Writer w1 = null;
		Writer w2 = null;
		Writer w3 = null;
		Writer w4 = null;
		Writer w5 = null;
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
			w5 = new BufferedWriter(new FileWriter(outFile5));
			u.writeOrDump(timeStamp, w1);
			u.writeOrDump(timeStamp, w2);
			u.writeOrDump(timeStamp, w3);
			u.writeOrDump(timeStamp, w4);
			u.writeOrDump(timeStamp, w5);
			listQuery.process(u, w1, w2, w3, w4,w5);
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
				w5.close();
			} catch (Exception e) {
				System.err.print("An exception occurred while closing a report file or database connection");
				e.printStackTrace();
			}
		}
	}


	void process(Utils u,Writer w1, Writer w2, Writer w3, Writer w4, Writer w5) throws IOException,SQLException{
		logger.info("Setting up Attribute table");
		qualityNodeID = u.getQualityNodeID();   //set to the root of PATO
		attributeMap = u.setupAttributes();
		
		PhenotypeExpression.getEQTop(u);   //just to initialize early.

		attributeSet.addAll(attributeMap.values());		
		attributeSet.add(qualityNodeID);

		// process taxa annotations
		logger.info("Building Taxonomy Tree");
		TaxonomyTree t = new TaxonomyTree(ANALYSISROOT,u);
		t.traverseOntologyTree(u);
		Map<Integer,Collection<TaxonPhenotypeLink>> allLinks = getAllTaxonPhenotypeLinksFromKB(t,u);
		taxonProfiles = loadTaxonProfiles(allLinks,u, attributeMap, qualityNodeID, badTaxonQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);

		traverseTaxonomy(t, t.getRootNodeID(), taxonProfiles, taxonVariation, u);
		t.report(u, w1);
		taxonVariation.variationReport(u,w1);	
		flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		if (taxonProfiles.isEmpty()){
			throw new RuntimeException("No taxa in Profile Set");
		}

		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);

		geneProfiles = processGeneExpression(geneVariation,u, w2);
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

		logger.info("Building entity parents");
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();

		/* Test introduction of phenotypeParentCache, which should map an attribute level EQ to all its parents via inheres_in_part_of entity parents and is_a quality parents (cross product) */
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		buildEQParents(phenotypeParentCache,entityParentCache,u);

		logger.info("Filling count table");
		//fillCountTable(taxonProfiles, phenotypeCountsForTaxa,phenotypeParentCache, u, TAXONPHENOTYPECOUNTQUERY, u.countAssertedTaxonPhenotypeAnnotations());
		fillCountTable(geneProfiles, phenotypeCountsForGenes,phenotypeParentCache, u, GENEPHENOTYPECOUNTQUERY, GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());

		//sumCountTables(phenotypeCountsCombined,phenotypeCountsForTaxa,phenotypeCountsForGenes);

		CountTable phenotypeCountsToUse = phenotypeCountsForGenes;

		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();


		logger.info("Done building entity parents; building phenotype match cache");
		buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, phenotypeCountsToUse, u);
		w1.close();
		logger.info("Finished building phenotype match cache; Writing Phenotype match summary");
		writePhenotypeMatchSummary(phenotypeScores,u,w3);		
		w3.close();

		logger.info("Finished Writing Phenotype match summary");

		logger.info("Writing maxIC for taxon/gene summary");
		writeTaxonGeneMaxICSummary(phenotypeScores,u,w5);
		w5.close();
		logger.info("Finished Writing maxIC for taxon/gene summary");		

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
		if (Double.isInfinite(sum))
			return -1.0;
		else
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
	HashMap<Integer,Profile> loadTaxonProfiles(Map<Integer,Collection<TaxonPhenotypeLink>> allLinks, Utils u, Map<Integer,Integer> attMap,int nodeIDofQuality, Map<Integer,Integer> badQualities) throws SQLException{	
		HashMap<Integer,Profile> taxonProfileSet = new HashMap<Integer,Profile>();
		Set<Integer> taxonSet = allLinks.keySet();
		for (Integer taxonID : taxonSet){
			Profile myProfile = new Profile();
			for(TaxonPhenotypeLink link : allLinks.get(taxonID)){
				u.putNodeUIDName(link.getPhenotypeNodeID(), link.getPhenotypeUID(),link.getPhenotypeLabel());
				if (attMap.containsKey(link.getQualityNodeID())){
					final int attribute_id = attMap.get(link.getQualityNodeID());
					myProfile.addPhenotype(link.getEntityNodeID(),attribute_id, link.getPhenotypeNodeID());

				}
				else{
					final int linkQualityID = link.getQualityNodeID();
					myProfile.addPhenotype(link.getEntityNodeID(),nodeIDofQuality, link.getPhenotypeNodeID());
					if (badQualities.containsKey(linkQualityID)){
						badQualities.put(linkQualityID, badQualities.get(linkQualityID).intValue()+1);
					}
					else {
						badQualities.put(linkQualityID, 1);
						u.putNodeUIDName(linkQualityID, link.getQualityUID(), link.getQualityLabel());
					}
				}
				u.putNodeUIDName(link.getEntityNodeID(), link.getEntityUID(),link.getEntityLabel());
			}
			taxonProfileSet.put(taxonID,myProfile);
		}
		return taxonProfileSet;
	}


	Map<Integer,Collection<TaxonPhenotypeLink>> getAllTaxonPhenotypeLinksFromKB(TaxonomyTree t, Utils u) throws SQLException{
		Map<Integer,Collection<TaxonPhenotypeLink>> result = new HashMap<Integer,Collection<TaxonPhenotypeLink>>();
		Set<Integer> taxonSet = t.getAllTaxa();
		for (Integer taxonID : taxonSet){
			Collection<TaxonPhenotypeLink> tLinks = getTaxonPhenotypeLinksFromKB(u,taxonID);
			result.put(taxonID, tLinks);
		}
		return result;
	}


	Collection<TaxonPhenotypeLink> getTaxonPhenotypeLinksFromKB(Utils u, int taxonID) throws SQLException{
		final PreparedStatement p = u.getPreparedStatement(TaxonPhenotypeLink.getQuery());
		final Collection<TaxonPhenotypeLink> result = new HashSet<TaxonPhenotypeLink>();
		p.setInt(1, taxonID);
		ResultSet ts = p.executeQuery();
		while (ts.next()){
			TaxonPhenotypeLink l = new TaxonPhenotypeLink(ts);
			result.add(l);
		}
		return result;
	}


	/**
	 * This method marks taxon phenotypes that display variation. 
	 *  There are two additional twists: the absence of an annotation for a particular entity-attribute
	 * combination in a child taxon is treated as variation if there are sister taxa with annotation for the same combination, but if the taxon
	 * has no annotations whatsoever, it is ignored.
	 * @param t
	 * @param taxon
	 * @param profiles
	 * @param u
	 */
	void traverseTaxonomy(TaxonomyTree t, Integer taxon, Map<Integer, Profile> profiles, VariationTable variation, Utils u){
		if (t.nodeIsInternal(taxon, u)){
			//build set of children
			final Set<Integer> children = t.getTable().get(taxon);
			Set<Profile> childProfiles = new HashSet<Profile>();
			for(Integer child : children){
				traverseTaxonomy(t,child,profiles, variation, u);
				childProfiles.add(profiles.get(child));
			}
			Profile parentProfile = profiles.get(taxon);			
			//This builds the union and intersection sets (upwards) sets annotations for each taxon with childProfiles
			//Changed to propagate the intersection rather than the union 21 Feb
			Set<Integer> usedEntities = new HashSet<Integer>();
			Set<Integer> usedAttributes = new HashSet<Integer>();
			for (Profile childProfile : childProfiles){
				usedEntities.addAll(childProfile.getUsedEntities());
				usedAttributes.addAll(childProfile.getUsedAttributes());
			}
			for(Integer ent : usedEntities){
				for(Integer att : usedAttributes){
					Set <Integer>unionSet = new HashSet<Integer>();
					Set <Integer>intersectionSet = new HashSet<Integer>();
					for (Profile childProfile : childProfiles){
						if (!childProfile.isEmpty() && childProfile.hasPhenotypeSet(ent, att)){
							unionSet.addAll(childProfile.getPhenotypeSet(ent,att));
						}
					}
					intersectionSet.addAll(unionSet);  // start intersection from the union and intersect each child in turn
					for (Profile childProfile : childProfiles){
						if (!childProfile.isEmpty() && childProfile.hasPhenotypeSet(ent, att)){
							intersectionSet.retainAll(childProfile.getPhenotypeSet(ent,att));
						}
					}
					// now the union and intersection sets reflect the sets of children.
					// add the asserted phenotypes of this parent (if any) to both union and intersection
					if (parentProfile.hasPhenotypeSet(ent, att)){
						unionSet.addAll(parentProfile.getPhenotypeSet(ent, att));
						intersectionSet.addAll(parentProfile.getPhenotypeSet(ent,att));  //this union operation is correct!
					}
					if (!unionSet.equals(intersectionSet)){  //if variation
						variation.addExhibitor(ent, att, taxon);  //add to the variation table
					}
					parentProfile.setPhenotypeSet(ent, att, intersectionSet);
				}
			}
		}
	}


	/**
	 * This method removes all phenotypes that don't indicate variation from the profile.  
	 * Note: After this runs, cells in each profile will either contain the intersection set (which may be empty)
	 * or null which indicates no variation in the entity attribute combination that addresses the cell.  It is
	 * important to notice that an empty set still indicates variation (and will frequently do so), and should not
	 * be confused with a null entry.  Code downstream from this method just look at whether there is a non-null value
	 * in profile cells.
	 */
	void flushUnvaryingPhenotypes(Map<Integer,Profile> taxonProfileSet, VariationTable variation, Utils u){
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
	HashMap<Integer,Profile> processGeneExpression(VariationTable variation, Utils u, Writer reportWriter) throws SQLException{
		HashMap<Integer,Profile> profiles = new HashMap<Integer,Profile>();
		int annotationCount = 0;
		int usableAnnotationCount = 0;
		Set<Integer>uniqueGenes = new HashSet<Integer>();
		Collection<DistinctGeneAnnotationRecord> annotationList = getAllGeneAnnotationsFromKB(u);
		for (DistinctGeneAnnotationRecord annotation : annotationList){
			final int geneID = annotation.getGeneID();
			final int phenotype_id = annotation.getPhenotypeID();
			final int entity_id = annotation.getEntityID();
			final int quality_id = annotation.getQualityID();
			if (attributeMap.containsKey(annotation.getQualityID())){
				final int attribute_id = attributeMap.get(annotation.getQualityID());
				if (profiles.containsKey(geneID)){
					profiles.get(geneID).addPhenotype(annotation.getEntityID(),attribute_id, phenotype_id);
				}
				else {
					profiles.put(geneID, new Profile());
					profiles.get(geneID).addPhenotype(entity_id,attribute_id, phenotype_id);
				}
				variation.addExhibitor(entity_id, attribute_id, geneID);
				usableAnnotationCount++;
			}
			else{
				if (profiles.containsKey(geneID)){
					profiles.get(geneID).addPhenotype(entity_id, qualityNodeID, phenotype_id);
				}
				else {
					profiles.put(geneID, new Profile());
					profiles.get(geneID).addPhenotype(entity_id, qualityNodeID, phenotype_id);
				}
				variation.addExhibitor(entity_id, qualityNodeID, geneID);
				if (badGeneQualities.containsKey(quality_id)){
					badGeneQualities.put(quality_id, badGeneQualities.get(quality_id).intValue()+1);
				}
				else {
					badGeneQualities.put(quality_id, 1);
				}
			}
			u.putNodeUIDName(annotation.getPhenotypeID(), annotation.getPhenotypeUID(), annotation.getPhenotypeLabel());
			u.putNodeUIDName(quality_id, annotation.getQualityUID(),annotation.getQualityLabel());
			u.putNodeUIDName(entity_id, annotation.getEntityUID(), annotation.getEntityLabel());
			u.putNodeUIDName(geneID, annotation.getGeneUID(),annotation.getGeneLabel());
			annotationCount++;
			uniqueGenes.add(annotation.getGeneID());
		}
		u.writeOrDump("Raw Genes " + uniqueGenes.size() + "; Distinct Gene annotations " + annotationCount + "; Count of genes with annotations " + profiles.keySet().size() + "; Annotations to attributes other than Quality " + usableAnnotationCount, reportWriter);
		return profiles;
	}

	
	Collection<DistinctGeneAnnotationRecord> getAllGeneAnnotationsFromKB(Utils u) throws SQLException{
		final Statement s = u.getStatement();
		final Collection<DistinctGeneAnnotationRecord> result = new HashSet<DistinctGeneAnnotationRecord>();
		ResultSet ts = s.executeQuery(DistinctGeneAnnotationRecord.getQuery());
		while (ts.next()){
			DistinctGeneAnnotationRecord l = new DistinctGeneAnnotationRecord(ts);
			result.add(l);
		}
		return result;
	}



	/**
	 * For each phenotype in the taxonProfile, this builds the set of class expression parents (both EQ and Q) of the phenotype.
	 * Changed to save the parents (iipo parents) indexed by the entity, which is more efficient and useful later on.
	 * 
	 * @param entityParentCache
	 * @param u
	 * @throws SQLException
	 */
	void buildEQParents(Map<PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache, Map <Integer,Set<Integer>> entityParentCache, Utils u) throws SQLException{
		for(Integer curAtt : attributeSet){
			Set<Integer> qualityParentSet = u.collectQualityParents(curAtt);
			for(Integer curEntity : entityParentCache.keySet()){
				PhenotypeExpression curEQ = new PhenotypeExpression(curEntity,curAtt);
				if (!phenotypeParentCache.containsKey(curEQ)){
					Set<Integer> entityParentSet = entityParentCache.get(curEntity);
					Set<PhenotypeExpression> eqParentSet = new HashSet<PhenotypeExpression>();  //pass phenotype id, list of entities returned
					phenotypeParentCache.put(curEQ,eqParentSet);
					if (entityParentSet.isEmpty() || qualityParentSet.isEmpty()){
						curEQ.fillNames(u);
						logger.info("Failed to add Parents of: " + curEQ);
						if (entityParentSet.isEmpty() && qualityParentSet.isEmpty())
							logger.info("Because both parent sets are empty");
						else if (entityParentSet.isEmpty())
							logger.info("Because the entity parent set is empty");
						else
							logger.info("Because the parent set of " + curAtt + " is empty");
					}
					for(Integer qualParent : qualityParentSet){
						for(Integer entParent : entityParentSet){
							PhenotypeExpression newParentEQ = new PhenotypeExpression(entParent,qualParent);
							String bestID = newParentEQ.getFullName(u);
							eqParentSet.add(newParentEQ);
						}
						PhenotypeExpression newParentQ = new PhenotypeExpression(qualParent);
						eqParentSet.add(newParentQ);
					}
					if (eqParentSet.isEmpty()){
						throw new RuntimeException("empty parentSet: " + u.getNodeName(curEntity) + " " + u.getNodeName(curAtt) );
					}
					// finally add quality
					eqParentSet.add(PhenotypeExpression.getEQTop(u));
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
	void fillCountTable(Map<Integer,Profile> profiles, CountTable counts,Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache, Utils u, String phenotypeQuery, String qualityQuery, int annotationCount) throws SQLException{
		counts.setSum(annotationCount);
		final PhenotypeExpression topEQ = PhenotypeExpression.getEQTop(u);
		counts.addCount(topEQ, annotationCount);
		final PreparedStatement phenotypeStatement = u.getPreparedStatement(phenotypeQuery);
		final PreparedStatement qualityStatement = u.getPreparedStatement(qualityQuery);
		for(Profile currentProfile : profiles.values()){
			Set <Integer> usedEntities = currentProfile.getUsedEntities();
			Set <Integer> usedAttributes = currentProfile.getUsedAttributes();
			for(Integer profileEntity : usedEntities){
				for (Integer curAttribute : usedAttributes){
					PhenotypeExpression curEQ = new PhenotypeExpression(profileEntity,curAttribute);
					Set<PhenotypeExpression> allParents = phenotypeParentCache.get(curEQ);
					if (allParents == null){
						logger.error("The Phenotype " + curEQ + " seems to have no parents");
					}
					else {
						curEQ.fillNames(u);
						//logger.info("Processing " + curEQ);
						for(PhenotypeExpression phenotypeParent : allParents){
							if (!counts.hasCount(phenotypeParent)){
								if (phenotypeParent.isSimpleQuality()){
									qualityStatement.setInt(1, phenotypeParent.getQuality());
									ResultSet qResult = qualityStatement.executeQuery();
									if(qResult.next()){
										int count = qResult.getInt(1);
										counts.addCount(phenotypeParent, count);
									}
									else {
										throw new RuntimeException("count query failed for quality " + u.getNodeName(phenotypeParent.getQuality()));
									}
								}
								else {
									phenotypeStatement.setInt(1, phenotypeParent.getEntity());
									phenotypeStatement.setInt(2, phenotypeParent.getQuality());
									ResultSet eaResult = phenotypeStatement.executeQuery();
									if(eaResult.next()){
										int count = eaResult.getInt(1);
										counts.addCount(phenotypeParent, count);
									}
									else {
										throw new RuntimeException("count query failed for phenotype expression " + u.getNodeName(phenotypeParent.getEntity()) + " " + u.getNodeName(phenotypeParent.getQuality()));
									}
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
		for(PhenotypeExpression p : table1.getPhenotypes()){
			int count1 = table1.getRawCount(p);
			if (table2.hasCount(p)){
				int count2 = table2.getRawCount(p);
				sum.addCount(p, count1+count2);
			}
			else{
				sum.addCount(p, count1);
			}
		}
		for(PhenotypeExpression p : table2.getPhenotypes()){
			if (!sum.hasCount(p)){
				sum.addCount(p, table2.getRawCount(p));
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
	 * @throws SQLException 
	 */
	int buildPhenotypeMatchCache(Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache, PhenotypeScoreTable phenotypeScores, CountTable eaCounts, Utils u) throws SQLException{
		int attOverlaps = 0;
		for (Integer curAtt : attributeSet){
			for(Integer currentTaxon : taxonProfiles.keySet()){
				Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
				if (!currentTaxonProfile.isEmpty()){
					for (Integer taxonEntity : currentTaxonProfile.getUsedEntities()){
						PhenotypeExpression taxonEQ = new PhenotypeExpression(taxonEntity,curAtt);
						Set<PhenotypeExpression> teParents = phenotypeParentCache.get(taxonEQ);
						if (teParents == null){
							throw new RuntimeException("parents of " + taxonEQ.getFullName(u) + " from " + u.getNodeName(currentTaxon) + " is null" );
						}
						if (teParents.isEmpty()){
							throw new RuntimeException("parents of " + taxonEQ.getFullName(u) + " is empty" );
						}
						for(Integer currentGene : geneProfiles.keySet()){
							Profile currentGeneProfile = geneProfiles.get(currentGene);
							//u.writeOrDump("Checking taxon = " + u.getNodeName(currentTaxon) + " with " + u.getNodeName(curAtt) + " " + currentTaxonProfile.usesAttribute(curAtt) + " " + currentGeneProfile.usesAttribute(curAtt), null);
							//u.writeOrDump("Processing taxon = " + u.getNodeName(currentTaxon) + " with " + u.getNodeName(curAtt), null);
							for (Integer geneEntity : currentGeneProfile.getUsedEntities()){
								if (currentTaxonProfile.hasPhenotypeSet(taxonEntity, curAtt) && currentGeneProfile.hasPhenotypeSet(geneEntity, curAtt)){
									if (!phenotypeScores.hasScore(taxonEntity, geneEntity, curAtt)){
										PhenotypeExpression geneEQ = new PhenotypeExpression(geneEntity,curAtt);
										Set<PhenotypeExpression> geParents = phenotypeParentCache.get(geneEQ);
										Set<PhenotypeExpression>matches = new HashSet<PhenotypeExpression>();
										matches.addAll(teParents);	// add the EQ parents of the EA level taxon phenotype
										matches.retainAll(geParents);   // intersect the EQ parents of the gene phenotype, leaving intersection in matches
										if (matches.isEmpty()){
											u.writeOrDump("Taxon Parents", null);
											for (PhenotypeExpression taxonP : teParents){
												taxonP.fillNames(u);
												u.writeOrDump(taxonP.getFullUID(u),null);
											}
											u.writeOrDump("Gene Parents", null);
											for (PhenotypeExpression geneP : geParents){
												geneP.fillNames(u);
												u.writeOrDump(geneP.getFullUID(u),null);
											}
											throw new RuntimeException("Bad intersection");
										}
										double bestMatch = Double.MAX_VALUE;  //we're using fractions, so minimize
										PhenotypeExpression bestEQ = null;
										for(PhenotypeExpression eqM : matches){
											if (eaCounts.hasCount(eqM)){    
												eqM.fillNames(u);
												double matchScore = eaCounts.getFraction(eqM);
												if (matchScore<bestMatch){
													bestMatch = matchScore;
													bestEQ = eqM;
												}
												else if (matchScore < 0)
													throw new RuntimeException("Bad match score value < 0: " + matchScore + " " + u.getNodeName(eqM.getEntity()) + " " + u.getNodeName(eqM.getQuality()));
											}
											else {
												throw new RuntimeException("eq has no score " + eqM.getFullName(u),null);
											}
										}
										if (bestMatch<Double.MAX_VALUE && bestEQ != null){
											phenotypeScores.addScore(taxonEntity,geneEntity,curAtt,CountTable.calcIC(bestMatch),bestEQ);
										}
										else{
											u.writeOrDump("Intersection", null);
											for (PhenotypeExpression shared : matches){
												shared.fillNames(u);
												u.writeOrDump(shared.getFullName(u),null);
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
									if (phenotypeScores.getScore(tEntity, gEntity,att)>=IC_CUTOFF){
										PhenotypeExpression bestSubsumer = phenotypeScores.getBestSubsumer(tEntity, gEntity,att);
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

	void writeTaxonGeneMaxICSummary(PhenotypeScoreTable phenotypeScores,Utils u, Writer w) throws SQLException{
		final Map<Integer,Double> taxonMaxICScores = new HashMap<Integer,Double>();
		final Map<Integer,Double> geneMaxICScores = new HashMap<Integer,Double>();
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			for(Integer currentGene : geneProfiles.keySet()){
				Profile currentGeneProfile = geneProfiles.get(currentGene);
				for(Integer tEntity : currentTaxonProfile.getUsedEntities()){
					for(Integer gEntity : currentGeneProfile.getUsedEntities()){
						for (Integer att : attributeSet){
							if (currentTaxonProfile.hasPhenotypeSet(tEntity, att) && currentGeneProfile.hasPhenotypeSet(gEntity, att)){
								if (phenotypeScores.hasScore(tEntity, gEntity, att)){
									double score = phenotypeScores.getScore(tEntity, gEntity, att);
									if (taxonMaxICScores.containsKey(currentTaxon)){
										if (score > taxonMaxICScores.get(currentTaxon).doubleValue()){
											taxonMaxICScores.put(currentTaxon, score);
										}
									}
									else {
										taxonMaxICScores.put(currentTaxon, score);
									}
									if (geneMaxICScores.containsKey(currentGene)){
										if (score > geneMaxICScores.get(currentGene).doubleValue()){
											geneMaxICScores.put(currentGene, score);
										}
									}
									else {
										geneMaxICScores.put(currentGene, score);
									}
								}
							}
						}
					}
				}
			}
		}
		u.writeOrDump("Bearer\tMaxIC", w);
		for(Integer taxon : taxonMaxICScores.keySet()){
			u.writeOrDump(u.getNodeName(taxon) + "\t" + taxonMaxICScores.get(taxon).toString(), w);
		}
		u.writeOrDump("",w);
		u.writeOrDump("",w);
		for(Integer gene : geneMaxICScores.keySet()){
			u.writeOrDump(u.getNodeName(gene) + "\t" + geneMaxICScores.get(gene).toString(), w);
		}

	}


	//Misc field accessors

	public int getQualityNodeID(){
		return qualityNodeID;
	}

	public Map<Integer,Integer> getAttributeMap(){
		return attributeMap;
	}

	public Set<Integer> getAttributeSet(){
		return attributeSet;
	}	




}
