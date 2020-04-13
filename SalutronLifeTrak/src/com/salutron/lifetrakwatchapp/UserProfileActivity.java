package com.salutron.lifetrakwatchapp;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.view.HeightPickerView;
import com.salutron.lifetrakwatchapp.util.SalutronSDKCallback;

public class UserProfileActivity extends BaseActivity implements SalutronSDKCallback {
	private EditText mWeightValue;
	private TextView mWeightUnit;
	private EditText mHeightValue;
	private TextView mHeightUnit;
	private TextView mBirthdayValue;
	private RadioGroup mGender;
	private RadioButton mGenderMale;
	private RadioButton mGenderFemale;
	private UserProfile mUserProfile;
	private HeightPickerView mHeightPickerView;
	private DatePickerDialog mDatePickerDialog;

	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private final InputFilter[] mInputFilter = new InputFilter[1];
	private AlertDialog mAlertDialog;
	private Calendar baseLineCalendar;
	private long maxSelectedMilis;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);

		initializeObjects();
		mAlertDialog = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}
		}).create();
		
		baseLineCalendar = Calendar.getInstance();
		maxSelectedMilis = baseLineCalendar.getTimeInMillis();
	}
	
	@Override
	public void onStart()
	{
	   super.onStart();
	   FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
	   
	}
	@Override
	public void onStop()
	{
	   super.onStop();
	   FlurryAgent.onEndSession(this);
	 
	}

	@Override
	public void onResume() {
		super.onResume();
		bindBLEService();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindBLEService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_user_profile, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_save) {
			onClickSave();
		}
		return super.onOptionsItemSelected(item);
	}

	private void onClickSave() {
		
//		mUserProfile.setWeight(weight);
//		mUserProfile.setHeight(height);
//		mUserProfile.setBirthDay(birthDay);
//		mUserProfile.setBirthMonth(birthMonth);
//		mUserProfile.setBirthYear(birthYear);
//		mUserProfile.setGender(gender);
//		mUserProfile.update();
		setResult(RESULT_OK);
		finish();
	}

	private void initializeObjects() {
		setSalutronSDKCallback(this);

		mWeightValue = (EditText) findViewById(R.id.tvwWeightValue);
		mWeightUnit = (TextView) findViewById(R.id.tvwWeightUnit);
		mHeightValue = (EditText) findViewById(R.id.tvwHeightValue);
		mHeightUnit = (TextView) findViewById(R.id.tvwHeightUnit);
		mBirthdayValue = (TextView) findViewById(R.id.tvwBirthdayValue);
		mGender = (RadioGroup) findViewById(R.id.rdgGender);
		mGenderMale = (RadioButton) findViewById(R.id.radMale);
		mGenderFemale = (RadioButton) findViewById(R.id.radFemale);

		mWeightUnit.setOnClickListener(onUnitClickListener);
		mHeightUnit.setOnClickListener(onUnitClickListener);

		mUserProfile = getLifeTrakApplication().getUserProfile();

		mDateFormat.applyPattern("MMM dd,yyyy");

		double weightValue = (double) mUserProfile.getWeight();
		double heightValue = (double) mUserProfile.getHeight();

		switch (mUserProfile.getUnitSystem()) {
		case UNIT_IMPERIAL:
			double feetValue = Math.floor(heightValue / FEET_CM);
			double inchValue = (heightValue / INCH_CM) - (feetValue * 12);

			if (Math.round(inchValue) == 12) {
				feetValue++;
				inchValue = 0;
			}

			mWeightValue.setHint(String.format("%d", (int) Math.round(weightValue) > 400 ? 400 : (int) Math.round(weightValue)));
			mWeightValue.setHint(String.format("%d", (int) Math.round(weightValue) < 44 ? 44 : (int) Math.round(weightValue)));
			/*mWeightValue.setHint(String.format("%d", (int) weightValue));*/
			mWeightUnit.setText("lbs");
			mHeightValue.setText(String.format("%d' %02d\"", (int) feetValue, (int) inchValue));
			mHeightUnit.setText("");
			mHeightValue.setFocusable(false);
			break;
		case UNIT_METRIC:
			mWeightValue.setHint(String.format("%d", Math.round(weightValue * KG) > 200 ? 200 : Math.round(weightValue * KG)));
			mWeightValue.setHint(String.format("%d", Math.round(weightValue * KG) < 20 ? 20 : Math.round(weightValue * KG)));
			/*mWeightValue.setHint(String.format("%d", (int) (weightValue * KG)));*/
			mWeightUnit.setText("kg");
			mHeightValue.setHint(String.valueOf((int) heightValue));
			mHeightUnit.setText("cm");
			mHeightValue.setFocusable(true);

			mInputFilter[0] = new InputFilter.LengthFilter(3);
			mHeightValue.setFilters(mInputFilter);

			break;
		}

		//mWeightValue.addTextChangedListener(mTextWatcherWeight);
		mWeightValue.setOnFocusChangeListener(mTextFocusChangeListener);
		//mHeightValue.addTextChangedListener(mTextWatcherHeight);
		mHeightValue.setOnFocusChangeListener(mTextFocusChangeListener);

		mHeightValue.setHintTextColor(getResources().getColor(R.color.abs__primary_text_holo_light));
		mWeightValue.setHintTextColor(getResources().getColor(R.color.abs__primary_text_holo_light));

		mHeightValue.setOnClickListener(mClickListener);
		mBirthdayValue.setOnClickListener(mClickListener);

		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.DAY_OF_MONTH, mUserProfile.getBirthDay());
		calendar.set(Calendar.MONTH, mUserProfile.getBirthMonth() - 1);
		calendar.set(Calendar.YEAR, mUserProfile.getBirthYear());

		mBirthdayValue.setText(mDateFormat.format(calendar.getTime()));
		mGenderMale.setChecked(mUserProfile.getGender() == GENDER_MALE);
		mGenderFemale.setChecked(mUserProfile.getGender() == GENDER_FEMALE);

		mHeightPickerView = new HeightPickerView(this);
		mHeightPickerView.setOnSelectHeightListener(mSelectHeightListener);
		mHeightPickerView.setValue(mUserProfile.getHeight());

		mDatePickerDialog = new DatePickerDialog(this, mDateSetListener, mUserProfile.getBirthYear(), mUserProfile.getBirthMonth() - 1, mUserProfile.getBirthDay());

		mGender.setOnCheckedChangeListener(mCheckChangedListener);
	}

	private final OnClickListener onUnitClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == mHeightUnit) {
				mHeightValue.requestFocus();
			} else if (v == mWeightUnit) {
				mWeightValue.requestFocus();
			}
		}
	};

	private final TextWatcher mTextWatcherWeight = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			int value = 0;

			if (!s.toString().equals("")) {
				value = Integer.parseInt(s.toString());
			} else {
				value = Integer.parseInt(mWeightValue.getHint().toString());
			}

			if (mUserProfile.getUnitSystem() == UNIT_METRIC) {
				if (value > 200) {
					value = 200;
					setValue(s, value);
				}
				value = (int) (value / KG);
			} else {
				if (value > 440) {
					value = 440;
					setValue(s, value);
				} else if (value <= 0){
					value = 44;
					setValue(s, value);
					mAlertDialog.setMessage(getString(R.string.invalid_weight, getString(R.string.old_password)));
					mAlertDialog.show();
				}
			}
			mUserProfile.setWeight(value);
			
			mUserProfile.update();
		}

		private void setValue(Editable s, int value) {
			mWeightValue.removeTextChangedListener(mTextWatcherWeight);
			s.replace(0, s.length(), String.valueOf(value));
			mWeightValue.addTextChangedListener(mTextWatcherWeight);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	};

	private final TextWatcher mTextWatcherHeight = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			if (mUserProfile.getUnitSystem() == UNIT_METRIC) {
				int value = 0;

				if (!s.toString().equals("")) {
					value = Integer.parseInt(s.toString());
				} else {
					value = Integer.parseInt(mHeightValue.getHint().toString());
				}

				if (mUserProfile.getUnitSystem() == UNIT_METRIC) {
					if (value > 220) {
						value = 220;
						setValue(s, value);
					}
				}
				mUserProfile.setHeight(value);
				mUserProfile.update();
			}
		}

		private void setValue(Editable s, int value) {
			mHeightValue.removeTextChangedListener(mTextWatcherHeight);
			s.replace(0, s.length(), String.valueOf(value));
			mHeightValue.addTextChangedListener(mTextWatcherHeight);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	};

	private final View.OnFocusChangeListener mTextFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			if (!arg1) {
				if (!arg1) {
					switch (arg0.getId()) {
					case R.id.tvwWeightValue:

						int weightValue = 0;
						if (!mWeightValue.getText().toString().isEmpty()) {
							weightValue = Integer.parseInt(mWeightValue.getText().toString());
						} else {
							weightValue = Integer.parseInt(mWeightValue.getHint().toString());
						}

						if (mUserProfile.getUnitSystem() == UNIT_METRIC) {
							if (weightValue > 200) {
								weightValue = 200;
							} else if (weightValue < 20) {
								weightValue = 20;
							}
							mWeightValue.removeTextChangedListener(mTextWatcherWeight);
							mWeightValue.setText(String.valueOf(weightValue));
							mWeightValue.addTextChangedListener(mTextWatcherWeight);
							mWeightValue.setHint(String.valueOf(weightValue));
							weightValue = (int) (weightValue / KG);
						} else {
							if (weightValue < 44 || weightValue <= 0) {
								weightValue = 44;
							} else if (weightValue > 440) {
								weightValue = 440;
							}
							mWeightValue.removeTextChangedListener(mTextWatcherWeight);
							mWeightValue.setText(String.valueOf(weightValue));
							mWeightValue.addTextChangedListener(mTextWatcherWeight);
							mWeightValue.setHint(String.valueOf(weightValue));
						}
						mUserProfile.setWeight(weightValue);
						mUserProfile.update();
						break;
					case R.id.tvwHeightValue:

						int heightValue = 0;
						if (!mHeightValue.getText().toString().isEmpty()) {
							heightValue = Integer.parseInt(mHeightValue.getText().toString());
						} else {
							heightValue = Integer.parseInt(mHeightValue.getHint().toString());
						}

						if (mUserProfile.getUnitSystem() == UNIT_METRIC) {
							if (heightValue < 102) {
								heightValue = 102;
							}
						}
						mHeightValue.setHint(String.valueOf(heightValue));
						mHeightValue.removeTextChangedListener(mTextWatcherHeight);
						mHeightValue.setText(String.valueOf(heightValue));
						mHeightValue.addTextChangedListener(mTextWatcherHeight);
						mUserProfile.setHeight(heightValue);
						mUserProfile.update();

						break;
					}
				}

			}
			else{
				switch (arg0.getId()) {
					case R.id.tvwWeightValue:
						mWeightValue.setText("");
						break;
					case R.id.tvwHeightValue:
						mHeightValue.setText("");
				}
			}
		}
	};

	private final View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tvwHeightValue:
				mWeightValue.clearFocus();
				mHeightValue.clearFocus();
				if (mUserProfile.getUnitSystem() == UNIT_IMPERIAL) {
					mHeightPickerView.setValue(mUserProfile.getHeight());
					mHeightPickerView.show();
				}
				break;
			case R.id.tvwBirthdayValue:
				mWeightValue.clearFocus();
				mHeightValue.clearFocus();
				mDatePickerDialog.show();
				break;
			}
		}
	};

	private final HeightPickerView.OnSelectHeightListener mSelectHeightListener = new HeightPickerView.OnSelectHeightListener() {
		@Override
		public void onSelectHeight(int valueInCm) {
			mUserProfile.setHeight(valueInCm);
			double feetValue = Math.floor(valueInCm / FEET_CM);
			double inchValue = (valueInCm / INCH_CM) - (feetValue * 12);

			if (Math.round(inchValue) == 12) {
				feetValue++;
				inchValue = 0;
			}

			mHeightValue.setText(String.format("%d' %02d\"", (int) feetValue, Math.round(inchValue)));
		}
	};

	private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Calendar calendar = new GregorianCalendar();
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, monthOfYear);
			calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			
			long selectedMilis = calendar.getTimeInMillis();
			Date selecctedDate = new Date(selectedMilis);
			
			// Clear focus on other edit texts when date picker is displayed
			mWeightValue.clearFocus();
			mHeightValue.clearFocus();
			
			//if calendar date is future date
			if (selecctedDate.after(new Date(maxSelectedMilis))){
				mAlertDialog.setMessage(getString(R.string.max_date, getString(R.string.old_password)));
				mAlertDialog.show();
			} else { 
				mUserProfile.setBirthYear(year);
				mUserProfile.setBirthMonth(monthOfYear + 1);
				mUserProfile.setBirthDay(dayOfMonth);
				mUserProfile.update();
				mBirthdayValue.setText(mDateFormat.format(calendar.getTime()));
			}	
			
		}
	};

	private final RadioGroup.OnCheckedChangeListener mCheckChangedListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.radMale:
				mUserProfile.setGender(GENDER_MALE);
				break;
			case R.id.radFemale:
				mUserProfile.setGender(GENDER_FEMALE);
				break;
			}
			mUserProfile.update();
		}
	};

	@Override
	public void onDeviceConnected(BluetoothDevice device) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDeviceReady() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDeviceDisconnected() {
		Log.i(TAG, "device disconnected");
	}

	@Override
	public void onSyncTime() {
	}

	@Override
	public void onSyncStatisticalDataHeaders() {
	}

	@Override
	public void onSyncStatisticalDataPoint(int percent) {
	}

	@Override
	public void onSyncStepGoal() {
	}

	@Override
	public void onSyncDistanceGoal() {
	}

	@Override
	public void onSyncCalorieGoal() {
	}

	@Override
	public void onSyncSleepSetting() {
	}

	@Override
	public void onSyncCalibrationData() {
	}

	@Override
	public void onSyncWorkoutDatabase() {
	}

	@Override
	public void onSyncSleepDatabase() {
	}

	@Override
	public void onSyncUserProfile() {
	}

	@Override
	public void onStartSync() {
	}

	@Override
	public void onSyncFinish() {
	}

}
