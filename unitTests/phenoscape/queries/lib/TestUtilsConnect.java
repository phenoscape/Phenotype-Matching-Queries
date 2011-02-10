package phenoscape.queries.lib;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestUtilsConnect {

	Utils u;
	
	@Before
	public void setUp() throws Exception {
		u = new Utils();
	}

	@Test
	public void testOpenKB() throws SQLException {
		u.openKB();
	}

	@Test
	public void testCloseKB() throws SQLException {
		u.openKB();
		u.closeKB();
	}

	
	@After
	public void tearDown() throws Exception {
		u.closeKB();
	}

}
