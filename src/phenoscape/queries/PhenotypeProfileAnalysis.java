package phenoscape.queries;

/*
 * Copyright (c) 2007-2010 Peter E. Midford

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import phenoscape.queries.lib.CountTable;
import phenoscape.queries.lib.DistinctGeneAnnotationRecord;
import phenoscape.queries.lib.PhenotypeExpression;
import phenoscape.queries.lib.PhenotypeScoreTable;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.ProfileScoreSet;
import phenoscape.queries.lib.TaxonPhenotypeLink;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;



public class PhenotypeProfileAnalysis {

	private static final String TTOROOT = "TTO:0";
	private static final String OSTARIOCLUPEOMORPHAROOT = "TTO:253";
	private static final String ASPIDORASROOT = "TTO:105426";
	private static final String CALLICHTHYIDAEROOT = "TTO:11200";
	private static final String SILURIFORMESROOT = "TTO:1380";
	private static final String TESTROOT = "TTO:0000015";

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


	private static final String SPATIALPOSTCOMPUIDPREFIX = "BSPO:";

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

	/**
	 * 
	 */
	int taxonPhenotypeLinkCount = 0;


	/**
	 * This holds the number of taxa that have phenotype annotations and their parents respectively
	 */
	int annotatedTaxa=0;
	int parentsOfAnnotatedTaxa = 0;
	
	Random rand = new Random();

	static Logger logger = Logger.getLogger(PhenotypeProfileAnalysis.class.getName());

	public PhenotypeProfileAnalysis(Utils u) throws SQLException{
	}


	/**
	 * Entry point for analysis - this opens the KB and the report files, creates an instance of this class and invokes processing with it
	 * @param args currently any command-line args are ignored
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		Utils u = new Utils();
		String kbName;
		try {
			kbName = u.openKBFromConnections(CONNECTION_PROPERTIES_FILENAME);
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
			e.printStackTrace();
		} catch (IOException e) {
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
				System.err.print("An exception occurred while closing a report file or database connection");
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
	 * @param w3 the stream for the phenotype match report
	 * @param w4 the stream for the profile match report
	 * @param w5 the stream for the maxIC report
	 * @throws IOException this might be thrown when writers are closed; not sure why things work better when writers are closed here
	 * @throws SQLException pass this through to next level, which will catch it.
	 */
	void process(Utils u,Writer taxonWriter, Writer geneWriter, Writer w3, Writer w4, Writer w5) throws IOException, SQLException{
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
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = getAllTaxonPhenotypeLinksFromKB(t,u);
		taxonProfiles = loadTaxonProfiles(allLinks,u, attributeMap, qualityNodeID, badTaxonQualities);
		countAnnotatedTaxa(t,t.getRootNodeID(),taxonProfiles,u);
		int eaCount = countEAAnnotations(taxonProfiles,u);
		u.writeOrDump("Count of distinct taxon-phenotype assertions (EQ level): " + taxonPhenotypeLinkCount, taxonWriter);
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
		flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		if (taxonProfiles.isEmpty()){
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

		CountTable phenotypeCountsForGenes = new CountTable();

		logger.info("Building entity parents");
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
		logger.info("Finished entity parents");
		logger.info("Building EQ parents");
		/* Test introduction of phenotypeParentCache, which should map an attribute level EQ to all its parents via inheres_in_part_of entity parents and is_a quality parents (cross product) */
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		buildEQParents(phenotypeParentCache,entityParentCache,u);

		logger.info("Filling count table");
		fillCountTable(geneProfiles, phenotypeCountsForGenes,phenotypeParentCache, u, GENEPHENOTYPECOUNTQUERY, GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());

		CountTable phenotypeCountsToUse = phenotypeCountsForGenes;

		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();


		logger.info("Done building entity parents; building phenotype match cache");
		buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, phenotypeCountsToUse, u);
		taxonWriter.close();
		logger.info("Finished building phenotype match cache; Writing Phenotype match summary");
		writePhenotypeMatchSummary(phenotypeScores,u,w3);		
		w3.close();

		logger.info("Finished Writing Phenotype match summary");

		writeTaxonGeneMaxICSummary(phenotypeScores,u,w5);
		w5.close();
		logger.info("Finished Writing maxIC for taxon/gene summary");		

		logger.info("Calculating Profile Scores");
		List<PermutedProfileScore> pScores = calcPermutedProfileScores(taxonProfiles,geneProfiles,phenotypeScores,u);

		for(PermutedProfileScore score : pScores){
			score.writeDist(RANDOMIZATIONREPORTSFOLDER);
		}
		
		profileMatchReport(phenotypeScores,pScores,w4,u);
		w4.close();
		
		
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

	/**
	 * This writes the report containing profile matches
	 * @param phenotypeScores holds the IC scores for each pair of matchable phenotypes
	 * @param w connected to the file to receive report, or null for console output
	 * @param u just used for writing
	 */
	void profileMatchReport(PhenotypeScoreTable phenotypeScores,List<PermutedProfileScore> pScores, Writer w, Utils u){
		u.writeOrDump("Taxon \t Gene \t taxon phenotypes \t gene phenotypes \t maxIC \t 95% \t 99%  \t decile \t iccs",w);

		long zeroCount = 0;
		//u.writeOrDump("Sizes: Taxon profiles: " + taxonProfiles.keySet().size() + "; Gene profiles: " + geneProfiles.keySet().size(), null);
		for(Integer currentTaxon : taxonProfiles.keySet()){
			for(Integer currentGene : geneProfiles.keySet()){
				ProfileScoreSet thisMatch = matchOneProfilePair(currentTaxon,currentGene,pScores,phenotypeScores);
				if (thisMatch.isNonZero())
					thisMatch.writeScores(u, w);
				else
					zeroCount++;
			}
		}
		u.writeOrDump("Pairs with zero score = " + zeroCount, w);

	}
	
	ProfileScoreSet matchOneProfilePair(Integer taxon, Integer gene,List<PermutedProfileScore> pScores, PhenotypeScoreTable phenotypeScores){
		final Profile taxonProfile = taxonProfiles.get(taxon);
		final Profile geneProfile = geneProfiles.get(gene);
		ProfileScoreSet result = new ProfileScoreSet(taxon, gene,taxonProfile.getAllEAPhenotypes(), geneProfile.getAllEAPhenotypes());
		PermutedProfileScore pScore = matchProfileSizes(taxonProfile.getAllEAPhenotypes().size(),
				                                        geneProfile.getAllEAPhenotypes().size(),
				                                        pScores);
		// calculate maxIC
		double maxIC = calcMaxIC(taxonProfile.getAllEAPhenotypes(), geneProfile.getAllEAPhenotypes(),phenotypeScores);
		result.setMaxICScore(maxIC);
		
		// install critical values
		result.setMaxIC95(pScore.cutoff095);
		result.setMaxIC99(pScore.cutoff099);
		
		// testing p-value distribution
		if (maxIC<pScore.cutoff010)
			result.setDecile(0);
		else if (maxIC<pScore.cutoff020)
			result.setDecile(1);
		else if (maxIC<pScore.cutoff030)
			result.setDecile(2);
		else if (maxIC<pScore.cutoff040)
			result.setDecile(3);
		else if (maxIC<pScore.cutoff050)
			result.setDecile(4);
		else if (maxIC<pScore.cutoff060)
			result.setDecile(5);
		else if (maxIC<pScore.cutoff070)
			result.setDecile(6);
		else if (maxIC<pScore.cutoff080)
			result.setDecile(7);
		else if (maxIC<pScore.cutoff090)
			result.setDecile(8);
		else if (maxIC<pScore.cutoff095)
			result.setDecile(9);
		else if (maxIC<pScore.cutoff099)
			result.setDecile(10);
		else result.setDecile(11);
		

		//calculate ICCS score for this pair of profiles
		double iccs = calcICCS(taxonProfile, geneProfile, phenotypeScores);
		result.setICCSScore(iccs);

		//calculate simIC score for this pair of profiles
		//double simICScore = calcSimIC(currentTaxonProfile, currentGeneProfile, phenotypeScores, phenotypeCountsToUse);
		result.setSimICScore(-1.0);

		//calculate simJ score for this pair of profiles
		//double simJScore = calcSimJ(currentTaxonProfile, currentGeneProfile, phenotypeScores, phenotypeCountsToUse);
		result.setSimJScore(-1.0);

		return result;
	}
	
	PermutedProfileScore matchProfileSizes(int taxonSize, int geneSize,List<PermutedProfileScore> scores){
		for(PermutedProfileScore pps : scores){
			if (pps.matchSize(taxonSize,geneSize))
				return pps;
		}
		throw new RuntimeException("Couldn't find a permuted Profile size for taxon profile size = " + taxonSize + " and gene profile size = " + geneSize);
	}

	
	
	
	final static int DISTSIZE = 1000;
	protected List<PermutedProfileScore> calcPermutedProfileScores(Map<Integer,Profile>taxonProfiles,Map<Integer,Profile>geneProfiles,PhenotypeScoreTable phenotypeScores, Utils u){
		logger.info("Starting generation of permuted profile scores");
		List<PermutedProfileScore> result = new ArrayList<PermutedProfileScore>();
		List<PhenotypeExpression> allTaxonPhenotypes = new ArrayList<PhenotypeExpression>();
		HashSet<Integer>taxonProfileSizes = new HashSet<Integer>();
		HashSet<Integer>geneProfileSizes = new HashSet<Integer>();
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Set<PhenotypeExpression> eaPhenotypes = taxonProfiles.get(currentTaxon).getAllEAPhenotypes();
			allTaxonPhenotypes.addAll(eaPhenotypes);
			taxonProfileSizes.add(eaPhenotypes.size());
		}
		ArrayList<PhenotypeExpression> allGenePhenotypes = new ArrayList<PhenotypeExpression>();
		for(Integer currentGene : geneProfiles.keySet()){
			Set<PhenotypeExpression> eaPhenotypes = geneProfiles.get(currentGene).getAllEAPhenotypes();
			allGenePhenotypes.addAll(eaPhenotypes);
			geneProfileSizes.add(eaPhenotypes.size());
		}
		logger.info("taxon profile sizes: " + u.listIntegerMembers(taxonProfileSizes));
		logger.info("gene profile sizes: " +  u.listIntegerMembers(geneProfileSizes));
		logger.info("Number of phenotypes in appended taxon profile: " + allTaxonPhenotypes.size());
		logger.info("Number of phenotypes in appended gene profile: " + allGenePhenotypes.size());
		for(Integer taxonSize : taxonProfileSizes){
			for (Integer geneSize : geneProfileSizes){
				logger.info("Permuting taxon profile of size: " + taxonSize + " against gene profile of size: " + geneSize);
				final double[] dist = new double[DISTSIZE]; 
				for (int i = 0; i<DISTSIZE;i++){
					Set<PhenotypeExpression> generatedTaxonProfile = generateProfile(allTaxonPhenotypes,taxonSize);
					Set<PhenotypeExpression> generatedGeneProfile = generateProfile(allGenePhenotypes,geneSize);
					dist[i]=calcMaxIC(generatedTaxonProfile,generatedGeneProfile,phenotypeScores);
				}
				PermutedProfileScore p = new PermutedProfileScore(dist,taxonSize,geneSize);
				result.add(p);
			}
		}
		//logger.info("Finished generation of permuted profile scores");
		return result;
	}
	
	
	private void showHistogram(double[] dist){
		double bottom = dist[0];
		double top = dist[DISTSIZE-1];
		System.out.println("Bottom is " + bottom + "; top is " + top);
		double binsize = (top-bottom)/10.0;
		int[] hist = new int[10];
		for(int i = 0;i<DISTSIZE;i++){
			if (dist[i]<binsize+bottom)
				hist[0]++;
			else if (dist[i]<(2*binsize+bottom))
				hist[1]++;
			else if (dist[i]<(3*binsize+bottom))
				hist[2]++;
			else if (dist[i]<(4*binsize+bottom))
				hist[3]++;
			else if (dist[i]<(5*binsize+bottom))
				hist[4]++;
			else if (dist[i]<(6*binsize+bottom))
				hist[5]++;
			else if (dist[i]<(7*binsize+bottom))
				hist[6]++;
			else if (dist[i]<(8*binsize+bottom))
				hist[7]++;
			else if (dist[i]<(9*binsize+bottom))
				hist[8]++;
			else if (dist[i]<=top)
				hist[9]++;
		}
		for (int i = 0; i< 10; i++){
			double baseline = ((double)i)*binsize + bottom;
			System.out.print(baseline + ":");
			for (int j =0;j<hist[i];j++){
				System.out.print('*');
			}
			System.out.println();
		}
	}
	

	Set<PhenotypeExpression> generateProfile(List<PhenotypeExpression> allProfiles, Integer profileSize){
		final int profilesCount = allProfiles.size();
		final Set<PhenotypeExpression>result = new HashSet<PhenotypeExpression>();
		while(result.size()<profileSize){
			int index = rand.nextInt(profilesCount);
			PhenotypeExpression e = allProfiles.get(index);
			result.add(e);
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
	 * 
	 * @param taxonProfile
	 * @param geneProfile
	 * @param phenotypeScores
	 * @return
	 */
	double calcICCS(Profile taxonProfile, Profile geneProfile, PhenotypeScoreTable phenotypeScores){
		List<Double> maxByTaxon = new ArrayList<Double>();
		for (PhenotypeExpression  tPhenotype : taxonProfile.getAllEAPhenotypes()){
			for (PhenotypeExpression  gPhenotype : geneProfile.getAllEAPhenotypes()){
				double bestIC = 0.0;
				for (Integer gEntity : geneProfile.getUsedEntities()){
					if (phenotypeScores.hasScore(tPhenotype,gPhenotype)) {
						if (phenotypeScores.getScore(tPhenotype,gPhenotype) > bestIC){
							bestIC = phenotypeScores.getScore(tPhenotype,gPhenotype);
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

	//	private double calcSimIC(Profile taxonProfile, Profile geneProfile, PhenotypeScoreTable phenotypeScores, CountTable phenotypeCounts){
	//		double simICScore = 0;
	//		double matchSum = 0;
	//		double totalSum = 0;
	//		for(Integer att : attributeSet){
	//			for (Integer tEntity : taxonProfile.getUsedEntities()){
	//				for (Integer gEntity : geneProfile.getUsedEntities()){
	//					if(phenotypeScores.hasScore(tEntity, gEntity, att))
	//						matchSum += phenotypeScores.getScore(tEntity, gEntity, att);
	//				}
	//			}
	//			for (Integer tEntity : taxonProfile.getUsedEntities()){
	//				totalSum += phenotypeCounts.getIC(tEntity, att);
	//			}
	//			for (Integer gEntity : geneProfile.getUsedEntities()){
	//				totalSum += phenotypeCounts.getIC(gEntity,att);
	//			}
	//		}
	//		if (totalSum == 0){
	//			throw new RuntimeException("Unexpected value in simIC: matchSum = " + matchSum + "; totalSum = " + totalSum);
	//		}
	//		simICScore = matchSum/totalSum;
	//		return simICScore;
	//	}
	//
	//	private double calcSimJ(Profile taxonProfile, Profile geneProfile, PhenotypeScoreTable phenotypeScores, CountTable phenotypeCounts){
	//		int matchCount = 0;
	//		int totalCount = 0;
	//		for(Integer att : attributeSet){
	//			for (Integer tEntity : taxonProfile.getUsedEntities()){
	//				for (Integer gEntity : geneProfile.getUsedEntities()){
	//					if(phenotypeScores.hasScore(tEntity, gEntity, att))
	//						matchCount++;
	//				}
	//			}
	//			for (Integer tEntity : taxonProfile.getUsedEntities()){
	//				if (phenotypeCounts.hasCount(tEntity, att))
	//					totalCount++;
	//			}
	//			for (Integer gEntity : taxonProfile.getUsedEntities()){
	//				if (phenotypeCounts.hasCount(gEntity, att))
	//					totalCount++;
	//			}
	//		}
	//		return ((double)matchCount)/((double)totalCount);	
	//	}



	/**
	 * 
	 * @param t 
	 * @param u
	 * @param reportWriter
	 * @throws SQLException
	 */
	HashMap<Integer,Profile> loadTaxonProfiles(Map<Integer, Set<TaxonPhenotypeLink>> allLinks, Utils u, Map<Integer,Integer> attMap,int nodeIDofQuality, Map<Integer,Integer> badQualities) throws SQLException{	
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
			taxonProfileSet.put(taxonID,myProfile);
		}
		return taxonProfileSet;
	}

	int linkedTaxa = 0;

	//	private static final String TAXONQUERY = "SELECT taxon.node_id,taxon.node_id, ata.phenotype_node_id,phenotype.entity_node_id, phenotype.entity_uid, phenotype.quality_node_id,phenotype.quality_uid,phenotype.uid,simple_label(phenotype.node_id),simple_label(phenotype.entity_node_id),simple_label(phenotype.quality_node_id) FROM asserted_taxon_annotation AS ata " +
	//	"JOIN taxon ON (taxon.node_id = ata.taxon_node_id) " +
	//	"JOIN phenotype ON (phenotype.node_id = ata.phenotype_node_id)";		
	//
	//	Map<Integer,Set<TaxonPhenotypeLink>> getAllTaxonPhenotypeLinksFromKB(TaxonomyTree t, Utils u) throws SQLException{
	//		Map<Integer,Set<TaxonPhenotypeLink>> result = new HashMap<Integer,Set<TaxonPhenotypeLink>>();
	//		Set<Integer> taxonSet = t.getAllTaxa();
	//		final Statement s = u.getStatement();
	//		ResultSet ts = s.executeQuery(TAXONQUERY);
	//		while (ts.next()){
	//			if (taxonSet.contains(ts.getInt(1))){
	//				TaxonPhenotypeLink l = new TaxonPhenotypeLink(ts);
	//				taxonPhenotypeLinkCount++;
	//				if (result.containsKey(l.getTaxonNodeID())){
	//					Set<TaxonPhenotypeLink> links =result.get(l.getTaxonNodeID());
	//					links.add(l);
	//				}
	//				else {
	//					Set<TaxonPhenotypeLink> links = new HashSet<TaxonPhenotypeLink>();
	//					links.add(l);
	//					result.put(l.getTaxonNodeID(),links);
	//					linkedTaxa++;
	//				}
	//			}
	//			else {
	//				logger.info("Missed taxon? " + ts.getInt(1));
	//			}
	//		}
	//		return result;
	//	}


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
		if (!result.isEmpty()){
			linkedTaxa++;
		}
		return result;
	}


	/**
	 * 
	 * @param profiles
	 * @param u
	 * @return
	 */
	int countEAAnnotations(Map<Integer,Profile> profiles, Utils u){
		int result = 0;
		System.out.println("Number of profiles = " + profiles.keySet().size());
		for (Integer bearer : profiles.keySet()){
			Profile curProfile = profiles.get(bearer);
			for (Integer ent : curProfile.getUsedEntities()){
				for (Integer att : curProfile.getUsedAttributes()){
					if (curProfile.hasPhenotypeSet(ent, att))
						//logger.info("Taxon: " + u.getNodeUID(bearer) + " Entity: " + u.getNodeName(ent) + " Attribute: " + u.getNodeName(att));
						result++;
				}
			}
		}
		return result;
	}



	/**
	 * @param t holds the taxonomy loaded from the KB
	 * @param taxon current taxon in recursive traversal
	 * @param profiles map from a taxon to its loaded (and assumed unaltered) profile
	 * @param u passed to nodeIsInternal to format an error message
	 * This method simply counts the number of annotated taxa and the number of taxa that are parents of these annotated taxa
	 * Class fields annotatedTaxa and parentsOfAnnotatedTaxa will hold the totals at the end
	 */
	void countAnnotatedTaxa(TaxonomyTree t, Integer taxon, Map<Integer,Profile> profiles, Utils u){
		if (t.nodeIsInternal(taxon,u)){
			final Set<Integer>children = t.getTable().get(taxon);
			boolean hasAnnotatedChild = false;
			for (Integer child : children){
				if (profiles.containsKey(child) && !profiles.get(child).isEmpty()){
					annotatedTaxa++;
					hasAnnotatedChild = true;
				}
				countAnnotatedTaxa(t,child,profiles,u);
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
						if (!childProfile.isEmpty()){
							if (childProfile.hasPhenotypeSet(ent, att)){
								intersectionSet.retainAll(childProfile.getPhenotypeSet(ent,att));
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
					profiles.get(geneID).addPhenotype(annotation.getEntityID(),attribute_id, phenotype_id);	
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
			//String fullName = u.getGeneFullName(geneID);
			u.putNodeUIDName(geneID, annotation.getGeneUID(),annotation.getGeneLabel());
			annotationCount++;
			uniqueGenes.add(annotation.getGeneID());
		}
		u.writeOrDump("Count of genes with annotations " + profiles.keySet().size() + "; Distinct Gene-Phenotype assertions: " + annotationCount +  "; Assertions with phenotype attributes other than Quality " + usableAnnotationCount, reportWriter);
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
						//curEQ.fillNames(u);
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
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			for (PhenotypeExpression tPhenotype : currentTaxonProfile.getAllEAPhenotypes()){
				Set<PhenotypeExpression> tParents = phenotypeParentCache.get(tPhenotype);
				if (tParents == null){
					throw new RuntimeException("parents of " + tPhenotype.getFullName(u) + " from " + u.getNodeName(currentTaxon) + " is null" );
				}
				if (tParents.isEmpty()){
					throw new RuntimeException("parents of " + tPhenotype.getFullName(u) + " is empty" );
				}
				for(Integer currentGene : geneProfiles.keySet()){
					Profile currentGeneProfile = geneProfiles.get(currentGene);
					for (PhenotypeExpression gPhenotype : currentGeneProfile.getAllEAPhenotypes()){
						Set<PhenotypeExpression> gParents = phenotypeParentCache.get(gPhenotype);

						//u.writeOrDump("Checking taxon = " + u.getNodeName(currentTaxon) + " with " + u.getNodeName(curAtt) + " " + currentTaxonProfile.usesAttribute(curAtt) + " " + currentGeneProfile.usesAttribute(curAtt), null);
						//u.writeOrDump("Processing taxon = " + u.getNodeName(currentTaxon) + " with " + u.getNodeName(curAtt), null);
						if (!phenotypeScores.hasScore(tPhenotype,gPhenotype) && tPhenotype.getQuality() == gPhenotype.getQuality()){
							Set<PhenotypeExpression>matches = new HashSet<PhenotypeExpression>();
							matches.addAll(tParents);	// add the EQ parents of the EA level taxon phenotype
							matches.retainAll(gParents);   // intersect the EQ parents of the gene phenotype, leaving intersection in matches

							final Set<PhenotypeExpression> matchesCopy = new HashSet<PhenotypeExpression>();
							matchesCopy.addAll(matches);
							// filter out spatial postcompositions
							for(PhenotypeExpression pe : matchesCopy){	
								if (!pe.isSimpleQuality()){
									if (u.getNodeUID(pe.getEntity()) == null){
										u.cacheOneNode(pe.getEntity());
									
									}
									final String eUID = u.getNodeUID(pe.getEntity()); 
									//logger.info("Checking " + eUID);
									if (eUID != null){
										if (SPATIALPOSTCOMPUIDPREFIX.equals(eUID.substring(0,5))){
											//logger.info("Supressing " + pe.getFullName(u) + " from intersection");
											matches.remove(pe);
										}
									}
									else {
										logger.info("Found null entity: " + pe.getFullUID(u));
									}
								}
							}
							if (matches.isEmpty()){
								u.writeOrDump("Taxon Parents", null);
								for (PhenotypeExpression taxonP : tParents){
									taxonP.fillNames(u);
									u.writeOrDump(taxonP.getFullUID(u),null);
								}
								u.writeOrDump("Gene Parents", null);
								for (PhenotypeExpression geneP : gParents){
									geneP.fillNames(u);
									u.writeOrDump(geneP.getFullUID(u),null);
								}
								throw new RuntimeException("Bad intersection");
							}
							int bestMatch = Integer.MAX_VALUE;  //we're using counts, so minimize
							Set<PhenotypeExpression> bestEQSet = new HashSet<PhenotypeExpression>();
							for(PhenotypeExpression eqM : matches){
								if (eaCounts.hasCount(eqM)){    
									int matchScore = eaCounts.getRawCount(eqM);
									if (matchScore<bestMatch){
										eqM.fillNames(u);
										bestMatch = matchScore;
										bestEQSet.clear();
										bestEQSet.add(eqM);
									}
									else if (matchScore == bestMatch){
										eqM.fillNames(u);
										bestEQSet.add(eqM);
									}
									else if (matchScore < 0)
										throw new RuntimeException("Bad match score value < 0: " + matchScore + " " + u.getNodeName(eqM.getEntity()) + " " + u.getNodeName(eqM.getQuality()));
								}
								else {
									throw new RuntimeException("eq has no score " + eqM.getFullName(u),null);
								}
							}
							if (bestMatch<Double.MAX_VALUE && !bestEQSet.isEmpty()){
								final SortedMap<String,PhenotypeExpression> sortedPhenotypes = new TreeMap<String,PhenotypeExpression>();
								for (PhenotypeExpression eq : bestEQSet){
									String eqName = eq.getFullName(u);
									if (eqName == null){
										eqName = eq.toString();
									}
									sortedPhenotypes.put(eqName,eq);
								}
								final String last = sortedPhenotypes.lastKey();
								final PhenotypeExpression bestPhenotype = sortedPhenotypes.get(last);
								phenotypeScores.addScore(tPhenotype,gPhenotype,eaCounts.getIC(bestPhenotype),bestPhenotype);
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
		u.writeOrDump("Taxon\tGene\tTaxon Entity\tGeneEntity\tAttribute\tLowest Common Subsumer\tScore", w);
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			for (PhenotypeExpression tPhenotype : currentTaxonProfile.getAllEAPhenotypes()){
				for(Integer currentGene : geneProfiles.keySet()){
					Profile currentGeneProfile = geneProfiles.get(currentGene);
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
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			for (PhenotypeExpression tPhenotype : currentTaxonProfile.getAllEAPhenotypes()){
				for(Integer currentGene : geneProfiles.keySet()){
					Profile currentGeneProfile = geneProfiles.get(currentGene);
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
