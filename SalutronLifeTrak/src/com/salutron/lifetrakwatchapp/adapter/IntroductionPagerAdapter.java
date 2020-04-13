package com.salutron.lifetrakwatchapp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.salutron.lifetrakwatchapp.fragment.IntroductionPageFragment;

public class IntroductionPagerAdapter extends FragmentPagerAdapter {

	public IntroductionPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return IntroductionPageFragment.newInstance(position);
	}

	@Override
	public int getCount() {
		return 5;
	}
}
