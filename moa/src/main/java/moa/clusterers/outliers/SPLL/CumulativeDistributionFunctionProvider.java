package moa.clusterers.outliers.SPLL;

public interface CumulativeDistributionFunctionProvider {
	
	double cumulativeProbability(double x, int df);
	
}
