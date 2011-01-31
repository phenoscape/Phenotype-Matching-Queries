package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import phenoscape.queries.CountTree.AnnotationType;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.Utils;


public class EntityCountTree extends CountTree {


	private final Map<Integer,Map<Integer,Integer>> taxonCountsMap = new HashMap<Integer,Map<Integer,Integer>>();
	private final Map<Integer,Map<Integer,Integer>> geneCountsMap = new HashMap<Integer,Map<Integer,Integer>>();
	private final Map<Integer,Map<Integer,Integer>> combinedCountsMap = new HashMap<Integer,Map<Integer,Integer>>();
	private final Map<Integer,Map<Integer,Integer>> taxonChildCountsMap = new HashMap<Integer,Map<Integer,Integer>>();
	private final Map<Integer,Map<Integer,Integer>> geneChildCountsMap = new HashMap<Integer,Map<Integer,Integer>>();
	private final Map<Integer,Map<Integer,Integer>> combinedChildCountsMap = new HashMap<Integer,Map<Integer,Integer>>();
	
	private final Map<Integer, Integer> taxonSums = new HashMap<Integer, Integer>();
	private final Map<Integer, Integer> geneSums = new HashMap<Integer, Integer>();
	private final Map<Integer, Integer> combinedSums = new HashMap<Integer, Integer>();
	private final Map<Integer, Integer> grandTotal = new HashMap<Integer, Integer>();
	private final Map<Integer, Integer> grandTotalChildren = new HashMap<Integer,Integer>();
	
	
	public EntityCountTree(String rootUID, Utils u) {
		super(rootUID, u);
	}

