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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.Utils;

public abstract class CountTree {

	protected enum AnnotationType{
			gene,
			taxon,
			combined,
			grandtotal
		}

	protected static final String INFINITYSTR = "inf";
	protected static final String DESTDIR = "/Users/peter/Desktop/";
	protected boolean useIC = true;
	protected final Map<Integer,List<Integer>> ontologyTable = new HashMap<Integer,List<Integer>>();
	final Set<String> nexusTaxa = new HashSet<String>();
	protected int grandSum;
	private static final String ROOTQUERY = "SELECT node_id,label FROM node WHERE (uid = '";
	private static final String ONTOLOGYTREEQUERY = "SELECT n.node_id,n.uid,simple_label(n.node_id) FROM link AS l "+   
		"JOIN node AS n ON (n.node_id = l.node_id) " +
		"WHERE l.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:is_a') " +
		"AND l.object_id = ? AND is_inferred=false";
	
	private final int rootNodeID;
	



	public CountTree(String rootUID,Utils u) {
		super();
		int rootHolder = -1;
		try {
			final Statement s = u.getStatement();
			ResultSet r = s.executeQuery(ROOTQUERY + rootUID + "')");
			if(r.next()){
				rootHolder = r.getInt(1);
				String rootNodeLabel = r.getString(2);
				u.putNodeUIDName(rootHolder, rootUID,rootNodeLabel); 
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			rootNodeID = rootHolder;
		}
	}

	public final int getRootNodeID (){
		return rootNodeID;
	}

	public void traverseOntologyTree(Map<Integer, List<Integer>> ontologyTable, Utils u) throws SQLException {
		PreparedStatement p1 = u.getPreparedStatement(ONTOLOGYTREEQUERY);
		traverseOntologyTreeAux(getRootNodeID(),ontologyTable,p1,u);
	}

	private void traverseOntologyTreeAux(int node_id, Map<Integer, List<Integer>> ontologyTable,PreparedStatement p, Utils u) throws SQLException {	
		final List<Integer> childList = new ArrayList<Integer>();
		p.setInt(1, node_id);
		ResultSet ts = p.executeQuery();
		while(ts.next()){
			final int nodeID = ts.getInt(1);
			if (!u.hasNodeName(nodeID)){
				final String nodeUID = ts.getString(2);
				final String nodeName = ts.getString(3);
				u.putNodeUIDName(nodeID,nodeUID,nodeName);
			}
			childList.add(nodeID);
		}
		ontologyTable.put(node_id,childList);
		ts.close();
		//listIntegerMembers(childList);
		for(Integer child : childList){
			traverseOntologyTreeAux(child, ontologyTable,p,u);
		}
	}

	public void writeTREfile(Integer root, Map<Integer, List<Integer>> oTable, Utils u, AnnotationType aType, Integer att) {
		if (!checkChildren(aType, att))
			return;
		StringBuilder nexusBuilder = new StringBuilder(500); 
		nexusTaxa.clear();
		Map<Integer,Integer> childCounts = null;
		Integer sums = null;
//		switch(aType){
//		case taxon: {
//			childCounts = taxonChildCountsMap.get(att);
//			sums = taxonSums.get(att);
//			break;
//		}
//		case gene:{
//			childCounts = geneChildCountsMap.get(att);
//			sums = geneSums.get(att);
//			break;
//		}
//		case combined:{
//			childCounts = combinedChildCountsMap.get(att);
//			sums = combinedSums.get(att);
//			break;
//		}
//		case grandtotal:{
//			childCounts = grandTotalChildren;
//			sums = grandSum;
//		}
//		}
		writeTREheader(nexusBuilder);
		writeTreeByNamesGeneral(root, oTable, nexusBuilder,nexusTaxa,u,childCounts,sums,att);
		writeTREfooter(nexusBuilder);
		File treOutput;
		if (att != null){
			String attStr = u.getNodeName(att);
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


	private boolean writeTreeByNamesGeneral(Integer node, Map<Integer,List<Integer>> ontologyTable,
			StringBuilder treeDescription, Set<String> node_ids, Utils u, Map<Integer,Integer> childCounts, Integer sum, Integer att) {
		if (nodeIsInternal(node,ontologyTable,u)) {
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
						boolean wrote = writeTreeByNamesGeneral(daughters.get(i), ontologyTable, treeDescription,node_ids,u, childCounts, sum, att);
						if (wrote){
							if (wroteCount<annotatedChildCount)
								treeDescription.append(",\n");
							else treeDescription.append("\n");
							wroteCount++;
						}
					}
				}
				writeTreeByNamesGeneral(daughters.get(daughters.size()-1), ontologyTable, treeDescription,node_ids,u,childCounts,sum, att);
				treeDescription.append(')');
				treeDescription.append(wrapInternalNode(sanitizeID(node,node_ids,u),node, childCounts,sum, att));
				treeDescription.append('\n');
				return true;
			}
			else {  //treat it like a tip
				String sanitizedID = wrapTipNode(sanitizeID(node,node_ids,u),node,childCounts,sum,att);
				if (!"".equals(sanitizedID)){
					treeDescription.append(sanitizedID);
					return true;
				}
				else
					return false;
			}
		}
		else {
			String sanitizedID = wrapTipNode(sanitizeID(node,node_ids,u),node,childCounts, sum,att);
			if (!"".equals(sanitizedID)){
				treeDescription.append(sanitizedID);
				return true;
			}
			else
				return false;
		}
	}
			// 

