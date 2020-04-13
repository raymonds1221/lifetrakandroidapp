package com.salutron.lifetrakwatchapp.model;

import android.content.Context;
import android.os.Parcel;

import com.salutron.lifetrakwatchapp.annotation.*;

@DataTable(name="WakeupSetting")
public class WakeupSetting extends BaseModel {
	@DataColumn(name="enabled")
	private boolean enabled;
	@DataColumn(name="time")
	private int time;
	@DataColumn(name="wakeupTimeHour")
	private int wakeupTimeHour;
	@DataColumn(name="wakeupTimeMinute")
	private int wakeupTimeMinute;
	@DataColumn(name="window")
	private int window;
	@DataColumn(name="snoozeEnabled")
	private boolean snoozeEnabled;
	@DataColumn(name="snoozeTime")
	private int snoozeTime;
	@DataColumn(name="watchWakeupSetting", isPrimary=true)
	private Watch watch;
	
	public WakeupSetting() { }
	
	public WakeupSetting(Context context) {
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

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getWakeupTimeHour() {
		return wakeupTimeHour;
	}

	public void setWakeupTimeHour(int wakeupTimeHour) {
		this.wakeupTimeHour = wakeupTimeHour;
	}

	public int getWakeupTimeMinute() {
		return wakeupTimeMinute;
	}

	public void setWakeupTimeMinute(int wakeupTimeMinute) {
		this.wakeupTimeMinute = wakeupTimeMinute;
	}

	public int getWindow() {
		return window;
	}

	public void setWindow(int window) {
		this.window = window;
	}

	public boolean isSnoozeEnabled() {
		return snoozeEnabled;
	}

	public void setSnoozeEnabled(boolean snoozeEnabled) {
		this.snoozeEnabled = snoozeEnabled;
	}

	public int getSnoozeTime() {
		return snoozeTime;
	}

	public void setSnoozeTime(int snoozeTime) {
		this.snoozeTime = snoozeTime;
	}

	public Watch getWatch() {
		return watch;
	}

	public void setWatch(Watch watch) {
		this.watch = watch;
	}
}
