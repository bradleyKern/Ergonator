package com.ergonator.test;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener, GraphFragment.OnFragmentInteractionListener {

    private Sensor mSensorAccel;
    private Sensor mSensorGyro;
    private Sensor mSensorLinAccel;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;

    // globally
    private String dataUrl = "http://10.231.227.151:3000/data";
    private Timer dataCollectTimer;
    private Timer dataSendTimer;
    private Button sendDataButton;
    private Button viewGraphButton;
    //collected data is separated by new lines
    private String collectedData;
    private SendDataTask mDataTask;
    private String userID = "";
    private String userToken = "";
    private long startTime = 0;

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
       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        Intent intentBundle = getIntent();
        userID = intentBundle.getStringExtra("_id");
        userToken = intentBundle.getStringExtra("token");

        sendDataButton = (Button)findViewById(R.id.send_data_button);
        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSendingData();
            }
        });
        viewGraphButton = (Button)findViewById(R.id.view_graph_button);
        viewGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGraph();
            }
        });

        collectedData = "";

        /*GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);*/

        /*serverButton.setOnClickListener(new View.OnClickListener() {
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
            }
        });*/

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

    private void startSendingData()
    {
        dataCollectTimer = new Timer();
        dataCollectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                collectData();
            }

        }, 0, 500);

        dataSendTimer = new Timer();
        dataSendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendData();
            }

        }, 0, 10000);

        startTime = System.currentTimeMillis();

        sendDataButton.setText("Stop Sending Data");
        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSendingData();
            }
        });
    }

    private void stopSendingData()
    {
        dataCollectTimer.cancel();

        dataSendTimer.cancel();

        sendDataButton.setText("Start Sending Data");

        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSendingData();
            }
        });
    }

    private void showGraph()
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GraphFragment fragment = new GraphFragment();
        fragmentTransaction.add(R.id.layout, fragment);
        fragmentTransaction.commit();
    }

    private void collectData(){
        String dataString = "";
        dataString += (System.currentTimeMillis() - startTime) + ",";
        dataString += accelX + ",";
        dataString += accelY + ",";
        dataString += accelZ + ",";
        dataString += gyroX + ",";
        dataString += gyroY + ",";
        dataString += gyroZ + ",";
        dataString += linAccelX + ",";
        dataString += linAccelY + ",";
        dataString += linAccelZ + ",";
        dataString += Long.toString(System.currentTimeMillis());
        dataString += "\n";
        collectedData += dataString;
        Log.e("TAG", "DATA COLLECTED");
    }

    private void sendData(){
        Log.e("TAG", "ATTEMPTING TO SEND DATA");

        if (mDataTask != null) {
            return;
        }

        Map<String, String>  params = new HashMap<String, String>();
        params.put("_id", userID);
        params.put("token", userToken);
        params.put("data", collectedData);

        mDataTask = new SendDataTask(params);
        collectedData = "";
        mDataTask.execute((Void) null);
    }

    public class SendDataTask extends AsyncTask<Void, Void, Boolean> {

        // This is the data we are sending
        JSONObject postData;

        // This is a constructor that allows you to pass in the JSON body
        public SendDataTask(Map<String, String> postData) {
            if (postData != null) {
                this.postData = new JSONObject(postData);
            }
        }

        // This is a function that we are overriding from AsyncTask. It takes Strings as parameters because that is what we defined for the parameters of our async task
        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // This is getting the url from the string we passed in
                URL url = new URL(dataUrl);

                // Create the urlConnection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();


                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                urlConnection.setRequestProperty("Content-Type", "application/json");

                urlConnection.setRequestMethod("POST");


                // OPTIONAL - Sets an authorization header
                urlConnection.setRequestProperty("Authorization", "someAuthString");

                // Send the post body
                if (this.postData != null) {
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write(postData.toString());
                    writer.flush();
                }

                int statusCode = urlConnection.getResponseCode();

                if (statusCode == 200) {

                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                    String response = convertInputStreamToString(inputStream);

                    Log.e("DATA RESPONSE", response);
                    return true;
                    // From here you can convert the string to JSON with whatever JSON parser you like to use
                    // After converting the string to JSON, I call my custom callback. You can follow this process too, or you can implement the onPostExecute(Result) method
                } else {
                    // Status code is not 200
                    // Do something to handle the error
                    Log.e("DATA ERROR", "Couldn't send data");
                    return false;
                }

            } catch (Exception e) {
                Log.d("TAG", e.getLocalizedMessage());
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mDataTask = null;

            if (success) {
                Log.e("DATAMESSAGE", "Data Sent");
            }
        }

        @Override
        protected void onCancelled() {
            mDataTask = null;
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    @Override
    public void onFragmentInteraction(String uri) {
        System.out.println(uri);
    }
}
