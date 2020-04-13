package com.salutron.lifetrakwatchapp.adapter;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.salutron.lifetrakwatchapp.fragment.FragmentFactory;
import com.salutron.lifetrakwatchapp.fragment.DashboardItemFragment;

public class DashboardAdapter extends BaseFragmentAdapter {
	private final List<Fragment> mFragments = new ArrayList<Fragment>() {
		private static final long serialVersionUID = 4172562806595906753L;
		{
			add(FragmentFactory.newInstance(DashboardItemFragment.class));
			add(FragmentFactory.newInstance(DashboardItemFragment.class));
			add(FragmentFactory.newInstance(DashboardItemFragment.class));
		}
	};
	
	public DashboardAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public DashboardAdapter(FragmentManager fm, Date date) {
		super(fm);
		
		Date yesterday = getYesterdayForDate(date);
		Date tomorrow = getTomorrowForDate(date);
		
		Bundle bundleDate1 = new Bundle();
		Bundle bundleDate2 = new Bundle();
		Bundle bundleDate3 = new Bundle();
		
		bundleDate1.putInt(POSITION, 0);
		bundleDate2.putInt(POSITION, 1);
		bundleDate3.putInt(POSITION, 2);
		bundleDate1.putLong(DATE, yesterday.getTime());
		bundleDate2.putLong(DATE, date.getTime());
		bundleDate3.putLong(DATE, tomorrow.getTime());
		
		DashboardItemFragment fragment1 = (DashboardItemFragment) mFragments.get(0);
		DashboardItemFragment fragment2 = (DashboardItemFragment) mFragments.get(1);
		DashboardItemFragment fragment3 = (DashboardItemFragment) mFragments.get(2);
		
		fragment1.setDate(yesterday);
		fragment2.setDate(date);
		fragment3.setDate(tomorrow);
		
		mFragments.get(0).setArguments(bundleDate1);
		mFragments.get(1).setArguments(bundleDate2);
		mFragments.get(2).setArguments(bundleDate3);
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
