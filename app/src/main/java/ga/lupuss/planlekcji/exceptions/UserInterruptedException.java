package ga.lupuss.planlekcji.exceptions;

public class UserInterruptedException extends UserMessageException {

    public UserInterruptedException(String userMessage) {

        super(userMessage, "Task interrupted!");
    }
}
