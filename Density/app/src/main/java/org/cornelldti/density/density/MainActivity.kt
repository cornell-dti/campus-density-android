package org.cornelldti.density.density

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar

import com.android.volley.VolleyError
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList

import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.arch.core.util.Function
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : BaseActivity() {

    private var facilities: RecyclerView? = null

    private var spinner: ProgressBar? = null

    private var swipeRefresh: SwipeRefreshLayout? = null

    private var adapter: FacilitiesListAdapter? = null

    private var collapsingToolbarLayout: CollapsingToolbarLayout? = null
    private var appBarLayout: AppBarLayout? = null

    private var toolbar: Toolbar? = null

    private var layoutManager: RecyclerView.LayoutManager? = null

    private var filterChips: ChipGroup? = null

    private var all: Chip? = null
    private var wasChecked: Int = 0

    private val facilitiesScroll: Float = 0.toFloat()

    private var nestedScrollView: NestedScrollView? = null
    private var searchView: SearchView? = null
    private var failurePage: View? = null
    private var progressBar: View? = null

    private var loaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        loaded = false
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        facilities = findViewById(R.id.facilities)

        spinner = findViewById(R.id.progressBar)

        swipeRefresh = findViewById(R.id.swipe_refresh)
        setOnRefreshListener()

        appBarLayout = findViewById(R.id.appbar)
        toolbar = findViewById(R.id.toolbar)

        failurePage = findViewById(R.id.failure_page)
        progressBar = findViewById(R.id.progressBar)

        nestedScrollView = findViewById(R.id.nestedScrollView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nestedScrollView!!.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
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

        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar)
        swipeRefresh!!.isNestedScrollingEnabled = true

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        appBarLayout!!.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            var verticalOffset = verticalOffset
            verticalOffset = Math.abs(verticalOffset)

            val r = resources

            // TODO Don't recalculate
            val offset = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    5f,
                    r.displayMetrics
            )

            if (verticalOffset > offset) {
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

        facilities!!.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this)
        facilities!!.layoutManager = layoutManager

        fetchFacilities(false) { }
    }

    private fun setOnRefreshListener() {
        swipeRefresh = findViewById(R.id.swipe_refresh)
        swipeRefresh!!.setOnRefreshListener {
            swipeRefresh!!.isRefreshing = true
            if (adapter == null) {
                refreshToken()
                fetchFacilities(false) { success ->
                    swipeRefresh!!.isRefreshing = false
                }
            } else {
                fetchFacilities(true) { success ->
                    swipeRefresh!!.isRefreshing = false
                }
                handleCheckChange(filterChips!!.checkedChipId)
            }
        }
    }

    private fun setChipOnClickListener() {
        all = findViewById(R.id.all)

        filterChips = findViewById(R.id.filterChips)

        filterChips!!.setOnCheckedChangeListener { group, checkedId -> handleCheckChange(checkedId) }
    }

    private fun handleCheckChange(checkedId: Int) {
        when (checkedId) {
            R.id.all -> {
                adapter!!.showAllLocations()
                wasChecked = R.id.all
                Log.d("SIZEALL", adapter!!.itemCount.toString())
            }

            R.id.north -> {
                adapter!!.filterFacilitiesByLocation(Facility.CampusLocation.NORTH)
                wasChecked = R.id.north
                Log.d("SIZENORTH", adapter!!.itemCount.toString())
            }

            R.id.west -> {
                adapter!!.filterFacilitiesByLocation(Facility.CampusLocation.WEST)
                wasChecked = R.id.west
                Log.d("SIZEWEST", adapter!!.itemCount.toString())
            }

            R.id.central -> {
                adapter!!.filterFacilitiesByLocation(Facility.CampusLocation.CENTRAL)
                wasChecked = R.id.central
                Log.d("SIZECENTRAL", adapter!!.itemCount.toString())
            }

            -1 -> filterChips!!.check(wasChecked)
        }
    }

    override fun updateUI() {
        Log.d("updatedMainUI", "success")
        if (adapter == null)
            fetchFacilities(false) { success -> null }
        else
            fetchFacilities(true) { success -> null }
    }

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
                    swipeRefresh!!.isEnabled = false
                    appBarLayout!!.setExpanded(false)
                    val layoutParams = appBarLayout!!.layoutParams as CoordinatorLayout.LayoutParams
                    (layoutParams.behavior as LockableAppBarLayoutBehavior).lockScroll()
                }

                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                if (appBarLayout != null) {
                    swipeRefresh!!.isEnabled = true
                    appBarLayout!!.setExpanded(false)
                    val layoutParams = appBarLayout!!.layoutParams as CoordinatorLayout.LayoutParams
                    (layoutParams.behavior as LockableAppBarLayoutBehavior).unlockScroll()
                }

                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    // FETCH FUNCTIONS OVERRIDES

    override fun fetchFacilitiesOnResponse(response: JSONArray, refresh: Boolean, success: (Boolean) -> Unit) {
        try {
            failurePage!!.visibility = View.GONE
            val f = ArrayList<Facility>()
            for (i in 0 until response.length()) {
                val facility = response.getJSONObject(i)
                f.add(Facility(facility.getString("displayName"), facility.getString("id")))
            }
            fetchFacilityInfo(f, refresh, success)
        } catch (e: JSONException) {
            success(false)
            e.printStackTrace()
        }

    }

    override fun fetchFacilitiesOnError(error: VolleyError, success: (Boolean) -> Unit) {
        super.fetchFacilitiesOnError(error, success)
        val handler = Handler()
        handler.postDelayed({
            // Do something after 10s = 10000ms
            if (adapter == null) {
                failurePage!!.visibility = View.VISIBLE
                progressBar!!.visibility = View.INVISIBLE
            }
        }, 10000)
    }

    override fun fetchFacilityOccupancyOnResponse(
            list: ArrayList<Facility>,
            response: JSONArray,
            refresh: Boolean,
            success: (Boolean) -> Unit
    ) {
        super.fetchFacilityOccupancyOnResponse(list, response, refresh, success)
        if (!refresh) {
            this@MainActivity.adapter = FacilitiesListAdapter(all_facilities!!)
            this@MainActivity.adapter!!.setOnItemClickListener(object : FacilitiesListAdapter.ClickListener {
                override fun onItemClick(position: Int, v: View) {
                    val intent = Intent(this@MainActivity, FacilityPage::class.java)
                    val b = Bundle()
                    b.putSerializable(FacilityPage.ARG_PARAM, adapter!!.dataSet!![position])
                    intent.putExtras(b)
                    startActivity(intent)
                }
            })

            this@MainActivity.facilities!!.adapter = adapter
            this@MainActivity.spinner!!.visibility = View.GONE
            this@MainActivity.facilities!!.visibility = View.VISIBLE
            success(true)
            setChipOnClickListener()
        } else {
            this@MainActivity.adapter!!.setDataSet(all_facilities!!)
            success(true)
            when (filterChips!!.checkedChipId) {
                R.id.all -> adapter!!.showAllLocations()

                R.id.north -> adapter!!.filterFacilitiesByLocation(Facility.CampusLocation.NORTH)

                R.id.west -> adapter!!.filterFacilitiesByLocation(Facility.CampusLocation.WEST)

                R.id.central -> adapter!!.filterFacilitiesByLocation(Facility.CampusLocation.CENTRAL)

                -1 -> all!!.isChecked = true
            }
        }
    }

}
