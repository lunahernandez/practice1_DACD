package hernandez.guerra.model;

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
        weatherStore.open(this.locationList);
        Timer timer = new Timer();

        long currentTimeMillis = System.currentTimeMillis();
        long delayMillis = 6 * 60 * 60 * 1000 - (currentTimeMillis % (6 * 60 * 60 * 1000));

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                for (Location location : locationList) {
                    for (Weather weather : weatherProvider.get(location)) {
                        weatherStore.save(weather);
                    }
                }
                System.out.println("New query finished at " + Instant.now() + ".");

            }
        };

        timer.schedule(task, delayMillis, 6 * 60 * 60 * 1000);
    }

}
