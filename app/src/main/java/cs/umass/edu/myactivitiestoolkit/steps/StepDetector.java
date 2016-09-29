package cs.umass.edu.myactivitiestoolkit.steps;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import java.util.ArrayList;

import cs.umass.edu.myactivitiestoolkit.processing.Filter;

/**
 * This class is responsible for detecting steps from the accelerometer sensor.
 * All {@link OnStepListener step listeners} that have been registered will
 * be notified when a step is detected.
 */
public class StepDetector implements SensorEventListener {
    /** Used for debugging purposes. */
    @SuppressWarnings("unused")
    private static final String TAG = StepDetector.class.getName();
    private int count = 0;
    private double average = 0;
    private int side = 0;
    private long latest = 0;

    /** Maintains the set of listeners registered to handle step events. **/
    private ArrayList<OnStepListener> mStepListeners;

    /**
     * The number of steps taken.
     */
    private int stepCount;

    public StepDetector(){
        mStepListeners = new ArrayList<>();
        stepCount = 0;
    }

    /**
     * Registers a step listener for handling step events.
     * @param stepListener defines how step events are handled.
     */
    public void registerOnStepListener(final OnStepListener stepListener){
        mStepListeners.add(stepListener);
    }

    /**
     * Unregisters the specified step listener.
     * @param stepListener the listener to be unregistered. It must already be registered.
     */
    public void unregisterOnStepListener(final OnStepListener stepListener){
        mStepListeners.remove(stepListener);
    }

    /**
     * Unregisters all step listeners.
     */
    public void unregisterOnStepListeners(){
        mStepListeners.clear();
    }

    /**
     * Here is where you will receive accelerometer readings, buffer them if necessary
     * and run your step detection algorithm. When a step is detected, call
     * {@link #onStepDetected(long, float[])} to notify all listeners.
     *
     * Recall that human steps tend to take anywhere between 0.5 and 2 seconds.
     *
     * @param event sensor reading
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //TODO: Implement step detection algorithm here.
            // detectStep will call onStepDetected for us
            // when it determines we've taken a step.
            detectStep(event.timestamp, event.values);
        }
    }

    private void detectStep(long timestamp, float[] filteredValues) {
        // sum of squares of each direction, preserving negatives
        double combined = 0;
        for (float value : filteredValues) {
            combined += (Math.abs(value) * value);
        }
        // Square root of combined, preserving negatives
        if (combined < 0) {
            combined = -1 * (Math.pow(Math.abs(combined), 0.5));
        }
        else {
            combined = Math.pow(combined, 0.5);
        }

        // Multiply the average by the count to add this point to the average
        double newSum = average * count;
        // Increment the count
        count++;
        // Calculate the new average
        average = ((newSum + combined) / count);

        // This initializes the side variable by putting it on one side at first
        if ((combined != average) && side == 0) {
            if (combined > average) {
                side = 1;
            } else {
                side = -1;
            }
        }

        //If the current point is greater than the average and the last one was less
        if ((combined >= average) && (side < 0)) {
            // If the difference between this 0-crossing and the last one is in the step range
            if (((timestamp - latest) < 750) && ((timestamp - latest) > 315)) {
                onStepDetected(timestamp, filteredValues);
            }
            // Update the latest 0-crossing and side
            latest = timestamp;
            side *= (-1);
        }

        // If the current point is less than the average and the last one was greater
        if ((combined <= average) && side > 0) {
            // If the difference between this 0-crossing and the last one is in the step range
            if (((timestamp - latest) < 750) && ((timestamp - latest) > 315)) {
                onStepDetected(timestamp, filteredValues);
            }
            // Update the latest 0-crossing and side
            latest = timestamp;
            side *= (-1);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // do nothing
    }

    /**
     * This method is called when a step is detected. It updates the current step count,
     * notifies all listeners that a step has occurred and also notifies all listeners
     * of the current step count.
     */
    private void onStepDetected(long timestamp, float[] values){
        stepCount++;
        for (OnStepListener stepListener : mStepListeners){
            stepListener.onStepDetected(timestamp, values);
            stepListener.onStepCountUpdated(stepCount);
        }
    }
}
