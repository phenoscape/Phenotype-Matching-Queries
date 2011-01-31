package phenoscape.queries.lib;

import java.util.HashMap;
import java.util.Map;

public class ProfileScoreTable {

	
	private Map<Integer,Map<Integer,Double>> table = new HashMap<Integer,Map<Integer,Double>>();  //Taxa, Genes, Scores
	
	public void addScore(Integer taxon_id, Integer gene_id, Double score){
		if (table.containsKey(taxon_id)){
			Map<Integer,Double> taxon_entry = table.get(taxon_id);
			taxon_entry.put(gene_id, score);
		}
		else {
			Map<Integer,Double> taxon_entry = new HashMap<Integer,Double>();
			taxon_entry.put(gene_id, score);
			table.put(taxon_id, taxon_entry);
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

	public boolean hasScore(Integer taxon, Integer gene){
		if (table.containsKey(taxon))
			return (table.get(taxon).containsKey(gene));
		else
			return false;
	}
	
	public double getScore(Integer taxon, Integer gene){
		return table.get(taxon).get(gene).doubleValue();
	}

}
