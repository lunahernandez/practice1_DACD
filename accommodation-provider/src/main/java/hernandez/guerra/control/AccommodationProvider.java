package hernandez.guerra.control;

import hernandez.guerra.exceptions.AccommodationProviderException;
import hernandez.guerra.model.Accommodation;
import hernandez.guerra.model.Location;

import java.util.List;

public interface AccommodationProvider {
    List<Accommodation> get(Location location) throws AccommodationProviderException;

}
