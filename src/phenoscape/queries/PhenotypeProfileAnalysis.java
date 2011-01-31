package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import phenoscape.queries.lib.PhenotypeScoreTable;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.ProfileScoreSet;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;



public class PhenotypeProfileAnalysis {

	private static final String CAROROOT = "CARO:0000000";
	private static final String TAOROOT = "TAO:0100000";
	private static final String PATOROOT = "PATO:0000001";
	private static final String TTOROOT = "TTO:0";
	private static final String OSTARIOPHYSIROOT = "TTO:302";
	

	private static final String TAXONREPORTFILENAME = "../TaxonVariationReport.txt";
	private static final String GENEREPORTFILENAME = "../GeneVariationReport.txt";
	private static final String PHENOTYPEMATCHREPORTFILENAME = "../PhenotypeMatchReport.txt";
	private static final String PROFILEMATCHREPORTFILENAME = "../ProfileMatchReport.txt";
	
		
	private static final String TAXONCHILDQUERY = "SELECT child.node_id,link.node_id, phenotype.node_id,phenotype.entity_node_id, phenotype.entity_uid, phenotype.quality_node_id,phenotype.quality_uid,phenotype.uid,simple_label(phenotype.node_id),simple_label(phenotype.entity_node_id),simple_label(phenotype.quality_node_id) FROM link " +
	"JOIN taxon AS child ON (child.node_id = link.node_id AND child.parent_node_id = ? AND link.predicate_id = (select node_id FROM node WHERE uid = 'PHENOSCAPE:exhibits'))" +
	"JOIN phenotype ON (link.object_id = phenotype.node_id) WHERE is_inferred = false";
	
	private static final String GENEANNOTATIONQUERY = 		
		"SELECT genotype_node_id, genotype_uid, genotype_label, qga.phenotype_node_id, p1.entity_node_id, p1.entity_uid, p1.quality_node_id, p1.quality_uid,p1.uid,simple_label(qga.phenotype_node_id), simple_label(p1.entity_node_id),simple_label(p1.quality_node_id) FROM queryable_gene_annotation AS qga " +
		"JOIN phenotype AS p1 ON (p1.node_id = qga.phenotype_node_id)";
	
