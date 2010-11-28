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

import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;



public class GenusVariationList {


	private static final String PATOSHAPE = "PATO:0000052";
	private static final String PATOSIZE = "PATO:0000117";
	private static final String PATOSTRUCTURE = "PATO:0000141";
	private static final String PATOTEXTURE = "PATO:0000150";
	private static final String PATOPOSITION = "PATO:0000140";
	private static final String PATOCOLOR = "PATO:0000014";
	private static final String PATOCOUNT = "PATO:0000070";
	private static final String PATOCOMPOSITION = "PATO:0000025";

	private static final int MINIMUMPHENOTYPEANNOTATIONS =2;

	//final static String[] attributeList = {PATOSHAPE,PATOSIZE,PATOSTRUCTURE,PATOTEXTURE,PATOPOSITION,PATOCOLOR,PATOCOUNT,PATOCOMPOSITION};

	HashMap<Integer,Profile> taxonProfiles = new HashMap<Integer,Profile>();  //taxon_node_id -> Phenotype profile for taxon
	HashMap<Integer,Profile> geneProfiles = new HashMap<Integer,Profile>();   //gene_node_id -> Phenotype profile for gene

	Map<Integer,String> UIDCache = new HashMap<Integer,String>();

	Map<Integer,Set<Integer>> derivedAnnotations = new HashMap<Integer,Set<Integer>>();

	VariationTable hasVariation = new VariationTable();    



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
		Set<Integer> entSet = collectEntities(c);    // this will retrieve entities that appear in EQ's
		Set<Integer> attSet = collectAttributes(c);
		System.out.println("Entities: " + entSet.size() + " Attributes: " + attSet.size());
		Statement s1;
		PreparedStatement p1;
		PreparedStatement p2;
		PreparedStatement p3;
		PreparedStatement p4;
		long simpleCount = 0;
		int emptyCount = 0;
		int childCount = 0;
		if (c == null)
			return;
		s1 = c.createStatement();
		p1 = c.prepareStatement("select child.node_id,link.object_id,phenotype.node_id, phenotype.entity_node_id, phenotype.quality_node_id, qa.attribute_node_id from link " +
				"join taxon as child on (child.node_id = link.node_id and child.parent_node_id = ? and link.predicate_id = (select node_id from node where uid = 'PHENOSCAPE:exhibits'))" +
				"join phenotype on (link.object_id = phenotype.node_id)" +
		"join quality_to_attribute as qa on (phenotype.quality_node_id = qa.quality_node_id )");

		final Set<Integer> varSet = new HashSet<Integer>();
		//hasVariation.put(att,varSet);
		final Map<Integer,Set<Integer>> attChildSets = new HashMap<Integer,Set<Integer>>();
		ResultSet taxaResults = s1.executeQuery("select node_id,uid from taxon");

