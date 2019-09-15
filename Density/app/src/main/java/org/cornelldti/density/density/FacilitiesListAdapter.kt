package org.cornelldti.density.density

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

import androidx.arch.core.util.Function
import androidx.recyclerview.widget.RecyclerView

class FacilitiesListAdapter(data: ArrayList<Facility>) : RecyclerView.Adapter<FacilitiesListAdapter.ViewHolder>(), Filterable {

    private var facilities: List<Facility>? = null
    var dataSet: List<Facility>? = null
        private set

    private var clickListener: ClickListener? = null
    private var context: Context? = null

    open inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        internal var name: TextView
        internal var openStatus: TextView
        internal var firstBar: ImageView
        internal var secondBar: ImageView
        internal var thirdBar: ImageView
        internal var fourthBar: ImageView

        init {
            v.setOnClickListener(this)
            name = v.findViewById(R.id.facility_name)
            openStatus = v.findViewById(R.id.openStatusDescription)
            firstBar = v.findViewById(R.id.first_bar)
            secondBar = v.findViewById(R.id.second_bar)
            thirdBar = v.findViewById(R.id.third_bar)
            fourthBar = v.findViewById(R.id.fourth_bar)
        }

        override fun onClick(v: View) {
            clickListener!!.onItemClick(adapterPosition, v)
        }
    }

    inner class DescriptionViewHolder(v: View) : FacilitiesListAdapter.ViewHolder(v) {
        internal var description: TextView

        init {
            description = v.findViewById(R.id.description_phrase)
        }
    }

    fun setOnItemClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onItemClick(position: Int, v: View)
    }

    init {
        this.facilities = ArrayList(data).apply { sort() }
        this.dataSet = facilities
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): FacilitiesListAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.facility_card_layout, parent, false)

        context = parent.context

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = dataSet!![position].name
        holder.openStatus.setText(dataSet!![position].densityResId)
        setBars(if (dataSet!![position].isOpen) dataSet!![position].occupancyRating else -1, holder)

        if (holder is DescriptionViewHolder) {
            holder.description.text = dataSet!![position].description
        }
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString().toLowerCase()
                if (!charString.isEmpty()) {
                    val filteredList = ArrayList<Facility>()
                    for (f in facilities!!) {
                        if (f.name!!.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(f)
                        }
                    }
                    Collections.sort(filteredList, SEARCH_SORT.apply(charString))
                    dataSet = filteredList
                } else {
                    Collections.sort(facilities)
                    dataSet = facilities
                }
                val filterResults = FilterResults()
                filterResults.values = dataSet
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                dataSet = filterResults.values as List<Facility>
                notifyDataSetChanged()
            }
        }
    }

    fun filterFacilitiesByLocation(location: Facility.CampusLocation) {
        val filtered_list = ArrayList<Facility>()
        for (f in facilities!!) {
            if (f.location == location) {
                filtered_list.add(f)
            }
        }
        this.dataSet = filtered_list
        notifyDataSetChanged()
    }

    fun showAllLocations() {
        this.dataSet = facilities
        notifyDataSetChanged()
    }

    fun setDataSet(f: ArrayList<Facility>) {
        this.facilities = ArrayList(f).apply { sort() }
        this.dataSet = this.facilities
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataSet!!.size
    }

    private fun setBars(rating: Int, holder: ViewHolder) {
        when (rating) {
            -1 -> setClosed(holder)
            0 -> setVeryEmpty(holder)
            1 -> setPrettyEmpty(holder)
            2 -> setPrettyCrowded(holder)
            3 -> setVeryCrowded(holder)
        }
    }

    private fun setClosed(holder: ViewHolder) {
        holder.firstBar.setColorFilter(context!!.resources.getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
        holder.secondBar.setColorFilter(context!!.resources.getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
        holder.thirdBar.setColorFilter(context!!.resources.getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
        holder.fourthBar.setColorFilter(context!!.resources.getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
    }

    private fun setVeryCrowded(holder: ViewHolder) {
        holder.firstBar.setColorFilter(context!!.resources.getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY)
        holder.secondBar.setColorFilter(context!!.resources.getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY)
        holder.thirdBar.setColorFilter(context!!.resources.getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY)
        holder.fourthBar.setColorFilter(context!!.resources.getColor(R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY)
    }

    private fun setPrettyCrowded(holder: ViewHolder) {
        holder.firstBar.setColorFilter(context!!.resources.getColor(R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY)
        holder.secondBar.setColorFilter(context!!.resources.getColor(R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY)
        holder.thirdBar.setColorFilter(context!!.resources.getColor(R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY)
        holder.fourthBar.setColorFilter(context!!.resources.getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
    }

    private fun setPrettyEmpty(holder: ViewHolder) {
        holder.firstBar.setColorFilter(context!!.resources.getColor(R.color.pretty_empty),
                PorterDuff.Mode.MULTIPLY)
        holder.secondBar.setColorFilter(context!!.resources.getColor(R.color.pretty_empty),
                PorterDuff.Mode.MULTIPLY)
        holder.thirdBar.setColorFilter(context!!.resources.getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
        holder.fourthBar.setColorFilter(context!!.resources.getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
    }

    private fun setVeryEmpty(holder: ViewHolder) {
        holder.firstBar.setColorFilter(context!!.resources.getColor(R.color.very_empty),
                PorterDuff.Mode.MULTIPLY)
        holder.secondBar.setColorFilter(context!!.resources.getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
        holder.thirdBar.setColorFilter(context!!.resources.getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
        holder.fourthBar.setColorFilter(context!!.resources.getColor(R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
    }

    companion object {

        private val SEARCH_SORT: Function<String, Comparator<Facility>>

        init {
            SEARCH_SORT = Function { charString: String ->
                Comparator { a: Facility, b: Facility ->
                    val lowerA = a.name!!.toLowerCase()
                    val lowerB = b.name!!.toLowerCase()
                    if (lowerA.startsWith(charString) && !lowerB.startsWith(charString)) {
                        return@Comparator -1
                    }

                    if (lowerB.startsWith(charString) && !lowerA.startsWith(charString)) {
                        return@Comparator 1
                    }

                    val partsA = lowerA.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val partsB = lowerB.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    for (`as` in partsA) {
                        for (bs in partsB) {
                            if (`as`.startsWith(charString) && !bs.startsWith(charString)) {
                                return@Comparator -1
                            }

                            if (bs.startsWith(charString) && !`as`.startsWith(charString)) {
                                return@Comparator 1
                            }
                        }
                    }

                    a.name!!.compareTo(b.name!!)
                }
            }
        }
    }
}
