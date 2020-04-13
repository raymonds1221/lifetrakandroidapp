package com.salutron.lifetrakwatchapp.util;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

/**
 * Created by raymondsarmiento on 10/6/15.
 */
public interface SalutronSDKCallback420 extends SalutronSDKCallback {
    public void onDeviceFound(BluetoothDevice bluetoothDevice, Bundle data);
}