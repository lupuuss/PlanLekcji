package ga.lupuss.planlekcji.managers.timetablemanager;

import android.support.v4.util.Pair;

public enum TimetableType {

    CLASS("classes/", "class", "Klasa", "slug", false, 0,
            new Pair<>("subject", -1),
            new Pair<>("classroom", 2),
            new Pair<>("teacherName", 1),
            new Pair<>("teacherSlug", 1)),

    TEACHER("teachers/", "teacher", "Nauczyciel", "slug", true, 1,
            new Pair<>("subject", -1),
            new Pair<>("class", 0),
            new Pair<>("classroom", 2)),

    CLASSROOM("classrooms/", "classroom", "Sala", "number", false, 2,
            new Pair<>("subject", -1),
            new Pair<>("class", 0),
            new Pair<>("teacherName", 1),
            new Pair<>("teacherSlug", 1));

    final private String apiPath;
    final private String offlinePath;
    final private String prefix;
    final private String slugName;
    final private Pair<String, Integer>[] fields;
    final private boolean nameAvailable;
    final private int id;

    @SafeVarargs
    TimetableType(String apiPath,
                  String offlinePath,
                  String prefix,
                  String slugName,
                  boolean name,
                  int id,
                  Pair<String, Integer> ... fields) {

        this.apiPath = apiPath;
        this.offlinePath = offlinePath;
        this.prefix = prefix;
        this.slugName = slugName;
        this.fields = fields;
        this.nameAvailable = name;
        this.id = id;
    }

    public String getApiPath() {
        return apiPath;
    }

    public String getOfflinePath() {
        return offlinePath;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSlugName() {

        return slugName;
    }

    public Pair<String, TimetableType> getField(int i){

        TimetableType type = null;

        if (fields[i].second != -1) {
            type = values()[fields[i].second];
        }

        return new Pair<>(fields[i].first, type);
    }

    public boolean isNameAvailable(){

        return nameAvailable;
    }

    public int getId() {
        return id;
    }
}
