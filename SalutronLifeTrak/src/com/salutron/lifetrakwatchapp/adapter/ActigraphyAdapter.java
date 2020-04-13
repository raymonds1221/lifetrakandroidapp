package com.salutron.lifetrakwatchapp.adapter;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.salutron.lifetrakwatchapp.fragment.FragmentFactory;
import com.salutron.lifetrakwatchapp.fragment.ActigraphyItemFragment;

public class ActigraphyAdapter extends BaseFragmentAdapter {
	private final List<Fragment> mFragments = new ArrayList<Fragment>() {
		private static final long serialVersionUID = -6019694145656573632L;
		{
			add(FragmentFactory.newInstance(ActigraphyItemFragment.class));
			add(FragmentFactory.newInstance(ActigraphyItemFragment.class));
			add(FragmentFactory.newInstance(ActigraphyItemFragment.class));
		}
	};

	public ActigraphyAdapter(FragmentManager fm, Date date) {
		super(fm);
		
		Date yesterday = getYesterdayForDate(date);
		Date tomorrow = getTomorrowForDate(date);
		
		Bundle bundle1 = new Bundle();
		Bundle bundle2 = new Bundle();
		Bundle bundle3 = new Bundle();
		
		bundle1.putLong(DATE, yesterday.getTime());
		bundle2.putLong(DATE, date.getTime());
		bundle3.putLong(DATE, tomorrow.getTime());
		
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