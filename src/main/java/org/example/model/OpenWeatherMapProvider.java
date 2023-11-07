package org.example.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import com.google.gson.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OpenWeatherMapProvider implements WeatherProvider {
    private final String apiKey;

    public OpenWeatherMapProvider(String apikey) {
        this.apiKey = apikey;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public List<Weather> get(Location location) {
        JsonObject jsonObject = getJsonObjectFromOpenWeather(location.getLat(), location.getLon(), getApiKey());
        return convertJsonToWeatherList(jsonObject, location);
    }

    private JsonObject getJsonObjectFromOpenWeather(String lat, String lon, String apiKey) {
        JsonObject jsonObject = null;

        try {
            String apiUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat +
                    "&lon=" + lon + "&units=metric&appid=" + apiKey;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            connection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return jsonObject;
    }

    private List<Weather> convertJsonToWeatherList(JsonObject jsonObject, Location location) {
        List<Weather> weatherList = new ArrayList<>();
        JsonArray weatherListArray = jsonObject.getAsJsonArray("list");
        for (JsonElement element : weatherListArray) {
            JsonObject weatherInfo = element.getAsJsonObject();

            String dateTime = weatherInfo.get("dt_txt").getAsString();

            if (isMidday(dateTime)) {
                JsonObject mainInfo = weatherInfo.getAsJsonObject("main");
                JsonObject cloudsInfo = weatherInfo.getAsJsonObject("clouds");
                JsonObject windInfo = weatherInfo.getAsJsonObject("wind");

                double temperature = mainInfo.get("temp").getAsDouble();
                double pop = weatherInfo.get("pop").getAsDouble();
                int humidity = mainInfo.get("humidity").getAsInt();
                int clouds = cloudsInfo.get("all").getAsInt();
                double windSpeed = windInfo.get("speed").getAsDouble();

                Weather weather = new Weather(dateTime, location, temperature, pop, humidity, clouds, windSpeed);
                weatherList.add(weather);
            }
        }
        return weatherList;
    }

    private static boolean isMidday(String dateTime) {
        return dateTime.endsWith("12:00:00");
    }
}

