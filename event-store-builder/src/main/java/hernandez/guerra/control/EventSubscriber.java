package hernandez.guerra.control;

import hernandez.guerra.exceptions.EventStoreBuilderException;

public interface EventSubscriber {
    void subscribe() throws EventStoreBuilderException;
}
