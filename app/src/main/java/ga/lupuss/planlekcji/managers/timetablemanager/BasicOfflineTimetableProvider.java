package ga.lupuss.planlekcji.managers.timetablemanager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ga.lupuss.planlekcji.statics.Info;
import ga.lupuss.planlekcji.tools.Files;

public final class BasicOfflineTimetableProvider implements OfflineTimetableProvider {

    private final String OFFLINE_TIMETABLE_DIR = "timetables";
    private final File TIMETABLE_DIR =  new File(Info.APP_FILES_DIR, OFFLINE_TIMETABLE_DIR);
    private final String HOURS_FILE_NAME = "hours.json";

    private Map<TimetableType, Map<String, OfflineTimetable>> timetables = new HashMap<>();
    private List<Pair<String, String>> hours = null;
    private List<String> classrooms = null;
    private List<String> classes = null;
    private Map<String, String> teachers = null;
    private boolean anyTimetable = false;
    private int timetablesCounter = 0;

    public BasicOfflineTimetableProvider() {

        fillTimetableMapWithTypes();
    }

    private void fillTimetableMapWithTypes(){

        for(TimetableType type : TimetableType.values()){

            timetables.put(type, new LinkedHashMap<>());
        }
    }

    // prepare offline data

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public boolean prepareOfflineData() {

        if (!TIMETABLE_DIR.exists()){
            TIMETABLE_DIR.mkdirs();
        }

        List<File> timetablesDirs = Arrays.asList(TIMETABLE_DIR.listFiles());

        if(timetablesDirs.isEmpty()) {

            Log.d(BasicOfflineTimetableProvider.class.getName(), "Main dir empty");

            return false;

        } else {


            try {

                hours = ListParser.parseHoursList(new JSONArray(loadHoursJson()));

            } catch (JSONException e) {
                e.printStackTrace();
            }


            loadAllTimetables(timetablesDirs);

            boolean any = !timetables.get(TimetableType.CLASS).isEmpty()
                    || !timetables.get(TimetableType.TEACHER).isEmpty()
                    || !timetables.get(TimetableType.CLASSROOM).isEmpty();

            if (any) {

                buildLists();
            }

            anyTimetable = any;

            return any;
        }

    }

    @Override
    public void refreshLists() {

        classes = new ArrayList<>(timetables.get(TimetableType.CLASS).keySet());
        refreshTeachers();
        classrooms = new ArrayList<>(timetables.get(TimetableType.CLASSROOM).keySet());

    }

    @SuppressWarnings("Convert2streamapi")
    private void refreshTeachers() {

        List<String> toDelete = new ArrayList<>();

        for (String slug : getTeachers().keySet()) {

            if (!timetables.get(TimetableType.TEACHER).containsKey(slug)) {
                toDelete.add(slug);
            }
        }

        for (String slug : toDelete) {

            teachers.remove(slug);
        }

    }

    private void loadAllTimetables(@NonNull List<File> dirs){

        Log.d(TimetableManager.class.getName(), "Dirs exists");

        for (File dir : dirs) {

            try {

                TimetableType type = pickType(dir);

                for (File timetable : dir.listFiles()) {

                    Log.d(BasicOfflineTimetableProvider.class.getName(), "- Timetable file > " + timetable.toString());

                    JSONObject json = loadTimetableFromInternal(timetable);

                    String slug = readSlug(type, json);

                    timetablesCounter++;
                    addToList(slug, type, new OfflineTimetable(slug, json, timetable));
                }

            } catch (IllegalArgumentException | JSONException | IOException e){

                e.printStackTrace();
            }
        }
    }

    @NonNull
    private String readSlug(@NonNull TimetableType type,
                                     @NonNull JSONObject json) throws JSONException {

        return json.getString(type.getSlugName());
    }

    @NonNull
    private TimetableType pickType(@NonNull File dir) throws IllegalArgumentException {

        if (dir.getName().equals(TimetableType.CLASS.getOfflinePath())) {

            return TimetableType.CLASS;

        } else if (dir.getName().equals(TimetableType.TEACHER.getOfflinePath())) {

            return TimetableType.TEACHER;

        } else if (dir.getName().equals(TimetableType.CLASSROOM.getOfflinePath())) {

            return  TimetableType.CLASSROOM;
        } else {

            throw new IllegalArgumentException("No matching type for dir: " + dir.toString());
        }
    }

