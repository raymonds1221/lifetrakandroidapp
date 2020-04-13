package com.salutron.lifetrakwatchapp;

import java.util.List;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALBLEService.LocalBinder;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.fragment.DashboardFragment;
import com.salutron.lifetrakwatchapp.model.Notification;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.LifeTrakWatchUtil;
import com.salutron.lifetrakwatchapp.view.ConnectionFailedView;

public class NotificationSettingActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener {

	private static String TAG = NotificationSettingActivity.class.getCanonicalName();

	@InjectView(R.id.notification_switch)
	private Switch mNotifSwitch;

	@InjectView(R.id.display_onwatch)
	private TableRow mDisplayOnWatchRow;

	@InjectView(R.id.display_onwatch_value_txv)
	private TextView mDisplayOnWatchTxv;

	@InjectView(R.id.email_switch)
	private Switch mEmailSwitch;

	@InjectView(R.id.news_switch)
	private Switch mNewsSwitch;

	@InjectView(R.id.incomingcall_switch)
	private Switch mIncomingCallSwitch;

	@InjectView(R.id.missedcall_switch)
	private Switch mMissedCallSwitch;

	@InjectView(R.id.sms_switch)
	private Switch mSMSSwitch;

	@InjectView(R.id.voicemail_switch)
	private Switch mVoiceMailSwitch;

	@InjectView(R.id.schedule_switch)
	private Switch mScheduleSwitch;

	@InjectView(R.id.highpriority_switch)
	private Switch mHighPrioritySwitch;

	@InjectView(R.id.instantmessage_switch)
	private Switch mInstantmessageSwitch;

	@InjectView (R.id.cfvConnectionFailedNotif)
	private ConnectionFailedView mConnectionFailedView;

	@InjectView (R.id.notif_Linear)
	private LinearLayout notifLinear;

	@InjectView(R.id.view1)
	private View view1;
	@InjectView(R.id.view2)
	private View view2;
	@InjectView(R.id.view3)
	private View view3;
	@InjectView(R.id.view4)
	private View view4;
	@InjectView(R.id.view5)
	private View view5;
	@InjectView(R.id.view6)
	private View view6;
	@InjectView(R.id.view7)
	private View view7;
	@InjectView(R.id.view8)
	private View view8;
	@InjectView(R.id.view10)
	private View view10;
	@InjectView (R.id.unitprefs_caption)
	private TextView unitprefs_caption;
	@InjectView (R.id.myLabel)
	private TextView Label;
	@InjectView (R.id.display_onwatch_value_txv)
	private TextView display_onwatch_value_txv;

	private int mSelecteddisplayOnWatch;
	private AlertDialog.Builder dialogBuilder ;
	private Dialog mDialogDisplayOnWatch;

	private Notification mNotificationSettings;

	int nItemsInit = 0;
	boolean bRegsFlag = false;

	private SALBLEService objService;
	private ProgressDialog mProgressDialog;
	private ServiceConnection objServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			objService = binder.getService();

