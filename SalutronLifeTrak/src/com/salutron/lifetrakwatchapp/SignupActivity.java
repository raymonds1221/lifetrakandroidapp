package com.salutron.lifetrakwatchapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import roboguice.inject.InjectView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.method.LinkMovementMethod;
import android.text.TextWatcher;

import com.actionbarsherlock.view.MenuItem;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.flurry.android.FlurryAgent;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;
import com.salutron.lifetrakwatchapp.util.RealPathUtil;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;
import com.salutron.lifetrakwatchapp.web.AsyncListener;
import com.salutron.lifetrakwatchapp.web.LoginAsync;
import com.salutron.lifetrakwatchapp.web.RegisterAsync;

public class SignupActivity extends BaseActivity implements AsyncListener, SalutronLifeTrakUtility {
	@InjectView(R.id.imgProfilePic)
	private ImageView mProfilePic;
	@InjectView(R.id.edtFirstname)
	private EditText mFirstname;
	@InjectView(R.id.edtLastname)
	private EditText mLastname;
	@InjectView(R.id.edtEmail)
	private EditText mEmail;
	@InjectView(R.id.edtPassword)
	private EditText mPassword;
	@InjectView(R.id.edtConfirmPassword)
	private EditText mConfirmPassword;
	@InjectView(R.id.chkTermsAndConditions)
	private CheckBox mTermsAndConditions;
	@InjectView(R.id.tvwTermsAndConditionsLabel)
	private TextView mTermsAndConditionsLabel;

	private AlertDialog mAlertDialog;
	private AlertDialog mProfilePicAlert;
	private RegisterAsync<JSONObject> mRegisterAsync;
	private ProgressDialog mProgressDialog;
	private final Intent mGalleryIntent = new Intent();
	private Bitmap mBitmap;
	private String mPath;
	private File mCameraFile;
	private String mFBFirstname;
	private String mFBLastname;
	private String mFBEmail;
	private String mFBProfilePic;
	private int mLoginType = LOGIN_TYPE_MANUAL;
	private boolean mWatchConnected = false;

    private final int API_REQUEST_LOGIN = 0x01;
    private final int API_REQUEST_RESTORE = 0x02;
    private final int API_REQUEST_USER = 0x03;

