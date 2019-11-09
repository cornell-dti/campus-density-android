package org.cornelldti.density.density.data

import org.cornelldti.density.density.R
import java.io.Serializable

class FacilityClass(val name: String, val id: String) : Serializable, Comparable<FacilityClass> {
    var occupancyRating: Int = 0
    var description: String? = null
    private var closingAt: Long = 0

    val densityResId: Int
        get() = if (!this.isOpen) {
            R.string.closed
        } else when (this.occupancyRating) {
            0 -> R.string.very_empty
            1 -> R.string.pretty_empty
            2 -> R.string.pretty_crowded
            3 -> R.string.very_crowded
            else -> R.string.unknown
        }

    var location: CampusLocation? = null
        private set

    val isOpen: Boolean
        get() = this.closingAt != -1L

    val locationString: String
        get() = when (this.location) {
            CampusLocation.NORTH -> "NORTH"
            CampusLocation.CENTRAL -> "CENTRAL"
            CampusLocation.WEST -> "WEST"
            else -> ""
        }

    fun setClosingAt(closingAt: Long) {
        this.closingAt = closingAt
    }

    override fun compareTo(other: FacilityClass): Int = when {
        !other.isOpen && this.isOpen -> -1
        other.isOpen && !this.isOpen -> 1
        other.occupancyRating < this.occupancyRating -> 1
        else -> if (other.occupancyRating > this.occupancyRating) {
            -1
        } else 0
    }

    enum class CampusLocation {
        NORTH, WEST, CENTRAL
    }

    constructor(name: String, id: String, description: String, nextOpen: Long, closingAt: Long, address: String, campusLocation: CampusLocation, occupancy_rating: Int) : this(name = name, id = id) {
        this.description = description
        this.location = campusLocation
        this.occupancyRating = occupancy_rating
        this.closingAt = closingAt
    }

    fun setOccupancyRating(i: Int): FacilityClass {
        if (i in 0..3) {
            this.occupancyRating = i
        }
        return this
    }

    fun setLocation(loc: String): FacilityClass {
        when (loc) {
            "north" -> this.location = CampusLocation.NORTH
            "central" -> this.location = CampusLocation.CENTRAL
            "west" -> this.location = CampusLocation.WEST
        }
        return this
    }
}
