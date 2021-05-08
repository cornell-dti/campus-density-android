package org.cornelldti.density.flux.facilitydetail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cornelldti.density.flux.R
import androidx.recyclerview.widget.RecyclerView
import org.cornelldti.density.flux.data.MenuItem
import kotlinx.android.synthetic.main.menu_category_list_item.view.*
import kotlinx.android.synthetic.main.menu_food_list_item.view.*
import org.cornelldti.density.flux.data.CategoryItem
import org.cornelldti.density.flux.data.FoodItem


class MenuListAdapter(private val menuItems: List<MenuItem>, private val context: Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    open inner class CategoryViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val categoryName = v.category_name

        fun bind(categoryItem: CategoryItem) {
            categoryName.text = categoryItem.category
        }
    }

    open inner class FoodViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val foodName = v.food_name

        fun bind(foodItem: FoodItem) {
            foodName.text = foodItem.food
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == MenuItem.TYPE_CATEGORY) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.menu_category_list_item, parent, false)
            return CategoryViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(context).inflate(R.layout.menu_food_list_item, parent, false)
            return FoodViewHolder(itemView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return menuItems[position].getType()
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val type = getItemViewType(position)
        if (type == MenuItem.TYPE_CATEGORY) {
            val categoryItem = menuItems[position] as CategoryItem
            (viewHolder as CategoryViewHolder).bind(categoryItem)
        } else {
            val foodItem = menuItems[position] as FoodItem
            (viewHolder as FoodViewHolder).bind(foodItem)
        }
    }

    override fun getItemCount(): Int = menuItems.size

}

class CafeMenuListAdapter(private val cafeMenuItems: List<String>, private val context: Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val foodName = v.food_name

        fun bind(foodItem: String) {
            foodName.text = foodItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.menu_food_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = cafeMenuItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val foodItem = cafeMenuItems[position]
        (holder as ViewHolder).bind(foodItem)
    }


}