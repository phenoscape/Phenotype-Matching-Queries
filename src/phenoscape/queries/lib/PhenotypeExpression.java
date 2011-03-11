package phenoscape.queries.lib;

import java.sql.SQLException;

public class PhenotypeExpression {
	
	private final int entity;
	private final int quality;
	private final int hashCode;
	private String nameString;
	private static PhenotypeExpression eqTop = null;
	
	private final int HASHBASE = 47;
	private final int HASHMULTIPLIER = 31;
	
	private final int VOIDENTITY = -1;
	/**
	 * Constructor for phenotypes pairing an entity and a quality
	 * @param e
	 * @param q
	 */
	public PhenotypeExpression(Integer e, Integer q){
		if (e.intValue()<= 0)
			throw new IllegalArgumentException("Negative or zero entity id passed to EQPair constructor");
		if (q.intValue()<= 0)
			throw new IllegalArgumentException("Negative or zero quality id passed to EQPair constructor");
		entity = e.intValue();
		quality = q.intValue();
		int hc = HASHBASE;
		hc = HASHMULTIPLIER*hc + entity;
		hc = HASHMULTIPLIER*hc + quality;
		hashCode = hc;
		final StringBuilder b = new StringBuilder(100);
		b.append("EQ expression; E: ");
		b.append(entity);
		b.append(" Q: ");
		b.append(quality);
		nameString = b.toString();
	}
	
	/**
	 * Constructor for qualities subsuming phenotypes
	 * @param q
	 */
	public PhenotypeExpression(Integer q){
		entity = VOIDENTITY;
		quality = q.intValue();
		int hc = HASHBASE;
		hc = 31*HASHMULTIPLIER + entity;
		hc = 31*HASHMULTIPLIER + quality;
		hashCode = hc;
	}
	
	private PhenotypeExpression(Utils u) throws SQLException{
		entity = VOIDENTITY;
		quality = u.getQualityNodeID();
		int hc = HASHBASE;
		hc = 31*HASHMULTIPLIER + entity;
		hc = 31*HASHMULTIPLIER + quality;
		hashCode = hc;
		nameString = "EQ expression - Quality";
	}
	
	// This represents the root of all phenotypes, which is the quality node
	public static PhenotypeExpression getEQTop(Utils u) throws SQLException{
		if (eqTop == null)
			eqTop = new PhenotypeExpression(u);
		return eqTop;
	}
	
	public int getEntity(){
		return entity;
	}
	
	public int getQuality(){
		return quality;
	}
	
	public boolean isSimpleQuality(){
		return (getEntity() == VOIDENTITY);
	}
	
	@Override 
	public boolean equals(Object anObject){
		if (anObject instanceof PhenotypeExpression){
			PhenotypeExpression other = (PhenotypeExpression)anObject;
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
		if (isSimpleQuality()){
			b.append("Subsuming Quality: ");
			b.append(u.getNodeName(quality));
		}
		else{
			b.append("Phenotype expression; E: ");
			b.append(u.getNodeName(entity));
			b.append(" Q: ");
			b.append(u.getNodeName(quality));
		}
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
			if (!u.checkConnection()){
				u.closeKB();
				u.retryKB();
			}
			u.cacheOneNode(quality);
			qualityName = u.getNodeName(quality);
		}
		if (!isSimpleQuality()){
			String entityName = u.getNodeName(entity);
			if (entityName == null) {
				if (!u.checkConnection()){
					u.closeKB();
					u.retryKB();
				}
				u.cacheOneNode(entity);
				entityName = u.getNodeName(entity);
			}
			b.append(qualityName);
			b.append("^OBO_REL:inheres_in(");
			b.append(entityName);
			b.append(")");
		}
		else{
			b.append(qualityName);
		}
		
		return b.toString();
	}

	public String getFullUID(Utils u) throws SQLException{
		final StringBuilder b =  new StringBuilder(200);
		String qualityUID = u.getNodeUID(quality);
		if (qualityUID == null) {
			if (!u.checkConnection()){
				u.closeKB();
				u.retryKB();
			}
			u.cacheOneNode(quality);
			qualityUID = u.getNodeUID(quality);
		}
		if (!isSimpleQuality()){
			String entityUID = u.getNodeUID(entity);
			if (entityUID == null) {
				if (!u.checkConnection()){
					u.closeKB();
					u.retryKB();
				}
				u.cacheOneNode(entity);
				entityUID = u.getNodeUID(entity);
			}
			b.append(qualityUID);
			b.append("^OBO_REL:inheres_in(");
			b.append(entityUID);
			b.append(")");
		}
		else{
			b.append(qualityUID);
		}
		return b.toString();
	}


	
	
}
