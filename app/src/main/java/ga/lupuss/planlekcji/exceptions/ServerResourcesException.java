package ga.lupuss.planlekcji.exceptions;

import android.support.annotation.NonNull;

public class ServerResourcesException extends UserMessageException {

    public ServerResourcesException(@NonNull String userMessage) {

        super(userMessage, "Server couldn't find requested data!");
    }
}
