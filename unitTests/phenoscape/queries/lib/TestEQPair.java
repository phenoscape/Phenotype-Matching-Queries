package phenoscape.queries.lib;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestEQPair {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEQPair() {
		EQPair test = new EQPair(1006,70);
		Assert.assertNotNull(test);
	}
	
	@Test (expected = IllegalArgumentException.class) public void testBadEntityConstructor(){
		EQPair test = new EQPair(-1006,70);
	}

	@Test (expected = IllegalArgumentException.class) public void testBadQualityConstructor(){
		EQPair test = new EQPair(1006,-70);
		Assert.assertNotNull(test);		
	}

	@Test
	public void testGetEntity() {
		EQPair test = new EQPair(1006,70);
		Assert.assertEquals(1006,test.getEntity());
	}

	@Test
	public void testGetQuality() {
		EQPair test = new EQPair(1006,70);
		Assert.assertEquals(70,test.getQuality());
	}

	@Test
	public void testEqualsObject() {
		EQPair test1 = new EQPair(1006,70);
		EQPair test1b = new EQPair(1006,70);
		EQPair test2 = new EQPair(1008,70);
		EQPair test3 = new EQPair(1006,52);
		EQPair test4 = new EQPair(1008,52);
		EQPair test4b = new EQPair(1008,52);
		Assert.assertFalse(test1.equals(test2));
		Assert.assertFalse(test1.equals(test3));
		Assert.assertFalse(test1.equals(test4));
		Assert.assertFalse(test2.equals(test1));
		Assert.assertFalse(test2.equals(test3));
		Assert.assertFalse(test2.equals(test4));
		Assert.assertFalse(test3.equals(test1));
		Assert.assertFalse(test3.equals(test2));
		Assert.assertFalse(test3.equals(test4));
		Assert.assertFalse(test4.equals(test1));
		Assert.assertFalse(test4.equals(test2));
		Assert.assertFalse(test4.equals(test3));
		Assert.assertTrue(test1.equals(test1));
		Assert.assertTrue(test2.equals(test2));
		Assert.assertTrue(test3.equals(test3));
		Assert.assertTrue(test4.equals(test4));
		Assert.assertTrue(test1.equals(test1b));
		Assert.assertTrue(test1b.equals(test1));
		Assert.assertTrue(test4.equals(test4b));
		
	}
	
	@Test
	public void testHashCode() {
		EQPair test1 = new EQPair(1006,70);
		EQPair test1b = new EQPair(1006,70);
		EQPair test2 = new EQPair(1008,70);
		EQPair test3 = new EQPair(1006,52);
		EQPair test4 = new EQPair(1008,52);
		EQPair test4b = new EQPair(1008,52);
		Assert.assertFalse(test1.hashCode() == test2.hashCode());
		Assert.assertFalse(test1.hashCode() == test3.hashCode());
		Assert.assertFalse(test1.hashCode() == test4.hashCode());
		Assert.assertFalse(test2.hashCode() == test1.hashCode());
		Assert.assertFalse(test2.hashCode() == test3.hashCode());
		Assert.assertFalse(test2.hashCode() == test4.hashCode());
		Assert.assertFalse(test3.hashCode() == test1.hashCode());
		Assert.assertFalse(test3.hashCode() == test2.hashCode());
		Assert.assertFalse(test3.hashCode() == test4.hashCode());
		Assert.assertFalse(test4.hashCode() == test1.hashCode());
		Assert.assertFalse(test4.hashCode() == test2.hashCode());
		Assert.assertFalse(test4.hashCode() == test3.hashCode());
		Assert.assertTrue(test1.hashCode() == test1.hashCode());
		Assert.assertTrue(test2.hashCode() == test2.hashCode());
		Assert.assertTrue(test3.hashCode() == test3.hashCode());
		Assert.assertTrue(test4.hashCode() == test4.hashCode());
	}

	@Test
	public void testEQSet(){
		EQPair test1 = new EQPair(1006,70);
		EQPair test1b = new EQPair(1006,70);
		EQPair test2 = new EQPair(1008,70);
		EQPair test3 = new EQPair(1006,52);
		EQPair test4 = new EQPair(1008,52);
		Set<EQPair> testSet1 = new HashSet<EQPair>();
		testSet1.add(test1);
		Assert.assertTrue(testSet1.contains(test1));
		testSet1.add(test2);
		testSet1.add(test3);
		testSet1.add(test4);
		Assert.assertEquals(4,testSet1.size());
		testSet1.add(test1b);
		Assert.assertEquals(4,testSet1.size());
		
	}

	@Test
	public void testEQMap(){
		
	}
	
}
