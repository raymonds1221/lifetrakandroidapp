package com.salutron.lifetrakwatchapp.fragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.database.Cursor;
import android.graphics.Paint.Align;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.StatisticalDataPoint;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.view.GraphScrollView;

import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

public class FitnessResultsItemFragment extends BaseFragment {
	private int mDashboardType;
	
	private TextView mMetricTitle;
	private TextView mMetricValue;
	private TextView mMetricValueUnit;
	private ImageView mMetricIcon;
	private ImageView mMetricGoalIcon;
	private TextView mMetricGoalPercent;
	private TextView mMetricGoal;
	private TextView mMetricUnit;
	private View mMetricProgressValue;
	private RadioGroup mToDateGroup;
	private FrameLayout mFitnessResultsPlotContainer;
	private FrameLayout mFitnessResultsLoadingText;
	private FrameLayout mFitnessResultsCenterContainer;
	private RelativeLayout mFitnessResultsTopData;
	private ImageView mPlayheadImage;
	
	private Spinner mCalendarModeSpinner;
	private CheckBox mHeartRateLabel;
	private TextView mHeartRateValue;
	private CheckBox mStepsLabel;
	private TextView mStepsValue;
	private TextView mStepsGoal;
	private View mStepsGoalValue;
	private CheckBox mCaloriesLabel;
	private TextView mCaloriesValue;
	private TextView mCaloriesGoal;
	private View mCaloriesGoalValue;
	private CheckBox mDistanceLabel;
	private TextView mDistanceValue;
	private TextView mDistanceGoal;
	private View mDistanceGoalValue;
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
	
	private BarChart mBarChart;
	private GraphicalView mGraphView;
	
	private Date mDateNow;
	private Date mDateFrom;
	private Date mDateTo;
	private int mYear;
	
	private final DecimalFormat mDecimalFormat = new DecimalFormat("###,##0.00");
	private final DecimalFormat mDecimalFormat2 = new DecimalFormat("###,##0");
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private String[] mMonths;
	private final String[] mHours = {"12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM", "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM", "8PM", "9PM", "10PM", "11PM"};
	
	private final Handler mHandlerDelay = new Handler();
	private double mScrollPosition;
	private int mCalendarMode = MODE_DAY;
	private final String DATE_FROM = "date_from";
	private final String DATE_TO = "date_to";
	
