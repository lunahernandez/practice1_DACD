package org.example.model;

import java.time.Instant;
import java.util.List;

public interface WeatherProvider {
    List<Weather> get(Location location, Instant ts);
}
