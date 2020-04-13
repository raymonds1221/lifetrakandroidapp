package com.salutron.lifetrakwatchapp.adapter;

import java.util.Date;
import java.util.Calendar;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.salutron.lifetrak.R;

public class DashboardItemHeartRate implements DashboardItem {
	private int mDashboardType;
	private int heartRateProgress;
	private float value;
	private float percent;
	private Date mDate;
	
	private final DecimalFormat mDecimalFormat = new DecimalFormat("##0");
	
	public DashboardItemHeartRate() { }
	
	public DashboardItemHeartRate(Parcel source) {
		readFromParcel(source);
	}
	
	DashboardItemHeartRate(int type) {
		mDashboardType = type;
	}
	
	@Override
	public int getItemViewType() {
		return DASHBOARD_ITEM_TYPE_HEART_RATE;
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view = convertView;
		ViewHolder viewHolder;
		
		viewHolder = new ViewHolder();
		
		if(value > 0) {
			view = inflater.inflate(R.layout.adapter_dashboard_heartrate_item, null);
		} else {
			view = inflater.inflate(R.layout.adapter_dashboard_heart_rate_none_item, null);
		}
		
		viewHolder.heartRateProgress = (ImageView) view.findViewById(R.id.imgHeartRateProgress);
		viewHolder.heartRatePercent = (TextView) view.findViewById(R.id.tvwHeartRatePercent);
		viewHolder.dashboardItemTitle = (TextView) view.findViewById(R.id.tvwDashboardItemTitle);
		viewHolder.dashboardItemValue = (TextView) view.findViewById(R.id.tvwDashboardItemValue);
		
		if(value > 0) {
			if(percent * 100.0f >= 100) {
				viewHolder.heartRateProgress.setImageResource(R.drawable.ll_dashboard_heartrate_wheel_max);
			} else if(percent * 100.0f >= 75) {
				viewHolder.heartRateProgress.setImageResource(R.drawable.ll_dashboard_heartrate_wheel_hard);
			} else if(percent * 100.0f >= 50) {
				viewHolder.heartRateProgress.setImageResource(R.drawable.ll_dashboard_heartrate_wheel_moderate);
			} else if(percent * 100.0f >= 25) {
				viewHolder.heartRateProgress.setImageResource(R.drawable.ll_dashboard_heartrate_wheel_light);
			} else if(percent * 100.0f >= 0) {
				viewHolder.heartRateProgress.setImageResource(R.drawable.ll_dashboard_heartrate_wheel_vlight);
			}
			
			mDecimalFormat.setRoundingMode(RoundingMode.DOWN);
			
			viewHolder.heartRatePercent.setText(mDecimalFormat.format(percent * 100f) + "%");
			viewHolder.dashboardItemValue.setText(mDecimalFormat.format(value));
		} else {
			Calendar calendarToday = Calendar.getInstance();
			Calendar calendarDate = Calendar.getInstance();
			
			calendarDate.setTime(mDate);
			
			if(calendarToday.get(Calendar.DAY_OF_MONTH) == calendarDate.get(Calendar.DAY_OF_MONTH) &&
					calendarToday.get(Calendar.MONTH) == calendarDate.get(Calendar.MONTH) &&
					calendarToday.get(Calendar.YEAR) == calendarDate.get(Calendar.YEAR)) {
				viewHolder.heartRateProgress.setImageResource(R.drawable.dash_heartrate_reminder);
				viewHolder.dashboardItemTitle.setText(view.getContext().getString(R.string.metric_reminder,
						view.getContext().getString(R.string.heart_rate_small)));
				viewHolder.dashboardItemTitle.setTextColor(view.getContext().getResources().getColor(android.R.color.black));
			} else {
				viewHolder.heartRateProgress.setImageResource(R.drawable.dash_heartrate_none);
				viewHolder.dashboardItemTitle.setText(view.getContext().getString(R.string.metric_none,
						view.getContext().getString(R.string.heart_rate_small)));
				viewHolder.dashboardItemTitle.setTextColor(view.getContext().getResources().getColor(R.color.abs__primary_text_holo_light));
			}
		}
		
		return view;
	}
	
	private class ViewHolder {
		public ImageView heartRateProgress;
		public TextView heartRatePercent;
		public TextView dashboardItemTitle;
		public TextView dashboardItemValue;
	}

	public int getHeartRateProgress() {
		return heartRateProgress;
	}

	public void setHeartRateProgress(int heartRateProgress) {
		this.heartRateProgress = heartRateProgress;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public float getPercent() {
		return percent;
	}

	public void setPercent(float percent) {
		this.percent = percent;
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
		dest.writeInt(mDashboardType);
		dest.writeInt(heartRateProgress);
		dest.writeFloat(value);
		dest.writeLong(mDate.getTime());
	}
	
	private void readFromParcel(Parcel source) {
		mDashboardType = source.readInt();
		heartRateProgress = source.readInt();
		value = source.readInt();
		mDate = new Date(source.readLong());
	}
	
	public static final Parcelable.Creator<DashboardItem> CREATOR = new Parcelable.Creator<DashboardItem>() {

		@Override
		public DashboardItem createFromParcel(Parcel source) {
			return new DashboardItemHeartRate(source);
		}

		@Override
		public DashboardItem[] newArray(int size) {
			return new DashboardItemHeartRate[size];
		}
	};
}
