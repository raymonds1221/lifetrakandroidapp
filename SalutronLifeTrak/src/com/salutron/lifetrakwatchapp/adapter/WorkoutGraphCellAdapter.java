package com.salutron.lifetrakwatchapp.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.content.Context;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;

public class WorkoutGraphCellAdapter extends BaseArrayAdapter<WorkoutInfo> {
	private int mLayoutResourceId;
	private List<WorkoutInfo> mWorkoutInfos;
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();

	public WorkoutGraphCellAdapter(Context context, int resource,
			List<WorkoutInfo> objects) {
		super(context, resource, objects);
		mWorkoutInfos = objects;
		mDateFormat.applyPattern("hh:mm:ss aa");
		mLayoutResourceId = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		WorkoutInfo workoutInfo = mWorkoutInfos.get(position);
		
		if(view == null) {
			view = mInflater.inflate(mLayoutResourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.workoutName = (TextView) view.findViewById(R.id.tvwWorkoutName2);
			viewHolder.totalWorkoutDuration = (TextView) view.findViewById(R.id.totalWorkoutDuration);
			viewHolder.startAndEndTime = (TextView) view.findViewById(R.id.startAndEndTime);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		viewHolder.workoutName.setText(view.getContext().getString(R.string.workout_small) + (position + 1));
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(workoutInfo.getDateStamp());
		calendar.set(Calendar.HOUR_OF_DAY, workoutInfo.getTimeStampHour());
		calendar.set(Calendar.MINUTE, workoutInfo.getTimeStampMinute());
		calendar.set(Calendar.SECOND, workoutInfo.getTimeStampSecond());
		

		int endTimeSeconds = getEndTime(workoutInfo);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(workoutInfo.getDateStamp());
		calendar2.set(Calendar.HOUR_OF_DAY, endTimeSeconds/3600);
		calendar2.set(Calendar.MINUTE, (endTimeSeconds%3600)/60);
		calendar2.set(Calendar.SECOND, endTimeSeconds%60);
		
		if(getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_12_HR) {
			viewHolder.startAndEndTime.setText(mDateFormat.format(calendar.getTime()) + " - " + mDateFormat.format(calendar2.getTime()));	
		} else {
			viewHolder.startAndEndTime.setText(String.format("%02d:%02d:%02d - %02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), calendar2.get(Calendar.HOUR_OF_DAY), calendar2.get(Calendar.MINUTE), calendar2.get(Calendar.SECOND)));
		}
		
		if (workoutInfo.getHour() > 0){
			viewHolder.totalWorkoutDuration.setText(String.format("%02d hr %02d min %02d sec", workoutInfo.getHour(), workoutInfo.getMinute(), workoutInfo.getSecond()));	
		}
		else{
			viewHolder.totalWorkoutDuration.setText(String.format("%02d min %02d sec %02d hund", workoutInfo.getMinute(), workoutInfo.getSecond(), workoutInfo.getHundredths()));
		}
		return view;
	}
	
	private int getEndTime(WorkoutInfo workoutInfo){
		int endTime = getStartTime(workoutInfo) + workoutInfo.getHour()*3600 + workoutInfo.getMinute()*60 + workoutInfo.getSecond();
		if(workoutInfo.getWorkoutStopInfos() != null){
			List<WorkoutStopInfo> workoutStops = new ArrayList<WorkoutStopInfo>();
			workoutStops = workoutInfo.getWorkoutStopInfos();
			workoutStops = filterDuplicateWorkoutStops(workoutStops);
			for (WorkoutStopInfo workoutStop : workoutStops) {
				endTime += (workoutStop.getStopHours()*3600) + (workoutStop.getStopMinutes()*60) + workoutStop.getStopSeconds();
			}
		}
		if (endTime > 86399)
			endTime = 86399;
		return endTime;
	}
	
	private List<WorkoutStopInfo> filterDuplicateWorkoutStops(List<WorkoutStopInfo> workoutStops){
		List<WorkoutStopInfo> filteredWorkoutStops = new ArrayList<WorkoutStopInfo>();
		if (workoutStops.size() > 0){
			filteredWorkoutStops.add(workoutStops.get(0));
		}
		for (WorkoutStopInfo workoutStop : workoutStops) {
			if(filteredWorkoutStops.size()>0){
				for (WorkoutStopInfo filteredWorkoutStop : filteredWorkoutStops) {
					if(workoutStop.getStopHours() == filteredWorkoutStop.getStopHours() && 
					   workoutStop.getStopMinutes() == filteredWorkoutStop.getStopMinutes() && 
					   workoutStop.getStopSeconds() == filteredWorkoutStop.getStopSeconds() && 
					   workoutStop.getStopHundreds() == filteredWorkoutStop.getStopHundreds() &&
					   workoutStop.getWorkoutHours() == filteredWorkoutStop.getWorkoutHours() && 
					   workoutStop.getWorkoutMinutes() == filteredWorkoutStop.getWorkoutMinutes() && 
					   workoutStop.getWorkoutSeconds() == filteredWorkoutStop.getWorkoutSeconds() && 
					   workoutStop.getWorkoutHundreds() == filteredWorkoutStop.getWorkoutHundreds()){
						break;
					}
					else{
						filteredWorkoutStops.add(workoutStop);
					}
				}
			}
		}
		return filteredWorkoutStops;
	}
	//in seconds
	private int getStartTime(WorkoutInfo workoutInfo){
		int hr = workoutInfo.getTimeStampHour();
		int min = workoutInfo.getTimeStampMinute();
		int sec = workoutInfo.getTimeStampSecond();
		return (hr*3600) + (min*60) + sec;
	}
	
	private class ViewHolder {
		public TextView workoutName;
		public TextView totalWorkoutDuration;
		public TextView startAndEndTime;
	}
}
