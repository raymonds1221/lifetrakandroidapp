package com.salutron.lifetrakwatchapp.util;

import android.content.Context;

import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.CalibrationData;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.SleepSetting;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.WorkoutSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raymondsarmiento on 10/12/15.
 */
public class LifeTrakSaveManager {
    private static LifeTrakSaveManager ourInstance;
    private Context context;
    private Watch watch;
    private TimeDate timeDate;
    private List<StatisticalDataHeader> statisticalDataHeaders;
    private List<WorkoutHeader> workoutHeaders;
    private List<SleepDatabase> sleepDatabases;
    private List<Goal> goals;
    private SleepSetting sleepSetting;
    private long stepGoal;
    private double distanceGoal;
    private long calorieGoal;
    private CalibrationData calibrationData;
    private UserProfile userProfile;
    private WorkoutSettings workoutSettings;

    public static LifeTrakSaveManager getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new LifeTrakSaveManager(context);
        }
        return ourInstance;
    }

    private LifeTrakSaveManager(Context context) {
        this.context = context;
    }

    public LifeTrakSaveManager watch(Watch watch) {
        this.watch = watch;
        return this;
    }

    public LifeTrakSaveManager timeDate(TimeDate timeDate) {
        this.timeDate = timeDate;
        return this;
    }

    public LifeTrakSaveManager statisticalDataHeaders(List<StatisticalDataHeader> statisticalDataHeaders) {
        this.statisticalDataHeaders = statisticalDataHeaders;
        return this;
    }

    public LifeTrakSaveManager workoutHeaders(List<WorkoutHeader> workoutHeaders) {
        this.workoutHeaders = workoutHeaders;
        return this;
    }

    public LifeTrakSaveManager sleepDatabases(List<SleepDatabase> sleepDatabases) {
        this.sleepDatabases = sleepDatabases;
        return this;
    }

    public LifeTrakSaveManager goals(List<Goal> goals) {
        this.goals = goals;
        return this;
    }

    public LifeTrakSaveManager sleepSetting(SleepSetting sleepSetting) {
        this.sleepSetting = sleepSetting;
        return this;
    }

    public LifeTrakSaveManager stepGoal(long stepGoal) {
        this.stepGoal = stepGoal;
        return this;
    }

    public LifeTrakSaveManager distacneGoal(double distanceGoal) {
        this.distanceGoal = distanceGoal;
        return this;
    }

    public LifeTrakSaveManager calorieGoal(long calorieGoal) {
        this.calorieGoal = calorieGoal;
        return this;
    }

    public LifeTrakSaveManager calibrationData(CalibrationData calibrationData) {
        this.calibrationData = calibrationData;
        return this;
    }

    public LifeTrakSaveManager userProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        return this;
    }
    public LifeTrakSaveManager workoutSettings(WorkoutSettings workoutSettings) {
        this.workoutSettings = workoutSettings;
        return this;
    }

    public void saveStatisticalDataHeader() {
        if (watch.getId() == 0) {
            watch.insert();
        }
        if (statisticalDataHeaders != null)
            saveStatisticalDataHeaders(statisticalDataHeaders);
    }

    public void save() {
        if (watch.getId() == 0) {
            watch.insert();
        }
        if (statisticalDataHeaders != null)
            saveStatisticalDataHeaders(statisticalDataHeaders);
        if (workoutHeaders != null)
            saveWorkoutHeaders(workoutHeaders);
        if (sleepDatabases != null)
            saveSleepDatabases(sleepDatabases);
        if (calibrationData != null)
            saveCalibrationData(calibrationData);
        if (sleepSetting != null)
            saveSleepSetting(sleepSetting);
        if (timeDate != null)
             saveTimeDate(timeDate);
        if (goals != null)
            saveGoals(goals);
        if (userProfile != null)
            saveUserProfile(userProfile);
        if (workoutSettings != null)
            saveWorkoutSettings(workoutSettings);
    }

    private void saveTimeDate(TimeDate timeDate) {
        timeDate.setContext(context);
        timeDate.setWatch(watch);

        List<TimeDate> timeDateFromDB = DataSource.getInstance(context)
                                                    .getReadOperation()
                                                    .query("watchTimeDate = ?", String.valueOf(watch.getId()))
                                                    .getResults(TimeDate.class);

        if (timeDateFromDB.size() == 0) {
            timeDate.insert();;
        }
    }

    private void saveStatisticalDataHeaders(List<StatisticalDataHeader> statisticalDataHeaders) {
        for (StatisticalDataHeader statisticalDataHeader : statisticalDataHeaders) {
            statisticalDataHeader.setContext(context);
            statisticalDataHeader.setWatch(watch);

            if (statisticalDataHeader.getId() == 0) {
                statisticalDataHeader.insert();
            } else {
                statisticalDataHeader.update();
            }
        }
    }

    private void saveWorkoutHeaders(List<WorkoutHeader> workoutHeaders) {
        for (WorkoutHeader workoutHeader : workoutHeaders) {
            if (!workoutHeader.isExists()) {
                workoutHeader.setContext(context);
                workoutHeader.setWatch(watch);
                workoutHeader.insert();
            }
        }
    }

    private void saveSleepDatabases(List<SleepDatabase> sleepDatabases) {
        for (SleepDatabase sleepDatabase : sleepDatabases) {
            if (!sleepDatabase.isExists()) {
                sleepDatabase.setContext(context);
                sleepDatabase.setWatch(watch);
                sleepDatabase.setSyncedToCloud(false);
                sleepDatabase.insert();;
            }
        }
    }

    private void saveGoals(List<Goal> goals) {
        for (Goal goal : goals) {
            goal.setContext(context);
            goal.setWatch(watch);
            goal.insert();
        }
    }

    private void saveCalibrationData(CalibrationData calibrationData) {
        calibrationData.setContext(context);
        calibrationData.setWatch(watch);

        List<CalibrationData> calibrationDataFromDB = DataSource.getInstance(context)
                                                                .getReadOperation()
                                                                .query("watchCalibrationData = ?", String.valueOf(watch.getId()))
                                                                .getResults(CalibrationData.class);

        if (calibrationDataFromDB.size() == 0) {
            calibrationData.insert();
        }
    }

    private void saveSleepSetting(SleepSetting sleepSetting) {
        sleepSetting.setContext(context);
        sleepSetting.setWatch(watch);

        List<SleepSetting> sleepSettingFromDB = DataSource.getInstance(context)
                                                            .getReadOperation()
                                                            .query("watchSleepSetting = ?", String.valueOf(watch.getId()))
                                                            .getResults(SleepSetting.class);

        if (sleepSettingFromDB.size() == 0) {
            sleepSetting.insert();
        }
    }

    private void saveUserProfile(UserProfile userProfile) {
        userProfile.setContext(context);
        userProfile.setWatch(watch);

        List<UserProfile> userProfileFromDB = DataSource.getInstance(context)
                                                        .getReadOperation()
                                                        .query("watchUserProfile = ?", String.valueOf(watch.getId()))
                                                        .getResults(UserProfile.class);

        if (userProfileFromDB.size() == 0) {
            userProfile.insert();
        }
    }

    private void saveWorkoutSettings(WorkoutSettings workoutSettings) {
        workoutSettings.setContext(context);
        workoutSettings.setWatch(watch);

        List<WorkoutSettings> userWorkOutFromDB = DataSource.getInstance(context)
                .getReadOperation()
                .query("watchDataHeader = ?", String.valueOf(watch.getId()))
                .getResults(WorkoutSettings.class);

        if (userWorkOutFromDB.size() == 0) {
            this.workoutSettings.insert();
        }
        else{
           WorkoutSettings mWorkoutSettings = userWorkOutFromDB.get(0);
            mWorkoutSettings.setDatabaseUsage(workoutSettings.getDatabaseUsage());
            mWorkoutSettings.setDatabaseUsageMax(workoutSettings.getDatabaseUsageMax());
            mWorkoutSettings.setReconnectTime(workoutSettings.getReconnectTime());
            mWorkoutSettings.setWatch(watch);
            mWorkoutSettings.setContext(context);
            mWorkoutSettings.update();
        }
    }
}
