package hernandez.guerra.control;

import hernandez.guerra.exceptions.ExpressTravelBusinessUnitException;
import hernandez.guerra.model.AccommodationData;
import hernandez.guerra.model.WeatherData;

import java.util.*;

import static java.lang.Math.abs;


public class TravelRecommendationLogic {
    private static ExpressTravelDatamart datamart = null;
    private static boolean temperaturePreference;
    private static int[] humidityLimits;
    private static boolean sunRainPreference;
    private static double[] windSpeedLimits;
    private static double[] accommodationPriceLimits;

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

    public Map<Map.Entry<AccommodationData, WeatherData>, Double> getTheBestOption() throws ExpressTravelBusinessUnitException {
        LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> sortedTravelDestinations =
                getSortedTravelDestinations();
        assert sortedTravelDestinations != null;
        if (sortedTravelDestinations.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Map.Entry<AccommodationData, WeatherData>, Double> bestOptionMap = new HashMap<>();
        Map.Entry<AccommodationData, WeatherData> bestOption = sortedTravelDestinations.keySet().iterator().next();
        double bestOptionScore = sortedTravelDestinations.get(bestOption);
        bestOptionMap.put(bestOption, bestOptionScore);

        return bestOptionMap;
    }

    private static Set<String> getAllLocationsSets() throws ExpressTravelBusinessUnitException {
        Set<String> weatherLocations = datamart.getAllLocations("weatherPredictions");
        Set<String> accommodationLocations = datamart.getAllLocations("accommodations");
        Set<String> allLocations = new HashSet<>(weatherLocations);
        allLocations.addAll(accommodationLocations);
        return allLocations;
    }


    private static LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> getSortedTravelDestinations() throws ExpressTravelBusinessUnitException {
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

    private static LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> getSortedCombinedScores(Map<Map.Entry<AccommodationData, WeatherData>, Double> travelDestinations) {
        List<Map.Entry<Map.Entry<AccommodationData, WeatherData>, Double>> entryList = new ArrayList<>(travelDestinations.entrySet());

        entryList.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));

        LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> sortedTravelDestinations = new LinkedHashMap<>();
        for (Map.Entry<Map.Entry<AccommodationData, WeatherData>, Double> entry : entryList) {
            sortedTravelDestinations.put(entry.getKey(), entry.getValue());
        }
        return sortedTravelDestinations;
    }

    private static Map<Map.Entry<AccommodationData, WeatherData>, Double> getCombinedScores(
            List<WeatherData> weatherDataWithScores, List<AccommodationData> accommodationDataWithScores
    ){
        Map<AccommodationData, WeatherData> travelDestination = new HashMap<>();
        for (AccommodationData accommodation : accommodationDataWithScores){
            for (WeatherData weather : weatherDataWithScores) {
                if (accommodation.locationName().equals(weather.locationName())) {
                    travelDestination.put(accommodation, weather);
                }
            }
        }
        Map<Map.Entry<AccommodationData, WeatherData>, Double> travelDestinationList = new HashMap<>();
        for (Map.Entry<AccommodationData, WeatherData> entry : travelDestination.entrySet()) {
            AccommodationData accommodation = entry.getKey();
            WeatherData weather = entry.getValue();
            double combinedScore = accommodation.score() + weather.score();
            travelDestinationList.put(entry, combinedScore);
        }
        return travelDestinationList;
    }

    private static List<WeatherData> setWeatherScores(Set<String> allLocations) throws ExpressTravelBusinessUnitException {
        List<WeatherData> averageWeatherLocations = new ArrayList<>();
        for (String location : allLocations) {
            List<WeatherData> weatherData = datamart.getWeather(location);
            WeatherData averageWeatherData = getAverageWeatherData(weatherData);
            averageWeatherLocations.add(averageWeatherData);
        }
        return getWeatherLocationScores(averageWeatherLocations);
    }

    private static List<WeatherData> getWeatherLocationScores(List<WeatherData> averageWeatherLocations) {
        if (averageWeatherLocations == null || averageWeatherLocations.isEmpty()) {
            System.out.println("No weather data available.");
            return Collections.emptyList();
        }

        List<WeatherData> result = new ArrayList<>();

        for (WeatherData weatherData : averageWeatherLocations) {
            if (weatherData != null) {
                double score = calculateLocationScore(weatherData);
                weatherData.setScore(score);
                result.add(weatherData);
            }
        }

        return result;
    }


    private static double calculateLocationScore(WeatherData weatherData) {
        double tempScore = calculateTempScore(weatherData.temp());
        double sunnyScore = calculateSunnyScore(weatherData.pop(), weatherData.clouds());
        double humidityScore = calculateHumidityScore(weatherData.humidity());
        double windSpeedScore = calculateWindSpeedScore(weatherData.windSpeed());

        return (tempScore + sunnyScore + humidityScore + windSpeedScore) * 0.5;
    }

    private static double calculateTempScore(double temp) {
        double baseScore = temperaturePreference
                ? (temp <= TEMPERATURE_LIMIT ? 1.0 : 0.3)
                : (temp <= TEMPERATURE_LIMIT ? 0.3 : 1.0);

        return baseScore * TEMPERATURE_WEIGHT;
    }

    private static double calculateSunnyScore(double pop, int clouds) {
        double popScore;
        double cloudsScore;
        if (sunRainPreference) {
            popScore = (1.0 - calculatePopScore(pop));
            cloudsScore = calculateCloudsScore(clouds) * 0.4;
        } else {
            popScore = calculatePopScore(pop);
            cloudsScore = (1.0 - calculateCloudsScore(clouds) * 0.4);
        }
        return (cloudsScore + popScore) * SUNNY_WEIGHT;
    }

