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
import com.salutron.lifetrakwatchapp.adapter.ActigraphyAdapter;
import com.salutron.lifetrakwatchapp.adapter.ActigraphyAdapterR420;
import com.salutron.lifetrakwatchapp.util.CalendarDateChangeListener;

import java.util.Calendar;
import java.util.Date;

public class ActigraphyFragmentR420 extends BaseFragment implements CalendarDateChangeListener {
	private ViewPager mActigraphyPager;
	private ActigraphyAdapterR420 mAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_actigraphy, null);
		
		mActigraphyPager = (ViewPager) view.findViewById(R.id.pgrActigraphy);
		
		return view;
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActigraphyPager.setScrollX(0);
        toggleNavigationMenu();
    }
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(getActivity() != null) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		}
		
		initializeObjects();
		showCalendar();
	}
	
	private void initializeObjects() {
		Date date = getLifeTrakApplication().getCurrentDate();
		mAdapter = new ActigraphyAdapterR420(getChildFragmentManager(), date);
		mActigraphyPager.setAdapter(mAdapter);
		mActigraphyPager.setOnPageChangeListener(mPageChangeListener);
		mActigraphyPager.setOffscreenPageLimit(1);
		mActigraphyPager.setCurrentItem(1, false);
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
				mActigraphyPager.setCurrentItem(1, false);
			}
		}
	};
	
	public void setDataWithDate(Date date) {
		ActigraphyItemFragmentR420 fragment1 = (ActigraphyItemFragmentR420) mAdapter.instantiateItem(mActigraphyPager, 0);
		ActigraphyItemFragmentR420 fragment2 = (ActigraphyItemFragmentR420) mAdapter.instantiateItem(mActigraphyPager, 1);
		ActigraphyItemFragmentR420 fragment3 = (ActigraphyItemFragmentR420) mAdapter.instantiateItem(mActigraphyPager, 2);
		
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
	}
	
	private void setDataWithDate(Date from, Date to, int calendarMode) {
		ActigraphyItemFragmentR420 fragment1 = (ActigraphyItemFragmentR420) mAdapter.instantiateItem(mActigraphyPager, 0);
		ActigraphyItemFragmentR420 fragment2 = (ActigraphyItemFragmentR420) mAdapter.instantiateItem(mActigraphyPager, 1);
		ActigraphyItemFragmentR420 fragment3 = (ActigraphyItemFragmentR420) mAdapter.instantiateItem(mActigraphyPager, 2);
		
		Date date1 = new Date();
		Date date2 = new Date();
		
		switch(calendarMode) {
		case MODE_WEEK:
			date1 = getLifeTrakApplication().getLastWeekFrom();
			date2 = getLifeTrakApplication().getLastWeekTo();
			fragment1.setDataWithRange(date1, date2, MODE_WEEK);
			fragment2.setDataWithRange(from, to, MODE_WEEK);
			date1 = getLifeTrakApplication().getLastMonthFrom();
			date2 = getLifeTrakApplication().getLastMonthTo();
			fragment3.setDataWithRange(date1, date2, MODE_WEEK);
			break;
		case MODE_MONTH:
			date1 = getLifeTrakApplication().getLastMonthFrom();
			date2 = getLifeTrakApplication().getLastMonthTo();
			fragment1.setDataWithRange(date1, date2, MODE_MONTH);
			fragment2.setDataWithRange(from, to, MODE_MONTH);
			date1 = getLifeTrakApplication().getNextMonthFrom();
			date2 = getLifeTrakApplication().getNextMonthTo();
			fragment3.setDataWithRange(date1, date2, MODE_MONTH);
			break;
		}
	}
	
	private void setDataWithYear(int year) {
		//ActigraphyItemFragment fragment1 = (ActigraphyItemFragment) mAdapter.instantiateItem(null, 0);
		ActigraphyItemFragmentR420 fragment2 = (ActigraphyItemFragmentR420) mAdapter.instantiateItem(mActigraphyPager, 1);
		//ActigraphyItemFragment fragment3 = (ActigraphyItemFragment) mAdapter.instantiateItem(null, 2);
		
		//fragment1.setDataWithYear(year);
		fragment2.setDataWithYear(year);
		//fragment3.setDataWithYear(year);
	}

	/*
	 * CalendarDateChangeListener methods
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
	public void onCalendarYearChange(int year) {
		setDataWithYear(year);
	}
}
