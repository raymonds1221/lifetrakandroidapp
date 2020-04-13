package com.salutron.lifetrakwatchapp.model;

import java.util.List;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.content.Context;

import com.salutron.lifetrakwatchapp.annotation.DataTable;
import com.salutron.lifetrakwatchapp.annotation.DataColumn;
import com.salutron.lifetrakwatchapp.util.PreferenceWrapper;

@DataTable(name="Watch")
public class Watch extends BaseModel implements Cloneable {
	@DataColumn(name="model")
	private int model;
	@DataColumn(name="name")
	private String name;
	@DataColumn(name="macAddress")
	private String macAddress;
	@DataColumn(name="watchFirmWare")
	private String watchFirmWare;
	@DataColumn(name="watchSoftWare")
	private String watchSoftWare;
	@DataColumn(name="watchDataHeader", isForeign=true, model=StatisticalDataHeader.class)
	private List<StatisticalDataHeader> statisticalDataHeaders;
	@DataColumn(name="watchGoal", isForeign=true)
	private List<Goal> goals;
	@DataColumn(name="lastSyncDate")
	private Date lastSyncDate;
	@DataColumn(name="cloudLastSyncDate")
	private Date cloudLastSyncDate;
	@DataColumn(name="watchSleepSetting")
	private SleepSetting sleepSetting;
	@DataColumn(name="watchSleepDatabase", isForeign=true, model=SleepDatabase.class)
	private List<SleepDatabase> sleepDatabases;
	@DataColumn(name="watchWorkoutInfo", isForeign=true, model=WorkoutInfo.class)
	private List<WorkoutInfo> workoutInfos;
	@DataColumn(name="watchUserProfile", isForeign=true, model=UserProfile.class)
	private UserProfile userProfile;
	@DataColumn(name="watchTimeDate", isForeign=true, model=TimeDate.class)
	private TimeDate timeDate;
	@DataColumn(name="watchCalibrationData", isForeign=true, model=CalibrationData.class)
	private CalibrationData calibrationData;
	@DataColumn(name="image")
	private String image;
	@DataColumn(name="accessToken")
	private String accessToken;
	@DataColumn(name="watchWakeupSetting", isForeign=true, model=WakeupSetting.class)
	private WakeupSetting wakeupSetting;
	@DataColumn(name="watchNotification", isForeign=true, model=Notification.class)
	private Notification notification;
	@DataColumn(name="watchActivityAlert", isForeign=true, model=ActivityAlertSetting.class)
	private ActivityAlertSetting activityAlertSetting;
	@DataColumn(name="watchDaylightSetting", isForeign=true, model=DayLightDetectSetting.class)
	private DayLightDetectSetting dayLightDetectSetting;
	@DataColumn(name="watchNightlightSetting", isForeign=true, model=NightLightDetectSetting.class)
	private NightLightDetectSetting nightLightDetectSetting;
	@DataColumn(name="profileId")
	private long profileId;

	public Watch() { }

	public Watch(Context context) {
		super(context);
	}

	public Watch(Parcel source) {
		super(source);
	}

	public Watch(Context context, int model, String name) {
		this(context);
		this.model = model;
		this.name = name;
	}

	public int getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public List<StatisticalDataHeader> getStatisticalDataHeaders() {
		return statisticalDataHeaders;
	}

	public void setStatisticalDataHeaders(
			List<StatisticalDataHeader> statisticalDataHeaders) {
		this.statisticalDataHeaders = statisticalDataHeaders;

		for(StatisticalDataHeader statisticalDataHeader : this.statisticalDataHeaders) {
			statisticalDataHeader.setWatch(this);
		}
	}

	public List<Goal> getGoal() {
		return goals;
	}

	public void setGoal(List<Goal> goals) {
		this.goals = goals;

		for(Goal goal : this.goals) {
			goal.setWatch(this);
		}
	}

	public Date getLastSyncDate() {
		return lastSyncDate;
	}

	public void setLastSyncDate(Date lastSyncDate) {
		this.lastSyncDate = lastSyncDate;
	}

	public Date getCloudLastSyncDate() {
		return cloudLastSyncDate;
	}

	public void setCloudLastSyncDate(Date cloudLastSyncDate) {
		this.cloudLastSyncDate = cloudLastSyncDate;
	}

	public long getGoogleFitLastSyncedDataTime() {
		return PreferenceWrapper.getInstance(mContext)
				.getPreferenceLongValue(GOOGLE_FIT_LAST_SYNCED_DATA_TIME_PREFIX + macAddress);
	}

