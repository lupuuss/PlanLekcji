package ga.lupuss.planlekcji.managers.timetablemanager;

import android.support.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import ga.lupuss.planlekcji.statics.Info;
import ga.lupuss.simplehttp.MultiRequest;
import ga.lupuss.simplehttp.Response;
import ga.lupuss.simplehttp.SimpleHttp;

public final class BasicTimetableResponseProvider implements TimetableResponseProvider {

    @Override
    @NonNull public Response getTimetable(@NonNull String slug, @NonNull TimetableType type) {

        try {
            slug = URLEncoder.encode(slug, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            // ignored
        }

        return SimpleHttp.get(Info.API_URL + type.getApiPath() + slug)
                .response();
    }

    @Override
    @NonNull public List<Response> getAllLists() {

        MultiRequest multiRequest = new MultiRequest(
                true,
                SimpleHttp.get(Info.API_URL + TimetableType.CLASS.getApiPath()).build(),
                SimpleHttp.get(Info.API_URL + TimetableType.CLASSROOM.getApiPath()).build(),
                SimpleHttp.get(Info.API_URL + HOURS_PATH).build(),
                SimpleHttp.get(Info.API_URL + TimetableType.TEACHER.getApiPath()).build()
        );

        return multiRequest.getResponses();
    }
}
