package nz.co.pearson.vuwexams.networking;

import java.util.List;

import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;

/**
 * Created by michael on 13/11/15.
 */
public interface DataSource {
    boolean authenticate() throws DataSourceError;
    List<Course> retrieveCourses() throws DataSourceError;
    List<ClassEvent> retrieveWeekOfClasses(int day, int month, int year) throws DataSourceError;
}
