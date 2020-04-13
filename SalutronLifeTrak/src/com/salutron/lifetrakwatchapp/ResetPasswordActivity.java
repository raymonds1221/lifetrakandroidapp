package com.salutron.lifetrakwatchapp;

import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;
import com.salutron.lifetrakwatchapp.web.AsyncListener;
import com.salutron.lifetrakwatchapp.web.ResetPasswordAsync;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;

import roboguice.inject.InjectView;

import org.json.JSONObject;

public class ResetPasswordActivity extends BaseActivity implements AsyncListener {
	@InjectView(R.id.edtEmail) 	private EditText mEmail;
	private ResetPasswordAsync<JSONObject> mResetPasswordAsync;
	private AlertDialog mAlertDialog;
	private ProgressDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);
		
		createEmailTextWatcher();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.reset_password);
		
		mResetPasswordAsync = new ResetPasswordAsync<JSONObject>(this);
		mResetPasswordAsync.setAsyncListener(this);
		
		mAlertDialog = new AlertDialog.Builder(this)
										.setTitle(R.string.lifetrak_title)
										.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface arg0, int arg1) {
												arg0.dismiss();
											}
										}).create();
		
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.please_wait));
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
	 * Create text watcher to delete spaces from the mEmail EditText
	 */
	private void createEmailTextWatcher() {
		mEmail.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String result = s.toString().replaceAll(" ", "");
			    if (!s.toString().equals(result)) {
			         mEmail.setText(result);
			         mEmail.setSelection(result.length());
			    }
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onSendResetPasswordClick(View view) {
		if(NetworkUtil.getInstance(this).isNetworkAvailable()) {
			if(isValidEmail(mEmail.getText().toString())) {
				mProgressDialog.show();
				mResetPasswordAsync.url(getApiUrl() + FORGOT_PASSWORD_URL)
									.addParam("email", mEmail.getText().toString())
									.post();
			} else {
				mAlertDialog.setMessage(getString(R.string.invalid_email));
				mAlertDialog.show();
			}
		} else {
			NetworkUtil.getInstance(this).showConnectionErrorMessage();
		}
	}
	
	private boolean isValidEmail(String email) {
		String regex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
		return email.matches(regex);
	}

	@Override
	public void onAsyncStart() {
		
	}

	@Override
	public void onAsyncFail(int status, String message) {
		mProgressDialog.dismiss();

		if (message.startsWith("We can't find a user with")){
				mAlertDialog.setMessage(getString(R.string.string_we_cant_find));
		}
		else{
			mAlertDialog.setMessage(message);
		}
		
		mAlertDialog.show();
	}

	@Override
	public void onAsyncSuccess(JSONObject result) {
		mProgressDialog.dismiss();
		mAlertDialog.setMessage(getString(R.string.password_sent));
		mAlertDialog.show();
	}
}
