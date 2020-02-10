package org.cornelldti.density.density.facilitydetail

import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.android.synthetic.main.facility_info_page.*
import org.cornelldti.density.density.BaseActivity
import org.cornelldti.density.density.R
import org.cornelldti.density.density.colorbarutil.ColorBarChartRenderer
import org.cornelldti.density.density.colorbarutil.ColorBarDataSet
import org.cornelldti.density.density.colorbarutil.ColorBarMarkerView
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.data.MenuClass
import org.cornelldti.density.density.util.FluxUtil
import org.cornelldti.density.density.util.ValueFormatter


class FacilityInfoPage : BaseActivity() {

    private var selectedDay: String? = null

    private lateinit var feedback: TextView

    private lateinit var menuItemList: RecyclerView
    private lateinit var menuItemListViewAdapter: RecyclerView.Adapter<*>
    private lateinit var menuItemListViewManager: RecyclerView.LayoutManager

    private var facilityClass: FacilityClass? = null

    private var wasChecked: Int = 0

    private var opHours: List<String> = ArrayList() // KEEPS TRACK OF OPERATING HOURS FOR FACILITY
    private var densities: List<Double> = ArrayList() // KEEPS TRACK OF HISTORICAL DENSITIES

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.facility_info_page)

        val b = intent.extras
        if (b != null) {
            facilityClass = b.getSerializable(ARG_PARAM) as FacilityClass
        }
        // TODO Uncomment this
        //     facilityClass = refreshFacilityOccupancy(facilityClass);

        feedback = findViewById(R.id.accuracy)

        densityChart.setNoDataText("")

        initializeView()
    }

    private fun refreshFacilityOccupancy(fac: FacilityClass): FacilityClass {
        api.singleFacilityOccupancy(fac.id)
        return fac.setOccupancyRating(super.facilityOccupancyRating)
    }


    private fun setDayChipOnClickListener() {
        dayChips.setOnCheckedChangeListener { _, checkedId -> setDay(checkedId) }
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
            -1 -> dayChips.check(wasChecked)
        }
        if (checkedId != -1 && wasChecked != checkedId) {
            wasChecked = checkedId
            selectedDay = day
            fetchHistoricalJSON(day, facilityClass!!.id)
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

        densityChart.description.isEnabled = false
        densityChart.legend.isEnabled = false
        densityChart.setScaleEnabled(false)
        densityChart.setTouchEnabled(true)

        // sets the marker for the graph
        if (!isClosed) {
            // allows rounded bars on graph
            densityChart.renderer = ColorBarChartRenderer(densityChart, densityChart.animator, densityChart.viewPortHandler)
            // removes gap between graph and the x-axis
            densityChart.axisLeft.axisMinimum = 0f
            val marker = ColorBarMarkerView(applicationContext, R.layout.marker_layout)
            densityChart.marker = marker
        }

        densityChart.axisLeft.isEnabled = false
        densityChart.axisRight.isEnabled = false
        densityChart.xAxis.setDrawGridLines(false)
        densityChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        densityChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxis)
        densityChart.xAxis.labelCount = xAxis.size
        if (!isClosed)
            densityChart.data = data
        else {
            densityChart.data = null
            val p = densityChart.getPaint(Chart.PAINT_INFO)
            p.textSize = 36f
            p.color = Color.BLACK
            p.isFakeBoldText = true
            densityChart.setNoDataText("Closed")
        }
        densityChart.invalidate()
        densityChart.animateY(500)

    }

    private fun initializeView() {
        facilityName.text = facilityClass!!.name
        currentOccupancy.text = getString(facilityClass!!.densityResId)
        feedback.movementMethod = LinkMovementMethod.getInstance()

        backButton.setOnClickListener { onBackPressed() }

        setToday(FluxUtil.dayString)
        setDayChipOnClickListener()
        setPills()
    }

    private fun setPills() {
        val bars = ArrayList<ImageView?>()
        bars.add(firstPill)
        bars.add(secondPill)
        bars.add(thirdPill)
        bars.add(fourthPill)

        var color = R.color.filler_boxes
        if (facilityClass!!.isOpen) {
            when (facilityClass!!.occupancyRating) {
                0 -> color = R.color.very_empty
                1 -> color = R.color.pretty_empty
                2 -> color = R.color.pretty_crowded
                3 -> color = R.color.very_crowded
            }
        }

        for (i in 0..facilityClass!!.occupancyRating) {
            bars[i]?.setColorFilter(ContextCompat.getColor(applicationContext, color))
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
            "SUN" -> sun.isChecked = true
            "MON" -> mon.isChecked = true
            "TUE" -> tue.isChecked = true
            "WED" -> wed.isChecked = true
            "THU" -> thu.isChecked = true
            "FRI" -> fri.isChecked = true
            "SAT" -> sat.isChecked = true
        }
        wasChecked = dayChips.checkedChipId
    }

    private fun setOperatingHours(day: String) {
        Log.d("SET", "OPERATING")
        val hourTitle = FluxUtil.dayFullString(day)
        todayHours.text = hourTitle
        facilityHours.text = ""
        for (operatingSegment in opHours) {
            val allHours = facilityHours.text.toString() + operatingSegment + if (opHours.indexOf(operatingSegment) == opHours.size - 1) "" else "\n"
            facilityHours.text = allHours
        }
    }

    override fun updateUI() {
        Log.d("updatedFPUI", "updating")
        fetchHistoricalJSON(day = selectedDay!!, facilityId = facilityClass!!.id)
        fetchMenuJSON(day = FluxUtil.getCurrentDate(), facilityId = facilityClass!!.id)
    }

    private fun fetchHistoricalJSON(day: String, facilityId: String) {
        api.fetchHistoricalJSON(
                day = day,
                facilityId = facilityId,
                fetchHistoricalJSONOnResponse = { historicalDensities ->
                    densities = historicalDensities
                    setupBarChart()
                },
                fetchOperatingHoursOnResponse = { operatingHours ->
                    opHours = operatingHours
                    setOperatingHours(day)
                }
        )
    }

    private fun fetchMenuJSON(day: String, facilityId: String) {
        api.fetchMenuJSON(
                day = day,
                facilityId = facilityId,
                fetchMenuJSONOnResponse = { menu ->
                    if(menu?.breakfastItems?.size == 0) {
                        breakfast.isVisible = false
                    }
                    if(menu?.brunchItems?.size == 0) {
                        brunch.isVisible = false
                    }
                    if(menu?.lunchItems?.size == 0) {
                        lunch.isVisible = false
                    }
                    if(menu?.liteLunchItems?.size == 0) {
                        lite_lunch.isVisible = false
                    }
                    if(menu?.dinnerItems?.size == 0) {
                        dinner.isVisible = false
                    }
                    showMenu(menu, firstVisibleChipId(menu))
                    menuChips.setOnCheckedChangeListener{_, checkedId -> showMenu(menu, checkedId) }
                }
        )
    }

    /**
     * Helper function that selects first visible chip and returns its ID
     */
    private fun firstVisibleChipId(menu: MenuClass?): Int {
        if(menu?.breakfastItems?.size == 0) {
            if(menu?.brunchItems?.size == 0) {
                if(menu?.lunchItems?.size == 0) {
                    if(menu?.liteLunchItems?.size == 0) {
                        if(menu?.dinnerItems?.size == 0) {
                            return -1
                        }
                        else {
                            dinner.isChecked = true
                            return R.id.dinner
                        }
                    }
                    else {
                        lite_lunch.isChecked = true
                        return R.id.lite_lunch
                    }
                }
                else {
                    lunch.isChecked = true
                    return R.id.lunch
                }
            }
            else {
                brunch.isChecked = true
                return R.id.brunch
            }
        }
        else {
            breakfast.isChecked = true
            return R.id.breakfast
        }
    }


    private fun showMenu(menu : MenuClass?, mealOfDay: Int) {
        if(menu != null) {
            menuItemListViewManager = LinearLayoutManager(this)
            when(mealOfDay) {
                R.id.breakfast -> menuItemListViewAdapter = MenuListAdapter(menu.breakfastItems, this)
                R.id.brunch -> menuItemListViewAdapter = MenuListAdapter(menu.brunchItems, this)
                R.id.lunch -> menuItemListViewAdapter = MenuListAdapter(menu.lunchItems, this)
                R.id.lite_lunch -> menuItemListViewAdapter = MenuListAdapter(menu.liteLunchItems, this)
                R.id.dinner -> menuItemListViewAdapter = MenuListAdapter(menu.dinnerItems, this)
                else -> menuItemListViewAdapter = MenuListAdapter(ArrayList(), this)
            }

            menuItemList = findViewById<RecyclerView>(R.id.menuItemsList).apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                layoutManager = menuItemListViewManager

                adapter = menuItemListViewAdapter

            }

        }
        else {
            Log.d("Menu", "Null")
        }
    }

    override fun onBackPressed(): Unit = finish()

    companion object {
        const val ARG_PARAM = "Facility_Object"
    }
}