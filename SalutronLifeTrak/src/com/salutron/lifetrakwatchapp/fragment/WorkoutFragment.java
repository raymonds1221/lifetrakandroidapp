package com.salutron.lifetrakwatchapp.fragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.ViewSwitcher;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.WorkoutInfo;
import com.salutron.lifetrakwatchapp.adapter.WorkoutAdapter;
import com.salutron.lifetrakwatchapp.util.CalendarDateChangeListener;

import roboguice.inject.InjectView;

public class WorkoutFragment extends BaseFragment implements CalendarDateChangeListener {
	@InjectView(R.id.swtWorkout)
	private ViewSwitcher mWorkoutSwitcher;
	@InjectView(R.id.lstWorkout) private ListView mWorkoutList;
	private final List<WorkoutInfo> mWorkoutInfos = new ArrayList<WorkoutInfo>();
	private WorkoutAdapter mAdapter;
	private MainActivity mMainActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_workout, null);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initializeObjects();
	}
	
	private void initializeObjects() {
		mAdapter = new WorkoutAdapter(getActivity(), R.layout.adapter_workout, mWorkoutInfos);
		mWorkoutList.setAdapter(mAdapter);
		mMainActivity = (MainActivity) getActivity();
		
		setDataForDate(getLifeTrakApplication().getCurrentDate());
	}
	
	private void setDataForDate(Date date) {
		/*Date now = new Date();
		
		if(date.after(now)) {
			getLifeTrakApplication().setCurrentDate(now);
			mMainActivity.setCalendarLabel(now);
			return;
		}*/
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;
		
		List<WorkoutInfo> workoutInfos = DataSource.getInstance(getActivity())
													.getReadOperation()
													.query("watchWorkoutInfo = ? and dateStampYear = ? and dateStampMonth = ? and dateStampDay = ?", 
															String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()),
															String.valueOf(year), String.valueOf(month), String.valueOf(day))
													.orderBy("timeStampHour, timeStampMinute, timeStampSecond", "")
													.getResults(WorkoutInfo.class);
		
		if(workoutInfos.size() > 0) {
			mWorkoutInfos.clear();
			mWorkoutInfos.addAll(workoutInfos);
			mWorkoutSwitcher.setDisplayedChild(0);
		} else {
			mWorkoutSwitcher.setDisplayedChild(1);
		}
		
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onCalendarDateChange(Date date) {
		setDataForDate(date);
	}

	@Override
	public void onCalendarWeekChange(Date from, Date to) {
		
	}

	@Override
	public void onCalendarMonthChange(Date from, Date to) {
		
	}

	@Override
	public void onCalendarYearChange(int year) {
		
	}
}
