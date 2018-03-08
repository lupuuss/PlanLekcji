package ga.lupuss.planlekcji.ui.fragments;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import ga.lupuss.planlekcji.presenters.timetablepresenter.LoadMode;
import ga.lupuss.planlekcji.presenters.timetablepresenter.Principal;
import ga.lupuss.planlekcji.tools.AntiSpam;
import ga.lupuss.planlekcji.statics.Bundles;
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableManager;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;

public final class LoadingFragment extends Fragment {

    private boolean isOffline;
    private Button loadOffline;
    private AntiSpam antiSpam = new AntiSpam();

    private Handler handler = new Handler();

    public enum Owner {

        TIMETABLE, APP_INIT
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.loading, container, false);

        savedInstanceState = getArguments();

        loadOffline = layout.findViewById(R.id.load_offline);

        isOffline = savedInstanceState.getBoolean(Bundles.IS_OFFLINE_TO_LOADING_SCREEN);
        setOwner(Owner.valueOf(savedInstanceState.getString(Bundles.LOADING_OWNER)));

        return layout;
    }

    @Override
    public void onResume() {

        super.onResume();

        if (!((MainActivity) getActivity()).getSwipeRefreshLayout().isRefreshing()) {

            ((MainActivity) getActivity()).getSwipeRefreshLayout().setRefreshing(true);
        }

        setOfflineButtonIf(isOffline);

    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) getActivity()).getSwipeRefreshLayout().setRefreshing(false);
        loadOffline.setVisibility(View.INVISIBLE);
    }

    public final void setOfflineButtonIf(boolean offline) {

        isOffline = offline;

        handler.removeCallbacksAndMessages(null);

        if (loadOffline != null) {

            if (offline && loadOffline.getVisibility() == View.INVISIBLE) {

                handler.postDelayed(() -> loadOffline.setVisibility(View.VISIBLE), 1500);

            } else if(!offline) {

                loadOffline.setVisibility(View.INVISIBLE);

            }

        }
    }

    public final void setOwner(Owner owner) {

        MainActivity mainActivity = (MainActivity) getActivity();

        if (owner.equals(Owner.TIMETABLE)) {

            loadOffline.setOnClickListener(view -> {


                if (antiSpam.isFunctionAvailable("load_offline", 500)) {

                    Pair<String, TimetableType> pair =
                            mainActivity
                                    .getTimetablePresenter()
                                    .getLastFocusedTimetable();
                    if (pair != null) {

                        mainActivity.loadTimetable(
                                pair.first,
                                pair.second,
                                LoadMode.FORCE_OFFLINE,
                                Principal.USER
                        );
                    }
                }
            });

        } else if (owner.equals(Owner.APP_INIT)) {

            loadOffline.setOnClickListener(
                    view ->{

                        if (antiSpam.isFunctionAvailable("load_offline", 500)) {

                            mainActivity.appInit(LoadMode.FORCE_OFFLINE);
                        }
                    }
            );

        }

    }

}