	private int mPosition;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_fitness_results_item, null);
		String[] months = {getString(R.string.jan), getString(R.string.feb), getString(R.string.mar),
				getString(R.string.apr), getString(R.string.may), getString(R.string.jun), getString(R.string.jul),
				getString(R.string.aug), getString(R.string.sep), getString(R.string.oct), getString(R.string.nov),
				getString(R.string.dec)};
		mMonths = months;
		initializeViews(view);
		
		switch(orientation()) {
		case Configuration.ORIENTATION_LANDSCAPE:
			hideActionBarAndCalendar();
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			showActionBarAndCalendar();
			break;
		}
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initializeObjects(savedInstanceState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		
		switch(newConfig.orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			changeFramentView(inflater, R.layout.fragment_fitness_results_item, (ViewGroup) getView());
			showActionBarAndCalendar();
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			changeFramentView(inflater, R.layout.fragment_fitness_results_item_land, (ViewGroup) getView());
			hideActionBarAndCalendar();
			break;
		}
		
		initializeViews(getView());
		initializeDataOnOrientationChange();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		int calendarMode = MODE_DAY;
		
		if(orientation() == Configuration.ORIENTATION_LANDSCAPE) {
			if(mToDateGroup != null) {
				switch(mToDateGroup.getCheckedRadioButtonId()) {
				case R.id.radDay:
					calendarMode = MODE_DAY;
					break;
				case R.id.radWeek:
					calendarMode = MODE_WEEK;
					break;
				case R.id.radMonth:
					calendarMode = MODE_MONTH;
					break;
				case R.id.radYear:
					calendarMode = MODE_YEAR;
					break;
				}
			}
		} else {
			if(mCalendarModeSpinner != null) {
				switch(mCalendarModeSpinner.getSelectedItemPosition()) {
				case 0:
					calendarMode = MODE_DAY;
					break;
				case 1:
					calendarMode = MODE_WEEK;
					break;
				case 2:
					calendarMode = MODE_MONTH;
					break;
				case 3:
					calendarMode = MODE_YEAR;
					break;
				}
			}
		}
		
		if(mDateFrom != null && mDateTo != null) {
			outState.putLong(DATE_FROM, mDateFrom.getTime());
			outState.putLong(DATE_TO, mDateTo.getTime());
		}
		outState.putInt(CALENDAR_MODE_KEY, calendarMode);
	}
	
	private void initializeViews(View view) {
		mMetricTitle = (TextView) view.findViewById(R.id.tvwMetricTitle);
		mMetricValue = (TextView) view.findViewById(R.id.tvwMetricValue);
		mMetricValueUnit = (TextView) view.findViewById(R.id.tvwMetricValueUnit);
		mMetricIcon = (ImageView) view.findViewById(R.id.imgMetricIcon);
		mMetricGoalIcon = (ImageView) view.findViewById(R.id.imgMetricGoalIcon);
		mMetricGoalPercent = (TextView) view.findViewById(R.id.tvwMetricGoalPercent);
		mMetricGoal = (TextView) view.findViewById(R.id.tvwMetricGoal);
		mMetricUnit = (TextView) view.findViewById(R.id.tvwMetricUnit);
		mMetricProgressValue = view.findViewById(R.id.viewMetricProgressValue);
		mToDateGroup = (RadioGroup) view.findViewById(R.id.rdgToDate);
		mCalendarModeSpinner = (Spinner) view.findViewById(R.id.spnCalendarMode);
		
		mHeartRateLabel = (CheckBox) view.findViewById(R.id.chkHeartRateLabel);
		mHeartRateValue = (TextView) view.findViewById(R.id.tvwHeartRateValue);
		mStepsLabel = (CheckBox) view.findViewById(R.id.chkStepsLabel);
		mStepsValue = (TextView) view.findViewById(R.id.tvwStepsValue);
		mStepsGoal = (TextView) view.findViewById(R.id.tvwStepsGoal);
		mStepsGoalValue = view.findViewById(R.id.vwStepsGoalValue);
		mCaloriesLabel = (CheckBox) view.findViewById(R.id.chkCaloriesLabel);
		mCaloriesValue = (TextView) view.findViewById(R.id.tvwCaloriesValue);
		mCaloriesGoal = (TextView) view.findViewById(R.id.tvwCaloriesGoal);
		mCaloriesGoalValue = view.findViewById(R.id.vwCaloriesGoalValue);
		mDistanceLabel = (CheckBox) view.findViewById(R.id.chkDistanceLabel);
		mDistanceValue = (TextView) view.findViewById(R.id.tvwDistanceValue);
		mDistanceGoal = (TextView) view.findViewById(R.id.tvwDistanceGoal);
		mDistanceGoalValue = view.findViewById(R.id.vwDistanceGoalValue);

		LifeTrakLogger.configure();
		
		mFitnessResultsPlotContainer = (FrameLayout) view.findViewById(R.id.frmFitnessResultsPlotContainer);
		
		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			mFitnessResultsLoadingText = (FrameLayout) view.findViewById(R.id.frmFitnessResultsLoadingText);
			mFitnessResultsTopData = (RelativeLayout) view.findViewById(R.id.topDataLayout);
			mFitnessResultsCenterContainer = (FrameLayout) view.findViewById(R.id.frmFitnessResultsCenterContainer);
		}
		
		mPlayheadImage = (ImageView) view.findViewById(R.id.imgPlayhead);
		mLeftView = view.findViewById(R.id.viewGraphLeftPadding);
		
		mGraphScroll = (GraphScrollView) view.findViewById(R.id.hsvGraphScroll);
		
		switch(orientation()) {
		case Configuration.ORIENTATION_PORTRAIT:
			mToDateGroup.setOnCheckedChangeListener(null);
			
			switch(mCalendarMode) {
			case MODE_DAY:
				mToDateGroup.check(R.id.radDay);
				break;
			case MODE_WEEK:
				mToDateGroup.check(R.id.radWeek);
				break;
			case MODE_MONTH:
				mToDateGroup.check(R.id.radMonth);
				break;
			case MODE_YEAR:
				mToDateGroup.check(R.id.radYear);
				break;
			}
			
			mToDateGroup.setOnCheckedChangeListener(mCheckedChangeListener);
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.calendar_mode_spinner));
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mCalendarModeSpinner.setAdapter(adapter);
			
			mCalendarModeSpinner.setOnItemSelectedListener(null);
			
			switch(mCalendarMode) {
			case MODE_DAY:
				mCalendarModeSpinner.setSelection(0);
				break;
			case MODE_WEEK:
				mCalendarModeSpinner.setSelection(1);
				break;
			case MODE_MONTH:
				mCalendarModeSpinner.setSelection(2);
				break;
			case MODE_YEAR:
				mCalendarModeSpinner.setSelection(3);
				break;
			}
			
			mCalendarModeSpinner.setOnItemSelectedListener(mItemSelectedListener);
			
			mHeartRateLabel.setOnCheckedChangeListener(mLandscapeStatCheckedChangeListener);
			mStepsLabel.setOnCheckedChangeListener(mLandscapeStatCheckedChangeListener);
			mCaloriesLabel.setOnCheckedChangeListener(mLandscapeStatCheckedChangeListener);
			mDistanceLabel.setOnCheckedChangeListener(mLandscapeStatCheckedChangeListener);
			mGraphScroll.setGraphScrollViewListener(mGraphScrollViewListener);
			break;
		}
	}
	
	private void initializeObjects(Bundle savedInstanceState) {
		if(!isAdded())
			return;
		
		if(savedInstanceState != null) {
			mCalendarMode = savedInstanceState.getInt(CALENDAR_MODE_KEY);
			mDateFrom = new Date(savedInstanceState.getLong(DATE_FROM));
			mDateTo = new Date(savedInstanceState.getLong(DATE_TO));
		}
		
		//mDecimalFormat.setRoundingMode(RoundingMode.HALF_UP);
		//mDecimalFormat2.setRoundingMode(RoundingMode.DOWN);
		mDateFormat.applyPattern("dd MMM");
		
		if(mHeartRateValues != null)
			mHeartRateValues.clear();
		if(mStepsValues != null)
			mStepsValues.clear();
		if(mCaloriesValues != null)
			mCaloriesValues.clear();
		if(mDistanceValues != null)
			mDistanceValues.clear();
		
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
			mDashboardType = getArguments().getInt(DASHBOARD_TYPE);
			mPosition = getArguments().getInt(POSITION);
			
			Date from = getLifeTrakApplication().getDateRangeFrom();
			Date to = getLifeTrakApplication().getDateRangeTo();
			
			if(mDateFrom != null && mDateTo != null) {
				from = mDateFrom;
				to = mDateTo;
			}
			
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
			case MODE_WEEK:
				if(from != null && to != null) {
					setDataWithWeek(from, to);
					
					if(mPosition == 1) {
						activity.setCalendarDateWeek(from, to);
						activity.setCalendarPickerMode(MODE_WEEK, from, to);
					}
				}
				break;
			case MODE_MONTH:
				if(from != null && to != null) {
					setDataWithMonth(from, to);
					
					if(mPosition == 1) {
						activity.setCalendarDateWeek(from, to);
						activity.setCalendarMonth(from);
						activity.setCalendarPickerMode(MODE_MONTH, from, to);
					}
				}
				break;
			case MODE_YEAR:
				setDataWithYear(getLifeTrakApplication().getCurrentYear(), mPosition);
				
				if(mPosition == 1)
					activity.setCalendarYear(getLifeTrakApplication().getCurrentYear());
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
		
		switch(mCalendarMode) {
		case MODE_DAY:
			initializeStats(mDashboardType, values, mDateNow, mDateNow);
			initializeGraph(values, mDashboardType, mCalendarMode);
			break;
		case MODE_WEEK:
			initializeStats(mDashboardType, values, mDateFrom, mDateTo);
			initializeGraph(values, mDashboardType, mCalendarMode);
			break;
		case MODE_MONTH:
			initializeStats(mDashboardType, values, mDateFrom, mDateTo);
			initializeGraph(values, mDashboardType, mCalendarMode);
			break;
		case MODE_YEAR:
			Calendar calendarFrom = Calendar.getInstance();
			Calendar calendarTo = Calendar.getInstance();
			
			calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
			calendarFrom.set(Calendar.MONTH, 0);
			calendarFrom.set(Calendar.YEAR, mYear);
			
			calendarTo.set(Calendar.DAY_OF_MONTH, 31);
			calendarTo.set(Calendar.MONTH, 11);
			calendarTo.set(Calendar.YEAR, mYear);
			
			initializeStats(mDashboardType, values, calendarFrom.getTime(), calendarTo.getTime());
			initializeGraph(values, mDashboardType, mCalendarMode);
			break;
		}
		
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
			
			/*final List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(getActivity())
																		.getReadOperation()
																		.query("watchDataHeader == ? AND dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ?", 
																				String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
																				String.valueOf(day), String.valueOf(month), String.valueOf(year))
																		.getResults(StatisticalDataHeader.class);
			
			
			if(dataHeaders.size() > 0) {
				StatisticalDataHeader dataHeader = dataHeaders.get(0);
				
				switch(dashboardType) {
				case TYPE_STEPS:
					totalValue += dataHeader.getTotalSteps();
					break;
				case TYPE_DISTANCE:
					totalValue += dataHeader.getTotalDistance();
					break;
				case TYPE_CALORIES:
					totalValue += dataHeader.getTotalCalorie();
					break;
				}
			}*/
			
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
		
		switch(dashboardType) {
		case TYPE_STEPS:
			if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
				mMetricTitle.setText(R.string.steps);
				mMetricIcon.setImageResource(R.drawable.ll_fitnessres_icon_stat_steps);
				mMetricValue.setText(mDecimalFormat2.format(totalValue));
				initializeGoalsWithDate(from, to, dashboardType, totalValue);
			}
			break;
		case TYPE_DISTANCE:
			if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
				mMetricUnit.setVisibility(View.VISIBLE);
				
				if(getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL) {
					totalValue *= MILE;
					mMetricUnit.setText("mi");
					BigDecimal bigDecimal = new BigDecimal(totalValue);
					bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_DOWN);
					mMetricValue.setText(String.valueOf(bigDecimal));
				} else {
					mMetricUnit.setText("km");
					BigDecimal bigDecimal = new BigDecimal(totalValue);
					bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
					mMetricValue.setText(String.valueOf(bigDecimal));
				}
				
				mMetricTitle.setText(R.string.distance);
				mMetricIcon.setImageResource(R.drawable.ll_fitnessres_icon_stat_distance);
				initializeGoalsWithDate(from, to, dashboardType, totalValue);
			}
			break;
		case TYPE_CALORIES:
			if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
				mMetricTitle.setText(R.string.calories);
				mMetricIcon.setImageResource(R.drawable.ll_fitnessres_icon_stat_calorie);
				mMetricValue.setText(mDecimalFormat2.format(totalValue));
				mMetricUnit.setText("kcal");
				initializeGoalsWithDate(from, to, dashboardType, totalValue);
			}
			break;
		}
	}
	
	private void initializeStatsLand(int dashboardType, List<Double> values, Date from, Date to) {
		double totalValue = 0;
		
		for(Double value : values) {
			totalValue += value.doubleValue();
		}
		
		if(orientation() == Configuration.ORIENTATION_LANDSCAPE) {
			String field = "stepGoal";
			
			switch(dashboardType) {
			case TYPE_STEPS:
				field = "stepGoal";
				break;
			case TYPE_DISTANCE:
				field = "distanceGoal";
				break;
			case TYPE_CALORIES:
				field = "calorieGoal";
				break;
			}
			
			if(getCalendarMode() == MODE_DAY) {
				/*String query = "select " + field + " from Goal where watchGoal = ? order by (date - " + getLifeTrakApplication().getCurrentDate().getTime() + ")";
				Cursor cursor = DataSource.getInstance(getActivity())
											.getReadOperation()
											.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));*/
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(getLifeTrakApplication().getCurrentDate());
				
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				int month = calendar.get(Calendar.MONTH) + 1;
				int year = calendar.get(Calendar.YEAR) - 1900;
				
				List<Goal> goals = DataSource.getInstance(getActivity())
												.getReadOperation()
												.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
														String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
														String.valueOf(day), String.valueOf(month),String.valueOf(year))
												.orderBy("_id", SORT_DESC)
												.getResults(Goal.class);
				
				if    (goals.size() == 0) {
					goals = DataSource.getInstance(getActivity())
										.getReadOperation()
										.query("watchGoal = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
										.orderBy("abs(date - " + calendar.getTimeInMillis() + ")", SORT_ASC)
										.limit(1)
										.getResults(Goal.class);

					Calendar now = Calendar.getInstance();

					if(now.get(Calendar.DATE) < calendar.get(Calendar.DATE) ){
						Goal mGoal = goals.get(0);
						goals = DataSource
								.getInstance(getActivity())
								.getReadOperation()
								.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
										String.valueOf(getLifeTrakApplication()
												.getSelectedWatch().getId()),
										String.valueOf(mGoal.getDateStampDay()), String.valueOf(mGoal.getDateStampMonth()),
										String.valueOf(mGoal.getDateStampYear()))
								.orderBy("_id", SORT_DESC)
										//.limit(2)
								.getResults(Goal.class);
					}
				}
				
				
				if(goals.size() > 0) {
					double goal = 0;
					Goal goalObject = goals.get(0);
					
					switch (dashboardType) {
					case TYPE_STEPS:
						goal = goalObject.getStepGoal();
						break;
					case TYPE_DISTANCE:
						goal = goalObject.getDistanceGoal();
						break;
					case TYPE_CALORIES:
						goal = goalObject.getCalorieGoal();
						break;
					}
					
					double percent = totalValue / goal;
					int parentWidth = 0;
					int width = 0;
					FrameLayout.LayoutParams params = null;
					
					switch(dashboardType) {
					case TYPE_STEPS:
						mStepsGoal.setText(getResources().getString(R.string.string_goal) +" " + mDecimalFormat2.format(goal));
						parentWidth = getView().findViewById(R.id.frmDistanceGoalContainer).getMeasuredWidth();
						width = (int) ((double) parentWidth * percent);
						params = new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT);
						mStepsGoalValue.setLayoutParams(params);
						mStepsGoalValue.setBackgroundColor(colorForPercent(percent));
						break;
					case TYPE_DISTANCE:
						if(getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL)
							goal *= MILE;
						mDistanceGoal.setText(getResources().getString(R.string.string_goal) +" " + mDecimalFormat.format(goal));
						parentWidth = getView().findViewById(R.id.frmDistanceGoalContainer).getMeasuredWidth();
						width = (int) ((double) parentWidth * percent);
						params = new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT);
						mDistanceGoalValue.setLayoutParams(params);
						mDistanceGoalValue.setBackgroundColor(colorForPercent(percent));
						break;
					case TYPE_CALORIES:
						mCaloriesGoal.setText(getResources().getString(R.string.string_goal) +" " + mDecimalFormat2.format(goal));
						parentWidth = getView().findViewById(R.id.frmCaloriesGoalContainer).getMeasuredWidth();
						width = (int) ((double) parentWidth * percent);
						params = new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT);
						mCaloriesGoalValue.setLayoutParams(params);
						mCaloriesGoalValue.setBackgroundColor(colorForPercent(percent));
						break;
					}
				}
			} else if(getCalendarMode() == MODE_YEAR && mPosition == 1) {
				final int year = getLifeTrakApplication().getCurrentYear();
				final Calendar calendarFrom = Calendar.getInstance();
				final Calendar calendarTo = Calendar.getInstance();
				
				calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
				calendarFrom.set(Calendar.MONTH, 0);
				calendarFrom.set(Calendar.YEAR, year);
				
				calendarTo.set(Calendar.DAY_OF_MONTH, 31);
				calendarTo.set(Calendar.MONTH, 11);
				calendarTo.set(Calendar.YEAR, year);
				
				Date dateto = calendarTo.getTime();
				LifeTrakLogger.info("dateto: " + dateto);
				
				double totalGoal = 0;
				
				while(calendarFrom.getTime().before(dateto) || calendarFrom.getTime().equals(dateto)) {
					calendarFrom.set(Calendar.HOUR_OF_DAY, 0);
					calendarFrom.set(Calendar.MINUTE, 0);
					calendarFrom.set(Calendar.SECOND, 0);
					
					int day = calendarFrom.get(Calendar.DAY_OF_MONTH);
					int month = calendarFrom.get(Calendar.MONTH) + 1;
					int year2 = calendarFrom.get(Calendar.YEAR) - 1900;
					
					List<Goal> goals = DataSource.getInstance(getActivity())
												.getReadOperation()
												.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
														String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
														String.valueOf(day), String.valueOf(month),String.valueOf(year2))
												.orderBy("_id", SORT_DESC)
												.getResults(Goal.class);
					
					if    (goals.size() == 0) {
						goals = DataSource.getInstance(getActivity())
											.getReadOperation()
											.query("watchGoal = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
											.orderBy("abs(date - " + calendarFrom.getTimeInMillis() + ")", SORT_ASC)
											.limit(1)
											.getResults(Goal.class);

						Calendar now = Calendar.getInstance();

						if(now.get(Calendar.DATE) < calendarFrom.get(Calendar.DATE) ){
							Goal mGoal = goals.get(0);
							goals = DataSource
									.getInstance(getActivity())
									.getReadOperation()
									.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
											String.valueOf(getLifeTrakApplication()
													.getSelectedWatch().getId()),
											String.valueOf(mGoal.getDateStampDay()), String.valueOf(mGoal.getDateStampMonth()),
											String.valueOf(mGoal.getDateStampYear()))
									.orderBy("_id", SORT_DESC)
											//.limit(2)
									.getResults(Goal.class);
						}
					}
					
					if    (goals.size() > 0) {
						Goal goal = goals.get(0);
						
						switch    (dashboardType) {
						case TYPE_STEPS:
							totalGoal += goal.getStepGoal();
							break;
						case TYPE_DISTANCE:
							totalGoal += goal.getDistanceGoal();
							break;
						case TYPE_CALORIES:
							totalGoal += goal.getCalorieGoal();
							break;
						}
						
					}
					
					calendarFrom.add(Calendar.DAY_OF_MONTH, 1);
				}
				
				double percent = totalValue / totalGoal;
				int parentWidth = 0;
				int width = 0;
				FrameLayout.LayoutParams params = null;
				
				switch(dashboardType) {
				case TYPE_STEPS:
					mStepsGoal.setText(getString(R.string.goal) + " " + mDecimalFormat2.format(totalGoal));
					parentWidth = getView().findViewById(R.id.frmDistanceGoalContainer).getMeasuredWidth();
					width = (int) ((double) parentWidth * percent);
					params = new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT);
					mStepsGoalValue.setLayoutParams(params);
					mStepsGoalValue.setBackgroundColor(colorForPercent(percent));
					break;
				case TYPE_DISTANCE:
					if(getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL)
						totalGoal *= MILE;
					mDistanceGoal.setText(getString(R.string.goal) + " " + mDecimalFormat.format(totalGoal));
					parentWidth = getView().findViewById(R.id.frmDistanceGoalContainer).getMeasuredWidth();
					width = (int) ((double) parentWidth * percent);
					params = new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT);
					mDistanceGoalValue.setLayoutParams(params);
					mDistanceGoalValue.setBackgroundColor(colorForPercent(percent));
					break;
				case TYPE_CALORIES:
					mCaloriesGoal.setText(getString(R.string.goal) + " " + mDecimalFormat2.format(totalGoal));
					parentWidth = getView().findViewById(R.id.frmCaloriesGoalContainer).getMeasuredWidth();
					width = (int) ((double) parentWidth * percent);
					params = new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT);
					mCaloriesGoalValue.setLayoutParams(params);
					mCaloriesGoalValue.setBackgroundColor(colorForPercent(percent));
					break;
				}
			} else if((getCalendarMode() == MODE_WEEK || getCalendarMode() == MODE_MONTH) && mPosition == 1) {
				Calendar calFrom = Calendar.getInstance();
				calFrom.setTime(from);
				
				double totalGoal = 0;
				
				while(calFrom.getTime().before(to) || calFrom.getTime().equals(to)) {
					Date date = calFrom.getTime();
					LifeTrakLogger.info("date: " + date);
					
					calFrom.set(Calendar.HOUR_OF_DAY, 0);
					calFrom.set(Calendar.MINUTE, 0);
					calFrom.set(Calendar.SECOND, 0);
					
					int day = calFrom.get(Calendar.DAY_OF_MONTH);
					int month = calFrom.get(Calendar.MONTH) + 1;
					int year2 = calFrom.get(Calendar.YEAR) - 1900;
					
					List<Goal> goals = DataSource.getInstance(getActivity())
												.getReadOperation()
												.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
														String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
														String.valueOf(day), String.valueOf(month),String.valueOf(year2))
												.orderBy("_id", SORT_DESC)
												.getResults(Goal.class);
					
					if    (goals.size() == 0) {
						goals = DataSource.getInstance(getActivity())
											.getReadOperation()
											.query("watchGoal = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
											.orderBy("abs(date - " + calFrom.getTimeInMillis() + ")", SORT_ASC)
											.limit(1)
											.getResults(Goal.class);

						Calendar now = Calendar.getInstance();

						if(now.get(Calendar.DATE) < calFrom.get(Calendar.DATE) ){
							Goal mGoal = goals.get(0);
							goals = DataSource
									.getInstance(getActivity())
									.getReadOperation()
									.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
											String.valueOf(getLifeTrakApplication()
													.getSelectedWatch().getId()),
											String.valueOf(mGoal.getDateStampDay()), String.valueOf(mGoal.getDateStampMonth()),
											String.valueOf(mGoal.getDateStampYear()))
									.orderBy("_id", SORT_DESC)
											//.limit(2)
									.getResults(Goal.class);
						}

					}
					
					if    (goals.size() > 0) {
						Goal goal = goals.get(0);
						
						switch    (dashboardType) {
						case TYPE_STEPS:
							totalGoal += goal.getStepGoal();
							break;
						case TYPE_DISTANCE:
							totalGoal += goal.getDistanceGoal();
							break;
						case TYPE_CALORIES:
							totalGoal += goal.getCalorieGoal();
							break;
						}
						
					}
					
					calFrom.add(Calendar.DAY_OF_MONTH, 1);
				}
				
				double percent = totalValue / totalGoal;
				int parentWidth = 0;
				int width = 0;
				FrameLayout.LayoutParams params = null;
				
				switch(dashboardType) {
				case TYPE_STEPS:
					mStepsGoal.setText(getString(R.string.goal) + " " + mDecimalFormat2.format(totalGoal));	
					parentWidth = getView().findViewById(R.id.frmStepsGoalContainer).getMeasuredWidth();
					width = (int) ((double) parentWidth * percent);
					params = new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT);
					mStepsGoalValue.setLayoutParams(params);
					mStepsGoalValue.setBackgroundColor(colorForPercent(percent));
					break;
				case TYPE_DISTANCE:
					if(getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL)
						totalGoal *= MILE;
					mDistanceGoal.setText(getString(R.string.goal) + " " + mDecimalFormat.format(totalGoal));
					parentWidth = getView().findViewById(R.id.frmDistanceGoalContainer).getMeasuredWidth();
					width = (int) ((double) parentWidth * percent);
					params = new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT);
					mDistanceGoalValue.setLayoutParams(params);
					mDistanceGoalValue.setBackgroundColor(colorForPercent(percent));
					break;
				case TYPE_CALORIES:
					mCaloriesGoal.setText(getString(R.string.goal) + " " + mDecimalFormat2.format(totalGoal));
					parentWidth = getView().findViewById(R.id.frmCaloriesGoalContainer).getMeasuredWidth();
					width = (int) ((double) parentWidth * percent);
					params = new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT);
					mCaloriesGoalValue.setLayoutParams(params);
					mCaloriesGoalValue.setBackgroundColor(colorForPercent(percent));
					break;
				}
			}
		}
	}
	
	private void initializeGoalsWithDate(final Date from, final Date to, final int dashboardType, final double totalValue) {
		if(!isAdded())
			return;
		
		String field = "stepGoal";
		
		switch(dashboardType) {
		case TYPE_STEPS:
			field = "stepGoal";
			break;
		case TYPE_DISTANCE:
			field = "distanceGoal";
			break;
		case TYPE_CALORIES:
			field = "calorieGoal";
			break;
		}
		
		String query = "";
		Cursor cursor = null;
		
		final Calendar calendarFrom = Calendar.getInstance();
		calendarFrom.setTime(from);
		
		mMetricGoal.setText("0");
		mMetricGoalPercent.setText("0%");
		
		if(getCalendarMode() == MODE_DAY) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(from);
			
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH) + 1;
			int year = calendar.get(Calendar.YEAR) - 1900;
			
			List<Goal> goals = DataSource.getInstance(getActivity())
											.getReadOperation()
											.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
													String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
													String.valueOf(day), String.valueOf(month),String.valueOf(year))
											.orderBy("_id", SORT_DESC)
											.getResults(Goal.class);
			
			if (goals.size() == 0) {
				goals = DataSource.getInstance(getActivity())
									.getReadOperation()
									.query("watchGoal = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
									.orderBy("abs(date - " + from.getTime() + ")", SORT_ASC)
									.limit(1)
									.getResults(Goal.class);

				Calendar now = Calendar.getInstance();

				if(now.get(Calendar.DATE) < calendar.get(Calendar.DATE) ){
					Goal mGoal = goals.get(0);
					goals = DataSource
							.getInstance(getActivity())
							.getReadOperation()
							.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
									String.valueOf(getLifeTrakApplication()
											.getSelectedWatch().getId()),
									String.valueOf(mGoal.getDateStampDay()), String.valueOf(mGoal.getDateStampMonth()),
									String.valueOf(mGoal.getDateStampYear()))
							.orderBy("_id", SORT_DESC)
									//.limit(2)
							.getResults(Goal.class);
				}
			}
			
			float goal = 0;
			
			if(goals.size() > 0) {
				Goal goalItem = goals.get(0);
				
				switch(dashboardType) {
				case TYPE_STEPS:
					goal = goalItem.getStepGoal();
					break;
				case TYPE_DISTANCE:
					goal = (float) goalItem.getDistanceGoal();
					break;
				case TYPE_CALORIES:
					goal = goalItem.getCalorieGoal();
					break;
				}
				
			} else {
				query = "select " + field + " from Goal where watchGoal = ? order by (date - " + from.getTime() + ") limit 1";
				
				cursor = DataSource.getInstance(getActivity())
									.getReadOperation()
									.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));
				
				if(cursor.moveToFirst())
					goal = cursor.getFloat(0);
			}
			
			float percent = ((float)totalValue / (float)goal);
			
			if(dashboardType == TYPE_DISTANCE) {
				float imperialGoal = goal;
				
				if(getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL) {
					percent = ((float)(totalValue / MILE)) / (float) goal;
					imperialGoal *= MILE;
				}
				//mMetricGoal.setText(mDecimalFormat.format(imperialGoal));
				mMetricGoal.setText(""+Math.round(imperialGoal*100)/100.00);
			} else {
				mMetricGoal.setText(mDecimalFormat2.format(goal));
			}
			mMetricGoalPercent.setText(String.valueOf((int)(percent * 100.0f)) + "%");
			float width = dpToPx(170) * percent;
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int)width, FrameLayout.LayoutParams.MATCH_PARENT);
			mMetricProgressValue.setLayoutParams(layoutParams);
			
			int totalPercent = (int)(percent * 100.0f);
			
			if(totalPercent >= 75) {
				mMetricGoalPercent.setTextColor(getResources().getColor(R.color.color_100_percent));
				mMetricProgressValue.setBackgroundColor(getResources().getColor(R.color.color_100_percent));
				mMetricGoalIcon.setImageResource(R.drawable.ll_fitnessres_icon_goal_05);
			} else if(totalPercent >= 50) {
				mMetricGoalPercent.setTextColor(getResources().getColor(R.color.color_75_percent));
				mMetricProgressValue.setBackgroundColor(getResources().getColor(R.color.color_75_percent));
				mMetricGoalIcon.setImageResource(R.drawable.ll_fitnessres_icon_goal_04);
			} else if(totalPercent >= 25) {
				mMetricGoalPercent.setTextColor(getResources().getColor(R.color.color_50_percent));
				mMetricProgressValue.setBackgroundColor(getResources().getColor(R.color.color_50_percent));
				mMetricGoalIcon.setImageResource(R.drawable.ll_fitnessres_icon_goal_03);
			} else if(totalPercent > 0) {
				mMetricGoalPercent.setTextColor(getResources().getColor(R.color.color_25_percent));
				mMetricProgressValue.setBackgroundColor(getResources().getColor(R.color.color_25_percent));
				mMetricGoalIcon.setImageResource(R.drawable.ll_fitnessres_icon_goal_02);
			} else {
				mMetricGoalPercent.setTextColor(getResources().getColor(R.color.color_gray));
				mMetricProgressValue.setBackgroundColor(getResources().getColor(R.color.color_gray));
				mMetricGoalIcon.setImageResource(R.drawable.ll_fitnessres_icon_goal_01);
			}
		} else {
			final double totalValue01 = totalValue;
			
			synchronized(LOCK_OBJECT) {
				if(!isAdded())
					return;
				
				double totalGoal = 0;
				Goal lastGoal;
				while(calendarFrom.getTime().before(to) || calendarFrom.getTime().equals(to)) {
					calendarFrom.set(Calendar.HOUR_OF_DAY, 0);
					calendarFrom.set(Calendar.MINUTE, 0);
					calendarFrom.set(Calendar.SECOND, 0);
					
					int day = calendarFrom.get(Calendar.DAY_OF_MONTH);
					int month = calendarFrom.get(Calendar.MONTH) + 1;
					int year = calendarFrom.get(Calendar.YEAR) - 1900;
					
					List<Goal> goals = DataSource.getInstance(getActivity())
												.getReadOperation()
												.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
														String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
														String.valueOf(day), String.valueOf(month),String.valueOf(year))
												.orderBy("_id", SORT_DESC)
												.getResults(Goal.class);


					
					if    (goals.size() == 0) {
						goals = DataSource.getInstance(getActivity())
								.getReadOperation()
								.query("watchGoal = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
								.orderBy("abs(date - " + calendarFrom.getTimeInMillis() + ")", SORT_ASC)
								.limit(2)
								.getResults(Goal.class);
						Calendar now = Calendar.getInstance();

						if(now.get(Calendar.DATE) < calendarFrom.get(Calendar.DATE) ){
							Goal mGoal = goals.get(0);
							goals = DataSource
									.getInstance(getActivity())
									.getReadOperation()
									.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
											String.valueOf(getLifeTrakApplication()
													.getSelectedWatch().getId()),
											String.valueOf(mGoal.getDateStampDay()), String.valueOf(mGoal.getDateStampMonth()),
											String.valueOf(mGoal.getDateStampYear()))
									.orderBy("_id", SORT_DESC)
											//.limit(2)
									.getResults(Goal.class);
						}

					}

					
					if    (goals.size() > 0) {
						Goal goal = goals.get(0);

						switch    (dashboardType) {
						case TYPE_STEPS:
							totalGoal += goal.getStepGoal();
							break;
						case TYPE_DISTANCE:
							totalGoal += goal.getDistanceGoal();
							break;
						case TYPE_CALORIES:
							totalGoal += goal.getCalorieGoal();
							break;
						}
						
					}
					
					calendarFrom.add(Calendar.DAY_OF_MONTH, 1);
				}
				
				if (dashboardType == TYPE_DISTANCE)
					if(getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL)
						totalGoal *= MILE;
					
				final double totalGaol01 = totalGoal;
				
				float percent = ((float)totalValue01 / (float)totalGaol01);
				
				if(dashboardType == TYPE_DISTANCE) {
					mMetricGoal.setText(mDecimalFormat.format(totalGaol01));
				} else {
					mMetricGoal.setText(mDecimalFormat2.format(totalGaol01));
				}
				
				mMetricGoalPercent.setText((int)(percent * 100.0f) + "%");
				float width = dpToPx(170) * percent;
				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int)width, FrameLayout.LayoutParams.MATCH_PARENT);
				mMetricProgressValue.setLayoutParams(layoutParams);
				
				int totalPercent = (int)Math.floor(percent * 100.0f);
				
				if(totalPercent >= 75) {
					mMetricGoalPercent.setTextColor(getResources().getColor(R.color.color_100_percent));
					mMetricProgressValue.setBackgroundColor(getResources().getColor(R.color.color_100_percent));
					mMetricGoalIcon.setImageResource(R.drawable.ll_fitnessres_icon_goal_05);
				} else if(totalPercent >= 50) {
					mMetricGoalPercent.setTextColor(getResources().getColor(R.color.color_75_percent));
					mMetricProgressValue.setBackgroundColor(getResources().getColor(R.color.color_75_percent));
					mMetricGoalIcon.setImageResource(R.drawable.ll_fitnessres_icon_goal_04);
				} else if(totalPercent >= 25) {
					mMetricGoalPercent.setTextColor(getResources().getColor(R.color.color_50_percent));
					mMetricProgressValue.setBackgroundColor(getResources().getColor(R.color.color_50_percent));
					mMetricGoalIcon.setImageResource(R.drawable.ll_fitnessres_icon_goal_03);
				} else if(totalPercent > 0) {
					mMetricGoalPercent.setTextColor(getResources().getColor(R.color.color_25_percent));
					mMetricProgressValue.setBackgroundColor(getResources().getColor(R.color.color_25_percent));
					mMetricGoalIcon.setImageResource(R.drawable.ll_fitnessres_icon_goal_02);
				} else {
					mMetricGoalPercent.setTextColor(getResources().getColor(R.color.color_gray));
					mMetricProgressValue.setBackgroundColor(getResources().getColor(R.color.color_gray));
					mMetricGoalIcon.setImageResource(R.drawable.ll_fitnessres_icon_goal_01);
				}
				
				if (orientation() == Configuration.ORIENTATION_PORTRAIT) {
					// Return layout
					mFitnessResultsTopData.setVisibility(View.VISIBLE);
					mFitnessResultsPlotContainer.setVisibility(View.VISIBLE);
					mFitnessResultsCenterContainer.setVisibility(View.VISIBLE);
					mFitnessResultsLoadingText.setVisibility(View.GONE);
					
				}
			}
		}
	}
	
	public void setDataWithDay(Date date) {
		if(!isAdded())
			return;
		
		mDateNow = date;
		
		resizeGraphContainer((int)dpToPx(5000));
		//int graphWidth = (int) dpToPx(sizeWithCalendarMode(MODE_DAY));
		//resizeGraphContainer(graphWidth);
		
		List<Double> values = createValuesForDay(date, mDashboardType);
		
		setDataForMetric(values);
		initializeStats(mDashboardType, values, date, date);
		initializeGraph(values, mDashboardType, MODE_DAY);
	}
	
	public void setDataWithWeek(Date from, Date to) {
		if(!isAdded())
			return;
		
		mDateFrom = from;
		mDateTo = to;
		
		resizeGraphContainer((int)dpToPx(2500));
		//int graphWidth = (int) dpToPx(sizeWithCalendarMode(MODE_WEEK));
		//resizeGraphContainer(graphWidth);
		
		List<Double> values = createValuesForWeek(from, to, mDashboardType);
		
		setDataForMetric(values);
		initializeStats(mDashboardType, values, from, to);
		initializeGraph(values, mDashboardType, MODE_WEEK);
	}
	
	public void setDataWithMonth(Date from, Date to) {
		if(!isAdded())
			return;
		mDateFrom = from;
		mDateTo = to;
		
		resizeGraphContainer((int)dpToPx(1200));
		
		List<Double> values = createValuesForMonth(from, to, mDashboardType);
		
		setDataForMetric(values);
		initializeStats(mDashboardType, values, from, to);
		initializeGraph(values, mDashboardType, MODE_MONTH);
	}
	
	public void setDataWithYear(int year) {
		if(!isAdded())
			return;
		
		mYear = year;
		
		final List<Double> values = createValuesForYear(year, mDashboardType);
		
		final Calendar calendarFrom = Calendar.getInstance();
		final Calendar calendarTo = Calendar.getInstance();
		
		calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
		calendarFrom.set(Calendar.MONTH, 0);
		calendarFrom.set(Calendar.YEAR, year);
		
		calendarTo.set(Calendar.DAY_OF_MONTH, 31);
		calendarTo.set(Calendar.MONTH, 11);
		calendarTo.set(Calendar.YEAR, year);
		
		resizeGraphContainer((int)dpToPx(1000));
		//int graphWidth = (int) dpToPx(sizeWithCalendarMode(MODE_YEAR));
		//resizeGraphContainer(graphWidth);
		
		setDataForMetric(values);
		
		mHandlerDelay.postDelayed(new Runnable() {
			public void run() {
				initializeGraph(values, mDashboardType, MODE_YEAR);
				initializeStats(mDashboardType, values, calendarFrom.getTime(), calendarTo.getTime());
			}
		}, 10);
	}
	
	private void setDataWithYear(final int year, int position) {
		if(!isAdded())
			return;
		
		mYear = year;
		
		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			setDataWithYear(year);
		} else {
			if(position == 1) {
				new Thread(new Runnable() {
					public void run() {		
						final List<Double> values = createValuesForYear(year, mDashboardType);
						
						mHandlerDelay.post(new Runnable() {
							public void run() {
								Calendar calendarFrom = Calendar.getInstance();
								Calendar calendarTo = Calendar.getInstance();
								
								calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
								calendarFrom.set(Calendar.MONTH, 0);
								calendarFrom.set(Calendar.YEAR, year);
								
								calendarTo.set(Calendar.DAY_OF_MONTH, 31);
								calendarTo.set(Calendar.MONTH, 11);
								calendarTo.set(Calendar.YEAR, year);
								
								//int graphWidth = (int) dpToPx(sizeWithCalendarMode(MODE_YEAR));
								//resizeGraphContainer(graphWidth);
								
								initializeStats(mDashboardType, values, calendarFrom.getTime(), calendarTo.getTime());
								initializeGraph(values, mDashboardType, MODE_YEAR);
							}
						});
					}
				}).start();
			}
		}
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
		if(mFitnessResultsPlotContainer != null && orientation() == Configuration.ORIENTATION_LANDSCAPE) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, RelativeLayout.LayoutParams.MATCH_PARENT);
			params.setMargins(0, (int) dpToPx(10), 0, 0);
			params.addRule(RelativeLayout.RIGHT_OF, R.id.viewGraphLeftPadding);
			mFitnessResultsPlotContainer.setLayoutParams(params);
		}
	}
	
	private float widthOfBar() {
		float barWidth = 0f;
		int metricCount = 0;
		
		if    (!mHeartRateLabel.isChecked()) {
			metricCount += 1;
		}
		
		if    (!mStepsLabel.isChecked()) {
			metricCount += 1;
		}
		
		if    (!mCaloriesLabel.isChecked()) {
			metricCount += 1;
		}
		
		if    (!mDistanceLabel.isChecked()) {
			metricCount += 1;
		}
		
		barWidth = metricCount * 5f + 5f;
		
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
		Date from = getLifeTrakApplication().getDateRangeFrom();
		Date to = getLifeTrakApplication().getDateRangeTo();
		
		switch(getCalendarMode()) {
		case MODE_DAY:
			values = createValuesForDay(getLifeTrakApplication().getCurrentDate(), type);
			break;
		case MODE_WEEK:
			values = createValuesForWeek(from, to, type);
			break;
		case MODE_MONTH:
			values = createValuesForMonth(from, to, type);
			break;
		case MODE_YEAR:
			values = createValuesForYear(getLifeTrakApplication().getCurrentYear(), type);
			break;
		}
		
		return values;
	}
	
	private List<Double> createValues(int type, Date from, Date to, int calendarMode) {
		List<Double> values = new ArrayList<Double>();
		
		switch(calendarMode) {
		case MODE_DAY:
			values = createValuesForDay(getLifeTrakApplication().getCurrentDate(), type);
			break;
		case MODE_WEEK:
			values = createValuesForWeek(from, to, type);
			break;
		case MODE_MONTH:
			values = createValuesForMonth(from, to, type);
			break;
		case MODE_YEAR:
			values = createValuesForYear(getLifeTrakApplication().getCurrentYear(), type);
			break;
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
		
		if (getLifeTrakApplication().getSelectedWatch() == null) {
			return null;
		}
		Cursor cursor = DataSource.getInstance(getActivity())
									.getReadOperation()
									.rawQuery(query, String.valueOf(day), String.valueOf(month), String.valueOf(year),
											String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));
		
		List<Double> values = DataSource.getInstance(getActivity())
										.getReadOperation()
										.cursorToList(cursor);
		
		return values;
	}
	
	private XYSeries createSeriesForDay(Date date, int dashboardType) {
		XYSeries xySeries = new XYSeries(columnWithDashboardType(dashboardType));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;
		
		double maxValue = 0;
		
		String query = "select max(" + columnWithDashboardType(dashboardType) + ") from StatisticalDataPoint dataPoint " +
				"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
				"where dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and dataHeader.watchDataHeader = ?";
		
		Cursor cursor = DataSource.getInstance(getActivity())
				.getReadOperation()
				.rawQuery(query, String.valueOf(day), String.valueOf(month), String.valueOf(year),
							String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));
		
		if(cursor.moveToFirst()) {
			maxValue = cursor.getDouble(0);
		}
		
		cursor.close();
		
		query = "select " + columnWithDashboardType(dashboardType) + " from StatisticalDataPoint dataPoint " +
					"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
					"where dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and dataHeader.watchDataHeader = ?";
		
		cursor = DataSource.getInstance(getActivity())
									.getReadOperation()
									.rawQuery(query, String.valueOf(day), String.valueOf(month), String.valueOf(year),
												String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));
		int x = 0;
		
		while(cursor.moveToNext()) {
			double value = cursor.getDouble(0);
			double y = getYWithMaxValue(value, maxValue, FITNESS_RESULTS_MIN_Y, FITNESS_RESULTS_MAX_Y);
			xySeries.add(x, y);
			x++;
		}
		
		cursor.close();
		return xySeries;
	}
	
	private List<Double> createValuesForWeek(Date from, Date to, int dashboardType) {
		Calendar calendarFrom = Calendar.getInstance();
		Calendar calendarTo = Calendar.getInstance();
		
		calendarFrom.setTime(from);
		calendarTo.setTime(to);
		List<Double> values = new ArrayList<Double>();
		if (dashboardType == TYPE_HEART_RATE) {
			LifeTrakLogger.info("heart rate values");
			if(dashboardType == TYPE_HEART_RATE) {
				while (calendarFrom.getTime().before(calendarTo.getTime()) || calendarFrom.getTime().equals(calendarTo.getTime())) {
					int day = calendarFrom.get(Calendar.DAY_OF_MONTH);
					int month = calendarFrom.get(Calendar.MONTH) + 1;
					int year = calendarFrom.get(Calendar.YEAR) - 1900;

					List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(getActivity())
							.getReadOperation()
							.query("watchDataHeader == ? AND dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ?",
									String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
									String.valueOf(day), String.valueOf(month), String.valueOf(year))
							.getResults(StatisticalDataHeader.class);
					if (dataHeaders.size() > 0) {
						StatisticalDataHeader dataHeader = dataHeaders.get(0);
						String column = columnWithDashboardType(dashboardType);

						List<Number> dataPointVals = DataSource.getInstance(getActivity())
								.getReadOperation()
								.query("dataHeaderAndPoint == ?", String.valueOf(dataHeader.getId()))
								.getResults(StatisticalDataPoint.class, column);

						double value = 0;
						int index = 0;
						int sumValue = 0;
						for (Number dataPointVal : dataPointVals) {
							value += dataPointVal.doubleValue();

							if (dataPointVal.doubleValue() > 0)	{
								sumValue++;
							}
							index++;

							if (index % 12 == 0) {
								if (sumValue == 0){
									values.add(0.0);
								}
								else {
									int avgValue = (int) value / sumValue;
									values.add((double) avgValue);
									value = 0;
									sumValue = 0;
								}
							}
							if (value > 0 && index == dataPointVals.size()) {
								int avgValue = (int) value / sumValue;
								values.add((double) avgValue);
								value = 0;
								sumValue = 0;
							}
						}
					} else {
						values.add(0.0);
						values.add(0.0);
						values.add(0.0);
						values.add(0.0);
						values.add(0.0);
						values.add(0.0);

						values.add(0.0);
						values.add(0.0);
						values.add(0.0);
						values.add(0.0);
						values.add(0.0);
						values.add(0.0);
					}
					calendarFrom.add(Calendar.DAY_OF_MONTH, 1);
				}
			}
		}
		else {
			while (calendarFrom.getTime().before(calendarTo.getTime()) || calendarFrom.getTime().equals(calendarTo.getTime())) {
				int day = calendarFrom.get(Calendar.DAY_OF_MONTH);
				int month = calendarFrom.get(Calendar.MONTH) + 1;
				int year = calendarFrom.get(Calendar.YEAR) - 1900;

				List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(getActivity())
						.getReadOperation()
						.query("watchDataHeader == ? AND dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ?",
								String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
								String.valueOf(day), String.valueOf(month), String.valueOf(year))
						.getResults(StatisticalDataHeader.class);
				if (dataHeaders.size() > 0) {
					StatisticalDataHeader dataHeader = dataHeaders.get(0);
					String column = columnWithDashboardType(dashboardType);

					List<Number> dataPointVals = DataSource.getInstance(getActivity())
							.getReadOperation()
							.query("dataHeaderAndPoint == ?", String.valueOf(dataHeader.getId()))
							.getResults(StatisticalDataPoint.class, column);

					double value = 0;
					int index = 0;

					for (Number dataPointVal : dataPointVals) {
						value += dataPointVal.doubleValue();

						index++;

						if (index % 12 == 0) {
							int avgValue = ((int) value / 12) * 100;
							values.add(value);
							value = 0;
						}
						if (value > 0 && index == dataPointVals.size()) {
							int avgValue = ((int) value / dataPointVals.size()) * 100;
							values.add(value);
							value = 0;
						}
					}
				} else {
					values.add(0.0);
					values.add(0.0);
					values.add(0.0);
					values.add(0.0);
					values.add(0.0);
					values.add(0.0);

					values.add(0.0);
					values.add(0.0);
					values.add(0.0);
					values.add(0.0);
					values.add(0.0);
					values.add(0.0);
				}
				calendarFrom.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		return values;
	}
	
	private List<Double> createValuesForMonth(Date from, Date to, int dashboardType) {
		Calendar calendarFrom = Calendar.getInstance();
		Calendar calendarTo = Calendar.getInstance();
		
		calendarFrom.setTime(from);
		calendarTo.setTime(to);
		
		List<Double> values = new ArrayList<Double>();
		if(dashboardType == TYPE_HEART_RATE) {
			while (calendarFrom.getTime().before(calendarTo.getTime()) || calendarFrom.getTime().equals(calendarTo.getTime())) {
				int day = calendarFrom.get(Calendar.DAY_OF_MONTH);
				int month = calendarFrom.get(Calendar.MONTH) + 1;
				int year = calendarFrom.get(Calendar.YEAR) - 1900;

				String query = "select avg(" + columnWithDashboardType(dashboardType) + "),count(dataHeader._id) from StatisticalDataPoint dataPoint " +
						"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
						"where dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and dataHeader.watchDataHeader = ? and " + columnWithDashboardType(dashboardType) + " > 0";

				Cursor cursor = DataSource.getInstance(getActivity())
						.getReadOperation()
						.rawQuery(query, String.valueOf(day), String.valueOf(month), String.valueOf(year),
								String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));

				if (cursor.moveToFirst()) {
					values.add(cursor.getDouble(0));
				} else {
					values.add(0.0);
				}

				cursor.close();

				calendarFrom.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		else {
			while (calendarFrom.getTime().before(calendarTo.getTime()) || calendarFrom.getTime().equals(calendarTo.getTime())) {
				int day = calendarFrom.get(Calendar.DAY_OF_MONTH);
				int month = calendarFrom.get(Calendar.MONTH) + 1;
				int year = calendarFrom.get(Calendar.YEAR) - 1900;

				String query = "select sum(" + columnWithDashboardType(dashboardType) + "),count(dataHeader._id) from StatisticalDataPoint dataPoint " +
						"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
						"where dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and dataHeader.watchDataHeader = ?";

				Cursor cursor = DataSource.getInstance(getActivity())
						.getReadOperation()
						.rawQuery(query, String.valueOf(day), String.valueOf(month), String.valueOf(year),
								String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));

				if (cursor.moveToFirst()) {
					values.add(cursor.getDouble(0));
				} else {
					values.add(0.0);
				}

				cursor.close();

				calendarFrom.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		return values;
	}
	
	private List<Double> createValuesForYear(int year, int dashboardType) {
		List<Double> values = new ArrayList<Double>();
		
		if(dashboardType == TYPE_HEART_RATE) {
			for(int i=0;i<12;i++) {
				String query = "select avg(" + columnWithDashboardType(dashboardType) + ") from StatisticalDataPoint dataPoint " +
						"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
						"where dateStampMonth = ? and dateStampYear = ? and dataHeader.watchDataHeader = ? and " + columnWithDashboardType(dashboardType) + " > 0";
				
				Cursor cursor = DataSource.getInstance(getActivity())
											.getReadOperation()
											.rawQuery(query, String.valueOf(i + 1), String.valueOf(year - 1900),
														String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));
				
				if(cursor.moveToFirst()) {
					double value = cursor.getDouble(0);
					values.add(value);
				} else {
					values.add(0.0);
				}
				cursor.close();
			}
		} else {
			for(int i=0;i<12;i++) {
				String query = "select sum(" + columnWithDashboardType(dashboardType) + ") from StatisticalDataPoint dataPoint " +
						"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
						"where dateStampMonth = ? and dateStampYear = ? and dataHeader.watchDataHeader = ?";
				
				Cursor cursor = DataSource.getInstance(getActivity())
											.getReadOperation()
											.rawQuery(query, String.valueOf(i + 1), String.valueOf(year - 1900),
														String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));
				
				if(cursor.moveToFirst()) {
					double value = cursor.getDouble(0);
					values.add(value);
				} else {
					values.add(0.0);
				}
				cursor.close();
			}
		}
		
		return values;
	}
	
	private void initializeGraph(List<Double> values, int type, int calendarMode) {
		if(!isAdded())
			return;
		
		mFitnessResultsPlotContainer.removeAllViews();
		
		mMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
		mMultipleSeriesDataset = new XYMultipleSeriesDataset();
		
		mMultipleSeriesRenderer.setZoomEnabled(false, false);
		mMultipleSeriesRenderer.setYLabels(0);
		mMultipleSeriesRenderer.setShowGridY(false);
		mMultipleSeriesRenderer.setXLabels(0);
		
		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			XYSeries xySeries = new XYSeries(getString(R.string.metric));
			double maxValue = 0;
			
			if(values.size() > 0) {
				for(Double value : values) {
					maxValue = Math.max(maxValue, value.doubleValue());
				}
			} else {
				for (int i=0;i<144;i++) {
					values.add(0.0);
				}
			}
			
			int x = 0;
			
			for(Double value : values) {
				double v = getYWithMaxValue(value.doubleValue(), maxValue, FITNESS_RESULTS_MIN_Y, FITNESS_RESULTS_MAX_Y);
				xySeries.add(x, v);
				x++;
			}
			
			mMultipleSeriesDataset.addSeries(xySeries);
			
			switch(type) {
			case TYPE_HEART_RATE:
				mMultipleSeriesRenderer.removeSeriesRenderer(mHeartRateRenderer);
				mMultipleSeriesRenderer.addSeriesRenderer(mHeartRateRenderer);
				break;
			case TYPE_STEPS:
				mMultipleSeriesRenderer.removeSeriesRenderer(mStepsRenderer);
				mMultipleSeriesRenderer.addSeriesRenderer(mStepsRenderer);
				break;
			case TYPE_CALORIES:
				mMultipleSeriesRenderer.removeSeriesRenderer(mCaloriesRenderer);
				mMultipleSeriesRenderer.addSeriesRenderer(mCaloriesRenderer);
				break;
			case TYPE_DISTANCE:
				mMultipleSeriesRenderer.removeSeriesRenderer(mDistanceRenderer);
				mMultipleSeriesRenderer.addSeriesRenderer(mDistanceRenderer);
				break;
			}
			
			switch(calendarMode) {
			case MODE_DAY:
				mMultipleSeriesRenderer.setXLabelsAlign(Align.RIGHT);
				//mMultipleSeriesRenderer.setXAxisMin(0);
				mMultipleSeriesRenderer.setXAxisMin(-6);
				mMultipleSeriesRenderer.setXAxisMax(144);
				mMultipleSeriesRenderer.setLabelsTextSize(dpToPx(13));
				
				switch(getLifeTrakApplication().getTimeDate().getHourFormat()) {
				case TIME_FORMAT_12_HR:
					mMultipleSeriesRenderer.addXTextLabel(7, getString(R.string.am));
					mMultipleSeriesRenderer.addXTextLabel(144, getString(R.string.pm));
					break;
				case TIME_FORMAT_24_HR:
					mMultipleSeriesRenderer.addXTextLabel(9, "0:00");
					mMultipleSeriesRenderer.addXTextLabel(144, "23:59");
					break;
				}
				
				mMultipleSeriesRenderer.setShowTickMarks(false);
				break;
			case MODE_WEEK:
				mMultipleSeriesRenderer.setXLabelsAlign(Align.RIGHT);
				//mMultipleSeriesRenderer.setXAxisMin(0);
				mMultipleSeriesRenderer.setXAxisMin(-7);
				mMultipleSeriesRenderer.setXAxisMax(12*7);
				mMultipleSeriesRenderer.setLabelsTextSize(dpToPx(13));
				mMultipleSeriesRenderer.addXTextLabel(10, mDateFormat.format(mDateFrom));
				mMultipleSeriesRenderer.addXTextLabel(12*7, mDateFormat.format(mDateTo));
				mMultipleSeriesRenderer.setShowTickMarks(false);
				break;
			case MODE_MONTH:
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(mDateFrom);
				int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				
				mMultipleSeriesRenderer.setXLabelsAlign(Align.CENTER);
				//mMultipleSeriesRenderer.setXAxisMin(0);
				mMultipleSeriesRenderer.setXAxisMin(-1);
				mMultipleSeriesRenderer.setXAxisMax(maxDays);
				mMultipleSeriesRenderer.setXLabels(0);
				mMultipleSeriesRenderer.setLabelsTextSize(dpToPx(13));
				mMultipleSeriesRenderer.setShowTickMarks(true);
				
				for(int i=0;i<maxDays;i++) {
					if(i % 2 == 0) {
						mMultipleSeriesRenderer.addXTextLabel(i, String.valueOf(i + 1));
					}
				}
				break;
			case MODE_YEAR:
				final int numberOfMonths = 12;
				mMultipleSeriesRenderer.setXLabelsAlign(Align.LEFT);
				//mMultipleSeriesRenderer.setXAxisMin(0);
				mMultipleSeriesRenderer.setXAxisMin(-1);
				mMultipleSeriesRenderer.setXAxisMax(numberOfMonths);
				mMultipleSeriesRenderer.setLabelsTextSize(dpToPx(9));
				mMultipleSeriesRenderer.setShowTickMarks(true);
				
				for(int i=0;i<numberOfMonths;i++) {
					mMultipleSeriesRenderer.addXTextLabel(i, mMonths[i]);
				}
				break;
			}
			
			mMultipleSeriesRenderer.setBarWidth(dpToPx(2.1f));
			mMultipleSeriesRenderer.setPanEnabled(false);
			mMultipleSeriesRenderer.setMargins(new int[] {0,(int)dpToPx(5),0,(int)dpToPx(5)});
		} else {
			if(mHeartRateValues == null)
				mHeartRateValues = createValues(TYPE_HEART_RATE, mDateFrom, mDateTo, calendarMode);
			if(mStepsValues == null)
				mStepsValues = createValues(TYPE_STEPS, mDateFrom, mDateTo, calendarMode);
			if(mCaloriesValues == null)
				mCaloriesValues = createValues(TYPE_CALORIES, mDateFrom, mDateTo, calendarMode);
			if(mDistanceValues == null)
				mDistanceValues = createValues(TYPE_DISTANCE, mDateFrom, mDateTo, calendarMode);
			
			switch(type) {
			case TYPE_HEART_RATE:
				mHeartRateLabel.setChecked(true);
				break;
			case TYPE_STEPS:
				mStepsLabel.setChecked(true);
				break;
			case TYPE_CALORIES:
				mCaloriesLabel.setChecked(true);
				break;
			case TYPE_DISTANCE:
				mDistanceLabel.setChecked(true);
				break;
			}
			
			if(mHeartRateLabel.isChecked()) {
				if (mHeartRateValues.size() == 0) {
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
			if(mStepsLabel.isChecked()) {
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
			if(mCaloriesLabel.isChecked()) {
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
			if(mDistanceLabel.isChecked()) {
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
			
			initializeStatsLand(TYPE_STEPS, mStepsValues, mDateFrom, mDateTo);
			initializeStatsLand(TYPE_CALORIES, mCaloriesValues, mDateFrom, mDateTo);
			initializeStatsLand(TYPE_DISTANCE, mDistanceValues, mDateFrom, mDateTo);
			
			Calendar calendar = Calendar.getInstance();
			
			switch(calendarMode) {
			case MODE_DAY:
				mMultipleSeriesRenderer.setXAxisMax(144);
				int index = 0;
				
				for(int i=0;i<144;i++) {
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
				break;
			case MODE_WEEK:
				mMultipleSeriesRenderer.setXAxisMax(12*7);
				
				calendar.setTime(mDateFrom);
				
				for(int i=0;i<7*12;i++) {
					if(i % 12 == 0) {
						mMultipleSeriesRenderer.addXTextLabel(i, mDateFormat.format(calendar.getTime()));
						calendar.add(Calendar.DAY_OF_MONTH, 1);
					}
				}
				break;
			case MODE_MONTH:
				
				calendar.setTime(mDateFrom);
				int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				
				mMultipleSeriesRenderer.setXAxisMax(maxDays);
				
				for(int i=0;i<maxDays;i++) {
					mMultipleSeriesRenderer.addXTextLabel(i, String.valueOf(i+1));
				}
				break;
			case MODE_YEAR:
				mMultipleSeriesRenderer.setXAxisMax(12);
				
				for(int i=0;i<12;i++) {
					mMultipleSeriesRenderer.addXTextLabel(i, mMonths[i]);
				}
				break;
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
		}
		
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
		mFitnessResultsPlotContainer.addView(mGraphView);
	}
	
	private final RadioGroup.OnCheckedChangeListener mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(RadioGroup view, int checkedId) {
			MainActivity mainActivity = (MainActivity) getActivity();
			
			switch(checkedId) {
			case R.id.radDay:
				mainActivity.setCalendarMode(MODE_DAY);
				mCalendarMode = MODE_DAY;
				break;
			case R.id.radWeek:
				mainActivity.setCalendarMode(MODE_WEEK);
				mCalendarMode = MODE_WEEK;
				break;
			case R.id.radMonth:
				mainActivity.setCalendarMode(MODE_MONTH);
				mCalendarMode = MODE_MONTH;
				break;
			case R.id.radYear:
				mainActivity.setCalendarMode(MODE_YEAR);
				mCalendarMode = MODE_YEAR;
				break;
			}
		}
	};
	
	private final AdapterView.OnItemSelectedListener mItemSelectedListener = new AdapterView.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if(mPosition == 1) {
				MainActivity mainActivity = (MainActivity) getActivity();
				
				mHeartRateValues = null;
				mStepsValues = null;
				mDistanceValues = null;
				mCaloriesValues = null;
				
				switch(position) {
				case 0:
					mainActivity.setCalendarMode(MODE_DAY);
					mCalendarMode = MODE_DAY;
					break;
				case 1:
					mainActivity.setCalendarMode(MODE_WEEK);
					mCalendarMode = MODE_WEEK;
					break;
				case 2:
					mainActivity.setCalendarMode(MODE_MONTH);
					mCalendarMode = MODE_MONTH;
					break;
				case 3:
					mainActivity.setCalendarMode(MODE_YEAR);
					mCalendarMode = MODE_YEAR;
					break;
				}
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
	};
	
	private boolean isRemainingCheckbox(CompoundButton buttonView) {
		switch(buttonView.getId()) {
		case R.id.chkHeartRateLabel:
			return !(mStepsLabel.isChecked() || mCaloriesLabel.isChecked() || mDistanceLabel.isChecked());
		case R.id.chkStepsLabel:
			return !(mHeartRateLabel.isChecked() || mCaloriesLabel.isChecked() || mDistanceLabel.isChecked());
		case R.id.chkCaloriesLabel:
			return !(mHeartRateLabel.isChecked() || mStepsLabel.isChecked() || mDistanceLabel.isChecked());
		case R.id.chkDistanceLabel:
			return !(mHeartRateLabel.isChecked() || mStepsLabel.isChecked() || mCaloriesLabel.isChecked());
		}
		return false;
	}
	
	private final CompoundButton.OnCheckedChangeListener mLandscapeStatCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			
			if(isRemainingCheckbox(buttonView)) {
				buttonView.setChecked(true);
				return;
			}
			
			switch(buttonView.getId()) {
			case R.id.chkHeartRateLabel:
				if(isChecked) {
					mHeartRateValues = createValues(TYPE_HEART_RATE);
					mHeartRateSeries = createSeries(mHeartRateValues, getString(R.string.heart_rate_small));
					mMultipleSeriesDataset.removeSeries(mHeartRateSeries);
					mMultipleSeriesDataset.addSeries(mHeartRateSeries);
					mMultipleSeriesRenderer.removeSeriesRenderer(mHeartRateRenderer);
					mMultipleSeriesRenderer.addSeriesRenderer(mHeartRateRenderer);
				} else {
					mMultipleSeriesDataset.removeSeries(mHeartRateSeries);
					mMultipleSeriesRenderer.removeSeriesRenderer(mHeartRateRenderer);
					mHeartRateValue.setText(R.string.ellipses);
				}
				break;
			case R.id.chkStepsLabel:
				if(isChecked) {
					mStepsValues = createValues(TYPE_STEPS);
					mStepsSeries = createSeries(mStepsValues, getString(R.string.steps_small));
					mMultipleSeriesDataset.removeSeries(mStepsSeries);
					mMultipleSeriesDataset.addSeries(mStepsSeries);
					mMultipleSeriesRenderer.removeSeriesRenderer(mStepsRenderer);
					mMultipleSeriesRenderer.addSeriesRenderer(mStepsRenderer);
				} else {
					mMultipleSeriesDataset.removeSeries(mStepsSeries);
					mMultipleSeriesRenderer.removeSeriesRenderer(mStepsRenderer);
					mStepsValue.setText(R.string.ellipses);
				}
				break;
			case R.id.chkCaloriesLabel:
				if(isChecked) {
					mCaloriesValues = createValues(TYPE_CALORIES);
					mCaloriesSeries = createSeries(mCaloriesValues, getString(R.string.calories_small));
					mMultipleSeriesDataset.removeSeries(mCaloriesSeries);
					mMultipleSeriesDataset.addSeries(mCaloriesSeries);
					mMultipleSeriesRenderer.removeSeriesRenderer(mCaloriesRenderer);
					mMultipleSeriesRenderer.addSeriesRenderer(mCaloriesRenderer);
				} else {
					mMultipleSeriesDataset.removeSeries(mCaloriesSeries);
					mMultipleSeriesRenderer.removeSeriesRenderer(mCaloriesRenderer);
					mCaloriesValue.setText(R.string.ellipses);
				}
				break;
			case R.id.chkDistanceLabel:
				if(isChecked) {
					mDistanceValues = createValues(TYPE_DISTANCE);
					mDistanceSeries = createSeries(mDistanceValues, getString(R.string.distance_small));
					mMultipleSeriesDataset.removeSeries(mDistanceSeries);
					mMultipleSeriesDataset.addSeries(mDistanceSeries);
					mMultipleSeriesRenderer.removeSeriesRenderer(mDistanceRenderer);
					mMultipleSeriesRenderer.addSeriesRenderer(mDistanceRenderer);
				} else {
					mMultipleSeriesDataset.removeSeries(mDistanceSeries);
					mMultipleSeriesRenderer.removeSeriesRenderer(mDistanceRenderer);
					mDistanceValue.setText(R.string.ellipses);
				}
				break;
			}
			
			//int graphWidth = (int) dpToPx(sizeWithCalendarMode(mCalendarMode));
			//resizeGraphContainer(graphWidth);
			
			mMultipleSeriesRenderer.setBarWidth(widthOfBar());
			
			if(mGraphView != null && mGraphView.isChartDrawn())
				mGraphView.repaint();
		}
	};
	
	private final GraphScrollView.GraphScrollViewListener mGraphScrollViewListener = new GraphScrollView.GraphScrollViewListener() {
		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {
			
			int multiplier = 0;
			
			if(mHeartRateLabel.isChecked())
				multiplier++;
			if(mStepsLabel.isChecked())
				multiplier++;
			if(mCaloriesLabel.isChecked())
				multiplier++;
			if(mDistanceLabel.isChecked())
				multiplier++;
			
			if(mHeartRateLabel.isChecked() && mHeartRateSeries != null) {
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
			
			if(mStepsLabel.isChecked() && mStepsSeries != null) {
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
			
			if(mCaloriesLabel.isChecked() && mCaloriesSeries != null) {
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
			
			if(mDistanceLabel.isChecked() && mDistanceSeries != null) {
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

						try {
							if (mDistanceValues != null) {
								double distanceValue;
								if (mDistanceValues.size() >= index) {
									distanceValue = mDistanceValues.get(index);
								}
								else {
									distanceValue  = mDistanceValues.get(mDistanceValues.size()-1);
								}
								if (getLifeTrakApplication().getUserProfile().getUnitSystem() == UNIT_IMPERIAL)
									distanceValue = distanceValue * MILE;

								mDistanceValue.setText(mDecimalFormat.format(distanceValue));
							}
						}
						catch (Exception e){
							LifeTrakLogger.info("Error distance : "+ e.getLocalizedMessage());
							mDistanceValue.setText("0.00");
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
	
	public void setCalendarMode(int calendarMode) {
		if(!isAdded())
			return;
		
		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			mToDateGroup.setOnCheckedChangeListener(null);
			switch(calendarMode) {
			case MODE_DAY:
				mToDateGroup.check(R.id.radDay);
				break;
			case MODE_WEEK:
				mToDateGroup.check(R.id.radWeek);
				break;
			case MODE_MONTH:
				mToDateGroup.check(R.id.radMonth);
				break;
			case MODE_YEAR:
				mToDateGroup.check(R.id.radYear);
				break;
			}
			mToDateGroup.setOnCheckedChangeListener(mCheckedChangeListener);
		} else {
			mCalendarModeSpinner.setOnItemSelectedListener(null);
			switch(calendarMode) {
			case MODE_DAY:
				mCalendarModeSpinner.setSelection(0, false);
				break;
			case MODE_WEEK:
				mCalendarModeSpinner.setSelection(1, false);
				break;
			case MODE_MONTH:
				mCalendarModeSpinner.setSelection(2, false);
				break;
			case MODE_YEAR:
				mCalendarModeSpinner.setSelection(3, false);
				break;
			}
			mCalendarModeSpinner.setOnItemSelectedListener(mItemSelectedListener);
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
		} else {
			switch(mCalendarModeSpinner.getSelectedItemPosition()) {
			case 1:
				return MODE_WEEK;
			case 2:
				return MODE_MONTH;
			case 3:
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
	
	private int colorForPercent(double percent) {
		if(percent * 100 >= 75) {
			return getResources().getColor(R.color.color_100_percent);
		} else if(percent * 100 >= 50) {
			return getResources().getColor(R.color.color_75_percent);
		} else if(percent * 100 >= 25) {
			return getResources().getColor(R.color.color_50_percent);
		} else if(percent * 100 > 0) {
			return getResources().getColor(R.color.color_25_percent);
		}
		return getResources().getColor(R.color.color_gray);
	}

	public FrameLayout getmFitnessResultsPlotContainer() {
		return mFitnessResultsPlotContainer;
	}

	public FrameLayout getmFitnessResultsLoadingText() {
		return mFitnessResultsLoadingText;
	}

	public RelativeLayout getmFitnessResultsTopData() {
		return mFitnessResultsTopData;
	}

	public FrameLayout getmFitnessResultsCenterContainer() {
		return mFitnessResultsCenterContainer;
	}
	
}
