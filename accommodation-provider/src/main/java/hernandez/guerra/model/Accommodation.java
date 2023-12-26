package hernandez.guerra.model;

import java.time.Instant;


//TODO add other attributes

public record Accommodation(Instant ts, String ss, String name, double rating, int price, int persons, String type, String url) {}
