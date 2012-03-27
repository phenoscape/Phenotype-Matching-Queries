package phenoscape.queries.lib;



import java.sql.SQLException;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestEntitySet {
	
	protected static final String UNITTESTKB = "unitTestconnection.properties"; 

	private final Utils testUtils = new Utils();
	private EntitySet testSet;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();   //prevent complaints by log4j
		testUtils.openKBFromConnections(UNITTESTKB);
		testSet = new EntitySet(testUtils);
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void testFillTaxonPhenotypeAnnotationsToEntities() throws SQLException {
		testSet.fillTaxonPhenotypeAnnotationsToEntities();
		Assert.assertEquals(6,testSet.size());
		Assert.assertEquals(43,testSet.annotationTotal());
	}

	@Test
	public void testFillGenePhenotypeAnnotationsToEntities() throws SQLException {
		testSet.fillGenePhenotypeAnnotationsToEntities();
		Assert.assertEquals(5,testSet.size());
		Assert.assertEquals(23,testSet.annotationTotal());
	}

	@Test
	public void testFillTaxonGenePhenotypeAnnotationsToEntities() throws SQLException {
		testSet.fillTaxonPhenotypeAnnotationsToEntities();
		testSet.fillGenePhenotypeAnnotationsToEntities();
		Assert.assertEquals(9,testSet.size());  //not 11
		Assert.assertEquals(66,testSet.annotationTotal());
	}

	
}
