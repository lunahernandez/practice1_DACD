package hernandez.guerra.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

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

                String directoryPath = buildEventStoreDirectoryPath(eventTimestamp, eventData);
                String filePath = buildEventFilePath(eventTimestamp, eventData);

                saveEventToFile(directoryPath, filePath, eventData);
                System.out.println("Event stored successfully at: " + filePath);

            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private String buildEventStoreDirectoryPath(Instant timestamp, String eventData) {

        LocalDate localDate = LocalDate.ofInstant(timestamp, ZoneOffset.UTC);
        String dateString = localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        return eventStoreDirectory + "/" + topicName + "/" + extractSsFromJson(eventData) + "/" + dateString;
    }

    private String extractSsFromJson(String jsonData) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();
            return jsonObject.get("ss").getAsString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildEventFilePath(Instant timestamp, String eventData) {
        return buildEventStoreDirectoryPath(timestamp, eventData) + ".events";
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
