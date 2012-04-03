package phenoscape.queries.lib;

public class AnnotationPair {

	private final int bearer;
	private final int phenotype;
	private final int hashCode;
	
	private final int HASHBASE = 47;
	private final int HASHMULTIPLIER = 31;

	/**
	 * 
	 * @param b bearer of this annotation; a taxon or a gene
	 * @param p phenotype; an EQ
	 */
	public AnnotationPair(final int b, final int p){
		if (b <= 0)
			throw new IllegalArgumentException("Negative or zero bearer id passed to AnnotationPair constructor");
		if (p <= 0)
			throw new IllegalArgumentException("Negative or zero phenotype id passed to AnnotationPair constructor");
		bearer = b;
		phenotype = p;
		int hc = HASHBASE;
		hc = HASHMULTIPLIER*hc + bearer;
		hc = HASHMULTIPLIER*hc + phenotype;
		hashCode = (int)(hc % Integer.MAX_VALUE);
	}
	
	
	public int getBearer(){
		return bearer;
	}
	
	public int getPhenotype(){
		return phenotype;
	}
	
	@Override
	public boolean equals(Object anObject){
		if (anObject instanceof AnnotationPair){
			AnnotationPair other = (AnnotationPair)anObject;
			if (other.getBearer() == getBearer() &&
					(other.getPhenotype() == getPhenotype())){
				return true;
			}
			else
				return false;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return hashCode;
	}
	
}