	protected boolean nodeIsInternal(Integer node_id, Map<Integer, List<Integer>> ontologyTable, Utils u) {
		final List<Integer> children = ontologyTable.get(node_id);
		if (children == null){
			System.out.println("Node with no child list: " + u.getNodeUID(node_id));
			return false;
		}
		else if (children.size() == 0)
			return false;
		else
			return true;
	}

	protected String sanitizeID(final Integer node_id, Set<String> node_ids, Utils u) {
		String newNode = u.getNodeName(node_id);
		while(node_ids.contains(newNode)){
			newNode = newNode+"+";
		}
		node_ids.add(newNode);
		return newNode;
	}

	private String wrapInternalNode(String sanitizedID, Integer node, Map<Integer,Integer>childCounts,Integer sum, Integer att) {
		String label = "";
		double value = childCounts.get(node).doubleValue()/sum.doubleValue();
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

	private String wrapTipNode(String sanitizedID, Integer node, Map<Integer, Integer>childCounts, Integer sum, Integer att) {
		String label;
		double value = childCounts.get(node).doubleValue()/sum.doubleValue();
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


	private void writeTREheader(StringBuilder nexusBuilder) {
		nexusBuilder.append("#NEXUS\n\n"); 
		nexusBuilder.append("begin trees;\n");
		nexusBuilder.append("tree tree_1 = [&R]");
	}

	private void writeTREfooter(StringBuilder nexusBuilder) { 
		nexusBuilder.append(";\n\nend;");
	}

	private boolean checkChildren(AnnotationType atype, Integer att) {
//		switch (atype){
//		case taxon: {
//			return taxonChildCountsMap.containsKey(att);
//		}
//		case gene:{
//			return geneChildCountsMap.containsKey(att);
//		}
//		case combined:{
//			return combinedChildCountsMap.containsKey(att);
//		}
//		}
		return true;
	
	}


//	/**
//	 * Return a fraction rather than an information content, which shouldn't be infinite, but might come out that way in some cases
//	 * @param attribute
//	 * @param entity
//	 * @return
//	 */
//	public double taxonFraction(Integer attribute, Integer entity) {
//		return taxonChildCountsMap.get(attribute).get(entity).doubleValue()/taxonSums.get(attribute).doubleValue();
//	}
//
//	/**
//	 * Return a fraction rather than an information content, which shouldn't be infinite, but might come out that way in some cases
//	 * @param attribute
//	 * @param entity
//	 * @return
//	 */
//	public double geneFraction(Integer attribute, Integer entity) {
//		return geneChildCountsMap.get(attribute).get(entity).doubleValue()/geneSums.get(attribute).doubleValue();
//	}

	public double grandSumFraction() {
		return 1.0/((double)grandSum);
	}

	private void listIntegerMembers(Collection<Integer> phenotypeSet) {
		for(Integer item : phenotypeSet){
			System.out.print(item.intValue() + " ");
		}
		System.out.println();
	}

}