    private static double calculateCloudsScore(int clouds) {
        if (clouds <= 30) {
            return 1.0;
        } else if (clouds <= 60) {
            return 0.25;
        } else {
            return 0.05;
        }
    }

    private static double calculatePopScore(double pop) {
        if (pop <= 10) {
            return 0.05;
        } else if (pop <= 30) {
            return 0.10;
        } else if (pop <= 50) {
            return 0.25;
        } else {
            return 1.0;
        }
    }

    private static double calculateHumidityScore(int humidity) {
        int lowerLimit = humidityLimits[0];
        int upperLimit = humidityLimits[1];

        double baseScore = (humidity >= lowerLimit && humidity <= upperLimit)
                ? 1.0
                : (abs(humidity - lowerLimit) < 20 || abs(humidity - upperLimit) < 20)
                ? 0.35
                : (humidity <= 40) ? 0.10 : 0.0;

        return baseScore * HUMIDITY_WEIGHT;
    }

    private static double calculateWindSpeedScore(double windSpeed) {
        double lowerLimit = windSpeedLimits[0];
        double upperLimit = windSpeedLimits[1];

        double baseScore = (windSpeed >= lowerLimit && windSpeed <= upperLimit)
                ? 1.0
                : (abs(windSpeed - lowerLimit) < 2 || abs(windSpeed - upperLimit) < 2)
                ? 0.35
                : (windSpeed <= 4) ? 0.10 : 0.0;

        return baseScore * WIND_SPEED_WEIGHT;
    }

    private static List<AccommodationData> setAccommodationScores(Set<String> allLocations) throws ExpressTravelBusinessUnitException {
        List<AccommodationData> accommodationDataWithScores = new ArrayList<>();

        for (String location : allLocations) {
            List<AccommodationData> accommodationData = datamart.getAccommodation(location);

            if (!accommodationData.isEmpty()) {
                for (AccommodationData accommodation : accommodationData) {
                    double score = calculateAccommodationScore(accommodation);
                    accommodation.setScore(score);
                }
                accommodationDataWithScores.addAll(accommodationData);
            }
        }

        if (accommodationDataWithScores.isEmpty()) {
            System.out.println("No accommodation data available.");
            return null;
        }

        return accommodationDataWithScores;
    }


    private static double calculateAccommodationScore(AccommodationData accommodation) {
        double ratingScore = calculateRatingScore(accommodation.rating(), accommodation.reviewsCount());
        double priceScore = calculatePriceScore(accommodation.totalPrice());

        return (ratingScore + priceScore) * 0.5;
    }

    private static double calculateRatingScore(double rating, int reviewsCount) {
        if (reviewsCount < 3) return 0.0;

        double[] ratingRanges = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] ratingScores = {0.0, 0.1, 0.2, 0.3, 0.6, 1.0};

        double ratingScore = 0.0;
        for (int i = 0; i < ratingRanges.length - 1; i++) {
            if (rating >= ratingRanges[i] && rating < ratingRanges[i + 1]) {
                ratingScore = ratingScores[i];
                break;
            }
        }

        double reviewsCountScore = (reviewsCount >= 10) ? 1.0 : 0.4;

        return (ratingScore * 0.6 + reviewsCountScore * 0.4) * RATING_WEIGHT;
    }

    private static double calculatePriceScore(double price) {
        double lowerLimit = accommodationPriceLimits[0];
        double upperLimit = accommodationPriceLimits[1];

        double baseScore = (price >= lowerLimit && price <= upperLimit)
                ? 1.0
                : (abs(price - lowerLimit) < 300 || abs(price - upperLimit) < 500)
                ? 0.35
                : (price <= 600) ? 0.10 : 0.0;

        return baseScore * PRICE_WEIGHT;
    }


    private static WeatherData getAverageWeatherData(List<WeatherData> weatherDataList) {
        if (weatherDataList.isEmpty()) {
            System.out.println("No weather data available.");
            return null;
        }

        String locationName = weatherDataList.get(0).locationName();
        double averageTemp = weatherDataList.stream()
                .mapToDouble(WeatherData::temp)
                .average()
                .orElse(0.0);

        double averagePop = weatherDataList.stream()
                .mapToDouble(WeatherData::pop)
                .average()
                .orElse(0.0);

        int averageHumidity = (int) Math.round(weatherDataList.stream()
                .mapToDouble(WeatherData::humidity)
                .average()
                .orElse(0.0));

        int averageClouds = (int) Math.round(weatherDataList.stream()
                .mapToDouble(WeatherData::clouds)
                .average()
                .orElse(0.0));

        double averageWindSpeed = weatherDataList.stream()
                .mapToDouble(WeatherData::windSpeed)
                .average()
                .orElse(0.0);

        return new WeatherData(locationName, averageTemp, averagePop, averageHumidity, averageClouds, averageWindSpeed);
    }

    public Map<Map.Entry<AccommodationData, WeatherData>, Double> getRecommendations() throws ExpressTravelBusinessUnitException {
        LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> sortedTravelDestinations =
                getSortedTravelDestinations();
        assert sortedTravelDestinations != null;
        return createRecommendations(sortedTravelDestinations);
    }
    private static Map<Map.Entry<AccommodationData, WeatherData>, Double> createRecommendations(
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

    public void setPreferences(
            boolean temperaturePreference,
            boolean sunRainPreference,
            int[] humidityLimits,
            double[] windSpeedLimits,
            double[] accommodationPriceLimits
    ) {
        setTemperaturePreference(temperaturePreference);
        setSunRainPreference(sunRainPreference);
        setHumidityLimits(humidityLimits);
        setWindSpeedLimits(windSpeedLimits);
        setAccommodationPriceLimits(accommodationPriceLimits);

        System.out.println("Preferences set successfully!");
    }
}
