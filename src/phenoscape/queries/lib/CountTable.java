package phenoscape.queries.lib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CountTable {

	final static double LOG2 = Math.log(2.0);
	
	private long sum;
	
	private Map<Integer,Map<Integer,Integer>> table = new HashMap<Integer,Map<Integer,Integer>>();  // entity -> (quality -> count)
	
	
	public void addCount(Integer entity_id, Integer quality_id, int score){
		if (table.containsKey(entity_id)){
			Map<Integer,Integer> entity_entry = table.get(entity_id);
			entity_entry.put(quality_id, score);
		}
		else {
			Map<Integer,Integer> entity_entry = new HashMap<Integer,Integer>();
			entity_entry.put(quality_id, score);
			table.put(entity_id, entity_entry);
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
	
	public Set<Integer> getEntities(){
		return table.keySet();
	}
	
	@SuppressWarnings("unchecked")
	public Set<Integer> getQualitiesForEntity(Integer entity_id){
		Map<Integer,Integer> entity_entry = table.get(entity_id);
		if (entity_entry == null){
			return (Set<Integer>)Collections.EMPTY_SET;
		}
		else
			return entity_entry.keySet();
	}

	public boolean hasCount(Integer entity, Integer quality){
		if (table.containsKey(entity))
			return (table.get(entity).containsKey(quality));
		else
			return false;
	}
	
	public int getRawCount(Integer entity, Integer quality){
		return table.get(entity).get(quality).intValue();
	}
	
	public double getFraction(Integer entity, Integer quality){
		return ((double)getRawCount(entity, quality))/(double)sum;
	}
	
	public double getIC(Integer entity, Integer quality){
		return -1*Math.log(getFraction(entity,quality))/LOG2;
	}

	public void setSum(long count){
		sum = count;
	}
	
	public long getSum(){
		return sum;
	}
	
	public static double calcIC(double fraction){
		return -1*Math.log(fraction)/LOG2;
	}
	
	

}
