package org.cornelldti.density.density.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import org.cornelldti.density.density.R;
import org.w3c.dom.Text;

import androidx.core.content.ContextCompat;

public class ColorBarMarkerView extends MarkerView {
    private TextView markerText;
    private View markerLine;
    private LinearLayout markerLayout;
    private Entry entry;

    public ColorBarMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        // this markerview only displays a textview
        markerText = (TextView) findViewById(R.id.marker_text);
        markerLine = findViewById(R.id.marker_line);
        markerLayout = findViewById(R.id.marker_layout);
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        int saveId = canvas.save();

        float eX = entry.getX();
        float eY = 0;

        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.border));
        paint.setStrokeWidth(3);
        canvas.drawLine(posX, eY, posX, posY, paint);

        canvas.translate(posX + (float) getXOffset(eX), eY);
        draw(canvas);
        canvas.restoreToCount(saveId);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        entry = e;
        float eX = e.getX();
        float eY = e.getY();

        String time = "";
        if (eX <= 4) {
            time = (int) eX + 7 + "am: ";
        } else if (eX == 5) {
            time = "12pm: ";
        } else {
            time = (int) eX - 5 + "pm: ";
        }

        String crowd = "";
        if (eY >= 0.75) {
            crowd = getContext().getString(R.string.very_crowded);
        } else if (eY >= 0.5) {
            crowd = getContext().getString(R.string.pretty_crowded);
        } else if (eY >= 0.25) {
            crowd = getContext().getString(R.string.pretty_empty);
        } else if (eY > 0) {
            crowd = getContext().getString(R.string.very_empty);
        } else {
            crowd = getContext().getString(R.string.closed);
        }

        String currentStatus = time + crowd;
        markerText.setText(Html.fromHtml("<b>" + time + "</b>" + crowd));
    }

    public double getXOffset(float xpos) {
        // this will center the marker-view horizontally
        // sets position of marker based on the entry; this ensures that it doesn't overflow
        // the chart's boundaries
        double k = 2;
        if (xpos <= 2) {
            k = 0.96 * Math.pow(xpos - 2, 2) + 2.2;
        } else if (xpos >= 14) {
            k = 0.08 * Math.pow(xpos - 17, 2) + 1.11;
        }
        return -(getWidth() / k);
    }

    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return 0;
    }
}
