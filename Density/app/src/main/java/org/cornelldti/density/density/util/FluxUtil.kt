package org.cornelldti.density.density.util

import java.text.SimpleDateFormat
import java.util.*

object FluxUtil {

    val daysList = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    val dayString: String
        get() = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "SUN"
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            else -> ""
        }

    fun dayFullString(day: String): String = when (day) {
        "MON" -> "Monday"
        "TUE" -> "Tuesday"
        "WED" -> "Wednesday"
        "THU" -> "Thursday"
        "FRI" -> "Friday"
        "SAT" -> "Saturday"
        "SUN" -> "Sunday"
        else -> ""
    }

    /**
     * getDate(day) provides the date for historical endpoint request.
     */
    fun getDate(day: String): String {
        val current = Calendar.getInstance()
        val format = SimpleDateFormat("MM-dd-yy", Locale.US)
        val checkFormat = SimpleDateFormat("E", Locale.US)

        var dayCheck = checkFormat.format(current.time).toUpperCase(Locale.US)
        while (dayCheck != day) {
            current.add(Calendar.DAY_OF_MONTH, 1)
            dayCheck = checkFormat.format(current.time).toUpperCase(Locale.US)
        }

        return format.format(current.time)
    }

    fun getDayDifference(currentDay: String, tappedDay: String): Int {
        var x: Int = daysList.indexOf(currentDay)
        var count = 0
        while (daysList[x % 7] != tappedDay) {
            count += 1
            x += 1
        }
        return count
    }

    fun getDateStringDaysAfter(daysAfter: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysAfter)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return format.format(calendar.time)
    }

    fun getDateDaysAfter(daysAfter: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysAfter)
        return calendar.time
    }

    /**
     * getCurrentDate() provides the current date for the menu endpoint request.
     */
    fun getCurrentDate(): String {
        val current = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return format.format(current.time)
    }
}
