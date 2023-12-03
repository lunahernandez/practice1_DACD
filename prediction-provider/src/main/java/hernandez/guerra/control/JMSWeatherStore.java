package hernandez.guerra.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hernandez.guerra.model.Location;
import hernandez.guerra.model.Weather;

import java.time.Instant;
import java.util.List;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
public class JMSWeatherStore implements WeatherStore {
    private final String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private final String topicName;

    public JMSWeatherStore(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public void save(Weather weather) {
        try (Connection connection = createConnection()) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);

            TextMessage message = createTextMessage(weather, session);
            producer.send(message);

        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private TextMessage createTextMessage(Weather weather, Session session) throws JMSException {
        String jsonWeather = convertWeatherToJson(weather);
        return session.createTextMessage(jsonWeather);
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }

    private String convertWeatherToJson(Weather weather) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .create();
        return gson.toJson(weather);
    }

    @Override
    public void open(List<Location> locationList) {

    }

    @Override
    public Weather get(Location location, Instant ts) {
        return null;
    }

    @Override
    public void close() {

    }

}
