package ga.lupuss.planlekcji.exceptions;

public class ServerException extends UserMessageException {

    public ServerException(String userMessage) {

        super(userMessage, "Server not working!");
    }
}
