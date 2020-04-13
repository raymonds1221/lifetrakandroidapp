package com.salutron.lifetrakwatchapp.util;

import com.salutron.blesdk.SALTimeDate;
import com.salutron.blesdk.SALUserProfile;
import com.salutron.blesdk.SALBLEService;

/**
 * Interface of all constants
 * 
 * @author rsarmiento
 * 
 */
public interface SalutronLifeTrakUtility {
	public final String TAG = "SalutronLifeTrak";
	/*
	 * List of watch models
	 */
	public static final int WATCHMODEL_C300 = SALBLEService.MODEL_C300;
    public static final int WATCHMODEL_C300_IOS = 300;

	public static final int WATCHMODEL_C410 = SALBLEService.MODEL_C410;
	public static final int WATCHMODEL_R415 = SALBLEService.MODEL_R415;
	public static final int WATCHMODEL_R500 = SALBLEService.MODEL_R500;
	public static final int WATCHMODEL_R420 = SALBLEService.MODEL_R420;
	/*
	 * List of watch names
	 */
	public static final String WATCHNAME_C300 = "Move C300 / C320";
	public static final String WATCHNAME_C410 = "Zone C410 / C410w";
	public static final String WATCHNAME_R415 = "Brite R450";
	public static final String WATCHNAME_R500 = "R500";
	public static final String WATCHNAME_R420 = "Zone R420";
	/*
	 * List of keys
	 */
	public static final String SELECTED_WATCH_MODEL = "selected_watch_model";

	/*
	 * List of request codes
	 */
	public static final int REQUEST_CODE_PAIR_DEVICE = 0x01;
	public static final int REQUEST_CODE_USER_PROFILE = 0x02;
	public static final int REQUEST_CODE_UPDATE_GOAL = 0x03;
	public static final int REQUEST_CODE_PAIR_DEVICE_SYNC = 0x04;
	public static final int REQUEST_CODE_WELCOME_PAGE = 0x05;
	public static final int REQUEST_CODE_ENABLE_BLUETOOTH = 0x06;
	public static final int REQUEST_CODE_PROFILE_SELECT = 0x07;
	public static final int REQUEST_CODE_IMAGE_GALLERY = 0x08;
	public static final int REQUEST_CODE_IMAGE_CAMERA = 0x09;
	public static final int REQUEST_CODE_PAIR_DEVICE_GOAL_SYNC = 0x10;
    public static final int REQUEST_CODE_PROFILE_SELECT_R450 = 0x11;
	public static final int REQUEST_CODE_PROFILE_SELECT_R420 = 0x12;
	public static final int REQUEST_GOOGLE_FIT_OAUTH = 0x13;


	/*
	 * Database constants
	 */
	public static final String DATABASE_NAME = "SalutronLifeTrak.db";
	public static final int DATABASE_VERSION = 14;
	public static final String SORT_ASC = "asc";
	public static final String SORT_DESC = "desc";
	public static final String DEVICE_SEARCH = "Device_Search";
	public static final String DEVICE_INITIALIZE_CONNECT = "Device_Initialize_Connect";
	public static final String DEVICE_CONNECTED = "Device_Connected";
	public static final String DEVICE_READY = "Device_Ready";
	public static final String DEVICE_START_SYNC = "Device_Start_Sync";
	public static final String GET_DATA_HEADER ="Get_Data_Header";
	public static final String GET_DATA_POINTS = "Get_Data_Points";
	public static final String GET_LIGHT_DATA_POINTS = "Get_Light_Data_Points";
	public static final String GET_WORKOUT= "Get_Workout";
	public static final String GET_WORKOUT_STOP = "Get_Workout_Stop";
	public static final String GET_SLEEP_DATABASE = "Get_Sleep_Database";
	public static final String GET_SLEEP_SETTINGS = "Get_Sleep_Setting";
	public static final String GET_STEP_GOAL = "Get_Step_Goal";
	public static final String GET_DISTANCE_GOAL ="Get_Distance_Goal";
	public static final String GET_CALORIE_GOAL = "Get_Calorie_Goal";
	public static final String GET_CALIBRATION_DATA = "Get_Calibration_Data";
	public static final String GET_WAKEUP_SETTING = "Get_Wakeup_Setting";
	public static final String GET_NOTIFICATION = "Get_Notification";
	public static final String GET_ACTIVITY_ALERT = "Get_Activity_Alert";
	public static final String GET_DAYLIGHT_SETTING = "Get_Daylight_Setting";
	public static final String GET_NIGHTLIGHT_SETTING = "Get_Nightlight_Setting";
	public static final String GET_USER_PROFILE = "Get_User_Profile";
	public static final String GET_TIME = "Get_Time";
	

