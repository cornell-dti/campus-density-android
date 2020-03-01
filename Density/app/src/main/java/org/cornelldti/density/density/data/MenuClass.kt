package org.cornelldti.density.density.data


class MenuClass {
    var breakfastItems: List<MenuItem> = ArrayList()
    var brunchItems: List<MenuItem> = ArrayList()
    var lunchItems: List<MenuItem> = ArrayList()
    var liteLunchItems: List<MenuItem> = ArrayList()
    var dinnerItems: List<MenuItem> = ArrayList()
}

    abstract class MenuItem {
        companion object {
            const val TYPE_CATEGORY = 0
            const val TYPE_FOOD = 1
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