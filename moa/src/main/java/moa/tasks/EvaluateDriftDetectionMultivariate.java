package moa.tasks;

import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;

import moa.classifiers.core.driftdetection.multivariate.MultivariateChangeDetector;
import moa.core.Example;
import moa.core.ObjectRepository;
import moa.evaluation.LearningPerformanceEvaluator;
import moa.options.ClassOption;
import moa.streams.InstanceStream;
import moa.streams.generators.cd.ConceptDriftGenerator;
import moa.streams.generators.cd.multivariate.AbstractMultivariateConceptDriftGenerator;

public class EvaluateDriftDetectionMultivariate extends ConceptDriftMainTask {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -966207978036771732L;

	@Override
    public String getPurposeString() {
        return "Evaluates a drift detection method on a stream by presenting each example in sequence.";
    }

    public ClassOption ddmOption = new ClassOption("ddm", 'd',
            "Drift detection method to evaluate on stream", MultivariateChangeDetector.class, "SPLLDetector");

    public ClassOption streamOption = new ClassOption("stream", 's',
            "Stream to detect concept drift in.", AbstractMultivariateConceptDriftGenerator.class,
            "MultivariateDriftGeneratorEnsemble");

    public ClassOption evaluatorOption = new ClassOption("evaluator", 'e',
            "Classification performance evaluation method.",
            LearningPerformanceEvaluator.class,
            "BasicConceptDriftPerformanceEvaluator");
    
    public IntOption instanceLimitOption = new IntOption("numInstances", 'n', "The number of instances to generate", 1000);
    
	@Override
	public Class<?> getTaskResultType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object doMainTask(TaskMonitor monitor, ObjectRepository repository) {
		
		this.prepareClassOptions(monitor, repository);
		
		// Materialise class options
		MultivariateChangeDetector cd 				= (MultivariateChangeDetector) this.getPreparedClassOption(ddmOption);
		ConceptDriftGenerator stream 				= (ConceptDriftGenerator) this.getPreparedClassOption(streamOption);
		LearningPerformanceEvaluator<?> evaluator 	= (LearningPerformanceEvaluator<?>) this.getPreparedClassOption(evaluatorOption);
		
		int count = 0;
		while(count++ < instanceLimitOption.getValue()) {
			// TODO: Set monitor
			Example<Instance> next = stream.nextInstance();
			cd.input(next.getData());
			// TODO: Evaluator
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
