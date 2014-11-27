package moa.classifiers.core.driftdetection.multivariate;

import com.yahoo.labs.samoa.instances.Instance;

import moa.options.OptionHandler;

public interface MultivariateChangeDetector extends OptionHandler {


    /**
     * Resets this change detector. It must be similar to starting a new change
     * detector from scratch.
     *
     */
    public void resetLearning();

    /**
     * Adding a numeric value to the change detector<br><br>
     *
     * The output of the change detector is modified after the insertion of a
     * new item inside.
     *
     * @param inputValue the number to insert into the change detector
     */
    public void input(Instance inputValue);

    /**
     * Gets whether there is change detected.
     *
     * @return true if there is change
     */
    public boolean getChange();

    /**
     * Gets whether the change detector is in the warning zone, after a warning alert and before a change alert.
     *
     * @return true if the change detector is in the warning zone
     */
    public boolean getWarningZone();

    /**
     * Gets the prediction of next values.
     *
     * @return a prediction of the next value
     */
    public double getEstimation();

    /**
     * Gets the length of the delay in the change detected.
     *
     * @return he length of the delay in the change detected
     */
    public double getDelay();

    /**
     * Gets the output state of the change detection.
     *
     * @return an array with the number of change detections, number of
     * warnings, delay, and estimation.
     */
    public double[] getOutput();

    /**
     * Returns a string representation of the model.
     *
     * @param out	the stringbuilder to add the description
     * @param indent	the number of characters to indent
     */
    @Override
    public void getDescription(StringBuilder sb, int indent);

    /**
     * Produces a copy of this drift detection method
     *
     * @return the copy of this drift detection method
     */
    @Override
    public MultivariateChangeDetector copy();
}
