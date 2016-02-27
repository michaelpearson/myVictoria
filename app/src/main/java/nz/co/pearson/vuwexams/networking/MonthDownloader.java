package nz.co.pearson.vuwexams.networking;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.util.EventListener;
import java.util.List;

import io.realm.Realm;
import nz.co.pearson.vuwexams.MainActivity;
import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;
import nz.co.pearson.vuwexams.networking.models.ClassEvent;
import nz.co.pearson.vuwexams.networking.models.Month;

public class MonthDownloader extends AsyncTask<Void, Void, List<ClassEvent>> {
    private Context context;
    private int month;
    private int year;
    private boolean error = false;
    private static int loadingCount = 0;
    private static DownloadCompleteListener eventListener;

    public interface DownloadCompleteListener {
        void notifyComplete();
    }


    public static void setEventListener(DownloadCompleteListener eventListener) {
        MonthDownloader.eventListener = eventListener;
    }

    public MonthDownloader(Context context, int month, int year) {
        this.context = context;
        this.month = month;
        this.year = year;
    }

    public static int getLoadingCount() {
        return loadingCount;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Realm r = MainActivity.getRealm();
        if (r.where(Month.class).equalTo("month", month).equalTo("year", year).findAll().size() > 0) {
            cancel(true);
        } else {
            r.beginTransaction();
            r.copyToRealm(new Month(month, year));
            r.commitTransaction();
            loadingCount++;
        }
    }

    @Override
    protected List<ClassEvent> doInBackground(Void... params) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = sharedPreferences.getString(context.getString(R.string.KEY_USERNAME), null);
        String password = sharedPreferences.getString(context.getString(R.string.KEY_PASSWORD), null);
        DataSource ds = new MyVicPortal(username, password);
        try {
            return ds.getEvents(month, year);
        } catch (DataSourceError e) {
            error = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<ClassEvent> events) {
        loadingCount--;
        if(!error) {
            Realm r = MainActivity.getRealm();
            r.beginTransaction();
            r.where(ClassEvent.class).equalTo("startMonth", month).equalTo("startYear", year).findAll().clear();
            r.copyToRealm(events);
            r.commitTransaction();
        }
        if(eventListener != null) {
            eventListener.notifyComplete();
        }
    }

    public static void removeEventListener() {
        eventListener = null;
    }
}