package com.salutron.lifetrakwatchapp.util;

import android.bluetooth.BluetoothDevice;

import com.salutron.blesdk.SALBLEService;
import com.salutron.lifetrakwatchapp.model.Watch;

public interface DeviceScanListener {

	void onDeviceConnected(final BluetoothDevice device,
			final SALBLEService service, final Watch watch);
}
