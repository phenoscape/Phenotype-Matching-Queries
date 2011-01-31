package phenoscape.queries.lib;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestProfile {

	Profile testProfile;
	
	int testAttribute1 = 7;
	int testAttribute2 = 9;
	
	int testEntity1 = 4001;
	int testEntity2 = 4002;
	
	int testPhenotype1 = 1506;
	int testPhenotype2 = 1507;
	int testPhenotype3 = 1508;
	
	static Set<Integer> testSet1 = new HashSet<Integer>();
	static Set<Integer> testSet2 = new HashSet<Integer>();
	static Set<Integer> testSet3 = new HashSet<Integer>();
	static Set<Integer> testSet4 = new HashSet<Integer>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testSet1.add(2030);
		testSet2.add(2031);
		testSet3.add(2032);
		testSet3.add(2033);
		testSet4.add(2033);
	}

	@Before
	public void setUp() throws Exception {
		testProfile = new Profile();
	}

	@Test
	public void testAddPhenotype() {
		testProfile.addPhenotype(testAttribute1, testEntity1, testPhenotype1);
		testProfile.addPhenotype(testAttribute1, testEntity1, testPhenotype2);
		Assert.assertTrue(testProfile.hasPhenotypeSet(testAttribute1, testEntity1));
		Assert.assertFalse(testProfile.hasPhenotypeSet(testAttribute2, testEntity1));
		Assert.assertFalse(testProfile.hasPhenotypeSet(testAttribute1, testEntity2));
	}

	@Test
	public void testAddAlltoPhenotypeSet() {
		testProfile.addAlltoPhenotypeSet(testAttribute2, testEntity2, testSet1);
		Assert.assertEquals(1,testProfile.getPhenotypeSet(testAttribute2,testEntity2).size());
		testProfile.addAlltoPhenotypeSet(testAttribute2, testEntity2, testSet2);
		Assert.assertEquals(2,testProfile.getPhenotypeSet(testAttribute2,testEntity2).size());
		Assert.assertTrue(testProfile.hasPhenotypeSet(testAttribute2, testEntity2));
	}

	@Test
	public void testHasPhenotypeSet() {
		testProfile.addPhenotype(testAttribute1, testEntity1, testPhenotype1);
		testProfile.addPhenotype(testAttribute1, testEntity1, testPhenotype2);
		Assert.assertTrue(testProfile.hasPhenotypeSet(testAttribute1, testEntity1));
		Assert.assertFalse(testProfile.hasPhenotypeSet(testAttribute2, testEntity1));
		Assert.assertFalse(testProfile.hasPhenotypeSet(testAttribute1, testEntity2));
	}

	@Test
	public void testGetPhenotypeSet() {
		testProfile.addPhenotype(testAttribute1, testEntity1, testPhenotype1);
		testProfile.addPhenotype(testAttribute1, testEntity1, testPhenotype2);
		Assert.assertTrue(testProfile.hasPhenotypeSet(testAttribute1, testEntity1));
		Set<Integer> result = testProfile.getPhenotypeSet(testAttribute1, testEntity1);
		Assert.assertEquals(2, result.size());
		Assert.assertTrue(result.contains(testPhenotype1));
		Assert.assertTrue(result.contains(testPhenotype2));
		Assert.assertFalse(result.contains(testPhenotype3));
		Integer testPhenotype1shadow = new Integer(1506);
		Assert.assertTrue(result.contains(testPhenotype1shadow));
	}

	@Test
	public void testGetUsedAttributes() {
		testProfile.addPhenotype(testAttribute1, testEntity1, testPhenotype1);
		testProfile.addPhenotype(testAttribute1, testEntity1, testPhenotype2);
		testProfile.addAlltoPhenotypeSet(testAttribute2, testEntity2, testSet1);
		Set<Integer> attributeList = testProfile.getUsedAttributes();
		Assert.assertNotNull(attributeList);
		Assert.assertEquals(2, attributeList.size());
		Assert.assertTrue(attributeList.contains(testAttribute1));
		Assert.assertTrue(attributeList.contains(testAttribute2));
		Assert.assertFalse(attributeList.contains(testEntity1));
		Assert.assertFalse(attributeList.contains(testEntity2));
	}

	@Test
	public void testGetUsedEntities() {
		testProfile.addPhenotype(testAttribute1, testEntity1, testPhenotype1);
		testProfile.addPhenotype(testAttribute1, testEntity1, testPhenotype2);
		testProfile.addAlltoPhenotypeSet(testAttribute2, testEntity2, testSet1);
		Set<Integer> entityList = testProfile.getUsedEntities();
		Assert.assertNotNull(entityList);
		Assert.assertEquals(2, entityList.size());
		Assert.assertFalse(entityList.contains(testAttribute1));
		Assert.assertFalse(entityList.contains(testAttribute2));
		Assert.assertTrue(entityList.contains(testEntity1));
		Assert.assertTrue(entityList.contains(testEntity2));
	}
	
	@Test
	public void testGetAllPhenotypes(){
		testProfile.addPhenotype(testAttribute1, testEntity1, testPhenotype1);
		testProfile.addPhenotype(testAttribute1, testEntity1, testPhenotype2);
		testProfile.addAlltoPhenotypeSet(testAttribute2, testEntity2, testSet3);
		Set<Integer> allPhenotypes = testProfile.getAllPhenotypes();
		Assert.assertEquals(4,allPhenotypes.size());
		Assert.assertTrue(allPhenotypes.contains(testPhenotype1));
		Assert.assertTrue(allPhenotypes.contains(testPhenotype2));
	}

}
