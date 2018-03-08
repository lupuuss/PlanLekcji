package ga.lupuss.planlekcji.presenters;

import android.os.AsyncTask;
import android.widget.Toast;

import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.onlineoptions.ReportOutOfDataCallback;
import ga.lupuss.planlekcji.ui.activities.MainActivity;

public class OutOffDataReportPresenter {

    final private MainActivity mainActivity;

    public OutOffDataReportPresenter(MainActivity mainActivity) {

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

                Toast.makeText(
                        mainActivity,
                        mainActivity.getString(R.string.report_ok),
                        Toast.LENGTH_LONG
                ).show();

            } else if (integer == 403) {

                Toast.makeText(
                        mainActivity,
                        mainActivity.getString(R.string.report_ok_but_up_to_date),
                        Toast.LENGTH_LONG
                ).show();

            } else {

                Toast.makeText(
                        mainActivity,
                        mainActivity.getString(R.string.report_error),
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    public void report() {

        new OutOfDateReporter().execute();
    }
}
