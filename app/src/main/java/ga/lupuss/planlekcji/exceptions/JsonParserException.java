package ga.lupuss.planlekcji.exceptions;

import ga.lupuss.planlekcji.R;

public class JsonParserException extends UserMessageException {

    public JsonParserException() {

        super(R.string.msg_json_error, "Json parsing error");
    }
}
