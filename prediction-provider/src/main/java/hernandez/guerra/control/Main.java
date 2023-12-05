package hernandez.guerra.control;

import hernandez.guerra.model.Location;

import java.util.ArrayList;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        String apikey = args[0];

        List<Location> locationList = loadLocations();

        WeatherProvider weatherProvider = new OpenWeatherMapProvider(apikey);
        WeatherStore weatherStore = new JMSWeatherStore("prediction.Weather");
        WeatherController weatherController = new WeatherController(weatherProvider, weatherStore, locationList);

        weatherController.execute();
    }

    public static List<Location> loadLocations() {
        List<Location> locationList = new ArrayList<>();
        locationList.add(new Location("28.01", "-15.53", "GC"));
        locationList.add(new Location("28.40", "-13.86", "FTV"));
        locationList.add(new Location("28.97", "-13.55", "LZ"));
        locationList.add(new Location("29.28", "-13.50", "LGR"));
        locationList.add(new Location("28.46", "-16.25", "TF"));
        locationList.add(new Location("28.75", "-17.89", "LP"));
        locationList.add(new Location("28.15", "-17.26", "GM"));
        locationList.add(new Location("27.80", "-17.89", "EH"));
        return locationList;
    }
}