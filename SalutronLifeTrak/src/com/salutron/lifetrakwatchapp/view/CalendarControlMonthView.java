package com.salutron.lifetrakwatchapp.view;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.GridView;
import android.widget.AdapterView;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.adapter.CalendarControlMonthAdapter;

public class CalendarControlMonthView extends LinearLayout 
										implements AdapterView.OnItemClickListener {
	private TextView mMonthLabel;
	private GridView mCalendarMonthGrid;
	private CalendarControlMonthAdapter mAdapter;
	private Calendar mCalendar;
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private OnDateSelectedListener mDateSelectedListener;
	private List<CalendarControlMonthAdapter.CalendarDate> mSyncDates = new ArrayList<CalendarControlMonthAdapter.CalendarDate>();
	private List<CalendarControlMonthAdapter.CalendarDate> mSelectedDates = new ArrayList<CalendarControlMonthAdapter.CalendarDate>();
	private int mIndex;
	
	public CalendarControlMonthView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_calendar_control_month, this, true);
		initializeObjects();
	}

	public CalendarControlMonthView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.view_calendar_control_month, this, true);
		initializeObjects();
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mDateSelectedListener != null) {
			CalendarControlMonthAdapter.CalendarDate calendarDate = mAdapter.getCalendarDay(position);
			mDateSelectedListener.onDateSelected(this, calendarDate);
		}
	}
	
	private void initializeObjects() {
		mMonthLabel = (TextView) findViewById(R.id.tvwMonth);
		mCalendarMonthGrid = (GridView) findViewById(R.id.grdCalendar);
		
		mDateFormat.applyPattern("MMMM yyyy");
	}
	
	private void refreshDates() {
		if (mCalendar != null) {
			mMonthLabel.setText(mDateFormat.format(mCalendar.getTime()));
			mAdapter = new CalendarControlMonthAdapter(getContext(), (GregorianCalendar) mCalendar);
			mAdapter.setSyncedDates(mSyncDates);
			mCalendarMonthGrid.setAdapter(mAdapter);
			mCalendarMonthGrid.setOnItemClickListener(this);
		}
	}
	
	public void setCalendar(Calendar calendar) {
		mCalendar = calendar;
		refreshDates();
	}
	
	public void setSyncedDates(List<CalendarControlMonthAdapter.CalendarDate> syncedDates) {
		mSyncDates.clear();
		mSyncDates.addAll(syncedDates);
	}
	
	public void addSyncedDate(CalendarControlMonthAdapter.CalendarDate syncedDate) {
		mSyncDates.add(syncedDate);
		mAdapter.setSyncedDates(mSyncDates);
	}
	
	public void setSelectedDates(List<CalendarControlMonthAdapter.CalendarDate> selectedDates) {
		mSelectedDates.clear();
		mSelectedDates.addAll(selectedDates);
		
		clearSelectable();
		
		mAdapter.setSelectedDates(mSelectedDates);
		updateDates();
	}
	
	public void setOnDateSelectedListener(OnDateSelectedListener listener) {
		mDateSelectedListener = listener;
	}
	
	public int getIndex() {
		return mIndex;
	}
	
	public void setIndex(int index) {
		mIndex = index;
	}
	
	public void clearSelectable() {
		mAdapter.clearSelectable();
	}
	
	public void updateDates() {
		mAdapter.notifyDataSetChanged();
	}
	
	public static interface OnDateSelectedListener {
		public void onDateSelected(CalendarControlMonthView monthView, CalendarControlMonthAdapter.CalendarDate calendarDate);
	}
	
	public CalendarControlMonthAdapter.CalendarDate getCalendarDateFrom() {
		return mAdapter.getCalendarDateFrom();
	}
	
	public CalendarControlMonthAdapter.CalendarDate getCalendarDateTo() {
		return mAdapter.getCalendarDateTo();
	}
}