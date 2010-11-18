package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import phenoscape.queries.lib.Match;
import phenoscape.queries.lib.Utils;

public class PhenotypeReport {


	public final static String REPORTFILENAME = "Phenotype_Report.txt";
	
	final static String DELIMITER = "\t";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PhenotypeReport report = new PhenotypeReport();
		Utils u = new Utils();
		Connection c = u.openKB();
		Map<Integer,String> uidCache = new HashMap<Integer,String>();
		File outFile = new File(REPORTFILENAME);
		BufferedWriter bw;

		try{
			bw = new BufferedWriter(new FileWriter(outFile));

			report.generate(u, c, uidCache, bw);
		} catch (SQLException e){
			System.err.println("Problem with query");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Problem opening report file");
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


	}

	private void generate(Utils u, Connection c, Map<Integer, String> uIDCache, BufferedWriter bw)  throws SQLException{
		if (c == null)
			return;
		final Statement s = c.createStatement();
		final PreparedStatement tc = c.prepareStatement("SELECT node_id,uid where node_id = ?");


		ResultSet rs1 = s.executeQuery("SELECT DISTINCT " + 
				"publication.label, " +
				"character.character_number, " +
				"character.label, " +
				"state.label, " +
				"phenotype.entity_uid, " +
				"phenotype.entity_label, " +
				"phenotype.quality_uid, " +
				"phenotype.quality_label, " +
				"phenotype.related_entity_uid, " +
				"phenotype.related_entity_label, "+
				"attribute.label, " +
				"attribute.uid, " + 
				"character.comment, " +
				"state.comment " +
				"FROM " +
				"annotation_source " +
				"JOIN taxon_annotation ON (taxon_annotation.annotation_id = annotation_source.annotation_id) " +
				"JOIN node publication ON (publication.node_id = annotation_source.publication_node_id) " +
				"JOIN character ON (character.node_id = annotation_source.character_node_id) " +
				"JOIN state ON (state.node_id = annotation_source.state_node_id) " +	
				"JOIN phenotype ON (phenotype.node_id = taxon_annotation.phenotype_node_id) " +
				"LEFT JOIN quality_to_attribute ON (quality_to_attribute.quality_node_id = phenotype.node_id) " +
		        "LEFT JOIN node attribute ON (attribute.node_id = quality_to_attribute.attribute_node_id)");
		u.writeOrDump("label\tcharacter_number\tcharacter\tcharacter_comment\tstate\tstate_comment\tentity_label\tquality_label\trelated_entity_label\tattribute",bw);
		while(rs1.next()){
			final String publication_label = rs1.getString(1);    
			final String character_number = rs1.getString(2);  
			final String character_label = rs1.getString(3);  	
			final String state_label = rs1.getString(4);  	   
			final String phenotype_entity_uid = rs1.getString(5);
			final String phenotype_entity_label = rs1.getString(6);
			final String phenotype_quality_uid = rs1.getString(7);
			final String phenotype_quality_label = rs1.getString(8);
			final String related_entity_uid = rs1.getString(9);
			final String related_entity_label = rs1.getString(10);
			final String attribute_label = rs1.getString(11);
			final String attribute_uid = rs1.getString(12);
			final String character_comment = rs1.getString(13);
			final String state_comment = rs1.getString(14);

			final StringBuilder b = new StringBuilder(200);
			b.append(publication_label + DELIMITER);
			if (character_number == null)
				b.append(DELIMITER);
			else
				b.append(character_number + DELIMITER);
			if (character_label == null)
				b.append(DELIMITER);
			else
				b.append(character_label + DELIMITER);
			if (character_comment == null)
				b.append(DELIMITER);
			else
				b.append(character_comment + DELIMITER);
			if (state_label == null)
				b.append(DELIMITER);
			else
				b.append(state_label + DELIMITER);
			if (state_comment == null)
				b.append(DELIMITER);
			else
				b.append(state_comment + DELIMITER);
			if (phenotype_entity_label == null){
				if (phenotype_entity_uid != null){
					b.append(u.doSubstitutions(phenotype_entity_uid) + DELIMITER);
				}
				else
					b.append(DELIMITER);
			}
			else
				b.append(phenotype_entity_label + DELIMITER);
			if (phenotype_quality_label == null) {
				if(phenotype_quality_uid != null) {
					b.append(u.doSubstitutions(phenotype_quality_uid) + DELIMITER);
				}
				else
					b.append(DELIMITER);
			}
			else
				b.append(phenotype_quality_label + DELIMITER);
			if (related_entity_label == null){
				if (related_entity_uid != null){
					b.append(u.doSubstitutions(related_entity_uid) + DELIMITER);
				}
				else
					b.append(DELIMITER);
			}
			else
				b.append(related_entity_label + DELIMITER);
			if (attribute_label == null){
				if (attribute_uid != null){
					b.append(u.doSubstitutions(attribute_uid) + DELIMITER);
				}
				else
					b.append(DELIMITER);
			}
			else
				b.append(attribute_label + DELIMITER);
			u.writeOrDump(b.toString(),bw);
		}
	}

}
