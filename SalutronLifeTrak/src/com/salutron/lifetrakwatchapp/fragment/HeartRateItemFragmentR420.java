package com.salutron.lifetrakwatchapp.fragment;

import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.common.primitives.Ints;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.StatisticalDataPoint;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;
import com.salutron.lifetrakwatchapp.util.StringUtils;
import com.salutron.lifetrakwatchapp.view.GraphScrollView;

import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.RangeBarChart;
import org.achartengine.model.Point;
import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HeartRateItemFragmentR420 extends BaseFragment {
	private FrameLayout mHeartRatePlotContainer;
	private FrameLayout mHeartRateLoadingText;
	private RelativeLayout mHeartRateTopData;
	private RadioGroup mToDateGroup;
	private Spinner mCalendarModeSpinner;
	private TextView mAverageBPMValue;
	private TextView mIntensityTag;
	//private TextView mMaxBPM;
	private TextView tvMaxRateValue;
	private TextView tvMinRateValue;
	private TextView tvIntenseHRDuration;
	//private TextView mMinBPM;
	private ImageView mPlayhead;
	private GraphScrollView mGraphScroll;
	private FrameLayout FrameGraphBackGround;
	private LinearLayout mView;
	private TableLayout mTableLayoutMinMaxContainer;
	private ArrayList<Float> avgAverage = new ArrayList<Float>();
	private LinearLayout linearHover;
	private ArrayList<Integer> mHeartRateIndex = new ArrayList<Integer>();
	private BarChart mBarChart;
	private RangeBarChart mRangeBarChart;
	private GraphicalView mGraphView;
	private RangeCategorySeries mCategorySeries = new RangeCategorySeries("Category Series");
	private final Handler mHandler = new Handler();

	private final XYSeries mHeartRateSeries = new XYSeries("Heart Rate");
	private final List<Double> mHeartRateValues = new ArrayList<Double>();
	private final List<Double> mAverageHRs = new ArrayList<Double>();

	private final String[] mHours = {"12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM", "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM", "8PM", "9PM", "10PM", "11PM"};
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private final DecimalFormat mDecimalFormat = new DecimalFormat("#####0");
	private final DecimalFormat mDecimalFormat2 = new DecimalFormat("##0");

	private int heartMinValue = 0;
	private int heartMaxValue = 0;

	private final String[] mYlabel = {"0","20", "40", "60", "80", "100","120", "140", "160", "180", "200", "220", "240"};
	private int mPosition;
	private double mScrollPosition;
	private int mCalendarMode = MODE_DAY;

	private ArrayList<Integer> trueValuesHR = new ArrayList<Integer>();

	private int mYearScrollPosition = 0;
	private int mMonthScrollPosition = 0;
	private int mWeekScrollPosition = 0;

	private Date mDateNow;
	private Date mDateFrom;
	private Date mDateTo;
	private int mYear;

	private ProgressBar mProgressBar;
	private LinearLayout LinearValues;
	private int mYearCount = 0;
	private final List<WorkoutHeader> mWorkoutHeadersData = new ArrayList<WorkoutHeader>();
	private final String SCROLLER_RECEIVER = "com.salutron.lifetrak.SCROLLER_RECEIVER";

	private ArrayList<Integer> LandScapeHeartRateMaxBPM = new ArrayList<Integer>();
	private ArrayList<Integer> LandScapeHeartRateMinBPM = new ArrayList<Integer>();

	private ArrayList<Integer> LandScapeHeartRateIndex = new ArrayList<Integer>();
	private ArrayList<Integer> LandScapeHeartRateAVG = new ArrayList<Integer>();



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_heart_rate_item_r420, null);

		initializeViews(view);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if(savedInstanceState != null)
			mCalendarMode = MODE_DAY;

		initializeObjects();
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		LayoutInflater inflater = LayoutInflater.from(getActivity());

		switch(newConfig.orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			changeFramentView(inflater, R.layout.fragment_heart_rate_item_r420, (ViewGroup) getView());
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			changeFramentView(inflater, R.layout.fragment_heart_rate_item_land_r420, (ViewGroup) getView());
			break;
		}

		initializeViews(getView());
		mWorkoutHeadersData.clear();
		getWorkoutDataWithDay(mDateNow);
		setDataWithDate(mDateNow);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	private void initializeViews(View view) {
		mHeartRatePlotContainer = (FrameLayout) view.findViewById(R.id.frmHeartRatePlotContainer);
		mView  = (LinearLayout)view.findViewById(R.id.mainLayout);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		mToDateGroup = (RadioGroup) view.findViewById(R.id.rdgToDate);
		mAverageBPMValue = (TextView) view.findViewById(R.id.tvwAvgBpmValue);
		mIntensityTag = (TextView) view.findViewById(R.id.tvwIntentsityTag);
		mCalendarModeSpinner = (Spinner) view.findViewById(R.id.spnCalendarMode);
//		mMaxBPM = (TextView) view.findViewById(R.id.tvwMaxBPM);
//		mMinBPM = (TextView) view.findViewById(R.id.tvwMinBPM);
		mPlayhead = (ImageView) view.findViewById(R.id.imgPlayhead);
		mGraphScroll = (GraphScrollView) view.findViewById(R.id.gsvGraphScroll);
		mTableLayoutMinMaxContainer = (TableLayout) view.findViewById(R.id.tableBpmMinMaxContainer);
		FrameGraphBackGround = (FrameLayout) view.findViewById(R.id.FrameGraphBackGround);

		tvMaxRateValue = (TextView) view.findViewById(R.id.tvMaxRateValue);
		tvMinRateValue = (TextView) view.findViewById(R.id.tvMinRateValue);
		tvIntenseHRDuration = (TextView) view.findViewById(R.id.tvIntenseHRDuration);

		linearHover = (LinearLayout) view.findViewById(R.id.linear_hover);

		mHeartRatePlotContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});

		showHover();


		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			mHeartRateLoadingText = (FrameLayout) view.findViewById(R.id.frmHeartRateLoadingText);
			mHeartRateTopData = (RelativeLayout) view.findViewById(R.id.tvwAvgBpmLayout);
		}
		if (orientation() == Configuration.ORIENTATION_LANDSCAPE){
			LinearValues = (LinearLayout)getView().findViewById(R.id.linear_x_values);
		}
		if(mCalendarModeSpinner != null)
			mCalendarModeSpinner.setOnItemSelectedListener(null);
		if(mToDateGroup != null)
			mToDateGroup.setOnCheckedChangeListener(null);

		switch(orientation()) {
		case Configuration.ORIENTATION_LANDSCAPE:
			hideActionBarAndCalendar();
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			showActionBarAndCalendar();
			break;
		}

	}

	private void showHover(){
		mHeartRatePlotContainer.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						(getActivity().findViewById(R.id.linear_hover)).setVisibility(View.VISIBLE);
						(getActivity().findViewById(R.id.linear_color)).setVisibility(View.GONE);

						break;
					case MotionEvent.ACTION_UP:
						(getActivity().findViewById(R.id.linear_hover)).setVisibility(View.GONE);
						(getActivity().findViewById(R.id.linear_color)).setVisibility(View.VISIBLE);
						break;
					case MotionEvent.ACTION_MOVE:
//						(getActivity().findViewById(R.id.linear_hover)).setVisibility(View.VISIBLE);
//						(getActivity().findViewById(R.id.linear_color)).setVisibility(View.GONE);
						break;
					case MotionEvent.ACTION_CANCEL:
						(getActivity().findViewById(R.id.linear_hover)).setVisibility(View.GONE);
						(getActivity().findViewById(R.id.linear_color)).setVisibility(View.VISIBLE);
						break;
				}
				return false;
			}
		});
	}

	private void hideHover(){
		mHeartRatePlotContainer.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				}
				return false;
			}
		});
	}

	private void initializeObjects() {
		mDateFormat.applyPattern("dd MMM");
		//mDecimalFormat2.setRoundingMode(RoundingMode.DOWN);

		if(getArguments() != null) {
			mPosition = getArguments().getInt(POSITION);
			Date date = new Date(getArguments().getLong(DATE));

			Date from = getLifeTrakApplication().getDateRangeFrom();
			Date to = getLifeTrakApplication().getDateRangeTo();

			MainActivity activity = (MainActivity) getActivity();

			switch(mPosition) {
				case 0:
					date = getYesterdayForDate(getLifeTrakApplication().getCurrentDate());
					break;
				case 1:
					date = getLifeTrakApplication().getCurrentDate();
					break;
				case 2:
					date = getTomorrowForDate(getLifeTrakApplication().getCurrentDate());
					break;
			}

			setDataWithDate(date);
			getWorkoutDataWithDay(date);

			if(mPosition == 1) {
					activity.setCalendarDate(date);
					activity.setCalendarPickerMode(MODE_DAY);
			}


		}


	}

	public void setDataWithDate(Date date) {
		if(!isAdded())
			return;
		trueValuesHR.clear();
		mDateNow = date;

		mWorkoutHeadersData.clear();
		avgAverage.clear();
		getWorkoutDataWithDay(date);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		mHeartRateSeries.clear();
		mHeartRateValues.clear();

		XYMultipleSeriesDataset multipleDataset = new XYMultipleSeriesDataset();
		XYMultipleSeriesRenderer multipleRenderer = new XYMultipleSeriesRenderer();

		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setColor(getResources().getColor(R.color.color_heart_rate_bar));

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		String query = "select averageHR from StatisticalDataPoint dataPoint " +
				"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
				"where dataHeader.watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?";

		Cursor cursor = DataSource.getInstance(getActivity())
				.getReadOperation()
				.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
						String.valueOf(day), String.valueOf(month), String.valueOf(year));

		double minValue = 0;
		double maxValue = 0;

		if (cursor.getCount() > 0) {
			while(cursor.moveToNext()) {
				double value = cursor.getDouble(0);

				if(value > 0) {
					minValue = Math.min(minValue, value);
					avgAverage.add((float)value);
				}

				maxValue = Math.max(maxValue, value);
				mHeartRateValues.add(value);
			}
		} else {
			for (int i=0;i<144;i++) {
				mHeartRateValues.add(0.0);
			}
		}

		if(minValue == 0)
			minValue = maxValue;

		cursor.close();

		int hrIndex = 0;

		if (mHeartRateValues.size() < 144)
		{
			for (int i=mHeartRateValues.size() - 1;i<144;i++) {
				mHeartRateValues.add(0d);
			}
		}

		for(Double value : mHeartRateValues) {
			mHeartRateSeries.add(hrIndex, value);
			hrIndex++;
		}



