package ga.lupuss.planlekcji.onlinetools;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InterruptedIOException;

import ga.lupuss.planlekcji.exceptions.JsonParserException;
import ga.lupuss.planlekcji.exceptions.ServerException;
import ga.lupuss.planlekcji.exceptions.ServerResourcesException;
import ga.lupuss.planlekcji.exceptions.SomethingGoesWrongException;
import ga.lupuss.planlekcji.exceptions.TimeoutException;
import ga.lupuss.planlekcji.exceptions.UserInterruptedException;
import ga.lupuss.planlekcji.exceptions.UserMessageException;
import ga.lupuss.simplehttp.Response;

public class ResponseUtil {

    @NonNull
    public static JSONObject fetchResponseToJsonObject(@NonNull Response response)
            throws UserMessageException {

        if (response.isResponseCodeOK()) {

            try {

                return response.bodyAsJsonObj();

            } catch (JSONException e) {

                e.printStackTrace();
                throw new JsonParserException();
            }

        } else {

            throw fetchResponseException(response);
        }
    }

    @NonNull
    public static  JSONArray fetchResponseToJsonArray(@NonNull Response response) throws UserMessageException {

        if (response.isResponseCodeOK()) {

            try {

                return response.bodyAsJsonArray();

            } catch (JSONException e) {

                e.printStackTrace();
                throw new JsonParserException();
            }

        } else {

            throw fetchResponseException(response);
        }
    }

    @NonNull
    private static UserMessageException fetchResponseException(@NonNull Response response)  {

        int code = response.getResponseCode();

        if (code == 0){

            if (response.getResponseMessage().startsWith(Response.TIMEOUT_MSG)) {

                return new TimeoutException();

            }

            try {

                response.rethrowException();

            } catch (IOException e) {

                if (e instanceof InterruptedIOException ||
                        e.getCause() instanceof InterruptedIOException) {

                    return new UserInterruptedException();
                }

            }

        }

        if (code > 500) {

            return new ServerException();

        } else if(code >= 400) {

            return new ServerResourcesException();
        }

        return new SomethingGoesWrongException();
    }

}
