package nz.co.pearson.vuwexams.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import nz.co.pearson.vuwexams.MainActivity;
import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.networking.DataSource;
import nz.co.pearson.vuwexams.networking.MonthDownloader;
import nz.co.pearson.vuwexams.networking.MyVicPortal;
import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;
import nz.co.pearson.vuwexams.networking.models.ClassEvent;
import nz.co.pearson.vuwexams.networking.models.Month;

public class TimetableFragment extends Fragment implements MonthDownloader.DownloadCompleteListener {
    private WeekView timetable = null;
    private Animation rotationAnimation = null;
    private ImageView refreshButtonImage = null;
    private static final String KEY_DAYS_VISIBLE = "days_visible";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.timetable_options, menu);
        MenuItem refreshButton = menu.findItem(R.id.refresh);
        refreshButton.setActionView(R.layout.spinner_animation);
        rotationAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.refresh);
        rotationAnimation.setRepeatCount(Animation.INFINITE);
        refreshButtonImage = (ImageView)refreshButton.getActionView().findViewById(R.id.refresh_image);
        refreshButton.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Timetable", "Refresh clicked");
                Realm r = MainActivity.getRealm();
                r.beginTransaction();
                r.where(ClassEvent.class).findAll().clear();
                r.where(Month.class).findAll().clear();
                r.commitTransaction();
                timetable.notifyDatasetChanged();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.three_days:
                timetable.setNumberOfVisibleDays(3);
                break;
            case R.id.five_days:
                timetable.setNumberOfVisibleDays(5);
                break;
            case R.id.seven_days:
                timetable.setNumberOfVisibleDays(7);
                break;
            default:
                return false;
        }
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);
        timetable = (WeekView)view.findViewById(R.id.weekView);
        timetable.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Override
            public List<? extends WeekViewEvent> onMonthChange(final int newYear, final int newMonth) {
                List<WeekViewEvent> calendarEvents = new ArrayList<>();
                Log.i("Timetable", String.format("Month changed %d/%d", newMonth, newYear));
                Realm r = MainActivity.getRealm();
                if (r.where(Month.class).equalTo("month", newMonth).equalTo("year", newYear).findAll().size() > 0) {
                    List<ClassEvent> events = r.where(ClassEvent.class).equalTo("startYear", newYear).equalTo("startMonth", newMonth).findAll();
                    for (ClassEvent event : events) {
                        calendarEvents.add(ClassEvent.getEvent(event));
                    }
                } else {
                    new MonthDownloader(getActivity(), newMonth, newYear).execute();
                }

                if (MonthDownloader.getLoadingCount() > 0) {
                    if (refreshButtonImage.getAnimation() == null) {
                        refreshButtonImage.startAnimation(rotationAnimation);
                    }
                } else {
                    if (refreshButtonImage != null) {
                        refreshButtonImage.clearAnimation();
                    }
                }

                return calendarEvents;
            }
        });
        if(savedInstanceState != null) {
            timetable.setNumberOfVisibleDays(savedInstanceState.getInt(KEY_DAYS_VISIBLE, 5));
        } else {
            timetable.setNumberOfVisibleDays(5);
        }
        timetable.setNowLineColor(Color.RED);
        timetable.setNowLineThickness(10);
        timetable.setShowNowLine(true);

        MonthDownloader.setEventListener(this);

        return(view);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_DAYS_VISIBLE, timetable.getNumberOfVisibleDays());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        refreshButtonImage.clearAnimation();
        super.onDestroy();
    }

    @Override
    public void notifyComplete() {
        timetable.notifyDatasetChanged();
    }
}
