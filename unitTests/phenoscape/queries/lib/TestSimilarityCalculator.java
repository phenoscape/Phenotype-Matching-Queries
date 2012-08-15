package phenoscape.queries.lib;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import phenoscape.queries.lib.PhenotypeExpression;
import phenoscape.queries.lib.SimilarityCalculator;

public class TestSimilarityCalculator {
	
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
	public void testSimHyperSS() throws SQLException {
		List<Integer>testTaxonEntities = new ArrayList<Integer>();
		List<Integer>testGeneEntities = new ArrayList<Integer>();
		testTaxonEntities.add(1);
		testGeneEntities.add(1);
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		double result = testEntityCalculator.simHyperSS(testTaxonEntities.size(), testGeneEntities.size(),0);
		System.out.println("Pop size = 2933; taxon Entities = {1}; gene Entities = {1}");
		System.out.println("HyperSS result = " + result);

		testTaxonEntities.add(1);
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testTaxonEntities.size(), testGeneEntities.size(),0);
		System.out.println("Pop size = 2933; taxon Entities = {1,1}; gene Entities = {1}");
		System.out.println("HyperSS result = " + result);

		testTaxonEntities.clear();
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testTaxonEntities.size(), testGeneEntities.size(),0);
		System.out.println("Pop size = 2933; taxon Entities = {}; gene Entities = {1}");
		System.out.println("HyperSS result = " + result);

		
		testTaxonEntities.clear();
		testTaxonEntities.add(1);
		testGeneEntities.clear();
		testGeneEntities.add(2);
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testTaxonEntities.size(), testGeneEntities.size(),0);
		System.out.println("Pop size = 2933; taxon Entities = {1}; gene Entities = {2}");
		System.out.println("HyperSS result = " + result);
		
		testGeneEntities.add(3);
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testTaxonEntities.size(), testGeneEntities.size(),0);
		System.out.println("Pop size = 2933;  taxon Entities = {1} gene Entities = {2,3};");  //swapping
		System.out.println("HyperSS result = " + result);

		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testGeneEntities.size(), testTaxonEntities.size(),0);
		System.out.println("Pop size = 2933;  taxon Entities = {2,3} gene Entities = {1};");  //swapping
		System.out.println("HyperSS result = " + result);

		testTaxonEntities.clear();
		testTaxonEntities.add(1);
		testGeneEntities.clear();
		testGeneEntities.add(1);
		testGeneEntities.add(2);
		testGeneEntities.add(3);
		testGeneEntities.add(4);
		testGeneEntities.add(5);
		testGeneEntities.add(6);
		testGeneEntities.add(7);
		testGeneEntities.add(8);
		testGeneEntities.add(9);
		testGeneEntities.add(10);
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testGeneEntities.size(), testTaxonEntities.size(),0);
		System.out.println("Pop size = 2933;  taxon Entities = {1} gene Entities = {1,2,3,4,5,6,7,8,9,10};");  //swapping
		System.out.println("HyperSS result = " + result);

		testTaxonEntities.add(2);
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testGeneEntities.size(), testTaxonEntities.size(),0);
		System.out.println("Pop size = 2933;  taxon Entities = {1,2} gene Entities = {1,2,3,4,5,6,7,8,9,10};");  //swapping
		System.out.println("HyperSS result = " + result);

		testTaxonEntities.add(3);
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testGeneEntities.size(), testTaxonEntities.size(),0);
		System.out.println("Pop size = 2933;  taxon Entities = {1,2,3} gene Entities = {1,2,3,4,5,6,7,8,9,10};");  //swapping
		System.out.println("HyperSS result = " + result);

		testTaxonEntities.add(4);
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testGeneEntities.size(), testTaxonEntities.size(),0);
		System.out.println("Pop size = 2933;  taxon Entities = {1,2,3,4} gene Entities = {1,2,3,4,5,6,7,8,9,10};");  //swapping
		System.out.println("HyperSS result = " + result);

		//
		testTaxonEntities.clear();
		testTaxonEntities.add(11);
		
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testGeneEntities.size(), testTaxonEntities.size(),0);
		System.out.println("Pop size = 2933;  taxon Entities = {11} gene Entities = {1,2,3,4,5,6,7,8,9,10};");  //swapping
		System.out.println("HyperSS result = " + result + "\n");

		testTaxonEntities.add(12);
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testGeneEntities.size(), testTaxonEntities.size(),0);
		System.out.println("Pop size = 2933;  taxon Entities = {11,12} gene Entities = {1,2,3,4,5,6,7,8,9,10};");  //swapping
		System.out.println("HyperSS result = " + result + "\n");

		testTaxonEntities.add(13);
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testGeneEntities.size(), testTaxonEntities.size(),0);
		System.out.println("Pop size = 2933;  taxon Entities = {11,12,13} gene Entities = {1,2,3,4,5,6,7,8,9,10};");  //swapping
		System.out.println("HyperSS result = " + result + "\n");

		testTaxonEntities.add(14);
		testEntityCalculator = new SimilarityCalculator<Integer>(2933);
		result = testEntityCalculator.simHyperSS(testGeneEntities.size(), testTaxonEntities.size(),0);
		System.out.println("Pop size = 2933;  taxon Entities = {11,12,13,14} gene Entities = {1,2,3,4,5,6,7,8,9,10};");  //swapping
		System.out.println("HyperSS result = " + result + "\n");

	}

}
