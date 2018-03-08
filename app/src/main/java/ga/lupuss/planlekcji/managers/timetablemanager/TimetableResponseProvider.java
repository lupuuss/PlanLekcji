package ga.lupuss.planlekcji.managers.timetablemanager;

import java.util.List;

import ga.lupuss.simplehttp.Response;

@SuppressWarnings("WeakerAccess")
public interface TimetableResponseProvider {

    String HOURS_PATH = "hours";

    Response getTimetable(String slug, TimetableType type);

    List<Response> getAllLists();

}
