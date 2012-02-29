package phenoscape.queries.lib;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math.distribution.HypergeometricDistribution;
import org.apache.commons.math.distribution.HypergeometricDistributionImpl;
import org.apache.log4j.Logger;


public class SimilarityCalculator {

	
	private static final String SPATIALPOSTCOMPUIDPREFIX = "BSPO:";

	static Logger logger = Logger.getLogger(SimilarityCalculator.class.getName());

	private Set<PhenotypeExpression>taxonPhenotypeParents = null;
	private Set<PhenotypeExpression>genePhenotypeParents = null;
	private final Set<PhenotypeExpression>phenotypeMatchIntersection = new HashSet<PhenotypeExpression>();
	private final Set<PhenotypeExpression>phenotypeMatchUnion = new HashSet<PhenotypeExpression>();

	//Entities are still just represented by their database ids
	private Set<Integer>taxonEntityParents = null;
	private Set<Integer>geneEntityParents = null;
	private final Set<Integer>entityMatchIntersection = new HashSet<Integer>();
	private final Set<Integer>entityMatchUnion = new HashSet<Integer>();
	

	final private int annotationCount;
	
	
	
	//constructor is simplified - moving the initialization code to setters
	public SimilarityCalculator(long annotations) throws SQLException{
		if (annotations > Integer.MAX_VALUE){
			throw new IllegalArgumentException("Annotation count too large for hypergeometric distribution: " + annotations);
		}
		annotationCount = (int)annotations;

	}
	
	public void setTaxonPhenotypeParents(Set<PhenotypeExpression> tpp, Utils u) throws SQLException{
		taxonPhenotypeParents = filterSpatialPostComps(tpp,u);
		updatePhenotypeSets(u);
	}
	
	public void setGenePhenotypeParents(Set<PhenotypeExpression> gpp, Utils u) throws SQLException{
		genePhenotypeParents = filterSpatialPostComps(gpp,u);
		updatePhenotypeSets(u);
	}
	
	private void updatePhenotypeSets(Utils u) throws SQLException{
		if (taxonPhenotypeParents != null && genePhenotypeParents != null){
			phenotypeMatchIntersection.addAll(taxonPhenotypeParents);	// add the EQ parents of the EA level taxon phenotype
			phenotypeMatchIntersection.retainAll(genePhenotypeParents);   // intersect the EQ parents of the gene phenotype, leaving intersection in matchIntersetcion
			phenotypeMatchUnion.addAll(taxonPhenotypeParents);
			phenotypeMatchUnion.addAll(genePhenotypeParents);
			for(PhenotypeExpression eqM : phenotypeMatchUnion){
				eqM.fillNames(u);
			}

		}
	}
	
	public void setTaxonEntityParents(Set<Integer> tep, Utils u) throws SQLException{
		taxonEntityParents = filterSpatialPostCompEntities(tep,u);
		updateEntitySets(u);
	}
	
	public void setGeneEntityParents(Set<Integer> gep, Utils u) throws SQLException{
		geneEntityParents = filterSpatialPostCompEntities(gep,u);
		updateEntitySets(u);
	}
	
	private void updateEntitySets(Utils u) throws SQLException{
		if (taxonEntityParents != null && geneEntityParents != null){
			entityMatchIntersection.addAll(taxonEntityParents);	// add the EQ parents of the EA level taxon phenotype
			entityMatchIntersection.retainAll(geneEntityParents);   // intersect the EQ parents of the gene phenotype, leaving intersection in matchIntersetcion
			entityMatchUnion.addAll(taxonEntityParents);
			entityMatchUnion.addAll(geneEntityParents);
			for (Integer e : entityMatchUnion){
				u.cacheOneNode(e);
			}
		}
	}
	
	
	public double maxIC(Set<PhenotypeExpression> parents1, Set<PhenotypeExpression> parents2, CountTable eaCounts, Utils u) throws SQLException{
		int bestMatch = Integer.MAX_VALUE;  //we're using counts, so minimize
		Set<PhenotypeExpression> bestEQSet = new HashSet<PhenotypeExpression>();
		for(PhenotypeExpression eqM : phenotypeMatchIntersection){
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
			return CountTable.calcIC((double)bestMatch/(double)eaCounts.getSum());
		}
		else{
			u.writeOrDump("Intersection", null);
			for (PhenotypeExpression shared : phenotypeMatchIntersection){
				shared.fillNames(u);
				u.writeOrDump(shared.getFullName(u),null);
			}
			return -1;
		}
	}
	
