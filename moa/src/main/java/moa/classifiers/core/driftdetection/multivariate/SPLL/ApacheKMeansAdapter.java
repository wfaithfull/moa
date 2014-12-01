package moa.classifiers.core.driftdetection.multivariate.SPLL;

import java.util.ArrayList;
import java.util.List;

import moa.cluster.InstanceRetainingCluster;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

public class ApacheKMeansAdapter implements ClusterProvider {
	
	private final class InstanceAdapter implements Clusterable {
		
		private Instance inst;
		
		public InstanceAdapter(Instance inst) {
			this.inst = inst;
		}

		@Override
		public double[] getPoint() {
			return inst.toDoubleArray();
		}
		
		public Instance getInstance() {
			return inst;
		}
		
		
	}

	@Override
	public List<InstanceRetainingCluster> cluster(final Instances data, final int k, final int maxIterations) {

		int nInst = data.numInstances();
		
		KMeansPlusPlusClusterer<InstanceAdapter> km = 
				new KMeansPlusPlusClusterer<InstanceAdapter>(k, maxIterations);
		
		List<InstanceAdapter> points = new ArrayList<InstanceAdapter>();
		for(int i = 0; i < nInst; i++) {
			points.add(new InstanceAdapter(data.get(i)));
		}
		
		List<CentroidCluster<InstanceAdapter>> clusters = km.cluster(points);
		
		List<InstanceRetainingCluster> adaptedClusters = new ArrayList<InstanceRetainingCluster>();
		
		for(CentroidCluster<InstanceAdapter> cluster : clusters) {
			InstanceRetainingCluster thisCluster = new InstanceRetainingCluster(new DenseInstance(1, cluster.getCenter().getPoint()));
			for (InstanceAdapter instanceAdapter : cluster.getPoints()) {
				thisCluster.addInstance(instanceAdapter.getInstance());
			}
			adaptedClusters.add(thisCluster);
		}
		
		return adaptedClusters;
	}

}
