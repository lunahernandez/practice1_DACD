package hernandez.guerra.exceptions;

import jakarta.jms.JMSException;

import java.io.IOException;

public class EventStoreBuilderException extends Exception {
    public EventStoreBuilderException(String message) {
        super(message);
    }

    public EventStoreBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
