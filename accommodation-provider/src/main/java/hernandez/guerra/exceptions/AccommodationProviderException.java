package hernandez.guerra.exceptions;

public class AccommodationProviderException extends Exception{
    public AccommodationProviderException(String message) {
        super(message);
    }

    public AccommodationProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
