package phenoscape.queries.lib;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DistinctGeneAnnotationRecord {

	
	private int geneID;
	private String gene_uid;
	private String gene_label;
	private int phenotype_id;
	private int entity_id;
	private String entity_uid;
	private int quality_id;
	private String quality_uid;
	private String phenotype_uid;
	private String phenotype_label;
	private String entity_label;
	private String quality_label;
	
	
	private static final String GENEQUERY = 		
		"SELECT gene_node_id, gene_uid, gene_label, dga.phenotype_node_id, p1.entity_node_id, p1.entity_uid, p1.quality_node_id, p1.quality_uid,p1.uid,simple_label(dga.phenotype_node_id), simple_label(p1.entity_node_id),simple_label(p1.quality_node_id) FROM distinct_gene_annotation AS dga " +
		"JOIN phenotype AS p1 ON (p1.node_id = dga.phenotype_node_id)";

	
	public DistinctGeneAnnotationRecord(){
		
	}
	
	public DistinctGeneAnnotationRecord(ResultSet r) throws SQLException{
		geneID = r.getInt(1);
		gene_uid = r.getString(2);
		gene_label = r.getString(3);
		phenotype_id = r.getInt(4);
		entity_id = r.getInt(5);
		entity_uid = r.getString(6);
		quality_id = r.getInt(7);
		quality_uid = r.getString(8);
		phenotype_uid = r.getString(9);
		phenotype_label = r.getString(10);
		entity_label = r.getString(11);
		quality_label = r.getString(12);

	}
	
	public static String getQuery(){
		return GENEQUERY;
	}

	public int getGeneID(){
		return geneID;
	}
	
	public String getGeneUID(){
		return gene_uid;
	}
	
	public String getGeneLabel(){
		return gene_label;
	}
	
	public int getPhenotypeID(){
		return phenotype_id;
	}

	public int getEntityID(){
		return entity_id;
	}
	
	public String getEntityUID(){
		return entity_uid;
	}
	
	public int getQualityID(){
		return quality_id;
	}
	
	public String getQualityUID(){
		return quality_uid;
	}
	
	public String getPhenotypeUID(){
		return phenotype_uid;
	}
	
	public String getPhenotypeLabel(){
		return phenotype_label;
	}
	
	public String getEntityLabel(){
		return entity_label;
	}
	
	public String getQualityLabel(){
		return quality_label;
	}
	
}