	//Note: these queries are currently identical, but they are constructed separately to allow divergence
	private static final String TAXONPHENOTYPENEIGHBORQUERY = 
		"SELECT target.node_id FROM node AS entity " +
	    "JOIN link ON (entity.node_id=link.node_id AND link.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:inheres_in_part_of')) " + 
	    "JOIN node AS target ON (target.node_id = link.object_id) WHERE entity.node_id = ? " +
        "GROUP BY entity.uid, target.uid,target.node_id";
	private static final String GENEPHENOTYPENEIGHBORQUERY =
		"SELECT target.node_id FROM node AS entity " +
		"JOIN link ON (entity.node_id=link.node_id AND link.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:inheres_in_part_of')) " + 
		"JOIN node AS target ON (target.node_id = link.object_id) WHERE entity.node_id = ? " +
		"GROUP BY entity.uid, target.uid, target.node_id";
	

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
		PhenotypeProfileAnalysis listQuery = new PhenotypeProfileAnalysis();
		Utils u = new Utils();
		u.openKB();
		File outFile1 = new File(TAXONREPORTFILENAME);
		File outFile2 = new File(GENEREPORTFILENAME);
		File outFile3 = new File(PHENOTYPEMATCHREPORTFILENAME);
		File outFile4 = new File(PROFILEMATCHREPORTFILENAME);
		BufferedWriter bw1 = null;
		BufferedWriter bw2 = null;
		BufferedWriter bw3 = null;
		BufferedWriter bw4 = null;
		Date today;
		DateFormat dateFormatter;

		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT);
		today = new Date();
		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
		String timeStamp = dateFormatter.format(today) + " " + timeFormatter.format(today);		
		try {
			bw1 = new BufferedWriter(new FileWriter(outFile1));
			bw2 = new BufferedWriter(new FileWriter(outFile2));
			bw3 = new BufferedWriter(new FileWriter(outFile3));
			bw4 = new BufferedWriter(new FileWriter(outFile4));
			u.writeOrDump(timeStamp, bw1);
			u.writeOrDump(timeStamp, bw2);
			u.writeOrDump(timeStamp, bw3);
			u.writeOrDump(timeStamp, bw4);
			listQuery.process(u, bw1, bw2, bw3, bw4);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				u.closeKB();
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


	private void process(Utils u,BufferedWriter bw1, BufferedWriter bw2, BufferedWriter bw3, BufferedWriter bw4) throws IOException,SQLException{
		uidCacheEntities(u);    // this will retrieve entities that appear in EQ's

		qualityNodeID = getQualityNodeID(u);   //set to the root of PATO

		attributeMap = u.setupAttributes();
		
		// process taxa annotations
		System.out.println("Building Taxonomy Tree");
		TaxonomyTree t = new TaxonomyTree(TTOROOT,u);
		Map<Integer,List<Integer>> taxonomyTable = new HashMap<Integer,List<Integer>>(40000);
		t.traverseOntologyTree(taxonomyTable,u);
		processTaxonVariation(taxonomyTable, u, bw1);
		t.report(u, bw1);
		taxonVariation.variationReport(u,bw1);	

		processGeneExpression(u, bw2);
		geneVariation.variationReport(u, bw2);
		bw2.close();
		
		/* These need to happen after the profiles have been constructed, since we don't want to count taxon annotations that don't reflect change */
		EntityCountTree entityCounts = new EntityCountTree(TAOROOT, u);  
		entityCounts.build(u,taxonProfiles,geneProfiles);   //will be CAROROOT when things are cleaned up
		
		PhenotypeCountTree phenotypeCounts = new PhenotypeCountTree(PATOROOT,u);
		phenotypeCounts.build(u, taxonProfiles, geneProfiles);
		
		//counts.writeTrees(u,UIDCache);
		Map <Integer,Set<Integer>> phenotypeNeighborCache = new HashMap<Integer,Set<Integer>>();
		System.out.println("Building entity neighbors of taxon phenotypes");
		buildTaxonEntityNeighbors(phenotypeNeighborCache,u);
		
		System.out.println("Building entity neighbors of gene phenotypes");
		buildGeneEntityNeighbors(phenotypeNeighborCache,u);

		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		
		
		System.out.println("Done building entity neighbors; building phenotype match cache");
		int attOverlaps = buildPhenotypeMatchCache(phenotypeNeighborCache, phenotypeScores, entityCounts, u);
		u.writeOrDump("gene and taxon profiles overlapping on an attribute:  " + attOverlaps,bw1);
		bw1.close();
		System.out.println("Finished building phenotype match cache; Writing Phenotype match summary");
		writePhenotypeMatchSummary(phenotypeScores,u,bw3);		
		bw3.close();
		
		System.out.println("Finished Writing Phenotype match summary");
		System.out.println("Calculating Profile Scores");
		
		//List<ProfileScoreSet> results = new ArrayList<ProfileScoreSet>(1000);
		u.writeOrDump("Taxon \t Gene \t taxon phenotypes \t gene phenotypes \t maxIC \t iccs \t simIC \t simJ",bw4);

		long zeroCount = 0;
		u.writeOrDump("Sizes: Taxon profiles: " + taxonProfiles.keySet().size() + "; Gene profiles: " + geneProfiles.keySet().size(), null);
		for(Integer currentTaxon : taxonProfiles.keySet()){
			Profile currentTaxonProfile = taxonProfiles.get(currentTaxon);
			Set<Integer> taxonPhenotypes = currentTaxonProfile.getAllPhenotypes();
			for(Integer currentGene : geneProfiles.keySet()){
				Profile currentGeneProfile = geneProfiles.get(currentGene);
				Set<Integer> genePhenotypes = currentGeneProfile.getAllPhenotypes();
				
				ProfileScoreSet result = new ProfileScoreSet(currentTaxon, currentGene,taxonPhenotypes, genePhenotypes);
				//results.add(result);
				
				//calculate maxIC score for this pair of profiles
				double maxPhenotypeMatch = Double.NEGATIVE_INFINITY;
				for(Integer taxonPhenotype : taxonPhenotypes){
					for(Integer genePhenotype : currentGeneProfile.getAllPhenotypes()){
						if (phenotypeScores.hasScore(taxonPhenotype, genePhenotype)){
							if (phenotypeScores.getScore(taxonPhenotype, genePhenotype) > maxPhenotypeMatch){
								maxPhenotypeMatch = phenotypeScores.getScore(taxonPhenotype, genePhenotype);
							}
						}
					}
				}
				result.setMaxICScore(maxPhenotypeMatch);

				//calculate ICCS score for this pair of profiles
				double iccsScoreTG = 0;
				List<Double> maxByTaxon = new ArrayList<Double>(taxonPhenotypes.size());
				for (Integer taxonPhenotype : taxonPhenotypes){
					double bestIC = Double.NEGATIVE_INFINITY;
					for (Integer genePhenotype: genePhenotypes){
						if (phenotypeScores.hasScore(taxonPhenotype, genePhenotype)){
							if (phenotypeScores.getScore(taxonPhenotype, genePhenotype) > bestIC){
								bestIC = phenotypeScores.getScore(taxonPhenotype, genePhenotype);
							}
						}
					}
					maxByTaxon.add(bestIC);
				}
				double sum =0;
				for(Double s : maxByTaxon){
					if (!s.isInfinite())
						sum += s.doubleValue();
				}
				iccsScoreTG = sum/((double)maxByTaxon.size());
				result.setICCSScore(iccsScoreTG);

				//calculate simIC score for this pair of profiles
				double simICScore = 0;
				double matchSum = 0;
				double totalSum = 0;
				for(Integer tPhenotype : taxonPhenotypes){
					if (genePhenotypes.contains(tPhenotype)){
						if (phenotypeScores.hasScore(tPhenotype, tPhenotype)){
							if (phenotypeScores.getScore(tPhenotype, tPhenotype)>-1){
								matchSum += phenotypeScores.getScore(tPhenotype,tPhenotype);
							}
						}
					}
				}
				for(Integer tPhenotype : taxonPhenotypes){
					if (phenotypeScores.hasScore(tPhenotype, tPhenotype) && phenotypeScores.getScore(tPhenotype, tPhenotype)>-1){
						totalSum += phenotypeScores.getScore(tPhenotype,tPhenotype);
					}
				}
				for (Integer gPhenotype : genePhenotypes){
					if (phenotypeScores.hasScore(gPhenotype, gPhenotype) && phenotypeScores.getScore(gPhenotype, gPhenotype)>-1){
						totalSum += phenotypeScores.getScore(gPhenotype,gPhenotype);
					}
				}
				if (totalSum == 0){
					System.err.println("Unexpected value in simIC: matchSum = " + matchSum + "; totalSum = " + totalSum);
					simICScore = 0;
				}
				else
					simICScore = matchSum/totalSum;
				result.setSimICScore(simICScore);

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
				result.setSimJScore(((double)matchCount)/((double)totalCount));	
				if (result.isNonZero())
					result.writeScores(u, bw4);
				else
					zeroCount++;
			}
		}
		u.writeOrDump("Pairs with zero score = " + zeroCount, bw4);
		bw4.close();
	}




	private int getQualityNodeID(Utils u) throws SQLException{
		int result = -1;
		Statement s1 = u.getStatement();
		ResultSet attResults = s1.executeQuery("SELECT node.node_id,node.uid,simple_label(node.node_id) FROM node WHERE node.label = 'quality'");
		if(attResults.next()){
			result = attResults.getInt(1);
			u.putNodeUIDName(result,attResults.getString(2),attResults.getString(3));
		}
		return result;
	}

	private void uidCacheEntities(Utils u) throws SQLException{
		final Statement s1 = u.getStatement();
		ResultSet entResults = s1.executeQuery("SELECT DISTINCT entity_node_id,entity_uid,simple_label(entity_node_id) FROM phenotype");
		while(entResults.next()){
			Integer ent = new Integer(entResults.getInt(1));
			String uid = entResults.getString(2);
			u.putNodeUIDName(ent,uid,entResults.getString(3));
		}
	}

	

	/**
	 * 
	 * @param taxonomyTree 
	 * @param u
	 * @param reportWriter
	 * @throws SQLException
	 */
	private void processTaxonVariation(Map<Integer, List<Integer>> taxonomyTree, Utils u, BufferedWriter reportWriter) throws SQLException{		
		int emptyCount = 0;
		int childCount = 0;
		Set<Integer> taxonSet = taxonomyTree.keySet();

		
		for (Integer taxonID : taxonSet){
			Map<Integer,Profile> childProfiles = new HashMap<Integer,Profile>();
			final PreparedStatement p1 = u.getPreparedStatement(TAXONCHILDQUERY);
			p1.setInt(1, taxonID);
			ResultSet linkResults = p1.executeQuery();
			while(linkResults.next()){
				final int child_id = linkResults.getInt(1);
				final int link_id = linkResults.getInt(2);
				final int phenotype_id = linkResults.getInt(3);
				final int entity_id = linkResults.getInt(4);
				final String entity_uid = linkResults.getString(5);
				final int quality_id = linkResults.getInt(6);
				final String quality_uid = linkResults.getString(7);
				final String phenotype_uid = linkResults.getString(8);
				final String phenotype_label = linkResults.getString(9);
				final String entity_label = linkResults.getString(10);
				final String quality_label = linkResults.getString(11);
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
					if (badTaxonQualities.containsKey(quality_id)){
						badTaxonQualities.put(quality_id, badTaxonQualities.get(quality_id).intValue()+1);
					}
					else {
						badTaxonQualities.put(quality_id, 1);
						u.putNodeUIDName(quality_id, quality_uid, quality_label);
					}
				}
				u.putNodeUIDName(entity_id, entity_uid,entity_label);
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
							if (!childProfile.isEmpty()){       //if nothing has been reported for this child then absent annotations are uninformative
								if (childProfile.hasPhenotypeSet(att, ent)){
									//System.out.println("att = " + att.intValue() + " ;ent = " + ent.intValue() + " ;parent profile size " + currentTaxonProfile.getPhenotypeSet(att,ent).size() + " ; child profile size" + childProfile.getPhenotypeSet(att, ent).size());
									if (currentTaxonProfile.getPhenotypeSet(att,ent).size() != childProfile.getPhenotypeSet(att, ent).size()){
										taxonVariation.addExhibitor(att, ent, taxonID);
									}
								}
								else if (currentTaxonProfile.hasPhenotypeSet(att, ent)){  //child not represented = change in sibling
									taxonVariation.addExhibitor(att,ent,taxonID);
								}
							}
							else {
								System.out.println("Empty Child: " + u.getNodeName(childID));
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
	private void processGeneExpression(Utils u, BufferedWriter reportWriter) throws SQLException{
		int annotationCount = 0;
		int usableAnnotationCount = 0;
		Set<Integer>uniqueGenes = new HashSet<Integer>();

		final Statement s2 = u.getStatement();
		ResultSet annotationResults = s2.executeQuery(GENEANNOTATIONQUERY);

		while(annotationResults.next()){
			final int geneID = annotationResults.getInt(1);
			final String gene_uid = annotationResults.getString(2);
			final String gene_label = annotationResults.getString(3);
			final int phenotype_id = annotationResults.getInt(4);
			final int entity_id = annotationResults.getInt(5);
			final String entity_uid = annotationResults.getString(6);
			final int quality_id = annotationResults.getInt(7);
			final String quality_uid = annotationResults.getString(8);
			final String phenotype_uid = annotationResults.getString(9);
			final String phenotype_label = annotationResults.getString(10);
			final String entity_label = annotationResults.getString(11);
			final String quality_label = annotationResults.getString(12);
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
			u.putNodeUIDName(entity_id, entity_uid, entity_label);
			u.putNodeUIDName(geneID, gene_uid,gene_label);
		}

		u.writeOrDump("Raw Genes " + uniqueGenes.size() + "; Gene annotations" + annotationCount + ";Count of genes with annotations " + geneProfiles.keySet().size() + "; Annotations with known attributes " + usableAnnotationCount, reportWriter);

		u.writeOrDump("\nList of qualities that were placed under quality as an attribute by default\n", reportWriter);
		for(Integer bad_id : badGeneQualities.keySet()){
			u.writeOrDump(u.getNodeName(bad_id) + " " + badGeneQualities.get(bad_id), reportWriter);
		}
	}
	
	private void buildTaxonEntityNeighbors(Map <Integer,Set<Integer>> phenotypeNeighborCache, Utils u) throws SQLException{
		final PreparedStatement p3 = u.getPreparedStatement(TAXONPHENOTYPENEIGHBORQUERY);
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

	}

	private void buildGeneEntityNeighbors(Map <Integer,Set<Integer>> phenotypeNeighborCache, Utils u) throws SQLException{
		final PreparedStatement p4 = u.getPreparedStatement(GENEPHENOTYPENEIGHBORQUERY);

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

	}
	
	private int buildPhenotypeMatchCache(Map <Integer,Set<Integer>> phenotypeNeighborCache, PhenotypeScoreTable phenotypeScores, EntityCountTree entityCounts, Utils u){
		int hitCount = 0;
		int attOverlaps = 0;
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
													System.out.println("Adding self score: " + u.getNodeName(tPhenotype) + " = " + (-1*Math.log(bestMatch)));
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
														u.listIntegerMembers(tpNeighbors,null);
														u.listIntegerMembers(gpNeighbors,null);
														for(Integer ent : matches){
															double matchScore = entityCounts.combinedFraction(curAtt, ent);
															System.out.println("Entity: " + ent + "Attribute: " + curAtt + "; score: " + matchScore);
														}
														phenotypeScores.addScore(tPhenotype, gPhenotype, 0.0);
													}
													if (!phenotypeScores.hasScore(tPhenotype, tPhenotype)){
														Set<Integer>tsNeighbors = phenotypeNeighborCache.get(tPhenotype);
														bestMatch = Double.MAX_VALUE;  //we're using fractions, so minimize
														for(Integer ent : tsNeighbors){  //simple (but slow) way to get the entity
															double matchScore = entityCounts.combinedFraction(curAtt, ent);
															if (matchScore<bestMatch && matchScore > 0.0){
																bestMatch = matchScore;
															}
														}	
														System.out.println("Adding self score: " + u.getNodeName(tPhenotype) + " = " + (-1*Math.log(bestMatch)));
														phenotypeScores.addScore(tPhenotype, tPhenotype, (-1*Math.log(bestMatch)));

													}
													if (!phenotypeScores.hasScore(gPhenotype, gPhenotype)){
														Set<Integer>gsNeighbors = phenotypeNeighborCache.get(gPhenotype);
														bestMatch = Double.MAX_VALUE;  //we're using fractions, so minimize
														for(Integer ent : gsNeighbors){  //simple (but slow) way to get the entity
															double matchScore = entityCounts.combinedFraction(curAtt, ent);
															if (matchScore<bestMatch && matchScore > 0.0){
																bestMatch = matchScore;
															}
														}	
														System.out.println("Adding self score: " + u.getNodeName(gPhenotype) + " = " + (-1*Math.log(bestMatch)));
														phenotypeScores.addScore(gPhenotype, gPhenotype, (-1*Math.log(bestMatch)));
														
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
		return attOverlaps;
	}
	
	
	private void writePhenotypeMatchSummary(PhenotypeScoreTable phenotypeScores,Utils u, BufferedWriter bw3){
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
				for(Integer tPhenotype : taxonPhenotypes){
					for(Integer gPhenotype : genePhenotypes){
						if (phenotypeScores.hasScore(tPhenotype, gPhenotype)){
							if (phenotypeScores.getScore(tPhenotype, gPhenotype)>-1){
								u.writeOrDump("Taxon: " + u.getNodeName(currentTaxon) + "\tGene: " + u.getNodeName(currentGene) + "\ttPhenotype: " + u.getNodeName(tPhenotype) + "\tgPhenotype: " +  u.getNodeName(gPhenotype)  + "\t" + phenotypeScores.getScore(tPhenotype, gPhenotype),bw3);
							}
						}
					}
				}
			}
		}

	}
}
