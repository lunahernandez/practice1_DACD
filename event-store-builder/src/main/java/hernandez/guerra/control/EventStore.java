package hernandez.guerra.control;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class EventStore {
    //TODO File fileOf(Message message)
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

    public void subscribeToEvents() throws JMSException {
        Connection connection = createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic destination = session.createTopic(topicName);
        MessageConsumer consumer = session.createDurableSubscriber(destination, clientID + "-" +topicName);

        consumer.setMessageListener(this::processReceivedMessage);
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = connectionFactory.createConnection();
        connection.setClientID(clientID);
        connection.start();
        return connection;
    }

    private void processReceivedMessage(Message message) {
        if (message instanceof TextMessage textMessage) {
            saveEventToFile(textMessage);
        }
    }


    private String buildEventStoreDirectoryPath(TextMessage textMessage) {
        try {
            String eventData = textMessage.getText();
            String ss = extractSsFromJson(eventData);
            return eventStoreDirectory + "/" + topicName + "/" + ss;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getDateString(Instant timestamp) {
        LocalDate localDate = LocalDate.ofInstant(timestamp, ZoneOffset.UTC);
        return localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
    }


    private String extractSsFromJson(String eventData) {
        try {
            JsonElement jsonElement = JsonParser.parseString(eventData);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.getAsJsonPrimitive("ss").getAsString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildEventFilePath(TextMessage textMessage) {
        try {
            Instant eventTimestamp = Instant.ofEpochMilli(textMessage.getJMSTimestamp());
            String dateString = getDateString(eventTimestamp);
            return buildEventStoreDirectoryPath(textMessage) + "/" + dateString + ".events";
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveEventToFile(TextMessage textMessage) {
        String directoryPath = buildEventStoreDirectoryPath(textMessage);
        String filePath = buildEventFilePath(textMessage);
        checkIfDirectoryExists(directoryPath);
        saveEvent(filePath, textMessage);
    }

    private void checkIfDirectoryExists(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + directoryPath);
            }
        }
    }


    private void saveEvent(String filePath, TextMessage textMessage) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            String eventData = textMessage.getText();
            writer.write(eventData + System.lineSeparator());
            System.out.println("Saved: " + eventData + " in: " + new File(filePath).getAbsolutePath());
        } catch (IOException | JMSException e) {
            throw new RuntimeException("Error saving event to file.", e);
        }
    }

}
