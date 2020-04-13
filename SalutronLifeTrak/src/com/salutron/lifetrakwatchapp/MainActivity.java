package com.salutron.lifetrakwatchapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.chainsaw.Main;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.apptentive.android.sdk.Apptentive;
import com.facebook.Session;
import com.flurry.android.FlurryAgent;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
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
import com.salutron.lifetrakwatchapp.fragment.ActigraphyFragment;
import com.salutron.lifetrakwatchapp.fragment.BaseFragment;
import com.salutron.lifetrakwatchapp.fragment.DashboardFragment;
import com.salutron.lifetrakwatchapp.fragment.DashboardItemFragment;
import com.salutron.lifetrakwatchapp.fragment.FitnessResultsFragment;
import com.salutron.lifetrakwatchapp.fragment.FragmentFactory;
import com.salutron.lifetrakwatchapp.fragment.GoalFragment;
import com.salutron.lifetrakwatchapp.fragment.GoalItemFragment;
import com.salutron.lifetrakwatchapp.fragment.HeartRateFragment;
import com.salutron.lifetrakwatchapp.fragment.HeartRateFragmentR420;
import com.salutron.lifetrakwatchapp.fragment.HelpFragment;
import com.salutron.lifetrakwatchapp.fragment.LightPlotPagerFragment;
import com.salutron.lifetrakwatchapp.fragment.MenuFragment;
import com.salutron.lifetrakwatchapp.fragment.MyAccountFragment;
import com.salutron.lifetrakwatchapp.fragment.RewardsFragment;
import com.salutron.lifetrakwatchapp.fragment.SleepDataFragment;
import com.salutron.lifetrakwatchapp.fragment.SleepDataUpdate;
import com.salutron.lifetrakwatchapp.fragment.WatchSettingsFragment;
import com.salutron.lifetrakwatchapp.fragment.WorkoutFragment;
import com.salutron.lifetrakwatchapp.fragment.WorkoutGraphFragment;
import com.salutron.lifetrakwatchapp.fragment.WorkoutGraphFragmentR420;
import com.salutron.lifetrakwatchapp.fragment.dialog.AlertDialogFragment;
import com.salutron.lifetrakwatchapp.model.ActivityAlertSetting;
import com.salutron.lifetrakwatchapp.model.CalibrationData;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.NightLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.Notification;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.SleepDatabaseDeleted;
import com.salutron.lifetrakwatchapp.model.SleepSetting;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.StatisticalDataPoint;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.WakeupSetting;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.service.AlarmNotifReceiver;
import com.salutron.lifetrakwatchapp.service.BluetoothListener;
import com.salutron.lifetrakwatchapp.util.AmazonTransferUtility;
import com.salutron.lifetrakwatchapp.service.GoogleFitSyncService;
import com.salutron.lifetrakwatchapp.util.CalendarDateChangeListener;
import com.salutron.lifetrakwatchapp.util.DeviceScanListener;
import com.salutron.lifetrakwatchapp.util.DialogActivity;
import com.salutron.lifetrakwatchapp.util.DialogActivityIssueC300;
import com.salutron.lifetrakwatchapp.util.DialogActivitySyncSuccess;
import com.salutron.lifetrakwatchapp.util.GoogleApiClientManager;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.LifeTrakSyncR420;
import com.salutron.lifetrakwatchapp.util.LifeTrakSyncR450;
import com.salutron.lifetrakwatchapp.util.LifeTrakUpdateR420;
import com.salutron.lifetrakwatchapp.util.LifeTrakUpdateR450;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;
import com.salutron.lifetrakwatchapp.util.PreferenceWrapper;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;
import com.salutron.lifetrakwatchapp.util.SalutronSDKCallback420;
import com.salutron.lifetrakwatchapp.util.SalutronSDKCallback450;
import com.salutron.lifetrakwatchapp.view.CalendarControlView;
import com.salutron.lifetrakwatchapp.web.AsyncListener;
import com.salutron.lifetrakwatchapp.web.EditProfileAsync;
import com.salutron.lifetrakwatchapp.web.LoginAsyncTask;
import com.salutron.lifetrakwatchapp.web.ServerRestoreAsync;
import com.salutron.lifetrakwatchapp.web.ServerSyncAsync;
import com.salutron.lifetrakwatchapp.view.ConnectionFailedView;
import com.salutron.lifetrakwatchapp.web.ServerSyncAsyncS3Amazon;
import com.salutron.lifetrakwatchapp.web.ServerSyncAsyncTask;
import com.salutron.lifetrakwatchapp.web.TwoWaySyncAsyncTask;
import com.salutron.lifetrakwatchapp.web.TwoWaySyncChecker;

