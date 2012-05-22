package phenoscape.queries.lib;

import java.io.Writer;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ProfileScoreSet{
	
	private final Integer taxon;
	private final Integer gene;
	private double maxICScore;
	private double cutOff95maxIC;
	private double cutOff99maxIC;
	private double meanICScore;
	private double cutOff95meanIC;
	private double cutOff99meanIC;
	private double iccsScore;
	private double simICScore;
	private double simJScore;
	private double simGOSScore;
	private double simNormGOSScore;
	private Set<PhenotypeExpression> taxonPhenotypes;
	private Set<PhenotypeExpression> genePhenotypes;
	private int decileMaxIC;
	private int decileMeanIC;
	private double hyperSSScore;

	public ProfileScoreSet(Integer t, Integer g, Set<PhenotypeExpression> set, Set<PhenotypeExpression> set2){
		taxon = t;
		gene = g;
		taxonPhenotypes = set;
		genePhenotypes = set2;
	}
	
	public void setMaxICScore(double score){
		maxICScore = score;
	}
	
	public void setMeanICScore(double score){
		meanICScore = score;
	}
	
	// only used for testing
	public double getMaxICScore(){
		return maxICScore;
	}

	// only used for testing
	public double getMeanICScore(){
		return meanICScore;
	}

	
	public void setcutOff95maxIC(double score){
		cutOff95maxIC = score;
	}
	
	double getcutOff95maxIC(){
		return cutOff95maxIC;
	}

	public void setcutOff99maxIC(double score){
		cutOff99maxIC = score;
	}
	
	double getcutOff99maxIC(){
		return cutOff99maxIC;
	}

	public void setcutOff95meanIC(double score){
		cutOff95meanIC = score;
	}
	
	double getcutOff95meanIC(){
		return cutOff95meanIC;
	}

	public void setcutOff99meanIC(double score){
		cutOff99meanIC = score;
	}
	
	double getcutOff99meanIC(){
		return cutOff99meanIC;
	}

	public void setDecileMaxIC(int d){
		decileMaxIC = d;
	}
	
	public int getDecileMaxIC(){
		return decileMaxIC;
	}

	public void setDecileMeanIC(int d){
		decileMeanIC = d;
	}
	
	public int getDecileMeanIC(){
		return decileMeanIC;
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
	
	public void setHyperSSScore(double score){
		hyperSSScore = score;
	}
	
	public double getHyperSSScore(){
		return hyperSSScore;
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
		scores.append(cutOff95maxIC);
		scores.append("\t");
		scores.append(cutOff95maxIC);
		scores.append("\t");
		scores.append(decileMaxIC);
		scores.append("\t");
		scores.append(meanICScore);
		scores.append("\t");		
		scores.append(cutOff95meanIC);
		scores.append("\t");
		scores.append(cutOff95meanIC);
		scores.append("\t");
		scores.append(decileMeanIC);
		scores.append("\t");
		
//		scores.append(maxICScore);
//		scores.append("\t");
//		scores.append(iccsScore);
//		scores.append("\t");
//		scores.append(simJScore);
//		scores.append("\t");
//		scores.append(simICScore);
//		scores.append("\t");
//		scores.append(simGOSScore);
//		scores.append("\t");
//		scores.append(simNormGOSScore);
		u.writeOrDump(scores.toString(),w);
	}
	
//	public void writeComplete(Utils u, Writer w) throws SQLException{
//		Set<PhenotypeExpression>used = new HashSet<PhenotypeExpression>();
//		u.writeOrDump(u.getNodeName(taxon) + "\t" + u.getNodeName(gene) + "\t\t\t" + hyperSSScore + "\t" + cutOff95 + "\t" + cutOff99 + "\t" + decile + "\t" + maxICScore + "\t" + iccsScore + "\t" + simJScore + "\t" + simICScore,w);		
//		for (PhenotypeExpression tph : taxonPhenotypes){
//			if (genePhenotypes.contains(tph))
//				u.writeOrDump("\t\t" + tph.getFullName(u) + "\t" + tph.getFullName(u), w);
//			else
//				u.writeOrDump("\t\t" + tph.getFullName(u),w);
//			used.add(tph);
//		}
//		for (PhenotypeExpression gph : genePhenotypes){
//			if (!used.contains(gph)){
//				u.writeOrDump("\t\t\t" + gph.getFullName(u), w);
//			}
//		}
//	}

	public boolean isNonZero(){
		if (Double.isInfinite(maxICScore) && (iccsScore == 0.0) && (simICScore == 0.0) && (simJScore == 0.0))
			return false;
		else return true;	
	}
	
	
	
	
	
	
}
