package ga.lupuss.planlekcji.managers.timetablemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ga.lupuss.planlekcji.exceptions.UserInterruptedException;
import ga.lupuss.planlekcji.presenters.timetablepresenter.Principal;
import ga.lupuss.planlekcji.statics.Bundles;
import ga.lupuss.planlekcji.statics.Preferences;
import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.exceptions.JsonParserException;
import ga.lupuss.planlekcji.exceptions.NoInternetException;
import ga.lupuss.planlekcji.exceptions.SomethingGoesWrongException;
import ga.lupuss.planlekcji.exceptions.UserMessageException;
import ga.lupuss.planlekcji.tools.OldConfig;
import ga.lupuss.planlekcji.tools.ResponseUtil;
import ga.lupuss.planlekcji.tools.Utils;
import ga.lupuss.simplehttp.Response;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Manage all timetables and timetable's lists. Provides methods to download and parse them.
 * Manager keeps some downloaded data to avoid unnecessary internet connection usage.
 * However, manager provides methods to force "fresh" download.
 */

public final class TimetableManager {

    private List<Pair<String, String>> hours = null;
    private List<String> classrooms = null;
    private List<String> classes = null;
    private Map<String, String> teachers = null;

    private final List<String> headersList;

    private Context appContext;
    private TimetableResponseProvider responseProvider;
    private OfflineTimetableProvider offlineTimetablesProvider;

    private Pair<String, TimetableType> lastFocusedTimetable;
    private Pair<String, TimetableType> defaultTimetable;

    private SharedPreferences preferences;
    private TimetableStats stats = new TimetableStats();


    public TimetableManager(Context appContext,
                            TimetableResponseProvider responseProvider,
                            OfflineTimetableProvider offlineTimetableProvider) {

        this.appContext = appContext;
        this.responseProvider = responseProvider;
        this.offlineTimetablesProvider = offlineTimetableProvider;
        this.preferences = getDefaultSharedPreferences(appContext);
        headersList = Arrays.asList(appContext.getResources().getStringArray(R.array.headers));
    }

    // LISTS GETTERS

    public List<Pair<String, String>> getHours() {

        return hours;
    }

    @SuppressWarnings("WeakerAccess")
    public List<String> getClassrooms() {

        return classrooms;
    }

    @SuppressWarnings("WeakerAccess")
    public List<String> getClasses() {

        return classes;
    }

    public Map<String, String> getTeachers() {

        return teachers;
    }

    // FOR EXPANDABLE LIST VIEW LISTS

    public List<String> getExpandableListHeaders() {

        return headersList;
    }

    public Map<String, List<String>> getExpandableListChildren(boolean needSort) {

        assert classes != null;
        assert teachers != null;
        assert classrooms != null;

        Map<String, List<String>> map = new HashMap<>();

        if (needSort) {
            Collections.sort(classes, String::compareTo);
            Collections.sort(classrooms, String::compareTo);
        }

        map.put(headersList.get(0), addDashIfEmpty(getClasses()));

        map.put(headersList.get(2), addDashIfEmpty(getClassrooms()));

        List<String> teachers = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> onlySlugs = new ArrayList<>();

        for (Map.Entry<String, String> entry : getTeachers().entrySet()) {

            if (entry.getValue() != null) {

                names.add(entry.getValue());

            } else {

                onlySlugs.add(entry.getKey());
            }

        }

        if (needSort) {

            Utils.teachersNamesSort(names);
            Collections.sort(onlySlugs, String::compareTo);
        }

        teachers.addAll(names);
        teachers.addAll(onlySlugs);

        map.put(headersList.get(1), addDashIfEmpty(teachers));

        return map;
    }

    private List<String> addDashIfEmpty(List<String> list) {

        if (list.isEmpty()) {

            return Collections.singletonList("-");

        } else {

            return list;
        }
    }

    // TIMETABLE

