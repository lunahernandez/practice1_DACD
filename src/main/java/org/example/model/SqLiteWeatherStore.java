package org.example.model;

import java.sql.*;
import java.time.Instant;
import java.util.List;

public class SqLiteWeatherStore implements WeatherStore {
    private String dbPath;

    public SqLiteWeatherStore(String dbPath) {
        this.dbPath = dbPath;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void save(Weather weather) {
        try (Connection connection = connect(dbPath)) {
            insert(connection,
                    weather.getLocation().getIsland(),
                    weather.getDateTime(),
                    weather.getTemp(),
                    weather.getPop(),
                    weather.getHumidity(),
                    weather.getClouds(),
                    weather.getWindSpeed(),
                    String.valueOf(weather.getTs()),
                    weather.getLocation().getLat() + ", " + weather.getLocation().getLon());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void open(List<Location> locationList) {
        try (SqLiteWeatherStore weatherStore = new SqLiteWeatherStore(this.dbPath)) {
            Connection connection = connect(weatherStore.getDbPath());
            Statement statement = connection.createStatement();

            for (Location location : locationList) {
                createTable(statement, location.getIsland());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() throws Exception {

    }


    private static void displayWeatherData(Connection connection, String tableName) throws SQLException {
        Statement statement = connection.createStatement();
        String query = "SELECT * FROM " + tableName;
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String dateTime = resultSet.getString("dateTime");
            double temp = resultSet.getDouble("temp");
            double pop = resultSet.getDouble("pop");
            int humidity = resultSet.getInt("humidity");
            int clouds = resultSet.getInt("clouds");
            double windSpeed = resultSet.getDouble("windSpeed");
            String ts = resultSet.getString("ts");
            String location = resultSet.getString("location");

            System.out.println("ID: " + id);
            System.out.println("DateTime: " + dateTime);
            System.out.println("Temperature: " + temp);
            System.out.println("Precipitation: " + pop);
            System.out.println("Humidity: " + humidity);
            System.out.println("Clouds: " + clouds);
            System.out.println("Wind speed: " + windSpeed);
            System.out.println("TS: " + ts);
            System.out.println("Location: " + location);
            System.out.println();
        }
    }

    private static void delete(Statement statement, String tableName, String condition) throws SQLException {
        String deleteSQL = "DELETE FROM " + tableName + " WHERE " + condition;
        statement.execute(deleteSQL);
    }

    private static void dropTable(Statement statement, String tableName) throws SQLException {
        String dropTableSQL = "DROP TABLE IF EXISTS " + tableName + ";";
        statement.execute(dropTableSQL);
    }

    public static void createTable(Statement statement, String tableName) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "dateTime TEXT," +
                "temp REAL," +
                "pop REAL," +
                "humidity INTEGER," +
                "clouds INTEGER," +
                "windSpeed REAL," +
                "ts TEXT," +
                "location TEXT" +
                ");");
    }

    private static void insert(
            Connection connection, String tableName, String dateTime, double temp, double pop, int humidity,
            int clouds, double windSpeed, String ts, String location) throws SQLException {
        String insertSQL =
                "INSERT INTO " + tableName + " (dateTime, temp, pop, humidity, clouds, windSpeed, ts, location) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
        preparedStatement.setString(1, dateTime);
        preparedStatement.setDouble(2, temp);
        preparedStatement.setDouble(3, pop);
        preparedStatement.setInt(4, humidity);
        preparedStatement.setInt(5, clouds);
        preparedStatement.setDouble(6, windSpeed);
        preparedStatement.setString(7, ts);
        preparedStatement.setString(8, location);
        preparedStatement.executeUpdate();
    }


    private static void update(Statement statement, String tableName, String newData, String where)
            throws SQLException {
        statement.execute("UPDATE " + tableName + "\n" +
                "SET " + newData + " \n" +
                "WHERE " + where + ";");
        System.out.println("Table " + tableName + " updated");
    }

    public static Connection connect(String dbPath) {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + dbPath;
            conn = DriverManager.getConnection(url);
            return conn;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    @Override
    public void get(Location location, Instant ts) {

    }
}
