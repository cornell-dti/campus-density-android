package org.cornelldti.density.density.network

import android.text.format.DateFormat
import org.cornelldti.density.density.DensityApplication
import org.cornelldti.density.density.data.FacilityClass
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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