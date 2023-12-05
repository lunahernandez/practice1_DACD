package hernandez.guerra.exceptions;

public class EventStoreBuilderException extends Exception {
    public EventStoreBuilderException(String message) {
        super(message);
    }

    public EventStoreBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
