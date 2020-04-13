package com.salutron.lifetrakwatchapp.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.salutron.lifetrakwatchapp.fragment.FragmentFactory;
import com.salutron.lifetrakwatchapp.fragment.HeartRateItemFragment;
import com.salutron.lifetrakwatchapp.fragment.HeartRateItemFragmentR420;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HeartRateAdapterR420 extends BaseFragmentAdapter {
	private List<HeartRateItemFragmentR420> mFragments = new ArrayList<HeartRateItemFragmentR420>() {
		private static final long serialVersionUID = 3802421703481751775L;
		{
			add(FragmentFactory.newInstance(HeartRateItemFragmentR420.class));
			add(FragmentFactory.newInstance(HeartRateItemFragmentR420.class));
			add(FragmentFactory.newInstance(HeartRateItemFragmentR420.class));
		}
	};

	public HeartRateAdapterR420(FragmentManager fm) {
		super(fm);
	}

	public HeartRateAdapterR420(FragmentManager fm, Date date) {
		super(fm);
		
		Date yesterday = getYesterdayForDate(date);
		Date tomorrow = getTomorrowForDate(date);
		
		Bundle bundle1 = new Bundle();
		Bundle bundle2 = new Bundle();
		Bundle bundle3 = new Bundle();
		
		bundle1.putLong(DATE, yesterday.getTime());
		bundle1.putInt(POSITION, 0);
		bundle2.putLong(DATE, date.getTime());
		bundle2.putInt(POSITION, 1);
		bundle3.putLong(DATE, tomorrow.getTime());
		bundle3.putInt(POSITION, 2);
		
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
