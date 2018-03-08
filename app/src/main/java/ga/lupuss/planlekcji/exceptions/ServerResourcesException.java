package ga.lupuss.planlekcji.exceptions;

public class ServerResourcesException extends UserMessageException {

    public ServerResourcesException(String userMessage) {

        super(userMessage, "Server couldn't find requested data!");
    }
}