@SuppressWarnings("deprecation")
public class MainActivity extends SlidingFragmentActivity implements SalutronLifeTrakUtility, OnItemClickListener, AsyncListener, SalutronSDKCallback450, SalutronSDKCallback420,
		GoogleApiClientManager.Provider {
	private final String FRAGMENT_TAG = "fragment_tag1";

	private SherlockFragment mMenuFragment;
	private SherlockFragment mContentFragment;
	private TextView mCalendarDay;
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private final SimpleDateFormat mDateFormatMonth = (SimpleDateFormat) DateFormat.getInstance();
	private final SimpleDateFormat mDateFormatWeek = (SimpleDateFormat) DateFormat.getInstance();
	private SlidingDrawer mCalendarContainer;
	private ViewFlipper mContentSwitcher;
	//private CalendarPicker mCalendar;
	private CalendarControlView mCalendar;
	private NumberPicker mYearList;
	private ListView mYearListView;
	private ArrayAdapter<CharSequence> mYearListAdapter;
	private static LifeTrakApplication mLifeTrakApplication;
	private SALBLEService mSalutronService;
	public ProgressDialog mProgressDialog;
	private AlertDialog mAlertDialog;

	private boolean showCheckNotification = false;

	private EditProfileAsync<JSONObject> mEditProfileAsync;

	private Bitmap mBitmap;
	private String mPath;

	private boolean flag_sync = false;
	private boolean flag_sync_no_bluetooth = false;

	private boolean flag_finished_syncing = false;

	private boolean flag_finished_syncing_r450 = false;

	private boolean flag_sync_cloud_error = false;

	private boolean deviceFound;

	private static final int OPERATION_SYNC_WATCH = 0x01;
	private static final int OPERATION_SYNC_SETTINGS = 0x02;
	private static final int OPERATION_SEARCH_BLUETOOTH = 0x03;
	private static int mOperation = OPERATION_SYNC_WATCH;

	private final Handler mTimeoutHandler = new Handler();
	private final Handler mErrorHandler = new Handler();
	private List<BluetoothDevice> mBluetoothDevices = new ArrayList<BluetoothDevice>();

	private InputMethodManager mInputMethodManager;
	public boolean mSyncSuccess;

	private AlertDialog mGoalsAlert;

	private PreferenceWrapper mPreferenceWrapper;
	private ServerSyncAsync mServerSyncAsync;
	private TwoWaySyncAsyncTask mServerSyncAsyncTask;
	private ServerRestoreAsync<JSONObject> mServerRestoreAsync;
	private ServerSyncAsyncS3Amazon mServerSyncAsyncAmazon;

	private final int API_REQUEST_SEND = 0x01;
	private final int API_REQUEST_STORE = 0x02;
	private final int OPERATION_REFRESH_TOKEN = 0x03;
	private final int OPERATION_SYNC_TO_CLOUD = 0x04;
	private final int OPERATION_CHECK_SERVERTIME = 0x05;
	private final int OPERATION_GET_USERID = 0x06;
	private final int OPERATION_RESTORE_FROM_CLOUD = 0x07;
	private final int OPERATION_EDIT_PROFILE = 0x08;
	private final int OPERATION_BULK_INSERT_S3 = 0x09;
	private int mCurrentApiRequest = API_REQUEST_SEND;
	private int mCurrentOperation = OPERATION_SYNC_TO_CLOUD;



	private List<CharSequence> mYearItems = new ArrayList<CharSequence>();

	private static LifeTrakSyncR450 mLifeTrakSyncR450;
	private static LifeTrakSyncR420 mLifeTrakSyncR420;

	private static AlarmManager mAlarmManager;
	private static PendingIntent mPendingIntent;
	private static boolean visible;
	private int counter =0;
	private boolean mCancelled = false;
	private Thread mSyncThread;

	private static boolean mWatchConnected = false;

	public AlarmManager alarmManager;
	private Intent alarmIntent;
	private PendingIntent pendingIntent;
	static final String NOTIFICATION_COUNT = "notificationCount";
	private boolean mFromOnError = false;
	private ConnectionFailedView mConnectionFailedView;

	private boolean flag_disable_menu = false;

	private GoogleApiClientManager googleApiClientManager;

	private int retryCounter = 0;
	private int indexListCounter = 0;
	private String uuid;
	private List<StatisticalDataHeader> dataHeaders;

	private SALUserProfile salUserProfile;

	@Override
	public GoogleApiClientManager getGoogleApiClientManager() {
		return googleApiClientManager;
	}

	/*
	 * Service Connection for registering handler
	 */
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder localBinder = (LocalBinder) service;
			mSalutronService = localBinder.getService();

			if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
				mTimeoutHandler.postDelayed(new Runnable() {
					public void run() {
						mLifeTrakSyncR450.setServiceInstance(mSalutronService);
						mLifeTrakSyncR450.registerHandler();
					}
				}, 1000);
			}else if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420){
				mTimeoutHandler.postDelayed(new Runnable() {
					public void run() {
						mLifeTrakSyncR420.setServiceInstance(mSalutronService);
						mLifeTrakSyncR420.registerHandler();
					}
				}, 1000);
			}
			else {
				mSalutronService.registerDevListHandler(mHandler);
				mSalutronService.registerDevDataHandler(mHandler);
			}

			mHandler.postDelayed(new Runnable() {
				public void run() {
					if (mPreferenceWrapper.getPreferenceBooleanValue(NOTIFICATION_ENABLED)) {
						mSalutronService.enableANSServer();
					} else {
						mSalutronService.disableANSServer();
					}
				}
			}, 1000);

			registerTelephonyService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mSalutronService = null;
			mLifeTrakSyncR450.setServiceInstance(null);
			mLifeTrakSyncR420.setServiceInstance(null);
		}
	};

	public static boolean isVisible() {
		return visible;
	}

	private void registerTelephonyService() {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(mSalutronService.phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	public void onBackPressed() {
		try {
			if (mContentFragment instanceof WatchSettingsFragment) {
				final WatchSettingsFragment watchSettings = (WatchSettingsFragment) mContentFragment;

				if (!watchSettings.canBackPressed()) {
					watchSettings.handleBackPressed();
					return;
				}
			}

			if (mContentFragment instanceof DashboardFragment) {
				final DashboardFragment dashboard = (DashboardFragment) mContentFragment;

				dashboard.handleBackPressed();
			}

			if (mContentFragment instanceof MyAccountFragment) {
				final MyAccountFragment account = (MyAccountFragment) mContentFragment;

				account.handleBackPressed();
			}
			super.onBackPressed();
		} catch (IllegalStateException e) {}
	}

	/*
	 * Handler message for Salutron Service
	 */
	private final Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message message) {
			final Bundle data = message.getData();
			final Watch watch = mLifeTrakApplication.getSelectedWatch();
			final BluetoothDevice device = data.getParcelable(BluetoothDevice.EXTRA_DEVICE);

			switch (message.what) {
				case SALBLEService.GATT_DEVICE_FOUND_MSG:

					deviceFound = false;

					for (BluetoothDevice bluetoothDevice : mBluetoothDevices) {
						if (bluetoothDevice.getAddress().equals(device.getAddress()) && !device.getName().isEmpty() && !device.getAddress().isEmpty()) {
							deviceFound = true;
							break;
						}
					}

					if (!deviceFound) {
						mBluetoothDevices.add(device);
						mSalutronService.stopScan();

						mHandler.postDelayed(new Runnable() {
							public void run() {
								// Toast.makeText(MainActivity.this,
								// "device found! with address: " +
								// device.getAddress(), Toast.LENGTH_LONG).show();
								onDeviceFound(device, data);
							}
						}, HANDLER_DELAY);
					}

					break;
				case SALBLEService.GATT_DEVICE_CONNECT_MSG:
					FlurryAgent.logEvent(DEVICE_CONNECTED, true);
					FlurryAgent.endTimedEvent(DEVICE_INITIALIZE_CONNECT);
					LifeTrakLogger.info("device connected on MainActivity");
					break;
				case SALBLEService.GATT_DEVICE_READY_MSG:
					FlurryAgent.logEvent(DEVICE_READY, true);
					FlurryAgent.endTimedEvent(DEVICE_CONNECTED);
					if (mOperation == OPERATION_SYNC_WATCH) {
					} else if (mOperation == OPERATION_SYNC_SETTINGS) {
						mTimeoutHandler.postDelayed(new Runnable() {
							public void run() {
								mSyncSuccess = false;
								mSalutronService.registerDevDataHandler(mHandler);
								mSalutronService.registerDevListHandler(mHandler);

								final DeviceScanListener l = (DeviceScanListener) mContentFragment;

								try {
									mSyncSuccess = false;
									mProgressDialog.setMessage(getString(R.string.syncing_data_2));

									l.onDeviceConnected(device, mSalutronService, watch);

								} catch (Exception e) {
									LifeTrakLogger.error(e.getMessage());
								}
							}
						}, 500);
					}
					break;
				case SALBLEService.SAL_MSG_DEVICE_DATA_RECEIVED:
					//flag_finished_syncing = true;
					final int dataType = data.getInt(SALBLEService.SAL_DEVICE_DATA_TYPE);

					switch (dataType) {
						case SALBLEService.COMMAND_GET_TIME:
							FlurryAgent.logEvent(GET_TIME, true);
							FlurryAgent.endTimedEvent(GET_USER_PROFILE);
							SALTimeDate timeDate = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
							onGetTimeAndDate(timeDate);
							break;
						case SALBLEService.COMMAND_GET_STAT_DATA_HEADERS:
							flag_finished_syncing = true;
							FlurryAgent.logEvent(GET_DATA_HEADER, true);
							FlurryAgent.endTimedEvent(DEVICE_START_SYNC);
							int status = data.getInt(SALBLEService.SAL_DEVICE_DATA_STATUS);

							switch (status) {
								case SALStatus.NO_ERROR:
									List<SALStatisticalDataHeader> statisticalDataHeaders = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
									onGetStatisticalDataHeaders(statisticalDataHeaders);
									break;
								case SALStatus.ERROR_CHECKSUM:
									break;
							}

							break;
						case SALBLEService.COMMAND_GET_DATA_POINTS_FOR_DATE:
							FlurryAgent.logEvent(GET_DATA_POINTS, true);
							FlurryAgent.endTimedEvent(GET_DATA_HEADER);
							List<SALStatisticalDataPoint> statisticalDataPoints = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
							onGetStatisticalDataPoint(statisticalDataPoints);
							break;
						case SALBLEService.COMMAND_GET_GOAL_STEPS:
							FlurryAgent.logEvent(GET_STEP_GOAL, true);
							FlurryAgent.endTimedEvent(GET_SLEEP_SETTINGS);
							long stepGoal = data.getLong(SALBLEService.SAL_DEVICE_DATA);
							onGetStepGoal(stepGoal);
							break;
						case SALBLEService.COMMAND_GET_GOAL_DISTANCE:
							FlurryAgent.logEvent(GET_DISTANCE_GOAL, true);
							FlurryAgent.endTimedEvent(GET_STEP_GOAL);
							long distanceGoal = data.getLong(SALBLEService.SAL_DEVICE_DATA);
							onGetDistanceGoal(distanceGoal);
							break;
						case SALBLEService.COMMAND_GET_GOAL_CALORIE:
							FlurryAgent.logEvent(GET_CALORIE_GOAL, true);
							FlurryAgent.endTimedEvent(GET_DISTANCE_GOAL);
							long calorieGoal = data.getLong(SALBLEService.SAL_DEVICE_DATA);
							onGetCalorieGoal(calorieGoal);
							break;
						case SALBLEService.COMMAND_GET_SLEEP_SETTING:
							FlurryAgent.logEvent(GET_SLEEP_SETTINGS, true);
							FlurryAgent.endTimedEvent(GET_SLEEP_DATABASE);
							SALSleepSetting sleepSetting = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
							onGetSleepSetting(sleepSetting);
							break;
						case SALBLEService.COMMAND_GET_CALIBRATION_DATA:
							FlurryAgent.logEvent(GET_CALIBRATION_DATA, true);
							FlurryAgent.endTimedEvent(GET_CALORIE_GOAL);
							SALCalibration calibrationData = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
							onGetCalibrationData(calibrationData);
							break;
						case SALBLEService.COMMAND_GET_WORKOUT_DB:
							FlurryAgent.logEvent(GET_WORKOUT, true);
							FlurryAgent.endTimedEvent(GET_DATA_POINTS);
							List<SALWorkoutInfo> workoutInfos = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
							onGetWorkoutDatabase(workoutInfos);
							break;
						case SALBLEService.COMMAND_GET_SLEEP_DB:
							FlurryAgent.logEvent(GET_SLEEP_DATABASE, true);
							FlurryAgent.endTimedEvent(GET_WORKOUT);
							List<SALSleepDatabase> sleepDatabases = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
							onGetSleepDatabase(sleepDatabases);
							break;
						case SALBLEService.COMMAND_GET_USER_PROFILE:
							FlurryAgent.logEvent(GET_USER_PROFILE, true);
							FlurryAgent.endTimedEvent(GET_CALIBRATION_DATA);
							SALUserProfile userProfile = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
							onGetUserProfile(userProfile);
							break;
					}

					break;
				case SALBLEService.GATT_DEVICE_DISCONNECT_MSG:
					if (mProgressDialog.isShowing())
						mProgressDialog.dismiss();
					if (!flag_finished_syncing){
						flag_finished_syncing = false ;
						LifeTrakLogger.info("flag_finished_syncing = false");
						startSyncCheck();
					}
					else{
						flag_finished_syncing = false;
					}
					mSyncSuccess = false;
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
			}


		}
	};

	private void startSyncCheck() {
		LifeTrakLogger.info("Sync Started from Watch - " + new Date());

		mTimeoutHandler.postDelayed(new Runnable() {
			public void run() {
				if (mProgressDialog == null)
					reinitializeProgress();

				mSalutronService.registerDevDataHandler(mHandler);
				mSalutronService.registerDevListHandler(mHandler);
				int status = mSalutronService.getCurrentTimeAndDate();

				switch (status) {
					case SALStatus.NO_ERROR:

						break;
					case SALStatus.ERROR_NOT_SUPPORTED:

						if (mProgressDialog == null)
							reinitializeProgress();
						mProgressDialog.dismiss();
						syncingWatchFailed();


						break;
					case SALStatus.ERROR_NOT_INITIALIZED:
						if (mProgressDialog == null)
							reinitializeProgress();
						mProgressDialog.dismiss();

						syncingWatchFailed();
						break;
					default:
						LifeTrakLogger.error("status unknown");
						break;
				}
			}
		}, 500);
	}

	/**
	 * Method for binding the Salutron BLE Service
	 */
	protected void bindBLEService() {
		Intent intent = new Intent(this, SALBLEService.class);
		bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
	}



	private void startSync() {
		LifeTrakLogger.info("Sync Started from Watch - " + new Date());

		mTimeoutHandler.postDelayed(new Runnable() {
			public void run() {
				if (isFinishing()) {
					if (mProgressDialog == null)
						reinitializeProgress();
					mProgressDialog.setMessage(getString(R.string.syncing_data_2));

					if (!mProgressDialog.isShowing())
						mProgressDialog.show();
				}
				mSalutronService.registerDevDataHandler(mHandler);
				mSalutronService.registerDevListHandler(mHandler);
				int status = mSalutronService.getCurrentTimeAndDate();

				switch (status) {
					case SALStatus.NO_ERROR:
						LifeTrakLogger.info("getCurrentTimeAndDate status no error");
						mProgressDialog.setMessage(getString(R.string.syncing_data_2));
						break;
					case SALStatus.ERROR_NOT_SUPPORTED:
						LifeTrakLogger.error("getCurrentTimeAndDate status not supported");
						if (mProgressDialog == null)
							reinitializeProgress();
						mProgressDialog.dismiss();

						syncingWatchFailed();


						break;
					case SALStatus.ERROR_NOT_INITIALIZED:
						LifeTrakLogger.error("getCurrentTimeAndDate status error not initialized");
						if (mProgressDialog == null)
							reinitializeProgress();
						mProgressDialog.dismiss();
						//flag_finished_syncing = false;
						syncingWatchFailed();

						break;
					default:
						LifeTrakLogger.error("status unknown");
						break;
				}
			}
		}, 500);
	}

	public void syncingWatchFailed(){
		counter = counter + 1;
		if (counter == 2){
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R415 && mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R420 ) {
				if (mSalutronService != null)
					mSalutronService.disconnectFromDevice();
				//lollipop C300/C410
				//lollipop C300/C410
				if (!mPreferenceWrapper.getPreferenceBooleanValue(IS_REMEMBER_ME_ISSUE)) {

					if (mConnectionFailedView.isShowing()){
						mConnectionFailedView.hide();
						flag_disable_menu = false;
					}

					LayoutInflater factory = LayoutInflater.from(MainActivity.this);
					final View issueDialogView = factory.inflate(R.layout.alert_dialog_c300_c410_issue, null);
					final AlertDialog issueDialog = new AlertDialog.Builder(MainActivity.this).create();
					issueDialog.setView(issueDialogView);
					issueDialogView.findViewById(R.id.issue_dialog_yes).setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							counter = 1;
							flag_finished_syncing = false;
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(API_LOLLIPOP_ISSUE_URL));
							startActivity(intent);
							issueDialog.dismiss();
						}
					});
					issueDialogView.findViewById(R.id.issue_dialog_no).setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							counter = 1;
							flag_finished_syncing = false;
							issueDialog.dismiss();
						}
					});
					CheckBox cb = (CheckBox) issueDialogView.findViewById(R.id.issue_checkbox_remember_choice);
					cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							mPreferenceWrapper.setPreferenceBooleanValue(IS_REMEMBER_ME_ISSUE, isChecked).synchronize();
						}
					});
					if (isFinishing()) {
						issueDialog.show();
					}
					else{
						counter = 1;
						Intent intent = new Intent(getApplicationContext(), DialogActivityIssueC300.class);
						startActivity(intent);
					}
				}
				else{
					LifeTrakLogger.info("Alert is Remember me true");
					counter = 1;
					if (!mConnectionFailedView.isShown()) {
						mConnectionFailedView.show();
						flag_disable_menu = true;
					}
				}
			}
			else{
				if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420)
					mLifeTrakSyncR420.getBLEService().disconnectFromDevice();

				counter = 1;
				if (!mConnectionFailedView.isShown()) {
					mConnectionFailedView.show();
					flag_disable_menu = true;
				}
			}
		}else {
			if (!mConnectionFailedView.isShown()) {
				mConnectionFailedView.show();
				flag_disable_menu = true;
			}
		}
	}

	@Override
	public void onDeviceFound(final BluetoothDevice device, Bundle data) {
		if (!(mContentFragment instanceof DeviceScanListener) && mOperation == OPERATION_SYNC_SETTINGS) {
			return;
		}

		Watch watch = mLifeTrakApplication.getSelectedWatch();
//		if (watch == null){
//
//		}

		String watchMacAddress = watch.getMacAddress();

		if (!watchMacAddress.contains(":"))
			watchMacAddress = convertiOSToAndroidMacAddress(watchMacAddress);
		if (device != null && watch != null && device.getAddress().equals(watchMacAddress)) {
			deviceFound = true;

			int status = SALStatus.ERROR_NOT_CONNECTED;

			if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {

				mPreferenceWrapper.setPreferenceStringValue(LAST_SYNCED_R450_WATCH_MAC_ADDRESS, watchMacAddress).synchronize();

				if (mLifeTrakSyncR450 != null)
					status = mLifeTrakSyncR450.connectToDevice(device.getAddress(), WATCHMODEL_R415);
			}
			else if(mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
				status = mLifeTrakSyncR420.connectToDevice(device.getAddress(), WATCHMODEL_R420);
			}
			else {
				FlurryAgent.logEvent(DEVICE_INITIALIZE_CONNECT, true);
				FlurryAgent.endTimedEvent(DEVICE_SEARCH);
				status = mSalutronService.connectToDevice(device.getAddress(), mLifeTrakApplication.getSelectedWatch().getModel());
			}

			if (status == SALStatus.NO_ERROR)
				if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
					if (mLifeTrakSyncR450 != null)
						mLifeTrakSyncR450.stopScan();
				}
				else if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
					if (mLifeTrakSyncR420 != null)
						mLifeTrakSyncR420.stopScan();
				}
				else {
					mSalutronService.stopScan();
					//startSync2();
				}
		}
	}

	private void startSync2() {
		LifeTrakLogger.info("Sync Started from Watch - " + new Date());

		mTimeoutHandler.postDelayed(new Runnable() {
			public void run() {
				if (mProgressDialog == null)
					reinitializeProgress();
				//	mProgressDialog.setMessage(getString(R.string.syncing_data_2));

				if (!mProgressDialog.isShowing())
					mProgressDialog.show();

				mSalutronService.registerDevDataHandler(mHandler);
				mSalutronService.registerDevListHandler(mHandler);
				int status = mSalutronService.getCurrentTimeAndDate();

				switch (status) {
					case SALStatus.NO_ERROR:
						break;
					case SALStatus.ERROR_NOT_SUPPORTED:
						LifeTrakLogger.error("getCurrentTimeAndDate status not supported");
						mProgressDialog.dismiss();
						syncingWatchFailed();


						break;
					case SALStatus.ERROR_NOT_INITIALIZED:
						LifeTrakLogger.error("getCurrentTimeAndDate status error not initialized");
						mProgressDialog.dismiss();
						syncingWatchFailed();
						break;
					default:
						LifeTrakLogger.error("status unknown");
						break;
				}
			}
		}, 500);
	}


	public void disconnectFromDevice() {
		try {
			mSalutronService.disconnectFromDevice();
			deviceFound = false;
		} catch (Exception e) {
		}

		startActivity(new Intent(this, WelcomePageActivity.class));
		finish();
	}

	/**
	 * Method for unbinding the Salutron BLE Service
	 */
	protected void unbindBLEService() {
		unbindService(mServiceConnection);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!showCheckNotification)
			checkNotificationListener();

		visible = true;
		mLifeTrakSyncR450.setSalutronSDKCallback(this);
		mLifeTrakSyncR420.setSDKCallback(this);

		if (mLifeTrakApplication != null){
			if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415 && mAlarmManager != null) {
				if (mPendingIntent != null) {
					mAlarmManager.cancel(mPendingIntent);
				} else {
//					Intent intent = new Intent(this, BluetoothSearchReceiver.class);
//					mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
				}

				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 1000, mPendingIntent);
			}
		}
		/*if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
			mLifeTrakSyncR450.bindService();
		} else {
			bindBLEService();
		}*/

		bindBLEService();

		registerReceiver(bluetoothListener, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

		mTimeoutHandler.postDelayed(new Runnable() {
			public void run() {
				if (mLifeTrakApplication.getSelectedWatch() != null) {
					if (mLifeTrakSyncR450 != null && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
						if (mLifeTrakSyncR450.getBLEService() != null) {
							if (mPreferenceWrapper.getPreferenceBooleanValue(NOTIFICATION_ENABLED)) {
								mLifeTrakSyncR450.enableANSServer();
							} else {
								mLifeTrakSyncR450.disableANSServer();
							}
						}
					}
				}
			}
		}, 2500);
	}

	@Override
	public void onPause() {
		super.onPause();
		visible = false;
		try{
			if (mPendingIntent != null) {
				mAlarmManager.cancel(mPendingIntent);
			}
		}catch (Exception e){}
	}

	@Override
	public void onStart() {
		super.onStart();
		try{Apptentive.onStart(this);}catch (Exception e) {}
		FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
		if (mPreferenceWrapper.getPreferenceBooleanValue(GOOGLE_FIT_ENABLED)) {
			googleApiClientManager.getClient().connect();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		// unregisterReceiver(finisher);
		if (bluetoothListener != null) {
			unregisterReceiver(bluetoothListener);
			bluetoothListener = null;
		}
		Apptentive.onStop(this);
		FlurryAgent.onEndSession(this);
		googleApiClientManager.getClient().disconnect();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		/*if (mLifeTrakApplication.getSelectedWatch() != null &&
                mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
			mLifeTrakSyncR450.unbindService();
		} else {
			unbindBLEService();
		}*/

//		String macAddress = null;
//		if (mLifeTrakApplication.getSelectedWatch() != null)
//			macAddress = mLifeTrakApplication.getSelectedWatch().getMacAddress();
//		else
//			macAddress = mPreferenceWrapper.getPreferenceStringValue(LAST_R450_SYNC);
//		if (macAddress != null && !macAddress.contains(":"))
//			macAddress = convertiOSToAndroidMacAddress(macAddress);
//		LifeTrakLogger.info("Reconnect to LifeTrakSyncR450 with mac address = " + macAddress);
//		if (macAddress != null)
//			mSalutronService.connectToDevice(macAddress, SALBLEService.MODEL_R415);

		if (mLifeTrakApplication.getSelectedWatch() != null &&
				mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
			mLifeTrakSyncR450.registerHandler();
		}
		if (mLifeTrakApplication.getSelectedWatch() != null &&
				mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
			mLifeTrakSyncR420.registerHandler();
		}


		unbindBLEService();



		if (mPendingIntent != null)
			mAlarmManager.cancel(mPendingIntent);

		/*mPreferenceWrapper.setPreferenceLongValue(LAST_CONNECTED_WATCH_ID, mLifeTrakApplication.getSelectedWatch().getId())
                            .synchronize();*/


		mLifeTrakApplication.setSelectedWatch(null);
		//unbindBLEService();
		DataSource.getInstance(this).closeDB();


	}

	public void watchRegisterHandler(){
		if (mLifeTrakApplication.getSelectedWatch() != null &&
				mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
			mLifeTrakSyncR450.registerHandler();
		}
		if (mLifeTrakApplication.getSelectedWatch() != null &&
				mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
			mLifeTrakSyncR420.registerHandler();
		}
	}

	public void onSyncGoalMenuItemClick() {
		if (mContentFragment instanceof WatchSettingsFragment){
			LifeTrakLogger.info("Set Cancel flag to false on WatchSettingsFragment");
			WatchSettingsFragment mWatchSettingsFragment = (WatchSettingsFragment) mContentFragment;
			mWatchSettingsFragment.setCancelledSyncing(false);
		}

		if (mContentFragment instanceof GoalFragment){
			LifeTrakLogger.info("Set Cancel flag to false on GoalFragment");
			GoalFragment mGoalFragment = (GoalFragment) mContentFragment;
			mGoalFragment.setCancelledSyncing(false);
		}

		if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
			if (mLifeTrakSyncR450 != null){


				if (mLifeTrakSyncR450.getConnectedDevice() != null) {
					registerReceiver(bluetoothListener, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

					final DeviceScanListener l = (DeviceScanListener) mContentFragment;

					try {
						mSyncSuccess = false;
						if (mProgressDialog == null)
							reinitializeProgress();
						mProgressDialog.setMessage(getString(R.string.syncing_data_2));
						mProgressDialog.show();
						mHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								LifeTrakLogger.info("Salutron watch is connected");
								//flag_finished_syncing = true;
								l.onDeviceConnected(mLifeTrakSyncR450.getConnectedDevice(), mLifeTrakSyncR450.getBLEService(),
										mLifeTrakApplication.getSelectedWatch());
							}
						}, 3000);
					} catch (NullPointerException e) {
						LifeTrakLogger.error(e.getMessage());

						Watch watch = mLifeTrakApplication.getSelectedWatch();

						if (watch.getModel() == WATCHMODEL_R415) {
							if (mAlarmManager != null && mPendingIntent != null)
								mAlarmManager.cancel(mPendingIntent);

							mOperation = OPERATION_SYNC_SETTINGS;
							mLifeTrakSyncR450.connectToDevice(watch.getMacAddress(), WATCHMODEL_R415);

							if (mAlarmManager != null && mPendingIntent != null)
								mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (10 * 1000), 10 * 1000, mPendingIntent);
						}
					}
				}
				else{
					deviceFound = false;
					Intent intent = new Intent(this, PairDeviceAutoActivity.class);
					intent.putExtra(SYNC_TYPE, SYNC_TYPE_DASHBOARD);
					intent.putExtra(SELECTED_WATCH_MODEL, mLifeTrakApplication.getSelectedWatch().getModel());
					startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE_GOAL_SYNC);
				}
			} else {
				deviceFound = false;

				final int watchModel = mLifeTrakApplication.getSelectedWatch().getModel();
				Intent intent = new Intent(this, PairDeviceActivity.class);
				intent.putExtra(SELECTED_WATCH_MODEL, watchModel);
				startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE);
			}
		} else {
//			if (mLifeTrakSyncR450 != null)
//				mLifeTrakSyncR450.disconnectR450();

			deviceFound = false;


			mOperation = OPERATION_SYNC_SETTINGS;

			final int watchModel = mLifeTrakApplication.getSelectedWatch().getModel();
			Intent intent = new Intent(this, PairDeviceActivity.class);
			intent.putExtra(SELECTED_WATCH_MODEL, watchModel);
			startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		googleApiClientManager.onActivityResult(requestCode, resultCode, data);
		try{
			if (requestCode == REQUEST_CODE_PAIR_DEVICE && resultCode == RESULT_OK) {
				mBluetoothDevices.clear();

				if (mProgressDialog == null)
					reinitializeProgress();

				mProgressDialog.setMessage(getString(R.string.searching_device, stringNameForModel(mLifeTrakApplication.getSelectedWatch().getModel())));

				mProgressDialog.show();

				mHandler.postDelayed(new Runnable() {
					public void run() {
						if (mAlarmManager != null && mPendingIntent != null)
							mAlarmManager.cancel(mPendingIntent);

						if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
							BluetoothDevice device = mLifeTrakSyncR450.getConnectedDevice();

							mOperation = OPERATION_SYNC_SETTINGS;

							//if (!mLifeTrakSyncR450.isDisconnected() && data != null) {

							if (mLifeTrakSyncR450.getConnectedDevice() != null && data != null) {
								onDeviceFound(device, data.getExtras());
							} else {
								mLifeTrakSyncR450.startScan();
							}

						}
						else if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
							mLifeTrakSyncR420.setSDKCallback(MainActivity.this);
							mLifeTrakSyncR420.registerHandler();
							mLifeTrakSyncR420.startScan();
						}

						else {
							FlurryAgent.logEvent(DEVICE_SEARCH, true);

							mSalutronService.registerDevListHandler(mHandler);
							mSalutronService.registerDevDataHandler(mHandler);
							mSalutronService.startScan();
						}

						if (mAlarmManager != null && mPendingIntent != null)
							mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (10 * 1000), 10 * 1000, mPendingIntent);
					}
				}, 750);

				/*mErrorHandler.postDelayed(new Runnable() {
					public void run() {
						if (mLifeTrakApplication.getSelectedWatch() != null && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
							mLifeTrakSyncR450.stopScan();
						} else {
							mSalutronService.stopScan();
						}

						if (!deviceFound) {
							mProgressDialog.dismiss();
							//onPairTimeout();
							syncingWatchFailed();
						}

						deviceFound = false;
					}
				}, 10000);*/
				mErrorHandler.postDelayed(mErrorRunnable, 10000);
			}
			else if (requestCode == REQUEST_CODE_PAIR_DEVICE_GOAL_SYNC && resultCode == RESULT_OK){
				deviceFound = data.getBooleanExtra(DEVICE_FOUND, false);

				if (deviceFound) {
					final DeviceScanListener l = (DeviceScanListener) mContentFragment;
					try {
						mSyncSuccess = false;
						if (mProgressDialog == null)
							reinitializeProgress();
						mProgressDialog.setMessage(getString(R.string.syncing_data_2));
						mProgressDialog.show();
						//flag_finished_syncing = true;
						if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
							l.onDeviceConnected(mLifeTrakSyncR450.getConnectedDevice(), mLifeTrakSyncR450.getBLEService(),
									mLifeTrakApplication.getSelectedWatch());
						}
					} catch (NullPointerException e) {
						LifeTrakLogger.error(e.getMessage());

						Watch watch = mLifeTrakApplication.getSelectedWatch();

						if (watch.getModel() == WATCHMODEL_R415) {
							if (mAlarmManager != null && mPendingIntent != null)
								mAlarmManager.cancel(mPendingIntent);

							mOperation = OPERATION_SYNC_SETTINGS;
							mLifeTrakSyncR450.connectToDevice(watch.getMacAddress(), WATCHMODEL_R415);

							if (mAlarmManager != null && mPendingIntent != null)
								mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (10 * 1000), 10 * 1000, mPendingIntent);
						}
					}
				} else {
					mTimeoutHandler.postDelayed(new Runnable() {
						public void run() {
							if (mProgressDialog == null)
								reinitializeProgress();

							if (mProgressDialog.isShowing())
								mProgressDialog.dismiss();

							if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
								mLifeTrakSyncR450.stopScan();
							}
							else if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420){
								mLifeTrakSyncR420.stopScan();
							}else {
								mSalutronService.stopScan();
							}
							//onPairTimeout();
							syncingWatchFailed();
						}
					}, 1000);
				}
			}
			else if (requestCode == REQUEST_CODE_PAIR_DEVICE_SYNC && resultCode == RESULT_OK) {
				mBluetoothDevices.clear();

				// Add searching for watch dialog
				if (mProgressDialog == null)
					reinitializeProgress();

				if (mProgressDialog.isShowing())
					mProgressDialog.dismiss();

				mProgressDialog.setMessage(getString(R.string.searching_device, stringNameForModel(mLifeTrakApplication.getSelectedWatch().getModel())));
				mProgressDialog.show();

				deviceFound = data.getBooleanExtra(DEVICE_FOUND, false);

				if (deviceFound) {
					flag_sync_no_bluetooth = true;
					//flag_finished_syncing = true;
					mTimeoutHandler.postDelayed(watchSyncRunnable2, 6000);
					mTimeoutHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							if (!mLifeTrakSyncR450.salutronStatusActive()){
								if (mProgressDialog == null)
									reinitializeProgress();
								if (mProgressDialog.isShowing())
									mProgressDialog.dismiss();

								if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
									mLifeTrakSyncR450.stopScan();
								}
								else if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
									mLifeTrakSyncR420.stopScan();
								}
								else {
									mSalutronService.stopScan();
								}
								//onPairTimeout();
								syncingWatchFailed();
							}

						}
					}, 10000);

				} else {
					mTimeoutHandler.postDelayed(new Runnable() {
						public void run() {
							mProgressDialog.dismiss();

							if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
								mLifeTrakSyncR450.stopScan();
							}
							else if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420){
								mLifeTrakSyncR420.stopScan();
							}else {
								mSalutronService.stopScan();
							}
							//onPairTimeout();
							syncingWatchFailed();
						}
					}, 1000);
				}
			} else if (requestCode == REQ_BT_ENABLE) {
				mBluetoothDevices.clear();
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			} else if (requestCode == REQUEST_CODE_PROFILE_SELECT && resultCode == RESULT_OK) {
				if (data != null) {
					final UserProfile userProfile = data.getExtras().getParcelable(USER_PROFILE);
					final TimeDate timeDate = data.getExtras().getParcelable(TIME_DATE);
					final CalibrationData calibrationData = data.getExtras().getParcelable(CALIBRATION_DATA_FROM_WATCH);

					if (calibrationData != null) {
						calibrationData.setWatch(mLifeTrakApplication.getSelectedWatch());
						mCalibrationData = calibrationData;
					}

					mGoal = data.getExtras().getParcelable(GOAL_FROM_WATCH);

					if (mGoal != null) {
						if (mGoal.getId() == 0) {
							mGoal.insert();
						} else {
							mGoal.update();
						}
					}

					Handler handler = new Handler();

					handler.postDelayed(new Runnable() {
						public void run() {
							SALUserProfile salUserProfile = new SALUserProfile();
							salUserProfile.setWeight(userProfile.getWeight());
							salUserProfile.setHeight(userProfile.getHeight());
							salUserProfile.setBirthDay(userProfile.getBirthDay());
							salUserProfile.setBirthMonth(userProfile.getBirthMonth());
							salUserProfile.setBirthYear(userProfile.getBirthYear() - 1900);
							salUserProfile.setSensitivityLevel(userProfile.getSensitivity());
							salUserProfile.setGender(userProfile.getGender());
							salUserProfile.setUnitSystem(userProfile.getUnitSystem());
							if (mSalutronService != null){
								int status = mSalutronService.updateUserProfile(salUserProfile);
								LifeTrakLogger.info("status: " + status);
							}
						}
					}, HANDLER_DELAY);

					handler.postDelayed(new Runnable() {
						public void run() {
							SALTimeDate salTimeDate = new SALTimeDate();
							salTimeDate.setToNow();

							switch (timeDate.getHourFormat()) {
								case TIME_FORMAT_12_HR:
									salTimeDate.setTimeFormat(SALTimeDate.FORMAT_12HOUR);
									break;
								case TIME_FORMAT_24_HR:
									salTimeDate.setTimeFormat(SALTimeDate.FORMAT_24HOUR);
									break;
							}
							salTimeDate.setDateFormat(timeDate.getDateFormat());
							if (mSalutronService != null){
								int status = mSalutronService.updateTimeAndDate(salTimeDate);

								if (status == SALStatus.NO_ERROR) {

								}
							}
						}
					}, HANDLER_DELAY * 2);

					final SALCalibration salCalibration = new SALCalibration();

					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (calibrationData != null) {
								salCalibration.setCalibrationType(SALCalibration.STEP_CALIBRATION);
								salCalibration.setStepCalibration(calibrationData.getStepCalibration());
								if (mSalutronService != null)
									mSalutronService.updateCalibrationData(salCalibration);
							}
						}
					}, HANDLER_DELAY * 3);

					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (calibrationData != null) {
								salCalibration.setCalibrationType(SALCalibration.WALK_DISTANCE_CALIBRATION);
								salCalibration.setCalibrationValue(calibrationData.getDistanceCalibrationWalk());
								if (mSalutronService != null)
									mSalutronService.updateCalibrationData(salCalibration);
							}
						}
					}, HANDLER_DELAY * 4);

					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (calibrationData != null) {
								salCalibration.setCalibrationType(SALCalibration.AUTO_EL_SETTING);
								salCalibration.setCalibrationValue(calibrationData.getAutoEL());

								if (mSalutronService != null)
									mSalutronService.updateCalibrationData(salCalibration);
							}
						}
					}, HANDLER_DELAY * 5);

					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (mSalutronService != null)
								mSalutronService.updateStepGoal(mGoal.getStepGoal());
						}
					}, HANDLER_DELAY * 6);

					mHandler.postDelayed(new Runnable() {
						public void run() {
							double distanceGoal = mGoal.getDistanceGoal() * 100.0;
							if (mSalutronService != null)
								mSalutronService.updateDistanceGoal((long)distanceGoal);
						}
					}, HANDLER_DELAY * 7);

					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (mSalutronService != null)
								mSalutronService.updateCalorieGoal(mGoal.getCalorieGoal());
						}
					}, HANDLER_DELAY * 8);

					mHandler.postDelayed(new Runnable() {
						public void run() {
							if(mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_C300) {
								SALSleepSetting sleepSetting = new SALSleepSetting();
								List<SleepSetting> sleepSettings = DataSource.getInstance(MainActivity.this)
										.getReadOperation()
										.query("watchSleepSetting = ?", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
										.getResults(SleepSetting.class);

								if(sleepSettings.size() > 0) {
									sleepSetting.setSleepDetectType(sleepSettings.get(0).getSleepDetectType());
									sleepSetting.setSleepGoal(mGoal.getSleepGoal());
									if (mSalutronService != null)
										mSalutronService.updateSleepSetting(sleepSetting);
								}
							}
						}
					}, HANDLER_DELAY * 9);

					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R415)
								mSalutronService.disconnectFromDevice();

							if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC)) {

								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										if (mContentFragment instanceof MyAccountFragment){
											((MyAccountFragment) mContentFragment).initializeObjects();
										}

									}
								});



								mProgressDialog.setMessage(getString(R.string.syncing_to_server));
								mCurrentApiRequest = API_REQUEST_SEND;

								Date expirationDate = getExpirationDate();
								Date now = new Date();

								if (now.after(expirationDate)) {
									mCurrentOperation = OPERATION_REFRESH_TOKEN;
									refreshToken();
								} else {
									if (mSyncSuccess) {
										mCurrentOperation = OPERATION_CHECK_SERVERTIME;
										startCheckingServer();
										//startSyncToServer();
									}
								}
							} else {
								mProgressDialog.dismiss();
								if (mSalutronService != null)
									if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R415)
										mSalutronService.disconnectFromDevice();

								if (mContentFragment instanceof DashboardFragment) {
									Date date = new Date();
									mLifeTrakApplication.setCurrentDate(date);
									setCalendarDate(date);
									DashboardFragment fragment = (DashboardFragment) mContentFragment;
									fragment.initializeObjects();
								}

								if(isFinishing()) {
									AlertDialog alert = new AlertDialog.Builder(MainActivity.this).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setCancelable(false)
											.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface arg0, int arg1) {
													arg0.dismiss();
												}
											}).create();
									alert.show();
								}
								else{
									Intent intent = new Intent(getApplicationContext(), DialogActivitySyncSuccess.class);
									startActivity(intent);
								}
							}
						}
					}, HANDLER_DELAY * 10);
				}
			} else if (requestCode == REQUEST_CODE_PROFILE_SELECT_R450 && resultCode == RESULT_OK) {
				if (mProgressDialog == null)
					reinitializeProgress();
				mProgressDialog.setMessage(getString(R.string.syncing_data_2));
				mProgressDialog.show();


				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Watch watch = (Watch) mLifeTrakApplication.getSelectedWatch().clone();
							watch.setGoals(mLifeTrakSyncR450.mGoals);
							watch.setTimeDate(mLifeTrakSyncR450.mTimeDate);
							watch.setCalibrationData(mLifeTrakSyncR450.mCalibrationData);
							watch.setWakeupSetting(mLifeTrakSyncR450.mWakeupSetting);
							watch.setDayLightDetectSetting(mLifeTrakSyncR450.mDayLightDetectSetting);
							watch.setNightLightDetectSetting(mLifeTrakSyncR450.mNightLightDetectSetting);
							watch.setActivityAlertSetting(mLifeTrakSyncR450.mActivityAlertSetting);
							watch.setUserProfile(mLifeTrakSyncR450.mUserProfile);

							LifeTrakUpdateR450 lifeTrakUpdateR450 = LifeTrakUpdateR450.newInstance(MainActivity.this, watch);
							int useSetting = data.getIntExtra(USE_SETTING, USE_APP);
							switch (useSetting) {
								case USE_APP:
									lifeTrakUpdateR450.updateAllSettingsFromApp(mSalutronService);
									break;
								case USE_WATCH:
									lifeTrakUpdateR450.updateAllSettingsFromWatch(mLifeTrakApplication);
									mLifeTrakApplication.setUserProfile(watch.getUserProfile());
									mLifeTrakApplication.setTimeDate(lifeTrakUpdateR450.timeDateForCurrentWatch());
									break;
							}



							mHandler.postDelayed(new Runnable() {
								@Override
								public void run() {

									if (mContentFragment instanceof MyAccountFragment){
										Log.v("Salutron", "initializeObjects");
										((MyAccountFragment) mContentFragment).initializeObjects();
									}

									if (mContentFragment instanceof DashboardFragment) {
										Date date = new Date();
										mLifeTrakApplication.setCurrentDate(date);
										setCalendarDate(date);
										DashboardFragment fragment = (DashboardFragment) mContentFragment;
										fragment.initializeObjects();
									}

									initializeSyncToServer();
								}
							}, 750);
						} catch (CloneNotSupportedException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
			else if (requestCode == REQUEST_GOOGLE_FIT_OAUTH){
//				authInProgress = false;
//				if (resultCode == RESULT_OK) {
//					// Make sure the app is not already connected or attempting to connect
//					if (!mClient.isConnecting() && !mClient.isConnected()) {
//						mClient.connect();
//					}
//				}
			}
			else if (requestCode == REQUEST_CODE_PROFILE_SELECT_R420 && resultCode == RESULT_OK){
				if (mProgressDialog == null)
					reinitializeProgress();
				mProgressDialog.setMessage(getString(R.string.syncing_data_2));
				if (!mProgressDialog.isShowing())
					mProgressDialog.show();


				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Watch watch = (Watch) mLifeTrakApplication.getSelectedWatch().clone();
							List<Goal> mGoals = new ArrayList<Goal>();
							mGoals.add(mLifeTrakSyncR420.getGoal());
							watch.setGoals(mGoals);
							watch.setTimeDate(mLifeTrakSyncR420.getTimeDate());
							watch.setCalibrationData(mLifeTrakSyncR420.getCalibrationData());
							watch.setUserProfile(mLifeTrakSyncR420.getUserProfile());

							LifeTrakUpdateR420 lifeTrakUpdateR420 = LifeTrakUpdateR420.newInstance(MainActivity.this, watch);
							int useSetting = data.getIntExtra(USE_SETTING, USE_APP);
							switch (useSetting) {
								case USE_APP:
									lifeTrakUpdateR420.updateAllSettingsFromApp(mSalutronService, mLifeTrakSyncR420.getTimeDate());
									break;
								case USE_WATCH:
									lifeTrakUpdateR420.updateAllSettingsFromWatch(mLifeTrakApplication, mLifeTrakSyncR420.getWorkoutSettings());
									mLifeTrakApplication.setTimeDate(mLifeTrakSyncR420.getTimeDate());
									mLifeTrakApplication.setUserProfile(mLifeTrakSyncR420.getUserProfile());
									break;
							}




							mHandler.postDelayed(new Runnable() {
								@Override
								public void run() {
									mLifeTrakSyncR420.getBLEService().disconnectFromDevice();

									if (mContentFragment instanceof MyAccountFragment){
										Log.v("Salutron", "initializeObjects");
										((MyAccountFragment) mContentFragment).initializeObjects();
									}

									if (mContentFragment instanceof DashboardFragment) {
										Date date = new Date();
										mLifeTrakApplication.setCurrentDate(date);
										setCalendarDate(date);
										DashboardFragment fragment = (DashboardFragment) mContentFragment;
										fragment.initializeObjects();
									}

									initializeSyncToServer();
								}
							}, 750);
						} catch (CloneNotSupportedException e) {
							e.printStackTrace();
						}
					}
				}).start();


			}

