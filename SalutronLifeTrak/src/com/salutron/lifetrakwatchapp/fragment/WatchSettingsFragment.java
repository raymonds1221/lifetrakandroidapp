package com.salutron.lifetrakwatchapp.fragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.flurry.android.FlurryAgent;
import com.salutron.blesdk.SALActivityAlertSetting;
import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALCalibration;
import com.salutron.blesdk.SALConnectionSetting;
import com.salutron.blesdk.SALDayLightDetectSetting;
import com.salutron.blesdk.SALDynamicWorkoutInfo;
import com.salutron.blesdk.SALNightLightDetectSetting;
import com.salutron.blesdk.SALSleepDatabase;
import com.salutron.blesdk.SALSleepSetting;
import com.salutron.blesdk.SALStatisticalDataHeader;
import com.salutron.blesdk.SALStatisticalDataPoint;
import com.salutron.blesdk.SALStatus;
import com.salutron.blesdk.SALTimeDate;
import com.salutron.blesdk.SALUserProfile;
import com.salutron.blesdk.SALWakeupSetting;
import com.salutron.blesdk.SALWorkoutSetting;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.NotificationSettingActivity;
import com.salutron.lifetrakwatchapp.WelcomePageActivity;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.ActivityAlertSetting;
import com.salutron.lifetrakwatchapp.model.CalibrationData;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.NightLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.WakeupSetting;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.WorkoutSettings;
import com.salutron.lifetrakwatchapp.util.DateTimeUtil;
import com.salutron.lifetrakwatchapp.util.DeviceScanListener;
import com.salutron.lifetrakwatchapp.util.Gallery;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;
import com.salutron.lifetrakwatchapp.util.StringUtils;
import com.salutron.lifetrakwatchapp.view.BetterSeekBar;
import com.salutron.lifetrakwatchapp.view.HeightPickerView;

public class WatchSettingsFragment extends BaseFragment implements DeviceScanListener, OnClickListener, OnCheckedChangeListener {

	public static final int IMAGE_CAPTURE_REQUEST = 100;
	private String imageFilename;
	private String imagePath;
	private int displayedChild;
	private Gallery galleryAdapter = new Gallery();
	private ViewFlipper viewFlipper;
	private CalibrationData calibrationData;
	private SharedPreferences prefs;
	private BetterSeekBar mWalkSeekBar;
	private TextView mWalkCalibration;
	private BetterSeekBar mCalorieSeekBar;
	private TextView mCalorieCalibration;
	private RadioGroup mStepsCalibrationGroup;
	private Switch mAutoEL;
	private RadioGroup mTwoDateFormat;
	private TextView mDateFormat;
	private LinearLayout mDateFormatLayout;
	private LinearLayout mDateFormatForCModel;
	private RadioGroup mTimeFormat;
	private RadioGroup mGenderGroup;
	private RadioGroup mUnitSystemGroup;
	private RadioGroup mPromptSettings;
	private RadioGroup mWatchDisplayGroup;
	private LinearLayout mWatchDisplay;
	private int mHeightValueInCm;
	private RelativeLayout mSmartCalibrationButton;

	private TextView mTextviewUnitSelection;

	private Switch mPromptAlertSwitch;

	private TextView mLastSyncDate;
	private TextView mWatchName;
	private EditText mDeviceName;
	private ImageView mImageviewWatch;
	private TextView mTextviewWatchDisplay;

	private Button mButtonSyncToCloud;

	private Switch mSyncToCloudSwitch;
	private TextView mTextViewSwitchWatch;
	private Button mButtonSyncSettingWatch;
	private Button mButtonUpdateFirmware;

	private boolean flag_reset_workout = false;
	private boolean flag_update_firmware = false;

	private boolean flag_watchname_is_empty = false;

	private TextView textviewWorkoutStorageLeft;

	private TextView mTextviewHourFormat;

	private TextView textviewHRLoggingRate;

	private int  statusUpdate = SALStatus.NO_ERROR;
	private final SimpleDateFormat mDateFormatDate = (SimpleDateFormat) DateFormat.getInstance();

    private Activity mActivity;

	private List<NightLightDetectSetting> nightLightDetectSetting;
	private static NightLightDetectSetting mNightLightDetectSetting;

	private List<DayLightDetectSetting> dayLightDetectSettings;
	private static DayLightDetectSetting mDayLightDetectSetting;

	private WorkoutSettings mWorkoutSettings;

	private List<WakeupSetting> wakeSettings;
	private static WakeupSetting mWakeupSetting;

	private List<ActivityAlertSetting> alertSettings;
	private static ActivityAlertSetting mActivityAlertSettings;
	private TextView textviewWorkOutReconnectTime;

	private TextView textViewResetWorkOut;

	private Handler handler;

	private boolean isCancelledSyncing = false;

	private static MainActivity mMainActivity;


	private HeightPickerView mHeightPickerView;

	private Button mAlertSettings;
	private RelativeLayout mNotification;
	private Switch mDataSyncReminderAlert;

	private TableLayout mDataSyncReminderAlertGroup;
	private Switch mAutoSyncTime;

	private BluetoothDevice mDevice;
	private SALBLEService mService;
	private Watch mWatch;

	// once a day
	private TableRow mOnceADAyTableRow;
	private TextView mOnceADay;
	private TimePickerDialog mTimePicker;
	private Boolean mIsOnceADay;

	// once a week
	private TableRow mOnceAWeekTableRow;
	private LinearLayout mOnceAWeekLayout;
	private TextView mOnceAWeekDay;
	private TextView mOnceAWeekTime;
	private Boolean mIsOnceAWeek;
	private NumberPicker weekPicker;
	private Dialog alertDialog;
	private TimePicker alertTimePicker;
	private String day;
	private int wkHour;
	private int wkMin;
	private String wkAM_PM;
	private LifeTrakApplication mApp;

	// pref
	public static final String ONCEADAY = "onceADay";
	public static final String ONCEAWEEK = "onceAWeek";
	public static final String ONCEAWEEKDAYSELECTED = "onceAWeekDaySelected";

	private String lastWatchName;


	public boolean canBackPressed() {
		return viewFlipper.getDisplayedChild() == 0;
	}

