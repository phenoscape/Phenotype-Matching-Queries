package phenoscape.queries;

import static org.junit.Assert.*;


import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import phenoscape.queries.lib.CountTable;
import phenoscape.queries.lib.DistinctGeneAnnotationRecord;
import phenoscape.queries.lib.PhenotypeExpression;
import phenoscape.queries.lib.PhenotypeScoreTable;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.ProfileMap;
import phenoscape.queries.lib.TaxonPhenotypeLink;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;

public class TestPhenotypeProfileAnalysis {

	private static final String UNITTESTROOT = "TTO:0000015";	
	private static final String UNITTESTKB = "unitTestconnection.properties"; 
	private static final String TAXON1STR = "TTO:0000001";
	private static final String TAXON2STR = "TTO:0000002";
	private static final String TAXON3STR = "TTO:0000003";
	private static final String TAXON4STR = "TTO:0000004";
	private static final String TAXON5STR = "TTO:0000005";
	private static final String TAXON6STR = "TTO:0000006";
	private static final String TAXON7STR = "TTO:0000007";
	private static final String TAXON8STR = "TTO:0000008";
	private static final String TAXON9STR = "TTO:0000009";
	private static final String TAXON10STR = "TTO:0000010";
	private static final String TAXON11STR = "TTO:0000011";
	private static final String TAXON12STR = "TTO:0000012";
	private static final String TAXON13STR = "TTO:0000013";
	private static final String TAXON14STR = "TTO:0000014";

	
	PhenotypeProfileAnalysis testAnalysis;
	Utils u = new Utils();
	StringWriter testWriter1;
	StringWriter testWriter2;
	StringWriter testWriter3;
	StringWriter testWriter4;
	Map<Integer,Integer> attMap;
	TaxonomyTree t1;
	int nodeIDofQuality;
	Map<Integer,Integer> badQualities;
	Map<Integer,Integer> badTaxonQualities;

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();   //prevent complaints by log4j
		u.openKBFromConnections(UNITTESTKB);
		testAnalysis = new PhenotypeProfileAnalysis(u);
		attMap = u.setupAttributes();
		nodeIDofQuality = u.getQualityNodeID();
		testAnalysis.attributeMap = u.setupAttributes();   // this is icky
		
		PhenotypeExpression.getEQTop(u);   //just to initialize early.

		testAnalysis.attributeSet.addAll(testAnalysis.attributeMap.values());		
		testAnalysis.attributeSet.add(nodeIDofQuality);

