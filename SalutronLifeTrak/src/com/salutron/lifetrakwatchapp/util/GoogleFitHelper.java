package com.salutron.lifetrakwatchapp.util;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Batch;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;

import com.google.common.primitives.Ints;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.StatisticalDataPoint;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Facilitates the conversion and upload of fitness data to Google Fit
 *
 * @author Darwin Bautista
 */
public final class GoogleFitHelper {

	private static final String MANUFACTURER = "Salutron";

	/**
	 * The Google Fit Activity type used for representing workout activities.
	 * Currently this is generic because the actual activity made by the user is not known.
	 */
	private static final String WORKOUT_ACTIVITY = FitnessActivities.OTHER;

	private static final String SESSION_WORKOUT_NAME = "Workout";
	private static final String SESSION_SLEEP_NAME = "Sleep";

	/**
	 * Unit conversion constants
	 */
	private static final float METERS_PER_KM = 1000.0f;
	private static final float METERS_PER_CM = 0.01f;
	private static final float KG_PER_LB = SalutronLifeTrakUtility.KG;

	private final GoogleApiClient client;
	private final Batch.Builder batchBuilder;
	private final Device device;

	private DataSource stepsDataSource;
	private DataSource distanceDataSource;
	private DataSource caloriesDataSource;
	private DataSource heartRateDataSource;
	private DataSource heightDataSource;
	private DataSource weightDataSource;
	private DataSource workoutSegmentsDataSource;