    private int mCurrentApiRequest = API_REQUEST_LOGIN;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.signup);

		if (getIntent().getExtras() != null) {
			Bundle bundle = getIntent().getExtras();
			mWatchConnected = bundle.getBoolean(IS_WATCH_CONNECTED);
		}

		initializeObjects();
	}

	@Override
	public void onResume() {
		super.onResume();
		bindBLEService();
		FlurryAgent.logEvent("Registration_Page");
		FlurryAgent.logEvent("Help_Page");

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
	public void onDestroy() {
		super.onDestroy();
		unbindBLEService();
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mProgressDialog.isShowing())
			mProgressDialog.dismiss();

		mRegisterAsync.cancel();
		initializeObjects();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_IMAGE_GALLERY
				&& resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();
			String[] columns = new String[] { MediaStore.Images.Media.DATA };
			Cursor cursor = this.getContentResolver().query(uri, columns, null,
					null, null);

			if (cursor != null){
				if (cursor.moveToNext()) {
					mPath = cursor.getString(0);
				}
			}
			if (mPath == null) {
				if (Build.VERSION.SDK_INT < 11) {
					mPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this,
							uri);
				} else if (Build.VERSION.SDK_INT < 19) { // SDK >= 11 && SDK <
					// 19
					mPath = RealPathUtil
							.getRealPathFromURI_API11to18(this, uri);
				} else { // SDK > 19 (Android 4.4)
					mPath = RealPathUtil.getRealPathFromURI_API19(this, uri);
				}
			}

			if (mPath != null) {
				setProfileImage(mPath);
			} else {
				LifeTrakLogger.debug("image path is null");
			}
		} else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE
				&& resultCode == Activity.RESULT_OK) {
			onCameraIntentResult(requestCode, resultCode, data);
		} else if (requestCode == Session.DEFAULT_AUTHORIZE_ACTIVITY_CODE) {
			Session.getActiveSession().onActivityResult(this, requestCode,
					resultCode, data);
		}
	}

	private void setProfileImage(String imgPath) {
		orientProfileImage(imgPath);
		if (mBitmap != null){
			mBitmap = createSquareBitmap(mBitmap);
			mProfilePic.setImageBitmap(makeRoundedBitmap(mBitmap, (int) dpToPx(100), (int) dpToPx(100)));
        //    mProfilePic.setImageBitmap(getCroppedBitmap(mBitmap, (int) dpToPx(100), (int) dpToPx(100)));

		}
		else{
			mAlertDialog
			.setMessage(getString(R.string.image_not_found));
			mAlertDialog.show();
		}
	}

	private void orientProfileImage(String imgPath) {
		try {
			ExifInterface exif = new ExifInterface(imgPath);
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			int rotateXDegrees = 0;
			if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
				rotateXDegrees = 90;
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
				rotateXDegrees = 180;
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
				rotateXDegrees = 270;
			}
			mBitmap = BitmapFactory.decodeFile(imgPath);
			if (mBitmap != null)
				mBitmap = shrinkBitmap(mBitmap, 300, rotateXDegrees);
		} catch (IOException e) {
			LifeTrakLogger.error(e.getMessage());
		}
	}

	/**
	 * Shrinks and rotates (if necessary) a passed Bitmap.
	 * 
	 * @param bm
	 * @param maxLengthOfEdge
	 * @param rotateXDegree
	 * @return Bitmap
	 */
	public static Bitmap shrinkBitmap(Bitmap bm, int maxLengthOfEdge,
			int rotateXDegree) {
		if (maxLengthOfEdge > bm.getWidth() && maxLengthOfEdge > bm.getHeight()) {
			return bm;
		} else {
			// shrink image
			float scale = (float) 1.0;
			if (bm.getHeight() > bm.getWidth()) {
				scale = ((float) maxLengthOfEdge) / bm.getHeight();
			} else {
				scale = ((float) maxLengthOfEdge) / bm.getWidth();
			}
			// CREATE A MATRIX FOR THE MANIPULATION
			Matrix matrix = new Matrix();
			// RESIZE THE BIT MAP
			matrix.postScale(scale, scale);
			matrix.postRotate(rotateXDegree);

			// RECREATE THE NEW BITMAP
			bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(),
					matrix, false);

			matrix = null;
			System.gc();

			return bm;
		}
	}

	private Bitmap createSquareBitmap(Bitmap src) {
		Bitmap bitmap = null;

		if (src.getWidth() >= src.getHeight()) {
			bitmap = Bitmap.createBitmap(src, (src.getWidth() / 2) - (src.getHeight() / 2), 0, src.getHeight(), src.getHeight());
		} else {
			bitmap = Bitmap.createBitmap(src, 0, (src.getHeight() / 2) - (src.getWidth() / 2), src.getWidth(), src.getWidth());
		}

		return bitmap;
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

	@Override
	public void onNetworkChanged() {
		super.onNetworkChanged();

		if (!NetworkUtil.getInstance(this).isNetworkAvailable()) {
			if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();

			mAlertDialog
			.setMessage(getString(R.string.check_network_connection));
			mAlertDialog.show();
		}
	}

	private void initializeObjects() {

		mRegisterAsync = new RegisterAsync<JSONObject>(this);
		mRegisterAsync.setAsyncListener(this);

		mAlertDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.lifetrak_title)
		.setMessage(R.string.check_network_connection)
		.setNeutralButton(R.string.ok,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}
		}).create();
		mAlertDialog.setCancelable(false);

		mProfilePicAlert = new AlertDialog.Builder(this)
		.setTitle(R.string.lifetrak_title)
		.setItems(R.array.image_items,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dismiss,
					int which) {
				switch (which) {
				case 0:
					// try {
					// mCameraFile = createTempFile();
					// startCamera(mCameraFile);
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
					startCameraIntent();
					break;
				case 1:
					startActivityForResult(mGalleryIntent,
							REQUEST_CODE_IMAGE_GALLERY);
					break;
				}
			}
		})
		.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,
					int which) {
				dialog.dismiss();
			}
		}).create();

		mGalleryIntent.setType("image/*");
		mGalleryIntent.setAction(Intent.ACTION_GET_CONTENT);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle(R.string.lifetrak_title);
		mProgressDialog.setMessage(getString(R.string.please_wait));
		mProgressDialog.setCancelable(false);

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
				String result = s.toString().replace(" ", "");

				if (!s.toString().equals(result)) {
					mEmail.setText(result);
					mEmail.setSelection(result.length());
				}
			}
		});

		initializeTermsAndConditions();
	}

	private void initializeTermsAndConditions() {
		SpannableString spannableString = new SpannableString(
				getString(R.string.terms_and_conditions));
		ClickableSpan clickableSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				Intent intent = new Intent(SignupActivity.this,
						TermsAndConditionsActivity.class);
				startActivity(intent);
			}
		};

		if (Locale.ENGLISH.getLanguage().equals(Locale.getDefault().getLanguage())) {
			spannableString.setSpan(clickableSpan, 20, 40,
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		} 
		else if (Locale.FRENCH.getLanguage().equals(Locale.getDefault().getLanguage()))
		{
			spannableString.setSpan(clickableSpan, 14, 38,
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		else{
			spannableString.setSpan(clickableSpan, 20, 40,
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		mTermsAndConditionsLabel.setText(spannableString);
		mTermsAndConditionsLabel.setMovementMethod(LinkMovementMethod
				.getInstance());
	}

	public void onCreateAccountClick(View view) {
		if (!NetworkUtil.getInstance(this).isNetworkAvailable()) {
			mAlertDialog
			.setMessage(getString(R.string.check_network_connection));
			mAlertDialog.show();
			return;
		}

		mFirstname.setText(mFirstname.getText().toString().trim());
		mLastname.setText(mLastname.getText().toString().trim());

		mLoginType = LOGIN_TYPE_MANUAL;

		if (isValidInputs()) {
            mCurrentApiRequest = API_REQUEST_LOGIN;
			mRegisterAsync
			.url(getApiUrl() + REGISTER_URI)
			.addParam("role", "mobile")
			.addParam("email", mEmail.getText().toString())
			.addParam("password", mPassword.getText().toString())
			.addParam("first_name", mFirstname.getText().toString())
			.addParam("last_name", mLastname.getText().toString())
			.addParam("client_id",
					"WzXdfbBrMtC10MAVouC3rqy8cA0NNPSycAvpBElF")
					.addParam("client_secret",
							"PRqOLPMb60MCxLeRuJRlROpwKGGdYQGoKzNWUIsD");

			if (mBitmap != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				if (mPath.endsWith(".png")) {
					mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
				} else {
					mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				}

				mRegisterAsync.addParam("image", stream.toByteArray());
			}

			mRegisterAsync.post();
		}
	}

	public void onProfilePictureClick(View view) {
		mProfilePicAlert.show();
	}

	public void onSignupFacebookClick(View view) {
		if (!mTermsAndConditions.isChecked()) {
			mAlertDialog
			.setMessage(getString(R.string.accept_terms_and_conditions));
			mAlertDialog.show();
			return;
		}
		mLoginType = LOGIN_TYPE_FACEBOOK;

		List<String> permissions = new ArrayList<String>();
		permissions.add("email");

		Session.openActiveSession(this, true, permissions,
				new Session.StatusCallback() {
			@Override
			public void call(final Session session, SessionState state,
					Exception exception) {
				if (session.isOpened()) {
					Request.newMeRequest(session,
							new Request.GraphUserCallback() {
						@Override
						public void onCompleted(GraphUser user,
								Response response) {
							LifeTrakLogger.info("access token: "
									+ session.getAccessToken());
							if (null != user) {
								mRegisterAsync
								.url(getApiUrl()
										+ FACEBOOK_URI)
										.addParam(
												"facebook_token",
												session.getAccessToken())
												.addParam(
														"client_id",
														getString(R.string.client_id))
														.addParam(
																"client_secret",
																getString(R.string.client_secret))
																.post();
								try{
									mFBFirstname = user
											.getFirstName();
								}catch (Exception e){}
								try{
									mFBLastname = user
											.getLastName();
								}catch (Exception e){}
								try{
									mFBEmail = user.getProperty(
											"email").toString();
								}catch (Exception e){}
								// mFBProfilePic =
								// "http://graph.facebook.com/"
								// + user.getId() + "/picture";
								try{
									mFBProfilePic = "http://graph.facebook.com/v2.1/"
											+ user.getId()
											+ "/picture?redirect=0&height=200&type=normal&width=200";
								}catch (Exception e){}

								// } else {
								// AlertDialog dialog = new
								// AlertDialog.Builder(SignupActivity.this).setTitle(R.string.lifetrak_title).setMessage(R.string.invalid_facebook_account)
								// .setNeutralButton(R.string.ok,
								// new
								// DialogInterface.OnClickListener()
								// {
								// @Override
								// public void
								// onClick(DialogInterface arg0,
								// int
								// arg1) {
								// arg0.dismiss();
								// }
								// }).create();
								// dialog.show();
							}
						}
					}).executeAsync();
				}
			}
		});
	}

	private boolean isValidEmail(String email) {
		String regex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
		return email.matches(regex);
	}

	private boolean isValidName(String name) {
		/*<<<<<<< HEAD
		String regex = "^[a-zA-Z,-.][a-zA-Z,-.]*$";
		return name.matches(regex);
=======
		return !name.contains("  ");
>>>>>>> server*/
		return !name.contains("  ");
	}

	private boolean isValidInputs() {
		if (isEditTextEmpty(mFirstname) 
				&& isEditTextEmpty(mLastname) 	
				&& isEditTextEmpty(mEmail) 
				&& isEditTextEmpty(mPassword)
				&& isEditTextEmpty(mConfirmPassword))
		{
			mAlertDialog.setMessage(getString(R.string.please_fill));
			mAlertDialog.show();
			return false;
		}
		else if (isEditTextEmpty(mFirstname)) {
			mAlertDialog.setMessage(getString(R.string.enter_input, getString(R.string.firstname)));
			mAlertDialog.show();
			return false;
		} else if (!isEditTextEmpty(mFirstname)
				&& mFirstname.getText().length() < 2) {
			mAlertDialog
			.setMessage(getString(R.string.firstname_minimum_chars));
			mAlertDialog.show();
			return false;
		} else if (!isValidName(mFirstname.getText().toString().trim())) {
			mAlertDialog.setMessage(getString(R.string.invalid_first_name));
			mAlertDialog.show();
			return false;
		} else if (isEditTextEmpty(mLastname)) {
			mAlertDialog.setMessage(getString(R.string.enter_input, getString(R.string.lastname)));
			mAlertDialog.show();
			return false;
		} else if (!isEditTextEmpty(mLastname)
				&& mLastname.getText().length() < 2) {
			mAlertDialog.setMessage(getString(R.string.lastname_minimum_chars));
			mAlertDialog.show();
			return false;
		} else if (!isValidName(mLastname.getText().toString())) {
			mAlertDialog.setMessage(getString(R.string.invalid_last_name));
			mAlertDialog.show();
			return false;
		} else if (isEditTextEmpty(mEmail)) {
			mAlertDialog.setMessage(getString(R.string.enter_input, getString(R.string.email)));
			mAlertDialog.show();
			return false;
		} else if (!isEditTextEmpty(mEmail)
				&& !isValidEmail(mEmail.getText().toString())) {
			mAlertDialog.setMessage(getString(R.string.invalid_email));
			mAlertDialog.show();
			return false;
		} else if (isEditTextEmpty(mPassword)) {
			mAlertDialog
			.setMessage(getString(R.string.enter_input, getString(R.string.password)));
			mAlertDialog.show();
			return false;
		} else if (!mPassword.getText().toString().trim()
				.equals(mConfirmPassword.getText().toString().trim())) {
			mAlertDialog.setMessage(getString(R.string.password_not_matched));
			mAlertDialog.show();
			return false;
		} else if (!isEditTextEmpty(mPassword)
				&& (mPassword.getText().length() < 6 || mPassword.getText()
						.length() > 30)) {
			mAlertDialog.setMessage(getString(R.string.password_min_max_chars));
			mAlertDialog.show();
			return false;
		} else if (!mTermsAndConditions.isChecked()) {
			mAlertDialog
			.setMessage(getString(R.string.accept_terms_and_conditions));
			mAlertDialog.show();
			return false;
		}

		return true;
	}

	private boolean isEditTextEmpty(EditText txt) {
		return txt.getText().toString().trim().isEmpty();
	}

	@Override
	public void onAsyncStart() {
		mProgressDialog.show();
	}

	@Override
	public void onAsyncFail(int status, String message) {
		LifeTrakLogger.error("register fail result: " + message);

		if (Session.getActiveSession() != null
				&& Session.getActiveSession().isOpened()) {
			Session.getActiveSession().closeAndClearTokenInformation();
		}

		mProgressDialog.dismiss();

		mAlertDialog.setMessage(message);
		mAlertDialog.show();
	}

	@Override
	public void onAsyncSuccess(JSONObject result) {
        try {
        switch (mCurrentApiRequest) {
            case API_REQUEST_LOGIN:

                    mProgressDialog.dismiss();

                    if (getLifeTrakApplication().getSelectedWatch() != null) {
                        getLifeTrakApplication().getSelectedWatch().setContext(this);
                        getLifeTrakApplication().getSelectedWatch().setAccessToken(
                                mPreferenceWrapper
                                        .getPreferenceStringValue(ACCESS_TOKEN));
                        getLifeTrakApplication().getSelectedWatch().update();
                    }

                    mPreferenceWrapper.setPreferenceStringValue(ACCESS_TOKEN, result.getString("access_token"))
                            .setPreferenceStringValue(REFRESH_TOKEN, result.getString("refresh_token"))
                            .setPreferenceLongValue(EXPIRATION_DATE, result.getLong("expires"))
                            .setPreferenceBooleanValue(AUTO_SYNC, true)
                            .setPreferenceIntValue(USE_SETTING, USE_APP)
                            .synchronize();

                    if (getLifeTrakApplication().getUserProfile() != null) {
                        getLifeTrakApplication().getUserProfile().setContext(this);

                        switch (mLoginType) {
                            case LOGIN_TYPE_MANUAL:
                                mPreferenceWrapper
                                        .setPreferenceStringValue(FIRST_NAME,
                                                mFirstname.getText().toString())
                                        .setPreferenceStringValue(LAST_NAME,
                                                mLastname.getText().toString())
                                        .setPreferenceStringValue(EMAIL,
                                                mEmail.getText().toString()).synchronize();

                                getLifeTrakApplication().getUserProfile().setFirstname(
                                        mFirstname.getText().toString());
                                getLifeTrakApplication().getUserProfile().setLastname(
                                        mLastname.getText().toString());
                                getLifeTrakApplication().getUserProfile().setEmail(
                                        mEmail.getText().toString());
                                getLifeTrakApplication().getUserProfile()
                                        .setProfileImageLocal(mPath);
                                getLifeTrakApplication().getUserProfile().setAccessToken(
                                        result.getString("access_token"));
                                getLifeTrakApplication().getUserProfile().update();
                                break;
                            case LOGIN_TYPE_FACEBOOK:
                                mPreferenceWrapper
                                        .setPreferenceStringValue(FIRST_NAME, mFBFirstname)
                                        .setPreferenceStringValue(LAST_NAME, mFBLastname)
                                        .setPreferenceStringValue(EMAIL, mFBEmail)
                                        .synchronize();

                                getLifeTrakApplication().getUserProfile().setFirstname(
                                        mFBFirstname);
                                getLifeTrakApplication().getUserProfile().setLastname(
                                        mFBLastname);
                                getLifeTrakApplication().getUserProfile()
                                        .setEmail(mFBEmail);
                                getLifeTrakApplication().getUserProfile()
                                        .setProfileImageWeb(mFBProfilePic);
                                getLifeTrakApplication().getUserProfile().setAccessToken(
                                        result.getString("access_token"));
                                getLifeTrakApplication().getUserProfile().update();
                                break;
                        }
                    } else {
                        switch (mLoginType) {
                            case LOGIN_TYPE_MANUAL:
                                mPreferenceWrapper
                                        .setPreferenceStringValue(FIRST_NAME,
                                                mFirstname.getText().toString())
                                        .setPreferenceStringValue(LAST_NAME,
                                                mLastname.getText().toString())
                                        .setPreferenceStringValue(EMAIL,
                                                mEmail.getText().toString())
                                        .setPreferenceStringValue(PROFILE_IMG, mPath)
                                        .synchronize();
                                break;
                            case LOGIN_TYPE_FACEBOOK:
                                mPreferenceWrapper
                                        .setPreferenceStringValue(FIRST_NAME, mFBFirstname)
                                        .setPreferenceStringValue(LAST_NAME, mFBLastname)
                                        .setPreferenceStringValue(EMAIL, mFBEmail)
                                        .setPreferenceStringValue(PROFILE_IMG,
                                                mFBProfilePic).synchronize();
                                mPreferenceWrapper.setPreferenceStringValue(FIRST_NAME, mFirstname.getText().toString()).setPreferenceStringValue(LAST_NAME, mLastname.getText().toString())
                                        .setPreferenceStringValue(EMAIL, mEmail.getText().toString()).setPreferenceStringValue(PROFILE_IMG, mPath).synchronize();
                                break;
                        }
                    }



                mCurrentApiRequest = API_REQUEST_USER;
                String mAccessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
                mPreferenceWrapper.setPreferenceStringValue(ACCESS_TOKEN, mAccessToken);
                LoginAsync<JSONObject> mLoginAsync =new LoginAsync<JSONObject>(this);
                mLoginAsync.setAsyncListener(this);
                mLoginAsync.url(getApiUrl() + USER_URI).addParam("access_token", mAccessToken).get();


                break;

            case API_REQUEST_USER:

                JSONObject objectResult = result.getJSONObject("result");

                int id = objectResult.getInt("id");

                mPreferenceWrapper
                        .setPreferenceIntValue(USER_ID, id)
                        .synchronize();

                if (getLifeTrakApplication().getSelectedWatch() != null) {
                    if (getLifeTrakApplication().getUserProfile() != null) {
                        long profileId = getLifeTrakApplication().getUserProfile().getId();
                        getLifeTrakApplication().getSelectedWatch().setProfileId(profileId);
                    }
                    getLifeTrakApplication().getSelectedWatch().setContext(this);
                    getLifeTrakApplication().getSelectedWatch().setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
                    getLifeTrakApplication().getSelectedWatch().update();
                }

                List<Watch> watches = new ArrayList<Watch>();

                if (mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN) != null) {
                    watches = DataSource
                            .getInstance(this)
                            .getReadOperation()
                            .query("accessToken = ?",
                                    mPreferenceWrapper
                                            .getPreferenceStringValue(ACCESS_TOKEN))
                            .getResults(Watch.class);
                } else {
                    watches = DataSource.getInstance(this).getReadOperation()
                            .getResults(Watch.class);
                }

                if (watches.size() == 0) {
                    watches = DataSource.getInstance(this).getReadOperation()
                            .query("accessToken is null or accessToken = ?", "")
                            .getResults(Watch.class);
                }

                Intent intent = new Intent();

                if (watches.size() > 0) {
                    long profileId = 0;

                    if (getLifeTrakApplication().getUserProfile() != null)
                        profileId = getLifeTrakApplication().getUserProfile().getId();

                    for (Watch watch : watches) {
                        watch.setProfileId(profileId);
                        watch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
                        watch.update();
                    }

                    if (mPreferenceWrapper.getPreferenceBooleanValue(FIRST_INSTALL)) {
                        mPreferenceWrapper.setPreferenceBooleanValue(FIRST_INSTALL,
                                false);
                        intent.setClass(this, ServerSyncActivity.class);
                        intent.putExtra(WATCH, watches.get(0));
                        intent.putExtra(IS_WATCH_CONNECTED, mWatchConnected);
                    } else {
                        intent.setClass(this, WelcomePageActivity.class);
                    }
                } else {
                    intent.setClass(this, ServerSyncActivity.class);
                    intent.putExtra(WATCH, getLifeTrakApplication()
                            .getSelectedWatch());
                    intent.putExtra(IS_WATCH_CONNECTED, mWatchConnected);
                }

                intent.putExtra(LOGIN_TYPE, mLoginType);

                startActivity(intent);
                setResult(RESULT_PROCESS_COMPLETE);
                finish();
                break;
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }
	}

	private void startCamera(File output) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
		startActivityForResult(intent, REQUEST_CODE_IMAGE_CAMERA);
	}

	@SuppressLint("SimpleDateFormat")
	private File createTempFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_hhmmss")
		.format(new Date());
		String imageFilename = "JPEG_" + timeStamp + "_";
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFilename, ".jpg", storageDir);
		return image;
	}

	/**
	 * Date and time the camera intent was started.
	 */
	protected static Date dateCameraIntentStarted = null;
	/**
	 * Default location where we want the photo to be ideally stored.
	 */
	protected static Uri preDefinedCameraUri = null;
	/**
	 * Potential 3rd location of photo data.
	 */
	protected static Uri photoUriIn3rdLocation = null;
	/**
	 * Retrieved location of the photo.
	 */
	protected static Uri photoUri = null;
	/**
	 * Orientation of the retrieved photo.
	 */
	protected static int rotateXDegrees = 0;

	private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

	/**
	 * Starts the camera intent depending on the device configuration.
	 * 
	 * <b>for Samsung and Sony devices:</b> We call the camera activity with the
	 * method call to startActivityForResult. We only set the constant
	 * CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE. We do NOT set any other intent
	 * extras.
	 * 
	 * <b>for all other devices:</b> We call the camera activity with the method
	 * call to startActivityForResult as previously. This time, however, we
	 * additionally set the intent extra MediaStore.EXTRA_OUTPUT and provide an
	 * URI, where we want the image to be stored.
	 * 
	 * In both cases we remember the time the camera activity was started.
	 */
	protected void startCameraIntent() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				// NOTE: Do NOT SET: intent.putExtra(MediaStore.EXTRA_OUTPUT,
				// cameraPicUri)
				// on Samsung Galaxy S2/S3/.. for the following reasons:
				// 1.) it will break the correct picture orientation
				// 2.) the photo will be stored in two locations (the given path
				// and, additionally, in the MediaStore)
				String manufacturer = android.os.Build.MANUFACTURER
						.toLowerCase(Locale.ENGLISH);
				String model = android.os.Build.MODEL
						.toLowerCase(Locale.ENGLISH);
				String buildType = android.os.Build.TYPE
						.toLowerCase(Locale.ENGLISH);
				String buildDevice = android.os.Build.DEVICE
						.toLowerCase(Locale.ENGLISH);
				String buildId = android.os.Build.ID
						.toLowerCase(Locale.ENGLISH);
				// String sdkVersion =
				// android.os.Build.VERSION.RELEASE.toLowerCase(Locale.ENGLISH);

				boolean setPreDefinedCameraUri = false;
				if (!(manufacturer.contains("samsung"))
						&& !(manufacturer.contains("sony"))) {
					setPreDefinedCameraUri = true;
				}
				if (manufacturer.contains("samsung")
						&& model.contains("galaxy nexus")) { // TESTED
					setPreDefinedCameraUri = true;
				}
				if (manufacturer.contains("samsung")
						&& model.contains("gt-n7000")
						&& buildId.contains("imm76l")) { // TESTED
					setPreDefinedCameraUri = true;
				}

				if (buildType.contains("userdebug")
						&& buildDevice.contains("ariesve")) { // TESTED
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug")
						&& buildDevice.contains("crespo")) { // TESTED
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug")
						&& buildDevice.contains("gt-i9100")) { // TESTED
					setPreDefinedCameraUri = true;
				}

				// /////////////////////////////////////////////////////////////////////////
				// TEST
				if (manufacturer.contains("samsung")
						&& model.contains("sgh-t999l")) { // T-Mobile
					// LTE
					// enabled
					// Samsung
					// S3
					setPreDefinedCameraUri = true;
				}
				if (buildDevice.contains("cooper")) {
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug")
						&& buildDevice.contains("t0lte")) {
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug")
						&& buildDevice.contains("kot49h")) {
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug")
						&& buildDevice.contains("t03g")) {
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug")
						&& buildDevice.contains("gt-i9300")) {
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug")
						&& buildDevice.contains("gt-i9195")) {
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug")
						&& buildDevice.contains("xperia u")) {
					setPreDefinedCameraUri = true;
				}

				// /////////////////////////////////////////////////////////////////////////

				dateCameraIntentStarted = new Date();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (setPreDefinedCameraUri) {
					String filename = System.currentTimeMillis() + ".jpg";
					ContentValues values = new ContentValues();
					values.put(MediaStore.Images.Media.TITLE, filename);
					preDefinedCameraUri = getContentResolver().insert(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							values);
					intent.putExtra(MediaStore.EXTRA_OUTPUT,
							preDefinedCameraUri);
				}
				startActivityForResult(intent,
						CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			} catch (ActivityNotFoundException e) {
				logException(e);
				onCouldNotTakePhoto();
			}
		} else {
			onSdCardNotMounted();
		}
	}

	/**
	 * Being called if the photo could be located. The photo's Uri and its
	 * orientation could be retrieved.
	 */
	protected void onPhotoUriFound() {
		logMessage("Your photo is stored under: " + photoUri.toString());
		mPath = photoUri.toString().replace("file://", "");
		mBitmap = BitmapFactory.decodeFile(mPath);
		mBitmap = shrinkBitmap(mBitmap, 300, rotateXDegrees);
		mProfilePic.setImageBitmap(makeRoundedBitmap(mBitmap,
				(int) dpToPx(100), (int) dpToPx(100)));

	}

	/**
	 * Being called if the photo could not be located (Uri == null).
	 */
	protected void onPhotoUriNotFound() {
		logMessage("Could not find a photoUri that is != null");
	}

	/**
	 * Being called if the camera intent could not be started or something else
	 * went wrong.
	 */
	protected void onCouldNotTakePhoto() {
		Toast.makeText(this, getString(R.string.error_could_not_take_photo),
				Toast.LENGTH_LONG).show();
	}

	/**
	 * Being called if the SD card (or the internal mass storage respectively)
	 * is not mounted.
	 */
	protected void onSdCardNotMounted() {
		Toast.makeText(this, getString(R.string.error_sd_card_not_mounted),
				Toast.LENGTH_LONG).show();
	}

	/**
	 * Being called if the camera intent was canceled.
	 */
	protected void onCanceled() {
		logMessage("Camera Intent was canceled");
	}

	/**
	 * Logs the passed exception.
	 * 
	 * @param exception
	 */
	protected void logException(Exception exception) {
		logMessage(exception.toString());
	}

	/**
	 * Logs the passed exception messages.
	 * 
	 * @param exceptionMessage
	 */
	protected void logMessage(String exceptionMessage) {
		LifeTrakLogger.debug(exceptionMessage);
	}

	/**
	 * On camera activity result, we try to locate the photo.
	 * 
	 * <b>Mediastore:</b> First, we try to read the photo being captured from
	 * the MediaStore. Using a ContentResolver on the MediaStore content, we
	 * retrieve the latest image being taken, as well as its orientation
	 * property and its timestamp. If we find an image and it was not taken
	 * before the camera intent was called, it is the image we were looking for.
	 * Otherwise, we dismiss the result and try one of the following approaches.
	 * <b>Intent extra:</b> Second, we try to get an image Uri from
	 * intent.getData() of the returning intent. If this is not successful
	 * either, we continue with step 3. <b>Default photo Uri:</b> If all of the
	 * above mentioned steps did not work, we use the image Uri we passed to the
	 * camera activity.
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param intent
	 */
	protected void onCameraIntentResult(int requestCode, int resultCode,
			Intent intent) {
		if (resultCode == RESULT_OK) {
			Cursor myCursor = null;
			Date dateOfPicture = null;
			try {
				// Create a Cursor to obtain the file Path for the large image
				String[] largeFileProjection = {
						MediaStore.Images.ImageColumns._ID,
						MediaStore.Images.ImageColumns.DATA,
						MediaStore.Images.ImageColumns.ORIENTATION,
						MediaStore.Images.ImageColumns.DATE_TAKEN };
				String largeFileSort = MediaStore.Images.ImageColumns._ID
						+ " DESC";
				myCursor = getContentResolver().query(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						largeFileProjection, null, null, largeFileSort);
				myCursor.moveToFirst();
				if (!myCursor.isAfterLast()) {
					// This will actually give you the file path location of the
					// image.
					String largeImagePath = myCursor
							.getString(myCursor
									.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
					photoUri = Uri.fromFile(new File(largeImagePath));
					if (photoUri != null) {
						dateOfPicture = new Date(
								myCursor.getLong(myCursor
										.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)));
						if (dateOfPicture != null
								&& dateOfPicture.after(dateCameraIntentStarted)) {
							rotateXDegrees = myCursor
									.getInt(myCursor
											.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
						} else {
							photoUri = null;
						}
					}
					if (myCursor.moveToNext() && !myCursor.isAfterLast()) {
						String largeImagePath3rdLocation = myCursor
								.getString(myCursor
										.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
						Date dateOfPicture3rdLocation = new Date(
								myCursor.getLong(myCursor
										.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)));
						if (dateOfPicture3rdLocation != null
								&& dateOfPicture3rdLocation
								.after(dateCameraIntentStarted)) {
							photoUriIn3rdLocation = Uri.fromFile(new File(
									largeImagePath3rdLocation));
						}
					}
				}
			} catch (Exception e) {
				logException(e);
			} finally {
				if (myCursor != null && !myCursor.isClosed()) {
					myCursor.close();
				}
			}

			if (photoUri == null) {
				try {
					photoUri = intent.getData();
				} catch (Exception e) {
				}
			}

			if (photoUri == null) {
				photoUri = preDefinedCameraUri;
			}

			try {
				if (photoUri != null
						&& new File(photoUri.getPath()).length() <= 0) {
					if (preDefinedCameraUri != null) {
						Uri tempUri = photoUri;
						photoUri = preDefinedCameraUri;
						preDefinedCameraUri = tempUri;
					}
				}
			} catch (Exception e) {
			}

			photoUri = getFileUriFromContentUri(photoUri);
			preDefinedCameraUri = getFileUriFromContentUri(preDefinedCameraUri);
			try {
				if (photoUriIn3rdLocation != null) {
					if (photoUriIn3rdLocation.equals(photoUri)
							|| photoUriIn3rdLocation
							.equals(preDefinedCameraUri)) {
						photoUriIn3rdLocation = null;
					} else {
						photoUriIn3rdLocation = getFileUriFromContentUri(photoUriIn3rdLocation);
					}
				}
			} catch (Exception e) {
			}

			if (photoUri != null) {
				onPhotoUriFound();
			} else {
				onPhotoUriNotFound();
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			onCanceled();
		} else {
			onCanceled();
		}
	}

	/**
	 * Given an Uri that is a content Uri (e.g.
	 * content://media/external/images/media/1884) this function returns the
	 * respective file Uri, that is e.g. file://media/external/DCIM/abc.jpg
	 * 
	 * @param cameraPicUri
	 * @return Uri
	 */
	private Uri getFileUriFromContentUri(Uri cameraPicUri) {
		try {
			if (cameraPicUri != null
					&& cameraPicUri.toString().startsWith("content")) {
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(cameraPicUri, proj,
						null, null, null);
				cursor.moveToFirst();
				// This will actually give you the file path location of the
				// image.
				String largeImagePath = cursor
						.getString(cursor
								.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
				return Uri.fromFile(new File(largeImagePath));
			}
			return cameraPicUri;
		} catch (Exception e) {
			return cameraPicUri;
		}
	}

}
