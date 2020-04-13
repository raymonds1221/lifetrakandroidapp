package com.salutron.lifetrakwatchapp;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALBLEService.LocalBinder;
import com.salutron.blesdk.SALStatus;
import com.salutron.lifetrak.R;

/**
 * Class for Pairing and Connecting to watch. as of
 * now It should support the following
 * 
 * Notification Settings 
 * Wake up Alarm Settings 
 * Day Light Settings 
 * Night Light
 * Settings Inactive Alert Settings 
 * Calibration Data Sleep Settings 
 * User Profile
 * Settings Time and Date Settings 
 * Connection Settings
 * 
 * 
 **/
public class PairWatchActivity extends BaseActivity {
	static final String TAG = "SAL Watch Utility";
	static final int REQUEST_ENABLE_BT = 1;

	SALBLEService objService;

	BluetoothDevice devSelected;

	private boolean bScanning = false;

	private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
	private List<Integer> modelList = new ArrayList<Integer>();
	private ServiceConnection objServiceConnection;

	private Handler objHandler;
	private Handler mTimeoutHandler = new Handler();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pair_device_auto);
		init();
	}

	private void init() {
		objServiceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName name, IBinder service) {
				LocalBinder binder = (LocalBinder) service;

				objService = binder.getService();

				if (objService != null) {
					objService.registerDevListHandler(objHandler);
					startBluetoothScan();
				}
			}

			public void onServiceDisconnected(ComponentName name) {
				objService = null;
			}
		};

		objHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SALBLEService.GATT_DEVICE_FOUND_MSG:

					Bundle data = msg.getData();
					final BluetoothDevice device = data.getParcelable(BluetoothDevice.EXTRA_DEVICE);
					final int model = data.getInt(SALBLEService.SAL_DEVICE_MODEL_NUMBER);
					//Log.d(TAG, "Device found message received");

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							boolean deviceFound = false;

							for (BluetoothDevice listDev : deviceList) {
								if (listDev.getAddress().equals(getLifeTrakApplication().getSelectedWatch().getMacAddress())) {
									deviceFound = true;
									break;
								}
							}

							if (!deviceFound) {
								deviceList.add(device);
								modelList.add(model);
							} else {
								if (objService != null){
									objService.stopScan();
									objService.connectToDevice(getLifeTrakApplication().getSelectedWatch().getMacAddress(), getLifeTrakApplication().getSelectedWatch().getModel());
								}
							}
						}
					});
					break;

				case SALBLEService.GATT_DEVICE_CONNECT_MSG:
					break;

				case SALBLEService.GATT_DEVICE_READY_MSG:
					setResult(RESULT_OK);
					finish();
					break;

				case SALBLEService.GATT_DEVICE_DISCONNECT_MSG:
					break;

				default:
					super.handleMessage(msg);
				}
			}

		};

		mTimeoutHandler.postDelayed(mTimeoutRunnable, 15000);
	}

	private void startBluetoothScan() {
		int status;

		if (bScanning == true) {
			objService.stopScan();

			deviceList.clear();
			modelList.clear();
		}

		status = objService.startScan();

		switch (status) {
		case SALStatus.NO_ERROR:
			bScanning = true;
			break;
		case SALStatus.ERROR_BLUETOOTH_DISABLED:
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(this, SALBLEService.class);
		bindService(intent, objServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (bScanning == true) {
			objService.stopScan();
		}
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unbindService(objServiceConnection);

		Intent intent = new Intent(this, SALBLEService.class);
		stopService(intent);
		objService = null;

		mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
	}

	public void showMessage(String value) {
		Log.d(TAG, value);
		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	}

	public void onButtonClick(View v) {
		objService.stopScan();
		setResult(RESULT_CANCELED);
		finish();
	}


	private Runnable mTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			Intent intent = new Intent();
			intent.putExtra(DEVICE_FOUND, false);
			setResult(RESULT_CANCELED, intent);
			finish();
		}
	};

	@Override
	public void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));

	}
	
}


