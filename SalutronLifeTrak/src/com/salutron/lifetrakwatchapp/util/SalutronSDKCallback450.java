package com.salutron.lifetrakwatchapp.util;

import android.os.Bundle;
import android.bluetooth.BluetoothDevice;

public interface SalutronSDKCallback450 extends SalutronSDKCallback {
	public void onDeviceFound(BluetoothDevice bluetoothDevice, Bundle data);
	public void onSyncLightDataPoints(int percent);
	public void onSyncWorkoutStopDatabase(int percent);
	public void onSyncWakeupSetting();
	public void onSyncNotifications();
	public void onSyncActivityAlertSettingsData();
	public void onSyncDayLightSettingsData();
	public void onSyncNightLightSettingsData();
    public void onError(int status);
}
