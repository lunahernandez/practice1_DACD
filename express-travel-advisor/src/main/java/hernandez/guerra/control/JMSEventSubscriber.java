package hernandez.guerra.control;

import hernandez.guerra.exceptions.ExpressTravelBusinessUnitException;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JMSEventSubscriber implements EventSubscriber {
    private final String brokerUrl;
    private final String topicName1;
    private final String topicName2;
    private final String clientID;
    private final ExpressTravelDatamart expressTravelDatamart;

    public JMSEventSubscriber(String brokerUrl, String topicName1, String topicName2, String clientID,
                              ExpressTravelDatamart expressTravelDatamart) {
        this.brokerUrl = brokerUrl;
        this.topicName1 = topicName1;
        this.topicName2 = topicName2;
        this.clientID = clientID;
        this.expressTravelDatamart = expressTravelDatamart;
    }

    @Override
    public void subscribe() throws ExpressTravelBusinessUnitException {
        try {
            Connection connection = createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            subscribeToTopic(session, topicName1);
            subscribeToTopic(session, topicName2);

        } catch (JMSException e) {
            throw new ExpressTravelBusinessUnitException(e.getMessage(), e);
        }
    }

    private void subscribeToTopic(Session session, String topicName) throws JMSException {
        Topic destination = session.createTopic(topicName);
        MessageConsumer consumer = session.createDurableSubscriber(destination, clientID + "-" + topicName);
        setupMessageListener(consumer, topicName);
    }

    private void setupMessageListener(MessageConsumer consumer, String topicName) throws JMSException {
        consumer.setMessageListener(message -> {
            try {
                processReceivedMessage(message, topicName);
            } catch (ExpressTravelBusinessUnitException e) {
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

    private void processReceivedMessage(Message message, String topicName) throws ExpressTravelBusinessUnitException {
        if (message instanceof TextMessage textMessage) {
            expressTravelDatamart.update(textMessage, topicName);
        } else {
            System.out.println("Received message is not an instance of TextMessage. Ignoring...");
        }
    }
}
