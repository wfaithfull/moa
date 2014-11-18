package moa.clusterers.outliers.SPLL;

import java.util.ArrayList;
import java.util.List;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

import moa.cluster.Cluster;
import moa.cluster.Clustering;
import moa.cluster.InstanceRetainingCluster;
import moa.clusterers.KMeansPlusPlus;

public class KMeansAdapter implements ClusterProvider {

	@Override
	public Clustering kMeans(Instances data, int k, int maxIterations) {

		KMeansPlusPlus<Instance> kmpp = new KMeansPlusPlus<Instance>(k, maxIterations);
		
		List<Instance> convertedData = new ArrayList<Instance>();
		for(int i=0;i<data.numInstances();i++) {
			convertedData.add(data.get(i));
		}
		
		
		List<InstanceRetainingCluster> clustered = new ArrayList<InstanceRetainingCluster>();
		Clustering clustering = null;
		try {
			clustered = kmpp.cluster(convertedData);
			clustering = new Clustering();
			for(InstanceRetainingCluster irc : clustered) {
				clustering.add((Cluster)irc);
			}
		}
		catch (Exception ex) {
			System.out.print("Oh no.");
		}
		
		return clustering;
	}
	
	public List<InstanceRetainingCluster> kMeansPP(final List<? extends Instance> data, final int k){
		
		// TODO K Means Plus Plus
		return null;
	}

}
