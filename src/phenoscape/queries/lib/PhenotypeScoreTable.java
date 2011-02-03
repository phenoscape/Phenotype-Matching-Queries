package phenoscape.queries.lib;

import java.util.HashMap;
import java.util.Map;

public class PhenotypeScoreTable {

	
	private Map<Integer,Map<Integer,Map<Integer,Double>>> table = new HashMap<Integer,Map<Integer,Map<Integer,Double>>>();  //Taxon Phenotype, Gene Phenotype, Score
	
	public void addScore(Integer tEntity, Integer gEntity, Integer attribute, Double score){
		if (table.containsKey(tEntity)){
			Map<Integer,Map<Integer,Double>> taxon_entry = table.get(tEntity);
			if (taxon_entry.containsKey(gEntity)){
				Map<Integer,Double> gene_entry = taxon_entry.get(gEntity);
				gene_entry.put(attribute, score);
			}
			else{
				Map<Integer,Double> gene_entry = new HashMap<Integer,Double>();
				gene_entry.put(attribute, score);
				taxon_entry.put(gEntity,gene_entry);
			}
		}
		else {
			Map <Integer,Map<Integer,Double>> taxon_entry = new HashMap<Integer,Map<Integer,Double>>();
			Map<Integer,Double> gene_entry = new HashMap<Integer,Double>();
			gene_entry.put(attribute, score);
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
		return table.get(tEntity).get(gEntity).get(attribute).doubleValue();
	}

}
