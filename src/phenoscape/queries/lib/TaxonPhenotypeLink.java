package phenoscape.queries.lib;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TaxonPhenotypeLink {


	private static final String TAXONQUERY = 
		"SELECT ata.taxon_node_id,ata.phenotype_node_id,p1.entity_node_id, p1.entity_uid, p1.quality_node_id, p1.quality_uid, p1.uid,simple_label(p1.node_id),simple_label(p1.entity_node_id),simple_label(p1.quality_node_id), p1.related_entity_node_id, p1.related_entity_uid, simple_label(p1.related_entity_node_id)" +
		"      FROM asserted_taxon_annotation AS ata " +
	    "JOIN phenotype AS p1 ON (p1.node_id = ata.phenotype_node_id)" +
	    "WHERE ata.taxon_node_id = ?";
		
	
	int taxonNodeID;
	int phenotypeNodeID;
	int entityNodeID;
	String entityUID; 
	int qualityNodeID;
	String qualityUID;
	String phenotypeUID;
	String phenotypeLabel;
	String entityLabel;
	String qualityLabel;
	int relatedEntityNodeID;
	String relatedEntityUID;
	String relatedEntityLabel;

	public TaxonPhenotypeLink(){
	}

	public TaxonPhenotypeLink(ResultSet r) throws SQLException{
		taxonNodeID = r.getInt(1);
		phenotypeNodeID = r.getInt(2);
		entityNodeID = r.getInt(3);
		entityUID = r.getString(4);
		qualityNodeID = r.getInt(5);
		qualityUID = r.getString(6);
		phenotypeUID = r.getString(7);
		phenotypeLabel = r.getString(8);
		entityLabel = r.getString(9);
		qualityLabel = r.getString(10);
		relatedEntityNodeID = r.getInt(11);
		relatedEntityUID = r.getString(12);
		relatedEntityLabel = r.getString(13);
	}

	public static String getQuery(){
		return TAXONQUERY;
	}



	public int getTaxonNodeID(){
		return taxonNodeID;
	}


	public int getPhenotypeNodeID(){
		return phenotypeNodeID;
	}

	public int getEntityNodeID(){
		return entityNodeID;
	}

	public String getEntityUID(){
		return entityUID; 
	}

	public int getQualityNodeID(){
		return qualityNodeID;
	}

	public String getQualityUID(){
		return qualityUID;
	}

	public String getPhenotypeUID(){
		return phenotypeUID;
	}

	public String getPhenotypeLabel(){
		return phenotypeLabel;
	}

	public String getEntityLabel(){
		return entityLabel;
	}

	public String getQualityLabel(){
		return qualityLabel;
	}
	
	public int getRelatedEntityNodeID(){
		return relatedEntityNodeID;
	}
	
	public String getRelatedEntityUID(){
		return relatedEntityUID;
	}
	
	public String getRelatedEntityLabel(){ 
		return relatedEntityLabel;
	}
	

}