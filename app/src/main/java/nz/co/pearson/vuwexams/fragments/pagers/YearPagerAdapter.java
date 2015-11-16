package nz.co.pearson.vuwexams.fragments.pagers;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import nz.co.pearson.vuwexams.data.CourseModel;
import nz.co.pearson.vuwexams.fragments.NamedFragment;
import nz.co.pearson.vuwexams.fragments.YearGradesFragment;
import nz.co.pearson.vuwexams.networking.Course;

public class YearPagerAdapter extends FragmentStatePagerAdapter {
    private List<NamedFragment> fragments = new ArrayList<>();
    private Context context;
    private CourseModel courseModel;

    public YearPagerAdapter(FragmentManager fm, CourseModel model) {
        super(fm);
        this.courseModel = model;
        initFragments();
    }

    private void initFragments() {
        List<Course> courseList = courseModel.getData();

        if(courseList.size() == 0) {
            fragments.add(new NamedFragment() {
                @Override
                public String getTitle() {
                    return "Default";
                }
            });
            return;
        }

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
            @Override public int compare(Fragment lhs, Fragment rhs) {
                return ((YearGradesFragment) lhs).getYear() < ((YearGradesFragment) rhs).getYear() ? 1 : -1;
            }
        });
    }

    @Override
    public void notifyDataSetChanged() {
        fragments.clear();
        initFragments();
        super.notifyDataSetChanged();
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

    @Override
    public int getItemPosition(Object object) {
        return fragments.contains(object) ? POSITION_UNCHANGED : POSITION_NONE;
    }
}