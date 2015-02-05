package moa.classifiers.core.driftdetection.multivariate.SPLL;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import moa.classifiers.core.driftdetection.multivariate.SPLL.SPLL.LikelihoodResult;
import moa.streams.generators.RandomRBFGeneratorDrift;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.junit.Test;

import com.yahoo.labs.samoa.instances.ArffLoader;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

public class SPLLTest {

	@Test
	public void testSPLLClusterProviderCumulativeDistributionFunctionProvider() {
		
		ClusterProvider km = new ApacheKMeansAdapter();
		StatsProvider stats = new ApacheStatsAdapter();
		
		SPLL spll = new SPLL(km, stats);
		
		assertEquals(100,	spll.getMaxIterations());
		assertEquals(3, 	spll.getNumClusters());
		assertEquals(km,	spll.getClusterer());
		assertEquals(stats,	spll.getStatsProvider());
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
	public void testLogLLStatic() throws IOException {

		final int INSTANCES = 150; // Horrible, but no way to get #instances from ArffLoader!
		
		SPLL spll = new SPLL();
		Reader reader = new FileReader("iris.arff");
		
		ArffLoader loader = new ArffLoader(reader);
		Instances data = new Instances(new StringReader(""), INSTANCES);
		
		for(int i=0; i<INSTANCES;i++) {
			Instance inst = loader.readInstance();
			data.add(inst);	
		}
		
		LikelihoodResult ll = spll.logLL(data, data);
		System.out.println(String.format("[c=%b,pst=%f,st=%f]",ll.change, ll.pStat,ll.cStat));
		
		assertEquals(false, ll.change);
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
