package org.cornelldti.density.density.util;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.List;

public class ColorBarDataSet extends BarDataSet {

    public ColorBarDataSet(List<BarEntry> y_vals, String label) {
        super(y_vals, label);
    }
    @Override
    public int getColor(int index) {
        if(getEntryForIndex(index).getY() >= 0.75) {
            return getColors().get(3);
        }
        else if(getEntryForIndex(index).getY() >= 0.5 ) {
            return getColors().get(2);
        }
        else if(getEntryForIndex(index).getY() >= 0.25) {
            return getColors().get(1);
        }
        else {
            return getColors().get(0);
        }
    }

}
