package phenoscape.queries.lib;

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestUtils {

	Utils u;
	private static final String UNITTESTKB = "unitTestconnection.properties"; 

	
	@Before
	public void setUp() throws Exception {
		u = new Utils();
		//u.openKBFromConnections(UNITTESTKB);
		u.openKB();
		//u.cacheEntities();
	}

	@Test
	public void testOpenKB() throws SQLException{
		String kbStr = u.openKB();
		System.out.println(kbStr);
		u.closeKB();
		kbStr = u.openKBFromConnections(UNITTESTKB);
		System.out.println(kbStr);
		u.closeKB();
	}
	
	
	final String opercleLookup = "SELECT node.node_id FROM node WHERE node.label = 'opercle'";
	final String headLookup = "SELECT node.node_id FROM node WHERE node.label = 'head'";

	@Test
	public void testSetupEntityParents() throws SQLException{
		Map <Integer,Set<Integer>> entParents = new HashMap<Integer,Set<Integer>>();
		Map <Integer,Set<Integer>> entChildren = new HashMap<Integer,Set<Integer>>();
		u.setupEntityParents(entParents,entChildren);
		assertNotNull(entParents);
		assertFalse(entParents.isEmpty());
		System.out.println("Entity parents size = " + entParents.size());
		
		Statement s = u.getStatement();
		int opercleNodeID = -1;
		ResultSet rs = s.executeQuery(opercleLookup);
		if (rs.next())
			opercleNodeID = rs.getInt(1);
		else{
			fail("Couldn't retreive node id for 'opercle'");
		}
		Set <Integer> opercleParents = entParents.get(opercleNodeID);
		assertNotNull(opercleParents);
		assertFalse(opercleParents.isEmpty());
		System.out.println("Opercle parents = " + u.listIntegerMembers(opercleParents));
		for(Integer i : opercleParents){
			u.cacheOneNode(i);
			System.out.println("Parent: " + u.getNodeName(i));
		}
		Set <Integer> opercleChildren = entChildren.get(opercleNodeID);
		assertNotNull(opercleChildren);
		assertFalse(opercleChildren.isEmpty());
		System.out.println("Opercle Children = " + u.listIntegerMembers(opercleChildren));
		for(Integer i : opercleChildren){
			u.cacheOneNode(i);
			System.out.println("Child: " + u.getNodeName(i));
		}
		s = u.getStatement();
		int headNodeID = -1;
		rs = s.executeQuery(headLookup);
		if (rs.next())
			headNodeID = rs.getInt(1);
		else{
			fail("Couldn't retreive node id for 'dermal bone'");
		}
		Set <Integer> headParents = entParents.get(headNodeID);
//		assertNotNull(headParents);
//		assertFalse(headParents.isEmpty());
//		System.out.println("Dermal Bone parents = " + u.listIntegerMembers(headParents));
//		for(Integer i : headParents){
//			u.cacheOneNode(i);
//			System.out.println("Parent: " + u.getNodeName(i));
//		}
		Set <Integer> headChildren = entChildren.get(headNodeID);
		assertNotNull(headChildren);
		assertFalse(headChildren.isEmpty());
		System.out.println("Dermal Bone Children = " + u.listIntegerMembers(headChildren));
		for(Integer i : headChildren){
			u.cacheOneNode(i);
			System.out.println("Child: " + u.getNodeName(i));
		}

	}
	
	
	String shapeLookup = "SELECT node.node_id FROM node WHERE node.label = 'shape'";
	@Test
	public void testCollectQualityParents() throws SQLException{
		Statement s = u.getStatement();
		int shapeNodeID = -1;
		ResultSet rs = s.executeQuery(shapeLookup);
		if (rs.next())
			shapeNodeID = rs.getInt(1);
		else{
			fail("Couldn't retreive node id for 'shape'");
		}
		Set <Integer> shapeParents = u.collectQualityParents(shapeNodeID);
		assertFalse(shapeParents.size() == 0);
	}

	
	
	private final static String NAMELOOKUP = "SELECT node.node_id FROM node WHERE node.uid = ?";
	@Test
	public void testSetupAttributes() throws SQLException{
		PreparedStatement p = u.getPreparedStatement(NAMELOOKUP);
		Map<Integer,Integer> attMap = u.setupAttributes();
		int compositionID;
		p.setString(1, "PATO:0000025");
		ResultSet lookupResults = p.executeQuery();
		if (lookupResults.next()){
			compositionID = lookupResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of 'composition' failed");
		int structureID;
		p.setString(1, "PATO:0000141");
		lookupResults = p.executeQuery();
		if (lookupResults.next()){
			structureID = lookupResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of 'structure' failed");
		int nodeID;
		p.setString(1, "PATO:0001448");
		lookupResults = p.executeQuery();
		if (lookupResults.next()){
			nodeID = lookupResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of PATO:0001448 (ossified) failed");
		System.out.println("Composition id = " + compositionID + "; structure id = " + structureID);
		assertEquals(compositionID,attMap.get(nodeID).intValue());
		p.setString(1, "PATO:0001449");
		lookupResults = p.executeQuery();
		if (lookupResults.next()){
			nodeID = lookupResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of PATO:0001449 (cartilaginous) failed");
		assertEquals(compositionID,attMap.get(nodeID).intValue());
		p.setString(1, "PATO:0000025");
		lookupResults = p.executeQuery();
		if (lookupResults.next()){
			nodeID = lookupResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of PATO:0000025 (composition) failed");
		assertEquals(compositionID,attMap.get(nodeID).intValue());
		p.setString(1, "PATO:0001447");
		lookupResults = p.executeQuery();
		if (lookupResults.next()){
			nodeID = lookupResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of PATO:0001447 (calcified) failed");
		assertEquals(compositionID,attMap.get(nodeID).intValue());
		p.setString(1, "PATO:0001450");
		lookupResults = p.executeQuery();
		if (lookupResults.next()){
			nodeID = lookupResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of PATO:0001450 (edematous) failed");
		assertEquals(compositionID,attMap.get(nodeID).intValue());
		p.setString(1, "PATO:0001759");
		lookupResults = p.executeQuery();
		if (lookupResults.next()){
			nodeID = lookupResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of PATO:0001759 (granular) failed");
		assertEquals(compositionID,attMap.get(nodeID).intValue());
		p.setString(1, "PATO:0001853");
		lookupResults = p.executeQuery();
		if (lookupResults.next()){
			nodeID = lookupResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of PATO:0001853 (hydrocephalic) failed");
		assertEquals(compositionID,attMap.get(nodeID).intValue());
		p.setString(1, "PATO:0002104");
		lookupResults = p.executeQuery();
		if (lookupResults.next()){
			nodeID = lookupResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of PATO:0002104 (inflammatory) failed");
		assertEquals(compositionID,attMap.get(nodeID).intValue());

	}
	
	@Test
	public void testSetupReciprocals() throws SQLException{
		PreparedStatement p = u.getPreparedStatement(NAMELOOKUP);
		Map<Integer,Integer> testMap = u.setupReciprocals();
		assertNotNull(testMap);
		assertFalse(testMap.isEmpty());
		p.setString(1, "PATO:0001555");
		int preferredID;
		ResultSet lookupResults = p.executeQuery();
		if (lookupResults.next()){
			preferredID = lookupResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of PATO:0001555 (has number of) failed");
		p.setString(1, "PATO:0000070");
		int legacyID;
		lookupResults = p.executeQuery();
		if (lookupResults.next()){
			legacyID = lookupResults.getInt(1);
		}
		else 
			throw new RuntimeException("Query for node id of PATO:0000070 (count) failed");
		assertTrue(testMap.containsKey(legacyID));
		assertEquals(preferredID,testMap.get(legacyID).intValue());
	}
	

	@Test
	public void testHasNodeName() throws SQLException {
		Statement s = u.getStatement();
		int opercleNodeID = -1;
		ResultSet rs = s.executeQuery(opercleLookup);
		if (rs.next())
			opercleNodeID = rs.getInt(1);
		else{
			fail("Couldn't retreive node id for 'opercle'");
		}
		assertFalse(u.hasNodeName(opercleNodeID));
		u.cacheOneNode(opercleNodeID);
		assertTrue(u.hasNodeName(opercleNodeID));		
	}


	@Test
	public void testGetNodeName() throws SQLException {
		Statement s = u.getStatement();
		int opercleNodeID = -1;
		ResultSet rs = s.executeQuery(opercleLookup);
		if (rs.next())
			opercleNodeID = rs.getInt(1);
		else{
			fail("Couldn't retreive node id for 'opercle'");
		}
		u.cacheOneNode(opercleNodeID);
		assertEquals("opercle",u.getNodeName(opercleNodeID));
	}

	@Test
	public void testCountDistinctGenePhenotypeAnnotations() throws SQLException {
		 assertEquals(24,u.countDistinctGenePhenotypeAnnotations());
	}
	
	
	@Test
	public void testPutNodeUIDName() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNodeUID() {
		fail("Not yet implemented");
	}

	@Test
	public void testHasNodeUID() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNodeSet() {
		fail("Not yet implemented");
	}

	@Test
	public void testHasNodeIDToName() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddLinkPhenotypePair() {
		fail("Not yet implemented");
	}

	@Test
	public void testHasLinkToPhenotype() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPhenotypeFromLink() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddParent() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetParents() {
		fail("Not yet implemented");
	}

	@Test
	public void testPrintTableReport() {
		fail("Not yet implemented");
	}


	@Test
	public void testWriteOrDump() {
		fail("Not yet implemented");
	}

	@Test
	public void testListIntegerMembers() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testBuildPhenotypeSubsumers() throws SQLException{
		Map<Integer,Set<Integer>> testmap = u.buildPhenotypeSubsumers();
		assertEquals(28,testmap.size());
		for(Integer p : testmap.keySet()){
			assertFalse(testmap.get(p).isEmpty());
		}
	}
	
	@After
	public void tearDown() throws Exception {
		u.closeKB();
	}


}
