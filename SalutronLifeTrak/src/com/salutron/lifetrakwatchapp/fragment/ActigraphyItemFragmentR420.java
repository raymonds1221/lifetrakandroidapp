package com.salutron.lifetrakwatchapp.fragment;

import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.view.GraphScrollView;

import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.tools.PanListener;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@SuppressWarnings("deprecation")
public class ActigraphyItemFragmentR420 extends BaseFragment {
	private FrameLayout mActigraphyPlotContainer;
	private Spinner mCalendarModeSpinner;
	private ViewSwitcher mActigraphyStatsSwitcher;
	private TextView mActiveTimeHour;
	private TextView mActiveTimeMin;
	private TextView mTotalActiveTime;
	private TextView mTotalSedentaryTime;
	private TextView mTotalSleepTime;
	private TextView mDateLabel;
	private TextView mAwakeTimesLabel;
	private TextView mActiveTimesLabel;
	private TextView mDeepSleepTimesLabel;
	private TextView mLightSleepTimesLabel;
	private ImageView mActigraphyPlayheadImage;
	private AbsoluteLayout mActigraphyHorizontalLine;
	private TextView mActigraphyStartTime;
	private TextView mActigraphyEndTime;
	
	private BarChart mBarChart;
	private XYSeries mActiveTimeSeries;
	private XYSeries mSedentarySeries;
	private XYSeries mLightSleepSeries;
	private XYSeries mMediumSleepSeries;
	private XYSeries mDeepSleepSeries;	
	
	private final int ACTIGRAPHY_LIGHT_SLEEP = 0x01;
	private final int ACTIGRAPHY_MEDIUM_SLEEP = 0X02;
	private final int ACTIGRAPHY_DEEP_SLEEP = 0x03;
	private final int ACTIGRAPHY_ACTIVE_TIME = 0x04;
	private final int ACTIGRAPHY_SEDENTARY_TIME = 0x05;
	private int mCurrentActigraphyType = 0;
	private double mScrollPosition;

	private final List<WorkoutHeader> mWorkoutHeadersData = new ArrayList<WorkoutHeader>();
	
	private final String[] mHours = {"12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM", "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM", "8PM", "9PM", "10PM", "11PM"};
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private List<ActigraphyItemCount> mActigraphyItemCounts = new ArrayList<ActigraphyItemCount>();
	
	private List<Integer> mSleepIndexes = new ArrayList<Integer>();
	private List<Integer> mSleepPoints = new ArrayList<Integer>();
	
	private GraphScrollView mGraphScrollView;
	private View mLeftView;
	 
	private Date mDate;
	private final Handler mHandler = new Handler();
	
