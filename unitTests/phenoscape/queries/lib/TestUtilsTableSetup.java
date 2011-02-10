package phenoscape.queries.lib;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestUtilsTableSetup {

	
	Utils u;

	@Before
	public void setUp() throws Exception {
		u = new Utils();
		u.openKB();
	}

	
	final String testStatementStr = "SELECT node.label FROM node WHERE node.uid = 'OBO_REL:is_a'";
	@Test
	public void testGetStatement() throws SQLException {
		Statement s = u.getStatement();
		ResultSet testResults = s.executeQuery(testStatementStr);
		Assert.assertTrue(testResults.next());
		String nodeLabel = testResults.getString(1);
		Assert.assertEquals("is_a", nodeLabel);
	}

	final String testPreparedStatementStr = "SELECT node.uid FROM node WHERE node.label = ?";
	@Test
	public void testGetPreparedStatement() throws SQLException {
		PreparedStatement p = u.getPreparedStatement(testPreparedStatementStr);
		Assert.assertNotNull(p);
		p.setString(1, "is_a");
		ResultSet testResults = p.executeQuery();
		Assert.assertTrue(testResults.next());
		String nodeUID = testResults.getString(1);
		Assert.assertEquals("OBO_REL:is_a", nodeUID);
	}



	final String qualityNameTest = "SELECT node.name FROM node WHERE node.node_id = ?";
	@Test
	public void testGetQualityNodeID() throws SQLException {
		int qualityID = u.getQualityNodeID();
		PreparedStatement p = u.getPreparedStatement(qualityNameTest);
		p.setInt(1,qualityID);
		ResultSet testResult = p.executeQuery();
		Assert.assertEquals("quality",testResult.getString(1));
	}

	@Test
	public void testCacheEntities() throws SQLException {
		u.cacheEntities();
		
	}
	
	final String nameToIDQuery = "SELECT node.node_id FROM node WHERE node.label = ?";
	@Test
	public void testSetupAttributes() throws SQLException {
		Map<Integer, Integer> attributeMap = u.setupAttributes();
		PreparedStatement p = u.getPreparedStatement(nameToIDQuery);
		Assert.assertNotNull(attributeMap);
		
		p.setString(1, "structure");
		ResultSet testResult = p.executeQuery();
		Assert.assertTrue(testResult.next());
		int structureID = testResult.getInt(1);
		
		p.setString(1, "composition");
		testResult = p.executeQuery();
		Assert.assertTrue(testResult.next());
		int compositionID = testResult.getInt(1);
		
		
		p.setString(1,"ossified");
		testResult = p.executeQuery();
		Assert.assertTrue(testResult.next());
		int nodeID = testResult.getInt(1);
		Assert.assertTrue(attributeMap.containsKey(nodeID));
		Assert.assertEquals(attributeMap.get(nodeID).intValue(),compositionID);
		Assert.assertFalse(attributeMap.get(nodeID).intValue()==structureID);

		p.setString(1,"cartilaginous");
		testResult = p.executeQuery();
		Assert.assertTrue(testResult.next());
		nodeID = testResult.getInt(1);
		Assert.assertTrue(attributeMap.containsKey(nodeID));
		Assert.assertEquals(attributeMap.get(nodeID).intValue(),compositionID);
		Assert.assertFalse(attributeMap.get(nodeID).intValue()==structureID);

		p.setString(1,"fused with");
		testResult = p.executeQuery();
		Assert.assertTrue(testResult.next());
		nodeID = testResult.getInt(1);
		Assert.assertTrue(attributeMap.containsKey(nodeID));
		Assert.assertEquals(attributeMap.get(nodeID).intValue(),structureID);
		Assert.assertFalse(attributeMap.get(nodeID).intValue()==compositionID);
	}
	
	@After
	public void tearDown() throws Exception {
		u.closeKB();
	}

}
