package ga.lupuss.planlekcji.managers.timetablemanager;

import org.json.JSONObject;

@SuppressWarnings({"unused","WeakerAccess"})
public class Timetable {

    private String slug = "";
    private JSONObject data = new JSONObject();
    private boolean offline;
    private boolean fromOfflineSource;

    Timetable(String slug, JSONObject data, boolean offline, boolean fromOfflineSource) {
        this.slug = slug;
        this.data = data;
        this.offline = offline;
        this.fromOfflineSource = fromOfflineSource;
    }

    public String getSlug() {

        return slug;
    }

    public JSONObject getJsonData() {

        return data;
    }

    public boolean isOffline() {

        return offline;
    }

    public boolean isFromOfflineSource() {

        return fromOfflineSource;
    }

    void setJsonData(JSONObject jsonData) {

        data = jsonData;
    }
}
