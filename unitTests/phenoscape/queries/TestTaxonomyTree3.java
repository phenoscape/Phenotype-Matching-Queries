package phenoscape.queries;

import static org.junit.Assert.*;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import phenoscape.queries.TaxonomyTree.TaxonomicNode;
import phenoscape.queries.lib.Utils;

public class TestTaxonomyTree3 {

	Utils u;
	TaxonomicNode rt;
	TaxonomicNode f1;
	TaxonomicNode f2;
	TaxonomicNode g1;
	TaxonomicNode g2;
	TaxonomicNode g3;
	TaxonomicNode sp1;
	TaxonomicNode sp2;
	TaxonomicNode sp3;
	TaxonomicNode sp4;
	TaxonomicNode sp5;
	TaxonomicNode sp6;
	TaxonomicNode sp7;
	Map<TaxonomicNode,Integer> testTable;

	
	@Before
	public void setUp() throws Exception {
		u = new Utils();
		rt = new TaxonomicNode(1, "NT:0000001", "Order1", false,"order");
		f1 = new TaxonomicNode(2, "NT:0000002", "Family1", false,"family");
		f2 = new TaxonomicNode(3, "NT:0000003", "Family2", false,"family");
		g1 = new TaxonomicNode(4, "NT:0000004", "Genus1", false, "genus");
		g2 = new TaxonomicNode(5, "NT:0000005", "Genus2", false, "genus");
		g3 = new TaxonomicNode(6, "NT:0000006", "Genus3", false, "genus");
		sp1 = new TaxonomicNode(7, "NT:0000007", "Species1", false, "species");
		sp2 = new TaxonomicNode(8, "NT:0000008", "Species2", false, "species");
		sp3 = new TaxonomicNode(9, "NT:0000009", "Species3", false, "species");
		sp4 = new TaxonomicNode(10, "NT:0000010", "Species4", false, "species");
		sp5 = new TaxonomicNode(11, "NT:0000011", "Species5", false, "species");
		sp6 = new TaxonomicNode(12, "NT:0000012", "Species6", false, "species");
		sp7 = new TaxonomicNode(13, "NT:0000013", "Species7", false, "species");
		testTable = new HashMap<TaxonomicNode,Integer>();
		testTable.put(f1,1);
		testTable.put(f2,1);
		testTable.put(g1,2);
		testTable.put(g2,2);
		testTable.put(g3,3);
		testTable.put(sp1,4);
		testTable.put(sp2,4);
		testTable.put(sp3,4);
		testTable.put(sp4,5);
		testTable.put(sp5,5);
		testTable.put(sp6,6);
		testTable.put(sp7,6);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTaxonomyTree() {
		TaxonomyTree t1 = new TaxonomyTree(rt,u);
		assertNotNull(t1);
		t1.traverseOntologyTreeUsingTaxonNodes(testTable,u);
		t1.report(u,null);
	}

	@Test
	public void testTaxonomyTreeTaxonomicNodeUtils() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRootNodeID() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTable() {
		fail("Not yet implemented");
	}

	@Test
	public void testNodeIsInternal() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAllTaxa() {
		fail("Not yet implemented");
	}

	@Test
	public void testTraverseOntologyTreeUsingTaxonNodes() {
		fail("Not yet implemented");
	}

	@Test
	public void testTraverseOntologyTree() {
		fail("Not yet implemented");
	}

	@Test
	public void testReport() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRootFromKB() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetChildNodesFromKB() {
		fail("Not yet implemented");
	}

}
