package hernandez.guerra.model;

import java.time.Instant;

public record Weather(Instant ts, String ss, Instant predictionTime, Location location,
                      double temp, double pop, int humidity, int clouds, double windSpeed) {}