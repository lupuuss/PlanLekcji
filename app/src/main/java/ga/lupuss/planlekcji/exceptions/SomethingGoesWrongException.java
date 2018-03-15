package ga.lupuss.planlekcji.exceptions;

import android.support.annotation.NonNull;

public class SomethingGoesWrongException extends UserMessageException {

    public SomethingGoesWrongException(@NonNull String userMessage){

        super(userMessage, "Unknown error!");
    }
}
