package phenoscape.queries;

import java.io.BufferedWriter;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import phenoscape.queries.lib.CountTable;
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
	private static final String OSTARIOPHYSIROOT = "TTO:302";
	private static final String ASPIDORASROOT = "TTO:105426";
	private static final String AMIIDAEROOT = "TTO:10360";
	private static final String CALLICHTHYIDAEROOT = "TTO:11200";
	private static final String SILURIFORMESROOT = "TTO:1380";
	
	private String ANALYSISROOT = CALLICHTHYIDAEROOT;


	private static final String TAXONREPORTFILENAME = "../TaxonVariationReport.txt";
	private static final String GENEREPORTFILENAME = "../GeneVariationReport.txt";
	private static final String PHENOTYPEMATCHREPORTFILENAME = "../PhenotypeMatchReport.txt";
	private static final String PROFILEMATCHREPORTFILENAME = "../ProfileMatchReport.txt";


	private static final String TAXONCHILDQUERY = "SELECT child.node_id,link.node_id, phenotype.node_id,phenotype.entity_node_id, phenotype.entity_uid, phenotype.quality_node_id,phenotype.quality_uid,phenotype.uid,simple_label(phenotype.node_id),simple_label(phenotype.entity_node_id),simple_label(phenotype.quality_node_id) FROM link " +
	"JOIN taxon AS child ON (child.node_id = link.node_id AND child.parent_node_id = ? AND link.predicate_id = (select node_id FROM node WHERE uid = 'PHENOSCAPE:exhibits'))" +
	"JOIN phenotype ON (link.object_id = phenotype.node_id) WHERE is_inferred = false";

	private static final String TAXONQUERY = "SELECT taxon.node_id,link.node_id, phenotype.node_id,phenotype.entity_node_id, phenotype.entity_uid, phenotype.quality_node_id,phenotype.quality_uid,phenotype.uid,simple_label(phenotype.node_id),simple_label(phenotype.entity_node_id),simple_label(phenotype.quality_node_id) FROM link " +
	"JOIN taxon ON (taxon.node_id = link.node_id AND taxon.node_id = ? AND link.predicate_id = (select node_id FROM node WHERE uid = 'PHENOSCAPE:exhibits'))" +
	"JOIN phenotype ON (link.object_id = phenotype.node_id) WHERE is_inferred = false";

	private static final String GENEANNOTATIONQUERY = 		
		"SELECT gene_node_id, gene_uid, gene_label, dga.phenotype_node_id, p1.entity_node_id, p1.entity_uid, p1.quality_node_id, p1.quality_uid,p1.uid,simple_label(dga.phenotype_node_id), simple_label(p1.entity_node_id),simple_label(p1.quality_node_id) FROM distinct_gene_annotation AS dga " +
		"JOIN phenotype AS p1 ON (p1.node_id = dga.phenotype_node_id)";

	//Note: these queries are currently identical, but they are constructed separately to allow divergence
	private static final String TAXONPHENOTYPENEIGHBORQUERY = 
		"SELECT target.node_id FROM node AS entity " +
		"JOIN link ON (entity.node_id=link.node_id AND link.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:inheres_in_part_of')) " + 
		"JOIN node AS target ON (target.node_id = link.object_id) WHERE entity.node_id = ? " +
		"GROUP BY entity.uid, target.uid,target.node_id";
	
	private static final String GENEPHENOTYPENEIGHBORQUERY =
		"SELECT target.node_id FROM node AS entity " +
		"JOIN link ON (entity.node_id=link.node_id AND link.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:inheres_in_part_of')) " + 
		"JOIN node AS target ON (target.node_id = link.object_id) WHERE entity.node_id = ? " +
		"GROUP BY entity.uid, target.uid, target.node_id";

	private static final String TAXONPHENOTYPECOUNTQUERY = 
		"SELECT count(*) FROM asserted_taxon_annotation  WHERE asserted_taxon_annotation.phenotype_node_id IN " +
		"(SELECT phenotype.node_id from phenotype " + 
		"JOIN link phenotype_inheres_in_part_of ON (phenotype_inheres_in_part_of.node_id = phenotype.node_id AND phenotype_inheres_in_part_of.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:inheres_in_part_of')) " +
		"JOIN link quality_is_a ON (quality_is_a.node_id = phenotype.node_id AND quality_is_a.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a')) " + 
		"WHERE (phenotype_inheres_in_part_of.object_id = ?  AND quality_is_a.object_id = ?))";
	
	private static final String ASSERTEDTAXONPHENOTYPECOUNTQUERY =
		"SELECT COUNT(*) FROM asserted_taxon_annotation";
	

	private static final String GENEPHENOTYPECOUNTQUERY =
		"SELECT count(*) FROM distinct_gene_annotation  WHERE distinct_gene_annotation.phenotype_node_id IN " +
		"(SELECT phenotype.node_id from phenotype " +
		"JOIN link phenotype_inheres_in_part_of ON (phenotype_inheres_in_part_of.node_id = phenotype.node_id AND phenotype_inheres_in_part_of.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:inheres_in_part_of')) " +
		"JOIN link quality_is_a ON (quality_is_a.node_id = phenotype.node_id AND quality_is_a.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a')) " +
		"WHERE (phenotype_inheres_in_part_of.object_id = ?  AND quality_is_a.object_id = ?))";

	private static final String ASSERTEDGENEPHENOTYPECOUNTQUERY =
		"SELECT COUNT(*) FROM distinct_gene_annotation";

	
	Map<Integer,Profile> taxonProfiles = new HashMap<Integer,Profile>();  //taxon_node_id -> Phenotype profile for taxon
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		PhenotypeProfileAnalysis listQuery = new PhenotypeProfileAnalysis();
		Utils u = new Utils();
		u.openKB();
		File outFile1 = new File(TAXONREPORTFILENAME);
		File outFile2 = new File(GENEREPORTFILENAME);
		File outFile3 = new File(PHENOTYPEMATCHREPORTFILENAME);
		File outFile4 = new File(PROFILEMATCHREPORTFILENAME);
		BufferedWriter bw1 = null;
		BufferedWriter bw2 = null;
		BufferedWriter bw3 = null;
		BufferedWriter bw4 = null;
		Date today;
		DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT);
		today = new Date();
		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
		String timeStamp = dateFormatter.format(today) + " " + timeFormatter.format(today);		
		try {
			bw1 = new BufferedWriter(new FileWriter(outFile1));
			bw2 = new BufferedWriter(new FileWriter(outFile2));
			bw3 = new BufferedWriter(new FileWriter(outFile3));
			bw4 = new BufferedWriter(new FileWriter(outFile4));
			u.writeOrDump(timeStamp, bw1);
			u.writeOrDump(timeStamp, bw2);
			u.writeOrDump(timeStamp, bw3);
			u.writeOrDump(timeStamp, bw4);
			listQuery.process(u, bw1, bw2, bw3, bw4);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				u.closeKB();
				bw1.close();
				bw2.close();
				bw3.close();
				bw4.close();
			} catch (Exception e) {
				System.err.print("An exception occurred while closing a report file or database connection");
				e.printStackTrace();
			}
		}
	}


	void process(Utils u,BufferedWriter bw1, BufferedWriter bw2, BufferedWriter bw3, BufferedWriter bw4) throws IOException,SQLException{
		logger.info("Caching entity UIDs and labels");
		u.uidCacheEntities();    // this will retrieve entities that appear in EQ's
		
		logger.info("Setting up Attribute table");
		attributeMap = u.setupAttributes();

		attributeSet.addAll(attributeMap.values());
		attributeSet.add(qualityNodeID);

		
		qualityNodeID = u.getQualityNodeID();   //set to the root of PATO


		// process taxa annotations
		logger.info("Building Taxonomy Tree");
		TaxonomyTree t = new TaxonomyTree(ANALYSISROOT,u);
		t.traverseOntologyTree(u);
		processTaxonVariation(t,u, bw1);
		t.report(u, bw1);
		taxonVariation.variationReport(u,bw1);	

		processGeneExpression(u, bw2);
		geneVariation.variationReport(u, bw2);
		bw2.close();

		/* These need to happen after the profiles have been constructed, since we don't want to count taxon annotations that don't reflect change */
		CountTable phenotypeCountsForTaxa = new CountTable();  
		CountTable phenotypeCountsForGenes = new CountTable();
		CountTable phenotypeCountsCombined = new CountTable();

		Map <Integer,Set<Integer>> phenotypeNeighborCache = new HashMap<Integer,Set<Integer>>();
		logger.info("Building entity parents of taxon phenotypes");
		buildTaxonEntityParents(phenotypeNeighborCache,u);

		logger.info("Building entity parents of gene phenotypes");
		buildGeneEntityParents(phenotypeNeighborCache,u);

		fillCountTable(taxonProfiles, phenotypeCountsForTaxa,phenotypeNeighborCache, u, TAXONPHENOTYPECOUNTQUERY, ASSERTEDTAXONPHENOTYPECOUNTQUERY);
		fillCountTable(geneProfiles, phenotypeCountsForGenes,phenotypeNeighborCache, u, GENEPHENOTYPECOUNTQUERY, ASSERTEDGENEPHENOTYPECOUNTQUERY);
		sumCountTables(phenotypeCountsCombined,phenotypeCountsForTaxa,phenotypeCountsForGenes);

		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();


		logger.info("Done building entity parents; building phenotype match cache");
		int attOverlaps = buildPhenotypeMatchCache(phenotypeNeighborCache, phenotypeScores, phenotypeCountsCombined, u);
		u.writeOrDump("gene and taxon profiles overlapping on an attribute:  " + attOverlaps,bw1);
		bw1.close();
		logger.info("Finished building phenotype match cache; Writing Phenotype match summary");
		writePhenotypeMatchSummary(phenotypeScores,u,bw3);		
		bw3.close();

		logger.info("Finished Writing Phenotype match summary");
		logger.info("Calculating Profile Scores");

		//List<ProfileScoreSet> results = new ArrayList<ProfileScoreSet>(1000);
		u.writeOrDump("Taxon \t Gene \t taxon phenotypes \t gene phenotypes \t maxIC \t iccs \t simIC \t simJ",bw4);

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
				//double simICScore = calcSimIC(currentTaxonProfile, currentGeneProfile, phenotypeScores, phenotypeCountsCombined);
				result.setSimICScore(-1.0);

				//calculate simJ score for this pair of profiles
				//double simJScore = calcSimJ(currentTaxonProfile, currentGeneProfile, phenotypeScores, phenotypeCountsCombined);
				result.setSimJScore(-1.0);

				if (result.isNonZero())
					result.writeScores(u, bw4);
				else
					zeroCount++;
			}
		}
		u.writeOrDump("Pairs with zero score = " + zeroCount, bw4);
		bw4.close();
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
	void processTaxonVariation(TaxonomyTree t, Utils u, BufferedWriter reportWriter) throws SQLException{		
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
			taxonProfiles.put(taxonID,myProfile);
		}
		traverseTaxonomy(t, t.getRootNodeID(), taxonProfiles, u, reportWriter);
		flushUnvaryingPhenotypes();
	}


	void traverseTaxonomy(TaxonomyTree t, Integer taxon, Map<Integer, Profile> profiles, Utils u, BufferedWriter reportWriter){
		if (t.nodeIsInternal(taxon, u)){
			//build set of children
			final Set<Integer> children = t.getTable().get(taxon);
			Set<Profile> childProfiles = new HashSet<Profile>();
			for(Integer child : children){
				traverseTaxonomy(t,child,profiles, u,reportWriter);
				childProfiles.add(profiles.get(child));
			}			
			//This builds the inferred (upwards) sets annotations for each taxon with childProfiles
			Profile parentProfile = profiles.get(taxon);
			for (Profile childProfile : childProfiles){
				if (!childProfile.isEmpty()){
					for (Integer ent : childProfile.getUsedEntities()){
						for (Integer att : childProfile.getUsedAttributes()){
							if (childProfile.hasPhenotypeSet(ent,att)){
								parentProfile.addAlltoPhenotypeSet(ent, att, childProfile.getPhenotypeSet(ent, att));
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
							if (childProfile.hasPhenotypeSet(ent, att)){
								//System.out.println("att = " + att.intValue() + " ;ent = " + ent.intValue() + " ;parent profile size " + currentTaxonProfile.getPhenotypeSet(att,ent).size() + " ; child profile size" + childProfile.getPhenotypeSet(att, ent).size());
								if (parentProfile.getPhenotypeSet(ent,att).size() != childProfile.getPhenotypeSet(ent,att).size()){
									taxonVariation.addExhibitor(ent, att, taxon);
								}
							}
							else if (parentProfile.hasPhenotypeSet(ent,att)){  //child not represented = change in sibling
								taxonVariation.addExhibitor(ent,att,taxon);
							}
						}
					}
				}
			}
		}
	}
	

	// remove non-varying entries from the taxon profile
	void flushUnvaryingPhenotypes(){
		for (Integer taxon : taxonProfiles.keySet()){
			Profile p = taxonProfiles.get(taxon);
			Set<Integer> entitySet = new HashSet<Integer>();
			Set<Integer> attributeSet = new HashSet<Integer>();
			entitySet.addAll(p.getUsedEntities());
			attributeSet.addAll(p.getUsedAttributes());
			for (Integer ent : entitySet){
				for (Integer att : attributeSet){
					if (p.hasPhenotypeSet(ent, att)){
						if (!taxonVariation.taxonExhibits(ent,att,taxon)){
							p.clearPhenotypeSet(ent, att);
						}
					}
				}
			}
		}
		Set<Integer> taxa = new HashSet<Integer>();
		taxa.addAll(taxonProfiles.keySet());
		for (Integer taxon : taxa){
			if (taxonProfiles.get(taxon).isEmpty())
			taxonProfiles.remove(taxon);
		}
	}

	/**
	 * Name is a little dicy, but better than GeneVariation
	 */
	void processGeneExpression(Utils u, BufferedWriter reportWriter) throws SQLException{
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

		u.writeOrDump("Raw Genes " + uniqueGenes.size() + "; Gene annotations" + annotationCount + ";Count of genes with annotations " + geneProfiles.keySet().size() + "; Annotations with known attributes " + usableAnnotationCount, reportWriter);

		u.writeOrDump("\nList of qualities that were placed under quality as an attribute by default\n", reportWriter);
		for(Integer bad_id : badGeneQualities.keySet()){
			u.writeOrDump(u.getNodeName(bad_id) + " " + badGeneQualities.get(bad_id), reportWriter);
		}
	}





	/**
	 * For each phenotype in the taxonProfile, this builds the set of entity parents.
	 * Changed to save the parents (iipo parents) indexed by the entity, which is more efficient and useful later on.
	 * 
	 * @param phenotypeNeighborCache
	 * @param u
	 * @throws SQLException
	 */
	void buildTaxonEntityParents(Map <Integer,Set<Integer>> phenotypeNeighborCache, Utils u) throws SQLException{
		final PreparedStatement p3 = u.getPreparedStatement(TAXONPHENOTYPENEIGHBORQUERY);
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			Set<Integer> taxonAttributes = currentTaxonProfile.getUsedAttributes();
			Set<Integer> taxonEntities = currentTaxonProfile.getUsedEntities();
			for (Integer att : taxonAttributes){
				for (Integer ent : taxonEntities){
					if (currentTaxonProfile.hasPhenotypeSet(ent, att)){
						for (Integer pheno : currentTaxonProfile.getPhenotypeSet(ent, att)){
							if (!phenotypeNeighborCache.containsKey(ent)){
								Set<Integer> parentset = new HashSet<Integer>();
								phenotypeNeighborCache.put(ent, parentset);
								p3.setInt(1, pheno);
								ResultSet entityparents = p3.executeQuery();
								while(entityparents.next()){
									int target_id = entityparents.getInt(1);
									parentset.add(target_id);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Similar to buildTaxonEntityparents, perhaps these should be merged
	 * @param phenotypeNeighborCache
	 * @param u
	 * @throws SQLException
	 */
	void buildGeneEntityParents(Map <Integer,Set<Integer>> phenotypeNeighborCache, Utils u) throws SQLException{
		final PreparedStatement genePhenotypeQuery = u.getPreparedStatement(GENEPHENOTYPENEIGHBORQUERY);
		for(Integer currentGene : geneProfiles.keySet()){
			Profile currentGeneProfile = geneProfiles.get(currentGene);
			Set<Integer> geneAttributes = currentGeneProfile.getUsedAttributes();
			Set<Integer> geneEntities = currentGeneProfile.getUsedEntities();
			for (Integer att : geneAttributes){
				for (Integer ent : geneEntities){
					if (currentGeneProfile.hasPhenotypeSet(ent, att)){
						for (Integer pheno : currentGeneProfile.getPhenotypeSet(ent, att)){
							if (!phenotypeNeighborCache.containsKey(ent)){
								Set<Integer> parentset = new HashSet<Integer>();
								phenotypeNeighborCache.put(ent, parentset);
								genePhenotypeQuery.setInt(1, pheno);
								ResultSet entityparents = genePhenotypeQuery.executeQuery();
								while(entityparents.next()){
									int target_id = entityparents.getInt(1);
									parentset.add(target_id);
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
	void fillCountTable(Map<Integer,Profile> profiles, CountTable counts,Map <Integer,Set<Integer>> parents, Utils u, String query,String countQuery) throws SQLException{
		final Statement s = u.getStatement();
		ResultSet countResult = s.executeQuery(countQuery);
		if (countResult.next()){
			int count = countResult.getInt(1);
			counts.setSum(count);
		}
		else {
			throw new RuntimeException("Count query failed");
		}
		final PreparedStatement p = u.getPreparedStatement(query);
		for(Profile currentProfile : profiles.values()){
			Set <Integer> usedEntities = currentProfile.getUsedEntities();
			Set <Integer> usedAttributes = currentProfile.getUsedAttributes();
			for(Integer profileEntity : usedEntities){
				for (Integer curAttribute : usedAttributes){
					Set<Integer> allEntities = parents.get(profileEntity);
					if (allEntities == null){
						throw new RuntimeException("Entity " + u.getNodeName(profileEntity) + " seems to have no inheres_in_part_of parents");
					}
					else {
						for(Integer curEntity : allEntities){
							if (!counts.hasCount(curEntity, curAttribute)){
								p.setInt(1, curEntity);
								p.setInt(2, curAttribute);
								ResultSet eaResult = p.executeQuery();
								if(eaResult.next()){
									int count = eaResult.getInt(1);
									counts.addCount(curEntity, curAttribute, count);
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
			Set<Integer>attSet = table1.getAttributesForEntity(entity);
			for (Integer att : attSet){
				int count1 = table1.getRawCount(entity, att);
				if (table2.hasCount(entity, att)){
					int count2 = table2.getRawCount(entity, att);
					sum.addCount(entity, att, count1+count2);
				}
				else{
					sum.addCount(entity, att, count1);
				}
			}
		}
		for(Integer entity : table2.getEntities()){
			Set<Integer>attSet = table2.getAttributesForEntity(entity);
			for (Integer att : attSet){
				if (!sum.hasCount(entity, att)){
					sum.addCount(entity, att, table2.getRawCount(entity, att));
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
	int buildPhenotypeMatchCache(Map <Integer,Set<Integer>> phenotypeParentCache, PhenotypeScoreTable phenotypeScores, CountTable eaCounts, Utils u){
		int attOverlaps = 0;

		for (Integer curAtt : attributeSet){

			for(Integer currentTaxon : taxonProfiles.keySet()){
				Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
				if (!currentTaxonProfile.isEmpty()){
					for (Integer taxonEntity : currentTaxonProfile.getUsedEntities()){
						Set<Integer> teParents = phenotypeParentCache.get(taxonEntity);

						for(Integer currentGene : geneProfiles.keySet()){
							Profile currentGeneProfile = geneProfiles.get(currentGene);
							//u.writeOrDump("Checking taxon = " + u.getNodeName(currentTaxon) + " with " + u.getNodeName(curAtt) + " " + currentTaxonProfile.usesAttribute(curAtt) + " " + currentGeneProfile.usesAttribute(curAtt), null);
								//u.writeOrDump("Processing taxon = " + u.getNodeName(currentTaxon) + " with " + u.getNodeName(curAtt), null);
							for (Integer geneEntity : currentGeneProfile.getUsedEntities()){
								if (currentTaxonProfile.hasPhenotypeSet(taxonEntity, curAtt) && currentGeneProfile.hasPhenotypeSet(geneEntity, curAtt)){
									if (!phenotypeScores.hasScore(taxonEntity, geneEntity, curAtt)){
										Set<Integer> geParents = phenotypeParentCache.get(geneEntity);
										Set<Integer>matches = new HashSet<Integer>();
										for(Integer tParent : teParents){
											for (Integer gParent : geParents){
												if (tParent.equals(gParent))
													matches.add(tParent);
											}
										}
										double bestMatch = Double.MAX_VALUE;  //we're using fractions, so minimize
										Integer bestEntity = null;
										for(Integer ent : matches){
											if (eaCounts.hasCount(ent, curAtt)){
												double matchScore = eaCounts.getFraction(ent, curAtt);
												if (matchScore<bestMatch){
													bestMatch = matchScore;
													bestEntity = ent;
												}
												else if (matchScore < 0)
													throw new RuntimeException("Bad match score value < 0: " + matchScore + " " + u.getNodeName(ent) + " " + u.getNodeName(curAtt));
											}
										}
										if (bestMatch<Double.MAX_VALUE && bestEntity != null){
											phenotypeScores.addScore(taxonEntity,geneEntity,curAtt,CountTable.calcIC(bestMatch),bestEntity);
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


	void writePhenotypeMatchSummary(PhenotypeScoreTable phenotypeScores,Utils u, BufferedWriter bw3){
		u.writeOrDump("Taxon\tGene\tTaxon Entity\tGeneEntity\tAttribute\tBest Entity Match\tScore", bw3);
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			for(Integer currentGene : geneProfiles.keySet()){
				Profile currentGeneProfile = geneProfiles.get(currentGene);
				for(Integer tEntity : currentTaxonProfile.getUsedEntities()){
					for(Integer gEntity : currentGeneProfile.getUsedEntities()){
						for (Integer att : attributeSet){
							if (currentTaxonProfile.hasPhenotypeSet(tEntity, att) && currentGeneProfile.hasPhenotypeSet(gEntity, att)){
								if (phenotypeScores.hasScore(tEntity, gEntity, att)){
									Integer bestEntity = new Integer(phenotypeScores.getBestEntity(tEntity, gEntity,att));
									StringBuilder lineBuilder = new StringBuilder(200);
									String bestID =u.getNodeName(bestEntity);
									if (bestID == null)
										bestID = u.getNodeUID(bestEntity);
									if (bestID == null)
										bestID = Integer.toString(bestEntity);
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
									u.writeOrDump(lineBuilder.toString(),bw3);								
								}
							}
						}
					}
				}
			}

		}

	}
}
