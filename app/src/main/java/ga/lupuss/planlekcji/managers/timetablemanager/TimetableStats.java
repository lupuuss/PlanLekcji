package ga.lupuss.planlekcji.managers.timetablemanager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ga.lupuss.planlekcji.statics.Info;
import ga.lupuss.planlekcji.tools.Files;

public final class TimetableStats {

    private Map<TimetableType, Map<String, Long>> allStats = new HashMap<>();

    private String STATS_FILENAME = "stats.txt";

    private Runnable listener;

    private MostVisitedTimetable mostVisitedTimetable;

    TimetableStats() {

        for (TimetableType type : TimetableType.values()) {

            allStats.put(type, new HashMap<>());
        }

    }

    @SuppressWarnings("WeakerAccess")
    public class MostVisitedTimetable {

        private String slug;
        private TimetableType type;
        private long clicks;

        private MostVisitedTimetable(String slug, TimetableType type, long clicks){

            this.slug = slug;
            this.type = type;
            this.clicks = clicks;
        }

        public String getSlug() {

            return slug;
        }

        public TimetableType getType() {

            return type;
        }

        long getClicks() {

            return clicks;
        }
    }

    void deleteStat(@NonNull String slug, @NonNull TimetableType type) {

        allStats.get(type).remove(slug);
        save();
    }

    Map<TimetableType, Map<String, Long>> getAllStats() {

        return allStats;
    }

    void load(){

        try {

            Log.d(TimetableStats.class.getName(), "Loading stats...");

            byte[] data;
            data = Files.readAllBytes(new File(Info.APP_FILES_DIR, STATS_FILENAME));


            String strData = new String( data, "UTF-8" );

            String[] tab = strData.split("\n");

            for( String str : tab ){
                fetchStatString( str );
            }

            refreshLeader();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void update(@NonNull String slug, @NonNull TimetableType type , long byValue){

        Log.d(TimetableStats.class.getName(),
                String.format("Slug: %s Type: %s stats +%d", slug, type, byValue));

        if (allStats.get(type).containsKey(slug)) {

            allStats.get(type).put(slug, allStats.get(type).get(slug) + byValue);

        } else {

            allStats.get(type).put(slug, byValue);

        }

        refreshLeader();
        save();
    }

    void refreshLeader() {

        long clicks = 0;
        String slug = "unknown";
        TimetableType type = null;

        for (Map.Entry<TimetableType, Map<String, Long>> statsForType : allStats.entrySet()){

            for (Map.Entry<String, Long> stat : statsForType.getValue().entrySet()) {

                if (stat.getValue() > clicks) {

                    slug = stat.getKey();
                    clicks = stat.getValue();
                    type = statsForType.getKey();
                }
            }
        }

        MostVisitedTimetable maybe = new MostVisitedTimetable(slug, type, clicks);

        if (mostVisitedTimetable != null) {

            if (maybe.clicks > mostVisitedTimetable.clicks) {

                if (listener != null &&
                        (!maybe.getSlug().equals(mostVisitedTimetable.getSlug()) ||
                                !maybe.getType().equals(mostVisitedTimetable.getType()))) {

                    mostVisitedTimetable = maybe;
                    listener.run();

                    Log.d(
                            TimetableStats.class.getName(),
                            String.format(
                                    "New leader: %s %s %d",
                                    maybe.getSlug(),
                                    maybe.getType().toString(),
                                    maybe.getClicks()
                            )
                    );
                }

                mostVisitedTimetable = maybe;
            }

        } else {

            mostVisitedTimetable = maybe;
        }
    }

    private void save(){

        StringBuilder strBuilder = new StringBuilder();

        for (Map.Entry<TimetableType, Map<String, Long>> statsForType : allStats.entrySet()) {

            for (Map.Entry<String, Long> stat : statsForType.getValue().entrySet()) {

                strBuilder.append(stat.getKey());
                strBuilder.append('$');
                strBuilder.append(statsForType.getKey().getId());
                strBuilder.append('=');
                strBuilder.append(stat.getValue());
                strBuilder.append('\n');
            }
        }

        try {

            Files.writeAllBytes(
                    new File(Info.APP_FILES_DIR, STATS_FILENAME),
                    strBuilder.toString().getBytes()
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchStatString(@NonNull String str ){

        int eq = str.indexOf('=');
        int dolarPos = str.indexOf('$');

        String slug = str.substring(0, dolarPos);
        int type = Integer.valueOf(str.substring(dolarPos + 1, eq));
        long clicks = Long.valueOf(str.substring(eq + 1));


        Log.d(
                TimetableStats.class.getName(),
                String.format(
                        "Loaded: %s %s %d",
                        slug,
                        TimetableType.values()[type],
                        clicks
                )
        );

        allStats.get(TimetableType.values()[type]).put(slug, clicks);
    }

    @Nullable
    MostVisitedTimetable getMostVisitedTimetable() {

        return mostVisitedTimetable;
    }

    void setOnLeaderChangedListener(Runnable listener) {

        this.listener = listener;
    }
}
