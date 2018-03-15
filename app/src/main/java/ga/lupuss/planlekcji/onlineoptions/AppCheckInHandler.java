package ga.lupuss.planlekcji.onlineoptions;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ga.lupuss.planlekcji.BuildConfig;
import ga.lupuss.planlekcji.statics.Info;
import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.exceptions.JsonParserException;
import ga.lupuss.planlekcji.exceptions.UserMessageException;
import ga.lupuss.planlekcji.tools.ResponseUtil;
import ga.lupuss.simplehttp.Header;
import ga.lupuss.simplehttp.Response;
import ga.lupuss.simplehttp.SimpleHttp;

@SuppressWarnings("FieldCanBeLocal")
public class AppCheckInHandler {

    private String currentVersion;

    private String apiVersion;
    private String apiMessage;
    private List<String> apiChangeLog;
    private String apiApkUrl;

    private final String PATH_MOBILE_APP = "mobile-app";
    private final String PATH_USERS = "/users";
    private final String VERSION_FIELD = "version";
    private final String APK_URL_FIELD = "apkFileUrl";
    private final String MESSAGE_FIELD = "message";
    private final String CHANGELOG_FIELD = "changelog";

    private Context context;

    public AppCheckInHandler(@NonNull Context context, @NonNull String version) {
        currentVersion = version;
        this.context = context;
    }

    public boolean checkForUpdate() throws UserMessageException {


        Response response = SimpleHttp.get(Info.API_URL + PATH_MOBILE_APP).response();

        parseJson(ResponseUtil.fetchResponseToJsonObject(context, response));

        return !currentVersion.equals(apiVersion);
    }

    private void parseJson(@NonNull JSONObject json) throws UserMessageException {

        try {

            apiVersion = json.getString(VERSION_FIELD);
            apiMessage = json.getString(MESSAGE_FIELD);
            apiApkUrl = json.getString(APK_URL_FIELD);

            JSONArray array = json.getJSONArray(CHANGELOG_FIELD);

            apiChangeLog = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {

                apiChangeLog.add(array.getString(i));
            }


        } catch (JSONException e) {

            throw new JsonParserException(context.getString(R.string.msg_json_error));

        }
    }

    @NonNull
    public String getApiVersion() {

        checkNull(apiVersion);

        return apiVersion;
    }

    @NonNull
    public String getApiMessage() {

        checkNull(apiMessage);
        return apiMessage;
    }

    @NonNull
    public List<String> getApiChangeLog() {

        checkNull(apiChangeLog);
        return apiChangeLog;
    }

    @NonNull
    public String getApiApkUrl() {

        checkNull(apiApkUrl);
        return apiApkUrl;
    }

    public void sendIdentity(@NonNull String mostVisitedSlug,
                             @NonNull String mostVisitedSlugType,
                             @NonNull String androidID) {

        try {

            JSONObject jsonObj = new JSONObject();
            JSONObject mostPop = new JSONObject();

            mostPop.put("slug", mostVisitedSlug);
            mostPop.put("type", mostVisitedSlugType.toLowerCase());

            jsonObj.put("phoneModel", Info.getDeviceName());
            jsonObj.put("phoneID", androidID);
            jsonObj.put("mostPopularTimetable", mostPop);
            jsonObj.put("appVersion", BuildConfig.VERSION_NAME);
            jsonObj.put("dev", BuildConfig.DEBUG);
            jsonObj.put("osVersion", Info.getAndroidName());

            Log.d(AppCheckInHandler.class.getName(), "Prepared json: " + jsonObj.toString());

            Log.d(
                    AppCheckInHandler.class.getName(),
                    "Request result: " +
                    SimpleHttp.put(Info.API_URL + PATH_MOBILE_APP + PATH_USERS)
                        .header(Header.ContentType, "application/json")
                        .header(Header.Accept, "application/json")
                        .header(Header.Authorization, Info.getAuthorization())
                        .header(Header.UserAgent, Info.getUserAgent())
                        .data(jsonObj)
                        .response()
                        .bodyAsString()
            );

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private <T> void checkNull(@Nullable T mayBeNull) {

        if(mayBeNull == null) {

            throw new IllegalStateException("Getter used before checkForUpdate() method");
        }
    }
}
