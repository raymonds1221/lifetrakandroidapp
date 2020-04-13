package com.salutron.lifetrakwatchapp.web;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Collections;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.salutron.lifetrakwatchapp.model.ActivityAlertSetting;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.LightDataPoint;
import com.salutron.lifetrakwatchapp.model.NightLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.Notification;
import com.salutron.lifetrakwatchapp.model.WakeupSetting;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.StatisticalDataPoint;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.SleepSetting;
import com.salutron.lifetrakwatchapp.model.CalibrationData;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.WorkoutSettings;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.androidquery.callback.AjaxStatus;

import com.salutron.blesdk.SALCalibration;

public class ServerRestoreAsync<T> extends BaseAsync<T> {
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private final Calendar mCalendar = Calendar.getInstance();

	public ServerRestoreAsync(Context context) {
		super(context);
	}

	@Override
	public void onCallback(String url, JSONObject result, AjaxStatus status) {
		if(result != null) {
			if(mListener != null)
				mListener.onAsyncSuccess(result);
		} else {
			if(mListener != null)
				mListener.onAsyncFail(status.getCode(), status.getMessage());
			status.invalidate();
		}
	}
	
	public List<StatisticalDataHeader> getStatisticalDataHeaders(JSONArray arrayDataHeaders, Watch watch) throws JSONException, ParseException {
		List<StatisticalDataHeader> dataHeaders = new ArrayList<StatisticalDataHeader>();

		for(int i=0;i<arrayDataHeaders.length();i++) {
			JSONObject objectDataHeader = arrayDataHeaders.getJSONObject(i);
			StatisticalDataHeader dataHeader = new StatisticalDataHeader();
			dataHeader.setAllocationBlockIndex(objectDataHeader.getInt("allocation_block_index"));
			dataHeader.setTotalSleep(objectDataHeader.getInt("total_sleep"));
			dataHeader.setTotalSteps(objectDataHeader.getLong("total_steps"));
			dataHeader.setTotalCalorie(objectDataHeader.getDouble("total_calories"));
			dataHeader.setTotalDistance(objectDataHeader.getDouble("total_distance"));
            dataHeader.setLightExposure(objectDataHeader.getInt("total_exposure_time"));
			
			mDateFormat.applyPattern("yyyy-MM-dd");
			mCalendar.setTime(mDateFormat.parse(objectDataHeader.getString("header_created_date")));
			
			int day = mCalendar.get(Calendar.DAY_OF_MONTH);
			int month = mCalendar.get(Calendar.MONTH) + 1;
			int year = mCalendar.get(Calendar.YEAR) - 1900;
			
			mDateFormat.applyPattern("hh:mm:ss");
			mCalendar.setTime(mDateFormat.parse(objectDataHeader.getString("start_time")));
			
			int startHour = mCalendar.get(Calendar.HOUR_OF_DAY);
			int startMinute = mCalendar.get(Calendar.MINUTE);
			int startSecond = mCalendar.get(Calendar.SECOND);
			
			mCalendar.setTime(mDateFormat.parse(objectDataHeader.getString("end_time")));
			
			int endHour = mCalendar.get(Calendar.HOUR_OF_DAY);
			int endMinute = mCalendar.get(Calendar.MINUTE);
			int endSecond = mCalendar.get(Calendar.SECOND);
			
			dataHeader.setDateStamp(mCalendar.getTime());
			dataHeader.setDateStampDay(day);
			dataHeader.setDateStampMonth(month);
			dataHeader.setDateStampYear(year);
			dataHeader.setTimeStartHour(startHour);
			dataHeader.setTimeStartMinute(startMinute);
			dataHeader.setTimeStartSecond(startSecond);
			dataHeader.setTimeEndHour(endHour);
			dataHeader.setTimeEndMinute(endMinute);
			dataHeader.setTimeEndSecond(endSecond);
			
			JSONArray arrayDataPoints = objectDataHeader.getJSONArray("data_point");
			List<StatisticalDataPoint> dataPoints = new ArrayList<StatisticalDataPoint>();
			
			for(int j=0;j<arrayDataPoints.length();j++) {
				JSONObject objectDataPoint = arrayDataPoints.getJSONObject(j);
				StatisticalDataPoint dataPoint = new StatisticalDataPoint();
				dataPoint.setDataPointId(objectDataPoint.getLong("datapoint_id"));
				dataPoint.setAverageHR(objectDataPoint.getInt("average_HR"));
				dataPoint.setAxisDirection(objectDataPoint.getInt("axis_direction"));
				dataPoint.setAxisMagnitude(objectDataPoint.getInt("axis_magnitude"));
				dataPoint.setDominantAxis(objectDataPoint.getInt("dominant_axis"));
				dataPoint.setSleepPoint02(objectDataPoint.getInt("sleep_point_02"));
				dataPoint.setSleepPoint24(objectDataPoint.getInt("sleep_point_24"));
				dataPoint.setSleepPoint46(objectDataPoint.getInt("sleep_point_46"));
				dataPoint.setSleepPoint68(objectDataPoint.getInt("sleep_point_68"));
				dataPoint.setSleepPoint810(objectDataPoint.getInt("sleep_point_810"));
				dataPoint.setSteps(objectDataPoint.getInt("steps"));
				dataPoint.setCalorie(objectDataPoint.getDouble("calorie"));
				dataPoint.setDistance(objectDataPoint.getDouble("distance"));
				dataPoint.setLux(objectDataPoint.getInt("lux"));
				dataPoint.setWristOff02(objectDataPoint.getInt("wrist_detection"));
				dataPoint.setStatisticalDataHeader(dataHeader);
				dataPoints.add(dataPoint);
			}
			
			Collections.sort(dataPoints, new StatisticalDataPointComparator());
			
			List<LightDataPoint> lightDataPoints = new ArrayList<LightDataPoint>();
			
			if (objectDataHeader.has("light_datapoint") && !objectDataHeader.isNull("light_datapoint")) {
				JSONArray arrayLightDataPoints = objectDataHeader.getJSONArray("light_datapoint");
				
				for (int j=0;j<arrayLightDataPoints.length();j++) {
					JSONObject objectLightDataPoint = arrayLightDataPoints.getJSONObject(j);
					LightDataPoint lightDataPoint = new LightDataPoint();
					lightDataPoint.setDataPointId(objectLightDataPoint.getInt("light_datapoint_id"));
					lightDataPoint.setRedValue(objectLightDataPoint.getInt("red"));
					lightDataPoint.setGreenValue(objectLightDataPoint.getInt("blue"));
					lightDataPoint.setBlueValue(objectLightDataPoint.getInt("green"));
					lightDataPoint.setIntegrationTime(objectLightDataPoint.getInt("integration_time"));
					lightDataPoint.setSensorGain(objectLightDataPoint.getInt("sensor_gain"));
					lightDataPoint.setStatisticalDataHeader(dataHeader);
					lightDataPoints.add(lightDataPoint);
				}
				
				Collections.sort(lightDataPoints, new LightDataPointComparator());
			}
			for (int j=0;j<lightDataPoints.size();j++) {
				if (j < dataPoints.size()) {
					int wristOff = dataPoints.get(j).getWristOff02();
					lightDataPoints.get(j).setWristOff02(wristOff);
				}
			}
			
			dataHeader.setStatisticalDataPoints(dataPoints);
			dataHeader.setLightDataPoints(lightDataPoints);
			dataHeader.setSyncedToCloud(true);
			dataHeader.setWatch(watch);
			dataHeaders.add(dataHeader);
		}
		
		return dataHeaders;
	}


