package phenoscape.queries.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Profile {
	
	private Map<Integer,Map<Integer,Set<Integer>>> table = new HashMap<Integer,Map<Integer,Set<Integer>>>();  //Attributes, entities, phenotypes
	
	
	public void addPhenotype(Integer attribute_node_id, Integer entity_node_id, Integer phenotype_node_id){
		if (table.containsKey(attribute_node_id)){
			Map<Integer,Set<Integer>> attribute_entry = table.get(attribute_node_id);
			if (attribute_entry.containsKey(entity_node_id)){
				Set<Integer> phenotypeSet = attribute_entry.get(entity_node_id);
				phenotypeSet.add(phenotype_node_id);
			}
			else {
				Set<Integer> phenotypeSet = new HashSet<Integer>();
				phenotypeSet.add(phenotype_node_id);
				attribute_entry.put(entity_node_id,phenotypeSet);
			}
		}
		else {
			Map<Integer,Set<Integer>> attribute_entry = new HashMap<Integer,Set<Integer>>();
			Set<Integer> phenotypeSet = new HashSet<Integer>();
			phenotypeSet.add(phenotype_node_id);
			attribute_entry.put(entity_node_id,phenotypeSet);
			table.put(attribute_node_id, attribute_entry);
		}
	}
	
	public void addAlltoPhenotypeSet(Integer attribute_node_id, Integer entity_node_id,Set<Integer> toAdd){
		if (table.containsKey(attribute_node_id)){
			Map<Integer,Set<Integer>> attribute_entry = table.get(attribute_node_id);
			if (attribute_entry.containsKey(attribute_node_id)){
				Set<Integer> phenotypeSet = attribute_entry.get(entity_node_id);
				phenotypeSet.addAll(toAdd);
			}
			else {
				Set<Integer> phenotypeSet = new HashSet<Integer>();
				phenotypeSet.addAll(toAdd);
				attribute_entry.put(entity_node_id,phenotypeSet);
			}
		}
		else {
			Map<Integer,Set<Integer>> attribute_entry = new HashMap<Integer,Set<Integer>>();
			Set<Integer> phenotypeSet = new HashSet<Integer>();
			phenotypeSet.addAll(toAdd);
			attribute_entry.put(entity_node_id,phenotypeSet);
			table.put(attribute_node_id, attribute_entry);
		}
	}
	
	public boolean isEmpty(){
		return table.isEmpty();
	}
	
	public String summary(){
		StringBuilder b = new StringBuilder(200);
		b.append("Profile has " + getUsedAttributes().size() + " attributes by " + getUsedEntities().size() + " entities");
		int count = 0;
		int fillCount = 0;
		int entries = 0;
		for(Integer att : getUsedAttributes()){
			for(Integer ent : getUsedEntities()){
				if (hasPhenotypeSet(att,ent)){
					count++;
					if (getPhenotypeSet(att,ent).size()>0){
						fillCount++;
						entries += getPhenotypeSet(att,ent).size();
					}
				}
			}
		}
		b.append("Profile has " + getUsedAttributes().size() + " attributes by " + getUsedEntities().size() + " entities" + " with " + count + " cells and " + fillCount + " filled cells with " + entries + " phenotypes in them");
		return b.toString();
	}
	
	public boolean hasPhenotypeSet(Integer attribute, Integer entity){
		if (table.containsKey(attribute))
			return (table.get(attribute).containsKey(entity));
		else
			return false;
	}
	public Set<Integer> getPhenotypeSet (Integer attribute, Integer entity){
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
	
	public Set<Integer> getAllPhenotypes(){
		Set<Integer> result = new HashSet<Integer>();
		for (Integer curAtt : getUsedAttributes()){ 
			Map<Integer,Set<Integer>> attValue = table.get(curAtt);
			for (Set<Integer> entValue : attValue.values()){
				result.addAll(entValue);
			}
		}
		return result;
	}

}
