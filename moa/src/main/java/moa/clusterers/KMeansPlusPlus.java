package moa.clusterers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import moa.cluster.Cluster;
import moa.cluster.InstanceRetainingCluster;
import moa.distance.DistanceMeasure;
import moa.distance.EuclideanDistance;

import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;

/**
 * Clustering algorithm based on David Arthur and Sergei Vassilvitski k-means++ algorithm.
 * 
 * Adapted from implementation in Apache Commons Math.
 * 
 * @author Will Faithfull (w.faithfull@bangor.ac.uk)
 *
 * @param <T> The type of data point to be clustered, derived from SAMOA Instance.
 * @see <a href="http://en.wikipedia.org/wiki/K-means%2B%2B">K-means++ (wikipedia)</a>
 * @see <a href="http://bit.ly/1xMCTcs">Apache Commons Source</a>
 */
public class KMeansPlusPlus<T extends Instance> {
	
	/** Strategies to use for replacing an empty cluster. */
    public static enum EmptyClusterStrategy {

        /** Split the cluster with largest distance variance. */
        LARGEST_VARIANCE,

        /** Split the cluster with largest number of points. */
        LARGEST_POINTS_NUMBER,

        /** Create a cluster around the point farthest from its centroid. */
        FARTHEST_POINT,

        /** Generate an error. */
        ERROR

    }
    
    private EmptyClusterStrategy emptyStrategy;
	
	private final int k;
	
	private final int maxIterations;
	
	private DistanceMeasure<T> measure;
	
	private final Random random;
	
	public KMeansPlusPlus(final int k) {
		this(k, -1);
	}
	
	public KMeansPlusPlus(final int k, final int maxIterations) {
		this(k, maxIterations, new EuclideanDistance<T>());
	}
	
	public KMeansPlusPlus(final int k, final int maxIterations, final DistanceMeasure<T> measure) {
		this.k 				= k;
		this.maxIterations 	= maxIterations;
		this.measure 		= measure;
		
		random = new Random();
		emptyStrategy = EmptyClusterStrategy.FARTHEST_POINT;
	}
	
	public List<InstanceRetainingCluster> cluster(final Collection<T> points) throws IllegalArgumentException, Exception {
		if(points.isEmpty())
			throw new IllegalArgumentException("Cannot cluster an empty set.");
		
		if(points.size() < k)
			throw new IllegalArgumentException("The number of points must be greater than K.");
		
		List<InstanceRetainingCluster> clusters = chooseInitialCenters(points);
		
		int[] assignments = new int[points.size()];
		
		assignPointsToClusters(clusters, points, assignments);
		
		// iterate through updating the centers until we're done
        final int max = (maxIterations < 0) ? Integer.MAX_VALUE : maxIterations;
        for (int count = 0; count < max; count++) {
            boolean emptyCluster = false;
            List<InstanceRetainingCluster> newClusters = new ArrayList<InstanceRetainingCluster>();
            for (final InstanceRetainingCluster cluster : clusters) {
                final Instance newCenter;
                if (cluster.getInstances().isEmpty()) {
                    switch (emptyStrategy) {
                        case LARGEST_POINTS_NUMBER :
                            newCenter = getPointFromLargestNumberCluster(clusters);
                            break;
                        case FARTHEST_POINT :
                            newCenter = getFarthestPoint(clusters);
                            break;
                        default :
                            throw new Exception("Empty Cluster");
                    }
                    emptyCluster = true;
                } else {
                    newCenter = centroidOf((Collection<T>)cluster.getInstances(), cluster.getCenterAsInstance().toDoubleArray().length);
                }
                newClusters.add(new InstanceRetainingCluster(newCenter));
            }
            int changes = assignPointsToClusters(newClusters, points, assignments);
            clusters = newClusters;

            // if there were no more changes in the point-to-cluster assignment
            // and there are no empty clusters left, return the current clusters
            if (changes == 0 && !emptyCluster) {
                return clusters;
            }
        }
		
		return clusters;
	}
	
	/**
     * Adds the given points to the closest {@link Cluster}.
     *
     * @param clusters the {@link Cluster}s to add the points to
     * @param points the points to add to the given {@link Cluster}s
     * @param assignments points assignments to clusters
     * @return the number of points assigned to different clusters as the iteration before
     */
    private int assignPointsToClusters(final List<InstanceRetainingCluster> clusters,
                                       final Collection<T> points,
                                       final int[] assignments) {
        int assignedDifferently = 0;
        int pointIndex = 0;
        for (final T p : points) {
            int clusterIndex = getNearestCluster(clusters, p);
            if (clusterIndex != assignments[pointIndex]) {
                assignedDifferently++;
            }

            InstanceRetainingCluster cluster = clusters.get(clusterIndex);
            cluster.addInstance(p);
            assignments[pointIndex++] = clusterIndex;
        }

        return assignedDifferently;
    }
	
