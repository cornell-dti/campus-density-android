package org.cornelldti.density.density.util

import java.util.Calendar

object FluxUtil {

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
}
