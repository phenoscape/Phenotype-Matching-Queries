package phenoscape.queries.lib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CountTable {

	final static double LOG2 = Math.log(2.0);
	
	private long sum;
	
	private Map<PhenotypeExpression,Integer>table = new HashMap<PhenotypeExpression,Integer>();  // entity -> (quality -> count)
	

	public void addCount(PhenotypeExpression p, int score){
		table.put(p, score);
	}

	
	public void addCount(Integer entity_id, Integer quality_id, int score){
		PhenotypeExpression p = new PhenotypeExpression(entity_id,quality_id);
		table.put(p, score);
	}

	public boolean isEmpty(){
		return table.isEmpty();
	}
	
	public String summary(){
		StringBuilder b = new StringBuilder(2000);
		//TDB
		return b.toString();
	}
	
	public Set<PhenotypeExpression> getPhenotypes(){
		return table.keySet();
	}
	

	public boolean hasCount(Integer entity, Integer quality){
		PhenotypeExpression p = new PhenotypeExpression(entity,quality);
		return table.containsKey(p);
	}

	public boolean hasCount(PhenotypeExpression p){
		return table.containsKey(p);
	}

	
	public int getRawCount(Integer entity, Integer quality){
		PhenotypeExpression p = new PhenotypeExpression(entity,quality);
		return table.get(p).intValue();
	}

	public int getRawCount(PhenotypeExpression p){
		return table.get(p).intValue();
	}

	
	public double getFraction(PhenotypeExpression p){
		return ((double)getRawCount(p))/(double)sum;
	}

	public double getFraction(Integer entity, Integer quality){
		PhenotypeExpression p = new PhenotypeExpression(entity,quality);
		return ((double)getRawCount(p))/(double)sum;
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
