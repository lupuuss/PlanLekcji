package ga.lupuss.planlekcji.exceptions;

import android.support.annotation.NonNull;

public class NoInternetException extends UserMessageException {

    public NoInternetException(@NonNull String userMessage) {

        super(userMessage, "No internet connection");
    }
}
