package com.salutron.lifetrakwatchapp.adapter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.TimePickerDialog;
import android.os.Handler;
import android.os.Parcel;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.EditText;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.view.CustomTimePickerDialog;

public class GoalItemSleep implements GoalItem {

	private int hours;
	private int minutes;
	private int minHours;
	private int maxHours;
	private int minMinutes;
	private int maxMinutes;
	private int sleepGoal;
	private TimeDate timeDate;
	private CustomTimePickerDialog mTimePickerDialog;
	private TimePickerDialog mTimePickerDialogNative;

	public GoalItemSleep(final TimeDate timeDate) {
		this.timeDate = timeDate;
	}

	public int getSleepGoal() {
		return sleepGoal;
	}

	public void setSleepGoal(int sleepGoal) {
		this.sleepGoal = sleepGoal;
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public int getMinHours() {
		return minHours;
	}

	public void setMinHours(int minHours) {
		this.minHours = minHours;
	}

	public int getMaxHours() {
		return maxHours;
	}

	public void setMaxHours(int maxHours) {
		this.maxHours = maxHours;
	}

	public int getMinMinutes() {
		return minMinutes;
	}

	public void setMinMinutes(int minMinutes) {
		this.minMinutes = minMinutes;
	}

	public int getMaxMinutes() {
		return maxMinutes;
	}

	public void setMaxMinutes(int maxMinutes) {
		this.maxMinutes = maxMinutes;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
	}

	@Override
	public void setDate(Date date) {
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		final View view = inflater.inflate(R.layout.adapter_goal_item, null);
		final ViewHolder viewHolder = new ViewHolder();

		viewHolder.title = (TextView) view.findViewById(R.id.goal_iteration);
		viewHolder.value = (TextView) view.findViewById(R.id.goal_value);
		viewHolder.valueMin = (TextView) view.findViewById(R.id.goal_value_min);
		viewHolder.valueMax = (TextView) view.findViewById(R.id.goal_value_max);
		viewHolder.valueSeeker = (SeekBar) view
				.findViewById(R.id.goal_value_seeker);
		viewHolder.valueEdit = (EditText) view.findViewById(R.id.goal_edit_value);

		final TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(final TimePicker view,
					final int selectedHour, final int selectedMinute) {
				if ((selectedHour < minHours || selectedHour > maxHours)
						|| (selectedMinute < minMinutes || selectedMinute > maxMinutes)) {
					return;
				}

				viewHolder.valueSeeker
						.setProgress(selectedHour * 60 + selectedMinute);

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						hours = selectedHour;
						minutes = selectedMinute;

						viewHolder.value.setText(String.format("%dh%dm",selectedHour, selectedMinute));
						
					}
				}, 500);
			}
		};

		viewHolder.title.setText(R.string.sleep);
		viewHolder.value.setText(String.format("%dh%dm", hours, minutes));
		viewHolder.value.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (LifeTrakApplication.goalItem != null) {
					LifeTrakApplication.goalItem.applyValueFromEditor();
				}

				LifeTrakApplication.goalItem = GoalItemSleep.this;
				if (hours == 0){
					hours = 7;
				}
//				mTimePickerDialogNative = new TimePickerDialog(v.getContext(), timePickerListener, hours, minutes, true);
//				mTimePickerDialogNative.show();
				mTimePickerDialog = new CustomTimePickerDialog(
						v.getContext(), timePickerListener, hours, minutes,
						true, timeDate);
				mTimePickerDialog.setValue(hours, minutes);
				mTimePickerDialog.show();
			}
		});

		viewHolder.valueMin.setText(String.format("%dh%dm", minHours,
				minMinutes));
		viewHolder.valueMax.setText(String.format("%dh%dm", maxHours,
				maxMinutes));
		viewHolder.valueSeeker.setMax((maxHours * 60) + 50);
		viewHolder.valueSeeker
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (progress < 60) {
							progress = 60;
						}

						final int step = 10;
						final int sleep = progress - progress % step;

						viewHolder.value.setText(String.format("%dh%dm",
								sleep / 60, sleep % 60));

						hours = sleep / 60;
						minutes = sleep % 60;
						
						setSleepGoal(sleep);
						//updateGoal(view.getContext(), sleep);
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
					}
				});

		int sleepGoal = this.sleepGoal;
		if (sleepGoal == 60)
			sleepGoal = 0;

		viewHolder.valueSeeker.setProgress(sleepGoal);
		viewHolder.valueSeeker.setProgressDrawable(view.getResources()
				.getDrawable(R.drawable.asset_goals_4_sleep_bar1));
		viewHolder.valueSeeker.setThumb(view.getResources().getDrawable(
				R.drawable.selector_goal_sleep));

		return view;
	}

	@Override
	public void updateGoal(Context context, double value) {
		LifeTrakApplication application = (LifeTrakApplication) context
				.getApplicationContext();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		List<Goal> goals = DataSource
				.getInstance(context)
				.getReadOperation()
				.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
						String.valueOf(application.getSelectedWatch().getId()),
						String.valueOf(day), String.valueOf(month),
						String.valueOf(year)).getResults(Goal.class);

		if (goals.size() > 0) {
			Goal goal = goals.get(0);
			goal.setContext(context);
			goal.setSleepGoal((int) value);
			goal.update();
		} else {
			Goal goal = new Goal(context);
			goal.setSleepGoal((int) value);
			goal.setDate(calendar.getTime());
			goal.setDateStampDay(day);
			goal.setDateStampMonth(month);
			goal.setDateStampYear(year);
			goal.insert();
		}
	}

	@Override
	public void applyValueFromEditor() {
		// TODO Auto-generated method stub

	}
}
