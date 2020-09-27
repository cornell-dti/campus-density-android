package org.cornelldti.density.density.data

class OperatingHoursClass(todayOperatingHours: List<Pair<Long, Long>>, tomorrowFirstOpHours: Pair<Long, Long>) {
    lateinit var todayOperatingHours: List<Pair<Long, Long>>
    lateinit var tomorrowFirstOpHours: Pair<Long, Long>
    init {
        this.todayOperatingHours = todayOperatingHours
        this.tomorrowFirstOpHours = tomorrowFirstOpHours
    }
}