package com.salutron.lifetrakwatchapp.adapter;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Parcel;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.adapter.GoalItem.ViewHolder;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;
import com.salutron.lifetrakwatchapp.view.BetterSeekBar;

public class GoalItemDistance implements GoalItem {

	private float value;
	private float minValue = 0.62f;
	private float maxValue = 62.14f;
	private String nameOfMeasurement = "mi";
	private View view;
	private ViewHolder viewHolder;

	public float getMinValue() {
		return minValue;
	}

	public void setMinValue(float minValue) {
		this.minValue = minValue;
	}

	public float getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(float maxValue) {
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
		
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
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

		viewHolder.title.setText(R.string.distance);
		viewHolder.value.setText(String.valueOf(value));
		viewHolder.valueMin.setText(String.valueOf(minValue));
		viewHolder.valueMax.setText(String.valueOf(maxValue));
		viewHolder.valueEdit.setText(String.valueOf(value));

		final BetterSeekBar betterSeekBar = (BetterSeekBar) viewHolder.valueSeeker;
		betterSeekBar.setMinimumValue(minValue);
		betterSeekBar.setMaximumValue(maxValue);

		final LifeTrakApplication application = (LifeTrakApplication) view
				.getContext().getApplicationContext();

		betterSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				final NumberFormat nf = new DecimalFormat("#,###.##");
				final float value = betterSeekBar.getFloatValue();
				setValue(value);
				
				viewHolder.value.setText(String.format("%s %s",
						nf.format(value), nameOfMeasurement));
				viewHolder.valueEdit.setText(String.valueOf(value));
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				final InputMethodManager imm = (InputMethodManager) view
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				final EditText editor = (EditText) view
						.findViewById(R.id.goal_edit_value);
				editor.setVisibility(View.GONE);
				viewHolder.value.setVisibility(View.VISIBLE);

				try {
					imm.hideSoftInputFromWindow(editor.getWindowToken(), 0);
				} catch (Exception e) {
				}
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		betterSeekBar.setFloatValue(value);
		viewHolder.valueSeeker.setProgressDrawable(view.getResources()
				.getDrawable(R.drawable.asset_goals_2_distance_bar1));
		viewHolder.valueSeeker.setThumb(view.getResources().getDrawable(
				R.drawable.selector_goal_distance));

		viewHolder.value.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (LifeTrakApplication.goalItem != null) {
					LifeTrakApplication.goalItem.applyValueFromEditor();
				}

				LifeTrakApplication.goalItem = GoalItemDistance.this;

				final InputMethodManager imm = (InputMethodManager) view
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				final EditText editor = (EditText) view
						.findViewById(R.id.goal_edit_value);

				editor.setText(String.format("%.2f", value));
				editor.setText("");
				editor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View view, boolean hasFocus) {
						if (hasFocus) {

						} else {
							if (!editor.toString().trim().equals("")) {
								applyValueFromEditor(betterSeekBar, viewHolder, editor.toString().replaceAll(",", ""));
								editor.setSelection(editor.getText().length());
							} else {
								editor.setText(String.format("%.2f", value));
								applyValueFromEditor(betterSeekBar, viewHolder, editor.toString().replaceAll(",", ""));
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
//							applyValueFromEditor(betterSeekBar,viewHolder, s.toString().replaceAll(",", ""));
//							editor.setSelection(editor.getText().length());
//						}
					}
				});
				
				editor.setOnKeyListener(new View.OnKeyListener() {

					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_ENTER) {
							applyValueFromEditor(betterSeekBar, imm, editor);
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
		
		viewHolder.value.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				try{
					if(!arg0.toString().isEmpty() && !Float.isNaN(Float.parseFloat(arg0.toString().replaceAll("(km|mi)", "")))) {
						float goalValue = Float.parseFloat(arg0.toString().replaceAll("(km|mi)", ""));

						if (application.getUserProfile().getUnitSystem() == SalutronLifeTrakUtility.UNIT_IMPERIAL) {
							goalValue = goalValue / SalutronLifeTrakUtility.MILE;
						}

						//updateGoal(view.getContext(), goalValue);
					}
				}catch (Exception e){
					LifeTrakLogger.info("Error on GoalItemDistance :"+ e.getLocalizedMessage());
				}

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
		});

		return view;
	}

	@Override
	public void updateGoal(Context context, double value) {
		LifeTrakApplication application = (LifeTrakApplication) context.getApplicationContext();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - 1900;

		List<Goal> goals = DataSource.getInstance(context)
										.getReadOperation()
										.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
												String.valueOf(application.getSelectedWatch().getId()),
												String.valueOf(day), String.valueOf(month),
												String.valueOf(year)).getResults(Goal.class);

		if (goals.size() > 0) {
			Goal goal = goals.get(0);
			goal.setContext(context);
			goal.setDistanceGoal(value);
			goal.update();
		} else {
			Goal goal = new Goal(context);
			goal.setDistanceGoal(value);
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

			applyValueFromEditor((BetterSeekBar) viewHolder.valueSeeker, imm,
					editor);
		} catch (Exception e) {
			Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	private void applyValueFromEditor(final BetterSeekBar betterSeekBar,
			final InputMethodManager imm, final EditText editor) {
		imm.hideSoftInputFromWindow(editor.getWindowToken(), 0);

		try {
			final float value = Float.parseFloat(editor.getText().toString());

			editor.setVisibility(View.GONE);
			viewHolder.value.setVisibility(View.VISIBLE);

			if (value < minValue || value > maxValue)
				return;

			viewHolder.value.setText(String.valueOf(value));
			viewHolder.valueEdit.setText(String.valueOf(value));
			betterSeekBar.setFloatValue(value);

			GoalItemDistance.this.value = value;
		} catch (NumberFormatException nfex) {
			editor.setVisibility(View.GONE);
			viewHolder.value.setVisibility(View.VISIBLE);
		}

		LifeTrakApplication.goalItem = null;
	}
	
	private void applyValueFromEditor(final BetterSeekBar betterSeekBar,final ViewHolder viewHolder,
			final String editor) {
		try {
			 float value = Integer.parseInt(editor);

			//viewHolder.value.setVisibility(View.VISIBLE);

//						if (value < minValue || value > maxValue)
//				return;

			if (value < minValue){
				value = minValue;
			}
			if (maxValue > value){
				value = maxValue;
			}

			//progressManualChange = false;
			viewHolder.value.setText(editor);
			GoalItemDistance.this.value = value;


		} catch (NumberFormatException nfex) {
			//viewHolder.value.setVisibility(View.VISIBLE);
		}

		LifeTrakApplication.goalItem = null;
	}


}
