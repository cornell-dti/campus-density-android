package org.cornelldti.density.density;

import android.app.SearchManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
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

    private RecyclerView facilities;

    private SwipeRefreshLayout swipeRefresh;

    private FacilitiesListAdapter adapter;

    private RecyclerView.LayoutManager layoutManager;

    private ChipGroup filterChips;

    private Chip all;

    private ArrayList<Facility> all_facilities;

    private ArrayList<Facility> filtered_fac;

    private SearchView searchBar;

    private RequestQueue queue;

    private static final String FACILITY_LIST_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/facilityList";

    private static final String HOW_DENSE_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/howDense";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        facilities = findViewById(R.id.facilities);

        setOnRefreshListener();
        setChipOnClickListener();
        setupSearchQuery();

        queue = Volley.newRequestQueue(this);

        facilities.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        facilities.setLayoutManager(layoutManager);
        fetchFacilities();
        adapter = new FacilitiesListAdapter(filtered_fac);
        adapter.setOnItemClickListener(new FacilitiesListAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
//                Fragment facility_page = Facility_Page.newInstance(filtered_fac.get(position));
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                ft.replace(R.id.main_container, facility_page);
//                ft.commit();
                Facility_Page dialog = Facility_Page.newInstance(filtered_fac.get(position));
                dialog.show(ft, "facility page");
            }
        });
        facilities.setAdapter(adapter);
    }

    private void setOnRefreshListener() {
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                fetchFacilities();
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
    private void fetchFacilities() {
        ArrayList<Facility> f = new ArrayList<Facility>();
        f.add(new Facility("Keeton Dining", getString(R.string.Keeton), "9:00 AM",
                "9:00 PM", "address",
                Facility.campus_location.WEST, 3));
        f.add(new Facility("Libe Cafe", getString(R.string.Libe), "10:00 AM",
                "9:00 PM", "address",
                Facility.campus_location.CENTRAL, 0));
        f.add(new Facility("Bethe Dining", getString(R.string.Bethe), "9:30 AM",
                "11:00 PM", "address",
                Facility.campus_location.WEST, 1));
        f.add(new Facility("RPCC Dining", getString(R.string.RPME), "9:30 AM",
                "11:00 PM", "address",
                Facility.campus_location.NORTH, 2));
        all_facilities = f;
        filtered_fac = all_facilities;
//        JsonArrayRequest firstJsonArrayRequest = new JsonArrayRequest
//                (Request.Method.GET, FACILITY_LIST_ENDPOINT, null, new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        try {
//                            ArrayList<Facility> f = new ArrayList<Facility>();
//                            for(int i = 0; i < response.length();i++)
//                            {
//                                JSONObject facility = response.getJSONObject(i);
//                                f.add(new Facility(facility.getString("displayName"), facility.getString("id")));
//                            }
//                            fetchOccupancies(f);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // NEVER SHOWS THIS
//                        Toast.makeText(MainActivity.this, "Please check internet connection", Toast.LENGTH_LONG).show();
//                        Log.d("ERROR MESSAGE", error.toString());
//                    }
//                }) {
//            @Override
//            public Map<String, String> getHeaders() {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Content-Type", "application/json; charset=utf-8");
//                return headers;
//            }
//        };
//        queue.add(firstJsonArrayRequest);
    }

    private void fetchOccupancies(final ArrayList<Facility> list) {
        JsonArrayRequest secondJsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, HOW_DENSE_ENDPOINT, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            ArrayList<Facility> f_list = list;
                            for (int i = 0; i < f_list.size(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                f_list.set(i, f_list.get(i).setOccupancy_rating(obj.getInt("density")));
                            }
                            all_facilities = f_list;
                            filtered_fac = all_facilities;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // NEVER SHOWS THIS
                        Toast.makeText(MainActivity.this, "Please check internet connection", Toast.LENGTH_LONG).show();
                        Log.d("ERROR MESSAGE", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(secondJsonArrayRequest);
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
