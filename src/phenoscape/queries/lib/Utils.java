package phenoscape.queries.lib;

import java.io.IOException;
import java.io.Writer;
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

import org.apache.log4j.Logger;
import org.phenoscape.obd.loader.Vocab;

public class Utils {
	
	
	private static final String CONNECTION_PROPERTIES_FILENAME = "unitTestconnection.properties"; 
	
	
	final private Map<Integer,String> nodeNames = new HashMap<Integer,String>(40000);
	final private Map<Integer,String> nodeUIDs = new HashMap<Integer, String>(40000);
	
	final private Map<Integer,Set<Integer>> parents = new HashMap<Integer,Set<Integer>>(30000);
	
	final private Map<Integer,Integer> linkPhenotypeMap = new HashMap<Integer,Integer>();   //link_node_id -> phenotype_node_id

	private Connection connection;
	
	
	static final Logger logger = Logger.getLogger(Utils.class);

	
	public String getNodeName(int id){
		return nodeNames.get(id);
	}
	
	public int getIDFromName(String name){
		if (name == null)
			return -1;
		for (Integer nodeInt : nodeNames.keySet()){
			if (name.equals(nodeNames.get(nodeInt)))
				return nodeInt.intValue();
		}
		return -1;
	}

	public int getUIDFromName(String name){
		if (name == null)
			return -1;
		for (Integer nodeInt : nodeUIDs.keySet()){
			if (name.equals(nodeUIDs.get(nodeInt)))
				return nodeInt.intValue();
		}
		return -1;
	}
	
	public int getIDFromUID(String uid){
		if (uid == null)
			return -1;
		for (Integer nodeInt : nodeUIDs.keySet()){
			if (uid.equals(nodeUIDs.get(nodeInt)))
				return nodeInt.intValue();
		}
		return -1;
	}

	public boolean hasNodeName(int id){
		return nodeNames.containsKey(id);
	}
	
	/**
	 * Adds the node to the lookup tables for UIDs and names (so they can be retrieved later for debugging and reporting)
	 * @param id database id of the node
	 * @param uid the unique identifier string (may be an EQ expression) of the node
	 * @param name the assigned name (if any) of the node
	 */
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
	

