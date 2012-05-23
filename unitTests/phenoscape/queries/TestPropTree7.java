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
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import phenoscape.queries.lib.CountTable;
import phenoscape.queries.lib.DistinctGeneAnnotationRecord;
import phenoscape.queries.lib.EntitySet;
import phenoscape.queries.lib.PermutedScoreSet;
import phenoscape.queries.lib.PhenotypeExpression;
import phenoscape.queries.lib.PhenotypeScoreTable;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.ProfileMap;
import phenoscape.queries.lib.ProfileScoreSet;
import phenoscape.queries.lib.TaxonPhenotypeLink;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;

public class TestPropTree7 extends PropTreeTest{


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
	CountTableCheck countTableCheck;
	
	Map<Integer,Set<Integer>> qualitySubsumers;

	
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
		testAnalysis.qualitySubsumers = new HashMap<Integer,Set<Integer>>(); 

		badTaxonQualities = new HashMap<Integer,Integer>();
		badGeneQualities = new HashMap<Integer,Integer>();
		String taxonomyRoot = UNITTESTROOT; 
		t1 = new TaxonomyTree(taxonomyRoot,u);
		t1.traverseOntologyTree(u);
		
		Statement s = u.getStatement();
		ResultSet ts = s.executeQuery(DistinctGeneAnnotationRecord.getQuery());
		while (ts.next()){
			int geneID = ts.getInt(1);
			u.cacheOneNode(geneID);
		}

	}


	private static final String NODEQUERY = "SELECT n.node_id FROM node AS n WHERE n.uid = ?";


	@Test
	public void TestGetTaxonPhenotypeLinksFromKB() throws Exception{
		int taxonid = -1;
		ResultSet r;
		Set<TaxonPhenotypeLink> lset;
		PreparedStatement p = u.getPreparedStatement(NODEQUERY);

		p.setString(1,TAXON1STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON1STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertTrue(lset.isEmpty());

		p.setString(1,TAXON2STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON2STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertTrue(lset.isEmpty());

		p.setString(1,TAXON3STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON3STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertTrue(lset.isEmpty());

		p.setString(1,TAXON4STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON4STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertFalse(lset.isEmpty());
		Assert.assertEquals(4,lset.size());  //will be adjusted to 3 by removeSymmetricLinks

		p.setString(1,TAXON5STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON5STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertFalse(lset.isEmpty());
		int delCount = testAnalysis.removeSymmetricLinksOneTaxon(lset, u);
		Assert.assertEquals(9,lset.size());    //will be adjusted to 8 by removeSymmetricLinks
		Assert.assertEquals(1,delCount);
		p.setString(1,TAXON6STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON6STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertFalse(lset.isEmpty());
		delCount = testAnalysis.removeSymmetricLinksOneTaxon(lset, u);
		//Assert.assertEquals(6,c.size());  //should be 7 when fixed
		Assert.assertEquals(8,lset.size());  //is 8 until fixed
		Assert.assertEquals(1,delCount);
		p.setString(1,TAXON7STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON7STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertTrue(lset.isEmpty());

		p.setString(1,TAXON8STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON8STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertTrue(lset.isEmpty());

		p.setString(1,TAXON9STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON9STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertTrue(lset.isEmpty());

		p.setString(1,TAXON10STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON10STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertFalse(lset.isEmpty());
		delCount = testAnalysis.removeSymmetricLinksOneTaxon(lset, u);
		Assert.assertEquals(8,lset.size());  
		Assert.assertEquals(1,delCount);
		
		p.setString(1,TAXON11STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON11STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertFalse(lset.isEmpty());
		delCount = testAnalysis.removeSymmetricLinksOneTaxon(lset, u);
		Assert.assertEquals(8,lset.size());  
		Assert.assertEquals(1,delCount);

		p.setString(1,TAXON12STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON12STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertFalse(lset.isEmpty());
		delCount = testAnalysis.removeSymmetricLinksOneTaxon(lset, u);
		Assert.assertEquals(8,lset.size());  
		Assert.assertEquals(1,delCount);

		p.setString(1,TAXON13STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON13STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertFalse(lset.isEmpty());
		delCount = testAnalysis.removeSymmetricLinksOneTaxon(lset, u);
		Assert.assertEquals(7,lset.size());  
		Assert.assertEquals(1,delCount);

		p.setString(1,TAXON14STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON14STR);
		}
		lset = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(lset);
		assertFalse(lset.isEmpty());
		delCount = testAnalysis.removeSymmetricLinksOneTaxon(lset, u);
		Assert.assertEquals(7,lset.size());  
		Assert.assertEquals(1,delCount);

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
		testAnalysis.removeSymmetricLinks(allLinks,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);		
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15, taxonProfiles.domainSize());  //again, should be equal to the number of taxa
	}
	
	// SubsumingUnion and Intersection operate on sets of Integers, retrieving subsumption relations for the phenotypes represented by those
	// integers from the quality subsumers table.  
	// Tests for subsumingUnion and intersection are laid out as follows:
	//   1. test for normal operation (no subsumption)
	//   2. test for adding singleton sets (e.g., sets built at the genus level from species
	//   3. test for more complex cases
	
	
	// Test that that subsumingUnion operates as a union operation when there are no subsumption relations among the elements
	@Test
	public void testSubsumingUnionNoSubsumption(){
		testAnalysis.qualitySubsumers.clear();  //start with an empty table, verify basic union,intersection
		Set<Integer>test1 = new HashSet<Integer>();
		test1.add(1);
		test1.add(2);
		test1.add(3);
		Set<Integer>test2 = new HashSet<Integer>();
		test2.add(2);
		test2.add(4);
		test2.add(6);
		Set<Integer>test3 = new HashSet<Integer>();
		test3.add(3);
		test3.add(6);
		test3.add(9);
		Set<Integer>unionSet = new HashSet<Integer>();
		unionSet = testAnalysis.subsumingUnion(unionSet, test1);
		assertEquals(3,unionSet.size());
		unionSet = testAnalysis.subsumingUnion(unionSet, test2);
		assertEquals(5,unionSet.size());
		unionSet = testAnalysis.subsumingUnion(unionSet, test3);
		assertEquals(6,unionSet.size());
	}

	// Test that that subsumingUnion works correctly when there are subsuming relations
	//  2 subsumes 4, 6
	//  3 subsumes 6, 9
	@Test
	public void testSubsumingUnionWithSubsumption(){
		Set<Integer>test1 = new HashSet<Integer>();
		test1.add(1);
		test1.add(2);
		test1.add(3);
		Set<Integer>test2 = new HashSet<Integer>();
		test2.add(2);
		test2.add(4);
		test2.add(6);
		Set<Integer>test3 = new HashSet<Integer>();
		test3.add(3);
		test3.add(6);
		test3.add(9);
		Set<Integer>test4 = new HashSet<Integer>();
		test4.add(7);
		Set<Integer>unionSet = new HashSet<Integer>();
		Set<Integer>fourParents = new HashSet<Integer>();
		fourParents.add(2);
		testAnalysis.qualitySubsumers = new HashMap<Integer,Set<Integer>>();  //empty subsumption table to fill
		testAnalysis.qualitySubsumers.put(4,fourParents);
		Set<Integer>sixParents = new HashSet<Integer>();
		sixParents.add(2);
		sixParents.add(3);
		testAnalysis.qualitySubsumers.put(6,sixParents);
		Set<Integer>eightParents = new HashSet<Integer>();
		eightParents.add(2);
		eightParents.add(4);
		testAnalysis.qualitySubsumers.put(8,eightParents);
		Set<Integer>nineParents = new HashSet<Integer>();
		nineParents.add(3);
		testAnalysis.qualitySubsumers.put(9,nineParents);
		
		//set up some singleton sets (e.g., species phenotypes)
		Set<Integer>test2x = new HashSet<Integer>();
		test2x.add(2);
		Set<Integer>test2y = new HashSet<Integer>();
		test2y.add(4);
		Set<Integer>test2z = new HashSet<Integer>();
		test2z.add(6);

		//test2w tests that subsumingUnion is order of operation independent for adding singleton sets
		Set<Integer> test2w = new HashSet<Integer>();
		test2w = testAnalysis.subsumingUnion(test2w, test2x);  //add {2}
		assertEquals(1,test2w.size());
		assertTrue(test2w.contains(2));
		test2w = testAnalysis.subsumingUnion(test2w, test2y); // {2} U {4}
		assertEquals(1,test2w.size());
		assertTrue(test2w.contains(2));  // 2 subsumes 4, so nothing is added to the union
		test2w = testAnalysis.subsumingUnion(test2w, test2z);  //{2} U {6}
		assertEquals(1,test2w.size());
		assertTrue(test2w.contains(2));  // 2 subsumes 6, so nothing is added to the union
		
		// add sets in the opposite order
		test2w.clear();
		test2w = testAnalysis.subsumingUnion(test2w, test2z);  // add {6}
		assertEquals(1,test2w.size());
		assertTrue(test2w.contains(6));
		test2w = testAnalysis.subsumingUnion(test2w, test2y);  // {6} U {4}
		assertEquals(2,test2w.size());
		assertTrue(test2w.contains(6));
		assertTrue(test2w.contains(4));  //{4, 6}
		test2w = testAnalysis.subsumingUnion(test2w, test2x);
		assertEquals(1,test2w.size());
		assertTrue(test2w.contains(2));   // 2 subsumes both 4 and 6
		
		
		unionSet = testAnalysis.subsumingUnion(unionSet, test1);  //{} U {1,2,3}
		assertEquals(3,unionSet.size());
		assertTrue(unionSet.contains(1));
		assertTrue(unionSet.contains(2));
		assertTrue(unionSet.contains(3));		
		unionSet = testAnalysis.subsumingUnion(unionSet, test2); //{1,2,3} U {2,4,6} - 2 subsumes 4 and 6, so no change
		assertEquals(3,unionSet.size());
		assertTrue(unionSet.contains(1));
		assertTrue(unionSet.contains(2));
		assertTrue(unionSet.contains(3));		
		unionSet = testAnalysis.subsumingUnion(unionSet, test3);  //{1,2,3} U {3,6,9} - 3 subsumes 6 and 9, no change
		assertEquals(3,unionSet.size());
		assertTrue(unionSet.contains(1));
		assertTrue(unionSet.contains(2));
		assertTrue(unionSet.contains(3));		
		unionSet = testAnalysis.subsumingUnion(unionSet, test4);  //{1,2,3} U {7} - 7 not subsumed, so is added
		assertEquals(4,unionSet.size());
		assertTrue(unionSet.contains(1));
		assertTrue(unionSet.contains(2));
		assertTrue(unionSet.contains(3));		
		assertTrue(unionSet.contains(7));		

	}


	
	final List<String>entityNames= Arrays.asList("body","opercle","pectoral fin","posterior region of frontal bone","process of anterior region of dentary","vertebra");
	final List<String>attNames= Arrays.asList("optical quality","shape","size","count");
	
	@Test
	public void testTaxonAnalysis() throws SQLException{
		initNames(u);
		testAnalysis.qualitySubsumers = u.buildPhenotypeSubsumers();
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		assertFalse(allLinks.isEmpty());
		testAnalysis.removeSymmetricLinks(allLinks,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);

		int taxon = genus1ID; 
		assertEquals(15,t1.getTable().size());
		Set<Integer> children = t1.getTable().get(taxon);
		assertFalse(children.isEmpty());
		final Set<Profile> childProfiles = new HashSet<Profile>();
		for(Integer child : children){
			childProfiles.add(taxonProfiles.getProfile(child));
		}
		Profile parentProfile = taxonProfiles.getProfile(taxon);			
		final Set<Integer> usedEntities = new HashSet<Integer>();
		final Set<Integer> usedAttributes = new HashSet<Integer>();
		for (Profile childProfile : childProfiles){
			usedEntities.addAll(childProfile.getUsedEntities());
			usedAttributes.addAll(childProfile.getUsedAttributes());
		}
		//logger.info("Processing taxon: " + u.getNodeName(taxon));
		for(Integer ent : usedEntities){
			for(Integer att : usedAttributes){
				if ((ent.intValue() == pectoralFinID  && att.intValue() == sizeID) ||
					(ent.intValue() == vertebraID && att.intValue() == countID) ||
					(ent.intValue() == opercleID && att.intValue() == shapeID) ||
					(ent.intValue() == opercleID && att.intValue() == opticalQualityID)){
						assertTrue("Genus 1; ent: " + ent + "; att: " + att + "should be true",testAnalysis.taxonAnalysis(childProfiles,ent,att,parentProfile));
					}
				else {
					assertFalse("Genus 1; ent: " + ent + "; att: " + att + "should be false",testAnalysis.taxonAnalysis(childProfiles,ent,att,parentProfile));
				}
			}
		}
		taxon = genus3ID; 
		children = t1.getTable().get(taxon);
		assertFalse(children.isEmpty());
		childProfiles.clear();
		for(Integer child : children){
			childProfiles.add(taxonProfiles.getProfile(child));
		}
		parentProfile = taxonProfiles.getProfile(taxon);			
		usedEntities.clear();
		usedAttributes.clear();
		for (Profile childProfile : childProfiles){
			usedEntities.addAll(childProfile.getUsedEntities());
			usedAttributes.addAll(childProfile.getUsedAttributes());
		}
		for(Integer ent : usedEntities){
			for(Integer att : usedAttributes){
				if (
					(ent.intValue() == vertebraID && att.intValue() == countID) ||
					(ent.intValue() == opercleID && att.intValue() == opticalQualityID) ||
					(ent.intValue() == bodyID && att.intValue() == opticalQualityID)){
						assertTrue("Genus 3; ent: " + ent + "; att: " + att + "should be true",testAnalysis.taxonAnalysis(childProfiles,ent,att,parentProfile));
					}
				else {
					assertFalse("Genus 3; ent: " + ent + "; att: " + att + "should be false",testAnalysis.taxonAnalysis(childProfiles,ent,att,parentProfile));
				}
			}
		}
	}
	
	
	
	
	@Test
	public void testTraverseTaxonomy() throws SQLException {
		testAnalysis.qualitySubsumers = u.buildPhenotypeSubsumers();
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		assertFalse(allLinks.isEmpty());
		testAnalysis.removeSymmetricLinks(allLinks,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
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
		Assert.assertEquals("Taxon profiles at end of traverse taxonomy",15,taxonProfiles.domainSize()); //The taxonVariation table 'knows' where the variation is, but profiles not updated yet
	}


	@Test
	public void testFlushUnvaryingPhenotypes() throws SQLException {
		testAnalysis.qualitySubsumers = u.buildPhenotypeSubsumers();
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		testAnalysis.removeSymmetricLinks(allLinks,u);
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
		Assert.assertEquals("Number of gene phenotype annotations",24,annotations.size());
	}

	@Test
	public void testProcessGeneExpression() throws SQLException {
		initNames(u);
		Assert.assertFalse("failed to lookup entity opercle",opercleID==-1);
		Assert.assertFalse("failed to lookup entity eye",eyeID==-1);
		Assert.assertFalse("failed to lookup entity pectoral fin",pectoralFinID==-1);
		Assert.assertFalse("failed to lookup entity dorsal region of cerebellum",dorsalRegionOfCerebellumID==-1);
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
		assertEquals("Count of genes in variation table",20,genes.size());
		
		Assert.assertTrue(geneVariation.geneExhibits(pectoralFinID,sizeID,alfID));
		Assert.assertTrue(geneVariation.geneExhibits(opercleID,shapeID,furinaID));
		Assert.assertTrue(geneVariation.geneExhibits(opercleID,shapeID,jag1bID));
		Assert.assertTrue(geneVariation.geneExhibits(opercleID,shapeID,edn1ID));
		Assert.assertTrue(geneVariation.geneExhibits(opercleID,countID,edn1ID));
		Assert.assertTrue(geneVariation.geneExhibits(dorsalRegionOfCerebellumID,shapeID,apcID));
		Assert.assertTrue(geneVariation.geneExhibits(dorsalRegionOfCerebellumID,sizeID,apcID));
		Assert.assertTrue(geneVariation.geneExhibits(eyeID,sizeID,apcID));
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
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		assertTrue(phenotypeParentCache.size()>0);
		System.out.println("Size is " + phenotypeParentCache.size());
//		for (PhenotypeExpression pe : phenotypeParentCache.keySet()){
//			System.out.println("Phenotype: " + u.stringForMessage(pe) + "; set: " + phenotypeParentCache.get(pe).hashCode());
//		}
	}
	
	@Test
	public void testCountGeneEntities() throws SQLException {
		Assert.assertEquals(6,u.countDistinctGeneEntityPhenotypeAnnotations());
	}

	@Test
	public void testCountTaxonEntities() throws SQLException {
		Assert.assertEquals(7,u.countDistinctTaxonEntityPhenotypeAnnotations());
	}

	
	@Test
	public void testFillCountTable() throws SQLException {
		testAnalysis.qualitySubsumers = u.buildPhenotypeSubsumers();
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		testAnalysis.removeSymmetricLinks(allLinks,u);
		ProfileMap taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
		CountTableCheck countTableCheck = new CountTableCheck(u);  //captures expected values
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		ProfileMap geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		Map <Integer,Set<Integer>> entityParentCache = new HashMap<Integer,Set<Integer>>();
		Map <Integer,Set<Integer>> entityChildCache = new HashMap<Integer,Set<Integer>>();
		u.setupEntityParents(entityParentCache,entityChildCache);
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		CountTable <PhenotypeExpression> counts = testAnalysis.fillPhenotypeCountTable(geneProfiles, taxonProfiles, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		for(PhenotypeExpression p : counts.getPhenotypes()){
			Assert.assertNotNull("Phenotype p", p);
			p.fillNames(u);
			final String fullName = p.getFullName(u);
			Assert.assertNotNull("Full phenotype name",fullName);
			Assert.assertNotNull(countTableCheck);
			Assert.assertNotNull("Count table does not contain: " + fullName,countTableCheck.hasPhenotype(p));
			Assert.assertNotNull("Raw count of "+ fullName + " is null?",counts.getRawCount(p));
			//Assert.assertNotNull("countTableCheck of " + fullName,countTableCheck.get(p));
			//Assert.assertNotNull("rawCount of " + fullName, counts.getRawCount(p));
			//Assert.assertEquals(countTableCheck.get(p),counts.getRawCount(p));
		}
	}


	@Test
	public void testBuildPhenotypeMatchCache() throws SQLException {
		testAnalysis.qualitySubsumers = u.buildPhenotypeSubsumers();
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		testAnalysis.removeSymmetricLinks(allLinks,u);
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
	public void testWritePhenotypeMatchSummary() throws SQLException{
		testAnalysis.qualitySubsumers = u.buildPhenotypeSubsumers();
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		testAnalysis.removeSymmetricLinks(allLinks,u);
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
		testAnalysis.qualitySubsumers = u.buildPhenotypeSubsumers();
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		testAnalysis.removeSymmetricLinks(allLinks,u);
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
		initNames(u);

		//test order1 against alf
		double maxICScore = testAnalysis.calcMaxIC(taxonProfiles.getProfile(order1ID).getAllEAPhenotypes(),
				geneProfiles.getProfile(alfID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching order1 against alf; Expected " + IC13 + "; found " + maxICScore,softCompare(maxICScore,IC13));

		//test order1 against apa
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.getProfile(order1ID).getAllEAPhenotypes(),
				geneProfiles.getProfile(apaID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching order1 against apa; Expected " + IC13 + "; found " + maxICScore,softCompare(maxICScore,IC13));

		//test order1 against apc
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.getProfile(order1ID).getAllEAPhenotypes(),
				geneProfiles.getProfile(apcID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching order1 against apc; Expected " + IC1 + "; found " + maxICScore,softCompare(maxICScore,IC1));

		//test order1 against cyp26b1
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.getProfile(order1ID).getAllEAPhenotypes(),
				geneProfiles.getProfile(cyp26b1ID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching order1 against cyp26b1; Expected " + IC13 + "; found " + maxICScore,softCompare(maxICScore,IC13));

		//test order1 against jag1b
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.getProfile(order1ID).getAllEAPhenotypes(),
				geneProfiles.getProfile(jag1bID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching order1 against jag1b; Expected " + IC3 + "; found " + maxICScore, softCompare(maxICScore,IC3));
		
		//test family1 against apc
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.getProfile(family1ID).getAllEAPhenotypes(),
				geneProfiles.getProfile(apcID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching family1 against apc; Expected " + IC1 + "; found " + maxICScore,softCompare(maxICScore,IC1));

		//test family1 against jag1b
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.getProfile(family1ID).getAllEAPhenotypes(),
				geneProfiles.getProfile(jag1bID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching family1 against jag1b; Expected " + IC3 + "; found " + maxICScore, softCompare(maxICScore,IC3));
		
		//test genus1 against apc
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.getProfile(genus1ID).getAllEAPhenotypes(),
				geneProfiles.getProfile(apcID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching genus1 against apc; Expected " + IC4 + "; found " + maxICScore,softCompare(maxICScore,IC4));
		
		//test genus1 against jag1b
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.getProfile(genus1ID).getAllEAPhenotypes(),
				geneProfiles.getProfile(jag1bID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching genus1 against jag1b; Expected " + IC3 + "; found " + maxICScore,softCompare(maxICScore,IC3));
		
		//test genus2 against apc
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.getProfile(genus2ID).getAllEAPhenotypes(),
				geneProfiles.getProfile(apcID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching genus2 against apc; Expected " + IC4 + "; found " + maxICScore,softCompare(maxICScore,IC4));

		//test genus2 against jag1b
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.getProfile(genus2ID).getAllEAPhenotypes(),
				geneProfiles.getProfile(jag1bID).getAllEAPhenotypes(),
				phenotypeScores);
		Assert.assertTrue("Matching genus2 against jag1b; Expected " + IC3 + "; found " + maxICScore,softCompare(maxICScore,IC3));
	}



	@Test
	public void testMatchOneProfilePair() throws SQLException, IOException{
		testAnalysis.qualitySubsumers = u.buildPhenotypeSubsumers();
		t1.traverseOntologyTree(u);
		
		testAnalysis.entityAnnotations = new EntitySet(u);
		
		testAnalysis.entityAnnotations.fillTaxonPhenotypeAnnotationsToEntities();
		final int ata = testAnalysis.entityAnnotations.annotationTotal();
		Assert.assertEquals(u.countAssertedTaxonPhenotypeAnnotations(),ata);
		testAnalysis.entityAnnotations.fillGenePhenotypeAnnotationsToEntities();
		final int tea = testAnalysis.entityAnnotations.annotationTotal();
		final int dga = u.countDistinctGenePhenotypeAnnotations();
		Assert.assertEquals(tea, ata+dga);
		
		testAnalysis.totalAnnotations = ata + dga;

		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		testAnalysis.removeSymmetricLinks(allLinks,u);
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
		CountTable<Integer> geneEntityCounts = testAnalysis.fillGeneEntityCountTable(testAnalysis.geneProfiles,  entityParentCache, u,PhenotypeProfileAnalysis.GENEENTITYCOUNTQUERY , u.countDistinctGeneEntityPhenotypeAnnotations());		
		CountTable<Integer> taxonEntityCounts = testAnalysis.fillTaxonEntityCountTable(testAnalysis.taxonProfiles,  entityParentCache, u,PhenotypeProfileAnalysis.TAXONENTITYCOUNTQUERY , u.countDistinctGeneEntityPhenotypeAnnotations());		

		
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		CountTable<PhenotypeExpression> counts = testAnalysis.fillPhenotypeCountTable(geneProfiles, taxonProfiles, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);
		PermutedScoreSet s = new PermutedScoreSet(testAnalysis.taxonProfiles,testAnalysis.geneProfiles,entityParentCache, entityChildCache, phenotypeScores, u);
		s.setRandom(new Random());
		s.calcPermutedProfileScores();

		initNames(u);
		// check genes against order1
		ProfileScoreSet pSet = testAnalysis.matchOneProfilePair(order1ID,alfID,s,phenotypeScores,entityParentCache,entityChildCache,testAnalysis.entityAnnotations, phenotypeParentCache,u);
		Assert.assertEquals(IC13, pSet.getMaxICScore());
		Assert.assertEquals(IC13,pSet.getMeanICScore());
	}

	
	private static final String TAXONREPORTFILENAME =  "../../SmallKBTests/PropTree7/TaxonVariationReport.txt";
	private static final String GENEREPORTFILENAME =  "../../SmallKBTests/PropTree7/GeneVariationReport.txt";
	private static final String PHENOTYPEMATCHREPORTFILENAME = "../../SmallKBTests/PropTree7/PhenotypeMatchReport.txt";
	private static final String PROFILEMATCHREPORTFILENAME = "../../SmallKBTests/PropTree7/ProfileMatchReport.txt";
	private static final String TAXONGENEMAXICSCOREFILENAME = "../../SmallKBTests/PropTree7/MaxICReport.txt";
	private static final String RANDOMIZATIONREPORTSFOLDER = "../../SmallKBTests/PropTree7/RandomizationReports";

	

	@Test
	public void testOutputFiles() throws SQLException, IOException{
		File outFile1 = new File(TAXONREPORTFILENAME);
		File outFile2 = new File(GENEREPORTFILENAME);
		File outFile3 = new File(PHENOTYPEMATCHREPORTFILENAME);
		File outFile4 = new File(PROFILEMATCHREPORTFILENAME);
		File outFile5 = new File(TAXONGENEMAXICSCOREFILENAME);
		Writer taxonWriter = null;
		Writer geneWriter = null;
		Writer phenoWriter = null;
		Writer profileWriter = null;
		Writer w5 = null;
		Date today;
		DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT);
		today = new Date();
		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
		String timeStamp = dateFormatter.format(today) + " " + timeFormatter.format(today) + " on PropTreeSymmetricTest";		
		taxonWriter = new BufferedWriter(new FileWriter(outFile1));
		geneWriter = new BufferedWriter(new FileWriter(outFile2));
		phenoWriter = new BufferedWriter(new FileWriter(outFile3));
		profileWriter = new BufferedWriter(new FileWriter(outFile4));
		w5 = new BufferedWriter(new FileWriter(outFile5));
		u.writeOrDump(timeStamp, taxonWriter);
		u.writeOrDump(timeStamp, geneWriter);
		u.writeOrDump(timeStamp, phenoWriter);
		u.writeOrDump(timeStamp, profileWriter);
		u.writeOrDump(timeStamp, w5);
		u.writeOrDump("Starting analysis: " + timeStamp, null);

		PhenotypeExpression.getEQTop(u);   //just to initialize early.

		testAnalysis.qualitySubsumers = u.buildPhenotypeSubsumers();

		testAnalysis.entityAnnotations = new EntitySet(u);
		
		testAnalysis.entityAnnotations.fillTaxonPhenotypeAnnotationsToEntities();
		int ata = testAnalysis.entityAnnotations.annotationTotal();
		Assert.assertEquals(u.countAssertedTaxonPhenotypeAnnotations(),ata);
		testAnalysis.entityAnnotations.fillGenePhenotypeAnnotationsToEntities();
		int tea = testAnalysis.entityAnnotations.annotationTotal();
		int dga = u.countDistinctGenePhenotypeAnnotations();
		Assert.assertEquals(tea, ata+dga);
		
		testAnalysis.totalAnnotations = ata + dga;

		// process taxa annotations
		
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		testAnalysis.removeSymmetricLinks(allLinks,u);
		testAnalysis.taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badTaxonQualities);
		testAnalysis.countAnnotatedTaxa(t1,t1.getRootNodeID(),testAnalysis.taxonProfiles,u);
		int eaCount = testAnalysis.countEAAnnotations(testAnalysis.taxonProfiles,u);
		u.writeOrDump("Count of distinct taxon-phenotype assertions (EQ level): " + testAnalysis.taxonPhenotypeLinkCount, taxonWriter);
		u.writeOrDump("Count of distinct taxon-phenotype assertions (EA level; not filtered for variation): " + eaCount, taxonWriter);
		u.writeOrDump("Count of annotated taxa = " + testAnalysis.annotatedTaxa, taxonWriter);
		u.writeOrDump("Count of parents of annotated taxa = " + testAnalysis.parentsOfAnnotatedTaxa, taxonWriter);

		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);

		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), testAnalysis.taxonProfiles, taxonVariation, u);
		t1.report(u, taxonWriter);
		taxonVariation.variationReport(u,taxonWriter);	
		u.writeOrDump("\nList of qualities that were placed under quality as an attribute by default\n", taxonWriter);
		for(Integer bad_id : badTaxonQualities.keySet()){
			u.writeOrDump(u.getNodeName(bad_id) + " " + badTaxonQualities.get(bad_id), taxonWriter);
		}
		testAnalysis.flushUnvaryingPhenotypes(testAnalysis.taxonProfiles,taxonVariation,u);
		Assert.assertFalse(testAnalysis.taxonProfiles.isEmpty());

		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);

		testAnalysis.geneProfiles = testAnalysis.processGeneExpression(geneVariation,u, geneWriter);
		geneVariation.variationReport(u, geneWriter);
		u.writeOrDump("\nList of qualities that were placed under quality as an attribute by default\n", geneWriter);
		for(Integer bad_id : badGeneQualities.keySet()){
			u.writeOrDump(u.getNodeName(bad_id) + " " + badGeneQualities.get(bad_id), geneWriter);
		}

		geneWriter.close();

		Map <Integer,Set<Integer>> entityParentCache = new HashMap<Integer,Set<Integer>>();
		Map <Integer,Set<Integer>> entityChildCache = new HashMap<Integer,Set<Integer>>();
		u.setupEntityParents(entityParentCache,entityChildCache);
		CountTable<Integer> geneEntityCounts = testAnalysis.fillGeneEntityCountTable(testAnalysis.geneProfiles,  entityParentCache, u,PhenotypeProfileAnalysis.GENEENTITYCOUNTQUERY , u.countDistinctGeneEntityPhenotypeAnnotations());		
		CountTable<Integer> taxonEntityCounts = testAnalysis.fillTaxonEntityCountTable(testAnalysis.taxonProfiles,  entityParentCache, u,PhenotypeProfileAnalysis.GENEENTITYCOUNTQUERY , u.countDistinctGeneEntityPhenotypeAnnotations());		
		CountTable<Integer> sumTable = geneEntityCounts.addTable(taxonEntityCounts);
		/* Test introduction of phenotypeParentCache, which should map an attribute level EQ to all its parents via inheres_in_part_of entity parents and is_a quality parents (cross product) */
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);

		CountTable<PhenotypeExpression> phenotypeCountsToUse =testAnalysis.fillPhenotypeCountTable(testAnalysis.geneProfiles, testAnalysis.taxonProfiles,phenotypeParentCache, u, GENEPHENOTYPECOUNTQUERY, GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());

		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();


		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, phenotypeCountsToUse, u);
		taxonWriter.close();
		testAnalysis.writePhenotypeMatchSummary(phenotypeScores,u,phenoWriter);		
		phenoWriter.close();


		testAnalysis.writeTaxonGeneMaxICSummary(phenotypeScores,u,w5);
		w5.close();
		//List<PermutedProfileScore> pScores = calcPermutedProfileScores(taxonProfiles,geneProfiles,entityParentCache, entityChildCache, phenotypeScores,entityAnnotations, u);
		PermutedScoreSet s = new PermutedScoreSet(testAnalysis.taxonProfiles,testAnalysis.geneProfiles,entityParentCache, entityChildCache, phenotypeScores, u);
		s.setRandom(new Random());
		s.calcPermutedProfileScores();

		s.writeDist(RANDOMIZATIONREPORTSFOLDER);
		testAnalysis.profileMatchReport(phenotypeScores,s,profileWriter,entityParentCache, entityChildCache, testAnalysis.entityAnnotations,phenotypeParentCache, u);
		
		

		testAnalysis.profileMatchReport(phenotypeScores,s,profileWriter,entityParentCache,entityChildCache,testAnalysis.entityAnnotations, phenotypeParentCache, u);
		profileWriter.close();
		
		
		taxonWriter.close();
		geneWriter.close();
		phenoWriter.close();
		profileWriter.close();
		w5.close();
	}


	@After
	public void tearDown() throws Exception {
		u.closeKB();
	}

}
