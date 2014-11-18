package moa.distance;

import com.yahoo.labs.samoa.instances.Instance;

public interface DistanceMeasure<T extends Instance> {
	double compute(T vec1, T vec2);
}