	public PhenotypeExpression MICS(Set<PhenotypeExpression> parents1, Set<PhenotypeExpression> parents2, CountTable eaCounts, Utils u) throws SQLException{
		
		int bestMatch = Integer.MAX_VALUE;  //we're using counts, so minimize
		Set<PhenotypeExpression> bestEQSet = new HashSet<PhenotypeExpression>();
		for(PhenotypeExpression eqM : phenotypeMatchIntersection){
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
			return bestPhenotype;
		}
		else{
			u.writeOrDump("Intersection", null);
			for (PhenotypeExpression shared : phenotypeMatchIntersection){
				shared.fillNames(u);
				u.writeOrDump(shared.getFullName(u),null);
			}
			return null;
		}
	}
	
	/**
	 * 
	 * @param taxonProfile
	 * @param geneProfile
	 * @param phenotypeScores
	 * @return
	 */
	
	public double iccs(Set<PhenotypeExpression> parents1, Set<PhenotypeExpression> parents2, CountTable eaCounts, Utils u){
		List<Double> maxByTaxon = new ArrayList<Double>();
//		for (PhenotypeExpression  tPhenotype : parents1){
//			for (PhenotypeExpression  gPhenotype : parents2){
//				double bestIC = 0.0;
//				for (Integer gEntity : geneProfile.getUsedEntities()){
//					if (phenotypeScores.hasScore(tPhenotype,gPhenotype)) {
//						if (phenotypeScores.getScore(tPhenotype,gPhenotype) > bestIC){
//							bestIC = phenotypeScores.getScore(tPhenotype,gPhenotype);
//						}
//					}
//				}
//				maxByTaxon.add(bestIC);
//			}
//		}
		double sum =0;
		for(Double s : maxByTaxon){
			sum += s.doubleValue();
		}
		if (Double.isInfinite(sum) || true)
			return -1.0;
		else
			return sum/((double)maxByTaxon.size());

	}


	
	
	/**
	 * 
	 * @param u used to load names if errors are to be reported
	 * @return simJ jacquard similarity metric for taxon and gene parents (induced by profiles or individual exhibitors)
	 * @throws SQLException
	 */
	public double simJ(Utils u) throws SQLException{
		if (phenotypeMatchIntersection.isEmpty()){
			logger.warn("No intersection between taxon and gene parents");
			logger.info("Taxon Parents: ");
			for (PhenotypeExpression taxonP : taxonPhenotypeParents){
				taxonP.fillNames(u);
				logger.info(taxonP.getFullUID(u));
			}
			logger.warn("Gene Parents: ");
			for (PhenotypeExpression geneP : genePhenotypeParents){
				geneP.fillNames(u);
				logger.info(geneP.getFullUID(u));
			}
			throw new RuntimeException("Bad intersection");
		}
		return ((double)phenotypeMatchIntersection.size())/(double)phenotypeMatchUnion.size();

	}
	
