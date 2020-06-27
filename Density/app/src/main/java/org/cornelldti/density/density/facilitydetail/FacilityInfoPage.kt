package org.cornelldti.density.density.facilitydetail

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isGone
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
import java.util.*
import kotlin.collections.ArrayList

class FacilityInfoPage : BaseActivity() {

    private var selectedDay: String? = null

    private val months = listOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")

    private val maxCapacity: Int = 100

    private lateinit var feedback: TextView

    private lateinit var menuItemList: RecyclerView
    private lateinit var menuItemListViewAdapter: RecyclerView.Adapter<*>
    private lateinit var menuItemListViewManager: RecyclerView.LayoutManager

    private var facilityClass: FacilityClass? = null

    private var wasCheckedDay: Int = -1
    private var wasCheckedMenu: Int = -1

    private var opHoursStrings: List<String> = ArrayList() // KEEPS TRACK OF OPERATING HOURS STRINGS FOR FACILITY
    // USED FOR DISPLAYING OPERATING HOURS OF FACILITY AT SELECTED DAY

    private var opHoursTimestamps: List<Pair<Long, Long>> = ArrayList() // KEEPS TRACK OF OPERATING HOURS TIMESTAMPS FOR FACILITY,
    // USED FOR CHECKING WHETHER OPEN/WHEN IT WILL OPEN

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

        // initialize view
        densityChart.setNoDataText("")

        topBar.title = facilityClass!!.name
        topBar.setNavigationOnClickListener { onBackPressed() }

        feedback = findViewById(R.id.accuracy)
        feedback.movementMethod = LinkMovementMethod.getInstance()

