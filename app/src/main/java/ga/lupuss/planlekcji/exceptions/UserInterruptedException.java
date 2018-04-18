package ga.lupuss.planlekcji.exceptions;

import ga.lupuss.planlekcji.R;

public class UserInterruptedException extends UserMessageException {

    public UserInterruptedException() {

        super(R.string.msg_something_goes_wrong, "Task interrupted!");
    }
}
