package com.ergonator.test;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Sensor mSensorAccel;
    private Sensor mSensorGyro;
    private Sensor mSensorLinAccel;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;

    // globally
    private TextView textView_X;
    private TextView textView_Y;
    private TextView textView_Z;
    private Button serverButton;
    private RequestQueue requestQueue;
    private String url = "http://10.231.227.151:3000/data";

    //All Sensor Data Values
    private float accelX = 0;
    private float accelY = 0;
    private float accelZ = 0;
    private float gyroX = 0;
    private float gyroY = 0;
    private float gyroZ = 0;
    private float linAccelX = 0;
    private float linAccelY = 0;
    private float linAccelZ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        textView_X = (TextView)findViewById(R.id.textView_X);
        textView_Y = (TextView)findViewById(R.id.textView_Y);
        textView_Z = (TextView)findViewById(R.id.textView_Z);
        serverButton = (Button)findViewById(R.id.serverButton);
        requestQueue = Volley.newRequestQueue(this);

        serverButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Log.d("AXVALUE", Float.toString(accelX));
                Log.d("AYVALUE", Float.toString(accelY));
                Log.d("AZVALUE", Float.toString(accelZ));
                Log.d("GXVALUE", Float.toString(gyroX));
                Log.d("GYVALUE", Float.toString(gyroY));
                Log.d("GZVALUE", Float.toString(gyroZ));
                Log.d("LAXVALUE", Float.toString(linAccelX));
                Log.d("LAYVALUE", Float.toString(linAccelY));
                Log.d("LAZVALUE", Float.toString(linAccelZ));
                /*
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                // response
                                Log.d("Response", response);
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d("Error.Response", error.toString());
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("User", "Bradley");
                        params.put("MessageType", "Hi Ryan");
                        params.put("AccelX", Float.toString(accelX));
                        params.put("AccelY", Float.toString(accelY));
                        params.put("AccelZ", Float.toString(accelZ));
                        params.put("GyroX", Float.toString(gyroX));
                        params.put("GyroY", Float.toString(gyroY));
                        params.put("GyroZ", Float.toString(gyroZ));
                        params.put("LinAccelX", Float.toString(linAccelX));
                        params.put("LinAccelY", Float.toString(linAccelY));
                        params.put("LinAccelZ", Float.toString(linAccelZ));

                        return params;
                    }
                };
                requestQueue.add(postRequest);*/
            }
        });

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mSensorAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorLinAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
       /* float j = event.values[0];
        float k = event.values[1];
        float l = event.values[2];
        // Do something with this sensor value.

        textView_X.setText("X: " + j + "");
        textView_Y.setText("Y: " + k + "");
        textView_Z.setText("Z: " + l + "");*/

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelX = event.values[0];
            accelY = event.values[1];
            accelZ = event.values[2];
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroX = event.values[0];
            gyroY = event.values[1];
            gyroZ = event.values[2];
        }
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            linAccelX = event.values[0];
            linAccelY = event.values[1];
            linAccelZ = event.values[2];
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorAccel, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorGyro, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorLinAccel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
