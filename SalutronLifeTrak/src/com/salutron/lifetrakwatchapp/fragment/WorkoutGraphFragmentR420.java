package com.salutron.lifetrakwatchapp.fragment;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.adapter.WorkoutGraphAdapter;
import com.salutron.lifetrakwatchapp.adapter.WorkoutGraphAdapterR420;
import com.salutron.lifetrakwatchapp.util.CalendarDateChangeListener;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Janwel Ocampo on 11/10/2015.
 */
public class WorkoutGraphFragmentR420 extends BaseFragment implements CalendarDateChangeListener {
    private ViewPager mWorkoutGraphPager;
    private WorkoutGraphAdapterR420 mAdapter;
    private MainActivity mMainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_workout_graph_main, null);
        mWorkoutGraphPager = (ViewPager) view.findViewById(R.id.workoutgraphpager);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }

        initializeObjects();
        showCalendar();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mWorkoutGraphPager.setScrollX(0);
        toggleNavigationMenu();
    }

    private void initializeObjects() {
        int dashboardType = 0;

        mMainActivity = (MainActivity) getActivity();

        if(getArguments() != null) {
            dashboardType = getArguments().getInt(DASHBOARD_TYPE);
        }

        mAdapter = new WorkoutGraphAdapterR420(getChildFragmentManager(),
                getLifeTrakApplication().getCurrentDate(), dashboardType);
        mWorkoutGraphPager.setAdapter(mAdapter);
        mWorkoutGraphPager.setOnPageChangeListener(mOnPageChangeListener);
        mWorkoutGraphPager.setOffscreenPageLimit(1);
        mWorkoutGraphPager.setCurrentItem(1, false);
    }

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        private int mPosition;

        @Override
        public void onPageSelected(int state) {

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            mPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if(state == ViewPager.SCROLL_STATE_IDLE && isAdded()) {
                Date date = getLifeTrakApplication().getCurrentDate();

                switch(mPosition) {
                    case 0:
                        date = getLifeTrakApplication().getPreviousDay();
                        break;
                    case 2:
                        date = getLifeTrakApplication().getNextDay();
                        break;
                }

                ((MainActivity) getActivity()).setCalendarDate(date);
                ((MainActivity) getActivity()).setCalendarMode(MODE_DAY);
                mWorkoutGraphPager.setCurrentItem(1, false);
            }

        }
    };

    public void setDataWithDate(Date date) {
        WorkoutGraphItemFragmentR420 fragment1 = (WorkoutGraphItemFragmentR420) mAdapter.instantiateItem(mWorkoutGraphPager, 0);
        WorkoutGraphItemFragmentR420 fragment2 = (WorkoutGraphItemFragmentR420) mAdapter.instantiateItem(mWorkoutGraphPager, 1);
        WorkoutGraphItemFragmentR420 fragment3 = (WorkoutGraphItemFragmentR420) mAdapter.instantiateItem(mWorkoutGraphPager, 2);

        Calendar calYesterday = Calendar.getInstance();
        Calendar calNow = Calendar.getInstance();
        Calendar calTomorrow = Calendar.getInstance();

        calYesterday.setTime(date);
        calNow.setTime(date);
        calTomorrow.setTime(date);

        calYesterday.add(Calendar.DAY_OF_MONTH, -1);
        calTomorrow.add(Calendar.DAY_OF_MONTH, 1);

        fragment1.setDataWithDay(calYesterday.getTime());
        fragment2.setDataWithDay(calNow.getTime());
        fragment3.setDataWithDay(calTomorrow.getTime());

    }

    /*
     * CalendarDateChangeListener Methods
     */
    @Override
    public void onCalendarDateChange(Date date) {
        setDataWithDate(date);
    }

    @Override
    public void onCalendarWeekChange(Date from, Date to) {
    }

    @Override
    public void onCalendarMonthChange(Date from, Date to) {
    }

    @Override
    public void onCalendarYearChange(int year) {
    }
}