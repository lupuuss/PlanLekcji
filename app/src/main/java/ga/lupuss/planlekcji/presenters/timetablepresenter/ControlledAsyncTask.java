package ga.lupuss.planlekcji.presenters.timetablepresenter;

import android.os.AsyncTask;
import android.util.Log;

import ga.lupuss.planlekcji.managers.timetablemanager.TimetableManager;
import ga.lupuss.planlekcji.ui.activities.MainActivityInterface;

abstract class ControlledAsyncTask extends AsyncTask<Void, Void, Integer> {

    private final ThreadControl control;
    private boolean running;
    final TimetablePresenter controlPresenter;
    final TimetableManager timetableManager;
    final protected MainActivityInterface mainActivity;

    ControlledAsyncTask(TimetablePresenter controlPresenter) {

        this.control = controlPresenter.getThreadControl();
        this.controlPresenter = controlPresenter;
        this.timetableManager = controlPresenter.getTimetableManager();
        this.mainActivity = controlPresenter.getMainActivity();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        start();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        stop();
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        stop();
    }

    boolean isRunning() {
        return running;
    }

    private void start() {

        Log.v(ControlledAsyncTask.class.getName(), " async task is running...");
        running = true;
    }

    private void stop() {

        Log.v(ControlledAsyncTask.class.getName(), " async task stopped");
        running = false;
    }

    void pauseIfMainActivityPaused() {

        try {
            control.waitIfPaused();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
