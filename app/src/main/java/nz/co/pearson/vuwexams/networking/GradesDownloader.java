package nz.co.pearson.vuwexams.networking;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.util.List;

import io.realm.Realm;
import nz.co.pearson.vuwexams.MainActivity;
import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;
import nz.co.pearson.vuwexams.networking.models.Course;

public class GradesDownloader extends AsyncTask<Void, Void, List<Course>> {
    private Context context;
    private ProgressDialog pd;
    private boolean error = false;

    public GradesDownloader(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        pd = new ProgressDialog(context);
        pd.setTitle("Loading classes");
        pd.setMessage("Please wait while your classes and grades are downloaded");
        pd.setCancelable(false);
        pd.show();
    }

    @Override
    protected List<Course> doInBackground(Void... params) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = sharedPreferences.getString(context.getString(R.string.KEY_USERNAME), null);
        String password = sharedPreferences.getString(context.getString(R.string.KEY_PASSWORD), null);
        DataSource ds = new MyVicPortal(username, password);
        try {
            return ds.retrieveCourses();
        } catch (DataSourceError e) {
            error = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Course> courses) {
        Realm r = MainActivity.getRealm();
        r.beginTransaction();
        r.where(Course.class).findAll().clear();
        r.copyToRealm(courses);
        r.commitTransaction();

        try {
            pd.dismiss();
            if(error) {
                AlertDialog ad = new AlertDialog.Builder(context).setTitle("Error").setMessage("Could not retrieve courses").setPositiveButton(android.R.string.ok, null).create();
                ad.show();
            }
        } catch (IllegalArgumentException ignore) {}
    }
}