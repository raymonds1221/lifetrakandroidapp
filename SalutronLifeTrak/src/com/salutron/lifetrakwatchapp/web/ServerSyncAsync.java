package com.salutron.lifetrakwatchapp.web;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.androidquery.callback.AjaxStatus;
import com.salutron.blesdk.SALCalibration;
import com.salutron.blesdk.SALTimeDate;
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
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.WakeupSetting;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.model.WorkoutSettings;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.PreferenceWrapper;

import android.content.Context;

public class ServerSyncAsync extends BaseAsync<JSONObject> {
	private Context mContext;
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private final Calendar mCalendar = Calendar.getInstance();

    private PreferenceWrapper mPreferenceWrapper;

	public ServerSyncAsync(Context context) {
		super(context);
		mContext = context;
        mPreferenceWrapper = PreferenceWrapper.getInstance(context);
	}

	@Override
	public void onCallback(String url, JSONObject result, AjaxStatus status) {
		if(result != null ) {
			if(mListener != null)
				mListener.onAsyncSuccess(result);
		} else {
			LifeTrakLogger.info("server sync status: " + status.getMessage() + " " + status.getError() + " " + status.getCode());
			if(mListener != null)
				mListener.onAsyncFail(status.getCode(), status.getMessage());
			status.invalidate();
		}
	}
	
	public List<Watch> getAllWatches() {
		List<Watch> watches = DataSource.getInstance(mContext)
										.getReadOperation()
										.getResults(Watch.class);
		return watches;
	}
	
	public synchronized JSONObject getDevice(String macAddress) throws JSONException {
		mDateFormat.applyPattern("yyyy-MM-dd hh:mm:ss");
		
		if (macAddress == null)
			macAddress = "";
		
		List<Watch> watches = DataSource.getInstance(mContext)
										.getReadOperation()
										.query("macAddress	= ?", macAddress)
                                        .orderBy("_id", SORT_ASC)
										.getResults(Watch.class);
		
		if(watches.size() > 0) {
			Watch watch = watches.get(0);
			JSONObject objectWatch = new JSONObject();
			objectWatch.put("device_id", watch.getId());
			objectWatch.put("mac_address", watch.getMacAddress());
			objectWatch.put("model_number", watch.getModel());
			objectWatch.put("device_name", watch.getName());
			//objectWatch.put("last_date_synced", mDateFormat.format(watch.getLastSyncDate()));

			Calendar c= Calendar.getInstance();
			SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String strDate=sdf.format(c.getTime());
			objectWatch.put("last_date_synced", strDate);
			//objectWatch.put("last_date_synced", "2015-08-04 22:00:00");
			//LifeTrakLogger.info("device: " + objectWatch.toString());
			return objectWatch;

		}
		
		return null;
	}
	
