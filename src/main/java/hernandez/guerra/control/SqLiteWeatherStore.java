package hernandez.guerra.control;

import hernandez.guerra.model.Location;
import hernandez.guerra.model.Weather;

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

            createTablesForLocations(statement, locationList);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createTablesForLocations(Statement statement, List<Location> locationList)
            throws SQLException {
        for (Location location : locationList) {
            createTable(statement, location.island());
        }
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

    @Override
    public Weather get(Location location, Instant ts) {
        try (Connection connection = connect(dbPath)) {
            String query = "SELECT temp, pop, humidity, clouds, windSpeed FROM " +
                    location.island() + " WHERE ts = ?";
            return executeQuery(connection, query, ts, location);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Weather executeQuery(Connection connection, String query, Instant ts, Location location)
            throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, String.valueOf(ts));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? extractWeatherFromResultSet(ts, location, resultSet) : null;
            }
        }
    }

    private Weather extractWeatherFromResultSet(Instant ts, Location location, ResultSet resultSet)
            throws SQLException {
        double temp = resultSet.getDouble("temp");
        double pop = resultSet.getDouble("pop");
        int humidity = resultSet.getInt("humidity");
        int clouds = resultSet.getInt("clouds");
        double windSpeed = resultSet.getDouble("windSpeed");

        return new Weather(ts, location, temp, pop, humidity, clouds, windSpeed);
    }


    @Override
    public void close() {

    }

    private static void insert(Connection connection, Weather weather) throws SQLException {
        String tableName = weather.location().island();
        String insertSQL = String.format(
                "INSERT INTO %s (ts, temp, pop, humidity, clouds, windSpeed) VALUES (?, ?, ?, ?, ?, ?)", tableName);

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            setInsertParameters(preparedStatement, weather);
            preparedStatement.executeUpdate();
        }
    }

    private static void setInsertParameters(PreparedStatement preparedStatement, Weather weather) throws SQLException {
        preparedStatement.setString(1, String.valueOf(weather.ts()));
        preparedStatement.setDouble(2, weather.temp());
        preparedStatement.setDouble(3, weather.pop());
        preparedStatement.setInt(4, weather.humidity());
        preparedStatement.setInt(5, weather.clouds());
        preparedStatement.setDouble(6, weather.windSpeed());
    }

    private static void update(Connection connection, Weather weather) throws SQLException {
        String tableName = weather.location().island();
        String updateSQL = String.format(
                "UPDATE %s SET temp = ?, pop = ?, humidity = ?, clouds = ?, windSpeed = ? WHERE ts = ?", tableName);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            setUpdateParameters(preparedStatement, weather);
            preparedStatement.executeUpdate();
        }
    }

    private static void setUpdateParameters(PreparedStatement preparedStatement, Weather weather) throws SQLException {
        preparedStatement.setDouble(1, weather.temp());
        preparedStatement.setDouble(2, weather.pop());
        preparedStatement.setInt(3, weather.humidity());
        preparedStatement.setInt(4, weather.clouds());
        preparedStatement.setDouble(5, weather.windSpeed());
        preparedStatement.setString(6, String.valueOf(weather.ts()));
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
            executeUpdateOrInsert(weather, connection, tableName, ts);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void executeUpdateOrInsert(Weather weather, Connection connection, String tableName, Instant ts)
            throws SQLException {
        if (isDateTimeInTable(connection, tableName, ts)) {
            update(connection, weather);
        } else {
            insert(connection, weather);
        }
    }

    private static boolean isDateTimeInTable(Connection connection, String tableName, Instant ts)
            throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE ts = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, String.valueOf(ts));
            return countDateTimeInTable(preparedStatement);
        }
    }

    private static boolean countDateTimeInTable(PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) > 0;
        }
    }
}
