package com.salutron.lifetrakwatchapp.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.salutron.blesdk.SALBLEService;
import com.salutron.lifetrakwatchapp.fragment.FragmentFactory;
import com.salutron.lifetrakwatchapp.fragment.GoalItemFragment;
import com.salutron.lifetrakwatchapp.model.Watch;

public class GoalAdapter extends BaseFragmentAdapter {
	private final List<Fragment> mFragments = new ArrayList<Fragment>() {
		private static final long serialVersionUID = 4172562806595906753L;

		{
			add(FragmentFactory.newInstance(GoalItemFragment.class));
		}
	};

	public GoalAdapter(FragmentManager fm, Date date) {
		super(fm);
		
		final Bundle bundle = new Bundle();
		bundle.putLong(DATE, date.getTime());
		
		mFragments.get(0).setArguments(bundle);
	}

	@Override
	public Fragment getItem(int position) {
		return mFragments.get(position);
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}
	
	public boolean doUpdate(final BluetoothDevice device,
			final SALBLEService service, final Watch watch) {
		final GoalItemFragment goalItemFragment = (GoalItemFragment) getItem(0);
//		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
//			//lollipop
//			return goalItemFragment.doSyncLollipop(device, service, watch);
//		else
//			//kitkat
			return goalItemFragment.doSync(device, service, watch);
	}

	public void removeCallback() {
		final GoalItemFragment goalItemFragment = (GoalItemFragment) getItem(0);
		goalItemFragment.removeCallback();
	}

	public void setCancelledSyncing(boolean mBoolean) {
		final GoalItemFragment goalItemFragment = (GoalItemFragment) getItem(0);
		goalItemFragment.setCancelledSyncing(mBoolean);
	}
}
