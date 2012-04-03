package phenoscape.queries.lib;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntitySet {


	private final Map<Integer,Set<AnnotationPair>> contents = new HashMap<Integer,Set<AnnotationPair>>(); //entity -> Set<AnnotationPair>
	private final Utils u;


	private static final String ASSERTEDTAXONENTITYQUERY = 
		"select taxon_node_id,phenotype_node_id, p.entity_node_id from asserted_taxon_annotation as ata " +
		"left join phenotype as p on (ata.phenotype_node_id = p.node_id)";

	private static final String DISTINCTGENEENTITYQUERY = 
		"select gene_node_id, phenotype_node_id, entity_node_id from distinct_gene_annotation";

	public EntitySet(Utils ut){
		u = ut;
	}

	public void fillTaxonPhenotypeAnnotationsToEntities() throws SQLException {
		final Statement s = u.getStatement();
		final ResultSet annotations = s.executeQuery(ASSERTEDTAXONENTITYQUERY);
		while(annotations.next()){
			final int taxonid = annotations.getInt(1);
			final int phenotypeid = annotations.getInt(2);
			final int entityid = annotations.getInt(3);
			final AnnotationPair ap = new AnnotationPair(taxonid,phenotypeid);
			if (contents.containsKey(entityid)){
				Set<AnnotationPair> supportingAnnotations = contents.get(entityid);
				supportingAnnotations.add(ap);  // if AnnotationPair.equals works correctly
			}
			else{
				final Set<AnnotationPair> supportingAnnotations = new HashSet<AnnotationPair>();
				supportingAnnotations.add(ap);
				contents.put(entityid, supportingAnnotations);
			}
		}
	}

	public void fillGenePhenotypeAnnotationsToEntities() throws SQLException{
		final Statement s = u.getStatement();
		final ResultSet annotations = s.executeQuery(DISTINCTGENEENTITYQUERY);
		while(annotations.next()){
			final int geneid = annotations.getInt(1);
			final int phenotypeid = annotations.getInt(2);
			final int entityid = annotations.getInt(3);
			final AnnotationPair ap = new AnnotationPair(geneid,phenotypeid);
			if (contents.containsKey(entityid)){
				Set<AnnotationPair> supportingAnnotations = contents.get(entityid);
				supportingAnnotations.add(ap);  // if AnnotationPair.equals works correctly				
			}
			else {
				final Set<AnnotationPair> supportingAnnotations = new HashSet<AnnotationPair>();
				supportingAnnotations.add(ap);
				contents.put(entityid, supportingAnnotations);
			}
		}
	}
	
	public boolean hasEntity(Integer e){
		return contents.containsKey(e);
	}
	
	public int size(){
		return contents.size();
	}
	
	public int annotationCount(int ent){
		return contents.get(ent).size();
	}
	
	public Set<AnnotationPair> getAnnotations(int ent){
		return contents.get(ent);
	}
	
	public int annotationTotal(){
		int sum = 0;
		for(Integer ent : contents.keySet()){
			sum += annotationCount(ent);
		}
		return sum;
	}
	
}
