package com.ergonator.test;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GraphFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GraphFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //risk ArrayLists
    private ArrayList<String> riskTimes;
    private ArrayList<Integer> pushDuration;
    private ArrayList<Integer> liftDuration;
    private ArrayList<Integer> pushFrequency;
    private ArrayList<Integer> liftFrequency;

    //Data points arrays
    private DataPoint[] pushDur;
    private DataPoint[] liftDur;
    private DataPoint[] pushFreq;
    private DataPoint[] liftFreq;

    private ImageView closeButton;

    private GraphFragment fragment;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String uri);
    }

    public GraphFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GraphFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GraphFragment newInstance(ArrayList<String> times, ArrayList<Integer> pushDur, ArrayList<Integer> liftDur, ArrayList<Integer> pushFreq, ArrayList<Integer> liftFreq) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("times", times);
        args.putIntegerArrayList("pushDur", pushDur);
        args.putIntegerArrayList("liftDur", liftDur);
        args.putIntegerArrayList("pushFreq", pushFreq);
        args.putIntegerArrayList("liftFreq", liftFreq);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            riskTimes = getArguments().getStringArrayList("times");
            pushDuration = getArguments().getIntegerArrayList("pushDur");
            liftDuration = getArguments().getIntegerArrayList("liftDur");
            pushFrequency = getArguments().getIntegerArrayList("pushFreq");
            liftFrequency = getArguments().getIntegerArrayList("liftFreq");
            createDataArrays();
        }
    }

    private void createDataArrays() {
        pushDur = new DataPoint[riskTimes.size()];
        liftDur = new DataPoint[riskTimes.size()];
        pushFreq = new DataPoint[riskTimes.size()];
        liftFreq = new DataPoint[riskTimes.size()];

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        try {
            for (int i = 0; i < riskTimes.size(); i++) {
                Date d = format.parse(riskTimes.get(i));
                pushDur[i] = new DataPoint(d, pushDuration.get(i));
                liftDur[i] = new DataPoint(d, liftDuration.get(i));
                pushFreq[i] = new DataPoint(d, pushFrequency.get(i));
                liftFreq[i] = new DataPoint(d, liftFrequency.get(i));
            }
        } catch (Exception e) { Log.e("Error", "Cannot parse date string");}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        GraphView graph = view.findViewById(R.id.graph);
        graph.setTitle("Risk");
        graph.setTitleTextSize(144f);
        graph.setTitleColor(Color.parseColor("#ffffff"));
        GridLabelRenderer label =  graph.getGridLabelRenderer();
        label.setGridColor(Color.parseColor("#ffffff"));
        label.setHorizontalAxisTitle("Time");
        label.setHorizontalAxisTitleColor(Color.parseColor("#ffffff"));
        label.setHorizontalLabelsAngle(45);
        label.setVerticalAxisTitle("Risk Level");
        label.setVerticalAxisTitleColor(Color.parseColor("#ffffff"));
        label.setHorizontalLabelsColor(Color.parseColor("#ffffff"));
        label.setVerticalLabelsColor(Color.parseColor("#ffffff"));
        graph.getViewport().setBackgroundColor(Color.parseColor("#000000"));
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setTextColor(Color.WHITE);
        graph.getGridLabelRenderer().setLabelFormatter(new ErgoDateLabelFormatter(getActivity()));
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getGridLabelRenderer().setHumanRounding(false);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(4);

        if (riskTimes.size() != 0) { //if there is data
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(pushDur[0].getX());
            graph.getViewport().setMaxX(pushDur[pushDur.length - 1].getX());
        }

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);

        //risk graphs
        LineGraphSeries<DataPoint> pushDSeries = new LineGraphSeries<>(pushDur);
        LineGraphSeries<DataPoint> liftDSeries = new LineGraphSeries<>(liftDur);
        LineGraphSeries<DataPoint> pushFSeries = new LineGraphSeries<>(pushFreq);
        LineGraphSeries<DataPoint> liftFSeries = new LineGraphSeries<>(liftFreq);

        //graph names for legend
        pushDSeries.setTitle("Push Duration");
        liftDSeries.setTitle("Lift Duration");
        pushFSeries.setTitle("Push Frequency");
        liftFSeries.setTitle("Lift Frequency");

        //colors
        liftDSeries.setColor(Color.RED);
        pushFSeries.setColor(Color.GREEN);
        liftFSeries.setColor(Color.YELLOW);

        //adding graphs to the ACTUAL graph
        graph.addSeries(pushDSeries);
        graph.addSeries(liftDSeries);
        graph.addSeries(pushFSeries);
        graph.addSeries(liftFSeries);

        fragment = this;

        closeButton = (ImageView)view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getActivity().getFragmentManager().popBackStack();
                FragmentManager fragmentManager = getFragmentManager();
                // Check to see if the fragment is already showing.

                if (fragment != null) {
                    ((MainActivity) getActivity()).returnFromGraph();
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

    public class ErgoDateLabelFormatter extends DefaultLabelFormatter {
        /**
         * the date format that will convert
         * the unix timestamp to string
         */
        protected final DateFormat mDateFormat;
        protected final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        /**
         * calendar to avoid creating new date objects
         */
        protected final Calendar mCalendar;

        /**
         * create the formatter with the Android default date format to convert
         * the x-values.
         *
         * @param context the application context
         */
        public ErgoDateLabelFormatter(Context context) {
            mDateFormat = android.text.format.DateFormat.getDateFormat(context);
            mCalendar = Calendar.getInstance();
        }

        /**
         * create the formatter with your own custom
         * date format to convert the x-values.
         *
         * @param context the application context
         * @param dateFormat custom date format
         */
        public ErgoDateLabelFormatter(Context context, DateFormat dateFormat) {
            mDateFormat = dateFormat;
            mCalendar = Calendar.getInstance();
        }

        /**
         * formats the x-values as date string.
         *
         * @param value raw value
         * @param isValueX true if it's a x value, otherwise false
         * @return value converted to string
         */
        @Override
        public String formatLabel(double value, boolean isValueX) {
            if (isValueX) {
                // format as date
                mCalendar.setTimeInMillis((long) value);
                return format.format(mCalendar.getTimeInMillis());
            } else {
                return super.formatLabel(value, isValueX);
            }
        }
    }
}
