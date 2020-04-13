package com.salutron.lifetrakwatchapp;


import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

import com.salutron.blesdk.SALStatus;
import com.salutron.blesdk.SALUserProfile;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.adapter.WatchAdapter;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.util.SalutronSDKCallback420;
import com.salutron.lifetrakwatchapp.view.ConnectionFailedView;
import com.salutron.lifetrakwatchapp.view.SearchDeviceView;
import com.salutron.lifetrakwatchapp.view.SyncStatusView;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;
import com.salutron.lifetrakwatchapp.util.SalutronSDKCallback;
import com.salutron.lifetrakwatchapp.util.SalutronSDKCallback450;
import com.salutron.lifetrakwatchapp.util.LifeTrakSyncR450;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import com.google.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Activity for listing and syncing the device you want to connect to the device
 * 
 * @author rsarmiento
 * 
 */
@ContentView(R.layout.activity_connection)
public class ConnectionActivity extends BaseActivity implements WatchAdapter.WatchAdapterListener, SalutronSDKCallback, SalutronSDKCallback450, SalutronSDKCallback420 {
	/*
	 * Primitive types
	 */
	private int mSelectedWatchModel;
	public static boolean mDeviceFound;
	private boolean mSearchCancelled;
	private boolean mSuccess = false;

	private int counterC300 =0;
	private int counterC410 =0;
	/*
	 * Android views
	 */
	@InjectView(R.id.lstWatch)
	private ListView mWatchList;
	@InjectView(R.id.cfvConnectionFailed)
	private ConnectionFailedView mConnectionFailedView;
	@InjectView(R.id.sdvSearchDevice)
	private SearchDeviceView mSearchDeviceView;
	@InjectView(R.id.ssvSyncStatus)
	private SyncStatusView mSyncStatusView;
	private AlertDialog alert;
	/*
	 * Declared objects
	 */

	@Inject
	private ArrayList<Watch> mWatches;
	@Inject
	private Handler mHandler = new Handler();

	private String mWatchName;
	private String mWatchAddress;
	private boolean mWatchExists;
	private boolean mProfileUpdated;
	private Watch mSelectedWatch;
	private boolean mSyncStarted = false;
	private boolean mFromError = false;

