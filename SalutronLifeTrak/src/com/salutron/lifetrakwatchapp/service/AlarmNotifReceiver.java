package com.salutron.lifetrakwatchapp.service;

import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.salutron.lifetrakwatchapp.util.SalutronSDKCallback450;

public class AlarmNotifReceiver extends BroadcastReceiver implements SalutronSDKCallback450 {

    private final String TAG = AlarmNotifReceiver.class.getSimpleName();
    public static final String EXTRAS_NOTIF_MESSAGE = "meh";
    public String message;
    public NotificationManager notificationManager;
    
	@Override
	public void onReceive(Context context, Intent intent) {
		 Intent service1 = new Intent(context, AlarmService.class);
	     context.startService(service1);
	}

	@Override
	public void onDeviceConnected(BluetoothDevice device) {
			
	}

	@Override
	public void onDeviceReady() {
		
	}

	@Override
	public void onDeviceDisconnected() {
		
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

	@Override
	public void onDeviceFound(BluetoothDevice bluetoothDevice, Bundle data) {
	}

	@Override
	public void onSyncLightDataPoints(int percent) {
	}

	@Override
	public void onSyncWorkoutStopDatabase(int percent) {
	}

	@Override
	public void onSyncWakeupSetting() {
	}

	@Override
	public void onSyncNotifications() {
	}

	@Override
	public void onSyncActivityAlertSettingsData() {
	}

	@Override
	public void onSyncDayLightSettingsData() {
	}

	@Override
	public void onSyncNightLightSettingsData() {
	}

    public void onError(int status) { }
}

