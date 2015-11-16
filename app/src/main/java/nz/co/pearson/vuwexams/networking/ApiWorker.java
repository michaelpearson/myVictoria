package nz.co.pearson.vuwexams.networking;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.List;

import io.realm.Realm;
import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.SettingsActivity;
import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;

public abstract class ApiWorker extends AsyncTask<Void, String, List<Course>> {
    private Context context;

    private DataSource datasource;
    private AlertDialog progressDialog;


    public ApiWorker(final Context context) {
        this.context = context;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = sharedPreferences.getString(context.getString(R.string.KEY_USERNAME), null);
        String password = sharedPreferences.getString(context.getString(R.string.KEY_PASSWORD), null);
        if(username == null || password == null || username.equals("") || password.equals(""))  {
            missingUsernameAndPassword();
        } else {
            datasource = new MyVicPortal(username, password);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showFeedback();
    }

    @Override
    protected List<Course> doInBackground(Void... params) {
        if(datasource == null) {
            return null;
        }
        try {
            datasource.authenticate();
            return datasource.retrieveCourses();
        } catch(DataSourceError e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if(values.length > 0) {
            postProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(List<Course> courses) {
        super.onPostExecute(courses);
        dismissFeedback();
        if(courses != null && courses.size() > 0) {
            Realm realm = Realm.getInstance(context);
            realm.beginTransaction();
            realm.clear(Course.class);
            realm.copyToRealm(courses);
            realm.commitTransaction();
            realm.close();
            dataReady();
        }
    }

    protected abstract void dataReady();

    public void showFeedback() {
        progressDialog = ProgressDialog.show(context, "Loading exam results", "Loading. Please wait...");
    }

    public void postProgress(String progress) {
        if(progressDialog != null) {
            progressDialog.setMessage(progress);
        }
    }

    public void dismissFeedback() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void missingUsernameAndPassword() {
        (new AlertDialog.Builder(context)).setMessage("Please enter your username and password").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(context, SettingsActivity.class);
                context.startActivity(i);
            }
        }).setNegativeButton(android.R.string.cancel, null).show();
    }
}
