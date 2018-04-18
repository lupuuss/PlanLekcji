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

        mainView.setListNameTitle(listName, type);
        mainView.lockSaveSwitch();

        boolean showOfflineButton;

        if (mode == LoadMode.FORCE_OFFLINE) {

            mainView.setModeIndicator(MainActivity.IndicatorMode.OFFLINE);
            showOfflineButton = false;

        } else {

            showOfflineButton = timetableManager.isOfflineAvailable(listName, type);

            if (showOfflineButton && !mainView.isOnline()) {


                mainView.setModeIndicator(MainActivity.IndicatorMode.OFFLINE);

            } else {

                mainView.setModeIndicatorByInternetConnection();
            }
        }

        mainView.addLoadingFragmentAndKeepTimetableOnBackStack(
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
                            mainView.isOnline()
                    );
                    break;

                case FORCE_ONLINE:

                    timetable = timetableManager.getOnlineTimetable(
                            listName,
                            type,
                            principal,
                            mainView.isOnline()
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
            message = e.getUserMessageId();
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

            mainView.addTimetableFragmentSmooth(timetable, false);

        } else {

            Log.i(
                    BasicTimetableLoader.class.getName(),
                    logLine() + "> Failed"
            );

            mainView.showSingleLongToast(message);
            mainView.setModeIndicator(MainActivity.IndicatorMode.NO_NET);
            mainView.addFailTimetableLoadingFragment();
        }

        super.onPostExecute(integer);
    }
}
