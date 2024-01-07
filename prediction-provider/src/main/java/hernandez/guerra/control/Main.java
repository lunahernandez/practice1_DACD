package hernandez.guerra.control;

import hernandez.guerra.exceptions.PredictionProviderException;
import hernandez.guerra.model.Location;
import org.apache.activemq.ActiveMQConnection;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) throws PredictionProviderException {
        String apikey = args[0];
        String locationsTsvPath = args[1];
        String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
        List<Location> locationList = readLocations(locationsTsvPath);

        WeatherProvider weatherProvider = new OpenWeatherMapProvider(apikey);
        WeatherStore weatherStore = new JMSWeatherStore("prediction.Weather", brokerUrl);
        WeatherController weatherController = new WeatherController(weatherProvider, weatherStore, locationList);

        weatherController.execute();
    }

    private static List<Location> readLocations(String locationsTsvPath) throws PredictionProviderException {
        List<Location> locationList = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(locationsTsvPath))) {
            if (scanner.hasNextLine()) scanner.nextLine();
            addLocationsToList(scanner, locationList);

        } catch (FileNotFoundException e) {
            throw new PredictionProviderException(e.getMessage(), e);
        }
        return locationList;
    }

    private static void addLocationsToList(Scanner scanner, List<Location> locationList) {
        while (scanner.hasNextLine()) {
            String[] data = scanner.nextLine().split("\t");
            locationList.add(new Location(data[0], data[1], data[2]));
        }
    }
}