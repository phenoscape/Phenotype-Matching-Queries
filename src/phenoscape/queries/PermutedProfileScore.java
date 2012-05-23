package phenoscape.queries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class PermutedProfileScore {

	public static enum ScoreType {MAXIC,MEANIC};

	enum CutoffIndex {level010,level020,level030,level040,level050,level060,level070,level080,level090,level095,level099,level100}

	final static Map <CutoffIndex,Integer>cutoffDeciles = new EnumMap<CutoffIndex,Integer>(CutoffIndex.class);  //

	static {
		cutoffDeciles.put(CutoffIndex.level010, 0);
		cutoffDeciles.put(CutoffIndex.level020, 1);
		cutoffDeciles.put(CutoffIndex.level030, 2);
		cutoffDeciles.put(CutoffIndex.level040, 3);
		cutoffDeciles.put(CutoffIndex.level050, 4);
		cutoffDeciles.put(CutoffIndex.level060, 5);
		cutoffDeciles.put(CutoffIndex.level070, 6);
		cutoffDeciles.put(CutoffIndex.level080, 7);
		cutoffDeciles.put(CutoffIndex.level090, 8);
		cutoffDeciles.put(CutoffIndex.level095, 9);
		cutoffDeciles.put(CutoffIndex.level099, 10);
		cutoffDeciles.put(CutoffIndex.level100, 11);
	}
	
	
	final Map <CutoffIndex,Double>maxICcutoffs = new EnumMap<CutoffIndex,Double>(CutoffIndex.class);  //CutoffIndex -> cutoff value for max IC distribution
	final Map <CutoffIndex,Double>meanICcutoffs = new EnumMap<CutoffIndex,Double>(CutoffIndex.class); //CutoffIndex -> cutoff value for mean IC distribution
	

	
	public int taxonSize;
	public int geneSize;
	public double[] maxICdist;
	public double[] meanICdist;

	public PermutedProfileScore(double[] maxdistribution, double[] meandistribution, int taxaCount,int geneCount){
		taxonSize = taxaCount;
		geneSize = geneCount;
		maxICdist=maxdistribution;
		meanICdist=meandistribution;
		Arrays.sort(maxdistribution);  // this puts values in ascending order, opposite of what we what with HyperSS
		Arrays.sort(meandistribution);
		final int distsize = maxdistribution.length;
		maxICcutoffs.put(CutoffIndex.level010, maxICdist[(int)(0.9*distsize)]);
		meanICcutoffs.put(CutoffIndex.level010, meanICdist[(int)(0.9*distsize)]);

		maxICcutoffs.put(CutoffIndex.level020, maxICdist[(int)(0.8*distsize)]);
		meanICcutoffs.put(CutoffIndex.level020, meanICdist[(int)(0.8*distsize)]);

		maxICcutoffs.put(CutoffIndex.level030, maxICdist[(int)(0.7*distsize)]);
		meanICcutoffs.put(CutoffIndex.level030, meanICdist[(int)(0.7*distsize)]);

		maxICcutoffs.put(CutoffIndex.level040, maxICdist[(int)(0.6*distsize)]);
		meanICcutoffs.put(CutoffIndex.level040, meanICdist[(int)(0.6*distsize)]);

		maxICcutoffs.put(CutoffIndex.level050, maxICdist[(int)(0.5*distsize)]);
		meanICcutoffs.put(CutoffIndex.level050, meanICdist[(int)(0.5*distsize)]);

		maxICcutoffs.put(CutoffIndex.level060, maxICdist[(int)(0.4*distsize)]);
		meanICcutoffs.put(CutoffIndex.level060, meanICdist[(int)(0.4*distsize)]);

		maxICcutoffs.put(CutoffIndex.level070, maxICdist[(int)(0.3*distsize)]);
		meanICcutoffs.put(CutoffIndex.level070, meanICdist[(int)(0.3*distsize)]);

		maxICcutoffs.put(CutoffIndex.level080, maxICdist[(int)(0.2*distsize)]);
		meanICcutoffs.put(CutoffIndex.level080, meanICdist[(int)(0.2*distsize)]);

		maxICcutoffs.put(CutoffIndex.level090, maxICdist[(int)(0.1*distsize)]);
		meanICcutoffs.put(CutoffIndex.level090, meanICdist[(int)(0.1*distsize)]);

		maxICcutoffs.put(CutoffIndex.level095, maxICdist[(int)(0.05*distsize)]);
		meanICcutoffs.put(CutoffIndex.level095, meanICdist[(int)(0.05*distsize)]);

		maxICcutoffs.put(CutoffIndex.level099, maxICdist[(int)(0.01*distsize)]);
		meanICcutoffs.put(CutoffIndex.level099, meanICdist[(int)(0.01*distsize)]);

	}
	
	public boolean matchSize(int tSize, int gSize){
		return (taxonSize == tSize && geneSize == gSize);
	}

	

	private final static String lineSeparator = System.getProperty("line.separator");
	public void writeDist(String folder, ScoreType tp) throws IOException {
		final String reportFileName = folder + "/dist" + Integer.toString(taxonSize) + "_" + Integer.toString(geneSize);
		final BufferedWriter distWriter = new BufferedWriter(new FileWriter(new File(reportFileName)));
		double dist[];
		if (ScoreType.MAXIC == tp)
			dist = maxICdist;
		else
			dist = meanICdist;
		for(double d : dist){
			distWriter.append(Double.toString(d));
			distWriter.append(lineSeparator);
		}
		distWriter.close();
	}
	
	// return a decile code

	int getDecile(double s, ScoreType tp ){
		Map <CutoffIndex,Double> cutoffs;
		
		if (ScoreType.MAXIC == tp){
			cutoffs = maxICcutoffs;
		}
		else{
			cutoffs = meanICcutoffs;
		}
    // testing p-value distribution
	if (s<=cutoffs.get(CutoffIndex.level010))
		return cutoffDeciles.get(CutoffIndex.level010);
	else if (s<=cutoffs.get(CutoffIndex.level020))
		return cutoffDeciles.get(CutoffIndex.level020);
	else if (s<=cutoffs.get(CutoffIndex.level030))
		return cutoffDeciles.get(CutoffIndex.level030);
	else if (s<=cutoffs.get(CutoffIndex.level040))
		return cutoffDeciles.get(CutoffIndex.level040);
	else if (s<=cutoffs.get(CutoffIndex.level050))
		return cutoffDeciles.get(CutoffIndex.level050);
	else if (s<=cutoffs.get(CutoffIndex.level060))
		return cutoffDeciles.get(CutoffIndex.level060);
	else if (s<=cutoffs.get(CutoffIndex.level070))
		return cutoffDeciles.get(CutoffIndex.level070);
	else if (s<=cutoffs.get(CutoffIndex.level080))
		return cutoffDeciles.get(CutoffIndex.level080);
	else if (s<=cutoffs.get(CutoffIndex.level090))
		return cutoffDeciles.get(CutoffIndex.level090);
	else if (s<=cutoffs.get(CutoffIndex.level095))
		return cutoffDeciles.get(CutoffIndex.level095);
	else if (s<=cutoffs.get(CutoffIndex.level099))
		return cutoffDeciles.get(CutoffIndex.level099);
	else return cutoffDeciles.get(CutoffIndex.level100);
	}
	
	//Convenience functions
	
	public double cutoff095(ScoreType tp){
		if (ScoreType.MAXIC == tp)
			return maxICcutoffs.get(CutoffIndex.level095);
		else
			return meanICcutoffs.get(CutoffIndex.level095);
	}
	
	public double cutoff099(ScoreType tp){
		if (ScoreType.MAXIC == tp)
			return maxICcutoffs.get(CutoffIndex.level099);
		else
			return meanICcutoffs.get(CutoffIndex.level099);
	}

}