package phenoscape.queries.lib;

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class TestUtils {

	Utils u;
	
	@Before
	public void setUp() throws Exception {
		u = new Utils();
		u.openKB();
		//u.cacheEntities();
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
	public void testGetNodeName() {
		fail("Not yet implemented");
	}

	@Test
	public void testHasNodeName() {
		fail("Not yet implemented");
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

	String scaleLookup = "SELECT node.node_id FROM node WHERE node.label = 'scale'";
	@Test
	public void testCollectEntityParents() throws SQLException{
		Statement s = u.getStatement();
		int scaleNodeID = -1;
		ResultSet rs = s.executeQuery(scaleLookup);
		if (rs.next())
			scaleNodeID = rs.getInt(1);
		else{
			fail("Couldn't retreive node id for 'scale'");
		}
		Set <Integer> scaleParents = u.collectEntityParents(scaleNodeID);
		
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

}
