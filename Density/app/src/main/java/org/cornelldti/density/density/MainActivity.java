package org.cornelldti.density.density;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;

public class MainActivity extends AppCompatActivity {

    private RecyclerView facilities;

    private FacilitiesListAdapter adapter;

    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        facilities = findViewById(R.id.facilities);

        facilities.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        facilities.setLayoutManager(layoutManager);


//        adapter = new FacilitiesListAdapter(fetchFacilities());
        Facility[] fac = new Facility[4];
        fac[0] = new Facility("Keeton Dining","id", "opensAt",
                "closesAt", "address",
        20, 100, true);
        fac[1] = new Facility("Libe Cafe","id", "opensAt",
                "closesAt", "address",
                45, 100, true);
        fac[2] = new Facility("Jansen's Market","id", "opensAt",
                "closesAt", "address",
                65, 100, true);
        fac[3] = new Facility("Bethe Dining","id", "opensAt",
                "closesAt", "address",
                80, 100, true);
        adapter = new FacilitiesListAdapter(fac);
        facilities.setAdapter(adapter);
    }

    /**
     * Implement this TODO
     * @return
     */
    private Facility[] fetchFacilities()
    {
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
}