    /**
     * Tries to get timetable in any possible way.
     * @param listName Name from the list of timetables
     * @param type Type of timetable
     * @return All lessons views for all days
     * @throws UserMessageException when timetable can't be reached
     */
    public Timetable getTimetable(String listName,
                                  TimetableType type,
                                  Principal principal,
                                  boolean internetStatus) throws UserMessageException {

        setLastFocusedTimetable(new Pair<>(listName, type));

        Timetable timetable;

        try {

            timetable = getOnlineTimetable(listName, type, principal, internetStatus);

        } catch (UserInterruptedException e) {

            throw e;

        } catch (UserMessageException e){

            try {

                timetable = getOfflineTimetable(listName, type, principal);

            } catch (UserMessageException ignored){

                throw e;
            }
        }
        return timetable;
    }

    public Timetable getOfflineTimetable(String listName,
                                         TimetableType type,
                                         Principal principal) throws UserMessageException {

        setLastFocusedTimetable(new Pair<>(listName, type));

        String slug = getSlugForListName(listName, type);

        if (offlineTimetablesProvider.containsTimetable(slug, type)) {

            stats.update(slug, type, principal.getStatValue());
            return  offlineTimetablesProvider.getTimetable(slug, type);
        }

        throw new SomethingGoesWrongException(
                appContext.getString(R.string.msg_something_goes_wrong)
        );
    }

    public Timetable getOnlineTimetable(String listName,
                                        TimetableType type,
                                        Principal principal,
                                        boolean internetStatus) throws UserMessageException {

        setLastFocusedTimetable(new Pair<>(listName, type));

        String slug = getSlugForListName(listName, type);

        if (!internetStatus) {

            throw new NoInternetException(appContext.getString(R.string.msg_no_internet));
        }

        JSONObject json =
                ResponseUtil.fetchResponseToJsonObject(
                        appContext,
                        responseProvider.getTimetable(slug, type)
                );

        if (offlineTimetablesProvider.containsTimetable(slug, type)) {

            offlineTimetablesProvider.updateOfflineTimetable(slug, type, json);
        }

        stats.update(slug, type, principal.getStatValue());

        return new Timetable(
                slug,
                json,
                offlineTimetablesProvider.containsTimetable(slug, type),
                false
        );
    }

    public Pair<String, TimetableType> pickAutoTimetable() throws UserMessageException {

        if(isLastFocusedTimetableAvailable()) {

            return getLastFocusedTimetable();

        } else if(isDefaultTimetableAvailable()) {

            return getDefaultTimetable();

        } else {

            return getFirstTimetableFromLists();

        }
    }

    @SuppressWarnings("LoopStatementThatDoesntLoop")
    private Pair<String, TimetableType> getFirstTimetableFromLists() throws UserMessageException {

        for (String str : classes) {
            return new Pair<>(str, TimetableType.CLASS);
        }

        for (Map.Entry<String, String> entry : teachers.entrySet()) {
            return new Pair<>(
                    entry.getValue() != null ? entry.getValue() : entry.getKey(),
                    TimetableType.TEACHER
            );
        }

        for (String str : classrooms) {
            return new Pair<>(str, TimetableType.CLASSROOM);
        }

        throw new SomethingGoesWrongException(appContext.getString(R.string.msg_no_internet));
    }

    public Pair<String, TimetableType> getLastFocusedTimetable() {

        return lastFocusedTimetable;
    }

    public boolean isLastFocusedTimetableAvailable() {

        return lastFocusedTimetable != null;
    }

    public void setLastFocusedTimetable(Pair<String, TimetableType> lastFocusedTimetable) {

        this.lastFocusedTimetable = lastFocusedTimetable;
    }

    public void backUpLastFocusedTimetable(@NonNull Bundle savedInstanceState) {

        if (getLastFocusedTimetable() != null) {

            savedInstanceState.putString(Bundles.LAST_TRIED_TIMETABLE,
                    getLastFocusedTimetable().first);
            savedInstanceState.putString(Bundles.LAST_TRIED_TIMETABLE_TYPE,
                    getLastFocusedTimetable().second.name());

        }
    }

