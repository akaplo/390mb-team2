package cs.umass.edu.myactivitiestoolkit.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import cs.umass.edu.myactivitiestoolkit.R;
import cs.umass.edu.myactivitiestoolkit.ppg.HRSensorReading;
import cs.umass.edu.myactivitiestoolkit.ppg.PPGSensorReading;
import cs.umass.edu.myactivitiestoolkit.constants.Constants;
import cs.umass.edu.myactivitiestoolkit.ppg.HeartRateCameraView;
import cs.umass.edu.myactivitiestoolkit.ppg.PPGEvent;
import cs.umass.edu.myactivitiestoolkit.ppg.PPGListener;
import cs.umass.edu.myactivitiestoolkit.processing.FFT;
import cs.umass.edu.myactivitiestoolkit.processing.Filter;
import cs.umass.edu.myactivitiestoolkit.util.Interpolator;
import edu.umass.cs.MHLClient.client.MobileIOClient;

/**
 * Photoplethysmography service. This service uses a {@link HeartRateCameraView}
 * to collect PPG data using a standard camera with continuous flash. This is where
 * you will do most of your work for this assignment.
 * <br><br>
 * <b>ASSIGNMENT (PHOTOPLETHYSMOGRAPHY)</b> :
 * In {@link #onSensorChanged(PPGEvent)}, you should smooth the PPG reading using
 * a {@link Filter}. You should send the filtered PPG reading both to the server
 * and to the {@link cs.umass.edu.myactivitiestoolkit.view.fragments.HeartRateFragment}
 * for visualization. Then call your heart rate detection algorithm, buffering the
 * readings if necessary, and send the bpm measurement back to the UI.
 * <br><br>
 * EXTRA CREDIT:
 *      Follow the steps outlined <a href="http://www.marcoaltini.com/blog/heart-rate-variability-using-the-phones-camera">here</a>
 *      to acquire a cleaner PPG signal. For additional extra credit, you may also try computing
 *      the heart rate variability from the heart rate, as they do.
 *
 * @author CS390MB
 *
 * @see HeartRateCameraView
 * @see PPGEvent
 * @see PPGListener
 * @see Filter
 * @see MobileIOClient
 * @see PPGSensorReading
 * @see Service
 */
public class PPGService extends SensorService implements PPGListener
{
    @SuppressWarnings("unused")
    /** used for debugging purposes */
    private static final String TAG = PPGService.class.getName();

    /* Surface view responsible for collecting PPG data and displaying the camera preview. */
    private HeartRateCameraView mPPGSensor;

    @Override
    protected void start() {
        Log.d(TAG, "START");
        mPPGSensor = new HeartRateCameraView(getApplicationContext(), null);

        WindowManager winMan = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);

        //surface view dimensions and position specified where service intent is called
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        //display the surface view as a stand-alone window
        winMan.addView(mPPGSensor, params);
        mPPGSensor.setZOrderOnTop(true);

        // only once the surface has been created can we start the PPG sensor
        mPPGSensor.setSurfaceCreatedCallback(new HeartRateCameraView.SurfaceCreatedCallback() {
            @Override
            public void onSurfaceCreated() {
                mPPGSensor.start(); //start recording PPG
            }
        });

