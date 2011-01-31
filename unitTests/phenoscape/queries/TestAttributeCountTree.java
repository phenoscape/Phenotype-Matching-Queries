package phenoscape.queries;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import phenoscape.queries.EntityCountTree;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.Utils;

public class TestAttributeCountTree {

	PhenotypeCountTree countQueryPATO;
	EntityCountTree countQueryTAO;
	Utils u;
	String PATOUID = "PATO:0000001";
	String TAOUID = "TAO:0100000";
	
	Map<Integer,Profile> taxonProfiles;  //taxon_node_id -> Phenotype profile for taxon
	Map<Integer,Profile> geneProfiles;   //gene_node_id -> Phenotype profile for gene
	
	Map<Integer,Integer> attributeMap;

	private static final String TAXON1 = "Aspidoras";
	private static final String TAXON2 = "Aspidoras albater";
	private static final String TAXON3 = "Aspidoras pauciradiatus";
	private static final String TAXON10 = "Otophysi";
	private static final String GENE10 = "brpf1";

	private static final String IDQUERY = "SELECT node_id FROM node WHERE label = ?";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		u = new Utils();
		u.openKB();
		countQueryTAO = new EntityCountTree(TAOUID,u);
		countQueryPATO = new PhenotypeCountTree(PATOUID,u);
		taxonProfiles = new HashMap<Integer,Profile>();  //taxon_node_id -> Phenotype profile for taxon
		geneProfiles = new HashMap<Integer,Profile>();   //gene_node_id -> Phenotype profile for gene
		attributeMap = u.setupAttributes();
		addProfile(TAXON1,taxonProfiles,u);
		int t2id = addProfile(TAXON2,taxonProfiles,u);
		Profile t2Profile = taxonProfiles.get(t2id);
		t2Profile.addPhenotype(attributeMap.get(2475), 547894, 762298);
		t2Profile.addPhenotype(attributeMap.get(11),3136,762111);
		//t2Profile.addPhenotype(attributeMap.get(2389),4610,761924);
		
		addProfile(TAXON3,taxonProfiles,u);
		addProfile(TAXON10,taxonProfiles,u);
		int g10id = addProfile(GENE10,geneProfiles,u);
		Profile g10Profile = geneProfiles.get(g10id);
		//g10Profile.addPhenotype(attributeMap.get(978),354,335627);
		//g10Profile.addPhenotype(attributeMap.get(1088),3488,430903);
		//g10Profile.addPhenotype(attributeMap.get(972),364,533700);
	}
	
	
	private int addProfile(String exhibitor, Map<Integer,Profile> profileTable, Utils u) throws SQLException{
		PreparedStatement idstatement = u.getPreparedStatement(IDQUERY);
		idstatement.setString(1, exhibitor);
		ResultSet idresult = idstatement.executeQuery();
		if (idresult.next()){
			int taxonID = idresult.getInt(1);
			profileTable.put(taxonID, new Profile());
			return taxonID;
		}	
		return -1;
	}

	@Test
	public void testTraverseOntologyTree() throws Exception {
		countQueryTAO.traverseOntologyTree(countQueryTAO.ontologyTable,u);
//		for(Integer entity : countQueryTAO.ontologyTable.keySet()){
//			System.out.println("Nodeid: " + entity + "\tUID: " + u.getNodeUID(entity) + "\tName: " + u.getNodeName(entity));
//		}
		System.out.println("Caro table size = " + countQueryTAO.ontologyTable.size());
		countQueryPATO.traverseOntologyTree(countQueryPATO.ontologyTable,u);
//		for(Integer phenotype : countQueryPATO.ontologyTable.keySet()){
//			System.out.println("Nodeid: " + phenotype + "\tUID: " + u.getNodeUID(phenotype) + "\tName: " + u.getNodeName(phenotype));
//		}
		System.out.println("PATO table size = " + countQueryPATO.ontologyTable.size());
	}

	@Test
	public void testBuild() throws Exception{
		countQueryTAO.build(u,taxonProfiles, geneProfiles);
		
		countQueryPATO.build(u,taxonProfiles, geneProfiles);
	}
	
	

}
