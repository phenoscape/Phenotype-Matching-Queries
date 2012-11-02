package phenoscape.queries.lib;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import phenoscape.queries.lib.PhenotypeExpression;
import phenoscape.queries.lib.SimilarityCalculator;
import junit.framework.Assert;

public class TestSimilarityCalculator {
	
	
	final static Double[] array1 = new Double[]{25.0};
	final static Double[] array2 = new Double[]{23.0,23.0};
	final static Double[] array3 = new Double[]{23.0,25.0};
	final static Double[] array4 = new Double[]{25.0,23.0};
	final static Double[] array5 = new Double[]{1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0};
	final static Double[] array6 = new Double[]{5.0,9.0,3.0,2.0,10.0,4.0,7.0,6.0,1.0,8.0};
	final static Double[] array7 = new Double[]{19.0,10.0,94.0,11.0,23.0,17.0};
	
	SimilarityCalculator<Integer> testEntityCalculator;
	SimilarityCalculator<PhenotypeExpression> testPhenotypeCalculator;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSimilarityCalculator() throws SQLException {
		testEntityCalculator = new SimilarityCalculator<Integer>(200);
		testPhenotypeCalculator = new SimilarityCalculator<PhenotypeExpression>(200);
	}
	

	@Test
	public void testQuickSelect() throws SQLException {
		Assert.assertEquals(25.0,SimilarityCalculator.select(array1,0));
		Assert.assertEquals(23.0,SimilarityCalculator.select(array2,0));
		Assert.assertEquals(23.0,SimilarityCalculator.select(array3,0));
		Assert.assertEquals(23.0,SimilarityCalculator.select(array4,0));
		Assert.assertEquals(5.0,SimilarityCalculator.select(array5,4));
		Assert.assertEquals(5.0,SimilarityCalculator.select(array6,4));
		Assert.assertEquals(17.0,SimilarityCalculator.select(array7,2));
		
	}
}
