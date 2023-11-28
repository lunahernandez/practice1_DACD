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
    private final String topicName = "prediction.Weather";
    @Override
    public void save(Weather weather) {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        try (Connection connection = connectionFactory.createConnection()) {
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic destination = session.createTopic(topicName);

            MessageProducer producer = session.createProducer(destination);
            //TextMessage message = session.createTextMessage("Hello from JMS Publisher!");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                    .create();
            String jsonWeather = gson.toJson(weather);
            TextMessage message = session.createTextMessage(jsonWeather);
            producer.send(message);

            System.out.println("Message sent to topic '" + topicName + "': '" + message.getText() + "'");
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void open(List<Location> locationList) {

    }

    @Override
    public Weather get(Location location, Instant ts) {
        return null;
    }

    @Override
    public void close() throws Exception {

    }

}
