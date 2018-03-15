package ga.lupuss.planlekcji.exceptions;

import android.support.annotation.NonNull;

public class JsonParserException extends UserMessageException {

    public JsonParserException(@NonNull String userMessage) {

        super(userMessage, "Json parsing error");
    }
}
