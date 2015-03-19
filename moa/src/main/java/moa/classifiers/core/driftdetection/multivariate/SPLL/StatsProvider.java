package moa.classifiers.core.driftdetection.multivariate.SPLL;

import java.io.Serializable;

public interface StatsProvider extends Serializable {
	public double[] featureWiseVariance(double[][] data);
	
	public double cumulativeProbability(double x, int df);
}
