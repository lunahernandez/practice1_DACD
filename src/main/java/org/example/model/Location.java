package org.example.model;

public record Location(String lat, String lon, String island) {


    @Override
    public String toString() {
        return "Location{" +
                "lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", island='" + island + '\'' +
                '}';
    }
}
