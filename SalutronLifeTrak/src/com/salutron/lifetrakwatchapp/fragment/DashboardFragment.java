package com.salutron.lifetrakwatchapp.fragment;

import java.util.Date;
import java.util.Calendar;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.content.pm.ActivityInfo;
import android.support.v4.view.ViewPager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.adapter.DashboardAdapter;
import com.salutron.lifetrakwatchapp.fragment.dialog.AlertDialogFragment;
import com.salutron.lifetrakwatchapp.service.GoogleFitSyncService;
import com.salutron.lifetrakwatchapp.util.CalendarDateChangeListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.flurry.android.FlurryAgent;
import com.salutron.lifetrakwatchapp.util.GoogleApiClientManager;

import roboguice.inject.InjectView;

public class DashboardFragment extends BaseFragment implements CalendarDateChangeListener, AlertDialogFragment.OnClickListener,
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	@InjectView(R.id.pgrDashboard) private ViewPager mDashboardPager;
	private DashboardAdapter mAdapter;

	private GoogleApiClientManager googleApiClientManager;
	private boolean googleFitUserResponded = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_dashboard, null);
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		googleApiClientManager = ((GoogleApiClientManager.Provider) getActivity()).getGoogleApiClientManager();
		
		((MainActivity)getActivity()).hideSoftKeyboard();
		
		if(getActivity() != null) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		FlurryAgent.logEvent("Dashboard_Page");
		initializeObjects();
		showCalendar();
	}

	@Override
	public void onStart() {
		super.onStart();
		googleApiClientManager.registerConnectionCallbacks(this);
		googleApiClientManager.registerConnectionFailedListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	public void showGoogleFitDialog(){
		if (!googleFitUserResponded && !mPreferenceWrapper.getPreferenceBooleanValue(GOOGLE_FIT_ENABLED) && !mPreferenceWrapper.getPreferenceBooleanValue(GOOGLE_FIT_CHOICE)) {
			AlertDialogFragment.newInstance(0, R.string.google_fit_prompt, R.string.continue_button_text, R.string.prompt_no)
					.show(this, "GoogleFitPrompt");
		}
	}

	@Override
	public void onClick(AlertDialogFragment dialogFragment, int which) {
		googleFitUserResponded = true;
		if (which == AlertDialogFragment.OnClickListener.BUTTON_POSITIVE) {
			googleApiClientManager.getClient().connect();
		}
		else{
			mPreferenceWrapper.setPreferenceBooleanValue(GOOGLE_FIT_CHOICE, true).synchronize();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		googleApiClientManager.unregisterConnectionCallbacks(this);
		googleApiClientManager.unregisterConnectionFailedListener(this);
	}

	/**
	 * Called when the user chooses not to complete a provided resolution,
	 * for example by canceling a dialog, or when a network error occurs.
	 * @param connectionResult
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		mPreferenceWrapper.setPreferenceBooleanValue(GOOGLE_FIT_ENABLED, false).synchronize();
	}

	@Override
	public void onConnected(Bundle bundle) {
		final boolean wasDisabled = !mPreferenceWrapper.getPreferenceBooleanValue(GOOGLE_FIT_ENABLED);
		mPreferenceWrapper.setPreferenceBooleanValue(GOOGLE_FIT_ENABLED, true).synchronize();
		// Perform initial sync if Google Fit was previously disabled
		if (wasDisabled) {
			GoogleFitSyncService.start(getActivity(), getLifeTrakApplication().getSelectedWatch());
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		// No UI elements to disable
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_sync, menu);
	}

	public void initializeObjects() {
		Date date = getLifeTrakApplication().getCurrentDate();
		mAdapter = new DashboardAdapter(getChildFragmentManager(), date);
		
		mDashboardPager.setAdapter(null);
		mDashboardPager.setAdapter(mAdapter);
		mDashboardPager.setOnPageChangeListener(mPageChangeListener);
		mDashboardPager.setCurrentItem(1);
		mDashboardPager.setOffscreenPageLimit(1);
		
		MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.selectCalendarDate(date);
		mainActivity.updateCalendarDate();
	}
	
	public void notifyAdapter() {
		mAdapter.notifyDataSetChanged();
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
				mDashboardPager.setCurrentItem(1, false);
			}
		}
	};
	
	public void setDataWithDate(Date date) {
		DashboardItemFragment fragment1 = (DashboardItemFragment) mAdapter.instantiateItem(mDashboardPager, 0);
		DashboardItemFragment fragment2 = (DashboardItemFragment) mAdapter.instantiateItem(mDashboardPager, 1);
		DashboardItemFragment fragment3 = (DashboardItemFragment) mAdapter.instantiateItem(mDashboardPager, 2);
		
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
		
		fragment1.setDataWithDate(calYesterday.getTime(), 0);
		fragment2.setDataWithDate(calNow.getTime(), 1);
		fragment3.setDataWithDate(calTomorrow.getTime(), 2);
	}
	
	public void enableDashboard() {
		DashboardItemFragment fragment1 = (DashboardItemFragment) mAdapter.instantiateItem(mDashboardPager, 0);
		DashboardItemFragment fragment2 = (DashboardItemFragment) mAdapter.instantiateItem(mDashboardPager, 1);
		DashboardItemFragment fragment3 = (DashboardItemFragment) mAdapter.instantiateItem(mDashboardPager, 2);
		
		fragment1.enableDashboard();
		fragment2.enableDashboard();
		fragment3.enableDashboard();
	}
	
	public void disableDashboard() {
		DashboardItemFragment fragment1 = (DashboardItemFragment) mAdapter.instantiateItem(mDashboardPager, 0);
		DashboardItemFragment fragment2 = (DashboardItemFragment) mAdapter.instantiateItem(mDashboardPager, 1);
		DashboardItemFragment fragment3 = (DashboardItemFragment) mAdapter.instantiateItem(mDashboardPager, 2);
		
		fragment1.disableDashboard();
		fragment2.disableDashboard();
		fragment3.disableDashboard();
	}
	
	public void arrangeDashboardItems() {
		DashboardItemFragment fragment1 = (DashboardItemFragment) mAdapter.instantiateItem(mDashboardPager, 0);
		DashboardItemFragment fragment3 = (DashboardItemFragment) mAdapter.instantiateItem(mDashboardPager, 2);
		
		fragment1.arrangeDashboardItems();
		fragment3.arrangeDashboardItems();
	}

	/*
	 * CalendarDateChangeListener Methods
	 */
	@Override
	public void onCalendarDateChange(Date date) {
		setDataWithDate(date);
	}

	@Override
	public void onCalendarWeekChange(Date from, Date to) {
	}

	@Override
	public void onCalendarMonthChange(Date from, Date to) {
	}

	@Override
	public void onCalendarYearChange(int year) {
		
	}

	public void handleBackPressed() {
		
	}
}
