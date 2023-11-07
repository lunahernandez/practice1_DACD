package org.example.model;

import java.time.Instant;

public class Weather {
    //TODO delete ts
    //TODO attributes final
    private final double temp;
    private final double pop;
    private final int humidity;
    private final int clouds;
    private final double windSpeed;
    private final String dateTime;
    private final Instant ts;
    private final Location location;

    public Weather(double temp, double pop, int humidity, int clouds, double windSpeed, String dateTime,
                   Instant ts, Location location) {
        this.temp = temp;
        this.pop = pop;
        this.humidity = humidity;
        this.clouds = clouds;
        this.windSpeed = windSpeed;
        this.dateTime = dateTime;
        this.ts = ts;
        this.location = location;
    }

    public double getTemp() {
        return temp;
    }

    public double getPop() {
        return pop;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getClouds() {
        return clouds;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String getDateTime() {
        return dateTime;
    }

    public Instant getTs() {
        return ts;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "temp=" + temp +
                ", pop=" + pop +
                ", humidity=" + humidity +
                ", clouds=" + clouds +
                ", windSpeed=" + windSpeed +
                ", dateTime='" + dateTime + '\'' +
                ", ts=" + ts +
                ", location=" + location +
                '}';
    }
}
