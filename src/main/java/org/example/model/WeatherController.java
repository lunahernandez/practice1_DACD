package org.example.model;

import java.util.List;

public class WeatherController {
    private WeatherProvider weatherProvider;
    private WeatherStore weatherStore;
    private List<Location> locationList;

    public WeatherController(WeatherProvider weatherProvider, WeatherStore weatherStore, List<Location> locationList) {
        this.weatherProvider = weatherProvider;
        this.weatherStore = weatherStore;
        this.locationList = locationList;
    }

    public WeatherProvider getWeatherProvider() {
        return weatherProvider;
    }

    public void setWeatherProvider(WeatherProvider weatherProvider) {
        this.weatherProvider = weatherProvider;
    }

    public WeatherStore getWeatherStore() {
        return weatherStore;
    }

    public void setWeatherStore(WeatherStore weatherStore) {
        this.weatherStore = weatherStore;
    }

    public List<Location> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<Location> locationList) {
        this.locationList = locationList;
    }

    //TODO execute: hace toda la tarea (aquí va el timertask)
}
