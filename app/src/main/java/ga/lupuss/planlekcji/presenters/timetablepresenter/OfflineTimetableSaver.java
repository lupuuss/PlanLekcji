package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableManager;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.activities.MainView;
import ga.lupuss.planlekcji.ui.fragments.TimetableFragment;

final class OfflineTimetableSaver extends AsyncTask<Void, Void, Integer> {

    private String listName;
    private TimetableType type;
    private String json;
    private boolean timetableExist = false;

    private final int NO_TT_TO_SAVE = 0;
    @SuppressWarnings("FieldCanBeLocal")
    private final int CAN_NOT_SAVE = 1;
    private final int OK = 2;

    private final MainView mainView;
    private final TimetableManager timetableManager;

    OfflineTimetableSaver(@NonNull TimetablePresenter timetablePresenter) {

        this.mainView = timetablePresenter.getMainView();
        this.timetableManager = timetablePresenter.getTimetableManager();
    }

    @Override
    protected void onPreExecute() {

        mainView.lockSaveSwitch();

        if (mainView.timetableFragmentExists()) {

            Fragment fragment = mainView.getCurrentFragment();

            listName = ((TimetableFragment) fragment).getListName();
            json = ((TimetableFragment) fragment).getJson().toString();
            type = ((TimetableFragment) fragment).getTimetableType();
            timetableExist = true;
        }

        super.onPreExecute();
    }

    @NonNull
    @Override
    protected Integer doInBackground(Void... voids) {

        if (timetableExist) {

            return timetableManager
                    .keepTimetableOffline(listName, type, json) ? OK : CAN_NOT_SAVE;

        }

        return NO_TT_TO_SAVE;
    }

    @Override
    protected void onPostExecute(Integer integer) {

        super.onPostExecute(integer);

        if (integer != OK) {

            Integer msgId;

            if (integer == NO_TT_TO_SAVE) {

                msgId = R.string.msg_no_timetable_to_save;

            } else {

                msgId = R.string.msg_can_not_save_timetable;
            }

            mainView.showSingleLongToast(msgId);

            mainView.setSaveSwitchCheckedWithoutEvent(false);
        }

        Log.i(OfflineTimetableSaver.class.getName(),
                "timetableExist: " + timetableExist + " saved: " + (integer == OK));

        mainView.unlockSaveSwitch();
    }
}