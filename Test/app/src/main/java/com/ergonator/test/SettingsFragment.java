package com.ergonator.test;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int rate;
    private int time;

    private ImageView closeButton;

    private SettingsFragment fragment;
    private SeekBar rateBar;
    private SeekBar timeBar;

    //constants
    private final int SAMPLING_RATE_MINIMUM = 125;
    private final int SAMPLING_RATE_INCREMENT = 15;
    private final int TIME_RATE_MINIMUM = 15;
    private final int TIME_RATE_INCREMENT = 15;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String uri);
    }

    public SettingsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

            rate = Integer.parseInt(mParam1);

            //convert rate from millisecond timing to amount per second
            switch (rate)
            {
                case 8:
                    rate = 125;
                    break;
                case 7:
                    rate = 150;
                    break;
                case 6:
                    rate = 175;
                    break;
                case 5:
                    rate = 200;
                    break;
            }

            rate -= SAMPLING_RATE_MINIMUM;

            time = Integer.parseInt(mParam2) / 1000;
            time -= TIME_RATE_MINIMUM;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        fragment = this;

        closeButton = (ImageView)view.findViewById(R.id.closeButton);
        rateBar = (SeekBar) view.findViewById(R.id.samplingRateBar);
        rateBar.setProgress(rate);
        timeBar = (SeekBar) view.findViewById(R.id.timeBar);
        timeBar.setProgress(time);

        final TextView rateValue = (TextView)view.findViewById(R.id.rateNum);
        rateValue.setText(String.valueOf(rate + SAMPLING_RATE_MINIMUM));
        final TextView timeValue = (TextView)view.findViewById(R.id.timeNum);
        timeValue.setText(String.valueOf(time + TIME_RATE_MINIMUM));

        rateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                progress = (int) Math.round(progress / ((double)SAMPLING_RATE_INCREMENT));
                progress = progress * SAMPLING_RATE_INCREMENT;
                seekBar.setProgress(progress);
                rateValue.setText(String.valueOf(progress + SAMPLING_RATE_MINIMUM));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                progress = (int) Math.round(progress / ((double)TIME_RATE_INCREMENT));
                progress = progress * TIME_RATE_INCREMENT;
                seekBar.setProgress(progress);
                timeValue.setText(String.valueOf(progress + TIME_RATE_MINIMUM));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                // Check to see if the fragment is already showing.

                if (fragment != null) {
                    ((MainActivity) getActivity()).returnFromSettings(rateBar.getProgress() + SAMPLING_RATE_MINIMUM, timeBar.getProgress() + TIME_RATE_MINIMUM);

                    // Create and commit the transaction to remove the fragment.
                    FragmentTransaction fragmentTransaction =
                            fragmentManager.beginTransaction();
                    fragmentTransaction.remove(fragment).commit();
                }
            }
        });

        return view;
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
}
