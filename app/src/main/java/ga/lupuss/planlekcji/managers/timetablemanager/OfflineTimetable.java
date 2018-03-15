package ga.lupuss.planlekcji.managers.timetablemanager;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import ga.lupuss.planlekcji.tools.Files;


final class OfflineTimetable extends Timetable {

    private final File file;

    OfflineTimetable(@NonNull String slug, @NonNull JSONObject data, @NonNull File file) {

        super(slug, data, true);
        this.file = file;
    }

    @NonNull public File getFile() {

        return file;
    }

    public void update(@NonNull JSONObject data) {

        try {

            setJsonData(data);
            saveToInternal();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void saveToInternal() throws IOException {

        getFile().getParentFile().mkdirs();

        Files.writeAllBytes(getFile(), getJsonData().toString().getBytes());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void deleteFromInternal() {

        Log.d(OfflineTimetable.class.getName(), "Delete from internal: " + file.delete());
    }
}
