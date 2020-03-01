package org.cornelldti.density.density.network

import android.text.format.DateFormat
import org.cornelldti.density.density.DensityApplication
import org.cornelldti.density.density.data.*
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*

object JsonParser {
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

    fun parseMenu(jsonArray: JSONArray, day: String): MenuClass? {
        try {
            val dayMenus = getDayMenu(jsonArray, day)
            val menu = MenuClass()
            if (dayMenus != null) {
                for (i in 0 until dayMenus.length()) {
                    val categoryItemsJSONArray = dayMenus.getJSONObject(i).getJSONArray("menu")
                    val menuItems = arrayListOf<MenuItem>()
                    for (j in 0 until categoryItemsJSONArray.length()) {
                        val category = categoryItemsJSONArray.getJSONObject(j).getString("category")
                        val categoryItem = CategoryItem()
                        categoryItem.category = category
                        menuItems.add(categoryItem)
                        val foodItemJSONArray = categoryItemsJSONArray.getJSONObject(j).getJSONArray("items")
                        for (k in 0 until foodItemJSONArray.length()) {
                            val food = foodItemJSONArray.getString(k)
                            val foodItem = FoodItem()
                            foodItem.food = food
                            menuItems.add(foodItem)
                        }
                    }
                    when(dayMenus.getJSONObject(i).getString("description")) {
                        "Breakfast" -> menu.breakfastItems = menuItems
                        "Brunch" -> menu.brunchItems = menuItems
                        "Lunch" -> menu.lunchItems = menuItems
                        "Lite Lunch" -> menu.liteLunchItems = menuItems
                        "Dinner" -> menu.dinnerItems = menuItems
                    }
                }
            }
            return menu
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

    // Helper function that gets menus for specific day
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

    fun parseOperatingHours(jsonArray: JSONArray): List<String> {
        val operatingHours = arrayListOf<String>()
        try {
            val hours = jsonArray.getJSONObject(0).getJSONArray("hours")
            for (i in 0 until hours.length()) {
                val segment = hours.getJSONObject(i).getJSONObject("dailyHours")
                val start = segment.getLong("startTimestamp")
                val end = segment.getLong("endTimestamp")
                operatingHours.add(parseTime(start) + " – " + parseTime(end))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return operatingHours
    }

    fun parseHistorical(jsonArray: JSONArray, day: String): List<Double> {
        val densities = arrayListOf<Double>()
        try {
            val facilityHistory = jsonArray.getJSONObject(0).getJSONObject("hours")
            val facOnDay = facilityHistory.getJSONObject(day)
            for (hour in 7..23) {
                densities.add(facOnDay.getDouble(hour.toString()))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return densities
    }

    private fun parseTime(timestamp: Long): String {
        val timeZone = Calendar.getInstance().timeZone
        var format = SimpleDateFormat("h:mma", Locale.US)
        if (DateFormat.is24HourFormat(DensityApplication.getAppContext())) {
            format = SimpleDateFormat("HH:mm", Locale.US)
        }
        format.timeZone = timeZone

        return format.format(Date(timestamp * 1000)).toLowerCase(Locale.US)
    }
}