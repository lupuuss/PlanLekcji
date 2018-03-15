package ga.lupuss.planlekcji.managers.timetablemanager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public interface OfflineTimetableProvider {

    Timetable getTimetable(@NonNull String slug, @NonNull TimetableType type);
    boolean containsTimetable(@NonNull String slug, @NonNull TimetableType type);
    void deleteTimetable(@NonNull String slug, @NonNull TimetableType type);
    boolean prepareOfflineData();
    boolean saveHours(@NonNull String json);
    @Nullable String loadHoursJson();
    @NonNull List<Pair<String, String>> getHours();
    @NonNull List<String> getClasses();
    @NonNull List<String> getClassrooms();
    @NonNull Map<String, String> getTeachers();
    boolean isAnyTimetable();
    boolean keepTimetableOffline(@NonNull String slug, TimetableType type, @NonNull String json);
    void updateOfflineTimetable(@NonNull String slug, @NonNull TimetableType type, @NonNull JSONObject json);
    void refreshLists();
}