//	
		}
		catch (Exception e){

		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		googleApiClientManager = new GoogleApiClientManager(this);
		googleApiClientManager.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminate(true);

		setContentView(R.layout.fragment_content_frame);
		setBehindContentView(R.layout.fragment_menu_frame);

		LifeTrakLogger.configure();



		bluetoothListener = new BluetoothListener(this);
		mLifeTrakApplication = (LifeTrakApplication) getApplicationContext();
		mLifeTrakSyncR450 = LifeTrakSyncR450.getInstance(this);
		mLifeTrakSyncR420 = LifeTrakSyncR420.getInstance(this);
		mPreferenceWrapper = PreferenceWrapper.getInstance(this);
		mServerSyncAsync = new ServerSyncAsync(this);
		mServerSyncAsync.setAsyncListener(this);
		mServerSyncAsyncAmazon = new ServerSyncAsyncS3Amazon(this);
		mServerSyncAsyncAmazon.setAsyncListener(this);

		mServerRestoreAsync = new ServerRestoreAsync<JSONObject>(MainActivity.this);
		mServerRestoreAsync.setAsyncListener(this);

		mEditProfileAsync = new EditProfileAsync<JSONObject>(MainActivity.this);
		mEditProfileAsync.setAsyncListener(this);

		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		alarmIntent = new Intent(MainActivity.this, AlarmNotifReceiver.class);
		pendingIntent = PendingIntent.getBroadcast(  MainActivity.this, 0, alarmIntent, 0);

		if (getIntent().getExtras() != null) {
			mSyncSuccess = getIntent().getExtras().getBoolean(SYNC_SUCCESS);
			mWatchConnected = getIntent().getExtras().getBoolean(IS_WATCH_CONNECTED, false);
			boolean openedFromNotification = getIntent().getExtras().getBoolean(OPENED_FROM_NOTIFICATION);
			//mWatchConnected = mPreferenceWrapper.getPreferenceBooleanValue(IS_WATCH_CONNECTED);

			if (openedFromNotification) {

				final List<Watch> watches = DataSource.getInstance(this)
						.getReadOperation()
						.query("_id = ?", String.valueOf(mPreferenceWrapper.getPreferenceLongValue(LAST_CONNECTED_WATCH_ID)))
						.getResults(Watch.class);
				if (watches.size() != 0) {
					mLifeTrakApplication.setSelectedWatch(watches.get(0));

					mHandler.postDelayed(new Runnable() {
						public void run() {
							syncWatch();
						}
					}, HANDLER_DELAY);
				}

			}
		}

		if (savedInstanceState != null) {
			mMenuFragment = (SherlockFragment) getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_MENU);
			mContentFragment = (SherlockFragment) getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_CONTENT);
			mLifeTrakApplication.setSelectedWatch((Watch) savedInstanceState.getParcelable(LAST_CONNECTED_WATCH_ID));
		} else {
			mMenuFragment = FragmentFactory.newInstance(MenuFragment.class);
			mContentFragment = FragmentFactory.newInstance(DashboardFragment.class);
		}

		getSupportFragmentManager().beginTransaction().detach(mMenuFragment).add(R.id.frmMenuFrame, mMenuFragment).attach(mMenuFragment).commit();

		getSupportFragmentManager().beginTransaction().detach(mContentFragment).add(R.id.frmContentFrame, mContentFragment).attach(mContentFragment).commit();

		SlidingMenu slidingMenu = getSlidingMenu();
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setShadowDrawable(R.drawable.sliding_menu_shadow);
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setIcon(R.drawable.lifetrak_mnav_icon_menu);

		mDateFormat.applyPattern("MMMM dd, yyyy");
		mDateFormatMonth.applyPattern("MMMM yyyy");
		mDateFormatWeek.applyPattern("MMM dd, yyyy");

		mCalendarDay = (TextView) findViewById(R.id.tvwCalendarDay);
		if (mLifeTrakApplication != null)
			mCalendarDay.setText(mDateFormat.format(mLifeTrakApplication.getCurrentDate()));
		else
			mCalendarDay.setText(mDateFormat.format(new Date()));

		mCalendarContainer = (SlidingDrawer) findViewById(R.id.sldCalendarContainer);

		mCalendarContainer.setOnDrawerOpenListener(mDrawerOpenListener);
		mCalendarContainer.setOnDrawerCloseListener(mDrawerCloseListener);

		mContentSwitcher = (ViewFlipper) findViewById(R.id.cdrContent);
		//mCalendar = (CalendarPicker) findViewById(R.id.cdrCalendarMain);
		mCalendar = (CalendarControlView) findViewById(R.id.viewCalendarControl);
		mYearList = (NumberPicker) findViewById(R.id.numYear);
		mYearListView = (ListView) findViewById(R.id.lstYear);
		mConnectionFailedView = (ConnectionFailedView) findViewById(R.id.cfvConnectionFailed);

		Calendar calendarMin = Calendar.getInstance();
		Calendar calendarMax = Calendar.getInstance();

		calendarMin.add(Calendar.YEAR, -1);
		calendarMax.add(Calendar.YEAR, 1);

		//mCalendar.init(calendarMin.getTime(), calendarMax.getTime()).inMode(SelectionMode.SINGLE).withSelectedDate(mLifeTrakApplication.getCurrentDate());
		//mCalendar.setOnSelectionListener(mSelectionListener);

		Calendar calendarYear = Calendar.getInstance();
		calendarYear.setTime(new Date());

		int currentYear = calendarYear.get(Calendar.YEAR);

		calendarYear.setTime(mLifeTrakApplication.getCurrentDate());

		int yearStart = calendarYear.get(Calendar.YEAR) - 3;
		int yearEnd = calendarYear.get(Calendar.YEAR) + 1;

		mYearList.setMinValue(yearStart);
		mYearList.setMaxValue(yearEnd);
		mYearList.setValue(calendarYear.get(Calendar.YEAR));
		mYearList.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		mYearList.setOnValueChangedListener(mValueChangeListener);

		for (int i=currentYear+1;i>currentYear-100;i--) {
			mYearItems.add(String.valueOf(i));
		}

		mYearListAdapter = new ArrayAdapter<CharSequence>(this, R.layout.adapter_year_item, android.R.id.text1, mYearItems);
		mYearListView.setAdapter(mYearListAdapter);
		mYearListView.setOnItemClickListener(mListClickListener);
		mYearListView.setOnScrollListener(mListScrollListener);
		mYearListView.setSelection(1);

		mProgressDialog = new ProgressDialog(this);
		//		mProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		//		mProgressDialog.getWindow().setDimAmount(0.5f);

		mProgressDialog.setCancelable(false);
		mProgressDialog.setTitle(getString(R.string.app_name));
		mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R415) {
					if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R420)
						mSalutronService.disconnectFromDevice();
					else{
						if (mLifeTrakSyncR420!= null)
							mLifeTrakSyncR420.getBLEService().disconnectFromDevice();
					}
				}
//				else if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R420){
//					if (mLifeTrakSyncR420!= null)
//						mLifeTrakSyncR420.getBLEService().disconnectFromDevice();
//				}

				if (flag_sync){
					flag_sync = false;
					mTimeoutHandler.removeCallbacksAndMessages(watchSyncRunnable);
				}
				if (flag_sync_no_bluetooth){
					flag_sync_no_bluetooth = false;
					mTimeoutHandler.removeCallbacksAndMessages(watchSyncRunnable2);
				}
				if (mContentFragment instanceof WatchSettingsFragment){
					WatchSettingsFragment mWatchSettingsFragment = (WatchSettingsFragment) mContentFragment;
					mWatchSettingsFragment.removeCallback();
				}

				if (mContentFragment instanceof GoalFragment){
					GoalFragment mGoalFragment = (GoalFragment) mContentFragment;
					mGoalFragment.removeCallback();
				}

				mErrorHandler.removeCallbacks(mErrorRunnable);
				cancelSync();

				try {
					mServerSyncAsyncTask.cancel(true);
				}
				catch (Exception e){
					LifeTrakLogger.info("mServerSyncAsyncTask error on cancel:" + e.getLocalizedMessage());
				}
			}
		});

		if (mLifeTrakApplication.getSelectedWatch() != null)
			mProgressDialog.setMessage(getString(R.string.searching_device, stringNameForModel(mLifeTrakApplication.getSelectedWatch().getModel())));

		mAlertDialog = new AlertDialog.Builder(this).setTitle(getString(R.string.app_name)).setMessage(R.string.sync_failed).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}
		}).create();

		mGoalsAlert = new AlertDialog.Builder(this).setTitle(getString(R.string.app_name)).setItems(R.array.goals_menu_items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				switch (arg1) {
					case 0:
						onSyncGoalMenuItemClick();
						break;
					case 1:
						if (mContentFragment instanceof GoalFragment) {
							GoalFragment goalFragment = (GoalFragment) mContentFragment;
							goalFragment.resetGoals();
						}
						break;
					case 2:
						if (mContentFragment instanceof GoalFragment) {
							GoalFragment goalFragment = (GoalFragment) mContentFragment;
							goalFragment.restoreGoals();
						}
						break;
				}
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {

			}
		}).create();

		mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		getSupportFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);

		requestBluetoothOn();
		initializeCalendar();
		//initializeTickerDates();

		mLifeTrakSyncR450 = LifeTrakSyncR450.getInstance(this);

		if (mLifeTrakApplication != null){
			if (mLifeTrakApplication.getSelectedWatch() != null && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415)  {
				mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			}
		}
		mConnectionFailedView.setConnectionFailedListener(new ConnectionFailedView.ConnectionFailedListener() {
			@Override
			public void onCancelClick() {
				mConnectionFailedView.hide();
				flag_finished_syncing = false;
				flag_disable_menu = false;
			}

			@Override
			public void onTryAgainClick() {
				mConnectionFailedView.hide();
				flag_disable_menu = false;
				flag_finished_syncing = false;
				if ((mContentFragment instanceof GoalFragment)) {
					deviceFound = false;
					final int watchModel = mLifeTrakApplication.getSelectedWatch().getModel();
					Intent intent = new Intent(MainActivity.this, PairDeviceActivity.class);
					intent.putExtra(SELECTED_WATCH_MODEL, watchModel);
					startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE);
				} else {
					reSyncwatch();
				}
			}
		});



