package com.salutron.lifetrakwatchapp.util;

import java.util.List;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Message;
import android.util.Log;

import com.salutron.blesdk.SALActivityAlertSetting;
import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALCalibration;
import com.salutron.blesdk.SALConnectionSetting;
import com.salutron.blesdk.SALDayLightDetectSetting;
import com.salutron.blesdk.SALNightLightDetectSetting;
import com.salutron.blesdk.SALNotification;
import com.salutron.blesdk.SALSleepSetting;
import com.salutron.blesdk.SALStatus;
import com.salutron.blesdk.SALTimeDate;
import com.salutron.blesdk.SALUserProfile;
import com.salutron.blesdk.SALWakeupSetting;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.db.*;
import com.salutron.lifetrakwatchapp.model.*;

/**
 * Created by rsarmiento on 3/27/15.
 */
public class LifeTrakUpdateR450 implements  SalutronLifeTrakUtility{
	public static final Object LOCK_OBJECT = LifeTrakUpdateR450.class;
	public static LifeTrakUpdateR450 mLifeTrakUpdateR450;
	private Context mContext;
	private Watch mWatch;

	public static LifeTrakUpdateR450 newInstance(Context context) {
		synchronized (LOCK_OBJECT) {
			mLifeTrakUpdateR450 = new LifeTrakUpdateR450(context);
			return mLifeTrakUpdateR450;
		}
	}

	public static LifeTrakUpdateR450 newInstance(Context context, Watch watch) {
		synchronized (LOCK_OBJECT) {
			mLifeTrakUpdateR450 = new LifeTrakUpdateR450(context, watch); 
			return mLifeTrakUpdateR450;
		}
	}

	private LifeTrakUpdateR450(Context context) {
		mContext = context;
	}

	private LifeTrakUpdateR450(Context context, Watch watch) {
		mContext = context;
		mWatch = watch;
	}

	public Goal goalForCurrentWatch() {
		Calendar calendar = Calendar.getInstance();

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		List<Goal> goals = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
						String.valueOf(mWatch.getId()), String.valueOf(day), String.valueOf(month), String.valueOf(year))
						//.limit(1)
                        .orderBy("_id", SORT_DESC)
                        .getResults(Goal.class);

