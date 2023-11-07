package org.example.model;


public class Weather {

    private final String dateTime;
    private final Location location;
    private final double temp;
    private final double pop;
    private final int humidity;
    private final int clouds;
    private final double windSpeed;

    public Weather(String dateTime, Location location, double temp, double pop, int humidity, int clouds, double windSpeed) {
        this.dateTime = dateTime;
        this.location = location;
        this.temp = temp;
        this.pop = pop;
        this.humidity = humidity;
        this.clouds = clouds;
        this.windSpeed = windSpeed;
    }

    public String getDateTime() {
        return dateTime;
    }

    public Location getLocation() {
        return location;
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

    @Override
    public String toString() {
        return "Weather{" +
                "dateTime='" + dateTime + '\'' +
                ", location=" + location +
                ", temp=" + temp +
                ", pop=" + pop +
                ", humidity=" + humidity +
                ", clouds=" + clouds +
                ", windSpeed=" + windSpeed +
                '}';
    }
}
