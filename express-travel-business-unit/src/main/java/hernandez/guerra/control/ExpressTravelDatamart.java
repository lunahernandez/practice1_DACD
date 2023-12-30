package hernandez.guerra.control;

import jakarta.jms.TextMessage;

public interface ExpressTravelDatamart {
    void update(TextMessage textMessage, String topicName);
}
