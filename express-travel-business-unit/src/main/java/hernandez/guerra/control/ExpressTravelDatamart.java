package hernandez.guerra.control;

import hernandez.guerra.exceptions.ExpressTravelBusinessUnitException;
import jakarta.jms.TextMessage;

public interface ExpressTravelDatamart {
    void update(TextMessage textMessage, String topicName);

    void initialize(String weatherTopicName, String accommodationTopicName, DatamartInitializer datamartInitializer)
            throws ExpressTravelBusinessUnitException;
}
