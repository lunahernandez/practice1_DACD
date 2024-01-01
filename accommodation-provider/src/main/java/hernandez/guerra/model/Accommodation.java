package hernandez.guerra.model;

import java.time.Instant;

public record Accommodation(Instant ts, String ss, String url, String name, Location location, String city, String lat,
                            String lng, int reviewsCount, double rating, int totalPrice) {}
