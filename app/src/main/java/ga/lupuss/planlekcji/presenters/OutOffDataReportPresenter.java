package ga.lupuss.planlekcji.presenters;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.onlinetools.ReportOutOfDataCallback;
import ga.lupuss.planlekcji.ui.activities.MainView;

public class OutOffDataReportPresenter {

    final private MainView mainActivity;

    public OutOffDataReportPresenter(@NonNull MainView mainActivity) {

        this.mainActivity = mainActivity;
    }

    private final static class OutOfDataReporter extends AsyncTask<Void, Void, Integer> {

        private MainView mainActivity;

        OutOfDataReporter(MainView mainView) {

            mainActivity = mainView;
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            return new ReportOutOfDataCallback(mainActivity.getAndroidID()).report();
        }

        @Override
        protected void onPostExecute(Integer integer) {

            super.onPostExecute(integer);

            if (integer == 200){

                mainActivity.showSingleLongToast(R.string.report_ok);

            } else if (integer == 403) {

                mainActivity.showSingleLongToast(R.string.report_ok_but_up_to_date);
            } else {

                mainActivity.showSingleLongToast(R.string.report_error);
            }
        }
    }

    public void report() {

        new OutOfDataReporter(mainActivity).execute();
    }
}
