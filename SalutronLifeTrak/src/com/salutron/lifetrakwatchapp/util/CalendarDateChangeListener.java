package com.salutron.lifetrakwatchapp.util;

import java.util.Date;

public interface CalendarDateChangeListener {
	public void onCalendarDateChange(Date date);
	public void onCalendarWeekChange(Date from, Date to);
	public void onCalendarMonthChange(Date from, Date to);
	public void onCalendarYearChange(int year);
}