			objService.registerDevDataHandler(objHandler);
			regsTel();
		}

		public void onServiceDisconnected(ComponentName name) {
			objService = null;
		}
	};

	public void regsTel() {
		if (bRegsFlag == false) {
			TelephonyManager telMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
			telMgr.listen(objService.phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

			bRegsFlag = true;
		}
	}

	private Handler objHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SALBLEService.SAL_MSG_DEVICE_DATA_RECEIVED:

				final Bundle bundleData = msg.getData();
				final int devDataType = bundleData.getInt(SALBLEService.SAL_DEVICE_DATA_TYPE);

				runOnUiThread(new Runnable() {
					public void run() {
						switch (devDataType) {
						case SALBLEService.COMMAND_GET_NOTIFICATION_FILTER:
							byte[] statusData = bundleData.getByteArray(SALBLEService.SAL_DEVICE_DATA);
							nItemsInit = 1;
							saveNotificationSettings();
							break;
						}
					}
				});

				break;

			default:
				super.handleMessage(msg);
			}

		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification_settings);
		getSupportActionBar().setTitle(getString(R.string.settings_notification_btn_caption));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		LifeTrakLogger.debug("OnCreate");
		initViews();

		mNotificationSettings = new Notification();
		dialogBuilder = new AlertDialog.Builder(this);
		List<Notification> notifications = DataSource.getInstance(this)
				.getReadOperation()
				.query("watchNotification = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(Notification.class);

		if (notifications.size() > 0) {
			mNotificationSettings = notifications.get(0);
			initNotifications(mNotificationSettings);
		}

		mNotifSwitch.setChecked(mPreferenceWrapper.getPreferenceBooleanValue(NOTIFICATION_ENABLED));
		showView(mPreferenceWrapper.getPreferenceBooleanValue(NOTIFICATION_ENABLED));

		mConnectionFailedView.setConnectionFailedListener(new ConnectionFailedView.ConnectionFailedListener() {
			@Override
			public void onCancelClick() {
				notifLinear.setVisibility(View.VISIBLE);
				mConnectionFailedView.hide();
			}

			@Override
			public void onTryAgainClick() {
				mConnectionFailedView.hide();
				BluetoothDevice connectedDevice = mSalutronService.getConnectedDevice();
				if (connectedDevice != null) {
					mProgressDialog.show();
					new Thread(
							new Runnable() {
								@Override
								public void run() {
									saveNotificationSettings();
								}
							}
							).start();
				} else {
					syncToWatch();
				}

			}
		});

	}
	
	@Override
	public void onStart()
	{
	   super.onStart();
	   FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
	   
	}
