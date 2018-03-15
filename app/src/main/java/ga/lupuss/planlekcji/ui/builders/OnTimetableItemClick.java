package ga.lupuss.planlekcji.ui.builders;

import android.support.annotation.NonNull;

import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;

public interface OnTimetableItemClick {

    void onClick(@NonNull TimetableType type, @NonNull String slug);
}
