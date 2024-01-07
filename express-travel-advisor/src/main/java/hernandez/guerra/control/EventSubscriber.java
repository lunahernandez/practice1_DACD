package hernandez.guerra.control;

import hernandez.guerra.exceptions.ExpressTravelBusinessUnitException;

public interface EventSubscriber {
    void subscribe() throws ExpressTravelBusinessUnitException;
}
