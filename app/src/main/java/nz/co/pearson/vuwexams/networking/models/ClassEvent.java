package nz.co.pearson.vuwexams.networking.models;

import android.graphics.Color;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.Calendar;
import java.util.Random;

import io.realm.RealmObject;

public class ClassEvent extends RealmObject {
    private int startYear;
    private int startMonth;
    private int startDay;
    private int startHour;
    private int startMinute;
    private int durationMinutes;
    private String className;
    private String classLocation;

    @SuppressWarnings("unused")
    public ClassEvent() {}

    public ClassEvent(Calendar startDate, Calendar endDate, String className, String classLocation) {
        this.className = className;
        this.classLocation = classLocation;

        startYear = startDate.get(Calendar.YEAR);
        startMonth = startDate.get(Calendar.MONTH) + 1;
        startDay = startDate.get(Calendar.DAY_OF_MONTH);
        startHour = startDate.get(Calendar.HOUR_OF_DAY);
        startMinute = startDate.get(Calendar.MINUTE);

        durationMinutes = (int)((endDate.getTimeInMillis() - startDate.getTimeInMillis()) / 1000 / 60);
    }

    public int getStartYear() {
        return startYear;
    }

    @SuppressWarnings("unused")
    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getStartMonth() {
        return startMonth;
    }

    @SuppressWarnings("unused")
    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }

    public int getStartDay() {
        return startDay;
    }

    @SuppressWarnings("unused")
    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public int getStartHour() {
        return startHour;
    }

    @SuppressWarnings("unused")
    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    @SuppressWarnings("unused")
    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    @SuppressWarnings("unused")
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getClassName() {
        return className;
    }

    @SuppressWarnings("unused")
    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassLocation() {
        return classLocation;
    }

    @SuppressWarnings("unused")
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
        event.setColor(generateRandomColor(classEvent.getClassName()));
        event.setLocation(classEvent.getClassLocation());
        return event;
    }

    private static int generateRandomColor(String seed) {
        if(seed == null) {
            seed = "";
        }
        Random random = new Random();
        random.setSeed(seed.hashCode());
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        red = (red + 100) / 2;
        green = (green + 100) / 2;
        blue = (blue + 100) / 2;

        return Color.rgb(red, green, blue);
    }
}