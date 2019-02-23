package org.cornelldti.density.density.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
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

        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.border));
        paint.setStrokeWidth(3);

        //canvas.drawLine(posX, getYOffset(posY), posX + 5, 8, paint);
        canvas.drawLine(posX, 8, posX, posY, paint);

        // TODO: implement some way to prevent text box from overflowing the chart;
        // issues occur when x is at 1, 2, 3, 14, 15, 16
        if (entry.getX() == 14) {
            canvas.translate(posX - 2 * (getWidth() / 3), 8);
        } else if (entry.getX() >= 15) {
            canvas.translate(posX - 5 * (getWidth() / 6), 8);
        } else if (entry.getX() <= 2) {
            canvas.translate(posX - (getWidth() / 6), 8);
        } else {
            canvas.translate(posX + getXOffset(posX), 8);
        }
        draw(canvas);
        canvas.restoreToCount(saveId);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        entry = e;
        float posX = e.getX();
        float posY = e.getY();


        String time = "";
        if (posX <= 5) {
            time = (int) posX + 6 + " AM: ";
        } else if (posX == 6) {
            time = "12 PM: ";
        } else {
            time = (int) posX - 6 + " PM: ";
        }

        String crowd = "";
        if (posY >= 0.75) {
            crowd = "Very Crowded";
        } else if (posY >= 0.5) {
            crowd = "Pretty Crowded";
        } else if (posY >= 0.25) {
            crowd = "Pretty Empty";
        } else {
            crowd = "Very Empty";
        }

        String currentStatus = time + crowd;
        markerText.setText(Html.fromHtml("<b>" + time + "</b>" + crowd));
    }

    public int getXOffset(float xpos) {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return 0;
    }
}
