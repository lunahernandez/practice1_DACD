package hernandez.guerra.model;

import java.time.Instant;
import java.util.List;

public interface WeatherStore extends AutoCloseable {

    void save(Weather weather);

    void open(List<Location> locationList);

    Weather get(Location location, Instant ts);
}
