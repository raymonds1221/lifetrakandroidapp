package com.salutron.lifetrakwatchapp.adapter;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.ImageView;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.view.DashboardMetricView;

public class DashboardItemSleep implements DashboardItem {
	private float progressGoal;
	private float progressValue;
	private int mDashboardType;
	private Date mDate;
	private final DecimalFormat mDecimalFormat = new DecimalFormat("#00");
	
	public DashboardItemSleep() { }
	
	public DashboardItemSleep(Parcel source) {
		readFromParcel(source);
	}
	
	DashboardItemSleep(int type) {
		mDashboardType = type;
	}

	@Override
	public int getItemViewType() {
		return DASHBOARD_ITEM_TYPE_SLEEP;
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view = convertView;
		ViewHolder viewHolder;
		
		if(progressValue > 0) {
			view = inflater.inflate(R.layout.adapter_dashboard_sleep_item, null);
		} else {
			view = inflater.inflate(R.layout.adapter_dashboard_sleep_none_item, null);
		}
		
		mDecimalFormat.setRoundingMode(RoundingMode.DOWN);
		
		viewHolder = new ViewHolder();
		viewHolder.dashboardMetricView = (DashboardMetricView) view.findViewById(R.id.dmvProgress);
		viewHolder.sleepImage = (ImageView) view.findViewById(R.id.imgSleep);
		viewHolder.dashboardItemTitle = (TextView) view.findViewById(R.id.tvwDashboardItemTitle);
		viewHolder.dashboardItemHourValue = (TextView) view.findViewById(R.id.tvwDashboardItemHourValue);
		viewHolder.dashboardItemMinuteValue = (TextView) view.findViewById(R.id.tvwDashboardItemMinuteValue);
		
		if(progressValue > 0) {
			viewHolder.dashboardMetricView.setGoal(progressGoal);
			viewHolder.dashboardMetricView.setValue(progressValue);
			viewHolder.dashboardItemHourValue.setText(mDecimalFormat.format(progressValue / 60));
			viewHolder.dashboardItemMinuteValue.setText(mDecimalFormat.format(progressValue % 60));
		} else {
			Calendar calendarNow = new GregorianCalendar();
			Calendar calendarDate = new GregorianCalendar();
			
			calendarDate.setTime(mDate);
			
			if(calendarNow.get(Calendar.DAY_OF_MONTH) == calendarDate.get(Calendar.DAY_OF_MONTH) &&
					(calendarNow.get(Calendar.MONTH) == calendarDate.get(Calendar.MONTH)) &&
					(calendarNow.get(Calendar.YEAR) == calendarDate.get(Calendar.YEAR))) {
				viewHolder.sleepImage.setImageResource(R.drawable.dash_sleep_reminder);
				viewHolder.dashboardItemTitle.setText(view.getContext().getString(R.string.metric_reminder,
													view.getContext().getString(R.string.sleep_small)));
				viewHolder.dashboardItemTitle.setTextColor(view.getContext().getResources().getColor(android.R.color.black));
			} else {
				viewHolder.sleepImage.setImageResource(R.drawable.dash_sleep_none);
				viewHolder.dashboardItemTitle.setText(view.getContext().getString(R.string.metric_none,
													view.getContext().getString(R.string.sleep_small)));
				viewHolder.dashboardItemTitle.setTextColor(view.getContext().getResources().getColor(R.color.abs__primary_text_holo_light));
			}
		}
		
		return view;
	}

	private class ViewHolder {
		public DashboardMetricView dashboardMetricView;
		public ImageView sleepImage;
		public TextView dashboardItemTitle;
		public TextView dashboardItemHourValue;
		public TextView dashboardItemMinuteValue;
	}

	public float getProgressGoal() {
		return progressGoal;
	}

	public void setProgressGoal(float progressGoal) {
		this.progressGoal = progressGoal;
	}
	
	public float getProgressValue() {
		return progressValue;
	}

	public void setProgressValue(float progressValue) {
		this.progressValue = progressValue;
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
		dest.writeFloat(progressGoal);
		dest.writeFloat(progressValue);
		dest.writeInt(mDashboardType);
		dest.writeLong(mDate.getTime());
	}
	
	public void readFromParcel(Parcel source) {
		progressGoal = source.readFloat();
		progressValue = source.readFloat();
		mDashboardType = source.readInt();
		mDate = new Date(source.readLong());
	}
	
	public static final Parcelable.Creator<DashboardItem> CREATOR = new Parcelable.Creator<DashboardItem>() {

		@Override
		public DashboardItem createFromParcel(Parcel source) {
			return new DashboardItemSleep(source);
		}

		@Override
		public DashboardItem[] newArray(int size) {
			return new DashboardItemSleep[size];
		}
	};
}
