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


	public double getGOSScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype){
		return table.get(tPhenotype).get(gPhenotype).getSimGOSScore();
	}
	
	public void setGOSScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype, double score){
		table.get(tPhenotype).get(gPhenotype).setSimGOSScore(score);
	}
	
	public double getNormGOSScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype){
		return table.get(tPhenotype).get(gPhenotype).getSimNormGOSScore();
	}
	
	public void setNormGOSScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype, double score){
		table.get(tPhenotype).get(gPhenotype).setSimNormGOSScore(score);
	}
	
	public void setHypergeoScore(PhenotypeExpression tPhenotype,PhenotypeExpression gPhenotype, double score) {
		 table.get(tPhenotype).get(gPhenotype).setHypergeoScore(score);
	}
	
	public double getHypergeoScore(PhenotypeExpression tPhenotype, PhenotypeExpression gPhenotype, double score){
		return table.get(tPhenotype).get(gPhenotype).getHypergeoScore();
	}

	
	private static class Result {
		double maxICScore;
		double iccsScore;
		double simGOSScore;
		double simNormGOSScore;
		double hypergeoScore;
		PhenotypeExpression bestIC;

		Result(Double sc, PhenotypeExpression bestPair){
			maxICScore = sc.doubleValue();
			bestIC = bestPair;
		}
		
		public double getHypergeoScore() {
			return hypergeoScore;	
		}

		public void setHypergeoScore(double score) {
			hypergeoScore = score;
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

		

		void setSimGOSScore(double score){
			simGOSScore = score;
		}
		
		double getSimGOSScore(){
			return simGOSScore;
		}

		void setSimNormGOSScore(double score){
			simNormGOSScore = score;
		}
		
		double getSimNormGOSScore(){
			return simNormGOSScore;
		}

	}



	
}