		badQualities = new HashMap<Integer,Integer>();
		String taxonomyRoot = UNITTESTROOT; 
		t1 = new TaxonomyTree(taxonomyRoot,u);
		t1.traverseOntologyTree(u);

	}

	private static final String NODEQUERY = "SELECT n.node_id FROM node AS n WHERE n.uid = ?";

	@Test
	public void TestGetTaxonPhenotypeLinksFromKB() throws Exception{
		int taxonid = -1;
		ResultSet r;
		Collection<TaxonPhenotypeLink> c;
		PreparedStatement p = u.getPreparedStatement(NODEQUERY);
		
		p.setString(1,TAXON1STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON1STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertTrue(c.isEmpty());

		p.setString(1,TAXON2STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON2STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertTrue(c.isEmpty());

		p.setString(1,TAXON3STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON3STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertTrue(c.isEmpty());

		p.setString(1,TAXON4STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON4STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(1,c.size());

		p.setString(1,TAXON5STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON5STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(2,c.size());

		p.setString(1,TAXON6STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON6STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(2,c.size());

		p.setString(1,TAXON7STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON7STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertTrue(c.isEmpty());

		p.setString(1,TAXON8STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON8STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertTrue(c.isEmpty());

		p.setString(1,TAXON9STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON9STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertTrue(c.isEmpty());

		p.setString(1,TAXON10STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON10STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(1,c.size());

		p.setString(1,TAXON11STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON11STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(1,c.size());

		p.setString(1,TAXON12STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON12STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(1,c.size());

		p.setString(1,TAXON13STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON13STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(1,c.size());
		
		p.setString(1,TAXON14STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON14STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(1,c.size());
		
	}
	
	
	@Test
	public void TestGetAllTaxonPhenotypeLinksFromKB() throws Exception{
		Map<Integer,Set<TaxonPhenotypeLink>> links = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1, u);
		assertNotNull(links);
		Assert.assertEquals(15,links.size());   //this is just the number of taxa in the KB
		for(Integer taxonID : t1.getAllTaxa()){
			assertNotNull(links.get(taxonID));
		}
	}
	
	@Test
	public void testLoadTaxonProfiles() throws SQLException{
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);		
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15, taxonProfiles.domainSize());  //again, should be equal to the number of taxa
	}
	
	
	@Test
	public void testTraverseTaxonomy() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		Assert.assertEquals(1,taxonVariation.getUsedEntities().size());  //This should be {'opercle'}
		Iterator<Integer> e_Itr = taxonVariation.getUsedEntities().iterator();
		assertTrue(e_Itr.hasNext());
		Integer entity = e_Itr.next();
		Assert.assertEquals("opercle", u.getNodeName(entity.intValue()));
		Assert.assertEquals(1,taxonVariation.getUsedAttributes().size());  //This should be {'shape'}
		Iterator<Integer> a_Itr = taxonVariation.getUsedAttributes().iterator();
		assertTrue(a_Itr.hasNext());
		Integer att = a_Itr.next();
		Assert.assertEquals("shape", u.getNodeName(att.intValue()));
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.domainSize()); //The taxonVariation table 'knows' where the variation is, but profiles not updated yet
	}

	
	@Test
	public void testFlushUnvaryingPhenotypes() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.domainSize()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(3,taxonProfiles.domainSize()); //profiles has now been trimmed to only those taxa with variation
		Set<String>taxonUIDs = new HashSet<String>();
		for (Integer taxon : taxonProfiles.domainSet()){ 
			taxonUIDs.add(u.getNodeUID(taxon));
			Profile curProfile = taxonProfiles.getProfile(taxon);
			curProfile.getUsedEntities();
			Iterator<Integer> e_Itr = curProfile.getUsedEntities().iterator();
			assertTrue(e_Itr.hasNext());
			Integer ent = e_Itr.next();
			Assert.assertEquals("opercle", u.getNodeName(ent.intValue()));
			Iterator<Integer> a_Itr = curProfile.getUsedAttributes().iterator();
			assertTrue(a_Itr.hasNext());
			Integer att = a_Itr.next();
			Assert.assertEquals("shape", u.getNodeName(att.intValue()));
		}
	}

	@Test
	public void testGetAllGeneAnnotationsFromKB() throws SQLException {
		Collection<DistinctGeneAnnotationRecord> annotations = testAnalysis.getAllGeneAnnotationsFromKB(u);
		System.out.println("Annotation Count = " + annotations.size());
	}

	@Test
	public void testProcessGeneExpression() throws SQLException {
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		testAnalysis.processGeneExpression(geneVariation, u, null);
		Set<Integer> genes = new HashSet<Integer>();
		for(Integer att : geneVariation.getUsedAttributes()){
			for (Integer ent : geneVariation.getUsedEntities()){
				if (geneVariation.hasExhibitorSet(ent,att)){					
					genes.addAll(geneVariation.getExhibitorSet(ent,att));
				}
			}
		}
		System.out.println("Genes");
		for (Integer gene : genes){
			System.out.println(u.getNodeName(gene));
		}
	}

	@Test
	public void testBuildEQParents() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.domainSize()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		Map <Integer,Set<Integer>> entityParentCache = new HashMap<Integer,Set<Integer>>();
		Map <Integer,Set<Integer>> entityChildCache = new HashMap<Integer,Set<Integer>>();
		u.setupEntityParents(entityParentCache,entityChildCache);
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
//		for(PhenotypeExpression pe : phenotypeParentCache.keySet()){
//			pe.fillNames(u);
//			System.out.println("Expression is " + pe);
//			for (PhenotypeExpression peParent : phenotypeParentCache.get(pe)){
//				peParent.fillNames(u);
//				System.out.println("  Parent is " + peParent);
//			}
//		}
	}


	@Test
	public void testFillCountTable() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		ProfileMap geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		Map <Integer,Set<Integer>> entityParentCache = new HashMap<Integer,Set<Integer>>();
		Map <Integer,Set<Integer>> entityChildCache = new HashMap<Integer,Set<Integer>>();
		u.setupEntityParents(entityParentCache,entityChildCache);
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		CountTable <PhenotypeExpression> counts = testAnalysis.fillPhenotypeCountTable(geneProfiles, taxonProfiles, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		for(PhenotypeExpression p : counts.getPhenotypes()){
			p.fillNames(u);
			System.out.println("Phenotype: " + p.getFullName(u) + " count: " + counts.getRawCount(p));
		}
	}


	@Test
	public void testBuildPhenotypeMatchCache() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		testAnalysis.taxonProfiles= taxonProfiles;
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.domainSize()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		ProfileMap geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		testAnalysis.geneProfiles= geneProfiles;
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		Map <Integer,Set<Integer>> entityParentCache = new HashMap<Integer,Set<Integer>>();
		Map <Integer,Set<Integer>> entityChildCache = new HashMap<Integer,Set<Integer>>();
		u.setupEntityParents(entityParentCache,entityChildCache);
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		CountTable<PhenotypeExpression> counts = testAnalysis.fillPhenotypeCountTable(geneProfiles, taxonProfiles, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);
	}

	@Test
	public void testWritePhenotypeMatchSummary() {
		
		fail("Not yet implemented");
	}

	@Test
	public void testFillUnionSets() throws SQLException{
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		testAnalysis.taxonProfiles= taxonProfiles;
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.domainSize()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		ProfileMap geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		testAnalysis.geneProfiles= geneProfiles;
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		Map <Integer,Set<Integer>> entityParentCache = new HashMap<Integer,Set<Integer>>();
		Map <Integer,Set<Integer>> entityChildCache = new HashMap<Integer,Set<Integer>>();
		u.setupEntityParents(entityParentCache,entityChildCache);
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		CountTable<PhenotypeExpression> counts = testAnalysis.fillPhenotypeCountTable(geneProfiles, taxonProfiles, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);
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
