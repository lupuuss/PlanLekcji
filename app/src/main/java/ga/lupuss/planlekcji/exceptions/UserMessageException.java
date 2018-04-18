package ga.lupuss.planlekcji.exceptions;

import android.support.annotation.NonNull;

public class UserMessageException extends Exception {

    private int userMessageId;

    public UserMessageException(int userMessageId, @NonNull String message) {

        super(message);
        this.userMessageId = userMessageId;
    }

    public int getUserMessageId() {
        return userMessageId;
    }
}
