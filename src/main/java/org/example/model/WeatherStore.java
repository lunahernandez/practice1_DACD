package org.example.model;

public interface WeatherStore extends AutoCloseable{

    void save(Weather weather);

    public void get();
}
