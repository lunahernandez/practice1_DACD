package hernandez.guerra.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hernandez.guerra.exceptions.ExpressTravelBusinessUnitException;
import hernandez.guerra.model.AccommodationData;
import hernandez.guerra.model.WeatherData;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public record ExpressTravelSQLiteDatamart(String dbPath) implements ExpressTravelDatamart {
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
                "reviewsCount INTEGER," +
                "rating REAL," +
                "totalPrice INTEGER" +
                ");");
    }

    public void initialize(String weatherTopicName, String accommodationTopicName, DatamartInitializer datamartInitializer)
            throws ExpressTravelBusinessUnitException {
        try (Connection connection = connect()) {
            clearDatamart(connection);

            initializeWeather(datamartInitializer, weatherTopicName, connection);
            initializeAccommodation(datamartInitializer, accommodationTopicName, connection);

        } catch (SQLException e) {
            throw new ExpressTravelBusinessUnitException(e.getMessage(), e);
        }
    }

    private void initializeWeather(
            DatamartInitializer datamartInitializer, String weatherTopicName, Connection connection
    ) throws ExpressTravelBusinessUnitException {
        File weatherFile = datamartInitializer.findLatestEventFile(weatherTopicName);
        processEventFileIfNotNull(weatherFile, "weatherPredictions", connection);
    }

    private void initializeAccommodation(
            DatamartInitializer datamartInitializer, String accommodationTopicName, Connection connection
    ) throws ExpressTravelBusinessUnitException {
        File accommodationFile = datamartInitializer.findLatestEventFile(accommodationTopicName);
        processEventFileIfNotNull(accommodationFile, "accommodations", connection);
    }

    private void processEventFileIfNotNull(File eventFile, String tableName, Connection connection)
            throws ExpressTravelBusinessUnitException {
        if (eventFile != null) {
            processEventFile(eventFile, tableName, connection);
        }
    }


    private void clearDatamart(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM weatherPredictions;");
            statement.executeUpdate("DELETE FROM accommodations;");
        }
    }

    private void processEventFile(File eventFile, String tableName, Connection connection)
            throws ExpressTravelBusinessUnitException {
        try (BufferedReader reader = new BufferedReader(new FileReader(eventFile))) {
            List<String> recentEvents = readRecentEvents(reader);

            for (String eventData : recentEvents) {
                insertEventIntoTable(eventData, tableName, connection);
            }

        } catch (IOException e) {
            throw new ExpressTravelBusinessUnitException(e.getMessage(), e);
        }
    }

    private List<String> readRecentEvents(BufferedReader reader) throws IOException {
        List<String> recentEvents = new ArrayList<>();
        String line;
        String lastTimestamp = null;

        while ((line = reader.readLine()) != null) {
            String currentTimestamp = extractDateAndTime(line);
            lastTimestamp = updateIfNeeded(lastTimestamp, currentTimestamp, recentEvents);
            recentEvents.add(line);
        }

        return recentEvents;
    }

    private static String updateIfNeeded(String lastTimestamp, String currentTimestamp, List<String> recentEvents) {
        if (lastTimestamp == null || currentTimestamp.compareTo(lastTimestamp) > 0) {
            lastTimestamp = currentTimestamp;
            recentEvents.clear();
        }
        return lastTimestamp;
    }


    private String extractDateAndTime(String eventData) {
        JsonObject json = JsonParser.parseString(eventData).getAsJsonObject();
        String fullTimestamp = json.get("ts").getAsString();
        return fullTimestamp.substring(0, 13);
    }

    private void insertEventIntoTable(String eventData, String tableName, Connection connection)
            throws ExpressTravelBusinessUnitException {
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
        String insertSQL =
                "INSERT INTO accommodations (locationName, url, name, city, lat, lng, reviewsCount, rating, totalPrice) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
        preparedStatement.setInt(7, json.get("reviewsCount").getAsInt());
        preparedStatement.setDouble(8, json.get("rating").getAsDouble());
        preparedStatement.setInt(9, json.get("totalPrice").getAsInt());
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
    public void update(TextMessage textMessage, String topicName) throws ExpressTravelBusinessUnitException {
        try (Connection connection = connect()) {
            String tableName = getTableName(topicName);
            insertEvent(textMessage.getText(), tableName, connection);
            deleteOldData(connection, tableName, extractDateAndTime(textMessage.getText()));
        } catch (SQLException | JMSException e) {
            throw new ExpressTravelBusinessUnitException(e.getMessage(), e);
        }
    }

    private String getTableName(String topicName) {
        return switch (topicName) {
            case "prediction.Weather" -> "weatherPredictions";
            case "prediction.Accommodation" -> "accommodations";
            default -> throw new IllegalArgumentException("Unrecognized topic: " + topicName);
        };
    }


    private void insertEvent(String eventData, String tableName, Connection connection)
            throws ExpressTravelBusinessUnitException {
        switch (tableName) {
            case "weatherPredictions":
            case "accommodations":
                insertEventIntoTable(eventData, tableName, connection);
                break;
            default:
                System.out.println("Unknown table name: " + tableName);
        }
    }


    private void deleteOldData(Connection connection, String tableName, String eventTimestamp) throws SQLException {
        String deleteSQL = "DELETE FROM " + tableName + " WHERE SUBSTR(ts, 1, 13) < ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {
            preparedStatement.setString(1, eventTimestamp.substring(0, 13));
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<WeatherData> getWeather(String location) throws ExpressTravelBusinessUnitException {
        List<WeatherData> result = new ArrayList<>();

        try (Connection connection = connect()) {
            String sql = "SELECT * FROM weatherPredictions WHERE locationName = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, location);
                executeQuery(statement, result);
            }
        } catch (SQLException | ExpressTravelBusinessUnitException e) {
            handleException(e);
        }

        return result;
    }

    private void executeQuery(PreparedStatement statement, List<WeatherData> result) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            getWeatherData(result, resultSet);
        }
    }

    private void getWeatherData(List<WeatherData> result, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            WeatherData weatherData = createWeatherDataFromResultSet(resultSet);
            result.add(weatherData);
        }
    }

    private WeatherData createWeatherDataFromResultSet(ResultSet resultSet) throws SQLException {
        String locationName = resultSet.getString("locationName");
        double temp = resultSet.getDouble("temp");
        double pop = resultSet.getDouble("pop");
        int humidity = resultSet.getInt("humidity");
        int clouds = resultSet.getInt("clouds");
        double windSpeed = resultSet.getDouble("windSpeed");

        return new WeatherData(locationName, temp, pop, humidity, clouds, windSpeed);
    }

    private void handleException(Exception e) throws ExpressTravelBusinessUnitException {
        if (e instanceof SQLException) {
            throw new ExpressTravelBusinessUnitException("SQL Exception: " + e.getMessage(), e);
        } else if (e instanceof ExpressTravelBusinessUnitException) {
            throw new RuntimeException("Business Unit Exception: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AccommodationData> getAccommodation(String location) throws ExpressTravelBusinessUnitException {
        List<AccommodationData> result = new ArrayList<>();

        try (Connection connection = connect()) {
            String sql = "SELECT * FROM accommodations WHERE locationName = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, location);
                executeQueryAndGetAccommodationData(statement, result);
            }
        } catch (SQLException | ExpressTravelBusinessUnitException e) {
            handleException(e);
        }

        return result;
    }

    private static void executeQueryAndGetAccommodationData(PreparedStatement statement, List<AccommodationData> result)
            throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                AccommodationData accommodationData = createAccommodationDataFromResultSet(resultSet);
                result.add(accommodationData);
            }
        }
    }

    private static AccommodationData createAccommodationDataFromResultSet(ResultSet resultSet) throws SQLException {
        String locationName = resultSet.getString("locationName");
        String url = resultSet.getString("url");
        String name = resultSet.getString("name");
        String city = resultSet.getString("city");
        String lat = resultSet.getString("lat");
        String lng = resultSet.getString("lng");
        int reviewsCount = resultSet.getInt("reviewsCount");
        double rating = resultSet.getDouble("rating");
        int totalPrice = resultSet.getInt("totalPrice");

        return new AccommodationData(locationName, url, name, city,
                lat, lng, reviewsCount, rating, totalPrice);
    }

    public Set<String> getAllLocations(String tableName) throws ExpressTravelBusinessUnitException {
        Set<String> locations = new HashSet<>();

        try (Connection connection = connect()) {
            String sql = "SELECT DISTINCT locationName FROM " + tableName;
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                addLocationToSet(resultSet, locations);
            }
        } catch (SQLException e) {
            throw new ExpressTravelBusinessUnitException(e.getMessage(), e);
        }

        return locations;
    }

    private static void addLocationToSet(ResultSet resultSet, Set<String> locations) throws SQLException {
        while (resultSet.next()) {
            locations.add(resultSet.getString("locationName"));
        }
    }
}

