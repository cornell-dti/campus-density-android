package org.cornelldti.density.density;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.cornelldti.density.density.util.ColorBarChartRenderer;
import org.cornelldti.density.density.util.ColorBarDataSet;
import org.cornelldti.density.density.util.ColorBarMarkerView;
import org.cornelldti.density.density.util.ValueFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;
import androidx.core.content.ContextCompat;

public class FacilityPage extends AppCompatActivity {
    // TODO: Rename para meter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PARAM = "Facility_Object";

    private TextView facilityName, facilityHours, currentOccupancy, feedback;
    private ImageButton backButton;
    private BarChart densityChart;
    private Facility facility;
    private ImageView firstBar, secondBar, thirdBar, fourthBar;

    private ChipGroup dayChips;
    private Chip sun, mon, tue, wed, thu, fri, sat;

    private ArrayList<Double> densities = new ArrayList<>();

    private RequestQueue queue;
    private SharedPreferences pref;

    public static final String HISTORICAL_DATA_ENDPOINT =
            "https://us-central1-campus-density-backend.cloudfunctions.net/historicalData";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facility_page);

        queue = Volley.newRequestQueue(this);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            facility = (Facility) b.getSerializable(ARG_PARAM);
        }

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        backButton = findViewById(R.id.backButton);
        facilityName = findViewById(R.id.f_name);

        currentOccupancy = findViewById(R.id.currentOccupancy);
        firstBar = findViewById(R.id.first_bar);
        secondBar = findViewById(R.id.second_bar);
        thirdBar = findViewById(R.id.third_bar);
        fourthBar = findViewById(R.id.fourth_bar);

        feedback = findViewById(R.id.accuracy);

        dayChips = findViewById(R.id.dayChips);
        sun = findViewById(R.id.sun);
        mon = findViewById(R.id.mon);
        tue = findViewById(R.id.tue);
        wed = findViewById(R.id.wed);
        thu = findViewById(R.id.thu);
        fri = findViewById(R.id.fri);
        sat = findViewById(R.id.sat);

        densityChart = findViewById(R.id.densityChart);
        facilityHours = findViewById(R.id.f_hours);

        initializeView();
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

    private void fetchHistoricalJSON(Function<Boolean, Void> success, String day) {
        Log.d("URL", HISTORICAL_DATA_ENDPOINT + "?id=" + facility.getId());
        JsonArrayRequest historicalDataRequest = new JsonArrayRequest
                (Request.Method.GET, HISTORICAL_DATA_ENDPOINT + "?id=" + facility.getId(), null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<Double> historicalDensities = new ArrayList<Double>();
                        try {
                            JSONObject facilityHistory = response.getJSONObject(0).getJSONObject("history");
                            JSONObject fac_on_day = facilityHistory.getJSONObject(day);
                            for (int hour = 7; hour <= 23; hour++) {
                                historicalDensities.add(fac_on_day.getDouble(String.valueOf(hour)));
                            }
                            Log.d("historicalSiz", String.valueOf(historicalDensities.size()));
                            densities = historicalDensities;
                            setupBarChart();
                        }
                        catch(JSONException e)
                        {
                            e.printStackTrace();
                        }
                        success.apply(true);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(FacilityPage.this, "Please check internet connection", Toast.LENGTH_LONG).show();
                        Log.d("ERROR MESSAGE", error.toString());
                        success.apply(false);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + getString(R.string.auth_key));
                headers.put("x-api-key", pref.getString("auth_token", ""));
                return headers;
            }
        };
        queue.add(historicalDataRequest);
    }

