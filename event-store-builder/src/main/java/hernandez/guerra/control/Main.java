package hernandez.guerra.control;

import org.apache.activemq.ActiveMQConnection;

public class Main {
    public static void main(String[] args) {
        String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
        String topicName = "prediction.Weather";
        String eventStoreDirectory = "eventStore";

        EventStore eventStore = new EventStore(brokerUrl, topicName, "subscriber-1", eventStoreDirectory);
        eventStore.subscribeToWeatherEvents();
    }
}
