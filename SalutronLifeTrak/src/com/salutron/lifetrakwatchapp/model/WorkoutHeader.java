package com.salutron.lifetrakwatchapp.model;

import android.content.Context;
import android.os.Parcel;

import com.salutron.blesdk.SALWorkoutHeader;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.annotation.*;
import com.salutron.lifetrakwatchapp.db.DataSource;

import java.util.List;

/**
 * Created by raymondsarmiento on 10/7/15.
 */
@DataTable(name="WorkoutHeader")
public class WorkoutHeader extends BaseModel {
    @DataColumn(name="timeStampSecond")
    private int timeStampSecond;
    @DataColumn(name="timeStampMinute")
    private int timeStampMinute;
    @DataColumn(name="timeStampHour")
    private int timeStampHour;
    @DataColumn(name="dateStampDay")
    private int dateStampDay;
    @DataColumn(name="dateStampMonth")
    private int dateStampMonth;
    @DataColumn(name="dateStampYear")
    private int dateStampYear;
    @DataColumn(name="hundredths")
    private int hundredths;
    @DataColumn(name="second")
    private int second;
    @DataColumn(name="minute")
    private int minute;
    @DataColumn(name="hour")
    private int hour;
    @DataColumn(name="distance")
    private double distance;
    @DataColumn(name="calories")
    private double calories;
    @DataColumn(name="steps")
    private long steps;
    @DataColumn(name="countSplitsRecord")
    private int countSplitsRecord;
    @DataColumn(name="countStopsRecord")
    private int countStopsRecord;
    @DataColumn(name="countHRRecord")
    private int countHRRecord;
    @DataColumn(name="countTotalRecord")
    private int countTotalRecord;
    @DataColumn(name="averageBPM")
    private int averageBPM;
    @DataColumn(name="minimumBPM")
    private int minimumBPM;
    @DataColumn(name="maximumBPM")
    private int maximumBPM;
    @DataColumn(name="statusFlags")
    private int statusFlags;
    @DataColumn(name="logRateHR")
    private int logRateHR;
    @DataColumn(name="autoSplitType")
    private int autoSplitType;
    @DataColumn(name="zoneTrainType")
    private int zoneTrainType;
    @DataColumn(name="userMaxHR")
    private int userMaxHR;
    @DataColumn(name="zone0UpperHR")
    private int zone0UpperHR;
    @DataColumn(name="zone0LowerHR")
    private int zone0LowerHR;
    @DataColumn(name="zone1LowerHR")
    private int zone1LowerHR;
    @DataColumn(name="zone2LowerHR")
    private int zone2LowerHR;
    @DataColumn(name="zone3LowerHR")
    private int zone3LowerHR;
    @DataColumn(name="zone4LowerHR")
    private int zone4LowerHR;
    @DataColumn(name="zone5LowerHR")
    private int zone5LowerHR;
    @DataColumn(name="zone5UpperHR")
    private int zone5UpperHR;


    @DataColumn(name="headerHeartRate")
    private String headerHeartRate;
    @DataColumn(name="autoSplitThreshold")
    private int autoSplitThreshold;

    @DataColumn(name="watchWorkoutHeader", isPrimary=true)
    private Watch watch;
    @DataColumn(name="workoutHeaderAndRecord", isForeign=true, model=WorkoutRecord.class)
    private List<WorkoutRecord> workoutRecords;



    @DataColumn(name="headerAndStop", isForeign=true, model=WorkoutStopInfo.class)
    private List<WorkoutStopInfo> workoutStopInfo;

    @DataColumn(name="syncedToCloud")
    private boolean syncedToCloud;

    public WorkoutHeader() { }

