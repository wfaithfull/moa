package moa.classifiers.core.driftdetection.multivariate.SPLL;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import moa.classifiers.core.driftdetection.multivariate.SPLL.SPLL.LikelihoodResult;
import moa.streams.ArffFileStream;
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
        	double[][] w1 = getRandomWindowRaw(50, stream);
			double[][] w2 = getRandomWindowRaw(50, stream);
			LikelihoodResult result = spll.logLL(w1, w2);
        }
	}
	
	static double[][] instancesTo2DArray(Instances moaData)
	{
		int nObsv = moaData.numInstances();
		int nAttr = moaData.numAttributes();
		
		double[][] toReturn = new double[nObsv][nAttr];
		for(int i=0;i<nObsv;i++) {
			toReturn[i] = moaData.get(i).toDoubleArray();
		}
		
		return toReturn;
	}
	
	@Test
	public void testLogLLStatic() throws IOException {
		SPLL spll = new SPLL();
		Instances data = loadDataFromArff();
		
		int nAttr = data.get(0).toDoubleArray().length;
		int nObsv = data.numInstances();
		
		double[][] rawData = new double[nObsv][nAttr];
		
		for(int i=0; i<nObsv; i++)
		{
			rawData[i] = data.get(i).toDoubleArray();
		}
		
		LikelihoodResult llr = spll.logLL(rawData, rawData);
		System.out.println(String.format("[c=%b,pst=%f,st=%f]",llr.change, llr.pStat,llr.cStat));
		
		assertEquals(false, llr.change);
	}
	
	public void testSpeed() throws IOException {
		SPLL spll = new SPLL();
		
		double[][] data = instancesTo2DArray(loadDataFromArff());
		
		final int RUNS = 10000;
		long duration = 0;
		
		for(int i=0;i<RUNS;i++)
		{
			long start = System.nanoTime();
			spll.logLL(data, data);
			long end = System.nanoTime();
			duration += (end-start);
		}
		
		System.out.println(String.format("Duration of 10k runs: %d seconds\nAverage per run: %d seconds", duration/1000, (duration/RUNS)/1000));
	}

	/*
	 * Helper method loads iris.arff into MOA Instances
	 */
	static Instances loadDataFromArff()
	{
		ArffFileStream afs = new ArffFileStream("iris.arff", 0);

		Instances data = new Instances(new StringReader(""), (int)afs.estimatedRemainingInstances());
		
		while(afs.hasMoreInstances()) {
			Instance inst = afs.nextInstance().instance;
			data.add(inst);	
		}
		
		return data;
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
	
	static double[][] getRandomWindowRaw(int size, RandomRBFGeneratorDrift stream) {
		int nSamples = 0;
		double[][] data = new double[size][stream.getHeader().numAttributes()];
		
		while(stream.hasMoreInstances() && nSamples < size) {
			data[nSamples] = stream.nextInstance().getData().toDoubleArray();
			nSamples++;
		}
		
		return data;
	}

}
