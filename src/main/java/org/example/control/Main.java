package org.example.control;

import org.example.model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.example.model.SqLiteWeatherStore.connect;
import static org.example.model.SqLiteWeatherStore.createTable;

public class Main {

    public static void main(String[] args) {
        List<Location> locationList = loadLocations();
        createTables(locationList);
        String apikey = String.valueOf(readAPIKey("src/main/java/org/example/model/apikey.txt"));

        WeatherProvider weatherProvider = new OpenWeatherMapProvider(apikey);
        SqLiteWeatherStore sqLiteWeatherStore = new SqLiteWeatherStore("src/main/resources/weather.db");

        for (Location location : locationList) {
            for (Weather weather : weatherProvider.get(location, Instant.now())) {
                sqLiteWeatherStore.save(weather);
            }
        }
    }

    public static void createTables(List<Location> locationList) {
        try (SqLiteWeatherStore weatherStore = new SqLiteWeatherStore("src/main/resources/weather.db")) {
            Connection connection = connect(weatherStore.getDbPath());
            Statement statement = connection.createStatement();

            for (Location location : locationList) {
                createTable(statement, location.getIsland());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Location> loadLocations() {
        List<Location> locationList = new ArrayList<>();
        locationList.add(new Location("28.01", "-15.53", "GC"));
        locationList.add(new Location("28.40", "-13.86", "FTV"));
        locationList.add(new Location("28.97", "-13.55", "LZ"));
        locationList.add(new Location("29.28", "-13.50", "LGR"));
        locationList.add(new Location("28.46", "-16.25", "TF"));
        locationList.add(new Location("28.75", "-17.89", "LP"));
        locationList.add(new Location("28.15", "-17.26", "GM"));
        locationList.add(new Location("27.80", "-17.89", "EH"));
        return locationList;
    }

    public static StringBuilder readAPIKey(String filePath) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }

            return content;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}