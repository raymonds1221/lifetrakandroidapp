package com.salutron.lifetrakwatchapp.fragment;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.adapter.HeartRateAdapter;
import com.salutron.lifetrakwatchapp.adapter.HeartRateAdapterR420;
import com.salutron.lifetrakwatchapp.util.CalendarDateChangeListener;

import java.util.Calendar;
import java.util.Date;

import roboguice.inject.InjectView;

public class HeartRateFragmentR420 extends BaseFragment implements CalendarDateChangeListener {
	@InjectView(R.id.pgrHeartRate) private ViewPager mHeartRatePager;
	private HeartRateAdapterR420 mAdapter;

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
		mAdapter = new HeartRateAdapterR420(getChildFragmentManager(), date);
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
				HeartRateItemFragmentR420 fragment = (HeartRateItemFragmentR420) mAdapter.instantiateItem(null, 1);
				Date date = getLifeTrakApplication().getCurrentDate();
				Date dateFrom = new Date();
				Date dateTo = new Date();
				int year = 0;
				
				MainActivity mainActivity = (MainActivity) getActivity();
				


					switch(mPosition) {
					case 0:
						date = getLifeTrakApplication().getPreviousDay();
						break;
					case 2:
						date = getLifeTrakApplication().getNextDay();
						break;
					}
					mainActivity.setCalendarDate(date);

				mainActivity.setCalendarMode(MODE_DAY);
				mHeartRatePager.setCurrentItem(1, false);
			}
		}
	};
	
	public void setDataWithDay(Date date) {
		HeartRateItemFragmentR420 fragment1 = (HeartRateItemFragmentR420) mAdapter.instantiateItem(mHeartRatePager, 0);
		HeartRateItemFragmentR420 fragment2 = (HeartRateItemFragmentR420) mAdapter.instantiateItem(mHeartRatePager, 1);
		HeartRateItemFragmentR420 fragment3 = (HeartRateItemFragmentR420) mAdapter.instantiateItem(mHeartRatePager, 2);
		
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
	

	public void setCalendarMode(int calendarMode) {
		HeartRateItemFragmentR420 fragment1 = (HeartRateItemFragmentR420) mAdapter.instantiateItem(mHeartRatePager, 0);
		HeartRateItemFragmentR420 fragment2 = (HeartRateItemFragmentR420) mAdapter.instantiateItem(mHeartRatePager, 2);

		fragment1.setCalendarMode(calendarMode);
		fragment2.setCalendarMode(calendarMode);

	}
	
	public int getCalendarMode() {
		HeartRateItemFragmentR420 fragment = (HeartRateItemFragmentR420) mAdapter.instantiateItem(mHeartRatePager, 1);
		return MODE_DAY;

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
//		setDataWithWeek(from, to);
	}

	@Override
	public void onCalendarMonthChange(Date from, Date to) {
//		setDataWithMonth(from, to);
	}

	@Override
	public void onCalendarYearChange(final int year) {
//		final HeartRateItemFragmentR420 fragment2 = (HeartRateItemFragmentR420) mAdapter.instantiateItem(mHeartRatePager, 1);
//
//		if (fragment2.orientation() == Configuration.ORIENTATION_PORTRAIT) {
//			fragment2.getmHeartRateTopData().setVisibility(View.GONE);
//			fragment2.getmHeartRatePlotContainer().setVisibility(View.GONE);
//			fragment2.getmHeartRateLoadingText().setVisibility(View.VISIBLE);
//		}
//
//		Handler handler = new Handler();
//		handler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				setDataWithYear(year, fragment2);
//			}
//		}, 500);
	}
}
