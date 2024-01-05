package hernandez.guerra.control;

import hernandez.guerra.exceptions.ExpressTravelBusinessUnitException;
import hernandez.guerra.model.AccommodationData;
import hernandez.guerra.model.WeatherData;

import java.util.*;

import static java.lang.Math.abs;

public class CommandLineInterface {
    private static ExpressTravelDatamart datamart;

    private static boolean temperaturePreference;
    private static int[] humidityLimits;

    private static boolean sunRainPreference;
    private static double[] windSpeedLimits;
    private static final double TEMPERATURE_LIMIT = 18.0;

    private static final int[] LOW_HUMIDITY_LIMIT = new int[]{0, 39};
    private static final int[] MODERATE_HUMIDITY_LIMIT = new int[]{40, 59};
    private static final int[] HIGH_HUMIDITY_LIMIT = new int[]{60, 100};

    private static final double[] LOW_WIND_SPEED_LIMIT = new double[]{0.0, 3.0};
    private static final double[] MODERATE_WIND_SPEED_LIMIT = new double[]{3.0, 10.0};
    private static final double[] HIGH_WIND_SPEED_LIMIT = new double[]{10.0, 100.0};
    private static final double TEMPERATURE_WEIGHT = 0.3;
    private static final double SUNNY_WEIGHT = 0.3;
    private static final double HUMIDITY_WEIGHT = 0.2;
    private static final double WIND_SPEED_WEIGHT = 0.2;

    private static double[] accommodationPriceLimits;
    private static final double[] ECONOMIC_ACCOMMODATION_PRICE_LIMIT = new double[]{0.0, 550.0};
    private static final double[] STANDARD_ACCOMMODATION_PRICE_LIMIT = new double[]{550.0, 750.0};
    private static final double[] EXPENSIVE_ACCOMMODATION_PRICE_LIMIT = new double[]{750.0, 10000.0};
    private static final double PRICE_WEIGHT = 0.5;
    private static final double RATING_WEIGHT = 0.5;


    public CommandLineInterface(ExpressTravelDatamart datamart) {
        CommandLineInterface.datamart = datamart;
        temperaturePreference = false;
        sunRainPreference = true;
        humidityLimits = LOW_HUMIDITY_LIMIT;
        windSpeedLimits = LOW_WIND_SPEED_LIMIT;
        accommodationPriceLimits = ECONOMIC_ACCOMMODATION_PRICE_LIMIT;
    }

    public static void run() throws ExpressTravelBusinessUnitException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. Surprise me!");
            System.out.println("2. Set my preferences");
            System.out.println("3. Recommendations");
            System.out.println("4. Exit");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    showTheBestOption();
                    break;
                case 2:
                    setPreferences(scanner);
                    showTheBestOption();
                    break;
                case 3:
                    showRecommendations();
                    break;
                case 4:
                    System.out.println("Leaving...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private static void showTheBestOption() throws ExpressTravelBusinessUnitException {
        Set<String> weatherLocations = datamart.getAllLocations("weatherPredictions");
        Set<String> accommodationLocations = datamart.getAllLocations("accommodations");
        Set<String> allLocations = new HashSet<>(weatherLocations);
        allLocations.addAll(accommodationLocations);

        List<WeatherData> weatherDataWithScores = setWeatherScores(allLocations);
        List<AccommodationData> accommodationDataWithScores = setAccommodationScores(allLocations);
        Map<Map.Entry<AccommodationData, WeatherData>, Double> sortedTravelDestinations =
                getSortedTravelDestinations(accommodationDataWithScores, weatherDataWithScores);
        assert sortedTravelDestinations != null;
        Map.Entry<AccommodationData, WeatherData> bestOption =
                sortedTravelDestinations.keySet().iterator().next();
        double bestOptionScore = sortedTravelDestinations.get(bestOption);
        System.out.println("Best Travel Destination:");
        System.out.println(bestOption + " - Score: " + bestOptionScore);

    }

