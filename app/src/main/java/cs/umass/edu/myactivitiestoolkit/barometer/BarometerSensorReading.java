package cs.umass.edu.myactivitiestoolkit.barometer;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.MHLClient.sensors.SensorReading;

/**
 * Created by Aaron Kaplowitz on 12/9/16.
 */

public class BarometerSensorReading extends SensorReading {
    /** The heart rate value. **/
    private final float[] values;

    /**
     * Instantiates a Barometer sensor reading.
     * @param userID a 10-byte hex string identifying the current user.
     * @param deviceType describes the device
     * @param deviceID unique device identifier
     * @param t the timestamp at which the event occurred, in Unix time by convention.
     * @param values the barometer reading in hPa
     */
    public BarometerSensorReading(String userID, String deviceType, String deviceID, long t, float[] values){
        super(userID, deviceType, deviceID, "SENSOR_BAROMETER", t);

        this.values = values;
    }
    // COLLECT LABELED DATA WITH THIS ONE
    public BarometerSensorReading(String userID, String deviceType, String deviceID, long t, float[] values, int label){
        super(userID, deviceType, deviceID, "SENSOR_BAROMETER", t, label);

        this.values = values;
    }

    @Override
    protected JSONObject toJSONObject(){
        JSONObject obj = getBaseJSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("t", timestamp);
            data.put("value", values);

            obj.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