	public StatisticalDataHeader getStatisticalDataHeaders(JSONObject arrayDataHeaders, Watch watch) throws JSONException, ParseException {
			JSONObject objectDataHeader = arrayDataHeaders;
			StatisticalDataHeader dataHeader = new StatisticalDataHeader();
			dataHeader.setAllocationBlockIndex(objectDataHeader.getInt("allocation_block_index"));
			dataHeader.setTotalSleep(objectDataHeader.getInt("total_sleep"));
			dataHeader.setTotalSteps(objectDataHeader.getLong("total_steps"));
			dataHeader.setTotalCalorie(objectDataHeader.getDouble("total_calories"));
			dataHeader.setTotalDistance(objectDataHeader.getDouble("total_distance"));
			dataHeader.setLightExposure(objectDataHeader.getInt("total_exposure_time"));

			mDateFormat.applyPattern("yyyy-MM-dd");
			mCalendar.setTime(mDateFormat.parse(objectDataHeader.getString("header_created_date")));

			int day = mCalendar.get(Calendar.DAY_OF_MONTH);
			int month = mCalendar.get(Calendar.MONTH) + 1;
			int year = mCalendar.get(Calendar.YEAR) - 1900;

			mDateFormat.applyPattern("hh:mm:ss");
			mCalendar.setTime(mDateFormat.parse(objectDataHeader.getString("start_time")));

			int startHour = mCalendar.get(Calendar.HOUR_OF_DAY);
			int startMinute = mCalendar.get(Calendar.MINUTE);
			int startSecond = mCalendar.get(Calendar.SECOND);

			mCalendar.setTime(mDateFormat.parse(objectDataHeader.getString("end_time")));

			int endHour = mCalendar.get(Calendar.HOUR_OF_DAY);
			int endMinute = mCalendar.get(Calendar.MINUTE);
			int endSecond = mCalendar.get(Calendar.SECOND);

			dataHeader.setDateStamp(mCalendar.getTime());
			dataHeader.setDateStampDay(day);
			dataHeader.setDateStampMonth(month);
			dataHeader.setDateStampYear(year);
			dataHeader.setTimeStartHour(startHour);
			dataHeader.setTimeStartMinute(startMinute);
			dataHeader.setTimeStartSecond(startSecond);
			dataHeader.setTimeEndHour(endHour);
			dataHeader.setTimeEndMinute(endMinute);
			dataHeader.setTimeEndSecond(endSecond);

			JSONArray arrayDataPoints = objectDataHeader.getJSONArray("data_point");
			List<StatisticalDataPoint> dataPoints = new ArrayList<StatisticalDataPoint>();

			for(int j=0;j<arrayDataPoints.length();j++) {
				JSONObject objectDataPoint = arrayDataPoints.getJSONObject(j);
				StatisticalDataPoint dataPoint = new StatisticalDataPoint();
				dataPoint.setDataPointId(objectDataPoint.getLong("datapoint_id"));
				dataPoint.setAverageHR(objectDataPoint.getInt("average_HR"));
				dataPoint.setAxisDirection(objectDataPoint.getInt("axis_direction"));
				dataPoint.setAxisMagnitude(objectDataPoint.getInt("axis_magnitude"));
				dataPoint.setDominantAxis(objectDataPoint.getInt("dominant_axis"));
				dataPoint.setSleepPoint02(objectDataPoint.getInt("sleep_point_02"));
				dataPoint.setSleepPoint24(objectDataPoint.getInt("sleep_point_24"));
				dataPoint.setSleepPoint46(objectDataPoint.getInt("sleep_point_46"));
				dataPoint.setSleepPoint68(objectDataPoint.getInt("sleep_point_68"));
				dataPoint.setSleepPoint810(objectDataPoint.getInt("sleep_point_810"));
				dataPoint.setSteps(objectDataPoint.getInt("steps"));
				dataPoint.setCalorie(objectDataPoint.getDouble("calorie"));
				dataPoint.setDistance(objectDataPoint.getDouble("distance"));
				dataPoint.setLux(objectDataPoint.getInt("lux"));
				dataPoint.setWristOff02(objectDataPoint.getInt("wrist_detection"));
				dataPoint.setStatisticalDataHeader(dataHeader);
				dataPoints.add(dataPoint);
			}

			Collections.sort(dataPoints, new StatisticalDataPointComparator());

			List<LightDataPoint> lightDataPoints = new ArrayList<LightDataPoint>();

			if (objectDataHeader.has("light_datapoint") && !objectDataHeader.isNull("light_datapoint")) {
				JSONArray arrayLightDataPoints = objectDataHeader.getJSONArray("light_datapoint");

				for (int j=0;j<arrayLightDataPoints.length();j++) {
					JSONObject objectLightDataPoint = arrayLightDataPoints.getJSONObject(j);
					LightDataPoint lightDataPoint = new LightDataPoint();
					lightDataPoint.setDataPointId(objectLightDataPoint.getInt("light_datapoint_id"));
					lightDataPoint.setRedValue(objectLightDataPoint.getInt("red"));
					lightDataPoint.setGreenValue(objectLightDataPoint.getInt("blue"));
					lightDataPoint.setBlueValue(objectLightDataPoint.getInt("green"));
					lightDataPoint.setIntegrationTime(objectLightDataPoint.getInt("integration_time"));
					lightDataPoint.setSensorGain(objectLightDataPoint.getInt("sensor_gain"));
					lightDataPoint.setStatisticalDataHeader(dataHeader);
					lightDataPoints.add(lightDataPoint);
				}

				Collections.sort(lightDataPoints, new LightDataPointComparator());
			}
			for (int j=0;j<lightDataPoints.size();j++) {
				if (j < dataPoints.size()) {
					int wristOff = dataPoints.get(j).getWristOff02();
					lightDataPoints.get(j).setWristOff02(wristOff);
				}
			}

			dataHeader.setStatisticalDataPoints(dataPoints);
			dataHeader.setLightDataPoints(lightDataPoints);

			dataHeader.setWatch(watch);


		return dataHeader;
	}

	
	public List<WorkoutInfo> getWorkoutInfos(JSONArray arrayWorkoutInfos, Watch watch, boolean isFromIOS) throws JSONException, ParseException {
		List<WorkoutInfo> workoutInfos = new ArrayList<WorkoutInfo>();
		
		for(int i=0;i<arrayWorkoutInfos.length();i++) {
			JSONObject objectWorkoutInfo = arrayWorkoutInfos.getJSONObject(i);
			WorkoutInfo workoutInfo = new WorkoutInfo();
			
			int duration = objectWorkoutInfo.getInt("workout_duration");
			if (isFromIOS && watch.getModel() == WATCHMODEL_C410){
				duration = duration * 100;
			}
			
			int hour = (duration / 100) / 3600;
			int minute = ((duration / 100) / 60) - (hour * 60);
			int seconds = (duration / 100) % 60;
			int hundredths = duration % 100;
			
			workoutInfo.setHour(hour);
			workoutInfo.setMinute(minute);
			workoutInfo.setSecond(seconds);
			workoutInfo.setHundredths(hundredths);
			workoutInfo.setCalories(objectWorkoutInfo.getDouble("calories"));
			workoutInfo.setDistance(objectWorkoutInfo.getDouble("distance"));
			workoutInfo.setSteps(objectWorkoutInfo.getLong("steps"));
			workoutInfo.setFlags(objectWorkoutInfo.getInt("distance_unit_flag"));
			
			mDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
			mCalendar.setTime(mDateFormat.parse(objectWorkoutInfo.getString("start_date_time")));
			
			workoutInfo.setDateStamp(mCalendar.getTime());
			workoutInfo.setDateStampDay(mCalendar.get(Calendar.DAY_OF_MONTH));
			workoutInfo.setDateStampMonth(mCalendar.get(Calendar.MONTH) + 1);
			workoutInfo.setDateStampYear(mCalendar.get(Calendar.YEAR) - 1900);
			workoutInfo.setTimeStampHour(mCalendar.get(Calendar.HOUR_OF_DAY));
			workoutInfo.setTimeStampMinute(mCalendar.get(Calendar.MINUTE));
			workoutInfo.setTimeStampSecond(mCalendar.get(Calendar.SECOND));
			
			if (objectWorkoutInfo.has("workout_stop") && !objectWorkoutInfo.isNull("workout_stop")) {
				JSONArray arrayWorkoutStops = objectWorkoutInfo.getJSONArray("workout_stop");
				List<WorkoutStopInfo> workoutStopInfos = new ArrayList<WorkoutStopInfo>();
				
				for (int j=0;j<arrayWorkoutStops.length();j++) {
					JSONObject objectWorkoutStop = arrayWorkoutStops.getJSONObject(j);
					WorkoutStopInfo workoutStopInfo = new WorkoutStopInfo();
					
					mDateFormat.applyPattern("HH:mm:ss");
					
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(mDateFormat.parse(objectWorkoutStop.getString("workout_time")));
					
					workoutStopInfo.setWorkoutHours(calendar.get(Calendar.HOUR_OF_DAY));
					workoutStopInfo.setWorkoutMinutes(calendar.get(Calendar.MINUTE));
					workoutStopInfo.setWorkoutSeconds(calendar.get(Calendar.SECOND));
					
					calendar.setTime(mDateFormat.parse(objectWorkoutStop.getString("stop_time")));
					
					workoutStopInfo.setStopHours(calendar.get(Calendar.HOUR_OF_DAY));
					workoutStopInfo.setStopMinutes(calendar.get(Calendar.MINUTE));
					workoutStopInfo.setStopSeconds(calendar.get(Calendar.SECOND));
					
					if(!workoutStopInfos.contains(workoutStopInfo))
						workoutStopInfos.add(workoutStopInfo);
					
				}
				
				workoutInfo.setWorkoutStopInfos(workoutStopInfos);
				workoutInfo.setWatch(watch);
				workoutInfo.setSyncedToCloud(true);
				workoutInfos.add(workoutInfo);
			}
		}
		
		return workoutInfos;
	}

