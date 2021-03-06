package org.cornelldti.density.density.facilitydetail

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
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
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.facility_info_page.*
import org.cornelldti.density.density.BaseActivity
import org.cornelldti.density.density.DensityApplication
import org.cornelldti.density.density.R
import org.cornelldti.density.density.colorbarutil.ColorBarChartRenderer
import org.cornelldti.density.density.colorbarutil.ColorBarDataSet
import org.cornelldti.density.density.colorbarutil.ColorBarMarkerView
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.data.MenuClass
import org.cornelldti.density.density.data.OperatingHoursClass
import org.cornelldti.density.density.facilitydetail.feedback.FeedbackDialogFragment
import org.cornelldti.density.density.util.FluxUtil
import org.cornelldti.density.density.util.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FacilityInfoPage : BaseActivity() {

    private var selectedDay: String = FluxUtil.dayString

    private val months = listOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")

    private val maxCapacity: Int = 100

    private lateinit var feedback: TextView

    /**
     * Ordered list of available meals (e.g., ["breakfast", "lunch", "dinner"]
     */
    private var availableMenus: List<String> = listOf()

    /**
     * Current menu of the day
     */
    private var currentMenu: MenuClass? = null

    private lateinit var menuItemList: RecyclerView
    private lateinit var menuItemListViewAdapter: RecyclerView.Adapter<*>
    private lateinit var menuItemListViewManager: RecyclerView.LayoutManager

    private var facilityClass: FacilityClass? = null

    private var wasCheckedDay: Int = -1

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

        setFeedbackOnClickListener()

        setAvailability()
        setToday(FluxUtil.dayString)
        setDayChipsDate()
        setDayChipOnClickListener()
        setDataLastUpdated()
        setOnTabSelectedListener()
    }

    private fun setFeedbackOnClickListener() {
        feedback = findViewById(R.id.accuracy)
        feedback.setOnClickListener {
            val feedbackDialogFragment = FeedbackDialogFragment()
            feedbackDialogFragment.show(supportFragmentManager, "FeedbackDialogFragment")
        }
    }

    private fun setDataLastUpdated() {
        val currDate = FluxUtil.getCurrentDateObject()
        val timeZone = Calendar.getInstance().timeZone
        var format = SimpleDateFormat("h:mma", Locale.US)
        if (DateFormat.is24HourFormat(DensityApplication.getAppContext())) {
            format = SimpleDateFormat("HH:mm", Locale.US)
        }
        format.timeZone = timeZone
        lastUpdated.setText("Last updated " + months[currDate.month] + " " + currDate.date + ", " + format.format(currDate).toLowerCase(Locale.US))
    }

    // TODO: complete or remove
    private fun refreshFacilityOccupancy(fac: FacilityClass): FacilityClass {
        api.singleFacilityOccupancy(fac.id)
        return fac.setOccupancyRating(super.facilityOccupancyRating)
    }

    private fun setAvailability() {
        max_capacity.text = getString(R.string.max_capacity, maxCapacity)
        when (facilityClass!!.densityResId) {
            R.string.closed -> {
                availability_num.text = getString(R.string.closed)
                accessibility_icon.isGone = true
            }
            R.string.very_empty -> {
                availability_num.text = getString(R.string.availability_lt, (0.26 * this.maxCapacity).toInt())
                availability_card.setBackgroundColor(ContextCompat.getColor(this, R.color.very_empty))
            }
            R.string.pretty_empty -> {
                availability_num.text = getString(R.string.availability_range, (0.26 * this.maxCapacity).toInt(), (0.5 * this.maxCapacity).toInt())
                availability_card.setBackgroundColor(ContextCompat.getColor(this, R.color.pretty_empty))
            }
            R.string.pretty_crowded -> {
                availability_num.text = getString(R.string.availability_range, (0.5 * this.maxCapacity).toInt(), (0.85 * this.maxCapacity).toInt())
                availability_card.setBackgroundColor(ContextCompat.getColor(this, R.color.pretty_crowded))
            }
            R.string.very_crowded -> {
                availability_num.text = getString(R.string.availability_gt, (0.85 * this.maxCapacity).toInt())
                availability_card.setBackgroundColor(ContextCompat.getColor(this, R.color.very_crowded))
            }
        }
    }

    private fun setDayChipOnClickListener() {
        dayChips.setOnCheckedChangeListener { _, checkedId ->
            setDay(checkedId)
        }
    }

    private fun setDay(checkedId: Int) {
        val day = when (checkedId) {
            R.id.sun -> getString(R.string.SUN)
            R.id.mon -> getString(R.string.MON)
            R.id.tue -> getString(R.string.TUE)
            R.id.wed -> getString(R.string.WED)
            R.id.thu -> getString(R.string.THU)
            R.id.fri -> getString(R.string.FRI)
            R.id.sat -> getString(R.string.SAT)
            else -> FluxUtil.dayString
        }
        if (checkedId != -1 && wasCheckedDay != checkedId) {
            wasCheckedDay = checkedId
            selectedDay = day
            val daysDifference = FluxUtil.getDayDifference(FluxUtil.dayString, selectedDay)
            fetchMenuJSON(day = FluxUtil.getDateStringDaysAfter(daysDifference), facilityId = facilityClass!!.id)
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
        colors.add(ContextCompat.getColor(applicationContext, R.color.light_grey))

        dataSet.colors = colors
        dataSet.valueTextColor = Color.DKGRAY
        dataSet.valueFormatter = ValueFormatter

        val data = BarData(dataSet)
        data.setValueTextSize(13f)
        // adjusts the width of the data bars
        data.barWidth = 1f

        val is24 = DateFormat.is24HourFormat(applicationContext)
        val xAxis = ArrayList<String>()

        val timeFormat24 = SimpleDateFormat("HH:mm", Locale.US)
        val timeFormat = SimpleDateFormat("ha", Locale.US)

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.AM_PM, Calendar.AM)

        for (i in 1..17) {
            if (i % 3 == 0) {
                xAxis.add(
                        if (is24) timeFormat24.format(calendar.time)
                        else timeFormat.format(calendar.time)
                )
                calendar.add(Calendar.HOUR, 3)
            } else {
                xAxis.add("")
            }
        }

        densityChart.description.isEnabled = false
        densityChart.legend.isEnabled = false
        densityChart.setScaleEnabled(false)
        densityChart.setTouchEnabled(true)

        // sets gap between graph and the x-axis
        densityChart.axisLeft.spaceBottom = 0.5f
        densityChart.axisLeft.isEnabled = false
        densityChart.axisRight.isEnabled = false

        densityChart.xAxis.setDrawGridLines(false)
        densityChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        densityChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxis)
        densityChart.xAxis.labelCount = xAxis.size
        densityChart.xAxis.textColor = ContextCompat.getColor(this, R.color.dark_grey)
        densityChart.xAxis.textSize = 10f
        densityChart.xAxis.yOffset = 10f
        densityChart.xAxis.axisLineWidth = 1.5f
        densityChart.xAxis.axisLineColor = ContextCompat.getColor(this, R.color.mid_grey)

        if (!isClosed) {
            // allows rounded bars on graph
            densityChart.renderer = ColorBarChartRenderer(densityChart, densityChart.animator, densityChart.viewPortHandler)
            val marker = ColorBarMarkerView(applicationContext, R.layout.marker_layout)
            densityChart.marker = marker
            densityChart.data = data
        } else {
            densityChart.data = null
            val p = densityChart.getPaint(Chart.PAINT_INFO)
            p.textSize = 36f
            p.color = Color.BLACK
            p.isFakeBoldText = true
            densityChart.setNoDataText("Closed")
        }
        densityChart.invalidate()
        densityChart.animateY(400)

    }

    private fun fetchOperatingHours(date: Date) {
        val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.DATE, 1)
        val nextDay: Date = c.time
        api.facilityHours(facilityId = facilityClass!!.id, startDate = FluxUtil.convertDateObjectToString(date),
                endDate = FluxUtil.convertDateObjectToString(nextDay),
                facilityHoursTimeStampsOnResponse = { hoursTimeStampsList ->
                    setOpenOrClosedOnToolbar(hoursTimeStampsList)
                }
        )
    }

    /**
     * This function uses the opHoursTimestamps class variable in order to determine if the facility is open or closed
     * given the current time. It also keeps track of when it is open until, if open and when it opens next, if closed.
     */
    private fun setOpenOrClosedOnToolbar(opHoursTimestamps: OperatingHoursClass) {
        var isOpen = false
        var openUntil: Long = -1L
        var opensNext: Long = -1L

        val currentTime = System.currentTimeMillis() / 1000L
        val todayOperatingHours = opHoursTimestamps.todayOperatingHours
        val tomorrowFirstOpHours = opHoursTimestamps.tomorrowFirstOpHours

        if (todayOperatingHours.isNotEmpty()) {
            // Current Time before first time slot of day
            if (currentTime < todayOperatingHours[0].first) {
                opensNext = todayOperatingHours[0].first
                openUntil = -1L
            }
            // Current Time is after the last time slot of day and there is hours tomorrow
            else if (tomorrowFirstOpHours.first != -1L && tomorrowFirstOpHours.second != -1L
                    && currentTime >= todayOperatingHours[todayOperatingHours.size - 1].second) {
                opensNext = tomorrowFirstOpHours.first
                openUntil = -1L
            }
            // Current Time is after the last time slot of day and there are no hours tomorrow
            else if (tomorrowFirstOpHours.first == -1L && tomorrowFirstOpHours.second == -1L &&
                    currentTime >= todayOperatingHours[todayOperatingHours.size - 1].second) {
                opensNext = -1L
                openUntil = -1L
            } else {
                for (i in 0 until todayOperatingHours.size) {
                    // Current Time falls into one of the open time slots
                    if (currentTime >= todayOperatingHours[i].first && currentTime < todayOperatingHours[i].second) {
                        isOpen = true
                        openUntil = todayOperatingHours[i].second
                        opensNext = -1L
                    }
                    // Current Time is between two time slots in day
                    else if (i > 0 && currentTime >= todayOperatingHours[i - 1].second && currentTime < todayOperatingHours[i].first) {
                        opensNext = todayOperatingHours[i].first
                        openUntil = -1L
                    }
                }
            }
        }
        // NO OP HOURS TODAY, CHECK TOMORROW!
        else {
            // This is the case that there are hours tomorrow
            if (tomorrowFirstOpHours.first != -1L && tomorrowFirstOpHours.second != -1L) {
                opensNext = tomorrowFirstOpHours.first
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

        val dayChipList = arrayListOf<RadioButton>(sun, mon, tue, wed, thu, fri, sat)
        dayChips.removeAllViews()

        val todayInt = FluxUtil.dayInt
        for (i in todayInt..(todayInt + 6)) {
            dayChips.addView(dayChipList[i % 7])
        }
        dayChipList[todayInt].isChecked = true
        wasCheckedDay = dayChips.checkedRadioButtonId
    }

    override fun updateUI() {
        Log.d("updatedFPUI", "updating")
        fetchOperatingHours(date = FluxUtil.getCurrentDateObject()) // TODO FIX!! ON REFRESH!!
        fetchMenuJSON(FluxUtil.getCurrentDate(), facilityClass!!.id)
    }

    private fun fetchMenuJSON(day: String, facilityId: String) {
        api.fetchMenuJSON(
                day = day,
                facilityId = facilityId
        ) { menu ->
            // set the current menu to the fetched menu
            currentMenu = menu

            // once loaded, hide the loader
            menuProgressBar.isGone = true

            val dayAvailableMenus = mutableListOf<String>()
            val dayAvailableMenuTabs = mutableListOf<TabLayout.Tab>()

            if (menu?.breakfastItems?.size != 0) {
                dayAvailableMenuTabs.add(menuTabs.newTab().setText(R.string.breakfast))
                dayAvailableMenus.add("breakfast")
            }
            if (menu?.brunchItems?.size != 0) {
                dayAvailableMenuTabs.add(menuTabs.newTab().setText(R.string.brunch))
                dayAvailableMenus.add("brunch")
            }
            if (menu?.lunchItems?.size != 0) {
                dayAvailableMenuTabs.add(menuTabs.newTab().setText(R.string.lunch))
                dayAvailableMenus.add("lunch")
            }
            if (menu?.dinnerItems?.size != 0) {
                dayAvailableMenuTabs.add(menuTabs.newTab().setText(R.string.dinner))
                dayAvailableMenus.add("dinner")
            }

            if (menu?.breakfastItems?.isEmpty() == true
                    && menu.brunchItems.isEmpty()
                    && menu.lunchItems.isEmpty()
                    && menu.dinnerItems.isEmpty()) {
                menuTabs.isGone = true
                defaultMenuText.isVisible = true
                showMenu(menu, "")
            } else {
                menuTabs.isVisible = true
                defaultMenuText.isGone = true

                var lastSelectedMeal = ""
                if (availableMenus.isNotEmpty())
                    lastSelectedMeal = availableMenus[menuTabs.selectedTabPosition]

                availableMenus = dayAvailableMenus
                menuTabs.removeAllTabs()
                for (tab in dayAvailableMenuTabs)
                    menuTabs.addTab(tab)

                var selectedMealIndex = availableMenus.indexOf(lastSelectedMeal)

                if (selectedMealIndex == -1)
                    selectedMealIndex = 0
                menuTabs.selectTab(menuTabs.getTabAt(selectedMealIndex))
                showMenu(menu, availableMenus[selectedMealIndex])
            }
        }
    }

    private fun setOnTabSelectedListener() {
        val listener = object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Handle tab select
                if (availableMenus.isNotEmpty()) {
                    showMenu(currentMenu, availableMenus[menuTabs.selectedTabPosition])
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        }

        menuTabs.addOnTabSelectedListener(listener)
    }

    private fun showMenu(menu: MenuClass?, mealOfDay: String) {
        if (menu != null) {
            menuItemListViewManager = LinearLayoutManager(this)
            menuItemListViewAdapter = when (mealOfDay) {
                "breakfast" -> MenuListAdapter(menu.breakfastItems, this)
                "brunch" -> MenuListAdapter(menu.brunchItems, this)
                "lunch" -> MenuListAdapter(menu.lunchItems, this)
                "dinner" -> MenuListAdapter(menu.dinnerItems, this)
                else -> MenuListAdapter(listOf(), this)
            }

            menuItemList = findViewById<RecyclerView>(R.id.menuItemsList).apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                layoutManager = menuItemListViewManager

                adapter = menuItemListViewAdapter
            }

            // This is where the operating hours for the selected meal of day is set!
            if (availableMenus.isNotEmpty() && menu.operatingHours.isNotEmpty()) {
                menuHours.text = menu.operatingHours[availableMenus.indexOf(mealOfDay)]
                clock_image.visibility = View.VISIBLE
            } else {
                menuHours.text = ""
                clock_image.visibility = View.GONE
            }

        } else {
            Log.d("Menu", "Null")
        }
    }

    fun getFacilityName(): String {
        return facilityClass!!.name
    }

    fun getCampusLocation(): String {
        return facilityClass!!.id
    }

    fun getPredictedDensity(): Int {
        return facilityClass!!.occupancyRating
    }

    override fun onBackPressed(): Unit = finish()

    companion object {
        const val ARG_PARAM = "Facility_Object"
    }
}