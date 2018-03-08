package ga.lupuss.planlekcji.exceptions;

public class TimeoutException extends UserMessageException {

    public TimeoutException(String userMessage){

        super(userMessage, "Timeout reached!");
    }
}
