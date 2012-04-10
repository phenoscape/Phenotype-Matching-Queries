package phenoscape.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import phenoscape.queries.lib.ProfileMap;
import phenoscape.queries.lib.ProfileScoreSet;
import phenoscape.queries.lib.TaxonPhenotypeLink;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;

public class TestPropTree2 extends PropTreeTest {




	PhenotypeProfileAnalysis testAnalysis;
	Utils u = new Utils();
	StringWriter testWriter1;
	StringWriter testWriter2;
	StringWriter testWriter3;
	StringWriter testWriter4;
	Map<Integer,Integer> attMap;
	TaxonomyTree t1;
	int nodeIDofQuality;
	Map<Integer,Integer> badTaxonQualities;
	Map<Integer,Integer> badGeneQualities;

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

		badTaxonQualities = new HashMap<Integer,Integer>();
		badGeneQualities = new HashMap<Integer,Integer>();
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
		Assert.assertEquals("Phenotype count for " + TAXON4STR,2,c.size());

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
		Assert.assertEquals("Phenotype count for " + TAXON5STR,4,c.size());

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
		Assert.assertEquals("Phenotype count for " + TAXON6STR,4,c.size());

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
		Assert.assertEquals("Phenotype count for " + TAXON10STR,3,c.size());

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
		Assert.assertEquals("Phenotype count for " + TAXON11STR,3,c.size());

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
		Assert.assertEquals("Phenotype count for " + TAXON12STR,3,c.size());

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
		Assert.assertEquals("Phenotype count for " + TAXON13STR,2,c.size());

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
		Assert.assertEquals("Phenotype count for " + TAXON14STR,2,c.size());

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
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);		
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15, taxonProfiles.domainSize());  //again, should be equal to the number of taxa
	}


	final List<String>entityNames= Arrays.asList("body","opercle","pectoral fin");
	final List<String>attNames= Arrays.asList("optical quality","shape","size");
	@Test
	public void testTraverseTaxonomy() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		Assert.assertEquals("Count of entities used",3,taxonVariation.getUsedEntities().size()); 
		for (Integer entity : taxonVariation.getUsedEntities()){
			Assert.assertTrue("Found unexpected entity " + u.getNodeName(entity.intValue()),entityNames.contains(u.getNodeName(entity.intValue())));
		}
		Assert.assertEquals("Count of qualities used",3,taxonVariation.getUsedAttributes().size());  
		for (Integer att : taxonVariation.getUsedAttributes()){
			Assert.assertTrue("Found unexpected attribute " + u.getNodeName(att.intValue()),attNames.contains(u.getNodeName(att.intValue())));
		}
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals("Taxon profiles at end of traverse taxonomy",15,taxonProfiles.domainSize()); //The taxonVariation table 'knows' where the variation is, but profiles not updated yet
	}


	@Test
	public void testFlushUnvaryingPhenotypes() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals("Count of taxa before flush",15,taxonProfiles.domainSize()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals("Count of taxa with variation",5,taxonProfiles.domainSize()); //profiles has now been trimmed to only those taxa with variation
	}

	@Test
	public void testGetAllGeneAnnotationsFromKB() throws SQLException {
		Collection<DistinctGeneAnnotationRecord> annotations = testAnalysis.getAllGeneAnnotationsFromKB(u);
		Assert.assertEquals(24,annotations.size());
	}

	@Test
	public void testProcessGeneExpression() throws SQLException {
		initNames(u);
		Assert.assertFalse("failed to lookup entity opercle",opercleID==-1);
		Assert.assertFalse("failed to lookup entity pectoral fin",pectoralFinID==-1);
		Assert.assertFalse("failed to lookup entity ventral region of cerebellum",ventralRegionOfCerebellumID==-1);
		Assert.assertFalse("failed to lookup quality count",countID==-1);
		Assert.assertFalse("failed to lookup quality position",positionID==-1);
		Assert.assertFalse("failed to lookup quality shape",shapeID==-1);
		Assert.assertFalse("failed to lookup quality size",sizeID==-1);
		Assert.assertFalse("failed to lookup quality texture",textureID==-1);
		
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
		assertEquals("Count of genes in variation table",19,genes.size());
		
		Assert.assertTrue(geneVariation.geneExhibits(pectoralFinID,sizeID,alfID));
		Assert.assertTrue(geneVariation.geneExhibits(opercleID,shapeID,furinaID));
		Assert.assertTrue(geneVariation.geneExhibits(opercleID,shapeID,jag1bID));
		Assert.assertTrue(geneVariation.geneExhibits(opercleID,shapeID,edn1ID));
		Assert.assertTrue(geneVariation.geneExhibits(opercleID,countID,edn1ID));
		Assert.assertTrue(geneVariation.geneExhibits(ventralRegionOfCerebellumID,sizeID,apcID));
		Assert.assertTrue(geneVariation.geneExhibits(pectoralFinID,sizeID,sec24dID));
		Assert.assertTrue(geneVariation.geneExhibits(pectoralFinID,sizeID,sec23aID));
		Assert.assertTrue(geneVariation.geneExhibits(pectoralFinID,sizeID,shhaID));
		Assert.assertTrue(geneVariation.geneExhibits(pectoralFinID,sizeID,lama5ID));
		Assert.assertTrue(geneVariation.geneExhibits(opercleID,positionID,fgf8aID));
		Assert.assertTrue(geneVariation.geneExhibits(pectoralFinID,sizeID,henID));
		Assert.assertTrue(geneVariation.geneExhibits(pectoralFinID,sizeID,rndID));
		Assert.assertTrue(geneVariation.geneExhibits(opercleID,countID,brpf1ID));
		Assert.assertTrue(geneVariation.geneExhibits(pectoralFinID,sizeID,cyp26b1ID));
		Assert.assertTrue(geneVariation.geneExhibits(pectoralFinID,sizeID,ugdhID));
		Assert.assertTrue(geneVariation.geneExhibits(opercleID,textureID,macf1ID));
		Assert.assertTrue(geneVariation.geneExhibits(pectoralFinID,sizeID,fgf24ID));
		Assert.assertTrue(geneVariation.geneExhibits(pectoralFinID,sizeID,lofID));
	}

	@Test
	public void testBuildEQParents() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
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
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
	}


	@Test
	public void testFillCountTable() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
		CountTableCheck countTableCheck = new CountTableCheck(u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		ProfileMap geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		Map <Integer,Set<Integer>> entityParentCache = new HashMap<Integer,Set<Integer>>();
		Map <Integer,Set<Integer>> entityChildCache = new HashMap<Integer,Set<Integer>>();
		u.setupEntityParents(entityParentCache,entityChildCache);
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		CountTable<PhenotypeExpression> counts = testAnalysis.fillPhenotypeCountTable(geneProfiles, taxonProfiles, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		for(PhenotypeExpression p : counts.getPhenotypes()){
			p.fillNames(u);
			final String fullName = p.getFullName(u);
			Assert.assertNotNull("Full phenotype name",fullName);
			Assert.assertNotNull(countTableCheck);
			Assert.assertNotNull("Count table does not contain: " + p.getFullName(u),countTableCheck.hasPhenotype(p));
			Assert.assertNotNull("Raw count of "+ fullName + " is null?",counts.getRawCount(p));
			Assert.assertEquals(countTableCheck.get(p),counts.getRawCount(p));
		}
	}


	@Test
	public void testBuildPhenotypeMatchCache() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
		testAnalysis.taxonProfiles= taxonProfiles;
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.domainSize()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		ProfileMap geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		testAnalysis.geneProfiles = geneProfiles;
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		Map <Integer,Set<Integer>> entityParentCache = new HashMap<Integer,Set<Integer>>();
		Map <Integer,Set<Integer>> entityChildCache = new HashMap<Integer,Set<Integer>>();
		u.setupEntityParents(entityParentCache,entityChildCache);
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		CountTable <PhenotypeExpression> counts = testAnalysis.fillPhenotypeCountTable(geneProfiles, taxonProfiles, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);
	}

	@Test
	public void testWritePhenotypeMatchSummary() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
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
	public void testCalcMaxIC() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
		testAnalysis.taxonProfiles= taxonProfiles;
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.domainSize()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		ProfileMap geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		testAnalysis.geneProfiles = geneProfiles;
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		Map <Integer,Set<Integer>> entityParentCache = new HashMap<Integer,Set<Integer>>();
		Map <Integer,Set<Integer>> entityChildCache = new HashMap<Integer,Set<Integer>>();
		u.setupEntityParents(entityParentCache,entityChildCache);
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		CountTable <PhenotypeExpression> counts = testAnalysis.fillPhenotypeCountTable(geneProfiles, taxonProfiles, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);

		initNames(u);
		
		//test order1 against alf
		double maxICScore = testAnalysis.calcMaxIC(taxonProfiles.getProfile(order1ID).getAllEAPhenotypes(),
				geneProfiles.getProfile(alfID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching order1 against alf; Expected " + 0.0 + "; found " + maxICScore,softCompare(maxICScore,0.0));

		//test order1 against apa
//		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(order1ID).getAllEAPhenotypes(),
//				geneProfiles.get(apaID).getAllEAPhenotypes(),
//				phenotypeScores);
//		Assert.assertTrue("Matching order1 against apa; Expected " + IC13 + "; found " + maxICScore,softCompare(maxICScore,IC13));
//
//		//test order1 against apc
//		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(order1ID).getAllEAPhenotypes(),
//				geneProfiles.get(apcID).getAllEAPhenotypes(),
//				phenotypeScores);
//		Assert.assertTrue("Matching order1 against apc; Expected " + IC1 + "; found " + maxICScore,softCompare(maxICScore,IC1));
//
//		//test order1 against cyp26b1
//		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(order1ID).getAllEAPhenotypes(),
//				geneProfiles.get(cyp26b1ID).getAllEAPhenotypes(),
//				phenotypeScores);
//		Assert.assertTrue("Matching order1 against cyp26b1; Expected " + IC13 + "; found " + maxICScore,softCompare(maxICScore,IC13));
//
//		//test order1 against jag1b
//		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(order1ID).getAllEAPhenotypes(),
//				geneProfiles.get(jag1bID).getAllEAPhenotypes(),
//				phenotypeScores);
//		Assert.assertTrue("Matching order1 against jag1b; Expected " + IC3 + "; found " + maxICScore, softCompare(maxICScore,IC3));
//		
//		//test family1 against apc
//		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(family1ID).getAllEAPhenotypes(),
//				geneProfiles.get(apcID).getAllEAPhenotypes(),
//				phenotypeScores);
//		Assert.assertTrue("Matching family1 against apc; Expected " + IC1 + "; found " + maxICScore,softCompare(maxICScore,IC1));
//
//		//test family1 against jag1b
//		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(family1ID).getAllEAPhenotypes(),
//				geneProfiles.get(jag1bID).getAllEAPhenotypes(),
//				phenotypeScores);
//		Assert.assertTrue("Matching family1 against jag1b; Expected " + IC3 + "; found " + maxICScore, softCompare(maxICScore,IC3));
//		
//		//test genus1 against apc
//		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(genus1ID).getAllEAPhenotypes(),
//				geneProfiles.get(apcID).getAllEAPhenotypes(),
//				phenotypeScores);
//		Assert.assertTrue("Matching genus1 against apc; Expected " + IC4 + "; found " + maxICScore,softCompare(maxICScore,IC4));
//		
//		//test genus1 against jag1b
//		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(genus1ID).getAllEAPhenotypes(),
//				geneProfiles.get(jag1bID).getAllEAPhenotypes(),
//				phenotypeScores);
//		Assert.assertTrue("Matching genus1 against jag1b; Expected " + IC3 + "; found " + maxICScore,softCompare(maxICScore,IC3));
//		
//		//test genus2 against apc
//		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(genus2ID).getAllEAPhenotypes(),
//				geneProfiles.get(apcID).getAllEAPhenotypes(),
//				phenotypeScores);
//		Assert.assertTrue("Matching genus2 against apc; Expected " + IC4 + "; found " + maxICScore,softCompare(maxICScore,IC4));
//
//		//test genus2 against jag1b
//		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(genus2ID).getAllEAPhenotypes(),
//				geneProfiles.get(jag1bID).getAllEAPhenotypes(),
//				phenotypeScores);
//		Assert.assertTrue("Matching genus2 against jag1b; Expected " + IC3 + "; found " + maxICScore,softCompare(maxICScore,IC3));

	}


	@Test
	public void testMatchOneProfilePair() throws SQLException, IOException{
//		t1.traverseOntologyTree(u);
//		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
//		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
//		testAnalysis.taxonProfiles= taxonProfiles;
//		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
//		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
//		assertFalse(taxonProfiles.isEmpty());
//		Assert.assertEquals(15,taxonProfiles.domainSize()); //profiles before the flush includes all taxa
//		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
//		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
//		ProfileMap geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
//		testAnalysis.geneProfiles= geneProfiles;
//		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
//		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
//		CountTable<Integer> entityCounts = testAnalysis.fillGeneEntityCountTable(testAnalysis.geneProfiles, entityParentCache, u,PhenotypeProfileAnalysis.GENEENTITYCOUNTQUERY , u.countDistinctGeneEntityPhenotypeAnnotations());		
//
//		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
//		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
//		CountTable<PhenotypeExpression> counts = testAnalysis.fillPhenotypeCountTable(geneProfiles, taxonProfiles, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
//		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);
//		List<PermutedProfileScore> pScores = testAnalysis.calcPermutedProfileScores(taxonProfiles,geneProfiles,phenotypeScores,entityCounts,u);
//		
//		// test distribution dumping, though this is temporary...
////		for(PermutedProfileScore score : pScores){
////			score.writeDist(PhenotypeProfileAnalysis.RANDOMIZATIONREPORTSFOLDER);
////		}
//
//		initNames(u);
//
//		
//		// check genes against order1
//		ProfileScoreSet pSet = testAnalysis.matchOneProfilePair(order1ID,alfID,pScores,phenotypeScores,entityParentCache,entityCounts, phenotypeParentCache,u);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,apaID,pScores,phenotypeScores);
//		Assert.assertEquals(IC13, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,apcID,pScores,phenotypeScores);
//		Assert.assertEquals(IC1, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,cyp26b1ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC13, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,edn1ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,fgf24ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC13, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,furinaID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,henID,pScores,phenotypeScores);
//		Assert.assertEquals(IC13, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,jag1bID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,lama5ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC13, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,lofID,pScores,phenotypeScores);
//		Assert.assertEquals(IC13, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,rndID,pScores,phenotypeScores);
//		Assert.assertEquals(IC13, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,sec23aID,pScores,phenotypeScores);
//		Assert.assertEquals(IC13, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,sec24dID,pScores,phenotypeScores);
//		Assert.assertEquals(IC13, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,shhaID,pScores,phenotypeScores);
//		Assert.assertEquals(IC13, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(order1ID,ugdhID,pScores,phenotypeScores);
//		Assert.assertEquals(IC13, pSet.getMaxICScore());


		// check genes against family1
//		pSet = testAnalysis.matchOneProfilePair(family1ID,jag1bID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,apaID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,apcID,pScores,phenotypeScores);
//		Assert.assertEquals(IC1, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,cyp26b1ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,edn1ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,fgf24ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,furinaID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,henID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,jag1bID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,lama5ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,lofID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,rndID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,sec23aID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,sec24dID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,shhaID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(family1ID,ugdhID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());

		
		// check genes against genus1
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,jag1bID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,apaID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,apcID,pScores,phenotypeScores);
//		Assert.assertEquals(IC4, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,cyp26b1ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,edn1ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC2, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,fgf24ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,furinaID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,henID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,jag1bID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,lama5ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,lofID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,rndID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,sec23aID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,sec24dID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,shhaID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus1ID,ugdhID,pScores,phenotypeScores);
//		Assert.assertEquals(IC12, pSet.getMaxICScore());

		
		// check genes against genus2
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,jag1bID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,apaID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,apcID,pScores,phenotypeScores);
//		Assert.assertEquals(IC4, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,cyp26b1ID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,edn1ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC2, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,fgf24ID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,furinaID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,henID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,jag1bID,pScores,phenotypeScores);
//		Assert.assertEquals(IC3, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,lama5ID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,lofID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,rndID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,sec23aID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,sec24dID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,shhaID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus2ID,ugdhID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());

		
		// check genes against genus3
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,jag1bID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,apaID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,apcID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,cyp26b1ID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,edn1ID,pScores,phenotypeScores);
//		Assert.assertEquals(IC2, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,fgf24ID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,furinaID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,henID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,jag1bID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,lama5ID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,lofID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,rndID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,sec23aID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,sec24dID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,shhaID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
//
//		pSet = testAnalysis.matchOneProfilePair(genus3ID,ugdhID,pScores,phenotypeScores);
//		Assert.assertEquals(0.0, pSet.getMaxICScore());
	}

	private static final String TAXONREPORTFILENAME = "../../SmallKBTests/PropTree2/TaxonVariationReport.txt";
	private static final String GENEREPORTFILENAME = "../../SmallKBTests/PropTree2/GeneVariationReport.txt";
	private static final String PHENOTYPEMATCHREPORTFILENAME = "../../SmallKBTests/PropTree2/PhenotypeMatchReport.txt";
	private static final String PROFILEMATCHREPORTFILENAME = "../../SmallKBTests/PropTree2/ProfileMatchReport.txt";
	private static final String TAXONGENEMAXICSCOREFILENAME = "../../SmallKBTests/PropTree2/MaxICReport.txt";

	

	@Test
