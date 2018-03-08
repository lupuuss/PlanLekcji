package ga.lupuss.planlekcji.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import ga.lupuss.planlekcji.statics.Bundles;
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.ui.builders.OnTimetableItemClick;
import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.ui.builders.TimetableViewsCreator;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;


@SuppressWarnings("Convert2streamapi")
public final class TimetableDayFragment extends Fragment {

    private JSONArray jsonArray;
    private TimetableType type;
    private int id;

    private int lastLesson = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        savedInstanceState = getArguments();

        initFieldsBasedOnBundle(savedInstanceState);

    }

    final void initFieldsBasedOnBundle(Bundle savedInstanceState){

        if (savedInstanceState != null) {

            try {

                jsonArray =
                        new JSONArray(
                                savedInstanceState.getString(Bundles.TIMETABLE_DAY_JSON)
                        );
                type = TimetableType.valueOf(savedInstanceState.getString(Bundles.TYPE));
                id = savedInstanceState.getInt(Bundles.PAGE_ID);

            } catch (JSONException e) {

                throw new IllegalStateException("Critical JSON error");
            }


        } else {

            throw new IllegalStateException("Bundle fucked up!");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        NestedScrollView scrollView =
                (NestedScrollView) inflater.inflate(R.layout.timetable_day_fragment, container, false);

        LinearLayout layout = scrollView.findViewById(R.id.lessons_container);

        List<View> viewList = new TimetableViewsCreator(
                inflater, layout, getHoursFromMainActivity(),
                getTimetableLoaderMethodReferenceFromMainActivity()

        ).create(jsonArray, type);

        for (View v : viewList) {

            layout.addView(v);
        }

        return scrollView;
    }

    private OnTimetableItemClick getTimetableLoaderMethodReferenceFromMainActivity() {

        return (type1, slug) -> (
                (MainActivity) getParentFragment()
                        .getActivity()).
                loadTimetableFromHref(slug, type1);
    }

    private List<Pair<String, String>> getHoursFromMainActivity() {

        return ((MainActivity) getParentFragment()
                .getActivity())
                .getTimetablePresenter()
                .getHoursList();
    }

    @Override
    public void onResume() {
        super.onResume();

        markCurrentLesson();
    }

    private void markCurrentLesson() {
        if (PreferenceManager.getDefaultSharedPreferences(
                this.getContext()).getBoolean(getString(R.string.is_mark_lesson), true)) {

            Pair<Integer, Integer> lesson =
                    ((TimetableFragment) getParentFragment()).pickCurrentLessonAndDay();

            if (lesson.first == id && lesson.second != -1) {

                View mainView = getView();

                if (mainView != null) {

                    View view = ((LinearLayout)mainView
                            .findViewById(R.id.lessons_container))
                            .getChildAt(lesson.second);

                    lastLesson = lesson.second;

                    if (view != null) {
                        view.setBackground(
                                ContextCompat.getDrawable(this.getContext(), R.drawable.back_lesson_current)
                        );
                    }
                }

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unmarkPreviousLesson();
    }

    private void unmarkPreviousLesson() {

        if (PreferenceManager.getDefaultSharedPreferences(
                this.getContext()).getBoolean(getString(R.string.is_mark_lesson), true)) {

            if (lastLesson != -1) {

                View mainView = getView();

                if (mainView != null) {

                    View view = ((LinearLayout) mainView
                            .findViewById(R.id.lessons_container))
                            .getChildAt(lastLesson);

                    if (view != null) {
                        view.setBackground(
                                ContextCompat.getDrawable(this.getContext(), R.drawable.back_lesson)
                        );
                    }

                    lastLesson = -1;

                }

            }
        }

    }
}
