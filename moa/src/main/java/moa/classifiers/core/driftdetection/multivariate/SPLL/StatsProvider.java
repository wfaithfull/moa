package moa.classifiers.core.driftdetection.multivariate.SPLL;

import java.util.List;

import com.yahoo.labs.samoa.instances.Instance;

public interface StatsProvider {
	public double[] featureWiseVariance(double[][] data);
	
	public double[] featureWiseVariance(List<Instance> data);
	
	public double cumulativeProbability(double x, int df);
}
