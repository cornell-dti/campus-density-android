package org.cornelldti.density.density.data

class MenuClass(facility: String) {
    var facilityId: String? = null

    var breakfastItems: ArrayList<CategoryItems>? = null
    var brunchItems: ArrayList<CategoryItems>? = null
    var lunchItems: ArrayList<CategoryItems>? = null
    var liteLunchItems: ArrayList<CategoryItems>? = null
    var dinnerItems: ArrayList<CategoryItems>? = null

    init {
        facilityId = facility
    }

    class CategoryItems(categoryName: String, itemNames: ArrayList<String>) {
        var category: String? = null
        var items: ArrayList<String>? = null
        init {
            category = categoryName
            items = itemNames
        }
        fun stringify() : String {
            val s = category + "\n"
            for (i in 0 until items!!.size) {
                s.plus(items!![i] + "\n")
            }
            return s
        }
    }
}