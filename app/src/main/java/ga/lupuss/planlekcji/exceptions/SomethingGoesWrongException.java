package ga.lupuss.planlekcji.exceptions;

import ga.lupuss.planlekcji.R;

public class SomethingGoesWrongException extends UserMessageException {

    public SomethingGoesWrongException(){

        super(R.string.msg_something_goes_wrong, "Unknown error!");
    }
}
