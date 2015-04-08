package moa.streams.generators.cd.multivariate;

import moa.options.ClassOption;

import com.github.javacliparser.ListOption;
import com.github.javacliparser.Option;

import moa.options.OptionHandler;
import moa.streams.generators.cd.AbstractConceptDriftGenerator;
import moa.streams.generators.cd.ConceptDriftGenerator;

/*
 * MultivariateDriftGeneratorEnsemble
 * 
 * This is a configurable ensemble of drift generation techniques, so that we can recreate tricky
 * problems with individually drifting features, features drifting at different rates, wildly
 * different means, standard deviation, distribution of the individual features, etc.
 */
public class MultivariateDriftGeneratorEnsemble extends AbstractMultivariateConceptDriftGenerator {

	private static final long serialVersionUID = 2360124636284627056L;
	private AbstractConceptDriftGenerator[] ensemble;
	
	public ListOption driftGeneratorsOption = new ListOption("generators", 'g',
            "Generator(s) to use.", new ClassOption("generator", 'c',
	            "Drift generation method to use.", ConceptDriftGenerator.class, "NoChangeGenerator"),
	            new Option[0], 
	            ',');
	
	@Override
	protected void prepareForUseImpl(moa.tasks.TaskMonitor monitor, moa.core.ObjectRepository repository) {
		
		super.prepareForUseImpl(monitor, repository);
		
		Option[] driftGenerators = this.driftGeneratorsOption.getList();
		
		// I would like to dynamically update the features option based on the number of generators,
		// but this is somewhat impractical.
		
		AbstractConceptDriftGenerator[] generators = new AbstractConceptDriftGenerator[driftGenerators.length];
		
		for(int i=0; i<driftGenerators.length; i++) {
			// This is rather inelegant, to put it lightly.
			
			//generators[i] = ((AbstractConceptDriftGenerator)(this.getPreparedClassOption((ClassOption)driftGenerators[i]));
			generators[i] = ((AbstractConceptDriftGenerator)((ClassOption)driftGenerators[i]).materializeObject(monitor, repository));
			
			if(monitor.taskShouldAbort()) {
				return;
			}
			
			if(generators[i] instanceof OptionHandler) {
				monitor.setCurrentActivity("Preparing drift generator " + (i+1) + "...", -1.0);
			}
		}
		
		int nFeatures = this.numFeaturesOption.getValue();
		int nGenerators = generators.length;
		
		
		if(nFeatures < nGenerators) {
			String message = String.format("Generators: [%d]\t Features [%d]", driftGenerators.length, this.numFeaturesOption.getValue());
			throw new IllegalArgumentException(message + "\nThere must be at least as many features as specified generators");
		}
		else if(nFeatures > nGenerators){
			ensemble = padEnsemble(generators, nFeatures);
		}
		else{
			ensemble = generators;
		}

	}
	
	private AbstractConceptDriftGenerator[] padEnsemble(AbstractConceptDriftGenerator[] driftGenerators, int nFeatures) {
		
		// In the event that we have more requested features than generators, this method pads out the ensemble.
		AbstractConceptDriftGenerator[] generatorEnsemble = new AbstractConceptDriftGenerator[nFeatures];
		
		for(int i=0,j=0;i<nFeatures;i++,j++) {
			if(j >= driftGenerators.length)
				j=0;
			generatorEnsemble[i] = driftGenerators[j];
			generatorEnsemble[i].prepareForUse();
		}
		
		return generatorEnsemble;
	}
	
	@Override
	protected double[] nextValues() {
		
		int dimensions = this.numFeaturesOption.getValue();
		
		double[] values = new double[dimensions];
		
		// Grab the values from the respective drift generators
		for(int i=0;i<dimensions;i++) {
			values[i] = this.ensemble[i].nextInstance().getData().value(0);
			this.featureWiseChange[i] = this.ensemble[i].getChange();
		}
		
		return values;
	}

}
