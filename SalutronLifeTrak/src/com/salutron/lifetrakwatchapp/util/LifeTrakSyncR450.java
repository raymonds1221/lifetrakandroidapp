package com.salutron.lifetrakwatchapp.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Message;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.util.Log;

import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALBLEService.LocalBinder;
import com.salutron.blesdk.SALCalibration;
import com.salutron.blesdk.SALConnectionSetting;
import com.salutron.blesdk.SALDateStamp;
import com.salutron.blesdk.SALSleepSetting;
import com.salutron.blesdk.SALStatisticalDataHeader;
import com.salutron.blesdk.SALStatisticalDataPoint;
import com.salutron.blesdk.SALLightDataPoint;
import com.salutron.blesdk.SALTimeDate;
import com.salutron.blesdk.SALTimeStamp;
import com.salutron.blesdk.SALUserProfile;
import com.salutron.blesdk.SALWorkoutInfo;
import com.salutron.blesdk.SALWorkoutStopInfo;
import com.salutron.blesdk.SALSleepDatabase;
import com.salutron.blesdk.SALWakeupSetting;
import com.salutron.blesdk.SALActivityAlertSetting;
import com.salutron.blesdk.SALDayLightDetectSetting;
import com.salutron.blesdk.SALNightLightDetectSetting;
import com.salutron.blesdk.SALStatus;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.LightDataPoint;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.StatisticalDataPoint;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;
import com.salutron.lifetrakwatchapp.model.SleepSetting;
import com.salutron.lifetrakwatchapp.model.CalibrationData;
import com.salutron.lifetrakwatchapp.model.WakeupSetting;
import com.salutron.lifetrakwatchapp.model.Notification;
import com.salutron.lifetrakwatchapp.model.ActivityAlertSetting;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.NightLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.Watch;

