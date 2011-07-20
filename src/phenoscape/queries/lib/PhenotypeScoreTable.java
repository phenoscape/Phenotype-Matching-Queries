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
		return table.get(tPhenotype).get(gPhenotype).getMaxICScore();
	}
	
	public PhenotypeExpression getBestSubsumer(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype){
		return table.get(tPhenotype).get(gPhenotype).getBestSubsumer();
	}

	public double getICCSScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype){
		return table.get(tPhenotype).get(gPhenotype).getICCSScore();
	}

	public void setICCSScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype, double score){
		table.get(tPhenotype).get(gPhenotype).setICCSScore(score);
	}

	public double getSimICScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype){
		return table.get(tPhenotype).get(gPhenotype).getSimICScore();
	}

	public void setSimICScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype, double score){
			table.get(tPhenotype).get(gPhenotype).setSimICScore(score);
	}

	public double getSimJScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype){
		return table.get(tPhenotype).get(gPhenotype).getSimJScore();
	}

	public void setSimJScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype, double score){
		table.get(tPhenotype).get(gPhenotype).setSimJScore(score);
	}

	
	private static class Result {
		double maxICScore;
		double iccsScore;
		double simJScore;
		double simICScore;
		PhenotypeExpression bestIC;

		Result(Double sc, PhenotypeExpression bestPair){
			maxICScore = sc.doubleValue();
			bestIC = bestPair;
		}
		
		double getMaxICScore(){
			return maxICScore;
		}
		
		PhenotypeExpression getBestSubsumer(){
			return bestIC;
		}
	
		void setICCSScore(double score){
			iccsScore = score;
		}
		
		double getICCSScore(){
			return iccsScore;
		}

		
		void setSimICScore(double score){
			simICScore = score;
		}
		
		double getSimICScore(){
			return simICScore;
		}

		
		void setSimJScore(double score){
			simJScore = score;
		}
		
		double getSimJScore(){
			return simJScore;
		}

	}

	
}