        super.start();
    }

    @Override
    protected void onServiceStarted() {
        broadcastMessage(Constants.MESSAGE.PPG_SERVICE_STARTED);
    }

    @Override
    protected void onServiceStopped() {
        if (mPPGSensor != null)
            mPPGSensor.stop();
        if (mPPGSensor != null) {
            ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeView(mPPGSensor);
        }
        broadcastMessage(Constants.MESSAGE.PPG_SERVICE_STOPPED);
    }

    private SensorManager mSensorManager;
    private Sensor ppgSensor;


    @Override
    protected void registerSensors() {
        // TODO: Register a PPG listener with the PPG sensor (mPPGSensor)
        mPPGSensor.registerListener(this);


    }

    @Override
    protected void unregisterSensors() {
        // TODO: Unregister the PPG listener
        mPPGSensor.unregisterListener(this);
    }

    @Override
    protected int getNotificationID() {
        return Constants.NOTIFICATION_ID.PPG_SERVICE;
    }

    @Override
    protected String getNotificationContentText() {
        return getString(R.string.ppg_service_notification);
    }

    @Override
    protected int getNotificationIconResourceID() {
        return R.drawable.ic_whatshot_white_48dp;
    }

    /**
     * This method is called each time a PPG sensor reading is received.
     * <br><br>
     * You should smooth the data using {@link Filter} and then send the filtered data both
     * to the server and the main UI for real-time visualization. Run your algorithm to
     * detect heart beats, calculate your current bpm and send the bmp measurement to the
     * main UI. Additionally, it may be useful for you to send the peaks you detect to
     * the main UI, using {@link #broadcastPeak(long, double)}. The plot is already set up
     * to draw these peak points upon receiving them.
     * <br><br>
     * Also make sure to send your bmp measurement to the server for visualization. You
     * can do this using {@link HRSensorReading}.
     *
     * @param event The PPG sensor reading, wrapping a timestamp and mean red value.
     *
     * @see PPGEvent
     * @see PPGSensorReading
     * @see HeartRateCameraView#onPreviewFrame(byte[], Camera)
     * @see MobileIOClient
     * @see HRSensorReading
     */
    @SuppressWarnings("deprecation")

    public Filter filter = new Filter(10);
    public double[] FValues;
    public float[] filterValues(float[] values){
        FValues = filter.getFilteredValues(values); //returns array of doubles
        float[] ret = new float[FValues.length];
        for(int i=0; i<FValues.length;i++){
            ret[i]=(float)FValues[i];
        }
        return ret;
    }

    @Override
    public void onSensorChanged(PPGEvent event) {
        Log.d(TAG, "Received PPGEvent");
        // TODO: Smooth the signal using a Butterworth / exponential smoothing filter

        // TODO: send the data to the UI fragment for visualization, using broadcastPPGReading(...)
        double [] f = filter.getFilteredValues((float)event.value);
        broadcastPPGReading(event.timestamp, f[0]);


        // TODO: Send the filtered mean red value to the server
        mClient.sendSensorReading(new PPGSensorReading(mUserID, "MOBILE", "", event.timestamp, event.value));
        // TODO: Buffer data if necessary for your algorithm
        // TODO: Call your heart beat and bpm detection algorithm
        BPMdetection(event.timestamp, f[0]);
        // TODO: Send your heart rate estimate to the server
    }

    private double totalValue =0;
    private List<Long> time = new ArrayList<>();
    private List<Double> values = new ArrayList<>();

    public void BPMdetection(long timestamp, double value){
        time.add(timestamp);
        values.add(value);
        totalValue+=value;
        long latest = time.get(0);
        //while the least recent timestamp is over 6 seconds ago
        //remove the least recent timestamp and the corresponding value
        while(latest+6000<timestamp){
            time.remove(0);
            latest = time.get(0);
            totalValue-=values.get(0);
            values.remove(0);
        }
        Log.d("size",""+time.size()+ "  " + values.size());
        //calculating the mean, variance, stdDev
        double mean = totalValue / values.size();
        double temp =0;
        for(int i =0; i<values.size(); i++){
            temp += ((values.get(i)-mean) * (values.get(i)-mean));
        }
        double variance = temp / values.size();
        double stdDev = Math.sqrt(variance);
        //getting the slope roughly by comparing times and values 10 units apart
        //the size of the arrays can vary a lot. It usually starts off staying around 110(size)
        //but can quickly drop to 50 or below
        List<Double> slopes = new ArrayList<>();
        for(int i = values.size(); i>10; i--){
            double slope = values.get(i-10) - values.get(i-1);
            slopes.add(slope / (time.get(i-10) - time.get(i-1)));
        }

        //lastTrue is used to make sure slope values next to each other do not get counted twice for BPM
        int BPM=0;
        boolean lastTrue=false;
        for(int i =1; i<slopes.size(); i++){

            //arbitrary value. Changing it doesn't seem to do much
            if(slopes.get(i)>0.00000025){
                if(!lastTrue) {
                    lastTrue = true;
                    BPM++;
                }
            }
            else{
                lastTrue=false;
            }
        }

        //times 10 because this is over a 6 second window
        broadcastBPM(BPM*10);
        Log.d("BPM",""+BPM);
    }

    /**
     * Broadcasts the PPG reading to other application components, e.g. the main UI.
     * @param ppgReading the mean red value.
     */
    public void broadcastPPGReading(final long timestamp, final double ppgReading) {
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY.PPG_DATA, ppgReading);
        intent.putExtra(Constants.KEY.TIMESTAMP, timestamp);
        intent.setAction(Constants.ACTION.BROADCAST_PPG);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(intent);
    }

    /**
     * Broadcasts the current heart rate in BPM to other application components, e.g. the main UI.
     * @param bpm the current beats per minute measurement.
     */
    public void broadcastBPM(final int bpm) {
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY.HEART_RATE, bpm);
        intent.setAction(Constants.ACTION.BROADCAST_HEART_RATE);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(intent);
    }

    /**
     * Broadcasts the current heart rate in BPM to other application components, e.g. the main UI.
     * @param timestamp the current beats per minute measurement.
     */
    public void broadcastPeak(final long timestamp, final double value) {
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY.PPG_PEAK_TIMESTAMP, timestamp);
        intent.putExtra(Constants.KEY.PPG_PEAK_VALUE, value);
        intent.setAction(Constants.ACTION.BROADCAST_PPG_PEAK);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(intent);
    }
}