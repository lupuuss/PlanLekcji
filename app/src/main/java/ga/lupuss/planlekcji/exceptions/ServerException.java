package ga.lupuss.planlekcji.exceptions;

import android.support.annotation.NonNull;

public class ServerException extends UserMessageException {

    public ServerException(@NonNull String userMessage) {

        super(userMessage, "Server not working!");
    }
}
