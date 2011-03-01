package phenoscape.queries;

import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import phenoscape.queries.lib.Utils;

public class TaxonomyTree {

	private static final String TAXONNODEFIELDS = "n.node_id,n.uid,simple_label(n.node_id),t.is_extinct,t.rank_label";

	private static final String ONTOLOGYTREEQUERY = "SELECT " + TAXONNODEFIELDS +  " FROM link AS l "+   
	"JOIN node AS n ON (n.node_id = l.node_id) " +
	"JOIN taxon AS t ON (t.node_id = l.node_id) " +
	"WHERE l.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:is_a') " +
	"AND l.object_id = ? AND is_inferred=false";

	private final int rootNodeID;


	private final Map<String,Integer> rankCounts;
	private int extinctCounter = 0;
	private int taxonCounter = -1;
	private final Map<Integer,Set<Integer>> taxonomyTable = new HashMap<Integer,Set<Integer>>(40000);  //This holds the taxonomy <parent, children> using node_ids
	private final Set<Integer> allTaxa;


	public TaxonomyTree(String rootUID,Utils u) throws SQLException {
		this(getRootFromKB(u,rootUID),u);
	}

	public TaxonomyTree(TaxonomicNode root, Utils u){
		super();
		allTaxa = new HashSet<Integer>();
		rankCounts = new HashMap<String,Integer>();
		rankCounts.put("phylum", 0);
		rankCounts.put("class", 0);
		rankCounts.put("order", 0);
		rankCounts.put("family", 0);
		rankCounts.put("genus", 0);
		rankCounts.put("species", 0);
		int rootHolder = -1;
		rootHolder = root.getID();
		String rootNodeLabel = root.getLabel();
		String rootRank = root.getRank();
		u.putNodeUIDName(rootHolder, root.getUID(),rootNodeLabel); 
		Integer rCount = rankCounts.get(rootRank);
		if (rCount != null){
			rankCounts.put(rootRank,rCount.intValue()+1);
		}
		rootNodeID = rootHolder;		
	}




	public int getRootNodeID (){
		return rootNodeID;
	}

	public Map<Integer,Set<Integer>> getTable(){
		return taxonomyTable;
	}

	/**
	 * 
	 * @param node_id index of the node in the kb.
	 * @param u used to report the uid of the node in case of a fatal error (exception
	 * @return true if the node has children (so is internal)
	 * @throws RuntimeException if there is no entry in the taxonomy table (meaning the presence of children is undefined) - shouldn't happen
	 */
	public boolean nodeIsInternal(Integer node_id,  Utils u) {
		final Set<Integer> children = taxonomyTable.get(node_id);
		if (children == null){
			throw new RuntimeException("Node with no child list: " + u.getNodeUID(node_id));
		}
		else if (children.size() == 0)
			return false;
		else
			return true;
	}


	public Set<Integer> getAllTaxa(){
		return allTaxa;
	}




	public void traverseOntologyTree(Utils u) throws SQLException {
		PreparedStatement p1 = u.getPreparedStatement(ONTOLOGYTREEQUERY);
		allTaxa.add(getRootNodeID());
		traverseOntologyTreeAux(getRootNodeID(),taxonomyTable,p1,u);
		taxonCounter = taxonomyTable.size();
	}

	private void traverseOntologyTreeAux(int parentID, Map<Integer, Set<Integer>> ontologyTable,PreparedStatement p, Utils u) throws SQLException {	
		final Set<Integer> childList = new HashSet<Integer>();
		Collection<TaxonomicNode> children = getChildNodesFromKB(u, parentID, p);
		for(TaxonomicNode child : children){
			final int childID = child.getID();
			if (!u.hasNodeName(childID)){
				final String childUID = child.getUID();
				final String childName = child.getLabel();
				u.putNodeUIDName(childID,child.getUID(),child.getLabel());
				if (child.getExtinct())
					extinctCounter++;
				final String rankLabel = child.getRank();
				Integer rCount = rankCounts.get(rankLabel);
				if (rCount != null)
					rankCounts.put(rankLabel,rCount.intValue()+1);
			}
			childList.add(childID);
			allTaxa.add(childID);
		}
		ontologyTable.put(parentID,childList);
		for(Integer child : childList){
			traverseOntologyTreeAux(child, ontologyTable,p,u);
		}
	}

