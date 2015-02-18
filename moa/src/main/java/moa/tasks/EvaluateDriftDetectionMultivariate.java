package moa.tasks;

import moa.classifiers.core.driftdetection.multivariate.MultivariateChangeDetector;
import moa.core.ObjectRepository;
import moa.evaluation.LearningPerformanceEvaluator;
import moa.options.ClassOption;
import moa.streams.InstanceStream;

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
            "Stream to detect concept drift in.", InstanceStream.class,
            "ArffFileStream");

    public ClassOption evaluatorOption = new ClassOption("evaluator", 'e',
            "Classification performance evaluation method.",
            LearningPerformanceEvaluator.class,
            "BasicConceptDriftPerformanceEvaluator");
	
	@Override
	public Class<?> getTaskResultType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object doMainTask(TaskMonitor monitor, ObjectRepository repository) {
		// TODO Auto-generated method stub
		return null;
	}

}
