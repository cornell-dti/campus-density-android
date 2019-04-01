package org.cornelldti.density.density;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
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
import org.cornelldti.density.density.util.FluxUtil;
import org.cornelldti.density.density.util.ValueFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import androidx.arch.core.util.Function;
import androidx.core.content.ContextCompat;

public class FacilityPage extends BaseActivity {
    public static final String ARG_PARAM = "Facility_Object";

    private String selectedDay;

    private TextView facilityName, facilityHours, currentOccupancy, feedback, todayHours;
    private ImageButton backButton;
    private ToggleButton favButton; // origin/feat/favorites
    private BarChart densityChart;
    private Facility facility;
    private ImageView firstPill, secondPill, thirdPill, fourthPill;

    private ChipGroup dayChips;
    private Chip sun, mon, tue, wed, thu, fri, sat;
    private int wasChecked;

    private List<String> opHours = new ArrayList<>(); // KEEPS TRACK OF OPERATING HOURS FOR FACILITY
    private List<Double> densities = new ArrayList<>(); // KEEPS TRACK OF HISTORICAL DENSITIES


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facility_page);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            facility = (Facility) b.getSerializable(ARG_PARAM);
        }
        // TODO Uncomment this
//     facility = refreshFacilityOccupancy(facility);

        backButton = findViewById(R.id.backButton);
        facilityName = findViewById(R.id.f_name);
        favButton = findViewById(R.id.fav_button); // origin/feat/favorites

        currentOccupancy = findViewById(R.id.currentOccupancy);
        firstPill = findViewById(R.id.first_pill);
        secondPill = findViewById(R.id.second_pill);
        thirdPill = findViewById(R.id.third_pill);
        fourthPill = findViewById(R.id.fourth_pill);

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
        densityChart.setNoDataText("");
        todayHours = findViewById(R.id.today_hours);
        facilityHours = findViewById(R.id.f_hours);

        initializeView();
    }

    private Facility refreshFacilityOccupancy(Facility fac) {
        singleFacilityOccupancy(fac.getId());
        Facility f = fac.setOccupancy_rating(super.getFacility_occupancy_rating());
        return f;
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
        String day = "";
        switch (checkedId) {
            case R.id.sun:
                day = "SUN";
                break;
            case R.id.mon:
                day = "MON";
                break;
            case R.id.tue:
                day = "TUE";
                break;
            case R.id.wed:
                day = "WED";
                break;
            case R.id.thu:
                day = "THU";
                break;
            case R.id.fri:
                day = "FRI";
                break;
            case R.id.sat:
                day = "SAT";
                break;
            case -1:
                dayChips.check(wasChecked);
        }
        if (checkedId != -1 && wasChecked != checkedId) {
            wasChecked = checkedId;
            selectedDay = day;
            fetchHistoricalJSON(success -> null, day, facility);
        }
    }

    private void setupBarChart() {
        Log.d("SETUP", "BARCHART");
        ArrayList<BarEntry> entries = new ArrayList<>();
        boolean isClosed = true;
        for (int i = 0; i < densities.size(); i++) {
            if (densities.get(i) != -1) {
                entries.add(new BarEntry(i, (float) densities.get(i).doubleValue()));
                isClosed = false;
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

        boolean is24 = DateFormat.is24HourFormat(getApplicationContext());
        ArrayList<String> xAxis = new ArrayList<>();
        xAxis.add("");
        xAxis.add("");
        xAxis.add(is24 ? "09:00" : "9am");
        xAxis.add("");
        xAxis.add("");
        xAxis.add(is24 ? "12:00" : "12pm");
        xAxis.add("");
        xAxis.add("");
        xAxis.add(is24 ? "15:00" : "3pm");
        xAxis.add("");
        xAxis.add("");
        xAxis.add(is24 ? "18:00" : "6pm");
        xAxis.add("");
        xAxis.add("");
        xAxis.add(is24 ? "21:00" : "9pm");
        xAxis.add("");
        xAxis.add("");

        densityChart.getDescription().setEnabled(false);
        densityChart.getLegend().setEnabled(false);
        densityChart.setScaleEnabled(false);
        densityChart.setTouchEnabled(true);

        // sets the marker for the graph
        if (!isClosed) {
            // allows rounded bars on graph
            densityChart.setRenderer(new ColorBarChartRenderer(densityChart, densityChart.getAnimator(), densityChart.getViewPortHandler()));
            // removes gap between graph and the x-axis
            densityChart.getAxisLeft().setAxisMinimum(0f);
            IMarker marker = new ColorBarMarkerView(getApplicationContext(), R.layout.marker_layout);
            densityChart.setMarker(marker);
        }

        densityChart.getAxisLeft().setEnabled(false);
        densityChart.getAxisRight().setEnabled(false);
        densityChart.getXAxis().setDrawGridLines(false);
        densityChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        densityChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxis));
        densityChart.getXAxis().setLabelCount(xAxis.size());
        if (!isClosed) densityChart.setData(data);
        else {
            densityChart.setData(null);
            Paint p = densityChart.getPaint(Chart.PAINT_INFO);
            p.setTextSize(36);
            p.setColor(Color.BLACK);
            p.setFakeBoldText(true);
            densityChart.setNoDataText("Closed");
        }
        densityChart.invalidate();
        densityChart.animateY(500);

    }

    private void initializeView() {
        facilityName.setText(facility.getName());
        currentOccupancy.setText(getString(facility.getDensityResId()));
//        feedback.setMovementMethod(LinkMovementMethod.getInstance());

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accuracyLink = getResources().getString(R.string.accuracy_link);
                Uri uri = Uri.parse(accuracyLink);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // origin/feat/favorites
        favButton.setChecked(favFacilities.contains(facility.getId()));
        favButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                favButton.setChecked(isChecked);
                toggleFavorite(facility);
            }
        });

        fetchHistoricalJSON(success -> null, FluxUtil.getDayString(), facility);
        setToday(FluxUtil.getDayString());
        setChipOnClickListener();
        setPills();
    }

    private void setPills() {
        List<ImageView> bars = new ArrayList<>();
        bars.add(firstPill);
        bars.add(secondPill);
        bars.add(thirdPill);
        bars.add(fourthPill);

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

    /**
     * Updates the chip to be "checked" when it is selected
     *
     * @param dayString specifies the day to be checked
     */
    private void setToday(String dayString) {
        selectedDay = dayString;
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
        wasChecked = dayChips.getCheckedChipId();
    }

    private void setOperatingHours(String day) {
        Log.d("SET", "OPERATING");
        String hourTitle = FluxUtil.dayFullString(day);
        todayHours.setText(hourTitle);
        facilityHours.setText("");
        for (String operatingSegment : opHours) {
            String allHours = facilityHours.getText() + operatingSegment + (opHours.indexOf(operatingSegment) == opHours.size() - 1 ? "" : "\n");
            facilityHours.setText(allHours);
        }
    }

    @Override
    protected void updateUI() {
        Log.d("updatedFPUI", "updating");
        fetchHistoricalJSON(success -> null, selectedDay, facility);
    }

    private String getDate(String day) {
        Calendar current = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy");
        SimpleDateFormat checkFormat = new SimpleDateFormat("E");

        String dayCheck = checkFormat.format(current.getTime()).toUpperCase();
        while (!dayCheck.equals(day)) {
            current.add(Calendar.DAY_OF_MONTH, 1);
            dayCheck = checkFormat.format(current.getTime()).toUpperCase();
        }

        return format.format(current.getTime());
    }

    private String parseTime(long timestamp) {
        TimeZone timeZone = Calendar.getInstance().getTimeZone();
        SimpleDateFormat format = new SimpleDateFormat("h:mma");
        if (DateFormat.is24HourFormat(getApplicationContext())) {
            format = new SimpleDateFormat("HH:mm");
        }
        format.setTimeZone(timeZone);

        return format.format(new Date((long) timestamp * 1000)).toLowerCase();
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(FacilityPage.this, MainActivity.class);
//        startActivity(intent);
        saveFavorites(); // origin/feat/favorites
        finish();
    }

    // OVERRIDE API FUNCTIONS


    @Override
    public void fetchOperatingHoursOnResponse(JSONArray response, Function<Boolean, Void> success, String day) {
        super.fetchOperatingHoursOnResponse(response, success, day);
        opHours = new ArrayList<>();
        ArrayList<String> operatingHours = new ArrayList<>();
        try {
            JSONArray hours = response.getJSONObject(0).getJSONArray("hours");
            for (int i = 0; i < hours.length(); i++) {
                JSONObject segment = hours.getJSONObject(i).getJSONObject("dailyHours");
                long start = segment.getLong("startTimestamp");
                long end = segment.getLong("endTimestamp");
                operatingHours.add(parseTime(start) + " – " + parseTime(end));
            }
            opHours = operatingHours;
            setOperatingHours(day);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        success.apply(true);
    }

    @Override
    public void fetchHistoricalJSONOnResponse(JSONArray response, Function<Boolean, Void> success, String day) {
        ArrayList<Double> historicalDensities = new ArrayList<>();
        try {
            JSONObject facilityHistory = response.getJSONObject(0).getJSONObject("hours");
            JSONObject fac_on_day = facilityHistory.getJSONObject(day);
            for (int hour = 7; hour <= 23; hour++) {
                historicalDensities.add(fac_on_day.getDouble(String.valueOf(hour)));
            }
            densities = historicalDensities;
            setupBarChart();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        success.apply(true);
    }
}