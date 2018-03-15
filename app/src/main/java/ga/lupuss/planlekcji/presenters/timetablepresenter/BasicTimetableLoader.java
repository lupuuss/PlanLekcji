package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.support.annotation.NonNull;
import android.util.Log;

import ga.lupuss.planlekcji.exceptions.UserMessageException;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.ui.fragments.LoadingFragment;


final class BasicTimetableLoader extends TimetableLoader {

    private LoadMode mode;
    private Principal principal;

    BasicTimetableLoader(@NonNull TimetablePresenter controlPresenter,
                         @NonNull String listName,
                         @NonNull TimetableType type,
                         @NonNull LoadMode mode,
                         @NonNull Principal principal) {

        super(controlPresenter, listName, type);
        this.mode = mode;
        this.principal = principal;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mainActivity.setListNameTitle(listName, type);
        mainActivity.lockSaveSwitch();

        boolean showOfflineButton;

        if (mode == LoadMode.FORCE_OFFLINE) {

            mainActivity.setModeIndicator(MainActivity.IndicatorMode.OFFLINE);
            showOfflineButton = false;

        } else {

            showOfflineButton = timetableManager.isOfflineAvailable(listName, type);

            if (showOfflineButton && !mainActivity.isOnline()) {


                mainActivity.setModeIndicator(MainActivity.IndicatorMode.OFFLINE);

            } else {

                mainActivity.setModeIndicatorByInternetConnection();
            }
        }

        mainActivity.addLoadingFragmentAndKeepTimetableOnBackStack(
                showOfflineButton,
                LoadingFragment.Owner.TIMETABLE
        );
    }

    @NonNull
    @Override
    protected Integer doInBackground(Void... voids) {

        Log.i(BasicTimetableLoader.class.getName(), logLine() + "> Started");

        int status;

        try {

            switch (mode) {

                case ANY:

                    timetable = timetableManager.getTimetable(
                            listName,
                            type,
                            principal,
                            mainActivity.isOnline()
                    );
                    break;

                case FORCE_ONLINE:

                    timetable = timetableManager.getOnlineTimetable(
                            listName,
                            type,
                            principal,
                            mainActivity.isOnline()
                    );
                    break;

                case FORCE_OFFLINE:

                    timetable = timetableManager.getOfflineTimetable(
                            listName,
                            type,
                            principal
                    );
                    break;
            }

            status = timetable.isFromOfflineSource() ? OK_OFFLINE : OK_ONLINE;

        } catch (UserMessageException e) {

            e.printStackTrace();
            message = e.getUserMessage();
            status = BAD;
        }

        sleepItForSmoothDrawerHide();
        pauseIfMainActivityPaused();
        return status;
    }

    @Override
    protected void onPostExecute(Integer integer) {

        if (integer != BAD) {

            Log.i(
                    BasicTimetableLoader.class.getName(),
                    logLine() + "> Loaded: "
                            + ((integer == OK_ONLINE) ? "ONLINE" : "OFFLINE")
            );

            mainActivity.addTimetableFragmentSmooth(timetable, false);

        } else {

            Log.i(
                    BasicTimetableLoader.class.getName(),
                    logLine() + "> Failed"
            );

            mainActivity.makeSingleLongToast(message);
            mainActivity.setModeIndicator(MainActivity.IndicatorMode.NO_NET);
            mainActivity.addFailTimetableLoadingFragment();
        }

        super.onPostExecute(integer);
    }
}