    private static void showRecommendations() throws ExpressTravelBusinessUnitException {
        Set<String> weatherLocations = datamart.getAllLocations("weatherPredictions");
        Set<String> accommodationLocations = datamart.getAllLocations("accommodations");
        Set<String> allLocations = new HashSet<>(weatherLocations);
        allLocations.addAll(accommodationLocations);

        List<WeatherData> weatherDataWithScores = setWeatherScores(allLocations);
        List<AccommodationData> accommodationDataWithScores = setAccommodationScores(allLocations);

        if (accommodationDataWithScores != null) {
            Map<Map.Entry<AccommodationData, WeatherData>, Double> sortedTravelDestinations =
                    getSortedTravelDestinations(accommodationDataWithScores, weatherDataWithScores);

            int count = 0;
            for (Map.Entry<AccommodationData, WeatherData> entry : sortedTravelDestinations.keySet()) {
                if (count < 3) {
                    double score = sortedTravelDestinations.get(entry);
                    System.out.println("Travel Destination " + (count + 1) + ":");
                    System.out.println(entry + " - Score: " + score);
                    count++;
                } else {
                    break;
                }
            }
        }
    }
    private static LinkedHashMap<Map.Entry<AccommodationData, WeatherData>, Double> getSortedTravelDestinations(List<AccommodationData> accommodationDataWithScores, List<WeatherData> weatherDataWithScores) {
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




    private static void setPreferences(Scanner scanner) {
        setClimatePreferences(scanner);
        setAccommodationPreferences(scanner);
    }

    private static void setClimatePreferences(Scanner scanner) {
        System.out.println("Set your climate preferences:");

        System.out.println("Team Cold(1) vs. Team Warm(2)");
        temperaturePreference = setTemperaturePreference(scanner);

        System.out.println("Team Sun(1) vs. Team Rain(2)");
        sunRainPreference = setSunRainPreference(scanner);

        System.out.println("Humidity Preference:");
        humidityLimits = setHumidityPreference(scanner);

        System.out.println("Wind Speed Preference (m/s):");
        windSpeedLimits = setWindSpeedPreference(scanner);

        System.out.println("Climate preferences set successfully!");
    }


    private static boolean setTemperaturePreference(Scanner scanner) {
        int temperatureChoice = scanner.nextInt();
        return temperatureChoice == 1;
    }

    private static boolean setSunRainPreference(Scanner scanner) {
        int sunRainChoice = scanner.nextInt();
        return sunRainChoice == 1;
    }

    private static int[] setHumidityPreference(Scanner scanner) {
        System.out.println("1. Low");
        System.out.println("2. Moderate");
        System.out.println("3. High");
        System.out.println("4. Not Sure");

        int humidityChoice = scanner.nextInt();

        return switch (humidityChoice) {
            case 2 -> MODERATE_HUMIDITY_LIMIT;
            case 3 -> HIGH_HUMIDITY_LIMIT;
            default -> LOW_HUMIDITY_LIMIT;
        };
    }


    private static double[] setWindSpeedPreference(Scanner scanner) {
        System.out.println("1. Low");
        System.out.println("2. Moderate");
        System.out.println("3. High");
        System.out.println("4. Not Sure");

        int windSpeedChoice = scanner.nextInt();

        return switch (windSpeedChoice) {
            case 2 -> MODERATE_WIND_SPEED_LIMIT;
            case 3 -> HIGH_WIND_SPEED_LIMIT;
            default -> LOW_WIND_SPEED_LIMIT;
        };
    }

    private static void setAccommodationPreferences(Scanner scanner) {
        System.out.println("Set your accommodation preferences:");

        System.out.println("Price preferences:");
        accommodationPriceLimits = setAccommodationPricePreference(scanner);

        System.out.println("Accommodation preferences set successfully!");
    }


    private static double[] setAccommodationPricePreference(Scanner scanner) {
        System.out.println("1. Economic");
        System.out.println("2. Standard");
        System.out.println("3. Expensive");
        System.out.println("4. No preferences");


        int accommodationPriceChoice = scanner.nextInt();

        return switch (accommodationPriceChoice) {
            case 2 -> STANDARD_ACCOMMODATION_PRICE_LIMIT;
            case 3 -> EXPENSIVE_ACCOMMODATION_PRICE_LIMIT;
            default -> ECONOMIC_ACCOMMODATION_PRICE_LIMIT;
        };
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
}
