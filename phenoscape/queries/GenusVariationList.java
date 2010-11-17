package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import phenoscape.queries.lib.Utils;



public class GenusVariationList {


	private static final String PATOSHAPE = "PATO:0000052";
	private static final String PATOSIZE = "PATO:0000117";
	private static final String PATOSTRUCTURE = "PATO:0000141";
	private static final String PATOTEXTURE = "PATO:0000150";
	private static final String PATOPOSITION = "PATO:0000140";
	private static final String PATOCOLOR = "PATO:0000014";
	private static final String PATOCOUNT = "PATO:0000070";

	private static final int MINIMUMPHENOTYPEANNOTATIONS =2;

	//final static String[] attributeList = {PATOSHAPE,PATOSIZE,PATOSTRUCTURE,PATOTEXTURE,PATOPOSITION,PATOCOLOR,PATOCOUNT};

	Map<Integer,Map<Integer,Set<Integer>>> taxonPhenotypeMap = new HashMap<Integer,Map<Integer,Set<Integer>>>();
	Map<Integer,Map<Integer,Set<Integer>>> genePhenotypeMap = new HashMap<Integer,Map<Integer,Set<Integer>>>();

	Map<Integer,String> UIDCache = new HashMap<Integer,String>();
	
	Map<Integer,Set<Integer>> derivedAnnotations = new HashMap<Integer,Set<Integer>>();
	
