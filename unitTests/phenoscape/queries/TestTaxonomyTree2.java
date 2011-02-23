package phenoscape.queries;

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import phenoscape.queries.TaxonomyTree.TaxonomicNode;
import phenoscape.queries.lib.Utils;

public class TestTaxonomyTree2 {
	
	private static final String OSTARIOPHYSIROOT = "TTO:302";

	Utils u;

	private int ostariophysirootnode;

	private static final String ROOTQUERY = "SELECT n.node_id,simple_label(n.node_id),t.rank_label FROM node AS n " +
	"JOIN taxon AS t ON (t.node_id = n.node_id) "+
	"WHERE (n.uid = ?)";

	TaxonomyTree tree;


	@Before
	public void setUp() throws Exception {
		u = new Utils();
		u.openKB();
		final PreparedStatement p = u.getPreparedStatement(ROOTQUERY);
		p.setString(1,OSTARIOPHYSIROOT);
		ResultSet r = p.executeQuery();
		if(r.next()){
			ostariophysirootnode = r.getInt(1);
		}
		tree = new TaxonomyTree(OSTARIOPHYSIROOT,u);
		tree.traverseOntologyTree(u);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTaxonomyTreeStringUtils() throws SQLException {
		TaxonomyTree t = new TaxonomyTree(OSTARIOPHYSIROOT,u);
		Assert.assertNotNull(t);
	}

	@Test
	public void testTaxonomyTreeTaxonomicNodeUtils() throws SQLException {
		TaxonomicNode n = TaxonomyTree.getRootFromKB(u,OSTARIOPHYSIROOT);
		TaxonomyTree t = new TaxonomyTree(n,u);
		Assert.assertNotNull(t);
	}

	@Test
	public void testGetRootNodeID() {
		Integer rootNode = tree.getRootNodeID();
		Assert.assertEquals(ostariophysirootnode, rootNode.intValue());
	}

	
	@Test
	public void testGetAllTaxa() {
		Set <Integer> treeTaxa = tree.getAllTaxa();
		Assert.assertEquals(11407, treeTaxa.size());
	}

	@Test
	public void testGetTable() {
		Map<Integer,Set<Integer>> table = tree.getTable();
		Assert.assertNotNull(table);
		Assert.assertEquals(11407,table.keySet().size());
		Assert.assertEquals(true,table.keySet().contains(tree.getRootNodeID()));
		Assert.assertEquals(3,table.get(tree.getRootNodeID()).size());
	}

	@Test
	public void testNodeIsInternal() {
		Assert.assertEquals(true,tree.nodeIsInternal(tree.getRootNodeID(), u));
	}

	@Test
	public void testTraverseOntologyTreeUsingTaxonNodes() {
		fail("Not yet implemented");
	}

	@Test
	public void testTraverseOntologyTree() throws SQLException {
		TaxonomyTree t = new TaxonomyTree(OSTARIOPHYSIROOT,u);
		t.traverseOntologyTree(u);
	}

	@Test
	public void testReport() {
		tree.report(u, null);
	}

	@Test
	public void testGetRootFromKB() throws SQLException {
		TaxonomicNode n = TaxonomyTree.getRootFromKB(u, OSTARIOPHYSIROOT);
		assertEquals(n.getUID(),OSTARIOPHYSIROOT);
	}

	@Test
	public void testGetChildNodesFromKB() {
		fail("Not yet implemented");
	}

}
