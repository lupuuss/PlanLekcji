package ga.lupuss.planlekcji.ui.activities;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ProgressBar;

import java.io.File;
import java.util.List;
import java.util.Map;

import ga.lupuss.planlekcji.managers.timetablemanager.Timetable;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.fragments.LoadingFragment;

public interface MainView {

    @SuppressLint("HardwareIds")
    String getAndroidID();
    Fragment getCurrentFragment();

    SwipeRefreshLayout getSwipeRefreshingLayout();
    ProgressBar getUpdateProgressBar();

    void lockSaveSwitch();
    void unlockSaveSwitch();

    void showSingleLongToast(int id);
    void showSingleLongToast(@NonNull String message);

    void addLoadingFragmentAndKeepTimetableOnBackStack(boolean isOffline,
                                                       @NonNull LoadingFragment.Owner owner);
    void addTimetableFragmentSmooth(@NonNull Timetable timetable, boolean removeLast);
    void addFailTimetableLoadingFragment();
    void addAppInitFailScreen();
    
    void installApk(File file);

    void postNewUpdateDialog(@NonNull Runnable onYesClick);
    void postInfoDialog(@NonNull String message);
    void postChangelogDialog(@NonNull String changelogString);

    void setTitleLabel(@NonNull  String str);
    void setTitleLabel(int id);
    void setModeIndicator(MainActivity.IndicatorMode mode);
    void setSaveSwitchCheckedWithoutEvent(boolean b);
    void setModeIndicatorByInternetConnection();
    void setListNameTitle(@NonNull  String name, @NonNull TimetableType type);
    void setExpandableListViewEmpty();
    void setExpandableListViewData(@NonNull Map<String, List<String>> children);

    boolean isOnline();
    boolean isStoragePermissionGranted();
    boolean timetableFragmentExists();

    void executeOnUiThread(Runnable runnable);
    void setOrientation(int orientation);
}
