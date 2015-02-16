package moa.classifiers.core.driftdetection.multivariate;

import java.util.Collection;

import moa.classifiers.core.driftdetection.multivariate.SPLL.MultivariateSlidingWindowPair;
import moa.classifiers.core.driftdetection.multivariate.SPLL.MultivariateSlidingWindowPair.WindowStrategy;
import moa.classifiers.core.driftdetection.multivariate.SPLL.SPLL;
import moa.classifiers.core.driftdetection.multivariate.SPLL.SPLL.LikelihoodResult;
import moa.core.ObjectRepository;
import moa.tasks.TaskMonitor;

import com.yahoo.labs.samoa.instances.Instance;


/**
 * Semi Parametric Log Likelihood change detector conforming to MOA style.
 * 
 * @author Will Faithfull (w.faithfull@bangor.ac.uk)
 * @see {@link moa.classifiers.core.driftdetection.multivariate.SPLL}
 */
public class SPLLDetector extends AbstractMultivariateChangeDetector {

	/**
	 * UID for serialisation.
	 */
	private static final long serialVersionUID = -6462770156931895806L;
	
	private SPLL detector;
	private MultivariateSlidingWindowPair windows;

	public SPLLDetector()
	{
		this(new SPLL());
	}
	
	public SPLLDetector(SPLL detector)
	{
		this(detector, 50);
	}
	
	public SPLLDetector(SPLL detector, int windowSize)
	{
		this.detector = detector;
		this.windows = new MultivariateSlidingWindowPair(WindowStrategy.TOGETHER, windowSize);
	}
	
	@Override
	public void input(Instance inputValue) {
		windows.update(inputValue);
		
		if(windows.enoughObservations()) {
			// TODO: detect change
			double[][] w1 = instancesTo2DArray(windows.getW1());
			double[][] w2 = instancesTo2DArray(windows.getW2());
			
			LikelihoodResult lr = detector.testChange(w1, w2);
			this.isChangeDetected = lr.change;
		}
	}
	
	private static double[][] instancesTo2DArray(Collection<Instance> instances) {
		
		Instance[] intermediate = instances.toArray(new Instance[instances.size()]);
		int nObsv = intermediate.length;
		int nAttr = intermediate[0].numAttributes();
		
		double[][] array = new double[nObsv][nAttr];
		
		for(int i=0; i<nObsv; i++) {
			array[i] = intermediate[i].toDoubleArray();
		}
		
		return array;
	}
	
	@Override
	public void getDescription(StringBuilder sb, int indent) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void prepareForUseImpl(TaskMonitor monitor,
			ObjectRepository repository) {
		// TODO Auto-generated method stub

	}

}
