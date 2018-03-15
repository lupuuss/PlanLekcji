package ga.lupuss.planlekcji.exceptions;

import android.support.annotation.NonNull;

public class TimeoutException extends UserMessageException {

    public TimeoutException(@NonNull String userMessage){

        super(userMessage, "Timeout reached!");
    }
}
