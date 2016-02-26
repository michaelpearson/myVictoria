package nz.co.pearson.vuwexams.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import nz.co.pearson.vuwexams.MainActivity;
import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.networking.DataSource;
import nz.co.pearson.vuwexams.networking.MyVicPortal;
import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;
import nz.co.pearson.vuwexams.networking.models.ClassEvent;
import nz.co.pearson.vuwexams.networking.models.Course;
import nz.co.pearson.vuwexams.networking.models.Month;

public class TimetableFragment extends Fragment {
    private WeekView timetable = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.timetable_options, menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i("Timetable", "Refresh clicked");
                Realm r = MainActivity.getRealm();
                r.beginTransaction();
                r.where(ClassEvent.class).findAll().clear();
                r.where(Month.class).findAll().clear();
                r.commitTransaction();
                timetable.notifyDatasetChanged();
                return true;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);
        timetable = (WeekView)view.findViewById(R.id.weekView);
        timetable.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Override
            public List<? extends WeekViewEvent> onMonthChange(final int newYear, final int newMonth) {
                Log.i("Timetable", String.format("Month changed %d/%d", newMonth, newYear));
                Realm r = MainActivity.getRealm();
                if(r.where(Month.class).equalTo("month", newMonth).equalTo("year", newYear).findAll().size() > 0) {
                    List<ClassEvent> events = r.where(ClassEvent.class).equalTo("startYear", newYear).equalTo("startMonth", newMonth).findAll();
                    List<WeekViewEvent> calendarEvents = new ArrayList<>();
                    for (ClassEvent event : events) {
                        calendarEvents.add(ClassEvent.getEvent(event));
                    }
                    return calendarEvents;
                } else {
                    new MonthDownloader(getActivity(), newMonth, newYear).execute();
                    return new ArrayList<WeekViewEvent>();
                }
            }
        });
        MainActivity.getRealm().addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                timetable.notifyDatasetChanged();
            }
        });
        return(view);
    }

    @Override
    public void onDestroyView() {
        MainActivity.getRealm().removeAllChangeListeners();
        super.onDestroyView();
    }

    private class MonthDownloader extends AsyncTask<Void, Void, Void> {
        private Context context;
        private int month;
        private int year;
        private boolean error = false;

        public MonthDownloader(Context context, int month, int year) {
            this.context = context;
            this.month = month;
            this.year = year;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String username = sharedPreferences.getString(context.getString(R.string.KEY_USERNAME), null);
            String password = sharedPreferences.getString(context.getString(R.string.KEY_PASSWORD), null);
            DataSource ds = new MyVicPortal(username, password);
            try {
                List<ClassEvent> events = ds.getEvents(month, year);
                Realm r = Realm.getInstance(context);
                r.beginTransaction();
                r.where(ClassEvent.class).equalTo("month", month).equalTo("year", year).findAll().clear();
                r.copyToRealm(new Month(month, year));
                r.copyToRealm(events);
                r.commitTransaction();
                r.close();
            } catch (DataSourceError e) {
                error = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
