package com.salutron.lifetrakwatchapp.view;

import java.lang.reflect.Field;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import com.salutron.lifetrakwatchapp.model.TimeDate;

public class CustomTimePickerDialogBrightLight extends TimePickerDialog {

	private final static int TIME_PICKER_INTERVAL = 1;
	private TimePicker timePicker;
	private final OnTimeSetListener callback;
	private TimeDate timeDate;
	private NumberPicker mMinuteSpinner;
	private NumberPicker mHourSpinner;
	
	private int mHourValue;
	private int mMinuteValue;

	public CustomTimePickerDialogBrightLight(Context context, OnTimeSetListener callBack,
			int hourOfDay, int minute, boolean is24HourView,
			final TimeDate timeDate) {
		super(context, callBack, hourOfDay, minute / TIME_PICKER_INTERVAL,
				is24HourView);
		this.callback = callBack;
		this.timeDate = timeDate;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (callback != null && timePicker != null) {
			timePicker.clearFocus();
			callback.onTimeSet(timePicker, timePicker.getCurrentHour(),
					timePicker.getCurrentMinute() * TIME_PICKER_INTERVAL);
		}		
	}

	@Override
	protected void onStop() {
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		try {
			Class<?> classForid = Class.forName("com.android.internal.R$id");
			Field timePickerField = classForid.getField("timePicker");
			this.timePicker = (TimePicker) findViewById(timePickerField
					.getInt(null));
			Field field = classForid.getField("minute");
			Field field0 = classForid.getField("hour");

			mMinuteSpinner = (NumberPicker) timePicker
					.findViewById(field.getInt(null));
			mHourSpinner = (NumberPicker) timePicker
					.findViewById(field0.getInt(null));
			
			mMinuteSpinner.setOnValueChangedListener(mMinuteValueChanged);
			mHourSpinner.setOnValueChangedListener(mHourValueChanged);
			
			mHourSpinner.setMinValue(0);
			mHourSpinner.setMaxValue(2);

			mMinuteSpinner.setMinValue(0);
			mMinuteSpinner.setMaxValue(60);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void setValue(int hour, int minute) {
		mHourValue = hour;
		mMinuteValue = minute;
	}

	private final NumberPicker.OnValueChangeListener mHourValueChanged = new NumberPicker.OnValueChangeListener() {
		
		@Override
		public void onValueChange(NumberPicker arg0, int oldVal, int newVal) {
			String title = String.format("%d:%02d", newVal, mMinuteSpinner.getValue());
			if(newVal == 2)
				mMinuteSpinner.setMaxValue(0);
			else 
				mMinuteSpinner.setMaxValue(60);
			setTitle(title);
		}
	};
	
	private final NumberPicker.OnValueChangeListener mMinuteValueChanged = new NumberPicker.OnValueChangeListener() {
		
		@Override
		public void onValueChange(NumberPicker arg0, int oldVal, int newVal) {
			String title = String.format("%d:%02d", mHourSpinner.getValue(), newVal);
			if(mHourSpinner.getValue() == 0)
				mMinuteSpinner.setMinValue(10);
			else 
				mMinuteSpinner.setMaxValue(60);
			setTitle(title);
		}
	};

}
