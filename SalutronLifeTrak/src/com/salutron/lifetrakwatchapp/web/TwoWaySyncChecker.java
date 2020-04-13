package com.salutron.lifetrakwatchapp.web;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.salutron.blesdk.SALActivityAlertSetting;
import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALCalibration;
import com.salutron.blesdk.SALDateStamp;
import com.salutron.blesdk.SALDayLightDetectSetting;
import com.salutron.blesdk.SALLightDataPoint;
import com.salutron.blesdk.SALNightLightDetectSetting;
import com.salutron.blesdk.SALSleepDatabase;
import com.salutron.blesdk.SALSleepSetting;
import com.salutron.blesdk.SALStatisticalDataHeader;
import com.salutron.blesdk.SALStatisticalDataPoint;
import com.salutron.blesdk.SALStatus;
import com.salutron.blesdk.SALTimeDate;
import com.salutron.blesdk.SALTimeStamp;
import com.salutron.blesdk.SALUserProfile;
import com.salutron.blesdk.SALWakeupSetting;
import com.salutron.blesdk.SALWorkoutInfo;
import com.salutron.blesdk.SALWorkoutStopInfo;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.ActivityAlertSetting;
import com.salutron.lifetrakwatchapp.model.CalibrationData;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.LightDataPoint;
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
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;
import com.salutron.lifetrakwatchapp.util.PreferenceWrapper;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by janwelcris on 4/30/2015.
 */
public class TwoWaySyncChecker implements SalutronLifeTrakUtility {

    private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
    private final Calendar mCalendar = Calendar.getInstance();
    public static TwoWaySyncChecker mTwoWaySyncChecker;
    private static Context mContext;
    private Watch mWatch;
    private static LifeTrakApplication mLifeTrakApplication;

    private static List<StatisticalDataHeader> mStatisticalDataHeaders = new ArrayList<StatisticalDataHeader>();
    private static List<List<StatisticalDataPoint>> mStatisticalDataPoints = new ArrayList<List<StatisticalDataPoint>>();
    private static List<List<LightDataPoint>> mLightDataPoints = new ArrayList<List<LightDataPoint>>();
    private static List<WorkoutInfo> mWorkoutInfos = new ArrayList<WorkoutInfo>();
    private static List<List<WorkoutStopInfo>> mWorkoutStopInfos = new ArrayList<List<WorkoutStopInfo>>();
    private static List<SleepDatabase> mSleepDatabases = new ArrayList<SleepDatabase>();
    private static SleepSetting mSleepSetting;
    private static long mStepGoal;
    private static double mDistanceGoal;
    private static long mCalorieGoal;
    public static List<Goal> mGoals = new ArrayList<Goal>();
    public static CalibrationData mCalibrationData;
    public static WakeupSetting mWakeupSetting;
    private static Notification mNotification;
    public static ActivityAlertSetting mActivityAlertSetting;
    public static DayLightDetectSetting mDayLightDetectSetting;
    public static NightLightDetectSetting mNightLightDetectSetting;
    public static TimeDate mTimeDate;
    public static UserProfile mUserProfile;
    private static List<Integer> mHeaderIndexes = new ArrayList<Integer>();
    private static int mDataHeaderIndexForDataPoint = 0;
    private static int mDataHeaderIndexForLightPoint = 0;
    private static int mWorkoutIndex = 0;
    private static int mCalibrationIndex = 0;
    private static int mWakeupIndex = 0;
    private static int mActivityAlertIndex = 0;
    private static int mDayLightDetectIndex = 0;
    private static int mNightLightDetectIndex = 0;
    private static PreferenceWrapper mPreferenceWrapper;


    public static TwoWaySyncChecker newInstance(Context context) {
        synchronized (LOCK_OBJECT) {
            mTwoWaySyncChecker = new TwoWaySyncChecker(context);
            mLifeTrakApplication = (LifeTrakApplication) context.getApplicationContext();
            return mTwoWaySyncChecker;
        }
    }

    public static TwoWaySyncChecker newInstance(Context context, Watch watch) {
        synchronized (LOCK_OBJECT) {
            mTwoWaySyncChecker = new TwoWaySyncChecker(context, watch);
            return mTwoWaySyncChecker;
        }
    }

    private TwoWaySyncChecker(Context context) {
        mContext = context;
    }

    private TwoWaySyncChecker(Context context, Watch watch) {
        mContext = context;
        mWatch = watch;
    }

    public boolean isServerLatest(Date mdateApp, Date mdateServer){
//        if (mdateServer.getHours() < 9){
//            if(mdateApp.getHours() > 12)
//                mdateApp.setHours(mdateApp.getHours() - 12);
//        }
//
//        if (mdateServer.getHours() == 12)
//            mdateServer.setHours(mdateServer.getHours() - 12);

        if (mdateApp.getTime() >= mdateServer.getTime())
            return false;
        else
            return true;

    }

    private Goal goalForCurrentWatch(Goal goal) {
        int day = goal.getDateStampDay();
        int month = goal.getDateStampMonth();
        int year = goal.getDateStampYear();

        List<Goal> goals = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
                        String.valueOf(mWatch.getId()), String.valueOf(day), String.valueOf(month), String.valueOf(year))
                .limit(1)
                .getResults(Goal.class);

