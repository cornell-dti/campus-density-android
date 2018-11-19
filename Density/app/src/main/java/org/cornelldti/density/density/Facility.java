package org.cornelldti.density.density;

public class Facility {
    private String name;
    private String id;
    private String opensAt;
    private String closesAt;
    private String address;
    private double currentCapacity;
    private double totalCapacity;
    private boolean isFavorite;
    private double occupancy_percentage;
    public enum Occupancy_Rating {VERY_CROWDED, PRETTY_CROWDED, PRETTY_EMPTY, VERY_EMPTY};
    public enum campus_location {NORTH, WEST, CENTRAL};
    private campus_location loc;

    public Facility (String name, String id, String opensAt, String closesAt, String address,
                     double currentCapacity, double totalCapacity, boolean isFavorite,
                     campus_location location)
    {
        this.name = name;
        this.id = id;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.address = address;
        this.currentCapacity = currentCapacity;
        this.totalCapacity = totalCapacity;
        this.isFavorite = isFavorite;
        this.occupancy_percentage = currentCapacity / totalCapacity;
        this.loc = location;
    }

    public String getName()
    {
        return this.name;
    }

    public String getId()
    {
        return this.id;
    }

    public String getOpensAt()
    {
        return this.opensAt;
    }

    public String getClosesAt()
    {
        return this.closesAt;
    }

    public String getAddress()
    {
        return this.address;
    }

    public double getCurrentCapacity()
    {
        return this.currentCapacity;
    }

    public double getTotalCapacity()
    {
        return this.totalCapacity;
    }

    public boolean isFavorite()
    {
        return this.isFavorite;
    }

    public double getOccupancy_percentage()
    {
        return occupancy_percentage;
    }

    public Occupancy_Rating get_occupancy_rating()
    {
        double percent_occupancy = occupancy_percentage;
        Occupancy_Rating rating;

        if(percent_occupancy >= 0.75)
        {
            rating = Occupancy_Rating.VERY_CROWDED;
        }
        else if(percent_occupancy >= 0.5)
        {
            rating = Occupancy_Rating.PRETTY_CROWDED;
        }
        else if(percent_occupancy >= 0.25)
        {
            rating = Occupancy_Rating.PRETTY_EMPTY;
        }
        else
        {
            rating = Occupancy_Rating.VERY_EMPTY;
        }
        return rating;
    }

    public String getDescription()
    {
        String description = "";
        Occupancy_Rating rating = get_occupancy_rating();
        switch (rating)
        {
            case VERY_EMPTY:
                description = "Very Empty";
                break;
            case PRETTY_EMPTY:
                description = "Pretty Empty";
                break;
            case PRETTY_CROWDED:
                description = "Pretty Crowded";
                break;
            case VERY_CROWDED:
                description = "Very Crowded";
                break;
        }
        return description;
    }

    public campus_location getLocation()
    {
        return this.loc;
    }

}
