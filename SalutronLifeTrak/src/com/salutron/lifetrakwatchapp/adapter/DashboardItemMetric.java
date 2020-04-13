package com.salutron.lifetrakwatchapp.adapter;

import java.util.Date;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.ImageView;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.view.DashboardMetricView;

public class DashboardItemMetric implements DashboardItem {
	private double progressGoal;
	private double progressValue;
	private int mDashboardType;
	private int unitSystem;
	private final DecimalFormat mDecimalFormat = new DecimalFormat("#.00");
	private final DecimalFormat mDecimalFormat2 = new DecimalFormat("#####0");
	
	public DashboardItemMetric() { }
	
	public DashboardItemMetric(Parcel source) {
		readFromParcel(source);
	}
	
	DashboardItemMetric(int type) {
		mDashboardType = type;
	}

	@Override
	public int getItemViewType() {
		return DASHBOARD_ITEM_TYPE_METRIC;
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view = convertView;
		ViewHolder viewHolder;
		
		view = inflater.inflate(R.layout.adapter_dashboard_metric_item, null);
		
		viewHolder = new ViewHolder();
		viewHolder.dashboardMetricView = (DashboardMetricView) view.findViewById(R.id.dmvProgress);
		viewHolder.dashboardItemTitle = (TextView) view.findViewById(R.id.tvwDashboardItemTitle);
		viewHolder.dashboardItemValue = (TextView) view.findViewById(R.id.tvwDashboardItemValue);
		viewHolder.dashboardItemUnit = (TextView) view.findViewById(R.id.tvwDashboardItemUnit);
		viewHolder.dashboardItemIcon = (ImageView) view.findViewById(R.id.imgDashboadItemIcon);
		
		viewHolder.dashboardMetricView.setValue(progressValue);
		viewHolder.dashboardMetricView.setGoal(progressGoal);
		
		//mDecimalFormat.setRoundingMode(RoundingMode.DOWN);
		//mDecimalFormat2.setRoundingMode(RoundingMode.DOWN);
		
		switch(mDashboardType) {
		case TYPE_STEPS:
			viewHolder.dashboardItemTitle.setText(R.string.steps);
			viewHolder.dashboardItemValue.setText(mDecimalFormat2.format(progressValue));
			viewHolder.dashboardItemIcon.setImageResource(R.drawable.dash_4_1steps);
			break;
		case TYPE_DISTANCE:
			LifeTrakApplication application = (LifeTrakApplication) view.getContext().getApplicationContext();
			
			viewHolder.dashboardItemTitle.setText(R.string.distance);
			
			switch(application.getUserProfile().getUnitSystem()) {
			case UNIT_IMPERIAL:
				double value = progressValue * MILE;
				viewHolder.dashboardItemValue.setText(""+Math.round(value*100)/100.00);
//				BigDecimal bigDecimal = new BigDecimal(value);
//				bigDecimal = bigDecimal.setScale(4, BigDecimal.ROUND_DOWN);
//				
//				viewHolder.dashboardItemValue.setText(String.valueOf(bigDecimal));
				viewHolder.dashboardItemUnit.setText("mi");
				break; 
			case UNIT_METRIC:
				//viewHolder.dashboardItemValue.setText(mDecimalFormat.format(progressValue));
				viewHolder.dashboardItemValue.setText(""+Math.round(progressValue*100)/100.00);
				viewHolder.dashboardItemUnit.setText("km");
				break;
			}
			
			viewHolder.dashboardItemIcon.setImageResource(R.drawable.dash_4_2distance);
			break;
		case TYPE_CALORIES:
			viewHolder.dashboardItemTitle.setText(R.string.calories);
			viewHolder.dashboardItemValue.setText(mDecimalFormat2.format(progressValue));
			viewHolder.dashboardItemIcon.setImageResource(R.drawable.dash_4_3calories);
			break;
		}
		
		return view;
	}
	
	private class ViewHolder {
		public DashboardMetricView dashboardMetricView;
		public TextView dashboardItemTitle;
		public TextView dashboardItemValue;
		public TextView dashboardItemUnit;
		public ImageView dashboardItemIcon;
	}

	public double getProgressGoal() {
		return progressGoal;
	}

	public void setProgressGoal(float progressGoal) {
		this.progressGoal = progressGoal;
	}

	public double getProgressValue() {
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
	}

	public void setUnitSystem(int unitSystem) {
		this.unitSystem = unitSystem;
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
		dest.writeDouble(progressGoal);
		dest.writeDouble(progressValue);
		dest.writeInt(mDashboardType);
		dest.writeInt(unitSystem);
	}
	
	public void readFromParcel(Parcel source) {
		progressGoal = source.readDouble();
		progressValue = source.readDouble();
		mDashboardType = source.readInt();
		unitSystem = source.readInt();
	}
	
	public static final Parcelable.Creator<DashboardItem> CREATOR = new Parcelable.Creator<DashboardItem>() {

		@Override
		public DashboardItem createFromParcel(Parcel source) {
			return new DashboardItemMetric(source);
		}

		@Override
		public DashboardItem[] newArray(int size) {
			return new DashboardItemMetric[size];
		}
	};
}