	/*
	 * Key for Fragments
	 */
	public static final String FRAGMENT_MENU = "fragment_menu";
	public static final String FRAGMENT_CONTENT = "fragment_content";
	public static final String DASHBOARD_FRAGMENT1 = "dashboard_fragment1";
	public static final String DASHBOARD_FRAGMENT2 = "dashboard_fragment2";
	public static final String DASHBOARD_FRAGMENT3 = "dashboard_fragment3";

	/*
	 * Menu View Types
	 */
	public static final int MENU_VIEW_TYPE_ACCOUNT = 0x01;
	public static final int MENU_VIEW_TYPE_PAGE = 0x02;
	public static final int MENU_VIEW_TYPE_SEPARATOR = 0x03;

	/*
	 * Menu Item Types
	 */
	public static final int MENU_ITEM_ACCOUNT = 0x01a;
	public static final int MENU_ITEM_DASHBOARD = 0x02a;
	public static final int MENU_ITEM_GOALS = 0x03a;
	public static final int MENU_ITEM_SETTINGS = 0x04a;
	public static final int MENU_ITEM_PARTNERS = 0x05a;
	public static final int MENU_ITEM_HELP = 0x06a;
	public static final int MENU_ITEM_LOGOUT = 0x07a;
	public static final int MENU_ITEM_SEPARATOR = 0x08a;

	/*
	 * Types
	 */
	public static final String DASHBOARD_TYPE = "dashboard_type";
	public static final int TYPE_STEPS = 0x0010;
	public static final int TYPE_CALORIES = 0x0020;
	public static final int TYPE_DISTANCE = 0x0030;
	public static final int TYPE_HEART_RATE = 0x0040;
	public static final int TYPE_SLEEP = 0x0050;
	public static final int TYPE_WORKOUT = 0x0060;
	public static final int TYPE_ACTIGRAPHY = 0x0070;
	public static final int TYPE_LIGHT_EXPOSURE = 0x0080;

	/*
	 * Dashboard item types
	 */
	public static final int DASHBOARD_ITEM_TYPE_HEART_RATE = 0x01;
	public static final int DASHBOARD_ITEM_TYPE_METRIC = 0x02;
	public static final int DASHBOARD_ITEM_TYPE_SLEEP = 0x03;
	public static final int DASHBOARD_ITEM_TYPE_WORKOUT = 0x04;
	public static final int DASHBOARD_ITEM_TYPE_ACTIGRAPHY = 0x05;
	public static final int DASHBOARD_ITEM_TYPE_LIGHT_EXPOSURE = 0x06;

	/*
	 * Handler delay
	 */
	public static final int SYNC_DELAY = 2000;
	public static final int MINI_SYNC_DELAY = 750;
	public static final int HANDLER_DELAY = 900;

	/*
	 * Declared fields
	 */
	public static final String FIELD_WATCH = "watch";

