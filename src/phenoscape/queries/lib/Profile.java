package phenoscape.queries.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Profile {
	
	private Map<Integer,Map<Integer,Set<Integer>>> eqtable = new HashMap<Integer,Map<Integer,Set<Integer>>>();  //entities, attributes phenotypes
	//private Map<Integer,Map<Integer,Set<PhenotypeExpression>>> eqtable = new HashMap<Integer,Map<Integer,Set<PhenotypeExpression>>>();  //entities, attributes phenotypes
	private Set<PhenotypeExpression> eaSet = null;
	private Set<PhenotypeExpression> unionSet = null;

	
	//The next five methods can alter the eqtable; 
	public void addPhenotype(Integer entity_node_id, Integer attribute_node_id, Integer phenotype_node_id){
		if (eqtable.containsKey(entity_node_id)){
			Map<Integer,Set<Integer>> entity_entry = eqtable.get(entity_node_id);
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
			eqtable.put(entity_node_id, entity_entry);
		}
		eaSet = null;  //delete because it's invalid
	}
	
	public void addAlltoPhenotypeSet(Integer entity_node_id, Integer attribute_node_id,Set<Integer> toAdd){
		if (eqtable.containsKey(entity_node_id)){
			Map<Integer,Set<Integer>> entity_entry = eqtable.get(entity_node_id);
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
			eqtable.put(entity_node_id, entity_entry);
		}
		eaSet = null;  //delete because it's invalid
	}

	
	public void clearPhenotypeSet(Integer entity, Integer att) {
		if (eqtable.containsKey(entity)){
			Map<Integer,Set<Integer>> entityValue = eqtable.get(entity);
			if (entityValue.containsKey(att)){
				entityValue.remove(att);
			}
			if (entityValue.isEmpty()){
				eqtable.remove(entity);
			}
		}
		eaSet = null;   //
		unionSet = null;  //
	}
	
	public void removeAllEmpties(){
		for (Integer ent : eqtable.keySet()){
			Map<Integer,Set<Integer>> entityValue = eqtable.get(ent);
			for (Integer att : entityValue.keySet()){
				Set<Integer> attSet = entityValue.get(att);
				if (attSet.isEmpty())
					entityValue.remove(ent);
			}
		}
		eaSet = null;  //This may not be strictly necessary
	}

	public void setPhenotypeSet(Integer ent, Integer att, Set<Integer> newSet) {
		if (eqtable.containsKey(ent)){
			Map<Integer,Set<Integer>> entity_entry = eqtable.get(ent);
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
			eqtable.put(ent, entity_entry);
		}
		eaSet = null;  //delete because it's invalid
	}



	
	public boolean isEmpty(){
		return eqtable.isEmpty();
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
		if (eqtable.containsKey(entity))
			return (eqtable.get(entity).containsKey(attribute));
		else
			return false;
	}
	
	public Set<Integer> getPhenotypeSet (Integer entity, Integer attribute){
		return eqtable.get(entity).get(attribute);
	}
	
	public Set<Integer> getUsedEntities(){
		return eqtable.keySet();
	}
	
	public Set<Integer> getUsedAttributes(){
		Set<Integer>result = new HashSet<Integer>();
		for (Map<Integer,Set<Integer>> entity_value : eqtable.values()){
			result.addAll(entity_value.keySet());
		}
		return result;
	}

	public boolean usesAttribute(Integer att){
		for (Map<Integer,Set<Integer>> entity_value : eqtable.values()){
			if (entity_value.containsKey(att))
				return true;
		}
		return false;
	}

	
	public Set<Integer> getAllEQPhenotypes(){
		Set<Integer> result = new HashSet<Integer>();
		for (Integer curEnt : getUsedEntities()){ 
			Map<Integer,Set<Integer>> entValue = eqtable.get(curEnt);
			for (Set<Integer> attValue : entValue.values()){
				result.addAll(attValue);
			}
		}
		return result;
	}
	
	//effectively converting EQ phenotypes into EAphenotypes
	public Set<PhenotypeExpression> getAllEAPhenotypes(){
		if (eaSet == null){
			eaSet = new HashSet<PhenotypeExpression>();
			for (Integer curEnt : getUsedEntities()){ 
				Map<Integer,Set<Integer>> entValue = eqtable.get(curEnt);
				for (Integer att : entValue.keySet()){
					eaSet.add(new PhenotypeExpression(curEnt,att));
				}
			}
		}
		return eaSet;
	}
	
	// Not sure it makes sense to actually compute the union set here
	public void setUnionSet(Set<PhenotypeExpression> union){
		unionSet=union;
	}
	
	public Set<PhenotypeExpression> getUnionSet(){
		return unionSet;
	}



}
