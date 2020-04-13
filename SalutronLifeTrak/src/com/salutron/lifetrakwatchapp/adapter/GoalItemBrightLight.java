package com.salutron.lifetrakwatchapp.adapter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.adapter.GoalItem.ViewHolder;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.TimeDate;
import com.salutron.lifetrakwatchapp.view.CustomTimePickerDialog;
import com.salutron.lifetrakwatchapp.view.CustomTimePickerDialogBrightLight;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class GoalItemBrightLight implements GoalItem {

	/*private int minTime;
	private int maxTime;*/
	private int hours;
	private int minutes;
	private int minHours;
	private int maxHours;
	private int minMinutes;
	private int maxMinutes;
	private TimeDate timeDate;
	private int brightLightGoal;
	private CustomTimePickerDialogBrightLight mTimePickerDialog;
	
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
	
	public GoalItemBrightLight(TimeDate timeDate) {
		this.timeDate = timeDate;
	}
	
	public int getBrightLightGoal() {
		return brightLightGoal;
	}

	public void setBrightLightGoal(int brightLightGoal) {
		this.brightLightGoal = brightLightGoal;
	}

	/*public int getMinTime() {
		return minTime;
	}
	

	public void setMinTime(int minTime) {
		this.minTime = minTime;
	}
	

	public int getMaxTime() {
		return maxTime;
	}
	

	public void setMaxTime(int maxTime) {
		this.maxTime = maxTime;
	}*/

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
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
		viewHolder.valueSeeker = (SeekBar) view.findViewById(R.id.goal_value_seeker);
		
		viewHolder.title.setText(R.string.bright_light_duration);
		/*viewHolder.valueMin.setText(String.valueOf(minTime));
		viewHolder.valueMax.setText(String.valueOf(maxTime));*/
		viewHolder.valueMin.setText(String.format("%dh10m", minHours));
		viewHolder.valueMax.setText(String.format("%dh0m", maxHours));
		viewHolder.value.setText(""+ getHours()+"h" +getMinutes()+ "m" );
		
		final TimePickerDialog.OnTimeSetListener BLtimePickerListener = new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(final TimePicker view,
					final int selectedHour, final int selectedMinute) {
				/*if ((selectedHour < minHours || selectedHour > maxHours)
						|| (selectedMinute < minMinutes || selectedMinute > maxMinutes)) {
					return;
				}*/
				System.out.println("selectedHour " + selectedHour);
				System.out.println("selectedMinute " + selectedMinute);
				viewHolder.valueSeeker.setProgress(selectedHour * 60 + selectedMinute);

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						hours = selectedHour;
						minutes = selectedMinute;

						viewHolder.value.setText(String.format("%dh%dm",selectedHour, selectedMinute));
					}
				}, 1000);
			}
		};
		viewHolder.valueSeeker.setMax((maxHours * 60));
		viewHolder.valueSeeker
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						/*if(progress < minTime) {
							progress = minTime;
						}

						if (progress > maxTime) {
							progress = maxTime;
						}

						setBrightLightGoal(progress);
						
						int mHours = progress / 60;
						int mMins = progress - (mHours * 60);
						
						viewHolder.value.setText(Integer.toString(mHours) + "h" + Integer.toString(mMins) + "m");*/
						if (progress < 10) {
							progress = 10;
						}

						final int step = 1;
						final int brightLight = progress - progress % step;

						viewHolder.value.setText(String.format("%dh%dm", brightLight / 60, brightLight % 60));

						hours = brightLight / 60;
						minutes = brightLight % 60;
						
						setBrightLightGoal(progress);
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
					}
				});
		
		viewHolder.valueSeeker.setProgress(brightLightGoal);
		viewHolder.valueSeeker.setProgressDrawable(view.getResources()
				.getDrawable(R.drawable.asset_goals_lightgraph_bar1));
		viewHolder.valueSeeker.setThumb(view.getResources().getDrawable(
				R.drawable.selector_goal_bright_light));
		viewHolder.value.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (LifeTrakApplication.goalItem != null) {
					LifeTrakApplication.goalItem.applyValueFromEditor();
				}

				LifeTrakApplication.goalItem = GoalItemBrightLight.this;

				mTimePickerDialog = new CustomTimePickerDialogBrightLight(
						v.getContext(), BLtimePickerListener, hours, minutes,
						true, timeDate);
				mTimePickerDialog.setValue(hours, minutes);
				mTimePickerDialog.show();
			}
		});
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
			goal.setBrightLightGoal((int) value);
			goal.update();
		} else {
			Goal goal = new Goal(context);
			goal.setBrightLightGoal((int) value);
			goal.setDate(calendar.getTime());
			goal.setDateStampDay(day);
			goal.setDateStampMonth(month);
			goal.setDateStampYear(year);
			goal.insert();
		}
	}

	@Override
	public void applyValueFromEditor() {
		//TODO
	}


}