	/**
	 * Adds the Fitness-related configuration required by this helper to function properly
	 * @param builder
	 * @return builder
	 */
	public static GoogleApiClient.Builder configureClient(GoogleApiClient.Builder builder) {
		return builder.addApi(Fitness.HISTORY_API)
				.addApi(Fitness.SESSIONS_API)
				.addApi(Fitness.CONFIG_API)
				.addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
				.addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
				.addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE));
	}

	/**
	 * Convenience method for disabling Google Fit
	 * @param client
	 * @return
	 */
	public static PendingResult<Status> disable(GoogleApiClient client) {
		return Fitness.ConfigApi.disableFit(client);
	}

	/**
	 * Create new GoogleFitHelper object for the given client and watch
	 * @param client
	 * @param watch
	 */
	public GoogleFitHelper(GoogleApiClient client, Watch watch) {
		this.client = client;
		this.batchBuilder = new Batch.Builder(client);
		this.device = new Device(MANUFACTURER, watch.getName(), watch.getMacAddress(), Device.TYPE_WATCH);
	}

	/**
	 * Get the final request resulting from all the insert*Data operations performed beforehand
	 * @return Batch object containing all the PendingResults
	 */
	public Batch buildRequest() {
		return batchBuilder.build();
	}

	private DataSource createDataSource(String streamName, DataType dataType) {
		return new DataSource.Builder()
				.setAppPackageName(client.getContext())
				.setDevice(device)
				.setType(DataSource.TYPE_RAW)
				.setDataType(dataType)
				.setStreamName(streamName)
				.build();
	}

	// --- Data Sources ---

	private DataSource getStepsDataSource() {
		if (stepsDataSource == null) {
			stepsDataSource = createDataSource("Steps", DataType.TYPE_STEP_COUNT_DELTA);
		}
		return stepsDataSource;
	}

	private DataSource getDistanceDataSource() {
		if (distanceDataSource == null) {
			distanceDataSource = createDataSource("Distance", DataType.TYPE_DISTANCE_DELTA);
		}
		return distanceDataSource;
	}

	private DataSource getCaloriesDataSource() {
		if (caloriesDataSource == null) {
			caloriesDataSource = createDataSource("Calories", DataType.TYPE_CALORIES_EXPENDED);
		}
		return caloriesDataSource;
	}

	private DataSource getHeartRateDataSource() {
		if (heartRateDataSource == null) {
			heartRateDataSource = createDataSource("Heart Rate", DataType.TYPE_HEART_RATE_BPM);
		}
		return heartRateDataSource;
	}

	private DataSource getWeightDataSource() {
		if (weightDataSource == null) {
			weightDataSource = createDataSource("Weight", DataType.TYPE_WEIGHT);
		}
		return weightDataSource;
	}

	private DataSource getHeightDataSource() {
		if (heightDataSource == null) {
			heightDataSource = createDataSource("Height", DataType.TYPE_HEIGHT);
		}
		return heightDataSource;
	}

	private DataSource getWorkoutSegmentsDataSource() {
		if (workoutSegmentsDataSource == null) {
			workoutSegmentsDataSource = createDataSource("Workout Segments", DataType.TYPE_ACTIVITY_SEGMENT);
		}
		return workoutSegmentsDataSource;
	}

	public GoogleFitHelper deleteDuplicateData(List<StatisticalDataHeader> dataHeaders) {
		// Prevent data duplication by deleting existing data (if any) which overlaps the
		// time interval of the data to be inserted.
		if (dataHeaders.size() > 0) {
			long startTime = dataHeaders.get(0).getStartTime();
			long endTime = dataHeaders.get(dataHeaders.size() - 1).getEndTime();
			if (startTime > endTime) {
				startTime = dataHeaders.get(0).getStartTime();
				endTime = dataHeaders.get(0).getEndTime();
			}

			final DataDeleteRequest deleteRequest = new DataDeleteRequest.Builder()
					.addDataSource(getStepsDataSource())
					.addDataSource(getDistanceDataSource())
					.addDataSource(getCaloriesDataSource())
					.addDataSource(getHeartRateDataSource())
					.deleteAllSessions()
					.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
					.build();
			batchBuilder.add(Fitness.HistoryApi.deleteData(client, deleteRequest));
		}
		return this;
	}

	/**
	 * Utility method used by insertWorkoutData
	 */
	private static void addWorkoutSegment(DataSet workoutSegments, long segmentStartTime, long segmentEndTime) {
		try{
			long endtime = segmentEndTime;
			if (segmentStartTime > segmentEndTime){
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(segmentStartTime);
				calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR_OF_DAY) + 1);
				endtime = calendar.getTimeInMillis();
			}
		final DataPoint segmentDataPoint = workoutSegments.createDataPoint()
				.setTimeInterval(segmentStartTime, endtime, TimeUnit.MILLISECONDS);
		segmentDataPoint.getValue(Field.FIELD_ACTIVITY).setActivity(WORKOUT_ACTIVITY);
		workoutSegments.add(segmentDataPoint);
			LifeTrakLogger.info("Google Fit R50-C300-C410 Added Workout : " + segmentDataPoint + " at Date " + ( DateFormat.format("dd/MM/yyyy hh:mm:ss",segmentStartTime).toString()));
		}catch (Exception e){
			LifeTrakLogger.info("Google Fit R50-C300-C410 Error : " + e.getLocalizedMessage());
		}
	}

	public GoogleFitHelper insertWorkoutData(WorkoutInfo workout) {

		long startTime = workout.getStartTime();
		long endTime = workout.getEndTime();

		if (startTime > endTime) {
			int hr = workout.getTimeStampHour();
			int min = workout.getTimeStampMinute();
			int sec = workout.getTimeStampSecond();
			startTime = (hr * 3600) + (min * 60) + sec;

			endTime = startTime + workout.getHour() * 3600 + workout.getMinute() * 60 + workout.getSecond();
			if (workout.getWorkoutStopInfos() != null) {
				List<WorkoutStopInfo> workoutStops = new ArrayList<WorkoutStopInfo>();
				workoutStops = workout.getWorkoutStopInfos();
				workoutStops = filterDuplicateWorkoutStops(workoutStops);
				for (WorkoutStopInfo workoutStop : workoutStops) {
					endTime += (workoutStop.getStopHours() * 3600) + (workoutStop.getStopMinutes() * 60) + workoutStop.getStopSeconds();
				}
			}
		}
		final Session session = new Session.Builder()
				.setName(SESSION_WORKOUT_NAME)
				.setStartTime(startTime, TimeUnit.MILLISECONDS)
				.setEndTime(endTime, TimeUnit.MILLISECONDS)
				.setActivity(WORKOUT_ACTIVITY)
				.build();

		final SessionInsertRequest.Builder requestBuilder = new SessionInsertRequest.Builder()
				.setSession(session);

		final List<WorkoutStopInfo> workoutStops = workout.getWorkoutStopInfos();
		if (workoutStops != null && workoutStops.size() != 0) {

			final DataSet workoutSegments = DataSet.create(getWorkoutSegmentsDataSource());
			long segmentStartTime = startTime;

			for (final WorkoutStopInfo workoutStop : workoutStops) {
				// Workaround for DB-query bug. Make sure that parent workout is set.
				workoutStop.setWorkoutInfo(workout);
				// Get end time for current segment
				 long segmentEndTime = workoutStop.getPauseTime();

				if (segmentStartTime > segmentEndTime) {
					addWorkoutSegment(workoutSegments, segmentStartTime, workoutStop.getResumeTime());
				}
				else {
					addWorkoutSegment(workoutSegments, segmentStartTime, segmentEndTime);
				}
				// Get start time for next segment
				segmentStartTime = workoutStop.getResumeTime();
			}

			// Add the last workout segment, if any
			if (segmentStartTime != startTime) {
				addWorkoutSegment(workoutSegments, segmentStartTime, endTime);
			}

			requestBuilder.addDataSet(workoutSegments);
		}

		batchBuilder.add(Fitness.SessionsApi.insertSession(client, requestBuilder.build()));

		return this;
	}

	public GoogleFitHelper insertWorkoutDataHeader(WorkoutHeader workout) {

		long startTime = getStartTime(workout);
		long endTime = getEndTime(workout);

		if (startTime > endTime) {
			int hr = workout.getTimeStampHour();
			int min = workout.getTimeStampMinute();
			int sec = workout.getTimeStampSecond();
			startTime = (hr * 3600) + (min * 60) + sec;

			endTime = startTime + workout.getHour() * 3600 + workout.getMinute() * 60 + workout.getSecond();
			if (workout.getWorkoutStopInfo() != null) {
				List<WorkoutStopInfo> workoutStops = new ArrayList<WorkoutStopInfo>();
				workoutStops = workout.getWorkoutStopInfo();
				workoutStops = filterDuplicateWorkoutStops(workoutStops);
				for (WorkoutStopInfo workoutStop : workoutStops) {
					endTime += (workoutStop.getStopHours() * 3600) + (workoutStop.getStopMinutes() * 60) + workoutStop.getStopSeconds();
				}
			}
		}
		final Session session = new Session.Builder()
				.setName(SESSION_WORKOUT_NAME)
				.setStartTime(startTime, TimeUnit.MILLISECONDS)
				.setEndTime(endTime, TimeUnit.MILLISECONDS)
				.setActivity(WORKOUT_ACTIVITY)
				.build();

		final SessionInsertRequest.Builder requestBuilder = new SessionInsertRequest.Builder()
				.setSession(session);

		final List<WorkoutStopInfo> workoutStops = workout.getWorkoutStopInfo();
		if (workoutStops != null && workoutStops.size() != 0) {

			final DataSet workoutSegments = DataSet.create(getWorkoutSegmentsDataSource());
			long segmentStartTime = startTime;

			for (final WorkoutStopInfo workoutStop : workoutStops) {
				// Workaround for DB-query bug. Make sure that parent workout is set.
				workoutStop.setWorkoutHeader(workout);
				// Get end time for current segment
				long segmentEndTime = workoutStop.getPauseTime();

				if (segmentStartTime > segmentEndTime) {
					addWorkoutSegment(workoutSegments, segmentStartTime, workoutStop.getResumeTime());
				}
				else {
					addWorkoutSegment(workoutSegments, segmentStartTime, segmentEndTime);
				}
				// Get start time for next segment
				segmentStartTime = workoutStop.getResumeTime();
			}

			// Add the last workout segment, if any
			if (segmentStartTime != startTime) {
				addWorkoutSegment(workoutSegments, segmentStartTime, endTime);
			}

			requestBuilder.addDataSet(workoutSegments);
		}

		batchBuilder.add(Fitness.SessionsApi.insertSession(client, requestBuilder.build()));

		return this;
	}

	private int getStartTime(WorkoutHeader workoutheader){
		int hr = workoutheader.getTimeStampHour();
		int min = workoutheader.getTimeStampMinute();
		int sec = workoutheader.getTimeStampSecond();
		return (hr*3600) + (min*60) + sec;
	}

	private int getEndTime(WorkoutHeader workoutHeader){
		int endTime = getStartTime(workoutHeader) + workoutHeader.getHour()*3600 + workoutHeader.getMinute()*60 + workoutHeader.getSecond();

		List<WorkoutStopInfo> workoutStops = workoutHeader.getWorkoutStopInfo();

				synchronized (workoutStops) {
					if (workoutStops.size() != 0) {
						workoutStops = filterDuplicateWorkoutStops(workoutStops);
						for (WorkoutStopInfo workoutStop : workoutStops) {
							endTime += (workoutStop.getStopHours() * 3600) + (workoutStop.getStopMinutes() * 60) + workoutStop.getStopSeconds();
						}
		}


		}



		return endTime;
	}


	private List<WorkoutStopInfo> filterDuplicateWorkoutStops(List<WorkoutStopInfo> workoutStops){
		List<WorkoutStopInfo> filteredWorkoutStops = new ArrayList<WorkoutStopInfo>();
		if (workoutStops.size() > 0){
			filteredWorkoutStops.add(workoutStops.get(0));
		}
		for (WorkoutStopInfo workoutStop : workoutStops) {
			if(filteredWorkoutStops.size()>0){
				for (WorkoutStopInfo filteredWorkoutStop : filteredWorkoutStops) {
					if(workoutStop.getStopHours() == filteredWorkoutStop.getStopHours() &&
							workoutStop.getStopMinutes() == filteredWorkoutStop.getStopMinutes() &&
							workoutStop.getStopSeconds() == filteredWorkoutStop.getStopSeconds() &&
							workoutStop.getStopHundreds() == filteredWorkoutStop.getStopHundreds() &&
							workoutStop.getWorkoutHours() == filteredWorkoutStop.getWorkoutHours() &&
							workoutStop.getWorkoutMinutes() == filteredWorkoutStop.getWorkoutMinutes() &&
							workoutStop.getWorkoutSeconds() == filteredWorkoutStop.getWorkoutSeconds() &&
							workoutStop.getWorkoutHundreds() == filteredWorkoutStop.getWorkoutHundreds()){
						break;
					}
					else{
						filteredWorkoutStops.add(workoutStop);
					}
				}
			}
		}
		return filteredWorkoutStops;
	}

	/**
	 * Convenience method for inserting lists of workouts
	 * @param workouts
	 * @return helper instance
	 */
	public GoogleFitHelper insertWorkoutData(List<WorkoutInfo> workouts) {
		try{
			for (final WorkoutInfo workout : workouts) {
				insertWorkoutData(workout);
			}
		}
		catch (Exception e){

		}

		return this;
	}

	public GoogleFitHelper insertWorkoutHeaderData(List<WorkoutHeader> workoutsHeader) {
		try{
			for (final WorkoutHeader workout : workoutsHeader) {
				insertWorkoutDataHeader(workout);
			}
		}
		catch (Exception e){

		}

		return this;
	}

	/**
	 * Lightweight class for holding and combining data points into hourly aggregates
	 */
	private static final class HourlyAggregate {

		// Number of steps
		int steps;
		// Distance in km
		double distance;
		// Calories in kcal
		double calories;

		int numDataPoints;

		final Calendar calendar = Calendar.getInstance();

		void clear() {
			steps = 0;
			distance = 0.0;
			calories = 0.0;
			numDataPoints = 0;
		}

		void initialize(StatisticalDataHeader dataHeader) {
			calendar.setTimeInMillis(dataHeader.getStartTime());
			// Clear previous data
			clear();
		}

		long getEndTime() {
			return calendar.getTimeInMillis();
		}

		void add(StatisticalDataPoint dataPoint) {
			calories += dataPoint.getCalorie();
			steps += dataPoint.getSteps();
			distance += dataPoint.getDistance();
			// Each data point is 10 minutes worth of data
			calendar.add(Calendar.MINUTE, 10);
			numDataPoints++;
		}
		void add(WorkoutHeader dataPoint) {
			calories += dataPoint.getCalories();
			steps += dataPoint.getSteps();
			distance += dataPoint.getDistance();
			// Each data point is 10 minutes worth of data
			calendar.add(Calendar.MINUTE, 10);
			numDataPoints++;
		}

		boolean isComplete() {
			// Six data points are needed to complete an hour worth of data
			return numDataPoints == 6;
		}
	}

	public GoogleFitHelper insertActivityData(List<StatisticalDataHeader> dataHeaders) {

		final DataSet stepsDataSet = DataSet.create(getStepsDataSource());
		final DataSet distanceDataSet = DataSet.create(getDistanceDataSource());
		final DataSet caloriesDataSet = DataSet.create(getCaloriesDataSource());
		final DataSet heartRateDataSet = DataSet.create(getHeartRateDataSource());

		final HourlyAggregate hourlyAggregate = new HourlyAggregate();

		for (final StatisticalDataHeader dataHeader : dataHeaders) {
			// Get initial start time
			long startTime = dataHeader.getStartTime();
			long hrStartTime = startTime;
			hourlyAggregate.initialize(dataHeader);

			final List<StatisticalDataPoint> dataPoints = dataHeader.getStatisticalDataPoints();
			if (dataPoints != null) {
				if (dataPoints.size() < 144) {
					for (int i = dataPoints.size() - 1; i < 144; i++) {
						StatisticalDataPoint statisticalDataPoint = new StatisticalDataPoint();
						statisticalDataPoint.setAverageHR(0);
						statisticalDataPoint.setSteps(0);
						statisticalDataPoint.setDistance(0);
						statisticalDataPoint.setCalorie(0);
						dataPoints.add(statisticalDataPoint);
					}
				}
				for (int i = 0; i < dataPoints.size(); i++) {

					final StatisticalDataPoint dataPoint = dataPoints.get(i);
					hourlyAggregate.add(dataPoint);
					final long hrEndTime = hourlyAggregate.getEndTime();
					if (dataPoint.getAverageHR() > 0) {
						LifeTrakLogger.info("heartRateDataSet " + i + ": " + dataPoint.getAverageHR() + " on Date : " + (DateFormat.format("dd/MM/yyyy hh:mm:ss", hrStartTime).toString()));
						final DataPoint heartRateDataPoint = heartRateDataSet.createDataPoint()
								.setTimeInterval(hrStartTime, hrEndTime, TimeUnit.MILLISECONDS);
						heartRateDataPoint.getValue(Field.FIELD_BPM).setFloat(dataPoint.getAverageHR());
						heartRateDataSet.add(heartRateDataPoint);
					}

					hrStartTime = hrEndTime;

					if (hourlyAggregate.isComplete() || i == dataPoints.size() - 1) {

						final long endTime = hourlyAggregate.getEndTime();

						final DataPoint stepsDataPoint = stepsDataSet.createDataPoint()
								.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
						stepsDataPoint.getValue(Field.FIELD_STEPS).setInt(hourlyAggregate.steps);
						stepsDataSet.add(stepsDataPoint);

						final DataPoint distanceDataPoint = distanceDataSet.createDataPoint()
								.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
						distanceDataPoint.getValue(Field.FIELD_DISTANCE).setFloat((float) hourlyAggregate.distance * METERS_PER_KM);
						distanceDataSet.add(distanceDataPoint);

						final DataPoint caloriesDataPoint = caloriesDataSet.createDataPoint()
								.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
						caloriesDataPoint.getValue(Field.FIELD_CALORIES).setFloat((float) hourlyAggregate.calories);
						caloriesDataSet.add(caloriesDataPoint);

						hourlyAggregate.clear();
						startTime = endTime;
					}
				}
			}
			try {
				if (!stepsDataSet.isEmpty()) {
					batchBuilder.add(Fitness.HistoryApi.insertData(client, stepsDataSet));
				}
			} catch (Exception e) {
				LifeTrakLogger.info("Error on steps dataset : " + e.getLocalizedMessage());
			}
			try {
				if (!distanceDataSet.isEmpty()) {
					batchBuilder.add(Fitness.HistoryApi.insertData(client, distanceDataSet));
				}
			} catch (Exception e) {
				LifeTrakLogger.info("Error on distance dataset : " + e.getLocalizedMessage());
			}
			try {
				if (!caloriesDataSet.isEmpty()) {
					batchBuilder.add(Fitness.HistoryApi.insertData(client, caloriesDataSet));
				}
			} catch (Exception e) {
				LifeTrakLogger.info("Error on calories dataset : " + e.getLocalizedMessage());
			}
			try {
				if (!heartRateDataSet.isEmpty()) {
					batchBuilder.add(Fitness.HistoryApi.insertData(client, heartRateDataSet));
				}
			} catch (Exception e) {
				LifeTrakLogger.info("Error on heartRate dataset : " + e.getLocalizedMessage());
			}
		}
		return this;
	}

	public GoogleFitHelper insertActivityDataR420(List<StatisticalDataHeader> dataHeaders, Context context, Watch watch) {
		LifeTrakLogger.info("GOOGLE FIT R420 sync started ");
		final DataSet stepsDataSet = DataSet.create(getStepsDataSource());
		final DataSet distanceDataSet = DataSet.create(getDistanceDataSource());
		final DataSet caloriesDataSet = DataSet.create(getCaloriesDataSource());
		final DataSet heartRateDataSet = DataSet.create(getHeartRateDataSource());

		final HourlyAggregate hourlyAggregate = new HourlyAggregate();

		for (final StatisticalDataHeader dataHeader : dataHeaders) {
			// Get initial start time
			long startTime = dataHeader.getStartTime();
			long hrStartTime = startTime;
			hourlyAggregate.initialize(dataHeader);

			int day = dataHeader.getDateStampDay();
			int month = dataHeader.getDateStampMonth();
			int year = dataHeader.getDateStampYear();

			LifeTrakLogger.info("GOOGLE FIT dataheader at time year: " +
					(year + 1900) + " month: " + (month - 1) + " day: " + day);
			ArrayList<Integer> trueValuesHR = new ArrayList<Integer>();

			List<WorkoutHeader> workoutHeaders = com.salutron.lifetrakwatchapp.db.DataSource.getInstance(context)
					.getReadOperation()
					.query("watchWorkoutHeader == ? AND dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ?",
							String.valueOf(watch.getId()),
							String.valueOf(day), String.valueOf(month), String.valueOf(year))
					.getResults(WorkoutHeader.class);
			WorkoutHeader workoutHeader = null;
			if (workoutHeaders.size() > 0)
				workoutHeader = workoutHeaders.get(0);


			final List<StatisticalDataPoint> dataPoints = dataHeader.getStatisticalDataPoints();

			if (dataPoints != null) {
				for (int i = 0; i < dataPoints.size(); i++) {

					final StatisticalDataPoint dataPoint = dataPoints.get(i);
					hourlyAggregate.add(dataPoint);
					final long hrEndTime = hourlyAggregate.getEndTime();
					if (dataPoint.getAverageHR() > 0) {
						final DataPoint heartRateDataPoint = heartRateDataSet.createDataPoint()
								.setTimeInterval(hrStartTime, hrEndTime, TimeUnit.MILLISECONDS);
						heartRateDataPoint.getValue(Field.FIELD_BPM).setFloat(dataPoint.getAverageHR());
						heartRateDataSet.add(heartRateDataPoint);
						LifeTrakLogger.info("GOOGLE FIT dataheader at time year: " +
								(year + 1900) + " month: " + (month - 1) + " day: " + day
								+ " heartRateDataPoint per 10 mins = " + dataPoint.getAverageHR());
					}


					hrStartTime = hrEndTime;

					if (hourlyAggregate.isComplete() || i == dataPoints.size() - 1) {

						final long endTime = hourlyAggregate.getEndTime();

						final DataPoint stepsDataPoint = stepsDataSet.createDataPoint()
								.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
						stepsDataPoint.getValue(Field.FIELD_STEPS).setInt(hourlyAggregate.steps);
						stepsDataSet.add(stepsDataPoint);

						final DataPoint distanceDataPoint = distanceDataSet.createDataPoint()
								.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
						distanceDataPoint.getValue(Field.FIELD_DISTANCE).setFloat((float) hourlyAggregate.distance * METERS_PER_KM);
						distanceDataSet.add(distanceDataPoint);

						final DataPoint caloriesDataPoint = caloriesDataSet.createDataPoint()
								.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
						caloriesDataPoint.getValue(Field.FIELD_CALORIES).setFloat((float) hourlyAggregate.calories);
						caloriesDataSet.add(caloriesDataPoint);

						hourlyAggregate.clear();
						startTime = endTime;
					}
				}

				if (workoutHeader != null) {
					LifeTrakLogger.info("GOOGLE FIT workoutHeader is not null");

					String heartRate = workoutHeader.getHeaderHeartRate();
					int hrLogRate = workoutHeader.getLogRateHR();


					ArrayList<Integer> mHeartRateIndex = new ArrayList<Integer>();
					try {
						JSONArray jsonArray = new JSONArray(heartRate);
						if (jsonArray != null) {

							for (int i = 0; i < jsonArray.length(); ++i) {
								for (int y = 0; y < hrLogRate; y++) {
									trueValuesHR.add(jsonArray.optInt(i));
								}
							}


							int[] numbers = Ints.toArray(trueValuesHR);


							int startingTime = (workoutHeader.getTimeStampHour() * 3600) + (workoutHeader.getTimeStampMinute() * 60) + workoutHeader.getTimeStampSecond();
							int plotIndex = startingTime / 600;
							if ((startingTime % 600) != 0)
								plotIndex++;
							int startingTimeInTenMinutes = startingTime / 600;


							double sum10Min = 0;
							double average10Min = 0.0;


							int valueMinus = 0;
							int overAllMinus = 0;
							for (int a = 0; a < 144; a++) {
								if (a == startingTimeInTenMinutes) {
									valueMinus = ((startingTimeInTenMinutes + 1) * 600) - startingTime;
									if (mHeartRateIndex.size() == 0) {
										if (valueMinus > numbers.length) {
											mHeartRateIndex.add(numbers.length);
											break;
										} else {
											mHeartRateIndex.add(valueMinus);
											overAllMinus = overAllMinus + valueMinus;
											startingTimeInTenMinutes++;
										}

									} else {
										if (overAllMinus < numbers.length) {
											valueMinus = valueMinus - overAllMinus;
											mHeartRateIndex.add(valueMinus);
											overAllMinus = overAllMinus + valueMinus;
											startingTimeInTenMinutes++;
										} else {
											overAllMinus = overAllMinus - 600;
											if (overAllMinus > 0)
												if ((numbers.length - overAllMinus) < 60) {
													mHeartRateIndex.remove(mHeartRateIndex.size() - 1);
												} else {
													mHeartRateIndex.set((mHeartRateIndex.size() - 1), (numbers.length - overAllMinus));
												}
											else {

											}
											break;
										}

									}
								}
							}

							int valueOverAll = 0;
							int index = 0;
							final Calendar calendar = Calendar.getInstance();
							// Start hour, minute, and second are not updated properly. Ignore them.
							// Data headers always start at 12am (00:00:00.000) anyway.
							int minutevalue = 0;
							if ((workoutHeader.getMinute() % 10) != 0) {
								if (workoutHeader.getMinute() < 10) {
									minutevalue = 0;
								} else {
									minutevalue = workoutHeader.getMinute() - (workoutHeader.getMinute() % 10);
								}
							} else {
								minutevalue = workoutHeader.getMinute();
							}
							calendar.set(workoutHeader.getDateStampYear() + 1900, workoutHeader.getDateStampMonth() - 1, workoutHeader.getDateStampDay(),
									workoutHeader.getTimeStampHour(), minutevalue, workoutHeader.getSecond());
							calendar.set(Calendar.MILLISECOND, 0);

							long startTimeWorkout = calendar.getTimeInMillis();

							calendar.add(Calendar.MINUTE, 10);
							long endTimeWorkout = calendar.getTimeInMillis();
							for (int i = 0; i < mHeartRateIndex.size(); i++) {
								int valueEnding = mHeartRateIndex.get(i);

								int minValues = 240;
								int maxValues = 0;
								for (int y = 0; y < valueEnding; y++) {
									if (numbers[y + valueOverAll] > 0) {
										sum10Min = sum10Min + numbers[y + valueOverAll];
										if (minValues > numbers[y + valueOverAll]) {
											minValues = numbers[y + valueOverAll];
										}
										if (maxValues < numbers[y + valueOverAll]) {
											maxValues = numbers[y + valueOverAll];
										}
										index++;
									}
								}
								valueOverAll = valueOverAll + valueEnding;
								average10Min = sum10Min / index;

								if (average10Min > 0) {
									final DataPoint heartRateDataPoint = heartRateDataSet.createDataPoint()
											.setTimeInterval(startTimeWorkout, endTimeWorkout, TimeUnit.MILLISECONDS);
									heartRateDataPoint.getValue(Field.FIELD_BPM).setFloat((int) average10Min);
									heartRateDataSet.add(heartRateDataPoint);
									LifeTrakLogger.info("GOOGLE FIT workoutHeader at time year: " +
											(year + 1900) + " month: " + (month - 1) + " day: " + day
											+ " heartRateDataPoint per 10 mins = " + String.valueOf((int) average10Min));
								}
								startTimeWorkout = endTimeWorkout;
								calendar.add(Calendar.MINUTE, 10);
								endTimeWorkout = calendar.getTimeInMillis();
								average10Min = 0;
								sum10Min = 0;
								index = 0;
								plotIndex++;
							}
						}
					} catch (Exception e) {
						LifeTrakLogger.info("Exception at :" + e.getLocalizedMessage());
					}
				}
			}

			try {
				if (!stepsDataSet.isEmpty()) {
					batchBuilder.add(Fitness.HistoryApi.insertData(client, stepsDataSet));
				}
			} catch (Exception e) {
				LifeTrakLogger.info("Error on steps dataset : " + e.getLocalizedMessage());
			}
			try {
				if (!distanceDataSet.isEmpty()) {
					batchBuilder.add(Fitness.HistoryApi.insertData(client, distanceDataSet));
				}
			} catch (Exception e) {
				LifeTrakLogger.info("Error on distance dataset : " + e.getLocalizedMessage());
			}
			try {
				if (!caloriesDataSet.isEmpty()) {
					batchBuilder.add(Fitness.HistoryApi.insertData(client, caloriesDataSet));
				}
			} catch (Exception e) {
				LifeTrakLogger.info("Error on calories dataset : " + e.getLocalizedMessage());
			}
			try {
				if (!heartRateDataSet.isEmpty()) {
					batchBuilder.add(Fitness.HistoryApi.insertData(client, heartRateDataSet));
				}
			} catch (Exception e) {
				LifeTrakLogger.info("Error on heartRate dataset : " + e.getLocalizedMessage());
			}
		}

		return this;
	}


	public GoogleFitHelper insertSleepData(List<SleepDatabase> sleepDatabases) {
		// Nothing to do here...
		if (sleepDatabases.size() == 0) {
			return this;
		}

		for (final SleepDatabase sleepData : sleepDatabases) {
			try {
				long endtime = sleepData.getEndTime();
				long startTime = sleepData.getStartTime();
				if (sleepData.getStartTime() > sleepData.getEndTime()) {
					startTime = sleepData.getEndTime();
					endtime = sleepData.getStartTime();
				} else {

				}

				final Session session = new Session.Builder()
						.setName(SESSION_SLEEP_NAME)
						.setStartTime(startTime, TimeUnit.MILLISECONDS)
						.setEndTime(endtime, TimeUnit.MILLISECONDS)
						.setActivity(FitnessActivities.SLEEP)
						.build();

				final SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
						.setSession(session).build();

				batchBuilder.add(Fitness.SessionsApi.insertSession(client, insertRequest));
			}catch (Exception e){
				LifeTrakLogger.info("Error sleep:" + e.getLocalizedMessage());
			}
		}

		return this;
	}

	/**
	 * Insert the user's weight and height
	 * @param userProfile
	 * @return this helper
	 */
	public GoogleFitHelper insertProfileData(UserProfile userProfile) {
		// Just use the current time as timestamp
		final long timestamp = System.currentTimeMillis();

		// Height
		final DataSet heightDataSet = DataSet.create(getHeightDataSource());
		final DataPoint heightDataPoint = heightDataSet.createDataPoint()
				.setTimestamp(timestamp, TimeUnit.MILLISECONDS);
		// Convert height from centimeters to meters
		final float height = userProfile.getHeight() * METERS_PER_CM;
		heightDataPoint.getValue(Field.FIELD_HEIGHT).setFloat(height);
		heightDataSet.add(heightDataPoint);

		// Weight
		final DataSet weightDataSet = DataSet.create(getWeightDataSource());
		final DataPoint weightDataPoint = weightDataSet.createDataPoint()
				.setTimestamp(timestamp, TimeUnit.MILLISECONDS);
		// Convert weight from lb to kg
		final float weight = userProfile.getWeight() * KG_PER_LB;
		weightDataPoint.getValue(Field.FIELD_WEIGHT).setFloat(weight);
		weightDataSet.add(weightDataPoint);

		batchBuilder.add(Fitness.HistoryApi.insertData(client, weightDataSet));
		batchBuilder.add(Fitness.HistoryApi.insertData(client, heightDataSet));

		return this;
	}
}