    public void restoreLastFocusedTimetable(@NonNull Bundle savedInstanceState) {

        String listName = savedInstanceState.getString(Bundles.LAST_TRIED_TIMETABLE);
        String strType = savedInstanceState.getString(Bundles.LAST_TRIED_TIMETABLE_TYPE);

        if (listName != null && strType != null) {

            lastFocusedTimetable = new Pair<>(listName, TimetableType.valueOf(strType));
        }
    }

    public boolean isAnyOfflineTimetable(){

        return offlineTimetablesProvider.isAnyTimetable();
    }

    public boolean isOfflineAvailable(@NonNull String listName, @NonNull TimetableType type) {

        String slug = getSlugForListName(listName, type);
        return offlineTimetablesProvider.containsTimetable(slug, type);
    }

    private @NonNull String getSlugForListName(@NonNull String listName,
                                               @NonNull TimetableType type){

        if (!type.isNameAvailable()){

            return listName;

        } else {

            if (teachers.containsValue(listName)) {

                for (Map.Entry<String, String> entry : teachers.entrySet()) {

                    if (entry.getValue() != null && entry.getValue().equals(listName)) {

                        return entry.getKey();
                    }
                }
            } else {

                return listName;
            }
        }

        return listName;
    }


    // DEFAULT TIMETABLE

    public Pair<String, TimetableType> getDefaultTimetable() {
        return defaultTimetable;
    }

    public boolean isDefaultTimetableAvailable() {

        return defaultTimetable != null;
    }

    public void setDefaultTimetable(String listName, TimetableType type) {

        defaultTimetable = new Pair<>(listName, type);

        preferences.edit()
                .putString(Preferences.DEFAULT_TIMETABLE_TYPE, type.name())
                .putString(Preferences.DEFAULT_TIMETABLE_LIST_NAME, listName)
                .apply();
    }

    public void loadDefaultTimetable() {

        String listName = preferences
                .getString(Preferences.DEFAULT_TIMETABLE_LIST_NAME, null);

        String typeStr = preferences
                .getString(Preferences.DEFAULT_TIMETABLE_TYPE, null);

        if (listName != null && typeStr != null) {

            Log.d(TimetableManager.class.getName(),
                    "Default timetable from SharedPreferences loaded");

            defaultTimetable = new Pair<>(listName, TimetableType.valueOf(typeStr));

        } else {

            try {

                defaultTimetable = OldConfig.readDefaultTimetable();

                if (defaultTimetable != null) {

                    Log.d(TimetableManager.class.getName(),
                            "Default timetable from old cofig loaded");

                    setDefaultTimetable(defaultTimetable.first, defaultTimetable.second);
                }

            } catch (Exception e){

                Log.d(TimetableManager.class.getName(), "\"default.txt\" can't be processed");
                e.printStackTrace();
            }
        }
    }

    // LISTS LOADING

    /**
     * Returns true if lists are downloaded in that call.
     * @return true if lists are downloaded in that call
     * @throws UserMessageException when list cannot be loaded
     */
    public boolean prepareOnlineLists(boolean forceFresh, boolean internetStatus) throws UserMessageException {

        try {

            if (areListsOK() && !forceFresh) {

                return false;

            } else {

                if (!internetStatus) {

                    throw new NoInternetException(appContext.getString(R.string.msg_no_internet));
                }

                List<Response> responses = responseProvider.getAllLists();

                // responses are sorted by url: classes, classrooms, hours, teachers

                offlineTimetablesProvider
                        .saveHours(
                                ResponseUtil.fetchResponseToJsonArray(
                                        appContext, responses.get(2)
                                ).toString()
                        );

                parseResponsesToLists(responses);

                verifyOfflineData();

                verifyStats();

                return true;
            }

        } catch (JSONException e){

            throw new JsonParserException(appContext.getString(R.string.msg_json_error));
        }

    }

