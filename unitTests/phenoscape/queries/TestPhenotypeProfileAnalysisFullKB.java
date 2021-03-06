package phenoscape.queries;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import phenoscape.queries.lib.DistinctGeneAnnotationRecord;
import phenoscape.queries.lib.PhenotypeExpression;
import phenoscape.queries.lib.ProfileMap;
import phenoscape.queries.lib.TaxonPhenotypeLink;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;

public class TestPhenotypeProfileAnalysisFullKB {

	//private static final String OSTARIOCLUPEOMORPHAROOT = "TTO:253";	
	//private static final String FULLKBCONNECTION = "stagingconnection.properties"; 
	private static final String ASPIDORASALBATERNODESTR = "TTO:1004787";
	private static final String ASPIDORASROOT = "TTO:105426";
	private static final String UNC45BSTR = "unc45b";

	private static final String CONNECTION_PROPERTIES_FILENAME = "connection.properties"; 

	
	PhenotypeProfileAnalysis testAnalysis;
	Utils u = new Utils();
	TaxonomyTree t1;
	StringWriter testWriter1;
	StringWriter testWriter2;
	StringWriter testWriter3;
	StringWriter testWriter4;
	Map<Integer,Integer> attMap;
	int nodeIDofQuality;
	Map<Integer,Integer> badTaxonQualities;
	Map<Integer,Integer> badGeneQualities;
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();   //prevent complaints by log4j
		String kbName;
		kbName = u.openKBFromConnections(CONNECTION_PROPERTIES_FILENAME);
		attMap = u.setupAttributes();
		nodeIDofQuality = u.getQualityNodeID();
		badTaxonQualities = new HashMap<Integer,Integer>();
		badGeneQualities = new HashMap<Integer,Integer>();

		String taxonomyRoot = ASPIDORASROOT; 
		t1 = new TaxonomyTree(taxonomyRoot,u);
		t1.traverseOntologyTree(u);

	}


	
	private static final String NODEQUERY = "SELECT n.node_id FROM node AS n WHERE n.uid = ?";

	@Test
	public void TestGetTaxonPhenotypeLinksFromKB() throws Exception{
		int aspidorasid = -1;
		PreparedStatement p = u.getPreparedStatement(NODEQUERY);
		p.setString(1,ASPIDORASALBATERNODESTR);
		ResultSet r = p.executeQuery();
		if (r.next()){
			aspidorasid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + ASPIDORASALBATERNODESTR);
		}
		Set<TaxonPhenotypeLink> c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, aspidorasid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		
	}
	
	
	@Test
	public void TestGetAllTaxonPhenotypeLinksFromKB() throws Exception{
		Map<Integer,Set<TaxonPhenotypeLink>> links = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1, u);
		assertNotNull(links);
		assertFalse(links.isEmpty());
		for(Integer taxonID : t1.getAllTaxa()){
			assertNotNull(links.get(taxonID));
		}
	}
	


	Collection<TaxonPhenotypeLink> getTaxonPhenotypeLinksFromKB(Utils u, int taxonID) throws SQLException{
		final PreparedStatement p = u.getPreparedStatement(TaxonPhenotypeLink.getQuery());
		final Collection<TaxonPhenotypeLink> result = new HashSet<TaxonPhenotypeLink>();
		p.setInt(1, taxonID);
		ResultSet ts = p.executeQuery();
		while (ts.next()){
			TaxonPhenotypeLink l = new TaxonPhenotypeLink(ts);
			result.add(l);
		}
		return result;
	}

	@Test
	public void testProcessTaxonVariation() throws SQLException {
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
		assertFalse(taxonProfiles.isEmpty());
	}

	@Test
	public void testGetAllGeneAnnotationsFromKB() throws SQLException {
		Collection<DistinctGeneAnnotationRecord> annotations = testAnalysis.getAllGeneAnnotationsFromKB(u);
		System.out.println("Annotation Count = " + annotations.size());
	}
	
	@Test
	public void testProcessGeneExpression() {
		fail("Not yet implemented");
	}
	
	
	@Test
	public void testBuildEQParent() throws SQLException {
		int unc45bid = -1;
		PreparedStatement p = u.getPreparedStatement(NODEQUERY);
		p.setString(1,UNC45BSTR);
		ResultSet r = p.executeQuery();
		if (r.next()){
			unc45bid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + UNC45BSTR);
		}

		testAnalysis.qualitySubsumers = u.buildPhenotypeSubsumers();
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		testAnalysis.removeSymmetricLinks(allLinks,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.domainSize()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		Map <Integer,Set<Integer>> entityParentCache = new HashMap<Integer,Set<Integer>>();
		Map <Integer,Set<Integer>> entityChildCache = new HashMap<Integer,Set<Integer>>();
		u.setupEntityParents(entityParentCache,entityChildCache);
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		PhenotypeExpression curEQ = null;
		testAnalysis.buildEQParent(phenotypeParentCache,curEQ,entityParentCache,u);
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
	
	@Test
	public void testCalcMaxIC() {
		fail("Not yet implemented");
	}

	@Test
	public void testCalcICCS() {
		fail("Not yet implemented");
	}

	
	@After
	public void tearDown() throws Exception {
		u.closeKB();
	}

}
