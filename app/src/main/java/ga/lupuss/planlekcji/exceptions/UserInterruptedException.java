package ga.lupuss.planlekcji.exceptions;

import android.support.annotation.NonNull;

public class UserInterruptedException extends UserMessageException {

    public UserInterruptedException(@NonNull String userMessage) {

        super(userMessage, "Task interrupted!");
    }
}
