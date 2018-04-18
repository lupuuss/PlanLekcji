package ga.lupuss.planlekcji.exceptions;

import ga.lupuss.planlekcji.R;

public class ServerResourcesException extends UserMessageException {

    public ServerResourcesException() {

        super(R.string.msg_server_resources_error, "Server couldn't find requested data!");
    }
}
