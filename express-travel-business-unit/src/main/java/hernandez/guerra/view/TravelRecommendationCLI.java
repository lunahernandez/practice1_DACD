package hernandez.guerra.view;

import hernandez.guerra.control.TravelRecommendationLogic;
import hernandez.guerra.exceptions.ExpressTravelBusinessUnitException;
import hernandez.guerra.model.AccommodationData;
import hernandez.guerra.model.WeatherData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class TravelRecommendationCLI {
    private static TravelRecommendationLogic recommendationLogic;

    private static final int[] LOW_HUMIDITY_LIMIT = new int[]{0, 39};
    private static final int[] MODERATE_HUMIDITY_LIMIT = new int[]{40, 59};
    private static final int[] HIGH_HUMIDITY_LIMIT = new int[]{60, 100};

    private static final double[] LOW_WIND_SPEED_LIMIT = new double[]{0.0, 3.0};
    private static final double[] MODERATE_WIND_SPEED_LIMIT = new double[]{3.0, 10.0};
    private static final double[] HIGH_WIND_SPEED_LIMIT = new double[]{10.0, 100.0};

    private static final double[] ECONOMIC_ACCOMMODATION_PRICE_LIMIT = new double[]{0.0, 550.0};
    private static final double[] STANDARD_ACCOMMODATION_PRICE_LIMIT = new double[]{550.0, 750.0};
    private static final double[] EXPENSIVE_ACCOMMODATION_PRICE_LIMIT = new double[]{750.0, 10000.0};


    private static boolean temperaturePreference = false;
    private static int[] humidityLimits = LOW_HUMIDITY_LIMIT;

    private static boolean sunRainPreference = true;
    private static double[] windSpeedLimits = LOW_WIND_SPEED_LIMIT;


    private static double[] accommodationPriceLimits = ECONOMIC_ACCOMMODATION_PRICE_LIMIT;

    public TravelRecommendationCLI(TravelRecommendationLogic travelRecommendationLogic) {
        recommendationLogic = travelRecommendationLogic;
    }

    public void run() throws ExpressTravelBusinessUnitException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            showWelcome();
            displayMenu();
            int choice = getUserChoice(scanner);

            switch (choice) {
                case 1:
                    setDefaultValues();
                    setPreferences();
                    Map<Map.Entry<AccommodationData, WeatherData>, Double> bestDefaultOption =
                            recommendationLogic.getTheBestOption();
                    showBestOption(bestDefaultOption);
                    break;
                case 2:
                    setUserValues(scanner);
                    setPreferences();
                    Map<Map.Entry<AccommodationData, WeatherData>, Double> bestUserOption =
                            recommendationLogic.getTheBestOption();
                    showBestOption(bestUserOption);
                    break;
                case 3:
                    setDefaultValues();
                    setPreferences();
                    Map<Map.Entry<AccommodationData, WeatherData>, Double> recommendations =
                            recommendationLogic.getRecommendations();
                    showRecommendations(recommendations);
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
    private static void showWelcome() {
        System.out.println("\nWelcome to ExpressTravel App! If you enjoy last-minute plans, you're in the right place.");
        System.out.println("Explore our options and discover your perfect destination for a 5-day getaway!");
    }

    private static void displayMenu() {
        System.out.println("1. Surprise me!");
        System.out.println("2. Set my preferences");
        System.out.println("3. Recommendations");
        System.out.println("4. Exit");
    }

    private static int getUserChoice(Scanner scanner) {
        try {
            return scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next();
            return getUserChoice(scanner);
        }
    }


    private static void setDefaultValues() {
        temperaturePreference = false;
        sunRainPreference = true;
        humidityLimits = LOW_HUMIDITY_LIMIT;
        windSpeedLimits = LOW_WIND_SPEED_LIMIT;
        accommodationPriceLimits = ECONOMIC_ACCOMMODATION_PRICE_LIMIT;
    }

    private static void setPreferences() {
        recommendationLogic.setPreferences(
                temperaturePreference,
                sunRainPreference,
                humidityLimits,
                windSpeedLimits,
                accommodationPriceLimits
        );
    }


    private static void setUserValues(Scanner scanner) {
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

    private static void showBestOption(Map<Map.Entry<AccommodationData, WeatherData>, Double> bestOptionMap) {
        Map.Entry<AccommodationData, WeatherData> bestOption = bestOptionMap.keySet().iterator().next();
        double bestOptionScore = bestOptionMap.get(bestOption);
        System.out.println("\nBest Travel Destination with a score of " + round(bestOptionScore, 4) +"/1:");
        showTravelDestination(bestOption);
        System.out.println("\nEnjoy your stay and thank you for choosing us!");

    }

    private static void showTravelDestination(Map.Entry<AccommodationData, WeatherData> bestOption) {
        AccommodationData accommodation = bestOption.getKey();
        WeatherData weather = bestOption.getValue();
        showAccommodationInfo(accommodation);
        showWeatherInfo(weather);
    }

    private static void showRecommendations(Map<Map.Entry<AccommodationData, WeatherData>, Double> bestOptionMap) {
        System.out.println("\nWe have chosen the three best rooms for you:");

        int count = 1;
        for (Map.Entry<AccommodationData, WeatherData> bestOption : bestOptionMap.keySet()) {
            double bestOptionScore = bestOptionMap.get(bestOption);

            System.out.println("\nTravel Destination " + count +
                    " with a score of " + round(bestOptionScore, 4) + "/1:");
            showTravelDestination(bestOption);

            count++;
        }
        System.out.println("\nChoose the one that best suits your preferences.");
        System.out.println("Enjoy your stay and thank you for choosing us!");
    }


    private static void showAccommodationInfo(AccommodationData accommodation) {
        System.out.println("Name: " + accommodation.name());
        System.out.println("The accommodation is located in " + accommodation.city() +
                ", in " + accommodation.locationName());
        System.out.println("Here you have the coordinates: " + accommodation.lat() + ", " + accommodation.lng());
        System.out.println(accommodation.reviewsCount() +
                " people have given their opinion about this accommodation, obtaining a rating of " +
                accommodation.rating());
        System.out.println("The price for the entire stay is " + accommodation.totalPrice() + "€");
        System.out.println("You can find more information about the accommodation here: " + accommodation.url());
    }

    private static void showWeatherInfo(WeatherData weather) {
        System.out.println("\nWe have averaged the weather forecasts for the next few days for you to enjoy at your leisure.");
        System.out.println("Here we leave some data that may be useful for you:");
        System.out.println("The average temperature will be " + round(weather.temp(), 2) + "ºC");
        System.out.println("On average, there will be a " + round(weather.pop(), 4) + "% chance of precipitation and " +
                weather.humidity() + "% humidity");
        System.out.println("You will find a " + weather.clouds() + "% of cloudiness and an average wind speed of "
                + round(weather.windSpeed(), 2) + "m/s");
    }

    private static double round(double value, int decimals) {
        if (decimals < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
