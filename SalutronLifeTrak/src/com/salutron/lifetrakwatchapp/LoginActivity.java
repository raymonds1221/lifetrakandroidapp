package com.salutron.lifetrakwatchapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.actionbarsherlock.view.MenuItem;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.flurry.android.FlurryAgent;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;
import com.salutron.lifetrakwatchapp.web.AsyncListener;
import com.salutron.lifetrakwatchapp.web.LoginAsync;
import com.salutron.lifetrakwatchapp.web.LoginAsyncTask;

public class LoginActivity extends BaseActivity implements AsyncListener, DialogInterface.OnCancelListener {
	@InjectView(R.id.edtEmail)
	private EditText mEmail;
	@InjectView(R.id.edtPassword)
	private EditText mPassword;
	@InjectView(R.id.chkRememberMe)
	private CheckBox mRememberMe;

	private LoginAsync<JSONObject> mLoginAsync;
	private LoginAsyncTask mLoginAsyncTask;
	private ProgressDialog mProgressDialog;
	private AlertDialog mAlertDialog;
	private SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private final int API_REQUEST_LOGIN = 0x01;
	private final int API_REQUEST_RESTORE = 0x02;
	private final int API_REQUEST_USER = 0x03;
	private int mCurrentApiRequest = API_REQUEST_LOGIN;
	private int mLoginType = LOGIN_TYPE_MANUAL;
	private String mAccessToken;
	private String mEmailValue;
	private static final int REAUTH_ACTIVITY_CODE = 100;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		getKeyHash();
		createEmailTextWatcher();

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		initializeObjects();
		
