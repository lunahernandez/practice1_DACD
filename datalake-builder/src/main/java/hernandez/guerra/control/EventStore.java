package hernandez.guerra.control;

import hernandez.guerra.exceptions.EventStoreBuilderException;
import jakarta.jms.TextMessage;

public interface EventStore {
    void saveEvent(TextMessage textMessage, String topicName) throws EventStoreBuilderException;
}