	public List<WorkoutHeader> getWorkoutHeaders(JSONArray arrayWorkoutHeaders, Watch watch) throws JSONException, ParseException {
		List<WorkoutHeader> workoutHeaders = new ArrayList<>();

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		simpleDateFormat.applyPattern("MM-dd-yyyy hh:mm:ss");

		for (int i=0;i<arrayWorkoutHeaders.length();i++) {
			JSONObject objectWorkoutHeader = arrayWorkoutHeaders.getJSONObject(i);
			WorkoutHeader workoutHeader = new WorkoutHeader();

			int timeStampSecond = objectWorkoutHeader.getInt("stamp_second");
			int timeStampMinute = objectWorkoutHeader.getInt("stamp_minute");
			int timeStampHour = objectWorkoutHeader.getInt("stamp_hour");
			int dateStampDay = objectWorkoutHeader.getInt("stamp_day");
			int dateStampMonth = objectWorkoutHeader.getInt("stamp_month");
			int dateStampYear = objectWorkoutHeader.getInt("stamp_year");

			calendar.set(Calendar.SECOND, timeStampSecond);
			calendar.set(Calendar.MINUTE, timeStampMinute);
			calendar.set(Calendar.HOUR_OF_DAY, timeStampHour);
			calendar.set(Calendar.DAY_OF_MONTH, dateStampDay);
			calendar.set(Calendar.MONTH, dateStampMonth);
			calendar.set(Calendar.YEAR, dateStampYear);

			workoutHeader.setAutoSplitThreshold(objectWorkoutHeader.getInt("auto_split_threshold"));
			workoutHeader.setAutoSplitType(objectWorkoutHeader.getInt("auto_split_type"));
			workoutHeader.setAverageBPM(objectWorkoutHeader.getInt("average_bpm"));
			workoutHeader.setHour(objectWorkoutHeader.getInt("hour"));
			workoutHeader.setHundredths(objectWorkoutHeader.getInt("hundredths"));
			workoutHeader.setLogRateHR(objectWorkoutHeader.getInt("log_rate_hr"));
			workoutHeader.setMaximumBPM(objectWorkoutHeader.getInt("maximum_bpm"));
			workoutHeader.setMinimumBPM(objectWorkoutHeader.getInt("minimum_bpm"));
			workoutHeader.setMinute(objectWorkoutHeader.getInt("minute"));
			workoutHeader.setCountHRRecord(objectWorkoutHeader.getInt("record_count_hr"));
			workoutHeader.setCountSplitsRecord(objectWorkoutHeader.getInt("record_count_splits"));
			workoutHeader.setCountStopsRecord(objectWorkoutHeader.getInt("record_count_stops"));
			workoutHeader.setCountTotalRecord(objectWorkoutHeader.getInt("record_count_total"));
			workoutHeader.setSecond(objectWorkoutHeader.getInt("second"));
			workoutHeader.setDateStampDay(dateStampDay);
			workoutHeader.setTimeStampHour(timeStampHour);
			workoutHeader.setTimeStampMinute(timeStampMinute);
			workoutHeader.setDateStampMonth(dateStampMonth);
			workoutHeader.setTimeStampHour(timeStampHour);
			workoutHeader.setTimeStampSecond(timeStampSecond);
			workoutHeader.setDateStampYear(dateStampYear);
			workoutHeader.setStatusFlags(objectWorkoutHeader.getInt("status_flag"));
			workoutHeader.setUserMaxHR(objectWorkoutHeader.getInt("user_max_hr"));
			workoutHeader.setZone0LowerHR(objectWorkoutHeader.getInt("zone0_lower_hr"));
			workoutHeader.setZone0UpperHR(objectWorkoutHeader.getInt("zone0_upper_hr"));
			workoutHeader.setZone1LowerHR(objectWorkoutHeader.getInt("zone1_lower_hr"));
			workoutHeader.setZone2LowerHR(objectWorkoutHeader.getInt("zone2_lower_hr"));
			workoutHeader.setZone3LowerHR(objectWorkoutHeader.getInt("zone3_lower_hr"));
			workoutHeader.setZone4LowerHR(objectWorkoutHeader.getInt("zone4_lower_hr"));
			workoutHeader.setZone5LowerHR(objectWorkoutHeader.getInt("zone5_lower_hr"));
			workoutHeader.setZone5UpperHR(objectWorkoutHeader.getInt("zone5_upper_hr"));
			workoutHeader.setZoneTrainType(objectWorkoutHeader.getInt("zone_train_type"));
			workoutHeader.setSteps(objectWorkoutHeader.getLong("steps"));
			workoutHeader.setCalories(objectWorkoutHeader.getDouble("calories"));
			workoutHeader.setDistance(objectWorkoutHeader.getDouble("distance"));

			JSONArray arrayWorkoutStops = objectWorkoutHeader.getJSONArray("workout_stop");
			JSONArray arrayWorkoutHRData = objectWorkoutHeader.getJSONArray("workout_hr_data");

			List<WorkoutStopInfo> workoutStopInfos = getWorkoutStopInfos(arrayWorkoutStops);

			//int[] workoutHRValues = new int[arrayWorkoutHRData.length()];
			Integer[] integers = new Integer[arrayWorkoutHRData.length()];
			Arrays.fill(integers, 0);
			List<Integer> integerList = Arrays.asList(integers);


			for (int j=0;j<integerList.size();j++) {
				JSONObject objectWorkoutHRData = arrayWorkoutHRData.getJSONObject(j);
				integerList.set(objectWorkoutHRData.getInt("index"), objectWorkoutHRData.getInt("hr_data"));
			}

			String headerHeartRate = integerList.toString();

			workoutHeader.setWorkoutStopInfo(workoutStopInfos);
			workoutHeader.setHeaderHeartRate(headerHeartRate);
			workoutHeader.setWatch(watch);
			workoutHeader.setSyncedToCloud(true);

			workoutHeaders.add(workoutHeader);
		}

		return workoutHeaders;
	}

