package cs.umass.edu.myactivitiestoolkit.magnetometer;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.MHLClient.sensors.SensorReading;

/**
 * Created by aaron on 12/10/16.
 */

public class MagnetometerSensorReading extends SensorReading {
    /** The acceleration along the x-axis **/
    private final float x;

    /** The acceleration along the y-axis **/
    private final float y;

    /** The acceleration along the z-axis **/
    private final float z;


    /**
     * Instantiates a Barometer sensor reading.
     * @param userID a 10-byte hex string identifying the current user.
     * @param deviceType describes the device
     * @param deviceID unique device identifier
     * @param t the timestamp at which the event occurred, in Unix time by convention.
     * @param values the barometer reading in hPa
     */
    public MagnetometerSensorReading(String userID, String deviceType, String deviceID, long t, float[] values){
        super(userID, deviceType, deviceID, "SENSOR_MAGNETOMETER", t);

        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
    }
    // COLLECT LABELED DATA WITH THIS ONE
    public MagnetometerSensorReading(String userID, String deviceType, String deviceID, long t, float[] values, int label){
        super(userID, deviceType, deviceID, "SENSOR_MAGNETOMETER", t, label);

        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
    }

    @Override
    protected JSONObject toJSONObject(){
        JSONObject obj = getBaseJSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("t", timestamp);
            data.put("x", x);
            data.put("y", y);
            data.put("z", z);

            obj.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
