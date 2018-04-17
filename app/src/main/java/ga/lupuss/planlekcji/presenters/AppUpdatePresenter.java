package ga.lupuss.planlekcji.presenters;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import ga.lupuss.planlekcji.BuildConfig;
import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.exceptions.UserMessageException;
import ga.lupuss.planlekcji.managers.ChangelogManager;
import ga.lupuss.planlekcji.managers.MessageManager;
import ga.lupuss.planlekcji.onlinetools.AppCheckInHandler;
import ga.lupuss.planlekcji.statics.Bundles;
import ga.lupuss.planlekcji.statics.Preferences;
import ga.lupuss.planlekcji.tools.Files;
import ga.lupuss.planlekcji.ui.activities.MainActivityInterface;

public class AppUpdatePresenter {

    private static boolean updateProcess = false;
    final private AppCheckInHandler appCheckInHandler;
    final private MainActivityInterface mainActivity;
    final private ChangelogManager changelogManager = new ChangelogManager();
    final private MessageManager messageManager = new MessageManager();

    public AppUpdatePresenter(MainActivityInterface mainActivity) {

        this.appCheckInHandler =
                new AppCheckInHandler(
                        mainActivity.getContextByInterface(),
                        BuildConfig.VERSION_NAME
                );

        this.mainActivity = mainActivity;
    }

    private final static class AppChecker extends AsyncTask<Void, Void, Integer> {

        private String message;

        private final int UPDATE_NEEDED = 2;
        private final int APP_UP_TO_DATA = 1;
        private final int FAIL = 0;
        private final boolean quiet;
        private final String mostVisitedSlug;
        private final String type;
        private final MainActivityInterface mainActivity;
        private final MessageManager messageManager;
        private final AppCheckInHandler appCheckInHandler;
        private final AppUpdatePresenter appUpdatePresenter;

        AppChecker(boolean quiet,
                   @Nullable String mostVisitedSlug,
                   @Nullable String type,
                   @NonNull MainActivityInterface mainActivityInterface,
                   @NonNull MessageManager messageManager,
                   @NonNull AppCheckInHandler appCheckInHandler,
                   @NonNull AppUpdatePresenter appUpdatePresenter) {

            this.quiet = quiet;
            this.mostVisitedSlug = mostVisitedSlug;
            this.type = type;
            this.mainActivity = mainActivityInterface;
            this.messageManager = messageManager;
            this.appCheckInHandler = appCheckInHandler;
            this.appUpdatePresenter = appUpdatePresenter;
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            try {

                appUpdatePresenter.sendIdentity(mostVisitedSlug, type);

                if (appCheckInHandler.checkForUpdate()) {

                    return UPDATE_NEEDED;

                } else {

                    return APP_UP_TO_DATA;
                }

            } catch (UserMessageException e) {

                message = e.getUserMessage();

                return FAIL;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (integer == UPDATE_NEEDED) {

                new AlertDialog.Builder(mainActivity.getContextByInterface(), R.style.DialogTheme)
                        .setTitle(mainActivity.getStringByInterface(R.string.update))
                        .setMessage(mainActivity.getStringByInterface(R.string.update_found))
                        .setPositiveButton("Tak",
                                (dialogInterface, i) ->
                                        appUpdatePresenter.startAppUpdateOrRequestForPermissions())

                        .setNegativeButton("Nie", null)
                        .show();

            }

            if (integer != FAIL) {

                PreferenceManager
                        .getDefaultSharedPreferences(mainActivity.getContextByInterface())
                        .edit()
                        .putLong(Preferences.LAST_CHECK_IN, System.currentTimeMillis())
                        .apply();

                messageManager.showIfNew(
                        mainActivity.getContextByInterface(),
                        mainActivity.getLayoutInflaterByInterface(),
                        appCheckInHandler.getApiMessage()
                );
            }

            if (!quiet) {

                if (integer == APP_UP_TO_DATA) {

                    mainActivity.showSingleLongToastByStringId(R.string.app_up_to_data);

                } else if(integer == FAIL){

                    mainActivity.showSingleLongToast(message);
                }

            }
        }
    }

