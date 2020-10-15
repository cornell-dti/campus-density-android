package org.cornelldti.density.density.data


data class MenuClass(
        val breakfastItems: List<MenuItem>,
        val brunchItems: List<MenuItem>,
        val lunchItems: List<MenuItem>,
        val dinnerItems: List<MenuItem>,
        val operatingHours: List<String>
)

abstract class MenuItem {
    companion object {
        const val TYPE_CATEGORY = 0
        const val TYPE_FOOD = 1
    }

    abstract fun getType(): Int
}

data class CategoryItem(val category: String) : MenuItem() {
    override fun getType() = TYPE_CATEGORY
}

data class FoodItem(val food: String) : MenuItem() {
    override fun getType() = TYPE_FOOD
}