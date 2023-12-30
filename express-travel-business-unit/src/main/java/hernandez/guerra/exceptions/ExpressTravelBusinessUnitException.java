package hernandez.guerra.exceptions;

public class ExpressTravelBusinessUnitException extends Exception{
    public ExpressTravelBusinessUnitException(String message) {
        super(message);
    }

    public ExpressTravelBusinessUnitException(String message, Throwable cause) {
        super(message, cause);
    }
}
