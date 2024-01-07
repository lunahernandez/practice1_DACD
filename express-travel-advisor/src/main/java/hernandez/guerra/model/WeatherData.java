package hernandez.guerra.model;

import java.util.Objects;

public final class WeatherData {
    private final String locationName;
    private final double temp;
    private final double pop;
    private final int humidity;
    private final int clouds;
    private final double windSpeed;
    private double score;

    public WeatherData(String locationName, double temp, double pop, int humidity, int clouds, double windSpeed) {
        this.locationName = locationName;
        this.temp = temp;
        this.pop = pop;
        this.humidity = humidity;
        this.clouds = clouds;
        this.windSpeed = windSpeed;
    }

    public String locationName() {
        return locationName;
    }

    public double temp() {
        return temp;
    }

    public double pop() {
        return pop;
    }

    public int humidity() {
        return humidity;
    }

    public int clouds() {
        return clouds;
    }

    public double windSpeed() {
        return windSpeed;
    }

    public double score() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WeatherData) obj;
        return Objects.equals(this.locationName, that.locationName) &&
                Double.doubleToLongBits(this.temp) == Double.doubleToLongBits(that.temp) &&
                Double.doubleToLongBits(this.pop) == Double.doubleToLongBits(that.pop) &&
                this.humidity == that.humidity &&
                this.clouds == that.clouds &&
                Double.doubleToLongBits(this.windSpeed) == Double.doubleToLongBits(that.windSpeed) &&
                Double.doubleToLongBits(this.score) == Double.doubleToLongBits(that.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationName, temp, pop, humidity, clouds, windSpeed, score);
    }

    @Override
    public String toString() {
        return "WeatherData[" +
                "locationName=" + locationName + ", " +
                "temp=" + temp + ", " +
                "pop=" + pop + ", " +
                "humidity=" + humidity + ", " +
                "clouds=" + clouds + ", " +
                "windSpeed=" + windSpeed + ", " +
                "score=" + score + ']';
    }


}