    @NonNull
    private JSONObject loadTimetableFromInternal(@NonNull File file) throws JSONException, IOException {

        return new JSONObject(new String(Files.readAllBytes(file), "UTF-8"));
    }

    private void buildLists() {

        classes = new ArrayList<>(timetables.get(TimetableType.CLASS).keySet());
        classrooms = new ArrayList<>(timetables.get(TimetableType.CLASSROOM).keySet());
        teachers = new HashMap<>();

        for (Map.Entry<String,OfflineTimetable> timetable :
                timetables.get(TimetableType.TEACHER).entrySet()) {

            teachers.put(timetable.getKey(), readTeacherName(timetable.getValue().getJsonData()));
        }
    }

    @Nullable
    private String readTeacherName(@NonNull JSONObject json) {

        if (json.isNull("name")) {

            return null;
        } else {

            try {

                return json.getString("name");

            } catch (JSONException e) {

                e.printStackTrace();

                return null;
            }
        }

    }


    // public manipulation methods

    @Override
    public boolean containsTimetable(@NonNull String slug, @NonNull TimetableType type) {

        return timetables.get(type).containsKey(slug);
    }

    @Override
    public OfflineTimetable getTimetable(@NonNull String slug, @NonNull TimetableType type) {

        return timetables.get(type).get(slug);
    }

    @Override
    public boolean keepTimetableOffline(@NonNull String slug,
                                        @NonNull TimetableType type,
                                        @NonNull String json) {

        try {

            OfflineTimetable timetable;

            if (timetables.get(type).containsKey(slug)) {

                timetable = timetables.get(type).get(slug);

            } else {

                File file = generateFileForOfflineTimetable(slug, type);
                timetable =  new OfflineTimetable(slug, new JSONObject(json), file);
            }

            timetable.saveToInternal();

            addToList(slug, type, timetable);

            Log.d(
                    BasicOfflineTimetableProvider.class.getName(),
                    "Timetable (slug: " + slug + " type: " + type.name()
                            + ") saved to" + timetable.getFile().toString()
            );

            return true;

        } catch (JSONException | IOException e) {

            e.printStackTrace();

            return false;
        }
    }

    @Override
    public void updateOfflineTimetable(@NonNull String slug,
                                       @NonNull TimetableType type,
                                       @NonNull JSONObject json) {

        OfflineTimetable timetable = getTimetable(slug, type);

        if (timetable != null) {

            timetable.update(json);
        }
    }

    @NonNull
    private File generateFileForOfflineTimetable(@NonNull String slug, @NonNull TimetableType type) {

        File typeDir = new File(TIMETABLE_DIR, type.getOfflinePath());

        return new File(typeDir, slug + "_" + (++timetablesCounter));
    }

    private void addToList(@NonNull String slug,
                           @NonNull TimetableType type,
                           @NonNull OfflineTimetable timetable) {

        timetables.get(type).put(slug, timetable);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void deleteTimetable(@NonNull String slug, @NonNull TimetableType type) {

        OfflineTimetable timetable = getTimetable(slug, type);

        if (timetable != null) {

            timetable.deleteFromInternal();
        }
        timetables.get(type).remove(slug);
    }

    @Override
    public boolean saveHours(@NonNull String json)  {

        try {

            Files.writeAllBytes(new File(Info.APP_FILES_DIR , HOURS_FILE_NAME),json.getBytes());
            return true;

        } catch (IOException e) {

            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Nullable
    public String loadHoursJson() {

        try {
            return new String(
                    Files.readAllBytes(new File(Info.APP_FILES_DIR , HOURS_FILE_NAME))
            );

        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }

    }

    // offline lists getters

    @Override
    @NonNull
    public List<Pair<String, String>> getHours() {

        return hours;
    }

    @Override
    @NonNull
    public List<String> getClassrooms() {

        return classrooms;
    }

    @Override
    @NonNull
    public List<String> getClasses() {

        return classes;
    }

    @Override
    @NonNull
    public Map<String, String> getTeachers() {

        return teachers;
    }

    @Override
    public boolean isAnyTimetable() {

        return anyTimetable;
    }
}
