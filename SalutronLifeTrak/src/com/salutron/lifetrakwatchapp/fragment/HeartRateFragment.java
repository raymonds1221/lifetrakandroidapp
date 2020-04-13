package com.salutron.lifetrakwatchapp.fragment;

import java.util.Calendar;
import java.util.Date;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.view.ViewPager;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.adapter.HeartRateAdapter;
import com.salutron.lifetrakwatchapp.util.CalendarDateChangeListener;

import roboguice.inject.InjectView;

public class HeartRateFragment extends BaseFragment implements CalendarDateChangeListener {
	@InjectView(R.id.pgrHeartRate) private ViewPager mHeartRatePager;
	private HeartRateAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_heart_rate, null);
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
        mHeartRatePager.setScrollX(0);
        toggleNavigationMenu();
    }
	
	private void initializeObjects() {
		Date date = getLifeTrakApplication().getCurrentDate();
		mAdapter = new HeartRateAdapter(getChildFragmentManager(), date);
		mHeartRatePager.setAdapter(mAdapter);
		mHeartRatePager.setOnPageChangeListener(mPageChangeListener);
		mHeartRatePager.setCurrentItem(1, false);
		mHeartRatePager.setOffscreenPageLimit(1);
	}
	
	private final ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
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
				HeartRateItemFragment fragment = (HeartRateItemFragment) mAdapter.instantiateItem(null, 1);
				Date date = getLifeTrakApplication().getCurrentDate();
				Date dateFrom = new Date();
				Date dateTo = new Date();
				int year = 0;
				
				MainActivity mainActivity = (MainActivity) getActivity();
				
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
					mainActivity.setCalendarDate(date);
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
					mainActivity.setCalendarDateWeek(dateFrom, dateTo);
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
					mainActivity.setCalendarDateWeek(dateFrom, dateTo);
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
				
				mainActivity.setCalendarMode(fragment.getCalendarMode());
				mHeartRatePager.setCurrentItem(1, false);
			}
		}
	};
	
	public void setDataWithDay(Date date) {
		HeartRateItemFragment fragment1 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 0);
		HeartRateItemFragment fragment2 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 1);
		HeartRateItemFragment fragment3 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 2);
		
		Calendar calYesterday = Calendar.getInstance();
		Calendar calNow = Calendar.getInstance();
		Calendar calTomorrow = Calendar.getInstance();
		
		calYesterday.setTime(date);
		calNow.setTime(date);
		calTomorrow.setTime(date);
		
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);
		calTomorrow.add(Calendar.DAY_OF_MONTH, 1);
		
		fragment1.setDataWithDate(calYesterday.getTime());
		fragment2.setDataWithDate(calNow.getTime());
		fragment3.setDataWithDate(calTomorrow.getTime());
	}
	
	public void setDataWithWeek(Date from, Date to) {
		HeartRateItemFragment fragment1 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 0);
		HeartRateItemFragment fragment2 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 1);
		HeartRateItemFragment fragment3 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 2);
		
		Calendar calLastWeekFrom = Calendar.getInstance();
		Calendar calLastWeekTo = Calendar.getInstance();
		
		calLastWeekFrom.setTime(from);
		calLastWeekFrom.add(Calendar.WEEK_OF_YEAR, -1);
		calLastWeekTo.setTime(calLastWeekFrom.getTime());
		calLastWeekTo.add(Calendar.DAY_OF_MONTH, 6);
		
		//fragment1.setDataWithDateRange(calLastWeekFrom.getTime(), calLastWeekTo.getTime(),  MODE_WEEK);
		//fragment2.setDataWithDateRange(from, to, MODE_WEEK);
		fragment1.setDataWithWeek(calLastWeekFrom.getTime(), calLastWeekTo.getTime());
		fragment2.setDataWithWeek(from, to);
		
		Calendar calNextWeekFrom = Calendar.getInstance();
		Calendar calNextWeekTo = Calendar.getInstance();
		
		calNextWeekFrom.setTime(from);
		calNextWeekFrom.add(Calendar.WEEK_OF_YEAR, 1);
		calNextWeekTo.setTime(calNextWeekFrom.getTime());
		calNextWeekTo.add(Calendar.DAY_OF_MONTH, 6);
		
		//fragment3.setDataWithDateRange(calNextWeekFrom.getTime(), calNextWeekTo.getTime(), MODE_WEEK);
		fragment3.setDataWithWeek(calNextWeekFrom.getTime(), calNextWeekTo.getTime());
	}
	
	public void setDataWithMonth(Date from, Date to) {
		HeartRateItemFragment fragment1 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 0);
		HeartRateItemFragment fragment2 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 1);
		HeartRateItemFragment fragment3 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 2);
		
		Date date1 = new Date();
		Date date2 = new Date();
		
		/*
		date1 = getLifeTrakApplication().getLastMonthFrom();
		date2 = getLifeTrakApplication().getLastMonthTo();
		
		fragment1.setDataWithDateRange(date1, date2, MODE_MONTH);
		fragment2.setDataWithDateRange(from, to, MODE_MONTH);
		
		date1 = getLifeTrakApplication().getNextMonthFrom();
		date2 = getLifeTrakApplication().getNextMonthTo();
		
		fragment3.setDataWithDateRange(date1, date2, MODE_MONTH);*/
		
		Calendar calLastFrom = Calendar.getInstance();
		Calendar calLastTo = Calendar.getInstance();
		Calendar calNextFrom = Calendar.getInstance();
		Calendar calNextTo = Calendar.getInstance();
		
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
		fragment1.setDataWithDateRange(date1, date2, MODE_MONTH);
		fragment2.setDataWithDateRange(from, to, MODE_MONTH);
		date1 = calNextFrom.getTime();
		date2 = calNextTo.getTime();
		fragment3.setDataWithDateRange(date1, date2, MODE_MONTH);
	}
	
	public void setDataWithYear(int year, HeartRateItemFragment fragment2) {
		HeartRateItemFragment fragment1 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 0);
		HeartRateItemFragment fragment3 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 2);
		
		fragment1.setDataWithYear(year - 1);
		fragment2.setDataWithYear(year);
		fragment3.setDataWithYear(year + 1);
	}
	
	public void setCalendarMode(int calendarMode) {
		HeartRateItemFragment fragment1 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 0);
		HeartRateItemFragment fragment2 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 2);
		
		fragment1.setCalendarMode(calendarMode);
		fragment2.setCalendarMode(calendarMode);
		
	}
	
	public int getCalendarMode() {
		HeartRateItemFragment fragment = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 1);
		return fragment.getCalendarMode();
	}

	/*
	 * (non-Javadoc)
	 * CalendarDateChangeListener methods
	 */
	
	@Override
	public void onCalendarDateChange(Date date) {
		setDataWithDay(date);
	}

	@Override
	public void onCalendarWeekChange(Date from, Date to) {
		setDataWithWeek(from, to);
	}

	@Override
	public void onCalendarMonthChange(Date from, Date to) {
		setDataWithMonth(from, to);
	}

	@Override
	public void onCalendarYearChange(final int year) {
		final HeartRateItemFragment fragment2 = (HeartRateItemFragment) mAdapter.instantiateItem(mHeartRatePager, 1);
		
		if (fragment2.orientation() == Configuration.ORIENTATION_PORTRAIT) {
			fragment2.getmHeartRateTopData().setVisibility(View.GONE);
			fragment2.getmHeartRatePlotContainer().setVisibility(View.GONE);
			fragment2.getmHeartRateLoadingText().setVisibility(View.VISIBLE);
		}
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				setDataWithYear(year, fragment2);
			}
		}, 500);
	}
}
