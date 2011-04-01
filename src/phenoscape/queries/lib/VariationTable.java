package phenoscape.queries.lib;

import java.io.BufferedWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;



// Could have a class hierarchy with this and Profile under a common parent, or maybe a common member that does the table operations...

public class VariationTable {
	
	public enum VariationType {TAXON,GENE};
	
	final private Map<Integer,Map<Integer,Set<Integer>>> table = new HashMap<Integer,Map<Integer,Set<Integer>>>();  //entities, attributes, taxa
	
	final private VariationType repType;
	
	public VariationTable(VariationType myType){
		repType = myType;
	}
	
	
	public void addExhibitor(Integer entity_node_id, Integer attribute_node_id, Integer exhibitor_node_id){
		if (table.containsKey(entity_node_id)){
			Map<Integer,Set<Integer>> entity_entry = table.get(entity_node_id);
			if (entity_entry.containsKey(attribute_node_id)){
				Set<Integer> exhibitorSet = entity_entry.get(attribute_node_id);
				exhibitorSet.add(exhibitor_node_id);
			}
			else {
				Set<Integer> exhibitorSet = new HashSet<Integer>();
				exhibitorSet.add(exhibitor_node_id);
				entity_entry.put(attribute_node_id,exhibitorSet);
			}
		}
		else {
			Map<Integer,Set<Integer>> entity_entry = new HashMap<Integer,Set<Integer>>();
			Set<Integer> exhibitorSet = new HashSet<Integer>();
			exhibitorSet.add(exhibitor_node_id);
			entity_entry.put(attribute_node_id,exhibitorSet);
			table.put(entity_node_id, entity_entry);
		}
	}
	
	public void reportTable(){
		System.out.println("After Table = " + table + "; size = " + table.size());		
	}
	
	
	public boolean hasExhibitorSet(Integer entity, Integer attribute){
		if (table.containsKey(entity))
			return (table.get(entity).containsKey(attribute));
		else
			return false;
	}
	
	
	public Set<Integer> getExhibitorSet (Integer entity, Integer attribute){
		return table.get(entity).get(attribute);
	}
	
	public Set<Integer> getUsedEntities(){
		return table.keySet();
	}
	
	public Set<Integer> getUsedAttributes(){
		Set<Integer>result = new HashSet<Integer>();
		for (Map<Integer,Set<Integer>> entity_value : table.values()){
			result.addAll(entity_value.keySet());
		}
		return result;
	}
	
	public void variationReport(Utils u, Writer bw){		
		int sum = 0;
		Map<Integer,Integer> attributeSums = new HashMap<Integer,Integer>();
		Map<Integer,Integer> entitySums = new HashMap<Integer,Integer>();
		Map<Integer,String> taxonList = new HashMap<Integer,String>();
		final SortedMap<String,Integer> sortedAttributes = new TreeMap<String,Integer>();
		final SortedMap<String,Integer> sortedEntities = new TreeMap<String,Integer>();
		for (Integer att : getUsedAttributes()){
			String attName = u.getNodeName(att);
			if (attName == null){
				attName = att.toString();
			}
			sortedAttributes.put(attName, att);
		}
		for (Integer ent : getUsedEntities()){
			String entName = u.getNodeName(ent);
			if (entName == null){
				entName = ent.toString();
			}			
			sortedEntities.put(entName, ent);
		}
		for(String attName : sortedAttributes.keySet()){
			for (String entName : sortedEntities.keySet()){
				final Integer att = sortedAttributes.get(attName);
				final Integer ent = sortedEntities.get(entName);
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
						String addition = "\n\t" + entName + "\t" + attName;
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
		for(String attName : sortedAttributes.keySet()){
			final Integer att = sortedAttributes.get(attName);
			final int attributeCount =  attributeSums.get(att).intValue();
			attributeTotal += attributeCount;
			u.writeOrDump(attName + "\t" + attributeCount,bw);
		}
		if (repType==VariationType.TAXON){
			u.writeOrDump("Total number of phenotypes (at attribute level) at taxonomically variable nodes (attribute counts should sum to this):\t" + attributeTotal, bw);
		}
		else {
			u.writeOrDump("Total number of phenotypes (at attribute level) for genes (attribute counts should sum to this; will be less than gene phenotype count in KB):\t" + attributeTotal, bw);			
		}
		int entityTotal = 0;
		u.writeOrDump("\n\n-- Entity Summary --",bw);
		for(String entName : sortedEntities.keySet()){
			final Integer ent = sortedEntities.get(entName);
			if (entitySums.containsKey(ent)){
				final int entityCount = entitySums.get(ent).intValue();
				entityTotal += entityCount;
				u.writeOrDump(entName + "\t" + entityCount,bw);
			}
		}
		if (repType==VariationType.TAXON){
			u.writeOrDump("Total number of phenotypes (at attribute level) at taxonomically variable nodes (entity counts should sum to this):\t" + entityTotal, bw);
			u.writeOrDump("\n\n-- Taxon Summary --",bw);
		}
		else{
			u.writeOrDump("Total number of phenotypes (at attribute level) for genes (entity counts should sum to this; will be less than gene phenotype count in KB):\t" + entityTotal, bw);
			u.writeOrDump("\n\n-- Gene Summary --",bw);
		}
		for (Integer taxon : taxonList.keySet()){
			u.writeOrDump(taxonList.get(taxon),bw);
		}
		if (repType==VariationType.TAXON){
			u.writeOrDump("\n Count of taxa with variation:\t" + taxonList.size(),bw);
		}
		else{
			u.writeOrDump("Count of genes with annotations:\t" + taxonList.size(), bw);
		}		
		u.writeOrDump("\n\n Total phenotypes at attribute level:\t" + sum,bw);
	}


	public boolean taxonExhibits(Integer ent, Integer att,Integer taxon) {
		if (hasExhibitorSet(ent, att)){
			if (getExhibitorSet(ent,att).contains(taxon))
				return true;
		}
		return false;
	}

}
