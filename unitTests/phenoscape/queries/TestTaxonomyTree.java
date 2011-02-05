package phenoscape.queries;

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import phenoscape.queries.lib.Utils;

public class TestTaxonomyTree {

	
	Utils u;
	private static final String OSTARIOPHYSIROOT = "TTO:302";
	private static final String BROCHISROOT = "TTO:103621";
	private static final String ASPIDORASROOT = "TTO:105426";
	
	private int aspidorasrootnode;
	private int ostariophysirootnode;

	private static final String ROOTQUERY = "SELECT n.node_id,simple_label(n.node_id),t.rank_label FROM node AS n " +
	"JOIN taxon AS t ON (t.node_id = n.node_id) "+
	"WHERE (n.uid = ?)";

	TaxonomyTree t1;
	TaxonomyTree t2;
	
	@Before
	public void setUp() throws Exception {
		u = new Utils();
		u.openKB();
		final PreparedStatement p = u.getPreparedStatement(ROOTQUERY);
		p.setString(1,ASPIDORASROOT);
		ResultSet r = p.executeQuery();
		if(r.next()){
			aspidorasrootnode = r.getInt(1);
		}
		p.setString(1,OSTARIOPHYSIROOT);
		r = p.executeQuery();
		if(r.next()){
			ostariophysirootnode = r.getInt(1);
		}
		t1 = new TaxonomyTree(ASPIDORASROOT,u);
		t1.traverseOntologyTree(u);
		//t2 = new TaxonomyTree(OSTARIOPHYSIROOT,u);
	}

	@Test
	public void testTaxonomyTree() {
		TaxonomyTree t = new TaxonomyTree(ASPIDORASROOT,u);
		Assert.assertNotNull(t);
	}

	@Test
	public void testGetRootNodeID() {
		Integer rootNode = t1.getRootNodeID();
		Assert.assertEquals(aspidorasrootnode, rootNode.intValue());
	}

	@Test
	public void testGetAllTaxa() {
		Set <Integer> t1Taxa = t1.getAllTaxa();
		Assert.assertEquals(23, t1Taxa.size());
	}

	@Test
	public void testGetTable() {
		Map<Integer,List<Integer>> table = t1.getTable();
		Assert.assertNotNull(table);
		Assert.assertEquals(23,table.keySet().size());
		Assert.assertEquals(true,table.keySet().contains(t1.getRootNodeID()));
		Assert.assertEquals(22,table.get(t1.getRootNodeID()).size());
	}

	@Test
	public void testNodeIsInternal() {
		Assert.assertEquals(true,t1.nodeIsInternal(t1.getRootNodeID(), u));
	}


	@Test
	public void testTraverseOntologyTree() throws SQLException {
		TaxonomyTree t = new TaxonomyTree(BROCHISROOT,u);
		t.traverseOntologyTree(u);
	}

	@Test
	public void testReport() {
		t1.report(u, null);
	}

}
