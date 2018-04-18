package ga.lupuss.planlekcji.exceptions;

import ga.lupuss.planlekcji.R;

public class TimeoutException extends UserMessageException {

    public TimeoutException(){

        super(R.string.msg_timeout_error, "Timeout reached!");
    }
}
