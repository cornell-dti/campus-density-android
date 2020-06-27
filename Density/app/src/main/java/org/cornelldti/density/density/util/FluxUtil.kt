package org.cornelldti.density.density.util

import android.text.format.DateFormat
import org.cornelldti.density.density.DensityApplication
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

    val dayInt: Int
        get() = when (dayString) {
            "SUN" -> 0
            "MON" -> 1
            "TUE" -> 2
            "WED" -> 3
            "THU" -> 4
            "FRI" -> 5
            "SAT" -> 6
            else -> -1
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
     * This function provides the date in string format for historical endpoint request.
     */
    fun getDateObject(day: String): Date {
        val current = Calendar.getInstance()
        val checkFormat = SimpleDateFormat("E", Locale.US)

        var dayCheck = checkFormat.format(current.time).toUpperCase(Locale.US)
        while (dayCheck != day) {
            current.add(Calendar.DAY_OF_MONTH, 1)
            dayCheck = checkFormat.format(current.time).toUpperCase(Locale.US)
        }
        return current.time
    }

    /**
     * This function provides the date in string format for historical and operating hours endpoint request.
     */
    fun convertDateObjectToString(date: Date): String {
        val format = SimpleDateFormat("MM-dd-yyyy", Locale.US)
        return format.format(date)
    }

    /**
     * Temporary function to convert from MM-DD-YYYY to YYYY-MM-DD
     * Can remove this once we universally go by YYYY-MM-DD format for dates in backend TODO
     */
    fun reverseDateString(date: String): String {
        val components = date.split("-")
        return components[2] + "-" + components[0] + "-" + components[1]
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

    /**
     * @param daysAfter days after today to fetch date string for
     * @param yearBeginning is whether year should be in beginning of date string format
     * @return Date String for the date that is [daysAfter] days after the current date
     */
    fun getDateStringDaysAfter(daysAfter: Int, yearBeginning: Boolean): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysAfter)
        var format: SimpleDateFormat
        if (yearBeginning) {
            format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        } else {
            format = SimpleDateFormat("MM-dd-yyyy", Locale.US)
        }
        return format.format(calendar.time)
    }

    fun getDateDaysAfter(daysAfter: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysAfter)
        return calendar.time
    }

    /**
     * This function provides the current date for the menu endpoint request.
     * @param yearBeginning true if the format has year at beginning, false otherwise
     */
    fun getCurrentDate(yearBeginning: Boolean): String {
        val current = Calendar.getInstance()
        var format: SimpleDateFormat
        if (yearBeginning) {
            format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        } else {
            format = SimpleDateFormat("MM-dd-yyyy", Locale.US)
        }
        return format.format(current.time)
    }

    fun getCurrentDateObject(): Date {
        val current = Calendar.getInstance()
        return current.time
    }

    fun parseTime(timestamp: Long): String {
        val timeZone = Calendar.getInstance().timeZone
        var format = SimpleDateFormat("h:mma", Locale.US)
        if (DateFormat.is24HourFormat(DensityApplication.getAppContext())) {
            format = SimpleDateFormat("HH:mm", Locale.US)
        }
        format.timeZone = timeZone

        return format.format(Date(timestamp * 1000)).toLowerCase(Locale.US)
    }
}
