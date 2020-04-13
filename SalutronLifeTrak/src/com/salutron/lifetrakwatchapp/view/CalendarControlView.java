package com.salutron.lifetrakwatchapp.view;

import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Nullable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.LinearLayout;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.adapter.CalendarControlMonthAdapter;
import com.salutron.lifetrakwatchapp.adapter.CalendarControlMonthAdapter.CalendarDate;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;
import com.salutron.lifetrakwatchapp.util.CalendarDateChangeListener;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Iterables;

public class CalendarControlView extends ScrollView 
									implements SalutronLifeTrakUtility, CalendarControlMonthView.OnDateSelectedListener {
	private LinearLayout mCalendarContainer;
	private int mCalendarMode;
	private LifeTrakApplication mLifeTrakApplication;
	private CalendarDateChangeListener mDateChangeListener;
	private final List<CalendarControlMonthView> mMonthViews = new ArrayList<CalendarControlMonthView>();
	private Date previousDate;
	private Date previousDateFrom;
	private Date previousDateTo;

	public CalendarControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.view_calendar_control, this, true);
	}
	
	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		initializeObjects();
	}
	
	@Override
	public void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		
		View view = mCalendarContainer.getChildAt(mCalendarContainer.getChildCount() - 1);
		int diff = view.getBottom() - (getMeasuredHeight() + getScrollY());
		
		if (t == 0) {
			addMonthViewAtTop();
			scrollTo(0, (int)dpToPx(10));
		} else if(diff == 0) {
			addMonthViewAtBottom();
		}
	}
	
	private void initializeObjects() {
		mLifeTrakApplication = (LifeTrakApplication) getContext().getApplicationContext();
		mCalendarContainer = (LinearLayout) findViewById(R.id.lnrCalendarContainer);
		mMonthViews.clear();
	}
	
	private List<CalendarControlMonthAdapter.CalendarDate> retrieveCalendarDates(Calendar calendar) {
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;
		
		List<CalendarControlMonthAdapter.CalendarDate> syncedDates = new ArrayList<CalendarControlMonthAdapter.CalendarDate>();
		
		if (mLifeTrakApplication.getSelectedWatch() != null) {
			List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(getContext())
					.getReadOperation()
					.query("watchDataHeader = ? and dateStampMonth = ? and dateStampYear = ?", 
							String.valueOf(mLifeTrakApplication.getSelectedWatch().getId()), String.valueOf(month), String.valueOf(year))
					.getResults(StatisticalDataHeader.class);
			
			for (StatisticalDataHeader dataHeader : dataHeaders) {
				CalendarControlMonthAdapter.CalendarDate calendarDate = new CalendarControlMonthAdapter.CalendarDate();
				calendarDate.setDay(dataHeader.getDateStampDay());
				calendarDate.setMonth(dataHeader.getDateStampMonth() - 1);
				calendarDate.setYear(dataHeader.getDateStampYear() + 1900);
				syncedDates.add(calendarDate);
			}
		}
		
		
		return syncedDates;
	}
	
	public void addMonthView(Calendar from, Calendar to) {
		from.set(Calendar.SECOND, 0);
		from.set(Calendar.MINUTE, 0);
		from.set(Calendar.HOUR, 0);
		to.set(Calendar.SECOND, 0);
		to.set(Calendar.MINUTE, 0);
		to.set(Calendar.HOUR, 0);
		
		int index = 0;
		
		Calendar calendarNow = Calendar.getInstance();
		calendarNow.setTime(new Date());
		
		
		while (from.getTime().before(to.getTime()) || from.getTime().equals(to.getTime())) {
			CalendarControlMonthView monthView = new CalendarControlMonthView(getContext());
			
			monthView.setSyncedDates(retrieveCalendarDates(from));
			monthView.setCalendar(from);
			monthView.setOnDateSelectedListener(this);
			monthView.setIndex(index);
			mCalendarContainer.addView(monthView);
			
			mMonthViews.add(monthView);
			
			from.add(Calendar.MONTH, 1);
			index++;
		}
	}
	
	private void addMonthViewAtTop() {
		CalendarControlMonthView monthViewFirst = (CalendarControlMonthView) mCalendarContainer.getChildAt(0);
		CalendarControlMonthView monthView = new CalendarControlMonthView(getContext());
		
		Calendar calendar = monthViewFirst.getCalendarDateFrom().toCalendar();
		calendar.add(Calendar.MONTH, -1);
		
		monthView.setSyncedDates(retrieveCalendarDates(calendar));
		monthView.setCalendar(calendar);
		monthView.setOnDateSelectedListener(this);
		mCalendarContainer.addView(monthView, 0);
		mMonthViews.add(0, monthView);
	}
	
	private void addMonthViewAtBottom() {
		CalendarControlMonthView monthViewFirst = (CalendarControlMonthView) mCalendarContainer.getChildAt(mCalendarContainer.getChildCount() - 1);
		CalendarControlMonthView monthView = new CalendarControlMonthView(getContext());
		
		Calendar calendar = monthViewFirst.getCalendarDateFrom().toCalendar();
		calendar.add(Calendar.MONTH, 1);
		
		monthView.setSyncedDates(retrieveCalendarDates(calendar));
		monthView.setCalendar(calendar);
		monthView.setOnDateSelectedListener(this);
		mCalendarContainer.addView(monthView);
		mMonthViews.add(monthView);
	}
	
	public void scrollToCurrentCalendar(Date date) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		
		for (int i=0;i<mCalendarContainer.getChildCount();i++) {
			CalendarControlMonthView monthView = (CalendarControlMonthView) mCalendarContainer.getChildAt(i);
			if (monthView.getCalendarDateFrom().getMonth() == calendar.get(Calendar.MONTH) &&
					monthView.getCalendarDateFrom().getYear() == calendar.get(Calendar.YEAR)) {
				int top = monthView.getTop();
				scrollTo(0, top);
				break;
			}
		}
	}

	@Override
	public void onDateSelected(CalendarControlMonthView monthView, CalendarDate calendarDate) {
		Calendar calendar = Calendar.getInstance();
		
		calendar.set(Calendar.DAY_OF_MONTH, calendarDate.getDay());
		calendar.set(Calendar.MONTH, calendarDate.getMonth());
		calendar.set(Calendar.YEAR, calendarDate.getYear());
		
		changeApplicationDate(mCalendarMode, calendar.getTime());
	}
	
	private void changeApplicationDate(int calendarMode, Date date) {
		GregorianCalendar calendar = new GregorianCalendar();
		Calendar calendarFrom = Calendar.getInstance();
		Calendar calendarTo = Calendar.getInstance();
		
		calendar.setTime(date);
		calendarFrom.setTime(calendar.getTime());
		
		switch(mCalendarMode) {
		case MODE_DAY:
			clearPreviousDate();
			
			mLifeTrakApplication.setCurrentDate(calendar.getTime());
			previousDate = calendar.getTime();
			
			if (mDateChangeListener != null)
				mDateChangeListener.onCalendarDateChange(calendar.getTime());
			selectDate(calendar.getTime());
			break;
		case MODE_WEEK:
			calendarFrom.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
			calendarTo.setTime(calendarFrom.getTime());
			calendarTo.add(Calendar.DAY_OF_MONTH, 6);
			
			if (previousDateFrom != null && previousDateTo != null) {
				clearSelectableDates(previousDateFrom, previousDateTo);
			}
			
			mLifeTrakApplication.setDateRangeFrom(calendarFrom.getTime());
			mLifeTrakApplication.setDateRangeTo(calendarTo.getTime());
			
			previousDateFrom = mLifeTrakApplication.getDateRangeFrom();
			previousDateTo = mLifeTrakApplication.getDateRangeTo();
			
			if (mDateChangeListener != null)
				mDateChangeListener.onCalendarWeekChange(calendarFrom.getTime(), calendarTo.getTime());
			
			selectDates(calendarFrom.getTime(), calendarTo.getTime());
			break;
		case MODE_MONTH:
			calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
			calendarTo.setTime(calendarFrom.getTime());
			calendarTo.set(Calendar.DAY_OF_MONTH, calendarFrom.getActualMaximum(Calendar.DAY_OF_MONTH));
			
			if (previousDateFrom != null && previousDateTo != null) {
				clearSelectableDates(previousDateFrom, previousDateTo);
			}
			
			mLifeTrakApplication.setDateRangeFrom(calendarFrom.getTime());
			mLifeTrakApplication.setDateRangeTo(calendarTo.getTime());
			
			previousDateFrom = mLifeTrakApplication.getDateRangeFrom();
			previousDateTo = mLifeTrakApplication.getDateRangeTo();
			
			if (mDateChangeListener != null)
				mDateChangeListener.onCalendarMonthChange(calendarFrom.getTime(), calendarTo.getTime());
			
			selectDates(calendarFrom.getTime(), calendarTo.getTime());
			break;
		}
	}
	
	public void setCalendarMode(int calendarMode) {
		mCalendarMode = calendarMode;
		changeApplicationDate(calendarMode, mLifeTrakApplication.getCurrentDate());
	}
	
	public void setDateChangeListener(CalendarDateChangeListener listener) {
		mDateChangeListener = listener;
	}
	
	public void selectDate(final Date date) {
		if (mMonthViews.size() > 0) {
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			
			CalendarDate calendarDate = CalendarDate.from(calendar);
			int index = indexOfMonthView(date);
			
			CalendarControlMonthView monthView = (CalendarControlMonthView) mCalendarContainer.getChildAt(index);
			
			if (monthView != null) {
				monthView.clearSelectable();
				monthView.setSelectedDates(Lists.newArrayList(calendarDate));
			}
		}
	}
	
	public void selectDates(final Date from, final Date to) {
		List<CalendarDate> calendarDates = Lists.newArrayList();
		List<Integer> indexes = new ArrayList<Integer>();
		GregorianCalendar calendarFrom = new GregorianCalendar();
		calendarFrom.setTime(from);
		
		while (calendarFrom.getTime().before(to) || calendarFrom.getTime().equals(to)) {
			calendarDates.add(CalendarDate.from(calendarFrom));
			
			int index = indexOfMonthView(calendarFrom.getTime());
			
			if (!indexes.contains(index)) {
				indexes.add(index);
			}
			
			calendarFrom.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		for (Integer index : indexes) {
			CalendarControlMonthView monthView = (CalendarControlMonthView) mCalendarContainer.getChildAt(index);
			
			if (monthView != null) {
				monthView.clearSelectable();
				monthView.setSelectedDates(calendarDates);
			}
		}
	}
	
	private void clearSelectableDate(Date date) {
		int index = indexOfMonthView(date);
		CalendarControlMonthView monthView = (CalendarControlMonthView) mCalendarContainer.getChildAt(index);
		
		if (monthView != null) {
			monthView.clearSelectable();
			monthView.updateDates();
		}
	}
	
	public void clearPreviousDate() {
		if (previousDate != null) {
			clearSelectableDate(previousDate);
		}
	}
	
	private void clearSelectableDates(Date from, Date to) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(from);
		
		List<Integer> indexes = new ArrayList<Integer>();
		
		while (calendar.getTime().before(to) || calendar.getTime().equals(to)) {
			int index = indexOfMonthView(calendar.getTime());
			
			if (!indexes.contains(index)) {
				indexes.add(index);
			}
			
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		for (Integer index : indexes) {
			CalendarControlMonthView monthView = (CalendarControlMonthView) mCalendarContainer.getChildAt(index);
			
			if (monthView != null) {
				monthView.clearSelectable();
				monthView.updateDates();
			}
		}
	}
	
	public void addSyncedDate(Date date) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		
		CalendarDate calendarDate = CalendarDate.from(calendar);
		int index = indexOfMonthView(date);
		
		CalendarControlMonthView monthView = (CalendarControlMonthView) mCalendarContainer.getChildAt(index);
		
		if (monthView != null) {
			monthView.addSyncedDate(calendarDate);
			monthView.updateDates();
		}
	}
	
	public void addSyncedDates(Date from, Date to) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(from);
		
		List<Integer> indexes = new ArrayList<Integer>();
		
		while (calendar.getTime().before(to) || calendar.getTime().equals(to)) {
			int index = indexOfMonthView(calendar.getTime());
			
			CalendarControlMonthView monthView = (CalendarControlMonthView) mCalendarContainer.getChildAt(index);
			CalendarDate calendarDate = CalendarDate.from(calendar);
			
			if (monthView != null) {
				monthView.addSyncedDate(calendarDate);
			}
			
			if (!indexes.contains(index)) {
				indexes.add(index);
			}
			
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		for (int index : indexes) {
			CalendarControlMonthView monthView = (CalendarControlMonthView) mCalendarContainer.getChildAt(index);
			
			if (monthView != null) {
				monthView.updateDates();
			}
		}
	}
	
	private int indexOfMonthView(final Date date) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		
		final CalendarDate calendarDate = CalendarDate.from(calendar);
		
		int index = Iterables.indexOf(mMonthViews, new Predicate<CalendarControlMonthView>() {
			@Override
			public boolean apply(@Nullable CalendarControlMonthView monthView) {
				return monthView.getCalendarDateFrom().getMonth() == calendarDate.getMonth() &&
						monthView.getCalendarDateFrom().getYear() == calendarDate.getYear();
			}
		});
		
		return index;
	}
	
	private float dpToPx(int dp) {
		return dp * (getResources().getDisplayMetrics().densityDpi / 160.0f);
	}
}