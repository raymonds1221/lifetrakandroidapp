package com.salutron.lifetrakwatchapp.util;

import java.util.concurrent.TimeUnit;

import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALNotification;
import com.salutron.blesdk.SALWakeupSetting;
import com.salutron.lifetrakwatchapp.model.Notification;
import com.salutron.lifetrakwatchapp.model.WakeupSetting;

public class LifeTrakWatchUtil {

	public static void saveNotificationSettings(SALBLEService service, Notification notification) {

	}

	public static void saveWakeUpSettings(SALBLEService service, WakeupSetting wakeUpSetting) {
		SALWakeupSetting wakeup = new SALWakeupSetting();
		wakeup.setWakeupSetting(SALWakeupSetting.WAKEUP_ALERT_STATUS, wakeUpSetting.isEnabled() ? SALWakeupSetting.ENABLE : SALWakeupSetting.DISABLE);
		// wakeup.setWakeupSetting(SALWakeupSetting.WAKEUP_TIME, ((nHour << 8)+ nMinute));
		wakeup.setWakeupSetting(SALWakeupSetting.WAKEUP_WINDOW, wakeUpSetting.getWindow());
		wakeup.setWakeupSetting(SALWakeupSetting.SNOOZE_ALERT_STATUS, wakeUpSetting.isSnoozeEnabled() ? SALWakeupSetting.ENABLE : SALWakeupSetting.DISABLE);
		wakeup.setWakeupSetting(SALWakeupSetting.SNOOZE_TIME, wakeUpSetting.getSnoozeTime());
		service.updateWakeupSettingData(wakeup);

	}
}
