package ga.lupuss.planlekcji.tools;

import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InterruptedIOException;

import ga.lupuss.planlekcji.R;
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
    public static JSONObject fetchResponseToJsonObject(@NonNull Context appContext,
                                                       @NonNull Response response) throws UserMessageException {

        if (response.isResponseCodeOK()) {

            try {

                return response.bodyAsJsonObj();

            } catch (JSONException e) {

                e.printStackTrace();
                throw new JsonParserException(appContext.getString(R.string.msg_json_error));
            }

        } else {

            throw fetchResponseException(appContext, response);
        }
    }

    @NonNull
    public static  JSONArray fetchResponseToJsonArray(@NonNull Context appContext,
                                                      @NonNull Response response) throws UserMessageException {

        if (response.isResponseCodeOK()) {

            try {

                return response.bodyAsJsonArray();

            } catch (JSONException e) {

                e.printStackTrace();
                throw new JsonParserException(appContext.getString(R.string.msg_json_error));
            }

        } else {

            throw fetchResponseException(appContext, response);
        }
    }

    @NonNull
    private static UserMessageException fetchResponseException(@NonNull Context appContext,
                                                               @NonNull Response response)  {

        int code = response.getResponseCode();

        if (code == 0){

            if (response.getResponseMessage().startsWith(Response.TIMEOUT_MSG)) {

                return new TimeoutException(appContext.getString(R.string.msg_timeout_error));

            }

            try {

                response.rethrowException();

            } catch (IOException e) {

                if (e instanceof InterruptedIOException ||
                        e.getCause() instanceof InterruptedIOException) {

                    return new UserInterruptedException(
                            appContext.getString(R.string.msg_something_goes_wrong)
                    );
                }

            }

        }

        if (code > 500) {

            return new ServerException(appContext.getString(R.string.msg_server_error));

        } else if(code >= 400) {

            return new ServerResourcesException(
                    appContext.getString(R.string.msg_server_resources_error)
            );
        }

        return new SomethingGoesWrongException(
                appContext.getString(R.string.msg_something_goes_wrong)
        );
    }

}