//		for(int i=0;i<mHeartRateValues.size();i++) {
//			if(mHeartRateValues.get(i) > 0) {
//				mScrollPosition = i;
//				break;
//			}
//		}


		multipleRenderer.setZoomEnabled(false, false);

		multipleRenderer.setShowLegend(false);
		multipleRenderer.setShowLabels(true);
		multipleRenderer.setShowGridY(false);
		multipleRenderer.setShowCustomTextGridY(false);
		multipleRenderer.setShowAxes(false);
		multipleRenderer.addSeriesRenderer(renderer);
		multipleRenderer.setMarginsColor(getResources().getColor(R.color.color_xaxis_bar));
		multipleRenderer.setLabelsTextSize(dpToPx(12));
		multipleRenderer.setXLabelsColor(getResources().getColor(android.R.color.black));
		multipleRenderer.setYLabels(0);

		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			if (mWorkoutHeadersData.size() != 0){
				showHover();
				(getActivity().findViewById(R.id.linear_color)).setVisibility(View.VISIBLE);
//				FrameGraphBackGround.setBackgroundResource(0);
//				FrameGraphBackGround.setVisibility(View.GONE);

			//	mHeartRateSeries.clear();
				int maxBPM = 0;
				int minBPM = 0;
				float sum = 0;
				double average = 0.0;

				int intenseHR = 0;

				for (int x = 0; x < mWorkoutHeadersData.size(); x++) {
					trueValuesHR.clear();

					WorkoutHeader workoutHeader = mWorkoutHeadersData.get(x);

					if (maxBPM < Math.abs(workoutHeader.getMaximumBPM()))
						maxBPM = Math.abs(workoutHeader.getMaximumBPM());
					if (minBPM < workoutHeader.getMinimumBPM())
						minBPM = workoutHeader.getMinimumBPM();

					int hour = workoutHeader.getTimeStampHour();
					int minute = workoutHeader.getTimeStampMinute();
					int sec = workoutHeader.getTimeStampSecond();

					String heartRate = workoutHeader.getHeaderHeartRate();
					int hrLogRate = workoutHeader.getLogRateHR();
					try {
						JSONArray jsonArray = new JSONArray(heartRate);
						if (jsonArray != null) {

								for (int i = 0; i < jsonArray.length(); ++i) {
									for (int y = 0; y < hrLogRate; y ++){
										trueValuesHR.add(jsonArray.optInt(i));
										sum = sum + jsonArray.optInt(i);
									}
								}


							int[] numbers = Ints.toArray(trueValuesHR);


							int startingTime = (hour * 3600) + (minute * 60) + sec;
							int plotIndex = startingTime / 600;
							if ((startingTime % 600) != 0)
								plotIndex++;
							int startingTimeInTenMinutes = startingTime / 600;


							double sum10Min = 0;
							double average10Min = 0.0;



								mHeartRateIndex.clear();

								int valueMinus = 0;
								int overAllMinus = 0;
								for (int a = 0; a < 144; a++){
									if (a == startingTimeInTenMinutes){
										valueMinus = ((startingTimeInTenMinutes + 1) * 600) - startingTime;
										if (mHeartRateIndex.size() == 0){
											if (valueMinus > numbers.length){
												mHeartRateIndex.add(numbers.length);
												break;
											}
											else{
												mHeartRateIndex.add(valueMinus);
												overAllMinus = overAllMinus + valueMinus;
												startingTimeInTenMinutes ++;
											}

										}
										else{
											if (overAllMinus < numbers.length){
												valueMinus = valueMinus - overAllMinus;
												mHeartRateIndex.add(valueMinus);
												overAllMinus = overAllMinus + valueMinus;
												startingTimeInTenMinutes++;
											}
											else{
												overAllMinus = overAllMinus - 600;
												if (overAllMinus > 0)
													if ((numbers.length - overAllMinus) < 60){
														mHeartRateIndex.remove(mHeartRateIndex.size()- 1);
													}
												else {
														mHeartRateIndex.set((mHeartRateIndex.size() - 1), (numbers.length - overAllMinus));
													}
												else{

												}
												break;
											}

										}
									}
								}


								int valueOverAll = 0;
								int index = 0;
								for (int i = 0; i < mHeartRateIndex.size() ; i++){
									int valueEnding = mHeartRateIndex.get(i);


									for (int y = 0; y < valueEnding; y++){
										if (numbers[y + valueOverAll] > 0){
											sum10Min = sum10Min + numbers[y + valueOverAll];
											index++;
										}
									}
									valueOverAll = valueOverAll + valueEnding;
									average10Min = sum10Min / index;

									if (average10Min > 0) {
										mHeartRateSeries.remove(plotIndex);
										mHeartRateSeries.add((int) plotIndex + i, (int) average10Min);
										mHeartRateValues.set((int) plotIndex + i, average10Min);
										avgAverage.add((float)average10Min);
									}
									average10Min = 0;
									sum10Min = 0;
									index = 0;

								}


//							for (int i = 0; i < numbers.length; i++) {
//								//mHeartRateSeries.add(startingTime, numbers[i]);
//								startingTime++;
//							}



						}

					} catch (JSONException e) {
						e.printStackTrace();
					}


				}



				multipleDataset.addSeries(mHeartRateSeries);
				multipleRenderer.setYAxisMin(0);
				multipleRenderer.setYAxisMax(240);
				multipleRenderer.setPanEnabled(false);
				multipleRenderer.setMargins(new int[]{0, 30, 0, 30});
				multipleRenderer.setXAxisMin(0);
				multipleRenderer.setXAxisMax(144);
				multipleRenderer.setXLabels(0);
				multipleRenderer.setYLabels(0);
				multipleRenderer.setXLabelsAlign(Align.CENTER);
				multipleRenderer.setYLabelsAlign(Align.CENTER);
				multipleRenderer.setYLabelsColor(0, Color.BLACK);
				multipleRenderer.setShowTickMarks(false);
				//multipleRenderer.setMargins(new int[]{0, (int) dpToPx(5), 0, (int) dpToPx(5)});



				switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
					case TIME_FORMAT_12_HR:
						multipleRenderer.addXTextLabel(5, getString(R.string.am));
						multipleRenderer.addXTextLabel(140, getString(R.string.pm));
						break;
					case TIME_FORMAT_24_HR:
						multipleRenderer.addXTextLabel(5, "0:00");
						multipleRenderer.addXTextLabel(140, "23:59");
						break;
				}



				mBarChart = new BarChart(multipleDataset, multipleRenderer, BarChart.Type.STACKED);
				GraphicalView graphView = new GraphicalView(getActivity(), mBarChart);
				mHeartRatePlotContainer.removeAllViews();
				mHeartRatePlotContainer.addView(graphView);

				tvMaxRateValue.setText(Integer.toString(maxBPM));
				tvMinRateValue.setText(Integer.toString(minBPM));

				if (avgAverage.size() > 0){
					float sumTotal = 0;
					float sumTotalAVG = 0;
					int index = 0;
					for (int i = 0; i < avgAverage.size(); i++){
						if (avgAverage.get(i) > 0) {
							sumTotal = sumTotal + avgAverage.get(i);
							index++;
						}
					}
					sumTotalAVG  = sumTotal / index;
					mAverageBPMValue.setText(String.valueOf((int)sumTotalAVG));

				}
				else {
					mAverageBPMValue.setText("0");
				}
				heartMaxValue = maxBPM;
				heartMinValue = minBPM;
				if (intenseHR != 0){
					if (intenseHR > 60)
					{
						int intenseHRMinute = intenseHR / 60;
						int intenseHRSec = intenseHR % 60;
						tvIntenseHRDuration.setText(String.valueOf(intenseHRMinute) + ":" + String.format("%02d", intenseHRSec));
					}
					else
						tvIntenseHRDuration.setText("0:"+ String.format("%02d", intenseHR));
				}

			}
			else{
				//hideHover();

				multipleDataset.addSeries(mHeartRateSeries);
				multipleRenderer.setYAxisMin(0);
				multipleRenderer.setYAxisMax(240);
				multipleRenderer.setPanEnabled(false);
				multipleRenderer.setMargins(new int[]{0, 30, 0, 30});
				multipleRenderer.setXAxisMin(0);
				multipleRenderer.setXAxisMax(144);
				multipleRenderer.setXLabels(0);
				multipleRenderer.setYLabels(0);
				multipleRenderer.setXLabelsAlign(Align.CENTER);
				multipleRenderer.setYLabelsAlign(Align.CENTER);
				multipleRenderer.setYLabelsColor(0, Color.BLACK);
				multipleRenderer.setShowTickMarks(false);

				switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
					case TIME_FORMAT_12_HR:
						multipleRenderer.addXTextLabel(5, getString(R.string.am));
						multipleRenderer.addXTextLabel(140, getString(R.string.pm));
						break;
					case TIME_FORMAT_24_HR:
						multipleRenderer.addXTextLabel(5, "0:00");
						multipleRenderer.addXTextLabel(140, "23:59");
						break;
				}


				mAverageBPMValue.setText("0");

				String queryDataHeader = "select minimumBPM,maximumBPM from StatisticalDataHeader " +
						"where watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? ";

				Cursor cursorDataHeader = DataSource.getInstance(getActivity())
						.getReadOperation()
						.rawQuery(queryDataHeader, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
								String.valueOf(day), String.valueOf(month), String.valueOf(year));

				if (cursorDataHeader.moveToFirst()){
					minValue = cursorDataHeader.getInt(0);
					maxValue = cursorDataHeader.getInt(1);

					if (maxValue != 0){

						if(minValue == maxValue) {
							minValue -= 5;
							maxValue += 5;
						}

						if (minValue < 0) {
							maxValue = 0;
							minValue = 0;
						}

						tvMaxRateValue.setText(mDecimalFormat.format(maxValue));
						tvMinRateValue.setText(mDecimalFormat.format(minValue));
					}
					else{
						query = "select min(averageHR),max(averageHR) from StatisticalDataPoint dataPoint " +
								"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
								"where dataHeader.watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and averageHR > 0";

						cursor = DataSource.getInstance(getActivity())
								.getReadOperation()
								.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
										String.valueOf(day), String.valueOf(month), String.valueOf(year));

						if(cursor.moveToFirst()) {
							minValue = cursor.getInt(0);
							maxValue = cursor.getInt(1);

							if(minValue == maxValue) {
								minValue -= 5;
								maxValue += 5;
							}

							if (minValue < 0) {
								maxValue = 0;
								minValue = 0;
							}
							tvMaxRateValue.setText(mDecimalFormat.format(maxValue));
							tvMinRateValue.setText(mDecimalFormat.format(minValue));
						} else {
							tvMaxRateValue.setText("0");
							tvMinRateValue.setText("0");
						}

						cursor.close();
					}
				}
				else{
					query = "select min(averageHR),max(averageHR) from StatisticalDataPoint dataPoint " +
							"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
							"where dataHeader.watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and averageHR > 0";

					Cursor cursorData = DataSource.getInstance(getActivity())
							.getReadOperation()
							.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
									String.valueOf(day), String.valueOf(month), String.valueOf(year));

					if(cursorData.moveToFirst()) {
						minValue = cursorData.getInt(0);
						maxValue = cursorData.getInt(1);

						if(minValue == maxValue) {
							minValue -= 5;
							maxValue += 5;
						}

						if (minValue < 0) {
							maxValue = 0;
							minValue = 0;
						}
						tvMaxRateValue.setText(mDecimalFormat.format(maxValue));
						tvMinRateValue.setText(mDecimalFormat.format(minValue));
					} else {
						tvMaxRateValue.setText("0");
						tvMinRateValue.setText("0");
					}

					cursorData.close();
				}
				cursorDataHeader.close();

				multipleRenderer.setShowTickMarks(false);
				mBarChart = new BarChart(multipleDataset, multipleRenderer, BarChart.Type.STACKED);
				GraphicalView graphView = new GraphicalView(getActivity(), mBarChart);
				mHeartRatePlotContainer.removeAllViews();
				mHeartRatePlotContainer.addView(graphView);
			}

		} else {

			multipleRenderer.setMargins(new int[]{0, 0, 0, 0});

			if (mHeartRateValues.size() < 144)
			{
				for (int i=mHeartRateValues.size() - 1;i<144;i++) {
					mHeartRateValues.add(0d);
				}
			}

			if (mWorkoutHeadersData.size() != 0 && mPosition == 1) {
				(getActivity().findViewById(R.id.linear_color)).setVisibility(View.VISIBLE);
				showHover();
//				FrameGraphBackGround.setBackgroundResource(0);
//				FrameGraphBackGround.setVisibility(View.GONE);
				LandScapeHeartRateMaxBPM.clear();
				LandScapeHeartRateMinBPM.clear();
				LandScapeHeartRateIndex.clear();
				LandScapeHeartRateAVG.clear();
				//mHeartRateSeries.clear();
				int intenseHR = 0;
				int maxBPM = 0;
				int minBPM = 0;
				double sum = 0;
				double average = 0.0;
				double sumLength = 0;


				for (int x = 0; x < mWorkoutHeadersData.size(); x++) {
					trueValuesHR.clear();
					WorkoutHeader workoutHeader = mWorkoutHeadersData.get(x);
					int hour = workoutHeader.getTimeStampHour();
					int minute = workoutHeader.getTimeStampMinute();
					int sec = workoutHeader.getTimeStampSecond();
					String heartRate = workoutHeader.getHeaderHeartRate();
					int hrLogRate  = workoutHeader.getLogRateHR();
					try {
						JSONArray jsonArray = new JSONArray(heartRate);

						if (jsonArray != null){

							for (int i = 0; i < jsonArray.length(); ++i) {
								for (int y = 0; y < hrLogRate; y ++){
									trueValuesHR.add(jsonArray.optInt(i));
									sum = sum + jsonArray.optInt(i);
								}
							}


							int[] numbers = Ints.toArray(trueValuesHR);


							int startingTime = (hour * 3600) + (minute * 60) + sec;
							int plotIndex = startingTime / 600;
							if ((startingTime % 600) != 0)
								plotIndex++;
							int startingTimeInTenMinutes = startingTime / 600;


							double sum10Min = 0;
							double average10Min = 0.0;



							mHeartRateIndex.clear();

							int valueMinus = 0;
							int overAllMinus = 0;
							for (int a = 0; a < 144; a++){
								if (a == startingTimeInTenMinutes){
									valueMinus = ((startingTimeInTenMinutes + 1) * 600) - startingTime;
									if (mHeartRateIndex.size() == 0){
										if (valueMinus > numbers.length){
											mHeartRateIndex.add(numbers.length);
											break;
										}
										else{
											mHeartRateIndex.add(valueMinus);
											overAllMinus = overAllMinus + valueMinus;
											startingTimeInTenMinutes ++;
										}

									}
									else{
										if (overAllMinus < numbers.length){
											valueMinus = valueMinus - overAllMinus;
											mHeartRateIndex.add(valueMinus);
											overAllMinus = overAllMinus + valueMinus;
											startingTimeInTenMinutes++;
										}
										else{
											overAllMinus = overAllMinus - 600;
											if (overAllMinus > 0)
												if ((numbers.length - overAllMinus) < 60){
													mHeartRateIndex.remove(mHeartRateIndex.size()- 1);
												}
												else {
													mHeartRateIndex.set((mHeartRateIndex.size() - 1), (numbers.length - overAllMinus));
												}
												//mHeartRateIndex.set((mHeartRateIndex.size() - 1), (numbers.length - overAllMinus));
											else{

											}
											break;
										}

									}
								}
							}

							int valueOverAll = 0;
							int index = 0;
							for (int i = 0; i < mHeartRateIndex.size() ; i++){
								int valueEnding = mHeartRateIndex.get(i);

								int minValues= 240;
								int maxValues= 0;
								for (int y = 0; y < valueEnding; y++){
									if (numbers[y + valueOverAll] > 0){
										sum10Min = sum10Min + numbers[y + valueOverAll];
										if (minValues > numbers[y + valueOverAll]){
											minValues = numbers[y + valueOverAll];
										}
										if (maxValues < numbers[y + valueOverAll]){
											maxValues = numbers[y + valueOverAll];
										}
										index++;
									}
								}
								valueOverAll = valueOverAll + valueEnding;
								average10Min = sum10Min / index;

								if (average10Min > 0) {
									mHeartRateSeries.remove(plotIndex);
									mHeartRateSeries.add((int) plotIndex + i, (int) average10Min);
									mHeartRateValues.set((int) plotIndex + i, average10Min);
									avgAverage.add((float) average10Min);

									LandScapeHeartRateIndex.add(plotIndex + i);
									LandScapeHeartRateAVG.add((int)average10Min);
									LandScapeHeartRateMinBPM.add(minValues);
									LandScapeHeartRateMaxBPM.add(maxValues);
								}
								average10Min = 0;
								sum10Min = 0;
								index = 0;

							}
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}


				}

				multipleDataset.addSeries(mHeartRateSeries);
				multipleRenderer.setYAxisMin(0);
				multipleRenderer.setYAxisMax(240);
				multipleRenderer.setPanEnabled(false, false);
				multipleRenderer.setBarWidth(50);
				multipleRenderer.setXAxisMin(0);
				multipleRenderer.setXAxisMax(144);
				multipleRenderer.setMargins(new int[] {0, 0, 0, 0});
				multipleRenderer.setShowTickMarks(true);
				multipleRenderer.setPanEnabled(false);
				multipleRenderer.setXLabels(0);
				int index = 0;

				for(int i=0;i<144;i++) {
					if(i % 6 == 0) {
						switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
							case TIME_FORMAT_12_HR:
								multipleRenderer.addXTextLabel(i+1, mHours[index]);
								break;
							case TIME_FORMAT_24_HR:
								multipleRenderer.addXTextLabel(i+1, String.format("%02d:00", index));
								break;
						}
						index++;
					}
				}
				//Log.v("Series Count", ""+ mHeartRateSeries.getItemCount());
				if(orientation() == Configuration.ORIENTATION_LANDSCAPE && mPosition == 1) {
					mGraphScroll.setGraphScrollViewListener(mGraphScrollViewListener);
				}

				mBarChart = new BarChart(multipleDataset, multipleRenderer, BarChart.Type.STACKED);
				GraphicalView graphView = new GraphicalView(getActivity(), mBarChart);
				mHeartRatePlotContainer.removeAllViews();
				mHeartRatePlotContainer.addView(graphView);

				if (intenseHR != 0){
					if (intenseHR > 60)
					{
						int intenseHRMinute = intenseHR / 60;
						int intenseHRSec = intenseHR % 60;
						tvIntenseHRDuration.setText(String.valueOf(intenseHRMinute) + ":" + String.format("%02d", intenseHRSec));
					}
					else
						tvIntenseHRDuration.setText("0:"+ String.format("%02d", intenseHR));
				}
			} else{

				//hideHover();
				LinearValues.setVisibility(View.GONE);
//				(getActivity().findViewById(R.id.linear_color)).setVisibility(View.GONE);
//				FrameGraphBackGround.setBackgroundResource(R.drawable.ll_fitnessres_landscape_bg_main);
//				FrameGraphBackGround.setVisibility(View.VISIBLE);

				multipleDataset.addSeries(mHeartRateSeries);
				multipleRenderer.setYAxisMin(0);
				multipleRenderer.setYAxisMax(240);
				multipleRenderer.setPanEnabled(false, false);
				multipleRenderer.setBarWidth(50);
				multipleRenderer.setXAxisMin(0);
				multipleRenderer.setXAxisMax(144);
				multipleRenderer.setMargins(new int[]{0, 0, 0, 0});
				multipleRenderer.setShowTickMarks(true);
				multipleRenderer.setPanEnabled(false);
				multipleRenderer.setXLabels(0);
				int index = 0;


				for(int i=0;i<144;i++) {
					if(i % 6 == 0) {
						switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
						case TIME_FORMAT_12_HR:
							multipleRenderer.addXTextLabel(i+1, mHours[index]);
							break;
						case TIME_FORMAT_24_HR:
							multipleRenderer.addXTextLabel(i+1, String.format("%02d:00", index));
							break;
						}
						index++;
					}
				}

				String queryDataHeader = "select minimumBPM,maximumBPM from StatisticalDataHeader " +
						"where watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? ";

				Cursor cursorDataHeader = DataSource.getInstance(getActivity())
						.getReadOperation()
						.rawQuery(queryDataHeader, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
								String.valueOf(day), String.valueOf(month), String.valueOf(year));

				if (cursorDataHeader.moveToFirst()){
					minValue = cursorDataHeader.getInt(0);
					maxValue = cursorDataHeader.getInt(1);

					if (maxValue != 0){

						if(minValue == maxValue) {
							minValue -= 5;
							maxValue += 5;
						}

						if (minValue < 0) {
							maxValue = 0;
							minValue = 0;
						}

						tvMaxRateValue.setText(mDecimalFormat.format(maxValue));
						tvMinRateValue.setText(mDecimalFormat.format(minValue));
					}
					else{
						query = "select min(averageHR),max(averageHR) from StatisticalDataPoint dataPoint " +
								"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
								"where dataHeader.watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and averageHR > 0";

						cursor = DataSource.getInstance(getActivity())
								.getReadOperation()
								.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
										String.valueOf(day), String.valueOf(month), String.valueOf(year));

						if(cursor.moveToFirst()) {
							minValue = cursor.getInt(0);
							maxValue = cursor.getInt(1);

							if(minValue == maxValue) {
								minValue -= 5;
								maxValue += 5;
							}

							if (minValue < 0) {
								maxValue = 0;
								minValue = 0;
							}
							tvMaxRateValue.setText(mDecimalFormat.format(maxValue));
							tvMinRateValue.setText(mDecimalFormat.format(minValue));
						} else {
							tvMaxRateValue.setText("0");
							tvMinRateValue.setText("0");
						}

						cursor.close();
					}
				}
				else{
					query = "select min(averageHR),max(averageHR) from StatisticalDataPoint dataPoint " +
							"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
							"where dataHeader.watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and averageHR > 0";

					Cursor cursorData = DataSource.getInstance(getActivity())
							.getReadOperation()
							.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
									String.valueOf(day), String.valueOf(month), String.valueOf(year));

					if(cursorData.moveToFirst()) {
						minValue = cursorData.getInt(0);
						maxValue = cursorData.getInt(1);

						if(minValue == maxValue) {
							minValue -= 5;
							maxValue += 5;
						}

						if (minValue < 0) {
							maxValue = 0;
							minValue = 0;
						}
						tvMaxRateValue.setText(mDecimalFormat.format(maxValue));
						tvMinRateValue.setText(mDecimalFormat.format(minValue));
					} else {
						tvMaxRateValue.setText("0");
						tvMinRateValue.setText("0");
					}

					cursorData.close();
				}
				cursorDataHeader.close();

				if(orientation() == Configuration.ORIENTATION_LANDSCAPE && mPosition == 1) {
					mGraphScroll.setGraphScrollViewListener(mGraphScrollViewListener);

					mBarChart = new BarChart(multipleDataset, multipleRenderer, BarChart.Type.STACKED);
					GraphicalView graphView = new GraphicalView(getActivity(), mBarChart);
					mHeartRatePlotContainer.removeAllViews();
					mHeartRatePlotContainer.addView(graphView);
				}
		}


			initializeStatsPort(date);

