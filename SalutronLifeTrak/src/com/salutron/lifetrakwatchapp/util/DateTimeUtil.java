package com.salutron.lifetrakwatchapp.util;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.salutron.lifetrakwatchapp.LifeTrakApplication;

public class DateTimeUtil {
	/**
	 * getTime in correct format based on settings
	 * 
	 * @param strTime
	 *            - param time e.g. 1:00 PM or 13:00
	 * 
	 * @return string Time in correct format e.g. 1:00 PM or 13:00
	 * 
	 * */
	public static String getTimeCorrectFormat(String strTime, LifeTrakApplication lifetrakApp) {
		if (strTime.isEmpty()) {
			return "";
		}
		DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
		if (strTime.toLowerCase().contains("am") || strTime.toLowerCase().contains("pm")) {
			dateFormat = new SimpleDateFormat("hh:mm a");
		} else {
			dateFormat = new SimpleDateFormat("HH:mm");
		}
		Date date = null;
		try {
			date = dateFormat.parse(strTime);
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
		if (lifetrakApp.getTimeDate().getHourFormat() == SalutronLifeTrakUtility.TIME_FORMAT_24_HR) {
			dateFormat = new SimpleDateFormat("HH:mm");
		} else {
			dateFormat = new SimpleDateFormat("hh:mm a");
		}

		return dateFormat.format(date);
	}

	/**
	 * Convert Time to Date
	 * 
	 * @param strTime
	 *            - param time e.g. 1:00 PM or 13:00
	 * 
	 * @return Date in correct format e.g. 1:00 PM or 13:00
	 * 
	 * */
	public static Date convertTimeToDate(String strTime, LifeTrakApplication lifetrakApp) {
		if (strTime.isEmpty()) {
			return new Date();
		}
		DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
		if (lifetrakApp.getTimeDate().getHourFormat() == SalutronLifeTrakUtility.TIME_FORMAT_24_HR) {
			dateFormat = new SimpleDateFormat("HH:mm");
		} else {
			dateFormat = new SimpleDateFormat("hh:mm a");
		}
		Date date = null;
		try {
			date = dateFormat.parse(strTime);
			adjustDateToNow(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return new Date();
		}

		return date;
	}

	/**
	 * @param isShortWeekdays - set true if short weekday display else false
	 * @return returns currentDay
	 **/
	public static String getCurrentDay(boolean isShortWeekdays) {
		Calendar c = Calendar.getInstance();
		Date now = new Date(c.getTimeInMillis());

		if (isShortWeekdays) {
			return String.format("%ta", now);
		} else {
			return String.format("%tA", now);
		}

	}
	
	@SuppressWarnings("deprecation")
	private static void adjustDateToNow(Date date) {
		Date now = new Date();
		date.setDate(now.getDate());
		date.setMonth(now.getMonth());
		date.setYear(now.getYear());
	}

    public static Date convertTimeToDate(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        return calendar.getTime();
    }

}
