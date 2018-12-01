package org.cornelldti.density.density;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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

    private RecyclerView.LayoutManager layoutManager;

    private ChipGroup filterChips;

    private Chip all;

    private ArrayList<Facility> all_facilities;

    private ArrayList<Facility> filtered_fac;

    private SearchView searchBar;

    private RequestQueue queue;

    private static final String TOKEN_REQUEST_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/authv1";

    private static final String FACILITY_LIST_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/facilityList";

    private static final String FACILITY_INFO_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/facilityInfo";

    private static final String HOW_DENSE_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/howDense";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        facilities = findViewById(R.id.facilities);
        spinner = findViewById(R.id.progressBar);

        setupSearchQuery();

        queue = Volley.newRequestQueue(this);

        facilities.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        facilities.setLayoutManager(layoutManager);

        pref = getPreferences(Context.MODE_PRIVATE);

        if (!pref.contains("auth_token")) {
            requestToken();
        }
        else {
            fetchFacilities(false);
        }

    }

    private void requestToken() {
        JsonObjectRequest tokenRequest = new JsonObjectRequest
                (Request.Method.PUT, TOKEN_REQUEST_ENDPOINT, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String token = response.getString("token");
                            Log.d("TOKEN", token);
                            pref.edit().putString("auth_token", token).commit();
                            fetchFacilities(false);
                        }
                        catch (JSONException e)
                        {
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
                headers.put("x-api-key", InstanceID.getInstance(MainActivity.this).getId());
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
                filtered_fac = all_facilities;
                adapter = new FacilitiesListAdapter(filtered_fac);
                facilities.setAdapter(adapter);
                break;

            case R.id.north:
                filtered_fac = getFiltered_fac(Facility.campus_location.NORTH);
                adapter = new FacilitiesListAdapter(filtered_fac);
                facilities.setAdapter(adapter);
                break;

            case R.id.west:
                filtered_fac = getFiltered_fac(Facility.campus_location.WEST);
                adapter = new FacilitiesListAdapter(filtered_fac);
                facilities.setAdapter(adapter);
                break;

            case R.id.central:
                filtered_fac = getFiltered_fac(Facility.campus_location.CENTRAL);
                adapter = new FacilitiesListAdapter(filtered_fac);
                facilities.setAdapter(adapter);
                break;

            case -1:
                all.setChecked(true);
        }
    }

    private ArrayList<Facility> getFiltered_fac(Facility.campus_location location) {
        ArrayList<Facility> filtered_list = new ArrayList<>();
        for (Facility f : all_facilities) {
            if (f.getLocation().equals(location)) {
                filtered_list.add(f);
            }
        }
        return filtered_list;
    }

    private void setupSearchQuery() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchBar = findViewById(R.id.search);
        searchBar.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchBar.setMaxWidth(Integer.MAX_VALUE);

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.getFilter().filter(query);
                return false;
            }
        });
    }

    /**
     * Implement this TODO
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
                                        f_list.set(i, f_list.get(i).setLocation(obj.getString("campusLocation")).setOpen(isOpen(obj)));
                                    }
                                }
                            }
                            ArrayList<Facility> f_list_updated = new ArrayList<Facility>();
                            for(int i = 0; i < f_list.size(); i++)
                            {
                                Facility f = f_list.get(i);
                                if(f.isOpen())
                                {
                                    f_list_updated.add(f);
                                }
                            }
                            fetchFacilityOccupancy(f_list_updated, refresh);
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

    private boolean isOpen(JSONObject facility)
    {
        long currentTime = System.currentTimeMillis() / 1000L;
        try {
            JSONArray dailyHours = facility.getJSONArray("dailyHours");
            for(int i = 0; i < dailyHours.length(); i++)
            {
                if(currentTime >= dailyHours.getJSONObject(i).getLong("startTimestamp")
                        && currentTime < dailyHours.getJSONObject(i).getLong("endTimestamp"))
                {
                    return true;
                }
            }
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private void fetchFacilityOccupancy(final ArrayList<Facility> list, final boolean refresh)
    {
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
                            filtered_fac = all_facilities;

                            if(!refresh) {
                                MainActivity.this.adapter = new FacilitiesListAdapter(MainActivity.this.filtered_fac);
                                MainActivity.this.adapter.setOnItemClickListener(new FacilitiesListAdapter.ClickListener() {
                                    @Override
                                    public void onItemClick(int position, View v) {
                                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                        Facility_Page dialog = Facility_Page.newInstance(filtered_fac.get(position));
                                        dialog.show(ft, "facility page");
                                    }
                                });

                                MainActivity.this.facilities.setAdapter(adapter);
                                MainActivity.this.spinner.setVisibility(View.GONE);
                                MainActivity.this.facilities.setVisibility(View.VISIBLE);
                                setChipOnClickListener();
                                setOnRefreshListener();
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("ON", "FRAGMENT INTERACTION");
    }
}
