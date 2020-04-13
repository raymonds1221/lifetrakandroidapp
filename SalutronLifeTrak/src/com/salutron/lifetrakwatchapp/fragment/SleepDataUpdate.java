package com.salutron.lifetrakwatchapp.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TimePicker;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.SleepDatabaseDeleted;
import com.salutron.lifetrakwatchapp.util.DialogActivity;
import com.salutron.lifetrakwatchapp.util.DialogActivityNetworkError;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.LifeTrakWatchUtil;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;
import com.salutron.lifetrakwatchapp.web.AsyncListener;
import com.salutron.lifetrakwatchapp.web.ServerSyncAsyncTask;
import com.salutron.lifetrakwatchapp.web.SleepAsyncTask;

import org.json.JSONObject;

import roboguice.inject.InjectView;

public class SleepDataUpdate extends BaseFragment implements AsyncListener {
	@InjectView(R.id.tvwSleepDate)
	private TextView mSleepDate;
	@InjectView(R.id.edtStartTime)
	private EditText mSleepStartTime;
	@InjectView(R.id.edtEndTime)
	private EditText mSleepEndTime;
	@InjectView(R.id.btnSave)
	private Button mButtonSave;

	private SleepDatabase mSleepDatabase;
	private int mSleepStartHour;
	private int mSleepStartMin;
	private int mSleepEndHour;
	private int mSleepEndMin;

	private TimePickerDialog mStartSleepTimePicker;
	private TimePickerDialog mEndSleepTimePicker;
	private InputMethodManager mInputMethodManager;

	private SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private SimpleDateFormat mDateFormat2 = (SimpleDateFormat) DateFormat.getInstance();

	private List<SleepDatabase> mSleepDatabases = new ArrayList<SleepDatabase>();
	private AlertDialog mAlertExists;

	private long sleepDuration;
	private Calendar calNow;

	private static final int OPERATION_SYNC_SLEEP = 0x01;
	private static final int OPERATION_DELETE_SLEEP = 0x02;

	private int mCurrentOperation = OPERATION_SYNC_SLEEP;

