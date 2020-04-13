package com.salutron.lifetrakwatchapp.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;
import android.content.Context;

import com.salutron.lifetrakwatchapp.annotation.DataTable;
import com.salutron.lifetrakwatchapp.annotation.DataColumn;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.blesdk.SALSleepDatabase;

@DataTable(name="SleepDatabase")
public class SleepDatabase extends BaseModel {
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

	@DataColumn(name="syncedToCloud")
	private boolean syncedToCloud;

	@DataColumn(name="isWatch")
	private boolean isWatch;
	@DataColumn(name="isModified")
	private boolean isModified;
	
	// Derived fields
	private long startTime;
	private long endTime;

	public SleepDatabase() { }
	
	public SleepDatabase(Parcel source) { 
		readFromParcel(source);
	}
	
	public SleepDatabase(Context context) {
		super(context);
	}
	
	public static final SleepDatabase buildSleepDatabase(Context context, SALSleepDatabase salSleepDatabase) {
		SleepDatabase sleepDatabase = new SleepDatabase(context);
		
		sleepDatabase.setDateStampYear(salSleepDatabase.datestamp.nYear);
		sleepDatabase.setDateStampMonth(salSleepDatabase.datestamp.nMonth);
		sleepDatabase.setDateStampDay(salSleepDatabase.datestamp.nDay);
		sleepDatabase.setMinuteSleepStart(salSleepDatabase.minuteSleepStart);
		sleepDatabase.setHourSleepStart(salSleepDatabase.hourSleepStart);
		sleepDatabase.setMinuteSleepEnd(salSleepDatabase.minuteSleepEnd);
		sleepDatabase.setHourSleepEnd(salSleepDatabase.hourSleepEnd);
		sleepDatabase.setLapses(salSleepDatabase.lapses);
		sleepDatabase.setDeepSleepCount(salSleepDatabase.deepSleepCount);
		sleepDatabase.setLightSleepCount(salSleepDatabase.lightSleepCount);
		sleepDatabase.setSleepOffset(salSleepDatabase.sleepOffset);
		sleepDatabase.setExtraInfo(salSleepDatabase.extraInfo);
		sleepDatabase.setSleepDuration(salSleepDatabase.sleepDuration);
		
		return sleepDatabase;
	}
	
	public static final List<SleepDatabase> buildSleepDatabase(Context context, List<SALSleepDatabase> salSleepDatabases) {
		List<SleepDatabase> sleepDatabases = new ArrayList<SleepDatabase>();
		
		for (SALSleepDatabase salSleepDatabase : salSleepDatabases) {
			SleepDatabase sleepDatabase = SleepDatabase.buildSleepDatabase(context, salSleepDatabase);
			sleepDatabases.add(sleepDatabase);
		}
		
		return sleepDatabases;
	}

	public boolean isExists() {
		if (getLifeTrakApplication().getSelectedWatch() == null) {
			return false;
		}
		
		String query = "watchSleepDatabase = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ? and " +
							"minuteSleepStart = ? and hourSleepStart = ? and minuteSleepEnd = ? and hourSleepEnd = ?";
		
		List<SleepDatabase> sleepDatabases = DataSource.getInstance(mContext)
														.getReadOperation()
														.query(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
																String.valueOf(dateStampYear), String.valueOf(dateStampMonth), String.valueOf(dateStampDay),
																String.valueOf(minuteSleepStart), String.valueOf(hourSleepStart), String.valueOf(minuteSleepEnd), String.valueOf(hourSleepEnd))
														.getResults(SleepDatabase.class);
		
		if (sleepDatabases.size() > 0) {
			SleepDatabase sleepDatabase = sleepDatabases.get(0);
			this.id = sleepDatabase.getId();
			return true;
		}
		
		return false;
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
	
	public int getAdjustedEndMinutes() {
		int sleepEndMinutes = (hourSleepEnd * 60) + minuteSleepEnd;
		int extraInfo = (this.extraInfo&0xf0) >> 4;
		
		if(extraInfo == 1)
			sleepEndMinutes += 10;
		else if(extraInfo == 2)
			sleepEndMinutes += 20;
		else if(extraInfo == 3)
			sleepEndMinutes += 4;
		else if(extraInfo >= 4 && extraInfo <= 6)
			sleepEndMinutes += 60;
		
		if(sleepEndMinutes >= 24 * 60)
			sleepEndMinutes -= 24 * 60;
		
		return sleepEndMinutes;
	}

	/**
	 * Get start time of sleep
	 * @return start time in milliseconds
	 */
	public long getStartTime() {
		if (startTime == 0) {
			final Calendar calendar = Calendar.getInstance();
			calendar.set(dateStampYear + 1900, dateStampMonth - 1, dateStampDay,
					hourSleepStart, minuteSleepStart, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			startTime = calendar.getTimeInMillis();
		}
		return startTime;
	}

	/**
	 * Determine whether end time falls on the next day or not
	 * @return true if end time is on next day, false if on same day
	 */
	private boolean isEndTimeOnNextDay() {
		return 60 * hourSleepStart + minuteSleepStart > 60 * hourSleepEnd + minuteSleepEnd;
	}

	/**
	 * Get end time of sleep
	 * @return end time in milliseconds
	 */
	public long getEndTime() {
		if (endTime == 0) {
			final Calendar calendar = Calendar.getInstance();
			calendar.set(dateStampYear + 1900, dateStampMonth - 1, dateStampDay,
					hourSleepEnd, minuteSleepEnd, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			if (isEndTimeOnNextDay()) {
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			endTime = calendar.getTimeInMillis();
		}
		return endTime;
	}
	
	public static final Parcelable.Creator<BaseModel> CREATOR = new Parcelable.Creator<BaseModel>() {

		@Override
		public BaseModel createFromParcel(Parcel arg0) {
			return new SleepDatabase(arg0);
		}

		@Override
		public BaseModel[] newArray(int arg0) {
			return new SleepDatabase[arg0];
		}
	};

	public boolean isSyncedToCloud() {
		return syncedToCloud;
	}

	public void setSyncedToCloud(boolean syncedToCloud) {
		this.syncedToCloud = syncedToCloud;
	}

	public boolean isModified() {
		return isModified;
	}

	public void setIsModified(boolean isModified) {
		this.isModified = isModified;
	}

	public boolean isWatch(){
		return isWatch;
	}

	public void setIsWatch(boolean isWatch){
		this.isWatch = isWatch;
	}

}
