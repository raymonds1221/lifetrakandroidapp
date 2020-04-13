package com.salutron.lifetrakwatchapp;

import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;
import com.facebook.Session;
import com.flurry.android.FlurryAgent;
import com.salutron.blesdk.SALBLEService;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.adapter.WelcomeWatchAdapter;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;
import com.salutron.lifetrakwatchapp.web.AsyncListener;
import com.salutron.lifetrakwatchapp.web.DeleteWatchAsync;
import com.salutron.lifetrakwatchapp.web.DeleteWatchAsyncTask;

/**
 * Activity for displaying a list of connected device to choose from
 * 
 * @author rsarmiento
 * 
 */
@ContentView(R.layout.activity_main)
public class WelcomePageActivity extends BaseActivity implements WelcomeWatchAdapter.WelcomeWatchAdapterListener, AsyncListener {
	@InjectView(R.id.lstWatch)
	private ListView mWatchList;
	private WelcomeWatchAdapter mAdapter;
	private List<Watch> mWatches;
	private AlertDialog mAlertDialog;
	private DeleteWatchAsync<JSONObject> mDelWatchAsync;
	private ProgressDialog mProgressDialog;
	private Watch mTobeDeletedWatch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		Intent intentService = new Intent(this, SALBLEService.class);
		startService(intentService);

		mDelWatchAsync = new DeleteWatchAsync<JSONObject>(this);
		mDelWatchAsync.setAsyncListener(this);

