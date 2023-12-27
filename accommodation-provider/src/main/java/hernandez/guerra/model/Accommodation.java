package hernandez.guerra.model;

import java.time.Instant;

public record Accommodation(Instant ts, String ss, String url, String name, String city, String lat, String lng,
                            int persons, double rating, int totalPrice) {}
