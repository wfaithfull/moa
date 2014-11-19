package moa.clusterers.outliers.SPLL;

import java.util.List;

import com.yahoo.labs.samoa.instances.Instances;

import moa.cluster.Clustering;
import moa.cluster.InstanceRetainingCluster;

/**
 * Created by Will Faithfull on 14/10/14.
 */
public interface ClusterProvider {

    List<InstanceRetainingCluster> cluster(Instances data, int k, int maxIterations);

}
