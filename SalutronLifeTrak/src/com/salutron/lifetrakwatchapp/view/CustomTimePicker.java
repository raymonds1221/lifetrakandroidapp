package com.salutron.lifetrakwatchapp.view;

import android.app.TimePickerDialog;
import android.content.Context;

public class CustomTimePicker extends TimePickerDialog {
	private final int TIME_INTERVAL = 10;

	public CustomTimePicker(Context context, int theme,
			OnTimeSetListener callBack, int hourOfDay, int minute,
			boolean is24HourView) {
		super(context, theme, callBack, hourOfDay, minute, is24HourView);
	}
	
	

}
