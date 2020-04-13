package com.salutron.lifetrakwatchapp.fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;

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
import android.content.pm.ActivityInfo;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.ui.widget.Widget;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;
import com.salutron.lifetrakwatchapp.util.RealPathUtil;
import com.salutron.lifetrakwatchapp.web.AsyncListener;
import com.salutron.lifetrakwatchapp.web.DownloadImageTask;
import com.salutron.lifetrakwatchapp.web.EditProfileAsync;

public class EditProfileFragment extends BaseFragment implements View.OnClickListener, AsyncListener {
	@InjectView(R.id.imgProfilePic)
	private ImageView mProfilePic;
	@InjectView(R.id.tvwEditProfilePic)
	private TextView mEditProfilePic;
	@InjectView(R.id.edtFirstname)
	private EditText mFirstname;
	@InjectView(R.id.edtLastname)
	private EditText mLastname;
	// @InjectView(R.id.edtEmail) private EditText mEmail;
	@InjectView(R.id.edtOldPassword)
	private EditText mOldPassword;
	@InjectView(R.id.edtNewPassword)
	private EditText mNewPassword;
	@InjectView(R.id.edtConfirmPassword)
	private EditText mConfirmPassword;
	@InjectView(R.id.llPassword)
	private ViewGroup mPasswordGroup;
	private AlertDialog mAlertDialog;
	private AlertDialog mImageAlertDialog;
	private ProgressDialog mProgressDialog;
	private EditProfileAsync<JSONObject> mEditProfileAsync;
	private final Intent mGalleryIntent = new Intent();
	private Bitmap mBitmap;
	private String mPath;
	private File mCameraFile;
	private String mEmailString;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_edit_account, null);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initializeObjects();
	}

	private void initializeObjects() {
		getView().findViewById(R.id.btnSaveChanges).setOnClickListener(this);
		getView().findViewById(R.id.tvwEditProfilePic).setOnClickListener(this);

		LifeTrakLogger.configure();

		mEditProfileAsync = new EditProfileAsync<JSONObject>(getActivity());
		mEditProfileAsync.setAsyncListener(this);

		mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}
		}).create();

		mImageAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setItems(R.array.image_items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
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
					startActivityForResult(mGalleryIntent, REQUEST_CODE_IMAGE_GALLERY);
					break;
				}
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}
		}).create();

		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setTitle(R.string.lifetrak_title);
		mProgressDialog.setMessage(getString(R.string.please_wait));
		mProgressDialog.setCancelable(false);

		mGalleryIntent.setType("image/*");
		mGalleryIntent.setAction(Intent.ACTION_GET_CONTENT);

		mFirstname.setText(getLifeTrakApplication().getUserProfile().getFirstname());
		mLastname.setText(getLifeTrakApplication().getUserProfile().getLastname());
		// mEmail.setText(getLifeTrakApplication().getUserProfile().getEmail());
		/*
		mFirstname.addTextChangedListener(new TextWatcher() {

	          public void afterTextChanged(Editable s) {

	            // you can call or do what you want with your EditText here
	           // yourEditText. ... 
	        	  String editedString = mFirstname.getText().toString();
		        	 String newString = editedString.trim().replaceAll(" +", " ");
		         mFirstname.setText(newString);
	          }

	          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	          public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	 
	          }
	       });
		
		mLastname.addTextChangedListener(new TextWatcher() {

	          public void afterTextChanged(Editable s) {

	            // you can call or do what you want with your EditText here
	           // yourEditText. ... 

	          }

	          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	          public void onTextChanged(CharSequence s, int start, int before, int count) {}
	       });
		
		*/
		mEmailString = getLifeTrakApplication().getUserProfile().getEmail();
		LifeTrakLogger.info("mEmailString = " + mEmailString);
		UserProfile profile = getLifeTrakApplication().getUserProfile();
		if (mBitmap == null) {
			if (null != profile.getProfileImageLocal() && !"".equals(profile.getProfileImageLocal())) {
				File file = new File(profile.getProfileImageLocal());

				if (file.exists()) {
					showPictureFromLocal(getLifeTrakApplication().getUserProfile().getProfileImageLocal());
				}
			} else if (null != profile.getProfileImageWeb()) {
				showPictureFromWeb(getLifeTrakApplication().getUserProfile().getProfileImageWeb());
			}
		}

		if (mPreferenceWrapper.getPreferenceBooleanValue(IS_FACEBOOK)) {
			mPasswordGroup.setVisibility(View.GONE);
		} else {
			mPasswordGroup.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_IMAGE_GALLERY && resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();
			String[] columns = new String[] { MediaStore.Images.Media.DATA };
			Cursor cursor = getActivity().getContentResolver().query(uri, columns, null, null, null);

			if (cursor.moveToNext()) {
				mPath = cursor.getString(0);
			}

			if (mPath == null) {
				if (Build.VERSION.SDK_INT < 11) {
					mPath = RealPathUtil.getRealPathFromURI_BelowAPI11(getActivity(), uri);
				} else if (Build.VERSION.SDK_INT < 19) { // SDK >= 11 && SDK <
															// 19
					mPath = RealPathUtil.getRealPathFromURI_API11to18(getActivity(), uri);
				} else { // SDK > 19 (Android 4.4)
					mPath = RealPathUtil.getRealPathFromURI_API19(getActivity(), uri);
				}
			}

			if (mPath != null) {
				setProfileImage(mPath);
			} else {
				LifeTrakLogger.error("image path is null");
			}

		} else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			onCameraIntentResult(requestCode, resultCode, data);
		}
	}

	private void setProfileImage(String imgPath) {
		orientProfileImage(imgPath);
		//mBitmap = createSquareBitmap(mBitmap);
		mProfilePic.setImageBitmap(makeRoundedBitmap(mBitmap, (int) dpToPx(100), (int) dpToPx(100)));
	}

	/*private void orientProfileImage(String imgPath) {
		try {
			ExifInterface exif = new ExifInterface(imgPath);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			Matrix matrix = new Matrix();
			
			int rotateAngle = 0;
			
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotateAngle = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotateAngle = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotateAngle = 270;
				break;
			}
			
			mBitmap = BitmapFactory.decodeFile(imgPath);
			
			matrix.postRotate(rotateAngle);
			
			mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
			mBitmap = createScaledBitmap(mBitmap, dpToPx(200), dpToPx(200));
		} catch (IOException e) {
			Log.e("image-rotation", e.getMessage());
		}
	}*/
	
	private void orientProfileImage(String imgPath) {
		try {
			ExifInterface exif = new ExifInterface(imgPath);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			int rotateXDegrees = 0;
			if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
				rotateXDegrees = 90;
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
				rotateXDegrees = 180;
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
				rotateXDegrees = 270;
			}
			mBitmap = BitmapFactory.decodeFile(imgPath);
			mBitmap = BaseFragment.shrinkBitmap(mBitmap, 300, rotateXDegrees);
		} catch (IOException e) {
			LifeTrakLogger.error(e.getMessage());
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
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btnSaveChanges:

			if (!NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
				NetworkUtil.getInstance(getActivity()).showConnectionErrorMessage();
				return;
			}
			
			mFirstname.setText(mFirstname.getText().toString().trim());
			mLastname.setText(mLastname.getText().toString().trim());

			if (isValidInputs()) {
				mProgressDialog.show();
				String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
				mEditProfileAsync.url(getApiUrl() + USER_UPDATE_URI).addParam("access_token", accessToken).addParam("first_name", mFirstname.getText().toString())
						.addParam("last_name", mLastname.getText().toString()).addParam("email", mEmailString);

				if (!isEditTextEmpty(mOldPassword) && !isEditTextEmpty(mNewPassword)) {
					mEditProfileAsync.addParam("old_password", mOldPassword.getText().toString());
					mEditProfileAsync.addParam("password", mNewPassword.getText().toString());
				}

				if (mBitmap != null) {
					if (!isEditTextEmpty(mOldPassword) && !isEditTextEmpty(mNewPassword)) {
						mEditProfileAsync.addParam("old_password", mOldPassword.getText().toString());
						mEditProfileAsync.addParam("password", mNewPassword.getText().toString());
					}

					if (mBitmap != null) {
						ByteArrayOutputStream stream = new ByteArrayOutputStream();

						if (mPath != null) {
							if (mPath.endsWith(".png")) {
								orientProfileImage(mPath);
								mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
							} else {
								mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
							}
						}

						mEditProfileAsync.addParam("image", stream.toByteArray());
					}
				}
				mEditProfileAsync.post();
			}
			break;
		case R.id.tvwEditProfilePic:
			mImageAlertDialog.show();
			break;
		}
	}

	@Override
	public void onAsyncStart() {

	}

	@Override
	public void onAsyncFail(int status, String message) {
		mProgressDialog.dismiss();

		try {
			JSONObject jsonObject = new JSONObject(message);

			if (jsonObject.has("error_description")) {
				mAlertDialog.setMessage(jsonObject.getString("error_description"));
				mAlertDialog.show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAsyncSuccess(JSONObject result) {
		mProgressDialog.dismiss();
		mAlertDialog.setMessage(getString(R.string.update_success));
		mAlertDialog.show();

        mNewPassword.setText("");
        mConfirmPassword.setText("");
        mOldPassword.setText("");

		UserProfile userProfile = getLifeTrakApplication().getUserProfile();
		
		List<UserProfile> userProfiles = DataSource.getInstance(getActivity())
													.getReadOperation()
													.query("_id = ?", String.valueOf(userProfile.getId()))
													.getResults(UserProfile.class);
		
		if (userProfiles.size() > 0)
			userProfile = userProfiles.get(0);

		userProfile.setContext(getActivity());
		userProfile.setFirstname(mFirstname.getText().toString());
		userProfile.setLastname(mLastname.getText().toString());
		userProfile.setEmail(mEmailString);
		
		mPreferenceWrapper.setPreferenceStringValue(FIRST_NAME, userProfile.getFirstname())
							.setPreferenceStringValue(LAST_NAME, userProfile.getLastname())
							.setPreferenceStringValue(EMAIL, userProfile.getEmail())
							.synchronize();

		if (mBitmap != null) {
			try {
				String image = result.getJSONObject("result").getString("image");
				userProfile.setProfileImageWeb(image);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		if (mPath != null) {
			userProfile.setProfileImageLocal(mPath);
		}

		userProfile.update();
		getLifeTrakApplication().setUserProfile(userProfile);

		MainActivity activity = (MainActivity) getActivity();
		activity.updateMainMenu();
	}

	private boolean isValidInputs() {
		String editedString = mFirstname.getText().toString();
		editedString = editedString.trim().replaceAll(" +", " ");
		mFirstname.setText(editedString);
		
		String editedString2 = mLastname.getText().toString();
		editedString2 = editedString2.trim().replaceAll(" +", " ");
		mLastname.setText(editedString2);
		
		if (isEditTextEmpty(mFirstname)) {
			mAlertDialog.setMessage(getString(R.string.enter_input, getString(R.string.firstname)));
			mAlertDialog.show();
			return false;
		} else if (isEditTextEmpty(mLastname)) {
			mAlertDialog.setMessage(getString(R.string.enter_input, getString(R.string.lastname)));
			mAlertDialog.show();
			return false;
		} else if (!isEditTextEmpty(mFirstname) && mFirstname.getText().length() < 2) {
			mAlertDialog.setMessage(getString(R.string.firstname_minimum_chars));
			mAlertDialog.show();
			return false;
		} else if (!isEditTextEmpty(mLastname) && mLastname.getText().length() < 2) {
			mAlertDialog.setMessage(getString(R.string.lastname_minimum_chars));
			mAlertDialog.show();
			return false;
		}/* else if (!isValidName(mFirstname.getText().toString())) {
			mAlertDialog.setMessage(getString(R.string.invalid_first_name));
			mAlertDialog.show();
			return false;
		} else if (!isValidName(mLastname.getText().toString())) {
			mAlertDialog.setMessage(getString(R.string.invalid_last_name));
			mAlertDialog.show();
			return false;
		} */
		else if (isEditTextEmpty(mOldPassword) && isEditTextEmpty(mNewPassword) && isEditTextEmpty(mConfirmPassword)){
			return true;
		}
		else if (isEditTextEmpty(mOldPassword) && !isEditTextEmpty(mNewPassword) && !isEditTextEmpty(mOldPassword)) {
			mAlertDialog.setMessage(getString(R.string.enter_input, getString(R.string.old_password)));
			mAlertDialog.show();
			return false;
		} else if(!isEditTextEmpty(mNewPassword) && (mNewPassword.getText().length() < 6 || mNewPassword.getText().length() > 30)) {
			mAlertDialog.setMessage(getString(R.string.password_min_max_chars));
			mAlertDialog.show();
			return false;
		} else if (!mNewPassword.getText().toString().equals(mConfirmPassword.getText().toString())) {
			mAlertDialog.setMessage(getString(R.string.password_not_matched));
			mAlertDialog.show();
			return false;
		} else if (mOldPassword.getText().length() < 6 || mNewPassword.getText().length() < 6 || mConfirmPassword.getText().length() < 6 ) {
			mAlertDialog.setMessage(getString(R.string.password_min_max_chars));
			mAlertDialog.show();
			return false;
		}
		return true;
	}

	private boolean isValidName(String name) {
/*<<<<<<< HEAD
		String regex = "^[a-zA-Z,-.][a-zA-Z,-.]*$"; //^[a-zA-Z0-9][a-zA-Z0-9]*$+%
		return name.matches(regex);
=======
		return !name.contains("  ");
>>>>>>> server*/
        return !name.contains("  ");
	}

	private boolean isEditTextEmpty(EditText txt) {
		return txt.getText().toString().isEmpty();
	}

	private boolean isValidEmail(String email) {
		String regex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
		return email.matches(regex);
	}

	@SuppressLint("SimpleDateFormat")
	private File createTempFile() throws IOException {
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		if (!storageDir.exists()) {
			if (!storageDir.mkdirs()) {
				LifeTrakLogger.info("failed to create directory");
				return null;
			}
		}
		String timeStamp = new SimpleDateFormat("yyyyMMdd_hhmmss").format(new Date());
		String imageFilename = storageDir.getPath() + File.separator + "JPEG_" + timeStamp + "_.jpeg";

		File image = new File(imageFilename);
		return image;
	}

	Uri mUriFile;

	private void startCamera(File output) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
			if (output != null) {
				mPath = output.getAbsolutePath();
				mUriFile = Uri.fromFile(output);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriFile);
				startActivityForResult(intent, REQUEST_CODE_IMAGE_CAMERA);
			}
		}
	}

	private void showPictureFromLocal(String path) {
		orientProfileImage(path);
		mBitmap = createSquareBitmap(mBitmap);
		mProfilePic.setImageBitmap(makeRoundedBitmap(mBitmap, (int) dpToPx(100), (int) dpToPx(100)));
	}

	private void showPictureFromWeb(final String imageUrl) {
		if (NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
			DownloadImageTask imageTask = new DownloadImageTask() {
				@Override
				protected void onPostExecute(Bitmap result) {
					if (null != result && mBitmap == null) {
						mProfilePic.setImageBitmap(makeRoundedBitmap(result, (int) dpToPx(100), (int) dpToPx(100)));
					}
				}
			};
			mProfilePic.setTag(imageUrl);
			imageTask.execute(mProfilePic);
		}

		// new Thread(new Runnable() {
		// public void run() {
		// try {
		// URL url = new URL(imageUrl);
		// HttpURLConnection urlConnection = (HttpURLConnection)
		// url.openConnection();
		// urlConnection.connect();
		//
		// InputStream is = urlConnection.getInputStream();
		// final Bitmap bitmap = BitmapFactory.decodeStream(is);
		//
		// getActivity().runOnUiThread(new Runnable() {
		// public void run() {
		// mProfilePic.setImageBitmap(makeRoundedBitmap(bitmap, (int)
		// dpToPx(100), (int) dpToPx(100)));
		// }
		// });
		// } catch (MalformedURLException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }).start();
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

	private static int rotateXDegrees = 0;

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
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			try {
				// NOTE: Do NOT SET: intent.putExtra(MediaStore.EXTRA_OUTPUT,
				// cameraPicUri)
				// on Samsung Galaxy S2/S3/.. for the following reasons:
				// 1.) it will break the correct picture orientation
				// 2.) the photo will be stored in two locations (the given path
				// and, additionally, in the MediaStore)
				String manufacturer = android.os.Build.MANUFACTURER.toLowerCase(Locale.ENGLISH);
				String model = android.os.Build.MODEL.toLowerCase(Locale.ENGLISH);
				String buildType = android.os.Build.TYPE.toLowerCase(Locale.ENGLISH);
				String buildDevice = android.os.Build.DEVICE.toLowerCase(Locale.ENGLISH);
				String buildId = android.os.Build.ID.toLowerCase(Locale.ENGLISH);
				// String sdkVersion =
				// android.os.Build.VERSION.RELEASE.toLowerCase(Locale.ENGLISH);

				boolean setPreDefinedCameraUri = false;
				if (!(manufacturer.contains("samsung")) && !(manufacturer.contains("sony"))) {
					setPreDefinedCameraUri = true;
				}
				if (manufacturer.contains("samsung") && model.contains("galaxy nexus")) { // TESTED
					setPreDefinedCameraUri = true;
				}
				if (manufacturer.contains("samsung") && model.contains("gt-n7000") && buildId.contains("imm76l")) { // TESTED
					setPreDefinedCameraUri = true;
				}

				if (buildType.contains("userdebug") && buildDevice.contains("ariesve")) { // TESTED
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug") && buildDevice.contains("crespo")) { // TESTED
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug") && buildDevice.contains("gt-i9100")) { // TESTED
					setPreDefinedCameraUri = true;
				}

				// /////////////////////////////////////////////////////////////////////////
				// TEST
				if (manufacturer.contains("samsung") && model.contains("sgh-t999l")) { // T-Mobile
																						// LTE
																						// enabled
																						// Samsung
																						// S3
					setPreDefinedCameraUri = true;
				}
				if (buildDevice.contains("cooper")) {
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug") && buildDevice.contains("t0lte")) {
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug") && buildDevice.contains("kot49h")) {
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug") && buildDevice.contains("t03g")) {
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug") && buildDevice.contains("gt-i9300")) {
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug") && buildDevice.contains("gt-i9195")) {
					setPreDefinedCameraUri = true;
				}
				if (buildType.contains("userdebug") && buildDevice.contains("xperia u")) {
					setPreDefinedCameraUri = true;
				}

				// /////////////////////////////////////////////////////////////////////////

				dateCameraIntentStarted = new Date();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (setPreDefinedCameraUri) {
					String filename = System.currentTimeMillis() + ".jpg";
					ContentValues values = new ContentValues();
					values.put(MediaStore.Images.Media.TITLE, filename);
					preDefinedCameraUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, preDefinedCameraUri);
				}
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
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
		//mBitmap = BitmapFactory.decodeFile(mPath);
		/*mBitmap = shrinkBitmap(mBitmap, 300, rotateXDegrees);
		mProfilePic.setImageBitmap(makeRoundedBitmap(mBitmap, (int) dpToPx(100), (int) dpToPx(100)));*/
		orientProfileImage(mPath);
		mBitmap = createSquareBitmap(mBitmap);
		mProfilePic.setImageBitmap(makeRoundedBitmap(mBitmap, (int) dpToPx(100), (int) dpToPx(100)));
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
		Toast.makeText(getActivity(), getString(R.string.error_could_not_take_photo), Toast.LENGTH_LONG).show();
	}

	/**
	 * Being called if the SD card (or the internal mass storage respectively)
	 * is not mounted.
	 */
	protected void onSdCardNotMounted() {
		Toast.makeText(getActivity(), getString(R.string.error_sd_card_not_mounted), Toast.LENGTH_LONG).show();
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
	protected void onCameraIntentResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == getActivity().RESULT_OK) {
			Cursor myCursor = null;
			Date dateOfPicture = null;
			try {
				// Create a Cursor to obtain the file Path for the large image
				String[] largeFileProjection = { MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.ORIENTATION, MediaStore.Images.ImageColumns.DATE_TAKEN };
				String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
				myCursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort);
				myCursor.moveToFirst();
				if (!myCursor.isAfterLast()) {
					// This will actually give you the file path location of the
					// image.
					String largeImagePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
					photoUri = Uri.fromFile(new File(largeImagePath));
					if (photoUri != null) {
						dateOfPicture = new Date(myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)));
						if (dateOfPicture != null && dateOfPicture.after(dateCameraIntentStarted)) {
							rotateXDegrees = myCursor.getInt(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
						} else {
							photoUri = null;
						}
					}
					if (myCursor.moveToNext() && !myCursor.isAfterLast()) {
						String largeImagePath3rdLocation = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
						Date dateOfPicture3rdLocation = new Date(myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)));
						if (dateOfPicture3rdLocation != null && dateOfPicture3rdLocation.after(dateCameraIntentStarted)) {
							photoUriIn3rdLocation = Uri.fromFile(new File(largeImagePath3rdLocation));
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
				if (photoUri != null && new File(photoUri.getPath()).length() <= 0) {
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
					if (photoUriIn3rdLocation.equals(photoUri) || photoUriIn3rdLocation.equals(preDefinedCameraUri)) {
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
			if (cameraPicUri != null && cameraPicUri.toString().startsWith("content")) {
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = getActivity().getContentResolver().query(cameraPicUri, proj, null, null, null);
				cursor.moveToFirst();
				// This will actually give you the file path location of the
				// image.
				String largeImagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
				return Uri.fromFile(new File(largeImagePath));
			}
			return cameraPicUri;
		} catch (Exception e) {
			return cameraPicUri;
		}
	}

}
