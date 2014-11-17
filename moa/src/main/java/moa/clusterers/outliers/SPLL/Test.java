package moa.clusterers.outliers.SPLL;

/**
 * @author wfaithfull
 * 
 * Test class for SPLL.
 */
public class Test {
	
	public static void main(String[] args) {
		
		ClusterProvider kmeans = new KMeansAdapter();
		
		SPLL logLL = new SPLL(kmeans, null);
	}
}
