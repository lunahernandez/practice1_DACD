package org.example.model;

public class Location {
    private String lat;
    private String lon;
    private String island;

    public Location(String lat, String lon, String island) {
        this.lat = lat;
        this.lon = lon;
        this.island = island;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getIsland() {
        return island;
    }

    public void setIsland(String island) {
        this.island = island;
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
