package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableManager;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.ui.fragments.TimetableFragment;

final class OfflineTimetableDeleter extends AsyncTask<Void, Void, Boolean> {

    private String listName;
    private TimetableType type;
    private boolean timetableExist;
    final private MainActivity mainActivity;
    final private TimetableManager timetableManager;


    OfflineTimetableDeleter(@NonNull TimetablePresenter timetablePresenter) {

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
            type = ((TimetableFragment) fragment).getTimetableType();
            timetableExist = true;
        }

        super.onPreExecute();
    }

    @NonNull
    @Override
    protected Boolean doInBackground(Void... voids) {

        if (timetableExist) {

            timetableManager.deleteOfflineTimetable(listName, type);
            return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean bool) {

        super.onPostExecute(bool);

        if (!bool) {

            mainActivity.makeSingleLongToastByStringId(R.string.msg_no_timetable_to_delete);
            mainActivity.setSaveSwitchCheckedWithoutEvent(true);
        }

        mainActivity.unlockSaveSwitch();
    }
}