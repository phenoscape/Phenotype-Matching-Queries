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
	
	final private Map<Integer,Integer> linkPhenotypeMap = new HashMap<Integer,Integer>();   //link_node_id -> phenotype_node_id

	
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
	
	public void addLinkPhenotypePair(int linkId, int phenotypeId) {
		linkPhenotypeMap.put(linkId, phenotypeId);		
	}
	
	public void hasLinkToPhenotype(int linkId){
		linkPhenotypeMap.containsKey(linkId);
	}
	
	public Integer getPhenotypeFromLink(int linkId){
		return linkPhenotypeMap.get(linkId);
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
	
	private final static String COMPOSITIONQUERY = "select node.node_id FROM NODE WHERE node.label = 'composition'";
	private final static String STRUCTUREQUERY = "select node.node_id FROM node where node.label = 'structure'";
	
	/**
	 * This creates and fills a Map from qualities to attributes, stored as node_ids
	 * @param c
	 * @param u
	 * @throws SQLException
	 */
	public Map<Integer,Integer> setupAttributes() throws SQLException{
		Map<Integer,Integer> attMap = new HashMap<Integer,Integer>();
		Statement s1 = connection.createStatement();
		
		int compositionNodeID = 0;
		int structureNodeID = 0;
		ResultSet compositionResults = s1.executeQuery(COMPOSITIONQUERY);
		if (compositionResults.next()){
			compositionNodeID = compositionResults.getInt(1);
		}
		ResultSet structureResults = s1.executeQuery(STRUCTUREQUERY);
		if (structureResults.next()){
			structureNodeID = structureResults.getInt(1);
		}
		
		ResultSet attributeResults = s1.executeQuery(ATTRIBUTEQUERY);
		while(attributeResults.next()){
			final int quality_id = attributeResults.getInt(1);
			final int attribute_id = attributeResults.getInt(2);
			if (attribute_id == structureNodeID && attMap.containsKey(quality_id) && attMap.get(quality_id).intValue()==compositionNodeID){
				// do nothing
			}
			else {
				attMap.put(quality_id,attribute_id);				
			}
			if (!hasNodeUID(attribute_id))
				putNodeUIDName(attribute_id,attributeResults.getString(3),attributeResults.getString(4));
		}		
		
		return attMap;
	}

	
	

	public int getQualityNodeID() throws SQLException{
		int result = -1;
		Statement s1 = getStatement();
		ResultSet attResults = s1.executeQuery("SELECT node.node_id,node.uid,simple_label(node.node_id) FROM node WHERE node.label = 'quality'");
		if(attResults.next()){
			result = attResults.getInt(1);
			putNodeUIDName(result,attResults.getString(2),attResults.getString(3));
		}
		else{
			throw new RuntimeException("No node for 'quality' found in KB");
		}
		return result;
	}

	public void uidCacheEntities() throws SQLException{
		final Statement s1 = getStatement();
		final PreparedStatement p1 = getPreparedStatement("SELECT node.uid,simple_label(node.node_id) FROM node WHERE node.node_id = ?");
		ResultSet entResults = s1.executeQuery("SELECT DISTINCT node_id FROM link where link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a') AND link.object_id = (SELECT node.node_id FROM node WHERE node.label = 'teleost anatomical entity')");
		while(entResults.next()){
			int entityID = entResults.getInt(1);
			p1.setInt(1, entityID);
			ResultSet uidResults = p1.executeQuery();
			if (uidResults.next()){
				String uid = uidResults.getString(1);
				String label = uidResults.getString(2);
				putNodeUIDName(entityID,uid,label);
			}
			else {
				throw new RuntimeException("Entity query failed; id = " + entityID);
			}
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
	
	public void listIntegerMembers(Set<Integer> s, BufferedWriter b){
		for(Integer v : s){
			if (b == null){
				System.out.print(v.intValue() + " ");
			}
			else{
				try {
					b.write(v.intValue() + " ");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (b == null)
			System.out.println();
		else
			try {
				b.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public Statement getStatement() throws SQLException{
		return connection.createStatement();
	}
	
	public PreparedStatement getPreparedStatement(String sqlStatement) throws SQLException{
		return connection.prepareStatement(sqlStatement);
	}


	
}
