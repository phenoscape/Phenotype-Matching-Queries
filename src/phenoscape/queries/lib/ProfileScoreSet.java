package phenoscape.queries.lib;

import java.io.BufferedWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ProfileScoreSet implements Comparable {
	
	private final Integer taxon;
	private final Integer gene;
	private double maxICScore;
	private double maxIC95;
	private double maxIC99;
	private double iccsScore;
	private double simICScore;
	private double simJScore;
	private Set<PhenotypeExpression> taxonPhenotypes;
	private Set<PhenotypeExpression> genePhenotypes;

	public ProfileScoreSet(Integer t, Integer g, Set<PhenotypeExpression> set, Set<PhenotypeExpression> set2){
		taxon = t;
		gene = g;
		taxonPhenotypes = set;
		genePhenotypes = set2;
	}
	
	public void setMaxICScore(double score){
		maxICScore = score;
	}

	public void setMaxIC95(double score){
		maxIC95 = score;
	}

	public void setMaxIC99(double score){
		maxIC99 = score;
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
	
	public void writeScores(Utils u, Writer w){
		u.writeOrDump(u.getNodeName(taxon) + "\t" + u.getNodeName(gene) + "\t" + taxonPhenotypes.size() + "\t" + genePhenotypes.size() + "\t" + maxICScore + "\t" + maxIC95 + "\t" + maxIC99 + "\t" +iccsScore,w);		
	}
	
	public void writeComplete(Utils u, Writer w) throws SQLException{
		Set<PhenotypeExpression>used = new HashSet<PhenotypeExpression>();
		u.writeOrDump(u.getNodeName(taxon) + "\t" + u.getNodeName(gene) + "\t\t\t" + maxICScore + "\t" + maxIC95 + "\t" + maxIC99 + "\t" + iccsScore,w);		
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
	
	
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	
}
