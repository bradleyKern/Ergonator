package com.ergonator.test;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import static com.ergonator.test.MainActivity.convertInputStreamToString;

public class RiskRequest extends AsyncTask<Void, Void, Boolean> {

    // This is the data we are sending
    String riskUrl;
    String userID;
    String userToken;

    // This is a constructor that allows you to pass in the JSON body
    public RiskRequest(Map<String, String> data) {
        riskUrl = data.get("url");
        userID = data.get("_id");
        userToken = data.get("token");
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

            Log.d("GET", "here3");

            if (statusCode == 200) {

                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                String response = convertInputStreamToString(inputStream);

                Log.d("DATA RESPONSE", response);
                // From here you can convert the string to JSON with whatever JSON parser you like to use
                // After converting the string to JSON, I call my custom callback. You can follow this process too, or you can implement the onPostExecute(Result) method
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
            Log.e("DATAMESSAGE", "Data Sent");
        }
    }

    @Override
    protected void onCancelled() {
    }
}
