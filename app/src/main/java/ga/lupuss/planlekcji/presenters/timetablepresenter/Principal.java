package ga.lupuss.planlekcji.presenters.timetablepresenter;

public enum Principal{

    USER(3), UPDATER(2), APP(1);

    final private int statValue;

    Principal(int statValue) {

        this.statValue = statValue;
    }

    public int getStatValue() {
        return statValue;
    }
}