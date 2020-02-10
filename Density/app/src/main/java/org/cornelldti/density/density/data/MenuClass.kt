package org.cornelldti.density.density.data


class MenuClass(facility: String) {
    var facilityId: String? = null

    var breakfastItems: ArrayList<MenuItem> = ArrayList()
    var brunchItems: ArrayList<MenuItem> = ArrayList()
    var lunchItems: ArrayList<MenuItem> = ArrayList()
    var liteLunchItems: ArrayList<MenuItem> = ArrayList()
    var dinnerItems: ArrayList<MenuItem> = ArrayList()

    init {
        facilityId = facility
    }

}

    abstract class MenuItem {
        companion object {
            val TYPE_CATEGORY = 0
            val TYPE_FOOD = 1
        }
        abstract fun getType(): Int
    }

    class CategoryItem : MenuItem() {

        var category: String? = null

        override fun getType() = TYPE_CATEGORY

    }

    class FoodItem : MenuItem() {

        var food: String? = null

        override fun getType() = TYPE_FOOD

    }