    public WorkoutHeader(Context context) {
        super(context);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { }

    @Override
    public void readFromParcel(Parcel source) { }



    public String getHeaderHeartRate() {
        return headerHeartRate;
    }

    public void setHeaderHeartRate(String headerHeartRate) {
        this.headerHeartRate = headerHeartRate;
    }

    public int getTimeStampSecond() {
        return timeStampSecond;
    }

    public void setTimeStampSecond(int timeStampSecond) {
        this.timeStampSecond = timeStampSecond;
    }

    public int getTimeStampMinute() {
        return timeStampMinute;
    }

    public void setTimeStampMinute(int timeStampMinute) {
        this.timeStampMinute = timeStampMinute;
    }

    public int getTimeStampHour() {
        return timeStampHour;
    }

    public void setTimeStampHour(int timeStampHour) {
        this.timeStampHour = timeStampHour;
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

    public int getHundredths() {
        return hundredths;
    }

    public void setHundredths(int hundredths) {
        this.hundredths = hundredths;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public int getCountSplitsRecord() {
        return countSplitsRecord;
    }

    public void setCountSplitsRecord(int countSplitsRecord) {
        this.countSplitsRecord = countSplitsRecord;
    }

    public int getCountStopsRecord() {
        return countStopsRecord;
    }

    public void setCountStopsRecord(int countStopsRecord) {
        this.countStopsRecord = countStopsRecord;
    }

    public int getCountHRRecord() {
        return countHRRecord;
    }

    public void setCountHRRecord(int countHRRecord) {
        this.countHRRecord = countHRRecord;
    }

    public int getCountTotalRecord() {
        return countTotalRecord;
    }

    public void setCountTotalRecord(int countTotalRecord) {
        this.countTotalRecord = countTotalRecord;
    }

    public int getAverageBPM() {
        return averageBPM;
    }

    public void setAverageBPM(int averageBPM) {
        this.averageBPM = averageBPM;
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

    public int getStatusFlags() {
        return statusFlags;
    }

    public void setStatusFlags(int statusFlags) {
        this.statusFlags = statusFlags;
    }

    public int getLogRateHR() {
        return logRateHR;
    }

    public void setLogRateHR(int logRateHR) {
        this.logRateHR = logRateHR;
    }

    public int getAutoSplitType() {
        return autoSplitType;
    }

    public void setAutoSplitType(int autoSplitType) {
        this.autoSplitType = autoSplitType;
    }

    public int getZoneTrainType() {
        return zoneTrainType;
    }

    public void setZoneTrainType(int zoneTrainType) {
        this.zoneTrainType = zoneTrainType;
    }

    public int getUserMaxHR() {
        return userMaxHR;
    }

    public void setUserMaxHR(int userMaxHR) {
        this.userMaxHR = userMaxHR;
    }

    public int getZone0UpperHR() {
        return zone0UpperHR;
    }

    public void setZone0UpperHR(int zone0UpperHR) {
        this.zone0UpperHR = zone0UpperHR;
    }

    public int getZone0LowerHR() {
        return zone0LowerHR;
    }

    public void setZone0LowerHR(int zone0LowerHR) {
        this.zone0LowerHR = zone0LowerHR;
    }

    public int getZone1LowerHR() {
        return zone1LowerHR;
    }

    public void setZone1LowerHR(int zone1LowerHR) {
        this.zone1LowerHR = zone1LowerHR;
    }

    public int getZone2LowerHR() {
        return zone2LowerHR;
    }

    public void setZone2LowerHR(int zone2LowerHR) {
        this.zone2LowerHR = zone2LowerHR;
    }

    public int getZone3LowerHR() {
        return zone3LowerHR;
    }

    public void setZone3LowerHR(int zone3LowerHR) {
        this.zone3LowerHR = zone3LowerHR;
    }

    public int getZone4LowerHR() {
        return zone4LowerHR;
    }

    public void setZone4LowerHR(int zone4LowerHR) {
        this.zone4LowerHR = zone4LowerHR;
    }

    public int getZone5LowerHR() {
        return zone5LowerHR;
    }

    public void setZone5LowerHR(int zone5LowerHR) {
        this.zone5LowerHR = zone5LowerHR;
    }

    public int getZone5UpperHR() {
        return zone5UpperHR;
    }

    public void setZone5UpperHR(int zone5UpperHR) {
        this.zone5UpperHR = zone5UpperHR;
    }

    public int getAutoSplitThreshold() {
        return autoSplitThreshold;
    }

    public void setAutoSplitThreshold(int autoSplitThreshold) {
        this.autoSplitThreshold = autoSplitThreshold;
    }

    public Watch getWatch() {
        return watch;
    }

    public void setWatch(Watch watch) {
        this.watch = watch;
    }

    public List<WorkoutRecord> getWorkoutRecords() {
        return workoutRecords;
    }

    public void setWorkoutRecords(List<WorkoutRecord> workoutRecords) {
        this.workoutRecords = workoutRecords;

        if (this.workoutRecords != null) {
            for (WorkoutRecord workoutRecord : this.workoutRecords) {
                workoutRecord.setWorkoutHeader(this);
            }
        }
    }

    public List<WorkoutStopInfo> getWorkoutStopInfo() {
        return workoutStopInfo;
    }

    public void setWorkoutStopInfo(List<WorkoutStopInfo> workoutStopInfo) {
        this.workoutStopInfo = workoutStopInfo;
    }

    public static final WorkoutHeader buildWorkoutHeader(Context context, SALWorkoutHeader salWorkoutHeader) {
        WorkoutHeader workoutHeader = new WorkoutHeader(context);

        workoutHeader.setTimeStampHour(salWorkoutHeader.timestamp.nHour);
        workoutHeader.setTimeStampMinute(salWorkoutHeader.timestamp.nMinute);
        workoutHeader.setTimeStampSecond(salWorkoutHeader.timestamp.nSecond);
        workoutHeader.setDateStampDay(salWorkoutHeader.datestamp.nDay);
        workoutHeader.setDateStampMonth(salWorkoutHeader.datestamp.nMonth);
        workoutHeader.setDateStampYear(salWorkoutHeader.datestamp.nYear);
        workoutHeader.setHundredths(salWorkoutHeader.hundredths);
        workoutHeader.setSecond(salWorkoutHeader.second);
        workoutHeader.setMinute(salWorkoutHeader.minute);
        workoutHeader.setHour(salWorkoutHeader.hour);
        workoutHeader.setDistance(salWorkoutHeader.distance);
        workoutHeader.setCalories(salWorkoutHeader.calories);
        workoutHeader.setSteps(salWorkoutHeader.steps);
        workoutHeader.setCountSplitsRecord(salWorkoutHeader.countSplitsRecord);
        workoutHeader.setCountStopsRecord(salWorkoutHeader.countStopsRecord);
        workoutHeader.setCountHRRecord(salWorkoutHeader.countHRRecord);
        workoutHeader.setCountTotalRecord(salWorkoutHeader.countTotalRecord);
        workoutHeader.setAverageBPM(salWorkoutHeader.averageBPM);
        workoutHeader.setMinimumBPM(salWorkoutHeader.minimumBPM);
        workoutHeader.setMaximumBPM(salWorkoutHeader.maximumBPM);
        workoutHeader.setStatusFlags(salWorkoutHeader.statusFlags);
        workoutHeader.setLogRateHR(salWorkoutHeader.logRateHR);
        workoutHeader.setAutoSplitType(salWorkoutHeader.autoSplitType);
        workoutHeader.setZoneTrainType(salWorkoutHeader.zoneTrainType);
        workoutHeader.setUserMaxHR(salWorkoutHeader.userMaxHR);
        workoutHeader.setZone0UpperHR(salWorkoutHeader.zone0UpperHR);
        workoutHeader.setZone0LowerHR(salWorkoutHeader.zone0LowerHR);
        workoutHeader.setZone1LowerHR(salWorkoutHeader.zone1LowerHR);
        workoutHeader.setZone2LowerHR(salWorkoutHeader.zone2LowerHR);
        workoutHeader.setZone3LowerHR(salWorkoutHeader.zone3LowerHR);
        workoutHeader.setZone4LowerHR(salWorkoutHeader.zone4LowerHR);
        workoutHeader.setZone5LowerHR(salWorkoutHeader.zone5LowerHR);
        workoutHeader.setZone5UpperHR(salWorkoutHeader.zone5UpperHR);


        return workoutHeader;
    }

    public boolean isExists() {
        LifeTrakApplication lifeTrakApplication = (LifeTrakApplication) mContext.getApplicationContext();

        if (lifeTrakApplication.getSelectedWatch() == null) {
            return false;
        }

        String query = "dateStampDay = ? and dateStampMonth = ? and dateStampYear =? " +
                            "and timeStampSecond = ? and timeStampMinute = ? and timeStampHour = ? and watchWorkoutHeader = ?";

        List<WorkoutHeader> workoutHeaders = DataSource.getInstance(mContext)
                .getReadOperation()
                .query(query, String.valueOf(dateStampDay), String.valueOf(dateStampMonth), String.valueOf(dateStampYear),
                        String.valueOf(timeStampSecond), String.valueOf(timeStampMinute), String.valueOf(timeStampHour), String.valueOf(lifeTrakApplication.getSelectedWatch().getId()))
                .getResults(WorkoutHeader.class);

        if (workoutHeaders != null && workoutHeaders.size() > 0) {
            return true;
        }

        return false;
    }


    public WorkoutHeader isExistsWorkoutHeader() {
        LifeTrakApplication lifeTrakApplication = (LifeTrakApplication) mContext.getApplicationContext();

        if (lifeTrakApplication.getSelectedWatch() == null) {
            return null;
        }

        String query = "dateStampDay = ? and dateStampMonth = ? and dateStampYear =? " +
                    "and timeStampSecond = ? and timeStampMinute = ? and timeStampHour = ? and watchWorkoutHeader = ?";

            List<WorkoutHeader> workoutHeaders = DataSource.getInstance(mContext)
                    .getReadOperation()
                    .query(query, String.valueOf(dateStampDay), String.valueOf(dateStampMonth), String.valueOf(dateStampYear),
                            String.valueOf(timeStampSecond), String.valueOf(timeStampMinute), String.valueOf(timeStampHour), String.valueOf(lifeTrakApplication.getSelectedWatch().getId()))
                    .getResults(WorkoutHeader.class);

            if (workoutHeaders != null && workoutHeaders.size() > 0) {
                return workoutHeaders.get(0);
        }

        return null;
    }

    public boolean isSyncedToCloud() {
        return syncedToCloud;
    }

    public void setSyncedToCloud(boolean syncedToCloud) {
        this.syncedToCloud = syncedToCloud;
    }
}
