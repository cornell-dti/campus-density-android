package org.cornelldti.density.density.facilitydetail

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
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.facility_info_page.*
import org.cornelldti.density.density.BaseActivity
import org.cornelldti.density.density.DensityApplication
import org.cornelldti.density.density.R
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.data.MenuClass
import org.cornelldti.density.density.data.OperatingHoursClass
import org.cornelldti.density.density.facilitydetail.feedback.FeedbackDialogFragment
import org.cornelldti.density.density.util.FluxUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class holds the detail page activity for each individual dining location.
 * Display: Menus, waitTimes, and availabilities.
 * Authentication takes place in BaseActivity.
 */
class FacilityInfoPage : BaseActivity() {

    private lateinit var menuItemList: RecyclerView
    private lateinit var menuItemListViewAdapter: RecyclerView.Adapter<*>
    private lateinit var menuItemListViewManager: RecyclerView.LayoutManager
    private lateinit var cafeMenuItemListViewAdapter: RecyclerView.Adapter<*>
    private lateinit var cafeMenuItemListViewManager: RecyclerView.LayoutManager
    private lateinit var feedback: TextView
    private lateinit var waitTimes: TextView

    /**
     * Ordered list of available meals (e.g., ["breakfast", "lunch", "dinner"]
     */
    private var availableMenus: List<String> = listOf()

    private var currentMenu: MenuClass? = null
    private var facilityClass: FacilityClass? = null
    private var wasCheckedDay: Int = -1
    private var waitTimesValue: Int? = 0
    private var receivedWait: Any? = null
    private var selectedDay: String = FluxUtil.dayString
    private var hoursTimeStampsList: OperatingHoursClass? = null

    private val maxCapacity: Int = 100
    private val months = listOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.facility_info_page)

        val b = intent.extras
        if (b != null) {
            facilityClass = b.getSerializable(ARG_PARAM) as FacilityClass
            receivedWait = b.get("waitTimes")
        }

        topBar.title = facilityClass!!.name
        topBar.setNavigationOnClickListener { onBackPressed() }

        setFeedbackOnClickListener()

        dayChips.isVisible = false
        setAvailability()
        setToday(FluxUtil.dayString)
        setDayChipsDate()
        setDayChipOnClickListener()
        setDataLastUpdated()
        setOnTabSelectedListener()
        setWaitTimes()
    }

    /**
     * This function displays the wait time for the facility.
     */
    private fun setWaitTimes() {
        waitTimes = findViewById(R.id.waitTimes)
        if (facilityClass!!.isOpen) {
            if (receivedWait != null) {
                waitTimesValue = receivedWait.toString().toInt()
                waitTimes.text = "$waitTimesValue min. wait"
            } else {
                waitTimes.text = "Unknown wait"
            }
        } else {
            waitTimes.text = ""
        }

    }

    /**
     * This function sets a listener to open the detail page feedback.
     */
    private fun setFeedbackOnClickListener() {
        feedback = findViewById(R.id.accuracy)
        feedback.setOnClickListener {
            val feedbackDialogFragment = FeedbackDialogFragment()
            feedbackDialogFragment.show(supportFragmentManager, "FeedbackDialogFragment")
        }
    }

    /**
     * This function calculates and sets text for "Last Updated."
     */
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

    /**
     * This function sets the text and color for the availability card.
     */
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

    /**
     * This function sets each day of the week from today, and fetches menu accordingly
     */
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

    /**
     * This function sets the chips (menu) for each day of the week from today
     */
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
     * This function fetches the operating hours for each dining facility, and indicates availability
     */
    private fun fetchOperatingHours(date: Date) {
        val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.DATE, 1)
        val nextDay: Date = c.time
        api.facilityHours(facilityId = facilityClass!!.id, startDate = FluxUtil.convertDateObjectToString(date),
                endDate = FluxUtil.convertDateObjectToString(nextDay),
                facilityHoursTimeStampsOnResponse = { hoursTimeStampsList ->
                    this.hoursTimeStampsList = hoursTimeStampsList
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

    /**
     * This function fetches operating hours and menu after user authentication.
     * This function overrides updateUI in BaseActivity.
     */
    override fun updateUI() {
        Log.d("updatedFPUI", "updating")
        fetchOperatingHours(date = FluxUtil.getCurrentDateObject()) // TODO FIX!! ON REFRESH!!
        fetchMenuJSON(FluxUtil.getCurrentDate(), facilityClass!!.id)
    }

    /**
     * This function fetches dining location's menu and displays on each tabs.
     * If empty, tabs are invisible.
     */
    private fun fetchMenuJSON(day: String, facilityId: String) {
        api.fetchMenuJSON(
                day = day,
                facilityId = facilityId
        ) { menu ->
            // set the current menu to the fetched menu
            currentMenu = menu

            // once loaded, hide the loader
            menuProgressBar.isGone = true

            if (currentMenu?.facilityType == "dining-hall") {

                dayChips.isVisible = true
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

            } else {
                if (hoursTimeStampsList != null) {
                    val startHour = FluxUtil.parseTime(hoursTimeStampsList!!.todayOperatingHours[0].first)
                    val endHour = FluxUtil.parseTime(hoursTimeStampsList!!.todayOperatingHours[0].second)
                    menuHours.text = startHour + " - " + endHour
                    clock_image.visibility = View.VISIBLE
                } else {
                    menuHours.visibility = View.GONE
                    clock_image.visibility = View.GONE
                }
                cafeMenuItemListViewManager = LinearLayoutManager(this)
                cafeMenuItemListViewAdapter = CafeMenuListAdapter(menu!!.cafeMenuItems, this)
                menuItemList = findViewById<RecyclerView>(R.id.menuItemsList).apply {
                    setHasFixedSize(true)
                    layoutManager = cafeMenuItemListViewManager
                    adapter = cafeMenuItemListViewAdapter
                }
            }
        }
    }

    /**
     * This function sets a listener for tabs to select and show each day's available menu
     */
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

    /**
     * This function gets menu list from the adapter and displays on layout if available.
     */
    private fun showMenu(menu: MenuClass?, mealOfDay: String) {
        if (menu != null) {
            menuItemListViewManager = LinearLayoutManager(this)
            menuItemListViewAdapter = when (mealOfDay) {
                "breakfast" -> {
                    defaultMenuText.isVisible = menu.breakfastItems.isEmpty()
                    MenuListAdapter(menu.breakfastItems, this)
                }
                "brunch" -> {
                    defaultMenuText.isVisible = menu.brunchItems.isEmpty()
                    MenuListAdapter(menu.brunchItems, this)
                }
                "lunch" -> {
                    defaultMenuText.isVisible = menu.lunchItems.isEmpty()
                    MenuListAdapter(menu.lunchItems, this)
                }
                "dinner" -> {
                    defaultMenuText.isVisible = menu.dinnerItems.isEmpty()
                    MenuListAdapter(menu.dinnerItems, this)
                }
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
                menuHours.visibility = View.VISIBLE
                clock_image.visibility = View.VISIBLE
            } else {
                menuHours.visibility = View.GONE
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

    override fun onBackPressed(): Unit = finish()

    companion object {
        const val ARG_PARAM = "Facility_Object"
    }
}