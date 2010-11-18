package phenoscape.queries.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


// Could have a class hierarchy with this and Profile under a common parent, or maybe a common member that does the table operations...

public class VariationTable {
	
	private Map<Integer,Map<Integer,Set<Integer>>> table = new HashMap<Integer,Map<Integer,Set<Integer>>>();  //Attribute, entities, taxa
	
	
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
	
	public void variationReport(Map<Integer,String> UIDCache){
		int sum = 0;
		Map<Integer,Integer> attributeSums = new HashMap<Integer,Integer>();
		Map<Integer,Integer> entitySums = new HashMap<Integer,Integer>();
		for(Integer att : getUsedAttributes()){
			for (Integer ent : getUsedEntities()){
				if (hasTaxonSet(att,ent)){
					sum += getTaxonSet(att,ent).size();
					if (attributeSums.containsKey(att)){
						attributeSums.put(att,attributeSums.get(att).intValue()+getTaxonSet(att,ent).size());
					}
					else
						attributeSums.put(att,getTaxonSet(att,ent).size());
					if (entitySums.containsKey(ent)){
						entitySums.put(ent,entitySums.get(ent).intValue()+getTaxonSet(att,ent).size());
					}
					else
						entitySums.put(ent,getTaxonSet(att,ent).size());
				}
			}
		}
		System.out.println("Summary of detected variation");
		System.out.println("-- Attribute Summary --");
		for(Integer att : attributeSums.keySet()){
			if (UIDCache.containsKey(att)){
				System.out.println(UIDCache.get(att) + "   " + attributeSums.get(att).intValue());
			}
			else
				System.out.println(att.intValue() + "   " + attributeSums.get(att).intValue());
		}
		System.out.println("\n-- Entity Summary --");
		for (Integer ent : entitySums.keySet()){
			if (UIDCache.containsKey(ent)){
				System.out.println(UIDCache.get(ent) + "   " + entitySums.get(ent).intValue());
			}
			else
				System.out.println(ent.intValue() + "   " + entitySums.get(ent).intValue());
			if (ent.intValue() == 7 || ent.intValue() == 9 || ent.intValue() == 11 || ent.intValue() == 12 || ent.intValue() == 13 || ent.intValue() == 14 || ent.intValue() == 15){
				for (Integer att : getUsedAttributes()){
					if (hasTaxonSet(att, ent)){
						System.out.println("Bad entity: " + ent.intValue() + " with attribute: " + att.intValue());
						Set<Integer> badSet = getTaxonSet(att,ent);
						for(Integer badtaxon : badSet){
							System.out.println(" Bad taxon: " + badtaxon.intValue());
						}
					}
				}
			}
		}
		System.out.println("\n\n Total is: " + sum);
	}

}