	public void handleBackPressed() {
		setHasOptionsMenu(false);
		viewFlipper.setDisplayedChild(0);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);
		mApp = (LifeTrakApplication) getActivity().getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_watch_settings, null);
		return v;
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			if (mDeviceName.getText().toString().trim().equals("")) {
				getLifeTrakApplication().getSelectedWatch().setName(lastWatchName);
				getLifeTrakApplication().getSelectedWatch().update();
				mWatchName.setText(lastWatchName);
				mDeviceName.setText(lastWatchName);
			} else {
				getLifeTrakApplication().getSelectedWatch().setName(mDeviceName.getText().toString());
				getLifeTrakApplication().getSelectedWatch().update();
				lastWatchName = mDeviceName.getText().toString();
			}
		}
		catch (Exception e){
			LifeTrakLogger.info("Error"+ e.getLocalizedMessage());
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mMainActivity = (MainActivity) getActivity();
		((MainActivity)getActivity()).hideSoftKeyboard();

		FlurryAgent.logEvent("Settings_Page");

		handler = new Handler();
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

		mHeightPickerView = new HeightPickerView(getActivity());
		mHeightPickerView.setOnSelectHeightListener(mSelectHeightListener);

		viewFlipper = (ViewFlipper) getView().findViewById(R.id.watch_settings_flipper);

		mStepsCalibrationGroup = (RadioGroup) getView().findViewById(R.id.rdgStepsCalibration);
		mWalkCalibration = (TextView) getView().findViewById(R.id.smart_distance_value);
		mWalkSeekBar = (BetterSeekBar) getView().findViewById(R.id.sbrWalkCalibration);
		mCalorieCalibration = (TextView) getView().findViewById(R.id.smart_calorie_value);
		mCalorieSeekBar = (BetterSeekBar) getView().findViewById(R.id.sbrCaloriesCalibration);

		mAutoEL = (Switch) getView().findViewById(R.id.smart_auto_el);
		mAutoEL.setVisibility(View.GONE);

		mTwoDateFormat = (RadioGroup) getView().findViewById(R.id.rdgDateFormat);
		mDateFormat = (TextView) getView().findViewById(R.id.textview_datetime);

		mDateFormat.setOnClickListener(this);
		mTimeFormat = (RadioGroup) getView().findViewById(R.id.rdgTimeFormat);
		mGenderGroup = (RadioGroup) getView().findViewById(R.id.rdgGender);
		mUnitSystemGroup = (RadioGroup) getView().findViewById(R.id.rdgUnitPrefs);
		mPromptSettings = (RadioGroup) getView().findViewById(R.id.rdgPrompt);
		mWatchDisplayGroup = (RadioGroup) getView().findViewById(R.id.rdgWatchDisplayPrefs);

		mAlertSettings = (Button) getView().findViewById(R.id.alert_settings);

		mDataSyncReminderAlert = (Switch) getView().findViewById(R.id.data_sync_reminder_alert_switch);
		if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415)
			mDataSyncReminderAlert.setVisibility(View.VISIBLE);
		else
			mDataSyncReminderAlert.setVisibility(View.GONE);

		mDataSyncReminderAlertGroup = (TableLayout) getView().findViewById(R.id.data_sync_reminder_alert_group);
		mAutoSyncTime = (Switch) getView().findViewById(R.id.auto_sync_time_switch);
		mNotification = (RelativeLayout) getView().findViewById(R.id.relative_notification);
		mNotification.setOnClickListener(this);

		mButtonUpdateFirmware = (Button) getView().findViewById(R.id.button_update_watch);
		mButtonUpdateFirmware.setOnClickListener(this);

		mButtonSyncToCloud = (Button) getView().findViewById(R.id.button_sync_to_cloud);
		mButtonSyncToCloud.setOnClickListener(this);

		mSyncToCloudSwitch = (Switch) getView().findViewById(R.id.sync_to_cloud_switch);
		boolean autoSync = mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC);
		if (!autoSync)
		{
			mButtonSyncToCloud.setVisibility(View.GONE);
			(getView().findViewById(R.id.line_for_button_sync_to_cloud)).setVisibility(View.GONE);
		}

		mSyncToCloudSwitch.setChecked(autoSync);
		mSyncToCloudSwitch.setOnCheckedChangeListener(this);

		mTextViewSwitchWatch = (TextView) getView().findViewById(R.id.textView_switch_watch);
		mTextViewSwitchWatch.setOnClickListener(this);

		mWatchDisplay = (LinearLayout) getView().findViewById(R.id.watch_display);
		mDateFormatLayout = (LinearLayout) getView().findViewById(R.id.date_format_linearlayout);
		mDateFormatLayout = (LinearLayout) getView().findViewById(R.id.date_format_linearlayout);
		mDateFormatForCModel = (LinearLayout) getView().findViewById(R.id.lnrDateFormatForCModel);

		textviewHRLoggingRate = (TextView)getView().findViewById(R.id.textview_hr_logging_rate);
		textviewHRLoggingRate.setOnClickListener(this);
		try {
			mButtonSyncSettingWatch = (Button) getView().findViewById(R.id.button_sync_setting_watch);
			mButtonSyncSettingWatch.setOnClickListener(this);
		}catch (Exception e){
			LifeTrakLogger.info("Error" + e.getLocalizedMessage());
		}
		mTextviewWatchDisplay = (TextView) getView().findViewById(R.id.textview_watch_display_watch_settings);
		mTextviewWatchDisplay.setOnClickListener(this);
		// once a day
		mOnceADAyTableRow = (TableRow) getView().findViewById(R.id.once_aday_row);
		mOnceADay = (TextView) getView().findViewById(R.id.onceaday_txv);
		mOnceADAyTableRow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPreferenceWrapper.setPreferenceBooleanValue(SalutronLifeTrakUtility.SYNC_WEEK_VALUE, false).synchronize();
				//				mOnceADay.setVisibility(View.VISIBLE);
				//				mOnceAWeekTime.setVisibility(View.GONE);
				//				mOnceAWeekDay.setVisibility(View.GONE);
				setIsOnceADay(true);
				checkSyncReminderValue();
			}
		});
		mOnceADay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickOnceAday();
			}
		});

		mTextviewHourFormat = (TextView) getView().findViewById(R.id.textview_hour_format);
		mTextviewHourFormat.setOnClickListener(this);

		mTextviewUnitSelection = (TextView) getView().findViewById(R.id.textview_unit_selection);
		mTextviewUnitSelection.setOnClickListener(this);

		textviewWorkoutStorageLeft = (TextView) getView().findViewById(R.id.textview_workout_storage_left);
		textviewWorkOutReconnectTime = (TextView)getView().findViewById(R.id.textview_workout_reconnect_timeout);
		textviewWorkOutReconnectTime.setOnClickListener(this);

		textViewResetWorkOut = (TextView) getView().findViewById(R.id.textView_reset_workout);
		textViewResetWorkOut.setOnClickListener(this);

		if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420){

			List<WorkoutSettings> workoutSettingsList = DataSource
					.getInstance(getActivity())
					.getReadOperation()
					.query("watchDataHeader = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
					.getResults(WorkoutSettings.class);

			if (!workoutSettingsList.isEmpty()) {
				WorkoutSettings workoutSettings = workoutSettingsList.get(0);
				int prefLogging = workoutSettings.getHrLoggingRate();
				int loggingRate = 1;
				if (prefLogging > 0){
					loggingRate = prefLogging;
				}
				if (workoutSettings.getDatabaseUsageMax() > 0) {
					float storageLeft = workoutSettings.getDatabaseUsageMax() - workoutSettings.getDatabaseUsage();
					storageLeft -= 46.0f;
					storageLeft = storageLeft * loggingRate;
					storageLeft /= 1.125f;
					storageLeft /= 3600.0f;
					if (storageLeft > 0)
						textviewWorkoutStorageLeft.setText(String.format("%.2f", storageLeft) + " hours");
					else
						textviewWorkoutStorageLeft.setText("0");
				}

				if (workoutSettings.getReconnectTime() != 0){
					String subText = " sec";
					if (workoutSettings.getReconnectTime() > 1){
						subText = " secs";
					}

					textviewWorkOutReconnectTime.setText(String.valueOf(workoutSettings.getReconnectTime()) + subText);
				}
			}
		}


		// once a week
		mOnceAWeekTableRow = (TableRow) getView().findViewById(R.id.once_aweek_row);
		mOnceAWeekLayout = (LinearLayout) getView().findViewById(R.id.once_aweek_layout);
		mOnceAWeekTime = (TextView) getView().findViewById(R.id.onceaweek_time_txv);
		mOnceAWeekDay = (TextView) getView().findViewById(R.id.onceaweek_day_txv);
		mOnceAWeekTableRow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPreferenceWrapper.setPreferenceBooleanValue(SalutronLifeTrakUtility.SYNC_WEEK_VALUE, true).synchronize();
				//				mOnceADay.setVisibility(View.GONE);
				//				mOnceAWeekTime.setVisibility(View.VISIBLE);
				//				mOnceAWeekDay.setVisibility(View.VISIBLE);
				//				if (StringUtils.isBlank(mOnceAWeekTime.getText().toString())) {
				//					DateFormat df = new SimpleDateFormat("HH:mm aa");
				//					String initialTime = DateTimeUtil.getTimeCorrectFormat(df.format(new Date()), getLifeTrakApplication());
				//					mOnceAWeekTime.setText(initialTime);
				//					mOnceAWeekDay.setText(DateTimeUtil.getCurrentDay(false));
				//				}
				setIsOnceADay(false);
				checkSyncReminderValue();
			}
		});
		mOnceAWeekLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickOnceAWeek();
			}
		});

		mSmartCalibrationButton = (RelativeLayout) getView().findViewById(R.id.relative_smart_calibration);
		mSmartCalibrationButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (calibrationData == null)
					return;

				setHasOptionsMenu(false);
				viewFlipper.setDisplayedChild(1);

				displayCalibration();
			}
		});


		/*
		 * if (getLifeTrakApplication().getSelectedWatch().getModel() ==
		 * WATCHMODEL_C300) { mSmartCalibrationButton.setVisibility(View.GONE);
		 * } else { mSmartCalibrationButton.setVisibility(View.VISIBLE); }
		 */
		//mAutoEL.setVisibility(View.GONE);


		mWalkSeekBar.setMinimumValue(-25);
		mWalkSeekBar.setMaximumValue(25);
		mCalorieSeekBar.setMinimumValue(-25);
		mCalorieSeekBar.setMaximumValue(25);

		final Bundle args = getArguments();
		calibrationData = args.getParcelable("calibrationData");

		mPromptAlertSwitch = (Switch) getView().findViewById(R.id.prompt_alert_switch);

		boolean doNotShowPrompt = mPreferenceWrapper.getPreferenceBooleanValue(DO_NOT_SHOW_PROMPT_DIALOG);

		if (doNotShowPrompt) {
			mPromptSettings.check(R.id.radPromptNo);
			mPromptAlertSwitch.setChecked(false);
		} else {
			mPromptSettings.check(R.id.radPromptYes);
			mPromptAlertSwitch.setChecked(true);
		}

		switch (getLifeTrakApplication().getTimeDate().getDisplaySize()) {
		case DISPLAY_FORMAT_SMALL_DIGIT:
			mWatchDisplayGroup.check(R.id.WatchDisplayprefs_unit_option2);
			mTextviewWatchDisplay.setText(getString(R.string.caption_full));
			break;
		case DISPLAY_FORMAT_BIG_DIGIT:
			mTextviewWatchDisplay.setText(getString(R.string.caption_simple));
			mWatchDisplayGroup.check(R.id.WatchDisplayprefs_unit_option1);
			break;
		}
		if (getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_12_HR){
			mTimeFormat.check(R.id.unitprefs_time_option1);
		}
		else
			mTimeFormat.check(R.id.unitprefs_time_option2);

		if (calibrationData == null) {
			final LifeTrakApplication app = (LifeTrakApplication) getActivity().getApplication();

			List<CalibrationData> calibrationDataList = DataSource.getInstance(getActivity()).getReadOperation().query("watchCalibrationData = ?", String.valueOf(app.getSelectedWatch().getId()))
					.getResults(CalibrationData.class);

			if (!calibrationDataList.isEmpty()) {
				calibrationData = calibrationDataList.get(0);
			}
		}

		if (calibrationData != null){
			calibrationData.setAutoEL(SALCalibration.AUTO_EL_ON);
			calibrationData.update();
		}


		if (calibrationData != null) {
			mWalkSeekBar.setFloatValue(calibrationData.getDistanceCalibrationWalk());
			mWalkSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar arg0) {
				}

				@Override
				public void onStartTrackingTouch(SeekBar arg0) {
				}

				@Override
				public void onProgressChanged(SeekBar arg0, int value, boolean arg2) {
					int walkValue = (int) mWalkSeekBar.getFloatValue();

					if (walkValue > 0) {
						mWalkCalibration.setText("+" + walkValue + "%");
					} else if (walkValue == 0) {
						mWalkCalibration.setText(walkValue + "%");
					} else {
						mWalkCalibration.setText(walkValue + "%");
					}
					calibrationData.setDistanceCalibrationWalk(walkValue);
					calibrationData.update();
				}
			});

			int walkValue = calibrationData.getDistanceCalibrationWalk();

			if (walkValue > 0) {
				mWalkCalibration.setText("+" + walkValue + "%");
			} else if (walkValue == 0) {
				mWalkCalibration.setText(walkValue + "%");
			} else {
				mWalkCalibration.setText(walkValue + "%");
			}

			mCalorieSeekBar.setFloatValue(calibrationData.getCaloriesCalibration());
			mCalorieSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar arg0) {
				}

				@Override
				public void onStartTrackingTouch(SeekBar arg0) {
				}

				@Override
				public void onProgressChanged(SeekBar arg0, int value, boolean arg2) {
					int calorieValue = (int) mCalorieSeekBar.getFloatValue();

					if (calorieValue > 0) {
						mCalorieCalibration.setText("+" + calorieValue + "%");
					} else if (calorieValue == 0) {
						mCalorieCalibration.setText(calorieValue + "%");
					} else {
						mCalorieCalibration.setText(calorieValue + "%");
					}
					calibrationData.setCaloriesCalibration(calorieValue);
					calibrationData.update();
				}
			});

			int calorieValue = calibrationData.getCaloriesCalibration();

			if (calorieValue > 0) {
				mCalorieCalibration.setText("+" + calorieValue + "%");
			} else if (calorieValue == 0) {
				mCalorieCalibration.setText(calorieValue + "%");
			} else {
				mCalorieCalibration.setText(calorieValue + "%");
			}
		}

		mGenderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				switch (arg1) {
				case R.id.urprofile_male_option:
					getLifeTrakApplication().getUserProfile().setGender(GENDER_MALE);
					break;
				case R.id.urprofile_female_option:
					getLifeTrakApplication().getUserProfile().setGender(GENDER_FEMALE);
					break;
				}
				getLifeTrakApplication().getUserProfile().update();
			}
		});

		mTwoDateFormat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_R415) {
					switch (arg1) {
					case R.id.unitprefs_date_option1:
						getLifeTrakApplication().getTimeDate().setDateFormat(DATE_FORMAT_MMDD);
						break;
					case R.id.unitprefs_date_option2:
						getLifeTrakApplication().getTimeDate().setDateFormat(DATE_FORMAT_DDMM);
						break;
					}

					getLifeTrakApplication().getTimeDate().update();
				}
			}
		});

		if (getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_12_HR){
			mTimeFormat.check(R.id.unitprefs_time_option2);
			mTextviewHourFormat.setText(getString(R.string.caption_12_hour));
		}
		else {
			mTimeFormat.check(R.id.unitprefs_time_option1);
			mTextviewHourFormat.setText(getString(R.string.caption_24_hour));
		}

		mTimeFormat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				switch (arg1) {
				case R.id.unitprefs_time_option1:
					getLifeTrakApplication().getTimeDate().setHourFormat(TIME_FORMAT_12_HR);
					setDataSyncReminderAlertTimeFormat(TIME_FORMAT_12_HR);
					break;
				case R.id.unitprefs_time_option2:
					getLifeTrakApplication().getTimeDate().setHourFormat(TIME_FORMAT_24_HR);
					setDataSyncReminderAlertTimeFormat(TIME_FORMAT_24_HR);
					break;
				}
				getLifeTrakApplication().getTimeDate().update();

				MainActivity activity = (MainActivity) getActivity();
				activity.updateLastSyncDate();
			}
		});

		mUnitSystemGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				switch (arg1) {
				case R.id.unitprefs_unit_option1:
					getLifeTrakApplication().getUserProfile().setUnitSystem(UNIT_IMPERIAL);
					break;
				case R.id.unitprefs_unit_option2:
					getLifeTrakApplication().getUserProfile().setUnitSystem(UNIT_METRIC);
					break;
				}
				getLifeTrakApplication().getUserProfile().update();
				fetchUserProfile();
			}
		});

		mPromptSettings.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				mPreferenceWrapper.setPreferenceBooleanValue(DO_NOT_SHOW_PROMPT_DIALOG, arg1 != R.id.radPromptYes).synchronize();
			}
		});

		mPromptAlertSwitch.setOnCheckedChangeListener(this);

		mStepsCalibrationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				if (calibrationData != null) {
					switch (arg1) {
						case R.id.smart_default:
							calibrationData.setStepCalibration(SALCalibration.STEP_DEFAULT);
							break;
						case R.id.smart_optionA:
							calibrationData.setStepCalibration(SALCalibration.STEP_OPTION_A);
							break;
						case R.id.smart_optionB:
							calibrationData.setStepCalibration(SALCalibration.STEP_OPTION_B);
							break;
					}
					calibrationData.update();
				}
			}
		});

		mAutoEL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (calibrationData != null) {
					calibrationData.setAutoEL((arg1) ? SALCalibration.AUTO_EL_ON : SALCalibration.AUTO_EL_OFF);
					calibrationData.update();
				}
			}
		});

		mWatchDisplayGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				//				case R.id.WatchDisplayprefs_unit_option1:
				//					getLifeTrakApplication().getTimeDate().setDisplaySize(DISPLAY_FORMAT_SMALL_DIGIT);
				//					break;
				//				case R.id.WatchDisplayprefs_unit_option2:
				//					getLifeTrakApplication().getTimeDate().setDisplaySize(DISPLAY_FORMAT_BIG_DIGIT);
				//					break;
				case R.id.WatchDisplayprefs_unit_option2:
					getLifeTrakApplication().getTimeDate().setDisplaySize(DISPLAY_FORMAT_SMALL_DIGIT);
					break;
				case R.id.WatchDisplayprefs_unit_option1:
					getLifeTrakApplication().getTimeDate().setDisplaySize(DISPLAY_FORMAT_BIG_DIGIT);
					break;
				}
				getLifeTrakApplication().getTimeDate().update();
			}
		});

		mImageviewWatch = (ImageView)getView().findViewById(R.id.imageview_watch);
		switch (getLifeTrakApplication().getSelectedWatch().getModel()) {
			case WATCHMODEL_C300:
				mImageviewWatch.setImageResource(R.drawable.watch_c300_green);
				break;
			case WATCHMODEL_C300_IOS:
				mImageviewWatch.setImageResource(R.drawable.watch_c300_green);
				break;
			case WATCHMODEL_C410:
				mImageviewWatch.setImageResource(R.drawable.watch_c410_red);
				break;
			case WATCHMODEL_R415:
				mImageviewWatch.setImageResource(R.drawable.watch_r415_blue);
				break;
			case WATCHMODEL_R500:
				mImageviewWatch.setImageResource(R.drawable.watch_r500_black);
				break;
			case WATCHMODEL_R420:
				mImageviewWatch.setImageResource(R.drawable.r420);
				break;
		}
		if (getLifeTrakApplication().getSelectedWatch().getLastSyncDate() == null){
			LifeTrakLogger.info("mWatch.getCloudLastSyncDate() is NULL on Settings Fragment");
			getLifeTrakApplication().getSelectedWatch().setCloudLastSyncDate(new Date());
			getLifeTrakApplication().getSelectedWatch().update();
		}
		mLastSyncDate = (TextView) getView().findViewById(R.id.textView_last_date_synced);
		if (mDateFormatDate.format(getLifeTrakApplication().getSelectedWatch().getLastSyncDate()).equals(mDateFormatDate.format(new Date()))) {
			mLastSyncDate.setText(getString(R.string.synced_today, mDateFormatDate.format(getLifeTrakApplication().getSelectedWatch().getLastSyncDate())));
		} else {
			mLastSyncDate.setText(getString(R.string.synced_at, mDateFormatDate.format(getLifeTrakApplication().getSelectedWatch().getLastSyncDate())));
		}

		lastWatchName = getLifeTrakApplication().getSelectedWatch().getName();
		mWatchName = (TextView) getView().findViewById(R.id.textView_watchname);
		mDeviceName = (EditText) getView().findViewById(R.id.textview_devicename_caption);
		mWatchName.setText(getLifeTrakApplication().getSelectedWatch().getName());
		mDeviceName.setText(getLifeTrakApplication().getSelectedWatch().getName());
		mDeviceName.setSelection(mDeviceName.getText().length());
		mDeviceName.setOnEditorActionListener(mWatchNameEditorAction());
		mDeviceName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (!editable.toString().trim().equals("")) {
//					getLifeTrakApplication().getSelectedWatch().setName(editable.toString());
//					getLifeTrakApplication().getSelectedWatch().update();
					mWatchName.setText(editable);
					flag_watchname_is_empty = false;

				} else {
					flag_watchname_is_empty = true;

					mWatchName.setText("");
				}
			}
		});


		mDeviceName.clearFocus();
		((LinearLayout) getView().findViewById(R.id.focus_linear)).requestFocus();

		setViewObservers();
		fetchUserProfile();
		hideCalendar();
		displayCalibration();
		hideViews();



		if (savedInstanceState != null) {
			imagePath = savedInstanceState.getString(IMAGE_PATH);
			displayedChild = savedInstanceState.getInt(WATCH_SETTINGS_CHILD);
		}

		if (displayedChild == 1)
			setHasOptionsMenu(false);
		else
			setHasOptionsMenu(false);

		// if (getLifeTrakApplication().getSelectedWatch().getModel() ==
		// WATCHMODEL_R415) {
		// setHasOptionsMenu(false);
		// setR450ViewsVisibility(View.VISIBLE);
		// } else {
		// setHasOptionsMenu(true);
		// setR450ViewsVisibility(View.GONE);
		// }

		viewFlipper.setDisplayedChild(displayedChild);

		if (getActivity() != null) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		mAutoSyncTime.setOnCheckedChangeListener(this);
		mDataSyncReminderAlert.setOnCheckedChangeListener(this);

		getSettingsData();
		populateViews();
		getWorkoutSettingsData();
		//hideViews();

		if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420){
			if (mWorkoutSettings != null) {
				switch (mWorkoutSettings.getHrLoggingRate()) {
					case 1:
						textviewHRLoggingRate.setText("1 sec");
						break;
					case 2:
						textviewHRLoggingRate.setText("2 secs");
						break;
					case 3:
						textviewHRLoggingRate.setText("3 secs");
						break;
					case 4:
						textviewHRLoggingRate.setText("4 secs");
						break;
					case 5:
						textviewHRLoggingRate.setText("5 secs");
						break;

				}
			}
		}


	}

	// Switching status
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.data_sync_reminder_alert_switch:
			if (mDataSyncReminderAlert.isChecked()) {
				mDataSyncReminderAlertGroup.setVisibility(View.VISIBLE);

				mPreferenceWrapper.setPreferenceBooleanValue(SalutronLifeTrakUtility.AUTOSYNCTIME, true).synchronize();;

				//				if (StringUtils.isBlank(mOnceADay.getText().toString()) && StringUtils.isBlank(mOnceAWeekTime.getText().toString())) {
				//					DateFormat df = new SimpleDateFormat("HH:mm aa");
				//					String initialTime = DateTimeUtil.getTimeCorrectFormat(df.format(new Date()), getLifeTrakApplication());
				//					mOnceADay.setText(initialTime);
				//					setIsOnceADay(true);
				//
				//				}
				//((MainActivity)getActivity()).setAlarm();
			} else {

				mDataSyncReminderAlertGroup.setVisibility(View.GONE);
				mPreferenceWrapper.setPreferenceBooleanValue(SalutronLifeTrakUtility.AUTOSYNCTIME, false).synchronize();
				//((MainActivity)getActivity()).removeAlarm();
			}

			//			Editor edit = prefs.edit();
			//			edit.putBoolean(AUTOSYNCTIME, mDataSyncReminderAlert.isChecked());
			//			edit.commit();
			break;
		case R.id.auto_sync_time_switch:
			mPreferenceWrapper.setPreferenceBooleanValue(AUTO_SYNC_TIME, isChecked).synchronize();
			break;
		case R.id.prompt_alert_switch:
			mPreferenceWrapper.setPreferenceBooleanValue(DO_NOT_SHOW_PROMPT_DIALOG, !isChecked).synchronize();
			break;
		case R.id.sync_to_cloud_switch:
			mPreferenceWrapper.setPreferenceBooleanValue(AUTO_SYNC, isChecked).synchronize();
			getView().findViewById(R.id.button_sync_to_cloud).setVisibility((isChecked) ? View.VISIBLE : View.GONE);
			getView().findViewById(R.id.line_for_button_sync_to_cloud).setVisibility((isChecked) ? View.VISIBLE : View.GONE);

			break;

		}
	}

	private void setIsOnceADay(boolean isOnceAday) {

		if (isOnceAday == true) {
			mOnceADay.setVisibility(View.VISIBLE);
			mOnceAWeekTime.setVisibility(View.GONE);
			mOnceAWeekDay.setVisibility(View.GONE);
		} else {
			mOnceADay.setVisibility(View.GONE);
			mOnceAWeekTime.setVisibility(View.VISIBLE);
			mOnceAWeekDay.setVisibility(View.VISIBLE);
		}
		//setDataSyncReminderAlertTimeFormat(getLifeTrakApplication().getTimeDate().getHourFormat());
	}

	private void onClickOnceAWeek() {

		alertDialog = new Dialog(getActivity());
		alertDialog.setTitle(mOnceAWeekTime.getText().toString() + " - " + mOnceAWeekDay.getText().toString());
		alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.WHITE));
		alertDialog.setContentView(R.layout.date_picker_weekly_custom_layout);

		alertDialog.findViewById(R.id.date_picker_weekly_set).setOnClickListener(alertListener);
		alertDialog.findViewById(R.id.date_picker_weekly_cancel).setOnClickListener(alertListener);

		// Hours Minutes
		alertTimePicker = (TimePicker) alertDialog.findViewById(R.id.date_picker_weekly_time_picker);

		Date fDate = DateTimeUtil.convertTimeToDate(mOnceAWeekTime.getText().toString(), getLifeTrakApplication());
		Calendar fcalendar = Calendar.getInstance();
		fcalendar.setTime(fDate);
		int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
		int fminutes = fcalendar.get(Calendar.MINUTE);
		alertTimePicker.setCurrentHour(fhours);
		alertTimePicker.setCurrentMinute(fminutes);

		alertTimePicker.setIs24HourView(getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_24_HR ? true : false);
		alertTimePicker.setOnTimeChangedListener(onTimeChangedListener);

		// Days
		String nums[] = getResources().getStringArray(R.array.days);
		weekPicker = (NumberPicker) alertDialog.findViewById(R.id.date_picker_weekly_number_picker);
		weekPicker.setMaxValue(nums.length - 1);
		weekPicker.setMinValue(0);
		weekPicker.setWrapSelectorWheel(false);
		weekPicker.setDisplayedValues(nums);
		weekPicker.setValue(Arrays.asList(nums).indexOf(mOnceAWeekDay.getText().toString()));
		weekPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		alertDialog.show();
		setIsOnceADay(false);

		mOnceADay.setVisibility(View.GONE);
	}

	private void onClickOnceAday() {
		Date fDate = DateTimeUtil.convertTimeToDate(mOnceADay.getText().toString(), getLifeTrakApplication());
		Calendar fcalendar = Calendar.getInstance();
		fcalendar.setTime(fDate);
		int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
		int fminutes = fcalendar.get(Calendar.MINUTE);

		boolean is24HourView = getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_24_HR ? true : false;
        if(getActivity() != null) {
            mTimePicker = new TimePickerDialog(getActivity(), mOnTimeSetListener, fhours, fminutes, is24HourView);
            mTimePicker.show();
        }
        else
        {
            if (mActivity != null){
                mTimePicker = new TimePickerDialog(mActivity, mOnTimeSetListener, fhours, fminutes, is24HourView);
                mTimePicker.show();
            }

        }


		mOnceADay.setVisibility(View.VISIBLE);
		mOnceAWeekTime.setVisibility(View.GONE);
		mOnceAWeekDay.setVisibility(View.GONE);

		setIsOnceADay(true);
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

	private void populateViews() {
		// Getting save pref data
		if (prefs.contains(ONCEADAY)) {
			mOnceADay.setText(prefs.getString(ONCEADAY, ""));
		}
		if (prefs.contains(ONCEAWEEK)) {
			mOnceAWeekTime.setText(prefs.getString(ONCEAWEEK, ""));
			mOnceAWeekDay.setText(prefs.getString(ONCEAWEEKDAYSELECTED, ""));
		}

		setIsOnceADay(!StringUtils.isBlank(mOnceADay.getText().toString()));


		if (mTextviewHourFormat.getText().toString().equals(getString(R.string.caption_12_hour)))
			setDataSyncReminderAlertTimeFormat(TIME_FORMAT_12_HR);
		else
			setDataSyncReminderAlertTimeFormat(TIME_FORMAT_24_HR);


		switch (mTimeFormat.getCheckedRadioButtonId()) {
		case R.id.unitprefs_time_option1:
			setDataSyncReminderAlertTimeFormat(TIME_FORMAT_12_HR);
			break;
		case R.id.unitprefs_time_option2:
			setDataSyncReminderAlertTimeFormat(TIME_FORMAT_24_HR);
			break;
		}





		if(!mPreferenceWrapper.getPreferenceBooleanValue(SalutronLifeTrakUtility.AUTOSYNCTIME)){
			mDataSyncReminderAlertGroup.setVisibility(View.GONE);
		}
		mAutoSyncTime.setChecked(mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME));
		checkSyncReminderValue();
	}

	/**
	 *
	 * set the time format of DataSycnReminderAlert
	 *
	 * @param timeFormat
	 *            - SalutronLifeTrakUtility.TIME_FORMAT_12_HR or
	 *            SalutronLifeTrakUtility.TIME_FORMAT_12_HR
	 * */
	private void setDataSyncReminderAlertTimeFormat(int timeFormat) {
		if (null != mIsOnceADay) {
			if (mIsOnceADay == true) {
				String onceADayTime = DateTimeUtil.getTimeCorrectFormat(mOnceADay.getText().toString(), getLifeTrakApplication());
				mOnceADay.setText(onceADayTime);
			} else {
				String onceAWeekTime = DateTimeUtil.getTimeCorrectFormat(mOnceAWeekTime.getText().toString(), getLifeTrakApplication());
				mOnceAWeekTime.setText(onceAWeekTime);
			}
		}
	}

	private void setR450ViewsVisibility(int visibility) {
		mAlertSettings.setVisibility(visibility);
		mNotification.setVisibility(visibility);
		mDataSyncReminderAlert.setVisibility(visibility);
		mDataSyncReminderAlertGroup.setVisibility(visibility);
		mAutoSyncTime.setVisibility(visibility);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (viewFlipper != null) {
			outState.putString(IMAGE_PATH, imagePath);
			outState.putInt(WATCH_SETTINGS_CHILD, viewFlipper.getDisplayedChild());
		}
	}

	private void setViewObservers() {
		EditText value = (EditText) getView().findViewById(R.id.urprofile_weight_value);
		value.addTextChangedListener(mTextWatcherWeight);
		value.setOnFocusChangeListener(mWeightFocusChangeListener);
		value.setOnEditorActionListener(mWeightActionListener);

		value = (EditText) getView().findViewById(R.id.urprofile_height_value);
		value.addTextChangedListener(mTextWatcherHeight);
		value.setOnFocusChangeListener(mHeightFocusChangeListener);
		value.setOnEditorActionListener(mHeightActionListener);
		value.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final LifeTrakApplication app = (LifeTrakApplication) getActivity().getApplication();
				final UserProfile userProfile = app.getUserProfile();

				if (userProfile.getUnitSystem() == UNIT_IMPERIAL) {
					mHeightPickerView.setValue(userProfile.getHeight());
					mHeightPickerView.show();
				}
			}
		});

		mAlertSettings.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				// Go to AlertSettings Fragment
				AlertSettingsFragment alertSettings = new AlertSettingsFragment();
				switchFragment(alertSettings);
			}
		});

	}

	private void switchFragment(final SherlockFragment fragment) {
		final MainActivity activity = ((MainActivity) getActivity());
		activity.getSupportFragmentManager().beginTransaction().detach(fragment).setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).replace(R.id.frmContentFrame, fragment).attach(fragment)
		.addToBackStack("fragment_tag1").commit();
	}

	private void displayCalibration() {
		// Initialize radio buttons.
		final RadioButton defaultBtn = ((RadioButton) getView().findViewById(R.id.smart_default));
		final RadioButton optionABtn = ((RadioButton) getView().findViewById(R.id.smart_optionA));
		final RadioButton optionBBtn = ((RadioButton) getView().findViewById(R.id.smart_optionB));

		// Add listeners to each radio button to correctly toggle them.  (workaround for putting radio group in table layout)
		OnCheckedChangeListener stepsRadioBtnListener = createStepsRadioBtnListener(defaultBtn, optionABtn, optionBBtn);
		defaultBtn.setOnCheckedChangeListener(stepsRadioBtnListener);
		optionABtn.setOnCheckedChangeListener(stepsRadioBtnListener);
		optionBBtn.setOnCheckedChangeListener(stepsRadioBtnListener);

		if (calibrationData != null) {
			switch (calibrationData.getStepCalibration()) {
			case SALCalibration.STEP_DEFAULT:
				defaultBtn.setChecked(true);
				optionABtn.setChecked(false);
				optionBBtn.setChecked(false);
				break;
			case SALCalibration.STEP_OPTION_A:
				optionABtn.setChecked(true);
				defaultBtn.setChecked(false);
				optionBBtn.setChecked(false);
				break;
			case SALCalibration.STEP_OPTION_B:
				optionBBtn.setChecked(true);
				defaultBtn.setChecked(false);
				optionABtn.setChecked(false);
				break;
			}

			mAutoEL.setChecked(calibrationData.getAutoEL() == SALCalibration.AUTO_EL_ON);

			if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_R415){
				((TableLayout)getView().findViewById(R.id.tbl_calories)).setVisibility(View.GONE);
			}

		}
	}



	private OnCheckedChangeListener createStepsRadioBtnListener(
			final RadioButton defaultBtn, final RadioButton optionABtn,
			final RadioButton optionBBtn) {
		return new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (calibrationData != null) {
					switch (buttonView.getId()) {
					case R.id.smart_default:
						if (isChecked) {
							optionABtn.setChecked(false);
							optionBBtn.setChecked(false);
							calibrationData.setStepCalibration(SALCalibration.STEP_DEFAULT);
						}
						break;
					case R.id.smart_optionA:
						if (isChecked) {
							defaultBtn.setChecked(false);
							optionBBtn.setChecked(false);
							calibrationData.setStepCalibration(SALCalibration.STEP_OPTION_A);
						}
						break;
					case R.id.smart_optionB:
						if (isChecked) {
							defaultBtn.setChecked(false);
							optionABtn.setChecked(false);
							calibrationData.setStepCalibration(SALCalibration.STEP_OPTION_B);
						}
						break;
					}
					calibrationData.update();
				}
			}
		};
	}

	private void fetchUserProfile() {
		final SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy hh:mm aa", Locale.ENGLISH);
		final UserProfile userProfile = mApp.getUserProfile();
		final View fragmentView = getView();

		final long millis = prefs.getLong("lastSyncDate", -1);
		final Date dt;

		if (millis > 0) {
			dt = new Date(millis);
		} else {
			dt = mApp.getSelectedWatch().getLastSyncDate();
		}
		/*
		 * TextView textView = (TextView) fragmentView
		 * .findViewById(R.id.urwatch_lastsync);
		 * textView.setText(df.format(dt));
		 *
		 * textView = (TextView) fragmentView.findViewById(R.id.urwatch_name);
		 * textView.setText(app.getSelectedWatch().getName());
		 */
		// fetchWatchProfilePic(app);
		if (userProfile != null)
			fetchProfileInfo(userProfile, fragmentView);
		fetchUnit(mApp, fragmentView);
	}

	private void fetchUnit(final LifeTrakApplication app, final View fragmentView) {
		final TimeDate timeDate = app.getTimeDate();

		if (timeDate != null) {
			final int timeFormat = timeDate.getHourFormat();

			switch (timeFormat) {
			case SALTimeDate.FORMAT_12HOUR:
				((RadioButton) fragmentView.findViewById(R.id.unitprefs_time_option1)).setChecked(true);
				mTextviewHourFormat.setText(getString(R.string.caption_12_hour));
				break;
			default:
				((RadioButton) fragmentView.findViewById(R.id.unitprefs_time_option2)).setChecked(true);
				mTextviewHourFormat.setText(getString(R.string.caption_24_hour));
			}

			final int dateFormat = timeDate.getDateFormat();

			if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415){
				setDateFormat();
			}
			else{
				switch (dateFormat) {
					case SALTimeDate.FORMAT_MMDD:
						mDateFormat.setText(getString(R.string.unitprefs_date_option1_caption));
						break;
					default:
						mDateFormat.setText(getString(R.string.unitprefs_date_option2_caption));
						break;
				}
			}
		}
	}

	private void fetchProfileInfo(final UserProfile userProfile, final View fragmentView) {
		TextView value = (TextView) fragmentView.findViewById(R.id.urprofile_weight_value);

		double weight = userProfile.getWeight();
		double height = userProfile.getHeight();

		switch (userProfile.getUnitSystem()) {
		case UNIT_IMPERIAL:
		//	((RadioButton) fragmentView.findViewById(R.id.unitprefs_unit_option1)).setChecked(true);
			mTextviewUnitSelection.setText(getString(R.string.caption_unit_imperial));
			double feetValue = Math.floor(height / FEET_CM);
			double inchValue = (height / INCH_CM) - (feetValue * 12);

			if (Math.round(inchValue) == 12) {
				feetValue++;
				inchValue = 0;
			}

			value.setText(String.format("%d", (int) weight));
			value.setHint(value.getText());

			((TextView) fragmentView.findViewById(R.id.urprofile_weight_measurement)).setText(" lbs");

			value = (TextView) fragmentView.findViewById(R.id.urprofile_height_value);
			value.setText(String.format("%d' %02d\"", (int) feetValue, Math.round(inchValue)));
			value.setHint(value.getText());
			value.setFocusable(false);

			((TextView) fragmentView.findViewById(R.id.urprofile_height_measurement)).setText("");

			break;
		case UNIT_METRIC:
			mTextviewUnitSelection.setText(getString(R.string.caption_unit_metric));

			value.setText(String.format("%d", Math.round(weight * KG)));
			value.setHint(value.getText());

			((TextView) fragmentView.findViewById(R.id.urprofile_weight_measurement)).setText(" kg");

			value = (TextView) fragmentView.findViewById(R.id.urprofile_height_value);
			value.setText(String.format("%d", (int) height));
			value.setHint(value.getText());
			value.setFocusableInTouchMode(true);
			value.setFocusable(true);

			((TextView) fragmentView.findViewById(R.id.urprofile_height_measurement)).setText(" cm");

			break;
		}

		final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, userProfile.getBirthDay());
		calendar.set(Calendar.MONTH, userProfile.getBirthMonth() - 1);
		calendar.set(Calendar.YEAR, userProfile.getBirthYear());
		final Date dob = calendar.getTime();

		value = (TextView) fragmentView.findViewById(R.id.urprofile_dob_value);
		value.setText(dateFormat.format(dob));
		value.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new DatePickerDialog(getActivity(), mDateSetListener, userProfile.getBirthYear(), userProfile.getBirthMonth() - 1, userProfile.getBirthDay()).show();
			}
		});

		switch (userProfile.getGender()) {
		case SALUserProfile.MALE:
			((RadioButton) fragmentView.findViewById(R.id.urprofile_male_option)).setChecked(true);
			break;
		default:
			((RadioButton) fragmentView.findViewById(R.id.urprofile_female_option)).setChecked(true);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.watch_settings_menu, menu);
	}

	@Override
	public void onDeviceConnected(final BluetoothDevice device, final SALBLEService service, final Watch watch) {
		if (mDeviceName.getText().toString().trim().equals("")){
			getLifeTrakApplication().getSelectedWatch().setName(lastWatchName);
			getLifeTrakApplication().getSelectedWatch().update();
			mWatchName.setText(lastWatchName);
			mDeviceName.setText(lastWatchName);
		}
		else{
			getLifeTrakApplication().getSelectedWatch().setName(mDeviceName.getText().toString());
			getLifeTrakApplication().getSelectedWatch().update();
			lastWatchName = mDeviceName.getText().toString();
		}

		mDevice = device;
		mService = service;
		mWatch = watch;
		if (!flag_reset_workout && !flag_update_firmware) {
			flag_reset_workout = false;
			flag_update_firmware = false;
			doUpdate(device, service, watch);
		}
		else if (flag_update_firmware == true && !flag_reset_workout){
			flag_reset_workout = false;
			flag_update_firmware = false;
			updateFirmWare(device,service,watch);
		}
		else{
			flag_reset_workout = false;
			flag_update_firmware = false;
			final MainActivity activity = (MainActivity) getActivity();
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
				handler.postDelayed(new Runnable() {
					public void run() {
						LifeTrakLogger.info("Resetting R420 workoutdatabase");
						int status = service.resetWorkoutDatabase();



						if (status == SALStatus.NO_ERROR){
							int databaseUsage = 0;
							int databaseUsageMax = 48342;

							mWorkoutSettings.setDatabaseUsageMax(databaseUsageMax);
							mWorkoutSettings.setDatabaseUsage(databaseUsage);
							mWorkoutSettings.setContext(getActivity());
							float storageLeft = databaseUsageMax - databaseUsage;
							storageLeft -= 46.0f;
							storageLeft = storageLeft * mWorkoutSettings.getHrLoggingRate();
							storageLeft /= 1.125f;
							storageLeft /= 3600.0f;

							if (storageLeft >= 0)
								textviewWorkoutStorageLeft.setText(String.format("%.2f", storageLeft) + " hours");
							else
								textviewWorkoutStorageLeft.setText("0");
							disconnectC300C410watch(service, status, activity);
						}

					}
				}, 2000);
			}
			else{
				handler.postDelayed(new Runnable() {
					public void run() {
						LifeTrakLogger.info("Resetting R420 workoutdatabase");
						int status = service.resetWorkoutDatabase();

						if (status == SALStatus.NO_ERROR){
							int databaseUsage = 0;
							int databaseUsageMax = 48342;

							mWorkoutSettings.setDatabaseUsageMax(databaseUsageMax);
							mWorkoutSettings.setDatabaseUsage(databaseUsage);
							mWorkoutSettings.setContext(getActivity());
							float storageLeft = databaseUsageMax - databaseUsage;
							storageLeft -= 46.0f;
							storageLeft = storageLeft * mWorkoutSettings.getHrLoggingRate();
							storageLeft /= 1.125f;
							storageLeft /= 3600.0f;
							if (storageLeft >= 0)
								textviewWorkoutStorageLeft.setText(String.format("%.2f", storageLeft) + " hours");
							else
								textviewWorkoutStorageLeft.setText("0");

							disconnectC300C410watch(service, status, activity);
						}
					}
				}, SYNC_DELAY);
			}
		}

	}

	private boolean doUpdate(final BluetoothDevice device, final SALBLEService service, final Watch watch) {
		LifeTrakLogger.info("Start Syncing on Settings");
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
			//lollipop
			handler.postDelayed(new Runnable() {
				public void run() {
					updateProfile(device, service, watch);
				}
			}, 2000);


			handler.postDelayed(new Runnable() {
				public void run() {

					boolean autoSyncTime = mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME);
					if (autoSyncTime) {
						LifeTrakLogger.info("Update Time and Date From Settings");
						mMainActivity.updateTimeDate();
					}
				}
			}, 4000);

			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					updateTimeAndDateFormat(device, service, watch);
				}
			}, 6000);
		}
		else {
			//kitkat
			handler.postDelayed(new Runnable() {
				public void run() {
					updateProfile(device, service, watch);
				}
			}, 2000);
			//updateProfile(device, service, watch);

			handler.postDelayed(new Runnable() {
				public void run() {
					boolean autoSyncTime = mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME);
					if (autoSyncTime) {
						LifeTrakLogger.info("Update Time and Date From Settings");
						mMainActivity.updateTimeDate();
					}
				}
			}, 4000);

			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					updateTimeAndDateFormat(device, service, watch);
				}
			}, 6000 );
		}
		return true;
	}

	public void removeCallback(){
		isCancelledSyncing = true;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				LifeTrakLogger.info("Sync on setting is cancelled");
				handler.removeCallbacksAndMessages(null);
				mInactiveAlert.removeCallbacksAndMessages(null);
				mUpdateWakeUpAlertHandler.removeCallbacksAndMessages(null);
				mUpdateNightAlertHandler.removeCallbacksAndMessages(null);
				mUpdateDayLightAlertHandler.removeCallbacksAndMessages(null);
			}
		}, 2000);

	}

	private final Handler mInactiveAlert = new Handler() {
		@Override
		public void handleMessage(Message message) {

			final int updateStatus;

			switch (message.what) {
			case 1: {
				SALActivityAlertSetting setting = new SALActivityAlertSetting();
				SALBLEService service = (SALBLEService) message.obj;

				setting.setActivityAlertStatus((mActivityAlertSettings.isEnabled()) ? SALActivityAlertSetting.ENABLE : SALActivityAlertSetting.DISABLE);
				LifeTrakLogger.info("ActivityAlert isEnabled = " + String.valueOf(mActivityAlertSettings.isEnabled()));
				updateStatus = service.updateActivityAlertSettingData(setting);
				LifeTrakLogger.info("ActivityAlert isEnabled Update Status = " + String.valueOf(updateStatus));

				Message msg1 = Message.obtain();
				msg1.what = 2;
				msg1.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mInactiveAlert.sendMessageDelayed(msg1, 2000);
				}
				else{
					//kitkat
					mInactiveAlert.sendMessageDelayed(msg1, SYNC_DELAY);
				}

				break;
			}
			case 2: {
				SALActivityAlertSetting setting = new SALActivityAlertSetting();
				SALBLEService service = (SALBLEService) message.obj;
				int startHours = mActivityAlertSettings.getStartTime() / 60;
				int startMinutes = mActivityAlertSettings.getStartTime() - (startHours * 60);

				setting.setStartTime(startHours, startMinutes);
				LifeTrakLogger.info("ActivityAlert Start Hour = " + String.valueOf(startHours) + " Start Min = " + String.valueOf(startMinutes));
				updateStatus = service.updateActivityAlertSettingData(setting);
				LifeTrakLogger.info("ActivityAlert setStartTime Update Status = " + String.valueOf(updateStatus));
				if (updateStatus != SALStatus.NO_ERROR) {
                    Log.v (TAG, ""+updateStatus);
				}
				Message msg2 = Message.obtain();
				msg2.what = 3;
				msg2.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mInactiveAlert.sendMessageDelayed(msg2, 2000);
				}
				else{
					//kitkat
					mInactiveAlert.sendMessageDelayed(msg2, SYNC_DELAY);
				}

				break;
			}
			case 3: {
				SALActivityAlertSetting setting = new SALActivityAlertSetting();
				SALBLEService service = (SALBLEService) message.obj;
				int endHours = mActivityAlertSettings.getEndTime() / 60;
				int endMinutes = mActivityAlertSettings.getEndTime() - (endHours * 60);

				setting.setEndTime(endHours, endMinutes);
				LifeTrakLogger.info("ActivityAlert End Hour = " + String.valueOf(endHours) + " End Min = " + String.valueOf(endMinutes));
				updateStatus = service.updateActivityAlertSettingData(setting);
				LifeTrakLogger.info("ActivityAlert setEndTime Update Status = " + String.valueOf(updateStatus));
				Message msg3 = Message.obtain();
				msg3.what = 4;
				msg3.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mInactiveAlert.sendMessageDelayed(msg3, 2000);
				}
				else{
					//kitkat
					mInactiveAlert.sendMessageDelayed(msg3, SYNC_DELAY);
				}

				break;
			}
			case 4: {
				SALActivityAlertSetting setting = new SALActivityAlertSetting();
				SALBLEService service = (SALBLEService) message.obj;

				setting.setTimeInterval(mActivityAlertSettings.getTimeInterval());
				LifeTrakLogger.info("ActivityAlert setTimeInterval = " + String.valueOf(mActivityAlertSettings.getTimeInterval()));
				updateStatus = service.updateActivityAlertSettingData(setting);
				LifeTrakLogger.info("ActivityAlert setTimeInterval Update Status = " + String.valueOf(updateStatus));
				Message msg4 = Message.obtain();
				msg4.what = 5;
				msg4.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mInactiveAlert.sendMessageDelayed(msg4, 2000);
				}
				else{
					//kitkat
					mInactiveAlert.sendMessageDelayed(msg4, SYNC_DELAY);
				}

				break;
			}
			case 5: {
				SALActivityAlertSetting setting = new SALActivityAlertSetting();
				final SALBLEService service = (SALBLEService) message.obj;

				setting.setStepsThreshold(mActivityAlertSettings.getStepsThreshold());
				LifeTrakLogger.info("ActivityAlert setStepsThreshold = " + String.valueOf(mActivityAlertSettings.getStepsThreshold()));
				updateStatus = service.updateActivityAlertSettingData(setting);
				LifeTrakLogger.info("ActivityAlert setStepsThreshold Update Status = " + String.valueOf(updateStatus));
				if (mMainActivity.mProgressDialog == null)
					mMainActivity.reinitializeProgress();
				mMainActivity.mProgressDialog.dismiss();

				mMainActivity.watchRegisterHandler();

				if (updateStatus != SALStatus.NO_ERROR) {
                    Log.v (TAG, ""+updateStatus);
                    AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.sync_watch_wrong)
                            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    //						service.disconnectFromDevice();
                                    arg0.dismiss();
                                }
                            }).create();
                    alert.show();
				}
				else

					LifeTrakLogger.info("End Syncing on Settings");
				{
					if (!isCancelledSyncing) {
						AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.sync_success)
								.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										//						service.disconnectFromDevice();
										arg0.dismiss();
									}
								}).create();
						alert.show();
					}
					else{
						isCancelledSyncing = false;
					}
				}

				break;
			}
			}

		}
	};

	private final Handler mUpdateWakeUpAlertHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {

			final int updateStatus;

			switch (message.what) {
			case 1: {
				SALBLEService service = (SALBLEService) message.obj;
				SALWakeupSetting wakeUpLightSettings = new SALWakeupSetting();
				wakeUpLightSettings.setWakeupSetting(SALWakeupSetting.WAKEUP_ALERT_STATUS, (mWakeupSetting.isEnabled()) ? SALWakeupSetting.ENABLE : SALWakeupSetting.DISABLE);
				LifeTrakLogger.info("WakeUp enable = " + String.valueOf(mWakeupSetting.isEnabled()));
				updateStatus = service.updateWakeupSettingData(wakeUpLightSettings);
				LifeTrakLogger.info("WakeUp enable Status Update= " + String.valueOf(updateStatus));
				Message msg1 = Message.obtain();
				msg1.what = 2;
				msg1.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateWakeUpAlertHandler.sendMessageDelayed(msg1, 2000);
				}
				else{
					//kitkat
					mUpdateWakeUpAlertHandler.sendMessageDelayed(msg1, SYNC_DELAY);
				}

				break;
			}
			case 2: {
				SALBLEService service = (SALBLEService) message.obj;
				SALWakeupSetting wakeUpLightSettings = new SALWakeupSetting();
				int mHours = mWakeupSetting.getTime() / 60;
				int mMinutes = mWakeupSetting.getTime() - (mHours * 60);
				wakeUpLightSettings.setWakeupSetting(SALWakeupSetting.WAKEUP_TIME, ((mHours << 8) + mMinutes));
				LifeTrakLogger.info("WakeUp  Hour = " + String.valueOf(mHours) + "  Min = " + String.valueOf(mMinutes));
				updateStatus = service.updateWakeupSettingData(wakeUpLightSettings);
				LifeTrakLogger.info("WakeUp setWakeupSettingTime Status Update= " + String.valueOf(updateStatus));
				Message msg2 = Message.obtain();
				msg2.what = 3;
				msg2.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateWakeUpAlertHandler.sendMessageDelayed(msg2, 2000);
				}
				else{
					//kitkat
					mUpdateWakeUpAlertHandler.sendMessageDelayed(msg2, SYNC_DELAY);
				}

				break;
			}
			case 3: {
				SALBLEService service = (SALBLEService) message.obj;
				SALWakeupSetting wakeUpLightSettings = new SALWakeupSetting();
				wakeUpLightSettings.setWakeupSetting(SALWakeupSetting.WAKEUP_WINDOW, mWakeupSetting.getSnoozeTime());
				LifeTrakLogger.info("WakeUp getSnoozeTime= " + mWakeupSetting.getSnoozeTime());
				updateStatus = service.updateWakeupSettingData(wakeUpLightSettings);
				LifeTrakLogger.info("WakeUp getSnoozeTime Status Update= " + String.valueOf(updateStatus));
				Message msg3 = Message.obtain();
				msg3.what = 4;
				msg3.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateWakeUpAlertHandler.sendMessageDelayed(msg3, 2000);
				}
				else{
					//kitkat
					mUpdateWakeUpAlertHandler.sendMessageDelayed(msg3, SYNC_DELAY);
				}

				break;
			}
			case 4: {
				SALBLEService service = (SALBLEService) message.obj;
				SALWakeupSetting wakeUpLightSettings = new SALWakeupSetting();
				wakeUpLightSettings.setWakeupSetting(SALWakeupSetting.SNOOZE_ALERT_STATUS, (mWakeupSetting.isSnoozeEnabled()) ? SALWakeupSetting.ENABLE : SALWakeupSetting.DISABLE);
				LifeTrakLogger.info("WakeUp isSnoozeEnabled = " + String.valueOf(mWakeupSetting.isSnoozeEnabled()));
				updateStatus = service.updateWakeupSettingData(wakeUpLightSettings);
				LifeTrakLogger.info("WakeUp isSnoozeEnabled Status Update= " + String.valueOf(updateStatus));
				Message msg4 = Message.obtain();
				msg4.what = 5;
				msg4.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateWakeUpAlertHandler.sendMessageDelayed(msg4, 2000);
				}
				else{
					//kitkat
					mUpdateWakeUpAlertHandler.sendMessageDelayed(msg4, SYNC_DELAY);
				}

				break;
			}
			case 5: {
				SALBLEService service = (SALBLEService) message.obj;
				SALWakeupSetting wakeUpLightSettings = new SALWakeupSetting();
				wakeUpLightSettings.setWakeupSetting(SALWakeupSetting.SNOOZE_TIME, mWakeupSetting.getSnoozeTime());
				LifeTrakLogger.info("WakeUp isSnoozeEnabled = " + String.valueOf(mWakeupSetting.getSnoozeTime()));
				updateStatus = service.updateWakeupSettingData(wakeUpLightSettings);
				LifeTrakLogger.info("WakeUp getSnoozeTime Status Update= " + String.valueOf(updateStatus));

				Message msg = Message.obtain();
				msg.obj = service;
				msg.what = 1;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mInactiveAlert.sendMessageDelayed(msg, 2000);
				}
				else{
					//kitkat
					mInactiveAlert.sendMessageDelayed(msg, SYNC_DELAY);
				}


				break;
			}
			}

		}
	};

	private final Handler mUpdateNightAlertHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {

			final int updateStatus;

			switch (message.what) {
			case 1: {
				SALBLEService service = (SALBLEService) message.obj;
				SALNightLightDetectSetting salNightLightSettings = new SALNightLightDetectSetting();
				salNightLightSettings.setLightDetectStatus((mNightLightDetectSetting.isEnabled()) ? SALNightLightDetectSetting.ENABLE : SALNightLightDetectSetting.DISABLE);
				LifeTrakLogger.info(" NightLight enable = " + String.valueOf(mNightLightDetectSetting.isEnabled()));
				updateStatus = service.updateNightLightSettingData(salNightLightSettings);
				LifeTrakLogger.info(" NightLight enable Status Update= " + String.valueOf(updateStatus));
				Message msg1 = Message.obtain();
				msg1.what = 2;
				msg1.obj = service;

				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateNightAlertHandler.sendMessageDelayed(msg1, 2000);
				}
				else{
					//kitkat
					mUpdateNightAlertHandler.sendMessageDelayed(msg1, SYNC_DELAY);
				}

				break;
			}
			case 2: {
				SALBLEService service = (SALBLEService) message.obj;
				SALNightLightDetectSetting salNightLightSettings = new SALNightLightDetectSetting();
				int startHours = mNightLightDetectSetting.getStartTime() / 60;
				int startMinutes = mNightLightDetectSetting.getStartTime() - (startHours * 60);
				salNightLightSettings.setStartTime(startHours, startMinutes);
				LifeTrakLogger.info(" NightLight Start Hour = " + String.valueOf(startHours) + " Start Min = " + String.valueOf(startMinutes));
				updateStatus = service.updateNightLightSettingData(salNightLightSettings);
				LifeTrakLogger.info(" NightLight setStartTime Status Update= " + String.valueOf(updateStatus));
				Message msg2 = Message.obtain();
				msg2.what = 3;
				msg2.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateNightAlertHandler.sendMessageDelayed(msg2, 2000);
				}
				else{
					//kitkat
					mUpdateNightAlertHandler.sendMessageDelayed(msg2, SYNC_DELAY);
				}

				break;
			}
			case 3: {
				SALBLEService service = (SALBLEService) message.obj;
				SALNightLightDetectSetting salNightLightSettings = new SALNightLightDetectSetting();
				int endHours = mNightLightDetectSetting.getEndTime() / 60;
				int endMinutes = mNightLightDetectSetting.getEndTime() - (endHours * 60);
				salNightLightSettings.setEndTime(endHours, endMinutes);
				LifeTrakLogger.info(" NightLight End Hour = " + String.valueOf(endHours) + " End Min = " + String.valueOf(endMinutes));
				updateStatus = service.updateNightLightSettingData(salNightLightSettings);
				LifeTrakLogger.info(" NightLight setEndTime Status Update= " + String.valueOf(updateStatus));
				Message msg3 = Message.obtain();
				msg3.what = 4;
				msg3.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateNightAlertHandler.sendMessageDelayed(msg3, 2000);
				}
				else{
					//kitkat
					mUpdateNightAlertHandler.sendMessageDelayed(msg3, SYNC_DELAY);
				}

				break;
			}
			case 4: {
				SALBLEService service = (SALBLEService) message.obj;
				SALNightLightDetectSetting salNightLightSettings = new SALNightLightDetectSetting();
				salNightLightSettings.setExposureLevel(mNightLightDetectSetting.getExposureLevel());
				LifeTrakLogger.info(" NightLight setExposureLevel= " + String.valueOf(salNightLightSettings.getExposureLevel()));
				updateStatus = service.updateNightLightSettingData(salNightLightSettings);
				LifeTrakLogger.info(" NightLight setExposureLevel Status Update= " + String.valueOf(updateStatus));
				Message msg4 = Message.obtain();
				msg4.what = 5;
				msg4.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateNightAlertHandler.sendMessageDelayed(msg4, 2000);
				}
				else{
					//kitkat
					mUpdateNightAlertHandler.sendMessageDelayed(msg4, SYNC_DELAY);
				}
				break;
			}
			case 5: {
				SALBLEService service = (SALBLEService) message.obj;
				SALNightLightDetectSetting salNightLightSettings = new SALNightLightDetectSetting();
				salNightLightSettings.setExposureDuration(mNightLightDetectSetting.getExposureDuration());
				LifeTrakLogger.info(" NightLight setExposureDuration = " + String.valueOf(salNightLightSettings.getExposureDuration()));
				updateStatus = service.updateNightLightSettingData(salNightLightSettings);
				LifeTrakLogger.info(" NightLight setExposureDuration Status Update= " + String.valueOf(updateStatus));

				Message msg5 = Message.obtain();
				msg5.what = 1;
				msg5.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateDayLightAlertHandler.sendMessageDelayed(msg5, 2000);
				}
				else{
					//kitkat
					mUpdateDayLightAlertHandler.sendMessageDelayed(msg5, SYNC_DELAY);
				}
				break;
			}
			}

		}
	};

	private final  Handler mUpdateDayLightAlertHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {

			final int updateStatus;

			switch (message.what) {
			case 1: {
				SALBLEService service = (SALBLEService) message.obj;
				SALDayLightDetectSetting salDayLightSettings = new SALDayLightDetectSetting();

				salDayLightSettings.setLightDetectStatus((mDayLightDetectSetting.isEnabled()) ? SALDayLightDetectSetting.ENABLE : SALDayLightDetectSetting.DISABLE);
				LifeTrakLogger.info("DayLight setLightDetectStatus Status Update= " + String.valueOf(mDayLightDetectSetting.isEnabled()));
				updateStatus = service.updateDayLightSettingData(salDayLightSettings);
				LifeTrakLogger.info("DayLight setLightDetectStatus Status Update= " + String.valueOf(updateStatus));
				Message msg1 = Message.obtain();
				msg1.what = 2;
				msg1.obj = service;

				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateDayLightAlertHandler.sendMessageDelayed(msg1, 2000);
				}
				else{
					//kitkat
					mUpdateDayLightAlertHandler.sendMessageDelayed(msg1, SYNC_DELAY);
				}
				break;
			}
			case 2: {
				SALBLEService service = (SALBLEService) message.obj;
				SALDayLightDetectSetting salDayLightSettings = new SALDayLightDetectSetting();
				int startHours = mDayLightDetectSetting.getStartTime() / 60;
				int startMinutes = mDayLightDetectSetting.getStartTime() - (startHours * 60);
				salDayLightSettings.setStartTime(startHours, startMinutes);
				LifeTrakLogger.info("DayLight Start Hour = " + String.valueOf(startHours) + " Start Min = " + String.valueOf(startMinutes));
				updateStatus = service.updateDayLightSettingData(salDayLightSettings);
				LifeTrakLogger.info("DayLight setStartTime Status Update= " + String.valueOf(updateStatus));
				Message msg2 = Message.obtain();
				msg2.what = 3;
				msg2.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateDayLightAlertHandler.sendMessageDelayed(msg2, 2000);
				}
				else{
					//kitkat
					mUpdateDayLightAlertHandler.sendMessageDelayed(msg2, SYNC_DELAY);
				}

				break;
			}
			case 3: {
				SALBLEService service = (SALBLEService) message.obj;
				SALDayLightDetectSetting salDayLightSettings = new SALDayLightDetectSetting();
				int endHours = mDayLightDetectSetting.getEndTime() / 60;
				int endMinutes = mDayLightDetectSetting.getEndTime() - (endHours * 60);
				salDayLightSettings.setEndTime(endHours, endMinutes);
				LifeTrakLogger.info("DayLight End Hour = " + String.valueOf(endHours) + " Start Min = " + String.valueOf(endMinutes));
				updateStatus = service.updateDayLightSettingData(salDayLightSettings);
				LifeTrakLogger.info("DayLight setEndTime Status Update= " + String.valueOf(updateStatus));
				Message msg3 = Message.obtain();
				msg3.what = 4;
				msg3.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateDayLightAlertHandler.sendMessageDelayed(msg3, 2000);
				}
				else{
					//kitkat
					mUpdateDayLightAlertHandler.sendMessageDelayed(msg3, SYNC_DELAY);
				}

				break;
			}
			case 4: {
				SALBLEService service = (SALBLEService) message.obj;
				SALDayLightDetectSetting salDayLightSettings = new SALDayLightDetectSetting();
				salDayLightSettings.setExposureLevel(mDayLightDetectSetting.getExposureLevel());
				LifeTrakLogger.info("DayLight setExposureLevel= " + String.valueOf(mDayLightDetectSetting.getExposureLevel()));
				updateStatus = service.updateDayLightSettingData(salDayLightSettings);
				LifeTrakLogger.info("DayLight setExposureLevel Status Update= " + String.valueOf(updateStatus));
				Message msg4 = Message.obtain();
				msg4.what = 5;
				msg4.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateDayLightAlertHandler.sendMessageDelayed(msg4, 2000);
				}
				else{
					//kitkat
					mUpdateDayLightAlertHandler.sendMessageDelayed(msg4, SYNC_DELAY);
				}

				break;
			}
			case 5: {
				SALBLEService service = (SALBLEService) message.obj;
				SALDayLightDetectSetting salDayLightSettings = new SALDayLightDetectSetting();
				salDayLightSettings.setExposureDuration(mDayLightDetectSetting.getExposureDuration());
				LifeTrakLogger.info("DayLight setExposureDuration= " + String.valueOf(mDayLightDetectSetting.getExposureDuration()));
				updateStatus = service.updateDayLightSettingData(salDayLightSettings);
				LifeTrakLogger.info("DayLight setExposureDuration Status Update= " + String.valueOf(updateStatus));
				Message msg5 = Message.obtain();
				msg5.what = 6;
				msg5.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateDayLightAlertHandler.sendMessageDelayed(msg5, 2000);
				}
				else{
					//kitkat
					mUpdateDayLightAlertHandler.sendMessageDelayed(msg5, SYNC_DELAY);
				}
				break;

			}
			case 6: {
				SALBLEService service = (SALBLEService) message.obj;
				SALDayLightDetectSetting salDayLightSettings = new SALDayLightDetectSetting();
				salDayLightSettings.setInterval(mDayLightDetectSetting.getInterval());
				LifeTrakLogger.info("DayLight setInterval = " + String.valueOf(mDayLightDetectSetting.getInterval()));
				updateStatus = service.updateDayLightSettingData(salDayLightSettings);
				LifeTrakLogger.info("DayLight setInterval Status Update= " + String.valueOf(updateStatus));

				Message msg6 = Message.obtain();
				msg6.what = 1;
				msg6.obj = service;
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
					//lollipop
					mUpdateWakeUpAlertHandler.sendMessageDelayed(msg6, 2000);
				}
				else{
					//kitkat
					mUpdateWakeUpAlertHandler.sendMessageDelayed(msg6, SYNC_DELAY);
				}


				break;
			}
			}

		}
	};

	private void updateUnit(BluetoothDevice device, SALBLEService service, Watch watch) {
		final LifeTrakApplication app = (LifeTrakApplication) getActivity().getApplication();

		int dateFormat = SALTimeDate.FORMAT_DDMM;
		final boolean isMMDD = ((RadioButton) getView().findViewById(R.id.unitprefs_date_option1)).isChecked();

		if (isMMDD) {
			dateFormat = SALTimeDate.FORMAT_MMDD;
		}

		int timeFormat = SALTimeDate.FORMAT_24HOUR;
		final boolean is12hr = ((RadioButton) getView().findViewById(R.id.unitprefs_time_option1)).isChecked();

		if (is12hr) {
			timeFormat = SALTimeDate.FORMAT_12HOUR;
		}

		final SALTimeDate salTimeDate = new SALTimeDate();
		final TimeDate timedate = app.getTimeDate();

		salTimeDate.setDate(timedate.getDay(), timedate.getMonth(), timedate.getYear());
		salTimeDate.setTime(timedate.getSecond(), timedate.getMinute(), timedate.getHour());
		timedate.setDateFormat(dateFormat);
		timedate.setHourFormat(timeFormat);
		salTimeDate.setDateFormat(dateFormat);
		salTimeDate.setTimeFormat(timeFormat);

		final int updateStatus = service.updateTimeAndDate(salTimeDate);

		if (updateStatus != SALStatus.NO_ERROR) {
			throw new RuntimeException(getString(R.string.update_time_error) + " " + updateStatus);
		}

		DataSource.getInstance(getActivity()).getWriteOperation().open().beginTransaction().update(timedate).endTransaction().close();
	}

	private void updateProfile(BluetoothDevice device, SALBLEService service, Watch watch) {
		final LifeTrakApplication app = (LifeTrakApplication) getActivity().getApplication();
		final SALUserProfile salUserProfile = new SALUserProfile();
		final UserProfile userProfile = app.getUserProfile();
		if (userProfile != null){
			salUserProfile.setWeight(userProfile.getWeight());
			LifeTrakLogger.info("User Info weight = " + String.valueOf(userProfile.getWeight()));
			salUserProfile.setHeight(userProfile.getHeight());
			LifeTrakLogger.info("User Info height = " + String.valueOf(userProfile.getHeight()));
			salUserProfile.setBirthYear(userProfile.getBirthYear() - 1900);
			LifeTrakLogger.info("User Info BirthYear = " + String.valueOf(userProfile.getBirthYear() - 1900));
			salUserProfile.setBirthMonth(userProfile.getBirthMonth());
			LifeTrakLogger.info("User Info BirthMonth = " + String.valueOf(userProfile.getBirthMonth()));
			salUserProfile.setBirthDay(userProfile.getBirthDay());
			LifeTrakLogger.info("User Info BirthDay = " + String.valueOf(userProfile.getBirthDay()));

			final boolean isMale = ((RadioButton) getView().findViewById(R.id.urprofile_male_option)).isChecked();

			if (isMale) {
				userProfile.setGender(GENDER_MALE);
			} else {
				userProfile.setGender(GENDER_FEMALE);
			}

			salUserProfile.setGender(userProfile.getGender());
			LifeTrakLogger.info("User Info Gender = " + String.valueOf(userProfile.getGender()));
			salUserProfile.setSensitivityLevel(userProfile.getSensitivity());
			LifeTrakLogger.info("User Info Sensitivity = " + String.valueOf(userProfile.getSensitivity()));
			int unit = SalutronLifeTrakUtility.UNIT_METRIC;

			if (mTextviewUnitSelection.getText().toString().equals(getString(R.string.caption_unit_imperial))) {
				unit = SalutronLifeTrakUtility.UNIT_IMPERIAL;
			}

			LifeTrakLogger.info("User Info Unit = " + String.valueOf(unit));
			salUserProfile.setUnitSystem(unit);
			userProfile.setUnitSystem(unit);

			final int updateStatus = service.updateUserProfile(salUserProfile);
			LifeTrakLogger.info("Update Profile status = " + String.valueOf(updateStatus));

			DataSource.getInstance(getActivity()).getWriteOperation().open().beginTransaction().update(userProfile).endTransaction().close();
		}
	}

	private void updateTimeAndDateFormat(final BluetoothDevice device, final SALBLEService service, final Watch watch) {

//		if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME)) {
//			timeDate.setToNow();
//		} else {
////			TimeDate timeDateModel = getLifeTrakApplication().getTimeDate();
////			timeDate.set(timeDateModel.getSecond(), timeDateModel.getMinute(), timeDateModel.getHour(),
////					timeDateModel.getDay(), timeDateModel.getMonth(), timeDateModel.getYear());
//		}
		service.getCurrentTimeAndDate();
		service.registerDevDataHandler(timeHandler);
		service.registerDevListHandler(timeHandler);



	}

	private void updateTimeDate(final BluetoothDevice device, final SALBLEService service, final Watch watch,final SALTimeDate oldTimeDate){
		final SALTimeDate timeDate = new SALTimeDate();
		if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME)){
			timeDate.setToNow();
		}
		else{
			timeDate.setTime(0, oldTimeDate.getMinute(),oldTimeDate.getHour());
		}

		final Handler newHandler = new Handler(){
			public void handleMessage(Message message) {
				switch (message.what) {
					case 0:
						if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
							timeDate.setDateFormat(getLifeTrakApplication().getTimeDate().getDateFormat());
						} else {
							if (mDateFormat.getText().toString().equals(getString(R.string.unitprefs_date_option1_caption)))
								timeDate.setDateFormat(SALTimeDate.FORMAT_MMDD);
							else
								timeDate.setDateFormat(SALTimeDate.FORMAT_DDMM);
						}
						LifeTrakLogger.info("Time Format DateFormat = " + String.valueOf(timeDate.getDateFormat()));
						statusUpdate = service.updateTimeAndDate(timeDate);
						LifeTrakLogger.info("Time Format DateFormat Update value = " + String.valueOf(statusUpdate));
						Message msg = Message.obtain();
						msg.what = 2;
						if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
							//lollipop
							sendMessageDelayed(msg, 2000);
						} else {
							//kitkat
							sendMessageDelayed(msg, 750);
						}
						break;
					case 2:

						if (mTextviewHourFormat.getText().toString().equals(getString(R.string.caption_12_hour)))
							timeDate.setTimeFormat(SALTimeDate.FORMAT_12HOUR);
						else
							timeDate.setTimeFormat(SALTimeDate.FORMAT_24HOUR);

						if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415 || getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420) {
							statusUpdate = service.updateTimeAndDate(timeDate);
							LifeTrakLogger.info("Time Format TimeFormat Update Value = " + String.valueOf(statusUpdate));
							msg = Message.obtain();
							msg.what = 3;
							if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
								//lollipop
								sendMessageDelayed(msg, 2000);
							} else {
								//kitkat
								sendMessageDelayed(msg, 750);
							}
						} else {
							try {
								int status = service.updateTimeAndDate(timeDate);
								if (status == SALStatus.NO_ERROR)
									updateCalibrationData(device, service, watch);

							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

						break;
					case 3:

						if (mTextviewWatchDisplay.getText().toString().equals(getString(R.string.caption_simple)))
							timeDate.setTimeDisplaySize(DISPLAY_FORMAT_BIG_DIGIT);
						else
							timeDate.setTimeDisplaySize(DISPLAY_FORMAT_SMALL_DIGIT);

						LifeTrakLogger.info("Time Format Watch Display = " + String.valueOf(timeDate.getTimeDisplaySize()));
						statusUpdate = service.updateTimeAndDate(timeDate);
						LifeTrakLogger.info("Time Format Watch Display Update Value = " + String.valueOf(statusUpdate));
						if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420) {
							msg = Message.obtain();
							msg.what = 4;
							if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
								//lollipop
								sendMessageDelayed(msg, 2000);
							} else {
								//kitkat
								sendMessageDelayed(msg, 750);
							}
						} else {
							try {

								updateCalibrationData(device, service, watch);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						break;

					case 4:
						if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420) {
							if (mWorkoutSettings != null) {
								LifeTrakLogger.info("Logging Rate = " + String.valueOf(mWorkoutSettings.getHrLoggingRate()));
								statusUpdate = service.updateWorkoutHRLogRate(mWorkoutSettings.getHrLoggingRate());
								LifeTrakLogger.info("updateWorkoutHRLogRate Update Value = " + String.valueOf(statusUpdate));

								if (mWorkoutSettings.getDatabaseUsageMax() > 0) {
									float storageLeft = mWorkoutSettings.getDatabaseUsageMax() - mWorkoutSettings.getDatabaseUsage();
									storageLeft -= 46.0f;
									storageLeft = storageLeft * mWorkoutSettings.getHrLoggingRate();
									storageLeft /= 1.125f;
									storageLeft /= 3600.0f;
									if (storageLeft > 0)
										textviewWorkoutStorageLeft.setText(String.format("%.2f", storageLeft) + " hours");
									else
										textviewWorkoutStorageLeft.setText("0");
								}

								try {
									updateCalibrationData(device, service, watch);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						} else {
							try {
								updateCalibrationData(device, service, watch);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						break;

				}
			}
		};

		newHandler.sendMessage(Message.obtain());
	}


	 Handler timeHandler = new Handler() {
		final SALTimeDate timeDate = new SALTimeDate();

		public void handleMessage(Message message) {
			switch (message.what) {
				case 0:
					break;
				case SALBLEService.SAL_MSG_DEVICE_DATA_RECEIVED:
					int dataType = message.getData().getInt(SALBLEService.SAL_DEVICE_DATA_TYPE);

					switch (dataType) {
						case SALBLEService.COMMAND_GET_TIME:
							SALTimeDate salTimeDate = message.getData().getParcelable(SALBLEService.SAL_DEVICE_DATA);
							TimeDate oldTimeDate = TimeDate.buildTimeDate(getActivity(), salTimeDate);
							if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME)) {
								timeDate.setToNow();
							} else {
								timeDate.setTime(oldTimeDate.getSecond(), oldTimeDate.getMinute(), oldTimeDate.getHour());
							}
							updateTimeDate(mDevice, mService, mWatch, timeDate);
							break;
					}
					break;
				case SALBLEService.SAL_MSG_DEVICE_INFO:
					final int devInfoType = message.getData().getInt(SALBLEService.SAL_DEVICE_INFO_TYPE);
					switch (devInfoType) {
						case SALBLEService.DEV_INFO_FIRMWARE_VERSION:
							onGetFirmware(message.getData().getString(SALBLEService.SAL_DEVICE_INFO));
							break;
					}
					break;
			}
		}
	};


	private final HeightPickerView.OnSelectHeightListener mSelectHeightListener = new HeightPickerView.OnSelectHeightListener() {
		@Override
		public void onSelectHeight(int valueInCm) {
			final LifeTrakApplication app = (LifeTrakApplication) getActivity().getApplication();
			final UserProfile userProfile = app.getUserProfile();
			userProfile.setHeight(valueInCm);
			double feetValue = Math.floor(valueInCm / FEET_CM);
			double inchValue = (valueInCm / INCH_CM) - (feetValue * 12);

			if (Math.round(inchValue) == 12) {
				feetValue++;
				inchValue = 0;
			}

			mHeightValueInCm = valueInCm;

			final EditText m = (EditText) getView().findViewById(R.id.urprofile_height_value);

			m.setText(String.format("%d' %02d\"", (int) feetValue, Math.round(inchValue)));
			userProfile.update();
		}
	};

	private final TextWatcher mTextWatcherWeight = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			final LifeTrakApplication app = (LifeTrakApplication) getActivity().getApplication();
			final UserProfile userProfile = app.getUserProfile();
			final EditText m = (EditText) getView().findViewById(R.id.urprofile_weight_value);

			int value = 0;

			if (!s.toString().equals("")) {
				value = Integer.parseInt(s.toString());
			} else {
				value = Integer.parseInt(m.getHint().toString());
			}

			if (userProfile.getUnitSystem() == UNIT_METRIC) {
				value = Math.round(value / KG);
			}

			userProfile.setWeight(value);
			userProfile.update();
		}
	};

	private final TextWatcher mTextWatcherHeight = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			final LifeTrakApplication app = (LifeTrakApplication) getActivity().getApplication();
			final UserProfile userProfile = app.getUserProfile();
			final EditText m = (EditText) getView().findViewById(R.id.urprofile_height_value);

			int value = 0;
			try {
				if (userProfile.getUnitSystem() == UNIT_METRIC) {
					if (!s.toString().equals("")) {
						if (s.toString().contains("\\'")) {
							value = (int) (Integer.parseInt(s.toString().replace("\\'", ".")) * 30.48);
						}

						value = Integer.parseInt(s.toString());
					} else {
						value = Integer.parseInt(m.getHint().toString());
					}
				} else {
					value = mHeightValueInCm;
				}

				if (value == 0)
					value = getLifeTrakApplication().getUserProfile().getHeight();

				if (value > 0)
					userProfile.setHeight(value);
				userProfile.update();
			}
			catch (Exception e){
				LifeTrakLogger.info("Error : " + e.getLocalizedMessage());
			}
		}
	};

	private final View.OnFocusChangeListener mWeightFocusChangeListener = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			TextView textWeight = (TextView) arg0;

			if (!arg1 && !textWeight.getText().toString().isEmpty()) {
				int weight = Integer.parseInt(textWeight.getText().toString());
				final LifeTrakApplication app = (LifeTrakApplication) getActivity().getApplication();
				final UserProfile userProfile = app.getUserProfile();

				if (getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_METRIC) {
					weight = Math.round(weight / KG);
				}

				if (weight < 44) {
					weight = 44;
				} else if (weight > 440) {
					weight = 440;
				}

				textWeight.removeTextChangedListener(mTextWatcherWeight);
				if (getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_METRIC) {
					textWeight.setText(String.valueOf(Math.round(weight * KG)));
				} else {
					textWeight.setText(String.valueOf(weight));
				}
				textWeight.addTextChangedListener(mTextWatcherWeight);

				userProfile.setWeight((int) weight);
				userProfile.update();
			}
		}
	};

	private final View.OnFocusChangeListener mHeightFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			TextView textHeight = (TextView) arg0;

			if (!arg1 && !textHeight.getText().toString().isEmpty()) {
				int height = 0;
				if (getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL) {
					height = mHeightValueInCm;

					if (height == 0)
						getLifeTrakApplication().getUserProfile().getHeight();

				} else {
					height = Integer.parseInt(textHeight.getText().toString());
				}
				final LifeTrakApplication app = (LifeTrakApplication) getActivity().getApplication();
				final UserProfile userProfile = app.getUserProfile();

				textHeight.removeTextChangedListener(mTextWatcherHeight);
				if (userProfile.getUnitSystem() == UNIT_IMPERIAL) {
					height = mHeightValueInCm;
				}
				textHeight.addTextChangedListener(mTextWatcherHeight);

				if (height < 100) {
					height = 100;
				} else if (height > 220) {
					height = 220;
				}

				if (userProfile.getUnitSystem() == UNIT_METRIC) {
					textHeight.setText(String.valueOf(height));
				}

				userProfile.setHeight(height);
				userProfile.update();
			}

		}
	};

	private final TextView.OnEditorActionListener mWeightActionListener = new TextView.OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
			if (arg1 == EditorInfo.IME_ACTION_DONE && !arg0.getText().toString().isEmpty()) {
				int weight = Integer.parseInt(arg0.getText().toString());
				final LifeTrakApplication app = (LifeTrakApplication) getActivity().getApplication();
				final UserProfile userProfile = app.getUserProfile();

				if (getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_METRIC) {
					weight = Math.round(weight / KG);
				}

				if (weight < 44) {
					weight = 44;
				} else if (weight > 440) {
					weight = 440;
				}

				arg0.removeTextChangedListener(mTextWatcherWeight);
				if (getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_METRIC) {
					arg0.setText(String.valueOf(Math.round(weight * KG)));
				} else {
					arg0.setText(String.valueOf(weight));
				}
				arg0.addTextChangedListener(mTextWatcherWeight);

				userProfile.setWeight((int) weight);
				userProfile.update();
			}
			return false;
		}
	};

	private final TextView.OnEditorActionListener mHeightActionListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
			if (arg1 == EditorInfo.IME_ACTION_DONE && !arg0.getText().toString().isEmpty()) {
				int height = 0;
				if (getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL) {
					height = mHeightValueInCm;

					if (height == 0)
						getLifeTrakApplication().getUserProfile().getHeight();

				} else {
					height = Integer.parseInt(arg0.getText().toString());
				}
				final LifeTrakApplication app = (LifeTrakApplication) getActivity().getApplication();
				final UserProfile userProfile = app.getUserProfile();

				arg0.removeTextChangedListener(mTextWatcherHeight);
				if (userProfile.getUnitSystem() == UNIT_IMPERIAL) {
					height = mHeightValueInCm;
				}
				arg0.addTextChangedListener(mTextWatcherHeight);

				if (height < 100) {
					height = 100;
				} else if (height > 220) {
					height = 220;
				}

				if (userProfile.getUnitSystem() == UNIT_METRIC) {
					arg0.setText(String.valueOf(height));
				}

				userProfile.setHeight(height);
				userProfile.update();
			}
			return false;
		}
	};

	private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Calendar calendarNow = Calendar.getInstance();
			calendarNow.set(Calendar.YEAR, year);
			calendarNow.set(Calendar.MONTH, monthOfYear);
			calendarNow.set(Calendar.DAY_OF_MONTH, dayOfMonth);

			if (calendarNow.getTime().after(new Date()))
				return;

			final LifeTrakApplication app = (LifeTrakApplication) getActivity().getApplication();
			final UserProfile userProfile = app.getUserProfile();

			userProfile.setBirthYear(year);
			userProfile.setBirthMonth(monthOfYear + 1);
			userProfile.setBirthDay(dayOfMonth);

			Calendar calendar = new GregorianCalendar();
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, monthOfYear);
			calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

			final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
			final Date dob = calendar.getTime();

			final TextView value = (TextView) getView().findViewById(R.id.urprofile_dob_value);
			value.setText(dateFormat.format(dob));
		}
	};

	private void updateCalibrationData(BluetoothDevice device, final SALBLEService service, Watch watch) throws InterruptedException {
		Handler handler = new Handler();


		final SALCalibration calibration = new SALCalibration();
		int delayValue = SYNC_DELAY;
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
			//lollipop
			delayValue = 2000;
		}
		else{
			//kitkat
			delayValue = SYNC_DELAY;
		}
		LifeTrakLogger.info("updateCalibrationData Delay Value  = " + String.valueOf(delayValue));
		handler.postDelayed(new Runnable() {
			public void run() {
				if (calibrationData != null) {
					calibration.setCalibrationType(SALCalibration.STEP_CALIBRATION);

					calibrationData.setContext(getActivity());

					calibration.setCalibrationValue(calibrationData.getStepCalibration());
					LifeTrakLogger.info("calibrationData getStepCalibration  = " + String.valueOf(calibrationData.getStepCalibration()));
					int status = service.updateCalibrationData(calibration);
					LifeTrakLogger.info("calibrationData getStepCalibration Update Status = " + String.valueOf(status));
				}
			}
		}, delayValue);

		handler.postDelayed(new Runnable() {
			public void run() {
				if (calibrationData != null) {
					calibration.setCalibrationType(SALCalibration.WALK_DISTANCE_CALIBRATION);
					calibration.setCalibrationValue((int) mWalkSeekBar.getFloatValue());

					calibrationData.setDistanceCalibrationWalk((int) mWalkSeekBar.getFloatValue());
					LifeTrakLogger.info("calibrationData getDistanceCalibrationWalk= " + String.valueOf(calibration.getCalibrationValue()));
					int status = service.updateCalibrationData(calibration);
					LifeTrakLogger.info("calibrationData getDistanceCalibrationWalk Update Status = " + String.valueOf(status));

				}
			}
		}, delayValue * 2);

		handler.postDelayed(new Runnable() {
			public void run() {
				if (getLifeTrakApplication().getSelectedWatch().getModel()== WATCHMODEL_R415) {
					SALSleepSetting sleepSetting = new SALSleepSetting();
					sleepSetting.setSleepDetectType(3);
					int updateStatus = service.updateSleepSetting(sleepSetting);
					LifeTrakLogger.info("calibrationData setSleepDetectType Update Status = " + String.valueOf(updateStatus));
				}
			}
		}, delayValue * 3);

		handler.postDelayed(new Runnable() {
			public void run() {
				final MainActivity activity = (MainActivity) getActivity();
				int status = 0;
				if (calibrationData != null) {

					calibration.setCalibrationType(SALCalibration.AUTO_EL_SETTING);


					calibration.setCalibrationValue(SALCalibration.AUTO_EL_ON);
					calibrationData.setAutoEL(SALCalibration.AUTO_EL_ON);

					LifeTrakLogger.info("calibrationData getAutoEL = " + String.valueOf(calibration.getAutoELStatus()));
					status = service.updateCalibrationData(calibration);
					LifeTrakLogger.info("calibrationData getAutoEL Update Status= " + String.valueOf(status));
					calibrationData.update();

				}
				final Date now = new Date();

				final LifeTrakApplication app = getLifeTrakApplication();
				if (app != null) {
					app.getSelectedWatch().setLastSyncDate(now);
					app.getSelectedWatch().update();
				}
				Editor edit = prefs.edit();
				edit.putLong("lastSyncDate", now.getTime());
				edit.commit();

				if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_R415 &&
						getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_R420) {
					disconnectC300C410watch(service, status, activity);
				}

			}
		}, delayValue * 4);

		if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
			handler.postDelayed(new Runnable() {
				public void run() {
					if (calibrationData != null) {
						calibration.setCalibrationType(SALCalibration.CALORIE_CALIBRATION);
						calibration.setCalibrationValue((int) mCalorieSeekBar.getFloatValue());
						LifeTrakLogger.info("calibrationData getCalibrationValue = " + String.valueOf(calibration.getCalibrationValue()));
						int status = service.updateCalibrationData(calibration);
						LifeTrakLogger.info("calibrationData getCalibrationValue Update Status= " + String.valueOf(status));
						if (nightLightDetectSetting.size() > 0) {

							mNightLightDetectSetting = nightLightDetectSetting.get(0);
							Message msg = Message.obtain();
							msg.obj = service;
							msg.what = 1;
							if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
								//lollipop
								mUpdateNightAlertHandler.sendMessageDelayed(msg, 2000);
							}
							else{
								//kitkat
								mUpdateNightAlertHandler.sendMessageDelayed(msg, SYNC_DELAY);
							}

						}


					}
				}
			}, delayValue * 5);
		}
		else if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420){
			handler.postDelayed(new Runnable() {
				public void run() {
					LifeTrakLogger.info("R420 Update reconnectTimeOut = " + String.valueOf(mWorkoutSettings.getReconnectTime()));
					int status  = service.updateReconnectTimeout(mWorkoutSettings.getReconnectTime());

					final MainActivity activity = (MainActivity) getActivity();
					disconnectC300C410watch(service, status, activity);
				}
			}, delayValue * 5);
		}
	}

	private void disconnectC300C410watch(final SALBLEService service,final int status,final MainActivity activity){
		Handler handler = new Handler();
		int delayValue = SYNC_DELAY;
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
			//lollipop
			delayValue = 2000;
		}
		else{
			//kitkat
			delayValue = SYNC_DELAY;
		}
		handler.postDelayed(new Runnable() {
			public void run() {
				fetchUserProfile();
				activity.mProgressDialog.dismiss();
				service.disconnectFromDevice();

				//if (status == SALStatus.NO_ERROR) {
				activity.C300C410SyncSuccess(true);
				if (!isCancelledSyncing) {
					AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.sync_success)
							.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {

									arg0.dismiss();
								}
							}).create();
					alert.show();
				} else {
					isCancelledSyncing = false;
				}
				//	}
			}
		}, delayValue);

	}



	@SuppressLint("SimpleDateFormat")
	private Date returnDate(String time) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
		try {
			return dateFormat.parse(time);
		} catch (ParseException e) {
			return new Date();
		}
	}

	// Once a day Date Picker Listers
	private final TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			Date mDate = null;
			if (view.is24HourView()) {
				mOnceADay.setText(hourOfDay + ":" + String.format("%02d", minute));
				//mDate = DateTimeUtil.convertTimeToDate(hourOfDay + ":" + String.format("%02d", minute), getLifeTrakApplication());
			} else {
				/*String AM_PM;
				if (hourOfDay < 12) {
					AM_PM = "AM";

				} else {
					AM_PM = "PM";
					hourOfDay = hourOfDay - 12;
				}

				if (hourOfDay == 0) {
					hourOfDay = 12;
				}*/

				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE, minute);

				SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getInstance();
				dateFormat.applyPattern("hh:mm aa");

				mOnceADay.setText(dateFormat.format(calendar.getTime()));
				//mDate = DateTimeUtil.convertTimeToDate(hourOfDay + ":" + String.format("%02d", minute) + " " + AM_PM, getLifeTrakApplication());
			}
			mDate = DateTimeUtil.convertTimeToDate(hourOfDay, minute);
			mPreferenceWrapper.setPreferenceLongValue(SalutronLifeTrakUtility.TIME_ALERT, mDate.getTime()).synchronize();

			mMainActivity.setAlarm();
		}
	};

	private void setCalendarDateTimeOnceDay() {
		Date fDate = DateTimeUtil.convertTimeToDate(mOnceADay.getText().toString(), getLifeTrakApplication());
		Calendar fcalendar = Calendar.getInstance();
		fcalendar.setTime(fDate);
		int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
		int fminutes = fcalendar.get(Calendar.MINUTE);

		Calendar dcaCalendar = Calendar.getInstance();
		dcaCalendar.set(Calendar.HOUR_OF_DAY, fhours);
		dcaCalendar.set(Calendar.MINUTE, fminutes);

		Log.d("Hours: ", "" + fhours);
		Log.d("Minutes: ", String.format("%02d", fminutes));

		long selectedTimeForAutoSync = dcaCalendar.getTimeInMillis();
		Log.d("timeStamp Value : ", "" + selectedTimeForAutoSync);

		postTimedNotification(selectedTimeForAutoSync);
	}

	// Once a week Listers
	private final View.OnClickListener alertListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.date_picker_weekly_set:
				// Log.d("alertListener", "day value = " +
				// weekPicker.getValue());
				Date mdate = null;
				int daySelected = weekPicker.getValue();
				switch (daySelected) {
				case 0:
					day = getString(R.string.sunday);
					break;
				case 1:
					day = getString(R.string.monday);
					break;
				case 2:
					day = getString(R.string.tuesday);
					break;
				case 3:
					day = getString(R.string.wednesday);
					break;
				case 4:
					day = getString(R.string.thursday);
					break;
				case 5:
					day = getString(R.string.friday);
					break;
				case 6:
					day = getString(R.string.saturday);
					break;
				default:
				}
				if (wkAM_PM != null){
					mOnceAWeekTime.setText(wkHour + ":" + String.format("%02d", wkMin) + " " + wkAM_PM);
					mdate = DateTimeUtil.convertTimeToDate(wkHour + ":" + String.format("%02d", wkMin) + " " + wkAM_PM, getLifeTrakApplication());
				}
				else{
					setHoursAndMinutes();
					mOnceAWeekTime.setText(wkHour + ":" + String.format("%02d", wkMin) + " " + wkAM_PM);
					mdate = DateTimeUtil.convertTimeToDate(wkHour + ":" + String.format("%02d", wkMin) + " " + wkAM_PM, getLifeTrakApplication());
				}
				mPreferenceWrapper.setPreferenceLongValue(SalutronLifeTrakUtility.TIME_ALERT, mdate.getTime()).synchronize();;
				mPreferenceWrapper.setPreferenceStringValue(SYNC_DAY,  day).synchronize();

				((MainActivity)getActivity()).setAlarm();
				mOnceAWeekDay.setText(day);
				alertDialog.dismiss();
				break;
			case R.id.date_picker_weekly_cancel:
				alertDialog.dismiss();
				break;

			}
		}

		/*
		 * Get phone's current hours and minutes to sync with date picker
		 */
		private void setHoursAndMinutes() {
			Date fDate = DateTimeUtil.convertTimeToDate(mOnceAWeekTime.getText().toString(), getLifeTrakApplication());
			Calendar fcalendar = Calendar.getInstance();
			fcalendar.setTime(fDate);
			int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
			int fminutes = fcalendar.get(Calendar.MINUTE);

			if (fhours < 12) {
				wkAM_PM = getString(R.string.am);

			} else {
				wkAM_PM = getString(R.string.pm);
				fhours = fhours - 12;
			}

			if (fhours == 0) {
				fhours = 12;
			}

			wkHour = fhours;
			wkMin = fminutes;
		}
	};

	private TimePicker.OnTimeChangedListener onTimeChangedListener = new TimePicker.OnTimeChangedListener() {
		@Override
		public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

			if (view.is24HourView()) {
				wkHour = hourOfDay;
				wkMin = minute;
			} else {
				if (hourOfDay < 12) {
					wkAM_PM = getString(R.string.am);
				} else {
					wkAM_PM = getString(R.string.pm);
					hourOfDay = hourOfDay - 12;
				}
				if (hourOfDay == 0) {
					hourOfDay = 12;
				}
				wkHour = hourOfDay;
				wkMin = minute;
			}

		}
	};


	protected void setCalendarDateTimeOnceWeek() {
		if (weekPicker == null)
			return;

		Date fDate = returnDate(mOnceAWeekTime.getText().toString());
		Calendar fcalendar = Calendar.getInstance();
		fcalendar.setTime(fDate);
		int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
		int fminutes = fcalendar.get(Calendar.MINUTE);
		Calendar dcaCalendar = Calendar.getInstance();

		dcaCalendar.set(Calendar.HOUR_OF_DAY, fhours);
		dcaCalendar.set(Calendar.MINUTE, fminutes);
		dcaCalendar.set(Calendar.DAY_OF_WEEK, weekPicker.getValue() + 1);

		Log.d("Hours: ", "" + fhours);
		Log.d("Minutes: ", String.format("%02d", fminutes));

		long selectedTimeForAutoSync = dcaCalendar.getTimeInMillis();
		Log.d("timeStamp Value : ", "" + selectedTimeForAutoSync);

		postTimedNotification(selectedTimeForAutoSync);
	}

	/**
	 * Schedule notification.
	 *
	 * @param timestampToPostNotif
	 *            The date to post notification.
	 */
	private void postTimedNotification(long timestampToPostNotif) {
		Log.d("Post Time", "postTimedNotification called");
		Intent intentAlarm = new Intent(getActivity(), com.salutron.lifetrakwatchapp.service.AlarmNotifReceiver.class);
		// intentAlarm.putExtra(EXTRAS_NOTIF_MESSAGE, message);
		AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intentAlarm, PendingIntent.FLAG_ONE_SHOT);
		alarmManager.set(AlarmManager.RTC_WAKEUP, timestampToPostNotif, pendingIntent);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		//		if (mDataSyncReminderAlert.isChecked() == true) {
		////			setIsOnceADay(mOnceADay.getVisibility() == View.VISIBLE);
		////			Editor edit = prefs.edit();
		////			if (mIsOnceADay == true) {
		////				// Save to pref
		////				edit.putString(ONCEADAY, mOnceADay.getText().toString());
		////				edit.putString(ONCEAWEEK, "");
		////				edit.putString(ONCEAWEEKDAYSELECTED, "");
		////				edit.commit();
		////
		////				setCalendarDateTimeOnceDay();
		////			} else if (mIsOnceAWeek == true) {
		////				setCalendarDateTimeOnceWeek();
		////				edit.putString(ONCEAWEEK, mOnceAWeekTime.getText().toString());
		////				edit.putString(ONCEAWEEKDAYSELECTED, mOnceAWeekDay.getText().toString());
		////				edit.commit();
		////			}
		//		}
	}

	private void showListDateFormatDialog() {
		final CharSequence[] items = { getString(R.string.unitprefs_date_option2_caption),
				getString(R.string.unitprefs_date_option1_caption),
				getString(R.string.unitprefs_date_option3_caption),
				getString(R.string.unitprefs_date_option4_caption)};

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.daylight_exposure_select));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				// flag_ischange = true;
				mDateFormat.setText(items[item]);
				switch (item) {
				case 0: // DD/MM/YY
					getLifeTrakApplication().getTimeDate().setDateFormat(DATE_FORMAT_DDMM);

					break;
				case 1: // MM/DD/YY
					getLifeTrakApplication().getTimeDate().setDateFormat(DATE_FORMAT_MMDD);
					break;
				case 2: // MMM/DD
					getLifeTrakApplication().getTimeDate().setDateFormat(DATE_FORMAT_MMMDD);
					break;
				case 3: // DD/MMM
					getLifeTrakApplication().getTimeDate().setDateFormat(DATE_FORMAT_DDMMM);
					break;
				}
				getLifeTrakApplication().getTimeDate().update();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void setDateFormat() {
		final CharSequence[] items = { getString(R.string.unitprefs_date_option1_caption),
				getString(R.string.unitprefs_date_option2_caption),
				getString(R.string.unitprefs_date_option3_caption),
				getString(R.string.unitprefs_date_option4_caption) };
		switch (getLifeTrakApplication().getTimeDate().getDateFormat()) {
		case DATE_FORMAT_DDMM: // DD/MM/YY
			mDateFormat.setText(items[0]);
			break;
		case DATE_FORMAT_MMDD: // MM/DD/YY
			mDateFormat.setText(items[1]);
			break;
		case DATE_FORMAT_MMMDD: // MMM/DD
			mDateFormat.setText(items[2]);
			break;
		case DATE_FORMAT_DDMMM: // DD/MMM
			mDateFormat.setText(items[3]);
			break;
		}
	}

	private void checkSyncReminderValue(){

		boolean is24Hours = false;
		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
		case TIME_FORMAT_12_HR:
			is24Hours= false;
			break;
		case TIME_FORMAT_24_HR:
			is24Hours = true;
			break;

		}

		mDataSyncReminderAlert.setChecked(mPreferenceWrapper.getPreferenceBooleanValue(SalutronLifeTrakUtility.AUTOSYNCTIME));
		boolean weekchecker = mPreferenceWrapper.getPreferenceBooleanValue(SalutronLifeTrakUtility.SYNC_WEEK_VALUE);
		if (mDataSyncReminderAlert.isChecked()){
			long timeChecker = mPreferenceWrapper.getPreferenceLongValue(SalutronLifeTrakUtility.TIME_ALERT);
			String AM_PM = "";
			if (!weekchecker){
				setIsOnceADay(true);
				if (timeChecker != 0){
					//Day
					//mOnceADay.setVisibility(View.VISIBLE);
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(new Date(timeChecker));
					int hours = calendar.get(Calendar.HOUR_OF_DAY);
					int minutes = calendar.get(Calendar.MINUTE);

					if (is24Hours){
						mOnceADay.setText(String.format("%02d", hours) + ":" + String.format("%02d", minutes));
					}
					else{
						if(hours < 12) {
							AM_PM = getString(R.string.am);

						} else {
							AM_PM = getString(R.string.pm);
							hours = hours-12;
						}
						if (hours == 0)
							hours = 12;

						mOnceADay.setText(String.format("%02d", hours) + ":" + String.format("%02d", minutes) + " " + AM_PM);
					}
				}
				else{
					if (is24Hours)
						mOnceADay.setText("9:00");
					else
						mOnceADay.setText("9:00 " + getString(R.string.am));
				}
			}
			else
			{
				setIsOnceADay(false);
				if (timeChecker != 0){
					//Week
					//mOnceAWeekTime.setVisibility(View.VISIBLE);
					//mOnceAWeekDay.setVisibility(View.VISIBLE);
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(new Date(timeChecker));
					int hours = calendar.get(Calendar.HOUR_OF_DAY);
					int minutes = calendar.get(Calendar.MINUTE);

					if (is24Hours){
						mOnceAWeekTime.setText(String.format("%02d", hours) + ":" + String.format("%02d", minutes));
					}
					else{
						if(hours < 12) {
							AM_PM = getString(R.string.am);

						} else {
							AM_PM = getString(R.string.pm);
							hours = hours-12;
						}

						if (hours == 0)
							hours = 12;
						mOnceAWeekTime.setText(String.format("%02d", hours) + ":" + String.format("%02d", minutes) + " " + AM_PM);
					}
					mOnceAWeekDay.setText(mPreferenceWrapper.getPreferenceStringValue(SalutronLifeTrakUtility.SYNC_DAY));
				}
				else{
					if (is24Hours)
						mOnceAWeekTime.setText("9:00");
					else
						mOnceAWeekTime.setText("9:00 " + getString(R.string.am));
					mOnceAWeekDay.setText(getString(R.string.monday));
				}

			}
		}
	}

	private void getSettingsData(){
		dayLightDetectSettings = DataSource.getInstance(getActivity()).getReadOperation()
				.query("watchDaylightSetting = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId())).getResults(DayLightDetectSetting.class);

		if (dayLightDetectSettings.size() > 0) {
			mDayLightDetectSetting = dayLightDetectSettings.get(0);
		}

		wakeSettings = DataSource.getInstance(getActivity()).getReadOperation().query("watchWakeupSetting = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(WakeupSetting.class);

		if (wakeSettings.size() > 0) {
			mWakeupSetting = wakeSettings.get(0);
		}

		alertSettings = DataSource.getInstance(getActivity()).getReadOperation().query("watchActivityAlert = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(ActivityAlertSetting.class);

		if (alertSettings.size() > 0) {
			mActivityAlertSettings = alertSettings.get(0);
		}

		nightLightDetectSetting = DataSource.getInstance(getActivity()).getReadOperation()
				.query("watchNightlightSetting = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId())).getResults(NightLightDetectSetting.class);
	}

	public void setCancelledSyncing (boolean mBoolean){
		this.isCancelledSyncing = mBoolean;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.relative_notification:
				Intent intent = new Intent(getActivity(), NotificationSettingActivity.class);
				startActivity(intent);
				break;
			case R.id.button_sync_setting_watch:
				if (mMainActivity == null)
					mMainActivity = (MainActivity) getActivity();
				mMainActivity.onSyncGoalMenuItemClick();
				break;
			case R.id.textview_hour_format:
				showListHourFormatDialog();
				break;
			case R.id.textview_unit_selection:
				showListUnitFormatDialog();
				break;
			case R.id.textview_watch_display_watch_settings:
				showListDisplayFormatDialog();
				break;
			case R.id.textview_datetime:
				if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415)
					showListDateFormatDialog();
				else
					showListDateFormatDialogForC300C410();
				break;
			case R.id.button_sync_to_cloud:

				if (mDeviceName.getText().toString().trim().equals("")){
				getLifeTrakApplication().getSelectedWatch().setName(lastWatchName);
				getLifeTrakApplication().getSelectedWatch().update();
				mWatchName.setText(lastWatchName);
				mDeviceName.setText(lastWatchName);
				}
				else{
					getLifeTrakApplication().getSelectedWatch().setName(mDeviceName.getText().toString());
					getLifeTrakApplication().getSelectedWatch().update();
					lastWatchName = mDeviceName.getText().toString();
				}

				if (NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
					if (mMainActivity == null)
						mMainActivity = (MainActivity) getActivity();

					if (mMainActivity.mProgressDialog == null)
						mMainActivity.reinitializeProgress();
					mMainActivity.mProgressDialog.setMessage(getString(R.string.sync_to_cloud));
					mMainActivity.mProgressDialog.show();

					Date expirationDate = getExpirationDate();
					Date now = new Date();

					if (now.after(expirationDate)) {
						mMainActivity.refreshTokenFromFragment();
					} else {
						mMainActivity.startCheckingServerFromFragment();
					}
				} else {
					AlertDialog alert = new AlertDialog.Builder(getActivity())
							.setTitle(R.string.lifetrak_title)
							.setMessage(R.string.check_network_connection)
							.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();
								}
							}).create();
					alert.show();
				}
				break;
			case R.id.textView_switch_watch:

				AlertDialog alert = new AlertDialog.Builder(getActivity())
						.setTitle(R.string.disconnect_caption)
						.setMessage(R.string.disconnection_message)
						.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						})

						.setPositiveButton(R.string.continue_button_text, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
								mPreferenceWrapper.setPreferenceBooleanValue(HAS_USER_PROFILE, true).synchronize();
								Intent intentSwitchWatch = new Intent(getActivity(), WelcomePageActivity.class);
								intentSwitchWatch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
								startActivity(intentSwitchWatch);
								getActivity().finish();
							}
						})
						.create();
				alert.show();

				break;
			case R.id.textview_hr_logging_rate:
				showListHRDialog();
				break;

			case R.id.textview_workout_reconnect_timeout:
				showListReconnectDialog();
				break;

			case R.id.textView_reset_workout:
				if (mMainActivity == null)
					mMainActivity = (MainActivity) getActivity();
				flag_reset_workout = true;
				flag_update_firmware = false;
				mMainActivity.onSyncGoalMenuItemClick();
				break;

			case R.id.button_update_watch:
				AlertDialog alertUpdate = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.update_firmware_desc)
						.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
											arg0.dismiss();
										flag_update_firmware = true;
										flag_reset_workout =false;
										if (mMainActivity == null)
											mMainActivity = (MainActivity) getActivity();
										mMainActivity.onSyncGoalMenuItemClick();
									}
						}
						)
						.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								//						service.disconnectFromDevice();
								arg0.dismiss();
							}
						}).create();
				alertUpdate.show();

				break;

		}

