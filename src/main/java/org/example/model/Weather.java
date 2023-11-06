package org.example.model;

import java.time.Instant;

public class Weather {
    private double temp;
    private double pop;
    private int humidity;
    private int clouds;
    private double windSpeed;
    private String dateTime;
    private Instant ts;
    private Location location;

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

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getPop() {
        return pop;
    }

    public void setPop(double pop) {
        this.pop = pop;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getClouds() {
        return clouds;
    }

    public void setClouds(int clouds) {
        this.clouds = clouds;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Instant getTs() {
        return ts;
    }

    public void setTs(Instant ts) {
        this.ts = ts;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