		if (goals.size() > 0) {
			return goals.get(0);
		}
		return null;
	}

    public Notification notificationForCurrentWatch() {
        List<Notification> notifications = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchNotification = ?", String.valueOf(mWatch.getId()))
                .getResults(Notification.class);


        if (notifications.size() > 0) {
            return notifications.get(0);
        }
        return null;
    }

	public TimeDate timeDateForCurrentWatch() {
		List<TimeDate> timeDates = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchTimeDate = ?", String.valueOf(mWatch.getId()))
				.getResults(TimeDate.class);

		if (timeDates.size() > 0)
			return timeDates.get(0);

		return null;
	}

	public CalibrationData calibrationDataForCurrentWatch() {
		List<CalibrationData> calibrations = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchCalibrationData = ?", String.valueOf(mWatch.getId()))
				.getResults(CalibrationData.class);

		if (calibrations.size() > 0)
			return calibrations.get(0);

		return null;
	}

	public WakeupSetting wakeupSettingForCurrentWatch() {
		List<WakeupSetting> wakeupSettings = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchWakeupSetting = ?", String.valueOf(mWatch.getId()))
				.getResults(WakeupSetting.class);
		if (wakeupSettings.size() > 0)
			return wakeupSettings.get(0);

		return null;
	}

	public DayLightDetectSetting daylightSettingForCurrentWatch() {
		List<DayLightDetectSetting> daylightSettings = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchDaylightSetting = ?", String.valueOf(mWatch.getId()))
				.getResults(DayLightDetectSetting.class);

		if (daylightSettings.size() > 0)
			return daylightSettings.get(0);

		return null;
	}

	public NightLightDetectSetting nightlightSettingForCurrentWatch() {
		List<NightLightDetectSetting> nightlightSettings = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchNightlightSetting = ?", String.valueOf(mWatch.getId()))
				.getResults(NightLightDetectSetting.class);

		if (nightlightSettings.size() > 0)
			return nightlightSettings.get(0);

		return null;
	}

	public ActivityAlertSetting activityAlertForCurrentWatch() {
		List<ActivityAlertSetting> activityAlertSettings = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchActivityAlert = ?", String.valueOf(mWatch.getId()))
				.getResults(ActivityAlertSetting.class);

		if (activityAlertSettings.size() > 0)
			return activityAlertSettings.get(0);

		return null;
	}

	public UserProfile userProfileForCurrentWatch() {
		List<UserProfile> userProfiles = DataSource.getInstance(mContext)
				.getReadOperation().query("watchUserProfile = ?", String.valueOf(mWatch.getId()))
				.getResults(UserProfile.class);

		if (userProfiles.size() > 0)
			return userProfiles.get(0);

		return null;
	}

	public boolean isGoalsEqualToApp(Goal goal) {
		Goal goalApp = goalForCurrentWatch();

		if (goalApp != null) {
			boolean value = (goal.getDistanceGoal() == goalApp.getDistanceGoal() && goal.getStepGoal() == goalApp.getStepGoal() &&
					goal.getCalorieGoal() == goalApp.getCalorieGoal() && goal.getSleepGoal() == goalApp.getSleepGoal() && goal.getBrightLightGoal() == goalApp.getBrightLightGoal());
			return value;
		}

		return false;
	}

	public boolean isTimeDateEqualToApp(TimeDate timeDate) {
		TimeDate timeDateApp = timeDateForCurrentWatch();

		if (timeDateApp != null) {
			boolean value = timeDate.getHourFormat() == timeDateApp.getHourFormat()  && timeDate.getDateFormat() == timeDateApp.getDateFormat();
			return value;
		}
		return false;
	}

	public boolean isCalibrationEqualToApp(CalibrationData calibrationData) {
		CalibrationData calibApp = calibrationDataForCurrentWatch();

		if (calibApp != null) {
			boolean value =  (calibApp.getStepCalibration() == calibrationData.getStepCalibration() 
					//&& calibApp.getDistanceCalibrationWalk() == calibrationData.getDistanceCalibrationWalk()
					&& calibApp.getCaloriesCalibration() == calibrationData.getCaloriesCalibration() && calibApp.getAutoEL() == calibrationData.getAutoEL());
			return value;
		}
		return false;
	}

	public boolean isWakeupSettingEqualToApp(WakeupSetting wakeupSetting) {
		WakeupSetting wakeupSettingApp = wakeupSettingForCurrentWatch();

		if (wakeupSettingApp != null) {
			boolean value = false;
			if (wakeupSetting.isEnabled() == false && wakeupSettingApp.isEnabled() == false)
				value = true;
			else
				value = (wakeupSetting.isEnabled() == wakeupSettingApp.isEnabled() && wakeupSetting.getWakeupTimeHour() == wakeupSettingApp.getWakeupTimeHour() &&
				wakeupSetting.getWakeupTimeMinute() == wakeupSettingApp.getWakeupTimeMinute() && wakeupSetting.getWindow() == wakeupSettingApp.getWindow());
			return value;
		}

		return false;
	}

	public boolean isDaylightSettingEqualToApp(DayLightDetectSetting daylightSetting) {
		DayLightDetectSetting daylightSettingApp = daylightSettingForCurrentWatch();

		if (daylightSettingApp != null) {
			boolean value = false;
			if (daylightSetting.isEnabled() == false && daylightSettingApp.isEnabled() == false)
				value = true;
			else
				value = (daylightSetting.isEnabled() == daylightSettingApp.isEnabled() && daylightSetting.getExposureLevel() == daylightSettingApp.getExposureLevel() &&
				daylightSetting.getExposureDuration() == daylightSettingApp.getExposureDuration() && daylightSetting.getStartTime() == daylightSettingApp.getStartTime() &&
				daylightSetting.getEndTime() == daylightSettingApp.getEndTime()
				&& daylightSetting.getInterval() == daylightSettingApp.getInterval() 
				&& daylightSetting.getDetectHighThreshold() == daylightSettingApp.getDetectHighThreshold() &&
				daylightSetting.getDetectMediumThreshold() == daylightSettingApp.getDetectMediumThreshold() && daylightSetting.getDetectLowThreshold() == daylightSettingApp.getDetectLowThreshold());
			return value;
		}
		return false;
	}

	public boolean isNightlightSettingEqualToApp(NightLightDetectSetting nightlightSetting) {
		NightLightDetectSetting nightlightSettingApp = nightlightSettingForCurrentWatch();

		if (nightlightSettingApp != null) {
			boolean value = false;
			if (nightlightSetting.isEnabled() == false && nightlightSettingApp.isEnabled() == false)
				value = true;
			else
				value = (nightlightSetting.isEnabled() == nightlightSettingApp.isEnabled() && nightlightSetting.getExposureLevel() == nightlightSettingApp.getExposureLevel() &&
				nightlightSetting.getExposureDuration() == nightlightSettingApp.getExposureDuration() && nightlightSetting.getStartTime() == nightlightSettingApp.getStartTime() &&
				nightlightSetting.getEndTime() == nightlightSettingApp.getEndTime());
			return value;
		}
		return false;
	}

	public boolean isActivitySettingEqualToApp(ActivityAlertSetting activitySetting) {
		ActivityAlertSetting activitySettingApp = activityAlertForCurrentWatch();

		if (activitySettingApp != null) {
			boolean value = false;
			if (activitySetting.isEnabled() == false && activitySettingApp.isEnabled() == false)
				value = true;
			else
				value = (activitySetting.isEnabled() == activitySettingApp.isEnabled() && activitySetting.getTimeInterval() == activitySettingApp.getTimeInterval() &&
				activitySetting.getStartTime() == activitySettingApp.getStartTime() && activitySetting.getEndTime() == activitySettingApp.getEndTime() && activitySetting.getStepsThreshold() == activitySettingApp.getStepsThreshold());
			return value;
		}
		return false;
	}


	public boolean isProfileSettingEqualToApp(UserProfile userProfile) {
		UserProfile userProfileApp = userProfileForCurrentWatch();

		if (userProfileApp != null) {
			boolean value = (userProfile.getWeight() == userProfileApp.getWeight() && userProfile.getHeight() == userProfileApp.getHeight() && userProfile.getBirthDay() == userProfileApp.getBirthDay() &&
					userProfile.getBirthMonth() == userProfileApp.getBirthMonth() && userProfile.getBirthYear() == userProfileApp.getBirthYear() && userProfile.getGender() == userProfileApp.getGender() && userProfile
					.getUnitSystem() == userProfileApp.getUnitSystem());
			return value;
		}
		return false;
	}

	/*
	 * From watch to app
	 * */
	public void updateGoalFromWatch(Goal goal){
		Calendar calendar = Calendar.getInstance();

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		Goal goalApp = goalForCurrentWatch();

		if (goalApp != null) {
			goalApp.setContext(mContext);
			goalApp.setDistanceGoal(goal.getDistanceGoal());
			goalApp.setStepGoal(goal.getStepGoal());
			goalApp.setCalorieGoal(goal.getCalorieGoal());
			goalApp.setSleepGoal(goal.getSleepGoal());
			goalApp.setBrightLightGoal(goal.getBrightLightGoal());
			goalApp.setContext(mContext);
			goalApp.update();
		}
		else
		{
			goalApp = new Goal(mContext);
			goalApp.setDistanceGoal(goal.getDistanceGoal());
			goalApp.setStepGoal(goal.getStepGoal());
			goalApp.setCalorieGoal(goal.getCalorieGoal());
			goalApp.setSleepGoal(goal.getSleepGoal());
			goalApp.setBrightLightGoal(goal.getBrightLightGoal());
			goalApp.setContext(mContext);
			goalApp.insert(); 
		}
	}

	public void updateTimeDateFromWatch(TimeDate timeDate, LifeTrakApplication mLifeTrakApplication) {
		TimeDate timeDateApp = timeDateForCurrentWatch();

		if (timeDateApp != null) {
			timeDateApp.setHourFormat(timeDate.getHourFormat());
			timeDateApp.setDateFormat(timeDate.getDateFormat());
			timeDateApp.setDisplaySize(timeDate.getDisplaySize());
			timeDateApp.setContext(mContext);
			timeDateApp.update();
		}
		else{
			timeDateApp = new TimeDate();
			timeDateApp.setHourFormat(timeDate.getHourFormat());
			timeDateApp.setDateFormat(timeDate.getDateFormat());
			timeDateApp.setDisplaySize(timeDate.getDisplaySize());
			timeDateApp.setContext(mContext);
			timeDateApp.insert();
		}
		mLifeTrakApplication.getSelectedWatch().setTimeDate(timeDateApp);
		mLifeTrakApplication.getSelectedWatch().getTimeDate().setDisplaySize(timeDate.getDisplaySize());
		mLifeTrakApplication.getSelectedWatch().getTimeDate().setHourFormat(timeDate.getHourFormat());
	}

	public void updateCalibrationFromWatch(CalibrationData calibrationData) {
		CalibrationData calibApp = calibrationDataForCurrentWatch();

		if (calibApp != null) {
			calibApp.setContext(mContext);
			calibApp.setStepCalibration(calibrationData.getStepCalibration());
			calibApp.setDistanceCalibrationWalk(calibrationData.getDistanceCalibrationWalk());
			calibApp.setCaloriesCalibration(calibrationData.getCaloriesCalibration());
			calibApp.setAutoEL(calibrationData.getAutoEL());
			calibApp.setContext(mContext);
			calibApp.update();
		}
		else
		{
			calibApp =new CalibrationData(mContext);
			calibApp.setStepCalibration(calibrationData.getStepCalibration());
			calibApp.setDistanceCalibrationWalk(calibrationData.getDistanceCalibrationWalk());
			calibApp.setCaloriesCalibration(calibrationData.getCaloriesCalibration());
			calibApp.setAutoEL(calibrationData.getAutoEL());
			calibApp.setContext(mContext);
			calibApp.insert();
		}
	}

	public void updateWakeupSettingFromWatch(WakeupSetting wakeupSetting) {
		WakeupSetting wakeupSettingApp = wakeupSettingForCurrentWatch();

		if (wakeupSettingApp != null) {
			wakeupSettingApp.setContext(mContext);
			wakeupSettingApp.setEnabled(wakeupSetting.isEnabled());
			wakeupSettingApp.setWakeupTimeHour(wakeupSetting.getWakeupTimeHour());
			wakeupSettingApp.setWakeupTimeMinute(wakeupSetting.getWakeupTimeMinute());
			wakeupSettingApp.setWindow(wakeupSetting.getWindow());
			wakeupSettingApp.setContext(mContext);
			wakeupSettingApp.update();
		}
		else
		{
			wakeupSettingApp = new WakeupSetting(mContext);
			wakeupSettingApp.setEnabled(wakeupSetting.isEnabled());
			wakeupSettingApp.setWakeupTimeHour(wakeupSetting.getWakeupTimeHour());
			wakeupSettingApp.setWakeupTimeMinute(wakeupSetting.getWakeupTimeMinute());
			wakeupSettingApp.setWindow(wakeupSetting.getWindow());
			wakeupSettingApp.setContext(mContext);
			wakeupSettingApp.insert();
		}
	}



	public void updateNightlightSettingFromWatch(NightLightDetectSetting nightlightSetting) {
		NightLightDetectSetting nightlightSettingApp = nightlightSettingForCurrentWatch();

		if (nightlightSettingApp != null) {
			nightlightSettingApp.setContext(mContext);
			nightlightSettingApp.setEnabled(nightlightSetting.isEnabled());
			nightlightSettingApp.setExposureLevel(nightlightSetting.getExposureLevel());
			nightlightSettingApp.setExposureDuration(nightlightSetting.getExposureDuration());
			nightlightSettingApp.setStartTime(nightlightSetting.getStartTime());
			nightlightSettingApp.setEndTime(nightlightSetting.getEndTime());
			nightlightSettingApp.setContext(mContext);
			nightlightSettingApp.update();
		}
		else{
			nightlightSettingApp = new NightLightDetectSetting(mContext);
			nightlightSettingApp.setEnabled(nightlightSetting.isEnabled());
			nightlightSettingApp.setExposureLevel(nightlightSetting.getExposureLevel());
			nightlightSettingApp.setExposureDuration(nightlightSetting.getExposureDuration());
			nightlightSettingApp.setStartTime(nightlightSetting.getStartTime());
			nightlightSettingApp.setEndTime(nightlightSetting.getEndTime());
			nightlightSettingApp.setContext(mContext);
			nightlightSettingApp.update();
		}
	}


	public void updateActivitySettingFromWatch(ActivityAlertSetting activitySetting) {
		ActivityAlertSetting activitySettingApp = activityAlertForCurrentWatch();

		if (activitySettingApp != null) {
			activitySettingApp.setContext(mContext);
			activitySettingApp.setEnabled(activitySetting.isEnabled());
			activitySettingApp.setTimeInterval(activitySetting.getTimeInterval());
			activitySettingApp.setStartTime(activitySetting.getStartTime());
			activitySettingApp.setEndTime(activitySetting.getEndTime());
			activitySettingApp.setStepsThreshold(activitySetting.getStepsThreshold());
			activitySettingApp.setContext(mContext);
			activitySettingApp.update();
		}
		else{
			activitySettingApp = new ActivityAlertSetting(mContext);
			activitySettingApp.setEnabled(activitySetting.isEnabled());
			activitySettingApp.setTimeInterval(activitySetting.getTimeInterval());
			activitySettingApp.setStartTime(activitySetting.getStartTime());
			activitySettingApp.setEndTime(activitySetting.getEndTime());
			activitySettingApp.setStepsThreshold(activitySetting.getStepsThreshold());
			activitySettingApp.setContext(mContext);
			activitySettingApp.insert();
		}
	}

	public void updateDaylightSettingFromWatch(DayLightDetectSetting daylightSetting) {
		DayLightDetectSetting daylightSettingApp = daylightSettingForCurrentWatch();

		if (daylightSettingApp != null) {
			daylightSettingApp.setContext(mContext);
			daylightSettingApp.setEnabled(daylightSetting.isEnabled());
			daylightSettingApp.setExposureLevel(daylightSetting.getExposureLevel());
			daylightSettingApp.setExposureDuration(daylightSetting.getExposureDuration());
			daylightSettingApp.setStartTime(daylightSetting.getStartTime());
			daylightSettingApp.setEndTime(daylightSetting.getEndTime());
			daylightSettingApp.setInterval(daylightSetting.getInterval());
			daylightSettingApp.setDetectHighThreshold(daylightSetting.getDetectHighThreshold());
			daylightSettingApp.setDetectMediumThreshold(daylightSetting.getDetectMediumThreshold());
			daylightSettingApp.setDetectLowThreshold(daylightSetting.getDetectLowThreshold());
			daylightSettingApp.setContext(mContext);
			daylightSettingApp.update();
		}
		else{
			daylightSettingApp = new DayLightDetectSetting(mContext);
			daylightSettingApp.setEnabled(daylightSetting.isEnabled());
			daylightSettingApp.setExposureLevel(daylightSetting.getExposureLevel());
			daylightSettingApp.setExposureDuration(daylightSetting.getExposureDuration());
			daylightSettingApp.setStartTime(daylightSetting.getStartTime());
			daylightSettingApp.setEndTime(daylightSetting.getEndTime());
			daylightSettingApp.setInterval(daylightSetting.getInterval());
			daylightSettingApp.setDetectHighThreshold(daylightSetting.getDetectHighThreshold());
			daylightSettingApp.setDetectMediumThreshold(daylightSetting.getDetectMediumThreshold());
			daylightSettingApp.setDetectLowThreshold(daylightSetting.getDetectLowThreshold());
			daylightSettingApp.setContext(mContext);
			daylightSettingApp.insert();
		}
	}

	public void updateProfileSettingFromWatch(UserProfile userProfile, LifeTrakApplication mLifeTrakApplication) {
		UserProfile userProfileApp = userProfileForCurrentWatch();

		if (userProfileApp != null) {
            userProfileApp.setContext(mContext);
			userProfileApp.setWeight(userProfile.getWeight());
			userProfileApp.setHeight(userProfile.getHeight());
			userProfileApp.setGender(userProfile.getGender());
			userProfileApp.setUnitSystem(userProfile.getUnitSystem());	
			userProfileApp.setFirstname(userProfileApp.getFirstname());
			userProfileApp.setLastname(userProfileApp.getLastname());
			userProfileApp.setEmail(userProfileApp.getEmail());
            userProfileApp.setBirthDay(userProfile.getBirthDay());
            userProfileApp.setBirthMonth(userProfile.getBirthMonth());
            userProfileApp.setBirthYear(userProfile.getBirthYear());
            userProfileApp.setContext(mContext);
			userProfileApp.update();
		}
		else {
			userProfileApp = new UserProfile();
			userProfileApp.setContext(mContext);
			userProfileApp.setWeight(userProfile.getWeight());
			userProfileApp.setHeight(userProfile.getHeight());
			userProfileApp.setBirthDay(userProfile.getBirthDay());
			userProfileApp.setBirthMonth(userProfile.getBirthMonth());
			userProfileApp.setBirthYear(userProfile.getBirthYear());
			userProfileApp.setGender(userProfile.getGender());
			userProfileApp.setUnitSystem(userProfile.getUnitSystem());	
			userProfileApp.setFirstname(userProfileApp.getFirstname());
			userProfileApp.setLastname(userProfileApp.getLastname());
			userProfileApp.setEmail(userProfileApp.getEmail());
            userProfileApp.setContext(mContext);
			userProfileApp.insert();
		}

		mLifeTrakApplication.getSelectedWatch().setUserProfile(userProfileApp);
		
	}


	/*
	 * From app to watch
	 * */

	private void sleeptime() throws InterruptedException{
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
			//lollipop
			TimeUnit.MILLISECONDS.sleep(2000);
		}
		else {
			//kitkat
			TimeUnit.MILLISECONDS.sleep(750);
		}
	}

	public void updateGoalFromApp(SALBLEService service, Goal goal) throws InterruptedException {
		sleeptime();
		updateDistanceGoalFromApp(service, (long) (goal.getDistanceGoal() * 100.0));
		sleeptime();
		updateStepGoalFromApp(service, goal.getStepGoal());
		sleeptime();
		updateCalorieGoalFromApp(service, goal.getCalorieGoal());
		sleeptime();
		updateSleepGoalFromApp(service, goal.getSleepGoal());
		sleeptime();
		updateBrightLightGoalFromApp(service, goal.getBrightLightGoal());
		sleeptime();

		/*SALConnectionSetting setting = new SALConnectionSetting();
		setting.setBLEWristOffOperationStatus(SALConnectionSetting.ENABLE);
		LifeTrakLogger.info("Update Calibration WristOffStatus = " + String.valueOf(SALConnectionSetting.ENABLE));
		int returnVal = service.updateConnectionSettingData(setting);
		LifeTrakLogger.info("Update Calibration WristOffStatus Update Status = " + String.valueOf(returnVal));*/
		sleeptime();
	}

    public void updateNotification(SALBLEService service, Notification notification) throws InterruptedException {
        int status = 1;

        LifeTrakLogger.info("Update Notification isEmailEnabled = " + String.valueOf(notification.isEmailEnabled() ? 1 : 0));
        status = service.updateNotificationStatus(SALNotification.NOTIFICATION_ID_EMAIL, notification.isEmailEnabled() ? 1 : 0);
        LifeTrakLogger.info("Update Notification isEmailEnabled Update Status = " + String.valueOf(status));
		sleeptime();

        LifeTrakLogger.info("Update Notification isNewsEnabled = " + String.valueOf(notification.isNewsEnabled() ? 1 : 0));
        status = service.updateNotificationStatus(SALNotification.NOTIFICATION_ID_NEWS, notification.isNewsEnabled() ? 1 : 0);
        LifeTrakLogger.info("Update Notification isNewsEnabled Update Status = " + String.valueOf(status));
		sleeptime();

        LifeTrakLogger.info("Update Notification isIncomingCallEnabled = " + String.valueOf(notification.isIncomingCallEnabled() ? 1 : 0));
        status = service.updateNotificationStatus(SALNotification.NOTIFICATION_ID_CALL, notification.isIncomingCallEnabled() ? 1 : 0);
        LifeTrakLogger.info("Update Notification isIncomingCallEnabled Update Status = " + String.valueOf(status));
        sleeptime();

        LifeTrakLogger.info("Update Notification isMissedCallEnabled = " + String.valueOf(notification.isMissedCallEnabled() ? 1 : 0));
        status =  service.updateNotificationStatus(SALNotification.NOTIFICATION_ID_MISSED, notification.isMissedCallEnabled() ? 1 : 0);
        LifeTrakLogger.info("Update Notification isMissedCallEnabled Update Status = " + String.valueOf(status));
        sleeptime();

        LifeTrakLogger.info("Update Notification isSmsEnabled = " + String.valueOf(notification.isSmsEnabled() ? 1 : 0));
        status = service.updateNotificationStatus(SALNotification.NOTIFICATION_ID_SMS, notification.isSmsEnabled() ? 1 : 0);
        LifeTrakLogger.info("Update Notification isSmsEnabled Update Status = " + String.valueOf(status));
        sleeptime();

        LifeTrakLogger.info("Update Notification isVoiceMailEnabled = " + String.valueOf(notification.isVoiceMailEnabled() ? 1 : 0));
        status =  service.updateNotificationStatus(SALNotification.NOTIFICATION_ID_VOICEMAIL, notification.isVoiceMailEnabled() ? 1 : 0);
        LifeTrakLogger.info("Update Notification isVoiceMailEnabled Update Status = " + String.valueOf(status));
        sleeptime();

        LifeTrakLogger.info("Update Notification isScheduleEnabled = " + String.valueOf(notification.isScheduleEnabled() ? 1 : 0));
        status = service.updateNotificationStatus(SALNotification.NOTIFICATION_ID_SCHEDULE, notification.isScheduleEnabled() ? 1 : 0);
        LifeTrakLogger.info("Update Notification isScheduleEnabled Update Status = " + String.valueOf(status));
        sleeptime();

        LifeTrakLogger.info("Update Notification isHighPriorityEnabled = " + String.valueOf(notification.isHighPriorityEnabled() ? 1 : 0));
        status = service.updateNotificationStatus(SALNotification.NOTIFICATION_ID_HIGH_PRIORITY_ALERT, notification.isHighPriorityEnabled() ? 1 : 0);
        LifeTrakLogger.info("Update Notification isHighPriorityEnabled Update Status = " + String.valueOf(status));
        sleeptime();

        LifeTrakLogger.info("Update Notification isInstantMessageEnabled = " + String.valueOf(notification.isInstantMessageEnabled() ? 1 : 0));
        status =  service.updateNotificationStatus(SALNotification.NOTIFICATION_ID_INSTANT_MESSAGE, notification.isInstantMessageEnabled() ? 1 : 0);
        LifeTrakLogger.info("Update Notification isInstantMessageEnabled Update Status = " + String.valueOf(status));
    }

	public int updateDistanceGoalFromApp(SALBLEService service, long distanceValue){
        LifeTrakLogger.info("Update Goal updateDistanceGoal  = " + String.valueOf(distanceValue));
        int status = service.updateDistanceGoal(distanceValue);
        LifeTrakLogger.info("Update Goal updateDistanceGoal Update Status  = " + String.valueOf(status));
		return status;
	}

	public int updateStepGoalFromApp(SALBLEService service, long stepGoal){
        LifeTrakLogger.info("Update Goal stepGoal  = " + String.valueOf(stepGoal));
        int status = service.updateStepGoal(stepGoal);
        LifeTrakLogger.info("Update Goal stepGoal Update Status  = " + String.valueOf(status));
        return status;
	}

	public int updateCalorieGoalFromApp(SALBLEService service, long caloriesGoal){
        LifeTrakLogger.info("Update Goal caloriesGoal  = " + String.valueOf(caloriesGoal));
        int status =  service.updateCalorieGoal(caloriesGoal);
        LifeTrakLogger.info("Update Goal caloriesGoal Update Status  = " + String.valueOf(status));
        return status;
	}

	public int updateSleepGoalFromApp(SALBLEService service, int sleep){
		SALSleepSetting sleepSetting = new SALSleepSetting();
		sleepSetting.setSleepGoal(sleep);
		sleepSetting.setSleepDetectType(3);
        LifeTrakLogger.info("Update Goal sleep  = " + String.valueOf(sleep));
        int status = service.updateSleepSetting(sleepSetting);
        LifeTrakLogger.info("Update Goal sleep Update Status  = " + String.valueOf(status));
        return status;
	}

	public int updateBrightLightGoalFromApp(SALBLEService service, int light){
		SALDayLightDetectSetting dayLightSetting = new SALDayLightDetectSetting();
        LifeTrakLogger.info("Update Goal light  = " + String.valueOf(light));
		dayLightSetting.setExposureDuration(light);
        int status = service.updateDayLightSettingData(dayLightSetting);
        LifeTrakLogger.info("Update Goal light Update Status  = " + String.valueOf(status));
		return status;
	}

	public int updateTimeDateFromApp(SALBLEService service,TimeDate timeDate) throws InterruptedException {
		sleeptime();
		SALTimeDate salTimeDate = new SALTimeDate();

		PreferenceWrapper mPreferenceWrapper = PreferenceWrapper.getInstance(mContext);
		if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME)) {
			salTimeDate.setToNow();
		}

		salTimeDate.setTimeFormat(timeDate.getHourFormat());
        LifeTrakLogger.info("Update Time setTimeFormat = " + String.valueOf(timeDate.getHourFormat()));
		salTimeDate.setDateFormat(timeDate.getDateFormat());
        LifeTrakLogger.info("Update Time setDateFormat = " + String.valueOf(timeDate.getDateFormat()));
		salTimeDate.setTimeDisplaySize(timeDate.getDisplaySize());
        LifeTrakLogger.info("Update Time setTimeDisplaySize = " + String.valueOf(timeDate.getDisplaySize()));
        int returnVal =service.updateTimeAndDate(salTimeDate);
        LifeTrakLogger.info("Update Time Update Status  = " + String.valueOf(returnVal));

		return returnVal;
	}

	public int updateUserProfileFromApp(SALBLEService service, UserProfile userProfile) throws InterruptedException {
		sleeptime();
		SALUserProfile salUserProfile = new SALUserProfile();
		salUserProfile.setWeight(userProfile.getWeight());
        LifeTrakLogger.info("Update userProfile setWeight = " + String.valueOf(userProfile.getWeight()));
		salUserProfile.setHeight(userProfile.getHeight());
        LifeTrakLogger.info("Update userProfile setHeight = " + String.valueOf(userProfile.getHeight()));
		salUserProfile.setBirthDay(userProfile.getBirthDay());
        LifeTrakLogger.info("Update userProfile setBirthDay = " + String.valueOf(userProfile.getBirthDay()));
		salUserProfile.setBirthMonth(userProfile.getBirthMonth());
        LifeTrakLogger.info("Update userProfile setBirthMonth = " + String.valueOf(userProfile.getBirthMonth()));
		salUserProfile.setBirthYear(userProfile.getBirthYear() - 1900);
        LifeTrakLogger.info("Update userProfile setBirthYear = " + String.valueOf(userProfile.getBirthYear() - 1900));
		salUserProfile.setSensitivityLevel(userProfile.getSensitivity());
        LifeTrakLogger.info("Update userProfile setSensitivityLevel = " + String.valueOf(userProfile.getSensitivity()));
		salUserProfile.setGender(userProfile.getGender());
        LifeTrakLogger.info("Update userProfile setGender = " + String.valueOf(userProfile.getGender()));
		salUserProfile.setUnitSystem(userProfile.getUnitSystem());
        LifeTrakLogger.info("Update userProfile setUnitSystem = " + String.valueOf(userProfile.getUnitSystem()));
        int returnVal = service.updateUserProfile(salUserProfile);
        LifeTrakLogger.info("Update userProfile Update Status  = " + String.valueOf(returnVal));
		return	returnVal;
	}

	public void updateCalibrationFromApp(SALBLEService service, CalibrationData calibrationData) throws InterruptedException {
        int returnVal = 0;
		sleeptime();
		SALCalibration calibApp = new SALCalibration();
		calibApp.setCalibrationType(SALCalibration.STEP_CALIBRATION);
		calibApp.setStepCalibration(calibrationData.getStepCalibration());
        LifeTrakLogger.info("Update Calibration setStepCalibration = " + String.valueOf(calibrationData.getStepCalibration()));
        returnVal = service.updateCalibrationData(calibApp);
        LifeTrakLogger.info("Update Calibration setStepCalibration Update Status = " + String.valueOf(returnVal));


		sleeptime();
		calibApp.setCalibrationType(SALCalibration.WALK_DISTANCE_CALIBRATION);
		calibApp.setWalkDistanceCalibration(calibrationData.getDistanceCalibrationWalk());
        LifeTrakLogger.info("Update Calibration setWalkDistanceCalibration = " + String.valueOf(calibrationData.getDistanceCalibrationWalk()));
        returnVal = service.updateCalibrationData(calibApp);
        LifeTrakLogger.info("Update Calibration setStepCalibration Update Status = " + String.valueOf(returnVal));
		sleeptime();
		calibApp.setCalibrationType(SALCalibration.CALORIE_CALIBRATION);
		calibApp.setCalorieCalibration(calibrationData.getCaloriesCalibration());
        LifeTrakLogger.info("Update Calibration setCalorieCalibration = " + String.valueOf(calibrationData.getCaloriesCalibration()));
        returnVal = service.updateCalibrationData(calibApp);
        LifeTrakLogger.info("Update Calibration setStepCalibration Update Status = " + String.valueOf(returnVal));

		//service.updateCalibrationData(calibApp);

		sleeptime();
		calibApp.setCalibrationType(SALCalibration.AUTO_EL_SETTING);
		calibApp.setAutoELStatus(SALCalibration.AUTO_EL_ON);
        LifeTrakLogger.info("Update Calibration setAutoELStatus = " + String.valueOf(SALCalibration.AUTO_EL_ON));
        returnVal = service.updateCalibrationData(calibApp);
        LifeTrakLogger.info("Update Calibration setStepCalibration Update Status = " + String.valueOf(returnVal));

		sleeptime();
		SALConnectionSetting setting = new SALConnectionSetting();
		setting.setBLEWristOffOperationStatus(SALConnectionSetting.ENABLE);
		LifeTrakLogger.info("Update Calibration WristOffStatus = " + String.valueOf(SALConnectionSetting.ENABLE));
		returnVal = service.updateConnectionSettingData(setting);
		LifeTrakLogger.info("Update Calibration WristOffStatus Update Status = " + String.valueOf(returnVal));
	}

	public void updateWakeupSetting(SALBLEService service, WakeupSetting wakeupSetting) throws InterruptedException{
		sleeptime();

        int status = 1;

		SALWakeupSetting salWakeupSetting = new SALWakeupSetting();
		salWakeupSetting.setWakeupSetting(SALWakeupSetting.WAKEUP_ALERT_STATUS, wakeupSetting.isEnabled() ? SALWakeupSetting.ENABLE : SALWakeupSetting.DISABLE);
        LifeTrakLogger.info("Update WakeupSetting isEnabled = " + String.valueOf(wakeupSetting.isEnabled()));
        status = service.updateWakeupSettingData(salWakeupSetting);
        LifeTrakLogger.info("Update WakeupSetting Update Status = " + String.valueOf(status));
        sleeptime();

		int hours = wakeupSetting.getTime() / 60;
		int minutes = wakeupSetting.getTime() % 60;
		salWakeupSetting.setWakeupSetting(SALWakeupSetting.WAKEUP_TIME, (hours << 8) + minutes);
        LifeTrakLogger.info("Update WakeupSetting getTime Hour = " + String.valueOf(hours) + "Min = " + String.valueOf(minutes));
        status = service.updateWakeupSettingData(salWakeupSetting);
        LifeTrakLogger.info("Update WakeupSetting Update Status = " + String.valueOf(status));
		sleeptime();

		salWakeupSetting.setWakeupSetting(SALWakeupSetting.WAKEUP_WINDOW, wakeupSetting.getWindow());
        LifeTrakLogger.info("Update WakeupSetting setWindow = " + String.valueOf(wakeupSetting.getWindow()));
        status = service.updateWakeupSettingData(salWakeupSetting);
        LifeTrakLogger.info("Update WakeupSetting Update Status = " + String.valueOf(status));
		sleeptime();

		salWakeupSetting.setWakeupSetting(SALWakeupSetting.SNOOZE_ALERT_STATUS, (wakeupSetting.isSnoozeEnabled()) ? SALWakeupSetting.ENABLE : SALWakeupSetting.DISABLE);
        LifeTrakLogger.info("Update WakeupSetting isSnoozeEnabled = " + String.valueOf(wakeupSetting.isSnoozeEnabled()));
        status = service.updateWakeupSettingData(salWakeupSetting);
        LifeTrakLogger.info("Update WakeupSetting Update Status = " + String.valueOf(status));
		sleeptime();

		salWakeupSetting.setWakeupSetting(SALWakeupSetting.SNOOZE_TIME, wakeupSetting.getSnoozeTime());
        LifeTrakLogger.info("Update WakeupSetting setSnoozeTime = " + String.valueOf(wakeupSetting.getSnoozeTime()));
        status = service.updateWakeupSettingData(salWakeupSetting);
        LifeTrakLogger.info("Update WakeupSetting Update Status = " + String.valueOf(status));

	}



	public void updateDaylightAlertSetting(SALBLEService service, DayLightDetectSetting daylightSetting) throws InterruptedException {
		sleeptime();

        int status = 1;

		SALDayLightDetectSetting salDayLightDetectSetting = new SALDayLightDetectSetting();
		salDayLightDetectSetting.setLightDetectSetting(SALDayLightDetectSetting.DETECT_STATUS, (daylightSetting.isEnabled()) ? SALDayLightDetectSetting.ENABLE : SALDayLightDetectSetting.DISABLE);
        LifeTrakLogger.info("Update Daylight isEnabled = " + String.valueOf(daylightSetting.isEnabled()));
        status = service.updateDayLightSettingData(salDayLightDetectSetting);
        LifeTrakLogger.info("Update Daylight Update Status = " + String.valueOf(status));
		sleeptime();

		int hours = daylightSetting.getStartTime() / 60;
		int minutes = daylightSetting.getStartTime() - (hours * 60);

		salDayLightDetectSetting.setStartTime(hours, minutes);
        LifeTrakLogger.info("Update Daylight setStartTime Hour = " + String.valueOf(hours) + " Min = " + String.valueOf(minutes));
        status = service.updateDayLightSettingData(salDayLightDetectSetting);
        LifeTrakLogger.info("Update Daylight Update Status = " + String.valueOf(status));
		sleeptime();

		hours = daylightSetting.getEndTime() / 60;
		minutes = daylightSetting.getStartTime() % 60;

		if (minutes < 0)
			minutes = 0;

		salDayLightDetectSetting.setEndTime(hours, minutes);
        LifeTrakLogger.info("Update Daylight setEndTime Hour = " + String.valueOf(hours) + " Min = " + String.valueOf(minutes));
        status = service.updateDayLightSettingData(salDayLightDetectSetting);
        LifeTrakLogger.info("Update Daylight Update Status = " + String.valueOf(status));
		sleeptime();

		salDayLightDetectSetting.setExposureLevel(daylightSetting.getExposureLevel());
        LifeTrakLogger.info("Update Daylight setExposureLevel = " + String.valueOf(daylightSetting.getExposureLevel()));
        status = service.updateDayLightSettingData(salDayLightDetectSetting);
        LifeTrakLogger.info("Update Daylight Update Status = " + String.valueOf(status));
		sleeptime();

		salDayLightDetectSetting.setExposureDuration(daylightSetting.getExposureDuration());
        LifeTrakLogger.info("Update Daylight setExposureDuration = " + String.valueOf(daylightSetting.getExposureDuration()));
        status = service.updateDayLightSettingData(salDayLightDetectSetting);
        LifeTrakLogger.info("Update Daylight Update Status = " + String.valueOf(status));
		sleeptime();

		salDayLightDetectSetting.setInterval(daylightSetting.getInterval());
        LifeTrakLogger.info("Update Daylight setInterval = " + String.valueOf(daylightSetting.getInterval()));
        status = service.updateDayLightSettingData(salDayLightDetectSetting);
        LifeTrakLogger.info("Update Daylight Update Status = " + String.valueOf(status));

	}

    public void updateNightlightSetting(SALBLEService service, NightLightDetectSetting nightlightSetting) throws InterruptedException {
		sleeptime();

        int status = 1;

		SALNightLightDetectSetting salNightLightDetectSetting = new SALNightLightDetectSetting();
		salNightLightDetectSetting.setLightDetectStatus((nightlightSetting.isEnabled()) ? SALNightLightDetectSetting.ENABLE : SALNightLightDetectSetting.DISABLE);
        LifeTrakLogger.info("Update NightLight isEnabled = " + String.valueOf(nightlightSetting.isEnabled()));
        status = service.updateNightLightSettingData(salNightLightDetectSetting);
        LifeTrakLogger.info("Update NightLight Update Status = " + String.valueOf(status));
		sleeptime();

		int hours = nightlightSetting.getStartTime() / 60;
		int minutes = nightlightSetting.getStartTime() % 60;
		salNightLightDetectSetting.setStartTime(hours, minutes);
        LifeTrakLogger.info("Update NightLight setStartTime Hour = " + String.valueOf(hours) + " Min = " + String.valueOf(minutes));
        status = service.updateNightLightSettingData(salNightLightDetectSetting);
        LifeTrakLogger.info("Update NightLight Update Status = " + String.valueOf(status));
		sleeptime();

		hours = nightlightSetting.getEndTime() / 60;
		minutes = nightlightSetting.getEndTime() - (hours * 60);
		salNightLightDetectSetting.setEndTime(hours, minutes);
        LifeTrakLogger.info("Update NightLight setEndTime Hour = " + String.valueOf(hours) + " Min = " + String.valueOf(minutes));
        status = service.updateNightLightSettingData(salNightLightDetectSetting);
        LifeTrakLogger.info("Update NightLight Update Status = " + String.valueOf(status));
		sleeptime();

		salNightLightDetectSetting.setExposureLevel(nightlightSetting.getExposureLevel());
        LifeTrakLogger.info("Update NightLight setExposureLevel = " + String.valueOf(nightlightSetting.getExposureLevel()));
        status = service.updateNightLightSettingData(salNightLightDetectSetting);
        LifeTrakLogger.info("Update NightLight Update Status = " + String.valueOf(status));
		sleeptime();

		salNightLightDetectSetting.setExposureDuration(nightlightSetting.getExposureDuration());
        LifeTrakLogger.info("Update NightLight setExposureDuration = " + String.valueOf(nightlightSetting.getExposureDuration()));
        status = service.updateNightLightSettingData(salNightLightDetectSetting);
        LifeTrakLogger.info("Update NightLight Update Status = " + String.valueOf(status));
	}

	public void updateActivityAlertSetting(SALBLEService service, ActivityAlertSetting activityAlertSetting) throws InterruptedException {
		sleeptime();

        int status = 1;

		SALActivityAlertSetting salActivityAlertSetting = new SALActivityAlertSetting();
		salActivityAlertSetting.setActivityAlertStatus((activityAlertSetting.isEnabled()) ? SALActivityAlertSetting.ENABLE : SALActivityAlertSetting.DISABLE);
        LifeTrakLogger.info("Update ActivityAlert isEnabled = " + String.valueOf(activityAlertSetting.isEnabled()));
        status = service.updateActivityAlertSettingData(salActivityAlertSetting);
        LifeTrakLogger.info("Update ActivityAlert Update Status = " + String.valueOf(status));
		sleeptime();

		int hours = activityAlertSetting.getStartTime() / 60;
		int minutes = activityAlertSetting.getStartTime() % 60;
		salActivityAlertSetting.setStartTime(hours, minutes);
        LifeTrakLogger.info("Update ActivityAlert setStartTime Hour = " + String.valueOf(hours) + " Min = " + String.valueOf(minutes));
        status = service.updateActivityAlertSettingData(salActivityAlertSetting);
        LifeTrakLogger.info("Update ActivityAlert Update Status = " + String.valueOf(status));
		sleeptime();

		hours = activityAlertSetting.getEndTime() / 60;
		minutes = activityAlertSetting.getEndTime() - (hours * 60);
		salActivityAlertSetting.setEndTime(hours, minutes);
        LifeTrakLogger.info("Update ActivityAlert setEndTime Hour = " + String.valueOf(hours) + " Min = " + String.valueOf(minutes));
        status = service.updateActivityAlertSettingData(salActivityAlertSetting);
        LifeTrakLogger.info("Update ActivityAlert Update Status = " + String.valueOf(status));
		sleeptime();

		salActivityAlertSetting.setTimeInterval(activityAlertSetting.getTimeInterval());
        LifeTrakLogger.info("Update ActivityAlert setTimeInterval = " + String.valueOf(activityAlertSetting.getTimeInterval()));
        status = service.updateActivityAlertSettingData(salActivityAlertSetting);
        LifeTrakLogger.info("Update ActivityAlert Update Status = " + String.valueOf(status));
		sleeptime();

		salActivityAlertSetting.setStepsThreshold(activityAlertSetting.getStepsThreshold());
        LifeTrakLogger.info("Update ActivityAlert setStepsThreshold = " + String.valueOf(activityAlertSetting.getStepsThreshold()));
        status = service.updateActivityAlertSettingData(salActivityAlertSetting);
        LifeTrakLogger.info("Update ActivityAlert Update Status = " + String.valueOf(status));

	}

	public void updateAllSettingsFromApp(SALBLEService service) {
		Goal goal = goalForCurrentWatch();
		TimeDate timeDate = timeDateForCurrentWatch();
		UserProfile userProfile = userProfileForCurrentWatch();
		CalibrationData calibrationData = calibrationDataForCurrentWatch();
		WakeupSetting wakeupSetting = wakeupSettingForCurrentWatch();
		DayLightDetectSetting dayLightDetectSetting = daylightSettingForCurrentWatch();
		NightLightDetectSetting nightLightDetectSetting = nightlightSettingForCurrentWatch();
		ActivityAlertSetting activityAlertSetting = activityAlertForCurrentWatch();
        Notification mNotification = notificationForCurrentWatch();

		try {
			if (goal != null)
				updateGoalFromApp(service, goal);
			if (timeDate != null)
				updateTimeDateFromApp(service, timeDate);
			if (userProfile != null)
				updateUserProfileFromApp(service, userProfile);
			if (calibrationData != null)
				updateCalibrationFromApp(service, calibrationData);
			if (wakeupSetting != null)
				updateWakeupSetting(service, wakeupSetting);
			if (dayLightDetectSetting != null)
				updateDaylightAlertSetting(service, dayLightDetectSetting);
			if (nightLightDetectSetting != null)
				updateNightlightSetting(service, nightLightDetectSetting);
			if (activityAlertSetting != null)
				updateActivityAlertSetting(service, activityAlertSetting);
            if (mNotification != null)
                updateNotification(service, mNotification);
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    public void updateAllSettingsFromWatch(LifeTrakApplication mLifeTrakApplication) {
        updateGoalFromWatch(mWatch.getGoal().get(mWatch.getGoals().size() - 1));
        updateTimeDateFromWatch(mWatch.getTimeDate(), mLifeTrakApplication);
        updateProfileSettingFromWatch(mWatch.getUserProfile(), mLifeTrakApplication);
        updateCalibrationFromWatch(mWatch.getCalibrationData());
        updateWakeupSettingFromWatch(mWatch.getWakeupSetting());
        updateDaylightSettingFromWatch(mWatch.getDayLightDetectSetting());
        updateNightlightSettingFromWatch(mWatch.getNightLightDetectSetting());
        updateActivitySettingFromWatch(mWatch.getActivityAlertSetting());
    }
}


