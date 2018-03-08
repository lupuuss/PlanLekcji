package ga.lupuss.planlekcji.exceptions;

public class SomethingGoesWrongException extends UserMessageException {

    public SomethingGoesWrongException(String userMessage){

        super(userMessage, "Unknown error!");
    }
}
