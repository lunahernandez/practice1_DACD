package hernandez.guerra.control;

import hernandez.guerra.model.Location;
import hernandez.guerra.model.Weather;

import java.time.Instant;
import java.util.List;

public interface WeatherStore extends AutoCloseable {

    void save(Weather weather);

    void open(List<Location> locationList);

    Weather get(Location location, Instant ts);
}
