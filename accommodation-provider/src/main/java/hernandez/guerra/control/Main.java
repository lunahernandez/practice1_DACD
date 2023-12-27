package hernandez.guerra.control;

import hernandez.guerra.model.Accommodation;
import hernandez.guerra.model.LocationArea;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String apiKey = args[0];
        LocationArea granCanaria = new LocationArea("28.01", "-15.53", "27.99", "-15.58", "GG");
        LocationArea fuerteventura = new LocationArea("28.40", "-13.85", "28.37", "-13.89", "FTV");
        LocationArea lanzarote = new LocationArea("29.12", "-13.54", "29.11", "-13.57", "LZT");
        LocationArea laGraciosa = new LocationArea("29.24", "-13.50", "29.23", "-13.51", "LGR");
        LocationArea tenerife = new LocationArea("28.04", "-16.59", "28.02", "-16.62", "TF");
        LocationArea laPalma = new LocationArea("28.76", "-17.74", "28.73", "-17.76", "LP");
        LocationArea laGomera = new LocationArea("28.17", "-17.31", "28.16", "-17.34", "LG");
        LocationArea elHierro = new LocationArea("27.72", "-17.97", "27.70", "-18.00", "EH");

        AccommodationProvider accommodationProvider = new AirbnbProvider(apiKey);
        List<Accommodation> accommodations = accommodationProvider.get(elHierro);
        System.out.println(accommodations);
    }
}