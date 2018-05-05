package com.ergonator.test;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
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
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener,
        MenuFragment.OnFragmentInteractionListener, GraphFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener, PersonalFragment.OnFragmentInteractionListener,
        HelpFragment.OnFragmentInteractionListener, ProjectFragment.OnFragmentInteractionListener {

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

    //sensor settings
    protected static int samplingRate = 8; //sampling rate in milliseconds
    protected static int timeShift = 15000; //time between data transfer in milliseconds

    // globally
    private Timer dataCollectTimer;
    private Timer dataSendTimer;
    private ImageView sendDataButton;
    private ImageView viewGraphButton;
    private ImageView viewMenuButton;
    private boolean inFragment = false;

    //collected data is separated by new lines
    private String collectedData;
    protected static SendDataTask mDataTask;
    private String userID = "";
    private String userToken = "";
    private long startTime = 0;

    //risk values
    private String riskUrl = LoginActivity.IPANDPORT + "/history";
    private static ArrayList<String>  riskTimes;
    private static ArrayList<Integer> pushDuration;
    private static ArrayList<Integer> liftDuration;
    private static ArrayList<Integer> pushFrequency;
    private static ArrayList<Integer> liftFrequency;

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
    private static int durationRisk = 0;
    private static int frequencyRisk = 0;
    private static int maxRisk = 0;

    //battery
    private IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent batteryStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intentBundle = getIntent();
        userID = intentBundle.getStringExtra("_id");
        userToken = intentBundle.getStringExtra("token");

        if (intentBundle.getStringExtra("rate") != null) {
            samplingRate = Integer.parseInt(intentBundle.getStringExtra("rate"));
            timeShift = Integer.parseInt(intentBundle.getStringExtra("time"));
        }

        //battery
         batteryStatus = this.registerReceiver(null, ifilter);

        //setting up notification things
        v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
        mpWait = MediaPlayer.create(this, R.raw.wait);
        mpBegin = MediaPlayer.create(this, R.raw.begin);

        //buttons
        sendDataButton = (ImageView)findViewById(R.id.send_data_button);
        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSendingData();
            }
        });

        viewGraphButton = (ImageView)findViewById(R.id.view_graph_button);
        viewGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpeech.stopListening();
                inFragment = true;
                showGraph();
            }
        });

        viewMenuButton = (ImageView)findViewById(R.id.menu);
        viewMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpeech.stopListening();
                inFragment = true;
                showMenu();
            }
        });

        collectedData = "";

        //risk
        riskTimes = new ArrayList<>();
        riskTimes = new ArrayList<>();
        pushDuration = new ArrayList<>();
        liftDuration = new ArrayList<>();
        pushFrequency = new ArrayList<>();
        liftFrequency = new ArrayList<>();

        Map<String, String>  params = new HashMap<String, String>();
        params.put("_id", userID);
        params.put("token", userToken);
        params.put("url", riskUrl);

        RiskRequest mRisk = new RiskRequest(params);
        mRisk.execute((Void) null);

        //sensors
        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mSensorAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorLinAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        //speech recognition
        mSpeech = SpeechRecognizer.createSpeechRecognizer(this);

        mSpeech.setRecognitionListener(new SpeechListener());

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.ergonator.test");

        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);

        mSpeech.startListening(speechIntent);
    }

    @Override
    public void onBackPressed() {
        if (inFragment)
            return;
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

        mSpeech.setRecognitionListener(new SpeechListener());

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
        sendDataButton.setImageResource(android.R.drawable.ic_popup_sync);
        RotateAnimation anim = new RotateAnimation(0f, 350f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(700);
        sendDataButton.startAnimation(anim);
        v.vibrate(400);

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

                }, 0, samplingRate);

                dataSendTimer = new Timer();
                dataSendTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendData();
                    }

                }, timeShift, timeShift);

                sendDataButton.setAnimation(null);
                sendDataButton.setImageResource(android.R.drawable.ic_media_pause);
                sendDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stopSendingData();
                    }
                });

                v.vibrate(vibePatt, -1);
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

        sendDataButton.setImageResource(android.R.drawable.ic_media_play);

        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSendingData();
            }
        });
    }

    private void batteryLow()
    {
        dataSendTimer.cancel();
        dataCollectTimer.cancel();
        v.vibrate(1000);
        mSpeech.cancel();
        sendDataButton.setImageResource(android.R.drawable.ic_lock_idle_low_battery);
        sendDataButton.setOnClickListener(null);
    }

    //shows the graph fragment
    private void showGraph()
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GraphFragment fragment = GraphFragment.newInstance(riskTimes, pushDuration, liftDuration, pushFrequency, liftFrequency);
        fragmentTransaction.add(R.id.layout, fragment);
        fragmentTransaction.commit();
    }

    public void returnFromGraph()
    {
        inFragment = false;
        mSpeech.startListening(speechIntent);
    }

    public void showMenu()
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MenuFragment fragment = MenuFragment.newInstance(userID, userToken);
        fragmentTransaction.add(R.id.layout, fragment);
        fragmentTransaction.commit();
    }

    public void returnFromMenu()
    {
        inFragment = false;
        mSpeech.startListening(speechIntent);
    }

    /**
     * Returning from Settings screen
     * This passed in value needs to be converted into nearest
     * millisecond integer value. These are hardcoded for calculation
     * saving.
     * @param newRate the new rate in samples/second
     * @param newTime the new time between data uploads
     */
    public void returnFromSettings(int newRate, int newTime)
    {
        switch (newRate)
        {
            case 125:
                samplingRate = 8;
                break;
            case 150:
                samplingRate = 7;
                break;
            case 175:
                samplingRate = 6;
                break;
            case 200:
                samplingRate = 5;
                break;
        }

        //convert to milliseconds by multiplying by 1000
        timeShift = newTime * 1000;
    }

    public static void setRiskArrays(ArrayList<String> times, ArrayList<Integer> pushDur, ArrayList<Integer> liftDur, ArrayList<Integer> pushFreq, ArrayList<Integer> liftFreq) {
        riskTimes = times;
        pushDuration = pushDur;
        liftDuration = liftDur;
        pushFrequency = pushFreq;
        liftFrequency = liftFreq;
    }

    public static void addToRiskArray(String time, int pushDur, int liftDur, int pushFreq, int liftFreq) {
        riskTimes.add(time);
        pushDuration.add(pushDur);
        liftDuration.add(liftDur);
        pushFrequency.add(pushFreq);
        liftFrequency.add(liftFreq);

        durationRisk = pushDur + liftDur;
        frequencyRisk = pushFreq + liftFreq;
        maxRisk = Math.max(durationRisk, frequencyRisk);
    }

    private void collectData(){
        String dataString = "";
        dataString += ((System.currentTimeMillis() - startTime) / 100.0) + ",";
        dataString += accelX + ",";
        dataString += accelY + ",";
        dataString += accelZ + ",";
        dataString += linAccelX + ",";
        dataString += linAccelY + ",";
        dataString += linAccelZ + ",";
        dataString += gyroX + ",";
        dataString += gyroY + ",";
        dataString += gyroZ;
        dataString += "\n";
        collectedData += dataString;
        //Log.e("TAG", "DATA COLLECTED");
    }

    private void sendData() {
        //Log.e("TAG", "ATTEMPTING TO SEND DATA");

        if (mDataTask != null) {
            return;
        }

        Map<String, String>  params = new HashMap<String, String>();
        params.put("_id", userID);
        params.put("token", userToken);
        params.put("rate", (1000.0 / samplingRate) + "");
        params.put("time", (timeShift / 1000) + "");
        params.put("data", collectedData);

        mDataTask = new SendDataTask(params);
        collectedData = "";
        mDataTask.execute((Void) null);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;
        Log.d("Battery", batteryPct + "");

        //If battery is less than 10% we stop
        if (batteryPct < 0.1) {
            batteryLow();
        }

        //Prevents errors - see https://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateRiskUI();
            }
        });
    }

    private void updateRiskUI() {
        switch(maxRisk) {
            case 0:
                ((ImageView)findViewById(R.id.silhouette)).setImageResource(R.drawable.male_green);
                ((ImageView)findViewById(R.id.risk_level_text)).setImageResource(R.drawable.low);
                break;
            case 1:
                ((ImageView)findViewById(R.id.silhouette)).setImageResource(R.drawable.male_green_yellow);
                ((ImageView)findViewById(R.id.risk_level_text)).setImageResource(R.drawable.moderate);
                break;
            case 2:
                ((ImageView)findViewById(R.id.silhouette)).setImageResource(R.drawable.male_yellow_red);
                ((ImageView)findViewById(R.id.risk_level_text)).setImageResource(R.drawable.moderate);
                break;
            case 3:
                ((ImageView)findViewById(R.id.silhouette)).setImageResource(R.drawable.male_red);
                ((ImageView)findViewById(R.id.risk_level_text)).setImageResource(R.drawable.high);
                break;
        }
    }

    protected static String convertInputStreamToString(InputStream inputStream) throws IOException {
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

    //Our implementation of speech listener to get specific words caught
    public class SpeechListener implements RecognitionListener
    {

        @Override
        public void onReadyForSpeech(Bundle bundle) {
            //Log.d("Speech:", "Begin speech");
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
            if (i == 6 && !inFragment)
                mSpeech.startListening(speechIntent);
        }

        @Override
        public void onResults(Bundle results) {
            speechResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < speechResults.size(); i++)
            {
                String word = speechResults.get(i).toLowerCase();

                if (word.contains("rgo") && word.contains("start")) {
                    //Log.d("Speech:", "START DETECTED");
                    startSendingData();
                    break;
                }

                if (word.contains("rgo") && word.toLowerCase().contains("stop")) {
                    //Log.d("Speech:", "STOP DETECTED");
                    stopSendingData();
                    break;
                }
            }

            if (!inFragment)
                mSpeech.startListening(speechIntent);
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    }
}
