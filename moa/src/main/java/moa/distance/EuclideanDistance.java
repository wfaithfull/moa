package moa.distance;

import com.yahoo.labs.samoa.instances.Instance;

public class EuclideanDistance<T extends Instance> implements DistanceMeasure<T> {

	@Override
	public double compute(T vec1, T vec2) {
		final double[] p1 = vec1.toDoubleArray();
		final double[] p2 = vec2.toDoubleArray();
		
		double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            sum += Math.abs(p1[i] - p2[i]);
        }
        return sum;
	}

}
