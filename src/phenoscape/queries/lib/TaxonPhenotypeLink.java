package phenoscape.queries.lib;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TaxonPhenotypeLink {

//	private static final String TAXONQUERY = "SELECT taxon.node_id,link.node_id, phenotype.node_id,phenotype.entity_node_id, phenotype.entity_uid, phenotype.quality_node_id,phenotype.quality_uid,phenotype.uid,simple_label(phenotype.node_id),simple_label(phenotype.entity_node_id),simple_label(phenotype.quality_node_id) FROM link " +
//	"JOIN taxon ON (taxon.node_id = link.node_id AND taxon.node_id = ? AND link.predicate_id = (select node_id FROM node WHERE uid = 'PHENOSCAPE:exhibits'))" +
//	"JOIN phenotype ON (link.object_id = phenotype.node_id) WHERE is_inferred = false";		

	private static final String TAXONQUERY = "SELECT taxon.node_id,taxon.node_id, ata.phenotype_node_id,phenotype.entity_node_id, phenotype.entity_uid, phenotype.quality_node_id,phenotype.quality_uid,phenotype.uid,simple_label(phenotype.node_id),simple_label(phenotype.entity_node_id),simple_label(phenotype.quality_node_id) FROM asserted_taxon_annotation AS ata " +
	"JOIN taxon ON (taxon.node_id = ata.taxon_node_id AND taxon.node_id = ?) " +
	"JOIN phenotype ON (phenotype.node_id = ata.phenotype_node_id)";		
	
	
	
	int taxonNodeID;
	int linkNodeID;
	int phenotypeNodeID;
	int entityNodeID;
	String entityUID; 
	int qualityNodeID;
	String qualityUID;
	String phenotypeUID;
	String phenotypeLabel;
	String entityLabel;
	String qualityLabel;

	public TaxonPhenotypeLink(){
	}

	public TaxonPhenotypeLink(ResultSet r) throws SQLException{
		taxonNodeID = r.getInt(1);
		linkNodeID = r.getInt(2);
		phenotypeNodeID = r.getInt(3);
		entityNodeID = r.getInt(4);
		entityUID = r.getString(5);
		qualityNodeID = r.getInt(6);
		qualityUID = r.getString(7);
		phenotypeUID = r.getString(8);
		phenotypeLabel = r.getString(9);
		entityLabel = r.getString(10);
		qualityLabel = r.getString(11);
	}

	public static String getQuery(){
		return TAXONQUERY;
	}



	public int getTaxonNodeID(){
		return taxonNodeID;
	}

	public int getLinkNodeID(){
		return linkNodeID;
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
	
	//
	public void setTaxonNodeID(int id){
		taxonNodeID = id;
	}

	public void setLinkNodeID(int id){
		linkNodeID = id;
	}

	public void setPhenotypeNodeID(int id){
		phenotypeNodeID = id;
	}

	public void setEntityNodeID(int id){
		entityNodeID = id;
	}

	public void setEntityUID(String uid){
		entityUID = uid; 
	}

	public void setQualityNodeID(int id){
		qualityNodeID = id;
	}

	public void setQualityUID(String uid){
		qualityUID = uid;
	}

	public void setPhenotypeUID(String uid){
		phenotypeUID = uid;
	}

	public void setPhenotypeLabel(String label){
		phenotypeLabel = label;
	}

	public void setEntityLabel(String label){
		entityLabel = label;
	}

	public void setQualityLabel(String label){
		qualityLabel = label;
	}


}