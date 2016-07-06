package nz.co.pearson.vuwexams.networking.models;

import io.realm.RealmObject;

/**
 * Created by michael on 26/02/2016.
 */
public class Month extends RealmObject {
    private int month;
    private int year;

    @SuppressWarnings("unused")
    public int getYear() {
        return year;
    }

    @SuppressWarnings("unused")
    public void setYear(int year) {
        this.year = year;
    }

    @SuppressWarnings("unused")
    public int getMonth() {
        return month;
    }

    @SuppressWarnings("unused")
    public void setMonth(int month) {
        this.month = month;
    }

    @SuppressWarnings("unused")
    public Month() {}

    public Month(int month, int year) {
        this.month = month;
        this.year = year;
    }
}
