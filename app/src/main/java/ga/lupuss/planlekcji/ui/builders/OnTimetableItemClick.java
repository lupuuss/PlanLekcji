package ga.lupuss.planlekcji.ui.builders;

import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;

public interface OnTimetableItemClick {

    void onClick(TimetableType type, String slug);
}
