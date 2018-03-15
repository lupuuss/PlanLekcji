package ga.lupuss.planlekcji.managers.timetablemanager;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.Calendar;
import java.util.List;

import static java.lang.Math.abs;

public final class TimetableChooser {

    private List<Pair<TimeInt, TimeInt>> hours;

    private final Pair<Integer, Integer> WEEKEND = new Pair<>(0, -1);

    public TimetableChooser(@NonNull List<Pair<String, String>> hours) {

        this.hours = TimeInt.createHoursList(hours);
    }

    @NonNull
    public Pair<Integer, Integer> pick(List<Integer> lessonsCountForEachDay) {

        if (lessonsCountForEachDay.size() < 5) throw new AssertionError();

        TimeInt timeInt;
        Calendar time = Calendar.getInstance();

        int day = parseDay(time.get(Calendar.DAY_OF_WEEK));
        int lesson = -1;

        if (isWeekend(day)) {

            return WEEKEND;

        } else {

            if (anyHour(day, lessonsCountForEachDay)) {

                int currentHour = time.get( Calendar.HOUR_OF_DAY );
                int currentMinute = time.get( Calendar.MINUTE );

                lesson = getNearestLesson(new TimeInt(currentHour, currentMinute));

                timeInt = getLastHourEnd(day, lessonsCountForEachDay);

                int hour = time.get(Calendar.HOUR_OF_DAY);

                if ( hour > timeInt.getHour() ){

                    day += 1;
                    lesson = -1;

                } else if(hour == timeInt.getHour()){

                    if(time.get(Calendar.MINUTE) >= timeInt.getMinutes() ){

                        day += 1;
                        lesson = -1;
                    }
                }
            }

            if (isWeekend(day)) {

                return WEEKEND;
            } else {

                return new Pair<>(day, lesson);
            }

        }
    }

    @NonNull
    private TimeInt getLastHourEnd(int day, List<Integer> lessonsCountForEachDay) {

        return hours.get(lessonsCountForEachDay.get(day) - 1).second;
    }

    private int parseDay(int day){

        if(day == Calendar.SUNDAY)
            return 6;
        else if(day == Calendar.MONDAY)
            return 0;
        else
            return day - 2;
    }

    private boolean anyHour(int day, @NonNull List<Integer> lessonsCountForEachDay) {

        return lessonsCountForEachDay.get(day) != -1;
    }

    private boolean isWeekend(int day) {

        return day > 4;
    }

    private int getNearestLesson(@NonNull TimeInt currentTime){

        int hourTime;
        int betweenNowAndStart;
        int hour = currentTime.getHour();


        int minutesToFirst = TimeInt.minutesBetween(hours.get(0).first, currentTime);

        if(minutesToFirst <= 15 && minutesToFirst > -1){

            return 0;

        } else if(TimeInt.minutesBetween(currentTime, hours.get(hours.size() - 1 ).second ) > 0){

            return -1;

        } else if(minutesToFirst <= -1) {

            for (int i = 0; i < hours.size(); i++) {


                if (hour - hours.get(i).first.getHour() < 2) {

                    hourTime = abs(TimeInt.minutesBetween(hours.get(i).first, hours.get(i).second));
                    betweenNowAndStart = TimeInt.minutesBetween(currentTime, hours.get(i).first);

                    if (betweenNowAndStart < hourTime) {
                        return i;
                    }
                }
            }

        }
        return -1;

    }
}
