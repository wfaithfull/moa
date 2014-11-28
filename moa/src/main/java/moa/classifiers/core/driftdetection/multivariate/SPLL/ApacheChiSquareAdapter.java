package moa.classifiers.core.driftdetection.multivariate.SPLL;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public class ApacheChiSquareAdapter implements CumulativeDistributionFunctionProvider {

	@Override
	public double cumulativeProbability(double x, int df) {
		
		final ChiSquaredDistribution cdf = new ChiSquaredDistribution(df);
		//System.out.println(x + " || " + df + " || " + cdf.cumulativeProbability(x));
		return cdf.cumulativeProbability(x);
	}

}
