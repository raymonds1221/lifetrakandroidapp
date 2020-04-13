package com.salutron.lifetrakwatchapp.model;

import java.util.Date;
import java.util.List;
import java.util.Calendar;

import android.os.Parcel;
import android.content.Context;

import com.salutron.lifetrakwatchapp.annotation.DataTable;
import com.salutron.lifetrakwatchapp.annotation.DataColumn;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.blesdk.SALStatisticalDataHeader;
import com.salutron.blesdk.SALDateStamp;
import com.salutron.blesdk.SALTimeStamp;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;

@DataTable(name="StatisticalDataHeader")
public class StatisticalDataHeader extends BaseModel {
	@DataColumn(name="allocationBlockIndex")
	private int allocationBlockIndex;
	@DataColumn(name="totalSteps")
	private long totalSteps;
	@DataColumn(name="totalDistance")
	private double totalDistance;
	@DataColumn(name="totalCalorie")
	private double totalCalorie;
	@DataColumn(name="totalSleep")
	private int totalSleep;
	@DataColumn(name="dateStamp")
	private Date dateStamp;
	@DataColumn(name="dateStampDay")
	private int dateStampDay;
	@DataColumn(name="dateStampMonth")
	private int dateStampMonth;
	@DataColumn(name="dateStampYear")
	private int dateStampYear;
	@DataColumn(name="timeStartSecond")
	private int timeStartSecond;
	@DataColumn(name="timeStartMinute")
	private int timeStartMinute;
	@DataColumn(name="timeStartHour")
	private int timeStartHour;
	@DataColumn(name="timeEndSecond")
	private int timeEndSecond;
	@DataColumn(name="timeEndMinute")
	private int timeEndMinute;
	@DataColumn(name="timeEndHour")
	private int timeEndHour;
	@DataColumn(name="watchDataHeader", isPrimary=true)
	private Watch watch;
	@DataColumn(name="dataHeaderAndPoint", isForeign=true, model=StatisticalDataPoint.class)
	private List<StatisticalDataPoint> statisticalDataPoints;
	private List<LightDataPoint> lightDataPoints;
	@DataColumn(name="minimumBPM")
	private int minimumBPM;
	@DataColumn(name="maximumBPM")
	private int maximumBPM;
	@DataColumn(name="lightExposure")
	private int lightExposure;
	@DataColumn(name="syncedToCloud")
	private boolean syncedToCloud;

	// Derived fields
	private long startTime;
	private long endTime;
	
	private static StatisticalDataHeader mExistingDataHeader;
	
	public StatisticalDataHeader() { }
	
	private StatisticalDataHeader(Context context) {
		super(context);
	}
	
