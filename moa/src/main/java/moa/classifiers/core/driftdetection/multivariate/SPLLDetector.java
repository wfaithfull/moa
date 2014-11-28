package moa.classifiers.core.driftdetection.multivariate;

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

	@Override
	public void input(Instance inputValue) {
		// TODO Auto-generated method stub

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
