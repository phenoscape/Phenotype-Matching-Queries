package phenoscape.queries.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	final private Map<Integer,Set<Integer>> parents = new HashMap<Integer,Set<Integer>>(30000);
	final private Map<Integer,Set<Integer>> ancestors = new HashMap<Integer,Set<Integer>>(30000);
	
	private boolean parentsBuilt = false;
	
	private Connection connection;
	
	
	
	
	public String getNodeName(int id){
		return nodeNames.get(id);
	}
	
	public boolean hasNodeName(int id){
		return nodeNames.containsKey(id);
	}
	
	public void putNodeUIDName(int id, String uid, String name){
		nodeUIDs.put(id, uid);
		nodeNames.put(id, name);
	}
	
	public String getNodeUID(int nodeId){
		return nodeUIDs.get(nodeId);
	}
	
	public boolean hasNodeUID(int nodeId){
		return nodeUIDs.containsKey(nodeId);
	}
	
	public Set<Integer> getNodeSet(){
		return nodeUIDs.keySet();
	}

	
	
	public boolean hasNodeIDToName(int nodeId){
		return nodeNames.containsKey(nodeId);
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
	
	private final static String ATTRIBUTEQUERY = "SELECT quality_node_id,attribute_node_id,n.uid,simple_label(attribute_node_id) FROM quality_to_attribute " +
	"JOIN node AS n ON (n.node_id = attribute_node_id)";
	
	/**
	 * This creates and fills a Map from qualities to attributes, stored as node_ids
	 * @param c
	 * @param u
	 * @throws SQLException
	 */
	public Map<Integer,Integer> setupAttributes() throws SQLException{
		Map<Integer,Integer> attMap = new HashMap<Integer,Integer>();
		Statement s1 = connection.createStatement();
		ResultSet attributeResults = s1.executeQuery(ATTRIBUTEQUERY);
		while(attributeResults.next()){
			final int quality_id = attributeResults.getInt(1);
			final int attribute_id = attributeResults.getInt(2);
			attMap.put(quality_id,attribute_id);
			if (!hasNodeUID(attribute_id))
				putNodeUIDName(attribute_id,attributeResults.getString(3),attributeResults.getString(4));
		}
		return attMap;
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
	
	/** PSQL support stuff   */
	
	public Connection openKB(){
		final Properties properties = new Properties();
		try {
			//System.out.println("Connect path = " + this.getClass().getResource(CONNECTION_PROPERTIES_FILENAME));
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
		final String host = properties.getProperty("host");
		final String db = properties.getProperty("db");
		final String user = properties.getProperty("user");
		final String password = properties.getProperty("pw");
		try{
			connection = DriverManager.getConnection(String.format("jdbc:postgresql://%s/%s",host,db),user,password);
			return connection;
		} catch (SQLException e){
			System.err.println("Couldn't connect to server");
			e.printStackTrace();
			return null;
		}
	}
	
	public void closeKB(){
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("Problem closing connection");
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public Statement getStatement() throws SQLException{
		return connection.createStatement();
	}
	
	public PreparedStatement getPreparedStatement(String sqlStatement) throws SQLException{
		return connection.prepareStatement(sqlStatement);
	}
	

	
}
