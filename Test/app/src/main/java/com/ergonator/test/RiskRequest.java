package com.ergonator.test;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import static com.ergonator.test.MainActivity.convertInputStreamToString;

public class RiskRequest extends AsyncTask<Void, Void, Boolean> {

    // This is the data we are sending
    String riskUrl;
    String userID;
    String userToken;

    private ArrayList<String> riskTimes;
    private ArrayList<Integer> pushDuration;
    private ArrayList<Integer> liftDuration;
    private ArrayList<Integer> pushFrequency;
    private ArrayList<Integer> liftFrequency;

    // This is a constructor that allows you to pass in the JSON body
    public RiskRequest(Map<String, String> data) {
        riskUrl = data.get("url");
        userID = data.get("_id");
        userToken = data.get("token");

        riskTimes = new ArrayList<>();
        pushDuration = new ArrayList<>();
        liftDuration = new ArrayList<>();
        pushFrequency = new ArrayList<>();
        liftFrequency = new ArrayList<>();
    }

    /**
     * Converts a server response to necessary arrays
     * @param response the JSON string from the server
     */
    private void convertResponse(String response) {
        try {
            JSONObject data = new JSONObject(response);

            JSONArray times = data.getJSONArray("times");
            JSONArray pDur = data.getJSONArray("durPush");
            JSONArray lDur = data.getJSONArray("durLift");
            JSONArray pFreq = data.getJSONArray("freqLift");
            JSONArray lFreq = data.getJSONArray("freqPush");

            for (int i = 0; i < times.length(); i++) {
                riskTimes.add(times.getString(i));
                pushDuration.add(pDur.getInt(i));
                liftDuration.add(lDur.getInt(i));
                pushFrequency.add(pFreq.getInt(i));
                liftFrequency.add(lFreq.getInt(i));
            }

        } catch (Exception e) {Log.e("JSONConversion Error", e.getMessage());}
    }

    // This is a function that we are overriding from AsyncTask. It takes Strings as parameters because that is what we defined for the parameters of our async task
    @Override
    protected Boolean doInBackground(Void... params) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("M-d-yyyy");

        try {
            // This is getting the url from the string we passed in
            URL url = new URL(riskUrl);

            // Create the urlConnection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoInput(true);

            //Sets headers
            urlConnection.setRequestProperty("_id", userID);
            urlConnection.setRequestProperty("token", userToken);
            urlConnection.setRequestProperty("date", mdformat.format(calendar.getTime()));

            urlConnection.setRequestMethod("GET");

            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 200) {
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(inputStream);
                convertResponse(response);
                return true;
            } else {
                // Status code is not 200
                // Do something to handle the error
                Log.e("DATA ERROR", "Couldn't send data");
                return false;
            }

        } catch (Exception e) {
            Log.e("TAG", "ERROR BOIS " + e.toString());
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (success) {
            MainActivity.setRiskArrays(riskTimes, pushDuration, liftDuration, pushFrequency, liftFrequency);
        }
    }

    @Override
    protected void onCancelled() {
    }
}
