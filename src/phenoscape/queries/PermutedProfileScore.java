package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class PermutedProfileScore {
	public int taxonSize;
	public int geneSize;
	public double[] dist;
	public double cutoff010;
	public double cutoff020;
	public double cutoff030;
	public double cutoff040;
	public double cutoff050;
	public double cutoff060;
	public double cutoff070;
	public double cutoff080;
	public double cutoff090;
	public double cutoff095;
	public double cutoff099;

	PermutedProfileScore(double[]distribution, int taxaCount,int geneCount){
		taxonSize = taxaCount;
		geneSize = geneCount;
		dist=distribution;
		Arrays.sort(dist);  // this puts values in ascending order, opposite of what we what with HyperSS
		final int distsize = dist.length;
		cutoff010 = dist[(int)(0.9*distsize)];
		cutoff020 = dist[(int)(0.8*distsize)];
		cutoff030 = dist[(int)(0.7*distsize)];
		cutoff040 = dist[(int)(0.6*distsize)];
		cutoff050 = dist[(int)(0.5*distsize)];
		cutoff060 = dist[(int)(0.4*distsize)];
		cutoff070 = dist[(int)(0.3*distsize)];
		cutoff080 = dist[(int)(0.2*distsize)];
		cutoff090 = dist[(int)(0.1*distsize)];
		cutoff095 = dist[(int)(0.05*distsize)];
		cutoff099 = dist[(int)(0.01*distsize)];
	}
	
	boolean matchSize(int tSize, int gSize){
		return (taxonSize == tSize && geneSize == gSize);
	}

	private final static String lineSeparator = System.getProperty("line.separator");
	public void writeDist(String folder) throws IOException {
		String reportFileName = folder + "/dist" + Integer.toString(taxonSize) + "_" + Integer.toString(geneSize);
		BufferedWriter distWriter = new BufferedWriter(new FileWriter(new File(reportFileName)));
		for(double d : dist){
			distWriter.append(Double.toString(d));
			distWriter.append(lineSeparator);
		}
		distWriter.close();
	}
}