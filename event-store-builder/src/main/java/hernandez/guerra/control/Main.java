package hernandez.guerra.control;

import jakarta.jms.JMSException;
import org.apache.activemq.ActiveMQConnection;

public class Main {
    public static void main(String[] args) throws JMSException {
        String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
        String topicName = "prediction.Weather";
        String eventStoreDirectory = "eventStore";

        EventStore eventStore = new EventStore(brokerUrl, topicName, "eventStoreBuilder", eventStoreDirectory);
        eventStore.subscribeToEvents();
    }
}
