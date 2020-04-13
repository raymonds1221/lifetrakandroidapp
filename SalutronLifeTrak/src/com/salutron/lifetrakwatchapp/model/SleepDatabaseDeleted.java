package com.salutron.lifetrakwatchapp.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.salutron.lifetrakwatchapp.annotation.DataColumn;
import com.salutron.lifetrakwatchapp.annotation.DataTable;

@DataTable(name="SleepDatabaseDeleted")
public class SleepDatabaseDeleted extends BaseModel {
	@DataColumn(name="dateStampYear")
	private int dateStampYear;
	@DataColumn(name="dateStampMonth")
	private int dateStampMonth;
	@DataColumn(name="dateStampDay")
	private int dateStampDay;
	@DataColumn(name="minuteSleepStart")
	private int minuteSleepStart;
	@DataColumn(name="hourSleepStart")
	private int hourSleepStart;
	@DataColumn(name="minuteSleepEnd")
	private int minuteSleepEnd;
	@DataColumn(name="hourSleepEnd")
	private int hourSleepEnd;
	@DataColumn(name="lapses")
	private int lapses;
	@DataColumn(name="deepSleepCount")
	private int deepSleepCount;
	@DataColumn(name="lightSleepCount")
	private int lightSleepCount;
	@DataColumn(name="sleepOffset")
	private int sleepOffset;
	@DataColumn(name="extraInfo")
	private int extraInfo;
	@DataColumn(name="sleepDuration")
	private int sleepDuration;
	@DataColumn(name="reserve")
	private int reserve;
	@DataColumn(name="watchSleepDatabase", isPrimary=true)
	private Watch watch;
	
	public SleepDatabaseDeleted(Context context) {
		super(context);
	}
	
	public SleepDatabaseDeleted() { }
	
	public SleepDatabaseDeleted(Parcel source) { 
		readFromParcel(source);
	}
	
	public static final SleepDatabaseDeleted buildSleepDatabase(Context context, SleepDatabase sleepDatabase) {
		SleepDatabaseDeleted sleepDatabaseDeleted = new SleepDatabaseDeleted(context);
		
		sleepDatabaseDeleted.setDateStampYear(sleepDatabase.getDateStampYear());
		sleepDatabaseDeleted.setDateStampMonth(sleepDatabase.getDateStampMonth());
		sleepDatabaseDeleted.setDateStampDay(sleepDatabase.getDateStampDay());
		sleepDatabaseDeleted.setMinuteSleepStart(sleepDatabase.getMinuteSleepStart());
		sleepDatabaseDeleted.setHourSleepStart(sleepDatabase.getHourSleepStart());
		sleepDatabaseDeleted.setMinuteSleepEnd(sleepDatabase.getMinuteSleepEnd());
		sleepDatabaseDeleted.setHourSleepEnd(sleepDatabase.getHourSleepEnd());
		sleepDatabaseDeleted.setLapses(sleepDatabase.getLapses());
		sleepDatabaseDeleted.setDeepSleepCount(sleepDatabase.getDeepSleepCount());
		sleepDatabaseDeleted.setLightSleepCount(sleepDatabase.getLightSleepCount());
		sleepDatabaseDeleted.setSleepOffset(sleepDatabase.getSleepOffset());
		sleepDatabaseDeleted.setExtraInfo(sleepDatabase.getExtraInfo());
		sleepDatabaseDeleted.setSleepDuration(sleepDatabase.getSleepDuration());
		
		return sleepDatabaseDeleted;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeInt(dateStampYear);
		dest.writeInt(dateStampMonth);
		dest.writeInt(dateStampDay);
		dest.writeInt(minuteSleepStart);
		dest.writeInt(hourSleepStart);
		dest.writeInt(minuteSleepEnd);
		dest.writeInt(hourSleepEnd);
		dest.writeInt(lapses);
		dest.writeInt(deepSleepCount);
		dest.writeInt(lightSleepCount);
		dest.writeInt(sleepOffset);
		dest.writeInt(extraInfo);
		dest.writeInt(sleepDuration);
		dest.writeInt(reserve);
		dest.writeParcelable(watch, 0);
	}

	@Override
	public void readFromParcel(Parcel source) {
		id = source.readLong();
		dateStampYear = source.readInt();
		dateStampMonth = source.readInt();
		dateStampDay = source.readInt();
		minuteSleepStart = source.readInt();
		hourSleepStart = source.readInt();
		minuteSleepEnd = source.readInt();
		hourSleepEnd = source.readInt();
		lapses = source.readInt();
		deepSleepCount = source.readInt();
		lightSleepCount = source.readInt();
		sleepOffset = source.readInt();
		extraInfo = source.readInt();
		sleepDuration = source.readInt();
		reserve = source.readInt();
		watch = (Watch) source.readParcelable(Watch.class.getClassLoader());
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

	public int getMinuteSleepStart() {
		return minuteSleepStart;
	}

	public void setMinuteSleepStart(int minuteSleepStart) {
		this.minuteSleepStart = minuteSleepStart;
	}

	public int getHourSleepStart() {
		return hourSleepStart;
	}

	public void setHourSleepStart(int hourSleepStart) {
		this.hourSleepStart = hourSleepStart;
	}

	public int getMinuteSleepEnd() {
		return minuteSleepEnd;
	}

	public void setMinuteSleepEnd(int minuteSleepEnd) {
		this.minuteSleepEnd = minuteSleepEnd;
	}

	public int getHourSleepEnd() {
		return hourSleepEnd;
	}

	public void setHourSleepEnd(int hourSleepEnd) {
		this.hourSleepEnd = hourSleepEnd;
	}

	public int getLapses() {
		return lapses;
	}

	public void setLapses(int lapses) {
		this.lapses = lapses;
	}

	public int getDeepSleepCount() {
		return deepSleepCount;
	}

	public void setDeepSleepCount(int deepSleepCount) {
		this.deepSleepCount = deepSleepCount;
	}

	public int getLightSleepCount() {
		return lightSleepCount;
	}

	public void setLightSleepCount(int lightSleepCount) {
		this.lightSleepCount = lightSleepCount;
	}

	public int getSleepOffset() {
		return sleepOffset;
	}

	public void setSleepOffset(int sleepOffset) {
		this.sleepOffset = sleepOffset;
	}

	public int getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(int extraInfo) {
		this.extraInfo = extraInfo;
	}

	public int getSleepDuration() {
		return sleepDuration;
	}

	public void setSleepDuration(int sleepDuration) {
		this.sleepDuration = sleepDuration;
	}

	public int getReserve() {
		return reserve;
	}

	public void setReserve(int reserve) {
		this.reserve = reserve;
	}

	public Watch getWatch() {
		return watch;
	}

	public void setWatch(Watch watch) {
		this.watch = watch;
	}
	
	public static final Parcelable.Creator<BaseModel> CREATOR = new Parcelable.Creator<BaseModel>() {

		@Override
		public BaseModel createFromParcel(Parcel arg0) {
			return new SleepDatabaseDeleted(arg0);
		}

		@Override
		public BaseModel[] newArray(int arg0) {
			return new SleepDatabaseDeleted[arg0];
		}
	};
}
