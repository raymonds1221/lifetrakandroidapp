package com.salutron.lifetrakwatchapp.fragment;

import java.util.Date;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.flurry.android.FlurryAgent;
import com.salutron.blesdk.SALBLEService;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.adapter.GoalAdapter;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.util.DeviceScanListener;
import com.salutron.lifetrakwatchapp.view.ConnectionFailedView;

public class GoalFragment extends BaseFragment implements DeviceScanListener {
	private ViewPager mGoalPager;
	private GoalAdapter mAdapter;
	private GoalItemFragment mGoalItemFragment;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.goal_settings_menu, menu);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_goal, null);
		mGoalPager = (ViewPager) view.findViewById(R.id.pgrGoal);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		((MainActivity)getActivity()).hideSoftKeyboard();

		if(getActivity() != null) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		initializeObjects(savedInstanceState);
		hideCalendar();
		
		FlurryAgent.logEvent("Goals_Page");
		//setDataWithDate();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void initializeObjects(Bundle savedInstanceState) {
		mAdapter = new GoalAdapter(getChildFragmentManager(),
				getLifeTrakApplication().getCurrentDate());

		mGoalPager.setAdapter(mAdapter);
		mGoalPager.setOnPageChangeListener(mPageChangeListener);
		mGoalPager.setCurrentItem(1);
		mGoalPager.setOffscreenPageLimit(3);
		
		mGoalItemFragment = (GoalItemFragment) mAdapter.instantiateItem(mGoalPager, 0);
	}

	private final ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
		private int mPosition;

		@Override
		public void onPageSelected(int state) {
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			mPosition = position;
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_IDLE && isAdded()) {
				Date date = getLifeTrakApplication().getCurrentDate();

				switch (mPosition) {
				case 0:
					date = getLifeTrakApplication().getPreviousDay();
					break;
				case 2:
					date = getLifeTrakApplication().getNextDay();
					break;
				}

				((MainActivity) getActivity()).setCalendarDate(date);
				((MainActivity) getActivity())
						.setCalendarMode(MODE_DAY);
				mGoalPager.setCurrentItem(1, false);
			}
		}
	};

	@Override
	public void onDeviceConnected(final BluetoothDevice device,
			final SALBLEService service, final Watch watch) {
		mAdapter.doUpdate(device, service, watch);
	}
	
	public void resetGoals() {
		mGoalItemFragment.resetGoals();
	}
	
	public void restoreGoals() {
		mGoalItemFragment.restoreGoals();
	}

	public void removeCallback(){
		mGoalItemFragment.removeCallback();
		mAdapter.removeCallback();
	}

	public void setCancelledSyncing (boolean mBoolean){
		mGoalItemFragment.setCancelledSyncing(mBoolean);
	}

}
