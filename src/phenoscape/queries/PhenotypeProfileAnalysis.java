package phenoscape.queries;

/*
 * Copyright (c) 2007-2011 Peter E. Midford

 *
 * Licensed under the 'MIT' license (http://opensource.org/licenses/mit-license.php)
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 */



// How this works:
// Quick version
//    load taxa and assign each associated phenotype to a Set of phenotypes that share the same entity and qualities that map to the same attribute
//    for each parent taxon search for variation in each phenotype set among its children.  Variation is flagged when the union of the phenotype sets of the children differs from the intersection
//
//
//


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import phenoscape.queries.lib.CountTable;
import phenoscape.queries.lib.DistinctGeneAnnotationRecord;
import phenoscape.queries.lib.EntitySet;
import phenoscape.queries.lib.PermutedScoreSet;
import phenoscape.queries.lib.PhenotypeExpression;
import phenoscape.queries.lib.PhenotypeScoreTable;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.ProfileMap;
import phenoscape.queries.lib.ProfileScoreSet;
import phenoscape.queries.lib.SimilarityCalculator;
import phenoscape.queries.lib.TaxonPhenotypeLink;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;



public class PhenotypeProfileAnalysis {

	//private static final String TTOROOT = "TTO:0";
	private static final String OSTARIOCLUPEOMORPHAROOT = "TTO:253";
	//private static final String ASPIDORASROOT = "TTO:105426";
	//private static final String CALLICHTHYIDAEROOT = "TTO:11200";
	//private static final String SILURIFORMESROOT = "TTO:1380";
	//private static final String TESTROOT = "TTO:0000015";

	private String ANALYSISROOT = OSTARIOCLUPEOMORPHAROOT;
	private static final String CONNECTION_PROPERTIES_FILENAME = "connection.properties"; 

	private static final double IC_CUTOFF =  0.0;


	private static final String TAXONREPORTFILENAME = "../TaxonVariationReport.txt";
	private static final String GENEREPORTFILENAME = "../GeneVariationReport.txt";
	private static final String PHENOTYPEMATCHREPORTFILENAME = "../PhenotypeMatchReport.txt";
	private static final String PROFILEMATCHREPORTFILENAME = "../ProfileMatchReport.txt";
	private static final String TAXONGENEMAXICSCOREFILENAME = "../MaxICReport.txt";
	private static final String PERMUTATIONSCORESFILENAME = "../ProfileScores.txt";
	public static final String RANDOMIZATIONREPORTSFOLDER = "../RandomizationReports";



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

	static final String GENEENTITYCOUNTQUERY =
		"SELECT COUNT(*) FROM distinct_gene_annotation WHERE distinct_gene_annotation.phenotype_node_id IN "+
		"(SELECT phenotype.node_id from phenotype " +
		"JOIN link phenotype_inheres_in_part_of ON (phenotype_inheres_in_part_of.node_id = phenotype.node_id AND phenotype_inheres_in_part_of.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:inheres_in_part_of')) " +
		"WHERE phenotype_inheres_in_part_of.object_id =  ?)";

	static final String GENEENTITYQUERY = 
		"SELECT * FROM distinct_gene_annotation WHERE distinct_gene_annotation.phenotype_node_id IN "+
		"(SELECT phenotype.node_id from phenotype " +
		"JOIN link phenotype_inheres_in_part_of ON (phenotype_inheres_in_part_of.node_id = phenotype.node_id AND phenotype_inheres_in_part_of.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:inheres_in_part_of')) " +
		"WHERE phenotype_inheres_in_part_of.object_id =  ?)";


	static final String TAXONENTITYCOUNTQUERY =
		"SELECT COUNT(*) FROM asserted_taxon_annotation WHERE asserted_taxon_annotation.phenotype_node_id IN "+
		"(SELECT phenotype.node_id from phenotype " +
		"JOIN link phenotype_inheres_in_part_of ON (phenotype_inheres_in_part_of.node_id = phenotype.node_id AND phenotype_inheres_in_part_of.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:inheres_in_part_of')) " +
		"WHERE phenotype_inheres_in_part_of.object_id =  ?)";

	
	static final String TAXONENTITYQUERY =
		"SELECT * FROM asserted_taxon_annotation WHERE asserted_taxon_annotation.phenotype_node_id IN "+
		"(SELECT phenotype.node_id from phenotype " +
		"JOIN link phenotype_inheres_in_part_of ON (phenotype_inheres_in_part_of.node_id = phenotype.node_id AND phenotype_inheres_in_part_of.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:inheres_in_part_of')) " +
		"WHERE phenotype_inheres_in_part_of.object_id =  ?)";

	ProfileMap taxonProfiles;  //taxon_node_id -> Phenotype profile for taxon
	ProfileMap geneProfiles;  //gene_node_id -> Phenotype profile for gene


	Map<Integer,Integer>childDist = new HashMap<Integer,Integer>();

	//Map<Profile,Set<PhenotypeExpression>> taxonProfileParents = new HashMap<Profile,Set<PhenotypeExpression>>();
	//Map<Profile,Set<PhenotypeExpression>> geneProfileParents = new HashMap<Profile,Set<PhenotypeExpression>>();

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

	/**
	 * 
	 */
	int taxonPhenotypeLinkCount = 0;

	int totalAnnotations;   //cache total number of annotations
	
	EntitySet entityAnnotations;
	
	public Map<Integer,Set<Integer>> qualitySubsumers;
	
	
	/**
	 * This holds the number of taxa that have phenotype annotations and their parents respectively
	 */
	int annotatedTaxa=0;
	int parentsOfAnnotatedTaxa = 0;

	Random rand = new Random();

	static final Logger logger = Logger.getLogger(PhenotypeProfileAnalysis.class);

	public PhenotypeProfileAnalysis(Utils u) throws SQLException{
	}


