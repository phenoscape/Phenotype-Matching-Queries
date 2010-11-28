package phenoscape.queries.lib;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



// Could have a class hierarchy with this and Profile under a common parent, or maybe a common member that does the table operations...

public class VariationTable {
	
	private Map<Integer,Map<Integer,Set<Integer>>> table = new HashMap<Integer,Map<Integer,Set<Integer>>>();  //Attributes, entities, taxa
	
	
	public void addTaxon(Integer attribute_node_id, Integer entity_node_id, Integer taxon_node_id){
		if (table.containsKey(attribute_node_id)){
			Map<Integer,Set<Integer>> attribute_entry = table.get(attribute_node_id);
			if (attribute_entry.containsKey(entity_node_id)){
				Set<Integer> taxonSet = attribute_entry.get(entity_node_id);
				taxonSet.add(taxon_node_id);
			}
			else {
				Set<Integer> taxonSet = new HashSet<Integer>();
				taxonSet.add(taxon_node_id);
				attribute_entry.put(entity_node_id,taxonSet);
			}
		}
		else {
			Map<Integer,Set<Integer>> attribute_entry = new HashMap<Integer,Set<Integer>>();
			Set<Integer> taxonSet = new HashSet<Integer>();
			taxonSet.add(taxon_node_id);
			attribute_entry.put(entity_node_id,taxonSet);
			table.put(attribute_node_id, attribute_entry);
		}
	}
	
	
	public boolean hasTaxonSet(Integer attribute, Integer entity){
		if (table.containsKey(attribute))
			return (table.get(attribute).containsKey(entity));
		else
			return false;
	}
	
	
	public Set<Integer> getTaxonSet (Integer attribute, Integer entity){
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
	
	public void variationReport(Utils u, Map<Integer,String> UIDCache, BufferedWriter bw){
		int sum = 0;
		Map<Integer,Integer> attributeSums = new HashMap<Integer,Integer>();
		Map<Integer,Integer> entitySums = new HashMap<Integer,Integer>();
		Map<Integer,String> taxonList = new HashMap<Integer,String>();
		for(Integer att : getUsedAttributes()){
			for (Integer ent : getUsedEntities()){
				if (hasTaxonSet(att,ent)){
					Set <Integer> taxa = getTaxonSet(att,ent);
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
						if (taxonList.containsKey(taxon)){
							taxonList.put(taxon, taxonList.get(taxon)+ "\n\t" + u.doSubstitutions(UIDCache.get(ent)) + "\t" + u.lookupIDToName(UIDCache.get(att)));
						}
						else{
							taxonList.put(taxon, "\n" + u.lookupIDToName(UIDCache.get(taxon)) + "\n\t" + u.doSubstitutions(UIDCache.get(ent)) + "\t" + u.lookupIDToName(UIDCache.get(att)));
						}
					}
				}
			}
		}
		u.writeOrDump("Summary of detected variation",bw);
		u.writeOrDump("-- Attribute Summary --",bw);
		for(Integer att : attributeSums.keySet()){
			if (UIDCache.containsKey(att)){
				u.writeOrDump(u.lookupIDToName(UIDCache.get(att)) + "\t" + attributeSums.get(att).intValue(),bw);
			}
			else
				u.writeOrDump(att.intValue() + "   " + attributeSums.get(att).intValue(),bw);
		}
		u.writeOrDump("\n-- Entity Summary --",bw);
		for (Integer ent : entitySums.keySet()){
			if (UIDCache.containsKey(ent)){
				u.writeOrDump(u.doSubstitutions(UIDCache.get(ent)) + "\t" + entitySums.get(ent).intValue(),bw);
			}
			else
				u.writeOrDump(ent.intValue() + "   " + entitySums.get(ent).intValue(),bw);
		}
		u.writeOrDump("\n-- Taxon Summary --",bw);
		for (Integer taxon : taxonList.keySet()){
			u.writeOrDump(taxonList.get(taxon),bw);
		}
		u.writeOrDump("\n Taxon count is:\t" + taxonList.size(),bw);
		u.writeOrDump("\n\n Total is:\t" + sum,bw);
	}

}
