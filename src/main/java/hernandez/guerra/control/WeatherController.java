package hernandez.guerra.control;

import hernandez.guerra.model.Location;
import hernandez.guerra.model.Weather;

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WeatherController {
    private final WeatherProvider weatherProvider;
    private final WeatherStore weatherStore;
    private final List<Location> locationList;

    public WeatherController(WeatherProvider weatherProvider, WeatherStore weatherStore, List<Location> locationList) {
        this.weatherProvider = weatherProvider;
        this.weatherStore = weatherStore;
        this.locationList = locationList;
    }

    public void execute() {
        openWeatherStore();
        periodicWeatherUpdateTask();
    }

    private void openWeatherStore() {
        weatherStore.open(this.locationList);
    }

    private void periodicWeatherUpdateTask() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateWeatherData();
                System.out.println("New query finished at " + Instant.now() + ".");
            }
        };
        timer.schedule(task, 0, 6 * 60 * 60 * 1000);
    }

    private void updateWeatherData() {
        for (Location location : locationList) {
            for (Weather weather : weatherProvider.get(location)) {
                weatherStore.save(weather);
            }
        }
    }

}
