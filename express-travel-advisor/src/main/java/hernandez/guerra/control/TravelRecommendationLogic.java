package hernandez.guerra.control;

import hernandez.guerra.exceptions.ExpressTravelBusinessUnitException;
import hernandez.guerra.model.AccommodationData;
import hernandez.guerra.model.WeatherData;

import java.util.*;

import static java.lang.Math.abs;


public class TravelRecommendationLogic {
    private final ExpressTravelDatamart datamart;
    private boolean temperaturePreference;
    private int[] humidityLimits;
    private boolean sunRainPreference;
    private double[] windSpeedLimits;
    private double[] accommodationPriceLimits;

    private static final double TEMPERATURE_LIMIT = 18.0;

    private static final double TEMPERATURE_WEIGHT = 0.3;
    private static final double SUNNY_WEIGHT = 0.3;
    private static final double HUMIDITY_WEIGHT = 0.2;
    private static final double WIND_SPEED_WEIGHT = 0.2;
    private static final double PRICE_WEIGHT = 0.5;
    private static final double RATING_WEIGHT = 0.5;

    public TravelRecommendationLogic(ExpressTravelDatamart datamart) {
        this.datamart = datamart;
    }

    public void setTemperaturePreference(boolean temperaturePreference) {
        this.temperaturePreference = temperaturePreference;
    }

    public void setHumidityLimits(int[] humidityLimits) {
        this.humidityLimits = humidityLimits;
    }

    public void setSunRainPreference(boolean sunRainPreference) {
        this.sunRainPreference = sunRainPreference;
    }

    public void setWindSpeedLimits(double[] windSpeedLimits) {
        this.windSpeedLimits = windSpeedLimits;
    }

    public void setAccommodationPriceLimits(double[] accommodationPriceLimits) {
        this.accommodationPriceLimits = accommodationPriceLimits;
    }

    public Map<Map.Entry<AccommodationData, WeatherData>, Double> getTheBestOption()
            throws ExpressTravelBusinessUnitException {
        LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> sortedTravelDestinations =
                getSortedTravelDestinationsWithScores();

        if (sortedTravelDestinations == null || sortedTravelDestinations.isEmpty()) {
            return Collections.emptyMap();
        }

        return extractBestOption(sortedTravelDestinations);
    }

    private Map<Map.Entry<AccommodationData, WeatherData>, Double> extractBestOption(
            LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> sortedTravelDestinations) {
        Map.Entry<AccommodationData, WeatherData> bestOptionEntry = getFirstEntry(sortedTravelDestinations);

        if (bestOptionEntry == null) {
            return Collections.emptyMap();
        }

        return Collections.singletonMap(bestOptionEntry, sortedTravelDestinations.get(bestOptionEntry));
    }

    private Map.Entry<AccommodationData, WeatherData> getFirstEntry(
            LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> sortedTravelDestinations) {
        Iterator<Map.Entry<Map.Entry<AccommodationData, WeatherData>, Double>> iterator =
                sortedTravelDestinations.entrySet().iterator();

        return iterator.hasNext() ? iterator.next().getKey() : null;
    }


    private LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> getSortedTravelDestinationsWithScores()
            throws ExpressTravelBusinessUnitException {
        Set<String> allLocations = getAllLocationsSets();

        List<WeatherData> weatherDataWithScores = setWeatherScores(allLocations);
        List<AccommodationData> accommodationDataWithScores = setAccommodationScores(allLocations);
        if (accommodationDataWithScores != null) {
            Map<Map.Entry<AccommodationData, WeatherData>, Double> travelDestinations =
                    getCombinedScores(weatherDataWithScores, accommodationDataWithScores);

            return getSortedCombinedScores(travelDestinations);
        }
        return null;
    }

    private Set<String> getAllLocationsSets() throws ExpressTravelBusinessUnitException {
        Set<String> weatherLocations = datamart.getAllLocations("weatherPredictions");
        Set<String> accommodationLocations = datamart.getAllLocations("accommodations");
        Set<String> allLocations = new HashSet<>(weatherLocations);
        allLocations.addAll(accommodationLocations);
        return allLocations;
    }

