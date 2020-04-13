package com.salutron.lifetrakwatchapp.adapter;

import java.util.Calendar;
import java.util.Date;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.salutron.lifetrak.R;

public class DashboardItemActigraphy implements DashboardItem {
	private int mDashboardType;
	private int mHourValue;
	private int mMinuteValue;
	private Date mDate;
	private final DecimalFormat mDecimalFormat = new DecimalFormat("#00");
	
	DashboardItemActigraphy(int type) {
		mDashboardType = type;
	}
	
	DashboardItemActigraphy(Parcel in) {
		mDashboardType = in.readInt();
	}
	
	public static final Parcelable.Creator<DashboardItem> CREATOR = new Parcelable.Creator<DashboardItem>() {

		@Override
		public DashboardItem createFromParcel(Parcel source) {
			return new DashboardItemActigraphy(source);
		}

		@Override
		public DashboardItem[] newArray(int size) {
			return new DashboardItemActigraphy[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.mDashboardType);
	}

	@Override
	public int getItemViewType() {
		return DASHBOARD_ITEM_TYPE_ACTIGRAPHY;
	}

	@Override
	public int getDashboardType() {
		return mDashboardType;
	}

	@Override
	public void setDate(Date date) {
		mDate = date;
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view = convertView;
		ViewHolder viewHolder;
		
		if(view == null) {
			view = inflater.inflate(R.layout.adapter_dashboard_actigraphy_item, null);
			viewHolder = new ViewHolder();
			viewHolder.dashboardItemProgress = (ImageView) view.findViewById(R.id.dmvProgress);
			viewHolder.dashboardItemHourValue = (TextView) view.findViewById(R.id.tvwDashboardItemHourValue);
			viewHolder.dashboardItemMinuteValue = (TextView) view.findViewById(R.id.tvwDashboardItemMinuteValue);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		if(mHourValue > 0 || mMinuteValue > 0) {
			viewHolder.dashboardItemProgress.setImageResource(R.drawable.asset_dash_3_workouticon);
		} else {
			Calendar calValue = Calendar.getInstance();
			Calendar calNow = Calendar.getInstance();
			
			calValue.setTime(mDate);
			calNow.setTime(new Date());
			
			if(calValue.get(Calendar.DAY_OF_MONTH) == calNow.get(Calendar.DAY_OF_MONTH) &&
					calValue.get(Calendar.MONTH) == calNow.get(Calendar.MONTH) ||
					calValue.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)) {
				viewHolder.dashboardItemProgress.setImageResource(R.drawable.lt_icon_activetime_inactive);
			} else {
				viewHolder.dashboardItemProgress.setImageResource(R.drawable.lt_icon_activetime_inactive);
			}
		}
		
		//viewHolder.dashboardItemProgress.setImageResource(R.drawable.asset_dash_3_workouticon);
		//mDecimalFormat.setRoundingMode(RoundingMode.DOWN);
		viewHolder.dashboardItemHourValue.setText(mDecimalFormat.format(mHourValue));
		viewHolder.dashboardItemMinuteValue.setText(mDecimalFormat.format(mMinuteValue));
		
		return view;
	}
	
	public void setHourValue(int hour) {
		mHourValue = hour;
	}
	
	public void setMinuteValue(int minute) {
		mMinuteValue = minute;
	}
	
	private class ViewHolder {
		public ImageView dashboardItemProgress;
		public TextView dashboardItemHourValue;
		public TextView dashboardItemMinuteValue;
	}
}
