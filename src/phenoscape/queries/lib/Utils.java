package phenoscape.queries.lib;

import java.io.Writer;
import java.io.IOException;
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
import java.util.regex.Pattern;

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
	 * This creates and fills a Map from qualities to attributes, stored as node_ids and tries to separate composition from structure
	 * since these are not disjoint.
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
		else 
			throw new RuntimeException("Query for node id of 'composition' failed");

		ResultSet structureResults = s1.executeQuery(STRUCTUREQUERY);
		if (structureResults.next()){
			structureNodeID = structureResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of 'structure' failed");
		
		ResultSet attributeResults = s1.executeQuery(ATTRIBUTEQUERY);
		//loops through and if there is an assignment of 'composition' to the quality already and the current result
		//maps the quality to 'structure' then ignore this result.  This should be safe if using a character slim that
		//doesn't include 'composition' as an attribute.
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

	private final static String RECIPROCALIDQUERY = "SELECT node.node_id FROM node WHERE node.label = 'reciprocal_of'";
	private final static String RECIPROCALQUERY = "SELECT link.node_id,link.object_id FROM link WHERE link.is_inferred = false AND link.predicate_id = ";
	public Map<Integer,Integer> setupReciprocals() throws SQLException{
		Map<Integer,Integer> recipMap = new HashMap<Integer,Integer>();
		int recipID;
		Statement s1 = connection.createStatement();
		ResultSet idResult = s1.executeQuery(RECIPROCALIDQUERY);
		if (idResult.next()){
			recipID = idResult.getInt(1);
		}
		else
			throw new RuntimeException("Query for node id of 'reciprocal' failed");
		ResultSet recipResult = s1.executeQuery(RECIPROCALQUERY + recipID);
		while(recipResult.next()){
			int oldTerm = recipResult.getInt(1);
			int preferredTerm = recipResult.getInt(2);
			recipMap.put(oldTerm, preferredTerm);
		}
		return recipMap;
	}
	
	private final static String CORRELATESIDQUERY = "SELECT node.node_id FROM node WHERE node.label = 'correlates_with'";
	private final static String CORRELATESQUERY = "SELECT link.node_id,link.object_id FROM link WHERE link.predicate_id = ";
	public Map<Integer,Integer> setupCorrelatesMap() throws SQLException{
		Map<Integer,Integer> corMap = new HashMap<Integer,Integer>();
		int recipID;
		Statement s1 = connection.createStatement();
		ResultSet idResult = s1.executeQuery(CORRELATESIDQUERY);
		if (idResult.next()){
			recipID = idResult.getInt(1);
		}
		else
			throw new RuntimeException("Query for node id of 'reciprocal' failed");
		ResultSet recipResult = s1.executeQuery(CORRELATESQUERY + recipID);
		while(recipResult.next()){
			int oldTerm = recipResult.getInt(1);
			int preferredTerm = recipResult.getInt(2);
			corMap.put(oldTerm, preferredTerm);
		}
		return corMap;
	}

	
	private final static String QUALITYNODEQUERY = "SELECT node.node_id,node.uid,simple_label(node.node_id) FROM node WHERE node.label = 'quality'";
	public int getQualityNodeID() throws SQLException{
		int result = -1;
		Statement s1 = getStatement();
		ResultSet attResults = s1.executeQuery(QUALITYNODEQUERY);
		if(attResults.next()){
			result = attResults.getInt(1);
			putNodeUIDName(result,attResults.getString(2),attResults.getString(3));
		}
		else{
			throw new RuntimeException("No node for 'quality' found in KB");
		}
		return result;
	}

	
	/**
	 * 
	 * @throws SQLException
	 */
	public void cacheEntities() throws SQLException{
		final Statement s1 = getStatement();  //Not sure where the cut off between Statement and PreparedStatement lies...
		ResultSet entResults = s1.executeQuery("SELECT DISTINCT node_id FROM link where link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a') AND link.object_id = (SELECT node.node_id FROM node WHERE node.label = 'teleost anatomical entity')");
		cacheEntitiesFromResults(entResults);
		entResults = s1.executeQuery("SELECT DISTINCT node_id FROM link where link.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a') AND link.object_id = (SELECT node.node_id FROM node WHERE node.label = 'biological_process')");
		cacheEntitiesFromResults(entResults);
	}
	
	private void cacheEntitiesFromResults(ResultSet entityResults) throws SQLException{
		while(entityResults.next()){
			int entityID = entityResults.getInt(1);
			cacheOneNode(entityID);
		}
	}
	
	public void cacheOneNode(int nodeID) throws SQLException{
		final PreparedStatement p1 = getPreparedStatement("SELECT node.uid,simple_label(node.node_id) FROM node WHERE node.node_id = ?");
		p1.setInt(1, nodeID);
		ResultSet uidResults = p1.executeQuery();
		if (uidResults.next()){
			String uid = uidResults.getString(1);
			String label = uidResults.getString(2);
			putNodeUIDName(nodeID,uid,label);
		}
		else {
			throw new RuntimeException("Node lookup query failed; id = " + nodeID);
		}
		
	}


	
	private static final String ASSERTEDTAXONPHENOTYPECOUNTQUERY = "SELECT COUNT(*) FROM asserted_taxon_annotation";
	public int countAssertedTaxonPhenotypeAnnotations() throws SQLException{
		return getCount(ASSERTEDTAXONPHENOTYPECOUNTQUERY);
	}

	private static final String ASSERTEDGENEPHENOTYPECOUNTQUERY =  "SELECT COUNT(*) FROM distinct_gene_annotation";
	public int countDistinctGenePhenotypeAnnotations() throws SQLException{
		return getCount(ASSERTEDGENEPHENOTYPECOUNTQUERY);	
	}

	private static final String ENTITYPARENTQUERY = 
		"SELECT target.node_id FROM node AS pheno " +
		"JOIN link ON (pheno.node_id=link.node_id AND link.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:inheres_in_part_of')) " + 
		"JOIN node AS target ON (target.node_id = link.object_id) WHERE pheno.node_id = ? ";

	public Set<Integer> collectEntityParents(int pheno) throws SQLException{
		PreparedStatement entityParentsStatement = getPreparedStatement(ENTITYPARENTQUERY); 
		final Set<Integer> results = new HashSet<Integer>();
		entityParentsStatement.setInt(1, pheno);
		ResultSet entityParents = entityParentsStatement.executeQuery();
		while(entityParents.next()){
			int target_id = entityParents.getInt(1);
			results.add(target_id);
		}
		return results;
	}

	private static final String QUALITYPARENTQUERY = 
		"SELECT target.node_id FROM node AS quality " +
		"JOIN link ON (quality.node_id=link.node_id AND link.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:is_a')) " +
		"JOIN node AS target ON (target.node_id = link.object_id) WHERE quality.node_id = ? ";

	public Set<Integer> collectQualityParents(int quality) throws SQLException{
		PreparedStatement qualityParentsStatement = getPreparedStatement(QUALITYPARENTQUERY); 
		final Set<Integer> results = new HashSet<Integer>();
		qualityParentsStatement.setInt(1, quality);
		ResultSet entityParents = qualityParentsStatement.executeQuery();
		while(entityParents.next()){
			int target_id = entityParents.getInt(1);
			results.add(target_id);
		}
		if (results.isEmpty())
			throw new RuntimeException("");
		return results;
	}
	
	
	
	/**
	 * This returns a count from a query
	 * @param query
	 * @return
	 * @throws SQLException 
	 */
	private int getCount(String query) throws SQLException{
		final Statement s = getStatement();
		ResultSet countResult = s.executeQuery(query);
		if (countResult.next()){
			return countResult.getInt(1);
		}
		else {
			throw new RuntimeException("Count query failed");
		}

	}
	

	
	/** PSQL support stuff   */
	
	public String openKB() throws SQLException{
		return openKBFromConnections(CONNECTION_PROPERTIES_FILENAME);
	}
	
	private String openKBFromConnections(String connectionsSpec) throws SQLException {
		final Properties properties = new Properties();
		try {
			//System.out.println("Connect path = " + this.getClass().getResource(CONNECTION_PROPERTIES_FILENAME));
			properties.load(this.getClass().getResourceAsStream(connectionsSpec));
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
		connection = DriverManager.getConnection(String.format("jdbc:postgresql://%s/%s",host,db),user,password);
		return "Host: " + host + " db: " + db;
		
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
	
	final static String testQuery = "SELECT * FROM node limit 1";
	public boolean checkConnection(){
		try{
			Statement s = connection.createStatement();
			ResultSet tResult = s.executeQuery(testQuery);
		}
		catch (SQLException e){
			return false;
		}
		return true;
	}
	
	public void retryKB() throws SQLException{
		openKB();
	}

	final static String lineSeparator = System.getProperty("line.separator");
	public void writeOrDump(String contents, Writer b){
		if (b == null)
			System.out.println(contents);
		else {
			try {
				b.write(contents);
				b.write(lineSeparator);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void listIntegerMembers(Set<Integer> s, Writer b){
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
				b.write(lineSeparator);
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