	public static final StatisticalDataHeader buildStatisticalDataHeader(Context context, SALStatisticalDataHeader salStatisticalDataHeader) {
		StatisticalDataHeader statisticalDataHeader = new StatisticalDataHeader(context);
		
		statisticalDataHeader.setAllocationBlockIndex(salStatisticalDataHeader.allocationBlockIndex);
		statisticalDataHeader.setTotalSteps(salStatisticalDataHeader.totalSteps);
		statisticalDataHeader.setTotalDistance(salStatisticalDataHeader.totalDistance);
		statisticalDataHeader.setTotalCalorie(salStatisticalDataHeader.totalCalorie);
		statisticalDataHeader.setTotalSleep(salStatisticalDataHeader.totalSleep);
		statisticalDataHeader.setMinimumBPM(salStatisticalDataHeader.minimumBPM);
		statisticalDataHeader.setMaximumBPM(salStatisticalDataHeader.maximumBPM);
		statisticalDataHeader.setLightExposure(salStatisticalDataHeader.lightExposure);
		
		SALDateStamp salDateStamp = salStatisticalDataHeader.datestamp;
		Calendar calendar = Calendar.getInstance();
		
		calendar.set(Calendar.DAY_OF_MONTH, salDateStamp.nDay);
		calendar.set(Calendar.MONTH, salDateStamp.nMonth - 1);
		calendar.set(Calendar.YEAR, salDateStamp.nYear + 1900);
		
		statisticalDataHeader.dateStampDay = salDateStamp.nDay;
		statisticalDataHeader.dateStampMonth = salDateStamp.nMonth;
		statisticalDataHeader.dateStampYear = salDateStamp.nYear;
		
		statisticalDataHeader.setDateStamp(calendar.getTime());
		
		SALTimeStamp salTimeStart = salStatisticalDataHeader.timeStart;
		statisticalDataHeader.setTimeStartSecond(salTimeStart.nSecond);
		statisticalDataHeader.setTimeStartMinute(salTimeStart.nMinute);
		statisticalDataHeader.setTimeStartHour(salTimeStart.nHour);
		
		SALTimeStamp salTimeEnd = salStatisticalDataHeader.timeEnd;
		statisticalDataHeader.setTimeEndSecond(salTimeEnd.nSecond);
		statisticalDataHeader.setTimeEndMinute(salTimeEnd.nMinute);
		statisticalDataHeader.setTimeEndHour(salTimeEnd.nHour);
		
		return statisticalDataHeader;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

	@Override
	public void readFromParcel(Parcel source) {
	}

	public int getAllocationBlockIndex() {
		return allocationBlockIndex;
	}

	public void setAllocationBlockIndex(int allocationBlockIndex) {
		this.allocationBlockIndex = allocationBlockIndex;
	}

	public long getTotalSteps() {
		return totalSteps;
	}

	public void setTotalSteps(long totalSteps) {
		this.totalSteps = totalSteps;
	}

	public double getTotalDistance() {
		return totalDistance;
	}

	public void setTotalDistance(double totalDistance) {
		this.totalDistance = totalDistance;
	}

	public double getTotalCalorie() {
		return totalCalorie;
	}

	public void setTotalCalorie(double totalCalorie) {
		this.totalCalorie = totalCalorie;
	}

	public int getTotalSleep() {
		return totalSleep;
	}

	public void setTotalSleep(int totalSleep) {
		this.totalSleep = totalSleep;
	}

	public Date getDateStamp() {
		return dateStamp;
	}

	public void setDateStamp(Date dateStamp) {
		this.dateStamp = dateStamp;
	}
	
	public int getDateStampDay() {
		return dateStampDay;
	}

	public void setDateStampDay(int dateStampDay) {
		this.dateStampDay = dateStampDay;
	}

	public int getDateStampMonth() {
		return dateStampMonth;
	}

	public void setDateStampMonth(int dateStampMonth) {
		this.dateStampMonth = dateStampMonth;
	}

	public int getDateStampYear() {
		return dateStampYear;
	}

	public void setDateStampYear(int dateStampYear) {
		this.dateStampYear = dateStampYear;
	}

	public int getTimeStartSecond() {
		return timeStartSecond;
	}

	public void setTimeStartSecond(int timeStartSecond) {
		this.timeStartSecond = timeStartSecond;
	}

	public int getTimeStartMinute() {
		return timeStartMinute;
	}

	public void setTimeStartMinute(int timeStartMinute) {
		this.timeStartMinute = timeStartMinute;
	}

	public int getTimeStartHour() {
		return timeStartHour;
	}

	public void setTimeStartHour(int timeStartHour) {
		this.timeStartHour = timeStartHour;
	}

	public int getTimeEndSecond() {
		return timeEndSecond;
	}

	public void setTimeEndSecond(int timeEndSecond) {
		this.timeEndSecond = timeEndSecond;
	}

	public int getTimeEndMinute() {
		return timeEndMinute;
	}

	public void setTimeEndMinute(int timeEndMinute) {
		this.timeEndMinute = timeEndMinute;
	}

	public int getTimeEndHour() {
		return timeEndHour;
	}

	public void setTimeEndHour(int timeEndHour) {
		this.timeEndHour = timeEndHour;
	}

	public long getStartTime() {
		if (startTime == 0) {
			final Calendar calendar = Calendar.getInstance();
			// Start hour, minute, and second are not updated properly. Ignore them.
			// Data headers always start at 12am (00:00:00.000) anyway.
			calendar.set(dateStampYear + 1900, dateStampMonth - 1, dateStampDay, 0, 0, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			startTime = calendar.getTimeInMillis();
		}
		return startTime;
	}

	public long getEndTime() {
		if (endTime == 0) {
			final Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(getStartTime());
			// End hour, minute, and second are not updated properly. Ignore them.
			// Estimate end time based on the number of data points
			calendar.add(Calendar.MINUTE, 10 * statisticalDataPoints.size());
			endTime = calendar.getTimeInMillis();
		}
		return endTime;
	}

	public Watch getWatch() {
		return watch;
	}

	public void setWatch(Watch watch) {
		this.watch = watch;
	}

	public List<StatisticalDataPoint> getStatisticalDataPoints() {
		return statisticalDataPoints;
	}

	public void setStatisticalDataPoints(
			List<StatisticalDataPoint> statisticalDataPoints) {
		this.statisticalDataPoints = statisticalDataPoints;
		
		if (statisticalDataPoints != null)
			for(StatisticalDataPoint statisticalDataPoint : this.statisticalDataPoints) {
				statisticalDataPoint.setStatisticalDataHeader(this);
			}
	}
	
	public List<LightDataPoint> getLightDataPoints() {
		return lightDataPoints;
	}

	public void setLightDataPoints(List<LightDataPoint> lightDataPoints) {
		this.lightDataPoints = lightDataPoints;
		
		if (lightDataPoints != null)
			for (LightDataPoint lightDataPoint : lightDataPoints) {
				lightDataPoint.setStatisticalDataHeader(this);
			}
	}
	
	public Goal getGoal() {
		final List<Goal> goals = DataSource.getInstance(mContext)
											.getReadOperation()
											.orderBy("abs(date - " + dateStamp.getTime() + ")", SORT_ASC)
											.limit(1)
											.getResults(Goal.class);
		if(goals.size() > 0)
			return goals.get(0);
		return null;
	}
	
	public static final boolean isExists(Context context, long watchId, SALStatisticalDataHeader salDataHeader) {
		SALDateStamp dateStamp = salDataHeader.datestamp;
		
		List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(context)
															.getReadOperation()
															.query("watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?", 
																	String.valueOf(watchId), String.valueOf(dateStamp.nDay), String.valueOf(dateStamp.nMonth), String.valueOf(dateStamp.nYear))
															.getResults(StatisticalDataHeader.class);
		
		if (dataHeaders.size() > 0) {
			mExistingDataHeader = dataHeaders.get(0);
			return true;
		}
		return false;
	}

    public static final boolean isExists(Context context, long watchId, int mDateStampDay, int mDateStampMonth, int mDateStampYear ) {


        List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(context)
                .getReadOperation()
                .query("watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
                        String.valueOf(watchId), String.valueOf(mDateStampDay), String.valueOf(mDateStampMonth), String.valueOf(mDateStampYear))
                .getResults(StatisticalDataHeader.class);

        if (dataHeaders.size() > 0) {
            mExistingDataHeader = dataHeaders.get(0);
            return true;
        }
        return false;
    }
	
	public int getMinimumBPM() {
		return minimumBPM;
	}

	public void setMinimumBPM(int minimumBPM) {
		this.minimumBPM = minimumBPM;
	}

	public int getMaximumBPM() {
		return maximumBPM;
	}

	public void setMaximumBPM(int maximumBPM) {
		this.maximumBPM = maximumBPM;
	}

	public int getLightExposure() {
		return lightExposure;
	}

	public void setLightExposure(int lightExposure) {
		this.lightExposure = lightExposure;
	}

	public static final StatisticalDataHeader getExistingDataHeader() {
		return mExistingDataHeader;
	}
	
	public boolean isSyncedToCloud() {
		return syncedToCloud;
	}

	public void setSyncedToCloud(boolean syncedToCloud) {
		this.syncedToCloud = syncedToCloud;
	}

	@Override
	public String toString() {
		return dateStamp.toString();
	}
}
