package com.salutron.lifetrakwatchapp.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.database.Cursor;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.mobeta.android.dslv.DragSortListView;
import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALConnectionSetting;
import com.salutron.blesdk.SALDayLightDetectSetting;
import com.salutron.blesdk.SALSleepSetting;
import com.salutron.blesdk.SALStatus;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.adapter.GoalItem;
import com.salutron.lifetrakwatchapp.adapter.GoalItemBrightLight;
import com.salutron.lifetrakwatchapp.adapter.GoalItemCalories;
import com.salutron.lifetrakwatchapp.adapter.GoalItemDistance;
import com.salutron.lifetrakwatchapp.adapter.GoalItemSleep;
import com.salutron.lifetrakwatchapp.adapter.GoalItemSteps;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.SleepSetting;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;
import com.salutron.lifetrakwatchapp.view.ConnectionFailedView;

public class GoalItemFragment extends BaseFragment implements
AdapterView.OnItemClickListener {

	private DragSortListView mGoalItemList;
	private ArrayList<GoalItem> mGoalItems = new ArrayList<GoalItem>();
	private ArrayAdapter<GoalItem> mAdapter;

	private GoalItemSteps goalSteps;
	private GoalItemDistance goalDistance;
	private GoalItemCalories goalCalories;
	private GoalItemSleep goalSleep;
	private GoalItemBrightLight goalBrightLight;
	private SharedPreferences prefs;
	private Goal mGoal;
    private String LOGWATCHNAME = "";

	private List<DayLightDetectSetting> dayLightDetecttSettings ;
    private List<Goal> goals;
	private DayLightDetectSetting mDayLightDetectSetting;
	private boolean mBrightLightEnable = false;

	private final Handler mHandler = new Handler();
	private final UpdateGoalReceiver updateListener = new UpdateGoalReceiver();
	private boolean isCancelledSyncing = false;

	private class UpdateGoalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context ctx, Intent data) {
			final UpdateResult result = (UpdateResult) data
					.getSerializableExtra("updateResult");
			final MainActivity mainActivity = (MainActivity) getActivity();
			if (mainActivity.mProgressDialog == null)
				mainActivity.reinitializeProgress();
			mainActivity.mProgressDialog.setMessage(result.message);

			final Date now = new Date();
			final LifeTrakApplication app = getLifeTrakApplication();
			app.getSelectedWatch().setLastSyncDate(now);
			app.getSelectedWatch().update();

			Editor edit = prefs.edit();
			edit.putLong("lastSyncDate", now.getTime());
			edit.commit();
		}
	}

	public GoalItemFragment() {
	}

	@Override
	public void onResume() {
		super.onResume();
		final String action = "com.salutron.lifetrak.syncgoalaction";
		registerReceiver(updateListener, new IntentFilter(action));
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(updateListener);
	}


	private void registerReceiver(UpdateGoalReceiver receiver,
			IntentFilter filter) {
		getActivity().registerReceiver(receiver, filter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_goal_item, null);

		mGoalItemList = (DragSortListView) view.findViewById(R.id.lstGoalItem);
		mGoalItemList.setFocusableInTouchMode(false);

		mGoalItemList.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			public void onFocusChange(View v, boolean hasFocus) {
				int val = v.getId();
			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initializeObjects();

		dayLightDetecttSettings = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchDaylightSetting = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(DayLightDetectSetting.class);

		if (dayLightDetecttSettings.size() > 0) {
			mDayLightDetectSetting = dayLightDetecttSettings.get(0);
			mBrightLightEnable = mDayLightDetectSetting.isEnabled();
		}

		mAdapter = new ArrayAdapter<GoalItem>(getActivity(),
				android.R.layout.simple_list_item_1, mGoalItems) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				final GoalItem item = getItem(position);
				return item.getView(LayoutInflater.from(getActivity()),
						convertView);
			}
		};

		final LifeTrakApplication app = (LifeTrakApplication) getActivity()
				.getApplication();

		mGoalItems.clear();
		mGoalItems.add(goalSteps = new GoalItemSteps());
		mGoalItems.add(goalDistance = new GoalItemDistance());
		mGoalItems.add(goalCalories = new GoalItemCalories());
		goalSleep = new GoalItemSleep(app.getSelectedWatch().getTimeDate());
		goalBrightLight = new GoalItemBrightLight(app.getSelectedWatch()
				.getTimeDate());

		if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS) {
			mGoalItems.add(goalSleep);
		}
		// if watch model is r450
		if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300
                && getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS
				&& getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C410) {
			if (mBrightLightEnable)
				mGoalItems.add(goalBrightLight);
		}
		//}


		mGoalItemList.setAdapter(mAdapter);
		mGoalItemList.setDropListener(mDropListener);
		mGoalItemList.setOnItemClickListener(this);

		if (getArguments() != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(getArguments().getLong(DATE));

			if (isAdded())
				setDataWithDate(calendar.getTime());
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList(DASHBOARD_ITEMS, mGoalItems);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		double distanceGoal = goalDistance.getValue();
		if (getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL)
				distanceGoal = distanceGoal / MILE;

        Calendar calendar = Calendar.getInstance();

		if (getLifeTrakApplication().getCurrentDate().after(new Date()))
			calendar.setTime(new Date());
		else
			calendar.setTime(getLifeTrakApplication().getCurrentDate());


		calendar.setTime(new Date());

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;


		if (mGoal != null) {
			mGoal.setContext(getActivity());
			mGoal.setStepGoal(goalSteps.getValue());
			mGoal.setDistanceGoal(distanceGoal);
			mGoal.setCalorieGoal(goalCalories.getValue());
			mGoal.setSleepGoal(goalSleep.getSleepGoal());
			if (mBrightLightEnable)
				mGoal.setBrightLightGoal(goalBrightLight.getBrightLightGoal());
			mGoal.update();
		}
	 else {

		Goal goal = new Goal(getActivity());
		goal.setStepGoal(goalSteps.getValue());
		goal.setDistanceGoal(distanceGoal);
		goal.setCalorieGoal(goalCalories.getValue());
		goal.setSleepGoal(goalSleep.getSleepGoal());
		if (mBrightLightEnable)
			goal.setBrightLightGoal(goalBrightLight.getBrightLightGoal());
		goal.setDate(calendar.getTime());
		goal.setDateStampDay(day);
		goal.setDateStampMonth(month);
		goal.setDateStampYear(year);
		goal.setWatch(getLifeTrakApplication().getSelectedWatch());
		goal.insert();
	}

	if (mBrightLightEnable)
	{
		if (mDayLightDetectSetting != null) {
			mDayLightDetectSetting.setExposureDuration(goalBrightLight.getBrightLightGoal());
			mDayLightDetectSetting.update();
		} else {
			mDayLightDetectSetting = new DayLightDetectSetting();
			mDayLightDetectSetting.setExposureDuration(goalBrightLight.getBrightLightGoal());
			mDayLightDetectSetting.insert();
		}
	}

	}


	public boolean doSync(final BluetoothDevice device,
			final SALBLEService service, final Watch watch) {
		final Context context = getActivity();

		try {

            int value = getLifeTrakApplication().getSelectedWatch().getModel();
            switch (value){
                case WATCHMODEL_C300:
                    LOGWATCHNAME = "C300 - ";
                    break;
                case WATCHMODEL_C300_IOS:
                    LOGWATCHNAME = "C300 - ";
                    break;
                case WATCHMODEL_C410:
                    LOGWATCHNAME = "C410 - ";
                    break;
                case WATCHMODEL_R415:
                    LOGWATCHNAME = "R450 - ";
                    break;

            }
			LifeTrakLogger.info("Goal Sync Started at " + (new Date()).toString());
			updateStepGoal(service,context,watch);

		} catch (Exception e) {
			MainActivity mainActivity = (MainActivity) getActivity();

			mainActivity.mSyncSuccess = false;
			if (mainActivity.mProgressDialog != null  && mainActivity.mProgressDialog.isShowing())
				mainActivity.mProgressDialog.dismiss();
			LifeTrakLogger.error(e.getMessage());
			return false;
		}

		return true;
	}

	private void updateStepGoal(final SALBLEService service, final Context context , final Watch watch ){
		mHandler.postDelayed(new Runnable() {
			public void run() {
				long stepGoal = goalSteps.getValue();
				LifeTrakLogger.info(LOGWATCHNAME + " step goal:" + String.valueOf(stepGoal));
				int updateStatus = service.updateStepGoal(stepGoal);
				final UpdateResult result = new UpdateResult();

				if (updateStatus == SALStatus.NO_ERROR) {
					LifeTrakLogger.info(LOGWATCHNAME + " step goal updateStatus:" + updateStatus);
					result.message = getString(R.string.goal_updated, getString(R.string.steps_small));
					updateDistanceGoal(service, context, watch);
				} else {
					MainActivity mainActivity = (MainActivity) getActivity();

					mainActivity.mSyncSuccess = false;
					if (mainActivity.mProgressDialog != null && mainActivity.mProgressDialog.isShowing())
						mainActivity.mProgressDialog.dismiss();

					mainActivity.C300C410SyncSuccess(false);
					//mainActivity.syncingWatchFailed();
				}

				final Intent receiver = new Intent();
				receiver.setAction("com.salutron.lifetrak.syncgoalaction");
				receiver.putExtra("updateResult", result);

				context.sendBroadcast(receiver);
			}
		}, sleeptime());
	}

	private void updateDistanceGoal(final SALBLEService service, final Context context, final Watch watch){
		mHandler.postDelayed(new Runnable() {
			public void run() {
				double distanceGoal = goalDistance.getValue() * 100.0f;

				if (getLifeTrakApplication().getUserProfile()
						.getUnitSystem() == UNIT_IMPERIAL) {
					distanceGoal = Math.ceil(distanceGoal / MILE);
				}

				long distanceValue = Math.round(distanceGoal);
				LifeTrakLogger.info(LOGWATCHNAME + " distance goal:" + String.valueOf(distanceValue));


				int updateStatus = service.updateDistanceGoal(distanceValue);

				final UpdateResult result = new UpdateResult();

				if (updateStatus == SALStatus.NO_ERROR) {
					LifeTrakLogger.info(LOGWATCHNAME + " distance goal updateStatus:" + updateStatus);
					result.message = getString(R.string.goal_updated, getString(R.string.distance_small));
					updateCaloriesGoal(service, context, watch);
				} else {
					MainActivity mainActivity = (MainActivity) getActivity();
					mainActivity.mSyncSuccess = false;
					if (mainActivity.mProgressDialog != null && mainActivity.mProgressDialog.isShowing())
						mainActivity.mProgressDialog.dismiss();

					mainActivity.C300C410SyncSuccess(false);
					//mainActivity.syncingWatchFailed();
				}

				final Intent receiver = new Intent();
				receiver.setAction("com.salutron.lifetrak.syncgoalaction");
				receiver.putExtra("updateResult", result);

				context.sendBroadcast(receiver);
			}
		}, sleeptime());
	}

	private void updateCaloriesGoal(final SALBLEService service, final Context context, final Watch watch){
		mHandler.postDelayed(new Runnable() {
			public void run() {
				int caloriesGoal = goalCalories.getValue();
				LifeTrakLogger.info(LOGWATCHNAME + " calories goal:"+ String.valueOf(caloriesGoal));


				int updateStatus = service.updateCalorieGoal(caloriesGoal);

				final UpdateResult result = new UpdateResult();

				if (updateStatus == SALStatus.NO_ERROR) {
					LifeTrakLogger.info(LOGWATCHNAME + " calories goal updateStatus:"+ updateStatus);
					result.message = getString(R.string.goal_updated, getString(R.string.calories_small));
					updateSleepGoal(service, context, watch);
				} else {
					MainActivity mainActivity = (MainActivity) getActivity();
					mainActivity.mSyncSuccess = false;
					if (mainActivity.mProgressDialog != null && mainActivity.mProgressDialog.isShowing())
						mainActivity.mProgressDialog.dismiss();

					mainActivity.C300C410SyncSuccess(false);
					//mainActivity.syncingWatchFailed();
				}

				final Intent receiver = new Intent();
				receiver.setAction("com.salutron.lifetrak.syncgoalaction");
				receiver.putExtra("updateResult", result);

				context.sendBroadcast(receiver);

				final Goal goal = new Goal(getActivity());
				goal.setStepGoal(goalSteps.getValue());

				double distGoal = goalDistance.getValue();

				if (getLifeTrakApplication().getUserProfile()
						.getUnitSystem() == UNIT_IMPERIAL) {
					distGoal = goalDistance.getValue() / MILE;
				}
				long stepGoal = goalSteps.getValue();

				goal.setDistanceGoal(distGoal);
				goal.setCalorieGoal(caloriesGoal);
				goal.setStepGoal(stepGoal);

				storeToDatabase(goal);
			}
		}, sleeptime());
	}

	private void updateSleepGoal(final SALBLEService service, final Context context, final Watch watch){
		mHandler.postDelayed(new Runnable() {
			public void run() {
				SALSleepSetting sleepSetting = new SALSleepSetting();

				if(getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
						getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS) {
					List<SleepSetting> sleepSettings = DataSource.getInstance(getActivity())
							.getReadOperation()
							.query("watchSleepSetting = ?", String.valueOf(watch.getId()))
							.getResults(SleepSetting.class);

					if(sleepSettings.size() > 0) {
						sleepSetting.setSleepDetectType(sleepSettings.get(0).getSleepDetectType());

					}
					LifeTrakLogger.info(LOGWATCHNAME + " sleep goal:" + String.valueOf(goalSleep.getSleepGoal()));
					sleepSetting.setSleepGoal(goalSleep.getSleepGoal());

					if (getLifeTrakApplication().getSelectedWatch().getModel()== WATCHMODEL_R415) {
						sleepSetting.setSleepDetectType(3);
					}

					int updateStatus = service.updateSleepSetting(sleepSetting);
					final UpdateResult result = new UpdateResult();

					if (updateStatus == SALStatus.NO_ERROR) {
						LifeTrakLogger.info(LOGWATCHNAME + " sleep goal updateStatus:"+ updateStatus);
						result.message = getString(R.string.goal_updated, getString(R.string.sleep_small));
						updateLightGoal(service, context, watch);
					} else {

						MainActivity mainActivity = (MainActivity) getActivity();
						if (mainActivity.mProgressDialog != null )
							mainActivity.mProgressDialog.dismiss();

						mainActivity.C300C410SyncSuccess(false);
						//mainActivity.syncingWatchFailed();
						LifeTrakLogger.info("Update Status = " +  updateStatus);
					}

					final Intent receiver = new Intent();
					receiver.setAction("com.salutron.lifetrak.syncgoalaction");
					receiver.putExtra("updateResult", result);

					context.sendBroadcast(receiver);

					final Goal goal = new Goal(getActivity());
					goal.setStepGoal(goalSteps.getValue());

					double distGoal = goalDistance.getValue();

					if (getLifeTrakApplication().getUserProfile()
							.getUnitSystem() == UNIT_IMPERIAL) {
						distGoal = goalDistance.getValue() / MILE;
					}

					goal.setDistanceGoal(distGoal);
					goal.setCalorieGoal(goalCalories.getValue());
					goal.setSleepGoal(goalSleep.getSleepGoal());

					storeToDatabase(goal);
				}
				else{
					MainActivity mainActivity = (MainActivity) getActivity();
					mainActivity.C300C410SyncSuccess(true);
					mainActivity.mSyncSuccess = true;
					if (mainActivity.mProgressDialog != null )
						mainActivity.mProgressDialog.dismiss();
					service.disconnectFromDevice();

					LifeTrakLogger.info("Goal Sync Success at " + (new Date()).toString());
					if (!isCancelledSyncing) {
						AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {

								arg0.dismiss();
							}
						}).create();
						alert.show();
					}
					else{
						isCancelledSyncing = false;
					}
				}
			}
		}, sleeptime());
	}

	private void updateLightGoal(final SALBLEService service, final Context context, final Watch watch){
		mHandler.postDelayed(new Runnable() {
			public void run() {
				if(getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
					SALConnectionSetting setting = new SALConnectionSetting();
					int status = 0;
					setting.setBLEWristOffOperationStatus(SALConnectionSetting.ENABLE);
					LifeTrakLogger.info("calibrationData WristOFF = " + String.valueOf(SALConnectionSetting.ENABLE));
					status = service.updateConnectionSettingData(setting);
					LifeTrakLogger.info("calibrationData WristOFF Update Status= " + String.valueOf(status));
				}
			}

		}, sleeptime());

		mHandler.postDelayed(new Runnable() {
			public void run() {
				SALDayLightDetectSetting dayLightSetting = new SALDayLightDetectSetting();

				if(getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
					if (mBrightLightEnable){

						dayLightSetting.setExposureDuration(goalBrightLight.getBrightLightGoal());
						LifeTrakLogger.info(LOGWATCHNAME + " Brightlight goal:" + String.valueOf(goalBrightLight.getBrightLightGoal()));
						int updateStatus = service.updateDayLightSettingData(dayLightSetting);

						final UpdateResult result = new UpdateResult();

						if (updateStatus == SALStatus.NO_ERROR) {
							result.message = getString(R.string.goal_updated, getString(R.string.bright_light));
							LifeTrakLogger.info(LOGWATCHNAME + " Brightlight goal updateStatus:"+ updateStatus);
							MainActivity mainActivity = (MainActivity) getActivity();

							mainActivity.mSyncSuccess = true;
							if (mainActivity.mProgressDialog != null )
								mainActivity.mProgressDialog.dismiss();

							LifeTrakLogger.info("Goal Sync Success at " + (new Date()).toString());
							if (!isCancelledSyncing){
							AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.sync_success)
									.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface arg0, int arg1) {

											arg0.dismiss();
										}
									}).create();
							alert.show();
							}
							else{
								isCancelledSyncing = false;
							}
						} else {

							LifeTrakLogger.info("Update status = "+ updateStatus);

							mHandler.removeCallbacksAndMessages(null);

							MainActivity mainActivity = (MainActivity) getActivity();


							if (mainActivity.mProgressDialog != null && mainActivity.mProgressDialog.isShowing())
								mainActivity.mProgressDialog.dismiss();


						}

						final Intent receiver = new Intent();
						receiver.setAction("com.salutron.lifetrak.syncgoalaction");
						receiver.putExtra("updateResult", result);

						context.sendBroadcast(receiver);
					}
					else{
						MainActivity mainActivity = (MainActivity) getActivity();
						mainActivity.mSyncSuccess = true;
						if (mainActivity.mProgressDialog != null )
							mainActivity.mProgressDialog.dismiss();
						if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_R415)
							service.disconnectFromDevice();

						if (!isCancelledSyncing){
						AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.sync_success)
								.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {

										arg0.dismiss();
									}
								}).create();
						alert.show();
						}
						else{
							isCancelledSyncing = false;
						}
					}
					final Goal goal = new Goal(getActivity());
					goal.setStepGoal(goalSteps.getValue());

					double distGoal = goalDistance.getValue();

					if (getLifeTrakApplication().getUserProfile()
							.getUnitSystem() == UNIT_IMPERIAL) {
						distGoal = goalDistance.getValue() / MILE;
					}

					goal.setDistanceGoal(distGoal);
					goal.setCalorieGoal(goalCalories.getValue());
					goal.setSleepGoal(goalSleep.getSleepGoal());
					if (mBrightLightEnable)
						goal.setBrightLightGoal(goalBrightLight.getBrightLightGoal());

					storeToDatabase(goal);


				}
				else{
					MainActivity mainActivity = (MainActivity) getActivity();
					mainActivity.C300C410SyncSuccess(true);
					if (mainActivity.mProgressDialog == null)
						mainActivity.reinitializeProgress();

					if (mainActivity.mProgressDialog != null && mainActivity.mProgressDialog.isShowing())
						mainActivity.mProgressDialog.dismiss();

					if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_R415)
						service.disconnectFromDevice();
					LifeTrakLogger.info("Goal Sync Success at " + (new Date()).toString());
					if (!isCancelledSyncing) {
						AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.sync_success)
								.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {

										arg0.dismiss();
									}
								}).create();
						alert.show();
					}
					else{
						isCancelledSyncing = false;
					}
				}
			}

		}, sleeptime()* 2);
	}

	public void removeCallback(){
		LifeTrakLogger.info("Sync on goal is cancelled");
		isCancelledSyncing = true;
		mHandler.removeCallbacksAndMessages(null);

	}

	public void setCancelledSyncing (boolean mBoolean){
		this.isCancelledSyncing = mBoolean;
	}

	private int sleeptime(){
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
			//lollipop
		//	LifeTrakLogger.info("Sleep Time is : " + 2000);
			return 2000;
		}
		else {
			//kitkat
		//	LifeTrakLogger.info("Sleep Time is : " + (SYNC_DELAY * 3));
			return SYNC_DELAY * 3;
		}
	}

	private void storeToDatabase(Goal goal) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		final List<Goal> goals = DataSource
				.getInstance(getActivity())
				.getReadOperation()
				.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
						String.valueOf(getLifeTrakApplication()
								.getSelectedWatch().getId()),
								String.valueOf(day), String.valueOf(month),
								String.valueOf(year)).getResults(Goal.class, false);

		LifeTrakLogger.debug(String.format("Goals %d", goals.size()));

		if (goals.isEmpty()) {
			goal.setDate(calendar.getTime());
			goal.setDateStampDay(day);
			goal.setDateStampMonth(month);
			goal.setDateStampYear(year);
			goal.setWatch(getLifeTrakApplication().getSelectedWatch());
			//goal.insert();
		} else {
			final Goal temp = goals.get(0);
			temp.setStepGoal(goal.getStepGoal());
			temp.setDistanceGoal(goal.getDistanceGoal());
			temp.setCalorieGoal(goal.getCalorieGoal());
			temp.setSleepGoal(goal.getSleepGoal());
			temp.setBrightLightGoal(goal.getBrightLightGoal());

			goal = temp;
			//goal.update();
		}
	}

	/*
	 * Method for initializing objects
	 */
	private void initializeObjects() {
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity()
				.getApplicationContext());
		LifeTrakLogger.configure();
	}

	/*
	 * Method for drag and drop of listview item
	 */
	private final DragSortListView.DropListener mDropListener = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			GoalItem dashboardItem = mAdapter.getItem(from);
			mAdapter.remove(dashboardItem);
			mAdapter.insert(dashboardItem, to);
		}
	};

	/**
	 * Method for setting the data per day
	 * 
	 * @param date
	 *            The date of the record
	 */
	public void setDataWithDate(final Date date) {
//		Calendar calendar = Calendar.getInstance();
//
//		calendar.set(Calendar.HOUR, 0);
//		calendar.set(Calendar.MINUTE, 0);
//		calendar.set(Calendar.SECOND, 0);
//		calendar.set(Calendar.MILLISECOND, 0);

		Calendar calendar2 = Calendar.getInstance();
		// calendar2.setTime(getLifeTrakApplication().getCurrentDate());
		calendar2.setTime(new Date());

		int day = calendar2.get(Calendar.DAY_OF_MONTH);
		int month = calendar2.get(Calendar.MONTH) + 1;
		int year = calendar2.get(Calendar.YEAR) - 1900;

        goals	 = DataSource
				.getInstance(getActivity())
				.getReadOperation()
				.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
						String.valueOf(getLifeTrakApplication()
								.getSelectedWatch().getId()),
								String.valueOf(day), String.valueOf(month),
								String.valueOf(year))
                                .orderBy("_id", SORT_DESC)
                                //.limit(1)
								.getResults(Goal.class);

		if (goals.size() == 0) {
			goals = DataSource
					.getInstance(getActivity())
					.getReadOperation()
					.query("watchGoal = ?",
							String.valueOf(getLifeTrakApplication()
									.getSelectedWatch().getId()))
									.orderBy("abs(date - " + date.getTime() + ")", SORT_ASC)
									.limit(1).getResults(Goal.class);
		}

		// for dates that does not have goals
		/*String query = "select _id, dateStampDay, dateStampMonth, dateStampYear from StatisticalDataHeader where _id not in (select header._id from StatisticalDataHeader header, Goal goal "
				+ "inner join Watch watch1 on header.watchDataHeader = watch1._id inner join Watch watch2 on goal.watchGoal = watch2._id "
				+ "where header.dateStampDay = goal.dateStampDay and header.dateStampMonth = goal.dateStampMonth and header.dateStampYear = goal.dateStampYear and header.watchDataHeader = ?)";

		Cursor cursor = DataSource
				.getInstance(getActivity())
				.getReadOperation()
				.rawQuery(
						query,
						String.valueOf(getLifeTrakApplication()
								.getSelectedWatch().getId()));

		while (cursor.moveToNext()) {
			int headerDay = cursor
					.getInt(cursor.getColumnIndex("dateStampDay"));
			int headerMonth = cursor.getInt(cursor
					.getColumnIndex("dateStampMonth"));
			int headerYear = cursor.getInt(cursor
					.getColumnIndex("dateStampYear"));

			Calendar tempCalendar = Calendar.getInstance();
			tempCalendar.set(Calendar.DAY_OF_MONTH, headerDay);
			tempCalendar.set(Calendar.MONTH, headerMonth - 1);
			tempCalendar.set(Calendar.YEAR, headerYear + 1900);
			tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
			tempCalendar.set(Calendar.MINUTE, 0);
			tempCalendar.set(Calendar.SECOND, 0);

			List<Goal> tempGoals = DataSource
					.getInstance(getActivity())
					.getReadOperation()
					.query("watchGoal = ?",
							String.valueOf(getLifeTrakApplication()
									.getSelectedWatch().getId()))
									.orderBy(
											"abs(date - " + tempCalendar.getTimeInMillis()
											+ ")", SORT_ASC).limit(1)
											.getResults(Goal.class);

			if (tempGoals.size() > 0) {
				Goal tempGoal = tempGoals.get(0);
				Goal newGoal = new Goal(getActivity());
				newGoal.setStepGoal(tempGoal.getStepGoal());
				newGoal.setDistanceGoal(tempGoal.getDistanceGoal());
				newGoal.setCalorieGoal(tempGoal.getCalorieGoal());
				newGoal.setSleepGoal(tempGoal.getSleepGoal());
				newGoal.setBrightLightGoal(tempGoal.getBrightLightGoal());
				newGoal.setDate(tempCalendar.getTime());
				newGoal.setDateStampDay(headerDay);
				newGoal.setDateStampMonth(headerMonth);
				newGoal.setDateStampYear(headerYear);
				newGoal.setWatch(getLifeTrakApplication().getSelectedWatch());
				newGoal.insert();
			}
		}*/
		// ----

		final GoalItemSteps steps = (GoalItemSteps) mGoalItems.get(0);
		final GoalItemDistance distance = (GoalItemDistance) mGoalItems.get(1);
		final GoalItemCalories calories = (GoalItemCalories) mGoalItems.get(2);
		GoalItemSleep sleep = null;
		GoalItemBrightLight brightLight = null;

		if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS) {
			sleep = (GoalItemSleep) mGoalItems.get(3);
		}

		// if watch model is r450

		if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS
                &&
				getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C410 && mBrightLightEnable) {
			brightLight = (GoalItemBrightLight) mGoalItems.get(4);
		}

		steps.setMinValue(100);
		steps.setMaxValue(30000);

		calories.setMinValue(100);
		calories.setMaxValue(5000);

		final UserProfile userProfile = ((LifeTrakApplication) getActivity()
				.getApplication()).getUserProfile();

		switch (userProfile.getUnitSystem()) {
		case SalutronLifeTrakUtility.UNIT_IMPERIAL:
			distance.setMinValue(0.62f);
			distance.setMaxValue(25f);
			goalDistance.setNameOfMeasurement("mi");
			break;
		case SalutronLifeTrakUtility.UNIT_METRIC:
			distance.setMinValue(1f);
			distance.setMaxValue(40.23f);
			goalDistance.setNameOfMeasurement("km");
		}

		if (goals.size() > 0) {
			mGoal = goals.get(0);

			steps.setValue((int) mGoal.getStepGoal());

			float distanceGoal = (float) mGoal.getDistanceGoal();

			if (getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL) {
				distanceGoal = distanceGoal * MILE;
			}

			distance.setValue(distanceGoal);
			calories.setValue((int) mGoal.getCalorieGoal());

			if (sleep != null) {
				sleep.setMinHours(1);
				sleep.setMaxHours(14);
				sleep.setMinMinutes(0);
				sleep.setMaxMinutes(50);
				sleep.setSleepGoal(mGoal.getSleepGoal());
				sleep.setHours(mGoal.getSleepGoal() / 60);
				sleep.setMinutes(mGoal.getSleepGoal() % 60);
			}

			if (brightLight != null) {
				if (mDayLightDetectSetting != null) {
					int hours = mDayLightDetectSetting.getExposureDuration() / 60;
					int minutes = mDayLightDetectSetting.getExposureDuration() % 60;
					brightLight = (GoalItemBrightLight) mGoalItems.get(4);
					brightLight.setMinMinutes(0);
					brightLight.setMaxMinutes(60);
					brightLight.setMaxHours(2);
					brightLight.setMinHours(0);
					brightLight.setBrightLightGoal(mDayLightDetectSetting.getExposureDuration());
					brightLight.setHours(hours);
					brightLight.setMinutes(minutes);
				}
			}

		} else {

			steps.setValue(0);
			distance.setValue(0.62f);
			calories.setValue(0);

			if (sleep != null) {
				sleep.setHours(1);
				sleep.setMinHours(1);
				sleep.setMaxHours(14);
				sleep.setMinMinutes(0);
				sleep.setMaxMinutes(50);
				sleep.setSleepGoal(0);
			}

			if (brightLight != null) {
				brightLight.setMinHours(0);
				brightLight.setMaxHours(2);
				brightLight.setMinMinutes(0);
				brightLight.setMaxMinutes(60);
				brightLight.setBrightLightGoal(10);

			}


		}

		mAdapter.notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// DashboardItem dashboardItem = mAdapter.getItem(position);
		//
		// MainActivity mainActivity = (MainActivity) getActivity();
		//
		// if (dashboardItem.getDashboardType() == TYPE_CALORIES
		// || dashboardItem.getDashboardType() == TYPE_DISTANCE
		// || dashboardItem.getDashboardType() == TYPE_STEPS) {
		// Bundle bundle = new Bundle();
		// bundle.putInt(DASHBOARD_TYPE, dashboardItem.getDashboardType());
		// mainActivity.switchFragment(FragmentFactory.newInstance(
		// FitnessResultsFragment.class, bundle));
		// } else if (dashboardItem.getDashboardType() == TYPE_ACTIGRAPHY) {
		// mainActivity.switchFragment(FragmentFactory
		// .newInstance(ActigraphyFragment.class));
		// }

	}

	public void resetGoals() {
		goalSteps.setProgressManualChange(false);
		goalCalories.setProgressManualChange(false);

		goalSteps.setValue(10000);

		switch (getLifeTrakApplication().getUserProfile().getUnitSystem()) {
		case UNIT_IMPERIAL:
			goalDistance.setValue(0.62f);
			// goalDistance.updateGoal(getActivity(), 0.62 * MILE);
			break;
		case UNIT_METRIC:
			goalDistance.setValue(1.0f);
			// goalDistance.updateGoal(getActivity(), 1.0);
			break;
		}

		goalCalories.setValue(3000);
		goalSleep.setSleepGoal(480);
		if (mBrightLightEnable)
			goalBrightLight.setBrightLightGoal(10);

		mAdapter.notifyDataSetChanged();
	}

	public void restoreGoals() {
		Calendar calendar = Calendar.getInstance();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(new Date());

		int day = calendar2.get(Calendar.DAY_OF_MONTH);
		int month = calendar2.get(Calendar.MONTH) + 1;
		int year = calendar2.get(Calendar.YEAR) - 1900;

//		List<Goal> goals = DataSource
//				.getInstance(getActivity())
//				.getReadOperation()
//				.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
//						String.valueOf(getLifeTrakApplication()
//								.getSelectedWatch().getId()),
//								String.valueOf(day), String.valueOf(month),
//								String.valueOf(year)).getResults(Goal.class);

//		if (goals.size() == 0) {
//			goals = DataSource
//					.getInstance(getActivity())
//					.getReadOperation()
//					.query("watchGoal = ?",
//							String.valueOf(getLifeTrakApplication()
//									.getSelectedWatch().getId()))
//									.orderBy("abs(date - " + calendar.getTimeInMillis() + ")",
//											SORT_ASC).limit(1).getResults(Goal.class);
//		}


		List<Goal> goals  = DataSource
				.getInstance(getActivity())
				.getReadOperation()
				.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
						String.valueOf(getLifeTrakApplication()
								.getSelectedWatch().getId()),
						String.valueOf(day), String.valueOf(month),
						String.valueOf(year))
				.orderBy("_id", SORT_DESC)
				.getResults(Goal.class);

		if (goals.size() == 0) {
			goals = DataSource.getInstance(getActivity())
					.getReadOperation()
					.query("watchGoal = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
					.orderBy("abs(date - " + calendar.getTimeInMillis() + ")", SORT_ASC)
					.limit(1)
					.getResults(Goal.class);

			Calendar now = Calendar.getInstance();

			if(now.get(Calendar.DATE) < calendar.get(Calendar.DATE) ){
				Goal mGoal = goals.get(0);
				goals = DataSource
						.getInstance(getActivity())
						.getReadOperation()
						.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
								String.valueOf(getLifeTrakApplication()
										.getSelectedWatch().getId()),
								String.valueOf(mGoal.getDateStampDay()), String.valueOf(mGoal.getDateStampMonth()),
								String.valueOf(mGoal.getDateStampYear()))
						.orderBy("_id", SORT_DESC)
						.getResults(Goal.class);
			}
		}

		if (goals.size() > 0) {
			mGoal = goals.get(0);

			goalSteps.setProgressManualChange(false);
			goalCalories.setProgressManualChange(false);

			goalSteps.setValue((int) mGoal.getStepGoal());

			switch (getLifeTrakApplication().getUserProfile().getUnitSystem()) {
			case UNIT_IMPERIAL:
				goalDistance.setValue((float) mGoal.getDistanceGoal() * MILE);
				// goalDistance.updateGoal(getActivity(),
				// mGoal.getDistanceGoal() * MILE);
				break;
			case UNIT_METRIC:
				goalDistance.setValue((float) mGoal.getDistanceGoal());
				// goalDistance.updateGoal(getActivity(), 1.0);
				break;
			}

			goalCalories.setValue((int) mGoal.getCalorieGoal());
			goalSleep.setSleepGoal(mGoal.getSleepGoal());
			if (mBrightLightEnable)
				goalBrightLight.setBrightLightGoal(mGoal.getBrightLightGoal());
			mAdapter.notifyDataSetChanged();
		}
        else
        {
            final GoalItemSteps steps = (GoalItemSteps) mGoalItems.get(0);
            final GoalItemDistance distance = (GoalItemDistance) mGoalItems.get(1);
            final GoalItemCalories calories = (GoalItemCalories) mGoalItems.get(2);
            GoalItemSleep sleep = null;
            GoalItemBrightLight brightLight = null;

            if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                    getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS) {
                sleep = (GoalItemSleep) mGoalItems.get(3);
            }

            // if watch model is r450

            if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                    getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS
                    &&
                    getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C410 && mBrightLightEnable) {
                brightLight = (GoalItemBrightLight) mGoalItems.get(4);
            }

            steps.setMinValue(100);
            steps.setMaxValue(30000);

            calories.setMinValue(100);
            calories.setMaxValue(5000);

            final UserProfile userProfile = ((LifeTrakApplication) getActivity()
                    .getApplication()).getUserProfile();

            switch (userProfile.getUnitSystem()) {
                case SalutronLifeTrakUtility.UNIT_IMPERIAL:
                    distance.setMinValue(0.62f);
                    distance.setMaxValue(25f);
                    goalDistance.setNameOfMeasurement("mi");
                    break;
                case SalutronLifeTrakUtility.UNIT_METRIC:
                    distance.setMinValue(1f);
                    distance.setMaxValue(40.23f);
                    goalDistance.setNameOfMeasurement("km");
            }



                steps.setValue(0);
                distance.setValue(0.62f);
                calories.setValue(0);

                if (sleep != null) {
                    sleep.setHours(1);
                    sleep.setMinHours(1);
                    sleep.setMaxHours(14);
                    sleep.setMinMinutes(0);
                    sleep.setMaxMinutes(50);
                    sleep.setSleepGoal(0);
                }

                if (brightLight != null) {
                    brightLight.setMinHours(0);
                    brightLight.setMaxHours(2);
                    brightLight.setMinMinutes(0);
                    brightLight.setMaxMinutes(60);
                    brightLight.setBrightLightGoal(10);

                }




        }
	}
}

class UpdateResult implements Serializable {

	private static final long serialVersionUID = -33027971973410827L;
	String message;
}