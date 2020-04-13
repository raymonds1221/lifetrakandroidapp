package com.salutron.lifetrakwatchapp.adapter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.adapter.GoalItem.ViewHolder;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.Goal;

public class GoalItemCalories implements GoalItem {

	private int value;
	private int minValue = 100;
	private int maxValue = 5000;
	private String nameOfMeasurement = "kcal";
	private boolean add100;
	private View view;
	private ViewHolder viewHolder;

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public String getNameOfMeasurement() {
		return nameOfMeasurement;
	}

	public void setNameOfMeasurement(String nameOfMeasurement) {
		this.nameOfMeasurement = nameOfMeasurement;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDate(Date date) {
		// TODO Auto-generated method stub

	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void setProgressManualChange(boolean progressManualChange) {
		this.add100 = progressManualChange;
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		view = inflater.inflate(R.layout.adapter_goal_item, null);
		viewHolder = new ViewHolder();

		viewHolder.title = (TextView) view.findViewById(R.id.goal_iteration);
		viewHolder.value = (TextView) view.findViewById(R.id.goal_value);
		viewHolder.valueMin = (TextView) view.findViewById(R.id.goal_value_min);
		viewHolder.valueMax = (TextView) view.findViewById(R.id.goal_value_max);
		viewHolder.valueSeeker = (SeekBar) view
				.findViewById(R.id.goal_value_seeker);

		viewHolder.valueEdit = (EditText) view.findViewById(R.id.goal_edit_value);
		viewHolder.title.setText(R.string.calories);

		viewHolder.value.setText(String.valueOf(value));
		viewHolder.value.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (LifeTrakApplication.goalItem != null) {
					LifeTrakApplication.goalItem.applyValueFromEditor();
				}

				LifeTrakApplication.goalItem = GoalItemCalories.this;

				final InputMethodManager imm = (InputMethodManager) view
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				final EditText editor = (EditText) view
						.findViewById(R.id.goal_edit_value);

				//editor.setText("" + ((value == 0) ? minValue : value));
				editor.setText("");
				editor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View view, boolean hasFocus) {
						if (hasFocus){

						}
						else{
							if (!editor.toString().trim().equals("")){
								applyValueFromEditor(viewHolder, editor.toString());
								editor.setSelection(editor.getText().length());
							}
							else{
								editor.setText(value);
								applyValueFromEditor(viewHolder, editor.toString());
								editor.setSelection(editor.getText().length());
							}
						}
					}
				});

				editor.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {


					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {

					}

					@Override
					public void afterTextChanged(Editable s) {
//						if (!s.toString().equals("")){
//							applyValueFromEditor(viewHolder, s.toString());
//							editor.setSelection(editor.getText().length());
//						}
					}
				});


				editor.setOnKeyListener(new View.OnKeyListener() {

					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_ENTER) {
							applyValueFromEditor(imm, editor);
						}
						return false;
					}
				});
				viewHolder.value.setVisibility(View.INVISIBLE);
				editor.setVisibility(View.VISIBLE);
				editor.requestFocus();

				imm.showSoftInput(editor, InputMethodManager.SHOW_FORCED);
			}
		});

		viewHolder.valueMin.setText(String.valueOf(minValue));
		viewHolder.valueMax.setText(String.valueOf(maxValue));

		viewHolder.valueSeeker.setMax(maxValue/* - minValue*/);
		viewHolder.valueSeeker
		.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar,
					int progress, boolean fromUser) {

				if(progress < minValue) {
					progress = minValue;
				}

				setValue(progress);
				viewHolder.value.setText(String.format("%d %s",
						progress, nameOfMeasurement));
				viewHolder.valueEdit.setText(String.valueOf(progress));
				//updateGoal(view.getContext(), progress);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				add100 = true;

				final InputMethodManager imm = (InputMethodManager) view
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				final EditText editor = (EditText) view
						.findViewById(R.id.goal_edit_value);
				editor.setVisibility(View.GONE);
				viewHolder.value.setVisibility(View.VISIBLE);

				try {
					imm.hideSoftInputFromWindow(
							editor.getWindowToken(), 0);
				} catch (Exception e) {
				}
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		viewHolder.valueSeeker.setProgress(value);
		viewHolder.valueSeeker.setProgressDrawable(view.getResources()
				.getDrawable(R.drawable.asset_goals_3_calories_bar1));
		viewHolder.valueSeeker.setThumb(view.getResources().getDrawable(
				R.drawable.selector_goal_calorie));

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
			goal.setCalorieGoal((long) value);
			goal.update();
		} else {
			Goal goal = new Goal(context);
			goal.setCalorieGoal((long) value);
			goal.setDate(calendar.getTime());
			goal.setDateStampDay(day);
			goal.setDateStampMonth(month);
			goal.setDateStampYear(year);
			goal.insert();
		}
	}

	@Override
	public void applyValueFromEditor() {
		try {
			final InputMethodManager imm = (InputMethodManager) view
					.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			final EditText editor = (EditText) view
					.findViewById(R.id.goal_edit_value);

			applyValueFromEditor(imm, editor);
		} catch (Exception e) {
			Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_LONG)
			.show();
		}
	}

	private void applyValueFromEditor(final InputMethodManager imm,
			final EditText editor) {
		imm.hideSoftInputFromWindow(editor.getWindowToken(), 0);

		try {
			final int value = Integer.parseInt(editor.getText().toString());

			editor.setVisibility(View.GONE);
			viewHolder.value.setVisibility(View.VISIBLE);

			if (value < minValue || value > maxValue)
				return;

			add100 = false;
			viewHolder.value.setText(String.format("%d %s", value,
					nameOfMeasurement));
			viewHolder.valueSeeker.setProgress(value);
			viewHolder.valueEdit.setText(String.valueOf(value));
			GoalItemCalories.this.value = value;
		} catch (NumberFormatException nfex) {
			editor.setVisibility(View.GONE);
			viewHolder.value.setVisibility(View.VISIBLE);
		}

		LifeTrakApplication.goalItem = null;
	}

	private void applyValueFromEditor(final ViewHolder viewHolder,
			final String editor) {
		try {
			int value = Integer.parseInt(editor);

			//viewHolder.value.setVisibility(View.VISIBLE);

//			if (value < minValue || value > maxValue)
//				return;

			if (value < minValue){
				value = minValue;
			}
			if (maxValue > value){
				value = maxValue;
			}


			viewHolder.value.setText(String.format("%d %s", value,
					nameOfMeasurement));
			viewHolder.valueSeeker.setProgress(value);

			GoalItemCalories.this.value = value;
		} catch (NumberFormatException nfex) {
			//viewHolder.value.setVisibility(View.VISIBLE);
		}

		LifeTrakApplication.goalItem = null;
	}
}
