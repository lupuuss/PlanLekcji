package ga.lupuss.planlekcji.exceptions;

public class NoInternetException extends UserMessageException {

    public NoInternetException(String userMessage) {

        super(userMessage, "No internet connection");
    }
}
