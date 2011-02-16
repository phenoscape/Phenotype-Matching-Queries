package phenoscape.queries.lib;

public class EQPair {
	
	private final int entity;
	private final int quality;
	private final int hashCode;
	
	public EQPair(Integer e, Integer q){
		if (e.intValue()<= 0)
			throw new IllegalArgumentException("Negative or zero entity id passed to EQPair constructor");
		if (q.intValue()<= 0)
			throw new IllegalArgumentException("Negative or zero quality id passed to EQPair constructor");
		entity = e.intValue();
		quality = q.intValue();
		int hc = 47;
		hc = 31*hc + entity;
		hc = 31*hc + quality;
		hashCode = hc;
	}
	
	public int getEntity(){
		return entity;
	}
	
	public int getQuality(){
		return quality;
	}
	
	@Override 
	public boolean equals(Object anObject){
		if (anObject instanceof EQPair){
			EQPair other = (EQPair)anObject;
			if (other.getEntity() == getEntity()  &&
				other.getQuality() == getQuality()){
				return true;
			}
			else
				return false;
		}
		else return false;
	}
	
	@Override
	public int hashCode(){
		return hashCode;
	}

}
