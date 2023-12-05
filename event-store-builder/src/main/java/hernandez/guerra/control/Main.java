package hernandez.guerra.control;

import hernandez.guerra.exceptions.EventStoreBuilderException;
import org.apache.activemq.ActiveMQConnection;

public class Main {
    public static void main(String[] args) throws EventStoreBuilderException {
        String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
        String topicName = "prediction.Weather";
        String eventStoreDirectory = "eventStore";


        EventStore eventStore = new WeatherEventStore(eventStoreDirectory, topicName);
        EventSubscriber eventSubscriber = new JMSEventSubscriber(brokerUrl, topicName, "eventStoreBuilder", eventStore);
        eventSubscriber.subscribe();
    }
}
