package ga.lupuss.planlekcji.managers.timetablemanager;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ListParser {

    private final static String nameField = "name";

    private final static boolean ignoreTags = true;

    @NonNull static List<Pair<String, String>> parseHoursList(@NonNull JSONArray json) throws JSONException {

        List<Pair<String, String>> hours = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {
            JSONObject obj = json.getJSONObject(i);
            hours.add(new Pair<>(obj.getString("start"), obj.getString("end")));
        }

        return hours;
    }

    @NonNull static  List<String> parseTimetableList(@NonNull JSONArray json,
                                                     @NonNull TimetableType type) throws JSONException {

        if (type.isNameAvailable()) {
            throw new IllegalArgumentException("This method is for CLASS and CLASSROOM types.");
        }

        List<String> list = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {

            list.add(json.getJSONObject(i).getString(type.getSlugName()));
        }

        return list;
    }

    @NonNull static Map<String, String> parseTimetableListWithNames(@NonNull JSONArray json) throws JSONException {

        Map<String, String> map = new LinkedHashMap<>();
        TimetableType type = TimetableType.TEACHER;

        for (int i = 0; i < json.length(); i++) {

            JSONObject obj = json.getJSONObject(i);
            String slug = obj.getString(type.getSlugName());


            if (ignoreTags && !slug.contains("#")) {

                if (!obj.isNull(nameField)) {

                    map.put(slug, obj.getString(nameField));

                } else {

                    map.put(slug, null);
                }
            }
        }

        return map;
    }

}
