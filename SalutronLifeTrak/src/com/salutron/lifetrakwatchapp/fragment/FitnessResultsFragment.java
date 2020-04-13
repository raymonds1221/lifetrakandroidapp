package com.salutron.lifetrakwatchapp.fragment;

import java.util.Calendar;
import java.util.Date;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.adapter.FitnessResultsAdapter;
import com.salutron.lifetrakwatchapp.util.CalendarDateChangeListener;

public class FitnessResultsFragment extends BaseFragment implements CalendarDateChangeListener {
	private ViewPager mFitnessResultsPager;
	private FitnessResultsAdapter mAdapter;
	private MainActivity mMainActivity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_fitness_results_main, null);
		mFitnessResultsPager = (ViewPager) view.findViewById(R.id.pgrFitnessResults);
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
        mFitnessResultsPager.setScrollX(0);
        toggleNavigationMenu();
    }
	
	private void initializeObjects() {
		int dashboardType = 0;
		
		mMainActivity = (MainActivity) getActivity();
		
		if(getArguments() != null) {
			dashboardType = getArguments().getInt(DASHBOARD_TYPE);
		}
		
		mAdapter = new FitnessResultsAdapter(getChildFragmentManager(),
													getLifeTrakApplication().getCurrentDate(), dashboardType);
		mFitnessResultsPager.setAdapter(mAdapter);
		mFitnessResultsPager.setOnPageChangeListener(mOnPageChangeListener);
		mFitnessResultsPager.setOffscreenPageLimit(1);
		mFitnessResultsPager.setCurrentItem(1, false);
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
			if(state == ViewPager.SCROLL_STATE_IDLE) {
				Date date = getLifeTrakApplication().getCurrentDate();
				Date dateFrom = new Date();
				Date dateTo = new Date();
				int year = 0;
				
				FitnessResultsItemFragment fragment = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 1);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				
				switch(fragment.getCalendarMode()) {
				case MODE_DAY:
					switch(mPosition) {
					case 0:
						date = getLifeTrakApplication().getPreviousDay();
						break;
					case 2:
						date = getLifeTrakApplication().getNextDay();
						break;
					}
					
					mMainActivity.setCalendarDate(date);
					break;
				case MODE_WEEK:
					switch(mPosition) {
					case 0:
						dateFrom = getLifeTrakApplication().getLastWeekFrom();
						dateTo = getLifeTrakApplication().getLastWeekTo();
						break;
					case 2:
						dateFrom = getLifeTrakApplication().getNextWeekFrom();
						dateTo = getLifeTrakApplication().getNextWeekTo();
						break;
					}
					getLifeTrakApplication().setCurrentDate(dateFrom);
					mMainActivity.setCalendarDateWeek(dateFrom, dateTo);
					break;
				case MODE_MONTH:
					switch(mPosition) {
					case 0:
						dateFrom = getLifeTrakApplication().getLastMonthFrom();
						dateTo = getLifeTrakApplication().getLastMonthTo();
						break;
					case 2:
						dateFrom = getLifeTrakApplication().getNextMonthFrom();
						dateTo = getLifeTrakApplication().getNextMonthTo();
						break;
					}
					
					getLifeTrakApplication().setCurrentDate(dateFrom);
					mMainActivity.setCalendarDateWeek(dateFrom, dateTo);
					break;
				case MODE_YEAR:
					switch(mPosition) {
					case 0:
						year = getLifeTrakApplication().getLastYear();
						break;
					case 2:
						year = getLifeTrakApplication().getNextYear();
						break;
					}
					Log.i(TAG, "year: " + year);
					break;
				}
				
				mMainActivity.setCalendarMode(fragment.getCalendarMode());
				mFitnessResultsPager.setCurrentItem(1, false);
			}
		}
	};
	
	public void setDataWithDate(Date date) {
		FitnessResultsItemFragment fragment1 = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 0);
		FitnessResultsItemFragment fragment2 = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 1);
		FitnessResultsItemFragment fragment3 = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 2);
		
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
	
	public void setDataWithDate(Date from, Date to, int calendarMode) {
		FitnessResultsItemFragment fragment1 = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 0);
		FitnessResultsItemFragment fragment2 = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 1);
		FitnessResultsItemFragment fragment3 = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 2);
		
		Date date1 = new Date();
		Date date2 = new Date();
		
		Calendar calLastFrom = Calendar.getInstance();
		Calendar calLastTo = Calendar.getInstance();
		Calendar calNextFrom = Calendar.getInstance();
		Calendar calNextTo = Calendar.getInstance();
		
		switch(calendarMode) {
		case MODE_WEEK:
			//date1 = getLifeTrakApplication().getLastWeekFrom();
			//date2 = getLifeTrakApplication().getLastWeekTo();
			calLastFrom.setTime(from);
			calLastFrom.add(Calendar.WEEK_OF_YEAR, -1);
			calLastTo.setTime(calLastFrom.getTime());
			calLastTo.add(Calendar.DAY_OF_MONTH, 6);
			
			calNextFrom.setTime(from);
			calNextFrom.add(Calendar.WEEK_OF_YEAR, 1);
			calNextTo.setTime(calNextFrom.getTime());
			calNextTo.add(Calendar.DAY_OF_MONTH, 6);
			
			date1 = calLastFrom.getTime();
			date2 = calLastTo.getTime();
			fragment1.setDataWithWeek(date1, date2);
			fragment2.setDataWithWeek(from, to);
			//date1 = getLifeTrakApplication().getNextWeekFrom();
			//date2 = getLifeTrakApplication().getNextWeekTo();
			date1 = calNextFrom.getTime();
			date2 = calNextTo.getTime();
			fragment3.setDataWithWeek(date1, date2);
			break;
		case MODE_MONTH:
			/*date1 = getLifeTrakApplication().getLastMonthFrom();
			date2 = getLifeTrakApplication().getLastMonthTo();
			fragment1.setDataWithMonth(date1, date2);
			fragment2.setDataWithMonth(from, to);
			date1 = getLifeTrakApplication().getNextMonthFrom();
			date2 = getLifeTrakApplication().getNextMonthTo();
			fragment3.setDataWithMonth(date1, date2);*/
			
			calLastFrom.setTime(from);
			calLastFrom.add(Calendar.MONTH, -1);
			calLastTo.setTime(calLastFrom.getTime());
			int maxDays = calLastFrom.getActualMaximum(Calendar.DAY_OF_MONTH);
			calLastTo.add(Calendar.DAY_OF_MONTH, maxDays - 1);
			
			calNextFrom.setTime(from);
			calNextFrom.add(Calendar.MONTH, 1);
			calNextTo.setTime(calNextFrom.getTime());
			maxDays = calLastFrom.getActualMaximum(Calendar.DAY_OF_MONTH);
			calNextTo.add(Calendar.DAY_OF_MONTH, maxDays - 1);
			
			date1 = calLastFrom.getTime();
			date2 = calLastTo.getTime();
			fragment1.setDataWithMonth(date1, date2);
			fragment2.setDataWithMonth(from, to);
			date1 = calNextFrom.getTime();
			date2 = calNextTo.getTime();
			fragment3.setDataWithMonth(date1, date2);
			break;
		}
	}
	
	public void setDataWithDate(int year, FitnessResultsItemFragment fragment2) {
		FitnessResultsItemFragment fragment1 = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 0);
		FitnessResultsItemFragment fragment3 = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 2);
		
		fragment1.setDataWithYear(year - 1);
		fragment2.setDataWithYear(year);
		fragment3.setDataWithYear(year + 1);
	}
	
	public void setCalendarMode(int calendarMode) {
		FitnessResultsItemFragment fragment1 = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 0);
		FitnessResultsItemFragment fragment2 = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 2);
		
		fragment1.setCalendarMode(calendarMode);
		fragment2.setCalendarMode(calendarMode);
	}
	
	public int getCalendarMode() {
		FitnessResultsItemFragment fragment = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 1);
		return fragment.getCalendarMode();
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
		setDataWithDate(from, to, MODE_WEEK);
	}

	@Override
	public void onCalendarMonthChange(Date from, Date to) {
		setDataWithDate(from, to, MODE_MONTH);
	}

	@Override
	public void onCalendarYearChange(final int year) {
		final FitnessResultsItemFragment fragment2 = (FitnessResultsItemFragment) mAdapter.instantiateItem(mFitnessResultsPager, 1);
		
		if (fragment2.orientation() == Configuration.ORIENTATION_PORTRAIT) {
			fragment2.getmFitnessResultsTopData().setVisibility(View.GONE);
			fragment2.getmFitnessResultsPlotContainer().setVisibility(View.GONE);
			fragment2.getmFitnessResultsLoadingText().setVisibility(View.VISIBLE);
			fragment2.getmFitnessResultsCenterContainer().setVisibility(View.GONE);
		}
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				setDataWithDate(year, fragment2);
			}
		}, 500);
	}
}
