package moa.classifiers.core.driftdetection.multivariate;

import java.util.PriorityQueue;
import java.util.Queue;

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
	
	private Queue<Instance> window_1;
	private Queue<Instance> window_2;

	
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
		this.window_2 = new PriorityQueue<Instance>();
		this.window_1 = new PriorityQueue<Instance>();
	}
	
	@Override
	public void input(Instance inputValue) {
		updateBothWindows(inputValue);
		
		if(enoughObservations()) {
			// TODO: detect change
		}
	}
	
	private boolean enoughObservations() {
		return window_1.size() == windowSize && window_2.size() == windowSize;
	}
	
	private void updateBothWindows(Instance inputValue)
	{
		/*
		 * Pair of sliding windows, W1 & W2
		 * |-------[====][====]-------]
		 * 			 W1	   W2
		 * 
		 * Move together such that the last observation of W2
		 * will be the next observation of W1.
		 */
		
		// If W1 is full, remove oldest observation
		if(window_1.size() >= windowSize) {
			window_1.remove();
		}
		
		// If W2 is full, move oldest observation to W1
		if(window_2.size() >= windowSize) {
			window_1.add(window_2.remove());
		}
		
		// W2 is cutting edge
		window_2.add(inputValue);
	}

	private void updateWindow2(Instance inputValue) {
		
		/*
		 * Pair of sliding windows, W1 & W2
		 * |[====]-------[====]-------]
		 * 	  W1		   W2
		 * 
		 * Move independently, W1 stays put until change is detected.
		 */
		
		// TODO: Implement static window.
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