	public List<WorkoutStopInfo> getWorkoutStopInfos(JSONArray arrayWorkoutStopInfos) throws JSONException, ParseException{
		List<WorkoutStopInfo> workoutStopInfos = new ArrayList<>();

		for (int i=0;i<arrayWorkoutStopInfos.length();i++) {
			JSONObject objectWorkoutStop = arrayWorkoutStopInfos.getJSONObject(i);

			WorkoutStopInfo workoutStopInfo = new WorkoutStopInfo();

			mDateFormat.applyPattern("HH:mm:ss");

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(mDateFormat.parse(objectWorkoutStop.getString("workout_time")));

			workoutStopInfo.setWorkoutHours(calendar.get(Calendar.HOUR_OF_DAY));
			workoutStopInfo.setWorkoutMinutes(calendar.get(Calendar.MINUTE));
			workoutStopInfo.setWorkoutSeconds(calendar.get(Calendar.SECOND));

			calendar.setTime(mDateFormat.parse(objectWorkoutStop.getString("stop_time")));

			workoutStopInfo.setStopHours(calendar.get(Calendar.HOUR_OF_DAY));
			workoutStopInfo.setStopMinutes(calendar.get(Calendar.MINUTE));
			workoutStopInfo.setStopSeconds(calendar.get(Calendar.SECOND));

			workoutStopInfos.add(workoutStopInfo);
		}

		return workoutStopInfos;
	}
	
