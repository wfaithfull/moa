package moa.clusterers.outliers.SPLL;

public interface ChiSquareDistributionProvider {
	double cumulativeProbability(double x, double df);
}
