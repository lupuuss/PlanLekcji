package ga.lupuss.planlekcji.presenters;

import android.os.Bundle;

public interface Notifiable {

    default void onCreate(){}
    default void onResume(){}
    default void onPause(boolean isFinishing){}
    default void onDestroy(){}
    default void onSaveInstanceState(Bundle savedInstanceState, boolean isConfigurationChanging){}
    default void onRestore(Bundle savedInstanceState){}

}
