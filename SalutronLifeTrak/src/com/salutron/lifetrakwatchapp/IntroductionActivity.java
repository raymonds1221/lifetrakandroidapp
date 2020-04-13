package com.salutron.lifetrakwatchapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.bluetooth.BluetoothAdapter;
import android.database.Cursor;

import com.facebook.android.Facebook;
import com.flurry.android.FlurryAgent;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.adapter.IntroductionPagerAdapter;
import com.salutron.lifetrakwatchapp.service.BluetoothListener;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.viewpagerindicator.CirclePageIndicator;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Activity for displaying useful information regarding the app
 * 
 * @author rsarmiento
 *
 */
@ContentView(R.layout.activity_introduction)
public class 	IntroductionActivity extends BaseActivity {
	@InjectView(R.id.pgrIntroduction) 	private ViewPager mIntroductionPager;
	@InjectView(R.id.idrIntroIndicator) private CirclePageIndicator mIntroductionIndicator;
	
	private boolean mFromButtonClick = false;
    private int REQUEST_CODE_NEXT_ACTIVITY = 0x05;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_introduction);
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
        FlurryAgent.logEvent("Introduction_Page");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindBLEService();
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == BluetoothListener.REQ_BT_ENABLE && resultCode == RESULT_OK) {
			if (mFromButtonClick)
			{
			Intent intent = new Intent(IntroductionActivity.this, ConnectionActivity.class);
			startActivity(intent);
			}
		} else if(requestCode == REQUEST_CODE_NEXT_ACTIVITY && resultCode == RESULT_PROCESS_COMPLETE) {
            finish();
        }
	}
	
	/*
	 * Initialize views and objects
	 */
	private void initializeObjects() {
		IntroductionPagerAdapter adapter = new IntroductionPagerAdapter(getSupportFragmentManager());
		mIntroductionPager.setAdapter(adapter);
		mIntroductionIndicator.setViewPager(mIntroductionPager);
		
		mPreferenceWrapper.setPreferenceBooleanValue(AUTO_SYNC_TIME, true)
                            .setPreferenceBooleanValue(NOTIFICATION_ENABLED, true).synchronize();
		
		mFromButtonClick = false;
		requestBluetoothOn();
	}
	
	/*
	 * Events for button clicks
	 */
	public void onConnectYourWatchClick(View view) {

//						try {
//					File sd = Environment.getExternalStorageDirectory();
//					File data = Environment.getDataDirectory();
//					;
//					if (sd.canWrite()) {
//						//String currentDBPath = "/data/" + getActivity().getPackageName() + "/databases/";
//						String currentDBPath = IntroductionActivity.this.getDatabasePath("SalutronLifeTrak.db").getAbsolutePath();
//						String backupDBPath = Environment.getExternalStorageDirectory() + File.separator + "database";
//						File currentDB = new File(currentDBPath);
//						File backupDB = new File(backupDBPath);
//						if (!backupDB.exists()) {
//							backupDB.mkdirs();
//						}
//
//						OutputStream myOutput = new FileOutputStream(backupDB.getPath() + "/Salutron.backup");
//						InputStream myInput = new FileInputStream(currentDBPath);
//						byte[] buffer = new byte[1024];
//						int length;
//						while ((length = myInput.read(buffer)) > 0) {
//							myOutput.write(buffer, 0, length);
//						}
//
//						myOutput.flush();
//						myOutput.close();
//						myInput.close();
//
//						if (currentDB.exists()) {
//							FileChannel src = new FileInputStream(currentDB).getChannel();
//							FileChannel dst = new FileOutputStream(backupDB).getChannel();
//							dst.transferFrom(src, 0, src.size());
//							src.close();
//							dst.close();
//						}
//					}
//				} catch (Exception e) {
//					Log.e("Error", e.getLocalizedMessage());
//				}
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		
		if(adapter.isEnabled()) {
			
			String query = "select _id from Watch where accessToken is null or trim(accessToken) = ''";
			Cursor cursor = DataSource.getInstance(this)
										.getReadOperation()
										.rawQuery(query);
			
			Intent intent = new Intent();
			
			if(cursor.moveToFirst()) {
				mPreferenceWrapper.setPreferenceBooleanValue(FIRST_INSTALL, false);
				mPreferenceWrapper.setPreferenceBooleanValue(STARTED_FROM_LOGIN, true);
				intent.setClass(this, SignupActivity.class);
			} else {
				mPreferenceWrapper.setPreferenceBooleanValue(FIRST_INSTALL, true);
				intent.setClass(this, ConnectionActivity.class);
			}
            startActivityForResult(intent, REQUEST_CODE_NEXT_ACTIVITY);
		} else {
			mFromButtonClick = true;
			requestBluetoothOn();
		}
	}
	
	public void onSignupClick(View view) {
		onConnectYourWatchClick(view);
	}
	
	public void onSigninClick(View view) {
		Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEXT_ACTIVITY);
	}
}
