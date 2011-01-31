//package phenoscape.shard.queries;
//
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.util.Collection;
//import java.util.Properties;
//
//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;
//import org.obd.model.Statement;
//import org.obd.model.vocabulary.TermVocabulary;
//import org.obd.query.Shard;
//import org.obd.query.impl.OBDSQLShard;
//import org.purl.obo.vocab.RelationVocabulary;
//
//public class ShardQuery {
//	protected Shard shard;
//	private static final String CONNECTION_PROPERTIES_FILENAME = "connection.properties"; 
//	protected RelationVocabulary relationVocabulary = new RelationVocabulary();
//	protected TermVocabulary termVocabulary = new TermVocabulary();
//
//	static Logger logger = Logger.getLogger(ShardQuery.class.getName());
//
//	
//	public ShardQuery() throws SQLException, ClassNotFoundException {
//		super();
//		BasicConfigurator.configure();
//		OBDSQLShard obd = new OBDSQLShard();
//		final Properties properties = new Properties();
//		try {
//			//System.out.println("Connect path = " + this.getClass().getResource(CONNECTION_PROPERTIES_FILENAME));
//			properties.load(this.getClass().getResourceAsStream(CONNECTION_PROPERTIES_FILENAME));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} 
//		try {
//			Class.forName("org.postgresql.Driver");
//		} catch(ClassNotFoundException e){
//			System.err.println("Couldn't load PSQL Driver");
//			e.printStackTrace();
//		}
//		final String host = properties.getProperty("host");
//		final String db = properties.getProperty("db");
//		final String user = properties.getProperty("user");
//		final String password = properties.getProperty("pw");
//		obd.connect(String.format("jdbc:postgresql://%s/%s",host,db),user,password);
//		shard = obd;
//		String testNode = "TTO:103";
//		shard.includesEntailedStatements();
//		Collection<Statement> s = shard.getNonRedundantStatementsForNode(testNode);
//		for(Statement st : s){
//			if (!st.isInferred())
//				logger.info("Statement is " + st.getNodeId() + "; " + st.getRelationId() + "; " + st.getTargetId());
//		}
//		System.out.println("Collection for " + testNode + " size is " + s.size());
//		testNode = "TAO:0000103";
//		s = shard.getNonRedundantStatementsForNode(testNode);
//		for(Statement st : s){
//			logger.info("Statement is " + st.getNodeId() + "; " + st.getRelationId() + "; " + st.getTargetId());
//		}
//		System.out.println("Collection for " + testNode + " size is " + s.size());
//
//	}
//
//	public static void main(String[] args) {
//		try {
//			ShardQuery query = new ShardQuery();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//
//}