        if (goals.size() > 0) {
            return goals.get(0);
        }
        return new Goal();
    }

    public void UpdateGoals(List<Goal> goalList){
        for (int i = 0; i < goalList.size(); i++){
            Goal nGoal = goalForCurrentWatch(goalList.get(i));
            if (nGoal != null){
                nGoal.setCalorieGoal(goalList.get(i).getCalorieGoal());
                nGoal.setStepGoal(goalList.get(i).getStepGoal());
                nGoal.setDistanceGoal(goalList.get(i).getDistanceGoal());
                nGoal.setSleepGoal(goalList.get(i).getSleepGoal());
                nGoal.setDate(goalList.get(i).getDate());
                nGoal.setDateStampDay(goalList.get(i).getDateStampDay());
                nGoal.setDateStampMonth(goalList.get(i).getDateStampMonth());
                nGoal.setDateStampYear(goalList.get(i).getDateStampYear());
                nGoal.setContext(mContext);
                nGoal.update();
            }
            else{
                Goal newGoal = goalList.get(i);
                newGoal.setContext(mContext);
                newGoal.insert();
            }
        }
    }

    private TimeDate timeDateForCurrentWatch() {
        List<TimeDate> timeDates = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchTimeDate = ?", String.valueOf(mWatch.getId()))
                .getResults(TimeDate.class);

        if (timeDates.size() > 0)
            return timeDates.get(0);

        return new TimeDate();
    }

    public void UpdateTimeDate (TimeDate timeDate){
        TimeDate nTimeDate = timeDateForCurrentWatch();
        if (nTimeDate != null){
            nTimeDate.setHourFormat(timeDate.getHourFormat());
            nTimeDate.setDateFormat(timeDate.getDateFormat());
            nTimeDate.setContext(mContext);
            nTimeDate.update();
        }
        else {
            nTimeDate.setContext(mContext);
            nTimeDate.insert();
        }
    }

    private CalibrationData calibrationDataForCurrentWatch() {
        List<CalibrationData> calibrations = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchCalibrationData = ?", String.valueOf(mWatch.getId()))
                .getResults(CalibrationData.class);

        if (calibrations.size() > 0)
            return calibrations.get(0);

        return new CalibrationData();
    }

    public void UpdateCalibration (CalibrationData calibrationData){
        CalibrationData nCalibrationData = calibrationDataForCurrentWatch();
        if (nCalibrationData != null){
            calibrationData.setCalibrationType(calibrationData.getCalibrationType());
            nCalibrationData.setStepCalibration(calibrationData.getStepCalibration());
            nCalibrationData.setDistanceCalibrationWalk(calibrationData.getDistanceCalibrationWalk());
            nCalibrationData.setDistanceCalibrationRun(calibrationData.getDistanceCalibrationRun());
            nCalibrationData.setCaloriesCalibration(calibrationData.getCaloriesCalibration());
            nCalibrationData.setAutoEL(calibrationData.getAutoEL());
            nCalibrationData.setContext(mContext);
            nCalibrationData.update();
        }
        else {
            calibrationData.setContext(mContext);
            calibrationData.insert();
        }
    }

    private WakeupSetting wakeupSettingForCurrentWatch() {
        List<WakeupSetting> wakeupSettings = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchWakeupSetting = ?", String.valueOf(mWatch.getId()))
                .getResults(WakeupSetting.class);
        if (wakeupSettings.size() > 0)
            return wakeupSettings.get(0);

        return new WakeupSetting();
    }

    private UserProfile userProfileForCurrentWatch(){
        List<UserProfile> userProfiles = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchUserProfile = ?", String.valueOf(mWatch.getId()))
                .getResults(UserProfile.class);
        if (userProfiles.size() > 0)
            return userProfiles.get(0);

        return new UserProfile();
    }

    public void UpdateUserProfile (UserProfile userProfile, LifeTrakApplication mLifeTrakApplication){
        UserProfile mUserProfile = userProfileForCurrentWatch();
        if (mUserProfile != null){
            mUserProfile.setBirthDay(userProfile.getBirthDay());
            mUserProfile.setBirthMonth(userProfile.getBirthMonth());
            mUserProfile.setBirthYear(userProfile.getBirthYear());
            mUserProfile.setGender(userProfile.getGender());
            mUserProfile.setUnitSystem(userProfile.getUnitSystem());
            mUserProfile.setSensitivity(userProfile.getSensitivity());
            mUserProfile.setHeight(userProfile.getHeight());
            mUserProfile.setWeight(userProfile.getWeight());
            mUserProfile.setContext(mContext);
            mUserProfile.update();
            mLifeTrakApplication.setUserProfile(mUserProfile);
        }


    }

    public void UpdateWakeupSettings (WakeupSetting wakeupSetting){
        WakeupSetting nWakeupSetting = wakeupSettingForCurrentWatch();
        if (nWakeupSetting != null){
            nWakeupSetting.setSnoozeTime(wakeupSetting.getSnoozeTime());
            nWakeupSetting.setSnoozeEnabled(wakeupSetting.isSnoozeEnabled());
            nWakeupSetting.setTime(wakeupSetting.getTime());
            nWakeupSetting.setWakeupTimeHour(wakeupSetting.getWakeupTimeHour());
            nWakeupSetting.setWakeupTimeMinute(wakeupSetting.getWakeupTimeMinute());
            nWakeupSetting.setEnabled(wakeupSetting.isEnabled());
            nWakeupSetting.setContext(mContext);
            nWakeupSetting.update();
        }
        else {
            wakeupSetting.setContext(mContext);
            wakeupSetting.insert();
        }
    }

    private DayLightDetectSetting daylightSettingForCurrentWatch() {
        List<DayLightDetectSetting> daylightSettings = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchDaylightSetting = ?", String.valueOf(mWatch.getId()))
                .getResults(DayLightDetectSetting.class);

        if (daylightSettings.size() > 0)
            return daylightSettings.get(0);

        return new DayLightDetectSetting();
    }

    public void UpdateDayLightSettings (DayLightDetectSetting dayLightDetectSetting){
        DayLightDetectSetting nDayLightDetectSetting = daylightSettingForCurrentWatch();
        if (nDayLightDetectSetting != null){
            nDayLightDetectSetting.setExposureDuration(dayLightDetectSetting.getExposureDuration());
            nDayLightDetectSetting.setEndTime(dayLightDetectSetting.getEndTime());
            nDayLightDetectSetting.setStartTime(dayLightDetectSetting.getStartTime());
            nDayLightDetectSetting.setExposureLevel(dayLightDetectSetting.getExposureLevel());
            nDayLightDetectSetting.setDetectHighThreshold(dayLightDetectSetting.getDetectHighThreshold());
            nDayLightDetectSetting.setDetectLowThreshold(dayLightDetectSetting.getDetectLowThreshold());
            nDayLightDetectSetting.setDetectMediumThreshold(dayLightDetectSetting.getDetectMediumThreshold());
            nDayLightDetectSetting.setEnabled(dayLightDetectSetting.isEnabled());
            nDayLightDetectSetting.setInterval(dayLightDetectSetting.getInterval());
            nDayLightDetectSetting.setContext(mContext);
            nDayLightDetectSetting.update();
        }
        else {
            dayLightDetectSetting.setContext(mContext);
            dayLightDetectSetting.insert();
        }
    }

    private NightLightDetectSetting nightlightSettingForCurrentWatch() {
        List<NightLightDetectSetting> nightlightSettings = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchNightlightSetting = ?", String.valueOf(mWatch.getId()))
                .getResults(NightLightDetectSetting.class);

        if (nightlightSettings.size() > 0)
            return nightlightSettings.get(0);

        return new NightLightDetectSetting();
    }

    public void UpdateNightLightSettings (NightLightDetectSetting nightLightDetectSetting){
        NightLightDetectSetting nNightLightDetectSetting = nightlightSettingForCurrentWatch();
        if (nNightLightDetectSetting != null){
            nNightLightDetectSetting.setExposureDuration(nightLightDetectSetting.getExposureDuration());
            nNightLightDetectSetting.setEndTime(nightLightDetectSetting.getEndTime());
            nNightLightDetectSetting.setStartTime(nightLightDetectSetting.getStartTime());
            nNightLightDetectSetting.setExposureLevel(nightLightDetectSetting.getExposureLevel());
            nNightLightDetectSetting.setDetectHighThreshold(nightLightDetectSetting.getDetectHighThreshold());
            nNightLightDetectSetting.setDetectLowThreshold(nightLightDetectSetting.getDetectLowThreshold());
            nNightLightDetectSetting.setDetectMediumThreshold(nightLightDetectSetting.getDetectMediumThreshold());
            nNightLightDetectSetting.setEnabled(nightLightDetectSetting.isEnabled());
            nNightLightDetectSetting.setContext(mContext);
            nNightLightDetectSetting.update();
        }
        else{
            nightLightDetectSetting.setContext(mContext);
            nightLightDetectSetting.insert();
        }
    }

    private ActivityAlertSetting activityAlertForCurrentWatch() {
        List<ActivityAlertSetting> activityAlertSettings = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchActivityAlert = ?", String.valueOf(mWatch.getId()))
                .getResults(ActivityAlertSetting.class);

        if (activityAlertSettings.size() > 0)
            return activityAlertSettings.get(0);

        return null;
    }

    public void UpdateInactiveAlert (ActivityAlertSetting activityAlertSetting){
        ActivityAlertSetting nActivityAlertSetting = activityAlertForCurrentWatch();
        if (nActivityAlertSetting != null){
            nActivityAlertSetting.setStartTime(activityAlertSetting.getStartTime());
            nActivityAlertSetting.setEndTime(activityAlertSetting.getEndTime());
            nActivityAlertSetting.setStepsThreshold(activityAlertSetting.getStepsThreshold());
            nActivityAlertSetting.setTimeInterval(activityAlertSetting.getTimeInterval());
            nActivityAlertSetting.setEnabled(activityAlertSetting.isEnabled());
            nActivityAlertSetting.setContext(mContext);
            nActivityAlertSetting.update();
        }
        else {
            activityAlertSetting.setContext(mContext);
            activityAlertSetting.insert();
        }
    }

    private Notification notificationForCurrentWatch() {
        List<Notification> notifications = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchNotification = ?", String.valueOf(mWatch.getId()))
                .getResults(Notification.class);

        if (notifications.size() > 0)
            return notifications.get(0);

        return new Notification();
    }

    public void UpdateNotification (Notification notification){
        Notification nNotification = notificationForCurrentWatch();
        if (nNotification != null){
            nNotification.setEmailEnabled(notification.isEmailEnabled());
            nNotification.setIncomingCallEnabled(notification.isIncomingCallEnabled());
            nNotification.setMissedCallEnabled(notification.isMissedCallEnabled());
            nNotification.setSmsEnabled(notification.isSmsEnabled());
            nNotification.setVoiceMailEnabled(notification.isVoiceMailEnabled());
            nNotification.setScheduleEnabled(notification.isScheduleEnabled());
            nNotification.setHighPriorityEnabled(notification.isHighPriorityEnabled());
            nNotification.setInstantMessageEnabled(notification.isInstantMessageEnabled());
            nNotification.setContext(mContext);
            nNotification.update();
        }
        else {
            notification.setContext(mContext);
            notification.insert();
        }
    }

    private SleepSetting sleepForCurrentWatch(){
        List<SleepSetting> sleepSettings = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchSleepSetting = ?", String.valueOf(mWatch.getId()))
                .getResults(SleepSetting.class);
        if (sleepSettings.size() > 0)
        return sleepSettings.get(0);
        return  null;
    }

    public void UpdatedSleepSettings (SleepSetting sleepSetting){
        SleepSetting nsSleepSetting = sleepForCurrentWatch();
        if (nsSleepSetting != null){
            nsSleepSetting.setSleepGoalMinutes(sleepSetting.getSleepGoalMinutes());
            nsSleepSetting.setSleepDetectType(sleepSetting.getSleepDetectType());
            nsSleepSetting.setContext(mContext);
            nsSleepSetting.update();
        }
        else {
            sleepSetting.setContext(mContext);
            sleepSetting.insert();
        }
    }


//    private UserProfile userProfileForCurrentWatch() {
//        List<UserProfile> userProfiles = DataSource.getInstance(mContext)
//                .getReadOperation().query("watchUserProfile = ?", String.valueOf(mWatch.getId()))
//                .getResults(UserProfile.class);
//
//        if (userProfiles.size() > 0)
//            return userProfiles.get(0);
//
//        return null;
//    }

