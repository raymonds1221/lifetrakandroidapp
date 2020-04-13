package com.salutron.lifetrakwatchapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.util.Log;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.util.AmazonTransferUtility;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;
import com.salutron.lifetrakwatchapp.web.ServerSyncAsync;
import com.salutron.lifetrakwatchapp.web.ServerSyncAsyncS3Amazon;
import com.salutron.lifetrakwatchapp.web.ServerSyncAsyncTask;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.web.AsyncListener;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONException;

import roboguice.inject.InjectView;

public class ServerSyncActivity extends BaseActivity implements AsyncListener {
	@InjectView(R.id.imgSyncSprite) private ImageView mSyncSprite;
	@InjectView(R.id.tvwDescSubTitle) private TextView mDescSubTitle;
	private final int[] mPreloaders = {
			R.drawable.ll_preloader_cloud_01, R.drawable.ll_preloader_cloud_02,
			R.drawable.ll_preloader_cloud_03, R.drawable.ll_preloader_cloud_04,
			R.drawable.ll_preloader_cloud_05, R.drawable.ll_preloader_cloud_06,
			R.drawable.ll_preloader_cloud_07, R.drawable.ll_preloader_cloud_radar_01,
			R.drawable.ll_preloader_cloud_radar_02, R.drawable.ll_preloader_cloud_radar_03,
			R.drawable.ll_preloader_cloud_radar_04, R.drawable.ll_preloader_cloud_radar_05,
			R.drawable.ll_preloader_cloud_radar_06, R.drawable.ll_preloader_cloud_radar_07,
			R.drawable.ll_preloader_cloud_radar_08, R.drawable.ll_preloader_cloud_radar_09,
			R.drawable.ll_preloader_cloud_radar_10, R.drawable.ll_preloader_cloud_radar_11
	};
	private int mImageIndex;
	private ServerSyncAsync mServerSyncAsync;
	private ServerSyncAsyncS3Amazon mServerSyncAsyncAmazon;
	private ServerSyncAsyncTask mServerSyncAsyncTask;
	private Watch mWatch;
	private final int OPERATION_SYNC_TO_CLOUD = 0x01;
	private final int OPERATION_REFRESH_TOKEN = 0x02;
	private final int OPERATION_BULK_INSERT_S3 = 0x03;
	private final int API_REQUEST_STORE= 0x04;
	private int mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
	private boolean mWatchConnected = false;
	private Thread mSyncThread;
    private AlertDialog alert;

	private boolean mCancelled = false;