//		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//			@Override
//			public void uncaughtException(Thread thread, Throwable e) {
//				LifeTrakLogger.error("Exception in thread [" + thread.getName() + "] = " +  e);
//			}
//		});
	}

	private void reSyncwatch(){
		if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
			syncWatch();
		} else {
			if (mContentFragment instanceof DashboardFragment) {
				syncWatch();
			} else {
				final int watchModel = mLifeTrakApplication.getSelectedWatch().getModel();
				Intent intent = new Intent(MainActivity.this, PairDeviceActivity.class);
				intent.putExtra(SELECTED_WATCH_MODEL, watchModel);
				startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE);
			}
		}
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		switch (newConfig.orientation) {
			case Configuration.ORIENTATION_PORTRAIT:
				getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
				break;
			case Configuration.ORIENTATION_LANDSCAPE:
				getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
				break;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		googleApiClientManager.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, FRAGMENT_MENU, mMenuFragment);
		getSupportFragmentManager().putFragment(outState, FRAGMENT_CONTENT, mContentFragment);
		outState.putLong(LAST_CONNECTED_WATCH_ID, mLifeTrakApplication.getSelectedWatch().getId());
		outState.putParcelable(LAST_CONNECTED_WATCH_ID,  mLifeTrakApplication.getSelectedWatch());
		//(LAST_CONNECTED_WATCH_ID, mLifeTrakApplication.getSelectedWatch());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (flag_disable_menu){
			return false;
		}
		else{
			switch (item.getItemId()) {
				case android.R.id.home:
					getSlidingMenu().toggle(true);

					if (getCurrentFocus() != null) {
						mInputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
					}
					break;
				case R.id.action_sync_goal:
					mOperation = OPERATION_SYNC_SETTINGS;

					if (isBluetoothEnabled()) {
						mGoalsAlert.show();
					} else {
						startBluetoothRequest(REQUEST_CODE_ENABLE_BLUETOOTH);
					}
					break;
				case R.id.action_sync_settings:
					mOperation = OPERATION_SYNC_SETTINGS;
					//				if (mContentFragment instanceof WatchSettingsFragment) {
					//					if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415)
					//						syncWatch();
					//					else
					//						onSyncGoalMenuItemClick();
					//				}
					//				if (mContentFragment instanceof GoalItemFragment) {
					//					if (isBluetoothEnabled()) {
					//						onSyncGoalMenuItemClick();
					//					} else {
					//						startBluetoothRequest(REQUEST_CODE_ENABLE_BLUETOOTH);
					//					}
					//				}
					onSyncGoalMenuItemClick();

					break;
				case R.id.button_sync:
					//AmazonTransferUtility.getInstance(MainActivity.this).generateTextFileFromString("Blah Blah");
					syncWatch();
					break;
				case R.id.button_add_sleep:
					mPreferenceWrapper.setPreferenceBooleanValue(ADD_NEW_SLEEP, true).synchronize();
					switchFragment(FragmentFactory.newInstance(SleepDataUpdate.class));
					break;
				case R.id.button_delete_sleep:
					if (mContentFragment instanceof SleepDataUpdate) {
						AlertDialog alert = new AlertDialog.Builder(this).setTitle("LifeTrak").setMessage(R.string.delete_sleep_log).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
							}
						}).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								SleepDataUpdate sleepDataUpdate = (SleepDataUpdate) mContentFragment;
								sleepDataUpdate.deleteSleepLog();
							}
						}).create();
						alert.show();
					}
					break;
			}
			return true;
		}
	}

	private void initializeCalendar() {
		Calendar calendarFrom = Calendar.getInstance();
		Calendar calendarTo = Calendar.getInstance();

		calendarFrom.setTime(new Date());
		calendarTo.setTime(new Date());

		calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
		calendarFrom.set(Calendar.MONTH, 0);
		calendarTo.set(Calendar.DAY_OF_MONTH, 1);
		calendarTo.set(Calendar.MONTH, 11);

		mCalendar.setCalendarMode(MODE_DAY);
		mCalendar.addMonthView(calendarFrom, calendarTo);
		mCalendar.setDateChangeListener(mCalendarDateChangeListener);
	}

	/*private final CalendarPicker.OnSelectionListener mSelectionListener = new CalendarPicker.OnSelectionListener() {
		@Override
		public void onDaySelected(Date date) {
			mCalendarDay.setText(mDateFormat.format(date));

			if (mContentFragment instanceof CalendarDateChangeListener) {
				CalendarDateChangeListener listener = (CalendarDateChangeListener) mContentFragment;
				listener.onCalendarDateChange(date);
			}

			mCalendarContainer.close();
		}

		@Override
		public void onWeekSelected(Date from, Date to) {
			mCalendarDay.setText(mDateFormatWeek.format(from) + " - " + mDateFormatWeek.format(to));

			if (mContentFragment instanceof CalendarDateChangeListener) {
				CalendarDateChangeListener listener = (CalendarDateChangeListener) mContentFragment;
				listener.onCalendarWeekChange(from, to);
			}

			mCalendarContainer.close();
		}

		@Override
		public void onMonthSelected(Date from, Date to) {
			mCalendarDay.setText(mDateFormatMonth.format(from));

			if (mContentFragment instanceof CalendarDateChangeListener) {
				CalendarDateChangeListener listener = (CalendarDateChangeListener) mContentFragment;
				listener.onCalendarMonthChange(from, to);
			}

			mCalendarContainer.close();
		}
	};*/

	private final CalendarDateChangeListener mCalendarDateChangeListener = new CalendarDateChangeListener() {
		@Override
		public void onCalendarDateChange(Date date) {
			mCalendarDay.setText(mDateFormat.format(date));

			if (mContentFragment instanceof CalendarDateChangeListener) {
				CalendarDateChangeListener listener = (CalendarDateChangeListener) mContentFragment;
				listener.onCalendarDateChange(date);
			}

			mCalendarContainer.close();
		}

		@Override
		public void onCalendarWeekChange(Date from, Date to) {
			mCalendarDay.setText(mDateFormatWeek.format(from) + " - " + mDateFormatWeek.format(to));

			if (mContentFragment instanceof CalendarDateChangeListener) {
				CalendarDateChangeListener listener = (CalendarDateChangeListener) mContentFragment;
				listener.onCalendarWeekChange(from, to);
			}

			mCalendarContainer.close();
		}

		@Override
		public void onCalendarMonthChange(Date from, Date to) {
			mCalendarDay.setText(mDateFormatMonth.format(from));

			if (mContentFragment instanceof CalendarDateChangeListener) {
				CalendarDateChangeListener listener = (CalendarDateChangeListener) mContentFragment;
				listener.onCalendarMonthChange(from, to);
			}

			mCalendarContainer.close();
		}

		@Override
		public void onCalendarYearChange(int year) {
		}

	};

	private final NumberPicker.OnValueChangeListener mValueChangeListener = new NumberPicker.OnValueChangeListener() {

		@Override
		public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
			if (mContentFragment instanceof CalendarDateChangeListener) {
				Calendar calFrom = Calendar.getInstance();
				Calendar calTo = Calendar.getInstance();

				calFrom.set(Calendar.DAY_OF_MONTH, 1);
				calFrom.set(Calendar.MONTH, 0);
				calFrom.set(Calendar.YEAR, newVal);

				calTo.set(Calendar.DAY_OF_MONTH, 31);
				calTo.set(Calendar.MONTH, 11);
				calTo.set(Calendar.YEAR, newVal);

				mLifeTrakApplication.setDateRangeFrom(calFrom.getTime());
				mLifeTrakApplication.setDateRangeTo(calTo.getTime());

				mCalendarDay.setText(String.valueOf(newVal));
				CalendarDateChangeListener listener = (CalendarDateChangeListener) mContentFragment;
				listener.onCalendarYearChange(newVal);
				mLifeTrakApplication.setCurrentYear(newVal);
			}
			mCalendarContainer.close();
		}
	};

	private final AdapterView.OnItemClickListener mListClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
			int newVal = Integer.parseInt(mYearItems.get(position).toString());

			if (mContentFragment instanceof CalendarDateChangeListener) {
				Calendar calFrom = Calendar.getInstance();
				Calendar calTo = Calendar.getInstance();

				calFrom.set(Calendar.DAY_OF_MONTH, 1);
				calFrom.set(Calendar.MONTH, 0);
				calFrom.set(Calendar.YEAR, newVal);

				calTo.set(Calendar.DAY_OF_MONTH, 31);
				calTo.set(Calendar.MONTH, 11);
				calTo.set(Calendar.YEAR, newVal);

				mLifeTrakApplication.setDateRangeFrom(calFrom.getTime());
				mLifeTrakApplication.setDateRangeTo(calTo.getTime());

				mCalendarDay.setText(String.valueOf(newVal));
				CalendarDateChangeListener listener = (CalendarDateChangeListener) mContentFragment;
				listener.onCalendarYearChange(newVal);
				mLifeTrakApplication.setCurrentYear(newVal);
			}
			mCalendarContainer.close();
		}
	};

	private final AbsListView.OnScrollListener mListScrollListener = new AbsListView.OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (firstVisibleItem == 0) {
				int currentYear = Integer.parseInt(mYearItems.get(0).toString());
				currentYear += 1;
				mYearItems.add(0, String.valueOf(currentYear));
				mYearListAdapter.notifyDataSetChanged();
				mYearListView.setSelection(2);
			}
		}
	};

	public void setCalendarDate(Date date) {
		mContentSwitcher.setDisplayedChild(0);
		mCalendarDay.setText(mDateFormat.format(date));
		// setCalendarMode(MODE_DAY);
	}

	public void setCalendarDateWeek(Date from, Date to) {
		mContentSwitcher.setDisplayedChild(0);
		mCalendarDay.setText(mDateFormat.format(from) + " - " + mDateFormat.format(to));
	}

	public void setCalendarLabel(Date date) {
		mCalendarDay.setText(mDateFormat.format(date));
	}

	public void selectCalendarDate(Date date) {
		mCalendar.clearPreviousDate();
		mCalendar.selectDate(date);
	}

	public void setCalendarMonth(Date date) {
		mCalendarDay.setText(mDateFormatMonth.format(date));
	}

	public void setCalendarYear(int year) {
		mContentSwitcher.setDisplayedChild(1);
		mCalendarDay.setText(String.valueOf(year));
	}

	public void onCalendarNavClick(View view) {
		if (mContentFragment instanceof DashboardFragment) {
			Date date = new Date();

			switch (view.getId()) {
				case R.id.btnCalendarBack:
					date = mLifeTrakApplication.getPreviousDay();
					mCalendar.selectDate(date);
					break;
				case R.id.btnCalendarNext:
					date = mLifeTrakApplication.getNextDay();
					mCalendar.selectDate(date);
					break;
			}

			Calendar calLimitMin = Calendar.getInstance();
			Calendar calLimitMax = Calendar.getInstance();

			calLimitMin.add(Calendar.DAY_OF_MONTH, 1);
			calLimitMax.add(Calendar.DAY_OF_MONTH, -1);
			calLimitMin.add(Calendar.YEAR, -1);
			calLimitMax.add(Calendar.YEAR, 1);

			Date dateMin = calLimitMin.getTime();
			Date dateMax = calLimitMax.getTime();

			if (date.before(dateMin)) {
				mLifeTrakApplication.setCurrentDate(dateMin);
				return;
			}

			if (date.after(dateMax)) {
				mLifeTrakApplication.setCurrentDate(dateMax);
				return;
			}

			setCalendarDate(date);

			DashboardFragment fragment = (DashboardFragment) mContentFragment;
			fragment.setDataWithDate(date);

		} else if (mContentFragment instanceof FitnessResultsFragment) {
			initFitnessResultsData(view);
		} else if (mContentFragment instanceof HeartRateFragment) {
			initHeartRateData(view);
		}
		else if (mContentFragment instanceof HeartRateFragmentR420) {
			HeartRateFragmentR420 fragment = (HeartRateFragmentR420) mContentFragment;

			Date date = new Date();

			switch (view.getId()) {
				case R.id.btnCalendarBack:
					date = mLifeTrakApplication.getPreviousDay();
					break;
				case R.id.btnCalendarNext:
					date = mLifeTrakApplication.getNextDay();
					break;
			}

			setCalendarDate(date);
			fragment.setDataWithDay(date);

		}
		else if (mContentFragment instanceof ActigraphyFragment) {
			ActigraphyFragment fragment = (ActigraphyFragment) mContentFragment;
			Date date = new Date();

			switch (view.getId()) {
				case R.id.btnCalendarBack:
					date = mLifeTrakApplication.getPreviousDay();
					break;
				case R.id.btnCalendarNext:
					date = mLifeTrakApplication.getNextDay();
					break;
			}
			setCalendarDate(date);
			fragment.setDataWithDate(date);
		}
		else if (mContentFragment instanceof WorkoutGraphFragmentR420) {
			WorkoutGraphFragmentR420 fragment = (WorkoutGraphFragmentR420) mContentFragment;
			Date date = new Date();

			switch (view.getId()) {
				case R.id.btnCalendarBack:
					date = mLifeTrakApplication.getPreviousDay();
					break;
				case R.id.btnCalendarNext:
					date = mLifeTrakApplication.getNextDay();
					break;
			}
			setCalendarDate(date);
			fragment.setDataWithDate(date);
		}

		else if (mContentFragment instanceof SleepDataFragment) {
			SleepDataFragment fragment = (SleepDataFragment) mContentFragment;
			Date date = new Date();

			switch (view.getId()) {
				case R.id.btnCalendarBack:
					date = mLifeTrakApplication.getPreviousDay();
					break;
				case R.id.btnCalendarNext:
					date = mLifeTrakApplication.getNextDay();
					break;
			}
			setCalendarDate(date);
			fragment.onCalendarDateChange(date);
		} else if(mContentFragment instanceof WorkoutGraphFragment) {
			WorkoutGraphFragment fragment = (WorkoutGraphFragment) mContentFragment;
			Date date = new Date();

			switch (view.getId()) {
				case R.id.btnCalendarBack:
					date = mLifeTrakApplication.getPreviousDay();
					break;
				case R.id.btnCalendarNext:
					date = mLifeTrakApplication.getNextDay();
					break;
			}
			setCalendarDate(date);
			fragment.onCalendarDateChange(date);
		}
		else if (mContentFragment instanceof LightPlotPagerFragment) {
			LightPlotPagerFragment fragment = (LightPlotPagerFragment) mContentFragment;
			Date date = new Date();

			switch (view.getId()) {
				case R.id.btnCalendarBack:
					date = mLifeTrakApplication.getPreviousDay();
					break;
				case R.id.btnCalendarNext:
					date = mLifeTrakApplication.getNextDay();
					break;
			}
			setCalendarDate(date);
			fragment.onCalendarDateChange(date);
		}
		else if (mContentFragment instanceof WorkoutFragment) {
			WorkoutFragment fragment = (WorkoutFragment) mContentFragment;
			Date date = new Date();

			switch (view.getId()) {
				case R.id.btnCalendarBack:
					date = mLifeTrakApplication.getPreviousDay();
					break;
				case R.id.btnCalendarNext:
					date = mLifeTrakApplication.getNextDay();
					break;
			}
			setCalendarDate(date);
			fragment.onCalendarDateChange(date);
		}
	}

	private void initFitnessResultsData(View view) {
		FitnessResultsFragment fragment = (FitnessResultsFragment) mContentFragment;

		Calendar calFrom = Calendar.getInstance();
		Calendar calTo = Calendar.getInstance();

		Date dateNow = mLifeTrakApplication.getCurrentDate();
		Date dateFrom = new Date();
		Date dateTo = new Date();

		switch (fragment.getCalendarMode()) {
			case MODE_DAY:
				switch (view.getId()) {
					case R.id.btnCalendarBack:
						dateNow = mLifeTrakApplication.getPreviousDay();
						break;
					case R.id.btnCalendarNext:
						dateNow = mLifeTrakApplication.getNextDay();
						break;
				}
				setCalendarDate(dateNow);
				fragment.setDataWithDate(dateNow);
				break;
			case MODE_WEEK:
				calFrom.setTime(mLifeTrakApplication.getDateRangeFrom());

				switch (view.getId()) {
					case R.id.btnCalendarBack:
						calFrom.add(Calendar.WEEK_OF_YEAR, -1);
						calTo.setTime(calFrom.getTime());
						calTo.add(Calendar.DAY_OF_MONTH, 6);
						dateFrom = calFrom.getTime();
						dateTo = calTo.getTime();
						break;
					case R.id.btnCalendarNext:
						calFrom.add(Calendar.WEEK_OF_YEAR, 1);
						calTo.setTime(calFrom.getTime());
						calTo.add(Calendar.DAY_OF_MONTH, 6);
						dateFrom = calFrom.getTime();
						dateTo = calTo.getTime();
						break;
				}

				mLifeTrakApplication.setCurrentDate(dateFrom);
				mLifeTrakApplication.setDateRangeFrom(dateFrom);
				mLifeTrakApplication.setDateRangeTo(dateTo);

				setCalendarDateWeek(dateFrom, dateTo);
				fragment.setDataWithDate(dateFrom, dateTo, MODE_WEEK);
				break;
			case MODE_MONTH:
				calFrom.setTime(mLifeTrakApplication.getDateRangeFrom());
				calFrom.set(Calendar.DAY_OF_MONTH, calFrom.getFirstDayOfWeek());
				int maxDays = calFrom.getActualMaximum(Calendar.DAY_OF_MONTH);

				switch (view.getId()) {
					case R.id.btnCalendarBack:
						calFrom.add(Calendar.MONTH, -1);
						calTo.setTime(calFrom.getTime());
						maxDays = calFrom.getActualMaximum(Calendar.DAY_OF_MONTH);
						calTo.add(Calendar.DAY_OF_MONTH, maxDays - 1);
						dateFrom = calFrom.getTime();
						dateTo = calTo.getTime();
						break;
					case R.id.btnCalendarNext:
						calFrom.add(Calendar.MONTH, 1);
						calTo.setTime(calFrom.getTime());
						maxDays = calFrom.getActualMaximum(Calendar.DAY_OF_MONTH);
						calTo.add(Calendar.DAY_OF_MONTH, maxDays - 1);
						dateFrom = calFrom.getTime();
						dateTo = calTo.getTime();
						break;
				}

				mLifeTrakApplication.setCurrentDate(dateFrom);
				mLifeTrakApplication.setDateRangeFrom(dateFrom);
				mLifeTrakApplication.setDateRangeTo(dateTo);

				mCalendarDay.setText(mDateFormatMonth.format(dateFrom));
				fragment.setDataWithDate(dateFrom, dateTo, MODE_MONTH);
				break;
			case MODE_YEAR:
				int year = mLifeTrakApplication.getCurrentYear();

				switch (view.getId()) {
					case R.id.btnCalendarBack:
						year--;
						mLifeTrakApplication.setCurrentYear(year);
						break;
					case R.id.btnCalendarNext:
						year++;
						mLifeTrakApplication.setCurrentYear(year);
						break;
				}
				setCalendarYear(year);
				fragment.onCalendarYearChange(year);
				//			fragment.setDataWithDate(year);
				break;
		}
	}

	private void initHeartRateData(View view) {
		HeartRateFragment fragment = (HeartRateFragment) mContentFragment;

		Calendar calFrom = Calendar.getInstance();
		Calendar calTo = Calendar.getInstance();

		Date dateNow = mLifeTrakApplication.getCurrentDate();
		Date dateFrom = new Date();
		Date dateTo = new Date();

		switch (fragment.getCalendarMode()) {
			case MODE_DAY:
				switch (view.getId()) {
					case R.id.btnCalendarBack:
						dateNow = mLifeTrakApplication.getPreviousDay();
						break;
					case R.id.btnCalendarNext:
						dateNow = mLifeTrakApplication.getNextDay();
						break;
				}
				setCalendarDate(dateNow);
				fragment.setDataWithDay(dateNow);
				break;
			case MODE_WEEK:
				calFrom.setTime(mLifeTrakApplication.getDateRangeFrom());

				switch (view.getId()) {
					case R.id.btnCalendarBack:
						calFrom.add(Calendar.WEEK_OF_YEAR, -1);
						calTo.setTime(calFrom.getTime());
						calTo.add(Calendar.DAY_OF_MONTH, 6);
						dateFrom = calFrom.getTime();
						dateTo = calTo.getTime();
						break;
					case R.id.btnCalendarNext:
						calFrom.add(Calendar.WEEK_OF_YEAR, 1);
						calTo.setTime(calFrom.getTime());
						calTo.add(Calendar.DAY_OF_MONTH, 6);
						dateFrom = calFrom.getTime();
						dateTo = calTo.getTime();
						break;
				}

				mLifeTrakApplication.setCurrentDate(dateFrom);
				mLifeTrakApplication.setDateRangeFrom(dateFrom);
				mLifeTrakApplication.setDateRangeTo(dateTo);

				setCalendarDateWeek(dateFrom, dateTo);
				fragment.setDataWithWeek(dateFrom, dateTo);
				break;
			case MODE_MONTH:
				calFrom.setTime(mLifeTrakApplication.getDateRangeFrom());
				calFrom.set(Calendar.DAY_OF_MONTH, calFrom.getFirstDayOfWeek());
				int maxDays = calFrom.getActualMaximum(Calendar.DAY_OF_MONTH);

				switch (view.getId()) {
					case R.id.btnCalendarBack:
						calFrom.add(Calendar.MONTH, -1);
						calTo.setTime(calFrom.getTime());
						maxDays = calFrom.getActualMaximum(Calendar.DAY_OF_MONTH);
						calTo.add(Calendar.DAY_OF_MONTH, maxDays - 1);
						dateFrom = calFrom.getTime();
						dateTo = calTo.getTime();
						break;
					case R.id.btnCalendarNext:
						calFrom.add(Calendar.MONTH, 1);
						calTo.setTime(calFrom.getTime());
						maxDays = calFrom.getActualMaximum(Calendar.DAY_OF_MONTH);
						calTo.add(Calendar.DAY_OF_MONTH, maxDays - 1);
						dateFrom = calFrom.getTime();
						dateTo = calTo.getTime();
						break;
				}

				mLifeTrakApplication.setCurrentDate(dateFrom);
				mLifeTrakApplication.setDateRangeFrom(dateFrom);
				mLifeTrakApplication.setDateRangeTo(dateTo);

				mCalendarDay.setText(mDateFormatMonth.format(dateFrom));
				fragment.setDataWithMonth(dateFrom, dateTo);
				break;
			case MODE_YEAR:
				int year = mLifeTrakApplication.getCurrentYear();

				switch (view.getId()) {
					case R.id.btnCalendarBack:
						year--;
						mLifeTrakApplication.setCurrentYear(year);
						break;
					case R.id.btnCalendarNext:
						year++;
						mLifeTrakApplication.setCurrentYear(year);
						break;
				}
				setCalendarYear(year);
				fragment.onCalendarYearChange(year);
				//setCalendarMode(MODE_YEAR);
				break;
		}
	}

	public void switchFragment(SherlockFragment fragment) {
		mContentFragment = fragment;

		mTimeoutHandler.postDelayed(new Runnable() {
			public void run() {
				getSupportFragmentManager().beginTransaction().detach(mContentFragment).setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).replace(R.id.frmContentFrame, mContentFragment)
						.attach(mContentFragment).addToBackStack(FRAGMENT_TAG).commit();
			}
		}, 400);
	}

	public void switchFragment2(SherlockFragment fragment) {
		mContentFragment = fragment;
		getSupportFragmentManager().beginTransaction().detach(mContentFragment).setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).replace(R.id.frmContentFrame, mContentFragment)
				.attach(mContentFragment).addToBackStack(FRAGMENT_TAG).commit();
	}

	public void setCalendarMode(int calendarMode) {
		if (calendarMode == MODE_YEAR) {
			mContentSwitcher.setDisplayedChild(1);

			if (mContentFragment instanceof CalendarDateChangeListener) {
				int year = mLifeTrakApplication.getCurrentYear();

				CalendarDateChangeListener listener = (CalendarDateChangeListener) mContentFragment;
				listener.onCalendarYearChange(year);
				mCalendarDay.setText(String.valueOf(year));
				mYearList.setValue(year);
			}
		} else {
			mContentSwitcher.setDisplayedChild(0);
			mCalendar.setCalendarMode(calendarMode);
		}

		if (mContentFragment instanceof FitnessResultsFragment) {
			FitnessResultsFragment fragment = (FitnessResultsFragment) mContentFragment;
			fragment.setCalendarMode(calendarMode);
		} else if (mContentFragment instanceof HeartRateFragment) {
			HeartRateFragment fragment = (HeartRateFragment) mContentFragment;
			fragment.setCalendarMode(calendarMode);
		}
	}

	public void setCalendarPickerMode(int calendarMode) {
		//mCalendar.setCalendarModeOnly(calendarMode);
	}

	public void setCalendarPickerMode(int calendarMode, Date from, Date to) {
		//mCalendar.setCalendarModeOnly(calendarMode, from, to);
	}

	public void changeCalendarMode(int calendarMode, Date date) {
		//mCalendar.changeCalendarMode(calendarMode, date);
	}

	public void hideCalendar() {
		mCalendarContainer.setVisibility(View.GONE);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		findViewById(R.id.frmContentFrame).setLayoutParams(params);
	}

	public void showCalendar() {
		mCalendarContainer.setVisibility(View.VISIBLE);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		params.bottomMargin = (int) dpToPx(50);
		findViewById(R.id.frmContentFrame).setLayoutParams(params);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final com.salutron.lifetrakwatchapp.adapter.MenuItem menu = (com.salutron.lifetrakwatchapp.adapter.MenuItem) parent.getAdapter().getItem(position);

		switch (menu.getItemType()) {
			case MENU_ITEM_ACCOUNT:
				findViewById(R.id.sldCalendarContainer).setVisibility(View.INVISIBLE);
				switchFragment(FragmentFactory.newInstance(MyAccountFragment.class));
				hideCalendar();
				break;
			case MENU_ITEM_DASHBOARD:
				findViewById(R.id.sldCalendarContainer).setVisibility(View.VISIBLE);
				switchFragment(FragmentFactory.newInstance(DashboardFragment.class));
				showCalendar();
				break;
			case MENU_ITEM_GOALS:
				findViewById(R.id.sldCalendarContainer).setVisibility(View.INVISIBLE);
				switchFragment(FragmentFactory.newInstance(GoalFragment.class));
				hideCalendar();
				break;
			case MENU_ITEM_SETTINGS:
				final WatchSettingsFragment watchSettingsFragment = FragmentFactory.newInstance(WatchSettingsFragment.class);
				final Bundle args = new Bundle();

				args.putParcelable("calibrationData", mCalibrationData);
				watchSettingsFragment.setArguments(args);
				switchFragment(watchSettingsFragment);

				hideCalendar();
				break;
			case MENU_ITEM_PARTNERS:
				final RewardsFragment rewardsFragment = FragmentFactory.newInstance(RewardsFragment.class);
				switchFragment(rewardsFragment);
				break;
			case MENU_ITEM_HELP:
				switchFragment(FragmentFactory.newInstance(HelpFragment.class));
				hideCalendar();
				break;
			case MENU_ITEM_LOGOUT:
				AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(R.string.verify_signout)
						.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// mPreferenceWrapper.setPreferenceStringValue(ACCESS_TOKEN,
								// null)
								// .setPreferenceStringValue(REFRESH_TOKEN, null)
								// .synchronize();
								mPreferenceWrapper.clearSharedPref();
								mLifeTrakApplication.clearDB();
								if (null != Session.getActiveSession()) {
									Session.getActiveSession().closeAndClearTokenInformation();
								}
								Intent intent = new Intent(MainActivity.this, IntroductionActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
								startActivity(intent);
								finish();
							}
						}).create();
				alert.show();
				break;
		}

		findViewById(R.id.frmModalView).setVisibility(View.GONE);
		getSlidingMenu().toggle(true);
	}

	private final FragmentManager.OnBackStackChangedListener mBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
		@Override
		public void onBackStackChanged() {
			mContentFragment = (SherlockFragment) getSupportFragmentManager().findFragmentById(R.id.frmContentFrame);

			/*
			 * if (mContentFragment instanceof DashboardFragment ||
			 * mContentFragment instanceof ActigraphyFragment ||
			 * mContentFragment instanceof HeartRateFragment || mContentFragment
			 * instanceof SleepDataFragment) { showCalendar(); } else {
			 * hideCalendar(); }
			 */

			if (mContentFragment instanceof DashboardFragment) {
				mContentSwitcher.setDisplayedChild(0);
				//mCalendar.setOnSelectionListener(null);
				mCalendar.setCalendarMode(MODE_DAY);
				//mCalendar.setOnSelectionListener(mSelectionListener);
				mCalendarDay.setText(mDateFormat.format(mLifeTrakApplication.getCurrentDate()));
			}
		}
	};

	private String stringNameForModel(int model) {
		switch (model) {
			case WATCHMODEL_C300:
				return "Move C300";
			case WATCHMODEL_C300_IOS:
				return "Move C300";
			case WATCHMODEL_C410:
				return "Zone C410";
			case WATCHMODEL_R415:
				return "Brite R450";
			case WATCHMODEL_R420:
				return "R420";
			case WATCHMODEL_R500:
				return "R500";
		}
		return null;
	}

	/*
	 * Methods for handling sync
	 */
	private UserProfile mUserProfile;
	private TimeDate mTimeDate;
	private List<Integer> mDataHeaderIndexes = new ArrayList<Integer>();
	private List<StatisticalDataHeader> mDataHeaders = new ArrayList<StatisticalDataHeader>();
	private List<StatisticalDataPoint> mDataPoints = new ArrayList<StatisticalDataPoint>();
	private List<WorkoutInfo> mWorkoutInfos = new ArrayList<WorkoutInfo>();
	private List<SleepDatabase> mSleepDatabases = new ArrayList<SleepDatabase>();
	private StatisticalDataHeader mCurrentDataHeader;
	private int mCalibrationType;
	private CalibrationData mCalibrationData;
	private long mStepGoal;
	private double mDistanceGoal;
	private long mCalorieGoal;
	private SleepSetting mSleepSetting;
	private int mDataHeaderIndex;
	private Goal mGoal;
	private boolean mIsUpdateTimeDate = false;

	private List<StatisticalDataHeader> mTempDataHeaders = new ArrayList<StatisticalDataHeader>();

	protected void onGetTimeAndDate(SALTimeDate timeDate) {
		mTimeDate = TimeDate.buildTimeDate(this, timeDate);

		boolean autoSyncTime = mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME);
		if (mIsUpdateTimeDate) {
			mIsUpdateTimeDate = false;

			int dateFormat = mLifeTrakApplication.getTimeDate().getDateFormat();
			int hourFormat = mLifeTrakApplication.getTimeDate().getHourFormat();
			int displaySize = mLifeTrakApplication.getTimeDate().getDisplaySize();

			mTimeDate.setDateFormat(dateFormat);
			mTimeDate.setHourFormat(hourFormat);
			mTimeDate.setDisplaySize(displaySize);

			mLifeTrakApplication.setTimeDate(mTimeDate);
			return;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		int year = calendar.get(Calendar.YEAR) - 1900;
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);

		int status = SALStatus.NO_ERROR;

		mDataHeaderIndexes.clear();
		mDataHeaders.clear();

		timeDate.set(seconds, minute, hour, day, month, year);

		if (autoSyncTime) {
			status = mSalutronService.updateTimeAndDate(timeDate);
		}

		if (status == SALStatus.NO_ERROR) {
			mTimeoutHandler.postDelayed(new Runnable() {
				public void run() {
					int status = mSalutronService.getStatisticalDataHeaders();
					FlurryAgent.logEvent(DEVICE_START_SYNC, true);
					FlurryAgent.endTimedEvent(DEVICE_READY);

					if (status != SALStatus.NO_ERROR) {
						status = mSalutronService.getStepGoal();
					}
				}
			}, HANDLER_DELAY);
		} else {
			mSalutronService.getStepGoal();
		}
	}

	protected void onGetStatisticalDataHeaders(List<SALStatisticalDataHeader> statisticalDataHeaders) {
		int index = 0;

		for (SALStatisticalDataHeader salDataHeader : statisticalDataHeaders) {



			StatisticalDataHeader dataHeader = StatisticalDataHeader.buildStatisticalDataHeader(this, salDataHeader);
			dataHeader.setContext(this);
			dataHeader.setWatch(mLifeTrakApplication.getSelectedWatch());

			int day = dataHeader.getDateStampDay();
			int month = dataHeader.getDateStampMonth();
			int year = dataHeader.getDateStampYear();

			List<StatisticalDataHeader> dataHeaders = DataSource
					.getInstance(this)
					.getReadOperation()
					.query("watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()),
							String.valueOf(day), String.valueOf(month), String.valueOf(year)).getResults(StatisticalDataHeader.class);

			if (dataHeaders.size() > 0) {
				LifeTrakLogger.info("C300 C410 - " + String.format("data header date: %s, steps: %d, distance: %f, calories:%f ", "" + month + "-" + day + "-" + year, salDataHeader.totalSteps, salDataHeader.totalDistance, salDataHeader.totalCalorie));

				StatisticalDataHeader firstObject = dataHeaders.get(0);
				firstObject.setContext(this);
				firstObject.setTotalSteps(dataHeader.getTotalSteps());
				firstObject.setTotalDistance(dataHeader.getTotalDistance());
				firstObject.setTotalCalorie(dataHeader.getTotalCalorie());
				firstObject.setTotalSleep(dataHeader.getTotalSleep());
				firstObject.setWatch(mLifeTrakApplication.getSelectedWatch());

				LifeTrakLogger.info("" + dataHeader.getTotalSteps());
				LifeTrakLogger.info(""+ dataHeader.getTotalDistance());
				LifeTrakLogger.info(""+ dataHeader.getTotalCalorie());
				LifeTrakLogger.info(""+ dataHeader.getTotalSleep());
				firstObject.update();

				String query = "select count(dataPoint._id) from StatisticalDataPoint dataPoint " + "inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " + "where watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?";

				Cursor cursor = DataSource.getInstance(this).getReadOperation()
						.rawQuery(query, String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()), String.valueOf(day), String.valueOf(month), String.valueOf(year));

				if (cursor.moveToFirst()) {
					int count = cursor.getInt(0);

					if (count < 144) {
						mDataHeaderIndexes.add(index);
						mDataHeaders.add(firstObject);
					}
				}
			} else {
				// dataHeader.setWatch(mLifeTrakApplication.getSelectedWatch());
				// dataHeader.insert();
				mDataHeaderIndexes.add(index);
				mDataHeaders.add(dataHeader);
			}

			index++;
		}

		int status = SALStatus.NO_ERROR;

		if (mDataHeaderIndexes.size() > 0) {
			int headerIndex = mDataHeaderIndexes.remove(0);
			mCurrentDataHeader = mDataHeaders.get(mDataHeaderIndex);
			mDataHeaderIndex++;
			status = mSalutronService.getDataPointsOfSelectedDateStamp(headerIndex);
		} else {
			status = mSalutronService.getStepGoal();
		}

		if (status != SALStatus.NO_ERROR) {
			if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R415)
				mSalutronService.disconnectFromDevice();
			mProgressDialog.dismiss();
			mAlertDialog.show();
		}
	}

	protected void onGetStatisticalDataPoint(List<SALStatisticalDataPoint> salStatisticalDataPoints) {
		/*
		 * if (mDataHeaderIndexes.size() > 0) {
		 * 
		 * } else { int status = mSalutronService.getStepGoal();
		 * 
		 * if (status != SALStatus.NO_ERROR) { mProgressDialog.dismiss();
		 * mAlertDialog.show(); } }
		 */

		String query = "select count(dataPoint._id) from StatisticalDataPoint dataPoint " + "inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " + "where watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?";

		mCurrentDataHeader.setWatch(mLifeTrakApplication.getSelectedWatch());
		int day = mCurrentDataHeader.getDateStampDay();
		int month = mCurrentDataHeader.getDateStampMonth();
		int year = mCurrentDataHeader.getDateStampYear();

		Cursor cursor = DataSource.getInstance(this).getReadOperation()
				.rawQuery(query, String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()), String.valueOf(day), String.valueOf(month), String.valueOf(year));

		if (cursor.moveToFirst()) {
			int recordCount = cursor.getInt(0);

			if (recordCount < 144) {
				int index = 0;

				for (SALStatisticalDataPoint salDataPoint : salStatisticalDataPoints) {

					LifeTrakLogger.info(String.format("averageHR:%s, steps:%s, distance:%s, calories:%s", String.valueOf(salDataPoint.averageHR),
							String.valueOf(salDataPoint.steps), String.valueOf(salDataPoint.distance), String.valueOf(salDataPoint.calorie)));
					if (index > recordCount - 1) {
						StatisticalDataPoint dataPoint = StatisticalDataPoint.buildStatisticalDataPoint(this, salDataPoint);

						dataPoint.setContext(this);
						dataPoint.setDataPointId(index + 1);
						dataPoint.setStatisticalDataHeader(mCurrentDataHeader);

						mDataPoints.add(dataPoint);
					} else if (index == recordCount - 1) {
						List<StatisticalDataPoint> dataPoints = DataSource.getInstance(MainActivity.this).getReadOperation()
								.query("dataHeaderAndPoint = ?", String.valueOf(mCurrentDataHeader.getId())).getResults(StatisticalDataPoint.class);
						StatisticalDataPoint dataPoint = dataPoints.get(dataPoints.size() - 1);

						if    (dataPoint.getDataPointId() == 0) {
							dataPoint.setDataPointId(index + 1);
						}

						dataPoint.setContext(MainActivity.this);
						dataPoint.setStatisticalDataHeader(mCurrentDataHeader);
						dataPoint.setAverageHR(salDataPoint.averageHR);
						dataPoint.setDistance(salDataPoint.distance);
						dataPoint.setSteps(salDataPoint.steps);
						dataPoint.setCalorie(salDataPoint.calorie);
						dataPoint.setSleepPoint02(salDataPoint.sleepPoint_0_2);
						dataPoint.setSleepPoint24(salDataPoint.sleepPoint_2_4);
						dataPoint.setSleepPoint46(salDataPoint.sleepPoint_4_6);
						dataPoint.setSleepPoint68(salDataPoint.sleepPoint_6_8);
						dataPoint.setSleepPoint810(salDataPoint.sleepPoint_8_10);
						dataPoint.setDominantAxis(salDataPoint.dominantAxis);
						dataPoint.setLux(salDataPoint.lux);
						dataPoint.setAxisDirection(salDataPoint.axisDirection);
						dataPoint.setAxisMagnitude(salDataPoint.axisMagnitude);
						// dataPoint.update();
						mDataPoints.add(dataPoint);
					}

					index++;
				}
			}
		}

		if (mDataHeaderIndexes.size() > 0) {
			int headerIndex = mDataHeaderIndexes.remove(0);
			// mCurrentDataHeader = mDataHeaders.remove(0);
			mCurrentDataHeader = mDataHeaders.get(mDataHeaderIndex);

			mDataHeaderIndex++;

			int status = mSalutronService.getDataPointsOfSelectedDateStamp(headerIndex);

			if (status != SALStatus.NO_ERROR) {
				status = mSalutronService.getStepGoal();

				if (status != SALStatus.NO_ERROR) {
					mProgressDialog.dismiss();
					mAlertDialog.show();
				}
			}
		} else {
			mDataHeaderIndex = 0;
			int status = mSalutronService.getStepGoal();

			if (status != SALStatus.NO_ERROR) {
				mProgressDialog.dismiss();
				mAlertDialog.show();
			}
		}
	}

	protected void onGetStepGoal(long stepGoal) {
		/*
		 * Calendar calendar = Calendar.getInstance(); calendar.setTime(new
		 * Date());
		 * 
		 * int day = calendar.get(Calendar.DAY_OF_MONTH); int month =
		 * calendar.get(Calendar.MONTH) + 1; int year =
		 * calendar.get(Calendar.YEAR) - 1900;
		 * 
		 * List<Goal> goals = DataSource .getInstance(this) .getReadOperation()
		 * .query(
		 * "watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?"
		 * , String.valueOf(mLifeTrakApplication.getSelectedWatch() .getId()),
		 * String.valueOf(day), String.valueOf(month), String.valueOf(year))
		 * .getResults(Goal.class);
		 * 
		 * if (goals.size() > 0) { Goal goal = goals.get(0);
		 * goal.setContext(this); goal.setStepGoal(stepGoal);
		 * goal.setWatch(mLifeTrakApplication.getSelectedWatch());
		 * goal.update(); } else { Goal goal = new Goal(this);
		 * goal.setStepGoal(stepGoal); goal.setDate(calendar.getTime());
		 * goal.setDateStampDay(day); goal.setDateStampMonth(month);
		 * goal.setDateStampYear(year);
		 * goal.setWatch(mLifeTrakApplication.getSelectedWatch());
		 * goal.insert(); }
		 */

		mStepGoal = stepGoal;
		int status = mSalutronService.getDistanceGoal();

		if (status != SALStatus.NO_ERROR) {
			mProgressDialog.dismiss();
			mAlertDialog.show();
		}
	}

	protected void onGetDistanceGoal(double distanceGoal) {
		mDistanceGoal = distanceGoal;

		if (mDistanceGoal >= 100.0) {
			mDistanceGoal = mDistanceGoal / 100.0;
		}

		int status = mSalutronService.getCalorieGoal();

		if (status != SALStatus.NO_ERROR) {
			mProgressDialog.dismiss();
			mAlertDialog.show();
		}

	}

	protected void onGetCalorieGoal(long calorieGoal) {
		mCalorieGoal = calorieGoal;

		int status = mSalutronService.getSleepSetting();

		switch (status) {
			case SALStatus.NO_ERROR:
				break;
			case SALStatus.ERROR_NOT_SUPPORTED:
				mSalutronService.getUserProfile();
				break;
			default:
				mProgressDialog.dismiss();
				mAlertDialog.show();
				break;
		}
	}

	protected void onGetSleepSetting(SALSleepSetting sleepSetting) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		List<SleepSetting> sleepSettings = DataSource.getInstance(this).getReadOperation().query("watchSleepSetting = ?", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
				.getResults(SleepSetting.class);

		if (sleepSettings.size() > 0) {
			SleepSetting sleep = sleepSettings.get(0);
			sleep.setSleepDetectType(sleepSetting.getSleepDetectType());
			sleep.setSleepGoalMinutes(sleepSetting.getSleepGoal());
			sleep.setWatch(mLifeTrakApplication.getSelectedWatch());
			// sleep.update();
			mSleepSetting = sleep;
		} else {
			SleepSetting sleep = new SleepSetting(this);
			sleep.setSleepDetectType(sleepSetting.getSleepDetectType());
			sleep.setSleepGoalMinutes(sleepSetting.getSleepGoal());
			sleep.setWatch(mLifeTrakApplication.getSelectedWatch());
			mSleepSetting = sleep;
		}

		mCalibrationType = 0;

		List<CalibrationData> calibrationData = DataSource.getInstance(this).getReadOperation().query("watchCalibrationData = ?", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
				.getResults(CalibrationData.class);

		if (calibrationData.size() > 0) {
			mCalibrationData = calibrationData.get(0);
		} else {
			mCalibrationData = new CalibrationData(this);
		}

		int status = mSalutronService.getCalibrationData(SALCalibration.STEP_CALIBRATION);

		switch (status) {
			case SALStatus.NO_ERROR:
				break;
			case SALStatus.ERROR_NOT_SUPPORTED:
				mSalutronService.getUserProfile();
				break;
			default:
				mProgressDialog.dismiss();
				mAlertDialog.show();
				break;
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
			status = mSalutronService.getWorkoutDatabase();
		}

		if (status != SALStatus.NO_ERROR) {
			mProgressDialog.dismiss();
			mAlertDialog.show();
		}
	}

	protected void onGetWorkoutDatabase(List<SALWorkoutInfo> workoutInfos) {
		for (SALWorkoutInfo salWorkoutInfo : workoutInfos) {
			String query = "watchWorkoutInfo = ? and timeStampHour = ? and timeStampMinute = ? and timeStampSecond = ? and " + "dateStampYear = ? and dateStampMonth = ? and dateStampDay = ?";
			int timeStampHour = salWorkoutInfo.timestamp.nHour;
			int timeStampMinute = salWorkoutInfo.timestamp.nMinute;
			int timeStampSecond = salWorkoutInfo.timestamp.nSecond;
			int dateStampYear = salWorkoutInfo.datestamp.nYear;
			int dateStampMonth = salWorkoutInfo.datestamp.nMonth;
			int dateStampDay = salWorkoutInfo.datestamp.nDay;

			List<WorkoutInfo> workoutInfos2 = DataSource
					.getInstance(this)
					.getReadOperation()
					.query(query, String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()), String.valueOf(timeStampHour), String.valueOf(timeStampMinute), String.valueOf(timeStampSecond),
							String.valueOf(dateStampYear), String.valueOf(dateStampMonth), String.valueOf(dateStampDay)).getResults(WorkoutInfo.class);

			if (workoutInfos2.size() == 0) {
				WorkoutInfo workoutInfo = WorkoutInfo.buildWorkoutInfo(this, salWorkoutInfo);
				workoutInfo.setWatch(mLifeTrakApplication.getSelectedWatch());
				workoutInfo.setSyncedToCloud(false);
				// workoutInfo.insert();

				mWorkoutInfos.add(workoutInfo);
			} else {
				WorkoutInfo workoutInfo = workoutInfos2.get(0);

				workoutInfo.setFlags(salWorkoutInfo.flags);
				workoutInfo.setTimeStampHour(salWorkoutInfo.timestamp.nHour);
				workoutInfo.setTimeStampMinute(salWorkoutInfo.timestamp.nMinute);
				workoutInfo.setTimeStampSecond(salWorkoutInfo.timestamp.nSecond);
				workoutInfo.setDateStampYear(salWorkoutInfo.datestamp.nYear);
				workoutInfo.setDateStampMonth(salWorkoutInfo.datestamp.nMonth);
				workoutInfo.setDateStampDay(salWorkoutInfo.datestamp.nDay);

				Calendar calendar = new GregorianCalendar(workoutInfo.getDateStampYear(), workoutInfo.getDateStampMonth(), workoutInfo.getDateStampDay());
				workoutInfo.setDateStamp(calendar.getTime());
				workoutInfo.setHundredths(salWorkoutInfo.hundredths);
				workoutInfo.setSecond(salWorkoutInfo.second);
				workoutInfo.setMinute(salWorkoutInfo.minute);
				workoutInfo.setHour(salWorkoutInfo.hour);
				workoutInfo.setDistance(salWorkoutInfo.distance);
				workoutInfo.setCalories(salWorkoutInfo.calories);
				workoutInfo.setSteps(salWorkoutInfo.steps);
				workoutInfo.setWatch(mLifeTrakApplication.getSelectedWatch());
				workoutInfo.setSyncedToCloud(workoutInfo.isSyncedToCloud());

				mWorkoutInfos.add(workoutInfo);
			}
		}

		int status = mSalutronService.getSleepDatabase();

		if (status != SALStatus.NO_ERROR) {
			mProgressDialog.dismiss();
			mAlertDialog.show();
		}
	}

	protected void onGetSleepDatabase(List<SALSleepDatabase> sleepDatabases) {
		for (SALSleepDatabase salSleepDatabase : sleepDatabases) {

			SleepDatabase sleepDatabase0 = SleepDatabase.buildSleepDatabase(this, salSleepDatabase);

			if (hasOverlapOrDeletedSleepDatabase(sleepDatabase0))
				continue;

			String query = "watchSleepDatabase = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ? and hourSleepStart = ?";
			int dateStampYear = salSleepDatabase.datestamp.nYear;
			int dateStampMonth = salSleepDatabase.datestamp.nMonth;
			int dateStampDay = salSleepDatabase.datestamp.nDay;
			int minuteSleepStart = salSleepDatabase.minuteSleepStart;
			int hourSleepStart = salSleepDatabase.hourSleepStart;
			int minuteSleepEnd = salSleepDatabase.minuteSleepEnd;
			int hourSleepEnd = salSleepDatabase.hourSleepEnd;

			List<SleepDatabase> sleepDatabases2 = DataSource.getInstance(this)
					.getReadOperation()
					.query(query, String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()),
							String.valueOf(dateStampYear), String.valueOf(dateStampMonth), String.valueOf(dateStampDay),String.valueOf(hourSleepStart))
					.getResults(SleepDatabase.class);

			if (sleepDatabases2.size() == 0) {
				SleepDatabase sleepDatabase = SleepDatabase.buildSleepDatabase(this, salSleepDatabase);
				sleepDatabase.setWatch(mLifeTrakApplication.getSelectedWatch());
				// sleepDatabase.insert();
				mSleepDatabases.add(sleepDatabase);
			} else {
				SleepDatabase sleepDatabase = sleepDatabases2.get(0);

				if (!(sleepDatabase.getDateStampYear() == dateStampYear && sleepDatabase.getDateStampMonth() == dateStampMonth && sleepDatabase.getDateStampDay() == dateStampDay && sleepDatabase
						.getMinuteSleepStart() == minuteSleepStart && sleepDatabase.getHourSleepStart() == hourSleepStart && sleepDatabase.getMinuteSleepEnd() == minuteSleepEnd && sleepDatabase
						.getHourSleepEnd() == hourSleepEnd)) {
					sleepDatabase = SleepDatabase.buildSleepDatabase(this, salSleepDatabase);
					sleepDatabase.setWatch(mLifeTrakApplication.getSelectedWatch());
					mSleepDatabases.add(sleepDatabase);
				}

				// sleepDatabase.update();
			}
		}

		List<SleepDatabaseDeleted> sleepDatabaseDeleted = DataSource.getInstance(this).getReadOperation().getResults(SleepDatabaseDeleted.class);
		for (SleepDatabase sleepDatabase : mSleepDatabases) {
			for (SleepDatabaseDeleted deleted : sleepDatabaseDeleted) {
				if (sleepDatabase.getDateStampDay() == deleted.getDateStampDay() && sleepDatabase.getDateStampMonth() == deleted.getDateStampMonth() && sleepDatabase.getDateStampYear() == deleted
						.getDateStampYear() && sleepDatabase.getHourSleepStart() == deleted.getHourSleepStart() && sleepDatabase.getMinuteSleepStart() == deleted.getMinuteSleepStart() && sleepDatabase
						.getHourSleepEnd() == deleted.getHourSleepEnd() && sleepDatabase.getMinuteSleepEnd() == deleted.getMinuteSleepEnd()) {
					mSleepDatabases.remove(sleepDatabase);
				}
			}
		}

		int status = mSalutronService.getUserProfile();

		if (status != SALStatus.NO_ERROR) {
			mProgressDialog.dismiss();
			mAlertDialog.show();
		}
	}

	protected void onGetUserProfile(SALUserProfile salUserProfile) {
		mUserProfile = UserProfile.buildUserProfile(this, salUserProfile);
		this.salUserProfile = salUserProfile;
		int delay = 750;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				LifeTrakLogger.info("getSoftwareVersion on C300/C410");
				int status = mSalutronService.getFirmwareRevision();

				if (status != SALStatus.NO_ERROR) {
					mProgressDialog.dismiss();
					mAlertDialog.show();
				}
			}
		}, delay * 2);


	}

	protected void onGetFirmware(String firmwareVersion) {
		Pattern pattern = Pattern.compile("[^0-9]");
		Matcher matcher = pattern.matcher(firmwareVersion);
		String number = matcher.replaceAll("");

		mPreferenceWrapper.setPreferenceStringValue(FIRMWAREVERSION, "V" + number).synchronize();
		int delay = 750;

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				LifeTrakLogger.info("getSoftwareVersion on C300/C410");
				int status = mSalutronService.getSoftwareRevision();

				if (status != SALStatus.NO_ERROR) {
					mProgressDialog.dismiss();
					mAlertDialog.show();
				}
			}
		}, delay * 2);


	}

	protected void onGetSoftwareRevision(String firmwareVersion) {
		Pattern pattern = Pattern.compile("[^0-9]");
		Matcher matcher = pattern.matcher(firmwareVersion);
		String number = matcher.replaceAll("");
		mPreferenceWrapper.setPreferenceStringValue(SOFTWAREVERSION, "V" + number).synchronize();


		c300c410SyncFinish();

	}


	private void c300c410SyncFinish(){
		Watch watch = mLifeTrakApplication.getSelectedWatch();
		watch.setLastSyncDate(new Date());

		try{
			String firmwareVer = mPreferenceWrapper.getPreferenceStringValue(FIRMWAREVERSION);
			if(firmwareVer != null)
				watch.setWatchFirmWare(firmwareVer);
		}
		catch (Exception e){
			LifeTrakLogger.info("Erro e" + e.getLocalizedMessage());
		}
		try{
			String softwareVer = mPreferenceWrapper.getPreferenceStringValue(SOFTWAREVERSION);
			if(softwareVer != null)
				watch.setWatchSoftWare(softwareVer);
		}
		catch (Exception e){
			LifeTrakLogger.info("Erro e" + e.getLocalizedMessage());
		}

		watch.update();
		mLifeTrakApplication.setSelectedWatch(watch);

		mTempDataHeaders.clear();
		mTempDataHeaders.addAll(mDataHeaders);

		for (StatisticalDataHeader dataHeader : mDataHeaders) {
			if (dataHeader.getId() == 0) {
				dataHeader.insert();
			} else {
				dataHeader.update();
			}
		}

		for (StatisticalDataPoint dataPoint : mDataPoints) {
			if (dataPoint.getId() == 0) {
				dataPoint.insert();
			} else {
				dataPoint.update();
			}
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		List<Goal> goals = DataSource.getInstance(this)
				.getReadOperation()
				.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()), String.valueOf(day),
						String.valueOf(month), String.valueOf(year)).getResults(Goal.class);
		Goal goal = null;

		long stepGoal = 0;
		double distanceGoal = 0d;
		long calorieGoal = 0;
		int sleepGoal = 0;

		if (goals.size() > 0) {
			goal = goals.get(0);

			stepGoal = goal.getStepGoal();
			distanceGoal = goal.getDistanceGoal();
			calorieGoal = goal.getCalorieGoal();

			if (mSleepSetting != null) {
				sleepGoal = mSleepSetting.getSleepGoalMinutes();
			}

			goal.setStepGoal(mStepGoal);
			goal.setDistanceGoal(mDistanceGoal);
			goal.setCalorieGoal(mCalorieGoal);

			if (mSleepSetting != null) {
				goal.setSleepGoal(mSleepSetting.getSleepGoalMinutes());
			}
			//			if (mPreferenceWrapper.getPreferenceBooleanValue(DO_NOT_SHOW_PROMPT_DIALOG))
			//				goal.update();
		} else {
			goal = new Goal(this);
			goal.setStepGoal(mStepGoal);

			if (mDistanceGoal >= 100)
				mDistanceGoal = mDistanceGoal / 100;

			goal.setDistanceGoal(mDistanceGoal);
			goal.setCalorieGoal(mCalorieGoal);

			if (mSleepSetting != null)
				goal.setSleepGoal(mSleepSetting.getSleepGoalMinutes());

			goal.setDateStampDay(day);
			goal.setDateStampMonth(month);
			goal.setDateStampYear(year);
			goal.setWatch(mLifeTrakApplication.getSelectedWatch());
			goal.setDate(calendar.getTime());

			//			if (mPreferenceWrapper.getPreferenceBooleanValue(DO_NOT_SHOW_PROMPT_DIALOG))
			//				goal.insert();
		}

		mGoal = goal;

		/*if (mCalibrationData != null) {
			if (mCalibrationData.getId() == 0) {
				mCalibrationData.insert();
			} else {
				mCalibrationData.update();
			}
		}*/

		if (mSleepSetting != null) {
			if (mSleepSetting.getId() == 0) {
				mSleepSetting.insert();
			} else {
				mSleepSetting.update();
			}
		}

		for (WorkoutInfo workoutInfo : mWorkoutInfos) {
			if (workoutInfo.getId() == 0) {
				workoutInfo.setSyncedToCloud(false);
				workoutInfo.insert();
			} else {
				workoutInfo.update();
			}
		}

		for (SleepDatabase sleepDatabase : mSleepDatabases) {
			if (sleepDatabase.getId() == 0) {
				sleepDatabase.insert();
			} else {
				sleepDatabase.update();
			}
		}

		if (mLifeTrakApplication.getSelectedWatch() == null) {
			return;
		}
		List<UserProfile> userProfiles = DataSource.getInstance(this).getReadOperation().query("watchUserProfile = ?", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
				.getResults(UserProfile.class);

		if (userProfiles == null ||userProfiles.size() > 0) {
			UserProfile userProfile = userProfiles.get(0);

			TimeDate currentTimeDate = mLifeTrakApplication.getTimeDate();

			List<CalibrationData> calibrationData = DataSource.getInstance(this)
					.getReadOperation().query("watchCalibrationData = ?", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
					.getResults(CalibrationData.class);

			boolean isCalibrationMatched = false;
			CalibrationData calibData = null;

			if (calibrationData.size() > 0 && watch.getModel() != WATCHMODEL_C300 && watch.getModel() != WATCHMODEL_C300_IOS) {
				calibData = calibrationData.get(0);
				isCalibrationMatched = mCalibrationData.getAutoEL() == calibData.getAutoEL() &&
						mCalibrationData.getStepCalibration() == calibData.getStepCalibration() &&
						mCalibrationData.getDistanceCalibrationWalk() == calibData.getDistanceCalibrationWalk() &&
						mCalibrationData.getDistanceCalibrationRun() == calibData.getDistanceCalibrationRun();

				//				if (mPreferenceWrapper.getPreferenceBooleanValue(DO_NOT_SHOW_PROMPT_DIALOG)){
				//					calibData.setAutoEL(mCalibrationData.getAutoEL());
				//					calibData.setStepCalibration(mCalibrationData.getStepCalibration());
				//					calibData.setDistanceCalibrationWalk(mCalibrationData.getDistanceCalibrationWalk());
				//					calibData.setDistanceCalibrationRun(calibData.getDistanceCalibrationRun());
				//					calibData.update();
				//				}
			}
			else{
				isCalibrationMatched = true;
			}

			boolean isGoalMatched = false;

			if (goal != null) {
				BigDecimal bigDecimal = new BigDecimal(distanceGoal);
				double distanceToCompare = bigDecimal.setScale(2, RoundingMode.HALF_UP).doubleValue();

				if (distanceToCompare != mDistanceGoal) {
					bigDecimal = new BigDecimal(distanceGoal);
					distanceToCompare = bigDecimal.setScale(2, RoundingMode.DOWN).doubleValue();
				}

				distanceGoal = distanceToCompare;

				isGoalMatched = stepGoal == mStepGoal && distanceGoal == mDistanceGoal &&
						calorieGoal == mCalorieGoal && sleepGoal == mGoal.getSleepGoal();
			}
			counter = 0;
			if (userProfile.getWeight() == salUserProfile.getWeight() && userProfile.getHeight() == salUserProfile.getHeight() && userProfile.getBirthDay() == salUserProfile.getBirthDay() && userProfile
					.getBirthMonth() == salUserProfile.getBirthMonth() && userProfile.getBirthYear() == salUserProfile.getBirthYear() && userProfile.getGender() == salUserProfile.getGender() && userProfile
					.getUnitSystem() == salUserProfile.getUnitSystem() && mTimeDate.getDateFormat() == currentTimeDate.getDateFormat() && mTimeDate.getHourFormat() == currentTimeDate.getHourFormat() &&
					isCalibrationMatched && isGoalMatched) {

				if (mContentFragment instanceof DashboardFragment) {
					mLifeTrakApplication.setUserProfile(userProfile);
					userProfile.setWeight(salUserProfile.getWeight());
					userProfile.setHeight(salUserProfile.getHeight());
					userProfile.setBirthDay(salUserProfile.getBirthDay());
					userProfile.setBirthMonth(salUserProfile.getBirthMonth());
					userProfile.setBirthYear(salUserProfile.getBirthYear());
					userProfile.setSensitivity(salUserProfile.getSensitivityLevel());
					userProfile.setGender(salUserProfile.getGender());
					userProfile.setUnitSystem(salUserProfile.getUnitSystem());
					userProfile.setWatch(mLifeTrakApplication.getSelectedWatch());
					userProfile.update();

					Date date = new Date();
					mLifeTrakApplication.setCurrentDate(date);
					setCalendarDate(date);
					DashboardFragment fragment = (DashboardFragment) mContentFragment;
					fragment.initializeObjects();
				} else if (mContentFragment instanceof MyAccountFragment) {
					MyAccountFragment myAccountFragment = (MyAccountFragment) mContentFragment;
					myAccountFragment.updateSyncDates();

					UserProfile profile = mLifeTrakApplication.getUserProfile();

					salUserProfile.setWeight(profile.getWeight());
					salUserProfile.setHeight(profile.getHeight());
					salUserProfile.setBirthYear(profile.getBirthYear() - 1900);
					salUserProfile.setBirthMonth(profile.getBirthMonth());
					salUserProfile.setBirthDay(profile.getBirthDay());
					salUserProfile.setGender(profile.getGender());
					salUserProfile.setUnitSystem(profile.getUnitSystem());

					int status = mSalutronService.updateUserProfile(salUserProfile);

					switch (status) {
						case SALStatus.NO_ERROR:
							LifeTrakLogger.info("status is no error");
							break;
						default:
							LifeTrakLogger.info("unable to update user profile");
							break;
					}
				}

				LifeTrakLogger.info("Sync End from Watch - " + new Date());

				if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC)) {
					if (mProgressDialog == null)
						reinitializeProgress();
					if (!mProgressDialog.isShowing())
						mProgressDialog.show();
					mProgressDialog.setMessage(getString(R.string.sync_to_cloud));

					Date expirationDate = getExpirationDate();
					Date now = new Date();

					if (now.after(expirationDate)) {
						mCurrentOperation = OPERATION_REFRESH_TOKEN;
						refreshToken();
					} else {
						mCurrentOperation = OPERATION_CHECK_SERVERTIME;
						startCheckingServer();
						//startSyncToServer();
					}
				} else {
					if (mProgressDialog == null)
						reinitializeProgress();

					mProgressDialog.dismiss();
					if(isFinishing()) {
						AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setCancelable(false)
								.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										arg0.dismiss();
									}
								}).create();
						alert.show();
					}
					else{
						Intent intent = new Intent(getApplicationContext(), DialogActivitySyncSuccess.class);
						startActivity(intent);
					}
				}

				mTimeoutHandler.postDelayed(new Runnable() {
					public void run() {
						if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R415)
							mSalutronService.disconnectFromDevice();
					}
				}, 500);


			} else {
				if (mContentFragment instanceof DashboardFragment || mContentFragment instanceof MyAccountFragment) {
					UserProfile salProfile = new UserProfile();
					salProfile.setWeight(salUserProfile.getWeight());
					salProfile.setHeight(salUserProfile.getHeight());
					salProfile.setBirthDay(salUserProfile.getBirthDay());
					salProfile.setBirthMonth(salUserProfile.getBirthMonth());
					salProfile.setBirthYear(salUserProfile.getBirthYear());
					salProfile.setSensitivity(salUserProfile.getSensitivityLevel());
					salProfile.setGender(salUserProfile.getGender());
					salProfile.setUnitSystem(salUserProfile.getUnitSystem());

					Intent intent = new Intent(MainActivity.this, ProfileSelectActivity.class);
					intent.putExtra(USER_PROFILE, userProfile);
					intent.putExtra(SAL_USER_PROFILE, salProfile);
					intent.putExtra(TIME_DATE, mTimeDate);
					intent.putExtra(CALIBRATION_DATA_FROM_WATCH, mCalibrationData);
					intent.putExtra(GOAL_FROM_WATCH, mGoal);

					if (mPreferenceWrapper.getPreferenceBooleanValue(DO_NOT_SHOW_PROMPT_DIALOG)) {

						useWatchSettings();



						mTimeoutHandler.postDelayed(new Runnable() {
							public void run() {
								if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R415)
									mSalutronService.disconnectFromDevice();
								if (mContentFragment instanceof MyAccountFragment){
									((MyAccountFragment) mContentFragment).initializeObjects();
								}
							}
						}, 500);

						if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC)) {
							if (mProgressDialog == null)
								reinitializeProgress();
							if (!mProgressDialog.isShowing())
								mProgressDialog.show();

							mProgressDialog.setMessage(getString(R.string.sync_to_cloud));

							Date expirationDate = getExpirationDate();
							Date now = new Date();

							if (now.after(expirationDate)) {
								mCurrentOperation = OPERATION_REFRESH_TOKEN;
								refreshToken();
							} else {
								mCurrentOperation = OPERATION_CHECK_SERVERTIME;
								startCheckingServer();
								//startSyncToServer();
							}
						} else {
							if (mProgressDialog == null)
								reinitializeProgress();

							mProgressDialog.dismiss();

							if (mContentFragment instanceof DashboardFragment) {
								Date date = new Date();
								mLifeTrakApplication.setCurrentDate(date);
								setCalendarDate(date);
								DashboardFragment fragment = (DashboardFragment) mContentFragment;
								fragment.initializeObjects();
							}

							if(isFinishing()) {
								AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setCancelable(false)
										.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface arg0, int arg1) {
												arg0.dismiss();
											}
										}).create();
								alert.show();
							}
							else{
								intent = new Intent(getApplicationContext(), DialogActivitySyncSuccess.class);
								startActivity(intent);
							}

						}
					} else {
						startActivityForResult(intent, REQUEST_CODE_PROFILE_SELECT);
					}

				}
			}
		}

		// All new data are now stored
