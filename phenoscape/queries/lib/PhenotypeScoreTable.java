package phenoscape.queries.lib;

import java.util.HashMap;
import java.util.Map;

public class PhenotypeScoreTable {

	
	private Map<Integer,Map<Integer,Double>> table = new HashMap<Integer,Map<Integer,Double>>();  //Taxon Phenotype, Gene Phenotype, Score
	
	public void addScore(Integer tphenotype, Integer gphenotype, Double score){
		if (table.containsKey(tphenotype)){
			Map<Integer,Double> taxon_entry = table.get(tphenotype);
			taxon_entry.put(gphenotype, score);
		}
		else {
			Map<Integer,Double> taxon_entry = new HashMap<Integer,Double>();
			taxon_entry.put(gphenotype, score);
			table.put(tphenotype, taxon_entry);
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

	public boolean hasScore(Integer tphenotype, Integer gphenotype){
		if (table.containsKey(tphenotype))
			return (table.get(tphenotype).containsKey(gphenotype));
		else
			return false;
	}
	
	public double getScore(Integer tphenotype, Integer gphenotype){
		return table.get(tphenotype).get(gphenotype).doubleValue();
	}

}
