package nz.co.pearson.vuwexams.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import nz.co.pearson.vuwexams.MainActivity;
import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.networking.models.Course;
import nz.co.pearson.vuwexams.networking.DataSource;
import nz.co.pearson.vuwexams.networking.MyVicPortal;
import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;

public class GradesFragment extends Fragment {
    private AppBarLayout appBarLayout;
    private PagerSlidingTabStrip tabs;
    private ViewPager viewPager;
    private YearPagerAdapter yearPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.timetable_options, menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new GradesDownloader(getActivity()).execute();
                return true;
            }
        });
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("Grades", "Create View");

        View view = inflater.inflate(R.layout.fragment_grades, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.pager);

        appBarLayout = (AppBarLayout)getActivity().findViewById(R.id.toolbar_layout);

        tabs = new PagerSlidingTabStrip(appBarLayout.getContext());
        tabs.setMinimumHeight((int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics())));
        tabs.setShouldExpand(true);
        tabs.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tabs.setTextColorResource(R.color.white);
        tabs.setIndicatorColorResource(R.color.light);

        appBarLayout.addView(tabs);

        yearPagerAdapter = new YearPagerAdapter(getChildFragmentManager());

        viewPager = (ViewPager)view.findViewById(R.id.pager);
        viewPager.setAdapter(yearPagerAdapter);
        tabs.setViewPager(viewPager);

        MainActivity.getRealm().addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                yearPagerAdapter.notifyDataSetChanged();
            }
        });

        return(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        appBarLayout.removeView(tabs);
        MainActivity.getRealm().removeAllChangeListeners();
    }

    class GradesDownloader extends AsyncTask<Void, Void, List<Course>> {
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
                List<Course> courses = ds.retrieveCourses();
                Realm r = Realm.getInstance(context);
                r.beginTransaction();
                r.where(Course.class).findAll().clear();
                r.commitTransaction();
                r.beginTransaction();
                r.copyToRealm(courses);
                r.commitTransaction();
                r.close();
            } catch (DataSourceError e) {
                error = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Course> courses) {
            try {
                pd.dismiss();
                if(error) {
                    AlertDialog ad = new AlertDialog.Builder(context).setTitle("Error").setMessage("Could not retrieve courses").setPositiveButton(android.R.string.ok, null).create();
                    ad.show();
                }
            } catch (IllegalArgumentException ignore) {}
        }
    }

    public class YearPagerAdapter extends FragmentStatePagerAdapter {
        private List<Integer> years = null;

        public YearPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putInt("year", years.get(position));
            String name = String.valueOf(years.get(position));
            if(name.equals("0")) {
                name = "No Data";
            }
            args.putString("title", name);
            Fragment f = new YearGradesFragment();
            f.setArguments(args);
            return f;
        }

        @Override
        public int getCount() {
            if(years == null) {
                years = new ArrayList<>();
                Realm realm = MainActivity.getRealm();
                List<Course> courseList = realm.where(Course.class).findAll();
                for(Course course : courseList) {
                    if(!years.contains(course.getYear())) {
                        years.add(course.getYear());
                    }
                }
                if(years.size() == 0) {
                    years.add(0);
                }
            }
            return years.size();
        }

        @Override
        public void notifyDataSetChanged() {
            years = null;
            tabs.notifyDataSetChanged();
            super.notifyDataSetChanged();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getItem(position).getArguments().getString("title", "");
        }
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }
}