//	@Override
//	public void onStop()
//	{
//	   super.onStop();
//	   FlurryAgent.onEndSession(this);
//
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_sync, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		//bindServiceConnection();
		bindBLEService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//unbindService(objServiceConnection);
		unbindBLEService();
	}

	private void bindServiceConnection() {
		Intent intent = new Intent(this, SALBLEService.class);
		bindService(intent, objServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_PAIR_DEVICE && resultCode == RESULT_OK) {
			BluetoothDevice connectedDevice = mSalutronService.getConnectedDevice();
			if (null != connectedDevice) {
				mProgressDialog.show();
				Handler h = new Handler();
				h.postDelayed(new Runnable() {

					@Override
					public void run() {
						//objService.getNotificationStatus();
						saveNotificationSettings();
					}
				}, 5000);
			}
			else
			{
				mProgressDialog.hide();
				final Dialog dialog = new Dialog(this);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.view_device_not_found);

				Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
				Button btnTryAgain = (Button) dialog.findViewById(R.id.btnTryAgain);

				btnCancel.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				btnTryAgain.setOnClickListener(new OnClickListener( ) {
					public void onClick(View v) {
						dialog.dismiss();
						BluetoothDevice connectedDevice = mSalutronService.getConnectedDevice();
						if (connectedDevice != null) {
							mProgressDialog.show();
							new Thread(
									new Runnable() {
										@Override
										public void run() {
											saveNotificationSettings();
										}
									}
									).start();
						} else {
							syncToWatch();
						}
					}
				});
				dialog.show();
			}
		}
		else if(resultCode == RESULT_CANCELED){
			mProgressDialog.hide();
			try{

				boolean deviceFound = data.getBooleanExtra(DEVICE_FOUND, false);
				if (!deviceFound){


					final Dialog dialog = new Dialog(this);
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog.setContentView(R.layout.view_device_not_found);

					Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
					Button btnTryAgain = (Button) dialog.findViewById(R.id.btnTryAgain);

					btnCancel.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
						}
					});

					btnTryAgain.setOnClickListener(new OnClickListener( ) {
						public void onClick(View v) {
							dialog.dismiss();
							BluetoothDevice connectedDevice = mSalutronService.getConnectedDevice();
							if (connectedDevice != null) {
								mProgressDialog.show();
								new Thread(
										new Runnable() {
											@Override
											public void run() {
												saveNotificationSettings();
											}
										}
										).start();
							} else {
								syncToWatch();
							}
						}
					});
					dialog.show();
				}
				//onPairTimeout();
			}catch(Exception e){
				
			}
		} 
	}

	private void onPairTimeout() {
		notifLinear.setVisibility(View.GONE);
		if (!mConnectionFailedView.isShown())
			mConnectionFailedView.show();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.button_sync:
			BluetoothDevice connectedDevice = mSalutronService.getConnectedDevice();
			if (connectedDevice != null) {
				mProgressDialog.show();
				new Thread(
						new Runnable() {
							@Override
							public void run() {
								saveNotificationSettings();
							}
						}
						).start();
			} else {
				syncToWatch();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void initViews() {

		mDialogDisplayOnWatch = new Dialog(this);
		mDialogDisplayOnWatch.setContentView(R.layout.dialog_displaywatch);
		mDialogDisplayOnWatch.setTitle(getString(R.string.settings_notif_display_onwatch));
		OnClickListener displayOnWatchDialogBtnListenter = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.display_always) {
					// TODO: set the value of mSelecteddisplayOnWatch for
					// settings integration
					mDisplayOnWatchTxv.setText(getString(R.string.settings_notif_display_onwatch_always));
				} else if (v.getId() == R.id.display_whenawake) {
					mDisplayOnWatchTxv.setText(getString(R.string.settings_notif_display_onwatch_when_awake));
				}
				mDialogDisplayOnWatch.dismiss();
			}
		};
		mDialogDisplayOnWatch.findViewById(R.id.display_always).setOnClickListener(displayOnWatchDialogBtnListenter);
		mDialogDisplayOnWatch.findViewById(R.id.display_whenawake).setOnClickListener(displayOnWatchDialogBtnListenter);
		mDialogDisplayOnWatch.findViewById(R.id.cancel).setOnClickListener(displayOnWatchDialogBtnListenter);
		mDisplayOnWatchRow.setOnClickListener(this);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle(getString(R.string.settings_notification_btn_caption));
		mProgressDialog.setMessage(getString(R.string.now_syncing));

		mNotifSwitch.setOnCheckedChangeListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
		saveNotificationSettings();
	}

	private void initNotifications(Notification notification) {
		mEmailSwitch.setChecked(notification.isEmailEnabled());
		mNewsSwitch.setChecked(notification.isNewsEnabled());
		mIncomingCallSwitch.setChecked(notification.isIncomingCallEnabled());
		mMissedCallSwitch.setChecked(notification.isMissedCallEnabled());
		mSMSSwitch.setChecked(notification.isSmsEnabled());
		mVoiceMailSwitch.setChecked(notification.isVoiceMailEnabled());
		mScheduleSwitch.setChecked(notification.isScheduleEnabled());
		mHighPrioritySwitch.setChecked(notification.isHighPriorityEnabled());
		mInstantmessageSwitch.setChecked(notification.isInstantMessageEnabled());
	}

	@Override
	public void onClick(View v) {
		if (v == mDisplayOnWatchRow) {
			mDialogDisplayOnWatch.show();
		}
	}

	public void syncToWatch() {
		if (isBluetoothEnabled()) {
			Intent intent = new Intent(this, PairWatchActivity.class);
			startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE);
		} else {
			startBluetoothRequest(REQUEST_CODE_ENABLE_BLUETOOTH);
		}
	}

	private void startBluetoothRequest(int requestCode) {
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, requestCode);
	}

	/*public void saveNotificationSettings() {

		mNotificationSettings.setEmailEnabled(mEmailSwitch.isChecked());
		mNotificationSettings.setNewsEnabled(mNewsSwitch.isChecked());
		mNotificationSettings.setIncomingCallEnabled(mIncomingCallSwitch.isChecked());
		mNotificationSettings.setMissedCallEnabled(mMissedCallSwitch.isChecked());
		mNotificationSettings.setSmsEnabled(mSMSSwitch.isChecked());
		mNotificationSettings.setVoiceMailEnabled(mVoiceMailSwitch.isChecked());
		mNotificationSettings.setScheduleEnabled(mScheduleSwitch.isChecked());
		mNotificationSettings.setHighPriorityEnabled(mHighPrioritySwitch.isChecked());
		mNotificationSettings.setInstantMessageEnabled(mInstantmessageSwitch.isChecked());

		LifeTrakWatchUtil.saveNotificationSettings(objService, mNotificationSettings);
		mNotificationSettings.setContext(this);
		mNotificationSettings.update();
		mProgressDialog.dismiss();

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.settings_notification));
		alert.setMessage(getString(R.string.settings_saved));
		alert.setNeutralButton(getString(R.string.ok), new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}

		});
		alert.show();
	}*/

	public void saveNotificationSettings() {
		if (mNotificationSettings != null) {
			mNotificationSettings.setContext(this);
			mNotificationSettings.setEmailEnabled(mEmailSwitch.isChecked());
			mNotificationSettings.setNewsEnabled(mNewsSwitch.isChecked());
			mNotificationSettings.setIncomingCallEnabled(mIncomingCallSwitch.isChecked());
			mNotificationSettings.setMissedCallEnabled(mMissedCallSwitch.isChecked());
			mNotificationSettings.setSmsEnabled(mSMSSwitch.isChecked());
			mNotificationSettings.setVoiceMailEnabled(mVoiceMailSwitch.isChecked());
			mNotificationSettings.setScheduleEnabled(mScheduleSwitch.isChecked());
			mNotificationSettings.setHighPriorityEnabled(mHighPrioritySwitch.isChecked());
			mNotificationSettings.setInstantMessageEnabled(mInstantmessageSwitch.isChecked());

			mNotificationSettings.update();
		}
		else{
			mNotificationSettings = new Notification();
			mNotificationSettings.setContext(this);
			mNotificationSettings.setEmailEnabled(mEmailSwitch.isChecked());
			mNotificationSettings.setNewsEnabled(mNewsSwitch.isChecked());
			mNotificationSettings.setIncomingCallEnabled(mIncomingCallSwitch.isChecked());
			mNotificationSettings.setMissedCallEnabled(mMissedCallSwitch.isChecked());
			mNotificationSettings.setSmsEnabled(mSMSSwitch.isChecked());
			mNotificationSettings.setVoiceMailEnabled(mVoiceMailSwitch.isChecked());
			mNotificationSettings.setScheduleEnabled(mScheduleSwitch.isChecked());
			mNotificationSettings.setHighPriorityEnabled(mHighPrioritySwitch.isChecked());
			mNotificationSettings.setInstantMessageEnabled(mInstantmessageSwitch.isChecked());
			mNotificationSettings.setWatch(getLifeTrakApplication().getSelectedWatch());

			mNotificationSettings.insert();
		}

	}

	private void showView (boolean mboolean){
		mPreferenceWrapper.setPreferenceBooleanValue(NOTIFICATION_ENABLED, mboolean).synchronize();
		mEmailSwitch.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		mNewsSwitch.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		mIncomingCallSwitch.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		mMissedCallSwitch.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		mSMSSwitch.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		mVoiceMailSwitch.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		mScheduleSwitch.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		mHighPrioritySwitch.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		mInstantmessageSwitch.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view1.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view2.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view3.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view4.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view5.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view6.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view7.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view8.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		unitprefs_caption.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		Label.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		display_onwatch_value_txv.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view10.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		switch (v.getId()){
		case R.id.notification_switch:
			showView(isChecked);
			break;
		}
	}





}