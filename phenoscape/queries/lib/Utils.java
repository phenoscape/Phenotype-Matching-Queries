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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Queue;
import java.util.Set;

import org.obo.datamodel.OBOClass;

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
	final private static String termRoot = pathStrRoot;
	final private static String pathSuffix = "/path";

	final static public Pattern PARENPATTERN = Pattern.compile("\\(");
	final static public Pattern CIRCUMFLEXPATTERN = Pattern.compile("\\^");
	
	final static public Pattern TAOPATTERN = Pattern.compile("TAO:\\d+");
	final static public Pattern ZFAPATTERN = Pattern.compile("ZFA:\\d+");
	final static public Pattern GOPATTERN = Pattern.compile("GO:\\d+");
	final static public Pattern PATOPATTERN = Pattern.compile("PATO:\\d+");
	final static public Pattern BSPOPATTERN = Pattern.compile("BSPO:\\d+");

	private static final String CONNECTION_PROPERTIES_FILENAME = "connection.properties"; 
	
	
	//These are rather unfortunate, but KB-DEV currently (11-15-2010) uses an unreleased PATO with 
	//the relational qualities removed, but includes annotations with them.  So, the three relational 
	//qualities used by PATO will be hard coded here to support lookupIDToName and doSubstitutions.
	
	final static public String RELATIONALSHAPEQUALITYNAME = "relational shape quality";
	final static public String RELATIONALSPATIALQUALITYNAME = "relational spatial quality";
	final static public String RELATIONALSTRUCTURALQUALITYNAME = "relational structural quality";
	
	final static public String RELATIONALSHAPEQUALITYID = "PATO:0001647";
	final static public String RELATIONALSPATIALQUALITYID = "PATO:0001631";
	final static public String RELATIONALSTRUCTURALQUALITYID = "PATO:0001452";
	


	final private Map<Integer,String> nodeNames = new HashMap<Integer,String>(40000);
	final private Map<Integer,String> nodeUIDs = new HashMap<Integer, String>(40000);
	final private Map<String,String> UIDtoName = new HashMap<String,String>(40000);
	
	final private Map<Integer,Set<Integer>> parents = new HashMap<Integer,Set<Integer>>(30000);
	final private Map<Integer,Set<Integer>> ancestors = new HashMap<Integer,Set<Integer>>(30000);
	
	private boolean parentsBuilt = false;
	
	
	public String doSubstitutions(String s){
		Matcher taoMatcher = TAOPATTERN.matcher(s);
		while (taoMatcher.find()){
			final int first = taoMatcher.start();
			final int last = taoMatcher.end();
			final String id = s.substring(first,last);
			String name = lookupIDToName(id);
			s = s.substring(0,first)+name+s.substring(last);
			//System.out.println("New string is " + s );
			taoMatcher = TAOPATTERN.matcher(s);
		}
		Matcher zfaMatcher = ZFAPATTERN.matcher(s);
		while (zfaMatcher.find()){
			final int first = zfaMatcher.start();
			final int last = zfaMatcher.end();
			final String id = s.substring(first,last);
			String name = lookupIDToName(id);
			s = s.substring(0,first)+name+s.substring(last);
			//System.out.println("New string is " + s);
			zfaMatcher = ZFAPATTERN.matcher(s);
			
		}
		Matcher goMatcher = GOPATTERN.matcher(s);
		while (goMatcher.find()){
			final int first = goMatcher.start();
			final int last = goMatcher.end();
			final String id = s.substring(first,last);
			String name = lookupIDToName(id);
			s = s.substring(0,first)+name+s.substring(last);
			//System.out.println("New string is " + s);
			goMatcher = GOPATTERN.matcher(s);
			
		}
		Matcher patoMatcher = PATOPATTERN.matcher(s);
		while (patoMatcher.find()){
			final int first = patoMatcher.start();
			final int last = patoMatcher.end();
			final String id = s.substring(first,last);
			String name = lookupIDToName(id);
			s = s.substring(0,first)+name+s.substring(last);
			//System.out.println("New string is " + s);
			patoMatcher = PATOPATTERN.matcher(s);
			
		}
		Matcher bspoMatcher = BSPOPATTERN.matcher(s);
		while (bspoMatcher.find()){
			final int first = bspoMatcher.start();
			final int last = bspoMatcher.end();
			final String id = s.substring(first,last);
			String name = lookupIDToName(id);
			s = s.substring(0,first)+name+s.substring(last);
			//System.out.println("New string is " + s);
			bspoMatcher = BSPOPATTERN.matcher(s);			
		}
		//System.out.println("New string is " + s);
		return s;
	}
	
	OBOClass getTerm(Collection<OBOClass> terms, String id){
		for(OBOClass term : terms){
			if (id.equals(term.getID()))
				return term;
		}
		return null;
	}

	
	
	
	
	public String getNodeName(int id){
		return nodeNames.get(id);
	}
	
	public boolean hasNodeName(int id){
		return nodeNames.containsKey(id);
	}
	
	public void putNodeName(int id, String name){
		nodeNames.put(id, name);
	}
	
	public String getNodeUID(int nodeId){
		return nodeUIDs.get(nodeId);
	}
	
	public boolean hasNodeUID(int nodeId){
		return nodeUIDs.containsKey(nodeId);
	}
	
	public void putNodeUID(int nodeId, String name){
		nodeUIDs.put(nodeId, name);
	}
	
	public Set<Integer> getNodeSet(){
		return nodeUIDs.keySet();
	}

	public void cacheUIDtoName(String name, String UID){
		if (!hasUIDtoName(UID))
			UIDtoName.put(UID,name);
	}
	
	public boolean hasUIDtoName(String UID){
		return UIDtoName.containsKey(UID);
	}
	
	public String getNameFromUID(String UID){
		return UIDtoName.get(UID);
	}
	
	public void addParent(Integer nodeID, Integer parentID){
		if (parents.containsKey(nodeID)){
			parents.get(nodeID).add(parentID);
		}
		else{
			Set<Integer> parentSet = new HashSet<Integer>(5);
			parentSet.add(parentID);
			parents.put(nodeID, parentSet);
		}
	}
	
	public Set<Integer> getParents(Integer nodeID){
		return parents.get(nodeID);
	}
	
	public void printTableReport(){
		System.out.println("nodeUIDs.size() = " + nodeUIDs.size());
		System.out.println("parents.size() = " + parents.size());
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
					result.add(annotationObject);
				}
				return result;
			}
			else{ 
				System.out.println("Query returned null");
				return emptySJO;
			}
	}
	
	
	
	