	private List<InstanceRetainingCluster> chooseInitialCenters(final Collection<T> points) {
		
		final List<T> pointList = Collections.unmodifiableList(new ArrayList<T>(points));
		
		final int numPoints = pointList.size();
		
		final boolean[] taken = new boolean[numPoints];
		
		final List<InstanceRetainingCluster> resultSet = new ArrayList<InstanceRetainingCluster>();
		
		final int firstPointIndex = random.nextInt(numPoints);
		
		final T firstPoint = pointList.get(firstPointIndex);
		
		resultSet.add(new InstanceRetainingCluster(firstPoint));
		
		taken[firstPointIndex] = true;
		
		final double[] minDistSquared = new double[numPoints];
		
		for(int i = 0; i < numPoints; i++) {
			if(i != firstPointIndex) {
				double d = measure.compute(firstPoint, pointList.get(i));
				minDistSquared[i] = d*d;
			}
		}
		
		while(resultSet.size() < k) {
			double distSqSum = 0.0;
			
			for(int i = 0; i < numPoints; i++) {
				if(!taken[i]) {
					distSqSum += minDistSquared[i];
				}
			}
			
			final double r = random.nextDouble() * distSqSum;
			
			int nextPointIndex = -1;
			
			double sum = 0.0;
			for(int i = 0; i < numPoints; i++) {
				if(!taken[i]) {
					sum += minDistSquared[i];
					if(sum >= r) {
						nextPointIndex = i;
						break;
					}
				}
			}
			
			if(nextPointIndex == -1) {
				for (int i = numPoints - 1; i >= 0; i--) {
					if(!taken[i]) {
						nextPointIndex = i;
						break;
					}
				}
			}
			
			if(nextPointIndex >= 0) {
				final T p = pointList.get(nextPointIndex);
				
				resultSet.add(new InstanceRetainingCluster(p));
				
				taken[nextPointIndex] = true;
				
				if(resultSet.size() < k) {
					for (int j = 0; j < numPoints; j++) {
                        // Only have to worry about the points still not taken.
                        if (!taken[j]) {
                            double d = measure.compute(p, pointList.get(j));
                            double d2 = d * d;
                            if (d2 < minDistSquared[j]) {
                                minDistSquared[j] = d2;
                            }
                        }
                    }
				}
			} else {
                // None found --
                // Break from the while loop to prevent
                // an infinite loop.
                break;
            }
			
		}
		return resultSet;
	}
	
	
	/**
     * Get a random point from the {@link Cluster} with the largest number of points
     *
     * @param clusters the {@link Cluster}s to search
     * @return a random point from the selected cluster
     * @throws ConvergenceException if clusters are all empty
     */
    private T getPointFromLargestNumberCluster(final Collection<InstanceRetainingCluster> clusters)
            throws Exception {

        int maxNumber = 0;
        InstanceRetainingCluster selected = null;
        for (final InstanceRetainingCluster cluster : clusters) {

            // get the number of points of the current cluster
            final int number = cluster.getInstances().size();

            // select the cluster with the largest number of points
            if (number > maxNumber) {
                maxNumber = number;
                selected = cluster;
            }

        }

        // did we find at least one non-empty cluster ?
        if (selected == null) {
            throw new Exception("Empty Cluster");
        }

        // extract a random point from the cluster
        final List<T> selectedPoints = (List<T>)selected.getInstances();
        return selectedPoints.remove(random.nextInt(selectedPoints.size()));

    }

    /**
     * Get the point farthest to its cluster center
     *
     * @param clusters the {@link Cluster}s to search
     * @return point farthest to its cluster center
     * @throws ConvergenceException if clusters are all empty
     */
    private T getFarthestPoint(final Collection<InstanceRetainingCluster> clusters) throws Exception {

        double maxDistance = Double.NEGATIVE_INFINITY;
        InstanceRetainingCluster selectedCluster = null;
        int selectedPoint = -1;
        for (final InstanceRetainingCluster cluster : clusters) {

            // get the farthest point
            final Instance center = cluster.getCenterAsInstance();
            final List<T> points = (List<T>)cluster.getInstances();
            for (int i = 0; i < points.size(); ++i) {
                final double distance = measure.compute(points.get(i), (T)center);
                if (distance > maxDistance) {
                    maxDistance     = distance;
                    selectedCluster = cluster;
                    selectedPoint   = i;
                }
            }

        }

        // did we find at least one non-empty cluster ?
        if (selectedCluster == null) {
            throw new Exception("Empty cluster");
        }

        return (T)selectedCluster.getInstances().remove(selectedPoint);

    }
	
	/**
     * Returns the nearest {@link Cluster} to the given point
     *
     * @param clusters the {@link Cluster}s to search
     * @param point the point to find the nearest {@link Cluster} for
     * @return the index of the nearest {@link Cluster} to the given point
     */
    private int getNearestCluster(final Collection<InstanceRetainingCluster> clusters, final T point) {
        double minDistance = Double.MAX_VALUE;
        int clusterIndex = 0;
        int minCluster = 0;
        for (final InstanceRetainingCluster c : clusters) {
            final double distance = measure.compute(point, (T)c.getCenterAsInstance());
            if (distance < minDistance) {
                minDistance = distance;
                minCluster = clusterIndex;
            }
            clusterIndex++;
        }
        return minCluster;
    }
    
    /**
     * Computes the centroid for a set of points.
     *
     * @param points the set of points
     * @param dimension the point dimension
     * @return the computed centroid for the set of points
     */
    private Instance centroidOf(final Collection<T> points, final int dimension) {
        final double[] centroid = new double[dimension];
        for (final T p : points) {
            final double[] point = p.toDoubleArray();
            for (int i = 0; i < centroid.length; i++) {
                centroid[i] += point[i];
            }
        }
        for (int i = 0; i < centroid.length; i++) {
            centroid[i] /= points.size();
        }
        return new DenseInstance(1.0, centroid);
    }
}
