package com.salutron.lifetrakwatchapp.adapter;

import java.util.List;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;

public class SleepLogsAdapter extends BaseArrayAdapter<SleepDatabase> {
	private int mLayoutResource;
	private List<SleepDatabase> mSleepDatabases;
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private final SimpleDateFormat mDateFormat2 = (SimpleDateFormat) DateFormat.getInstance();

	public SleepLogsAdapter(Context context, int resource, List<SleepDatabase> objects) {
		super(context, resource, objects);
		mLayoutResource = resource;
		mSleepDatabases = objects;
		mDateFormat.applyPattern("MMMM dd");
		mDateFormat2.applyPattern("hh:mm aa");
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder = null;
		SleepDatabase sleepDatabase = mSleepDatabases.get(position);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, sleepDatabase.getDateStampDay());
		calendar.set(Calendar.MONTH, sleepDatabase.getDateStampMonth() - 1);
		calendar.set(Calendar.YEAR, sleepDatabase.getDateStampYear() + 1900);
		
		if(view == null) {
			view = mInflater.inflate(mLayoutResource, null);
			view.setId(1000);
			viewHolder = new ViewHolder();
			
			viewHolder.sleepDuration = (TextView) view.findViewById(R.id.tvwSleepDuration);
			viewHolder.sleepTime = (TextView) view.findViewById(R.id.tvwSleepTime);
			
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		/*
		int sleepStart = sleepDatabase.getHourSleepStart() * 60 + sleepDatabase.getMinuteSleepStart();
		int sleepEnd = sleepDatabase.getHourSleepEnd() * 60 + sleepDatabase.getMinuteSleepEnd();
		int sleepDuration = 0;
		
		if(sleepStart > sleepEnd) {
			sleepDuration = -(sleepEnd - sleepStart);
		} else {
			sleepDuration = sleepEnd - sleepStart;
		}
		*/
		int sleepDuration = sleepDatabase.getSleepDuration();
		
		viewHolder.sleepDuration.setText(String.format("%dH %02dM", sleepDuration / 60, sleepDuration % 60));
		
		int hourStart = 0;
		int minuteStart = 0;
		int hourEnd = 0;
		int minuteEnd = 0;
		String ampmStart = view.getContext().getString(R.string.am);
		String ampmEnd = view.getContext().getString(R.string.am);
		
		
		if(getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_12_HR) {
			if(sleepDatabase.getHourSleepStart() > 12 || (sleepDatabase.getHourSleepStart() >= 12 && sleepDatabase.getMinuteSleepStart() > 0)) {
				hourStart = sleepDatabase.getHourSleepStart() - 12;
				ampmStart = view.getContext().getString(R.string.pm);
			} else {
				hourStart = sleepDatabase.getHourSleepStart();
				ampmStart = view.getContext().getString(R.string.am);
			}
			
			if(hourStart == 0)
				hourStart = 12;
			
			minuteStart = sleepDatabase.getMinuteSleepStart();
			
			if(sleepDatabase.getHourSleepEnd() > 12 || (sleepDatabase.getHourSleepEnd() >= 12 && sleepDatabase.getMinuteSleepEnd() > 0)) {
				hourEnd = sleepDatabase.getHourSleepEnd() - 12;
				ampmEnd = view.getContext().getString(R.string.pm);
			} else {
				hourEnd = sleepDatabase.getHourSleepEnd();
				ampmEnd = view.getContext().getString(R.string.am);
			}
			
			if(hourEnd == 0)
				hourEnd = 12;
			
			minuteEnd = sleepDatabase.getMinuteSleepEnd();
			
			viewHolder.sleepTime.setText(mDateFormat.format(calendar.getTime()) + String.format(" %d:%02d%s - %d:%02d%s", hourStart, minuteStart, ampmStart, hourEnd, minuteEnd, ampmEnd));
		} else {
			hourStart = sleepDatabase.getHourSleepStart();
			minuteStart = sleepDatabase.getMinuteSleepStart();
			hourEnd = sleepDatabase.getHourSleepEnd();
			minuteEnd = sleepDatabase.getMinuteSleepEnd();
			
			viewHolder.sleepTime.setText(mDateFormat.format(calendar.getTime()) + String.format(" %d:%02d - %d:%02d", hourStart, minuteStart, hourEnd, minuteEnd));
		}
		
		return view;
	}
	
	private class ViewHolder {
		public TextView sleepDuration;
		public TextView sleepTime;
	}
}