//		if (mPreferenceWrapper.getPreferenceBooleanValue(GOOGLE_FIT_ENABLED)) {
//			GoogleFitSyncService.start(this, watch);
//		}

		mDataHeaders.clear();
		mDataPoints.clear();
		mWorkoutInfos.clear();
		mSleepDatabases.clear();

		if (mMenuFragment instanceof MenuFragment) {
			updateLastSyncDate();
		}

		mSyncSuccess = true;
	}

	public void updateLastSyncDate() {
		MenuFragment menuFragment = (MenuFragment) mMenuFragment;
		try {
			menuFragment.updateLastSyncDate();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void onPairTimeout() {

		if (!mConnectionFailedView.isShown()){
			mConnectionFailedView.show();
			flag_disable_menu = true;
		}
	}

	private static final int REQ_BT_ENABLE = BluetoothListener.REQ_BT_ENABLE;
	private final KillReceiver finisher = new KillReceiver();
	private BluetoothListener bluetoothListener;

	private final class KillReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	}

	private void requestBluetoothOn() {
		final BluetoothAdapter BTadapter = BluetoothAdapter.getDefaultAdapter();

		if (!BTadapter.isEnabled()) {
			final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQ_BT_ENABLE);
		}
	}

	private float dpToPx(float dp) {
		return dp * (getResources().getDisplayMetrics().densityDpi / 160.0f);
	}

	public void arrangeDashboardItems() {
		if (mContentFragment instanceof DashboardFragment) {
			DashboardFragment fragment = (DashboardFragment) mContentFragment;
			fragment.initializeObjects();
		}
	}

	private final SlidingDrawer.OnDrawerOpenListener mDrawerOpenListener = new SlidingDrawer.OnDrawerOpenListener() {
		@Override
		public void onDrawerOpened() {
			if (mContentFragment instanceof DashboardFragment) {
				DashboardFragment fragment = (DashboardFragment) mContentFragment;
				fragment.disableDashboard();
				findViewById(R.id.frmModalView).setVisibility(View.VISIBLE);
				mCalendar.scrollToCurrentCalendar(mLifeTrakApplication.getCurrentDate());
			}
		}
	};

	private final SlidingDrawer.OnDrawerCloseListener mDrawerCloseListener = new SlidingDrawer.OnDrawerCloseListener() {

		@Override
		public void onDrawerClosed() {
			if (mContentFragment instanceof DashboardFragment) {
				DashboardFragment fragment = (DashboardFragment) mContentFragment;
				fragment.enableDashboard();
				findViewById(R.id.frmModalView).setVisibility(View.GONE);
			}
		}
	};

	private boolean isBluetoothEnabled() {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		return adapter.isEnabled();
	}


	private void startBluetoothRequest(int requestCode) {
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, requestCode);
	}

	public void syncWatch() {
		mCancelled = false;
		mOperation = OPERATION_SYNC_WATCH;
		mDataHeaders.clear();
		mDataPoints.clear();
		mWorkoutInfos.clear();
		mSleepDatabases.clear();
		mDataHeaderIndex = 0;

		if (mConnectionFailedView.isShown()){
			mConnectionFailedView.hide();
			flag_disable_menu = false;
		}

		if (isBluetoothEnabled()) {

			if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
				flag_finished_syncing_r450 = false;
				final Watch watch = mLifeTrakApplication.getSelectedWatch();
				BluetoothDevice bluetoothDevice = mLifeTrakSyncR450.getConnectedDevice();
				mLifeTrakSyncR450.registerHandler();
				if (bluetoothDevice == null) {
					Intent intent = new Intent(this, PairDeviceAutoActivity.class);
					intent.putExtra(SYNC_TYPE, SYNC_TYPE_DASHBOARD);
					intent.putExtra(SELECTED_WATCH_MODEL, mLifeTrakApplication.getSelectedWatch().getModel());
					startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE_SYNC);
				} else {
					mLifeTrakSyncR450.stopScan();
					if (mProgressDialog == null)
						reinitializeProgress();
					try{
						mProgressDialog.setMessage(getString(R.string.searching_device, stringNameForModel(mLifeTrakApplication.getSelectedWatch().getModel())));
						if (!mProgressDialog.isShowing())
							mProgressDialog.show();

						if (mConnectionFailedView.isShown()){
							mConnectionFailedView.hide();
							flag_disable_menu = false;
						}
					}catch (Exception e){

					}

					flag_sync = true;
					flag_sync_no_bluetooth = true;
					mTimeoutHandler.postDelayed(watchSyncRunnable, 15000);
				}
				mWatchConnected = false;
				mPreferenceWrapper.setPreferenceBooleanValue(IS_WATCH_CONNECTED, false).synchronize();
			}
			else if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420){
//				if (mLifeTrakSyncR450 != null){
//					mLifeTrakSyncR450.disconnectR450();
//					LifeTrakLogger.info("Disconnect to R450 watch");
//				}
				final int watchModel = mLifeTrakApplication.getSelectedWatch().getModel();
				Intent intent = new Intent(this, PairDeviceAutoActivity.class);
				intent.putExtra(SELECTED_WATCH_MODEL, mLifeTrakApplication.getSelectedWatch().getModel());
				intent.putExtra(SYNC_TYPE, SYNC_TYPE_DASHBOARD);
				startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE_SYNC);
			}
			else {
//				if (mLifeTrakSyncR450 != null){
//					mLifeTrakSyncR450.disconnectR450();
//					LifeTrakLogger.info("Disconnect to R450 watch");
//				}
				final int watchModel = mLifeTrakApplication.getSelectedWatch().getModel();
				Intent intent = new Intent(this, PairDeviceAutoActivity.class);
				intent.putExtra(SELECTED_WATCH_MODEL, watchModel);
				intent.putExtra(SYNC_TYPE, SYNC_TYPE_DASHBOARD);
				startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE_SYNC);
			}
		} else {
			startBluetoothRequest(REQUEST_CODE_ENABLE_BLUETOOTH);
		}
	}



	Runnable watchSyncRunnable = new Runnable() {
		@Override
		public void run() {
			if (mConnectionFailedView.isShown()) {
				mConnectionFailedView.hide();
				flag_disable_menu = false;
			}
			Watch watch = mLifeTrakApplication.getSelectedWatch();
			try {
				if (mLifeTrakApplication.getSelectedWatch() != null && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
					if (mLifeTrakSyncR450 != null) {
						if (flag_sync_no_bluetooth)
							mLifeTrakSyncR450.startSync();
					}
				} else if (mLifeTrakApplication.getSelectedWatch() != null && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
					if (mLifeTrakSyncR420 != null) {
						if (flag_sync_no_bluetooth)
							mLifeTrakSyncR420.startSync();
					}
				} else {
					startSync();
				}
			} catch (NullPointerException e) {

				if (mLifeTrakApplication.getSelectedWatch() != null && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
					mOperation = OPERATION_SYNC_WATCH;
					mLifeTrakSyncR450.connectToDevice(watch.getMacAddress(), WATCHMODEL_R415);

				} else if (mLifeTrakApplication.getSelectedWatch() != null && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
					mOperation = OPERATION_SYNC_WATCH;
					mLifeTrakSyncR420.connectToDevice(watch.getMacAddress(), WATCHMODEL_R415);
				}

			}
		}
	};

	Runnable watchSyncRunnable2 = new Runnable() {
		@Override
		public void run() {
			try {
				if (mLifeTrakApplication.getSelectedWatch() != null && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
					if (mLifeTrakSyncR450 != null){
						if (flag_sync_no_bluetooth)
							mLifeTrakSyncR450.startSync();
					}
				}
				else if (mLifeTrakApplication.getSelectedWatch() != null && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420){
					if (mLifeTrakSyncR420 != null){
						if (flag_sync_no_bluetooth)
							mLifeTrakSyncR420.startSync();
					}
				}
				else {
					startSync();
				}
			} catch (NullPointerException e) {
			}
		}
	};



	private void cancelSync() {
		mCancelled = true;
		if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
			mLifeTrakSyncR450.cancelSync();
			mLifeTrakSyncR450.registerHandler();
		}
		else if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420){
			if (mLifeTrakSyncR420!= null)
				mLifeTrakSyncR420.stopCallables();
		}

		mServerSyncAsync.cancel();
		if (mSyncThread != null && mSyncThread.isAlive())
			mSyncThread.interrupt();
	}

	private void refreshToken() {
		String url = API_URL + REFRESH_TOKEN_URI;
		String refreshToken = mPreferenceWrapper.getPreferenceStringValue(REFRESH_TOKEN);

		mServerSyncAsync.url(url).addParam("grant_type", "refresh_token").addParam("refresh_token", refreshToken).addParam("client_id", getString(R.string.client_id))
				.addParam("client_secret", getString(R.string.client_secret)).post();
	}

	public void startCheckingServerFromFragment(){
		mCurrentOperation = OPERATION_CHECK_SERVERTIME;
		startCheckingServer();
	}

	public void refreshTokenFromFragment(){
		mCurrentOperation = OPERATION_REFRESH_TOKEN;
		refreshToken();
	}

	private void startCheckingServer(){
		flag_finished_syncing_r450 = true;

		if (mContentFragment instanceof MyAccountFragment) {
			if (mProgressDialog == null)
				reinitializeProgress();
			mProgressDialog.dismiss();
			if(isFinishing()) {
				AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setCancelable(false)
						.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						}).create();
				alert.show();
			}
			else{
				Intent intent = new Intent(getApplicationContext(), DialogActivitySyncSuccess.class);
				startActivity(intent);
			}
		}
		else {
			if (NetworkUtil.getInstance(MainActivity.this).isNetworkAvailable()) {
				mServerSyncAsyncTask = new TwoWaySyncAsyncTask();
				mServerSyncAsyncTask.listener(this);
				String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
				int userId = mPreferenceWrapper.getPreferenceIntValue(USER_ID);

				if (userId == 0){
					mCurrentOperation = OPERATION_GET_USERID;
					mServerSyncAsyncTask.addParam("access_token", accessToken);
					mServerSyncAsyncTask.execute(API_URL + USER_URI);
				}
				else {
					mCurrentOperation = OPERATION_CHECK_SERVERTIME;
					Watch watch = mLifeTrakApplication.getSelectedWatch();
					UserProfile profile = mLifeTrakApplication.getUserProfile();
					mServerSyncAsyncTask.addParam("access_token", accessToken);
					mServerSyncAsyncTask.addParam("mac_address", watch.getMacAddress());
					mServerSyncAsyncTask.execute(API_URL + DEVICE_DATA_URL + "/" + userId + "/" + watch.getMacAddress());
				}
			}
			else{
				if(isFinishing()) {
					NetworkUtil.getInstance(MainActivity.this).showConnectionErrorMessage();
				}
				else{
					Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
					startActivity(intent);
				}

				if (mProgressDialog == null)
					reinitializeProgress();
				if (mProgressDialog.isShowing())
					mProgressDialog.dismiss();

				//updateAllDataHeaders();
				if (mContentFragment instanceof DashboardFragment){
					DashboardFragment fragment = (DashboardFragment) mContentFragment;
					fragment.initializeObjects();
				}

			}
		}
	}

	private void startSyncToServer() {
		if (mProgressDialog == null)
			reinitializeProgress();
		if (!mProgressDialog.isShowing())
			mProgressDialog.show();

		mProgressDialog.setMessage(getString(R.string.sync_to_cloud));
		mCurrentApiRequest = API_REQUEST_SEND;

		mCurrentOperation = OPERATION_SYNC_TO_CLOUD;

		LifeTrakLogger.info("Start Sync to Server - " + new Date());

		if (mContentFragment instanceof MyAccountFragment){
			if (mProgressDialog == null)
				reinitializeProgress();

			mProgressDialog.dismiss();
			if(isFinishing()) {
				AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setCancelable(false)
						.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						}).create();
				alert.show();
			}
			else{
				Intent intent = new Intent(getApplicationContext(), DialogActivitySyncSuccess.class);
				startActivity(intent);
			}
		}
		else{
			if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420 || mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415 ||
					mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_C410 || mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_C300
					|| mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_C300_IOS){
				mSyncThread = new Thread(new Runnable() {
					@Override
					public void run() {
						synchronized(LOCK_OBJECT) {
							mCurrentApiRequest = API_REQUEST_SEND;
							mCurrentOperation = OPERATION_BULK_INSERT_S3;
							indexListCounter = 0;
							retryCounter = 0;
							dataHeaders = DataSource.getInstance(MainActivity.this)
									.getReadOperation()
									.query("watchDataHeader = ? and syncedToCloud = 0", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
									.orderBy("dateStamp", SORT_DESC)
									.getResults(StatisticalDataHeader.class, false);

							uuid = AmazonTransferUtility.generateRandomUUID();
							if (dataHeaders.size() > 0) {
								try {
									if (dataHeaders.size() > indexListCounter){
										LifeTrakLogger.info("Sync Data to s3 count " + String.valueOf(indexListCounter) + " OF " + String.valueOf(dataHeaders.size()));
										uploadS3(mLifeTrakApplication.getSelectedWatch(), dataHeaders.get(indexListCounter));
									}
								}
								catch (JSONException e){
									LifeTrakLogger.info("Error: " + e.getLocalizedMessage());
								}
							}
							else{
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (mContentFragment instanceof DashboardFragment) {
											DashboardFragment fragment = (DashboardFragment) mContentFragment;
											fragment.initializeObjects();
										}
										if (mProgressDialog == null)
											reinitializeProgress();
										if (mProgressDialog.isShowing())
											mProgressDialog.dismiss();

										if (mContentFragment instanceof DashboardFragment) {
											Date date = new Date();
											mLifeTrakApplication.setCurrentDate(date);
											setCalendarDate(date);
											DashboardFragment fragment = (DashboardFragment) mContentFragment;
											fragment.initializeObjects();
										}
										if (mPreferenceWrapper.getPreferenceBooleanValue(GOOGLE_FIT_ENABLED)) {
											GoogleFitSyncService.start(MainActivity.this, mLifeTrakApplication.getSelectedWatch());
										}


										if (isFinishing()) {
											AlertDialog alert = new AlertDialog.Builder(MainActivity.this).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setCancelable(false)
													.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface arg0, int arg1) {
															arg0.dismiss();
														}
													}).create();
											alert.show();
										} else {
											Intent intent = new Intent(getApplicationContext(), DialogActivitySyncSuccess.class);
											startActivity(intent);
										}

										LifeTrakLogger.info("Sync End to Server - " + new Date());
									}
								});

							}
						}
					}
				});
				mSyncThread.start();

			}
			else{
				mCurrentApiRequest = API_REQUEST_SEND;
				mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
				mSyncThread = new Thread(new Runnable() {
					public void run() {
						synchronized(LOCK_OBJECT) {
							try {
								if (mLifeTrakApplication != null){
									Watch watch = mLifeTrakApplication.getSelectedWatch();
									mServerSyncAsync.setAsyncListener(MainActivity.this);

									JSONObject data = new JSONObject();
									if (mServerSyncAsync.getDevice(watch.getMacAddress()) != null)
										data.put("device", mServerSyncAsync.getDevice(watch.getMacAddress()));
									if (mServerSyncAsync.getAllWorkoutInfos(watch.getId(),mPreferenceWrapper.getPreferenceBooleanValue(FROM_IOS), watch) != null)
										data.put("workout", mServerSyncAsync.getAllWorkoutInfos(watch.getId(),mPreferenceWrapper.getPreferenceBooleanValue(FROM_IOS), watch));
									if (mServerSyncAsync.getAllSleepDatabases(watch.getId()) != null)
										data.put("sleep", mServerSyncAsync.getAllSleepDatabases(watch.getId()));
									if (mServerSyncAsync.getAllDataHeaders(watch) != null)
										data.put("data_header", mServerSyncAsync.getAllDataHeaders(watch));
									if (mServerSyncAsync.getDeviceSettings(watch.getId(), mLifeTrakApplication) != null)
										data.put("device_settings", mServerSyncAsync.getDeviceSettings(watch.getId(), mLifeTrakApplication));
									if (mServerSyncAsync.getUserProfile(watch.getId()) != null)
										data.put("user_profile", mServerSyncAsync.getUserProfile(watch.getId()));
									if (mServerSyncAsync.getAllGoals(watch.getId()) != null)
										data.put("goal", mServerSyncAsync.getAllGoals(watch.getId()));
									if (mServerSyncAsync.getSleepSetting(watch.getId()) != null)
										data.put("sleep_settings", mServerSyncAsync.getSleepSetting(watch.getId()));

									if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
										if (mServerSyncAsync.getWakeupInfo(watch.getId()) != null)
											data.put("wakeup_info", mServerSyncAsync.getWakeupInfo(watch.getId()));
										if (mServerSyncAsync.getActivityAlertSetting(watch.getId()) != null)
											data.put("inactive_alert_settings", mServerSyncAsync.getActivityAlertSetting(watch.getId()));
										if (mServerSyncAsync.getLightSetting(watch.getId()) != null)
											data.put("light_settings", mServerSyncAsync.getLightSetting(watch.getId()));
									}

									if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
										data.put("workout_header", mServerSyncAsync.getAllWorkoutHeaders(watch.getId()));
									}

									String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);

									//LifeTrakLogger.info(" data:" +  data.toString());
									LifeTrakLogger.info(" accesstoken:" +  accessToken);
									mServerSyncAsync.url(API_URL + SYNC_URI).addParam("access_token", accessToken).addParam("data", data.toString()).post();
								}
								else{
									AlertDialog alert = new AlertDialog.Builder(MainActivity.this).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_watch_wrong).setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											if (mContentFragment instanceof DashboardFragment) {
												Date date = new Date();
												mLifeTrakApplication.setCurrentDate(date);
												setCalendarDate(date);
												DashboardFragment fragment = (DashboardFragment) mContentFragment;
												fragment.initializeObjects();
											}
											arg0.dismiss();
										}
									}).create();
									alert.show();
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
				mSyncThread.start();
			}

		}
	}

	@Override
	public void onAsyncStart() {
	}

	@Override
	public void onAsyncFail(int status, final String message) {
		if (mCurrentOperation == OPERATION_BULK_INSERT_S3) {

			if (!mCancelled) {

				mSyncThread = new Thread(new Runnable() {
					@Override
					public void run() {
						synchronized (LOCK_OBJECT) {
							retryCounter++;
							try {
								if (NetworkUtil.getInstance(MainActivity.this).isNetworkAvailable()) {
									if (retryCounter < 3) {
										LifeTrakLogger.info("Sync Data to s3 count " + String.valueOf(indexListCounter) + " OF " + String.valueOf(dataHeaders.size()) + " Error Count = " + String.valueOf(retryCounter));
										uploadS3(mLifeTrakApplication.getSelectedWatch(), dataHeaders.get(indexListCounter));
									} else {
										LifeTrakLogger.info("Error on sync to cloud : " + message);
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												mProgressDialog.dismiss();
												flag_sync_cloud_error = true;
												if (isFinishing()) {
													mAlertDialog = new AlertDialog.Builder(MainActivity.this).setTitle(R.string.lifetrak_title).setMessage(message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface arg0, int arg1) {
															arg0.dismiss();
														}
													}).create();
													mAlertDialog.show();
												} else {
													Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
													startActivity(intent);
												}
											}
										});

									}
								} else {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											flag_sync_cloud_error = true;
											if (isFinishing()) {
												NetworkUtil.getInstance(MainActivity.this).showConnectionErrorMessage();
											} else {
												Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
												startActivity(intent);
											}
										}
									});
								}
							} catch (JSONException e) {

							}
						}
					}
				});
				mSyncThread.start();
			}
			else{
				mCancelled = false;
			}
		}
		else {
			if (mProgressDialog == null)
				reinitializeProgress();

			mProgressDialog.dismiss();

			mServerSyncAsync = new ServerSyncAsync(this);
			mServerSyncAsync.setAsyncListener(this);

			if (mCancelled) {
				mCancelled = false;
				mLifeTrakSyncR450.resumeSync();
				return;
			}
			if (message != null) {
				if (message.equalsIgnoreCase("network error")) {
					flag_sync_cloud_error = true;
					if (mContentFragment instanceof DashboardFragment) {
						Date date = new Date();
						mLifeTrakApplication.setCurrentDate(date);
						setCalendarDate(date);
						DashboardFragment fragment = (DashboardFragment) mContentFragment;
						fragment.initializeObjects();
					}
					if (isFinishing()) {
						AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(R.string.check_network_connection).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						}).create();
						alert.show();
					}
					else{
						Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
						startActivity(intent);
					}

				} else if (message.equalsIgnoreCase("Unable to retrieve device with specified mac address.")) {
					if (NetworkUtil.getInstance(MainActivity.this).isNetworkAvailable()) {
						mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
						startSyncToServer();
					} else {
						if (isFinishing()) {
							NetworkUtil.getInstance(MainActivity.this).showConnectionErrorMessage();
						} else {
							Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
							startActivity(intent);
						}
						if (mProgressDialog == null)
							reinitializeProgress();
						if (mProgressDialog.isShowing())
							mProgressDialog.dismiss();
					}
				} else {
					flag_sync_cloud_error = true;
					AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(getString(R.string.network_error)).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if (mContentFragment instanceof DashboardFragment) {
								Date date = new Date();
								mLifeTrakApplication.setCurrentDate(date);
								setCalendarDate(date);
								DashboardFragment fragment = (DashboardFragment) mContentFragment;
								fragment.initializeObjects();
							}
							arg0.dismiss();
						}
					}).create();
					alert.show();
				}
			} else {
				flag_sync_cloud_error = true;
				if (mProgressDialog == null)
					reinitializeProgress();
				if (mProgressDialog.isShowing())
					mProgressDialog.dismiss();
				if (isFinishing()) {
					NetworkUtil.getInstance(MainActivity.this).showConnectionErrorMessage();
				} else {
					Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
					startActivity(intent);
				}
			}

			LifeTrakLogger.info("Sync End Fail to Server - " + new Date());
		}
	}

	@Override
	public void onAsyncSuccess(final JSONObject result) {
		switch (mCurrentOperation) {
			case OPERATION_REFRESH_TOKEN:
				try {
					mPreferenceWrapper.setPreferenceStringValue(ACCESS_TOKEN, result.getString("access_token")).setPreferenceStringValue(REFRESH_TOKEN, result.getString("refresh_token"))
							.setPreferenceLongValue(EXPIRATION_DATE, result.getLong("expires")).synchronize();

					String email = mPreferenceWrapper.getPreferenceStringValue(EMAIL);

					List<UserProfile> profiles = DataSource.getInstance(this).getReadOperation().query("email = ?", email).getResults(UserProfile.class);

					if (profiles.size() > 0) {
						UserProfile profile = profiles.get(0);

						List<Watch> watches = DataSource.getInstance(this).getReadOperation().query("accessToken = ?", profile.getAccessToken()).getResults(Watch.class);

						profile.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
						profile.update();

						for (Watch watch : watches) {
							watch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
							watch.update();

							if (mLifeTrakApplication.getSelectedWatch().getId() == watch.getId()) {
								mLifeTrakApplication.getSelectedWatch().setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
							}
						}
					}

					mCurrentOperation = OPERATION_CHECK_SERVERTIME;
					startCheckingServer();
					//startSyncToServer();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case OPERATION_SYNC_TO_CLOUD:
				String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
				switch (mCurrentApiRequest) {
					case API_REQUEST_SEND:
						mCurrentApiRequest = API_REQUEST_STORE;
						mServerSyncAsync.url(API_URL + STORE_URI).addParam("access_token", accessToken).addParam("mac_address", mLifeTrakApplication.getSelectedWatch().getMacAddress()).post();
						break;
					case API_REQUEST_STORE:
						if (!mCancelled){
						mCurrentApiRequest = API_REQUEST_SEND;
						syncProfileServer();
						}
						else{
							mCancelled = false;
						}
				}
				break;



			case OPERATION_CHECK_SERVERTIME:
				try{
//                    JSONObject objectResult = result.getJSONObject("result");
//                    String lastDateSynced = objectResult.getString(LAST_DATE_SYNCED);
//                    SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    Date dateServerLastSync = format.parse(lastDateSynced);
//
//                    Watch watch = mLifeTrakApplication.getSelectedWatch();
//                    Date dateAppLastSync = watch.getCloudLastSyncDate();
					JSONObject objectResult = result.getJSONObject("result");
					String lastDateSynced = objectResult.getString(LAST_DATE_SYNCED);
					SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					//format.setTimeZone(TimeZone.getTimeZone("GMT"));
					Date localTime = new Date();
					Date dateServerLastSync = format.parse(lastDateSynced);
					Date fromGmt = new Date(dateServerLastSync.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
					Watch watch = mLifeTrakApplication.getSelectedWatch();
					Date dTAppLastSync = watch.getCloudLastSyncDate();

					if (dTAppLastSync == null){
						watch.setCloudLastSyncDate(new Date());
						watch.update();
						dTAppLastSync = new Date();
					}

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					//sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
					String utcTime = sdf.format(dTAppLastSync);

					Date dateAppLastSync = null;
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try
					{
						dateAppLastSync = (Date)dateFormat.parse(utcTime);
					}
					catch (ParseException e)
					{
						e.printStackTrace();
					}

					final  TwoWaySyncChecker mTwoWaySyncChecker = TwoWaySyncChecker.newInstance(MainActivity.this, watch);

//					boolean isServerTimeLatest = mTwoWaySyncChecker.isServerLatest(dateAppLastSync, dateServerLastSync);
//					if (!isServerTimeLatest){
//						if (NetworkUtil.getInstance(MainActivity.this).isNetworkAvailable()) {
//							mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
//							startSyncToServer();
//						}
//						else{
//							if(isFinishing()) {
//								NetworkUtil.getInstance(MainActivity.this).showConnectionErrorMessage();
//							}
//							else{
//								Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
//								startActivity(intent);
//							}
//							if (mProgressDialog == null)
//								reinitializeProgress();
//							if (mProgressDialog.isShowing())
//								mProgressDialog.dismiss();
//						}
//					}
//					else{
						if (NetworkUtil.getInstance(MainActivity.this).isNetworkAvailable()) {
							if (!mCancelled){
							mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
							startSyncToServer();
							}
							else{
								mCancelled = false;
							}
						}
						else{
							if(isFinishing()) {
								NetworkUtil.getInstance(MainActivity.this).showConnectionErrorMessage();
							}
							else{
								Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
								startActivity(intent);
							}
							if (mProgressDialog == null)
								reinitializeProgress();
							if (mProgressDialog.isShowing())
								mProgressDialog.dismiss();
				//		}

						//Update data from server
//						watch.setContext(MainActivity.this);
//						watch.setCloudLastSyncDate(new Date());
//						watch.update();
//						startRestoreFromServer(dateServerLastSync);
					}


				} catch (JSONException e) {
					e.printStackTrace();
				}
				catch (ParseException e) {
					e.printStackTrace();
				}
				break;

			case OPERATION_GET_USERID:
				try {
					JSONObject objectResult = result.getJSONObject("result");
					int id = objectResult.getInt("id");
					mPreferenceWrapper
							.setPreferenceIntValue(USER_ID, id)
							.synchronize();
				}catch (JSONException e) {
					AlertDialog alert = new AlertDialog.Builder(MainActivity.this).setTitle(R.string.lifetrak_title).setMessage(result.toString())
							.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();
								}
							}).create();
					alert.show();

				}
				mCurrentOperation = OPERATION_CHECK_SERVERTIME;
				startCheckingServer();
				break;


			case OPERATION_EDIT_PROFILE:
				if (mProgressDialog == null)
					reinitializeProgress();

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mProgressDialog.isShowing())
							mProgressDialog.dismiss();
					}
				}, 750);


				mCurrentApiRequest = API_REQUEST_SEND;

				updateAllDataHeaders();
				String accessTokenStr = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
				final Watch watch = mLifeTrakApplication.getSelectedWatch();
				watch.setContext(MainActivity.this);
				watch.setAccessToken(accessTokenStr);
				watch.setCloudLastSyncDate(new Date());
				watch.update();


				if (mContentFragment instanceof DashboardFragment) {
					DashboardFragment fragment = (DashboardFragment) mContentFragment;
					fragment.initializeObjects();
				}
				if (mProgressDialog == null)
					reinitializeProgress();
				if (mProgressDialog.isShowing())
					mProgressDialog.dismiss();

				if (mContentFragment instanceof DashboardFragment) {
					Date date = new Date();
					mLifeTrakApplication.setCurrentDate(date);
					setCalendarDate(date);
					DashboardFragment fragment = (DashboardFragment) mContentFragment;
					fragment.initializeObjects();
				}
				if (mPreferenceWrapper.getPreferenceBooleanValue(GOOGLE_FIT_ENABLED)) {
					GoogleFitSyncService.start(MainActivity.this, watch);
				}

				if (flag_sync_cloud_error == true){
					flag_sync_cloud_error = false;
				}else {
					if (isFinishing()) {
						AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setCancelable(false)
								.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										arg0.dismiss();
									}
								}).create();
						alert.show();
					} else {
						Intent intent = new Intent(getApplicationContext(), DialogActivitySyncSuccess.class);
						startActivity(intent);
					}
				}
				LifeTrakLogger.info("Sync End to Server - " + new Date());
				break;
			case OPERATION_BULK_INSERT_S3:
				if (!mCancelled) {
					indexListCounter++;
					retryCounter = 0;
					final Watch mWatch = mLifeTrakApplication.getSelectedWatch();
					if (dataHeaders.size() == indexListCounter) {
						mSyncThread = new Thread(new Runnable() {
							@Override
							public void run() {
								synchronized (LOCK_OBJECT) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											//mDescSubTitle.setText("Getting All Files [ 1/"+ String.valueOf(fileNamesArray.length()) + " ]");
											if (mProgressDialog == null)
												reinitializeProgress();
											Double per = ((double) indexListCounter / dataHeaders.size()) * 100;
											mProgressDialog.setMessage(getString(R.string.sync_to_cloud));
											if (!mProgressDialog.isShowing())
												mProgressDialog.show();

										}
									});
									mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
									mCurrentApiRequest = API_REQUEST_STORE;

									String mAccessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
									ServerSyncAsyncTask mServerSyncAsyncTask = new ServerSyncAsyncTask();
									mServerSyncAsyncTask.listener(MainActivity.this);
									mServerSyncAsyncTask.addParam("access_token", mAccessToken);
									mServerSyncAsyncTask.addParam("uuid", uuid);
									mServerSyncAsyncTask.execute(API_URL + STORE_URI_V2);
								}
							}
						});
						mSyncThread.start();
					} else {
						mSyncThread = new Thread(new Runnable() {
							@Override
							public void run() {
								synchronized (LOCK_OBJECT) {
									try {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												//mDescSubTitle.setText("Getting All Files [ 1/"+ String.valueOf(fileNamesArray.length()) + " ]");
												if (mProgressDialog == null)
													reinitializeProgress();
												Double per = ((double) indexListCounter / dataHeaders.size()) * 100;
												mProgressDialog.setMessage("Uploading Data " + String.valueOf(Math.round(per)) + "%");
												if (!mProgressDialog.isShowing())
													mProgressDialog.show();

											}
										});
										LifeTrakLogger.info("Sync Data to s3 count " + String.valueOf(indexListCounter) + " OF " + String.valueOf(dataHeaders.size()));
										uploadS3(mWatch, dataHeaders.get(indexListCounter));

									} catch (JSONException e) {

									}
								}
							}
						});
						mSyncThread.start();

					}
				}
				else{
					mCancelled = false;
				}
				break;

		}
	}

	private void startRestoreFromServer(final Date date) {
		mCurrentOperation = OPERATION_RESTORE_FROM_CLOUD;
		if(mLifeTrakApplication.getSelectedWatch() != null) {
			new Thread(new Runnable() {
				public void run() {
//					mServerRestoreAsync.url(API_URL + RESTORE_URI + "/" + date.toString() + "/" + (new Date()).toString())
//							.addParam("access_token", mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN))
//							.addParam("mac_address", mLifeTrakApplication.getSelectedWatch().getMacAddress())
//							.get();
					mServerRestoreAsync.url(API_URL + RESTORE_URI)
							.addParam("access_token", mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN))
							.addParam("mac_address", mLifeTrakApplication.getSelectedWatch().getMacAddress())
							.get();
				}
			}).start();
		}
	}

	private Date getExpirationDate() {
		long millis = mPreferenceWrapper.getPreferenceLongValue(EXPIRATION_DATE) * 1000;
		Date date = new Date(millis);
		return date;
	}

	private void useWatchSettings() {
		final UserProfile userProfile = mLifeTrakApplication.getUserProfile();
		userProfile.setWeight(mUserProfile.getWeight());
		userProfile.setHeight(mUserProfile.getHeight());
		userProfile.setBirthDay(mUserProfile.getBirthDay());
		userProfile.setBirthMonth(mUserProfile.getBirthMonth());
		userProfile.setBirthYear(mUserProfile.getBirthYear());
		userProfile.setSensitivity(mUserProfile.getSensitivity());
		userProfile.setGender(mUserProfile.getGender());
		userProfile.setUnitSystem(mUserProfile.getUnitSystem());
		mLifeTrakApplication.setUserProfile(userProfile);

		if (mCalibrationData != null) {
			if (mCalibrationData.getId() == 0) {
				mCalibrationData.insert();
			} else {
				mCalibrationData.update();
			}
		}

		if (mGoal != null) {
			if (mGoal.getId() == 0) {
				mGoal.insert();
			} else {
				mGoal.update();
			}
		}

		Handler handler = new Handler();

		handler.postDelayed(new Runnable() {
			public void run() {
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
		}, HANDLER_DELAY);

		handler.postDelayed(new Runnable() {
			public void run() {
				SALTimeDate salTimeDate = new SALTimeDate();
				if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME))
					salTimeDate.setToNow();

				switch (mTimeDate.getHourFormat()) {
					case TIME_FORMAT_12_HR:
						salTimeDate.setTimeFormat(SALTimeDate.FORMAT_12HOUR);
						break;
					case TIME_FORMAT_24_HR:
						salTimeDate.setTimeFormat(SALTimeDate.FORMAT_24HOUR);
						break;
				}
				salTimeDate.setDateFormat(mTimeDate.getDateFormat());

				int status = mSalutronService.updateTimeAndDate(salTimeDate);

				if (status == SALStatus.NO_ERROR)
					initializeSyncToServer();
			}
		}, HANDLER_DELAY * 2);
	}

	private void useAppSettings() {
		final UserProfile userProfile = mLifeTrakApplication.getUserProfile();
		final TimeDate timeDate = mLifeTrakApplication.getTimeDate();

		Handler handler = new Handler();

		handler.postDelayed(new Runnable() {
			public void run() {
				SALUserProfile salUserProfile = new SALUserProfile();
				salUserProfile.setWeight(userProfile.getWeight());
				salUserProfile.setHeight(userProfile.getHeight());
				salUserProfile.setBirthDay(userProfile.getBirthDay());
				salUserProfile.setBirthMonth(userProfile.getBirthMonth());
				salUserProfile.setBirthYear(userProfile.getBirthYear() - 1900);
				salUserProfile.setSensitivityLevel(userProfile.getSensitivity());
				salUserProfile.setGender(userProfile.getGender());
				salUserProfile.setUnitSystem(userProfile.getUnitSystem());
				if (mSalutronService != null)
					mSalutronService.updateUserProfile(salUserProfile);
			}
		}, HANDLER_DELAY);

		handler.postDelayed(new Runnable() {
			public void run() {
				SALTimeDate salTimeDate = new SALTimeDate();
				salTimeDate.setToNow();

				switch (timeDate.getHourFormat()) {
					case TIME_FORMAT_12_HR:
						salTimeDate.setTimeFormat(SALTimeDate.FORMAT_12HOUR);
						break;
					case TIME_FORMAT_24_HR:
						salTimeDate.setTimeFormat(SALTimeDate.FORMAT_24HOUR);
						break;
				}
				salTimeDate.setDateFormat(timeDate.getDateFormat());
				if (mSalutronService != null)
					mSalutronService.updateTimeAndDate(salTimeDate);
			}
		}, HANDLER_DELAY * 2);

		if (mLifeTrakApplication != null && mLifeTrakApplication.getSelectedWatch() != null) {
			List<CalibrationData> calibData = DataSource.getInstance(this)
					.getReadOperation().query("watchCalibrationData = ?", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
					.getResults(CalibrationData.class);
			final CalibrationData calibrationData;

			if (calibData.size() > 0) {
				calibrationData = calibData.get(0);
			} else {
				calibrationData = mCalibrationData;
			}

			final SALCalibration salCalibration = new SALCalibration();

			mHandler.postDelayed(new Runnable() {
				public void run() {
					if (calibrationData != null) {
						salCalibration.setCalibrationType(SALCalibration.STEP_CALIBRATION);
						salCalibration.setStepCalibration(calibrationData.getStepCalibration());
						if (mSalutronService != null)
							mSalutronService.updateCalibrationData(salCalibration);
					}
				}
			}, HANDLER_DELAY * 3);

			mHandler.postDelayed(new Runnable() {
				public void run() {
					if (calibrationData != null) {
						salCalibration.setCalibrationType(SALCalibration.WALK_DISTANCE_CALIBRATION);
						salCalibration.setCalibrationValue(calibrationData.getDistanceCalibrationWalk());

						if (mSalutronService != null)
							mSalutronService.updateCalibrationData(salCalibration);
					}
				}
			}, HANDLER_DELAY * 4);

			mHandler.postDelayed(new Runnable() {
				public void run() {
					if (calibrationData != null) {
						salCalibration.setCalibrationType(SALCalibration.AUTO_EL_SETTING);
						salCalibration.setCalibrationValue(calibrationData.getAutoEL());
						if (mSalutronService != null){
							int status = mSalutronService.updateCalibrationData(salCalibration);

							if (status == SALStatus.NO_ERROR)
								initializeSyncToServer();
						}
					}
				}
			}, HANDLER_DELAY * 5);
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		List<Goal> goals = DataSource.getInstance(this)
				.getReadOperation()
				.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()), String.valueOf(day),
						String.valueOf(month), String.valueOf(year)).getResults(Goal.class);

		if (goals.size() > 0) {
			mGoal = goals.get(0);
		}


		mHandler.postDelayed(new Runnable() {
			public void run() {
				if (mSalutronService != null)
					mSalutronService.updateStepGoal(mGoal.getStepGoal());
			}
		}, HANDLER_DELAY * 6);

		mHandler.postDelayed(new Runnable() {
			public void run() {
				if (mSalutronService != null)
					mSalutronService.updateDistanceGoal((long)mGoal.getDistanceGoal() * 100l);
			}
		}, HANDLER_DELAY * 7);

		mHandler.postDelayed(new Runnable() {
			public void run() {
				if (mSalutronService != null)
					mSalutronService.updateCalorieGoal(mGoal.getCalorieGoal());
			}
		}, HANDLER_DELAY * 8);

		mHandler.postDelayed(new Runnable() {
			public void run() {
				if (mLifeTrakApplication != null){
					if(mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_C300) {
						SALSleepSetting sleepSetting = new SALSleepSetting();
						List<SleepSetting> sleepSettings = DataSource.getInstance(MainActivity.this)
								.getReadOperation()
								.query("watchSleepSetting = ?", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
								.getResults(SleepSetting.class);

						if(sleepSettings.size() > 0) {
							sleepSetting.setSleepDetectType(sleepSettings.get(0).getSleepDetectType());
							sleepSetting.setSleepGoal(mGoal.getSleepGoal());
							if (mSalutronService != null)
								mSalutronService.updateSleepSetting(sleepSetting);
						}
					}
				}
			}
		}, HANDLER_DELAY * 9);

	}

	private void initializeSyncToServer() {
		flag_finished_syncing_r450 = true;
		if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC)) {
			if (mContentFragment instanceof MyAccountFragment || mContentFragment instanceof WatchSettingsFragment ) {
				if (mProgressDialog.isShowing())
					mProgressDialog.dismiss();
				if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R415)
					mSalutronService.disconnectFromDevice();

				if (mContentFragment instanceof DashboardFragment) {
					Date date = new Date();
					mLifeTrakApplication.setCurrentDate(date);
					setCalendarDate(date);
					DashboardFragment fragment = (DashboardFragment) mContentFragment;
					fragment.initializeObjects();
				}

				if(isFinishing()) {
					AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setCancelable(false)
							.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();
								}
							}).create();
					alert.show();
				}
				else{
					Intent intent = new Intent(getApplicationContext(), DialogActivitySyncSuccess.class);
					startActivity(intent);
				}
			}
			else{
				if (mProgressDialog == null)
					reinitializeProgress();
				if (!mProgressDialog.isShowing())
					mProgressDialog.show();

				mProgressDialog.setMessage(getString(R.string.sync_to_watch));
				mCurrentApiRequest = API_REQUEST_SEND;

				Date expirationDate = getExpirationDate();
				Date now = new Date();

				if (!NetworkUtil.getInstance(MainActivity.this).isNetworkAvailable()) {
					if (isFinishing()) {
						NetworkUtil.getInstance(MainActivity.this).showConnectionErrorMessage();
					}
					else{
						Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
						startActivity(intent);
					}
					if (mProgressDialog == null)
						reinitializeProgress();

					mProgressDialog.dismiss();
				} else {
					if (now.after(expirationDate)) {
						mCurrentOperation = OPERATION_REFRESH_TOKEN;
						refreshToken();
					} else {

						mCurrentOperation = OPERATION_CHECK_SERVERTIME;
						startCheckingServer();
						//startSyncToServer();
					}
				}
			}
		} else {
			mProgressDialog.dismiss();
			if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R415)
				mSalutronService.disconnectFromDevice();

			if (mContentFragment instanceof DashboardFragment) {
				Date date = new Date();
				mLifeTrakApplication.setCurrentDate(date);
				setCalendarDate(date);
				DashboardFragment fragment = (DashboardFragment) mContentFragment;
				fragment.initializeObjects();
			}
			if(isFinishing()) {
				AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setCancelable(false)
						.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						}).create();
				alert.show();
			}
			else{
				Intent intent = new Intent(getApplicationContext(), DialogActivitySyncSuccess.class);
				startActivity(intent);
			}
		}

		mSyncSuccess = true;

		if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R415)
			mSalutronService.disconnectFromDevice();
	}

	public void updateMainMenu() {
		if (mMenuFragment instanceof MenuFragment) {
			MenuFragment menuFragment = (MenuFragment) mMenuFragment;
			try {
				menuFragment.initializeObjects();
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateCalendarDate() {
		if (mTempDataHeaders.size() > 0) {
			if (mTempDataHeaders.size() == 1) {
				Date date = mTempDataHeaders.get(0).getDateStamp();
				mCalendar.addSyncedDate(date);
			} else {
				Date dateFrom = mTempDataHeaders.get(0).getDateStamp();
				Date dateTo = mTempDataHeaders.get(mTempDataHeaders.size() - 1).getDateStamp();
				mCalendar.addSyncedDates(dateFrom, dateTo);
			}
		}
		mTempDataHeaders.clear();
	}

	@Override
	public void onDeviceConnected(BluetoothDevice device) {

		LifeTrakLogger.info("device connected on MainActivity");
		mWatchConnected = false;
		mPreferenceWrapper.setPreferenceBooleanValue(IS_WATCH_CONNECTED, false).synchronize();
		try {
			if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415)
				mPreferenceWrapper.setPreferenceStringValue(LAST_SYNCED_R450_WATCH_MAC_ADDRESS, device.getAddress()).synchronize();
		}
		catch (Exception e){

		}
	}

	@Override
	public void onDeviceReady() {
		LifeTrakLogger.info("device ready on MainActivity");
		deviceFound = true;
		mWatchConnected = false;
		if (mLifeTrakApplication.getSelectedWatch() == null){

		}
		else {
			if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
				if (mOperation == OPERATION_SYNC_SETTINGS) {
					mSyncSuccess = false;
					try {
						DeviceScanListener l = (DeviceScanListener) mContentFragment;

						mSyncSuccess = false;
						l.onDeviceConnected(mLifeTrakSyncR450.getConnectedDevice(), mLifeTrakSyncR450.getBLEService(),
								mLifeTrakApplication.getSelectedWatch());
						mProgressDialog.setMessage(getString(R.string.syncing_data_2));

					} catch (NullPointerException e) {
						Watch watch = mLifeTrakApplication.getSelectedWatch();

						if (watch.getModel() == WATCHMODEL_R415) {
							if (mAlarmManager != null && mPendingIntent != null)
								mAlarmManager.cancel(mPendingIntent);

							mOperation = OPERATION_SYNC_SETTINGS;
							mLifeTrakSyncR450.connectToDevice(watch.getMacAddress(), WATCHMODEL_R415);

							if (mAlarmManager != null && mPendingIntent != null)
								mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (10 * 1000), 10 * 1000, mPendingIntent);
						}
					}
				} else if (mOperation == OPERATION_SYNC_WATCH) {
					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (mFromOnError && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
								mFromOnError = false;
								mLifeTrakSyncR450.startSync();
							}
						}
					}, HANDLER_DELAY);
				}
			} else if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
				if (mOperation == OPERATION_SYNC_SETTINGS) {
					mSyncSuccess = false;
					try {
						DeviceScanListener l = (DeviceScanListener) mContentFragment;

						mSyncSuccess = false;
						l.onDeviceConnected(mLifeTrakSyncR420.getConnectedDevice(), mLifeTrakSyncR420.getBLEService(),
								mLifeTrakApplication.getSelectedWatch());
						mProgressDialog.setMessage(getString(R.string.syncing_data_2));

					} catch (NullPointerException e) {
					}

				} else if (mOperation == OPERATION_SYNC_WATCH) {
					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (mFromOnError && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
								mFromOnError = false;
								mLifeTrakSyncR420.startSync();
							}
						}
					}, HANDLER_DELAY);
				}
			}
		}
	}

	@Override
	public void onDeviceDisconnected() {
		try {
			if (!flag_finished_syncing_r450) {
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
					syncingWatchFailed();
					//onPairTimeout();
				}
			}
			else
			{
				flag_finished_syncing_r450 = false;
			}
		} catch (final IllegalArgumentException e) {
			// Handle or log or ignore
		} catch (final Exception e) {
			// Handle or log or ignore
		} finally {
			//mProgressDialog = null;
		}
		mOperation = OPERATION_SEARCH_BLUETOOTH;
	}

	@Override
	public void onSyncTime() {
	}

	@Override
	public void onSyncStatisticalDataHeaders() {
		/*mTimeoutHandler.postDelayed(new Runnable() {
            public void run() {
                if (!mLifeTrakSyncR450.isSyncStarted()) {
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    onPairTimeout();
                }
            }
        }, 15000);*/
	}

	@Override
	public void onSyncStatisticalDataPoint(int percent) { }

	@Override
	public void onSyncStepGoal() { }

	@Override
	public void onSyncDistanceGoal() { }

	@Override
	public void onSyncCalorieGoal() { }

	@Override
	public void onSyncSleepSetting() { }

	@Override
	public void onSyncCalibrationData() { }

	@Override
	public void onSyncWorkoutDatabase() { }

	@Override
	public void onSyncSleepDatabase() {
	}

	@Override
	public void onSyncUserProfile() { }

	@Override
	public void onStartSync() {
		mWatchConnected = false;
		mPreferenceWrapper.setPreferenceBooleanValue(IS_WATCH_CONNECTED, false).synchronize();

		if (mProgressDialog == null) {
			reinitializeProgress();
		}
		mProgressDialog.setMessage(getString(R.string.syncing_data_2));

		if (!mProgressDialog.isShowing())
			mProgressDialog.show();
	}

	@Override
	public void onSyncFinish() {
		//updateAllDataHeaders();

		new Thread(new Runnable() {
			public void run() {
				final Watch watch = mLifeTrakApplication.getSelectedWatch();
				watch.setLastSyncDate(new Date());
				watch.update();

				mLifeTrakApplication.setSelectedWatch(watch);

				if (mLifeTrakSyncR450 != null  && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415 ) {

					watch.setWatchFirmWare(mLifeTrakSyncR450.firmwareVersion);
					watch.setWatchSoftWare(mLifeTrakSyncR450.softwareRevision);
					mLifeTrakSyncR450.storeData(watch);

					try {
						watch.setWatchFirmWare(mLifeTrakSyncR450.firmwareVersion);
						watch.setWatchSoftWare(mLifeTrakSyncR450.softwareRevision);
						watch.setContext(MainActivity.this);
						watch.update();
					}
					catch (Exception e){
						LifeTrakLogger.info("Error:" + e.getLocalizedMessage());
					}

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (mMenuFragment instanceof MenuFragment) {
								updateLastSyncDate();
							}
						}
					});

					if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415){
						if (mContentFragment instanceof DashboardFragment || mContentFragment instanceof MyAccountFragment) {
							final LifeTrakUpdateR450 lifeTrakUpdateR450 = LifeTrakUpdateR450.newInstance(MainActivity.this, watch);

							boolean equalsToGoal = lifeTrakUpdateR450.isGoalsEqualToApp(mLifeTrakSyncR450.mGoals.get(mLifeTrakSyncR450.mGoals.size() - 1));
							boolean equalsToTimeDate = lifeTrakUpdateR450.isTimeDateEqualToApp(mLifeTrakSyncR450.mTimeDate);
							boolean equalsToCalibrationData = lifeTrakUpdateR450.isCalibrationEqualToApp(mLifeTrakSyncR450.mCalibrationData);
							boolean equalsToWakeupSettings = lifeTrakUpdateR450.isWakeupSettingEqualToApp(mLifeTrakSyncR450.mWakeupSetting);
							boolean equalsToDaylightDetectSettings = lifeTrakUpdateR450.isDaylightSettingEqualToApp(mLifeTrakSyncR450.mDayLightDetectSetting);
							boolean equalsToNightlightSettings = lifeTrakUpdateR450.isNightlightSettingEqualToApp(mLifeTrakSyncR450.mNightLightDetectSetting);
							boolean equalsToActivityAlertSettings = lifeTrakUpdateR450.isActivitySettingEqualToApp(mLifeTrakSyncR450.mActivityAlertSetting);
							boolean equalsToProfileSettings = lifeTrakUpdateR450.isProfileSettingEqualToApp(mLifeTrakSyncR450.mUserProfile);

							if (!equalsToGoal || !equalsToTimeDate || !equalsToCalibrationData || !equalsToWakeupSettings || !equalsToDaylightDetectSettings ||
									!equalsToNightlightSettings || !equalsToActivityAlertSettings || !equalsToProfileSettings) {
								if (mPreferenceWrapper.getPreferenceBooleanValue(DO_NOT_SHOW_PROMPT_DIALOG)) {


									new Thread(new Runnable() {

										@Override
										public void run() {

											Watch watch;
											try {
												watch = (Watch) mLifeTrakApplication.getSelectedWatch().clone();
												watch.setGoals(mLifeTrakSyncR450.mGoals);
												watch.setTimeDate(mLifeTrakSyncR450.mTimeDate);
												watch.setCalibrationData(mLifeTrakSyncR450.mCalibrationData);
												watch.setWakeupSetting(mLifeTrakSyncR450.mWakeupSetting);
												watch.setDayLightDetectSetting(mLifeTrakSyncR450.mDayLightDetectSetting);
												watch.setNightLightDetectSetting(mLifeTrakSyncR450.mNightLightDetectSetting);
												watch.setActivityAlertSetting(mLifeTrakSyncR450.mActivityAlertSetting);
												watch.setUserProfile(mLifeTrakSyncR450.mUserProfile);

												LifeTrakUpdateR450 lifeTrakUpdateR450 = LifeTrakUpdateR450.newInstance(MainActivity.this, watch);

												lifeTrakUpdateR450.updateAllSettingsFromWatch(mLifeTrakApplication);
												mLifeTrakApplication.setUserProfile(lifeTrakUpdateR450.userProfileForCurrentWatch());
												mLifeTrakApplication.setTimeDate(lifeTrakUpdateR450.timeDateForCurrentWatch());

												mHandler.postDelayed(new Runnable() {
													@Override
													public void run() {

														if (mContentFragment instanceof MyAccountFragment){
															((MyAccountFragment) mContentFragment).initializeObjects();
														}

														initializeSyncToServer();
													}
												}, 750);


											} catch (CloneNotSupportedException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									}).start();
								}
								else{
									mProgressDialog.dismiss();
									Intent intent = new Intent(MainActivity.this, ProfileSelectR450Activity.class);
									startActivityForResult(intent, REQUEST_CODE_PROFILE_SELECT_R450);
								}
							} else {
								new Thread(new Runnable() {

									@Override
									public void run() {
										try {

											TimeDate timeDateApp = lifeTrakUpdateR450.timeDateForCurrentWatch();
											if (timeDateApp != null && mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME))
												lifeTrakUpdateR450.updateTimeDateFromApp(mSalutronService, timeDateApp);

										} catch (InterruptedException e) {

											e.printStackTrace();
										}

										mHandler.postDelayed(new Runnable() {
											public void run() {
												if (mContentFragment instanceof DashboardFragment) {
													DashboardFragment fragment = (DashboardFragment) mContentFragment;
													fragment.initializeObjects();
												}

												initializeSyncToServer();
											}
										}, 1000);
									}

								}).start();

							}

						}
						else{
							if (mProgressDialog == null)
								reinitializeProgress();

							new Thread(new Runnable() {
								@Override
								public void run() {

									Watch watch;
									try {
										watch = (Watch) mLifeTrakApplication.getSelectedWatch().clone();
										watch.setGoals(mLifeTrakSyncR450.mGoals);
										watch.setTimeDate(mLifeTrakSyncR450.mTimeDate);
										watch.setCalibrationData(mLifeTrakSyncR450.mCalibrationData);
										watch.setWakeupSetting(mLifeTrakSyncR450.mWakeupSetting);
										watch.setDayLightDetectSetting(mLifeTrakSyncR450.mDayLightDetectSetting);
										watch.setNightLightDetectSetting(mLifeTrakSyncR450.mNightLightDetectSetting);
										watch.setActivityAlertSetting(mLifeTrakSyncR450.mActivityAlertSetting);
										watch.setUserProfile(mLifeTrakSyncR450.mUserProfile);

										LifeTrakUpdateR450 lifeTrakUpdateR450 = LifeTrakUpdateR450.newInstance(MainActivity.this, watch);

										lifeTrakUpdateR450.updateAllSettingsFromWatch(mLifeTrakApplication);
										mLifeTrakApplication.setUserProfile(lifeTrakUpdateR450.userProfileForCurrentWatch());
										mLifeTrakApplication.setTimeDate(lifeTrakUpdateR450.timeDateForCurrentWatch());

										mHandler.postDelayed(new Runnable() {
											@Override
											public void run() {
												if (mContentFragment instanceof MyAccountFragment){
													((MyAccountFragment) mContentFragment).initializeObjects();
												}

												initializeSyncToServer();
											}
										}, 750);


									} catch (CloneNotSupportedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}).start();

						}
					}

//                  else{
//                      if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
////                            mLifeTrakApplication.setTimeDate(mLifeTrakSyncR420.getTimeDate());
////                            mLifeTrakApplication.setUserProfile(mLifeTrakSyncR420.getUserProfile());
////                            mLifeTrakApplication.setSelectedWatch(mLifeTrakSyncR420.getWatch());
////                            mLifeTrakApplication.setCurrentDate(new Date());
//                          if (mContentFragment instanceof DashboardFragment || mContentFragment instanceof MyAccountFragment) {
//
//                          }
//                      }
//
//                      mHandler.postDelayed(new Runnable() {
//                          public void run() {
//
//                              if (mContentFragment instanceof DashboardFragment) {
//                                  DashboardFragment fragment = (DashboardFragment) mContentFragment;
//                                  fragment.initializeObjects();
//                              }
//                              initializeSyncToServer();
//                          }
//                      }, 1000);
//
//                  }
				} else {
					if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
//                      mLifeTrakApplication.setTimeDate(mLifeTrakSyncR420.getTimeDate());
//                      mLifeTrakApplication.setUserProfile(mLifeTrakSyncR420.getUserProfile());
//                      mLifeTrakApplication.setCurrentDate(new Date());
						try {
							watch.setWatchFirmWare(mLifeTrakSyncR420.firmwareVersion);
							watch.setWatchSoftWare(mLifeTrakSyncR420.softwareRevision);
							watch.setContext(MainActivity.this);
							watch.update();
						}
						catch (Exception e){
							LifeTrakLogger.info("Error:" + e.getLocalizedMessage());
						}
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (mMenuFragment instanceof MenuFragment) {
									updateLastSyncDate();
								}
							}
						});

						if (mContentFragment instanceof DashboardFragment || mContentFragment instanceof MyAccountFragment) {
							{
								final LifeTrakUpdateR420 lifeTrakUpdateR420 = LifeTrakUpdateR420.newInstance(MainActivity.this, watch);

								boolean equalsToGoal = lifeTrakUpdateR420.isGoalsEqualToApp(mLifeTrakSyncR420.getGoal());
								boolean equalsToTimeDate = lifeTrakUpdateR420.isTimeDateEqualToApp(mLifeTrakSyncR420.getTimeDate());
								boolean equalsToCalibrationData = lifeTrakUpdateR420.isCalibrationEqualToApp(mLifeTrakSyncR420.getCalibrationData());
								boolean equalsToProfileSettings = lifeTrakUpdateR420.isProfileSettingEqualToApp(mLifeTrakSyncR420.getUserProfile());

								if (!equalsToGoal || !equalsToTimeDate || !equalsToCalibrationData || !equalsToProfileSettings) {
									if (mPreferenceWrapper.getPreferenceBooleanValue(DO_NOT_SHOW_PROMPT_DIALOG)) {
										new Thread(new Runnable() {

											@Override
											public void run() {

												Watch watch;
												try {
													watch = (Watch) mLifeTrakApplication.getSelectedWatch().clone();
													List<Goal> mGoals = new ArrayList<Goal>();
													mGoals.add(mLifeTrakSyncR420.getGoal());
													watch.setGoals(mGoals);
													watch.setTimeDate(mLifeTrakSyncR420.getTimeDate());
													watch.setCalibrationData(mLifeTrakSyncR420.getCalibrationData());
													watch.setUserProfile(mLifeTrakSyncR420.getUserProfile());
//

													LifeTrakUpdateR420 lifeTrakUpdateR420 = LifeTrakUpdateR420.newInstance(MainActivity.this, watch);

													lifeTrakUpdateR420.updateAllSettingsFromWatch(mLifeTrakApplication, mLifeTrakSyncR420.getWorkoutSettings());
													mLifeTrakApplication.setTimeDate(mLifeTrakSyncR420.getTimeDate());
													mLifeTrakApplication.setUserProfile(mLifeTrakSyncR420.getUserProfile());
													//mLifeTrakApplication.setCurrentDate(new Date());

													mHandler.postDelayed(new Runnable() {
														@Override
														public void run() {

															if (mContentFragment instanceof MyAccountFragment){
																((MyAccountFragment) mContentFragment).initializeObjects();
															}

															initializeSyncToServer();
														}
													}, 750);


												} catch (CloneNotSupportedException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
										}).start();
									}
									else{
										mProgressDialog.dismiss();
										Intent intent = new Intent(MainActivity.this, ProfileSelectR420Activity.class);
										startActivityForResult(intent, REQUEST_CODE_PROFILE_SELECT_R420);
									}
								} else {
									new Thread(new Runnable() {

										@Override
										public void run() {
											try {

												TimeDate timeDateApp = lifeTrakUpdateR420.timeDateForCurrentWatch();
												if (timeDateApp != null && mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME))
													lifeTrakUpdateR420.updateTimeDateFromApp(mSalutronService, timeDateApp, mLifeTrakSyncR420.getTimeDate());

											} catch (InterruptedException e) {

												e.printStackTrace();
											}

											mHandler.postDelayed(new Runnable() {
												public void run() {
													if (mContentFragment instanceof DashboardFragment) {
														DashboardFragment fragment = (DashboardFragment) mContentFragment;
														fragment.initializeObjects();
													}

													initializeSyncToServer();
												}
											}, 1000);
										}

									}).start();

								}

							}
						}
					}

//

				}
			}
		}).start();
	}


	@Override
	public void onSyncLightDataPoints(int percent) { }

	@Override
	public void onSyncWorkoutStopDatabase(int percent) { }

	@Override
	public void onSyncWakeupSetting() { }

	@Override
	public void onSyncNotifications() { }

	@Override
	public void onSyncActivityAlertSettingsData() { }

	@Override
	public void onSyncDayLightSettingsData() { }

	@Override
	public void onSyncNightLightSettingsData() { }

	@Override
	public void onError(int status) {
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();

		if (status == SALStatus.ERROR_NOT_INITIALIZED) {
			LifeTrakLogger.info("on error not initialized");
		} else if (status == SALStatus.ERROR_CHECKSUM) {
			mAlertDialog.setMessage("Error checksum");
			mAlertDialog.show();
		} else if(status == SALStatus.ERROR_NOT_SUPPORTED) {
			mAlertDialog.setMessage(getString(R.string.device_not_supported));
			mAlertDialog.show();
		} else if(status == SALStatus.ERROR_HARDWARE_PROBLEM) {
			//onPairTimeout();
			syncingWatchFailed();
		}
	}

	public static class BluetoothSearchReceiver extends BroadcastReceiver {

		public BluetoothSearchReceiver() { }

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (mLifeTrakSyncR450 != null && mLifeTrakSyncR450.getBLEService() != null) {
				//if (mLifeTrakSyncR450.isDisconnected()) {
				if (mLifeTrakSyncR450.getConnectedDevice() == null) {
					LifeTrakLogger.info("searching bluetooth...");
					mOperation = OPERATION_SEARCH_BLUETOOTH;
					//mLifeTrakSyncR450.stopScan();
					//mLifeTrakSyncR450.startScan();

					/*if (mWatchConnected) {
						mLifeTrakSyncR450.connectToDevice(mLifeTrakApplication.getSelectedWatch().getMacAddress(), WATCHMODEL_R415);
					}*/
				} /*else if (mWatchConnected) {
                    mWatchConnected = false;
                    mOperation = OPERATION_SEARCH_BLUETOOTH;
                    mLifeTrakSyncR450.connectToDevice(mLifeTrakApplication.getSelectedWatch().getMacAddress(), WATCHMODEL_R415);
                }*/
			}
		}
	}

	private boolean isAllowNotificationListener() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			ContentResolver contentResolver = getContentResolver();
			String strListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
			String appID = getPackageName();

			return strListeners != null && strListeners.contains(appID);
		}

		return true;
	}

	private void checkNotificationListener() {

		if (!isAllowNotificationListener() && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
			AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.lifetrak_title)
					.setMessage(R.string.allow_notification)
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							showCheckNotification = true;
							dialog.dismiss();
							if (mContentFragment instanceof  DashboardFragment){
								DashboardFragment dashboardFragment = (DashboardFragment) mContentFragment;
								dashboardFragment.showGoogleFitDialog();
							}

						}
					})
					.setPositiveButton(R.string.continue_button_text, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							showCheckNotification = true;
							dialog.dismiss();
							if (mContentFragment instanceof  DashboardFragment){
								DashboardFragment dashboardFragment = (DashboardFragment) mContentFragment;
								dashboardFragment.showGoogleFitDialog();
							}

							Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
							startActivity(intent);
						}
					}).create();
			alert.show();
		}
		else{
			if (mContentFragment instanceof  DashboardFragment){
				DashboardFragment dashboardFragment = (DashboardFragment) mContentFragment;
				dashboardFragment.showGoogleFitDialog();
			}
		}
	}



	private void updateAllDataHeaders() {
		if (mLifeTrakApplication != null && mLifeTrakApplication.getSelectedWatch() != null) {
			Calendar calendarNow = Calendar.getInstance();
			calendarNow.setTime(new Date());

			int day = calendarNow.get(Calendar.DAY_OF_MONTH);
			int month = calendarNow.get(Calendar.MONTH) + 1;
			int year = calendarNow.get(Calendar.YEAR) - 1900;

			List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(this)
					.getReadOperation()
					.query("watchDataHeader = ? and syncedToCloud = 0", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
					.getResults(StatisticalDataHeader.class);

			if (dataHeaders.size() > 0) {
				for (StatisticalDataHeader dataHeader : dataHeaders) {
					if (!(dataHeader.getDateStampDay() == day && dataHeader.getDateStampMonth() == month && dataHeader.getDateStampYear() == year)) {
						if (getApplicationContext() != null) {
							dataHeader.setContext(this);
							dataHeader.setWatch(mLifeTrakApplication.getSelectedWatch());
							dataHeader.setSyncedToCloud(true);
							dataHeader.update();
						}
					}
				}
			}

			List<SleepDatabase> sleepDatabases = DataSource.getInstance(this)
					.getReadOperation()
					.query("watchSleepDatabase = ? and syncedToCloud = 0"
							, String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
					.getResults(SleepDatabase.class);

			for (SleepDatabase sleepDatabase : sleepDatabases) {
				sleepDatabase.setContext(this);
				sleepDatabase.setWatch(mLifeTrakApplication.getSelectedWatch());
				sleepDatabase.setSyncedToCloud(true);
				sleepDatabase.update();
			}

			List<WorkoutHeader> workoutHeaders = DataSource.getInstance(this)
					.getReadOperation()
					.query("watchWorkoutHeader = ? and syncedToCloud = 0", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
					.getResults(WorkoutHeader.class);

			for (WorkoutHeader workoutHeader : workoutHeaders) {
				workoutHeader.setContext(this);
				workoutHeader.setWatch(mLifeTrakApplication.getSelectedWatch());
				workoutHeader.setSyncedToCloud(true);
				workoutHeader.update();
			}

			List<WorkoutInfo> workoutInfos = DataSource.getInstance(this)
					.getReadOperation()
					.query("watchWorkoutInfo = ? and syncedToCloud = 0", String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()))
					.getResults(WorkoutInfo.class);

			for (WorkoutInfo workoutInfo : workoutInfos) {
				workoutInfo.setContext(this);
				workoutInfo.setWatch(mLifeTrakApplication.getSelectedWatch());
				workoutInfo.setSyncedToCloud(true);
				workoutInfo.update();
			}
		}
	}

	public void updateTimeDate() {
		mIsUpdateTimeDate = true;

		if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415 && mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME)) {
			if (mLifeTrakSyncR450 != null) {
				mLifeTrakSyncR450.setIsUpdateTimeAndDate(true);
				mLifeTrakSyncR450.getCurrentTimeAndDate();
				mIsUpdateTimeDate = false;
			}
		}
		else if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420 && mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME)){
			if (mLifeTrakSyncR420 != null) {
				mLifeTrakSyncR420.registerHandler();
				mLifeTrakSyncR420.setIsUpdateTimeAndDate(true);
				mLifeTrakSyncR420.getCurrentTimeAndDate();
				mIsUpdateTimeDate = false;
			}
		}
		else {
			mSalutronService.getCurrentTimeAndDate();
		}

	}

	@Override
	protected void onNewIntent( Intent intent ) {
		LifeTrakLogger.info("onNewIntent(), intent = " + intent);
		if (intent.getExtras() != null)
		{
			LifeTrakLogger.info("in onNewIntent = " + intent.getExtras().getString("test"));
		}
		super.onNewIntent( intent );
		setIntent( intent );
	}

	public void setAlarm() {
		boolean isWeekly = mPreferenceWrapper.getPreferenceBooleanValue(SYNC_WEEK_VALUE);
		long time = mPreferenceWrapper.getPreferenceLongValue(TIME_ALERT);

		Date date = new Date(time);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
		int currentMinute = calendar.get(Calendar.MINUTE);

		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(date);

		int hour = calendar2.get(Calendar.HOUR_OF_DAY);
		int minute = calendar2.get(Calendar.MINUTE);

		if (hour < currentHour || (hour <= currentHour && minute < currentMinute)) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);

		if (isWeekly) {
			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
		} else {
			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
		}
	}

	public void removeAlarm() {
		if (pendingIntent != null) {
			mAlarmManager.cancel(pendingIntent);
		}
	}

	private int getInterval(boolean isweekly, int scheadule){
		if (isweekly){
			int seconds = 60;
			int milliseconds = 1000;
			int weekMinute = 10080;
			int repeatMS = seconds * weekMinute * milliseconds;
			return repeatMS;
		}
		else
		{
			int seconds = 60;
			int milliseconds = 1000;
			int dayMinute = 1440;
			int repeatMS = seconds * dayMinute * milliseconds;
			return repeatMS;
		}

	}

	private boolean hasOverlapOrDeletedSleepDatabase(SleepDatabase sleepDatabase) {
		String query = "watchSleepDatabase = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ? " +
				"and ((? >= hourSleepStart and ? <= hourSleepEnd) or (? >= hourSleepStart and ? <= hourSleepEnd))";

		long watchSleepDatabase = mLifeTrakApplication.getSelectedWatch().getId();
		int dateStampYear = sleepDatabase.getDateStampYear();
		int dateStampMonth = sleepDatabase.getDateStampMonth();
		int dateStampDay = sleepDatabase.getDateStampDay();
		int hourSleepStart = sleepDatabase.getHourSleepStart();
		int hourSleepEnd = sleepDatabase.getHourSleepEnd();
		int minuteSleepStart = sleepDatabase.getMinuteSleepStart();
		int minuteSleepEnd = sleepDatabase.getMinuteSleepEnd();

		List<SleepDatabase> sleepDatabases = DataSource.getInstance(this)
				.getReadOperation()
				.query(query, String.valueOf(watchSleepDatabase), String.valueOf(dateStampYear), String.valueOf(dateStampMonth), String.valueOf(dateStampDay),
						String.valueOf(hourSleepStart), String.valueOf(hourSleepStart), String.valueOf(hourSleepEnd), String.valueOf(hourSleepEnd))
				.getResults(SleepDatabase.class);

		if (sleepDatabases.size() > 0)
			return true;

		query = "watchSleepDatabase = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ? and " +
				"minuteSleepStart = ? and hourSleepStart = ? and minuteSleepEnd = ? and hourSleepEnd = ?";

		List<SleepDatabaseDeleted> sleepDatabaseDeleted = DataSource.getInstance(this)
				.getReadOperation()
				.query(query, String.valueOf(watchSleepDatabase), String.valueOf(dateStampYear), String.valueOf(dateStampMonth), String.valueOf(dateStampDay),
						String.valueOf(minuteSleepStart), String.valueOf(hourSleepStart), String.valueOf(minuteSleepEnd), String.valueOf(hourSleepEnd))
				.getResults(SleepDatabaseDeleted.class);

		if (sleepDatabaseDeleted.size() > 0)
			return true;

		return false;
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

	public void reinitializeProgress() {
		mProgressDialog = new ProgressDialog(MainActivity.this);
		//		mProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		//		mProgressDialog.getWindow().setDimAmount(0.5f);
		mProgressDialog.setTitle(R.string.lifetrak_title);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R415) {
					if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R420)
						mSalutronService.disconnectFromDevice();
					else{
						if (mLifeTrakSyncR420!= null)
							mLifeTrakSyncR420.getBLEService().disconnectFromDevice();
					}
				}
