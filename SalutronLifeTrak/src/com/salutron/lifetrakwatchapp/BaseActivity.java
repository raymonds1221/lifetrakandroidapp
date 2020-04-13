package com.salutron.lifetrakwatchapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import roboguice.RoboGuice;
import roboguice.activity.event.OnActivityResultEvent;
import roboguice.activity.event.OnConfigurationChangedEvent;
import roboguice.activity.event.OnContentChangedEvent;
import roboguice.activity.event.OnCreateEvent;
import roboguice.activity.event.OnDestroyEvent;
import roboguice.activity.event.OnNewIntentEvent;
import roboguice.activity.event.OnResumeEvent;
import roboguice.activity.event.OnStopEvent;
import roboguice.event.EventManager;
import roboguice.inject.RoboInjector;
import roboguice.util.RoboContext;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.net.ConnectivityManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.inject.Key;
import com.flurry.android.FlurryAgent;
import com.androidquery.AQuery;

import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALBLEService.LocalBinder;
import com.salutron.blesdk.SALCalibration;
import com.salutron.blesdk.SALSleepDatabase;
import com.salutron.blesdk.SALSleepSetting;
import com.salutron.blesdk.SALStatisticalDataHeader;
import com.salutron.blesdk.SALStatisticalDataPoint;
import com.salutron.blesdk.SALStatus;
import com.salutron.blesdk.SALTimeDate;
import com.salutron.blesdk.SALUserProfile;
import com.salutron.blesdk.SALWorkoutInfo;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.CalibrationData;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.SleepSetting;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.StatisticalDataPoint;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.service.BluetoothListener;
import com.salutron.lifetrakwatchapp.util.PreferenceWrapper;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;
import com.salutron.lifetrakwatchapp.util.SalutronObjectList;
import com.salutron.lifetrakwatchapp.util.SalutronSDKCallback;
import com.salutron.lifetrakwatchapp.util.LifeTrakSyncR450;
import com.salutron.lifetrakwatchapp.util.LifeTrakSyncR420;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;

/**
 * Base class for all activities
 * 
 * @author rsarmiento
 * 
 */
abstract class BaseActivity extends SherlockFragmentActivity implements RoboContext, SalutronLifeTrakUtility {
	private static final int REQ_BT_ENABLE = BluetoothListener.REQ_BT_ENABLE;
	/*
	 * RoboGuice
	 */
	private EventManager mEventManager;
	protected HashMap<Key<?>, Object> scopedObjects = new HashMap<Key<?>, Object>();

	/*
	 * Primitive types
	 */
	private int mDataPointIndex;
	private int mCalibrationType;

	protected long mStepGoal;
	protected double mDistanceGoal;
	protected long mCalorieGoal;

	/*
	 * Declared objects
	 */
	protected PreferenceWrapper mPreferenceWrapper;
	protected SALBLEService mSalutronService;
	protected List<BluetoothDevice> mBluetoothDevices = new ArrayList<BluetoothDevice>();

	protected TimeDate mTimeDate;
	protected SalutronObjectList<StatisticalDataHeader> mStatisticalDataHeaders = new SalutronObjectList<StatisticalDataHeader>();
	protected SleepSetting mSleepSetting;
	protected CalibrationData mCalibrationData;
	protected SalutronObjectList<WorkoutInfo> mWorkoutInfos = new SalutronObjectList<WorkoutInfo>();
	protected SalutronObjectList<SleepDatabase> mSleepDatabases = new SalutronObjectList<SleepDatabase>();
	protected UserProfile mUserProfile;
	protected BluetoothDevice mBluetoothDevice;

	private final Handler mHandlerPost = new Handler();
	private static SalutronSDKCallback mSalutronSDKCallback = null;

	private LifeTrakApplication mLifeTrakApplication;
	private int mModelNumber;
	protected int mSyncType;
	protected boolean mWatchExists;

	protected AQuery mAQuery;
	
