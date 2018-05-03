package com.ergonator.test;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.ergonator.test.MainActivity.convertInputStreamToString;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PersonalFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PersonalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalFragment extends Fragment {

    private ImageView closeButton;
    private EditText nameBox;
    private EditText heightBox;
    private EditText weightBox;
    private EditText ageBox;
    private TextView riskBox;

    private PersonalFragment fragment;
    private String userID;
    private String token;
    private final String INFO_URL = "http://192.168.1.127:3000/profile";

    private OnFragmentInteractionListener mListener;

    public PersonalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment PersonalFragment.
     */
    public static PersonalFragment newInstance(String id, String tok) {
        PersonalFragment fragment = new PersonalFragment();
        Bundle args = new Bundle();
        args.putString("userID", id);
        args.putString("token", tok);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userID = getArguments().getString("userID");
            token = getArguments().getString("token");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_personal, container, false);
        fragment = this;

        //setting layout things
        nameBox = (EditText)view.findViewById(R.id.name);
        heightBox = (EditText)view.findViewById(R.id.height);
        weightBox = (EditText)view.findViewById(R.id.weight);
        ageBox = (EditText)view.findViewById(R.id.age);
        riskBox = (TextView)view.findViewById(R.id.risk);

        Map<String, String>  params = new HashMap<String, String>();
        params.put("_id", userID);
        params.put("token", token);
        params.put("url", INFO_URL);
        params.put("type", "GET");

        PersonalRequest mInfo = new PersonalRequest(params);
        mInfo.execute((Void) null);

        closeButton = (ImageView)view.findViewById(R.id.closeButton);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                // Check to see if the fragment is already showing.

                if (fragment != null) {
                    //updating user info on leave
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("_id", userID);
                    params.put("token", token);
                    params.put("url", INFO_URL);
                    params.put("name", nameBox.getText().toString());
                    params.put("height", heightBox.getText().toString());
                    params.put("weight", weightBox.getText().toString());
                    params.put("age", ageBox.getText().toString());
                    params.put("type", "POST");

                    PersonalRequest mInfo = new PersonalRequest(params);
                    mInfo.execute((Void) null);

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    MenuFragment menu = new MenuFragment();
                    fragmentTransaction.replace(R.id.layout, menu);
                    fragmentTransaction.commit();
                }
            }
        });

        return view;
    }

    public void setProfile(String name, String height, String weight, String age, String risk) {
        nameBox.setText(name);
        heightBox.setText(height);
        weightBox.setText(weight);
        ageBox.setText(age);
        riskBox.setText("Average Risk: " + risk);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri.toString());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String uri);
    }


    public class PersonalRequest extends AsyncTask<Void, Void, Boolean> {

        // This is the data we are sending
        String infoUrl;
        String userID;
        String userToken;
        String name;
        String weight;
        String height;
        String age;
        String risk;
        String type; //get or post
        JSONObject postData;

        // This is a constructor that allows you to pass in the JSON body
        public PersonalRequest(Map<String, String> data) {
            infoUrl = data.get("url");
            userID = data.get("_id");
            userToken = data.get("token");
            name = data.get("name");
            weight = data.get("weight");
            height = data.get("height");
            age = data.get("age");
            type = data.get("type");

            data.remove("url");
            data.remove("type");
            postData = new JSONObject(data);
        }

        /**
         * Converts a server response to necessary arrays
         * @param response the JSON string from the server
         */
        private void convertResponse(String response) {
            try {
                JSONObject data = new JSONObject(response);
                name = data.getString("name");
                weight = data.getString("weight");
                height = data.getString("height");
                age = data.getString("age");
                risk = data.getString("risk");
            } catch (Exception e) {
                Log.e("JSONConversion Error", e.getMessage());}
        }

        // This is a function that we are overriding from AsyncTask. It takes Strings as parameters because that is what we defined for the parameters of our async task
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // This is getting the url from the string we passed in
                URL url = new URL(infoUrl);

                // Create the urlConnection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setDoInput(true);

                if (type.equals("GET")) {
                    //Sets headers
                    urlConnection.setRequestProperty("_id", userID);
                    urlConnection.setRequestProperty("token", userToken);

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
                } else {
                    urlConnection.setDoOutput(true);

                    //set headers
                    urlConnection.setRequestProperty("Content-Type", "application/json");

                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write(postData.toString());
                    writer.flush();

                    int statusCode = urlConnection.getResponseCode();

                    if (statusCode == 200) {
                        return true;
                    } else {
                        // Status code is not 200
                        // Do something to handle the error
                        Log.e("DATA ERROR", "Couldn't send data");
                        return false;
                    }
                }

            } catch (Exception e) {
                Log.e("TAG", "ERROR BOIS " + e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                setProfile(name, height, weight, age, risk);
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
