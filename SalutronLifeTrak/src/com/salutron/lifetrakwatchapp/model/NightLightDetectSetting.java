package com.salutron.lifetrakwatchapp.model;

import android.os.Parcel;
import android.content.Context;

import com.salutron.lifetrakwatchapp.annotation.*;

@DataTable(name="NightLightDetectSetting")
public class NightLightDetectSetting extends BaseModel {
	@DataColumn(name="enabled")
	private boolean enabled;
	@DataColumn(name="exposureLevel")
	private int exposureLevel;
	@DataColumn(name="exposureDuration")
	private int exposureDuration;
	@DataColumn(name="startTime")
	private int startTime;
	@DataColumn(name="endTime")
	private int endTime;
	@DataColumn(name="detectLowThreshold")
	private int detectLowThreshold;
	@DataColumn(name="detectMediumThreshold")
	private int detectMediumThreshold;
	@DataColumn(name="detectHighThreshold")
	private int detectHighThreshold;
	@DataColumn(name="watchNightlightSetting", isPrimary=true)
	private Watch watch;
	
	public NightLightDetectSetting() { }
	
	public NightLightDetectSetting(Context context) {
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

	public int getExposureLevel() {
		return exposureLevel;
	}

	public void setExposureLevel(int exposureLevel) {
		this.exposureLevel = exposureLevel;
	}

	public int getExposureDuration() {
		return exposureDuration;
	}

	public void setExposureDuration(int exposureDuration) {
		this.exposureDuration = exposureDuration;
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

	public int getDetectLowThreshold() {
		return detectLowThreshold;
	}

	public void setDetectLowThreshold(int detectLowThreshold) {
		this.detectLowThreshold = detectLowThreshold;
	}

	public int getDetectMediumThreshold() {
		return detectMediumThreshold;
	}

	public void setDetectMediumThreshold(int detectMediumThreshold) {
		this.detectMediumThreshold = detectMediumThreshold;
	}

	public int getDetectHighThreshold() {
		return detectHighThreshold;
	}

	public void setDetectHighThreshold(int detectHighThreshold) {
		this.detectHighThreshold = detectHighThreshold;
	}

	public Watch getWatch() {
		return watch;
	}

	public void setWatch(Watch watch) {
		this.watch = watch;
	}
}
