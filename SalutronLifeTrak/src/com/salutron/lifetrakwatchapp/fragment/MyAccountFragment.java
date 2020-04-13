package com.salutron.lifetrakwatchapp.fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import roboguice.inject.InjectView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.facebook.Session;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.IntroductionActivity;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.WelcomePageActivity;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.fragment.dialog.AlertDialogFragment;
import com.salutron.lifetrakwatchapp.model.ActivityAlertSetting;
import com.salutron.lifetrakwatchapp.model.CalibrationData;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.NightLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.Notification;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.SleepSetting;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.StatisticalDataPoint;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.WakeupSetting;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.model.WorkoutSettings;
import com.salutron.lifetrakwatchapp.service.GoogleFitSyncService;
import com.salutron.lifetrakwatchapp.util.AmazonTransferUtility;
import com.salutron.lifetrakwatchapp.util.GoogleApiClientManager;
import com.salutron.lifetrakwatchapp.util.GoogleFitHelper;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.LifeTrakSyncR450;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;
import com.salutron.lifetrakwatchapp.util.RealPathUtil;
import com.salutron.lifetrakwatchapp.view.HeightPickerView;
import com.salutron.lifetrakwatchapp.web.AsyncListener;
import com.salutron.lifetrakwatchapp.web.DownloadImageTask;
import com.salutron.lifetrakwatchapp.web.EditProfileAsync;
import com.salutron.lifetrakwatchapp.web.ServerRestoreAsync;
import com.salutron.lifetrakwatchapp.web.ServerSyncAsync;
import com.salutron.lifetrakwatchapp.web.ServerSyncAsyncS3Amazon;
import com.salutron.lifetrakwatchapp.web.ServerSyncAsyncTask;
import com.salutron.lifetrakwatchapp.web.TwoWaySyncAsyncTask;
import com.salutron.lifetrakwatchapp.web.TwoWaySyncChecker;