	public List<SleepDatabase> getSleepDatabases(JSONArray arraySleepDatabases, Watch watch) throws JSONException, ParseException {
		List<SleepDatabase> sleepDatabases = new ArrayList<SleepDatabase>();
		
		for(int i=0;i<arraySleepDatabases.length();i++) {
			JSONObject objectSleepDatabase = arraySleepDatabases.getJSONObject(i);
			SleepDatabase sleepDatabase = new SleepDatabase();
			
			mDateFormat.applyPattern("HH:mm:ss");
			mCalendar.setTime(mDateFormat.parse(objectSleepDatabase.getString("sleep_start_time")));
			
			sleepDatabase.setHourSleepStart(mCalendar.get(Calendar.HOUR_OF_DAY));
			sleepDatabase.setMinuteSleepStart(mCalendar.get(Calendar.MINUTE));
			
			mCalendar.setTime(mDateFormat.parse(objectSleepDatabase.getString("sleep_end_time")));
			
			sleepDatabase.setHourSleepEnd(mCalendar.get(Calendar.HOUR_OF_DAY));
			sleepDatabase.setMinuteSleepEnd(mCalendar.get(Calendar.MINUTE));
			sleepDatabase.setSleepOffset(objectSleepDatabase.getInt("sleep_offset"));
			sleepDatabase.setDeepSleepCount(objectSleepDatabase.getInt("deep_sleep_count"));
			sleepDatabase.setLightSleepCount(objectSleepDatabase.getInt("light_sleep_count"));
			sleepDatabase.setLapses(objectSleepDatabase.getInt("lapses"));
			sleepDatabase.setSleepDuration(objectSleepDatabase.getInt("sleep_duration"));
			sleepDatabase.setExtraInfo(objectSleepDatabase.getInt("extra_info"));

			int isWatch = objectSleepDatabase.optInt("is_watch", 1);
			boolean isWatchB = (isWatch != 0);
			sleepDatabase.setIsWatch(isWatchB);

			int isModified = objectSleepDatabase.optInt("is_modified", 0);
			boolean isModifiedB = (isWatch != 0);
			sleepDatabase.setIsWatch(isModifiedB);

			
			mDateFormat.applyPattern("yyyy-MM-dd");
			mCalendar.setTime(mDateFormat.parse(objectSleepDatabase.getString("sleep_created_date")));
			
			sleepDatabase.setDateStampDay(mCalendar.get(Calendar.DAY_OF_MONTH));
			sleepDatabase.setDateStampMonth(mCalendar.get(Calendar.MONTH) + 1);
			sleepDatabase.setDateStampYear(mCalendar.get(Calendar.YEAR) - 1900);
			sleepDatabase.setWatch(watch);
			sleepDatabase.setSyncedToCloud(true);
			sleepDatabases.add(sleepDatabase);
		}
		
		return sleepDatabases;
	}
	
