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


	public boolean matchPhenotypeParentIDs(PhenoRec other, Utils u) {
		for(Integer parentId : u.getParents(other.getPhenoNodeID())){
			if (getPhenoNodeID() == parentId.intValue())
				return true;
			if (u.getNodeUID(getPhenoNodeID()) == null){
			//	System.err.println("Null UID found in   " + getPhenoNodeID());
			}
			else if (u.getNodeUID(parentId.intValue()) == null){
			//	System.err.println("Null UID found in   " + parentId.intValue());
			}
			else if (u.getNodeUID(getPhenoNodeID()).equals(u.getNodeUID(parentId.intValue())))
				return true;
		}
		for(Integer parentId : u.getParents(getPhenoNodeID())){
			if (other.getPhenoNodeID() == parentId.intValue())
				return true;
			if (u.getNodeUID(other.getPhenoNodeID()) == null){
				return false;
			//	System.err.println("Null UID found in   " + getPhenoNodeID());
			}
			else if (u.getNodeUID(parentId.intValue()) == null){
			//	System.err.println("Null UID found in   " + parentId.intValue());
			}			
			else if (u.getNodeUID(other.getPhenoNodeID()).equals(u.getNodeUID(parentId.intValue())))
				return true;	
		}
		return false;
	}

	public boolean matchPhenotypeIDs(PhenoRec other){
		return (other.phenoNode == phenoNode);
	}

	public boolean uidMatchPhenotype(PhenoRec other){
		return getUID().equals(other.getUID());
	}

	public boolean matchEntityIDs(PhenoRec other){
		return (entityNode == other.getPhenoEntityID());
	}

	public boolean matchQualityIDs(PhenoRec other){
		return (qualityNode == other.getPhenoEntityID());
	}

	public boolean matchEntityParentIDs(PhenoRec other, Utils u){
		for(Integer parentId : u.getParents(other.getPhenoEntityID())){
			if (getPhenoEntityID() == parentId.intValue())
				return true;
			if (u.getNodeUID(getPhenoEntityID()) == null){
			//	System.err.println("Null UID found in   " + getPhenoEntityID());
				return false;
			}
			else if (u.getNodeUID(parentId.intValue()) == null){
			//	System.err.println("Null UID found in   " + parentId.intValue());
			}
			else if (u.getNodeUID(getPhenoEntityID()).equals(u.getNodeUID(other.getPhenoEntityID())))
				return true;
		}
		for(Integer parentId : u.getParents(getPhenoEntityID())){
			if (other.getPhenoEntityID() == parentId.intValue())
				return true;
			if (u.getNodeUID(other.getPhenoEntityID()) == null){
				return false;
			//	System.err.println("Null UID found in   " + other.getPhenoEntityID());
			}
			else if (u.getNodeUID(parentId.intValue()) == null){
				//System.err.println("Null UID found in   " + parentId.intValue());
			}
			else if (u.getNodeUID(other.getPhenoEntityID()).equals(u.getNodeUID(parentId.intValue())))
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

	public boolean matchEntityUIDs(PhenoRec other) {
		return getEntityUID().equals(other.getEntityUID());
	}

	public boolean matchQualityUIDs(PhenoRec other) {
		return getQualityUID().equals(other.getQualityUID());
	}


	
}



