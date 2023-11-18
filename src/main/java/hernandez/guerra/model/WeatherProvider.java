package hernandez.guerra.model;

import java.util.List;

public interface WeatherProvider {
    List<Weather> get(Location location);
}
