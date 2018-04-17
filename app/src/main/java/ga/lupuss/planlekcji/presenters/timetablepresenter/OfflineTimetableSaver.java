package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableManager;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.activities.MainActivityInterface;
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

    private final MainActivityInterface mainActivity;
    private final TimetableManager timetableManager;

    OfflineTimetableSaver(@NonNull TimetablePresenter timetablePresenter) {

        this.mainActivity = timetablePresenter.getMainActivity();
        this.timetableManager = timetablePresenter.getTimetableManager();
    }

    @Override
    protected void onPreExecute() {

        mainActivity.lockSaveSwitch();

        Fragment fragment =
                mainActivity.getMyFragmentManager().findFragmentById(R.id.fragment_container);

        if (fragment instanceof TimetableFragment) {

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

            String msg;

            if (integer == NO_TT_TO_SAVE) {

                msg = mainActivity.getStringByInterface(R.string.msg_no_timetable_to_save);

            } else {

                msg = mainActivity.getStringByInterface(R.string.msg_can_not_save_timetable);
            }

            mainActivity.showSingleLongToast(msg);

            mainActivity.setSaveSwitchCheckedWithoutEvent(false);
        }

        Log.i(OfflineTimetableSaver.class.getName(),
                "timetableExist: " + timetableExist + " saved: " + (integer == OK));

        mainActivity.unlockSaveSwitch();
    }
}