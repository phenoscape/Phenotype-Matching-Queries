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

	private static final String CONNECTION_PROPERTIES_FILENAME = "connection.properties"; 
	private static final String TAOROOT = "TAO:0100000";
	private static final String PATOROOT = "PATO:0000001";
	private static final String PATOSHAPE = "PATO:0000052";
	private static final String PATOSIZE = "PATO:0000117";
	private static final String PATOSTRUCTURE = "PATO:0000141";
	private static final String PATOTEXTURE = "PATO:0000150";
	private static final String PATOPOSITION = "PATO:0000140";
	private static final String PATOCOLOR = "PATO:0000014";
	private static final String PATOCOUNT = "PATO:0000070";


	private static final String DESTDIR = "/Users/peter/Desktop/";

	
	private boolean useIC = true;
	
	private PreparedStatement p1;

	final Map<String, Map <String,Integer>> taxonCountsMap = new HashMap<String, Map<String,Integer>>();
	final Map<String, Map <String,Integer>> geneCountsMap = new HashMap<String, Map<String,Integer>>();
	final Map<String, Map <String,Integer>> combinedCountsMap = new HashMap<String, Map<String,Integer>>();

	final Map<String, Map <String,Integer>> taxonChildCountsMap = new HashMap<String, Map<String,Integer>>();
	final Map<String, Map <String,Integer>> geneChildCountsMap = new HashMap<String, Map<String,Integer>>();
	final Map<String, Map <String,Integer>> combinedChildCountsMap = new HashMap<String, Map<String,Integer>>();

	final Map<String, Integer>taxonSums = new HashMap<String, Integer>();
	final Map<String, Integer>geneSums = new HashMap<String, Integer>();
	final Map<String, Integer>combinedSums = new HashMap<String, Integer>();
	
	final Map<String, Integer> grandTotal = new HashMap<String, Integer>();
	
	final Map<String, Integer> grandTotalChildren = new HashMap<String,Integer>();

	final Map<String,List<String>> ontologyTable = new HashMap<String,List<String>>();

	final Set<String> nexusTaxa = new HashSet<String>();

	static Map<Integer,String> UIDCache = new HashMap<Integer,String>();
	
	private int grandSum;


	private static final int COLOR0 = 4095;
	private static final int COLOR1 = 16773120;
	private static final int COLORWHITE = 16777215;
	private static final int COLORBLACK = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AttributeCountTree countQuery = new AttributeCountTree();
		Utils u = new Utils();
		Connection c = u.openKB();

		try{
			countQuery.test(u, c,UIDCache);
		} catch (SQLException e){
			System.err.println("Problem with query");
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

	public void test(Utils u, Connection c, Map<Integer,String>uids) throws SQLException{
		build(u,c,null,null,uids);
		writeTrees(u);
	}
	
	
	public void build(Utils u, Connection c, Map<Integer,Profile> taxonProfiles, Map<Integer,Profile> geneProfiles,Map<Integer,String>uids) throws SQLException{
		if (c == null)
			return;
		final Statement s = c.createStatement();
		ResultSet rs1 = s.executeQuery("select distinct n2.label,n2.uid,entity.node_id, entity.uid, count (distinct ata.taxon_node_id), count (distinct dga.gene_node_id) from node entity " +
				"join phenotype as p on (entity.node_id = p.entity_node_id) " +
				"join quality_to_attribute as qa on (p.quality_node_id = qa.quality_node_id) " +
				"join node as n2 on (n2.node_id = qa.attribute_node_id) " +
				"left join asserted_taxon_annotation as ata on (ata.phenotype_node_id = p.node_id) " +
				"left join distinct_gene_annotation as dga on (dga.phenotype_node_id = p.node_id) " +
				"where entity.source_id = (select node_id from node where uid = 'teleost_anatomy') and entity.metatype = 'C' " +
		        "group by n2.uid,n2.label,entity.node_id, entity.uid;");
		while(rs1.next()){
			final String attribute = rs1.getString(1);     //attribute label
			final String attributeUID = rs1.getString(2);  //attribute uid
			final String nodeID = rs1.getString(3);  	   //node_id
			final String entityUID = rs1.getString(4);     //node_uid
			final int taxonCount = rs1.getInt(5);
			final int geneCount = rs1.getInt(6);
			if (attribute == null)
				continue;
			if (!taxonCountsMap.containsKey(attribute)){
				taxonCountsMap.put(attribute,new HashMap<String,Integer>());
				geneCountsMap.put(attribute, new HashMap<String, Integer>());
				combinedCountsMap.put(attribute, new HashMap<String, Integer>());
			}
			Map<String,Integer> taxonCounts = taxonCountsMap.get(attribute);
			Map<String,Integer> geneCounts = geneCountsMap.get(attribute);
			Map<String,Integer> combinedCounts = combinedCountsMap.get(attribute);
			taxonCounts.put(entityUID,taxonCount);
			geneCounts.put(entityUID, geneCount);
			combinedCounts.put(entityUID, taxonCount+geneCount);
			if (grandTotal.containsKey(entityUID)){
				grandTotal.put(entityUID, grandTotal.get(entityUID).intValue()+taxonCount+geneCount);
			}
			else
				grandTotal.put(entityUID, taxonCount+geneCount);
		}


		p1 = c.prepareStatement("select n.uid from link as l "+   
				"JOIN node as n on (n.node_id = l.node_id) " +
				"WHERE l.predicate_id = (select node_id from node where uid = 'OBO_REL:is_a') " +
		" AND l.object_id = (select node_id from node where node.uid = ?) AND is_inferred=false");


		traverseOntologyTree(TAOROOT,ontologyTable);
		System.out.println("TAO Table size is " + ontologyTable.size());
		grandSum = 0;
		for (String att : taxonCountsMap.keySet()){
			taxonChildCountsMap.put(att,new HashMap<String,Integer>());
			countChildren(TAOROOT,ontologyTable,taxonCountsMap.get(att),taxonChildCountsMap.get(att),att);
			taxonSums.put(att, taxonChildCountsMap.get(att).get(TAOROOT).intValue());
			geneChildCountsMap.put(att,new HashMap<String,Integer>());
			countChildren(TAOROOT,ontologyTable,geneCountsMap.get(att),geneChildCountsMap.get(att),att);
			geneSums.put(att, geneChildCountsMap.get(att).get(TAOROOT).intValue());
			combinedChildCountsMap.put(att,new HashMap<String,Integer>());
			countChildren(TAOROOT,ontologyTable,combinedCountsMap.get(att),combinedChildCountsMap.get(att),att);
			combinedSums.put(att, combinedChildCountsMap.get(att).get(TAOROOT).intValue());
		}
		countChildren(TAOROOT,ontologyTable,grandTotal,grandTotalChildren,null);
		grandSum += grandTotalChildren.get(TAOROOT).intValue();
	}

	private void traverseOntologyTree(String nodeUID,Map<String,List<String>> ontologyTable) throws SQLException{
		p1.setString(1, nodeUID);
		ResultSet ts = p1.executeQuery();
		List<String> childList = new ArrayList<String>();
		while(ts.next()){
			String nodeID = ts.getString(1);
			childList.add(nodeID);
		}
		ontologyTable.put(nodeUID,childList);
		ts.close();
		for(String child : childList){
			traverseOntologyTree(child, ontologyTable);
		}
	}

	private void writeTREfile(String root, Map<String, List<String>> oTable, Utils u, AnnotationType aType, String attribute){
		StringBuilder nexusBuilder = new StringBuilder(500); 
		nexusTaxa.clear();
		writeTREheader(nexusBuilder);
		writeTreeByNamesGeneral(root, oTable, nexusBuilder,nexusTaxa,u,aType,attribute);
		writeTREfooter(nexusBuilder);
		File treOutput;
		if (attribute != null)
			if (useIC){
				treOutput = new File(DESTDIR + aType + "_" + attribute + "_IC.tre");				
			}
			else
				treOutput = new File(DESTDIR + aType + "_" + attribute + ".tre");
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


	private int countChildren(String node, Map<String,List<String>> ontologyTable, Map<String, Integer> counts,Map<String, Integer> childCounts, String attribute){
		if (nodeIsInternal(node,ontologyTable)){
			int count = 0;
			for(String daughter : ontologyTable.get(node)){
				count += countChildren(daughter,ontologyTable,counts,childCounts, attribute);
			}
			if (counts.containsKey(node))
				count += counts.get(node).intValue();
			childCounts.put(node,count);
			return count;
		}
		else {
			if (counts.containsKey(node)){
				int count = counts.get(node).intValue();
				childCounts.put(node,count);
				return count;
			}
			else {
				childCounts.put(node,0);
				return 0;
			}
		}
	}

	private boolean writeTreeByNamesGeneral(String node, Map<String,List<String>> ontologyTable, StringBuilder treeDescription, Set<String> node_ids, Utils u, AnnotationType aType, String attribute) {
		Map<String,Integer> childCounts = null;
		switch(aType){
		case taxon: {
			childCounts = taxonChildCountsMap.get(attribute);
			break;
		}
		case gene:{
			childCounts = geneChildCountsMap.get(attribute);
			break;
		}
		case combined:{
			childCounts = combinedChildCountsMap.get(attribute);
			break;
		}
		case grandtotal:{
			childCounts = grandTotalChildren;
		}
		}
		if (nodeIsInternal(node,ontologyTable)) {
			final List<String> daughters = ontologyTable.get(node);
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
						boolean wrote = writeTreeByNamesGeneral(daughters.get(i), ontologyTable, treeDescription,node_ids,u, aType, attribute);
						if (wrote){
							if (wroteCount<annotatedChildCount)
								treeDescription.append(",\n");
							else treeDescription.append("\n");
							wroteCount++;
						}
					}
				}
				writeTreeByNamesGeneral(daughters.get(daughters.size()-1), ontologyTable, treeDescription,node_ids,u,aType, attribute);
				treeDescription.append(')');
				treeDescription.append(wrapInternalNode(sanitizeID(node,node_ids,u),node, aType, attribute));
				treeDescription.append('\n');
				return true;
			}
			else {  //treat it like a tip
				String sanitizedID = wrapTipNode(sanitizeID(node,node_ids,u),node,aType,attribute);
				if (!"".equals(sanitizedID)){
					treeDescription.append(sanitizedID);
					return true;
				}
				else
					return false;
			}
		}
		else {
			String sanitizedID = wrapTipNode(sanitizeID(node,node_ids,u),node,aType,attribute);
			if (!"".equals(sanitizedID)){
				treeDescription.append(sanitizedID);
				return true;
			}
			else
				return false;
		}
	}
	// 

	private boolean nodeIsInternal(String uid,Map<String,List<String>> ontologyTable){
		final List<String> children = ontologyTable.get(uid);
		if (children == null){
			System.out.println("Node with no child list: " + uid);
			return false;
		}
		else if (children.size() == 0)
			return false;
		else
			return true;
	}

	private String sanitizeID(final String node, Set<String> node_ids,Utils u){
		String newNode = node;
		newNode = u.doSubstitutions(newNode);
		while(node_ids.contains(newNode)){
			newNode = newNode+"+";
		}
		node_ids.add(newNode);
		return newNode;
	}

	private String wrapInternalNode(String sanitizedID, String rawNode, AnnotationType aType, String attribute){
		switch (aType){
		case taxon:{
			double value = taxonChildCountsMap.get(attribute).get(rawNode).doubleValue()/taxonSums.get(attribute).doubleValue();
			String label;
			if (useIC){
				if (value <= 0)
					label = "Inf";
				else
					label = Double.toString(-Math.log(value));
			}
			else
				label = Double.toString(value);
			return "[&!name=" + '"' + sanitizedID + "_" + label + '"' + "]";
		}
		case gene:{
			double value = geneChildCountsMap.get(attribute).get(rawNode).doubleValue()/geneSums.get(attribute).doubleValue();
			String label;
			if (useIC){
				if (value <= 0)
					label = "Inf";
				else
					label = Double.toString(-Math.log(value));
			}
			else
				label = Double.toString(value);
			return "[&!name=" + '"' + sanitizedID + "_" + label + '"' + "]";
		}
		case combined:{
			double value = combinedChildCountsMap.get(attribute).get(rawNode).doubleValue()/combinedSums.get(attribute).doubleValue();
			String label;
			if (useIC){
				if (value <= 0)
					label = "Inf";
				else
					label = Double.toString(-Math.log(value));
			}
			else
				label = Double.toString(value);
			return "[&!name=" + '"' + sanitizedID + "_" + label + '"' + "]";
		}
		case grandtotal:{
			double value = grandTotalChildren.get(rawNode).doubleValue()/grandSum;
			String label;
			if (useIC){
				if (value <= 0)
					label = "Inf";
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

	private String wrapTipNode(String sanitizedID, String rawNode, AnnotationType aType, String attribute){
		switch (aType){
		case taxon:{
			double value = taxonChildCountsMap.get(attribute).get(rawNode).doubleValue()/taxonSums.get(attribute).doubleValue();
			String label;
			if (useIC){
				if (value <= 0)
					label = "Inf";
				else
					label = Double.toString(-Math.log(value));
			}
			else
				label = Double.toString(value);
			return '"' + sanitizedID + "_" + label +'"' ;			
		}
		case gene:{
			double value = geneChildCountsMap.get(attribute).get(rawNode).doubleValue()/geneSums.get(attribute).doubleValue();
			String label;
			if (useIC){
				if (value <= 0)
					label = "Inf";
				else
					label = Double.toString(-Math.log(value));
			}
			else
				label = Double.toString(value);
			return '"' + sanitizedID + "_" + label +'"' ;			
		}
		case combined:{
			double value = combinedChildCountsMap.get(attribute).get(rawNode).doubleValue()/combinedSums.get(attribute).doubleValue();
			String label;
			if (useIC){
				if (value <= 0)
					label = "Inf";
				else
					label = Double.toString(-Math.log(value));
			}
			else
				label = Double.toString(value);
			return '"' + sanitizedID + "_" + label +'"' ;						
		}
		case grandtotal:{
			double value = grandTotalChildren.get(rawNode).doubleValue()/grandSum;
			String label;
			if (useIC){
				if (value <= 0)
					label = "Inf";
				else
					label = Double.toString(-Math.log(value));
			}
			else
				label = Double.toString(value);
			return '"' + sanitizedID + "_" + label + '"';
		}
		default:
			return null;
		}
	}

	private void writeTrees(Utils u){
		for (String att : taxonCountsMap.keySet()){
			writeTREfile(TAOROOT,ontologyTable,u,AnnotationType.taxon,att);
			writeTREfile(TAOROOT,ontologyTable,u,AnnotationType.gene,att);
			writeTREfile(TAOROOT,ontologyTable,u,AnnotationType.combined,att);
		}
		writeTREfile(TAOROOT,ontologyTable,u,AnnotationType.grandtotal,null);
	}


	private void writeTREheader(StringBuilder nexusBuilder){
		nexusBuilder.append("#NEXUS\n\n"); 
		nexusBuilder.append("begin trees;\n");
		nexusBuilder.append("tree tree_1 = [&R]");
	}

	private void writeTREfooter(StringBuilder nexusBuilder){ 
		nexusBuilder.append(";\n\nend;");
	}





}
