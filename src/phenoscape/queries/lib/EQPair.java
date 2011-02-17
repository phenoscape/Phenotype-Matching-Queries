package phenoscape.queries.lib;

import java.sql.SQLException;

public class EQPair {
	
	private final int entity;
	private final int quality;
	private final int hashCode;
	private String nameString;
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
		final StringBuilder b = new StringBuilder(100);
		b.append("EQ expression; E: ");
		b.append(entity);
		b.append(" Q: ");
		b.append(quality);
		nameString = b.toString();
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
	
	public void fillNames(Utils u){
		final StringBuilder b = new StringBuilder(100);
		b.append("EQ expression; E: ");
		b.append(u.getNodeName(entity));
		b.append(" Q: ");
		b.append(u.getNodeName(quality));
		nameString = b.toString();
	}
	
	@Override
	public String toString(){
		return nameString;
	}

	public String getFullName(Utils u) throws SQLException{
		final StringBuilder b =  new StringBuilder(200);
		String qualityName = u.getNodeName(quality);
		if (qualityName == null) {
			u.cacheOneNode(quality);
			qualityName = u.getNodeName(quality);
		}
		String entityName = u.getNodeName(entity);
		if (entityName == null) {
			u.cacheOneNode(entity);
			entityName = u.getNodeName(entity);
		}
		b.append(qualityName);
		b.append("^OBO_REL:inheres_in(");
		b.append(entityName);
		b.append(")");
		return b.toString();
	}

}
