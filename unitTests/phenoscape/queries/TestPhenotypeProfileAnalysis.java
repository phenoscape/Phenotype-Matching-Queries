package phenoscape.queries;

import static org.junit.Assert.*;


import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import phenoscape.queries.PhenotypeProfileAnalysis.TaxonPhenotypeLink;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.Utils;

public class TestPhenotypeProfileAnalysis {

	private static final String OSTARIOCLUPEOMORPHAROOT = "TTO:253";	
	private static final String UNITTESTKB = "unitTestconnection.properties"; 

	
	PhenotypeProfileAnalysis testAnalysis;
	Utils u = new Utils();
	StringWriter testWriter1;
	StringWriter testWriter2;
	StringWriter testWriter3;
	StringWriter testWriter4;
	Map<Integer,Integer> attMap;
	int nodeIDofQuality;
	Map<Integer,Integer> badQualities;
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();   //prevent complaints by log4j
		u.openKBFromConnections(UNITTESTKB);
		testAnalysis = new PhenotypeProfileAnalysis(u);
		attMap = u.setupAttributes();
		nodeIDofQuality = u.getQualityNodeID();
		badQualities = new HashMap<Integer,Integer>();
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
		String taxonomyRoot = OSTARIOCLUPEOMORPHAROOT; 
		TaxonomyTree t = new TaxonomyTree(taxonomyRoot,u);
		t.traverseOntologyTree(u);
		Map<Integer,Collection<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
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