	private ProgressDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_connection);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.set_up_device);

		mSuccess = false;

		initializeObjects();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mSelectedWatchModel = savedInstanceState.getInt(SELECTED_WATCH_MODEL);
	}

	@Override
	protected void onResume() {
		super.onResume();

		setSalutronSDKCallback(this);
		mLifeTrakSyncR450.setSalutronSDKCallback(this);
		mLifeTrakSyncR420.setSDKCallback(this);
        //mLifeTrakSyncR450.disconnectR450();
		FlurryAgent.logEvent("Setup_Device_Page");
		bindBLEService();
		//mLifeTrakSyncR450.bindService();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTED_WATCH_MODEL, mSelectedWatchModel);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unbindBLEService();
		//mLifeTrakSyncR450.unbindService();
	} 



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_PAIR_DEVICE && resultCode == RESULT_OK) {
			// mSalutronService.startScan();
			if (data != null) {
				mWatchName = data.getStringExtra(WATCH_NAME);
				mWatchAddress = data.getStringExtra(WATCH_ADDRESS);
				mDeviceFound = data.getBooleanExtra(DEVICE_FOUND, false);
				mWatchExists = data.getBooleanExtra(WATCH_EXISTS, false);

				// mSearchDeviceView.setWatchModel(mSelectedWatchModel);
				// mSearchDeviceView.show();

				if (mDeviceFound) {

					mBluetoothDevices.clear();
					mStatisticalDataHeaders.clear();
					mWorkoutInfos.clear();
					mSleepDatabases.clear();

					if (mSelectedWatchModel == WATCHMODEL_R415) {
						if (mProgressDialog == null)
							reinitializeProgress();
						mProgressDialog.setMessage(getString(R.string.searching_device, watchNameForModel(mSelectedWatchModel)));
						mProgressDialog.show();

						mHandler.postDelayed(new Runnable() {
							public void run() {
								if (mProgressDialog == null)
									reinitializeProgress();
								mProgressDialog.dismiss();

								mLifeTrakSyncR450.stopScan();
								boolean success = mLifeTrakSyncR450.startSync();
								LifeTrakLogger.info("start sync status: " + success);
							}
						}, 6000);
					} else if (mSelectedWatchModel == WATCHMODEL_R420) {
						mLifeTrakSyncR420.setSDKCallback(this);
						mLifeTrakSyncR420.stopScan();
						mLifeTrakSyncR420.startSync();
					} else {
						startSync();
					}
				}

				mHandler.postDelayed(new Runnable() {
					public void run() {
						if (!mDeviceFound && !mSearchCancelled) {
							if (!mWatchExists) {
								if (mSelectedWatchModel == WATCHMODEL_C300 || mSelectedWatchModel == WATCHMODEL_C300_IOS)
									counterC300 = counterC300 + 1;
								if (mSelectedWatchModel == WATCHMODEL_C410)
									counterC410 = counterC410 + 1;

								if (counterC300 == 2 || counterC410 == 2){
									if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && mSelectedWatchModel != WATCHMODEL_R415) {
										//lollipop C300/C410
										//lollipop C300/C410
										if (!mPreferenceWrapper.getPreferenceBooleanValue(IS_REMEMBER_ME_ISSUE)) {
											if (mConnectionFailedView.isShowing())
												mConnectionFailedView.hide();

											LayoutInflater factory = LayoutInflater.from(ConnectionActivity.this);
											final View issueDialogView = factory.inflate(R.layout.alert_dialog_c300_c410_issue, null);
											final AlertDialog issueDialog = new AlertDialog.Builder(ConnectionActivity.this).create();
											issueDialog.setView(issueDialogView);
											issueDialogView.findViewById(R.id.issue_dialog_yes).setOnClickListener(new View.OnClickListener() {

												@Override
												public void onClick(View v) {
													if (mSelectedWatchModel == WATCHMODEL_C300 || mSelectedWatchModel == WATCHMODEL_C300_IOS)
														counterC300 =  1;
													if (mSelectedWatchModel == WATCHMODEL_C410)
														counterC410 = 1;

													Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(API_LOLLIPOP_ISSUE_URL));
													startActivity(intent);
													issueDialog.dismiss();
												}
											});
											issueDialogView.findViewById(R.id.issue_dialog_no).setOnClickListener(new View.OnClickListener() {

												@Override
												public void onClick(View v) {
													if (mSelectedWatchModel == WATCHMODEL_C300 || mSelectedWatchModel == WATCHMODEL_C300_IOS)
														counterC300 = 1;
													if (mSelectedWatchModel == WATCHMODEL_C410)
														counterC410 = 1;
													issueDialog.dismiss();
												}
											});
											CheckBox cb = (CheckBox) issueDialogView.findViewById(R.id.issue_checkbox_remember_choice);
											cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
												@Override
												public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
													mPreferenceWrapper.setPreferenceBooleanValue(IS_REMEMBER_ME_ISSUE, isChecked).synchronize();
												}
											});

											issueDialog.show();
										}
										else{
											if (mSelectedWatchModel == WATCHMODEL_C300 || mSelectedWatchModel == WATCHMODEL_C300_IOS)
												counterC300 = 0;
											if (mSelectedWatchModel == WATCHMODEL_C410)
												counterC410 = 0;
											mSearchDeviceView.hide();
											mConnectionFailedView.show();
										}
									}
									else{
										if (mSelectedWatchModel == WATCHMODEL_C300 || mSelectedWatchModel == WATCHMODEL_C300_IOS)
											counterC300 = 1;
										if (mSelectedWatchModel == WATCHMODEL_C410)
											counterC410 = 1;
										mSearchDeviceView.hide();
										mConnectionFailedView.show();
									}
								}else {
									mSearchDeviceView.hide();
									mConnectionFailedView.show();
								}
							} else {
								AlertDialog alert = new AlertDialog.Builder(ConnectionActivity.this).setTitle(getString(R.string.app_name)).setMessage(R.string.already_connected)
										.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface arg0, int arg1) {
												arg0.dismiss();
											}
										}).create();
								alert.show();
							}
						}

						mDeviceFound = false;
						mSearchCancelled = false;
						mWatchExists = false;
					}
				}, 1000);
			}
		} else if (requestCode == REQUEST_CODE_USER_PROFILE && resultCode == RESULT_OK) {
			mUserProfile = getLifeTrakApplication().getUserProfile();
			SALUserProfile salUserProfile = new SALUserProfile();
			salUserProfile.setWeight(mUserProfile.getWeight());
			salUserProfile.setHeight(mUserProfile.getHeight());
			salUserProfile.setBirthYear(mUserProfile.getBirthYear() - 1900);
			salUserProfile.setBirthMonth(mUserProfile.getBirthMonth());
			salUserProfile.setBirthDay(mUserProfile.getBirthDay());
			salUserProfile.setGender(mUserProfile.getGender());
			salUserProfile.setUnitSystem(mUserProfile.getUnitSystem());

			if (null != mSalutronService) {
				int status = SALStatus.ERROR_NOT_CONNECTED;

				try {
					status = mSalutronService.updateUserProfile(salUserProfile);
				} catch (NullPointerException e) {
					e.printStackTrace();
				}

				if (status == SALStatus.NO_ERROR) {
					mSyncStatusView.showSuccess();
					mPreferenceWrapper.setPreferenceBooleanValue(HAS_USER_PROFILE, true);
					mPreferenceWrapper.synchronize();
				}
			} else {
				mSyncStatusView.showFail();
			}

			mSyncStatusView.stopAnimating();

			mHandler.postDelayed(new Runnable() {
				public void run() {
					if (mSelectedWatch != null){
						if (mSelectedWatch.getModel() != WATCHMODEL_R415) {
							if (mSalutronService != null)
								mSalutronService.disconnectFromDevice();
						}
					}
				}
			}, 500);

			Intent intent = new Intent();
			if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null && mPreferenceWrapper.getPreferenceStringValue(REFRESH_TOKEN) != null && !mPreferenceWrapper
					.getPreferenceBooleanValue(HAS_USER_PROFILE)) {
				intent.setClass(this, MainActivity.class);
				intent.putExtra(SYNC_SUCCESS, true);
				startActivity(intent);

			} else if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null && mPreferenceWrapper.getPreferenceStringValue(REFRESH_TOKEN) != null && mPreferenceWrapper
					.getPreferenceBooleanValue(HAS_USER_PROFILE)) {

				mHandler.postDelayed(new Runnable() {
					public void run() {
						if (mSelectedWatch.getModel() != WATCHMODEL_R415) {
							mSalutronService.disconnectFromDevice();
						}
					}
				}, 500);

				if (NetworkUtil.getInstance(ConnectionActivity.this).isNetworkAvailable()) {
					if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null) {
						intent.setClass(ConnectionActivity.this, ServerSyncActivity.class);
						intent.putExtra(WATCH, mSelectedWatch);
						startActivity(intent);
						finish();
					}
				}else{
					alert.show();
				}

			} else {
				intent.setClass(this, SignupActivity.class);
				intent.putExtra(IS_WATCH_CONNECTED, true);
				startActivity(intent);
				setResult(RESULT_PROCESS_COMPLETE);
				finish();
			}

		} else if (requestCode == REQUEST_CODE_USER_PROFILE && resultCode == RESULT_CANCELED) {
			Intent intent = new Intent();

			if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null && mPreferenceWrapper.getPreferenceStringValue(REFRESH_TOKEN) != null) {
				intent.setClass(this, MainActivity.class);
				intent.putExtra(SYNC_SUCCESS, true);
				startActivity(intent);
				finish();
			} else {
				intent.setClass(this, SignupActivity.class);
				intent.putExtra(IS_WATCH_CONNECTED, true);
				startActivity(intent);
				setResult(RESULT_PROCESS_COMPLETE);
				finish();
			}

			mHandler.postDelayed(new Runnable() {
				public void run() {
					mProfileUpdated = true;

					if (mSelectedWatch.getModel() != WATCHMODEL_R415)
						mSalutronService.disconnectFromDevice();
				}
			}, 500);
		} else if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH && resultCode == RESULT_OK) {
			setSalutronSDKCallback(null);
			Intent intent = new Intent(this, PairDeviceAutoActivity.class);
			intent.putExtra(SYNC_TYPE, SYNC_TYPE_INITIAL);
			intent.putExtra(SELECTED_WATCH_MODEL, mSelectedWatchModel);
			startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE);
		}
	}

	@Override
	public void onBackPressed() {
		if (mConnectionFailedView.isShown()) {
			mConnectionFailedView.hide();
			return;
		}
		if (mSyncStatusView.isShown()) {
			mBluetoothDevices.clear();
			mSyncStatusView.hide();

			if (mSelectedWatchModel != WATCHMODEL_R415)
				mSalutronService.disconnectFromDevice();
			return;
		}
		mBluetoothDevices.clear();
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mSyncStarted = false;

			if (mSyncStatusView.isShown()) {
				mSuccess = true;
				mBluetoothDevices.clear();
				mSyncStatusView.hide();

				mHandler.postDelayed(new Runnable() {
					public void run() {
						if (mSelectedWatchModel != WATCHMODEL_R415)
							mSalutronService.disconnectFromDevice();
						else
							mLifeTrakSyncR450.cancelSync();
					}
				}, 1000);

			} else {
				finish();
			}
			mBluetoothDevices.clear();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * Initialize views and objects
	 */
	private void initializeObjects() {
		mWatches.add(new Watch(ConnectionActivity.this, WATCHMODEL_C300, "Move C300 / C320"));
		mWatches.add(new Watch(ConnectionActivity.this, WATCHMODEL_C410, "Zone C410 / C410w"));
		mWatches.add(new Watch(ConnectionActivity.this, WATCHMODEL_R420, "Zone R420"));
		mWatches.add(new Watch(ConnectionActivity.this, WATCHMODEL_R415, "Brite R450"));
		// mWatches.add(new Watch(ConnectionActivity.this, WATCHMODEL_R500,
		// "R500"));

		WatchAdapter watchAdapter = new WatchAdapter(this, R.layout.adapter_watch, mWatches);
		mWatchList.setAdapter(watchAdapter);

		mConnectionFailedView.setConnectionFailedListener(mConnectionFailedListener);
		mSearchDeviceView.setSearchDeviceListener(mSearchDeviceListener);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle(R.string.lifetrak_title);
		mProgressDialog.setCancelable(false);

		alert = new AlertDialog.Builder(this)

				.setTitle(R.string.lifetrak_title)
				.setMessage(R.string.check_network_connection)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						Intent intent = new Intent(ConnectionActivity.this, MainActivity.class);
						intent.putExtra(SYNC_SUCCESS, true);
						startActivity(intent);
						finish();
					}
				})
				.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if (NetworkUtil.getInstance(ConnectionActivity.this).isNetworkAvailable()) {
							if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null) {
								Intent intent = new Intent();
								intent.setClass(ConnectionActivity.this, ServerSyncActivity.class);
								intent.putExtra(WATCH, mSelectedWatch);
								startActivity(intent);
								finish();
							}
						} else {
							alert.show();
						}
					}
				}).create();
	}

	private void reinitializeProgress(){
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle(R.string.lifetrak_title);
		mProgressDialog.setCancelable(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.salutron.lifetrakwatchapp.adapter.WatchAdapter.WatchAdapterListener
	 * #onConnectToDeviceClick(int)
	 */
	@Override
	public void onConnectToDeviceClick(int watchModel) {
		mSelectedWatchModel = watchModel;
		mBluetoothDevices.clear();
		mSalutronService.stopScan();
		setModelNumber(watchModel);

		if (isBluetoothEnabled()) {
			if (watchModel == WATCHMODEL_R415 && mLifeTrakSyncR450.getConnectedDevice() != null && !mLifeTrakSyncR450.isDisconnected()) {
				if (mProgressDialog == null)
					reinitializeProgress();
				mProgressDialog.setMessage(getString(R.string.searching_device, watchNameForModel(watchModel)));
				if (!mProgressDialog.isShowing())
					mProgressDialog.show();

				final BluetoothDevice device = mLifeTrakSyncR450.getConnectedDevice();

				String convertedMacAddress = convertAndroidToiOSMacAddress(device.getAddress());
				List<Watch> watches = DataSource.getInstance(this).getReadOperation().query("macAddress = ? or macAddress = ?", device.getAddress(), convertedMacAddress).getResults(Watch.class);

				if (watches.size() == 0) {
					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (mProgressDialog == null)
								reinitializeProgress();
							mProgressDialog.dismiss();
							mLifeTrakSyncR450.startSync();
						}
					}, 3000);
				} else {
					//here
					Intent intent = new Intent(this, PairDeviceAutoActivity.class);
					intent.putExtra(SYNC_TYPE, SYNC_TYPE_INITIAL);
					intent.putExtra(SELECTED_WATCH_MODEL, watchModel);
					startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE);
				}
			} else {
				if (mLifeTrakSyncR450 != null) {
					mLifeTrakSyncR450.disconnectR450();
					LifeTrakLogger.info("Disconnect to R450 watch");
				}
				Intent intent = new Intent(this, PairDeviceAutoActivity.class);
				intent.putExtra(SYNC_TYPE, SYNC_TYPE_INITIAL);
				intent.putExtra(SELECTED_WATCH_MODEL, watchModel);
				startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE);
			}
		} else if (watchModel == WATCHMODEL_R420) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
		} else {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
		}
	}

	/*
	 * Dialog events
	 */
	private final ConnectionFailedView.ConnectionFailedListener mConnectionFailedListener = new ConnectionFailedView.ConnectionFailedListener() {

		@Override
		public void onTryAgainClick() {
//			counter = counter + 1;
//			if (counter == 2){
//                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT && mSelectedWatchModel != WATCHMODEL_R415) {
//                    //lollipop C300/C410
//                    if (!mPreferenceWrapper.getPreferenceBooleanValue(IS_REMEMBER_ME_ISSUE)) {
//						if (mConnectionFailedView.isShowing())
//							mConnectionFailedView.hide();
//
//                        LayoutInflater factory = LayoutInflater.from(ConnectionActivity.this);
//                        final View issueDialogView = factory.inflate(R.layout.alert_dialog_c300_c410_issue, null);
//                        final AlertDialog issueDialog = new AlertDialog.Builder(ConnectionActivity.this).create();
//                        issueDialog.setView(issueDialogView);
//                        issueDialogView.findViewById(R.id.issue_dialog_yes).setOnClickListener(new View.OnClickListener() {
//
//                            @Override
//                            public void onClick(View v) {
//								counter = 1;
//								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(API_LOLLIPOP_ISSUE_URL));
//								startActivity(intent);
//								issueDialog.dismiss();
//                            }
//                        });
//                        issueDialogView.findViewById(R.id.issue_dialog_no).setOnClickListener(new View.OnClickListener() {
//
//                            @Override
//                            public void onClick(View v) {
//								counter = 1;
//                                issueDialog.dismiss();
//                            }
//                        });
//                        CheckBox cb = (CheckBox) issueDialogView.findViewById(R.id.issue_checkbox_remember_choice);
//                        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                            @Override
//                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                                mPreferenceWrapper.setPreferenceBooleanValue(IS_REMEMBER_ME_ISSUE, isChecked).synchronize();
//                            }
//                        });
//
//                        issueDialog.show();
//                    }
//                    else{
//						counter = 0;
//						setModelNumber(mSelectedWatchModel);
//						mBluetoothDevices.clear();
//						mSalutronService.stopScan();
//						mConnectionFailedView.hide();
//						Intent intent = new Intent(ConnectionActivity.this, PairDeviceAutoActivity.class);
//						intent.putExtra(SYNC_TYPE, SYNC_TYPE_INITIAL);
//						intent.putExtra(SELECTED_WATCH_MODEL, mSelectedWatchModel);
//						startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE);
//                    }
//                }
//                else{
//                    counter = 0;
//                    setModelNumber(mSelectedWatchModel);
//                    mBluetoothDevices.clear();
//                    mSalutronService.stopScan();
//                    mConnectionFailedView.hide();
//                    Intent intent = new Intent(ConnectionActivity.this, PairDeviceAutoActivity.class);
//                    intent.putExtra(SYNC_TYPE, SYNC_TYPE_INITIAL);
//                    intent.putExtra(SELECTED_WATCH_MODEL, mSelectedWatchModel);
//                    startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE);
//                }
//			}
//			else{
				setModelNumber(mSelectedWatchModel);
				mBluetoothDevices.clear();
				mSalutronService.stopScan();
				mConnectionFailedView.hide();
				Intent intent = new Intent(ConnectionActivity.this, PairDeviceAutoActivity.class);
				intent.putExtra(SYNC_TYPE, SYNC_TYPE_INITIAL);
				intent.putExtra(SELECTED_WATCH_MODEL, mSelectedWatchModel);
				startActivityForResult(intent, REQUEST_CODE_PAIR_DEVICE);
		//	}

		}

		@Override
		public void onCancelClick() {
			mConnectionFailedView.hide();
		}
	};

	private final SearchDeviceView.SearchDeviceListener mSearchDeviceListener = new SearchDeviceView.SearchDeviceListener() {
		@Override
		public void onCloseClick() {
			mSearchCancelled = true;
			mSearchDeviceView.hide();
		}
	};

	/* Salutron SDK Callbacks */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onDeviceConnected
	 * (android.bluetooth.BluetoothDevice)
	 */
	public void onDeviceConnected(BluetoothDevice device) {
		LifeTrakLogger.info("device connected on ConnectionActivity");

		setSalutronSDKCallback(this);
		mBluetoothDevice = device;
		mDeviceFound = true;
		mSearchDeviceView.hide();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onDeviceDisconnected
	 * ()
	 */
	@Override
	public void onDeviceDisconnected() {
		mSyncStarted = false;

		if (mSearchDeviceView.isShown()) {
			mSearchDeviceView.hide();
		}

		if (mConnectionFailedView.isShown()) {
			mConnectionFailedView.hide();
		}

		if (mSyncStatusView.isShown()) {
			mSyncStatusView.hide();
		}

		if (mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}

		if (mDeviceFound) {
			mHandler.postDelayed(new Runnable() {
				public void run() {
					if (!mSuccess) {
						mConnectionFailedView.show();
					}
					mSuccess = false;
				}
			}, HANDLER_DELAY);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onStartSync()
	 */
	@Override
	public void onStartSync() {
		if (!mSyncStatusView.isShown())
			mSyncStatusView.startAnimating();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onSyncTime()
	 */
	@Override
	public void onSyncTime() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, getString(R.string.time_caps)));
        //mSyncStatusView.setStatusText(getString(R.string.syncing_data, "STATISTICAL DATA HEADERS"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#
	 * onSyncStatisticalDataHeaders()
	 */
	@Override
	public void onSyncStatisticalDataHeaders() {
		mSyncStarted = true;
        //mSyncStatusView.setStatusText(getString(R.string.syncing_data, getString(R.string.time_caps)));
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "STATISTICAL DATA HEADERS"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#
	 * onSyncStatisticalDataPoint(int)
	 */
	@Override
	public void onSyncStatisticalDataPoint(int percent) {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "STATISTICAL DATA POINTS - " + percent + "%"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onSyncStepGoal()
	 */
	@Override
	public void onSyncStepGoal() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "STEP GOAL"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onSyncDistanceGoal
	 * ()
	 */
	@Override
	public void onSyncDistanceGoal() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "DISTANCE GOAL"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onSyncCalorieGoal
	 * ()
	 */
	@Override
	public void onSyncCalorieGoal() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "CALORIE GOAL"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onSyncSleepSetting
	 * ()
	 */
	@Override
	public void onSyncSleepSetting() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "SLEEP SETTINGS"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onSyncCalibrationData
	 * ()
	 */
	@Override
	public void onSyncCalibrationData() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "CALIBRATION DATA"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onSyncWorkoutDatabase
	 * ()
	 */
	@Override
	public void onSyncWorkoutDatabase() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "WORKOUT DATABASE"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onSyncSleepDatabase
	 * ()
	 */
	@Override
	public void onSyncSleepDatabase() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "SLEEP DATABASE"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onSyncUserProfile
	 * ()
	 */
	@Override
	public void onSyncUserProfile() {
		mSuccess = true;
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "USER PROFILE"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.salutron.lifetrakwatchapp.util.SalutronSDKCallback#onSyncFinish()
	 */
	@Override
	public void onSyncFinish() {
		mSyncStarted = false;
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "STORING DATA"));
		mPreferenceWrapper.setPreferenceBooleanValue(AUTO_SYNC, true)
		.synchronize();

		final Watch watch = new Watch(ConnectionActivity.this);

		final Calendar calendar = Calendar.getInstance();
		Date lastSyncDate = calendar.getTime();

		watch.setModel(mSelectedWatchModel);
		watch.setName(mWatchName);
		watch.setMacAddress(mWatchAddress);
		watch.setLastSyncDate(lastSyncDate);
		watch.setCloudLastSyncDate(new Date());

		mPreferenceWrapper.setPreferenceBooleanValue(IS_WATCH_CONNECTED, true).synchronize();

		if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null)
			watch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));

		if (mSelectedWatchModel == WATCHMODEL_R415) {
			mLifeTrakSyncR450.setSyncType(LifeTrakSyncR450.SYNC_TYPE_INITIAL);

			if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null) {
				mLifeTrakSyncR450.getUserProfile().setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
				mLifeTrakSyncR450.getUserProfile().setFirstname(mPreferenceWrapper.getPreferenceStringValue(FIRST_NAME));
				mLifeTrakSyncR450.getUserProfile().setLastname(mPreferenceWrapper.getPreferenceStringValue(LAST_NAME));
				mLifeTrakSyncR450.getUserProfile().setEmail(mPreferenceWrapper.getPreferenceStringValue(EMAIL));
			}

			new Thread(new Runnable() {
				public void run() {
					mLifeTrakSyncR450.storeData(watch);

					runOnUiThread(new Runnable() {
						public void run() {
							getLifeTrakApplication().setTimeDate(mLifeTrakSyncR450.getTimeDate());
							getLifeTrakApplication().setUserProfile(mLifeTrakSyncR450.getUserProfile());

							mPreferenceWrapper.setPreferenceBooleanValue(HAS_PAIRED, true).setPreferenceStringValue(MAC_ADDRESS, watch.getMacAddress()).synchronize();

							mHandler.post(new Runnable() {
								public void run() {

									mSyncStatusView.showSuccess();


								}
							});
							mHandler.postDelayed(new Runnable() {
								@Override
								public void run() {
									mSyncStatusView.stopAnimating();

									Intent intent = new Intent();
									getLifeTrakApplication().setSelectedWatch(watch);
									getLifeTrakApplication().setCurrentDate(new Date());
									mSuccess = true;
									mSelectedWatch = watch;
									if (mPreferenceWrapper.getPreferenceBooleanValue(HAS_USER_PROFILE)) {
										//mSalutronService.disconnectFromDevice();
										List<Watch> watches = new ArrayList<Watch>();

										watches = DataSource
												.getInstance(ConnectionActivity.this)
												.getReadOperation()
												.query("macAddress = ?",
														mWatchAddress)
												.getResults(Watch.class);

										if (watches.size() > 0){
											mSelectedWatch = watches.get(0);
											getLifeTrakApplication().setSelectedWatch(mSelectedWatch);
										}

										if (NetworkUtil.getInstance(ConnectionActivity.this).isNetworkAvailable()) {
											if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null) {
												intent.setClass(ConnectionActivity.this, ServerSyncActivity.class);
												intent.putExtra(IS_WATCH_CONNECTED, true);
												intent.putExtra(WATCH, watch);
												startActivity(intent);
												finish();
											} else {
												intent.setClass(ConnectionActivity.this, SignupActivity.class);
												intent.putExtra(IS_WATCH_CONNECTED, true);
												startActivity(intent);
												setResult(RESULT_PROCESS_COMPLETE);
												finish();
											}

										} else {

											intent.setClass(ConnectionActivity.this, MainActivity.class);
											intent.putExtra(SYNC_SUCCESS, true);
											startActivity(intent);
											finish();
										}
									} else {
										intent.setClass(ConnectionActivity.this, UserProfileActivity.class);
										startActivityForResult(intent, REQUEST_CODE_USER_PROFILE);

									}
								}
							}, 1500);
						}
					});
				}
			}).start();
		} else if (mSelectedWatchModel == WATCHMODEL_R420) {
			//UserProfile userProfile= mLifeTrakSyncR420.userProfile;
			if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null) {
				mLifeTrakSyncR420.getUserProfile().setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
				mLifeTrakSyncR420.getUserProfile().setFirstname(mPreferenceWrapper.getPreferenceStringValue(FIRST_NAME));
				mLifeTrakSyncR420.getUserProfile().setLastname(mPreferenceWrapper.getPreferenceStringValue(LAST_NAME));
				mLifeTrakSyncR420.getUserProfile().setEmail(mPreferenceWrapper.getPreferenceStringValue(EMAIL));
				mLifeTrakSyncR420.getWatch().setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
			}

			getLifeTrakApplication().setTimeDate(mLifeTrakSyncR420.getTimeDate());
			getLifeTrakApplication().setUserProfile(mLifeTrakSyncR420.getUserProfile());

			mLifeTrakSyncR420.getWatch().setContext(this);
			mLifeTrakSyncR420.getWatch().setModel(mSelectedWatchModel);
			mLifeTrakSyncR420.getWatch().setName(mWatchName);
			mLifeTrakSyncR420.getWatch().setMacAddress(mWatchAddress);
			mLifeTrakSyncR420.getWatch().update();

			getLifeTrakApplication().setSelectedWatch(mLifeTrakSyncR420.getWatch());

			getLifeTrakApplication().setCurrentDate(new Date());
			mHandler.post(new Runnable() {
				public void run() {

					mSyncStatusView.showSuccess();


				}
			});
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mSyncStatusView.stopAnimating();
					mSuccess = true;
					mSelectedWatch = mLifeTrakSyncR420.getWatch();
					Watch currentWatch  = watch;
					Intent intent = new Intent();

					if (mPreferenceWrapper.getPreferenceBooleanValue(HAS_USER_PROFILE)) {
						List<Watch> watches = new ArrayList<Watch>();

						watches = DataSource
								.getInstance(ConnectionActivity.this)
								.getReadOperation()
								.query("macAddress = ?",
										mWatchAddress)
								.getResults(Watch.class);

						if (watches.size() > 0){
							currentWatch = watches.get(0);
							getLifeTrakApplication().setSelectedWatch(currentWatch);
						}

						if (NetworkUtil.getInstance(ConnectionActivity.this).isNetworkAvailable()) {
							if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null) {

								mLifeTrakSyncR420.getBLEService().disconnectFromDevice();
								intent.setClass(ConnectionActivity.this, ServerSyncActivity.class);
								intent.putExtra(IS_WATCH_CONNECTED, true);
								intent.putExtra(WATCH, currentWatch);
								startActivity(intent);
								finish();
							} else {
								intent.setClass(ConnectionActivity.this, SignupActivity.class);
								intent.putExtra(IS_WATCH_CONNECTED, true);
								startActivity(intent);
								setResult(RESULT_PROCESS_COMPLETE);
								finish();
							}

						} else {
							intent.setClass(ConnectionActivity.this, MainActivity.class);
							intent.putExtra(SYNC_SUCCESS, true);
							startActivity(intent);
						}
					} else {
						intent.setClass(ConnectionActivity.this, UserProfileActivity.class);
						startActivityForResult(intent, REQUEST_CODE_USER_PROFILE);
					}
				}
			}, 1500);


		} else {
			new Thread(new Runnable() {
				public void run() {
					if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null) {
						watch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
						mUserProfile.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
						mUserProfile.setFirstname(mPreferenceWrapper.getPreferenceStringValue(FIRST_NAME));
						mUserProfile.setLastname(mPreferenceWrapper.getPreferenceStringValue(LAST_NAME));
						mUserProfile.setEmail(mPreferenceWrapper.getPreferenceStringValue(EMAIL));
					}

					try{
						String firmwareVer = mPreferenceWrapper.getPreferenceStringValue(FIRMWAREVERSION);
						if(firmwareVer != null)
						watch.setWatchFirmWare(firmwareVer);
					}
					catch (Exception e){
						LifeTrakLogger.info("Erro e" + e.getLocalizedMessage());
					}
					try{
						String softwareVer = mPreferenceWrapper.getPreferenceStringValue(SOFTWAREVERSION);
						if(softwareVer != null)
							watch.setWatchSoftWare(softwareVer);
					}
					catch (Exception e){
						LifeTrakLogger.info("Erro e" + e.getLocalizedMessage());
					}

					watch.setTimeDate(mTimeDate);
					watch.setStatisticalDataHeaders(mStatisticalDataHeaders);

					List<Goal> goals = new CopyOnWriteArrayList<Goal>();

					for (StatisticalDataHeader dataHeader : mStatisticalDataHeaders) {
						calendar.setTime(dataHeader.getDateStamp());

						int day = calendar.get(Calendar.DAY_OF_MONTH);
						int month = calendar.get(Calendar.MONTH) + 1;
						int year = calendar.get(Calendar.YEAR) - 1900;

						Goal goal = new Goal(ConnectionActivity.this);
						goal.setStepGoal(mStepGoal);
						goal.setDistanceGoal(mDistanceGoal);
						goal.setCalorieGoal(mCalorieGoal);

						if (mSleepSetting != null)
							goal.setSleepGoal(mSleepSetting.getSleepGoalMinutes());

						goal.setDate(calendar.getTime());
						goal.setDateStampDay(day);
						goal.setDateStampMonth(month);
						goal.setDateStampYear(year);
						goal.setWatch(watch);
						goals.add(goal);
					}

					if (mCalibrationData != null) {
						watch.setCalibrationData(mCalibrationData);
						mCalibrationData.setWatch(watch);
					}

					watch.setGoal(goals);

					if (mSleepSetting != null)
						watch.setSleepSetting(mSleepSetting);
					if (mSleepDatabases != null)
						watch.setSleepDatabases(mSleepDatabases);
					if (mWorkoutInfos != null)
						watch.setWorkoutInfos(mWorkoutInfos);
					watch.setUserProfile(mUserProfile);
					mUserProfile.setWatch(watch);

					DataSource.getInstance(ConnectionActivity.this).getWriteOperation().open().beginTransaction().insert(watch).insert(mTimeDate).insert(mSleepSetting).insert(mCalibrationData)
					.insert(mUserProfile).endTransaction().close();

                    LifeTrakLogger.info("Sync End From Watch - " + new Date());

					List<Watch> watches = DataSource.getInstance(ConnectionActivity.this).getReadOperation().getResults(Watch.class);

					LifeTrakLogger.info("watches: " + watches);

					mStatisticalDataHeaders.clear();
					mWorkoutInfos.clear();
					mSleepDatabases.clear();

					getLifeTrakApplication().setTimeDate(mTimeDate);
					getLifeTrakApplication().setUserProfile(mUserProfile);

					mPreferenceWrapper.setPreferenceBooleanValue(HAS_PAIRED, true).setPreferenceStringValue(MAC_ADDRESS, watch.getMacAddress()).synchronize();

					mHandler.post(new Runnable() {
						public void run() {


							getLifeTrakApplication().setSelectedWatch(watch);
							getLifeTrakApplication().setCurrentDate(new Date());

							mSyncStatusView.showSuccess();
						}
					});
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							Intent intent = new Intent();


							mSyncStatusView.stopAnimating();
							mSuccess = true;
							mSelectedWatch = watch;

							if (mPreferenceWrapper.getPreferenceBooleanValue(HAS_USER_PROFILE)) {
								Watch currentWatch = watch;
								List<Watch> watches = new ArrayList<Watch>();

								watches = DataSource
										.getInstance(ConnectionActivity.this)
										.getReadOperation()
										.query("macAddress = ?",
												mWatchAddress)
										.getResults(Watch.class);

								if (watches.size() > 0){
									currentWatch = watches.get(0);
									getLifeTrakApplication().setSelectedWatch(currentWatch);
								}
								mHandler.postDelayed(new Runnable() {
									public void run() {
										mSalutronService.disconnectFromDevice();
									}
								}, 1000);

								if (NetworkUtil.getInstance(ConnectionActivity.this).isNetworkAvailable()) {
									if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null) {
										intent.setClass(ConnectionActivity.this, ServerSyncActivity.class);
										intent.putExtra(WATCH, currentWatch);
										startActivity(intent);

									} else {
										intent.setClass(ConnectionActivity.this, SignupActivity.class);
										intent.putExtra(IS_WATCH_CONNECTED, true);
										startActivity(intent);
										setResult(RESULT_PROCESS_COMPLETE);
										finish();
									}
								} else {
									intent.setClass(ConnectionActivity.this, MainActivity.class);
									intent.putExtra(SYNC_SUCCESS, true);
									startActivity(intent);
									finish();
								}
							} else {
								intent.setClass(ConnectionActivity.this, UserProfileActivity.class);
								startActivityForResult(intent, REQUEST_CODE_USER_PROFILE);
							}
						}
					}, 1500);
				}
			}).start();
		}
	}

	@Override
	public void onDeviceReady() {
		LifeTrakLogger.info("device ready on ConnectionActivity");

		/*if (mFromError && mSelectedWatchModel == WATCHMODEL_R415) {
            mFromError = false;

            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            mLifeTrakSyncR450.startSync();
        }*/
	}

	@Override
	public void onSyncLightDataPoints(int percent) {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "LIGHT DATA POINTS - " + percent + "%"));
	}

	@Override
	public void onSyncWorkoutStopDatabase(int percent) {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "WORKOUT STOPS - " + percent + "%"));
	}

	@Override
	public void onSyncWakeupSetting() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "WAKEUP SETTINGS"));
	}

	@Override
	public void onSyncNotifications() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "NOTIFICATIONS"));
	}

	@Override
	public void onSyncActivityAlertSettingsData() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "ACTIVITY ALERT SETTINGS"));
	}

	@Override
	public void onSyncDayLightSettingsData() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "DAY LIGHT SETTINGS"));
	}

	@Override
	public void onSyncNightLightSettingsData() {
		mSyncStatusView.setStatusText(getString(R.string.syncing_data, "NIGHT LIGHT SETTINGS"));
	}

	@Override
	public void onDeviceFound(BluetoothDevice bluetoothDevice, Bundle data) {

	}

	@Override
	public void onError(int status) {
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();

		if (status == SALStatus.ERROR_NOT_SUPPORTED) {
			AlertDialog alert = new AlertDialog.Builder(this)
			.setTitle(R.string.lifetrak_title)
			.setMessage(R.string.device_not_supported)
			.setNegativeButton(R.string.cancel, null)
			.create();
			alert.show();
		} else if(status == SALStatus.ERROR_HARDWARE_PROBLEM && !mConnectionFailedView.isShowing()) {
            mConnectionFailedView.show();
        }
	}

	private String watchNameForModel(int model) {
		switch (model) {
		case WATCHMODEL_C300:
		case WATCHMODEL_C300_IOS:
			return WATCHNAME_C300;
		case WATCHMODEL_C410:
			return WATCHNAME_C410;
		case WATCHMODEL_R420:
			return WATCHNAME_R420;
		}
		return WATCHNAME_R415;
	}

	public void setDeviceFound(boolean mBoolean){
		mDeviceFound = mBoolean;
	}
}
