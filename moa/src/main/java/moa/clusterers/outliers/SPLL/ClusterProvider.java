package moa.clusterers.outliers.SPLL;

import com.yahoo.labs.samoa.instances.Instances;

import moa.cluster.Clustering;

/**
 * Created by Will Faithfull on 14/10/14.
 */
public interface ClusterProvider {

    Clustering kMeans(Instances data, int k, int maxIterations);

}
