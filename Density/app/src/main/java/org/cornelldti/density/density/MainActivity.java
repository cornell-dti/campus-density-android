package org.cornelldti.density.density;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements Facility_Page.OnFragmentInteractionListener {

    private SharedPreferences pref;

    private RecyclerView facilities;

    private ProgressBar spinner;

    private SwipeRefreshLayout swipeRefresh;

    private FacilitiesListAdapter adapter;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;

    private Toolbar mToolbar;

    private RecyclerView.LayoutManager layoutManager;


    private ChipGroup filterChips;

    private Chip all;

    private ArrayList<Facility> all_facilities;

    private RequestQueue queue;

    private float facilitiesScroll;

    private static final String TOKEN_REQUEST_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/authv1";
    private static final String FACILITY_LIST_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/facilityList";
    private static final String FACILITY_INFO_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/facilityInfo";
    private static final String HOW_DENSE_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/howDense";
    private NestedScrollView nestedScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        facilities = findViewById(R.id.facilities);

        spinner = findViewById(R.id.progressBar);

        swipeRefresh = findViewById(R.id.swipe_refresh);

        appBarLayout = findViewById(R.id.appbar);
        mToolbar = findViewById(R.id.toolbar);

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

        setSupportActionBar(mToolbar);
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

        pref = getPreferences(Context.MODE_PRIVATE);

        if (!pref.contains("auth_token")) {
            requestToken();
        } else {
            fetchFacilities(false);
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
                            fetchFacilities(false);
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
                fetchFacilities(true);
                handleCheckChange(filterChips.getCheckedChipId());
                swipeRefresh.setRefreshing(false);
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
                adapter.notifyDataSetChanged();
                Log.d("SIZEALL", String.valueOf(adapter.getItemCount()));
                break;

            case R.id.north:
                adapter.filterFacilitiesByLocation(Facility.campus_location.NORTH);
                adapter.notifyDataSetChanged();
                Log.d("SIZENORTH", String.valueOf(adapter.getItemCount()));
                break;

            case R.id.west:
                adapter.filterFacilitiesByLocation(Facility.campus_location.WEST);
                adapter.notifyDataSetChanged();
                Log.d("SIZEWEST", String.valueOf(adapter.getItemCount()));
                break;

            case R.id.central:
                adapter.filterFacilitiesByLocation(Facility.campus_location.CENTRAL);
                adapter.notifyDataSetChanged();
                Log.d("SIZECENTRAL", String.valueOf(adapter.getItemCount()));
                break;

            case -1:
                all.setChecked(true);
        }
    }

    /**
     *
     * @return
     */
    private void fetchFacilities(final boolean refresh) {
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
                            fetchFacilityInfo(f, refresh);
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
                headers.put("x-api-key", pref.getString("auth_token", ""));
                return headers;
            }
        };
        queue.add(facilityListRequest);
    }

    private void fetchFacilityInfo(final ArrayList<Facility> list, final boolean refresh) {
        JsonArrayRequest facilityInfoRequest = new JsonArrayRequest
                (Request.Method.GET, FACILITY_INFO_ENDPOINT, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("RESP", response.toString());
                        try {
                            ArrayList<Facility> f_list = list;
                            for (int i = 0; i < f_list.size(); i++) {
                                for (int x = 0; x < response.length(); x++) {
                                    JSONObject obj = response.getJSONObject(x);
                                    if (obj.getString("id")
                                            .equals(f_list.get(i).getId())) {
                                        f_list.set(i, f_list.get(i).setLocation(obj.getString("campusLocation")));
                                    }
                                }
                            }
                            fetchFacilityOccupancy(f_list, refresh);
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
                headers.put("x-api-key", pref.getString("auth_token", ""));
                return headers;
            }
        };
        queue.add(facilityInfoRequest);
    }

    private void fetchFacilityOccupancy(final ArrayList<Facility> list, final boolean refresh) {
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
                                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                        Facility_Page dialog = Facility_Page.newInstance(adapter.getDataSet().get(position));
                                        dialog.show(ft, "facility page");
                                    }
                                });

                                MainActivity.this.facilities.setAdapter(adapter);
                                MainActivity.this.spinner.setVisibility(View.GONE);
                                MainActivity.this.facilities.setVisibility(View.VISIBLE);
                                setChipOnClickListener();
                                setOnRefreshListener();
                            }
                            else {
                                MainActivity.this.adapter.setDataSet(all_facilities);
                                switch (filterChips.getCheckedChipId()) {
                                    case R.id.all:
                                        adapter.showAllLocations();
                                        adapter.notifyDataSetChanged();
                                        break;

                                    case R.id.north:
                                        adapter.filterFacilitiesByLocation(Facility.campus_location.NORTH);
                                        adapter.notifyDataSetChanged();
                                        break;

                                    case R.id.west:
                                        adapter.filterFacilitiesByLocation(Facility.campus_location.WEST);
                                        adapter.notifyDataSetChanged();
                                        break;

                                    case R.id.central:
                                        adapter.filterFacilitiesByLocation(Facility.campus_location.CENTRAL);
                                        adapter.notifyDataSetChanged();
                                        break;

                                    case -1:
                                        all.setChecked(true);
                                }
                            }

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

        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                Log.d("SIZEE", String.valueOf(adapter.getItemCount()));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.getFilter().filter(query);
                Log.d("SIZEE", String.valueOf(adapter.getItemCount()));
                return false;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (appBarLayout != null) {
                    appBarLayout.setExpanded(false);
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                    ((LockableAppBarLayoutBehavior) layoutParams.getBehavior()).lockScroll();
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (appBarLayout != null) {
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
