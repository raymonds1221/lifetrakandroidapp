package com.salutron.lifetrakwatchapp.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALNightLightDetectSetting;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.NightLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.util.DeviceScanListener;

public class NightLightAlertFragment extends BaseFragment implements OnCheckedChangeListener, DeviceScanListener {

	private static String TAG = NightLightAlertFragment.class.getCanonicalName();

	private Switch switchStatus;
	private TextView nightLightExposureLevel;
	private TextView nightLightDurationValueHour;
	private TextView nightLightDurationValueMin;
	private TextView nightLightStartTimeValue;
	private TextView nightLightEndTimeValue;

	private Dialog alertDialog;
	private TimePickerDialog mTimePicker;
	private boolean isStartTime = false;
	private boolean isEndTime = false;

	private NumberPicker nightLightNumberPickerHour;
	private NumberPicker nightLightNumberPickerMin;
	private String exposureDurationHourToDisplay = "0";
	private String exposureDurationMinToDisplay = "0";

	private boolean isdateshow = false;
	private List<NightLightDetectSetting> nightLightDetectSetting ;
	private NightLightDetectSetting mNightLightDetectSetting;
	private boolean flag_ischange= false;
	private boolean noError = false;

    private List<DayLightDetectSetting> dayLightDetecttSettings ;
    private DayLightDetectSetting mDayLightDetectSetting;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		Log.d(TAG, "OnCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_night_light_alert, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initializeObjects();
	}	

