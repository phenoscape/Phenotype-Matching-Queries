package phenoscape.queries.lib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CountTable {

	final static double LOG2 = Math.log(2.0);
	
	private long sum;
	
	private Map<Integer,Map<Integer,Integer>> table = new HashMap<Integer,Map<Integer,Integer>>();  // entity -> (attribute -> count)
	
	
	public void addCount(Integer entity_id, Integer attribute_id, int score){
		if (table.containsKey(entity_id)){
			Map<Integer,Integer> entity_entry = table.get(entity_id);
			entity_entry.put(attribute_id, score);
		}
		else {
			Map<Integer,Integer> entity_entry = new HashMap<Integer,Integer>();
			entity_entry.put(attribute_id, score);
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
	public Set<Integer> getAttributesForEntity(Integer entity_id){
		Map<Integer,Integer> entity_entry = table.get(entity_id);
		if (entity_entry == null){
			return (Set<Integer>)Collections.EMPTY_SET;
		}
		else
			return entity_entry.keySet();
	}

	public boolean hasCount(Integer entity, Integer attribute){
		if (table.containsKey(entity))
			return (table.get(entity).containsKey(attribute));
		else
			return false;
	}
	
	public int getRawCount(Integer entity, Integer attribute){
		return table.get(entity).get(attribute).intValue();
	}
	
	public double getFraction(Integer entity, Integer attribute){
		return ((double)getRawCount(entity, attribute))/(double)sum;
	}
	
	public double getIC(Integer entity, Integer attribute){
		return -1*Math.log(getFraction(entity,attribute))/LOG2;
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