	public void setGoogleFitLastSyncedDataTime(long timeInMillis) {
		PreferenceWrapper.getInstance(mContext)
				.setPreferenceLongValue(GOOGLE_FIT_LAST_SYNCED_DATA_TIME_PREFIX + macAddress, timeInMillis)
				.synchronize();
	}

	public List<Goal> getGoals() {
		return goals;
	}

	public void setGoals(List<Goal> goals) {
		this.goals = goals;

		for (Goal goal : this.goals) {
			goal.setWatch(this);
		}
	}

	public SleepSetting getSleepSetting() {
		return sleepSetting;
	}

	public void setSleepSetting(SleepSetting sleepSetting) {
		this.sleepSetting = sleepSetting;
		this.sleepSetting.setWatch(this);
	}

	public List<SleepDatabase> getSleepDatabases() {
		return sleepDatabases;
	}

	public void setSleepDatabases(List<SleepDatabase> sleepDatabases) {
		this.sleepDatabases = sleepDatabases;

		for(SleepDatabase sleepDatabase : sleepDatabases) {
			sleepDatabase.setWatch(this);
		}
	}

	public List<WorkoutInfo> getWorkoutInfos() {
		return workoutInfos;
	}

	public void setWorkoutInfos(List<WorkoutInfo> workoutInfos) {
		this.workoutInfos = workoutInfos;

		for(WorkoutInfo workoutInfo : workoutInfos) {
			workoutInfo.setWatch(this);
		}
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
		if (userProfile!= null)
			this.userProfile.setWatch(this);
	}

	public TimeDate getTimeDate() {
		return timeDate;
	}

	public void setTimeDate(TimeDate timeDate) {
		this.timeDate = timeDate;
		this.timeDate.setWatch(this);
	}

	public CalibrationData getCalibrationData() {
		return calibrationData;
	}

	public void setCalibrationData(CalibrationData calibrationData) {
		this.calibrationData = calibrationData;
		this.calibrationData.setWatch(this);
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public WakeupSetting getWakeupSetting() {
		return wakeupSetting;
	}

	public void setWakeupSetting(WakeupSetting wakeupSetting) {
		this.wakeupSetting = wakeupSetting;
		this.wakeupSetting.setWatch(this);
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
		this.notification.setWatch(this);
	}

	public ActivityAlertSetting getActivityAlertSetting() {
		return activityAlertSetting;
	}

	public void setActivityAlertSetting(ActivityAlertSetting activityAlertSetting) {
		this.activityAlertSetting = activityAlertSetting;
		this.activityAlertSetting.setWatch(this);
	}

	public DayLightDetectSetting getDayLightDetectSetting() {
		return dayLightDetectSetting;
	}

	public void setDayLightDetectSetting(DayLightDetectSetting dayLightDetectSetting) {
		this.dayLightDetectSetting = dayLightDetectSetting;
		this.dayLightDetectSetting.setWatch(this);
	}

	public NightLightDetectSetting getNightLightDetectSetting() {
		return nightLightDetectSetting;
	}

	public void setNightLightDetectSetting(
			NightLightDetectSetting nightLightDetectSetting) {
		this.nightLightDetectSetting = nightLightDetectSetting;
		this.nightLightDetectSetting.setWatch(this);
	}

	public long getProfileId() {
		return profileId;
	}

	public void setProfileId(long profileId) {
		this.profileId = profileId;
	}

	@Override
	public void readFromParcel(Parcel source) {
		id = source.readLong();
		model = source.readInt();
		name = source.readString();
		macAddress = source.readString();
		lastSyncDate = new Date(source.readLong());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeInt(model);
		dest.writeString(name);
		dest.writeString(macAddress);
		dest.writeLong(lastSyncDate.getTime());
	}

	public static final Parcelable.Creator<BaseModel> CREATOR = new Parcelable.Creator<BaseModel>() {
		@Override
		public BaseModel createFromParcel(Parcel source) {
			return new Watch(source);
		}

		@Override
		public BaseModel[] newArray(int size) {
			return new Watch[size];
		}
	};

    @Override
    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }

	public String getWatchSoftWare() {
		return watchSoftWare;
	}

	public void setWatchSoftWare(String watchSoftWare) {
		this.watchSoftWare = watchSoftWare;
	}

	public String getWatchFirmWare() {
		return watchFirmWare;
	}

	public void setWatchFirmWare(String watchFirmWare) {
		this.watchFirmWare = watchFirmWare;
	}
}
