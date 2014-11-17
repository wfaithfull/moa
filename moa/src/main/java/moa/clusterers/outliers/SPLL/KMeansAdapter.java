package moa.clusterers.outliers.SPLL;

import java.util.ArrayList;
import java.util.List;

import com.yahoo.labs.samoa.instances.Instances;

import moa.cluster.Cluster;
import moa.cluster.Clustering;
import moa.cluster.SphereCluster;
import moa.clusterers.KMeans;

public class KMeansAdapter implements ClusterProvider {

	@Override
	public Clustering kMeans(Instances data, int k, int maxIterations) {

		Cluster[] centres = new Cluster[k];
		
		for(int i=0;i<k; i++) {
			centres[i] = new SphereCluster(data.get(i).toDoubleArray(), 1);
		}
		
		List<Cluster> dataAsClusters = new ArrayList<Cluster>();
		
		for(int i=0;i<data.numInstances();i++) {
			dataAsClusters.add(new SphereCluster(data.get(i).toDoubleArray(),1));
		}
		
		return KMeans.kMeans(centres, dataAsClusters);
	}

}
