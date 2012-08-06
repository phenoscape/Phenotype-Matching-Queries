package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class PermutedProfileScore {

	public static enum ScoreType {MEDIANIC,MEANIC};	

	
	public int taxonSize;
	public int geneSize;
	public double[] medianICdist;
	public double[] meanICdist;

	public PermutedProfileScore(double[] mediandistribution, double[] meandistribution, int taxaCount,int geneCount){
		taxonSize = taxaCount;
		geneSize = geneCount;
		medianICdist=mediandistribution;
		meanICdist=meandistribution;
		Arrays.sort(mediandistribution);  
		Arrays.sort(meandistribution);
	}
	
	public boolean matchSize(int tSize, int gSize){
		return (taxonSize == tSize && geneSize == gSize);
	}

	

	private final static String lineSeparator = System.getProperty("line.separator");
	public void writeDist(String folder, ScoreType tp) throws IOException {
		final String reportFileName = folder + "/dist" + Integer.toString(taxonSize) + "_" + Integer.toString(geneSize);
		final BufferedWriter distWriter = new BufferedWriter(new FileWriter(new File(reportFileName)));
		double dist[];
		if (ScoreType.MEDIANIC == tp)
			dist = medianICdist;
		else
			dist = meanICdist;
		for(double d : dist){
			distWriter.append(Double.toString(d));
			distWriter.append(lineSeparator);
		}
		distWriter.close();
	}
	
	// return a p-value

	double[] get_pvalues(double s, ScoreType tp ){
		double[] dist;
		double result[] = new double[2];
		if (ScoreType.MEDIANIC == tp){
			dist = medianICdist;
		}
		else{
			dist = meanICdist;
		}
		int firstMatch = -1;
		int lastMatch = -1;
		for(int i=0;i<dist.length;i++){
			if (s == dist[i] && firstMatch == -1){
				firstMatch = i;
			}
			if (s < dist[i]){
				if (firstMatch == -1){
					firstMatch = i;
				}
				if (lastMatch == -1)
					lastMatch = i;
			}
		}
		result[0] = (double)firstMatch/(double)dist.length;
		result[1] = (double)lastMatch/(double)dist.length;
		return result;
	}
	

}