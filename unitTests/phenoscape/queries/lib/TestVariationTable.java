package phenoscape.queries.lib;


import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import phenoscape.queries.lib.VariationTable;

public class TestVariationTable {

	VariationTable testTable;
	
	int testAttribute1 = 7;
	int testAttribute2 = 9;
	
	int testEntity1 = 4001;
	int testEntity2 = 4002;
	
	int testTaxon1 = 1506;
	int testTaxon2 = 1507;
	int testTaxon3 = 1508;
	
	static Set<Integer> testSet1 = new HashSet<Integer>();
	static Set<Integer> testSet2 = new HashSet<Integer>();

	
	
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testSet1.add(2030);
		testSet1.add(2031);
	}

	@Before
	public void setUp() throws Exception {
		testTable = new VariationTable();
	}

	
	@Test
	public void testAddTaxon() {
		testTable.addExhibitor(testAttribute1, testEntity1, testTaxon1);
		testTable.addExhibitor(testAttribute1, testEntity1, testTaxon2);
		Assert.assertTrue(testTable.hasExhibitorSet(testAttribute1, testEntity1));
		Assert.assertFalse(testTable.hasExhibitorSet(testAttribute2, testEntity1));
		Assert.assertFalse(testTable.hasExhibitorSet(testAttribute1, testEntity2));
	}

	
	@Test
	public void testHasTaxonSet() {
		testTable.addExhibitor(testAttribute1, testEntity1, testTaxon1);
		testTable.addExhibitor(testAttribute1, testEntity1, testTaxon2);
		Assert.assertTrue(testTable.hasExhibitorSet(testAttribute1, testEntity1));
		Assert.assertFalse(testTable.hasExhibitorSet(testAttribute2, testEntity1));
		Assert.assertFalse(testTable.hasExhibitorSet(testAttribute1, testEntity2));
	}

	@Test
	public void testGetTaxonSet() {
		testTable.addExhibitor(testAttribute1, testEntity1, testTaxon1);
		testTable.addExhibitor(testAttribute1, testEntity1, testTaxon2);
		Assert.assertTrue(testTable.hasExhibitorSet(testAttribute1, testEntity1));
		Set<Integer> result = testTable.getExhibitorSet(testAttribute1, testEntity1);
		Assert.assertEquals(2, result.size());
		Assert.assertTrue(result.contains(testTaxon1));
		Assert.assertTrue(result.contains(testTaxon2));
		Assert.assertFalse(result.contains(testTaxon3));
		Integer testTaxon1shadow = new Integer(1506);
		Assert.assertTrue(result.contains(testTaxon1shadow));
	}

	@Test
	public void testGetUsedAttributes() {
		testTable.addExhibitor(testAttribute1, testEntity1, testTaxon1);
		testTable.addExhibitor(testAttribute1, testEntity1, testTaxon2);
		testTable.addExhibitor(testAttribute2, testEntity2, 2030);
		testTable.addExhibitor(testAttribute2, testEntity2, 2031);
		Set<Integer> attributeList = testTable.getUsedAttributes();
		Assert.assertNotNull(attributeList);
		Assert.assertEquals(2, attributeList.size());
		Assert.assertTrue(attributeList.contains(testAttribute1));
		Assert.assertTrue(attributeList.contains(testAttribute2));
		Assert.assertFalse(attributeList.contains(testEntity1));
		Assert.assertFalse(attributeList.contains(testEntity2));
	}

	@Test
	public void testGetUsedEntities() {
		testTable.addExhibitor(testAttribute1, testEntity1, testTaxon1);
		testTable.addExhibitor(testAttribute1, testEntity1, testTaxon2);
		testTable.addExhibitor(testAttribute2, testEntity2, 2030);
		testTable.addExhibitor(testAttribute2, testEntity2, 2031);
		Set<Integer> entityList = testTable.getUsedEntities();
		Assert.assertNotNull(entityList);
		Assert.assertEquals(2, entityList.size());
		Assert.assertFalse(entityList.contains(testAttribute1));
		Assert.assertFalse(entityList.contains(testAttribute2));
		Assert.assertTrue(entityList.contains(testEntity1));
		Assert.assertTrue(entityList.contains(testEntity2));
	}


	
}