	/**
	 * Entry point for analysis - this opens the KB and the report files, creates an instance of this class and invokes processing with it
	 * @param args currently any command-line args are ignored
	 */
	public static void main(String[] args) {
		Utils u = new Utils();
		String kbName;
		try {
			kbName = u.openKBFromConnections(CONNECTION_PROPERTIES_FILENAME);
		} catch (SQLException e) {
			System.err.println("Failed to open KB; will exit.  Reason was " + e.toString());
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
		Writer taxonWriter = null;
		Writer geneWriter = null;
		Writer phenoWriter = null;
		Writer profileWriter = null;
		Writer w5 = null;
		Date today;
		DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT);
		today = new Date();
		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
		String timeStamp = dateFormatter.format(today) + " " + timeFormatter.format(today) + " on " + kbName;		
		try {
			taxonWriter = new BufferedWriter(new FileWriter(outFile1));
			geneWriter = new BufferedWriter(new FileWriter(outFile2));
			phenoWriter = new BufferedWriter(new FileWriter(outFile3));
			profileWriter = new BufferedWriter(new FileWriter(outFile4));
			w5 = new BufferedWriter(new FileWriter(outFile5));
			u.writeOrDump(timeStamp, taxonWriter);
			u.writeOrDump(timeStamp, geneWriter);
			u.writeOrDump(timeStamp, phenoWriter);
			u.writeOrDump(timeStamp, profileWriter);
			u.writeOrDump(timeStamp, w5);
			u.writeOrDump("Starting analysis: " + timeStamp, null);
			listQuery.process(u, taxonWriter, geneWriter, phenoWriter, profileWriter,w5);
		} catch (SQLException e) {
			logger.error("Processing threw an SQL exception: " + e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Processing threw an IO exception: " + e.toString());
			e.printStackTrace();
		}
		finally{
			try {
				u.closeKB();
				taxonWriter.close();
				geneWriter.close();
				phenoWriter.close();
				profileWriter.close();
				w5.close();
			} catch (Exception e) {
				logger.error("An exception occurred while closing a report file or database connection: " + e.toString());
				e.printStackTrace();
			}
		}
	}


	/**
	 * This method drives most of the analysis - taxon loading and processing, gene loading and processing, generating phenotype parents,
	 * assigning IC scores, performing phenotype matches, and performing profile matches
	 * @param u handles db connections, name caching and some formatting
	 * @param taxonWriter the writer for the taxon variation report stream
	 * @param geneWriter the stream for the gene variation report
	 * @param phenoWriter the stream for the phenotype match report
	 * @param profileWriter the stream for the profile match report
	 * @param w5 the stream for the maxIC report
	 * @throws IOException this might be thrown when writers are closed; not sure why things work better when writers are closed here
	 * @throws SQLException pass this through to next level, which will catch it.
	 */
	void process(Utils u,Writer taxonWriter, Writer geneWriter, Writer phenoWriter, Writer profileWriter, Writer w5) throws IOException, SQLException{
		if (logger.isInfoEnabled())
			logger.info("Setting up Attribute table");
		qualityNodeID = u.getQualityNodeID();   //set to the root of PATO
		attributeMap = u.setupAttributes();
		
		int qCount = 0;
		for(Integer q_id : attributeMap.keySet()){
			if (attributeMap.get(q_id).intValue() == qualityNodeID)
				qCount++;
		}
		System.out.println("qCount = " + qCount);
		
		PhenotypeExpression.getEQTop(u);   //just to initialize early.

		attributeSet.addAll(attributeMap.values());		
		attributeSet.add(qualityNodeID);

		entityAnnotations = new EntitySet(u);
		
		if (logger.isInfoEnabled())
			logger.info("Loading all phenotype quality parents");
		qualitySubsumers = u.buildPhenotypeSubsumers();
		if (logger.isInfoEnabled()){
			logger.info("Finished loading all phenotype quality parents");
			logger.info("Loading taxon entity annotations");
		}
		entityAnnotations.fillTaxonPhenotypeAnnotationsToEntities();
		if (logger.isInfoEnabled())
			logger.info("Finished loading; checking counts");
		int ata = entityAnnotations.annotationTotal();
		if (u.countAssertedTaxonPhenotypeAnnotations() != ata){
			logger.error("Annotation counts for taxa did not match.  Direct count = " + u.countAssertedTaxonPhenotypeAnnotations() + " Entity set sum = " + ata);
		}
		
		if (logger.isInfoEnabled())
			logger.info("Loading gene entity annotations");
		entityAnnotations.fillGenePhenotypeAnnotationsToEntities();
		int tea = entityAnnotations.annotationTotal();
		int dga = u.countDistinctGenePhenotypeAnnotations();
		if (logger.isInfoEnabled())
			logger.info("Finished loading; checking counts dga = " + dga);
		if (tea != ata+dga){
			logger.error("Annotation counts for genes did not match.  Direct count = " + u.countDistinctGenePhenotypeAnnotations() + " Entity set sum = " + (tea -ata));			
		}
		
		totalAnnotations = ata + dga;
		if (logger.isDebugEnabled()){
			logger.debug("Distinct Gene annotations = " + dga);
			logger.debug("Asserted Taxon annotations = " + ata);
			logger.debug("Total annotations (dga + ata)" + totalAnnotations);
		}
		
		// process taxa annotations
		if (logger.isInfoEnabled())
			logger.info("Building Taxonomy Tree");
		TaxonomyTree t = new TaxonomyTree(ANALYSISROOT,u);
		t.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = getAllTaxonPhenotypeLinksFromKB(t,u);
		int removedCount = removeSymmetricLinks(allLinks,u);
		taxonProfiles = loadTaxonProfiles(allLinks,u, attributeMap, qualityNodeID, badTaxonQualities);
		countAnnotatedTaxa(t,t.getRootNodeID(),taxonProfiles,u);
		int eaCount = countEAAnnotations(taxonProfiles,u);
		u.writeOrDump("Raw count of asserted taxon annotations = " + ata, taxonWriter);
		u.writeOrDump("Count of distinct taxon-phenotype assertions (EQ level): " + taxonPhenotypeLinkCount, taxonWriter);
		u.writeOrDump("Count of assertions with symmetric properties where one member of a pair of inverses was removed: " + removedCount, taxonWriter);
		u.writeOrDump("Count of distinct taxon-phenotype assertions (EA level; not filtered for variation): " + eaCount, taxonWriter);
		u.writeOrDump("Count of annotated taxa = " + annotatedTaxa, taxonWriter);
		u.writeOrDump("Count of parents of annotated taxa = " + parentsOfAnnotatedTaxa, taxonWriter);

		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);

		traverseTaxonomy(t, t.getRootNodeID(), taxonProfiles, taxonVariation, u);
		t.report(u, taxonWriter);
		taxonVariation.variationReport(u,taxonWriter);	
		u.writeOrDump("\nList of qualities that were placed under quality as an attribute by default\n", taxonWriter);
		for(Integer bad_id : badTaxonQualities.keySet()){
			u.writeOrDump(u.getNodeName(bad_id) + " " + badTaxonQualities.get(bad_id), taxonWriter);
		}
		taxonWriter.close();
		logger.info("Finished writing taxon profiles");
		flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		if (taxonProfiles.isEmpty()){
			logger.fatal("No taxa in Profile Set");
			throw new RuntimeException("No taxa in Profile Set");
		}

		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);

		geneProfiles = processGeneExpression(geneVariation,u, geneWriter);
		geneVariation.variationReport(u, geneWriter);
		u.writeOrDump("\nList of qualities that were placed under quality as an attribute by default\n", geneWriter);
		for(Integer bad_id : badGeneQualities.keySet()){
			u.writeOrDump(u.getNodeName(bad_id) + " " + badGeneQualities.get(bad_id), geneWriter);
		}

		geneWriter.close();

