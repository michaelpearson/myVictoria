package nz.co.pearson.vuwexams.fragments;

import android.app.Fragment;
import android.os.Bundle;
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
import nz.co.pearson.vuwexams.MainActivity;
import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.networking.models.ClassEvent;
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
                List<ClassEvent> events = r.where(ClassEvent.class).equalTo("startYear", newYear).equalTo("startMonth", newMonth).findAll();
                List<WeekViewEvent> calendarEvents = new ArrayList<>();
                for (ClassEvent event : events) {
                    calendarEvents.add(ClassEvent.getEvent(event));
                }
                return calendarEvents;
            }
        });

        return(view);
    }


}
