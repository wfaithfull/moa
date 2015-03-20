package moa.streams.generators.cd.multivariate;

import java.util.ArrayList;
import java.util.Random;

import moa.core.FastVector;
import moa.core.InstanceExample;
import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.options.ClassOption;
import moa.streams.InstanceStream;
import moa.streams.clustering.ClusterEvent;
import moa.streams.generators.cd.ConceptDriftGenerator;
import moa.tasks.TaskMonitor;

import com.github.javacliparser.FlagOption;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.ListOption;
import com.github.javacliparser.Option;
import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;

/*
 * AbstractMultivariateConceptDriftGenerator.java
 * 
 * Apologies for the mouthful of a class name, but this is Java after all.
 * 
 * Multivariate extension of the equivalent AbstractConceptDriftGenerator.
 * @see AbstractConceptDriftGenerator
 * @author Will Faithfull (w.faithfull@bangor.ac.uk) (will@sourcepulp.com)
 */
public abstract class AbstractMultivariateConceptDriftGenerator extends AbstractOptionHandler implements ConceptDriftGenerator  {

	private static final long serialVersionUID = -7543824511173863017L;

	@Override
	public String getPurposeString() {
		return "Generates a multivariate stream problem of detecting concept drift";
	}
	
	public ArrayList<ClusterEvent> clusterEvents;
	
	@Override
	public ArrayList<ClusterEvent> getEventsList()
	{
		return this.clusterEvents;
	}
	
    public IntOption instanceRandomSeedOption = new IntOption(
            "instanceRandomSeed", 'i',
            "Seed for random generation of instances.", 1);
	

	public FlagOption notBinaryStreamOption = new FlagOption("notBinaryStream", 'b',
			"Don't convert to a binary stream of 0 and 1.");

	public IntOption numInstancesConceptOption = new IntOption("numInstancesConcept", 'p',
			"The number of instances for each concept.", 500, 0, Integer.MAX_VALUE);
	
	public IntOption numFeaturesOption = new IntOption("numFeatures", 'f',
			"The dimensionality of this stream", 1, 1, Integer.MAX_VALUE);
	
	protected InstancesHeader streamHeader;
	
	protected Random instanceRandom;
	
	protected int period;
	
	protected int numInstances;
	
	protected boolean change;
	
	protected boolean[] featureWiseChange;
	
	@Override
	protected void prepareForUseImpl(TaskMonitor monitor,
			ObjectRepository repository) {
		this.numInstances = 0;
		this.period = numInstancesConceptOption.getValue();
		
		// generate header
        FastVector attributes = new FastVector();

        FastVector binaryLabels = new FastVector();
        binaryLabels.addElement("0");
        binaryLabels.addElement("1");

        for(int i=0;i<this.numFeaturesOption.getValue();i++) {
            if (!this.notBinaryStreamOption.isSet()) {
                attributes.addElement(new Attribute("feature_" + i, binaryLabels));
            } else {
                attributes.addElement(new Attribute("feature_" + i));
            }               
        }
        // Ground Truth
        attributes.addElement(new Attribute("change", binaryLabels));
        attributes.addElement(new Attribute("ground truth input"));

        this.streamHeader = new InstancesHeader(new Instances(
                getCLICreationString(InstanceStream.class), attributes, 0));
        this.streamHeader.setClassIndex(this.streamHeader.numAttributes() - 1);

	}
	

    public long estimatedRemainingInstances() {
        return -1;
    }

    public InstancesHeader getHeader() {
        return this.streamHeader;
    }

    public boolean hasMoreInstances() {
        return true;
    }

    public boolean isRestartable() {
        return true;
    }
	
	protected abstract double[] nextValues();
	
	private double[] nextBinaryValues(double[] nums) {
		double[] values = new double[nums.length];
		for(int i=0;i<nums.length;i++) {
			values[i] = this.instanceRandom.nextDouble() <= nums[i] ? 0 : 1;
		}
		
		return values;
	}
	
	private void setValues(Instance inst, double[] values) {
		for(int i=0;i<inst.numAttributes();i++) {
			inst.setValue(i, values[i]);
		}
	}
	
	public InstanceExample nextInstance() {
        this.numInstances++;
        InstancesHeader header = getHeader();
        Instance inst = new DenseInstance(header.numAttributes());
        inst.setDataset(header);
        double[] nextValues = this.nextValues();
        if (this.notBinaryStreamOption.isSet()) {
        	this.setValues(inst, nextValues);
        } else {
        	this.setValues(inst, this.nextBinaryValues(nextValues));
        }
        // Ground truth
        for(boolean gt : this.featureWiseChange) {
        	// TODO: this may be naive, or deserve to be abstracted into
        	// TODO: a strategy pattern. 
        	if(gt) {
        		this.change = true;
        		break;
        	}
        }
        inst.setValue(nextValues.length + 1, this.getChange() ? 1 : 0);
        if (this.getChange() == true) {
            //this.clusterEvents.add(new ClusterEvent(this, this.numInstances, "Change", "Drift"));
        }

        return new InstanceExample(inst);
	}
	
	public boolean getChange() {
		return this.change;
	}

    public void restart() {
        this.instanceRandom = new Random(this.instanceRandomSeedOption.getValue());
    }

    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
    }
}
