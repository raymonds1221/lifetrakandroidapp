package com.salutron.lifetrakwatchapp.fragment;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.RangeBarChart;
import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
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

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.view.GraphScrollView;

public class HeartRateItemFragment extends BaseFragment {
	private FrameLayout mHeartRatePlotContainer;
	private FrameLayout mHeartRateLoadingText;
	private RelativeLayout mHeartRateTopData;
	private RadioGroup mToDateGroup;
	private Spinner mCalendarModeSpinner;
	private TextView mAverageBPMValue;
	private TextView mIntensityTag;
	private TextView mMaxBPM;
	private TextView mMinBPM;
	private ImageView mPlayhead;
	private GraphScrollView mGraphScroll;
	private LinearLayout mView;
	private TableLayout mTableLayoutMinMaxContainer;

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

	private int mPosition;
	private double mScrollPosition;
	private int mCalendarMode = MODE_DAY;

	private int mYearScrollPosition = 0;
	private int mMonthScrollPosition = 0;
	private int mWeekScrollPosition = 0;

	private Date mDateNow;
	private Date mDateFrom;
	private Date mDateTo;
	private int mYear;

	private ProgressBar mProgressBar;

	private int mYearCount = 0;

	private final String SCROLLER_RECEIVER = "com.salutron.lifetrak.SCROLLER_RECEIVER";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_heart_rate_item, null);

		initializeViews(view);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if(savedInstanceState != null)
			mCalendarMode = savedInstanceState.getInt(CALENDAR_MODE_KEY);

		initializeObjects();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		LayoutInflater inflater = LayoutInflater.from(getActivity());

		switch(newConfig.orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			changeFramentView(inflater, R.layout.fragment_heart_rate_item, (ViewGroup) getView());
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			changeFramentView(inflater, R.layout.fragment_heart_rate_item_land, (ViewGroup) getView());
			break;
		}

		initializeViews(getView());

		switch(getCalendarMode()) {
		case MODE_DAY:
			setDataWithDate(mDateNow);
			break;
		case MODE_WEEK:
			setDataWithWeek(mDateFrom, mDateTo);
			break;
		case MODE_MONTH:
			setDataWithDateRange(mDateFrom, mDateTo, MODE_MONTH);
			break;
		case MODE_YEAR:
			setDataWithYear(mYear);
			break;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		int calendarMode = MODE_DAY;

		switch(orientation()) {
		case Configuration.ORIENTATION_PORTRAIT:
			if(mCalendarModeSpinner != null)
				switch(mCalendarModeSpinner.getSelectedItemPosition()) {
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
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			if(mToDateGroup != null)
				switch(mToDateGroup.getCheckedRadioButtonId()) {
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
			break;
		}

		outState.putInt(CALENDAR_MODE_KEY, calendarMode);
	}

	private void initializeViews(View view) {
		mHeartRatePlotContainer = (FrameLayout) view.findViewById(R.id.frmHeartRatePlotContainer);
		mView  = (LinearLayout)view.findViewById(R.id.mainLayout);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		mToDateGroup = (RadioGroup) view.findViewById(R.id.rdgToDate);
		mAverageBPMValue = (TextView) view.findViewById(R.id.tvwAvgBpmValue);
		mIntensityTag = (TextView) view.findViewById(R.id.tvwIntentsityTag);
		mCalendarModeSpinner = (Spinner) view.findViewById(R.id.spnCalendarMode);
		mMaxBPM = (TextView) view.findViewById(R.id.tvwMaxBPM);
		mMinBPM = (TextView) view.findViewById(R.id.tvwMinBPM);
		mPlayhead = (ImageView) view.findViewById(R.id.imgPlayhead);
		mGraphScroll = (GraphScrollView) view.findViewById(R.id.gsvGraphScroll);
		mTableLayoutMinMaxContainer = (TableLayout) view.findViewById(R.id.tableBpmMinMaxContainer);

		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			mHeartRateLoadingText = (FrameLayout) view.findViewById(R.id.frmHeartRateLoadingText);
			mHeartRateTopData = (RelativeLayout) view.findViewById(R.id.tvwAvgBpmLayout);
		}
		if(mCalendarModeSpinner != null)
			mCalendarModeSpinner.setOnItemSelectedListener(null);
		if(mToDateGroup != null)
			mToDateGroup.setOnCheckedChangeListener(null);

		switch(orientation()) {
		case Configuration.ORIENTATION_LANDSCAPE:
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.calendar_mode_spinner, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mCalendarModeSpinner.setAdapter(adapter);

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

			hideActionBarAndCalendar();
			break;
		case Configuration.ORIENTATION_PORTRAIT:

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

			showActionBarAndCalendar();
			break;
		}

		if(mCalendarModeSpinner != null)
			mCalendarModeSpinner.setOnItemSelectedListener(mItemSelectedListener);
		if(mToDateGroup != null)
			mToDateGroup.setOnCheckedChangeListener(mOnCheckChangedListener);
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

			switch(getCalendarMode()) {
			case MODE_DAY:
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

				if(mPosition == 1) {
					activity.setCalendarDate(date);
					activity.setCalendarPickerMode(MODE_DAY);
				}
				break;
			case MODE_WEEK:
				setDataWithWeek(from, to);

				if(mPosition == 1) {
					activity.setCalendarDateWeek(from, to);
					activity.setCalendarPickerMode(MODE_WEEK, from, to);
				}
				break;
			case MODE_MONTH:
				setDataWithDateRange(from, to, MODE_MONTH);

				if(mPosition == 1) {
					activity.setCalendarDateWeek(from, to);
					activity.setCalendarPickerMode(MODE_MONTH, from, to);
				}
				break;
			case MODE_YEAR:
				int year = getLifeTrakApplication().getCurrentYear();
				setDataWithYear(year);

				if(mPosition == 1)
					activity.setCalendarYear(year);
				break;
			}
		}

		if(mCalendarModeSpinner != null)
			mCalendarModeSpinner.setOnItemSelectedListener(mItemSelectedListener);
		if(mToDateGroup != null)
			mToDateGroup.setOnCheckedChangeListener(mOnCheckChangedListener);
	}

	public void setDataWithDate(Date date) {
		if(!isAdded())
			return;

		mDateNow = date;

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

		for(Double value : mHeartRateValues) {
			double y = getYWithMaxValue(value, maxValue, HEART_RATE_MIN_Y, HEART_RATE_MAX_Y);
			mHeartRateSeries.add(hrIndex, y);
			hrIndex++;
		}

		for(int i=0;i<mHeartRateValues.size();i++) {
			if(mHeartRateValues.get(i) > 0) {
				mScrollPosition = i;
				break;
			}
		}

		multipleDataset.addSeries(mHeartRateSeries);
		multipleRenderer.addSeriesRenderer(renderer);

		multipleRenderer.setZoomEnabled(false, false);
		multipleRenderer.setMargins(new int[] {0, 0, 0, 0});
		multipleRenderer.setShowLegend(false);
		multipleRenderer.setShowLabels(true);
		multipleRenderer.setShowGridY(false);
		multipleRenderer.setShowCustomTextGridY(false);
		multipleRenderer.setShowAxes(false);
		multipleRenderer.setMarginsColor(getResources().getColor(R.color.color_xaxis_bar));
		multipleRenderer.setLabelsTextSize(dpToPx(12));
		multipleRenderer.setXLabelsColor(getResources().getColor(android.R.color.black));
		multipleRenderer.setYLabels(0);

		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			multipleRenderer.setYAxisMin(HEART_RATE_MIN_Y);
			multipleRenderer.setYAxisMax(HEART_RATE_MAX_Y);
			multipleRenderer.setPanEnabled(false);
			multipleRenderer.setBarWidth(10);
			multipleRenderer.setXAxisMin(0);
			multipleRenderer.setXAxisMax(143);
			multipleRenderer.setMargins(new int[] {0, (int)dpToPx(5), 0, (int)dpToPx(5)});

			multipleRenderer.setXLabels(0);
			multipleRenderer.setXLabelsAlign(Align.CENTER);

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

			multipleRenderer.setShowTickMarks(false);
		} else {
			multipleRenderer.setYAxisMin(HEART_RATE_MIN_Y);
			multipleRenderer.setYAxisMax(20);
			multipleRenderer.setPanEnabled(false, false);
			multipleRenderer.setBarWidth(50);
			multipleRenderer.setXAxisMin(0);
			multipleRenderer.setXAxisMax(144);
			multipleRenderer.setMargins(new int[] {0, 0, 0, 0});
			multipleRenderer.setShowTickMarks(true);

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
					
					mMaxBPM.setText(mDecimalFormat.format(maxValue));
					mMinBPM.setText(mDecimalFormat.format(minValue));
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
						mMaxBPM.setText(mDecimalFormat.format(maxValue));
						mMinBPM.setText(mDecimalFormat.format(minValue));
					} else {
						mMaxBPM.setText("0");
						mMinBPM.setText("0");
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
					mMaxBPM.setText(mDecimalFormat.format(maxValue));
					mMinBPM.setText(mDecimalFormat.format(minValue));
				} else {
					mMaxBPM.setText("0");
					mMinBPM.setText("0");
				}

				cursorData.close();
			}
			cursorDataHeader.close();

			

			mHandler.postDelayed(new Runnable() {
				public void run() {
					scrollToXValue(mScrollPosition);
				}
			}, 500);

			if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300
					&& getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C410) {
				mTableLayoutMinMaxContainer.setVisibility(View.VISIBLE);
			}
			else{
				mTableLayoutMinMaxContainer.setVisibility(View.GONE);
			}	
		}
        multipleRenderer.setPanEnabled(false);
		mBarChart = new BarChart(multipleDataset, multipleRenderer, BarChart.Type.STACKED);
		GraphicalView graphView = new GraphicalView(getActivity(), mBarChart);
		mHeartRatePlotContainer.removeAllViews();
		mHeartRatePlotContainer.addView(graphView);

		if(orientation() == Configuration.ORIENTATION_LANDSCAPE)
			mGraphScroll.setGraphScrollViewListener(mGraphScrollViewListener);

		initializeStatsPort(date);
	}

	public void setDataWithWeek(Date from, Date to) {
		if(!isAdded())
			return;


		mDateFrom = from;
		mDateTo = to;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(from);

		mCategorySeries.clear();
		mAverageHRs.clear();

		mScrollPosition = 0;
		if (mPosition == 1)
			mWeekScrollPosition = 0;

		int averageCount = 0;

		while(calendar.getTime().before(to) || calendar.getTime().equals(to)) {
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH) + 1;
			int year = calendar.get(Calendar.YEAR) - 1900;

			String query = "select averageHR from StatisticalDataHeader dataheader " +
					"inner join StatisticalDataPoint datapoint on dataheader._id = datapoint.dataHeaderAndPoint " +
					"where dataheader.dateStampDay = ? and dataheader.dateStampMonth = ? and dataheader.dateStampYear = ? and dataheader.watchDataHeader = ?";

			final Cursor cursor = DataSource.getInstance(getActivity())
					.getReadOperation()
					.rawQuery(query, String.valueOf(day), String.valueOf(month), String.valueOf(year), String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()));

			List<Double> values = new ArrayList<Double>();

			while(cursor.moveToNext()) {
				values.add(cursor.getDouble(0));
			}

			double minValue = 0;
			double maxValue = 0;
			int totalCount = 0;
			int scrollIndex = 0;
			int index =  0;

			if(cursor.getCount() > 0) {
				double totalAverageHR = 0;

				for (Double value : values) {
					double averageHR = value.doubleValue();
					totalAverageHR += averageHR;
					scrollIndex++;

					if(averageHR > 0) {
						if(minValue == 0)
							minValue = averageHR;

						minValue = Math.min(minValue, averageHR);
						maxValue = Math.max(maxValue, averageHR);
						totalCount++;


					}

					index++;

					if(index % 12 == 0) {
						if (mPosition == 1)
							averageCount ++;
						if (minValue > 0 && maxValue > 0) {
							if(minValue == maxValue) {
								minValue -= 5;
								maxValue += 5;
							}
							if (mWeekScrollPosition == 0 && mPosition == 1)
								mWeekScrollPosition = averageCount;
							mCategorySeries.add(minValue, maxValue);
							mAverageHRs.add(totalAverageHR / totalCount);
							minValue = 0;
							maxValue = 0;
						} else {
							mCategorySeries.add(0, 0);
							mAverageHRs.add(0.0);
						}

						totalAverageHR = 0;
						totalCount = 0;
					} else if(index == cursor.getCount()) {
						if (mPosition == 1)
							averageCount ++;
						if(minValue > 0 && maxValue > 0 && minValue == maxValue) {
							minValue -= 5;
							maxValue += 5;
							if (mWeekScrollPosition == 0 && mPosition == 1)
								mWeekScrollPosition = averageCount;
						}

						mCategorySeries.add(minValue, maxValue);
						mAverageHRs.add(totalAverageHR / totalCount);

						totalAverageHR = 0;
						totalCount = 0;
					}
				}
				index = 0;
			} else {
				mCategorySeries.add(0, 0);
				mAverageHRs.add(0.0);
				mCategorySeries.add(0, 0);
				mAverageHRs.add(0.0);
				mCategorySeries.add(0, 0);
				mAverageHRs.add(0.0);
				mCategorySeries.add(0, 0);
				mAverageHRs.add(0.0);
				mCategorySeries.add(0, 0);
				mAverageHRs.add(0.0);
				mCategorySeries.add(0, 0);
				mAverageHRs.add(0.0);

				mCategorySeries.add(0, 0);
				mAverageHRs.add(0.0);
				mCategorySeries.add(0, 0);
				mAverageHRs.add(0.0);
				mCategorySeries.add(0, 0);
				mAverageHRs.add(0.0);
				mCategorySeries.add(0, 0);
				mAverageHRs.add(0.0);
				mCategorySeries.add(0, 0);
				mAverageHRs.add(0.0);
				mCategorySeries.add(0, 0);
				mAverageHRs.add(0.0);
			}

			cursor.close();

			if(orientation() == Configuration.ORIENTATION_LANDSCAPE) {
				if(mScrollPosition == 0 && mPosition == 1) {
					mHandler.postDelayed(new Runnable() {
						public void run() {
							//scrollToXValueForRange(mScrollPosition, calendarMode);
							scrollToXValue2(mWeekScrollPosition, mCategorySeries.getItemCount());
						}
					}, 1500);

				}

				if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300
						&& getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C410) {
					mTableLayoutMinMaxContainer.setVisibility(View.VISIBLE);
				}
				else{
					mTableLayoutMinMaxContainer.setVisibility(View.GONE);
				}

			}
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		final XYMultipleSeriesDataset multipleDataset = new XYMultipleSeriesDataset();
		final XYMultipleSeriesRenderer multipleRenderer = new XYMultipleSeriesRenderer();

		multipleRenderer.setXLabels(0);
		multipleRenderer.setYLabels(0);
		multipleRenderer.setMarginsColor(getResources().getColor(R.color.color_xaxis_bar));
		multipleRenderer.setLabelsTextSize(dpToPx(12));
		multipleRenderer.setXLabelsColor(getResources().getColor(android.R.color.black));
		multipleRenderer.setShowGridY(false);
		multipleRenderer.setZoomEnabled(false, false);
		multipleRenderer.setXAxisMin(0);
		multipleRenderer.setXAxisMax(78);

		multipleRenderer.setMargins(new int[] {0, 0, 0, 0});
		multipleRenderer.setShowLegend(false);
		multipleRenderer.setShowAxes(false);

		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setColor(getResources().getColor(R.color.color_heart_rate_bar));

		multipleDataset.addSeries(mCategorySeries.toXYSeries());
		multipleRenderer.addSeriesRenderer(renderer);
		multipleRenderer.setPanEnabled(true, false);

		switch(orientation()) {
		case Configuration.ORIENTATION_PORTRAIT:
			multipleRenderer.setBarWidth(dpToPx(5));
			multipleRenderer.setMargins(new int[] {0, (int)dpToPx(5), 0, (int)dpToPx(5)});

			multipleRenderer.setXLabelsAlign(Align.CENTER);
			multipleRenderer.addXTextLabel(3, mDateFormat.format(from));
			multipleRenderer.addXTextLabel(74, mDateFormat.format(to));
			multipleRenderer.setShowTickMarks(false);
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			multipleRenderer.setXLabelsAlign(Align.CENTER);
			multipleRenderer.setBarWidth(dpToPx(20));

			calendar.setTime(from);

			for (int i=0;i<=7*12;i++) {
				if (i % 12 == 0) {
					multipleRenderer.addXTextLabel(i, mDateFormat.format(calendar.getTime()));
					calendar.add(Calendar.DAY_OF_MONTH, 1);
				}
			}

			break;
		}

		mRangeBarChart = new RangeBarChart(multipleDataset, multipleRenderer, BarChart.Type.DEFAULT);
		mGraphView = new GraphicalView(getActivity(), mRangeBarChart);

		mHeartRatePlotContainer.removeAllViews();
		mHeartRatePlotContainer.addView(mGraphView);
		initializeStatsPort(from, to);

		//		if(orientation() == Configuration.ORIENTATION_LANDSCAPE && mPosition == 1) {
		//			mGraphScroll.setGraphScrollViewListener(mGraphScrollViewListenerWeekAndMonth);
		//			mHandler.postDelayed(new Runnable() {
		//				public void run() {
		//					scrollToXValue2(mScrollPosition);
		//				}
		//			}, 1500);
		//		}
		if(mPosition == 1) {
			if(orientation() == Configuration.ORIENTATION_LANDSCAPE) {

				mGraphScroll.setGraphScrollViewListener(mGraphScrollViewListenerWeekAndMonth);
			}
		}
	}

	public void setDataWithDateRange(Date from, Date to, final int calendarMode) {
		if(!isAdded())
			return;

		mDateFrom = from;
		mDateTo = to;

		Calendar calendar = Calendar.getInstance();

		mCategorySeries.clear();
		mAverageHRs.clear();

		XYMultipleSeriesDataset multipleDataset = new XYMultipleSeriesDataset();
		XYMultipleSeriesRenderer multipleRenderer = new XYMultipleSeriesRenderer();

		multipleRenderer.setXLabels(0);
		multipleRenderer.setYLabels(0);
		multipleRenderer.setMarginsColor(getResources().getColor(R.color.color_xaxis_bar));
		multipleRenderer.setLabelsTextSize(dpToPx(12));
		multipleRenderer.setXLabelsColor(getResources().getColor(android.R.color.black));
		multipleRenderer.setShowGridY(false);
		multipleRenderer.setZoomEnabled(false, false);
		multipleRenderer.setPanEnabled(true, false);

		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setColor(getResources().getColor(R.color.color_heart_rate_bar));

		calendar.setTime(from);

		mScrollPosition = 0;
		if (mPosition == 1)
			mMonthScrollPosition = 0;

		int index = 0;
        int positionValue = 0;
        boolean isfound = false;

		while(calendar.getTime().before(to) || calendar.getTime().equals(to)) {
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH) + 1;
			int year = calendar.get(Calendar.YEAR) - 1900;

			String query = "select min(averageHR), max(averageHR) from StatisticalDataPoint dataPoint " +
					"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
					"where averageHR > 0 and dataHeader.watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?";

			double minValue = 0;
			double maxValue = 0;

			Cursor cursor = DataSource.getInstance(getActivity())
					.getReadOperation()
					.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
							String.valueOf(day), String.valueOf(month), String.valueOf(year));

			if(cursor.moveToFirst()) {
				minValue = cursor.getDouble(0);
				maxValue = cursor.getDouble(1);
			}

			cursor.close();



			if(minValue == maxValue && minValue > 0 && maxValue > 0) {
				minValue -= 5;
				maxValue += 5;
			}

			mCategorySeries.add(minValue, maxValue);

			query = "select avg(averageHR) from StatisticalDataPoint dataPoint " +
					"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
					"where dataHeader.watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and averageHR > 0";

			cursor = DataSource.getInstance(getActivity())
					.getReadOperation()
					.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
							String.valueOf(day), String.valueOf(month), String.valueOf(year));

			if(cursor.moveToFirst()) {
				mAverageHRs.add(cursor.getDouble(0));
                if (mPosition == 1) {
                    if (cursor.getDouble(0) > 1.0 && !isfound) {
                        isfound = true;
                        positionValue = index;

                        if (orientation() == Configuration.ORIENTATION_LANDSCAPE){
                            mMonthScrollPosition = positionValue + 1;
                            mHandler.postDelayed(new Runnable() {

                                public void run() {
                                    //scrollToXValueForRange(mScrollPosition, calendarMode);
                                    scrollToXValueMonth2(mMonthScrollPosition);

                                }
                            },2000);
                        }
                    }
                }
			}



			cursor.close();
			index++;

			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		multipleDataset.addSeries(mCategorySeries.toXYSeries());
		multipleRenderer.addSeriesRenderer(renderer);

		multipleRenderer.setMargins(new int[] {0, 0, 0, 0});
		multipleRenderer.setShowLegend(false);
		multipleRenderer.setShowAxes(false);


		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			multipleRenderer.setBarWidth(dpToPx(5));
			multipleRenderer.setMargins(new int[] {0, (int)dpToPx(5), 0, (int)dpToPx(5)});

			switch(calendarMode) {
			case MODE_WEEK:
				multipleRenderer.setXLabelsAlign(Align.CENTER);
				multipleRenderer.addXTextLabel(1.7, mDateFormat.format(from));
				multipleRenderer.addXTextLabel(7.1, mDateFormat.format(to));
				multipleRenderer.setShowTickMarks(false);
				break;
			case MODE_MONTH:
				multipleRenderer.setShowTickMarks(true);
				Calendar calFrom = Calendar.getInstance();
				calFrom.setTime(from);
//				int maxDays = calFrom.getActualMaximum(Calendar.DAY_OF_MONTH);
//                if (maxDays == 30){
//
//                    for (int i = 0; i < maxDays; i++) {
//                        if (i % 2 == 0) {
//                            multipleRenderer.addXTextLabel(i, String.valueOf(i));
//                        }
//                    }
//                }
//                else {
//                    for (int i = 0; i < maxDays; i++) {
//                        if (i % 2 == 0) {
//                            multipleRenderer.addXTextLabel(i, String.valueOf(i + 1));
//                        }
//                    }
//                }
                int x = 1;
                while(calFrom.getTime().before(to) || calFrom.getTime().equals(to)) {
                    if (x % 2 == 0) {
                        multipleRenderer.addXTextLabel(x, String.valueOf(calFrom.get(Calendar.DAY_OF_MONTH)));
                       }
                    x++;
                    calFrom.add(Calendar.DAY_OF_MONTH, 1);
                }
				break;
			}
		} else {
			if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300
					&& getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C410) {
				mTableLayoutMinMaxContainer.setVisibility(View.VISIBLE);
			}
			else{
				mTableLayoutMinMaxContainer.setVisibility(View.GONE);
			}

			multipleRenderer.setBarWidth(dpToPx(20));

			Calendar calFrom = Calendar.getInstance();
			calFrom.setTime(from);
			int x = 1;

			switch(calendarMode) {
			case MODE_WEEK:
				while(calFrom.getTime().before(to) || calFrom.getTime().equals(to)) {
					multipleRenderer.addXTextLabel(x, mDateFormat.format(calFrom.getTime()));
					x++;
					calFrom.add(Calendar.DAY_OF_MONTH, 1);
				}
				break;
			case MODE_MONTH:
				while(calFrom.getTime().before(to) || calFrom.getTime().equals(to)) {
					multipleRenderer.addXTextLabel(x, String.valueOf(calFrom.get(Calendar.DAY_OF_MONTH)));
					x++;
					calFrom.add(Calendar.DAY_OF_MONTH, 1);
				}
				break;
			}
		}

		int maxDays = 0;

		switch(calendarMode) {
		case MODE_WEEK:
			multipleRenderer.setXAxisMin(1);
			multipleRenderer.setXAxisMax(7.1);
			initializeStatsPort(from, to);
			break;
		case MODE_MONTH:
			calendar.setTime(from);
			maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			multipleRenderer.setXAxisMin(0);
			multipleRenderer.setXAxisMax(maxDays);
			initializeStatsPort(from, to);
			break;
		}

		mRangeBarChart = new RangeBarChart(multipleDataset, multipleRenderer, BarChart.Type.DEFAULT);
		mGraphView = new GraphicalView(getActivity(), mRangeBarChart);

		mHeartRatePlotContainer.removeAllViews();
		mHeartRatePlotContainer.addView(mGraphView);

		if(mPosition == 1) {
			if(orientation() == Configuration.ORIENTATION_LANDSCAPE) {

				mGraphScroll.setGraphScrollViewListener(mGraphScrollViewListenerWeekAndMonth);
			}
		}

        if(orientation() == Configuration.ORIENTATION_LANDSCAPE) {
//            if(minValue > 0 && maxValue > 0 && mScrollPosition == 0 && mPosition == 1) {
//                mScrollPosition = index;
//                mMonthScrollPosition = index + 1;
//
//                mHandler.postDelayed(new Runnable() {
//                    public void run() {
//                        //scrollToXValueForRange(mScrollPosition, calendarMode);
//                        scrollToXValueMonth(mMonthScrollPosition);
//
//                    }
//                }, 2000);
//
//            }



        }
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


	public void setDataWithYear(int year) {
		showProgress(true);
		if(!isAdded())
			return;

		mYear = year;

		XYMultipleSeriesDataset multipleDataset = new XYMultipleSeriesDataset();
		XYMultipleSeriesRenderer multipleRenderer = new XYMultipleSeriesRenderer();

		mCategorySeries.clear();
		mAverageHRs.clear();

		multipleRenderer.setXLabels(0);
		multipleRenderer.setYLabels(0);
		multipleRenderer.setMarginsColor(getResources().getColor(R.color.color_xaxis_bar));
		multipleRenderer.setXLabelsColor(getResources().getColor(android.R.color.black));
		multipleRenderer.setShowGridY(false);
		multipleRenderer.setZoomEnabled(false, false);

		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setColor(getResources().getColor(R.color.color_heart_rate_bar));

		Calendar calendar = Calendar.getInstance();

		SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getInstance();
		dateFormat.applyPattern("MMM");
		if (mPosition == 1)
			mYearScrollPosition = 0;
		mCategorySeries.clear();

		for(int i=0;i<12;i++) {
			String query = "select min(averageHR), max(averageHR) from StatisticalDataPoint dataPoint " +
					"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
					"where averageHR > 0 and dataHeader.watchDataHeader = ? and dateStampMonth = ? and dateStampYear = ?";

			Cursor cursor = DataSource.getInstance(getActivity())
					.getReadOperation()
					.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
							String.valueOf(i+1), String.valueOf(year - 1900));

			double minValue = 0.0;
			double maxValue = 0.0;

			if(cursor.moveToFirst()) {
				minValue = cursor.getDouble(0);
				maxValue = cursor.getDouble(1);

			}
			if(orientation() == Configuration.ORIENTATION_LANDSCAPE) {
				if(minValue > 0 && maxValue > 0 && mYearScrollPosition == 0) {
					if (mPosition == 1){
						mYearScrollPosition = i;
						mHandler.postDelayed(new Runnable() {
							public void run() {
								scrollToXValueYear(mYearScrollPosition);
							}
						}, 1500);
					}
				}
			}
			if(minValue == maxValue && minValue > 0 && maxValue > 0) {
				minValue -= 5;
				maxValue += 5;
			}

			mCategorySeries.add(minValue, maxValue);

			calendar.set(Calendar.MONTH, i);
			multipleRenderer.addXTextLabel(i+1, dateFormat.format(calendar.getTime()));

			cursor.close();

			query = "select avg(averageHR) from StatisticalDataPoint dataPoint " +
					"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
					"where averageHR > 0 and dataHeader.watchDataHeader = ? and dateStampMonth = ? and dateStampYear = ? and averageHR > 0";

			cursor = DataSource.getInstance(getActivity())
					.getReadOperation()
					.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
							String.valueOf(i+1), String.valueOf(year - 1900));


			if(cursor.moveToNext()) {
				mAverageHRs.add(cursor.getDouble(0));
			}
		}

		multipleDataset.addSeries(mCategorySeries.toXYSeries());
		multipleRenderer.addSeriesRenderer(renderer);
		multipleRenderer.setMargins(new int[] {0, 0, 0, 0});
		multipleRenderer.setShowLegend(false);
		multipleRenderer.setXAxisMin(0);
		multipleRenderer.setXAxisMax(13);
		multipleRenderer.setYAxisMin(40);
		multipleRenderer.setYAxisMax(240);
		multipleRenderer.setShowAxes(false);
		multipleRenderer.setPanEnabled(false);

		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			multipleRenderer.setBarWidth(dpToPx(5));
			multipleRenderer.setLabelsTextSize(dpToPx(9));
		} else {
			multipleRenderer.setBarWidth(dpToPx(20));
			multipleRenderer.setLabelsTextSize(dpToPx(12));
			mGraphScroll.setGraphScrollViewListener(mGraphScrollViewListenerYear);

			if (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300
					&& getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C410) {
				mTableLayoutMinMaxContainer.setVisibility(View.VISIBLE);
			}
			else{
				mTableLayoutMinMaxContainer.setVisibility(View.GONE);
			}
		}

		mRangeBarChart = new RangeBarChart(multipleDataset, multipleRenderer, BarChart.Type.DEFAULT);
		GraphicalView graphView = new GraphicalView(getActivity(), mRangeBarChart);

		mHeartRatePlotContainer.removeAllViews();
		mHeartRatePlotContainer.addView(graphView);

		Calendar calendarFrom = Calendar.getInstance();
		Calendar calendarTo = Calendar.getInstance();

		calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
		calendarFrom.set(Calendar.MONTH, 0);
		calendarFrom.set(Calendar.YEAR, year);

		calendarTo.set(Calendar.DAY_OF_MONTH, 31);
		calendarTo.set(Calendar.MONTH, 11);
		calendarTo.set(Calendar.YEAR, year);

		initializeStatsPort(calendarFrom.getTime(), calendarTo.getTime());
		showProgress(false);
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
			mAverageBPMValue.setText(String.valueOf(averageBPM));
			float percent = percentForAverageBPM(averageBPM);
			int leftMargin = (int) (dpToPx(150) * percent) - ((int)dpToPx(40) / 2);
			mIntensityTag.setText(mDecimalFormat2.format(percent * 100.0f) + "%");

			if(leftMargin < 0)
				leftMargin = 0;

			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			params.leftMargin = leftMargin;
			mIntensityTag.setLayoutParams(params);

			if(percent * 100 == 0) {
				mIntensityTag.setBackgroundResource(R.drawable.dash_2_6tagm);
			} else if(percent * 100 > 0 && percent * 100 < 25) {
				mIntensityTag.setBackgroundResource(R.drawable.dash_2_1tagvl);
			} else if(percent * 100 > 25 && percent * 100 < 50) {
				mIntensityTag.setBackgroundResource(R.drawable.dash_2_2tagl);
			} else if(percent * 100 > 50 && percent * 100 < 75) {
				mIntensityTag.setBackgroundResource(R.drawable.dash_2_3tagm);
			} else if(percent * 100 > 75 && percent * 100 < 100) {
				mIntensityTag.setBackgroundResource(R.drawable.dash_2_4tagh);
			} else if(percent * 100 >= 100) {
				mIntensityTag.setBackgroundResource(R.drawable.dash_2_5tagvh);
			}
		}
	}

	private void initializeStatsPort(final Date from, final Date to) {
		final Calendar calendarFrom = Calendar.getInstance();

		calendarFrom.setTime(from);

		String query = "select sum(averageHR), count(averageHR) from StatisticalDataPoint dataPoint " +
				"inner join StatisticalDataHeader dataHeader on dataPoint.dataHeaderAndPoint = dataHeader._id " +
				"where watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ? and averageHR > 0";

		float averageHRSum = 0;
		float averageHRCount = 0;

		while(calendarFrom.getTime().before(to)) {
			int day = calendarFrom.get(Calendar.DAY_OF_MONTH);
			int month = calendarFrom.get(Calendar.MONTH) + 1;
			int year = calendarFrom.get(Calendar.YEAR) - 1900;

			Cursor cursor = DataSource.getInstance(getActivity())
					.getReadOperation()
					.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
							String.valueOf(day), String.valueOf(month), String.valueOf(year));

			if(cursor.moveToNext()) {
				averageHRSum += cursor.getFloat(0);
				averageHRCount += cursor.getFloat(1);
			}

			cursor.close();

			calendarFrom.add(Calendar.DAY_OF_MONTH, 1);
		}

		final float averageHRSum01 = averageHRSum;
		final float averageHRCount01 = averageHRCount;

		float averageBPM = averageHRSum01 / averageHRCount01;
		mAverageBPMValue.setText(String.valueOf(Math.round(averageBPM)));
		float percent = percentForAverageBPM((int)averageBPM);
		int leftMargin = (int) (dpToPx(150) * percent) - ((int)dpToPx(40) / 2);

		mIntensityTag.setText(mDecimalFormat2.format(percent * 100.0f) + "%");

		if(leftMargin < 0)
			leftMargin = 0;

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		params.leftMargin = leftMargin;
		mIntensityTag.setLayoutParams(params);

		if(percent * 100 == 0) {
			mIntensityTag.setBackgroundResource(R.drawable.dash_2_6tagm);
		} else if(percent * 100 > 0 && percent * 100 < 25) {
			mIntensityTag.setBackgroundResource(R.drawable.dash_2_1tagvl);
		} else if(percent * 100 > 25 && percent * 100 < 50) {
			mIntensityTag.setBackgroundResource(R.drawable.dash_2_2tagl);
		} else if(percent * 100 > 50 && percent * 100 < 75) {
			mIntensityTag.setBackgroundResource(R.drawable.dash_2_3tagm);
		} else if(percent * 100 > 75 && percent * 100 < 100) {
			mIntensityTag.setBackgroundResource(R.drawable.dash_2_4tagh);
		} else if(percent * 100 >= 100) {
			mIntensityTag.setBackgroundResource(R.drawable.dash_2_5tagvh);
		}

		if (orientation() == Configuration.ORIENTATION_PORTRAIT) {
			// Return layout
			mHeartRateTopData.setVisibility(View.VISIBLE);
			mHeartRatePlotContainer.setVisibility(View.VISIBLE);
			mHeartRateLoadingText.setVisibility(View.GONE);

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

	private final RadioGroup.OnCheckedChangeListener mOnCheckChangedListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
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
			MainActivity mainActivity = (MainActivity) getActivity();

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

		@Override
		public void onNothingSelected(AdapterView<?> parent) { }
	};

	public int getCalendarMode() {
		if(orientation() == Configuration.ORIENTATION_PORTRAIT) {
			if(mToDateGroup != null) {
				switch(mToDateGroup.getCheckedRadioButtonId()) {
				case R.id.radDay:
					return MODE_DAY;
				case R.id.radWeek:
					return MODE_WEEK;
				case R.id.radMonth:
					return MODE_MONTH;
				case R.id.radYear:
					return MODE_YEAR;
				}
			}
		} else {
			if(mCalendarModeSpinner != null) {
				switch(mCalendarModeSpinner.getSelectedItemPosition()) {
				case 0:
					return MODE_DAY;
				case 1:
					return MODE_WEEK;
				case 2:
					return MODE_MONTH;
				case 3:
					return MODE_YEAR;
				}
			}
		}
		return MODE_DAY;
	}

	private final GraphScrollView.GraphScrollViewListener mGraphScrollViewListener = new GraphScrollView.GraphScrollViewListener() {
		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {
			View leftView = getView().findViewById(R.id.viewGraphLeftPadding);

			if(getCalendarMode() == MODE_DAY) {
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

						if(leftMargin < 0)
							leftMargin = 0;

						FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
						params.leftMargin = leftMargin;
						mIntensityTag.setLayoutParams(params);

						if(percent * 100 == 0) {
							mIntensityTag.setBackgroundResource(R.drawable.dash_2_6tagm);
						} else if(percent * 100 > 0 && percent * 100 < 25) {
							mIntensityTag.setBackgroundResource(R.drawable.dash_2_1tagvl);
						} else if(percent * 100 > 25 && percent * 100 < 50) {
							mIntensityTag.setBackgroundResource(R.drawable.dash_2_2tagl);
						} else if(percent * 100 > 50 && percent * 100 < 75) {
							mIntensityTag.setBackgroundResource(R.drawable.dash_2_3tagm);
						} else if(percent * 100 > 75 && percent * 100 < 100) {
							mIntensityTag.setBackgroundResource(R.drawable.dash_2_4tagh);
						} else if(percent * 100 >= 100) {
							mIntensityTag.setBackgroundResource(R.drawable.dash_2_5tagvh);
						}

						mAverageBPMValue.setText(String.valueOf(mHeartRateValues.get(index).intValue()));
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
		}
	};

	private final GraphScrollView.GraphScrollViewListener mGraphScrollViewListenerWeekAndMonth = new GraphScrollView.GraphScrollViewListener() {
		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {
			if(mRangeBarChart == null || mRangeBarChart.getDataset() == null || mRangeBarChart.getRenderer() == null)
				return;

			View leftView = getView().findViewById(R.id.viewGraphLeftPadding);

			try {
				for(int i=0;i<mCategorySeries.getItemCount();i++) {
					double[] screenPoint = mRangeBarChart.toScreenPoint(new double[] {i, i});
					int halfBarWidth =  (int)(mRangeBarChart.getRenderer().getBarWidth() / 2);
					int playHeadPositionX = (mPlayhead.getLeft() + (mPlayhead.getMeasuredWidth() / 2));
					int currentPointX = ((int)(leftView.getMeasuredWidth() + screenPoint[0])) - l + (int)mRangeBarChart.getRenderer().getBarSpacing();

					if(playHeadPositionX >= currentPointX - halfBarWidth && playHeadPositionX <= currentPointX + halfBarWidth) {
						double[] realPoint = mRangeBarChart.toRealPoint((float)screenPoint[0], 0);
						int index = 0;

						if(realPoint[0] < 0) {
							index = 0;
						} else {
							index = (int)Math.round(realPoint[0]);
						}

						if(index >= mCategorySeries.getItemCount()) {
							index = mCategorySeries.getItemCount() - 1;
						}

						if(index > 0) {
							index = index - 1;
						}

						int minValue = (int) mCategorySeries.getMinimumValue(index);
						int maxValue = (int) mCategorySeries.getMaximumValue(index);

						mMaxBPM.setText(String.valueOf(maxValue));
						mMinBPM.setText(String.valueOf(minValue));

						if(index >= 0 && index < mAverageHRs.size()) {
							int averageBPM = Math.round(mAverageHRs.get(index).floatValue());
							mAverageBPMValue.setText(String.valueOf(averageBPM));

							float percent = percentForAverageBPM(averageBPM);
							int leftMargin = (int) (dpToPx(150) * percent) - ((int)dpToPx(40) / 2);
							mIntensityTag.setText(mDecimalFormat2.format(percent * 100.0f) + "%");

							if(leftMargin < 0)
								leftMargin = 0;

							FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
							params.leftMargin = leftMargin;
							mIntensityTag.setLayoutParams(params);

							if(percent * 100 == 0) {
								mIntensityTag.setBackgroundResource(R.drawable.dash_2_6tagm);
							} else if(percent * 100 > 0 && percent * 100 < 25) {
								mIntensityTag.setBackgroundResource(R.drawable.dash_2_1tagvl);
							} else if(percent * 100 > 25 && percent * 100 < 50) {
								mIntensityTag.setBackgroundResource(R.drawable.dash_2_2tagl);
							} else if(percent * 100 > 50 && percent * 100 < 75) {
								mIntensityTag.setBackgroundResource(R.drawable.dash_2_3tagm);
							} else if(percent * 100 > 75 && percent * 100 < 100) {
								mIntensityTag.setBackgroundResource(R.drawable.dash_2_4tagh);
							} else if(percent * 100 >= 100) {
								mIntensityTag.setBackgroundResource(R.drawable.dash_2_5tagvh);
							}
						} else {
							mAverageBPMValue.setText("0");

							FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
							params.leftMargin = 0;
							mIntensityTag.setText("0%");
							mIntensityTag.setLayoutParams(params);
							mIntensityTag.setBackgroundResource(R.drawable.dash_2_6tagm);
						}
						break;
					} else {
						mMaxBPM.setText("0");
						mMinBPM.setText("0");
						mAverageBPMValue.setText("0");

						FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
						params.leftMargin = 0;
						mIntensityTag.setText("0%");
						mIntensityTag.setLayoutParams(params);
						mIntensityTag.setBackgroundResource(R.drawable.dash_2_6tagm);
					}
				}
			} catch(NullPointerException e) {
				e.printStackTrace();
			}
		}
	};

	private final GraphScrollView.GraphScrollViewListener mGraphScrollViewListenerYear = new GraphScrollView.GraphScrollViewListener() {
		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {
			if(mRangeBarChart == null || mRangeBarChart.getDataset() == null || mRangeBarChart.getRenderer() == null)
				return;

			View leftView = getView().findViewById(R.id.viewGraphLeftPadding);

			try {
				for(int i=0;i<=mCategorySeries.getItemCount();i++) {
					double[] screenPoint = mRangeBarChart.toScreenPoint(new double[] {i+1, 0});

					int halfBarWidth =  (int)(mRangeBarChart.getRenderer().getBarWidth() / 2);
					int playHeadPositionX = (mPlayhead.getLeft() + (mPlayhead.getMeasuredWidth() / 2));
					int currentPointX = ((int)(leftView.getMeasuredWidth() + screenPoint[0])) - l + (int)mRangeBarChart.getRenderer().getBarSpacing();

					if(playHeadPositionX >= currentPointX - halfBarWidth && playHeadPositionX <= currentPointX + halfBarWidth) {
						/*double[] realPoint = mRangeBarChart.toRealPoint((float)screenPoint[0], 0);
						int index = 0;

						if(realPoint[0] < 0) {
							index = 0;
						} else {
							index = (int)Math.round(realPoint[0]);
						}

						if(index >= mCategorySeries.getItemCount()) {
							index = mCategorySeries.getItemCount() - 1;
						}*/

						int index = i;

						if(index >= mCategorySeries.getItemCount()) {
							return;
						}

						int minValue = (int) mCategorySeries.getMinimumValue(index);
						int maxValue = (int) mCategorySeries.getMaximumValue(index);

						mMaxBPM.setText(String.valueOf(maxValue));
						mMinBPM.setText(String.valueOf(minValue));

						if(index >= 0 && index < mAverageHRs.size()) {
							int averageBPM = Math.round(mAverageHRs.get(index).floatValue());
							mAverageBPMValue.setText(String.valueOf(averageBPM));

							float percent = percentForAverageBPM(averageBPM);
							int leftMargin = (int) (dpToPx(150) * percent) - ((int)dpToPx(40) / 2);
							mIntensityTag.setText(mDecimalFormat2.format(percent * 100.0f) + "%");

							if(leftMargin < 0)
								leftMargin = 0;

							FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
							params.leftMargin = leftMargin;
							mIntensityTag.setLayoutParams(params);

							if(percent * 100 == 0) {
								mIntensityTag.setBackgroundResource(R.drawable.dash_2_6tagm);
							} else if(percent * 100 > 0 && percent * 100 < 25) {
								mIntensityTag.setBackgroundResource(R.drawable.dash_2_1tagvl);
							} else if(percent * 100 > 25 && percent * 100 < 50) {
								mIntensityTag.setBackgroundResource(R.drawable.dash_2_2tagl);
							} else if(percent * 100 > 50 && percent * 100 < 75) {
								mIntensityTag.setBackgroundResource(R.drawable.dash_2_3tagm);
							} else if(percent * 100 > 75 && percent * 100 < 100) {
								mIntensityTag.setBackgroundResource(R.drawable.dash_2_4tagh);
							} else if(percent * 100 >= 100) {
								mIntensityTag.setBackgroundResource(R.drawable.dash_2_5tagvh);
							}
						} else {
							mAverageBPMValue.setText("0");

							FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
							params.leftMargin = 0;
							mIntensityTag.setText("0%");
							mIntensityTag.setLayoutParams(params);
							mIntensityTag.setBackgroundResource(R.drawable.dash_2_6tagm);
						}
						break;
					} else {
						mMaxBPM.setText("0");
						mMinBPM.setText("0");
						mAverageBPMValue.setText("0");

						FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
						params.leftMargin = 0;
						mIntensityTag.setText("0%");
						mIntensityTag.setLayoutParams(params);
						mIntensityTag.setBackgroundResource(R.drawable.dash_2_6tagm);
					}
				}
			} catch(NullPointerException e) {
				e.printStackTrace();
			}
		}
	};

	private void scrollToXValue(double x) {
		if(!isAdded())
			return;

		View leftView = getView().findViewById(R.id.viewGraphLeftPadding);

		if(leftView != null) {
			double[] screenPoint = mBarChart.toScreenPoint(new double[] {x, 0});
			int scrollPositionX = leftView.getMeasuredWidth() + (int)screenPoint[0] - mPlayhead.getRight();
			mGraphScroll.smoothScrollTo(scrollPositionX, 0);
		}
	}

	private void scrollToXValue2(double x, int count) {
		if(!isAdded())
			return;

		View leftView = getView().findViewById(R.id.viewGraphLeftPadding);

		if(leftView != null) {
			double[] screenPoint = mRangeBarChart.toScreenPoint(new double[] {x, 0});
			int scrollPositionX = leftView.getMeasuredWidth() + (int)screenPoint[0] - mPlayhead.getRight();
			int barWidth = (int)mRangeBarChart.getRenderer().getBarWidth();
			int barSpacing = (int)mRangeBarChart.getRenderer().getBarSpacing();
			scrollPositionX = ((int)x * (barWidth + barSpacing) - mPlayhead.getLeft() - leftView.getMeasuredWidth()) + 300;
			mGraphScroll.smoothScrollTo(scrollPositionX, 0);
			//			int barWidth = (int)mRangeBarChart.getRenderer().getBarWidth();
			//			//int scrollPositionX = ((int)x) * (mHeartRatePlotContainer.getMeasuredWidth() / count) + barWidth;
			//			int scrollPositionX = (((int)x + 2) * ((mHeartRatePlotContainer.getMeasuredWidth() / count))) + barWidth;
			//			mGraphScroll.smoothScrollTo(scrollPositionX, 0);
		}
	}



	private void scrollToXValueMonth(double x) {
		if(!isAdded())
			return;

		View leftView = getView().findViewById(R.id.viewGraphLeftPadding);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getLifeTrakApplication().getCurrentDate());
		int noOfDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

		if (leftView != null) {
			//			int barWidth = (int)mRangeBarChart.getRenderer().getBarWidth();
			//			int scrollPositionX = ((int)x * (barWidth + mHeartRatePlotContainer.getMeasuredWidth() / noOfDays) - mPlayhead.getLeft() - leftView.getMeasuredWidth()) + 300;
			//			mGraphScroll.smoothScrollTo(scrollPositionX + mPlayhead.getLeft(), 0);
			int barWidth = (int)mRangeBarChart.getRenderer().getBarWidth();
			int scrollPositionX = ((int)x) * (mHeartRatePlotContainer.getMeasuredWidth() / noOfDays) + barWidth;
			//int scrollPositionX = (((int)x + 1) * (mHeartRatePlotContainer.getMeasuredWidth() / noOfDays)) - barWidth;
			mGraphScroll.smoothScrollTo(scrollPositionX, 0);
		}
	}

    private void scrollToXValueMonth2(int x) {
        if(!isAdded())
            return;

        View leftView = getView().findViewById(R.id.viewGraphLeftPadding);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getLifeTrakApplication().getCurrentDate());
        int noOfDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        if (leftView != null) {
            //			int barWidth = (int)mRangeBarChart.getRenderer().getBarWidth();
            //			int scrollPositionX = ((int)x * (barWidth + mHeartRatePlotContainer.getMeasuredWidth() / noOfDays) - mPlayhead.getLeft() - leftView.getMeasuredWidth()) + 300;
            //			mGraphScroll.smoothScrollTo(scrollPositionX + mPlayhead.getLeft(), 0);
            int barWidth = (int)mRangeBarChart.getRenderer().getBarWidth();
            int scrollPositionX = (x * (mHeartRatePlotContainer.getMeasuredWidth() / noOfDays)) +  (barWidth * x);
            //int scrollPositionX = (((int)x + 1) * (mHeartRatePlotContainer.getMeasuredWidth() / noOfDays)) - barWidth;
            mGraphScroll.smoothScrollTo(scrollPositionX, 0);
        }
    }

	private void scrollToXValueYear(double x) {
		if(!isAdded())
			return;

		View leftView = getView().findViewById(R.id.viewGraphLeftPadding);

		if (leftView != null) {
			int barWidth = (int)mRangeBarChart.getRenderer().getBarWidth();
			int scrollPositionX = (((int)x + 1) * (mHeartRatePlotContainer.getMeasuredWidth() / 12)) - barWidth;
			mGraphScroll.smoothScrollTo(scrollPositionX, 0);
		}
		mYearScrollPosition = 0;
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
			mToDateGroup.setOnCheckedChangeListener(mOnCheckChangedListener);
		}
	}

	public FrameLayout getmHeartRatePlotContainer() {
		return mHeartRatePlotContainer;
	}

	public FrameLayout getmHeartRateLoadingText() {
		return mHeartRateLoadingText;
	}

	public RelativeLayout getmHeartRateTopData() {
		return mHeartRateTopData;
	}

}