import com.flurry.android.FlurryAgent;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class LifeTrakSyncR450  implements SalutronLifeTrakUtility {

	/**
	 * Maximum execution time of a command (in milliseconds) before timing out
	 */
	private static final long COMMAND_TIMEOUT_INTERVAL = 2 * 60 * 1000L;

	/**
	 * Implements a simple timeout mechanism by tracking the execution time of commands
	 *
	 * When a timeout occurs, SalutronSDKCallback450.onError(int status) is called
	 * with status set to SALStatus.ERROR_HARDWARE_PROBLEM. The device is also
	 * automatically disconnected.
	 */
	private static final class CommandTimer extends CountDownTimer {

		private static final int COMMAND_NONE = -152688;
		private int currentCommand = COMMAND_NONE;

		public CommandTimer(long timeout) {
			super(timeout, timeout);
		}

		public void reset() {
			cancel();
			currentCommand = COMMAND_NONE;
		}

		public void startTimer(int command) {
			if (currentCommand == COMMAND_NONE) {
				LifeTrakLogger.info("CommandTimer.startTimer(): command " + command);
				currentCommand = command;
				start();
			} else {
				LifeTrakLogger.warn("CommandTimer.startTimer(): attempted to start timer for command " + command + " while command " + currentCommand + " is pending");
			}
		}

		public void stopTimer(int command) {
			if (currentCommand == command) {
				LifeTrakLogger.info("CommandTimer.stopTimer(): command " + command);
				currentCommand = COMMAND_NONE;
				cancel();
			} else {
				LifeTrakLogger.warn("CommandTimer.stopTimer(): attempted to stop timer for command " + command + " while command " + currentCommand + " is pending");
			}
		}

		@Override
		public void onTick(long l) {

		}

		@Override
		public void onFinish() {
			LifeTrakLogger.warn("CommandTimer: command " + currentCommand + " timed out");
			currentCommand = COMMAND_NONE;
			mSalutronService.disconnectFromDevice();


			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					String macAddress = null;
					if (mLifeTrakApplication.getSelectedWatch() != null)
						macAddress = mLifeTrakApplication.getSelectedWatch().getMacAddress();
					else
						macAddress = mPreferenceWrapper.getPreferenceStringValue(LAST_R450_SYNC);

					if (macAddress != null && !macAddress.contains(":"))
						macAddress = convertiOSToAndroidMacAddress(macAddress);

					LifeTrakLogger.info("Reconnect to LifeTrakSyncR450 with mac address = " + macAddress);
					mSalutronService.connectToDevice(macAddress, SALBLEService.MODEL_R415);
				}
			}, 3000);

			mSalutronSDKCallback.onError(SALStatus.ERROR_HARDWARE_PROBLEM);
		}
	}

	private static Context mContext;
	private static SALBLEService mSalutronService;
	private static LifeTrakSyncR450 mLifeTrakSyncR450;
	private static LifeTrakApplication mLifeTrakApplication;
	private static SalutronSDKCallback450 mSalutronSDKCallback;
	private static final CommandTimer commandTimer = new CommandTimer(COMMAND_TIMEOUT_INTERVAL);

	public static final int SYNC_TYPE_INITIAL = 0x01;
	public static final int SYNC_TYPE_DASHBOARD = 0x02;
	private static int mSyncType = SYNC_TYPE_INITIAL;

	private static int mDataHeaderIndexForDataPoint = 0;
	private static int mDataHeaderIndexForLightPoint = 0;
	private static int mWorkoutIndex = 0;
	private static int mCalibrationIndex = 0;
	private static int mWakeupIndex = 0;
	private static int mActivityAlertIndex = 0;
	private static int mDayLightDetectIndex = 0;
	private static int mNightLightDetectIndex = 0;

	private static List<Integer> mHeaderIndexes = new ArrayList<Integer>();

	private static List<StatisticalDataHeader> mStatisticalDataHeaders = new ArrayList<StatisticalDataHeader>();
	private static List<List<StatisticalDataPoint>> mStatisticalDataPoints = new ArrayList<List<StatisticalDataPoint>>();
	private static List<List<LightDataPoint>> mLightDataPoints = new ArrayList<List<LightDataPoint>>();
	private static List<WorkoutInfo> mWorkoutInfos = new ArrayList<WorkoutInfo>();
	private static List<List<WorkoutStopInfo>> mWorkoutStopInfos = new ArrayList<List<WorkoutStopInfo>>();
	private static List<SleepDatabase> mSleepDatabases = new ArrayList<SleepDatabase>();
	private static SleepSetting mSleepSetting;
	private static long mStepGoal;
	private static double mDistanceGoal;
	private static long mCalorieGoal;
	public static List<Goal> mGoals = new ArrayList<Goal>();
	public static CalibrationData mCalibrationData;
	public static WakeupSetting mWakeupSetting;
	private static Notification mNotification;
	public static ActivityAlertSetting mActivityAlertSetting;
	public static DayLightDetectSetting mDayLightDetectSetting;
	public static NightLightDetectSetting mNightLightDetectSetting;
	public static TimeDate mTimeDate;
	public static UserProfile mUserProfile;
	private static PreferenceWrapper mPreferenceWrapper;
	private static boolean mDisconnected = true;
	private static boolean mCancelled = false;
	private static boolean mSyncStarted = false;
	private static boolean mIsUpdateTimeAndDate = false;

	protected static List<BluetoothDevice> mBluetoothDevices = new ArrayList<BluetoothDevice>();

	private boolean mRegistered;
	private int status = SALStatus.NO_ERROR;

	private static LifeTrakUpdateR450 mLifeTrakUpdateR450;

	public static String firmwareVersion;
	public static String softwareRevision;

	private LifeTrakSyncR450(Context context) {
		mContext = context;
		mLifeTrakApplication = (LifeTrakApplication) mContext.getApplicationContext();
	}

	public static LifeTrakSyncR450 getInstance(Context context) {
		mLifeTrakSyncR450 = new LifeTrakSyncR450(context);
		mPreferenceWrapper = PreferenceWrapper.getInstance(mContext);
		mLifeTrakUpdateR450 = LifeTrakUpdateR450.newInstance(context);

		return mLifeTrakSyncR450;
	}

	public void setServiceInstance(SALBLEService bleService) {
		mSalutronService = bleService;
	}

	public void bindService() {
		Intent intent = new Intent(mContext, SALBLEService.class);
		mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	public void unbindService() {
		if (mRegistered)
			mContext.unbindService(mServiceConnection);
	}

	public void setSyncType(int syncType) {
		mSyncType = syncType;
	}

	public void startScan() {
		FlurryAgent.logEvent(DEVICE_SEARCH, true);
		mBluetoothDevices.clear();
		registerHandler();
		mSalutronService.startScan();
	}

	public void stopScan() {
		mBluetoothDevices.clear();
		mSalutronService.stopScan();
	}

	public int connectToDevice(String address, int model) {
		FlurryAgent.logEvent(DEVICE_INITIALIZE_CONNECT, true);
		FlurryAgent.endTimedEvent(DEVICE_SEARCH);
		return mSalutronService.connectToDevice(address, model);
	}

	public boolean startSync() {
		//LifeTrakLogger.configure();

		LifeTrakLogger.info("Sync Started From Watch - " + new Date());
		if (mSalutronService != null) {
			stopScan();
			registerHandler();
			mIsUpdateTimeAndDate = false;
			mHandler.postDelayed(new Runnable() {
				@Override

				public void run() {
					LifeTrakLogger.info(" Starting SALBLEService.COMMAND_GET_TIME");
					commandTimer.reset();
					commandTimer.startTimer(SALBLEService.COMMAND_GET_TIME);
					mCancelled = false;
					//status = mSalutronService.getStatisticalDataHeaders();
					LifeTrakLogger.info("mSalutronService.getCurrentTimeAndDate()");
					status = mSalutronService.getCurrentTimeAndDate();
					mIsUpdateTimeAndDate = false;
					LifeTrakLogger.info("startSync on LifeTrakSyncR450 status: " + status);


					if (status == SALStatus.NO_ERROR) {
						if (mSalutronSDKCallback != null) {
							mSalutronSDKCallback.onStartSync();
						}
					} else {
						if (mSalutronSDKCallback != null) {
							mSalutronSDKCallback.onError(status);
						}
					}
				}
			}, 1000);
		}

		return true;
	}

	public boolean salutronStatusActive(){
		if (status == SALStatus.NO_ERROR) {
			return true;
		}
		else{
			status = SALStatus.NO_ERROR;
			return false;
		}
	}

	public void cancelSync() {
		mCancelled = true;
		commandTimer.reset();
	}

	public void resumeSync() {
		mCancelled = false;
	}

	public boolean isRegistered() {
		return mRegistered;
	}

	public boolean isDisconnected() {
		return mDisconnected;
	}

	public boolean isSyncStarted() { return mSyncStarted; }

	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mSalutronService = binder.getService();

			if (mSalutronService != null) {
				registerHandler();
			}

			registerTelephonyService();
			mRegistered = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mSalutronService = null;
			mRegistered = false;
		}
	};

	private static void registerTelephonyService() {
		TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(mSalutronService.phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	public void registerHandler() {
		mSalutronService.registerDevListHandler(mHandler);
		mSalutronService.registerDevDataHandler(mHandler);
	}

	public void disconnectR450(){
		mSalutronService.disconnectFromDevice();
	}

	public static final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			final Bundle data = msg.getData();

			switch(msg.what) {
				case SALBLEService.GATT_DEVICE_FOUND_MSG:
					LifeTrakLogger.info("GATT_DEVICE_FOUND_MSG called on LifeTrakSyncR450");
					final BluetoothDevice device = data.getParcelable(BluetoothDevice.EXTRA_DEVICE);

					boolean deviceFound = false;

					for (BluetoothDevice bluetoothDevice : mBluetoothDevices) {
						String macAddress = device.getAddress();

						if (bluetoothDevice.getAddress().equals(macAddress) && device.getName() != null && device.getAddress() != null) {

							deviceFound = true;
							break;
						}
					}

					if (!deviceFound) {
						mBluetoothDevices.add(device);
						mSalutronService.stopScan();

						mHandler.postDelayed(new Runnable() {
							public void run() {
								if (mSalutronSDKCallback != null) {
									mSalutronSDKCallback.onDeviceFound(device, data);
								}
							}
						}, 750);
					}
					break;
				case SALBLEService.GATT_DEVICE_CONNECT_MSG:
					FlurryAgent.logEvent(DEVICE_CONNECTED, true);
					FlurryAgent.endTimedEvent(DEVICE_INITIALIZE_CONNECT);
					mDisconnected = false;

					if (mPreferenceWrapper.getPreferenceBooleanValue(SalutronLifeTrakUtility.NOTIFICATION_ENABLED)) {
						mSalutronService.enableANSServer();
					} else {
						mSalutronService.disableANSServer();
					}
					registerTelephonyService();

					final BluetoothDevice device2 = data.getParcelable(BluetoothDevice.EXTRA_DEVICE);

					if (mSalutronSDKCallback != null) {
						mSalutronSDKCallback.onDeviceConnected(device2);
					}


					break;
				case SALBLEService.GATT_DEVICE_READY_MSG:

					FlurryAgent.logEvent(DEVICE_READY, true);
					FlurryAgent.endTimedEvent(DEVICE_CONNECTED);
					mPreferenceWrapper.setPreferenceStringValue(SalutronLifeTrakUtility.SDK_VERSION,
							mSalutronService.getLibraryVersion()).synchronize();

					if (mSalutronSDKCallback != null) {
						mSalutronSDKCallback.onDeviceReady();
					}
					if (mLifeTrakApplication.getSelectedWatch() != null)
						mPreferenceWrapper.setPreferenceStringValue(LAST_R450_SYNC, mLifeTrakApplication.getSelectedWatch().getMacAddress()).synchronize();

					break;
				case SALBLEService.SAL_MSG_DEVICE_INFO:
					final int devInfoType = data.getInt(SALBLEService.SAL_DEVICE_INFO_TYPE);
					switch (devInfoType) {
						case SALBLEService.DEV_INFO_FIRMWARE_VERSION:
							onGetFirmware(data.getString(SALBLEService.SAL_DEVICE_INFO));
							break;
						case SALBLEService.DEV_INFO_SOFTWARE_VERSION:
							onGetSoftwareRevision(data.getString(SALBLEService.SAL_DEVICE_INFO));
							break;
					}
					break;
				case SALBLEService.GATT_DEVICE_DISCONNECT_MSG:
					LifeTrakLogger.info("GATT_DEVICE_DISCONNECT_MSG called on LifeTrakSyncR450");
					if (mSalutronSDKCallback != null) {
						mSalutronSDKCallback.onDeviceDisconnected();
					}

					LifeTrakLogger.info("Reconnect to LifeTrakSyncR450");


					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							String macAddress = null;
							if (mLifeTrakApplication.getSelectedWatch() != null)
								macAddress = mLifeTrakApplication.getSelectedWatch().getMacAddress();
							else
								macAddress = mPreferenceWrapper.getPreferenceStringValue(LAST_R450_SYNC);
							if (macAddress != null && !macAddress.contains(":"))
								macAddress = convertiOSToAndroidMacAddress(macAddress);
							LifeTrakLogger.info("Reconnect to LifeTrakSyncR450 with mac address = " + macAddress);

							mSalutronService.connectToDevice(macAddress, SALBLEService.MODEL_R415);
						}
					}, 3000);

					mDisconnected = true;
					mSyncStarted = false;
					LifeTrakLogger.info("disconnected on LifeTrakSyncR450");
					break;
				case SALBLEService.SAL_MSG_DEVICE_DATA_RECEIVED:

					final int dataType = data.getInt(SALBLEService.SAL_DEVICE_DATA_TYPE);
					commandTimer.stopTimer(dataType);

					LifeTrakLogger.info("SAL_MSG_DEVICE_DATA_RECEIVED data type: " + dataType);

				/*if (mCancelled) {
					mCancelled = false;
					return;
				}*/

					switch(dataType) {
						case SALBLEService.COMMAND_GET_STAT_DATA_HEADERS:
							LifeTrakLogger.info("COMMAND_GET_STAT_DATA_HEADERS called on LifeTrakSyncR450");
							FlurryAgent.logEvent(GET_DATA_HEADER, true);
							FlurryAgent.endTimedEvent(DEVICE_START_SYNC);
							List<SALStatisticalDataHeader> salStatisticalDataHeaders = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
							onGetStatisticalDataHeaders(salStatisticalDataHeaders);
							break;
						case SALBLEService.COMMAND_GET_DATA_POINTS_FOR_DATE:
							FlurryAgent.logEvent(GET_DATA_POINTS, true);
							FlurryAgent.endTimedEvent(GET_DATA_HEADER);
							LifeTrakLogger.info("COMMAND_GET_DATA_POINTS_FOR_DATE called on LifeTrakSyncR450");
							mSyncStarted = true;
							List<SALStatisticalDataPoint> salStatisticalDataPoints = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
							onGetStatisticalDataPoints(salStatisticalDataPoints);
							break;
						case SALBLEService.COMMAND_GET_AMBIENT_LIGHT_DBASE:
							FlurryAgent.logEvent(GET_LIGHT_DATA_POINTS, true);
							FlurryAgent.endTimedEvent(GET_DATA_POINTS);
							LifeTrakLogger.info("COMMAND_GET_AMBIENT_LIGHT_DBASE called on LifeTrakSyncR450");
							List<SALLightDataPoint> salLightDataPoints = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
							onGetLightDataPoints(salLightDataPoints);
							break;
						case SALBLEService.COMMAND_GET_WORKOUT_DB:
							FlurryAgent.logEvent(GET_WORKOUT, true);
							FlurryAgent.endTimedEvent(GET_LIGHT_DATA_POINTS);
							LifeTrakLogger.info("COMMAND_GET_WORKOUT_DB called on LifeTrakSyncR450");
							List<SALWorkoutInfo> salWorkoutInfos = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
							onGetWorkoutDatabase(salWorkoutInfos);
							break;
						case SALBLEService.COMMAND_GET_WORKOUT_STOP_DB:
							FlurryAgent.logEvent(GET_WORKOUT_STOP, true);
							FlurryAgent.endTimedEvent(GET_WORKOUT);
							LifeTrakLogger.info("COMMAND_GET_WORKOUT_STOP_DB called on LifeTrakSyncR450");
							List<SALWorkoutStopInfo> salWorkoutStopInfos = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
							onGetWorkoutStopDatabase(salWorkoutStopInfos);
							break;
						case SALBLEService.COMMAND_GET_SLEEP_DB:
							FlurryAgent.logEvent(GET_SLEEP_DATABASE, true);
							FlurryAgent.endTimedEvent(GET_WORKOUT_STOP);
							LifeTrakLogger.info("COMMAND_GET_SLEEP_DB called on LifeTrakSyncR450");
							List<SALSleepDatabase> salSleepDatabases = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
							onGetSleepDatabase(salSleepDatabases);
							break;
						case SALBLEService.COMMAND_GET_SLEEP_SETTING:
							FlurryAgent.logEvent(GET_SLEEP_SETTINGS, true);
							FlurryAgent.endTimedEvent(GET_SLEEP_DATABASE);
							LifeTrakLogger.info("COMMAND_GET_SLEEP_SETTING called on LifeTrakSyncR450");
							SALSleepSetting sleepSetting = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
							onGetSleepSetting(sleepSetting);
							break;
						case SALBLEService.COMMAND_GET_GOAL_STEPS:
							FlurryAgent.logEvent(GET_STEP_GOAL, true);
							FlurryAgent.endTimedEvent(GET_SLEEP_SETTINGS);
							LifeTrakLogger.info("COMMAND_GET_GOAL_STEPS called on LifeTrakSyncR450");
							long stepGoal = data.getLong(SALBLEService.SAL_DEVICE_DATA);
							onGetStepGoal(stepGoal);
							break;
						case SALBLEService.COMMAND_GET_GOAL_DISTANCE:
							FlurryAgent.logEvent(GET_DISTANCE_GOAL, true);
							FlurryAgent.endTimedEvent(GET_STEP_GOAL);
							LifeTrakLogger.info("COMMAND_GET_GOAL_DISTANCE called on LifeTrakSyncR450");
							long distance = data.getLong(SALBLEService.SAL_DEVICE_DATA);
							double distanceGoal = (double) distance / 100.0;
							onGetDistanceGoal(distanceGoal);
							break;
						case SALBLEService.COMMAND_GET_GOAL_CALORIE:
							FlurryAgent.logEvent(GET_CALORIE_GOAL, true);
							FlurryAgent.endTimedEvent(GET_DISTANCE_GOAL);
							LifeTrakLogger.info("COMMAND_GET_GOAL_CALORIE called on LifeTrakSyncR450");
							long calorieGoal = data.getLong(SALBLEService.SAL_DEVICE_DATA);
							onGetCalorieGoal(calorieGoal);
							break;
						case SALBLEService.COMMAND_GET_CALIBRATION_DATA:
							FlurryAgent.logEvent(GET_CALIBRATION_DATA, true);
							FlurryAgent.endTimedEvent(GET_CALORIE_GOAL);
							LifeTrakLogger.info("COMMAND_GET_CALIBRATION_DATA called on LifeTrakSyncR450");
							SALCalibration calibration = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
							onGetCalibrationData(calibration);
							break;
						case SALBLEService.COMMAND_GET_WAKEUP_SETTING:
							FlurryAgent.logEvent(GET_WAKEUP_SETTING, true);
							FlurryAgent.endTimedEvent(GET_CALIBRATION_DATA);
							LifeTrakLogger.info("COMMAND_GET_WAKEUP_SETTING called on LifeTrakSyncR450");
							SALWakeupSetting wakeupSetting = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
							onGetWakeupSetting(wakeupSetting);
							break;
						case SALBLEService.COMMAND_GET_NOTIFICATION_FILTER:
							FlurryAgent.logEvent(GET_NOTIFICATION, true);
							FlurryAgent.endTimedEvent(GET_WAKEUP_SETTING);
							LifeTrakLogger.info("COMMAND_GET_NOTIFICATION_FILTER called on LifeTrakSyncR450");
							byte[] notificationData = data.getByteArray(SALBLEService.SAL_DEVICE_DATA);
							onGetNotifications(notificationData);
							break;
						case SALBLEService.COMMAND_GET_ACTIVITY_ALERT_SETTING:
							FlurryAgent.logEvent(GET_ACTIVITY_ALERT, true);
							FlurryAgent.endTimedEvent(GET_NOTIFICATION);
							LifeTrakLogger.info("COMMAND_GET_ACTIVITY_ALERT_SETTING called on LifeTrakSyncR450");
							SALActivityAlertSetting activityAlertSetting = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
							onGetActivityAlertSetting(activityAlertSetting);
							break;
						case SALBLEService.COMMAND_GET_DAY_LIGHT_DETECT_SETTINGS:
							FlurryAgent.logEvent(GET_DAYLIGHT_SETTING, true);
							FlurryAgent.endTimedEvent(GET_ACTIVITY_ALERT);
							LifeTrakLogger.info("COMMAND_GET_DAY_LIGHT_DETECT_SETTINGS called on LifeTrakSyncR450");
							SALDayLightDetectSetting dayLightDetectSetting = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
							onGetDayLightDetectSetting(dayLightDetectSetting);
							break;
						case SALBLEService.COMMAND_GET_NIGHT_LIGHT_DETECT_SETTINGS:
							FlurryAgent.logEvent(GET_NIGHTLIGHT_SETTING, true);
							FlurryAgent.endTimedEvent(GET_DAYLIGHT_SETTING);
							LifeTrakLogger.info("COMMAND_GET_NIGHT_LIGHT_DETECT_SETTINGS called on LifeTrakSyncR450");
							SALNightLightDetectSetting nightLightDetectSetting = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
							onGetNightLightDetectSetting(nightLightDetectSetting);
							break;
						case SALBLEService.COMMAND_GET_USER_PROFILE:
							FlurryAgent.logEvent(GET_USER_PROFILE, true);
							FlurryAgent.endTimedEvent(GET_NIGHTLIGHT_SETTING);
							LifeTrakLogger.info("COMMAND_GET_USER_PROFILE called on LifeTrakSyncR450");
							SALUserProfile userProfile = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
							onGetUserProfile(userProfile);
							break;
						case SALBLEService.COMMAND_GET_TIME:
							FlurryAgent.logEvent(GET_TIME, true);
							FlurryAgent.endTimedEvent(GET_USER_PROFILE);
							LifeTrakLogger.info("COMMAND_GET_TIME called on LifeTrakSyncR450");
							SALTimeDate salTimeDate = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
							onGetTimeDate(salTimeDate);
							break;
						default:
							LifeTrakLogger.info("default called on LifeTrakSyncR450");
							break;
					}
					break;
				case SALBLEService.SAL_MSG_DEVICE_CHECKSUM_ERROR:
					LifeTrakLogger.info("SAL_MSG_DEVICE_CHECKSUM_ERROR called on LifeTrakSyncR450");
					if (mSalutronSDKCallback != null) {
						mSalutronSDKCallback.onError(SALStatus.ERROR_CHECKSUM);
					}
					break;
			}
		}
	};

	public void setSalutronSDKCallback(SalutronSDKCallback450 callback) {
		mSalutronSDKCallback = callback;
	}

	private static void onGetStatisticalDataHeaders(List<SALStatisticalDataHeader> salStatisticalDataHeaders) {
		LifeTrakLogger.info("onGetStatisticalDataHeaders on LifeTrakSyncR450");
		FlurryAgent.logEvent(DEVICE_START_SYNC, true);
		FlurryAgent.endTimedEvent(DEVICE_READY);

		mStatisticalDataHeaders.clear();
		mStatisticalDataPoints.clear();
		mLightDataPoints.clear();
		mHeaderIndexes.clear();

		int index = 0;

		for (SALStatisticalDataHeader salStatisticalDataHeader : salStatisticalDataHeaders) {
			StatisticalDataHeader dataHeader = null;

			String datestamp = String.format("%d/%d/%d", salStatisticalDataHeader.datestamp.nMonth, salStatisticalDataHeader.datestamp.nDay, salStatisticalDataHeader.datestamp.nYear);

			LifeTrakLogger.info("R450 - " + String.format("data header date: %s, steps: %d, distance: %f, calories:%f ", datestamp, salStatisticalDataHeader.totalSteps, salStatisticalDataHeader.totalDistance,salStatisticalDataHeader.totalCalorie));

			if (mLifeTrakApplication != null && mLifeTrakApplication.getSelectedWatch() != null &&
					StatisticalDataHeader.isExists(mContext, mLifeTrakApplication.getSelectedWatch().getId(), salStatisticalDataHeader)) {
				dataHeader = StatisticalDataHeader.getExistingDataHeader();

				dataHeader.setAllocationBlockIndex(salStatisticalDataHeader.allocationBlockIndex);
				dataHeader.setTotalSteps(salStatisticalDataHeader.totalSteps);
				dataHeader.setTotalDistance(salStatisticalDataHeader.totalDistance);
				dataHeader.setTotalCalorie(salStatisticalDataHeader.totalCalorie);
				dataHeader.setTotalSleep(salStatisticalDataHeader.totalSleep);
				dataHeader.setMinimumBPM(salStatisticalDataHeader.minimumBPM);
				dataHeader.setMaximumBPM(salStatisticalDataHeader.maximumBPM);
				dataHeader.setLightExposure(salStatisticalDataHeader.lightExposure);

				SALDateStamp salDateStamp = salStatisticalDataHeader.datestamp;
				Calendar calendar = Calendar.getInstance();

				calendar.set(Calendar.DAY_OF_MONTH, salDateStamp.nDay);
				calendar.set(Calendar.MONTH, salDateStamp.nMonth - 1);
				calendar.set(Calendar.YEAR, salDateStamp.nYear + 1900);

				dataHeader.setDateStampDay(salDateStamp.nDay);
				dataHeader.setDateStampMonth(salDateStamp.nMonth);
				dataHeader.setDateStampYear(salDateStamp.nYear);

				dataHeader.setDateStamp(calendar.getTime());

				SALTimeStamp salTimeStart = salStatisticalDataHeader.timeStart;
				dataHeader.setTimeStartSecond(salTimeStart.nSecond);
				dataHeader.setTimeStartMinute(salTimeStart.nMinute);
				dataHeader.setTimeStartHour(salTimeStart.nHour);

				SALTimeStamp salTimeEnd = salStatisticalDataHeader.timeEnd;
				dataHeader.setTimeEndSecond(salTimeEnd.nSecond);
				dataHeader.setTimeEndMinute(salTimeEnd.nMinute);
				dataHeader.setTimeEndHour(salTimeEnd.nHour);

				String query = "select count(_id) from StatisticalDataPoint where dataHeaderAndPoint = ?";

				Cursor cursor = DataSource.getInstance(mContext)
						.getReadOperation()
						.rawQuery(query, String.valueOf(dataHeader.getId()));

				if (cursor.moveToFirst()) {
					int dataPointCount = cursor.getInt(0);

					if (dataPointCount < 144) {
						mStatisticalDataHeaders.add(dataHeader);
						mHeaderIndexes.add(index);
					}
				}

			} else {
				dataHeader = StatisticalDataHeader.buildStatisticalDataHeader(mContext, salStatisticalDataHeader);
				mStatisticalDataHeaders.add(dataHeader);
				mHeaderIndexes.add(index);
			}

			index++;
		}

		if (mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onSyncStatisticalDataHeaders();
		}

		mDataHeaderIndexForDataPoint = 0;

		int status;

		if (mHeaderIndexes.size() > 0) {
			commandTimer.startTimer(SALBLEService.COMMAND_GET_DATA_POINTS_FOR_DATE);
			status = mSalutronService.getDataPointsOfSelectedDateStamp(mHeaderIndexes.get(mDataHeaderIndexForDataPoint));
		} else {
			commandTimer.startTimer(SALBLEService.COMMAND_GET_WORKOUT_DB);
			status = mSalutronService.getWorkoutDatabase();
		}

		if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onError(status);
		}
	}

	private static void onGetStatisticalDataPoints(List<SALStatisticalDataPoint> salStatisticalDataPoints) {
		LifeTrakLogger.info("onGetStatisticalDataPoints on LifeTrakSyncR450");

		mDataHeaderIndexForDataPoint++;

		List<StatisticalDataPoint> dataPoints = StatisticalDataPoint.buildStatisticalDataPoint(mContext, salStatisticalDataPoints);
		//mStatisticalDataHeaders.get(mDataHeaderIndexForDataPoint).setStatisticalDataPoints(dataPoints);
		mStatisticalDataPoints.add(dataPoints);

		if (mDataHeaderIndexForDataPoint < mStatisticalDataHeaders.size()) {

			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncStatisticalDataPoint((int) (((float) mDataHeaderIndexForDataPoint / mStatisticalDataHeaders.size()) * 100f));
			}

			commandTimer.startTimer(SALBLEService.COMMAND_GET_DATA_POINTS_FOR_DATE);
			int status = mSalutronService.getDataPointsOfSelectedDateStamp(mHeaderIndexes.get(mDataHeaderIndexForDataPoint));

			if (status != SALStatus.NO_ERROR) {
				if (mSalutronSDKCallback != null) {
					mSalutronSDKCallback.onError(status);
				}
			}

			StatisticalDataHeader dataHeader = mStatisticalDataHeaders.get(mDataHeaderIndexForDataPoint);
			for (SALStatisticalDataPoint salDataPoint : salStatisticalDataPoints) {
				LifeTrakLogger.info(String.format("data points data: %s distance:%f calories:%f steps:%d", dataHeader.getDateStamp().toString(),salDataPoint.distance,salDataPoint.calorie,salDataPoint.steps));
			}

		} else {

			if (mSalutronSDKCallback != null){
				mSalutronSDKCallback.onSyncLightDataPoints(0);
			}
			mDataHeaderIndexForLightPoint = 0;
			commandTimer.startTimer(SALBLEService.COMMAND_GET_AMBIENT_LIGHT_DBASE);
			int status = mSalutronService.getLightDataPoints(mHeaderIndexes.get(mDataHeaderIndexForLightPoint));

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}
		}
	}

	private static void onGetLightDataPoints(List<SALLightDataPoint> salLightDataPoints) {
		LifeTrakLogger.info("onGetLightDataPoints on LifeTrakSyncR450");

		List<StatisticalDataPoint> dataPoints = mStatisticalDataPoints.get(mDataHeaderIndexForLightPoint);

		mDataHeaderIndexForLightPoint++;

		List<LightDataPoint> lightDataPoints = LightDataPoint.buildLightDataPoint(mContext, salLightDataPoints);

		if (dataPoints.size() > 0) {
			for (int i=0;i<lightDataPoints.size();i++) {
				if (i < dataPoints.size()) {
					StatisticalDataPoint dataPoint = dataPoints.get(i);
					lightDataPoints.get(i).setWristOff02(dataPoint.getWristOff02());
					lightDataPoints.get(i).setWristOff24(dataPoint.getWristOff24());
					lightDataPoints.get(i).setWristOff46(dataPoint.getWristOff24());
					lightDataPoints.get(i).setWristOff68(dataPoint.getWristOff68());
					lightDataPoints.get(i).setWristOff810(dataPoint.getWristOff810());
				}
			}
		}

		mLightDataPoints.add(lightDataPoints);

		if (mDataHeaderIndexForLightPoint < mStatisticalDataHeaders.size()) {

			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncLightDataPoints((int) (((float) mDataHeaderIndexForLightPoint / mStatisticalDataHeaders.size()) * 100f));
			}

			commandTimer.startTimer(SALBLEService.COMMAND_GET_AMBIENT_LIGHT_DBASE);
			int status = mSalutronService.getLightDataPoints(mHeaderIndexes.get(mDataHeaderIndexForLightPoint));

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}

		} else {

			if(mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncWorkoutDatabase();
			}

			commandTimer.startTimer(SALBLEService.COMMAND_GET_WORKOUT_DB);
			int status = mSalutronService.getWorkoutDatabase();

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}
		}
	}

	private static void onGetWorkoutDatabase(List<SALWorkoutInfo> salWorkoutInfos) {
		LifeTrakLogger.info("onGetWorkoutDatabase on LifeTrakSyncR450");

		List<WorkoutInfo> workoutInfos = WorkoutInfo.buildWorkoutInfo(mContext, salWorkoutInfos);
		mWorkoutInfos.clear();
		mWorkoutInfos.addAll(workoutInfos);

		if (mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onSyncWorkoutStopDatabase(0);
		}

		if (mWorkoutInfos.size() > 0) {
			mWorkoutIndex = 0;
			mWorkoutStopInfos.clear();
			commandTimer.startTimer(SALBLEService.COMMAND_GET_WORKOUT_STOP_DB);
			int status = mSalutronService.getWorkoutStopDatabase(mWorkoutInfos.get(mWorkoutIndex).getWorkoutId());

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}

		} else {
			commandTimer.startTimer(SALBLEService.COMMAND_GET_SLEEP_DB);
			int status = mSalutronService.getSleepDatabase();

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}
		}
	}

	private static void onGetWorkoutStopDatabase(List<SALWorkoutStopInfo> salWorkoutStopInfos) {
		LifeTrakLogger.info("onGetWorkoutStopDatabase on LifeTrakSyncR450");

		mWorkoutIndex++;

		List<WorkoutStopInfo> workoutStopInfos = WorkoutStopInfo.buildWorkoutStopInfo(mContext, salWorkoutStopInfos);
		mWorkoutStopInfos.add(workoutStopInfos);

		if (mWorkoutIndex < mWorkoutInfos.size()) {
			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncWorkoutStopDatabase((mWorkoutIndex / mWorkoutInfos.size()) * 100);
			}

			commandTimer.startTimer(SALBLEService.COMMAND_GET_WORKOUT_STOP_DB);
			int workoutId = mWorkoutInfos.get(mWorkoutIndex).getWorkoutId();
			int status = mSalutronService.getWorkoutStopDatabase(workoutId);

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}

		} else {

			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncSleepDatabase();
			}

			commandTimer.startTimer(SALBLEService.COMMAND_GET_SLEEP_DB);
			int status = mSalutronService.getSleepDatabase();

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}
		}
	}

	private static void onGetSleepDatabase(List<SALSleepDatabase> salSleepDatabases) {
		LifeTrakLogger.info("onGetSleepDatabase on LifeTrakSyncR450");

		List<SleepDatabase> sleepDatabases = SleepDatabase.buildSleepDatabase(mContext, salSleepDatabases);
		mSleepDatabases.clear();
		mSleepDatabases.addAll(sleepDatabases);

		if (mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onSyncSleepSetting();
		}

		commandTimer.startTimer(SALBLEService.COMMAND_GET_SLEEP_SETTING);
		int status = mSalutronService.getSleepSetting();

		if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onError(status);
		}
	}

	private static void onGetSleepSetting(SALSleepSetting salSleepSetting) {
		LifeTrakLogger.info("onGetSleepSetting on LifeTrakSyncR450");

		mSleepSetting = SleepSetting.buildSleepSetting(mContext, salSleepSetting);

		if (mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onSyncStepGoal();
		}

		commandTimer.startTimer(SALBLEService.COMMAND_GET_GOAL_STEPS);
		int status = mSalutronService.getStepGoal();

		if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onError(status);
		}
	}

	private static void onGetStepGoal(long stepGoal) {
		LifeTrakLogger.info("onGetStepGoal on LifeTrakSyncR450");

		mStepGoal = stepGoal;

		if (mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onSyncDistanceGoal();
		}

		commandTimer.startTimer(SALBLEService.COMMAND_GET_GOAL_DISTANCE);
		int status = mSalutronService.getDistanceGoal();

		if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onError(status);
		}

	}

	private static void onGetDistanceGoal(double distanceGoal) {
		LifeTrakLogger.info("onGetDistanceGoal on LifeTrakSyncR450");

		mDistanceGoal = distanceGoal;

		if (mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onSyncCalorieGoal();
		}

		commandTimer.startTimer(SALBLEService.COMMAND_GET_GOAL_CALORIE);
		int status = mSalutronService.getCalorieGoal();

		if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onError(status);
		}
	}

	private static void onGetCalorieGoal(long calorieGoal) {
		LifeTrakLogger.info("onGetCalorieGoal on LifeTrakSyncR450");

		mCalorieGoal = calorieGoal;

		if (mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onSyncCalorieGoal();
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		mGoals.clear();

		if (mStatisticalDataHeaders.size() == 0) {
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH) + 1;
			int year = calendar.get(Calendar.YEAR) - 1900;

			Goal goal = new Goal(mContext);
			goal.setStepGoal(mStepGoal);
			goal.setDistanceGoal(mDistanceGoal);
			goal.setCalorieGoal(mCalorieGoal);
			goal.setSleepGoal(mSleepSetting.getSleepGoalMinutes());
			goal.setDate(calendar.getTime());
			goal.setDateStampDay(day);
			goal.setDateStampMonth(month);
			goal.setDateStampYear(year);

			mGoals.add(goal);
		} else {
			for (StatisticalDataHeader dataHeader : mStatisticalDataHeaders) {
				calendar.setTime(dataHeader.getDateStamp());

				int day = calendar.get(Calendar.DAY_OF_MONTH);
				int month = calendar.get(Calendar.MONTH) + 1;
				int year = calendar.get(Calendar.YEAR) - 1900;

				if (mLifeTrakApplication.getSelectedWatch() != null) {
					List<Goal> goals = DataSource.getInstance(mContext)
							.getReadOperation()
							.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
									String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()), String.valueOf(day), String.valueOf(month), String.valueOf(year))
							.getResults(Goal.class);

					if (goals.size() > 0)
						continue;
				}

				Goal goal = new Goal(mContext);
				goal.setStepGoal(mStepGoal);
				goal.setDistanceGoal(mDistanceGoal);
				goal.setCalorieGoal(mCalorieGoal);
				goal.setSleepGoal(mSleepSetting.getSleepGoalMinutes());
				goal.setDate(calendar.getTime());
				goal.setDateStampDay(day);
				goal.setDateStampMonth(month);
				goal.setDateStampYear(year);

				mGoals.add(goal);
			}
		}

		mCalibrationIndex = 0;

		commandTimer.startTimer(SALBLEService.COMMAND_GET_CALIBRATION_DATA);
		int status = mSalutronService.getCalibrationData(SALCalibration.AUTO_EL_SETTING);

		if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onError(status);
		}
	}

	private static void onGetCalibrationData(SALCalibration salCalibrationData) {
		LifeTrakLogger.info("onGetCalibrationData on LifeTrakSyncR450");

		if (mCalibrationData == null)
			mCalibrationData = new CalibrationData(mContext);

		mCalibrationIndex++;

		if (mCalibrationIndex < 4) {
			int type = SALCalibration.AUTO_EL_SETTING;

			switch(salCalibrationData.getCalibrationType()) {
				case SALCalibration.AUTO_EL_SETTING:
					mCalibrationData.setAutoEL(salCalibrationData.getCalibrationValue());
					type = SALCalibration.STEP_CALIBRATION;
					break;
				case SALCalibration.STEP_CALIBRATION:
					mCalibrationData.setStepCalibration(salCalibrationData.getCalibrationValue());
					type = SALCalibration.WALK_DISTANCE_CALIBRATION;
					break;
				case SALCalibration.WALK_DISTANCE_CALIBRATION:
					mCalibrationData.setDistanceCalibrationRun(salCalibrationData.getCalibrationValue());
					type = SALCalibration.CALORIE_CALIBRATION;
					break;
				case SALCalibration.CALORIE_CALIBRATION:
					mCalibrationData.setCaloriesCalibration(salCalibrationData.getCalibrationValue());
					break;
			}

			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncCalibrationData();
			}

			commandTimer.startTimer(SALBLEService.COMMAND_GET_CALIBRATION_DATA);
			int status = mSalutronService.getCalibrationData(type);

			if (status != SALStatus.NO_ERROR) {
				if (mSalutronSDKCallback != null) {
					mSalutronSDKCallback.onError(status);
				}
			}
		} else {

			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncWakeupSetting();
			}

			mWakeupIndex = 0;
			commandTimer.startTimer(SALBLEService.COMMAND_GET_WAKEUP_SETTING);
			int status = mSalutronService.getWakeupSettingData(SALWakeupSetting.WAKEUP_ALERT_STATUS);

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}
		}
	}

	private static void onGetWakeupSetting(SALWakeupSetting salWakeupSetting) {
		LifeTrakLogger.info("onGetWakeupSetting on LifeTrakSyncR450");

		if (mWakeupSetting == null)
			mWakeupSetting = new WakeupSetting(mContext);

		mWakeupIndex++;

		if (mWakeupIndex < 6) {
			int type = SALWakeupSetting.WAKEUP_ALERT_STATUS;

			switch (salWakeupSetting.getWakeupSettingType()) {
				case SALWakeupSetting.WAKEUP_ALERT_STATUS:
					mWakeupSetting.setEnabled(salWakeupSetting.getWakeupSetting() == SALWakeupSetting.ENABLE);
					type = SALWakeupSetting.WAKEUP_TIME;
					break;
				case SALWakeupSetting.WAKEUP_TIME:
					mWakeupSetting.setTime(salWakeupSetting.getWakeupSetting());
					mWakeupSetting.setWakeupTimeHour(salWakeupSetting.getWakeupHour());
					mWakeupSetting.setWakeupTimeMinute(salWakeupSetting.getWakeupMinute());
					type = SALWakeupSetting.WAKEUP_WINDOW;
					break;
				case SALWakeupSetting.WAKEUP_WINDOW:
					mWakeupSetting.setWindow(salWakeupSetting.getWakeupSetting());
					type = SALWakeupSetting.SNOOZE_ALERT_STATUS;
					break;
				case SALWakeupSetting.SNOOZE_ALERT_STATUS:
					mWakeupSetting.setSnoozeEnabled(salWakeupSetting.getWakeupSetting() == SALWakeupSetting.ENABLE);
					type = SALWakeupSetting.SNOOZE_TIME;
					break;
				case SALWakeupSetting.SNOOZE_TIME:
					mWakeupSetting.setSnoozeTime(salWakeupSetting.getWakeupSetting());
					break;
			}

			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncWakeupSetting();
			}

			commandTimer.startTimer(SALBLEService.COMMAND_GET_WAKEUP_SETTING);
			int status = mSalutronService.getWakeupSettingData(type);

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}
		} else {

			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncNotifications();
			}

			commandTimer.startTimer(SALBLEService.COMMAND_GET_NOTIFICATION_FILTER);
			int status = mSalutronService.getNotificationStatus();

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}
		}
	}

	private static void onGetNotifications(byte[] data) {
		LifeTrakLogger.info("onGetNotifications on LifeTrakSyncR450");

		if (mNotification == null)
			mNotification = new Notification(mContext);

		mNotification.setSimpleAlertEnabled(isNotificationEnabled(data[0] & 0xff));
		mNotification.setEmailEnabled(isNotificationEnabled(data[1] & 0xff));
		mNotification.setNewsEnabled(isNotificationEnabled(data[2] & 0xff));
		mNotification.setIncomingCallEnabled(isNotificationEnabled(data[3] & 0xff));
		mNotification.setMissedCallEnabled(isNotificationEnabled(data[4] & 0xff));
		mNotification.setSmsEnabled(isNotificationEnabled(data[5] & 0xff));
		mNotification.setVoiceMailEnabled(isNotificationEnabled(data[6] & 0xff));
		mNotification.setScheduleEnabled(isNotificationEnabled(data[7] & 0xff));
		mNotification.setHighPriorityEnabled(isNotificationEnabled(data[8] & 0xff));
		mNotification.setInstantMessageEnabled(isNotificationEnabled(data[9] & 0xff));

		if (mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onSyncActivityAlertSettingsData();
		}

		mActivityAlertIndex = 0;
		commandTimer.startTimer(SALBLEService.COMMAND_GET_ACTIVITY_ALERT_SETTING);
		int status = mSalutronService.getActivityAlertSettingData(SALActivityAlertSetting.ALERT_STATUS);

		if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onError(status);
		}
	}

	private static void onGetActivityAlertSetting(SALActivityAlertSetting salActivityAlertSetting) {
		LifeTrakLogger.info("onGetActivityAlertSetting on LifeTrakSyncR450");

		if (mActivityAlertSetting == null)
			mActivityAlertSetting = new ActivityAlertSetting(mContext);

		mActivityAlertIndex++;

		if (mActivityAlertIndex < 6) {
			int type = SALActivityAlertSetting.ALERT_STATUS;

			switch (salActivityAlertSetting.getActivityAlertSettingType()) {
				case SALActivityAlertSetting.ALERT_STATUS:
					mActivityAlertSetting.setEnabled(salActivityAlertSetting.getActivityAlertStatus() == SALActivityAlertSetting.ENABLE);
					type = SALActivityAlertSetting.ALERT_TIME_INTERVAL;
					break;
				case SALActivityAlertSetting.ALERT_TIME_INTERVAL:
					mActivityAlertSetting.setTimeInterval(salActivityAlertSetting.getTimeInterval());
					type = SALActivityAlertSetting.ALERT_STEPS_THRESHOLD;
					break;
				case SALActivityAlertSetting.ALERT_STEPS_THRESHOLD:
					mActivityAlertSetting.setStepsThreshold(salActivityAlertSetting.getStepsThreshold());
					type = SALActivityAlertSetting.ALERT_START_TIME;
					break;
				case SALActivityAlertSetting.ALERT_START_TIME:
					int startTime = salActivityAlertSetting.getActivityAlertSetting();
					int startHour = (startTime >> 8) & 0xFF;
					int startMinute = startTime & 0xFF;
					mActivityAlertSetting.setStartTime((startHour * 60) + startMinute);
					type = SALActivityAlertSetting.ALERT_END_TIME;
					break;
				case SALActivityAlertSetting.ALERT_END_TIME:
					int endTime = salActivityAlertSetting.getActivityAlertSetting();
					int endHour = (endTime >> 8) & 0xFF;
					int endMinute = endTime & 0xFF;
					mActivityAlertSetting.setEndTime((endHour * 60) + endMinute);
					break;
			}

			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncActivityAlertSettingsData();
			}

			commandTimer.startTimer(SALBLEService.COMMAND_GET_ACTIVITY_ALERT_SETTING);
			int status = mSalutronService.getActivityAlertSettingData(type);

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}

		} else {

			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncDayLightSettingsData();
			}

			mDayLightDetectIndex = 0;
			commandTimer.startTimer(SALBLEService.COMMAND_GET_DAY_LIGHT_DETECT_SETTINGS);
			int status = mSalutronService.getDayLightSettingData(SALDayLightDetectSetting.DETECT_STATUS);

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}
		}
	}

	private static void onGetDayLightDetectSetting(SALDayLightDetectSetting salDayLightDetectSetting) {
		LifeTrakLogger.info("onGetDayLightDetectSetting on LifeTrakSyncR450");

		if (mDayLightDetectSetting == null)
			mDayLightDetectSetting = new DayLightDetectSetting(mContext);

		mDayLightDetectIndex++;

		if (mDayLightDetectIndex < 10) {
			int type = SALDayLightDetectSetting.DETECT_STATUS;

			switch (salDayLightDetectSetting.getLightDetectSettingType()) {
				case SALDayLightDetectSetting.DETECT_STATUS:
					mDayLightDetectSetting.setEnabled(salDayLightDetectSetting.getLightDetectStatus() == SALDayLightDetectSetting.ENABLE);
					type = SALDayLightDetectSetting.DETECT_EXPOSURE_LEVEL;
					break;
				case SALDayLightDetectSetting.DETECT_EXPOSURE_LEVEL:
					mDayLightDetectSetting.setExposureLevel(salDayLightDetectSetting.getExposureLevel());
					type = SALDayLightDetectSetting.DETECT_EXPOSURE_DURATION;
					break;
				case SALDayLightDetectSetting.DETECT_EXPOSURE_DURATION:
					mDayLightDetectSetting.setExposureDuration(salDayLightDetectSetting.getExposureDuration());
					type = SALDayLightDetectSetting.DETECT_START_TIME;
					break;
				case SALDayLightDetectSetting.DETECT_START_TIME:
					int startTime = salDayLightDetectSetting.getLightDetectSetting();
					int startHour = (startTime >> 8) & 0xFF;
					int startMinute = startTime & 0xFF;
					mDayLightDetectSetting.setStartTime((startHour * 60) + startMinute);
					type = SALDayLightDetectSetting.DETECT_END_TIME;
					break;
				case SALDayLightDetectSetting.DETECT_END_TIME:
					int endTime = salDayLightDetectSetting.getLightDetectSetting();
					int endHour = (endTime >> 8) & 0xFF;
					int endMinute = endTime & 0xFF;
					mDayLightDetectSetting.setEndTime((endHour * 60) + endMinute);
					type = SALDayLightDetectSetting.DETECT_LOW_THRESHOLD;
					break;
				case SALDayLightDetectSetting.DETECT_LOW_THRESHOLD:
					mDayLightDetectSetting.setDetectLowThreshold(salDayLightDetectSetting.getLowThresholdValue());
					type = SALDayLightDetectSetting.DETECT_MED_THRESHOLD;
					break;
				case SALDayLightDetectSetting.DETECT_MED_THRESHOLD:
					mDayLightDetectSetting.setDetectMediumThreshold(salDayLightDetectSetting.getMediumThresholdValue());
					type = SALDayLightDetectSetting.DETECT_HIGH_THRESHOLD;
					break;
				case SALDayLightDetectSetting.DETECT_HIGH_THRESHOLD:
					mDayLightDetectSetting.setDetectHighThreshold(salDayLightDetectSetting.getHighThresholdValue());
					type = SALDayLightDetectSetting.DETECT_INTERVAL;
					break;
				case SALDayLightDetectSetting.DETECT_INTERVAL:
					mDayLightDetectSetting.setInterval(salDayLightDetectSetting.getInterval());
					break;
			}

			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncDayLightSettingsData();
			}

			commandTimer.startTimer(SALBLEService.COMMAND_GET_DAY_LIGHT_DETECT_SETTINGS);
			int status = mSalutronService.getDayLightSettingData(type);

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}
		} else {
			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncNightLightSettingsData();
			}

			mNightLightDetectIndex = 0;
			commandTimer.startTimer(SALBLEService.COMMAND_GET_NIGHT_LIGHT_DETECT_SETTINGS);
			int status = mSalutronService.getNightLightSettingData(SALNightLightDetectSetting.DETECT_STATUS);

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}
		}
	}

	private static void onGetNightLightDetectSetting(SALNightLightDetectSetting salNightLightDetectSetting) {
		LifeTrakLogger.info("onGetNightLightDetectSetting on LifeTrakSyncR450");

		if (mNightLightDetectSetting == null)
			mNightLightDetectSetting = new NightLightDetectSetting(mContext);

		mNightLightDetectIndex++;

		if (mNightLightDetectIndex < 9) {
			int type = SALNightLightDetectSetting.DETECT_STATUS;

			switch (salNightLightDetectSetting.getLightDetectSettingType()) {
				case SALNightLightDetectSetting.DETECT_STATUS:
					mNightLightDetectSetting.setEnabled(salNightLightDetectSetting.getLightDetectStatus() == SALNightLightDetectSetting.ENABLE);
					type = SALNightLightDetectSetting.DETECT_EXPOSURE_LEVEL;
					break;
				case SALNightLightDetectSetting.DETECT_EXPOSURE_LEVEL:
					mNightLightDetectSetting.setExposureLevel(salNightLightDetectSetting.getExposureLevel());
					type = SALNightLightDetectSetting.DETECT_EXPOSURE_DURATION;
					break;
				case SALNightLightDetectSetting.DETECT_EXPOSURE_DURATION:
					mNightLightDetectSetting.setExposureDuration(salNightLightDetectSetting.getExposureDuration());
					type = SALNightLightDetectSetting.DETECT_START_TIME;
					break;
				case SALNightLightDetectSetting.DETECT_START_TIME:
					int startTime = salNightLightDetectSetting.getLightDetectSetting();
					int startHour = (startTime >> 8) & 0xFF;
					int startMinute = startTime & 0xFF;
					mNightLightDetectSetting.setStartTime((startHour * 60) + startMinute);
					type = SALNightLightDetectSetting.DETECT_END_TIME;
					break;
				case SALNightLightDetectSetting.DETECT_END_TIME:
					int endTime = salNightLightDetectSetting.getLightDetectSetting();
					int endHour = (endTime >> 8) & 0xFF;
					int endMinute = endTime & 0xFF;
					mNightLightDetectSetting.setEndTime((endHour * 60) + endMinute);
					type = SALNightLightDetectSetting.DETECT_LOW_THRESHOLD;
					break;
				case SALNightLightDetectSetting.DETECT_LOW_THRESHOLD:
					mNightLightDetectSetting.setDetectLowThreshold(salNightLightDetectSetting.getLowThresholdValue());
					type = SALNightLightDetectSetting.DETECT_MED_THRESHOLD;
					break;
				case SALNightLightDetectSetting.DETECT_MED_THRESHOLD:
					mNightLightDetectSetting.setDetectMediumThreshold(salNightLightDetectSetting.getMediumThresholdValue());
					type = SALNightLightDetectSetting.DETECT_HIGH_THRESHOLD;
					break;
				case SALNightLightDetectSetting.DETECT_HIGH_THRESHOLD:
					mNightLightDetectSetting.setDetectHighThreshold(salNightLightDetectSetting.getHighThresholdValue());
					break;
			}

			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncNightLightSettingsData();
			}

			commandTimer.startTimer(SALBLEService.COMMAND_GET_NIGHT_LIGHT_DETECT_SETTINGS);
			int status = mSalutronService.getNightLightSettingData(type);

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}
		} else {

			if (mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onSyncUserProfile();
			}

			commandTimer.startTimer(SALBLEService.COMMAND_GET_USER_PROFILE);
			int status = mSalutronService.getUserProfile();

			if (status != SALStatus.NO_ERROR && mSalutronSDKCallback != null) {
				mSalutronSDKCallback.onError(status);
			}
		}
	}

	private static void onGetUserProfile(SALUserProfile salUserProfile) {
		LifeTrakLogger.info("onGetUserProfile on LifeTrakSyncR450");
		mUserProfile = UserProfile.buildUserProfile(mContext, salUserProfile);


		mHandler.postDelayed(new Runnable() {
			public void run() {
				SALConnectionSetting connect = new SALConnectionSetting();
				connect.setConnectionSettingType(SALConnectionSetting.BLE_OPERATION);
				connect.setBLEWristOffOperationStatus(SALConnectionSetting.ENABLE);
				connect.setBLESleepOperationStatus(SALConnectionSetting.ENABLE);
				mSalutronService.updateConnectionSettingData(connect);
				LifeTrakLogger.info("SET WRIST OFF TO ON");

			}
		}, MINI_SYNC_DELAY);
		mHandler.postDelayed(new Runnable() {
			public void run() {
				commandTimer.reset();
				commandTimer.startTimer(SALBLEService.DEV_INFO_FIRMWARE_VERSION);
				mSalutronService.getFirmwareRevision();
			}

		}, MINI_SYNC_DELAY * 4);
	}



	private static void onGetTimeDate(final SALTimeDate salTimeDate) {
		LifeTrakLogger.info("onGetTimeDate on LifeTrakSyncR450");

		mTimeDate = TimeDate.buildTimeDate(mContext, salTimeDate);
		//mSalutronService.enableANSServer();
		if (mSalutronSDKCallback != null) {
			mSalutronSDKCallback.onSyncTime();
		}
		if (mIsUpdateTimeAndDate && mLifeTrakApplication.getTimeDate() != null) {

			mIsUpdateTimeAndDate = false;
			LifeTrakLogger.info("Update mLifeTrakApplication Time from Settings");
			int dateFormat = mLifeTrakApplication.getTimeDate().getDateFormat();
			int hourFormat = mLifeTrakApplication.getTimeDate().getHourFormat();
			int displaySize = mLifeTrakApplication.getTimeDate().getDisplaySize();
			mTimeDate.setDateFormat(dateFormat);
			mTimeDate.setHourFormat(hourFormat);
			mTimeDate.setDisplaySize(displaySize);
			mLifeTrakApplication.setTimeDate(mTimeDate);

			return;
		}

		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
			//Lollipop
			if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME)) {
				SALTimeDate mSALTimeDate = new SALTimeDate();
				mSALTimeDate.setToNow();
				mSALTimeDate.setDateFormat(salTimeDate.getDateFormat());
				mSALTimeDate.setTimeFormat(salTimeDate.getHourFormat());
				mSALTimeDate.setTimeDisplaySize(salTimeDate.getTimeDisplaySize());
				LifeTrakLogger.info("updateTimeAndDate on LifeTrakSyncR450");
				mSalutronService.updateTimeAndDate(mSALTimeDate);

			}

			mHandler.postDelayed(new Runnable() {
				public void run() {
					LifeTrakLogger.info("getStatisticalDataHeaders on LifeTrakSyncR450");
					commandTimer.startTimer(SALBLEService.COMMAND_GET_STAT_DATA_HEADERS);
					mSalutronService.getStatisticalDataHeaders();
				}
			}, MINI_SYNC_DELAY * 4);
		} else {
			//Kitkat
			mHandler.postDelayed(new Runnable() {
				public void run() {
					if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME)) {
						SALTimeDate mSALTimeDate = new SALTimeDate();
						mSALTimeDate.setToNow();
						if (mLifeTrakApplication.getTimeDate() != null) {
							int dateFormat = salTimeDate.getDateFormat();
							int hourFormat = salTimeDate.getHourFormat();
							int displaySize = salTimeDate.getTimeDisplaySize();

							mSALTimeDate.setDateFormat(dateFormat);
							mSALTimeDate.setTimeFormat(hourFormat);
							mSALTimeDate.setTimeDisplaySize(displaySize);
						} else {
							mSALTimeDate.setDateFormat(salTimeDate.getDateFormat());
							mSALTimeDate.setTimeFormat(salTimeDate.getHourFormat());
							mSALTimeDate.setTimeDisplaySize(salTimeDate.getTimeDisplaySize());
						}
						LifeTrakLogger.info("updateTimeAndDate on LifeTrakSyncR450");
						mSalutronService.updateTimeAndDate(mSALTimeDate);

					}

				}
			}, 350);
			mHandler.postDelayed(new Runnable() {
				public void run() {
					LifeTrakLogger.info("getStatisticalDataHeaders on LifeTrakSyncR450");
					commandTimer.startTimer(SALBLEService.COMMAND_GET_STAT_DATA_HEADERS);
					mSalutronService.getStatisticalDataHeaders();
				}
			}, MINI_SYNC_DELAY);
		}
	}

	private static void onGetFirmware(String firmwareVersion) {
		Pattern pattern = Pattern.compile("[^0-9]");
		Matcher matcher = pattern.matcher(firmwareVersion);
		String number = matcher.replaceAll("");

		LifeTrakSyncR450.firmwareVersion = "V" +number;
		int delay = 750;

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				LifeTrakLogger.info("getStatisticalDataHeaders on LifeTrakSyncR450");
				commandTimer.startTimer(SALBLEService.DEV_INFO_SOFTWARE_VERSION);
				mSalutronService.getSoftwareRevision();
			}
		}, delay * 2);


	}

	private static void onGetSoftwareRevision(String softwareRevision) {
		Pattern pattern = Pattern.compile("[^0-9]");
		Matcher matcher = pattern.matcher(softwareRevision);
		String number = matcher.replaceAll("");

		LifeTrakSyncR450.softwareRevision = "V" +number;

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
//				if (mSalutronSDKCallback != null)
//					mSalutronSDKCallback.onSyncFinish();

				syncFinish();
			}
		}, MINI_SYNC_DELAY);


	}


	private static void syncFinish(){
		mSyncStarted = false;
		commandTimer.reset();
		mHandler.postDelayed(new Runnable() {
			public void run() {
				if (mSalutronSDKCallback != null) {
					if (mLifeTrakApplication.getSelectedWatch() != null) {
						List<UserProfile> userProfiles = DataSource.getInstance(mContext)
								.getReadOperation()
								.query("watchUserProfile = ?", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
								.getResults(UserProfile.class);

						if (userProfiles.size() > 0) {
							//updateUserProfile(userProfiles.get(0));
						}
					}
				}
			}
		}, 750);

		mHandler.postDelayed(new Runnable() {
			public void run() {
//                boolean autoSyncTime = mPreferenceWrapper.getPreferenceBooleanValue(SalutronLifeTrakUtility.AUTO_SYNC_TIME);
//
//                if (mLifeTrakApplication.getSelectedWatch() != null && autoSyncTime) {
//                    updateTimeAndDate(mLifeTrakApplication.getTimeDate());
//                } else if (autoSyncTime) {
//                    updateTimeAndDate(mTimeDate);
//                }

				mSalutronSDKCallback.onSyncFinish();
			}
		}, 750 * 2);
	}

	public void storeData(Watch watch) {
		LifeTrakLogger.info("storeData on LifeTrakSyncR450");

		watch.setWatchSoftWare(LifeTrakSyncR450.softwareRevision);
		watch.setWatchFirmWare(LifeTrakSyncR450.firmwareVersion);

		if (watch.getId() == 0)
			watch.insert();

		insertGoalsWithWatch(watch, mGoals);
		insertSleepDatabaseWithWatch(watch, mSleepDatabases);
		insertWorkoutDatabaseWithWatch(watch, mWorkoutInfos);
		insertStatisticalDataHeaderWithWatch(watch, mStatisticalDataHeaders);
		insertCalibrationDataWithWatch(watch, mCalibrationData);
		insertSleepSettingWithWatch(watch, mSleepSetting);
		insertTimeDateWithWatch(watch, mTimeDate);
		insertUserProfileWithWatch(watch, mUserProfile);
		insertNotificationWithWatch(watch, mNotification);
		insertWakeupSettingWithWatch(watch, mWakeupSetting);
		insertActivityAlertSettingWithWatch(watch, mActivityAlertSetting);
		insertDayLightDetectSettingWithWatch(watch, mDayLightDetectSetting);
		insertNightLightDetectSettingWithWatch(watch, mNightLightDetectSetting);

		mPreferenceWrapper.setPreferenceLongValue(SalutronLifeTrakUtility.LAST_CONNECTED_WATCH_ID, watch.getId()).synchronize();
		LifeTrakLogger.info("Sync End From Watch - " + new Watch());
	}

	private void insertStatisticalDataHeaderWithWatch(Watch watch, List<StatisticalDataHeader> dataHeaders) {
		int index = 0;
		synchronized (dataHeaders) {
			for (StatisticalDataHeader dataHeader : dataHeaders) {
				dataHeader.setContext(mContext);
				dataHeader.setWatch(watch);

				if (dataHeader.getId() == 0) {
					dataHeader.setStatisticalDataPoints(null);
					dataHeader.setLightDataPoints(null);
					dataHeader.insert();
				} else {
					dataHeader.update();
				}

				String query = "select _id from StatisticalDataPoint where dataHeaderAndPoint = ?";

				Cursor cursor = DataSource.getInstance(mContext)
						.getReadOperation()
						.rawQuery(query, String.valueOf(dataHeader.getId()));

				int dataPointCount = 0;

				if (cursor.moveToFirst())
					dataPointCount = cursor.getCount() - 1;

				cursor.close();

				List<StatisticalDataPoint> dataPoints = mStatisticalDataPoints.get(index);

				List<StatisticalDataPoint> lastDataPoints = DataSource.getInstance(mContext)
						.getReadOperation()
						.query("dataHeaderAndPoint = ?", String.valueOf(dataHeader.getId()))
						.orderBy("_id", SalutronLifeTrakUtility.SORT_DESC)
						.limit(1)
						.getResults(StatisticalDataPoint.class);

				for (int i = dataPointCount; i < dataPoints.size(); i++) {
					StatisticalDataPoint dataPoint = dataPoints.get(i);
					dataPoint.setContext(mContext);
					dataPoint.setStatisticalDataHeader(dataHeader);

					if (i == dataPointCount && lastDataPoints.size() > 0) {
						StatisticalDataPoint lastDataPoint = lastDataPoints.get(0);
						lastDataPoint.setContext(mContext);
						lastDataPoint.setStatisticalDataHeader(dataHeader);
						lastDataPoint.setAverageHR(dataPoint.getAverageHR());
						lastDataPoint.setDistance(dataPoint.getDistance());
						lastDataPoint.setSteps(dataPoint.getSteps());
						lastDataPoint.setCalorie(dataPoint.getCalorie());
						lastDataPoint.setSleepPoint02(dataPoint.getSleepPoint02());
						lastDataPoint.setSleepPoint24(dataPoint.getSleepPoint24());
						lastDataPoint.setSleepPoint46(dataPoint.getSleepPoint46());
						lastDataPoint.setSleepPoint68(dataPoint.getSleepPoint68());
						lastDataPoint.setSleepPoint810(dataPoint.getSleepPoint810());
						lastDataPoint.setDominantAxis(dataPoint.getDominantAxis());
						lastDataPoint.setLux(dataPoint.getLux());
						lastDataPoint.setAxisDirection(dataPoint.getAxisDirection());
						lastDataPoint.setAxisMagnitude(dataPoint.getAxisMagnitude());
						lastDataPoint.update();
					} else {
						dataPoint.insert();
					}
				}

				query = "select _id from LightDataPoint where dataHeaderAndPoint = ?";

				cursor = DataSource.getInstance(mContext)
						.getReadOperation()
						.rawQuery(query, String.valueOf(dataHeader.getId()));

				int lightPointCount = 0;

				if (cursor.moveToFirst())
					lightPointCount = cursor.getCount() - 1;

				cursor.close();

				List<LightDataPoint> lightDataPoints = mLightDataPoints.get(index);
				List<LightDataPoint> lastLightDataPoints = DataSource.getInstance(mContext)
						.getReadOperation()
						.query("dataHeaderAndPoint = ?", String.valueOf(dataHeader.getId()))
						.orderBy("_id", SalutronLifeTrakUtility.SORT_DESC)
						.limit(1)
						.getResults(LightDataPoint.class);

				for (int i = lightPointCount; i < lightDataPoints.size(); i++) {
					LightDataPoint lightDataPoint = lightDataPoints.get(i);
					lightDataPoint.setContext(mContext);
					lightDataPoint.setStatisticalDataHeader(dataHeader);

					if (i == lightPointCount && lastLightDataPoints.size() > 0) {
						LightDataPoint lastLightDataPoint = lastLightDataPoints.get(0);
						lastLightDataPoint.setRedValue(lightDataPoint.getRedValue());
						lastLightDataPoint.setGreenValue(lightDataPoint.getGreenValue());
						lastLightDataPoint.setBlueValue(lightDataPoint.getBlueValue());
						lastLightDataPoint.setIntegrationTime(lightDataPoint.getIntegrationTime());
						lastLightDataPoint.setSensorGain(lightDataPoint.getSensorGain());
						lastLightDataPoint.update();
					} else {
						lightDataPoint.insert();
					}
				}

				index++;
			}
		}
	}

	private void insertGoalsWithWatch(Watch watch, List<Goal> goals) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		if (goals.size() == 0) {
			if (mLifeTrakApplication.getSelectedWatch() != null) {
				List<Goal> goalTemp = DataSource.getInstance(mContext)
						.getReadOperation()
						.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
								String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()), String.valueOf(day), String.valueOf(month), String.valueOf(year))
						.getResults(Goal.class);

				Goal goal = new Goal(mContext);
				goal.setStepGoal(mStepGoal);
				goal.setDistanceGoal(mDistanceGoal);
				goal.setCalorieGoal(mCalorieGoal);
				goal.setSleepGoal(mSleepSetting.getSleepGoalMinutes());
				goal.setDate(calendar.getTime());
				goal.setDateStampDay(day);
				goal.setDateStampMonth(month);
				goal.setDateStampYear(year);
				goals.add(goal);

				if (goalTemp.size() == 0){
					insertGoals(goals, watch);
				}
			}
			else
			{
				Goal goal = new Goal(mContext);
				goal.setStepGoal(mStepGoal);
				goal.setDistanceGoal(mDistanceGoal);
				goal.setCalorieGoal(mCalorieGoal);
				goal.setSleepGoal(mSleepSetting.getSleepGoalMinutes());
				goal.setDate(calendar.getTime());
				goal.setDateStampDay(day);
				goal.setDateStampMonth(month);
				goal.setDateStampYear(year);
				goals.add(goal);
				insertGoals(goals, watch);
			}
		}
		else{
//			Goal goal = new Goal(mContext);
//			goal.setStepGoal(mStepGoal);
//			goal.setDistanceGoal(mDistanceGoal);
//			goal.setCalorieGoal(mCalorieGoal);
//			goal.setSleepGoal(mSleepSetting.getSleepGoalMinutes());
//			goal.setDate(calendar.getTime());
//			goal.setDateStampDay(day);
//			goal.setDateStampMonth(month);
//			goal.setDateStampYear(year);
//			goals.add(goal);
			insertGoals(goals, watch);
		}

	}

	private void insertGoals( List<Goal> goals, Watch watch){
		if (goals.size() > 0) {
			for (Goal goal : goals) {
				goal.setWatch(watch);
				goal.insert();
			}
		}
	}

	private void insertSleepDatabaseWithWatch(Watch watch, List<SleepDatabase> sleepDatabases) {

		for (SleepDatabase sleepDatabase : sleepDatabases) {
			sleepDatabase.setContext(mContext);
			sleepDatabase.setWatch(watch);

			if (sleepDatabase.isExists()) {
				sleepDatabase.update();
			} else {
				sleepDatabase.insert();
			}
		}
	}

	private void insertWorkoutDatabaseWithWatch(Watch watch, List<WorkoutInfo> workoutInfos) {

		for (WorkoutInfo workoutInfo : workoutInfos) {
			workoutInfo.setContext(mContext);
			workoutInfo.setWatch(watch);

			if (workoutInfo.isExists()) {

				workoutInfo.update();
			} else {
				workoutInfo.setSyncedToCloud(false);
				workoutInfo.insert();
			}

			String query = "select _id from WorkoutStopInfo where infoAndStop = ?";

			Cursor cursor = DataSource.getInstance(mContext)
					.getReadOperation()
					.rawQuery(query, String.valueOf(workoutInfo.getId()));

			int workoutStopCount = 0;

			if (cursor.moveToFirst())
				workoutStopCount = cursor.getCount();

			cursor.close();

			List<WorkoutStopInfo> workoutStopInfos = mWorkoutStopInfos.get(workoutInfos.indexOf(workoutInfo));

			if (workoutStopInfos.size() > 0) {
				if (workoutStopCount > 0) {
					workoutStopCount = workoutStopCount - 1;
				}

				for (int i=workoutStopCount;i<workoutStopInfos.size();i++) {
					WorkoutStopInfo workoutStopInfo = workoutStopInfos.get(0);
					workoutStopInfo.setWorkoutInfo(workoutInfo);
					workoutStopInfo.insert();
				}
			}
		}
	}

	private void insertCalibrationDataWithWatch(Watch watch, CalibrationData calibrationData) {
		calibrationData.setWatch(watch);
		calibrationData.setContext(mContext);

		List<CalibrationData> calibrationDataFromDB = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchCalibrationData = ?", String.valueOf(watch.getId()))
				.getResults(CalibrationData.class);

		if (calibrationDataFromDB.size() == 0)
			calibrationData.insert();
	}

	private void insertSleepSettingWithWatch(Watch watch, SleepSetting sleepSetting) {
		sleepSetting.setWatch(watch);
		sleepSetting.setContext(mContext);

		List<SleepSetting> sleepSettingFromDB = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchSleepSetting = ?", String.valueOf(watch.getId()))
				.getResults(SleepSetting.class);

		if (sleepSettingFromDB.size() == 0)
			sleepSetting.insert();
	}

	private void insertTimeDateWithWatch(Watch watch, TimeDate timeDate) {
		timeDate.setWatch(watch);
		timeDate.setContext(mContext);

		List<TimeDate> timeDateFromDB = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchTimeDate = ?", String.valueOf(watch.getId()))
				.getResults(TimeDate.class);

		if (timeDateFromDB.size() == 0)
			timeDate.insert();
	}

	private void insertUserProfileWithWatch(Watch watch, UserProfile userProfile) {
		userProfile.setWatch(watch);
		userProfile.setContext(mContext);

		List<UserProfile> userProfileFromDB = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchUserProfile = ?", String.valueOf(watch.getId()))
				.getResults(UserProfile.class);

		if (userProfileFromDB.size() == 0)
			userProfile.insert();
	}

	private void insertNotificationWithWatch(Watch watch, Notification notification) {
		notification.setWatch(watch);
		notification.setContext(mContext);

		List<Notification> notificationFromDB = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchNotification = ?", String.valueOf(watch.getId()))
				.getResults(Notification.class);

		if (notificationFromDB.size() == 0)
			notification.insert();
	}

	private void insertWakeupSettingWithWatch(Watch watch, WakeupSetting wakeupSetting) {
		wakeupSetting.setWatch(watch);
		wakeupSetting.setContext(mContext);

		List<WakeupSetting> wakeupSettingFromDB = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchWakeupSetting = ?", String.valueOf(watch.getId()))
				.getResults(WakeupSetting.class);

		if (wakeupSettingFromDB.size() == 0)
			wakeupSetting.insert();
	}

	private void insertActivityAlertSettingWithWatch(Watch watch, ActivityAlertSetting activityAlertSetting) {
		activityAlertSetting.setWatch(watch);
		activityAlertSetting.setContext(mContext);

		List<ActivityAlertSetting> activityAlertSettingFromDB = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchActivityAlert = ?", String.valueOf(watch.getId()))
				.getResults(ActivityAlertSetting.class);

		if (activityAlertSettingFromDB.size() == 0)
			activityAlertSetting.insert();
	}

	private void insertDayLightDetectSettingWithWatch(Watch watch, DayLightDetectSetting dayLightDetectSetting) {
		dayLightDetectSetting.setWatch(watch);
		dayLightDetectSetting.setContext(mContext);

		List<DayLightDetectSetting> dayLightDetectSettingFromDB = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchDaylightSetting = ?", String.valueOf(watch.getId()))
				.getResults(DayLightDetectSetting.class);

		if (dayLightDetectSettingFromDB.size() == 0)
			dayLightDetectSetting.insert();
	}

	private void insertNightLightDetectSettingWithWatch(Watch watch, NightLightDetectSetting nightLightDetectSetting) {
		nightLightDetectSetting.setWatch(watch);
		nightLightDetectSetting.setContext(mContext);

		List<NightLightDetectSetting> nightLightDetectSettingFromDB = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchNightlightSetting = ?", String.valueOf(watch.getId()))
				.getResults(NightLightDetectSetting.class);

		if (nightLightDetectSettingFromDB.size() == 0)
			nightLightDetectSetting.insert();
	}

	private static boolean isNotificationEnabled(int status) {
		if (status > 0)
			return true;
		else
			return false;
	}

	/*
	 * Properties
	 */
	public List<StatisticalDataHeader> getStatisticalDataHeaders() {
		return mStatisticalDataHeaders;
	}

	public UserProfile getUserProfile() {
		return mUserProfile;
	}

	public TimeDate getTimeDate() {
		return mTimeDate;
	}

	public void enableANSServer() {
		mSalutronService.enableANSServer();
	}

	public void disableANSServer() {
		mSalutronService.disableANSServer();
	}

	public BluetoothDevice getConnectedDevice() {
		if (mSalutronService == null)
			return null;
		return mSalutronService.getConnectedDevice();
	}

	public SALBLEService getBLEService() {
		return mSalutronService;
	}

	public void getCurrentTimeAndDate() {
		//mIsUpdateTimeAndDate = true;
		//commandTimer.startTimer(SALBLEService.COMMAND_GET_TIME);
		mSalutronService.getCurrentTimeAndDate();
	}
	public void setIsUpdateTimeAndDate(boolean mBoolean) {
		this.mIsUpdateTimeAndDate = mBoolean;

	}




	private static void updateUserProfile(UserProfile userProfile) {
		SALUserProfile salUserProfile = new SALUserProfile();
		salUserProfile.setWeight(userProfile.getWeight());
		salUserProfile.setHeight(userProfile.getHeight());
		salUserProfile.setBirthDay(userProfile.getBirthDay());
		salUserProfile.setBirthMonth(userProfile.getBirthMonth());
		salUserProfile.setBirthYear(userProfile.getBirthYear() - 1900);
		salUserProfile.setSensitivityLevel(userProfile.getSensitivity());
		salUserProfile.setGender(userProfile.getGender());
		salUserProfile.setUnitSystem(userProfile.getUnitSystem());

		mSalutronService.updateUserProfile(salUserProfile);
	}

	private static void updateTimeAndDate(TimeDate timeDate) {
		SALTimeDate salTimeDate = new SALTimeDate();
		salTimeDate.setToNow();
		salTimeDate.setDateFormat(timeDate.getDateFormat());
		salTimeDate.setTimeFormat(timeDate.getHourFormat());
		salTimeDate.setTimeDisplaySize(timeDate.getDisplaySize());

		mSalutronService.updateTimeAndDate(salTimeDate);
	}

	private static String convertiOSToAndroidMacAddress(String macAddress) {
		macAddress = macAddress.replace("0000", "").toUpperCase(Locale.getDefault());
		int start = 0;

		String value = "";

		while (start < macAddress.length() - 1) {
			value = macAddress.substring(start, start + 2) + ((start == 0) ? "" : ":") + value;

			start += 2;
		}

		return value;
	}

}
