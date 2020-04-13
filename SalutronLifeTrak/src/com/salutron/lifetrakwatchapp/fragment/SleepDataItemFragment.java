package com.salutron.lifetrakwatchapp.fragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;

import com.apptentive.android.sdk.Log;
import com.salutron.blesdk.SALSleepSetting;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.SleepSetting;
import com.salutron.lifetrakwatchapp.adapter.SleepLogsAdapter;

import com.actionbarsherlock.app.SherlockFragment;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class SleepDataItemFragment extends BaseFragment {
	private FrameLayout mSleepDataPlotContainer;
	private TextView mTotalSleepTimeHourValue;
	private TextView mTotalSleepTimeMinuteValue;
	private TextView mMetricGoalPercent;
	private TextView mMetricGoal;
	private TextView mTotalTimeAsleep;
	private TextView mHoursAwake;
	private TextView mSleepStartTime;
	private TextView mSleepEfficiency;
	private TextView mWakeAfterSleepOnset;
	private TextView mNumberOfAwakenings;
	private TextView mSleepOnsetLatency;
	private TextView mCurrentDate;

	private ImageView mMetricGoalIcon;
	private View mMetricProgressValue;
	private ListView mSleepDataList;
	private View mSleepDataHeader;

	private int mSleepStartHour;
	private int mSleepStartMin;
	private float mSleepEfficiencyPercent;
	private Date mDate;

	private final List<SleepDatabase> mSleepDatabases = new ArrayList<SleepDatabase>();
	private final String[] mHours = { "3PM", "4PM", "5PM", "6PM", "7PM", "8PM", "9PM", "10PM", "11PM", "12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM", "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM" };
	private final String[] m24Hours = { "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00" };
	private final DecimalFormat mDecimalFormat = new DecimalFormat("#####0");

	private int mTotalSleepTime;
	private int mTotalLapses;
	private int mSleepOffsetCount;

	private SleepSetting mSleepSetting;
	private SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_sleep_data_item, null);
		initializeViews(view);

		mDecimalFormat.setRoundingMode(RoundingMode.DOWN);

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
				changeFramentView(inflater, R.layout.fragment_sleep_data_item, (ViewGroup) getView());
				showActionBarAndCalendar();
				break;
			case Configuration.ORIENTATION_LANDSCAPE:
				changeFramentView(inflater, R.layout.fragment_sleep_data_item_land, (ViewGroup) getView());
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
		mSleepDataPlotContainer = (FrameLayout) view.findViewById(R.id.frmSleepDataPlotContainer);
		mTotalSleepTimeHourValue = (TextView) view.findViewById(R.id.tvwTotalSleepTimeHourValue);
		mTotalSleepTimeMinuteValue = (TextView) view.findViewById(R.id.tvwTotalSleepTimeMinuteValue);
		mMetricGoalPercent = (TextView) view.findViewById(R.id.tvwMetricGoalPercent);
		mMetricGoal = (TextView) view.findViewById(R.id.tvwMetricGoal);
		mTotalTimeAsleep = (TextView) view.findViewById(R.id.tvwTotalTimeAsleep);
		mHoursAwake = (TextView) view.findViewById(R.id.tvwHoursAwake);
		mSleepStartTime = (TextView) view.findViewById(R.id.tvwSleepStartTime);
		mSleepEfficiency = (TextView) view.findViewById(R.id.tvwSleepEfficiency);
		mWakeAfterSleepOnset = (TextView) view.findViewById(R.id.tvwWakeAfterSleepOnset);
		mNumberOfAwakenings = (TextView) view.findViewById(R.id.tvwNumberOfAwakenings);
		mSleepOnsetLatency = (TextView) view.findViewById(R.id.tvwSleepOnsetLatency);
		mCurrentDate = (TextView) view.findViewById(R.id.tvwCurrentDate);
		mMetricGoalIcon = (ImageView) view.findViewById(R.id.imgMetricGoalIcon);
		mMetricProgressValue = (View) view.findViewById(R.id.viewMetricProgressValue);
		mSleepDataList = (ListView) view.findViewById(R.id.lstSleepData);
	}

	private void initializeObjects() {
		// Date date = getLifeTrakApplication().getCurrentDate();
		Date date = new Date();

		if (getArguments() != null) {
			date = new Date(getArguments().getLong(DATE));
		}

		if (mDate != null)
			date = mDate;

		setDataForDay(date);

		if (orientation() == Configuration.ORIENTATION_PORTRAIT) {
			showActionBarAndCalendar();
		} else {
			hideActionBarAndCalendar();
		}

		List<SleepSetting> sleepSettings = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchSleepSetting = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(SleepSetting.class);

		if (sleepSettings.size() != 0)
			mSleepSetting = sleepSettings.get(0);
	}

	public void setDataForDay(Date date) {
		if (!isAdded())
			return;

		int sleepCount = 0;

		mDate = date;

		mSleepStartHour = 0;
		mSleepStartMin = 0;

		if (mCurrentDate != null) {
			mDateFormat.applyPattern("MMM dd, yyyy");
			mCurrentDate.setText(mDateFormat.format(date));
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		XYMultipleSeriesDataset multipleDataset = new XYMultipleSeriesDataset();
		XYMultipleSeriesRenderer multipleRenderer = new XYMultipleSeriesRenderer();

		XYSeries seriesLightSleep = new XYSeries("Light Sleep");
		XYSeries seriesMediumSleep = new XYSeries("Medium Sleep");
		XYSeries seriesDeepSleep = new XYSeries("Deep Sleep");
		XYSeries seriesActiveTime = new XYSeries("Active Time");
		XYSeries seriesSendentaryTime = new XYSeries("Sedentary Time");

		XYSeriesRenderer lightSleepRenderer = new XYSeriesRenderer();
		XYSeriesRenderer mediumSleepRenderer = new XYSeriesRenderer();
		XYSeriesRenderer deepSleepRenderer = new XYSeriesRenderer();
		XYSeriesRenderer activeTimeRenderer = new XYSeriesRenderer();
		XYSeriesRenderer sedentaryTimeRenderer = new XYSeriesRenderer();

		lightSleepRenderer.setColor(getResources().getColor(R.color.color_light_sleep));
		mediumSleepRenderer.setColor(getResources().getColor(R.color.color_medium_sleep));
		deepSleepRenderer.setColor(getResources().getColor(R.color.color_deep_sleep));
		activeTimeRenderer.setColor(getResources().getColor(R.color.color_active_time));
		sedentaryTimeRenderer.setColor(getResources().getColor(R.color.color_sedentary));

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		Calendar calYesterday = Calendar.getInstance();
		calYesterday.setTime(date);
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);

		int dayYesterday = calYesterday.get(Calendar.DAY_OF_MONTH);
		int monthYesterday = calYesterday.get(Calendar.MONTH) + 1;
		int yearYesterday = calYesterday.get(Calendar.YEAR) - 1900;

		double maxValue = 150 * 5;
		double maxTimeValue = 0;

		String maxQuery = "select max(sleepPoint02 + sleepPoint24 + sleepPoint46 + sleepPoint68 + sleepPoint810) " + "from StatisticalDataPoint dataPoint inner join StatisticalDataHeader dataHeader " + "on dataPoint.dataHeaderAndPoint = dataHeader._id where watchDataHeader = ? and dateStampYear = ? " + "and dateStampMonth = ? and dateStampDay = ?";

		Cursor cursorMax = DataSource.getInstance(getActivity()).getReadOperation()
				.rawQuery(maxQuery, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(yearYesterday), String.valueOf(monthYesterday), String.valueOf(dayYesterday));

		if (cursorMax.moveToFirst()) {
			maxTimeValue = cursorMax.getDouble(0);
			cursorMax.close();
		}

		cursorMax = DataSource.getInstance(getActivity()).getReadOperation()
				.rawQuery(maxQuery, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(year), String.valueOf(month), String.valueOf(day));

		if (cursorMax.moveToFirst()) {
			maxTimeValue = Math.max(maxTimeValue, cursorMax.getDouble(0));
			cursorMax.close();
		}

		String query = "select sleepPoint02, sleepPoint24, sleepPoint46, sleepPoint68, sleepPoint810 " + "from StatisticalDataPoint dataPoint inner join StatisticalDataHeader dataHeader " + "on dataPoint.dataHeaderAndPoint = dataHeader._id where watchDataHeader = ? and dateStampYear = ? " + "and dateStampMonth = ? and dateStampDay = ?";

		List<SleepDatabase> sleepDatabasesYesterday = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchSleepDatabase = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
						String.valueOf(yearYesterday), String.valueOf(monthYesterday), String.valueOf(dayYesterday))
				.orderBy("hourSleepStart, minuteSleepStart", "")
				.getResults(SleepDatabase.class);
		//List<Integer> sleepIndexes = new ArrayList<Integer>();
		List<Integer> sleepIndexesYesterday = new ArrayList<Integer>();

		mSleepDatabases.clear();

		for (SleepDatabase sleepDatabase : sleepDatabasesYesterday) {
			float sleepStart = (sleepDatabase.getHourSleepStart() * 6) + (sleepDatabase.getMinuteSleepStart() / 10);
			float sleepEnd = (sleepDatabase.getHourSleepEnd() * 6) + (sleepDatabase.getMinuteSleepEnd() / 10);

			if (sleepEnd >= sleepStart && sleepDatabase.getHourSleepEnd() >= 15) {
				mSleepDatabases.add(sleepDatabase);

				for (int i = (int)sleepStart; i <= (int) sleepEnd; i++) {
					sleepIndexesYesterday.add(i);

					if (i > 15 * 6 && mSleepStartHour == 0 && mSleepStartMin == 0) {
						mSleepStartHour = sleepDatabase.getHourSleepStart();
						mSleepStartMin = sleepDatabase.getMinuteSleepStart();
					}
				}
			} else if (sleepDatabase.getHourSleepStart() > sleepDatabase.getHourSleepEnd()) {
				mSleepDatabases.add(sleepDatabase);
				if ((int)sleepStart > (int) sleepEnd){
					sleepEnd = sleepEnd + (24 * 6);
				}
				else{
					sleepEnd = (24 * 6);
				}


				for (int i = (int)sleepStart; i < (int) sleepEnd; i++) {
					sleepIndexesYesterday.add(i);

					if (i > 15 * 6 && mSleepStartHour == 0 && mSleepStartMin == 0) {
						mSleepStartHour = sleepDatabase.getHourSleepStart();
						mSleepStartMin = sleepDatabase.getMinuteSleepStart();
					}
				}

				sleepEnd = (sleepDatabase.getHourSleepEnd() * 6) + (sleepDatabase.getMinuteSleepEnd() / 10);
				if ((int)sleepStart < (int) sleepEnd) {
					for (int i = 0; i <= (int) sleepEnd; i++) {
						sleepIndexesYesterday.add(i);
					}
				}
			}
		}

		Cursor cursor = DataSource.getInstance(getActivity()).getReadOperation()
				.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(yearYesterday), String.valueOf(monthYesterday), String.valueOf(dayYesterday));
		int index = 0;
		int x = 0;

		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				if (index >= 15 * 6) {
					int sleepPoint02 = cursor.getInt(0);
					int sleepPoint24 = cursor.getInt(1);
					int sleepPoint46 = cursor.getInt(2);
					int sleepPoint68 = cursor.getInt(3);
					int sleepPoint810 = cursor.getInt(4);

					double value = (double) sleepPoint02 + sleepPoint24 + sleepPoint46 + sleepPoint68 + sleepPoint810;

					if (sleepIndexesYesterday.contains(index)) {
						sleepIndexesYesterday.remove(sleepIndexesYesterday.indexOf(index));
						value = maxValue - value;

						if (value < 150) {
							value = -getYWithMaxValue(value, maxValue, 0, 10);
							seriesLightSleep.add(x, value);
						} else if (value > 150 && value < 300) {
							value = -getYWithMaxValue(value, maxValue, 0, 10);
							seriesMediumSleep.add(x, value);
						} else if (value > 300) {
							value = -getYWithMaxValue(value, maxValue, 0, 10);
							seriesDeepSleep.add(x, value);
						}
						sleepCount++;
					} else {
						if (value < 40 * 5) {
							value = getYWithMaxValue(value, maxTimeValue, 0, 10);
							seriesSendentaryTime.add(x, value);
						} else {
							value = getYWithMaxValue(value, maxTimeValue, 0, 10);
							seriesActiveTime.add(x, value);
						}
					}
					x++;
				}

				index++;
			}
			if (sleepIndexesYesterday.contains(145)){

				Calendar calendarNexDay = Calendar.getInstance();
				calendarNexDay.setTime(date);
				int calendarNextDay = calendarNexDay.get(Calendar.DAY_OF_MONTH);
				int calendarNextMonth = calendarNexDay.get(Calendar.MONTH) + 1;
				int calendarNextYear = calendarNexDay.get(Calendar.YEAR) - 1900;

				Cursor cursor2 = DataSource.getInstance(getActivity()).getReadOperation()
						.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(calendarNextYear), String.valueOf(calendarNextMonth), String.valueOf(calendarNextDay));
				index = 144;
				int index2 = 0;
				if (cursor2.getCount() > 0) {
					while (cursor2.moveToNext()) {
						if (index2 <= 15 * 6) {
							int sleepPoint02 = cursor2.getInt(0);
							int sleepPoint24 = cursor2.getInt(1);
							int sleepPoint46 = cursor2.getInt(2);
							int sleepPoint68 = cursor2.getInt(3);
							int sleepPoint810 = cursor2.getInt(4);

							double value = (double) sleepPoint02 + sleepPoint24 + sleepPoint46 + sleepPoint68 + sleepPoint810;

							if (sleepIndexesYesterday.contains(index)) {
								sleepIndexesYesterday.remove(sleepIndexesYesterday.indexOf(index));
								value = maxValue - value;

								if (value < 150) {
									value = -getYWithMaxValue(value, maxValue, 0, 10);
									seriesLightSleep.add(x, value);
								} else if (value > 150 && value < 300) {
									value = -getYWithMaxValue(value, maxValue, 0, 10);
									seriesMediumSleep.add(x, value);
								} else if (value > 300) {
									value = -getYWithMaxValue(value, maxValue, 0, 10);
									seriesDeepSleep.add(x, value);
								}
								sleepCount++;
								x++;
							}

						}
						index2++;
						index++;
					}
				}
			}

		} else {
			if (sleepDatabasesYesterday.size() > 0) {
				for (SleepDatabase sleepDatabase : sleepDatabasesYesterday) {
					if (sleepDatabase.getHourSleepEnd() >= 15) {
						for (int i = 0; i < 144; i++) {
							if (index >= 15 * 6) {
								if (sleepIndexesYesterday.contains(index)) {
									double value = 0;
									value = maxValue - value;

									if (value < 150) {
										value = -getYWithMaxValue(value, maxValue, 0, 10);
										seriesLightSleep.add(x, value);
									} else if (value > 150 && value < 300) {
										value = -getYWithMaxValue(value, maxValue, 0, 10);
										seriesMediumSleep.add(x, value);
									} else if (value > 300) {
										value = -getYWithMaxValue(value, maxValue, 0, 10);
										seriesDeepSleep.add(x, value);
									}
								} else {
									seriesSendentaryTime.add(x, 0);
								}

								x++;
							}

							index++;
						}
					}
				}
			} else {
				for (int i = 0; i < 144; i++) {
					if (index >= 15 * 6) {
						seriesSendentaryTime.add(x, 0);
						x++;
					}
					index++;
				}
			}
		}

		sleepIndexesYesterday.clear();
		List<Integer> sleepIndexes = new ArrayList<Integer>();

		if (sleepIndexesYesterday.size() > 0) {
			sleepIndexes.addAll(0, sleepIndexesYesterday);
		}

		float sleepStartNow = 0;
		float sleepEndNow = 0;

		mTotalSleepTime = 0;

		List<SleepDatabase> sleepDatabasesNow = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchSleepDatabase = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
						String.valueOf(year), String.valueOf(month), String.valueOf(day))
				.orderBy("hourSleepStart, minuteSleepStart", "")
				.getResults(SleepDatabase.class);

		for (SleepDatabase sleepDatabase : sleepDatabasesNow) {
			sleepStartNow = (sleepDatabase.getHourSleepStart() * 6) + (sleepDatabase.getMinuteSleepStart() / 10.0f);
			sleepEndNow = (sleepDatabase.getHourSleepEnd() * 6) + (sleepDatabase.getMinuteSleepEnd() / 10);

			sleepEndNow = (sleepEndNow >= 144 - 1) ? 144 - 1 : sleepEndNow;
			sleepEndNow = (sleepEndNow <= sleepStartNow) ? 144 - 1 : sleepEndNow;

			if ((sleepStartNow < sleepEndNow) && sleepDatabase.getHourSleepStart() < 15 && sleepDatabase.getHourSleepEnd() < 15) {
				mSleepDatabases.add(sleepDatabase);

				for (int i=(int)sleepStartNow;i<=sleepEndNow;i++) {
					sleepIndexes.add(i);
				}
			} else if (sleepDatabase.getHourSleepStart() < 15 && sleepDatabase.getHourSleepEnd() >= 15) {
				for (int i=(int)sleepStartNow;i<90;i++) {
					sleepIndexes.add(i);
				}
			}

			if (sleepDatabase.getHourSleepEnd() > sleepDatabase.getHourSleepStart() ||
					(sleepDatabase.getHourSleepEnd() == sleepDatabase.getHourSleepStart() &&
							sleepDatabase.getMinuteSleepEnd() > sleepDatabase.getMinuteSleepStart())) {
				int start = sleepDatabase.getHourSleepStart() * 60 + sleepDatabase.getMinuteSleepStart();
				int end = sleepDatabase.getHourSleepEnd() * 60 + sleepDatabase.getMinuteSleepEnd();

				mTotalSleepTime += (end - start);
			} else {
				mTotalSleepTime += ((24 * 60) - (sleepDatabase.getHourSleepStart() * 60 + sleepDatabase.getMinuteSleepStart()));
				mTotalSleepTime += (sleepDatabase.getHourSleepEnd() * 60 + sleepDatabase.getMinuteSleepEnd());
			}
		}

		index = 0;

		cursor.close();
		cursor = DataSource.getInstance(getActivity()).getReadOperation()
				.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(year), String.valueOf(month), String.valueOf(day));
		int x2 = x;
		x = 54;
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				if (index < 15 * 6) {
					int sleepPoint02 = cursor.getInt(0);
					int sleepPoint24 = cursor.getInt(1);
					int sleepPoint46 = cursor.getInt(2);
					int sleepPoint68 = cursor.getInt(3);
					int sleepPoint810 = cursor.getInt(4);

					double value = (double) sleepPoint02 + sleepPoint24 + sleepPoint46 + sleepPoint68 + sleepPoint810;

					if (sleepIndexes.contains(index)) {
						value = maxValue - value;

						if (value < 150) {
							value = -getYWithMaxValue(value, maxValue, 0, 10);
							seriesLightSleep.add(x, value);
						} else if (value > 150 && value < 300) {
							value = -getYWithMaxValue(value, maxValue, 0, 10);
							seriesMediumSleep.add(x, value);
						} else if (value > 300) {
							value = -getYWithMaxValue(value, maxValue, 0, 10);
							seriesDeepSleep.add(x, value);
						}
						sleepCount++;
					} else {
						if (x2 <= x) {
							if (value < 40 * 5) {
								value = getYWithMaxValue(value, maxTimeValue, 0, 10);
								seriesSendentaryTime.add(x, value);
							} else {
								value = getYWithMaxValue(value, maxTimeValue, 0, 10);
								seriesActiveTime.add(x, value);
							}
						}
					}

					x++;
				}

				index++;
			}
		} else {
			if (sleepDatabasesNow.size() > 0) {
				for (SleepDatabase sleepDatabase : sleepDatabasesNow) {
					if (sleepDatabase.getHourSleepEnd() < 15 || (sleepDatabase.getHourSleepEnd() <= 15) && (sleepDatabase.getMinuteSleepEnd() == 0)) {

						for (int i = 0; i < 144; i++) {
							if (index <= 15 * 6) {
								if (sleepIndexes.contains(index)) {
									double value = 0;
									value = maxValue - value;

									if (value < 150) {
										value = -getYWithMaxValue(value, maxValue, 0, 10);
										seriesLightSleep.add(x, value);
									} else if (value > 150 && value < 300) {
										value = -getYWithMaxValue(value, maxValue, 0, 10);
										seriesMediumSleep.add(x, value);
									} else if (value > 300) {
										value = -getYWithMaxValue(value, maxValue, 0, 10);
										seriesDeepSleep.add(x, value);
									}
								} else {
									seriesSendentaryTime.add(x, 0);
								}

								x++;
							}

							index++;
						}
					}
				}
			} else {
				for (int i = 0; i < 144; i++) {
					if (index >= 15 * 6) {
						seriesSendentaryTime.add(x, 0);
						x++;
					}
					index++;
				}
			}
		}

		multipleDataset.addSeries(seriesLightSleep);
		multipleDataset.addSeries(seriesMediumSleep);
		multipleDataset.addSeries(seriesDeepSleep);
		multipleDataset.addSeries(seriesSendentaryTime);
		multipleDataset.addSeries(seriesActiveTime);

		multipleRenderer.addSeriesRenderer(lightSleepRenderer);
		multipleRenderer.addSeriesRenderer(mediumSleepRenderer);
		multipleRenderer.addSeriesRenderer(deepSleepRenderer);
		multipleRenderer.addSeriesRenderer(sedentaryTimeRenderer);
		multipleRenderer.addSeriesRenderer(activeTimeRenderer);

		multipleRenderer.setMargins(new int[] { 0, 0, 0, 0 });
		multipleRenderer.setMarginsColor(getResources().getColor(R.color.color_xaxis_bar));
		multipleRenderer.setYAxisMin(-10);
		multipleRenderer.setYAxisMax(10);
		multipleRenderer.setShowLegend(false);
		multipleRenderer.setPanEnabled(false);
		multipleRenderer.setShowGridY(false);
		multipleRenderer.setShowAxes(false);
		multipleRenderer.setYLabels(0);

		if (orientation() == Configuration.ORIENTATION_PORTRAIT) {
			multipleRenderer.setShowLabels(false);
			multipleRenderer.setBarWidth(10);
			multipleRenderer.setXAxisMin(0);
			multipleRenderer.setXAxisMax(144);
			multipleRenderer.setMargins(new int[] { 0, (int) dpToPx(5), 0, (int) dpToPx(5) });
		} else {
			multipleRenderer.setShowLabels(true);
			multipleRenderer.setBarWidth(35);
			multipleRenderer.setXLabelsColor(Color.BLACK);
			multipleRenderer.setLabelsTextSize(dpToPx(12));
			multipleRenderer.setXAxisMin(-1);
			multipleRenderer.setXAxisMax(144);
			multipleRenderer.setMargins(new int[] { 0, 0, 0, 0 });

			multipleRenderer.setXLabels(0);

			int hourIndex = 0;

			for (int i = 0; i < 144; i++) {
				if (i % 6 == 0) {
					switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
						case TIME_FORMAT_12_HR:
							multipleRenderer.addXTextLabel(i, mHours[hourIndex]);
							break;
						case TIME_FORMAT_24_HR:
							multipleRenderer.addXTextLabel(i,m24Hours[hourIndex]);
							break;
					}
					hourIndex++;
				}
			}
		}

		multipleRenderer.setZoomEnabled(false, false);

		if (mSleepDataList != null) {
			SleepLogsAdapter adapter = new SleepLogsAdapter(getActivity(), R.layout.adapter_sleep_log, mSleepDatabases);

			if (mSleepDataHeader == null) {
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				mSleepDataHeader = inflater.inflate(R.layout.view_sleep_data_header, null);
			}

			TextView sleepTimeStart = (TextView) mSleepDataHeader.findViewById(R.id.tvwSleepTimeStart);
			TextView sleepTimeEnd = (TextView) mSleepDataHeader.findViewById(R.id.tvwSleepTimeEnd);

			switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
				case TIME_FORMAT_12_HR:
					sleepTimeStart.setText(R.string.three_pm);
					sleepTimeEnd.setText(R.string.three_pm);
					break;
				case TIME_FORMAT_24_HR:
					sleepTimeStart.setText(R.string.fifteen_hour);
					sleepTimeEnd.setText(R.string.fifteen_hour);
					break;
			}

			if (mSleepDataHeader != null)
				mSleepDataList.removeHeaderView(mSleepDataHeader);

			mSleepDataList.addHeaderView(mSleepDataHeader);
			mSleepDataPlotContainer = (FrameLayout) mSleepDataHeader.findViewById(R.id.frmSleepDataPlotContainer);
			mSleepDataList.setAdapter(adapter);
			//mSleepDataList.setOnItemClickListener(mOnItemClickListener);
		}

		mTotalSleepTime = 0;
		mTotalLapses = 0;

		for (SleepDatabase sleepDatabase : mSleepDatabases) {
			mTotalSleepTime += sleepDatabase.getSleepDuration();
			mTotalLapses += sleepDatabase.getLapses();
			mSleepOffsetCount += sleepDatabase.getSleepOffset();
		}

		mSleepDataPlotContainer.removeAllViews();
		GraphicalView graphView = ChartFactory.getBarChartView(getActivity(), multipleDataset, multipleRenderer, BarChart.Type.STACKED);
		mSleepDataPlotContainer.addView(graphView);
		initializeStats(date, sleepCount);
	}

	private void initializeStats(Date date, int sleepCount) {
		if (mSleepDatabases == null)
			return;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

//		final List<Goal> goals = DataSource.getInstance(getActivity())
//											.getReadOperation()
//											.query("watchGoal = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
//											.orderBy("abs(date - " + calendar.getTime().getTime() + ")", SORT_ASC)
//											.limit(1)
//											.getResults(Goal.class);
		List<Goal> goals  = DataSource
				.getInstance(getActivity())
				.getReadOperation()
				.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
						String.valueOf(getLifeTrakApplication()
								.getSelectedWatch().getId()),
						String.valueOf(day), String.valueOf(month),
						String.valueOf(year))
				.orderBy("_id", SORT_DESC)
				.getResults(Goal.class);

		if (goals.size() == 0) {
			goals = DataSource.getInstance(getActivity())
					.getReadOperation()
					.query("watchGoal = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
					.orderBy("abs(date - " + date.getTime() + ")", SORT_ASC)
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
						.getResults(Goal.class);
			}
		}

		int sleepMinute = 0;

		List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchDataHeader = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ?",
						String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(year), String.valueOf(month), String.valueOf(day))
				.getResults(StatisticalDataHeader.class);
		
		/*if (dataHeaders.size() > 0) {
			//sleepMinute = dataHeaders.get(0).getTotalSleep();
			sleepMinute = mTotalSleepTime;
		}*/

		sleepMinute = mTotalSleepTime;

		if (orientation() == Configuration.ORIENTATION_PORTRAIT) {
			if (sleepMinute > 0) {
				mTotalSleepTimeHourValue.setText(String.format("%02d", sleepMinute / 60));
				mTotalSleepTimeMinuteValue.setText(String.format("%02d", sleepMinute % 60));
			} else {
				mTotalSleepTimeHourValue.setText("00");
				mTotalSleepTimeMinuteValue.setText("00");
			}

			if (goals.size() > 0) {
				int sleepGoal = goals.get(0).getSleepGoal();
				float percent = (float) sleepMinute / (float) sleepGoal;
				float width = dpToPx(170) * percent;

				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) width, FrameLayout.LayoutParams.MATCH_PARENT);

				percent = percent * 100;

				if (Float.isNaN(percent))
					percent = 0;

				mMetricGoalPercent.setText(mDecimalFormat.format(percent) + "%");
				mMetricGoalPercent.setTextColor(colorForPercent(percent));
				mMetricGoal.setText(String.format("%02dh%02dm", sleepGoal / 60, sleepGoal % 60));
				mMetricGoalIcon.setImageResource(imageResourceForPercent(percent));
				mMetricProgressValue.setLayoutParams(params);
				mMetricProgressValue.setBackgroundColor(colorForPercent(percent));
			}
		} else {
			float sleepSum = sleepSumForDate(date);
			float lapsesSum = lapsesForDate(date);

			if (sleepSum > 0 && lapsesSum > 0)
				mSleepEfficiencyPercent = sleepSum / (lapsesSum + sleepSum);

			mTotalTimeAsleep.setText(String.format("%02dH%02dM", sleepMinute / 60, sleepMinute % 60));

			int hoursAwake = 1440 - sleepMinute;
			mHoursAwake.setText(String.format("%02dH%02dM", hoursAwake / 60, hoursAwake % 60));

			int waso = mTotalLapses * 2;
			mWakeAfterSleepOnset.setText(String.format("%02dH%02dM", waso / 60, waso % 60));

			mNumberOfAwakenings.setText(String.valueOf(mTotalLapses));

			if (mSleepSetting != null) {
				if (mSleepSetting.getSleepDetectType() == 1) {
					mSleepOnsetLatency.setText(String.valueOf(mSleepOffsetCount));
				} else {
					mSleepOnsetLatency.setText("0");
				}
			}
			mSleepStartTime.setText(String.format("%02dH%02dM", mSleepStartHour, mSleepStartMin));
			mSleepEfficiency.setText(String.valueOf(Math.round(mSleepEfficiencyPercent * 100.0f)) + "%");
		}
	}

	private float sleepSumForDate(Date date) {
		float sleepSum = 0;

		String querySleepSum = "select sum(deepSleepCount+lightSleepCount),sum(lapses) " + "from SleepDatabase where watchSleepDatabase = ? and dateStampYear = ? " + "and dateStampMonth = ? and dateStampDay = ?";

		Calendar calYesterday = Calendar.getInstance();
		Calendar calNow = Calendar.getInstance();

		calYesterday.setTime(date);
		calNow.setTime(date);

		calYesterday.add(Calendar.DAY_OF_MONTH, -1);

		int yearYesterday = calYesterday.get(Calendar.YEAR) - 1900;
		int monthYesterday = calYesterday.get(Calendar.MONTH) + 1;
		int dayYesterday = calYesterday.get(Calendar.DAY_OF_MONTH);

		int year = calNow.get(Calendar.YEAR) - 1900;
		int month = calNow.get(Calendar.MONTH) + 1;
		int day = calNow.get(Calendar.DAY_OF_MONTH);

		Cursor cursorSleepSum = DataSource
				.getInstance(getActivity())
				.getReadOperation()
				.rawQuery(querySleepSum, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(yearYesterday), String.valueOf(monthYesterday),
						String.valueOf(dayYesterday));

		if (cursorSleepSum.moveToFirst()) {
			sleepSum = cursorSleepSum.getFloat(0);
		}

		cursorSleepSum.close();

		cursorSleepSum = DataSource.getInstance(getActivity()).getReadOperation()
				.rawQuery(querySleepSum, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(year), String.valueOf(month), String.valueOf(day));

		if (cursorSleepSum.moveToFirst()) {
			sleepSum += cursorSleepSum.getFloat(0);
		}

		cursorSleepSum.close();

		return sleepSum;
	}

	private float lapsesForDate(Date date) {
		float lapses = 0;

		String querySleepSum = "select sum(lapses) " + "from SleepDatabase where watchSleepDatabase = ? and dateStampYear = ? " + "and dateStampMonth = ? and dateStampDay = ?";

		Calendar calYesterday = Calendar.getInstance();
		Calendar calNow = Calendar.getInstance();

		calYesterday.setTime(date);
		calNow.setTime(date);

		calYesterday.add(Calendar.DAY_OF_MONTH, -1);

		int yearYesterday = calYesterday.get(Calendar.YEAR) - 1900;
		int monthYesterday = calYesterday.get(Calendar.MONTH) + 1;
		int dayYesterday = calYesterday.get(Calendar.DAY_OF_MONTH);

		int year = calNow.get(Calendar.YEAR) - 1900;
		int month = calNow.get(Calendar.MONTH) + 1;
		int day = calNow.get(Calendar.DAY_OF_MONTH);

		Cursor cursorSleepSum = DataSource
				.getInstance(getActivity())
				.getReadOperation()
				.rawQuery(querySleepSum, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(yearYesterday), String.valueOf(monthYesterday),
						String.valueOf(dayYesterday));

		if (cursorSleepSum.moveToFirst()) {
			lapses = cursorSleepSum.getFloat(0);
		}

		cursorSleepSum.close();

		cursorSleepSum = DataSource.getInstance(getActivity()).getReadOperation()
				.rawQuery(querySleepSum, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()), String.valueOf(year), String.valueOf(month), String.valueOf(day));

		if (cursorSleepSum.moveToFirst()) {
			lapses += cursorSleepSum.getFloat(0);
		}

		cursorSleepSum.close();

		return lapses;
	}

	private final AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (view.getId() == 1000) {
				mPreferenceWrapper.setPreferenceBooleanValue(ADD_NEW_SLEEP, false).synchronize();
				SleepDatabase sleepDatabase = mSleepDatabases.get(position - 1);
				Bundle bundle = new Bundle();
				bundle.putParcelable(SLEEP_DATABASE, sleepDatabase);
				SherlockFragment fragment = FragmentFactory.newInstance(SleepDataUpdate.class, bundle);
				((MainActivity) getActivity()).switchFragment(fragment);
			}
		}
	};

	private int colorForPercent(float percent) {
		if (percent * 100 >= 100) {
			return getResources().getColor(R.color.color_100_percent);
		} else if (percent * 100 >= 75) {
			return getResources().getColor(R.color.color_75_percent);
		} else if (percent * 100 >= 50) {
			return getResources().getColor(R.color.color_50_percent);
		} else if (percent * 100 >= 25) {
			return getResources().getColor(R.color.color_25_percent);
		}
		return getResources().getColor(R.color.color_gray);
	}

	private int imageResourceForPercent(float percent) {
		if (percent * 100 >= 100) {
			return R.drawable.ll_fitnessres_icon_goal_05;
		} else if (percent * 100 >= 75) {
			return R.drawable.ll_fitnessres_icon_goal_04;
		} else if (percent * 100 >= 50) {
			return R.drawable.ll_fitnessres_icon_goal_03;
		} else if (percent * 100 >= 25) {
			return R.drawable.ll_fitnessres_icon_goal_02;
		}
		return R.drawable.ll_fitnessres_icon_goal_01;
	}

	public void setDate(Date date) {
		mDate = date;
	}
}