    private final static class AppUpdater extends AsyncTask<Void, Integer, Boolean> {

        private File apkPath;
        private final String UPDATE_FILENAME = "plan_lekcji_update.apk";
        private final MainActivityInterface mainActivity;
        private final AppCheckInHandler appCheckInHandler;
        private final ChangelogManager changelogManager;

        AppUpdater(MainActivityInterface mainActivityInterface,
                   AppCheckInHandler appCheckInHandler,
                   ChangelogManager changelogManager) {
            mainActivity = mainActivityInterface;
            this.appCheckInHandler = appCheckInHandler;
            this.changelogManager = changelogManager;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mainActivity.getUpdateProgressBar().setVisibility(View.VISIBLE);
            updateProcess = true;
            mainActivity.setRequestedOrientationByInterface(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            if(Files.fileOnServerExists(appCheckInHandler.getApiApkUrl())){

                apkPath = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS),
                        UPDATE_FILENAME
                );

                try {

                    URL url = new URL(appCheckInHandler.getApiApkUrl());

                    URLConnection connection = url.openConnection();
                    connection.setReadTimeout(3000);
                    connection.connect();

                    int fileLength = connection.getContentLength();

                    InputStream input = new BufferedInputStream( connection.getInputStream() );
                    OutputStream output = new FileOutputStream(apkPath);

                    byte data[] = new byte[1024];
                    long total = 0;
                    int count;

                    while ( (count = input.read(data)) != -1 ) {

                        total += count;
                        publishProgress( (int) (total * 100 / fileLength) );
                        output.write(data, 0, count);

                    }

                    output.flush();
                    output.close();
                    input.close();

                    return total == fileLength;

                } catch (Exception e) {

                    e.printStackTrace();
                    return false;
                }

            } else {

                return false;

            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (mainActivity.getUpdateProgressBar() != null)
                mainActivity.getUpdateProgressBar().setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            mainActivity.getUpdateProgressBar().setVisibility(View.INVISIBLE);
            mainActivity.setRequestedOrientationByInterface(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

            changelogManager.save(
                    appCheckInHandler.getApiChangeLog(),
                    appCheckInHandler.getApiVersion()
            );

            updateProcess = false;

            if (aBoolean) {
                mainActivity.installApk(apkPath);
            }
        }

        @Override
        protected void onCancelled() {

            mainActivity.setRequestedOrientationByInterface(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            super.onCancelled();
            updateProcess = false;
        }
    }

    public void loadData() {

        changelogManager.load();
        messageManager.load();
    }

    public void checkInToApi(boolean quiet, @Nullable String mostVisitedSlug, @Nullable String type) {

        new AppChecker(quiet, mostVisitedSlug, type, mainActivity, messageManager, appCheckInHandler, this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void sendIdentity(@Nullable String mostSlug, @Nullable String typeArg) {

        new Thread(() -> {

            String slug;
            String type;

            if (mostSlug == null) {

                slug = "unknown";

            } else {

                slug = mostSlug;
            }

            if (typeArg == null) {

                type = "unknown";

            } else {
                type = typeArg;
            }

            appCheckInHandler.sendIdentity(slug, type, mainActivity.getAndroidID());
        }).start();
    }

    private void startAppUpdateOrRequestForPermissions() {

        if (mainActivity.isStoragePermissionGranted()) {

            startAppUpdate();
        }
    }

    public void startAppUpdate() {

        new AppUpdater(mainActivity, appCheckInHandler, changelogManager)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void showChangelogIfReady() {

        changelogManager.showIfReady(
                mainActivity.getContextByInterface(),
                mainActivity.getLayoutInflaterByInterface()
        );
        changelogManager.clear();
    }

    public boolean isUpdateProcess() {

        return updateProcess;
    }

    public void backUpState(Bundle savedInstanceState) {

        savedInstanceState.putBoolean(Bundles.UPDATE_PROCESS, updateProcess);
    }

    public void restoreUpdateProcess(Bundle savedInstanceState) {

        updateProcess = savedInstanceState.getBoolean(Bundles.UPDATE_PROCESS);
    }
}
