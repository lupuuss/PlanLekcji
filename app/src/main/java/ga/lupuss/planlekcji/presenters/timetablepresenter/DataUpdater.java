package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;

import ga.lupuss.planlekcji.exceptions.UserMessageException;
import ga.lupuss.planlekcji.managers.timetablemanager.Timetable;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.adapters.BasicExpandableListAdapter;
import ga.lupuss.planlekcji.ui.fragments.LoadingFragment;

@SuppressWarnings("FieldCanBeLocal")
class DataUpdater extends ControlledAsyncTask {

    private Timetable timetable;

    final private int OK = 2;
    final private int ONLY_LISTS = 1;
    final private int BAD = 0;
    private String message;

    DataUpdater(TimetablePresenter controlPresenter) {

        super(controlPresenter);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mainActivity.lockSaveSwitch();

        boolean isOffline = false;

        if (timetableManager.isLastFocusedTimetableAvailable()) {

            Pair<String, TimetableType> pair = timetableManager.getLastFocusedTimetable();
            isOffline = timetableManager.isOfflineAvailable(pair.first, pair.second);
        }

        mainActivity.addLoadingFragmentAndKeepTimetableOnBackStack(isOffline,
                LoadingFragment.Owner.TIMETABLE);
    }

    @Override
    protected Integer doInBackground(Void... voids) {

        Log.i(DataUpdater.class.getName(), "data update...");

        int status = -1;

        try {

            timetableManager.prepareOnlineLists(true, mainActivity.isOnline());

            Pair<String, TimetableType> timetablePair = timetableManager.pickAutoTimetable();

            status = ONLY_LISTS;

            timetable = timetableManager.getOnlineTimetable(
                    timetablePair.first,
                    timetablePair.second,
                    Principal.UPDATER,
                    mainActivity.isOnline()
            );

            status = OK;

        } catch (UserMessageException e) {

            e.printStackTrace();
            message = e.getUserMessage();

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

            mainActivity.setExpandableListViewAdapter(
                    new BasicExpandableListAdapter(
                            mainActivity,
                            timetableManager.getExpandableListHeaders(),
                            timetableManager.getExpandableListChildren(false)
                    )
            );

            mainActivity.unlockSaveSwitch();
            mainActivity.addTimetableFragmentSmooth(timetable, true);

            Log.i(DataUpdater.class.getName(), "> > > All fine");

        } else {

            if (integer == ONLY_LISTS) {

                mainActivity.setExpandableListViewAdapter(
                        new BasicExpandableListAdapter(
                                mainActivity,
                                timetableManager.getExpandableListHeaders(),
                                timetableManager.getExpandableListChildren(false)
                        )
                );

                Log.i(DataUpdater.class.getName(), "> > > Only lists fine");
            }

            Log.i(DataUpdater.class.getName(), "> > > failed");
            mainActivity.addFailTimetableLoadingFragment();
            Toast.makeText(mainActivity, message, Toast.LENGTH_LONG).show();

        }

        super.onPostExecute(integer);
    }
}