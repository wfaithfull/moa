package moa.classifiers.core.driftdetection.multivariate.SPLL;

public interface CumulativeDistributionFunctionProvider {
	
	double cumulativeProbability(double x, int df);
	
}
