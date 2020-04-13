package com.salutron.lifetrakwatchapp.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.WakeupSetting;

public class WakeUpAlertFragment extends BaseFragment implements OnClickListener, OnCheckedChangeListener {
	private Switch switchStatus;
	private TextView textviewWakeUpCall;
	private EditText edittexttime;
	private TextView minuteLabel;
	private TextView wakeupLabel;
	private TextView intLabel;

	TimePickerDialog mTimePicker;

	private List<WakeupSetting> wakeSettings ;
	private WakeupSetting mWakeupSetting;

	private boolean flag_ischange = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_wake_up_alert, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		switchStatus = (Switch) getView().findViewById(R.id.switchStatus);	
		textviewWakeUpCall = (TextView) getView().findViewById(R.id.textviewWakeUpCall);
		edittexttime = (EditText) getView().findViewById(R.id.edittexttime);
		minuteLabel = (TextView) getView().findViewById(R.id.textView5);
		wakeupLabel = (TextView) getView().findViewById(R.id.textView2);
		intLabel = (TextView) getView().findViewById(R.id.textView3);

		intializeData();

		textviewWakeUpCall.setOnClickListener(this);
		switchStatus.setOnCheckedChangeListener(this);
		edittexttime.addTextChangedListener(new TextWatcher(){
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
			@Override
			public void afterTextChanged(Editable s) {
				flag_ischange = true;
				
				try{
					if (Integer.parseInt(s.toString().trim()) > 59)
						edittexttime.setText("59");
					
					if (Integer.parseInt(s.toString()) <= 1)
						minuteLabel.setText(getString(R.string.minutes_earlier));
					else
						minuteLabel.setText(getString(R.string.minutes_earlier));
				}
				catch (Exception e){
					
				}
			}
		}); 
	}

	private void SelectDate(final View v){
		Calendar mcurrentTime = Calendar.getInstance();
		int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
		int minute = mcurrentTime.get(Calendar.MINUTE);
		
		if (mWakeupSetting != null) {
			hour = mWakeupSetting.getWakeupTimeHour();
			minute = mWakeupSetting.getWakeupTimeMinute();
		}
		
		boolean is24hours = false;
		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
		case TIME_FORMAT_12_HR:
			is24hours = false;
			break;
		case TIME_FORMAT_24_HR:
			is24hours = true;
			break;					
		}

		mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				int mHour=hourOfDay;
				int mMin=minute;
				String AM_PM ;
				if(hourOfDay < 12) {
					AM_PM = getString(R.string.am);

				} else {
					AM_PM = getString(R.string.pm);
					mHour=mHour-12;
				}
				
				if (mWakeupSetting != null) {
					mWakeupSetting.setWakeupTimeHour(hourOfDay);
					mWakeupSetting.setWakeupTimeMinute(minute);
				}
				
				flag_ischange = true;
				textviewWakeUpCall.setText(changeTimeFormat("" + mHour + ":" + String.format("%02d", mMin) + " "+AM_PM));
			}
		}, hour, minute, is24hours);
		mTimePicker.setTitle(getString(R.string.select_time));
		mTimePicker.show();
	}

	private String convertMinuteToTime(int totalMinutesInt){


		int hours = totalMinutesInt / 60;
		int hoursToDisplay = hours;

		if (hours > 12) {
			hoursToDisplay = hoursToDisplay - 12;
		}

		int minutesToDisplay = totalMinutesInt - (hours * 60);

		String minToDisplay = null;
		if(minutesToDisplay == 0 ) minToDisplay = "00";     
		else if( minutesToDisplay < 10 ) minToDisplay = "0" + minutesToDisplay ;
		else minToDisplay = "" + minutesToDisplay ;

		String displayValue = hoursToDisplay + ":" + minToDisplay;

		if (hours < 12)
			displayValue = displayValue + " " + getString(R.string.am);
		else
			displayValue = displayValue + " " + getString(R.string.pm);

		return displayValue;
	}
	
	@SuppressLint("SimpleDateFormat")
	public String changeTimeFormat(String time) {
		String returnValue = "";
		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
		case TIME_FORMAT_12_HR:
			returnValue = time;
			break;
		case TIME_FORMAT_24_HR:
			SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
			SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
			try {
				returnValue = displayFormat.format(parseFormat.parse(time));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		}
		return returnValue;
	}

	@SuppressLint("SimpleDateFormat")
	private Date returnDate24Hours(String time){
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");	
		try {
			return dateFormat.parse(time);
		} catch (ParseException e) {
			return new Date();
		}
	}


	private void intializeData(){
		wakeSettings = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchWakeupSetting = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(WakeupSetting.class);

		if (wakeSettings.size() > 0) {
			mWakeupSetting = wakeSettings.get(0);
			mWakeupSetting.update();

			//textviewWakeUpCall.setText(changeTimeFormat(convertMinuteToTime(mWakeupSetting.getTime())));
			
			switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				if (mWakeupSetting.getWakeupTimeHour() >= 12) {
					textviewWakeUpCall.setText(String.format("%02d:%02d " + getString(R.string.pm),
							mWakeupSetting.getWakeupTimeHour() - 12, mWakeupSetting.getWakeupTimeMinute()));
				} else {
					textviewWakeUpCall.setText(String.format("%02d:%02d " + getString(R.string.am),
							mWakeupSetting.getWakeupTimeHour(), mWakeupSetting.getWakeupTimeMinute()));
				}
				break;
			case TIME_FORMAT_24_HR:
				textviewWakeUpCall.setText(String.format("%02d:%02d", mWakeupSetting.getWakeupTimeHour(), mWakeupSetting.getWakeupTimeMinute()));
				break;
			}
			
			edittexttime.setText(""+ mWakeupSetting.getSnoozeTime());
			switchStatus.setChecked(mWakeupSetting.isEnabled());

		} else 
			initialData();
		
		if (edittexttime.getText().toString().trim().equals("0") || edittexttime.getText().toString().trim().equals("1"))
			minuteLabel.setText(getString(R.string.minute_earlier)); 
		else
			minuteLabel.setText(getString(R.string.minutes_earlier));
		
		if (switchStatus.isChecked())
			showViews(true);
		else
			showViews(false);
	}
	
	private void showViews(boolean mboolean){
		textviewWakeUpCall.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		edittexttime.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		minuteLabel.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		wakeupLabel.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		intLabel.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
	}

	private void initialData(){

		textviewWakeUpCall.setText(changeTimeFormat("9:00 " + getString(R.string.pm)));

		mWakeupSetting = new WakeupSetting(getActivity());
	}

	@SuppressLint("SimpleDateFormat")
	private Date returnDate(String time){
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");	
		try {
			return dateFormat.parse(time);
		} catch (ParseException e) {
			return new Date();
		}
	}

	private void syncData(){

		Date fDate = null;
		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
		case TIME_FORMAT_12_HR:
			 fDate = returnDate(textviewWakeUpCall.getText().toString());
			break;
		case TIME_FORMAT_24_HR:
			 fDate = returnDate24Hours(textviewWakeUpCall.getText().toString());
			break;
		}
		
		Calendar fcalendar = Calendar.getInstance();
		fcalendar.setTime(fDate);
		int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
		int minutes = fcalendar.get(Calendar.MINUTE);

		minutes = (fhours*60) + minutes;
		
		int snoozeTime = 0;
		if (!edittexttime.getText().toString().trim().equals(""))
			snoozeTime = Integer.parseInt(edittexttime.getText().toString().trim());
		
		mWakeupSetting.setSnoozeTime(snoozeTime); 
		mWakeupSetting.setEnabled(switchStatus.isChecked());
		mWakeupSetting.setSnoozeEnabled(switchStatus.isChecked());
		mWakeupSetting.setTime(minutes);
		mWakeupSetting.setWatch(getLifeTrakApplication().getSelectedWatch());

		if (wakeSettings.size() > 0) {
			mWakeupSetting.update();
		} else {
			mWakeupSetting.insert();
		}

	}



	@Override
	public void onDetach() {
		super.onDetach();
		if(isRemoving()){
			if (flag_ischange){
				syncData();
			}
		}
	}

    @Override
	public void onDestroyView() {
        super.onDestroyView();

        if (flag_ischange)
            syncData();
    }


	@Override
	public void onClick(View v) { 
		switch (v.getId()){
		case R.id.textviewWakeUpCall:
			SelectDate(v);
			break;
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()){
		case R.id.switchStatus:
			flag_ischange = true;
			showViews(switchStatus.isChecked());
			// Toast.makeText(getActivity(), "The Switch is " + (isChecked ? "on" : "off"),Toast.LENGTH_SHORT).show();
			break;
		}
	}

}
