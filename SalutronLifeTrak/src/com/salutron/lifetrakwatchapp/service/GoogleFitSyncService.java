package com.salutron.lifetrakwatchapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.api.BatchResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;
import com.salutron.lifetrakwatchapp.util.GoogleFitHelper;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Queries data for the given watch and uploads the data to Google Fit
 *
 * @author Darwin Bautista
 */
public final class GoogleFitSyncService extends IntentService {

	/**
	 * The target Watch object (Parcelable extra)
	 */
	public static final String EXTRA_WATCH = "com.salutron.lifetrak.intent.extra.WATCH";

	/**
	 * Google API Client timeout in seconds
	 */
	private static final long TIMEOUT = 160 * 60L;

	private GoogleApiClient client;

	private List<SleepDatabase> sleepDatabases;
	private List<StatisticalDataHeader> dataHeaders;
	private List<WorkoutInfo> workoutInfos;
	private List<WorkoutHeader> workoutHeader;
	private UserProfile userProfile;

	private long timeOfLastSyncedData;

	/**
	 * Convenience method for starting the sync service
	 * @param context
	 * @param watch to sync data from
	 */
	public static void start(Context context, Watch watch) {
		final Intent serviceIntent = new Intent(context, GoogleFitSyncService.class);
		serviceIntent.putExtra(EXTRA_WATCH, watch);
		context.startService(serviceIntent);
	}

	public GoogleFitSyncService() {
		super(GoogleFitSyncService.class.getSimpleName());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		client = GoogleFitHelper.configureClient(new GoogleApiClient.Builder(getApplicationContext())).build();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final Watch watch = intent.getParcelableExtra(EXTRA_WATCH);
		try {
			queryData(watch);
			insertToGoogleFit(watch);
		}
		catch (Exception e) {
			LifeTrakLogger.info("Error" + e.getLocalizedMessage());
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		client.disconnect();
	}

	private void queryData(Watch watch) {
		timeOfLastSyncedData = watch.getGoogleFitLastSyncedDataTime();
		final String watchId = String.valueOf(watch.getId());

		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeOfLastSyncedData);
		// For good measure, also include data from the hour before
		calendar.add(Calendar.HOUR_OF_DAY, -1);
		final String lastSyncTimeSeconds = String.valueOf(calendar.getTimeInMillis() / 1000L);

		//---
		// The following queries will fetch all data for the given watch which have timestamps after timeOfLastSyncedData
		//---

		// Add 1 day to the computed end time to account for cases where sleep spans two days
		// i.e. start time > end time (this could happen because the end date is not tracked)
		sleepDatabases = DataSource.getInstance(getApplicationContext()).getReadOperation()
				.query("watchSleepDatabase = ? and datetime(" +
								"(dateStampYear + 1900) || '-' ||" +
								"substr('00' || dateStampMonth, -2, 2) || '-' ||" +
								"substr('00' || dateStampDay, -2, 2) || ' ' ||" +
								"substr('00' || hourSleepEnd, -2, 2) || ':' ||" +
								"substr('00' || minuteSleepEnd, -2, 2), '+1 day') >= date(?, 'unixepoch')",
						watchId, lastSyncTimeSeconds)
				.getResults(SleepDatabase.class);

		dataHeaders = DataSource.getInstance(getApplicationContext()).getReadOperation()
				.query("watchDataHeader = ? and date(" +
								"(dateStampYear + 1900) || '-' ||" +
								"substr('00' || dateStampMonth, -2, 2) || '-' ||" +
								"substr('00' || dateStampDay, -2, 2)) >= date(?, 'unixepoch')",
						watchId, lastSyncTimeSeconds)
				.orderBy("dateStampYear, dateStampMonth, dateStampDay", SalutronLifeTrakUtility.SORT_ASC)
				.getResults(StatisticalDataHeader.class, true);

		// Get all workouts with end times >= last sync time
		workoutInfos = DataSource.getInstance(getApplicationContext()).getReadOperation()
				.query("watchWorkoutInfo = ? and datetime(" +
								"(dateStampYear + 1900) || '-' ||" +
								"substr('00' || dateStampMonth, -2, 2) || '-' ||" +
								"substr('00' || dateStampDay, -2, 2) || ' ' ||" +
								"substr('00' || timeStampHour, -2, 2) || ':' ||" +
								"substr('00' || timeStampMinute, -2, 2) || ':' ||" +
								"substr('00' || timeStampSecond, -2, 2)," +
								"'+'||hour||' hours', '+'||minute||' minutes', '+'||second||' seconds') >= datetime(?, 'unixepoch')",
						watchId, lastSyncTimeSeconds)
				.getResults(WorkoutInfo.class, true);

		for (WorkoutInfo workoutInfo : workoutInfos){

			List<WorkoutStopInfo> workoutStopInfos = DataSource.getInstance(getApplicationContext())
					.getReadOperation()
					.query("infoAndStop = ?", String.valueOf(workoutInfo.getId()))
					.getResults(WorkoutStopInfo.class);
			workoutInfo.setWorkoutStopInfos(workoutStopInfos);
		}

		workoutHeader = DataSource.getInstance(getApplicationContext()).getReadOperation()
				.query("watchWorkoutHeader = ? and datetime(" +
								"(dateStampYear + 1900) || '-' ||" +
								"substr('00' || dateStampMonth, -2, 2) || '-' ||" +
								"substr('00' || dateStampDay, -2, 2) || ' ' ||" +
								"substr('00' || timeStampHour, -2, 2) || ':' ||" +
								"substr('00' || timeStampMinute, -2, 2) || ':' ||" +
								"substr('00' || timeStampSecond, -2, 2)," +
								"'+'||hour||' hours', '+'||minute||' minutes', '+'||second||' seconds') >= datetime(?, 'unixepoch')",
						watchId, lastSyncTimeSeconds)
				.getResults(WorkoutHeader.class, true);

		for (WorkoutHeader workoutHeader1 : workoutHeader){

			List<WorkoutStopInfo> workoutStopInfos = DataSource.getInstance(getApplicationContext())
					.getReadOperation()
					.query("infoAndStop = ?", String.valueOf(workoutHeader1.getId()))
					.getResults(WorkoutStopInfo.class);
			workoutHeader1.setWorkoutStopInfo(workoutStopInfos);
		}

		final List<UserProfile> profiles = DataSource.getInstance(getApplicationContext()).getReadOperation()
				.query("watchUserProfile = ?", watchId)
				.getResults(UserProfile.class);
		userProfile = profiles.size() > 0 ? profiles.get(0) : null;

		// Get new end time
		if (!dataHeaders.isEmpty()) {
			final StatisticalDataHeader lastDataHeader = dataHeaders.get(dataHeaders.size() - 1);
			timeOfLastSyncedData = lastDataHeader.getEndTime();
		}
	}

