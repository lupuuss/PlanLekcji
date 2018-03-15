package ga.lupuss.planlekcji.onlineoptions;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ga.lupuss.planlekcji.BuildConfig;
import ga.lupuss.planlekcji.statics.Info;
import ga.lupuss.simplehttp.Header;
import ga.lupuss.simplehttp.Response;
import ga.lupuss.simplehttp.SimpleHttp;

public class ReportOutOfDataCallback {

    private final static String OUT_OF_DATE_PATH = "request-update";

    private String phoneID;

    public ReportOutOfDataCallback(@NonNull String phoneID) {

        this.phoneID = phoneID;
    }

    public int report() {

        JSONObject json = new JSONObject();
        try {

            json.put("dev", BuildConfig.DEBUG);
            json.put("phoneID", phoneID);

        } catch (JSONException e) {

            e.printStackTrace();
        }

        Response response = SimpleHttp.put(Info.API_URL + OUT_OF_DATE_PATH)
                .header(Header.ContentType, "application/json")
                .header(Header.Accept, "application/json")
                .header(Header.Authorization, Info.getAuthorization())
                .header(Header.UserAgent, Info.getUserAgent())
                .data(json)
                .response();

        Log.d(ReportOutOfDataCallback.class.getName(), response.bodyAsString());

        return response.getResponseCode();
    }
}
