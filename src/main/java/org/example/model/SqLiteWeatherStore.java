package org.example.model;

import java.sql.*;
import java.time.Instant;
import java.util.List;

public record SqLiteWeatherStore(String dbPath) implements WeatherStore {


    @Override
    public void save(Weather weather) {
        try (Connection ignored = connect(dbPath)) {
            updateOrInsert(weather);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void open(List<Location> locationList) {
        try (SqLiteWeatherStore weatherStore = new SqLiteWeatherStore(this.dbPath)) {
            Connection connection = connect(weatherStore.dbPath());
            Statement statement = connection.createStatement();

            for (Location location : locationList) {
                createTable(statement, location.island());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Weather get(Location location, Instant ts) {
        Weather weather = null;

        try (Connection connection = connect(dbPath)) {
            String query = "SELECT temp, pop, humidity, clouds, windSpeed FROM " +
                    location.island() + " WHERE ts = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, String.valueOf(ts));
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        double temp = resultSet.getDouble("temp");
                        double pop = resultSet.getDouble("pop");
                        int humidity = resultSet.getInt("humidity");
                        int clouds = resultSet.getInt("clouds");
                        double windSpeed = resultSet.getDouble("windSpeed");

                        weather = new Weather(ts, location, temp, pop, humidity, clouds, windSpeed);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return weather;
    }


    @Override
    public void close() {

    }


    public static void createTable(Statement statement, String tableName) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "ts TEXT," +
                "temp REAL," +
                "pop REAL," +
                "humidity INTEGER," +
                "clouds INTEGER," +
                "windSpeed REAL" +
                ");");
    }

    private static void insert(
            Connection connection, Weather weather) throws SQLException {
        String insertSQL =
                "INSERT INTO " + weather.location().island() +
                        " (ts, temp, pop, humidity, clouds, windSpeed) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
        preparedStatement.setString(1, String.valueOf(weather.ts()));
        preparedStatement.setDouble(2, weather.temp());
        preparedStatement.setDouble(3, weather.pop());
        preparedStatement.setInt(4, weather.humidity());
        preparedStatement.setInt(5, weather.clouds());
        preparedStatement.setDouble(6, weather.windSpeed());

        preparedStatement.executeUpdate();
    }


    private static void update(Connection connection, Weather weather) throws SQLException {
        String updateSQL = "UPDATE " + weather.location().island() +
                " SET temp = ?, pop = ?, humidity = ?, clouds = ?, windSpeed = ? WHERE ts = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            preparedStatement.setDouble(1, weather.temp());
            preparedStatement.setDouble(2, weather.pop());
            preparedStatement.setInt(3, weather.humidity());
            preparedStatement.setInt(4, weather.clouds());
            preparedStatement.setDouble(5, weather.windSpeed());
            preparedStatement.setString(6, String.valueOf(weather.ts()));
            preparedStatement.executeUpdate();
        }
    }


    public static Connection connect(String dbPath) {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + dbPath;
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }


    private void updateOrInsert(Weather weather) {
        try (Connection connection = connect(dbPath)) {
            String tableName = weather.location().island();
            Instant ts = weather.ts();

            if (isDateTimeInTable(connection, tableName, ts)) {
                update(connection, weather);
            } else {
                insert(connection, weather);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isDateTimeInTable(Connection connection, String tableName, Instant ts)
            throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE ts = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, String.valueOf(ts));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }
}
