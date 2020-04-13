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
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.Goal;

public class GoalItemSteps implements GoalItem {

	private int value;
	private int minValue = 100;
	private int maxValue = 10000;
	private String nameOfMeasurement = "";
	private boolean progressManualChange;
	private View view;
	private ViewHolder viewHolder;

	private String stepsValue = "";
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
	
	public void setProgressManualChange(boolean progressManualChange) {
		this.progressManualChange = progressManualChange;
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

		viewHolder.title.setText(R.string.steps);
		viewHolder.value.setText(String.valueOf(value));
		viewHolder.valueEdit.setText(String.valueOf(value));
		viewHolder.value.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (LifeTrakApplication.goalItem != null) {
					LifeTrakApplication.goalItem.applyValueFromEditor();
				}

				LifeTrakApplication.goalItem = GoalItemSteps.this;

				final InputMethodManager imm = (InputMethodManager) view
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				final EditText editor = (EditText) view
						.findViewById(R.id.goal_edit_value);
				stepsValue = viewHolder.value.getText().toString();
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
								editor.setText(stepsValue);
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

					}
				});
				
				editor.setOnKeyListener(new View.OnKeyListener() {

					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_ENTER) {
							applyValueFromEditor(viewHolder, imm, editor);
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

		viewHolder.valueSeeker.setMax(maxValue);
		viewHolder.valueSeeker
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						
						if(progress < minValue) {
							progress = minValue;
						}

						if (progress > maxValue) {
							progress = maxValue;
						}
						
						setValue(progress);
						//updateGoal(view.getContext(), value);
						viewHolder.value.setText(Integer.toString(progress));
						viewHolder.valueEdit.setText(String.valueOf(progress));
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
						progressManualChange = true;
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
						progressManualChange = true;
					}
				});
		viewHolder.valueSeeker.setProgress(value);
		viewHolder.valueSeeker.setProgressDrawable(view.getResources()
				.getDrawable(R.drawable.asset_goals_1_steps_bar1));
		viewHolder.valueSeeker.setThumb(view.getResources().getDrawable(
				R.drawable.selector_goal_steps));

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
			goal.setStepGoal((long) value);
			goal.update();
		} else {
			Goal goal = new Goal(context);
			goal.setStepGoal((long) value);
			goal.setDate(calendar.getTime());
			goal.setDateStampDay(day);
			goal.setDateStampMonth(month);
			goal.setDateStampYear(year);
			goal.insert();
		}
	}

	private void applyValueFromEditor(final ViewHolder viewHolder,
			final InputMethodManager imm, final EditText editor) {
		imm.hideSoftInputFromWindow(editor.getWindowToken(), 0);

		try {
			final int value = Integer.parseInt(editor.getText().toString());
			editor.setVisibility(View.GONE);
			viewHolder.value.setVisibility(View.VISIBLE);

			if (value < minValue || value > maxValue)
				return;

			progressManualChange = false;
			viewHolder.value.setText(editor.getText());
			viewHolder.valueEdit.setText(String.valueOf(value));
			viewHolder.valueSeeker.setProgress(value);

			GoalItemSteps.this.value = value;
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

			progressManualChange = false;
			viewHolder.value.setText(String.valueOf(value));
			//viewHolder.valueEdit.setText(String.valueOf(value));
			viewHolder.valueSeeker.setProgress(value);

			GoalItemSteps.this.value = value;
		} catch (NumberFormatException nfex) {
			//viewHolder.value.setVisibility(View.VISIBLE);
		}

		LifeTrakApplication.goalItem = null;
	}

	@Override
	public void applyValueFromEditor() {
		try {
			final InputMethodManager imm = (InputMethodManager) view
					.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			final EditText editor = (EditText) view
					.findViewById(R.id.goal_edit_value);

			applyValueFromEditor(viewHolder, imm, editor);
		} catch (Exception e) {
			Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}



}
