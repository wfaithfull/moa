package moa.cluster;
import moa.cluster.Cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;

/**
 * Created by Will Faithfull on 14/10/14.
 */
public class InstanceRetainingCluster extends Cluster {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8617526568515076404L;
	private List<Instance> _instances;
    private Instance _center;

    public InstanceRetainingCluster() {
        this(new ArrayList<Instance>());
    }
    
    public InstanceRetainingCluster(Instance center) {
    	_instances = new ArrayList<Instance>();
    	_center = center;
    	
    }

    public InstanceRetainingCluster(List<Instance> instances) {
        _instances = instances;

        int nAttr = instances.get(0).numAttributes();

        double[] center = new double[nAttr];
        for(Instance i : instances) {
            double[] instance = i.toDoubleArray();

            for(int j=0;j<nAttr;j++) {
                center[j] += instance[j];
            }
        }

        for(int i=0;i<nAttr;i++) {
            center[i] = center[i] / nAttr;
        }

        _center = new DenseInstance(1.0d, center);
    }

    public List<Instance> getInstances() {
        return _instances;
    }

    public void addInstance(Instance instance) {
        _instances.add(instance);
    }

    @Override
    public double[] getCenter() {
        return _center.toDoubleArray();
    }
    
    public Instance getCenterAsInstance() {
    	return _center;
    }

    @Override
    public double getWeight() {
        return _instances.size();
    }

    @Override
    public Instance sample(Random random) {
        return _instances.get(random.nextInt(_instances.size()));
    }

	@Override
	public double getInclusionProbability(Instance instance) {
		// TODO Auto-generated method stub
		return 0;
	}
}
