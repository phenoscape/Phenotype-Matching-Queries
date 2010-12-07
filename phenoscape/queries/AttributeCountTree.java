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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.Utils;

public class AttributeCountTree {


	enum AnnotationType{
		gene,
		taxon,
		combined,
		grandtotal
	}

	private static final String CAROROOT = "CARO:0000000";
	private static final String PATOROOT = "PATO:0000001";
	private static final String PATOSHAPE = "PATO:0000052";
	private static final String PATOSIZE = "PATO:0000117";
	private static final String PATOSTRUCTURE = "PATO:0000141";
	private static final String PATOTEXTURE = "PATO:0000150";
	private static final String PATOPOSITION = "PATO:0000140";
	private static final String PATOCOLOR = "PATO:0000014";
	private static final String PATOCOUNT = "PATO:0000070";

	private static final String INFINITYSTR = "inf";


	private static final String DESTDIR = "/Users/peter/Desktop/";


	private boolean useIC = true;


	final Map<Integer, Map <Integer,Integer>> taxonCountsMap = new HashMap<Integer, Map<Integer,Integer>>();
	final Map<Integer, Map <Integer,Integer>> geneCountsMap = new HashMap<Integer, Map<Integer,Integer>>();
	final Map<Integer, Map <Integer,Integer>> combinedCountsMap = new HashMap<Integer, Map<Integer,Integer>>();

	final Map<Integer, Map <Integer,Integer>> taxonChildCountsMap = new HashMap<Integer, Map<Integer,Integer>>();
	final Map<Integer, Map <Integer,Integer>> geneChildCountsMap = new HashMap<Integer, Map<Integer,Integer>>();
	final Map<Integer, Map <Integer,Integer>> combinedChildCountsMap = new HashMap<Integer, Map<Integer,Integer>>();

	final Map<Integer, Integer>taxonSums = new HashMap<Integer, Integer>();
	final Map<Integer, Integer>geneSums = new HashMap<Integer, Integer>();
	final Map<Integer, Integer>combinedSums = new HashMap<Integer, Integer>();

	final Map<Integer, Integer> grandTotal = new HashMap<Integer, Integer>();

	final Map<Integer, Integer> grandTotalChildren = new HashMap<Integer,Integer>();

	final Map<Integer,List<Integer>> ontologyTable = new HashMap<Integer,List<Integer>>();

	final Set<String> nexusTaxa = new HashSet<String>();

	static Map<Integer,String> UIDCache = new HashMap<Integer,String>();

	private int grandSum;

	private int rootNodeID;

	final static private String ROOTQUERY = "SELECT node_id FROM node WHERE (uid = '"; 

	final static private String ONTOLOGYTREEQUERY = "SELECT n.node_id,n.uid FROM link AS l "+   
	"JOIN node AS n ON (n.node_id = l.node_id) " +
	"WHERE l.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:is_a') " +
	"AND l.object_id = ? AND is_inferred=false";


	private static final int COLOR0 = 4095;
	private static final int COLOR1 = 16773120;
	private static final int COLORWHITE = 16777215;
	private static final int COLORBLACK = 0;

	/**
	 * @param args
	 */
//	public static void main(String[] args) {
//		AttributeCountTree countQuery = new AttributeCountTree();
//		Utils u = new Utils();
//		Connection c = u.openKB();
//
//		try{
//			countQuery.test(u, c,UIDCache);
//		} catch (SQLException e){
//			System.err.println("Problem with query");
//			e.printStackTrace();
//		}
//		finally{
//			try {
//				c.close();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}

	public void test(Utils u, Connection c, Map<Integer,String>uids, String rootUID) throws SQLException{
		build(u,c,null,null,uids,rootUID);
		writeTrees(u,uids);
	}


