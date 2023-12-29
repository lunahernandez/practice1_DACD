package hernandez.guerra.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hernandez.guerra.exceptions.AccommodationProviderException;
import hernandez.guerra.model.Accommodation;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.time.Instant;

public class JMSAccommodationStore implements AccommodationStore{
    private final String topicName;
    private final String url;

    public JMSAccommodationStore(String topicName, String url) {
        this.topicName = topicName;
        this.url = url;
    }

    @Override
    public void save(Accommodation accommodation) throws AccommodationProviderException {
        try (Connection connection = createConnection()) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);

            TextMessage message = createTextMessage(accommodation, session);
            producer.send(message);

        } catch (JMSException e) {
            throw new AccommodationProviderException(e.getMessage(), e);
        }
    }

    private TextMessage createTextMessage(Accommodation accommodation, Session session) throws JMSException {
        String jsonAccommodation = convertAccommodationToJson(accommodation);
        return session.createTextMessage(jsonAccommodation);
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }

    private String convertAccommodationToJson(Accommodation accommodation) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .create();
        return gson.toJson(accommodation);
    }
}
