package org.cornelldti.density.density;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.arch.core.util.Function;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends BaseActivity {

    private RecyclerView facilities;

    private ProgressBar spinner;

    private SwipeRefreshLayout swipeRefresh;

    private FacilitiesListAdapter adapter;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;

    private Toolbar toolbar;

    private RecyclerView.LayoutManager layoutManager;

    private ChipGroup filterChips;

    private Chip all;
    private int wasChecked;

    private float facilitiesScroll;

    private NestedScrollView nestedScrollView;
    private SearchView searchView;
    private View failurePage, progressBar;

    private boolean loaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loaded = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        facilities = findViewById(R.id.facilities);

        spinner = findViewById(R.id.progressBar);

        swipeRefresh = findViewById(R.id.swipe_refresh);
        setOnRefreshListener();

        appBarLayout = findViewById(R.id.appbar);
        toolbar = findViewById(R.id.toolbar);

        failurePage = findViewById(R.id.failure_page);
        progressBar = findViewById(R.id.progressBar);

        nestedScrollView = findViewById(R.id.nestedScrollView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nestedScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    Resources r = getResources();

                    // TODO Don't recalculate
                    float offset =
                            TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    5f,
                                    r.getDisplayMetrics()
                            );

                    if (scrollY > offset) {
                        float dip = 4f;

                        float elevation =
                                TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        dip,
                                        r.getDisplayMetrics()
                                );
                        appBarLayout.setElevation(elevation);
                    } else {
                        appBarLayout.setElevation(0);
                    }
                }
            });
        }

        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        swipeRefresh.setNestedScrollingEnabled(true);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                verticalOffset = Math.abs(verticalOffset);

                Resources r = getResources();

                // TODO Don't recalculate
                float offset =
                        TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                5f,
                                r.getDisplayMetrics()
                        );

                if (verticalOffset > offset) {
                    float dip = 4f;

                    float elevation =
                            TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    dip,
                                    r.getDisplayMetrics()
                            );
                    appBarLayout.setElevation(elevation);
                } else {
                    appBarLayout.setElevation(0);
                }
            }
        });

        facilities.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        facilities.setLayoutManager(layoutManager);

        fetchFacilities(false, success -> null);
    }

    private void setOnRefreshListener() {
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                if (adapter == null) {
                    refreshToken();
                    fetchFacilities(false, (success) -> {
                        swipeRefresh.setRefreshing(false);
                        return null;
                    });
                } else {
                    fetchFacilities(true, (success) -> {
                        swipeRefresh.setRefreshing(false);
                        return null;
                    });
                    handleCheckChange(filterChips.getCheckedChipId());
                }
            }
        });
    }

    private void setChipOnClickListener() {
        all = findViewById(R.id.all);

        filterChips = findViewById(R.id.filterChips);

        filterChips.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                handleCheckChange(checkedId);
            }
        });
    }

    private void handleCheckChange(int checkedId) {
        switch (checkedId) {
            case R.id.all:
                adapter.showAllLocations();
                wasChecked = R.id.all;
                Log.d("SIZEALL", String.valueOf(adapter.getItemCount()));
                break;

            case R.id.north:
                adapter.filterFacilitiesByLocation(Facility.CampusLocation.NORTH);
                wasChecked = R.id.north;
                Log.d("SIZENORTH", String.valueOf(adapter.getItemCount()));
                break;

            case R.id.west:
                adapter.filterFacilitiesByLocation(Facility.CampusLocation.WEST);
                wasChecked = R.id.west;
                Log.d("SIZEWEST", String.valueOf(adapter.getItemCount()));
                break;

            case R.id.central:
                adapter.filterFacilitiesByLocation(Facility.CampusLocation.CENTRAL);
                wasChecked = R.id.central;
                Log.d("SIZECENTRAL", String.valueOf(adapter.getItemCount()));
                break;

            case -1:
                filterChips.check(wasChecked);
        }
    }

    @Override
    protected void updateUI() {
        Log.d("updatedMainUI", "success");
        if (adapter == null)
            fetchFacilities(false, success -> null);
        else
            fetchFacilities(true, success -> null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null) adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (adapter != null) adapter.getFilter().filter(query);
                return false;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (appBarLayout != null) {
                    swipeRefresh.setEnabled(false);
                    appBarLayout.setExpanded(false);
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                    ((LockableAppBarLayoutBehavior) layoutParams.getBehavior()).lockScroll();
                }

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (appBarLayout != null) {
                    swipeRefresh.setEnabled(true);
                    appBarLayout.setExpanded(false);
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                    ((LockableAppBarLayoutBehavior) layoutParams.getBehavior()).unlockScroll();
                }

                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    // FETCH FUNCTIONS OVERRIDES

    @Override
    public void fetchFacilitiesOnResponse(JSONArray response, boolean refresh, Function<Boolean, Void> success) {
        try {
            failurePage.setVisibility(View.GONE);
            ArrayList<Facility> f = new ArrayList<Facility>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject facility = response.getJSONObject(i);
                f.add(new Facility(facility.getString("displayName"), facility.getString("id")));
            }
            fetchFacilityInfo(f, refresh, success);
        } catch (JSONException e) {
            success.apply(false);
            e.printStackTrace();
        }
    }

    @Override
    public void fetchFacilitiesOnError(VolleyError error, Function<Boolean, Void> success) {
        super.fetchFacilitiesOnError(error, success);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 10s = 10000ms
                if (adapter == null) {
                    failurePage.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        }, 10000);
    }

    public void fetchFacilityOccupancyOnResponse(ArrayList<Facility> list, JSONArray response, boolean refresh, Function<Boolean, Void> success) {
        super.fetchFacilityOccupancyOnResponse(list, response, refresh, success);
        if (!refresh)
        {
            MainActivity.this.adapter = new FacilitiesListAdapter(getAll_facilities());
            MainActivity.this.adapter.setOnItemClickListener(new FacilitiesListAdapter.ClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Intent intent = new Intent(MainActivity.this, FacilityPage.class);
                    Bundle b = new Bundle();
                    b.putSerializable(FacilityPage.ARG_PARAM, adapter.getDataSet().get(position));
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });

            MainActivity.this.facilities.setAdapter(adapter);
            MainActivity.this.spinner.setVisibility(View.GONE);
            MainActivity.this.facilities.setVisibility(View.VISIBLE);
            success.apply(true);
            setChipOnClickListener();
        } else

        {
            MainActivity.this.adapter.setDataSet(getAll_facilities());
            success.apply(true);
            switch (filterChips.getCheckedChipId()) {
                case R.id.all:
                    adapter.showAllLocations();
                    break;

                case R.id.north:
                    adapter.filterFacilitiesByLocation(Facility.CampusLocation.NORTH);
                    break;

                case R.id.west:
                    adapter.filterFacilitiesByLocation(Facility.CampusLocation.WEST);
                    break;

                case R.id.central:
                    adapter.filterFacilitiesByLocation(Facility.CampusLocation.CENTRAL);
                    break;

                case -1:
                    all.setChecked(true);
            }
        }
    }

}
