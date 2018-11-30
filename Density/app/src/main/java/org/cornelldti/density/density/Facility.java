package org.cornelldti.density.density;

import java.io.Serializable;

public class Facility implements Serializable {
    private String name;
    private String id;
    private String opensAt;
    private String closesAt;
    private String address;
    public int occupancy_rating;
    public enum campus_location {NORTH, WEST, CENTRAL};
    private campus_location loc;

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
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.address = address;
        this.loc = location;
        this.occupancy_rating = occupancy_rating;
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

    public String getOpensAt()
    {
        return this.opensAt;
    }

    public void setOpensAt(String opensAt)
    {
        this.opensAt = opensAt;
    }

    public String getClosesAt()
    {
        return this.closesAt;
    }

    public void setClosesAt(String closesAt)
    {
        this.closesAt = closesAt;
    }

    public String getAddress()
    {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public void setLocation(campus_location loc)
    {
        this.loc = loc;
    }

}
