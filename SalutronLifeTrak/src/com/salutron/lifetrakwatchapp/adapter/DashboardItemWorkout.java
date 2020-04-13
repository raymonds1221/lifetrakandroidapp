package com.salutron.lifetrakwatchapp.adapter;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.salutron.lifetrak.R;

public class DashboardItemWorkout implements DashboardItem {
	private int workoutStatus;
	private int workoutValue;
	private int mDashboardType;
	private Date mDate;
	
	public DashboardItemWorkout() { }
	
	public DashboardItemWorkout(Parcel source) {
		readFromParcel(source);
	}
	
	DashboardItemWorkout(int type) {
		mDashboardType = type;
	}

	@Override
	public int getItemViewType() {
		return DASHBOARD_ITEM_TYPE_WORKOUT;
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view = convertView;
		ViewHolder viewHolder;
		
		if(workoutValue > 0) {
			view = inflater.inflate(R.layout.adapter_dashboard_workout_item, null);
		} else {
			view = inflater.inflate(R.layout.adapter_dashboard_workout_none_item, null);
		}
		
		viewHolder = new ViewHolder();
		viewHolder.workoutImage = (ImageView) view.findViewById(R.id.imgWorkout);
		viewHolder.dashboardItemTitle = (TextView) view.findViewById(R.id.tvwDashboardItemTitle);
		viewHolder.dashboardItemValue = (TextView) view.findViewById(R.id.tvwDashboardItemValue);
		
		if(workoutValue > 0) {
			viewHolder.workoutImage.setImageResource(R.drawable.lt_icon_workout);
			viewHolder.dashboardItemValue.setText(String.valueOf(workoutValue));
		} else {
			Calendar calendarNow = new GregorianCalendar();
			Calendar calendarDate = new GregorianCalendar();
			
			calendarDate.setTime(mDate);
			
			if(calendarNow.get(Calendar.DAY_OF_MONTH) == calendarDate.get(Calendar.DAY_OF_MONTH) &&
					(calendarNow.get(Calendar.MONTH) == calendarDate.get(Calendar.MONTH)) &&
					(calendarNow.get(Calendar.YEAR) == calendarDate.get(Calendar.YEAR))) {
				//viewHolder.workoutImage.setImageResource(R.drawable.dash_workout_reminder);
				viewHolder.dashboardItemTitle.setText(view.getContext().getString(R.string.metric_reminder,
													view.getContext().getString(R.string.workout_small)));
				viewHolder.dashboardItemTitle.setTextColor(view.getContext().getResources().getColor(android.R.color.black));
			} else {
				//viewHolder.workoutImage.setImageResource(R.drawable.dash_workout_none);
				viewHolder.dashboardItemTitle.setText(view.getContext().getString(R.string.metric_none,
													view.getContext().getString(R.string.workout_small)));
				viewHolder.dashboardItemTitle.setTextColor(view.getContext().getResources().getColor(R.color.abs__primary_text_holo_light));
			}
			viewHolder.workoutImage.setImageResource(R.drawable.lt_icon_workout_inactive);
		}
		
		//viewHolder.workoutImage.setImageResource(R.drawable.lt_icon_workout);
		
		return view;
	}

	private class ViewHolder {
		public ImageView workoutImage;
		public TextView dashboardItemTitle;
		public TextView dashboardItemValue;
	}

	public int getWorkoutStatus() {
		return workoutStatus;
	}

	public void setWorkoutStatus(int workoutStatus) {
		this.workoutStatus = workoutStatus;
	}

	public int getWorkoutValue() {
		return workoutValue;
	}

	public void setWorkoutValue(int workoutValue) {
		this.workoutValue = workoutValue;
	}

	@Override
	public int getDashboardType() {
		return mDashboardType;
	}

	@Override
	public void setDate(Date date) {
		mDate = date;
	}

	/*
	 * Parcelable methods
	 */
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(workoutStatus);
		dest.writeInt(workoutValue);
		dest.writeInt(mDashboardType);
		dest.writeLong(mDate.getTime());
	}
	
	private void readFromParcel(Parcel source) {
		workoutStatus = source.readInt();
		workoutValue = source.readInt();
		mDashboardType = source.readInt();
		mDate = new Date(source.readLong());
	}
	
	public static final Parcelable.Creator<DashboardItem> CREATOR = new Parcelable.Creator<DashboardItem>() {

		@Override
		public DashboardItem createFromParcel(Parcel source) {
			return new DashboardItemWorkout(source);
		}

		@Override
		public DashboardItem[] newArray(int size) {
			return new DashboardItemWorkout[size];
		}
	};
}
