package phenoscape.queries.lib;

import java.util.HashMap;
import java.util.Map;

public class PhenotypeScoreTable {

	
	private Map<Integer,Map<Integer,Map<Integer,Result>>> table = new HashMap<Integer,Map<Integer,Map<Integer,Result>>>();  //Taxon Entity, Gene Entity, Attribute Result
	
	public void addScore(Integer tEntity, Integer gEntity, Integer attribute, Double score, Integer bestEntity){
		if (table.containsKey(tEntity)){
			Map<Integer,Map<Integer,Result>> taxon_entry = table.get(tEntity);
			if (taxon_entry.containsKey(gEntity)){
				Map<Integer,Result> gene_entry = taxon_entry.get(gEntity);
				Result r = new Result(score,bestEntity);
				gene_entry.put(attribute, r);
			}
			else{
				Map<Integer,Result> gene_entry = new HashMap<Integer,Result>();
				Result r = new Result(score,bestEntity);
				gene_entry.put(attribute, r);
				taxon_entry.put(gEntity,gene_entry);
			}
		}
		else {
			Map <Integer,Map<Integer,Result>> taxon_entry = new HashMap<Integer,Map<Integer,Result>>();
			Map<Integer,Result> gene_entry = new HashMap<Integer,Result>();
			Result r = new Result(score,bestEntity);
			gene_entry.put(attribute, r);
			taxon_entry.put(gEntity, gene_entry);
			table.put(tEntity, taxon_entry);
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

	public boolean hasScore(Integer tEntity, Integer gEntity, Integer attribute){
		if (table.containsKey(tEntity))
			if (table.get(tEntity).containsKey(gEntity))
				return (table.get(tEntity).get(gEntity).containsKey(attribute));
			else
				return false;
		else
			return false;
	}
	
	public double getScore(Integer tEntity, Integer gEntity, Integer attribute){
		return table.get(tEntity).get(gEntity).get(attribute).getScore();
	}
	
	public int getBestEntity(Integer tEntity, Integer gEntity, Integer attribute){
		return table.get(tEntity).get(gEntity).get(attribute).getBestEntity();
	}

	
	
	static class Result {
		double score;
		int best;

		Result(Double sc, Integer bestEntity){
			score = sc.doubleValue();
			best = bestEntity.intValue();
		}
		
		double getScore(){
			return score;
		}
		
		int getBestEntity(){
			return best;
		}
	
	}

	
}
