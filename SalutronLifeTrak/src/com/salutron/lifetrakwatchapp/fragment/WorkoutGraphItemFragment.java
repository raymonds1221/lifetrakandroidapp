package com.salutron.lifetrakwatchapp.fragment;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import roboguice.inject.InjectView;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.adapter.WorkoutGraphCellAdapter;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.StatisticalDataPoint;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;
import com.salutron.lifetrakwatchapp.view.GraphScrollView;

public class WorkoutGraphItemFragment extends BaseFragment implements OnItemClickListener{
	private int mDashboardType;

	private TextView mHeartRateValue;
	private TextView mStepsValue;
	private TextView mCaloriesValue;
	private TextView mDistanceValue;

	private TextView mWorkoutOutPosition;
	private TextView mHourValue;
	private TextView mMinuteValue;
	private TextView mSecondValue;
	private TextView mHourLabel;
	private TextView mMinuteLabel;
	private TextView mSecondLabel;
	private TextView mStartTimeValue;
	private TextView mEndTimeValue;

	private RadioGroup mToDateGroup;
	private FrameLayout mWorkoutGraphPlotContainer;
	private ImageView mPlayheadImage;

	private GraphScrollView mGraphScroll;
	private View mLeftView;

	private XYMultipleSeriesRenderer mMultipleSeriesRenderer;
	private XYMultipleSeriesDataset mMultipleSeriesDataset;
	private XYSeries mHeartRateSeries;
	private XYSeries mStepsSeries;
	private XYSeries mCaloriesSeries;
	private XYSeries mDistanceSeries;
	private XYSeriesRenderer mHeartRateRenderer;
	private XYSeriesRenderer mStepsRenderer;
	private XYSeriesRenderer mCaloriesRenderer;
	private XYSeriesRenderer mDistanceRenderer;
	private List<Double> mHeartRateValues;
	private List<Double> mStepsValues;
	private List<Double> mCaloriesValues;
	private List<Double> mDistanceValues;


	private List<Double> mHeartRateValuesComplete;
	private List<Double> mStepsValuesComplete;
	private List<Double> mCaloriesValuesComplete;
	private List<Double> mDistanceValuesComplete;
	List<Integer> mWorkoutPoints;
	List<Integer> mWorkoutStopPoints;

	private BarChart mBarChart;
	private GraphicalView mGraphView;

	private Date mDateNow;
	private Date mDateFrom;
	private Date mDateTo;

	private final DecimalFormat mDecimalFormat = new DecimalFormat("###,##0.00");
	private final DecimalFormat mDecimalFormat2 = new DecimalFormat("###,##0");
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private final String[] mHours = {"12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM", "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM", "8PM", "9PM", "10PM", "11PM"};

	private final Handler mHandlerDelay = new Handler();
	private double mScrollPosition;
	private int mCalendarMode = MODE_DAY;
	private final String DATE_FROM = "date_from";
	private final String DATE_TO = "date_to";

	private int mPosition;
	private int mGraphContainerWidth;

	//@InjectView(R.id.swtWorkout2) 
	private ViewSwitcher mWorkoutSwitcher;
	//@InjectView(R.id.listViewWorkOutGraphData) 
	private ListView mWorkoutList;
	private final List<WorkoutInfo> mWorkoutInfos = new ArrayList<WorkoutInfo>();
	private WorkoutGraphCellAdapter mAdapter;
	private MainActivity mMainActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		Log.d(TAG, "OnCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_workout_graph, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initializeViews(getView());
		initializeObjects();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		LayoutInflater inflater = LayoutInflater.from(getActivity());

		switch(newConfig.orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			changeFramentView(inflater, R.layout.fragment_workout_graph, (ViewGroup) getView());
			showActionBarAndCalendar();
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			changeFramentView(inflater, R.layout.fragment_workout_graph_land, (ViewGroup) getView());
			hideActionBarAndCalendar();
			break;
		}

		initializeViews(getView());
		initializeObjects();
		initializeDataOnOrientationChange();
	}

	private void initializeViews(View view){
		mHeartRateValue = (TextView) view.findViewById(R.id.textViewHeartRate);
		mStepsValue = (TextView) view.findViewById(R.id.textViewSteps);
		mCaloriesValue = (TextView) view.findViewById(R.id.textViewCalories);
		mDistanceValue = (TextView) view.findViewById(R.id.textViewDistance);

		mWorkoutOutPosition = (TextView) view.findViewById(R.id.textViewWorkOut);
		mHourValue = (TextView) view.findViewById(R.id.textViewMin);
		mMinuteValue = (TextView) view.findViewById(R.id.textViewSec);
		mSecondValue = (TextView) view.findViewById(R.id.textViewHund);
		mHourLabel = (TextView) view.findViewById(R.id.textView20);
		mMinuteLabel = (TextView) view.findViewById(R.id.textView22);
		mSecondLabel = (TextView) view.findViewById(R.id.textView21);
		if (orientation() == Configuration.ORIENTATION_PORTRAIT){
			mStartTimeValue = (TextView) view.findViewById(R.id.textViewStartTime);
			mEndTimeValue = (TextView) view.findViewById(R.id.textViewEndTime);
			mWorkoutList = (ListView) view.findViewById(R.id.listViewWorkOutGraphData);
			mWorkoutSwitcher = (ViewSwitcher) view.findViewById(R.id.swtWorkout2);
		}
		mWorkoutGraphPlotContainer = (FrameLayout) view.findViewById(R.id.workoutGraphPlotContainer);
		mGraphContainerWidth = 250;//mWorkoutGraphPlotContainer.getWidth();
		mPlayheadImage = (ImageView) view.findViewById(R.id.imgPlayheadWorkout);
		mLeftView = view.findViewById(R.id.viewGraphLeftPaddingWorkout);

		mGraphScroll = (GraphScrollView) view.findViewById(R.id.hsvGraphScrollWorkout);
		switch(orientation()) {
		case Configuration.ORIENTATION_PORTRAIT:
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			mGraphScroll.setGraphScrollViewListener(mGraphScrollViewListener);
			break;
		}
	}

	public void setWorkoutDataWithDay(Date date) {
		synchronized (LOCK_OBJECT) {
			Date now = new Date();
			/*
		if(date.after(now)) {
		//	getLifeTrakApplication().setCurrentDate(now);
		//	mMainActivity.setCalendarLabel(now);
			return;
		}
			 */
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);

			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH) + 1;
			int year = calendar.get(Calendar.YEAR) - 1900;

			List<WorkoutInfo> workoutInfos = DataSource.getInstance(getActivity())
					.getReadOperation()
					.query("watchWorkoutInfo == ? AND dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ?", 
							String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
							String.valueOf(day), String.valueOf(month), String.valueOf(year))
							.getResults(WorkoutInfo.class);



			List<WorkoutInfo> spillOverWorkoutInfos = getSpillOverWorkoutFromPreviousDay(date);
			if (spillOverWorkoutInfos != null && spillOverWorkoutInfos.size() > 0){
				workoutInfos.addAll(spillOverWorkoutInfos);
			}

