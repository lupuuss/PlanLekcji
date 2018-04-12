package ga.lupuss.planlekcji.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ga.lupuss.planlekcji.ui.adapters.BasicViewPagerAdapter;
import ga.lupuss.planlekcji.statics.Bundles;
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableChooser;
import ga.lupuss.planlekcji.tools.Utils;


public final class TimetableFragment extends Fragment {

    private JSONObject json;
    private String listName;
    private TimetableType type;
    private boolean fromOfflineSource;
    private TimetableChooser timetableChooser;

    private ViewPager viewPager;
    private FrameLayout footer;

    private List<Integer> lessonsPerDay;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        savedInstanceState = getArguments();

        initFieldsBasedOnBundle(savedInstanceState);
    }

    final void initFieldsBasedOnBundle(@Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null) {

            try {

                json = new JSONObject(savedInstanceState.getString(Bundles.TIMETABLE_JSON));
                fromOfflineSource = savedInstanceState.getBoolean(Bundles.IS_FROM_OFFLINE_SOURCE);

                type = TimetableType.valueOf(json.getString("type").toUpperCase());

                if (type.isNameAvailable() && !json.isNull("name")) {

                    listName = json.getString("name");

                } else {

                    listName = json.getString(type.getSlugName());
                }

            } catch (JSONException e) {

                throw new IllegalStateException("Critical JSON error");

            }

        } else {

            throw new IllegalStateException("Bundle fucked up!");
        }
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_timetable, container, false);

        timetableChooser = new TimetableChooser(((MainActivity)getActivity()).getTimetablePresenter().getHoursList());
        lessonsPerDay = lessonsCount(json);

        viewPager = linearLayout.findViewById(R.id.pager);
        viewPager.setAdapter(
                new BasicViewPagerAdapter(getChildFragmentManager(), json, type)
        );
        viewPager.setOffscreenPageLimit(6);

        adjustViewPagerToSwipeRefresh(viewPager);

        TabLayout tabLayout = linearLayout.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        footer = linearLayout.findViewById(R.id.layout_footer);

        if (!setLastUpdate(footer)) {

            footer.setVisibility(View.GONE);
        }

        return linearLayout;
    }

    private boolean setLastUpdate(FrameLayout footer) {

        try {

            String dateStr = Utils.paresISO(json.getString("update"));

            if (dateStr.isEmpty()) {

                return false;

            } else {

                ((TextView) footer.findViewById(R.id.last_update))
                        .setText(
                                String.format(
                                        getString(R.string.last_update_text),
                                        dateStr
                                        )
                        );

                return true;
            }

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }

    private List<Integer> lessonsCount(@NonNull JSONObject js) {

        try {
            List<Integer> list = new ArrayList<>();
            JSONArray jsonArray = js.getJSONArray("timetable");

            for (int i = 0; i < jsonArray.length(); i++) {

                if (jsonArray.getJSONArray(i).length() == 0) {

                    list.add(-1);

                } else {

                    list.add(jsonArray.getJSONArray(i).length());
                }
            }

            return list;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    private void adjustViewPagerToSwipeRefresh(ViewPager viewPager) {

        viewPager.setOnTouchListener((v, event) -> {

            MainActivity mainActivity = (MainActivity)getActivity();
            mainActivity.getSwipeRefreshLayout().setEnabled(false);

            switch (event.getAction()) {

                case MotionEvent.ACTION_UP:
                    mainActivity.getSwipeRefreshLayout().setEnabled(true);
                    break;
            }
            return false;
        });
    }

    public Pair<Integer, Integer> pickCurrentLessonAndDay() {

        return timetableChooser.pick(lessonsPerDay);
    }

    @Override
    public void onResume() {

        super.onResume();
        refreshMainActivity();
        setCurrentPageIfNeeded();
        showLastUpdateFooterIfNeeded();
    }

    private void showLastUpdateFooterIfNeeded() {

        if (PreferenceManager.getDefaultSharedPreferences(this.getContext())
                .getBoolean(getString(R.string.show_last_update_footer), true)) {

            footer.setVisibility(View.VISIBLE);

        } else {

            footer.setVisibility(View.GONE);
        }
    }

    private void setCurrentPageIfNeeded() {

        if (PreferenceManager.getDefaultSharedPreferences(
                this.getContext()).getBoolean(getString(R.string.is_page_based), true)) {

            new Handler().postDelayed(
                    () -> viewPager.setCurrentItem(timetableChooser.pick(lessonsPerDay).first),
                    300
            );
        }
    }

    private void refreshMainActivity() {

        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.setListNameTitle(listName, type);
        mainActivity.setSaveSwitchCheckedWithoutEvent(
                mainActivity.getTimetablePresenter().isOfflineTimetableAvailable(listName, type)
        );
        mainActivity.setModeIndicatorByLoadType(isFromOfflineSource());
        mainActivity.getTimetablePresenter().setLastFocusedTimetable(new Pair<>(listName, type));
        mainActivity.unlockSaveSwitch();
    }

    @NonNull
    public JSONObject getJson(){

        return json;
    }

    public boolean isFromOfflineSource() {

        return fromOfflineSource;
    }

    @NonNull
    public String getListName() {

        return listName;
    }

    @NonNull
    public TimetableType getTimetableType() {

        return type;
    }
}