//	public void testOutputFiles() throws SQLException, IOException{
//		File outFile1 = new File(TAXONREPORTFILENAME);
//		File outFile2 = new File(GENEREPORTFILENAME);
//		File outFile3 = new File(PHENOTYPEMATCHREPORTFILENAME);
//		File outFile4 = new File(PROFILEMATCHREPORTFILENAME);
//		File outFile5 = new File(TAXONGENEMAXICSCOREFILENAME);
//		Writer taxonWriter = null;
//		Writer geneWriter = null;
//		Writer phenoWriter = null;
//		Writer profileWriter = null;
//		Writer w5 = null;
//		Date today;
//		DateFormat dateFormatter;
//
//		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT);
//		today = new Date();
//		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
//		String timeStamp = dateFormatter.format(today) + " " + timeFormatter.format(today) + " on PropTree2";		
//		taxonWriter = new BufferedWriter(new FileWriter(outFile1));
//		geneWriter = new BufferedWriter(new FileWriter(outFile2));
//		phenoWriter = new BufferedWriter(new FileWriter(outFile3));
//		profileWriter = new BufferedWriter(new FileWriter(outFile4));
//		w5 = new BufferedWriter(new FileWriter(outFile5));
//		u.writeOrDump(timeStamp, taxonWriter);
//		u.writeOrDump(timeStamp, geneWriter);
//		u.writeOrDump(timeStamp, phenoWriter);
//		u.writeOrDump(timeStamp, profileWriter);
//		u.writeOrDump(timeStamp, w5);
//		u.writeOrDump("Starting analysis: " + timeStamp, null);
//
//		PhenotypeExpression.getEQTop(u);   //just to initialize early.
//
//		// process taxa annotations
//		
//		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
//		testAnalysis.taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
//		testAnalysis.countAnnotatedTaxa(t1,t1.getRootNodeID(),testAnalysis.taxonProfiles,u);
//		int eaCount = testAnalysis.countEAAnnotations(testAnalysis.taxonProfiles,u);
//		u.writeOrDump("Count of distinct taxon-phenotype assertions (EQ level): " + testAnalysis.taxonPhenotypeLinkCount, taxonWriter);
//		u.writeOrDump("Count of distinct taxon-phenotype assertions (EA level; not filtered for variation): " + eaCount, taxonWriter);
//		u.writeOrDump("Count of annotated taxa = " + testAnalysis.annotatedTaxa, taxonWriter);
//		u.writeOrDump("Count of parents of annotated taxa = " + testAnalysis.parentsOfAnnotatedTaxa, taxonWriter);
//
//		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
//
//		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), testAnalysis.taxonProfiles, taxonVariation, u);
//		t1.report(u, taxonWriter);
//		taxonVariation.variationReport(u,taxonWriter);	
//		u.writeOrDump("\nList of qualities that were placed under quality as an attribute by default\n", taxonWriter);
//		for(Integer bad_id : badTaxonQualities.keySet()){
//			u.writeOrDump(u.getNodeName(bad_id) + " " + badTaxonQualities.get(bad_id), taxonWriter);
//		}
//		testAnalysis.flushUnvaryingPhenotypes(testAnalysis.taxonProfiles,taxonVariation,u);
//		Assert.assertFalse(testAnalysis.taxonProfiles.isEmpty());
//
//		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
//
//		testAnalysis.geneProfiles = testAnalysis.processGeneExpression(geneVariation,u, geneWriter);
//		geneVariation.variationReport(u, geneWriter);
//		u.writeOrDump("\nList of qualities that were placed under quality as an attribute by default\n", geneWriter);
//		for(Integer bad_id : badGeneQualities.keySet()){
//			u.writeOrDump(u.getNodeName(bad_id) + " " + badGeneQualities.get(bad_id), geneWriter);
//		}
//
//		geneWriter.close();
//
//		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
//		CountTable<Integer> entityCountsToUse = testAnalysis.fillGeneEntityCountTable(testAnalysis.geneProfiles, entityParentCache, u,PhenotypeProfileAnalysis.GENEENTITYCOUNTQUERY , u.countDistinctGeneEntityPhenotypeAnnotations());
//
//		/* Test introduction of phenotypeParentCache, which should map an attribute level EQ to all its parents via inheres_in_part_of entity parents and is_a quality parents (cross product) */
//		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
//		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
//
//		CountTable<PhenotypeExpression> phenotypeCountsToUse = testAnalysis.fillPhenotypeCountTable(testAnalysis.geneProfiles, testAnalysis.taxonProfiles, phenotypeParentCache, u, GENEPHENOTYPECOUNTQUERY, GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
//
//
//		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
//
//
//		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, phenotypeCountsToUse, u);
//		taxonWriter.close();
//		testAnalysis.writePhenotypeMatchSummary(phenotypeScores,u,phenoWriter);		
//		phenoWriter.close();
//
//
//		testAnalysis.writeTaxonGeneMaxICSummary(phenotypeScores,u,w5);
//		w5.close();
//		List<PermutedProfileScore> pScores = testAnalysis.calcPermutedProfileScores(testAnalysis.taxonProfiles,testAnalysis.geneProfiles,phenotypeScores, entityCountsToUse, u);
//		
//		testAnalysis.profileMatchReport(phenotypeScores,pScores,profileWriter,entityParentCache, entityCountsToUse,phenotypeParentCache, u);
//		profileWriter.close();
//		
//		taxonWriter.close();
//		geneWriter.close();
//		phenoWriter.close();
//		profileWriter.close();
//		w5.close();
//	}


	@After
	public void tearDown() throws Exception {
		u.closeKB();
	}

}