			//add today's workout
			if(workoutInfos.size() > 0) {
				//filter spill over for next day
				workoutInfos = getWorkoutInfosWithoutNextDaySpillOver(workoutInfos);

				for (WorkoutInfo workoutInfo : workoutInfos){

					List<WorkoutStopInfo> workoutStopInfos = DataSource.getInstance(getActivity())
							.getReadOperation()
							.query("infoAndStop = ?", String.valueOf(workoutInfo.getId()))
							.getResults(WorkoutStopInfo.class);
					workoutInfo.setWorkoutStopInfos(workoutStopInfos);
				}

				mWorkoutInfos.clear();
				mWorkoutInfos.addAll(workoutInfos);
				mAdapter.notifyDataSetChanged();
				mWorkoutSwitcher.setDisplayedChild(0);
				//mWorkoutList.performItemClick(mWorkoutList, 0, mWorkoutList.getItemIdAtPosition(0));

				//get all values
				mWorkoutPoints = new ArrayList<Integer>();
				mWorkoutStopPoints = new ArrayList<Integer>();
				for (WorkoutInfo workoutInfo : mWorkoutInfos) {

					int startPosition = getWorkoutStartDataPointPosition(mWorkoutInfos.indexOf(workoutInfo));
					int endPosition = getWorkoutEndDataPointPosition(mWorkoutInfos.indexOf(workoutInfo));
					int hr = workoutInfo.getTimeStampHour();
					int min = workoutInfo.getTimeStampMinute();
					int sec = workoutInfo.getTimeStampSecond();
					if(workoutInfo.getWorkoutStopInfos() != null){
						for(WorkoutStopInfo workoutStop : workoutInfo.getWorkoutStopInfos()){
							int workoutStartSec = (hr*3600 + min*60 + sec) + workoutStop.getWorkoutHours()*3600 + workoutStop.getWorkoutMinutes()*60 + workoutStop.getWorkoutSeconds();
							int workoutStartPosition = workoutStartSec/600;
							int workoutEndPosition = (workoutStartSec + (workoutStop.getStopHours()*3600) + (workoutStop.getStopMinutes()*60 + workoutStop.getStopSeconds()))/600;
							if(workoutEndPosition > 143)
								workoutEndPosition = 143;
							workoutStartPosition++;
							while (workoutStartPosition < workoutEndPosition){
								if(!mWorkoutStopPoints.contains(workoutStartPosition))
									mWorkoutStopPoints.add(workoutStartPosition);
								workoutStartPosition++;
								if (workoutStartPosition == endPosition && (int)workoutStartSec/600 != workoutStartPosition){
									mWorkoutStopPoints.add(workoutStartPosition);
								}
							}
						}
					}

					if(endPosition > 143)
						endPosition = 143;
					while (startPosition <= endPosition){
						if(!mWorkoutPoints.contains(startPosition))
							mWorkoutPoints.add(startPosition);
						startPosition++;
					}
				}

			} else {
				mWorkoutSwitcher.setDisplayedChild(1);

				mHourValue.setText("00");
				mMinuteValue.setText("00");
				mSecondValue.setText("00");
				//mWorkoutOutPosition.setText("TIME");

				mStartTimeValue.setText("00:00:00");
				mEndTimeValue.setText("00:00:00");

				mHeartRateValue.setText("0");
				mStepsValue.setText("0");
				mCaloriesValue.setText("0");

				if(getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL) {
					mDistanceValue.setText("00 mi");
				} else {
					mDistanceValue.setText("0.00 km");
				}
			}
		}
		//setDataWithDay(date);
	}

	private List<WorkoutInfo> getWorkoutInfosWithoutNextDaySpillOver(List<WorkoutInfo> workoutInfos){
		List<WorkoutInfo> workouts = workoutInfos;
		for (WorkoutInfo workoutInfo : workoutInfos){
			WorkoutInfo lastWorkout = workoutInfo;
			int startTime = getStartTime(lastWorkout);
			int endTime = getEndTime(lastWorkout);
			if(endTime > 86400){
				//int startPosition = startTime/600;
				//int endPosition = endTime/600;
				lastWorkout.setHour((86399-startTime)/3600);
				lastWorkout.setMinute(((86399-startTime)%3600)/60);
				lastWorkout.setSecond((86399-startTime)%60);
				//				lastWorkout.setHundredths(0);
				workouts.remove(workoutInfos.indexOf(workoutInfo));
				workouts.add(lastWorkout);
				break;
			}
		}

		return workouts;
	}

	private List<WorkoutInfo> getSpillOverWorkoutFromPreviousDay(Date date){
		Calendar calYesterday = Calendar.getInstance();
		calYesterday.setTime(date);
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);

		int day = calYesterday.get(Calendar.DAY_OF_MONTH);
		int month = calYesterday.get(Calendar.MONTH) + 1;
		int year = calYesterday.get(Calendar.YEAR) - 1900;

		List<WorkoutInfo> workoutInfos = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchWorkoutInfo = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ?", 
						String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
						String.valueOf(year), String.valueOf(month), String.valueOf(day))
						.orderBy("timeStampHour, timeStampMinute, timeStampSecond", "")
						.getResults(WorkoutInfo.class);
		List<WorkoutInfo> spillOverWorkoutInfos = new ArrayList<WorkoutInfo>();

		if (workoutInfos != null && workoutInfos.size() > 0){
			for(WorkoutInfo workoutInfo : workoutInfos){
				int startTime = getStartTime(workoutInfo);
				int endTime = getEndTime(workoutInfo);
				int startPosition = startTime/600;
				int endPosition = endTime/600;
				if (endPosition >  143){

					Calendar midnight = Calendar.getInstance();
					midnight.setTime(date);
					midnight.set(Calendar.HOUR, 0);
					midnight.set(Calendar.MINUTE,0);
					midnight.set(Calendar.SECOND,0);
					midnight.set(Calendar.AM_PM, Calendar.AM);

					workoutInfo.setTimeStampHour(midnight.get(Calendar.HOUR_OF_DAY));
					workoutInfo.setTimeStampMinute(midnight.get(Calendar.MINUTE));
					workoutInfo.setTimeStampSecond(midnight.get(Calendar.SECOND));

					//86400
					//still needs to compute duration if workout has stop
					workoutInfo.setHour((endTime-86400)/3600);
					workoutInfo.setMinute(((endTime-86400)%3600)/60);
					workoutInfo.setSecond((endTime-86400)%60);
					spillOverWorkoutInfos.add(workoutInfo);

					break;
				}
			}
		}
		return spillOverWorkoutInfos;

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
		WorkoutInfo workoutInfo = mWorkoutInfos.get(position);

		//mWorkoutOutPosition.setText(position + 1 + " " + getString(R.string.time_caps));

		if (workoutInfo.getHour() > 0){
			mHourLabel.setText("hr");
			mMinuteLabel.setText("min");
			mSecondLabel.setText("sec");
			mHourValue.setText(String.format("%02d", workoutInfo.getHour()));
			mMinuteValue.setText(String.format("%02d", workoutInfo.getMinute()));
			mSecondValue.setText(String.format("%02d", workoutInfo.getSecond()));
		}
		else{
			mHourLabel.setText("min");
			mMinuteLabel.setText("sec");
			mSecondLabel.setText("hund");
			mHourValue.setText(String.format("%02d", workoutInfo.getMinute()));
			mMinuteValue.setText(String.format("%02d", workoutInfo.getSecond()));
			mSecondValue.setText(String.format("%02d", workoutInfo.getHundredths()));
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(workoutInfo.getDateStamp());
		calendar.set(Calendar.HOUR_OF_DAY, workoutInfo.getTimeStampHour());
		calendar.set(Calendar.MINUTE, workoutInfo.getTimeStampMinute());
		calendar.set(Calendar.SECOND, workoutInfo.getTimeStampSecond());

		if(getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_12_HR) {
			//viewHolder.workoutStartTime.setText(mDateFormat.format(calendar.getTime()));	
			mStartTimeValue.setText((mDateFormat.format(calendar.getTime())));
		} else {
			//viewHolder.workoutStartTime.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE)));
			mStartTimeValue.setText(String.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)));
		}

		int endTime = getEndTime(workoutInfo);
		if (endTime > 86399)
			endTime = 86399;
		calendar.set(Calendar.HOUR_OF_DAY, endTime/3600);
		calendar.set(Calendar.MINUTE, (endTime%3600)/60);
		calendar.set(Calendar.SECOND, endTime%60);

		if(getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_12_HR) {
			//viewHolder.workoutStartTime.setText(mDateFormat.format(calendar.getTime()));	
			mEndTimeValue.setText((mDateFormat.format(calendar.getTime())));
		} else {
			//viewHolder.workoutStartTime.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE)));
			mEndTimeValue.setText(String.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)));
		}
		int heartRate = getAverageHeartRate(workoutInfo);

		mHeartRateValue.setText(heartRate + ""); 
		mStepsValue.setText(String.valueOf(workoutInfo.getSteps()));
		mCaloriesValue.setText(String.format("%d", (int)workoutInfo.getCalories()));
		//mDistanceValue.setText(String.valueOf(mWorkoutInfos.get(position).getDistance()));

		if(getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL) {
			double distance = workoutInfo.getDistance() * ((workoutInfo.getFlags() == 1)?MILE:1);
			mDistanceValue.setText(String.format("%.02f", distance) + " mi");
		} else {
			double distance = workoutInfo.getDistance() * ((workoutInfo.getFlags() == 1)?1:MILE);
			mDistanceValue.setText(String.format("%.02f", distance) + " km");
		}
		//if(position == 1){
		plotWorkoutGraphs(position);
		//}
	}

	private int getAverageHeartRate(WorkoutInfo workoutInfo) {
		int heartRate = 0;
		int numWithHeartRateVals = 0;

		try {
			String query = "select averageHR from StatisticalDataPoint dp " +
					"inner join StatisticalDataHeader dh on dh._id = dp.dataHeaderAndPoint where " +
					"dateStampDay == ? and dateStampMonth == ? and dateStampYear == ? and watchDataHeader == ?";

			Cursor cursor = DataSource.getInstance(getActivity())
					.getReadOperation()
					.rawQuery(query, String.valueOf(workoutInfo.getDateStampDay()), String.valueOf(workoutInfo.getDateStampMonth()), String.valueOf(workoutInfo.getDateStampYear()),
							String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));

			while (cursor.moveToNext()) {
				if (cursor.getInt(cursor.getColumnIndex("averageHR")) != 0) {
					numWithHeartRateVals++;
				}
				heartRate += cursor.getInt(cursor.getColumnIndex("averageHR"));
			}

			if (numWithHeartRateVals > 0) {
				heartRate = heartRate / numWithHeartRateVals;
			} else {
				heartRate = 0;
			}
			cursor.close();
		} catch (ArithmeticException e) {
			e.printStackTrace();
		}
		return heartRate;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		int calendarMode = MODE_DAY;

		if(mDateFrom != null && mDateTo != null) {
			outState.putLong(DATE_FROM, mDateFrom.getTime());
			outState.putLong(DATE_TO, mDateTo.getTime());
		}
		outState.putInt(CALENDAR_MODE_KEY, calendarMode);
	}

	private void initializeObjects() {
		if(!isAdded())
			return;

		mAdapter = new WorkoutGraphCellAdapter(getActivity(), R.layout.adapter_workout_graph_cell, mWorkoutInfos);
		mWorkoutList.setAdapter(mAdapter);
		mWorkoutList.setOnItemClickListener(this);
		mMainActivity = (MainActivity) getActivity();
		mDateFormat.applyPattern("hh:mm:ss aa");
		//setWorkoutDataWithDay(getLifeTrakApplication().getCurrentDate());

		//mDecimalFormat.setRoundingMode(RoundingMode.DOWN);
		mDecimalFormat2.setRoundingMode(RoundingMode.DOWN);
		//mDateFormat.applyPattern("dd MMM");

		if(mHeartRateValues != null)
			mHeartRateValues.clear();
		if(mStepsValues != null)
			mStepsValues.clear();
		if(mCaloriesValues != null)
			mCaloriesValues.clear();
		if(mDistanceValues != null)
			mDistanceValues.clear();

		if(mHeartRateValuesComplete != null)
			mHeartRateValuesComplete.clear();
		if(mStepsValuesComplete != null)
			mStepsValuesComplete.clear();
		if(mCaloriesValuesComplete != null)
			mCaloriesValuesComplete.clear();
		if(mDistanceValuesComplete != null)
			mDistanceValuesComplete.clear();


		mHeartRateRenderer = new XYSeriesRenderer();
		mStepsRenderer = new XYSeriesRenderer();
		mCaloriesRenderer = new XYSeriesRenderer();
		mDistanceRenderer = new XYSeriesRenderer();

		mMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
		mMultipleSeriesDataset = new XYMultipleSeriesDataset();

		mHeartRateRenderer.setColor(getResources().getColor(R.color.color_heart_rate_bar));
		mStepsRenderer.setColor(getResources().getColor(R.color.color_steps_bar));
		mCaloriesRenderer.setColor(getResources().getColor(R.color.color_calorie_bar));
		mDistanceRenderer.setColor(getResources().getColor(R.color.color_distance_bar));

		mMultipleSeriesRenderer.setZoomEnabled(false, false);
		mMultipleSeriesRenderer.setApplyBackgroundColor(true);
		mMultipleSeriesRenderer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		mMultipleSeriesRenderer.setMarginsColor(getResources().getColor(R.color.color_gray));
		mMultipleSeriesRenderer.setLabelsColor(getResources().getColor(android.R.color.black));
		mMultipleSeriesRenderer.setYAxisMin(FITNESS_RESULTS_MIN_Y);
		mMultipleSeriesRenderer.setYAxisMax(FITNESS_RESULTS_MAX_Y);

		if(getArguments() != null) {
			mDashboardType = TYPE_CALORIES;// getArguments().getInt(DASHBOARD_TYPE);
			mPosition = getArguments().getInt(POSITION);

			MainActivity activity = (MainActivity) getActivity();

			switch(mCalendarMode) {
			case MODE_DAY:
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(getLifeTrakApplication().getCurrentDate());

				switch(mPosition) {
				case 0:
					calendar.add(Calendar.DAY_OF_MONTH, -1);
					break;
				case 2:
					calendar.add(Calendar.DAY_OF_MONTH, 1);
					break;
				}

				setDataWithDay(calendar.getTime());

				if(mPosition == 1) {
					activity.setCalendarLabel(calendar.getTime());
					activity.setCalendarPickerMode(MODE_DAY);
				}
				break;
			default:
				mPosition = getArguments().getInt(POSITION);
				calendar = Calendar.getInstance();
				calendar.setTime(getLifeTrakApplication().getCurrentDate());

				switch(mPosition) {
				case 0:
					calendar.add(Calendar.DAY_OF_MONTH, -1);
					break;
				case 2:
					calendar.add(Calendar.DAY_OF_MONTH, 1);
					break;
				}

				setDataWithDay(calendar.getTime());
			}
		}
		/*
		if(mMetricValueUnit != null) {
			if(mDashboardType == TYPE_DISTANCE) {
				if(getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL) {
					mMetricValueUnit.setText("mi");
				} else {
					mMetricValueUnit.setText("km");
				}
			} else {
				mMetricValueUnit.setText("");
			}
		}
		 */
	}

	private void initializeDataOnOrientationChange() {
		List<Double> values = new ArrayList<Double>();

		switch(mDashboardType) {
		case TYPE_HEART_RATE:
			values = mHeartRateValues;
			break;
		case TYPE_STEPS:
			values = mStepsValues;
			break;
		case TYPE_DISTANCE:
			values = mDistanceValues;
			break;
		case TYPE_CALORIES:
			values = mCaloriesValues;
			break;
		}
		initializeStats(mDashboardType, values, mDateNow, mDateNow);
		if(mWorkoutInfos.size() == 0){
			mCaloriesValues = null;
			mStepsValues = null;
			mHeartRateValues = null;
			mDistanceValues = null;
			mCaloriesValuesComplete = null;
			mStepsValuesComplete = null;
			mHeartRateValuesComplete = null;
			mDistanceValuesComplete = null;
		}

		initializeGraph(values, mDashboardType, mCalendarMode);
	}

	private void initializeStats(int dashboardType, List<Double> values, Date from, Date to) {
		if(!isAdded())
			return;

		double totalValue = 0;

		final Calendar calFrom = Calendar.getInstance();
		calFrom.setTime(from);

		while(calFrom.getTime().before(to) || calFrom.getTime().equals(to)) {
			int day = calFrom.get(Calendar.DAY_OF_MONTH);
			int month = calFrom.get(Calendar.MONTH) + 1;
			int year = calFrom.get(Calendar.YEAR) - 1900;

			String query = "select sum(distance) distance, sum(steps) steps, sum(calorie) calorie from StatisticalDataPoint sdp " +
					"inner join StatisticalDataHeader sdh on sdp.dataHeaderAndPoint = sdh._id " +
					"where watchDataHeader == ? AND dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ?";

			Cursor cursor = DataSource.getInstance(getActivity())
					.getReadOperation()
					.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
							String.valueOf(day), String.valueOf(month), String.valueOf(year));

			if (cursor.moveToFirst()) {
				switch (dashboardType) {
				case TYPE_STEPS:
					totalValue += cursor.getDouble(cursor.getColumnIndex("steps"));
					break;
				case TYPE_DISTANCE:
					totalValue += cursor.getDouble(cursor.getColumnIndex("distance"));
					break;
				case TYPE_CALORIES:
					totalValue += cursor.getDouble(cursor.getColumnIndex("calorie"));
					break;
				}
			}

			cursor.close();

			calFrom.add(Calendar.DAY_OF_MONTH, 1);
		}
	}

	public void setDataWithDay(Date date) {
		if(!isAdded())
			return;

		mDateNow = date;

		mCaloriesValues = null;
		mStepsValues = null;
		mHeartRateValues = null;
		mDistanceValues = null;
		mCaloriesValuesComplete = null;
		mStepsValuesComplete = null;
		mHeartRateValuesComplete = null;
		mDistanceValuesComplete = null;
		mWorkoutGraphPlotContainer.removeAllViews();
		mWorkoutInfos.clear();


		setWorkoutDataWithDay(mDateNow);


		resizeGraphContainer((int)dpToPx(5000));
		//int graphWidth = (int) dpToPx(sizeWithCalendarMode(MODE_DAY));
		//resizeGraphContainer(graphWidth);

		if(mWorkoutInfos.size() > 0){
			List<Double> values = createValuesForDay(date, mDashboardType);
			if (values.size() > 0){
				try{
					int startPosition = getWorkoutStartDataPointPosition(0);
					int endPosition = getWorkoutEndDataPointPosition(0);
					if (endPosition>143)
						endPosition = 143;
					values = values.subList(startPosition, endPosition);
					setDataForMetric(values);
					initializeStats(mDashboardType, values, date, date);
					initializeGraph(values, mDashboardType, MODE_DAY);
				}catch (Exception e){

				}
				/*
				if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
					mWorkoutList.performItemClick(mWorkoutList, 0, mWorkoutList.getItemIdAtPosition(0));
				}*/
			}
		}
	}

	private int getWorkoutStartDataPointPosition(int position){
		return getStartTime(mWorkoutInfos.get(position))/600;
	}

	private int getWorkoutEndDataPointPosition(int position){
		int endTime = getEndTime(mWorkoutInfos.get(position));
		if (endTime > 86399)
			endTime = 86399;
		int endDataPoint = (int)(endTime/600);
		return endDataPoint;
	}

	//in seconds
	private int getEndTime(WorkoutInfo workoutInfo){
		int endTime = getStartTime(workoutInfo) + workoutInfo.getHour()*3600 + workoutInfo.getMinute()*60 + workoutInfo.getSecond();
		if(workoutInfo.getWorkoutStopInfos() != null){
			List<WorkoutStopInfo> workoutStops = new ArrayList<WorkoutStopInfo>();
			workoutStops = workoutInfo.getWorkoutStopInfos();
			workoutStops = filterDuplicateWorkoutStops(workoutStops);
			for (WorkoutStopInfo workoutStop : workoutStops) {
				endTime += (workoutStop.getStopHours()*3600) + (workoutStop.getStopMinutes()*60) + workoutStop.getStopSeconds();
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

	//in seconds
	private int getStartTime(WorkoutInfo workoutInfo){
		int hr = workoutInfo.getTimeStampHour();
		int min = workoutInfo.getTimeStampMinute();
		int sec = workoutInfo.getTimeStampSecond();
		return (hr*3600) + (min*60) + sec;
	}

	private void setDataForMetric(List<Double> values) {
		switch(mDashboardType) {
		case TYPE_HEART_RATE:
			mHeartRateValues = values;
			break;
		case TYPE_STEPS:
			mStepsValues = values;
			break;
		case TYPE_DISTANCE:
			mDistanceValues = values;
			break;
		case TYPE_CALORIES:
			mCaloriesValues = values;
			break;
		}
	}

	private void resizeGraphContainer(int width) {
		if(mWorkoutGraphPlotContainer != null && orientation() == Configuration.ORIENTATION_LANDSCAPE) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, RelativeLayout.LayoutParams.MATCH_PARENT);
			params.setMargins(0, (int) dpToPx(10), 0, 0);
			params.addRule(RelativeLayout.RIGHT_OF, R.id.viewGraphLeftPaddingWorkout);
			mWorkoutGraphPlotContainer.setLayoutParams(params);
		}
	}

	private float widthOfBar() {
		float barWidth = 0f;

		if(orientation() == Configuration.ORIENTATION_LANDSCAPE){
			barWidth = 7f;
		}
		else{
			barWidth = mGraphContainerWidth/(mCaloriesValues.size()*4);
		}

		return dpToPx(barWidth);
	}

	private XYSeries createSeries(List<Double> values, String legend) {
		XYSeries xySeries = new XYSeries(legend);

		double maxValue = 0;

		if(values.size() > 0) {
			for(Double value : values) {
				maxValue = Math.max(maxValue, value);
			}
		}

		int x = 0;
		for(Number value : values) {
			double y = getYWithMaxValue(value.doubleValue(), maxValue, FITNESS_RESULTS_MIN_Y, FITNESS_RESULTS_MAX_Y);
			xySeries.add(x, y);
			x++;
		}

		return xySeries;
	}

	private List<Double> createValues(int type) {
		List<Double> values = new ArrayList<Double>();
		values = createValuesForDay(mDateNow, type);

		//set all non work out points to 0
		for (int i = 0; i < values.size(); i++) {
			if(mWorkoutPoints != null){
				if(!mWorkoutPoints.contains(i)){
					values.set(i, 0.0);
				}
			}
			if(mWorkoutStopPoints != null){
				if(mWorkoutStopPoints.contains(i)){
					values.set(i, 0.0);
				}
			}
		}

		return values;
	}

	private List<Double> createValuesForDay(Date date, int dashboardType) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		String query = "select " + columnWithDashboardType(dashboardType) + " from StatisticalDataPoint dataPoint " +
				"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
				"where dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and dataHeader.watchDataHeader = ?";

		Cursor cursor = DataSource.getInstance(getActivity())
				.getReadOperation()
				.rawQuery(query, String.valueOf(day), String.valueOf(month), String.valueOf(year),
						String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));

		List<Double> values = DataSource.getInstance(getActivity())
				.getReadOperation()
				.cursorToList(cursor);

		return values;
	}

	private void initializeGraph(List<Double> values, int type, int calendarMode) {
		if(!isAdded())
			return;

		mWorkoutGraphPlotContainer.removeAllViews();

		mMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
		mMultipleSeriesDataset = new XYMultipleSeriesDataset();

		mMultipleSeriesRenderer.setZoomEnabled(false, false);
		mMultipleSeriesRenderer.setYLabels(0);
		mMultipleSeriesRenderer.setShowGridY(false);
		mMultipleSeriesRenderer.setXLabels(0);
		mMultipleSeriesRenderer.clearXTextLabels();

		if(mWorkoutInfos.size() > 0){
			if(mHeartRateValuesComplete == null)
				mHeartRateValuesComplete = createValues(TYPE_HEART_RATE);
			if(mStepsValuesComplete == null)
				mStepsValuesComplete = createValues(TYPE_STEPS);
			if(mCaloriesValuesComplete == null)
				mCaloriesValuesComplete = createValues(TYPE_CALORIES);
			if(mDistanceValuesComplete == null)
				mDistanceValuesComplete = createValues(TYPE_DISTANCE);
		}

		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			if(mWorkoutInfos.size() > 0){
				int startPosition = getWorkoutStartDataPointPosition(0);
				int endPosition = getWorkoutEndDataPointPosition(0);
				if(mWorkoutInfos.get(0).getMinute() > 0 && endPosition<144){
					endPosition += 1;
				}
				mHeartRateValues = mHeartRateValuesComplete.subList(startPosition, endPosition);
				mStepsValues = mStepsValuesComplete.subList(startPosition, endPosition);
				mCaloriesValues = mCaloriesValuesComplete.subList(startPosition, endPosition);
				mDistanceValues = mDistanceValuesComplete.subList(startPosition, endPosition);
			}
		}
		else{
			mHeartRateValues = mHeartRateValuesComplete;
			mStepsValues = mStepsValuesComplete;
			mCaloriesValues = mCaloriesValuesComplete;
			mDistanceValues = mDistanceValuesComplete;
			int totalworkoutHund = 0;
			if(mWorkoutInfos.size() > 0){
				for (WorkoutInfo workoutInfo : mWorkoutInfos) {
					totalworkoutHund += (workoutInfo.getHour()*(3600*100)) + (workoutInfo.getMinute()*(60*100)) + workoutInfo.getSecond()*100 + workoutInfo.getHundredths();
				}

				int hr = totalworkoutHund/(3600*100);
				int min = (totalworkoutHund%(3600*100))/(60*100);
				int sec = (totalworkoutHund%(60*100))/100;
				int hund = totalworkoutHund%100;

				if (hr > 0){
					mHourLabel.setText("hr");
					mMinuteLabel.setText("min");
					mSecondLabel.setText("sec");
					mHourValue.setText(String.format("%02d", hr));
					mMinuteValue.setText(String.format("%02d", min));
					mSecondValue.setText(String.format("%02d", sec));
				}
				else{
					mHourLabel.setText("min");
					mMinuteLabel.setText("sec");
					mSecondLabel.setText("hund");
					mHourValue.setText(String.format("%02d", min));
					mMinuteValue.setText(String.format("%02d", sec));
					mSecondValue.setText(String.format("%02d", hund));
				}
			}
			else{
				mHourLabel.setText("min");
				mMinuteLabel.setText("sec");
				mSecondLabel.setText("hund");
				mHourValue.setText(String.format("00"));
				mMinuteValue.setText(String.format("00"));
				mSecondValue.setText(String.format("00"));
			}
		}

		if(true) {
			if (mHeartRateValues == null){
				mHeartRateValues = new ArrayList<Double>();
				for (int i=0;i<144;i++) {
					mHeartRateValues.add(0d);
				}
			}
			else if (mHeartRateValues.size() == 0) {
				for (int i=0;i<144;i++) {
					mHeartRateValues.add(0d);
				}
			}

			mMultipleSeriesDataset.removeSeries(mHeartRateSeries);
			mHeartRateSeries = createSeries(mHeartRateValues, getString(R.string.heart_rate_small));
			mMultipleSeriesDataset.addSeries(mHeartRateSeries);
			mMultipleSeriesRenderer.removeSeriesRenderer(mHeartRateRenderer);
			mMultipleSeriesRenderer.addSeriesRenderer(mHeartRateRenderer);

			for(int i=0;i<mHeartRateValues.size();i++) {
				if(mHeartRateValues.get(i) > 0) {
					mScrollPosition = mHeartRateSeries.getX(i);
					break;
				}
			}
		}
		if(true) {
			if (mStepsValues == null){
				mStepsValues = new ArrayList<Double>();
				for (int i=0;i<144;i++) {
					mStepsValues.add(0d);
				}
			}
			else if (mStepsValues.size() == 0) {
				for (int i=0;i<144;i++) {
					mStepsValues.add(0d);
				}
			}

			mMultipleSeriesDataset.removeSeries(mStepsSeries);
			mStepsSeries = createSeries(mStepsValues, getString(R.string.steps_small));
			mMultipleSeriesDataset.addSeries(mStepsSeries);
			mMultipleSeriesRenderer.removeSeriesRenderer(mStepsRenderer);
			mMultipleSeriesRenderer.addSeriesRenderer(mStepsRenderer);

			for(int i=0;i<mStepsValues.size();i++) {
				if(mStepsValues.get(i) > 0) {
					mScrollPosition = mStepsSeries.getX(i);
					break;
				}
			}
		}
		if(true) {
			if (mCaloriesValues == null){
				mCaloriesValues = new ArrayList<Double>();
				for (int i=0;i<144;i++) {
					mCaloriesValues.add(0d);
				}
			}
			else if (mCaloriesValues.size() == 0) {
				for (int i=0;i<144;i++) {
					mCaloriesValues.add(0d);
				}
			}

			mMultipleSeriesDataset.removeSeries(mCaloriesSeries);
			mCaloriesSeries = createSeries(mCaloriesValues, getString(R.string.calories_small));
			mMultipleSeriesDataset.addSeries(mCaloriesSeries);
			mMultipleSeriesRenderer.removeSeriesRenderer(mCaloriesRenderer);
			mMultipleSeriesRenderer.addSeriesRenderer(mCaloriesRenderer);

			for(int i=0;i<mCaloriesValues.size();i++) {
				if(mCaloriesValues.get(i) > 0) {
					mScrollPosition = mCaloriesSeries.getX(i);
					break;
				}
			}
		}
		if(true) {
			if (mDistanceValues == null){
				mDistanceValues = new ArrayList<Double>();
				for (int i=0;i<144;i++) {
					mDistanceValues.add(0d);
				}
			}
			else if (mDistanceValues.size() == 0) {
				for (int i=0;i<144;i++) {
					mDistanceValues.add(0d);
				}
			}

			mMultipleSeriesDataset.removeSeries(mDistanceSeries);
			mDistanceSeries = createSeries(mDistanceValues, getString(R.string.distance_small));
			mMultipleSeriesDataset.addSeries(mDistanceSeries);
			mMultipleSeriesRenderer.removeSeriesRenderer(mDistanceRenderer);
			mMultipleSeriesRenderer.addSeriesRenderer(mDistanceRenderer);

			for(int i=0;i<mDistanceValues.size();i++) {
				if(mDistanceValues.get(i) > 0) {
					mScrollPosition = mDistanceSeries.getX(i);
					break;
				}
			}
		}

		mMultipleSeriesRenderer.setXAxisMax(mCaloriesValues.size());
		int index = 0;

		if (orientation() == Configuration.ORIENTATION_LANDSCAPE) {
			mMultipleSeriesRenderer.clearXTextLabels();
			for(int i=0;i<mHeartRateValues.size();i++) {
				if(i % 6 == 0) {
					switch(getLifeTrakApplication().getTimeDate().getHourFormat()) {
					case TIME_FORMAT_12_HR:
						mMultipleSeriesRenderer.addXTextLabel(i, mHours[index]);
						break;
					case TIME_FORMAT_24_HR:
						mMultipleSeriesRenderer.addXTextLabel(i, String.format("%02d:00", index));
						break;
					}
					index++;
				}
			}
		}

		//mMultipleSeriesRenderer.setBarWidth(dpToPx(5));
		mMultipleSeriesRenderer.setBarWidth(widthOfBar());
		mMultipleSeriesRenderer.setBarSpacing(0.5);
		mMultipleSeriesRenderer.setMargins(new int[] {0,0,0,0});
		mMultipleSeriesRenderer.setXAxisMin(-1);
		mMultipleSeriesRenderer.setLabelsTextSize(dpToPx(12));

		if(orientation() == Configuration.ORIENTATION_LANDSCAPE)
			mHandlerDelay.postDelayed(new Runnable() {
				public void run() {
					scrollToXValue(mScrollPosition);
				}
			}, 500);
		//}

		mMultipleSeriesRenderer.setPanEnabled(false);
		mMultipleSeriesRenderer.setZoomEnabled(false, false);
		mMultipleSeriesRenderer.setShowLabels(true);
		mMultipleSeriesRenderer.setShowLegend(false);
		mMultipleSeriesRenderer.setShowGridY(false);
		mMultipleSeriesRenderer.setShowCustomTextGridY(false);
		mMultipleSeriesRenderer.setApplyBackgroundColor(true);
		mMultipleSeriesRenderer.setShowAxes(false);
		mMultipleSeriesRenderer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		mMultipleSeriesRenderer.setMarginsColor(getResources().getColor(R.color.color_xaxis_bar));
		mMultipleSeriesRenderer.setXLabelsColor(getResources().getColor(R.color.color_black_text));
		mMultipleSeriesRenderer.setYAxisMin(FITNESS_RESULTS_MIN_Y);
		mMultipleSeriesRenderer.setYAxisMax(FITNESS_RESULTS_MAX_Y);

		mBarChart = new BarChart(mMultipleSeriesDataset, mMultipleSeriesRenderer, BarChart.Type.DEFAULT);
		mGraphView = new GraphicalView(getActivity(), mBarChart);
		mWorkoutGraphPlotContainer.addView(mGraphView);

		if(orientation() == Configuration.ORIENTATION_PORTRAIT && mWorkoutInfos.size() > 0) {
			mWorkoutList.performItemClick(mWorkoutList, 0, mWorkoutList.getItemIdAtPosition(0));
		}

	}

	private void plotWorkoutGraphs(int position){
		if(!isAdded())
			return;

		mWorkoutGraphPlotContainer.removeAllViews();

		mMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
		mMultipleSeriesDataset = new XYMultipleSeriesDataset();

		mMultipleSeriesRenderer.setZoomEnabled(false, false);
		mMultipleSeriesRenderer.setYLabels(0);
		mMultipleSeriesRenderer.setShowGridY(false);
		mMultipleSeriesRenderer.setXLabels(0);
		mMultipleSeriesRenderer.clearXTextLabels();

		int startPosition = getWorkoutStartDataPointPosition(position);
		int endPosition = getWorkoutEndDataPointPosition(position);
		if(mWorkoutInfos.get(position).getMinute() > 0 && endPosition < 144)
			endPosition += 1;
		if (endPosition > 143)
			endPosition = 144;
		//if(mHeartRateValuesComplete == null)
		mHeartRateValuesComplete = createValues(TYPE_HEART_RATE);
		//if(mStepsValuesComplete == null)
		mStepsValuesComplete = createValues(TYPE_STEPS);
		//if(mCaloriesValuesComplete == null)
		mCaloriesValuesComplete = createValues(TYPE_CALORIES);
		//if(mDistanceValuesComplete == null)
		mDistanceValuesComplete = createValues(TYPE_DISTANCE);

		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			mHeartRateValues = mHeartRateValuesComplete.subList(startPosition, endPosition);
			mStepsValues = mStepsValuesComplete.subList(startPosition, endPosition);
			mCaloriesValues = mCaloriesValuesComplete.subList(startPosition, endPosition);
			mDistanceValues = mDistanceValuesComplete.subList(startPosition, endPosition);
			if (mHeartRateValues.size() == 0) {
				for (int i=0;i<2;i++) {
					mHeartRateValues.add(0d);
				}
			}
			//setWorkoutDataWithDay(getLifeTrakApplication().getCurrentDate());
		}
		else{
			mHeartRateValues = mHeartRateValuesComplete;
			mStepsValues = mStepsValuesComplete;
			mCaloriesValues = mCaloriesValuesComplete;
			mDistanceValues = mDistanceValuesComplete;
			int totalworkoutHund = 0;
			for (WorkoutInfo workoutInfo : mWorkoutInfos) {
				totalworkoutHund += (workoutInfo.getHour()*(3600*100)) + (workoutInfo.getMinute()*(60*100)) + workoutInfo.getSecond()*100 + workoutInfo.getHundredths();
			}

			int hr = totalworkoutHund/(3600*100);
			int min = (totalworkoutHund%(3600*100))/(60*100);
			int sec = (totalworkoutHund%(60*100))/100;
			int hund = totalworkoutHund&100;

			if (hr > 0){
				mHourLabel.setText("hr");
				mMinuteLabel.setText("min");
				mSecondLabel.setText("sec");
				mHourValue.setText(String.format("%02d", hr));
				mMinuteValue.setText(String.format("%02d", min));
				mSecondValue.setText(String.format("%02d", sec));
			}
			else{
				mHourLabel.setText("min");
				mMinuteLabel.setText("sec");
				mSecondLabel.setText("hund");
				mHourValue.setText(String.format("%02d", min));
				mMinuteValue.setText(String.format("%02d", sec));
				mSecondValue.setText(String.format("%02d", hund));
			}
			if (mHeartRateValues.size() == 0) {
				for (int i=0;i<144;i++) {
					mHeartRateValues.add(0d);
				}
			}
		}

		if(true) {


			mMultipleSeriesDataset.removeSeries(mHeartRateSeries);
			mHeartRateSeries = createSeries(mHeartRateValues, getString(R.string.heart_rate_small));
			mMultipleSeriesDataset.addSeries(mHeartRateSeries);
			mMultipleSeriesRenderer.removeSeriesRenderer(mHeartRateRenderer);
			mMultipleSeriesRenderer.addSeriesRenderer(mHeartRateRenderer);

			for(int i=0;i<mHeartRateValues.size();i++) {
				if(mHeartRateValues.get(i) > 0) {
					mScrollPosition = mHeartRateSeries.getX(i);
					break;
				}
			}
		}
		if(true) {
			if (mStepsValues.size() == 0) {
				for (int i=0;i<144;i++) {
					mStepsValues.add(0d);
				}
			}

			mMultipleSeriesDataset.removeSeries(mStepsSeries);
			mStepsSeries = createSeries(mStepsValues, getString(R.string.steps_small));
			mMultipleSeriesDataset.addSeries(mStepsSeries);
			mMultipleSeriesRenderer.removeSeriesRenderer(mStepsRenderer);
			mMultipleSeriesRenderer.addSeriesRenderer(mStepsRenderer);

			for(int i=0;i<mStepsValues.size();i++) {
				if(mStepsValues.get(i) > 0) {
					mScrollPosition = mStepsSeries.getX(i);
					break;
				}
			}
		}
		if(true) {
			if (mCaloriesValues.size() == 0) {
				for (int i=0;i<144;i++) {
					mCaloriesValues.add(0d);
				}
			}

			mMultipleSeriesDataset.removeSeries(mCaloriesSeries);
			mCaloriesSeries = createSeries(mCaloriesValues, getString(R.string.calories_small));
			mMultipleSeriesDataset.addSeries(mCaloriesSeries);
			mMultipleSeriesRenderer.removeSeriesRenderer(mCaloriesRenderer);
			mMultipleSeriesRenderer.addSeriesRenderer(mCaloriesRenderer);

			for(int i=0;i<mCaloriesValues.size();i++) {
				if(mCaloriesValues.get(i) > 0) {
					mScrollPosition = mCaloriesSeries.getX(i);
					break;
				}
			}
		}
		if(true) {
			if (mDistanceValues.size() == 0) {
				for (int i=0;i<144;i++) {
					mDistanceValues.add(0d);
				}
			}

			mMultipleSeriesDataset.removeSeries(mDistanceSeries);
			mDistanceSeries = createSeries(mDistanceValues, getString(R.string.distance_small));
			mMultipleSeriesDataset.addSeries(mDistanceSeries);
			mMultipleSeriesRenderer.removeSeriesRenderer(mDistanceRenderer);
			mMultipleSeriesRenderer.addSeriesRenderer(mDistanceRenderer);

			for(int i=0;i<mDistanceValues.size();i++) {
				if(mDistanceValues.get(i) > 0) {
					mScrollPosition = mDistanceSeries.getX(i);
					break;
				}
			}
		}

		mMultipleSeriesRenderer.setXAxisMax(mHeartRateValues.size());
		mMultipleSeriesRenderer.clearXTextLabels();
		int index = 0;
		if(orientation() == Configuration.ORIENTATION_PORTRAIT){
			for(int i=0;i<mHeartRateValues.size();i++) {
				if(i==0){
					mMultipleSeriesRenderer.addXTextLabel(i, mStartTimeValue.getText().toString());
				}
				else if (i == mHeartRateValues.size() - 1){
					mMultipleSeriesRenderer.addXTextLabel(i, mEndTimeValue.getText().toString());
				}
				else{
					mMultipleSeriesRenderer.addXTextLabel(i, /*mHours[index]*/"");
				}
				index++;
			}
			/*
					if(mWorkoutInfos.get(position).getWorkoutStopInfos()!=null){
						int workoutStart = getWorkoutStartDataPointPosition(position);
						for (WorkoutStopInfo workStopInfo : mWorkoutInfos.get(position).getWorkoutStopInfos()){
							int workoutStartPosition = (mWorkoutInfos.get(position).getTimeStampHour()*6 + mWorkoutInfos.get(position).getTimeStampMinute()/10)+(workStopInfo.getWorkoutHours()*6) + (workStopInfo.getWorkoutMinutes()/10);
							int workoutStopStartMinutes = mWorkoutInfos.get(position).getTimeStampHour()*3600 + mWorkoutInfos.get(position).getTimeStampMinute()*60 + mWorkoutInfos.get(position).getTimeStampSecond() + workStopInfo.getWorkoutHours()*3600 + workStopInfo.getWorkoutMinutes()*60 + workStopInfo.getWorkoutSeconds();

							Calendar calendar = Calendar.getInstance();
							calendar.setTime(mWorkoutInfos.get(position).getDateStamp());

							if (workoutStopStartMinutes > 86399)
								workoutStopStartMinutes = 86399;
							calendar.set(Calendar.HOUR_OF_DAY, workoutStopStartMinutes/3600);
							calendar.set(Calendar.MINUTE, (workoutStopStartMinutes%3600)/60);
							calendar.set(Calendar.SECOND, workoutStopStartMinutes%60);

							String stopTime = "";
							if(getLifeTrakApplication().getTimeDate().getHourFormat() == TIME_FORMAT_12_HR) {
								//viewHolder.workoutStartTime.setText(mDateFormat.format(calendar.getTime()));	
								stopTime = mDateFormat.format(calendar.getTime());
							} else {
								//viewHolder.workoutStartTime.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE)));
								stopTime = String.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
							}
							mMultipleSeriesRenderer.addXTextLabel(workoutStartPosition - workoutStart, stopTime);
						}
					}
			 */

		}
		else{
			mMultipleSeriesRenderer.clearXTextLabels();
			for(int i=0;i<mHeartRateValues.size();i++) {
				if(i % 6 == 0) {
					switch(getLifeTrakApplication().getTimeDate().getHourFormat()) {
					case TIME_FORMAT_12_HR:
						mMultipleSeriesRenderer.addXTextLabel(i, mHours[index]);
						break;
					case TIME_FORMAT_24_HR:
						mMultipleSeriesRenderer.addXTextLabel(i, String.format("%02d:00", index));
						break;
					}
					index++;
				}
			}
		}


		//mMultipleSeriesRenderer.setBarWidth(dpToPx(5));
		mMultipleSeriesRenderer.setBarWidth(widthOfBar());
		mMultipleSeriesRenderer.setBarSpacing(0.5);
		mMultipleSeriesRenderer.setMargins(new int[] {0,35,0,35});
		mMultipleSeriesRenderer.setXAxisMin(-1);
		mMultipleSeriesRenderer.setLabelsTextSize(dpToPx(8));

		if(orientation() == Configuration.ORIENTATION_LANDSCAPE)
			mHandlerDelay.postDelayed(new Runnable() {
				public void run() {
					scrollToXValue(mScrollPosition);
				}
			}, 500);
		//}

		mMultipleSeriesRenderer.setPanEnabled(false);
		mMultipleSeriesRenderer.setZoomEnabled(false, false);
		mMultipleSeriesRenderer.setShowLabels(true);
		mMultipleSeriesRenderer.setShowLegend(false);
		mMultipleSeriesRenderer.setShowGridY(false);
		mMultipleSeriesRenderer.setShowCustomTextGridY(false);
		mMultipleSeriesRenderer.setApplyBackgroundColor(true);
		mMultipleSeriesRenderer.setShowAxes(false);
		mMultipleSeriesRenderer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		mMultipleSeriesRenderer.setMarginsColor(getResources().getColor(R.color.color_xaxis_bar));
		mMultipleSeriesRenderer.setXLabelsColor(getResources().getColor(R.color.color_black_text));
		mMultipleSeriesRenderer.setYAxisMin(FITNESS_RESULTS_MIN_Y);
		mMultipleSeriesRenderer.setYAxisMax(FITNESS_RESULTS_MAX_Y);

		mBarChart = new BarChart(mMultipleSeriesDataset, mMultipleSeriesRenderer, BarChart.Type.DEFAULT);
		mGraphView = new GraphicalView(getActivity(), mBarChart);
		mWorkoutGraphPlotContainer.addView(mGraphView);

	};

	private final GraphScrollView.GraphScrollViewListener mGraphScrollViewListener = new GraphScrollView.GraphScrollViewListener() {
		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {

			int multiplier = 0;

			multiplier++;
			multiplier++;
			multiplier++;
			multiplier++;

			if(mHeartRateSeries != null) {
				for(int i=0;i<mHeartRateSeries.getItemCount();i++) {
					double x = mHeartRateSeries.getX(i);
					double[] screenPoint = mBarChart.toScreenPoint(new double[] {x, 0});

					int halfBarWidth =  (int)(mBarChart.getRenderer().getBarWidth() / 2) * multiplier;
					int playHeadPositionX = (mPlayheadImage.getLeft() + (mPlayheadImage.getMeasuredWidth() / 2));
					int currentPointX = ((int)(mLeftView.getMeasuredWidth() + screenPoint[0])) - l + (int)mBarChart.getRenderer().getBarSpacing();

					if(playHeadPositionX >= currentPointX - halfBarWidth && playHeadPositionX <= currentPointX + halfBarWidth) {
						double[] realPoint = mBarChart.toRealPoint((float)screenPoint[0], 0);
						int index = 0;

						if(realPoint[0] < 0) {
							index = 0;
						} else {
							index = (int)Math.round(realPoint[0]);
						}

						if(index >= mHeartRateSeries.getItemCount()) {
							index = mHeartRateSeries.getItemCount() - 1;
						}

						if (mHeartRateValues != null) {
							mHeartRateValue.setText(String.valueOf(Math.round(mHeartRateValues.get(index).floatValue())));
						}
						break;
					} else {
						mHeartRateValue.setText("0");
					}
				}
			}

			if(mStepsSeries != null) {
				for(int i=0;i<mStepsSeries.getItemCount();i++) {
					double x = mStepsSeries.getX(i);
					double[] screenPoint = mBarChart.toScreenPoint(new double[] {x, 0});

					int halfBarWidth =  (int)(mBarChart.getRenderer().getBarWidth() / 2) * multiplier;
					int playHeadPositionX = (mPlayheadImage.getLeft() + (mPlayheadImage.getMeasuredWidth() / 2));
					int currentPointX = ((int)(mLeftView.getMeasuredWidth() + screenPoint[0])) - l + (int)mBarChart.getRenderer().getBarSpacing();

					if(playHeadPositionX >= currentPointX - halfBarWidth && playHeadPositionX <= currentPointX + halfBarWidth) {
						double[] realPoint = mBarChart.toRealPoint((float)screenPoint[0], 0);
						int index = 0;

						if(realPoint[0] < 0) {
							index = 0;
						} else {
							index = (int)Math.round(realPoint[0]);
						}

						if(index >= mStepsSeries.getItemCount()) {
							index = mStepsSeries.getItemCount() - 1;
						}

						if (mStepsValues != null && index < mStepsValues.size()) {
							mStepsValue.setText(String.valueOf(mDecimalFormat2.format(mStepsValues.get(index).doubleValue())));
						}
						break;
					} else {
						mStepsValue.setText("0");
					}
				}
			}

			if(mCaloriesSeries != null) {
				for(int i=0;i<mCaloriesSeries.getItemCount();i++) {
					double x = mCaloriesSeries.getX(i);
					double[] screenPoint = mBarChart.toScreenPoint(new double[] {x, 0});

					int halfBarWidth =  (int)(mBarChart.getRenderer().getBarWidth() / 2) * multiplier;
					int playHeadPositionX = (mPlayheadImage.getLeft() + (mPlayheadImage.getMeasuredWidth() / 2));
					int currentPointX = ((int)(mLeftView.getMeasuredWidth() + screenPoint[0])) - l + (int)mBarChart.getRenderer().getBarSpacing();

					if(playHeadPositionX >= currentPointX - halfBarWidth && playHeadPositionX <= currentPointX + halfBarWidth) {
						double[] realPoint = mBarChart.toRealPoint((float)screenPoint[0], 0);
						int index = 0;

						if(realPoint[0] < 0) {
							index = 0;
						} else {
							index = (int)Math.round(realPoint[0]);
						}

						if(index >= mCaloriesSeries.getItemCount()) {
							index = mCaloriesSeries.getItemCount() - 1;
						}

						if (mCaloriesValues != null) {
							mCaloriesValue.setText(String.valueOf(mDecimalFormat2.format(mCaloriesValues.get(index).doubleValue())));
						}
						break;
					} else {
						mCaloriesValue.setText("0");
					}
				}
			}

			if(mDistanceSeries != null) {
				for(int i=0;i<mDistanceSeries.getItemCount();i++) {
					double x = mDistanceSeries.getX(i);
					double[] screenPoint = mBarChart.toScreenPoint(new double[] {x, 0});

					int halfBarWidth =  (int)(mBarChart.getRenderer().getBarWidth() / 2) * multiplier;
					int playHeadPositionX = (mPlayheadImage.getLeft() + (mPlayheadImage.getMeasuredWidth() / 2));
					int currentPointX = ((int)(mLeftView.getMeasuredWidth() + screenPoint[0])) - l + (int)mBarChart.getRenderer().getBarSpacing();

					if(playHeadPositionX >= currentPointX - halfBarWidth && playHeadPositionX <= currentPointX + halfBarWidth) {
						double[] realPoint = mBarChart.toRealPoint((float)screenPoint[0], 0);
						int index = 0;

						if(realPoint[0] < 0) {
							index = 0;
						} else {
							index = (int)Math.round(realPoint[0]);
						}

						if(index >= mDistanceSeries.getItemCount()) {
							index = mDistanceSeries.getItemCount() - 1;
						}

						if (mDistanceValues != null) {
							double distanceValue = mDistanceValues.get(index);

							if(getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL)
								distanceValue = distanceValue * MILE;

							mDistanceValue.setText(mDecimalFormat.format(distanceValue));
						}
						break;
					} else {
						mDistanceValue.setText("0.00");
					}
				}
			}
		}
	};

	private void scrollToXValue(double x) {
		if(mPlayheadImage != null) {
			double[] screenPoint = mBarChart.toScreenPoint(new double[] {x, 0});
			int scrollPositionX = mLeftView.getMeasuredWidth() + (int)screenPoint[0] - mPlayheadImage.getRight();
			mGraphScroll.smoothScrollTo(scrollPositionX, 0);
		}
	}

	public int getCalendarMode() {
		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			switch(mToDateGroup.getCheckedRadioButtonId()) {
			case R.id.radWeek:
				return MODE_WEEK;
			case R.id.radMonth:
				return MODE_MONTH;
			case R.id.radYear:
				return MODE_YEAR;
			}
		}
		return MODE_DAY;
	}

	private String columnWithDashboardType(int dashboardType) {
		String column = "calorie";

		switch(dashboardType) {
		case TYPE_HEART_RATE:
			column = "averageHR";
			break;
		case TYPE_DISTANCE:
			column = "distance";
			break;
		case TYPE_STEPS:
			column = "steps";
			break;
		}

		return column;
	}

}
