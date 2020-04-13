package com.salutron.lifetrakwatchapp;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.content.Intent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ImageView;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.actionbarsherlock.view.MenuItem;
import com.amazonaws.services.s3.model.S3Object;
import com.flurry.android.FlurryAgent;
import com.salutron.blesdk.SALTimeDate;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.ActivityAlertSetting;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.NightLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.Notification;
import com.salutron.lifetrakwatchapp.model.WakeupSetting;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.SleepSetting;
import com.salutron.lifetrakwatchapp.model.CalibrationData;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.WorkoutSettings;
import com.salutron.lifetrakwatchapp.util.AmazonTransferUtility;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;
import com.salutron.lifetrakwatchapp.web.AsyncListener;
import com.salutron.lifetrakwatchapp.web.ServerRestoreAsync;
import com.salutron.lifetrakwatchapp.web.ServerSyncAsyncTask;
import com.salutron.lifetrakwatchapp.web.TwoWaySyncAsyncTask;

import roboguice.inject.InjectView;

public class ServerRestoreActivity extends BaseActivity implements AsyncListener {
	@InjectView(R.id.tvwDescSubTitle) private TextView mDescSubTitle;
	@InjectView(R.id.imgSyncSprite) private ImageView mSyncSprite;
	private final int[] mPreloaders = {
			R.drawable.ll_preloader_cloud_radar_02, R.drawable.ll_preloader_cloud_radar_03,
			R.drawable.ll_preloader_cloud_radar_04, R.drawable.ll_preloader_cloud_radar_05,
			R.drawable.ll_preloader_cloud_radar_06, R.drawable.ll_preloader_cloud_radar_07,
			R.drawable.ll_preloader_cloud_radar_08, R.drawable.ll_preloader_cloud_radar_09,
			R.drawable.ll_preloader_cloud_radar_10, R.drawable.ll_preloader_cloud_radar_11
	};
	private int mImageIndex;
	private Watch mWatch;
	private ServerRestoreAsync<JSONObject> mServerRestoreAsync;
	private final int OPERATION_RESTORE_FROM_CLOUD = 0x01;
	private final int OPERATION_REFRESH_TOKEN = 0x02;
	private final int OPERATION_RESTORE_FROM_CLOUD_S3 = 0x03;
	private final  int OPERATION_RESTORE_FROM_CLOUD_S3_PARTS_DOWNLOAD = 0x04;
	private int mCurrentOperation = OPERATION_RESTORE_FROM_CLOUD;
	private Thread mRestoreThread;