		if (null != Session.getActiveSession() && Session.getActiveSession().isOpened()) {
			Session.getActiveSession().closeAndClearTokenInformation();
		}
	}

    @Override
    public void onResume() {
        super.onResume();
        bindBLEService();
        FlurryAgent.logEvent("Signin_Page");
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
	 * Display keyhash so whenever there is an fb signup/login error,
	 * we'll just update the one in the developer's account
	 */
	private void getKeyHash() {
		try {
	        PackageInfo info = getPackageManager().getPackageInfo("com.salutron.lifetrak", PackageManager.GET_SIGNATURES);
	        for (Signature signature : info.signatures) {
	            MessageDigest md = MessageDigest.getInstance("SHA");
	            md.update(signature.toByteArray());
				LifeTrakLogger.info(Base64.encodeToString(md.digest(), Base64.DEFAULT));
	            }
	    } catch (NameNotFoundException e) {
	    	LifeTrakLogger.error(e.toString());
	    } catch (NoSuchAlgorithmException e) {
	    	LifeTrakLogger.error(e.toString());
	    }
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private ProgressDialog getProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setTitle(R.string.lifetrak_title);
			mProgressDialog.setMessage(getString(R.string.please_wait));
			mProgressDialog.setCancelable(true);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(this);
		}
		return mProgressDialog;
	}

	private void initializeObjects() {
		mDateFormat.applyPattern("yyyy-MM-dd hh:mm:ss");
		if (mPreferenceWrapper.getPreferenceBooleanValue(IS_REMEMBER_ME)) {
			mEmail.setText(mPreferenceWrapper.getPreferenceStringValue(EMAIL));
			mPassword.setText(mPreferenceWrapper.getPreferenceStringValue(PASSWORD));
			mRememberMe.setChecked(true);
		}

		mAlertDialog = new AlertDialog.Builder(this).setTitle(R.string.lifetrak_title).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}
		}).create();

		mLoginAsync = new LoginAsync<JSONObject>(this);
		mLoginAsync.setAsyncListener(this);
	}

	public void onButtonClick(View view) {
		if (!NetworkUtil.getInstance(this).isNetworkAvailable()) {
			mAlertDialog.setMessage(getString(R.string.check_network_connection));
			mAlertDialog.show();
			return;
		}

		switch (view.getId()) {
		case R.id.btnSignIn:
			if    (isValidInputs()) {
				mLoginType = LOGIN_TYPE_MANUAL;
				String email = mEmail.getText().toString();
				String password = mPassword.getText().toString();
				String clientId = getString(R.string.client_id);
				String clientSecret = getString(R.string.client_secret);
				String url = getApiUrl() + LOGIN_URI;
				mEmailValue = email;
				mCurrentApiRequest = API_REQUEST_LOGIN;
				mLoginAsyncTask = new LoginAsyncTask();
				mLoginAsyncTask.listener(this);

				mLoginAsyncTask.addParam("email", email);
				mLoginAsyncTask.addParam("password", password);
				mLoginAsyncTask.addParam("client_id", clientId);
				mLoginAsyncTask.addParam("client_secret", clientSecret);
				mLoginAsyncTask.execute(url);
				//mLoginAsync.url(url).addParam("email", email).addParam("password", password).addParam("client_id", clientId).addParam("client_secret", clientSecret).post();
			}
			break;
		case R.id.btnSignInFB:
			mLoginType = LOGIN_TYPE_FACEBOOK;
//            try {
//                PackageInfo info = getPackageManager().getPackageInfo(
//                        this.getPackageName(),
//                        PackageManager.GET_SIGNATURES);
//                for (Signature signature : info.signatures) {
//                    MessageDigest md = MessageDigest.getInstance("SHA");
//                    md.update(signature.    toByteArray());
//                    Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//                }
//            } catch (NameNotFoundException e) {
//                e.getLocalizedMessage();
//
//            } catch (NoSuchAlgorithmException e) {
//                e.getLocalizedMessage();
//            }

			onFBLogin();
			break;
		case R.id.tvwForgotPassword:
			Intent intent = new Intent(this, ResetPasswordActivity.class);
			startActivity(intent);
			break;
		}
	}

	private void onFBLogin() {
		Session.openActiveSession(this, true, Arrays.asList("email"), new Session.StatusCallback() {
			@Override
			public void call(final Session session, SessionState state, Exception exception) {
				if (session.isOpened()) {
					Request.newMeRequest(session, new Request.GraphUserCallback() {
						@Override
						public void onCompleted(GraphUser user, Response response) {
							// TODO find error here

							if (session == Session.getActiveSession()) {
								if (null != user) {
									mEmailValue = String.valueOf(user.getProperty("email"));
									mCurrentApiRequest = API_REQUEST_LOGIN;
                                    mLoginAsyncTask = new LoginAsyncTask();
                                    mLoginAsyncTask.listener(LoginActivity.this);
                                    mLoginAsyncTask.addParam("facebook_token", session.getAccessToken());
                                    mLoginAsyncTask.addParam("client_id", getString(R.string.client_id));
                                    mLoginAsyncTask.addParam("client_secret", getString(R.string.client_secret));
                                    mLoginAsyncTask.execute(getApiUrl() + FACEBOOK_URI);

//                                    mLoginAsync.url(getApiUrl() + FACEBOOK_URI).addParam("facebook_token", session.getAccessToken()).addParam("client_id", getString(R.string.client_id))
//											.addParam("client_secret", getString(R.string.client_secret)).post();
								}
							}
						}
					}).executeAsync();
				}
			}
		});
	}

	@Override
	public void onCancel(DialogInterface dialogInterface) {
		// Abort login when back button is pressed while progress dialog is shown
		if (dialogInterface == mProgressDialog) {
			mLoginAsyncTask.abortLogin();

		}
	}

	@Override
	public void onAsyncStart() {
		getProgressDialog().show();
	}

	@Override
	public void onAsyncFail(int status, String message) {
        mLoginAsync = new LoginAsync<JSONObject>(this);
        mLoginAsync.setAsyncListener(this);
        
        if ((message != null && message.equalsIgnoreCase("network error")) || status == -101)
        	message = getString(R.string.string_steps_network_error);
        
        if (message != null && message.startsWith("The selected email is invalid"))
        	message = getString(R.string.string_steps_email_invalid);
        
        if (message != null && message.startsWith("The password must be between"))
        	message = getString(R.string.string_steps_password_invalid);
        
        if (message != null && message.startsWith("The user credentials"))
        	message = getString(R.string.string_steps_user_credential);
        try {
			getProgressDialog().dismiss();
		}
		catch (Exception e){
			LifeTrakLogger.info("Error on Progress Login Activity :"  + e.getLocalizedMessage());
		}
		mAlertDialog.setMessage(message);
		mAlertDialog.show();
	}

	@Override
	public void onAsyncSuccess(JSONObject result) {
		try {
			switch (mCurrentApiRequest) {
			case API_REQUEST_LOGIN:
				mPreferenceWrapper.setPreferenceStringValue(ACCESS_TOKEN, result.getString("access_token"))
									.setPreferenceStringValue(REFRESH_TOKEN, result.getString("refresh_token"))
									.setPreferenceIntValue(USE_SETTING, USE_APP)
                                    .setPreferenceBooleanValue(AUTO_SYNC, true)
									.setPreferenceLongValue(EXPIRATION_DATE, result.getLong("expires")).synchronize();
				if (mRememberMe.isChecked()) {
					mPreferenceWrapper.setPreferenceBooleanValue(IS_REMEMBER_ME, true)
										.setPreferenceStringValue(EMAIL, mEmailValue)
										.setPreferenceStringValue(PASSWORD, mPassword.getText().toString())
										.synchronize();
				} else {
					mPreferenceWrapper.setPreferenceBooleanValue(IS_REMEMBER_ME, false).setPreferenceStringValue(PASSWORD, null).synchronize();
				}

				List<UserProfile> profiles = DataSource.getInstance(this).getReadOperation().query("email = ?", mEmailValue).getResults(UserProfile.class);

				if (profiles.size() > 0) {
					UserProfile profile = profiles.get(0);
					List<Watch> watches = new ArrayList<Watch>();

					if (!profile.getAccessToken().isEmpty()) {
						watches = DataSource.getInstance(this).getReadOperation().query("accessToken = ?", profile.getAccessToken()).getResults(Watch.class);

						if (watches.size() == 0) {
							watches = DataSource.getInstance(this).getReadOperation().query("accessToken = ?", "").getResults(Watch.class);
						}
					} else {
						watches = DataSource.getInstance(this).getReadOperation().getResults(Watch.class);
					}

					profile.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
					profile.update();

					for (Watch watch : watches) {
						watch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
						watch.setProfileId(profile.getId());
						watch.update();
					}
				}

				mAccessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
				mPreferenceWrapper.setPreferenceStringValue(ACCESS_TOKEN, mAccessToken);
				mLoginAsync.url(getApiUrl() + USER_URI).addParam("access_token", mAccessToken).get();

				mCurrentApiRequest = API_REQUEST_USER;
				break;
			case API_REQUEST_RESTORE:
				getProgressDialog().dismiss();
				
				JSONArray arrayResult = result.getJSONArray("result");

				/*
				if (arrayResult.length() > 0) {
					
				} else {
					Intent intent = new Intent(this, ConnectionActivity.class);
					startActivity(intent);
					finish();
				}*/
				
				for (int i = 0; i < arrayResult.length(); i++) {
					JSONObject objectDevice = arrayResult.getJSONObject(i);
					String macAddress = objectDevice.getString("mac_address");
					
					String query = "select w._id from Watch w inner join UserProfile u on w.profileId = u._id " +
										"where w.macAddress = ? and u.email = ?";
					
					Cursor cursor = DataSource.getInstance(this)
												.getReadOperation()
												.rawQuery(query, macAddress, mEmailValue);
					
					if (cursor.moveToFirst()) {
						long id = cursor.getLong(cursor.getColumnIndex("_id"));
						
						List<Watch> watches = DataSource.getInstance(this)
														.getReadOperation()
														.query("_id = ?", String.valueOf(id))
														.getResults(Watch.class);
						
						watches.get(0).setContext(this);
						watches.get(0).setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
						watches.get(0).update();
					} else {
						Watch watch = new Watch(this);
						watch.setMacAddress(macAddress);
						watch.setModel(objectDevice.getInt("model_number"));
						watch.setName(objectDevice.getString("device_name"));
						watch.setLastSyncDate(mDateFormat.parse(objectDevice.getString("last_date_synced")));
						watch.setCloudLastSyncDate(new Date());
						watch.setImage(objectDevice.getString("image"));
						watch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
						watch.insert();
					}
				}

				mPreferenceWrapper.setPreferenceBooleanValue(STARTED_FROM_LOGIN, true).synchronize();
				Intent intent = new Intent(this, WelcomePageActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				finish();

				mCurrentApiRequest = API_REQUEST_LOGIN;
				break;
			case API_REQUEST_USER:
				JSONObject objectResult = result.getJSONObject("result");
				String firstname = objectResult.getString("first_name");
				String lastname = objectResult.getString("last_name");
				String email = objectResult.getString("email");
				String image = objectResult.getString("image");
				int activated = objectResult.getInt("activated");
                int id = objectResult.getInt("id");
				int is_facebook = objectResult.getInt("is_facebook");

				mPreferenceWrapper	.setPreferenceStringValue(ACCESS_TOKEN, mAccessToken)
									.setPreferenceStringValue(FIRST_NAME, firstname)
									.setPreferenceStringValue(LAST_NAME, lastname)
									.setPreferenceStringValue(EMAIL, email)
									.setPreferenceStringValue(PROFILE_IMG, image)
									.setPreferenceBooleanValue(ACCOUNT_ACTIVATED, activated == 1)
									.setPreferenceBooleanValue(IS_FACEBOOK, is_facebook == 1)
                                    .setPreferenceIntValue(USER_ID, id)
									.synchronize();

				mLoginAsync.url(getApiUrl() + RESTORE_DEVICE_URI).addParam("access_token", mAccessToken).get();

				mCurrentApiRequest = API_REQUEST_RESTORE;
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isValidInputs() {
		if ( isEditTextEmpty(mEmail) 
				&& isEditTextEmpty(mPassword))
		{
			mAlertDialog.setMessage(getString(R.string.please_fill));
			mAlertDialog.show();
			return false;
		}
		else if (isEditTextEmpty(mEmail)) {
			mAlertDialog.setMessage(getString(R.string.enter_input, "Email"));
			mAlertDialog.show();
			return false;
		} else if (!isEditTextEmpty(mEmail) && !isValidEmail(mEmail.getText().toString())) {
			mAlertDialog.setMessage(getString(R.string.invalid_email));
			mAlertDialog.show();
			return false;
		} else if (isEditTextEmpty(mPassword)) {
			mAlertDialog.setMessage(getString(R.string.enter_input, "Password"));
			mAlertDialog.show();
			return false;
		}
		
		return true;
	}
	
	private boolean isEditTextEmpty(EditText txt) {
		return txt.getText().toString().isEmpty();
	}
	
	private boolean isValidEmail(String email) {
		String regex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
		return email.matches(regex);
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
}
