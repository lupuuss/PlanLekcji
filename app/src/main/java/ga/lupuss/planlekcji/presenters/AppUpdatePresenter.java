package ga.lupuss.planlekcji.presenters;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.ui.activities.MainView;
import ga.lupuss.planlekcji.ui.activities.ResourceProvider;

public class AppUpdatePresenter implements Notifiable{

    private static boolean updateProcess = false;
    final private AppCheckInHandler appCheckInHandler;
    final private MainView mainView;
    final private ResourceProvider resourceProvider;
    final private ChangelogManager changelogManager = new ChangelogManager();
    final private MessageManager messageManager = new MessageManager();

    public AppUpdatePresenter(MainActivity mainActivity) {

        this.appCheckInHandler =
                new AppCheckInHandler(
                        BuildConfig.VERSION_NAME
                );

        this.mainView = mainActivity;
        this.resourceProvider = mainActivity;
    }

    @Override
    public void onCreate() {

        changelogManager.loadChangelog();
        messageManager.load();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState, boolean isConfigurationChanging) {

        savedInstanceState.putBoolean(Bundles.UPDATE_PROCESS, updateProcess);
    }

    @Override
    public void onRestore(Bundle savedInstanceState) {
        updateProcess = savedInstanceState.getBoolean(Bundles.UPDATE_PROCESS);
    }

    private final static class AppChecker extends AsyncTask<Void, Void, Integer> {

        private Integer message = null;

        private final int UPDATE_NEEDED = 2;
        private final int APP_UP_TO_DATA = 1;
        private final int FAIL = 0;
        private final boolean quiet;
        private final String mostVisitedSlug;
        private final String type;
        private final AppUpdatePresenter appUpdatePresenter;

        AppChecker(boolean quiet,
                   @Nullable String mostVisitedSlug,
                   @Nullable String type,
                   @NonNull AppUpdatePresenter appUpdatePresenter) {

            this.quiet = quiet;
            this.mostVisitedSlug = mostVisitedSlug;
            this.type = type;
            this.appUpdatePresenter = appUpdatePresenter;
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            try {

                appUpdatePresenter.sendIdentity(mostVisitedSlug, type);

                if (appUpdatePresenter.appCheckInHandler.checkForUpdate()) {

                    return UPDATE_NEEDED;

                } else {

                    return APP_UP_TO_DATA;
                }

            } catch (UserMessageException e) {

                message = e.getUserMessageId();

                return FAIL;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (integer == UPDATE_NEEDED) {

                appUpdatePresenter.mainView.postNewUpdateDialog(
                        appUpdatePresenter::startAppUpdateOrRequestForPermissions
                );
            }

            if (integer != FAIL) {

                appUpdatePresenter.registerInPreferences();

                appUpdatePresenter.messageManager.showIfNew(
                        appUpdatePresenter.appCheckInHandler.getApiMessage(),
                        appUpdatePresenter.mainView
                );
            }

            if (!quiet) {

                if (integer == APP_UP_TO_DATA) {

                    appUpdatePresenter.mainView.showSingleLongToast(R.string.app_up_to_data);

                } else if (integer == FAIL) {

                    appUpdatePresenter.mainView.showSingleLongToast(message);
                }

            }
        }
    }

    private final static class AppUpdater extends AsyncTask<Void, Integer, Boolean> {

        private File apkPath;
        private final String UPDATE_FILENAME = "plan_lekcji_update.apk";
        private final AppUpdatePresenter appUpdatePresenter;

        AppUpdater(AppUpdatePresenter appUpdatePresenter) {

            this.appUpdatePresenter = appUpdatePresenter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            appUpdatePresenter.mainView.getUpdateProgressBar().setVisibility(View.VISIBLE);
            updateProcess = true;
            appUpdatePresenter.mainView.setOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            if (Files.fileOnServerExists(appUpdatePresenter.appCheckInHandler.getApiApkUrl())) {

                apkPath = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS),
                        UPDATE_FILENAME
                );

                try {

                    URL url = new URL(appUpdatePresenter.appCheckInHandler.getApiApkUrl());

                    URLConnection connection = url.openConnection();
                    connection.setReadTimeout(3000);
                    connection.connect();

                    int fileLength = connection.getContentLength();

                    InputStream input = new BufferedInputStream(connection.getInputStream());
                    OutputStream output = new FileOutputStream(apkPath);

                    byte data[] = new byte[1024];
                    long total = 0;
                    int count;

                    while ((count = input.read(data)) != -1) {

                        total += count;
                        publishProgress((int) (total * 100 / fileLength));
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

            if (appUpdatePresenter.mainView.getUpdateProgressBar() != null)
                appUpdatePresenter.mainView.getUpdateProgressBar().setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            appUpdatePresenter.mainView.getUpdateProgressBar().setVisibility(View.INVISIBLE);
            appUpdatePresenter.mainView.setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

            appUpdatePresenter.changelogManager.saveChangelog(
                    appUpdatePresenter.appCheckInHandler.getApiChangeLog(),
                    appUpdatePresenter.appCheckInHandler.getApiVersion()
            );

            updateProcess = false;

            if (aBoolean) {
                appUpdatePresenter.mainView.installApk(apkPath);
            }
        }

        @Override
        protected void onCancelled() {

            appUpdatePresenter.mainView.setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            super.onCancelled();
            updateProcess = false;
        }
    }

    public void checkInToApi(boolean quiet, @Nullable String mostVisitedSlug, @Nullable String type) {

        new AppChecker(
                quiet,
                mostVisitedSlug,
                type,
                this
        ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

            appCheckInHandler.sendIdentity(slug, type, mainView.getAndroidID());
        }).start();
    }

    private void startAppUpdateOrRequestForPermissions() {

        if (mainView.isStoragePermissionGranted()) {

            startAppUpdate();
        }
    }

    public void startAppUpdate() {

        new AppUpdater(this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void showChangelogIfReady() {

        if (changelogManager.isChangelogReady()) {

            mainView.postChangelogDialog(changelogManager.getChangelogString());
        }

        changelogManager.clearChangelog();
    }

    public boolean isUpdateProcess() {

        return updateProcess;
    }

    private void registerInPreferences() {

        resourceProvider.getSharedPreferences()
                .edit()
                .putLong(Preferences.LAST_CHECK_IN, System.currentTimeMillis())
                .apply();
    }
}
