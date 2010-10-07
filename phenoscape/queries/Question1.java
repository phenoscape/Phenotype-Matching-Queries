package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import phenoscape.queries.lib.Match;
import phenoscape.queries.lib.Utils;

import com.google.gson.JsonObject;

public class Question1 {

	final static int CHUNKSIZE = 5000;
	final static String TAXONSTR = "taxon";
	final static String IDSTR = "id";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Question1 reporter = new Question1();
		reporter.writeReport("TaxonGeneEQMatches.txt",new Utils());
	}

	void writeReport(String fileName, Utils u){

		int taxonCount = u.getTaxonAnnotationCount();
		double loopTime = 0;
		long entityMatch = 0;
		long qualityMatch = 0;
		long eqMatch = 0;
		long qualityParentMatch = 0;
		long entityParentMatch = 0;
		long qualityAncestorMatch = 0;
		long entityAncestorMatch = 0;
		long eqAncestorMatch = 0;
		final Collection<JsonObject> geneAnnotations = u.getGeneAnnotations();
    	if (geneAnnotations.isEmpty()){
    		System.out.println("No genes matched");
    		return;
    	}
		System.out.println("Read " + geneAnnotations.size() + " gene annotations");
		final String[] geneEntities = new String[geneAnnotations.size()];
		final String[] geneQualities = new String[geneAnnotations.size()];
		final String[] geneNames = new String[geneAnnotations.size()];
		int geneCount = 0;
		for(JsonObject g : geneAnnotations){
			geneEntities[geneCount] = g.get("entity").getAsJsonObject().get(IDSTR).getAsString();
			geneQualities[geneCount] = g.get("quality").getAsJsonObject().get(IDSTR).getAsString();
			geneNames[geneCount] = g.get("gene").getAsJsonObject().get(IDSTR).getAsString();
			geneCount++;
		}
		u.buildAncestors();
		File outFile = new File(fileName);
        BufferedWriter bw;
        try {
            //bw = new BufferedWriter(new FileWriter(outFile));
        	bw = null;
            writeOrDump("Taxon\tGene\tTaxon Entity\tTaxon Quality\tGene Entity\tGene Quality\tMatch Type",bw);

            for(int i= 0;i<taxonCount;i+=CHUNKSIZE){
            	final Collection<JsonObject> taxonAnnotations = u.getTaxonAnnotations(CHUNKSIZE,i);
            	if (taxonAnnotations.isEmpty()){
            		System.out.println("No taxa matched");
            		return;			
            	}
            	else{
            		System.out.println("Read " + taxonAnnotations.size() + " taxon annotations");
            		u.buildParents();
            		u.buildAncestors();
            		long startTime = System.nanoTime();
            		for(JsonObject t : taxonAnnotations){
            			String tEntity = t.get("entity").getAsJsonObject().get(IDSTR).getAsString();
            			String tQuality = t.get("quality").getAsJsonObject().get(IDSTR).getAsString();
            			boolean tEntityPostComp = (tEntity.indexOf('^') != -1);
            			for (int j = 0;j<geneEntities.length;j++){
            				String gEntity = geneEntities[j];
            				String gQuality = geneQualities[j];
            				if (tEntity.equals(gEntity)){
            					if (tQuality.equals(gQuality)){
            						eqMatch++;
            						String tName = t.get(TAXONSTR).getAsJsonObject().get(IDSTR).getAsString();
            						String gName = geneNames[j];
            						Match myMatch = new Match(tName,gName,gEntity,gQuality);
            						
            						writeOrDump(myMatch.reportWithNames(u),bw);
            					}
            					else if (u.matchAncestor(tQuality,gQuality) || u.matchAncestor(gQuality, tQuality)){
            						String tName = t.get(TAXONSTR).getAsJsonObject().get(IDSTR).getAsString();
            						String gName = geneNames[j];
            						Match myMatch;
            						if (u.matchParent(tQuality,gQuality) || u.matchParent(gQuality, tQuality)){
                						qualityParentMatch++;
                						myMatch = new Match(Match.MatchType.QUALITY_PARENT,tName,gName,tEntity, tQuality, gEntity,gQuality);
            						}
            						else{
            							qualityAncestorMatch++;
            							myMatch = new Match(Match.MatchType.QUALITY_ANCESTOR,tName,gName,tEntity, tQuality, gEntity,gQuality);
            						}
            						writeOrDump(myMatch.reportWithNames(u),bw);
            					}
            					else{
            						entityMatch++;
            					}
            				}
            				else if (tQuality.equals(gQuality)){
                    			boolean gEntityPostComp = (gEntity.indexOf('^') != -1);            				
            					if (u.matchAncestor(tEntity,gEntity) || u.matchAncestor(gEntity, tEntity)){
            						String tName = t.get(TAXONSTR).getAsJsonObject().get(IDSTR).getAsString();
            						String gName = geneNames[j];
            						Match myMatch;
                					if (u.matchParent(tEntity,gEntity) || u.matchParent(gEntity, tEntity)){
                						entityParentMatch++;
                						myMatch = new Match(Match.MatchType.ENTITY_PARENT,tName,gName,tEntity,tQuality,gEntity,gQuality);
                					}
                					else {
                						entityAncestorMatch++;
                						myMatch = new Match(Match.MatchType.ENTITY_ANCESTOR,tName,gName,tEntity,tQuality,gEntity,gQuality);
                					}
            						writeOrDump(myMatch.reportWithNames(u),bw);
            					}
            					else if (tEntityPostComp && gEntityPostComp && (u.matchAncestor(tQuality,gQuality) || u.matchAncestor(gQuality, tQuality))){
            						String[] tEntityComps = Utils.CIRCUMFLEXPATTERN.split(tEntity);
            						String tGenusEntity = tEntityComps[0];
            						String tDifferentiaFinal = tEntityComps[tEntityComps.length-1];   //tEntityComps seems to parse out each component of the postcomp...
            						tDifferentiaFinal = tDifferentiaFinal.substring(tDifferentiaFinal.indexOf('(')+1,tDifferentiaFinal.indexOf(')'));
            						//String[] tDifferentiaComps = PARENPATTERN.split(tDifferentiaFinal);
            						String[] gEntityComps = Utils.CIRCUMFLEXPATTERN.split(gEntity);
            						String gGenusEntity = gEntityComps[0];
            						String gDifferentiaFinal = gEntityComps[gEntityComps.length-1]; 
            						gDifferentiaFinal = gDifferentiaFinal.substring(gDifferentiaFinal.indexOf('(')+1,gDifferentiaFinal.indexOf(')'));
            						String tName = t.get(TAXONSTR).getAsJsonObject().get(IDSTR).getAsString();
            						String gName = geneNames[j];
//            						System.out.println("taxon: " + t);
//            						System.out.println("geneEntity: " + gEntity);
//            						System.out.println("Taxon Genus and Ancestors: " + u.getTermName(tGenusEntity) + " " + u.getAncestors(tGenusEntity));
//            						System.out.println("Gene Genus and Ancestors: " + u.getTermName(gGenusEntity) + " " + u.getAncestors(gGenusEntity));
//            						System.out.println("Taxon Differentia Final and Ancestors: " + u.getTermName(tGenusEntity) + " " + u.getAncestors(tGenusEntity));
//            						System.out.println("Gene Differentia Final and Ancestors: " + u.getTermName(gGenusEntity) + " " + u.getAncestors(gGenusEntity));
//            						System.out.print("Pair of postComps: " + tGenusEntity + "; " + gGenusEntity + " | " + tDifferentiaFinal + "; " + gDifferentiaFinal);
            						if ((u.matchAncestor(tGenusEntity,gGenusEntity) || u.matchAncestor(gGenusEntity,tGenusEntity)) && 
            							(u.matchAncestor(tDifferentiaFinal, gDifferentiaFinal) || u.matchAncestor(gDifferentiaFinal, tDifferentiaFinal))){
            							Match myMatch = new Match(Match.MatchType.ENTITY_ANCESTOR,tName,gName,tEntity,tQuality,gEntity,gQuality);
                						writeOrDump(myMatch.reportWithNames(u),bw);
//                						System.out.println(" - matched");
            						}
            						else {
            							qualityMatch++;
//                						System.out.println("");
            						}
            					}
            					else if (tEntityPostComp && gEntityPostComp){
            						String[] tEntityComps = Utils.CIRCUMFLEXPATTERN.split(tEntity);
            						String tGenusEntity = tEntityComps[0];
            						String tDifferentiaFinal = tEntityComps[tEntityComps.length-1];   //tEntityComps seems to parse out each component of the postcomp...
            						tDifferentiaFinal = tDifferentiaFinal.substring(tDifferentiaFinal.indexOf('(')+1,tDifferentiaFinal.indexOf(')'));
            						//String[] tDifferentiaComps = PARENPATTERN.split(tDifferentiaFinal);
            						String[] gEntityComps = Utils.CIRCUMFLEXPATTERN.split(gEntity);
            						String gGenusEntity = gEntityComps[0];
            						String gDifferentiaFinal = gEntityComps[gEntityComps.length-1]; 
            						gDifferentiaFinal = gDifferentiaFinal.substring(gDifferentiaFinal.indexOf('(')+1,gDifferentiaFinal.indexOf(')'));
            						String tName = t.get(TAXONSTR).getAsJsonObject().get(IDSTR).getAsString();
            						String gName = geneNames[j];
            						//System.out.print("Pair of postComps: " + tGenusEntity + "; " + gGenusEntity + " | " + tDifferentiaFinal + "; " + gDifferentiaFinal);
            						if ((u.matchAncestor(tGenusEntity,gGenusEntity) || u.matchAncestor(gGenusEntity,tGenusEntity)) && 
            							(u.matchAncestor(tDifferentiaFinal, gDifferentiaFinal) || u.matchAncestor(gDifferentiaFinal, tDifferentiaFinal))){
            							Match myMatch = new Match(Match.MatchType.EQ_ANCESTOR,tName,gName,tEntity,tQuality,gEntity,gQuality);
                						writeOrDump(myMatch.reportWithNames(u),bw);
                						//System.out.println(" - matched");
            						}
            						else {
            							qualityMatch++;
                						//System.out.println("");
            						}
            					}
            					else {
            						qualityMatch++;
            					}
            				}
            				else if ((u.matchAncestor(tEntity,gEntity) || u.matchAncestor(gEntity, tEntity)) && 
            						 (u.matchAncestor(tQuality,gQuality) || u.matchAncestor(gQuality, tQuality))){
            					eqAncestorMatch++;
        						String tName = t.get(TAXONSTR).getAsJsonObject().get(IDSTR).getAsString();
        						String gName = geneNames[j];
        						Match myMatch = new Match(Match.MatchType.EQ_ANCESTOR,tName,gName,tEntity,tQuality,gEntity,gQuality);
        						writeOrDump(myMatch.reportWithNames(u),bw);
            				}
            			}
            			//System.out.println(g);
            		}
            		double setTime =  (System.nanoTime() - startTime)/1.0e9;
            		System.out.println("Matches: Entities - " + entityMatch + "; Qualities -  " + qualityMatch + "; EQs - " + eqMatch + "; entityParent(Q=) - " + entityParentMatch + "; qualityParent(E=) - " + qualityParentMatch + "; entityAncestor(Q=) - " + entityAncestorMatch + "; qualityAncestor(E=) - " + qualityAncestorMatch + "; eqAncestor - " + eqAncestorMatch);
            		System.out.println("Time for this set =" + setTime);
            		loopTime += setTime;
            	}
            }
        	bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("Total time in loop = " + loopTime);
	}

	private void writeOrDump(String contents, BufferedWriter b){
		if (b == null)
			System.out.println(contents);
		else {
			try {
				b.write(contents);
				b.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