	public synchronized JSONArray getAllDataHeaders(Watch watch) throws JSONException {
		mDateFormat.applyPattern("yyyy-MM-dd");
		
		List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(mContext)
															.getReadOperation()
															.query("watchDataHeader = ? and syncedToCloud = 0", String.valueOf(watch.getId()))
															.orderBy("dateStamp", SORT_DESC)
															.getResults(StatisticalDataHeader.class, false);
		
		JSONArray arrayResult = new JSONArray();
		
		for(StatisticalDataHeader dataHeader : dataHeaders) {
			JSONObject objectDataHeader = new JSONObject();
			objectDataHeader.put("max_HR", dataHeader.getMaximumBPM());
			objectDataHeader.put("min_HR", dataHeader.getMinimumBPM());
			objectDataHeader.put("allocation_block_index", dataHeader.getAllocationBlockIndex());
			
			if(watch.getModel() != WATCHMODEL_C300)
				objectDataHeader.put("total_sleep", dataHeader.getTotalSleep());
			else
				objectDataHeader.put("total_sleep", "0");
			objectDataHeader.put("total_steps", dataHeader.getTotalSteps());
			objectDataHeader.put("total_calories", dataHeader.getTotalCalorie());
			objectDataHeader.put("total_distance", dataHeader.getTotalDistance());
			objectDataHeader.put("total_exposure_time", dataHeader.getLightExposure());
			
			mCalendar.set(Calendar.DAY_OF_MONTH, dataHeader.getDateStampDay());
			mCalendar.set(Calendar.MONTH, dataHeader.getDateStampMonth() - 1);
			mCalendar.set(Calendar.YEAR, dataHeader.getDateStampYear() + 1900);
			
			objectDataHeader.put("header_created_date", mDateFormat.format(mCalendar.getTime()));
			objectDataHeader.put("start_time", String.format("%d:%d:%d", dataHeader.getTimeStartHour(), dataHeader.getTimeStartMinute(), dataHeader.getTimeStartSecond()));
			objectDataHeader.put("end_time", String.format("%d:%d:%d", dataHeader.getTimeEndHour(), dataHeader.getTimeEndMinute(), dataHeader.getTimeEndSecond()));
			objectDataHeader.put("platform", "android");
			objectDataHeader.put("total_exposure_time", dataHeader.getLightExposure());
			
			JSONArray arrayDataPoints = new JSONArray();
			
			List<StatisticalDataPoint> dataPoints = DataSource.getInstance(mContext)
																.getReadOperation()
																.query("dataHeaderAndPoint = ?", String.valueOf(dataHeader.getId()))
																.getResults(StatisticalDataPoint.class);
			int dataPointId = 0;
			
			for(StatisticalDataPoint dataPoint : dataPoints) {
				dataPointId++;
				JSONObject objectDataPoint = new JSONObject();
				//objectDataPoint.put("datapoint_id", dataPoint.getDataPointId());
				objectDataPoint.put("datapoint_id", dataPointId);
				objectDataPoint.put("average_HR", dataPoint.getAverageHR());
				objectDataPoint.put("axis_direction", dataPoint.getAxisDirection());
				objectDataPoint.put("axis_magnitude", dataPoint.getAxisMagnitude());
				objectDataPoint.put("dominant_axis", dataPoint.getDominantAxis());
				objectDataPoint.put("sleep_point_02", dataPoint.getSleepPoint02());
				objectDataPoint.put("sleep_point_24", dataPoint.getSleepPoint24());
				objectDataPoint.put("sleep_point_46", dataPoint.getSleepPoint46());
				objectDataPoint.put("sleep_point_68", dataPoint.getSleepPoint68());
				objectDataPoint.put("sleep_point_810", dataPoint.getSleepPoint810());
				objectDataPoint.put("steps", dataPoint.getSteps());
				objectDataPoint.put("calorie", dataPoint.getCalorie());
				objectDataPoint.put("distance", dataPoint.getDistance());
				objectDataPoint.put("lux", dataPoint.getLux());
				
				int wristOff = dataPoint.getWristOff02() + dataPoint.getWristOff24() + dataPoint.getWristOff46() + dataPoint.getWristOff68() + dataPoint.getWristOff810();
				
				if (wristOff > 0) {
					objectDataPoint.put("wrist_detection", 1);
				} else {
					objectDataPoint.put("wrist_detection", 0);
				}
				
				objectDataPoint.put("ble_status", dataPoint.getBleStatus());
				
				arrayDataPoints.put(objectDataPoint);
			}
			
			objectDataHeader.put("data_point", arrayDataPoints);
			
			List<LightDataPoint> lightDataPoints = DataSource.getInstance(mContext)
															.getReadOperation()
															.query("dataHeaderAndPoint = ?", String.valueOf(dataHeader.getId()))
															.getResults(LightDataPoint.class, true);
			
			JSONArray arrayLightDataPoints = new JSONArray();
										
			for (LightDataPoint lightDataPoint : lightDataPoints) {
				JSONObject objectDataPoint = new JSONObject();
				
				objectDataPoint.put("light_datapoint_id", lightDataPoints.indexOf(lightDataPoint));
				objectDataPoint.put("red", lightDataPoint.getRedValue());
				objectDataPoint.put("blue", lightDataPoint.getBlueValue());
				objectDataPoint.put("green", lightDataPoint.getGreenValue());
				objectDataPoint.put("integration_time", lightDataPoint.getIntegrationTime());
				objectDataPoint.put("sensor_gain", lightDataPoint.getSensorGain());
				objectDataPoint.put("red_light_coeff", lightDataPoint.getRedCoeff());
				objectDataPoint.put("green_light_coeff", lightDataPoint.getGreenCoeff());
				objectDataPoint.put("blue_light_coeff", lightDataPoint.getBlueCoeff());
				
				arrayLightDataPoints.put(objectDataPoint);
			}
			
			objectDataHeader.put("light_datapoint", arrayLightDataPoints);
			
			arrayResult.put(objectDataHeader);
		}
		//LifeTrakLogger.info("dataheader: " + arrayResult.toString());
		return arrayResult;
	}
	
