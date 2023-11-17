package org.example.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import com.google.gson.*;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
        JsonObject jsonObject = getJsonObjectFromOpenWeather(location.lat(), location.lon(), getApiKey());
        return convertJsonToWeatherList(jsonObject, location);
    }

    private JsonObject getJsonObjectFromOpenWeather(String lat, String lon, String apiKey) {
        JsonObject jsonObject;

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

            if (isMidday(weatherInfo.get("dt").getAsLong())) {
                JsonObject mainInfo = weatherInfo.getAsJsonObject("main");
                JsonObject cloudsInfo = weatherInfo.getAsJsonObject("clouds");
                JsonObject windInfo = weatherInfo.getAsJsonObject("wind");

                Instant ts = Instant.ofEpochSecond(weatherInfo.get("dt").getAsLong());
                double temperature = mainInfo.get("temp").getAsDouble();
                double pop = weatherInfo.get("pop").getAsDouble();
                int humidity = mainInfo.get("humidity").getAsInt();
                int clouds = cloudsInfo.get("all").getAsInt();
                double windSpeed = windInfo.get("speed").getAsDouble();

                Weather weather = new Weather(ts, location, temperature, pop, humidity, clouds, windSpeed);
                weatherList.add(weather);
            }
        }
        return weatherList;
    }

    private static boolean isMidday(long timestamp) {
        Instant ts = Instant.ofEpochSecond(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(ts, ZoneOffset.UTC);
        return dateTime.getHour() == 12 && dateTime.getMinute() == 0 && dateTime.getSecond() == 0;

    }
}

