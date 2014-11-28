package moa.classifiers.core.driftdetection.multivariate.SPLL;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import moa.classifiers.core.driftdetection.multivariate.SPLL.SPLL.LikelihoodResult;
import moa.streams.ConceptDriftStream;
import moa.streams.clustering.RandomRBFGeneratorEvents;
import moa.streams.generators.RandomRBFGeneratorDrift;

import org.junit.Test;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

public class SPLLTest {

	@Test
	public void testSPLLClusterProviderCumulativeDistributionFunctionProvider() {
		
		ClusterProvider km = new ApacheKMeansAdapter();
		CumulativeDistributionFunctionProvider cdf = new ApacheChiSquareAdapter();
		
		SPLL spll = new SPLL(km, cdf);
		
		assertEquals(100,	spll.getMaxIterations());
		assertEquals(3, 	spll.getNumClusters());
		assertEquals(km,	spll.getClusterer());
		assertEquals(cdf,	spll.getCdf());
	}

	@Test
	public void testSPLL() {
		SPLL spll = new SPLL();
		assertEquals(100,	spll.getMaxIterations());
		assertEquals(3, 	spll.getNumClusters());
	}

	@Test
	public void testLogLL() {

		SPLL spll = new SPLL();
		RandomRBFGeneratorDrift stream = new RandomRBFGeneratorDrift();
        stream.prepareForUse();
		
        for(int i=0;i<25000;i++) {
			Instances w1 = getRandomWindow(50, stream);
			Instances w2 = getRandomWindow(50, stream);
			LikelihoodResult result = spll.logLL(w1, w2);
			System.out.println(String.format("%b %f %f",result.change, result.cStat, result.pStat));
        }
	}
	
	static Instances getRandomWindow(int size, RandomRBFGeneratorDrift stream) {
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
