package phenoscape.queries.lib;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProfileMap {
	
	private final Map<Integer,Profile>contents = new HashMap<Integer,Profile>();

	
	public Set<Integer>domainSet(){
		return contents.keySet();
	}
	
	public Collection<Profile>range(){
		return contents.values();
	}
	
	public int domainSize(){
		return contents.keySet().size();
	}
	
	public Profile getProfile(Integer i){
		return contents.get(i);
	}
	
	public boolean isEmpty() {
		return contents.isEmpty();
	}

	public void addProfile(Integer id, Profile profile) {
		contents.put(id, profile);
	}
	
	public boolean nonEmptyProfile(Integer id){
		return (contents.containsKey(id) && !contents.get(id).isEmpty());
	}
	
	public boolean hasEmptyProfile(Integer id){
		return (contents.containsKey(id) && contents.get(id).isEmpty());
	}
	
	public void removeEmptyProfiles(){
		Set<Integer> domainCopy = new HashSet<Integer>();
		domainCopy.addAll(domainSet());
		for (Integer member : domainCopy){
			if (hasEmptyProfile(member))
				contents.remove(member);
		}
	}

	public void addPhenotype(int id,int entityID,int attributeID,Integer phenotypeID){
		if (contents.containsKey(id)){
			contents.get(id).addPhenotype(entityID,attributeID, phenotypeID);						
		}
		else {
			contents.put(id, new Profile());
			contents.get(id).addPhenotype(entityID,attributeID, phenotypeID);	
		}

	}
	
}
