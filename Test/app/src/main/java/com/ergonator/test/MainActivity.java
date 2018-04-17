package com.ergonator.test;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.speech.SpeechRecognizer;

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
import java.util.ArrayList;
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

    //Speech stuff
    private SpeechRecognizer mSpeech;
    private Intent speechIntent;
    private ArrayList<String> speechResults;

    //Vibrator
    private Vibrator v;
    private long[] vibePatt = {0, 200, 200, 200};

    //Audio
    private MediaPlayer mpWait;
    private MediaPlayer mpBegin;

    // globally
    private String dataUrl = "http://10.231.62.128:3000/data";
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

        v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
        mpWait = MediaPlayer.create(this, R.raw.wait);
        mpBegin = MediaPlayer.create(this, R.raw.begin);

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

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mSensorAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorLinAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mSpeech = SpeechRecognizer.createSpeechRecognizer(this);

        mSpeech.setRecognitionListener(new speechListener());

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.ergonator.test");

        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);

        mSpeech.startListening(speechIntent);
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

        mSpeech = SpeechRecognizer.createSpeechRecognizer(this);

        mSpeech.setRecognitionListener(new speechListener());

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.ergonator.test");

        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);

        mSpeech.startListening(speechIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSpeech.stopListening();
        mSpeech.cancel();
        mSpeech.destroy();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSpeech.stopListening();
        mSpeech.cancel();
        mSpeech.destroy();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSpeech.stopListening();
        mSpeech.cancel();
        mSpeech.destroy();
        mSensorManager.unregisterListener(this);
    }

    private void startSendingData()
    {
        sendDataButton.setText("Starting...");
        //v.vibrate(400);

        mpWait.start();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                startTime = System.currentTimeMillis();

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

                sendDataButton.setText("Stop Sending Data");
                sendDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stopSendingData();
                    }
                });

                //v.vibrate(vibePatt, -1);
                mpBegin.start();
            }
        }, 3000);
    }

    private void stopSendingData()
    {
        v.vibrate(400);
        dataCollectTimer.cancel();

        dataSendTimer.cancel();

        sendData();

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
        dataString += ((System.currentTimeMillis() - startTime) / 100.0) + ",";
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

    public class speechListener implements RecognitionListener
    {

        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Log.d("Speech:", "Begin speech");
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int i) {
            if (i == 6)
                mSpeech.startListening(speechIntent);
        }

        @Override
        public void onResults(Bundle results) {
            speechResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < speechResults.size(); i++)
            {
                String word = speechResults.get(i).toLowerCase();

                if (word.contains("rgo") && word.contains("start")) {
                    Log.d("Speech:", "START DETECTED");
                    startSendingData();
                    break;
                }

                if (word.contains("rgo") && word.toLowerCase().contains("stop")) {
                    Log.d("Speech:", "STOP DETECTED");
                    stopSendingData();
                    break;
                }
            }
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    }
}
