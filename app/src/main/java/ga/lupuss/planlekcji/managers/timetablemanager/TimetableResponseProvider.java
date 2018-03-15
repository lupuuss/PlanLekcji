package ga.lupuss.planlekcji.managers.timetablemanager;

import android.support.annotation.NonNull;

import java.util.List;

import ga.lupuss.simplehttp.Response;

@SuppressWarnings("WeakerAccess")
public interface TimetableResponseProvider {

    String HOURS_PATH = "hours";

    @NonNull Response getTimetable(@NonNull String slug, @NonNull TimetableType type);

    @NonNull List<Response> getAllLists();

}
