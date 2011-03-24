package phenoscape.queries.lib;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPhenotypeScoreTable {

	PhenotypeScoreTable table1;
	
	// ought to set values for these from the KB
	final static int TAXONENTITY = 1006;
	final static int GENEENTITY = 1008;
	final static int ENTITY1 = 1010;
	final static int ENTITY2 = 1021;
	final static int ATTRIBUTE = 6;
	final static double SCORE1 = 0.12;
	final static double SCORE2 = 0.024;
	final static PhenotypeExpression BESTENTITY1 = new PhenotypeExpression(ENTITY1,ATTRIBUTE);
	final static PhenotypeExpression BESTENTITY2 = new PhenotypeExpression(ENTITY2,ATTRIBUTE);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		table1 = new PhenotypeScoreTable();
	}

	@Test
	public void testAddScore() {
		table1.addScore(TAXONENTITY,GENEENTITY,ATTRIBUTE, SCORE1,BESTENTITY1);
	}

	@Test
	public void testIsEmpty() {
		Assert.assertTrue(table1.isEmpty());
		table1.addScore(TAXONENTITY,GENEENTITY,ATTRIBUTE,SCORE1, BESTENTITY1);
		Assert.assertFalse(table1.isEmpty());
	}

	@Test
	public void testSummary() {
		table1.addScore(TAXONENTITY, GENEENTITY, ATTRIBUTE, SCORE1,BESTENTITY1);
		table1.addScore(TAXONENTITY, GENEENTITY, ATTRIBUTE, SCORE2,BESTENTITY2);
		table1.summary();
	}

	@Test
	public void testHasScore() {
		Assert.assertFalse(table1.hasScore(TAXONENTITY, GENEENTITY, ATTRIBUTE));
		table1.addScore(TAXONENTITY, GENEENTITY, ATTRIBUTE, SCORE1, BESTENTITY1);
		Assert.assertTrue(table1.hasScore(TAXONENTITY, GENEENTITY, ATTRIBUTE));
		Assert.assertFalse(table1.hasScore(TAXONENTITY, GENEENTITY, 8));
		Assert.assertFalse(table1.hasScore(10010, GENEENTITY, 6));
		Assert.assertFalse(table1.hasScore(TAXONENTITY,10012, 6));
	}

	@Test
	public void testGetScore() {
		table1.addScore(TAXONENTITY, GENEENTITY, ATTRIBUTE, SCORE1,BESTENTITY1);
		Assert.assertTrue(table1.getScore(TAXONENTITY, GENEENTITY, ATTRIBUTE)==SCORE1);
	}

	@Test
	public void testGetBestEntity() {
		table1.addScore(TAXONENTITY, GENEENTITY, ATTRIBUTE, SCORE1,BESTENTITY1);
		Assert.assertTrue(table1.getBestSubsumer(TAXONENTITY, GENEENTITY, ATTRIBUTE)  == BESTENTITY1);
	}

}
