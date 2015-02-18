package moa.streams.generators.cd.multivariate;

import java.util.ArrayList;
import java.util.Random;

import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.options.ClassOption;
import moa.streams.clustering.ClusterEvent;
import moa.streams.generators.cd.ConceptDriftGenerator;
import moa.tasks.TaskMonitor;

import com.github.javacliparser.FlagOption;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.ListOption;
import com.github.javacliparser.Option;
import com.yahoo.labs.samoa.instances.InstancesHeader;

/*
 * AbstractMultivariateConceptDriftGenerator.java
 * 
 * Apologies for the mouthful of a class name, but this is Java after all.
 * 
 * Multivariate extension of the equivalent AbstractConceptDriftGenerator. Essentially this
 * is a configurable ensemble of drift generation techniques, so that we can recreate tricky
 * problems with individually drifting features, features drifting at different rates, wildly
 * different means, standard deviation, distribution of the individual features, etc.
 * @see AbstractConceptDriftGenerator
 * @author Will Faithfull (w.faithfull@bangor.ac.uk)
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
	
	public ListOption changeDetectorsOption = new ListOption("generators", 'g',
            "Generator(s) to use.", new ClassOption("ConceptDriftGenerator", 'g',
	            "Drift generation method to use.", ConceptDriftGenerator.class, "NoChangeGenerator"),
	            new Option[0], ',');
	
	public FlagOption notBinaryStreamOption = new FlagOption("notBinaryStream", 'b',
			"Don't convert to a binary stream of 0 and 1.");

	public IntOption numInstancesConceptOption = new IntOption("numInstancesConcept", 'p',
			"The number of instances for each concept.", 500, 0, Integer.MAX_VALUE);
	
	protected InstancesHeader streamHeader;
	
	protected Random instanceRandom;
	
	protected int period;
	
	protected int numInstances;
	
	protected boolean change;
	
	@Override
	protected void prepareForUseImpl(TaskMonitor monitor,
			ObjectRepository repository) {
		this.numInstances = 0;
		this.period = numInstancesConceptOption.getValue();
	}
}
