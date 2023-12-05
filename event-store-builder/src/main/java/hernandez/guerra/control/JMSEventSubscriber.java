package hernandez.guerra.control;

import hernandez.guerra.exceptions.EventStoreBuilderException;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JMSEventSubscriber implements EventSubscriber {
    private final String brokerUrl;
    private final String topicName;
    private final String clientID;
    private final EventStore weatherEventStore;

    public JMSEventSubscriber(String brokerUrl, String topicName, String clientID, EventStore weatherEventStore) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.clientID = clientID;
        this.weatherEventStore = weatherEventStore;
    }

    @Override
    public void subscribe() throws EventStoreBuilderException {
        try {
            Connection connection = createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic destination = session.createTopic(topicName);
            MessageConsumer consumer = session.createDurableSubscriber(destination, clientID + "-" + topicName);

            setupMessageListener(consumer);
        } catch (JMSException e) {
            throw new EventStoreBuilderException(e.getMessage(), e);
        }
    }

    private void setupMessageListener(MessageConsumer consumer) throws JMSException {
        consumer.setMessageListener(message -> {
            try {
                processReceivedMessage(message);
            } catch (EventStoreBuilderException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = connectionFactory.createConnection();
        connection.setClientID(clientID);
        connection.start();
        return connection;
    }

    private void processReceivedMessage(Message message) throws EventStoreBuilderException {
        if (message instanceof TextMessage textMessage) {
            weatherEventStore.saveEvent(textMessage);
        }
    }
}