	public synchronized JSONObject getDeviceSettings(long watchId, LifeTrakApplication app) throws JSONException {
		JSONObject objectDeviceSettings = new JSONObject();

        List<CalibrationData> calibrationDataList = DataSource.getInstance(mContext).getReadOperation().query("watchCalibrationData = ?", String.valueOf(app.getSelectedWatch().getId()))
                .getResults(CalibrationData.class);

        if (calibrationDataList.size() > 0) {

            CalibrationData calibrationData = calibrationDataList.get(0);
			String calibType = "";
			if (calibrationData.getCalibrationType() == SALCalibration.STEP_CALIBRATION){
				calibType = "step";
			}
			else if (calibrationData.getCalibrationType() == SALCalibration.WALK_DISTANCE_CALIBRATION){
				calibType = "walk";
			}
			else{
				calibType = "run";
			}

            objectDeviceSettings.put("type", calibType);
//			if (app.getSelectedWatch().getModel() == WATCHMODEL_R415 )
//           	    objectDeviceSettings.put("calib_step", calibrationData.getCaloriesCalibration());
//            else
            objectDeviceSettings.put("calib_step", calibrationData.getStepCalibration());
            objectDeviceSettings.put("calib_walk", calibrationData.getDistanceCalibrationWalk());
            objectDeviceSettings.put("calib_run", calibrationData.getDistanceCalibrationRun());
			if(app.getSelectedWatch().getModel() == WATCHMODEL_R415 )
        	    objectDeviceSettings.put("calib_calories", calibrationData.getCaloriesCalibration());

            objectDeviceSettings.put("auto_EL", calibrationData.getAutoEL());
        }
        else{
            objectDeviceSettings.put("type", "step");
            objectDeviceSettings.put("calib_step", 1);
            objectDeviceSettings.put("calib_walk", 1);
            objectDeviceSettings.put("calib_run", 1);
			if(app.getSelectedWatch().getModel() == WATCHMODEL_R415 )
           		 objectDeviceSettings.put("calib_calories", 1);
            objectDeviceSettings.put("auto_EL", 1);
        }
		


        List<Notification> notifications = DataSource.getInstance(mContext)
                .getReadOperation()
                .query("watchNotification = ?", String.valueOf(watchId))
                .getResults(Notification.class);

        if (notifications.size() > 0) {
            Notification mNotificationSettings = notifications.get(0);
            objectDeviceSettings.put("noti_simple_alert", (mPreferenceWrapper.getPreferenceBooleanValue(NOTIFICATION_ENABLED)) ? 1 :0);
            objectDeviceSettings.put("noti_email", (mNotificationSettings.isEmailEnabled()) ? 1 :0);
            objectDeviceSettings.put("noti_news", (mNotificationSettings.isNewsEnabled())? 1 :0);
            objectDeviceSettings.put("noti_incoming_call", (mNotificationSettings.isIncomingCallEnabled())? 1 :0);
            objectDeviceSettings.put("noti_missed_call", (mNotificationSettings.isMissedCallEnabled())? 1 :0);
            objectDeviceSettings.put("noti_sms", (mNotificationSettings.isSmsEnabled())? 1 :0);
            objectDeviceSettings.put("noti_voice_mail", (mNotificationSettings.isVoiceMailEnabled())? 1 :0);
            objectDeviceSettings.put("noti_schedules", (mNotificationSettings.isScheduleEnabled())? 1 :0);
            objectDeviceSettings.put("noti_high_prio", (mNotificationSettings.isHighPriorityEnabled())? 1 :0);
            objectDeviceSettings.put("noti_social", (mNotificationSettings.isInstantMessageEnabled())? 1 :0);
        }
        else {
            objectDeviceSettings.put("noti_simple_alert", 1);
            objectDeviceSettings.put("noti_email", 1);
            objectDeviceSettings.put("noti_news", 1);
            objectDeviceSettings.put("noti_incoming_call", 1);
            objectDeviceSettings.put("noti_missed_call", 1);
            objectDeviceSettings.put("noti_sms", 1);
            objectDeviceSettings.put("noti_voice_mail", 1);
            objectDeviceSettings.put("noti_schedules", 1);
            objectDeviceSettings.put("noti_high_prio", 1);
            objectDeviceSettings.put("noti_social", 1);
        }

         if (app.getTimeDate().getHourFormat() == SALTimeDate.FORMAT_12HOUR){
             objectDeviceSettings.put("hour_format", "12");
        }
        else{
             objectDeviceSettings.put("hour_format", "24");
         }
        if(app.getSelectedWatch().getModel() == WATCHMODEL_R415 ) {

            if (app.getTimeDate().getDateFormat() == DATE_FORMAT_DDMM) {
                objectDeviceSettings.put("date_format", "DDMM");
            } else if (app.getTimeDate().getDateFormat() == DATE_FORMAT_MMDD) {
                objectDeviceSettings.put("date_format", "MMDD");
            } else if (app.getTimeDate().getDateFormat() == DATE_FORMAT_MMMDD) {
                objectDeviceSettings.put("date_format", "MMMDD");
            } else {
                objectDeviceSettings.put("date_format", "DDMMM");
            }

        }
        else{
            if (app.getTimeDate().getDateFormat() == DATE_FORMAT_DDMM) {
                objectDeviceSettings.put("date_format", "DDMM");
            } else {
                objectDeviceSettings.put("date_format", "MMDD");
            }
        }

        if (app.getTimeDate().getDisplaySize() == DISPLAY_FORMAT_BIG_DIGIT) {
            objectDeviceSettings.put("watch_face", "simple");
        } else {
            objectDeviceSettings.put("watch_face", "full");
        }

		//if(app.getSelectedWatch().getModel() == WATCHMODEL_R420 ) {
			List<WorkoutSettings> workoutSettings =
					DataSource.getInstance(mContext)
							.getReadOperation()
							.query("watchDataHeader = ?", String.valueOf(app.getSelectedWatch().getId()))
							.getResults(WorkoutSettings.class);

			if (workoutSettings.size() > 0) {
				WorkoutSettings workoutSettings1 = workoutSettings.get(0);
				objectDeviceSettings.put("hr_log_rate", workoutSettings1.getHrLoggingRate());
				objectDeviceSettings.put("database_usage", workoutSettings1.getDatabaseUsage());
				objectDeviceSettings.put("database_usage_max", workoutSettings1.getDatabaseUsageMax());
				objectDeviceSettings.put("reconnect_timeout", workoutSettings1.getReconnectTime());
			} else {
				objectDeviceSettings.put("hr_log_rate", 1);
				objectDeviceSettings.put("reconnect_timeout", 5);
				objectDeviceSettings.put("database_usage", 0);
				objectDeviceSettings.put("database_usage_max", 0);
			}
		//}
		//LifeTrakLogger.info("device settings: " + objectDeviceSettings.toString());
		return objectDeviceSettings;
	}
	