	private int retryCounter = 0;
	private int indexListCounter = 0;
	private String uuid;
	private List<StatisticalDataHeader> dataHeaders;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_server_sync);

		System.out.println("Show Cancel ServerSyncActivity");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.cancel);
        alert = new AlertDialog.Builder(this)

                .setTitle(R.string.lifetrak_title)
                .setMessage(R.string.check_network_connection)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                        Intent intent = new Intent(ServerSyncActivity.this, MainActivity.class);
                        intent.putExtra(IS_WATCH_CONNECTED, mWatchConnected);
                        startActivity(intent);
                        finish();
                    }
                })
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (NetworkUtil.getInstance(ServerSyncActivity.this).isNetworkAvailable()) {
                            mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
                            startSyncToServer();
                        } else {

                        }
                    }
                }).create();
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
	public void onResume() {
		super.onResume();
		bindBLEService();
		FlurryAgent.logEvent("Syncing_Page");

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindBLEService();
	}

	private void initializeObjects() {
		mServerSyncAsync = new ServerSyncAsync(this);


		if(getIntent().getExtras() != null) {
			mWatchConnected = getIntent().getExtras().getBoolean(IS_WATCH_CONNECTED, false);
			mWatch = getIntent().getExtras().getParcelable(WATCH);
			if (mWatch != null)
				mWatch.setContext(this);
		}


		mServerSyncAsync.setAsyncListener(this);
		mServerSyncAsyncAmazon = new ServerSyncAsyncS3Amazon(ServerSyncActivity.this);
		mServerSyncAsyncAmazon.setAsyncListener(this);
		mSpriteHandler.sendMessageDelayed(Message.obtain(), 100);

		Date expirationDate = getExpirationDate();

		if(new Date().after(expirationDate)) {
			mCurrentOperation = OPERATION_REFRESH_TOKEN;
			refreshToken();
		} else {
            if (NetworkUtil.getInstance(ServerSyncActivity.this).isNetworkAvailable()){
			    mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
			    startSyncToServer();
            }
            else{
                showNoInternetDialog();
            }
		}
	}

    private void showNoInternetDialog(){
		try{alert.show();}catch (Exception e){}
    }

	private Handler mSpriteHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			animateSprite();
		}
	};

	private void animateSprite() {
		mImageIndex++;

		if(mImageIndex == mPreloaders.length)
			mImageIndex = 0;

		mSyncSprite.setImageResource(mPreloaders[mImageIndex]);
		mSpriteHandler.sendMessageDelayed(Message.obtain(), 100);
	}

	private void startSyncToServer() {
		LifeTrakLogger.info("Sync Started To Server - " + new Date());

		if(mWatch != null) {
			if (mWatch.getModel() == WATCHMODEL_R420 || mWatch.getModel() == WATCHMODEL_R415 ||
					mWatch.getModel() == WATCHMODEL_C410 || mWatch.getModel() == WATCHMODEL_C300
					|| mWatch.getModel() == WATCHMODEL_C300_IOS){
				if (!mCancelled) {
					mSyncThread = new Thread(new Runnable() {
						@Override
						public void run() {
							synchronized (LOCK_OBJECT) {
								indexListCounter = 0;
								retryCounter = 0;
								mCurrentOperation = OPERATION_BULK_INSERT_S3;
								dataHeaders = DataSource.getInstance(ServerSyncActivity.this)
										.getReadOperation()
										.query("watchDataHeader = ? and syncedToCloud = 0", String.valueOf(mWatch.getId()))
										.orderBy("dateStamp", SORT_DESC)
										.getResults(StatisticalDataHeader.class, false);

								uuid = AmazonTransferUtility.generateRandomUUID();
								if (dataHeaders.size() > 0) {
									try {
										if (dataHeaders.size() > 1) {
											runOnUiThread(new Runnable() {
												@Override
												public void run() {
													mDescSubTitle.setText("Uploading Data 1%");
												}
											});
										}
										else{
											runOnUiThread(new Runnable() {
												@Override
												public void run() {
													mDescSubTitle.setText("Uploading Data on Server");
												}
											});
										}
										LifeTrakLogger.info("Sync Data to s3 count " + String.valueOf(indexListCounter) + " OF " + String.valueOf(dataHeaders.size()));
										uploadS3(mWatch, dataHeaders.get(indexListCounter));
									} catch (JSONException e) {
										LifeTrakLogger.info("Error: " + e.getLocalizedMessage());
									}
								}
							}
						}
					});
					mSyncThread.start();
				}

			}
			else {
				mSyncThread = new Thread(new Runnable() {
					public void run() {
						try {
							synchronized (LOCK_OBJECT) {
								if (getLifeTrakApplication().getUserProfile() != null) {
									long profileId = getLifeTrakApplication().getUserProfile().getId();
									mWatch.setProfileId(profileId);
								}


								JSONObject data = new JSONObject();
								data.put("device", mServerSyncAsync.getDevice(mWatch.getMacAddress()));
								data.put("workout", mServerSyncAsync.getAllWorkoutInfos(mWatch.getId(), mPreferenceWrapper.getPreferenceBooleanValue(FROM_IOS), mWatch));
								data.put("sleep", mServerSyncAsync.getAllSleepDatabases(mWatch.getId()));
								data.put("data_header", mServerSyncAsync.getAllDataHeaders(mWatch));
								data.put("device_settings", mServerSyncAsync.getDeviceSettings(mWatch.getId(), getLifeTrakApplication()));
								data.put("user_profile", mServerSyncAsync.getUserProfile(mWatch.getId()));
								data.put("goal", mServerSyncAsync.getAllGoals(mWatch.getId()));
								data.put("sleep_settings", mServerSyncAsync.getSleepSetting(mWatch.getId()));

								if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
									data.put("wakeup_info", mServerSyncAsync.getWakeupInfo(mWatch.getId()));
									data.put("inactive_alert_settings", mServerSyncAsync.getActivityAlertSetting(mWatch.getId()));
									data.put("light_settings", mServerSyncAsync.getLightSetting(mWatch.getId()));
								}

								data.put("workout_header", mServerSyncAsync.getAllWorkoutHeaders(mWatch.getId()));

								String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);

								// LifeTrakLogger.info(" data:" +  data.toString());
								LifeTrakLogger.info(" accesstoken:" + accessToken);
//
								mWatch.setCloudLastSyncDate(new Date());
								mWatch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
								mWatch.update();
//					mServerSyncAsyncTask.execute(getApiUrl() + SYNC_URI);
								mServerSyncAsync.url(API_URL + SYNC_URI).addParam("access_token", accessToken).addParam("data", data.toString()).post();

							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
				mSyncThread.start();
			}
		}
	}

	private void refreshToken() {
		String url = getApiUrl() + REFRESH_TOKEN_URI;

		
		mServerSyncAsyncTask = new ServerSyncAsyncTask();
		mServerSyncAsyncTask.addParam("grant_type", "refresh_token");
		mServerSyncAsyncTask.addParam("refresh_token", mPreferenceWrapper.getPreferenceStringValue(REFRESH_TOKEN));
		mServerSyncAsyncTask.addParam("client_id", getString(R.string.client_id));
		mServerSyncAsyncTask.addParam("client_secret", getString(R.string.client_secret));
		mServerSyncAsyncTask.listener(this);
		mServerSyncAsyncTask.execute(url);
	
	}

	@Override
	public void onAsyncStart() {

	}

	@Override
	public void onAsyncFail(int status,final String message) {
		if (mCurrentOperation == OPERATION_BULK_INSERT_S3){
			retryCounter++;
			if (!mCancelled) {
				mSyncThread = new Thread(new Runnable() {
					@Override
					public void run() {
						synchronized (LOCK_OBJECT) {
							try {
								if (NetworkUtil.getInstance(ServerSyncActivity.this).isNetworkAvailable()) {
									if (retryCounter < 3) {
										LifeTrakLogger.info("Sync Data to s3 count " + String.valueOf(indexListCounter) + " OF " + String.valueOf(dataHeaders.size()) + " Error Count = " + String.valueOf(retryCounter));
										uploadS3(getLifeTrakApplication().getSelectedWatch(), dataHeaders.get(indexListCounter));
									} else {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												LifeTrakLogger.info("Error on sync to cloud : " + message);
												AlertDialog mAlertDialog = new AlertDialog.Builder(ServerSyncActivity.this).setTitle(R.string.lifetrak_title).setMessage(message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface arg0, int arg1) {
														arg0.dismiss();
													}
												}).create();
												mAlertDialog.show();
											}
										});

									}
								} else {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if (isFinishing()) {
												AlertDialog alert = new AlertDialog.Builder(ServerSyncActivity.this)
														.setTitle(R.string.lifetrak_title)
														.setMessage(R.string.check_network_connection)
														.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface arg0, int arg1) {
																Intent intent = new Intent(ServerSyncActivity.this, MainActivity.class);
																intent.putExtra(IS_WATCH_CONNECTED, mWatchConnected);
																startActivity(intent);
																finish();
															}
														})
														.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int which) {
																startSyncToServer();
															}
														}).create();
												alert.show();
											}
											else{
												Intent intent = new Intent(ServerSyncActivity.this, MainActivity.class);
												intent.putExtra(IS_WATCH_CONNECTED, mWatchConnected);
												startActivity(intent);
												finish();
											}
										}
									});

								}
							} catch (JSONException e) {

							}
						}
					}
				});
				mSyncThread.start();
			}
		}
		else {
			LifeTrakLogger.info("server sync async: " + message);
			if (isFinishing()){
				AlertDialog alert = new AlertDialog.Builder(this)
						.setTitle(R.string.lifetrak_title)
						.setMessage(R.string.server_error_unknown)
						.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Intent intent = new Intent(ServerSyncActivity.this, MainActivity.class);
								intent.putExtra(IS_WATCH_CONNECTED, mWatchConnected);
								startActivity(intent);
								finish();
							}
						})
						.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								startSyncToServer();
							}
						}).create();
				try {
					alert.show();
				} catch (Exception e) {

				}
			}
			else{
				Intent intent = new Intent(ServerSyncActivity.this, MainActivity.class);
				intent.putExtra(IS_WATCH_CONNECTED, mWatchConnected);
				startActivity(intent);
				finish();
			}



			LifeTrakLogger.error("Sync End Fail To Server - " + new Date());
		}
	}

	@Override
	public void onAsyncSuccess(JSONObject result) {
		LifeTrakLogger.info("sync result :" + result.toString());
		final String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
		Intent intent = new Intent();
		if(mWatch != null) {
			try {
				switch(mCurrentOperation) {
				case OPERATION_REFRESH_TOKEN:
					mPreferenceWrapper.setPreferenceStringValue(ACCESS_TOKEN, result.getString("access_token"))
					.setPreferenceStringValue(REFRESH_TOKEN, result.getString("refresh_token"))
					.setPreferenceLongValue(EXPIRATION_DATE, result.getLong("expires"))
					.synchronize();

					String email = mPreferenceWrapper.getPreferenceStringValue(EMAIL);

					List<UserProfile> profiles = DataSource.getInstance(ServerSyncActivity.this)
							.getReadOperation()
							.query("email = ?", email)
							.getResults(UserProfile.class);

					if(profiles.size() > 0) {
						UserProfile profile = profiles.get(0);

						List<Watch> watches = DataSource.getInstance(ServerSyncActivity.this)
								.getReadOperation()
								.query("accessToken = ?", profile.getAccessToken())
								.getResults(Watch.class);

						profile.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
						profile.update();

						for(Watch watch : watches) {
							watch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
							watch.update();
						}
					}
					if (NetworkUtil.getInstance(ServerSyncActivity.this).isNetworkAvailable()){
					    mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
					    startSyncToServer();
					}
                    else{
                        showNoInternetDialog();
                    }
					break;
				case OPERATION_SYNC_TO_CLOUD:
					int status = result.getInt("status");

                    LifeTrakLogger.info("Sync End To Server - " + new Date());

					switch(status) {
					case 202:
						mServerSyncAsyncTask = new ServerSyncAsyncTask();
						mServerSyncAsyncTask.listener(ServerSyncActivity.this);
						mServerSyncAsyncTask.addParam("mac_address", mWatch.getMacAddress());
						mServerSyncAsyncTask.addParam("access_token", accessToken);
						mServerSyncAsyncTask.execute(getApiUrl() + STORE_URI);
						
						break;
					case 200:
						updateAllDataHeaders();
						intent.setClass(this, MainActivity.class);
						intent.putExtra(WATCH, mWatch);
						intent.putExtra(IS_WATCH_CONNECTED, mWatchConnected);
						startActivity(intent);
						finish();
						break;
					case 400:
						updateAllDataHeaders();
						intent.setClass(this, MainActivity.class);
						intent.putExtra(IS_WATCH_CONNECTED, mWatchConnected);
						startActivity(intent);
						finish();
						break;
					}
					break;
					case OPERATION_BULK_INSERT_S3:

						if (!mCancelled){
						indexListCounter ++;
						retryCounter = 0;
						final Watch watch = getLifeTrakApplication().getSelectedWatch();
						if (dataHeaders.size()  ==  indexListCounter){
							mSyncThread = new Thread(new Runnable() {
								@Override
								public void run() {
									synchronized (LOCK_OBJECT) {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												mDescSubTitle.setText(getString(R.string.syncing_to_server));
											}
										});
										mCurrentOperation = API_REQUEST_STORE;
										mServerSyncAsyncTask = new ServerSyncAsyncTask();
										mServerSyncAsyncTask.listener(ServerSyncActivity.this);
										mServerSyncAsyncTask.addParam("access_token", accessToken);
										mServerSyncAsyncTask.addParam("uuid", uuid);
										mServerSyncAsyncTask.execute(getApiUrl() + STORE_URI_V2);
									}
								}
							});
							mSyncThread.start();

						}
						else {
							mSyncThread = new Thread(new Runnable() {
								@Override
								public void run() {
									synchronized (LOCK_OBJECT) {
										try {
											runOnUiThread(new Runnable() {
												@Override
												public void run() {
													Double per = ((double) indexListCounter / dataHeaders.size()) * 100;
													mDescSubTitle.setText("Uploading Data " + String.valueOf(Math.round(per)) + "%");
												}
											});
											LifeTrakLogger.info("Sync Data to s3 count " + String.valueOf(indexListCounter) + " OF " + String.valueOf(dataHeaders.size()));
											uploadS3(watch, dataHeaders.get(indexListCounter));
										} catch (JSONException e) {

										}
									}
								}
							});
							mSyncThread.start();
						}
						}
						break;
					case API_REQUEST_STORE:
						if (!mCancelled) {
							updateAllDataHeaders();
							intent.setClass(this, MainActivity.class);
							intent.putExtra(WATCH, mWatch);
							intent.putExtra(IS_WATCH_CONNECTED, mWatchConnected);
							startActivity(intent);
							finish();
						}
						break;
				}
			} catch(JSONException e) {
				e.printStackTrace();
			}
		} else {
			intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	private void updateAllDataHeaders() {
		Calendar calendarNow = Calendar.getInstance();
		calendarNow.setTime(new Date());

		int day = calendarNow.get(Calendar.DAY_OF_MONTH);
		int month = calendarNow.get(Calendar.MONTH) + 1;
		int year = calendarNow.get(Calendar.YEAR) - 1900;
		if (getLifeTrakApplication().getSelectedWatch() != null) {
			List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(this)
					.getReadOperation()
					.query("watchDataHeader = ? and syncedToCloud = 0", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
					.getResults(StatisticalDataHeader.class);

			for (StatisticalDataHeader dataHeader : dataHeaders) {
				if (!(dataHeader.getDateStampDay() == day && dataHeader.getDateStampMonth() == month && dataHeader.getDateStampYear() == year)) {
					dataHeader.setContext(this);
					dataHeader.setWatch(getLifeTrakApplication().getSelectedWatch());
					dataHeader.setSyncedToCloud(true);
					dataHeader.update();
				}
			}

			List<SleepDatabase> sleepDatabases = DataSource.getInstance(this)
					.getReadOperation()
					.query("watchSleepDatabase = ? and syncedToCloud = 0"
							, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
					.getResults(SleepDatabase.class);

			for (SleepDatabase sleepDatabase : sleepDatabases) {
				sleepDatabase.setContext(this);
				sleepDatabase.setWatch(getLifeTrakApplication().getSelectedWatch());
				sleepDatabase.setSyncedToCloud(true);
				sleepDatabase.update();
			}

			List<WorkoutHeader> workoutHeaders = DataSource.getInstance(this)
					.getReadOperation()
					.query("watchWorkoutHeader = ? and syncedToCloud = 0", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
					.getResults(WorkoutHeader.class);

			for (WorkoutHeader workoutHeader : workoutHeaders) {
				workoutHeader.setContext(this);
				workoutHeader.setWatch(getLifeTrakApplication().getSelectedWatch());
				workoutHeader.setSyncedToCloud(true);
				workoutHeader.update();
			}

			List<WorkoutInfo> workoutInfos = DataSource.getInstance(this)
					.getReadOperation()
					.query("watchWorkoutInfo = ? and syncedToCloud = 0", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
					.getResults(WorkoutInfo.class);

			for (WorkoutInfo workoutInfo : workoutInfos) {
				workoutInfo.setContext(this);
				workoutInfo.setWatch(getLifeTrakApplication().getSelectedWatch());
				workoutInfo.setSyncedToCloud(true);
				workoutInfo.update();
			}

		}
		else{
			List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(this)
					.getReadOperation()
					.query("watchDataHeader = ? and syncedToCloud = 0", String.valueOf(mWatch.getId()))
					.getResults(StatisticalDataHeader.class);

			for (StatisticalDataHeader dataHeader : dataHeaders) {
				if (!(dataHeader.getDateStampDay() == day && dataHeader.getDateStampMonth() == month && dataHeader.getDateStampYear() == year)) {
					dataHeader.setContext(this);
					dataHeader.setWatch(mWatch);
					dataHeader.setSyncedToCloud(true);
					dataHeader.update();
				}
			}
			List<SleepDatabase> sleepDatabases = DataSource.getInstance(this)
					.getReadOperation()
					.query("watchSleepDatabase = ? and syncedToCloud = 0"
							, String.valueOf(mWatch.getId()))
					.getResults(SleepDatabase.class);

			for (SleepDatabase sleepDatabase : sleepDatabases) {
				sleepDatabase.setContext(this);
				sleepDatabase.setWatch(mWatch);
				sleepDatabase.setSyncedToCloud(true);
				sleepDatabase.update();
			}

			List<WorkoutHeader> workoutHeaders = DataSource.getInstance(this)
					.getReadOperation()
					.query("watchWorkoutHeader = ? and syncedToCloud = 0", String.valueOf(mWatch.getId()))
					.getResults(WorkoutHeader.class);

			for (WorkoutHeader workoutHeader : workoutHeaders) {
				workoutHeader.setContext(this);
				workoutHeader.setWatch(mWatch);
				workoutHeader.setSyncedToCloud(true);
				workoutHeader.update();
			}

			List<WorkoutInfo> workoutInfos = DataSource.getInstance(this)
					.getReadOperation()
					.query("watchWorkoutInfo = ? and syncedToCloud = 0", String.valueOf(mWatch.getId()))
					.getResults(WorkoutInfo.class);

			for (WorkoutInfo workoutInfo : workoutInfos) {
				workoutInfo.setContext(this);
				workoutInfo.setWatch(mWatch);
				workoutInfo.setSyncedToCloud(true);
				workoutInfo.update();
			}

			getLifeTrakApplication().setSelectedWatch(mWatch);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//TODO insert code to Cancel ServerSync
			mCancelled = true;
			new Thread(new Runnable() {
				public void run() {
					mServerSyncAsync.cancel();
					try {
						mServerSyncAsyncTask.cancel(true);
					}
					catch (Exception e){
						LifeTrakLogger.info("mServerSyncAsyncTask error on cancel:" + e.getLocalizedMessage());
					}
					if (mSyncThread != null && mSyncThread.isAlive())
						mSyncThread.interrupt();
				}
			}).start();
			Intent intent = new Intent(ServerSyncActivity.this, MainActivity.class);
			intent.putExtra(IS_WATCH_CONNECTED, mWatchConnected);
			startActivity(intent);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void uploadS3(Watch watch, StatisticalDataHeader dataHeader) throws  JSONException{
		if (watch != null) {
			JSONObject data = new JSONObject();
			data.put("device", mServerSyncAsyncAmazon.getDevice(watch.getMacAddress(), watch));
			data.put("workout", mServerSyncAsyncAmazon
					.getAllWorkoutInfos(watch.getId(), mPreferenceWrapper.getPreferenceBooleanValue(FROM_IOS), watch, dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
			data.put("sleep", mServerSyncAsyncAmazon
					.getAllSleepDatabases(watch.getId(), dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
			data.put("data_header", mServerSyncAsyncAmazon
					.getAllDataHeaders(watch, dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
			data.put("device_settings", mServerSyncAsyncAmazon.getDeviceSettings(watch.getId(), getLifeTrakApplication()));
			data.put("user_profile", mServerSyncAsyncAmazon.getUserProfile(watch.getId()));
			data.put("goal", mServerSyncAsyncAmazon.getAllGoals(watch.getId(), dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
			data.put("sleep_settings", mServerSyncAsyncAmazon.getSleepSetting(watch.getId()));
			data.put("wakeup_info", mServerSyncAsyncAmazon.getWakeupInfo(watch.getId()));

			if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
				data.put("wakeup_info", mServerSyncAsyncAmazon.getWakeupInfo(watch.getId()));
				data.put("inactive_alert_settings", mServerSyncAsyncAmazon.getActivityAlertSetting(watch.getId()));
				data.put("light_settings", mServerSyncAsyncAmazon.getLightSetting(watch.getId()));
			}
			if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420) {
				data.put("workout_header", mServerSyncAsyncAmazon.getAllWorkoutHeaders(watch.getId(), dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
			}

			AmazonTransferUtility
					.getInstance(ServerSyncActivity.this)
					.listener(this)
					.setUUID(uuid)
					.uploadFileToAmazonS3(data.toString(), dataHeader.getDateStamp());
		}
	}

}
