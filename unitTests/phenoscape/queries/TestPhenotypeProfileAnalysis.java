package phenoscape.queries;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.sql.SQLException;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import phenoscape.queries.lib.Utils;

public class TestPhenotypeProfileAnalysis {

	PhenotypeProfileAnalysis testAnalysis;
	Utils u = new Utils();
	StringWriter testWriter1;
	StringWriter testWriter2;
	StringWriter testWriter3;
	StringWriter testWriter4;
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();   //prevent complaints by log4j
		u.openKB();
		testAnalysis = new PhenotypeProfileAnalysis(u);
	}


	@Test
	public void testCalcMaxIC() {
		fail("Not yet implemented");
	}

	@Test
	public void testCalcICCS() {
		fail("Not yet implemented");
	}

	@Test
	public void testProcessTaxonVariation() throws SQLException {
		String taxonomyRoot = "TTO:105426"; 
		TaxonomyTree t = new TaxonomyTree(taxonomyRoot,u);
		t.traverseOntologyTree(u);
		testAnalysis.processTaxonVariation(t,u, testWriter1);
	}

	@Test
	public void testTraverseTaxonomy() {
		fail("Not yet implemented");
	}

	@Test
	public void testProcessGeneExpression() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuildTaxonEntityParents() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuildGeneEntityParents() {
		fail("Not yet implemented");
	}

	@Test
	public void testFillCountTable() {
		fail("Not yet implemented");
	}

	@Test
	public void testSumCountTables() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuildPhenotypeMatchCache() {
		fail("Not yet implemented");
	}

	@Test
	public void testWritePhenotypeMatchSummary() {
		
		fail("Not yet implemented");
	}

	
	@After
	public void tearDown() throws Exception {
		u.closeKB();
	}

}
