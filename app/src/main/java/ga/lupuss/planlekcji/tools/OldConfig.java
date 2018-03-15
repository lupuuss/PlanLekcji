package ga.lupuss.planlekcji.tools;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.io.File;

import ga.lupuss.planlekcji.statics.Info;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;

public class OldConfig {

    @Nullable
    public static Pair<String, TimetableType> readDefaultTimetable() {

            String data;

            File file = new File(Info.APP_FILES_DIR, "default.txt");

            try {

                byte[] bytes = Files.readAllBytes(file);

                data = new String(bytes, "UTF-8");

                int breakpos = data.indexOf("!$");

                return new Pair<>(data.substring(0, breakpos),
                        TimetableType.values()[Integer.valueOf(data.substring(breakpos + 2))]);

            } catch (Exception e) {

                return null;
            }
        }
}
