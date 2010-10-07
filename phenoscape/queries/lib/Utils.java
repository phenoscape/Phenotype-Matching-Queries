package phenoscape.queries.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Queue;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Utils {
	
	final private static String IDSTR = "id";
	final private static String NAMESTR = "name";
	final private static String GENESTR = "gene";
	final private static String TAXONSTR = "taxon";
	final private static String ENTITYSTR = "entity";
	final private static String QUALITYSTR = "quality";
	
	final private static String pathStrRoot = StringConsts.KBBASEURL + "term/";
	final private static String pathSuffix = "/path";

	final static public Pattern PARENPATTERN = Pattern.compile("\\(");
	final static public Pattern CIRCUMFLEXPATTERN = Pattern.compile("\\^");
	
	final private Set<String> allTerms = new HashSet<String>(40000);

	final private Map<String,String> termNames = new HashMap<String,String>(40000);
	
	final private Map<String,Set<String>> parents = new HashMap<String,Set<String>>(30000);
	final private Map<String,Set<String>> ancestors = new HashMap<String,Set<String>>(30000);
	
	private boolean parentsBuilt = false;
	
	
	public String getTermName(String id){
		return termNames.get(id);
	}
	
	
	
	final Set<JsonObject> emptySJO = new HashSet<JsonObject>();
	public Set<JsonObject> getGeneAnnotations(){
		Set<JsonObject>result = new HashSet<JsonObject>();
		String genesString = null; 
		try {
			//final String formattedQuery = URLEncoder.encode(jasonQuery, "UTF-8");
			String jasonQuery = "annotation/gene?media=jason";
			genesString = StringConsts.KBBASEURL + jasonQuery;
			System.out.println("Request is " + URLDecoder.decode(genesString, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();		
			return emptySJO;
		}
		JsonObject root = parseFromURLStr(genesString);
		if (root != null){
			System.out.println("Root count is " + root.get("total"));
			System.out.println("First annotation is " + root.get("annotations").getAsJsonArray().get(0).getAsJsonObject());
			JsonArray geneArray = root.get("annotations").getAsJsonArray();
			for(JsonElement annotationElement : geneArray){
					JsonObject annotationObject = annotationElement.getAsJsonObject();
					JsonObject geneObject = annotationObject.get(GENESTR).getAsJsonObject();
					String geneID = geneObject.get(IDSTR).getAsString();
					if (!allTerms.contains(geneID)){
						allTerms.add(geneID);
					}
					if (!termNames.containsKey(geneID)){
						String geneName = geneObject.get(NAMESTR).getAsString();
						termNames.put(geneID, geneName);
					}
					JsonObject entityObject = annotationObject.get(ENTITYSTR).getAsJsonObject();
					String entityID = entityObject.get(IDSTR).getAsString();
					if (entityID.indexOf('^') == -1){
						if (!allTerms.contains(entityID)){
							allTerms.add(entityID);
						}
						if (!termNames.containsKey(entityID)){
							String entityName = entityObject.get(NAMESTR).getAsString();
							termNames.put(entityID, entityName);
						}
					}
					else { //need to parse postcomp
						String[] entityComps = CIRCUMFLEXPATTERN.split(entityID);
						String entityGenus = entityComps[0];
						if (!allTerms.contains(entityGenus)){
							allTerms.add(entityGenus);
						}
						if (!termNames.containsKey(entityGenus)){
							String entityName = entityObject.get(NAMESTR).getAsString();
							termNames.put(entityID, entityName);
						}
						String entityDifferentiaFinal = entityComps[entityComps.length-1];   //tEntityComps seems to parse out each component of the postcomp...
						if (entityDifferentiaFinal.indexOf('(') != -1 && entityDifferentiaFinal.indexOf(')') != -1){
							entityDifferentiaFinal = entityDifferentiaFinal.substring(entityDifferentiaFinal.indexOf('(')+1,entityDifferentiaFinal.indexOf(')'));
							if (!allTerms.contains(entityDifferentiaFinal)){
								allTerms.add(entityDifferentiaFinal);
							}
							if (!termNames.containsKey(entityID)){
								String entityName = entityObject.get(NAMESTR).getAsString();
								termNames.put(entityID, entityName);
							}
						}
						else System.out.println("Questionable post-comp parse: " +  entityDifferentiaFinal);
					}
					JsonObject qualityObject = annotationObject.get(QUALITYSTR).getAsJsonObject();
					String qualityID = qualityObject.get(IDSTR).getAsString();
					if (!allTerms.contains(qualityID)){
						allTerms.add(qualityID);
					}
					if (!termNames.containsKey(qualityID)){
						String qualityName = qualityObject.get(NAMESTR).getAsString();
						termNames.put(qualityID, qualityName);
					}
					result.add(annotationObject);
				}
			return result;
			}
		else{ 
			System.out.println("Query returned null");
			return emptySJO;
		}
	}
	
	
	public Set<JsonObject> getTaxonAnnotations(int limit, int start){
		Set<JsonObject>result = new HashSet<JsonObject>();
		String jasonQuery = "limit=" + Integer.toString(limit) + "&index=" + Integer.toString(start);
			String taxonString = null;
			try {
				String formattedQuery = URLEncoder.encode(jasonQuery, "UTF-8");
				taxonString = StringConsts.KBBASEURL + "annotation/taxon/distinct?media=jason&" + formattedQuery;
				System.out.println("Request is " + URLDecoder.decode(taxonString, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				System.err.println("Error encoding query for retrieving taxa");
				e.printStackTrace();
				return emptySJO;
			}
			JsonObject root = parseFromURLStr(taxonString);
			if (root != null){
				JsonArray taxonArray = root.get("annotations").getAsJsonArray();
				for (JsonElement annotationElement : taxonArray){
					JsonObject annotationObject = annotationElement.getAsJsonObject();
					JsonObject taxonObject = annotationObject.get(TAXONSTR).getAsJsonObject();
					String taxonID = taxonObject.get(IDSTR).getAsString();
					if (!allTerms.contains(taxonID)){
						allTerms.add(taxonID);
					}
					if (!termNames.containsKey(taxonID)){
						String taxonName = taxonObject.get(NAMESTR).getAsString();
						termNames.put(taxonID, taxonName);						
					}
					JsonObject entityObject = annotationObject.get(ENTITYSTR).getAsJsonObject();
					String entityID = entityObject.get(IDSTR).getAsString();
					if (entityID.indexOf('^') == -1){
						if (!allTerms.contains(entityID)){
							allTerms.add(entityID);
						}
						if (!termNames.containsKey(entityID)){
							String entityName = entityObject.get(NAMESTR).getAsString();
							termNames.put(entityID, entityName);
						}
					}
					else { //need to parse postcomp
						String[] entityComps = CIRCUMFLEXPATTERN.split(entityID);
						String entityGenus = entityComps[0];
						String entityDifferentiaFinal = entityComps[entityComps.length-1];   //tEntityComps seems to parse out each component of the postcomp...
						entityDifferentiaFinal = entityDifferentiaFinal.substring(entityDifferentiaFinal.indexOf('(')+1,entityDifferentiaFinal.indexOf(')'));
						if (!allTerms.contains(entityGenus)){
							allTerms.add(entityGenus);
						}
						if (!termNames.containsKey(entityGenus)){
							String entityName = entityObject.get(NAMESTR).getAsString();
							termNames.put(entityID, entityName);
						}
						if (!allTerms.contains(entityDifferentiaFinal)){
							allTerms.add(entityDifferentiaFinal);
						}
						if (!termNames.containsKey(entityID)){
							String entityName = entityObject.get(NAMESTR).getAsString();
							termNames.put(entityID, entityName);
						}
					}
					JsonObject qualityObject = annotationObject.get(QUALITYSTR).getAsJsonObject();
					String qualityID = qualityObject.get(IDSTR).getAsString();
					if (!allTerms.contains(qualityID)){
						allTerms.add(qualityID);
					}
					if (!termNames.containsKey(qualityID)){
						String qualityName = qualityObject.get(NAMESTR).getAsString();
						termNames.put(qualityID, qualityName);
					}
					result.add(annotationObject);
				}
				return result;
			}
			else{ 
				System.out.println("Query returned null");
				return emptySJO;
			}
	}
	
	
	// modified to force adding all accessible parents to the parent table
	public void buildParents(){
		System.out.println("Starting to build parents");
		long startTime = System.nanoTime();
		while(buildParentsaux());
		parentsBuilt = true;
		double setTime =  (System.nanoTime() - startTime)/1.0e9;
		System.out.println("Parent table size = " + parents.size());
		System.out.println("Time to add parents =" + setTime);

	}
	
	private boolean buildParentsaux(){
		final String[] dummyStrs = new String[0];
		final String[] termArray =  allTerms.toArray(dummyStrs);  // to avoid concurrentModification...
		boolean termsAdded = false;
		for(String termID : termArray){
			if (!parents.containsKey(termID)){
				if (termID.indexOf('^') == -1){
					String formattedQuery = null;
					try {
						//String jasonQuery = termID ;
						formattedQuery = pathStrRoot + URLEncoder.encode(termID, "UTF-8") + pathSuffix;
						//System.out.println("Request is " + URLDecoder.decode(formattedQuery, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						System.err.println("Error encoding query for retrieving taxa");
						e.printStackTrace();
					}
					JsonObject root = parseFromURLStr(formattedQuery);
					JsonArray pathArray = root.get("path").getAsJsonArray();
					if (pathArray != null){
						for (JsonElement pathElement : pathArray){
							String elementID = pathElement.getAsJsonObject().get("id").getAsString();
							if (!allTerms.contains(elementID)){
								allTerms.add(elementID);
							}
							if (!termNames.containsKey(elementID)){
								String elementName = pathElement.getAsJsonObject().get(NAMESTR).getAsString();
								termNames.put(elementID, elementName);
								termsAdded = true;
							}
							if (!parents.containsKey(elementID)){
								//System.out.print("Adding parents to " + elementID + "(" + termNames.get(elementID) + "): ");
								JsonArray parentArray = pathElement.getAsJsonObject().get("parents").getAsJsonArray();
								Set<String> parentSet = new HashSet<String>();
								for (JsonElement parentElement : parentArray){
									JsonObject parentObject = parentElement.getAsJsonObject();
									String parentRelation = parentObject.get("relation").getAsJsonObject().get("id").getAsString();
									if ("OBO_REL:is_a".equals(parentRelation) || "is_a".equals(parentRelation)){
										JsonObject parentTarget = parentObject.get("target").getAsJsonObject();
										String parentID = parentTarget.get("id").getAsString();
										parentSet.add(parentID);
										if (!allTerms.contains(parentID)){
											allTerms.add(parentID);
										}
										if (!termNames.containsKey(parentID)){
											if (parentTarget.get(NAMESTR) != null){
												String elementName = parentTarget.get(NAMESTR).getAsString();
												termNames.put(parentID, elementName);
												termsAdded = true;
											}
											else System.err.println("Warning: " + parentObject + "has no name?");
										}
										//System.out.println(termNames.get(parentID) + " ");
									}
								}
								parents.put(elementID, parentSet);
								//System.out.println(" ");
							}
						}
					}
					else {
						System.err.println("Term " + termID + " returned no path");
					}
				}
				else {	//TODO - deal with postcomps
				
				}
			}
		}
		return termsAdded;
	}
	
	
	//is term1 a parent of term2
	public boolean matchParent(String term1, String term2){
		if (term1.equals(term2))
			return true;
		else if (parents.containsKey(term2))
			return parents.get(term2).contains(term1);
		else
			return false;
	}
	
	//
	public void buildAncestors(){
		if (!parentsBuilt)
			buildParents();
		final String[] dummyStrs = new String[0];
		final String[] termArray = allTerms.toArray(dummyStrs);  // to avoid concurrentModification...
		//System.out.println("Started Building Ancestors");
		long startTime = System.nanoTime();
		Queue<String> workingList = new ArrayDeque<String>();
		for(String termID : termArray){
			if (!ancestors.containsKey(termID)){
				Set<String>ancestorSet = new HashSet<String>();
				workingList.add(termID);
				while(!workingList.isEmpty()){
					String nextParent = workingList.remove();  //shouldn't be here if queue is empty
					Set<String> aSet = parents.get(nextParent);
					if (aSet != null){
						ancestorSet.addAll(aSet);
						for(String anc : aSet)
							workingList.add(anc);  //not sure addAll would work here
					}
					else {
						
					}
				}
				ancestors.put(termID,ancestorSet);
			}
		}
		double setTime =  (System.nanoTime() - startTime)/1.0e9;
//		System.out.println("Ancestor table size = " + ancestors.size());
//		System.out.println("Time to add ancestors =" + setTime);
//		System.out.println("Finished building ancestors");
        BufferedWriter bw;
//        try {
//        	bw = new BufferedWriter(new FileWriter("parents.txt"));
//        	for(Entry<String,Set<String>> s : parents.entrySet()){
//        		if (true){
//        			bw.write(s.getKey()+ " : ");
//        			for(String a : s.getValue()){
//        				bw.write(a + ": ");
//        			}
//        			bw.newLine();
//        		}
//        	}
//        	bw.close();
//        } catch (IOException e) {
//        	// TODO Auto-generated catch block
//        	e.printStackTrace();
//        }
//
//        try {
//        	bw = new BufferedWriter(new FileWriter("Ancestors.txt"));
//        	for(Entry<String,Set<String>> s : ancestors.entrySet()){
//        		if (true){
//        			bw.write(s.getKey()+ " : ");
//        			for(String a : s.getValue()){
//        				bw.write(a + ": ");
//        			}
//        			bw.newLine();
//        		}
//        	}
//        	bw.close();
//        } catch (IOException e) {
//        	// TODO Auto-generated catch block
//        	e.printStackTrace();
//        }
	}
	
	//is term1 an ancestor of term2 (not fully implemented yet)
	public boolean matchAncestor(String term1, String term2){
		if (term1.equals(term2))
			return true;
		else if (ancestors.containsKey(term2))
			return ancestors.get(term2).contains(term1);
		else{
			buildAncestors();
			if (ancestors.containsKey(term2))
				return ancestors.get(term2).contains(term1);
			else return false;
		}
	}
	
	public String getAncestors(String term){
		StringBuilder result = new StringBuilder();
		if (ancestors.get(term) == null)
			return "Ancestors for term was null";
    	for(String s : ancestors.get(term)){
    		result.append(s);
    		result.append(" ");
    	}
    	return result.toString();
	}
	
	
	public int getTaxonAnnotationCount() {
		int result = 0;
			final String taxonString = StringConsts.KBBASEURL + "annotation/taxon/distinct?media=jason&limit=1";
			JsonObject root = parseFromURLStr(taxonString);
			if (root != null){
				result = root.get("total").getAsInt();
				System.out.println("Root count is " + result);
				return result;
			}
			else{
				System.out.println("Query returned null");
				return -1;
			}
	}

		
	private JsonObject parseFromURLStr(String urlStr){
		URL taxonURL;
		try {
			taxonURL = new URL(urlStr);
			Object response = taxonURL.getContent();
			if (response instanceof InputStream){
				BufferedReader responseReader = new BufferedReader(new InputStreamReader((InputStream)response));
				JsonParser parser = new JsonParser();
				JsonElement jroot = parser.parse(responseReader);
				return jroot.getAsJsonObject();
			}
			else
				return null;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	
	/**
	 * This returns a map containing the id -> name mapping of all the pubs in the database
	 * @return
	 */
	public Map<String,String> getPublications(){
		Map<String,String>result = new HashMap<String,String>(100);
		final String publicationString = StringConsts.KBBASEURL + "publication/annotated?media=txt";
		try {
			URL timeStampURL = new URL(publicationString);
			Object response = timeStampURL.getContent();
			if (response instanceof InputStream){
				BufferedReader responseReader
				   = new BufferedReader(new InputStreamReader((InputStream)response));
				String line = responseReader.readLine();
				while(line != null){
					String[] lineSplit = line.split("\t");
					if (lineSplit.length == 2){
						result.put(lineSplit[0],lineSplit[1]);
					}
					else{
						System.out.println("Bad line encountered: " + line);
					}
					line = responseReader.readLine();
				}
				((InputStream) response).close();
			}
			else if (response != null){
				System.out.println("Returned value was not a string; received a " + (response.getClass().toString()) + " instead");
				System.out.println("Value was " + response.toString());
			}
			else 
				System.out.println("Query returned null");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}


	
}
