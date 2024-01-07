package hernandez.guerra.control;

import hernandez.guerra.exceptions.ExpressTravelBusinessUnitException;
import hernandez.guerra.view.TravelRecommendationCLI;
import org.apache.activemq.ActiveMQConnection;

public class Main {
    public static void main(String[] args) throws ExpressTravelBusinessUnitException {
        String dbPath = args[0];
        String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
        String weatherTopicName = "prediction.Weather";
        String accommodationTopicName = "prediction.Accommodation";
        String eventStoreDirectory = "eventStore";

        DatamartInitializer dataMartInitializer = new DatamartInitializer(eventStoreDirectory);
        ExpressTravelDatamart datamart = new ExpressTravelSQLiteDatamart(dbPath);
        EventSubscriber eventSubscriber = new JMSEventSubscriber(brokerUrl, weatherTopicName, accommodationTopicName,
                "expressTravelBusinessUnit", datamart);
        datamart.initialize(weatherTopicName, accommodationTopicName, dataMartInitializer);
        eventSubscriber.subscribe();

        TravelRecommendationLogic logic = new TravelRecommendationLogic(datamart);
        TravelRecommendationCLI cli = new TravelRecommendationCLI(logic);
        cli.run();
    }
}