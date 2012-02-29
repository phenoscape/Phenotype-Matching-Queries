package phenoscape.queries.lib;

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
	final static int ATTRIBUTE2 = 8;
	final static int ATTRIBUTE3 = 10;
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
		PhenotypeExpression tPhenotype = new PhenotypeExpression(TAXONENTITY,ATTRIBUTE);
		PhenotypeExpression gPhenotype = new PhenotypeExpression(GENEENTITY,ATTRIBUTE);		
		table1.addScore(tPhenotype,gPhenotype, SCORE1,BESTENTITY1);
	}

	@Test
	public void testIsEmpty() {
		Assert.assertTrue(table1.isEmpty());
		PhenotypeExpression tPhenotype = new PhenotypeExpression(TAXONENTITY,ATTRIBUTE);
		PhenotypeExpression gPhenotype = new PhenotypeExpression(GENEENTITY,ATTRIBUTE);		
		table1.addScore(tPhenotype,gPhenotype,SCORE1, BESTENTITY1);
		Assert.assertFalse(table1.isEmpty());
	}

	@Test
	public void testSummary() {
		PhenotypeExpression tPhenotype = new PhenotypeExpression(TAXONENTITY,ATTRIBUTE);
		PhenotypeExpression gPhenotype = new PhenotypeExpression(GENEENTITY,ATTRIBUTE);		
		table1.addScore(tPhenotype, gPhenotype, SCORE1,BESTENTITY1);
		table1.addScore(tPhenotype, gPhenotype, SCORE2,BESTENTITY2);
		table1.summary();
	}

	@Test
	public void testHasScore() {
		PhenotypeExpression tPhenotype = new PhenotypeExpression(TAXONENTITY,ATTRIBUTE);
		PhenotypeExpression gPhenotype = new PhenotypeExpression(GENEENTITY,ATTRIBUTE);
		PhenotypeExpression tPhenotype2 = new PhenotypeExpression(TAXONENTITY,ATTRIBUTE2);
		PhenotypeExpression gPhenotype2 = new PhenotypeExpression(GENEENTITY,ATTRIBUTE3);
		Assert.assertFalse(table1.hasScore(tPhenotype, gPhenotype));
		table1.addScore(tPhenotype, gPhenotype, SCORE1, BESTENTITY1);
		Assert.assertTrue(table1.hasScore(tPhenotype,gPhenotype));
		Assert.assertFalse(table1.hasScore(tPhenotype,gPhenotype2));
		Assert.assertFalse(table1.hasScore(tPhenotype2,gPhenotype));
		Assert.assertFalse(table1.hasScore(tPhenotype2,gPhenotype2));
	}

	@Test
	public void testGetScore() {
		PhenotypeExpression tPhenotype = new PhenotypeExpression(TAXONENTITY,ATTRIBUTE);
		PhenotypeExpression gPhenotype = new PhenotypeExpression(GENEENTITY,ATTRIBUTE);
		table1.addScore(tPhenotype, gPhenotype, SCORE1,BESTENTITY1);
		Assert.assertTrue(table1.getScore(tPhenotype, gPhenotype)==SCORE1);
	}

	@Test
	public void testGetBestEntity() {
		PhenotypeExpression tPhenotype = new PhenotypeExpression(TAXONENTITY,ATTRIBUTE);
		PhenotypeExpression gPhenotype = new PhenotypeExpression(GENEENTITY,ATTRIBUTE);
		table1.addScore(tPhenotype, gPhenotype, SCORE1,BESTENTITY1);
		Assert.assertTrue(table1.getBestSubsumer(tPhenotype, gPhenotype).equals(BESTENTITY1));
	}

}
