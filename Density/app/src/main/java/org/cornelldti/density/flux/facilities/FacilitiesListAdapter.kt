package org.cornelldti.density.flux.facilities

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.arch.core.util.Function
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cornelldti.density.flux.R
import org.cornelldti.density.flux.data.FacilityClass
import java.util.*


/**
 * This class holds FacilitiesActivity's adapter, which feeds in FacilityClass data into the Recyclerview.
 * It also implements functions that handles layout filtering in the main facilities page.
 */
class FacilitiesListAdapter(data: List<FacilityClass>) : RecyclerView.Adapter<FacilitiesListAdapter.ViewHolder>(), Filterable {

    private var facilityClasses: List<FacilityClass>? = null
    var dataSet: List<FacilityClass>? = null
        private set
    var waitTimesMap: Map<String, Double>? = null
        private set
    private var clickListener: ClickListener? = null
    private var context: Context? = null

    open inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        internal val name: TextView
        internal val waitTimes: TextView
        internal val firstBar: ImageView
        internal val secondBar: ImageView
        internal val thirdBar: ImageView
        internal val fourthBar: ImageView

        init {
            v.setOnClickListener(this)
            name = v.findViewById(R.id.facility_name)
            waitTimes = v.findViewById(R.id.facility_wait_time)
            firstBar = v.findViewById(R.id.first_bar)
            secondBar = v.findViewById(R.id.second_bar)
            thirdBar = v.findViewById(R.id.third_bar)
            fourthBar = v.findViewById(R.id.fourth_bar)
        }

        override fun onClick(v: View) {
            if (adapterPosition != -1) {
                clickListener!!.onItemClick(adapterPosition, v)
            }
        }
    }

    fun setOnItemClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onItemClick(position: Int, v: View)
    }

    init {
        this.facilityClasses = ArrayList(data).apply { sort() }
        this.dataSet = facilityClasses
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.facility_card_layout, parent, false)

        context = parent.context

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = dataSet!![position].name
        val waitTimes = waitTimesMap?.get(dataSet!![position].id)?.toInt()
        if (dataSet!![position].isOpen) {
            if (waitTimes != null) {
                holder.waitTimes.text = "$waitTimes min. wait"
            } else {
                holder.waitTimes.text = "Unknown wait"
            }
        } else {
            holder.waitTimes.text = "Closed"
        }
        setBars(if (dataSet!![position].isOpen) dataSet!![position].occupancyRating else -1, holder)
    }

    /**
     * This function sets up the filters for the searchbar in main facilities page.
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString().toLowerCase(Locale.US)
                dataSet = if (charString.isNotEmpty()) {
                    val filteredList = ArrayList<FacilityClass>()
                    for (f in facilityClasses!!) {
                        if (f.name.toLowerCase(Locale.US).contains(charString.toLowerCase(Locale.US))) {
                            filteredList.add(f)
                        }
                    }
                    Collections.sort(filteredList, SEARCH_SORT.apply(charString))
                    filteredList
                } else {
                    val mutFacilities = facilityClasses!!.toMutableList()
                    mutFacilities.sort()
                    mutFacilities
                }
                val filterResults = FilterResults()
                filterResults.values = dataSet
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                dataSet = filterResults.values as List<FacilityClass>
                notifyDataSetChanged()
            }
        }
    }

    /**
     * This function filters the data fed into adapter by iterating facilities
     */
    fun filterFacilitiesByLocation(location: FacilityClass.CampusLocation) {
        val filteredList = ArrayList<FacilityClass>()
        for (f in facilityClasses!!) {
            if (f.location == location) {
                filteredList.add(f)
            }
        }
        this.dataSet = filteredList
        notifyDataSetChanged()
    }

    fun showAllLocations() {
        this.dataSet = facilityClasses
        notifyDataSetChanged()
    }

    fun setDataSet(f: List<FacilityClass>) {
        this.facilityClasses = f.sorted()
        this.dataSet = this.facilityClasses
        notifyDataSetChanged()
    }

    fun setWaitTimesMap(map: Map<String, Double>) {
        this.waitTimesMap = map
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

    /**
     * The 5 functions below sets the bar color for each facility item depending on density.
     */
    private fun setClosed(holder: ViewHolder) {
        holder.firstBar.setColorFilter(ContextCompat.getColor(context!!, R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
        holder.secondBar.setColorFilter(ContextCompat.getColor(context!!, R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
        holder.thirdBar.setColorFilter(ContextCompat.getColor(context!!, R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
        holder.fourthBar.setColorFilter(ContextCompat.getColor(context!!, R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
    }

    private fun setVeryCrowded(holder: ViewHolder) {
        holder.firstBar.setColorFilter(ContextCompat.getColor(context!!, R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY)
        holder.secondBar.setColorFilter(ContextCompat.getColor(context!!, R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY)
        holder.thirdBar.setColorFilter(ContextCompat.getColor(context!!, R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY)
        holder.fourthBar.setColorFilter(ContextCompat.getColor(context!!, R.color.very_crowded),
                PorterDuff.Mode.MULTIPLY)
    }

    private fun setPrettyCrowded(holder: ViewHolder) {
        holder.firstBar.setColorFilter(ContextCompat.getColor(context!!, R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY)
        holder.secondBar.setColorFilter(ContextCompat.getColor(context!!, R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY)
        holder.thirdBar.setColorFilter(ContextCompat.getColor(context!!, R.color.pretty_crowded),
                PorterDuff.Mode.MULTIPLY)
        holder.fourthBar.setColorFilter(ContextCompat.getColor(context!!, R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
    }

    private fun setPrettyEmpty(holder: ViewHolder) {
        holder.firstBar.setColorFilter(ContextCompat.getColor(context!!, R.color.pretty_empty),
                PorterDuff.Mode.MULTIPLY)
        holder.secondBar.setColorFilter(ContextCompat.getColor(context!!, R.color.pretty_empty),
                PorterDuff.Mode.MULTIPLY)
        holder.thirdBar.setColorFilter(ContextCompat.getColor(context!!, R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
        holder.fourthBar.setColorFilter(ContextCompat.getColor(context!!, R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
    }

    private fun setVeryEmpty(holder: ViewHolder) {
        holder.firstBar.setColorFilter(ContextCompat.getColor(context!!, R.color.very_empty),
                PorterDuff.Mode.MULTIPLY)
        holder.secondBar.setColorFilter(ContextCompat.getColor(context!!, R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
        holder.thirdBar.setColorFilter(ContextCompat.getColor(context!!, R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
        holder.fourthBar.setColorFilter(ContextCompat.getColor(context!!, R.color.filler_boxes),
                PorterDuff.Mode.MULTIPLY)
    }

    companion object {

        private val SEARCH_SORT: Function<String, Comparator<FacilityClass>>

        init {
            SEARCH_SORT = Function { charString: String ->
                Comparator { a: FacilityClass, b: FacilityClass ->
                    val lowerA = a.name.toLowerCase(Locale.US)
                    val lowerB = b.name.toLowerCase(Locale.US)
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

                    a.name.compareTo(b.name)
                }
            }
        }
    }
}
