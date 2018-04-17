package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.List;
import java.util.Map;

import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.exceptions.UserMessageException;
import ga.lupuss.planlekcji.managers.timetablemanager.BasicOfflineTimetableProvider;
import ga.lupuss.planlekcji.managers.timetablemanager.BasicTimetableResponseProvider;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableManager;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableStats;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.ui.adapters.BasicExpandableListAdapter;
import ga.lupuss.planlekcji.ui.fragments.TimetableFragment;

public final class TimetablePresenter {

    private ThreadControl control = new ThreadControl();
    private ControlledAsyncTask mainAsyncTask;
    private Runnable onResumeLoad;
    private TimetableManager timetableManager;

    private MainActivity mainActivity;

    public TimetablePresenter(@NonNull MainActivity activity) {

        this.mainActivity = activity;
        this.timetableManager = new TimetableManager(
                mainActivity.getApplicationContext(),
                new BasicTimetableResponseProvider(),
                new BasicOfflineTimetableProvider()
        );
    }

    public void loadData() {

        timetableManager.prepareOfflineTimetables();
        timetableManager.loadDefaultTimetable();
        timetableManager.loadStats();
    }

    public void backUpState(@NonNull Bundle savedInstanceState) {

        timetableManager.backUpLists(savedInstanceState);
        timetableManager.backUpLastFocusedTimetable(savedInstanceState);
    }

    public boolean restoreState(@NonNull Bundle savedInstanceState) {

        boolean restore = restoreExpandableListView(savedInstanceState);
        timetableManager.restoreLastFocusedTimetable(savedInstanceState);

        return restore;
    }

    private boolean restoreExpandableListView(@NonNull Bundle savedInstanceState) {

        timetableManager.restoreLists(savedInstanceState);

        if (timetableManager.areListsOK()) {

            mainActivity.setExpandableListViewAdapter(
                    new BasicExpandableListAdapter(
                            mainActivity,
                            timetableManager.getExpandableListHeaders(),
                            timetableManager.getExpandableListChildren(true)
                    )
            );

            Log.d(MainActivity.class.getName(), "LISTS RESTORED");

            return true;
        }

        return false;

    }

    private void runControlledAsyncTask(@NonNull ControlledAsyncTask task) {

        if (mainAsyncTask != null && mainAsyncTask.isRunning()) {

            mainAsyncTask.cancel(true);
        }

        mainAsyncTask = task;
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void appInit(@NonNull LoadMode mode) {

        runControlledAsyncTask(
                new AppInitializer(this, mode)
        );

    }

    public void loadTimetable(@NonNull String listName,
                              @NonNull TimetableType type,
                              @NonNull LoadMode mode,
                              @NonNull Principal principal) {

        runControlledAsyncTask(new BasicTimetableLoader(this, listName, type, mode, principal));
    }

    public void loadAutoTimetable(@NonNull LoadMode mode) {

        try {

            Pair<String, TimetableType> autoTimetable = timetableManager.pickAutoTimetable();

            loadTimetable(
                    autoTimetable.first,
                    autoTimetable.second,
                    mode,
                    Principal.APP
            );

        } catch (UserMessageException e) {

            mainActivity.runOnUiThread(
                    () -> mainActivity.showSingleLongToast(e.getUserMessage())
            );

        }
    }


    public void loadTimetableFromHref(@NonNull String listName, @NonNull TimetableType type) {

        runControlledAsyncTask(new TimetableLoaderFromHref(this, listName, type));
    }

    public void updateData() {

        if (mainActivity.isOnline()) {

            runControlledAsyncTask(new DataUpdater(this));

        } else {

            mainActivity.runOnUiThread(
                    () -> {

                        mainActivity.showSingleLongToastByStringId(R.string.msg_no_internet);
                        mainActivity.swipeRefreshLayout.setRefreshing(false);
                    }
            );

        }
    }

    public void keepTimetableOffline() {

        new OfflineTimetableSaver(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void deleteOfflineTimetable() {

        new OfflineTimetableDeleter(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void setCurrentAsDefaultOnClick() {

        Fragment fragment =
                mainActivity.getMyFragmentManager().findFragmentById(R.id.fragment_container);
        String msg;

        if (fragment instanceof TimetableFragment) {

            Pair<String, TimetableType> pair = new Pair<>(
                    ((TimetableFragment)fragment).getListName(),
                    ((TimetableFragment)fragment).getTimetableType()
            );

            if (timetableManager.isDefaultTimetableAvailable()
                    && pair.equals(timetableManager.getDefaultTimetable())){

                msg = String.format(mainActivity.getString(R.string.already_default), pair.first);

            } else {

                timetableManager.setDefaultTimetable(pair.first, pair.second);

                msg = String.format(mainActivity.getString(R.string.now_is_default), pair.first);
            }

        } else {

            msg = mainActivity.getString(R.string.no_timetable_loaded);

        }

        mainActivity.runOnUiThread(() ->
                mainActivity.showSingleLongToast(msg)
        );

    }

    // main task control

    public void resumeThreadControl() {

        control.resume();
    }

    public void pauseThreadControl() {

        control.pause();
    }

    public boolean isMainTaskNull() {

        return mainAsyncTask == null;
    }

    public void cancelMainTask() {

        mainAsyncTask.cancel(true);
    }

    public void cancelThreadControl() {

        control.cancel();
    }

    public boolean isOnResumeLoadAvailable() {

        return onResumeLoad != null;
    }

    public void pauseCurrentControlledAsyncTask() {

        if (mainAsyncTask != null && mainAsyncTask.isRunning()) {
            mainAsyncTask.cancel(true);

            mainAsyncTask = null;
        }
    }

    public void setOnResumeLoad(@Nullable Runnable onResumeLoad) {

        this.onResumeLoad = onResumeLoad;
    }

    public void setOnResumeLoadNull() {

        onResumeLoad = null;
    }

    public void setOnStatsLeaderChanged(@Nullable Runnable runnable) {

        timetableManager.setOnStatsLeaderChanged(runnable);
    }

    public Pair<String, TimetableType> getLastFocusedTimetable(){

        return timetableManager.getLastFocusedTimetable();
    }

    public List<Pair<String, String>> getHoursList() {

        return timetableManager.getHours();
    }

    public Map<String, String> getTeachersMap() {

        return timetableManager.getTeachers();
    }

    public TimetableStats.MostVisitedTimetable getMostVisitedTimetable() {

        return timetableManager.getMostVisitedTimetable();
    }

    public void setLastFocusedTimetable(Pair<String, TimetableType> pair) {

        timetableManager.setLastFocusedTimetable(pair);
    }

    public boolean isOfflineTimetableAvailable(String listName, TimetableType type) {

        return timetableManager.isOfflineAvailable(listName, type);
    }

    public boolean isMostVisitedTimetableAvailable() {

        return timetableManager.isMostVisitedTimetableAvailable();
    }

    public Runnable getOnResumeLoad() {

        return onResumeLoad;
    }

    @NonNull ThreadControl getThreadControl() {

        return control;
    }

    @NonNull MainActivity getMainActivity() {

        return mainActivity;
    }

    @NonNull TimetableManager getTimetableManager() {

        return timetableManager;
    }
}
