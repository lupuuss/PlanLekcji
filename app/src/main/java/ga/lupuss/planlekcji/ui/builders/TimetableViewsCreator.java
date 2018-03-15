package ga.lupuss.planlekcji.ui.builders;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;

public final class TimetableViewsCreator {

    private LayoutInflater inflater;
    private ViewGroup parent;
    private List<Pair<String, String>> hours;
    private OnTimetableItemClick onTimetableItemClick;


    public TimetableViewsCreator(@NonNull LayoutInflater inflater,
                          ViewGroup parent,
                          @NonNull List<Pair<String, String>> hours,
                          OnTimetableItemClick onTimetableItemClick){

        this.inflater = inflater;
        this.parent = parent;
        this.hours = hours;
        this.onTimetableItemClick = onTimetableItemClick;
    }

    public List<View> create(@NonNull JSONArray timetableDayArray, @NonNull TimetableType type) {

        if (timetableDayArray.length() == 0) {

            return Collections.singletonList(LessonLayoutBuilder.empty(inflater, parent));
        }

        List<View> list = new ArrayList<>();

        try {

            for (int i = 0; i < timetableDayArray.length(); i++) {

                if (timetableDayArray.getJSONArray(i).isNull(0)){

                    list.add(emptyLesson(i));

                } else {

                    list.add(filledLesson(timetableDayArray.getJSONArray(i), type, i));

                }
            }
        } catch (JSONException e) {

            throw new IllegalStateException("Critical JSON error");
        }

        return list;
    }

    private View emptyLesson(int i) {
        return LessonLayoutBuilder.filled(inflater, parent)
                .number(i + 1)
                .hour(hours.get(i))
                .lessons("-")
                .addPopUpMenu(null, null)
                .build();
    }

    private View filledLesson(@NonNull JSONArray lessonArray, @NonNull TimetableType type, int number) {

        try {

            List<String> list = new ArrayList<>();
            List<Pair<TimetableType, String>> lessonsLinks = new ArrayList<>();

            for (int i = 0; i < lessonArray.length(); i++) {

                StringBuilder lessonLine = new StringBuilder();

                JSONObject jsonObject = lessonArray.getJSONObject(i);

                lessonLine.append(pickValueForField(type, jsonObject, 0));

                lessonLine.append(" ");

                if (type == TimetableType.TEACHER) {


                    fetchLessonPart(type, jsonObject, 1, lessonLine, lessonsLinks);

                    lessonLine.append(" ");

                    fetchLessonPart(type, jsonObject, 2, lessonLine, lessonsLinks);

                } else {

                    fetchLessonPart(type, jsonObject, 1, lessonLine, lessonsLinks);


                    lessonLine.append(" ");


                    if (!jsonObject.isNull(type.getField(2).first)) {

                        fetchLessonPart(type, jsonObject,2 , lessonLine, lessonsLinks);

                    } else {

                        fetchLessonPart(type, jsonObject,3 , lessonLine, lessonsLinks);
                    }
                }

                list.add(lessonLine.toString());
            }

            return LessonLayoutBuilder.filled(inflater, parent)
                    .number(number + 1)
                    .hour(hours.get(number))
                    .lessons(list.toArray(new String[]{}))
                    .addPopUpMenu(onTimetableItemClick, lessonsLinks)
                    .build();

        } catch (JSONException e) {

            throw new IllegalStateException("Critical JSON error", e);

        }
    }

    private void fetchLessonPart(@NonNull TimetableType type,
                                 @NonNull JSONObject jsonObject,
                                 int fieldID,
                                 @NonNull StringBuilder lessonLine,
                                 @NonNull List<Pair<TimetableType, String>> lessonsLinks) throws JSONException {

        String slugOrName = pickValueForField(type, jsonObject, fieldID);
        lessonLine.append(slugOrName);
        lessonsLinks.add(new Pair<>(pickTypeForField(type, fieldID), slugOrName));

    }

    private String pickValueForField(@NonNull TimetableType type, @NonNull JSONObject jsonObject, int fieldID) throws JSONException {

        return jsonObject.getString(type.getField(fieldID).first);
    }

    private TimetableType pickTypeForField(@NonNull TimetableType type, int fieldID) {

        return type.getField(fieldID).second;
    }
}