	public void build(Utils u, Connection c, Map<Integer,Profile> taxonProfiles, Map<Integer,Profile> geneProfiles,Map<Integer,String>uids,String rootUID) throws SQLException{
		if (c == null)
			return;
		System.out.println("Starting Ontology traversal");
		traverseOntologyTree(c,ontologyTable,rootUID);
		System.out.println("Finished Ontology traversal");
		final Statement s = c.createStatement();
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
				Map<Integer,Integer> taxonCounts = taxonCountsMap.get(att);
				Map<Integer,Integer> combinedCounts = combinedCountsMap.get(att);
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
					if (grandTotal.containsKey(ent)){
						grandTotal.put(ent, grandTotal.get(ent).intValue()+1);
					}
					else
						grandTotal.put(ent, 1);
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
					if (grandTotal.containsKey(ent)){
						grandTotal.put(ent, grandTotal.get(ent).intValue()+1);
					}
					else
						grandTotal.put(ent, 1);
				}
			}
		}
		
		grandSum = 0;
		for (Integer att : combinedCountsMap.keySet()){
			if (taxonCountsMap.containsKey(att)){
				taxonChildCountsMap.put(att,new HashMap<Integer,Integer>());
				countChildren(rootNodeID,ontologyTable,taxonCountsMap.get(att),taxonChildCountsMap.get(att),att);
				taxonSums.put(att, taxonChildCountsMap.get(att).get(rootNodeID).intValue());
			}
			if (geneCountsMap.containsKey(att)){
				geneChildCountsMap.put(att,new HashMap<Integer,Integer>());
				countChildren(rootNodeID,ontologyTable,geneCountsMap.get(att),geneChildCountsMap.get(att),att);
				geneSums.put(att, geneChildCountsMap.get(att).get(rootNodeID).intValue());
			}
			combinedChildCountsMap.put(att,new HashMap<Integer,Integer>());
			countChildren(rootNodeID,ontologyTable,combinedCountsMap.get(att),combinedChildCountsMap.get(att),att);
			combinedSums.put(att, combinedChildCountsMap.get(att).get(rootNodeID).intValue());
		}
		countChildren(rootNodeID,ontologyTable,grandTotal,grandTotalChildren,null);
		grandSum = grandTotalChildren.get(rootNodeID).intValue();
		System.out.println("Grandsum = " + grandSum);
	}

	private void traverseOntologyTree(Connection c, Map<Integer, List<Integer>> ontologyTable, String rootUID) throws SQLException{
		final Statement s = c.createStatement();
		ResultSet r = s.executeQuery(ROOTQUERY + rootUID + "')");
		if(r.next()){
			rootNodeID = r.getInt(1);
		}
		UIDCache.put(rootNodeID, rootUID);
		PreparedStatement p1 = c.prepareStatement(ONTOLOGYTREEQUERY);
		traverseOntologyTreeAux(rootNodeID,ontologyTable,p1);
	}


	private void traverseOntologyTreeAux(int node_id, Map<Integer, List<Integer>> ontologyTable, PreparedStatement p)throws SQLException{	
		final List<Integer> childList = new ArrayList<Integer>();
		p.setInt(1, node_id);
		ResultSet ts = p.executeQuery();
		while(ts.next()){
			int nodeID = ts.getInt(1);
			if (!UIDCache.containsKey(nodeID)){
				final String nodeUID = ts.getString(2);
				UIDCache.put(nodeID, nodeUID);
			}
			childList.add(nodeID);
		}
		ontologyTable.put(node_id,childList);
		ts.close();
		//listIntegerMembers(childList);
		for(Integer child : childList){
			traverseOntologyTreeAux(child, ontologyTable,p);
		}
	}

	private void writeTREfile(Integer root, Map<Integer, List<Integer>> oTable, Utils u, AnnotationType aType, Integer att, Map<Integer, String> uids){
		if (!checkChildren(aType, att))
			return;
		StringBuilder nexusBuilder = new StringBuilder(500); 
		nexusTaxa.clear();
		writeTREheader(nexusBuilder);
		writeTreeByNamesGeneral(root, oTable, nexusBuilder,nexusTaxa,u,aType,att);
		writeTREfooter(nexusBuilder);
		File treOutput;
		if (att != null){
			String attStr = u.doSubstitutions(uids.get(att));
			if (useIC){
				treOutput = new File(DESTDIR + aType + "_" + attStr+ "_IC.tre");				
			}
			else
				treOutput = new File(DESTDIR + aType + "_" + attStr + ".tre");
		}
		else if (useIC){
			treOutput = new File(DESTDIR + "grandTotal_IC.tre");
		}
		else 
			treOutput = new File(DESTDIR + "grandTotal.tre");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(treOutput));
			bw.write(nexusBuilder.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}


	private int countChildren(Integer node, Map<Integer, List<Integer>> ontologyTable, Map<Integer, Integer> entityCounts,Map<Integer, Integer> childCounts, Integer att){
		if (nodeIsInternal(node,ontologyTable)){
			int count = 0;
			for(Integer daughter : ontologyTable.get(node)){
				count += countChildren(daughter,ontologyTable,entityCounts,childCounts, att);
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

	private boolean writeTreeByNamesGeneral(Integer node, Map<Integer,List<Integer>> ontologyTable, StringBuilder treeDescription, Set<String> node_ids, Utils u, AnnotationType aType, Integer att) {
		Map<Integer,Integer> childCounts = null;
		switch(aType){
		case taxon: {
			childCounts = taxonChildCountsMap.get(att);
			break;
		}
		case gene:{
			childCounts = geneChildCountsMap.get(att);
			break;
		}
		case combined:{
			childCounts = combinedChildCountsMap.get(att);
			break;
		}
		case grandtotal:{
			childCounts = grandTotalChildren;
		}
		}
		if (nodeIsInternal(node,ontologyTable)) {
			final List<Integer> daughters = ontologyTable.get(node);
			int annotatedChildCount = 0;	
			for(int i = 0; i< daughters.size();i++){
				if (childCounts.get(daughters.get(i)) != null && childCounts.get(daughters.get(i)).intValue() > 0)
					annotatedChildCount++;
			}
			if (annotatedChildCount >0 ){
				int wroteCount =0;
				treeDescription.append('(');
				for(int i=0;i<daughters.size()-1;i++){    //can't use for-each here, index matters
					if (childCounts.get(daughters.get(i)) != null && childCounts.get(daughters.get(i)).intValue() > 0){
						boolean wrote = writeTreeByNamesGeneral(daughters.get(i), ontologyTable, treeDescription,node_ids,u, aType, att);
						if (wrote){
							if (wroteCount<annotatedChildCount)
								treeDescription.append(",\n");
							else treeDescription.append("\n");
							wroteCount++;
						}
					}
				}
				writeTreeByNamesGeneral(daughters.get(daughters.size()-1), ontologyTable, treeDescription,node_ids,u,aType, att);
				treeDescription.append(')');
				treeDescription.append(wrapInternalNode(sanitizeID(node,node_ids,u),node, aType, att));
				treeDescription.append('\n');
				return true;
			}
			else {  //treat it like a tip
				String sanitizedID = wrapTipNode(sanitizeID(node,node_ids,u),node,aType,att);
				if (!"".equals(sanitizedID)){
					treeDescription.append(sanitizedID);
					return true;
				}
				else
					return false;
			}
		}
		else {
			String sanitizedID = wrapTipNode(sanitizeID(node,node_ids,u),node,aType,att);
			if (!"".equals(sanitizedID)){
				treeDescription.append(sanitizedID);
				return true;
			}
			else
				return false;
		}
	}
	// 

	private boolean nodeIsInternal(Integer node_id,Map<Integer, List<Integer>> ontologyTable){
		final List<Integer> children = ontologyTable.get(node_id);
		if (children == null){
			System.out.println("Node with no child list: " + UIDCache.get(node_id));
			return false;
		}
		else if (children.size() == 0)
			return false;
		else
			return true;
	}

	private String sanitizeID(final Integer node_id, Set<String> node_ids,Utils u){
		String newNode = u.doSubstitutions(UIDCache.get(node_id));
		while(node_ids.contains(newNode)){
			newNode = newNode+"+";
		}
		node_ids.add(newNode);
		return newNode;
	}

	private String wrapInternalNode(String sanitizedID, Integer node, AnnotationType aType, Integer att){
		String label = "";
		switch (aType){
		case taxon:{
			double value = taxonChildCountsMap.get(att).get(node).doubleValue()/taxonSums.get(att).doubleValue();
			if (useIC){
				if (value <= 0)
					label = INFINITYSTR;
				else
					label = Double.toString(-Math.log(value));
			}
			else
				label = Double.toString(value);
			return "[&!name=" + '"' + sanitizedID + "_" + label + '"' + "]";
		}
		case gene:{
			double value = geneChildCountsMap.get(att).get(node).doubleValue()/geneSums.get(att).doubleValue();
			if (useIC){
				if (value <= 0)
					label = INFINITYSTR;
				else
					label = Double.toString(-Math.log(value));
			}
			else
				label = Double.toString(value);
			return "[&!name=" + '"' + sanitizedID + "_" + label + '"' + "]";
		}
		case combined:{
			double value = combinedChildCountsMap.get(att).get(node).doubleValue()/combinedSums.get(att).doubleValue();
			if (useIC){
				if (value <= 0)
					label = INFINITYSTR;
				else
					label = Double.toString(-Math.log(value));
			}
			else
				label = Double.toString(value);
			return "[&!name=" + '"' + sanitizedID + "_" + label + '"' + "]";
		}
		case grandtotal:{
			double value = grandTotalChildren.get(node).doubleValue()/grandSum;
			if (useIC){
				if (value <= 0)
					label = INFINITYSTR;
				else
					label = Double.toString(-Math.log(value));
			}
			else
				label = Double.toString(value);
			return "[&!name=" + '"' + sanitizedID + "_" + label + '"' + "]";
		}
		default:
			return "[&!name=" + '"' + sanitizedID + '"' + "]";
		}
	}

	private String wrapTipNode(String sanitizedID, Integer node, AnnotationType aType, Integer att){
		String label;
		double value = -1.0;  // just to keep the compiler happy
		switch (aType){
		case taxon:{
			value = taxonChildCountsMap.get(att).get(node).doubleValue()/taxonSums.get(att).doubleValue();
			break;
		}
		case gene:{
			value = geneChildCountsMap.get(att).get(node).doubleValue()/geneSums.get(att).doubleValue();
			break;
		}
		case combined:{
			value = combinedChildCountsMap.get(att).get(node).doubleValue()/combinedSums.get(att).doubleValue();
			break;
		}
		case grandtotal:{
			value = grandTotalChildren.get(node).doubleValue()/grandSum;
			break;
		}
		}
		if (useIC){
			if (value <= 0)
				label = INFINITYSTR;
			else
				label = Double.toString(-Math.log(value));
		}
		else
			label = Double.toString(value);
		return '"' + sanitizedID + "_" + label +'"' ;						
	}

	public void writeTrees(Utils u, Map<Integer, String> uids){
		for (Integer att : taxonCountsMap.keySet()){
			writeTREfile(rootNodeID,ontologyTable,u,AnnotationType.taxon,att,uids);
			writeTREfile(rootNodeID,ontologyTable,u,AnnotationType.gene,att,uids);
			writeTREfile(rootNodeID,ontologyTable,u,AnnotationType.combined,att,uids);
		}
		writeTREfile(rootNodeID,ontologyTable,u,AnnotationType.grandtotal,null,uids);
	}


	private void writeTREheader(StringBuilder nexusBuilder){
		nexusBuilder.append("#NEXUS\n\n"); 
		nexusBuilder.append("begin trees;\n");
		nexusBuilder.append("tree tree_1 = [&R]");
	}

	private void writeTREfooter(StringBuilder nexusBuilder){ 
		nexusBuilder.append(";\n\nend;");
	}

	private boolean checkChildren(AnnotationType atype, Integer att){
		switch (atype){
		case taxon: {
			return taxonChildCountsMap.containsKey(att);
		}
		case gene:{
			return geneChildCountsMap.containsKey(att);
		}
		case combined:{
			return combinedChildCountsMap.containsKey(att);
		}
		}
		return true;

	}

	/**
	 * Return a fraction rather than an information content, which shouldn't be infinite, but might come out that way in some cases
	 * @param attribute
	 * @param entity
	 * @return
	 */
	public double combinedFraction(Integer attribute,Integer entity){
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

	/**
	 * Return a fraction rather than an information content, which shouldn't be infinite, but might come out that way in some cases
	 * @param attribute
	 * @param entity
	 * @return
	 */
	public double taxonFraction(Integer attribute,Integer entity){
		return taxonChildCountsMap.get(attribute).get(entity).doubleValue()/taxonSums.get(attribute).doubleValue();
	}

	/**
	 * Return a fraction rather than an information content, which shouldn't be infinite, but might come out that way in some cases
	 * @param attribute
	 * @param entity
	 * @return
	 */
	public double geneFraction(Integer attribute,Integer entity){
		return geneChildCountsMap.get(attribute).get(entity).doubleValue()/geneSums.get(attribute).doubleValue();
	}

	public double grandSumFraction(){
		return 1.0/((double)grandSum);
	}
	
	private void listIntegerMembers(Collection<Integer> phenotypeSet) {
		for(Integer item : phenotypeSet){
			System.out.print(item.intValue() + " ");
		}
		System.out.println();
	}

	
}
