package moa.classifiers.core.driftdetection.multivariate.SPLL;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;

public class ApacheKMeansAdapter implements ClusterProvider {

	private static final long serialVersionUID = -7012522276632821253L;

	private final class DoubleAdapter implements Clusterable {

		private double[] inst;
		
		public DoubleAdapter(double[] inst) {
			this.inst = inst;
		}
		
		@Override
		public double[] getPoint() { 
			return inst;
		}
		
	}
	
	// Lazily initialised transient field to avoid serialisation. See getClusterer().
	private transient KMeansPlusPlusClusterer<DoubleAdapter> clusterer;
	private int k;
	private int maxIterations;
	
	public ApacheKMeansAdapter(final int k, final int maxIterations) {
		this.k = k;
		this.maxIterations = maxIterations;
	}
	
	public List<double[][]> cluster(final double[][] data) {
		int nInst = data.length;
		int nFeatures = data[0].length;

		List<DoubleAdapter> points = new ArrayList<DoubleAdapter>();
		for(int i=0;i<nInst;i++) {
			points.add(new DoubleAdapter(data[i]));
		}
		
		List<CentroidCluster<DoubleAdapter>> clusters = getClusterer().cluster(points);
		
		List<double[][]> convertedClusters = new ArrayList<double[][]>();
		for(CentroidCluster<DoubleAdapter> cluster : clusters) {
			
			List<DoubleAdapter> clusterData = cluster.getPoints();
			int clusterSize = clusterData.size();
			double[][] rawClusterData = new double[clusterSize][nFeatures];
			for(int i=0; i < clusterSize; i++){
				rawClusterData[i] = clusterData.get(i).getPoint();
			}
			convertedClusters.add(rawClusterData);
		}
			
		return convertedClusters;
		
	}

	public KMeansPlusPlusClusterer<DoubleAdapter> getClusterer() {
		return clusterer == null ? new KMeansPlusPlusClusterer<DoubleAdapter>(k, maxIterations, new EuclideanDistance(), 
				new JDKRandomGenerator(), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE) : clusterer;
	}

}
