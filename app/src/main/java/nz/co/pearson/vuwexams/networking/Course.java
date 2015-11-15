package nz.co.pearson.vuwexams.networking;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by michael on 13/11/15.
 */
public class Course extends RealmObject {
    private String letterGrade;
    private String courseCode;
    private String title;
    private String period;
    private int points;
    private double efts;
    private String registrationStatus;
    private int gradePoint;
    private int pointsGaines;
    private int year;

    @Ignore
    private int EnumPeriod;
    //Hack: Cannot have custom methods on a model.


    public enum CoursePeriod {
        TRIMESTER_1 ("1", "Trimester 1"),
        TRIMESTER_2 ("2", "Trimester 2"),
        TRIMESTER_3 ("3", "Trimester 3"),
        FULL_YEAR ("F", "Full Year"),
        UNKNOWN (null, "Unknown");
        private final String descriptor;
        private final String title;
        CoursePeriod(String descriptor, String title) {
            this.descriptor = descriptor;
            this.title = title;
        }
        public String getDescriptor() {
            return(descriptor);
        }
        public String getTitle() {
            return title;
        }
        public static CoursePeriod fromDescriptor(String descriptor) {
            for(CoursePeriod cp : CoursePeriod.values()) {
                if(cp.getDescriptor().equals(descriptor)) {
                    return(cp);
                }
            }
            return(UNKNOWN);
        }
    }

    public Course() {}

    protected Course(int year, String letterGrade, String courseCode, String title, String period, int points, double efts, String registrationStatus, int gradePoint, int pointsGained) {
        this.year = year;
        this.letterGrade = letterGrade;
        this.courseCode = courseCode;
        this.title = title;
        this.period = period;
        this.efts = efts;
        this.registrationStatus = registrationStatus;
        this.gradePoint = gradePoint;
        this.points = points;
        this.pointsGaines = pointsGained;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String letterGrade) {
        this.letterGrade = letterGrade;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }


    public CoursePeriod getEnumPeriod() {
        return(CoursePeriod.fromDescriptor(getPeriod()));
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public double getEfts() {
        return efts;
    }

    public void setEfts(double efts) {
        this.efts = efts;
    }

    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public int getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(int gradePoint) {
        this.gradePoint = gradePoint;
    }

    public int getPointsGaines() {
        return pointsGaines;
    }

    public void setPointsGaines(int pointsGaines) {
        this.pointsGaines = pointsGaines;
    }
}