//	
//	//
//	public void buildAncestors(){
//		if (!parentsBuilt)
//			//buildParents();
//		final Integer[] dummyStrs = new Integer[0];
//		final Integer[] termArray = allTerms.toArray(dummyStrs);  // to avoid concurrentModification...
//		//System.out.println("Started Building Ancestors");
//		long startTime = System.nanoTime();
//		Queue<Integer> workingList = new ArrayDeque<Integer>();
//		for(Integer termID : termArray){
//			if (!ancestors.containsKey(termID)){
//				Set<Integer>ancestorSet = new HashSet<Integer>();
//				workingList.add(termID);
//				while(!workingList.isEmpty()){
//					Integer nextParent = workingList.remove();  //shouldn't be here if queue is empty
//					Set<Integer> aSet = parents.get(nextParent);
//					if (aSet != null){
//						ancestorSet.addAll(aSet);
//						for(Integer anc : aSet)
//							workingList.add(anc);  //not sure addAll would work here
//					}
//					else {
//						
//					}
//				}
//				ancestors.put(termID,ancestorSet);
//			}
//		}
//		double setTime =  (System.nanoTime() - startTime)/1.0e9;
////		System.out.println("Ancestor table size = " + ancestors.size());
////		System.out.println("Time to add ancestors =" + setTime);
////		System.out.println("Finished building ancestors");
//        BufferedWriter bw;
////        try {
////        	bw = new BufferedWriter(new FileWriter("parents.txt"));
////        	for(Entry<String,Set<String>> s : parents.entrySet()){
////        		if (true){
////        			bw.write(s.getKey()+ " : ");
////        			for(String a : s.getValue()){
////        				bw.write(a + ": ");
////        			}
////        			bw.newLine();
////        		}
////        	}
////        	bw.close();
////        } catch (IOException e) {
////        	// TODO Auto-generated catch block
////        	e.printStackTrace();
////        }
////
////        try {
////        	bw = new BufferedWriter(new FileWriter("Ancestors.txt"));
////        	for(Entry<String,Set<String>> s : ancestors.entrySet()){
////        		if (true){
////        			bw.write(s.getKey()+ " : ");
////        			for(String a : s.getValue()){
////        				bw.write(a + ": ");
////        			}
////        			bw.newLine();
////        		}
////        	}
////        	bw.close();
////        } catch (IOException e) {
////        	// TODO Auto-generated catch block
////        	e.printStackTrace();
////        }
//	}
	
