package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import phenoscape.queries.lib.Match;
import phenoscape.queries.lib.Utils;

import com.google.gson.JsonObject;

public class PostCompList {

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
		final Collection<JsonObject> geneAnnotations = u.getGeneAnnotations();
    	if (geneAnnotations.isEmpty()){
    		System.out.println("No genes matched");
    		return;
    	}
		System.out.println("Read " + geneAnnotations.size() + " gene annotations");
		final Set<String> genePostComps = new HashSet<String>();
		for(JsonObject g : geneAnnotations){
			String gEntity = g.get("entity").getAsJsonObject().get(IDSTR).getAsString();
			if (gEntity.indexOf('^') != -1){
				genePostComps.add(gEntity);
			}
		}
		File outFile = new File(fileName);
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(outFile));
            writeOrDump("Post composed Entities in Gene annotations",bw);
            for (String pc : genePostComps){
            	writeOrDump(pc,bw);
            }
            
            final Set<String> taxaPostComps = new HashSet<String>();
            for(int i= 0;i<taxonCount;i+=CHUNKSIZE){
            	final Collection<JsonObject> taxonAnnotations = u.getTaxonAnnotations(CHUNKSIZE,i);
            	if (taxonAnnotations.isEmpty()){
            		System.out.println("No taxa matched");
            		return;			
            	}
            	else{
            		for(JsonObject t : taxonAnnotations){
            			System.out.println("Read " + taxonAnnotations.size() + " taxon annotations");
            			String tEntity = t.get("entity").getAsJsonObject().get(IDSTR).getAsString();
            			if (tEntity.indexOf('^') != -1)
            				taxaPostComps.add(tEntity);
            		}
            	}
            }
            writeOrDump("\n\nPost composed Entities in Taxa annotations",bw);
            for (String pc : taxaPostComps){
            	writeOrDump(pc,bw);
            }
            bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
