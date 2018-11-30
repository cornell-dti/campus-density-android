package org.cornelldti.density.density;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.cornelldti.density.density.util.ColorBarDataSet;
import org.cornelldti.density.density.util.ValueFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;


public class Facility_Page extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM = "Facility_Object";

    private TextView facility_name, facility_hours;

    private ImageButton backButton;

    private Facility facility;

    private OnFragmentInteractionListener mListener;

    private ArrayList<Double> densities = new ArrayList<Double>();

    private BarChart densityChart;

    public Facility_Page() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param facility Parameter 1.
     * @return A new instance of fragment Facility_Page.
     */
    // TODO: Rename and change types and number of parameters
    public static Facility_Page newInstance(Facility facility) {
        Facility_Page fragment = new Facility_Page();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM, facility);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);
        if (getArguments() != null) {
            facility = (Facility)getArguments().getSerializable(ARG_PARAM);
            loadHistoricalData();
        }
    }

    private String getDayString()
    {
        String dayString = "";
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY:
                dayString = getString(R.string.Sun);
                break;
            case Calendar.MONDAY:
                dayString = getString(R.string.Mon);
                break;
            case Calendar.TUESDAY:
                dayString = getString(R.string.Tue);
                break;
            case Calendar.WEDNESDAY:
                dayString = getString(R.string.Wed);
                break;
            case Calendar.THURSDAY:
                dayString = getString(R.string.Thu);
                break;
            case Calendar.FRIDAY:
                dayString = getString(R.string.Fri);
                break;
            case Calendar.SATURDAY:
                dayString = getString(R.string.Sat);
                break;

        }
        return dayString;
    }

    private String loadJSONFile() {
        String json = "";
        try {
            InputStream is = getActivity().getAssets().open("historical_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void loadHistoricalData()
    {
        try
        {
            String day = getDayString();
            JSONObject jsonObject = new JSONObject(loadJSONFile());
            JSONArray facilities = jsonObject.getJSONArray("Facilities");
            for(int i = 0; i < facilities.length(); i++)
            {
                if(facilities.getJSONObject(i).getString("id").equals(facility.getId()))
                {
                    JSONObject fac_on_day = facilities.getJSONObject(i).getJSONObject(day);
                    for(int hour = 7; hour <= 23; hour++)
                    {
                        densities.add(fac_on_day.getDouble(String.valueOf(hour)));
                    }
                }
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_facility__page, container, false);

        facility_name = v.findViewById(R.id.f_name);
        facility_hours = v.findViewById(R.id.f_hours);
        backButton = v.findViewById(R.id.backButton);
        densityChart = v.findViewById(R.id.densityChart);

        initializeView();
        setupBarChart();
        return v;
    }

    private void setupBarChart()
    {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for(int i = 0; i < densities.size(); i++)
        {
            if(densities.get(i).doubleValue() != -1)
            {
                entries.add(new BarEntry(i, (float) densities.get(i).doubleValue()));
            }
            else
            {
                entries.add(new BarEntry(i, 0));
            }
        }
        ColorBarDataSet dataSet = new ColorBarDataSet(entries, "Results");

        dataSet.setDrawValues(false);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(densityChart.getContext(), R.color.very_empty));
        colors.add(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.pretty_empty));
        colors.add(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.pretty_crowded));
        colors.add(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.very_crowded));
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setValueFormatter(new ValueFormatter());

        BarData data = new BarData(dataSet);
        data.setValueTextSize(13f);
        data.setBarWidth(0.6f);

        ArrayList<String> xAxis = new ArrayList<>();
        xAxis.add("");
        xAxis.add("");
        xAxis.add("9 AM");
        xAxis.add("");
        xAxis.add("");
        xAxis.add("12 PM");
        xAxis.add("");
        xAxis.add("");
        xAxis.add("3 PM");
        xAxis.add("");
        xAxis.add("");
        xAxis.add("6 PM");
        xAxis.add("");
        xAxis.add("");
        xAxis.add("9 PM");
        xAxis.add("");
        xAxis.add("");

        densityChart.getDescription().setEnabled(false);
        densityChart.getLegend().setEnabled(false);
        densityChart.setScaleEnabled(false);
        densityChart.setTouchEnabled(true);

        densityChart.getAxisLeft().setEnabled(false);
        densityChart.getAxisRight().setEnabled(false);
        densityChart.getXAxis().setDrawGridLines(false);
        densityChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        densityChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxis));
        densityChart.getXAxis().setLabelCount(xAxis.size());
        densityChart.setData(data);
        densityChart.invalidate();
        densityChart.animateY(500);

    }

    private void initializeView()
    {
        facility_name.setText(facility.getName());
        facility_hours.setText("Today's hours: \n\n" + operatingHours());

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private String operatingHours() {
        switch (facility.getName()) {
            case "Rose Dining Hall":
                switch (getDayString()) {
                case "SUN":
                    return "8:00 AM - 9:30 AM\n10:00 AM - 2:00 PM\n5:00 PM - 8:00 PM";
                case "MON":
                    return "7:30 AM - 10:00 AM\n5:00 PM - 8:00 PM";
                case "TUE":
                    return "7:30 AM - 10:00 AM\n5:00 PM - 8:00 PM";
                case "WED":
                    return "7:30 AM - 10:00 AM\n6:00 PM - 8:00 PM";
                case "THU":
                    return "7:30 AM - 10:00 AM\n5:00 PM - 8:00 PM";
                case "FRI":
                    return "7:30 AM - 10:00 AM\n5:00 PM - 8:00 PM";
                case "SAT":
                    return "8:00 AM - 10:00 AM\n10:30 AM - 2:00 PM\n5:00 PM - 8:00 PM";
                default:
                    return "Closed";
            }
            case "Risley":
                switch (getDayString()) {
                case "SUN":
                    return "Closed";
                case "MON":
                    return "11:00 AM - 2:00 PM\n5:00 PM - 7:00 PM";
                case "TUE":
                    return "11:00 AM - 2:00 PM\n5:00 PM - 7:00 PM";
                case "WED":
                    return "11:00 AM - 2:00 PM\n5:00 PM - 7:00 PM";
                case "THU":
                    return "11:00 AM - 2:00 PM\n5:00 PM - 7:00 PM";
                case "FRI":
                    return "11:00 AM - 2:00 PM\n5:00 PM - 7:00 PM";
                case "SAT":
                    return "Closed";
                default:
                    return "Closed";
            }
            case "RPCC Dining Hall":
                switch (getDayString()) {
                case "SUN":
                    return "10:00 AM - 2:00 PM\n5:30 PM - 8:30 PM";
                case "MON":
                    return "5:30 PM - 9:00 PM";
                case "TUE":
                    return "5:30 PM - 9:00 PM";
                case "WED":
                    return "5:30 PM - 9:00 PM";
                case "THU":
                    return "5:30 PM - 9:00 PM";
                case "FRI":
                    return "5:30 PM - 8:30 PM";
                case "SAT":
                    return "5:30 PM - 8:30 PM";
                default:
                    return "Closed";
            }
            case "Olin Libe Cafe":
                switch (getDayString()) {
                case "SUN":
                    return "10:00 AM - 12:00 AM";
                case "MON":
                    return "8:00 AM - 12:00 AM";
                case "TUE":
                    return "8:00 AM - 12:00 AM";
                case "WED":
                    return "8:00 AM - 12:00 AM";
                case "THU":
                    return "8:00 AM - 12:00 AM";
                case "FRI":
                    return "8:00 AM - 6:00 PM";
                case "SAT":
                    return "10:00 AM - 8:00 PM";
                default:
                    return "Closed";
            }
            case "Okenshields":
                if (getDayString().equals("FRI")) {
                return "11:00 AM - 2:30 PM";
            } else if (getDayString().equals("SUN") || getDayString().equals("SAT")) {
                return "Closed";
            } else {
                return "11:00 AM - 2:30 PM\n4:30 PM - 7:30 PM";
            }
            case "North Star at Appel":
                switch (getDayString()) {
                case "SUN":
                    return "10:00 AM - 2:00 PM\n2:00 PM - 4:00 PM\n5:00 PM - 8:00 PM";
                case "MON":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n2:00 PM - 4:00 PM\n5:00 PM - 8:00 PM";
                case "TUE":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n2:00 PM - 4:00 PM\n5:00 PM - 8:00 PM";
                case "WED":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n2:00 PM - 4:00 PM\n5:00 PM - 8:00 PM";
                case "THU":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n2:00 PM - 4:00 PM\n5:00 PM - 8:00 PM";
                case "FRI":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n2:00 PM - 4:00 PM\n5:00 PM - 8:00 PM";
                case "SAT":
                    return "8:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n2:00 PM - 4:00 PM\n5:00 PM - 8:00 PM";
                default:
                    return "Closed";
            }
            case "104West!":
                switch (getDayString()) {
                case "SUN":
                    return "11:00 AM - 2:00 PM\n5:00 PM - 7:00 PM";
                case "MON":
                    return "11:00 AM - 2:00 PM\n5:00 PM - 7:00 PM";
                case "TUE":
                    return "11:00 AM - 2:00 PM\n5:00 PM - 7:00 PM";
                case "WED":
                    return "11:00 AM - 2:00 PM\n5:00 PM - 7:00 PM";
                case "THU":
                    return "11:00 AM - 2:00 PM\n5:00 PM - 7:00 PM";
                case "FRI":
                    return "11:00 AM - 3:00 PM\n6:00 PM - 8:00 PM";
                case "SAT":
                    return "12:30 PM - 2:00 PM\n6:00 PM - 8:00 PM";
                default:
                    return "Closed";
            }
            case "Keeton House":
                switch (getDayString()) {
                case "SUN":
                    return "10:00 AM - 2:00 PM\n5:00 PM - 8:00 PM";
                case "MON":
                    return "7:30 AM - 10:00 AM\n5:00 PM - 8:00 PM";
                case "TUE":
                    return "11:00 AM - 2:00 PM\n5:00 PM - 8:00 PM";
                case "WED":
                    return "6:00 PM - 8:00 PM";
                case "THU":
                    return "7:30 AM - 10:00 AM\n5:00 PM - 8:00 PM";
                case "FRI":
                    return "7:30 AM - 10:00 AM\n5:00 PM - 8:00 PM";
                case "SAT":
                    return "10:30 AM - 2:00 PM\n5:00 PM - 8:00 PM";
                default:
                    return "Closed";
            }
            case "Jansen's at Bethe House":
                switch (getDayString()) {
                case "SUN":
                    return "10:00 AM - 2:00 PM\n4:30 PM - 7:30 PM";
                case "MON":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n4:30 PM - 7:30 PM";
                case "TUE":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n4:30 PM - 7:30 PM";
                case "WED":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n6:00 PM - 7:30 PM";
                case "THU":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n4:30 PM - 7:30 PM";
                case "FRI":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n4:30 PM - 7:30 PM";
                case "SAT":
                    return "10:30 AM - 2:00 PM\n4:30 PM - 7:30 PM";
                default:
                    return "Closed";
            }
            case "Carl Becker House":
                switch (getDayString()) {
                case "SUN":
                    return "10:00 AM - 2:00 PM\n5:00 PM - 8:00 PM";
                case "MON":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n5:00 PM - 8:00 PM";
                case "TUE":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n5:00 PM - 8:00 PM";
                case "WED":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 2:00 PM\n6:00 PM - 8:00 PM";
                case "THU":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 3:30 PM\n5:00 PM - 8:00 PM";
                case "FRI":
                    return "7:00 AM - 10:30 AM\n10:30 AM - 3:30 PM\n5:00 PM - 8:00 PM";
                case "SAT":
                    return "10:30 AM - 2:00 PM\n5:00 PM - 8:00 PM";
                default:
                    return "Closed";
            }
            case "Cafe Jennie":
                switch (getDayString()) {
                case "SUN":
                    return "Closed";
                case "MON":
                    return "8:00 AM - 6:00 PM";
                case "TUE":
                    return "8:00 AM - 6:00 PM";
                case "WED":
                    return "8:00 AM - 6:00 PM";
                case "THU":
                    return "8:00 AM - 6:00 PM";
                case "FRI":
                    return "8:00 AM - 6:00 PM";
                case "SAT":
                    return "10:00 AM - 5:00 PM";
                default:
                    return "Closed";
            }
            case "Alice Cook House":
                switch (getDayString()) {
                case "SUN":
                    return "10:00 AM - 2:00 PM\n5:00 PM - 9:00 PM";
                case "MON":
                    return "7:30 AM - 10:00 AM\n5:00 PM - 9:00 PM";
                case "TUE":
                    return "7:30 AM - 10:00 AM\n5:00 PM - 9:00 PM";
                case "WED":
                    return "6:00 PM - 9:00 PM";
                case "THU":
                    return "7:30 AM - 10:00 AM\n5:00 PM - 9:00 PM";
                case "FRI":
                    return "7:30 AM - 10:00 AM\n5:00 PM - 9:00 PM";
                case "SAT":
                    return "10:30 AM - 2:00 PM\n5:00 PM - 9:00 PM";
                default:
                    return "Closed";
            }
            default:
                return "";
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
    @Override
    public void dismiss() {
        super.dismiss();
    }
}
