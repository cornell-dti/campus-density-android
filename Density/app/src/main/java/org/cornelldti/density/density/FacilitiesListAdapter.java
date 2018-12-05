package org.cornelldti.density.density;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import androidx.recyclerview.widget.RecyclerView;

public class FacilitiesListAdapter extends RecyclerView.Adapter<FacilitiesListAdapter.ViewHolder>
        implements Filterable {

    private static ClickListener clickListener;

    private ArrayList<Facility> facilities;

    private ArrayList<Facility> filtered_facilities;

    private Context c;


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name, openStatus;
        ImageView firstBar, secondBar, thirdBar, fourthBar;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            name = v.findViewById(R.id.facility_name);
            openStatus = v.findViewById(R.id.openStatusDescription);
            firstBar = v.findViewById(R.id.first_bar);
            secondBar = v.findViewById(R.id.second_bar);
            thirdBar = v.findViewById(R.id.third_bar);
            fourthBar = v.findViewById(R.id.fourth_bar);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public static class DescriptionViewHolder extends FacilitiesListAdapter.ViewHolder {
        TextView description;

        public DescriptionViewHolder(View v) {
            super(v);
            description = v.findViewById(R.id.description_phrase);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        FacilitiesListAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public FacilitiesListAdapter(ArrayList<Facility> data) {
        Collections.sort(data = new ArrayList<>(data));
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
        holder.openStatus.setText(filtered_facilities.get(position).getDensityResId());
        setBars(filtered_facilities.get(position).isOpen() ? filtered_facilities.get(position).getOccupancyRating() : -1, holder);

        if (holder instanceof DescriptionViewHolder) {
            ((DescriptionViewHolder) holder).description.setText(filtered_facilities.get(position).getDescription());
        }
    }

    private void setBars(int rating, ViewHolder holder) {
        switch (rating) {
            case -1:
                setClosed(holder);
                break;
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

    private void setClosed(ViewHolder holder) {
        holder.firstBar.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        holder.secondBar.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        holder.thirdBar.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        holder.fourthBar.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
    }

    private void setVeryCrowded(ViewHolder holder) {
        holder.firstBar.setColorFilter(c.getResources().getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY);
        holder.secondBar.setColorFilter(c.getResources().getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY);
        holder.thirdBar.setColorFilter(c.getResources().getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY);
        holder.fourthBar.setColorFilter(c.getResources().getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY);
    }

    private void setPrettyCrowded(ViewHolder holder) {
        holder.firstBar.setColorFilter(c.getResources().getColor(R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY);
        holder.secondBar.setColorFilter(c.getResources().getColor(R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY);
        holder.thirdBar.setColorFilter(c.getResources().getColor(R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY);
        holder.fourthBar.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
    }

    private void setPrettyEmpty(ViewHolder holder) {
        holder.firstBar.setColorFilter(c.getResources().getColor(R.color.pretty_empty),
                PorterDuff.Mode.MULTIPLY);
        holder.secondBar.setColorFilter(c.getResources().getColor(R.color.pretty_empty),
                PorterDuff.Mode.MULTIPLY);
        holder.thirdBar.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        holder.fourthBar.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
    }

    private void setVeryEmpty(ViewHolder holder) {
        holder.firstBar.setColorFilter(c.getResources().getColor(R.color.very_empty),
                PorterDuff.Mode.MULTIPLY);
        holder.secondBar.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        holder.thirdBar.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        holder.fourthBar.setColorFilter(c.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                final String charString = charSequence.toString().toLowerCase();
                if (!charString.isEmpty()) {
                    ArrayList<Facility> filteredList = new ArrayList<Facility>();
                    for (Facility f : facilities) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (f.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(f);
                        }
                    }
                    Collections.sort(filteredList, new Comparator<Facility>() {
                        @Override
                        public int compare(Facility a, Facility b) {
                            String lowerA = a.getName().toLowerCase();
                            String lowerB = b.getName().toLowerCase();
                            if (lowerA.startsWith(charString) && !lowerB.startsWith(charString)) {
                                return -1;
                            }

                            if (lowerB.startsWith(charString) && !lowerA.startsWith(charString)) {
                                return 1;
                            }

                            String[] partsA = lowerA.split(" ");
                            String[] partsB = lowerB.split(" ");

                            for (String as : partsA) {
                                for (String bs : partsB) {
                                    if (as.startsWith(charString) && !bs.startsWith(charString)) {
                                        return -1;
                                    }

                                    if (bs.startsWith(charString) && !as.startsWith(charString)) {
                                        return 1;
                                    }
                                }
                            }

                            return a.getName().compareTo(b.getName());
                        }
                    });
                    filtered_facilities = filteredList;
                } else {
                    Collections.sort(facilities);
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

    public void filterFacilitiesByLocation(Facility.CampusLocation location) {
        ArrayList<Facility> filtered_list = new ArrayList<>();
        for (Facility f : facilities) {
            if (f.getLocation().equals(location)) {
                filtered_list.add(f);
            }
        }
        this.filtered_facilities = filtered_list;
    }

    public void showAllLocations() {
        this.filtered_facilities = facilities;
    }

    public ArrayList<Facility> getDataSet() {
        return this.filtered_facilities;
    }

    public void setDataSet(ArrayList<Facility> f) {
        Collections.sort(f = new ArrayList<>(f));
        this.facilities = f;
        this.filtered_facilities = this.facilities;
    }

    @Override
    public int getItemCount() {
        return filtered_facilities.size();
    }
}