	Map<Integer,Set<Integer>> hasVariation = new HashMap<Integer,Set<Integer>>();      //Attribute node_id -> set of taxon node_id's that show variation in children
	//Map<Integer,Map<Integer,Set<Integer>>>  = new HashMap<Integer,Map<Integer,Set<Integer>>>();  //Attribute node_id ->set of maps of taxon node_ids to union of child phenotypes
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GenusVariationList listQuery = new GenusVariationList();
		Utils u = new Utils();
		Connection c = u.openKB();
		try {
			listQuery.test(new Utils(), c);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
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


	private void test(Utils u,Connection c) throws SQLException{
		//		AttributeCountTree counts = new AttributeCountTree();
		//		counts.test(u,c,UIDCache);
		List<Integer> attList = listAttributes(c);
		Statement s1;
		PreparedStatement p1;
		PreparedStatement p2;
		PreparedStatement p3;
		PreparedStatement p4;
		long simpleCount = 0;
		if (c == null)
			return;
		s1 = c.createStatement();
		p1 = c.prepareStatement("select child.node_id,link.object_id,phenotype.node_id, phenotype.quality_node_id from link " +
				"join taxon as child on (child.node_id = link.node_id and child.parent_node_id = ? and link.predicate_id = (select node_id from node where uid = 'PHENOSCAPE:exhibits'))" +
				"join phenotype on (link.object_id = phenotype.node_id)" +
		"join quality_to_attribute as qa on (phenotype.quality_node_id = qa.quality_node_id AND qa.attribute_node_id = ?)");


		for(Integer att : attList){
			System.out.println("Processing attribute: " + att.intValue());
			final Set<Integer> varSet = new HashSet<Integer>();
			hasVariation.put(att,varSet);
			final Map<Integer,Set<Integer>> attChildSets = new HashMap<Integer,Set<Integer>>();
			taxonPhenotypeMap.put(att, attChildSets);
			ResultSet taxaResults = s1.executeQuery("select node_id,uid from taxon");

			while(taxaResults.next()){
				Map<Integer,Set<Integer>> taxonResults = new HashMap<Integer,Set<Integer>>();
				int taxonID = taxaResults.getInt(1);
				String taxonUID = taxaResults.getString(2);
				if (!UIDCache.containsKey(taxonID))
					UIDCache.put(taxonID,taxonUID);
				p1.setInt(1, taxonID);
				p1.setInt(2, att.intValue());
				ResultSet linkResults = p1.executeQuery();
				while(linkResults.next()){
					final int child_id = linkResults.getInt(1);
					final int link_id = linkResults.getInt(2);
					final int phenotype_id = linkResults.getInt(3);
					if (taxonResults.containsKey(child_id)){
						Set<Integer>childSet = taxonResults.get(child_id);
						childSet.add(phenotype_id);
					}
					else {
						Set<Integer>childSet = new HashSet<Integer>();
						childSet.add(phenotype_id);
						taxonResults.put(child_id, childSet);
					}
					simpleCount++;
					if (simpleCount % 100000 == 0)
						System.out.println("Read " + simpleCount + " exhibits links");
				}
				if (!taxonResults.isEmpty()){
					final Set<Integer>child_union = new HashSet<Integer>();
					for(Set<Integer> childSet : taxonResults.values()){  // constructs the union of all the child phenotype sets
						child_union.addAll(childSet);
					}
					boolean isInteresting = false;
					for(Set<Integer> childSet: taxonResults.values()){  //for each child, does it differ from the union of it and its siblings; if so: variation
						if (child_union.size() != childSet.size())       // if they're the same size, they will be equal since childSet is a subset of child_union
							isInteresting = true;
					}
					if (isInteresting){
						//System.out.println("Taxon: " + UIDCache.get(taxonID) + " size: " + taxonResults.size() + " INTERESTING");
						varSet.add(taxonID);
						attChildSets.put(taxonID, child_union);
					}
					//else
						//System.out.println("Taxon: " + UIDCache.get(taxonID) + " size: " + taxonResults.size() + " NOT INTERESTING");						
				}
			}
		}
		System.out.println("Counts of taxa with variation");
		for(Integer attribute : hasVariation.keySet()){
			System.out.println("Attribute: " + attribute + " " + hasVariation.get(attribute).size());
		}

		
		p4 = c.prepareStatement("SELECT gene_node_id, dga.phenotype_node_id,p1.uid,p1.quality_uid,p1.quality_label, n1.label,n1.uid,n1.node_id FROM distinct_gene_annotation AS dga " +
				"JOIN phenotype AS p1 ON (p1.node_id = dga.phenotype_node_id)" +
				"JOIN quality_to_attribute AS qa on (p1.quality_node_id = qa.quality_node_id) " +
		"JOIN node AS n1 ON (qa.attribute_node_id = n1.node_id)");
		for(Integer att : attList){
			Map<Integer,Set<Integer>> attributeMap = new HashMap<Integer,Set<Integer>>();
			genePhenotypeMap.put(att,attributeMap);
			Set<Integer> genePhenotypes;
			ResultSet ss = p4.executeQuery();
			while(ss.next()){
				int geneNode = ss.getInt(1);
				int phenoID = ss.getInt(2);
				String phenoUID = ss.getString(3);
				String qualityUID = ss.getString(4);
				String quality_label = ss.getString(5);
				String attribute_label = ss.getString(6);
				String attribute_uid = ss.getString(7);
				int attribute_id = ss.getInt(8);
				if (attributeMap.containsKey(geneNode)){
					genePhenotypes = attributeMap.get(geneNode);
				}
				else{
					genePhenotypes = new HashSet<Integer>();
					attributeMap.put(geneNode, genePhenotypes);
				}
				if (att.equals(attribute_id)){
					UIDCache.put(phenoID, phenoUID);
					genePhenotypes.add(phenoID);
				}
			}

		}
		System.out.println("Generating p3");
		int hitCount = 0;
		//Note: p3 and p4 might turn out to be identical, but they are constructed separately to allow divergence
		p3 = c.prepareStatement("select entity.uid,target.uid,target.node_id from node as entity " +
				"join link on (entity.node_id=link.node_id AND link.predicate_id = (select node_id from node where uid = 'OBO_REL:inheres_in_part_of')) " + 
				"join node as target on (target.node_id = link.object_id) where entity.node_id = ? " +
		"group by entity.uid, target.uid,target.node_id");
		p4 = c.prepareStatement("select entity.uid,target.uid,target.node_id from node as entity " +
				"join link on (entity.node_id=link.node_id AND link.predicate_id = (select node_id from node where uid = 'OBO_REL:inheres_in_part_of')) " + 
				"join node as target on (target.node_id = link.object_id) where entity.node_id = ? " +
		"group by entity.uid, target.uid, target.node_id");
		for(Integer currentAttribute : attList){
			Map<Integer,Set<Integer>> taxonProfileMap = taxonPhenotypeMap.get(currentAttribute);
			Map<Integer,Set<Integer>> geneProfileMap = genePhenotypeMap.get(currentAttribute);
			System.out.println("Total of " + taxonProfileMap.size() + " taxon profiles");
			System.out.println("Total of " + geneProfileMap.size() + " gene profiles");
			Map <Integer,Set<Integer>> taxonEntityCache = new HashMap<Integer,Set<Integer>>();
			Map <Integer,Set<Integer>> geneEntityCache = new HashMap<Integer,Set<Integer>>();
			// need to generalize this for families, etc - done??
			for(Integer taxon : taxonProfileMap.keySet()){
				Set<Integer> taxonProfile = taxonProfileMap.get(taxon);
				if (taxonProfile.size()>=MINIMUMPHENOTYPEANNOTATIONS){
					for(Integer taxonPhenotype : taxonProfile){
						if (!taxonEntityCache.containsKey(taxonPhenotype)){
							Set<Integer> neighborEntities = new HashSet<Integer>();
							taxonEntityCache.put(taxonPhenotype,neighborEntities);
							p3.setInt(1,taxonPhenotype.intValue());
							ResultSet tpNeighborEntities = p3.executeQuery();
							while(tpNeighborEntities.next()){
								int tpNeighbor = tpNeighborEntities.getInt(3);
								neighborEntities.add(tpNeighbor);
							}
							tpNeighborEntities.close();
						}
					}
				}
				else
					System.err.println("Shouldn't be here..." + taxonProfile.size());
			}
			for(Integer gene : geneProfileMap.keySet()){
				Set<Integer>geneProfile = geneProfileMap.get(gene);
				for(Integer genePhenotype: geneProfile){
					if (!geneEntityCache.containsKey(genePhenotype)){
						Set<Integer> neighborEntities = new HashSet<Integer>();
						geneEntityCache.put(genePhenotype,neighborEntities);
						p4.setInt(1, genePhenotype.intValue());
						ResultSet gpNeighborEntities = p4.executeQuery();
						while(gpNeighborEntities.next()){
							int tpNeighbor = gpNeighborEntities.getInt(3);
							neighborEntities.add(tpNeighbor);
						}
						gpNeighborEntities.close();
					}
				}
			}

//			for(Integer taxon : taxonProfileMap.keySet()){
//				Set<Integer> taxonProfile = taxonProfileMap.get(taxon);
//				for(Integer gene : geneProfileMap.keySet()){
//					Set<Integer>geneProfile = geneProfileMap.get(gene);
//					System.out.println("***************");
//					for(Integer taxonPhenotype : taxonProfile){
//						Set<Integer> tpNeighbors = taxonEntityCache.get(taxonPhenotype);
//						for(Integer genePhenotype : geneProfile){
//							Set<Integer> gpNeighbors = geneEntityCache.get(genePhenotype);
//							Set<Integer> matches = new HashSet<Integer>();
//							for(Integer tNeighbor : tpNeighbors){
//								for(Integer gNeighbor : gpNeighbors){
//									if (tNeighbor.equals(gNeighbor)){ 
//										matches.add(tNeighbor);
//									}
//								}
//							}
//							System.out.println("matches size: " + matches.size());
//						}
//					}
//				}
//			}
//
//
		}	


	}
	
	
	private List<Integer> listAttributes(Connection c) throws SQLException{
		List<Integer> result = new ArrayList<Integer>();
		Statement s1 = c.createStatement();
		ResultSet attResults = s1.executeQuery("SELECT DISTINCT attribute_node_id FROM quality_to_attribute");
		while(attResults.next()){
			result.add(attResults.getInt(1));
		}
		return result;
	}
	
	
	

	private void showSet(Set<String> theSet){
		for(String item : theSet){
			System.out.print(item + " ");
		}
		System.out.println();
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
