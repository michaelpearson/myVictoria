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

import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.data.ClassEventModel;
import nz.co.pearson.vuwexams.data.Model;
import nz.co.pearson.vuwexams.networking.ClassEvent;

public class TimetableFragment extends Fragment {
    private ClassEventModel classEvents = null;
    private WeekView timetable = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        classEvents = new ClassEventModel(getActivity());
        classEvents.addChangeListener(new Model.DataChangeListener() {
            @Override
            public void onDataChanged(Model model) {
                Log.i("Timetable", "Data changed");
                timetable.notifyDatasetChanged();
            }
            @Override
            public void refreshFailed() {
                Log.e("Timetable", "Refresh failed");
            }
        });
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        classEvents.destroy();
        classEvents = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.timetable_options, menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i("Timetable", "Refresh clicked");
                classEvents.refresh();
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
                classEvents.downloadMonth(newYear, newMonth);
                List<WeekViewEvent> events = new ArrayList<>();
                for (ClassEvent event : classEvents.matchEvents(newYear, newMonth)) {
                    events.add(ClassEvent.getEvent(event));
                    Log.i("Timetable", "Added event");
                }
                return events;
            }
        });


        return(view);
    }


}
