package hernandez.guerra.control;

import hernandez.guerra.exceptions.ExpressTravelBusinessUnitException;
import org.apache.activemq.ActiveMQConnection;

public class Main {
    public static void main(String[] args) throws ExpressTravelBusinessUnitException {
        String dbPath = args[0];
        System.out.println(dbPath);
        //String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
        String weatherTopicName = "prediction.Weather";
        String accommodationTopicName = "prediction.Accommodation";
        String eventStoreDirectory = "eventStore";

        DatamartInitializer dataMartInitializer = new DatamartInitializer(eventStoreDirectory);
        ExpressTravelDatamart datamart = new ExpressTravelSQLiteDatamart(dbPath);
        // EventSubscriber eventSubscriber = new JMSEventSubscriber(brokerUrl, weatherTopicName, accommodationTopicName,
        //         "expressTravelBusinessUnit", datamart);
        // System.out.println("Creados");
        // eventSubscriber.subscribe();
        datamart.initialize(weatherTopicName, accommodationTopicName, dataMartInitializer);

    }
}