package phenoscape.queries.lib;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math.distribution.HypergeometricDistribution;
import org.apache.commons.math.distribution.HypergeometricDistributionImpl;
import org.apache.log4j.Logger;


public class SimilarityCalculator<E> {

	
	public static final String SPATIALPOSTCOMPUIDPREFIX = "BSPO:";

	static Logger logger = Logger.getLogger(SimilarityCalculator.class.getName());

	private Set<E>taxonParents = null;
	private Set<E>geneParents = null;
	private final Set<E>matchIntersection = new HashSet<E>();
	private final Set<E>matchUnion = new HashSet<E>();

	

	final private int annotationCount;
	
	
	
	//constructor is simplified - moving the initialization code to setters
	public SimilarityCalculator(long annotations) throws SQLException{
		if (annotations > Integer.MAX_VALUE){
			throw new IllegalArgumentException("Annotation count too large for hypergeometric distribution: " + annotations);
		}
		annotationCount = (int)annotations;

	}
	
	public void setTaxonParents(Set<E> tpp, Utils u) throws SQLException{
		taxonParents = filterSpatialPostComps(tpp,u);
		updateSets(u);
	}
	
	public void setGeneParents(Set<E> gpp, Utils u) throws SQLException{
		geneParents = filterSpatialPostComps(gpp,u);
		updateSets(u);
	}
	
	private void updateSets(Utils u) throws SQLException{
		if (taxonParents != null && geneParents != null){
			matchIntersection.addAll(taxonParents);	// add the EQ parents of the EA level taxon phenotype
			matchIntersection.retainAll(geneParents);   // intersect the EQ parents of the gene phenotype, leaving intersection in matchIntersetcion
			matchUnion.addAll(taxonParents);
			matchUnion.addAll(geneParents);
			for(E item : matchUnion){
				if (item instanceof PhenotypeExpression)
					((PhenotypeExpression) item).fillNames(u);
			}

		}
	}
	
	
	
	public double maxIC(CountTable<E> eaCounts, Utils u) throws SQLException{
		int bestMatch = Integer.MAX_VALUE;  //we're using counts, so minimize
		Set<E> bestItemSet = new HashSet<E>();
		for(E eqM : matchIntersection){
			if (eaCounts.hasCount(eqM)){    
				int matchScore = eaCounts.getRawCount(eqM);
				if (matchScore<bestMatch){
					u.fillNames(eqM);
					bestMatch = matchScore;
					bestItemSet.clear();
					bestItemSet.add(eqM);
				}
				else if (matchScore == bestMatch){
					u.fillNames(eqM);
					bestItemSet.add(eqM);
				}
				else if (matchScore < 0)
					throw new RuntimeException("Bad match score value < 0: " + matchScore + " " + u.stringForMessage(eqM));
			}
			else {
				throw new RuntimeException("eq has no score " + u.stringForMessage(eqM),null);
			}
		}
		if (bestMatch<Double.MAX_VALUE && !bestItemSet.isEmpty()){
			return CountTable.calcIC((double)bestMatch/(double)eaCounts.getSum());
		}
		else{
			u.writeOrDump("Intersection", null);
			for (E shared : matchIntersection){
				u.fillNames(shared);
				u.writeOrDump(u.stringForMessage(shared),null);
			}
			return -1;
		}
	}
	