	public synchronized JSONArray getAllWorkoutInfos(long watchId, boolean isFromIOS,Watch watch) throws JSONException {
		mDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
		
		List<WorkoutInfo> workoutInfos = DataSource.getInstance(mContext)
													.getReadOperation()
													.query("watchWorkoutInfo = ?", String.valueOf(watchId))
													.getResults(WorkoutInfo.class);
		
		JSONArray arrayWorkoutInfos = new JSONArray();
		
		for(WorkoutInfo workoutInfo : workoutInfos) {
			JSONObject objectWorkoutInfo = new JSONObject();
			objectWorkoutInfo.put("workout_id", workoutInfo.getId());
			
			int workoutHour = workoutInfo.getHour() * 60 * 60;
			int workoutMinutes = workoutInfo.getMinute() * 60;
			int workoutSeconds = workoutInfo.getSecond();
			
			float duration = ((workoutHour + workoutMinutes + workoutSeconds) * 100) + workoutInfo.getHundredths();
			if (isFromIOS  && watch.getModel() == WATCHMODEL_C410){
				duration = duration / 100;
			}

			objectWorkoutInfo.put("workout_duration", duration);
			
			mCalendar.set(Calendar.DAY_OF_MONTH, workoutInfo.getDateStampDay());
			mCalendar.set(Calendar.MONTH, workoutInfo.getDateStampMonth() - 1);
			mCalendar.set(Calendar.YEAR, workoutInfo.getDateStampYear() + 1900);
			mCalendar.set(Calendar.HOUR_OF_DAY, workoutInfo.getTimeStampHour());
			mCalendar.set(Calendar.MINUTE, workoutInfo.getTimeStampMinute());
			mCalendar.set(Calendar.SECOND, workoutInfo.getTimeStampSecond());
			
			objectWorkoutInfo.put("start_date_time", mDateFormat.format(mCalendar.getTime()));
			objectWorkoutInfo.put("steps", workoutInfo.getSteps());
			objectWorkoutInfo.put("calories", workoutInfo.getCalories());
			objectWorkoutInfo.put("distance", workoutInfo.getDistance());
			objectWorkoutInfo.put("distance_unit_flag", workoutInfo.getFlags());
			objectWorkoutInfo.put("platform", "android");
			
			List<WorkoutStopInfo> workoutStopInfos = DataSource.getInstance(mContext)
																.getReadOperation()
																.query("infoAndStop = ?", String.valueOf(workoutInfo.getId()))
																.getResults(WorkoutStopInfo.class);
			
			
			JSONArray workoutStops = new JSONArray();
			
			for (WorkoutStopInfo workoutStopInfo : workoutStopInfos) {
				JSONObject objectWorkoutStop = new JSONObject();
				
				String workoutTime = String.format("%02d:%02d:%02d", workoutStopInfo.getWorkoutHours(), 
											workoutStopInfo.getWorkoutMinutes(), workoutStopInfo.getWorkoutSeconds());
				String stopTime = String.format("%02d:%02d:%02d", workoutStopInfo.getWorkoutHours(), 
											workoutStopInfo.getWorkoutMinutes(), workoutStopInfo.getWorkoutSeconds());
				
				objectWorkoutStop.put("workout_time", workoutTime);
				objectWorkoutStop.put("stop_time", stopTime);
				objectWorkoutStop.put("index", workoutStopInfos.indexOf(workoutStopInfo));
				
				workoutStops.put(objectWorkoutStop);
			}
			
			objectWorkoutInfo.put("workout_stop", workoutStops);
			
			arrayWorkoutInfos.put(objectWorkoutInfo);
		}
		//LifeTrakLogger.info("workout: " + arrayWorkoutInfos.toString());
		return arrayWorkoutInfos;
	}

