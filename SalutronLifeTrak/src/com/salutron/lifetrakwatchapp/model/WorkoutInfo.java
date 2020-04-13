package com.salutron.lifetrakwatchapp.model;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ArrayList;

import android.os.Parcel;
import android.content.Context;

import com.salutron.lifetrakwatchapp.annotation.DataTable;
import com.salutron.lifetrakwatchapp.annotation.DataColumn;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.blesdk.SALWorkoutInfo;

@DataTable(name="WorkoutInfo")
public class WorkoutInfo extends BaseModel {
	@DataColumn(name="flags")
	private int flags;
	@DataColumn(name="reserve1")
	private int reserve1;
	@DataColumn(name="reserve2")
	private int reserve2;
	@DataColumn(name="timeStampHour")
	private int timeStampHour;
	@DataColumn(name="timeStampMinute")
	private int timeStampMinute;
	@DataColumn(name="timeStampSecond")
	private int timeStampSecond;
	@DataColumn(name="dateStampYear")
	private int dateStampYear;
	@DataColumn(name="dateStampMonth")
	private int dateStampMonth;
	@DataColumn(name="dateStampDay")
	private int dateStampDay;
	@DataColumn(name="dateStamp")
	private Date dateStamp;
	@DataColumn(name="hundredths")
	private int hundredths;
	@DataColumn(name="second")
	private int second;
	@DataColumn(name="minute")
	private int minute;
	@DataColumn(name="hour")
	private int hour;
	@DataColumn(name="distance")
	private double distance;
	@DataColumn(name="calories")
	private double calories;
	@DataColumn(name="steps")
	private long steps;
	@DataColumn(name="watchWorkoutInfo", isPrimary=true)
	private Watch watch;
	@DataColumn(name="infoAndStop", isForeign=true, model = WorkoutStopInfo.class)
	private List<WorkoutStopInfo> workoutStopInfos;
	@DataColumn(name="workoutId")
	private int workoutId;

	@DataColumn(name="syncedToCloud")
	private boolean syncedToCloud;

	// Derived fields
	private long startTime;
	private long endTime;
	
	public WorkoutInfo() { }
	
	WorkoutInfo(Context context) {
		super(context);
	}
	
	public static final WorkoutInfo buildWorkoutInfo(Context context, SALWorkoutInfo salWorkoutInfo) {
		WorkoutInfo workoutInfo = new WorkoutInfo(context);
		
		workoutInfo.setFlags(salWorkoutInfo.flags);
		workoutInfo.setTimeStampHour(salWorkoutInfo.timestamp.nHour);
		workoutInfo.setTimeStampMinute(salWorkoutInfo.timestamp.nMinute);
		workoutInfo.setTimeStampSecond(salWorkoutInfo.timestamp.nSecond);
		workoutInfo.setDateStampYear(salWorkoutInfo.datestamp.nYear);
		workoutInfo.setDateStampMonth(salWorkoutInfo.datestamp.nMonth);
		workoutInfo.setDateStampDay(salWorkoutInfo.datestamp.nDay);
		
		Calendar calendar = new GregorianCalendar(workoutInfo.getDateStampYear(), 
											workoutInfo.getDateStampMonth(), workoutInfo.getDateStampDay());
		workoutInfo.setDateStamp(calendar.getTime());
		workoutInfo.setHundredths(salWorkoutInfo.hundredths);
		workoutInfo.setSecond(salWorkoutInfo.second);
		workoutInfo.setMinute(salWorkoutInfo.minute);
		workoutInfo.setHour(salWorkoutInfo.hour);
		workoutInfo.setDistance(salWorkoutInfo.distance);
		workoutInfo.setCalories(salWorkoutInfo.calories);
		workoutInfo.setSteps(salWorkoutInfo.steps);
		workoutInfo.setWorkoutId(salWorkoutInfo.workoutID);
		
		return workoutInfo;
	}
	
	public static final List<WorkoutInfo> buildWorkoutInfo(Context context, List<SALWorkoutInfo> salWorkoutInfos) {
		List<WorkoutInfo> workoutInfos = new ArrayList<WorkoutInfo>();
		
		for (SALWorkoutInfo salWorkoutInfo : salWorkoutInfos) {
			WorkoutInfo workoutInfo = buildWorkoutInfo(context, salWorkoutInfo);
			workoutInfos.add(workoutInfo);
		}
		
		return workoutInfos;
	}
	
