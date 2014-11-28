package moa.classifiers.core.driftdetection.multivariate;

import moa.options.AbstractOptionHandler;

public abstract class AbstractMultivariateChangeDetector extends AbstractOptionHandler implements
		MultivariateChangeDetector {
	
	/**
	 * UID for serialisation
	 */
	private static final long serialVersionUID = -5258329674360365476L;
	
	protected boolean isChangeDetected;
	protected boolean isWarningZone;
	protected double estimation;
	protected double delay;
	protected boolean isInitialized;
	
	@Override
	public boolean getChange() {
		return this.isChangeDetected;
	}
	
	@Override
	public boolean getWarningZone() {
		return this.isWarningZone;
	}

	@Override
	public double getEstimation() {
		return this.estimation;
	}
	
	@Override
	public double getDelay() {
		return this.delay;
	}
	
	@Override
	public double[] getOutput() {
		return new double[] { this.isChangeDetected ? 1 : 0, this.isWarningZone ? 1 : 0, this.delay, this.estimation };
	}

	@Override
	public abstract void getDescription(StringBuilder sb, int indent);

	@Override
	public void resetLearning() {
		this.isChangeDetected = false;
		this.isWarningZone = false;
		this.estimation = 0.0;
		this.delay = 0.0;
		this.isInitialized = false;
	}

	@Override
	public MultivariateChangeDetector copy() {
		return (MultivariateChangeDetector) super.copy();
	}

}
