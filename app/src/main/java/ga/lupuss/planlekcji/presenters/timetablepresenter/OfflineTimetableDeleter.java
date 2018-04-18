package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableManager;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.activities.MainView;
import ga.lupuss.planlekcji.ui.fragments.TimetableFragment;

final class OfflineTimetableDeleter extends AsyncTask<Void, Void, Boolean> {

    private String listName;
    private TimetableType type;
    private boolean timetableExist;
    final private MainView mainView;
    final private TimetableManager timetableManager;


    OfflineTimetableDeleter(@NonNull TimetablePresenter timetablePresenter) {

        this.mainView = timetablePresenter.getMainView();
        this.timetableManager = timetablePresenter.getTimetableManager();
    }

    @Override
    protected void onPreExecute() {

        mainView.lockSaveSwitch();

        if (mainView.timetableFragmentExists()) {

            Fragment fragment =
                    mainView.getCurrentFragment();

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

            mainView.showSingleLongToast(R.string.msg_no_timetable_to_delete);
            mainView.setSaveSwitchCheckedWithoutEvent(true);
        }

        mainView.unlockSaveSwitch();
    }
}