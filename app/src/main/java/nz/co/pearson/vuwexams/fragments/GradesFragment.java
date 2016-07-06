package nz.co.pearson.vuwexams.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
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
import nz.co.pearson.vuwexams.networking.GradesDownloader;

public class GradesFragment extends Fragment {
    private AppBarLayout appBarLayout;
    private PagerSlidingTabStrip tabs;
    private YearPagerAdapter yearPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.grades_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.refresh) {
            new GradesDownloader(getActivity()).execute();
            return(true);
        }
        return false;
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("Grades", "Create View");

        View view = inflater.inflate(R.layout.fragment_grades, container, false);

        appBarLayout = (AppBarLayout)getActivity().findViewById(R.id.toolbar_layout);

        tabs = new PagerSlidingTabStrip(appBarLayout.getContext());
        tabs.setShouldExpand(true);
        tabs.setBackgroundColor(ContextCompat.getColor(appBarLayout.getContext(), R.color.colorPrimary));
        tabs.setTextColorResource(R.color.white);
        tabs.setIndicatorColorResource(R.color.light);

        appBarLayout.addView(tabs, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics())) ));

        yearPagerAdapter = new YearPagerAdapter(getChildFragmentManager());

        ViewPager viewPager = (ViewPager)view.findViewById(R.id.pager);
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