		while(taxaResults.next()){
			int taxonID = taxaResults.getInt(1);
			String taxonUID = taxaResults.getString(2);
			if (!UIDCache.containsKey(taxonID))
				UIDCache.put(taxonID,taxonUID);
			p1.setInt(1, taxonID);
			Map<Integer,Profile> childProfiles = new HashMap<Integer,Profile>();
			ResultSet linkResults = p1.executeQuery();
			while(linkResults.next()){
				final int child_id = linkResults.getInt(1);
				final int link_id = linkResults.getInt(2);
				final int phenotype_id = linkResults.getInt(3);
				final int entity_id = linkResults.getInt(4);
				final int quality_id = linkResults.getInt(5);
				final int attribute_id = linkResults.getInt(6);
				if (childProfiles.containsKey(child_id)){
					childProfiles.get(child_id).addPhenotype(attribute_id,entity_id, phenotype_id);
				}
				else {
					childProfiles.put(child_id, new Profile());
					childProfiles.get(child_id).addPhenotype(attribute_id,entity_id, phenotype_id);
				}
				//System.out.println("Adding to profile; att = " + attribute_id + "; ent = " + entity_id + "phenotype = " + phenotype_id);
				simpleCount++;
				if (simpleCount % 100000 == 0)
					System.out.println("Read " + simpleCount + " exhibits links");
			}
			if (!childProfiles.isEmpty()){
				if (!taxonProfiles.containsKey(taxonID)){
					taxonProfiles.put(taxonID,new Profile());
				}
				Profile currentTaxonProfile = taxonProfiles.get(taxonID);
				Profile workProfile = new Profile();
				childCount += childProfiles.size();
				for (Profile childProfile : childProfiles.values()){
					Set<Integer> attset = childProfile.getUsedAttributes();
					Set<Integer> entset = childProfile.getUsedEntities();
					for (Integer att : childProfile.getUsedAttributes()){
						for (Integer ent : childProfile.getUsedEntities()){
							if (childProfile.hasPhenotypeSet(att, ent)){
								currentTaxonProfile.addAlltoPhenotypeSet(att, ent, childProfile.getPhenotypeSet(att, ent));
								//System.out.println("adding to taxon profile: att = " + att + "; ent = " + ent);
							}
						}
					}
				}
				//second pass to check for interesting variation
				for (Profile childProfile : childProfiles.values()){
					for (Integer att : childProfile.getUsedAttributes()){
						for (Integer ent : childProfile.getUsedEntities()){
							if (childProfile.hasPhenotypeSet(att, ent)){
								if (currentTaxonProfile.getPhenotypeSet(att, ent) == null)
									System.out.println("fail while adding taxon variation: att = " + att + "; ent = " + ent + "; taxon = " + UIDCache.get(taxonID));
								if (currentTaxonProfile.getPhenotypeSet(att,ent).size() != childProfile.getPhenotypeSet(att, ent).size()){
									hasVariation.addTaxon(att, ent, taxonID);
								}
							}
						}
					}
				}
				
			}
			else {
				emptyCount++;
			}
		}
		System.out.println("Count of taxa with derived annotations " + taxonProfiles.size() + "; taxon with no children with phenotypes: " + emptyCount + " Total children with phenotypes: " + childCount);
		hasVariation.variationReport(UIDCache);
			