    private void verifyStats() {

        Log.d(TimetableManager.class.getName(), "Verifying stats...");

        verifyStatsFor(
                getClasses(),
                new ArrayList<>(stats.getAllStats().get(TimetableType.CLASS).keySet()),
                TimetableType.CLASS
        );

        verifyStatsFor(
                new ArrayList<>(getTeachers().keySet()),
                new ArrayList<>(stats.getAllStats().get(TimetableType.TEACHER).keySet()),
                TimetableType.TEACHER
        );

        verifyStatsFor(
                getClassrooms(),
                new ArrayList<>(stats.getAllStats().get(TimetableType.CLASSROOM).keySet()),
                TimetableType.CLASSROOM
        );

        stats.refreshLeader();
    }

    @SuppressWarnings("Convert2streamapi")
    private void verifyStatsFor(List<String> online, List<String> offline, TimetableType type) {

        List<String> toDelete = new ArrayList<>();

        for (String slug : offline) {

            if (!online.contains(slug)) {

                toDelete.add(slug);
            }
        }

        for(String slug : toDelete) {

            stats.deleteStat(slug, type);
            Log.d(TimetableManager.class.getName(),
                    "> > > Deleting: Slug: " + slug + " Type: " + type);
        }
    }

    @SuppressWarnings("Convert2streamapi")
    private void verifyOfflineData() {

        if (offlineTimetablesProvider.isAnyTimetable()) {

            Log.d(TimetableManager.class.getName(), "Verifying offline data...");

            verifyOfflineDataFor(getClasses(), offlineTimetablesProvider.getClasses(), TimetableType.CLASS);
            verifyOfflineDataFor(
                    new ArrayList<>(getTeachers().keySet()),
                    new ArrayList<>(offlineTimetablesProvider.getTeachers().keySet()),
                    TimetableType.TEACHER
            );
            verifyOfflineDataFor(getClassrooms(), offlineTimetablesProvider.getClassrooms(), TimetableType.CLASSROOM);

            offlineTimetablesProvider.refreshLists();

        } else {
            Log.d(TimetableManager.class.getName(), "Verifying revoked - nothing to verifyOfflineDataFor");
        }

    }

    @SuppressWarnings("Convert2streamapi")
    private void verifyOfflineDataFor(List<String> online, List<String> offline, TimetableType type) {

        for (String offlineSlug : offline) {

            if (!online.contains(offlineSlug)) {

                Log.d(TimetableManager.class.getName(), " > > > Deleting:  " + offlineSlug + " " + type);
                offlineTimetablesProvider.deleteTimetable(offlineSlug, type);
            }
        }

    }

    public boolean areListsOK(){

        return hours != null
                && classes != null
                && teachers != null
                && classrooms != null;
    }

    private void parseResponsesToLists(List<Response> responses) throws UserMessageException, JSONException {


        List<String> classesList =
                ListParser.parseTimetableList(
                        ResponseUtil.fetchResponseToJsonArray(appContext, responses.get(0)),
                        TimetableType.CLASS
                );

        List<String> classroomsList =
                ListParser.parseTimetableList(
                        ResponseUtil.fetchResponseToJsonArray(appContext, responses.get(1)),
                        TimetableType.CLASSROOM
                );

        List<Pair<String, String>> hoursList =
                ListParser.parseHoursList(ResponseUtil.fetchResponseToJsonArray(appContext, responses.get(2)));

        Map<String, String> teachersList =
                ListParser.parseTimetableListWithNames(
                        ResponseUtil.fetchResponseToJsonArray(appContext, responses.get(3))
                );

        // no exceptions so all can be public

        classes = classesList;
        classrooms = classroomsList;
        teachers = teachersList;
        hours = hoursList;

    }

    // LISTS BACKUP

