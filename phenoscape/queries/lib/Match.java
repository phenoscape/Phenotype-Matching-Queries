package phenoscape.queries.lib;

import java.util.Formatter;
import java.util.Locale;

public class Match {
	
	public enum MatchType{
		EXACT, 
		ENTITY_PARENT, 
		ENTITY_ANCESTOR, 
		QUALITY_PARENT, 
		QUALITY_ANCESTOR,
		EQ_ANCESTOR};

	final private MatchType myType;
	final private String taxonID;
	final private String geneID;
	final private String tEntityID;
	final private String tQualityID;
	final private String gEntityID;
	final private String gQualityID;
	

	public Match(String taxon, String gene, String entity, String quality){
		myType = MatchType.EXACT;
		taxonID = taxon;
		geneID = gene;
		tEntityID = entity;
		tQualityID = quality;
		gEntityID = entity;
		gQualityID = quality;
	}

	
	public Match(String taxon, String gene, String tEntity, String tQuality, String gEntity, String gQuality){
		myType = MatchType.EXACT;
		taxonID = taxon;
		geneID = gene;
		tEntityID = tEntity;
		tQualityID = tQuality;
		gEntityID = gEntity;
		gQualityID = gQuality;
	}
	
	public Match(MatchType type, String taxon, String gene, String tEntity, String tQuality, String gEntity, String gQuality){
		myType = type;
		taxonID = taxon;
		geneID = gene;
		tEntityID = tEntity;
		tQualityID = tQuality;
		gEntityID = gEntity;
		gQualityID = gQuality;
		
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb,Locale.US);
		formatter.format("taxon: %s; gene: %s; taxon entity: %s; taxon quality %s; gene entity: %s; gene quality %s; type", taxonID, geneID, tEntityID, tQualityID, gEntityID, gQualityID, myType);
		return formatter.toString();
	}
	
	public String report(){
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb,Locale.US);
		formatter.format("%s\t%s\t%s\t%s\t%s\t%s\t%s",taxonID,geneID,tEntityID,tQualityID,gEntityID,gQualityID,myType);
		return formatter.toString();
	}
	
	public String reportWithNames(Utils u){
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb,Locale.US);
		formatter.format("%s(%s)\t%s(%s)\t%s(%s)\t%s(%s)\t%s(%s)\t%s(%s)\t%s",taxonID,
				                              u.getTermName(taxonID),
				                              geneID,
				                              u.getTermName(geneID),
				                              tEntityID,
				                              u.getTermName(tEntityID),
				                              tQualityID,
				                              u.getTermName(tQualityID),
				                              gEntityID,
				                              u.getTermName(gEntityID),
				                              gQualityID,
				                              u.getTermName(gQualityID),
				                              myType);
		return formatter.toString();		
	}
	
}