	public UserProfile getUserProfile(JSONObject objectUserProfile, Watch watch) throws ParseException, JSONException {
		UserProfile userProfile = new UserProfile();
		
		mDateFormat.applyPattern("yyyy-MM-dd");
		mCalendar.setTime(mDateFormat.parse(objectUserProfile.getString("birthday")));
		
		userProfile.setBirthDay(mCalendar.get(Calendar.DAY_OF_MONTH));
		userProfile.setBirthMonth(mCalendar.get(Calendar.MONTH) + 1);
		userProfile.setBirthYear(mCalendar.get(Calendar.YEAR));
		userProfile.setGender(objectUserProfile.getString("gender").equals("male")?GENDER_MALE:GENDER_FEMALE);
		userProfile.setUnitSystem(objectUserProfile.getString("unit").equals("metric")?UNIT_METRIC:UNIT_IMPERIAL);
		userProfile.setSensitivity(0);
		userProfile.setHeight(objectUserProfile.getInt("height"));
		userProfile.setWeight(objectUserProfile.getInt("weight"));
		userProfile.setWatch(watch);
		
		return userProfile;
	}
	
	public List<Goal> getGoals(JSONArray arrayGoals, Watch watch) throws JSONException, ParseException {
		List<Goal> goals = new ArrayList<Goal>();
		
		for(int i=0;i<arrayGoals.length();i++) {

			JSONObject objectGoal = arrayGoals.getJSONObject(i);

            mDateFormat.applyPattern("yyyy-MM-dd hh:mm:ss");
            mCalendar.setTime(mDateFormat.parse(objectGoal.getString("goal_created_date_time")));

			Goal goal = new Goal();
			goal.setCalorieGoal(objectGoal.getLong("calories"));
			goal.setStepGoal(objectGoal.getLong("steps"));
			goal.setDistanceGoal(objectGoal.getDouble("distance"));
			goal.setSleepGoal(objectGoal.getInt("sleep"));
			goal.setDate(mCalendar.getTime());
			goal.setDateStampDay(mCalendar.get(Calendar.DAY_OF_MONTH));
			goal.setDateStampMonth(mCalendar.get(Calendar.MONTH) + 1);
			goal.setDateStampYear(mCalendar.get(Calendar.YEAR) - 1900);
			goal.setWatch(watch);
			goals.add(goal);
		}
		
		return goals;
	}
	