		getSupportActionBar().setIcon(android.R.color.transparent);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setTitle(R.string.logout);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle(R.string.lifetrak_title);
		mProgressDialog.setMessage(getString(R.string.please_wait));
		mProgressDialog.setCancelable(false);
	}

	@Override
	public void onResume() {
		super.onResume();
		initializeObjects();
        bindBLEService();
        FlurryAgent.logEvent("Device_Listing_Page");
       
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
//		if (getLifeTrakApplication().getSelectedWatch()!= null && getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415){
//			String macAddress = null;
//			if (getLifeTrakApplication().getSelectedWatch() != null)
//				macAddress = getLifeTrakApplication().getSelectedWatch().getMacAddress();
//			else
//				macAddress = mPreferenceWrapper.getPreferenceStringValue(LAST_R450_SYNC);
//			if (macAddress != null && !macAddress.contains(":"))
//				macAddress = convertiOSToAndroidMacAddress(macAddress);
//			LifeTrakLogger.info("Reconnect to LifeTrakSyncR450 with mac address = " + macAddress);
//			if (macAddress != null)
//				mSalutronService.connectToDevice(macAddress, SALBLEService.MODEL_R415);
//		}

        unbindBLEService();
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

	/*
	 * Event for btnConnectNewWatch button
	 */
	public void onConnectNewWatchClicked(View view) {

		Intent intent = new Intent(this, ConnectionActivity.class);
		startActivity(intent);
		//finish();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_WELCOME_PAGE && resultCode == RESULT_CANCELED) {
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (android.R.id.home == item.getItemId()) {
			AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(R.string.verify_signout)
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// mPreferenceWrapper.setPreferenceStringValue(ACCESS_TOKEN,
							// null).setPreferenceStringValue(REFRESH_TOKEN,
							// null).synchronize();
							mPreferenceWrapper.clearSharedPref();
							getLifeTrakApplication().clearDB();
							if (null != Session.getActiveSession()) {
								Session.getActiveSession().closeAndClearTokenInformation();
							}
							Intent intent = new Intent(WelcomePageActivity.this, IntroductionActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
							startActivity(intent);
							finish();
						}
					}).create();
			alert.show();
		}

		return true;
	}

	private void initializeObjects() {

		mAlertDialog = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}
		}).create();

		String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
		
		if (accessToken != null) {
			try{
//				List<UserProfile> profiles =
//						DataSource.getInstance(this)
//								.getReadOperation()
//								.query("email = ?", mPreferenceWrapper.getPreferenceStringValue(EMAIL)).getResults(UserProfile.class);
//
//				if (profiles.size() > 0) {
//					UserProfile profile = profiles.get(0);
//					mWatches = DataSource.getInstance(this)
//							.getReadOperation()
//							.query("accessToken = ? and profileId = ?",
//									accessToken,
//									String.valueOf(profile.getId()))
//							.orderBy("lastSyncDate", SORT_DESC)
//							.getResults(Watch.class);
//				}
//				else {
					mWatches = DataSource.getInstance(this)
							.getReadOperation()
							.query("accessToken = ?", accessToken)
							.orderBy("lastSyncDate", SORT_DESC)
							.getResults(Watch.class);
				//}
				if (mWatches.size() == 0) {
					findViewById(R.id.tvwChooseDevice).setVisibility(View.GONE);
				}

				List<Watch> allWathes = DataSource.getInstance(this)
						.getReadOperation()
						.getResults(Watch.class);

				LifeTrakLogger.info("all watches: " + allWathes.size());
			}catch (Exception e){
				LifeTrakLogger.info("Error on Welcome Page " + e.getLocalizedMessage());

				Intent intent = new Intent(this, IntroductionActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				finish();
			}






		} else {
			Intent intent = new Intent(this, IntroductionActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			finish();
			return;
		}
		if (mWatches != null) {
			mAdapter = new WelcomeWatchAdapter(this, R.layout.adapter_welcome_watch, mWatches);
			mWatchList.setAdapter(mAdapter);

			if (mWatches.size() == 0 && (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) == null && mPreferenceWrapper.getPreferenceStringValue(REFRESH_TOKEN) == null)) {
				Intent intent = new Intent(this, IntroductionActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				finish();
			} else if (!mPreferenceWrapper.getPreferenceBooleanValue(ACCOUNT_ACTIVATED) && !mPreferenceWrapper.getPreferenceBooleanValue(IS_FACEBOOK) && mPreferenceWrapper
					.getPreferenceBooleanValue(STARTED_FROM_LOGIN)) {
//			AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setMessage(R.string.verify_account)
//					.setNeutralButton(R.string.remind_me_later, new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface arg0, int arg1) {
//							arg0.dismiss();
//						}
//					}).create();
				//alert.show();
			}
		}
	}

	@Override
	public void onWatchSelected(Watch watch) {
		if (watch.getAccessToken() == null) {	
			watch.setContext(this);
			
			if (getLifeTrakApplication().getUserProfile() != null) {
				long profileId = getLifeTrakApplication().getUserProfile().getId();
				watch.setProfileId(profileId);
			}

			watch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
			watch.update();
		}
		
		getLifeTrakApplication().setSelectedWatch(watch);
		getLifeTrakApplication().setCurrentDate(new Date());
		if (!watch.getMacAddress().toString().contains(":")){
			mPreferenceWrapper.setPreferenceBooleanValue(FROM_IOS, true).synchronize();
		}

		if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415 )
			mPreferenceWrapper.setPreferenceStringValue(LAST_R450_SYNC,getLifeTrakApplication().getSelectedWatch().getMacAddress());

		mPreferenceWrapper.setPreferenceStringValue(MAC_ADDRESS, watch.getMacAddress())
                            .setPreferenceLongValue(LAST_CONNECTED_WATCH_ID, watch.getId())
				.synchronize();

		List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(this)
															.getReadOperation().query("watchDataHeader = ?", String.valueOf(watch.getId()))
															.getResults(StatisticalDataHeader.class);

		List<UserProfile> userProfiles = DataSource.getInstance(this)
													.getReadOperation()
													.query("watchUserProfile == ?", String.valueOf(watch.getId()))
				.getResults(UserProfile.class);

		if (dataHeaders.size() > 0) {
			List<TimeDate> timeDate = DataSource.getInstance(this).getReadOperation().query("watchTimeDate == ?", String.valueOf(watch.getId())).getResults(TimeDate.class);

			if (timeDate.size() > 0) {
				getLifeTrakApplication().setTimeDate(timeDate.get(0));
			}

			if (userProfiles.size() > 0) {
				UserProfile userProfile = userProfiles.get(0);
				userProfile.setContext(getApplicationContext());
				userProfile.setFirstname(mPreferenceWrapper.getPreferenceStringValue(FIRST_NAME));
				userProfile.setLastname(mPreferenceWrapper.getPreferenceStringValue(LAST_NAME));
				userProfile.setEmail(mPreferenceWrapper.getPreferenceStringValue(EMAIL));
				
				if (getIntent().getExtras() != null) {
					int loginType = getIntent().getExtras().getInt(LOGIN_TYPE);
					
					switch (loginType) {
					case LOGIN_TYPE_MANUAL:
						userProfile.setProfileImageLocal(mPreferenceWrapper.getPreferenceStringValue(PROFILE_IMG));
						break;
					case LOGIN_TYPE_FACEBOOK:
						userProfile.setProfileImageWeb(mPreferenceWrapper.getPreferenceStringValue(PROFILE_IMG));
						break;
					}
				}
				
				userProfile.update();
				getLifeTrakApplication().setUserProfile(userProfile);
			}


			Intent intent = new Intent();

			if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null && mPreferenceWrapper.getPreferenceStringValue(REFRESH_TOKEN) != null) {
				if (mPreferenceWrapper.getPreferenceBooleanValue(STARTED_FROM_LOGIN)) {
					mPreferenceWrapper.setPreferenceBooleanValue(STARTED_FROM_LOGIN, false).synchronize();
					
					if(mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC)) {
						intent.setClass(this, ServerSyncActivity.class);
						intent.putExtra(WATCH, watch);
					} else {
						intent.setClass(this, MainActivity.class);
					}
					
				} else {
					intent.setClass(this, MainActivity.class);
				}
			} else {
				
				if(mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC)) {
					intent.setClass(this, ServerSyncActivity.class);
					intent.putExtra(WATCH, watch);
				} else {
					intent.setClass(this, MainActivity.class);
				}
			}

			startActivity(intent);
		} else {
			if (userProfiles.size() > 0) {
				getLifeTrakApplication().setUserProfile(userProfiles.get(0));
			}

			Intent intent = new Intent(this, ServerRestoreActivity.class);
			intent.putExtra(WATCH, watch);
			startActivity(intent);
		}
	}

	@Override
	public void onDeleteWatch(final Watch watch) {
		mTobeDeletedWatch = watch;
		if (!NetworkUtil.getInstance(this).isNetworkAvailable()) {
			mAlertDialog.setMessage(getString(R.string.check_network_connection));
			mAlertDialog.show();
			return;
		}

		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle(R.string.delete_watch_title);
		alertBuilder.setMessage(getString(R.string.delete_watch_message, watch.getName()));
		alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {

			}
		});
		alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				String url = getApiUrl() + DELETE_DEVICE_URI;
				String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
				String macAddress = mTobeDeletedWatch.getMacAddress();
				/*mDelWatchAsync.url(url).addParam(MAC_ADDRESS, macAddress)
								.addParam(ACCESS_TOKEN, accessToken)
								.post();*/

				new DeleteWatchAsyncTask(getApplicationContext(), accessToken, macAddress)
						.listener(WelcomePageActivity.this).execute(url);
			}
		});

		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	@Override
	public void onAsyncStart() {
		runOnUiThread(new Runnable() {
			public void run() {
				mProgressDialog.show();
			}
		});
	}

	@Override
	public void onAsyncFail(int status, String message) {
		mProgressDialog.dismiss();
		mAlertDialog.setMessage(message);
		mAlertDialog.show();
	}

	@Override
	public void onAsyncSuccess(JSONObject result) {
		mProgressDialog.dismiss();
		mWatches.remove(mTobeDeletedWatch);
		mTobeDeletedWatch.delete();
		mAdapter.notifyDataSetChanged();

		if (mWatches.size() == 0) {
			mPreferenceWrapper.setPreferenceBooleanValue(HAS_USER_PROFILE, false);
			mPreferenceWrapper.synchronize();
			findViewById(R.id.tvwChooseDevice).setVisibility(View.GONE);
		} else {
			findViewById(R.id.tvwChooseDevice).setVisibility(View.VISIBLE);
		}
	}

}
