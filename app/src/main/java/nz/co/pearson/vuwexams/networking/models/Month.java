package nz.co.pearson.vuwexams.networking.models;

import io.realm.RealmObject;

/**
 * Created by mpearson on 26/02/2016.
 */
public class Month extends RealmObject {
    private int month;
    private int year;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Month() {

    }

    public Month(int month, int year) {
        this.month = month;
        this.year = year;
    }
}
