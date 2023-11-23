package hernandez.guerra.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import com.google.gson.*;
import hernandez.guerra.model.Location;
import hernandez.guerra.model.Weather;

import java.net.MalformedURLException;
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
        try {
            HttpURLConnection connection = openOpenWeatherConnection(lat, lon, apiKey);
            return getJsonObjectFromConnection(connection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpURLConnection openOpenWeatherConnection(String lat, String lon, String apiKey) throws IOException {
        URL url = getUrl(lat, lon, apiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return connection;
    }

    private JsonObject getJsonObjectFromConnection(HttpURLConnection connection) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            connection.disconnect();
        }
    }

    private static URL getUrl(String lat, String lon, String apiKey) throws MalformedURLException {
        String apiUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat +
                "&lon=" + lon + "&units=metric&appid=" + apiKey;
        return new URL(apiUrl);
    }

    private List<Weather> convertJsonToWeatherList(JsonObject jsonObject, Location location) {
        List<Weather> weatherList = new ArrayList<>();
        JsonArray weatherListArray = jsonObject.getAsJsonArray("list");

        for (JsonElement element : weatherListArray) {
            JsonObject weatherInfo = element.getAsJsonObject();

            if (isMidday(weatherInfo.get("dt").getAsLong())) {
                Weather weather = createWeatherFromJson(weatherInfo, location);
                weatherList.add(weather);
            }
        }

        return weatherList;
    }

    private Weather createWeatherFromJson(JsonObject weatherInfo, Location location) {
        Instant ts = Instant.ofEpochSecond(weatherInfo.get("dt").getAsLong());
        double temperature = weatherInfo.getAsJsonObject("main").get("temp").getAsDouble();
        double pop = weatherInfo.get("pop").getAsDouble();
        int humidity = weatherInfo.getAsJsonObject("main").get("humidity").getAsInt();
        int clouds = weatherInfo.getAsJsonObject("clouds").get("all").getAsInt();
        double windSpeed = weatherInfo.getAsJsonObject("wind").get("speed").getAsDouble();

        return new Weather(ts, location, temperature, pop, humidity, clouds, windSpeed);
    }

    private static boolean isMidday(long timestamp) {
        Instant ts = Instant.ofEpochSecond(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(ts, ZoneOffset.UTC);
        return dateTime.getHour() == 12 && dateTime.getMinute() == 0 && dateTime.getSecond() == 0;

    }
}