	public boolean isSymmetricProperty(int nodeId) throws SQLException{
		cacheOneNode(nodeId);
		String uid = nodeUIDs.get(nodeId);
		return Vocab.SYMMETRIC_QUALITIES.contains(uid);
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
	 * This creates and fills a Map from qualities to attributes, stored as node_ids and tries to separate composition from structure
	 * since these are not disjoint.
	 * @param c
	 * @param u
	 * @throws SQLException
	 */
	public Map<Integer,Integer> setupAttributes() throws SQLException{
		Map<Integer,Integer> attMap = new HashMap<Integer,Integer>();
		//Map<Integer,Set<Integer>> attAll = new HashMap<Integer,Set<Integer>>();
		Statement s1 = connection.createStatement();
		
		
		// if getOneName fails, these will be assigned -1 and shouldn't affect anything (handles unit test cases with incomplete attribute sets)
		int compositionNodeID = getOneName("composition");
		int structureNodeID = getOneName("structure");
		int structureMinusCompositionNodeID = getOneName("structure minus composition");
		int closureNodeID = getOneName("closure");
		int sizeNodeID = getOneName("size");
		int shapeNodeID = getOneName("shape");
		int closureStructureNodeID = getOneName("Closure+Structure");
		int shapeSizeNodeID = getOneName("Shape+Size");
		
		
		ResultSet attributeResults = s1.executeQuery(ATTRIBUTEQUERY);
		//loops through and if there is an assignment of 'composition' to the quality already and the current result
		//maps the quality to 'structure' then ignore this result.  This should be safe if using a character slim that
		//doesn't include 'composition' as an attribute.
		while(attributeResults.next()){
			final int quality_id = attributeResults.getInt(1);
			final int attribute_id = attributeResults.getInt(2);
//			if (!attAll.containsKey(quality_id)){
//				attAll.put(quality_id, new HashSet<Integer>());
//			}
//			else {
				attMap.put(quality_id,attribute_id);
//				attAll.get(quality_id).add(attribute_id);
//			}
			if (!hasNodeUID(attribute_id)){
				putNodeUIDName(attribute_id,attributeResults.getString(3),attributeResults.getString(4));
			}
		}		
		attMap.put(structureNodeID,structureNodeID);
		attMap.put(structureNodeID, getOneUID("PATO:0000141^PHENOSCAPE:complement_of(PATO:0001590)"));
		//attAll.put(structureNodeID, new HashSet<Integer>());
		//attAll.get(structureNodeID).add(structureNodeID);
		
//		int count = 0;
//		for(Integer q : attAll.keySet())
//			if (attAll.get(q).size()>1)
//				count++;
//		logger.info("Total of " + count + " qualities assigned to multiple attributes");
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

	private final static String PHENOTYPEENTITYQUERY = "SELECT node_id, entity_node_id FROM phenotype";
	private static final String ENTITYPARENTQUERY = 
		"SELECT target.node_id FROM node AS pheno " +
		"JOIN link ON (pheno.node_id=link.node_id AND link.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:inheres_in_part_of')) " + 
		"JOIN node AS target ON (target.node_id = link.object_id) WHERE pheno.node_id = ? ";
	public void setupEntityParents(Map<Integer,Set<Integer>> parents, Map<Integer,Set<Integer>> children) throws SQLException{
		final Statement s1 = getStatement();
		final PreparedStatement entityParentsStatement = getPreparedStatement(ENTITYPARENTQUERY);
		ResultSet entResults = s1.executeQuery(PHENOTYPEENTITYQUERY);
		while(entResults.next()){
			int phenoID = entResults.getInt(1);
			int entityID = entResults.getInt(2);
			if (!parents.containsKey(entityID)){
				cacheOneNode(entityID);
				Set<Integer> entParentSet = new HashSet<Integer>();
				entityParentsStatement.setInt(1,phenoID);
				ResultSet parentResults = entityParentsStatement.executeQuery();
				while (parentResults.next()){
					int parentID = parentResults.getInt(1);
					entParentSet.add(parentID);
				}
				if (entParentSet.isEmpty()){
					throw new RuntimeException("empty parent set of " + getNodeName(entityID));
				}
				parents.put(entityID, entParentSet);
			}
			for(Integer parent : parents.get(entityID)){
				Set<Integer>childSet;
				if (!children.containsKey(parent)){
					childSet = new HashSet<Integer>();
					children.put(parent, childSet);
				}
				else{
					childSet = children.get(parent);
				}
				childSet.add(entityID);
			}
		}
	}
	
	private final static String NAMEQUERY = "select node.node_id FROM node WHERE node.label = ?";

	public int getOneName(String name) throws SQLException{
		int result= -1;
		PreparedStatement p1 = connection.prepareStatement(NAMEQUERY);
		p1.setString(1, name);
		ResultSet results = p1.executeQuery();
		if (results.next()){
			result = results.getInt(1);
			return result;
		}
		else {
			System.err.println("Failed to lookup " + name);
			return -1;
		}
	}
	
	private final static String UIDQUERY = "select node.node_id FROM node WHERE node.uid = ?";
	public int getOneUID(String uid) throws SQLException{
		int result= -1;
		PreparedStatement p1 = connection.prepareStatement(UIDQUERY);
		p1.setString(1, uid);
		ResultSet results = p1.executeQuery();
		if (results.next()){
			result = results.getInt(1);
			return result;
		}
		else {
			System.err.println("Failed to lookup " + uid);
			return -1;
		}
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

	public void cacheOneNodeFromUID(String nodeUID) throws SQLException{
		final PreparedStatement p1 = getPreparedStatement("SELECT node.node_id,simple_label(node.node_id) FROM node WHERE node.uid = ?");
		p1.setString(1, nodeUID);
		ResultSet uidResults = p1.executeQuery();
		if (uidResults.next()){
			int id = uidResults.getInt(1);
			String label = uidResults.getString(2);
			putNodeUIDName(id,nodeUID,label);
		}
		else {
			throw new RuntimeException("Node lookup query failed; uid = " + nodeUID);
		}
		
	}

	
	public void fillNames(Object e) throws SQLException{
		if (e instanceof PhenotypeExpression)
			((PhenotypeExpression) e).fillNames(this);
		else if (e instanceof Integer)
			cacheOneNode((Integer) e);
		else
			throw new IllegalArgumentException();
	}
	

	public String stringForMessage(Object e){
		if (e instanceof PhenotypeExpression){
			PhenotypeExpression pe = (PhenotypeExpression)e;
			return getNodeName(pe.getEntity()) + " " + getNodeName(pe.getQuality());
		}
		else if (e instanceof Integer){
			return getNodeName((Integer)e);
		}
		else
			throw new IllegalArgumentException();
	}
	
	public String fullNameString(Object e) throws SQLException{
		if (e instanceof PhenotypeExpression){
			return ((PhenotypeExpression) e).getFullName(this);
		}
		else if (e instanceof Integer){
			return getNodeName((Integer)e);
		}
		else
			throw new IllegalArgumentException();
		
	}
	
	
	private static final String ASSERTEDTAXONPHENOTYPECOUNTQUERY = "SELECT COUNT(*) FROM asserted_taxon_annotation";
	public int countAssertedTaxonPhenotypeAnnotations() throws SQLException{
		return getCount(ASSERTEDTAXONPHENOTYPECOUNTQUERY);
	}

	private static final String ASSERTEDGENEPHENOTYPECOUNTQUERY =  "SELECT COUNT(*) FROM distinct_gene_annotation";
	public int countDistinctGenePhenotypeAnnotations() throws SQLException{
		return getCount(ASSERTEDGENEPHENOTYPECOUNTQUERY);	
	}

	private static final String ASSERTEDGENEENTITYCOUNTQUERY = "SELECT entity_node_id FROM distinct_gene_annotation";
	public int countDistinctGeneEntityPhenotypeAnnotations() throws SQLException {
		Set<Integer> usedEntities = new HashSet<Integer>();
		Statement s = getStatement();
		ResultSet entities = s.executeQuery(ASSERTEDGENEENTITYCOUNTQUERY);
		while(entities.next()){
			int entityid = entities.getInt(1);
			usedEntities.add(entityid);
		}
		return usedEntities.size();
	}

	private static final String ASSERTEDTAXONENTITYCOUNTQUERY = 
			"select distinct p.entity_node_id from asserted_taxon_annotation as ata " +
			"left join phenotype as p on (ata.phenotype_node_id = p.node_id)";
	public int countDistinctTaxonEntityPhenotypeAnnotations() throws SQLException {
		Set<Integer> usedEntities = new HashSet<Integer>();
		Statement s = getStatement();
		ResultSet entities = s.executeQuery(ASSERTEDTAXONENTITYCOUNTQUERY);
		while(entities.next()){
			int entityid = entities.getInt(1);
			usedEntities.add(entityid);
		}
		return usedEntities.size();
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
		qualityParentsStatement.close();
		if (results.isEmpty())
			throw new RuntimeException("");
		return results;
	}

	private static final String ALLPHENOTYPESQUERY =
		"SELECT node_id FROM phenotype";
	
	public Map<Integer,Set<Integer>> buildPhenotypeSubsumers() throws SQLException{
		Statement phenotypesStatement = getStatement();
		final Set<Integer> phenolist = new HashSet<Integer>();
		final ResultSet phenoResults = phenotypesStatement.executeQuery(ALLPHENOTYPESQUERY);
		final Map<Integer,Set<Integer>> result = new HashMap<Integer,Set<Integer>>();
		while(phenoResults.next()){
			int pheno_id = phenoResults.getInt(1);
			phenolist.add(pheno_id);
		}
		System.out.println("Loaded phenotypes");
		int count =0;
		for(Integer i : phenolist){
			Set<Integer> parents = collectQualityParents(i);
			result.put(i,parents);
			count++;
			if (count % 100 == 0){
				logger.info("Processed " + count + " phenotype parents");				
			}
		}
		phenotypesStatement.close();
		return result;
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
	
	public String openKBFromConnections(String connectionsSpec) throws SQLException {
		final Properties properties = new Properties();
		try {
			properties.load(this.getClass().getResourceAsStream(connectionsSpec));
		} catch (Exception e1) {
			throw new RuntimeException("Failed to open connection properties file; path = " + connectionsSpec);
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
		String connectionStr = host + " " + db + " " + user;
		System.out.println("ConnectionStr is " + connectionStr);
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
			s.executeQuery(testQuery);
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
	
	public String listIntegerMembers(Set<Integer> s){
		StringBuilder b = new StringBuilder();
		b.append("{");
		for(Integer v : s){
			b.append(v.intValue());
			b.append(" ");
		}
		b.append("}");
		return b.toString();
	}

	public Statement getStatement() throws SQLException{
		return connection.createStatement();
	}
	
	public PreparedStatement getPreparedStatement(String sqlStatement) throws SQLException{
		return connection.prepareStatement(sqlStatement);
	}




	
}
