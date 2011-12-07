package phenoscape.queries.lib;

import java.io.Writer;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ProfileScoreSet{
	
	private final Integer taxon;
	private final Integer gene;
	private double maxICScore;
	private double maxIC95;
	private double maxIC99;
	private double iccsScore;
	private double simICScore;
	private double simJScore;
	private double simGOSScore;
	private double simNormGOSScore;
	private Set<PhenotypeExpression> taxonPhenotypes;
	private Set<PhenotypeExpression> genePhenotypes;
	private int decile;

	public ProfileScoreSet(Integer t, Integer g, Set<PhenotypeExpression> set, Set<PhenotypeExpression> set2){
		taxon = t;
		gene = g;
		taxonPhenotypes = set;
		genePhenotypes = set2;
	}
	
	public void setMaxICScore(double score){
		maxICScore = score;
	}
	
	// only used for testing
	public double getMaxICScore(){
		return maxICScore;
	}

	public void setMaxIC95(double score){
		maxIC95 = score;
	}
	
	double getMaxIC95(){
		return maxIC95;
	}

	public void setMaxIC99(double score){
		maxIC99 = score;
	}
	
	double getMaxIC99(){
		return maxIC99;
	}

	public void setDecile(int d){
		decile = d;
	}
	
	public int getDecile(){
		return decile;
	}
	
	public void setICCSScore(double score){
		iccsScore = score;
	}
	
	double getICCSScore(){
		return iccsScore;
	}
	
	public void setSimICScore(double score){
		simICScore = score;
	}
	
	double getSimICScore(){
		return simICScore;
	}
	
	public void setSimJScore(double score){
		simJScore = score;
	}
	
	double getSimJScore(){
		return simJScore;
	}
	
	public void setSimGOSScore(double score){
		simGOSScore = score;
	}

	double getSimGOSScore(){
		return simGOSScore;
	}

	public void setSimNormGOSScore(double score){
		simNormGOSScore = score;
	}

	double getSimNormGOSScore(){
		return simNormGOSScore;
	}

	public void writeScores(Utils u, Writer w){
		StringBuilder scores = new StringBuilder(400);
		scores.append(u.getNodeName(taxon));
		scores.append("\t");
		scores.append(u.getNodeName(gene));
		scores.append("\t");
		scores.append(taxonPhenotypes.size());
		scores.append("\t");
		scores.append(genePhenotypes.size());
		scores.append("\t");
		scores.append(maxICScore);
		scores.append("\t");
		scores.append(maxIC95);
		scores.append("\t");
		scores.append(maxIC99);
		scores.append("\t");
		scores.append(decile);
		scores.append("\t");
		scores.append(iccsScore);
		scores.append("\t");
		scores.append(simJScore);
		scores.append("\t");
		scores.append(simICScore);
		scores.append("\t");
		scores.append(simGOSScore);
		scores.append("\t");
		scores.append(simNormGOSScore);
		u.writeOrDump(scores.toString(),w);
	}
	
	public void writeComplete(Utils u, Writer w) throws SQLException{
		Set<PhenotypeExpression>used = new HashSet<PhenotypeExpression>();
		u.writeOrDump(u.getNodeName(taxon) + "\t" + u.getNodeName(gene) + "\t\t\t" + maxICScore + "\t" + maxIC95 + "\t" + maxIC99 + "\t" + decile + "\t" + iccsScore + "\t" + simJScore + "\t" + simICScore,w);		
		for (PhenotypeExpression tph : taxonPhenotypes){
			if (genePhenotypes.contains(tph))
				u.writeOrDump("\t\t" + tph.getFullName(u) + "\t" + tph.getFullName(u), w);
			else
				u.writeOrDump("\t\t" + tph.getFullName(u),w);
			used.add(tph);
		}
		for (PhenotypeExpression gph : genePhenotypes){
			if (!used.contains(gph)){
				u.writeOrDump("\t\t\t" + gph.getFullName(u), w);
			}
		}
	}

	public boolean isNonZero(){
		if (Double.isInfinite(maxICScore) && (iccsScore == 0.0) && (simICScore == 0.0) && (simJScore == 0.0))
			return false;
		else return true;	
	}
	
	
	
	
	
	
}
