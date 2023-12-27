package hernandez.guerra.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hernandez.guerra.model.Accommodation;
import hernandez.guerra.model.LocationArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AirbnbProvider implements AccommodationProvider {
    private final String apiKey;

    public AirbnbProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<Accommodation> get(LocationArea locationArea) {
        Instant now = Instant.now();
        Instant checkInDate = calculateCheckInDate(now);
        Instant checkOutDate = checkInDate.plus(5, ChronoUnit.DAYS);

        JsonObject jsonObject = getJsonObjectFromAccommodationProvider(
                locationArea.neLat(), locationArea.neLng(),
                locationArea.swLat(), locationArea.swLng(),
                checkInDate, checkOutDate, apiKey
        );
        return convertJsonToAccommodationList(jsonObject);
    }

    private Instant calculateCheckInDate(Instant now) {
        LocalDate currentDate = Instant.ofEpochSecond(now.getEpochSecond()).atZone(ZoneOffset.UTC).toLocalDate();
        LocalTime currentTime = Instant.ofEpochSecond(now.getEpochSecond()).atZone(ZoneOffset.UTC).toLocalTime();

        LocalDate checkInDate = currentTime.isBefore(LocalTime.NOON) ? currentDate : currentDate.plusDays(1);

        return checkInDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private JsonObject getJsonObjectFromAccommodationProvider(
            String neLat, String neLng, String swLat, String swLng,
            Instant checkInDate, Instant checkOutDate, String apiKey
    ) {
        try {
            HttpURLConnection connection = openAccommodationProviderConnection(neLat, neLng, swLat, swLng,
                    checkInDate, checkOutDate, apiKey);
            return getJsonObjectFromConnection(connection);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpURLConnection openAccommodationProviderConnection(
            String neLat, String neLng, String swLat, String swLng,
            Instant checkInDate, Instant checkOutDate, String apiKey
    ) throws IOException {
        URL url = getUrl(neLat, neLng, swLat, swLng, checkInDate, checkOutDate);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty("X-Rapidapi-Key", apiKey);
        connection.setRequestProperty("X-Rapidapi-Host", "airbnb13.p.rapidapi.com");

        return connection;
    }

    private JsonObject getJsonObjectFromConnection(HttpURLConnection connection) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return null;
    }

    private URL getUrl(String neLat, String neLng, String swLat, String swLng, Instant checkInDate, Instant checkOutDate)
            throws MalformedURLException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedCheckInDate = dateFormat.format(Date.from(checkInDate));
        String formattedCheckOutDate = dateFormat.format(Date.from(checkOutDate));

        String apiUrl = "https://airbnb13.p.rapidapi.com/search-geo?ne_lat=" + neLat + "&ne_lng=" + neLng +
                "&sw_lat=" + swLat + "&sw_lng=" + swLng + "&checkin=" + formattedCheckInDate + "&checkout=" +
                formattedCheckOutDate + "&adults=2";
        return new URL(apiUrl);
    }

    private List<Accommodation> convertJsonToAccommodationList(JsonObject jsonObject) {
        List<Accommodation> accommodationList = new ArrayList<>();
        JsonArray accommodationListArray = jsonObject.getAsJsonArray("results");
        for (JsonElement element : accommodationListArray)
            accommodationList.add(createAccommodationFromJson(element.getAsJsonObject()));

        return accommodationList;
    }

    private Accommodation createAccommodationFromJson(JsonObject accommodationInfo) {
        String url = accommodationInfo.get("url").getAsString();
        String name = accommodationInfo.get("name").getAsString();
        String city = accommodationInfo.get("city").getAsString();
        String lat = accommodationInfo.get("lat").getAsString();
        String lng = accommodationInfo.get("lng").getAsString();
        int persons = accommodationInfo.get("persons").getAsInt();
        double rating = accommodationInfo.get("rating").getAsDouble();
        int totalPrice = accommodationInfo.getAsJsonObject("price").get("total").getAsInt();
        return new Accommodation(Instant.now(), "AccommodationProvider", url, name, city, lat, lng,
                persons, rating, totalPrice);
    }

}