	private ProgressDialog mProgressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_sleep_data_update, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getActivity() != null) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		mProgressDialog = new ProgressDialog(getActivity());

		mProgressDialog.setTitle(R.string.lifetrak_title);
		mProgressDialog.setMessage(getString(R.string.sync_to_cloud));
		mProgressDialog.setCancelable(false);
		mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				mProgressDialog.dismiss();
				}
			});


		initializeObjects();
		hideCalendar();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_delete_sleep_log, menu);
	}

	private void initializeObjects() {
		mDateFormat.applyPattern("MMMM dd, yyyy");
		mDateFormat2.applyPattern("hh:mm aa");

		mAlertExists = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.sleep_database_exists).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}
		}).create();

		Calendar calYesterday = Calendar.getInstance();
		calYesterday.setTime(getLifeTrakApplication().getCurrentDate());
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);

		int dayYesterday = calYesterday.get(Calendar.DAY_OF_MONTH);
		int monthYesterday = calYesterday.get(Calendar.MONTH) + 1;
		int yearYesterday = calYesterday.get(Calendar.YEAR) - 1900;

		Calendar calNow = Calendar.getInstance();
		calNow.setTime(getLifeTrakApplication().getCurrentDate());

		int day = calNow.get(Calendar.DAY_OF_MONTH);
		int month = calNow.get(Calendar.MONTH) + 1;
		int year = calNow.get(Calendar.YEAR) - 1900;

		List<SleepDatabase> sleepYesterday = DataSource
				.getInstance(getActivity())
				.getReadOperation()
				.query("watchSleepDatabase = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and (hourSleepStart > hourSleepEnd or (hourSleepStart >= hourSleepEnd and minuteSleepStart > minuteSleepEnd))",
						String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(dayYesterday), String.valueOf(monthYesterday), String.valueOf(yearYesterday))
				.getResults(SleepDatabase.class);

		List<SleepDatabase> sleepNow = DataSource
				.getInstance(getActivity())
				.getReadOperation()
				.query("watchSleepDatabase = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and (hourSleepEnd > hourSleepStart or (hourSleepEnd >= hourSleepStart and minuteSleepEnd > minuteSleepStart))",
						String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(day), String.valueOf(month), String.valueOf(year)).getResults(SleepDatabase.class);

		for (SleepDatabase sleepDatabase : sleepYesterday) {
			mSleepDatabases.add(sleepDatabase);
		}

		for (SleepDatabase sleepDatabase : sleepNow) {
			mSleepDatabases.add(sleepDatabase);
		}

		if (getArguments() != null) {
			mSleepDatabase = getArguments().getParcelable(SLEEP_DATABASE);
			mSleepStartHour = mSleepDatabase.getHourSleepStart();
			mSleepStartMin = mSleepDatabase.getMinuteSleepStart();
			mSleepEndHour = mSleepDatabase.getHourSleepEnd();
			mSleepEndMin = mSleepDatabase.getMinuteSleepEnd();

			if (getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_24_HR) {
				mSleepStartTime.setText(String.format("%d:%02d", mSleepStartHour, mSleepStartMin));
				mSleepEndTime.setText(String.format("%d:%02d", mSleepEndHour, mSleepEndMin));
			} else {
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, mSleepStartHour);
				calendar.set(Calendar.MINUTE, mSleepStartMin);

				mSleepStartTime.setText(mDateFormat2.format(calendar.getTime()));

				calendar.set(Calendar.HOUR_OF_DAY, mSleepEndHour);
				calendar.set(Calendar.MINUTE, mSleepEndMin);

				mSleepEndTime.setText(mDateFormat2.format(calendar.getTime()));
			}

			setHasOptionsMenu(true);
		} else {
			if (getLifeTrakApplication().getTimeDate().getHourFormat()== TIME_FORMAT_24_HR) {
				mSleepStartTime.setText("0:00");
				mSleepEndTime.setText("0:00");
			}
			else{
				mSleepStartTime.setText("12:00 AM");
				mSleepEndTime.setText("12:00 AM");
			}
			setHasOptionsMenu(false);
		}

		mSleepDate.setText(getString(R.string.sleep_date_label, mDateFormat.format(getLifeTrakApplication().getCurrentDate())));
		mButtonSave.setOnClickListener(mOnClickListener);
		mInputMethodManager = (InputMethodManager) getView().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

		boolean is24HourView = false;

		if (getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_24_HR)
			is24HourView = true;

		mStartSleepTimePicker = new TimePickerDialog(getActivity(), mOnStartTimeListener, mSleepStartHour, mSleepStartMin, is24HourView);
		mEndSleepTimePicker = new TimePickerDialog(getActivity(), mOnEndTimeListener, mSleepEndHour, mSleepEndMin, is24HourView);
		mSleepStartTime.setOnClickListener(mTimeClickListener);
		mSleepEndTime.setOnClickListener(mTimeClickListener);
		mSleepStartTime.setOnFocusChangeListener(mFocusChangeListener);
		mSleepEndTime.setOnFocusChangeListener(mFocusChangeListener);
		mSleepStartTime.setInputType(InputType.TYPE_NULL);
		mSleepEndTime.setInputType(InputType.TYPE_NULL);
	}

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			Date currentDate = new Date();
			Calendar calStart = Calendar.getInstance();
			Calendar calEnd = Calendar.getInstance();

			calNow = Calendar.getInstance();
			int hour = calNow.get(Calendar.HOUR_OF_DAY);
			int minute = calNow.get(Calendar.MINUTE);

			calStart.setTime(getLifeTrakApplication().getCurrentDate());
			calStart.set(Calendar.HOUR_OF_DAY, mSleepStartHour);
			calStart.set(Calendar.MINUTE, mSleepStartMin);
			calStart.set(Calendar.SECOND, 0);

			calEnd.setTime(getLifeTrakApplication().getCurrentDate());
			
			calEnd.set(Calendar.HOUR_OF_DAY, mSleepEndHour);
			calEnd.set(Calendar.MINUTE, mSleepEndMin);
			calEnd.set(Calendar.SECOND, 0);

			int nowDay = calNow.get(Calendar.DAY_OF_MONTH);
			int nowMonth = calNow.get(Calendar.MONTH);
			int nowYear = calNow.get(Calendar.YEAR);
			
			int startDay = calStart.get(Calendar.DAY_OF_MONTH);
			int startMonth = calStart.get(Calendar.MONTH);
			int startYear = calStart.get(Calendar.YEAR);
			
			int endDay = calEnd.get(Calendar.DAY_OF_MONTH);
			int endMonth = calEnd.get(Calendar.MONTH);
			int endYear = calEnd.get(Calendar.YEAR);
			
			calEnd.set(Calendar.DAY_OF_MONTH, endDay);
			calEnd.set(Calendar.MONTH, endMonth);
			calEnd.set(Calendar.YEAR, endYear);
			
			calStart.set(Calendar.DAY_OF_MONTH, startDay);
			calStart.set(Calendar.MONTH, startMonth);
			calStart.set(Calendar.YEAR, startYear);
			if (getLifeTrakApplication().getTimeDate().getHourFormat()!= TIME_FORMAT_24_HR) {
				if (mSleepStartTime.getText().toString().equals("0:00") || mSleepEndTime.getText().toString().equals("0:00")) {
					AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.sleep_time_invalid)
							.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
								}
							}).create();
					alert.show();
					return;
				}
			}
			if (calStart.getTime().after(calEnd.getTime())) {

				if ((mSleepEndHour > hour || (mSleepEndHour >= hour && mSleepEndMin > minute)) && (nowDay == endDay && nowMonth == endMonth && nowYear == endYear)) {
					AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.end_time_invalid)
							.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
								}
							}).create();
					alert.show();
					return;
				}
				if (calEnd.getTime().after(new Date())) {
					AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.end_time_invalid)
							.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
								}
							}).create();
					alert.show();
					return;
				}

			}

			if ((mSleepStartHour > mSleepEndHour) || (mSleepStartHour >= mSleepEndHour && mSleepStartMin > mSleepEndMin)) {
					calStart.add(Calendar.DAY_OF_MONTH, -1);
			}

			Date date = calStart.getTime();
			Date date2 = calEnd.getTime();
			Log.i(TAG, "date: " + date + " date2: " + date2 + " current date: " + currentDate);

			long diff = calEnd.getTimeInMillis() - calStart.getTimeInMillis();
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			sleepDuration = diffHours * 60 + diffMinutes;
			
			if(sleepDuration >= (14 * 60) + 51){
				AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle("LifeTrak").setMessage(R.string.sleep_time_exceed)
						.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
							}
						}).create();
				alert.show();
				return;
				
			}

			if (mSleepDatabase != null) {
				calNow.set(Calendar.DAY_OF_MONTH, mSleepDatabase.getDateStampDay());
				calNow.set(Calendar.MONTH, mSleepDatabase.getDateStampMonth() - 1);
				calNow.set(Calendar.YEAR, mSleepDatabase.getDateStampYear() + 1900);
			} else {
				calNow.setTime(getLifeTrakApplication().getCurrentDate());
			}

			int day = calNow.get(Calendar.DAY_OF_MONTH);
			int month = calNow.get(Calendar.MONTH) + 1;
			int year = calNow.get(Calendar.YEAR) - 1900;

			if ((mSleepStartHour > mSleepEndHour) || (mSleepStartHour >= mSleepEndHour && mSleepStartMin > mSleepEndMin)) {
				if (mPreferenceWrapper.getPreferenceBooleanValue(ADD_NEW_SLEEP))
					calNow.add(Calendar.DAY_OF_MONTH, -1);

				day = calNow.get(Calendar.DAY_OF_MONTH);
				month = calNow.get(Calendar.MONTH) + 1;
				year = calNow.get(Calendar.YEAR) - 1900;
			}

			if (mSleepDatabase != null) {
				SleepDatabase sleepDatabase1 = new SleepDatabase(getActivity());
				sleepDatabase1.setHourSleepStart(mSleepStartHour);
				sleepDatabase1.setMinuteSleepStart(mSleepStartMin);
				sleepDatabase1.setHourSleepEnd(mSleepEndHour);
				sleepDatabase1.setMinuteSleepEnd(mSleepEndMin);
				sleepDatabase1.setSleepDuration((int) sleepDuration);
				sleepDatabase1.setDateStampDay(day);
				sleepDatabase1.setIsModified(true);
				sleepDatabase1.setDateStampMonth(month);
				sleepDatabase1.setDateStampYear(year);
				sleepDatabase1.setWatch(getLifeTrakApplication().getSelectedWatch());
				sleepDatabase1.setSyncedToCloud(mSleepDatabase.isSyncedToCloud());
				if (isSleepDatabaseOverlap(sleepDatabase1, true)) {
					mAlertExists.show();
				} else {
					sendDataToServer((int)sleepDuration);
				}
			} else {

					SleepDatabase sleepDatabase = new SleepDatabase(getActivity());
					sleepDatabase.setHourSleepStart(mSleepStartHour);
					sleepDatabase.setMinuteSleepStart(mSleepStartMin);
					sleepDatabase.setHourSleepEnd(mSleepEndHour);
					sleepDatabase.setMinuteSleepEnd(mSleepEndMin);
					sleepDatabase.setSleepDuration((int) sleepDuration);
					sleepDatabase.setDateStampDay(day);
					sleepDatabase.setDateStampMonth(month);
					sleepDatabase.setDateStampYear(year);
					sleepDatabase.setIsModified(false);
				 	sleepDatabase.setIsWatch(false);
					sleepDatabase.setWatch(getLifeTrakApplication().getSelectedWatch());
					if (isSleepDatabaseOverlap(sleepDatabase, false)) {
						mAlertExists.show();
					} else {
						saveSleepDatabase(false);
					}
			}



		}
	};

	private void saveSleepDatabase(boolean isSyncToCloud){
		if (mSleepDatabase != null) {
			calNow.set(Calendar.DAY_OF_MONTH, mSleepDatabase.getDateStampDay());
			calNow.set(Calendar.MONTH, mSleepDatabase.getDateStampMonth() - 1);
			calNow.set(Calendar.YEAR, mSleepDatabase.getDateStampYear() + 1900);
		} else {
			calNow.setTime(getLifeTrakApplication().getCurrentDate());
		}

		int day = calNow.get(Calendar.DAY_OF_MONTH);
		int month = calNow.get(Calendar.MONTH) + 1;
		int year = calNow.get(Calendar.YEAR) - 1900;

		if ((mSleepStartHour > mSleepEndHour) || (mSleepStartHour >= mSleepEndHour && mSleepStartMin > mSleepEndMin)) {
			if (mPreferenceWrapper.getPreferenceBooleanValue(ADD_NEW_SLEEP))
				calNow.add(Calendar.DAY_OF_MONTH, -1);

			day = calNow.get(Calendar.DAY_OF_MONTH);
			month = calNow.get(Calendar.MONTH) + 1;
			year = calNow.get(Calendar.YEAR) - 1900;
		}

		if (mSleepDatabase != null) {
			mSleepDatabase.setContext(getActivity());
			mSleepDatabase.setHourSleepStart(mSleepStartHour);
			mSleepDatabase.setMinuteSleepStart(mSleepStartMin);
			mSleepDatabase.setHourSleepEnd(mSleepEndHour);
			mSleepDatabase.setMinuteSleepEnd(mSleepEndMin);
			mSleepDatabase.setSleepDuration((int) sleepDuration);
			mSleepDatabase.setDateStampDay(day);
			mSleepDatabase.setDateStampMonth(month);
			mSleepDatabase.setDateStampYear(year);
			mSleepDatabase.setWatch(getLifeTrakApplication().getSelectedWatch());
			mSleepDatabase.setIsModified(true);
			if (isSyncToCloud){
				mSleepDatabase.setSyncedToCloud(mSleepDatabase.isSyncedToCloud());
			}
			else{
				mSleepDatabase.setSyncedToCloud(false);
			}

			if (isSleepDatabaseOverlap(mSleepDatabase, true)) {
				mAlertExists.show();
			} else {
				mSleepDatabase.update();
				goBack();
			}
		} else {

				SleepDatabase sleepDatabase = new SleepDatabase(getActivity());
				sleepDatabase.setHourSleepStart(mSleepStartHour);
				sleepDatabase.setMinuteSleepStart(mSleepStartMin);
				sleepDatabase.setHourSleepEnd(mSleepEndHour);
				sleepDatabase.setMinuteSleepEnd(mSleepEndMin);
				sleepDatabase.setSleepDuration((int) sleepDuration);
				sleepDatabase.setDateStampDay(day);
				sleepDatabase.setDateStampMonth(month);
				sleepDatabase.setDateStampYear(year);
				sleepDatabase.setIsModified(false);
				sleepDatabase.setIsWatch(false);
				sleepDatabase.setWatch(getLifeTrakApplication().getSelectedWatch());
				sleepDatabase.setSyncedToCloud(false);
				if (isSleepDatabaseOverlap(sleepDatabase, false)) {
					mAlertExists.show();
				} else {
					sleepDatabase.insert();
					goBack();
				}

		}
	}

	private void sendDataToServer(int duration){
		mCurrentOperation = OPERATION_SYNC_SLEEP;

			if (NetworkUtil.getInstance(getActivity()).isNetworkAvailable()){

			String mAccessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);

			if (getLifeTrakApplication().getSelectedWatch() != null) {
				SleepAsyncTask sleepAsyncTask = new SleepAsyncTask();
				sleepAsyncTask.listener(SleepDataUpdate.this);
				sleepAsyncTask.addParam("access_token", mAccessToken);
				sleepAsyncTask.addParam("mac_address", getLifeTrakApplication().getSelectedWatch().getMacAddress());
				SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat sdfHour= new SimpleDateFormat("HH:mm:ss");
				if (mSleepDatabase != null) {
					Calendar dateCreated = Calendar.getInstance();
					dateCreated.set(Calendar.DAY_OF_MONTH, mSleepDatabase.getDateStampDay());
					dateCreated.set(Calendar.MONTH, mSleepDatabase.getDateStampMonth() - 1);
					dateCreated.set(Calendar.YEAR, mSleepDatabase.getDateStampYear() + 1900);
					dateCreated.set(Calendar.HOUR_OF_DAY, mSleepDatabase.getHourSleepStart());
					dateCreated.set(Calendar.MINUTE, mSleepDatabase.getMinuteSleepStart());
					dateCreated.set(Calendar.SECOND, 0);
					sleepAsyncTask.addParam("sleep_created_date", sdfYear.format(dateCreated.getTime()));
					sleepAsyncTask.addParam("sleep_start_time", sdfHour.format(dateCreated.getTime()));

					Calendar dateStartTime = Calendar.getInstance();
					dateStartTime.set(Calendar.HOUR_OF_DAY, mSleepStartHour);
					dateStartTime.set(Calendar.MINUTE, mSleepStartMin);
					dateStartTime.set(Calendar.DAY_OF_MONTH, mSleepDatabase.getDateStampDay());
					dateStartTime.set(Calendar.MONTH, mSleepDatabase.getDateStampMonth() - 1);
					dateStartTime.set(Calendar.YEAR, mSleepDatabase.getDateStampYear() + 1900);

					Calendar dateEndTime = Calendar.getInstance();
					dateEndTime.set(Calendar.HOUR_OF_DAY, mSleepEndHour);
					dateEndTime.set(Calendar.MINUTE, mSleepEndMin);
					dateEndTime.set(Calendar.DAY_OF_MONTH, mSleepDatabase.getDateStampDay());
					dateEndTime.set(Calendar.MONTH, mSleepDatabase.getDateStampMonth() - 1);
					dateEndTime.set(Calendar.YEAR, mSleepDatabase.getDateStampYear() + 1900);

					sleepAsyncTask.addParam("new_sleep_start_time", sdfHour.format(dateStartTime.getTime()));
					sleepAsyncTask.addParam("new_sleep_end_time", sdfHour.format(dateEndTime.getTime()));
					boolean isWatch = mSleepDatabase.isWatch();
					sleepAsyncTask.addParam("is_watch", String.valueOf((isWatch) ? 1 : 0));
					sleepAsyncTask.addParam("is_modified", String.valueOf(1));

				}
				else{
					Calendar dateCreated = Calendar.getInstance();
					dateCreated.setTime(getLifeTrakApplication().getCurrentDate());
					dateCreated.set(Calendar.HOUR_OF_DAY, mSleepStartHour);
					dateCreated.set(Calendar.MINUTE, mSleepStartMin);
					dateCreated.set(Calendar.SECOND, 0);
					sleepAsyncTask.addParam("sleep_created_date", sdfYear.format(dateCreated.getTime()));
					sleepAsyncTask.addParam("sleep_start_time", sdfHour.format(dateCreated.getTime()));

					Calendar dateStartTime = Calendar.getInstance();
					dateStartTime.set(Calendar.HOUR, mSleepStartHour);
					dateStartTime.set(Calendar.MINUTE, mSleepStartMin);
					dateStartTime.set(Calendar.SECOND, 0);
					Calendar dateEndTime = Calendar.getInstance();
					dateEndTime.set(Calendar.HOUR_OF_DAY, mSleepEndHour);
					dateEndTime.set(Calendar.MINUTE, mSleepEndMin);
					dateEndTime.set(Calendar.SECOND, 0);

					sleepAsyncTask.addParam("new_sleep_start_time", sdfHour.format(dateStartTime.getTime()));
					sleepAsyncTask.addParam("new_sleep_end_time", sdfHour.format(dateEndTime.getTime()));
					sleepAsyncTask.addParam("is_watch",String.valueOf(0));
					sleepAsyncTask.addParam("is_modified", String.valueOf(1));

				}
				sleepAsyncTask.addParam("sleep_duration",String.valueOf(duration));
				sleepAsyncTask.execute(API_URL + SLEEP_URI_UPDATE);
			}
			else{
				saveSleepDatabase(false);
			}
		}
	}

	private final TimePickerDialog.OnTimeSetListener mOnStartTimeListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker arg0, int arg1, int arg2) {
			mSleepStartHour = arg1;
			mSleepStartMin = arg2;

			if (getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_24_HR) {
				mSleepStartTime.setText(String.format("%d:%02d", mSleepStartHour, mSleepStartMin));
			} else {
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, mSleepStartHour);
				calendar.set(Calendar.MINUTE, mSleepStartMin);

				mSleepStartTime.setText(mDateFormat2.format(calendar.getTime()));
			}
		}
	};

	private final TimePickerDialog.OnTimeSetListener mOnEndTimeListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker arg0, int arg1, int arg2) {
			mSleepEndHour = arg1;
			mSleepEndMin = arg2;

			if (getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_24_HR) {
				mSleepEndTime.setText(String.format("%d:%02d", mSleepEndHour, mSleepEndMin));
			} else {
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, mSleepEndHour);
				calendar.set(Calendar.MINUTE, mSleepEndMin);

				mSleepEndTime.setText(mDateFormat2.format(calendar.getTime()));
			}
		}
	};

	private final View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			if (arg1) {
				mInputMethodManager.hideSoftInputFromWindow(arg0.getWindowToken(), 0);

				switch (arg0.getId()) {
				case R.id.edtStartTime:
					mStartSleepTimePicker.show();
					break;
				case R.id.edtEndTime:
					mEndSleepTimePicker.show();
					break;
				}
			}
		}
	};

	private final View.OnClickListener mTimeClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			mInputMethodManager.hideSoftInputFromWindow(arg0.getWindowToken(), 0);

			switch (arg0.getId()) {
			case R.id.edtStartTime:
				mStartSleepTimePicker.show();
				break;
			case R.id.edtEndTime:
				mEndSleepTimePicker.show();
				break;
			}
		}
	};

	public void deleteSleepLog() {
		mCurrentOperation = OPERATION_DELETE_SLEEP;
		String mAccessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);

		SleepAsyncTask sleepAsyncTask = new SleepAsyncTask();
		sleepAsyncTask.listener(SleepDataUpdate.this);
		sleepAsyncTask.addParam("access_token", mAccessToken);
		sleepAsyncTask.addParam("mac_address", getLifeTrakApplication().getSelectedWatch().getMacAddress());
		SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfHour= new SimpleDateFormat("HH:mm:ss");
		if (mSleepDatabase != null) {
			Calendar dateCreated = Calendar.getInstance();
			dateCreated.set(Calendar.DAY_OF_MONTH, mSleepDatabase.getDateStampDay());
			dateCreated.set(Calendar.MONTH, mSleepDatabase.getDateStampMonth() - 1);
			dateCreated.set(Calendar.YEAR, mSleepDatabase.getDateStampYear() + 1900);
			dateCreated.set(Calendar.HOUR_OF_DAY, mSleepDatabase.getHourSleepStart());
			dateCreated.set(Calendar.MINUTE, mSleepDatabase.getMinuteSleepStart());
			dateCreated.set(Calendar.SECOND, 0);
			sleepAsyncTask.addParam("sleep_created_date", sdfYear.format(dateCreated.getTime()));
			sleepAsyncTask.addParam("sleep_start_time", sdfHour.format(dateCreated.getTime()));
		}
		else{
			Calendar dateCreated = Calendar.getInstance();
			dateCreated.setTime(getLifeTrakApplication().getCurrentDate());
			dateCreated.set(Calendar.HOUR_OF_DAY, mSleepStartHour);
			dateCreated.set(Calendar.MINUTE, mSleepStartMin);
			dateCreated.set(Calendar.SECOND, 0);
			sleepAsyncTask.addParam("sleep_created_date", sdfYear.format(dateCreated.getTime()));
			sleepAsyncTask.addParam("sleep_start_time", sdfHour.format(dateCreated.getTime()));
		}
		sleepAsyncTask.execute(API_URL + SLEEP_URI_DELETE);
	}

	private void deleteSleepLogDatabase(){
		if (mSleepDatabase != null) {
			SleepDatabaseDeleted sleepDatabaseDeleted = SleepDatabaseDeleted.buildSleepDatabase(getActivity(), mSleepDatabase);
			sleepDatabaseDeleted.setWatch(getLifeTrakApplication().getSelectedWatch());
			sleepDatabaseDeleted.insert();
			mSleepDatabase.setContext(getActivity());
			mSleepDatabase.delete();
			goBack();
		}
	}

	private void goBack() {
		getActivity().onBackPressed();
	}

	private boolean isSleepDatabaseOverlap(SleepDatabase sleepArg, boolean isUpdate) {
		for (SleepDatabase sleepDatabase : mSleepDatabases) {
			if (sleepArg.getDateStampDay() == sleepDatabase.getDateStampDay() && sleepArg.getDateStampMonth() == sleepDatabase.getDateStampMonth() && sleepArg.getDateStampYear() == sleepDatabase.getDateStampYear()){
				int sleepSaveStartHourTime  = sleepDatabase.getHourSleepStart();
				int sleepSaveEndHourTime  = sleepDatabase.getHourSleepEnd();
				int sleepSaveStartMinTime  = sleepDatabase.getMinuteSleepStart();
				int sleepSaveEndMinTime  = sleepDatabase.getMinuteSleepEnd();
				int sleepArgStartHourTime = sleepArg.getHourSleepStart();
				int sleepArgEndHourTime = sleepArg.getHourSleepEnd();
				int sleepArgStartMinTime  = sleepArg.getMinuteSleepStart();
				int sleepArgEndMinTime  = sleepArg.getMinuteSleepEnd();

				int sleepTimeStart = (sleepSaveStartHourTime* 60) + sleepSaveStartMinTime;
				int sleepTimeEnd = (sleepSaveEndHourTime* 60) + sleepSaveEndMinTime;

				int argTimeStart = (sleepArgStartHourTime* 60) + sleepArgStartMinTime;
				int argTimeEnd = (sleepArgEndHourTime* 60) + sleepArgEndMinTime;

				if ((argTimeStart >= sleepTimeStart && argTimeStart <= sleepTimeEnd) ||
						(argTimeEnd >= sleepTimeStart && argTimeEnd <= sleepTimeEnd))
				{
					if (!(argTimeStart == sleepTimeStart && argTimeEnd == sleepTimeEnd && isUpdate)){
						return true;
					}
				}


			}
		}
		return false;
	}



	@Override
	public void onAsyncStart() {
		mProgressDialog.show();
	}

	@Override
	public void onAsyncFail(int status, String message) {
		mProgressDialog.dismiss();
		LifeTrakLogger.info("ERROR: " + message);
		if (getActivity().isFinishing()) {
			AlertDialog mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(getString(R.string.string_steps_network_error)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			}).create();
			mAlertDialog.show();
		} else {
			Intent intent = new Intent(getActivity(), DialogActivityNetworkError.class);
			startActivity(intent);
		}
	}

	@Override
	public void onAsyncSuccess(JSONObject result) {
		mProgressDialog.dismiss();
		switch(mCurrentOperation){
			case OPERATION_SYNC_SLEEP:
				saveSleepDatabase(true);
				break;
			case OPERATION_DELETE_SLEEP:
				deleteSleepLogDatabase();
				break;
		}
	}
}
