package com.salutron.lifetrakwatchapp.util;

import android.bluetooth.BluetoothDevice;

public interface SalutronSDKCallback {
	public void onDeviceConnected(BluetoothDevice device);
	public void onDeviceReady();
	public void onDeviceDisconnected();
	public void onSyncTime();
	public void onSyncStatisticalDataHeaders();
	public void onSyncStatisticalDataPoint(int percent);
	public void onSyncStepGoal();
	public void onSyncDistanceGoal();
	public void onSyncCalorieGoal();
	public void onSyncSleepSetting();
	public void onSyncCalibrationData();
	public void onSyncWorkoutDatabase();
	public void onSyncSleepDatabase();
	public void onSyncUserProfile();
	public void onStartSync();
	public void onSyncFinish();
}
