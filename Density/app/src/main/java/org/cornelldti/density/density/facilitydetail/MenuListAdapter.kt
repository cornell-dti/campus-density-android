package org.cornelldti.density.density.facilitydetail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cornelldti.density.density.data.MenuClass
import org.cornelldti.density.density.R
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.menu_list_item.view.*

class MenuListAdapter(private val menu: MenuClass, private val context: Context)
    : RecyclerView.Adapter<MenuListAdapter.ViewHolder>() {

    open inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v){
        private val itemName = v.item_name

        fun bind(categoryItem: MenuClass.CategoryItem) {
            itemName.text = categoryItem.item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.menu_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(menu.lunchItems!![position])
    }

    override fun getItemCount(): Int = if(menu.lunchItems != null ) { menu.lunchItems!!.size } else {0}

}