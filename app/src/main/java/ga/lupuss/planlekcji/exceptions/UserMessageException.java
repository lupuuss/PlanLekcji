package ga.lupuss.planlekcji.exceptions;

import android.support.annotation.NonNull;

public class UserMessageException extends Exception {

    private String userMessage;

    public UserMessageException(@NonNull String userMessage, @NonNull String message) {

        super(message);
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