	/**
	 * 
	 * @param eaCounts
	 * @param u used to load names if errors are to be reported
	 * @return simIC jacquard-like metric that uses sum of IC-values rather than simple counts from taxon and gene parents (induced by profiles or individual exhibitors)
	 * @throws SQLException
	 */
	public double simIC(CountTable eaCounts, Utils u) throws SQLException{
		if (phenotypeMatchIntersection.isEmpty()){
			logger.warn("No intersection between taxon and gene parents");
			logger.info("Taxon Parents: ");
			for (PhenotypeExpression taxonP : taxonPhenotypeParents ){
				taxonP.fillNames(u);
				logger.info(taxonP.getFullUID(u));
			}
			logger.info("Gene Parents: ");
			for (PhenotypeExpression geneP : genePhenotypeParents){
				geneP.fillNames(u);
				logger.info(geneP.getFullUID(u));
			}
			throw new RuntimeException("Bad intersection");
		}
		double intersectionSum = 0.0;
		for(PhenotypeExpression e : phenotypeMatchIntersection){
			intersectionSum += eaCounts.getIC(e);
		}
		double unionSum = 0.0;
		for(PhenotypeExpression e : phenotypeMatchUnion){
			if (eaCounts.getRawCount(e) > 0)
				unionSum += eaCounts.getIC(e);
		}
		return intersectionSum/unionSum;
	}

	/**
	 * simGOS metric suggested by T. Vision
	 * @param xWeight
	 * @return
	 * @throws SQLException
	 */
	public double simGOS(double xWeight) throws SQLException {
		final double cInt = (double)phenotypeMatchIntersection.size();
		final double cUni = (double)phenotypeMatchUnion.size();
		final double cTotal = cInt+cUni;
		final double gos = -xWeight*Math.log((1-(cInt/cUni)) - (1-xWeight)*Math.log(cUni/cTotal));
		return gos;
	}

	
	/**
	 * normalized version of simGOS metric suggested by T. Vision
	 * @param xWeight
	 * @return
	 * @throws SQLException
	 */
	public double simNormGOS(double xWeight) throws SQLException {
		double cInt = (double)phenotypeMatchIntersection.size();
		double cUni = (double)phenotypeMatchUnion.size();
		double cTotal = cInt+cUni;
		double gos = -xWeight*Math.log((1-(cInt/cUni)) - (1-xWeight)*Math.log(cUni/cTotal));
		double gosNorm = gos/(-Math.log(1/cTotal));
		return gosNorm;
	}

	/**
	 * 
	 * @param x 
	 * @return distribution probability that the number of shared parents is exactly the number observed (= size of intersection)
	 */
	public double simHyperSS(){
		int popSize = annotationCount;
		int successes = taxonPhenotypeParents.size();    //taxon parent count
		int sampleSize = genePhenotypeParents.size();   //gene parent count
		HypergeometricDistribution hg = new HypergeometricDistributionImpl(popSize, successes, sampleSize);
		return hg.probability(phenotypeMatchIntersection.size());    //maybe cumulativeProbablility() ?
	}
	
	
	
	// filter out spatial postcompositions
	private static Set<PhenotypeExpression> filterSpatialPostComps(Set<PhenotypeExpression> matchIntersection, Utils u) throws SQLException{
		final Set<PhenotypeExpression> matchesCopy = new HashSet<PhenotypeExpression>();
		matchesCopy.addAll(matchIntersection);
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
						matchIntersection.remove(pe);
					}
				}
				else {
					logger.info("Found null entity: " + pe.getFullUID(u));
				}
			}
		}
		if (matchIntersection.isEmpty()){
			throw new RuntimeException("Bad intersection");
		}
		return matchIntersection;
	}

	// filter out spatial postcompositions
	private static Set<Integer> filterSpatialPostCompEntities(Set<Integer> matchIntersection, Utils u) throws SQLException{
		final Set<Integer> matchesCopy = new HashSet<Integer>();
		matchesCopy.addAll(matchIntersection);
		for(Integer e : matchesCopy){	
			final String eUID = u.getNodeUID(e); 
			//logger.info("Checking " + eUID);
			if (eUID != null){
				if (SPATIALPOSTCOMPUIDPREFIX.equals(eUID.substring(0,5))){
					//logger.info("Supressing " + pe.getFullName(u) + " from intersection");
					matchIntersection.remove(e);
				}
			}
			else {
				logger.info("Entity id had no UID: " + e);
			}
		}
		if (matchIntersection.isEmpty()){
			throw new RuntimeException("Bad intersection");
		}
		return matchIntersection;
	}

	
}
