package nz.co.pearson.vuwexams.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.networking.ClassEvent;
import nz.co.pearson.vuwexams.networking.DataSource;
import nz.co.pearson.vuwexams.networking.MyVicPortal;
import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;

public class ClassEventModel extends Model<ClassEvent> {
    private Context context;
    private static List<String> downloadedMonths = new ArrayList<>();
    private static DataSource dataSource = null;


    public ClassEventModel(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected RealmQuery<ClassEvent> getQuery(Realm realm) {
        return realm.where(ClassEvent.class);
    }

    @Override
    protected boolean refresh(Realm realm) {
        realm.beginTransaction();
        realm.where(ClassEvent.class).findAll().clear();
        realm.commitTransaction();
        downloadedMonths.clear();
        return(true);
    }

    public void downloadMonth(final int year, final int month) {
        for(String done : downloadedMonths) {
            if(done.equals(String.format("%d,%d", year, month))) {
                return;
            }
        }
        downloadedMonths.add(String.format("%d,%d", year, month));
        new Thread() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(context);
                downloadWeek(realm, 1, month, year);
                //downloadWeek(realm, 1 + 7, month, year);
                //downloadWeek(realm, 1 + 7 * 2, month, year);
                //downloadWeek(realm, 1 + 7 * 3, month, year);
                //downloadWeek(realm, 1 + 7 * 4, month, year);
                realm.close();
            }
        }.start();
    }


    private void downloadWeek(Realm realm, int day, int month, int year) {
        if(dataSource == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String username = sharedPreferences.getString(context.getString(R.string.KEY_USERNAME), null);
            String password = sharedPreferences.getString(context.getString(R.string.KEY_PASSWORD), null);
            if(username == null || password == null || username.equals("") || password.equals(""))  {
                return;
            } else {
                dataSource = new MyVicPortal(username, password);
                try {
                    dataSource.authenticate();
                } catch (DataSourceError e) {
                    dataSource = null;
                    Log.i("ClassEventModel", "Error authenticating with datasource");
                    e.printStackTrace();
                }
            }
        }
        if(dataSource == null) {
            return;
        }
        try {
            Log.i("ClassEventModel", String.format("Downloading week %d/%d/%d", day, month, year));
            List<ClassEvent> classes = dataSource.retrieveWeekOfClasses(day, month, year);
            Log.i("ClassEventModel", String.format("Downloaded %d class events", classes != null ? classes.size() : 0));
            realm.beginTransaction();
            realm.copyToRealm(classes);
            realm.commitTransaction();
        } catch (DataSourceError e) {
            dataSource = null;
            Log.i("ClassEventModel", "Error accessing datasource");
        }
    }

    public List<ClassEvent> matchEvents(int year, int month) {
        return new ArrayList<>(realm.where(ClassEvent.class).equalTo("startMonth", month).equalTo("startYear", year).findAll());
    }
}
