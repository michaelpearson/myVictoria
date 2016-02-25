package nz.co.pearson.vuwexams.networking;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.Calendar;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class ClassEvent extends RealmObject {
    private int startYear;
    private int startMonth;
    private int startDay;
    private int startHour;
    private int startMinute;
    private int durationMinutes;
    private String className;
    private String classLocation;

    public ClassEvent() {}

    public ClassEvent(int startYear, int startMonth, int startDay, int startHour, int startMinute, int durationMinutes, String className, String classLocation) {
        this.startYear = startYear;
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.durationMinutes = durationMinutes;
        this.className = className;
        this.classLocation = classLocation;
    }

    public int getStartYear() {

        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassLocation() {
        return classLocation;
    }

    public void setClassLocation(String classLocation) {
        this.classLocation = classLocation;
    }

    public static WeekViewEvent getEvent(ClassEvent classEvent) {
        WeekViewEvent event = new WeekViewEvent(classEvent.hashCode(),
                classEvent.getClassName(),
                classEvent.getStartYear(),
                classEvent.getStartMonth(),
                classEvent.getStartDay(),
                classEvent.getStartHour(),
                classEvent.getStartMinute(),
                classEvent.getStartYear(),
                classEvent.getStartMonth(),
                classEvent.getStartDay(),
                classEvent.getStartHour(),
                classEvent.getStartMinute() + classEvent.getDurationMinutes());
        event.setLocation(classEvent.getClassLocation());
        return event;
    }
}
