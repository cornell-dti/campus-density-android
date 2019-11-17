package org.cornelldti.density.density.facilities

import kotlinx.android.synthetic.main.activity_main.*

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar

import com.android.volley.VolleyError
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cornelldti.density.density.BaseActivity
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.LockableAppBarLayoutBehavior
import org.cornelldti.density.density.R
import org.cornelldti.density.density.facilitydetail.FacilityPage
import kotlin.math.absoluteValue

class FacilitiesActivity : BaseActivity() {

    private lateinit var spinner: ProgressBar

    private var adapter: FacilitiesListAdapter? = null

    private var collapsingToolbarLayout: CollapsingToolbarLayout? = null
    private var appBarLayout: AppBarLayout? = null

    private var layoutManager: RecyclerView.LayoutManager? = null

    private var filterChips: ChipGroup? = null

    private var all: Chip? = null
    private var wasChecked: Int = 0

    private val facilitiesScroll: Float = 0.toFloat()

    private var searchView: SearchView? = null

    private var loaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        loaded = false
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinner = findViewById(R.id.progressBar)

        setOnRefreshListener()

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

        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar)
        swipeRefresh.isNestedScrollingEnabled = true

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

        facilities.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this)
        facilities.layoutManager = layoutManager
    }

    private fun fetchFacilities(refresh: Boolean, success: (Boolean) -> Unit) {
        api.fetchFacilities(
                success = success,
                onBasicFacilitiesFetched = { failurePage.visibility = View.GONE },
                onResponse = { list ->
                    fetchFacilityOnResponse(list = list, refresh = refresh, success = success)
                },
                onError = { error ->
                    success(false)
                    fetchFacilitiesOnError(error = error)
                }
        )
    }

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

    private fun setChipOnClickListener() {
        all = findViewById(R.id.all)

        filterChips = findViewById(R.id.filterChips)

        filterChips!!.setOnCheckedChangeListener { _, checkedId -> handleCheckChange(checkedId) }
    }

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

    override fun updateUI() {
        Log.d("updatedMainUI", "success")
        if (adapter == null)
            fetchFacilities(false) { }
        else
            fetchFacilities(true) { }
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

    // FETCH FUNCTIONS OVERRIDES

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

    private fun fetchFacilityOnResponse(
            list: MutableList<FacilityClass>,
            refresh: Boolean,
            success: (Boolean) -> Unit
    ) {
        if (!refresh) {
            val adapter = FacilitiesListAdapter(list)
            adapter.setOnItemClickListener(object : FacilitiesListAdapter.ClickListener {
                override fun onItemClick(position: Int, v: View) {
                    val intent = Intent(this@FacilitiesActivity, FacilityPage::class.java)
                    val b = Bundle()
                    b.putSerializable(FacilityPage.ARG_PARAM, adapter.dataSet!![position])
                    intent.putExtras(b)
                    startActivity(intent)
                }
            })
            this.adapter = adapter
            this.facilities.adapter = adapter
            this.spinner.visibility = View.GONE
            this.facilities.visibility = View.VISIBLE
            success(true)
            setChipOnClickListener()
        } else {
            val adapter = this.adapter!!
            adapter.setDataSet(list)
            success(true)
            when (filterChips!!.checkedChipId) {
                R.id.all -> adapter.showAllLocations()
                R.id.north -> adapter.filterFacilitiesByLocation(FacilityClass.CampusLocation.NORTH)
                R.id.west -> adapter.filterFacilitiesByLocation(FacilityClass.CampusLocation.WEST)
                R.id.central -> adapter.filterFacilitiesByLocation(FacilityClass.CampusLocation.CENTRAL)
                -1 -> all!!.isChecked = true
            }
        }
    }
}
