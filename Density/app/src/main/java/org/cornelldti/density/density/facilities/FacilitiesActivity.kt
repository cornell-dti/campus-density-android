package org.cornelldti.density.density.facilities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.facilities_activity.*
import org.cornelldti.density.density.BaseActivity
import org.cornelldti.density.density.LockableAppBarLayoutBehavior
import org.cornelldti.density.density.R
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.facilitydetail.FacilityInfoPage
import kotlin.math.absoluteValue

/**
 * This class holds the main page activity that the user will first interact with while using the app.
 * Displays: waitTimes and density for all dining locations.
 * Authentication takes place in BaseActivity.
 */
class FacilitiesActivity : BaseActivity() {

    private lateinit var spinner: ProgressBar
    private lateinit var waitTimesMap: Map<String, Double>

    private var adapter: FacilitiesListAdapter? = null
    private var collapsingToolbarLayout: CollapsingToolbarLayout? = null
    private var appBarLayout: AppBarLayout? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var searchView: SearchView? = null
    private var loaded: Boolean = false

    private var filterChips: ChipGroup? = null
    private var all: Chip? = null
    private var wasChecked: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.facilities_activity)

        loaded = false
        swipeRefresh.isNestedScrollingEnabled = true

        setOnRefreshListener()
        setNestedScrollView()
        setToolbar()

        facilities.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        facilities.layoutManager = layoutManager

        covidPolicyButton.setBackgroundColor(resources.getColor(R.color.dark_grey))
        spinner = findViewById(R.id.progressBar)
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar)
    }

    /**
     * This function handles the collapsing toolbar UI in the main facilities page.
     */
    private fun setNestedScrollView() {
        appBarLayout = findViewById(R.id.appbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                val r = resources

                // TODO Don't recalculate
                val offset = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        5f,
                        r.displayMetrics
                )

                if (scrollY > offset) {
                    val dip = 4f

                    val elevation = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            dip,
                            r.displayMetrics
                    )
                    appBarLayout!!.elevation = elevation
                } else {
                    appBarLayout!!.elevation = 0f
                }
            }
        }
    }

    /**
     * This function handles the toolbar UI in the main facilities page.
     */
    private fun setToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        appBarLayout!!.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val vertOffset = verticalOffset.absoluteValue

            val r = resources

            // TODO Don't recalculate
            val offset = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    5f,
                    r.displayMetrics
            )

            if (vertOffset > offset) {
                val dip = 4f

                val elevation = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dip,
                        r.displayMetrics
                )
                appBarLayout.elevation = elevation
            } else {
                appBarLayout.elevation = 0f
            }
        })
    }

    /**
     * This function handles the swipeRefresh UI in the main facilities page.
     * The swipeRefresh motion triggers to re-fetch facilities.
     */
    private fun setOnRefreshListener() {
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            if (adapter == null) {
                refreshToken()
                fetchFacilities(refresh = false) { swipeRefresh.isRefreshing = false }
            } else {
                fetchFacilities(refresh = true) { swipeRefresh.isRefreshing = false }
                handleCheckChange(filterChips!!.checkedChipId)
            }
        }
    }

    /**
     * This function sets a listener for the location filter chips.
     * Changes made to the chip checked states are handled by handleCheckChange function.
     */
    private fun setChipOnClickListener() {
        all = findViewById(R.id.all)

        filterChips = findViewById(R.id.filterChips)

        filterChips!!.setOnCheckedChangeListener { _, checkedId -> handleCheckChange(checkedId) }
    }

    /**
     * This function handles the changes made to the checked states of chips.
     * Depending on the checked state, this function calls filterFacilitiesByLocation from
     * the adapter to filter the layout.
     */
    private fun handleCheckChange(checkedId: Int) {
        when (checkedId) {
            R.id.all -> {
                adapter!!.showAllLocations()
                wasChecked = R.id.all
                Log.d("SIZEALL", adapter!!.itemCount.toString())
            }

            R.id.north -> {
                adapter!!.filterFacilitiesByLocation(FacilityClass.CampusLocation.NORTH)
                wasChecked = R.id.north
                Log.d("SIZENORTH", adapter!!.itemCount.toString())
            }

            R.id.west -> {
                adapter!!.filterFacilitiesByLocation(FacilityClass.CampusLocation.WEST)
                wasChecked = R.id.west
                Log.d("SIZEWEST", adapter!!.itemCount.toString())
            }

            R.id.central -> {
                adapter!!.filterFacilitiesByLocation(FacilityClass.CampusLocation.CENTRAL)
                wasChecked = R.id.central
                Log.d("SIZECENTRAL", adapter!!.itemCount.toString())
            }

            -1 -> filterChips!!.check(wasChecked)
        }
    }

    /**
     * This function fetches facilities after user authentication.
     * This function overrides updateUI in BaseActivity.
     */
    override fun updateUI() {
        Log.d("updatedMainUI", "success")
        if (adapter == null)
            fetchFacilities(false) { }
        else
            fetchFacilities(true) { }
    }

    /**
     * This function handles the collapsing search-bar UI on the main facilities page.
     * The searchable toolbar filters out the facilities list according to the search query.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.toolbar_search, menu)

        val searchItem = menu.findItem(R.id.action_search)

        searchView = searchItem.actionView as SearchView

        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (adapter != null) adapter!!.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (adapter != null) adapter!!.filter.filter(query)
                return false
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                if (appBarLayout != null) {
                    swipeRefresh.isEnabled = false
                    appBarLayout!!.setExpanded(false)
                    val layoutParams = appBarLayout!!.layoutParams as CoordinatorLayout.LayoutParams
                    (layoutParams.behavior as LockableAppBarLayoutBehavior).lockScroll()
                }

                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                if (appBarLayout != null) {
                    swipeRefresh.isEnabled = true
                    appBarLayout!!.setExpanded(false)
                    val layoutParams = appBarLayout!!.layoutParams as CoordinatorLayout.LayoutParams
                    (layoutParams.behavior as LockableAppBarLayoutBehavior).unlockScroll()
                }

                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * This function fetches a list of estimated wait times for all dining locations.
     * The returned values are real-time and are measured by our waitTimes algorithm.
     */
    private fun fetchWaitTimes() {
        api.fetchWaitTimes(
                onDone = { map ->
                    waitTimesMap = map
                },
                onError = { error ->
                    Log.d("ERROR", error.toString())
                }
        )
    }

    /**
     * This function fetches a list of all dining facilities on campus.
     * The returned list is handled by fetchFacilityOnResponse
     */
    private fun fetchFacilities(refresh: Boolean, success: () -> Unit) {
        api.fetchFacilities(
                success = success,
                onDone = { list ->
                    failurePage.visibility = View.GONE
                    fetchFacilityOnResponse(list = list, refresh = refresh, success = success)
                },
                onError = { error ->
                    success()
                    fetchFacilitiesOnError(error = error)
                }
        )
    }

    /**
     * This function handles the error response from fetchFacilities.
     * If error persists, it prompts the failure page.
     */
    private fun fetchFacilitiesOnError(error: VolleyError) {
        Log.d("ERROR", error.toString())
        val handler = Handler()
        handler.postDelayed({
            // Do something after 10s = 10000ms
            if (adapter == null) {
                failurePage.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
            }
        }, 10000)
    }

    /**
     * This function handles the success response from fetchFacilities.
     * On initial call, it sets up a listener to pass Bundled data to a specific FacilityInfoPage.
     * On refresh, it resets waitTimes, adapter, the filter chips.
     * This function also triggers to fetch waitTimes.
     */
    private fun fetchFacilityOnResponse(
            list: List<FacilityClass>,
            refresh: Boolean,
            success: () -> Unit
    ) {
        if (!refresh) {
            fetchWaitTimes()
            val adapter = FacilitiesListAdapter(list)
            adapter.setOnItemClickListener(object : FacilitiesListAdapter.ClickListener {
                override fun onItemClick(position: Int, v: View) {
                    val intent = Intent(this@FacilitiesActivity, FacilityInfoPage::class.java)
                    val b = Bundle()
                    b.putSerializable(FacilityInfoPage.ARG_PARAM, adapter.dataSet!![position])
                    intent.putExtras(b)
                    intent.putExtra("waitTimes", waitTimesMap.get(adapter.dataSet!![position].id)?.toInt())
                    startActivity(intent)
                }
            })

            this.adapter = adapter
            this.facilities.adapter = adapter
            this.spinner.visibility = View.GONE
            this.facilities.visibility = View.VISIBLE

            success()
            setChipOnClickListener()

        } else {
            fetchWaitTimes()
            val adapter = this.adapter!!
            adapter.setDataSet(list)
            success()

            when (filterChips!!.checkedChipId) {
                R.id.all -> adapter.showAllLocations()
                R.id.north -> adapter.filterFacilitiesByLocation(FacilityClass.CampusLocation.NORTH)
                R.id.west -> adapter.filterFacilitiesByLocation(FacilityClass.CampusLocation.WEST)
                R.id.central -> adapter.filterFacilitiesByLocation(FacilityClass.CampusLocation.CENTRAL)
                -1 -> all!!.isChecked = true
            }
        }
    }

    /**
     * This function creates an intent to open up the COVID-19 dining policies webpage.
     */
    fun openCovidPolicy(v: View?) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://scl.cornell.edu/news-events/news/guide-cornell-dining-fall-2020"))
        startActivity(browserIntent)
    }
}
