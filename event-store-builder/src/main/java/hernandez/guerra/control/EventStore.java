package hernandez.guerra.control;

import jakarta.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class EventStore {
    private final String brokerUrl;
    private final String topicName;
    private final String clientID;
    private final String eventStoreDirectory;

    public EventStore(String brokerUrl, String topicName, String clientID, String eventStoreDirectory) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.clientID = clientID;
        this.eventStoreDirectory = eventStoreDirectory;
    }

    public void subscribeToWeatherEvents() {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        try (Connection connection = connectionFactory.createConnection()) {
            connection.setClientID(clientID);
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic destination = session.createTopic(topicName);

            MessageConsumer consumer = session.createDurableSubscriber(destination, clientID);

            consumer.setMessageListener(this::onMessage);

            waitForEvents();

        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void onMessage(Message message) {
        if (message instanceof TextMessage textMessage) {
            try {
                String eventData = textMessage.getText();
                Instant eventTimestamp = Instant.ofEpochMilli(textMessage.getJMSTimestamp());

                String directoryPath = buildEventStoreDirectoryPath(eventTimestamp);
                String filePath = buildEventFilePath(eventTimestamp);

                saveEventToFile(directoryPath, filePath, eventData);
                System.out.println("Event stored successfully at: " + filePath);

            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String buildEventStoreDirectoryPath(Instant timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String dateString = dateFormat.format(Date.from(timestamp));
        return eventStoreDirectory + "/" + topicName + "/" + dateString;
    }

    private String buildEventFilePath(Instant timestamp) {
        return buildEventStoreDirectoryPath(timestamp) + ".events";
    }

    private void saveEventToFile(String directoryPath, String filePath, String eventData) {
        new File(directoryPath).mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            writer.println(eventData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitForEvents() {
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
