package org.cornelldti.density.density;

import java.io.Serializable;

public class Facility implements Serializable, Comparable<Facility> {
    private String name;
    private String id;
    public int occupancy_rating;
    private String description;
    private long closingAt;

    public void setClosingAt(long closingAt) {
        this.closingAt = closingAt;
    }

    public int getDensityResId() {
        if (!this.isOpen()) {
            return R.string.closed;
        }

        switch (this.occupancy_rating) {
            case 0:
                return R.string.very_empty;
            case 1:
                return R.string.pretty_empty;
            case 2:
                return R.string.pretty_crowded;
            case 3:
                return R.string.very_crowded;
        }

        return R.string.unknown;
    }

    @Override
    public int compareTo(Facility o) {
        if (!o.isOpen() && this.isOpen()) {
            return -1;
        }

        if (o.isOpen() && !this.isOpen()) {
            return 1;
        }

        if (o.getOccupancyRating() < this.getOccupancyRating()) {
            return 1;
        }

        if (o.getOccupancyRating() > this.getOccupancyRating()) {
            return -1;
        }

        return 0;
    }

    public enum CampusLocation {NORTH, WEST, CENTRAL}

    private CampusLocation loc;

    public Facility(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public Facility(String name, String id, String description, long nextOpen, long closingAt, String address, CampusLocation campusLocation, int occupancy_rating) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.loc = campusLocation;
        this.occupancy_rating = occupancy_rating;
        this.closingAt = closingAt;
    }

    public boolean isOpen() {
        return this.closingAt != -1;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public int getOccupancyRating() {
        return occupancy_rating;
    }

    public Facility setOccupancy_rating(int i) {
        if (i >= 0 && i <= 3) {
            this.occupancy_rating = i;
        }
        return this;
    }

    public CampusLocation getLocation() {
        return this.loc;
    }

    public String getLocationString() {
        String loc_string = "";
        switch (this.loc) {
            case NORTH:
                loc_string = "NORTH";
                break;
            case CENTRAL:
                loc_string = "CENTRAL";
                break;
            case WEST:
                loc_string = "WEST";
                break;
        }
        return loc_string;
    }

    public Facility setLocation(String loc) {
        switch (loc) {
            case "north":
                this.loc = CampusLocation.NORTH;
                break;
            case "central":
                this.loc = CampusLocation.CENTRAL;
                break;
            case "west":
                this.loc = CampusLocation.WEST;
                break;
        }
        return this;
    }
}
