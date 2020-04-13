package com.salutron.lifetrakwatchapp;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.util.PreferenceWrapper;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

public class StartingActivity extends SherlockActivity {
	private PreferenceWrapper mPreferenceWrapper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set Locale. For debug only
//		 Resources res = getApplicationContext().getResources();
//	    // Change locale settings in the app.
//	    DisplayMetrics dm = res.getDisplayMetrics();
//	    android.content.res.Configuration conf = res.getConfiguration();
//	    conf.locale = new Locale("fr");
//	    res.updateConfiguration(conf, dm);
		
		// Start Crashlytics logging
		Crashlytics.start(this);
		
		mPreferenceWrapper = PreferenceWrapper.getInstance(this);
		
		String accessToken = mPreferenceWrapper.getPreferenceStringValue(SalutronLifeTrakUtility.ACCESS_TOKEN);
		Intent intent = new Intent();
		
		if (accessToken != null) {
			intent.setClass(this, WelcomePageActivity.class);
		} else {
			intent.setClass(this, IntroductionActivity.class);
		}
		
		startActivity(intent);
		finish();
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
	
}
