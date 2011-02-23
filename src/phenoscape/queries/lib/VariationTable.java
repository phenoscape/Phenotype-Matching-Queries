package phenoscape.queries.lib;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



// Could have a class hierarchy with this and Profile under a common parent, or maybe a common member that does the table operations...

public class VariationTable {
	
	private Map<Integer,Map<Integer,Set<Integer>>> table = new HashMap<Integer,Map<Integer,Set<Integer>>>();  //Attributes, entities, taxa
	
	
	public void addExhibitor(Integer entity_node_id, Integer attribute_node_id, Integer exhibitor_node_id){
		if (table.containsKey(attribute_node_id)){
			Map<Integer,Set<Integer>> attribute_entry = table.get(attribute_node_id);
			if (attribute_entry.containsKey(entity_node_id)){
				Set<Integer> exhibitorSet = attribute_entry.get(entity_node_id);
				exhibitorSet.add(exhibitor_node_id);
			}
			else {
				Set<Integer> exhibitorSet = new HashSet<Integer>();
				exhibitorSet.add(exhibitor_node_id);
				attribute_entry.put(entity_node_id,exhibitorSet);
			}
		}
		else {
			Map<Integer,Set<Integer>> attribute_entry = new HashMap<Integer,Set<Integer>>();
			Set<Integer> exhibitorSet = new HashSet<Integer>();
			exhibitorSet.add(exhibitor_node_id);
			attribute_entry.put(entity_node_id,exhibitorSet);
			table.put(attribute_node_id, attribute_entry);
		}
	}
	
	
	public boolean hasExhibitorSet(Integer entity, Integer attribute){
		if (table.containsKey(attribute))
			return (table.get(attribute).containsKey(entity));
		else
			return false;
	}
	
	
	public Set<Integer> getExhibitorSet (Integer entity, Integer attribute){
		return table.get(attribute).get(entity);
	}
	
	public Set<Integer> getUsedAttributes(){
		return table.keySet();
	}
	
	public Set<Integer> getUsedEntities(){
		Set<Integer>result = new HashSet<Integer>();
		for (Map<Integer,Set<Integer>> attribute_value : table.values()){
			result.addAll(attribute_value.keySet());
		}
		return result;
	}
	
	public void variationReport(Utils u, Writer bw){
		
		int sum = 0;
		Map<Integer,Integer> attributeSums = new HashMap<Integer,Integer>();
		Map<Integer,Integer> entitySums = new HashMap<Integer,Integer>();
		Map<Integer,String> taxonList = new HashMap<Integer,String>();
		for(Integer att : getUsedAttributes()){
			for (Integer ent : getUsedEntities()){
				if (hasExhibitorSet(ent,att)){					
					Set <Integer> taxa = getExhibitorSet(ent,att);
					sum += taxa.size(); 
					if (attributeSums.containsKey(att)){
						attributeSums.put(att,attributeSums.get(att).intValue()+taxa.size());
					}
					else
						attributeSums.put(att,taxa.size());
					if (entitySums.containsKey(ent)){
						entitySums.put(ent,entitySums.get(ent).intValue()+taxa.size());
					}
					else
						entitySums.put(ent,taxa.size());
					for(Integer taxon : taxa){
						String addition = "\n\t" + u.getNodeName(ent) + "\t" + u.getNodeName(att);
						if (!taxonList.containsKey(taxon))
							taxonList.put(taxon, "\n" + u.getNodeName(taxon));
						String tl = taxonList.get(taxon);
						taxonList.put(taxon, tl+ addition);					
					}
				}
			}
		}
		u.writeOrDump("Summary of detected variation",bw);
		int attributeTotal = 0;
		u.writeOrDump("-- Attribute Summary --",bw);
		for(Integer att : attributeSums.keySet()){
			final int attributeCount =  attributeSums.get(att).intValue();
			attributeTotal += attributeCount;
			if (u.hasNodeUID(att)){
				u.writeOrDump(u.getNodeName(att) + "\t" + attributeCount,bw);
			}
			else
				u.writeOrDump(att.intValue() + "\t" + attributeCount,bw);
		}
		u.writeOrDump("Total:\t" + attributeTotal, bw);
		int entityTotal = 0;
		u.writeOrDump("\n\n-- Entity Summary --",bw);
		for (Integer ent : entitySums.keySet()){
			final int entityCount = entitySums.get(ent).intValue();
			entityTotal += entityCount;
			if (u.hasNodeUID(ent)){
				u.writeOrDump(u.getNodeName(ent) + "\t" + entityCount,bw);
			}
			else
				u.writeOrDump(ent.intValue() + "\t" + entityCount,bw);
		}
		u.writeOrDump("Total:\t" + attributeTotal, bw);
		u.writeOrDump("\n\n-- Exhibitor (Taxon/Gene) Summary --",bw);
		for (Integer taxon : taxonList.keySet()){
			u.writeOrDump(taxonList.get(taxon),bw);
		}
		u.writeOrDump("\n Exhibitor count is:\t" + taxonList.size(),bw);
		u.writeOrDump("\n\n Total is:\t" + sum,bw);
	}


	public boolean taxonExhibits(Integer ent, Integer att,Integer taxon) {
		if (hasExhibitorSet(ent, att)){
			if (getExhibitorSet(ent,att).contains(taxon))
				return true;
		}
		return false;
	}

}
