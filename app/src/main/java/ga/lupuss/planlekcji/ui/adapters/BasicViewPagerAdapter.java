package ga.lupuss.planlekcji.ui.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ga.lupuss.planlekcji.statics.Bundles;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;
import ga.lupuss.planlekcji.ui.fragments.TimetableDayFragment;

public final class BasicViewPagerAdapter extends FragmentStatePagerAdapter {

    private String[] DAYS = {"pon", "wt", "śr", "czw", "pt"};
    private JSONArray timetable = new JSONArray();
    private TimetableType type;

    public BasicViewPagerAdapter(FragmentManager fm, JSONObject json, TimetableType type) {

        super(fm);

        this.type = type;

        try {

            timetable = json.getJSONArray("timetable");

        } catch (JSONException e) {

            throw new IllegalStateException("Critical json error");
        }

    }

    @Override
    public Fragment getItem(int position) {

        TimetableDayFragment fragment = new TimetableDayFragment();

        Bundle bundle = new Bundle();

        try {

            bundle.putString(
                    Bundles.TIMETABLE_DAY_JSON,
                    timetable.getJSONArray(position).toString()
            );
            bundle.putString(
                    Bundles.TYPE,
                    type.name()
            );

            bundle.putInt(Bundles.PAGE_ID, position);

        } catch (JSONException e) {

            throw new IllegalStateException("Critical JSON error");
        }

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getCount() {
        return DAYS.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return DAYS[position];
    }
}
