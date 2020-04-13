package com.salutron.lifetrakwatchapp.fragment;

import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.content.res.Configuration;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import org.achartengine.GraphicalView;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.chart.BarChart;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.LightDataPoint;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.view.GraphScrollView;

public class LightPlotFragment extends BaseFragment {
	private FrameLayout mLightPlotContainer;
	private Date mDate;
	private int mPosition;
	
	private XYSeriesRenderer mAllLuxRenderer = new XYSeriesRenderer();
	private XYSeriesRenderer mBlueLuxRenderer = new XYSeriesRenderer();
	private XYSeriesRenderer mAllLuxWristOffRenderer = new XYSeriesRenderer();
	private XYSeriesRenderer mBlueLuxWristOffRenderer = new XYSeriesRenderer();
	
	private XYSeries mAllLuxSeries = new XYSeries("All Lux");
	private XYSeries mBlueLuxSeries = new XYSeries("Blue Lux");
	private XYSeries mAllLuxWristOffSeries = new XYSeries("All Lux Wrist Off");
	private XYSeries mBlueLuxWristOffSeries = new XYSeries("Blue Lux Wrist Off");
	
	private BarChart mBarChart;
	private GraphicalView mGraphView;
	
	private TextView mLightExposureHour;
	private TextView mLightExposureMinute;
	private TextView mAllLuxValue;
	private TextView mBlueLuxValue;
	private TextView mDateView;
	private GraphScrollView mGraphScrollView;
	private ImageView mPlayheadImage;
	private View mLeftView;
	private Spinner mCalendarModeSpinner;
	private TextView mTimeStart;
	private TextView mTimeEnd;
	
	private List<LightPlotData> mLightPlotDataList;
	private List<LightPlotDataRangeValue> mLightPlotDataRangeList;
	private SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	
	private final String[] mHours = {"12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM", "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM", "8PM", "9PM", "10PM", "11PM"};
	
	private final int ALL_LIGHT_THRESHOLD_01 = 10;
	private final int ALL_LIGHT_THRESHOLD_02 = 4000;
	private final int ALL_LIGHT_THRESHOLD_03 = 6000;
	private final int ALL_LIGHT_THRESHOLD_04 = 8000;
	private final int ALL_LIGHT_THRESHOLD_05 = 10000;
	private final int BLUE_LIGHT_THRESHOLD_01 = 3;
	private final int BLUE_LIGHT_THRESHOLD_02 = 800;
	private final int BLUE_LIGHT_THRESHOLD_03 = 1200;
	private final int BLUE_LIGHT_THRESHOLD_04 = 1600;
	private final int BLUE_LIGHT_THRESHOLD_05 = 2000;
	
	private final int LIGHT_TYPE_BLUE = 0x01;
	private final int LIGHT_TYPE_ALL = 0x02;
	private final int LIGHT_TYPE_WRIST_OFF = 0x03;
	
	private final int THRESHOLD_LEVEL_01 = 0x03;
	private final int THRESHOLD_LEVEL_02 = 0x04;
	private final int THRESHOLD_LEVEL_03 = 0x05;
	private final int THRESHOLD_LEVEL_04 = 0x06;
	private final int THRESHOLD_LEVEL_05 = 0x07;
	private final int THRESHOLD_LEVEL_06 = 0x08;
	
