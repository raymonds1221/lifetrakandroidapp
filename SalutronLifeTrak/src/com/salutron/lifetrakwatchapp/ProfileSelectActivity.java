package com.salutron.lifetrakwatchapp;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;

import com.flurry.android.FlurryAgent;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.CalibrationData;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.blesdk.SALUserProfile;

import roboguice.inject.InjectView;

public class ProfileSelectActivity extends BaseActivity {
	@InjectView(R.id.chkDoNotShowAgain)	private CheckBox mDoNotShowDialog;
	private UserProfile mUserProfile;
	private SALUserProfile mSALUserProfile;
	private TimeDate mTimeDate;
	private CalibrationData mCalibrationData;
	private Goal mGoal;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_select);
		
		Bundle bundle = getIntent().getExtras();
		
		if(bundle != null) {
			mUserProfile = getLifeTrakApplication().getUserProfile();
			mTimeDate = bundle.getParcelable(TIME_DATE);
			UserProfile salProfile = bundle.getParcelable(SAL_USER_PROFILE);
			mCalibrationData = bundle.getParcelable(CALIBRATION_DATA_FROM_WATCH);
			mGoal = bundle.getParcelable(GOAL_FROM_WATCH);
			
			if (mCalibrationData != null)
				mCalibrationData.setWatch(getLifeTrakApplication().getSelectedWatch());
			
			mGoal.setWatch(getLifeTrakApplication().getSelectedWatch());
			
			mSALUserProfile = new SALUserProfile();
			mSALUserProfile.setWeight(salProfile.getWeight());
			mSALUserProfile.setHeight(salProfile.getHeight());
			mSALUserProfile.setBirthDay(salProfile.getBirthDay());
			mSALUserProfile.setBirthMonth(salProfile.getBirthMonth());
			mSALUserProfile.setBirthYear(salProfile.getBirthYear());
			mSALUserProfile.setSensitivityLevel(salProfile.getSensitivity());
			mSALUserProfile.setGender(salProfile.getGender());
			mSALUserProfile.setUnitSystem(salProfile.getUnitSystem());
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		return;
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
	
	public void onButtonSelectClick(View view) {
		int useSetting = USE_APP;
		
		if(mUserProfile != null) {
			Intent intent = new Intent();
			
			switch(view.getId()) {
			case R.id.btnUseWatch:
				mUserProfile.setWeight(mSALUserProfile.getWeight());
				mUserProfile.setHeight(mSALUserProfile.getHeight());
				mUserProfile.setBirthDay(mSALUserProfile.getBirthDay());
				mUserProfile.setBirthMonth(mSALUserProfile.getBirthMonth());
				mUserProfile.setBirthYear(mSALUserProfile.getBirthYear());
				mUserProfile.setSensitivity(mSALUserProfile.getSensitivityLevel());
				mUserProfile.setGender(mSALUserProfile.getGender());
				mUserProfile.setUnitSystem(mSALUserProfile.getUnitSystem());
				mUserProfile.update();
				
				getLifeTrakApplication().setUserProfile(mUserProfile);
				getLifeTrakApplication().setTimeDate(mTimeDate);
				
				intent.putExtra(USER_PROFILE, mUserProfile);
				intent.putExtra(TIME_DATE, getLifeTrakApplication().getTimeDate());
				intent.putExtra(CALIBRATION_DATA_FROM_WATCH, mCalibrationData);
				intent.putExtra(GOAL_FROM_WATCH, mGoal);
				
				setResult(RESULT_OK, intent);
				
				useSetting = USE_WATCH;
				break;
			case R.id.btnUseApp:
				intent.putExtra(USER_PROFILE, getLifeTrakApplication().getUserProfile());
				intent.putExtra(TIME_DATE, getLifeTrakApplication().getTimeDate());
				
				List<CalibrationData> calibrationData = DataSource.getInstance(this)
																	.getReadOperation().query("watchCalibrationData = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
																	.getResults(CalibrationData.class);
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());

				int day = calendar.get(Calendar.DAY_OF_MONTH);
				int month = calendar.get(Calendar.MONTH) + 1;
				int year = calendar.get(Calendar.YEAR) - 1900;
				
				List<Goal> goals = DataSource.getInstance(this)
												.getReadOperation()
												.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(day),
														String.valueOf(month), String.valueOf(year)).getResults(Goal.class);
				
				if (goals.size() == 0) {
					goals = DataSource.getInstance(this)
										.getReadOperation()
										.query("watchGoal = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
										.orderBy("abs(date - " + calendar.getTimeInMillis() + ")", SORT_ASC)
										.limit(1)
										.getResults(Goal.class);
				}
				
				if (calibrationData.size() > 0)
					mCalibrationData = calibrationData.get(0);
				
				if (goals.size() > 0) {
					mGoal = goals.get(0);
					intent.putExtra(GOAL_FROM_WATCH, goals.get(0));
				} else {
					Goal goal2 = goals.get(0);
					Goal goal = new Goal();
					goal.setStepGoal(goal2.getStepGoal());
					goal.setDistanceGoal(goal2.getDistanceGoal());
					goal.setCalorieGoal(goal2.getCalorieGoal());
					goal.setDateStampDay(day);
					goal.setDateStampMonth(month);
					goal.setDateStampYear(year);
					goal.setDate(calendar.getTime());
					intent.putExtra(GOAL_FROM_WATCH, goal);
				}
				
				intent.putExtra(CALIBRATION_DATA_FROM_WATCH, mCalibrationData);
				
				setResult(RESULT_OK, intent);
				
				useSetting = USE_APP;
				break;
			}
		}
		
		mPreferenceWrapper.setPreferenceBooleanValue(DO_NOT_SHOW_PROMPT_DIALOG, mDoNotShowDialog.isChecked())
							.setPreferenceIntValue(USE_SETTING, useSetting)
							.synchronize();
		
		finish();
	}
}