public class 	MyAccountFragment extends BaseFragment implements View.OnClickListener, View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener, AsyncListener,
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, TextWatcher {
	@InjectView(R.id.imgProfilePic)
	private ImageView mProfilePic;
	@InjectView(R.id.tvwUserName)
	private TextView mUserName;
	@InjectView(R.id.tvwUserEmail)
	private TextView mUserEmail;
	@InjectView(R.id.tvwWeightValue)
	private EditText mWeightValue;
	@InjectView(R.id.tvwWeightUnit)
	private TextView mWeightUnit;
	@InjectView(R.id.tvwHeightValue)
	private EditText mHeightValue;
	@InjectView(R.id.tvwHeightUnit)
	private TextView mHeightUnit;
	@InjectView(R.id.tvwBirthdayValue)
	private TextView mBirthdayValue;
	@InjectView(R.id.rdgGender)
	private RadioGroup mGenderGroup;
	@InjectView(R.id.radMale)
	private RadioButton mGenderMale;
	@InjectView(R.id.radFemale)
	private RadioButton mGenderFemale;
	@InjectView(R.id.tvwCloudLastSync)
	private TextView mCloudLastSync;
	@InjectView(R.id.tvwWatchLastSync)
	private TextView mWatchLastSync;
	@InjectView(R.id.imgWatchImage)
	private ImageView mWatchImage;
	@InjectView(R.id.tvwWatchModel)
	private TextView mWatchModel;
	@InjectView(R.id.tvwLastSyncDate)
	private TextView mLastSyncDate;
	@InjectView(R.id.edtWatchName)
	private TextView mWatchName;
	@InjectView(R.id.swtEnableSyncToCloud)
	private Switch mEnableSyncToCloud;

	@InjectView(R.id.swtEnableSyncToGoogleFit)
	private Switch googleFitSwitch;

	@InjectView(R.id.enableGoogleFitProgress)
	private ProgressBar googleFitProgress;
	@InjectView(R.id.relative_weight)
	private RelativeLayout mRelativeWeight;
	@InjectView(R.id.textview_first_name)
	private TextView mFirstName;
	@InjectView(R.id.textview_first_name_edit)
	private EditText mFirstNameEdit;

	@InjectView(R.id.textview_last_name)
	private TextView mLastName;
	@InjectView(R.id.linear_for_password)
	private LinearLayout linearPassword;
	@InjectView(R.id.textview_logged_in_as)
	private TextView mLoggedInAs;
	@InjectView(R.id.textview_save_password)
	private TextView mTextViewSavePassword;

	@InjectView(R.id.edittext_old_password)
	private EditText mOldPassword;
	@InjectView(R.id.edittext_new_password)
	private EditText mNewPassword;
	@InjectView(R.id.edittext_confirm_password)
	private EditText mSavePassword;

	@InjectView(R.id.textview_change_profile_pic)
	private  TextView mChangeProfilePic;

	@InjectView(R.id.rel_fname_edit)
	private RelativeLayout mRelativeFnameEdit;
	@InjectView(R.id.rel_fname)
	private RelativeLayout mRelativeFname;
	@InjectView (R.id.button_fname_save)
	private Button mButtonFNameSave;
	@InjectView (R.id.button_fname_cancel)
	private Button mButtonFNameCancel;


	@InjectView(R.id.rel_lname_edit)
	private RelativeLayout mRelativeLnameEdit;
	@InjectView(R.id.rel_lname)
	private RelativeLayout mRelativeLname;
	@InjectView (R.id.button_lname_save)
	private Button mButtonLNameSave;
	@InjectView (R.id.button_lname_cancel)
	private Button mButtonLNameCancel;
	@InjectView(R.id.textview_last_name_edit)
	private EditText mLastNameEdit;

	@InjectView(R.id.btnSaveChangesProfile)
	private Button mbtnSaveChangesProfile;

	private final Intent mGalleryIntent = new Intent();

	private EditProfileAsync<JSONObject> mEditProfileAsync;
	private ServerSyncAsyncS3Amazon mServerSyncAsyncAmazon;
	private ServerSyncAsync mServerSyncAsync;
	private ServerSyncAsyncTask mServerSyncAsyncTask;
    private TwoWaySyncAsyncTask  mTwoWaySyncAsyncTask ;
    private ServerRestoreAsync<JSONObject> mServerRestoreAsync;


	private final int API_REQUEST_SEND = 0x01;
	private final int API_REQUEST_STORE = 0x02;
	private int mCurrentApiRequest = API_REQUEST_SEND;

	private String mPath;

	private ProgressDialog mProgressDialog;
	private AlertDialog mAlertDialog;
	private AlertDialog mAlertDialogForValidInputs;
	private AlertDialog mImageAlertDialog;
	private AlertDialog mAlertDialogProfile;
	private Watch mWatch;
	private UserProfile mUserProfile;
	private final InputFilter[] mInputFilter = new InputFilter[1];
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private final SimpleDateFormat mDateFormat2 = (SimpleDateFormat) DateFormat.getInstance();
	private final SimpleDateFormat mDateTimeFormat = (SimpleDateFormat) DateFormat.getInstance();
	private HeightPickerView mHeightPickerView;
	private DatePickerDialog mDatePickerDialog;
	private final int OPERATION_SYNC_TO_CLOUD = 0x01;
	private final int OPERATION_REFRESH_TOKEN = 0x02;
    private final int OPERATION_GET_USERID = 0x03;
    private final int OPERATION_CHECK_SERVERTIME = 0x04;
    private final int OPERATION_RESTORE_FROM_CLOUD = 0x05;
	private final int OPERATION_EDIT_PROFILE = 0x06;
	private final int OPERATION_BULK_INSERT_S3 = 0x07;


	private int mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
	private Bitmap mBitmap;
	public Calendar baseLineCalendar;
	public long maxSelectedMilis;
	private int dataheightValue = 0;
	private int dataWeightValue = 0;
	private int dataBirthYear = 0;
	private int dataBirthMonth = 0;
	private int dataBirthDay = 0;
	private int gender = GENDER_MALE;
	private int unitSystem = 0;
	private String dataFirstName = "";
	private String dataLastName = "";

	boolean flag_edit_profile_change = false;


	/**
	 * Google API Client timeout in seconds
	 */
	private static final long GOOGLE_API_CLIENT_TIMEOUT = 60;

	private GoogleApiClientManager googleApiClientManager;
	private GoogleApiClient googleApiClient;

	private boolean googleFitDisableOnConnect;

	private int retryCounter = 0;
	private int indexListCounter = 0;
	private String uuid;

	private List<StatisticalDataHeader> dataHeaders;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		final View view = inflater.inflate(R.layout.fragment_my_account, container, false);
		baseLineCalendar = Calendar.getInstance();
		maxSelectedMilis = baseLineCalendar.getTimeInMillis();
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		googleApiClientManager = ((GoogleApiClientManager.Provider) getActivity()).getGoogleApiClientManager();
		googleApiClient = googleApiClientManager.getClient();

		//java.util.logging.Logger.getLogger("com.amazonaws").setLevel(java.util.logging.Level.FINEST);

		((MainActivity)getActivity()).hideSoftKeyboard();
		FlurryAgent.logEvent("MyAccounts_Page");
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setMessage(getString(R.string.syncing_to_server));
		mProgressDialog.setCancelable(false);

		mServerSyncAsyncAmazon = new ServerSyncAsyncS3Amazon(getActivity());
		mServerSyncAsyncAmazon.setAsyncListener(this);
		mServerSyncAsync = new ServerSyncAsync(getActivity());
		mServerSyncAsync.setAsyncListener(this);

		mServerSyncAsyncTask = new ServerSyncAsyncTask(getString(R.string.request_failed));

		mServerRestoreAsync = new ServerRestoreAsync<JSONObject>(getActivity());
		mServerRestoreAsync.setAsyncListener(this);

		mEditProfileAsync = new EditProfileAsync<JSONObject>(getActivity());
		mEditProfileAsync.setAsyncListener(this);

		//String name = getLifeTrakApplication().getUserProfile().getFirstname() + " " + getLifeTrakApplication().getUserProfile().getLastname();
		//String email = getLifeTrakApplication().getUserProfile().getEmail();
		String name = mPreferenceWrapper.getPreferenceStringValue(FIRST_NAME) + " " + mPreferenceWrapper.getPreferenceStringValue(LAST_NAME);
		String email = mPreferenceWrapper.getPreferenceStringValue(EMAIL);
		dataFirstName = mPreferenceWrapper.getPreferenceStringValue(FIRST_NAME);
		dataLastName = mPreferenceWrapper.getPreferenceStringValue(LAST_NAME);

		//mUserName.setText(name);
		//mUserEmail.setText(email);
		mTextViewSavePassword.setOnClickListener(this);
		mChangeProfilePic.setOnClickListener(this);

		try{
			mFirstName.setText(dataFirstName);
			mFirstNameEdit.setText(dataFirstName);
			mFirstNameEdit.setSelection(dataFirstName.length());
			mFirstNameEdit.addTextChangedListener(this);

			mLastName.setText(dataLastName);
			mLastNameEdit.setText(dataLastName);
			mLastNameEdit.setSelection(dataLastName.length());
			mLastNameEdit.addTextChangedListener(this);
			mLoggedInAs.setText(email);
		}
		catch (Exception e){
			LifeTrakLogger.info("Exception : " + e.getLocalizedMessage());
		}

		mButtonFNameSave.setOnClickListener(this);
		mFirstNameEdit.setOnFocusChangeListener(this);
		mFirstNameEdit.setOnClickListener(this);
		mFirstName.setOnClickListener(this);
		mButtonFNameCancel.setOnClickListener(this);

		mSavePassword.setOnClickListener(this);

		mbtnSaveChangesProfile.setOnClickListener(this);

		mLastName.setOnClickListener(this);
		mLastNameEdit.setOnFocusChangeListener(this);
		mLastNameEdit.setOnClickListener(this);
		mButtonLNameSave.setOnClickListener(this);
		mButtonLNameCancel.setOnClickListener(this);

		if (mPreferenceWrapper.getPreferenceBooleanValue(IS_FACEBOOK)) {
			linearPassword.setVisibility(View.GONE);
		} else {
			linearPassword.setVisibility(View.VISIBLE);
		}

		// Google Fit
		final boolean googleFitEnabled = mPreferenceWrapper.getPreferenceBooleanValue(GOOGLE_FIT_ENABLED);
		setGoogleFitEnabled(googleFitEnabled);
		if (googleFitEnabled && !googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
			setGoogleFitOperationInProgress(true);
			googleApiClient.connect();
		} else {
			setGoogleFitOperationInProgress(false);
		}

		((ScrollView) getView().findViewById(R.id.scroll1)).requestFocus();
		mOldPassword.addTextChangedListener(this);
		mNewPassword.addTextChangedListener(this);
		mSavePassword.addTextChangedListener(this);

		LifeTrakLogger.configure();
	}

	@Override
	public void onStart() {
		super.onStart();
		googleApiClientManager.registerConnectionCallbacks(this);
		googleApiClientManager.registerConnectionFailedListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		googleApiClientManager.unregisterConnectionCallbacks(this);
		googleApiClientManager.unregisterConnectionFailedListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		initializeObjects();
		mWeightValue.clearFocus();
	}

	public void initializeObjects() {
		mWatch = getLifeTrakApplication().getSelectedWatch();
		mUserProfile = getLifeTrakApplication().getUserProfile();

		//mWatch.setContext(getActivity());
		//mUserProfile.setContext(getActivity());
		mDateFormat.applyPattern("MMM dd,yyyy");






		getView().findViewById(R.id.imgSyncToCloud).setOnClickListener(this);
		getView().findViewById(R.id.imgSyncToWatch).setOnClickListener(this);
		getView().findViewById(R.id.tvwEditAccount).setOnClickListener(this);
		getView().findViewById(R.id.btnLogout).setOnClickListener(this);
		getView().findViewById(R.id.tvwSwitchNewWatch).setOnClickListener(this);


		mGalleryIntent.setType("image/*");
		mGalleryIntent.setAction(Intent.ACTION_GET_CONTENT);

		mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}
		}).create();



		mAlertDialogForValidInputs = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
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

		mProgressDialog.setMessage(getString(R.string.syncing_to_server));
		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				mDateFormat2.applyPattern("MMM dd,yyyy hh:mm aa");
				mDateTimeFormat.applyPattern("hh:mm aa");
				break;
			case TIME_FORMAT_24_HR:
				mDateFormat2.applyPattern("MMM dd,yyyy HH:mm");
				mDateTimeFormat.applyPattern("HH:mm");
				break;
		}

		double weightValue = (double) mUserProfile.getWeight();
		double heightValue = (double) mUserProfile.getHeight();

		if (mUserProfile.getGender()  == GENDER_MALE){
			mGenderMale.setChecked(true);
			mGenderFemale.setChecked(false);
		}
		else{
			mGenderFemale.setChecked(true);
			mGenderMale.setChecked(false);
		}


		dataheightValue = (int) mUserProfile.getHeight();
		dataWeightValue = (int) mUserProfile.getWeight();

		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.DAY_OF_MONTH, mUserProfile.getBirthDay());
		calendar.set(Calendar.MONTH, mUserProfile.getBirthMonth() - 1);
		calendar.set(Calendar.YEAR, mUserProfile.getBirthYear());
		dataBirthDay = mUserProfile.getBirthDay();
		dataBirthYear = mUserProfile.getBirthYear();
		dataBirthMonth = mUserProfile.getBirthMonth();
		gender = mUserProfile.getGender();

		mBirthdayValue.setText(mDateFormat.format(calendar.getTime()));

		unitSystem = mUserProfile.getUnitSystem();
		switch (mUserProfile.getUnitSystem()) {
		case UNIT_IMPERIAL:
			double feetValue = Math.floor(heightValue / FEET_CM);
			double inchValue = (heightValue / INCH_CM) - (feetValue * 12);

			if (Math.round(inchValue) == 12) {
				feetValue++;
				inchValue = 0;
			}

			if (weightValue < 400 && weightValue > 44){
				mWeightValue.setText(String.format("%d",  (int) Math.round(weightValue)));
				mWeightValue.setHint(String.format("%d",  (int) Math.round(weightValue)));
			}
			else if (weightValue > 400){
				mWeightValue.setText(String.format("%d",  400));
				mWeightValue.setHint(String.format("%d",  400));
			}
			else{
				mWeightValue.setText(String.format("%d",  44));
				mWeightValue.setHint(String.format("%d",  44));
			}
			mWeightUnit.setText("lbs");
			String heightImperial = ""+ ((int) feetValue) + "'" + ((int) Math.round(inchValue));
			mHeightValue.setText(heightImperial);
			mHeightUnit.setText("");
			mHeightValue.setFocusable(false);
			break;
		case UNIT_METRIC:
			if (Math.round(weightValue * KG) < 200 && Math.round(weightValue * KG) > 20){
				mWeightValue.setText(String.format("%d",  Math.round(weightValue * KG)));
				mWeightValue.setHint(String.format("%d",  Math.round(weightValue * KG)));
			}
			else if (Math.round(weightValue * KG) >= 200){
				mWeightValue.setText(String.format("%d",  200));
				mWeightValue.setHint(String.format("%d",  200));
			}
			else{
				mWeightValue.setText(String.format("%d",  20));
				mWeightValue.setHint(String.format("%d",  20));
			}
			//			mWeightValue.setText(String.format("%d", Math.round(weightValue * KG) > 200 ? 200 : Math.round(weightValue * KG)));
			//			mWeightValue.setText(String.format("%d", Math.round(weightValue * KG) < 20 ? 20 : Math.round(weightValue * KG)));
			mWeightUnit.setText("kg");
			mHeightValue.setText(String.valueOf((int) heightValue));
			mHeightValue.setHint(String.valueOf((int) heightValue));
			mHeightUnit.setText("cm");
			mHeightValue.setFocusable(true);

			mInputFilter[0] = new InputFilter.LengthFilter(3);
			mHeightValue.setFilters(mInputFilter);
			break;
		}



		mWatchName.setOnEditorActionListener(mWatchNameEditorAction());
        mWatchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().equals("")){
                        mWatch.setName(editable.toString());
                        mWatch.update();
                        getLifeTrakApplication().getSelectedWatch().setName(editable.toString());

                }
                else{
                    mWatchName.setText(mWatch.getName());
                }
            }
        });

		mWeightValue.addTextChangedListener(mTextWatcherWeight);
		mWeightValue.setOnFocusChangeListener(mTextFocusChangeListener);
		mWeightValue.setOnClickListener(mClickListener);
		mWeightUnit.setOnClickListener(onUnitClickListener);

		mHeightValue.addTextChangedListener(mTextWatcherHeight);
		mHeightValue.setOnFocusChangeListener(mTextFocusChangeListener);
		mHeightUnit.setOnClickListener(onUnitClickListener);

		mHeightValue.setHintTextColor(getResources().getColor(R.color.abs__primary_text_holo_light));
		mWeightValue.setHintTextColor(getResources().getColor(R.color.abs__primary_text_holo_light));

		mHeightPickerView = new HeightPickerView(getActivity());
		mHeightPickerView.setOnSelectHeightListener(mSelectHeightListener);
		mHeightPickerView.setValue(mUserProfile.getHeight());

		mHeightValue.setOnClickListener(mClickListener);
		mBirthdayValue.setOnClickListener(mClickListener);

		mWeightValue.setSelection(mWeightValue.getText().length());

		mDatePickerDialog = new DatePickerDialog(getActivity(), mDateSetListener, mUserProfile.getBirthYear(), mUserProfile.getBirthMonth() - 1, mUserProfile.getBirthDay());
		mGenderGroup.setOnCheckedChangeListener(mCheckChangedListener);
		updateSyncDates();

		UserProfile userProfile = getLifeTrakApplication().getUserProfile();

		if (null != mUserProfile.getProfileImageLocal() && !"".equals(mUserProfile.getProfileImageLocal())) {
			File file = new File(userProfile.getProfileImageLocal());

			if (file.exists()) {
				showPictureFromLocal(getLifeTrakApplication().getUserProfile().getProfileImageLocal());
			}
		} else if (null != userProfile.getProfileImageWeb() && !userProfile.getProfileImageWeb().equals("null")) {
			showPictureFromWeb(getLifeTrakApplication().getUserProfile().getProfileImageWeb());
		}

		boolean autoSync = mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC);
		mEnableSyncToCloud.setChecked(autoSync);
		mEnableSyncToCloud.setOnCheckedChangeListener(this);
		getView().findViewById(R.id.imgSyncToCloud).setVisibility((autoSync) ? View.VISIBLE : View.GONE);
	}


	private void reinitializeView(){
		mProgressDialog.setMessage(getString(R.string.syncing_to_server));
		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				mDateFormat2.applyPattern("MMM dd,yyyy hh:mm aa");
				mDateTimeFormat.applyPattern("hh:mm aa");
				break;
			case TIME_FORMAT_24_HR:
				mDateFormat2.applyPattern("MMM dd,yyyy HH:mm");
				mDateTimeFormat.applyPattern("HH:mm");
				break;
		}
		mUserProfile = getLifeTrakApplication().getUserProfile();
		double weightValue = (double) mUserProfile.getWeight();
		double heightValue = (double) mUserProfile.getHeight();

		if (mUserProfile.getGender()  == GENDER_MALE){
			mGenderMale.setChecked(true);
			mGenderFemale.setChecked(false);
		}
		else{
			mGenderFemale.setChecked(true);
			mGenderMale.setChecked(false);
		}


		dataheightValue = (int) mUserProfile.getHeight();
		dataWeightValue = (int) mUserProfile.getWeight();

		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.DAY_OF_MONTH, mUserProfile.getBirthDay());
		calendar.set(Calendar.MONTH, mUserProfile.getBirthMonth() - 1);
		calendar.set(Calendar.YEAR, mUserProfile.getBirthYear());
		dataBirthDay = mUserProfile.getBirthDay();
		dataBirthYear = mUserProfile.getBirthYear();
		dataBirthMonth = mUserProfile.getBirthMonth();

		mBirthdayValue.setText(mDateFormat.format(calendar.getTime()));


		switch (mUserProfile.getUnitSystem()) {
			case UNIT_IMPERIAL:
				double feetValue = Math.floor(heightValue / FEET_CM);
				double inchValue = (heightValue / INCH_CM) - (feetValue * 12);

				if (Math.round(inchValue) == 12) {
					feetValue++;
					inchValue = 0;
				}

				if (weightValue < 400 && weightValue > 44){
					mWeightValue.setText(String.format("%d",  (int) Math.round(weightValue)));
					mWeightValue.setHint(String.format("%d",  (int) Math.round(weightValue)));
				}
				else if (weightValue > 400){
					mWeightValue.setText(String.format("%d",  400));
					mWeightValue.setHint(String.format("%d",  400));
				}
				else{
					mWeightValue.setText(String.format("%d",  44));
					mWeightValue.setHint(String.format("%d",  44));
				}
				mWeightUnit.setText("lbs");
				String heightImperial = ""+ ((int) feetValue) + "'" + ((int) Math.round(inchValue));
				mHeightValue.setText(heightImperial);
				mHeightUnit.setText("");
				mHeightValue.setFocusable(false);
				break;
			case UNIT_METRIC:
				if (Math.round(weightValue * KG) < 200 && Math.round(weightValue * KG) > 20){
					mWeightValue.setText(String.format("%d",  Math.round(weightValue * KG)));
					mWeightValue.setHint(String.format("%d",  Math.round(weightValue * KG)));
				}
				else if (Math.round(weightValue * KG) > 200){
					mWeightValue.setText(String.format("%d",  200));
					mWeightValue.setHint(String.format("%d",  200));
				}
				else{
					mWeightValue.setText(String.format("%d",  20));
					mWeightValue.setHint(String.format("%d",  20));
				}
				//			mWeightValue.setText(String.format("%d", Math.round(weightValue * KG) > 200 ? 200 : Math.round(weightValue * KG)));
				//			mWeightValue.setText(String.format("%d", Math.round(weightValue * KG) < 20 ? 20 : Math.round(weightValue * KG)));
				mWeightUnit.setText("kg");
				mHeightValue.setText(String.valueOf((int) heightValue));
				mHeightValue.setHint(String.valueOf((int) heightValue));
				mHeightUnit.setText("cm");
				mHeightValue.setFocusable(true);

				mInputFilter[0] = new InputFilter.LengthFilter(3);
				mHeightValue.setFilters(mInputFilter);
				break;
		}
	}
	/*
	 * Create editor listener to check for the device's name
	 * If it's blank then return previous value
	 */
	private OnEditorActionListener mWatchNameEditorAction() {
		return new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					if (!v.getText().toString().trim().equals("")) {
						mWatch.setName(v.getText().toString());
						mWatch.update();
						getLifeTrakApplication().getSelectedWatch().setName(v.getText().toString());
					} else {
						mWatchName.setText(mWatch.getName());
					}
				}
				return false;
			}
		};
	}

	public void updateSyncDates() {

		if (mWatch.getCloudLastSyncDate() == null){
			LifeTrakLogger.info("mWatch.getCloudLastSyncDate() is NULL on Account Fragment");
			mWatch.setCloudLastSyncDate(new Date());
			mWatch.update();
		}
		if (mDateFormat.format(mWatch.getCloudLastSyncDate()).equals(mDateFormat.format(new Date()))) {
			mCloudLastSync.setText(getString(R.string.synced_today, mDateTimeFormat.format(mWatch.getCloudLastSyncDate())));
		} else {
			mCloudLastSync.setText(getString(R.string.synced_at, mDateFormat2.format(mWatch.getCloudLastSyncDate())));
		}

		if (mDateFormat.format(mWatch.getLastSyncDate()).equals(mDateFormat.format(new Date()))) {
			mWatchLastSync.setText(getString(R.string.synced_today, mDateTimeFormat.format(mWatch.getLastSyncDate())));
		} else {
			mWatchLastSync.setText(getString(R.string.synced_at, mDateFormat2.format(mWatch.getLastSyncDate())));
		}

		switch (mWatch.getModel()) {
		case WATCHMODEL_C300:
			mWatchImage.setImageResource(R.drawable.watch_c300_green);
			mWatchModel.setText(WATCHNAME_C300);
			break;
        case WATCHMODEL_C300_IOS:
                mWatchImage.setImageResource(R.drawable.watch_c300_green);
                mWatchModel.setText(WATCHNAME_C300);
        break;
		case WATCHMODEL_C410:
			mWatchImage.setImageResource(R.drawable.watch_c410_red);
			mWatchModel.setText(WATCHNAME_C410);
			break;
		case WATCHMODEL_R415:
			mWatchImage.setImageResource(R.drawable.watch_r415_blue);
			mWatchModel.setText(WATCHNAME_R415);
			break;
		case WATCHMODEL_R500:
			mWatchImage.setImageResource(R.drawable.watch_r500_black);
			mWatchModel.setText(WATCHNAME_R500);
			break;
		}

		if (mDateFormat.format(mWatch.getLastSyncDate()).equals(mDateFormat.format(new Date()))) {
			mLastSyncDate.setText(getString(R.string.synced_today, mDateTimeFormat.format(mWatch.getLastSyncDate())));
		} else {
			mLastSyncDate.setText(getString(R.string.synced_at, mDateFormat2.format(mWatch.getLastSyncDate())));
		}
		mWatchName.setText(mWatch.getName());
	}

	/**
	 * Called when the user chooses not to complete a provided resolution,
	 * for example by canceling a dialog, or when a network error occurs.
	 * @param connectionResult
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		setGoogleFitOperationInProgress(false);
		// Revert back to disabled state
		setGoogleFitEnabled(false);
		final int errorCode = connectionResult.getErrorCode();
		// If auth process is already in the approval of permissions and connection was lost during the process,
		// errorCode will be set to CANCELED so handle that as well
		if (errorCode == ConnectionResult.NETWORK_ERROR || errorCode == ConnectionResult.CANCELED) {
			checkNetworkAvailable();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		LifeTrakLogger.debug("MyAccountFragment.onConnected() connectionHint: " + connectionHint);
		if (googleFitDisableOnConnect) {
			googleFitDisableOnConnect = false;
			disableGoogleFit();
			return;
		}
		final boolean wasDisabled = !mPreferenceWrapper.getPreferenceBooleanValue(GOOGLE_FIT_ENABLED);
		// Perform initial sync if Google Fit was previously disabled
		if (wasDisabled) {
			GoogleFitSyncService.start(getActivity(), mWatch);
		}
		setGoogleFitOperationInProgress(false);
		setGoogleFitEnabled(true);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		LifeTrakLogger.debug("MyAccountFragment.onConnectionSuspended() cause: " + cause);
	}

	private boolean checkNetworkAvailable() {
		if (!NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
			AlertDialogFragment.newInstance(R.string.lifetrak_title, R.string.check_network_connection, R.string.ok)
					.show(getFragmentManager(), "NoNetwork");
			return false;
		}
		return true;
	}



	private void setGoogleFitOperationInProgress(boolean inProgress) {
		googleFitProgress.setVisibility(inProgress ? View.VISIBLE : View.INVISIBLE);
		googleFitSwitch.setVisibility(inProgress ? View.INVISIBLE : View.VISIBLE);
	}

	/**
	 * Ensures that all Google Fit-related controls and preference values stay in a consistent state
	 * @param enabled
	 */
	private void setGoogleFitEnabled(boolean enabled) {
		mPreferenceWrapper.setPreferenceBooleanValue(GOOGLE_FIT_ENABLED, enabled).synchronize();
		// Set the checked state without triggering OnCheckedChangeListener.onCheckedChanged()
		googleFitSwitch.setOnCheckedChangeListener(null);
		googleFitSwitch.setChecked(enabled);
		googleFitSwitch.setOnCheckedChangeListener(this);
	}

	private void disableGoogleFit() {
		setGoogleFitOperationInProgress(true);
		GoogleFitHelper.disable(googleApiClient).setResultCallback(new ResultCallback<Status>() {
			@Override
			public void onResult(Status status) {
				// Always trigger disconnection regardless of the result because:
				// 1. If this call succeeds, the connection is no longer needed
				// 2. If this call fails, it means that the connection is broken
				googleApiClient.disconnect();

				setGoogleFitOperationInProgress(false);
				// Google Fit is disabled if the operation succeeded or if it wasn't even enabled to begin with
				final boolean disabled = status.isSuccess() || status.getStatusCode() == FitnessStatusCodes.APP_NOT_FIT_ENABLED;
				setGoogleFitEnabled(!disabled);
			}
		}, GOOGLE_API_CLIENT_TIMEOUT, TimeUnit.SECONDS);
	}

	@Override
	public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
		if (buttonView == mEnableSyncToCloud) {
			mPreferenceWrapper.setPreferenceBooleanValue(AUTO_SYNC, isChecked).synchronize();
			getView().findViewById(R.id.imgSyncToCloud).setVisibility(isChecked ? View.VISIBLE : View.GONE);
		} else if (buttonView == googleFitSwitch) {
			if (!checkNetworkAvailable()) {
				// Revert back to original state and abort if network is unavailable
				setGoogleFitEnabled(!isChecked);
				return;
			}
			setGoogleFitOperationInProgress(true);
			if (isChecked) {
				if (googleApiClient.isConnected()) {
					// Already connected, nothing to do
					setGoogleFitOperationInProgress(false);
					setGoogleFitEnabled(true);
				} else if (!googleApiClient.isConnecting()) {
					// Attempt to connect, then update state in onConnected()
					googleApiClient.connect();
				}
			} else {
				if (googleApiClient.isConnected()) {
					disableGoogleFit();
				} else if (googleApiClient.isConnecting()) {
					// Requested to disable Google Fit but client is still in the process of connecting.
					// Disable it in onConnected()
					googleFitDisableOnConnect = true;
				} else {
					// Already disconnected and not connecting. Nothing left to do.
					setGoogleFitOperationInProgress(false);
					setGoogleFitEnabled(false);
				}
			}
		}
	}

	@Override
	public void onAsyncStart() {
	}

	@Override
	public void onAsyncFail(int status, String message) {
		if (mCurrentOperation == OPERATION_EDIT_PROFILE){
			if (mProgressDialog.isShowing())
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
		else if (mCurrentOperation == OPERATION_BULK_INSERT_S3){
			retryCounter ++;
			try{
				if (NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
					if (retryCounter < 3) {
						LifeTrakLogger.info("Sync Data to s3 count " + String.valueOf(indexListCounter) + " OF " + String.valueOf(dataHeaders.size()) + " Error Count = " + String.valueOf(retryCounter));
						uploadS3(getLifeTrakApplication().getSelectedWatch(), dataHeaders.get(indexListCounter));
					}else {
						LifeTrakLogger.info("Error on sync to cloud : " + message);
						mProgressDialog.dismiss();
						mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						}).create();
						mAlertDialog.show();
					}
				}
				else{
					NetworkUtil.getInstance(getActivity()).showConnectionErrorMessage();
					if (mProgressDialog.isShowing())
						mProgressDialog.dismiss();
				}
			}catch (JSONException e){

			}

		}
		else {
			if (message != null) {
				if (message.equals("Unable to retrieve device with specified mac address.")) {
					if (NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
						mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
						startSyncToServer();
					} else {
						NetworkUtil.getInstance(getActivity()).showConnectionErrorMessage();
						if (mProgressDialog.isShowing())
							mProgressDialog.dismiss();
					}
				} else {
					mProgressDialog.dismiss();
					mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					}).create();
					mAlertDialog.show();

				}
			} else {
				mProgressDialog.dismiss();
				mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(R.string.network_error).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				}).create();
				mAlertDialog.show();
			}
		}
	}

	@Override
	public void onAsyncSuccess(final JSONObject result) {
		String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);

		switch (mCurrentOperation) {
		case OPERATION_REFRESH_TOKEN:
			try {
				mPreferenceWrapper.setPreferenceStringValue(ACCESS_TOKEN, result.getString("access_token")).setPreferenceStringValue(REFRESH_TOKEN, result.getString("refresh_token"))
				.setPreferenceLongValue(EXPIRATION_DATE, result.getLong("expires")).synchronize();

				String email = mPreferenceWrapper.getPreferenceStringValue(EMAIL);

				List<UserProfile> profiles = DataSource.getInstance(getActivity()).getReadOperation().query("email = ?", email).getResults(UserProfile.class);

				if (profiles.size() > 0) {
					UserProfile profile = profiles.get(0);

					List<Watch> watches = DataSource.getInstance(getActivity()).getReadOperation().query("accessToken = ?", profile.getAccessToken()).getResults(Watch.class);

					profile.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
					profile.update();

					for (Watch watch : watches) {
						watch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
						watch.update();

						if (getLifeTrakApplication().getSelectedWatch().getId() == watch.getId())
							getLifeTrakApplication().getSelectedWatch().setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
					}
				}

//				mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
//				startSyncToServer();
                mCurrentOperation = OPERATION_CHECK_SERVERTIME;
                startCheckingServer();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case OPERATION_SYNC_TO_CLOUD:
			switch (mCurrentApiRequest) {
			case API_REQUEST_SEND:
				String macAddress = getLifeTrakApplication().getSelectedWatch().getMacAddress();
				mServerSyncAsync.url(getApiUrl() + STORE_URI).addParam("access_token", accessToken).addParam("mac_address", macAddress).post();
				/*mServerSyncAsyncTask = new ServerSyncAsyncTask();
				mServerSyncAsyncTask.listener(this)
									.addParam("access_token", accessToken)
									.addParam("mac_address", getLifeTrakApplication().getSelectedWatch().getMacAddress())
									.execute(getApiUrl() + STORE_URI);*/

				mCurrentApiRequest = API_REQUEST_STORE;
				break;
			case API_REQUEST_STORE:


				if (flag_edit_profile_change){
					syncProfile();
				}
				else{
					mProgressDialog.dismiss();
					mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					}).create();
					mAlertDialog.show();
					mCurrentApiRequest = API_REQUEST_SEND;
					updateAllDataHeaders();
					updateSyncDates();
				}
//				Watch watch = getLifeTrakApplication().getSelectedWatch();
////				watch.setContext(getActivity());
////				watch.setCloudLastSyncDate(new Date());
////				watch.update();
//				getLifeTrakApplication().setSelectedWatch(watch);
				break;
			}
			break;
            case OPERATION_GET_USERID:
                try {
                    JSONObject objectResult = result.getJSONObject("result");
                    int id = objectResult.getInt("id");
                    mPreferenceWrapper
                            .setPreferenceIntValue(USER_ID, id)
                            .synchronize();
                }catch (JSONException e) {
                    AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(result.toString())
                            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                }
                            }).create();
                    alert.show();

                }
                mCurrentOperation = OPERATION_CHECK_SERVERTIME;
                startCheckingServer();
                break;

            case OPERATION_CHECK_SERVERTIME:
                try{
                    JSONObject objectResult = result.getJSONObject("result");
                    String lastDateSynced = objectResult.getString(LAST_DATE_SYNCED);
                    SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //format.setTimeZone(TimeZone.getTimeZone("GMT"));

                    Date dateServerLastSync = format.parse(lastDateSynced);

                    Watch watch = getLifeTrakApplication().getSelectedWatch();
                    Date dTAppLastSync = watch.getCloudLastSyncDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String utcTime = sdf.format(dTAppLastSync);

                    Date dateAppLastSync = null;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try
                    {
                        dateAppLastSync = (Date)dateFormat.parse(utcTime);
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }

                    final  TwoWaySyncChecker mTwoWaySyncChecker = TwoWaySyncChecker.newInstance(getActivity(), watch);

                    boolean isServerTimeLatest = mTwoWaySyncChecker.isServerLatest(dateAppLastSync, dateServerLastSync);
					LifeTrakLogger.info(" dateAppLastSync = "+dateAppLastSync.toString());
					LifeTrakLogger.info(" dateServerLastSync = "+dateServerLastSync.toString());
                    if (!isServerTimeLatest){
                       mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
                       startSyncToServer();
                    }
                    else{
                        //Update data from server
						watch.setContext(getActivity());
						watch.setCloudLastSyncDate(new Date());
						watch.update();
						getLifeTrakApplication().setSelectedWatch(watch);
                       startRestoreFromServer(dateServerLastSync);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                break;

            case OPERATION_RESTORE_FROM_CLOUD:
                Thread mRestoreThread = new Thread(new Runnable() {
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
                            Watch watch  = getLifeTrakApplication().getSelectedWatch();
                            TwoWaySyncChecker mTwoWaySyncChecker = TwoWaySyncChecker.newInstance(getActivity(), watch);
                            List<StatisticalDataHeader> dataHeaders = mServerRestoreAsync.getStatisticalDataHeaders(arrayDataHeaders, mWatch);
                            List<WorkoutInfo> workoutInfos = mServerRestoreAsync.getWorkoutInfos(arrayWorkoutInfos, mWatch, mPreferenceWrapper.getPreferenceBooleanValue(FROM_IOS));
                            List<SleepDatabase> sleepDatabases = mServerRestoreAsync.getSleepDatabases(arraySleepDatabases, mWatch);

							WorkoutSettings workoutSettings = mServerRestoreAsync.getWorkOutSettings(objectDeviceSettings, mWatch);

                            UserProfile userProfile = mServerRestoreAsync.getUserProfile(objectUserProfile, mWatch);

                            List<Goal> goals = mServerRestoreAsync.getGoals(arrayGoals, mWatch);
                            if (goals.size() > 0){
                                mTwoWaySyncChecker.UpdateGoals(goals);
                            }

                            if(!objectResult.isNull("sleep_settings")) {
                                SleepSetting sleepSetting;
                                sleepSetting = mServerRestoreAsync.getSleepSetting(objectSleepSetting, mWatch);
                                mTwoWaySyncChecker.UpdatedSleepSettings(sleepSetting);
                            }
                            if (objectResult.has("wakeup_info") && !objectResult.isNull("wakeup_info")) {
                                WakeupSetting wakeupSetting;
                                wakeupSetting = mServerRestoreAsync.getWakeupSetting(objectWakeupSetting, mWatch);
                                mTwoWaySyncChecker.UpdateWakeupSettings(wakeupSetting);
                            }
                            if (objectResult.has("inactive_alert_settings") && !objectResult.isNull("inactive_alert_settings")) {
                                ActivityAlertSetting activityAlertSetting;
                                activityAlertSetting = mServerRestoreAsync.getActivityAlertSetting(objectInactiveAlertSetting, mWatch);
                                mTwoWaySyncChecker.UpdateInactiveAlert(activityAlertSetting);
							}
                            if (objectDayLightAlertSetting != null) {
                                DayLightDetectSetting dayLightDetectSetting;
                                dayLightDetectSetting = mServerRestoreAsync.getDayLightDetectSetting(objectDayLightAlertSetting, mWatch);
                                mTwoWaySyncChecker.UpdateDayLightSettings(dayLightDetectSetting);
                            }
                            if (objectNightLightAlertSetting != null) {
                                NightLightDetectSetting nightLightDetectSetting;
                                nightLightDetectSetting = mServerRestoreAsync.getNightLightDetectSetting(objectNightLightAlertSetting, mWatch);
                                mTwoWaySyncChecker.UpdateNightLightSettings(nightLightDetectSetting);
                            }

							mTwoWaySyncChecker.UpdateUserProfile(userProfile, getLifeTrakApplication());

                            Notification notification = mServerRestoreAsync.getNotification(objectDeviceSettings, mWatch);
                            mTwoWaySyncChecker.UpdateNotification(notification);

                            CalibrationData calibrationData = mServerRestoreAsync.getCalibrationData(objectDeviceSettings, mWatch);
                            mTwoWaySyncChecker.UpdateCalibration(calibrationData);

                            TimeDate timeDate = mServerRestoreAsync.getTimeDate(objectDeviceSettings, mWatch);
                            mTwoWaySyncChecker.UpdateTimeDate(timeDate);



                            String firstname = mPreferenceWrapper.getPreferenceStringValue(FIRST_NAME);
                            String lastname = mPreferenceWrapper.getPreferenceStringValue(LAST_NAME);
                            String email = mPreferenceWrapper.getPreferenceStringValue(EMAIL);
							getLifeTrakApplication().getUserProfile().setFirstname(firstname);
							getLifeTrakApplication().getUserProfile().setLastname(lastname);
							getLifeTrakApplication().getUserProfile().setEmail(email);
							getLifeTrakApplication().getUserProfile().setProfileImageWeb(mPreferenceWrapper.getPreferenceStringValue(PROFILE_IMG));
							getLifeTrakApplication().getUserProfile().setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
							getLifeTrakApplication().getUserProfile().update();


							mWatch.setProfileId(getLifeTrakApplication().getUserProfile().getId());
							mWatch.setAccessToken(mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN));
                            mWatch.update();


                            getLifeTrakApplication().setTimeDate(timeDate);

							if (objectDeviceSettings.optString("watch_face").toString().equals("simple")){
								getLifeTrakApplication().getTimeDate().setDisplaySize(DISPLAY_FORMAT_BIG_DIGIT);
							}
							else
							{
								getLifeTrakApplication().getTimeDate().setDisplaySize(DISPLAY_FORMAT_SMALL_DIGIT);
							}

                            DataSource.getInstance(getActivity())
                                   .getWriteOperation()
                                    .open()
                                    .beginTransaction()
                                    .deleteAll(new StatisticalDataHeader())
                                    .deleteAll(new StatisticalDataPoint())
                                    .deleteAll(new WorkoutInfo())
                                    .deleteAll(new SleepDatabase())
                                    .endTransaction()
									.close();


                            DataSource.getInstance(getActivity())
                                   .getWriteOperation()
                                    .open()
                                    .beginTransaction()
                                    .insert(dataHeaders)
                                    .insert(workoutInfos)
									.insert(workoutSettings)
									.insert(sleepDatabases)
                                    .endTransaction()
                                    .close();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        finally {
							if (flag_edit_profile_change){
								syncProfile();
							}
							else{
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										updateSyncDates();
										updateAllDataHeaders();
										//initializeObjects();
										reinitializeView();
										mProgressDialog.dismiss();
										mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(R.string.update_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface arg0, int arg1) {
												if (mProgressDialog.isShowing())
													mProgressDialog.dismiss();
												arg0.dismiss();
											}
										}).create();
										mAlertDialog.show();
									}
								});
							}
                        }
                    }
                });
                mRestoreThread.start();
                break;
			case OPERATION_EDIT_PROFILE:
				mProgressDialog.dismiss();
				mAlertDialog.setMessage(getString(R.string.update_success));
				mAlertDialog.show();

				mNewPassword.setText("");
				mSavePassword.setText("");
				mOldPassword.setText("");

				mFirstName.setText(mFirstNameEdit.getText().toString());
				mFirstNameEdit.setSelection(mFirstName.length());
