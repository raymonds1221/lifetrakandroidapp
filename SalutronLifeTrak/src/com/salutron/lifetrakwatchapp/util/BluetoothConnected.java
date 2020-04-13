package com.salutron.lifetrakwatchapp.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.ConnectivityManager;

import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.model.Watch;

import java.util.Locale;
import java.util.Set;

/**
 * Created by janwelcris on 6/15/2015.
 */
public class BluetoothConnected  implements SalutronLifeTrakUtility  {
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private static BluetoothConnected mBluetoothConnected;
    private static final Object LOCK_OBJECT = BluetoothConnected.class;
    private static LifeTrakApplication mLifeTrakApplication;
    private static PreferenceWrapper mPreferenceWrapper;

    private BluetoothConnected(Context context) {
        mContext = context;

    }

    public static final BluetoothConnected getInstance(Context context) {
        synchronized(LOCK_OBJECT) {
            if(mBluetoothConnected == null){
                mBluetoothConnected = new BluetoothConnected(context);
                mPreferenceWrapper = PreferenceWrapper.getInstance(context);
            }
            return mBluetoothConnected;
        }
    }

    public boolean deviceIsPaired(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            return true;
//            for (BluetoothDevice device : pairedDevices) {
//                String deviceConnectedMacAddress =  device.getAddress().toString();
//                String watchMacAddress = mPreferenceWrapper.getPreferenceStringValue(LAST_SYNCED_R450_WATCH_MAC_ADDRESS);
//
//                if (watchMacAddress != null){
//                    if (!watchMacAddress.contains(":"))
//                        watchMacAddress = convertiOSToAndroidMacAddress(watchMacAddress);
//
//                    if (deviceConnectedMacAddress.equalsIgnoreCase(watchMacAddress))
//                            return true;
//                }
//            }
        }
        return false;
    }

    private String convertiOSToAndroidMacAddress(String macAddress) {
        macAddress = macAddress.replace("0000", "").toUpperCase(Locale.getDefault());
        int start = 0;

        String value = "";

        while (start < macAddress.length() - 1) {
            value = macAddress.substring(start, start + 2) + ((start == 0) ? "" : ":") + value;

            start += 2;
        }

        return value;
    }
}
