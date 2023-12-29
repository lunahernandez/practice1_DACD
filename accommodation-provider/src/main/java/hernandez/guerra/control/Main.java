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
    public static void main(String[] args) {
        String apiKey = args[0];
        String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;

        List<Location> locationList = readLocations();
        AccommodationProvider accommodationProvider = new AirbnbProvider(apiKey);
        AccommodationStore accommodationStore = new JMSAccommodationStore("prediction.Accommodation", brokerUrl);
        AccommodationController accommodationController = new AccommodationController(accommodationProvider, accommodationStore, locationList);
        accommodationController.execute();
    }
    private static List<Location> readLocations() {
        List<Location> locationList = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File("locations.tsv"))) {
            if (scanner.hasNextLine()) scanner.nextLine();

            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split("\t");
                locationList.add(new Location(data[0], data[1], data[2],
                        new LocationArea(data[3], data[4], data[5], data[6], data[7])));
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(new AccommodationProviderException(e.getMessage(), e));
        }
        return locationList;
    }
}