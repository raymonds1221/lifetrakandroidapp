package com.salutron.lifetrakwatchapp.model;

import android.os.Parcel;
import android.content.Context;

import com.salutron.lifetrakwatchapp.annotation.*;
import com.salutron.blesdk.SALWorkoutRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raymondsarmiento on 10/8/15.
 */
@DataTable(name="WorkoutRecord")
public class WorkoutRecord extends BaseModel {
    @DataColumn(name="type")
    private int type;
    @DataColumn(name="splitHundredths")
    private int splitHundredths;
    @DataColumn(name="splitSecond")
    private int splitSecond;
    @DataColumn(name="splitMinute")
    private int splitMinute;
    @DataColumn(name="splitHour")
    private int splitHour;
    @DataColumn(name="steps")
    private long steps;
    @DataColumn(name="distance")
    private double distance;
    @DataColumn(name="calories")
    private double calories;
    @DataColumn(name="stopHundredths")
    private int stopHundredths;
    @DataColumn(name="stopSecond")
    private int stopSecond;
    @DataColumn(name="stopMinute")
    private int stopMinute;
    @DataColumn(name="stopHour")
    private int stopHour;
    @DataColumn(name="hr1")
    private int hr1;
    @DataColumn(name="hr2")
    private int hr2;
    @DataColumn(name="hr3")
    private int hr3;
    @DataColumn(name="hr4")
    private int hr4;
    @DataColumn(name="hr5")
    private int hr5;
    @DataColumn(name="hr6")
    private int hr6;
    @DataColumn(name="hr7")
    private int hr7;
    @DataColumn(name="hr8")
    private int hr8;
    @DataColumn(name="workoutHeaderAndRecord", isPrimary=true)
    private WorkoutHeader workoutHeader;

    public WorkoutRecord(Context context) {
        super(context);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { }

    @Override
    public void readFromParcel(Parcel source) { }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSplitHundredths() {
        return splitHundredths;
    }

    public void setSplitHundredths(int splitHundredths) {
        this.splitHundredths = splitHundredths;
    }

    public int getSplitSecond() {
        return splitSecond;
    }

    public void setSplitSecond(int splitSecond) {
        this.splitSecond = splitSecond;
    }

    public int getSplitMinute() {
        return splitMinute;
    }

    public void setSplitMinute(int splitMinute) {
        this.splitMinute = splitMinute;
    }

    public int getSplitHour() {
        return splitHour;
    }

    public void setSplitHour(int splitHour) {
        this.splitHour = splitHour;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
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

    public int getStopHundredths() {
        return stopHundredths;
    }

    public void setStopHundredths(int stopHundredths) {
        this.stopHundredths = stopHundredths;
    }

    public int getStopSecond() {
        return stopSecond;
    }

    public void setStopSecond(int stopSecond) {
        this.stopSecond = stopSecond;
    }

    public int getStopMinute() {
        return stopMinute;
    }

    public void setStopMinute(int stopMinute) {
        this.stopMinute = stopMinute;
    }

    public int getStopHour() {
        return stopHour;
    }

    public void setStopHour(int stopHour) {
        this.stopHour = stopHour;
    }

    public int getHr1() {
        return hr1;
    }

    public void setHr1(int hr1) {
        this.hr1 = hr1;
    }

    public int getHr2() {
        return hr2;
    }

    public void setHr2(int hr2) {
        this.hr2 = hr2;
    }

    public int getHr3() {
        return hr3;
    }

    public void setHr3(int hr3) {
        this.hr3 = hr3;
    }

    public int getHr4() {
        return hr4;
    }

    public void setHr4(int hr4) {
        this.hr4 = hr4;
    }

    public int getHr5() {
        return hr5;
    }

    public void setHr5(int hr5) {
        this.hr5 = hr5;
    }

    public int getHr6() {
        return hr6;
    }

    public void setHr6(int hr6) {
        this.hr6 = hr6;
    }

    public int getHr7() {
        return hr7;
    }

    public void setHr7(int hr7) {
        this.hr7 = hr7;
    }

    public int getHr8() {
        return hr8;
    }

    public void setHr8(int hr8) {
        this.hr8 = hr8;
    }

    public WorkoutHeader getWorkoutHeader() {
        return workoutHeader;
    }

    public void setWorkoutHeader(WorkoutHeader workoutHeader) {
        this.workoutHeader = workoutHeader;
    }

    public static final WorkoutRecord buildWorkoutRecord(Context context, SALWorkoutRecord salWorkoutRecord) {
        WorkoutRecord workoutRecord = new WorkoutRecord(context);

        workoutRecord.setType(salWorkoutRecord.nType);
        workoutRecord.setSplitHundredths(salWorkoutRecord.split_hundredths);
        workoutRecord.setSplitSecond(salWorkoutRecord.split_second);
        workoutRecord.setSplitMinute(salWorkoutRecord.split_minute);
        workoutRecord.setSplitHour(salWorkoutRecord.split_hour);
        workoutRecord.setSteps(salWorkoutRecord.steps);
        workoutRecord.setDistance(salWorkoutRecord.distance);
        workoutRecord.setCalories(salWorkoutRecord.calories);
        workoutRecord.setStopHundredths(salWorkoutRecord.stop_hundredths);
        workoutRecord.setStopSecond(salWorkoutRecord.stop_second);
        workoutRecord.setStopMinute(salWorkoutRecord.stop_minute);
        workoutRecord.setStopHour(salWorkoutRecord.stop_hour);
        workoutRecord.setHr1(salWorkoutRecord.HR1);
        workoutRecord.setHr2(salWorkoutRecord.HR2);
        workoutRecord.setHr3(salWorkoutRecord.HR3);
        workoutRecord.setHr4(salWorkoutRecord.HR4);
        workoutRecord.setHr5(salWorkoutRecord.HR5);
        workoutRecord.setHr6(salWorkoutRecord.HR6);
        workoutRecord.setHr7(salWorkoutRecord.HR7);
        workoutRecord.setHr8(salWorkoutRecord.HR8);

        return workoutRecord;
    }

    public static final List<WorkoutRecord> buildWorkoutRecord(Context context, List<SALWorkoutRecord> salWorkoutRecords) {
        List<WorkoutRecord> workoutRecords = new ArrayList<>();

        for (SALWorkoutRecord salWorkoutRecord : salWorkoutRecords) {
            WorkoutRecord workoutRecord = WorkoutRecord.buildWorkoutRecord(context, salWorkoutRecord);
            workoutRecords.add(workoutRecord);
        }

        return workoutRecords;
    }
}
