package org.cornelldti.density.flux.util

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler

import java.text.DecimalFormat

object ValueFormatter : IValueFormatter {

    private val format: DecimalFormat = DecimalFormat("#.###")

    override fun getFormattedValue(value: Float, entry: Entry, dataSetIndex: Int, viewPortHandler: ViewPortHandler): String {
        return format.format(value.toDouble())
    }
}
