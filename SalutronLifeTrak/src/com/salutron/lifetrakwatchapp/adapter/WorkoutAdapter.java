package com.salutron.lifetrakwatchapp.adapter;

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

public class WorkoutAdapter extends BaseArrayAdapter<WorkoutInfo> {
	private int mLayoutResourceId;
	private List<WorkoutInfo> mWorkoutInfos;
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();

	public WorkoutAdapter(Context context, int resource,
			List<WorkoutInfo> objects) {
		super(context, resource, objects);
		mWorkoutInfos = objects;
		mDateFormat.applyPattern("hh:mm aa");
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
			viewHolder.workoutName = (TextView) view.findViewById(R.id.tvwWorkoutName);
			viewHolder.totalCalories = (TextView) view.findViewById(R.id.tvwTotalCaloriesValue);
			viewHolder.totalSteps = (TextView) view.findViewById(R.id.tvwTotalStepsValue);
			viewHolder.totalDistance = (TextView) view.findViewById(R.id.tvwTotalDistanceValue);
			viewHolder.workoutStartTime = (TextView) view.findViewById(R.id.tvwWorkoutStartTimeValue);
			viewHolder.totalWorkoutTime = (TextView) view.findViewById(R.id.tvwTotalWorkoutTimeValue);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		viewHolder.workoutName.setText(view.getContext().getString(R.string.workout_small) + (position + 1));
		viewHolder.totalCalories.setText(workoutInfo.getCalories() + " kcal");
		viewHolder.totalSteps.setText(String.valueOf(workoutInfo.getSteps()));
		
		
		if(getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL) {
			double distance = workoutInfo.getDistance() * ((workoutInfo.getFlags() == 1)?MILE:1);
			viewHolder.totalDistance.setText(String.format("%.02f", distance) + " mi");
		} else {
			double distance = workoutInfo.getDistance() * ((workoutInfo.getFlags() == 1)?1:MILE);
			viewHolder.totalDistance.setText(String.format("%.02f", distance) + " km");
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(workoutInfo.getDateStamp());
		calendar.set(Calendar.HOUR_OF_DAY, workoutInfo.getTimeStampHour());
		calendar.set(Calendar.MINUTE, workoutInfo.getTimeStampMinute());
		calendar.set(Calendar.SECOND, workoutInfo.getTimeStampSecond());
		
		if(getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_12_HR) {
			viewHolder.workoutStartTime.setText(mDateFormat.format(calendar.getTime()));	
		} else {
			viewHolder.workoutStartTime.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
		}
		
		viewHolder.totalWorkoutTime.setText(String.format("%02d:%02d.%02d.%02d", workoutInfo.getHour(), workoutInfo.getMinute(), workoutInfo.getSecond(), workoutInfo.getHundredths()));
		
		return view;
	}
	
	private class ViewHolder {
		public TextView workoutName;
		public TextView totalCalories;
		public TextView totalSteps;
		public TextView totalDistance;
		public TextView workoutStartTime;
		public TextView totalWorkoutTime;
	}
}
