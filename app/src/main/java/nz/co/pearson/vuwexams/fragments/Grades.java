package nz.co.pearson.vuwexams.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.design.widget.AppBarLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.networking.ApiWorker;
import nz.co.pearson.vuwexams.networking.Course;

/**
 * Created by michael on 14/11/15.
 */
public class Grades extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private AppBarLayout appBarLayout;
    private PagerSlidingTabStrip tabs;
    private Realm realm;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager pagerView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_grades, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        realm = Realm.getInstance(this.getActivity());
        List<Course> courses = realm.where(Course.class).findAll();

        if(courses.size() > 0) {
            initDisplay(view, courses);
        } else {
            ApiWorker worker = new ApiWorker(this.getActivity()) {
                @Override
                protected void dataReady(List<Course> courses) {
                    if(courses == null) {
                        return;
                    }
                    realm.beginTransaction();
                    realm.copyToRealm(courses);
                    realm.commitTransaction();
                    initDisplay(view, courses);
                }
            };
            worker.execute();
        }
        return(view);
    }

    public void onRefresh() {
        new ApiWorker(getActivity(), null) {
            @Override
            protected void dataReady(List<Course> courses) {
                swipeRefreshLayout.setRefreshing(false);
                if(courses == null) {
                    return;
                }
                pagerView.removeAllViews();
                pagerView.setAdapter(new YearPager(getFragmentManager(), courses));

            }
        }.execute();
    }

    private void initDisplay(View view, List<Course> courseList) {
        AppCompatActivity activity = (AppCompatActivity)getActivity();

        appBarLayout = (AppBarLayout)activity.findViewById(R.id.toolbar_layout);
        tabs = new PagerSlidingTabStrip(appBarLayout.getContext());
        tabs.setMinimumHeight((int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics())));
        appBarLayout.addView(tabs);

        tabs.setShouldExpand(true);
        tabs.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tabs.setTextColorResource(R.color.white);
        tabs.setIndicatorColorResource(R.color.light);

        YearPager pageAdapter = new YearPager(getFragmentManager(), courseList);
        pagerView = (ViewPager) view.findViewById(R.id.pager);
        pagerView.setAdapter(pageAdapter);
        tabs.setViewPager(pagerView);

        pagerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                swipeRefreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });
    }

    @Override
    public void onDestroy() {
        appBarLayout.removeView(tabs);
        pagerView.removeAllViews();
        realm.close();
        super.onDestroy();
    }

    class YearPager extends FragmentStatePagerAdapter {
        private List<YearGradesFragment> fragments = new ArrayList<>();

        public YearPager(FragmentManager fm, List<Course> courseList) {
            super(fm);
            Map<Integer, List<Course>> courseMap = new HashMap<>();
            for(Course course : courseList) {
                List<Course> yearCourses = courseMap.get(course.getYear());
                if(yearCourses == null) {
                    yearCourses = new ArrayList<Course>();
                    courseMap.put(course.getYear(), yearCourses);
                }
                yearCourses.add(course);
            }
            for(Map.Entry<Integer, List<Course>> entry : courseMap.entrySet()) {
                YearGradesFragment fragment = new YearGradesFragment();
                fragment.setCourses(entry.getValue());
                fragments.add(fragment);
            }
            Collections.sort(fragments, new Comparator<YearGradesFragment>() {
                @Override
                public int compare(YearGradesFragment lhs, YearGradesFragment rhs) {
                    return lhs.getYear() < rhs.getYear() ? 1 : -1;
                }
            });
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return ((YearGradesFragment)getItem(position)).getTitle();
        }
    }
}
