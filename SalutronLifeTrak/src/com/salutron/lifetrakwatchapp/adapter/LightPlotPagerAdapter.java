package com.salutron.lifetrakwatchapp.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.salutron.lifetrakwatchapp.fragment.FragmentFactory;
import com.salutron.lifetrakwatchapp.fragment.LightPlotFragment;
import com.salutron.lifetrakwatchapp.fragment.SleepDataItemFragment;

public class LightPlotPagerAdapter  extends BaseFragmentAdapter {
	private final List<LightPlotFragment> mFragments = new ArrayList<LightPlotFragment>() {
		private static final long serialVersionUID = 4679896548907183614L;
		{
			add(FragmentFactory.newInstance(LightPlotFragment.class));
			add(FragmentFactory.newInstance(LightPlotFragment.class));
			add(FragmentFactory.newInstance(LightPlotFragment.class));
		}
	};
	
	public LightPlotPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	
	public LightPlotPagerAdapter(FragmentManager fm, Date date) {
		super(fm);
		
		Calendar calYesterday = Calendar.getInstance();
		Calendar calNow = Calendar.getInstance();
		Calendar calTomorrow = Calendar.getInstance();
		
		calYesterday.setTime(date);
		calNow.setTime(date);
		calTomorrow.setTime(date);
		
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);
		calTomorrow.add(Calendar.DAY_OF_MONTH, 1);
		
		Bundle bundle1 = new Bundle();
		Bundle bundle2 = new Bundle();
		Bundle bundle3 = new Bundle();
		
		bundle1.putLong(DATE, calYesterday.getTimeInMillis());
		bundle2.putLong(DATE, calNow.getTimeInMillis());
		bundle3.putLong(DATE, calTomorrow.getTimeInMillis());
		
		bundle1.putInt(POSITION, 0);
		bundle2.putInt(POSITION, 1);
		bundle3.putInt(POSITION, 2);
		
		mFragments.get(0).setDate(calYesterday.getTime());
		mFragments.get(1).setDate(calNow.getTime());
		mFragments.get(2).setDate(calTomorrow.getTime());
		
		mFragments.get(0).setArguments(bundle1);
		mFragments.get(1).setArguments(bundle2);
		mFragments.get(2).setArguments(bundle3);
	}

	@Override
	public Fragment getItem(int position) {
		return mFragments.get(position);
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

}
