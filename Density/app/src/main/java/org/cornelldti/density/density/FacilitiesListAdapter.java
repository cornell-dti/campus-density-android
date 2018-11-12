package org.cornelldti.density.density;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FacilitiesListAdapter extends RecyclerView.Adapter<FacilitiesListAdapter.MyViewHolder> {

    private Facility [] facilities;

    private ImageView firstBar, secondBar, thirdBar, fourthBar;

    private Context c;


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, description;
        public MyViewHolder(View v) {
            super(v);

            name = v.findViewById(R.id.facility_name);
            description = v.findViewById(R.id.description_phrase);
        }
    }

    public FacilitiesListAdapter(Facility[] data) {
        facilities = data;
    }

    @Override
    public FacilitiesListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.facility_card_layout, parent, false);

        c = parent.getContext();

        firstBar = v.findViewById(R.id.first_bar);
        secondBar = v.findViewById(R.id.second_bar);
        thirdBar = v.findViewById(R.id.third_bar);
        fourthBar = v.findViewById(R.id.fourth_bar);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.name.setText(facilities[position].getName());
        holder.description.setText(facilities[position].getDescription());
        setBars(facilities[position].get_occupancy_rating());
    }

    private void setBars(Facility.Occupancy_Rating rating)
    {
        switch (rating)
        {
            case VERY_EMPTY:
                setVeryEmpty();
                break;
            case PRETTY_EMPTY:
                setPrettyEmpty();
                break;
            case PRETTY_CROWDED:
                setPrettyCrowded();
                break;
            case VERY_CROWDED:
                setVeryCrowded();
                break;
        }
    }

    private void setVeryCrowded()
    {
        GradientDrawable firstDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);
        GradientDrawable secondDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);
        GradientDrawable thirdDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);
        GradientDrawable fourthDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);

        firstDrawable.setColorFilter(c.getResources().getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY);
        secondDrawable.setColorFilter(c.getResources().getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY);
        thirdDrawable.setColorFilter(c.getResources().getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY);
        fourthDrawable.setColorFilter(c.getResources().getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY);

        firstBar.setImageDrawable(firstDrawable);
        secondBar.setImageDrawable(secondDrawable);
        thirdBar.setImageDrawable(thirdDrawable);
        fourthBar.setImageDrawable(fourthDrawable);

    }

    private void setPrettyCrowded()
    {
        GradientDrawable firstDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);
        GradientDrawable secondDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);
        GradientDrawable thirdDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);
        GradientDrawable fourthDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);

        firstDrawable.setColorFilter(c.getResources().getColor(R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY);
        secondDrawable.setColorFilter(c.getResources().getColor(R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY);
        thirdDrawable.setColorFilter(c.getResources().getColor(R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY);
        fourthDrawable.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);

        firstBar.setImageDrawable(firstDrawable);
        secondBar.setImageDrawable(secondDrawable);
        thirdBar.setImageDrawable(thirdDrawable);
        fourthBar.setImageDrawable(fourthDrawable);
    }

    private void setPrettyEmpty()
    {
        GradientDrawable firstDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);
        GradientDrawable secondDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);
        GradientDrawable thirdDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);
        GradientDrawable fourthDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);

        firstDrawable.setColorFilter(c.getResources().getColor(R.color.pretty_empty),
                PorterDuff.Mode.MULTIPLY);
        secondDrawable.setColorFilter(c.getResources().getColor(R.color.pretty_empty),
                PorterDuff.Mode.MULTIPLY);
        thirdDrawable.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        fourthDrawable.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);

        firstBar.setImageDrawable(firstDrawable);
        secondBar.setImageDrawable(secondDrawable);
        thirdBar.setImageDrawable(thirdDrawable);
        fourthBar.setImageDrawable(fourthDrawable);
    }

    private void setVeryEmpty()
    {
        GradientDrawable firstDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);
        GradientDrawable secondDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);
        GradientDrawable thirdDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);
        GradientDrawable fourthDrawable = (GradientDrawable) c.getDrawable(R.drawable.rounded_box);

        firstDrawable.setColorFilter(c.getResources().getColor(R.color.very_empty),
                PorterDuff.Mode.MULTIPLY);
        secondDrawable.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        thirdDrawable.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        fourthDrawable.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);

        firstBar.setImageDrawable(firstDrawable);
        secondBar.setImageDrawable(secondDrawable);
        thirdBar.setImageDrawable(thirdDrawable);
        fourthBar.setImageDrawable(fourthDrawable);
    }

    @Override
    public int getItemCount() {
        return facilities.length;
    }
}