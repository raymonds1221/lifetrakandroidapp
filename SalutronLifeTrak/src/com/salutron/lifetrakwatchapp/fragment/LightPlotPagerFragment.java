package com.salutron.lifetrakwatchapp.fragment;

import java.util.Calendar;
import java.util.Date;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.adapter.LightPlotPagerAdapter;
import com.salutron.lifetrakwatchapp.util.CalendarDateChangeListener;

public class LightPlotPagerFragment extends BaseFragment implements CalendarDateChangeListener {

	private ViewPager mPager;
	private LightPlotPagerAdapter mLightPlotAdapter;
    private int mPosition;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_light_plot_pager, null);
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(false);

	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(getActivity() != null) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		}
		mPager = (ViewPager) getView().findViewById(R.id.pager);
		initializeObjects();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_add_sleep_log, menu);
	}
	
	private void initializeObjects() {
		Date date = getLifeTrakApplication().getCurrentDate();
		
		mLightPlotAdapter = new LightPlotPagerAdapter(getChildFragmentManager(), date);
		mPager.setAdapter(mLightPlotAdapter);
		mPager.setOnPageChangeListener(mOnPageChangeListener);
		mPager.setOffscreenPageLimit(1);
		mPager.setCurrentItem(1, false);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

        mPager.setScrollX(0);
        toggleNavigationMenu();
	}
	
	private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {

		
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
				mPager.setCurrentItem(1, false);
			}
		}
	};

	@Override
	public void onCalendarDateChange(Date date) {
		LightPlotFragment fragment1 = (LightPlotFragment) mLightPlotAdapter.instantiateItem(mPager, 0);
		LightPlotFragment fragment2 = (LightPlotFragment) mLightPlotAdapter.instantiateItem(mPager, 1);
		LightPlotFragment fragment3 = (LightPlotFragment) mLightPlotAdapter.instantiateItem(mPager, 2);
		
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
	
	

	@Override
	public void onCalendarWeekChange(Date from, Date to) {
		LightPlotFragment fragment2 = (LightPlotFragment) mLightPlotAdapter.instantiateItem(mPager, 1);
		fragment2.setDataWithDateRange(from, to);
	}

	@Override
	public void onCalendarMonthChange(Date from, Date to) {
		LightPlotFragment fragment2 = (LightPlotFragment) mLightPlotAdapter.instantiateItem(mPager, 1);
		fragment2.setDataWithDateRange(from, to);
	}

	@Override
	public void onCalendarYearChange(int year) {
		LightPlotFragment fragment2 = (LightPlotFragment) mLightPlotAdapter.instantiateItem(mPager, 1);
		
		Calendar calFrom = Calendar.getInstance();
		Calendar calTo = Calendar.getInstance();
		
		calFrom.set(Calendar.DAY_OF_MONTH, 1);
		calFrom.set(Calendar.MONTH, 0);
		calFrom.set(Calendar.YEAR, year);
		
		calTo.set(Calendar.DAY_OF_MONTH, 31);
		calTo.set(Calendar.MONTH, 11);
		calTo.set(Calendar.YEAR, year);
		
		fragment2.setDataWithDateRange(calFrom.getTime(), calTo.getTime());
	}
}
