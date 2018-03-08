package ga.lupuss.planlekcji.exceptions;

public class JsonParserException extends UserMessageException {

    public JsonParserException(String userMessage) {

        super(userMessage, "Json parsing error");
    }
}
