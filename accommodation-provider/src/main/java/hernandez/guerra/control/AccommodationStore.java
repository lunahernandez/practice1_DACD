package hernandez.guerra.control;

import hernandez.guerra.exceptions.AccommodationProviderException;
import hernandez.guerra.model.Accommodation;

public interface AccommodationStore {
    void save(Accommodation accommodation) throws AccommodationProviderException;
}
