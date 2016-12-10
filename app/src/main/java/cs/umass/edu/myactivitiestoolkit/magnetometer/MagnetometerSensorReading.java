package cs.umass.edu.myactivitiestoolkit.magnetometer;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.MHLClient.sensors.SensorReading;

/**
 * Created by aaron on 12/10/16.
 */

public class MagnetometerSensorReading extends SensorReading {
    private final float value;

    /**
     * Instantiates a Barometer sensor reading.
     * @param userID a 10-byte hex string identifying the current user.
     * @param deviceType describes the device
     * @param deviceID unique device identifier
     * @param t the timestamp at which the event occurred, in Unix time by convention.
     * @param value the barometer reading in hPa
     */
    public MagnetometerSensorReading(String userID, String deviceType, String deviceID, long t, float value){
        super(userID, deviceType, deviceID, "SENSOR_MAGNETOMETER", t);

        this.value = value;
    }
    // COLLECT LABELED DATA WITH THIS ONE
    public MagnetometerSensorReading(String userID, String deviceType, String deviceID, long t, float value, int label){
        super(userID, deviceType, deviceID, "SENSOR_MAGNETOMETER", t, label);

        this.value = value;
    }

    @Override
    protected JSONObject toJSONObject(){
        JSONObject obj = getBaseJSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("t", timestamp);
            data.put("value", value);

            obj.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
