package org.cornelldti.density.flux.data

/**
 * This class holds the data model for available dining menus.
 * Refer to this class for different MenuItem or operatingHours by facility.
 */
data class MenuClass(
        val breakfastItems: List<MenuItem>,
        val brunchItems: List<MenuItem>,
        val lunchItems: List<MenuItem>,
        val dinnerItems: List<MenuItem>,
        val cafeMenuItems: List<String>,
        val operatingHours: List<String>,
        val facilityType: String
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