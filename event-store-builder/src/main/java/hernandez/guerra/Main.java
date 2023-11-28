package hernandez.guerra;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Main {
    public static void main(String[] args) {
        String url = ActiveMQConnection.DEFAULT_BROKER_URL;
        String topicName = "prediction.Weather";

        // Obtener la conexión JMS desde el servidor
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        try (Connection connection = connectionFactory.createConnection()) {
            connection.setClientID("subscriber-1"); // ID del cliente para el suscriptor duradero
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic destination = session.createTopic(topicName);

            // Crear un suscriptor duradero
            MessageConsumer consumer = session.createDurableSubscriber(destination, "subscriber-1");

            // Establecer un MessageListener para recibir mensajes de forma asíncrona
            consumer.setMessageListener(message -> {
                if (message instanceof TextMessage textMessage) {
                    try {
                        System.out.println("Received message '" + textMessage.getText() + "'");
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            // Mantener el programa en ejecución para recibir mensajes continuamente
            Thread.sleep(1000000);
        } catch (JMSException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
