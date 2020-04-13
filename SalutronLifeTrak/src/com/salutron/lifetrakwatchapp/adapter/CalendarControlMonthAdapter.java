package com.salutron.lifetrakwatchapp.adapter;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.annotation.Nullable;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.salutron.lifetrak.R;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class CalendarControlMonthAdapter extends BaseAdapter {
	private GregorianCalendar mCalendar;
	private GregorianCalendar mCalendar2;
	private int mFirstDay;
	private List<CalendarDate> mCalendarDates = new ArrayList<CalendarDate>();
	private List<CalendarDate> mSyncedDates = new ArrayList<CalendarDate>();
	private List<CalendarDate> mSelectedDates = new ArrayList<CalendarDate>();
	private Context mContext;
	
	public CalendarControlMonthAdapter(GregorianCalendar calendar) {
		mCalendar = calendar;
		mCalendar.set(GregorianCalendar.DAY_OF_MONTH, 1);
		initializeCalendar();
	}
	
	public CalendarControlMonthAdapter(Context context, GregorianCalendar calendar) {
		this(calendar);
		mContext = context;
	}

	@Override
	public int getCount() {
		return mCalendarDates.size();
	}

	@Override
	public Object getItem(int position) {
		return mCalendarDates.get(position);
	}
	
	public CalendarDate getCalendarDay(int position) {
		if (mCalendarDates != null) {
			return mCalendarDates.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup container) {
		View view = convertView;
		ViewHolder viewHolder;
		
		CalendarDate calendarDate = mCalendarDates.get(position);
		
		if (view == null) {
			view = LayoutInflater.from(mContext).inflate(R.layout.adapter_calendar_day, null);
			viewHolder = new ViewHolder();
			viewHolder.syncedIcon = (ImageView) view.findViewById(R.id.imgSyncedIcon);
			viewHolder.dayLabel = (TextView) view.findViewById(R.id.tvwDayLabel);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		if (calendarDate.getDay() > 1 && position < mFirstDay) {
			viewHolder.dayLabel.setTextColor(mContext.getResources().getColor(R.color.calendar_date_inactive));
		} else if(calendarDate.getDay() < 7 && position > 28) {
			viewHolder.dayLabel.setTextColor(mContext.getResources().getColor(R.color.calendar_date_inactive));
		} else {
			viewHolder.dayLabel.setTextColor(mContext.getResources().getColor(android.R.color.black));
		}
		
		viewHolder.dayLabel.setText(String.valueOf(calendarDate.getDay()));
		
		if (calendarDate.isSynced())
			viewHolder.syncedIcon.setVisibility(View.VISIBLE);
		else
			viewHolder.syncedIcon.setVisibility(View.GONE);
		
		if (calendarDate.isSelected()) {
			view.setBackgroundColor(mContext.getResources().getColor(R.color.calendar_date_selected));
		} else {
			view.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
		}
		
		return view;
	}
	
	private void initializeCalendar() {
		mFirstDay = mCalendar.get(GregorianCalendar.DAY_OF_WEEK);
		int maxWeekNumber = mCalendar.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);
		int monthLength = maxWeekNumber * 7;
		int maxPrevDay = getPreviousMonthMaxDays();
		int offDay = maxPrevDay - (mFirstDay - 1);
		
		GregorianCalendar calendar = (GregorianCalendar) mCalendar2.clone();
		calendar.set(GregorianCalendar.DAY_OF_MONTH, offDay + 1);
		
		mCalendarDates.clear();
		
		for (int i=0;i<monthLength;i++) {
			CalendarDate calendarDay = new CalendarDate();
			calendarDay.setDay(calendar.get(Calendar.DAY_OF_MONTH));
			calendarDay.setMonth(calendar.get(Calendar.MONTH));
			calendarDay.setYear(calendar.get(Calendar.YEAR));
			mCalendarDates.add(calendarDay);
			calendar.add(GregorianCalendar.DATE, 1);
		}
	}
	
	private int getPreviousMonthMaxDays() {
		int maxDays = 0;
		mCalendar2 = (GregorianCalendar) mCalendar.clone();
		
		mCalendar2.add(GregorianCalendar.MONTH, -1);
		
		maxDays = mCalendar2.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
		
		return maxDays;
	}
	
	public void setSyncedDates(List<CalendarDate> calendarDates) {
		mSyncedDates.clear();
		mSyncedDates.addAll(calendarDates);
		
		for (CalendarDate calendarDate : mCalendarDates) {
			if (mSyncedDates.contains(calendarDate))
				calendarDate.setSynced(true);
		}
	}
	
	public void setSelectedDates(List<CalendarDate> calendarDates) {
		mSelectedDates.clear();
		mSelectedDates.addAll(calendarDates);
		
		for (CalendarDate calendarDate : mCalendarDates) {
			if (mSelectedDates.contains(calendarDate))
				calendarDate.setSelected(true);
		}
	}
	
	public void clearSelectable() {
		for (CalendarDate calendarDate : mCalendarDates) {
			calendarDate.setSelected(false);
		}
	}
	
	private class ViewHolder {
		public ImageView syncedIcon;
		public TextView dayLabel;
	}
	
	public static final class CalendarDate {
		private int day;
		private int month;
		private int year;
		private boolean synced;
		private boolean selected;
		
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
		public boolean isSynced() {
			return synced;
		}
		public void setSynced(boolean synced) {
			this.synced = synced;
		}
		public boolean isSelected() {
			return selected;
		}
		public void setSelected(boolean selected) {
			this.selected = selected;
		}
		
		public static CalendarDate from(GregorianCalendar calendar) {
			CalendarDate calendarDate = new CalendarDate();
			calendarDate.setDay(calendar.get(Calendar.DAY_OF_MONTH));
			calendarDate.setMonth(calendar.get(Calendar.MONTH));
			calendarDate.setYear(calendar.get(Calendar.YEAR));
			return calendarDate;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o == this) return true;
			
			if (o instanceof CalendarDate) {
				CalendarDate calendarDate = (CalendarDate) o;
				return this.getDay() == calendarDate.getDay() && 
							this.getMonth() == calendarDate.getMonth() && 
							this.getYear() == calendarDate.getYear();
			}
			
			return false;
		}
		
		@Override
		public String toString() {
			return this.getDay() + "-" + this.getMonth() + "-" + this.getYear();
		}
		
		public int monthsBetween(CalendarDate calendarDate) {
			int months = 0;
			GregorianCalendar calendarFrom = this.toCalendar();
			GregorianCalendar calendarTo = calendarDate.toCalendar();
			
			calendarFrom.set(Calendar.SECOND, 0);
			calendarFrom.set(Calendar.MINUTE, 0);
			calendarFrom.set(Calendar.HOUR, 0);
			calendarTo.set(Calendar.SECOND, 0);
			calendarTo.set(Calendar.MINUTE, 0);
			calendarTo.set(Calendar.HOUR, 0);
			
			while (calendarFrom.getTime().before(calendarTo.getTime()) || 
					calendarFrom.getTime().equals(calendarTo.getTime())) {
				months++;
				calendarFrom.add(Calendar.MONTH, 1);
			}
			
			return months;
		}
		
		public GregorianCalendar toCalendar() {
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.set(Calendar.DAY_OF_MONTH, this.getDay());
			calendar.set(Calendar.MONTH, this.getMonth());
			calendar.set(Calendar.YEAR, this.getYear());
			return calendar;
		}
	}
	
	public CalendarDate getCalendarDateFrom() {
		return Iterables.find(mCalendarDates, new Predicate<CalendarDate>() {
			@Override
			public boolean apply(@Nullable CalendarDate calendarDate) {
				return calendarDate.getDay() == 1;
			}
		});
	}
	
	public CalendarDate getCalendarDateTo() {
		return mCalendarDates.get(mCalendarDates.size() - 1);
	}
}
