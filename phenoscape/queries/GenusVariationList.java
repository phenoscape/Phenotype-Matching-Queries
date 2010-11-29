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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;



public class GenusVariationList {



	private static final String REPORTFILENAME = "VariationReport.txt";

	HashMap<Integer,Profile> taxonProfiles = new HashMap<Integer,Profile>();  //taxon_node_id -> Phenotype profile for taxon
	HashMap<Integer,Profile> geneProfiles = new HashMap<Integer,Profile>();   //gene_node_id -> Phenotype profile for gene

	Map<Integer,String> UIDCache = new HashMap<Integer,String>();

	Map<Integer,Set<Integer>> derivedAnnotations = new HashMap<Integer,Set<Integer>>();
	
	Map<Integer,Integer>childDist = new HashMap<Integer,Integer>();

	VariationTable taxonVariation = new VariationTable();    
	VariationTable geneVariation = new VariationTable();
	
	Map<Integer,Integer> attributeMap = new HashMap<Integer,Integer>();


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GenusVariationList listQuery = new GenusVariationList();
		Utils u = new Utils();
		Connection c = u.openKB();
		File outFile = new File(REPORTFILENAME);
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(outFile));
			listQuery.test(new Utils(), c, bw);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				c.close();
				bw.close();
			} catch (Exception e) {
				System.err.print("An exception occurred while closing a report file or database connection");
				e.printStackTrace();
			}
		}

	}


	private void test(Utils u,Connection c, BufferedWriter bw) throws SQLException{
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
		p1 = c.prepareStatement("SELECT child.node_id,phenotype.node_id, phenotype.entity_node_id, phenotype.entity_uid, qa.attribute_node_id FROM link " +
				"JOIN taxon AS child ON (child.node_id = link.node_id AND child.parent_node_id = ? AND link.predicate_id = (select node_id FROM node WHERE uid = 'PHENOSCAPE:exhibits'))" +
				"JOIN phenotype ON (link.object_id = phenotype.node_id)" +
		"JOIN quality_to_attribute AS qa ON (phenotype.quality_node_id = qa.quality_node_id ) WHERE is_inferred = false");

		
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
				final int phenotype_id = linkResults.getInt(2);
				final int entity_id = linkResults.getInt(3);
				final String entity_uid = linkResults.getString(4);
				final int attribute_id = linkResults.getInt(5);
				if (childProfiles.containsKey(child_id)){
					childProfiles.get(child_id).addPhenotype(attribute_id,entity_id, phenotype_id);
				}
				else {
					childProfiles.put(child_id, new Profile());
					childProfiles.get(child_id).addPhenotype(attribute_id,entity_id, phenotype_id);
				}
				if (!UIDCache.containsKey(entity_id))
					UIDCache.put(entity_id, entity_uid);
			}
			if (!childProfiles.isEmpty()){
				if (!taxonProfiles.containsKey(taxonID)){
					taxonProfiles.put(taxonID,new Profile());
				}
				Profile currentTaxonProfile = taxonProfiles.get(taxonID);
				childCount += childProfiles.size();
				childDist.put(taxonID,childProfiles.size());
				//This builds the derived annotations for each taxon with childProfiles
				for (Integer childID : childProfiles.keySet()){
					Profile childProfile = childProfiles.get(childID);
					for (Integer att : childProfile.getUsedAttributes()){
						for (Integer ent : childProfile.getUsedEntities()){
							if (childProfile.hasPhenotypeSet(att, ent)){
								//listIntegerMembers(childProfile.getPhenotypeSet(att, ent));
								currentTaxonProfile.addAlltoPhenotypeSet(att, ent, childProfile.getPhenotypeSet(att, ent));
							}
						}
					}
				}
				//second pass to check for interesting variation
				for (Integer att : currentTaxonProfile.getUsedAttributes()){
					for (Integer ent : currentTaxonProfile.getUsedEntities()){
						for (Integer childID : childProfiles.keySet()){
							Profile childProfile = childProfiles.get(childID);
							if (childProfile.hasPhenotypeSet(att, ent)){
								if (currentTaxonProfile.getPhenotypeSet(att, ent) == null)
									System.out.println("fail while adding taxon variation: att = " + att + "; ent = " + ent + "; taxon = " + UIDCache.get(taxonID));
								if (currentTaxonProfile.getPhenotypeSet(att,ent).size() != childProfile.getPhenotypeSet(att, ent).size()){
									taxonVariation.addTaxon(att, ent, taxonID);
								}
							}
							else if (currentTaxonProfile.hasPhenotypeSet(att, ent)){  //child not represented = change in sibling
								taxonVariation.addTaxon(att,ent,taxonID);
							}
						}
					}
				}

			}
			else {
				emptyCount++;
			}
		}

		u.writeOrDump("Count of taxa with derived annotations " + taxonProfiles.keySet().size() + "; taxa with no children with phenotypes: " + emptyCount + " Total children with phenotypes: " + childCount,bw);
		taxonVariation.variationReport(u,UIDCache,bw);

		int rawGeneCount = 0;
		int annotationCount = 0;

		//		ResultSet geneResults = s1.executeQuery("select node_id,uid from gene");
		ResultSet attributeResults = s1.executeQuery("select quality_node_id,attribute_node_id FROM quality_to_attribute");
		while(attributeResults.next()){
			final int quality_id = attributeResults.getInt(1);
			final int attribute_id = attributeResults.getInt(2);
			attributeMap.put(quality_id,attribute_id);
		}

		p4 = c.prepareStatement("SELECT gene_node_id, gene_uid, dga.phenotype_node_id, p1.entity_node_id, p1.entity_uid, p1.quality_node_id, p1.quality_uid FROM distinct_gene_annotation AS dga " +
				"JOIN phenotype AS p1 ON (p1.node_id = dga.phenotype_node_id) ");
		ResultSet annotationResults = p4.executeQuery();
		while(annotationResults.next()){
			final int geneID = annotationResults.getInt(1);
			final String gene_uid = annotationResults.getString(2);
			final int phenotype_id = annotationResults.getInt(3);
			final int entity_id = annotationResults.getInt(4);
			final String entity_uid = annotationResults.getString(5);
			final int quality_id = annotationResults.getInt(6);
			final String quality_uid = annotationResults.getString(7);
			if (attributeMap.containsKey(quality_id)){
				final int attribute_id = attributeMap.get(quality_id);
				if (geneProfiles.containsKey(geneID)){
					geneProfiles.get(geneID).addPhenotype(attribute_id,entity_id, phenotype_id);
				}
				else {
					geneProfiles.put(geneID, new Profile());
					geneProfiles.get(geneID).addPhenotype(attribute_id,entity_id, phenotype_id);
				}
				geneVariation.addTaxon(attribute_id, entity_id, geneID);
				annotationCount++;

			}
			//else u.writeOrDump("Bad quality: " + u.lookupIDToName(quality_uid),bw);
			if (!UIDCache.containsKey(geneID))
				UIDCache.put(geneID,gene_uid);
		}

		//u.writeOrDump("Raw Genes: " + rawGeneCount + "; gene annotations" + annotationCount + ";Count of genes with annotations " + geneProfiles.keySet().size(),bw);

		//geneVariation.variationReport(u, UIDCache, bw);
		
		/* These need to happen after the profiles have been constructed, since we don't want to count taxon annotations that don't reflect change */
		AttributeCountTree counts = new AttributeCountTree();  
		//		counts.test(u,c,UIDCache);
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
		PreparedStatement ap1 = c.prepareStatement("SELECT uid FROM node WHERE node_id = ?");
		for (Integer att : result){
			ap1.setInt(1, att.intValue());
			ResultSet attUIDResults = ap1.executeQuery();
			while (attUIDResults.next()){
				String uid = attUIDResults.getString(1);
				UIDCache.put(att,uid);
			}
		}


		return result;
	}

	private Set<Integer> collectEntities(Connection c) throws SQLException{
		Set<Integer> result = new HashSet<Integer>();
		Statement s1 = c.createStatement();
		ResultSet entResults = s1.executeQuery("SELECT DISTINCT entity_node_id,entity_uid FROM phenotype");
		while(entResults.next()){
			Integer ent = new Integer(entResults.getInt(1));
			String uid = entResults.getString(2);
			result.add(ent);
			UIDCache.put(ent,uid);
		}
		return result;
	}



	private void showSet(Set<String> theSet){
		for(String item : theSet){
			System.out.print(item + " ");
		}
		System.out.println();
	}

	private void listIntegerMembers(Set<Integer> phenotypeSet) {
		for(Integer item : phenotypeSet){
			System.out.print(item.intValue() + " ");
		}
		System.out.println();
	}



}
