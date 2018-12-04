package org.cornelldti.density.density;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class FacilitiesListAdapter extends RecyclerView.Adapter<FacilitiesListAdapter.ViewHolder>
        implements Filterable {

    private static ClickListener clickListener;

    private ArrayList<Facility> facilities;

    private ArrayList<Facility> filtered_facilities;

    private Context c;


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name, description;
        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            name = v.findViewById(R.id.facility_name);
            description = v.findViewById(R.id.description_phrase);
        }
        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        FacilitiesListAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public FacilitiesListAdapter(ArrayList<Facility> data) {
        facilities = data;
        filtered_facilities = facilities;
    }

    @Override
    public FacilitiesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.facility_card_layout, parent, false);

        c = parent.getContext();

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(filtered_facilities.get(position).getName());
        holder.description.setText(filtered_facilities.get(position).getDescription());
        setBars(filtered_facilities.get(position).getOccupancy_rating(), holder);
    }

    private void setBars(int rating, ViewHolder holder)
    {
        switch (rating)
        {
            case 0:
                setVeryEmpty(holder);
                break;
            case 1:
                setPrettyEmpty(holder);
                break;
            case 2:
                setPrettyCrowded(holder);
                break;
            case 3:
                setVeryCrowded(holder);
                break;
        }
    }

    private void setVeryCrowded(ViewHolder holder)
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

        ImageView firstBar = holder.itemView.findViewById(R.id.first_bar);
        ImageView secondBar = holder.itemView.findViewById(R.id.second_bar);
        ImageView thirdBar = holder.itemView.findViewById(R.id.third_bar);
        ImageView fourthBar = holder.itemView.findViewById(R.id.fourth_bar);

        firstBar.setImageDrawable(firstDrawable);
        secondBar.setImageDrawable(secondDrawable);
        thirdBar.setImageDrawable(thirdDrawable);
        fourthBar.setImageDrawable(fourthDrawable);

    }

    private void setPrettyCrowded(ViewHolder holder)
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

        ImageView firstBar = holder.itemView.findViewById(R.id.first_bar);
        ImageView secondBar = holder.itemView.findViewById(R.id.second_bar);
        ImageView thirdBar = holder.itemView.findViewById(R.id.third_bar);
        ImageView fourthBar = holder.itemView.findViewById(R.id.fourth_bar);

        firstBar.setImageDrawable(firstDrawable);
        secondBar.setImageDrawable(secondDrawable);
        thirdBar.setImageDrawable(thirdDrawable);
        fourthBar.setImageDrawable(fourthDrawable);
    }

    private void setPrettyEmpty(ViewHolder holder)
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

        ImageView firstBar = holder.itemView.findViewById(R.id.first_bar);
        ImageView secondBar = holder.itemView.findViewById(R.id.second_bar);
        ImageView thirdBar = holder.itemView.findViewById(R.id.third_bar);
        ImageView fourthBar = holder.itemView.findViewById(R.id.fourth_bar);

        firstBar.setImageDrawable(firstDrawable);
        secondBar.setImageDrawable(secondDrawable);
        thirdBar.setImageDrawable(thirdDrawable);
        fourthBar.setImageDrawable(fourthDrawable);
    }

    private void setVeryEmpty(ViewHolder holder)
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

        ImageView firstBar = holder.itemView.findViewById(R.id.first_bar);
        ImageView secondBar = holder.itemView.findViewById(R.id.second_bar);
        ImageView thirdBar = holder.itemView.findViewById(R.id.third_bar);
        ImageView fourthBar = holder.itemView.findViewById(R.id.fourth_bar);

        firstBar.setImageDrawable(firstDrawable);
        secondBar.setImageDrawable(secondDrawable);
        thirdBar.setImageDrawable(thirdDrawable);
        fourthBar.setImageDrawable(fourthDrawable);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (!charString.isEmpty()) {
                    ArrayList<Facility> filteredList = new ArrayList<Facility>();
                    for (Facility f : facilities) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (f.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(f);
                        }
                    }
                    filtered_facilities = filteredList;
                }
                else
                {
                    filtered_facilities = facilities;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filtered_facilities;
                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filtered_facilities = (ArrayList<Facility>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void filterFacilitiesByLocation(Facility.campus_location location) {
        ArrayList<Facility> filtered_list = new ArrayList<>();
        for (Facility f : facilities ) {
            if (f.getLocation().equals(location)) {
                filtered_list.add(f);
            }
        }
        this.filtered_facilities = filtered_list;
    }

    public void showAllLocations()
    {
        this.filtered_facilities = facilities;
    }

    public ArrayList<Facility> getDataSet()
    {
        return this.filtered_facilities;
    }

    public void setDataSet(ArrayList <Facility> f)
    {
        this.facilities = f;
        this.filtered_facilities = this.facilities;
    }

    @Override
    public int getItemCount() {
        return filtered_facilities.size();
    }
}