package org.example.model;

public class Location {
    private final String lat;
    private final String lon;
    private final String island;

    public Location(String lat, String lon, String island) {
        this.lat = lat;
        this.lon = lon;
        this.island = island;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }


    public String getIsland() {
        return island;
    }


    @Override
    public String toString() {
        return "Location{" +
                "lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", island='" + island + '\'' +
                '}';
    }
}
