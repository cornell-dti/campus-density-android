package org.cornelldti.density.density;

import java.io.Serializable;

public class Facility implements Serializable {
    private String name;
    private String id;
    public int occupancy_rating;
    public enum campus_location {NORTH, WEST, CENTRAL};
    private campus_location loc;
    private boolean open;

    public Facility(String name, String id)
    {
        this.name = name;
        this.id = id;
    }

    public Facility (String name, String id, String opensAt, String closesAt,
                     String address,
                     campus_location location, int occupancy_rating)
    {
        this.name = name;
        this.id = id;
        this.loc = location;
        this.occupancy_rating = occupancy_rating;
    }

    public Facility setOpen(boolean open)
    {
        this.open = open;
        return this;
    }

    public boolean isOpen()
    {
        return this.open;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getDescription()
    {
        String description = "";
        switch (this.occupancy_rating)
        {
            case 0:
                description = "Very Empty";
                break;
            case 1:
                description = "Pretty Empty";
                break;
            case 2:
                description = "Pretty Crowded";
                break;
            case 3:
                description = "Very Crowded";
                break;
        }
        return description;
    }

    public int getOccupancy_rating()
    {
        return occupancy_rating;
    }

    public Facility setOccupancy_rating(int i)
    {
        if(i >= 0 && i <= 3) {
            this.occupancy_rating = i;
        }
        return this;
    }

    public campus_location getLocation()
    {
        return this.loc;
    }

    public String getLocationString()
    {
        String l = "";
        switch (this.loc)
        {
            case NORTH:
                l = "NORTH";
                break;
            case CENTRAL:
                l = "CENTRAL";
                break;
            case WEST:
                l = "WEST";
                break;
        }
        return l;
    }

    public Facility setLocation(String loc)
    {
        switch (loc)
        {
            case "north":
                this.loc = campus_location.NORTH;
                break;
            case "central":
                this.loc = campus_location.CENTRAL;
                break;
            case "west":
                this.loc = campus_location.WEST;
                break;
        }
        return this;
    }

}
