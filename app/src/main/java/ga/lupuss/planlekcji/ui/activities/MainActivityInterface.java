package ga.lupuss.planlekcji.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.widget.ExpandableListAdapter;
import android.widget.ProgressBar;

import java.io.File;

import ga.lupuss.planlekcji.managers.timetablemanager.Timetable;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.fragments.LoadingFragment;

public interface MainActivityInterface {

    @SuppressLint("HardwareIds")
    String getAndroidID();
    FragmentManager getMyFragmentManager();
    ProgressBar getUpdateProgressBar();

    void lockSaveSwitch();
    void unlockSaveSwitch();

    void showSingleLongToastByStringId(int id);
    void showSingleLongToast(@NonNull String message);

    void addLoadingFragmentAndKeepTimetableOnBackStack(boolean isOffline,
                                                       @NonNull LoadingFragment.Owner owner);
    void addTimetableFragmentSmooth(@NonNull Timetable timetable, boolean removeLast);
    void addFailTimetableLoadingFragment();
    void addAppInitFailScreen();
    void installApk(File file);

    void setTitleLabel(@NonNull  String str);
    void setTitleLabel(int id);
    void setModeIndicator(MainActivity.IndicatorMode mode);
    void setSaveSwitchCheckedWithoutEvent(boolean b);
    void setModeIndicatorByInternetConnection();
    void setListNameTitle(@NonNull  String name, @NonNull TimetableType type);

    boolean isOnline();
    boolean isStoragePermissionGranted();

    void setRequestedOrientationByInterface(int orientation);
    LayoutInflater getLayoutInflaterByInterface();
    String getStringByInterface(int id);
    Context getContextByInterface();
    void setExpandableListViewAdapter(@NonNull ExpandableListAdapter adapter);
}