//				else if (mLifeTrakApplication.getSelectedWatch().getModel() != WATCHMODEL_R420){
//					if (mLifeTrakSyncR420!= null)
//						mLifeTrakSyncR420.getBLEService().disconnectFromDevice();
//				}

				if (flag_sync){
					flag_sync = false;
					mTimeoutHandler.removeCallbacksAndMessages(watchSyncRunnable);
				}
				if (flag_sync_no_bluetooth){
					flag_sync_no_bluetooth = false;
					mTimeoutHandler.removeCallbacksAndMessages(watchSyncRunnable2);
				}

				if (mContentFragment instanceof WatchSettingsFragment){
					WatchSettingsFragment mWatchSettingsFragment = (WatchSettingsFragment) mContentFragment;
					mWatchSettingsFragment.removeCallback();
				}

				if (mContentFragment instanceof GoalFragment){
					GoalFragment mGoalFragment = (GoalFragment) mContentFragment;
					mGoalFragment.removeCallback();
				}

				try {
					mServerSyncAsyncTask.cancel(true);
				}
				catch (Exception e){
					LifeTrakLogger.info("mServerSyncAsyncTask error on cancel:" + e.getLocalizedMessage());
				}

				mErrorHandler.removeCallbacks(mErrorRunnable);
				cancelSync();
			}
		});
	}

	public void C300C410SyncSuccess(boolean isSuccess){
		counter = 0;
		this.flag_finished_syncing = isSuccess;

	}


	public void hideSoftKeyboard() {
		if(getCurrentFocus()!=null) {
			//InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
	}


	private final Runnable mErrorRunnable = new Runnable() {
		@Override
		public void run() {
			if (mLifeTrakApplication.getSelectedWatch() != null && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
				mLifeTrakSyncR450.stopScan();
			}
			else if (mLifeTrakApplication.getSelectedWatch() != null && mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420){
				mLifeTrakSyncR420.stopScan();
			}
			else {
				mSalutronService.stopScan();
			}

			if (!deviceFound) {
				mProgressDialog.dismiss();
				//onPairTimeout();
				syncingWatchFailed();
			}

			deviceFound = false;
		}
	};

	private void syncProfileServer(){
		LifeTrakLogger.info("Syncing Profile to cloud ");
		if (NetworkUtil.getInstance(MainActivity.this).isNetworkAvailable()) {
			mCurrentOperation = OPERATION_EDIT_PROFILE;

			if (mProgressDialog == null)
				reinitializeProgress();

			if (!mProgressDialog.isShowing())
				mProgressDialog.show();
			String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);

			UserProfile userProfile = mLifeTrakApplication.getUserProfile();

			if (null != userProfile.getProfileImageLocal() && !"".equals(userProfile.getProfileImageLocal())) {
				File file = new File(userProfile.getProfileImageLocal());

				if (file.exists()) {
					orientProfileImage(userProfile.getProfileImageLocal());
					mBitmap = createSquareBitmap(mBitmap);
					mPath = userProfile.getProfileImageLocal();
				}
			}


			mEditProfileAsync.url(API_URL + USER_UPDATE_URI).addParam("access_token", accessToken).addParam("first_name",userProfile.getFirstname())
					.addParam("last_name", userProfile.getLastname()).addParam("email", mPreferenceWrapper.getPreferenceStringValue(EMAIL));
			if (mBitmap != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				if (mPath != null) {
					if (mPath.endsWith(".png")) {
						orientProfileImage(mPath);
						mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
					} else {
						mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					}
				}

				mEditProfileAsync.addParam("image", stream.toByteArray());
			}

			mEditProfileAsync.post();

		}

		else {
			if(isFinishing()) {
				NetworkUtil.getInstance(MainActivity.this).showConnectionErrorMessage();
			}
			else{
				Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
				startActivity(intent);
			}
			if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();
		}

	}


	private void orientProfileImage(String imgPath) {
		try {
			ExifInterface exif = new ExifInterface(imgPath);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			int rotateXDegrees = 0;
			if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
				rotateXDegrees = 90;
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
				rotateXDegrees = 180;
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
				rotateXDegrees = 270;
			}
			mBitmap = BitmapFactory.decodeFile(imgPath);
			mBitmap = BaseFragment.shrinkBitmap(mBitmap, 300, rotateXDegrees);
		} catch (IOException e) {
			LifeTrakLogger.error(e.getMessage());
		}
	}

	private Bitmap createSquareBitmap(Bitmap src) {
		Bitmap bitmap = null;

		if (src.getWidth() >= src.getHeight()) {
			bitmap = Bitmap.createBitmap(src, (src.getWidth() / 2) - (src.getHeight() / 2), 0, src.getHeight(), src.getHeight());
		} else {
			bitmap = Bitmap.createBitmap(src, 0, (src.getHeight() / 2) - (src.getWidth() / 2), src.getWidth(), src.getWidth());
		}

		return bitmap;
	}

	private void uploadS3(Watch watch, StatisticalDataHeader dataHeader) throws  JSONException{
		JSONObject data = new JSONObject();
		data.put("device", mServerSyncAsyncAmazon.getDevice(watch.getMacAddress(),watch));
		data.put("workout", mServerSyncAsyncAmazon
				.getAllWorkoutInfos(watch.getId(), mPreferenceWrapper.getPreferenceBooleanValue(FROM_IOS), watch, dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
		data.put("sleep", mServerSyncAsyncAmazon
				.getAllSleepDatabases(watch.getId(), dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
		data.put("data_header", mServerSyncAsyncAmazon
				.getAllDataHeaders(watch, dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
		data.put("device_settings", mServerSyncAsyncAmazon.getDeviceSettings(watch.getId(), mLifeTrakApplication));
		data.put("user_profile", mServerSyncAsync.getUserProfile(watch.getId()));
		data.put("goal", mServerSyncAsyncAmazon.getAllGoals(watch.getId(), dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
		data.put("sleep_settings", mServerSyncAsyncAmazon.getSleepSetting(watch.getId()));
		data.put("wakeup_info", mServerSyncAsyncAmazon.getWakeupInfo(watch.getId()));

		if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R415) {
			data.put("wakeup_info", mServerSyncAsyncAmazon.getWakeupInfo(watch.getId()));
			data.put("inactive_alert_settings", mServerSyncAsyncAmazon.getActivityAlertSetting(watch.getId()));
			data.put("light_settings", mServerSyncAsyncAmazon.getLightSetting(watch.getId()));
		}
		if (mLifeTrakApplication.getSelectedWatch().getModel() == WATCHMODEL_R420) {
			data.put("workout_header", mServerSyncAsyncAmazon.getAllWorkoutHeaders(watch.getId(), dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
		}

		AmazonTransferUtility
				.getInstance(MainActivity.this)
				.listener(this)
				.setUUID(uuid)
				.uploadFileToAmazonS3(data.toString(), dataHeader.getDateStamp());
	}
}