package hernandez.guerra.exceptions;

public class PredictionProviderException extends Exception {
    public PredictionProviderException(String message) {
        super(message);
    }

    public PredictionProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