//		if (v == mNotification) {
//
//		}
	}


	private void showListHourFormatDialog() {
		final CharSequence[] items = {
				getString(R.string.caption_12_hour),
				getString(R.string.caption_24_hour)};

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(true);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
					case 0:
						mTextviewHourFormat.setText(getString(R.string.caption_12_hour));
						getLifeTrakApplication().getTimeDate().setHourFormat(TIME_FORMAT_12_HR);
						setDataSyncReminderAlertTimeFormat(TIME_FORMAT_12_HR);
						break;
					case 1:
						mTextviewHourFormat.setText(getString(R.string.caption_24_hour));
						getLifeTrakApplication().getTimeDate().setHourFormat(TIME_FORMAT_24_HR);
						setDataSyncReminderAlertTimeFormat(TIME_FORMAT_24_HR);
						break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}


	private void showListUnitFormatDialog() {
		final CharSequence[] items = {
				getString(R.string.caption_unit_imperial),
				getString(R.string.caption_unit_metric)};

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(true);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
					case 0:
						mTextviewUnitSelection.setText(getString(R.string.caption_unit_imperial));
						getLifeTrakApplication().getUserProfile().setUnitSystem(UNIT_IMPERIAL);
						break;
					case 1:
						mTextviewUnitSelection.setText(getString(R.string.caption_unit_metric));
						getLifeTrakApplication().getUserProfile().setUnitSystem(UNIT_METRIC);
						break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showListDateFormatDialogForC300C410() {
		final CharSequence[] items = {
				getString(R.string.unitprefs_date_option1_caption),
				getString(R.string.unitprefs_date_option2_caption)};

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(true);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				mDateFormat.setText(items[item]);
				switch (item) {
					case 0:
						getLifeTrakApplication().getTimeDate().setDateFormat(DATE_FORMAT_MMDD);
						break;
					case 1:
						getLifeTrakApplication().getTimeDate().setDateFormat(DATE_FORMAT_DDMM);
						break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}


	private void showListDisplayFormatDialog() {
		final CharSequence[] items = {
				getString(R.string.caption_simple),
				getString(R.string.caption_full)};

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(true);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
					case 0:
						mTextviewWatchDisplay.setText(getString(R.string.caption_simple));
						getLifeTrakApplication().getTimeDate().setDisplaySize(DISPLAY_FORMAT_BIG_DIGIT);
						break;
					case 1:
						mTextviewWatchDisplay.setText(getString(R.string.caption_full));
						getLifeTrakApplication().getTimeDate().setDisplaySize(DISPLAY_FORMAT_SMALL_DIGIT);
						break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showListHRDialog() {
		final CharSequence[] items = {
				"1",
				"2",
				"3",
				"4",
				"5"};


		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(true);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
					case 0:
						textviewHRLoggingRate.setText("1 sec");
						if (mWorkoutSettings!= null){
							mWorkoutSettings.setHrLoggingRate(1);
							mWorkoutSettings.setContext(getActivity());
							mWorkoutSettings.update();
						}
						//mPreferenceWrapper.setPreferenceStringValue(R420_HR_LOG_RATE, "1").synchronize();
						break;
					case 1:
						textviewHRLoggingRate.setText("2 secs");
						//mPreferenceWrapper.setPreferenceStringValue(R420_HR_LOG_RATE, "2").synchronize();
						if (mWorkoutSettings!= null) {
							mWorkoutSettings.setHrLoggingRate(2);
							mWorkoutSettings.setContext(getActivity());
							mWorkoutSettings.update();
						}
						break;
					case 2:
						textviewHRLoggingRate.setText("3 secs");
						if (mWorkoutSettings!= null) {
							mWorkoutSettings.setHrLoggingRate(3);
							mWorkoutSettings.setContext(getActivity());
							mWorkoutSettings.update();
						}
						break;
					case 3:
						textviewHRLoggingRate.setText("4 secs");
						if (mWorkoutSettings!= null) {
							mWorkoutSettings.setHrLoggingRate(4);
							mWorkoutSettings.setContext(getActivity());
							mWorkoutSettings.update();
						}
						break;
					case 4:
						textviewHRLoggingRate.setText("5 secs");
						if (mWorkoutSettings!= null) {
							mWorkoutSettings.setHrLoggingRate(5);
							mWorkoutSettings.setContext(getActivity());
							mWorkoutSettings.update();
						}
						break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}


	private void showListReconnectDialog() {
		final CharSequence[] items = {
				"5",
				"6",
				"7",
				"8",
				"9",
				"10",
				"11",
				"12",
				"13",
				"14",
				"15",
				"16",
				"17",
				"18",
				"19",
				"20",
				"21",
				"22",
				"23",
				"24",
				"25",
				"26",
				"27",
				"28",
				"29",
				"30"

		};


		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(true);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				textviewWorkOutReconnectTime.setText( String.valueOf(item + 5) + " secs");
				if (mWorkoutSettings!= null) {
					mWorkoutSettings.setReconnectTime(item + 5);
					mWorkoutSettings.setContext(getActivity());
					mWorkoutSettings.update();
				}
//				switch (item) {
//					case 0:
//						textviewWorkOutReconnectTime.setText("5 secs");
//						if (mWorkoutSettings!= null){
//							mWorkoutSettings.setReconnectTime(5);
//							mWorkoutSettings.setContext(getActivity());
//							mWorkoutSettings.update();
//						}
//						//mPreferenceWrapper.setPreferenceStringValue(R420_HR_LOG_RATE, "1").synchronize();
//						break;
//					case 1:
//						textviewWorkOutReconnectTime.setText("6 secs");
//						//mPreferenceWrapper.setPreferenceStringValue(R420_HR_LOG_RATE, "2").synchronize();
//						if (mWorkoutSettings!= null) {
//							mWorkoutSettings.setReconnectTime(6);
//							mWorkoutSettings.setContext(getActivity());
//							mWorkoutSettings.update();
//						}
//						break;
//					case 2:
//						textviewWorkOutReconnectTime.setText("7 secs");
//						if (mWorkoutSettings!= null) {
//							mWorkoutSettings.setReconnectTime(7);
//							mWorkoutSettings.setContext(getActivity());
//							mWorkoutSettings.update();
//						}
//						break;
//					case 3:
//						textviewWorkOutReconnectTime.setText("8 secs");
//						if (mWorkoutSettings!= null) {
//							mWorkoutSettings.setReconnectTime(8);
//							mWorkoutSettings.setContext(getActivity());
//							mWorkoutSettings.update();
//						}
//						break;
//					case 4:
//						textviewWorkOutReconnectTime.setText("9 secs");
//						if (mWorkoutSettings!= null) {
//							mWorkoutSettings.setReconnectTime(9);
//							mWorkoutSettings.setContext(getActivity());
//							mWorkoutSettings.update();
//						}
//						break;
//					case 5:
//
//						break;
//				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void hideViews(){
		if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
				((View) getView().findViewById(R.id.viewunit1)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view1)).setVisibility(View.VISIBLE);
				((View) getView().findViewById(R.id.view2)).setVisibility(View.VISIBLE);
				((Switch) getView().findViewById(R.id.data_sync_reminder_alert_switch)).setVisibility(View.VISIBLE);
				((TableLayout) getView().findViewById(R.id.data_sync_reminder_alert_group)).setVisibility(View.VISIBLE);
				mDateFormatForCModel.setVisibility(View.GONE);
				mTwoDateFormat.setVisibility(View.GONE);
				((View) getView().findViewById(R.id.line_for_button_update_watch)).setVisibility(View.GONE);
				((Button) getView().findViewById(R.id.button_update_watch)).setVisibility(View.GONE);
			//	break;
		 }
		else if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_C410) {
				((View) getView().findViewById(R.id.view1)).setVisibility(View.GONE);
				((Switch) getView().findViewById(R.id.data_sync_reminder_alert_switch)).setVisibility(View.GONE);
				((LinearLayout) getView().findViewById(R.id.Linear_watch_display)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view_for_watch_display)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view_for_alarm)).setVisibility(View.GONE);
				((Button) getView().findViewById(R.id.alert_settings)).setVisibility(View.GONE);
				((RelativeLayout) getView().findViewById(R.id.relative_notification)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view_for_notification)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.viewunit1)).setVisibility(View.GONE);
				((TableLayout) getView().findViewById(R.id.data_sync_reminder_alert_group)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view1)).setVisibility(View.GONE);
				((Switch) getView().findViewById(R.id.data_sync_reminder_alert_switch)).setVisibility(View.GONE);
				((TableLayout) getView().findViewById(R.id.data_sync_reminder_alert_group)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.line_for_button_update_watch)).setVisibility(View.GONE);
				((Button) getView().findViewById(R.id.button_update_watch)).setVisibility(View.GONE);
				//break;
			}
			else if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420) {
				((View) getView().findViewById(R.id.view1)).setVisibility(View.GONE);
				((Switch) getView().findViewById(R.id.data_sync_reminder_alert_switch)).setVisibility(View.GONE);
				((TableLayout) getView().findViewById(R.id.data_sync_reminder_alert_group)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view_for_workout)).setVisibility(View.VISIBLE);
				((RelativeLayout) getView().findViewById(R.id.relative_workout)).setVisibility(View.VISIBLE);
				((View) getView().findViewById(R.id.view_for_workout_hr_logging)).setVisibility(View.VISIBLE);
				((LinearLayout) getView().findViewById(R.id.Linear_workout_hr_logging)).setVisibility(View.VISIBLE);
				((View) getView().findViewById(R.id.view_for_workout_storage_left)).setVisibility(View.VISIBLE);
				((LinearLayout) getView().findViewById(R.id.Linear_workout_storage_left)).setVisibility(View.VISIBLE);
				((View) getView().findViewById(R.id.view_for_reconnect_timeout)).setVisibility(View.VISIBLE);
				((LinearLayout) getView().findViewById(R.id.Linear_workout_reconnect_timeout)).setVisibility(View.VISIBLE);
				((TableLayout) getView().findViewById(R.id.data_sync_reminder_alert_group)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view_for_notification)).setVisibility(View.GONE);
				((LinearLayout) getView().findViewById(R.id.Linear_watch_display)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view_for_alarm)).setVisibility(View.GONE);
				((Button) getView().findViewById(R.id.alert_settings)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view_for_watch_display)).setVisibility(View.GONE);
				mNotification.setVisibility(View.GONE);
				((View) getView().findViewById(R.id.line_for_button_update_watch)).setVisibility(View.VISIBLE);
				((Button) getView().findViewById(R.id.button_update_watch)).setVisibility(View.VISIBLE);
				//break;
			}
			else if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_C300 ||
				getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_C300_IOS) {
				((View) getView().findViewById(R.id.view1)).setVisibility(View.GONE);
				((Switch) getView().findViewById(R.id.data_sync_reminder_alert_switch)).setVisibility(View.GONE);
				((LinearLayout) getView().findViewById(R.id.Linear_watch_display)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view_for_watch_display)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view_for_alarm)).setVisibility(View.GONE);
				((Button) getView().findViewById(R.id.alert_settings)).setVisibility(View.GONE);
				((RelativeLayout) getView().findViewById(R.id.relative_notification)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view_for_notification)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.viewunit1)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view_for_smart_calib)).setVisibility(View.GONE);
				((RelativeLayout) getView().findViewById(R.id.relative_smart_calibration)).setVisibility(View.GONE);
				((TableLayout) getView().findViewById(R.id.data_sync_reminder_alert_group)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.view1)).setVisibility(View.GONE);
				((Switch) getView().findViewById(R.id.data_sync_reminder_alert_switch)).setVisibility(View.GONE);
				((TableLayout) getView().findViewById(R.id.data_sync_reminder_alert_group)).setVisibility(View.GONE);
				((View) getView().findViewById(R.id.line_for_button_update_watch)).setVisibility(View.GONE);
				((Button) getView().findViewById(R.id.button_update_watch)).setVisibility(View.GONE);
				//break;
			}


	}

	private TextView.OnEditorActionListener mWatchNameEditorAction() {
		return new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					if (!v.getText().toString().trim().equals("")) {
						getLifeTrakApplication().getSelectedWatch().setName(v.getText().toString());
						getLifeTrakApplication().getSelectedWatch().update();
						getLifeTrakApplication().getSelectedWatch().setName(v.getText().toString());
						mWatchName.setText(getLifeTrakApplication().getSelectedWatch().getName());

					} else {
						mWatchName.setText(lastWatchName);
						mDeviceName.setText(lastWatchName);
					}
				}
				return false;
			}
		};
	}


	private void getWorkoutSettingsData(){
		if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420){
			List<WorkoutSettings> userWorkOutFromDB = DataSource.getInstance(getActivity())
					.getReadOperation()
					.query("watchDataHeader = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
					.getResults(WorkoutSettings.class);

			if (userWorkOutFromDB.size() > 0){
				mWorkoutSettings = userWorkOutFromDB.get(0);
			}
		}
	}

	private void updateFirmWare(final BluetoothDevice device, final SALBLEService service, final Watch watch){
		service.registerDevDataHandler(timeHandler);
		service.registerDevListHandler(timeHandler);
		int status = service.getFirmwareRevision();

		if (watch.getModel() == WATCHMODEL_R420) {
			if (status == SALStatus.NO_ERROR){
				LifeTrakLogger.info("getFirmwareRevision status : "+ status);
			}
		}
		else{
			if (mMainActivity.mProgressDialog == null)
				mMainActivity.reinitializeProgress();
			mMainActivity.mProgressDialog.dismiss();
			AlertDialog alertUpdate = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.update_firmware_desc_unsuccessful)
					.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();

								}
							}
					).create();
			alertUpdate.show();
		}
	}

	private void onGetFirmware(String firmware){
		Pattern pattern = Pattern.compile("[^0-9]");
		Matcher matcher = pattern.matcher(firmware);
		String number = matcher.replaceAll("");

		mMainActivity.watchRegisterHandler();

		if (Integer.parseInt(number) > 210) {
			int status = mService.startFirmwareUpdate();
			if (status == SALStatus.NO_ERROR){
				AlertDialog alertUpdate = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.update_firmware_desc_successful)
						.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										arg0.dismiss();

									}
								}
						).create();
				alertUpdate.show();

			}
			else{
				AlertDialog alertUpdate = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.update_firmware_desc_unsuccessful)
						.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										arg0.dismiss();

									}
								}
						).create();
				alertUpdate.show();
			}

		}
		else{

			AlertDialog alertUpdate = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.update_firmware_desc_unsuccessful_device_unsupported)
					.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();

								}
							}
					).create();
			alertUpdate.show();
		}
		if (mMainActivity.mProgressDialog == null)
			mMainActivity.reinitializeProgress();
		mMainActivity.mProgressDialog.dismiss();
	}


}

