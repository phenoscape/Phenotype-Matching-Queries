package phenoscape.queries.lib;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPhenotypeScoreTable {

	PhenotypeScoreTable table1;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		table1 = new PhenotypeScoreTable();
	}

	@Test
	public void testAddScore() {
		table1.addScore(1006,6, 4.5);
	}

	@Test
	public void testIsEmpty() {
		Assert.assertTrue(table1.isEmpty());
		table1.addScore(1006,6, 4.5);
		Assert.assertFalse(table1.isEmpty());
	}

	@Test
	public void testSummary() {
		table1.addScore(1006,6, 4.5);
		table1.addScore(1006,7, 5.9);
		table1.summary();
	}

	@Test
	public void testHasScore() {
		Assert.assertFalse(table1.hasScore(1006, 6));
		table1.addScore(1006,6, 4.5);
		Assert.assertTrue(table1.hasScore(1006, 6));
		Assert.assertFalse(table1.hasScore(1006, 8));
		Assert.assertFalse(table1.hasScore(10010, 6));
	}

	@Test
	public void testGetScore() {
		table1.addScore(1006,6, 4.5);
		Assert.assertTrue(table1.getScore(1006, 6)==4.5);
	}

}