    public void backUpLists(Bundle savedInstanceState) {

        if (areListsOK()) {

            savedInstanceState.putStringArrayList(Bundles.CLASSES, new ArrayList<>(classes));
            savedInstanceState.putStringArrayList(Bundles.CLASSROOMS, new ArrayList<>(classrooms));
            savedInstanceState
                    .putStringArrayList(Bundles.TEACHERS_KEYS, new ArrayList<>(teachers.keySet()));
            savedInstanceState
                    .putStringArrayList(Bundles.TEACHERS_VALUES, new ArrayList<>(teachers.values()));

            ArrayList<String> hoursStart = new ArrayList<>();
            ArrayList<String> hoursEnd = new ArrayList<>();

            for (Pair<String, String> pair : hours) {

                hoursStart.add(pair.first);
                hoursEnd.add(pair.second);
            }

            savedInstanceState.putStringArrayList(Bundles.HOURS_START, hoursStart);
            savedInstanceState.putStringArrayList(Bundles.HOURS_END, hoursEnd);

        }

    }

    public void restoreLists(Bundle savedInstanceState) {

        List<String> clsList = savedInstanceState.getStringArrayList(Bundles.CLASSES);
        List<String> clsrmsList = savedInstanceState.getStringArrayList(Bundles.CLASSROOMS);

        List<String> teachersKeys = savedInstanceState.getStringArrayList(Bundles.TEACHERS_KEYS);
        List<String> teachersValues = savedInstanceState.getStringArrayList(Bundles.TEACHERS_VALUES);

        Map<String, String> teachersMap = null;

        if (teachersKeys != null && teachersValues != null
                && teachersKeys.size() == teachersValues.size()) {


            teachersMap = new LinkedHashMap<>();

            for (int i = 0; i < teachersKeys.size(); i++) {

                teachersMap.put(teachersKeys.get(i), teachersValues.get(i));

            }
        }

        List<String> hoursStart = savedInstanceState.getStringArrayList(Bundles.HOURS_START);
        List<String> hoursEnd = savedInstanceState.getStringArrayList(Bundles.HOURS_END);
        List<Pair<String,String>> hoursList = null;

        if (hoursStart != null && hoursEnd != null && hoursStart.size() == hoursEnd.size()) {

            hoursList = new ArrayList<>();

            for (int i = 0; i < hoursStart.size(); i++) {

                hoursList.add(new Pair<>(hoursStart.get(i), hoursEnd.get(i)));
            }
        }

        if (hoursList != null && teachersMap != null && clsList != null && clsrmsList != null) {

            hours = hoursList;
            teachers = teachersMap;
            classes = clsList;
            classrooms = clsrmsList;
        }

    }

    // OFFLINE DATA

    /**
     * Prepares all timetables to be used by app.
     * @return true if any timetable loaded
     */
    public boolean prepareOfflineTimetables(){

        return offlineTimetablesProvider.prepareOfflineData();
    }

    public boolean prepareOfflineLists() {

        if (offlineTimetablesProvider.isAnyTimetable()) {

            classes = offlineTimetablesProvider.getClasses();
            teachers = offlineTimetablesProvider.getTeachers();
            classrooms = offlineTimetablesProvider.getClassrooms();
            hours = offlineTimetablesProvider.getHours();

            return true;

        } else {

            return false;

        }
    }

    public boolean keepTimetableOffline(String listName, TimetableType type, String json) {

        return offlineTimetablesProvider
                .keepTimetableOffline(getSlugForListName(listName, type), type, json);
    }

    public void deleteOfflineTimetable(String listName, TimetableType type) {

        offlineTimetablesProvider.deleteTimetable(getSlugForListName(listName, type), type);
    }

    // STATS

    public void loadStats() {

        stats.load();
    }

    public void setOnStatsLeaderChanged(Runnable runnable) {

        stats.setOnLeaderChangedListener(runnable);
    }

    public TimetableStats.MostVisitedTimetable getMostVisitedTimetable() {

        return stats.getMostVisitedTimetable();
    }

    public boolean isMostVisitedTimetableAvailable() {

        return stats.getMostVisitedTimetable() != null;
    }
}
