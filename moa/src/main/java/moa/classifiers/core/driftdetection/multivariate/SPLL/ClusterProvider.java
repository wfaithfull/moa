package moa.classifiers.core.driftdetection.multivariate.SPLL;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Will Faithfull on 14/10/14.
 */
public interface ClusterProvider extends Serializable {

    List<double[][]> cluster(double[][] data, int k, int maxIterations);

}
