package ga.lupuss.planlekcji.presenters;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.onlinetools.ReportOutOfDataCallback;
import ga.lupuss.planlekcji.ui.activities.MainActivityInterface;

public class OutOffDataReportPresenter {

    final private MainActivityInterface mainActivity;

    public OutOffDataReportPresenter(@NonNull MainActivityInterface mainActivity) {

        this.mainActivity = mainActivity;
    }

    private final static class OutOfDataReporter extends AsyncTask<Void, Void, Integer> {

        private MainActivityInterface mainActivity;

        OutOfDataReporter(MainActivityInterface mainActivityInterface) {

            mainActivity = mainActivityInterface;
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            return new ReportOutOfDataCallback(mainActivity.getAndroidID()).report();
        }

        @Override
        protected void onPostExecute(Integer integer) {

            super.onPostExecute(integer);

            if (integer == 200){

                mainActivity.showSingleLongToastByStringId(R.string.report_ok);

            } else if (integer == 403) {

                mainActivity.showSingleLongToastByStringId(R.string.report_ok_but_up_to_date);
            } else {

                mainActivity.showSingleLongToastByStringId(R.string.report_error);
            }
        }
    }

    public void report() {

        new OutOfDataReporter(mainActivity).execute();
    }
}