		if (logger.isInfoEnabled())
			logger.info("Building entity parents");
		Map <Integer,Set<Integer>> entityParentCache = new HashMap<Integer,Set<Integer>>();
		Map <Integer,Set<Integer>> entityChildCache = new HashMap<Integer,Set<Integer>>();
		u.setupEntityParents(entityParentCache,entityChildCache);
		if (logger.isInfoEnabled()){
			logger.info("Finished entity parents; Start building EQ parents");
			logger.info("Building EQ parents");
		}
		/* Test introduction of phenotypeParentCache, which should map an attribute level EQ to all its parents via inheres_in_part_of entity parents and is_a quality parents (cross product) */
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		buildEQParents(phenotypeParentCache,entityParentCache,u);

		if (logger.isInfoEnabled())
			logger.info("Filling count table");
		CountTable<PhenotypeExpression> phenotypeCountsForGenes = fillPhenotypeCountTable(geneProfiles, taxonProfiles,phenotypeParentCache, u, GENEPHENOTYPECOUNTQUERY, GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		CountTable<PhenotypeExpression> phenotypeCountsToUse = phenotypeCountsForGenes;

		//CountTable<Integer> entityCountsForGenes = fillGeneEntityCountTable(geneProfiles, entityParentCache, u, GENEENTITYCOUNTQUERY, u.countDistinctGeneEntityPhenotypeAnnotations());
		//CountTable<Integer> entityCountsForTaxa = fillTaxonEntityCountTable(taxonProfiles, entityParentCache, u, TAXONENTITYCOUNTQUERY, u.countDistinctTaxonEntityPhenotypeAnnotations());

		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();

		if (logger.isInfoEnabled())
			logger.info("Done building entity parents; building phenotype match cache");
		buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, phenotypeCountsToUse, u);
		taxonWriter.close();
		if (logger.isInfoEnabled())
			logger.info("Finished building phenotype match cache; Writing Phenotype match summary");
		writePhenotypeMatchSummary(phenotypeScores,u,phenoWriter);		
		phenoWriter.close();

		if (logger.isInfoEnabled())
			logger.info("Finished Writing Phenotype match summary");

		writeTaxonGeneMaxICSummary(phenotypeScores,u,w5);
		w5.close();
		logger.info("Finished Writing maxIC for taxon/gene summary");		

		logger.info("Calculating Profile Scores");
		fillUnionSets(phenotypeParentCache);
		List<PermutedProfileScore> pMaxICScores = new ArrayList<PermutedProfileScore>();
		List<PermutedProfileScore> pMeanICScores = new ArrayList<PermutedProfileScore>();
		PermutedScoreSet s = new PermutedScoreSet(taxonProfiles,geneProfiles,entityParentCache, entityChildCache, phenotypeScores, u);
		s.setTotalAnnotations(totalAnnotations);
		s.setRandom(rand);
		s.calcPermutedProfileScores();
		
		logger.info("Writing median/mean distribution reports");

		s.writeDist(RANDOMIZATIONREPORTSFOLDER);


		//CountTable<Integer> sumTable = entityCountsForGenes.addTable(entityCountsForTaxa);
		//CountTable<Integer> entityCountsToUse = sumTable;

		profileMatchReport(phenotypeScores,s,profileWriter,entityParentCache,entityChildCache, entityAnnotations, phenotypeParentCache, u);
		profileWriter.close();


