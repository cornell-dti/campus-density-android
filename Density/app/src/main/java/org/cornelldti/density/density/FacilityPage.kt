package org.cornelldti.density.density

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.format.DateFormat
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

import org.cornelldti.density.density.util.ColorBarChartRenderer
import org.cornelldti.density.density.util.ColorBarDataSet
import org.cornelldti.density.density.util.ColorBarMarkerView
import org.cornelldti.density.density.util.FluxUtil
import org.cornelldti.density.density.util.ValueFormatter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.text.SimpleDateFormat

import androidx.arch.core.util.Function
import androidx.core.content.ContextCompat
import java.util.*

class FacilityPage : BaseActivity() {

    private var selectedDay: String? = null

    private var facilityName: TextView? = null
    private var facilityHours: TextView? = null
    private var currentOccupancy: TextView? = null
    private var feedback: TextView? = null
    private var todayHours: TextView? = null
    private var backButton: ImageButton? = null
    private var densityChart: BarChart? = null
    private var facility: Facility? = null
    private var firstPill: ImageView? = null
    private var secondPill: ImageView? = null
    private var thirdPill: ImageView? = null
    private var fourthPill: ImageView? = null

    private var dayChips: ChipGroup? = null
    private var sun: Chip? = null
    private var mon: Chip? = null
    private var tue: Chip? = null
    private var wed: Chip? = null
    private var thu: Chip? = null
    private var fri: Chip? = null
    private var sat: Chip? = null
    private var wasChecked: Int = 0

