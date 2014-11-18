package moa.clusterers.outliers.SPLL;

import java.io.StringReader;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

import moa.clusterers.outliers.SPLL.SPLL.LikelihoodResult;
import moa.streams.clustering.RandomRBFGeneratorEvents;

/**
 * @author wfaithfull
 * 
 * Test class for SPLL.
 */
public class Test {
	
	public static void main(String[] args) {
		
		int n = 10000;
	        
        RandomRBFGeneratorEvents stream = new RandomRBFGeneratorEvents();
        stream.prepareForUse();
		
		ClusterProvider kmeans = new KMeansAdapter();
		CumulativeDistributionFunctionProvider cdf = new ChiSquareAdapter();
		
		SPLL logLL = new SPLL(kmeans, cdf);
		
		print("new spll");
		
		logLL.setModelContext(stream.getHeader());
        logLL.prepareForUse();  
        
        print("getting windows");
        Instances w1 = getRandomWindow(50, stream);
        Instances w2 = getRandomWindow(50, stream);
        
        print("logll");
        LikelihoodResult r = logLL.logLL(w1, w2);
        
        print("done.");
		
		System.out.println(String.format("Result = %b %f %f", r.change, r.cStat, r.pStat));
	}
	
	static void print(String s) {
		System.out.println(s);
	}
	
	static Instances getRandomWindow(int size, RandomRBFGeneratorEvents stream) {
		int nSamples = 0;
		Instances data = new Instances(new StringReader("This is ridiculous."), 50);
		while(stream.hasMoreInstances() && nSamples < size) {
        	Instance newInst = stream.nextInstance().getData();
        	data.add(newInst);
        	nSamples++;
        }
		return data;
	}
}
