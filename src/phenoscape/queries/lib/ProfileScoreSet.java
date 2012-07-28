package phenoscape.queries.lib;

import java.io.Writer;
import java.util.Set;

public class ProfileScoreSet{
	
	private final Integer taxon;
	private final Integer gene;
	private double medianICScore;
	private double cutOff95medianIC;
	private double cutOff99medianIC;
	private double meanICScore;
	private double cutOff95meanIC;
	private double cutOff99meanIC;
	private double iccsScore;
	private Set<PhenotypeExpression> taxonPhenotypes;
	private Set<PhenotypeExpression> genePhenotypes;
	private int decileMedianIC;
	private int decileMeanIC;
	private double hyperSSScore;

	public ProfileScoreSet(Integer t, Integer g, Set<PhenotypeExpression> set, Set<PhenotypeExpression> set2){
		taxon = t;
		gene = g;
		taxonPhenotypes = set;
		genePhenotypes = set2;
	}
	
	public void setMedianICScore(double score){
		medianICScore = score;
	}
	
	public void setMeanICScore(double score){
		meanICScore = score;
	}
	
	// only used for testing
	public double getMedianICScore(){
		return medianICScore;
	}

	// only used for testing
	public double getMeanICScore(){
		return meanICScore;
	}

	
	public void setcutOff95medianIC(double score){
		cutOff95medianIC = score;
	}
	
	double getcutOff95medianIC(){
		return cutOff95medianIC;
	}

	public void setcutOff99medianIC(double score){
		cutOff99medianIC = score;
	}
	
	double getcutOff99medianIC(){
		return cutOff99medianIC;
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

	public void setDecileMedianIC(int d){
		decileMedianIC = d;
	}
	
	public int getDecileMedianIC(){
		return decileMedianIC;
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
		scores.append(medianICScore);
		scores.append("\t");		
		scores.append(cutOff95medianIC);
		scores.append("\t");
		scores.append(cutOff95medianIC);
		scores.append("\t");
		scores.append(decileMedianIC);
		scores.append("\t");
		scores.append(meanICScore);
		scores.append("\t");		
		scores.append(cutOff95meanIC);
		scores.append("\t");
		scores.append(cutOff95meanIC);
		scores.append("\t");
		scores.append(decileMeanIC);
		scores.append("\t");
		
		u.writeOrDump(scores.toString(),w);
	}
	

	public boolean isNonZero(){
		if (Double.isInfinite(medianICScore) && (meanICScore == 0.0))
			return false;
		else return true;	
	}
	
	
	
	
	
	
}