//    public List<StatisticalDataHeader> getStatisticalDataHeaders(JSONArray arrayDataHeaders) throws JSONException, ParseException {
//        List<StatisticalDataHeader> dataHeaders = new ArrayList<StatisticalDataHeader>();
//
//        for(int i=0;i<arrayDataHeaders.length();i++) {
//            JSONObject objectDataHeader = arrayDataHeaders.getJSONObject(i);
//            StatisticalDataHeader dataHeader = new StatisticalDataHeader();
//            dataHeader.setAllocationBlockIndex(objectDataHeader.getInt("allocation_block_index"));
//            dataHeader.setTotalSleep(objectDataHeader.getInt("total_sleep"));
//            dataHeader.setTotalSteps(objectDataHeader.getLong("total_steps"));
//            dataHeader.setTotalCalorie(objectDataHeader.getDouble("total_calories"));
//            dataHeader.setTotalDistance(objectDataHeader.getDouble("total_distance"));
//            dataHeader.setLightExposure(objectDataHeader.getInt("total_exposure_time"));
//
//            mDateFormat.applyPattern("yyyy-MM-dd");
//            mCalendar.setTime(mDateFormat.parse(objectDataHeader.getString("header_created_date")));
//
//            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
//            int month = mCalendar.get(Calendar.MONTH) + 1;
//            int year = mCalendar.get(Calendar.YEAR) - 1900;
//
//            mDateFormat.applyPattern("hh:mm:ss");
//            mCalendar.setTime(mDateFormat.parse(objectDataHeader.getString("start_time")));
//
//            int startHour = mCalendar.get(Calendar.HOUR_OF_DAY);
//            int startMinute = mCalendar.get(Calendar.MINUTE);
//            int startSecond = mCalendar.get(Calendar.SECOND);
//
//            mCalendar.setTime(mDateFormat.parse(objectDataHeader.getString("end_time")));
//
//            int endHour = mCalendar.get(Calendar.HOUR_OF_DAY);
//            int endMinute = mCalendar.get(Calendar.MINUTE);
//            int endSecond = mCalendar.get(Calendar.SECOND);
//
//            dataHeader.setDateStamp(mCalendar.getTime());
//            dataHeader.setDateStampDay(day);
//            dataHeader.setDateStampMonth(month);
//            dataHeader.setDateStampYear(year);
//            dataHeader.setTimeStartHour(startHour);
//            dataHeader.setTimeStartMinute(startMinute);
//            dataHeader.setTimeStartSecond(startSecond);
//            dataHeader.setTimeEndHour(endHour);
//            dataHeader.setTimeEndMinute(endMinute);
//            dataHeader.setTimeEndSecond(endSecond);
//
//            JSONArray arrayDataPoints = objectDataHeader.getJSONArray("data_point");
//            List<StatisticalDataPoint> dataPoints = new ArrayList<StatisticalDataPoint>();
//
//            for(int j=0;j<arrayDataPoints.length();j++) {
//                JSONObject objectDataPoint = arrayDataPoints.getJSONObject(j);
//                StatisticalDataPoint dataPoint = new StatisticalDataPoint();
//                dataPoint.setDataPointId(objectDataPoint.getLong("datapoint_id"));
//                dataPoint.setAverageHR(objectDataPoint.getInt("average_HR"));
//                dataPoint.setAxisDirection(objectDataPoint.getInt("axis_direction"));
//                dataPoint.setAxisMagnitude(objectDataPoint.getInt("axis_magnitude"));
//                dataPoint.setDominantAxis(objectDataPoint.getInt("dominant_axis"));
//                dataPoint.setSleepPoint02(objectDataPoint.getInt("sleep_point_02"));
//                dataPoint.setSleepPoint24(objectDataPoint.getInt("sleep_point_24"));
//                dataPoint.setSleepPoint46(objectDataPoint.getInt("sleep_point_46"));
//                dataPoint.setSleepPoint68(objectDataPoint.getInt("sleep_point_68"));
//                dataPoint.setSleepPoint810(objectDataPoint.getInt("sleep_point_810"));
//                dataPoint.setSteps(objectDataPoint.getInt("steps"));
//                dataPoint.setCalorie(objectDataPoint.getDouble("calorie"));
//                dataPoint.setDistance(objectDataPoint.getDouble("distance"));
//                dataPoint.setLux(objectDataPoint.getInt("lux"));
//                dataPoint.setWristOff02(objectDataPoint.getInt("wrist_detection"));
//                dataPoint.setStatisticalDataHeader(dataHeader);
//                dataPoints.add(dataPoint);
//            }
//
//            Collections.sort(dataPoints, new StatisticalDataPointComparator());
//
//            List<LightDataPoint> lightDataPoints = new ArrayList<LightDataPoint>();
//
//            if (objectDataHeader.has("light_datapoint") && !objectDataHeader.isNull("light_datapoint")) {
//                JSONArray arrayLightDataPoints = objectDataHeader.getJSONArray("light_datapoint");
//
//                for (int j=0;j<arrayLightDataPoints.length();j++) {
//                    JSONObject objectLightDataPoint = arrayLightDataPoints.getJSONObject(j);
//                    LightDataPoint lightDataPoint = new LightDataPoint();
//                    lightDataPoint.setDataPointId(objectLightDataPoint.getInt("light_datapoint_id"));
//                    lightDataPoint.setRedValue(objectLightDataPoint.getInt("red"));
//                    lightDataPoint.setGreenValue(objectLightDataPoint.getInt("blue"));
//                    lightDataPoint.setBlueValue(objectLightDataPoint.getInt("green"));
//                    lightDataPoint.setIntegrationTime(objectLightDataPoint.getInt("integration_time"));
//                    lightDataPoint.setSensorGain(objectLightDataPoint.getInt("sensor_gain"));
//                    lightDataPoint.setStatisticalDataHeader(dataHeader);
//                    lightDataPoints.add(lightDataPoint);
//                }
//
//                Collections.sort(lightDataPoints, new LightDataPointComparator());
//            }
//            for (int j=0;j<lightDataPoints.size();j++) {
//                if (j < dataPoints.size()) {
//                    int wristOff = dataPoints.get(j).getWristOff02();
//                    lightDataPoints.get(j).setWristOff02(wristOff);
//                }
//            }
//
//            dataHeader.setStatisticalDataPoints(dataPoints);
//            dataHeader.setLightDataPoints(lightDataPoints);
//
//            dataHeader.setWatch(mWatch);
//            dataHeaders.add(dataHeader);
//        }
//
//        return dataHeaders;
//    }
//
//    public List<WorkoutInfo> getWorkoutInfos(JSONArray arrayWorkoutInfos) throws JSONException, ParseException {
//        List<WorkoutInfo> workoutInfos = new ArrayList<WorkoutInfo>();
//
//        for(int i=0;i<arrayWorkoutInfos.length();i++) {
//            JSONObject objectWorkoutInfo = arrayWorkoutInfos.getJSONObject(i);
//            WorkoutInfo workoutInfo = new WorkoutInfo();
//
//            int duration = objectWorkoutInfo.getInt("workout_duration");
//
//            int hour = (duration / 100) / 3600;
//            int minute = ((duration / 100) / 60) - (hour * 60);
//            int seconds = (duration / 100) % 60;
//            int hundredths = duration % 100;
//
//            workoutInfo.setHour(hour);
//            workoutInfo.setMinute(minute);
//            workoutInfo.setSecond(seconds);
//            workoutInfo.setHundredths(hundredths);
//            workoutInfo.setCalories(objectWorkoutInfo.getDouble("calories"));
//            workoutInfo.setDistance(objectWorkoutInfo.getDouble("distance"));
//            workoutInfo.setSteps(objectWorkoutInfo.getLong("steps"));
//            workoutInfo.setFlags(objectWorkoutInfo.getInt("distance_unit_flag"));
//
//            mDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
//            mCalendar.setTime(mDateFormat.parse(objectWorkoutInfo.getString("start_date_time")));
//
//            workoutInfo.setDateStamp(mCalendar.getTime());
//            workoutInfo.setDateStampDay(mCalendar.get(Calendar.DAY_OF_MONTH));
//            workoutInfo.setDateStampMonth(mCalendar.get(Calendar.MONTH) + 1);
//            workoutInfo.setDateStampYear(mCalendar.get(Calendar.YEAR) - 1900);
//            workoutInfo.setTimeStampHour(mCalendar.get(Calendar.HOUR_OF_DAY));
//            workoutInfo.setTimeStampMinute(mCalendar.get(Calendar.MINUTE));
//            workoutInfo.setTimeStampSecond(mCalendar.get(Calendar.SECOND));
//
//            if (objectWorkoutInfo.has("workout_stop") && !objectWorkoutInfo.isNull("workout_stop")) {
//                JSONArray arrayWorkoutStops = objectWorkoutInfo.getJSONArray("workout_stop");
//                List<WorkoutStopInfo> workoutStopInfos = new ArrayList<WorkoutStopInfo>();
//
//                for (int j=0;j<arrayWorkoutStops.length();j++) {
//                    JSONObject objectWorkoutStop = arrayWorkoutStops.getJSONObject(j);
//                    WorkoutStopInfo workoutStopInfo = new WorkoutStopInfo();
//
//                    mDateFormat.applyPattern("HH:mm:ss");
//
//                    Calendar calendar = Calendar.getInstance();
//                    calendar.setTime(mDateFormat.parse(objectWorkoutStop.getString("workout_time")));
//
//                    workoutStopInfo.setWorkoutHours(calendar.get(Calendar.HOUR_OF_DAY));
//                    workoutStopInfo.setWorkoutMinutes(calendar.get(Calendar.MINUTE));
//                    workoutStopInfo.setWorkoutSeconds(calendar.get(Calendar.SECOND));
//
//                    calendar.setTime(mDateFormat.parse(objectWorkoutStop.getString("stop_time")));
//
//                    workoutStopInfo.setStopHours(calendar.get(Calendar.HOUR_OF_DAY));
//                    workoutStopInfo.setStopMinutes(calendar.get(Calendar.MINUTE));
//                    workoutStopInfo.setStopSeconds(calendar.get(Calendar.SECOND));
//
//                    if(!workoutStopInfos.contains(workoutStopInfo))
//                        workoutStopInfos.add(workoutStopInfo);
//
//                }
//
//                workoutInfo.setWorkoutStopInfos(workoutStopInfos);
//                workoutInfo.setWatch(mWatch);
//                workoutInfos.add(workoutInfo);
//            }
//        }
//
//        return workoutInfos;
//    }
//
//    public List<SleepDatabase> getSleepDatabases(JSONArray arraySleepDatabases, Watch watch) throws JSONException, ParseException {
//        List<SleepDatabase> sleepDatabases = new ArrayList<SleepDatabase>();
//
//        for(int i=0;i<arraySleepDatabases.length();i++) {
//            JSONObject objectSleepDatabase = arraySleepDatabases.getJSONObject(i);
//            SleepDatabase sleepDatabase = new SleepDatabase();
//
//            mDateFormat.applyPattern("HH:mm:ss");
//            mCalendar.setTime(mDateFormat.parse(objectSleepDatabase.getString("sleep_start_time")));
//
//            sleepDatabase.setHourSleepStart(mCalendar.get(Calendar.HOUR_OF_DAY));
//            sleepDatabase.setMinuteSleepStart(mCalendar.get(Calendar.MINUTE));
//
//            mCalendar.setTime(mDateFormat.parse(objectSleepDatabase.getString("sleep_end_time")));
//
//            sleepDatabase.setHourSleepEnd(mCalendar.get(Calendar.HOUR_OF_DAY));
//            sleepDatabase.setMinuteSleepEnd(mCalendar.get(Calendar.MINUTE));
//            sleepDatabase.setSleepOffset(objectSleepDatabase.getInt("sleep_offset"));
//            sleepDatabase.setDeepSleepCount(objectSleepDatabase.getInt("deep_sleep_count"));
//            sleepDatabase.setLightSleepCount(objectSleepDatabase.getInt("light_sleep_count"));
//            sleepDatabase.setLapses(objectSleepDatabase.getInt("lapses"));
//            sleepDatabase.setSleepDuration(objectSleepDatabase.getInt("sleep_duration"));
//            sleepDatabase.setExtraInfo(objectSleepDatabase.getInt("extra_info"));
//
//            mDateFormat.applyPattern("yyyy-MM-dd");
//            mCalendar.setTime(mDateFormat.parse(objectSleepDatabase.getString("sleep_created_date")));
//
//            sleepDatabase.setDateStampDay(mCalendar.get(Calendar.DAY_OF_MONTH));
//            sleepDatabase.setDateStampMonth(mCalendar.get(Calendar.MONTH) + 1);
//            sleepDatabase.setDateStampYear(mCalendar.get(Calendar.YEAR) - 1900);
//            sleepDatabase.setWatch(watch);
//
//            sleepDatabases.add(sleepDatabase);
//        }
//
//        return sleepDatabases;
//    }
//
//    public void updateUserProfileFromServer(JSONObject objectUserProfile) throws ParseException, JSONException {
//        UserProfile userProfile = userProfileForCurrentWatch();
//
//        mDateFormat.applyPattern("yyyy-MM-dd");
//        mCalendar.setTime(mDateFormat.parse(objectUserProfile.getString("birthday")));
//
//        userProfile.setBirthDay(mCalendar.get(Calendar.DAY_OF_MONTH));
//        userProfile.setBirthMonth(mCalendar.get(Calendar.MONTH) + 1);
//        userProfile.setBirthYear(mCalendar.get(Calendar.YEAR));
//        userProfile.setGender(objectUserProfile.getString("gender").equals("male")?GENDER_MALE:GENDER_FEMALE);
//        userProfile.setUnitSystem(objectUserProfile.getString("unit").equals("metric")?UNIT_METRIC:UNIT_IMPERIAL);
//        userProfile.setSensitivity(0);
//        userProfile.setHeight(objectUserProfile.getInt("height"));
//        userProfile.setWeight(objectUserProfile.getInt("weight"));
//        userProfile.setWatch(mWatch);
//
//        userProfile.update();
//    }
//
//    public List<Goal> getGoals(JSONArray arrayGoals) throws JSONException, ParseException {
//        List<Goal> goals = new ArrayList<Goal>();
//
//        for(int i=0;i<arrayGoals.length();i++) {
//            JSONObject objectGoal = arrayGoals.getJSONObject(i);
//            Goal goal = new Goal();
//            goal.setCalorieGoal(objectGoal.getLong("calories"));
//            goal.setStepGoal(objectGoal.getLong("steps"));
//            goal.setDistanceGoal(objectGoal.getDouble("distance"));
//            goal.setSleepGoal(objectGoal.getInt("sleep"));
//
//            mDateFormat.applyPattern("yyyy-MM-dd hh:mm:ss");
//            mCalendar.setTime(mDateFormat.parse(objectGoal.getString("goal_created_date_time")));
//
//            goal.setDate(mCalendar.getTime());
//            goal.setDateStampDay(mCalendar.get(Calendar.DAY_OF_MONTH));
//            goal.setDateStampMonth(mCalendar.get(Calendar.MONTH) + 1);
//            goal.setDateStampYear(mCalendar.get(Calendar.YEAR) - 1900);
//            goal.setWatch(mWatch);
//            goals.add(goal);
//        }
//
//        return goals;
//    }
//
//    public SleepSetting getSleepSetting(JSONObject objectSleepSetting, Watch watch) throws JSONException {
//        SleepSetting sleepSetting = new SleepSetting();
//
//        sleepSetting.setSleepGoalMinutes(objectSleepSetting.getInt("sleep_goal_lo"));
//        sleepSetting.setSleepDetectType(objectSleepSetting.getString("sleep_mode").equals("manual")?0:1);
//        sleepSetting.setWatch(watch);
//
//        return sleepSetting;
//    }
//
//    public void updateCalibrationDataFromServer(JSONObject objectDeviceSettings) throws JSONException {
//        CalibrationData calibrationData = calibrationDataForCurrentWatch();
//
//        calibrationData.setCalibrationType(SALCalibration.STEP_CALIBRATION);
//        calibrationData.setStepCalibration(objectDeviceSettings.getInt("calib_step"));
//        calibrationData.setDistanceCalibrationWalk(objectDeviceSettings.getInt("calib_walk"));
//        calibrationData.setDistanceCalibrationRun(objectDeviceSettings.getInt("calib_run"));
//        calibrationData.setAutoEL(objectDeviceSettings.getInt("auto_EL"));
//        calibrationData.setWatch(mWatch);
//
//        calibrationData.update();
//    }
//
//    public void updateTimeDateFromServer(JSONObject objectDeviceSettings) throws JSONException {
//        TimeDate timeDate = timeDateForCurrentWatch();
//
//        int hourFormat = objectDeviceSettings.getInt("hour_format");
//        String dateFormat = objectDeviceSettings.getString("date_format");
//
//        if(hourFormat == 12) {
//            timeDate.setHourFormat(TIME_FORMAT_12_HR);
//        } else {
//            timeDate.setHourFormat(TIME_FORMAT_24_HR);
//        }
//
//        if(dateFormat.equals("DDMM")) {
//            timeDate.setDateFormat(DATE_FORMAT_DDMM);
//        } else {
//            timeDate.setDateFormat(DATE_FORMAT_MMDD);
//        }
//
//        timeDate.setWatch(mWatch);
//
//        timeDate.update();
//    }
//
//    public void updateWakeupSettingFromServer(JSONObject objectWakeupSetting) throws JSONException, ParseException {
//        WakeupSetting wakeupSetting = wakeupSettingForCurrentWatch();
//
//        mDateFormat.applyPattern("HH:mm:ss");
//
//        wakeupSetting.setSnoozeTime(objectWakeupSetting.getInt("snooze_min"));
//        wakeupSetting.setSnoozeEnabled(objectWakeupSetting.getInt("snooze_mode") == 1);
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(mDateFormat.parse(objectWakeupSetting.getString("wakeup_time")));
//
//        wakeupSetting.setTime(calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));
//
//        wakeupSetting.setEnabled(objectWakeupSetting.getInt("wakeup_mode") == 1);
//        wakeupSetting.setWatch(mWatch);
//
//        wakeupSetting.update();
//    }
//
//    public void updateDayLightDetectSettingFromServer(JSONObject objectDayLightSetting) throws JSONException {
//        DayLightDetectSetting dayLightDetectSetting = daylightSettingForCurrentWatch();
//
//        dayLightDetectSetting.setExposureDuration(objectDayLightSetting.getInt("duration"));
//
//        int endTime = objectDayLightSetting.getInt("end_hour") * 60 + objectDayLightSetting.getInt("end_min");
//        int startTime = objectDayLightSetting.getInt("start_hour") * 60 + objectDayLightSetting.getInt("start_min");
//
//        dayLightDetectSetting.setEndTime(endTime);
//        dayLightDetectSetting.setStartTime(startTime);
//        dayLightDetectSetting.setExposureLevel(objectDayLightSetting.getInt("level"));
//        dayLightDetectSetting.setDetectHighThreshold(objectDayLightSetting.getInt("level_high"));
//        dayLightDetectSetting.setDetectLowThreshold(objectDayLightSetting.getInt("level_low"));
//        dayLightDetectSetting.setDetectMediumThreshold(objectDayLightSetting.getInt("level_mid"));
//        dayLightDetectSetting.setEnabled(objectDayLightSetting.getInt("status") == 1);
//        dayLightDetectSetting.setInterval(objectDayLightSetting.getInt("alert_interval"));
//        dayLightDetectSetting.setWatch(mWatch);
//
//      dayLightDetectSetting.update();
//    }
//
//    public void updateNightLightDetectSettingFromServer(JSONObject objectNightLightDetectSetting) throws JSONException {
//        NightLightDetectSetting nightLightDetectSetting = nightlightSettingForCurrentWatch();
//
//        nightLightDetectSetting.setExposureDuration(objectNightLightDetectSetting.getInt("duration"));
//
//        int endTime = objectNightLightDetectSetting.getInt("end_hour") * 60 + objectNightLightDetectSetting.getInt("end_min");
//        int startTime = objectNightLightDetectSetting.getInt("start_hour") * 60 + objectNightLightDetectSetting.getInt("start_min");
//
//        nightLightDetectSetting.setEndTime(endTime);
//        nightLightDetectSetting.setStartTime(startTime);
//        nightLightDetectSetting.setExposureLevel(objectNightLightDetectSetting.getInt("level"));
//        nightLightDetectSetting.setDetectHighThreshold(objectNightLightDetectSetting.getInt("level_high"));
//        nightLightDetectSetting.setDetectLowThreshold(objectNightLightDetectSetting.getInt("level_low"));
//        nightLightDetectSetting.setDetectMediumThreshold(objectNightLightDetectSetting.getInt("level_mid"));
//        nightLightDetectSetting.setEnabled(objectNightLightDetectSetting.getInt("status") == 1);
//        nightLightDetectSetting.setWatch(mWatch);
//
//        nightLightDetectSetting.update();
//    }
//
//    public void updateActivityAlertSettingFromServer(JSONObject objectActivityAlertSetting) throws JSONException {
//        ActivityAlertSetting activityAlertSetting = new ActivityAlertSetting();
//
//        int endTime = objectActivityAlertSetting.getInt("end_hour") * 60 + objectActivityAlertSetting.getInt("end_min");
//        int startTime = objectActivityAlertSetting.getInt("start_hour") * 60 + objectActivityAlertSetting.getInt("start_min");
//
//        activityAlertSetting.setStartTime(startTime);
//        activityAlertSetting.setEndTime(endTime);
//        activityAlertSetting.setStepsThreshold(objectActivityAlertSetting.getInt("steps_threshold"));
//        activityAlertSetting.setTimeInterval(objectActivityAlertSetting.getInt("time_duration"));
//        activityAlertSetting.setEnabled(objectActivityAlertSetting.getInt("status") == 1);
//        activityAlertSetting.setWatch(mWatch);
//
//        activityAlertSetting.update();
//    }
//
//    private class StatisticalDataPointComparator implements Comparator<StatisticalDataPoint> {
//        @Override
//        public int compare(StatisticalDataPoint lhs, StatisticalDataPoint rhs) {
//
////            if    (lhs.getDataPointId() > rhs.getDataPointId())
////                return 1;
////            else if    (rhs.getDataPointId() > lhs.getDataPointId())
////                return -1;
////            return 0;
////        }
////    }
//
//    private class LightDataPointComparator implements Comparator<LightDataPoint> {
//        @Override
//        public int compare(LightDataPoint lhs, LightDataPoint rhs) {
//            if    (lhs.getDataPointId() > rhs.getDataPointId())
//                return 1;
//            else if    (rhs.getDataPointId() > lhs.getDataPointId())
//                return -1;
//            return 0;
//        }
//    }

    /*
    public void updateStatisticalDataHeaders(List<StatisticalDataHeader>nStatisticalDataHeaders) {


        mStatisticalDataHeaders.clear();
        mStatisticalDataPoints.clear();
        mLightDataPoints.clear();
        mHeaderIndexes.clear();

        int index = 0;

        for (StatisticalDataHeader statisticalDataHeader : nStatisticalDataHeaders) {
            StatisticalDataHeader dataHeader = null;

            if (mLifeTrakApplication != null && mLifeTrakApplication.getSelectedWatch() != null &&
                    StatisticalDataHeader.isExists(mContext, mLifeTrakApplication.getSelectedWatch().getId(), statisticalDataHeader.getDateStampDay(), statisticalDataHeader.getDateStampMonth(), statisticalDataHeader.getDateStampYear())) {
               dataHeader = StatisticalDataHeader.getExistingDataHeader();

                String query = "select count(_id) from StatisticalDataPoint where dataHeaderAndPoint = ?";

                Cursor cursor = DataSource.getInstance(mContext)
                        .getReadOperation()
                        .rawQuery(query, String.valueOf(dataHeader.getId()));

                if (cursor.moveToFirst()) {
                    int dataPointCount = cursor.getInt(0);

                    if (dataPointCount < 144) {
                        mStatisticalDataHeaders.add(statisticalDataHeader);
                        mHeaderIndexes.add(index);
                    }
                }
            } else {
                mStatisticalDataHeaders.add(statisticalDataHeader);
                mHeaderIndexes.add(index);
            }

            index++;
        }


    }

    public void updateStatisticalDataPoints(List<SALStatisticalDataPoint> salStatisticalDataPoints) {


        mDataHeaderIndexForDataPoint++;

        List<StatisticalDataPoint> dataPoints = StatisticalDataPoint.buildStatisticalDataPoint(mContext, salStatisticalDataPoints);
        mStatisticalDataPoints.add(dataPoints);

        if (mDataHeaderIndexForDataPoint < mStatisticalDataHeaders.size()) {


            StatisticalDataHeader dataHeader = mStatisticalDataHeaders.get(mDataHeaderIndexForDataPoint);
            for (SALStatisticalDataPoint salDataPoint : salStatisticalDataPoints) {
                Log.i(TAG, String.format("data points data: %s distance:%f calories:%f steps:%d", dataHeader.getDateStamp().toString(),salDataPoint.distance,salDataPoint.calorie,salDataPoint.steps));
            }

        } else {


        }
    }

    public void updateLightDataPoints(List<SALLightDataPoint> salLightDataPoints) {


        List<StatisticalDataPoint> dataPoints = mStatisticalDataPoints.get(mDataHeaderIndexForLightPoint);

        mDataHeaderIndexForLightPoint++;

        List<LightDataPoint> lightDataPoints = LightDataPoint.buildLightDataPoint(mContext, salLightDataPoints);

        if (dataPoints.size() > 0) {
            for (int i=0;i<lightDataPoints.size();i++) {
                if (i < dataPoints.size()) {
                    StatisticalDataPoint dataPoint = dataPoints.get(i);
                    lightDataPoints.get(i).setWristOff02(dataPoint.getWristOff02());
                    lightDataPoints.get(i).setWristOff24(dataPoint.getWristOff24());
                    lightDataPoints.get(i).setWristOff46(dataPoint.getWristOff24());
                    lightDataPoints.get(i).setWristOff68(dataPoint.getWristOff68());
                    lightDataPoints.get(i).setWristOff810(dataPoint.getWristOff810());
                }
            }
        }

        mLightDataPoints.add(lightDataPoints);

        if (mDataHeaderIndexForLightPoint < mStatisticalDataHeaders.size()) {

        }
    }

    public void updateWorkoutDatabase(List<SALWorkoutInfo> salWorkoutInfos) {
        Log.i(TAG, "onGetWorkoutDatabase on LifeTrakSyncR450");

        List<WorkoutInfo> workoutInfos = WorkoutInfo.buildWorkoutInfo(mContext, salWorkoutInfos);
        mWorkoutInfos.clear();
        mWorkoutInfos.addAll(workoutInfos);



        if (mWorkoutInfos.size() > 0) {
            mWorkoutIndex = 0;
            mWorkoutStopInfos.clear();


        }
    }

    public void updateWorkoutStopDatabase(List<SALWorkoutStopInfo> salWorkoutStopInfos) {
        mWorkoutIndex++;

        List<WorkoutStopInfo> workoutStopInfos = WorkoutStopInfo.buildWorkoutStopInfo(mContext, salWorkoutStopInfos);
        mWorkoutStopInfos.add(workoutStopInfos);

        if (mWorkoutIndex < mWorkoutInfos.size()) {
            int workoutId = mWorkoutInfos.get(mWorkoutIndex).getWorkoutId();
        }

    }

    public void updateSleepDatabase(List<SALSleepDatabase> salSleepDatabases) {

        List<SleepDatabase> sleepDatabases = SleepDatabase.buildSleepDatabase(mContext, salSleepDatabases);
        mSleepDatabases.clear();
        mSleepDatabases.addAll(sleepDatabases);

    }

    public void updateSleepSetting(SALSleepSetting salSleepSetting) {
        mSleepSetting = SleepSetting.buildSleepSetting(mContext, salSleepSetting);
    }

    public void updateStepGoal(long stepGoal) {
        mStepGoal = stepGoal;
    }

    public void updateDistanceGoal(double distanceGoal) {
        mDistanceGoal = distanceGoal;

    }

    public void onGetCalorieGoal(long calorieGoal) {
        mCalorieGoal = calorieGoal;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        mGoals.clear();

        if (mStatisticalDataHeaders.size() == 0) {
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR) - 1900;

            Goal goal = new Goal(mContext);
            goal.setStepGoal(mStepGoal);
            goal.setDistanceGoal(mDistanceGoal);
            goal.setCalorieGoal(mCalorieGoal);
            goal.setSleepGoal(mSleepSetting.getSleepGoalMinutes());
            goal.setDate(calendar.getTime());
            goal.setDateStampDay(day);
            goal.setDateStampMonth(month);
            goal.setDateStampYear(year);

            mGoals.add(goal);
        } else {
            for (StatisticalDataHeader dataHeader : mStatisticalDataHeaders) {
                calendar.setTime(dataHeader.getDateStamp());

                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH) + 1;
                int year = calendar.get(Calendar.YEAR) - 1900;

                if (mLifeTrakApplication.getSelectedWatch() != null) {
                    List<Goal> goals = DataSource.getInstance(mContext)
                            .getReadOperation()
                            .query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
                                    String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()), String.valueOf(day), String.valueOf(month), String.valueOf(year))
                            .getResults(Goal.class);

                    if (goals.size() > 0)
                        continue;
                }


                Goal goal = new Goal(mContext);
                goal.setStepGoal(mStepGoal);
                goal.setDistanceGoal(mDistanceGoal);
                goal.setCalorieGoal(mCalorieGoal);
                goal.setSleepGoal(mSleepSetting.getSleepGoalMinutes());
                goal.setDate(calendar.getTime());
                goal.setDateStampDay(day);
                goal.setDateStampMonth(month);
                goal.setDateStampYear(year);

                mGoals.add(goal);
            }
        }

        mCalibrationIndex = 0;
    }

    public void updateCalibrationData(SALCalibration salCalibrationData) {
        Log.i(TAG, "onGetCalibrationData on LifeTrakSyncR450");

        if (mCalibrationData == null)
            mCalibrationData = new CalibrationData(mContext);

        mCalibrationIndex++;

        if (mCalibrationIndex < 4) {
            int type = SALCalibration.AUTO_EL_SETTING;

            switch(salCalibrationData.getCalibrationType()) {
                case SALCalibration.AUTO_EL_SETTING:
                    mCalibrationData.setAutoEL(salCalibrationData.getCalibrationValue());
                    type = SALCalibration.STEP_CALIBRATION;
                    break;
                case SALCalibration.STEP_CALIBRATION:
                    mCalibrationData.setStepCalibration(salCalibrationData.getCalibrationValue());
                    type = SALCalibration.WALK_DISTANCE_CALIBRATION;
                    break;
                case SALCalibration.WALK_DISTANCE_CALIBRATION:
                    mCalibrationData.setDistanceCalibrationRun(salCalibrationData.getCalibrationValue());
                    type = SALCalibration.CALORIE_CALIBRATION;
                    break;
                case SALCalibration.CALORIE_CALIBRATION:
                    mCalibrationData.setCaloriesCalibration(salCalibrationData.getCalibrationValue());
                    break;
            }


        } else {
            mWakeupIndex = 0;
        }
    }

    public void updateWakeupSetting(SALWakeupSetting salWakeupSetting) {


        if (mWakeupSetting == null)
            mWakeupSetting = new WakeupSetting(mContext);

        mWakeupIndex++;

        if (mWakeupIndex < 6) {
            int type = SALWakeupSetting.WAKEUP_ALERT_STATUS;

            switch (salWakeupSetting.getWakeupSettingType()) {
                case SALWakeupSetting.WAKEUP_ALERT_STATUS:
                    mWakeupSetting.setEnabled(salWakeupSetting.getWakeupSetting() == SALWakeupSetting.ENABLE);
                    type = SALWakeupSetting.WAKEUP_TIME;
                    break;
                case SALWakeupSetting.WAKEUP_TIME:
                    mWakeupSetting.setTime(salWakeupSetting.getWakeupSetting());
                    mWakeupSetting.setWakeupTimeHour(salWakeupSetting.getWakeupHour());
                    mWakeupSetting.setWakeupTimeMinute(salWakeupSetting.getWakeupMinute());
                    type = SALWakeupSetting.WAKEUP_WINDOW;
                    break;
                case SALWakeupSetting.WAKEUP_WINDOW:
                    mWakeupSetting.setWindow(salWakeupSetting.getWakeupSetting());
                    type = SALWakeupSetting.SNOOZE_ALERT_STATUS;
                    break;
                case SALWakeupSetting.SNOOZE_ALERT_STATUS:
                    mWakeupSetting.setSnoozeEnabled(salWakeupSetting.getWakeupSetting() == SALWakeupSetting.ENABLE);
                    type = SALWakeupSetting.SNOOZE_TIME;
                    break;
                case SALWakeupSetting.SNOOZE_TIME:
                    mWakeupSetting.setSnoozeTime(salWakeupSetting.getWakeupSetting());
                    break;
            }


        }
    }

    public void updateActivityAlertSetting(SALActivityAlertSetting salActivityAlertSetting) {

        if (mActivityAlertSetting == null)
            mActivityAlertSetting = new ActivityAlertSetting(mContext);

        mActivityAlertIndex++;

        if (mActivityAlertIndex < 6) {
            int type = SALActivityAlertSetting.ALERT_STATUS;

            switch (salActivityAlertSetting.getActivityAlertSettingType()) {
                case SALActivityAlertSetting.ALERT_STATUS:
                    mActivityAlertSetting.setEnabled(salActivityAlertSetting.getActivityAlertStatus() == SALActivityAlertSetting.ENABLE);
                    type = SALActivityAlertSetting.ALERT_TIME_INTERVAL;
                    break;
                case SALActivityAlertSetting.ALERT_TIME_INTERVAL:
                    mActivityAlertSetting.setTimeInterval(salActivityAlertSetting.getTimeInterval());
                    type = SALActivityAlertSetting.ALERT_STEPS_THRESHOLD;
                    break;
                case SALActivityAlertSetting.ALERT_STEPS_THRESHOLD:
                    mActivityAlertSetting.setStepsThreshold(salActivityAlertSetting.getStepsThreshold());
                    type = SALActivityAlertSetting.ALERT_START_TIME;
                    break;
                case SALActivityAlertSetting.ALERT_START_TIME:
                    int startTime = salActivityAlertSetting.getActivityAlertSetting();
                    int startHour = (startTime >> 8) & 0xFF;
                    int startMinute = startTime & 0xFF;
                    mActivityAlertSetting.setStartTime((startHour * 60) + startMinute);
                    type = SALActivityAlertSetting.ALERT_END_TIME;
                    break;
                case SALActivityAlertSetting.ALERT_END_TIME:
                    int endTime = salActivityAlertSetting.getActivityAlertSetting();
                    int endHour = (endTime >> 8) & 0xFF;
                    int endMinute = endTime & 0xFF;
                    mActivityAlertSetting.setEndTime((endHour * 60) + endMinute);
                    break;
            }

        } else {

            mDayLightDetectIndex = 0;

        }
    }

    public void updateDayLightDetectSetting(SALDayLightDetectSetting salDayLightDetectSetting) {
        if (mDayLightDetectSetting == null)
            mDayLightDetectSetting = new DayLightDetectSetting(mContext);

        mDayLightDetectIndex++;

        if (mDayLightDetectIndex < 10) {
            int type = SALDayLightDetectSetting.DETECT_STATUS;

            switch (salDayLightDetectSetting.getLightDetectSettingType()) {
                case SALDayLightDetectSetting.DETECT_STATUS:
                    mDayLightDetectSetting.setEnabled(salDayLightDetectSetting.getLightDetectStatus() == SALDayLightDetectSetting.ENABLE);
                    type = SALDayLightDetectSetting.DETECT_EXPOSURE_LEVEL;
                    break;
                case SALDayLightDetectSetting.DETECT_EXPOSURE_LEVEL:
                    mDayLightDetectSetting.setExposureLevel(salDayLightDetectSetting.getExposureLevel());
                    type = SALDayLightDetectSetting.DETECT_EXPOSURE_DURATION;
                    break;
                case SALDayLightDetectSetting.DETECT_EXPOSURE_DURATION:
                    mDayLightDetectSetting.setExposureDuration(salDayLightDetectSetting.getExposureDuration());
                    type = SALDayLightDetectSetting.DETECT_START_TIME;
                    break;
                case SALDayLightDetectSetting.DETECT_START_TIME:
                    int startTime = salDayLightDetectSetting.getLightDetectSetting();
                    int startHour = (startTime >> 8) & 0xFF;
                    int startMinute = startTime & 0xFF;
                    mDayLightDetectSetting.setStartTime((startHour * 60) + startMinute);
                    type = SALDayLightDetectSetting.DETECT_END_TIME;
                    break;
                case SALDayLightDetectSetting.DETECT_END_TIME:
                    int endTime = salDayLightDetectSetting.getLightDetectSetting();
                    int endHour = (endTime >> 8) & 0xFF;
                    int endMinute = endTime & 0xFF;
                    mDayLightDetectSetting.setEndTime((endHour * 60) + endMinute);
                    type = SALDayLightDetectSetting.DETECT_LOW_THRESHOLD;
                    break;
                case SALDayLightDetectSetting.DETECT_LOW_THRESHOLD:
                    mDayLightDetectSetting.setDetectLowThreshold(salDayLightDetectSetting.getLowThresholdValue());
                    type = SALDayLightDetectSetting.DETECT_MED_THRESHOLD;
                    break;
                case SALDayLightDetectSetting.DETECT_MED_THRESHOLD:
                    mDayLightDetectSetting.setDetectMediumThreshold(salDayLightDetectSetting.getMediumThresholdValue());
                    type = SALDayLightDetectSetting.DETECT_HIGH_THRESHOLD;
                    break;
                case SALDayLightDetectSetting.DETECT_HIGH_THRESHOLD:
                    mDayLightDetectSetting.setDetectHighThreshold(salDayLightDetectSetting.getHighThresholdValue());
                    type = SALDayLightDetectSetting.DETECT_INTERVAL;
                    break;
                case SALDayLightDetectSetting.DETECT_INTERVAL:
                    mDayLightDetectSetting.setInterval(salDayLightDetectSetting.getInterval());
                    break;
            }


        } else {
            mNightLightDetectIndex = 0;

        }
    }

    public void onGetNightLightDetectSetting(SALNightLightDetectSetting salNightLightDetectSetting) {


        if (mNightLightDetectSetting == null)
            mNightLightDetectSetting = new NightLightDetectSetting(mContext);

        mNightLightDetectIndex++;

        if (mNightLightDetectIndex < 9) {
            int type = SALNightLightDetectSetting.DETECT_STATUS;

            switch (salNightLightDetectSetting.getLightDetectSettingType()) {
                case SALNightLightDetectSetting.DETECT_STATUS:
                    mNightLightDetectSetting.setEnabled(salNightLightDetectSetting.getLightDetectStatus() == SALNightLightDetectSetting.ENABLE);
                    type = SALNightLightDetectSetting.DETECT_EXPOSURE_LEVEL;
                    break;
                case SALNightLightDetectSetting.DETECT_EXPOSURE_LEVEL:
                    mNightLightDetectSetting.setExposureLevel(salNightLightDetectSetting.getExposureLevel());
                    type = SALNightLightDetectSetting.DETECT_EXPOSURE_DURATION;
                    break;
                case SALNightLightDetectSetting.DETECT_EXPOSURE_DURATION:
                    mNightLightDetectSetting.setExposureDuration(salNightLightDetectSetting.getExposureDuration());
                    type = SALNightLightDetectSetting.DETECT_START_TIME;
                    break;
                case SALNightLightDetectSetting.DETECT_START_TIME:
                    int startTime = salNightLightDetectSetting.getLightDetectSetting();
                    int startHour = (startTime >> 8) & 0xFF;
                    int startMinute = startTime & 0xFF;
                    mNightLightDetectSetting.setStartTime((startHour * 60) + startMinute);
                    type = SALNightLightDetectSetting.DETECT_END_TIME;
                    break;
                case SALNightLightDetectSetting.DETECT_END_TIME:
                    int endTime = salNightLightDetectSetting.getLightDetectSetting();
                    int endHour = (endTime >> 8) & 0xFF;
                    int endMinute = endTime & 0xFF;
                    mNightLightDetectSetting.setEndTime((endHour * 60) + endMinute);
                    type = SALNightLightDetectSetting.DETECT_LOW_THRESHOLD;
                    break;
                case SALNightLightDetectSetting.DETECT_LOW_THRESHOLD:
                    mNightLightDetectSetting.setDetectLowThreshold(salNightLightDetectSetting.getLowThresholdValue());
                    type = SALNightLightDetectSetting.DETECT_MED_THRESHOLD;
                    break;
                case SALNightLightDetectSetting.DETECT_MED_THRESHOLD:
                    mNightLightDetectSetting.setDetectMediumThreshold(salNightLightDetectSetting.getMediumThresholdValue());
                    type = SALNightLightDetectSetting.DETECT_HIGH_THRESHOLD;
                    break;
                case SALNightLightDetectSetting.DETECT_HIGH_THRESHOLD:
                    mNightLightDetectSetting.setDetectHighThreshold(salNightLightDetectSetting.getHighThresholdValue());
                    break;
            }

        }
    }

    public void onGetUserProfile(SALUserProfile salUserProfile) {
        Log.i(TAG, "onGetUserProfile on LifeTrakSyncR450");
        mUserProfile = UserProfile.buildUserProfile(mContext, salUserProfile);


    }

    public void onGetTimeDate(final SALTimeDate salTimeDate) {
        Log.i(TAG, "onGetTimeDate on LifeTrakSyncR450");

        mTimeDate = TimeDate.buildTimeDate(mContext, salTimeDate);
        //mSalutronService.enableANSServer();

        if (mLifeTrakApplication.getTimeDate() != null) {


            int dateFormat = mLifeTrakApplication.getTimeDate().getDateFormat();
            int hourFormat = mLifeTrakApplication.getTimeDate().getHourFormat();
            int displaySize = mLifeTrakApplication.getTimeDate().getDisplaySize();

            mTimeDate.setDateFormat(dateFormat);
            mTimeDate.setHourFormat(hourFormat);
            mTimeDate.setDisplaySize(displaySize);
            mLifeTrakApplication.setTimeDate(mTimeDate);
            return;
        }

                if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME)){
                    SALTimeDate mSALTimeDate = new SALTimeDate();
                    mSALTimeDate.setToNow();
                    if (mLifeTrakApplication.getTimeDate() != null){
                        int dateFormat = mLifeTrakApplication.getTimeDate().getDateFormat();
                        int hourFormat = mLifeTrakApplication.getTimeDate().getHourFormat();
                        int displaySize = mLifeTrakApplication.getTimeDate().getDisplaySize();

                        mSALTimeDate.setDateFormat(dateFormat);
                        mSALTimeDate.setTimeFormat(hourFormat);
                        mSALTimeDate.setTimeDisplaySize(displaySize);
                    }
                    else{
                        mSALTimeDate.setDateFormat(salTimeDate.getDateFormat());
                        mSALTimeDate.setTimeFormat(salTimeDate.getHourFormat());
                        mSALTimeDate.setTimeDisplaySize(salTimeDate.getTimeDisplaySize());
                    }


                }

            }


    private void insertStatisticalDataHeaderWithWatch(Watch watch, List<StatisticalDataHeader> dataHeaders) {
        int index = 0;

        for (StatisticalDataHeader dataHeader : dataHeaders) {
            dataHeader.setContext(mContext);
            dataHeader.setWatch(watch);

            if (dataHeader.getId() == 0) {
                dataHeader.setStatisticalDataPoints(null);
                dataHeader.setLightDataPoints(null);
                dataHeader.insert();
            } else {
                dataHeader.update();
            }

            String query = "select _id from StatisticalDataPoint where dataHeaderAndPoint = ?";

            Cursor cursor = DataSource.getInstance(mContext)
                    .getReadOperation()
                    .rawQuery(query, String.valueOf(dataHeader.getId()));

            int dataPointCount = 0;

            if (cursor.moveToFirst())
                dataPointCount = cursor.getCount() - 1;

            cursor.close();

            List<StatisticalDataPoint> dataPoints = mStatisticalDataPoints.get(index);

            List<StatisticalDataPoint> lastDataPoints = DataSource.getInstance(mContext)
                    .getReadOperation()
                    .query("dataHeaderAndPoint = ?", String.valueOf(dataHeader.getId()))
                    .orderBy("_id", SalutronLifeTrakUtility.SORT_DESC)
                    .limit(1)
                    .getResults(StatisticalDataPoint.class);

            for (int i=dataPointCount;i<dataPoints.size();i++) {
                StatisticalDataPoint dataPoint = dataPoints.get(i);
                dataPoint.setContext(mContext);
                dataPoint.setStatisticalDataHeader(dataHeader);

                if (i == dataPointCount && lastDataPoints.size() > 0) {
                    StatisticalDataPoint lastDataPoint = lastDataPoints.get(0);
                    lastDataPoint.setContext(mContext);
                    lastDataPoint.setStatisticalDataHeader(dataHeader);
                    lastDataPoint.setAverageHR(dataPoint.getAverageHR());
                    lastDataPoint.setDistance(dataPoint.getDistance());
                    lastDataPoint.setSteps(dataPoint.getSteps());
                    lastDataPoint.setCalorie(dataPoint.getCalorie());
                    lastDataPoint.setSleepPoint02(dataPoint.getSleepPoint02());
                    lastDataPoint.setSleepPoint24(dataPoint.getSleepPoint24());
                    lastDataPoint.setSleepPoint46(dataPoint.getSleepPoint46());
                    lastDataPoint.setSleepPoint68(dataPoint.getSleepPoint68());
                    lastDataPoint.setSleepPoint810(dataPoint.getSleepPoint810());
                    lastDataPoint.setDominantAxis(dataPoint.getDominantAxis());
                    lastDataPoint.setLux(dataPoint.getLux());
                    lastDataPoint.setAxisDirection(dataPoint.getAxisDirection());
                    lastDataPoint.setAxisMagnitude(dataPoint.getAxisMagnitude());
                    lastDataPoint.update();
                } else {
                    dataPoint.insert();
                }
            }

            query = "select _id from LightDataPoint where dataHeaderAndPoint = ?";

            cursor = DataSource.getInstance(mContext)
                    .getReadOperation()
                    .rawQuery(query, String.valueOf(dataHeader.getId()));

            int lightPointCount = 0;

            if (cursor.moveToFirst())
                lightPointCount = cursor.getCount() - 1;

            cursor.close();

            List<LightDataPoint> lightDataPoints = mLightDataPoints.get(index);
            List<LightDataPoint> lastLightDataPoints = DataSource.getInstance(mContext)
                    .getReadOperation()
                    .query("dataHeaderAndPoint = ?", String.valueOf(dataHeader.getId()))
                    .orderBy("_id", SalutronLifeTrakUtility.SORT_DESC)
                    .limit(1)
                    .getResults(LightDataPoint.class);

            for (int i=lightPointCount;i<lightDataPoints.size();i++) {
                LightDataPoint lightDataPoint = lightDataPoints.get(i);
                lightDataPoint.setContext(mContext);
                lightDataPoint.setStatisticalDataHeader(dataHeader);

                if (i == lightPointCount && lastLightDataPoints.size() > 0) {
                    LightDataPoint lastLightDataPoint = lastLightDataPoints.get(0);
                    lastLightDataPoint.setRedValue(lightDataPoint.getRedValue());
                    lastLightDataPoint.setGreenValue(lightDataPoint.getGreenValue());
                    lastLightDataPoint.setBlueValue(lightDataPoint.getBlueValue());
                    lastLightDataPoint.setIntegrationTime(lightDataPoint.getIntegrationTime());
                    lastLightDataPoint.setSensorGain(lightDataPoint.getSensorGain());
                    lastLightDataPoint.update();
                } else {
                    lightDataPoint.insert();
                }
            }

            index++;
        }
    }

    private void insertGoalsWithWatch(Watch watch, List<Goal> goals) {
        if (goals.size() == 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR) - 1900;

            Goal goal = new Goal(mContext);
            goal.setStepGoal(mStepGoal);
            goal.setDistanceGoal(mDistanceGoal);
            goal.setCalorieGoal(mCalorieGoal);
            goal.setSleepGoal(mSleepSetting.getSleepGoalMinutes());
            goal.setDate(calendar.getTime());
            goal.setDateStampDay(day);
            goal.setDateStampMonth(month);
            goal.setDateStampYear(year);
            goals.add(goal);
        }

        for (Goal goal : goals) {
            goal.setWatch(watch);
            goal.insert();
        }
    }

    private void insertSleepDatabaseWithWatch(Watch watch, List<SleepDatabase> sleepDatabases) {

        for (SleepDatabase sleepDatabase : sleepDatabases) {
            sleepDatabase.setContext(mContext);
            sleepDatabase.setWatch(watch);

            if (sleepDatabase.isExists()) {
                sleepDatabase.update();
            } else {
                sleepDatabase.insert();
            }
        }
    }

    private void insertWorkoutDatabaseWithWatch(Watch watch, List<WorkoutInfo> workoutInfos) {

        for (WorkoutInfo workoutInfo : workoutInfos) {
            workoutInfo.setContext(mContext);
            workoutInfo.setWatch(watch);

            if (workoutInfo.isExists()) {
                workoutInfo.update();
            } else {
                workoutInfo.insert();
            }

            String query = "select _id from WorkoutStopInfo where infoAndStop = ?";

            Cursor cursor = DataSource.getInstance(mContext)
                    .getReadOperation()
                    .rawQuery(query, String.valueOf(workoutInfo.getId()));

            int workoutStopCount = 0;

            if (cursor.moveToFirst())
                workoutStopCount = cursor.getCount();

            cursor.close();

            List<WorkoutStopInfo> workoutStopInfos = mWorkoutStopInfos.get(workoutInfos.indexOf(workoutInfo));

            if (workoutStopInfos.size() > 0) {
                if (workoutStopCount > 0) {
                    workoutStopCount = workoutStopCount - 1;
                }

                for (int i=workoutStopCount;i<workoutStopInfos.size();i++) {
                    WorkoutStopInfo workoutStopInfo = workoutStopInfos.get(0);
                    workoutStopInfo.setWorkoutInfo(workoutInfo);
                    workoutStopInfo.insert();
                }
            }
        }
    }

    private void insertCalibrationDataWithWatch(Watch watch, CalibrationData calibrationData) {
        calibrationData.setWatch(watch);
        calibrationData.setContext(mContext);

        List<CalibrationData> calibrationDataFromDB = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchCalibrationData = ?", String.valueOf(watch.getId()))
                .getResults(CalibrationData.class);

        if (calibrationDataFromDB.size() == 0)
            calibrationData.insert();
    }

    private void insertSleepSettingWithWatch(Watch watch, SleepSetting sleepSetting) {
        sleepSetting.setWatch(watch);
        sleepSetting.setContext(mContext);

        List<SleepSetting> sleepSettingFromDB = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchSleepSetting = ?", String.valueOf(watch.getId()))
                .getResults(SleepSetting.class);

        if (sleepSettingFromDB.size() == 0)
            sleepSetting.insert();
    }

    private void insertTimeDateWithWatch(Watch watch, TimeDate timeDate) {
        timeDate.setWatch(watch);
        timeDate.setContext(mContext);

        List<TimeDate> timeDateFromDB = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchTimeDate = ?", String.valueOf(watch.getId()))
                .getResults(TimeDate.class);

        if (timeDateFromDB.size() == 0)
            timeDate.insert();
    }

    private void insertUserProfileWithWatch(Watch watch, UserProfile userProfile) {
        userProfile.setWatch(watch);
        userProfile.setContext(mContext);

        List<UserProfile> userProfileFromDB = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchUserProfile = ?", String.valueOf(watch.getId()))
                .getResults(UserProfile.class);

        if (userProfileFromDB.size() == 0)
            userProfile.insert();
    }

    private void insertNotificationWithWatch(Watch watch, Notification notification) {
        notification.setWatch(watch);
        notification.setContext(mContext);

        List<Notification> notificationFromDB = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchNotification = ?", String.valueOf(watch.getId()))
                .getResults(Notification.class);

        if (notificationFromDB.size() == 0)
            notification.insert();
    }

    private void insertWakeupSettingWithWatch(Watch watch, WakeupSetting wakeupSetting) {
        wakeupSetting.setWatch(watch);
        wakeupSetting.setContext(mContext);

        List<WakeupSetting> wakeupSettingFromDB = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchWakeupSetting = ?", String.valueOf(watch.getId()))
                .getResults(WakeupSetting.class);

        if (wakeupSettingFromDB.size() == 0)
            wakeupSetting.insert();
    }

    private void insertActivityAlertSettingWithWatch(Watch watch, ActivityAlertSetting activityAlertSetting) {
        activityAlertSetting.setWatch(watch);
        activityAlertSetting.setContext(mContext);

        List<ActivityAlertSetting> activityAlertSettingFromDB = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchActivityAlert = ?", String.valueOf(watch.getId()))
                .getResults(ActivityAlertSetting.class);

        if (activityAlertSettingFromDB.size() == 0)
            activityAlertSetting.insert();
    }

    private void insertDayLightDetectSettingWithWatch(Watch watch, DayLightDetectSetting dayLightDetectSetting) {
        dayLightDetectSetting.setWatch(watch);
        dayLightDetectSetting.setContext(mContext);

        List<DayLightDetectSetting> dayLightDetectSettingFromDB = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchDaylightSetting = ?", String.valueOf(watch.getId()))
                .getResults(DayLightDetectSetting.class);

        if (dayLightDetectSettingFromDB.size() == 0)
            dayLightDetectSetting.insert();
    }

    private void insertNightLightDetectSettingWithWatch(Watch watch, NightLightDetectSetting nightLightDetectSetting) {
        nightLightDetectSetting.setWatch(watch);
        nightLightDetectSetting.setContext(mContext);

        List<NightLightDetectSetting> nightLightDetectSettingFromDB = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchNightlightSetting = ?", String.valueOf(watch.getId()))
                .getResults(NightLightDetectSetting.class);

        if (nightLightDetectSettingFromDB.size() == 0)
            nightLightDetectSetting.insert();
    }
    */
}