        setAvailability()
        setToday(FluxUtil.dayString)
        setDayChipsDate()
        setDayChipOnClickListener()
    }

    // TODO: complete or remove
    private fun refreshFacilityOccupancy(fac: FacilityClass): FacilityClass {
        api.singleFacilityOccupancy(fac.id)
        return fac.setOccupancyRating(super.facilityOccupancyRating)
    }

    private fun setAvailability() {
        max_capacity.text = getString(R.string.max_capacity, maxCapacity)
        when (facilityClass!!.densityResId) {
            R.string.closed -> availability_num.text = getString(R.string.closed)
            R.string.very_empty -> availability_num.text = getString(R.string.availability_lt, (0.26 * this.maxCapacity).toInt())
            R.string.pretty_empty -> availability_num.text = getString(R.string.availability_range, (0.26 * this.maxCapacity).toInt(), (0.5 * this.maxCapacity).toInt())
            R.string.pretty_crowded -> availability_num.text = getString(R.string.availability_range, (0.5 * this.maxCapacity).toInt(), (0.85 * this.maxCapacity).toInt())
            R.string.very_crowded ->
                availability_num.text = getString(R.string.availability_gt, (0.85 * this.maxCapacity).toInt())
        }
    }

    private fun setDayChipOnClickListener() {
        dayChips.setOnCheckedChangeListener { _, checkedId ->
            setDay(checkedId)
            setDayMenu(checkedId)
        }
    }

    private fun setDay(checkedId: Int) {
        var day = ""
        when (checkedId) {
            R.id.sun -> day = getString(R.string.SUN)
            R.id.mon -> day = getString(R.string.MON)
            R.id.tue -> day = getString(R.string.TUE)
            R.id.wed -> day = getString(R.string.WED)
            R.id.thu -> day = getString(R.string.THU)
            R.id.fri -> day = getString(R.string.FRI)
            R.id.sat -> day = getString(R.string.SAT)
            -1 -> dayChips.check(wasCheckedDay)
        }
        if (checkedId != -1 && wasCheckedDay != checkedId) {
            wasCheckedDay = checkedId
            selectedDay = day
            val daysDifference = FluxUtil.getDayDifference(FluxUtil.dayString, selectedDay!!)
            updateOperatingHoursOfSelectedDay(FluxUtil.getDateDaysAfter(daysDifference))
        }
    }

    private fun setDayMenu(checkedId: Int) {
        var selectedDay = FluxUtil.dayString
        when (checkedId) {
            R.id.sun -> selectedDay = getString(R.string.SUN)
            R.id.mon -> selectedDay = getString(R.string.MON)
            R.id.tue -> selectedDay = getString(R.string.TUE)
            R.id.wed -> selectedDay = getString(R.string.WED)
            R.id.thu -> selectedDay = getString(R.string.THU)
            R.id.fri -> selectedDay = getString(R.string.FRI)
            R.id.sat -> selectedDay = getString(R.string.SAT)
            -1 -> FluxUtil.dayString
        }
        val daysDifference = FluxUtil.getDayDifference(FluxUtil.dayString, selectedDay)
        fetchMenuJSON(day = FluxUtil.getDateStringDaysAfter(daysDifference), facilityId = facilityClass!!.id)
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

    private fun fetchOperatingHours(date: Date) {
        val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.DATE, 1)
        val nextDay: Date = c.time
        api.facilityHours(facilityId = facilityClass!!.id, startDate = FluxUtil.convertDateObjectToString(date),
                endDate = FluxUtil.convertDateObjectToString(nextDay),
                facilityHoursTimeStampsOnResponse = { hoursTimeStampsList ->
                    opHoursTimestamps = hoursTimeStampsList
                    setOpenOrClosedOnToolbar()
                },
                facilityHoursStringsOnResponse = { hoursStringsList ->
                    opHoursStrings = hoursStringsList
                    setOperatingHoursText(day = selectedDay!!, date = date)
                    fetchHistoricalJSON(day = selectedDay!!, facilityId = facilityClass!!.id)
                    fetchMenuJSON(day = FluxUtil.getCurrentDate(), facilityId = facilityClass!!.id)
                })
    }

    /**
     * This function is meant to only update the facility hours under the historical data chart
     * for the selected day, and does not affect the facility hours used to check if the place is open. Thus, here
     * facilityHoursTimeStampsOnResponse is not defined.
     */
    private fun updateOperatingHoursOfSelectedDay(date: Date) {
        val dateString = FluxUtil.convertDateObjectToString(date)
        api.facilityHours(facilityId = facilityClass!!.id, startDate = dateString,
                endDate = dateString,
                facilityHoursTimeStampsOnResponse = {
                    // Isn't Defined!
                },
                facilityHoursStringsOnResponse = { hoursStringsList ->
                    opHoursStrings = hoursStringsList
                    setOperatingHoursText(day = selectedDay!!, date = date)
                    fetchHistoricalJSON(day = selectedDay!!, facilityId = facilityClass!!.id)
                })
    }

    /**
     * This function uses the opHoursTimestamps class variable in order to determine if the facility is open or closed
     * given the current time. It also keeps track of when it is open until, if open and when it opens next, if closed.
     */
    private fun setOpenOrClosedOnToolbar() {
        var isOpen = false
        var openUntil: Long = -1L
        var opensNext: Long = -1L

        val currentTime = System.currentTimeMillis() / 1000L

        Log.d("timestamps", opHoursTimestamps.toString())
        if (opHoursTimestamps.size >= 2) {
            // Current Time before first time slot of day
            if (currentTime < opHoursTimestamps[0].first) {
                opensNext = opHoursTimestamps[0].first
                openUntil = -1L
            }
            // Current Time is after the last time slot of day
            else if (currentTime >= opHoursTimestamps[opHoursTimestamps.size - 2].second) {
                opensNext = opHoursTimestamps[opHoursTimestamps.size - 1].first
                openUntil = -1L
            } else {
                for (i in 0 until opHoursTimestamps.size - 1) {
                    // Current Time falls into one of the open time slots
                    if (currentTime >= opHoursTimestamps[i].first && currentTime < opHoursTimestamps[i].second) {
                        isOpen = true
                        openUntil = opHoursTimestamps[i].second
                        opensNext = -1L
                    }
                    // Current Time is between two time slots in day
                    else if (i > 0 && currentTime >= opHoursTimestamps[i - 1].second && currentTime < opHoursTimestamps[i].first) {
                        opensNext = opHoursTimestamps[i].first
                        openUntil = -1L
                    }
                }
            }
        }
        // SET TOOLBAR TEXT ACCORDINGLY
        if (opensNext == -1L && openUntil == -1L) {
            topBar.setSubtitleTextColor(getResources().getColor(R.color.closed_facility))
            topBar.subtitle = "Closed"
        } else {
            if (isOpen) {
                var text = SpannableStringBuilder("Open" + " until " + FluxUtil.parseTime(openUntil))
                text.setSpan(ForegroundColorSpan(resources.getColor(R.color.open_facility)), 0, 4, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                topBar.subtitle = text
            } else {
                var text = SpannableStringBuilder("Closed" + " \u2022 opens at " + FluxUtil.parseTime(opensNext))
                text.setSpan(ForegroundColorSpan(resources.getColor(R.color.closed_facility)), 0, 6, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                topBar.subtitle = text
            }
        }
    }

    private fun setDayChipsDate() {
        val chipsList = listOf(sun, mon, tue, wed, thu, fri, sat)
        val dayStrings = listOf(getString(R.string.SUN), getString(R.string.MON), getString(R.string.TUE), getString(R.string.WED),
                getString(R.string.THU), getString(R.string.FRI), getString(R.string.SAT))
        for (i in 0..6) {
            val daysDifference = FluxUtil.getDayDifference(FluxUtil.dayString, dayStrings.get(i))
            val date = FluxUtil.getDateDaysAfter(daysDifference)
            val chip = chipsList.get(i)
            chip.text = HtmlCompat.fromHtml(chip.text.toString() + "<br>" + "<br>" +
                    "<b>" + date.date + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }

    /**
     * Updates the chip to be "checked" when it is selected
     *
     * @param dayString specifies the day to be checked
     */
    private fun setToday(dayString: String) {
        selectedDay = dayString

        val dayChipList = arrayListOf<RadioButton>(sun, mon, tue, wed, thu, fri, sat);
        dayChips.removeAllViews()

        val todayInt = FluxUtil.dayInt
        for (i in todayInt..(todayInt + 6)) {
            dayChips.addView(dayChipList[i % 7])
        }
        dayChipList[todayInt].isChecked = true
        wasCheckedDay = dayChips.checkedRadioButtonId
    }

    /**
     * This function sets the operating hours text under the historical data chart
     * @param day The day for which the operating hours is set
     */
    private fun setOperatingHoursText(day: String, date: Date) {
        Log.d("SET", "OPERATING")
        val hourTitle = FluxUtil.dayFullString(day)
        todayHours.text = hourTitle
        todayDate.text = months[date.month] + " " + date.date
        facilityHours.text = ""
        for (operatingSegment in opHoursStrings) {
            val allHours = facilityHours.text.toString() + operatingSegment + if (opHoursStrings.indexOf(operatingSegment) == opHoursStrings.size - 1) "" else "\n"
            facilityHours.text = allHours
        }
    }

    override fun updateUI() {
        Log.d("updatedFPUI", "updating")
        fetchOperatingHours(date = FluxUtil.getDateObject(selectedDay!!)) // TODO FIX!! ON REFRESH!!
    }

    private fun fetchHistoricalJSON(day: String, facilityId: String) {
        api.fetchHistoricalJSON(
                day = day,
                facilityId = facilityId,
                fetchHistoricalJSONOnResponse = { historicalDensities ->
                    densities = historicalDensities
                    setupBarChart()
                }
        )
    }

    private fun fetchMenuJSON(day: String, facilityId: String) {
        api.fetchMenuJSON(
                day = day,
                facilityId = facilityId
        ) { menu ->
            if (menu?.breakfastItems?.size == 0
                    && menu.brunchItems.isEmpty()
                    && menu.lunchItems.isEmpty()
                    && menu.dinnerItems.isEmpty()) {
                menu_header.isGone = true
                menuCard.isGone = true
            } else {
                menu_header.isVisible = true
                menuCard.isVisible = true
                breakfast.isGone = !(menu?.breakfastItems?.isNotEmpty() ?: false)
                brunch.isGone = !(menu?.brunchItems?.isNotEmpty() ?: false)
                lunch.isGone = !(menu?.lunchItems?.isNotEmpty() ?: false)
                dinner.isGone = !(menu?.dinnerItems?.isNotEmpty() ?: false)
                wasCheckedMenu = firstVisibleChipId(menu)
                showMenu(menu, wasCheckedMenu)
                menuChips.setOnCheckedChangeListener { _, checkedId -> showMenu(menu, checkedId) }
            }
        }
    }

    /**
     * Helper function that selects first visible chip and returns its ID
     */
    private fun firstVisibleChipId(menu: MenuClass?): Int {
        if (menu != null) {
            when {
                menu.breakfastItems.isNotEmpty() -> {
                    breakfast.isChecked = true
                    return R.id.breakfast
                }
                menu.brunchItems.isNotEmpty() -> {
                    brunch.isChecked = true
                    return R.id.brunch
                }
                menu.lunchItems.isNotEmpty() -> {
                    lunch.isChecked = true
                    return R.id.lunch
                }
                menu.dinnerItems.isNotEmpty() -> {
                    dinner.isChecked = true
                    return R.id.dinner
                }
                else -> {
                    return -1
                }
            }
        } else {
            return -1
        }
    }


    private fun showMenu(menu: MenuClass?, mealOfDay: Int) {
        if (menu != null) {
            menuItemListViewManager = LinearLayoutManager(this)
            when (mealOfDay) {
                R.id.breakfast -> menuItemListViewAdapter = MenuListAdapter(menu.breakfastItems, this)
                R.id.brunch -> menuItemListViewAdapter = MenuListAdapter(menu.brunchItems, this)
                R.id.lunch -> menuItemListViewAdapter = MenuListAdapter(menu.lunchItems, this)
                R.id.dinner -> menuItemListViewAdapter = MenuListAdapter(menu.dinnerItems, this)
                -1 -> if (wasCheckedMenu != -1) menuChips.check(wasCheckedMenu)
                else menuItemListViewAdapter = MenuListAdapter(ArrayList(), this)
            }
            if (mealOfDay != -1 && wasCheckedMenu != mealOfDay) {
                wasCheckedMenu = mealOfDay
            }

            menuItemList = findViewById<RecyclerView>(R.id.menuItemsList).apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                layoutManager = menuItemListViewManager

                adapter = menuItemListViewAdapter

            }

        } else {
            Log.d("Menu", "Null")
        }
    }

    override fun onBackPressed(): Unit = finish()

    companion object {
        const val ARG_PARAM = "Facility_Object"
    }
}