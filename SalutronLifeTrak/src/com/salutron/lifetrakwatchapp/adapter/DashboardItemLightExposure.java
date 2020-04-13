package com.salutron.lifetrakwatchapp.adapter;

import java.util.Date;
import java.text.DecimalFormat;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.model.BaseModel;
import com.salutron.lifetrakwatchapp.view.DashboardLighExposureView;

public class DashboardItemLightExposure implements DashboardItem {
	private float progressGoal;
	private float progressValue;
	private int mDashboardType;
	private final DecimalFormat mDecimalFormat = new DecimalFormat("#00");
	
	public DashboardItemLightExposure() { }

    public DashboardItemLightExposure(Parcel parcel) {

    }
	
	DashboardItemLightExposure(int type) {
		mDashboardType = type;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		
	}

	@Override
	public int getItemViewType() {
		return DASHBOARD_ITEM_TYPE_LIGHT_EXPOSURE;
	}

	@Override
	public int getDashboardType() {
		return mDashboardType;
	}

	@Override
	public void setDate(Date date) {
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
	public View getView(LayoutInflater inflater, View convertView) {
		View view = convertView;
		ViewHolder viewHolder = null;
		
//		if (view == null) {
			view = inflater.inflate(R.layout.adapter_dashboard_light_exposure_item, null);
			viewHolder = new ViewHolder();
			viewHolder.dashboardLightExposureView = (DashboardLighExposureView) view.findViewById(R.id.dmvProgress);
			viewHolder.dashboardItemHour = (TextView) view.findViewById(R.id.tvwDashboardItemHourValue);
			viewHolder.dashboardItemMinute = (TextView) view.findViewById(R.id.tvwDashboardItemMinuteValue);
			view.setTag(viewHolder);
//		} else {
//			viewHolder = (ViewHolder) view.getTag();
//		}
//
		// if haven't set a progress goal, use default 10 (check GoalItemFragment)
		if (progressGoal == 0) {
			progressGoal = 10;
		}
		
		viewHolder.dashboardLightExposureView.setGoal(progressGoal);
		viewHolder.dashboardLightExposureView.setValue(progressValue);
		viewHolder.dashboardItemHour.setText(String.valueOf((int) progressValue / 60));
		viewHolder.dashboardItemMinute.setText(mDecimalFormat.format(progressValue % 60));
		
		return view;
	}
	
	private class ViewHolder {
		public DashboardLighExposureView  dashboardLightExposureView;
		public TextView dashboardItemHour;
		public TextView dashboardItemMinute;
	}

    public static final Creator<DashboardItemLightExposure> CREATOR = new Creator<DashboardItemLightExposure>() {
        @Override
        public DashboardItemLightExposure createFromParcel(Parcel parcel) {
            return new DashboardItemLightExposure(parcel);
        }

        @Override
        public DashboardItemLightExposure[] newArray(int i) {
            return new DashboardItemLightExposure[i];
        }
    };
}
