package org.cornelldti.density.flux.colorbarutil

import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class ColorBarDataSet(y_vals: List<BarEntry>, label: String) : BarDataSet(y_vals, label) {

    // We need this dummy override to make Kotlin compiler happy. Otherwise there will be a
    // collision in this method in the Java code that we can't control.
    @Suppress("RedundantOverride")
    override fun getEntryIndex(e: BarEntry): Int = super.getEntryIndex(e)

    override fun getColor(index: Int): Int {
        return if (getEntryForIndex(index).y >= 0.75) {
            colors[3]
        } else if (getEntryForIndex(index).y >= 0.5) {
            colors[2]
        } else if (getEntryForIndex(index).y >= 0.25) {
            colors[1]
        } else if (getEntryForIndex(index).y > 0) {
            colors[0]
        } else {
            colors[4]
        }
    }

}
