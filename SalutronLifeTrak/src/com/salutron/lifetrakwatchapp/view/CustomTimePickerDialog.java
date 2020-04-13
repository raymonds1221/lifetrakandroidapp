package com.salutron.lifetrakwatchapp.view;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.salutron.blesdk.SALTimeDate;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.model.TimeDate;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TimePicker;

public class CustomTimePickerDialog extends TimePickerDialog {

	private final static int TIME_PICKER_INTERVAL = 10;
	private TimePicker timePicker;
	private final OnTimeSetListener callback;
	private TimeDate timeDate;
	private NumberPicker mMinuteSpinner;
	private NumberPicker mHourSpinner;
	
	private int mHourValue;
	private int mMinuteValue;

	public CustomTimePickerDialog(Context context, OnTimeSetListener callBack,
			int hourOfDay, int minute, boolean is24HourView,
			final TimeDate timeDate) {
		super(context, callBack, hourOfDay, minute,
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

			mHourSpinner.setMinValue(1);
			mHourSpinner.setMaxValue(14);

			mMinuteSpinner.setMinValue(0);
			String minutes[] = {"00", "10", "20", "30", "40", "50"};
			mMinuteSpinner.setMaxValue((60 / TIME_PICKER_INTERVAL) - 1);
			//mMinuteSpinner.setMaxValue(minutes.length);
			List<String> displayedValues = new ArrayList<String>();
			for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
				displayedValues.add(String.format("%02d", i));
			}
			mMinuteSpinner.setDisplayedValues(displayedValues
					.toArray(new String[0]));

		//	mMinuteSpinner.setDisplayedValues(minutes);
			
			mHourSpinner.setValue(mHourValue);
			int val = mMinuteValue / 10;
			mMinuteSpinner.setValue(val);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void setValue(int hour, int minute) {
		mHourValue = hour;
		mMinuteValue = minute;
	}

	private int getMaxHour() {
		int max = 14;
		final int timeFormat = timeDate.getHourFormat();

		switch (timeFormat) {
		case SALTimeDate.FORMAT_12HOUR:
			max = 12;
			break;
		}

		return max;
	}
	
	private final NumberPicker.OnValueChangeListener mHourValueChanged = new NumberPicker.OnValueChangeListener() {
		
		@Override
		public void onValueChange(NumberPicker arg0, int oldVal, int newVal) {
			String title = String.format("%d:%02d", newVal, mMinuteSpinner.getValue() * 10);
			setTitle(title);
		}
	};
	
	private final NumberPicker.OnValueChangeListener mMinuteValueChanged = new NumberPicker.OnValueChangeListener() {
		
		@Override
		public void onValueChange(NumberPicker arg0, int oldVal, int newVal) {
			String title = String.format("%d:%02d", mHourSpinner.getValue(), newVal*10);
			setTitle(title);
		}
	};

}
