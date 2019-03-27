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
import java.util.HashMap;
import java.util.List;

import androidx.arch.core.util.Function;
import androidx.recyclerview.widget.RecyclerView;

public class FacilitiesListAdapter extends RecyclerView.Adapter<FacilitiesListAdapter.ViewHolder>
        implements Filterable {

    private List<Facility> facilities, filteredFacilities;
    private HashMap<String, Facility> facility_ids;

    private ClickListener clickListener;
    private Context context;

    private static final Function<String, Comparator<Facility>> SEARCH_SORT;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

    public class DescriptionViewHolder extends FacilitiesListAdapter.ViewHolder {
        TextView description;

        public DescriptionViewHolder(View v) {
            super(v);
            description = v.findViewById(R.id.description_phrase);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public FacilitiesListAdapter(ArrayList<Facility> data) {
        Collections.sort(data = new ArrayList<>(data));
        this.facilities = data;
        this.filteredFacilities = facilities;
        facility_ids = new HashMap<>();
        for (Facility fac : facilities) {
            facility_ids.put(fac.getId(), fac);
        }
    }

    @Override
    public FacilitiesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.facility_card_layout, parent, false);

        context = parent.getContext();

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(filteredFacilities.get(position).getName());
        holder.openStatus.setText(filteredFacilities.get(position).getDensityResId());
        setBars(filteredFacilities.get(position).isOpen() ? filteredFacilities.get(position).getOccupancyRating() : -1, holder);

        if (holder instanceof DescriptionViewHolder) {
            ((DescriptionViewHolder) holder).description.setText(filteredFacilities.get(position).getDescription());
        }
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
                        if (f.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(f);
                        }
                    }
                    Collections.sort(filteredList, SEARCH_SORT.apply(charString));
                    filteredFacilities = filteredList;
                } else {
                    Collections.sort(facilities);
                    filteredFacilities = facilities;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredFacilities;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredFacilities = (List<Facility>) filterResults.values;
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
        this.filteredFacilities = filtered_list;
        notifyDataSetChanged();
    }

    public void showFavLocations(ArrayList<String> favorites) {
        ArrayList<Facility> favorite_facilities = new ArrayList<>();
        for (String id : favorites) {
            if (facility_ids.containsKey(id)) {
                favorite_facilities.add(facility_ids.get(id));
            }
        }
        this.filteredFacilities = favorite_facilities;
        notifyDataSetChanged();
    }

    public void showAllLocations() {
        this.filteredFacilities = facilities;
        notifyDataSetChanged();
    }

    public List<Facility> getDataSet() {
        return this.filteredFacilities;
    }

    public void setDataSet(ArrayList<Facility> f) {
        Collections.sort(f = new ArrayList<>(f));
        this.facilities = f;
        this.filteredFacilities = this.facilities;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredFacilities.size();
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
        holder.firstBar.setColorFilter(context.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        holder.secondBar.setColorFilter(context.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        holder.thirdBar.setColorFilter(context.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        holder.fourthBar.setColorFilter(context.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
    }

    private void setVeryCrowded(ViewHolder holder) {
        holder.firstBar.setColorFilter(context.getResources().getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY);
        holder.secondBar.setColorFilter(context.getResources().getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY);
        holder.thirdBar.setColorFilter(context.getResources().getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY);
        holder.fourthBar.setColorFilter(context.getResources().getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY);
    }

    private void setPrettyCrowded(ViewHolder holder) {
        holder.firstBar.setColorFilter(context.getResources().getColor(R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY);
        holder.secondBar.setColorFilter(context.getResources().getColor(R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY);
        holder.thirdBar.setColorFilter(context.getResources().getColor(R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY);
        holder.fourthBar.setColorFilter(context.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
    }

    private void setPrettyEmpty(ViewHolder holder) {
        holder.firstBar.setColorFilter(context.getResources().getColor(R.color.pretty_empty),
                PorterDuff.Mode.MULTIPLY);
        holder.secondBar.setColorFilter(context.getResources().getColor(R.color.pretty_empty),
                PorterDuff.Mode.MULTIPLY);
        holder.thirdBar.setColorFilter(context.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        holder.fourthBar.setColorFilter(context.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
    }

    private void setVeryEmpty(ViewHolder holder) {
        holder.firstBar.setColorFilter(context.getResources().getColor(R.color.very_empty),
                PorterDuff.Mode.MULTIPLY);
        holder.secondBar.setColorFilter(context.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        holder.thirdBar.setColorFilter(context.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
        holder.fourthBar.setColorFilter(context.getResources().getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY);
    }

    static {
        SEARCH_SORT = (String charString) -> (Comparator<Facility>) (a, b) -> {
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
        };
    }
}