	private List<StatisticalDataHeader> mDataHeaders = new ArrayList<StatisticalDataHeader>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_light_plot, null);
		
		initializeViews(view);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			mDate = new Date(savedInstanceState.getLong(DATE));
		}

		initializeObjects();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		LayoutInflater inflater = LayoutInflater.from(getActivity());

		switch (newConfig.orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			changeFramentView(inflater, R.layout.fragment_light_plot, (ViewGroup) getView());
			showActionBarAndCalendar();
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			changeFramentView(inflater, R.layout.fragment_light_plot_land, (ViewGroup) getView());
			hideActionBarAndCalendar();
			break;
		}

		initializeViews(getView());
		setDataForDay(mDate);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mDate != null) {
			outState.putLong(DATE, mDate.getTime());
		}
	}

	private void initializeViews(View view) {
		if (getArguments() != null)
			mPosition = getArguments().getInt(POSITION);
		
		mLightPlotContainer = (FrameLayout) view.findViewById(R.id.chartLightPlot);
		mLightExposureHour = (TextView) view.findViewById(R.id.textViewHour);
		mLightExposureMinute = (TextView) view.findViewById(R.id.textViewMin);
		mGraphScrollView = (GraphScrollView) view.findViewById(R.id.hsvGraphScroll);
		mPlayheadImage = (ImageView) view.findViewById(R.id.imgPlayhead);
		mLeftView = view.findViewById(R.id.viewGraphLeftPadding);
		mAllLuxValue = (TextView) view.findViewById(R.id.textViewAllLight);
		mBlueLuxValue = (TextView) view.findViewById(R.id.textViewBlueRichLight);
		mDateView = (TextView) view.findViewById(R.id.textViewDate);
		mCalendarModeSpinner = (Spinner) view.findViewById(R.id.spnCalendarMode);
		mTimeStart = (TextView) view.findViewById(R.id.tvwTimeStart);
		mTimeEnd = (TextView) view.findViewById(R.id.tvwTimeEnd);
		
		mDateFormat.applyPattern("MMMM dd, yyyy");
		
		if (mCalendarModeSpinner != null) {
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.calendar_mode_spinner, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mCalendarModeSpinner.setAdapter(adapter);
			mCalendarModeSpinner.setOnItemSelectedListener(mOnItemSelectedListener);
		}
		
		if (mTimeStart != null && mTimeEnd != null) {
			switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				mTimeStart.setText("12" + getString(R.string.am));
				mTimeEnd.setText("11:59" + getString(R.string.pm));
				break;
			case TIME_FORMAT_24_HR:
				mTimeStart.setText("00:00");
				mTimeEnd.setText("24:00");
				break;
			}
		}
	}

	private void initializeObjects() {
		Date date = new Date();

		if (getArguments() != null) {
			date = new Date(getArguments().getLong(DATE));
		}

		if (mDate != null)
			date = mDate;

		LifeTrakLogger.configure();

		setDataForDay(date);

		if (orientation() == Configuration.ORIENTATION_PORTRAIT) {
			showActionBarAndCalendar();
		} else {
			hideActionBarAndCalendar();
		}
	}

	public void setDataForDay(Date date) {
		if(!isAdded())
			return;
		
		if (mDateView != null)
			mDateView.setText(mDateFormat.format(getLifeTrakApplication().getCurrentDate()));
		
		mLightPlotDataList = createValuesForDay(date);
		LightPlotData maxLightPlotData = maxLightPlotData(mLightPlotDataList);
		
		XYMultipleSeriesRenderer multipleSeriesRenderer = new XYMultipleSeriesRenderer();
		XYMultipleSeriesDataset multipleSeriesDataset = new XYMultipleSeriesDataset();
		
		multipleSeriesRenderer.removeSeriesRenderer(allLuxRenderer());
		multipleSeriesRenderer.addSeriesRenderer(allLuxRenderer());
		multipleSeriesRenderer.removeSeriesRenderer(blueLuxRenderer());
		multipleSeriesRenderer.addSeriesRenderer(blueLuxRenderer());
		multipleSeriesRenderer.removeSeriesRenderer(allLuxWristOffRenderer());
		multipleSeriesRenderer.addSeriesRenderer(allLuxWristOffRenderer());
		multipleSeriesRenderer.removeSeriesRenderer(blueLuxWristOffRenderer());
		multipleSeriesRenderer.addSeriesRenderer(blueLuxWristOffRenderer());
		
		int index = 0;
		
		mAllLuxSeries.clear();
		mBlueLuxSeries.clear();
		mAllLuxWristOffSeries.clear();
		mBlueLuxWristOffSeries.clear();
		
		for (LightPlotData lightPlotData : mLightPlotDataList) {
			double allLuxValue = getYWithMaxValue(lightPlotData.allLuxLog, maxLightPlotData.allLuxLog, LIGHT_PLOT_MIN_Y, LIGHT_PLOT_MAX_Y);
			double blueLuxValue = getYWithMaxValue(lightPlotData.blueLuxLog, maxLightPlotData.allLuxLog, LIGHT_PLOT_MIN_Y, LIGHT_PLOT_MAX_Y);
			
			if (lightPlotData.isWristOff) {
				addAllLuxWristOff(index, allLuxValue);
				addBlueLuxWristOff(index, blueLuxValue);
			} else {
				addAllLux(index, allLuxValue);
				addBlueLux(index, blueLuxValue);
			}
			
			index++;
		}
		
		multipleSeriesDataset.removeSeries(mAllLuxSeries);
		multipleSeriesDataset.addSeries(mAllLuxSeries);
		multipleSeriesDataset.removeSeries(mBlueLuxSeries);
		multipleSeriesDataset.addSeries(mBlueLuxSeries);
		multipleSeriesDataset.removeSeries(mAllLuxWristOffSeries);
		multipleSeriesDataset.addSeries(mAllLuxWristOffSeries);
		multipleSeriesDataset.removeSeries(mBlueLuxWristOffSeries);
		multipleSeriesDataset.addSeries(mBlueLuxWristOffSeries);
		
		switch (orientation()) {
		case Configuration.ORIENTATION_PORTRAIT:
			multipleSeriesRenderer.setBarWidth(dpToPx(3f));
			multipleSeriesRenderer.setMargins(new int[] {0, (int)dpToPx(5), 0, (int)dpToPx(5)});
			multipleSeriesRenderer.setXAxisMin(-1);
			multipleSeriesRenderer.setXAxisMax(144);
			
			switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				multipleSeriesRenderer.addXTextLabel(3, getString(R.string.am));
				multipleSeriesRenderer.addXTextLabel(140, getString(R.string.pm));
				break;
			case TIME_FORMAT_24_HR:
				multipleSeriesRenderer.addXTextLabel(3, "0:00");
				multipleSeriesRenderer.addXTextLabel(140, "23:59");
				break;
			}
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			multipleSeriesRenderer.setBarWidth(dpToPx(10f));
			multipleSeriesRenderer.setMargins(new int[] {0, 0, 0, 0});
			multipleSeriesRenderer.setXAxisMin(-5);
			multipleSeriesRenderer.setXAxisMax(144);
			
			index = 0;
			
			for (int i=0;i<144;i++) {
				if (i % 6 == 0) {
					switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
					case TIME_FORMAT_12_HR:
						multipleSeriesRenderer.addXTextLabel(i, mHours[index]);
						break;
					case TIME_FORMAT_24_HR:
						multipleSeriesRenderer.addXTextLabel(i, String.format("%02d:00", index));
						break;
					}
					index++;
				}
			}
			
			resizeGraphContainer((int)dpToPx(1500));
			mGraphScrollView.setGraphScrollViewListener(mGraphScrollViewListener);
			getView().findViewById(R.id.relLeftTimeContainer).setVisibility(View.GONE);
			mAllLuxValue.setVisibility(View.VISIBLE);
			mBlueLuxValue.setVisibility(View.VISIBLE);
			
			break;
		}
		
		multipleSeriesRenderer.setYLabels(0);
		multipleSeriesRenderer.setXLabels(0);
		multipleSeriesRenderer.setShowGrid(false);
		multipleSeriesRenderer.setShowTickMarks(false);
		multipleSeriesRenderer.setShowLegend(false);
		multipleSeriesRenderer.setShowAxes(false);
		multipleSeriesRenderer.setZoomEnabled(false, false);
		multipleSeriesRenderer.setXLabelsAlign(Align.CENTER);
		multipleSeriesRenderer.setYAxisMin(LIGHT_PLOT_MIN_Y);
		multipleSeriesRenderer.setYAxisMax(LIGHT_PLOT_MAX_Y);
		multipleSeriesRenderer.setApplyBackgroundColor(true);
		multipleSeriesRenderer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		multipleSeriesRenderer.setMarginsColor(getResources().getColor(R.color.color_xaxis_bar));
		multipleSeriesRenderer.setXLabelsColor(getResources().getColor(R.color.color_black_text));
		multipleSeriesRenderer.setLabelsTextSize(dpToPx(12));
		multipleSeriesRenderer.setPanEnabled(false);
		
		mBarChart = new BarChart(multipleSeriesDataset, multipleSeriesRenderer, BarChart.Type.STACKED);
		mGraphView = new GraphicalView(getActivity(), mBarChart);
		mLightPlotContainer.removeAllViews();
		mLightPlotContainer.addView(mGraphView);
	}
	
	public void setDataWithDateRange(Date from, Date to) {
		if(!isAdded())
			return;
		
		mDataHeaders.clear();
		
		XYMultipleSeriesRenderer multipleSeriesRenderer = new XYMultipleSeriesRenderer();
		XYMultipleSeriesDataset multipleSeriesDataset = new XYMultipleSeriesDataset();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(from);
		
		int xIndex = 0;
		
		List<LightPlotBarItem> barItems = new ArrayList<LightPlotBarItem>();
		
		while (calendar.getTime().before(to) || calendar.getTime().equals(to)) {
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH) + 1;
			int year = calendar.get(Calendar.YEAR) - 1900;
			
			List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(getActivity())
																.getReadOperation()
																.query("watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?", 
																		String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(day), String.valueOf(month), String.valueOf(year))
																.limit(1)
																.getResults(StatisticalDataHeader.class);
			if (dataHeaders.size() > 0) {
				StatisticalDataHeader dataHeader = dataHeaders.get(0);
				
				List<LightDataPoint> lightDataPoints = DataSource.getInstance(getActivity())
																	.getReadOperation()
																	.query("dataHeaderAndPoint = ?", String.valueOf(dataHeader.getId()))
																	.orderBy("_id", SORT_ASC)
																	.getResults(LightDataPoint.class);
				int yIndex = 0;
				barItems.clear();
				
				for (LightDataPoint lightDataPoint : lightDataPoints) {
					LightPlotDataRangeValue lightPlotData = LightPlotDataRangeValue.buildLightPlotDataRangeValue(lightDataPoint, xIndex, yIndex);
					
					double allLuxValue = lightPlotData.allLux;
					double blueLuxValue = lightPlotData.blueLux;
					double x = xIndex;
					double y = getYWithMaxValue(yIndex, 144, LIGHT_PLOT_MIN_Y, LIGHT_PLOT_MAX_Y);
					
					if (allLuxValue > 0 && allLuxValue <= ALL_LIGHT_THRESHOLD_01) {
						if (lightPlotData.isWristOff) {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_01, x, y));
						} else {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_ALL, THRESHOLD_LEVEL_01, x, y));
						}
					} else if (allLuxValue > ALL_LIGHT_THRESHOLD_01 && allLuxValue <= ALL_LIGHT_THRESHOLD_02) {
						if (lightPlotData.isWristOff) {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_02, x, y));
						} else {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_ALL, THRESHOLD_LEVEL_02, x, y));
						}
					} else if (allLuxValue > ALL_LIGHT_THRESHOLD_02 && allLuxValue <= ALL_LIGHT_THRESHOLD_03) {
						if (lightPlotData.isWristOff) {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_03, x, y));
						} else {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_ALL, THRESHOLD_LEVEL_03, x, y));
						}
					} else if (allLuxValue > ALL_LIGHT_THRESHOLD_04 && allLuxValue <= ALL_LIGHT_THRESHOLD_04) {
						if (lightPlotData.isWristOff) {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_04, x, y));
						} else {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_ALL, THRESHOLD_LEVEL_04, x, y));
						}
					} else if (allLuxValue > ALL_LIGHT_THRESHOLD_04 && allLuxValue <= ALL_LIGHT_THRESHOLD_05) {
						if (lightPlotData.isWristOff) {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_05, x, y));
						} else {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_ALL, THRESHOLD_LEVEL_05, x, y));
						}
					} else {
						if (lightPlotData.isWristOff) {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_06, x, y));
						} else {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_ALL, THRESHOLD_LEVEL_06, x, y));
						}
					}
					
					if (blueLuxValue > 0 && blueLuxValue <= BLUE_LIGHT_THRESHOLD_01) {
						if (lightPlotData.isWristOff) {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_01, x + 0.2, y));
						} else {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_BLUE, THRESHOLD_LEVEL_01, x + 0.2, y));
						}
					} else if (blueLuxValue > BLUE_LIGHT_THRESHOLD_01 && blueLuxValue <= BLUE_LIGHT_THRESHOLD_02) {
						if (lightPlotData.isWristOff) {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_02, x + 0.2, y));
						} else {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_BLUE, THRESHOLD_LEVEL_02, x + 0.2, y));
						}
					} else if (blueLuxValue > BLUE_LIGHT_THRESHOLD_02 && blueLuxValue <= BLUE_LIGHT_THRESHOLD_03) {
						if (lightPlotData.isWristOff) {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_03, x + 0.2, y));
						} else {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_BLUE, THRESHOLD_LEVEL_03, x + 0.2, y));
						}
					} else if (blueLuxValue > BLUE_LIGHT_THRESHOLD_03 && blueLuxValue <= BLUE_LIGHT_THRESHOLD_04) {
						if (lightPlotData.isWristOff) {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_04, x + 0.2, y));
						} else {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_BLUE, THRESHOLD_LEVEL_04, x + 0.2, y));
						}
					} else if (blueLuxValue > BLUE_LIGHT_THRESHOLD_04 && blueLuxValue <= BLUE_LIGHT_THRESHOLD_05) {
						if (lightPlotData.isWristOff) {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_05, x + 0.2, y));
						} else {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_BLUE, THRESHOLD_LEVEL_05, x + 0.2, y));
						}
					} else {
						if (lightPlotData.isWristOff) {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_06, x + 0.2, y));
						} else {
							barItems.add(LightPlotBarItem.create(LIGHT_TYPE_BLUE, THRESHOLD_LEVEL_06, x + 0.2, y));
						}
					}
					
					yIndex++;
				}
				
				for (int i=barItems.size()-1;i>=0;i--) {
					addBarToGraph(barItems.get(i), multipleSeriesRenderer, multipleSeriesDataset);
				}
				
				dataHeader.setDateStamp(calendar.getTime());
				mDataHeaders.add(dataHeader);
			} else {
				LightPlotBarItem barItem = LightPlotBarItem.create(LIGHT_TYPE_WRIST_OFF, THRESHOLD_LEVEL_01, xIndex);
				addBarToGraph(barItem, multipleSeriesRenderer, multipleSeriesDataset);
				
				StatisticalDataHeader dataHeader = new StatisticalDataHeader();
				dataHeader.setDateStamp(calendar.getTime());
				mDataHeaders.add(dataHeader);
			}
			
			if (getCalendarMode() == MODE_YEAR) {
				multipleSeriesRenderer.addXTextLabel(xIndex, String.valueOf(calendar.get(Calendar.DAY_OF_YEAR)));
			} else {
				multipleSeriesRenderer.addXTextLabel(xIndex, String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
			}
			
			xIndex++;
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		multipleSeriesRenderer.setYLabels(0);
		multipleSeriesRenderer.setXLabels(0);
		multipleSeriesRenderer.setShowGrid(false);
		multipleSeriesRenderer.setShowTickMarks(false);
		multipleSeriesRenderer.setShowLegend(false);
		multipleSeriesRenderer.setShowAxes(false);
		multipleSeriesRenderer.setZoomEnabled(false, false);
		multipleSeriesRenderer.setXLabelsAlign(Align.CENTER);
		multipleSeriesRenderer.setYAxisMin(LIGHT_PLOT_MIN_Y);
		multipleSeriesRenderer.setYAxisMax(LIGHT_PLOT_MAX_Y);
		multipleSeriesRenderer.setXAxisMin(-1);
		multipleSeriesRenderer.setXAxisMax(xIndex);
		multipleSeriesRenderer.setApplyBackgroundColor(true);
		multipleSeriesRenderer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		multipleSeriesRenderer.setMarginsColor(getResources().getColor(R.color.color_xaxis_bar));
		multipleSeriesRenderer.setXLabelsColor(getResources().getColor(R.color.color_black_text));
		multipleSeriesRenderer.setLabelsTextSize(dpToPx(12));
		multipleSeriesRenderer.setPanEnabled(false);
		multipleSeriesRenderer.setMargins(new int[] {0, 0, 0, 0});
		
		switch (getCalendarMode()) {
		case MODE_WEEK:
			multipleSeriesRenderer.setBarWidth(dpToPx(15));
			resizeGraphContainer((int)dpToPx(650));
			break;
		case MODE_MONTH:
			multipleSeriesRenderer.setBarWidth(dpToPx(15));
			resizeGraphContainer((int)dpToPx(2500));
			break;
		case MODE_YEAR:
			multipleSeriesRenderer.setBarWidth(dpToPx(8));
			resizeGraphContainer((int)dpToPx(15000));
			break;
		}
		
		getView().findViewById(R.id.relLeftTimeContainer).setVisibility(View.VISIBLE);
		mAllLuxValue.setVisibility(View.GONE);
		mBlueLuxValue.setVisibility(View.GONE);
		
		mBarChart = new BarChart(multipleSeriesDataset, multipleSeriesRenderer, BarChart.Type.STACKED);
		mGraphView = new GraphicalView(getActivity(), mBarChart);
		mLightPlotContainer.removeAllViews();
		mLightPlotContainer.addView(mGraphView);
		
		mGraphScrollView.setGraphScrollViewListener(mGraphScrollViewListener2);
	}
	
	private static class LightPlotBarItem {
		public int lightType;
		public int thresholdLevel;
		public double x;
		public double y;
		
		public static final LightPlotBarItem create(int lightType, int thresholdLevel, double x, double y) {
			LightPlotBarItem lightPlotBarItem = new LightPlotBarItem();
			
			lightPlotBarItem.lightType = lightType;
			lightPlotBarItem.thresholdLevel = thresholdLevel;
			lightPlotBarItem.x = x;
			lightPlotBarItem.y = y;
			
			return lightPlotBarItem;
		}
		
		public static final LightPlotBarItem create(int lightType, int thresholdLevel, double x) {
			LightPlotBarItem lightPlotBarItem = new LightPlotBarItem();
			
			lightPlotBarItem.lightType = lightType;
			lightPlotBarItem.thresholdLevel = thresholdLevel;
			lightPlotBarItem.x = x;
			lightPlotBarItem.y = 0;
			
			return lightPlotBarItem;
		}
	}
	
	private void addBarToGraph(LightPlotBarItem barItem, XYMultipleSeriesRenderer renderer, XYMultipleSeriesDataset dataset) {
		XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
		XYSeries xySeries = new XYSeries(String.valueOf(System.currentTimeMillis()));
		
		if (barItem.lightType == LIGHT_TYPE_ALL) {
			switch (barItem.thresholdLevel) {
			case THRESHOLD_LEVEL_01:
				xySeriesRenderer.setColor(getResources().getColor(R.color.color_all_light_line_01));
				break;
			case THRESHOLD_LEVEL_02:
				xySeriesRenderer.setColor(getResources().getColor(R.color.color_all_light_line_02));
				break;
			case THRESHOLD_LEVEL_03:
				xySeriesRenderer.setColor(getResources().getColor(R.color.color_all_light_line_03));
				break;
			case THRESHOLD_LEVEL_04:
				xySeriesRenderer.setColor(getResources().getColor(R.color.color_all_light_line_04));
				break;
			case THRESHOLD_LEVEL_05:
				xySeriesRenderer.setColor(getResources().getColor(R.color.color_all_light_line_05));
				break;
			case THRESHOLD_LEVEL_06:
				xySeriesRenderer.setColor(getResources().getColor(R.color.color_all_light_line_06));
				break;
			}
		} else if (barItem.lightType == LIGHT_TYPE_BLUE) { 
			switch (barItem.thresholdLevel) {
			case THRESHOLD_LEVEL_01:
				xySeriesRenderer.setColor(getResources().getColor(R.color.color_blue_light_line_01));
				break;
			case THRESHOLD_LEVEL_02:
				xySeriesRenderer.setColor(getResources().getColor(R.color.color_blue_light_line_02));
				break;
			case THRESHOLD_LEVEL_03:
				xySeriesRenderer.setColor(getResources().getColor(R.color.color_blue_light_line_03));
				break;
			case THRESHOLD_LEVEL_04:
				xySeriesRenderer.setColor(getResources().getColor(R.color.color_blue_light_line_04));
				break;
			case THRESHOLD_LEVEL_05:
				xySeriesRenderer.setColor(getResources().getColor(R.color.color_blue_light_line_05));
				break;
			case THRESHOLD_LEVEL_06:
				xySeriesRenderer.setColor(getResources().getColor(R.color.color_blue_light_line_06));
				break;
			}
		} else if (barItem.lightType == LIGHT_TYPE_WRIST_OFF) {
			xySeriesRenderer.setColor(getResources().getColor(R.color.color_light_plot_light_gray));
		}
		
		xySeries.add(barItem.x, barItem.y);
		
		renderer.addSeriesRenderer(xySeriesRenderer);
		dataset.addSeries(xySeries);
	}
	
	private List<LightPlotData> createValuesForDay(Date date) {
		List<LightPlotData> lightPlotDataList = new ArrayList<LightPlotData>();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;
		
		List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(getActivity())
															.getReadOperation()
															.query("watchDataHeader = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?", 
																	String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(day), String.valueOf(month), String.valueOf(year))
															.limit(1)
															.getResults(StatisticalDataHeader.class);
		
		if (dataHeaders.size() > 0) {
			StatisticalDataHeader dataHeader = dataHeaders.get(0);
			
			mLightExposureHour.setText(String.valueOf(dataHeader.getLightExposure() / 60));
			mLightExposureMinute.setText(String.format("%02d", dataHeader.getLightExposure() % 60));
			
			List<LightDataPoint> lightDataPoints = DataSource.getInstance(getActivity())
																.getReadOperation()
																.query("dataHeaderAndPoint = ?", String.valueOf(dataHeader.getId()))
																.getResults(LightDataPoint.class);
			
			for (LightDataPoint lightDataPoint : lightDataPoints) {
				LightPlotData lightPlotData = LightPlotData.buildLightPlotData(lightDataPoint);
				lightPlotDataList.add(lightPlotData);
			}
		} else {
			mLightExposureHour.setText("0");
			mLightExposureMinute.setText("00");
		}
		
		return lightPlotDataList;
	}
	
	public void setDate(Date date) {
		mDate = date;
	}
	
	private static class LightPlotData {
		public double blueLux;
		public double allLux;
		public double blueLuxLog;
		public double allLuxLog;
		public boolean isWristOff;
		
		protected static final int MAX_LIGHT_VALUE = 110000;
		
		public LightPlotData() { }
		
		public static final LightPlotData buildLightPlotData(LightDataPoint lightDataPoint) {
			LightPlotData lightPlotData = new LightPlotData();
			
			lightPlotData.blueLux = lightDataPoint.getBlueValue() * lightDataPoint.getBlueCoeff();
			lightPlotData.blueLux = (lightPlotData.blueLux < 1) ? 1 : lightPlotData.blueLux;
			lightPlotData.blueLux = (lightPlotData.blueLux > MAX_LIGHT_VALUE) ? MAX_LIGHT_VALUE : lightPlotData.blueLux;
			
			lightPlotData.allLux = (lightDataPoint.getRedValue() * lightDataPoint.getRedCoeff()) +
									(lightDataPoint.getGreenValue() * lightDataPoint.getGreenCoeff()) +
									(lightDataPoint.getBlueValue() * lightDataPoint.getBlueCoeff());
			lightPlotData.allLux = (lightPlotData.allLux < 1) ? 1 : lightPlotData.allLux;
			lightPlotData.allLux = (lightPlotData.allLux > MAX_LIGHT_VALUE) ? MAX_LIGHT_VALUE : lightPlotData.allLux;
			
			lightPlotData.blueLuxLog = Math.log10(lightPlotData.blueLux);
			lightPlotData.allLuxLog = Math.log10(lightPlotData.allLux);
			
			int wristOff = lightDataPoint.getWristOff02() + lightDataPoint.getWristOff24() + 
								lightDataPoint.getWristOff46() + lightDataPoint.getWristOff68() + lightDataPoint.getWristOff810();
			
			lightPlotData.isWristOff = wristOff == 0;
			
			return lightPlotData;
		}
	}
	
	private static class LightPlotDataRangeValue extends LightPlotData {
		public double x;
		public double y;
		
		public static final LightPlotDataRangeValue buildLightPlotDataRangeValue(LightDataPoint lightDataPoint, double x, double y) {
			LightPlotDataRangeValue lightPlotData = new LightPlotDataRangeValue();
			
			lightPlotData.blueLux = lightDataPoint.getBlueValue() * lightDataPoint.getBlueCoeff();
			lightPlotData.blueLux = (lightPlotData.blueLux < 1) ? 1 : lightPlotData.blueLux;
			lightPlotData.blueLux = (lightPlotData.blueLux > MAX_LIGHT_VALUE) ? MAX_LIGHT_VALUE : lightPlotData.blueLux;
			
			lightPlotData.allLux = (lightDataPoint.getRedValue() * lightDataPoint.getRedCoeff()) +
									(lightDataPoint.getGreenValue() * lightDataPoint.getGreenCoeff()) +
									(lightDataPoint.getBlueValue() * lightDataPoint.getBlueCoeff());
			lightPlotData.allLux = (lightPlotData.allLux < 1) ? 1 : lightPlotData.allLux;
			lightPlotData.allLux = (lightPlotData.allLux > MAX_LIGHT_VALUE) ? MAX_LIGHT_VALUE : lightPlotData.allLux;
			lightPlotData.allLux = (Math.log10(lightPlotData.allLux) > MAX_LIGHT_VALUE) ? MAX_LIGHT_VALUE : lightPlotData.allLux;
			
			lightPlotData.blueLuxLog = Math.log10(lightPlotData.blueLux);
			lightPlotData.allLuxLog = Math.log10(lightPlotData.allLux);
			
			int wristOff = lightDataPoint.getWristOff02() + lightDataPoint.getWristOff24() + 
								lightDataPoint.getWristOff46() + lightDataPoint.getWristOff68() + lightDataPoint.getWristOff810();
			
			lightPlotData.isWristOff = wristOff == 0;
			lightPlotData.x = x;
			lightPlotData.y = y;
			
			return lightPlotData;
		}
	}
	
	private LightPlotData maxLightPlotData(List<LightPlotData> lightPlotDataList) {
		if (lightPlotDataList != null && lightPlotDataList.size() > 0) {
			LightPlotData lightPlotData = Collections.max(lightPlotDataList, new Comparator<LightPlotData>() {
				@Override
				public int compare(LightPlotData lhs, LightPlotData rhs) {
					if (lhs.allLuxLog > rhs.allLuxLog)
						return 1;
					else if (lhs.allLuxLog < rhs.allLuxLog)
						return -1;
					return 0;
				} });
			return lightPlotData;
		} else {
			return new LightPlotData();
		}
	}
	
	private XYSeriesRenderer allLuxRenderer() {
		if (mAllLuxRenderer == null)
			mAllLuxRenderer = new XYSeriesRenderer();
		mAllLuxRenderer.setColor(getResources().getColor(R.color.color_light_plot_orange));
		return mAllLuxRenderer;
	}
	
	private XYSeriesRenderer blueLuxRenderer() {
		if (mBlueLuxRenderer == null)
			mBlueLuxRenderer = new XYSeriesRenderer();
		mBlueLuxRenderer.setColor(getResources().getColor(R.color.color_light_plot_blue));
		return mBlueLuxRenderer;
	}
	
	private XYSeriesRenderer allLuxWristOffRenderer() {
		if (mAllLuxWristOffRenderer == null)
			mAllLuxWristOffRenderer = new XYSeriesRenderer();
		mAllLuxWristOffRenderer.setColor(getResources().getColor(R.color.color_light_plot_light_gray));
		return mAllLuxWristOffRenderer;
	}
	
	private XYSeriesRenderer blueLuxWristOffRenderer() {
		if (mBlueLuxWristOffRenderer == null)
			mBlueLuxWristOffRenderer = new XYSeriesRenderer();
		mBlueLuxWristOffRenderer.setColor(getResources().getColor(R.color.color_light_plot_dark_gray));
		return mBlueLuxWristOffRenderer;
	}
	
	private void addAllLux(double x, double y) {
		if (mAllLuxSeries == null)
			mAllLuxSeries = new XYSeries("All Lux");
		mAllLuxSeries.add(x, y);
	}
	
	private void addBlueLux(double x, double y) {
		if (mBlueLuxSeries == null)
			mBlueLuxSeries = new XYSeries("Blue Lux");
		mBlueLuxSeries.add(x, y);
	}
	
	private void addAllLuxWristOff(double x, double y) {
		if (mAllLuxWristOffSeries == null)
			mAllLuxWristOffSeries = new XYSeries("All Lux Wrist Off");
		mAllLuxWristOffSeries.add(x, y);
	}
	
	private void addBlueLuxWristOff(double x, double y) {
		if (mBlueLuxWristOffSeries == null)
			mBlueLuxWristOffSeries = new XYSeries("Blue Lux Wrist Off");
		mBlueLuxWristOffSeries.add(x, y);
	}
	
	private final AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (mPosition == 1) {
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
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) { }
	};
	
	private int getCalendarMode() {
		switch (mCalendarModeSpinner.getSelectedItemPosition()) {
		case 1:
			return MODE_WEEK;
		case 2:
			return MODE_MONTH;
		case 3:
			return MODE_YEAR;
		}
		return MODE_DAY;
	}
	
	private final GraphScrollView.GraphScrollViewListener mGraphScrollViewListener = new GraphScrollView.GraphScrollViewListener() {
		
		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {
			
			for (int i=0;i<mLightPlotDataList.size();i++) {
				double[] screenPoint = mBarChart.toScreenPoint(new double[] {i, 0});
				
				int halfBarWidth =  (int)(mBarChart.getRenderer().getBarWidth() / 2);
				int playHeadPositionX = (mPlayheadImage.getLeft() + (mPlayheadImage.getMeasuredWidth() / 2));
				int currentPointX = ((int)(mLeftView.getMeasuredWidth() + screenPoint[0])) - l + (int)mBarChart.getRenderer().getBarSpacing();
				
				if(playHeadPositionX >= currentPointX - halfBarWidth && playHeadPositionX <= currentPointX + halfBarWidth) {
					LightPlotData lightPlotData = mLightPlotDataList.get(i);
                    LifeTrakLogger.info("ALLLUX:" + lightPlotData.allLux + " BLUELUX:" + lightPlotData.blueLux);

                    if (lightPlotData.allLux > 1 && lightPlotData.blueLux > 1) {
                        mAllLuxValue.setText(String.format("%.02f LX", lightPlotData.allLux));
                        mBlueLuxValue.setText(String.format("%.02f LX", lightPlotData.blueLux));
                    }
                    else{
                        mAllLuxValue.setText("1.00 LX");
                        mBlueLuxValue.setText("1.00 LX");
                    }

					break;
				}
			}
		}
	};
	
	private final GraphScrollView.GraphScrollViewListener mGraphScrollViewListener2 = new GraphScrollView.GraphScrollViewListener() {
		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {
			
			for (int i=0;i<mDataHeaders.size();i++) {
				double[] screenPoint = mBarChart.toScreenPoint(new double[] {i, 0});
				
				int halfBarWidth =  (int)(mBarChart.getRenderer().getBarWidth() * 2 / 2);
				int playHeadPositionX = (mPlayheadImage.getLeft() + (mPlayheadImage.getMeasuredWidth() / 2));
				int currentPointX = ((int)(mLeftView.getMeasuredWidth() + screenPoint[0])) - l + (int)mBarChart.getRenderer().getBarSpacing() + 
										getView().findViewById(R.id.relLeftTimeContainer).getMeasuredWidth();
				
				if(playHeadPositionX >= currentPointX - halfBarWidth && playHeadPositionX <= currentPointX + halfBarWidth) {
					StatisticalDataHeader dataHeader = mDataHeaders.get(i);
					mDateView.setText(mDateFormat.format(dataHeader.getDateStamp()));
					mLightExposureHour.setText(String.valueOf(dataHeader.getLightExposure() / 60));
					mLightExposureMinute.setText(String.format("%02d", dataHeader.getLightExposure() % 60));
					break;
				}
			}
		}
	};
	
	private void resizeGraphContainer(int width) {
        if (mLightPlotContainer != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.RIGHT_OF, R.id.viewGraphLeftPadding);
            mLightPlotContainer.setLayoutParams(params);
            mLightPlotContainer.invalidate();
        }
	}
}
