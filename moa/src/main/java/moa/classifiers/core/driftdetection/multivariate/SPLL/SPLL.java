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
	private CumulativeDistributionFunctionProvider cdf;

	public SPLL(final ClusterProvider clusterer, final CumulativeDistributionFunctionProvider cdf) {
		this.setClusterer(clusterer);
		this.setCdf(cdf);
		
		setMaxIterations(DEFAULT_MAX_ITERATIONS);
		setNumClusters(DEFAULT_N_CLUSTERS);
	}
	
	public SPLL() {
		// We use K-Means and Chi Square.
		this(new ApacheKMeansAdapter(), new ApacheChiSquareAdapter());
	}
	
	private List<double[]> getClusterVariance(List<InstanceRetainingCluster> clusters) {
		
		int nFeatures = clusters.get(0).getCenter().length;
		
		List<double[]> clusterVariance = new ArrayList<double[]>();
        // Calculate the REFERENCE distribution from Window 1
        for(InstanceRetainingCluster cluster : clusters) {
            double nObservations       = cluster.getWeight();
            double[] center         = cluster.getCenter();
            double maxLikelihood    = 1 / nObservations;

            List<Instance> data = cluster.getInstances();
            double[] variance = new double[nFeatures];

            for(int i=0;i<nObservations; i++) {
                double[] observation = data.get(i).toDoubleArray();
                // (value of each feature for each observation MINUS cluster mean for that feature)^SQUARED
                // Multiply that by 1 / Nk (Maximum likelihood estimate of the variance)
                for(int j=0;j<nFeatures; j++) {
                    variance[j] = Math.pow(observation[j] - center[j], 2) * maxLikelihood;
                }
            }

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

        // Calculate class priors by counting cluster membership
        List<Integer> classPriors = new ArrayList<Integer>();
        for(InstanceRetainingCluster cluster : clusters) {
			classPriors.add((int)cluster.getWeight());
		}
        
        List<double[]> clusterMeans = getClusterMeans(clusters);
        List<double[]> clusterVariance = getClusterVariance(clusters);
        
        System.out.println("Means");
        debugPrintList(clusterMeans);
        System.out.println("------------------------------\nVariance");
        debugPrintList(clusterVariance);
        System.out.println("------------------------------");

        int totalObservations   = w1.numInstances();
        int nFeatures           = w1.get(0).toDoubleArray().length;	

        /* Combine cluster variances into the final covariance matrix, weighted by priors.
        ~ One covariance matrix to rule them all,
        ~ One covariance matrix to find them.
        ~ One covariance matrix to bring them all,
        ~ And in the darkness bind them.
        */
        double[][] finalCovariance = new double[nFeatures][nFeatures];
        double[] featureVariance = new double[nFeatures];
        double minVariance = Double.MAX_VALUE;
        
        for(int j=0;j<nFeatures;j++) {
	        double cov = 0;
	        // Sum over clusters
	        for(int k=0;k<clusters.size();k++) {
	        	cov += classPriors.get(k) * clusterVariance.get(k)[j];
	        }
	        
	        if(cov != 0 && cov < minVariance)
                minVariance = cov;
	        
	        finalCovariance[j][j] = cov; // We can cheat and only do the diagonal
	        featureVariance[j] = cov;
        }

        double[] reciprocalVariance = new double[nFeatures];

        for(int j=0;j<nFeatures;j++) {
            if(featureVariance[j] == 0)
                featureVariance[j] = minVariance;
            reciprocalVariance[j] = 1 / featureVariance[j];
        }

        double logLikelihoodTerm = 0;
        for(int i=0;i<totalObservations;i++) {
            double minDist = Double.MAX_VALUE;
            for (int k = 0; k < clusters.size(); k++) {
                double dist = mahalanobis(w2.get(i).toDoubleArray(), clusterMeans.get(k), reciprocalVariance);
                if (dist < minDist) {
                    minDist = dist;
                }
            }

            logLikelihoodTerm += minDist;
        }
        
        double negLL = logLikelihoodTerm / totalObservations;
        
        double a = getCdf().cumulativeProbability(negLL, nFeatures);
        double b = 1-a;
        
        boolean change = negLL > nFeatures + Math.sqrt(2*nFeatures);
        
        double pStat = a < b ? a : b;
        // result.change = result.pStat < 0.05; // As yet undetermined issue with this approach.

        return new LikelihoodResult(change, pStat, negLL);
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

	public CumulativeDistributionFunctionProvider getCdf() {
		return cdf;
	}

	public void setCdf(CumulativeDistributionFunctionProvider cdf) {
		this.cdf = cdf;
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
