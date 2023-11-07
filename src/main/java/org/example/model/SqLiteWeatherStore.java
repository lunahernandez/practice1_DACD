package org.example.model;

import java.sql.*;
import java.time.Instant;
import java.util.List;

public class SqLiteWeatherStore implements WeatherStore {
    //TODO decide to use try-catch blocks or attribute Connection and close()
    //TODO delete displayWeatherData()
    //TODO get()
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
            updateOrInsert(weather);
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
    public void get(Location location, Instant ts) {

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
            String location = resultSet.getString("location");
            double temp = resultSet.getDouble("temp");
            double pop = resultSet.getDouble("pop");
            int humidity = resultSet.getInt("humidity");
            int clouds = resultSet.getInt("clouds");
            double windSpeed = resultSet.getDouble("windSpeed");

            System.out.println("ID: " + id);
            System.out.println("DateTime: " + dateTime);
            System.out.println("Location: " + location);
            System.out.println("Temperature: " + temp);
            System.out.println("Precipitation: " + pop);
            System.out.println("Humidity: " + humidity);
            System.out.println("Clouds: " + clouds);
            System.out.println("Wind speed: " + windSpeed);
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
                "location TEXT," +
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
                "INSERT INTO " + weather.getLocation().getIsland() +
                        " (dateTime, location, temp, pop, humidity, clouds, windSpeed) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
        preparedStatement.setString(1, weather.getDateTime());
        preparedStatement.setString(2,
                weather.getLocation().getLat() + ", " + weather.getLocation().getLon());
        preparedStatement.setDouble(3, weather.getTemp());
        preparedStatement.setDouble(4, weather.getPop());
        preparedStatement.setInt(5, weather.getHumidity());
        preparedStatement.setInt(6, weather.getClouds());
        preparedStatement.setDouble(7, weather.getWindSpeed());

        preparedStatement.executeUpdate();
    }


    private static void update(Connection connection, Weather weather) throws SQLException {
        String updateSQL = "UPDATE " + weather.getLocation().getIsland() +
                " SET temp = ?, pop = ?, humidity = ?, clouds = ?, windSpeed = ? WHERE dateTime = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            preparedStatement.setDouble(1, weather.getTemp());
            preparedStatement.setDouble(2, weather.getPop());
            preparedStatement.setInt(3, weather.getHumidity());
            preparedStatement.setInt(4, weather.getClouds());
            preparedStatement.setDouble(5, weather.getWindSpeed());
            preparedStatement.setString(6, weather.getDateTime());
            preparedStatement.executeUpdate();
        }
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


    public void updateOrInsert(Weather weather) {
        try (Connection connection = connect(dbPath)) {
            String tableName = weather.getLocation().getIsland();
            String dateTime = weather.getDateTime();

            if (isDateTimeInTable(connection, tableName, dateTime)) {
                update(connection, weather);
            } else {
                insert(connection, weather);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isDateTimeInTable(Connection connection, String tableName, String dateTime)
            throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE dateTime = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, dateTime);
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
