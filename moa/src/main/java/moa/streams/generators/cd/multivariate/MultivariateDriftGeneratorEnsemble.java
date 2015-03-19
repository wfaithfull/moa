package moa.streams.generators.cd.multivariate;

import moa.options.ClassOption;
import com.github.javacliparser.ListOption;
import com.github.javacliparser.Option;

import moa.options.OptionHandler;
import moa.streams.generators.cd.ConceptDriftGenerator;

public class MultivariateDriftGeneratorEnsemble extends AbstractMultivariateConceptDriftGenerator {

	private static final long serialVersionUID = 2360124636284627056L;
	private ConceptDriftGenerator[] ensemble;
	private int dimensions;
	
	public ListOption driftGeneratorsOption = new ListOption("generators", 'g',
            "Generator(s) to use.", new ClassOption("driftgen", 'c',
	            "Drift generation method to use.", ConceptDriftGenerator.class, "NoChangeGenerator"),
	            new Option[0], 
	            ',');
	
	
	public MultivariateDriftGeneratorEnsemble() {
		
	}
	
	public MultivariateDriftGeneratorEnsemble(ConceptDriftGenerator... generators) {
		this(generators.length, generators);
	}
	
	public MultivariateDriftGeneratorEnsemble(int dimensions, ConceptDriftGenerator... generators) {
		if(generators.length == 0) {
			throw new IllegalArgumentException("Please provide at least one concept drift generator");
		}
		
		this.ensemble = generators;
		this.dimensions = dimensions;
	}
	
	@Override
	protected void prepareForUseImpl(moa.tasks.TaskMonitor monitor, moa.core.ObjectRepository repository) {
		Option[] driftGenerators = this.driftGeneratorsOption.getList();
		ensemble = new ConceptDriftGenerator[driftGenerators.length];
		
		for(int i=0; i<driftGenerators.length; i++) {
			// This is rather inelegant, to put it lightly.
			ensemble[i] = ((ConceptDriftGenerator)((ClassOption)driftGenerators[i]).materializeObject(monitor, repository));
			
			if(monitor.taskShouldAbort()) {
				return;
			}
			
			if(ensemble[i] instanceof OptionHandler) {
				monitor.setCurrentActivity("Preparing drift generator " + (i+1) + "...", -1.0);
			}
		}
	}
	
	@Override
	protected double[] nextValues() {
		double[] values = new double[dimensions];
		
		// Grab the values from the respective drift generators
		for(int i=0;i<dimensions;i++) {
			values[i] = this.ensemble[i].nextInstance().getData().value(0);
		}
		
		return values;
	}

}