//	//is term1 an ancestor of term2 (not fully implemented yet)
//	public boolean matchAncestor(String term1, String term2){
//		if (term1.equals(term2))
//			return true;
//		else if (ancestors.containsKey(term2))
//			return ancestors.get(term2).contains(term1);
//		else{
//			buildAncestors();
//			if (ancestors.containsKey(term2))
//				return ancestors.get(term2).contains(term1);
//			else return false;
//		}
//	}
//	
//	public String getAncestors(String term){
//		StringBuilder result = new StringBuilder();
//		if (ancestors.get(term) == null)
//			return "Ancestors for term was null";
//    	for(Integer s : ancestors.get(term)){
//    		result.append(s.toString());
//    		result.append(" ");
//    	}
//    	return result.toString();
//	}
	
	
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

	
	public String lookupIDToName(String id){
		if (hasUIDtoName(id))
			return getNameFromUID(id);
		String formattedQuery = null;
		String result = null;
		try {
			formattedQuery = termRoot + URLEncoder.encode(id, "UTF-8");
			//System.out.println("Request is " + URLDecoder.decode(formattedQuery, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			System.err.println("Error encoding query for retrieving taxa");
			e.printStackTrace();
		}
		JsonObject nameResponse = parseFromURLStr(formattedQuery);
		if (nameResponse == null || nameResponse.get("name") == null){
			if (RELATIONALSHAPEQUALITYID.equals(id))    //ugly hard coded solution to PATO and annotation KB being out of synch
				return RELATIONALSHAPEQUALITYNAME;
			if (RELATIONALSPATIALQUALITYID.equals(id))
				return RELATIONALSPATIALQUALITYNAME;
			if (RELATIONALSTRUCTURALQUALITYID.equals(id))
				return RELATIONALSTRUCTURALQUALITYNAME;
			System.err.println("Null response to lookup of " + id);
			result = id.substring(0,1) + "x" + id.substring(2);  // avoid infinite loop in substitution
		}
		else result = nameResponse.get("name").getAsString();
		cacheUIDtoName(id,result);
		return result;
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
	
	/** PSQL support stuff   */
	
	public Connection openKB(){
		final Properties properties = new Properties();
		try {
			properties.load(this.getClass().getResourceAsStream(CONNECTION_PROPERTIES_FILENAME));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		try {
			Class.forName("org.postgresql.Driver");
		} catch(ClassNotFoundException e){
			System.err.println("Couldn't load PSQL Driver");
			e.printStackTrace();
		}
		Connection c= null;
		final String host = properties.getProperty("host");
		final String db = properties.getProperty("db");
		final String user = properties.getProperty("user");
		final String password = properties.getProperty("pw");
		try{
			c = DriverManager.getConnection(String.format("jdbc:postgresql://%s/%s",host,db),user,password);
			return c;
		} catch (SQLException e){
			System.err.println("Cound't connect to server");
			e.printStackTrace();
			return null;
		}

	}

	public void writeOrDump(String contents, BufferedWriter b){
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
