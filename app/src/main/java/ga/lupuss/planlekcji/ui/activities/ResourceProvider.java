package ga.lupuss.planlekcji.ui.activities;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public interface ResourceProvider {
    @NonNull
    SharedPreferences getSharedPreferences();
    String[] getStringArray(int id);
    String getStringById(int id);
}
