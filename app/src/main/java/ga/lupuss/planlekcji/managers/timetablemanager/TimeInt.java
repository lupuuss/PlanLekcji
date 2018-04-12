package ga.lupuss.planlekcji.managers.timetablemanager;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

final class TimeInt {

    private int hour;
    private int minutes;

    private TimeInt(@NonNull String time){

        if( !time.equals("") ) {
            setTimeInt( time );
        }

    }

    TimeInt(int hour, int minutes ){
        this.hour = hour;
        this.minutes = minutes;
    }

    @SuppressWarnings("Convert2streamapi")
    @NonNull
    static List<Pair<TimeInt, TimeInt>> createHoursList(@NonNull List<Pair<String, String>> hours){

        ArrayList< Pair< TimeInt, TimeInt > > hoursInts = new ArrayList<>();

        for (Pair<String, String> pair : hours) {
            hoursInts.add( new Pair<>(
                                new TimeInt(pair.first),
                                new TimeInt(pair.second)
                            ));
        }

        return hoursInts;
    }

    private void setTimeInt(@NonNull String time){
        int dDot = time.indexOf(':');

        String hour = time.substring(0, dDot);
        String minutes = time.substring(dDot + 1, time.length());

        this.hour = Integer.valueOf(hour);
        this.minutes = Integer.valueOf(minutes);
    }

    int getHour(){
        return hour;
    }

    int getMinutes(){
        return minutes;
    }

    static int minutesBetween(@NonNull TimeInt one,@NonNull TimeInt two){

        int hours = one.hour - two.hour;
        int minutes = one.minutes - two.minutes;

        return hours * 60 + minutes;

    }
}
