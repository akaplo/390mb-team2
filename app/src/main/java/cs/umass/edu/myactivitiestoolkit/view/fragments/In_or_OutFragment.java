package cs.umass.edu.myactivitiestoolkit.view.fragments;

import android.app.Fragment;
import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cs.umass.edu.myactivitiestoolkit.R;
import cs.umass.edu.myactivitiestoolkit.services.ServiceManager;



public class In_or_OutFragment extends Fragment implements SensorEventListener {

        //SensorManager lets you access the device's sensors
        //declare Variables
        TextView textView, TVAirPressure;
        private SensorManager sensorManager;
    private ServiceManager serviceManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceManager = ServiceManager.getInstance(getActivity());
        sensorManager = (SensorManager) getActivity().getSystemService(Service.SENSOR_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_barameter, container, false);
        textView = (TextView) view.findViewById(R.id.TextView);
        TVAirPressure = (TextView) view.findViewById(R.id.TVAirPressure);
        //create instance of sensor manager and get system service to interact with Sensor



        return view;
    }


        @Override
       public void onResume() {
            super.onResume();
            // register this class as a listener for the Pressure Sensor
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        // called when sensor value have changed
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                float[] values = event.values;
                TVAirPressure.setText("" + values[0]);
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
    }
