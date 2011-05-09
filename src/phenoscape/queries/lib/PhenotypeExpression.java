package phenoscape.queries.lib;

import java.sql.SQLException;

import org.phenoscape.obd.loader.Vocab;

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
		int hc = HASHBASE;
		hc = 31*HASHMULTIPLIER + entity;
		hc = 31*HASHMULTIPLIER + quality;
		hashCode = hc;
		entity2 = VOIDENTITY;
		entity2Name = null;
	}

	private PhenotypeExpression(Utils u) throws SQLException{
		entity = VOIDENTITY;
		quality = u.getQualityNodeID();
		int hc = HASHBASE;
		hc = 31*HASHMULTIPLIER + entity;
		hc = 31*HASHMULTIPLIER + quality;
		hashCode = hc;
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

	public boolean hasSymmetricRelationalQuality(Utils u) throws SQLException{
		String qualityUID = u.getNodeUID(quality);
		if (qualityUID == null) {
			if (!u.checkConnection()){
				u.closeKB();
				u.retryKB();
			}
			u.cacheOneNode(quality);
			qualityUID = u.getNodeUID(quality);
		}
		return Vocab.SYMMETRIC_QUALITIES.contains(qualityUID);
	}

	/**
	 * Returns a new phenotypeExpression that is the inverse of this, assuming one exists (symmetric relational quality and entity2 defined).
	 * Note: this method and the one that follows only support inverses involving the same symmetric relational property, as defined in
	 * the Vocab.SYMMETRIC_QUALITIES list defined in the phenoscapeDataLoader.  This means A attached_to B and B attached_to A are inverses,
	 * but A posterior_to B and B anterior_to A are not recognized as inverses.
	 * @param u passed through to hasSymmetricRelationalQuality
	 * @return new PhenotypeExpression that represents the inverse of this
	 * @throws SQLException
	 */
	public PhenotypeExpression getInverse(Utils u) throws SQLException{
		if (!hasSymmetricRelationalQuality(u))
			return null;
		if (!hasSecondEntity())
			return null;
		return new PhenotypeExpression(getEntity2(),getQuality(),getEntity());
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


	public void fillNames(Utils u){
		if (!isSimpleQuality()){
			entityName = u.getNodeName(entity);
		}
		qualityName = u.getNodeName(quality);
	}

	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder(100);
		if (isSimpleQuality()){
			b.append("Subsuming Quality: ");
			b.append(quality);
		}
		else{
			b.append("Phenotype expression; E: ");
			b.append(entityName);
			b.append(" Q: ");
			b.append(qualityName);
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
