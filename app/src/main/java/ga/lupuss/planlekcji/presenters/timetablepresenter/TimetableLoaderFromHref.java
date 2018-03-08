package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;

import ga.lupuss.planlekcji.exceptions.UserMessageException;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.adapters.BasicExpandableListAdapter;
import ga.lupuss.planlekcji.ui.fragments.LoadingFragment;

final class TimetableLoaderFromHref extends TimetableLoader {

    final private int OK_ONLINE_LISTS = 3;

    TimetableLoaderFromHref(TimetablePresenter controlPresenter, String listName, TimetableType type) {

        super(controlPresenter, listName, type);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mainActivity.setListNameTitle(listName, type);
        mainActivity.lockSaveSwitch();

        boolean showOfflineButton;

        mainActivity.setModeIndicatorByInternetConnection();
        showOfflineButton = timetableManager.isOfflineAvailable(listName, type);


        mainActivity.addLoadingFragmentAndKeepTimetableOnBackStack(
                showOfflineButton,
                LoadingFragment.Owner.TIMETABLE
        );

    }

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
                                mainActivity.isOnline()
                        );

                status = (timetable.isFromOfflineSource()) ? OK_OFFLINE : OK_ONLINE;
            } else {

                timetableManager.setLastFocusedTimetable(new Pair<>(listName, type));
                timetableManager.prepareOnlineLists(true, mainActivity.isOnline());
                timetable = timetableManager.getOnlineTimetable(
                        listName,
                        type,
                        Principal.USER,
                        mainActivity.isOnline()
                );

                status = OK_ONLINE_LISTS;
            }

        } catch (UserMessageException e) {
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

            if (integer == OK_ONLINE_LISTS) {

                mainActivity.setExpandableListViewAdapter(
                        new BasicExpandableListAdapter(
                                mainActivity,
                                timetableManager.getExpandableListHeaders(),
                                timetableManager.getExpandableListChildren(false)
                        )
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

            mainActivity.unlockSaveSwitch();
            mainActivity.addTimetableFragmentSmooth(timetable, false);

        } else {

            mainActivity.addFailTimetableLoadingFragment();
            Toast.makeText(mainActivity, message, Toast.LENGTH_LONG).show();
            Log.i(TimetableLoaderFromHref.class.getName(), logLine() + "> Failed");
        }

        super.onPostExecute(integer);
    }
}

