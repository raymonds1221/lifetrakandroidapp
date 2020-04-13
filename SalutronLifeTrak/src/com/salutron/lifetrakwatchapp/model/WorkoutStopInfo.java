package com.salutron.lifetrakwatchapp.model;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

import android.os.Parcel;
import android.content.Context;

import com.salutron.lifetrakwatchapp.annotation.*;

import com.salutron.blesdk.SALWorkoutStopInfo;

@DataTable(name="WorkoutStopInfo")
public class WorkoutStopInfo extends BaseModel {
	@DataColumn(name="workoutHundreds")
	private int workoutHundreds;
	@DataColumn(name="workoutSeconds")
	private int workoutSeconds;
	@DataColumn(name="workoutMinutes")
	private int workoutMinutes;
	@DataColumn(name="workoutHours")
	private int workoutHours;
	@DataColumn(name="stopHundreds")
	private int stopHundreds;
	@DataColumn(name="stopSeconds")
	private int stopSeconds;
	@DataColumn(name="stopMinutes")
	private int stopMinutes;
	@DataColumn(name="stopHours")
	private int stopHours;
	@DataColumn(name="infoAndStop", isPrimary=true)
	private WorkoutInfo workoutInfo;


	@DataColumn(name="headerAndStop", isPrimary=true)
	private WorkoutHeader workoutHeader;
	// Derived fields
	private long pauseTime;
	private long resumeTime;

	public WorkoutStopInfo() { }

	public WorkoutStopInfo(Context context) {
		super(context);
	}
	
	public static final WorkoutStopInfo buildWorkoutStopInfo(Context context, SALWorkoutStopInfo salWorkoutStopInfo) {
		WorkoutStopInfo workoutStopInfo = new WorkoutStopInfo(context);
		workoutStopInfo.setWorkoutHundreds(salWorkoutStopInfo.nWorkoutHundreds);
		workoutStopInfo.setWorkoutSeconds(salWorkoutStopInfo.nWorkoutSeconds);
		workoutStopInfo.setWorkoutMinutes(salWorkoutStopInfo.nWorkoutMinutes);
		workoutStopInfo.setWorkoutHours(salWorkoutStopInfo.nWorkoutHours);
		workoutStopInfo.setStopHundreds(salWorkoutStopInfo.nStopHundreds);
		workoutStopInfo.setStopSeconds(salWorkoutStopInfo.nStopSeconds);
		workoutStopInfo.setStopMinutes(salWorkoutStopInfo.nStopMinutes);
		workoutStopInfo.setStopHours(salWorkoutStopInfo.nStopHours);

		return workoutStopInfo;
	}
	
	public static final List<WorkoutStopInfo> buildWorkoutStopInfo(Context context, List<SALWorkoutStopInfo> salWorkoutStopInfos) {
		List<WorkoutStopInfo> workoutStopInfos = new ArrayList<WorkoutStopInfo>();
		
		if (salWorkoutStopInfos != null) {
			for (SALWorkoutStopInfo salWorkoutStopInfo : salWorkoutStopInfos) {
				WorkoutStopInfo workoutStopInfo = buildWorkoutStopInfo(context, salWorkoutStopInfo);
				workoutStopInfos.add(workoutStopInfo);
			}
		}
		
		return workoutStopInfos;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

	@Override
	public void readFromParcel(Parcel source) {
	}

	public int getWorkoutHundreds() {
		return workoutHundreds;
	}

	public void setWorkoutHundreds(int workoutHundreds) {
		this.workoutHundreds = workoutHundreds;
	}

	public int getWorkoutSeconds() {
		return workoutSeconds;
	}

	public void setWorkoutSeconds(int workoutSeconds) {
		this.workoutSeconds = workoutSeconds;
	}

	public int getWorkoutMinutes() {
		return workoutMinutes;
	}

	public void setWorkoutMinutes(int workoutMinutes) {
		this.workoutMinutes = workoutMinutes;
	}

	public int getWorkoutHours() {
		return workoutHours;
	}

	public void setWorkoutHours(int workoutHours) {
		this.workoutHours = workoutHours;
	}

	public int getStopHundreds() {
		return stopHundreds;
	}

	public void setStopHundreds(int stopHundreds) {
		this.stopHundreds = stopHundreds;
	}

	public int getStopSeconds() {
		return stopSeconds;
	}

	public void setStopSeconds(int stopSeconds) {
		this.stopSeconds = stopSeconds;
	}

	public int getStopMinutes() {
		return stopMinutes;
	}

	public void setStopMinutes(int stopMinutes) {
		this.stopMinutes = stopMinutes;
	}

	public int getStopHours() {
		return stopHours;
	}

	public void setStopHours(int stopHours) {
		this.stopHours = stopHours;
	}

	/**
	 * Get time when workout was paused
	 * @return pause time in milliseconds
	 */
	public long getPauseTime() {
		if (pauseTime == 0) {
			final Calendar calendar = Calendar.getInstance();
			// Set the workout date
			calendar.setTimeInMillis(workoutInfo.getStartTime());
			calendar.set(Calendar.HOUR_OF_DAY, stopHours);
			calendar.set(Calendar.MINUTE, stopMinutes);
			calendar.set(Calendar.SECOND, stopSeconds);
			calendar.set(Calendar.MILLISECOND, stopHundreds);
			pauseTime = calendar.getTimeInMillis();
		}
		return pauseTime;
	}

	/**
	 * Get time when workout was resumed
	 * @return resume time in milliseconds
	 */
	public long getResumeTime() {
		if (resumeTime == 0) {
			final Calendar calendar = Calendar.getInstance();
			// Set the workout date
			calendar.setTimeInMillis(workoutInfo.getStartTime());
			calendar.set(Calendar.HOUR_OF_DAY, workoutHours);
			calendar.set(Calendar.MINUTE, workoutMinutes);
			calendar.set(Calendar.SECOND, workoutSeconds);
			calendar.set(Calendar.MILLISECOND, workoutHundreds);
			resumeTime = calendar.getTimeInMillis();
		}
		return resumeTime;
	}

	public WorkoutInfo getWorkoutInfo() {
		return workoutInfo;
	}

	public void setWorkoutInfo(WorkoutInfo workoutInfo) {
		this.workoutInfo = workoutInfo;
	}



	public WorkoutHeader getWorkoutHeader() {
		return workoutHeader;
	}

	public void setWorkoutHeader(WorkoutHeader workoutHeader) {
		this.workoutHeader = workoutHeader;
	}
}
