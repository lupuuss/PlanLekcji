package ga.lupuss.planlekcji.managers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import ga.lupuss.planlekcji.statics.Info;
import ga.lupuss.planlekcji.R;
import ga.lupuss.planlekcji.tools.Files;
import ga.lupuss.planlekcji.ui.activities.MainView;

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

    public void showIfNew(@NonNull String message, MainView mainView) {

        if (!this.message.equals(message) && !message.equals("")) {

            mainView.postInfoDialog(message);

        }

        save(message);
    }

    private void save(@NonNull String message) {

        this.message = message;

        File file = new File(Info.APP_FILES_DIR, MESSAGE_FILENAME);
        try {
            Files.writeAllBytes(file, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