//				mRelativeFnameEdit.setVisibility(View.GONE);
//				mRelativeFname.setVisibility(View.VISIBLE);

				mLastName.setText(mLastNameEdit.getText().toString());
				mLastNameEdit.setSelection(mLastName.length());
//				mRelativeLnameEdit.setVisibility(View.GONE);
//				mRelativeLname.setVisibility(View.VISIBLE);

				mChangeProfilePic.setOnClickListener(this);
				mFirstName.setOnClickListener(this);
				mLastName.setOnClickListener(this);

				mOldPassword.setOnClickListener(this);

				UserProfile userProfile = getLifeTrakApplication().getUserProfile();

				List<UserProfile> userProfiles = DataSource.getInstance(getActivity())
						.getReadOperation()
						.query("_id = ?", String.valueOf(userProfile.getId()))
						.getResults(UserProfile.class);

				if (userProfiles.size() > 0)
					userProfile = userProfiles.get(0);

				userProfile.setContext(getActivity());
				userProfile.setFirstname(mFirstName.getText().toString());
				userProfile.setLastname(mLastName.getText().toString());
				userProfile.setEmail(mPreferenceWrapper.getPreferenceStringValue(EMAIL));

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

				mProfilePic.setImageBitmap(makeRoundedBitmap(mBitmap, (int) dpToPx(100), (int) dpToPx(100)));

				MainActivity activity = (MainActivity) getActivity();
				activity.updateMainMenu();

				updateSyncDates();
				updateAllDataHeaders();
				//initializeObjects();
				reinitializeView();
				if (mProgressDialog != null)
					activity.reinitializeProgress();

				if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();

				mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(R.string.update_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (mProgressDialog.isShowing())
							mProgressDialog.dismiss();
						arg0.dismiss();
					}
				}).create();
				mAlertDialog.show();
				break;

			case OPERATION_BULK_INSERT_S3:
				indexListCounter ++;
				retryCounter = 0;
				Watch watch = getLifeTrakApplication().getSelectedWatch();
				if (dataHeaders.size()  ==  indexListCounter){
					mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
					mCurrentApiRequest = API_REQUEST_STORE;


//					mServerSyncAsync.url(getApiUrl() + STORE_URI_V2)
//							.addParam("access_token", accessToken)
//							.addParam("uuid", uuid)
//							.post();
					String mAccessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
					mServerSyncAsyncTask = new ServerSyncAsyncTask();
					mServerSyncAsyncTask.listener(this);
					mServerSyncAsyncTask.addParam("access_token", mAccessToken);
					mServerSyncAsyncTask.addParam("uuid", uuid);
					mServerSyncAsyncTask.execute(API_URL + STORE_URI_V2);
				}
				else{
					try {
						LifeTrakLogger.info("Sync Data to s3 count " + String.valueOf(indexListCounter) + " OF " + String.valueOf(dataHeaders.size()) );
						uploadS3(watch, dataHeaders.get(indexListCounter));
					}catch (JSONException e){

					}

				}
				break;


		}
	}

    private void startCheckingServer(){
           mTwoWaySyncAsyncTask = new TwoWaySyncAsyncTask();
           mTwoWaySyncAsyncTask.listener(this);
            String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
            int userId = mPreferenceWrapper.getPreferenceIntValue(USER_ID);

            if (userId == 0){
                mCurrentOperation = OPERATION_GET_USERID;

                mTwoWaySyncAsyncTask.addParam("access_token", accessToken);
                mTwoWaySyncAsyncTask.execute(API_URL + USER_URI);
            }
            else {
                mCurrentOperation = OPERATION_CHECK_SERVERTIME;
                Watch watch = getLifeTrakApplication().getSelectedWatch();
                UserProfile profile = getLifeTrakApplication().getUserProfile();
                mTwoWaySyncAsyncTask.addParam("access_token", accessToken);
                mTwoWaySyncAsyncTask.addParam("mac_address", watch.getMacAddress());
                mTwoWaySyncAsyncTask.execute(API_URL + DEVICE_DATA_URL + "/" + userId + "/" + watch.getMacAddress());
            }

    }

    private void startRestoreFromServer(final Date date) {
        mCurrentOperation = OPERATION_RESTORE_FROM_CLOUD;
		mProgressDialog.setMessage(getString(R.string.restoring_from_server));
        if(mWatch != null) {
            new Thread(new Runnable() {
                public void run() {
//                    mServerRestoreAsync.url(getApiUrl() + RESTORE_URI + "/" + date.toString() + "/" + (new Date()).toString())
//                            .addParam("access_token", mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN))
//                            .addParam("mac_address", mWatch.getMacAddress())
//                            .get();
					mServerRestoreAsync.url(API_URL + RESTORE_URI )
							.addParam("access_token", mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN))
							.addParam("mac_address", mWatch.getMacAddress())
							.get();
                }
            }).start();
        }
    }


    private void startSyncToServer() {
		new Thread(new Runnable() {
			public void run() {
				try {
					synchronized (LOCK_OBJECT) {

						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mProgressDialog.setMessage(getString(R.string.sync_to_cloud));
							}
						});

						indexListCounter = 0;
						retryCounter = 0;
						Watch watch = getLifeTrakApplication().getSelectedWatch();
						if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420 || getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415 ||
								getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_C410 || getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_C300
								|| getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_C300_IOS){
							mCurrentOperation = OPERATION_BULK_INSERT_S3;
							dataHeaders = DataSource.getInstance(getActivity())
									.getReadOperation()
									.query("watchDataHeader = ? and syncedToCloud = 0", String.valueOf(watch.getId()))
									.orderBy("dateStamp", SORT_DESC)
									.getResults(StatisticalDataHeader.class, false);

							uuid = AmazonTransferUtility.generateRandomUUID();
							if (dataHeaders.size() > 0) {
								LifeTrakLogger.info("Sync Data to s3 count " + String.valueOf(indexListCounter) + " OF " + String.valueOf(dataHeaders.size()) );
								uploadS3(watch, dataHeaders.get(indexListCounter));
							}
						}
						else{
							mCurrentOperation = OPERATION_SYNC_TO_CLOUD;

							JSONObject data = new JSONObject();
							data.put("device", mServerSyncAsync.getDevice(watch.getMacAddress()));
							data.put("workout", mServerSyncAsync.getAllWorkoutInfos(watch.getId(), mPreferenceWrapper.getPreferenceBooleanValue(FROM_IOS),watch));
							data.put("sleep", mServerSyncAsync.getAllSleepDatabases(watch.getId()));
							data.put("data_header", mServerSyncAsync.getAllDataHeaders(watch));
							data.put("device_settings", mServerSyncAsync.getDeviceSettings(watch.getId(), getLifeTrakApplication()));
							data.put("user_profile", mServerSyncAsync.getUserProfile(watch.getId(), mBirthdayValue.getText().toString()));
							data.put("goal", mServerSyncAsync.getAllGoals(watch.getId()));
							data.put("sleep_settings", mServerSyncAsync.getSleepSetting(watch.getId()));
							data.put("wakeup_info", mServerSyncAsync.getWakeupInfo(watch.getId()));

							if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
								data.put("wakeup_info", mServerSyncAsync.getWakeupInfo(watch.getId()));
								data.put("inactive_alert_settings", mServerSyncAsync.getActivityAlertSetting(watch.getId()));
								data.put("light_settings", mServerSyncAsync.getLightSetting(watch.getId()));
							}
							if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420) {
								data.put("workout_header", mServerSyncAsync.getAllWorkoutHeaders(watch.getId()));
							}

							String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);

							watch.setContext(getActivity());
							watch.setCloudLastSyncDate(new Date());
							watch.update();
							mServerSyncAsync.url(getApiUrl() + SYNC_URI).addParam("access_token", accessToken).addParam("data", data.toString()).post();

						}

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void uploadS3(Watch watch, StatisticalDataHeader dataHeader) throws  JSONException{
		JSONObject data = new JSONObject();
		data.put("device", mServerSyncAsyncAmazon.getDevice(watch.getMacAddress(), watch));
		data.put("workout", mServerSyncAsyncAmazon
				.getAllWorkoutInfos(watch.getId(), mPreferenceWrapper.getPreferenceBooleanValue(FROM_IOS), watch, dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
		data.put("sleep", mServerSyncAsyncAmazon
				.getAllSleepDatabases(watch.getId(), dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
		data.put("data_header", mServerSyncAsyncAmazon
				.getAllDataHeaders(watch, dataHeader.getDateStampDay(), dataHeader.getDateStampMonth(), dataHeader.getDateStampYear()));
		data.put("device_settings", mServerSyncAsyncAmazon.getDeviceSettings(watch.getId(), getLifeTrakApplication()));
		data.put("user_profile", mServerSyncAsyncAmazon.getUserProfile(watch.getId(), mBirthdayValue.getText().toString()));
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
				.getInstance(getActivity())
				.listener(this)
				.setUUID(uuid)
				.uploadFileToAmazonS3(data.toString(), dataHeader.getDateStamp());
	}


//	private void startSyncToSserver() {
//
//
//		mProgressDialog.setMessage(getString(R.string.sync_to_cloud));
//		mCurrentApiRequest = API_REQUEST_SEND;
//
//		mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
//
//		LifeTrakLogger.info("Start Sync to Server - " + new Date());
//
//
//			new Thread(new Runnable() {
//				public void run() {
//					synchronized(LOCK_OBJECT) {
//						try {
//
//							Watch watch = getLifeTrakApplication().getSelectedWatch();
//							watch.setContext(getActivity());
//							watch.setCloudLastSyncDate(new Date());
//							watch.update();
//							JSONObject data = new JSONObject();
//
//									data.put("device", mServerSyncAsync.getDevice(watch.getMacAddress()));
//									data.put("workout", mServerSyncAsync.getAllWorkoutInfos(watch.getId(), mPreferenceWrapper.getPreferenceBooleanValue(FROM_IOS), watch));
//									data.put("sleep", mServerSyncAsync.getAllSleepDatabases(watch.getId()));
//									data.put("data_header", mServerSyncAsync.getAllDataHeaders(watch));
//									data.put("device_settings", mServerSyncAsync.getDeviceSettings(watch.getId(), getLifeTrakApplication()));
//									data.put("user_profile", mServerSyncAsync.getUserProfile(watch.getId()));
//									data.put("goal", mServerSyncAsync.getAllGoals(watch.getId()));
//							data.put("sleep_settings", mServerSyncAsync.getSleepSetting(watch.getId()));
//
//							if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
//										data.put("wakeup_info", mServerSyncAsync.getWakeupInfo(watch.getId()));
//										data.put("inactive_alert_settings", mServerSyncAsync.getActivityAlertSetting(watch.getId()));
//										data.put("light_settings", mServerSyncAsync.getLightSetting(watch.getId()));
//								}
//
//
//								String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
//
//								LifeTrakLogger.info(" data:" +  data.toString());
//								LifeTrakLogger.info(" accesstoken:" +  accessToken);
//									mServerSyncAsync.url(API_URL + SYNC_URI).addParam("access_token", accessToken).addParam("data", data.toString()).post();
//							}
//							 catch (JSONException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}).start();
//
//
//	}


	private void refreshToken() {
		String url = getApiUrl() + REFRESH_TOKEN_URI;

		mServerSyncAsync.url(url).addParam("grant_type", "refresh_token").addParam("refresh_token", mPreferenceWrapper.getPreferenceStringValue(REFRESH_TOKEN))
		.addParam("client_id", getString(R.string.client_id)).addParam("client_secret", getString(R.string.client_secret)).post();
	}

	private final OnClickListener onUnitClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == mHeightUnit) {
				mHeightValue.requestFocus();
			} else if (v == mWeightUnit) {
				mWeightValue.requestFocus();
			}
		}
	};

	private final TextWatcher mTextWatcherWeight = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			int value = 0;

			if (!s.toString().equals("")) {
				value = Integer.parseInt(s.toString());
			} else {
				value = Integer.parseInt(mWeightValue.getHint().toString());
			}

			if (mUserProfile.getUnitSystem() == UNIT_METRIC) {
				if (value > 200) {
					value = 200;
					setValue(s, value);
				}
				value = (int) (value / KG);
			} else {
				if (value > 440) {
					value = 440;
					setValue(s, value);
				}
			}
			mUserProfile.setWeight(value);
			dataWeightValue = value;

			mUserProfile.setHeight(dataheightValue);
			mUserProfile.setBirthYear(dataBirthYear);
			mUserProfile.setBirthMonth(dataBirthMonth);
			mUserProfile.setBirthDay(dataBirthDay);
			mUserProfile.update();
			List<UserProfile> userProfiles = DataSource.getInstance(getActivity())
					.getReadOperation()
					.query("watchUserProfile = ?", String.valueOf(mWatch.getId()))
					.getResults(UserProfile.class);
			if (userProfiles.size() > 0)
			{
				UserProfile userProfile = userProfiles.get(0);
				userProfile.setBirthYear(dataBirthYear);
				userProfile.setBirthMonth(dataBirthMonth);
				userProfile.setBirthDay(dataBirthDay);
				userProfile.setWeight(value);
				userProfile.setHeight(dataheightValue);
				userProfile.setGender(gender);
				userProfile.setUnitSystem(unitSystem);
				userProfile.setContext(getActivity());

				userProfile.update();

				getLifeTrakApplication().setUserProfile(userProfile);
				mUserProfile = userProfile;
			}
			else{
				mUserProfile.setWeight(value);
				mUserProfile.setHeight(dataheightValue);
				mUserProfile.setBirthYear(dataBirthYear);
				mUserProfile.setBirthMonth(dataBirthMonth);
				mUserProfile.setBirthDay(dataBirthDay);
				mUserProfile.setGender(gender);
				mUserProfile.setUnitSystem(unitSystem);
				mUserProfile.setContext(getActivity());
				mUserProfile.update();
			}
		}

		private void setValue(Editable s, int value) {
			mWeightValue.removeTextChangedListener(mTextWatcherWeight);
			s.replace(0, s.length(), String.valueOf(value));
			mWeightValue.addTextChangedListener(mTextWatcherWeight);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	};

	private final TextWatcher mTextWatcherHeight = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			LifeTrakLogger.info("UserProfile unit system" + mUserProfile.getUnitSystem());
			if (mUserProfile.getUnitSystem()== UNIT_METRIC) {
				int value = 0;
				if (!s.toString().equals("")) {
					value = Integer.parseInt(s.toString());
				} else {
					value = Integer.parseInt(mHeightValue.getHint().toString());
				}


				if (value > 220) {
					value = 220;
					setValue(s, value);
				}

				dataheightValue = value;

				List<UserProfile> userProfiles = DataSource.getInstance(getActivity())
						.getReadOperation()
						.query("watchUserProfile = ?", String.valueOf(mWatch.getId()))
						.getResults(UserProfile.class);
				if (userProfiles.size() > 0)
				{
					UserProfile userProfile = userProfiles.get(0);
					userProfile.setBirthYear(dataBirthYear);
					userProfile.setBirthMonth(dataBirthMonth);
					userProfile.setBirthDay(dataBirthDay);
					userProfile.setHeight(value);
					userProfile.setWeight(dataWeightValue);
					userProfile.setContext(getActivity());
					userProfile.setGender(gender);
					userProfile.setUnitSystem(unitSystem);
					userProfile.setContext(getActivity());
					userProfile.update();

					getLifeTrakApplication().setUserProfile(userProfile);
					mUserProfile = userProfile;
				}
				else{
					mUserProfile.setHeight(value);
					mUserProfile.setWeight(dataWeightValue);
					mUserProfile.setBirthYear(dataBirthYear);
					mUserProfile.setBirthMonth(dataBirthMonth);
					mUserProfile.setBirthDay(dataBirthDay);
					mUserProfile.setGender(gender);
					mUserProfile.setUnitSystem(unitSystem);
					mUserProfile.setContext(getActivity());
					mUserProfile.update();
				}
			}
		}

		private void setValue(Editable s, int value) {
			mHeightValue.removeTextChangedListener(mTextWatcherHeight);
			s.replace(0, s.length(), String.valueOf(value));
			mHeightValue.addTextChangedListener(mTextWatcherHeight);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	};

	private final View.OnFocusChangeListener mTextFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			if (!arg1) {
				List<UserProfile> userProfiles = DataSource.getInstance(getActivity())
						.getReadOperation()
						.query("watchUserProfile = ?", String.valueOf(mWatch.getId()))
						.getResults(UserProfile.class);
				switch (arg0.getId()) {
				case R.id.tvwWeightValue:
					int weightValue = 0;
					if (!mWeightValue.getText().toString().isEmpty()) {
						weightValue = Integer.parseInt(mWeightValue.getText().toString());
					} else {
						weightValue = Integer.parseInt(mWeightValue.getHint().toString());
					}

					if (mUserProfile.getUnitSystem() == UNIT_METRIC) {
						if (weightValue > 200) {
							weightValue = 200;
						} else if (weightValue < 20) {
							weightValue = 20;
						}
						mWeightValue.removeTextChangedListener(mTextWatcherWeight);
						mWeightValue.setText(String.valueOf(weightValue));
						mWeightValue.addTextChangedListener(mTextWatcherWeight);
						mWeightValue.setHint(String.valueOf(weightValue));
						weightValue = (int) (weightValue / KG);
					} 
					else {
						if (weightValue > 440) {
							weightValue = 440;
						} else if (weightValue < 44) {
							weightValue = 44;
						}
						mWeightValue.removeTextChangedListener(mTextWatcherWeight);
						mWeightValue.setText(String.valueOf(weightValue));
						mWeightValue.addTextChangedListener(mTextWatcherWeight);
						mWeightValue.setHint(String.valueOf(weightValue));			
					}
					dataWeightValue = weightValue;
					mUserProfile.setWeight(dataWeightValue);
					mUserProfile.setBirthYear(dataBirthYear);
					mUserProfile.setBirthMonth(dataBirthMonth);
					mUserProfile.setBirthDay(dataBirthDay);
					mUserProfile.setWeight(weightValue);
					mUserProfile.update();

					if (userProfiles.size() > 0)
					{
						UserProfile userProfile = userProfiles.get(0);
						userProfile.setBirthYear(dataBirthYear);
						userProfile.setBirthMonth(dataBirthMonth);
						userProfile.setBirthDay(dataBirthDay);
						userProfile.setWeight(dataWeightValue);
						userProfile.setHeight(dataheightValue);
						userProfile.setGender(gender);
						userProfile.setUnitSystem(unitSystem);
						userProfile.setContext(getActivity());
						userProfile.update();

						getLifeTrakApplication().setUserProfile(userProfile);
						mUserProfile = userProfile;
					}
					else{
						mUserProfile.setWeight(dataWeightValue);
						mUserProfile.setBirthYear(dataBirthYear);
						mUserProfile.setBirthMonth(dataBirthMonth);
						mUserProfile.setBirthDay(dataBirthDay);
						mUserProfile.setHeight(dataheightValue);
						mUserProfile.setGender(gender);
						mUserProfile.setUnitSystem(unitSystem);
						mUserProfile.setContext(getActivity());
						mUserProfile.update();
					}
					break;
				case R.id.tvwHeightValue:
					int heightValue = 0;
					if (!mHeightValue.getText().toString().isEmpty()) {
						heightValue = Integer.parseInt(mHeightValue.getText().toString());
					} else {
						heightValue = Integer.parseInt(mHeightValue.getHint().toString());
					}

					if (mUserProfile.getUnitSystem() == UNIT_METRIC) {
						if (heightValue < 102) {
							heightValue = 102;
						}
					}
					dataheightValue = heightValue;

					if (userProfiles.size() > 0)
					{
						UserProfile userProfile = userProfiles.get(0);
						userProfile.setBirthYear(dataBirthYear);
						userProfile.setBirthMonth(dataBirthMonth);
						userProfile.setBirthDay(dataBirthDay);
						userProfile.setWeight(dataWeightValue);
						userProfile.setHeight(heightValue);
						userProfile.setGender(gender);
						userProfile.setUnitSystem(unitSystem);
						userProfile.setContext(getActivity());
						userProfile.update();

						getLifeTrakApplication().setUserProfile(userProfile);
						mUserProfile = userProfile;
					}
					else{
						mUserProfile.setWeight(dataWeightValue);
						mUserProfile.setBirthYear(dataBirthYear);
						mUserProfile.setBirthMonth(dataBirthMonth);
						mUserProfile.setBirthDay(dataBirthDay);
						mUserProfile.setHeight(heightValue);
						mUserProfile.setGender(gender);
						mUserProfile.setUnitSystem(unitSystem);
						mUserProfile.setContext(getActivity());
						mUserProfile.update();
					}


					break;
				}
			}
			else {
				switch (arg0.getId()) {
					case R.id.tvwWeightValue:
						mWeightValue.setText("");
						break;
					case R.id.tvwHeightValue:
						mHeightValue.setText("");
						break;
				}

			}
		}
	};

	private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		@SuppressLint("ShowToast")
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

			Calendar calendar = new GregorianCalendar();
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, monthOfYear);
			calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

			long selectedMilis = calendar.getTimeInMillis();
			Date selecctedDate = new Date(selectedMilis);

			//if calendar date is future date
			if (selecctedDate.after(new Date(maxSelectedMilis))){
				//mAlertDialog.setMessage(getString(R.string.max_date, "Old Password"));
				mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(R.string.max_date).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				}).create();
				mAlertDialog.show();
				mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(R.string.sync_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				}).create();

			} else { 
				dataBirthDay = dayOfMonth;
				dataBirthMonth = monthOfYear + 1;
				dataBirthYear = year;

				mUserProfile.setBirthYear(year);
				mUserProfile.setBirthMonth(monthOfYear + 1);
				mUserProfile.setBirthDay(dayOfMonth);
				mUserProfile.setHeight(dataheightValue);
				mUserProfile.setWeight(dataWeightValue);
				mUserProfile.setContext(getActivity());
				mUserProfile.update();

				List<UserProfile> userProfiles = DataSource.getInstance(getActivity())
						.getReadOperation()
						.query("watchUserProfile = ?", String.valueOf(mWatch.getId()))
						.getResults(UserProfile.class);
				if (userProfiles.size() > 0)
				{
					UserProfile userProfile = userProfiles.get(0);
					userProfile.setBirthYear(year);
					userProfile.setBirthMonth(monthOfYear + 1);
					userProfile.setBirthDay(dayOfMonth);
					userProfile.setContext(getActivity());
					userProfile.setHeight(dataheightValue);
					userProfile.setWeight(dataWeightValue);
					userProfile.setGender(gender);
					userProfile.setUnitSystem(unitSystem);
					userProfile.setContext(getActivity());
					userProfile.update();
				}
				else{
					mUserProfile.setBirthYear(year);
					mUserProfile.setBirthMonth(monthOfYear + 1);
					mUserProfile.setBirthDay(dayOfMonth);
					mUserProfile.setHeight(dataheightValue);
					mUserProfile.setWeight(dataWeightValue);
					mUserProfile.setGender(gender);
					mUserProfile.setUnitSystem(unitSystem);
					mUserProfile.setContext(getActivity());
					mUserProfile.update();
				}


				mBirthdayValue.setText(mDateFormat.format(calendar.getTime()));
			}
			// Clear focus on other edit texts when date picker is displayed
			mWeightValue.clearFocus();
			mHeightValue.clearFocus();
		}
	};

	private final HeightPickerView.OnSelectHeightListener mSelectHeightListener = new HeightPickerView.OnSelectHeightListener() {
		@Override
		public void onSelectHeight(int valueInCm) {
//			mWeightValue.clearFocus();
//			mHeightValue.clearFocus();
//			mFirstNameEdit.clearFocus();
//			mLastNameEdit.clearFocus();

			dataheightValue = valueInCm;
//			mUserProfile.setWeight(dataWeightValue);
//			//mUserProfile.setHeight(dataheightValue);
//			mUserProfile.setBirthYear(dataBirthYear);
//			mUserProfile.setBirthMonth(dataBirthMonth);
//			mUserProfile.setBirthDay(dataBirthDay);
//
//			mUserProfile.setContext(getActivity());
//			mUserProfile.update();
			List<UserProfile> userProfiles = DataSource.getInstance(getActivity())
					.getReadOperation()
					.query("watchUserProfile = ?", String.valueOf(mWatch.getId()))
					.getResults(UserProfile.class);
			if (userProfiles.size() > 0)
			{
				UserProfile userProfile = userProfiles.get(0);
				userProfile.setBirthYear(dataBirthYear);
				userProfile.setBirthMonth(dataBirthMonth);
				userProfile.setBirthDay(dataBirthDay);
				userProfile.setGender(gender);
				userProfile.setWeight(dataWeightValue);
				userProfile.setBirthYear(dataBirthYear);
				userProfile.setUnitSystem(unitSystem);
				userProfile.setContext(getActivity());
				userProfile.update();

				getLifeTrakApplication().setUserProfile(userProfile);
				mUserProfile = userProfile;
			}
			else{
				mUserProfile.setHeight(valueInCm);
				mUserProfile.setWeight(dataWeightValue);
				//mUserProfile.setHeight(dataheightValue);
				mUserProfile.setBirthYear(dataBirthYear);
				mUserProfile.setBirthMonth(dataBirthMonth);
				mUserProfile.setBirthDay(dataBirthDay);
				mUserProfile.setGender(gender);
				mUserProfile.setUnitSystem(unitSystem);
				mUserProfile.setContext(getActivity());
				mUserProfile.update();
			}

			double feetValue = Math.floor(valueInCm / FEET_CM);
			double inchValue = (valueInCm / INCH_CM) - (feetValue * 12);

			if (Math.round(inchValue) == 12) {
				feetValue++;
				inchValue = 0;
			}
			String value = String.format("%d", (int) feetValue) + "'" + String.valueOf(Math.round(inchValue));
			mHeightValue.setText(value);
		}
	};

	private final RadioGroup.OnCheckedChangeListener mCheckChangedListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.radMale:
				mUserProfile.setGender(GENDER_MALE);
				gender = GENDER_MALE;
				break;
			case R.id.radFemale:
				mUserProfile.setGender(GENDER_FEMALE);
				gender = GENDER_FEMALE;
				break;
			}


			List<UserProfile> userProfiles = DataSource.getInstance(getActivity())
					.getReadOperation()
					.query("watchUserProfile = ?", String.valueOf(mWatch.getId()))
					.getResults(UserProfile.class);
			if (userProfiles.size() > 0)
			{
				UserProfile userProfile = userProfiles.get(0);
				userProfile.setBirthYear(dataBirthYear);
				userProfile.setBirthMonth(dataBirthMonth);
				userProfile.setBirthDay(dataBirthDay);
				userProfile.setWeight(dataWeightValue);
				userProfile.setHeight(dataheightValue);
				userProfile.setGender(gender);
				userProfile.setUnitSystem(unitSystem);
				userProfile.setContext(getActivity());
				userProfile.update();

				getLifeTrakApplication().setUserProfile(userProfile);
				mUserProfile = userProfile;
			} else {
				mUserProfile.setWeight(dataWeightValue);
				mUserProfile.setHeight(dataheightValue);
				mUserProfile.setBirthYear(dataBirthYear);
				mUserProfile.setBirthMonth(dataBirthMonth);
				mUserProfile.setBirthDay(dataBirthDay);
				mUserProfile.setUnitSystem(unitSystem);
				mUserProfile.setContext(getActivity());
				mUserProfile.update();
			}
		}
	};

	private final View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			if (mWeightValue.hasFocus()) {
				mWeightValue.clearFocus();
			}
			if (mHeightValue.hasFocus())
				mHeightValue.clearFocus();
			if (mFirstNameEdit.hasFocus()) {
				mFirstNameEdit.clearFocus();
			}
			if (mLastNameEdit.hasFocus())
				mLastNameEdit.clearFocus();

			switch (v.getId()) {
			case R.id.tvwHeightValue:
				if (mUserProfile.getUnitSystem() == UNIT_IMPERIAL) {
					mHeightPickerView.setValue(mUserProfile.getHeight());
					mHeightPickerView.show();

					//v.requestFocus();

					if (mFirstNameEdit.getText().toString().trim().length() == 0) {
						mFirstNameEdit.setText(dataFirstName);
					}

					((LinearLayout) getView().findViewById(R.id.linear_main)).requestFocus();
				}
				break;
			case R.id.tvwBirthdayValue:
				mDatePickerDialog.show();


				//v.requestFocus();

				if (mFirstNameEdit.getText().toString().trim().length() == 0) {
					mFirstNameEdit.setText(dataFirstName);
				}

				((LinearLayout) getView().findViewById(R.id.linear_main)).requestFocus();
				break;
			}
		}
	};

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
					if(null != result){
						mProfilePic.setImageBitmap(makeRoundedBitmap(result, (int) dpToPx(100), (int) dpToPx(100)));
					}
				}
			};
			mProfilePic.setTag(imageUrl);
			imageTask.execute(mProfilePic);
		}
	}



	/*private void orientProfileImage(String imgPath) {
		try {
			ExifInterface exif = new ExifInterface(imgPath);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
			Matrix matrix = new Matrix();
			if (orientation == 6) {
				matrix.postRotate(90);
			} else if (orientation == 3) {
				matrix.postRotate(180);
			} else if (orientation == 8) {
				matrix.postRotate(270);
			}
			mBitmap = BitmapFactory.decodeFile(imgPath);
			mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true); // rotatingbitmap
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

	private void updateAllDataHeaders() {
		Calendar calendarNow = Calendar.getInstance();
		calendarNow.setTime(new Date());

		int day = calendarNow.get(Calendar.DAY_OF_MONTH);
		int month = calendarNow.get(Calendar.MONTH) + 1;
		int year = calendarNow.get(Calendar.YEAR) - 1900;

		List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchDataHeader = ? and syncedToCloud = 0", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(StatisticalDataHeader.class);

		for (StatisticalDataHeader dataHeader : dataHeaders) {
			if (!(dataHeader.getDateStampDay() == day && dataHeader.getDateStampMonth() == month && dataHeader.getDateStampYear() == year)) {
				dataHeader.setContext(getActivity());
				dataHeader.setWatch(getLifeTrakApplication().getSelectedWatch());
				dataHeader.setSyncedToCloud(true);
				dataHeader.update();
			}
		}


		List<SleepDatabase> sleepDatabases = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchSleepDatabase = ? and syncedToCloud = 0"
						, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(SleepDatabase.class);

		for (SleepDatabase sleepDatabase : sleepDatabases) {
			sleepDatabase.setContext(getActivity());
			sleepDatabase.setWatch(getLifeTrakApplication().getSelectedWatch());
			sleepDatabase.setSyncedToCloud(true);
			sleepDatabase.update();
		}

		List<WorkoutHeader> workoutHeaders = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchWorkoutHeader = ? and syncedToCloud = 0", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(WorkoutHeader.class);

		for (WorkoutHeader workoutHeader : workoutHeaders) {
			workoutHeader.setContext(getActivity());
			workoutHeader.setWatch(getLifeTrakApplication().getSelectedWatch());
			workoutHeader.setSyncedToCloud(true);
			workoutHeader.update();
		}

		List<WorkoutInfo> workoutInfos = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchWorkoutInfo = ? and syncedToCloud = 0", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(WorkoutInfo.class);

		for (WorkoutInfo workoutInfo : workoutInfos) {
			workoutInfo.setContext(getActivity());
			workoutInfo.setWatch(getLifeTrakApplication().getSelectedWatch());
			workoutInfo.setSyncedToCloud(true);
			workoutInfo.update();
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
	switch (v.getId()){
		case R.id.textview_first_name_edit:
				if (!hasFocus){
					if (mFirstNameEdit.getText().toString().trim().length() == 0) {
						mFirstNameEdit.setText(dataFirstName);
					}
				}else {
					mFirstNameEdit.setText("");
				}
			break;
		case R.id.textview_last_name_edit:

			if (!hasFocus){
				if (mLastNameEdit.getText().toString().trim().length() == 0)
					mLastNameEdit.setText(dataLastName);
			}else {
				mLastNameEdit.setText("");
			}
			break;
		}
	}

	@Override
	public void onClick(View view) {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		switch (view.getId()) {
			case R.id.imgSyncToCloud:
				if (NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
					mProgressDialog.show();
					Date expirationDate = getExpirationDate();
					Date now = new Date();

					if (now.after(expirationDate)) {
						mCurrentOperation = OPERATION_REFRESH_TOKEN;
						refreshToken();
					} else {
//					mCurrentOperation = OPERATION_SYNC_TO_CLOUD;
//					startSyncToServer();
						mCurrentOperation = OPERATION_CHECK_SERVERTIME;
						startCheckingServer();
					}
				} else {
					AlertDialog alert = new AlertDialog.Builder(getActivity())
							.setTitle(R.string.lifetrak_title)
							.setMessage(R.string.check_network_connection)
							.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();
								}
							}).create();
					alert.show();
				}
				break;
			case R.id.imgSyncToWatch:
				((MainActivity) getActivity()).syncWatch();
				break;
			case R.id.tvwEditAccount:
				((MainActivity) getActivity()).switchFragment2(FragmentFactory.newInstance(EditProfileFragment.class));
				break;
			case R.id.btnLogout:
				AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(R.string.verify_signout)
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
								Intent intent = new Intent(getActivity(), IntroductionActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
								startActivity(intent);
								getActivity().finish();
							}
						}).create();
				alert.show();

				break;
			case R.id.tvwSwitchNewWatch:
				mPreferenceWrapper.setPreferenceBooleanValue(HAS_USER_PROFILE, true).synchronize();
				Intent intent = new Intent(getActivity(), WelcomePageActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				getActivity().finish();
				break;

			case R.id.textview_save_password:
				if (!NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
					NetworkUtil.getInstance(getActivity()).showConnectionErrorMessage();
					return;
				}
				mFirstName.setText(mFirstName.getText().toString().trim());
				mLastName.setText(mLastName.getText().toString().trim());

				if (isValidInputsPassword()) {
					mCurrentOperation = OPERATION_EDIT_PROFILE;
					mProgressDialog.show();
					String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
					mEditProfileAsync.url(getApiUrl() + USER_UPDATE_URI).addParam("access_token", accessToken).addParam("first_name", mFirstName.getText().toString())
							.addParam("last_name", mLastName.getText().toString()).addParam("email", mPreferenceWrapper.getPreferenceStringValue(EMAIL));

					if (!isEditTextEmpty(mOldPassword) && !isEditTextEmpty(mNewPassword)) {
						mEditProfileAsync.addParam("old_password", mOldPassword.getText().toString());
						mEditProfileAsync.addParam("password", mNewPassword.getText().toString());
					}

					if (mBitmap != null) {
//						if (!isEditTextEmpty(mOldPassword) && !isEditTextEmpty(mNewPassword)) {
//							mEditProfileAsync.addParam("old_password", mOldPassword.getText().toString());
//							mEditProfileAsync.addParam("password", mNewPassword.getText().toString());
//						}

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

			case R.id.textview_change_profile_pic:
				mImageAlertDialog.show();
				break;

			case R.id.textview_first_name:
				//removing onclick on other views
				mChangeProfilePic.setOnClickListener(null);
				mLastName.setOnClickListener(null);

				mRelativeFname.setVisibility(View.GONE);
				mRelativeFnameEdit.setVisibility(View.VISIBLE);
				break;

			case R.id.button_fname_save:

				if (isValidInputsFistname()){
					if (NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
						mCurrentOperation = OPERATION_EDIT_PROFILE;
						mProgressDialog.show();
						String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
						mEditProfileAsync.url(getApiUrl() + USER_UPDATE_URI).addParam("access_token", accessToken).addParam("first_name", mFirstNameEdit.getText().toString())
								.addParam("last_name", mLastName.getText().toString()).addParam("email", mPreferenceWrapper.getPreferenceStringValue(EMAIL));

						mEditProfileAsync.post();
					} else {
						NetworkUtil.getInstance(getActivity()).showConnectionErrorMessage();
						if (mProgressDialog.isShowing())
							mProgressDialog.dismiss();
					}
				}

				break;

			case R.id.button_fname_cancel:
				//adding onclick on other views
				mChangeProfilePic.setOnClickListener(this);
				mLastName.setOnClickListener(this);

				mRelativeFnameEdit.setVisibility(View.GONE);
				mRelativeFname.setVisibility(View.VISIBLE);
				mFirstName.setText(dataFirstName);


				break;

			case R.id.textview_last_name:
				//removing onclick on other views
				mChangeProfilePic.setOnClickListener(null);
				mFirstName.setOnClickListener(null);

				mRelativeLname.setVisibility(View.GONE);
				mRelativeLnameEdit.setVisibility(View.VISIBLE);
				mLastNameEdit.requestFocus();

				imm.showSoftInput(mLastNameEdit, InputMethodManager.SHOW_IMPLICIT);
				break;

			case R.id.button_lname_save:

				if (isValidInputsLastname()){
					if (NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
						mCurrentOperation = OPERATION_EDIT_PROFILE;
						mProgressDialog.show();
						String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
						mEditProfileAsync.url(getApiUrl() + USER_UPDATE_URI).addParam("access_token", accessToken).addParam("first_name", mFirstName.getText().toString())
								.addParam("last_name", mLastNameEdit.getText().toString()).addParam("email", mPreferenceWrapper.getPreferenceStringValue(EMAIL));

						mEditProfileAsync.post();
					} else {
						NetworkUtil.getInstance(getActivity()).showConnectionErrorMessage();
						if (mProgressDialog.isShowing())
							mProgressDialog.dismiss();
					}
				}

				imm.hideSoftInputFromWindow(mLastName.getWindowToken(), 0);
				break;

			case R.id.button_lname_cancel:
				//adding onclick on other views
				mChangeProfilePic.setOnClickListener(this);
				mFirstName.setOnClickListener(this);

				mRelativeLnameEdit.setVisibility(View.GONE);
				mRelativeLname.setVisibility(View.VISIBLE);
				mLastName.setText(dataLastName);

				imm.hideSoftInputFromWindow(mLastName.getWindowToken(), 0);
				break;

			case R.id.textview_first_name_edit:
				flag_edit_profile_change = true;
				//mFirstNameEdit.setText("");
				break;

			case R.id.btnSaveChangesProfile:
				try {
					File sd = Environment.getExternalStorageDirectory();
					File data = Environment.getDataDirectory();
					;
					if (sd.canWrite()) {
						//String currentDBPath = "/data/" + getActivity().getPackageName() + "/databases/";
						String currentDBPath = getActivity().getDatabasePath("SalutronLifeTrak.db").getAbsolutePath();
						String backupDBPath = Environment.getExternalStorageDirectory() + File.separator + "database";
						File currentDB = new File(currentDBPath);
						File backupDB = new File(backupDBPath);
						if (!backupDB.exists()) {
							backupDB.mkdirs();
						}

						OutputStream myOutput = new FileOutputStream(backupDB.getPath() + "/Salutron.backup");
						InputStream myInput = new FileInputStream(currentDBPath);
						byte[] buffer = new byte[1024];
						int length;
						while ((length = myInput.read(buffer)) > 0) {
							myOutput.write(buffer, 0, length);
						}

						myOutput.flush();
						myOutput.close();
						myInput.close();

						if (currentDB.exists()) {
							FileChannel src = new FileInputStream(currentDB).getChannel();
							FileChannel dst = new FileOutputStream(backupDB).getChannel();
							dst.transferFrom(src, 0, src.size());
							src.close();
							dst.close();
						}
					}
				} catch (Exception e) {
					Log.e("Error", e.getLocalizedMessage());
				}
				//smart sync
//				if (NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
//					mProgressDialog.show();
//					Date expirationDate = getExpirationDate();
//					Date now = new Date();
//					if (flag_edit_profile_change) {
//						if (isValidInputsProfilePic()) {
//							if (now.after(expirationDate)) {
//								mCurrentOperation = OPERATION_REFRESH_TOKEN;
//								refreshToken();
//							} else {
//								mCurrentOperation = OPERATION_CHECK_SERVERTIME;
//								startCheckingServer();
//							}
//						}
//						else{
//							if (mProgressDialog.isShowing())
//								mProgressDialog.dismiss();
//						}
//					}
//					else{
//						if (now.after(expirationDate)) {
//							mCurrentOperation = OPERATION_REFRESH_TOKEN;
//							refreshToken();
//						} else {
//							mCurrentOperation = OPERATION_CHECK_SERVERTIME;
//							startCheckingServer();
//						}
//					}
//
//				} else {
//					AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
//							.setTitle(R.string.lifetrak_title)
//							.setMessage(R.string.check_network_connection)
//							.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(DialogInterface arg0, int arg1) {
//									arg0.dismiss();
//								}
//							}).create();
//					alertDialog.show();
//				}
			break;

			case R.id.textview_last_name_edit:
				flag_edit_profile_change = true;
			break;

			case R.id.edittext_old_password:
				flag_edit_profile_change = true;
			break;
			case R.id.edittext_new_password:
				flag_edit_profile_change = true;
			break;
			case R.id.edittext_confirm_password:
				flag_edit_profile_change = true;
			break;
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
		mBitmap = createSquareBitmap(mBitmap);

		mAlertDialogProfile = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(R.string.verify_profile_pic)
				.setCancelable(false)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();

								if (NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
									if (isValidInputsProfilePic()) {
										mCurrentOperation = OPERATION_EDIT_PROFILE;
										mProgressDialog.show();
										String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
										mEditProfileAsync.url(getApiUrl() + USER_UPDATE_URI).addParam("access_token", accessToken).addParam("first_name", mFirstName.getText().toString())
												.addParam("last_name", mLastName.getText().toString()).addParam("email", mPreferenceWrapper.getPreferenceStringValue(EMAIL));
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

										if (!isEditTextEmpty(mOldPassword) && !isEditTextEmpty(mNewPassword)) {
											mEditProfileAsync.addParam("old_password", mOldPassword.getText().toString());
											mEditProfileAsync.addParam("password", mNewPassword.getText().toString());
										}
										mEditProfileAsync.post();
									}
								}

								else {
									NetworkUtil.getInstance(getActivity()).showConnectionErrorMessage();
									if (mProgressDialog.isShowing())
										mProgressDialog.dismiss();
								}


							}
						}

				)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						}
				)

				.create();

				mAlertDialogProfile.show();
				}


	private boolean isValidInputsPassword() {
			 if (isEditTextEmpty(mOldPassword) && isEditTextEmpty(mNewPassword) && isEditTextEmpty(mSavePassword)){
				mAlertDialogForValidInputs.setMessage(getString(R.string.enter_input, getString(R.string.old_password)));
				mAlertDialogForValidInputs.show();
				return false;
			}
			else if (isEditTextEmpty(mOldPassword) && !isEditTextEmpty(mNewPassword) && !isEditTextEmpty(mOldPassword)) {
				mAlertDialogForValidInputs.setMessage(getString(R.string.enter_input, getString(R.string.old_password)));
				mAlertDialogForValidInputs.show();
				return false;
			} else if(!isEditTextEmpty(mNewPassword) && (mNewPassword.getText().length() < 6 || mNewPassword.getText().length() > 30)) {
				mAlertDialogForValidInputs.setMessage(getString(R.string.password_min_max_chars));
				mAlertDialogForValidInputs.show();
				return false;
			} else if (!mNewPassword.getText().toString().equals(mSavePassword.getText().toString())) {
				mAlertDialogForValidInputs.setMessage(getString(R.string.password_not_matched));
				mAlertDialogForValidInputs.show();
				return false;
			} else if (mOldPassword.getText().length() < 6 || mNewPassword.getText().length() < 6 || mSavePassword.getText().length() < 6 ) {
				mAlertDialogForValidInputs.setMessage(getString(R.string.password_min_max_chars));
				mAlertDialogForValidInputs.show();
				return false;
			}
			return true;
		}

	private boolean isValidInputsFistname() {
		String editedString = mFirstNameEdit.getText().toString();
		editedString = editedString.trim().replaceAll(" +", " ");
		mFirstNameEdit.setText(editedString);
		if (isEditTextEmpty(mFirstNameEdit)) {
			mAlertDialogForValidInputs.setMessage(getString(R.string.enter_input, getString(R.string.firstname)));
			mAlertDialogForValidInputs.show();
			return false;
		} else if (!isEditTextEmpty(mFirstNameEdit) && mFirstNameEdit.getText().length() < 2) {
			mAlertDialogForValidInputs.setMessage(getString(R.string.firstname_minimum_chars));
			mAlertDialogForValidInputs.show();
			return false;
		}

		return true;
	}

	private boolean isValidInputsLastname() {
		String editedString = mLastNameEdit.getText().toString();
		editedString = editedString.trim().replaceAll(" +", " ");
		mLastNameEdit.setText(editedString);
		if (isEditTextEmpty(mLastNameEdit)) {
			mAlertDialogForValidInputs.setMessage(getString(R.string.enter_input, getString(R.string.lastname)));
			mAlertDialogForValidInputs.show();
			return false;
		} else if (!isEditTextEmpty(mLastNameEdit) && mLastNameEdit.getText().length() < 2) {
			mAlertDialogForValidInputs.setMessage(getString(R.string.lastname_minimum_chars));
			mAlertDialogForValidInputs.show();
			return false;
		}

		return true;
	}

	private boolean isValidInputsProfilePic() {
		String editedString = mFirstNameEdit.getText().toString();
		editedString = editedString.trim().replaceAll(" +", " ");
		mFirstNameEdit.setText(editedString);

		String editedString2 = mLastNameEdit.getText().toString();
		editedString2 = editedString2.trim().replaceAll(" +", " ");
		mLastName.setText(editedString2);

		if (isEditTextEmpty(mFirstNameEdit)) {
			mAlertDialogForValidInputs.setMessage(getString(R.string.enter_input, getString(R.string.firstname)));
			mAlertDialogForValidInputs.show();
			return false;
		} else if (isEditTextEmpty(mLastNameEdit)) {
			mAlertDialogForValidInputs.setMessage(getString(R.string.enter_input, getString(R.string.lastname)));
			mAlertDialogForValidInputs.show();
			return false;
		} else if (!isEditTextEmpty(mFirstNameEdit) && mFirstNameEdit.getText().length() < 2) {
			mAlertDialogForValidInputs.setMessage(getString(R.string.firstname_minimum_chars));
			mAlertDialogForValidInputs.show();
			return false;
		} else if (!isEditTextEmpty(mLastNameEdit) && mLastNameEdit.getText().length() < 2) {
			mAlertDialogForValidInputs.setMessage(getString(R.string.lastname_minimum_chars));
			mAlertDialogForValidInputs.show();
			return false;
		}

		return true;
	}

	private void syncProfile(){

			mCurrentOperation = OPERATION_EDIT_PROFILE;
			if (!mProgressDialog.isShowing())
			mProgressDialog.show();
			String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
			mEditProfileAsync.url(getApiUrl() + USER_UPDATE_URI).addParam("access_token", accessToken).addParam("first_name", mFirstNameEdit.getText().toString())
					.addParam("last_name", mLastNameEdit.getText().toString()).addParam("email", mPreferenceWrapper.getPreferenceStringValue(EMAIL));

			if (!isEditTextEmpty(mOldPassword) && !isEditTextEmpty(mNewPassword)) {
				mEditProfileAsync.addParam("old_password", mOldPassword.getText().toString());
				mEditProfileAsync.addParam("password", mNewPassword.getText().toString());
			}

			if (mBitmap != null) {
//				if (!isEditTextEmpty(mOldPassword) && !isEditTextEmpty(mNewPassword)) {
//					mEditProfileAsync.addParam("old_password", mOldPassword.getText().toString());
//					mEditProfileAsync.addParam("password", mNewPassword.getText().toString());
//				}

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


	private boolean isEditTextEmpty(EditText txt) {
		return txt.getText().toString().isEmpty();
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
		orientProfileImage(mPath);
		mBitmap = createSquareBitmap(mBitmap);

		mAlertDialogProfile = new AlertDialog.Builder(getActivity()).setTitle(R.string.lifetrak_title).setMessage(R.string.verify_profile_pic)
				.setCancelable(false)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						if (isValidInputsProfilePic()) {
							if (NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
								mCurrentOperation = OPERATION_EDIT_PROFILE;
								mProgressDialog.show();
								String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
								mEditProfileAsync.url(getApiUrl() + USER_UPDATE_URI).addParam("access_token", accessToken).addParam("first_name", mFirstName.getText().toString())
										.addParam("last_name", mLastName.getText().toString()).addParam("email", mPreferenceWrapper.getPreferenceStringValue(EMAIL));
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

								mEditProfileAsync.post();
							} else {
								NetworkUtil.getInstance(getActivity()).showConnectionErrorMessage();
								if (mProgressDialog.isShowing())
									mProgressDialog.dismiss();
							}
						}


					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				})

				.create();
		mAlertDialogProfile.show();



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
		LifeTrakLogger.info(exceptionMessage);
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


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		flag_edit_profile_change =true;
	}

	@Override
	public void afterTextChanged(Editable s) {

	}


	public void handleBackPressed() {
		mWeightValue.clearFocus();
		mHeightValue.clearFocus();
		mFirstNameEdit.clearFocus();
		mLastNameEdit.clearFocus();
	}
}