	private void initializeObjects() {
		switchStatus = (Switch) getView().findViewById(R.id.night_light_alert_switch_status);
		nightLightExposureLevel = (TextView) getView().findViewById(R.id.night_light_alert_exposure_level);
		nightLightDurationValueHour = (TextView) getView().findViewById(R.id.night_light_alert_exposure_duration_hour);
		nightLightDurationValueMin = (TextView) getView().findViewById(R.id.night_light_alert_exposure_duration_minute);
		nightLightStartTimeValue = (TextView) getView().findViewById(R.id.night_light_alert_start_time);
		nightLightEndTimeValue = (TextView) getView().findViewById(R.id.night_light_alert_end_time);

		intializeData();

		switchStatus.setOnCheckedChangeListener(this);
		nightLightStartTimeValue.setOnClickListener(mClickListener);
		nightLightExposureLevel.setOnClickListener(mClickListener);
		nightLightEndTimeValue.setOnClickListener(mClickListener);
		((LinearLayout) getView().findViewById(R.id.night_light_alert_exposure_duration)).setOnClickListener(mClickListener);


		View view = getView();
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (!switchStatus.isChecked()){
					getFragmentManager().popBackStackImmediate();
					return true;
				}
				else{
					if (event.getAction()!=KeyEvent.ACTION_DOWN)
						return true;	
					if( keyCode == KeyEvent.KEYCODE_BACK ) {
						if (switchStatus.isChecked()){
							isdateshow = true;
							compareTime();
						}
						if(noError || flag_ischange == false){
							getFragmentManager().popBackStackImmediate();
							return true;
						}
						else return false;
					} else {
						return false;
					}
				}

			}
		});

		showHideViews();
	}

	private void intializeData() {
		nightLightDetectSetting = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchNightlightSetting = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(NightLightDetectSetting.class);

		if (nightLightDetectSetting.size() > 0) {
			Log.d(TAG, "Has a data");
			mNightLightDetectSetting = nightLightDetectSetting.get(0);
			//mNightLightDetectSetting.update();
			mNightLightDetectSetting.setContext(getActivity());
			int exposure = mNightLightDetectSetting.getExposureLevel();
			String exposureLvl;
			if (exposure == SALNightLightDetectSetting.EXPOSURE_LOW){
				exposureLvl = getString(R.string.low);
			} else if (exposure == SALNightLightDetectSetting.EXPOSURE_MEDIUM) {
				exposureLvl = getString(R.string.medium);
			} else {
				exposureLvl = getString(R.string.high);
			}
			nightLightExposureLevel.setText(exposureLvl);
			int startMinutes = mNightLightDetectSetting.getStartTime();
			int endMinutes = mNightLightDetectSetting.getEndTime();
//			if ((startMinutes > 1440 || startMinutes < 1) || (endMinutes >1440 || endMinutes < 1)){
//				startMinutes = 1140;
//				endMinutes = 1200;
//			}

			nightLightStartTimeValue.setText(changeTimeFormat(convertMinuteToTime(startMinutes)));
			nightLightEndTimeValue.setText(changeTimeFormat(convertMinuteToTime(endMinutes)));
			switchStatus.setChecked(mNightLightDetectSetting.isEnabled());

			int hours = mNightLightDetectSetting.getExposureDuration() / 60;
			int hoursToDisplay = hours;

			if (hours > 12) {
				hoursToDisplay = hoursToDisplay - 12;
			}

			int minutesToDisplay = mNightLightDetectSetting.getExposureDuration() - (hours * 60);

			nightLightDurationValueMin.setText(String.format("%02d", minutesToDisplay));
			nightLightDurationValueHour.setText(""+ hours);
			exposureDurationHourToDisplay = ""+ hours;
			exposureDurationMinToDisplay = String.format("%02d", minutesToDisplay);


			Log.e(TAG, "After convert startTime = " + convertMinuteToTime(mNightLightDetectSetting.getStartTime()));
			Log.e(TAG, "After convert endTime = " + convertMinuteToTime(mNightLightDetectSetting.getEndTime()));
			Log.e(TAG, "After convert hour = " + hoursToDisplay);
			Log.e(TAG, "After convert min = " + String.format("%02d", minutesToDisplay));
		} else 
			initialData();	
	}

	private void initialData() {
		Log.d(TAG, "Initial data");

		nightLightStartTimeValue.setText(changeTimeFormat("" + 7 + ":" + String.format("%02d", 00) + " AM"));
		nightLightEndTimeValue.setText(changeTimeFormat("" + 12 + ":" + String.format("%02d", 00) + " PM"));
		nightLightExposureLevel.setText(getString(R.string.medium));
		//switchStatus.setChecked(true);
		nightLightDurationValueHour.setText(""+5);

		mNightLightDetectSetting = new NightLightDetectSetting();

		Date fDate = null;
		Date lDate = null; 
		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
		case TIME_FORMAT_12_HR:
			fDate = returnDate(nightLightEndTimeValue.getText().toString());
			lDate = returnDate(nightLightStartTimeValue.getText().toString());
			break;
		case TIME_FORMAT_24_HR:
			fDate = returnDate24Hours(nightLightEndTimeValue.getText().toString());
			lDate = returnDate24Hours(nightLightStartTimeValue.getText().toString());
			break;
		}

		Calendar fcalendar = Calendar.getInstance();
		fcalendar.setTime(fDate);
		int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
		int fminutes = fcalendar.get(Calendar.MINUTE);

		Calendar ecalendar = Calendar.getInstance();
		ecalendar.setTime(lDate);
		int ehours = ecalendar.get(Calendar.HOUR_OF_DAY);
		int eminutes = ecalendar.get(Calendar.MINUTE);

		int endminutes = (fhours*60) + fminutes;
		int startminutes = (ehours*60) + eminutes;


		int exposureLevel = 0;
		if (nightLightExposureLevel.getText().toString().equals(getString(R.string.low)))
			exposureLevel = SALNightLightDetectSetting.EXPOSURE_LOW;
		else if (nightLightExposureLevel.getText().toString().equals(getString(R.string.medium)))
			exposureLevel = SALNightLightDetectSetting.EXPOSURE_MEDIUM;
		else
			exposureLevel = SALNightLightDetectSetting.EXPOSURE_HIGH;


		mNightLightDetectSetting.setEnabled(switchStatus.isChecked());

		mNightLightDetectSetting.setStartTime(startminutes);
		mNightLightDetectSetting.setEndTime(endminutes);
		mNightLightDetectSetting.setExposureDuration((Integer.parseInt(nightLightDurationValueHour.getText().toString()) *60) + Integer.parseInt(nightLightDurationValueMin.getText().toString()));
		mNightLightDetectSetting.setExposureLevel(exposureLevel);
		
		mNightLightDetectSetting.insert();

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
			displayValue = displayValue + " " + getString(R.string.pm);;

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

	// Switching status
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()){
		case R.id.night_light_alert_switch_status:
			//Toast.makeText(getActivity(), "The Switch is " + (isChecked ? "on" : "off"),Toast.LENGTH_SHORT).show();
			showHideViews();
			isdateshow = true;
			flag_ischange = true;
			break;
		}
	}

	private void showHideViews() {
		if(switchStatus.isChecked() == true){
			nightLightExposureLevel.setVisibility(View.VISIBLE);
			nightLightDurationValueHour.setVisibility(View.VISIBLE);
			nightLightDurationValueMin.setVisibility(View.VISIBLE);
			nightLightStartTimeValue.setVisibility(View.VISIBLE);
			nightLightEndTimeValue.setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.night_light_alert_h)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.night_light_alert_m)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.night_light_exposure_level_hint)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.night_light_exposure_duration_hint)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.night_light_start_time_hint)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.night_light_end_time_hint)).setVisibility(View.VISIBLE);
			((View) getView().findViewById(R.id.night_light_line1)).setVisibility(View.VISIBLE);
			((View) getView().findViewById(R.id.night_light_line1)).setVisibility(View.VISIBLE);
			((View) getView().findViewById(R.id.night_light_line2)).setVisibility(View.VISIBLE);
			((View) getView().findViewById(R.id.night_light_line3)).setVisibility(View.VISIBLE);
			((View) getView().findViewById(R.id.night_light_line4)).setVisibility(View.VISIBLE);
			((View) getView().findViewById(R.id.night_light_line5)).setVisibility(View.VISIBLE);
		}else{
			nightLightExposureLevel.setVisibility(View.GONE);
			nightLightDurationValueHour.setVisibility(View.GONE);
			nightLightDurationValueMin.setVisibility(View.GONE);
			nightLightStartTimeValue.setVisibility(View.GONE);
			nightLightEndTimeValue.setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.night_light_alert_h)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.night_light_alert_m)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.night_light_exposure_level_hint)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.night_light_exposure_duration_hint)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.night_light_start_time_hint)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.night_light_end_time_hint)).setVisibility(View.GONE);
			((View) getView().findViewById(R.id.night_light_line1)).setVisibility(View.GONE);
			((View) getView().findViewById(R.id.night_light_line1)).setVisibility(View.GONE);
			((View) getView().findViewById(R.id.night_light_line2)).setVisibility(View.GONE);
			((View) getView().findViewById(R.id.night_light_line3)).setVisibility(View.GONE);
			((View) getView().findViewById(R.id.night_light_line4)).setVisibility(View.GONE);
			((View) getView().findViewById(R.id.night_light_line5)).setVisibility(View.GONE);
		}
	}

	// TimePicker
	private final TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			String AM_PM ;
			if(hourOfDay < 12) {
				AM_PM = getString(R.string.am);;

			} else {
				AM_PM = getString(R.string.pm);;
				hourOfDay=hourOfDay-12;
			}
			if (isStartTime) {
				nightLightStartTimeValue.setText(changeTimeFormat(hourOfDay + ":" + String.format("%02d", minute) + " " + AM_PM));
				isStartTime = false;
				//compareTime();
			}
			else if (isEndTime) {
				nightLightEndTimeValue.setText(changeTimeFormat(hourOfDay + ":" + String.format("%02d", minute) + " " + AM_PM));
				isEndTime = false;
				//compareTime();
			} 

		}
	};

	// OnClick Listener
	private final View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			boolean is24hours = false;
			String ampm;
			int finalHour;
			switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				is24hours = false;
				break;
			case TIME_FORMAT_24_HR:
				is24hours = true;
				break;					
			}

			switch (v.getId()) {
			case R.id.night_light_alert_exposure_duration:
				flag_ischange = true;
				isdateshow = true;

				alertDialog = new Dialog(getActivity());
				alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
				alertDialog.setContentView(R.layout.number_picker_custom_dialog_layout);

				alertDialog.findViewById(R.id.number_picker_custom_dialog_set).setOnClickListener(alertListener);
				alertDialog.findViewById(R.id.number_picker_custom_dialog_cancel).setOnClickListener(alertListener);

				// Hours
				nightLightNumberPickerHour = (NumberPicker) alertDialog.findViewById(R.id.number_picker_custom_dialog_hour);
				nightLightNumberPickerHour.setMaxValue(2);       
				nightLightNumberPickerHour.setMinValue(0);         
				nightLightNumberPickerHour.setValue(Integer.parseInt(exposureDurationHourToDisplay));
				nightLightNumberPickerHour.setWrapSelectorWheel(true);
				nightLightNumberPickerHour.setOnValueChangedListener( new NumberPicker.
						OnValueChangeListener() {
					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						int minVal = Integer.valueOf(exposureDurationMinToDisplay);
						int hourVal = newVal;
						Log.e(TAG, "Selected number is "+ exposureDurationHourToDisplay);    
						if (minVal < 10 && hourVal == 0) {
							minVal = 10;
						} else if (hourVal == 2 && minVal > 0) {
							minVal = 0;
							//showAlert(getResources().getString(R.string.invalid_duration_time));
						}

						exposureDurationHourToDisplay = Integer.toString(hourVal);
						nightLightNumberPickerHour.setValue(hourVal);
						exposureDurationMinToDisplay = Integer.toString(minVal);
						nightLightNumberPickerMin.setValue(minVal);
						//nightLightDurationValueHour.setText(exposureDurationHourToDisplay);
						//nightLightDurationValueMin.setText(exposureDurationMinToDisplay);
						Log.e(TAG, "Selected number is "+ exposureDurationHourToDisplay);
						flag_ischange = true;
					}
				});

				// Minutes
				nightLightNumberPickerMin = (NumberPicker) alertDialog.findViewById(R.id.number_picker_custom_dialog_minutes);
				nightLightNumberPickerMin.setMaxValue(59);       
				nightLightNumberPickerMin.setMinValue(0);         
				nightLightNumberPickerMin.setValue(Integer.parseInt(exposureDurationMinToDisplay)); //Integer.parseInt(nightLightDurationValueMin.getText().toString())
				nightLightNumberPickerMin.setWrapSelectorWheel(true);
				nightLightNumberPickerMin.setOnValueChangedListener( new NumberPicker.OnValueChangeListener() {
					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						int minVal = newVal;
						int hourVal = Integer.valueOf(exposureDurationHourToDisplay);
						Log.e(TAG, "Selected number is "+ exposureDurationHourToDisplay);    
						if (minVal < 10 && hourVal == 0) {
							minVal = 10;
						} else if (hourVal == 2 && minVal > 0) {
							minVal = 0;
							//showAlert(getResources().getString(R.string.invalid_duration_time));
						}

						exposureDurationHourToDisplay = Integer.toString(hourVal);
						nightLightNumberPickerHour.setValue(hourVal);
						exposureDurationMinToDisplay = Integer.toString(minVal);
						nightLightNumberPickerMin.setValue(minVal);
						//nightLightDurationValueHour.setText(exposureDurationHourToDisplay);
						//nightLightDurationValueMin.setText(exposureDurationMinToDisplay);
						flag_ischange = true;
					}
				});

				alertDialog.setCancelable(false);
				alertDialog.setCanceledOnTouchOutside(true);
				alertDialog.show();		
				break;
			case R.id.night_light_alert_start_time:
				isdateshow = true;
				isStartTime = true;
				String timeStart  = nightLightStartTimeValue.getText().toString().trim(); 
				String[] temp = timeStart.split(" ");
				String[] timeSplitData = temp[0].split(":");
				int hour = Integer.parseInt(timeSplitData[0]);
				int mins = Integer.parseInt(timeSplitData[1]);
				if (!is24hours) {
					if (temp[1].equals(getString(R.string.pm))) {
						hour += 12;
					}
				}
				mTimePicker = new TimePickerDialog(getActivity(), mOnTimeSetListener, hour, mins, is24hours);
				mTimePicker.show();
				break;
			case R.id.night_light_alert_end_time:
				isdateshow = true;
				isEndTime = true;
				String timeEnd  = nightLightEndTimeValue.getText().toString().trim(); 
				String[] tempEnd = timeEnd.split(" ");
				String[] timeSplitDataEnd = tempEnd[0].split(":");
				int hourEnd = Integer.parseInt(timeSplitDataEnd[0]);
				int minsEnd = Integer.parseInt(timeSplitDataEnd[1]);
				if (!is24hours) {
					if (tempEnd[1].equals(getString(R.string.pm))) {
						hourEnd += 12;
					}
				}
				mTimePicker = new TimePickerDialog(getActivity(), mOnTimeSetListener, hourEnd, minsEnd, is24hours);
				mTimePicker.show();
				break;
			case R.id.night_light_alert_exposure_level:
				showListExposureDialog();
				break;
			}	
		}
	}; 


	// OnClick Listener for Custom Alert Dialog
	private final View.OnClickListener alertListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.number_picker_custom_dialog_set:
				if(exposureDurationHourToDisplay == "")
					exposureDurationHourToDisplay = "0";
				if(exposureDurationMinToDisplay == "")
					exposureDurationMinToDisplay = "00";
				nightLightDurationValueHour.setText(exposureDurationHourToDisplay);
				nightLightDurationValueMin.setText(exposureDurationMinToDisplay);
				alertDialog.dismiss();
				break;
			case R.id.number_picker_custom_dialog_cancel:
				alertDialog.dismiss();
				break;
			}
		}
	};

	protected void compareTime() {
		noError = false;
		if (isdateshow){
			isdateshow = false;
			flag_ischange = true;

			Date fDate = null;
			Date lDate = null; 
			switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				lDate = returnDate(nightLightEndTimeValue.getText().toString());
				fDate = returnDate(nightLightStartTimeValue.getText().toString());
				break;
			case TIME_FORMAT_24_HR:
				lDate = returnDate24Hours(nightLightEndTimeValue.getText().toString());
				fDate = returnDate24Hours(nightLightStartTimeValue.getText().toString());
				break;
			}


			boolean isthirtymin = false;
			int mhours, mminutes;

			Calendar fcalendar = Calendar.getInstance();
			fcalendar.setTime(fDate);
			int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
			int fminutes = fcalendar.get(Calendar.MINUTE);

			Calendar ecalendar = Calendar.getInstance();
			ecalendar.setTime(lDate);
			int ehours = ecalendar.get(Calendar.HOUR_OF_DAY);
			int eminutes = ecalendar.get(Calendar.MINUTE);

			//			eminutes = eminutes - fminutes;
			//			if (eminutes < 0){
			//				eminutes = eminutes + 60;
			//				ehours = ehours -1;
			//			}
			//			nightLightDurationValueHour.setText("" + (ehours - fhours));
			//			nightLightDurationValueMin.setText("" + eminutes);

			mminutes = fminutes + 29;
			mhours = fhours;
			if (mminutes > 59){
				mhours = fhours + 1;
				mminutes = fminutes - 59;
			}
			if ((mminutes + (mhours*60)) >= ((ehours*60) + eminutes))
				isthirtymin = true;

			if (fDate.getTime() > lDate.getTime()){
				showAlert(getString(R.string.daylight_exposure_error1));
				setInitialDate(fhours, fminutes);
			}
			else if (fDate.getTime() == lDate.getTime()){
				showAlert(getString(R.string.daylight_exposure_error2));
				setInitialDate(fhours, fminutes);
			}
			else if (isthirtymin){
				showAlert(getString(R.string.daylight_exposure_error3));
				setInitialDate(eminutes, ehours, fhours, fminutes);
			}
			else if ((ehours*60 + eminutes) - (fhours*60 + fminutes) < (Integer.parseInt(exposureDurationHourToDisplay)*60 + Integer.parseInt(exposureDurationMinToDisplay))){
				showAlert(getString(R.string.daylight_exposure_error4));
				//setInitialDate(eminutes, ehours, fhours, fminutes);
				isdateshow = true;
				nightLightDurationValueHour.setText("" + (ehours - fhours));
				nightLightDurationValueMin.setText("" + eminutes);
			}
			else //valid time input
			{
                dayLightDetecttSettings = DataSource.getInstance(getActivity())
                        .getReadOperation()
                        .query("watchDaylightSetting = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
                        .getResults(DayLightDetectSetting.class);

                if (dayLightDetecttSettings.size() > 0){
                    mDayLightDetectSetting = dayLightDetecttSettings.get(0);
                    if  ( mDayLightDetectSetting.isEnabled()){
						String dayLightEndtime = convertMinuteToTime(mDayLightDetectSetting.getEndTime());
						String dayLightStarttime = convertMinuteToTime(mDayLightDetectSetting.getStartTime());

						SimpleDateFormat input = new SimpleDateFormat("hh:mm aa");
						SimpleDateFormat output = new SimpleDateFormat("HH:mm:ss");
						Date dt = null;
						try {
							dt = input.parse(dayLightEndtime);
						} catch (ParseException e) {
							e.printStackTrace();
						}


						String formattedDayLightEndtime = output.format(dt);
						try {
							dt = input.parse(dayLightStarttime);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						String formattedDayLightStartTime = output.format(dt);

						String formattedNightlightStartTime = null;
						String formattedNightlightEndTime = null;

						switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
							case TIME_FORMAT_12_HR:
								String nightLightStartTime = nightLightStartTimeValue.getText().toString();
								String nightLightEndTime = nightLightEndTimeValue.getText().toString();

								try {
									dt = input.parse(nightLightStartTime);
								} catch (ParseException e) {
									e.printStackTrace();
								}
								formattedNightlightStartTime = output.format(dt);

								try {
									dt = input.parse(nightLightEndTime);
								} catch (ParseException e) {
									e.printStackTrace();
								}
								formattedNightlightEndTime = output.format(dt);

								break;
							case TIME_FORMAT_24_HR:
								formattedNightlightStartTime = nightLightStartTimeValue.getText().toString();
								formattedNightlightEndTime = nightLightEndTimeValue.getText().toString();
								break;
						}


						boolean nightStartTime = false;
						boolean nightlightEndTime = false;
						try {
							nightStartTime = isTimeBetweenTwoTime(formattedDayLightStartTime, formattedDayLightEndtime,formattedNightlightStartTime );
							nightlightEndTime = isTimeBetweenTwoTime(formattedDayLightStartTime, formattedDayLightEndtime,formattedNightlightEndTime );
						} catch (ParseException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e){

						}

						if (nightStartTime == true || nightlightEndTime == true){
							isdateshow = true;
							noError = false;
							showAlert(getString(R.string.daylight_exposure_error7));
						}
						else{
							noError = true;
						}


//                    Calendar calendarNightLightEndTime = Calendar.getInstance();
//                    calendarNightLightEndTime.setTime(lDate);
//                    Calendar calendarNightLightStartTime = Calendar.getInstance();
//                    calendarNightLightStartTime.setTime(fDate);
//
//                    SimpleDateFormat  format =  new SimpleDateFormat("HH:mm");
//
//                    Calendar calendarDayLightEndTime = Calendar.getInstance();
//                    try {
//                        calendarDayLightEndTime.setTime(format.parse(changeTimeFormat(convertMinuteToTime(mDayLightDetectSetting.getEndTime()))));
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//
//                    Calendar calendarDayLightStartTime = Calendar.getInstance();
//                    try {
//                        calendarDayLightStartTime.setTime(format.parse(changeTimeFormat(convertMinuteToTime(mDayLightDetectSetting.getStartTime()))));
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//
//                    Date startTime =calendarNightLightStartTime.getTime();
//                    Date endTime =calendarNightLightEndTime.getTime();
//                        boolean isNightStartTimeInDayLight =startTime.after(calendarDayLightEndTime.getTime());
//                        boolean isNightEndTimeInDayLight = endTime.after(calendarDayLightEndTime.getTime());
//                    if (isNightStartTimeInDayLight == false || isNightEndTimeInDayLight == false){
//                        noError = false;
//                        isdateshow = true;
//                        showAlert(getString(R.string.daylight_exposure_error7));
//                    }
//                    else{
//                        noError = true;
//                    }
                    }
                    else
                        noError = true;
                }
                else {
                    noError = true;
                }
				//nightLightDurationValue.setText("" +(ehours - fhours) + " h " + eminutes + " m"  );
			}
		}
	}

	private boolean isTimeBetweenTwoTime(String argStartTime,String argEndTime, String argCurrentTime) throws ParseException {
		String reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
		//
		if (argStartTime.matches(reg) && argEndTime.matches(reg)
				&& argCurrentTime.matches(reg)) {
			boolean valid = false;
			// Start Time
			java.util.Date startTime = new SimpleDateFormat("HH:mm:ss")
					.parse(argStartTime);
			Calendar startCalendar = Calendar.getInstance();
			startCalendar.setTime(startTime);

			// Current Time
			java.util.Date currentTime = new SimpleDateFormat("HH:mm:ss")
					.parse(argCurrentTime);
			Calendar currentCalendar = Calendar.getInstance();
			currentCalendar.setTime(currentTime);

			// End Time
			java.util.Date endTime = new SimpleDateFormat("HH:mm:ss")
					.parse(argEndTime);
			Calendar endCalendar = Calendar.getInstance();
			endCalendar.setTime(endTime);

			//
			if (currentTime.compareTo(endTime) < 0) {

				currentCalendar.add(Calendar.DATE, 1);
				currentTime = currentCalendar.getTime();

			}

			if (startTime.compareTo(endTime) < 0) {

				startCalendar.add(Calendar.DATE, 1);
				startTime = startCalendar.getTime();

			}
			//
			if (currentTime.before(startTime)) {

				System.out.println(" Time is Lesser ");

				valid = false;
			} else {

				if (currentTime.after(endTime)) {
					endCalendar.add(Calendar.DATE, 1);
					endTime = endCalendar.getTime();

				}

				System.out.println("Comparing , Start Time /n " + startTime);
				System.out.println("Comparing , End Time /n " + endTime);
				System.out
						.println("Comparing , Current Time /n " + currentTime);

				if (currentTime.before(endTime)) {
					System.out.println("RESULT, Time lies b/w");
					valid = true;
				} else {
					valid = false;
					System.out.println("RESULT, Time does not lies b/w");
				}

			}
			return valid;

		} else {
			throw new IllegalArgumentException(
					"Not a valid time, expecting HH:MM:SS format");
		}

	}


	private void setInitialDate(int eminutes, int ehours, int fhours,int fminutes){
		eminutes = fminutes + 30;
		if (eminutes >= 60){
			eminutes = eminutes - 60;
			ehours = ehours+1;
		}
		nightLightDurationValueHour.setText("" + (ehours - fhours));
		nightLightDurationValueMin.setText("" + (eminutes - fminutes));

		String AM_PM ;
		if(ehours < 12) {
			AM_PM = getString(R.string.am);

		} else {
			AM_PM = getString(R.string.pm);
			ehours=ehours-12;
		}

		if (ehours == 0)
			ehours = 12;
		isdateshow = true;
		nightLightEndTimeValue.setText(changeTimeFormat(ehours + ":" + String.format("%02d", eminutes) + " " + AM_PM));
	}

	private void setInitialDate(int fhours,int fminutes){
		fminutes = fminutes + 30;
		if (fminutes >= 60){
			fminutes = fminutes - 60;
			fhours = fhours+1;
		}
		nightLightDurationValueHour.setText("0");
		nightLightDurationValueMin.setText("30");

		String AM_PM ;
		if(fhours < 12) {
			AM_PM = getString(R.string.am);

		} else {
			AM_PM = getString(R.string.pm);
			fhours=fhours-12;
		}

		if (fhours == 0)
			fhours = 12;
		isdateshow = true;
		nightLightEndTimeValue.setText(changeTimeFormat(fhours + ":" + String.format("%02d", fminutes) + " " + AM_PM));
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

	private void showAlert(String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message)
		.setTitle(getString(R.string.error_text));
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}

		}); 
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (flag_ischange)
			syncData();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if(isRemoving()) {
			if(flag_ischange)
				syncData();
			//noticeBeforeExit();
		}
	}

	private void showListExposureDialog(){
		final CharSequence[] items = {
				getString(R.string.low),
				getString(R.string.medium),
				getString(R.string.high)
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.daylight_exposure_select));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				flag_ischange = true;
				nightLightExposureLevel.setText(items[item]);
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void syncData() {
		if (flag_ischange) {
			Log.d(TAG, "sync Data");
			Date fDate = null;
			Date lDate = null; 
			switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				fDate = returnDate(nightLightEndTimeValue.getText().toString());
				lDate = returnDate(nightLightStartTimeValue.getText().toString());
				break;
			case TIME_FORMAT_24_HR:
				fDate = returnDate24Hours(nightLightEndTimeValue.getText().toString());
				lDate = returnDate24Hours(nightLightStartTimeValue.getText().toString());
				break;
			}

			Calendar fcalendar = Calendar.getInstance();
			fcalendar.setTime(fDate);
			int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
			int fminutes = fcalendar.get(Calendar.MINUTE);

			Calendar ecalendar = Calendar.getInstance();
			ecalendar.setTime(lDate);
			int ehours = ecalendar.get(Calendar.HOUR_OF_DAY);
			int eminutes = ecalendar.get(Calendar.MINUTE);

			int endminutes = (fhours*60) + fminutes;
			int startminutes = (ehours*60) + eminutes;


			int exposureLevel = 0;
			if (nightLightExposureLevel.getText().toString().equals(getString(R.string.low)))
				exposureLevel = SALNightLightDetectSetting.EXPOSURE_LOW;
			else if (nightLightExposureLevel.getText().toString().equals(getString(R.string.medium)))
				exposureLevel = SALNightLightDetectSetting.EXPOSURE_MEDIUM;
			else
				exposureLevel = SALNightLightDetectSetting.EXPOSURE_HIGH;


			mNightLightDetectSetting.setEnabled(switchStatus.isChecked());
			if (switchStatus.isChecked()){
				mNightLightDetectSetting.setStartTime(startminutes);
				mNightLightDetectSetting.setEndTime(endminutes);
				mNightLightDetectSetting.setExposureDuration((Integer.parseInt(nightLightDurationValueHour.getText().toString()) *60) + Integer.parseInt(nightLightDurationValueMin.getText().toString()));
				mNightLightDetectSetting.setExposureLevel(exposureLevel);
			}

			if (nightLightDetectSetting.size() > 0) {
				mNightLightDetectSetting.update();
			} else {
				mNightLightDetectSetting.insert();
			}

		}	
	}


	@Override
	public void onDeviceConnected(BluetoothDevice device,
			SALBLEService service, Watch watch) {

	}




}