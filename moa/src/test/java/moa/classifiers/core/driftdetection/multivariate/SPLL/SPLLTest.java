package moa.classifiers.core.driftdetection.multivariate.SPLL;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import moa.classifiers.core.driftdetection.multivariate.SPLL.SPLL.LikelihoodResult;
import moa.streams.generators.RandomRBFGeneratorDrift;

import org.junit.Test;

import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.DenseInstanceData;
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
	public void testLogLLRBF() {

		SPLL spll = new SPLL();
		RandomRBFGeneratorDrift stream = new RandomRBFGeneratorDrift();
        stream.prepareForUse();
		
        for(int i=0;i<25000;i++) {
			Instances w1 = getRandomWindow(50, stream);
			Instances w2 = getRandomWindow(50, stream);
			LikelihoodResult result = spll.logLL(w1, w2);
			//System.out.println(String.format("%b %f %f",result.change, result.cStat, result.pStat));
        }
	}
	
	@Test
	public void testLogLLStatic() {

		SPLL spll = new SPLL();

		double[] x = new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		Instance inst = makeInstance(x);
		Instances insts = makeIdenticalInstances(inst, 10);
		LikelihoodResult ll = spll.logLL(insts, insts);
		
		assertEquals(false, ll.change);
		assertEquals(0.2857, ll.cStat, 0.001);
		assertEquals(1.1400, ll.pStat, 0.001);
	}
	
	static Instances makeIdenticalInstances(Instance inst, int n) {
		Instances insts = new Instances(new StringReader(""), n);
		for(int i=0;i<n;i++){
			insts.add(inst);
		}
		return insts;
	}
	
	static Instance makeInstance(double[] data) {
		Instance inst = new DenseInstance(data.length);
		
		for(int i=0;i<data.length;i++) {
			inst.setValue(i, data[i]);
		}
		return inst;
	}
	
	static Instances getRandomWindow(int size, RandomRBFGeneratorDrift stream) {
		int nSamples = 0;
		Instances data = new Instances(new StringReader("I don't understand why this is necessary."), 50);
		while(stream.hasMoreInstances() && nSamples < size) {
        	Instance newInst = stream.nextInstance().getData();
        	data.add(newInst);
        	nSamples++;
        }
		return data;
	}

}
