package moa.clusterers.outliers.SPLL;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public class ApacheChiSquareAdapter implements CumulativeDistributionFunctionProvider {

	@Override
	public double cumulativeProbability(double x, int df) {
		final ChiSquaredDistribution cdf = new ChiSquaredDistribution(df);
		return cdf.cumulativeProbability(x);
	}

}
