package phenoscape.queries.lib;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTaxonPhenotypeLink {

	
	final Utils u = new Utils();
	private static final String UNITTESTKB = "unitTestconnection.properties"; 
	protected static final String TAXON1STR = "TTO:0000004";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();   //prevent complaints by log4j
		u.openKBFromConnections(UNITTESTKB);
	}

	@After
	public void tearDown() throws Exception {
	}

	private static final String NODEQUERY = "SELECT n.node_id FROM node AS n WHERE n.uid = ?";

	@Test
	public void testQuery() throws Exception {
			int taxon1id = -1;
			PreparedStatement p = u.getPreparedStatement(NODEQUERY);
			p.setString(1,TAXON1STR);
			ResultSet r = p.executeQuery();
			if (r.next()){
				taxon1id = r.getInt(1);
			}
			else{
				fail("Couldn't find node for " + TAXON1STR);
			}
			final PreparedStatement p2 = u.getPreparedStatement(TaxonPhenotypeLink.getQuery());
			System.out.println(TaxonPhenotypeLink.getQuery());
			p2.setInt(1, taxon1id);
			ResultSet r2 = p2.executeQuery();
			while (r2.next()){
				TaxonPhenotypeLink foo = new TaxonPhenotypeLink(r2);
				System.out.println("Related entity id is " + foo.relatedEntityNodeID);
			}
	}
}
