package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import phenoscape.queries.CountTree.AnnotationType;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.Utils;

public class PhenotypeCountTree extends CountTree {

	Integer taxonSum;
	Integer geneSum;
	Integer combinedSum;

	final Map<Integer, Set<Integer>> taxonSetsMap = new HashMap<Integer, Set<Integer>>();
	final Map<Integer, Set<Integer>> geneSetsMap = new HashMap<Integer, Set<Integer>>();
	final Map<Integer, Set<Integer>> combinedSetsMap = new HashMap<Integer, Set<Integer>>();

	final Map<Integer, Integer> taxonCountsMap = new HashMap<Integer, Integer>();
	final Map<Integer, Integer> geneCountsMap = new HashMap<Integer, Integer>();
	final Map<Integer, Integer> combinedCountsMap = new HashMap<Integer, Integer>();

	final Map<Integer, Integer> taxonChildCountsMap = new HashMap<Integer, Integer>();
	final Map<Integer, Integer> geneChildCountsMap = new HashMap<Integer, Integer>();
	final Map<Integer, Integer> combinedChildCountsMap = new HashMap<Integer, Integer>();

	final Map<Integer, Integer> phenotypeOccurances = new HashMap<Integer, Integer>();


	final Map<Integer, Integer> grandTotal = new HashMap<Integer, Integer>();
	final Map<Integer, Integer> grandTotalChildren = new HashMap<Integer,Integer>();

	final Map<Integer,Map<Integer,Set<Integer>>> phenotypeCounts = new HashMap<Integer,Map<Integer,Set<Integer>>>();
	
	private static final String TAXONPHENOQUERY = "SELECT phenotype.node_id FROM link " +
	"JOIN taxon AS t ON (t.node_id = link.node_id AND link.predicate_id = (select node_id FROM node WHERE uid = 'PHENOSCAPE:exhibits'))" +
	"JOIN phenotype ON (link.object_id = phenotype.node_id) WHERE is_inferred = false";
	
	private static final String GENEANNOTATIONQUERY = 		
		"SELECT dga.phenotype_node_id FROM distinct_gene_annotation AS dga ";

	public PhenotypeCountTree(String rootUID, Utils u) {
		super(rootUID, u);
	}

