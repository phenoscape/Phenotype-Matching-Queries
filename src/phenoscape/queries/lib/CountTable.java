package phenoscape.queries.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import phenoscape.queries.TaxonomyTree;

public class CountTable<E> {

	final static double LOG2 = Math.log(2.0);
	
	private long sum;
	
	private final Map<E,Integer>table = new HashMap<E,Integer>();  // EQ -> count
	
	static final Logger logger = Logger.getLogger(TaxonomyTree.class);

	public void addCount(E p, int score){
		table.put(p, score);
	}

	
//	public void addCount(Integer entity_id, Integer quality_id, int score){
//		PhenotypeExpression p = new PhenotypeExpression(entity_id,quality_id);
//		table.put(p, score);
//	}

	public boolean isEmpty(){
		return table.isEmpty();
	}
	
	public String summary(){
		StringBuilder b = new StringBuilder(2000);
		//TDB
		return b.toString();
	}
	
	
	//this needs some checking...
	public Set<E> getEntities(){
		return table.keySet();
	}
	
	public Set<E> getPhenotypes(){
		return table.keySet();
	}
	

//	public  boolean hasCount(Integer entity, Integer quality){
//		PhenotypeExpression p = new PhenotypeExpression(entity,quality);
//		return table.containsKey(p);
//	}

	public boolean hasCount(E p){
		return table.containsKey(p);
	}

	

	public int getRawCount(E p){
		if (!table.containsKey(p)){
			final String message = p.getClass().toString() + ": " + p + " had no count entry";
			logger.fatal(message);
			throw new RuntimeException(message);
		}
		return table.get(p).intValue();
	}

	
	public double getFraction(E p){
		return ((double)getRawCount(p))/(double)sum;
	}

//	private double getFraction(Integer entity, Integer quality){
//		PhenotypeExpression p = new PhenotypeExpression(entity,quality);
//		return ((double)getRawCount(p))/(double)sum;
//	}
	
	public double getIC(E p) {
		return -1*Math.log(getFraction(p))/LOG2;
	}
	
//	public  double getIC(Integer entity, Integer quality){
//		return -1*Math.log(getFraction(entity,quality))/LOG2;
//	}

	public void setSum(long count){
		sum = count;
	}
	
	public long getSum(){
		return sum;
	}
	
	public static double calcIC(double fraction){
		return -1*Math.log(fraction)/LOG2;
	}


	public CountTable<E> addTable(CountTable<E> table2) {
		CountTable<E> result = new CountTable<E>();
		int count=0;
		for (E item : getEntities()){
			if (table2.hasCount(item)){
				result.addCount(item, table2.getRawCount(item) + getRawCount(item));
				count++;
			}
			else{
				result.addCount(item,getRawCount(item));
				count++;
			}
		}
		for (E item : table2.getEntities()){
			if (!result.hasCount(item)){
				result.addCount(item,table2.getRawCount(item));
				count++;
			}
		}
		result.setSum(count);
		return result;
	}


	

}
