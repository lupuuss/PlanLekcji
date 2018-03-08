package ga.lupuss.planlekcji.managers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import ga.lupuss.planlekcji.statics.Info;
import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.tools.Files;

public class MessageManager {

    private final String MESSAGE_FILENAME = "message.txt";
    private String message = "";

    public void load() {

        File file = new File(Info.APP_FILES_DIR, MESSAGE_FILENAME);

        try {

            message = new String(Files.readAllBytes(file));

        } catch (IOException e) {
            e.printStackTrace();
            message = "";
        }

    }

    public void showIfNew(Context context, LayoutInflater inflater, String message) {

        if (!this.message.equals(message) && !message.equals("")) {

            @SuppressLint("InflateParams")
            ScrollView scrollView =
                    (ScrollView) inflater.inflate(R.layout.scrollable_alert, null, false);

            ((TextView) scrollView.findViewById(R.id.textViewWithScroll)).setText(message);

            new AlertDialog.Builder(context, R.style.DialogTheme)
                    .setTitle(context.getString(R.string.info))
                    .setView(scrollView)
                    .setPositiveButton("OK", null)
                    .show();

        }

        save(message);
    }

    private void save(String message) {

        this.message = message;

        File file = new File(Info.APP_FILES_DIR, MESSAGE_FILENAME);
        try {
            Files.writeAllBytes(file, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
