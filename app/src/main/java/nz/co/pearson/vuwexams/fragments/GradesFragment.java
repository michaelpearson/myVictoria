package nz.co.pearson.vuwexams.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import java.util.List;

import nz.co.pearson.vuwexams.R;
import nz.co.pearson.vuwexams.data.CourseModel;
import nz.co.pearson.vuwexams.data.Model;
import nz.co.pearson.vuwexams.fragments.pagers.YearPagerAdapter;
import nz.co.pearson.vuwexams.networking.Course;

public class GradesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Model.DataChangeListener {

    private AppBarLayout appBarLayout;
    private PagerSlidingTabStrip tabs;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager pagerView;
    private YearPagerAdapter yearPagerAdapter;

    @Override
    public void onResume() {
        CourseModel model = CourseModel.getInstance(this.getActivity());
        model.addChangeListener(this);
        swipeRefreshLayout.setRefreshing(model.isRefreshing());
        super.onResume();
    }

    @Override
    public void onStop() {
        CourseModel.getInstance(this.getActivity()).removeChangeListener(this);
        super.onStop();
    }

    public void onRefresh() {
        CourseModel cm = CourseModel.getInstance(this.getActivity());
        cm.refresh();
        swipeRefreshLayout.setRefreshing(cm.isRefreshing());
    }

    @Override
    public void onDataChanged(Model model) {
        yearPagerAdapter.notifyDataSetChanged();
        tabs.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(model.isRefreshing());
    }

    @Override
    public void refreshFailed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Failed to refresh data");
        builder.setTitle("Error");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        appBarLayout.removeView(tabs);
        pagerView.removeAllViews();
        pagerView.setAdapter(null);
        swipeRefreshLayout.setOnRefreshListener(null);
        super.onDestroyView();
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grades, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);
        pagerView = (ViewPager)view.findViewById(R.id.pager);
        swipeRefreshLayout.setOnRefreshListener(this);

        appBarLayout = (AppBarLayout)getActivity().findViewById(R.id.toolbar_layout);

        tabs = new PagerSlidingTabStrip(appBarLayout.getContext());
        tabs.setMinimumHeight((int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics())));
        tabs.setShouldExpand(true);
        tabs.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tabs.setTextColorResource(R.color.white);
        tabs.setIndicatorColorResource(R.color.light);

        appBarLayout.addView(tabs);

        yearPagerAdapter = new YearPagerAdapter(getChildFragmentManager(), getActivity());

        pagerView = (ViewPager)view.findViewById(R.id.pager);
        pagerView.setAdapter(yearPagerAdapter);
        pagerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override public void onPageSelected(int position) {}
            @Override public void onPageScrollStateChanged(int state) {
                swipeRefreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });
        tabs.setViewPager(pagerView);

        return(view);
    }
}
