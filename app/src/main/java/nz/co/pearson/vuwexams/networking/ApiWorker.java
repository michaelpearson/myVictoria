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

import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.SettingsActivity;
import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;

public abstract class ApiWorker extends AsyncTask<Void, String, List<Course>> {
    private Context context;

    private DataSource datasource;
    private FeedbackStrategy feedback;

    public interface FeedbackStrategy {
        void show();
        void postProgress(String progress);
        void dismiss();
    }

    public ApiWorker(final Context context) {
        this(context, new FeedbackStrategy() {
            private ProgressDialog progressDialog;
            @Override
            public void show() {
                progressDialog = ProgressDialog.show(context, "Loading exam results", "Loading. Please wait...");
            }
            @Override
            public void postProgress(String progress) {
                progressDialog.setMessage(progress);
            }
            @Override
            public void dismiss() {
                progressDialog.dismiss();
            }
        });
    }
    public ApiWorker(final Context context, FeedbackStrategy feedback) {
        this.context = context;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = sharedPreferences.getString(context.getString(R.string.KEY_USERNAME), null);
        String password = sharedPreferences.getString(context.getString(R.string.KEY_PASSWORD), null);
        if(username == null || password == null || username.equals("") || password.equals(""))  {
            (new AlertDialog.Builder(context)).setMessage("Please enter your username and password").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(context, SettingsActivity.class);
                    context.startActivity(i);
                }
            }).setNegativeButton(android.R.string.cancel, null).show();
        } else {
            datasource = new MyVicPortal(username, password);
            this.feedback = feedback;
        }
    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(feedback != null) {
            feedback.show();
        }
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
        if(values.length > 0 && feedback != null) {
            feedback.postProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(List<Course> courses) {
        super.onPostExecute(courses);
        if(feedback != null) {
            feedback.dismiss();
        }
        dataReady(courses);
    }

    protected abstract void dataReady(@Nullable List<Course> courses);
}