			//				final Set<Integer>child_union = new HashSet<Integer>();
//				for(Set<Integer> childSet : taxonResults.values()){  // constructs the union of all the child phenotype sets
//					child_union.addAll(childSet);
//				}
//				boolean isInteresting = false;
//				for(Set<Integer> childSet: taxonResults.values()){  //for each child, does it differ from the union of it and its siblings; if so: variation
//					if (child_union.size() != childSet.size())       // if they're the same size, they will be equal since childSet is a subset of child_union
//						isInteresting = true;
//				}
//				if (isInteresting){
//					//System.out.println("Taxon: " + UIDCache.get(taxonID) + " size: " + taxonResults.size() + " INTERESTING");
//					varSet.add(taxonID);
//					attChildSets.put(taxonID, child_union);
//				}
				//else
				//System.out.println("Taxon: " + UIDCache.get(taxonID) + " size: " + taxonResults.size() + " NOT INTERESTING");						

//		System.out.println("Counts of taxa with variation");
//		for(Integer attribute : hasVariation.keySet()){
//			System.out.println("Attribute: " + attribute + " " + hasVariation.get(attribute).size());
//		}


//		p4 = c.prepareStatement("SELECT gene_node_id, dga.phenotype_node_id,p1.uid,p1.quality_uid,p1.quality_label, n1.label,n1.uid,n1.node_id FROM distinct_gene_annotation AS dga " +
//				"JOIN phenotype AS p1 ON (p1.node_id = dga.phenotype_node_id)" +
//				"JOIN quality_to_attribute AS qa on (p1.quality_node_id = qa.quality_node_id) " +
//		"JOIN node AS n1 ON (qa.attribute_node_id = n1.node_id)");
//		for(Integer att : attSet){
//			Map<Integer,Set<Integer>> attributeMap = new HashMap<Integer,Set<Integer>>();
//			genePhenotypeMap.put(att,attributeMap);
//			Set<Integer> genePhenotypes;
//			ResultSet ss = p4.executeQuery();
//			while(ss.next()){
//				int geneNode = ss.getInt(1);
//				int phenoID = ss.getInt(2);
//				String phenoUID = ss.getString(3);
//				String qualityUID = ss.getString(4);
//				String quality_label = ss.getString(5);
//				String attribute_label = ss.getString(6);
//				String attribute_uid = ss.getString(7);
//				int attribute_id = ss.getInt(8);
//				if (attributeMap.containsKey(geneNode)){
//					genePhenotypes = attributeMap.get(geneNode);
//				}
//				else{
//					genePhenotypes = new HashSet<Integer>();
//					attributeMap.put(geneNode, genePhenotypes);
//				}
//				if (att.equals(attribute_id)){
//					UIDCache.put(phenoID, phenoUID);
//					genePhenotypes.add(phenoID);
//				}
//			}
//
//		}
//
//		/* These need to happen after the profiles have been constructed, since we don't want to count taxon annotations that don't reflect change */
//		//		AttributeCountTree counts = new AttributeCountTree();  
//		//		counts.test(u,c,UIDCache);
//
//		System.out.println("Generating p3");
//		int hitCount = 0;
//		//Note: p3 and p4 might turn out to be identical, but they are constructed separately to allow divergence
//		p3 = c.prepareStatement("select entity.uid,target.uid,target.node_id from node as entity " +
//				"join link on (entity.node_id=link.node_id AND link.predicate_id = (select node_id from node where uid = 'OBO_REL:inheres_in_part_of')) " + 
//				"join node as target on (target.node_id = link.object_id) where entity.node_id = ? " +
//		"group by entity.uid, target.uid,target.node_id");
//		p4 = c.prepareStatement("select entity.uid,target.uid,target.node_id from node as entity " +
//				"join link on (entity.node_id=link.node_id AND link.predicate_id = (select node_id from node where uid = 'OBO_REL:inheres_in_part_of')) " + 
//				"join node as target on (target.node_id = link.object_id) where entity.node_id = ? " +
//		"group by entity.uid, target.uid, target.node_id");
//		for(Integer currentAttribute : attSet){
//			Map<Integer,Set<Integer>> taxonProfileMap = taxonPhenotypeMap.get(currentAttribute);
//			Map<Integer,Set<Integer>> geneProfileMap = genePhenotypeMap.get(currentAttribute);
//			System.out.println("Total of " + taxonProfileMap.size() + " taxon profiles");
//			System.out.println("Total of " + geneProfileMap.size() + " gene profiles");
//			Map <Integer,Set<Integer>> taxonEntityCache = new HashMap<Integer,Set<Integer>>();
//			Map <Integer,Set<Integer>> geneEntityCache = new HashMap<Integer,Set<Integer>>();
//			// need to generalize this for families, etc - done??
//			for(Integer taxon : taxonProfileMap.keySet()){
//				Set<Integer> taxonProfile = taxonProfileMap.get(taxon);
//				if (taxonProfile.size()>=MINIMUMPHENOTYPEANNOTATIONS){
//					for(Integer taxonPhenotype : taxonProfile){
//						if (!taxonEntityCache.containsKey(taxonPhenotype)){
//							Set<Integer> neighborEntities = new HashSet<Integer>();
//							taxonEntityCache.put(taxonPhenotype,neighborEntities);
//							p3.setInt(1,taxonPhenotype.intValue());
//							ResultSet tpNeighborEntities = p3.executeQuery();
//							while(tpNeighborEntities.next()){
//								int tpNeighbor = tpNeighborEntities.getInt(3);
//								neighborEntities.add(tpNeighbor);
//							}
//							tpNeighborEntities.close();
//						}
//					}
//				}
//				else
//					System.err.println("Shouldn't be here..." + taxonProfile.size());
//			}
//			for(Integer gene : geneProfileMap.keySet()){
//				Set<Integer>geneProfile = geneProfileMap.get(gene);
//				for(Integer genePhenotype: geneProfile){
//					if (!geneEntityCache.containsKey(genePhenotype)){
//						Set<Integer> neighborEntities = new HashSet<Integer>();
//						geneEntityCache.put(genePhenotype,neighborEntities);
//						p4.setInt(1, genePhenotype.intValue());
//						ResultSet gpNeighborEntities = p4.executeQuery();
//						while(gpNeighborEntities.next()){
//							int tpNeighbor = gpNeighborEntities.getInt(3);
//							neighborEntities.add(tpNeighbor);
//						}
//						gpNeighborEntities.close();
//					}
//				}
//			}

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
	//	}	


	}


	private Set<Integer> collectAttributes(Connection c) throws SQLException{
		Set<Integer> result = new HashSet<Integer>();
		Statement s1 = c.createStatement();
		ResultSet attResults = s1.executeQuery("SELECT DISTINCT attribute_node_id FROM quality_to_attribute");
		while(attResults.next()){
			result.add(attResults.getInt(1));
		}
		return result;
	}

	private Set<Integer> collectEntities(Connection c) throws SQLException{
		Set<Integer> result = new HashSet<Integer>();
		Statement s1 = c.createStatement();
		ResultSet attResults = s1.executeQuery("SELECT DISTINCT entity_node_id FROM phenotype");
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