	protected static LifeTrakSyncR450 mLifeTrakSyncR450;
	protected static LifeTrakSyncR420 mLifeTrakSyncR420;
	protected boolean mBLEServiceCModelRegistered = false;
    protected final int RESULT_PROCESS_COMPLETE = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		final RoboInjector injector = RoboGuice.getInjector(this);
		mEventManager = injector.getInstance(EventManager.class);
		injector.injectMembersWithoutViews(this);

		super.onCreate(savedInstanceState);

		mEventManager.fire(new OnCreateEvent(savedInstanceState));
		mPreferenceWrapper = PreferenceWrapper.getInstance(this);
		bluetoothListener = new BluetoothListener(this);


		mSalutronSDKCallback = null;
		mLifeTrakSyncR450 = LifeTrakSyncR450.getInstance(this);
		mLifeTrakSyncR420 = LifeTrakSyncR420.getInstance(this);
		LifeTrakLogger.configure();

	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
			moveTaskToBack(true);
		}
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}
	
	protected void requestBluetoothOn() {
		BluetoothAdapter BTadapter = BluetoothAdapter.getDefaultAdapter();
		if (!BTadapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQ_BT_ENABLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		mEventManager.fire(new OnResumeEvent());
		
	}

	@Override
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mEventManager.fire(new OnNewIntentEvent());
		
	}

	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
		mEventManager.fire(new OnStopEvent());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// unregisterReceiver(finisher);
		// unregisterReceiver(bluetoothListener);
		mEventManager.fire(new OnDestroyEvent());
		//unregisterReceiver(mNetworkReceiver);
		RoboGuice.destroyInjector(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mEventManager.fire(new OnActivityResultEvent(requestCode, resultCode, data));
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		RoboGuice.getInjector(this).injectViewMembers(this);
		mEventManager.fire(new OnContentChangedEvent());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		final Configuration currentConfig = getResources().getConfiguration();
		super.onConfigurationChanged(newConfig);
		mEventManager.fire(new OnConfigurationChangedEvent(currentConfig, newConfig));
	}

	/**
	 * Method for binding the Salutron BLE Service
	 */
	protected void bindBLEService() {
		Intent intent = new Intent(this, SALBLEService.class);
		bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
	}

	/**
	 * Method for unbinding the Salutron BLE Service
	 */
	protected void unbindBLEService() {
		unbindService(mServiceConnection);
	}

	/*
	 * Service Connection for registering handler
	 */
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder localBinder = (LocalBinder) service;
			mSalutronService = localBinder.getService();

			mSalutronService.registerDevDataHandler(mHandler);
			mSalutronService.registerDevListHandler(mHandler);
			
			mBLEServiceCModelRegistered = true;
            mLifeTrakSyncR450.setServiceInstance(mSalutronService);
			mLifeTrakSyncR420.setServiceInstance(mSalutronService);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBLEServiceCModelRegistered = false;
            mLifeTrakSyncR450.setServiceInstance(null);
			mLifeTrakSyncR420.setServiceInstance(null);
		}
    };

	/*
	 * Handler message for Salutron Service
	 */
	private final Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message message) {
			final Bundle data = message.getData();

			switch (message.what) {
			case SALBLEService.GATT_DEVICE_FOUND_MSG:
				final BluetoothDevice device = data.getParcelable(BluetoothDevice.EXTRA_DEVICE);
				
				boolean deviceFound = false;

				for (BluetoothDevice bluetoothDevice : mBluetoothDevices) {
					String macAddress = device.getAddress();

					if (bluetoothDevice.getAddress().equals(macAddress) && 
							(device.getName() != null && !device.getName().isEmpty()) && 
							(device.getAddress() != null && !device.getAddress().isEmpty())) {
						deviceFound = true;
						break;
					}
				}

				if (!deviceFound) {
					mBluetoothDevices.add(device);
					mSalutronService.stopScan();

					mHandlerPost.postDelayed(new Runnable() {
						public void run() {
							onDeviceFound(device, data);
						}
					}, HANDLER_DELAY);
				}
				break;
			case SALBLEService.GATT_DEVICE_CONNECT_MSG:
				LifeTrakLogger.info("device connected on BaseActivity Handler");
				break;
			case SALBLEService.GATT_DEVICE_READY_MSG:
				LifeTrakLogger.info("device ready on BaseActivity Handler");
				
				String sdkVersion = mSalutronService.getLibraryVersion();
				
				mPreferenceWrapper.setPreferenceStringValue(SDK_VERSION, sdkVersion).synchronize();

				if (mSalutronSDKCallback instanceof ConnectionActivity) {
					LifeTrakLogger.info("sdk callback is connection activity");
				} else if (mSalutronSDKCallback instanceof PairDeviceAutoActivity) {
					LifeTrakLogger.info("sdk callback is pair device auto activity");
				}
				
				if (mSalutronSDKCallback != null) {
					mSalutronSDKCallback.onDeviceReady();
				}
				break;
			case SALBLEService.GATT_DEVICE_DISCONNECT_MSG:

				mBluetoothDevices.clear();
				if (mSalutronSDKCallback != null) {
					mSalutronSDKCallback.onDeviceDisconnected();
				}
				break;
			case SALBLEService.SAL_MSG_DEVICE_DATA_RECEIVED:
				final int dataType = data.getInt(SALBLEService.SAL_DEVICE_DATA_TYPE);

				switch (dataType) {
				case SALBLEService.COMMAND_GET_TIME:
					SALTimeDate timeDate = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
					onGetTimeAndDate(timeDate);
					break;
				case SALBLEService.COMMAND_GET_STAT_DATA_HEADERS:
					List<SALStatisticalDataHeader> statisticalDataHeaders = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
					onGetStatisticalDataHeaders(statisticalDataHeaders);
					break;
				case SALBLEService.COMMAND_GET_DATA_POINTS_FOR_DATE:
					List<SALStatisticalDataPoint> statisticalDataPoints = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
					onGetStatisticalDataPoint(statisticalDataPoints);
					break;
				case SALBLEService.COMMAND_GET_GOAL_STEPS:
					long stepGoal = data.getLong(SALBLEService.SAL_DEVICE_DATA);
					onGetStepGoal(stepGoal);
					break;
				case SALBLEService.COMMAND_GET_GOAL_DISTANCE:
					long distance = data.getLong(SALBLEService.SAL_DEVICE_DATA);
					double distanceGoal = (double) distance / 100.0;
					onGetDistanceGoal(distanceGoal);
					break;
				case SALBLEService.COMMAND_GET_GOAL_CALORIE:
					long calorieGoal = data.getLong(SALBLEService.SAL_DEVICE_DATA);
					onGetCalorieGoal(calorieGoal);
					break;
				case SALBLEService.COMMAND_GET_SLEEP_SETTING:
					SALSleepSetting sleepSetting = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
					onGetSleepSetting(sleepSetting);
					break;
				case SALBLEService.COMMAND_GET_CALIBRATION_DATA:
					SALCalibration calibrationData = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
					onGetCalibrationData(calibrationData);
					break;
				case SALBLEService.COMMAND_GET_WORKOUT_DB:
					List<SALWorkoutInfo> workoutInfos = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
					onGetWorkoutDatabase(workoutInfos);
					break;
				case SALBLEService.COMMAND_GET_SLEEP_DB:
					List<SALSleepDatabase> sleepDatabases = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
					onGetSleepDatabase(sleepDatabases);
					break;
				case SALBLEService.COMMAND_GET_USER_PROFILE:
					SALUserProfile userProfile = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
					onGetUserProfile(userProfile);
					break;
				}
				break;
			case SALBLEService.SAL_MSG_DEVICE_INFO:
				final int infoType = data.getInt(SALBLEService.SAL_DEVICE_INFO_TYPE);

				switch (infoType) {
				case SALBLEService.DEV_INFO_MODEL:
					String model = data.getString(SALBLEService.SAL_DEVICE_INFO);
					onGetModel(model);
					break;
				case SALBLEService.DEV_INFO_FIRMWARE_VERSION:
					onGetFirmware(data.getString(SALBLEService.SAL_DEVICE_INFO));
					break;
				case SALBLEService.DEV_INFO_SOFTWARE_VERSION:
					onGetSoftwareRevision(data.getString(SALBLEService.SAL_DEVICE_INFO));
					break;
				}
				break;
			case SALBLEService.SAL_MSG_DEVICE_CHECKSUM_ERROR:
				break;
			default:
				super.handleMessage(message);
			}
		}
	};

	protected void startSync() {
        LifeTrakLogger.info("Sync Started From Watch - " + new Date());

		mHandlerPost.postDelayed(new Runnable() {
			public void run() {
				if (mSalutronSDKCallback != null)
					mSalutronSDKCallback.onSyncTime();

				mSalutronService.registerDevDataHandler(mHandler);
				mSalutronService.registerDevListHandler(mHandler);
				mSalutronService.stopScan();
				int status = mSalutronService.getCurrentTimeAndDate();

				switch (status) {
				case SALStatus.NO_ERROR:
					LifeTrakLogger.info("getCurrentTimeAndDate status is no error");
					break;
				case SALStatus.ERROR_NOT_SUPPORTED:
					LifeTrakLogger.info("getCurrentTimeAndDate status is error not supported");
					if (mSalutronSDKCallback instanceof ConnectionActivity) {
						ConnectionActivity.mDeviceFound = false;
					}
					break;
				case SALStatus.ERROR_NOT_INITIALIZED:
					LifeTrakLogger.info("getCurrentTimeAndDate status is error not initialized");
					if (mSalutronSDKCallback instanceof ConnectionActivity) {
						ConnectionActivity.mDeviceFound = false;
					}
					break;
				}
			}
		}, 500);
	}

	protected String convertiOSToAndroidMacAddress(String macAddress) {
		macAddress = macAddress.replace("0000", "").toUpperCase(Locale.getDefault());
		int start = 0;

		String value = "";

		while (start < macAddress.length() - 1) {
			value = macAddress.substring(start, start + 2) + ((start == 0) ? "" : ":") + value;

			start += 2;
		}

		return value;
	}

	protected String convertAndroidToiOSMacAddress(String macAddress) {
		String[] macAddressPart = macAddress.toLowerCase(Locale.getDefault()).split(":");
		int middle = macAddressPart.length / 2;
		String value = "";

		for (int i = macAddressPart.length - 1; i >= 0; i--) {

			value += macAddressPart[i];

			if (i == middle) {
				value += "0000";
			}

		}

		return value;
	}

	/*
	 * Salutron SDK Listeners
	 */
	protected void onDeviceFound(final BluetoothDevice device, Bundle data) {
		final int modelNumber = data.getInt(MODEL_NUMBER);

		boolean matched = false;

		if (modelNumber == mModelNumber)
			matched = true;
		if (mModelNumber == 300 && modelNumber == 400)
			matched = true;

		String convertedMacAddress = convertAndroidToiOSMacAddress(device.getAddress());

		List<Watch> watches = DataSource.getInstance(this).getReadOperation().query("macAddress = ? or macAddress = ?", device.getAddress(), convertedMacAddress).getResults(Watch.class);

		switch (mSyncType) {
		case SYNC_TYPE_INITIAL:
			boolean hasAccessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null;

			if (matched && (watches.size() == 0 || !hasAccessToken)) {
				mHandlerPost.postDelayed(new Runnable() {
					public void run() {
						int status = SALStatus.ERROR_NOT_CONNECTED;
						
						if (modelNumber == WATCHMODEL_R415) {
							status = mLifeTrakSyncR450.connectToDevice(device.getAddress(), modelNumber);
						} else if(modelNumber == WATCHMODEL_R420) {
							status = mLifeTrakSyncR450.connectToDevice(device.getAddress(), modelNumber);
						} else {
							status = mSalutronService.connectToDevice(device.getAddress(), modelNumber);
						}

                        if (status == SALStatus.NO_ERROR && mSyncType == SYNC_TYPE_INITIAL) {
							if (mSalutronSDKCallback != null)
								mSalutronSDKCallback.onDeviceConnected(device);
						}
					}
				}, HANDLER_DELAY);
				
				if (mSalutronSDKCallback instanceof ConnectionActivity) {
					LifeTrakLogger.info("sdk callback is connection activity");
				} else if (mSalutronSDKCallback instanceof PairDeviceAutoActivity) {
					LifeTrakLogger.info("sdk callback is pair device auto activity");
				}
			}
			break;
		case SYNC_TYPE_DASHBOARD:
			boolean checkiOSMacAddress = false;

			if (getLifeTrakApplication().getSelectedWatch() != null && getLifeTrakApplication().getSelectedWatch().getMacAddress().indexOf(':') == -1) {
				String androidMacAddress = convertiOSToAndroidMacAddress(getLifeTrakApplication().getSelectedWatch().getMacAddress());
				checkiOSMacAddress = device.getAddress().equals(androidMacAddress);
			}

			if (device != null && getLifeTrakApplication().getSelectedWatch() != null 
					&& device.getAddress().equals(getLifeTrakApplication().getSelectedWatch().getMacAddress())
					|| checkiOSMacAddress) {
				
                int status = SALStatus.ERROR_NOT_CONNECTED;

                if (modelNumber == WATCHMODEL_R415) {
                    status = mLifeTrakSyncR450.connectToDevice(device.getAddress(), modelNumber);
                } else {
                    status = mSalutronService.connectToDevice(device.getAddress(), modelNumber);
                }

                if (status == SALStatus.NO_ERROR) {
                    if (mSalutronSDKCallback != null) {
                        mSalutronSDKCallback.onDeviceConnected(device);
                    }
                } else {
                    mSalutronService.disconnectFromDevice();
                }
			} else {
				mWatchExists = true;
			}
			break;
		}
	}

	protected void onGetModel(String model) {
	}

	protected void onGetTimeAndDate(SALTimeDate timeDate) {
		mTimeDate = TimeDate.buildTimeDate(this, timeDate);

		Calendar calendar = Calendar.getInstance();

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);

		int status = SALStatus.NO_ERROR;
		if (timeDate.getYear() != year || timeDate.getMonth() != month || timeDate.getDay() != day || timeDate.getHour() != hour || timeDate.getMinute() != min) {
			timeDate.set(calendar.get(Calendar.SECOND), calendar.get(Calendar.MINUTE), hour, day, month + 1, year - 1900);
			mTimeDate = TimeDate.buildTimeDate(this, timeDate);
			boolean autoSyncTime = mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME);
			if (autoSyncTime) {
				status = mSalutronService.updateTimeAndDate(timeDate);
			}
			mHandlerPost.postDelayed(new Runnable() {
				public void run() {
					if (mSalutronSDKCallback != null)
						mSalutronSDKCallback.onSyncStatisticalDataHeaders();

					int status = mSalutronService.getStatisticalDataHeaders();

					if (status != SALStatus.NO_ERROR) {
						status = mSalutronService.getStepGoal();
					}
				}
			}, HANDLER_DELAY);
		} else {

			if (mSalutronSDKCallback != null)
				mSalutronSDKCallback.onSyncStatisticalDataHeaders();

			status = mSalutronService.getStatisticalDataHeaders();
		}

		if (mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onStartSync();
		}

		if (status != SALStatus.NO_ERROR) {
			Log.e(TAG, "onGetTimeAndDate error");
		}
	}

	protected void onGetStatisticalDataHeaders(List<SALStatisticalDataHeader> statisticalDataHeaders) {
		mStatisticalDataHeaders.clear();

		for (SALStatisticalDataHeader salDataHeader : statisticalDataHeaders) {
			StatisticalDataHeader dataHeader = StatisticalDataHeader.buildStatisticalDataHeader(this, salDataHeader);
			mStatisticalDataHeaders.add(dataHeader);
		}

		mDataPointIndex = 0;

		if (mSalutronSDKCallback != null)
			mSalutronSDKCallback.onSyncStatisticalDataPoint(0);

		int status = mSalutronService.getDataPointsOfSelectedDateStamp(mDataPointIndex);

		if (status != SALStatus.NO_ERROR) {
			Log.e(TAG, "onGetStatisticalDataHeaders error");
		}
	}

	protected void onGetStatisticalDataPoint(List<SALStatisticalDataPoint> salStatisticalDataPoints) {
		int status = SALStatus.NO_ERROR;

		if (mDataPointIndex < mStatisticalDataHeaders.size()) {
			LifeTrakLogger.info("data point index: " + mDataPointIndex + mStatisticalDataHeaders.get(mDataPointIndex).getDateStamp().toString());

			List<StatisticalDataPoint> statisticalDataPoints = StatisticalDataPoint.buildStatisticalDataPoint(this, salStatisticalDataPoints);

			for (int i = 0; i < statisticalDataPoints.size(); i++) {
				statisticalDataPoints.get(i).setDataPointId(i + 1);
			}

			mStatisticalDataHeaders.get(mDataPointIndex).setStatisticalDataPoints(statisticalDataPoints);
			mDataPointIndex++;

			if (mSalutronSDKCallback != null)
				mSalutronSDKCallback.onSyncStatisticalDataPoint((int) (((float) mDataPointIndex / (float) mStatisticalDataHeaders.size()) * 100.0f));

			status = mSalutronService.getDataPointsOfSelectedDateStamp(mDataPointIndex);
		} else {
			if (mSalutronSDKCallback != null)
				mSalutronSDKCallback.onSyncStepGoal();

			status = mSalutronService.getStepGoal();
		}

		if (status != SALStatus.NO_ERROR) {
			Log.e(TAG, "onGetStatisticalDataPoint error");
		}
	}

	protected void onGetStepGoal(long stepGoal) {
		mStepGoal = stepGoal;

		if (mSalutronSDKCallback != null)
			mSalutronSDKCallback.onSyncDistanceGoal();

		int status = mSalutronService.getDistanceGoal();

		if (status != SALStatus.NO_ERROR) {
			Log.e(TAG, "onGetStepGoal error");
		}
	}

	protected void onGetDistanceGoal(double distanceGoal) {
		mDistanceGoal = distanceGoal;

		if (mSalutronSDKCallback != null)
			mSalutronSDKCallback.onSyncCalorieGoal();

		int status = mSalutronService.getCalorieGoal();

		if (status != SALStatus.NO_ERROR) {
			Log.e(TAG, "onGetDistanceGoal error");
		}
	}

	protected void onGetCalorieGoal(long calorieGoal) {
		mCalorieGoal = calorieGoal;

		if (mModelNumber == WATCHMODEL_C410) {
			if (mSalutronSDKCallback != null)
				mSalutronSDKCallback.onSyncSleepSetting();

			int status = mSalutronService.getSleepSetting();

			switch (status) {
			case SALStatus.NO_ERROR:
				LifeTrakLogger.info("getSleepSetting status is no error");
				break;
			case SALStatus.ERROR_NOT_SUPPORTED:
				LifeTrakLogger.info("getSleepSetting status is error not supported");
				break;
			case SALStatus.ERROR_NOT_INITIALIZED:
				LifeTrakLogger.info("getSleepSetting status is error not initialized");
				break;
			}

			if (status != SALStatus.NO_ERROR) {
				Log.e(TAG, "onGetCalorieGoal error");
				mCalibrationType = 0;
				status = mSalutronService.getUserProfile();

				if (status != SALStatus.NO_ERROR) {
					LifeTrakLogger.error("getUserProfile unsuccessfull call");
				} else {
					LifeTrakLogger.info("getUserProfile successfull call");
				}
			}
		} else {
			int status = mSalutronService.getUserProfile();

			if (status != SALStatus.NO_ERROR) {
				LifeTrakLogger.error("getUserProfile unsuccessfull call");
			} else {
				LifeTrakLogger.info("getUserProfile successfull call");
			}
		}

	}

	protected void onGetSleepSetting(SALSleepSetting sleepSetting) {
		mSleepSetting = new SleepSetting();
		mSleepSetting.setSleepDetectType(sleepSetting.getSleepDetectType());
		mSleepSetting.setSleepGoalMinutes(sleepSetting.getSleepGoal());

		mCalibrationType = SALCalibration.STEP_CALIBRATION;
		mCalibrationData = new CalibrationData();

		if (mSalutronSDKCallback != null)
			mSalutronSDKCallback.onSyncCalibrationData();

		int status = mSalutronService.getCalibrationData(mCalibrationType);

		if (status != SALStatus.NO_ERROR) {
			Log.e(TAG, "onGetSleepSetting error");
		}
	}

	protected void onGetCalibrationData(SALCalibration calibrationData) {
		int status = SALStatus.NO_ERROR;

		if (mCalibrationType < 3) {
			int type = 0;
			switch (calibrationData.getCalibrationType()) {
			case SALCalibration.STEP_CALIBRATION:
				mCalibrationData.setStepCalibration(calibrationData.getCalibrationValue());
				type = SALCalibration.WALK_DISTANCE_CALIBRATION;
				break;
			case SALCalibration.WALK_DISTANCE_CALIBRATION:
				mCalibrationData.setDistanceCalibrationWalk(calibrationData.getCalibrationValue());
				type = SALCalibration.AUTO_EL_SETTING;
				break;
			case SALCalibration.AUTO_EL_SETTING:
				mCalibrationData.setAutoEL(calibrationData.getCalibrationValue());
				break;
			}
			mCalibrationType++;
			status = mSalutronService.getCalibrationData(type);
		} else {

			if (mSalutronSDKCallback != null)
				mSalutronSDKCallback.onSyncWorkoutDatabase();

			status = mSalutronService.getWorkoutDatabase();
		}

		if (status != SALStatus.NO_ERROR) {
			Log.e(TAG, "onGetCalibrationData error");
		}
	}

	protected void onGetWorkoutDatabase(List<SALWorkoutInfo> workoutInfos) {

		for (SALWorkoutInfo salWorkoutInfo : workoutInfos) {
			WorkoutInfo workoutInfo = WorkoutInfo.buildWorkoutInfo(this, salWorkoutInfo);
			mWorkoutInfos.add(workoutInfo);
		}

		if (mSalutronSDKCallback != null)
			mSalutronSDKCallback.onSyncSleepDatabase();

		int status = mSalutronService.getSleepDatabase();

		if (status != SALStatus.NO_ERROR) {
			Log.e(TAG, "onGetWorkoutDatabase error");
		}
	}

	protected void onGetSleepDatabase(List<SALSleepDatabase> sleepDatabases) {
		for (SALSleepDatabase salSleepDatabase : sleepDatabases) {
			SleepDatabase sleepDatabase = SleepDatabase.buildSleepDatabase(this, salSleepDatabase);
			mSleepDatabases.add(sleepDatabase);
		}

		if (mSalutronSDKCallback != null)
			mSalutronSDKCallback.onSyncUserProfile();

		int status = mSalutronService.getUserProfile();

		if (status != SALStatus.NO_ERROR) {
			Log.e(TAG, "onGetSleepDatabase error");
		}
	}

	protected void onGetUserProfile(SALUserProfile userProfile) {
		mUserProfile = UserProfile.buildUserProfile(this, userProfile);

		/*if (mPreferenceWrapper.getPreferenceBooleanValue(HAS_USER_PROFILE))
			mSalutronService.disconnectFromDevice();*/

		int status = mSalutronService.getFirmwareRevision();

		if (status != SALStatus.NO_ERROR) {
			Log.e(TAG, "onGetSleepDatabase error");
		}

//		if (mSalutronSDKCallback != null) {
//			mSalutronSDKCallback.onSyncFinish();
//		}
	}

	protected void onGetFirmware(String firmwareVersion) {
		try {
			Pattern pattern = Pattern.compile("[^0-9]");
			Matcher matcher = pattern.matcher(firmwareVersion);
			String number = matcher.replaceAll("");

			mPreferenceWrapper.setPreferenceStringValue(FIRMWAREVERSION, "V" + number).synchronize();
		}
		catch (Exception e){
			LifeTrakLogger.info("Error e:" + e.getLocalizedMessage());
		}

		int delay = 750;

		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
			delay = 2000;

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				int status = mSalutronService.getSoftwareRevision();

				if (status != SALStatus.NO_ERROR) {
					Log.e(TAG, "onGetSleepDatabase error");
				}
			}
		}, delay);


	}

	protected void onGetSoftwareRevision(String softwareRevision) {
		Pattern pattern = Pattern.compile("[^0-9]");
		Matcher matcher = pattern.matcher(softwareRevision);
		String number = matcher.replaceAll("");

		mPreferenceWrapper.setPreferenceStringValue(SOFTWAREVERSION, "V" + number).synchronize();

		if (mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onSyncFinish();
		}
	}
	protected void setSalutronSDKCallback(SalutronSDKCallback callback) {
		mSalutronSDKCallback = callback;
	}

	protected LifeTrakApplication getLifeTrakApplication() {
		if (mLifeTrakApplication == null) {
			mLifeTrakApplication = (LifeTrakApplication) getApplicationContext();
		}
		return mLifeTrakApplication;
	}

	protected void setModelNumber(int modelNumber) {
		mModelNumber = modelNumber;
	}

	@Override
	public Map<Key<?>, Object> getScopedObjectMap() {
		return scopedObjects;
	}

	private final KillReceiver finisher = new KillReceiver();
	private BluetoothListener bluetoothListener;

	private final class KillReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	}

	private final NetworkReceiver mNetworkReceiver = new NetworkReceiver();

	private final class NetworkReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent data) {
			onNetworkChanged();
		}
	}

	protected boolean isBluetoothEnabled() {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		return adapter.isEnabled();
	}

	public String getApiUrl() {
		return API_URL;
	}

	protected float dpToPx(float dp) {
		return dp * (getResources().getDisplayMetrics().densityDpi / 160.0f);
	}

	protected Bitmap createScaledBitmap(Bitmap originalBitmap, float width, float height) {
		int originalWidth = originalBitmap.getWidth();
		int originalHeight = originalBitmap.getHeight();

		Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		float scale = width / originalWidth;
		float xtranslation = 0.0f;
		float ytranslation = (height - originalHeight * scale) / 2.0f;
		Matrix matrix = new Matrix();

		if (originalWidth > originalHeight)
			matrix.setRotate(90, originalWidth / 2, originalHeight / 2);

		matrix.postTranslate(xtranslation, ytranslation);
		matrix.postScale(scale, scale);

		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		canvas.drawBitmap(originalBitmap, matrix, paint);

		return bitmap;
	}

	protected Bitmap makeRoundedBitmap(Bitmap bitmap, int width, int height) {
		Bitmap targetBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(width / 2, height / 2, height / 2, Path.Direction.CCW);
		canvas.clipPath(path);
		canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, targetBitmap.getWidth(), targetBitmap.getHeight()), null);
		return targetBitmap;
	}


    protected Bitmap getCroppedBitmap(Bitmap bmp, int w, int h) {

        Bitmap sbmp;
            sbmp = bmp;

        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(w / 2, h / 2, (w < h ? w : h) / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }

	protected Date getExpirationDate() {
		long millis = mPreferenceWrapper.getPreferenceLongValue(EXPIRATION_DATE) * 1000;
		Date date = new Date(millis);
		return date;
	}

	protected void onNetworkChanged() {
		LifeTrakLogger.info("network changed");
	}
}
