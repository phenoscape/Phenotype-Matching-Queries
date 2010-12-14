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

import phenoscape.queries.lib.PhenotypeScoreTable;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.ProfileScoreTable;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;



public class GenusVariationList {

	private static final String CAROROOT = "CARO:0000000";
	private static final String PATOROOT = "PATO:0000001";


	private static final String TAXONREPORTFILENAME = "../TaxonVariationReport.txt";
	private static final String GENEREPORTFILENAME = "../GeneVariationReport.txt";
	private static final String PHENOTYPEMATCHREPORTFILENAME = "../PhenotypeMatchReport.txt";
	private static final String PROFILEMATCHREPORTFILENAME = "../ProfileMatchReport.txt";
	
	

	Map<Integer,Profile> taxonProfiles = new HashMap<Integer,Profile>();  //taxon_node_id -> Phenotype profile for taxon
	Map<Integer,Profile> geneProfiles = new HashMap<Integer,Profile>();   //gene_node_id -> Phenotype profile for gene


	Map<Integer,Set<Integer>> derivedAnnotations = new HashMap<Integer,Set<Integer>>();

	Map<Integer,Integer>childDist = new HashMap<Integer,Integer>();

	VariationTable taxonVariation = new VariationTable();    
	VariationTable geneVariation = new VariationTable();

	Map<Integer,Integer> attributeMap;

	Map<Integer,Integer> badTaxonQualities = new HashMap<Integer,Integer>();
	Map<Integer,Integer> badGeneQualities = new HashMap<Integer,Integer>();
	
	int qualityNodeID;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GenusVariationList listQuery = new GenusVariationList();
		Utils u = new Utils();
		Connection c = u.openKB();
		File outFile1 = new File(TAXONREPORTFILENAME);
		File outFile2 = new File(GENEREPORTFILENAME);
		File outFile3 = new File(PHENOTYPEMATCHREPORTFILENAME);
		File outFile4 = new File(PROFILEMATCHREPORTFILENAME);
		BufferedWriter bw1 = null;
		BufferedWriter bw2 = null;
		BufferedWriter bw3 = null;
		BufferedWriter bw4 = null;

