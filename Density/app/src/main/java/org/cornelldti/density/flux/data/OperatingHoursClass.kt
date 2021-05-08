package org.cornelldti.density.flux.data

/**
 * This class holds the data model for operating hours of dining locations.
 * Operating hours indicate day-to-day availabilities of each facility.
 */
data class OperatingHoursClass(val todayOperatingHours: List<Pair<Long, Long>>, val tomorrowFirstOpHours: Pair<Long, Long>)