	public synchronized JSONArray getAllWorkoutHeaders(long watchId) throws JSONException {
		List<WorkoutHeader> workoutHeaders = DataSource.getInstance(mContext)
														.getReadOperation()
														.query("watchWorkoutHeader = ?", String.valueOf(watchId))
														.getResults(WorkoutHeader.class);
		JSONArray arrayWorkoutHeaders = new JSONArray();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		simpleDateFormat.applyPattern("MM-dd-yyyy hh:mm:ss");

		for (WorkoutHeader workoutHeader : workoutHeaders) {
			JSONObject objectWorkoutHeader = new JSONObject();

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, workoutHeader.getDateStampYear() + 1900);
			calendar.set(Calendar.MONTH, workoutHeader.getDateStampMonth() - 1);
			calendar.set(Calendar.DAY_OF_MONTH, workoutHeader.getDateStampDay());
			calendar.set(Calendar.HOUR_OF_DAY, workoutHeader.getTimeStampHour());
			calendar.set(Calendar.MINUTE, workoutHeader.getTimeStampMinute());
			calendar.set(Calendar.SECOND, workoutHeader.getTimeStampSecond());

			String startDateTime = simpleDateFormat.format(calendar.getTime());

			objectWorkoutHeader.put("auto_split_threshold", workoutHeader.getAutoSplitThreshold());
			objectWorkoutHeader.put("auto_split_type", workoutHeader.getAutoSplitType());
			objectWorkoutHeader.put("average_bpm", workoutHeader.getAverageBPM());
			objectWorkoutHeader.put("hour", workoutHeader.getHour());
			objectWorkoutHeader.put("hundredths", workoutHeader.getHundredths());
			objectWorkoutHeader.put("log_rate_hr", workoutHeader.getLogRateHR());
			objectWorkoutHeader.put("maximum_bpm", workoutHeader.getMaximumBPM());
			objectWorkoutHeader.put("minimum_bpm", workoutHeader.getMinimumBPM());
			objectWorkoutHeader.put("minute", workoutHeader.getMinute());
			objectWorkoutHeader.put("record_count_hr", workoutHeader.getCountHRRecord());
			objectWorkoutHeader.put("record_count_splits", workoutHeader.getCountSplitsRecord());
			objectWorkoutHeader.put("record_count_stops", workoutHeader.getCountStopsRecord());
			objectWorkoutHeader.put("record_count_total", workoutHeader.getCountTotalRecord());
			objectWorkoutHeader.put("second", workoutHeader.getSecond());
			objectWorkoutHeader.put("stamp_day", workoutHeader.getDateStampDay());
			objectWorkoutHeader.put("stamp_hour", workoutHeader.getTimeStampHour());
			objectWorkoutHeader.put("stamp_minute", workoutHeader.getTimeStampMinute());
			objectWorkoutHeader.put("stamp_month", workoutHeader.getDateStampMonth());
			objectWorkoutHeader.put("stamp_second", workoutHeader.getTimeStampSecond());
			objectWorkoutHeader.put("stamp_year", workoutHeader.getDateStampYear());
			objectWorkoutHeader.put("start_date_time", startDateTime);
			objectWorkoutHeader.put("status_flag", workoutHeader.getStatusFlags());
			objectWorkoutHeader.put("user_max_hr", workoutHeader.getUserMaxHR());
			objectWorkoutHeader.put("zone0_lower_hr", workoutHeader.getZone0LowerHR());
			objectWorkoutHeader.put("zone0_upper_hr", workoutHeader.getZone0UpperHR());
			objectWorkoutHeader.put("zone1_lower_hr", workoutHeader.getZone1LowerHR());
			objectWorkoutHeader.put("zone2_lower_hr", workoutHeader.getZone2LowerHR());
			objectWorkoutHeader.put("zone3_lower_hr", workoutHeader.getZone3LowerHR());
			objectWorkoutHeader.put("zone4_lower_hr", workoutHeader.getZone4LowerHR());
			objectWorkoutHeader.put("zone5_lower_hr", workoutHeader.getZone5LowerHR());
			objectWorkoutHeader.put("zone5_upper_hr", workoutHeader.getZone5UpperHR());
			objectWorkoutHeader.put("zone_train_type", workoutHeader.getZoneTrainType());
			objectWorkoutHeader.put("steps", workoutHeader.getSteps());
			objectWorkoutHeader.put("calories", workoutHeader.getCalories());
			objectWorkoutHeader.put("distance", workoutHeader.getDistance());

			List<WorkoutStopInfo> workoutStopInfos = DataSource.getInstance(mContext)
																.getReadOperation()
																.query("headerAndStop = ?", String.valueOf(workoutHeader.getId()))
																.getResults(WorkoutStopInfo.class);
			JSONArray arrayWorkoutStops = new JSONArray();

			for (WorkoutStopInfo workoutStopInfo : workoutStopInfos) {
				JSONObject objectWorkoutStop = new JSONObject();

				String workoutTime = String.format("%02d:%02d:%02d", workoutStopInfo.getWorkoutHours(),
						workoutStopInfo.getWorkoutMinutes(), workoutStopInfo.getWorkoutSeconds());
				String stopTime = String.format("%02d:%02d:%02d", workoutStopInfo.getWorkoutHours(),
						workoutStopInfo.getWorkoutMinutes(), workoutStopInfo.getWorkoutSeconds());

				objectWorkoutStop.put("workout_time", workoutTime);
				objectWorkoutStop.put("stop_time", stopTime);
				objectWorkoutStop.put("index", workoutStopInfos.indexOf(workoutStopInfo));

				arrayWorkoutStops.put(objectWorkoutStop);
			}

			objectWorkoutHeader.put("workout_stop", arrayWorkoutStops);

			String headerHeartRate = workoutHeader.getHeaderHeartRate();
			List<String> workoutHRs = Arrays.asList(headerHeartRate.substring(1, headerHeartRate.length() - 1).split(","));

			JSONArray arrayWorkoutHRs = new JSONArray();

			for (String workoutHR : workoutHRs) {
				JSONObject objectWorkoutHR = new JSONObject();
				objectWorkoutHR.put("hr_data", Integer.parseInt(workoutHR.trim()));
				objectWorkoutHR.put("index", workoutHRs.indexOf(workoutHR));

				arrayWorkoutHRs.put(objectWorkoutHR);
			}

			objectWorkoutHeader.put("workout_hr_data", arrayWorkoutHRs);

			arrayWorkoutHeaders.put(objectWorkoutHeader);
		}

