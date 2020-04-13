package com.salutron.lifetrakwatchapp.fragment;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.database.Cursor;
import android.widget.Toast;

import com.google.common.primitives.Ints;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.model.SleepDatabase;
import com.salutron.lifetrakwatchapp.model.StatisticalDataHeader;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.StatisticalDataPoint;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.model.WorkoutHeader;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.WorkoutStopInfo;
import com.salutron.lifetrakwatchapp.adapter.DashboardItem;
import com.salutron.lifetrakwatchapp.adapter.DashboardItemMetric;
import com.salutron.lifetrakwatchapp.adapter.DashboardItemHeartRate;
import com.salutron.lifetrakwatchapp.adapter.DashboardItemSleep;
import com.salutron.lifetrakwatchapp.adapter.DashboardItemActigraphy;
import com.salutron.lifetrakwatchapp.adapter.DashboardItemWorkout;
import com.salutron.lifetrakwatchapp.adapter.DashboardItemFactory;
import com.salutron.lifetrakwatchapp.adapter.DashboardItemAdapter;
import com.salutron.lifetrakwatchapp.adapter.DashboardItemLightExposure;
import com.salutron.lifetrakwatchapp.db.DataSource;

import com.mobeta.android.dslv.DragSortListView;
import com.google.inject.Inject;
import roboguice.inject.InjectView;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class DashboardItemFragment extends BaseFragment implements AdapterView.OnItemClickListener {
	@InjectView(R.id.lstDashboardItem) 	private DragSortListView mDashboardItemList;
	@Inject 							private ArrayList<DashboardItem> mDashboardItems;
	private DashboardItemAdapter mAdapter;
	private DashboardItemHeartRate mAverageHeartRate;
	private DashboardItemMetric mStepsMetric;
	private DashboardItemMetric mDistanceMetric;
	private DashboardItemMetric mCalorieMetric;
	private DashboardItemSleep mSleepMetric;
	private DashboardItemWorkout mWorkoutInfo;
	private DashboardItemActigraphy mActigraphy;
	private DashboardItemLightExposure mLightExposure;
	private Date mDate;
	private ArrayList<Float> avgAverage = new ArrayList<Float>();
	private int mWorkoutCount;
	private int mSleepMinute;
	private ArrayList<Integer> trueValuesHR = new ArrayList<Integer>();
	private int mTotalSleepTime;
	private ArrayList<Integer> mHeartRateIndex = new ArrayList<Integer>();
	
	public DashboardItemFragment() { }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_dashboard_item, null);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		initializeObjects();
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mAdapter = new DashboardItemAdapter(getActivity(), 0, mDashboardItems);
		
		mDashboardItemList.setAdapter(mAdapter);
		mDashboardItemList.setDropListener(mDropListener);
		mDashboardItemList.setOnItemClickListener(this);
		
		if(getArguments() != null) {
			int position = getArguments().getInt(POSITION);
			
			switch(position) {
			case 0:
				mDate = getYesterdayForDate(getLifeTrakApplication().getCurrentDate());
				break;
			case 1:
				mDate = getLifeTrakApplication().getCurrentDate();
				break;
			case 2:
				mDate = getTomorrowForDate(getLifeTrakApplication().getCurrentDate());
				break;
			}
			
			//if(isAdded())
				setDataWithDate(mDate, position);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList(DASHBOARD_ITEMS, mDashboardItems);
	}
	
	/*
	 * Method for initializing objects
	 */
	private void initializeObjects() {

        showActionBarAndCalendar();
		
		mAverageHeartRate = (DashboardItemHeartRate) DashboardItemFactory
						.createDashboardItem(TYPE_HEART_RATE);
		mStepsMetric = (DashboardItemMetric) DashboardItemFactory
						.createDashboardItem(TYPE_STEPS);
		mDistanceMetric = (DashboardItemMetric) DashboardItemFactory
						.createDashboardItem(TYPE_DISTANCE);
		mCalorieMetric = (DashboardItemMetric) DashboardItemFactory
						.createDashboardItem(TYPE_CALORIES);
		mSleepMetric = (DashboardItemSleep) DashboardItemFactory
						.createDashboardItem(TYPE_SLEEP);
		mWorkoutInfo = (DashboardItemWorkout) DashboardItemFactory
						.createDashboardItem(TYPE_WORKOUT);
		mActigraphy = (DashboardItemActigraphy) DashboardItemFactory
						.createDashboardItem(TYPE_ACTIGRAPHY);
		mLightExposure = (DashboardItemLightExposure) DashboardItemFactory
						.createDashboardItem(TYPE_LIGHT_EXPOSURE);
		
		arrangeDashboardItems();
	}
	
	public void arrangeDashboardItems() {
		String json = mPreferenceWrapper.getPreferenceStringValue(DASHBOARD_ITEM_JSON);
		
		mDashboardItems.clear();
		
		if(json != null) {
			try {
				List<Integer> itemTypes = new ArrayList<Integer>();
				itemTypes.add(DASHBOARD_ITEM_TYPE_HEART_RATE);
				itemTypes.add(TYPE_STEPS);
				itemTypes.add(TYPE_DISTANCE);
				itemTypes.add(TYPE_CALORIES);
				itemTypes.add(DASHBOARD_ITEM_TYPE_SLEEP);
				itemTypes.add(DASHBOARD_ITEM_TYPE_WORKOUT);
				itemTypes.add(DASHBOARD_ITEM_TYPE_ACTIGRAPHY);
				
				JSONObject jsonObject = new JSONObject(json);
				JSONArray jsonArray = jsonObject.getJSONArray(ITEM_VIEW_TYPE);
				
				for(int i=0;i<jsonArray.length();i++) {
					int itemType = jsonArray.getInt(i);
					
					switch(itemType) {
					case DASHBOARD_ITEM_TYPE_HEART_RATE:
						mDashboardItems.add(mAverageHeartRate);
						itemTypes.remove((Integer)DASHBOARD_ITEM_TYPE_HEART_RATE);
						break;
					case TYPE_STEPS:
						mDashboardItems.add(mStepsMetric);
						itemTypes.remove((Integer)TYPE_STEPS);
						break;
					case TYPE_DISTANCE:
						mDashboardItems.add(mDistanceMetric);
						itemTypes.remove((Integer)TYPE_DISTANCE);
						break;
					case TYPE_CALORIES:
						mDashboardItems.add(mCalorieMetric);
						itemTypes.remove((Integer)TYPE_CALORIES);
						break;
					case DASHBOARD_ITEM_TYPE_SLEEP:
						if(getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                                getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS ) {
							mDashboardItems.add(mSleepMetric);
							itemTypes.remove((Integer)DASHBOARD_ITEM_TYPE_SLEEP);
						}
						break;
					case DASHBOARD_ITEM_TYPE_WORKOUT:
						if(getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                                getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS) {
							mDashboardItems.add(mWorkoutInfo);
							itemTypes.remove((Integer)DASHBOARD_ITEM_TYPE_WORKOUT);
						}
						break;
					case DASHBOARD_ITEM_TYPE_ACTIGRAPHY:
						if(getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                                getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS) {
							mDashboardItems.add(mActigraphy);
							itemTypes.remove((Integer)DASHBOARD_ITEM_TYPE_ACTIGRAPHY);
						}
						break;
					case DASHBOARD_ITEM_TYPE_LIGHT_EXPOSURE:
						if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
							mDashboardItems.add(mLightExposure);
							itemTypes.remove((Integer)DASHBOARD_ITEM_TYPE_LIGHT_EXPOSURE);
						}
						break;
					}
				}
				
				if(itemTypes.size() > 0) {
					for(int i=0;i<itemTypes.size();i++) {
						int itemType = itemTypes.get(i);
						
						switch(itemType) {
						case DASHBOARD_ITEM_TYPE_HEART_RATE:
							mDashboardItems.add(mAverageHeartRate);
							itemTypes.remove((Integer)DASHBOARD_ITEM_TYPE_HEART_RATE);
							break;
						case TYPE_STEPS:
							mDashboardItems.add(mStepsMetric);
							itemTypes.remove((Integer)TYPE_STEPS);
							break;
						case TYPE_DISTANCE:
							mDashboardItems.add(mDistanceMetric);
							itemTypes.remove((Integer)TYPE_DISTANCE);
							break;
						case TYPE_CALORIES:
							mDashboardItems.add(mCalorieMetric);
							itemTypes.remove((Integer)TYPE_CALORIES);
							break;
						case DASHBOARD_ITEM_TYPE_SLEEP:
							if(getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                                    getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS) {
								mDashboardItems.add(mSleepMetric);
								itemTypes.remove((Integer)DASHBOARD_ITEM_TYPE_SLEEP);
							}
							break;
						case DASHBOARD_ITEM_TYPE_WORKOUT:
							if(getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                                    getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS) {
								mDashboardItems.add(mWorkoutInfo);
								itemTypes.remove((Integer)DASHBOARD_ITEM_TYPE_WORKOUT);
							}
							break;
						case DASHBOARD_ITEM_TYPE_ACTIGRAPHY:
							if(getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                                    getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS) {
								mDashboardItems.add(mActigraphy);
								itemTypes.remove((Integer)DASHBOARD_ITEM_TYPE_ACTIGRAPHY);
							}
							break;
						case DASHBOARD_ITEM_TYPE_LIGHT_EXPOSURE:
							if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
								mDashboardItems.add(mLightExposure);
								itemTypes.remove((Integer)DASHBOARD_ITEM_TYPE_LIGHT_EXPOSURE);
							}
							break;
						}
					}
				}
				
				if(getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS ) {
					boolean hasSleepMetric = false;
					boolean hasActigraphy = false;
					boolean hasWorkoutInfo = false;
					boolean hasLightExposure = false;
					
					for(DashboardItem dashboardItem : mDashboardItems) {
						switch(dashboardItem.getItemViewType()) {
						case DASHBOARD_ITEM_TYPE_SLEEP:
							hasSleepMetric = true;
							break;
						case DASHBOARD_ITEM_TYPE_WORKOUT:
							hasActigraphy = true;
							break;
						case DASHBOARD_ITEM_TYPE_ACTIGRAPHY:
							hasWorkoutInfo = true;
							break;
						case DASHBOARD_ITEM_TYPE_LIGHT_EXPOSURE:
							hasLightExposure = true;
							break;
						}
					}
					
					if (!hasSleepMetric) {
						mDashboardItems.add(mSleepMetric);
					}
					if (!hasActigraphy) {
						mDashboardItems.add(mActigraphy);
					}
					if (!hasWorkoutInfo) {
						mDashboardItems.add(mWorkoutInfo);
					}
					if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415 && !hasLightExposure) {
						mDashboardItems.add(mLightExposure);
					}
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			mDashboardItems.add(mAverageHeartRate);
			mDashboardItems.add(mStepsMetric);
			mDashboardItems.add(mDistanceMetric);
			mDashboardItems.add(mCalorieMetric);
			
			if(getLifeTrakApplication().getSelectedWatch() != null && (getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300 &&
                    getLifeTrakApplication().getSelectedWatch().getModel() != WATCHMODEL_C300_IOS)) {
				mDashboardItems.add(mSleepMetric);
				mDashboardItems.add(mActigraphy);
				mDashboardItems.add(mWorkoutInfo);
				
				if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
					mDashboardItems.add(mLightExposure);
				}
			}
		}
	}
	
	/*
	 * Method for drag and drop of listview item
	 */
	private final DragSortListView.DropListener mDropListener = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			MainActivity mainActivity = (MainActivity) getActivity();
			
			DashboardItem dashboardItem = mAdapter.getItem(from);
			mAdapter.remove(dashboardItem);
			mAdapter.insert(dashboardItem, to);
			
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			
			for(int i=0;i<mAdapter.getCount();i++) {
				dashboardItem = mAdapter.getItem(i);
				
				switch(dashboardItem.getItemViewType()) {
				case DASHBOARD_ITEM_TYPE_HEART_RATE:
					jsonArray.put(DASHBOARD_ITEM_TYPE_HEART_RATE);
					break;
				case DASHBOARD_ITEM_TYPE_METRIC:
					switch(dashboardItem.getDashboardType()) {
					case TYPE_STEPS:
						jsonArray.put(TYPE_STEPS);
						break;
					case TYPE_DISTANCE:
						jsonArray.put(TYPE_DISTANCE);
						break;
					case TYPE_CALORIES:
						jsonArray.put(TYPE_CALORIES);
						break;
					}
					break;
				case DASHBOARD_ITEM_TYPE_SLEEP:
					jsonArray.put(DASHBOARD_ITEM_TYPE_SLEEP);
					break;
				case DASHBOARD_ITEM_TYPE_WORKOUT:
					jsonArray.put(DASHBOARD_ITEM_TYPE_WORKOUT);
					break;
				case DASHBOARD_ITEM_TYPE_ACTIGRAPHY:
					jsonArray.put(DASHBOARD_ITEM_TYPE_ACTIGRAPHY);
					break;
				case DASHBOARD_ITEM_TYPE_LIGHT_EXPOSURE:
					jsonArray.put(DASHBOARD_ITEM_TYPE_LIGHT_EXPOSURE);
					break;
				}
			}
			
			try {
				jsonObject.put(MAC_ADDRESS, getLifeTrakApplication().getSelectedWatch().getMacAddress());
				jsonObject.put(ITEM_VIEW_TYPE, jsonArray);
				String itemTypes = jsonObject.toString();
				mPreferenceWrapper.setPreferenceStringValue(DASHBOARD_ITEM_JSON, itemTypes);
				mPreferenceWrapper.synchronize();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			try {
				if(mainActivity != null)
					mainActivity.arrangeDashboardItems();
			} catch(Exception e) {
				Log.e(TAG, "error: " + e.getMessage());
			}
		}
	};
	
	/**
	 * Method for setting the data per day
	 * @param date	The date of the record
	 */
	public void setDataWithDate(Date date, int pos) {
		if (date == null)
			date = new Date();

		if(!isAdded())
			return;

        synchronized (LOCK_OBJECT) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR) - 1900;

            long startTime = System.currentTimeMillis();

            final List<StatisticalDataHeader> dataHeaders = DataSource.getInstance(getActivity())
                    .getReadOperation()
                    .query("dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ? AND watchDataHeader == ?",
                            String.valueOf(day), String.valueOf(month), String.valueOf(year),
                            String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
                    .limit(1)
                    .getResults(StatisticalDataHeader.class);

            mWorkoutCount = 0;
            mSleepMinute = 0;


//            List<Goal> goals = DataSource.getInstance(getActivity())
//                    .getReadOperation()
//                    .query("watchGoal = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
//                    .orderBy("abs(date - " + date.getTime() + ")", SORT_ASC)
//                    .limit(1)
//                    .getResults(Goal.class);

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

            Goal goal = null;

            if(goals.size() > 0)
                goal = goals.get(0);

            final int activeTime = activeTimeForDate(date);

            if(dataHeaders.size() > 0) {
                StatisticalDataHeader statisticalDataHeader = dataHeaders.get(0);


				if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420){
					List<WorkoutHeader> workoutHeader = DataSource.getInstance(getActivity())
							.getReadOperation()
							.query("watchWorkoutHeader == ? AND dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ?",
									String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
									String.valueOf(day), String.valueOf(month), String.valueOf(year))
							.orderBy("_id", SORT_ASC)
							.getResults(WorkoutHeader.class);

					mWorkoutCount = workoutHeader.size();

					mWorkoutInfo.setWorkoutValue(workoutHeader.size());
				}
				else {

					List<WorkoutInfo> workoutInfos = DataSource.getInstance(getActivity())
							.getReadOperation()
							.query("watchWorkoutInfo == ? AND dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ?",
									String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
									String.valueOf(day), String.valueOf(month), String.valueOf(year))
							.orderBy("_id", SORT_ASC)
							.getResults(WorkoutInfo.class);


					List<WorkoutInfo> spillOverWorkoutInfos = getSpillOverWorkoutFromPreviousDay(date);
					if (spillOverWorkoutInfos != null && spillOverWorkoutInfos.size() > 0) {
						workoutInfos.addAll(spillOverWorkoutInfos);
					}

					//add today's workout
					if (workoutInfos.size() > 0) {
						//filter spill over for next day
						workoutInfos = getWorkoutInfosWithoutNextDaySpillOver(workoutInfos);
					}

					mWorkoutCount = workoutInfos.size();

					mWorkoutInfo.setWorkoutValue(workoutInfos.size());
				}

				final List<Number> avgHeartRates = DataSource.getInstance(getActivity())
						.getReadOperation()
						.query("dataHeaderAndPoint == ? AND averageHR > 0", String.valueOf(statisticalDataHeader.getId()))
						.getResults(StatisticalDataPoint.class, "avg(averageHR) averageHR");

				avgAverage.clear();

                if(avgHeartRates.size() > 0) {
                    float averageHeartRate = avgHeartRates.get(0).floatValue();
                    mAverageHeartRate.setValue(averageHeartRate);
                    mAverageHeartRate.setPercent(percentForAverageBPM((int) averageHeartRate));

					if (averageHeartRate> 0 )
					avgAverage.add(averageHeartRate);

					//if (averageHeartRate == 0){
						if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420) {
							List<WorkoutHeader> workoutHeader = DataSource.getInstance(getActivity())
									.getReadOperation()
									.query("watchWorkoutHeader == ? AND dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ?",
											String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
											String.valueOf(day), String.valueOf(month), String.valueOf(year))
									.orderBy("_id", SORT_ASC)
									.getResults(WorkoutHeader.class);
							if (workoutHeader.size() > 0) {
								float avgHeartRate = 0;
								float sum = 0;
								float sumLength = 0;
								for (int b = 0; b < workoutHeader.size(); b++) {
									trueValuesHR.clear();
									WorkoutHeader workoutHeader1 = workoutHeader.get(b);
									String heartRate = workoutHeader1.getHeaderHeartRate();
									int hrLogRate = workoutHeader1.getLogRateHR();
									int hour = workoutHeader1.getTimeStampHour();
									int minute = workoutHeader1.getTimeStampMinute();
									int sec = workoutHeader1.getTimeStampSecond();
									try {
										JSONArray jsonArray = new JSONArray(heartRate);

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


										float sum10Min = 0;
										float average10Min = 0;



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


											for (int y = 0; y < valueEnding; y++){
												if (numbers[y + valueOverAll] > 0){
													sum10Min = sum10Min + numbers[y + valueOverAll];

													index++;
												}
											}
											valueOverAll = valueOverAll + valueEnding;
											average10Min = sum10Min / index;
											avgAverage.add((float)average10Min);
											sum10Min = 0;
											index = 0;
										}

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
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
									mAverageHeartRate.setValue(sumTotalAVG);
									if (sumTotalAVG > 0)
										mAverageHeartRate.setPercent(percentForAverageBPM((int) sumTotalAVG));
								}
								else {
									mAverageHeartRate.setValue(0);
								}
							}
						}
					//}


                } else {
					if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420){
						List<WorkoutHeader> workoutHeader = DataSource.getInstance(getActivity())
								.getReadOperation()
								.query("watchWorkoutHeader == ? AND dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ?",
										String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
										String.valueOf(day), String.valueOf(month), String.valueOf(year))
								.orderBy("_id", SORT_ASC)
								.getResults(WorkoutHeader.class);
						if (workoutHeader.size() > 0){
							float avgHeartRate = 0;
							float sum = 0;
							float sumLength = 0;
							for (int b = 0; b < workoutHeader.size(); b++) {
								trueValuesHR.clear();
								WorkoutHeader workoutHeader1 = workoutHeader.get(b);
								String heartRate = workoutHeader1.getHeaderHeartRate();
								int hrLogRate = workoutHeader1.getLogRateHR();
								int hour = workoutHeader1.getTimeStampHour();
								int minute = workoutHeader1.getTimeStampMinute();
								int sec = workoutHeader1.getTimeStampSecond();
								try {
									JSONArray jsonArray = new JSONArray(heartRate);

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


									float sum10Min = 0;
									float average10Min = 0;



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
										avgAverage.add(average10Min);
									}

								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
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
								mAverageHeartRate.setValue(sumTotalAVG);
								if (sumTotalAVG > 0)
									mAverageHeartRate.setPercent(percentForAverageBPM((int) sumTotalAVG));
							}
							else {
								mAverageHeartRate.setValue(0);
							}
						}
						else{
							mAverageHeartRate.setValue(0);
						}
					}
					else
                   	 mAverageHeartRate.setValue(0);
                }

                mActigraphy.setHourValue(activeTime / 60);
                mActigraphy.setMinuteValue(activeTime % 60);

                String query = "select sum(distance) distance, sum(steps) steps, sum(calorie) calorie from StatisticalDataPoint sdp " +
                        "inner join StatisticalDataHeader sdh on sdp.dataHeaderAndPoint = sdh._id " +
                        "where watchDataHeader == ? AND dateStampDay == ? AND dateStampMonth == ? AND dateStampYear == ?";

                Cursor cursor = DataSource.getInstance(getActivity())
                        .getReadOperation()
                        .rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
                                String.valueOf(day), String.valueOf(month), String.valueOf(year));

                if (cursor.moveToFirst()) {
                    mCalorieMetric.setProgressGoal(goal.getCalorieGoal());
                    mCalorieMetric.setProgressValue(cursor.getFloat(cursor.getColumnIndex("calorie")));

                    mStepsMetric.setProgressGoal(goal.getStepGoal());
                    mStepsMetric.setProgressValue(cursor.getFloat(cursor.getColumnIndex("steps")));

                    mDistanceMetric.setProgressGoal((float) goal.getDistanceGoal());
                    mDistanceMetric.setProgressValue(cursor.getFloat(cursor.getColumnIndex("distance")));
                    mDistanceMetric.setUnitSystem(getLifeTrakApplication().getUserProfile().getUnitSystem());
                }





                mSleepMetric.setProgressValue(statisticalDataHeader.getTotalSleep());

                if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
					if (pos == 1) {
						mLightExposure.setProgressValue(statisticalDataHeader.getLightExposure());
						if (goal.getBrightLightGoal() < 10) {
							goal.setBrightLightGoal(10);
						}
						mLightExposure.setProgressGoal(goal.getBrightLightGoal());
					}

                }

                mSleepMetric.setProgressValue(mTotalSleepTime);
            } else {
                mAverageHeartRate.setValue(0);
                mStepsMetric.setProgressValue(0);
                mDistanceMetric.setProgressValue(0);
                mCalorieMetric.setProgressValue(0);
                mSleepMetric.setProgressValue(0);
                mWorkoutInfo.setWorkoutValue(0);
				mActigraphy.setHourValue(0);
                mActigraphy.setMinuteValue(0);
                mSleepMetric.setProgressValue(0);
                mLightExposure.setProgressValue(0);
            }

            if (mSleepMetric != null && goal != null)
                mSleepMetric.setProgressGoal(goal.getSleepGoal());

            mAverageHeartRate.setDate(date);
            mSleepMetric.setDate(date);
            mWorkoutInfo.setDate(date);
            mActigraphy.setDate(date);

            mAdapter.notifyDataSetChanged();
        }
	
		
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DashboardItem dashboardItem = mAdapter.getItem(position);
		
		MainActivity mainActivity = (MainActivity) getActivity();
		
		if(dashboardItem.getDashboardType() == TYPE_CALORIES ||
				dashboardItem.getDashboardType() == TYPE_DISTANCE ||
				dashboardItem.getDashboardType() == TYPE_STEPS) {
			Bundle bundle = new Bundle();
			bundle.putInt(DASHBOARD_TYPE, dashboardItem.getDashboardType());
			mainActivity.switchFragment2(FragmentFactory.newInstance(FitnessResultsFragment.class, bundle));
		} else if(dashboardItem.getDashboardType() == TYPE_ACTIGRAPHY) {
//			if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420)
//				mainActivity.switchFragment2(FragmentFactory.newInstance(ActigraphyFragmentR420.class));
//			else
				mainActivity.switchFragment2(FragmentFactory.newInstance(ActigraphyFragment.class));
		} else if(dashboardItem.getDashboardType() == TYPE_WORKOUT) {

				if (mWorkoutCount > 0) {
					if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R415) {
						mainActivity.switchFragment(FragmentFactory.newInstance(WorkoutGraphFragment.class));
					}
					else if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420){
						mainActivity.switchFragment(FragmentFactory.newInstance(WorkoutGraphFragmentR420.class));
					}
					else {
						mainActivity.switchFragment(FragmentFactory.newInstance(WorkoutFragment.class));
					}
				}

		} else if(dashboardItem.getDashboardType() == TYPE_HEART_RATE) {
			if (getLifeTrakApplication().getSelectedWatch().getModel() == WATCHMODEL_R420)
				mainActivity.switchFragment2(FragmentFactory.newInstance(HeartRateFragmentR420.class));
			else
				mainActivity.switchFragment2(FragmentFactory.newInstance(HeartRateFragment.class));
		} else if(dashboardItem.getDashboardType() == TYPE_SLEEP) {
			mainActivity.switchFragment2(FragmentFactory.newInstance(SleepDataFragment.class));
		}
		else if(dashboardItem.getDashboardType() == TYPE_LIGHT_EXPOSURE) {
			mainActivity.switchFragment2(FragmentFactory.newInstance(LightPlotPagerFragment.class));
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
	
	private int activeTimeForDate(Date date) {
		Calendar calendarYesterday = Calendar.getInstance();
		calendarYesterday.setTime(date);
		calendarYesterday.add(Calendar.DAY_OF_MONTH, -1);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		int activeTime = 0;
		
		int dayYesterday = calendarYesterday.get(Calendar.DAY_OF_MONTH);
		int monthYesterday = calendarYesterday.get(Calendar.MONTH) + 1;
		int yearYesterday = calendarYesterday.get(Calendar.YEAR) - 1900;
		
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;
		
		List<SleepDatabase> sleepDatabasesYesterday = DataSource.getInstance(getActivity())
																.getReadOperation()
																.query("watchSleepDatabase = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ?", 
																		String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
																		String.valueOf(yearYesterday), String.valueOf(monthYesterday), String.valueOf(dayYesterday))
																.getResults(SleepDatabase.class);

		List<SleepDatabase> sleepDatabasesNow = DataSource.getInstance(getActivity())
															.getReadOperation()
															.query("watchSleepDatabase = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ?", 
																	String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
																	String.valueOf(year), String.valueOf(month), String.valueOf(day))
															.getResults(SleepDatabase.class);
		
		List<Integer> sleepIndexes = new ArrayList<Integer>();

        mTotalSleepTime = 0;

		for(SleepDatabase sleepDatabase : sleepDatabasesYesterday) {
			int sleepStart = (sleepDatabase.getHourSleepStart() * 6) + (sleepDatabase.getMinuteSleepStart() / 10);
			int sleepEnd = (sleepDatabase.getHourSleepEnd() * 6) + (sleepDatabase.getMinuteSleepEnd() / 10);

			/*if(sleepEnd < sleepStart) {
				for(int i=0;i<sleepEnd;i++) {
					sleepIndexes.add(i);
				}
			}*/
			if (sleepStart > sleepEnd) {
				for (int i=0;i<=sleepEnd;i++) {
					sleepIndexes.add(i);
				}
			}
			
			if(sleepDatabase.getHourSleepEnd() >= 15) {
				mSleepMinute += sleepDatabase.getSleepDuration();
			}

            if (sleepEnd >= sleepStart && sleepDatabase.getHourSleepEnd() >= 15) {
                mTotalSleepTime += sleepDatabase.getSleepDuration();
            } else if (sleepDatabase.getHourSleepStart() > sleepDatabase.getHourSleepEnd()) {
                mTotalSleepTime += sleepDatabase.getSleepDuration();
            }
		}
		
		for(SleepDatabase sleepDatabase : sleepDatabasesNow) {
			int sleepStart = (sleepDatabase.getHourSleepStart() * 6) + (sleepDatabase.getMinuteSleepStart() / 10);
			int sleepEnd = (sleepDatabase.getHourSleepEnd() * 6) + (sleepDatabase.getMinuteSleepEnd() / 10);
			sleepEnd = (sleepEnd >= 144 - 1) ? 144 - 1 : sleepEnd;
			sleepEnd = (sleepEnd <= sleepStart) ? 144 - 1 : sleepEnd;
			
			/*for (int i=(int)sleepStart;i<=sleepEnd;i++) {
				sleepIndexes.add(i);
			}*/

            if(sleepEnd > sleepStart) {
                for(int i=(int)sleepStart;i < (int)sleepEnd;i++) {
                    sleepIndexes.add(i);
                }
            } else if(sleepStart > sleepEnd) {
                for(int i=(int)sleepStart;i<144;i++) {
                    sleepIndexes.add(i);
                }
            }
			
			if(sleepDatabase.getHourSleepEnd() < 15 || 
					(sleepDatabase.getHourSleepEnd() <= 15) && (sleepDatabase.getMinuteSleepEnd() == 0)) {
				mSleepMinute += sleepDatabase.getSleepDuration();
			}

            if ((sleepStart < sleepEnd) && sleepDatabase.getHourSleepStart() < 15 && sleepDatabase.getHourSleepEnd() < 15) {
                mTotalSleepTime += sleepDatabase.getSleepDuration();
            }
			
			/*if (sleepDatabase.getHourSleepEnd() > sleepDatabase.getHourSleepStart() ||
					(sleepDatabase.getHourSleepEnd() == sleepDatabase.getHourSleepStart() &&
					sleepDatabase.getMinuteSleepEnd() > sleepDatabase.getMinuteSleepStart())) {
				int start = sleepDatabase.getHourSleepStart() * 60 + sleepDatabase.getMinuteSleepStart();
				int end = sleepDatabase.getHourSleepEnd() * 60 + sleepDatabase.getMinuteSleepEnd();
				
				mTotalSleepTime += (end - start);
			} else {
				mTotalSleepTime += ((24 * 60) - (sleepDatabase.getHourSleepStart() * 60 + sleepDatabase.getMinuteSleepStart()));
				mTotalSleepTime += (sleepDatabase.getHourSleepEnd() * 60 + sleepDatabase.getMinuteSleepEnd());
			}*/
		}
		
		String query = "select (sleepPoint02+sleepPoint24+sleepPoint46+sleepPoint68+sleepPoint810) " +
							"from StatisticalDataPoint dataPoint inner join StatisticalDataHeader dataHeader " +
								"on dataPoint.dataHeaderAndPoint = dataHeader._id where watchDataHeader = ? and dateStampYear = ? " +
								"and dateStampMonth = ? and dateStampDay = ?";

		Cursor cursor = DataSource.getInstance(getActivity())
							.getReadOperation()
							.rawQuery(query, String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
									String.valueOf(year), String.valueOf(month), String.valueOf(day));
		
		int index = 0;
		
		while(cursor.moveToNext()) {
			int value = cursor.getInt(0);
			
			if(!sleepIndexes.contains(index) && value >= 40 * 5) {
				activeTime++;
			}
			
			index++;
		}

        activeTime = activeTime * 10;
		
		cursor.close();
		
		return activeTime;
	}
	
	public void setDate(Date date) {
		mDate = date;
	}
	
	public void disableDashboard() {
		mDashboardItemList.setEnabled(false);
	}
	
	public void enableDashboard() {
		if (mDashboardItemList != null) {
			mDashboardItemList.setEnabled(true);
		}
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
			lastWorkout.setHour((86400-startTime)/3600);
			lastWorkout.setMinute(((86400-startTime)%3600)/60);
			lastWorkout.setSecond((86400-startTime)%60);
//			lastWorkout.setHundredths(0);
			workouts.remove(workouts.indexOf(workoutInfo));
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
		return spillOverWorkoutInfos;
	
	}
	
	//in seconds
	private int getEndTime(WorkoutInfo workoutInfo){
		int endTime = getStartTime(workoutInfo) + workoutInfo.getHour()*3600 + workoutInfo.getMinute()*60 + workoutInfo.getSecond();
		if(workoutInfo.getWorkoutStopInfos() != null){
			for (WorkoutStopInfo workoutStop : workoutInfo.getWorkoutStopInfos()) {
				endTime += (workoutStop.getStopHours()*3600) + (workoutStop.getStopMinutes()*60) + workoutStop.getStopSeconds();
			}
		}
		return endTime;
	}
	
	//in seconds
	private int getStartTime(WorkoutInfo workoutInfo){
		int hr = workoutInfo.getTimeStampHour();
		int min = workoutInfo.getTimeStampMinute();
		int sec = workoutInfo.getTimeStampSecond();
		return (hr*3600) + (min*60) + sec;
	}
	
	
}
