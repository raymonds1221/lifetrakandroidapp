package com.salutron.lifetrakwatchapp.model;

import android.os.Parcel;
import android.content.Context;

import com.salutron.lifetrakwatchapp.annotation.*;

@DataTable(name="ActivityAlertSetting")
public class ActivityAlertSetting extends BaseModel {
	@DataColumn(name="enabled")
	private boolean enabled;
	@DataColumn(name="timeInterval")
	private int timeInterval;
	@DataColumn(name="stepsThreshold")
	private int stepsThreshold;
	@DataColumn(name="startTime")
	private int startTime;
	@DataColumn(name="endTime")
	private int endTime;
	@DataColumn(name="watchActivityAlert", isPrimary=true)
	public Watch watch;
	
	public ActivityAlertSetting() { }
	
	public ActivityAlertSetting(Context context) {
		super(context);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

	@Override
	public void readFromParcel(Parcel source) {
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getTimeInterval() {
		return timeInterval;
	}

	public void setTimeInterval(int timeInterval) {
		this.timeInterval = timeInterval;
	}

	public int getStepsThreshold() {
		return stepsThreshold;
	}

	public void setStepsThreshold(int stepsThreshold) {
		this.stepsThreshold = stepsThreshold;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public Watch getWatch() {
		return watch;
	}

	public void setWatch(Watch watch) {
		this.watch = watch;
	}
}
