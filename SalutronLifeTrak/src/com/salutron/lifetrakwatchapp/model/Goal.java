package com.salutron.lifetrakwatchapp.model;

import java.util.Date;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.salutron.lifetrakwatchapp.annotation.DataTable;
import com.salutron.lifetrakwatchapp.annotation.DataColumn;

@DataTable(name="Goal")
public class Goal extends BaseModel {
	@DataColumn(name="stepGoal")
	private long stepGoal;
	@DataColumn(name="distanceGoal")
	private double distanceGoal;
	@DataColumn(name="calorieGoal")
	private long calorieGoal;
	@DataColumn(name="sleepGoal")
	private int sleepGoal;
	@DataColumn(name="brightLightGoal")
	private int brightLightGoal;
	@DataColumn(name="date")
	private Date date;
	@DataColumn(name="dateStampDay")
	private int dateStampDay;
	@DataColumn(name="dateStampMonth")
	private int dateStampMonth;
	@DataColumn(name="dateStampYear")
	private int dateStampYear;
	@DataColumn(name="watchGoal", isPrimary=true)
	private Watch watch;
	
	public Goal() { }
	
	public Goal(Context context) {
		super(context);
	}
	
	public Goal(Parcel source) {
		readFromParcel(source);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(stepGoal);
		dest.writeDouble(distanceGoal);
		dest.writeLong(calorieGoal);
		dest.writeInt(sleepGoal);
		dest.writeInt(brightLightGoal);
		dest.writeLong(date.getTime());
		dest.writeInt(dateStampDay);
		dest.writeInt(dateStampMonth);
		dest.writeInt(dateStampYear);
		dest.writeParcelable(watch, flags);
	}

	@Override
	public void readFromParcel(Parcel source) {
		id = source.readLong();
		stepGoal = source.readLong();
		distanceGoal = source.readDouble();
		calorieGoal = source.readLong();
		sleepGoal = source.readInt();
		brightLightGoal = source.readInt();
		date = new Date(source.readLong());
		dateStampDay = source.readInt();
		dateStampMonth = source.readInt();
		dateStampYear = source.readInt();
		watch = source.readParcelable(Watch.class.getClassLoader());
	}

	public long getStepGoal() {
		return stepGoal;
	}

	public void setStepGoal(long stepGoal) {
		this.stepGoal = stepGoal;
	}

	public double getDistanceGoal() {
		return distanceGoal;
	}

	public void setDistanceGoal(double distanceGoal) {
		this.distanceGoal = distanceGoal;
	}

	public long getCalorieGoal() {
		return calorieGoal;
	}

	public void setCalorieGoal(long calorieGoal) {
		this.calorieGoal = calorieGoal;
	}

	public int getSleepGoal() {
		return sleepGoal;
	}

	public void setSleepGoal(int sleepGoal) {
		this.sleepGoal = sleepGoal;
	}
	
	public int getBrightLightGoal() {
		return brightLightGoal;
	}

	public void setBrightLightGoal(int brightLightGoal) {
		this.brightLightGoal = brightLightGoal;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getDateStampDay() {
		return dateStampDay;
	}

	public void setDateStampDay(int dateStampDay) {
		this.dateStampDay = dateStampDay;
	}

	public int getDateStampMonth() {
		return dateStampMonth;
	}

	public void setDateStampMonth(int dateStampMonth) {
		this.dateStampMonth = dateStampMonth;
	}

	public int getDateStampYear() {
		return dateStampYear;
	}

	public void setDateStampYear(int dateStampYear) {
		this.dateStampYear = dateStampYear;
	}

	public Watch getWatch() {
		return watch;
	}

	public void setWatch(Watch watch) {
		this.watch = watch;
	}
	
	@Override
	public String toString() {
		return String.format("step:%d, distance:%f, calories:%d",  stepGoal, distanceGoal, calorieGoal);
	}
	
	public static final Parcelable.Creator<BaseModel> CREATOR = new Parcelable.Creator<BaseModel>() {

		@Override
		public BaseModel createFromParcel(Parcel source) {
			return new Goal(source);
		}

		@Override
		public BaseModel[] newArray(int size) {
			return new Goal[size];
		}
	};
}
