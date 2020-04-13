package com.salutron.lifetrakwatchapp.fragment;

import java.util.Calendar;
import java.util.Date;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.adapter.SleepDataAdapter;
import com.salutron.lifetrakwatchapp.util.CalendarDateChangeListener;

import roboguice.inject.InjectView;

public class SleepDataFragment extends BaseFragment implements CalendarDateChangeListener {
	@InjectView(R.id.pgrSleepData) private ViewPager mSleepDataPager;
	private SleepDataAdapter mAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sleep_data, null);
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Date date = new Date();
		
//		if(getLifeTrakApplication().getCurrentDate().after(date)) {
//			setHasOptionsMenu(false);
//		} else {
//			setHasOptionsMenu(true);
//		}
		setHasOptionsMenu(false);
	}

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mSleepDataPager.setScrollX(0);
        toggleNavigationMenu();
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(getActivity() != null) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		}
		
		initializeObjects();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		//inflater.inflate(R.menu.menu_add_sleep_log, menu);
	}
	
	private void initializeObjects() {
		Date date = getLifeTrakApplication().getCurrentDate();
		mAdapter = new SleepDataAdapter(getChildFragmentManager(), date);
		mSleepDataPager.setAdapter(mAdapter);
		mSleepDataPager.setOnPageChangeListener(mOnPageChangeListener);
		mSleepDataPager.setOffscreenPageLimit(1);
		mSleepDataPager.setCurrentItem(1, false);
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
				mSleepDataPager.setCurrentItem(1, false);
			}
		}
	};

	@Override
	public void onCalendarDateChange(Date date) {
		SleepDataItemFragment fragment1 = (SleepDataItemFragment) mAdapter.instantiateItem(mSleepDataPager, 0);
		SleepDataItemFragment fragment2 = (SleepDataItemFragment) mAdapter.instantiateItem(mSleepDataPager, 1);
		SleepDataItemFragment fragment3 = (SleepDataItemFragment) mAdapter.instantiateItem(mSleepDataPager, 2);
		
		Calendar calYesterday = Calendar.getInstance();
		Calendar calNow = Calendar.getInstance();
		Calendar calTomorrow = Calendar.getInstance();
		
		calYesterday.setTime(date);
		calNow.setTime(date);
		calTomorrow.setTime(date);
		
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);
		calTomorrow.add(Calendar.DAY_OF_MONTH, 1);
		
		fragment1.setDate(calYesterday.getTime());
		fragment2.setDate(calNow.getTime());
		fragment3.setDate(calTomorrow.getTime());
		
		fragment1.setDataForDay(calYesterday.getTime());
		fragment2.setDataForDay(calNow.getTime());
		fragment3.setDataForDay(calTomorrow.getTime());
		
		Date now = new Date();
		if(date.after(now)) {
			setHasOptionsMenu(false);
		} else {
			setHasOptionsMenu(true);
		}
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
