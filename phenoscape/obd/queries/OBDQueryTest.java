package phenoscape.obd.queries;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.obd.query.impl.OBDSQLShard;

public class OBDQueryTest {

	OBDSQLShard myShard;
	private static final String CONNECTION_PROPERTIES_FILENAME = "connection.properties"; 



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OBDQueryTest foo = new OBDQueryTest();
		foo.run();
		// TODO Auto-generated method stub

	}
	
	private void run(){
		myShard = new OBDSQLShard();
		if (!openKB(myShard))
			return;
		
	}
	
	private boolean openKB(OBDSQLShard s){
		final Properties properties = new Properties();
		try {
			properties.load(this.getClass().getResourceAsStream(CONNECTION_PROPERTIES_FILENAME));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		try {
			Class.forName("org.postgresql.Driver");
		} catch(ClassNotFoundException e){
			System.err.println("Couldn't load PSQL Driver");
			e.printStackTrace();
		}
		Connection c= null;
		final String host = properties.getProperty("host");
		final String db = properties.getProperty("db");
		final String user = properties.getProperty("user");
		final String password = properties.getProperty("pw");
		try{
			s.connect(String.format("jdbc:postgresql://%s/%s",host,db), user, password);
			return true;
		} catch (SQLException e){
			System.err.println("Cound't connect to server");
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}


}
