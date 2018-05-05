package com.ergonator.test;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static com.ergonator.test.MainActivity.convertInputStreamToString;


public class SendDataTask extends AsyncTask<Void, Void, Boolean> {

    // This is the data we are sending
    JSONObject postData;
    private String dataUrl = LoginActivity.IPANDPORT + "/data";

    private String time = "";
    private int pDur = 0;
    private int lDur = 0;
    private int pFreq = 0;
    private int lFreq = 0;


    // This is a constructor that allows you to pass in the JSON body
    public SendDataTask(Map<String, String> postData) {
        if (postData != null) {
            this.postData = new JSONObject(postData);
        }
    }

    /**
     * Converts a server response to necessary values
     * @param response the JSON string from the server
     */
    private void convertResponse(String response) {
        try {
            JSONObject data = new JSONObject(response);

            time = data.getString("time");
            pDur = data.getInt("durPush");
            lDur = data.getInt("durLift");
            pFreq = data.getInt("freqLift");
            lFreq = data.getInt("freqPush");
        } catch (Exception e) {Log.e("JSONConversion Error", e.getMessage());}
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

                convertResponse(response);

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
        MainActivity.mDataTask = null;

        if (success) {
            MainActivity.addToRiskArray(time, pDur, lDur, pFreq, lFreq);
        }
    }

    @Override
    protected void onCancelled() {
        MainActivity.mDataTask = null;
    }
}