	public boolean isExists() {
		if (getLifeTrakApplication().getSelectedWatch() == null) {
			return false;
		}
		
		String query = "watchWorkoutInfo = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ? and " +
						"timeStampHour = ? and timeStampMinute = ? and timeStampSecond = ?";
		
		List<WorkoutInfo> workoutInfos = DataSource.getInstance(mContext)
													.getReadOperation()
													.query(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
															String.valueOf(dateStampYear), String.valueOf(dateStampMonth), String.valueOf(dateStampDay),
															String.valueOf(timeStampHour), String.valueOf(timeStampMinute), String.valueOf(timeStampSecond))
													.getResults(WorkoutInfo.class);
		
		if (workoutInfos.size() > 0) {
			WorkoutInfo workoutInfo = workoutInfos.get(0);
			this.id = workoutInfo.getId();
			return true;
		}
		
		return false;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
	}

	@Override
	public void readFromParcel(Parcel source) {
		
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getReserve1() {
		return reserve1;
	}

	public void setReserve1(int reserve1) {
		this.reserve1 = reserve1;
	}

	public int getReserve2() {
		return reserve2;
	}

	public void setReserve2(int reserve2) {
		this.reserve2 = reserve2;
	}

	public int getTimeStampHour() {
		return timeStampHour;
	}

	public void setTimeStampHour(int timeStampHour) {
		this.timeStampHour = timeStampHour;
	}

	public int getTimeStampMinute() {
		return timeStampMinute;
	}

	public void setTimeStampMinute(int timeStampMinute) {
		this.timeStampMinute = timeStampMinute;
	}

	public int getTimeStampSecond() {
		return timeStampSecond;
	}

	public void setTimeStampSecond(int timeStampSecond) {
		this.timeStampSecond = timeStampSecond;
	}

	public int getDateStampYear() {
		return dateStampYear;
	}

	public void setDateStampYear(int dateStampYear) {
		this.dateStampYear = dateStampYear;
	}

	public int getDateStampMonth() {
		return dateStampMonth;
	}

	public void setDateStampMonth(int dateStampMonth) {
		this.dateStampMonth = dateStampMonth;
	}

	public int getDateStampDay() {
		return dateStampDay;
	}

	public void setDateStampDay(int dateStampDay) {
		this.dateStampDay = dateStampDay;
	}

	public Date getDateStamp() {
		return dateStamp;
	}

	public void setDateStamp(Date dateStamp) {
		this.dateStamp = dateStamp;
	}

	public int getHundredths() {
		return hundredths;
	}

	public void setHundredths(int hundredths) {
		this.hundredths = hundredths;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	/**
	 * Get start time of workout
	 * @return start time in milliseconds
	 */
	public long getStartTime() {
		if (startTime == 0) {
			final Calendar calendar = Calendar.getInstance();
			calendar.set(dateStampYear + 1900, dateStampMonth - 1, dateStampDay,
					timeStampHour, timeStampMinute, timeStampSecond);
			calendar.set(Calendar.MILLISECOND, 0);
			startTime = calendar.getTimeInMillis();
		}
		return startTime;
	}

	/**
	 * Get end time of workout
	 * @return end time in milliseconds
	 */
	public long getEndTime() {
		if (endTime == 0) {
			final Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(getStartTime());
			calendar.add(Calendar.HOUR_OF_DAY, hour);
			calendar.add(Calendar.MINUTE, minute);
			calendar.add(Calendar.SECOND, second);
			calendar.add(Calendar.MILLISECOND, hundredths);
			endTime = calendar.getTimeInMillis();
		}
		return endTime;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getCalories() {
		return calories;
	}

	public void setCalories(double calories) {
		this.calories = calories;
	}

	public long getSteps() {
		return steps;
	}

	public void setSteps(long steps) {
		this.steps = steps;
	}

	public Watch getWatch() {
		return watch;
	}

	public void setWatch(Watch watch) {
		this.watch = watch;
	}
	
	public int getWorkoutId() {
		return workoutId;
	}

	public void setWorkoutId(int workoutId) {
		this.workoutId = workoutId;
	}

	public List<WorkoutStopInfo> getWorkoutStopInfos() {
		return workoutStopInfos;
	}

	public void setWorkoutStopInfos(List<WorkoutStopInfo> workoutStopInfos) {
		this.workoutStopInfos = workoutStopInfos;
		
		for (WorkoutStopInfo workoutStopInfo : workoutStopInfos) {
			workoutStopInfo.setWorkoutInfo(this);
		}
	}

	public boolean isSyncedToCloud() {
		return syncedToCloud;
	}

	public void setSyncedToCloud(boolean syncedToCloud) {
		this.syncedToCloud = syncedToCloud;
	}
}
