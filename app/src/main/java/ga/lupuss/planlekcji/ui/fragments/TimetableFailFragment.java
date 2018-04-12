package ga.lupuss.planlekcji.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import ga.lupuss.planlekcji.tools.AntiSpam;
import ga.lupuss.planlekcji.ui.activities.MainActivity;
import ga.lupuss.planlekcji.R;

public final class TimetableFailFragment extends Fragment {

    private AntiSpam antiSpam = new AntiSpam();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LinearLayout layout =
                (LinearLayout) inflater.inflate(R.layout.fragment_timetable_loading_fail, container, false);

        setButtonsListeners(layout);

        return layout;
    }

    private void setButtonsListeners(LinearLayout layout) {
        Button refresh = layout.findViewById(R.id.refresh_button_fail_screen);

        MainActivity mainActivity = (MainActivity) getActivity();

        refresh.setOnClickListener(view ->{

            if (antiSpam.isFunctionAvailable("refresh", 500)) {

                mainActivity.updateData();
            }
        });
    }

}