//			mHandler.postDelayed(new Runnable() {
//				public void run() {
//					scrollToXValue(mScrollPosition);
//				}
//			}, 500);

//			if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300
//					&& getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C410) {
//				mTableLayoutMinMaxContainer.setVisibility(View.VISIBLE);
//			}
//			else{
//				mTableLayoutMinMaxContainer.setVisibility(View.GONE);
//			}
		}



//		if(orientation() == Configuration.ORIENTATION_LANDSCAPE)
//			mGraphScroll.setGraphScrollViewListener(mGraphScrollViewListener);


		initializeStatsPort(date);
	}


	private void showProgress(boolean enabled){
		if (enabled){
			mProgressBar.setVisibility(View.VISIBLE);
			enableDisableView(false);
		}
		else
		{
			mProgressBar.setVisibility(View.GONE);
			enableDisableView(true);
		}
	}
	private void enableDisableView (boolean enabled) {
		mView.setEnabled(enabled);
		for (int i = 0; i < mView.getChildCount(); i++) {
			View child = mView.getChildAt(i);
			child.setEnabled(enabled);
		}
	}


	private void initializeStatsPort(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		String query = "select sum(averageHR) / count(averageHR) from StatisticalDataPoint dataPoint " +
				"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
				"where watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and averageHR > 0";

		Cursor cursor = DataSource.getInstance(getActivity())
				.getReadOperation()
				.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
						String.valueOf(day), String.valueOf(month), String.valueOf(year));
		if(cursor.moveToFirst()) {
			int averageBPM = cursor.getInt(0);
			if (averageBPM > 0 && mWorkoutHeadersData.size() == 0)
				mAverageBPMValue.setText(String.valueOf(averageBPM));
			float percent = percentForAverageBPM(averageBPM);
			int leftMargin = (int) (dpToPx(150) * percent) - ((int)dpToPx(40) / 2);
			mIntensityTag.setText(mDecimalFormat2.format(percent * 100.0f) + "%");

			if(leftMargin < 0)
				leftMargin = 0;

			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			params.leftMargin = leftMargin;
			mIntensityTag.setLayoutParams(params);

//			if(percent * 100 == 0) {
//				mIntensityTag.setBackgroundResource(R.drawable.dash_2_6tagm);
//			} else if(percent * 100 > 0 && percent * 100 < 25) {
//				mIntensityTag.setBackgroundResource(R.drawable.dash_2_1tagvl);
//			} else if(percent * 100 > 25 && percent * 100 < 50) {
//				mIntensityTag.setBackgroundResource(R.drawable.dash_2_2tagl);
//			} else if(percent * 100 > 50 && percent * 100 < 75) {
//				mIntensityTag.setBackgroundResource(R.drawable.dash_2_3tagm);
//			} else if(percent * 100 > 75 && percent * 100 < 100) {
//				mIntensityTag.setBackgroundResource(R.drawable.dash_2_4tagh);
//			} else if(percent * 100 >= 100) {
//				mIntensityTag.setBackgroundResource(R.drawable.dash_2_5tagvh);
//			}
		}
	}


	private float percentForAverageBPM(int averageBPM) {
		UserProfile userProfile = getLifeTrakApplication().getUserProfile();

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, userProfile.getBirthDay());
		calendar.set(Calendar.MONTH, userProfile.getBirthMonth() - 1);
		calendar.set(Calendar.YEAR, userProfile.getBirthYear());

		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(new Date());

		calendar2.add(Calendar.DAY_OF_MONTH, -calendar.get(Calendar.DAY_OF_MONTH));
		calendar2.add(Calendar.MONTH, -calendar.get(Calendar.MONTH));
		calendar2.add(Calendar.YEAR, -calendar.get(Calendar.YEAR));

		int age = calendar2.get(Calendar.YEAR);
		int maxBPM = (userProfile.getGender() == GENDER_MALE) ? 220 : 226;
		maxBPM -= age;

		return (float) averageBPM / (float) maxBPM;
	}















	public void setCalendarMode(int calendarMode) {
		if(!isAdded())
			return;

		mCalendarMode = calendarMode;

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
			//mToDateGroup.setOnCheckedChangeListener(mOnCheckChangedListener);
		}
	}

	private final GraphScrollView.GraphScrollViewListener mGraphScrollViewListener = new GraphScrollView.GraphScrollViewListener() {
		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {


			View leftView = getView().findViewById(R.id.viewGraphLeftPadding);

			for(int i=0;i<mHeartRateSeries.getItemCount();i++) {
					double x = mHeartRateSeries.getX(i);
					double[] screenPoint = mBarChart.toScreenPoint(new double[] {x, 0});

					int halfBarWidth =  (int)(mBarChart.getRenderer().getBarWidth() / 2);
					int playHeadPositionX = (mPlayhead.getLeft() + (mPlayhead.getMeasuredWidth() / 2));
					int currentPointX = ((int)(leftView.getMeasuredWidth() + screenPoint[0])) - l + (int)mBarChart.getRenderer().getBarSpacing();

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

						int averageBPM = mHeartRateValues.get(index).intValue();
						float percent = percentForAverageBPM(averageBPM);
						int leftMargin = (int) (dpToPx(150) * percent) - ((int)dpToPx(40) / 2);
						mIntensityTag.setText(mDecimalFormat2.format(percent * 100.0f) + "%");

						mAverageBPMValue.setText(String.valueOf(mHeartRateValues.get(index).intValue()));

						if (LandScapeHeartRateIndex.size() > 0){
							for (int y = 0 ;y < LandScapeHeartRateIndex.size(); y++ ){
								int value = LandScapeHeartRateIndex.get(y);
								if (index == value){
									tvMaxRateValue.setText(String.valueOf(LandScapeHeartRateMaxBPM.get(y)));
									tvMinRateValue.setText(String.valueOf(LandScapeHeartRateMinBPM.get(y)));
									break;
								}
								else{
									tvMaxRateValue.setText("0");
									tvMinRateValue.setText("0");
								}
							}
						}
						break;
					} else {
						mAverageBPMValue.setText("0");

						FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
						params.leftMargin = 0;
						mIntensityTag.setText("0%");
						mIntensityTag.setLayoutParams(params);
						mIntensityTag.setBackgroundResource(R.drawable.dash_2_6tagm);
					}
				}



			}

	};




	public FrameLayout getmHeartRatePlotContainer() {
		return mHeartRatePlotContainer;
	}

	public FrameLayout getmHeartRateLoadingText() {
		return mHeartRateLoadingText;
	}

	public RelativeLayout getmHeartRateTopData() {
		return mHeartRateTopData;
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
					synchronized (workoutStops) {
						workoutStops = filterDuplicateWorkoutStops(workoutStops);
						for (WorkoutStopInfo workoutStop : workoutStops) {
							endTime += (workoutStop.getStopHours() * 3600) + (workoutStop.getStopMinutes() * 60) + workoutStop.getStopSeconds();
						}
					}
				}
			}
		}
		cursor.close();


		return endTime;
	}

	private List<WorkoutStopInfo> filterDuplicateWorkoutStops(List<WorkoutStopInfo> workoutStops){
		List<WorkoutStopInfo> filteredWorkoutStops = new CopyOnWriteArrayList<WorkoutStopInfo>();
		if (workoutStops.size() > 0){
			filteredWorkoutStops.add(workoutStops.get(0));
		}
		synchronized (workoutStops) {
			for (WorkoutStopInfo workoutStop : workoutStops) {
				if(filteredWorkoutStops.size() > 0){
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
		}

		return filteredWorkoutStops;
	}



}