package phenoscape.queries.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EntityCountTable {

	final static double LOG2 = Math.log(2.0);
	
	private long sum;
	
	private Map<Integer,Integer>table = new HashMap<Integer,Integer>();  // EQ -> count
	

	public void addCount(Integer entity, int score){
		table.put(entity, score);
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
	

	public  boolean hasCount(Integer entity){
		return table.containsKey(entity);
	}
	
	private int getRawCount(Integer e){
		if (!table.containsKey(e)){
			throw new RuntimeException("Entity " + e + " had no count entry");
		}
		return table.get(e).intValue();
	}

	
	public double getFraction(Integer e){
		return ((double)getRawCount(e))/(double)sum;
	}

	public double getIC(Integer e) {
		return -1*Math.log(getFraction(e))/LOG2;
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
