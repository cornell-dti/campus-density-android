package org.cornelldti.density.density.facilities

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.VolleyError
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.eatery_facilities_fragment.*
import org.cornelldti.density.density.BaseFragment
import org.cornelldti.density.density.LockableAppBarLayoutBehavior
import org.cornelldti.density.density.R
import org.cornelldti.density.density.data.FacilityClass
import org.cornelldti.density.density.facilitydetail.FacilityInfoPage
import kotlin.math.absoluteValue


class EateryFacilitiesFragment: BaseFragment() {

    private var adapter: FacilitiesListAdapter? = null

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var appbar: AppBarLayout
    private lateinit var facilities: RecyclerView

    private var layoutManager: RecyclerView.LayoutManager? = null

    private lateinit var filterChips: ChipGroup

    private var all: Chip? = null
    private var wasChecked: Int = 0

    private val facilitiesScroll: Float = 0.toFloat()

    private var searchView: SearchView? = null

    private var loaded: Boolean = false

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.eatery_facilities_fragment, container, false)
        loaded = false

        setHasOptionsMenu(true);

        swipeRefresh = v.findViewById(R.id.swipeRefresh)
        nestedScrollView = v.findViewById(R.id.nestedScrollView)
        appbar = v.findViewById(R.id.appbar)
        facilities = v.findViewById(R.id.facilities)
        filterChips = v.findViewById(R.id.filterChips)

        setOnRefreshListener()

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
                    appbar.elevation = elevation
                } else {
                    appbar.elevation = 0f
                }
            }
        }

        swipeRefresh.isNestedScrollingEnabled = true

//        activity!!.actionBar!!.setDisplayShowTitleEnabled(false)
        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appbar, verticalOffset ->
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
                appbar.elevation = elevation
            } else {
                appbar.elevation = 0f
            }
        })

        facilities.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this.context)
        facilities.layoutManager = layoutManager
        return v
    }

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

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater): Unit {
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
                if (appbar != null) {
                    swipeRefresh.isEnabled = false
                    appbar.setExpanded(false)
                    val layoutParams = appbar.layoutParams as CoordinatorLayout.LayoutParams
                    (layoutParams.behavior as LockableAppBarLayoutBehavior).lockScroll()
                }

                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                if (appbar != null) {
                    swipeRefresh.isEnabled = true
                    appbar.setExpanded(false)
                    val layoutParams = appbar.layoutParams as CoordinatorLayout.LayoutParams
                    (layoutParams.behavior as LockableAppBarLayoutBehavior).unlockScroll()
                }

                return true
            }
        })

        super.onCreateOptionsMenu(menu, menuInflater)
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
            list: List<FacilityClass>,
            refresh: Boolean,
            success: () -> Unit
    ) {
        if (!refresh) {
            val adapter = FacilitiesListAdapter(list)
            adapter.setOnItemClickListener(object : FacilitiesListAdapter.ClickListener {
                override fun onItemClick(position: Int, v: View) {
                    val facilityInfoPage = FacilityInfoPage()
                    val b = Bundle()
                    b.putSerializable(FacilityInfoPage.ARG_PARAM, adapter.dataSet!![position])
                    facilityInfoPage.arguments = b
                    fragmentManager!!.beginTransaction()
                            .replace(R.id.full_layout, facilityInfoPage, facilityInfoPage.tag)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }
            })
            this.adapter = adapter
            this.facilities.adapter = adapter
            progressBar.visibility = View.GONE
            this.facilities.visibility = View.VISIBLE
            success()
            setChipOnClickListener()
        } else {
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
}