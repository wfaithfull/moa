package moa.clusterers.outliers.SPLL;

/**
 * ChiSquareAdapter
 * @author wfaithfull
 *
 * Implements a chi-square cdf.
 */
public class ChiSquareAdapter implements CumulativeDistributionFunctionProvider {

	@Override
	public double cumulativeProbability(double cv, int df) {
		if(cv < 0 || df < 1)
			return 0.0;
		
		double k = ((double)df) * 0.5;
		double x = cv * 0.5;
		
		if(df == 2)
			return Math.exp(-1.0 * x);
		
		double p = igf(k,x);
		if(Double.isNaN(p) || Double.isInfinite(p) || p < 1e-8)
			return 1e-14;
		
		p /= fastGamma(k);
		
		return 1.0 - p;
	}
	
	/**
	 * Incomplete gamma function
	 * @param s
	 * @param z
	 * @return
	 */
	private static double igf(double s, double z) {
		if(z < 1.0)
			return 0.0;
		
		double sc = (1.0 / s);
		sc *= Math.pow(z,s);
		sc *= Math.exp(-z);
		
		double sum, nom, denom;
		sum = nom = denom = 1.0;
		
		for(int i=0;i<200;i++) {
			nom *= z;
			s++;
			denom *= s;
			sum += nom / denom;
		}
		
		return sum * sc;
	}
	
	/**
	 * Fast gamma function for approximation of decimal factorials
	 * @param n 
	 * @return
	 */
	private static double fastGamma(double n) {
		final double RECIP_E = 0.36787944117144232159552377016147;  // RECIP_E = (E^-1) = (1.0 / E)
	    final double TWOPI = 6.283185307179586476925286766559;  // TWOPI = 2.0 * PI
	    
	    double d = 1.0 / (10.0d * n);
	    d = 1.0 / ((12*n) -d);
	    d = (d+n) * RECIP_E;
	    d = Math.pow(d,n);
	    d *= Math.sqrt(TWOPI / n);
	    
	    return d;
	}

}
