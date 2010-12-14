package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import phenoscape.queries.lib.StringConsts;
import phenoscape.queries.lib.Utils;

import com.google.gson.JsonObject;

public class PostCompList {

	final static int CHUNKSIZE = 5000;
	final static String TAXONSTR = "taxon";
	final static String IDSTR = "id";
	
	final static public Pattern TAOPATTERN = Pattern.compile("TAO:\\d+");
	final static public Pattern ZFAPATTERN = Pattern.compile("ZFA:\\d+");
	final static public Pattern GOPATTERN = Pattern.compile("GO:\\d+");
	final static public Pattern PATOPATTERN = Pattern.compile("PATO:\\d+");
	final static public Pattern BSPOPATTERN = Pattern.compile("BSPO:\\d+");

	final private static String pathStrRoot = StringConsts.KBBASEURL + "term/";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		PostCompList reporter = new PostCompList();
		reporter.writeReport("PostCompList.txt",new Utils());

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
				genePostComps.add(doSubstitutions(gEntity,u));
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
            			String tEntity = t.get("entity").getAsJsonObject().get(IDSTR).getAsString();
            			if (tEntity.indexOf('^') != -1)
            				taxaPostComps.add(doSubstitutions(tEntity,u));
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

	private String doSubstitutions(String s, Utils u){
		
		Matcher taoMatcher = TAOPATTERN.matcher(s);
		while (taoMatcher.find()){
			final int first = taoMatcher.start();
			final int last = taoMatcher.end();
			final String id = s.substring(first,last);
			String name = u.lookupNameFromID  (id);
			s = s.substring(0,first)+name+s.substring(last);
			//System.out.println("New string is " + s);
			taoMatcher = TAOPATTERN.matcher(s);
		}
		Matcher zfaMatcher = ZFAPATTERN.matcher(s);
		while (zfaMatcher.find()){
			final int first = zfaMatcher.start();
			final int last = zfaMatcher.end();
			final String id = s.substring(first,last);
			String name = u.lookupNameFromID(id);
			s = s.substring(0,first)+name+s.substring(last);
			//System.out.println("New string is " + s);
			zfaMatcher = ZFAPATTERN.matcher(s);
			
		}
		Matcher goMatcher = GOPATTERN.matcher(s);
		while (goMatcher.find()){
			final int first = goMatcher.start();
			final int last = goMatcher.end();
			final String id = s.substring(first,last);
			String name = u.lookupNameFromID(id);
			s = s.substring(0,first)+name+s.substring(last);
			//System.out.println("New string is " + s);
			goMatcher = GOPATTERN.matcher(s);
			
		}
		Matcher patoMatcher = PATOPATTERN.matcher(s);
		while (patoMatcher.find()){
			final int first = patoMatcher.start();
			final int last = patoMatcher.end();
			final String id = s.substring(first,last);
			String name = u.lookupNameFromID(id);
			s = s.substring(0,first)+name+s.substring(last);
			patoMatcher = PATOPATTERN.matcher(s);
			
		}
		Matcher bspoMatcher = BSPOPATTERN.matcher(s);
		while (bspoMatcher.find()){
			final int first = bspoMatcher.start();
			final int last = bspoMatcher.end();
			final String id = s.substring(first,last);
			String name = u.lookupNameFromID(id);
			s = s.substring(0,first)+name+s.substring(last);
			//System.out.println("New string is " + s);
			bspoMatcher = BSPOPATTERN.matcher(s);			
		}
		//System.out.println("New string is " + s);
		return s;
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
