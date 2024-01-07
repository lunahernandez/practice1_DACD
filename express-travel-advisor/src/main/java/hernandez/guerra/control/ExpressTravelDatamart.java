package hernandez.guerra.control;

import hernandez.guerra.exceptions.ExpressTravelBusinessUnitException;
import hernandez.guerra.model.AccommodationData;
import hernandez.guerra.model.WeatherData;
import jakarta.jms.TextMessage;

import java.util.List;
import java.util.Set;

public interface ExpressTravelDatamart {
    void update(TextMessage textMessage, String topicName) throws ExpressTravelBusinessUnitException;

    void initialize(String weatherTopicName, String accommodationTopicName, DatamartInitializer datamartInitializer)
            throws ExpressTravelBusinessUnitException;

    List<WeatherData> getWeather(String locationName) throws ExpressTravelBusinessUnitException;

    List<AccommodationData> getAccommodation(String locationName) throws ExpressTravelBusinessUnitException;

    Set<String> getAllLocations(String weatherPredictions) throws ExpressTravelBusinessUnitException;
}
