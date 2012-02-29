package phenoscape.queries;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import phenoscape.queries.TaxonomyTree.TaxonomicNode;
import phenoscape.queries.lib.Utils;

public class TestTaxonomyTree {

	
	Utils u;
	private static final String UNITTESTKB = "unitTestconnection.properties"; 

	private static final String UNITTESTKBROOTSTR = "TTO:0000015";
	private static final String UNITTESTORDERSTR = "TTO:0000001";
	private int unittestrootnode;

	private static final String ROOTQUERY = "SELECT n.node_id,simple_label(n.node_id),t.rank_label FROM node AS n " +
	"JOIN taxon AS t ON (t.node_id = n.node_id) "+
	"WHERE (n.uid = ?)";

	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();   //prevent complaints by log4j
		u = new Utils();
		u.openKBFromConnections(UNITTESTKB);
		final PreparedStatement p = u.getPreparedStatement(ROOTQUERY);
		p.setString(1,UNITTESTKBROOTSTR);
		ResultSet r = p.executeQuery();
		if(r.next()){
			unittestrootnode = r.getInt(1);
		}
	}

	@Test
	public void testTaxonomyTree() throws SQLException {
		TaxonomyTree t = new TaxonomyTree(UNITTESTKBROOTSTR,u);
		Assert.assertNotNull(t);
	}

	@Test
	public void testTaxonomyTree_fromNode() throws SQLException {
		TaxonomicNode n = TaxonomyTree.getRootFromKB(u,UNITTESTORDERSTR);
		TaxonomyTree t = new TaxonomyTree(n,u);
		Assert.assertNotNull(t);
	}

	
	
	@Test
	public void testGetRootNodeID() throws SQLException {
		TaxonomyTree t = new TaxonomyTree(UNITTESTKBROOTSTR,u);
		Integer rootNode = t.getRootNodeID();
		Assert.assertEquals(unittestrootnode, rootNode.intValue());
	}
	
	@Test
	public void testTraverseOntologyTree() throws SQLException {
		TaxonomyTree t = new TaxonomyTree(UNITTESTKBROOTSTR,u);
		t.traverseOntologyTree(u);
	}


	@Test
	public void testGetAllTaxa() throws SQLException {
		TaxonomyTree t = new TaxonomyTree(UNITTESTKBROOTSTR,u);
		t.traverseOntologyTree(u);
		Set <Integer> t1Taxa = t.getAllTaxa();
		Assert.assertEquals(15, t1Taxa.size());
	}

	@Test
	public void testGetTable() throws SQLException {
		TaxonomyTree t = new TaxonomyTree(UNITTESTKBROOTSTR,u);
		t.traverseOntologyTree(u);
		Map<Integer,Set<Integer>> table = t.getTable();
		Assert.assertNotNull(table);
		Assert.assertEquals(15,table.keySet().size());
		Assert.assertEquals(true,table.keySet().contains(t.getRootNodeID()));
		Assert.assertEquals(2,table.get(t.getRootNodeID()).size());
	}

	@Test
	public void testNodeIsInternal() throws SQLException {
		TaxonomyTree t = new TaxonomyTree(UNITTESTKBROOTSTR,u);
		t.traverseOntologyTree(u);
		Assert.assertEquals(true,t.nodeIsInternal(t.getRootNodeID(), u));
	}

	@Test
	public void testReport() throws SQLException {
		TaxonomyTree t = new TaxonomyTree(UNITTESTKBROOTSTR,u);
		t.traverseOntologyTree(u);
		t.report(u, null);
	}

}
