package hernandez.guerra.control;

import hernandez.guerra.exceptions.EventStoreBuilderException;
import org.apache.activemq.ActiveMQConnection;

public class Main {
    public static void main(String[] args) throws EventStoreBuilderException {
        String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
        String weatherTopicName = "prediction.Weather";
        String accommodationTopicName = "prediction.Accommodation";
        String eventStoreDirectory = "eventStore";

        EventStore eventStore = new FileEventStore(eventStoreDirectory);
        EventSubscriber eventSubscriber = new JMSEventSubscriber(brokerUrl, weatherTopicName, accommodationTopicName, "eventStoreBuilder", eventStore);
        System.out.println("Creados");
        eventSubscriber.subscribe();
    }
}
