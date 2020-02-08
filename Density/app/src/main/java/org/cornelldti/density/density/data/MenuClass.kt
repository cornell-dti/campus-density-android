package org.cornelldti.density.density.data

import android.util.Log

class MenuClass(facility: String) {
    var facilityId: String? = null

    var breakfastItems: ArrayList<CategoryItem>? = null
    var brunchItems: ArrayList<CategoryItem>? = null
    var lunchItems: ArrayList<CategoryItem>? = null
    var liteLunchItems: ArrayList<CategoryItem>? = null
    var dinnerItems: ArrayList<CategoryItem>? = null

    init {
        facilityId = facility
    }

    class CategoryItem(categoryName: String, itemName: String) {
        var category: String? = null
        var item: String? = null
        init {
            category = categoryName
            item = itemName
        }
    }
}