    private static LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> getSortedCombinedScores(
            Map<Map.Entry<AccommodationData, WeatherData>, Double> travelDestinations) {
        List<Map.Entry<Map.Entry<AccommodationData, WeatherData>, Double>> entryList =
                new ArrayList<>(travelDestinations.entrySet());

        entryList.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));

        LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> sortedTravelDestinations = new LinkedHashMap<>();
        for (Map.Entry<Map.Entry<AccommodationData, WeatherData>, Double> entry : entryList) {
            sortedTravelDestinations.put(entry.getKey(), entry.getValue());
        }
        return sortedTravelDestinations;
    }

    private Map<Map.Entry<AccommodationData, WeatherData>, Double> getCombinedScores(
            List<WeatherData> weatherDataWithScores, List<AccommodationData> accommodationDataWithScores) {
        Map<AccommodationData, WeatherData> travelDestination =
                createTravelDestinationMap(accommodationDataWithScores, weatherDataWithScores);
        return calculateCombinedScores(travelDestination);
    }

    private Map<AccommodationData, WeatherData> createTravelDestinationMap(
            List<AccommodationData> accommodationDataWithScores, List<WeatherData> weatherDataWithScores) {
        Map<AccommodationData, WeatherData> travelDestination = new HashMap<>();
        for (AccommodationData accommodation : accommodationDataWithScores) {
            for (WeatherData weather : weatherDataWithScores) {
                if (accommodation.locationName().equals(weather.locationName())) {
                    travelDestination.put(accommodation, weather);
                }
            }
        }
        return travelDestination;
    }

    private Map<Map.Entry<AccommodationData, WeatherData>, Double> calculateCombinedScores(
            Map<AccommodationData, WeatherData> travelDestination
    ) {
        Map<Map.Entry<AccommodationData, WeatherData>, Double> travelDestinationList = new HashMap<>();
        for (Map.Entry<AccommodationData, WeatherData> entry : travelDestination.entrySet()) {
            AccommodationData accommodation = entry.getKey();
            WeatherData weather = entry.getValue();
            double combinedScore = calculateCombinedScore(accommodation, weather);
            travelDestinationList.put(entry, combinedScore);
        }
        return travelDestinationList;
    }

    private double calculateCombinedScore(AccommodationData accommodation, WeatherData weather) {
        return accommodation.score() + weather.score();
    }

    private List<WeatherData> setWeatherScores(Set<String> allLocations) throws ExpressTravelBusinessUnitException {
        List<WeatherData> averageWeatherLocations = new ArrayList<>();
        for (String location : allLocations) {
            List<WeatherData> weatherData = datamart.getWeather(location);
            WeatherData averageWeatherData = getAverageWeatherData(weatherData);
            averageWeatherLocations.add(averageWeatherData);
        }
        return getWeatherLocationScores(averageWeatherLocations);
    }

    private List<WeatherData> getWeatherLocationScores(List<WeatherData> averageWeatherLocations) {
        if (checkIfWeatherDataIsUnavailable(averageWeatherLocations)) {
            return Collections.emptyList();
        }

        List<WeatherData> result = new ArrayList<>();
        calculateAndSetLocationScores(averageWeatherLocations, result);

        return result;
    }

    private boolean checkIfWeatherDataIsUnavailable(List<WeatherData> averageWeatherLocations) {
        return averageWeatherLocations == null || averageWeatherLocations.isEmpty();
    }

    private void calculateAndSetLocationScores(List<WeatherData> weatherDataList, List<WeatherData> result) {
        for (WeatherData weatherData : weatherDataList) {
            if (weatherData != null) {
                double score = calculateLocationScore(weatherData);
                weatherData.setScore(score);
                result.add(weatherData);
            }
        }
    }

    private double calculateLocationScore(WeatherData weatherData) {
        double tempScore = calculateTempScore(weatherData.temp());
        double sunnyScore = calculateSunnyScore(weatherData.pop(), weatherData.clouds());
        double humidityScore = calculateHumidityScore(weatherData.humidity());
        double windSpeedScore = calculateWindSpeedScore(weatherData.windSpeed());

        return (tempScore + sunnyScore + humidityScore + windSpeedScore) * 0.5;
    }

    private double calculateTempScore(double temp) {
        double baseScore = (temperaturePreference
                ? (temp <= TEMPERATURE_LIMIT ? 1.0 : 0.3)
                : (temp <= TEMPERATURE_LIMIT ? 0.3 : 1.0));
        double temperatureRank = (temp < 0) ? 1.0 : 1.0 / (temp + 1);
        return baseScore * TEMPERATURE_WEIGHT * temperatureRank;
    }

    private double calculateSunnyScore(double pop, int clouds) {
        double popScore = calculatePopScore(pop);
        double cloudsScore = calculateCloudsScore(clouds);

        double baseScore = getBaseScore(popScore, cloudsScore);
        return baseScore * SUNNY_WEIGHT;
    }

    private double getBaseScore(double popScore, double cloudsScore) {
        if (sunRainPreference) {
            return (1.0 - popScore) * 0.6 + (1.0 - cloudsScore) * 0.4;
        } else {
            return popScore * 0.6 + cloudsScore * 0.4;
        }
    }

    private double calculateCloudsScore(int clouds) {
        return 1.0 - clouds / 100.0;
    }

    private double calculatePopScore(double pop) {
        return 1.0 - pop / 100.0;
    }

    private double calculateHumidityScore(int humidity) {
        int lowerLimit = humidityLimits[0];
        int upperLimit = humidityLimits[1];

        double baseScore = (humidity >= lowerLimit && humidity <= upperLimit)
                ? 1.0
                : (abs(humidity - lowerLimit) < 20 || abs(humidity - upperLimit) < 20)
                ? 0.35
                : (humidity <= 40) ? 0.10 : 0.0;

        return baseScore * HUMIDITY_WEIGHT;
    }

    private double calculateWindSpeedScore(double windSpeed) {
        double lowerLimit = windSpeedLimits[0];
        double upperLimit = windSpeedLimits[1];

        double baseScore = (windSpeed >= lowerLimit && windSpeed <= upperLimit)
                ? 1.0
                : (abs(windSpeed - lowerLimit) < 2 || abs(windSpeed - upperLimit) < 2)
                ? 0.35
                : (windSpeed <= 4) ? 0.10 : 0.0;

        return baseScore * WIND_SPEED_WEIGHT;
    }

    private List<AccommodationData> setAccommodationScores(Set<String> allLocations)
            throws ExpressTravelBusinessUnitException {
        List<AccommodationData> accommodationDataWithScores = new ArrayList<>();

        for (String location : allLocations) {
            List<AccommodationData> accommodationData = datamart.getAccommodation(location);
            addAccommodationWithScoreToList(accommodationData, accommodationDataWithScores);
        }

        return (accommodationDataWithScores.isEmpty()) ? null : accommodationDataWithScores;
    }

    private void addAccommodationWithScoreToList(
            List<AccommodationData> accommodationData, List<AccommodationData> accommodationDataWithScores) {
        if (!accommodationData.isEmpty()) {
            setScores(accommodationData);
            accommodationDataWithScores.addAll(accommodationData);
        }
    }

    private void setScores(List<AccommodationData> accommodationData) {
        for (AccommodationData accommodation : accommodationData) {
            double score = calculateAccommodationScore(accommodation);
            accommodation.setScore(score);
        }
    }


    private double calculateAccommodationScore(AccommodationData accommodation) {
        double ratingScore = calculateRatingScore(accommodation.rating(), accommodation.reviewsCount());
        double priceScore = calculatePriceScore(accommodation.totalPrice());

        return (ratingScore + priceScore) * 0.5;
    }

    private double calculateRatingScore(double rating, int reviewsCount) {
        if (reviewsCount < 3) {
            return 0.0;
        }

        double ratingScore = getRatingScore(rating);
        double reviewsCountScore = getReviewsCountScore(reviewsCount);

        return calculateCombinedScore(ratingScore, reviewsCountScore);
    }

    private double getRatingScore(double rating) {
        double[] ratingRanges = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] ratingScores = {0.0, 0.1, 0.2, 0.3, 0.6, 1.0};

        for (int i = 0; i < ratingRanges.length - 1; i++) {
            if (rating >= ratingRanges[i] && rating < ratingRanges[i + 1]) {
                return ratingScores[i];
            }
        }
        return 0.0;
    }

    private double getReviewsCountScore(int reviewsCount) {
        return (reviewsCount >= 10) ? 1.0 : 0.4;
    }

    private double calculateCombinedScore(double ratingScore, double reviewsCountScore) {
        return (ratingScore * 0.6 + reviewsCountScore * 0.4) * RATING_WEIGHT;
    }

    private double calculatePriceScore(double price) {
        double lowerLimit = accommodationPriceLimits[0];
        double upperLimit = accommodationPriceLimits[1];

        double baseScore = (price >= lowerLimit && price <= upperLimit)
                ? 1.0
                : (abs(price - lowerLimit) < 300 || abs(price - upperLimit) < 500)
                ? 0.35
                : (price <= 600) ? 0.10 : 0.0;

        return baseScore * PRICE_WEIGHT;
    }


    private WeatherData getAverageWeatherData(List<WeatherData> weatherDataList) {
        if (checkIfIsEmpty(weatherDataList)) return null;

        String locationName = weatherDataList.get(0).locationName();
        double averageTemp = getAverageTemp(weatherDataList);
        double averagePop = getAveragePop(weatherDataList);
        int averageHumidity = getAverageHumidity(weatherDataList);
        int averageClouds = getAverageClouds(weatherDataList);
        double averageWindSpeed = getAverageWindSpeed(weatherDataList);

        return new WeatherData(locationName, averageTemp, averagePop, averageHumidity, averageClouds, averageWindSpeed);
    }

    private static boolean checkIfIsEmpty(List<WeatherData> weatherDataList) {
        if (weatherDataList.isEmpty()) {
            System.out.println("No weather data available.");
            return true;
        }
        return false;
    }

    private static double getAverageWindSpeed(List<WeatherData> weatherDataList) {
        return weatherDataList.stream()
                .mapToDouble(WeatherData::windSpeed)
                .average()
                .orElse(0.0);
    }

    private static int getAverageClouds(List<WeatherData> weatherDataList) {
        return (int) Math.round(weatherDataList.stream()
                .mapToDouble(WeatherData::clouds)
                .average()
                .orElse(0.0));
    }

    private static int getAverageHumidity(List<WeatherData> weatherDataList) {
        return (int) Math.round(weatherDataList.stream()
                .mapToDouble(WeatherData::humidity)
                .average()
                .orElse(0.0));
    }

    private static double getAveragePop(List<WeatherData> weatherDataList) {
        return weatherDataList.stream()
                .mapToDouble(WeatherData::pop)
                .average()
                .orElse(0.0);
    }

    private static double getAverageTemp(List<WeatherData> weatherDataList) {
        return weatherDataList.stream()
                .mapToDouble(WeatherData::temp)
                .average()
                .orElse(0.0);
    }

    public Map<Map.Entry<AccommodationData, WeatherData>, Double> getRecommendations() {
        try {
            LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> sortedTravelDestinations =
                    getSortedTravelDestinationsWithScores();

            if (sortedTravelDestinations != null) {
                return createRecommendations(sortedTravelDestinations);
            }
        } catch (ExpressTravelBusinessUnitException e) {
            throw new RuntimeException("Error while getting recommendations", e);
        }

        return null;
    }

    private Map<Map.Entry<AccommodationData, WeatherData>, Double> createRecommendations(
            Map<Map.Entry<AccommodationData, WeatherData>, Double> sortedTravelDestinations
    ) {
        Map<Map.Entry<AccommodationData, WeatherData>, Double> recommendationsMap = new LinkedHashMap<>();
        int count = 0;

        for (Map.Entry<AccommodationData, WeatherData> entry : sortedTravelDestinations.keySet()) {
            if (count < 3) {
                double score = sortedTravelDestinations.get(entry);
                recommendationsMap.put(entry, score);
                count++;
            } else {
                break;
            }
        }

        return recommendationsMap;
    }

    public void setPreferences(boolean temperaturePreference, boolean sunRainPreference, int[] humidityLimits,
                               double[] windSpeedLimits, double[] accommodationPriceLimits) {
        setTemperaturePreference(temperaturePreference);
        setSunRainPreference(sunRainPreference);
        setHumidityLimits(humidityLimits);
        setWindSpeedLimits(windSpeedLimits);
        setAccommodationPriceLimits(accommodationPriceLimits);

        System.out.println("Preferences set successfully!");
    }
}
