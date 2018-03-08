package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;

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

    public TimetablePresenter(MainActivity activity) {

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

    public void backUpState(Bundle savedInstanceState) {

        timetableManager.backUpLists(savedInstanceState);
        timetableManager.backUpLastFocusedTimetable(savedInstanceState);
    }

    public boolean restoreState(Bundle savedInstanceState) {

        boolean restore = restoreExpandableListView(savedInstanceState);
        timetableManager.restoreLastFocusedTimetable(savedInstanceState);

        return restore;
    }

    private boolean restoreExpandableListView(Bundle savedInstanceState) {

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

    private void runControlledAsyncTask(ControlledAsyncTask task) {

        if (mainAsyncTask != null && mainAsyncTask.isRunning()) {

            mainAsyncTask.cancel(true);
        }

        mainAsyncTask = task;
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void appInit(LoadMode mode) {

        runControlledAsyncTask(
                new AppInitializer(this, mode)
        );

    }

    public void loadTimetable(String listName, TimetableType type,
                              LoadMode mode, Principal principal) {

        runControlledAsyncTask(new BasicTimetableLoader(this, listName, type, mode, principal));
    }

    public void loadAutoTimetable(LoadMode mode) {

        try {

            Pair<String, TimetableType> autoTimetable = timetableManager.pickAutoTimetable();

            if (autoTimetable != null) {

                loadTimetable(
                        autoTimetable.first,
                        autoTimetable.second,
                        mode,
                        Principal.APP
                );
            }
        } catch (UserMessageException e) {

            mainActivity.runOnUiThread(
                    () -> Toast.makeText(mainActivity, e.getUserMessage(), Toast.LENGTH_LONG).show()
            );

        }
    }


    public void loadTimetableFromHref(String listName, TimetableType type) {

        runControlledAsyncTask(new TimetableLoaderFromHref(this, listName, type));
    }

    public void updateData() {

        if (mainActivity.isOnline()) {

            runControlledAsyncTask(new DataUpdater(this));

        } else {

            mainActivity.runOnUiThread(
                    () -> {

                        Toast.makeText(
                                mainActivity,
                                mainActivity.getString(R.string.msg_no_internet),
                                Toast.LENGTH_LONG
                        ).show();

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

        mainActivity.runOnUiThread(() -> Toast.makeText(
                mainActivity, msg, Toast.LENGTH_LONG).show()
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

    public void setOnResumeLoad(Runnable onResumeLoad) {

        this.onResumeLoad = onResumeLoad;
    }

    public void setOnResumeLoadNull() {

        onResumeLoad = null;
    }

    public void setOnStatsLeaderChanged(Runnable runnable) {

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

    ThreadControl getThreadControl() {

        return control;
    }

    MainActivity getMainActivity() {

        return mainActivity;
    }

    TimetableManager getTimetableManager() {

        return timetableManager;
    }
}
