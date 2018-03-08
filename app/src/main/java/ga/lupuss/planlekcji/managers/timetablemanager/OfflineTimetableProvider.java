package ga.lupuss.planlekcji.managers.timetablemanager;

import android.support.v4.util.Pair;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public interface OfflineTimetableProvider {

    Timetable getTimetable(String slug, TimetableType type);
    boolean containsTimetable(String slug, TimetableType type);
    void deleteTimetable(String slug, TimetableType type);
    boolean prepareOfflineData();
    boolean saveHours(String json);
    String loadHours();
    List<Pair<String, String>> getHours();
    List<String> getClasses();
    List<String> getClassrooms();
    Map<String, String> getTeachers();
    boolean isAnyTimetable();
    boolean keepTimetableOffline(String slug, TimetableType type, String json);
    void updateOfflineTimetable(String slug, TimetableType type, JSONObject json);
    void refreshLists();
}
