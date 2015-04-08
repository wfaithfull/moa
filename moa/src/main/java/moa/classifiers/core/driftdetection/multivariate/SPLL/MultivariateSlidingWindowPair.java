package moa.classifiers.core.driftdetection.multivariate.SPLL;

import java.io.Serializable;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.yahoo.labs.samoa.instances.Instance;

public class MultivariateSlidingWindowPair implements Serializable {

	private static final long serialVersionUID = 211852219984361142L;
	private Queue<Instance> window_1;
	private Queue<Instance> window_2;
	
	private WindowStrategy strategy;
	private int windowSize;
	
	public MultivariateSlidingWindowPair(WindowStrategy strategy, int windowSize) {
		this.setStrategy(strategy);
		this.setWindowSize(windowSize);
		this.window_1 = new ArrayBlockingQueue<Instance>(windowSize);
		this.window_2 = new ArrayBlockingQueue<Instance>(windowSize);
	}
	
	public enum WindowStrategy {
		TOGETHER, 
		FIXED_UNTIL_CHANGE
	}
	
	public void update(Instance inputValue) {
		switch(getStrategy()) {
			case TOGETHER 			: together(inputValue);
			case FIXED_UNTIL_CHANGE	: fixedUntilChange(inputValue);
		}
	}
	
	public void resetFixedWindow() {
		if(this.getStrategy() != WindowStrategy.FIXED_UNTIL_CHANGE)
			return;
		
		// TODO: reset fixed window
	}
	
	private void together(Instance inputValue)
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
		if(window_1.size() >= getWindowSize()) {
			window_1.remove();
		}
		
		// If W2 is full, move oldest observation to W1
		if(window_2.size() >= getWindowSize()) {
			window_1.add(window_2.remove());
		}
		
		// W2 is cutting edge
		window_2.add(inputValue);
	}
	
	private void fixedUntilChange(Instance inputValue) {
		
		/*
		 * Pair of sliding windows, W1 & W2
		 * |[====]-------[====]-------]
		 * 	  W1		   W2
		 * 
		 * Move independently, W1 stays put until change is detected.
		 */
		
		// TODO: Implement static window.
	}
	
	public boolean enoughObservations() {
		return window_1.size() == windowSize && window_2.size() == windowSize;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public WindowStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(WindowStrategy strategy) {
		this.strategy = strategy;
	}
	
	public Collection<Instance> getW1() {
		return window_1;
	}
	
	public Collection<Instance> getW2() {
		return window_2;
	}
} 

