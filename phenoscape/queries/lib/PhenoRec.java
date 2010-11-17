package phenoscape.queries.lib;

enum ExhibitorType{
	GENE, 
	TAXON
};


public class PhenoRec {
	final private ExhibitorType myType;
	final private int exhibitorNode;
	final private int phenoNode;
	final private String phenoUID;
	final private int entityNode;
	final private String entityUID;
	final private int qualityNode;
	final private String qualityUID;




	private PhenoRec(ExhibitorType mt, int node, int phenoNodeG, String pheno_uid, int phenoEntityNode, String entity_uid, int quality_node_id, String quality_uid){
		myType = mt;
		exhibitorNode = node;
		phenoNode = phenoNodeG;
		phenoUID = pheno_uid;
		entityNode = phenoEntityNode;
		entityUID = entity_uid;
		qualityNode = quality_node_id;
		qualityUID = quality_uid;
	}


	public static PhenoRec makeGenePheno(int node, int phenoNode, String pheno_uid, int phenoEntityNode, String entity_uid, int quality_node_id, String quality_uid){
		return new PhenoRec(ExhibitorType.GENE, node, phenoNode, pheno_uid, phenoEntityNode, entity_uid, quality_node_id, quality_uid);
	}

	public static PhenoRec makeTaxonPheno(int node, int phenoNode, String pheno_uid, int phenoEntityNode, String entity_uid, int quality_node_id, String quality_uid){
		return new PhenoRec(ExhibitorType.TAXON, node, phenoNode, pheno_uid, phenoEntityNode, entity_uid, quality_node_id, quality_uid);
	}


	public boolean matchPhenotypeParentIDs(int phenoNodeID, Utils u) {
		for(Integer parentId : u.getParents(phenoNodeID)){
			if (phenoNode == parentId.intValue())
				return true;
		}
		return false;
	}

	public boolean matchPhenotypeIDs(int pNode){
		return (pNode == phenoNode);
	}

	public boolean exactMatchPhenotype(String phenoUID){
		return phenoUID.equals(phenoUID);
	}

	public boolean matchEntityIDs(int entityID){
		return (entityNode == entityID);
	}

	public boolean matchQualityIDs(int qualityID){
		return (qualityNode == qualityID);
	}

	public boolean matchEntityParentIDs(int entityID, Utils u){
		for(Integer parentId : u.getParents(entityID)){
			if (entityNode == parentId.intValue())
				return true;
		}
		return false;
	}

	public String getUID(){
		return phenoUID;
	}

	public int getExhibitorNodeID(){
		return exhibitorNode;
	}

	public int getPhenoNodeID(){
		return phenoNode;
	}

	public int getPhenoEntityID(){
		return entityNode;
	}

	public int getPhenoQualityID(){
		return qualityNode;
	}

	public String getEntityUID(){
		return entityUID;
	}

	public String getQualityUID(){
		return qualityUID;
	}
}