	public SleepSetting getSleepSetting(JSONObject objectSleepSetting, Watch watch) throws JSONException {
		SleepSetting sleepSetting = new SleepSetting();
		
		sleepSetting.setSleepGoalMinutes(objectSleepSetting.getInt("sleep_goal_lo"));
		sleepSetting.setSleepDetectType(objectSleepSetting.getString("sleep_mode").equals("manual") ? 0 : 1);
		sleepSetting.setWatch(watch);
		
		return sleepSetting;
	}
	
	public CalibrationData getCalibrationData(JSONObject objectDeviceSettings, Watch watch) throws JSONException {
		CalibrationData calibrationData = new CalibrationData();



		int calType = SALCalibration.STEP_CALIBRATION;
		if (objectDeviceSettings.getString("type").toString().equals("step")){
			calType =  SALCalibration.STEP_CALIBRATION;
		}
		else if (objectDeviceSettings.getString("walk").toString().equals("step")){
			calType =  SALCalibration.WALK_DISTANCE_CALIBRATION;
		}
		else{
			calType =  SALCalibration.RUN_DISTANCE_CALIBRATION;
		}
		calibrationData.setCalibrationType(calType);

        calibrationData.setStepCalibration(objectDeviceSettings.getInt("calib_step"));
		calibrationData.setDistanceCalibrationWalk(objectDeviceSettings.getInt("calib_walk"));
        calibrationData.setDistanceCalibrationRun(objectDeviceSettings.getInt("calib_run"));
        calibrationData.setCaloriesCalibration(objectDeviceSettings.getInt("calib_calories"));
		calibrationData.setAutoEL(objectDeviceSettings.getInt("auto_EL"));
		calibrationData.setWatch(watch);
		
		return calibrationData;
	}

    public Notification getNotification(JSONObject objectDeviceSettings, Watch watch) throws JSONException {
        Notification notification = new Notification();
        notification.setEmailEnabled(objectDeviceSettings.getInt("noti_email") == 1);
        notification.setIncomingCallEnabled(objectDeviceSettings.getInt("noti_incoming_call") == 1);
        notification.setMissedCallEnabled(objectDeviceSettings.getInt("noti_missed_call") == 1);
        notification.setSmsEnabled(objectDeviceSettings.getInt("noti_sms") == 1);
        notification.setVoiceMailEnabled(objectDeviceSettings.getInt("noti_voice_mail") == 1);
        notification.setScheduleEnabled(objectDeviceSettings.getInt("noti_schedules") == 1);
        notification.setHighPriorityEnabled(objectDeviceSettings.getInt("noti_high_prio") == 1);
        notification.setInstantMessageEnabled(objectDeviceSettings.getInt("noti_social") == 1);
        notification.setWatch(watch);
        return notification;
    }

	public WorkoutSettings getWorkOutSettings(JSONObject objectDeviceSettings, Watch watch) throws JSONException {
		WorkoutSettings workoutSettings = new WorkoutSettings();
		workoutSettings.setHrLoggingRate(objectDeviceSettings.optInt("hr_log_rate"));
		workoutSettings.setDatabaseUsage(objectDeviceSettings.optInt("database_usage"));
		workoutSettings.setDatabaseUsageMax(objectDeviceSettings.optInt("database_usage_max"));
		workoutSettings.setReconnectTime(objectDeviceSettings.optInt("reconnect_timeout"));
		workoutSettings.setWatch(watch);
		return workoutSettings;
	}
	
	public TimeDate getTimeDate(JSONObject objectDeviceSettings, Watch watch) throws JSONException {
		TimeDate timeDate = new TimeDate();
		
		int hourFormat = objectDeviceSettings.getInt("hour_format");
		String dateFormat = objectDeviceSettings.getString("date_format");
		
		if(hourFormat == 12) {
			timeDate.setHourFormat(TIME_FORMAT_12_HR);
		} else {
			timeDate.setHourFormat(TIME_FORMAT_24_HR);
		}
		if (watch.getModel() ==  WATCHMODEL_R415 ) {

            if (dateFormat.equals("DDMM")) {
                timeDate.setDateFormat(DATE_FORMAT_DDMM);
            } else if (dateFormat.equals("MMDD")) {
                timeDate.setDateFormat(DATE_FORMAT_MMDD);
            } else if (dateFormat.equals("MMMDD")) {
                timeDate.setDateFormat(DATE_FORMAT_MMMDD);
            } else {
                timeDate.setDateFormat(DATE_FORMAT_DDMMM);
            }
        }
        else{
            if (dateFormat.equals("DDMM")) {
                timeDate.setDateFormat(DATE_FORMAT_DDMM);
            } else {
                timeDate.setDateFormat(DATE_FORMAT_MMDD);
            }
        }

		
		timeDate.setWatch(watch);
		
		return timeDate;
	}
	
	public WakeupSetting getWakeupSetting(JSONObject objectWakeupSetting, Watch watch) throws JSONException, ParseException {
		WakeupSetting wakeupSetting = new WakeupSetting();
		
		mDateFormat.applyPattern("HH:mm:ss");
		

		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(mDateFormat.parse(objectWakeupSetting.getString("wakeup_time")));

        wakeupSetting.setSnoozeTime(objectWakeupSetting.getInt("snooze_min"));
        wakeupSetting.setSnoozeEnabled(objectWakeupSetting.getInt("snooze_mode") == 1);
		wakeupSetting.setTime(calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));
		wakeupSetting.setWakeupTimeHour(calendar.get(Calendar.HOUR_OF_DAY));
		wakeupSetting.setWakeupTimeMinute(calendar.get(Calendar.MINUTE));
		wakeupSetting.setEnabled(objectWakeupSetting.getInt("wakeup_mode") == 1);
		wakeupSetting.setWatch(watch);
		
