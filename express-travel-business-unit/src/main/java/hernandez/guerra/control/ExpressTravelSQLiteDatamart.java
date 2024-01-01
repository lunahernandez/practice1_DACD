package hernandez.guerra.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hernandez.guerra.exceptions.ExpressTravelBusinessUnitException;
import jakarta.jms.TextMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public record ExpressTravelSQLiteDatamart(String dbPath) implements ExpressTravelDatamart{
    //TODO initialize from datalake

    public ExpressTravelSQLiteDatamart {
        try {
            createTables();
        } catch (ExpressTravelBusinessUnitException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTables() throws ExpressTravelBusinessUnitException {
        try (Connection connection = connect()) {
            Statement statement = connection.createStatement();

            createWeatherTable(statement);
            createAccommodationTable(statement);

        } catch (SQLException e) {
            throw new ExpressTravelBusinessUnitException(e.getMessage(), e);
        }
    }

    private void createWeatherTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS weatherPredictions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "predictionTime TEXT," +
                "locationName TEXT," +
                "temp REAL," +
                "pop REAL," +
                "humidity INTEGER," +
                "clouds INTEGER," +
                "windSpeed REAL" +
                ");");
    }

    private void createAccommodationTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS accommodations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "locationName TEXT," +
                "url TEXT," +
                "name TEXT," +
                "city TEXT," +
                "lat TEXT," +
                "lng TEXT," +
                "rating REAL," +
                "totalPrice INTEGER" +
                ");");
    }

    public void initialize(String weatherTopicName, String accommodationTopicName, DatamartInitializer datamartInitializer)
            throws ExpressTravelBusinessUnitException {
        try (Connection connection = connect()) {
            clearDatamart(connection);

            File weatherFile = datamartInitializer.findLatestEventFile(weatherTopicName);
            File accommodationFile = datamartInitializer.findLatestEventFile(accommodationTopicName);
            System.out.println(weatherFile);
            System.out.println(accommodationFile);

            if (weatherFile != null) {
                processEventFile(weatherFile, "weatherPredictions", connection);
            }

            if (accommodationFile != null) {
                processEventFile(accommodationFile, "accommodations", connection);
            }

        } catch (SQLException e) {
            throw new ExpressTravelBusinessUnitException(e.getMessage(), e);
        }
    }

    private void clearDatamart(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM weatherPredictions;");
            statement.executeUpdate("DELETE FROM accommodations;");
        }
    }
    private void processEventFile(File eventFile, String tableName, Connection connection) throws ExpressTravelBusinessUnitException {
        try (BufferedReader reader = new BufferedReader(new FileReader(eventFile))) {
            String line;
            List<String> recentEvents = new ArrayList<>();
            String lastTimestamp = null;

            while ((line = reader.readLine()) != null) {
                String currentTimestamp = extractDateAndTime(line);
                System.out.println(currentTimestamp);

                if (lastTimestamp == null || currentTimestamp.compareTo(lastTimestamp) > 0) {
                    lastTimestamp = currentTimestamp;
                    recentEvents.clear();
                }
                System.out.println(lastTimestamp);

                recentEvents.add(line);
            }

            for (String eventData : recentEvents) {
                insertEventIntoTable(eventData, tableName, connection);
            }

        } catch (IOException e) {
            throw new ExpressTravelBusinessUnitException(e.getMessage(), e);
        }
    }

    private String extractDateAndTime(String eventData) {
        JsonObject json = JsonParser.parseString(eventData).getAsJsonObject();
        String fullTimestamp = json.get("ts").getAsString();
        return fullTimestamp.substring(0, 13);
    }

    private void insertEventIntoTable(String eventData, String tableName, Connection connection) throws ExpressTravelBusinessUnitException {
        switch (tableName) {
            case "weatherPredictions":
                insertWeatherPrediction(eventData, connection);
                break;
            case "accommodations":
                insertAccommodation(eventData, connection);
                break;
            default:
                System.out.println("Unknown table name: " + tableName);
        }
    }



    private void insertWeatherPrediction(String eventData, Connection connection) throws ExpressTravelBusinessUnitException {
            JsonObject json = JsonParser.parseString(eventData).getAsJsonObject();
            String insertSQL = "INSERT INTO weatherPredictions (predictionTime, locationName, temp, pop, humidity, clouds, windSpeed) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                setInsertWeatherParameters(preparedStatement, json);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new ExpressTravelBusinessUnitException(e.getMessage(), e);
            }

    }
    private static void setInsertWeatherParameters(PreparedStatement preparedStatement, JsonObject json) throws SQLException {
        preparedStatement.setString(1, json.get("predictionTime").getAsString());
        preparedStatement.setString(2, json.getAsJsonObject("location").get("name").getAsString());
        preparedStatement.setDouble(3, json.get("temp").getAsDouble());
        preparedStatement.setDouble(4, json.get("pop").getAsDouble());
        preparedStatement.setInt(5, json.get("humidity").getAsInt());
        preparedStatement.setInt(6, json.get("clouds").getAsInt());
        preparedStatement.setDouble(7, json.get("windSpeed").getAsDouble());
    }

    private void insertAccommodation(String eventData, Connection connection) throws ExpressTravelBusinessUnitException {
            JsonObject json = JsonParser.parseString(eventData).getAsJsonObject();
            String insertSQL = "INSERT INTO accommodations (locationName, url, name, city, lat, lng, rating, totalPrice) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                setInsertAccommodationParameters(preparedStatement, json);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new ExpressTravelBusinessUnitException(e.getMessage(), e);
            }
    }

    private static void setInsertAccommodationParameters(PreparedStatement preparedStatement, JsonObject json) throws SQLException {
        preparedStatement.setString(1, json.getAsJsonObject("location").get("name").getAsString());
        preparedStatement.setString(2, json.get("url").getAsString());
        preparedStatement.setString(3, json.get("name").getAsString());
        preparedStatement.setString(4, json.get("city").getAsString());
        preparedStatement.setString(5, json.get("lat").getAsString());
        preparedStatement.setString(6, json.get("lng").getAsString());
        preparedStatement.setDouble(7, json.get("rating").getAsDouble());
        preparedStatement.setInt(8, json.get("totalPrice").getAsInt());
    }



    public void insertWeatherPrediction(String locationName, String predictionTime, double temp,
                                        double pop, int humidity, int clouds, double windSpeed) throws ExpressTravelBusinessUnitException {
        try (Connection connection = connect()) {
            String insertSQL = "INSERT INTO weatherPredictions (predictionTime, locationName, temp, pop, humidity, clouds, windSpeed) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                preparedStatement.setString(1, predictionTime);
                preparedStatement.setString(2, locationName);
                preparedStatement.setDouble(3, temp);
                preparedStatement.setDouble(4, pop);
                preparedStatement.setInt(5, humidity);
                preparedStatement.setInt(6, clouds);
                preparedStatement.setDouble(7, windSpeed);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ExpressTravelBusinessUnitException(e.getMessage(), e);
        }
    }


    private Connection connect() throws ExpressTravelBusinessUnitException {
        Connection conn;
        try {
            String url = "jdbc:sqlite:datamart.db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new ExpressTravelBusinessUnitException(e.getMessage(), e);
        }
        return conn;
    }

    @Override
    public void update(TextMessage textMessage, String topicName) {

    }
}

