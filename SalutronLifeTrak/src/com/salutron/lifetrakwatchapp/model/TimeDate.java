package com.salutron.lifetrakwatchapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.content.Context;

import com.salutron.blesdk.SALTimeDate;
import com.salutron.lifetrakwatchapp.annotation.*;

@DataTable(name="TimeDate")
public class TimeDate extends BaseModel {
	@DataColumn(name="second")
	private int second;
	@DataColumn(name="minute")
	private int minute;
	@DataColumn(name="hour")
	private int hour;
	@DataColumn(name="day")
	private int day;
	@DataColumn(name="month")
	private int month;
	@DataColumn(name="year")
	private int year;
	@DataColumn(name="hourFormat")
	private int hourFormat;
	@DataColumn(name="dateFormat")
	private int dateFormat;
	@DataColumn(name="watchTimeDate", isPrimary=true)
	private Watch watch;
	@DataColumn(name="displaySize")
	private int displaySize;
	
	public TimeDate() { }
	
	public TimeDate(Parcel source) {
		readFromParcel(source);
	}
	
	TimeDate(Context context) {
		super(context);
	}
	
	public static final TimeDate buildTimeDate(Context context, SALTimeDate salTimeDate) {
		TimeDate timeDate = new TimeDate(context);
		
		timeDate.setSecond(salTimeDate.getSecond());
		timeDate.setMinute(salTimeDate.getMinute());
		timeDate.setHour(salTimeDate.getHour());
		timeDate.setDay(salTimeDate.getDay());
		timeDate.setMonth(salTimeDate.getMonth());
		timeDate.setYear(salTimeDate.getYear());
		timeDate.setHourFormat(salTimeDate.getHourFormat());
		timeDate.setDateFormat(salTimeDate.getDateFormat());
		timeDate.setDisplaySize(salTimeDate.getTimeDisplaySize());
		
		return timeDate;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeInt(second);
		dest.writeInt(minute);
		dest.writeInt(hour);
		dest.writeInt(day);
		dest.writeInt(month);
		dest.writeInt(year);
		dest.writeInt(hourFormat);
		dest.writeInt(dateFormat);
	}

	@Override
	public void readFromParcel(Parcel source) {
		id = source.readLong();
		second = source.readInt();
		minute = source.readInt();
		hour = source.readInt();
		day = source.readInt();
		month = source.readInt();
		year = source.readInt();
		hourFormat = source.readInt();
		dateFormat = source.readInt();
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

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getHourFormat() {
		return hourFormat;
	}

	public void setHourFormat(int hourFormat) {
		this.hourFormat = hourFormat;
	}

	public int getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(int dateFormat) {
		this.dateFormat = dateFormat;
	}

	public Watch getWatch() {
		return watch;
	}

	public void setWatch(Watch watch) {
		this.watch = watch;
	}
	
	public int getDisplaySize() {
		return displaySize;
	}

	public void setDisplaySize(int displaySize) {
		this.displaySize = displaySize;
	}

	public static final Parcelable.Creator<BaseModel> CREATOR = new Parcelable.Creator<BaseModel>() {

		@Override
		public BaseModel createFromParcel(Parcel arg0) {
			return new TimeDate(arg0);
		}

		@Override
		public BaseModel[] newArray(int arg0) {
			return new TimeDate[arg0];
		}
	};
}