	private int retryCounter = 0;
	private int indexListCounter = 0;
	private String uuid;
	private String bucketName;
	private JSONArray fileNamesArray;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_server_sync);
		
		System.out.println("Show Cancel ServerRestoreActivity");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.cancel);
		
		initializeObjects();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		new Thread(new Runnable() {
			public void run() {
				mServerRestoreAsync.cancel();
				
				if (mRestoreThread != null && mRestoreThread.isAlive())
					mRestoreThread.interrupt();
			}
		}).start();
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
	
	private void initializeObjects() {
		if(getIntent().getExtras() != null)
			mWatch = getIntent().getExtras().getParcelable(WATCH);
		
		mDescSubTitle.setText(R.string.restoring_from_server);
		mSpriteHandler.sendMessageDelayed(Message.obtain(), 100);
		mServerRestoreAsync = new ServerRestoreAsync<JSONObject>(this);
		mServerRestoreAsync.setAsyncListener(this);
		
		Date expirationDate = getExpirationDate();
		Date now = new Date();
		
		if(now.after(expirationDate)) {
			mCurrentOperation = OPERATION_REFRESH_TOKEN;
			refreshToken();
		} else {

			startRestoreFromServer();
		}
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
	
	private void refreshToken() {
		String url = getApiUrl() + REFRESH_TOKEN_URI;
		
		mServerRestoreAsync.url(url)
							.addParam("grant_type", "refresh_token")
							.addParam("refresh_token", mPreferenceWrapper.getPreferenceStringValue(REFRESH_TOKEN))
							.addParam("client_id", getString(R.string.client_id))
							.addParam("client_secret", getString(R.string.client_secret))
							.post();
	}
	
	private void startRestoreFromServer() {
		if(mWatch != null) {
			new Thread(new Runnable() {
				public void run() {
					if (mWatch.getModel() == WATCHMODEL_R420 || mWatch.getModel() == WATCHMODEL_R415 ||
							mWatch.getModel() == WATCHMODEL_C410 || mWatch.getModel() == WATCHMODEL_C300
							 || mWatch.getModel() == WATCHMODEL_C300_IOS){

						mCurrentOperation = OPERATION_RESTORE_FROM_CLOUD_S3;

//						mServerRestoreAsync.url(getApiUrl() + RESTORE_URI_V2)
//								.addParam("access_token", mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN))
//								.addParam("mac_address", mWatch.getMacAddress())
//								.get();
						TwoWaySyncAsyncTask mServerSyncAsyncTask = new TwoWaySyncAsyncTask();
						mServerSyncAsyncTask.listener(ServerRestoreActivity.this);
						mServerSyncAsyncTask.addParam("access_token", mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
						mServerSyncAsyncTask.addParam("mac_address", mWatch.getMacAddress());
						mServerSyncAsyncTask.execute(getApiUrl() + RESTORE_URI_V2);
//						runOnUiThread(new Runnable() {
//							@Override
//							public void run() {
//								mDescSubTitle.setText("Getting Date Count");
//							}
//						});
					}
					else {
						mCurrentOperation = OPERATION_RESTORE_FROM_CLOUD;
						mServerRestoreAsync.url(getApiUrl() + RESTORE_URI)
								.addParam("access_token", mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN))
								.addParam("mac_address", mWatch.getMacAddress())
								.get();
					}
				}
			}).start();
		}
	}

	@Override
	public void onAsyncStart() {
		
	}

	@Override
	public void onAsyncFail(int status, String message) {
		if (mCurrentOperation == OPERATION_RESTORE_FROM_CLOUD_S3_PARTS_DOWNLOAD){
			LifeTrakLogger.info("server restore: " + message);
			retryCounter ++;
			try{
				if (retryCounter < 3)
					downloadS3Files();
				else{
					AlertDialog mAlertDialog = new AlertDialog.Builder(ServerRestoreActivity.this).setTitle(R.string.lifetrak_title).setMessage(message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							finish();
						}
					}).create();
					mAlertDialog.show();
				}
			}catch (JSONException e){

			}
		}
		else {
			LifeTrakLogger.info("server restore: " + message);
			finish();
		}
	}

	@Override
	public void onAsyncSuccess(final JSONObject result) {
		switch(mCurrentOperation) {
		case OPERATION_REFRESH_TOKEN:
			try {
				mPreferenceWrapper.setPreferenceStringValue(ACCESS_TOKEN, result.getString("access_token"))
									.setPreferenceStringValue(REFRESH_TOKEN, result.getString("refresh_token"))
									.setPreferenceLongValue(EXPIRATION_DATE, result.getLong("expires"))
									.synchronize();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			
			startRestoreFromServer();
			mCurrentOperation = OPERATION_RESTORE_FROM_CLOUD;
			break;
		case OPERATION_RESTORE_FROM_CLOUD:
			mRestoreThread = new Thread(new Runnable() {
				public void run() {
					try {
						JSONObject objectResult = result.getJSONObject("result");
						JSONArray arrayDataHeaders = objectResult.getJSONArray("data_header");
						JSONArray arrayWorkoutInfos = new JSONArray();
						
						if(objectResult.has("workout") && !objectResult.isNull("workout"))
							arrayWorkoutInfos = objectResult.getJSONArray("workout");
						
						
						JSONArray arraySleepDatabases = new JSONArray();
						
						if(objectResult.has("sleep") && !objectResult.isNull("sleep"))
							arraySleepDatabases = objectResult.getJSONArray("sleep");
						
						JSONObject objectUserProfile = objectResult.getJSONObject("user_profile");
						JSONArray arrayGoals = objectResult.getJSONArray("goal");
						
						JSONObject objectSleepSetting = new JSONObject();
						
						if(objectResult.has("sleep_settings") && !objectResult.isNull("sleep_settings"))
							objectSleepSetting = objectResult.getJSONObject("sleep_settings");
						
						JSONObject objectWakeupSetting = new JSONObject();
						
						if (objectResult.has("wakeup_info") && !objectResult.isNull("wakeup_info"))
							objectWakeupSetting = objectResult.getJSONObject("wakeup_info");
						
						JSONObject objectInactiveAlertSetting = new JSONObject();
						
						if (objectResult.has("inactive_alert_settings") && !objectResult.isNull("inactive_alert_settings"))
							objectInactiveAlertSetting = objectResult.getJSONObject("inactive_alert_settings");
						
						JSONObject objectDayLightAlertSetting = null;
						JSONObject objectNightLightAlertSetting = null;
						
						if (objectResult.has("light_settings") && !objectResult.isNull("light_settings")) {
							
							JSONArray arrayLightSetting = objectResult.getJSONArray("light_settings");
							
							for (int i=0;i<arrayLightSetting.length();i++) {
								JSONObject objectLightSetting = arrayLightSetting.getJSONObject(i);
								
								if (objectLightSetting.getString("settings").equals("day")) {
									objectDayLightAlertSetting = objectLightSetting;
								} else if(objectLightSetting.getString("settings").equals("night")) {
									objectNightLightAlertSetting = objectLightSetting;
								}
							}
							
						}
						
						JSONObject objectDeviceSettings = objectResult.getJSONObject("device_settings");
						JSONArray arrayWorkoutHeaders = objectResult.getJSONArray("workout_header");

						List<StatisticalDataHeader> dataHeaders = mServerRestoreAsync.getStatisticalDataHeaders(arrayDataHeaders, mWatch);
						List<WorkoutInfo> workoutInfos = mServerRestoreAsync.getWorkoutInfos(arrayWorkoutInfos, mWatch, mPreferenceWrapper.getPreferenceBooleanValue(FROM_IOS));
						List<SleepDatabase> sleepDatabases = mServerRestoreAsync.getSleepDatabases(arraySleepDatabases, mWatch);
						UserProfile userProfile = mServerRestoreAsync.getUserProfile(objectUserProfile, mWatch);
						List<Goal> goals = mServerRestoreAsync.getGoals(arrayGoals, mWatch);
						SleepSetting sleepSetting = new SleepSetting();
						
						if(!objectResult.isNull("sleep_settings"))
							sleepSetting = mServerRestoreAsync.getSleepSetting(objectSleepSetting, mWatch);
						
						WakeupSetting wakeupSetting = new WakeupSetting();
						
						if (objectResult.has("wakeup_info") && !objectResult.isNull("wakeup_info"))
							wakeupSetting = mServerRestoreAsync.getWakeupSetting(objectWakeupSetting, mWatch);
						
						ActivityAlertSetting activityAlertSetting = new ActivityAlertSetting();
						
						if (objectResult.has("inactive_alert_settings") && !objectResult.isNull("inactive_alert_settings"))
							activityAlertSetting = mServerRestoreAsync.getActivityAlertSetting(objectInactiveAlertSetting, mWatch);
						
						DayLightDetectSetting dayLightDetectSetting = new DayLightDetectSetting();
						
						if (objectDayLightAlertSetting != null)
							dayLightDetectSetting = mServerRestoreAsync.getDayLightDetectSetting(objectDayLightAlertSetting, mWatch);
						
						NightLightDetectSetting nightLightDetectSetting = new NightLightDetectSetting();
						
						if (objectNightLightAlertSetting != null)
							nightLightDetectSetting = mServerRestoreAsync.getNightLightDetectSetting(objectNightLightAlertSetting, mWatch);
						
						CalibrationData calibrationData = mServerRestoreAsync.getCalibrationData(objectDeviceSettings, mWatch);
						TimeDate timeDate = mServerRestoreAsync.getTimeDate(objectDeviceSettings, mWatch);

						Notification notification = mServerRestoreAsync.getNotification(objectDeviceSettings, mWatch);

						List<WorkoutHeader> workoutHeaders = mServerRestoreAsync.getWorkoutHeaders(arrayWorkoutHeaders, mWatch);

						WorkoutSettings workoutSettings = mServerRestoreAsync.getWorkOutSettings(objectDeviceSettings, mWatch);
						
						String firstname = mPreferenceWrapper.getPreferenceStringValue(FIRST_NAME);
						String lastname = mPreferenceWrapper.getPreferenceStringValue(LAST_NAME);
						String email = mPreferenceWrapper.getPreferenceStringValue(EMAIL);
						
						userProfile.setFirstname(firstname);
						userProfile.setLastname(lastname);
						userProfile.setEmail(email);
						userProfile.setProfileImageWeb(mPreferenceWrapper.getPreferenceStringValue(PROFILE_IMG));
						userProfile.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
						userProfile.update();
						
						mWatch.setProfileId(userProfile.getId());
						mWatch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
						mWatch.update();
						
						DataSource.getInstance(ServerRestoreActivity.this)
									.getWriteOperation()
									.open()
									.beginTransaction()
									.insert(dataHeaders)
									.insert(workoutInfos)
									.insert(userProfile)
									.insert(goals)
									.insert(sleepSetting)
									.insert(wakeupSetting)
									.insert(activityAlertSetting)
									.insert(dayLightDetectSetting)
									.insert(nightLightDetectSetting)
									.insert(calibrationData)
									.insert(timeDate)
									.insert(notification)
									.insert(workoutHeaders)
									.insert(workoutSettings)
									.endTransaction()
									.close();


						
						getLifeTrakApplication().setUserProfile(userProfile);
						getLifeTrakApplication().setTimeDate(timeDate);


						if (objectDeviceSettings.get("watch_face").toString().equals("simple")){
							getLifeTrakApplication().getTimeDate().setDisplaySize(DISPLAY_FORMAT_BIG_DIGIT);
						}
						else{
							getLifeTrakApplication().getTimeDate().setDisplaySize(DISPLAY_FORMAT_SMALL_DIGIT);
						}

                        if (objectDeviceSettings.getInt("noti_simple_alert") == 1){
                            mPreferenceWrapper.setPreferenceBooleanValue(NOTIFICATION_ENABLED, true).synchronize();
                        }
                        else{
                            mPreferenceWrapper.setPreferenceBooleanValue(NOTIFICATION_ENABLED, false).synchronize();
                        }

                        if (objectDeviceSettings.get("hour_format").toString().equals("12")){
                            getLifeTrakApplication().getTimeDate().setHourFormat(SALTimeDate.FORMAT_12HOUR);
                        }
                        else{
                            getLifeTrakApplication().getTimeDate().setHourFormat(SALTimeDate.FORMAT_24HOUR);
                        }



						runOnUiThread(new Runnable() {
							public void run() {
								Intent intent = new Intent(ServerRestoreActivity.this, MainActivity.class);
								startActivity(intent);
								finish();
							}
						});
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			});
			mRestoreThread.start();
			break;
			case OPERATION_RESTORE_FROM_CLOUD_S3:
				try{
					JSONObject objectResult = result.getJSONObject("result");
					bucketName = objectResult.getString("bucket");
					uuid = objectResult.getString("uuid");
					fileNamesArray = objectResult.getJSONArray("files");
					if (fileNamesArray.length() > 0){
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								//mDescSubTitle.setText("Getting All Files [ 1/"+ String.valueOf(fileNamesArray.length()) + " ]");
								mDescSubTitle.setText("Downloading Data 1%");
							}
						});
						mCurrentOperation = OPERATION_RESTORE_FROM_CLOUD_S3_PARTS_DOWNLOAD;
						downloadS3Files();
					}
					else{
						AlertDialog mAlertDialog = new AlertDialog.Builder(ServerRestoreActivity.this)
								.setTitle(R.string.lifetrak_title)
								.setMessage("It seems we cannot find your data on our server. Please contact Lifetrak Helpdesk for more information").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
								finish();
							}
						}).create();
						mAlertDialog.show();
					}
				}
				catch (JSONException e){
					LifeTrakLogger.info("Error" + e.getLocalizedMessage());
				}
				break;


			case OPERATION_RESTORE_FROM_CLOUD_S3_PARTS_DOWNLOAD:
				indexListCounter++;
				if (fileNamesArray.length() > indexListCounter){
					try {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								//mDescSubTitle.setText("Getting All Files [ " + String.valueOf(indexListCounter + 1)+"/"+ String.valueOf(fileNamesArray.length()) + " ]");
								Double per = ((double)indexListCounter/fileNamesArray.length()) * 100;
								mDescSubTitle.setText("Downloading Data " + String.valueOf(Math.round(per)) + "%");
							}
						});
						downloadS3Files();
					}
					catch(JSONException e){

					}
				}
				else{
					//continue saving
					new Thread(new Runnable() {
						public void run() {
							try {
								LifeTrakLogger.info("Saving offline of files");
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										mDescSubTitle.setText("Saving All Files");
									}
								});
								for (int x = 0; x < fileNamesArray.length() ; x++) {
									try {
										File tempFile = AmazonTransferUtility.getInstance(ServerRestoreActivity.this)
												.getFile(fileNamesArray.getString(x));
										FileInputStream stream = new FileInputStream(tempFile);
										String jsonStr = null;

										FileChannel fc = stream.getChannel();
										MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
										jsonStr = Charset.defaultCharset().decode(bb).toString();
										stream.close();
										JSONObject objectResult = new JSONObject(jsonStr);
										JSONObject arrayDataHeaders = objectResult.getJSONObject("data_header");
										JSONArray arrayWorkoutInfos = new JSONArray();

										if (objectResult.has("workout") && !objectResult.isNull("workout"))
											arrayWorkoutInfos = objectResult.getJSONArray("workout");


										JSONArray arraySleepDatabases = new JSONArray();

										if (objectResult.has("sleep") && !objectResult.isNull("sleep"))
											arraySleepDatabases = objectResult.getJSONArray("sleep");

										JSONObject objectUserProfile = objectResult.getJSONObject("user_profile");
										JSONArray arrayGoals = objectResult.getJSONArray("goal");

										JSONObject objectSleepSetting = new JSONObject();

										if (objectResult.has("sleep_settings") && !objectResult.isNull("sleep_settings"))
											objectSleepSetting = objectResult.getJSONObject("sleep_settings");

										JSONObject objectWakeupSetting = new JSONObject();

										if (objectResult.has("wakeup_info") && !objectResult.isNull("wakeup_info"))
											objectWakeupSetting = objectResult.getJSONObject("wakeup_info");

										JSONObject objectInactiveAlertSetting = new JSONObject();

										if (objectResult.has("inactive_alert_settings") && !objectResult.isNull("inactive_alert_settings"))
											objectInactiveAlertSetting = objectResult.getJSONObject("inactive_alert_settings");

										JSONObject objectDayLightAlertSetting = null;
										JSONObject objectNightLightAlertSetting = null;

										if (objectResult.has("light_settings") && !objectResult.isNull("light_settings")) {

											JSONArray arrayLightSetting = objectResult.getJSONArray("light_settings");

											for (int i = 0; i < arrayLightSetting.length(); i++) {
												JSONObject objectLightSetting = arrayLightSetting.getJSONObject(i);

												if (objectLightSetting.getString("settings").equals("day")) {
													objectDayLightAlertSetting = objectLightSetting;
												} else if (objectLightSetting.getString("settings").equals("night")) {
													objectNightLightAlertSetting = objectLightSetting;
												}
											}

										}

										JSONObject objectDeviceSettings = objectResult.getJSONObject("device_settings");
										JSONArray arrayWorkoutHeaders = objectResult.getJSONArray("workout_header");

										StatisticalDataHeader dataHeaders = mServerRestoreAsync.getStatisticalDataHeaders(arrayDataHeaders, mWatch);
										List<WorkoutInfo> workoutInfos = mServerRestoreAsync.getWorkoutInfos(arrayWorkoutInfos, mWatch, mPreferenceWrapper.getPreferenceBooleanValue(FROM_IOS));
										List<SleepDatabase> sleepDatabases = mServerRestoreAsync.getSleepDatabases(arraySleepDatabases, mWatch);
										UserProfile userProfile = mServerRestoreAsync.getUserProfile(objectUserProfile, mWatch);
										List<Goal> goals = mServerRestoreAsync.getGoals(arrayGoals, mWatch);
										SleepSetting sleepSetting = new SleepSetting();

										if (!objectResult.isNull("sleep_settings"))
											sleepSetting = mServerRestoreAsync.getSleepSetting(objectSleepSetting, mWatch);

										WakeupSetting wakeupSetting = new WakeupSetting();

										if (objectResult.has("wakeup_info") && !objectResult.isNull("wakeup_info"))
											wakeupSetting = mServerRestoreAsync.getWakeupSetting(objectWakeupSetting, mWatch);

										ActivityAlertSetting activityAlertSetting = new ActivityAlertSetting();

										if (objectResult.has("inactive_alert_settings") && !objectResult.isNull("inactive_alert_settings"))
											activityAlertSetting = mServerRestoreAsync.getActivityAlertSetting(objectInactiveAlertSetting, mWatch);

										DayLightDetectSetting dayLightDetectSetting = new DayLightDetectSetting();

										if (objectDayLightAlertSetting != null)
											dayLightDetectSetting = mServerRestoreAsync.getDayLightDetectSetting(objectDayLightAlertSetting, mWatch);

										NightLightDetectSetting nightLightDetectSetting = new NightLightDetectSetting();

										if (objectNightLightAlertSetting != null)
											nightLightDetectSetting = mServerRestoreAsync.getNightLightDetectSetting(objectNightLightAlertSetting, mWatch);

										CalibrationData calibrationData = mServerRestoreAsync.getCalibrationData(objectDeviceSettings, mWatch);
										TimeDate timeDate = mServerRestoreAsync.getTimeDate(objectDeviceSettings, mWatch);

										Notification notification = mServerRestoreAsync.getNotification(objectDeviceSettings, mWatch);

										List<WorkoutHeader> workoutHeaders = mServerRestoreAsync.getWorkoutHeaders(arrayWorkoutHeaders, mWatch);

										WorkoutSettings workoutSettings = mServerRestoreAsync.getWorkOutSettings(objectDeviceSettings, mWatch);

										String firstname = mPreferenceWrapper.getPreferenceStringValue(FIRST_NAME);
										String lastname = mPreferenceWrapper.getPreferenceStringValue(LAST_NAME);
										String email = mPreferenceWrapper.getPreferenceStringValue(EMAIL);

										userProfile.setFirstname(firstname);
										userProfile.setLastname(lastname);
										userProfile.setEmail(email);
										userProfile.setProfileImageWeb(mPreferenceWrapper.getPreferenceStringValue(PROFILE_IMG));
										userProfile.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
										userProfile.update();

										mWatch.setProfileId(userProfile.getId());
										mWatch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
										mWatch.update();

										DataSource.getInstance(ServerRestoreActivity.this)
												.getWriteOperation()
												.open()
												.beginTransaction()
												.insert(dataHeaders)
												.insert(workoutInfos)
												.insert(userProfile)
												.insert(goals)
												.insert(sleepSetting)
												.insert(wakeupSetting)
												.insert(activityAlertSetting)
												.insert(dayLightDetectSetting)
												.insert(nightLightDetectSetting)
												.insert(calibrationData)
												.insert(timeDate)
												.insert(notification)
												.insert(workoutHeaders)
												.insert(workoutSettings)
												.endTransaction()
												.close();

//										List<UserProfile> userProfiles = DataSource
//												.getInstance(ServerRestoreActivity.this)
//												.getReadOperation()
//												.query("accessToken = ? and email = ? and watchUserProfile = ?",
//														String.valueOf(userProfile.getAccessToken()), String.valueOf(userProfile.getEmail()), String.valueOf(userProfile.getWatch()))
//												.getResults(UserProfile.class);
//
//										if (userProfiles.size() == 0){
//											DataSource.getInstance(ServerRestoreActivity.this)
//													.getWriteOperation()
//													.open()
//													.beginTransaction()
//													.insert(userProfile)
//													.endTransaction()
//													.close();
//										}


										for (int i =0; i < sleepDatabases.size(); i++){
											SleepDatabase sleepDatabase = sleepDatabases.get(i);

											List<SleepDatabase> sleepDb = DataSource
													.getInstance(ServerRestoreActivity.this)
													.getReadOperation()
													.query("watchSleepDatabase = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and hourSleepStart = ? and hourSleepEnd = ? and minuteSleepEnd = ? and minuteSleepStart = ? ",
															String.valueOf(mWatch.getId()), String.valueOf(sleepDatabase.getDateStampDay()), String.valueOf(sleepDatabase.getDateStampMonth()), String.valueOf(sleepDatabase.getDateStampYear()),
															String.valueOf(sleepDatabase.getHourSleepStart()), String.valueOf(sleepDatabase.getHourSleepEnd()), String.valueOf(sleepDatabase.getMinuteSleepEnd()), String.valueOf(sleepDatabase.getMinuteSleepStart())
													).getResults(SleepDatabase.class);

											if (sleepDb.size() == 0){

												DataSource.getInstance(ServerRestoreActivity.this)
														.getWriteOperation()
														.open()
														.beginTransaction()
														.insert(sleepDatabase)
														.endTransaction()
														.close();
											}
										}



										getLifeTrakApplication().setUserProfile(userProfile);
										getLifeTrakApplication().setTimeDate(timeDate);


										if (objectDeviceSettings.get("watch_face").toString().equals("simple")) {
											getLifeTrakApplication().getTimeDate().setDisplaySize(DISPLAY_FORMAT_BIG_DIGIT);
										} else {
											getLifeTrakApplication().getTimeDate().setDisplaySize(DISPLAY_FORMAT_SMALL_DIGIT);
										}

										if (objectDeviceSettings.getInt("noti_simple_alert") == 1) {
											mPreferenceWrapper.setPreferenceBooleanValue(NOTIFICATION_ENABLED, true).synchronize();
										} else {
											mPreferenceWrapper.setPreferenceBooleanValue(NOTIFICATION_ENABLED, false).synchronize();
										}

										if (objectDeviceSettings.get("hour_format").toString().equals("12")) {
											getLifeTrakApplication().getTimeDate().setHourFormat(SALTimeDate.FORMAT_12HOUR);
										} else {
											getLifeTrakApplication().getTimeDate().setHourFormat(SALTimeDate.FORMAT_24HOUR);
										}
										AmazonTransferUtility.getInstance(ServerRestoreActivity.this)
												.deleteFile(fileNamesArray.getString(x));
									} catch (Exception e) {
										LifeTrakLogger.info("Error on saving offline per dataheader " + e.getLocalizedMessage());
									}
								}
								Intent intent = new Intent(ServerRestoreActivity.this, MainActivity.class);
								startActivity(intent);
								finish();
							}
							catch (Exception e){
								LifeTrakLogger.info("Error on saving offline " + e.getLocalizedMessage());
								Intent intent = new Intent(ServerRestoreActivity.this, MainActivity.class);
								startActivity(intent);
								finish();
							}
						}
					}).start();
				}
				break;
		}

	}

	private void downloadS3Files() throws JSONException{
		AmazonTransferUtility
				.getInstance(ServerRestoreActivity.this)
				.listener(this)
				.setUUID(uuid)
				.downloadFileToAmazonS3(fileNamesArray.getString(indexListCounter), bucketName);
	}

	private S3Object readS3Files() throws JSONException{
		return  AmazonTransferUtility
				.getInstance(ServerRestoreActivity.this)
				.listener(this)
				.setUUID(uuid)
				.getDownloadObject(bucketName,fileNamesArray.getString(indexListCounter));
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
				//TODO insert code to Cancel ServerSync
				finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
