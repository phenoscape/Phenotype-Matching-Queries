package phenoscape.queries;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import phenoscape.queries.lib.ProfileScoreSet;
import phenoscape.queries.lib.TaxonPhenotypeLink;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;

public class TestPropTree5 {
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

	private final int genePhenotypeAnnotationCount = 23;   // True independent of the taxon data loaded
	private final double IC1 = -1*(Math.log(1.0/(double)genePhenotypeAnnotationCount)/Math.log(2));
	private final double IC3 = -1*(Math.log(3.0/(double)genePhenotypeAnnotationCount)/Math.log(2));
	private final double IC4 = -1*(Math.log(4.0/(double)genePhenotypeAnnotationCount)/Math.log(2));
	private final double IC12 = -1*(Math.log(12.0/(double)genePhenotypeAnnotationCount)/Math.log(2));
	private final double IC13 = -1*(Math.log(13.0/(double)genePhenotypeAnnotationCount)/Math.log(2));


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
	CountTableCheck countTableCheck;

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
		Assert.assertEquals(2,c.size());

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
		Assert.assertEquals(7,c.size());

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
		Assert.assertEquals(6,c.size());

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
		Assert.assertEquals(6,c.size());

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
		Assert.assertEquals(6,c.size());

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
		Assert.assertEquals(6,c.size());

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
		Assert.assertEquals(5,c.size());

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
		Assert.assertEquals(5,c.size());

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
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);		
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15, taxonProfiles.size());  //again, should be equal to the number of taxa
	}

	final List<String>entityNames= Arrays.asList("body","opercle","pectoral fin","posterior region of frontal bone","process of anterior region of dentary","vertebra");
	final List<String>attNames= Arrays.asList("optical quality","shape","size","count");
	@Test
	public void testTraverseTaxonomy() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		Assert.assertEquals("Count of entities used",6,taxonVariation.getUsedEntities().size()); 
		for (Integer entity : taxonVariation.getUsedEntities()){
			Assert.assertTrue("Found unexpected entity " + u.getNodeName(entity.intValue()),entityNames.contains(u.getNodeName(entity.intValue())));
		}
		Assert.assertEquals("Count of qualities used",4,taxonVariation.getUsedAttributes().size());  
		for (Integer att : taxonVariation.getUsedAttributes()){
			Assert.assertTrue("Found unexpected attribute " + u.getNodeName(att.intValue()),attNames.contains(u.getNodeName(att.intValue())));
		}
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals("Taxon profiles at end of traverse taxonomy",15,taxonProfiles.size()); //The taxonVariation table 'knows' where the variation is, but profiles not updated yet
	}


	@Test
	public void testFlushUnvaryingPhenotypes() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals("Count of taxa before flush",15,taxonProfiles.size()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals("Count of taxa with variation",5,taxonProfiles.size()); //profiles has now been trimmed to only those taxa with variation
	}

	@Test
	public void testGetAllGeneAnnotationsFromKB() throws SQLException {
		Collection<DistinctGeneAnnotationRecord> annotations = testAnalysis.getAllGeneAnnotationsFromKB(u);
		Assert.assertEquals(23,annotations.size());
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
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.size()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
	}

	@Test
	public void testFillCountTable() throws SQLException {
		CountTableCheck countTableCheck = new CountTableCheck(u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		HashMap<Integer,Profile>geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		CountTable counts = new CountTable();
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		testAnalysis.fillCountTable(geneProfiles, counts, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		for(PhenotypeExpression p : counts.getPhenotypes()){
			p.fillNames(u);
			final String fullName = p.getFullName(u);
			Assert.assertNotNull("Full phenotype name",fullName);
			Assert.assertNotNull(countTableCheck);
			Assert.assertNotNull("Count table does not contain: " + fullName,countTableCheck.hasPhenotype(p));
			Assert.assertNotNull("Raw count of "+ fullName + " is null?",counts.getRawCount(p));
			Assert.assertEquals(countTableCheck.get(p),counts.getRawCount(p));
		}
	}


	@Test
	public void testBuildPhenotypeMatchCache() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		testAnalysis.taxonProfiles= taxonProfiles;
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.size()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		HashMap<Integer,Profile>geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		testAnalysis.geneProfiles= geneProfiles;
		CountTable counts = new CountTable();
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		testAnalysis.fillCountTable(geneProfiles, counts, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);
	}

	@Test
	public void testWritePhenotypeMatchSummary() throws SQLException{
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		testAnalysis.taxonProfiles= taxonProfiles;
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.size()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		HashMap<Integer,Profile>geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		testAnalysis.geneProfiles= geneProfiles;
		CountTable counts = new CountTable();
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		testAnalysis.fillCountTable(geneProfiles, counts, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);
	}


	@Test
	public void testCalcMaxIC() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		testAnalysis.taxonProfiles= taxonProfiles;
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.size()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		HashMap<Integer,Profile>geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		testAnalysis.geneProfiles= geneProfiles;
		CountTable counts = new CountTable();
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		testAnalysis.fillCountTable(geneProfiles, counts, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);
		int order1ID = u.getIDFromName("Order 1");
		int family1ID = u.getIDFromName("Family 1");
		int genus1ID = u.getIDFromName("Genus 1");
		int genus2ID = u.getIDFromName("Genus 2");
		int genus3ID = u.getIDFromName("Genus 3");
		int alfID = u.getIDFromName("alf");
		int apaID = u.getIDFromName("apa");
		int apcID = u.getIDFromName("apc");
		int cyp26b1ID = u.getIDFromName("cyp26b1");
		int edn1ID = u.getIDFromName("edn1");
		int fgf24ID = u.getIDFromName("fgf24");
		int furinaID = u.getIDFromName("furina");
		int henID = u.getIDFromName("hen");
		int jag1bID = u.getIDFromName("jag1b");
		int lama5ID = u.getIDFromName("lama5");
		int lofID = u.getIDFromName("lof");
		int rndID = u.getIDFromName("rnd");
		int sec23aID = u.getIDFromName("sec23a");
		int sec24aID = u.getIDFromName("sec24b");
		int shhaID = u.getIDFromName("shha");
		int ugdhID = u.getIDFromName("ugdh");


		//test order1 against alf
		double maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(order1ID).getAllEAPhenotypes(),
				geneProfiles.get(alfID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching order1 against alf; Expected " + IC13 + "; found " + maxICScore,softCompare(maxICScore,IC13));

		//test order1 against apa
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(order1ID).getAllEAPhenotypes(),
				geneProfiles.get(apaID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching order1 against apa; Expected " + IC13 + "; found " + maxICScore,softCompare(maxICScore,IC13));

		//test order1 against apc
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(order1ID).getAllEAPhenotypes(),
				geneProfiles.get(apcID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching order1 against apc; Expected " + IC1 + "; found " + maxICScore,softCompare(maxICScore,IC1));

		//test order1 against cyp26b1
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(order1ID).getAllEAPhenotypes(),
				geneProfiles.get(cyp26b1ID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching order1 against cyp26b1; Expected " + IC13 + "; found " + maxICScore,softCompare(maxICScore,IC13));

		//test order1 against jag1b
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(order1ID).getAllEAPhenotypes(),
				geneProfiles.get(jag1bID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching order1 against jag1b; Expected " + IC3 + "; found " + maxICScore, softCompare(maxICScore,IC3));
		
		//test family1 against apc
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(family1ID).getAllEAPhenotypes(),
				geneProfiles.get(apcID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching family1 against apc; Expected " + IC1 + "; found " + maxICScore,softCompare(maxICScore,IC1));

		//test family1 against jag1b
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(family1ID).getAllEAPhenotypes(),
				geneProfiles.get(jag1bID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching family1 against jag1b; Expected " + IC3 + "; found " + maxICScore, softCompare(maxICScore,IC3));
		
		//test genus1 against apc
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(genus1ID).getAllEAPhenotypes(),
				geneProfiles.get(apcID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching genus1 against apc; Expected " + IC4 + "; found " + maxICScore,softCompare(maxICScore,IC4));
		
		//test genus1 against jag1b
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(genus1ID).getAllEAPhenotypes(),
				geneProfiles.get(jag1bID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching genus1 against jag1b; Expected " + IC3 + "; found " + maxICScore,softCompare(maxICScore,IC3));
		
		//test genus2 against apc
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(genus2ID).getAllEAPhenotypes(),
				geneProfiles.get(apcID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching genus2 against apc; Expected " + IC4 + "; found " + maxICScore,softCompare(maxICScore,IC4));

		//test genus2 against jag1b
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(genus2ID).getAllEAPhenotypes(),
				geneProfiles.get(jag1bID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching genus2 against jag1b; Expected " + IC3 + "; found " + maxICScore,softCompare(maxICScore,IC3));

	}

	@Test
	public void testCalcICCS() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		testAnalysis.taxonProfiles= taxonProfiles;
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.size()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		HashMap<Integer,Profile>geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		testAnalysis.geneProfiles= geneProfiles;
		CountTable counts = new CountTable();
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		testAnalysis.fillCountTable(geneProfiles, counts, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);

		int order1ID = u.getIDFromName("Order 1");
		int genus1ID = u.getIDFromName("Genus 1");
		int genus2ID = u.getIDFromName("Genus 2");
		int jag1bID = u.getIDFromName("jag1b");
		int apcID = u.getIDFromName("apc");

		double iccsScore = testAnalysis.calcICCS(taxonProfiles.get(order1ID), geneProfiles.get(jag1bID), phenotypeScores);
		System.out.println("ICCS Score = " +  iccsScore);

		iccsScore = testAnalysis.calcICCS(taxonProfiles.get(order1ID), geneProfiles.get(apcID), phenotypeScores);
		System.out.println("ICCS Score = " +  iccsScore);
	}


	@Test
	public void testMatchOneProfilePair() throws SQLException{
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		testAnalysis.taxonProfiles= taxonProfiles;
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.size()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		HashMap<Integer,Profile>geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		testAnalysis.geneProfiles= geneProfiles;
		CountTable counts = new CountTable();
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		testAnalysis.fillCountTable(geneProfiles, counts, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);
		List<PhenotypeProfileAnalysis.PermutedProfileScore> pScores = testAnalysis.calcPermutedProfileScores(taxonProfiles,geneProfiles,phenotypeScores,u);
		int order1ID = u.getIDFromName("Order 1");
		int genus1ID = u.getIDFromName("Genus 1");
		int genus2ID = u.getIDFromName("Genus 2");
		int jag1bID = u.getIDFromName("jag1b");
		int apcID = u.getIDFromName("apc");

		ProfileScoreSet pSet = testAnalysis.matchOneProfilePair(order1ID,jag1bID,pScores,phenotypeScores);
		Assert.assertEquals(IC3, pSet.getMaxICScore());

		//	testAnalysis.profileMatchReport(phenotypeScores, pScores, null, u);
	}

	/**
	 * Compares double values to within a range of the expected value (avoiding exact comparison of doubles)
	 * @param value
	 * @param expected
	 * @return
	 */
	private boolean softCompare(double value, double expected){
		if ((value < 1.0001*expected) && (0.9999*expected < value))
			return true;
		return false;
	}




	@After
	public void tearDown() throws Exception {
		u.closeKB();
	}

}
