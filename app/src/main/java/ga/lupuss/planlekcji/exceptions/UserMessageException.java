package ga.lupuss.planlekcji.exceptions;

public class UserMessageException extends Exception {

    private String userMessage;

    public UserMessageException(String userMessage, String message) {

        super(message);
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
