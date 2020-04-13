package com.salutron.lifetrakwatchapp;

import android.os.Bundle;
import android.webkit.WebView;

import com.salutron.lifetrak.R;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;

public class TermsAndConditionsActivity extends BaseActivity {
	private WebView mTermsAndConditions;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_terms_and_conditions);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mTermsAndConditions = (WebView) findViewById(R.id.webTermsAndConditions);
		mTermsAndConditions.loadUrl(TERMS_AND_CONDITIONS_URI);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch    (menuItem.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(menuItem);
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
