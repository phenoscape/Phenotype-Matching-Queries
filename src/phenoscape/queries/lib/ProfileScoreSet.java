package phenoscape.queries.lib;

import java.io.Writer;
import java.util.Set;

public class ProfileScoreSet{
	
	private final Integer taxon;
	private final Integer gene;
	private double medianICScore;
	private double medianPV;
	private double medianTiesPV;
	private double meanICScore;
	private double meanPV;
	private double meanTiesPV;
	private Set<PhenotypeExpression> taxonPhenotypes;
	private Set<PhenotypeExpression> genePhenotypes;

	public ProfileScoreSet(Integer t, Integer g, Set<PhenotypeExpression> set, Set<PhenotypeExpression> set2){
		taxon = t;
		gene = g;
		taxonPhenotypes = set;
		genePhenotypes = set2;
	}
	
	public void setMedianICScore(double score){
		medianICScore = score;
	}
	
	// only used for testing
	public double getMedianICScore(){
		return medianICScore;
	}

	
	public void setMedianPV(double score){
		medianPV = score;
	}

	public double getMedianPV(){
		return medianPV;
	}

	
	public void setMedianTiesPV(double score){
		medianTiesPV = score;
	}
	
	public double getMedianTiesPV(){
		return medianTiesPV;
	}
	
	

	// only used for testing
	public double getMeanICScore(){
		return meanICScore;
	}

	public void setMeanICScore(double score){
		meanICScore = score;
	}

	
	public void setMeanPV(double score){
		meanPV = score;
	}
	
	public void setMeanTiesPV(double score){
		meanTiesPV = score;
	}
	
	
	final static String REPORT_HEADER = 
		"Taxon \t Gene \t taxon phenotypes \t gene phenotypes \t median IC \t pv \t pv(ties) \t meanIC \t pv \t pv(ties) ";
	
	public static void writeHeader(Utils u, Writer w){
		u.writeOrDump(REPORT_HEADER,w);		
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
		scores.append(medianPV);
		scores.append("\t");
		scores.append(medianTiesPV);
		scores.append("\t");
		scores.append(meanICScore);
		scores.append("\t");		
		scores.append(meanPV);
		scores.append("\t");
		scores.append(meanTiesPV);
		scores.append("\t");
		
		u.writeOrDump(scores.toString(),w);
	}
	

	public boolean isNonZero(){
		if (Double.isInfinite(medianICScore) && (meanICScore == 0.0))
			return false;
		else return true;	
	}
	
	
	
	
	
	
}
