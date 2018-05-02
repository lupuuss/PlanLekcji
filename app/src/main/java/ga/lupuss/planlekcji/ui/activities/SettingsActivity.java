package ga.lupuss.planlekcji.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.widget.Toast;

import ga.lupuss.planlekcji.BuildConfig;
import ga.lupuss.planlekcji.R;


public final class SettingsActivity extends AppCompatPreferenceActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();

        getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public static class MainPreferenceFragment extends PreferenceFragment {

        private int easterEggClicks = 0;
        private Preference versionPref;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_main);

            versionPref = findPreference(getString(R.string.version_pref));

            versionPref.setSummary(BuildConfig.VERSION_NAME);
            versionOnClick();

        }

        private void versionOnClick() {

            versionPref.setOnPreferenceClickListener(preference -> {

                if( easterEggClicks < 20 ) {

                    switch(easterEggClicks++){

                        case 2:
                            toast("Nie zostaniesz tutaj programistą :(");
                            break;
                        case 4:
                            toast("No serio idź sobie :/");
                            break;
                    }
                }

                return true;
            });
        }

        private void toast(String toast){
            Toast.makeText( getActivity().getApplicationContext(), toast, Toast.LENGTH_SHORT ).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