	/*
	 * Preference Wrapper & Bundle Keys
	 */
	public static final String WATCH = "watch";
	public static final String HAS_PAIRED = "has_paired";
	public static final String MAC_ADDRESS = "mac_address";
	public static final String FROM_IOS = "is_from_ios";
	public static final String DATE = "date";
	public static final String DATE_FROM = "date_from";
	public static final String DATE_TO = "date_to";
	public static final String YEAR = "year";
	public static final String HAS_USER_PROFILE = "has_user_profile";
	public static final String METRIC_FIELD = "metric_field";
	public static final String DASHBOARD_ITEMS = "dashboard_items";
	public static final String MODEL_NUMBER = "com.salutron.blesdk.modelnumber";
	public static final String CALENDAR_MODE_KEY = "calendar_mode_key";
	public static final String POSITION = "position";
	public static final String WATCH_NAME = "watch_name";
	public static final String WATCH_ADDRESS = "watch_address";
	public static final String DEVICE_FOUND = "device_found";
	public static final String SYNC_TYPE = "sync_type";
	public static final String SLEEP_DATABASE = "sleep_database";
	public static final String SDK_VERSION = "sdk_version";
	public static final String ITEM_VIEW_TYPE = "item_view_type";
	public static final String DASHBOARD_ITEM_JSON = "dashboard_items";
	public static final String SYNC_SUCCESS = "sync_success";
	public static final String WATCH_EXISTS = "watch_exists";
	public static final String IMAGE_PATH = "image_path";
	public static final String USER_PROFILE = "user_profile";
	public static final String SAL_USER_PROFILE = "sal_user_profile";
	public static final String WATCH_SETTINGS_CHILD = "watch_settings_child";
	public static final String TIME_DATE = "time_date";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String REFRESH_TOKEN = "refresh_token";
	public static final String EXPIRATION_DATE = "expiration_date";
	public static final String FIRST_NAME = "firstname";
	public static final String LAST_NAME = "lastname";
	public static final String EMAIL = "email";
    public static final String LAST_DATE_SYNCED = "last_date_synced";
	public static final String PROFILE_IMG = "profile_img";
	public static final String PASSWORD = "password";
	public static final String IS_REMEMBER_ME = "is_remember_me";
	public static final String IS_REMEMBER_ME_ISSUE = "is_remember_me_issue";
	public static final String FIRST_INSTALL = "first_install";
	public static final String STARTED_FROM_LOGIN = "from_login";
	public static final String IS_WALGREENS_CONNECTED = "is_walgreens_connected";
	public static final String AUTHORIZE_URL = "authorize_url";
	public static final String AUTO_SYNC = "auto_sync";
	public static final String AUTO_SYNC_TIME = "auto_sync_time";
	public static final String SCROLL_POSITION = "scroll_position";
	public static final String DO_NOT_SHOW_PROMPT_DIALOG = "do_not_show_prompt_dialog";
	public static final String USE_SETTING = "use_setting";
	public static final String ACCOUNT_ACTIVATED = "account_activated";
	public static final String IS_FACEBOOK = "is_facebook";
    public static final String USER_ID = "id";
	public static final String LOGIN_TYPE = "login_type";
	public static final String CALIBRATION_DATA_FROM_WATCH = "calibration_data_from_watch";
	public static final String GOAL_FROM_WATCH = "goal_from_watch";
	public static final String IS_WATCH_CONNECTED = "is_watch_connected";
    public static final String LAST_CONNECTED_WATCH_ID = "last_connected_watch_id";
    public static final String LAST_SYNCED_R450_WATCH_MAC_ADDRESS = "last_synced_r450_watch";
    public static final String OPENED_FROM_NOTIFICATION = "opened_from_notification";
    public static final String NOTIFICATION_ENABLED = "notification_enabled";
	public static final String LAST_R450_SYNC = "r450_last_sync";
	public static final String ADD_NEW_SLEEP = "new_sleep_added";
	public static final String FIRMWAREVERSION = "firmware_version";
	public static final String SOFTWAREVERSION = "software_version";
	public static final String R420_HR_LOG_RATE = "r420_hr_logging_rate";
	/**
	 * boolean which indicates if Google Fit is currently enabled or not
	 */
	String GOOGLE_FIT_ENABLED = "google_fit_enabled";
	String GOOGLE_FIT_CHOICE = "google_fit_choice";
	/**
	 * long which tracks the end time of the last synced data header
	 *
	 * The actual key is this prefix concatenated with the watch MAC address
	 */
	String GOOGLE_FIT_LAST_SYNCED_DATA_TIME_PREFIX = "google_fit_last_synced_data_time_";

	/*
	 * Watch settings
	 */
	public static final int TIME_FORMAT_12_HR = SALTimeDate.FORMAT_12HOUR;
	public static final int TIME_FORMAT_24_HR = SALTimeDate.FORMAT_24HOUR;
	public static final int DATE_FORMAT_DDMM = SALTimeDate.FORMAT_DDMM;
	public static final int DATE_FORMAT_MMDD = SALTimeDate.FORMAT_MMDD;
	public static final int DATE_FORMAT_MMMDD = SALTimeDate.FORMAT_MMMDD;
	public static final int DATE_FORMAT_DDMMM = SALTimeDate.FORMAT_DDMMM;
	public static final int DISPLAY_FORMAT_SMALL_DIGIT = SALTimeDate.FORMAT_TIME_SMALL_DIGIT;
	public static final int DISPLAY_FORMAT_BIG_DIGIT = SALTimeDate.FORMAT_TIME_BIG_DIGIT;
	public static final int UNIT_IMPERIAL = SALUserProfile.IMPERIAL;
	public static final int UNIT_METRIC = SALUserProfile.METRIC;
	public static final int GENDER_MALE = SALUserProfile.MALE;
	public static final int GENDER_FEMALE = SALUserProfile.FEMALE;
	public static final float MILE = 0.621371f;
	public static final float KG = 0.453592f;
	public static final float FEET = 0.0328084f;
	public static final float INCH = 12.0f;
	public static final float INCH_CM = 2.54f;
	public static final float FEET_CM = 30.48f;
	public static final int USE_APP = 0x01b;
	public static final int USE_WATCH = 0x02b;

	/*
	 * Lock object
	 */
	public static final Object LOCK_OBJECT = new Object();