	public void report(Utils u, Writer w){
		u.writeOrDump("Taxon Count = " + taxonCounter,w);
		u.writeOrDump("Extinct Taxa = " + extinctCounter,w);
		u.writeOrDump("Taxa at phylum rank = " + rankCounts.get("phylum"),w);
		u.writeOrDump("Taxa at class rank = " + rankCounts.get("class"),w);
		u.writeOrDump("Taxa at order rank = " + rankCounts.get("order"),w);
		u.writeOrDump("Taxa at family rank = " + rankCounts.get("family"),w);
		u.writeOrDump("Taxa at genus rank = " + rankCounts.get("genus"),w);
		u.writeOrDump("Taxa at species rank = " + rankCounts.get("species"),w);
	}

	private static final String ROOTQUERY = "SELECT " + TAXONNODEFIELDS + " FROM node AS n " +
	"JOIN taxon AS t ON (t.node_id = n.node_id) "+
	"WHERE (n.uid = ?)";

	static TaxonomicNode getRootFromKB(Utils u, String rootUID) throws SQLException{
		final PreparedStatement p = u.getPreparedStatement(ROOTQUERY);
		p.setString(1,rootUID);
		ResultSet r = p.executeQuery();
		if(r.next()){
			return new TaxonomicNode(r);
		}
		else{
			throw new RuntimeException("Failed to find a node with UID " + rootUID + " to root the taxonomy");
		}
	}

	
	Collection<TaxonomicNode> getChildNodesFromKB(Utils u, int parentID, PreparedStatement p) throws SQLException{
		final Collection<TaxonomicNode> result = new HashSet<TaxonomicNode>();
		p.setInt(1, parentID);
		ResultSet ts = p.executeQuery();
		while (ts.next()){
			TaxonomicNode n = new TaxonomicNode(ts);
			result.add(n);
		}
		return result;
	}
	
	
	public void traverseOntologyTreeUsingTaxonNodes(Map<TaxonomicNode,Integer> taxa, Utils u){
		allTaxa.add(getRootNodeID());
		traverseOntologyTreeAuxUsingTaxonNodes(getRootNodeID(),taxonomyTable,taxa,u);
		taxonCounter = taxonomyTable.size();
	}

	private void traverseOntologyTreeAuxUsingTaxonNodes(int parentID, Map<Integer, Set<Integer>> ontologyTable,Map<TaxonomicNode,Integer> taxa, Utils u)  {	
		final Set<Integer> childList = new HashSet<Integer>();
		Collection<TaxonomicNode> children = getChildNodesFromCollection(taxa,parentID);
		for(TaxonomicNode child : children){
			final int childID = child.getID();
			if (!u.hasNodeName(childID)){
				final String childUID = child.getUID();
				final String childName = child.getLabel();
				u.putNodeUIDName(childID,child.getUID(),child.getLabel());
				if (child.getExtinct())
					extinctCounter++;
				final String rankLabel = child.getRank();
				Integer rCount = rankCounts.get(rankLabel);
				if (rCount != null)
					rankCounts.put(rankLabel,rCount.intValue()+1);
			}
			childList.add(childID);
			allTaxa.add(childID);
		}
		ontologyTable.put(parentID,childList);
		for(Integer child : childList){
			traverseOntologyTreeAuxUsingTaxonNodes(child, ontologyTable,taxa,u);
		}
	}

	Collection<TaxonomicNode> getChildNodesFromCollection(Map<TaxonomicNode,Integer> taxa, int parentID){
		final Collection<TaxonomicNode> result = new HashSet<TaxonomicNode>();
		for(TaxonomicNode n : taxa.keySet()){
			if (taxa.get(n).intValue()== parentID)
				result.add(n);
		}
		return result;
	}


	static class TaxonomicNode{

		private int id;
		private String uid;
		private String label;
		private boolean extinct;
		private String rank;

		TaxonomicNode(int p_id, String p_uid, String p_label, boolean p_extinct, String p_rank){
			id = p_id;
			uid = p_uid;
			label = p_label;
			extinct = p_extinct;
			rank = p_rank;
		}

		TaxonomicNode(ResultSet r) throws SQLException{
			id = r.getInt(1);
			uid = r.getString(2);
			label = r.getString(3);
			extinct = r.getBoolean(4);
			rank = r.getString(5);
		}

		int getID(){
			return id;
		}

		String getUID(){
			return uid;
		}

		String getLabel(){
			return label;
		}

		boolean getExtinct(){
			return extinct;
		}

		String getRank(){
			return rank;
		}
	}

}