//    private List<Double> loadHistoricalData(String day) {
//        List<Double> densities = new ArrayList<>();
//        try {
//            JSONObject jsonObject = new JSONObject(loadJSONFile());
//            JSONArray facilities = jsonObject.getJSONArray("Facilities");
//            for (int i = 0; i < facilities.length(); i++) {
//                if (facilities.getJSONObject(i).getString("id").equals(facility.getId())) {
//                    JSONObject fac_on_day = facilities.getJSONObject(i).getJSONObject(day);
//                    for (int hour = 7; hour <= 23; hour++) {
//                        densities.add(fac_on_day.getDouble(String.valueOf(hour)));
//                    }
//                }
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return densities;
//    }

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
                facilityHours.setText(operatingHours("SUN"));
                fetchHistoricalJSON(success -> null, "SUN");
                break;
            case R.id.mon:
                facilityHours.setText(operatingHours("MON"));
                fetchHistoricalJSON(success -> null, "MON");
                break;
            case R.id.tue:
                facilityHours.setText(operatingHours("TUE"));
                fetchHistoricalJSON(success -> null, "TUE");
                break;
            case R.id.wed:
                facilityHours.setText(operatingHours("WED"));
                fetchHistoricalJSON(success -> null, "WED");
                break;
            case R.id.thu:
                facilityHours.setText(operatingHours("THU"));
                fetchHistoricalJSON(success -> null, "THU");
                break;
            case R.id.fri:
                facilityHours.setText(operatingHours("FRI"));
                fetchHistoricalJSON(success -> null, "FRI");
                break;
            case R.id.sat:
                facilityHours.setText(operatingHours("SAT"));
                fetchHistoricalJSON(success -> null, "SAT");
                break;
        }
    }

    private void setupBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        Log.d("SIZ", String.valueOf(densities.size()));
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
        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.very_empty));
        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.pretty_empty));
        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.pretty_crowded));
        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.very_crowded));

        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setValueFormatter(new ValueFormatter());

        BarData data = new BarData(dataSet);
        data.setValueTextSize(13f);
        // adjusts the width of the data bars
        data.setBarWidth(0.9f);

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

        // allows rounded bars on graph
        densityChart.setRenderer(new ColorBarChartRenderer(densityChart, densityChart.getAnimator(), densityChart.getViewPortHandler()));
        // removes gap between graph and the x-axis
        densityChart.getAxisLeft().setAxisMinimum(0f);

        // sets the marker for the graph
        IMarker marker = new ColorBarMarkerView(getApplicationContext(), R.layout.marker_layout);
        densityChart.setMarker(marker);

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
        facilityHours.setText(operatingHours(getDayString()));
        currentOccupancy.setText(getString(facility.getDensityResId()));
        feedback.setMovementMethod(LinkMovementMethod.getInstance());

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fetchHistoricalJSON(success -> null, getDayString());
        setChipOnClickListener();
        setToday(getDayString());
        setBars();
    }

    private void setBars() {
        List<ImageView> bars = new ArrayList<>();
        bars.add(firstBar);
        bars.add(secondBar);
        bars.add(thirdBar);
        bars.add(fourthBar);

        int color = R.color.filler_boxes;
        if (facility.isOpen()) {
            switch (facility.getOccupancyRating()) {
                case 0:
                    color = R.color.very_empty;
                    break;
                case 1:
                    color = R.color.pretty_empty;
                    break;
                case 2:
                    color = R.color.pretty_crowded;
                    break;
                case 3:
                    color = R.color.very_crowded;
                    break;
            }
        }

        for (int i = 0; i <= facility.getOccupancyRating(); i++) {
            bars.get(i).setColorFilter(getResources().getColor(color));
        }

    }

    private void setToday(String dayString) {
        switch (dayString) {
            case "SUN":
                sun.setChecked(true);
                break;
            case "MON":
                mon.setChecked(true);
                break;
            case "TUE":
                tue.setChecked(true);
                break;
            case "WED":
                wed.setChecked(true);
                break;
            case "THU":
                thu.setChecked(true);
                break;
            case "FRI":
                fri.setChecked(true);
                break;
            case "SAT":
                sat.setChecked(true);
                break;
        }
    }

    private String operatingHours(String day) {
        switch (facility.getName()) {
            case "Rose Dining Hall":
                switch (day) {
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
                switch (day) {
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
                switch (day) {
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
                switch (day) {
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
                if (day.equals("FRI")) {
                    return "11:00 AM - 2:30 PM";
                } else if (day.equals("SUN") || day.equals("SAT")) {
                    return "Closed";
                } else {
                    return "11:00 AM - 2:30 PM\n4:30 PM - 7:30 PM";
                }
            case "North Star at Appel":
                switch (day) {
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
                switch (day) {
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
                switch (day) {
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
                switch (day) {
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
                switch (day) {
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
                switch (day) {
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
                switch (day) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}