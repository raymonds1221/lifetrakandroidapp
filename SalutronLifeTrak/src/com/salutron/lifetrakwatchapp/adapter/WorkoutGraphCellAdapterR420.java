package com.salutron.lifetrakwatchapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorkoutGraphCellAdapterR420 extends BaseArrayAdapter<WorkoutHeader> {
	private int mLayoutResourceId;
	private List<WorkoutHeader> mWorkoutheader;
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private Context mContext;

	public WorkoutGraphCellAdapterR420(Context context, int resource,
									   List<WorkoutHeader> objects) {
		super(context, resource, objects);
		mWorkoutheader = objects;
		mContext = context;
		mDateFormat.applyPattern("hh:mm:ss aa");
		mLayoutResourceId = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		WorkoutHeader workoutHeader = mWorkoutheader.get(position);

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

		viewHolder.workoutName.setText(view.getContext().getString(R.string.workout_small) + " " + (position + 1));

		Calendar calendar = Calendar.getInstance();
		//calendar.setTime(workoutInfo.getDateStamp());
		calendar.set(Calendar.YEAR, workoutHeader.getDateStampYear() + 1900);
		calendar.set(Calendar.MONTH, workoutHeader.getDateStampMonth() -1);
		calendar.set(Calendar.DAY_OF_MONTH, workoutHeader.getDateStampDay());
		calendar.set(Calendar.HOUR_OF_DAY, workoutHeader.getTimeStampHour());
		calendar.set(Calendar.MINUTE, workoutHeader.getTimeStampMinute());
		calendar.set(Calendar.SECOND, workoutHeader.getTimeStampSecond());


		int endTimeSeconds = getEndTime(workoutHeader);
		Calendar calendar2 = Calendar.getInstance();
		calendar.set(Calendar.YEAR, workoutHeader.getDateStampYear());
		calendar.set(Calendar.MONTH, workoutHeader.getDateStampMonth() -1);
		calendar.set(Calendar.DAY_OF_MONTH, workoutHeader.getDateStampDay());
		calendar2.set(Calendar.HOUR_OF_DAY, endTimeSeconds/3600);
		calendar2.set(Calendar.MINUTE, (endTimeSeconds%3600)/60);
		calendar2.set(Calendar.SECOND, endTimeSeconds%60);

		if(getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_12_HR) {
			viewHolder.startAndEndTime.setText(mDateFormat.format(calendar.getTime()) + " - " + mDateFormat.format(calendar2.getTime()));
		} else {
			viewHolder.startAndEndTime.setText(String.format("%02d:%02d:%02d - %02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), calendar2.get(Calendar.HOUR_OF_DAY), calendar2.get(Calendar.MINUTE), calendar2.get(Calendar.SECOND)));
		}

		if (workoutHeader.getHour() > 0){
			viewHolder.totalWorkoutDuration.setText(String.format("%02d hr %02d min %02d sec", workoutHeader.getHour(), workoutHeader.getMinute(), workoutHeader.getSecond()));
		}
		else{
			viewHolder.totalWorkoutDuration.setText(String.format("%02d min %02d sec %02d hund", workoutHeader.getMinute(), workoutHeader.getSecond(), workoutHeader.getHundredths()));
		}
		return view;
	}
	
//	private int getEndTime(WorkoutHeader workoutInfo){
//		int endTime = getStartTime(workoutInfo) + workoutInfo.getHour()*3600 + workoutInfo.getMinute()*60 + workoutInfo.getSecond();
//		if(workoutInfo.getWorkoutStopInfos() != null){
//			List<WorkoutStopInfo> workoutStops = new ArrayList<WorkoutStopInfo>();
//			workoutStops = workoutInfo.getWorkoutStopInfos();
//			workoutStops = filterDuplicateWorkoutStops(workoutStops);
//			for (WorkoutStopInfo workoutStop : workoutStops) {
//				endTime += (workoutStop.getStopHours()*3600) + (workoutStop.getStopMinutes()*60) + workoutStop.getStopSeconds();
//			}
//		}
//		if (endTime > 86399)
//			endTime = 86399;
//		return endTime;
//	}

	private int getEndTime(WorkoutHeader workoutHeader){
		int endTime = getStartTime(workoutHeader) + workoutHeader.getHour()*3600 + workoutHeader.getMinute()*60 + workoutHeader.getSecond();

		String query = "SELECT * FROM WorkoutHeader WHERE dateStampDay = ? and dateStampMonth = ? and dateStampYear =? " +
				"and timeStampSecond = ? and timeStampMinute = ? and timeStampHour = ? and watchWorkoutHeader = ?";

		Cursor cursor = DataSource.getInstance(mContext)
				.getReadOperation()
				.rawQuery(query, String.valueOf(workoutHeader.getDateStampDay()), String.valueOf(workoutHeader.getDateStampMonth()), String.valueOf(workoutHeader.getDateStampYear()),
						String.valueOf(workoutHeader.getTimeStampSecond()), String.valueOf(workoutHeader.getTimeStampMinute()), String.valueOf(workoutHeader.getTimeStampHour()), String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));

		if (cursor != null && cursor.getCount() > 0) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				List<WorkoutStopInfo> workoutStops = DataSource.getInstance(mContext)
						.getReadOperation().query("headerAndStop = ?", cursor.getString(cursor.getColumnIndex("_id"))).getResults(WorkoutStopInfo.class);
				synchronized (workoutStops) {
					if (workoutStops.size() != 0) {
						workoutStops = filterDuplicateWorkoutStops(workoutStops);
						for (WorkoutStopInfo workoutStop : workoutStops) {
							endTime += (workoutStop.getStopHours() * 3600) + (workoutStop.getStopMinutes() * 60) + workoutStop.getStopSeconds();
						}
					}
				}
			}
		}
		cursor.close();


		return endTime;
	}

	private List<WorkoutStopInfo> filterDuplicateWorkoutStops(List<WorkoutStopInfo> workoutStops){
		List<WorkoutStopInfo> filteredWorkoutStops = new CopyOnWriteArrayList<WorkoutStopInfo>();
		synchronized (workoutStops) {
		if (workoutStops.size() > 0){
			filteredWorkoutStops.add(workoutStops.get(0));
		}
		for (WorkoutStopInfo workoutStop : workoutStops) {
			if (filteredWorkoutStops.size() > 0) {
				for (WorkoutStopInfo filteredWorkoutStop : filteredWorkoutStops) {
					if (workoutStop.getStopHours() == filteredWorkoutStop.getStopHours() &&
							workoutStop.getStopMinutes() == filteredWorkoutStop.getStopMinutes() &&
							workoutStop.getStopSeconds() == filteredWorkoutStop.getStopSeconds() &&
							workoutStop.getStopHundreds() == filteredWorkoutStop.getStopHundreds() &&
							workoutStop.getWorkoutHours() == filteredWorkoutStop.getWorkoutHours() &&
							workoutStop.getWorkoutMinutes() == filteredWorkoutStop.getWorkoutMinutes() &&
							workoutStop.getWorkoutSeconds() == filteredWorkoutStop.getWorkoutSeconds() &&
							workoutStop.getWorkoutHundreds() == filteredWorkoutStop.getWorkoutHundreds()) {
						break;
					} else {
						filteredWorkoutStops.add(workoutStop);
					}
				}
			}
		}
		}
		return filteredWorkoutStops;
	}
	//in seconds
	private int getStartTime(WorkoutHeader mWorkoutHeader){
		int hr = mWorkoutHeader.getTimeStampHour();
		int min = mWorkoutHeader.getTimeStampMinute();
		int sec = mWorkoutHeader.getTimeStampSecond();
		return (hr*3600) + (min*60) + sec;
	}
	
	private class ViewHolder {
		public TextView workoutName;
		public TextView totalWorkoutDuration;
		public TextView startAndEndTime;
	}
}
