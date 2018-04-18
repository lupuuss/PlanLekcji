package ga.lupuss.planlekcji.exceptions;

import ga.lupuss.planlekcji.R;

public class ServerException extends UserMessageException {

    public ServerException() {

        super(R.string.msg_server_error, "Server not working!");
    }
}
