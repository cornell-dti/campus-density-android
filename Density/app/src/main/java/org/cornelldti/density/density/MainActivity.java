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

    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        facilities = (RecyclerView) findViewById(R.id.facilities);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        facilities.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        facilities.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
//        adapter = new FacilitiesListAdapter(myDataset);
        facilities.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
}
