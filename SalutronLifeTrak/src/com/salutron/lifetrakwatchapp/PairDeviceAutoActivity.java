package com.salutron.lifetrakwatchapp;

import java.util.Locale;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.flurry.android.FlurryAgent;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.util.BluetoothConnected;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.SalutronSDKCallback;
import com.salutron.lifetrakwatchapp.util.SalutronSDKCallback420;
import com.salutron.lifetrakwatchapp.util.SalutronSDKCallback450;

/**
 * Activity for confirming and displaying information for syncing to the watch
 * 
 * @author rsarmiento
 *
 */
public class PairDeviceAutoActivity extends BaseActivity implements SalutronSDKCallback, SalutronSDKCallback450, SalutronSDKCallback420 {
	private ImageView mPairDeviceSprite;
	private int mSelectedWatchModel;
	private int mImageIndex;
	private boolean mDeviceFound = false;
	private String mWatchName;
	private String mWatchAddress;
    private boolean mIsReady = false;
    private TextView pairingTextView;
    private  TextView textviewContent;
	
	private final Handler mHandlerScan = new Handler();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pair_device_auto);

		initializeObjects();
	}
	
	@Override
	public void onStart()
	{
	   super.onStart();
	   FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
	}

	@Override
	public void onStop()
	{
	   super.onStop();
	   FlurryAgent.onEndSession(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

        mIsReady = true;
		setSalutronSDKCallback(this);
		mLifeTrakSyncR450.setSalutronSDKCallback(this);
		mLifeTrakSyncR420.setSDKCallback(this);
		
		if (mSelectedWatchModel == WATCHMODEL_R415) {
			mLifeTrakSyncR450.bindService();
		} else {
			bindBLEService();
		}
		
		FlurryAgent.logEvent("Pairing_Page");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mBluetoothDevices.clear();
		try {
			if (mSelectedWatchModel == WATCHMODEL_R415) {
				mLifeTrakSyncR450.stopScan();
				mLifeTrakSyncR450.unbindService();
			} else {
				mSalutronService.stopScan();
				unbindBLEService();
			}

			mHandler.removeMessages(0);
			mHandlerScan.removeMessages(0);
			mHandlerScan.removeCallbacks(mHandlerScanRunnable);
		}catch (Exception e){
			LifeTrakLogger.info("Error :"+ e.getLocalizedMessage());
		}
	}
	
	/*
	 * Initialiaze views and objects
	 */
	private void initializeObjects() {
		getSupportActionBar().setTitle(R.string.pair_device_title);
		
		mSyncType = getIntent().getExtras().getInt(SYNC_TYPE);
		
		mSelectedWatchModel = getIntent().getExtras().getInt(SELECTED_WATCH_MODEL);
		setModelNumber(mSelectedWatchModel);

        pairingTextView = (TextView) findViewById(R.id.pairingTextView);
        mPairDeviceSprite = (ImageView) findViewById(R.id.imgPairDeviceSprite);
        textviewContent = (TextView) findViewById(R.id.textView_content);
		String watchName = String.valueOf(mSelectedWatchModel);
		if(mSelectedWatchModel == WATCHMODEL_C300){
			watchName = "Move C300 / C320";
		}
        else if(mSelectedWatchModel == WATCHMODEL_C300_IOS){
            watchName = "Move C300 / C320";
        }
		else if (mSelectedWatchModel == WATCHMODEL_C410){
			watchName = "Zone C410 / C410w";
		} else if(mSelectedWatchModel == WATCHMODEL_R415) {
			watchName = "Brite R450";
		}
		else if(mSelectedWatchModel == WATCHMODEL_R420) {
			watchName = "Zone R420";
		}
		pairingTextView.setText(getString(R.string.pairing) + " " + watchName);


		
		if(mSelectedWatchModel == WATCHMODEL_C300 || mSelectedWatchModel == WATCHMODEL_C410 || mSelectedWatchModel == WATCHMODEL_C300_IOS) {
			mPairDeviceSprite.setImageResource(R.drawable.ll_alert_devsetup_iphone4_rectangle_frame00);
		} else {
			mPairDeviceSprite.setImageResource(R.drawable.ll_alert_devsetup_iphone4_circle_frame00);
		}

        boolean isPaired = BluetoothConnected.getInstance(PairDeviceAutoActivity.this).deviceIsPaired();
        if (mSelectedWatchModel == WATCHMODEL_R415) {
            if (isPaired) {

                ActionBar bar = getSupportActionBar();
                bar.setBackgroundDrawable(new ColorDrawable(Color.rgb(83, 179, 196)));
               bar.setTitle(getString(R.string.pairing_sync));
//                TextView customView = (TextView)
//                        LayoutInflater.from(this).inflate(R.layout.header_bar,
//                                null);
//
//                ActionBar.LayoutParams params = new ActionBar.LayoutParams(
//                        ActionBar.LayoutParams.MATCH_PARENT,
//                        ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER );
//
//                customView.setText(getString(R.string.pairing_sync));
//                getSupportActionBar().setCustomView(customView, params);

//            View viewActionBar = getLayoutInflater().inflate(R.layout.actionbar_titletext_layout, null);
//            ActionBar.LayoutParams params = new ActionBar.LayoutParams(
//                    ActionBar.LayoutParams.WRAP_CONTENT,
//                    ActionBar.LayoutParams.MATCH_PARENT,
//                    Gravity.CENTER);
//            TextView textviewTitle = (TextView) viewActionBar.findViewById(R.id.actionbar_textview);
//            textviewTitle.setText(getString(R.string.pairing_sync));
//            bar.setCustomView(viewActionBar, params);
                bar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

                pairingTextView.setVisibility(View.GONE);
                ((TextView) findViewById(R.id.textView2)).setText(getString(R.string.pairing_sync_text));
                ((TextView) findViewById(R.id.textView2)).setTypeface(null, Typeface.BOLD);
//            ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(mPairDeviceSprite.getLayoutParams());
//            marginParams.setMargins(0, 160, 0, 0);
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
//            mPairDeviceSprite.setLayoutParams(layoutParams);
                textviewContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
                textviewContent.setGravity(Gravity.LEFT);
                textviewContent.setText(getString(R.string.pairing_sync_description));
//                textviewContent.setText("Sorry, your watch data is not syncing" +
//                        " to your app.\n" +
//                        "- Make sure the Bluetooth is active on" +
//                        " your watch (see illustration above)\n" +
//                        "- Make sure the Bluetooth is active on" +
//                        " your smartphone / tablet.\n" +
//                        "- Check to make sure your smartphone /" +
//                        " tablet has at least 20% battery power.");
            }
        }
		mHandler.sendMessageDelayed(Message.obtain(), 10);
		
		mHandlerScan.postDelayed(new Runnable() {
			public void run() {
				mBluetoothDevices.clear();
				BluetoothDevice device = null;
				
				if (mSelectedWatchModel == WATCHMODEL_R415) {
					device = mLifeTrakSyncR450.getConnectedDevice();
				} else {
					device = mSalutronService.getConnectedDevice();
				}
				
				if (mSelectedWatchModel == WATCHMODEL_R415 && device != null && mLifeTrakSyncR450.getConnectedDevice() != null) {
					mWatchName = watchNameForDevice(device);
					mWatchAddress = device.getAddress();
					
					switch (mSyncType) {
					case SYNC_TYPE_INITIAL:
						mHandler.postDelayed(new Runnable() {
							public void run() {
								Intent intent = new Intent();
								intent.putExtra(WATCH_NAME, mWatchName);
								intent.putExtra(WATCH_ADDRESS, mWatchAddress);
								intent.putExtra(DEVICE_FOUND, true);
								setResult(RESULT_OK, intent);
								finish();
							}
						}, 750);
						break;
					case SYNC_TYPE_DASHBOARD:
						boolean checkiOSMacAddress = false;
						
						if (getLifeTrakApplication().getSelectedWatch().getMacAddress().indexOf(':') == -1) {
							String androidMacAddress = convertiOSToAndroidMacAddress(getLifeTrakApplication().getSelectedWatch().getMacAddress());
							checkiOSMacAddress = device.getAddress().equals(androidMacAddress);
						}
						
						if (device.getAddress().equals(getLifeTrakApplication().getSelectedWatch().getMacAddress()) || checkiOSMacAddress) {
							mHandler.postDelayed(new Runnable() {
								public void run() {
									Intent intent = new Intent();
									intent.putExtra(WATCH_NAME, mWatchName);
									intent.putExtra(WATCH_ADDRESS, mWatchAddress);
									intent.putExtra(DEVICE_FOUND, true);
									setResult(RESULT_OK, intent);
									finish();
								}
							}, 750);
						}
						
						break;
					}
					/*Bundle data = new Bundle();
					data.putInt(MODEL_NUMBER, mSelectedWatchModel);
					onDeviceFound(bluetoothDevice, data);*/
				} else {
					if (mSelectedWatchModel == WATCHMODEL_R415) {
						mLifeTrakSyncR450.startScan();
					} else if (mSelectedWatchModel == WATCHMODEL_R420) {
						mLifeTrakSyncR420.startScan();
					} else {
						mSalutronService.startScan();
					}
				}
			}
		}, 2000);
		
		mHandlerScan.postDelayed(mHandlerScanRunnable, 15000);
	}
	
	private final Runnable mHandlerScanRunnable = new Runnable() {
		public void run() {
            Intent intent = new Intent();
            intent.putExtra(WATCH_NAME, mWatchName);
            intent.putExtra(WATCH_ADDRESS, mWatchAddress);
            intent.putExtra(DEVICE_FOUND, mDeviceFound);
            intent.putExtra(WATCH_EXISTS, mWatchExists);
            mWatchExists = false;
            setResult(RESULT_OK, intent);
            finish();
		}
	};
	
	private final Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message message) {
			
			if(mImageIndex == 30)
				mImageIndex = 0;
			
			animatePairDeviceSprite(mImageIndex);
			mImageIndex++;
		}
	};
	
	public void onButtonClick(View view) {
		if (mSelectedWatchModel == WATCHMODEL_R415) {
			mLifeTrakSyncR450.stopScan();
		} else {
			mSalutronService.stopScan();
		}
		finish();
	}
	
	/**
	 * Method for getting the drawable with supplied resource name
	 * 
	 * @param name	The name of the resource
	 * @return		Returns (Drawable) object.
	 */
	private Drawable drawableFromResourceName(String name) {
		int resourceId = getResources().getIdentifier(name, "drawable", getPackageName());
		
		if(resourceId > 0) {
			return getResources().getDrawable(resourceId);
		}
		return null;
	}
	
	/*
	 * Method for animating image sprites
	 */
	private void animatePairDeviceSprite(int index) {
		String resourceName = "";
		
		if(mSelectedWatchModel == WATCHMODEL_C300 || mSelectedWatchModel == WATCHMODEL_C410 || mSelectedWatchModel == WATCHMODEL_C300_IOS) {


                resourceName = String.format(Locale.getDefault(), "ll_alert_devsetup_iphone4_rectangle_frame%02d", index);

		} else {
            boolean isPaired = BluetoothConnected.getInstance(PairDeviceAutoActivity.this).deviceIsPaired();
            if (isPaired){
                resourceName = String.format(Locale.getDefault(), "ll_alert_devsetup_iphone4_circle_frame_blue%02d", index);
            }else{
                resourceName = String.format(Locale.getDefault(), "ll_alert_devsetup_iphone4_circle_frame%02d", index);
            }
		}

		Drawable drawableImage = drawableFromResourceName(resourceName);
		
		if(drawableImage != null) {
			mPairDeviceSprite.setImageDrawable(drawableImage);
			mHandler.sendMessageDelayed(Message.obtain(), 10);
		}
	}
	

	/**
	 * SalutronSDKCallback methods
	 */
	

	@Override
	public void onDeviceConnected(BluetoothDevice device) {
		LifeTrakLogger.info("device connected on PairDeviceAutoActivity");

		mDeviceFound = true;
		
		mWatchName = watchNameForDevice(device);
		mWatchAddress = device.getAddress();
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
		LifeTrakLogger.info("PairDeviceAutoActivity onStartSync");
	}

	@Override
	public void onSyncFinish() {
		
	}

    @Override
    public void onError(int status) {
        Toast.makeText(this, "syncing error: " + status, Toast.LENGTH_LONG).show();
    }

	@Override
	public void onDeviceReady() {
		if (mWatchName != null && mWatchAddress != null) {
			LifeTrakLogger.info("device ready on PairDeviceAutoActivity (with device name and address");

			mHandlerScan.removeMessages(0);
			mHandlerScan.removeCallbacks(mHandlerScanRunnable);

			mHandler.postDelayed(new Runnable() {
				public void run() {
					Intent intent = new Intent();
					intent.putExtra(WATCH_NAME, mWatchName);
					intent.putExtra(WATCH_ADDRESS, mWatchAddress);
					intent.putExtra(DEVICE_FOUND, true);
					setResult(RESULT_OK, intent);
					finish();
				}
			}, HANDLER_DELAY);
        } else {
			LifeTrakLogger.info("device ready on PairDeviceAutoActivity (without device name and address");
		}
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

	@Override
	public void onDeviceFound(BluetoothDevice bluetoothDevice, Bundle data) {
		LifeTrakLogger.info("device found on pair device auto activity");
		super.onDeviceFound(bluetoothDevice, data);
	}
	
	private String watchNameForDevice(BluetoothDevice device) {
		String watchName = device.getName();
		
		switch    (mSelectedWatchModel) {
		case WATCHMODEL_C300:
			watchName = WATCHNAME_C300;
			break;
		case WATCHMODEL_C410:
			watchName = WATCHNAME_C410;
			break;
		case WATCHMODEL_R415:
			watchName = WATCHNAME_R415;
			break;
		case WATCHMODEL_R420:
			watchName = WATCHNAME_R420;
			break;
		}
		
		return watchName;
	}
	
}