	/*
	 * Calendar modes
	 */
	public static final int MODE_DAY = 0xa1;
	public static final int MODE_WEEK = 0xa2;
	public static final int MODE_MONTH = 0xa3;
	public static final int MODE_YEAR = 0xa4;
	
	/*
	 * Sync Reminder
	 */
	public static final String SYNC_WEEK_VALUE = "sync_day";
	public static final String TIME_ALERT = "sync_time";
	public static final String SYNC_DAY="sync_choosen_day";
	/*
	 * Min Y & Max Y Ranges
	 */
	public static final double FITNESS_RESULTS_MIN_Y = 0.0;
	public static final double FITNESS_RESULTS_MAX_Y = 20.0;
	public static final double LIGHT_PLOT_MIN_Y = 0.0;
	public static final double LIGHT_PLOT_MAX_Y = 20.0;
	public static final double HEART_RATE_MIN_Y = 0.0;
	public static final double HEART_RATE_MAX_Y = 20.0;
	public static final double ACTIGRAPHY_MIN_X = 0.0;
	public static final double ACTIGRAPHY_MAX_X = 144.0;
	public static final double ACTIGRAPHY_MIN_Y = -20.0;
	public static final double ACTIGRAPHY_MAX_Y = 20.0;
	public static final double ACTIGRAPHY_MIN_RANGE_Y = -10;
	public static final double ACTIGRAPHY_MAX_RANGE_Y = 10;

	public static final int INTENSE_HR = 200;

	public static final String AUTOSYNCTIME = "autoSyncTime";
	
	/*
	 * Sync Types
	 */
	public static final int SYNC_TYPE_INITIAL = 0x01;
	public static final int SYNC_TYPE_DASHBOARD = 0x02;

	/*
	 * Login Types
	 */
	public static final int LOGIN_TYPE_MANUAL = 0x01a;
	public static final int LOGIN_TYPE_FACEBOOK = 0x02a;

	/*
	 * API URI
	 */
	public static final String API_URL_PRODUCTION = "http://api.lifetrakusa.com";
  //  public static final String API_URL_PRODUCTION = "http://lifetrak-elb-01-326114032.us-west-2.elb.amazonaws.com";
   // public static final String API_URL_PRODUCTION = "http://ec2-52-11-3-253.us-west-2.compute.amazonaws.com";
//    public static final String API_URL_PRODUCTION = "https://my.lifetrakusa.com";
	public static final String API_URL_DEVELOPMENT = "http://staging.lifetrakusa.com";
	public static final String API_URL_DEVELOPMENT_2 = "http://lifetrakdev.lifetrakusa.com";
	//public static final String API_URL_DEVELOPMENT = "http://ec2-54-69-130-82.us-west-2.compute.amazonaws.com";
	public static final String API_URL = API_URL_DEVELOPMENT;
	public static final String REFRESH_TOKEN_URI = "/api/v1/oauth/refreshtoken";
	public static final String REGISTER_URI = "/api/v1/user/register";
	public static final String ERROR_SYNCING = "/error";
	public static final String SYNC_URI = "/api/v1/sync/send";
	public static final String STORE_URI = "/api/v1/sync/store";
	public static final String STORE_URI_V2 = "/api/v2/sync/store";
	public static final String SLEEP_URI_UPDATE = "/api/v2/sleep/update";
	public static final String SLEEP_URI_DELETE = "/api/v2/sleep/delete";
	public static final String LOGIN_URI = "/api/v1/user/login";
	public static final String FACEBOOK_URI = "/api/v1/user/facebook";
	public static final String RESTORE_DEVICE_URI = "/api/v1/restore/devices";
	public static final String TERMS_AND_CONDITIONS_URI = "file:///android_asset/terms.html";
	public static final String API_LOLLIPOP_ISSUE_URL = "http://www.lifetrakusa.com/support/lollipopissue";
	/**
	 * @params mac_address
	 * @params access_token
	 * */
	public static final String DELETE_DEVICE_URI = "/api/v1/device/delete";
	public static final String RESTORE_URI = "/api/v1/restore/";
	public static final String RESTORE_URI_V2 = "/api/v2/restore/";
	public static final String USER_URI = "/api/v1/user";
	public static final String USER_UPDATE_URI = "/api/v1/user/update";
	public static final String FORGOT_PASSWORD_URL = "/api/v1/password/send";
	public static final String WALGREENS_CONNECT = "/api/v1/walgreens/connect"; // mac_address,
																				// access_token,
																				// channel
																				// =
																				// mobile
	public static final String WALGREENS_DISCONNECT = "/api/v1/walgreens/disconnect";

    public static final String DEVICE_DATA_URL = "/api/v1/device";
	
	public static final String FLURRY_KEY = "44V5DG4SWVKJ99WGRJC5";
}
