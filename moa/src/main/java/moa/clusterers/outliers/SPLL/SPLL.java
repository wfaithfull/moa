package moa.clusterers.outliers.SPLL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;

import moa.cluster.InstanceRetainingCluster;
import moa.cluster.Cluster;
import moa.cluster.Clustering;
import moa.clusterers.outliers.MyBaseOutlierDetector;
import moa.clusterers.outliers.SPLL.CumulativeDistributionFunctionProvider;
import moa.clusterers.outliers.SPLL.ClusterProvider;

/**
 * Created by Will Faithfull on 10/10/14.
 */
public class SPLL extends MyBaseOutlierDetector {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8146972197755773303L;
	
	private final static long MIN_OBJ_ID = 1L;
    private final static int DEFAULT_CLUSTERS = 3;
    private final static int MAX_ITERATIONS = 100;

    private long _objId;
    private int _windowSize;
    private int _numClusters;

    private ClusterProvider _clusterer;
    private CumulativeDistributionFunctionProvider _cdf;

    public long get_objId() {
        return _objId;
    }

    public int get_windowSize() {
        return _windowSize;
    }

    public int getNumClusters() {
        return _numClusters;
    }

    public void setNumClusters(int _numClusters) {
        this._numClusters = _numClusters;
    }

    
    public SPLL(ClusterProvider clusterer, CumulativeDistributionFunctionProvider cdf) {
        _clusterer = clusterer;
        _cdf = cdf;
        _numClusters = DEFAULT_CLUSTERS;
    }
    
    public SPLL() {
    	this(new KMeansAdapter(), new ChiSquareAdapter());
    }

    @Override
    public void Init() {
        _windowSize = windowSizeOption.getValue();
    }

    public long getWindowEnd() {
        return _objId;
    }

    public long getWindowStart() {
        long start = getWindowEnd() - _windowSize;
        // Guard against negatives; if < windowSize instances, grow the window.
        return start > MIN_OBJ_ID ? start : MIN_OBJ_ID;
    }

    @Override
    public void ProcessNewStreamObj(Instance inst) {
    	this.processNewInstanceImpl(inst);
    }
    
    @Override
    public void processNewInstanceImpl(Instance inst) {
    	super.processNewInstanceImpl(inst);
    }

    public LikelihoodResult logLL(Instances w1, Instances w2) {
        Clustering clusters = _clusterer.kMeans(w1, _numClusters, MAX_ITERATIONS);
        
        ArrayList<Instance> w1conv = new ArrayList<Instance>();
        for(int i=0;i<w1.numInstances();i++) {
        	w1conv.add(w1.get(i));
        }
        
        for(Entry<Integer, Integer> point : Clustering.classValues(w1conv).entrySet()){
        	System.out.println(String.format("%d %d", point.getKey(), point.getValue()));
        }

        List<Integer> classPriors = new ArrayList<Integer>();
        List<double[]> clusterMeans = new ArrayList<double[]>();
        List<double[]> clusterVariance = new ArrayList<double[]>();

        int totalObservations   = w1.numInstances();
        int nFeatures           = clusters.dimension();

        // Calculate the REFERENCE distribution from Window 1
        for(Cluster moacluster : clusters.getClustering()) {
            InstanceRetainingCluster cluster = (InstanceRetainingCluster)moacluster;

            int nObservations       = (int)cluster.getWeight();
            double[] center         = cluster.getCenter();
            double maxLikelihood    = 1 / nObservations;

            clusterMeans.add(center);
            classPriors.add(nObservations / totalObservations);

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
	        
	        finalCovariance[j][j] = cov;
	        featureVariance[j] = cov;
        }
        /*
        for(int i=0;i<totalObservations;i++) {
            for(int j=0;j<nFeatures; j++) {
                cov = 0;
               
                for(int k=0;k<clusters.size();k++) {
                	// Don't think this is right. Need to know what cluster the ith
                	// observation falls into.
                	for(int n=0;n<clusters.get(k).getWeight();n++) {
                		cov += classPriors.get(k) * clusterVariance.get(k)[n][j];
                	}
                }
                finalCovariance[i][j] = cov;
            }
        }*/

        //double[] featureVariance = new double[nFeatures];
        double[] reciprocalVariance = new double[nFeatures];
        	
        
        
        /*for(int j=0;j<nFeatures; j++) {
            double total = 0;

            for(int i=0;i<totalObservations; i++) {
                total += finalCovariance[i][j];
            }
            double variance = total / totalObservations;

            if(variance != 0 && variance < minVariance)
                minVariance = variance;

            featureVariance[j] = variance;

        }*/

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
        LikelihoodResult result = new LikelihoodResult();
        result.cStat = logLikelihoodTerm / totalObservations;
        
        double a = _cdf.cumulativeProbability(result.cStat, nFeatures);
        double b = 1-a;

        result.pStat = a < b ? a : b;

        result.change = result.pStat < 0.05;
        return result;
    }

    static double mahalanobis(double[] xx, double[] mk, double[] reciprocals) {
        double dist = 0;
        for(int j=0;j<xx.length;j++) {
            dist += Math.pow(xx[j] - mk[j], 2) * reciprocals[j];
        }
        return dist;
    }



    //region Internal classes
    public class LikelihoodResult {
        public boolean change;
        public double pStat;
        public double cStat;
    }
    //endregion
}
