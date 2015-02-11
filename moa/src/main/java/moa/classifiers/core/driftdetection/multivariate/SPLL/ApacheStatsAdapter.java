package moa.classifiers.core.driftdetection.multivariate.SPLL;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

public class ApacheStatsAdapter implements StatsProvider {
	
	Variance v;
	
	public ApacheStatsAdapter()
	{
		 v = new Variance();
	}

	@Override
	public double[] featureWiseVariance(double[][] data) {
		
		RealMatrix m = MatrixUtils.createRealMatrix(data);
		int nFeatures = m.getColumnDimension();
		
		double[] variance = new double[nFeatures];
		for(int j=0;j<nFeatures;j++)
		{
			variance[j] = v.evaluate(m.getColumn(j));
		}

		return variance;
	}
	
	@Override
	public double cumulativeProbability(double x, int df) {
		
		final ChiSquaredDistribution cdf = new ChiSquaredDistribution(df);
		//System.out.println(x + " || " + df + " || " + cdf.cumulativeProbability(x));
		return cdf.cumulativeProbability(x);
	}

}
