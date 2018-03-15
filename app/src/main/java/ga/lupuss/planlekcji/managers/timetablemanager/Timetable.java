package ga.lupuss.planlekcji.managers.timetablemanager;

import android.support.annotation.NonNull;

import org.json.JSONObject;

public class Timetable {

    private String slug = "";
    private JSONObject data = new JSONObject();
    private boolean fromOfflineSource;

    Timetable(@NonNull String slug,
              @NonNull JSONObject data,
              boolean fromOfflineSource) {

        this.slug = slug;
        this.data = data;
        this.fromOfflineSource = fromOfflineSource;
    }

    @NonNull
    public String getSlug() {

        return slug;
    }

    @NonNull
    public JSONObject getJsonData() {

        return data;
    }

    public boolean isFromOfflineSource() {

        return fromOfflineSource;
    }

    void setJsonData(@NonNull JSONObject jsonData) {

        data = jsonData;
    }
}
