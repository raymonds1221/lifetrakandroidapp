package com.salutron.lifetrakwatchapp.db;

import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.salutron.lifetrakwatchapp.model.WorkoutSettings;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;
import com.salutron.lifetrakwatchapp.model.BaseModel;
import com.salutron.lifetrakwatchapp.model.LightDataPoint;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;
import com.salutron.lifetrakwatchapp.model.WakeupSetting;
import com.salutron.lifetrakwatchapp.model.Notification;
import com.salutron.lifetrakwatchapp.model.ActivityAlertSetting;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.NightLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutRecord;
import com.salutron.lifetrakwatchapp.annotation.DataColumn;
import com.salutron.lifetrakwatchapp.annotation.DataTable;

/**
 * Database Helper
 * 
 * @author rsarmiento
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper implements SalutronLifeTrakUtility {
	private static final Object LOCK_OBJECT = DatabaseHelper.class;
	private static DatabaseHelper sDatabaseHelper;

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static DatabaseHelper getInstance(Context context) {
		synchronized(LOCK_OBJECT) {
			if(sDatabaseHelper == null)
				sDatabaseHelper = new DatabaseHelper(context);
			return sDatabaseHelper;
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			Set<Class<?>> classes = classesFromModels();

			for(Class<?> cls: classes) {
				createTableForClass(cls, db);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void createTableForClass(Class<?> cls, SQLiteDatabase db) {
		Annotation annotation = cls.getAnnotation(DataTable.class);
		StringBuffer sb = new StringBuffer();

		if(annotation != null) {
			DataTable dataTable = (DataTable) annotation;
			sb.append("CREATE TABLE IF NOT EXISTS " + dataTable.name() + "(");
			sb.append("_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,");

			Field[] fields = cls.getDeclaredFields();

			for(int i=0;i<fields.length;i++) {
				Field field = fields[i];

				Annotation annotationColumn = field.getAnnotation(DataColumn.class);

				if(annotationColumn != null) {
					DataColumn dataColumn = (DataColumn) annotationColumn;

					if(field.getType().isAssignableFrom(String.class)) {
						sb.append(dataColumn.name() + " TEXT NOT NULL");
					} else if(field.getType().isAssignableFrom(int.class) || 
							field.getType().isAssignableFrom(long.class) ||
							field.getType().isAssignableFrom(short.class) ||
							field.getType().isAssignableFrom(boolean.class) ||
							field.getType().isAssignableFrom(Date.class)) {
						sb.append(dataColumn.name() + " INTEGER NOT NULL DEFAULT 0");
					} else if(field.getType().isAssignableFrom(float.class) ||
							field.getType().isAssignableFrom(double.class)) {
						sb.append(dataColumn.name() + " REAL NOT NULL");
					} else if(field.getType().getSuperclass() != null &&
							field.getType().getSuperclass().isAssignableFrom(BaseModel.class) &&
							dataColumn.isPrimary() && !dataColumn.isForeign()) {
						sb.append(dataColumn.name() + " INTEGER");
					} else {
						continue;
					}

					sb.append(",\n");
				}
			}

			int index = sb.lastIndexOf(",");
			sb.replace(index, index+1, "");
			sb.append(");");

			db.execSQL(sb.toString());

			if(dataTable.name().equals("StatisticalDataHeader"))
				db.execSQL("CREATE INDEX IF NOT EXISTS idx_date_components ON StatisticalDataHeader (watchDataHeader,dateStampDay,dateStampMonth,dateStampYear);");
			if(dataTable.name().equals("StatisticalDataPoint"))
				db.execSQL("CREATE INDEX IF NOT EXISTS idx_dataHeaderAndPoint ON StatisticalDataPoint (dataHeaderAndPoint);");

			sb.append(");");
			db.execSQL(sb.toString());

			if(dataTable.name().equals("StatisticalDataHeader"))
				db.execSQL("CREATE INDEX IF NOT EXISTS idx_date_components ON StatisticalDataHeader (watchDataHeader,dateStampDay,dateStampMonth,dateStampYear);");
			if(dataTable.name().equals("StatisticalDataPoint"))
				db.execSQL("CREATE INDEX IF NOT EXISTS idx_dataHeaderAndPoint ON StatisticalDataPoint (dataHeaderAndPoint);");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (!ifExistOnTable(db, "WorkoutStopInfo", "headerAndStop"))
			db.execSQL("ALTER TABLE WorkoutStopInfo ADD COLUMN headerAndStop TEXT");

        if (!ifExistOnTable(db, "UserProfile", "firstname"))
            db.execSQL("ALTER TABLE UserProfile ADD COLUMN firstname TEXT");
        if (!ifExistOnTable(db, "UserProfile", "lastname"))
            db.execSQL("ALTER TABLE UserProfile ADD COLUMN lastname TEXT");
        if (!ifExistOnTable(db, "UserProfile", "email"))
            db.execSQL("ALTER TABLE UserProfile ADD COLUMN email TEXT");
        if (!ifExistOnTable(db, "UserProfile", "profile_image_web"))
            db.execSQL("ALTER TABLE UserProfile ADD COLUMN profile_image_web TEXT");
        if (!ifExistOnTable(db, "UserProfile", "profile_image_local"))
            db.execSQL("ALTER TABLE UserProfile ADD COLUMN profile_image_local TEXT");
        if (!ifExistOnTable(db, "UserProfile", "accessToken"))
            db.execSQL("ALTER TABLE UserProfile ADD COLUMN accessToken TEXT");

        if (!ifExistOnTable(db, "StatisticalDataPoint", "dataPointId"))
            db.execSQL("ALTER TABLE StatisticalDataPoint ADD COLUMN dataPointId INTEGER DEFAULT 0");

        if (!ifExistOnTable(db, "Watch", "cloudLastSyncDate"))
            db.execSQL("ALTER TABLE Watch ADD COLUMN cloudLastSyncDate INTEGER DEFAULT 0");
        if (!ifExistOnTable(db, "Watch", "accessToken"))
            db.execSQL("ALTER TABLE Watch ADD COLUMN accessToken TEXT");
        if (!ifExistOnTable(db, "Watch", "profileId"))
            db.execSQL("ALTER TABLE Watch ADD COLUMN profileId INTEGER");
		if (!ifExistOnTable(db, "Watch", "watchFirmWare"))
			db.execSQL("ALTER TABLE Watch ADD COLUMN watchFirmWare TEXT");
		if (!ifExistOnTable(db, "Watch", "watchSoftWare"))
			db.execSQL("ALTER TABLE Watch ADD COLUMN watchSoftWare TEXT");

        if (!ifExistOnTable(db, "StatisticalDataHeader", "minimumBPM"))
            db.execSQL("ALTER TABLE StatisticalDataHeader ADD COLUMN minimumBPM INTEGER");
        if (!ifExistOnTable(db, "StatisticalDataHeader", "maximumBPM"))
            db.execSQL("ALTER TABLE StatisticalDataHeader ADD COLUMN maximumBPM INTEGER");
        if (!ifExistOnTable(db, "StatisticalDataHeader", "lightExposure"))
            db.execSQL("ALTER TABLE StatisticalDataHeader ADD COLUMN lightExposure INTEGER");

        if (!ifExistOnTable(db, "StatisticalDataPoint", "wristOff02"))
            db.execSQL("ALTER TABLE StatisticalDataPoint ADD COLUMN wristOff02 INTEGER");
        if (!ifExistOnTable(db, "StatisticalDataPoint", "wristOff24"))
            db.execSQL("ALTER TABLE StatisticalDataPoint ADD COLUMN wristOff24 INTEGER");
        if (!ifExistOnTable(db, "StatisticalDataPoint", "wristOff46"))
            db.execSQL("ALTER TABLE StatisticalDataPoint ADD COLUMN wristOff46 INTEGER");
        if (!ifExistOnTable(db, "StatisticalDataPoint", "wristOff68"))
            db.execSQL("ALTER TABLE StatisticalDataPoint ADD COLUMN wristOff68 INTEGER");
        if (!ifExistOnTable(db, "StatisticalDataPoint", "wristOff810"))
            db.execSQL("ALTER TABLE StatisticalDataPoint ADD COLUMN wristOff810 INTEGER");
        if (!ifExistOnTable(db, "StatisticalDataPoint", "bleStatus"))
            db.execSQL("ALTER TABLE StatisticalDataPoint ADD COLUMN bleStatus INTEGER");
        if (!ifExistOnTable(db, "StatisticalDataHeader", "syncedToCloud"))
            db.execSQL("ALTER TABLE StatisticalDataHeader ADD COLUMN syncedToCloud INTEGER NOT NULL DEFAULT 0");

        if (!ifExistOnTable(db, "WorkoutInfo", "workoutId"))
            db.execSQL("ALTER TABLE WorkoutInfo ADD COLUMN workoutId INTEGER");

        if (!ifExistOnTable(db, "TimeDate", "displaySize"))
            db.execSQL("ALTER TABLE TimeDate ADD COLUMN displaySize INTEGER");

        if (!ifExistOnTable(db, "CalibrationData", "caloriesCalibration"))
            db.execSQL("ALTER TABLE CalibrationData ADD COLUMN caloriesCalibration TEXT");

        if (!ifExistOnTable(db, "Goal", "brightLightGoal"))
            db.execSQL("ALTER TABLE Goal ADD COLUMN brightLightGoal INTEGER");

		if (!ifExistOnTable(db, "UserProfile","password"))
			db.execSQL("ALTER TABLE UserProfile ADD COLUMN password TEXT");

		if (!ifExistOnTable(db, "SleepDatabase","syncedToCloud"))
			db.execSQL("ALTER TABLE SleepDatabase ADD COLUMN syncedToCloud INTEGER NOT NULL DEFAULT 0");


		if (!ifExistOnTable(db, "SleepDatabase","isWatch"))
			db.execSQL("ALTER TABLE SleepDatabase ADD COLUMN isWatch INTEGER NOT NULL DEFAULT 1");

		if (!ifExistOnTable(db, "SleepDatabase","isModified"))
			db.execSQL("ALTER TABLE SleepDatabase ADD COLUMN isModified INTEGER NOT NULL DEFAULT 0");

		if (!ifExistOnTable(db, "WorkoutHeader","syncedToCloud"))
			db.execSQL("ALTER TABLE WorkoutHeader ADD COLUMN syncedToCloud INTEGER NOT NULL DEFAULT 0");
		if (!ifExistOnTable(db, "WorkoutInfo","syncedToCloud"))
			db.execSQL("ALTER TABLE WorkoutInfo ADD COLUMN syncedToCloud INTEGER NOT NULL DEFAULT 0");

        createTableForClass(LightDataPoint.class, db);
        createTableForClass(WorkoutStopInfo.class, db);
        createTableForClass(WakeupSetting.class, db);
        createTableForClass(Notification.class, db);
        createTableForClass(ActivityAlertSetting.class, db);
        createTableForClass(DayLightDetectSetting.class, db);
        createTableForClass(NightLightDetectSetting.class, db);
		createTableForClass(WorkoutHeader.class, db);
		createTableForClass(WorkoutRecord.class, db);
		createTableForClass(WorkoutSettings.class, db);


        try {db.execSQL("CREATE INDEX IF NOT EXISTS idx_date_components ON StatisticalDataHeader (watchDataHeader,dateStampDay,dateStampMonth,dateStampYear);");} catch (Exception e) { }
        try {db.execSQL("CREATE INDEX IF NOT EXISTS idx_dataHeaderAndPoint ON StatisticalDataPoint (dataHeaderAndPoint);");}catch (Exception e){}
        try {db.execSQL("CREATE INDEX IF NOT EXISTS idx_dataHeaderAndLight ON LightDataPoint (dataHeaderAndPoint)");}catch (Exception e){}
    }

	private boolean ifExistOnTable(SQLiteDatabase mDatabase, String mTable, String mColumn) {
		try{
			Cursor mCursor  = mDatabase.rawQuery( "SELECT * FROM " + mTable + " LIMIT 0", null );
			if(mCursor.getColumnIndex(mColumn) != -1)
				return true;
			else
				return false;

		} catch (Exception Exp){
			Log.i("ifExistOnTable:","" + Exp.getMessage());
			return false;
		}
	}

	@Override
	public Object clone() {
		throw new UnsupportedOperationException("clone is not permitted");
	}

	/*
	 * Method for getting all classes in model package
	 */
	private Set<Class<?>> classesFromModels() throws ClassNotFoundException {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		String[] classNames = {
				"Watch", "StatisticalDataHeader", "StatisticalDataPoint", "Goal", 
				"WorkoutInfo", "SleepSetting", "SleepDatabase", "UserProfile", "TimeDate", 
				"CalibrationData", "SleepDatabaseDeleted", "LightDataPoint", "WorkoutStopInfo", 
				"WakeupSetting", "Notification", "ActivityAlertSetting", "DayLightDetectSetting", "NightLightDetectSetting", "WorkoutHeader", "WorkoutRecord", "WorkoutSettings"
		};

		for(String className: classNames) {
			Class<?> cls = Class.forName("com.salutron.lifetrakwatchapp.model." + className);
			classes.add(cls);
		}

		return classes;
	}
}
