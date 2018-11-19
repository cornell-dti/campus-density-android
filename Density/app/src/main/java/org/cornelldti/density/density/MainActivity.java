package org.cornelldti.density.density;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    private RecyclerView facilities;

    private SwipeRefreshLayout swipeRefresh;

    private FacilitiesListAdapter adapter;

    private RecyclerView.LayoutManager layoutManager;

    private ChipGroup filterChips;

    private Chip all;

    private ArrayList<Facility> fac;

    private ArrayList<Facility> filtered_fac;

    private SearchView searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        facilities = findViewById(R.id.facilities);

        setOnRefreshListener();
        setOnClickListeners();
        setupSearchQuery();

        facilities.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        facilities.setLayoutManager(layoutManager);
        fetchFacilities();
        adapter = new FacilitiesListAdapter(fac);
        facilities.setAdapter(adapter);

    }

    private void setOnRefreshListener()
    {
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

    private void setOnClickListeners()
    {
        all = findViewById(R.id.all);

        filterChips = findViewById(R.id.filterChips);

        filterChips.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                handleCheckChange(checkedId);
            }
        });
    }

    private void handleCheckChange(int checkedId)
    {
        switch (checkedId) {
            case R.id.all:
                filtered_fac = fac;
                adapter = new FacilitiesListAdapter(filtered_fac);
                facilities.setAdapter(adapter);
                break;

            case R.id.favorites:
                ArrayList<Facility> filteredList = new ArrayList<Facility>();
                for(Facility f : fac)
                {
                    if(f.isFavorite())
                    {
                        filteredList.add(f);
                    }
                }
                filtered_fac = filteredList;
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

    private ArrayList<Facility> getFiltered_fac(Facility.campus_location location)
    {
        ArrayList<Facility> filtered_list = new ArrayList<>();
        for(Facility f : fac)
        {
            if(f.getLocation().equals(location))
            {
                filtered_list.add(f);
            }
        }
        return filtered_list;
    }

    private void setupSearchQuery()
    {
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
     * @return
     */
    private void fetchFacilities()
    {
        ArrayList<Facility> f = new ArrayList<Facility>();
        f.add(new Facility("Keeton Dining","id", "opensAt",
                "closesAt", "address",
                20, 100, true, Facility.campus_location.WEST));
        f.add(new Facility("Libe Cafe","id", "opensAt",
                "closesAt", "address",
                45, 100, false, Facility.campus_location.CENTRAL));
        f.add(new Facility("Jansen's Market","id", "opensAt",
                "closesAt", "address",
                65, 100, true, Facility.campus_location.WEST));
        f.add(new Facility("Bethe Dining","id", "opensAt",
                "closesAt", "address",
                80, 100, true, Facility.campus_location.WEST));
        fac = f;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
}
