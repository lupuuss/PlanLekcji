package ga.lupuss.planlekcji.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import ga.lupuss.planlekcji.BuildConfig;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableStats;
import ga.lupuss.planlekcji.presenters.AppUpdatePresenter;
import ga.lupuss.planlekcji.presenters.OutOffDataReportPresenter;
import ga.lupuss.planlekcji.presenters.timetablepresenter.LoadMode;
import ga.lupuss.planlekcji.presenters.timetablepresenter.Principal;
import ga.lupuss.planlekcji.presenters.timetablepresenter.TimetablePresenter;
import ga.lupuss.planlekcji.statics.Bundles;
import ga.lupuss.planlekcji.statics.Info;
import ga.lupuss.planlekcji.statics.Preferences;
import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.managers.timetablemanager.Timetable;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.tools.AntiSpam;
import ga.lupuss.planlekcji.ui.fragments.AppInitFailFragment;
import ga.lupuss.planlekcji.ui.fragments.LoadingFragment;
import ga.lupuss.planlekcji.ui.fragments.TimetableFailFragment;
import ga.lupuss.planlekcji.ui.fragments.TimetableFragment;

public final class MainActivity extends AppCompatActivity {

    private TimetablePresenter timetablePresenter;
    private AppUpdatePresenter appUpdatePresenter;

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private AntiSpam antiSpam = new AntiSpam();

    private Handler timetableLoadHandler = new Handler();
    private Toast toast;

    //LAYOUT

    private DrawerLayout drawerLayout;
    private ExpandableListView expandableListView;
    public SwipeRefreshLayout swipeRefreshLayout;
    private int lastExpandableListViewGroup = -1;

    //  TOOLBAR

    private ActionBarDrawerToggle drawerToggle;
    private SwitchCompat saveSwitch;
    private LinearLayout modeIndicator;
    private ProgressBar updateProgressBar;

    public enum IndicatorMode {
        NO_NET, NET, OFFLINE
    }

    //Title label

    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Info.APP_FILES_DIR = getFilesDir();

        timetablePresenter = new TimetablePresenter(this);
        appUpdatePresenter = new AppUpdatePresenter(this);
        loadData();

        super.onCreate(savedInstanceState);

        timetablePresenter.setOnStatsLeaderChanged(this::sendIdentity);

        setContentView(R.layout.activity_main);

        initFields();

        final boolean listsRestored;

        if (savedInstanceState != null) {

            appUpdatePresenter.restoreUpdateProcess(savedInstanceState);

            if (appUpdatePresenter.isUpdateProcess()) {

                updateProgressBar.setVisibility(View.VISIBLE);
            }

            title.setText(savedInstanceState.getString(Bundles.TITLE));
            listsRestored = timetablePresenter.restoreState(savedInstanceState);

        } else {

            listsRestored = false;

        }

        drawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(

                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        drawerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        if (savedInstanceState != null && listsRestored) {

                            unlockSaveSwitchAfterRestore();
                            loadTimetableAfterRestore();

                        } else {


                            appUpdatePresenter.showChangelogIfReady();
                            appInit(LoadMode.ANY);
                        }
                    }
                });

    }

    @SuppressLint("InflateParams")
    private void initFields() {

        drawerLayout = findViewById(R.id.drawer_layout);

        title = findViewById(R.id.main_title);

        saveSwitch = findViewById(R.id.save_switch);
        lockSaveSwitch();
        setSaveSwitchListener(false);

        expandableListView = findViewById(R.id.left_drawer);
        setExpandableListViewOnClicks();

        modeIndicator = findViewById(R.id.mode_indicator);
        setModeIndicatorByInternetConnection();

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        setSwipeRefreshListener();

        updateProgressBar = findViewById(R.id.update_progress_bar);
        toolbarInit();
    }

    private void loadData() {

        timetablePresenter.loadData();
        appUpdatePresenter.loadData();
    }

    private void setSwipeRefreshListener() {

        swipeRefreshLayout.setOnRefreshListener(() -> {

            Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

            if (fragment instanceof AppInitFailFragment) {

                appInit(LoadMode.ANY);

            } else {

                updateData();
            }

        });

    }

    private void setExpandableListViewOnClicks() {

        final String [] IGNORED_VALUES = {"-"};

        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {

            if(antiSpam.isFunctionAvailable("expClick", 750)) {

                String slugOrName = (String) expandableListView
                        .getExpandableListAdapter()
                        .getChild(groupPosition, childPosition);

                for (String str : IGNORED_VALUES)
                    if (slugOrName.equals(str))
                        return true;


                loadTimetable(
                        slugOrName,
                        TimetableType.values()[groupPosition],
                        LoadMode.ANY,
                        Principal.USER
                );

                hideDrawer();

                return true;
            }
            return true;
        });

        expandableListView.setOnGroupExpandListener(i -> {

            if(lastExpandableListViewGroup >= 0){

                expandableListView.collapseGroup(lastExpandableListViewGroup);
            }

            lastExpandableListViewGroup= i;

        });

        expandableListView.setOnGroupCollapseListener(i -> {

            if(lastExpandableListViewGroup == i){

                lastExpandableListViewGroup = -1;
            }
        });

    }

    private void hideDrawer(){
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void setSaveSwitchListener(boolean withoutAction) {

        saveSwitch.setOnCheckedChangeListener((toggle, isChecked) -> {

            if (isChecked) {

                if (!withoutAction) {
                    keepTimetableOffline();
                }
                toggle.setText(MainActivity.this.getString(R.string.save_switch_delete));

            } else {

                toggle.setText(MainActivity.this.getString(R.string.save_switch_save));

                if (!withoutAction) {
                    deleteOfflineTimetable();
                }
            }

        });
    }

    private void toolbarInit(){

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if( getSupportActionBar() != null ) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setHomeButtonEnabled(true);
        }

    }

    private void loadTimetableAfterRestore() {

        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (!(fragment instanceof TimetableFragment)) {

            loadAutoTimetable(LoadMode.ANY);
        }
    }

    private void unlockSaveSwitchAfterRestore() {

        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (fragment instanceof TimetableFragment) {

            unlockSaveSwitch();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        timetablePresenter.resumeThreadControl();

        checkForInToApiIfNeeded();

        if (timetablePresenter.isOnResumeLoadAvailable()) {

            Log.d(MainActivity.class.getName(), "Load timetable after pause...");

            new Handler().postDelayed(() -> {

                timetablePresenter.getOnResumeLoad().run();
                timetablePresenter.setOnResumeLoadNull();
            }, 100);
        }

    }

    private void checkForInToApiIfNeeded() {

        // check for update procedure

        long lastCheck = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getLong(Preferences.LAST_CHECK_IN, 0);

        final long MAX_DIFF = 2 * 60 * 60 * 1000;

        if (System.currentTimeMillis() - lastCheck > MAX_DIFF && isOnline()
                && !BuildConfig.VERSION_NAME.contains("ALPHA")) {

            checkInToApi(true);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!isFinishing()) {

            timetablePresenter.pauseThreadControl();
        }
    }

    protected void onDestroy() {
        super.onDestroy();

        if (!timetablePresenter.isMainTaskNull()) {

            timetablePresenter.cancelMainTask();
        }

        timetablePresenter.cancelThreadControl();

        timetableLoadHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.right_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return drawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {

            hideDrawer();

        } else {

            // removes current fragment
            fragmentManager.beginTransaction()
                    .remove(fragmentManager.findFragmentById(R.id.fragment_container))
                    .commit();

            timetablePresenter.pauseCurrentControlledAsyncTask();

            super.onBackPressed();

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        timetablePresenter.backUpState(savedInstanceState);
        appUpdatePresenter.backUpState(savedInstanceState);

        savedInstanceState.putString(Bundles.TITLE, title.getText().toString());

        if (!timetablePresenter.isMainTaskNull() && isChangingConfigurations()) {

            timetablePresenter.cancelMainTask();
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if( grantResults[0]== PackageManager.PERMISSION_GRANTED ){

            startAppUpdate();
        }
    }

    // Right menu onClicks

    public void setCurrentAsDefaultOnClick(MenuItem item) {

       timetablePresenter.setCurrentAsDefaultOnClick();
    }

    public void reportOutOfDateOnClick(MenuItem item) {

        new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle(getString(R.string.report))
                .setMessage(getString(R.string.report_warning))
                .setPositiveButton(
                        "Tak",
                        (dialogInterface, i) ->
                                new OutOffDataReportPresenter(MainActivity.this).report()
                ).setNegativeButton("Nie", null)
                .show();
    }

    public void checkForUpdateOnClick(MenuItem item) {

        if (!appUpdatePresenter.isUpdateProcess()) {

            checkInToApi(false);

        } else {

            runOnUiThread(() -> makeSingleLongToastByStringId(R.string.update_in_progress));
        }
    }

    public void goToSettings(MenuItem item) {

        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    // TimetablePresenter tasks inits

    public void appInit(LoadMode mode) {

        timetablePresenter.appInit(mode);
    }

    public void loadTimetable(@NonNull String listName, @NonNull TimetableType type,
                              @NonNull LoadMode mode, @NonNull Principal principal) {

        timetablePresenter.loadTimetable(listName, type, mode, principal);
    }

    public void loadTimetableFromHref(@NonNull String listName, @NonNull TimetableType type) {

        timetablePresenter.loadTimetableFromHref(listName, type);
    }

    private void loadAutoTimetable(@NonNull LoadMode mode) {

        timetablePresenter.loadAutoTimetable(mode);
    }

    public void updateData() {

        timetablePresenter.updateData();
    }

    private void keepTimetableOffline() {

        timetablePresenter.keepTimetableOffline();
    }

    private void deleteOfflineTimetable() {

        timetablePresenter.deleteOfflineTimetable();
    }

    // AppUpdatePresenter tasks inits

    private void checkInToApi(boolean quiet) {

        if (timetablePresenter.isMostVisitedTimetableAvailable()) {

            TimetableStats.MostVisitedTimetable mostVisitedTimetable =
                    timetablePresenter.getMostVisitedTimetable();

            appUpdatePresenter.checkInToApi(
                    quiet,
                    mostVisitedTimetable.getSlug(),
                    mostVisitedTimetable.getType().toString()
            );

        } else {

            appUpdatePresenter.checkInToApi(quiet, null, null);
        }
    }

    private void sendIdentity() {

        if (timetablePresenter.isMostVisitedTimetableAvailable()) {

            TimetableStats.MostVisitedTimetable mostVisitedTimetable =
                    timetablePresenter.getMostVisitedTimetable();

            appUpdatePresenter.sendIdentity(
                    mostVisitedTimetable.getSlug(),
                    mostVisitedTimetable.getType().toString()
            );

        } else {

            appUpdatePresenter.sendIdentity(null, null);
        }
    }

    private void startAppUpdate() {

        appUpdatePresenter.startAppUpdate();
    }

    // intents

    public void installApk(@NonNull File path){

        Intent i = new Intent();

        Uri uri;

        if(Build.VERSION.SDK_INT <= 23){

            uri = Uri.fromFile( path );

        } else {

            uri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".ga.lupuss.planlekcji.provider", path);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        i.setAction(Intent.ACTION_VIEW);
        i.setDataAndType(uri, "application/vnd.android.package-archive");

        startActivity(i);
        finish();
    }

    // fragments inits

    public void addTimetableFragmentSmooth(@NonNull Timetable timetable, boolean removeLast) {

        fragmentManager.beginTransaction()
                .remove(fragmentManager.findFragmentById(R.id.fragment_container))
                .commit();

        timetableLoadHandler.removeCallbacksAndMessages(null);

        timetableLoadHandler.postDelayed(() ->{

            if (MainActivity.this.isFinishing() || !MainActivity.isAppInForeground(this)) {

                timetablePresenter.setOnResumeLoad(() -> addTimetableFragment(timetable, removeLast));
                Log.d(MainActivity.class.getName(), "Load timetable later...");

            } else {

                addTimetableFragment(timetable, removeLast);
            }

        }, 230);

    }

    private void addTimetableFragment(@NonNull Timetable timetable, boolean removeLast) {

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        TimetableFragment timetableFragment = new TimetableFragment();
        Bundle bundle = new Bundle();

        bundle.putString(
                Bundles.TIMETABLE_JSON,
                timetable.getJsonData().toString());
        bundle.putBoolean(
                Bundles.IS_FROM_OFFLINE_SOURCE,
                timetable.isFromOfflineSource()
        );
        timetableFragment.setArguments(bundle);
        if (removeLast) {
            fragmentManager.popBackStack();
        }

        transaction.replace(R.id.fragment_container, timetableFragment);
        transaction.commit();

    }

    public void addLoadingFragmentAndKeepTimetableOnBackStack(boolean isOffline,
                                                              @NonNull LoadingFragment.Owner owner) {

        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (!(fragment instanceof LoadingFragment) || fragment.getView() == null) { // avoid double animation

            Log.v(MainActivity.class.getName(), "New LoadingFragment...");

            Fragment loading = new LoadingFragment();

            Bundle bundle = new Bundle();

            bundle.putBoolean(Bundles.IS_OFFLINE_TO_LOADING_SCREEN, isOffline);

            bundle.putString(Bundles.LOADING_OWNER, owner.name());

            loading.setArguments(bundle);

            FragmentTransaction transaction =
                    fragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_container, loading);

            // Keep on back stack only TimetableFragments

            if (fragment instanceof TimetableFragment) {

                transaction.addToBackStack(null);
            }

            transaction.commit();

        } else {

            // change owner and isOffline if necessary

            Log.v(MainActivity.class.getName(), "Old LoadingFragment...");

            ((LoadingFragment)fragment).setOfflineButtonIf(isOffline);
            ((LoadingFragment)fragment).setOwner(owner);

        }
    }

    public void addFailTimetableLoadingFragment(){

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new TimetableFailFragment())
                .commit();
    }

    public void addAppInitFailScreen() {

        Fragment fragment = new AppInitFailFragment();

        fragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

    }

    //

    public void makeSingleLongToast(@NonNull String message) {

        if (toast != null) {

            toast.cancel();
        }

        toast = Toast.makeText(this, message, Toast.LENGTH_LONG);

        toast.show();
    }

    public void makeSingleLongToastByStringId(int id) {

        makeSingleLongToast(getString(id));
    }

    // setters && getters

    public ProgressBar getUpdateProgressBar() {

        return updateProgressBar;
    }

    public TimetablePresenter getTimetablePresenter() {

        return timetablePresenter;
    }

    public FragmentManager getMyFragmentManager() {
        return fragmentManager;
    }

    public void setExpandableListViewAdapter(@NonNull ExpandableListAdapter adapter) {
        expandableListView.setAdapter(adapter);
        lastExpandableListViewGroup = -1;
    }

    public void setSaveSwitchCheckedWithoutEvent(boolean b) {

        setSaveSwitchListener(true);
        saveSwitch.setChecked(b);
        setSaveSwitchListener(false);
    }

    public void setListNameTitle(@NonNull  String name, @NonNull TimetableType type) {

        if (!type.isNameAvailable()) {

            title.setText(type.getPrefix() + " " + name);

        } else if (timetablePresenter.getTeachersMap() != null){

            if (!timetablePresenter.getTeachersMap().containsValue(name)
                    && timetablePresenter.getTeachersMap().containsKey(name)){

                title.setText(type.getPrefix() + " " + name);

            } else {

                title.setText(name);
            }

        }
    }

    public void setTitleLabel(@NonNull  String str) {
        title.setText(str);
    }

    public void lockSaveSwitch(){

        saveSwitch.setEnabled(false);
    }

    public void unlockSaveSwitch(){

        saveSwitch.setEnabled(true);
    }

    public void setModeIndicatorByLoadType(boolean fromOfflineSource) {

        if (fromOfflineSource) {

            setModeIndicator(IndicatorMode.OFFLINE);

        } else {

            setModeIndicator(IndicatorMode.NET);

        }
    }

    public void setModeIndicatorByInternetConnection() {

        if (isOnline()) {

            setModeIndicator(IndicatorMode.NET);
        } else {

            setModeIndicator(IndicatorMode.NO_NET);
        }

    }

    public void setModeIndicator(@NonNull IndicatorMode mode) {

        switch (mode) {

            case NO_NET:

                modeIndicator.setBackground(
                        ContextCompat.getDrawable(this, R.drawable.circle_online_no_net)
                );

                break;
            case NET:

                modeIndicator.setBackground(
                        ContextCompat.getDrawable(this, R.drawable.circle_online_net)
                );

                break;
            case OFFLINE:

                modeIndicator.setBackground(
                        ContextCompat.getDrawable(this, R.drawable.circle_offline)
                );

                break;
        }

    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {

        return swipeRefreshLayout;
    }

    //checkers

    public static boolean isAppInForeground(@NonNull Context context){
        boolean isInForeground = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInForeground = true;
                    }
                }
            }
        }

        return isInForeground;
    }

    public boolean isStoragePermissionGranted() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {

                return true;

            } else {

                ActivityCompat.requestPermissions( this , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {

            return true;
        }
    }

    @SuppressLint("HardwareIds")
    public String getAndroidID() {
        return Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public boolean isOnline(){
        ConnectivityManager cm =
                (ConnectivityManager)this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
