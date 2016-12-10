package cs.umass.edu.myactivitiestoolkit.view.fragments;

import android.app.Fragment;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import cs.umass.edu.myactivitiestoolkit.R;
import cs.umass.edu.myactivitiestoolkit.barometer.BarometerSensorReading;
import cs.umass.edu.myactivitiestoolkit.constants.Constants;
import cs.umass.edu.myactivitiestoolkit.services.ServiceManager;
import edu.umass.cs.MHLClient.client.MobileIOClient;
import edu.umass.cs.MHLClient.sensors.AccelerometerReading;


public class In_or_OutFragment extends Fragment implements SensorEventListener, AdapterView.OnItemSelectedListener {

    // Label for each activity
    public final static int NO_LABEL = -1;
    public final static int INSIDE_LABEL = 0;
    public final static int OUTSIDE_LABEL = 1;
    int mCurrentLabel = NO_LABEL;
    String TAG = getTag();

    TextView TVAirPressure, MagneticField, Light;
        private SensorManager sensorManager;
    private ServiceManager serviceManager;
    protected MobileIOClient mClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceManager = ServiceManager.getInstance(getActivity());
        sensorManager = (SensorManager) getActivity().getSystemService(Service.SENSOR_SERVICE);
        mClient = MobileIOClient.getInstance(getString(R.string.mobile_health_client_user_id));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_barameter, container, false);
        TVAirPressure = (TextView) view.findViewById(R.id.TVAirPressure);
        MagneticField = (TextView) view.findViewById(R.id.Magnetic);
        Light = (TextView) view.findViewById(R.id.light);
        //create instance of sensor manager and get system service to interact with Sensor

// "Spinner" is the dropdown box on the UI - a terrible class name
        Spinner spinner = (Spinner) view.findViewById(R.id.barometer_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.in_or_out, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        // Specify that this class (see onItemSelected below) is the desired listener
        spinner.setOnItemSelectedListener(this);
        return view;
    }


        @Override
       public void onResume() {
            super.onResume();
            // register this class as a listener for the Pressure Sensor
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        }

        // called when sensor value have changed
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                long timestamp_in_milliseconds = (long) ((double) event.timestamp / Constants.TIMESTAMPS.NANOSECONDS_PER_MILLISECOND);
                float[] values = event.values;
                TVAirPressure.setText("" + values[0]);
                mClient.connect();
                if (mCurrentLabel == ExerciseFragment.NO_LABEL) {
                    mClient.sendSensorReading(new BarometerSensorReading(getString(R.string.mobile_health_client_user_id), "MOBILE", "", timestamp_in_milliseconds, values));
                }
                // If the data is labeled, send the label along with the data
                else {
                    mClient.sendSensorReading(new BarometerSensorReading(getString(R.string.mobile_health_client_user_id), "MOBILE", "", timestamp_in_milliseconds, values, mCurrentLabel));
                }
            }
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                long timestamp_in_milliseconds = (long) ((double) event.timestamp / Constants.TIMESTAMPS.NANOSECONDS_PER_MILLISECOND);
                float[] values = event.values;
                MagneticField.setText("" + values[0]);
                if (mCurrentLabel == ExerciseFragment.NO_LABEL) {
                    mClient.sendSensorReading(new BarometerSensorReading(getString(R.string.mobile_health_client_user_id), "MOBILE", "", timestamp_in_milliseconds, values));
                }
                // If the data is labeled, send the label along with the data
                else {
                    mClient.sendSensorReading(new BarometerSensorReading(getString(R.string.mobile_health_client_user_id), "MOBILE", "", timestamp_in_milliseconds, values, mCurrentLabel));
                }
            }
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){
                long timestamp_in_milliseconds = (long) ((double) event.timestamp / Constants.TIMESTAMPS.NANOSECONDS_PER_MILLISECOND);
                float[] values = event.values;
                Light.setText(""+ (int)values[0]);
                if (mCurrentLabel == ExerciseFragment.NO_LABEL) {
                    mClient.sendSensorReading(new BarometerSensorReading(getString(R.string.mobile_health_client_user_id), "MOBILE", "", timestamp_in_milliseconds, values));
                }
                // If the data is labeled, send the label along with the data
                else {
                    mClient.sendSensorReading(new BarometerSensorReading(getString(R.string.mobile_health_client_user_id), "MOBILE", "", timestamp_in_milliseconds, values, mCurrentLabel));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onPause() {
            // unregister listener
            super.onPause();
            sensorManager.unregisterListener(this);
        }

    /**
     * Callback function for when the user has selected an activity
     * from the dropdown on the UI.
     * Required to implement as part of AdapterView.OnItemSelected interface.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // An item was selected. Retrieve the selected item using
        // parent.getItemAtPosition(position)
        Log.d(TAG, parent.getItemAtPosition(position).toString());
        // Turn the name of the activity into an int
        mCurrentLabel = getLabelFromString(parent.getItemAtPosition(position).toString());
    }

    /**
     * Required to implement as part of AdapterView.OnItemSelected interface.
     * Not needed by us, though.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }

    private int getLabelFromString(String label) {
        switch (label) {
            case "No Label": return  NO_LABEL;
            case "Outside": return OUTSIDE_LABEL;
            case "Inside": return INSIDE_LABEL;
            default: return Integer.MIN_VALUE;
        }
    }
    }
