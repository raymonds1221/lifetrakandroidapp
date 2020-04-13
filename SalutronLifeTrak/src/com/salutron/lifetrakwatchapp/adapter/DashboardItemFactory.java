package com.salutron.lifetrakwatchapp.adapter;

import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

public final class DashboardItemFactory implements SalutronLifeTrakUtility {
	
	public static final DashboardItem createDashboardItem(int type) {
		switch(type) {
		case TYPE_HEART_RATE:
			return new DashboardItemHeartRate(type);
		case TYPE_STEPS:
			return new DashboardItemMetric(type);
		case TYPE_DISTANCE:
			return new DashboardItemMetric(type);
		case TYPE_CALORIES:
			return new DashboardItemMetric(type);
		case TYPE_SLEEP:
			return new DashboardItemSleep(type);
		case TYPE_WORKOUT:
			return new DashboardItemWorkout(type);
		case TYPE_ACTIGRAPHY:
			return new DashboardItemActigraphy(type);
		case TYPE_LIGHT_EXPOSURE:
			return new DashboardItemLightExposure(type);
		}
		return null;
	}
}
