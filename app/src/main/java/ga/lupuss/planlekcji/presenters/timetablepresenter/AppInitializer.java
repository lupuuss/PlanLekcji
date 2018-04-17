package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.support.annotation.NonNull;
import android.util.Log;

import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.exceptions.UserMessageException;
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.ui.adapters.BasicExpandableListAdapter;
import ga.lupuss.planlekcji.ui.fragments.LoadingFragment;

final class AppInitializer extends ControlledAsyncTask {

    private String message;

    final private int OK_ONLINE = 2;
    final private int OK_OFFLINE = 1;
    final private int BAD = 0;
    final private LoadMode mode;

    AppInitializer(@NonNull TimetablePresenter controlPresenter,@NonNull LoadMode mode) {

        super(controlPresenter);
        this.mode = mode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        boolean showOfflineButton = false;

        if (mode != LoadMode.FORCE_OFFLINE) {
            showOfflineButton = timetableManager.isAnyOfflineTimetable();
        }

        mainActivity.setModeIndicatorByInternetConnection();
        mainActivity.addLoadingFragmentAndKeepTimetableOnBackStack(
                showOfflineButton,
                LoadingFragment.Owner.APP_INIT
        );
        mainActivity.setTitleLabel(R.string.lists_loading);
        mainActivity.setExpandableListViewAdapter(
                BasicExpandableListAdapter.empty(
                        mainActivity.getContextByInterface(),
                        timetableManager.getExpandableListHeaders()
                ));
    }

    @NonNull
    @Override
    protected Integer doInBackground(Void... voids) {

        Log.i(AppInitializer.class.getName(), "Loading lists started");

        int status;

        if ((mainActivity.isOnline()
                || timetableManager.areListsOK()) && mode != LoadMode.FORCE_OFFLINE) {

            try {

                boolean isFresh = timetableManager.prepareOnlineLists(false, mainActivity.isOnline());

                Log.i(AppInitializer.class.getName(),
                        "Lists reached from " + (isFresh ? "internet" : "RAM"));

                status = OK_ONLINE;

            } catch (UserMessageException e) {

                message = e.getUserMessage();

                status = tryOffline();
            }
        } else {

            message = mainActivity.getStringByInterface(R.string.msg_no_internet);
            status = tryOffline();
        }

        pauseIfMainActivityPaused();
        return status;
    }

    private int tryOffline(){

        Log.i(AppInitializer.class.getName(), "Trying offline lists...");

        if (timetableManager.prepareOfflineLists()) {

            Log.i(AppInitializer.class.getName(), "Offline lists loaded");

            return OK_OFFLINE;
        }

        return BAD;
    }

    @Override
    protected void onPostExecute(Integer integer) {

        if(integer != BAD) {

            mainActivity.setExpandableListViewAdapter(
                    new BasicExpandableListAdapter(
                            mainActivity.getContextByInterface(),
                            timetableManager.getExpandableListHeaders(),
                            timetableManager.getExpandableListChildren(integer == OK_OFFLINE)
                    )
            );

            if (integer == OK_ONLINE) {

                mainActivity.setModeIndicator(MainActivity.IndicatorMode.NET);
                controlPresenter.loadAutoTimetable(LoadMode.ANY);

            } else if(integer == OK_OFFLINE) {

                mainActivity.setModeIndicator(MainActivity.IndicatorMode.OFFLINE);
                controlPresenter.loadAutoTimetable(LoadMode.FORCE_OFFLINE);
            }

        } else {

            Log.w(AppInitializer.class.getName(), "Lists not reached");

            mainActivity.showSingleLongToast(message);
            mainActivity.setModeIndicator(MainActivity.IndicatorMode.NO_NET);
            mainActivity.addAppInitFailScreen();
        }

        super.onPostExecute(integer);
    }

}