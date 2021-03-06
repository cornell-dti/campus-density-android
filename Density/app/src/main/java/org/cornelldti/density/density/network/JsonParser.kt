package org.cornelldti.density.density.network

import org.cornelldti.density.density.data.*
import org.cornelldti.density.density.util.FluxUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object JsonParser {

    /**
     * This function returns a MutableList of all dining facilities on campus.
     */
    fun parseFacilities(jsonArray: JSONArray): MutableList<FacilityClass>? =
            try {
                val facilities = arrayListOf<FacilityClass>()
                for (i in 0 until jsonArray.length()) {
                    val facilityJson = jsonArray.getJSONObject(i)
                    val facility = FacilityClass(
                            name = facilityJson.getString("displayName"),
                            id = facilityJson.getString("id")
                    )
                    facilities.add(facility)
                }
                facilities
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }

    /**
     * This function parses each category of daily dining menus and returns a MenuClass object.
     */
    fun parseMenu(jsonArray: JSONArray, day: String): MenuClass? {
        try {
            val facilityType = jsonArray.getJSONObject(0).getString("type")
            if (facilityType == "dining-hall") {

                val dayMenus = getDayMenu(jsonArray, day)
                val breakfast = arrayListOf<MenuItem>()
                val brunch = arrayListOf<MenuItem>()
                val lunch = arrayListOf<MenuItem>()
                val liteLunch = arrayListOf<MenuItem>()
                val dinner = arrayListOf<MenuItem>()
                val operatingHoursList = arrayListOf<String>()

                if (dayMenus != null) {
                    for (i in 0 until dayMenus.length()) {
                        val categoryItemsJSONArray = dayMenus.getJSONObject(i).getJSONArray("menu")
                        val menuItems = arrayListOf<MenuItem>()
                        // Menu is empty in API response, but there is still a meal.
                        if (categoryItemsJSONArray.length() == 0) {
                            menuItems.add(CategoryItem(""))
                        } else {
                            for (j in 0 until categoryItemsJSONArray.length()) {
                                val category = categoryItemsJSONArray.getJSONObject(j).getString("category")
                                val categoryItem = CategoryItem(category)
                                menuItems.add(categoryItem)
                                val foodItemJSONArray = categoryItemsJSONArray.getJSONObject(j).getJSONArray("items")
                                for (k in 0 until foodItemJSONArray.length()) {
                                    val food = foodItemJSONArray.getString(k)
                                    val foodItem = FoodItem(food)
                                    menuItems.add(foodItem)
                                }
                            }
                        }
                        when (dayMenus.getJSONObject(i).getString("description")) {
                            "Breakfast" -> breakfast.addAll(menuItems)
                            "Brunch" -> brunch.addAll(menuItems)
                            "Lunch" -> lunch.addAll(menuItems)
                            "Lite Lunch" -> liteLunch.addAll(menuItems)
                            "Dinner" -> dinner.addAll(menuItems)
                        }
                        val start = dayMenus.getJSONObject(i).getLong("startTime")
                        val end = dayMenus.getJSONObject(i).getLong("endTime")
                        operatingHoursList.add(FluxUtil.parseTime(start) + " – " + FluxUtil.parseTime(end))
                    }
                }

                return MenuClass(
                        breakfastItems = breakfast,
                        brunchItems = brunch,
                        lunchItems = lunch,
                        dinnerItems = dinner,
                        cafeMenuItems = emptyList(),
                        operatingHours = operatingHoursList,
                        facilityType = facilityType
                )

            } else {

                val cafeMenu = jsonArray.getJSONObject(0).getJSONArray("weeksMenus")
                val cafeMenuItems = arrayListOf<String>()
                for (i in 0 until cafeMenu.length()) {
                    cafeMenuItems.add(cafeMenu.get(i).toString())
                }

                return MenuClass(
                        breakfastItems = emptyList(),
                        brunchItems = emptyList(),
                        lunchItems = emptyList(),
                        dinnerItems = emptyList(),
                        cafeMenuItems = cafeMenuItems,
                        operatingHours = emptyList(),
                        facilityType = facilityType
                )
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * This is a helper function that gets menus for specific day.
     */
    private fun getDayMenu(jsonArray: JSONArray, day: String): JSONArray? {
        try {
            val weeksMenus = jsonArray.getJSONObject(0).getJSONArray("weeksMenus")
            for (i in 0 until weeksMenus.length()) {
                if (weeksMenus.getJSONObject(i).getString("date") == day) {
                    return weeksMenus.getJSONObject(i).getJSONArray("menus")
                }
            }
            return null
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * This function returns a list of the operating hour time slots of a specified day and the first slot of the next day.
     * This is returned in format of a list of pairs of start and end timestamps.
     * Sample Response: [(1000000, 1200000), (1300000, 1500000), (1600000, 1800000)]
     */
    fun parseOperatingHoursToTimestampList(jsonArray: JSONArray): OperatingHoursClass {
        val todayOperatingHours = arrayListOf<Pair<Long, Long>>()
        var tomorrowFirstOpHours = Pair(-1L, -1L)
        try {
            val hours = jsonArray.getJSONObject(0).getJSONArray("hours")
            val currDate = FluxUtil.getCurrentDate()
            for (i in 0 until hours.length()) {
                if (hours.getJSONObject(i).getString("date") == currDate) {
                    val segment = hours.getJSONObject(i).getJSONObject("dailyHours")
                    val start = segment.getLong("startTimestamp")
                    val end = segment.getLong("endTimestamp")
                    todayOperatingHours.add(Pair(start, end))
                } else {
                    val segment = hours.getJSONObject(i).getJSONObject("dailyHours")
                    val start = segment.getLong("startTimestamp")
                    val end = segment.getLong("endTimestamp")
                    tomorrowFirstOpHours = Pair(start, end)
                    break
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return OperatingHoursClass(todayOperatingHours, tomorrowFirstOpHours)
    }

    /**
     * This function returns a mutable map object of the predicted waitTimes for all dining locations.
     * The original predicted waitTimes are doubles, so we've decided to floor the decimals.
     */
    fun parseWaitTimes(jsonObject: JSONObject): MutableMap<String, Double> {
        val waitTimes = mutableMapOf<String, Double>()
        try {
            for (key in jsonObject.keys()) {
                waitTimes[key] = kotlin.math.floor(jsonObject.getDouble(key))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return waitTimes
    }
}