	private final DecimalFormat mDecimalFormat = new DecimalFormat("#00");

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_actigraphy_item, null);
		
		initializeViews(view);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(savedInstanceState != null)
			mDate = new Date(savedInstanceState.getLong(DATE));
		
		initializeObjects();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		
		switch(newConfig.orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			changeFramentView(inflater, R.layout.fragment_actigraphy_item_r420, (ViewGroup) getView());
            MainActivity activity = (MainActivity) getActivity();
            activity.setCalendarDate(getLifeTrakApplication().getCurrentDate());
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			changeFramentView(inflater, R.layout.fragment_actigraphy_item_land_r420, (ViewGroup) getView());
			break;
		}
		
		initializeViews(getView());
		setDataForDay(mDate);
		getWorkoutDataWithDay(mDate);
	}
	
	public void onSaveInstanceState(Bundle outState) {
		if(mDate != null)
			outState.putLong(DATE, mDate.getTime());
	}
	
	private void initializeViews(View view) {
		mActigraphyPlotContainer = (FrameLayout) view.findViewById(R.id.frmActigraphyPlotContainer);
		mCalendarModeSpinner = (Spinner) view.findViewById(R.id.spnCalendarMode);
		mActigraphyStatsSwitcher = (ViewSwitcher) view.findViewById(R.id.swtActigraphyStats);
		mActiveTimeHour = (TextView) view.findViewById(R.id.tvwActiveTimeHour);
		mActiveTimeMin = (TextView) view.findViewById(R.id.tvwActiveTimeMin);
		mTotalActiveTime = (TextView) view.findViewById(R.id.tvwTotalActiveTime);
		mTotalSedentaryTime = (TextView) view.findViewById(R.id.tvwTotalSedentaryTime);
		mTotalSleepTime = (TextView) view.findViewById(R.id.tvwTotalSleepTime);
		mDateLabel = (TextView) view.findViewById(R.id.tvwDateLabel);
		mAwakeTimesLabel = (TextView) view.findViewById(R.id.tvwAwakeTimesLabel);
		mActiveTimesLabel = (TextView) view.findViewById(R.id.tvwActiveTimesLabel);
		mDeepSleepTimesLabel = (TextView) view.findViewById(R.id.tvwDeepSleepTimesLabel);
		mLightSleepTimesLabel = (TextView) view.findViewById(R.id.tvwLightSleepTimesLabel);
		mActigraphyPlayheadImage = (ImageView) view.findViewById(R.id.imgActigraphyPlayhead);
		mActigraphyHorizontalLine = (AbsoluteLayout) view.findViewById(R.id.lnrActigraphyHorizontalLine);
		mGraphScrollView = (GraphScrollView) view.findViewById(R.id.gsvGraphScroll);
		mLeftView = view.findViewById(R.id.viewGraphLeftPadding);
		mActigraphyStartTime = (TextView) view.findViewById(R.id.tvwActigraphyStartTime);
		mActigraphyEndTime = (TextView) view.findViewById(R.id.tvwActigraphyEndTime);
		
		switch(orientation()) {
		case Configuration.ORIENTATION_LANDSCAPE:
			hideActionBarAndCalendar();
			
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, 
																	getResources().getStringArray(R.array.calendar_mode_spinner));
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mCalendarModeSpinner.setAdapter(adapter);
			mCalendarModeSpinner.setOnItemSelectedListener(mOnItemSelectedListener);
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			showActionBarAndCalendar();
			break;
		}

		LifeTrakLogger.configure();
	}
	
	private void initializeObjects() {
		mDecimalFormat.setRoundingMode(RoundingMode.DOWN);
		
		mActiveTimeSeries = new XYSeries(getString(R.string.active_time));
		mSedentarySeries = new XYSeries(getString(R.string.sedentary));
		mLightSleepSeries = new XYSeries(getString(R.string.light_sleep_small));
		mMediumSleepSeries = new XYSeries(getString(R.string.medium_sleep));
		mDeepSleepSeries = new XYSeries(getString(R.string.deep_sleep_small));
		
		if(getArguments() != null) {
			Date date = new Date(getArguments().getLong(DATE));
			
			if(mDate != null)
				date = mDate;
			
			setDataForDay(date);			
		}
	}
	
	public void setDataForDay(Date date) {
		if(!isAdded())
			return;
		
		mDate = date;
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		mSleepPoints.clear();
		mSleepIndexes.clear();
		mActiveTimeSeries.clear();
		mSedentarySeries.clear();
		mLightSleepSeries.clear();
		mMediumSleepSeries.clear();
		mDeepSleepSeries.clear();
		
		View leftPadding = null; 
		View rightPadding = null;
		
		if(getView() != null)
			 leftPadding = getView().findViewById(R.id.viewGraphLeftPadding);
		
		if(getView() != null)
			rightPadding = getView().findViewById(R.id.viewGraphRightPadding);
		
		if(leftPadding != null && rightPadding != null) {
			leftPadding.setVisibility(View.GONE);
			rightPadding.setVisibility(View.GONE);
		}
		
		if(mActigraphyStatsSwitcher != null) {
			mActigraphyStatsSwitcher.setDisplayedChild(0);
			mActigraphyPlayheadImage.setVisibility(View.GONE);
			getView().findViewById(R.id.rtvHourContainer).setVisibility(View.GONE);
		}
		
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		
		String query = "select sleepPoint02, sleepPoint24, sleepPoint46, sleepPoint68, sleepPoint810 " +
								"from StatisticalDataPoint dataPoint inner join StatisticalDataHeader dataHeader " +
									"on dataPoint.dataHeaderAndPoint = dataHeader._id where watchDataHeader = ? and dateStampYear = ? " +
									"and dateStampMonth = ? and dateStampDay = ?";
		
		Cursor cursor = DataSource.getInstance(getActivity())
									.getReadOperation()
									.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
											String.valueOf(year), String.valueOf(month), String.valueOf(day));
		int recordCount = cursor.getCount();
		
		while(cursor.moveToNext()) {
			int sleepPoint02 = cursor.getInt(0);
			int sleepPoint24 = cursor.getInt(1);
			int sleepPoint46 = cursor.getInt(2);
			int sleepPoint68 = cursor.getInt(3);
			int sleepPoint810 = cursor.getInt(4);
			
			int value = sleepPoint02 + sleepPoint24 + sleepPoint46 + sleepPoint68 + sleepPoint810;
			mSleepPoints.add(value);
		}
		
		if (mSleepPoints.size() == 0) {
			for (int i=0;i<144;i++) {
				mSleepPoints.add(0);
			}
		}
		
		Calendar calYesterday = Calendar.getInstance();
		calYesterday.setTime(date);
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);
		
		int dayYesterday = calYesterday.get(Calendar.DAY_OF_MONTH);
		int monthYesterday = calYesterday.get(Calendar.MONTH) + 1;
		int yearYesterday = calYesterday.get(Calendar.YEAR) - 1900;
		
		List<SleepDatabase> sleepDatabasesYesterday = DataSource.getInstance(getActivity())
																.getReadOperation()
																.query("watchSleepDatabase = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ? and hourSleepStart > hourSleepEnd", 
																		String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
																		String.valueOf(yearYesterday), String.valueOf(monthYesterday), String.valueOf(dayYesterday))
																.getResults(SleepDatabase.class);
		
		List<SleepDatabase> sleepDatabasesNow = DataSource.getInstance(getActivity())
														.getReadOperation()
														.query("watchSleepDatabase = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ? and hourSleepEnd > hourSleepStart", 
																String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
																String.valueOf(year), String.valueOf(month), String.valueOf(day))
														.getResults(SleepDatabase.class);
		
		for(SleepDatabase sleepDatabase : sleepDatabasesYesterday) {
			float sleepStart = (sleepDatabase.getHourSleepStart() * 60 + sleepDatabase.getMinuteSleepStart()) / 10.0f;
			float sleepEnd = (sleepDatabase.getHourSleepEnd() * 60 + sleepDatabase.getMinuteSleepEnd()) / 10;
			//float sleepEnd = sleepDatabase.getAdjustedEndMinutes() / 10;
			
			if(sleepEnd < sleepStart) {
				for(int i=0;i<sleepEnd;i++) {
					mSleepIndexes.add(i);
				}
			}
		}
		
		for(SleepDatabase sleepDatabase : sleepDatabasesNow) {
			float sleepStart = (sleepDatabase.getHourSleepStart() * 60 + sleepDatabase.getMinuteSleepStart()) / 10.0f;
			float sleepEnd = (sleepDatabase.getHourSleepEnd() * 60 + sleepDatabase.getMinuteSleepEnd()) / 10;
			//float sleepEnd = sleepDatabase.getAdjustedEndMinutes() / 10;
			
			if(sleepEnd > sleepStart) {
				for(int i=(int)sleepStart;i < (int)sleepEnd;i++) {
					mSleepIndexes.add(i);
				}
			} else if(sleepStart > sleepEnd) {
				for(int i=(int)sleepStart;i<144;i++) {
					mSleepIndexes.add(i);
				}
			}
		}
		
		int maxValue = 0;
		int maxSleepValue = 0;
		
		for(int sleepPoint : mSleepPoints) {
			 maxValue = Math.max(maxValue, sleepPoint);
			 maxSleepValue = Math.max(maxSleepValue, (150 * 5) - sleepPoint);
		}
		
		int activeTime = 0;
		int sedentaryTime = 0;
		int sleepTime = 0;
		
		mCurrentActigraphyType = 0;
		
		for(int i=0;i<mSleepPoints.size();i++) {
			double value = mSleepPoints.get(i).doubleValue();
			
			if(mSleepIndexes.contains(i)) {
				value = (150 * 5) - value;
				
				if(value < 150) {
					value = -value;
					value = getYWithMaxValue(value, maxSleepValue, ACTIGRAPHY_MIN_RANGE_Y, ACTIGRAPHY_MAX_RANGE_Y);
					mLightSleepSeries.add(i, value);
				} else if(value > 150 && value < 300) {
					value = -value;
					value = getYWithMaxValue(value, maxSleepValue, ACTIGRAPHY_MIN_RANGE_Y, ACTIGRAPHY_MAX_RANGE_Y);
					mMediumSleepSeries.add(i, value);
				} else if(value > 300) {
					value = -value;
					value = getYWithMaxValue(value, maxSleepValue, ACTIGRAPHY_MIN_RANGE_Y, ACTIGRAPHY_MAX_RANGE_Y);
					mDeepSleepSeries.add(i, value);
				}
				sleepTime++;
			} else {
				if(value > 40 * 5) {
					value = getYWithMaxValue(value, maxValue, ACTIGRAPHY_MIN_RANGE_Y, ACTIGRAPHY_MAX_RANGE_Y);
					mActiveTimeSeries.add(i, value);
					activeTime++;
				} else {
					value = getYWithMaxValue(value, maxValue, ACTIGRAPHY_MIN_RANGE_Y, ACTIGRAPHY_MAX_RANGE_Y);
					mSedentarySeries.add(i, value);
					sedentaryTime++;
				}
			}
		}
		
		if(activeTime > 0) {
			activeTime = activeTime * 10;
			sedentaryTime = sedentaryTime * 10;
			sleepTime = sleepTime * 10;
			
			if(mActiveTimeHour != null && mActiveTimeMin != null) {
				mActiveTimeHour.setText(mDecimalFormat.format(activeTime / 60));
				mActiveTimeMin.setText(mDecimalFormat.format(activeTime % 60));
			}
			
			if(mTotalActiveTime != null && mTotalSedentaryTime != null && mTotalSleepTime != null) {
				mTotalActiveTime.setText(getString(R.string.total_active_time2, mDecimalFormat.format(activeTime / 60) + "H " + 
														mDecimalFormat.format(activeTime % 60) + "M"));
			}
			
			activeTime = 0;
		} else {
			if(mActiveTimeHour != null && mActiveTimeMin != null) {
				mActiveTimeHour.setText("0");
				mActiveTimeMin.setText("00");
			}

			if(mTotalActiveTime != null && mTotalSedentaryTime != null && mTotalSleepTime != null) {
				mTotalActiveTime.setText(getString(R.string.total_active_time2, "0H 00M"));
			}
		}
		
		if(sedentaryTime > 0) {
			if(mTotalSedentaryTime != null) {
				if (recordCount > 0) {
					mTotalSedentaryTime.setText(getString(R.string.total_sedentary_time, mDecimalFormat.format(sedentaryTime / 60) + "H " + 
							mDecimalFormat.format(sedentaryTime % 60) + "M"));
				} else {
					mTotalSedentaryTime.setText(getString(R.string.total_active_time2, "0H 00M"));
				}
			}
		} else {
			if(mTotalSedentaryTime != null && recordCount > 0) {
				mTotalSedentaryTime.setText(getString(R.string.total_sedentary_time, "0H 00M"));
			}
		}
		
		if(sleepTime > 0) {
			/*sleepTime = 0;
			
			for(SleepDatabase sleepDatabase : sleepDatabasesYesterday) {
				sleepTime += sleepDatabase.getSleepDuration();
			}
			
			for(SleepDatabase sleepDatabase : sleepDatabasesNow) {
				sleepTime += sleepDatabase.getSleepDuration();
			}*/
			
			if(mTotalSleepTime != null) {
				mTotalSleepTime.setText(getString(R.string.total_sleep_time, mDecimalFormat.format(sleepTime / 60) + "H " + 
											mDecimalFormat.format(sleepTime % 60) + "M"));
			}
		} else {
			if(mTotalSleepTime != null) {
				mTotalSleepTime.setText(getString(R.string.total_sleep_time, "0H 00M"));
			}
		}
		
		dataset.addSeries(mLightSleepSeries);
		dataset.addSeries(mMediumSleepSeries);
		dataset.addSeries(mDeepSleepSeries);
		dataset.addSeries(mActiveTimeSeries);
		dataset.addSeries(mSedentarySeries);
		
		mBarChart = new BarChart(dataset, generateSeriesRenderer(), BarChart.Type.STACKED);
		GraphicalView graphView = new GraphicalView(getActivity(), mBarChart);
		
		mActigraphyPlotContainer.removeAllViews();
		resizeGraphContainer((int)dpToPx(1600));
		
		if(mActigraphyHorizontalLine != null && orientation() == Configuration.ORIENTATION_LANDSCAPE) {
			mActigraphyHorizontalLine.setVisibility(View.VISIBLE);
			initializeHorizontalLine();
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int)dpToPx(1800), FrameLayout.LayoutParams.MATCH_PARENT);
			mActigraphyPlotContainer.setLayoutParams(params);
		} else {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int)dpToPx(200));
			mActigraphyPlotContainer.setLayoutParams(params);
		}
		
		mActigraphyPlotContainer.addView(graphView);
	}
	
	public void setDataWithRange(Date from, Date to, int calendarMode) {
		mCurrentActigraphyType = 0;
		
		mActigraphyHorizontalLine.setVisibility(View.GONE);
		
		Calendar calendarFrom = Calendar.getInstance();
		Calendar calendarTo = Calendar.getInstance();
		
		View leftPadding = getView().findViewById(R.id.viewGraphLeftPadding); 
		View rightPadding = getView().findViewById(R.id.viewGraphRightPadding);
		
		if(leftPadding != null && rightPadding != null) {
			leftPadding.setVisibility(View.VISIBLE);
			rightPadding.setVisibility(View.VISIBLE);
		}
		
		calendarFrom.setTime(from);
		calendarTo.setTime(to);
		
		if(mActigraphyStatsSwitcher != null) {
			mActigraphyStatsSwitcher.setDisplayedChild(1);
			mActigraphyPlayheadImage.setVisibility(View.VISIBLE);
			getView().findViewById(R.id.rtvHourContainer).setVisibility(View.VISIBLE);
		}
		
		int index = 0;
		
		XYMultipleSeriesRenderer multipleRenderer = new XYMultipleSeriesRenderer();
		XYMultipleSeriesDataset multipleDataset = new XYMultipleSeriesDataset();
		
		multipleRenderer.setXLabels(0);
		multipleRenderer.setYLabels(0);
		multipleRenderer.setShowGridY(false);
		
		List<ActigraphyTypeHolder> actigraphyTypes = new ArrayList<ActigraphyTypeHolder>();
		
		mActigraphyItemCounts.clear();
		
		while(calendarFrom.getTime().before(calendarTo.getTime()) || calendarFrom.getTime().equals(calendarTo.getTime())) {
			int day = calendarFrom.get(Calendar.DAY_OF_MONTH);
			int month = calendarFrom.get(Calendar.MONTH) + 1;
			int year = calendarFrom.get(Calendar.YEAR) - 1900;
			
			if(mCalendarModeSpinner.getSelectedItemPosition() == 3) {
				multipleRenderer.addXTextLabel(index, String.valueOf(index + 1));
			} else {
				multipleRenderer.addXTextLabel(index, String.valueOf(day));
			}
			
			String query = "select sleepPoint02, sleepPoint24, sleepPoint46, sleepPoint68, sleepPoint810 " +
									"from StatisticalDataPoint dataPoint inner join StatisticalDataHeader dataHeader " +
										"on dataPoint.dataHeaderAndPoint = dataHeader._id where watchDataHeader = ? and dateStampYear = ? " +
										"and dateStampMonth = ? and dateStampDay = ?";
			
			Cursor cursor = DataSource.getInstance(getActivity())
										.getReadOperation()
										.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
												String.valueOf(year), String.valueOf(month), String.valueOf(day));
			
			int count = cursor.getCount();
			
			List<SleepDatabase> sleepDatabasesNow = DataSource.getInstance(getActivity())
					.getReadOperation()
					.query("watchSleepDatabase = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ?", 
							String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
							String.valueOf(year), String.valueOf(month), String.valueOf(day))
					.getResults(SleepDatabase.class);

			List<Integer> sleepIndexes = new ArrayList<Integer>();
			
			for(SleepDatabase sleepDatabase : sleepDatabasesNow) {
				float sleepStart = (sleepDatabase.getHourSleepStart() * 60 + sleepDatabase.getMinuteSleepStart()) / 10.0f;
				float sleepEnd = (sleepDatabase.getHourSleepEnd() * 60 + sleepDatabase.getMinuteSleepEnd()) / 10;
				//float sleepEnd = sleepDatabase.getAdjustedEndMinutes() / 10;
				
				if(sleepEnd > sleepStart) {
					for(int i=(int)sleepStart;i < (int)sleepEnd;i++) {
						sleepIndexes.add(i);
					}
				} else if(sleepStart > sleepEnd) {
					for(int i=(int)sleepStart;i<144;i++) {
						sleepIndexes.add(i);
					}
				}
			}
			
			int rowIndex = 0;
			int awakeCount = 0;
			int activeCount = 0;
			int deepSleepCount = 0;
			int mediumSleepCount = 0;
			int lightSleepCount = 0;
			
			while(cursor.moveToNext()) {
				int sleepPoint02 = cursor.getInt(0);
				int sleepPoint24 = cursor.getInt(1);
				int sleepPoint46 = cursor.getInt(2);
				int sleepPoint68 = cursor.getInt(3);
				int sleepPoint810 = cursor.getInt(4);
				
				int value = sleepPoint02 + sleepPoint24 + sleepPoint46 + sleepPoint68 + sleepPoint810;
				double y = getYWithMaxValue(rowIndex, 144, 0, 10);
				
				if(sleepIndexes.contains(rowIndex)) {
					value = (150 * 5) - value;
					
					if(value < 150) {
						if(mCurrentActigraphyType != ACTIGRAPHY_LIGHT_SLEEP) {
							actigraphyTypes.add(createActigrpahyType(mCurrentActigraphyType, index, y));
							mCurrentActigraphyType = ACTIGRAPHY_LIGHT_SLEEP;
							lightSleepCount++;
						}
					} else if(value > 150 && value < 300) {
						if(mCurrentActigraphyType != ACTIGRAPHY_MEDIUM_SLEEP) {
							actigraphyTypes.add(createActigrpahyType(mCurrentActigraphyType, index, y));
							mCurrentActigraphyType = ACTIGRAPHY_MEDIUM_SLEEP;
							mediumSleepCount++;
						}
					} else {
						if(mCurrentActigraphyType != ACTIGRAPHY_DEEP_SLEEP) {
							actigraphyTypes.add(createActigrpahyType(mCurrentActigraphyType, index, y));
							mCurrentActigraphyType = ACTIGRAPHY_DEEP_SLEEP;
							deepSleepCount++;
						}
					}
				} else {
					if(value > 40 * 5) {
						if(mCurrentActigraphyType != ACTIGRAPHY_ACTIVE_TIME) {
							actigraphyTypes.add(createActigrpahyType(mCurrentActigraphyType, index, y));
							mCurrentActigraphyType = ACTIGRAPHY_ACTIVE_TIME;
							activeCount++;
						}
					} else {
						if(mCurrentActigraphyType != ACTIGRAPHY_SEDENTARY_TIME) {
							actigraphyTypes.add(createActigrpahyType(mCurrentActigraphyType, index, y));
							mCurrentActigraphyType = ACTIGRAPHY_SEDENTARY_TIME;
							awakeCount++;
						}
					}
				}
				
				rowIndex++;
				
				if(rowIndex == count) {
					actigraphyTypes.add(createActigrpahyType(mCurrentActigraphyType, index, y));
				}
			}
			
			ActigraphyItemCount actigraphyItemCount = new ActigraphyItemCount();
			actigraphyItemCount.awakeCount = awakeCount;
			actigraphyItemCount.activeCount = activeCount;
			actigraphyItemCount.deepSleepCount = deepSleepCount;
			actigraphyItemCount.mediumSleepCount = mediumSleepCount;
			actigraphyItemCount.lightSleepCount = lightSleepCount;
			
			mActigraphyItemCounts.add(actigraphyItemCount);
			
			calendarFrom.add(Calendar.DAY_OF_MONTH, 1);
			index++;
		}
		
		double firstX = 999;
		
		for(int i=actigraphyTypes.size()-1;i>0;i--) {
			ActigraphyTypeHolder actigraphyType = actigraphyTypes.get(i);
			createSeriesWithXY(actigraphyType.x, actigraphyType.y, actigraphyType.actigrpahyType, multipleDataset, multipleRenderer);

			if(actigraphyType.x < firstX && actigraphyType.y > 0) {
				firstX = actigraphyType.x;
				LifeTrakLogger.info("firstX = " + firstX);
			}
		}
		
		if(actigraphyTypes.size() > 0) {
			int width = mActigraphyPlotContainer.getMeasuredWidth() / index;
			mScrollPosition = width * firstX;
		}
		multipleRenderer.setZoomEnabled(false, false);
		multipleRenderer.setShowLegend(false);
		multipleRenderer.setShowAxes(false);
		multipleRenderer.setMargins(new int[] {0, 0, 0, 0});
		multipleRenderer.setMarginsColor(getResources().getColor(R.color.color_xaxis_bar));
		multipleRenderer.setYAxisMin(0);
		multipleRenderer.setYAxisMax(10);
		multipleRenderer.setXAxisMin(-1);
		multipleRenderer.setLabelsTextSize(dpToPx(12));
		multipleRenderer.setXLabelsColor(Color.BLACK);
		
		Calendar calendar = Calendar.getInstance();
		int maxDays = 0;
		
		switch(mCalendarModeSpinner.getSelectedItemPosition()) {
		case 1:
			multipleRenderer.setXAxisMax(7);
			mDateFormat.applyPattern("MMMM dd,yyyy");
			mDateLabel.setText(mDateFormat.format(from) + " - " + mDateFormat.format(to));
			break;
		case 2:
			calendar.setTime(from);
			maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			multipleRenderer.setXAxisMax(maxDays);
			mDateFormat.applyPattern("MMMM yyyy");
			mDateLabel.setText(mDateFormat.format(from));
			break;
		case 3:
			calendar.setTime(from);
			maxDays = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
			multipleRenderer.setXAxisMax(maxDays);
			mDateLabel.setText(String.valueOf(getLifeTrakApplication().getCurrentYear()));
			break;
		}
		
		multipleRenderer.setBarWidth(50);
		multipleRenderer.setPanEnabled(false, false);
		
		mBarChart = new BarChart(multipleDataset, multipleRenderer, BarChart.Type.STACKED);
		GraphicalView graphView = new GraphicalView(getActivity(), mBarChart);
		
		mActigraphyPlotContainer.removeAllViews();
		
		switch(calendarMode) {
		case MODE_WEEK:
			resizeGraphContainer((int)dpToPx(1000));
			if(actigraphyTypes.size() > 0) {
				int width = (int)dpToPx(1000) / index;
				mScrollPosition = width * (firstX + 1);
				
			}
			break;
		case MODE_MONTH:
			resizeGraphContainer((int)dpToPx(2500));
			if(actigraphyTypes.size() > 0) {
				int width = (int)dpToPx(2500) / index;
				mScrollPosition = width * (firstX + 1);
				
			}
			break;
		case MODE_YEAR:
			resizeGraphContainer((int)dpToPx(10000));
			if(actigraphyTypes.size() > 0) {
				int width = (int)dpToPx(10000) / index;
				mScrollPosition = width * (firstX + 6);
				
			}
			break;
		}

		
		if(orientation() == Configuration.ORIENTATION_LANDSCAPE) {
			mAwakeTimesLabel.setText(getString(R.string.number_of_times, 0));
			mActiveTimesLabel.setText(getString(R.string.number_of_times, 0));
			mDeepSleepTimesLabel.setText(getString(R.string.number_of_times, 0));
			mLightSleepTimesLabel.setText(getString(R.string.number_of_times, 0));
			mGraphScrollView.setGraphScrollViewListener(mGraphScrollViewListener);
			
			mHandler.postDelayed(new Runnable() {
				public void run() {
					scrollToXValue(mScrollPosition);
				}
			}, 3000);
		}
		
		mActigraphyPlotContainer.addView(graphView);
	}
	
	public void setDataWithYear(int year) {
		Calendar calendarFrom = Calendar.getInstance();
		Calendar calendarTo = Calendar.getInstance();
		
		calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
		calendarFrom.set(Calendar.MONTH, 0);
		calendarFrom.set(Calendar.YEAR, year);
		
		calendarTo.set(Calendar.DAY_OF_MONTH, 30);
		calendarTo.set(Calendar.MONTH, 11);
		calendarTo.set(Calendar.YEAR, year);
		
		setDataWithRange(calendarFrom.getTime(), calendarTo.getTime(), MODE_YEAR);
	}
	
	private XYMultipleSeriesRenderer generateSeriesRenderer() {
		XYMultipleSeriesRenderer multipleRenderer = new XYMultipleSeriesRenderer();
		
		XYSeriesRenderer lightSleepRenderer = new XYSeriesRenderer();
		XYSeriesRenderer mediumSleepRenderer = new XYSeriesRenderer();
		XYSeriesRenderer deepSleepRenderer = new XYSeriesRenderer();
		XYSeriesRenderer activeTimeRenderer = new XYSeriesRenderer();
		XYSeriesRenderer sedentaryRenderer = new XYSeriesRenderer();
		
		lightSleepRenderer.setColor(getResources().getColor(R.color.color_light_sleep));
		mediumSleepRenderer.setColor(getResources().getColor(R.color.color_medium_sleep));
		deepSleepRenderer.setColor(getResources().getColor(R.color.color_deep_sleep));
		activeTimeRenderer.setColor(getResources().getColor(R.color.color_active_time));
		sedentaryRenderer.setColor(getResources().getColor(R.color.color_sedentary));
		
		multipleRenderer.addSeriesRenderer(lightSleepRenderer);
		multipleRenderer.addSeriesRenderer(mediumSleepRenderer);
		multipleRenderer.addSeriesRenderer(deepSleepRenderer);
		multipleRenderer.addSeriesRenderer(activeTimeRenderer);
		multipleRenderer.addSeriesRenderer(sedentaryRenderer);
		
		multipleRenderer.setZoomEnabled(false, false);
		multipleRenderer.setMarginsColor(getResources().getColor(R.color.color_xaxis_bar));
		multipleRenderer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		multipleRenderer.setXAxisMin(ACTIGRAPHY_MIN_X);
		multipleRenderer.setXAxisMax(ACTIGRAPHY_MAX_X);
		multipleRenderer.setYAxisMin(ACTIGRAPHY_MIN_Y);
		multipleRenderer.setYAxisMax(ACTIGRAPHY_MAX_Y);
		multipleRenderer.setShowLegend(false);
		multipleRenderer.setPanEnabled(false, false);
		multipleRenderer.setLabelsTextSize(dpToPx(12));
		multipleRenderer.setXLabelsColor(Color.BLACK);
		multipleRenderer.setXLabels(0);
		multipleRenderer.setYLabels(0);
		multipleRenderer.setShowGridY(false);
		multipleRenderer.setInScroll(true);
		multipleRenderer.setShowAxes(false);
		
		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			multipleRenderer.setXAxisMin(0);
			multipleRenderer.setXAxisMax(144);
			multipleRenderer.setShowLabels(false);
			multipleRenderer.setBarWidth(dpToPx(2.5f));
			multipleRenderer.setMargins(new int[] {0, (int)dpToPx(5), 0, (int)dpToPx(5)});
			
			switch(getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				mActigraphyStartTime.setText("12:00AM");
				mActigraphyEndTime.setText("11:59PM");
				break;
			case TIME_FORMAT_24_HR:
				mActigraphyStartTime.setText("00:00");
				mActigraphyEndTime.setText("23:59");
				break;
			}
			
		} else {
			multipleRenderer.setXAxisMin(0);
			multipleRenderer.setXAxisMax(144);
			multipleRenderer.setShowLabels(true);
			multipleRenderer.setBarWidth(dpToPx(12.5f));
			multipleRenderer.setMargins(new int[] {0, 0, 0, 0});
			
			int index = 0;
			
			for(int i=0;i<144;i++) {
				if(i % 6 == 0) {
					switch(getLifeTrakApplication().getTimeDate().getHourFormat()) {
					case TIME_FORMAT_12_HR:
						multipleRenderer.addXTextLabel(i, mHours[index]);
						break;
					case TIME_FORMAT_24_HR:
						multipleRenderer.addXTextLabel(i, String.format("%02d:00", index));
						break;
					}
					index++;
				}
			}
		}
		
		return multipleRenderer;
	}
	
	private void initializeHorizontalLine() {
		float barWidth = mBarChart.getRenderer().getBarWidth();
		
		mActigraphyHorizontalLine.removeAllViews();
		
		int startIndex = 0;
		
		boolean hasStartIndex = false;
		
		for(int i=0;i<mSleepPoints.size();i++) {
			int value = mSleepPoints.get(i);
			
			if(mSleepIndexes.contains(i)) {
				value = (150 * 5) - value;
				
				if(value < 150) {
					if(i == mSleepPoints.size() - 1) {
						View view = viewWithStartX(startIndex, i + 1, barWidth, ACTIGRAPHY_LIGHT_SLEEP);
						mActigraphyHorizontalLine.addView(view);
						startIndex = i;
						return;
					}
					
					if(mCurrentActigraphyType != ACTIGRAPHY_LIGHT_SLEEP) {
						if(hasStartIndex) {
							View view = viewWithStartX(startIndex, i, barWidth, mCurrentActigraphyType);
							mActigraphyHorizontalLine.addView(view);
							startIndex = i;
						} else {
							startIndex = i;
							hasStartIndex = true;
						}
						mCurrentActigraphyType = ACTIGRAPHY_LIGHT_SLEEP;
					}
				} else if(value > 150 && value < 300) {
					if(i == mSleepPoints.size() - 1) {
						View view = viewWithStartX(startIndex, i + 1, barWidth, ACTIGRAPHY_MEDIUM_SLEEP);
						mActigraphyHorizontalLine.addView(view);
						startIndex = i;
						return;
					}
					
					if(mCurrentActigraphyType != ACTIGRAPHY_MEDIUM_SLEEP) {
						if(hasStartIndex) {
							View view = viewWithStartX(startIndex, i, barWidth, mCurrentActigraphyType);
							mActigraphyHorizontalLine.addView(view);
							startIndex = i;
						} else {
							startIndex = i;
							hasStartIndex = true;
						}
						mCurrentActigraphyType = ACTIGRAPHY_MEDIUM_SLEEP;
					}
				} else if(value > 300) {
					if(i == mSleepPoints.size() - 1) {
						View view = viewWithStartX(startIndex, i + 1, barWidth, ACTIGRAPHY_DEEP_SLEEP);
						mActigraphyHorizontalLine.addView(view);
						startIndex = i;
						return;
					}
					
					if(mCurrentActigraphyType != ACTIGRAPHY_DEEP_SLEEP) {
						if(hasStartIndex) {
							View view = viewWithStartX(startIndex, i, barWidth, mCurrentActigraphyType);
							mActigraphyHorizontalLine.addView(view);
							startIndex = i;
						} else {
							startIndex = i;
							hasStartIndex = true;
						}
						mCurrentActigraphyType = ACTIGRAPHY_DEEP_SLEEP;
					}
				}
			} else {
				if(value > 40 * 5) {
					if(i == mSleepPoints.size() - 1) {
						View view = viewWithStartX(startIndex, i + 1, barWidth, ACTIGRAPHY_ACTIVE_TIME);
						mActigraphyHorizontalLine.addView(view);
						startIndex = i;
						return;
					}
					
					if(mCurrentActigraphyType != ACTIGRAPHY_ACTIVE_TIME) {
						if(hasStartIndex) {
							View view = viewWithStartX(startIndex, i, barWidth, mCurrentActigraphyType);
							mActigraphyHorizontalLine.addView(view);
							startIndex = i;
						} else {
							startIndex = i;
							hasStartIndex = true;
						}
						mCurrentActigraphyType = ACTIGRAPHY_ACTIVE_TIME;
					}
				} else {
					if(i == mSleepPoints.size() - 1) {
						View view = viewWithStartX(startIndex, i + 1, barWidth, ACTIGRAPHY_SEDENTARY_TIME);
						mActigraphyHorizontalLine.addView(view);
						startIndex = i;
						return;
					}
					
					if(mCurrentActigraphyType != ACTIGRAPHY_SEDENTARY_TIME) {
						if(hasStartIndex) {
							View view = viewWithStartX(startIndex, i, barWidth, mCurrentActigraphyType);
							mActigraphyHorizontalLine.addView(view);
							startIndex = i;
						} else {
							startIndex = i;
							hasStartIndex = true;
						}
						mCurrentActigraphyType = ACTIGRAPHY_SEDENTARY_TIME;
					}
				}
			}
		}
	}
	
	private View viewWithStartX(int startX, int endX, float barWidth, int actigraphyType) {
		int x = (int) (barWidth * ((float)startX) - dpToPx(5.5f));
		float width = (barWidth * (float)endX) - (barWidth * (float)startX);
		
		AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams((int)width, 
													AbsoluteLayout.LayoutParams.MATCH_PARENT, x, 0);
		FrameLayout view = new FrameLayout(getActivity());
		
		if(actigraphyType == ACTIGRAPHY_DEEP_SLEEP) {
			ImageView imageView = new ImageView(getActivity());
			imageView.setImageResource(R.drawable.ll_fitnessres_landscape_actigraphy_icon_sleep);
			FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, 
																FrameLayout.LayoutParams.WRAP_CONTENT);
			frameParams.gravity = Gravity.RIGHT;
			imageView.setLayoutParams(frameParams);
			view.addView(imageView);
		}
		
		view.setLayoutParams(params);
		view.setBackgroundColor(colorWithActigrpahyType(actigraphyType));
		
		return view;
	}
	
	private int colorWithActigrpahyType(int actigraphyType) {
		switch(actigraphyType) {
		case ACTIGRAPHY_LIGHT_SLEEP:
			return getResources().getColor(R.color.color_light_sleep);
		case ACTIGRAPHY_MEDIUM_SLEEP:
			return getResources().getColor(R.color.color_medium_sleep);
		case ACTIGRAPHY_DEEP_SLEEP:
			return getResources().getColor(R.color.color_deep_sleep);
		case ACTIGRAPHY_ACTIVE_TIME:
			return getResources().getColor(R.color.color_active_time);
		}
		
		return getResources().getColor(R.color.color_sedentary);
	}
	
	private void createSeriesWithXY(double x, double y, int actigraphyType, 
										XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
		XYSeries xySeries = new XYSeries(getString(R.string.metric) + x);
		XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
		
		xySeries.add(x, y);
		
		switch(actigraphyType) {
		case ACTIGRAPHY_LIGHT_SLEEP:
			seriesRenderer.setColor(getResources().getColor(R.color.color_light_sleep));
			break;
		case ACTIGRAPHY_MEDIUM_SLEEP:
			seriesRenderer.setColor(getResources().getColor(R.color.color_medium_sleep));
			break;
		case ACTIGRAPHY_DEEP_SLEEP:
			seriesRenderer.setColor(getResources().getColor(R.color.color_deep_sleep));
			break;
		case ACTIGRAPHY_ACTIVE_TIME:
			seriesRenderer.setColor(getResources().getColor(R.color.color_active_time));
			break;
		case ACTIGRAPHY_SEDENTARY_TIME:
			seriesRenderer.setColor(getResources().getColor(R.color.color_sedentary));
			break;
		}
		
		dataset.addSeries(xySeries);
		renderer.addSeriesRenderer(seriesRenderer);
	}
	
	private ActigraphyTypeHolder createActigrpahyType(int actigraphyType, double x, double y) {
		ActigraphyTypeHolder actigraphyTypeHolder = new ActigraphyTypeHolder();
		
		actigraphyTypeHolder.actigrpahyType = actigraphyType;
		actigraphyTypeHolder.x = x;
		actigraphyTypeHolder.y = y;
		
		return actigraphyTypeHolder;
	}
	
	private final AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			MainActivity mainActivity = (MainActivity) getActivity();
			
			switch(position) {
			case 0:
				mainActivity.setCalendarMode(MODE_DAY);
				break;
			case 1:
				mainActivity.setCalendarMode(MODE_WEEK);
				break;
			case 2:
				mainActivity.setCalendarMode(MODE_MONTH);
				break;
			case 3:
				mainActivity.setCalendarMode(MODE_YEAR);
				break;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) { }
	};
	
	private class ActigraphyTypeHolder {
		public int actigrpahyType;
		public double x;
		public double y;
	}
	
	private class ActigraphyItemCount {
		public int awakeCount;
		public int activeCount;
		public int deepSleepCount;
		public int mediumSleepCount;
		public int lightSleepCount;
	}
	
	private final PanListener mPanListener = new PanListener() {
		@Override
		public void panApplied() {
			if(orientation() == Configuration.ORIENTATION_LANDSCAPE && mBarChart != null) {
				int playHeadX = mActigraphyPlayheadImage.getLeft() + (mActigraphyPlayheadImage.getMeasuredWidth() / 2) - getView().findViewById(R.id.rtvHourContainer).getMeasuredWidth();
				double[] realPoint = mBarChart.toRealPoint(playHeadX, 0);
				LifeTrakLogger.info("realpoint x: " + realPoint[0] + " y: " + realPoint[1]);
				
				int x = (int) Math.round(realPoint[0]);
				
				if(x >= 0 && x < mActigraphyItemCounts.size()) {
					ActigraphyItemCount actigraphyItemCount = mActigraphyItemCounts.get(x);
					
					mAwakeTimesLabel.setText(getString(R.string.number_of_times, actigraphyItemCount.awakeCount));
					mActiveTimesLabel.setText(getString(R.string.number_of_times, actigraphyItemCount.activeCount));
					mDeepSleepTimesLabel.setText(getString(R.string.number_of_times, actigraphyItemCount.deepSleepCount));
					mLightSleepTimesLabel.setText(getString(R.string.number_of_times, actigraphyItemCount.lightSleepCount));
				}
			}
		}
	};
	
	public void setDate(Date date) {
		mDate = date;
	}
	
	private final GraphScrollView.GraphScrollViewListener mGraphScrollViewListener = new GraphScrollView.GraphScrollViewListener() {
		
		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {
			try {
				View leftView = getView().findViewById(R.id.viewGraphLeftPadding);
				
				for(int i=0;i<mActigraphyItemCounts.size();i++) {
					double[] screenPoint = mBarChart.toScreenPoint(new double[] {i, 0});
					int halfBarWidth =  (int)(mBarChart.getRenderer().getBarWidth() / 2);
					int playHeadPositionX = (mActigraphyPlayheadImage.getLeft() + (mActigraphyPlayheadImage.getMeasuredWidth() / 2));
					int currentPointX = ((int)(leftView.getMeasuredWidth() + screenPoint[0])) - l;
					
					switch(mCalendarModeSpinner.getSelectedItemPosition()) {
					case 1:
						currentPointX += (int)dpToPx(55);
						break;
					case 2:
						currentPointX += (int)dpToPx(55);
						break;
					case 3:
						currentPointX += (int)dpToPx(55);
						break;
					}
					
					if(playHeadPositionX >= currentPointX - halfBarWidth && playHeadPositionX <= currentPointX + halfBarWidth) {
						double[] realPoint = mBarChart.toRealPoint((float)screenPoint[0], 0);
						LifeTrakLogger.info("real point: " + realPoint[0]);
						
						ActigraphyItemCount actigraphyItemCount = mActigraphyItemCounts.get(i);
						mAwakeTimesLabel.setText(getString(R.string.number_of_times, actigraphyItemCount.awakeCount));
						mActiveTimesLabel.setText(getString(R.string.number_of_times, actigraphyItemCount.activeCount));
						mDeepSleepTimesLabel.setText(getString(R.string.number_of_times, actigraphyItemCount.deepSleepCount));
						mLightSleepTimesLabel.setText(getString(R.string.number_of_times, actigraphyItemCount.lightSleepCount));
						
						break;
					} else {
						mAwakeTimesLabel.setText(getString(R.string.number_of_times, 0));
						mActiveTimesLabel.setText(getString(R.string.number_of_times, 0));
						mDeepSleepTimesLabel.setText(getString(R.string.number_of_times, 0));
						mLightSleepTimesLabel.setText(getString(R.string.number_of_times, 0));
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	private void scrollToXValue(double x) {
		/*double[] screenPoint = mBarChart.toScreenPoint(new double[] {x, 0});
		int scrollPositionX = mLeftView.getMeasuredWidth() + (int)screenPoint[0] - mActigraphyPlayheadImage.getRight();
		mGraphScrollView.smoothScrollTo(scrollPositionX, 0);*/

		LifeTrakLogger.info("scroll position: " + x);
		
		if (mGraphScrollView != null)
			mGraphScrollView.smoothScrollTo((int)x, 0);
	}
	
	private void resizeGraphContainer(int width) {
		if(mActigraphyPlotContainer != null && orientation() == Configuration.ORIENTATION_LANDSCAPE) {
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT);
			params.setMargins(0, (int)dpToPx(8), 0, 0);
			mActigraphyPlotContainer.setLayoutParams(params);
		}
	}


	public void getWorkoutDataWithDay(Date date) {
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

			List<WorkoutHeader> workoutHeader = DataSource.getInstance(getActivity())
					.getReadOperation()
					.query("watchWorkoutHeader == ? AND dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ?",
							String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
							String.valueOf(day), String.valueOf(month), String.valueOf(year))
					.getResults(WorkoutHeader.class);



			List<WorkoutHeader> spillOverWorkoutInfos = getSpillOverWorkoutFromPreviousDay(date);
			if (spillOverWorkoutInfos != null && spillOverWorkoutInfos.size() > 0){
				mWorkoutHeadersData.addAll(spillOverWorkoutInfos);
			}

			//add today's workout
			if(workoutHeader.size() > 0) {
				//filter spill over for next day
				workoutHeader = getWorkoutInfosWithoutNextDaySpillOver(workoutHeader);

				for (WorkoutHeader mwWorkoutHeader : workoutHeader) {

					String query = "SELECT * FROM WorkoutHeader WHERE dateStampDay = ? and dateStampMonth = ? and dateStampYear =? " +
							"and timeStampSecond = ? and timeStampMinute = ? and timeStampHour = ? and watchWorkoutHeader = ?";

					Cursor cursor = DataSource.getInstance(getActivity())
							.getReadOperation()
							.rawQuery(query, String.valueOf(mwWorkoutHeader.getDateStampDay()), String.valueOf(mwWorkoutHeader.getDateStampMonth()), String.valueOf(mwWorkoutHeader.getDateStampYear()),
									String.valueOf(mwWorkoutHeader.getTimeStampSecond()), String.valueOf(mwWorkoutHeader.getTimeStampMinute()), String.valueOf(mwWorkoutHeader.getTimeStampHour()), String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));
					if (cursor != null && cursor.getCount() > 0) {
						for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
							List<WorkoutStopInfo> workoutStopInfos = DataSource.getInstance(getActivity())
									.getReadOperation()
									.query("headerAndStop = ?", cursor.getString(cursor.getColumnIndex("_id")))
									.getResults(WorkoutStopInfo.class);
							mwWorkoutHeader.setWorkoutStopInfo(workoutStopInfos);
						}
					}


				}

				mWorkoutHeadersData.clear();
				mWorkoutHeadersData.addAll(workoutHeader);

			}
		}
		//setDataWithDay(date);
	}

	private List<WorkoutHeader> getWorkoutInfosWithoutNextDaySpillOver(List<WorkoutHeader> workoutHeaders){
		List<WorkoutHeader> workouts = workoutHeaders;
		for (WorkoutHeader workoutIhead : workoutHeaders){
			WorkoutHeader lastWorkout = workoutIhead;
			int startTime = getStartTime(lastWorkout);
			int endTime = getEndTime(lastWorkout);
			if(endTime > 86400){
				//int startPosition = startTime/600;
				//int endPosition = endTime/600;
				lastWorkout.setHour((86399-startTime)/3600);
				lastWorkout.setMinute(((86399-startTime)%3600)/60);
				lastWorkout.setSecond((86399-startTime)%60);
				//				lastWorkout.setHundredths(0);
				workouts.remove(workoutHeaders.indexOf(workoutIhead));
				workouts.add(lastWorkout);
				break;
			}
		}

		return workouts;
	}

	private List<WorkoutHeader> getSpillOverWorkoutFromPreviousDay(Date date){
		Calendar calYesterday = Calendar.getInstance();
		calYesterday.setTime(date);
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);

		int day = calYesterday.get(Calendar.DAY_OF_MONTH);
		int month = calYesterday.get(Calendar.MONTH) + 1;
		int year = calYesterday.get(Calendar.YEAR) - 1900;

		List<WorkoutHeader> workoutInfos = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchWorkoutHeader = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ?",
						String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
						String.valueOf(year), String.valueOf(month), String.valueOf(day))
				.orderBy("timeStampHour, timeStampMinute, timeStampSecond", "")
				.getResults(WorkoutHeader.class);
		List<WorkoutHeader> spillOverWorkoutInfos = new ArrayList<WorkoutHeader>();

		if (workoutInfos != null && workoutInfos.size() > 0){
			for(WorkoutHeader workoutInfo : workoutInfos){
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

	private int getStartTime(WorkoutHeader workoutheader){
		int hr = workoutheader.getTimeStampHour();
		int min = workoutheader.getTimeStampMinute();
		int sec = workoutheader.getTimeStampSecond();
		return (hr*3600) + (min*60) + sec;
	}

	private int getEndTime(WorkoutHeader workoutHeader){
		int endTime = getStartTime(workoutHeader) + workoutHeader.getHour()*3600 + workoutHeader.getMinute()*60 + workoutHeader.getSecond();

		String query = "SELECT * FROM WorkoutHeader WHERE dateStampDay = ? and dateStampMonth = ? and dateStampYear =? " +
				"and timeStampSecond = ? and timeStampMinute = ? and timeStampHour = ? and watchWorkoutHeader = ?";

		Cursor cursor = DataSource.getInstance(getActivity())
				.getReadOperation()
				.rawQuery(query, String.valueOf(workoutHeader.getDateStampDay()), String.valueOf(workoutHeader.getDateStampMonth()), String.valueOf(workoutHeader.getDateStampYear()),
						String.valueOf(workoutHeader.getTimeStampSecond()), String.valueOf(workoutHeader.getTimeStampMinute()), String.valueOf(workoutHeader.getTimeStampHour()), String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));

		if (cursor != null && cursor.getCount() > 0) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				List<WorkoutStopInfo> workoutStops = DataSource.getInstance(getActivity())
						.getReadOperation().query("headerAndStop = ?", cursor.getString(cursor.getColumnIndex("_id"))).getResults(WorkoutStopInfo.class);
				if (workoutStops.size()!= 0) {
					workoutStops = filterDuplicateWorkoutStops(workoutStops);
					for (WorkoutStopInfo workoutStop : workoutStops) {
						endTime += (workoutStop.getStopHours() * 3600) + (workoutStop.getStopMinutes() * 60) + workoutStop.getStopSeconds();
					}
				}
			}
		}
		cursor.close();


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


}
