package org.cornelldti.density.density.colorbarutil

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Html
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight

import org.cornelldti.density.density.R

class ColorBarMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    // this markerview only displays a textview
    private val markerText: TextView = findViewById<View>(R.id.marker_text) as TextView
    private val markerLine: View = findViewById(R.id.marker_line)
    private val markerLayout: LinearLayout = findViewById(R.id.marker_layout)
    private var entry: Entry? = null

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        val saveId = canvas.save()

        val eX = entry!!.x
        val eY = 0f

        val paint = Paint()
        paint.color = resources.getColor(R.color.border)
        paint.strokeWidth = 3f
        canvas.drawLine(posX, eY, posX, posY, paint)

        canvas.translate(posX + getXOffset(eX).toFloat(), eY)
        draw(canvas)
        canvas.restoreToCount(saveId)
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry, highlight: Highlight?) {
        entry = e
        val eX = e.x
        val eY = e.y

        var time = ""
        val is24 = DateFormat.is24HourFormat(context)
        if (is24) {
            time = if (eX.toInt() + 7 < 10) "0" + (eX.toInt() + 7) + ":00  " else (eX.toInt() + 7).toString() + ":00 "
        } else {
            if (eX <= 4) {
                time = (eX.toInt() + 7).toString() + "am "
            } else if (eX == 5f) {
                time = "12pm "
            } else {
                time = (eX.toInt() - 5).toString() + "pm "
            }
        }

        val crowd = when {
            eY >= 0.75 -> context.getString(R.string.very_crowded)
            eY >= 0.5 -> context.getString(R.string.pretty_crowded)
            eY >= 0.25 -> context.getString(R.string.pretty_empty)
            eY > 0 -> context.getString(R.string.very_empty)
            else -> context.getString(R.string.closed)
        }

        val currentStatus = time + crowd
        markerText.text = Html.fromHtml("<b>$time</b>$crowd")
    }

    fun getXOffset(xpos: Float): Double {
        // this will center the marker-view horizontally
        // sets position of marker based on the entry; this ensures that it doesn't overflow
        // the chart's boundaries
        var k = 2.0
        if (xpos <= 2) {
            k = 0.96 * Math.pow((xpos - 2).toDouble(), 2.0) + 2.2
        } else if (xpos >= 14) {
            k = 0.08 * Math.pow((xpos - 17).toDouble(), 2.0) + 1.11
        }
        return -(width / k)
    }

    fun getYOffset(ypos: Float): Int {
        // this will cause the marker-view to be above the selected value
        return 0
    }
}
