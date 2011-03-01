package phenoscape.queries.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Profile {
	
	private Map<Integer,Map<Integer,Set<Integer>>> table = new HashMap<Integer,Map<Integer,Set<Integer>>>();  //entities, attributes phenotypes
	
	
	public void addPhenotype(Integer entity_node_id, Integer attribute_node_id, Integer phenotype_node_id){
		if (table.containsKey(entity_node_id)){
			Map<Integer,Set<Integer>> entity_entry = table.get(entity_node_id);
			if (entity_entry.containsKey(attribute_node_id)){
				Set<Integer> phenotypeSet = entity_entry.get(attribute_node_id);
				phenotypeSet.add(phenotype_node_id);
			}
			else {
				Set<Integer> phenotypeSet = new HashSet<Integer>();
				phenotypeSet.add(phenotype_node_id);
				entity_entry.put(attribute_node_id,phenotypeSet);
			}
		}
		else {
			Map<Integer,Set<Integer>> entity_entry = new HashMap<Integer,Set<Integer>>();
			Set<Integer> phenotypeSet = new HashSet<Integer>();
			phenotypeSet.add(phenotype_node_id);
			entity_entry.put(attribute_node_id,phenotypeSet);
			table.put(entity_node_id, entity_entry);
		}
	}
	
	public void addAlltoPhenotypeSet(Integer entity_node_id, Integer attribute_node_id,Set<Integer> toAdd){
		if (table.containsKey(entity_node_id)){
			Map<Integer,Set<Integer>> entity_entry = table.get(entity_node_id);
			if (entity_entry.containsKey(attribute_node_id)){
				Set<Integer> phenotypeSet = entity_entry.get(attribute_node_id);
				phenotypeSet.addAll(toAdd);
			}
			else {
				Set<Integer> phenotypeSet = new HashSet<Integer>();
				phenotypeSet.addAll(toAdd);
				entity_entry.put(attribute_node_id,phenotypeSet);
			}
		}
		else {
			Map<Integer,Set<Integer>> entity_entry = new HashMap<Integer,Set<Integer>>();
			Set<Integer> phenotypeSet = new HashSet<Integer>();
			phenotypeSet.addAll(toAdd);
			entity_entry.put(attribute_node_id,phenotypeSet);
			table.put(entity_node_id, entity_entry);
		}
	}

	public void retainAllFromPhenotypeSet(Integer entity_node_id, Integer attribute_node_id,Set<Integer> toRetain){
		if (table.containsKey(entity_node_id)){
			Map<Integer,Set<Integer>> entity_entry = table.get(entity_node_id);
			if (entity_entry.containsKey(attribute_node_id)){
				Set<Integer> phenotypeSet = entity_entry.get(attribute_node_id);
				phenotypeSet.retainAll(toRetain);
			}
			else {
				Set<Integer> phenotypeSet = new HashSet<Integer>();
				phenotypeSet.addAll(toRetain);   //add here because new set
				entity_entry.put(attribute_node_id,phenotypeSet);
			}
		}
		else {
			Map<Integer,Set<Integer>> entity_entry = new HashMap<Integer,Set<Integer>>();
			Set<Integer> phenotypeSet = new HashSet<Integer>();
			phenotypeSet.addAll(toRetain);  //add here because new set
			entity_entry.put(attribute_node_id,phenotypeSet);
			table.put(entity_node_id, entity_entry);
		}
	}
	

	
	public boolean isEmpty(){
		return table.isEmpty();
	}
	
	public String summary(){
		StringBuilder b = new StringBuilder(200);
		int count = 0;
		int fillCount = 0;
		int entries = 0;
		for(Integer ent : getUsedEntities()){
			for(Integer att : getUsedAttributes()){
				if (hasPhenotypeSet(ent,att)){
					count++;
					if (getPhenotypeSet(ent,att).size()>0){
						fillCount++;
						entries += getPhenotypeSet(ent,att).size();
					}
				}
			}
		}
		b.append("Profile has " + getUsedAttributes().size() + " attributes by " + getUsedEntities().size() + " entities" + " with " + count + " cells and " + fillCount + " filled cells with " + entries + " phenotypes in them");
		return b.toString();
	}
	
	public boolean hasPhenotypeSet(Integer entity, Integer attribute){
		if (table.containsKey(entity))
			return (table.get(entity).containsKey(attribute));
		else
			return false;
	}
	
	public Set<Integer> getPhenotypeSet (Integer entity, Integer attribute){
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

	public boolean usesAttribute(Integer att){
		for (Map<Integer,Set<Integer>> entity_value : table.values()){
			if (entity_value.containsKey(att))
				return true;
		}
		return false;
	}

	
	public Set<Integer> getAllPhenotypes(){
		Set<Integer> result = new HashSet<Integer>();
		for (Integer curEnt : getUsedEntities()){ 
			Map<Integer,Set<Integer>> entValue = table.get(curEnt);
			for (Set<Integer> attValue : entValue.values()){
				result.addAll(attValue);
			}
		}
		return result;
	}

	public void clearPhenotypeSet(Integer entity, Integer att) {
		if (table.containsKey(entity)){
			Map<Integer,Set<Integer>> entityValue = table.get(entity);
			if (entityValue.containsKey(att)){
				entityValue.remove(att);
			}
			if (entityValue.isEmpty()){
				table.remove(entity);
			}
		}
	}
	
	public void removeAllEmpties(){
		for (Integer ent : table.keySet()){
			Map<Integer,Set<Integer>> entityValue = table.get(ent);
			for (Integer att : entityValue.keySet()){
				Set<Integer> attSet = entityValue.get(att);
				if (attSet.isEmpty())
					entityValue.remove(ent);
			}
		}
	}

	public void setPhenotypeSet(Integer ent, Integer att, Set<Integer> newSet) {
		if (table.containsKey(ent)){
			Map<Integer,Set<Integer>> entity_entry = table.get(ent);
			if (entity_entry.containsKey(att)){
				Set<Integer> phenotypeSet = entity_entry.get(att);
				phenotypeSet.clear();
				phenotypeSet.addAll(newSet);
			}
			else {
				Set<Integer> phenotypeSet = new HashSet<Integer>();
				phenotypeSet.addAll(newSet);
				entity_entry.put(att,phenotypeSet);
			}
		}
		else {
			Map<Integer,Set<Integer>> entity_entry = new HashMap<Integer,Set<Integer>>();
			Set<Integer> phenotypeSet = new HashSet<Integer>();
			phenotypeSet.addAll(newSet);
			entity_entry.put(att,phenotypeSet);
			table.put(ent, entity_entry);
		}
	}



}
