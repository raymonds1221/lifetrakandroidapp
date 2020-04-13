package com.salutron.lifetrakwatchapp.util;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.bluetooth.BluetoothDevice;
import android.database.Cursor;
import android.util.Log;

import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALCalibration;
import com.salutron.blesdk.SALDateStamp;
import com.salutron.blesdk.SALDynamicWorkoutInfo;
import com.salutron.blesdk.SALSleepDatabase;
import com.salutron.blesdk.SALSleepSetting;
import com.salutron.blesdk.SALStatisticalDataHeader;
import com.salutron.blesdk.SALStatisticalDataPoint;
import com.salutron.blesdk.SALStatus;
import com.salutron.blesdk.SALTimeDate;
import com.salutron.blesdk.SALUserProfile;
import com.salutron.blesdk.SALWorkoutHeader;
import com.salutron.blesdk.SALWorkoutSetting;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.CalibrationData;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.SleepSetting;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.StatisticalDataPoint;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutRecord;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.WorkoutSettings;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;

import org.json.JSONArray;

import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by raymondsarmiento on 10/6/15.
 */
public class LifeTrakSyncR420 implements SalutronLifeTrakUtility {
    private static SALBLEService salbleService;
    private static LifeTrakSyncR420 lifeTrakSyncR420;
    private static BluetoothDevice bluetoothDevice;
    private static SalutronSDKCallback420 salutronSDKCallback420; // the callback every after successful call the SDK method
    private static List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private static Handler delayHandler = new Handler(); // use when need to have a delay
    private static Handler mainThreadHandler = new Handler();
    private static Queue<Callable> callables = new LinkedList<>();
    private static List<Integer> headerIndexes = new ArrayList<>(); // field to get all header indexes that needed to insert/delete
    public static ExecutorService executorService; // use to execute a callable object
    private static LifeTrakApplication lifeTrakApplication;
    private static Context context;
    private static int headerIndex; // current index of the data header
    private static int calibrationType; // use to determine the current calibration type
    private static int calibrationIndex; // current index of the calibration

    private static final int DELAY = 750;

    private static List<List<WorkoutStopInfo>> mWorkoutStopInfos = new ArrayList<List<WorkoutStopInfo>>();

    private static final int get_workout_settings_max_storage = 0x01;
    private static final int get_workout_settings_max_usage = 0x02;
    private static final int get_workout_settings_logging_rate= 0x03;
    private static final int get_workout_settings_reconnect_timeout= 0x04;
    private static int mCurrentWorkOutRequest = get_workout_settings_max_storage;

    private static PreferenceWrapper mPreferenceWrapper;

    private static WorkoutHeader mExistingWorkoutHeader;

    private static boolean mSetFromSettings = false;

    // Models
    private static Watch watch;
    private static TimeDate timeDate;
    private static List<StatisticalDataHeader> statisticalDataHeaders = new ArrayList<>();
    private static List<List<StatisticalDataPoint>> mStatisticalDataPoints = new ArrayList<List<StatisticalDataPoint>>();
    private static List<WorkoutHeader> workoutHeaders = new ArrayList<>();
    private static List<SleepDatabase> sleepDatabases = new ArrayList<>();
    private static SleepSetting sleepSetting;
    private static long stepGoal;
    private static double distanceGoal;
    private static long calorieGoal;
    private static CalibrationData calibrationData;
    public static UserProfile userProfile;
    public static WorkoutSettings workoutSettings;

    public static Future future;
    public static int workoutInfoCounter;
    public static String softwareRevision;

    public static String firmwareVersion;
    private static int workoutCount;
    private static int workoutHeaderIndex;
    private static List<Integer> workoutHeaderIndexes = new ArrayList<>();
    private static List<Integer> workoutValidIndex = new ArrayList<>();
    //region Singleton

    public LifeTrakSyncR420(Context context) {
        lifeTrakApplication = (LifeTrakApplication) context.getApplicationContext();
        executorService = Executors.newSingleThreadExecutor();
        mPreferenceWrapper = PreferenceWrapper.getInstance(context);
        this.context = context;
    }

    public static LifeTrakSyncR420 getInstance(Context context) {
        if (lifeTrakSyncR420 == null) {
            lifeTrakSyncR420 = new LifeTrakSyncR420(context);

        }
        return lifeTrakSyncR420;
    }

    //endregion

    //region Getters / Setters

    public void setServiceInstance(SALBLEService salbleService) {
        this.salbleService = salbleService;
    }

    public void setSDKCallback(SalutronSDKCallback420 salutronSDKCallback420) {
        this.salutronSDKCallback420 = salutronSDKCallback420;
    }

    public Watch getWatch() {
        return watch;
    }

    public TimeDate getTimeDate() {
        return timeDate;
    }

    public CalibrationData getCalibrationData(){return  calibrationData;}
    public  WorkoutSettings getWorkoutSettings(){ return  workoutSettings;}

    public Goal getGoal(){
        Goal newGoal = new Goal();
        newGoal.setCalorieGoal(calorieGoal);
        newGoal.setStepGoal(stepGoal);
        newGoal.setDistanceGoal(distanceGoal);
        newGoal.setSleepGoal(sleepSetting.getSleepGoalMinutes());
        return newGoal;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    private static List<Goal> goals() {
        List<Goal> goals = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();

        for (StatisticalDataHeader statisticalDataHeader : statisticalDataHeaders) {
            calendar.setTime(statisticalDataHeader.getDateStamp());

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR) - 1900;

            if (lifeTrakApplication.getSelectedWatch() != null) {
                List<Goal> goalsFromDB = DataSource.getInstance(context)
                        .getReadOperation()
                        .query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
                                String.valueOf(lifeTrakApplication.getSelectedWatch().getId()), String.valueOf(day), String.valueOf(month), String.valueOf(year))
                        .getResults(Goal.class);

                if (goalsFromDB.size() > 0)
                    continue;
            }

            Goal goal = new Goal(context);
            goal.setStepGoal(stepGoal);
            goal.setDistanceGoal(distanceGoal);
            goal.setCalorieGoal(calorieGoal);
            goal.setSleepGoal(sleepSetting.getSleepGoalMinutes());
            goal.setDate(calendar.getTime());
            goal.setDateStampDay(day);
            goal.setDateStampMonth(month);
            goal.setDateStampYear(year);

            goals.add(goal);
        }

