package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import phenoscape.queries.lib.Match;
import phenoscape.queries.lib.PhenoRec;
import phenoscape.queries.lib.Utils;

public class KBTestQuery {

	private static final String CONNECTION_PROPERTIES_FILENAME = "connection.properties"; 


	private static final String REPORTFILENAME = "TaxonGeneEQTestMatches.txt";

	final Collection<PhenoRec>genePhenos = new HashSet<PhenoRec>(30000);
	final Collection<PhenoRec>taxonPhenos = new HashSet<PhenoRec>(600000);
	final List<Match>matchList = new ArrayList<Match>();
	
	private int isa_id;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		KBTestQuery testQuery = new KBTestQuery();
		testQuery.test(new Utils());
	}

	private void test(Utils u){
		Connection c = openKB();
		if (c == null)
			return;
		try{
			final Statement s = c.createStatement();
			ResultSet rs1 = s.executeQuery("select * from distinct_gene_annotation AS dga;");
			while(rs1.next()){
				final int geneID = rs1.getInt(1);         		//gene_node_id
				final String geneUID = rs1.getString(2);  		//gene_uid
				final String geneLabel = rs1.getString(3);  	//gene_label
				final int phenotypeID = rs1.getInt(5);      	//phenotype_node_id
				final String phenotypeUID = rs1.getString(6);	//phenotype_uid
				final int entityID = rs1.getInt(8);	 			//entity_node_id
				final String entityUID = rs1.getString(9);		//entity_uid
				final String entityLabel = rs1.getString(10);	//entity_label
				final int qualityID = rs1.getInt(11);			//quality_id
				final String qualityUID = rs1.getString(12);	//quality_uid
				final String qualityLabel = rs1.getString(13);	//quality_label
				final int relatedID = rs1.getInt(14);			//related_entity_node_id
				final String relatedUID = rs1.getString(15);	//related_entity_uid
				final String relatedLabel = rs1.getString(16);	//related_entity_label
				genePhenos.add(PhenoRec.makeGenePheno(geneID,
						phenotypeID,
						phenotypeUID,
						entityID,
						entityUID,
						qualityID,
						qualityUID));
				if (!u.hasNodeName(geneID)){
					u.putNodeUIDName(geneID,geneUID,geneLabel);
				}
				if (!u.hasNodeUID(entityID) ){
					u.putNodeUIDName(entityID,entityUID,entityLabel);
				}
				if (!u.hasNodeUID(qualityID)){
					u.putNodeUIDName(qualityID,qualityUID,qualityLabel);
				}
			}
			ResultSet rs2 = s.executeQuery("select ata.taxon_node_id,ata.phenotype_node_id,p.* from asserted_taxon_annotation AS ata join phenotype as p on (ata.phenotype_node_id = p.node_id);");
			while(rs2.next()){
				taxonPhenos.add(PhenoRec.makeTaxonPheno(rs2.getInt(1),   //column for taxon_node_id
						rs2.getInt(2),                      //column for phenotype_nod_id
						rs2.getString("uid"),
						rs2.getInt("entity_node_id"),
						rs2.getString("entity_uid"),
						rs2.getInt("quality_node_id"),
						rs2.getString("quality_uid")));
			}
			System.out.println("Finished Phenotypes");
			// Taxon labels need a separate pass because asserted_taxon_annotation is just node_ids
			PreparedStatement ts = c.prepareStatement("select uid,label from taxon where node_id = ?");
			for(PhenoRec tp : taxonPhenos){
				final int taxonID = tp.getExhibitorNodeID();
				if (!u.hasNodeName(taxonID)){
					ts.setInt(1,taxonID);
					ResultSet tr = ts.executeQuery();
					if (tr.next()){
						u.putNodeUIDName(taxonID, tr.getString(1),tr.getString(2));  //column for "label"
					}
					tr.close();
				}
			}
			ts.close();
			System.out.println("Finished loading labels");
			ResultSet rs3a = s.executeQuery("select node_id,label from node where label = 'is_a'");
			if (rs3a.next())
				isa_id = rs3a.getInt(1);
			else{
				System.err.println("Couldn't find the ID for is_a predicate");
				return;
			}
			PreparedStatement ps = c.prepareStatement("select node_id,predicate_id,object_id,is_negated,is_obsolete from link where predicate_id = ? AND is_negated = FALSE AND is_obsolete = FALSE");
			ps.setInt(1, isa_id);
			ResultSet rs3 = ps.executeQuery();
			int parentCount = 0;

			while(rs3.next()){
				u.addParent(rs3.getInt(1), rs3.getInt(3));
				parentCount++;
			}
			ps.close();
			System.out.println("Finished loading parents");
			u.printTableReport();
			System.out.println("Parent Count is " + parentCount);
		} catch (SQLException e){
			System.err.println("Problem with query");
			e.printStackTrace();
		}
		finally{
			try {
				c.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		long matchCount1 = 0;
		long matchCount2 = 0;
		long eqParentMatchCount = 0;
		long entityParentMatchCount = 0;
		// need to test for inheres_in (depth 0) inheres_in_is_a and inheres_in_part_of (depth 1) or inheres_in_is_a_is_a, inheres_in_partof_is_a, inheres_in_part_of_part_of
		for(PhenoRec gp : genePhenos){
			for(PhenoRec tp : taxonPhenos){
				if (gp.uidMatchPhenotype(tp)){
					matchList.add(new Match(tp,gp));
					matchCount1++;
				}
//				else if (gp.matchEntityUIDs(tp) && gp.matchQualityUIDs(tp)){
//					matchList.add(new Match(Match.MatchType.ENTITY_QUALITY_INDEPENDENTLY,tp,gp));
//					System.out.println("Taxon: " + tp.getUID() + "; Gene: " + gp.getUID());
//					matchCount2++;
//				}
				//				else if (gp.matchEntityParentIDs(tp,u) && gp.matchQualityIDs(tp)){
//					matchList.add(new Match(Match.MatchType.ENTITY_PARENT,tp,gp));
//					entityParentMatchCount++;
//				}
//				else if (gp.matchPhenotypeParentIDs(tp,u)){
//					matchList.add(new Match(Match.MatchType.EQ_PARENT,tp,gp));
//					eqParentMatchCount++;						
//				}
//				if (matchCount+entityParentMatchCount+eqParentMatchCount > 500)  //escape clause for testing
//					break;
			}
		}

		System.out.println("gCount is " + genePhenos.size());
		System.out.println("tCount is " + taxonPhenos.size());
		System.out.println("Match Count1 is " + matchCount1);
		System.out.println("Match Count2 is " + matchCount2);
		System.out.println("EQ Parent Match count is " + eqParentMatchCount);
		System.out.println("Entity Parent Match Count is " + entityParentMatchCount);
		
		System.out.println("\nWriting to report");
		
		File outFile = new File(REPORTFILENAME);
		BufferedWriter bw;

		try {
			bw = new BufferedWriter(new FileWriter(outFile));
			//bw = null;
			writeOrDump("Taxon\tGene\tTaxon Entity\tTaxon Quality\tGene Entity\tGene Quality\tMatch Type",bw);
			for(Match m : matchList){
				writeOrDump(m.reportWithNames(u),bw);
			}
			if (bw != null)
				bw.close();
		} catch(IOException e){

		}

	}

	private Connection openKB(){
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
			c = DriverManager.getConnection(String.format("jdbc:postgresql://%s/%s",host,db),user,password);
			return c;
		} catch (SQLException e){
			System.err.println("Cound't connect to server");
			e.printStackTrace();
			return null;
		}

	}

	private void writeOrDump(String contents, BufferedWriter b){
		if (b == null)
			System.out.println(contents);
		else {
			try {
				b.write(contents);
				b.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}



}
