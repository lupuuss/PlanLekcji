package ga.lupuss.planlekcji.exceptions;

import ga.lupuss.planlekcji.R;

public class NoInternetException extends UserMessageException {

    public NoInternetException() {

        super(R.string.msg_no_internet, "No internet connection");
    }
}
