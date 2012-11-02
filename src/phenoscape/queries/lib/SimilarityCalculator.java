package phenoscape.queries.lib;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
		final E bestSubsumer = MICS(eaCounts,u);
		final double bestCounts = (double)eaCounts.getRawCount(bestSubsumer);
		return CountTable.calcIC(bestCounts/(double)eaCounts.getSum());
	}



	// This returns the most informative common subsumer
	public E MICS(CountTable<E> eaCounts, Utils u) throws SQLException{
		int bestMatch = Integer.MAX_VALUE;  //we're using counts, so minimize
		Set<E> bestItemSet = new HashSet<E>();
		for(E eqM : matchIntersection){
			if (eaCounts.hasCount(eqM)){  
				int matchScore = eaCounts.getRawCount(eqM);
				if (matchScore < 0)
					throw new RuntimeException("Bad match score value < 0: " + matchScore + " " + u.stringForMessage(eqM));
				if (matchScore > 0){
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
				}  // else matchScore == 0; ignore, this results from some annotations to structure being assigned structure minus function as attribute
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
	 * This calculates the mean IC, ignoring intersection pairs with IC=0 
	 * @param taxonProfile
	 * @param geneProfile
	 * @param phenotypeScores
	 * @return
	 * @throws SQLException 
	 */
	public static double calcMeanIC(Set<PhenotypeExpression> taxonPhenotypes, Set<PhenotypeExpression> genePhenotypes, PhenotypeScoreTable phenotypeScores, Utils u) throws SQLException{
		double icSum = 0;
		int icCount = 0;
		for (PhenotypeExpression tPhenotype : taxonPhenotypes){
			for (PhenotypeExpression gPhenotype : genePhenotypes){
				if(phenotypeScores.hasScore(tPhenotype,gPhenotype)){
					final double score = phenotypeScores.getScore(tPhenotype,gPhenotype);
					if (score >= 0 && !Double.isInfinite(score)){
						icSum += phenotypeScores.getScore(tPhenotype,gPhenotype);
						icCount++;   //mean against all possible matches
					}
				}
			}
		}
		if (icCount>0)
			return icSum/(double)icCount;
		else
			return 0;
	}


	final static private Double[] seedArray = new Double[0];
	/**
	 * This calculates the median IC, ignoring intersection pairs with IC=0 
	 * @param taxonProfile
	 * @param geneProfile
	 * @param phenotypeScores
	 * @return median IC score across all phenotype pairs with IC<>0 (zero is returned if there are no such pairs)
	 */
	public static double calcMedianIC(Set<PhenotypeExpression> taxonPhenotypes, Set<PhenotypeExpression> genePhenotypes, PhenotypeScoreTable phenotypeScores){
		final List<Double> scores = new ArrayList<Double>();
		for (PhenotypeExpression tPhenotype : taxonPhenotypes){
			for (PhenotypeExpression gPhenotype : genePhenotypes){
				if(phenotypeScores.hasScore(tPhenotype,gPhenotype))
					if (phenotypeScores.getScore(tPhenotype,gPhenotype) >= 0){
						scores.add(phenotypeScores.getScore(tPhenotype,gPhenotype));
					}
			}
		}
		if (scores.size() > 0){
			final Double[] scoreArray = scores.toArray(seedArray);
			final int midpoint = scoreArray.length/2;
			Double median;
			if (scoreArray.length%2 == 0){
				median = (select(scoreArray,midpoint-1) + select(scoreArray,midpoint))/2.0;
			}
			else {
				median = select(scoreArray,midpoint);
			}
			return median;
		}
		else {
			return 0;
		}
	}

	
	
	//Select implementation is from http://www.brilliantsheep.com/java-implementation-of-hoares-selection-algorithm-quickselect/
	//Naive adaptation of code from Numerical Recipes didn't pass unit testing.
    /** Helper method for select( Double[ ], int, int, int ) */
    public static double select( Double[ ] array, int k ) {
        return select( array, 0, array.length - 1, k );
    }
 
    /** 
     * Returns the value of the kth lowest element. 
     * Do note that for nth lowest element, k = n - 1.
     */
    private static double select( Double[ ] array, int left, int right, int k ) {
 
        while( true ) {
 
            if( right <= left + 1 ) { 
 
                if( right == left + 1 && array[ right ] < array[ left ] ) {
                    swap( array, left, right );
                }
 
                return array[ k ];
 
            } else {
 
                int middle = ( left + right ) >>> 1; 
                swap( array, middle, left + 1 );
 
                if( array[ left ] > array[ right ] ) {
                    swap( array, left, right );
                }
 
                if( array[ left + 1 ] > array[ right ] ) {
                    swap( array, left + 1, right );
                }
 
                if( array[ left ] > array[ left + 1 ] ) {
                    swap( array, left, left + 1 );
                }
 
                int i = left + 1;
                int j = right;
                Double pivot = array[ left + 1 ];
 
                while( true ) { 
                    do i++; while( array[ i ] < pivot ); 
                    do j--; while( array[ j ] > pivot ); 
 
                    if( j < i ) {
                        break; 
                    }
 
                    swap( array, i, j );
                } 
 
                array[ left + 1 ] = array[ j ];
                array[ j ] = pivot;
 
                if( j >= k ) {
                    right = j - 1;
                }
 
                if( j <= k ) {
                    left = i;
                }
            }
        }
    }
 
    /** Helper method for swapping array entries */
    private static void swap( Double[ ] array, int a, int b ) {
        Double temp = array[ a ];
        array[ a ] = array[ b ];
        array[ b ] = temp;
    }
 
	
//	static void elemSwap(Double[] arr,int a, int b){
//		Double t = arr[a];
//		arr[a]=arr[b];
//		arr[b]=t;
//	}
//	
//	static double quickSelect(Double[] scores){
//		final int n = scores.length;
//		int low, high;
//
//		int median;
//		int middle, ll, hh;
//
//		low = 0 ; high = n-1 ; median = (low + high) / 2;
//
//		while(true){
//			if (high <= low) /* One element only */
//				return scores[median] ;
//
//			if (high == low + 1) {  /* Two elements only */
//				if (scores[low] > scores[high])
//					elemSwap(scores,low,high);
//				return scores[median] ;
//			}
//
//			/* Find median of low, middle and high items; swap into position low */
//			middle = (low + high) / 2;
//			if (scores[middle] > scores[high])    elemSwap(scores,middle,high);
//			if (scores[low] > scores[high])       elemSwap(scores,low,high);
//			if (scores[middle] > scores[low])     elemSwap(scores,middle,low);
//
//			/* Swap low item (now in position middle) into position (low+1) */
//			elemSwap(scores,middle,low);
//
//			/* Nibble from each end towards middle, swapping items when stuck */
//			ll = low + 1;
//			hh = high;
//			while(true) {
//				do ll++; while (scores[low] > scores[ll]) ;
//				do hh--; while (scores[hh]  > scores[low]) ;
//
//				if (hh < ll)
//					break;
//
//				elemSwap(scores,ll,hh) ;
//			}
//
//			/* Swap middle item (in position low) back into correct position */
//			elemSwap(scores,low,hh) ;
//
//			/* Re-set active partition */
//			if (hh <= median)
//				low = ll;
//			if (hh >= median)
//				high = hh - 1;
//		}
//	}





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
public double simHyperSS(int taxonCount,int geneCount, int entIntersectionScore){
	int popSize = annotationCount;
	if (taxonCount == 0){
		throw new RuntimeException("simHyperSS received an empty set of taxon entities");
	}
	if (geneCount == 0){
		logger.error("simHyperSS received an empty set of gene entities");
	}
	final int successes = taxonCount;    //taxon parent count
	final int sampleSize = geneCount;   //gene parent count
	final HypergeometricDistribution hg = new HypergeometricDistributionImpl(popSize, successes, sampleSize);
	final double result = hg.probability(entIntersectionScore);    //maybe cumulativeProbablility() ?
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
					matchIntersection.remove(ent);
				}
			}
			else {
				logger.warn("Entity id had no UID: " + ent);
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


static class MICS<E>{
	final E subsumer;
	final double ic;

	MICS(E sub, double maxIC){
		subsumer = sub;
		ic = maxIC;
	}

	E getSubsumer(){
		return subsumer;
	}

	double getMaxIC(){
		return ic;
	}

}


}
