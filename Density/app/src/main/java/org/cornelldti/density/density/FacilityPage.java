package org.cornelldti.density.density;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.cornelldti.density.density.util.ColorBarDataSet;
import org.cornelldti.density.density.util.ValueFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;


public class FacilityPage extends DialogFragment {
    // TODO: Rename para meter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM = "Facility_Object";

    private Spinner daysDropdown;
    private TextView facilityName, facilityHours;
    private ImageButton backButton;
    private BarChart densityChart;
    private Facility facility;
    private OnFragmentInteractionListener listener;

    private ChipGroup dayChips;
    private Chip day;

    private List<Double> densities = new ArrayList<>();

    public FacilityPage() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param facility Parameter 1.
     * @return A new instance of fragment FacilityPage.
     */
    // TODO: Rename and change types and number of parameters
    public static FacilityPage newInstance(Facility facility) {
        FacilityPage fragment = new FacilityPage();
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
            facility = (Facility) getArguments().getSerializable(ARG_PARAM);
            densities = loadHistoricalData(getDayString());
        }
    }

    private String getDayString() {
        String dayString = "";
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY:
                dayString = getString(R.string.SUN);
                break;
            case Calendar.MONDAY:
                dayString = getString(R.string.MON);
                break;
            case Calendar.TUESDAY:
                dayString = getString(R.string.TUE);
                break;
            case Calendar.WEDNESDAY:
                dayString = getString(R.string.WED);
                break;
            case Calendar.THURSDAY:
                dayString = getString(R.string.THU);
                break;
            case Calendar.FRIDAY:
                dayString = getString(R.string.FRI);
                break;
            case Calendar.SATURDAY:
                dayString = getString(R.string.SAT);
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

    private List<Double> loadHistoricalData(String day) {
        List<Double> densities = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(loadJSONFile());
            JSONArray facilities = jsonObject.getJSONArray("Facilities");
            for (int i = 0; i < facilities.length(); i++) {
                if (facilities.getJSONObject(i).getString("id").equals(facility.getId())) {
                    JSONObject fac_on_day = facilities.getJSONObject(i).getJSONObject(day);
                    for (int hour = 7; hour <= 23; hour++) {
                        densities.add(fac_on_day.getDouble(String.valueOf(hour)));
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return densities;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_facility__page, container, false);

        facilityName = v.findViewById(R.id.f_name);
        facilityHours = v.findViewById(R.id.f_hours);
        facilityHours = v.findViewById(R.id.f_hours);
        backButton = v.findViewById(R.id.backButton);
        densityChart = v.findViewById(R.id.densityChart);

        // daysDropdown = v.findViewById(R.id.daysDropDown);
        day = v.findViewById(R.id.mon);
        dayChips = v.findViewById(R.id.dayChips);

        initializeView();
        setupBarChart();
        return v;
    }

    private void setChipOnClickListener() {
        dayChips.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                setDay(checkedId);
            }
        });

    }

    private void setDay(int checkedId) {
        switch (checkedId) {
            case R.id.sun:
                densities = loadHistoricalData("SUN");
                break;
            case R.id.mon:
                densities = loadHistoricalData("MON");
                break;
            case R.id.tue:
                densities = loadHistoricalData("TUE");
                break;
            case R.id.wed:
                densities = loadHistoricalData("WED");
                break;
            case R.id.thu:
                densities = loadHistoricalData("THU");
                break;
            case R.id.fri:
                densities = loadHistoricalData("FRI");
                break;
            case R.id.sat:
                densities = loadHistoricalData("SAT");
                break;
        }
        setupBarChart();
    }

    private void setToday(String dayString) {
        switch (dayString) {
            case "SUN":
                day = getView().findViewById(R.id.sun);
                day.setChecked(true);
                break;
            case "MON":
                day = getView().findViewById(R.id.mon);
                day.setChecked(true);
                break;
            case "TUE":
                day = getView().findViewById(R.id.tue);
                day.setChecked(true);
                break;
            case "WED":
                day = getView().findViewById(R.id.wed);
                day.setChecked(true);
                break;
            case "THU":
                day = getView().findViewById(R.id.thu);
                day.setChecked(true);
                break;
            case "FRI":
                day = getView().findViewById(R.id.fri);
                day.setChecked(true);
                break;
            case "SAT":
                day = getView().findViewById(R.id.sat);
                day.setChecked(true);
                break;
        }
    }

    private void setupBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < densities.size(); i++) {
            if (densities.get(i) != -1) {
                entries.add(new BarEntry(i, (float) densities.get(i).doubleValue()));
            } else {
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
        densityChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String msg = "";
                if (e.getY() >= 0.75) {
                    msg = "Very Crowded";
                } else if (e.getY() >= 0.5) {
                    msg = "Pretty Crowded";
                } else if (e.getY() >= 0.25) {
                    msg = "Pretty Empty";
                } else {
                    msg = "Very Empty";
                }
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {
                // DO NOTHING
            }
        });


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

    private void initializeView() {
        facilityName.setText(facility.getName());
        facilityHours.setText(operatingHours());

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        densities = loadHistoricalData(getDayString());
        setToday(getDayString());
        setupBarChart();
        setChipOnClickListener();

        /*
        String[] dropdownItems = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        ArrayAdapter<String> dropdownMenuAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, dropdownItems);

        daysDropdown.setAdapter(dropdownMenuAdapter);
        daysDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // Deal with Monday
                        densities = loadHistoricalData("MON");
                        break;
                    case 1:
                        // Deal with Tuesday
                        densities = loadHistoricalData("TUE");
                        break;
                    case 2:
                        // Deal with Wednesday
                        densities = loadHistoricalData("WED");
                        break;
                    case 3:
                        // Deal with Thursday
                        densities = loadHistoricalData("THU");
                        break;
                    case 4:
                        // Deal with Friday
                        densities = loadHistoricalData("FRI");
                        break;
                    case 5:
                        // Deal with Saturday
                        densities = loadHistoricalData("SAT");
                        break;
                    case 6:
                        // Deal with Sunday
                        densities = loadHistoricalData("SUN");
                        break;
                }
                setupBarChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        */
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
        if (listener != null) {
            listener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
