package phenoscape.queries.lib;

import java.sql.SQLException;


public class PhenotypeExpression {

	private final static String IIPOSTR = "^OBO_REL:inheres_in_part_of(";

	private final int entity;
	private final int quality;
	private final int entity2;
	private final int hashCode;
	private String entityName;
	private String qualityName;
	private String entity2Name;
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
		entityName = e.toString();
		qualityName = q.toString();
		entity2 = VOIDENTITY;
		entity2Name = null;
	}


	/**
	 * Constructor for phenotypes consisting of 2 entities joined by a relation quality
	 * Note that the inverse of a symmetric relation is not equal to the relation, but there are
	 * predicates for handling this case
	 * @param e
	 * @param q
	 * @param e2
	 */
	public PhenotypeExpression(Integer e, Integer q, Integer e2){
		if (e.intValue()<= 0)
			throw new IllegalArgumentException("Negative or zero entity id passed to EQPair constructor");
		if (q.intValue()<= 0)
			throw new IllegalArgumentException("Negative or zero quality id passed to EQPair constructor");
		if (e2.intValue()<= 0)
			throw new IllegalArgumentException("Negative or zero entity2 id passed to EQPair constructor");
		entity = e.intValue();
		quality = q.intValue();
		entity2 = e2.intValue();
		int hc = HASHBASE;
		hc = HASHMULTIPLIER*hc + entity;
		hc = HASHMULTIPLIER*hc + quality;
		hc = HASHMULTIPLIER*hc + entity2;
		hashCode = hc;
		entityName = e.toString();
		qualityName = q.toString();
		entity2Name = e2.toString();
	}

	/**
	 * Constructor for qualities subsuming phenotypes
	 * @param q
	 */
	public PhenotypeExpression(Integer q){
		entity = VOIDENTITY;
		quality = q.intValue();
		int hc = HASHBASE + quality;
		hashCode = (int)(hc % Integer.MAX_VALUE);
		entity2 = VOIDENTITY;
		entity2Name = null;
	}

	private PhenotypeExpression(Utils u) throws SQLException{
		entity = VOIDENTITY;
		quality = u.getQualityNodeID();
		int hc = HASHBASE;
		hc = HASHMULTIPLIER*hc + entity;
		hc = HASHMULTIPLIER*hc + quality;
		hashCode = (int)(hc % Integer.MAX_VALUE);
		qualityName = "Quality";
		entity2 = VOIDENTITY;
		entity2Name = null;
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

	public int getEntity2(){
		return entity2;
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

	public boolean hasSecondEntity(){
		return (getEntity2() != VOIDENTITY);
	}



	/**
	 * Tests if the argument is the symmetric inverse phenotype (e.g. reversed entities, same symmetric relational quality) of this.
	 * @param p2 candidate is symmetric inverse phenotype of this
	 * @return true if p2 is symmetric inverse of this
	 */
	public boolean isSymmetricInverse(PhenotypeExpression p2){
		if (getEntity() == p2.getEntity2() && getQuality() == p2.getQuality() && getEntity2() == p2.getEntity())
			return true;
		return false;
	}


	public void fillNames(Utils u) throws SQLException{
		if (!isSimpleQuality()){
			entityName = u.getNodeName(entity);
			if (entityName == null){
				u.cacheOneNode(entity);
				entityName = u.getNodeName(entity);
			}
		}
		qualityName = u.getNodeName(quality);
		if (qualityName == null){
			u.cacheOneNode(quality);
			qualityName = u.getNodeName(quality);			
		}
	}

	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder(100);
		if (isSimpleQuality()){
			b.append("Subsuming Quality: ");
			b.append(quality);
		}
		else if (getEntity2() == VOIDENTITY){
			b.append("Phenotype expression; E: ");
			b.append(entityName);
			b.append(" Q: ");
			b.append(qualityName);
		}
		else{
			b.append("Phenotype expression; E: ");
			b.append(entityName);
			b.append(" Q: ");
			b.append(qualityName);
			b.append(" E2: ");
			b.append(entity2Name);
		}
		return b.toString();
	}

	public String getFullName(Utils u) throws SQLException{
		final StringBuilder b =  new StringBuilder(200);
		if (qualityName == null || !qualityName.equals(u.getNodeName(quality))) {
			if (!u.checkConnection()){
				u.closeKB();
				u.retryKB();
			}
			u.cacheOneNode(quality);
			qualityName = u.getNodeName(quality);
		}
		if (!isSimpleQuality()){
			if (entityName == null || !entityName.equals(u.getNodeName(entity))) {
				if (!u.checkConnection()){
					u.closeKB();
					u.retryKB();
				}
				u.cacheOneNode(entity);
				entityName = u.getNodeName(entity);
			}
			b.append(qualityName);
			b.append(IIPOSTR);
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
			b.append(IIPOSTR);
			b.append(entityUID);
			b.append(")");
		}
		else{
			b.append(qualityUID);
		}
		return b.toString();
	}

}
