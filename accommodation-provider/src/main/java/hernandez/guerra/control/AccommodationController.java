package hernandez.guerra.control;

import hernandez.guerra.exceptions.AccommodationProviderException;
import hernandez.guerra.model.Accommodation;
import hernandez.guerra.model.Location;

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AccommodationController {
    private final AccommodationProvider accommodationProvider;
    private final AccommodationStore accommodationStore;
    private final List<Location> locationList;

    public AccommodationController(AccommodationProvider accommodationProvider, AccommodationStore accommodationStore, List<Location> locationList) {
        this.accommodationProvider = accommodationProvider;
        this.accommodationStore = accommodationStore;
        this.locationList = locationList;
    }
    public void execute(){
        periodicAccommodationUpdateTask();
    }

    private void periodicAccommodationUpdateTask() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateAccommodationDataTask();
            }
        };
        timer.schedule(task, 0, 6 * 60 * 60 * 1000);
    }
    private void updateAccommodationDataTask() {
        try {
            updateWeatherData();
        } catch (AccommodationProviderException e) {
            throw new RuntimeException(e);
        }
        System.out.println("New query finished at " + Instant.now() + ".");
    }

    private void updateWeatherData() throws AccommodationProviderException {
        for (Location location : locationList) {
            for (Accommodation accommodation : accommodationProvider.get(location.locationArea())) {
                accommodationStore.save(accommodation);
            }
        }
    }
}
