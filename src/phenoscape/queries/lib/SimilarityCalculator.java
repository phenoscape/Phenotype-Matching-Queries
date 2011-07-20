package phenoscape.queries.lib;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import phenoscape.queries.PhenotypeProfileAnalysis;

public class SimilarityCalculator {

	
	private static final String SPATIALPOSTCOMPUIDPREFIX = "BSPO:";

	static Logger logger = Logger.getLogger(SimilarityCalculator.class.getName());

	
	static public double maxIC(Set<PhenotypeExpression> parents1, Set<PhenotypeExpression> parents2, CountTable eaCounts, Utils u) throws SQLException{
		Set<PhenotypeExpression>matchIntersection = new HashSet<PhenotypeExpression>();
		matchIntersection.addAll(parents1 );	// add the EQ parents of the EA level taxon phenotype
		matchIntersection.retainAll(parents2);   // intersect the EQ parents of the gene phenotype, leaving intersection in matchIntersetcion
				
		matchIntersection = filterSpatialPostComps(matchIntersection, u);

		int bestMatch = Integer.MAX_VALUE;  //we're using counts, so minimize
		Set<PhenotypeExpression> bestEQSet = new HashSet<PhenotypeExpression>();
		for(PhenotypeExpression eqM : matchIntersection){
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
			for (PhenotypeExpression shared : matchIntersection){
				shared.fillNames(u);
				u.writeOrDump(shared.getFullName(u),null);
			}
			return -1;
		}
	}
	
	static public PhenotypeExpression MICS(Set<PhenotypeExpression> parents1, Set<PhenotypeExpression> parents2, CountTable eaCounts, Utils u) throws SQLException{
		Set<PhenotypeExpression>matchIntersection = new HashSet<PhenotypeExpression>();
		matchIntersection.addAll(parents1);	// add the EQ parents of the EA level taxon phenotype
		matchIntersection.retainAll(parents2);   // intersect the EQ parents of the gene phenotype, leaving intersection in matchIntersetcion
		

		matchIntersection = filterSpatialPostComps(matchIntersection,u);
		
		int bestMatch = Integer.MAX_VALUE;  //we're using counts, so minimize
		Set<PhenotypeExpression> bestEQSet = new HashSet<PhenotypeExpression>();
		for(PhenotypeExpression eqM : matchIntersection){
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
			for (PhenotypeExpression shared : matchIntersection){
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
	
	static public double iccs(Set<PhenotypeExpression> parents1, Set<PhenotypeExpression> parents2, CountTable eaCounts, Utils u){
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
		if (Double.isInfinite(sum))
			return -1.0;
		else
			return sum/((double)maxByTaxon.size());

	}


	
	
	
	static public double simJ(Set<PhenotypeExpression> parents1, Set<PhenotypeExpression> parents2, CountTable eaCounts, Utils u) throws SQLException{
		Set<PhenotypeExpression>matchIntersection = new HashSet<PhenotypeExpression>();
		matchIntersection.addAll(parents1);	// add the EQ parents of the EA level taxon phenotype
		matchIntersection.retainAll(parents2);   // intersect the EQ parents of the gene phenotype, leaving intersection in matchIntersetcion
		
		Set<PhenotypeExpression>matchUnion = new HashSet<PhenotypeExpression>();  //The union is the set of all parents
		matchUnion.addAll(parents1);
		matchUnion.addAll(parents2);

		for(PhenotypeExpression eqM : matchUnion){
			eqM.fillNames(u);
		}
		
		matchIntersection = filterSpatialPostComps(matchIntersection,u);
		matchUnion = filterSpatialPostComps(matchUnion,u);
		if (matchIntersection.isEmpty()){
			u.writeOrDump("Taxon Parents", null);
			for (PhenotypeExpression taxonP : parents1 ){
				taxonP.fillNames(u);
				u.writeOrDump(taxonP.getFullUID(u),null);
			}
			u.writeOrDump("Gene Parents", null);
			for (PhenotypeExpression geneP : parents2){
				geneP.fillNames(u);
				u.writeOrDump(geneP.getFullUID(u),null);
			}
			throw new RuntimeException("Bad intersection");
		}
		return ((double)matchIntersection.size())/(double)matchUnion.size();

	}
	
	static public double simIC(Set<PhenotypeExpression> parents1, Set<PhenotypeExpression> parents2, CountTable eaCounts, Utils u) throws SQLException{
		Set<PhenotypeExpression>matchIntersection = new HashSet<PhenotypeExpression>();
		matchIntersection.addAll(parents1);	// add the EQ parents of the EA level taxon phenotype
		matchIntersection.retainAll(parents2);   // intersect the EQ parents of the gene phenotype, leaving intersection in matchIntersetcion
		
		Set<PhenotypeExpression>matchUnion = new HashSet<PhenotypeExpression>();  //The union is the set of all parents
		matchUnion.addAll(parents1);
		matchUnion.addAll(parents2);

		for(PhenotypeExpression eqM : matchUnion){
			eqM.fillNames(u);
		}
		
		matchIntersection = filterSpatialPostComps(matchIntersection,u);
		matchUnion = filterSpatialPostComps(matchUnion,u);

		if (matchIntersection.isEmpty()){
			u.writeOrDump("Taxon Parents", null);
			for (PhenotypeExpression taxonP : parents1 ){
				taxonP.fillNames(u);
				u.writeOrDump(taxonP.getFullUID(u),null);
			}
			u.writeOrDump("Gene Parents", null);
			for (PhenotypeExpression geneP : parents2){
				geneP.fillNames(u);
				u.writeOrDump(geneP.getFullUID(u),null);
			}
			throw new RuntimeException("Bad intersection");
		}
		double intersectionSum = 0.0;
		for(PhenotypeExpression e : matchIntersection){
			intersectionSum += eaCounts.getIC(e);
		}
		double unionSum = 0.0;
		for(PhenotypeExpression e : matchUnion){
			if (eaCounts.getRawCount(e) > 0)
				unionSum += eaCounts.getIC(e);
		}
		return intersectionSum/unionSum;

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
}
