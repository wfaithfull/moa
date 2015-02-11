package moa.classifiers.core.driftdetection.multivariate;

import java.util.PriorityQueue;
import java.util.Queue;

import moa.classifiers.core.driftdetection.multivariate.SPLL.SPLL;
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
	
	private Queue<Instance> window;
	
	private int windowSize;

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
		this.windowSize = windowSize;
		this.window = new PriorityQueue<Instance>();
	}
	
	@Override
	public void input(Instance inputValue) {
		// TODO Auto-generated method stub
		window.add(inputValue);

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
