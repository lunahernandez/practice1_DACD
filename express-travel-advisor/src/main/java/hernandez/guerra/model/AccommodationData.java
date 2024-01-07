package hernandez.guerra.model;

import java.util.Objects;

public final class AccommodationData {
    private final String locationName;
    private final String url;
    private final String name;
    private final String city;
    private final String lat;
    private final String lng;
    private final int reviewsCount;
    private final double rating;
    private final int totalPrice;
    private double score;

    public AccommodationData(String locationName, String url, String name, String city, String lat,
                             String lng, int reviewsCount, double rating, int totalPrice) {
        this.locationName = locationName;
        this.url = url;
        this.name = name;
        this.city = city;
        this.lat = lat;
        this.lng = lng;
        this.reviewsCount = reviewsCount;
        this.rating = rating;
        this.totalPrice = totalPrice;
    }

    public String locationName() {
        return locationName;
    }

    public String url() {
        return url;
    }

    public String name() {
        return name;
    }

    public String city() {
        return city;
    }

    public String lat() {
        return lat;
    }

    public String lng() {
        return lng;
    }

    public int reviewsCount() {
        return reviewsCount;
    }

    public double rating() {
        return rating;
    }

    public int totalPrice() {
        return totalPrice;
    }

    public double score() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccommodationData that = (AccommodationData) o;
        return reviewsCount == that.reviewsCount && Double.compare(rating, that.rating) == 0 &&
                totalPrice == that.totalPrice && Double.compare(score, that.score) == 0 &&
                Objects.equals(locationName, that.locationName) &&
                Objects.equals(url, that.url) &&
                Objects.equals(name, that.name) &&
                Objects.equals(city, that.city) &&
                Objects.equals(lat, that.lat) &&
                Objects.equals(lng, that.lng);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationName, url, name, city, lat, lng, reviewsCount, rating, totalPrice, score);
    }

    @Override
    public String toString() {
        return "AccommodationData{" +
                "locationName='" + locationName + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", reviewsCount=" + reviewsCount +
                ", rating=" + rating +
                ", totalPrice=" + totalPrice +
                ", score=" + score +
                '}';
    }
}
