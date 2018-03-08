package ga.lupuss.planlekcji.presenters.timetablepresenter;

import ga.lupuss.planlekcji.managers.timetablemanager.Timetable;
import ga.lupuss.planlekcji.managers.timetablemanager.TimetableType;

abstract class TimetableLoader extends ControlledAsyncTask  {

    protected String message = null;
    protected Timetable timetable;
    String listName;
    protected TimetableType type;

    final int OK_ONLINE = 2;
    final  int OK_OFFLINE = 1;
    final int BAD = 0;

    TimetableLoader(TimetablePresenter timetablePresenter, String listName, TimetableType type) {

        super(timetablePresenter);
        this.listName = listName;
        this.type = type;
    }

    void sleepItForSmoothDrawerHide() {

        try {

            Thread.sleep(500);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    String logLine() {

        return "Loading task( List name: " + listName + " Type: " + type.name() + " ) ";
    }
}