		File outFile6 = new File(PERMUTATIONSCORESFILENAME);
		Writer w6 = new BufferedWriter(new FileWriter(outFile6));		
		Date today;
		DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT);
		today = new Date();
		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
		String timeStamp = dateFormatter.format(today) + " " + timeFormatter.format(today);		

		u.writeOrDump(timeStamp, w6);

		w6.close();
		logger.info("Done");
	}

	int removeSymmetricLinks(Map<Integer, Set<TaxonPhenotypeLink>> links, Utils u) throws SQLException {
		int removeCount = 0;
		for (Integer taxon : links.keySet()){
			removeCount += removeSymmetricLinksOneTaxon(links.get(taxon),u);
		}		
		return removeCount;
	}
	
	int removeSymmetricLinksOneTaxon(final Set<TaxonPhenotypeLink> lset, Utils u) throws SQLException{
		int removeCount = 0;
		final Set<TaxonPhenotypeLink> dups = new HashSet<TaxonPhenotypeLink>();  //need this to avoid modifying lset inside loop
		for (TaxonPhenotypeLink l : lset){
			if (u.isSymmetricProperty(l.getQualityNodeID())){
				if (l.getEntityUID() != null && l.getRelatedEntityUID() != null && l.getEntityUID().compareTo(l.getRelatedEntityUID())<0){
					dups.add(l);  
				}
			} //otherwise ignore
		}
		removeCount += dups.size();
		lset.removeAll(dups);
		return removeCount;
	}


	/**
	 * This writes the report containing profile matches
	 * @param phenotypeScores holds the IC scores for each pair of matchable phenotypes
	 * @param w connected to the file to receive report, or null for console output
	 * @param u just used for writing
	 * @throws SQLException 
	 */
	void profileMatchReport(PhenotypeScoreTable phenotypeScores,PermutedScoreSet scores, Writer w,Map<Integer, Set<Integer>> entityParentCache,Map<Integer, Set<Integer>> entityChildCache, EntitySet entityAnnotations,Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache, Utils u) throws SQLException{
		ProfileScoreSet.writeHeader(u,w);
		long zeroCount = 0;
		for(Integer currentTaxon : taxonProfiles.domainSet()){
			for(Integer currentGene : geneProfiles.domainSet()){
				ProfileScoreSet thisMatch = matchOneProfilePair(currentTaxon,currentGene,scores,phenotypeScores,entityParentCache,entityChildCache,entityAnnotations,phenotypeParentCache, u);
				if (thisMatch.isNonZero())
					thisMatch.writeScores(u, w);
				else
					zeroCount++;
			}
		}
		u.writeOrDump("Pairs with zero score = " + zeroCount, w);
	}

	
	/**
	 * 
	 * @param taxon database id of the taxon
	 * @param gene database id of the gene
	 * @param scores
	 * @param phenotypeScores
	 * @param entityParentCache
	 * @param entityChildCache
	 * @param entityAnnotations
	 * @param phenotypeParentCache
	 * @param u
	 * @return
	 * @throws SQLException
	 */
	ProfileScoreSet matchOneProfilePair(Integer taxon, 
			Integer gene,
			PermutedScoreSet scores, 
			PhenotypeScoreTable phenotypeScores,
			Map<Integer, Set<Integer>> entityParentCache, 
			Map<Integer, Set<Integer>> entityChildCache,
			EntitySet entityAnnotations, 
			Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache, 
			Utils u) throws SQLException{
		final Profile taxonProfile = taxonProfiles.getProfile(taxon);  //get the profile associated with (the children of) taxon
		final Profile geneProfile = geneProfiles.getProfile(gene);  //get the profile associated with the gene
		ProfileScoreSet result = new ProfileScoreSet(taxon, gene,taxonProfile.getAllEAPhenotypes(), geneProfile.getAllEAPhenotypes());
		PermutedProfileScore pScore = scores.matchProfileSizes(taxonProfile.getAllEAPhenotypes().size(),geneProfile.getAllEAPhenotypes().size());

		//		Set<Integer> taxonEntityUnion = new HashSet<Integer>();
		//		for(Integer e : taxonProfile.getUsedEntities()){
		//			taxonEntityUnion.addAll(entityParentCache.get(e));
		//		}

//		final List<Integer>taxonEntityList = buildEntityList(taxonProfile,u);
//
//		Collection<AnnotationPair>taxonBag = new ArrayList<AnnotationPair>();
//		Set<Integer>allTaxonEntities = new HashSet<Integer>();
//		for(Integer ent : taxonEntityList){
//			if (!allTaxonEntities.contains(ent)){
//				allTaxonEntities.addAll(entityChildCache.get(ent));
//				if (entityAnnotations.hasEntity(ent))
//					taxonBag.addAll(entityAnnotations.getAnnotations(ent));
//			}
//		}

		//		Set<Integer> geneEntityUnion = new HashSet<Integer>();
		//		for(Integer e : geneProfile.getUsedEntities()){
		//			geneEntityUnion.addAll(entityParentCache.get(e));
		//		}

//		final List<Integer>geneEntityList = buildEntityList(geneProfile,u);
//
//		Collection<AnnotationPair>geneBag = new ArrayList<AnnotationPair>();
//		Set<Integer>allGeneEntities = new HashSet<Integer>();
//		for(Integer ent : geneEntityList){
//			if (!allGeneEntities.contains(ent)){
//				allGeneEntities.addAll(entityChildCache.get(ent));
//				if (entityAnnotations.hasEntity(ent))
//					geneBag.addAll(entityAnnotations.getAnnotations(ent));
//
//			}
//		}
		//SimilarityCalculator<AnnotationPair> sc = new SimilarityCalculator<AnnotationPair>(totalAnnotations);

		//Collection<AnnotationPair> entityIntersection = sc.collectionIntersection(taxonBag, geneBag);

		//double hyperSSScore = sc.simHyperSS(taxonBag.size(),geneBag.size(),entityIntersection.size());
		// calculate maxIC
		//double maxIC = calcMaxIC(taxonProfile.getAllEAPhenotypes(), geneProfile.getAllEAPhenotypes(),phenotypeScores);
		double medianICScore = SimilarityCalculator.calcMedianIC(taxonProfile.getAllEAPhenotypes(),geneProfile.getAllEAPhenotypes(),phenotypeScores);
		double meanICScore = SimilarityCalculator.calcMeanIC(taxonProfile.getAllEAPhenotypes(),geneProfile.getAllEAPhenotypes(),phenotypeScores,u);

		result.setMedianICScore(medianICScore);
		result.setMeanICScore(meanICScore);

		double[] meanICPVs = pScore.get_pvalues(meanICScore, PermutedProfileScore.ScoreType.MEANIC); 
		result.setMeanPV(meanICPVs[1]);
		result.setMeanTiesPV(meanICPVs[0]);

		double[] medianICPVs = pScore.get_pvalues(medianICScore, PermutedProfileScore.ScoreType.MEDIANIC); 
		result.setMedianPV(medianICPVs[1]);
		result.setMedianTiesPV(medianICPVs[0]);

		// testing p-value distribution
		
		return result;
	}






	List<Integer> buildEntityList(Profile profile, Utils u){
		return buildEntityListfromListofPhenotypes(profile.getAllEAPhenotypes(),u);
	}

	List<Integer> buildEntityListfromListofPhenotypes(Collection<PhenotypeExpression> pel, Utils u){
		final List<Integer> result = new ArrayList<Integer>();
		for(PhenotypeExpression p : pel){
			if (!p.isSimpleQuality()){
				Integer e = p.getEntity();
				String eUID = u.getNodeUID(e); 
				if (eUID != null){
					if (!SimilarityCalculator.SPATIALPOSTCOMPUIDPREFIX.equals(eUID.substring(0,5))){
						result.add(p.getEntity());
					}
				}
				if (p.getEntity2() != PhenotypeExpression.VOIDENTITY){
					e = p.getEntity();
					eUID = u.getNodeUID(e); 
					if (eUID != null){
						if (!SimilarityCalculator.SPATIALPOSTCOMPUIDPREFIX.equals(eUID.substring(0,5))){
							result.add(e);
						}
					}
				}
			}
		}
		return result;
	}


	/**
	 * 
	 * @param taxonProfile
	 * @param geneProfile
	 * @param phenotypeScores
	 * @return
	 */
	double calcMaxIC(Set<PhenotypeExpression> taxonPhenotypes, Set<PhenotypeExpression> genePhenotypes, PhenotypeScoreTable phenotypeScores){
		double maxPhenotypeMatch = 0;
		for (PhenotypeExpression tPhenotype : taxonPhenotypes){
			for (PhenotypeExpression gPhenotype : genePhenotypes){
				if(phenotypeScores.hasScore(tPhenotype,gPhenotype))
					if (phenotypeScores.getScore(tPhenotype,gPhenotype) > maxPhenotypeMatch){
						maxPhenotypeMatch = phenotypeScores.getScore(tPhenotype,gPhenotype);
					}
			}
		}
		return maxPhenotypeMatch;
	}


	/**
	 * Creates profile objects for each taxon and installs into taxonProfiles 
	 * @param allLinks
	 * @param u
	 * @param reportWriter
	 * @throws SQLException
	 */
	ProfileMap loadTaxonProfiles(Map<Integer, Set<TaxonPhenotypeLink>> allLinks, Utils u, Map<Integer,Integer> attMap,int nodeIDofQuality, Map<Integer,Integer> badQualities) throws SQLException{	
		ProfileMap taxonProfiles = new ProfileMap();
		final Set<Integer> taxonSet = allLinks.keySet();
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
						myProfile.addPhenotype(link.getEntityNodeID(),qualityNodeID,link.getPhenotypeNodeID());
					}
					else {
						badQualities.put(linkQualityID, 1);
						u.putNodeUIDName(linkQualityID, link.getQualityUID(), link.getQualityLabel());
						myProfile.addPhenotype(link.getEntityNodeID(),qualityNodeID,link.getPhenotypeNodeID());
					}
				}
				u.putNodeUIDName(link.getEntityNodeID(), link.getEntityUID(),link.getEntityLabel());
			}
			taxonProfiles.addProfile(taxonID,myProfile);
		}
		return taxonProfiles;
	}


	/**
	 * 
	 * @param t provides the set of taxa to query against the KB
	 * @param u provides access to the KB connection
	 */
	Map<Integer,Set<TaxonPhenotypeLink>> getAllTaxonPhenotypeLinksFromKB(TaxonomyTree t, Utils u) throws SQLException{
		Map<Integer,Set<TaxonPhenotypeLink>> result = new HashMap<Integer,Set<TaxonPhenotypeLink>>();
		Set<Integer> taxonSet = t.getAllTaxa();
		for (Integer taxonID : taxonSet){
			Set<TaxonPhenotypeLink> tLinks = getTaxonPhenotypeLinksFromKB(u,taxonID);
			result.put(taxonID, tLinks);
		}
		return result;
	}

	/**
	 * 
	 * @param u used to get a prepared statement from the KB connection
	 * @param taxonID nodeID of the taxon 
	 * @return links (marshalled as TaxonPhenotypeLinks) with the specified taxon as subject
	 * @throws SQLException
	 */
	Set<TaxonPhenotypeLink> getTaxonPhenotypeLinksFromKB(Utils u, int taxonID) throws SQLException{
		final PreparedStatement p = u.getPreparedStatement(TaxonPhenotypeLink.getQuery());
		final Set<TaxonPhenotypeLink> result = new HashSet<TaxonPhenotypeLink>();
		p.setInt(1, taxonID);
		ResultSet ts = p.executeQuery();
		while (ts.next()){
			TaxonPhenotypeLink l = new TaxonPhenotypeLink(ts);
			taxonPhenotypeLinkCount++;
			result.add(l);
		}
		return result;
	}


	/**
	 * 
	 * @param taxonProfiles2
	 * @param u
	 * @return
	 */
	int countEAAnnotations(ProfileMap taxonProfiles2, Utils u){
		int result = 0;
		if (logger.isDebugEnabled())
			logger.debug("Number of profiles = " + taxonProfiles2.domainSize());
		for (Integer exhibitor : taxonProfiles2.domainSet()){
			Profile curProfile = taxonProfiles2.getProfile(exhibitor);
			for (Integer ent : curProfile.getUsedEntities()){
				for (Integer att : curProfile.getUsedAttributes()){
					if (curProfile.hasPhenotypeSet(ent, att))
						//logger.info("Taxon: " + u.getNodeUID(exhibitor) + " Entity: " + u.getNodeName(ent) + " Attribute: " + u.getNodeName(att));
						result++;
				}
			}
		}
		return result;
	}



	/**
	 * @param t holds the taxonomy loaded from the KB
	 * @param taxon current taxon in recursive traversal
	 * @param taxonProfiles2 map from a taxon to its loaded (and assumed unaltered) profile
	 * @param u passed to nodeIsInternal to format an error message
	 * This method simply counts the number of annotated taxa and the number of taxa that are parents of these annotated taxa
	 * Class fields annotatedTaxa and parentsOfAnnotatedTaxa will hold the totals at the end
	 */
	void countAnnotatedTaxa(TaxonomyTree t, Integer taxon, ProfileMap taxonProfiles2, Utils u){
		if (t.nodeIsInternal(taxon,u)){
			final Set<Integer>children = t.getTable().get(taxon);
			boolean hasAnnotatedChild = false;
			for (Integer child : children){
				if (taxonProfiles2.nonEmptyProfile(child)){
					annotatedTaxa++;
					hasAnnotatedChild = true;
				}
				countAnnotatedTaxa(t,child,taxonProfiles2,u);
			}
			if (hasAnnotatedChild)
				parentsOfAnnotatedTaxa++;
		}
	}


	/**
	 * This method marks taxon phenotypes that display variation. 
	 *  There are two additional twists: the absence of an annotation for a particular entity-attribute
	 * combination in a child taxon is treated as variation if there are sister taxa with annotation for the same combination, but if the taxon
	 * has no annotations whatsoever, it is ignored.
	 * @param t
	 * @param taxon
	 * @param taxonProfiles2
	 * @param u
	 * @throws SQLException 
	 */
	void traverseTaxonomy(TaxonomyTree t, Integer taxon, ProfileMap taxonProfiles2, VariationTable variation, Utils u) throws SQLException{
		if (t.nodeIsInternal(taxon, u)){
			//build set of children
			final Set<Integer> children = t.getTable().get(taxon);
			final Set<Profile> childProfiles = new HashSet<Profile>();
			for(Integer child : children){
				traverseTaxonomy(t,child,taxonProfiles2, variation, u);
				childProfiles.add(taxonProfiles2.getProfile(child));
			}
			final Profile parentProfile = taxonProfiles2.getProfile(taxon);			
			//This builds the union and intersection sets (upwards) sets annotations for each taxon with childProfiles
			//Changed to propagate the intersection rather than the union 21 Feb
			final Set<Integer> usedEntities = new HashSet<Integer>();
			final Set<Integer> usedAttributes = new HashSet<Integer>();
			for (Profile childProfile : childProfiles){
				usedEntities.addAll(childProfile.getUsedEntities());
				usedAttributes.addAll(childProfile.getUsedAttributes());
			}
			//logger.info("Processing taxon: " + u.getNodeName(taxon));
			for(Integer ent : usedEntities){
				for(Integer att : usedAttributes){
					if (taxonAnalysis(childProfiles,ent,att,parentProfile)){
						variation.addExhibitor(ent,att,taxon);//add to the variation table
					}
				}
			}
		}
	}

	/**
	 * This method calculates the union and intersection sets for one taxon, one entity, and one attribute (including the qualities
	 * subsumed under the attribute). Currently Union sets are formed using subsumption of phenotypes, but intersection sets ignore
	 * subsumption.  Note that because profiles are already sorted by entities, subsumption will only be detected 'along the quality axis.'  
	 * @param childProfiles profiles of the children of this node
	 * @param ent specifies the entity (as term id)
	 * @param att specifies the attribute (as term id)
	 * @param parentProfile the profile of this taxon
	 * @return true if variation was inferred among the children of this taxon for the specified E and A
	 */
	boolean taxonAnalysis(Set<Profile> childProfiles,Integer ent, Integer att,Profile parentProfile){
		Set <Integer>unionSet = new HashSet<Integer>();
		Set <Integer>intersectionSet = new HashSet<Integer>();
		for (Profile childProfile : childProfiles){
			if (!childProfile.isEmpty() && childProfile.hasPhenotypeSet(ent, att)){
				//unionSet.addAll(childProfile.getPhenotypeSet(ent,att));
				unionSet = subsumingUnion(unionSet,childProfile.getPhenotypeSet(ent,att));
			}
		}					
		intersectionSet.addAll(unionSet);  // start intersection from the union and intersect each child in turn
		for (Profile childProfile : childProfiles){
			if (!childProfile.isEmpty()){
				if (childProfile.hasPhenotypeSet(ent, att)){
					intersectionSet.retainAll(childProfile.getPhenotypeSet(ent,att));  //turns out non-subsuming intersection is correct here
					//intersectionSet = subsumingIntersection(intersectionSet,childProfile.getPhenotypeSet(ent, att));
				}
				else {
					intersectionSet.clear();	//if a child has no annotations to this ent/att pair, this will tag variation
				}
			}
		}
		// now the union and intersection sets reflect the sets of children.
		// add the asserted phenotypes of this parent (if any) to both union and intersection
		if (parentProfile.hasPhenotypeSet(ent, att)){
			unionSet.addAll(parentProfile.getPhenotypeSet(ent, att));
			intersectionSet.addAll(parentProfile.getPhenotypeSet(ent,att));  //this union operation is correct!
		}
		parentProfile.setPhenotypeSet(ent, att, intersectionSet);
		//logger.info("Entity: " + entName + "; Attribute: " + attName + "; Union: " + unionSet + "; Intersection: " + intersectionSet);
		if (!unionSet.equals(intersectionSet)){  //if variation
			return true;
		}
		return false;
	}

	
	// SubsumingUnion and Intersection operate on sets of Integers, retrieving subsumption relations for the phenotypes represented by those
	// integers from the quality subsumers table (accessed by phenotypeSubsumes()).
	Set<Integer>subsumingUnion(Set<Integer>unionSet, Set<Integer>newSet){
		for (Integer phenotype : newSet){
			unionSet = addOneToUnionSubsuming(unionSet,phenotype);
		}
		return unionSet;
	}
	
	Set<Integer>addOneToUnionSubsuming(Set<Integer>unionSet, Integer phenotype){
		Set<Integer>toremove = new HashSet<Integer>();
		for (Integer unionMember : unionSet){
			if (phenotypeSubsumes(phenotype,unionMember)){  //phenotype subsumes an entry in the unionset
				toremove.add(unionMember);
			}
			else if (phenotypeSubsumes(unionMember,phenotype)){  //if an entry in the unionset subsumes the phenotype, we're done
				return unionSet;
			}
		}
		if (!toremove.isEmpty()){
			unionSet.removeAll(toremove);
		}
		unionSet.add(phenotype);
		return unionSet;
	}
	
	
	
	/**
	 * This method removes all phenotypes that don't indicate variation from the profile.  
	 * Note: After this runs, cells in each profile will either contain the intersection set (which may be empty)
	 * or null which indicates no variation in the entity attribute combination that addresses the cell.  It is
	 * important to notice that an empty set still indicates variation (and will frequently do so), and should not
	 * be confused with a null entry.  Code downstream from this method just look at whether there is a non-null value
	 * in profile cells.
	 */
	void flushUnvaryingPhenotypes(ProfileMap taxonProfiles2, VariationTable variation, Utils u){
		for (Integer taxon : taxonProfiles2.domainSet()){
			Profile p = taxonProfiles2.getProfile(taxon);
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
		taxonProfiles2.removeEmptyProfiles();
	}

	/**
	 * Name is a little dicy, but better than GeneVariation
	 */
	ProfileMap processGeneExpression(VariationTable variation, Utils u, Writer reportWriter) throws SQLException{
		ProfileMap profiles = new ProfileMap();
		int annotationCount = 0;
		int usableAnnotationCount = 0;
		Set<Integer>uniqueGenes = new HashSet<Integer>();
		Collection<DistinctGeneAnnotationRecord> annotationList = getAllGeneAnnotationsFromKB(u);
		for (DistinctGeneAnnotationRecord annotation : annotationList){
			final int geneID = annotation.getGeneID();
			if (true){
				final int phenotype_id = annotation.getPhenotypeID();
				final int entity_id = annotation.getEntityID();
				final int quality_id = annotation.getQualityID();
				if (attributeMap.containsKey(annotation.getQualityID())){
					final int attribute_id = attributeMap.get(annotation.getQualityID());
					profiles.addPhenotype(geneID,annotation.getEntityID(),attribute_id,phenotype_id);
					variation.addExhibitor(entity_id, attribute_id, geneID);
					usableAnnotationCount++;
				}
				else{
					profiles.addPhenotype(geneID, entity_id, qualityNodeID, phenotype_id);
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
				//String fullName = u.getGeneFullName(geneID);
				u.putNodeUIDName(geneID, annotation.getGeneUID(),annotation.getGeneLabel());
				annotationCount++;
				uniqueGenes.add(annotation.getGeneID());
			}
		}
		u.writeOrDump("Count of genes with annotations " + profiles.domainSize() + "; Distinct Gene-Phenotype assertions: " + annotationCount +  "; Assertions with phenotype attributes other than Quality " + usableAnnotationCount, reportWriter);
		return profiles;
	}

	/**
	 * 
	 * @param u Utils object provides access to database connection
	 * @return
	 * @throws SQLException
	 */
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
	 * For each phenotype in the taxonProfile, this builds the set of class expression subsumers (both EQ and Q) of the phenotype.
	 * Changed to save the parents (iipo parents) indexed by the entity, which is more efficient and useful later on.
	 * 
	 * @param entityParentCache
	 * @param u
	 * @throws SQLException
	 */
	void buildEQParents(Map<PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache, Map <Integer,Set<Integer>> entityParentCache, Utils u) throws SQLException{
		for(Integer curAtt : attributeSet){
			for(Integer curEntity : entityParentCache.keySet()){
				PhenotypeExpression curEQ = new PhenotypeExpression(curEntity,curAtt);
				buildEQParent(phenotypeParentCache,curEQ,entityParentCache,u);
			}
		}
	}

	/**
	 * builds the set of class expression subsumers for one phenotype
	 * @param phenotypeParentCache
	 * @param curEQ
	 * @param entityParentCache
	 * @param u
	 */
	void buildEQParent(Map<PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache, PhenotypeExpression curEQ,Map <Integer,Set<Integer>> entityParentCache, Utils u) throws SQLException{
		if (!phenotypeParentCache.containsKey(curEQ)){
			final Integer curEntity = curEQ.getEntity();
			final Integer curAtt = curEQ.getQuality();
			final Set<Integer>qualityParentSet = u.collectQualityParents(curAtt);
			Set<Integer> entityParentSet = entityParentCache.get(curEntity);
			Set<PhenotypeExpression> eqParentSet = new HashSet<PhenotypeExpression>();  //pass phenotype id, list of entities returned
			phenotypeParentCache.put(curEQ,eqParentSet);
			if (entityParentSet.isEmpty() || qualityParentSet.isEmpty()){
				curEQ.fillNames(u);
				if (logger.isInfoEnabled()){
					logger.info("Failed to add Parents of: " + curEQ);
					if (entityParentSet.isEmpty() && qualityParentSet.isEmpty())
						logger.info("Because both parent sets are empty");
					else if (entityParentSet.isEmpty())
						logger.info("Because the entity parent set is empty");
					else
						logger.info("Because the parent set of " + curAtt + " is empty");
				}
			}
			for(Integer qualParent : qualityParentSet){
				for(Integer entParent : entityParentSet){
					PhenotypeExpression newParentEQ = new PhenotypeExpression(entParent,qualParent);
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
		
		
	/**
	 * 
	 * @param profiles
	 * @param counts
	 * @param parents
	 * @throws SQLException 
	 */
	CountTable<PhenotypeExpression> fillPhenotypeCountTable(ProfileMap geneProfiles,ProfileMap taxonProfiles, Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache, Utils u, String phenotypeQuery, String qualityQuery, int annotationCount) throws SQLException{
		final CountTable<PhenotypeExpression> result = new CountTable<PhenotypeExpression>();
		final PreparedStatement phenotypeStatement = u.getPreparedStatement(phenotypeQuery);
		final PreparedStatement qualityStatement = u.getPreparedStatement(qualityQuery);
		final PhenotypeExpression topEQ = PhenotypeExpression.getEQTop(u);
		result.addCount(topEQ, annotationCount);
		result.setSum(annotationCount);
		for(Profile currentProfile : geneProfiles.range()){
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
							if (!result.hasCount(phenotypeParent)){
								if (phenotypeParent.isSimpleQuality()){
									qualityStatement.setInt(1, phenotypeParent.getQuality());
									ResultSet qResult = qualityStatement.executeQuery();
									if(qResult.next()){
										int count = qResult.getInt(1);
										result.addCount(phenotypeParent, count);
										phenotypeParent.fillNames(u);
										if (count == 0){
											System.err.print(" ** Adding count to parent: " + phenotypeParent + "; count = " + count);
											if (result.hasCount(phenotypeParent)){
												System.err.println("; prior count = " + result.getRawCount(phenotypeParent));
											}
											else{
												System.err.println();
											}
										}
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
										phenotypeParent.fillNames(u);
										if (count == 0){
											System.err.print(" ** Adding count to parent: " + phenotypeParent + "; count = " + count);
											if (result.hasCount(phenotypeParent)){
												System.err.println("; prior count = " + result.getRawCount(phenotypeParent));
											}
											else{
												System.err.println();
											}
										}
										result.addCount(phenotypeParent, count);
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
		for(Profile currentProfile : taxonProfiles.range()){
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
						//curEQ.fillNames(u);
						//System.out.println("Processing " + curEQ);
						for(PhenotypeExpression phenotypeParent : allParents){
							if (!result.hasCount(phenotypeParent)){
								result.addCount(phenotypeParent,0);
							}
						}
					}
				}
			}
		}
		return result;
	}


	/**
	 * 
	 * @param profiles
	 * @param counts
	 * @param parents
	 * @throws SQLException 
	 */
	CountTable<Integer> fillGeneEntityCountTable(ProfileMap geneProfiles, Map <Integer,Set<Integer>> entityParentCache, Utils u, String entityQuery, int annotationCount) throws SQLException{
		final CountTable<Integer> result = new CountTable<Integer>();
		final PreparedStatement entityStatement = u.getPreparedStatement(entityQuery);
		result.setSum(annotationCount);
		for(Profile currentProfile : geneProfiles.range()){
			Set <Integer> usedEntities = currentProfile.getUsedEntities();
			for(Integer profileEntity : usedEntities){
				int curEntity = profileEntity;
				String entityName = u.getNodeName(curEntity);
				Set<Integer> allParents = entityParentCache.get(curEntity);
				if (allParents == null){
					logger.error("The Entity " + entityName + " seems to have no parents");
				}
				else {
					for(Integer entityParent : allParents){
						if (!result.hasCount(entityParent)){
							entityStatement.setInt(1, entityParent);
							ResultSet eResult = entityStatement.executeQuery();
							if(eResult.next()){
								int count = eResult.getInt(1);
								result.addCount(entityParent, count);
							}
							else {
								throw new RuntimeException("count query failed for entity " + u.getNodeName(entityParent));
							}
						}
					}
				}
			}
		}
		return result;
	}



	/**
	 * 
	 * @param taxonProfiles
	 * @param entityParentCache
	 * @param u
	 * @param entityQuery
	 * @param annotationCount
	 * @return
	 * @throws SQLException
	 */
	CountTable<Integer> fillTaxonEntityCountTable(ProfileMap taxonProfiles, Map <Integer,Set<Integer>> entityParentCache, Utils u, String entityQuery, int annotationCount) throws SQLException{
		final CountTable<Integer> result = new CountTable<Integer>();
		final PreparedStatement entityStatement = u.getPreparedStatement(entityQuery);
		result.setSum(annotationCount);
		for(Profile currentProfile : taxonProfiles.range()){
			Set <Integer> usedEntities = currentProfile.getUsedEntities();
			for(Integer profileEntity : usedEntities){
				int curEntity = profileEntity;
				String entityName = u.getNodeName(curEntity);
				Set<Integer> allParents = entityParentCache.get(curEntity);
				if (allParents == null){
					logger.error("The Entity " + entityName + " seems to have no parents");
				}
				else {
					for(Integer entityParent : allParents){
						if (!result.hasCount(entityParent)){
							entityStatement.setInt(1, entityParent);
							ResultSet eResult = entityStatement.executeQuery();
							if(eResult.next()){
								int count = eResult.getInt(1);
								result.addCount(entityParent, count);
							}
							else {
								throw new RuntimeException("count query failed for entity " + u.getNodeName(entityParent));
							}
						}
					}
				}
			}
		}
		return result;
	}




	/** 
	 * This sets the unionset (all subsumers of all EA phenotypes) in each profile.  This is necessary for simIC and simJ statistics on profiles.
	 */
	void fillUnionSets(Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache){
		for(Integer currentTaxon : taxonProfiles.domainSet()){
			Profile currentTaxonProfile = taxonProfiles.getProfile(currentTaxon);
			Set <PhenotypeExpression> unionSet = new HashSet<PhenotypeExpression>();
			for (PhenotypeExpression ea : currentTaxonProfile.getAllEAPhenotypes()){
				Set<PhenotypeExpression> parents = phenotypeParentCache.get(ea);
				unionSet.addAll(parents);
			}
			currentTaxonProfile.setUnionSet(unionSet);
		}
		for(Integer currentGene : geneProfiles.domainSet()){
			logger.info("Current gene is :" + currentGene);
			Profile currentGeneProfile = geneProfiles.getProfile(currentGene);
			Set <PhenotypeExpression> unionSet = new HashSet<PhenotypeExpression>();
			for (PhenotypeExpression ea : currentGeneProfile.getAllEAPhenotypes()){
				Set<PhenotypeExpression> parents = phenotypeParentCache.get(ea);
				unionSet.addAll(parents);
			}
			currentGeneProfile.setUnionSet(unionSet);
		}
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
	int buildPhenotypeMatchCache(Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache, PhenotypeScoreTable phenotypeScores, CountTable<PhenotypeExpression> eaCounts, Utils u) throws SQLException{
		int attOverlaps = 0;
		for(Integer currentTaxon : taxonProfiles.domainSet()){
			Profile currentTaxonProfile = taxonProfiles.getProfile(currentTaxon);
			for (PhenotypeExpression tPhenotype : currentTaxonProfile.getAllEAPhenotypes()){
				tPhenotype.fillNames(u);
				Set<PhenotypeExpression> tParents = phenotypeParentCache.get(tPhenotype);
				if (tParents == null){
					throw new RuntimeException("parents of " + tPhenotype.getFullName(u) + " from " + u.getNodeName(currentTaxon) + " is null" );
				}
				if (tParents.isEmpty()){
					throw new RuntimeException("parents of " + tPhenotype.getFullName(u) + " is empty" );
				}
				//System.out.println("Taxon Phenotype: " + u.stringForMessage(tPhenotype) + " (" + tPhenotype.hashCode() + ") set: " + tParents.hashCode());
				for(Integer currentGene : geneProfiles.domainSet()){
					Profile currentGeneProfile = geneProfiles.getProfile(currentGene);
					for (PhenotypeExpression gPhenotype : currentGeneProfile.getAllEAPhenotypes()){
						gPhenotype.fillNames(u);
						Set<PhenotypeExpression> gParents = phenotypeParentCache.get(gPhenotype);
						//System.out.println("Gene Phenotype: " + u.stringForMessage(gPhenotype) + " (" + gPhenotype.hashCode() + ") set: " + gParents.hashCode());

						if (!phenotypeScores.hasScore(tPhenotype,gPhenotype) && tPhenotype.getQuality() == gPhenotype.getQuality()){
							//logger.info("T Phenotype is " + tPhenotype.toString());
							//							for(PhenotypeExpression e : tParents){
							//								//System.out.println("Parent is " + e);
							//							}
							//logger.info("G Phenotype is " + gPhenotype.toString());
							for(PhenotypeExpression e : gParents){
								e.fillNames(u);
								//System.out.println("Parent is " + e);
							}
							SimilarityCalculator<PhenotypeExpression> sc = new SimilarityCalculator<PhenotypeExpression>(eaCounts.getSum());
							sc.setTaxonParents(tParents, u);
							sc.setGeneParents(gParents, u);
							double icScore = sc.maxIC(eaCounts,u);
							PhenotypeExpression bestPhenotype = sc.MICS(eaCounts,u);
							if (icScore != eaCounts.getIC(bestPhenotype)){
								throw new RuntimeException("IC value of bestPhenotype " + bestPhenotype + " not equal to icScore = " + icScore);
							}
							double iccsScore = 0;
							double simGOSScore = sc.simGOS(0.5);
							double simNormGOSScore = sc.simNormGOS(0.5);
							//double hypergeoProb = sc.simHyperSS();
							if (Double.isInfinite(eaCounts.getIC(bestPhenotype))){
								System.out.println("**toxic infinite score; tPhenotype = " + tPhenotype.getFullName(u) + "gPhenotype = " + gPhenotype.getFullName(u) + " best: " + bestPhenotype.getFullName(u));
							}
							phenotypeScores.addScore(tPhenotype,gPhenotype,eaCounts.getIC(bestPhenotype),bestPhenotype);
							phenotypeScores.setICCSScore(tPhenotype,gPhenotype,iccsScore);
							phenotypeScores.setGOSScore(tPhenotype, gPhenotype, simGOSScore);
							phenotypeScores.setNormGOSScore(tPhenotype, gPhenotype, simNormGOSScore);
							//phenotypeScores.setHypergeoScore(tPhenotype, gPhenotype,hypergeoProb);
						}
					}
				}
			}
		}
		return attOverlaps;
	}





	/**
	 * 
	 * @param phenotypeScores
	 * @param u
	 * @param w
	 * @throws SQLException
	 */
	void writePhenotypeMatchSummary(PhenotypeScoreTable phenotypeScores,Utils u, Writer w) throws SQLException{
		u.writeOrDump("Taxon\tGene\tTaxon Entity\tGeneEntity\tAttribute\tLowest Common Subsumer\tmaxIC", w);
		for(Integer currentTaxon : taxonProfiles.domainSet()){
			Profile currentTaxonProfile = taxonProfiles.getProfile(currentTaxon);
			for (PhenotypeExpression tPhenotype : currentTaxonProfile.getAllEAPhenotypes()){
				for(Integer currentGene : geneProfiles.domainSet()){
					Profile currentGeneProfile = geneProfiles.getProfile(currentGene);
					for (PhenotypeExpression gPhenotype : currentGeneProfile.getAllEAPhenotypes()){
						if (tPhenotype.getQuality()==gPhenotype.getQuality()){
							if (phenotypeScores.hasScore(tPhenotype, gPhenotype)){
								if (phenotypeScores.getScore(tPhenotype, gPhenotype)>=IC_CUTOFF){
									PhenotypeExpression bestSubsumer = phenotypeScores.getBestSubsumer(tPhenotype,gPhenotype);
									StringBuilder lineBuilder = new StringBuilder(200);
									String bestID = bestSubsumer.getFullName(u);
									lineBuilder.append(u.getNodeName(currentTaxon));
									lineBuilder.append("\t");
									lineBuilder.append(u.getNodeName(currentGene));
									lineBuilder.append("\t");
									lineBuilder.append(u.getNodeName(tPhenotype.getEntity()));
									lineBuilder.append("\t");
									lineBuilder.append(u.getNodeName(gPhenotype.getEntity()));
									lineBuilder.append("\t");
									lineBuilder.append(u.getNodeName(gPhenotype.getQuality()));
									lineBuilder.append("\t");
									lineBuilder.append(bestID);
									lineBuilder.append("\t");
									lineBuilder.append(phenotypeScores.getScore(tPhenotype, gPhenotype));
									u.writeOrDump(lineBuilder.toString(),w);	
								}
							}
							else {
								logger.warn("PhenotypeScores missing entry for " + tPhenotype.getFullName(u) + ", " + gPhenotype.getFullName(u));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * This writes a requested report of the largest IC value reported for a phenotype associated with a taxon or gene.
	 * @param phenotypeScores
	 * @param u
	 * @param w
	 * @throws SQLException
	 */
	void writeTaxonGeneMaxICSummary(PhenotypeScoreTable phenotypeScores,Utils u, Writer w) throws SQLException{
		final Map<Integer,Double> taxonMaxICScores = new HashMap<Integer,Double>();
		final Map<Integer,Double> geneMaxICScores = new HashMap<Integer,Double>();
		for(Integer currentTaxon : taxonProfiles.domainSet()){
			Profile currentTaxonProfile = taxonProfiles.getProfile(currentTaxon);
			for (PhenotypeExpression tPhenotype : currentTaxonProfile.getAllEAPhenotypes()){
				for(Integer currentGene : geneProfiles.domainSet()){
					Profile currentGeneProfile = geneProfiles.getProfile(currentGene);
					for (PhenotypeExpression gPhenotype : currentGeneProfile.getAllEAPhenotypes()){
						if (phenotypeScores.hasScore(tPhenotype,gPhenotype)){
							double score = phenotypeScores.getScore(tPhenotype,gPhenotype);
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


	//More misc Utils
	public boolean phenotypeSubsumes(Integer parent,Integer child){
		if (qualitySubsumers.containsKey(child)){
			return qualitySubsumers.get(child).contains(parent);
		}
		else
			return (parent.equals(child));
			
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
