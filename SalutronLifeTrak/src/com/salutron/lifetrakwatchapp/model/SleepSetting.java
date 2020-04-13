package com.salutron.lifetrakwatchapp.model;

import android.os.Parcel;
import android.content.Context;

import com.salutron.blesdk.SALSleepSetting;
import com.salutron.lifetrakwatchapp.annotation.DataTable;
import com.salutron.lifetrakwatchapp.annotation.DataColumn;

@DataTable(name="SleepSetting")
public class SleepSetting extends BaseModel {
	@DataColumn(name="sleepGoalMinutes")
	private int sleepGoalMinutes;
	@DataColumn(name="sleepDetectType")
	private int sleepDetectType;
	@DataColumn(name="watchSleepSetting", isPrimary=true)
	private Watch watch;
	
	public SleepSetting() { }
	
	public SleepSetting(Context context) {
		super(context);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

	@Override
	public void readFromParcel(Parcel source) {
	}

	public int getSleepGoalMinutes() {
		return sleepGoalMinutes;
	}

	public void setSleepGoalMinutes(int sleepGoalMinutes) {
		this.sleepGoalMinutes = sleepGoalMinutes;
	}

	public int getSleepDetectType() {
		return sleepDetectType;
	}

	public void setSleepDetectType(int sleepDetectType) {
		this.sleepDetectType = sleepDetectType;
	}

	public Watch getWatch() {
		return watch;
	}

	public void setWatch(Watch watch) {
		this.watch = watch;
	}
	
	public static final SleepSetting buildSleepSetting(Context context, SALSleepSetting salSleepSetting) {
		SleepSetting sleepSetting = new SleepSetting(context);
		
		sleepSetting.setSleepDetectType(salSleepSetting.getSleepDetectType());
		sleepSetting.setSleepGoalMinutes(salSleepSetting.getSleepGoal());
		
		return sleepSetting;
	}
}