	public void build(Utils u, Map<Integer,Profile> taxonProfiles, Map<Integer,Profile> geneProfiles) throws SQLException {
		System.out.println("Starting Ontology traversal");
		traverseOntologyTree(ontologyTable,u);
		System.out.println("Finished Ontology traversal");

		for(Integer taxon : taxonProfiles.keySet()){
			Profile taxonProfile = taxonProfiles.get(taxon);
			Set<Integer>attributes = taxonProfile.getUsedAttributes();
			Set<Integer>entities = taxonProfile.getUsedEntities();
			
			Set<Integer>phenotypes = taxonProfile.getAllPhenotypes();
			for(Integer phenotype : phenotypes){
				int taxonCount;
				if (taxonCountsMap.containsKey(phenotype)){
					taxonCount = taxonCountsMap.get(phenotype);
				}
				else{
					taxonCount = 0;
				}
				taxonCount++;
				taxonCountsMap.put(phenotype,taxonCount);
				int combinedCount;
				if (combinedCountsMap.containsKey(phenotype)){
					combinedCount = combinedCountsMap.get(phenotype);
				}
				else{
					combinedCount = 0;
				}
				combinedCount++;
				combinedCountsMap.put(phenotype, combinedCount);
				if (grandTotal.containsKey(phenotype)){                            //ent -> count
					grandTotal.put(phenotype, grandTotal.get(phenotype).intValue()+1);
				}
				else
					grandTotal.put(taxon, 1);
			}
		}


		for (Integer gene : geneProfiles.keySet()){
			Profile geneProfile = geneProfiles.get(gene);
			Set<Integer>phenotypes = geneProfile.getAllPhenotypes();
			for(Integer phenotype : phenotypes){
				int geneCount;
				if (geneCountsMap.containsKey(phenotype)){
					geneCount = geneCountsMap.get(phenotype);
				}
				else{
					geneCount = 0;
				}
				geneCount++;
				geneCountsMap.put(phenotype,geneCount);
				int combinedCount;
				if (combinedCountsMap.containsKey(phenotype)){
					combinedCount = combinedCountsMap.get(phenotype);
				}
				else{
					combinedCount = 0;
				}
				combinedCount++;
				combinedCountsMap.put(phenotype, combinedCount);
				if (grandTotal.containsKey(phenotype)){
					grandTotal.put(gene, grandTotal.get(phenotype).intValue()+1);
				}
				else
					grandTotal.put(phenotype, 1);
			}
		}

		grandSum = 0;
		BufferedWriter dumpWriter = null;
		try {
			dumpWriter = new BufferedWriter(new FileWriter("PhenotypeTreeTest.txt"));
			countChildren(getRootNodeID(),ontologyTable,taxonCountsMap,taxonChildCountsMap,u,dumpWriter);
			taxonSum = taxonChildCountsMap.get(getRootNodeID());
			countChildren(getRootNodeID(),ontologyTable,geneCountsMap,geneChildCountsMap,u,dumpWriter);
			geneSum = geneChildCountsMap.get(getRootNodeID());
			countChildren(getRootNodeID(),ontologyTable,combinedCountsMap,combinedChildCountsMap,u,dumpWriter);
			combinedSum = combinedChildCountsMap.get(getRootNodeID());
			grandSum = combinedSum;
			//writeTree(getRootNodeID(),ontologyTable,combinedCountsMap, combinedChildCountsMap,u,dumpWriter,0);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				dumpWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Grandsum = " + grandSum);
	}

	private void writeTree(Integer node, Map<Integer,List<Integer>> ontologyTable, Map<Integer,Integer> phenotypeCounts, Map<Integer,Integer>childCounts, Utils u, BufferedWriter dumpWriter, int indent) throws IOException{
		char[] indentBuf = new char[indent];
		for(int i=0;i<indent;i++){
			indentBuf[i] = ' ';
		}
		dumpWriter.write(indentBuf);
		dumpWriter.write(u.getNodeName(node) + " " + phenotypeCounts.get(node) + " " + childCounts.get(node) + "\n");
		if (nodeIsInternal(node,ontologyTable,u)){
			for(Integer daughter : ontologyTable.get(node)){
				writeTree(daughter,ontologyTable,phenotypeCounts, childCounts,u, dumpWriter,indent+1);
			}
		}
	}
	
	
	
	protected int countChildren(Integer node, Map<Integer, List<Integer>> ontologyTable, Map<Integer, Integer> phenotypeCounts,
			Map<Integer, Integer> childCounts,Utils u, BufferedWriter dumpWriter) {
		if (nodeIsInternal(node,ontologyTable,u)){
			int count = 0;
			for(Integer daughter : ontologyTable.get(node)){
				count += countChildren(daughter,ontologyTable,phenotypeCounts,childCounts, u,dumpWriter);
			}
			if (phenotypeCounts.containsKey(node)){
				//System.out.println(u.getNodeName(node) + "( " + node + ") has " + phenotypeCounts.get(node).intValue() + " counts");
				count += phenotypeCounts.get(node).intValue();
			}
			childCounts.put(node,count);
			return count;
		}
		else {
			if (phenotypeCounts.containsKey(node)){
				//System.out.println(u.getNodeName(node) + "( " + node + ") has " + phenotypeCounts.get(node).intValue() + " counts");
				int count = phenotypeCounts.get(node).intValue();
				childCounts.put(node,count);
				return count;
			}
			else {
				childCounts.put(node,0);
				return 0;
			}
		}
	}

	public void writeTrees(Utils u) {
		for (Integer att : taxonCountsMap.keySet()){
			//writeTREfile(rootNodeID,ontologyTable,u,AnnotationType.taxon,att);
			//writeTREfile(rootNodeID,ontologyTable,u,AnnotationType.gene,att);
			writeTREfile(getRootNodeID(),ontologyTable,u,AnnotationType.combined,att);
		}
		writeTREfile(getRootNodeID(),ontologyTable,u,AnnotationType.grandtotal,null);
	}

	/**
	 * Return a fraction rather than an information content, which shouldn't be infinite, but might come out that way in some cases
	 * @param attribute
	 * @param entity
	 * @return
	 */
	public double combinedFraction(Integer phenotype) {
		Map<Integer,Integer>countTable = combinedChildCountsMap;
		if (countTable != null){
			Integer num = countTable.get(phenotype);
			if (num != null){
				double numerator = num.doubleValue();
				double denominator = combinedSum.doubleValue();
				return numerator/denominator;
			}
		}
		return 0.0;
	}


}
