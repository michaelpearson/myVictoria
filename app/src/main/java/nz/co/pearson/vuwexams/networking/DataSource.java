package nz.co.pearson.vuwexams.networking;

import java.util.List;

import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;
import nz.co.pearson.vuwexams.networking.models.ClassEvent;
import nz.co.pearson.vuwexams.networking.models.Course;

/**
 * Created by michael on 13/11/15.
 */
public interface DataSource {
    boolean authenticate() throws DataSourceError;
    List<Course> retrieveCourses() throws DataSourceError;
    List<ClassEvent> getEvents(int month, int year) throws DataSourceError;
}