        return goals;
    }

    //endregion

    //region Private Methods

    public void registerHandler() {
        if (salbleService != null) {
            salbleService.registerDevDataHandler(serviceHandler);
            salbleService.registerDevListHandler(serviceHandler);

        }
    }

    private static int executeCallable(Callable callable) {
        future = executorService.submit(callable);
        try {
            LifeTrakLogger.info("callable = " + future.get());
            Integer value = (Integer) future.get();
            return value.intValue();
        } catch (InterruptedException e) {
            LifeTrakLogger.error(e.getMessage());
        } catch (ExecutionException e) {
            LifeTrakLogger.error(e.getMessage());
        }
        return SALStatus.ERROR_NOT_INITIALIZED;
    }

    public void stopCallables(){
        try{
            executorService.shutdownNow();
            future.cancel(true);
            executorService = Executors.newSingleThreadExecutor();
        }
        catch (Exception e){

        }
    }

    private static boolean isDataPointComplete(long dataHeaderId) {
        String query = "select count(_id) from StatisticalDataPoint where dataHeaderAndPoint = ?";

        Cursor cursor = DataSource.getInstance(context)
                .getReadOperation()
                .rawQuery(query, String.valueOf(dataHeaderId));

        if (cursor.moveToFirst() && cursor.getCount() >= 144) {
            return true;
        }

        return false;
    }

    private static void insertStatisticalDataHeaderWithWatch(Watch watch, List<StatisticalDataHeader> dataHeaders){

        int index = 0;
        for (StatisticalDataHeader dataHeader : dataHeaders) {
            dataHeader.setContext(context);
            dataHeader.setWatch(watch);

            if (dataHeader.getId() == 0) {
                dataHeader.setStatisticalDataPoints(mStatisticalDataPoints.get(index));
                dataHeader.insert();
            }
            {
                dataHeader.update();


            }
            String query = "select _id from StatisticalDataPoint where dataHeaderAndPoint = ?";

            Cursor cursor = DataSource.getInstance(context)
                    .getReadOperation()
                    .rawQuery(query, String.valueOf(dataHeader.getId()));

            int dataPointCount = 0;

            if (cursor.moveToFirst())
                dataPointCount = cursor.getCount() - 1;

            cursor.close();

            List<StatisticalDataPoint> dataPoints = mStatisticalDataPoints.get(index);

            List<StatisticalDataPoint> lastDataPoints = DataSource.getInstance(context)
                    .getReadOperation()
                    .query("dataHeaderAndPoint = ?", String.valueOf(dataHeader.getId()))
                    .orderBy("_id", SalutronLifeTrakUtility.SORT_DESC)
                    .getResults(StatisticalDataPoint.class);

            for (int i=dataPointCount;i<dataPoints.size();i++) {
                StatisticalDataPoint dataPoint = dataPoints.get(i);
                dataPoint.setContext(context);
                dataPoint.setStatisticalDataHeader(dataHeader);

                if (i == dataPointCount && lastDataPoints.size() > 0) {
                    StatisticalDataPoint lastDataPoint = lastDataPoints.get(0);
                    lastDataPoint.setContext(context);
                    lastDataPoint.setStatisticalDataHeader(dataHeader);
                    lastDataPoint.setAverageHR(dataPoint.getAverageHR());
                    lastDataPoint.setDistance(dataPoint.getDistance());
                    lastDataPoint.setSteps(dataPoint.getSteps());
                    lastDataPoint.setCalorie(dataPoint.getCalorie());
                    lastDataPoint.setSleepPoint02(dataPoint.getSleepPoint02());
                    lastDataPoint.setSleepPoint24(dataPoint.getSleepPoint24());
                    lastDataPoint.setSleepPoint46(dataPoint.getSleepPoint46());
                    lastDataPoint.setSleepPoint68(dataPoint.getSleepPoint68());
                    lastDataPoint.setSleepPoint810(dataPoint.getSleepPoint810());
                    lastDataPoint.setDominantAxis(dataPoint.getDominantAxis());
                    lastDataPoint.setLux(dataPoint.getLux());
                    lastDataPoint.setAxisDirection(dataPoint.getAxisDirection());
                    lastDataPoint.setAxisMagnitude(dataPoint.getAxisMagnitude());
                    lastDataPoint.update();
                } else {
                    dataPoint.setStatisticalDataHeader(dataHeader);
                    dataPoint.setContext(context);
                    dataPoint.insert();
                }
            }

            index++;
        }
    }

    public static void save() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                watch = lifeTrakApplication.getSelectedWatch();

                if (watch == null) {
                    watch = new Watch();
                    watch.setContext(context);
                    watch.setName(bluetoothDevice.getName());
                    watch.setModel(SalutronLifeTrakUtility.WATCHMODEL_R420);
                    watch.setMacAddress(bluetoothDevice.getAddress());
                    watch.setWatchFirmWare(firmwareVersion);
                    watch.setWatchSoftWare(softwareRevision);
                    watch.setLastSyncDate(new Date());
                    watch.setCloudLastSyncDate(new Date());
                }


                LifeTrakSaveManager.getInstance(context)
                        .watch(watch)
                        .timeDate(timeDate)
                        .sleepDatabases(sleepDatabases)
                        .sleepSetting(sleepSetting)
                        .calibrationData(calibrationData)
                        .goals(goals())
                        .userProfile(userProfile)
                        .workoutSettings(workoutSettings)
                        .save();

                saveWorkout(workoutHeaders);

                if (lifeTrakApplication.getSelectedWatch() != null) {
                    insertStatisticalDataHeaderWithWatch(watch, statisticalDataHeaders);
                }
                else{
                    LifeTrakSaveManager.getInstance(context)
                            .watch(watch)
                            .statisticalDataHeaders(statisticalDataHeaders)
                            .saveStatisticalDataHeader();
                }

                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (salutronSDKCallback420 != null) {
                            if (lifeTrakApplication.getSelectedWatch() == null)
                            {
                            lifeTrakApplication.setSelectedWatch(watch);
                            lifeTrakApplication.setUserProfile(userProfile);
                            lifeTrakApplication.setTimeDate(timeDate);
                            }
                            salutronSDKCallback420.onSyncFinish();
                        }
                    }
                });
            }
        }).start();
    }

    //endregion

    //region Public Methods

    public void startScan() {
        if (salbleService != null) {
            registerHandler();
            int status = salbleService.startScan();

            if (status != SALStatus.NO_ERROR) {
                LifeTrakLogger.info("status: " + status);
            }
        }
    }

    public void startSync() {
        if (salbleService != null) {
            headerIndex = 0;
            calibrationType = SALCalibration.AUTO_EL_SETTING;
            calibrationIndex = 0;
            bluetoothDevices.clear();
            headerIndexes.clear();
            mWorkoutStopInfos.clear();
            statisticalDataHeaders.clear();
            mStatisticalDataPoints.clear();
            workoutHeaders.clear();
            sleepDatabases.clear();

            registerHandler();

            callables.clear();
            callables.add(callableGetTime);
            callables.add(callableDeviceFirmware);
            callables.add(callableSoftwareRevision);
            callables.add(callableGetDataHeader);
            //callables.add(callableGetDataPoints);
            //callables.add(callableGetWorkoutInfo);
            callables.add(callableGetSleepDatabase);
            callables.add(callableGetSleepSetting);
            callables.add(callableGetStepGoal);
            callables.add(callableGetDistanceGoal);
            callables.add(callableGetCalorieGoal);
            callables.add(callableGetCalibrationData);
            callables.add(callableGetUserProfile);
            callables.add(callableWorkoutSettingsMaxUsage);
            callables.add(callableWorkoutSettingsUsage);

            executeCallable(callables.poll());

            if (salutronSDKCallback420 != null) {
                salutronSDKCallback420.onStartSync();
            }
        }
    }

    public void stopScan() {
        if (salbleService != null) {
            bluetoothDevices.clear();
            salbleService.stopScan();
        }
    }

    public int connectToDevice(String address, int model) {
        return salbleService.connectToDevice(address, model);
    }

    //endregion

    //region SDK Handler
    private static final Handler serviceHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            final Bundle data = message.getData();

            switch (message.what) {
                case SALBLEService.GATT_DEVICE_FOUND_MSG:
                    bluetoothDevice = data.getParcelable(BluetoothDevice.EXTRA_DEVICE);

                    boolean deviceFound = false;
                    String macAddress = bluetoothDevice.getAddress();

                    for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
                        if (bluetoothDevice.getAddress().equals(macAddress)) {
                            deviceFound = true;
                            break;
                        }
                    }

                    if (!deviceFound) {
                        bluetoothDevices.add(bluetoothDevice);
                        salbleService.stopScan();

                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (salutronSDKCallback420 != null) {
                                    salutronSDKCallback420.onDeviceFound(bluetoothDevice, data);
                                }
                            }
                        }, DELAY);
                    }
                    break;
                case SALBLEService.GATT_DEVICE_CONNECT_MSG:
                    bluetoothDevice = data.getParcelable(BluetoothDevice.EXTRA_DEVICE);

                    if (salutronSDKCallback420 != null) {
                        salutronSDKCallback420.onDeviceConnected(bluetoothDevice);
                    }
                    break;
                case SALBLEService.GATT_DEVICE_READY_MSG:
                    if (salutronSDKCallback420 != null) {
                        salutronSDKCallback420.onDeviceReady();
                    }
                    break;
                case SALBLEService.GATT_DEVICE_DISCONNECT_MSG:
                    if (salutronSDKCallback420 != null) {
                        salutronSDKCallback420.onDeviceDisconnected();
                    }
                    break;
                case SALBLEService.SAL_MSG_DEVICE_INFO:
                    final int devInfoType = data.getInt(SALBLEService.SAL_DEVICE_INFO_TYPE);
                    switch (devInfoType) {
                        case SALBLEService.DEV_INFO_FIRMWARE_VERSION:
                            onGetFirmware(data.getString(SALBLEService.SAL_DEVICE_INFO));
                            break;
                        case SALBLEService.DEV_INFO_SOFTWARE_VERSION:
                            onGetSoftwareRevision(data.getString(SALBLEService.SAL_DEVICE_INFO));
                            break;
                    }
                    break;
                case SALBLEService.SAL_MSG_DEVICE_DATA_RECEIVED:
                    int dataType = data.getInt(SALBLEService.SAL_DEVICE_DATA_TYPE);

                    switch (dataType) {
                        case SALBLEService.COMMAND_GET_TIME:
                            SALTimeDate salTimeDate = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);

                            onGetDateTime(salTimeDate);
                            break;
                        case SALBLEService.COMMAND_GET_STAT_DATA_HEADERS:
                            LifeTrakLogger.info("Start Of COMMAND_GET_STAT_DATA_HEADERS");
                            List<SALStatisticalDataHeader> salStatisticalDataHeaders = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
                            onGetStatisticalDataHeader(salStatisticalDataHeaders);
                            break;
                        case SALBLEService.COMMAND_GET_DATA_POINTS_FOR_DATE:
                            List<SALStatisticalDataPoint> salStatisticalDataPoints = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
                            onGetStatisticalDataPoint(salStatisticalDataPoints);
                            break;
                        case SALBLEService.COMMAND_GET_WORKOUT_DB:
                            LifeTrakLogger.info("Start Of COMMAND_GET_WORKOUT_DB");
                            List<SALDynamicWorkoutInfo> salDynamicWorkoutInfos = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
                            onGetWorkoutInfo(salDynamicWorkoutInfos);
                            break;
                        case SALBLEService.COMMAND_GET_WORKOUT_HEADER:
                            ArrayList<SALWorkoutHeader> workoutHeaderList  = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
                            onGetWorkoutHeader(workoutHeaderList);
                            break;

                        case SALBLEService.COMMAND_GET_WORKOUT_DB_INDEX:
                            List<SALDynamicWorkoutInfo> salDynamicWorkoutInfoss = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
                            onGetWorkoutInfo(salDynamicWorkoutInfoss);
                            break;

                        case SALBLEService.COMMAND_GET_SLEEP_DB:
                            List<SALSleepDatabase> salSleepDatabases = data.getParcelableArrayList(SALBLEService.SAL_DEVICE_DATA);
                            onGetSleepDatabase(salSleepDatabases);
                            break;
                        case SALBLEService.COMMAND_GET_SLEEP_SETTING:
                            SALSleepSetting salSleepSetting = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
                            onGetSleepSetting(salSleepSetting);
                            break;
                        case SALBLEService.COMMAND_GET_GOAL_STEPS:
                            long steps = data.getLong(SALBLEService.SAL_DEVICE_DATA);
                            onGetStepGoal(steps);
                            break;
                        case SALBLEService.COMMAND_GET_GOAL_DISTANCE:
                            double distance = (double) data.getLong(SALBLEService.SAL_DEVICE_DATA) / 100.0d;
                            onGetDistanceGoal(distance);
                            break;
                        case SALBLEService.COMMAND_GET_GOAL_CALORIE:
                            long calorie = data.getLong(SALBLEService.SAL_DEVICE_DATA);
                            onGetCalorieGoal(calorie);
                            break;
                        case SALBLEService.COMMAND_GET_CALIBRATION_DATA:
                            SALCalibration salCalibration = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
                            onGetCalibrationData(salCalibration);
                            break;
                        case SALBLEService.COMMAND_GET_USER_PROFILE:
                            SALUserProfile salUserProfile = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
                            onGetUserProfile(salUserProfile);
                            break;
                        case SALBLEService.COMMAND_GET_WORKOUT_SETTINGS:
                            SALWorkoutSetting salWorkoutSetting = data.getParcelable(SALBLEService.SAL_DEVICE_DATA);
                            if (salWorkoutSetting.getWorkoutSettingType() == SALWorkoutSetting.WORKOUT_COUNT){
                                onGetWorkoutCount(salWorkoutSetting.getWorkoutSetting());
                            }
                            else {
                                if (mCurrentWorkOutRequest == get_workout_settings_max_storage) {
                                    mCurrentWorkOutRequest = get_workout_settings_max_usage;
                                    onWorkoutMaxStorage(salWorkoutSetting);

                                } else if (mCurrentWorkOutRequest == get_workout_settings_max_usage) {
                                    mCurrentWorkOutRequest = get_workout_settings_logging_rate;
                                    onWorkoutMaxUsage(salWorkoutSetting);
                                } else if (mCurrentWorkOutRequest == get_workout_settings_logging_rate) {
                                    mCurrentWorkOutRequest = get_workout_settings_reconnect_timeout;
                                    onWorkoutLoggingRate(salWorkoutSetting);
                                } else if (mCurrentWorkOutRequest == get_workout_settings_reconnect_timeout) {
                                    mCurrentWorkOutRequest = get_workout_settings_max_storage;
                                    onWorkoutReconnectTimeOut(salWorkoutSetting);
                                }
                            }
                            break;
                    }
                    break;
            }
        }
    };
    //endregion

    //region Callables

    private static Callable<Integer> callableGetTime = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getCurrentTimeAndDate();
        }
    };

    private static Callable<Integer> callableDeviceFirmware = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getFirmwareRevision();
        }
    };

    private static Callable<Integer> callableSoftwareRevision = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getSoftwareRevision();
        }
    };


    private static Callable<Integer> callableGetDataHeader = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getStatisticalDataHeaders();
        }
    };

    private static Callable<Integer> callableGetDataPoints = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            int headerID = headerIndexes.remove(0);
            LifeTrakLogger.info("headerIndexes ON :" + headerID);
            return salbleService.getDataPointsOfSelectedDateStamp(headerID);
        }
    };

    private static Callable<Integer> callableGetWorkoutInfo = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getWorkoutDatabase();
        }
    };

    private static Callable<Integer> callableGetSleepDatabase = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getSleepDatabase();
        }
    };

    private static Callable<Integer> callableGetSleepSetting = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getSleepSetting();
        }
    };

    private static Callable<Integer> callableGetStepGoal = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getStepGoal();
        }
    };

    private static Callable<Integer> callableGetDistanceGoal = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getDistanceGoal();
        }
    };

    private static Callable<Integer> callableGetCalorieGoal = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getCalorieGoal();
        }
    };

    private static Callable<Integer> callableGetCalibrationData = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getCalibrationData(calibrationType);
        }
    };

    private static Callable<Integer> callableGetUserProfile = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getUserProfile();
        }
    };

    private static Callable<Integer> callableWorkoutSettingsMaxUsage = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getWorkoutDatabaseMaxUsage();
        }
    };

    private static Callable<Integer> callableWorkoutSettingsUsage = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getWorkoutDatabaseUsage();
        }
    };

    private static Callable<Integer> callableWorkoutSettingsLoggingRate = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getWorkoutHRLogRate();
        }
    };
    private static Callable<Integer> callableWorkoutSettingsReconnectTimeOut = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getReconnectTimeout();
        }
    };

    private static Callable<Integer> callableWorkoutCount = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return salbleService.getWorkoutCount();
        }
    };

    private static Callable<Integer> callableGetWorkoutHeader = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            LifeTrakSyncR420.workoutHeaderIndex = workoutHeaderIndexes.get(0);
            workoutHeaderIndexes.remove(0);
            LifeTrakLogger.info("workoutHeaderIndexes ON :" + workoutHeaderIndex);
            return salbleService.getWorkoutHeader(workoutHeaderIndex);
        }
    };

    private static Callable<Integer> callableGetWorkoutDatabaseForIndex = new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            LifeTrakSyncR420.workoutHeaderIndex = workoutValidIndex.get(0);
            workoutValidIndex.remove(0);
            LifeTrakLogger.info("workoutValidIndex ON :" + workoutHeaderIndex);
            return salbleService.getWorkoutDatabaseForIndex(workoutHeaderIndex);
        }
    };

    //endregion

    //region SDK Response Handler

    private static void onGetDateTime(SALTimeDate salTimeDate) {
        if (salutronSDKCallback420 != null) {
            salutronSDKCallback420.onSyncTime();
        }

        timeDate = TimeDate.buildTimeDate(context, salTimeDate);


        if (mSetFromSettings && lifeTrakApplication.getTimeDate() != null) {
            mSetFromSettings = false;
            LifeTrakLogger.info("Update mLifeTrakApplication Time from Settings");
            int dateFormat = lifeTrakApplication.getTimeDate().getDateFormat();
            int hourFormat = lifeTrakApplication.getTimeDate().getHourFormat();
            int displaySize = lifeTrakApplication.getTimeDate().getDisplaySize();
            timeDate.setDateFormat(dateFormat);
            timeDate.setHourFormat(hourFormat);
            timeDate.setDisplaySize(displaySize);
            lifeTrakApplication.setTimeDate(timeDate);

            return;
        }


        int delay = 750;

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
            delay = 2000;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mPreferenceWrapper.getPreferenceBooleanValue(AUTO_SYNC_TIME)) {
                    SALTimeDate mSALTimeDate = new SALTimeDate();
                    mSALTimeDate.setToNow();
                    mSALTimeDate.setDateFormat(timeDate.getDateFormat());
                    mSALTimeDate.setTimeFormat(timeDate.getHourFormat());
                    LifeTrakLogger.info("updateTimeAndDate on LifeTrakSyncR450");
                    salbleService.updateTimeAndDate(mSALTimeDate);

                }
            }
        }, delay);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                executeCallable(callables.poll());
            }
        }, delay * 2);


    }

    private static void onGetFirmware(String firmwareVersion) {
        Pattern pattern = Pattern.compile("[^0-9]");
        Matcher matcher = pattern.matcher(firmwareVersion);
        String number = matcher.replaceAll("");

        LifeTrakSyncR420.firmwareVersion = "V" +number;
        int delay = 750;

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
            delay = 2000;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                executeCallable(callables.poll());
            }
        }, delay);


    }

    private static void onGetSoftwareRevision(String softwareRevision) {
        Pattern pattern = Pattern.compile("[^0-9]");
        Matcher matcher = pattern.matcher(softwareRevision);
        String number = matcher.replaceAll("");

        LifeTrakSyncR420.softwareRevision = "V" +number;
        int delay = 750;

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
            delay = 2000;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                executeCallable(callables.poll());
            }
        }, delay);


    }


    private static void onGetStatisticalDataHeader(List<SALStatisticalDataHeader> salStatisticalDataHeaders) {
        headerIndexes.clear();
        statisticalDataHeaders.clear();
        mStatisticalDataPoints.clear();

        for (SALStatisticalDataHeader salStatisticalDataHeader : salStatisticalDataHeaders) {

            String datestamp = String.format("%d/%d/%d", salStatisticalDataHeader.datestamp.nMonth, salStatisticalDataHeader.datestamp.nDay, salStatisticalDataHeader.datestamp.nYear);
            LifeTrakLogger.info("Sync R420 watch data header = " + String.format("data header date: %s, steps: %d, distance: %f, calories:%f ", datestamp, salStatisticalDataHeader.totalSteps, salStatisticalDataHeader.totalDistance, salStatisticalDataHeader.totalCalorie));

            if (lifeTrakApplication.getSelectedWatch() != null) {
                long watchId = lifeTrakApplication.getSelectedWatch().getId();

                if (StatisticalDataHeader.isExists(context, watchId, salStatisticalDataHeader) &&
                        !isDataPointComplete(StatisticalDataHeader.getExistingDataHeader().getId())) {
                    StatisticalDataHeader dataHeader = StatisticalDataHeader.getExistingDataHeader();

                    SALDateStamp salDateStamp = salStatisticalDataHeader.datestamp;
                    Calendar calendar = Calendar.getInstance();

                    calendar.set(Calendar.DAY_OF_MONTH, salDateStamp.nDay);
                    calendar.set(Calendar.MONTH, salDateStamp.nMonth - 1);
                    calendar.set(Calendar.YEAR, salDateStamp.nYear + 1900);

                    dataHeader.setDateStamp(calendar.getTime());
                    dataHeader.setDateStampDay(salStatisticalDataHeader.datestamp.nDay);
                    dataHeader.setDateStampMonth(salStatisticalDataHeader.datestamp.nMonth);
                    dataHeader.setDateStampYear(salStatisticalDataHeader.datestamp.nYear);
                    dataHeader.setTimeStartSecond(salStatisticalDataHeader.timeStart.nSecond);
                    dataHeader.setTimeStartMinute(salStatisticalDataHeader.timeStart.nMinute);
                    dataHeader.setTimeStartHour(salStatisticalDataHeader.timeStart.nHour);
                    dataHeader.setTimeEndSecond(salStatisticalDataHeader.timeEnd.nSecond);
                    dataHeader.setTimeEndMinute(salStatisticalDataHeader.timeEnd.nMinute);
                    dataHeader.setTimeEndHour(salStatisticalDataHeader.timeEnd.nHour);
                    dataHeader.setAllocationBlockIndex(salStatisticalDataHeader.allocationBlockIndex);
                    dataHeader.setTotalSteps(salStatisticalDataHeader.totalSteps);
                    dataHeader.setTotalDistance(salStatisticalDataHeader.totalDistance);
                    dataHeader.setTotalSleep(salStatisticalDataHeader.totalSleep);
                    dataHeader.setMinimumBPM(salStatisticalDataHeader.minimumBPM);
                    dataHeader.setMaximumBPM(salStatisticalDataHeader.maximumBPM);
                    dataHeader.setLightExposure(salStatisticalDataHeader.lightExposure);




                    String query = "select count(_id) from StatisticalDataPoint where dataHeaderAndPoint = ?";

                    Cursor cursor = DataSource.getInstance(context)
                            .getReadOperation()
                            .rawQuery(query, String.valueOf(dataHeader.getId()));

                    if (cursor.moveToFirst()) {
                        int dataPointCount = cursor.getInt(0);

                        if (dataPointCount < 144) {
                            statisticalDataHeaders.add(dataHeader);
                            LifeTrakLogger.info("headerIndexes IS" + String.valueOf(salStatisticalDataHeaders.indexOf(salStatisticalDataHeader)) + " on Dataheader Date DAY: " + salStatisticalDataHeader.datestamp.nDay + " MONTH:"+ salStatisticalDataHeader.datestamp.nMonth + " YEAR:" +salStatisticalDataHeader.datestamp.nYear);
                            headerIndexes.add(salStatisticalDataHeaders.indexOf(salStatisticalDataHeader));
                        }
                    }


                } else {
                    statisticalDataHeaders.add(StatisticalDataHeader.buildStatisticalDataHeader(context, salStatisticalDataHeader));
                    LifeTrakLogger.info("headerIndexes IS" + String.valueOf(salStatisticalDataHeaders.indexOf(salStatisticalDataHeader)) + " on Dataheader Date DAY: " + salStatisticalDataHeader.datestamp.nDay + " MONTH:" + salStatisticalDataHeader.datestamp.nMonth + " YEAR:" + salStatisticalDataHeader.datestamp.nYear);
                    headerIndexes.add(salStatisticalDataHeaders.indexOf(salStatisticalDataHeader));
                }
            }
            else {
                statisticalDataHeaders.add(StatisticalDataHeader.buildStatisticalDataHeader(context, salStatisticalDataHeader));
                LifeTrakLogger.info("headerIndexes IS" + String.valueOf(salStatisticalDataHeaders.indexOf(salStatisticalDataHeader)) + " on Dataheader Date DAY: " + salStatisticalDataHeader.datestamp.nDay + " MONTH:" + salStatisticalDataHeader.datestamp.nMonth + " YEAR:" + salStatisticalDataHeader.datestamp.nYear);
                headerIndexes.add(salStatisticalDataHeaders.indexOf(salStatisticalDataHeader));
            }
        }
        if (headerIndexes.size() > 0){
            LifeTrakLogger.info("Start Of COMMAND_GET_DATA_POINTS_FOR_DATE");
            headerIndex = 0;
        if (headerIndexes.size() > 0){
            executeCallable(callableGetDataPoints);
        }
        else {
            Pattern pattern = Pattern.compile("[^0-9]");
            Matcher matcher = pattern.matcher(firmwareVersion);
            String number = matcher.replaceAll("");

            if (Integer.parseInt(number) > 210) {

                LifeTrakLogger.info("New r420 Firmware");
                executeCallable(callableWorkoutCount);
            }
            else{
                int status = executeCallable(callableGetWorkoutInfo);

                if (status == SALStatus.NO_ERROR) {
                    LifeTrakLogger.info("status is no error");
                } else {
                    LifeTrakLogger.error("status has error");
                }
            }
        }
    }
    }

    private static void onGetStatisticalDataPoint(List<SALStatisticalDataPoint> salStatisticalDataPoints) {
        List<StatisticalDataPoint> statisticalDataPoints = StatisticalDataPoint.buildStatisticalDataPoint(context, salStatisticalDataPoints);

        if (lifeTrakApplication.getSelectedWatch() == null) {
            statisticalDataHeaders.get(headerIndex).setStatisticalDataPoints(statisticalDataPoints);
        }
        else {
            mStatisticalDataPoints.add(statisticalDataPoints);
        }
        if (salutronSDKCallback420 != null) {
            float percent = (float) (headerIndex + 1) / (float) statisticalDataHeaders.size();
            percent = percent * 100.0f;
            salutronSDKCallback420.onSyncStatisticalDataPoint((int) percent);
        }

        if (headerIndexes.size() > 0) {
            for (SALStatisticalDataPoint salDataPoint : salStatisticalDataPoints) {
                LifeTrakLogger.info("Sync R420 watch datapoints = " + String.format("data points data: %s distance:%f calories:%f steps:%d", statisticalDataHeaders.get(headerIndex).getDateStamp().toString(),salDataPoint.distance,salDataPoint.calorie,salDataPoint.steps));
            }
            headerIndex++;

            executeCallable(callableGetDataPoints);
        } else {
            Pattern pattern = Pattern.compile("[^0-9]");
            Matcher matcher = pattern.matcher(firmwareVersion);
            String number = matcher.replaceAll("");

            if (Integer.parseInt(number) > 210) {

                LifeTrakLogger.info("New r420 Firmware");
                executeCallable(callableWorkoutCount);
            }
            else{
                int status = executeCallable(callableGetWorkoutInfo);

                if (status == SALStatus.NO_ERROR) {
                    LifeTrakLogger.info("status is no error");
                } else {
                    LifeTrakLogger.error("status has error");
                }
            }
        }
    }

    private static void onGetWorkoutCount(int workoutCount) {
        workoutHeaderIndexes.clear();
        workoutValidIndex.clear();
        LifeTrakSyncR420.workoutCount = workoutCount;

        int delay = 750;

        if (salutronSDKCallback420 != null) {
            salutronSDKCallback420.onSyncWorkoutDatabase();
        }

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
            delay = 2000;

        if (workoutCount > 0){
            for (int i = 0; i < workoutCount; i++){
                workoutHeaderIndexes.add(i);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    executeCallable(callableGetWorkoutHeader);
                }
            }, delay);
        }
        else
        {
            executeCallable(callables.poll());
        }




    }
    private static void onGetWorkoutHeader(List<SALWorkoutHeader> workoutHeaders) {
        for (SALWorkoutHeader workoutHeader : workoutHeaders) {
            WorkoutHeader workoutHeader1 = WorkoutHeader.buildWorkoutHeader(context, workoutHeader);
            WorkoutHeader mWorkoutHeader = workoutHeader1.isExistsWorkoutHeader();
            if (mWorkoutHeader == null){
                workoutValidIndex.add(workoutHeaderIndex);
            }
        }

        if (LifeTrakSyncR420.workoutHeaderIndexes.size() > 0){
            executeCallable(callableGetWorkoutHeader);
        }
        else{
            if (workoutValidIndex.size() > 0){
                executeCallable(callableGetWorkoutDatabaseForIndex);
            }
            else
            {
                executeCallable(callables.poll());
            }
        }
    }


    private static void onGetWorkoutInfo(List<SALDynamicWorkoutInfo> salDynamicWorkoutInfos) {
        Pattern pattern = Pattern.compile("[^0-9]");
        Matcher matcher = pattern.matcher(firmwareVersion);
        String number = matcher.replaceAll("");
        if (Integer.parseInt(number) > 0210) {

        }else{
            if (salDynamicWorkoutInfos == null && workoutInfoCounter < 3) {
                    executeCallable(callableGetWorkoutInfo);
                    workoutInfoCounter++;
                    return;
            }
        }

         workoutInfoCounter = 0;

        for (SALDynamicWorkoutInfo salDynamicWorkoutInfo : salDynamicWorkoutInfos) {
                WorkoutHeader workoutHeader = WorkoutHeader.buildWorkoutHeader(context, salDynamicWorkoutInfo.getHeaderInfo());
                List<WorkoutRecord> workoutRecords = WorkoutRecord.buildWorkoutRecord(context, salDynamicWorkoutInfo.getWorkoutRecords());
                workoutHeader.setWorkoutRecords(workoutRecords);


                List<WorkoutStopInfo> workoutStopInfos = WorkoutStopInfo.buildWorkoutStopInfo(context, salDynamicWorkoutInfo.getWorkoutStopDatabase());
                mWorkoutStopInfos.add(workoutStopInfos);
                workoutHeader.setWorkoutStopInfo(workoutStopInfos);
                workoutHeader.setHeaderHeartRate(java.util.Arrays.toString(salDynamicWorkoutInfo.getHRData()));
                workoutHeaders.add(workoutHeader);

                //int[] value = ;
       }


       if (salutronSDKCallback420 != null) {
                salutronSDKCallback420.onSyncWorkoutDatabase();
       }


       if (Integer.parseInt(number) > 0210) {
           if (workoutValidIndex.size() > 0){
               executeCallable(callableGetWorkoutDatabaseForIndex);
           }
           else
           {
               executeCallable(callables.poll());
           }
       }
       else{
           executeCallable(callables.poll());
       }

    }

    private static void onGetSleepDatabase(List<SALSleepDatabase> salSleepDatabases) {
        sleepDatabases.addAll(SleepDatabase.buildSleepDatabase(context, salSleepDatabases));

        if (salutronSDKCallback420 != null) {
            salutronSDKCallback420.onSyncSleepDatabase();
        }

        executeCallable(callables.poll());
    }

    private static void onGetSleepSetting(SALSleepSetting salSleepSetting) {
        sleepSetting = SleepSetting.buildSleepSetting(context, salSleepSetting);

        if (salutronSDKCallback420 != null) {
            salutronSDKCallback420.onSyncSleepSetting();
        }

        executeCallable(callables.poll());
    }

    private static void onGetStepGoal(long stepGoal) {
        LifeTrakSyncR420.stepGoal = stepGoal;

        if (salutronSDKCallback420 != null) {
            salutronSDKCallback420.onSyncStepGoal();
        }

        executeCallable(callables.poll());
    }

    private static void onGetDistanceGoal(double distanceGoal) {
        LifeTrakSyncR420.distanceGoal = distanceGoal;

        if (salutronSDKCallback420 != null) {
            salutronSDKCallback420.onSyncDistanceGoal();
        }

        executeCallable(callables.poll());
    }

    private static void onGetCalorieGoal(long calorieGoal) {
        LifeTrakSyncR420.calorieGoal = calorieGoal;

        if (salutronSDKCallback420 != null) {
            salutronSDKCallback420.onSyncCalorieGoal();
        }

        executeCallable(callables.poll());
    }

    private static void onGetCalibrationData(SALCalibration salCalibration) {
        if (calibrationData == null) {
            calibrationData = new CalibrationData();
        }

        if (calibrationIndex < 4) {
            switch (salCalibration.getCalibrationType()) {
                case SALCalibration.AUTO_EL_SETTING:
                    calibrationData.setAutoEL(salCalibration.getCalibrationValue());
                    calibrationType = SALCalibration.STEP_CALIBRATION;
                    break;
                case SALCalibration.STEP_CALIBRATION:
                    calibrationData.setStepCalibration(salCalibration.getCalibrationValue());
                    calibrationType = SALCalibration.WALK_DISTANCE_CALIBRATION;
                    break;
                case SALCalibration.WALK_DISTANCE_CALIBRATION:
                    calibrationData.setDistanceCalibrationWalk(salCalibration.getCalibrationValue());
                    calibrationType = SALCalibration.CALORIE_CALIBRATION;
                    break;
                case SALCalibration.CALORIE_CALIBRATION:
                    calibrationData.setCaloriesCalibration(salCalibration.getCalibrationValue());
                    break;
            }

            calibrationIndex++;
            if (salutronSDKCallback420 != null) {
                salutronSDKCallback420.onSyncCalibrationData();
            }

            executeCallable(callableGetCalibrationData);
        } else {
            executeCallable(callables.poll());
        }
    }

    private static void onGetUserProfile(SALUserProfile salUserProfile) {

        if (salutronSDKCallback420 != null) {
            salutronSDKCallback420.onSyncUserProfile();
        }

        userProfile = UserProfile.buildUserProfile(context, salUserProfile);
        executeCallable(callableWorkoutSettingsMaxUsage);

    }

    private static void onWorkoutMaxStorage(SALWorkoutSetting salWorkoutSetting) {

        workoutSettings = WorkoutSettings.buildWorkoutSettings(context, salWorkoutSetting);
        executeCallable(callableWorkoutSettingsUsage);

    }

    private static void onWorkoutMaxUsage(SALWorkoutSetting salWorkoutSetting) {
        if (workoutSettings != null)
            workoutSettings.setDatabaseUsage(salWorkoutSetting.getDatabaseUsage());
        executeCallable(callableWorkoutSettingsLoggingRate);


    }

    private static void onWorkoutLoggingRate(SALWorkoutSetting salWorkoutSetting) {
        if (workoutSettings != null)
            workoutSettings.setHrLoggingRate(salWorkoutSetting.getHRLoggingRate());
        executeCallable(callableWorkoutSettingsReconnectTimeOut);
    }

    private static void onWorkoutReconnectTimeOut(SALWorkoutSetting salWorkoutSetting) {
        if (workoutSettings != null)
            workoutSettings.setReconnectTime(salWorkoutSetting.getReconnectTimeout());
        save();
    }

    public static final boolean isWorkoutHeaderExists(Context context, long watchId, WorkoutHeader workoutHeader) {


        List<WorkoutHeader> workoutHeaderData = DataSource.getInstance(context)
                .getReadOperation()
                .query("watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and timeStampHour = ?",
                        String.valueOf(watchId), String.valueOf(workoutHeader.getDateStampDay()), String.valueOf(workoutHeader.getDateStampMonth()), String.valueOf(workoutHeader.getDateStampYear()) ,
                        String.valueOf(workoutHeader.getTimeStampHour()))
                .getResults(WorkoutHeader.class);

        if (workoutHeaderData.size() > 0) {
            mExistingWorkoutHeader = workoutHeaderData.get(0);
            return true;
        }
        return false;
    }

    private static void saveWorkout(List<WorkoutHeader> workoutHeaders){
        if (!ifExistOnTable("WorkoutStopInfo", "headerAndStop")){
            DataSource.getInstance(context).getWriteOperation().open().execQuery("ALTER TABLE WorkoutStopInfo ADD COLUMN headerAndStop INTEGER").close();
        }
        if (!ifExistOnTable("WorkoutHeader", "headerHeartRate")){
            DataSource.getInstance(context).getWriteOperation().open().execQuery("ALTER TABLE WorkoutHeader ADD COLUMN headerHeartRate TEXT").close();
        }
        String queryInsert = "";
        for (WorkoutHeader workoutHeader : workoutHeaders) {
            workoutHeader.setContext(context);
        if (lifeTrakApplication.getSelectedWatch() != null) {
            workoutHeader.setWatch(lifeTrakApplication.getSelectedWatch());

            WorkoutHeader mWorkoutHeader = workoutHeader.isExistsWorkoutHeader();
                if (mWorkoutHeader != null){
                    mWorkoutHeader.setContext(context);
                    mWorkoutHeader.setWatch(lifeTrakApplication.getSelectedWatch());
                    mWorkoutHeader.setTimeStampHour(workoutHeader.getTimeStampHour());
                    mWorkoutHeader.setTimeStampMinute(workoutHeader.getTimeStampMinute());
                    mWorkoutHeader.setTimeStampSecond(workoutHeader.getTimeStampSecond());
                    mWorkoutHeader.setDateStampDay(workoutHeader.getDateStampDay());
                    mWorkoutHeader.setDateStampMonth(workoutHeader.getDateStampMonth());
                    mWorkoutHeader.setDateStampYear(workoutHeader.getDateStampYear());
                    mWorkoutHeader.setHundredths(workoutHeader.getHundredths());
                    mWorkoutHeader.setSecond(workoutHeader.getSecond());
                    mWorkoutHeader.setMinute(workoutHeader.getMinute());
                    mWorkoutHeader.setHour(workoutHeader.getHour());
                    mWorkoutHeader.setDistance(workoutHeader.getDistance() / 100);
                    mWorkoutHeader.setCalories(workoutHeader.getCalories() / 10);
                    mWorkoutHeader.setSteps(workoutHeader.getSteps());
                    mWorkoutHeader.setCountSplitsRecord(workoutHeader.getCountSplitsRecord());
                    mWorkoutHeader.setCountStopsRecord(workoutHeader.getCountStopsRecord());
                    mWorkoutHeader.setCountHRRecord(workoutHeader.getCountHRRecord());
                    mWorkoutHeader.setCountTotalRecord(workoutHeader.getCountTotalRecord());
                    mWorkoutHeader.setAverageBPM(workoutHeader.getAverageBPM());
                    mWorkoutHeader.setMinimumBPM(workoutHeader.getMinimumBPM());
                    mWorkoutHeader.setMaximumBPM(workoutHeader.getMaximumBPM());
                    mWorkoutHeader.setStatusFlags(workoutHeader.getStatusFlags());
                    mWorkoutHeader.setLogRateHR(workoutHeader.getLogRateHR());
                    mWorkoutHeader.setAutoSplitType(workoutHeader.getAutoSplitType());
                    mWorkoutHeader.setZoneTrainType(workoutHeader.getZoneTrainType());
                    mWorkoutHeader.setUserMaxHR(workoutHeader.getUserMaxHR());
                    mWorkoutHeader.setZone0UpperHR(workoutHeader.getZone0UpperHR());
                    mWorkoutHeader.setZone1LowerHR(workoutHeader.getZone1LowerHR());
                    mWorkoutHeader.setZone2LowerHR(workoutHeader.getZone2LowerHR());
                    mWorkoutHeader.setZone3LowerHR(workoutHeader.getZone3LowerHR());
                    mWorkoutHeader.setZone4LowerHR(workoutHeader.getZone4LowerHR());
                    mWorkoutHeader.setZone5LowerHR(workoutHeader.getZone5LowerHR());
                    mWorkoutHeader.setZone5UpperHR(workoutHeader.getZone5UpperHR());
                    mWorkoutHeader.setSyncedToCloud(mWorkoutHeader.isSyncedToCloud());
                    mWorkoutHeader.update();

                    String query = "SELECT * FROM WorkoutHeader WHERE dateStampDay = ? and dateStampMonth = ? and dateStampYear =? " +
                            "and timeStampSecond = ? and timeStampMinute = ? and timeStampHour = ? and watchWorkoutHeader = ?";

                    Cursor cursor = DataSource.getInstance(context)
                            .getReadOperation()
                            .rawQuery(query, String.valueOf(workoutHeader.getDateStampDay()), String.valueOf(workoutHeader.getDateStampMonth()), String.valueOf(workoutHeader.getDateStampYear()),
                                    String.valueOf(workoutHeader.getTimeStampSecond()), String.valueOf(workoutHeader.getTimeStampMinute()), String.valueOf(workoutHeader.getTimeStampHour()), String.valueOf(watch.getId()));
                    List<WorkoutStopInfo> workoutStopInfos = workoutHeader.getWorkoutStopInfo();

                    if (cursor != null && cursor.getCount() > 0) {
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                            for (int i=0;i<workoutStopInfos.size();i++) {

                                WorkoutStopInfo workoutStopInfo = workoutStopInfos.get(i);
                                queryInsert = insertWorkoutStopQuery(workoutStopInfo,cursor.getInt(cursor.getColumnIndex("_id")));
                                DataSource.getInstance(context).getWriteOperation().open().execQuery(queryInsert);
                            }
                        }
                        cursor.close();
                    }
                }
                else {
                    workoutHeader.setCalories(workoutHeader.getCalories() / 10);
                    workoutHeader.setDistance(workoutHeader.getDistance() / 100);
                    workoutHeader.setSyncedToCloud(false);
                    workoutHeader.insert();

                    String query = "SELECT * FROM WorkoutHeader WHERE dateStampDay = ? and dateStampMonth = ? and dateStampYear =? " +
                            "and timeStampSecond = ? and timeStampMinute = ? and timeStampHour = ? and watchWorkoutHeader = ?";

                    Cursor cursor = DataSource.getInstance(context)
                            .getReadOperation()
                            .rawQuery(query, String.valueOf(workoutHeader.getDateStampDay()), String.valueOf(workoutHeader.getDateStampMonth()), String.valueOf(workoutHeader.getDateStampYear()),
                                    String.valueOf(workoutHeader.getTimeStampSecond()), String.valueOf(workoutHeader.getTimeStampMinute()), String.valueOf(workoutHeader.getTimeStampHour()), String.valueOf(watch.getId()));

                    List<WorkoutStopInfo> workoutStopInfos = workoutHeader.getWorkoutStopInfo();
                    if (cursor != null && cursor.getCount() > 0) {
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                            for (int i=0;i<workoutStopInfos.size();i++) {

                                WorkoutStopInfo workoutStopInfo = workoutStopInfos.get(i);
                                queryInsert = insertWorkoutStopQuery(workoutStopInfo,cursor.getInt(cursor.getColumnIndex("_id")));
                                DataSource.getInstance(context).getWriteOperation().open().execQuery(queryInsert);
                            }
                        }

                    }

                    cursor.close();
                    }
                }
           else {
            workoutHeader.setWatch(watch);
            workoutHeader.setCalories(workoutHeader.getCalories() / 10);
            workoutHeader.setDistance(workoutHeader.getDistance() / 100);
            workoutHeader.setSyncedToCloud(false);
            workoutHeader.insert();


            String query = "SELECT * FROM WorkoutHeader WHERE dateStampDay = ? and dateStampMonth = ? and dateStampYear =? " +
                    "and timeStampSecond = ? and timeStampMinute = ? and timeStampHour = ? and watchWorkoutHeader = ?";

            Cursor cursor = DataSource.getInstance(context)
                    .getReadOperation()
                    .rawQuery(query, String.valueOf(workoutHeader.getDateStampDay()), String.valueOf(workoutHeader.getDateStampMonth()), String.valueOf(workoutHeader.getDateStampYear()),
                            String.valueOf(workoutHeader.getTimeStampSecond()), String.valueOf(workoutHeader.getTimeStampMinute()), String.valueOf(workoutHeader.getTimeStampHour()), String.valueOf(watch.getId()));

            List<WorkoutStopInfo> workoutStopInfos = workoutHeader.getWorkoutStopInfo();

            if (cursor != null && cursor.getCount() > 0) {

                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    for (int i=0;i<workoutStopInfos.size();i++) {
                        WorkoutStopInfo workoutStopInfo = workoutStopInfos.get(i);
                        queryInsert = insertWorkoutStopQuery(workoutStopInfo,cursor.getInt(cursor.getColumnIndex("_id")));
                        DataSource.getInstance(context).getWriteOperation().open().execQuery(queryInsert);
                }
                }
                cursor.close();
            }


            }

        }

    }

    private static String insertWorkoutStopQuery(WorkoutStopInfo workoutStopInfo, int headerID){
        return "INSERT INTO WorkoutStopInfo (workoutHundreds, workoutSeconds, workoutMinutes, workoutHours, stopHundreds, stopSeconds, stopMinutes, stopHours, headerAndStop) " +
                "VALUES ("+ workoutStopInfo.getWorkoutHundreds() +", " +
                ""+workoutStopInfo.getWorkoutSeconds() +", " +
                ""+workoutStopInfo.getWorkoutMinutes()+", " +
                "" + workoutStopInfo.getWorkoutHours() + ", " +
                ""+ workoutStopInfo.getStopHundreds()+", " +
                ""+ workoutStopInfo.getStopSeconds() +", " +
                ""+ workoutStopInfo.getStopMinutes() +", " +
                ""+ workoutStopInfo.getStopHours() +", " +
                ""+ headerID +" )";
    }

    private static boolean ifExistOnTable(String mTable, String mColumn) {
        try{

            Cursor cursor = DataSource.getInstance(context)
                    .getReadOperation()
                    .rawQuery("SELECT * FROM " + mTable + " LIMIT 0", null);
            if(cursor.getColumnIndex(mColumn) != -1)
                return true;
            else
                return false;

        } catch (Exception Exp){
            Log.i("ifExistOnTable:", "" + Exp.getMessage());
            return false;
        }
    }
    public void setIsUpdateTimeAndDate(boolean mBoolean) {
        this.mSetFromSettings = mBoolean;

    }

    public void getCurrentTimeAndDate() {
        salbleService.getCurrentTimeAndDate();
    }

    public BluetoothDevice getConnectedDevice() {
        if (salbleService == null)
            return null;
        return salbleService.getConnectedDevice();
    }

    public SALBLEService getBLEService() {
        return salbleService;
    }

    //endregion
}