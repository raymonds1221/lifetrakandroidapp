package com.salutron.lifetrakwatchapp.util;

import android.content.Context;
import android.os.Build;

import com.salutron.blesdk.SALActivityAlertSetting;
import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALCalibration;
import com.salutron.blesdk.SALDayLightDetectSetting;
import com.salutron.blesdk.SALNightLightDetectSetting;
import com.salutron.blesdk.SALNotification;
import com.salutron.blesdk.SALSleepSetting;
import com.salutron.blesdk.SALTimeDate;
import com.salutron.blesdk.SALUserProfile;
import com.salutron.blesdk.SALWakeupSetting;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.ActivityAlertSetting;
import com.salutron.lifetrakwatchapp.model.CalibrationData;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.NightLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.Notification;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.WakeupSetting;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.WorkoutSettings;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by rsarmiento on 3/27/15.
 */
public class LifeTrakUpdateR420 implements  SalutronLifeTrakUtility{
	public static final Object LOCK_OBJECT = LifeTrakUpdateR420.class;
	public static LifeTrakUpdateR420 mLifeTrakUpdateR450;
	private Context mContext;
	private Watch mWatch;

	public static LifeTrakUpdateR420 newInstance(Context context) {
		synchronized (LOCK_OBJECT) {
			mLifeTrakUpdateR450 = new LifeTrakUpdateR420(context);
			return mLifeTrakUpdateR450;
		}
	}

	public static LifeTrakUpdateR420 newInstance(Context context, Watch watch) {
		synchronized (LOCK_OBJECT) {
			mLifeTrakUpdateR450 = new LifeTrakUpdateR420(context, watch);
			return mLifeTrakUpdateR450;
		}
	}

	private LifeTrakUpdateR420(Context context) {
		mContext = context;
	}

	private LifeTrakUpdateR420(Context context, Watch watch) {
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

    public WorkoutSettings workoutSettingsForCurrentWatch() {
		List<WorkoutSettings> userWorkOutFromDB = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchDataHeader = ?", String.valueOf(mWatch.getId()))
				.getResults(WorkoutSettings.class);



		if (userWorkOutFromDB.size() > 0) {
            return userWorkOutFromDB.get(0);
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
					goal.getCalorieGoal() == goalApp.getCalorieGoal());
			return value;
		}

		return false;
	}

	public boolean isTimeDateEqualToApp(TimeDate timeDate) {
		TimeDate timeDateApp = timeDateForCurrentWatch();

		if (timeDateApp != null) {
			boolean value =
					timeDate.getHourFormat() == timeDateApp.getHourFormat()  &&
							timeDate.getDateFormat() == timeDateApp.getDateFormat();
			return value;
		}
		return false;
	}

	public boolean isCalibrationEqualToApp(CalibrationData calibrationData) {
		CalibrationData calibApp = calibrationDataForCurrentWatch();

		if (calibApp != null) {
			boolean value =  (calibApp.getStepCalibration() == calibrationData.getStepCalibration()
					//&& calibApp.getDistanceCalibrationWalk() == calibrationData.getDistanceCalibrationWalk()
					&& calibApp.getCaloriesCalibration() == calibrationData.getCaloriesCalibration());
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
			//goalApp.setBrightLightGoal(goal.getBrightLightGoal());
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
			goalApp.setWatch(mWatch);//goalApp.setBrightLightGoal(goal.getBrightLightGoal());
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
			timeDateApp.setWatch(mWatch);
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
			calibApp.setWatch(mWatch);
			calibApp.insert();
		}
	}

	public void updateWorkoutSettingsFromWatch(WorkoutSettings workoutSettings) {
		WorkoutSettings mWorkoutSettings = workoutSettingsForCurrentWatch();

		if (mWorkoutSettings != null) {
			mWorkoutSettings.setContext(mContext);
			mWorkoutSettings.setHrLoggingRate(workoutSettings.getHrLoggingRate());
			mWorkoutSettings.setDatabaseUsage(workoutSettings.getDatabaseUsage());
			mWorkoutSettings.setDatabaseUsageMax(workoutSettings.getDatabaseUsageMax());
			mWorkoutSettings.setContext(mContext);
			mWorkoutSettings.update();
		}
		else
		{
			mWorkoutSettings =new WorkoutSettings();
			mWorkoutSettings.setHrLoggingRate(workoutSettings.getHrLoggingRate());
			mWorkoutSettings.setDatabaseUsage(workoutSettings.getDatabaseUsage());
			mWorkoutSettings.setDatabaseUsageMax(workoutSettings.getDatabaseUsageMax());
			mWorkoutSettings.setWatch(mWatch);
			mWorkoutSettings.setContext(mContext);
			mWorkoutSettings.insert();
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
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
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

	public int updateTimeDateFromApp(SALBLEService service,TimeDate timeDate, TimeDate oldTimeDate) throws InterruptedException {
		sleeptime();
		SALTimeDate salTimeDate = new SALTimeDate();

		PreferenceWrapper mPreferenceWrapper = PreferenceWrapper.getInstance(mContext);
		if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME)) {
			salTimeDate.setToNow();
		}
		else
		{
			salTimeDate.setTime(oldTimeDate.getSecond(), oldTimeDate.getMinute(), oldTimeDate.getHour());
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

	public int updateWorkoutSettingsFromApp(SALBLEService service, WorkoutSettings workoutSettings) throws InterruptedException {
		sleeptime();
		int returnVal = service.updateWorkoutHRLogRate(workoutSettings.getHrLoggingRate());
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
		sleeptime();
		calibApp.setCalibrationType(SALCalibration.AUTO_EL_SETTING);
		calibApp.setAutoELStatus(SALCalibration.AUTO_EL_ON);
        LifeTrakLogger.info("Update Calibration setAutoELStatus = " + String.valueOf(calibrationData.getAutoEL()));
        returnVal = service.updateCalibrationData(calibApp);
        LifeTrakLogger.info("Update Calibration setStepCalibration Update Status = " + String.valueOf(returnVal));

	}


	public void updateAllSettingsFromApp(SALBLEService service, TimeDate mTimeDate) {
		Goal goal = goalForCurrentWatch();
		TimeDate timeDate = timeDateForCurrentWatch();
		UserProfile userProfile = userProfileForCurrentWatch();
		CalibrationData calibrationData = calibrationDataForCurrentWatch();
		WorkoutSettings workoutSettings = workoutSettingsForCurrentWatch();
		try {
			if (goal != null)
				updateGoalFromApp(service, goal);
			if (timeDate != null)
				updateTimeDateFromApp(service, timeDate, mTimeDate);
			if (userProfile != null)
				updateUserProfileFromApp(service, userProfile);
			if (calibrationData != null)
				updateCalibrationFromApp(service, calibrationData);
			if (workoutSettings != null)
				updateWorkoutSettingsFromApp(service, workoutSettings);

		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    public void updateAllSettingsFromWatch(LifeTrakApplication mLifeTrakApplication, WorkoutSettings workoutSettings) {
        updateGoalFromWatch(mWatch.getGoal().get(mWatch.getGoals().size() - 1));
        updateTimeDateFromWatch(mWatch.getTimeDate(), mLifeTrakApplication);
        updateProfileSettingFromWatch(mWatch.getUserProfile(), mLifeTrakApplication);
        updateCalibrationFromWatch(mWatch.getCalibrationData());
		updateWorkoutSettingsFromWatch(workoutSettings);
    }
}