    private var opHours: List<String> = ArrayList() // KEEPS TRACK OF OPERATING HOURS FOR FACILITY
    private var densities: List<Double> = ArrayList() // KEEPS TRACK OF HISTORICAL DENSITIES


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.facility_page)

        val b = intent.extras
        if (b != null) {
            facility = b.getSerializable(ARG_PARAM) as Facility
        }
        // TODO Uncomment this
        //     facility = refreshFacilityOccupancy(facility);

        backButton = findViewById(R.id.backButton)
        facilityName = findViewById(R.id.f_name)

        currentOccupancy = findViewById(R.id.currentOccupancy)
        firstPill = findViewById(R.id.first_pill)
        secondPill = findViewById(R.id.second_pill)
        thirdPill = findViewById(R.id.third_pill)
        fourthPill = findViewById(R.id.fourth_pill)

        feedback = findViewById(R.id.accuracy)

        dayChips = findViewById(R.id.dayChips)
        sun = findViewById(R.id.sun)
        mon = findViewById(R.id.mon)
        tue = findViewById(R.id.tue)
        wed = findViewById(R.id.wed)
        thu = findViewById(R.id.thu)
        fri = findViewById(R.id.fri)
        sat = findViewById(R.id.sat)

        densityChart = findViewById(R.id.densityChart)
        densityChart!!.setNoDataText("")
        todayHours = findViewById(R.id.today_hours)
        facilityHours = findViewById(R.id.f_hours)

        initializeView()
    }

    private fun refreshFacilityOccupancy(fac: Facility): Facility {
        singleFacilityOccupancy(fac.id!!)
        return fac.setOccupancy_rating(super.facility_occupancy_rating)
    }

    private fun setChipOnClickListener() {
        dayChips!!.setOnCheckedChangeListener { group, checkedId -> setDay(checkedId) }
    }

    private fun setDay(checkedId: Int) {
        var day = ""
        when (checkedId) {
            R.id.sun -> day = "SUN"
            R.id.mon -> day = "MON"
            R.id.tue -> day = "TUE"
            R.id.wed -> day = "WED"
            R.id.thu -> day = "THU"
            R.id.fri -> day = "FRI"
            R.id.sat -> day = "SAT"
            -1 -> dayChips!!.check(wasChecked)
        }
        if (checkedId != -1 && wasChecked != checkedId) {
            wasChecked = checkedId
            selectedDay = day
            fetchHistoricalJSON({ }, day, facility!!)
        }
    }

    private fun setupBarChart() {
        Log.d("SETUP", "BARCHART")
        val entries = ArrayList<BarEntry>()
        var isClosed = true
        for (i in densities.indices) {
            if (densities[i] != (-1).toDouble()) {
                entries.add(BarEntry(i.toFloat(), densities[i].toFloat()))
                isClosed = false
            } else {
                entries.add(BarEntry(i.toFloat(), 0f))
            }
        }

        val dataSet = ColorBarDataSet(entries, "Results")
        dataSet.setDrawValues(false)

        val colors = ArrayList<Int>()
        colors.add(ContextCompat.getColor(applicationContext, R.color.very_empty))
        colors.add(ContextCompat.getColor(applicationContext, R.color.pretty_empty))
        colors.add(ContextCompat.getColor(applicationContext, R.color.pretty_crowded))
        colors.add(ContextCompat.getColor(applicationContext, R.color.very_crowded))

        dataSet.colors = colors
        dataSet.valueTextColor = Color.DKGRAY
        dataSet.valueFormatter = ValueFormatter

        val data = BarData(dataSet)
        data.setValueTextSize(13f)
        // adjusts the width of the data bars
        data.barWidth = 0.9f

        val is24 = DateFormat.is24HourFormat(applicationContext)
        val xAxis = ArrayList<String>()
        xAxis.add("")
        xAxis.add("")
        xAxis.add(if (is24) "09:00" else "9am")
        xAxis.add("")
        xAxis.add("")
        xAxis.add(if (is24) "12:00" else "12pm")
        xAxis.add("")
        xAxis.add("")
        xAxis.add(if (is24) "15:00" else "3pm")
        xAxis.add("")
        xAxis.add("")
        xAxis.add(if (is24) "18:00" else "6pm")
        xAxis.add("")
        xAxis.add("")
        xAxis.add(if (is24) "21:00" else "9pm")
        xAxis.add("")
        xAxis.add("")

        densityChart!!.description.isEnabled = false
        densityChart!!.legend.isEnabled = false
        densityChart!!.setScaleEnabled(false)
        densityChart!!.setTouchEnabled(true)

        // sets the marker for the graph
        if (!isClosed) {
            // allows rounded bars on graph
            densityChart!!.renderer = ColorBarChartRenderer(densityChart!!, densityChart!!.animator, densityChart!!.viewPortHandler)
            // removes gap between graph and the x-axis
            densityChart!!.axisLeft.axisMinimum = 0f
            val marker = ColorBarMarkerView(applicationContext, R.layout.marker_layout)
            densityChart!!.marker = marker
        }

        densityChart!!.axisLeft.isEnabled = false
        densityChart!!.axisRight.isEnabled = false
        densityChart!!.xAxis.setDrawGridLines(false)
        densityChart!!.xAxis.position = XAxis.XAxisPosition.BOTTOM

        densityChart!!.xAxis.valueFormatter = IndexAxisValueFormatter(xAxis)
        densityChart!!.xAxis.labelCount = xAxis.size
        if (!isClosed)
            densityChart!!.data = data
        else {
            densityChart!!.data = null
            val p = densityChart!!.getPaint(Chart.PAINT_INFO)
            p.textSize = 36f
            p.color = Color.BLACK
            p.isFakeBoldText = true
            densityChart!!.setNoDataText("Closed")
        }
        densityChart!!.invalidate()
        densityChart!!.animateY(500)

    }

    private fun initializeView() {
        facilityName!!.text = facility!!.name
        currentOccupancy!!.text = getString(facility!!.densityResId)
        feedback!!.movementMethod = LinkMovementMethod.getInstance()

        backButton!!.setOnClickListener { onBackPressed() }

        fetchHistoricalJSON({ }, FluxUtil.dayString, facility!!)
        setToday(FluxUtil.dayString)
        setChipOnClickListener()
        setPills()
    }

    private fun setPills() {
        val bars = ArrayList<ImageView?>()
        bars.add(firstPill)
        bars.add(secondPill)
        bars.add(thirdPill)
        bars.add(fourthPill)

        var color = R.color.filler_boxes
        if (facility!!.isOpen) {
            when (facility!!.occupancyRating) {
                0 -> color = R.color.very_empty
                1 -> color = R.color.pretty_empty
                2 -> color = R.color.pretty_crowded
                3 -> color = R.color.very_crowded
            }
        }

        for (i in 0..facility!!.occupancyRating) {
            bars[i]?.setColorFilter(resources.getColor(color))
        }

    }

    /**
     * Updates the chip to be "checked" when it is selected
     *
     * @param dayString specifies the day to be checked
     */
    private fun setToday(dayString: String) {
        selectedDay = dayString
        when (dayString) {
            "SUN" -> sun!!.isChecked = true
            "MON" -> mon!!.isChecked = true
            "TUE" -> tue!!.isChecked = true
            "WED" -> wed!!.isChecked = true
            "THU" -> thu!!.isChecked = true
            "FRI" -> fri!!.isChecked = true
            "SAT" -> sat!!.isChecked = true
        }
        wasChecked = dayChips!!.checkedChipId
    }

    private fun setOperatingHours(day: String) {
        Log.d("SET", "OPERATING")
        val hourTitle = FluxUtil.dayFullString(day)
        todayHours!!.text = hourTitle
        facilityHours!!.text = ""
        for (operatingSegment in opHours) {
            val allHours = facilityHours!!.text.toString() + operatingSegment + if (opHours.indexOf(operatingSegment) == opHours.size - 1) "" else "\n"
            facilityHours!!.text = allHours
        }
    }

    override fun updateUI() {
        Log.d("updatedFPUI", "updating")
        fetchHistoricalJSON({ }, selectedDay!!, facility!!)
    }

    private fun getDate(day: String): String {
        val current = Calendar.getInstance()
        val format = SimpleDateFormat("MM-dd-yy", Locale.US)
        val checkFormat = SimpleDateFormat("E", Locale.US)

        var dayCheck = checkFormat.format(current.time).toUpperCase(Locale.US)
        while (dayCheck != day) {
            current.add(Calendar.DAY_OF_MONTH, 1)
            dayCheck = checkFormat.format(current.time).toUpperCase(Locale.US)
        }

        return format.format(current.time)
    }

    private fun parseTime(timestamp: Long): String {
        val timeZone = Calendar.getInstance().timeZone
        var format = SimpleDateFormat("h:mma", Locale.US)
        if (DateFormat.is24HourFormat(applicationContext)) {
            format = SimpleDateFormat("HH:mm", Locale.US)
        }
        format.timeZone = timeZone

        return format.format(Date(timestamp * 1000)).toLowerCase(Locale.US)
    }

    override fun onBackPressed() {
        //        Intent intent = new Intent(FacilityPage.this, MainActivity.class);
        //        startActivity(intent);
        finish()
    }

    // OVERRIDE API FUNCTIONS


    override fun fetchOperatingHoursOnResponse(response: JSONArray, success: (Boolean) -> Unit, day: String) {
        super.fetchOperatingHoursOnResponse(response, success, day)
        opHours = ArrayList()
        val operatingHours = ArrayList<String>()
        try {
            val hours = response.getJSONObject(0).getJSONArray("hours")
            for (i in 0 until hours.length()) {
                val segment = hours.getJSONObject(i).getJSONObject("dailyHours")
                val start = segment.getLong("startTimestamp")
                val end = segment.getLong("endTimestamp")
                operatingHours.add(parseTime(start) + " – " + parseTime(end))
            }
            opHours = operatingHours
            setOperatingHours(day)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        success(true)
    }

    override fun fetchHistoricalJSONOnResponse(response: JSONArray, success: (Boolean) -> Unit, day: String) {
        val historicalDensities = ArrayList<Double>()
        try {
            val facilityHistory = response.getJSONObject(0).getJSONObject("hours")
            val fac_on_day = facilityHistory.getJSONObject(day)
            for (hour in 7..23) {
                historicalDensities.add(fac_on_day.getDouble(hour.toString()))
            }
            densities = historicalDensities
            setupBarChart()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        success(true)
    }

    companion object {
        val ARG_PARAM = "Facility_Object"
    }
}