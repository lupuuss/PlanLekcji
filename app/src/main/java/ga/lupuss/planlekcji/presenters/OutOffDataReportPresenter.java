package ga.lupuss.planlekcji.presenters;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.onlineoptions.ReportOutOfDataCallback;
import ga.lupuss.planlekcji.ui.activities.MainActivity;

public class OutOffDataReportPresenter {

    final private MainActivity mainActivity;

    public OutOffDataReportPresenter(@NonNull MainActivity mainActivity) {

        this.mainActivity = mainActivity;
    }

    private final class OutOfDateReporter extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {

            return new ReportOutOfDataCallback(mainActivity.getAndroidID()).report();
        }

        @Override
        protected void onPostExecute(Integer integer) {

            super.onPostExecute(integer);

            if (integer == 200){

                mainActivity.makeSingleLongToastByStringId(R.string.report_ok);

            } else if (integer == 403) {

                mainActivity.makeSingleLongToastByStringId(R.string.report_ok_but_up_to_date);
            } else {

                mainActivity.makeSingleLongToastByStringId(R.string.report_error);
            }
        }
    }

    public void report() {

        new OutOfDateReporter().execute();
    }
}
