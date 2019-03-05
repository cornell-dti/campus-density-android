package org.cornelldti.density.density;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.iid.InstanceID;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.arch.core.util.Function;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements FacilityPage.OnFragmentInteractionListener {

    private SharedPreferences pref;

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

    private ArrayList<Facility> all_facilities;

    private RequestQueue queue;

    private float facilitiesScroll;

    private static final String TOKEN_REQUEST_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/authv1";
    private static final String FACILITY_LIST_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/facilityList";
    private static final String FACILITY_INFO_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/facilityInfo";
    private static final String HOW_DENSE_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/howDense";
    private NestedScrollView nestedScrollView;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        facilities = findViewById(R.id.facilities);

        spinner = findViewById(R.id.progressBar);

        swipeRefresh = findViewById(R.id.swipe_refresh);

        appBarLayout = findViewById(R.id.appbar);
        toolbar = findViewById(R.id.toolbar);

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

        queue = Volley.newRequestQueue(this);

        facilities.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        facilities.setLayoutManager(layoutManager);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!pref.contains("auth_token")) {
            requestToken();
        } else {
            fetchFacilities(false, success -> null);
        }
    }

    private void requestToken() {
        final String instanceId = InstanceID.getInstance(MainActivity.this).getId();
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("platform", "android");
            requestBody.put("receipt", "");
            requestBody.put("instanceId", instanceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest tokenRequest = new JsonObjectRequest
                (Request.Method.PUT, TOKEN_REQUEST_ENDPOINT, requestBody, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String token = response.getString("token");
                            Log.d("TOKEN", token);
                            pref.edit().putString("auth_token", token).commit();
                            fetchFacilities(false, (success) -> null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Please check internet connection", Toast.LENGTH_LONG).show();
                        Log.d("ERROR MESSAGE", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + getString(R.string.auth_key));
                headers.put("x-api-key", instanceId);
                return headers;
            }
        };
        queue.add(tokenRequest);
    }

    private void setOnRefreshListener() {
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                fetchFacilities(true, (success) -> {
                    swipeRefresh.setRefreshing(false);
                    return null;
                });
                handleCheckChange(filterChips.getCheckedChipId());
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
                Log.d("SIZEALL", String.valueOf(adapter.getItemCount()));
                break;

            case R.id.north:
                adapter.filterFacilitiesByLocation(Facility.CampusLocation.NORTH);
                Log.d("SIZENORTH", String.valueOf(adapter.getItemCount()));
                break;

            case R.id.west:
                adapter.filterFacilitiesByLocation(Facility.CampusLocation.WEST);
                Log.d("SIZEWEST", String.valueOf(adapter.getItemCount()));
                break;

            case R.id.central:
                adapter.filterFacilitiesByLocation(Facility.CampusLocation.CENTRAL);
                Log.d("SIZECENTRAL", String.valueOf(adapter.getItemCount()));
                break;

            case -1:
                all.setChecked(true);
        }
    }

    /**
     * @return
     */
    private void fetchFacilities(final boolean refresh, Function<Boolean, Void> success) {
        JsonArrayRequest facilityListRequest = new JsonArrayRequest
                (Request.Method.GET, FACILITY_LIST_ENDPOINT, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
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
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Please check internet connection", Toast.LENGTH_LONG).show();
                        Log.d("ERROR", error.toString());
                        success.apply(false);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + getString(R.string.auth_key));
                headers.put("x-api-key", pref.getString("auth_token", ""));
                return headers;
            }
        };
        queue.add(facilityListRequest);
    }

    private void fetchFacilityInfo(final ArrayList<Facility> list, final boolean refresh, Function<Boolean, Void> success) {
        JsonArrayRequest facilityInfoRequest = new JsonArrayRequest
                (Request.Method.GET, FACILITY_INFO_ENDPOINT, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            ArrayList<Facility> f_list = list;
                            for (int i = 0; i < f_list.size(); i++) {
                                for (int x = 0; x < response.length(); x++) {
                                    JSONObject obj = response.getJSONObject(x);
                                    if (obj.getString("id").equals(f_list.get(i).getId())) {
                                        Facility f = f_list.get(i);
                                        if (obj.has("campusLocation")) {
                                            f.setLocation(obj.getString("campusLocation"));
                                        }

                                        if (obj.has("description")) {
                                            f.setDescription(obj.getString("description"));
                                        }

                                        if (obj.has("closingAt")) {
                                            f.setClosingAt(obj.getLong("closingAt"));
                                        }

                                        f_list.set(i, f);
                                    }
                                }
                            }
                            fetchFacilityOccupancy(f_list, refresh, success);
                        } catch (JSONException e) {
                            success.apply(false);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Please check internet connection", Toast.LENGTH_LONG).show();
                        Log.d("ERROR MESSAGE", error.toString());
                        success.apply(false);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + getString(R.string.auth_key));
                headers.put("x-api-key", pref.getString("auth_token", ""));
                return headers;
            }
        };
        queue.add(facilityInfoRequest);
    }

    private void fetchFacilityOccupancy(final ArrayList<Facility> list, final boolean refresh, Function<Boolean, Void> success) {
        JsonArrayRequest facilityOccupancyRequest = new JsonArrayRequest
                (Request.Method.GET, HOW_DENSE_ENDPOINT, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            ArrayList<Facility> f_list = list;
                            for (int i = 0; i < f_list.size(); i++) {
                                for (int x = 0; x < response.length(); x++) {
                                    JSONObject obj = response.getJSONObject(x);
                                    if (obj.getString("id")
                                            .equals(f_list.get(i).getId())) {
                                        f_list.set(i, f_list.get(i).setOccupancy_rating(obj.getInt("density")));
                                    }
                                }
                            }

                            all_facilities = f_list;

                            if (!refresh) {
                                MainActivity.this.adapter = new FacilitiesListAdapter(MainActivity.this.all_facilities);
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
                                setChipOnClickListener();
                                setOnRefreshListener();
                            } else {
                                MainActivity.this.adapter.setDataSet(all_facilities);
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

                        } catch (JSONException e) {
                            success.apply(false);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        success.apply(false);
                        Toast.makeText(MainActivity.this, "Please check internet connection", Toast.LENGTH_LONG).show();
                        Log.d("ERROR MESSAGE", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + getString(R.string.auth_key));
                headers.put("x-api-key", pref.getString("auth_token", ""));
                return headers;
            }
        };
        queue.add(facilityOccupancyRequest);
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
                adapter.getFilter().filter(query);
                Log.d("Size of search results:", String.valueOf(adapter.getItemCount()));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.getFilter().filter(query);
                Log.d("Size of search results:", String.valueOf(adapter.getItemCount()));
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


    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("ON", "FRAGMENT INTERACTION");
    }
}
