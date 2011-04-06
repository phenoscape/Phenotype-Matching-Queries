package phenoscape.queries.lib;

import java.util.HashMap;
import java.util.Map;

public class PhenotypeScoreTable {

	
	private Map<PhenotypeExpression,Map<PhenotypeExpression,Result>> table = new HashMap<PhenotypeExpression,Map<PhenotypeExpression,Result>>();  //Taxon Entity, Gene Entity, Attribute Result
	
	public void addScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype, Double score, PhenotypeExpression bestSubsumer){
		if (table.containsKey(tPhenotype)){
			Map<PhenotypeExpression,Result>taxon_entry = table.get(tPhenotype);
			Result r = new Result(score,bestSubsumer);
			taxon_entry.put(gPhenotype, r);
		}
		else {
			Map <PhenotypeExpression,Result> taxon_entry = new HashMap<PhenotypeExpression,Result>();
			Result r = new Result(score,bestSubsumer);
			taxon_entry.put(gPhenotype, r);
			table.put(tPhenotype, taxon_entry);
		}
	}

	public boolean isEmpty(){
		return table.isEmpty();
	}
	
	public String summary(){
		StringBuilder b = new StringBuilder(2000);
		//TDB
		return b.toString();
	}

	public boolean hasScore(PhenotypeExpression tPhenotype , PhenotypeExpression gPhenotype){
		if (table.containsKey(tPhenotype))
			return (table.get(tPhenotype).containsKey(gPhenotype));
		else
			return false;
	}
	
	public double getScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype){
		return table.get(tPhenotype).get(gPhenotype).getScore();
	}
	
	public PhenotypeExpression getBestSubsumer(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype){
		return table.get(tPhenotype).get(gPhenotype).getBestSubsumer();
	}

	
	
	static class Result {
		double score;
		PhenotypeExpression best;

		Result(Double sc, PhenotypeExpression bestPair){
			score = sc.doubleValue();
			best = bestPair;
		}
		
		double getScore(){
			return score;
		}
		
		PhenotypeExpression getBestSubsumer(){
			return best;
		}
	
	}

	
}
