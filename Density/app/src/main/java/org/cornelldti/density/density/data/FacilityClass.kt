package org.cornelldti.density.density.data

import org.cornelldti.density.density.R
import java.io.Serializable

/**
 * This class holds the data model for dining facilities on campus.
 * Refer to this class for getting / setting specific dining facility information.
 */
class FacilityClass(val name: String, val id: String) : Serializable, Comparable<FacilityClass> {
    var occupancyRating: Int = 0
    var description: String? = null

    enum class CampusLocation { NORTH, WEST, CENTRAL }

    var location: CampusLocation? = null
        private set

    private var closingAt: Long = 0

    val isOpen: Boolean
        get() = this.closingAt != -1L

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

    override fun compareTo(other: FacilityClass): Int = when {
        !other.isOpen && this.isOpen -> -1
        other.isOpen && !this.isOpen -> 1
        other.occupancyRating < this.occupancyRating -> 1
        else -> if (other.occupancyRating > this.occupancyRating) {
            -1
        } else 0
    }

    fun setClosingAt(closingAt: Long) {
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