	public void build(Utils u, Map<Integer,Profile> taxonProfiles, Map<Integer,Profile> geneProfiles) throws SQLException {
		System.out.println("Starting Ontology traversal");
		traverseOntologyTree(ontologyTable,u);
		System.out.println("Finished Ontology traversal");

		for(Integer taxonKey : taxonProfiles.keySet()){

			Profile taxonProfile = taxonProfiles.get(taxonKey);
			Set<Integer>attributes = taxonProfile.getUsedAttributes();
			Set<Integer>entities = taxonProfile.getUsedEntities();
			for(Integer att : attributes){
				if (!taxonCountsMap.containsKey(att)){
					taxonCountsMap.put(att,new HashMap<Integer,Integer>());
					if (!combinedCountsMap.containsKey(att))
						combinedCountsMap.put(att, new HashMap<Integer, Integer>());
				}
				Map<Integer,Integer> taxonCounts = taxonCountsMap.get(att);       //phenotype -> count
				Map<Integer,Integer> combinedCounts = combinedCountsMap.get(att); //phenotype -> count
				for(Integer ent : entities){
					int taxonCount;
					if (taxonCounts.containsKey(ent)){
						taxonCount = taxonCounts.get(ent);
					}
					else{
						taxonCount = 0;
					}
					taxonCount++;
					taxonCounts.put(ent,taxonCount);
					int combinedCount;
					if (combinedCounts.containsKey(ent)){
						combinedCount = combinedCounts.get(ent);
					}
					else{
						combinedCount = 0;
					}
					combinedCount++;
					combinedCounts.put(ent, combinedCount);
					if (getGrandTotal().containsKey(ent)){                            //phenotype -> count
						getGrandTotal().put(ent, getGrandTotal().get(ent).intValue()+1);
					}
					else
						getGrandTotal().put(ent, 1);

				}
			}
		}
		for (Integer geneKey : geneProfiles.keySet()){
			Profile geneProfile = geneProfiles.get(geneKey);
			Set<Integer>attributes = geneProfile.getUsedAttributes();
			Set<Integer>entities = geneProfile.getUsedEntities();
			for(Integer att : attributes){
				//String attributeLabel = u.lookupIDToName(uids.get(att));  // no need to key these by label - switch to node_id indexing
				if (!geneCountsMap.containsKey(att)){
					geneCountsMap.put(att,new HashMap<Integer,Integer>());
					if (!combinedCountsMap.containsKey(att))
						combinedCountsMap.put(att, new HashMap<Integer, Integer>());
				}
				Map<Integer,Integer> geneCounts = geneCountsMap.get(att);
				Map<Integer,Integer> combinedCounts = combinedCountsMap.get(att);
				for(Integer ent : entities){
					int geneCount;
					if (geneCounts.containsKey(ent)){
						geneCount = geneCounts.get(ent);
					}
					else{
						geneCount = 0;
					}
					geneCount++;
					geneCounts.put(ent,geneCount);
					int combinedCount;
					if (combinedCounts.containsKey(ent)){
						combinedCount = combinedCounts.get(ent);
					}
					else{
						combinedCount = 0;
					}
					combinedCount++;
					combinedCounts.put(ent, combinedCount);
					if (getGrandTotal().containsKey(ent)){
						getGrandTotal().put(ent, getGrandTotal().get(ent).intValue()+1);
					}
					else
						getGrandTotal().put(ent, 1);
				}
			}
		}

		grandSum = 0;
		BufferedWriter dumpWriter = null;
		try {
			dumpWriter = new BufferedWriter(new FileWriter("EntityTreeTest.txt"));
			for (Integer att : combinedCountsMap.keySet()){
				if (taxonCountsMap.containsKey(att)){
					taxonChildCountsMap.put(att,new HashMap<Integer,Integer>());
					countChildren(getRootNodeID(),ontologyTable,taxonCountsMap.get(att),taxonChildCountsMap.get(att),att,u,dumpWriter);
					taxonSums.put(att, taxonChildCountsMap.get(att).get(getRootNodeID()).intValue());
				}
				if (geneCountsMap.containsKey(att)){
					geneChildCountsMap.put(att,new HashMap<Integer,Integer>());
					countChildren(getRootNodeID(),ontologyTable,geneCountsMap.get(att),geneChildCountsMap.get(att),att,u,dumpWriter);
					geneSums.put(att, geneChildCountsMap.get(att).get(getRootNodeID()).intValue());
				}
				combinedChildCountsMap.put(att,new HashMap<Integer,Integer>());
				countChildren(getRootNodeID(),ontologyTable,combinedCountsMap.get(att),combinedChildCountsMap.get(att),att,u,dumpWriter);
				combinedSums.put(att, combinedChildCountsMap.get(att).get(getRootNodeID()).intValue());
			}
			countChildren(getRootNodeID(),ontologyTable,getGrandTotal(),grandTotalChildren,null,u,dumpWriter);
			grandSum = grandTotalChildren.get(getRootNodeID()).intValue();
			System.out.println("Grandsum = " + grandSum);

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

	}

	protected int countChildren(Integer node, Map<Integer, List<Integer>> ontologyTable, Map<Integer, Integer> entityCounts,
			Map<Integer, Integer> childCounts, Integer att, Utils u, BufferedWriter dumpWriter) {
		if (nodeIsInternal(node,ontologyTable,u)){
			int count = 0;
			if (att == null){
				u.writeOrDump("Node is: " + u.getNodeName(node.intValue()),dumpWriter);
			}
			for(Integer daughter : ontologyTable.get(node)){
				if (att == null){
					u.writeOrDump("   Child is: " + u.getNodeName(daughter.intValue()),dumpWriter);
				}
				count += countChildren(daughter,ontologyTable,entityCounts,childCounts, att,u,dumpWriter);
			}
			if (entityCounts.containsKey(node))
				count += entityCounts.get(node).intValue();
			childCounts.put(node,count);
			return count;
		}
		else {
			if (entityCounts == null){
				System.out.println("Null count table?, node = " + node);
			}
			if (entityCounts.containsKey(node)){
				int count = entityCounts.get(node).intValue();
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
	public double combinedFraction(Integer attribute, Integer entity) {
		Map<Integer,Integer>attributeTable = combinedChildCountsMap.get(attribute);
		if (attributeTable != null){
			Integer num = attributeTable.get(entity);
			if (num != null){
				double numerator = num.doubleValue();
				double denominator = combinedSums.get(attribute).doubleValue();
				return numerator/denominator;
			}
		}
		return 0.0;
	}

	
	
	
	
	public Map<Integer, Integer> getGrandTotal() {
		return grandTotal;
	}

}
