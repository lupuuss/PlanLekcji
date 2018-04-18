package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import ga.lupuss.planlekcji.presenters.Notifiable;
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.ui.activities.MainView;
import ga.lupuss.planlekcji.ui.activities.ResourceProvider;
import ga.lupuss.planlekcji.ui.fragments.TimetableFragment;

public final class TimetablePresenter implements Notifiable {

    private ThreadControl control = new ThreadControl();
    private ControlledAsyncTask mainAsyncTask;
    private Runnable onResumeLoad;
    private TimetableManager timetableManager;

    private MainView mainView;
    private ResourceProvider resourceProvider;

    public TimetablePresenter(@NonNull MainActivity activity) {

        this.mainView = activity;
        resourceProvider = activity;
        this.timetableManager = new TimetableManager(
                resourceProvider.getSharedPreferences(),
                new BasicTimetableResponseProvider(),
                resourceProvider.getStringArray(R.array.headers),
                new BasicOfflineTimetableProvider()
        );
    }

    @Override
    public void onCreate() {
        timetableManager.prepareOfflineTimetables();
        timetableManager.loadDefaultTimetable();
        timetableManager.loadStats();
    }

    @Override
    public void onResume() {

        control.resume();

        if (onResumeLoad != null) {

            Log.d(TimetablePresenter.class.getName(), "Load timetable after pause...");

            new Handler().postDelayed(() -> {

                onResumeLoad.run();
                onResumeLoad = null;
            }, 100);
        }

    }

    @Override
    public void onPause(boolean isFinishing) {

        if (!isFinishing) {
            control.pause();
        }
    }

    @Override
    public void onDestroy(){

        if (mainAsyncTask != null) {

            mainAsyncTask.cancel(true);
        }
        control.cancel();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState, boolean isConfigurationChanging) {

        backUpState(savedInstanceState);

        if (mainAsyncTask != null && isConfigurationChanging){

            mainAsyncTask.cancel(true);
        }
    }

    private void backUpState(@NonNull Bundle savedInstanceState) {

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

            mainView.setExpandableListViewData(
                            timetableManager.getExpandableListChildren(true)
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

            mainView.executeOnUiThread(
                    () -> mainView.showSingleLongToast(e.getUserMessageId())
            );

        }
    }


    public void loadTimetableFromHref(@NonNull String listName, @NonNull TimetableType type) {

        runControlledAsyncTask(new TimetableLoaderFromHref(this, listName, type));
    }

    public void updateData() {

        if (mainView.isOnline()) {

            runControlledAsyncTask(new DataUpdater(this));

        } else {

            mainView.executeOnUiThread(
                    () -> {

                        mainView.showSingleLongToast(R.string.msg_no_internet);
                        mainView.getSwipeRefreshingLayout().setRefreshing(false);
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
        String msg;

        if (mainView.timetableFragmentExists()) {

            Fragment fragment =
                    mainView.getCurrentFragment();

            Pair<String, TimetableType> pair = new Pair<>(
                    ((TimetableFragment)fragment).getListName(),
                    ((TimetableFragment)fragment).getTimetableType()
            );

            if (timetableManager.isDefaultTimetableAvailable()
                    && pair.equals(timetableManager.getDefaultTimetable())){

                msg = String.format(
                        resourceProvider.getStringById(R.string.already_default),
                        pair.first
                );

            } else {

                timetableManager.setDefaultTimetable(pair.first, pair.second);

                msg = String.format(
                        resourceProvider.getStringById(R.string.now_is_default),
                        pair.first
                );
            }

        } else {

            msg = resourceProvider.getStringById(R.string.no_timetable_loaded);

        }

        mainView.executeOnUiThread(() ->
                mainView.showSingleLongToast(msg)
        );

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

    @NonNull ThreadControl getThreadControl() {

        return control;
    }

    @NonNull MainView getMainView() {

        return mainView;
    }

    @NonNull TimetableManager getTimetableManager() {

        return timetableManager;
    }
}
