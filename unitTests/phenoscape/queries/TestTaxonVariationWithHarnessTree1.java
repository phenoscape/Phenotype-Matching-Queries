package phenoscape.queries;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import phenoscape.queries.PhenotypeProfileAnalysis.TaxonPhenotypeLink;
import phenoscape.queries.TaxonomyTree.TaxonomicNode;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;

public class TestTaxonVariationWithHarnessTree1 {

	
	static final int OPERCLENODEID = 101;
	static final int QUALITYNODEID = 200;
	static final int SHAPENODEID = 201;
	static final int ROUNDNODEID = 202;
	static final int RECTANGULARNODEID = 203;
	static final int ELLIPTICNODEID = 204;
	static final int SQUARENODEID = 205;
	
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
	
	TaxonomyTree t1;
	
	Map<Integer,Integer> attMap;
	Map<Integer,Integer> badQualities;
	PhenotypeProfileAnalysis a;
	Map<Integer,Collection<TaxonPhenotypeLink>> testLinks;



	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();   //prevent complaints by log4j
		u = new Utils();
		//u.openKBFromConnections(UNITTESTKB);
		//u.openKB();

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
		
		t1 = new TaxonomyTree(rt,u);
		assertNotNull(t1);
		t1.traverseOntologyTreeUsingTaxonNodes(testTable,u);
		t1.report(u,null);
		
		a = new PhenotypeProfileAnalysis(u);

		attMap = a.getAttributeMap();
		badQualities = new HashMap<Integer,Integer>();
		Collection<TaxonPhenotypeLink> links = new HashSet<TaxonPhenotypeLink>();
		TaxonPhenotypeLink s1p1 = new TaxonPhenotypeLink();
		s1p1.setTaxonNodeID(7);
		s1p1.setEntityNodeID(OPERCLENODEID);
		s1p1.setQualityNodeID(ROUNDNODEID);
		links.add(s1p1);
		
		TaxonPhenotypeLink s2p1 = new TaxonPhenotypeLink();
		s2p1.setTaxonNodeID(8);
		s2p1.setEntityNodeID(OPERCLENODEID);
		s2p1.setQualityNodeID(ROUNDNODEID);
		links.add(s2p1);

		TaxonPhenotypeLink s3p2 = new TaxonPhenotypeLink();
		s3p2.setTaxonNodeID(9);
		s3p2.setEntityNodeID(OPERCLENODEID);
		s3p2.setQualityNodeID(RECTANGULARNODEID);
		links.add(s3p2);

		TaxonPhenotypeLink s4p1 = new TaxonPhenotypeLink();
		s4p1.setTaxonNodeID(10);
		s4p1.setEntityNodeID(OPERCLENODEID);
		s4p1.setQualityNodeID(ROUNDNODEID);
		links.add(s4p1);
		
		TaxonPhenotypeLink s5p2 = new TaxonPhenotypeLink();
		s5p2.setTaxonNodeID(11);
		s5p2.setEntityNodeID(OPERCLENODEID);
		s5p2.setQualityNodeID(RECTANGULARNODEID);
		links.add(s5p2);

		TaxonPhenotypeLink s6p3 = new TaxonPhenotypeLink();
		s6p3.setEntityNodeID(OPERCLENODEID);
		s6p3.setQualityNodeID(ELLIPTICNODEID);
		s6p3.setTaxonNodeID(12);
		links.add(s6p3);

		TaxonPhenotypeLink s7p3 = new TaxonPhenotypeLink();
		s7p3.setEntityNodeID(OPERCLENODEID);
		s7p3.setQualityNodeID(ELLIPTICNODEID);
		s7p3.setTaxonNodeID(13);
		links.add(s7p3);
		
		testLinks = fillTestLinks(links);
				
		attMap = new HashMap<Integer,Integer>();
		attMap.put(QUALITYNODEID,QUALITYNODEID);
		attMap.put(SHAPENODEID,SHAPENODEID);
		attMap.put(ROUNDNODEID,SHAPENODEID);
		attMap.put(RECTANGULARNODEID,SHAPENODEID);
		attMap.put(ELLIPTICNODEID,SHAPENODEID);
		attMap.put(SQUARENODEID,SHAPENODEID);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testProcessTaxonVariation() throws SQLException {
		//Map<Integer,Collection<TaxonPhenotypeLink>> allLinks = a.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile> result = a.loadTaxonProfiles(testLinks, u, attMap, 200, badQualities);	
		assertEquals(7,result.size());
		Map<Integer,Profile>testProfiles = new HashMap<Integer,Profile>();
		VariationTable vt = new VariationTable(VariationTable.VariationType.TAXON);
		a.traverseTaxonomy(t1, t1.getRootNodeID(), testProfiles, vt, u);

	}

	
	private Map<Integer,Collection<TaxonPhenotypeLink>>fillTestLinks(Collection<TaxonPhenotypeLink> links){
		Map<Integer,Collection<TaxonPhenotypeLink>> result = new HashMap<Integer,Collection<TaxonPhenotypeLink>>();
		for(TaxonPhenotypeLink link : links){
			int taxon = link.getTaxonNodeID();
			if (result.containsKey(taxon)){
				Collection<TaxonPhenotypeLink> linkSet = result.get(link.getTaxonNodeID());
				linkSet.add(link);
			}
			else {
				Collection<TaxonPhenotypeLink> linkSet = new HashSet<TaxonPhenotypeLink>();
				linkSet.add(link);
				result.put(link.getTaxonNodeID(),linkSet);
			}
		}
		return result;
	}
	
	
	
	
}
