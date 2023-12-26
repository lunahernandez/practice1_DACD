package hernandez.guerra.control;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import hernandez.guerra.exceptions.EventStoreBuilderException;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class FileEventStore implements EventStore {
    private final String eventStoreDirectory;
    private final String topicName;

    public FileEventStore(String eventStoreDirectory, String topicName) {
        this.eventStoreDirectory = eventStoreDirectory;
        this.topicName = topicName;
    }

    @Override
    public void saveEvent(TextMessage textMessage) throws EventStoreBuilderException {
        File file = fileOf(textMessage);
        createDirectory(file);
        writeEventToFile(file, textMessage);
    }

    private File fileOf(TextMessage textMessage) throws EventStoreBuilderException {
        String filePath = buildEventFilePath(textMessage);
        return new File(filePath);
    }

    private String buildEventFilePath(TextMessage textMessage) throws EventStoreBuilderException {
        try {
            String eventData = textMessage.getText();
            String ss = extractSsFromJson(eventData);
            Instant eventTimestamp = Instant.ofEpochMilli(textMessage.getJMSTimestamp());
            String dateString = getDateString(eventTimestamp);
            return eventStoreDirectory + "/" + topicName + "/" + ss + "/" + dateString + ".events";
        } catch (JMSException e) {
            throw new EventStoreBuilderException(e.getMessage(), e);
        }
    }

    private static String getDateString(Instant timestamp) {
        LocalDate localDate = LocalDate.ofInstant(timestamp, ZoneOffset.UTC);
        return localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
    }


    private String extractSsFromJson(String eventData) throws EventStoreBuilderException {
        try {
            JsonElement jsonElement = JsonParser.parseString(eventData);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.getAsJsonPrimitive("ss").getAsString();
        } catch (JsonSyntaxException e) {
            throw new EventStoreBuilderException(e.getMessage(), e);
        }
    }

    private void createDirectory(File file) throws EventStoreBuilderException {
        File parentDirectory = file.getParentFile();

        if (!parentDirectory.exists()) {
            if (!parentDirectory.mkdirs()) {
                throw new EventStoreBuilderException("Failed to create directory: " + parentDirectory.getAbsolutePath());
            }
        }
    }

    private void writeEventToFile(File file, TextMessage textMessage) throws EventStoreBuilderException {
        try (FileWriter writer = new FileWriter(file, true)) {
            String eventData = textMessage.getText();
            writer.write(eventData + System.lineSeparator());
        } catch (IOException | JMSException e) {
            throw new EventStoreBuilderException(e.getMessage(), e);
        }
    }
}
