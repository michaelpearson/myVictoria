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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;

import io.realm.Realm;
import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.networking.ApiWorker;
import nz.co.pearson.vuwexams.networking.Course;

/**
 * Created by michael on 14/11/15.
 */
public class GradesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private AppBarLayout appBarLayout;
    private PagerSlidingTabStrip tabs;
    private Realm realm;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager pagerView;
    private boolean dataLoaded = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grades, container, false);
        realm = Realm.getInstance(this.getActivity());
        initDisplay(view);
        return(view);
    }

    public void onRefresh() {
        new ApiWorker(getActivity()) {
            @Override
            protected void dataReady(List<Course> courses) {
                swipeRefreshLayout.setRefreshing(false);
                pagerView.setAdapter(new YearPager(getFragmentManager(), courses));
                tabs.setViewPager(pagerView);
            }
            @Override
            public void showFeedback() {
                if(!dataLoaded) {
                    super.showFeedback();
                }
            }

        }.execute();
    }


    @Override
    public void onResume() {
        super.onResume();
        if(!dataLoaded) {
            onRefresh();
        }
    }

    private void initDisplay(final View view) {
        List<Course> courses = realm.where(Course.class).findAll();
        initDisplay(view, courses);
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
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {}
            @Override
            public void onPageScrollStateChanged(int state) {
                swipeRefreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
        appBarLayout.removeView(tabs);
        pagerView.removeAllViews();
    }

    class YearPager extends FragmentStatePagerAdapter {
        private List<NamedFragment> fragments = new ArrayList<>();

        public YearPager(FragmentManager fm, List<Course> courseList) {
            super(fm);
            if(courseList == null || courseList.size() == 0) {
                dataLoaded = false;
                NamedFragment f = new YearGradesFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(YearGradesFragment.KEY_YEAR, Calendar.getInstance().get(Calendar.YEAR));
                f.setArguments(bundle);
                fragments.add(f);
                return;
            }
            dataLoaded = true;
            Set<Integer> years = new HashSet<>(64);
            for(Course course : courseList) {
                if(years.add(course.getYear())) {
                    YearGradesFragment f = new YearGradesFragment();
                    Bundle args = new Bundle();
                    args.putInt(YearGradesFragment.KEY_YEAR, course.getYear());
                    f.setArguments(args);
                    fragments.add(f);
                }
            }
            Collections.sort(fragments, new Comparator<Fragment>() {
                @Override
                public int compare(Fragment lhs, Fragment rhs) {
                    return ((YearGradesFragment)lhs).getYear() < ((YearGradesFragment)rhs).getYear() ? 1 : -1;
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
            return ((NamedFragment)getItem(position)).getTitle();
        }
    }
}
