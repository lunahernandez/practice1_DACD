package hernandez.guerra.control;

import java.sql.*;

public record ExpressTravelSQLiteDatamart(String dbPath) {
    //TODO initialize from datalake

    public ExpressTravelSQLiteDatamart {
        createTables();
    }

    private void initialize() {
        try (Connection connection = connect()) {
            Statement statement = connection.createStatement();

            String createTableSQL = "CREATE TABLE IF NOT EXISTS events ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "topic TEXT NOT NULL,"
                    + "data TEXT NOT NULL,"
                    + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            statement.execute(createTableSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Connection connection = connect()) {
            Statement statement = connection.createStatement();

            createWeatherTable(statement);
            createAccommodationTable(statement);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createWeatherTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS weatherPredictions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "predictionTime TEXT," +
                "islandName TEXT," +
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
                "islandName TEXT," +
                "url TEXT," +
                "name TEXT," +
                "city TEXT," +
                "lat TEXT," +
                "lng TEXT," +
                "persons INTEGER," +
                "rating REAL," +
                "totalPrice INTEGER" +
                ");");
    }

    public void insertWeatherPrediction(String locationName, String predictionTime, double temp,
                                        double pop, int humidity, int clouds, double windSpeed) {
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
            e.printStackTrace();
        }
    }


    public void insertAccommodation(String locationName, String url, String name, String city,
                                    String lat, String lng, int persons, double rating, int totalPrice) {
        try (Connection connection = connect()) {
            String insertSQL = "INSERT INTO accommodations (locationName, url, name, city, lat, lng, persons, rating, totalPrice) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                preparedStatement.setString(1, locationName);
                preparedStatement.setString(2, url);
                preparedStatement.setString(3, name);
                preparedStatement.setString(4, city);
                preparedStatement.setString(5, lat);
                preparedStatement.setString(6, lng);
                preparedStatement.setInt(7, persons);
                preparedStatement.setDouble(8, rating);
                preparedStatement.setInt(9, totalPrice);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + dbPath;
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}