		return arrayWorkoutHeaders;
	}
	
	public synchronized JSONArray getAllSleepDatabases(long watchId) throws JSONException {
		mDateFormat.applyPattern("yyyy-MM-dd");
		
		List<SleepDatabase> sleepDatabases = DataSource.getInstance(mContext)
														.getReadOperation()
														.query("watchSleepDatabase = ?", String.valueOf(watchId))
														.getResults(SleepDatabase.class);
		
		JSONArray arraySleepDatabases = new JSONArray();
		
		for(SleepDatabase sleepDatabase : sleepDatabases) {
			JSONObject objectSleepDatabase = new JSONObject();
			objectSleepDatabase.put("sleep_start_time", String.format("%d:%d:%d", sleepDatabase.getHourSleepStart(), sleepDatabase.getMinuteSleepStart(), 0));
			objectSleepDatabase.put("sleep_end_time", String.format("%d:%d:%d", sleepDatabase.getHourSleepEnd(), sleepDatabase.getMinuteSleepEnd(), 0));
			objectSleepDatabase.put("sleep_offset", sleepDatabase.getSleepOffset());
			objectSleepDatabase.put("deep_sleep_count", sleepDatabase.getDeepSleepCount());
			objectSleepDatabase.put("light_sleep_count", sleepDatabase.getLightSleepCount());
			objectSleepDatabase.put("lapses", sleepDatabase.getLapses());
			objectSleepDatabase.put("sleep_duration", sleepDatabase.getSleepDuration());
			objectSleepDatabase.put("extra_info", sleepDatabase.getExtraInfo());
			
			mCalendar.set(Calendar.DAY_OF_MONTH, sleepDatabase.getDateStampDay());
			mCalendar.set(Calendar.MONTH, sleepDatabase.getDateStampMonth() - 1);
			mCalendar.set(Calendar.YEAR, sleepDatabase.getDateStampYear() + 1900);
			
			objectSleepDatabase.put("sleep_created_date", mDateFormat.format(mCalendar.getTime()));
			objectSleepDatabase.put("platform", "android");
			
			arraySleepDatabases.put(objectSleepDatabase);
		}
		//LifeTrakLogger.info("sleep: " + arraySleepDatabases.toString());
		return arraySleepDatabases;
	}
	
	public synchronized JSONObject getUserProfile(long watchId, String birthday) throws JSONException {
		mDateFormat.applyPattern("MMM dd,yyyy");
		Date myDate = null;
		try {
			myDate = mDateFormat.parse(birthday);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		
		List<UserProfile> userProfiles = DataSource.getInstance(mContext)
													.getReadOperation()
													.query("watchUserProfile = ?", String.valueOf(watchId))
													.getResults(UserProfile.class);
		
		if(userProfiles.size() > 0) {
			UserProfile userProfile = userProfiles.get(0);
			JSONObject objectUserProfile = new JSONObject();

			if (myDate != null){
				mCalendar.setTime(myDate);
			}
			else{
				mCalendar.set(Calendar.DAY_OF_MONTH, userProfile.getBirthDay());
				mCalendar.set(Calendar.MONTH, userProfile.getBirthMonth() - 1);
				mCalendar.set(Calendar.YEAR, userProfile.getBirthYear());
			}

			mDateFormat.applyPattern("yyyy-MM-dd");

			//objectUserProfile.put("birthday", mDateFormat.format(mCalendar.getTime()));
			objectUserProfile.put("birthday", mDateFormat.format(mCalendar.getTime()));
			objectUserProfile.put("gender", (userProfile.getGender() == GENDER_MALE ? "male" : "female"));
			objectUserProfile.put("unit", (userProfile.getUnitSystem() == UNIT_IMPERIAL ? "imperial" : "metric"));
			objectUserProfile.put("sensitivity", "low");
			objectUserProfile.put("height", userProfile.getHeight());
			objectUserProfile.put("weight", userProfile.getWeight());

			//LifeTrakLogger.info("user profile: " + objectUserProfile.toString());
			return objectUserProfile;
		}
		
		return null;
	}

	public synchronized JSONObject getUserProfile(long watchId) throws JSONException {
		mDateFormat.applyPattern("yyyy-MM-dd");

		List<UserProfile> userProfiles = DataSource.getInstance(mContext)
				.getReadOperation()
				.query("watchUserProfile = ?", String.valueOf(watchId))
				.getResults(UserProfile.class);

		if(userProfiles.size() > 0) {
			UserProfile userProfile = userProfiles.get(0);
			JSONObject objectUserProfile = new JSONObject();

			mCalendar.set(Calendar.DAY_OF_MONTH, userProfile.getBirthDay());
			mCalendar.set(Calendar.MONTH, userProfile.getBirthMonth() - 1);
			mCalendar.set(Calendar.YEAR, userProfile.getBirthYear());

			objectUserProfile.put("birthday", mDateFormat.format(mCalendar.getTime()));
			objectUserProfile.put("gender", (userProfile.getGender() == GENDER_MALE ? "male" : "female"));
			objectUserProfile.put("unit", (userProfile.getUnitSystem() == UNIT_IMPERIAL ? "imperial" : "metric"));
			objectUserProfile.put("sensitivity", "low");
			objectUserProfile.put("height", userProfile.getHeight());
			objectUserProfile.put("weight", userProfile.getWeight());

			//LifeTrakLogger.info("user profile: " + objectUserProfile.toString());
			return objectUserProfile;
		}

		return null;
	}



	public synchronized JSONArray getAllGoals(long watchId) throws JSONException {
		mDateFormat.applyPattern("yyyy-MM-dd hh:mm:ss");
		
		List<Goal> goals = DataSource.getInstance(mContext)
										.getReadOperation()
										.query("watchGoal = ?", String.valueOf(watchId))
										.getResults(Goal.class);
		
		JSONArray arrayGoals = new JSONArray();
		
		for(Goal goal : goals) {
			JSONObject objectGoal = new JSONObject();
			objectGoal.put("calories", goal.getCalorieGoal());
			objectGoal.put("steps", goal.getStepGoal());
			objectGoal.put("distance", goal.getDistanceGoal());
			objectGoal.put("sleep", goal.getSleepGoal());
			int year = goal.getDateStampYear() + 1900;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date date = null;
			try {
				 date = format.parse(String.valueOf(year) + "-" + goal.getDateStampMonth() + "-" + goal.getDateStampDay());
				System.out.println(date);
			} catch (ParseException e) {
				date = goal.getDate();
				e.printStackTrace();
			}

			objectGoal.put("goal_created_date_time", mDateFormat.format(date));
			
			arrayGoals.put(objectGoal);
		}
		//LifeTrakLogger.info("goals: " + arrayGoals.toString());
		return arrayGoals;
	}
	
	public synchronized JSONObject getSleepSetting(long watchId) throws JSONException {
		List<SleepSetting> sleepSettings = DataSource.getInstance(mContext)
														.getReadOperation()
														.query("watchSleepSetting = ?", String.valueOf(watchId))
														.getResults(SleepSetting.class);
		if(sleepSettings.size() > 0) {
			SleepSetting sleepSetting = sleepSettings.get(0);
			JSONObject objectSleepSetting = new JSONObject();
			objectSleepSetting.put("sleep_goal_lo", sleepSetting.getSleepGoalMinutes());
			objectSleepSetting.put("sleep_goal_hi", sleepSetting.getSleepGoalMinutes());
			objectSleepSetting.put("sleep_mode", sleepSetting.getSleepDetectType() == 0 ? "manual" : "auto");
			//LifeTrakLogger.info("sleep settings : " + objectSleepSetting.toString());
			return objectSleepSetting;
		}
		return null;
	}
	
	public synchronized JSONObject getWakeupInfo(long watchId) throws JSONException {
		JSONObject objectWakeupInfo = new JSONObject();
		
		List<WakeupSetting> wakeupSettings = DataSource.getInstance(mContext)
														.getReadOperation()
														.query("watchWakeupSetting = ?", String.valueOf(watchId))
														.getResults(WakeupSetting.class);
		
		if (wakeupSettings.size() > 0) {
			WakeupSetting wakeupSetting = wakeupSettings.get(0);
			
			String wakeupTime = String.format("%02d:%02d:%02d", wakeupSetting.getWakeupTimeHour(), wakeupSetting.getWakeupTimeMinute(), 0);
			
			objectWakeupInfo.put("snooze_min", wakeupSetting.getSnoozeTime());
			objectWakeupInfo.put("snooze_mode", wakeupSetting.isSnoozeEnabled());
			objectWakeupInfo.put("wakeup_time", wakeupTime);
			objectWakeupInfo.put("wakeup_mode", wakeupSetting.isEnabled());
			objectWakeupInfo.put("wakeup_window", wakeupSetting.getSnoozeTime());
			objectWakeupInfo.put("wakeup_type", 2);
		} else {
			objectWakeupInfo.put("snooze_min", 1);
			objectWakeupInfo.put("snooze_mode", 1);
			objectWakeupInfo.put("wakeup_time", "05:10:00");
			objectWakeupInfo.put("wakeup_mode", 1);
			objectWakeupInfo.put("wakeup_window", 1);
			objectWakeupInfo.put("wakeup_type", 2);
		}
		//LifeTrakLogger.info("wakeup info : " + objectWakeupInfo.toString());
		return objectWakeupInfo;
	}
	
	public synchronized JSONArray getLightSetting(long watchId) throws JSONException {
		JSONArray arrayLightSetting = new JSONArray();
		JSONObject objectDayLightAlertSetting = new JSONObject();
		JSONObject objectNightLightAlertSetting = new JSONObject();
		
		List<DayLightDetectSetting> daylightDetectSettings = DataSource.getInstance(mContext)
																		.getReadOperation()
																		.query("watchDaylightSetting = ?", String.valueOf(watchId))
																		.getResults(DayLightDetectSetting.class);
		
		List<NightLightDetectSetting> nightlightDetectSettings = DataSource.getInstance(mContext)
																			.getReadOperation()
																			.query("watchNightlightSetting = ?", String.valueOf(watchId))
																			.getResults(NightLightDetectSetting.class);
		
		if (daylightDetectSettings.size() > 0) {
			DayLightDetectSetting dayLightDetectSetting = daylightDetectSettings.get(0);
			objectDayLightAlertSetting.put("settings", "day");
			objectDayLightAlertSetting.put("duration", dayLightDetectSetting.getExposureDuration());
			objectDayLightAlertSetting.put("end_hour", dayLightDetectSetting.getEndTime() / 60);
			objectDayLightAlertSetting.put("end_min", dayLightDetectSetting.getEndTime() % 60);
			objectDayLightAlertSetting.put("level", dayLightDetectSetting.getExposureLevel());
			objectDayLightAlertSetting.put("level_high", dayLightDetectSetting.getDetectHighThreshold());
			objectDayLightAlertSetting.put("level_low", dayLightDetectSetting.getDetectLowThreshold());
			objectDayLightAlertSetting.put("level_mid", dayLightDetectSetting.getDetectMediumThreshold());
			objectDayLightAlertSetting.put("start_hour", dayLightDetectSetting.getStartTime() / 60);
			objectDayLightAlertSetting.put("start_min", dayLightDetectSetting.getStartTime() % 60);
			objectDayLightAlertSetting.put("status", dayLightDetectSetting.isEnabled());
			objectDayLightAlertSetting.put("alert_interval", dayLightDetectSetting.getInterval());
			objectDayLightAlertSetting.put("type", 1);
		}
		
		if (nightlightDetectSettings.size() > 0) {
			NightLightDetectSetting nightLightDetectSetting = nightlightDetectSettings.get(0);
			objectNightLightAlertSetting.put("settings", "night");
			objectNightLightAlertSetting.put("duration", nightLightDetectSetting.getExposureDuration());
			objectNightLightAlertSetting.put("end_hour", nightLightDetectSetting.getEndTime() / 60);
			objectNightLightAlertSetting.put("end_min", nightLightDetectSetting.getEndTime() % 60);
			objectNightLightAlertSetting.put("level", nightLightDetectSetting.getExposureLevel());
			objectNightLightAlertSetting.put("level_high", nightLightDetectSetting.getDetectHighThreshold());
			objectNightLightAlertSetting.put("level_low", nightLightDetectSetting.getDetectLowThreshold());
			objectNightLightAlertSetting.put("level_mid", nightLightDetectSetting.getDetectMediumThreshold());
			objectNightLightAlertSetting.put("start_hour", nightLightDetectSetting.getStartTime() / 60);
			objectNightLightAlertSetting.put("start_min", nightLightDetectSetting.getStartTime() % 60);
			objectNightLightAlertSetting.put("status", nightLightDetectSetting.isEnabled());
			objectNightLightAlertSetting.put("alert_interval", 0);
			objectNightLightAlertSetting.put("type", 1);
		}
		
		arrayLightSetting.put(objectDayLightAlertSetting);
		arrayLightSetting.put(objectNightLightAlertSetting);
		//.info("array light : " + arrayLightSetting.toString());
		return arrayLightSetting;
	}
	
	public synchronized JSONObject getActivityAlertSetting(long watchId) throws JSONException {
		JSONObject objectActivityAlert = new JSONObject();
		
		List<ActivityAlertSetting> activityAlertSettings = DataSource.getInstance(mContext)
																	.getReadOperation()
																	.query("watchActivityAlert = ?", String.valueOf(watchId))
																	.getResults(ActivityAlertSetting.class);
		
		if (activityAlertSettings.size() > 0) {
			ActivityAlertSetting activityAlertSetting = activityAlertSettings.get(0);
			objectActivityAlert.put("end_hour", activityAlertSetting.getEndTime() / 60);
			objectActivityAlert.put("end_min", activityAlertSetting.getEndTime() % 60);
			objectActivityAlert.put("start_hour", activityAlertSetting.getStartTime() / 60);
			objectActivityAlert.put("start_min", activityAlertSetting.getStartTime() % 60);
			objectActivityAlert.put("steps_threshold", activityAlertSetting.getStepsThreshold());
			objectActivityAlert.put("time_duration", activityAlertSetting.getTimeInterval());
			objectActivityAlert.put("type", 1);
//			objectActivityAlert.put("status", (activityAlertSetting.isEnabled()) ? 1 : 0 );
			objectActivityAlert.put("status", activityAlertSetting.isEnabled());
		}
		//LifeTrakLogger.info("activity alert: " + objectActivityAlert.toString());
		return objectActivityAlert;
	}
}
