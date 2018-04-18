package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import ga.lupuss.planlekcji.exceptions.UserMessageException;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.ui.adapters.BasicExpandableListAdapter;
import ga.lupuss.planlekcji.ui.fragments.LoadingFragment;

final class TimetableLoaderFromHref extends TimetableLoader {

    final private int OK_ONLINE_LISTS = 3;

    TimetableLoaderFromHref(@NonNull TimetablePresenter controlPresenter,
                            @NonNull String listName,
                            @NonNull TimetableType type) {

        super(controlPresenter, listName, type);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mainView.setListNameTitle(listName, type);
        mainView.lockSaveSwitch();

        boolean showOfflineButton;

        mainView.setModeIndicatorByInternetConnection();
        showOfflineButton = timetableManager.isOfflineAvailable(listName, type);


        mainView.addLoadingFragmentAndKeepTimetableOnBackStack(
                showOfflineButton,
                LoadingFragment.Owner.TIMETABLE
        );

    }

    @NonNull
    @Override
    protected Integer doInBackground(Void... voids) {

        Log.i(TimetableLoaderFromHref.class.getName(), logLine() + "> Started");

        int status;

        try {

            if (type != TimetableType.TEACHER ||
                    (timetableManager.getTeachers().containsKey(listName) ||
                            timetableManager.getTeachers().containsValue(listName))) {

                timetable =
                        timetableManager.getTimetable(
                                listName,
                                type,
                                Principal.USER,
                                mainView.isOnline()
                        );

                status = (timetable.isFromOfflineSource()) ? OK_OFFLINE : OK_ONLINE;
            } else {

                timetableManager.setLastFocusedTimetable(new Pair<>(listName, type));
                timetableManager.prepareOnlineLists(true, mainView.isOnline());
                timetable = timetableManager.getOnlineTimetable(
                        listName,
                        type,
                        Principal.USER,
                        mainView.isOnline()
                );

                status = OK_ONLINE_LISTS;
            }

        } catch (UserMessageException e) {
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

            if (integer == OK_ONLINE_LISTS) {

                mainView.setExpandableListViewData(
                        timetableManager.getExpandableListChildren(false)
                );

                Log.i(
                        TimetableLoaderFromHref.class.getName(),
                        logLine() + "> Lists + timetable OK"
                );

            } else {

                Log.i(
                        TimetableLoaderFromHref.class.getName()
                        ,logLine() + "> Loaded: "
                                + ((integer == OK_ONLINE) ? "ONLINE" : "OFFLINE")
                );
            }

            mainView.unlockSaveSwitch();
            mainView.addTimetableFragmentSmooth(timetable, false);

        } else {

            mainView.setModeIndicator(MainActivity.IndicatorMode.NO_NET);
            mainView.addFailTimetableLoadingFragment();
            mainView.showSingleLongToast(message);
            Log.i(TimetableLoaderFromHref.class.getName(), logLine() + "> Failed");
        }

        super.onPostExecute(integer);
    }
}

