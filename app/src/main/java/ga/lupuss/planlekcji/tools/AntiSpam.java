package ga.lupuss.planlekcji.tools;

import android.os.SystemClock;

import java.util.Map;
import java.util.TreeMap;


public final class AntiSpam {

    private Map<String, Long> antiSpamStats = new TreeMap<>();

    public final boolean isFunctionAvailable(String name, long limit){

        Long elapsed = SystemClock.elapsedRealtime();

        if(antiSpamStats.containsKey( name )){

            if( ( elapsed - antiSpamStats.get( name ) ) > limit ){

                antiSpamStats.put( name, elapsed );

                return true;
            }
            return false;

        } else {
            antiSpamStats.put( name, elapsed );
            return true;
        }

    }
}
