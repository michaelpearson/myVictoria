package nz.co.pearson.vuwexams.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.networking.Course;
import nz.co.pearson.vuwexams.networking.DataSource;
import nz.co.pearson.vuwexams.networking.MyVicPortal;
import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;

public class CourseModel extends Model<Course> {
    private static CourseModel instance;
    private Context context;

    public static CourseModel getInstance(Context context) {
        if(instance == null) {
            instance = new CourseModel(context);
        }
        return(instance);
    }

    private CourseModel(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected RealmQuery<Course> getQuery(Realm realm) {
        return realm.where(Course.class);
    }

    @Override
    protected boolean refresh(Realm realm) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = sharedPreferences.getString(context.getString(R.string.KEY_USERNAME), null);
        String password = sharedPreferences.getString(context.getString(R.string.KEY_PASSWORD), null);
        if(username == null || password == null || username.equals("") || password.equals(""))  {
            return false;
        } else {
            DataSource datasource = new MyVicPortal(username, password);
            try {
                datasource.authenticate();
                final List<Course> courseList = datasource.retrieveCourses();
                realm.beginTransaction();
                realm.where(Course.class).findAll().clear();
                realm.copyToRealm(courseList);
                realm.commitTransaction();
                return true;
            } catch (DataSourceError e) {
                return false;
            }
        }
    }
}
