package com.salutron.lifetrakwatchapp.model;

import android.os.Parcel;
import android.content.Context;

import com.salutron.lifetrakwatchapp.annotation.*;

@DataTable(name="Notification")
public class Notification extends BaseModel {
	@DataColumn(name="simpleAlertEnabled")
	private boolean simpleAlertEnabled;
	@DataColumn(name="emailEnabled")
	private boolean emailEnabled;
	@DataColumn(name="newsEnabled")
	private boolean newsEnabled;
	@DataColumn(name="incomingCallEnabled")
	private boolean incomingCallEnabled;
	@DataColumn(name="missedCallEnabled")
	private boolean missedCallEnabled;
	@DataColumn(name="smsEnabled")
	private boolean smsEnabled;
	@DataColumn(name="voiceMailEnabled")
	private boolean voiceMailEnabled;
	@DataColumn(name="scheduleEnabled")
	private boolean scheduleEnabled;
	@DataColumn(name="highPriorityEnabled")
	private boolean highPriorityEnabled;
	@DataColumn(name="instantMessageEnabled")
	private boolean instantMessageEnabled;
	@DataColumn(name="watchNotification", isPrimary=true)
	private Watch watch;
	
	public Notification() { }
	
	public Notification(Context context) {
		super(context);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

	@Override
	public void readFromParcel(Parcel source) {
	}

	public boolean isSimpleAlertEnabled() {
		return simpleAlertEnabled;
	}

	public void setSimpleAlertEnabled(boolean simpleAlertEnabled) {
		this.simpleAlertEnabled = simpleAlertEnabled;
	}

	public boolean isEmailEnabled() {
		return emailEnabled;
	}

	public void setEmailEnabled(boolean emailEnabled) {
		this.emailEnabled = emailEnabled;
	}

	public boolean isNewsEnabled() {
		return newsEnabled;
	}

	public void setNewsEnabled(boolean newsEnabled) {
		this.newsEnabled = newsEnabled;
	}

	public boolean isIncomingCallEnabled() {
		return incomingCallEnabled;
	}

	public void setIncomingCallEnabled(boolean incomingCallEnabled) {
		this.incomingCallEnabled = incomingCallEnabled;
	}

	public boolean isMissedCallEnabled() {
		return missedCallEnabled;
	}

	public void setMissedCallEnabled(boolean missedCallEnabled) {
		this.missedCallEnabled = missedCallEnabled;
	}

	public boolean isSmsEnabled() {
		return smsEnabled;
	}

	public void setSmsEnabled(boolean smsEnabled) {
		this.smsEnabled = smsEnabled;
	}

	public boolean isVoiceMailEnabled() {
		return voiceMailEnabled;
	}

	public void setVoiceMailEnabled(boolean voiceMailEnabled) {
		this.voiceMailEnabled = voiceMailEnabled;
	}

	public boolean isScheduleEnabled() {
		return scheduleEnabled;
	}

	public void setScheduleEnabled(boolean scheduleEnabled) {
		this.scheduleEnabled = scheduleEnabled;
	}

	public boolean isHighPriorityEnabled() {
		return highPriorityEnabled;
	}

	public void setHighPriorityEnabled(boolean highPriorityEnabled) {
		this.highPriorityEnabled = highPriorityEnabled;
	}

	public boolean isInstantMessageEnabled() {
		return instantMessageEnabled;
	}

	public void setInstantMessageEnabled(boolean instantMessageEnabled) {
		this.instantMessageEnabled = instantMessageEnabled;
	}

	public Watch getWatch() {
		return watch;
	}

	public void setWatch(Watch watch) {
		this.watch = watch;
	}
}
