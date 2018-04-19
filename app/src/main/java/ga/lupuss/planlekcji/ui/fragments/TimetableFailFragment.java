package ga.lupuss.planlekcji.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ga.lupuss.planlekcji.tools.AntiSpam;
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.R;

public final class TimetableFailFragment extends Fragment {

    private AntiSpam antiSpam = new AntiSpam();

    @BindView(R.id.refresh_button_fail_screen) Button refreshButton;
    private Unbinder unbinder;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LinearLayout layout =
                (LinearLayout) inflater.inflate(R.layout.fragment_timetable_loading_fail, container, false);

        unbinder = ButterKnife.bind(this, layout);

        return layout;
    }

    @OnClick(R.id.refresh_button_fail_screen)
    void onRefreshButtonClick(View view) {

        MainActivity mainActivity = (MainActivity) getActivity();

        if (antiSpam.isFunctionAvailable("refresh", 500)) {

            mainActivity.updateData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
