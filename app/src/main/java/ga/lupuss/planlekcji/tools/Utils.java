package ga.lupuss.planlekcji.tools;


import android.support.annotation.NonNull;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    @NonNull
    public static String paresISO(@NonNull String str) throws Exception {

        int pos = str.indexOf('T');

        String dateStr = str.substring(0, pos);
        String hourStr = str.substring(pos + 1, str.lastIndexOf(':'));

        String[] q = dateStr.split("-");
        String[] p = hourStr.split(":");

        Calendar date = Calendar.getInstance();

        date.set(
                Integer.valueOf(q[0]),
                Integer.valueOf(q[1]) - 1,
                Integer.valueOf(q[2]) - 1,
                Integer.valueOf(p[0]),
                Integer.valueOf(p[1])
        );

        long diff = TimeZone.getTimeZone("CET").getOffset(date.getTimeInMillis())
                - TimeZone.getTimeZone("GMT").getOffset(date.getTimeInMillis());

        Calendar dateToPrint = Calendar.getInstance();

        dateToPrint.setTimeInMillis(date.getTimeInMillis() + diff);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMAN);

        return  simpleDateFormat.format(dateToPrint.getTime());
    }

    public static void teachersNamesSort(@NonNull List<String> names) {

        Collections.sort(names, new Comparator<String>() {

            @Override
            public int compare(String s, String t1) {

                String s1 = s.substring(findDeepestDot(s));

                String s2 = t1.substring(findDeepestDot(t1));

                Collator collator = Collator.getInstance( new Locale("pl","PL"));

                return collator.compare(s1, s2);
            }

            private int findDeepestDot(String str) {
                int k = -1;
                int i;

                while (true) {

                    i = str.indexOf('.', k + 1);
                    if (i > -1) {

                        k = i;

                    } else {
                        break;
                    }
                }

                return k;
            }

        });

    }
}