	/**
	 * Inserts the watch data to Google Fit.
	 * @param watch
	 */
	private void insertToGoogleFit(Watch watch) {
		// Make sure that client is connected before proceeding
		if (!client.blockingConnect(TIMEOUT, TimeUnit.SECONDS).isSuccess()) {
			return;
		}
		if(watch.getModel() == SalutronLifeTrakUtility.WATCHMODEL_R420){
			final GoogleFitHelper helper = new GoogleFitHelper(client, watch)
					.deleteDuplicateData(dataHeaders)
					.insertActivityDataR420(dataHeaders, getApplicationContext(), watch)
					.insertSleepData(sleepDatabases)
					.insertWorkoutHeaderData(workoutHeader);
			if (userProfile != null) {
				helper.insertProfileData(userProfile);
			}
			final BatchResult batchResult = helper.buildRequest().await(TIMEOUT, TimeUnit.SECONDS);
			if (batchResult.getStatus().isSuccess()) {
				watch.setGoogleFitLastSyncedDataTime(timeOfLastSyncedData);
			}
		}
		else {
			final GoogleFitHelper helper = new GoogleFitHelper(client, watch)
					.deleteDuplicateData(dataHeaders)
					.insertActivityData(dataHeaders)
					.insertSleepData(sleepDatabases)
					.insertWorkoutData(workoutInfos);
			if (userProfile != null) {
				helper.insertProfileData(userProfile);
			}
			final BatchResult batchResult = helper.buildRequest().await(TIMEOUT, TimeUnit.SECONDS);
			if (batchResult.getStatus().isSuccess()) {
				watch.setGoogleFitLastSyncedDataTime(timeOfLastSyncedData);
			}else{
				LifeTrakLogger.info("Error adding : "+ batchResult.getStatus());
			}
		}

	}
}
