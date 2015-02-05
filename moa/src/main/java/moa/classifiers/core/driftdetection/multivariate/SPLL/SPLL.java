package moa.classifiers.core.driftdetection.multivariate.SPLL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import moa.cluster.InstanceRetainingCluster;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

/**
 * Semi Parametric Log Likelihood change detection criterion for multivariate data.
 * 
 * Algorithm courtesy of Prof. Ludmila I. Kuncheva (l.i.kuncheva@bangor.ac.uk), introduced in:
 * 
 * Change detection in streaming multivariate data using likelihood detectors.
 * Knowledge and Data Engineering, IEEE Transactions on 25.5 (2013): 1175-1180.
 * 
 * @author Will Faithfull (w.faithfull@bangor.ac.uk)
 */
public class SPLL {
	
	// These are the values we tend to use.
	private final int DEFAULT_MAX_ITERATIONS = 100;
	private final int DEFAULT_N_CLUSTERS = 3;
	
	// But you can change them if you disagree.
	private int maxIterations;
	private int numClusters;
	
	// Injected clustering and CDF implementations.
	private ClusterProvider clusterer;
	private StatsProvider stats;

	public SPLL(final ClusterProvider clusterer,final StatsProvider stats) {
		this.setClusterer(clusterer);
		this.setStatsProvider(stats);
		
		setMaxIterations(DEFAULT_MAX_ITERATIONS);
		setNumClusters(DEFAULT_N_CLUSTERS);
	}
	
	public SPLL() {
		// We use K-Means and Chi Square.
		this(new ApacheKMeansAdapter(), new ApacheStatsAdapter());
	}
	
	private List<double[]> getClusterVariance(List<InstanceRetainingCluster> clusters) {
		
		List<double[]> clusterVariance = new ArrayList<double[]>();
		// Calculate the REFERENCE distribution from Window 1
        for(InstanceRetainingCluster cluster : clusters) {
        	double[] variance = stats.featureWiseVariance(cluster.getInstances());
        	clusterVariance.add(variance);
        }
		
        return clusterVariance;
	}
	
	private List<double[]> getClusterMeans(List<InstanceRetainingCluster> clusters) {
		List<double[]> clusterMeans = new ArrayList<double[]>();
		for(InstanceRetainingCluster cluster : clusters) {
			double[] center = cluster.getCenter();
			clusterMeans.add(center);
		}
		return clusterMeans;
	}
	
	private static double mahalanobis(double[] xx, double[] mk, double[] reciprocals) {
        double dist = 0;
        for(int j=0;j<xx.length;j++) {
            dist += Math.pow(xx[j] - mk[j], 2) * reciprocals[j];
        }
        return dist;
    }
	
	public void debugPrintList (List<double[]> toPrint)
	{
		for(double[] row : toPrint)
		{
			System.out.println(Arrays.toString(row));
		}
	}
	
    public LikelihoodResult logLL(Instances w1, Instances w2) {
    	
    	// Cluster w1 using injected clustering strategy
        List<InstanceRetainingCluster> clusters = getClusterer().cluster(w1, numClusters, maxIterations);
    	
    	int totalObservations   = w1.numInstances();
        int nFeatures           = w1.get(0).toDoubleArray().length;	
        int nClusters			= clusters.size();
        
        double[] classCount 	= new double[nClusters];
        double[] classPriors 	= new double[nClusters];

        // Calculate class priors by counting cluster membership
        for(int k=0;k<nClusters;k++) {
        	InstanceRetainingCluster cluster = clusters.get(k);
			classCount[k] 	= cluster.getWeight();
			classPriors[k] 	= classCount[k] / totalObservations;
		}
        
        List<double[]> clusterMeans = getClusterMeans(clusters);
        List<double[]> clusterVariance = getClusterVariance(clusters);

        /* Combine cluster variances into the final covariance matrix, weighted by priors.
        ~ One covariance matrix to rule them all,
        ~ One covariance matrix to find them.
        ~ One covariance matrix to bring them all,
        ~ And in the darkness bind them.
        */
        double[] featureVariance = new double[nFeatures];
        double minVariance = Double.MAX_VALUE;
        
        for(int j=0;j<nFeatures;j++) {
	        double cov = 0;
	        
	        // Sum over clusters. Weight by priors.
	        for(int k=0;k<clusters.size();k++) {
	        	cov += (clusterVariance.get(k)[j] * classPriors[k]);
	        }

	        if(cov != 0 && cov < minVariance)
                minVariance = cov;

	        featureVariance[j] = cov; // We can cheat and only do the diagonal
        }

        double[] reciprocalVariance = new double[nFeatures];

        for(int j=0;j<nFeatures;j++) {
            if(featureVariance[j] == 0)
                featureVariance[j] = minVariance; // Guard against 0 variance
            
            reciprocalVariance[j] = 1 / featureVariance[j]; // Precalculate reciprocals
        }
		
        double logLikelihoodTerm = 0;
        for(int i=0;i<totalObservations;i++) {
            double minDist = Double.MAX_VALUE;
            for (int k = 0; k < nClusters; k++) {
            	
            	// This is a transliteration of the actual MATLAB code
            	double[] distanceToMean = new double[nFeatures];
            	for(int j=0;j<nFeatures;j++)
            	{
            		double[] clusterMean = clusterMeans.get(k);
            		double[] xx = w2.get(i).toDoubleArray();
            		distanceToMean[j] = (clusterMean[j] - xx[j]);
            	}
            	double dst = 0;
            	for(int j=0;j<nFeatures;j++)
            	{
            		dst += (distanceToMean[j] * featureVariance[j]) * distanceToMean[j];
            	}
            	// </transliteration>
            	
            	// This is the suggested measure. They produce quite different results.
                //double dst = mahalanobis(w2.get(i).toDoubleArray(), clusterMeans.get(k), reciprocalVariance);
            	
                if (dst < minDist) {
                    minDist = dst;
                }
            }

            logLikelihoodTerm += minDist;
        }
        
        double negLL 	= logLikelihoodTerm / totalObservations; // Mean Log-Likelihood term
        
        double cStat 	= negLL / (nFeatures + Math.sqrt(2*nFeatures));
        
        double a 		= getStatsProvider().cumulativeProbability(cStat, nFeatures);
        double b 		= 1-a;

        double pStat 	= a < b ? a : b;
        boolean change 	= negLL > nFeatures + Math.sqrt(2*nFeatures);
        // boolean change = pStat < 0.05;
        
        return new LikelihoodResult(change, pStat, cStat);
    }

    public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public int getNumClusters() {
		return numClusters;
	}

	public void setNumClusters(int numClusters) {
		this.numClusters = numClusters;
	}
	
	public ClusterProvider getClusterer() {
		return clusterer;
	}

	public void setClusterer(ClusterProvider clusterer) {
		this.clusterer = clusterer;
	}
	
	public StatsProvider getStatsProvider() {
		return stats;
	}
	
	public void setStatsProvider(StatsProvider provider)
	{
		this.stats = provider;
	}

    public class LikelihoodResult {
    	
        public final boolean change;
        public final double pStat;
        public final double cStat;
        
        public LikelihoodResult(final boolean change, final double pStat, final double cStat)
        {
        	this.change = change;
        	this.pStat = pStat;
        	this.cStat = cStat;
        }
        
    }
}