		try {
			bw1 = new BufferedWriter(new FileWriter(outFile1));
			bw2 = new BufferedWriter(new FileWriter(outFile2));
			bw3 = new BufferedWriter(new FileWriter(outFile3));
			bw4 = new BufferedWriter(new FileWriter(outFile4));
			listQuery.test(new Utils(), c, bw1, bw2, bw3, bw4);
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
				bw1.close();
				bw2.close();
				bw3.close();
				bw4.close();
			} catch (Exception e) {
				System.err.print("An exception occurred while closing a report file or database connection");
				e.printStackTrace();
			}
		}
	}


	private void test(Utils u,Connection c, BufferedWriter bw1, BufferedWriter bw2, BufferedWriter bw3, BufferedWriter bw4) throws SQLException{
		if (c == null)
			return;
		uidCacheEntities(c,u);    // this will retrieve entities that appear in EQ's


		qualityNodeID = getQualityNodeID(c,u);   //set to the root of PATO

		attributeMap = setupAttributes(c,u);
		
		// process taxa annotations

		processTaxonVariation(c, u, bw1);
		//taxonVariation.variationReport(u,bw1);

		processGeneExpression(c, u, bw2);
		//geneVariation.variationReport(u, bw2);

		/* These need to happen after the profiles have been constructed, since we don't want to count taxon annotations that don't reflect change */
		AttributeCountTree entityCounts = new AttributeCountTree();  
		entityCounts.build(u,c,taxonProfiles,geneProfiles,CAROROOT);
			
		AttributeCountTree phenotypeCounts = new AttributeCountTree();
		phenotypeCounts.build(u, c, taxonProfiles, geneProfiles,PATOROOT);
		
		//counts.writeTrees(u,UIDCache);

		System.out.println("Generating p3");
		int hitCount = 0;
		int attOverlaps = 0;
		//Note: p3 and p4 might turn out to be identical, but they are constructed separately to allow divergence
		final PreparedStatement p3 = c.prepareStatement("select target.node_id from node as entity " +
				"join link on (entity.node_id=link.node_id AND link.predicate_id = (select node_id from node where uid = 'OBO_REL:inheres_in_part_of')) " + 
				"join node as target on (target.node_id = link.object_id) where entity.node_id = ? " +
		"group by entity.uid, target.uid,target.node_id");
		Map <Integer,Set<Integer>> phenotypeNeighborCache = new HashMap<Integer,Set<Integer>>();
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			Set<Integer> taxonPhenotypes = currentTaxonProfile.getAllPhenotypes();
			for (Integer currentPhenotype : taxonPhenotypes){
				if (!phenotypeNeighborCache.containsKey(currentPhenotype)){
					Set<Integer> neighborSet = new HashSet<Integer>();
					phenotypeNeighborCache.put(currentPhenotype, neighborSet);
					p3.setInt(1, currentPhenotype);
					ResultSet entityNeighbors = p3.executeQuery();
					while(entityNeighbors.next()){
						int target_id = entityNeighbors.getInt(1);
						neighborSet.add(target_id);
					}
				}
			}
		}
		
		final PreparedStatement p4 = c.prepareStatement("select target.node_id from node as entity " +
				"join link on (entity.node_id=link.node_id AND link.predicate_id = (select node_id from node where uid = 'OBO_REL:inheres_in_part_of')) " + 
				"join node as target on (target.node_id = link.object_id) where entity.node_id = ? " +
		"group by entity.uid, target.uid, target.node_id");

		for(Integer currentGene : geneProfiles.keySet()){
			Profile currentGeneProfile = geneProfiles.get(currentGene);
			Set<Integer> genePhenotypes = currentGeneProfile.getAllPhenotypes();
			for (Integer currentPhenotype : genePhenotypes){
				if (!phenotypeNeighborCache.containsKey(currentPhenotype)){
					Set<Integer> neighborSet = new HashSet<Integer>();
					phenotypeNeighborCache.put(currentPhenotype, neighborSet);
					p4.setInt(1, currentPhenotype);
					ResultSet entityNeighbors = p4.executeQuery();
					while(entityNeighbors.next()){
						int target_id = entityNeighbors.getInt(1);
						neighborSet.add(target_id);
					}
				}
			}
		}
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			for (Integer curAtt : attributeMap.keySet()){
				for(Integer currentGene : geneProfiles.keySet()){
					Profile currentGeneProfile = geneProfiles.get(currentGene);
					if (currentTaxonProfile.getUsedAttributes().contains(curAtt) && 
							currentGeneProfile.getUsedAttributes().contains(curAtt)){
						attOverlaps++;
						for(Integer currentTaxonEntity : currentTaxonProfile.getUsedEntities()){
							if (currentGeneProfile.getUsedEntities().contains(currentTaxonEntity)){
								Set<Integer> taxonPhenotypes = currentTaxonProfile.getPhenotypeSet(curAtt, currentTaxonEntity);
								Set<Integer> genePhenotypes = currentGeneProfile.getPhenotypeSet(curAtt, currentTaxonEntity);
								if (taxonPhenotypes != null && genePhenotypes != null){   //TODO make this unnecessary
									Set<Integer>matches = new HashSet<Integer>();
									for(Integer tPhenotype : taxonPhenotypes){
										Set<Integer> tpNeighbors = phenotypeNeighborCache.get(tPhenotype);
										for(Integer gPhenotype : genePhenotypes){
											if (!phenotypeScores.hasScore(tPhenotype, gPhenotype)){
												if (tPhenotype.equals(gPhenotype)){  													//check for tPhenotype = gPhenotype? 
													Set<Integer>gpNeighbors = phenotypeNeighborCache.get(gPhenotype);
													double bestMatch = Double.MAX_VALUE;  //we're using fractions, so minimize
													for(Integer ent : gpNeighbors){  //simple (but slow) way to get the entity
														double matchScore = entityCounts.combinedFraction(curAtt, ent);
														if (matchScore<bestMatch && matchScore > 0.0){
															bestMatch = matchScore;
														}
													}	
													phenotypeScores.addScore(tPhenotype, gPhenotype, (-1*Math.log(bestMatch)));
													hitCount++;
													//System.out.println("Added phenotype match; total = " + hitCount);
												}
												else{
													Set<Integer>gpNeighbors = phenotypeNeighborCache.get(gPhenotype);
													for(Integer tNeighbor : tpNeighbors){
														for (Integer gNeighbor : gpNeighbors){
															if (tNeighbor.equals(gNeighbor))
																matches.add(tNeighbor);
														}
													}
													double bestMatch = Double.MAX_VALUE;  //we're using fractions, so minimize
													Integer bestEntity = null;
													for(Integer ent : matches){
														double matchScore = entityCounts.combinedFraction(curAtt, ent);
														if (matchScore<bestMatch && matchScore > 0.0){
															bestMatch = matchScore;
															bestEntity = ent;
														}
													}
													if (bestEntity != null){  //either no matches, or none of them have a score (e.g. orphaned TAO terms)
														phenotypeScores.addScore(tPhenotype, gPhenotype, (-1*Math.log(bestMatch)));
														hitCount++;
														//System.out.println("Added phenotype match; total = " + hitCount);
													}
													else{
														System.out.println(" -- match fail -- " + tPhenotype + "; " + gPhenotype);
														//listIntegerMembers(tpNeighbors);
														//listIntegerMembers(gpNeighbors);
														for(Integer ent : matches){
															double matchScore = entityCounts.combinedFraction(curAtt, ent);
															System.out.println("Entity: " + ent + "; score: " + matchScore);
														}
														phenotypeScores.addScore(tPhenotype, gPhenotype, -1.0);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		System.out.println("Finished building phenotype match cache");
		System.out.println("Writing Phenotype match summary");
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			for(Integer currentGene : geneProfiles.keySet()){
				Profile currentGeneProfile = geneProfiles.get(currentGene);
				Set<Integer> taxonPhenotypes = currentTaxonProfile.getAllPhenotypes();
				Set<Integer> genePhenotypes = currentGeneProfile.getAllPhenotypes();
				boolean hasOverLap = false;
				for(Integer tPhenotype : taxonPhenotypes){
					for(Integer gPhenotype : genePhenotypes){
						if (phenotypeScores.hasScore(tPhenotype, gPhenotype)){
							if (phenotypeScores.getScore(tPhenotype, gPhenotype)>-1){
								hasOverLap = true;
							}
						}			
					}
				}
				if (hasOverLap)
					u.writeOrDump("Taxon: " + u.getNodeName(currentTaxon) + "\tGene: " + u.getNodeName(currentGene),bw3);
				for(Integer tPhenotype : taxonPhenotypes){
					for(Integer gPhenotype : genePhenotypes){
						if (phenotypeScores.hasScore(tPhenotype, gPhenotype)){
							if (phenotypeScores.getScore(tPhenotype, gPhenotype)>-1){
								u.writeOrDump("\t\t" + u.getNodeName(tPhenotype) + "\t" + u.getNodeName(gPhenotype) + "\t" + phenotypeScores.getScore(tPhenotype, gPhenotype),bw3);
							}
							else {
								u.writeOrDump("\t\t" + u.getNodeName(tPhenotype) + "\t" + u.getNodeName(gPhenotype) + "\t No Match",bw3);								
							}
						}
					}
				}
			}
		}
		System.out.println("Finished Writing Phenotype match summary");
		System.out.println("Calculating Profile Scores");
		
		ProfileScoreTable maxICScores = new ProfileScoreTable();
		ProfileScoreTable iccsScores = new ProfileScoreTable();
		ProfileScoreTable simICScores = new ProfileScoreTable();
		ProfileScoreTable simJScores = new ProfileScoreTable();

		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			for(Integer currentGene : geneProfiles.keySet()){
				Profile currentGeneProfile = geneProfiles.get(currentGene);
				//calculate maxIC score for this pair of profiles
				double maxPhenotypeMatch = Double.NEGATIVE_INFINITY;
				for(Integer taxonPhenotype : currentTaxonProfile.getAllPhenotypes()){
					for(Integer genePhenotype : currentGeneProfile.getAllPhenotypes()){
						if (phenotypeScores.hasScore(taxonPhenotype, genePhenotype)){
							if (phenotypeScores.getScore(taxonPhenotype, genePhenotype) > maxPhenotypeMatch){
								maxPhenotypeMatch = phenotypeScores.getScore(taxonPhenotype, genePhenotype);
							}
						}
					}
				}
				maxICScores.addScore(currentTaxon, currentGene, maxPhenotypeMatch);

				//calculate ICCS score for this pair of profiles
				double iccsScore = 0;
				for (Integer curAtt : attributeMap.keySet()){
					if (currentTaxonProfile.getUsedAttributes().contains(curAtt) && 
							currentGeneProfile.getUsedAttributes().contains(curAtt)){
						for(Integer currentTaxonEntity : currentTaxonProfile.getUsedEntities()){
							if (currentGeneProfile.getUsedEntities().contains(currentTaxonEntity)){
								Set<Integer> taxonPhenotypes = currentTaxonProfile.getPhenotypeSet(curAtt, currentTaxonEntity);
								Set<Integer> genePhenotypes = currentGeneProfile.getPhenotypeSet(curAtt, currentTaxonEntity);
								if (taxonPhenotypes != null && genePhenotypes != null){   //TODO make this unnecessary
									for(Integer tPhenotype : taxonPhenotypes){
										for(Integer gPhenotype : genePhenotypes){
										}
									}
								}
							}
						}
					}
				}
				iccsScores.addScore(currentTaxon, currentGene, iccsScore);

				//calculate simIC score for this pair of profiles
				double simICScore = 0;
				double matchSum = 0;
				Set<Integer> taxonPhenotypes = currentTaxonProfile.getAllPhenotypes();
				Set<Integer> genePhenotypes = currentGeneProfile.getAllPhenotypes();
				for(Integer tPhenotype : taxonPhenotypes){
					if (genePhenotypes.contains(tPhenotype)){
						if (phenotypeScores.hasScore(tPhenotype, tPhenotype)){
							if (phenotypeScores.getScore(tPhenotype, tPhenotype)>-1){
								matchSum += phenotypeScores.getScore(tPhenotype,tPhenotype);
							}
						}
					}
				}
				simICScores.addScore(currentTaxon, currentGene, simICScore);

				//calculate simJ score for this pair of profiles
				int matchCount = 0;
				taxonPhenotypes = currentTaxonProfile.getAllPhenotypes();
			    genePhenotypes = currentGeneProfile.getAllPhenotypes();
				int totalCount = taxonPhenotypes.size()+genePhenotypes.size();
				for(Integer tPhenotype : taxonPhenotypes){
					if (genePhenotypes.contains(tPhenotype)){
						matchCount++;
					}
				}
				simJScores.addScore(currentTaxon,currentGene,((double)matchCount)/((double)totalCount));
				
			}
		}
		
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			for(Integer currentGene : geneProfiles.keySet()){
				Profile currentGeneProfile = geneProfiles.get(currentGene);
				u.writeOrDump("Taxon: " + u.getNodeName(currentTaxon) + "\tGene: " + u.getNodeName(currentGene),bw3);
				Set<Integer> taxonPhenotypes = currentTaxonProfile.getAllPhenotypes();
				Set<Integer> genePhenotypes = currentGeneProfile.getAllPhenotypes();
				for(Integer tPhenotype : taxonPhenotypes){
					for(Integer gPhenotype : genePhenotypes){
						if (phenotypeScores.hasScore(tPhenotype, gPhenotype)){
							if (phenotypeScores.getScore(tPhenotype, gPhenotype)>-1){
								u.writeOrDump("\t\t" + u.getNodeName(tPhenotype) + "\t" + u.getNodeName(gPhenotype) + "\t" + phenotypeScores.getScore(tPhenotype, gPhenotype),bw3);
							}
							else {
								u.writeOrDump("\t\t" + u.getNodeName(tPhenotype) + "\t" + u.getNodeName(gPhenotype) + "\t No Match",bw3);								
							}
						}
					}
				}
			}
		}
		u.writeOrDump("gene and taxon profiles overlapping on an attribute:  " + attOverlaps,bw1);

	}




	private int getQualityNodeID(Connection c, Utils u) throws SQLException{
		int result = -1;
		Statement s1 = c.createStatement();
		ResultSet attResults = s1.executeQuery("SELECT node.node_id,node.uid,simple_label(node.node_id) FROM node WHERE node.label = 'quality'");
		if(attResults.next()){
			result = attResults.getInt(1);
			u.putNodeUIDName(result,attResults.getString(2),attResults.getString(3));
		}
		return result;
	}

	private void uidCacheEntities(Connection c, Utils u) throws SQLException{
		final Statement s1 = c.createStatement();
		ResultSet entResults = s1.executeQuery("SELECT DISTINCT entity_node_id,entity_uid,simple_label(entity_node_id) FROM phenotype");
		while(entResults.next()){
			Integer ent = new Integer(entResults.getInt(1));
			String uid = entResults.getString(2);
			u.putNodeUIDName(ent,uid,entResults.getString(3));
		}
	}

	
	/**
	 * This creates and fills a Map from qualities to attributes, stored as node_ids
	 * @param c
	 * @param u
	 * @throws SQLException
	 */
	private Map<Integer,Integer> setupAttributes(Connection c, Utils u) throws SQLException{
		Map<Integer,Integer> attMap = new HashMap<Integer,Integer>();
		Statement s1 = c.createStatement();
		ResultSet attributeResults = s1.executeQuery("select quality_node_id,attribute_node_id,n.uid,simple_label(attribute_node_id) FROM quality_to_attribute " +
		"JOIN node AS n ON (n.node_id = attribute_node_id)");
		while(attributeResults.next()){
			final int quality_id = attributeResults.getInt(1);
			final int attribute_id = attributeResults.getInt(2);
			attMap.put(quality_id,attribute_id);
			u.putNodeUIDName(attribute_id,attributeResults.getString(3),attributeResults.getString(4));
		}
		return attMap;
	}

	
	private void processTaxonVariation(Connection c, Utils u, BufferedWriter reportWriter) throws SQLException{
		
		int emptyCount = 0;
		int childCount = 0;

		Statement s1 = c.createStatement();
		ResultSet taxaResults = s1.executeQuery("select node_id,uid,label from taxon");

		while(taxaResults.next()){
			int taxonID = taxaResults.getInt(1);
			String taxonUID = taxaResults.getString(2);
			String taxonlabel = taxaResults.getString(3);
			u.putNodeUIDName(taxonID, taxonUID,taxonlabel);
			Map<Integer,Profile> childProfiles = new HashMap<Integer,Profile>();
			final PreparedStatement p1 = c.prepareStatement("SELECT child.node_id,phenotype.node_id, phenotype.entity_node_id, phenotype.entity_uid, phenotype.quality_node_id,phenotype.quality_uid,phenotype.uid,simple_label(phenotype.node_id),simple_label(phenotype.entity_node_id),simple_label(phenotype.quality_node_id) FROM link " +
					"JOIN taxon AS child ON (child.node_id = link.node_id AND child.parent_node_id = ? AND link.predicate_id = (select node_id FROM node WHERE uid = 'PHENOSCAPE:exhibits'))" +
			"JOIN phenotype ON (link.object_id = phenotype.node_id) WHERE is_inferred = false");
			p1.setInt(1, taxonID);
			ResultSet linkResults = p1.executeQuery();
			while(linkResults.next()){
				if (taxonID == 19402)
					System.out.println("Parent?");
				final int child_id = linkResults.getInt(1);
				final int phenotype_id = linkResults.getInt(2);
				final int entity_id = linkResults.getInt(3);
				final String entity_uid = linkResults.getString(4);
				final int quality_id = linkResults.getInt(5);
				final String quality_uid = linkResults.getString(6);
				final String phenotype_uid = linkResults.getString(7);
				final String phenotype_label = linkResults.getString(8);
				final String entity_label = linkResults.getString(9);
				final String quality_label = linkResults.getString(10);
				u.putNodeUIDName(phenotype_id, phenotype_uid,phenotype_label);
				if (attributeMap.containsKey(quality_id)){
					final int attribute_id = attributeMap.get(quality_id);
					if (childProfiles.containsKey(child_id)){
						childProfiles.get(child_id).addPhenotype(attribute_id,entity_id, phenotype_id);
					}
					else {
						childProfiles.put(child_id, new Profile());
						childProfiles.get(child_id).addPhenotype(attribute_id,entity_id, phenotype_id);
					}
				}
				else{
					if (childProfiles.containsKey(child_id)){
						childProfiles.get(child_id).addPhenotype(qualityNodeID,entity_id, phenotype_id);
					}
					else {
						childProfiles.put(child_id, new Profile());
						childProfiles.get(child_id).addPhenotype(qualityNodeID,entity_id, phenotype_id);
					}
					taxonVariation.addExhibitor(quality_id, entity_id, child_id);
					if (badTaxonQualities.containsKey(quality_id)){
						badTaxonQualities.put(quality_id, badTaxonQualities.get(quality_id).intValue()+1);
					}
					else {
						badTaxonQualities.put(quality_id, 1);
						u.putNodeUIDName(quality_id, quality_uid, quality_label);
					}
				}
				u.putNodeUIDName(entity_id, entity_uid,quality_label);
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
									System.out.println("fail while adding taxon variation: att = " + att + "; ent = " + ent + "; taxon = " + u.getNodeUID(taxonID));
								if (currentTaxonProfile.getPhenotypeSet(att,ent).size() != childProfile.getPhenotypeSet(att, ent).size()){
									taxonVariation.addExhibitor(att, ent, taxonID);
								}
							}
							else if (currentTaxonProfile.hasPhenotypeSet(att, ent)){  //child not represented = change in sibling
								taxonVariation.addExhibitor(att,ent,taxonID);
							}
						}
					}
				}
			}
			else {
				emptyCount++;
			}
		}
		u.writeOrDump("Count of taxa with derived annotations " + taxonProfiles.keySet().size() + "; taxa with no children with phenotypes: " + emptyCount + " Total children with phenotypes: " + childCount, reportWriter);
		u.writeOrDump("\nList of qualities that were placed under quality as an attribute by default\n", reportWriter);
		for(Integer bad_id : badTaxonQualities.keySet()){
			u.writeOrDump(u.getNodeName(bad_id) + "\t" + badTaxonQualities.get(bad_id),  reportWriter);
		}
	}
	
	/**
	 * Name is a little dicy, but better than GeneVariation
	 */
	private void processGeneExpression(Connection c, Utils u, BufferedWriter reportWriter) throws SQLException{
		int annotationCount = 0;
		int usableAnnotationCount = 0;
		Set<Integer>uniqueGenes = new HashSet<Integer>();

		final PreparedStatement p2 = c.prepareStatement("SELECT gene_node_id, gene_uid, gene_label, dga.phenotype_node_id, p1.entity_node_id, p1.quality_node_id, p1.quality_uid,p1.uid,simple_label(dga.phenotype_node_id), simple_label(p1.entity_node_id),simple_label(p1.quality_node_id) FROM distinct_gene_annotation AS dga " +
		"JOIN phenotype AS p1 ON (p1.node_id = dga.phenotype_node_id) ");
		ResultSet annotationResults = p2.executeQuery();
		while(annotationResults.next()){
			final int geneID = annotationResults.getInt(1);
			final String gene_uid = annotationResults.getString(2);
			final String gene_label = annotationResults.getString(3);
			final int phenotype_id = annotationResults.getInt(4);
			final int entity_id = annotationResults.getInt(5);
			final int quality_id = annotationResults.getInt(6);
			final String quality_uid = annotationResults.getString(7);
			final String phenotype_uid = annotationResults.getString(8);
			final String phenotype_label = annotationResults.getString(9);
			final String entity_label = annotationResults.getString(10);
			final String quality_label = annotationResults.getString(11);
			u.putNodeUIDName(phenotype_id, phenotype_uid, phenotype_label);
			annotationCount++;
			uniqueGenes.add(geneID);
			if (attributeMap.containsKey(quality_id)){
				final int attribute_id = attributeMap.get(quality_id);
				if (geneProfiles.containsKey(geneID)){
					geneProfiles.get(geneID).addPhenotype(attribute_id,entity_id, phenotype_id);
				}
				else {
					geneProfiles.put(geneID, new Profile());
					geneProfiles.get(geneID).addPhenotype(attribute_id,entity_id, phenotype_id);
				}
				geneVariation.addExhibitor(attribute_id, entity_id, geneID);
				usableAnnotationCount++;
			}
			else{
				if (geneProfiles.containsKey(geneID)){
					geneProfiles.get(geneID).addPhenotype(qualityNodeID,entity_id, phenotype_id);
				}
				else {
					geneProfiles.put(geneID, new Profile());
					geneProfiles.get(geneID).addPhenotype(qualityNodeID,entity_id, phenotype_id);
				}
				geneVariation.addExhibitor(qualityNodeID, entity_id, geneID);
				if (badGeneQualities.containsKey(quality_id)){
					badGeneQualities.put(quality_id, badGeneQualities.get(quality_id).intValue()+1);
				}
				else {
					badGeneQualities.put(quality_id, 1);
				}
			}
			u.putNodeUIDName(quality_id, quality_uid,quality_label);
			u.putNodeUIDName(geneID, gene_uid,gene_label);
		}

		u.writeOrDump("Raw Genes " + uniqueGenes.size() + "; Gene annotations" + annotationCount + ";Count of genes with annotations " + geneProfiles.keySet().size() + "; Annotations with known attributes " + usableAnnotationCount, reportWriter);

		u.writeOrDump("\nList of qualities that were placed under quality as an attribute by default\n", reportWriter);
		for(Integer bad_id : badGeneQualities.keySet()){
			u.writeOrDump(u.getNodeName(bad_id) + " " + badGeneQualities.get(bad_id), reportWriter);
		}
	}
	


}
