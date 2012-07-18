package phenoscape.queries.lib;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import phenoscape.queries.PermutedProfileScore;
import phenoscape.queries.PhenotypeProfileAnalysis;

public class PermutedScoreSet {
	
	static final Logger logger = Logger.getLogger(PhenotypeProfileAnalysis.class);
	
	final static int DISTSIZE = 1000;

	private final List<PermutedProfileScore> scores = new ArrayList<PermutedProfileScore>();
	
	private final ProfileMap taxonProfiles;
	private final ProfileMap geneProfiles;
	private final Map<Integer,Set<Integer>> entityParents;
	private final Map<Integer,Set<Integer>> entityChildren;
	private final PhenotypeScoreTable phenotypeScores;
	private final Utils u;
	private int totalAnnotations;
	
	
	private Random rand;

	public PermutedScoreSet(ProfileMap taxonProf,ProfileMap geneProf,Map<Integer,Set<Integer>> entityParentCache, Map<Integer,Set<Integer>> entityChildCache, PhenotypeScoreTable pScores,Utils util){
		taxonProfiles = taxonProf;
		geneProfiles = geneProf;
		entityParents = entityParentCache;
		entityChildren = entityChildCache;
		phenotypeScores = pScores;
		u = util;
	}
	
	public void setTotalAnnotations(int count){
		totalAnnotations = count;
	}
	
	public void setRandom(Random r){
		rand = r;
	}
	
	public void calcPermutedProfileScores(){
		logger.info("Starting generation of permuted profile scores");
		//List<PermutedProfileScore> result = new ArrayList<PermutedProfileScore>();
		List<PhenotypeExpression> allTaxonPhenotypes = new ArrayList<PhenotypeExpression>();
		HashSet<Integer>taxonProfileSizes = new HashSet<Integer>();
		HashSet<Integer>geneProfileSizes = new HashSet<Integer>();
		for(Integer currentTaxon : taxonProfiles.domainSet()){
			Set<PhenotypeExpression> eaPhenotypes = taxonProfiles.getProfile(currentTaxon).getAllEAPhenotypes();
			allTaxonPhenotypes.addAll(eaPhenotypes);
			taxonProfileSizes.add(eaPhenotypes.size());
		}
		ArrayList<PhenotypeExpression> allGenePhenotypes = new ArrayList<PhenotypeExpression>();
		for(Integer currentGene : geneProfiles.domainSet()){
			Set<PhenotypeExpression> eaPhenotypes = geneProfiles.getProfile(currentGene).getAllEAPhenotypes();
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
				final double[] meanICdist = new double[DISTSIZE];
				final double[] medianICdist = new double[DISTSIZE];
				for (int i = 0; i<DISTSIZE;i++){
					Set<PhenotypeExpression> generatedTaxonProfile = generateProfile(allTaxonPhenotypes,taxonSize);
					Set<PhenotypeExpression> generatedGeneProfile = generateProfile(allGenePhenotypes,geneSize);
					meanICdist[i]=SimilarityCalculator.calcMeanIC(generatedTaxonProfile,generatedGeneProfile,phenotypeScores);
					medianICdist[i]=SimilarityCalculator.calcMedianIC(generatedTaxonProfile, generatedGeneProfile, phenotypeScores);
				}
				PermutedProfileScore score = new PermutedProfileScore(medianICdist,meanICdist,taxonSize,geneSize);
				scores.add(score);
			}
		}
		//logger.info("Finished generation of permuted profile scores");
	}

	
	private Set<PhenotypeExpression> generateProfile(List<PhenotypeExpression> allProfiles, Integer profileSize){
		final int profilesCount = allProfiles.size();
		final Set<PhenotypeExpression>result = new HashSet<PhenotypeExpression>();
		while(result.size()<profileSize){
			int index = rand.nextInt(profilesCount);
			PhenotypeExpression e = allProfiles.get(index);
			result.add(e);
		}
		return result;
	}


	
	List<Integer> buildEntityListFromProfiles(List<PhenotypeExpression> allProfiles, Integer profileSize, Utils u){
		final int profilesCount = allProfiles.size();
		final List<Integer>result = new ArrayList<Integer>();
		while(result.size()<profileSize){
			int index = rand.nextInt(profilesCount);
			final PhenotypeExpression pe = allProfiles.get(index);
			if (!pe.isSimpleQuality()){
				Integer e = pe.getEntity();
				String eUID = u.getNodeUID(e); 
				if (eUID != null){
					if (!SimilarityCalculator.SPATIALPOSTCOMPUIDPREFIX.equals(eUID.substring(0,5))){
						result.add(e);
					}
				}
				if (pe.getEntity2() != PhenotypeExpression.VOIDENTITY){
					Integer e2 = pe.getEntity();
					String e2UID = u.getNodeUID(e2); 
					if (e2UID != null && result.size()<profileSize){
						if (!SimilarityCalculator.SPATIALPOSTCOMPUIDPREFIX.equals(e2UID.substring(0,5))){
							result.add(e);
						}
					}
				}
			}
		}
		if (result.size() == profileSize){
			return result;
		}
		else{
			final String message = "buildEntityListFromProfiles failed; wanted: " + profileSize + "; got: " + result.size();	
			logger.fatal(message);
			throw new RuntimeException(message);
		}

	}

	public void writeDist(String reportFolder) throws IOException{
		final String maxICFolder = reportFolder + "/maxIC";
		final String meanICFolder = reportFolder + "/meanIC";
		for(PermutedProfileScore score : scores){
			score.writeDist(maxICFolder, PermutedProfileScore.ScoreType.MAXIC);
			score.writeDist(meanICFolder, PermutedProfileScore.ScoreType.MEANIC);
		}
	}

	public PermutedProfileScore matchProfileSizes(int taxonSize, int geneSize){
		for(PermutedProfileScore pps : scores){
			if (pps.matchSize(taxonSize,geneSize))
				return pps;
		}
		final String message = "Couldn't find a permuted Profile size for taxon profile size = " + taxonSize + " and gene profile size = " + geneSize;
		logger.fatal(message);
		throw new RuntimeException(message);
	}
	
	
	
}
