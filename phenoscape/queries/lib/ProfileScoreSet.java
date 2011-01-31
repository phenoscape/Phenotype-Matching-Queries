package phenoscape.queries.lib;

import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.Set;

public class ProfileScoreSet implements Comparable {
	
	private final Integer taxon;
	private final Integer gene;
	private double maxICScore;
	private double iccsScore;
	private double simICScore;
	private double simJScore;
	private Set<Integer> taxonPhenotypes;
	private Set<Integer> genePhenotypes;

	public ProfileScoreSet(Integer t, Integer g, Set<Integer> tp, Set<Integer> gp){
		taxon = t;
		gene = g;
		taxonPhenotypes = tp;
		genePhenotypes = gp;
	}
	
	public void setMaxICScore(double score){
		maxICScore = score;
	}
	
	public void setICCSScore(double score){
		iccsScore = score;
	}
	
	public void setSimICScore(double score){
		simICScore = score;
	}
	
	public void setSimJScore(double score){
		simJScore = score;
	}
	
	public void writeScores(Utils u, BufferedWriter b){
		u.writeOrDump(u.getNodeName(taxon) + "\t" + u.getNodeName(gene) + "\t" + taxonPhenotypes.size() + "\t" + genePhenotypes.size() + "\t" + maxICScore + "\t" + iccsScore + "\t" + simICScore + "\t" + simJScore,b);		
	}
	
	public void writeComplete(Utils u, BufferedWriter b){
		Set<Integer>used = new HashSet<Integer>();
		u.writeOrDump(u.getNodeName(taxon) + "\t" + u.getNodeName(gene) + "\t\t\t" + maxICScore + "\t" + iccsScore + "\t" + simICScore + "\t" + simJScore,b);		
		for (Integer tph : taxonPhenotypes){
			if (genePhenotypes.contains(tph))
				u.writeOrDump("\t\t" + u.getNodeName(tph) + "\t" + u.getNodeName(tph), b);
			else
				u.writeOrDump("\t\t" + u.getNodeName(tph),b);
			used.add(tph);
		}
		for (Integer gph : genePhenotypes){
			if (!used.contains(gph)){
				u.writeOrDump("\t\t\t" + u.getNodeName(gph), b);
			}
		}
	}

	public boolean isNonZero(){
		if (Double.isInfinite(maxICScore) && (iccsScore == 0.0) && (simICScore == 0.0) && (simJScore == 0.0))
			return false;
		else return true;	
	}
	
	
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	
}
