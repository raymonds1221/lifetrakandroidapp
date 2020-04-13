package com.salutron.lifetrakwatchapp.adapter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.salutron.lifetrakwatchapp.fragment.FragmentFactory;
import com.salutron.lifetrakwatchapp.fragment.FitnessResultsItemFragment;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

public class FitnessResultsAdapter extends FragmentStatePagerAdapter implements SalutronLifeTrakUtility {
	private final List<Fragment> mFragments = new ArrayList<Fragment>() {
		private static final long serialVersionUID = -6309076276787890710L;
		{
			add(FragmentFactory.newInstance(FitnessResultsItemFragment.class));
			add(FragmentFactory.newInstance(FitnessResultsItemFragment.class));
			add(FragmentFactory.newInstance(FitnessResultsItemFragment.class));
		}
	};

	public FitnessResultsAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public FitnessResultsAdapter(FragmentManager fm, Date date, int dashboardType) {
		super(fm);
		
		Calendar calYesterday = Calendar.getInstance();
		Calendar calNow = Calendar.getInstance();
		Calendar calTomorrow = Calendar.getInstance();
		
		calYesterday.setTime(date);
		calNow.setTime(date);
		calTomorrow.setTime(date);
		
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);
		calTomorrow.add(Calendar.DAY_OF_MONTH, 1);
		
		Date yesterday = calYesterday.getTime();
		Date now = calNow.getTime();
		Date tomorrow = calTomorrow.getTime();
		
		Bundle bundleDate1 = new Bundle();
		Bundle bundleDate2 = new Bundle();
		Bundle bundleDate3 = new Bundle();
		
		bundleDate1.putLong(DATE, yesterday.getTime());
		bundleDate2.putLong(DATE, now.getTime());
		bundleDate3.putLong(DATE, tomorrow.getTime());
		bundleDate1.putInt(DASHBOARD_TYPE, dashboardType);
		bundleDate2.putInt(DASHBOARD_TYPE, dashboardType);
		bundleDate3.putInt(DASHBOARD_TYPE, dashboardType);
		bundleDate1.putInt(POSITION, 0);
		bundleDate2.putInt(POSITION, 1);
		bundleDate3.putInt(POSITION, 2);
		
		FitnessResultsItemFragment fragment1 = (FitnessResultsItemFragment) mFragments.get(0);
		FitnessResultsItemFragment fragment2 = (FitnessResultsItemFragment) mFragments.get(1);
		FitnessResultsItemFragment fragment3 = (FitnessResultsItemFragment) mFragments.get(2);
		
		fragment1.setArguments(bundleDate1);
		fragment2.setArguments(bundleDate2);
		fragment3.setArguments(bundleDate3);
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