	public E MICS(CountTable<E> eaCounts, Utils u) throws SQLException{
		
		int bestMatch = Integer.MAX_VALUE;  //we're using counts, so minimize
		Set<E> bestItemSet = new HashSet<E>();
		for(E eqM : matchIntersection){
			if (eaCounts.hasCount(eqM)){    
				int matchScore = eaCounts.getRawCount(eqM);
				if (matchScore<bestMatch){
					u.fillNames(eqM);
					bestMatch = matchScore;
					bestItemSet.clear();
					bestItemSet.add(eqM);
				}
				else if (matchScore == bestMatch){
					u.fillNames(eqM);
					bestItemSet.add(eqM);
				}
				else if (matchScore < 0)
					throw new RuntimeException("Bad match score value < 0: " + matchScore + " " + u.stringForMessage(eqM));
			}
			else {
				throw new RuntimeException("eq has no score " + u.stringForMessage(eqM),null);
			}
		}
		if (bestMatch<Double.MAX_VALUE && !bestItemSet.isEmpty()){
			final SortedMap<String,E> sortedItems = new TreeMap<String,E>();
			for (E eq : bestItemSet){
				String eqName = u.fullNameString(eq);
				if (eqName == null){
					eqName = eq.toString();
				}
				sortedItems.put(eqName,eq);
			}
			final String last = sortedItems.lastKey();
			final E bestItem = sortedItems.get(last);
			return bestItem;
		}
		else{
			u.writeOrDump("Intersection", null);
			for (E shared : matchIntersection){
				u.fillNames(shared);
				u.writeOrDump(u.fullNameString(shared),null);
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
	
	public double iccs(Set<E> parents1, Set<E> parents2, CountTable<E> eaCounts, Utils u){
//		List<Double> maxByTaxon = new ArrayList<Double>();
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
//		double sum =0;
//		for(Double s : maxByTaxon){
//			sum += s.doubleValue();
//		}
//		if (Double.isInfinite(sum) || true)
			return -1.0;
//		else
//			return sum/((double)maxByTaxon.size());

	}


	
	
	/**
	 * 
	 * @param u used to load names if errors are to be reported
	 * @return simJ jacquard similarity metric for taxon and gene parents (induced by profiles or individual exhibitors)
	 * @throws SQLException
	 */
	public double simJ(Utils u) throws SQLException{
//		if (matchIntersection.isEmpty()){
//			logger.warn("No intersection between taxon and gene parents");
//			logger.info("Taxon Parents: ");
//			for (E taxonP : taxonParents){
//				u.fillNames(taxonP);
//				logger.info(u.stringForMessage(taxonP));
//			}
//			logger.warn("Gene Parents: ");
//			for (E geneP : geneParents){
//				u.fillNames(geneP);
//				logger.info(u.stringForMessage(geneP));
//			}
//		}
		return ((double)matchIntersection.size())/(double)matchUnion.size();

	}
	
	/**
	 * 
	 * @param eaCounts
	 * @param u used to load names if errors are to be reported
	 * @return simIC jacquard-like metric that uses sum of IC-values rather than simple counts from taxon and gene parents (induced by profiles or individual exhibitors)
	 * @throws SQLException
	 */
	public double simIC(CountTable<E> eaCounts, Utils u) throws SQLException{
		if (matchIntersection.isEmpty()){
//			logger.warn("No intersection between taxon and gene parents");
//			logger.info("Taxon Parents: ");
//			for (E taxonP : taxonParents ){
//				u.fillNames(taxonP);
//				logger.info(u.stringForMessage(taxonP));
//			}
//			logger.info("Gene Parents: ");
//			for (E geneP : geneParents){
//				u.fillNames(geneP);
//				logger.info(u.stringForMessage(geneP));
//			}
			return 0;
		}
		double intersectionSum = 0.0;
		for(E e : matchIntersection){
			intersectionSum += eaCounts.getIC(e);
		}
		double unionSum = 0.0;
		for(E e : matchUnion){
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
		final double cInt = (double)matchIntersection.size();
		final double cUni = (double)matchUnion.size();
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
		double cInt = (double)matchIntersection.size();
		double cUni = (double)matchUnion.size();
		double cTotal = cInt+cUni;
		double gos = -xWeight*Math.log((1-(cInt/cUni)) - (1-xWeight)*Math.log(cUni/cTotal));
		double gosNorm = gos/(-Math.log(1/cTotal));
		return gosNorm;
	}

	/**
	 * 
	 * @param geneEntityList 
	 * @param taxonEntityList 
	 * @param x 
	 * @return distribution probability that the number of shared parents is exactly the number observed (= size of intersection)
	 */
	public double simHyperSS(Collection<E> taxonEntities, Collection<E> geneEntities){
		int popSize = annotationCount;
		if (taxonEntities.size() == 0){
			logger.error("simHyperSS received an empty set of taxon entities");
		}
		if (geneEntities.size() == 0){
			logger.error("simHyperSS received an empty set of gene entities");
		}
		final int successes = taxonEntities.size();    //taxon parent count
		final int sampleSize = geneEntities.size();   //gene parent count
		final HypergeometricDistribution hg = new HypergeometricDistributionImpl(popSize, successes, sampleSize);
		final int intersectionSize = collectionIntersectionSize(taxonEntities,geneEntities);
		final double result = hg.probability(intersectionSize);    //maybe cumulativeProbablility() ?
		//System.out.println("population: " + popSize + "; te: " + successes + "; ge: " + sampleSize + "; intersection: " + intersectionSize + "; stat: " + result);
		return result;
	}
	
	/**
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	public int collectionIntersectionSize(Collection<E>c1,Collection<E>c2){
		int result = 0;
		Set<E> overlap = new HashSet<E>();  //will hold a list non-duplicated members
		overlap.addAll(c1);
		overlap.addAll(c2);
		for (E item : overlap){
			final int count1 = countOccurrences(c1,item);
			final int count2 = countOccurrences(c2,item);
			if (count1<= count2)
				result += count1;
			else
				result += count2;
		}
		return result;
	}
	
	
	public int countOccurrences(Collection<E> c1, E item){
		int result = 0;
		for (E entry : c1){
			if (entry.equals(item))
				result++;
		}
		return result;
	}
	
	
	
	// filter out spatial postcompositions
	private Set<E> filterSpatialPostComps(Set<E> matchIntersection, Utils u) throws SQLException{
		final Set<E> matchesCopy = new HashSet<E>();
		matchesCopy.addAll(matchIntersection);
		for(E item : matchesCopy){
			if (item instanceof Integer){
				Integer ent = (Integer)item;
				final String eUID = u.getNodeUID(ent); 
				//logger.info("Checking " + eUID);
				if (eUID != null){
					if (SPATIALPOSTCOMPUIDPREFIX.equals(eUID.substring(0,5))){
						//logger.info("Supressing " + pe.getFullName(u) + " from intersection");
						matchIntersection.remove(ent);
					}
				}
				else {
					logger.info("Entity id had no UID: " + ent);
				}
			}  
			else{ 
				PhenotypeExpression pe = (PhenotypeExpression)item;
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
		}	
		return matchIntersection;
	}


	
}
