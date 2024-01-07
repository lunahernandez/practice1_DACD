package hernandez.guerra.control;

import hernandez.guerra.exceptions.AccommodationProviderException;
import hernandez.guerra.model.Location;
import hernandez.guerra.model.LocationArea;
import org.apache.activemq.ActiveMQConnection;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws AccommodationProviderException {
        String apiKey = args[0];
        String locationsTsvPath = args[1];
        String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;

        List<Location> locationList = readLocations(locationsTsvPath);
        AccommodationProvider accommodationProvider = new AirbnbProvider(apiKey);
        AccommodationStore accommodationStore =
                new JMSAccommodationStore("prediction.Accommodation", brokerUrl);
        AccommodationController accommodationController =
                new AccommodationController(accommodationProvider, accommodationStore, locationList);
        accommodationController.execute();
    }

    private static List<Location> readLocations(String locationsTsvPath) throws AccommodationProviderException {
        List<Location> locationList = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(locationsTsvPath))) {
            if (scanner.hasNextLine()) scanner.nextLine();
            addLocationsToList(scanner, locationList);

        } catch (FileNotFoundException e) {
            throw new AccommodationProviderException(e.getMessage(), e);
        }
        return locationList;
    }

    private static void addLocationsToList(Scanner scanner, List<Location> locationList) {
        while (scanner.hasNextLine()) {
            String[] data = scanner.nextLine().split("\t");
            locationList.add(new Location(data[0], data[1], data[2],
                    new LocationArea(data[3], data[4], data[5], data[6], data[7])));
        }
    }
}