package com.salutron.lifetrakwatchapp.model;

import android.content.Context;
import android.os.Parcel;

import com.salutron.blesdk.SALDateStamp;
import com.salutron.blesdk.SALStatisticalDataHeader;
import com.salutron.blesdk.SALTimeStamp;
import com.salutron.blesdk.SALWorkoutSetting;
import com.salutron.lifetrakwatchapp.annotation.DataColumn;
import com.salutron.lifetrakwatchapp.annotation.DataTable;

import java.util.Calendar;

/**
 * Created by Janwel Ocampo on 12/9/2015.
 */
@DataTable(name="WorkoutSettings")
public class WorkoutSettings extends BaseModel {

    @DataColumn(name="watchDataHeader", isPrimary=true)
    private Watch watch;
    @DataColumn(name="databaseUsageMax")
    private int databaseUsageMax;
    @DataColumn(name="databaseUsage")
    private int databaseUsage;


    @DataColumn(name="hrLoggingRate")
    private int hrLoggingRate;
    @DataColumn(name="reconnectTime")
    private int reconnectTime;

    public WorkoutSettings() { }

    private WorkoutSettings(Context context) {
        super(context);
    }


    public static final WorkoutSettings buildWorkoutSettings(Context context, SALWorkoutSetting salWorkoutSetting) {
        WorkoutSettings workoutSettings = new WorkoutSettings(context);
        workoutSettings.setDatabaseUsage(salWorkoutSetting.getDatabaseUsage());
        workoutSettings.setDatabaseUsageMax(salWorkoutSetting.getDatabaseMaxUsage());
        workoutSettings.setHrLoggingRate(salWorkoutSetting.getHRLoggingRate());
        workoutSettings.setReconnectTime(salWorkoutSetting.getReconnectTimeout());
        return workoutSettings;
    }

    public int getHrLoggingRate() {
        return hrLoggingRate;
    }

    public void setHrLoggingRate(int hrLoggingRate) {
        this.hrLoggingRate = hrLoggingRate;
    }


    public Watch getWatch() {
        return watch;
    }

    public void setWatch(Watch watch) {
        this.watch = watch;
    }

    public int getDatabaseUsageMax() {
        return databaseUsageMax;
    }

    public void setDatabaseUsageMax(int databaseUsageMax) {
        this.databaseUsageMax = databaseUsageMax;
    }

    public int getDatabaseUsage() {
        return databaseUsage;
    }

    public void setDatabaseUsage(int databaseUsage) {
        this.databaseUsage = databaseUsage;
    }
    public int getReconnectTime() {
        return reconnectTime;
    }

    public void setReconnectTime(int reconnectTime) {
        this.reconnectTime = reconnectTime;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @Override
    public void readFromParcel(Parcel source) {

    }
}
