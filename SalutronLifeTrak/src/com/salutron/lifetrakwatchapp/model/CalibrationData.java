package com.salutron.lifetrakwatchapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.content.Context;

import com.salutron.lifetrakwatchapp.annotation.*;

@DataTable(name="CalibrationData")
public class CalibrationData extends BaseModel {
	@DataColumn(name="calibrationType")
	private int calibrationType;
	@DataColumn(name="stepCalibration")
	private int stepCalibration;
	@DataColumn(name="distanceCalibrationWalk")
	private int distanceCalibrationWalk;
	@DataColumn(name="distanceCalibrationRun")
	private int distanceCalibrationRun;
	@DataColumn(name="caloriesCalibration")
	private int caloriesCalibration;
	@DataColumn(name="autoEL")
	private int autoEL;
	@DataColumn(name="watchCalibrationData", isPrimary=true)
	private Watch watch;
	
	public CalibrationData() {
		
	}
	
	public CalibrationData(Context context) {
		super(context);
	}
	
	public CalibrationData(Parcel source) {
		readFromParcel(source);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeInt(calibrationType);
		dest.writeInt(stepCalibration);
		dest.writeInt(distanceCalibrationWalk);
		dest.writeInt(distanceCalibrationRun);
		dest.writeInt(autoEL);
		dest.writeParcelable(watch, flags);
	}

	@Override
	public void readFromParcel(Parcel source) {
		id = source.readLong();
		calibrationType = source.readInt();
		stepCalibration = source.readInt();
		distanceCalibrationWalk = source.readInt();
		distanceCalibrationRun = source.readInt();
		autoEL = source.readInt();
		watch = source.readParcelable(Watch.class.getClassLoader());
	}

	public int getCalibrationType() {
		return calibrationType;
	}

	public void setCalibrationType(int calibrationType) {
		this.calibrationType = calibrationType;
	}

	public int getStepCalibration() {
		return stepCalibration;
	}

	public void setStepCalibration(int stepCalibration) {
		this.stepCalibration = stepCalibration;
	}

	public int getDistanceCalibrationWalk() {
		return distanceCalibrationWalk;
	}

	public void setDistanceCalibrationWalk(int distanceCalibrationWalk) {
		this.distanceCalibrationWalk = distanceCalibrationWalk;
	}

	public int getDistanceCalibrationRun() {
		return distanceCalibrationRun;
	}

	public int getCaloriesCalibration() {
		return caloriesCalibration;
	}

	public void setCaloriesCalibration(int caloriesCalibration) {
		this.caloriesCalibration = caloriesCalibration;
	}

	public void setDistanceCalibrationRun(int distanceCalibrationRun) {
		this.distanceCalibrationRun = distanceCalibrationRun;
	}

	public int getAutoEL() {
		return autoEL;
	}

	public void setAutoEL(int autoEL) {
		this.autoEL = autoEL;
	}

	public Watch getWatch() {
		return watch;
	}

	public void setWatch(Watch watch) {
		this.watch = watch;
	}
	
	public static final Parcelable.Creator<BaseModel> CREATOR = new Parcelable.Creator<BaseModel>() {

		@Override
		public BaseModel createFromParcel(Parcel source) {
			return new CalibrationData(source);
		}

		@Override
		public BaseModel[] newArray(int size) {
			return new CalibrationData[size];
		}
	};
	
}
