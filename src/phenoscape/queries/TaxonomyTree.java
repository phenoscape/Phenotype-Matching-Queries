package phenoscape.queries;

import java.io.BufferedWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phenoscape.queries.lib.Utils;

public class TaxonomyTree {

	private static final String ROOTQUERY = "SELECT n.node_id,simple_label(n.node_id),t.rank_label FROM node AS n " +
		"JOIN taxon AS t ON (t.node_id = n.node_id) "+
		"WHERE (n.uid = ?)";
	private static final String ONTOLOGYTREEQUERY = "SELECT n.node_id,n.uid,simple_label(n.node_id),t.is_extinct,t.rank_label FROM link AS l "+   
		"JOIN node AS n ON (n.node_id = l.node_id) " +
		"JOIN taxon AS t ON (t.node_id = l.node_id) " +
		"WHERE l.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:is_a') " +
		"AND l.object_id = ? AND is_inferred=false";
	
	private final int rootNodeID;
	
	
	private final Map<String,Integer> rankCounts;
	private int extinctCounter = 0;
	private int taxonCounter = -1;


	public TaxonomyTree(String rootUID,Utils u) {
		super();
		rankCounts = new HashMap<String,Integer>();
		rankCounts.put("phylum", new Integer(0));
		rankCounts.put("class", new Integer(0));
		rankCounts.put("order", new Integer(0));
		rankCounts.put("family",new Integer(0));
		rankCounts.put("genus",new Integer(0));
		rankCounts.put("species",new Integer(0));
		int rootHolder = -1;
		try {
			final PreparedStatement p = u.getPreparedStatement(ROOTQUERY);
			p.setString(1,rootUID);
			ResultSet r = p.executeQuery();
			if(r.next()){
				rootHolder = r.getInt(1);
				String rootNodeLabel = r.getString(2);
				String rootLabel = r.getString(3);
				u.putNodeUIDName(rootHolder, rootUID,rootNodeLabel); 
				Integer rCount = rankCounts.get(rootLabel);
				if (rCount != null)
					rankCounts.put(rootLabel,rCount.intValue()+1);
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
		taxonCounter = ontologyTable.size();
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
				final boolean nodeExtinct = ts.getBoolean(4);
				final String rankLabel = ts.getString(5);
				u.putNodeUIDName(nodeID,nodeUID,nodeName);
				if (nodeExtinct)
					extinctCounter++;
				Integer rCount = rankCounts.get(rankLabel);
				if (rCount != null)
					rankCounts.put(rankLabel,rCount.intValue()+1);
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
	
	public void report(Utils u, BufferedWriter bw){
		u.writeOrDump("Taxon Count = " + taxonCounter,bw);
		u.writeOrDump("Extinct Taxa = " + extinctCounter,bw);
		u.writeOrDump("Taxa at phylum rank = " + rankCounts.get("phylum"),bw);
		u.writeOrDump("Taxa at class rank = " + rankCounts.get("class"),bw);
		u.writeOrDump("Taxa at order rank = " + rankCounts.get("order"),bw);
		u.writeOrDump("Taxa at family rank = " + rankCounts.get("family"),bw);
		u.writeOrDump("Taxa at genus rank = " + rankCounts.get("genus"),bw);
		u.writeOrDump("Taxa at species rank = " + rankCounts.get("species"),bw);
	}

}
