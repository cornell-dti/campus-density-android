package org.cornelldti.density.density

import java.io.Serializable

class Facility : Serializable, Comparable<Facility> {
    var name: String? = null
    var id: String? = null
    var occupancyRating: Int = 0
    var description: String? = null
    private var closingAt: Long = 0

    val densityResId: Int
        get() {
            if (!this.isOpen) {
                return R.string.closed
            }

            when (this.occupancyRating) {
                0 -> return R.string.very_empty
                1 -> return R.string.pretty_empty
                2 -> return R.string.pretty_crowded
                3 -> return R.string.very_crowded
            }

            return R.string.unknown
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

    override fun compareTo(o: Facility): Int {
        if (!o.isOpen && this.isOpen) {
            return -1
        }

        if (o.isOpen && !this.isOpen) {
            return 1
        }

        if (o.occupancyRating < this.occupancyRating) {
            return 1
        }

        return if (o.occupancyRating > this.occupancyRating) {
            -1
        } else 0

    }

    enum class CampusLocation {
        NORTH, WEST, CENTRAL
    }

    constructor(name: String, id: String) {
        this.name = name
        this.id = id
    }

    constructor(name: String, id: String, description: String, nextOpen: Long, closingAt: Long, address: String, campusLocation: CampusLocation, occupancy_rating: Int) {
        this.name = name
        this.id = id
        this.description = description
        this.location = campusLocation
        this.occupancyRating = occupancy_rating
        this.closingAt = closingAt
    }

    fun setOccupancy_rating(i: Int): Facility {
        if (i in 0..3) {
            this.occupancyRating = i
        }
        return this
    }

    fun setLocation(loc: String): Facility {
        when (loc) {
            "north" -> this.location = CampusLocation.NORTH
            "central" -> this.location = CampusLocation.CENTRAL
            "west" -> this.location = CampusLocation.WEST
        }
        return this
    }
}
