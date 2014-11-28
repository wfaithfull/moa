package moa.classifiers.core.driftdetection.multivariate.SPLL;

import java.util.List;

import moa.cluster.InstanceRetainingCluster;

import com.yahoo.labs.samoa.instances.Instances;

/**
 * Created by Will Faithfull on 14/10/14.
 */
public interface ClusterProvider {

    List<InstanceRetainingCluster> cluster(Instances data, int k, int maxIterations);

}
