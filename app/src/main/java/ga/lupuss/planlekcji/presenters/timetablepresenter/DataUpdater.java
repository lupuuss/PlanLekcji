package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import ga.lupuss.planlekcji.exceptions.UserMessageException;
import ga.lupuss.planlekcji.managers.timetablemanager.Timetable;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.ui.adapters.BasicExpandableListAdapter;
import ga.lupuss.planlekcji.ui.fragments.LoadingFragment;

@SuppressWarnings("FieldCanBeLocal")
class DataUpdater extends ControlledAsyncTask {

    private Timetable timetable;

    final private int OK = 2;
    final private int ONLY_LISTS = 1;
    final private int BAD = 0;
    private Integer message = null;

    DataUpdater(@NonNull TimetablePresenter controlPresenter) {

        super(controlPresenter);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mainView.lockSaveSwitch();

        boolean isOffline = false;

        if (timetableManager.isLastFocusedTimetableAvailable()) {

            Pair<String, TimetableType> pair = timetableManager.getLastFocusedTimetable();
            isOffline = timetableManager.isOfflineAvailable(pair.first, pair.second);
        }

        mainView.addLoadingFragmentAndKeepTimetableOnBackStack(isOffline,
                LoadingFragment.Owner.TIMETABLE);

        mainView.setModeIndicatorByInternetConnection();
    }

    @Override
    @NonNull
    protected Integer doInBackground(Void... voids) {

        Log.i(DataUpdater.class.getName(), "data update...");

        int status = -1;

        try {

            timetableManager.prepareOnlineLists(true, mainView.isOnline());

            Pair<String, TimetableType> timetablePair = timetableManager.pickAutoTimetable();

            status = ONLY_LISTS;

            timetable = timetableManager.getOnlineTimetable(
                    timetablePair.first,
                    timetablePair.second,
                    Principal.UPDATER,
                    mainView.isOnline()
            );

            status = OK;

        } catch (UserMessageException e) {

            e.printStackTrace();
            message = e.getUserMessageId();

            if (status != ONLY_LISTS) {

                status = BAD;
            }
        }

        pauseIfMainActivityPaused();
        return status;
    }

    @Override
    protected void onPostExecute(Integer integer) {

        if (integer == OK) {

            Log.i(DataUpdater.class.getName(), "> > > All fine");

            mainView.setExpandableListViewData(
                    timetableManager.getExpandableListChildren(false)
            );

            mainView.unlockSaveSwitch();
            mainView.addTimetableFragmentSmooth(timetable, true);
            mainView.setModeIndicator(MainActivity.IndicatorMode.NET);

        } else {

            if (integer == ONLY_LISTS) {

                Log.i(DataUpdater.class.getName(), "> > > Only lists fine");

                mainView.setExpandableListViewData(
                        timetableManager.getExpandableListChildren(false)
                );
            }

            Log.i(DataUpdater.class.getName(), "> > > failed");
            mainView.addFailTimetableLoadingFragment();
            mainView.showSingleLongToast(message);
            mainView.setModeIndicator(MainActivity.IndicatorMode.NO_NET);

        }

        super.onPostExecute(integer);
    }
}