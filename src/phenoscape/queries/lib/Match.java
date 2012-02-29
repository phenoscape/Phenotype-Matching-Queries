package phenoscape.queries.lib;


public class Match {
	
	public enum MatchType{
		EXACT, 
		ENTITY_QUALITY_INDEPENDENTLY,
		ENTITY_PARENT, 
		ENTITY_ANCESTOR, 
		QUALITY_PARENT, 
		QUALITY_ANCESTOR,
		EQ_PARENT,
		EQ_ANCESTOR};

	final private MatchType myType;
	final private PhenoRec gRec;
	final private PhenoRec tRec;
	

	public Match(PhenoRec t, PhenoRec g){
		myType = MatchType.EXACT;
		tRec = t;
		gRec = g;
	}

	public Match(MatchType type, PhenoRec t, PhenoRec g){
		myType = type;
		tRec = t;
		gRec = g;
	}

	

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("taxon: ");
		sb.append(tRec.getExhibitorNodeID());
		sb.append("; gene: ");
		sb.append(gRec.getExhibitorNodeID());
		sb.append("; taxon entity: ");
		sb.append(tRec.getEntityUID());
		sb.append("; gene entity: ");
		sb.append(gRec.getEntityUID());
		sb.append("; taxon quality: ");
		sb.append(tRec.getQualityUID());
		sb.append("; gene quality: ");
		sb.append(gRec.getQualityUID());
		sb.append("; type: ");
		sb.append(myType);
		return sb.toString();
	}		
		
	
	public String report(){
		StringBuilder sb = new StringBuilder();
		sb.append(tRec.getExhibitorNodeID());
		sb.append("\t");
		sb.append(gRec.getExhibitorNodeID());
		sb.append("\t");
		sb.append(tRec.getEntityUID());
		sb.append("\t");
		sb.append(gRec.getEntityUID());
		sb.append("\t");
		sb.append(tRec.getQualityUID());
		sb.append("\t");
		sb.append(gRec.getQualityUID());
		sb.append("\t");
		sb.append(myType);
		return sb.toString();
	}
	
	public String reportWithNames(Utils u){
		StringBuilder sb = new StringBuilder();
		sb.append(u.getNodeName(tRec.getExhibitorNodeID()));
		sb.append("(");
		sb.append(u.getNodeUID(tRec.getExhibitorNodeID()));
		sb.append(")\t");
		sb.append(u.getNodeName(gRec.getExhibitorNodeID()));
		sb.append("(");
		sb.append(u.getNodeUID(gRec.getExhibitorNodeID()));
		sb.append(")\t");

//		sb.append(tRec.getUID());
//		sb.append("\t");
//		sb.append(gRec.getUID());
//		sb.append("\t");

		
		
		sb.append(tRec.getEntityUID());
		sb.append("(");
		//sb.append(u.getTermName(tRec.getEntityUID());
		sb.append(")\t");
		sb.append(tRec.getQualityUID());
		sb.append("(");
		//sb.append(u.getTermName(tRec.getQualityUID());
		sb.append(")\t");
		sb.append(gRec.getEntityUID());
		sb.append("(");
		//sb.append(u.getTermName(gRec.getEntityUID());
		sb.append(")\t");
		sb.append(gRec.getQualityUID());
		sb.append("(");
		//sb.append(u.getTermName(gRec.getQualityUID());
		sb.append(")\t");
		sb.append(myType);
		return sb.toString();		
	}
	
}