		return wakeupSetting;
	}
	
	public DayLightDetectSetting getDayLightDetectSetting(JSONObject objectDayLightSetting, Watch watch) throws JSONException {
		DayLightDetectSetting dayLightDetectSetting = new DayLightDetectSetting();
		int endTime = objectDayLightSetting.getInt("end_hour") * 60 + objectDayLightSetting.getInt("end_min");
		int startTime = objectDayLightSetting.getInt("start_hour") * 60 + objectDayLightSetting.getInt("start_min");

		dayLightDetectSetting.setExposureDuration(objectDayLightSetting.getInt("duration"));
		dayLightDetectSetting.setEndTime(endTime);
		dayLightDetectSetting.setStartTime(startTime);
		dayLightDetectSetting.setExposureLevel(objectDayLightSetting.getInt("level"));
		dayLightDetectSetting.setDetectHighThreshold(objectDayLightSetting.getInt("level_high"));
		dayLightDetectSetting.setDetectLowThreshold(objectDayLightSetting.getInt("level_low"));
		dayLightDetectSetting.setDetectMediumThreshold(objectDayLightSetting.getInt("level_mid"));
		dayLightDetectSetting.setEnabled(objectDayLightSetting.getInt("status") == 1);
		dayLightDetectSetting.setInterval(objectDayLightSetting.getInt("alert_interval"));
		dayLightDetectSetting.setWatch(watch);
		
		return dayLightDetectSetting;
	}
	
	public NightLightDetectSetting getNightLightDetectSetting(JSONObject objectNightLightDetectSetting, Watch watch) throws JSONException {
		NightLightDetectSetting nightLightDetectSetting = new NightLightDetectSetting();
		int endTime = objectNightLightDetectSetting.getInt("end_hour") * 60 + objectNightLightDetectSetting.getInt("end_min");
		int startTime = objectNightLightDetectSetting.getInt("start_hour") * 60 + objectNightLightDetectSetting.getInt("start_min");

        nightLightDetectSetting.setExposureDuration(objectNightLightDetectSetting.getInt("duration"));
		nightLightDetectSetting.setEndTime(endTime);
		nightLightDetectSetting.setStartTime(startTime);
		nightLightDetectSetting.setExposureLevel(objectNightLightDetectSetting.getInt("level"));
		nightLightDetectSetting.setDetectHighThreshold(objectNightLightDetectSetting.getInt("level_high"));
		nightLightDetectSetting.setDetectLowThreshold(objectNightLightDetectSetting.getInt("level_low"));
		nightLightDetectSetting.setDetectMediumThreshold(objectNightLightDetectSetting.getInt("level_mid"));
		nightLightDetectSetting.setEnabled(objectNightLightDetectSetting.getInt("status") == 1);
		nightLightDetectSetting.setWatch(watch);

		return nightLightDetectSetting;
	}
	
	public ActivityAlertSetting getActivityAlertSetting(JSONObject objectActivityAlertSetting, Watch watch) throws JSONException {
		ActivityAlertSetting activityAlertSetting = new ActivityAlertSetting();
		
		int endTime = objectActivityAlertSetting.getInt("end_hour") * 60 + objectActivityAlertSetting.getInt("end_min");
		int startTime = objectActivityAlertSetting.getInt("start_hour") * 60 + objectActivityAlertSetting.getInt("start_min");
		
		activityAlertSetting.setStartTime(startTime);
		activityAlertSetting.setEndTime(endTime);
		activityAlertSetting.setStepsThreshold(objectActivityAlertSetting.getInt("steps_threshold"));
		activityAlertSetting.setTimeInterval(objectActivityAlertSetting.getInt("time_duration"));
		activityAlertSetting.setEnabled(objectActivityAlertSetting.getInt("status") == 1);
		activityAlertSetting.setWatch(watch);
		
		return activityAlertSetting;
	}
	
	private class StatisticalDataPointComparator implements Comparator<StatisticalDataPoint> {
		@Override
		public int compare(StatisticalDataPoint lhs, StatisticalDataPoint rhs) {
			
			if    (lhs.getDataPointId() > rhs.getDataPointId())
				return 1;
			else if    (rhs.getDataPointId() > lhs.getDataPointId())
				return -1;
			return 0;
		}
	}
	
	private class LightDataPointComparator implements Comparator<LightDataPoint> {
		@Override
		public int compare(LightDataPoint lhs, LightDataPoint rhs) {
			if    (lhs.getDataPointId() > rhs.getDataPointId())
				return 1;
			else if    (rhs.getDataPointId() > lhs.getDataPointId())
				return -1;
			return 0;
		}
	}
}
