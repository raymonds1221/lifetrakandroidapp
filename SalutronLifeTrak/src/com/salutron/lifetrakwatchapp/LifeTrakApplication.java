package com.salutron.lifetrakwatchapp;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Application;
import android.os.Environment;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.salutron.lifetrakwatchapp.adapter.GoalItem;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.Watch;

public class LifeTrakApplication extends Application {

	public static final File PUBLIC_DIR = Environment.getExternalStoragePublicDirectory("com.salutron.lifetrakwatchapp");

	public static GoalItem goalItem;

	private int mYear;
	private Watch selectedWatch;
	private Date currentDate;
	private Date dateRangeFrom;
	private Date dateRangeTo;
	private TimeDate timeDate;
	private UserProfile userProfile;

	public boolean isExternalStorageAvailable() {
		boolean isExternalStorageAvailable = false;
		boolean isExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			isExternalStorageAvailable = isExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			isExternalStorageAvailable = true;
			isExternalStorageWriteable = false;
		}

		return (isExternalStorageAvailable && isExternalStorageWriteable);
	}

	public Watch getSelectedWatch() {
		return selectedWatch;
	}

	public void setSelectedWatch(Watch selectedWatch) {
		this.selectedWatch = selectedWatch;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public Date getDateRangeFrom() {
		return dateRangeFrom;
	}

	public Date getDateRangeTo() {
		return dateRangeTo;
	}

	public Date getPreviousDay() {
		Date date = getCurrentDate();
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(date.getTime());
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date previousDay = calendar.getTime();
		setCurrentDate(previousDay);

		return previousDay;
	}

	public Date getNextDay() {
		Date date = getCurrentDate();
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(date.getTime());
		calendar.add(Calendar.DAY_OF_MONTH, 1);

		Date nextDay = calendar.getTime();
		setCurrentDate(nextDay);

		return nextDay;
	}

	public Date getLastWeekFrom() {
		Date date = getCurrentDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		calendar.add(Calendar.WEEK_OF_MONTH, -1);
		dateRangeFrom = calendar.getTime();
		return calendar.getTime();
	}

	public Date getLastWeekTo() {
		Date date = dateRangeFrom;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, 6);
		dateRangeTo = calendar.getTime();
		return calendar.getTime();
	}

	public Date getNextWeekFrom() {
		Date date = getCurrentDate();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);
		//calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		calendar.add(Calendar.WEEK_OF_MONTH, 1);
		dateRangeFrom = calendar.getTime();
		return calendar.getTime();
	}

	public Date getNextWeekTo() {
		Date date = dateRangeFrom;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, 6);
		dateRangeTo = calendar.getTime();
		return calendar.getTime();
	}

	public Date getLastMonthFrom() {
		Date date = getCurrentDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getFirstDayOfWeek());
		calendar.add(Calendar.MONTH, -1);
		dateRangeFrom = calendar.getTime();
		return calendar.getTime();
	}

	public Date getLastMonthTo() {
		Date date = dateRangeFrom;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.add(Calendar.DAY_OF_MONTH, maxDays);
		dateRangeTo = calendar.getTime();
		return calendar.getTime();
	}

	public Date getNextMonthFrom() {
		Date date = getCurrentDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getFirstDayOfWeek());
		calendar.add(Calendar.MONTH, 1);
		dateRangeFrom = calendar.getTime();
		return calendar.getTime();
	}

	public Date getNextMonthTo() {
		Date date = dateRangeFrom;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.add(Calendar.DAY_OF_MONTH, maxDays);
		dateRangeTo = calendar.getTime();
		return calendar.getTime();
	}

	public int getCurrentYear() {
		if (mYear == 0) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(getCurrentDate());
			mYear = calendar.get(Calendar.YEAR);
		}
		return mYear;
	}

	public int getLastYear() {
		mYear = getCurrentYear() - 1;
		return mYear;
	}

	public int getNextYear() {
		mYear = getCurrentYear() + 1;
		return mYear;
	}

	public TimeDate getTimeDate() {
		return timeDate;
	}

	public void setTimeDate(TimeDate timeDate) {
		this.timeDate = timeDate;
	}

	public void setCurrentYear(int year) {
		mYear = year;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public void setDateRangeFrom(Date dateRangeFrom) {
		this.dateRangeFrom = dateRangeFrom;
	}

	public void setDateRangeTo(Date dateRangeTo) {
		this.dateRangeTo = dateRangeTo;
	}

	public void clearDB() {
		//deleteDatabase(SalutronLifeTrakUtility.DATABASE_NAME);
	}

	@Override
	protected void attachBaseContext(Context context) {
		super.attachBaseContext(context);
		MultiDex.install(this);
	}
}
