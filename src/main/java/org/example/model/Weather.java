package org.example.model;


import java.time.Instant;

public record Weather(Instant ts, Location location, double temp, double pop, int humidity, int clouds,
                      double windSpeed) {}